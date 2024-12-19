/*
 * Copyright (C) 2020 Nicola De Nisco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.sirio6.services;

import com.workingdogs.village.Record;
import com.workingdogs.village.TableDataSet;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Role;
import org.apache.torque.util.Transaction;
import org.commonlib5.lambda.FunctionTrowException;
import org.commonlib5.lambda.LEU;
import org.commonlib5.utils.JavaLoggingToCommonLoggingRedirector;
import org.rigel5.db.DbUtils;
import org.sirio6.utils.SU;

/**
 * Controllo dei parametri fondamentali per il corretto funzionamento dell'applicazione.
 *
 * @author Nicola De Nisco
 */
public class CoreAppSanity
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreAppSanity.class);

  protected void sanityApplication(AbstractCoreBaseService service)
     throws Exception
  {
    sanitySystem(service);
    sanityJava(service);
    sanityDatabase(service);
    sanitySecurity(service);
    sanityScheduler(service);
    sanityDir(service);
  }

  /**
   * Effettua controlli sul sistema per evidenziare
   * situazioni non conformi al funzionamento dell'applicazione.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanitySystem(AbstractCoreBaseService service)
     throws Exception
  {
    // redirige il logger standard di Java all'interno di Log4j
    JavaLoggingToCommonLoggingRedirector.activate();

    // imposta il truststore di default
    CoreTlsManager.getInstance().initTLScomunication();

    log.info("sanitySystem superato");
  }

  /**
   * Effettua controlli sull'ambiente java per evidenziare
   * situazioni non conformi al funzionamento dell'applicazione.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityJava(AbstractCoreBaseService service)
     throws Exception
  {
    // controlla la presenza di java e javac nella path
    // di sistema: vengono utilizzati da sottoprogrammi
    // lanciati esternamente (tipo Jasper)

    boolean found = false;
    String path = System.getenv("PATH");
    String[] dirs = SU.split(path, File.pathSeparatorChar);
    for(int i = 0; i < dirs.length; i++)
    {
      log.debug("Test " + dirs[i] + " for java/javac");
      File fDir = new File(dirs[i]);
      if(!fDir.isDirectory())
        continue;

      File testJava = new File(fDir, "java");
      File testJavac = new File(fDir, "javac");
      if(testJava.exists() && testJavac.exists())
      {
        found = true;
        break;
      }

      File testJavaw = new File(fDir, "java.exe");
      File testJavacw = new File(fDir, "javac.exe");
      if(testJavaw.exists() && testJavacw.exists())
      {
        found = true;
        break;
      }
    }

    if(!found)
      throw new CoreServiceException("I programmi java e/o javac non sono presenti nella path.");

    log.info("sanityJava superato");
  }

  /**
   * Verifica e regolarizza il database.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityDatabase(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Verifica le impostazioni di sicurezza di base.
   * L'utente turbine deve esistere ed avere id=0.
   * Il ruolo turbine_root deve esistere ad avere id=1.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanitySecurity(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Verifica e regolarizza il file system.
   * Cancella eventuali directory temporanee o cose simili.
   * @param service
   * @throws Exception
   */
  protected void sanityDir(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Verifica presenza e setup dei job dello scheduler.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityScheduler(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Assegna tutti i permessi al ruolo indicato.
   * @param role ruolo amministratore
   * @throws Exception
   */
  protected void grantAllPermission(Role role)
     throws Exception
  {
    executeInTransaction((con) -> grantAllPermission(con, role));
  }

  protected boolean grantAllPermission(Connection connection, Role role)
     throws Exception
  {
    String sSQL
       = "select permission_id\n"
       + "  from turbine_permission\n"
       + " where permission_id NOT IN ("
       + "    select permission_id from turbine_role_permission where role_id=" + role.getId() + ")";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, connection);
    if(lsRecs.isEmpty())
      return false;

    int[] permissionToGrant = lsRecs.stream()
       .mapToInt(LEU.rethrowFunctionInt((r) -> r.getValue(1).asInt()))
       .distinct().sorted().toArray();
    if(permissionToGrant.length == 0)
      return false;

    String sINS
       = "INSERT INTO turbine_role_permission(\n"
       + "	role_id, permission_id)\n"
       + "	VALUES (?, ?);";
    try(PreparedStatement ps = connection.prepareStatement(sINS))
    {
      int roleid = (Integer) role.getId();
      for(int i = 0; i < permissionToGrant.length; i++)
      {
        int permid = permissionToGrant[i];

        ps.clearParameters();
        ps.setInt(1, roleid);
        ps.setInt(2, permid);
        ps.executeUpdate();
      }
    }

    return true;
  }

  /**
   * Crea ruoli se non esistono.
   * @param roleNames elenco di nomi di ruolo
   * @throws Exception
   */
  protected void createRoles(String... roleNames)
     throws Exception
  {
    executeInTransaction((con) -> createRoles(con, roleNames));
  }

  protected boolean createRoles(Connection con, String[] roleNames)
     throws Exception
  {
    String sSQL
       = "SELECT *"
       + "  FROM turbine_role";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, con);

    int roleid = lsRecs.stream().mapToInt(
       LEU.rethrowFunctionInt((r) -> r.getValue("role_id").asInt())).max().orElse(0) + 1;

    String sINS
       = "INSERT INTO turbine_role(\n"
       + "	role_id, role_name, objectdata)\n"
       + "	VALUES (?, ?, NULL);";
    try(PreparedStatement ps = con.prepareStatement(sINS))
    {
      for(int i = 0; i < roleNames.length; i++)
      {
        String roleName = roleNames[i];
        if(lsRecs.stream()
           .anyMatch(LEU.rethrowPredicate((r) -> SU.isEqu(roleName, r.getValue("role_name").asString()))))
          continue;

        ps.clearParameters();
        ps.setInt(1, roleid++);
        ps.setString(2, roleName);
        ps.executeUpdate();
      }
    }

    return true;
  }

  /**
   * Crea permessi se non esistono.
   * @param permissionNames elenco di nomi di ruolo
   * @throws Exception
   */
  protected void createPermissions(String... permissionNames)
     throws Exception
  {
    executeInTransaction((con) -> createPermissions(con, permissionNames));
  }

  protected boolean createPermissions(Connection con, String[] permissionNames)
     throws Exception
  {
    String sSQL
       = "SELECT *"
       + "  FROM turbine_permission";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, con);

    int permissionid = lsRecs.stream().mapToInt(
       LEU.rethrowFunctionInt((r) -> r.getValue("permission_id").asInt())).max().orElse(0) + 1;

    String sINS
       = "INSERT INTO turbine_permission(\n"
       + "	permission_id, permission_name, objectdata)\n"
       + "	VALUES (?, ?, NULL);";
    try(PreparedStatement ps = con.prepareStatement(sINS))
    {
      for(int i = 0; i < permissionNames.length; i++)
      {
        String permissionName = permissionNames[i];
        if(lsRecs.stream()
           .anyMatch(LEU.rethrowPredicate((r) -> SU.isEqu(permissionName, r.getValue("permission_name").asString()))))
          continue;

        ps.clearParameters();
        ps.setInt(1, permissionid++);
        ps.setString(2, permissionName);
        ps.executeUpdate();
      }
    }

    return true;
  }

  /**
   * Assegna permessi al ruolo indicato.
   * @param role ruolo
   * @param permissions elenco permessi
   * @throws Exception
   */
  protected void grantPermission(Role role, String... permissions)
     throws Exception
  {
    executeInTransaction((con) -> grantPermission(con, role, permissions));
  }

  protected boolean grantPermission(Connection connection, Role role, String... permissions)
     throws Exception
  {
    String sSQL
       = "select permission_id,permission_name\n"
       + "  from turbine_permission\n"
       + " where permission_id NOT IN ("
       + "    select permission_id from turbine_role_permission where role_id=" + role.getId() + ")";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, connection);
    if(lsRecs.isEmpty())
      return false;

    String sINS
       = "INSERT INTO turbine_role_permission(\n"
       + "	role_id, permission_id)\n"
       + "	VALUES (?, ?);";
    try(PreparedStatement ps = connection.prepareStatement(sINS))
    {
      int roleid = (Integer) role.getId();
      for(String perm : permissions)
      {
        Record rPerm = lsRecs.stream()
           .filter(LEU.rethrowPredicate((r) -> perm.equals(r.getValue(2).asString())))
           .findFirst().orElse(null);

        if(rPerm == null)
          continue;

        int permid = rPerm.getValue(1).asInt();

        ps.clearParameters();
        ps.setInt(1, roleid);
        ps.setInt(2, permid);
        ps.executeUpdate();
      }
    }

    return true;
  }

  protected void createUsers(String... utenti)
     throws Exception
  {
    executeInTransaction((con) -> createUsers(con, utenti));
  }

  protected boolean createUsers(Connection con, String... utenti)
     throws Exception
  {
    try(TableDataSet td = new TableDataSet(con, "turbine_user"))
    {
      for(String loginName : utenti)
      {
        td.clear();
        td.where("login_name='" + loginName + "'");
        td.fetchRecords();

        Record r;
        if(td.size() == 0)
        {
          r = td.addRecord();
          long userID = DbUtils.getMaxField("turbine_user", "user_id", con);
          r.setValue("user_id", userID);
          r.setValue("login_name", loginName);
          r.setValue("password_value", "disattiva123456!!!");
          r.setValue("first_name", loginName);
          r.setValue("last_name", loginName);

          long nextID = td.getNextID();
          r.setValue("user_id", nextID);
          r.save();
        }
      }
    }

    return true;
  }

  protected void grantRole(String logonUser, String groupName, String roleName)
     throws Exception
  {
    executeInTransaction((con) -> grantRole(con, logonUser, groupName, roleName));
  }

  protected boolean grantRole(Connection con, String loginName, String groupName, String roleName)
     throws Exception
  {
    int[] idu = DbUtils.queryForID(con, "SELECT user_id  FROM turbine_user  WHERE login_name='" + loginName + "'");
    int[] idg = DbUtils.queryForID(con, "SELECT group_id FROM turbine_group WHERE group_name='" + groupName + "'");
    int[] idr = DbUtils.queryForID(con, "SELECT role_id  FROM turbine_role  WHERE role_name='" + roleName + "'");

    if(idu.length < 1 || idg.length < 1 || idr.length < 1)
      return false;

    try(PreparedStatement ps = con.prepareStatement(
       "SELECT * FROM turbine_user_group_role\n"
       + "WHERE user_id=? AND group_id=? AND role_id=?"))
    {
      ps.setInt(1, idu[0]);
      ps.setInt(2, idg[0]);
      ps.setInt(3, idr[0]);
      if(ps.executeQuery().next())
        return false;
    }

    try(PreparedStatement ps = con.prepareStatement(
       "INSERT INTO turbine_user_group_role(\n"
       + "	user_id, group_id, role_id)\n"
       + "	VALUES (?, ?, ?);"))
    {
      ps.setInt(1, idu[0]);
      ps.setInt(2, idg[0]);
      ps.setInt(3, idr[0]);
      ps.executeUpdate();
    }

    return true;
  }

  protected void executeInTransaction(FunctionTrowException<Connection, Boolean> toRun)
     throws Exception
  {
    Connection connection = Transaction.begin();
    try
    {
      if(!toRun.apply(connection))
      {
        Transaction.safeRollback(connection);
        return;
      }

      Transaction.commit(connection);
    }
    catch(Exception ex)
    {
      Transaction.safeRollback(connection);
      throw ex;
    }
  }
}

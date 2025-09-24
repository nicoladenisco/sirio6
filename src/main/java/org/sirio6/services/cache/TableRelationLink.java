/*
 * Copyright (C) 2024 Nicola De Nisco
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
package org.sirio6.services.cache;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.map.ForeignKeyMap;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.Persistent;
import org.commonlib5.lambda.ConsumerThrowException;
import org.commonlib5.lambda.FunctionTrowException;
import org.commonlib5.lambda.PredicateThrowException;
import org.rigel5.db.torque.TableMapHelper;
import org.sirio6.utils.SU;

/**
 * Caricamento ottimizzato di tabelle collegate.
 *
 * @author Nicola De Nisco
 * @param <T> Tipo di oggetti recuperati
 * @param <O> Tipo di oggetti origine
 */
public interface TableRelationLink<T extends Persistent, O extends Persistent>
   extends Iterable<T>
{
  public static final int TARGET_UNDEFINED = 0;
  public static final int TARGET_DETAIL = 1;
  public static final int TARGET_MASTER = 2;

  /**
   * Recupera metodo per chiave primaria.
   * A causa del cambio nome in Torque 5.1 e successivi
   * qui vengono cercati entrambi i nomi utilizzati nelle varie versioni di Torque.
   * @param targetPeerClass
   * @return
   */
  public default Method getMetodPrimary(Class targetPeerClass)
  {
    Method m;

    if((m = getMetodPrimary("retrieveByPKs", targetPeerClass)) == null)
      if((m = getMetodPrimary("retrieveByObjectKeys", targetPeerClass)) == null)
        throw new RuntimeException("L'oggetto non implementa il recupero da chiavi primarie.");

    return m;
  }

  public default Method getMetodPrimary(String name, Class targetPeerClass)
  {

    return getMetodQuiet(name, targetPeerClass, Collection.class, Connection.class);
  }

  public default Method getMetodQuiet(String name, Class targetPeerClass, Class... parametri)
  {
    try
    {
      return targetPeerClass.getMethod(name, parametri);
    }
    catch(NoSuchMethodException noSuchMethodException)
    {
      return null;
    }
    catch(SecurityException securityException)
    {
      throw securityException;
    }
  }

  /**
   * Analizza le relazioni fra le due tabelle
   * e determina se il collegamento rispetto al target
   * è master o detail.
   * @param lsOther lista di altri oggetti
   * @param targetTableName nome tabella del target
   * @param targetTableMap tablemap del target
   * @return una delle costanti TARGET_...
   * @throws Exception
   */
  public default int getRelationType(Collection<O> lsOther, String targetTableName, TableMap targetTableMap)
     throws Exception
  {
    if(lsOther.isEmpty())
      return TARGET_UNDEFINED;

    O primo = lsOther.iterator().next();
    TableMapHelper tmOther = TableMapHelper.getByObject(primo);
    String otherTableName = tmOther.getNomeTabella();

    for(ForeignKeyMap fkm : tmOther.getTmap().getForeignKeys())
    {
      if(fkm.getForeignTableName().equals(targetTableName))
      {
        if(fkm.getColumns().size() != 1)
          throw new Exception("troppe colonne in chiave esterna: solo una supportata");

        return TARGET_DETAIL;
      }
    }

    for(ForeignKeyMap fkm : targetTableMap.getForeignKeys())
    {
      if(fkm.getForeignTableName().equals(otherTableName))
      {
        if(fkm.getColumns().size() != 1)
          throw new Exception("troppe colonne in chiave esterna: solo una supportata");

        return TARGET_MASTER;
      }
    }

    return TARGET_UNDEFINED;
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (StatoRec ge 10)
   * @return lista di oggetti
   */
  public default List<T> extractByFieldValue(String fieldName, Object valueFilter, boolean ignoreDeleted)
  {
    ArrayList<T> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ignoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (STATO_REC ge 10)
   * @return lista di oggetti
   */
  public default List<T> extractByFieldValuePeerName(String fieldName, Object valueFilter, boolean ignoreDeleted)
  {
    ArrayList<T> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ignoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByPeerName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param cm riferimento al campo da cercare
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (STATO_REC ge 10)
   * @return lista di oggetti
   */
  public default List<T> extractByFieldValuePeerName(ColumnMap cm, Object valueFilter, boolean ignoreDeleted)
  {
    return extractByFieldValuePeerName(cm.getColumnName(), valueFilter, ignoreDeleted);
  }

  /**
   * Ritorna il primo oggetto che soddisfa il filtro.
   * @param fn espressione lambda del filtro
   * @return oggetto o null
   * @throws Exception
   */
  public default T findFirst(PredicateThrowException<T> fn)
     throws Exception
  {
    for(T t : this)
    {
      if(fn.test(t))
        return t;
    }
    return null;
  }

  /**
   * Ritorna l'ultimo oggetto che soddisfa il filtro.
   * @param fn espressione lambda del filtro
   * @return oggetto o null
   * @throws Exception
   */
  public default T findLast(PredicateThrowException<T> fn)
     throws Exception
  {
    T rv = null;
    for(T t : this)
    {
      if(fn.test(t))
        rv = t;
    }
    return rv;
  }

  /**
   * Esegue sul primo oggetto che soddisfa il filtro.
   * @param fn espressione lambda del filtro
   * @param call azione da intraprendere
   * @return oggetto che ha ricevuto l'azione o null
   * @throws Exception
   */
  public default T findFirstExecute(PredicateThrowException<T> fn, ConsumerThrowException<T> call)
     throws Exception
  {
    for(T t : this)
    {
      if(fn.test(t))
      {
        call.accept(t);
        return t;
      }
    }
    return null;
  }

  /**
   * Esegue sul primo oggetto che soddisfa il filtro.
   * @param <V> tipo di ritorno azione
   * @param fn espressione lambda del filtro
   * @param call azione da intraprendere
   * @return oggetto che ha ricevuto l'azione o null
   * @throws Exception
   */
  public default <V> V findFirstFunction(PredicateThrowException<T> fn, FunctionTrowException<T, V> call)
     throws Exception
  {
    for(T t : this)
    {
      if(fn.test(t))
        return call.apply(t);
    }
    return null;
  }
}

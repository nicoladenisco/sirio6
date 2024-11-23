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
package org.sirio6.beans;

import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.PullService;
import org.apache.turbine.services.pull.tools.UITool;
import static org.sirio6.CoreConst.APP_PREFIX;
import org.sirio6.CsrfProtectionException;
import org.sirio6.services.modellixml.modelliXML;
import org.sirio6.services.token.TokenAuthService;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.DT;
import org.sirio6.utils.LI;
import org.sirio6.utils.SU;
import org.sirio6.utils.TR;

/**
 * Classe base di tutti i bean persistenti in sessione.
 *
 * @author Nicola De Nisco
 */
public class CoreBaseBean implements HttpSessionBindingListener
{
  protected Date today = new Date();
  protected String currJspName = null;
  protected PullService ps = null;
  protected UITool ui = null;
  protected String jvm, jlc;
  public String tagTabelleForm = null;
  public String tagTabelleList = null;
  // loggin
  protected Log log = LogFactory.getLog(this.getClass());

  /**
   * Inizializzazione del bean.
   * Quando viene creato il bean viene chiamato questo metodo per la sua inizializzazione.
   * @param data dati della richiesta
   * @throws Exception
   */
  public void init(CoreRunData data)
     throws Exception
  {
    ps = getService(PullService.SERVICE_NAME);
    ui = (UITool) ps.getGlobalContext().get("ui");

    tagTabelleForm = TR.getString("tag.tabelle.form", // NOI18N
       "TABLE WIDTH=100%"); // NOI18N

    tagTabelleList = TR.getString("tag.tabelle.list", // NOI18N
       "TABLE WIDTH=100%"); // NOI18N
  }

  /**
   * Verifica se questo bean è ancora valido.
   * Questa funzione viene chiamata quando
   * il bean viene recuperato dalla sessione.
   * Se nella richiesta vi sono parametri che
   * ne inficiano l'utilizzo questo metodo
   * deve ritornare false e una nuova istanza
   * di questo bean verrà creata e inizializzata.
   * @param data dati della richiesta
   * @return vero se il bean è valido
   */
  public boolean isValid(CoreRunData data)
  {
    return true;
  }

  /**
   * Notifica estrazione dalla sessione.
   * Ogni volta che BeanFactory estrae questo bean dalla sessione
   * viene invocato questo metodo.
   * Non viene chiamato alla creazione: vedi init().
   * @param data dati della richiesta
   * @throws java.lang.Exception
   */
  public void refreshSession(CoreRunData data)
     throws Exception
  {
  }

  /**
   * Conservazione dati alla scadenza di validita.
   * Quando un bean non è più valido (isValid torna false)
   * ne viene creata una nuova istanza.
   * Prima che quella corrente venga distrutta viene
   * chiamata questa funzione per consentire di trasferire
   * alcuni dati da una istanza all'altra.
   * @param data dati della richiesta
   * @param beanNonValido istanza precedente del bean destinata alla distruzione
   * @throws java.lang.Exception
   */
  public void preserveData(CoreRunData data, CoreBaseBean beanNonValido)
     throws Exception
  {
  }

  /**
   * Data una url relativa torna l'url completa a seconda dell'ambiente.
   * La stringa si riferisce a una risorsa JSP
   * ES url=jsmia.jsp produce http://localhost:8080/src/jsmia.jsp
   * oppure http://mio.server.it:8080/miaapp/servlet/miaapp/template/jsmia.jsp
   * @param url parte ultima dell'url
   * @return url completa
   */
  public String getServerUrl(String url)
  {
    if(url.startsWith("/"))
      url = url.substring(1);

    return Turbine.getContextPath() + "/" + APP_PREFIX + "/template/" + url;
  }

  public String getImgGeneric(String imgName, String tip)
  {
    return "<img src=\"" + ui.image(imgName) + "\" alt=\"" + tip
       + "\" title=\"" + tip + "\" border=\"0\">";
  }

  public String getIconGeneric(String iconName, String tip)
  {
    return LI.getImgIcon(iconName, tip);
  }

  public String getCurrJspName()
  {
    return currJspName;
  }

  public void setCurrJspName(String currJspName)
  {
    this.currJspName = currJspName;
  }

  public <T> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  public String getEsercizio()
     throws Exception
  {
    return Integer.toString(1900 + today.getYear());
  }

  public Date getToday()
  {
    return today;
  }

  public String getTodayAsString()
  {
    return DT.formatData(today);
  }

  public String getTodayAsStringFull()
  {
    return DT.formatDataFull(today);
  }

  /**
   * Esecuzione di comandi. Il form invia un parametro speciale chiamato
   * 'command' con una stringa identificativa dell'operazione richiesta
   * dall'utente. Questa stringa diviene parte di un metodo doCmd_stringa
   * ricercato a runtime e se presente eseguito. Vedi doCmd_... per ulteriori
   * dettagli.
   *
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri request più eventuali parametri permanenti
   * @param args eventuali argomenti aggiuntivi a piacere
   * @throws Exception
   */
  public void doCommand(String command, CoreRunData data, Map params, Object... args)
     throws Exception
  {
    SU.doCommand(this, command, data, params, args);
  }

  /**
   * Notifica inserimento in sessione.
   * Viene chiamata quando questo bean viene inserito in una sessione.
   * @param hsbe
   */
  @Override
  public void valueBound(HttpSessionBindingEvent hsbe)
  {
  }

  /**
   * Notifica rimozione da sessione.
   * Viene chiamata quando questo bean viene rimosso da una sessione.
   * Questo include il caso di una sessione scaduta per timeout.
   * @param hsbe
   */
  @Override
  public void valueUnbound(HttpSessionBindingEvent hsbe)
  {
  }

  public void readParameters(CoreRunData data)
     throws Exception
  {
    // azzera variabili che non sono correttamente settate dalla setProperies()
    clearBoolean();

    // trasferisce tutti i parametri dall'html a proprietà del bean
    data.getParameters().setProperties(this);
  }

  public void clearBoolean()
     throws Exception
  {
  }

  public void readParams(Map params)
     throws Exception
  {
  }

  public void ASSERT(boolean test, String cause)
  {
    if(!test)
    {
      String mess = "ASSERT failed: " + cause;
      log.error(mess);
      throw new RuntimeException(mess);
    }
  }

  public String getJvm()
  {
    return jvm;
  }

  public void setJvm(String jvm)
  {
    this.jvm = jvm;
  }

  public String getJlc()
  {
    return jlc;
  }

  public void setJlc(String jlc)
  {
    this.jlc = jlc;
  }

  protected void checkTokenCSRF(CoreRunData data, boolean obbligatorio)
     throws Exception
  {
    String token = data.getParameters().getString(modelliXML.CSRF_TOKEN_FIELD_NAME);

    if(obbligatorio && token == null)
      throw new CsrfProtectionException("Missing token in request.");

    if(token == null)
      return;

    TokenAuthService tas = getService(TokenAuthService.SERVICE_NAME);
    int verifica = tas.verificaTokenAntiCSRF(token, true, data.getRequest(), data.getSession());

    switch(verifica)
    {
      case 0:
        return;

      case 1:
        throw new CsrfProtectionException("Unknow token in request.");
      case 2:
        throw new CsrfProtectionException("Invalid token in request.");
    }
  }
}

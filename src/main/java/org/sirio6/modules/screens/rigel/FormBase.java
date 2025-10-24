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
package org.sirio6.modules.screens.rigel;

import java.util.*;
import javax.servlet.http.HttpSession;
import org.apache.velocity.context.*;
import org.commonlib5.utils.ClassOper;
import org.rigel5.exceptions.MissingListException;
import org.rigel5.glue.pager.PeerTablePagerEditApp;
import org.rigel5.glue.table.PeerAppMaintFormTable;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.html.*;
import org.sirio6.CoreConst;
import org.sirio6.beans.CoreBaseBean;
import org.sirio6.rigel.RigelUtils;
import org.sirio6.services.modellixml.MDL;
import org.sirio6.services.security.SEC;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.SU;

/**
 * Classe base di tutti i visualizzatori di form XML.
 * @author Nicola De Nisco
 */
abstract public class FormBase extends RigelEditBaseScreen
{
  @Override
  protected void doBuildTemplate2(CoreRunData data, Context context)
     throws Exception
  {
    String type = data.getParameters().getString("type");
    if(type == null)
      throw new Exception("Errore interno: parametro type non definito; rivedere flusso.");

    HtmlWrapperBase pwl = getForm(data, type);
    Map params = SU.getParMap(data);
    boolean forceNew = false, duplica = false, nuovoDetail = false;
    String baseUri = makeSelfUrl(data, type);

    String cmd = SU.okStr(data.getParameters().getString("command")); // NOI18N

    switch(cmd)
    {
      case CoreConst.SAVE_AND_NEW:
        forceNew = true;
        break;

      case CoreConst.DUP_CURRENT:
        forceNew = duplica = true;
        break;

      case CoreConst.NEW_DETAIL:
        nuovoDetail = true;
        break;
    }

    if(!RigelUtils.checkPermessiLettura(data, pwl))
    {
      // permessi della lista non posseduti dall'utente
      redirectUnauthorized(data);
      return;
    }

    if(forceNew || SU.isOkStr(data.getParameters().getString("new")))
    {
      if(!pwl.isNewEnabled())
        throw new Exception("Creazione nuovi oggetti non consentita.");

      if(!RigelUtils.checkPermessiCreazione(data, pwl))
      {
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(duplica)
      params.put("dup", "1");

    if(nuovoDetail)
      params.put("nuovoDetail", "1");

    makeContextHtml(forceNew, duplica, nuovoDetail, params, data, context, pwl, type, baseUri);
  }

  protected void makeContextHtml(boolean forceNew, boolean duplica, boolean nuovoDetail,
     Map params, CoreRunData data, Context context, HtmlWrapperBase pwl, String type, String baseUri)
     throws Exception
  {
    HttpSession session = data.getSession();
    String titolo = data.i18n(pwl.getTitolo());
    String header = data.i18n(pwl.getHeader());

    HashMap<String, String> extraParams = new HashMap<>();
    extraParams.put("jlc", type);
    extraParams.put("jvm", ClassOper.getClassName(getClass()) + ".vm");
    //byNIK: provoca un gran casino
    //params.forEach((k, v) -> extraParams.put(k.toString(), v.toString()));

    String html = getHtmlEdit(data, context, pwl, params, session, forceNew, extraParams);
    String scTest = getScriptTest(data, context, pwl, params, session, forceNew);

    String jlc = SU.okStr(params.get("jlc"));
    String jvm = SU.okStr(params.get("jvm"));

    context.put("jlc", jlc);
    context.put("jvm", jvm);
    context.put("type", type);
    context.put("baseUri", baseUri);
    context.put("phtml", html);
    context.put("pwl", pwl);
    context.put("scTest", "\n" + scTest);
    context.put("document", MDL.getDocument());
    context.put("header", header);
    context.put("titolo", titolo);

    if(SU.isOkStr(pwl.getCustomScript()))
      context.put("cscriptm", pwl.getCustomScript());

    // imposta abilitazioni edit, salva, nuovo
    enableEditSaveNew(data, context, params, pwl);

    boolean isNewObject = false;

    if(pwl.getTbl() instanceof PeerAppMaintFormTable)
    {
      PeerAppMaintFormTable tbl = (PeerAppMaintFormTable) (pwl.getTbl());
      context.put("obj", tbl.getLastObjectInEdit());
      isNewObject = tbl.isNewObject();
    }

    // verifica per oggetto nuovo
    if(!isNewObject)
    {
      // se previsto inserisce header button master
      if(pwl.isHeaderButton())
        context.put("hbuts", makeHeaderButtons(data, pwl, baseUri));

      // verifica per master/detail
      if(pwl.getMdInfo() != null)
      {
        String dettType = pwl.getMdInfo().getEditList();
        if(dettType == null)
          throw new Exception("Manca dettaglio nella definizione master-detail. Controllare lista.xml.");

        PeerWrapperEditHtml eh = (PeerWrapperEditHtml) getLista(data, dettType);
        if(eh == null)
          throw new MissingListException("Lista dettaglio " + dettType + " non trovata. Controllare lista.xml.");

        if(!RigelUtils.checkPermessiLettura(data, eh))
        {
          // permessi della lista non posseduti dall'utente
          redirectUnauthorized(data);
          return;
        }

        // in caso di nuovi dettail aggiunti dall'action non esegue
        // la rebind per preservare il contenuto del table model
        if(!nuovoDetail && pwl instanceof PeerWrapperFormHtml)
        {
          // carica eventuale filtro sul detail
          eh.populateParametri(params);

          // estae i parametri di collegamento dal master
          Map linkParams = ((PeerWrapperFormHtml) pwl).makeMapMasterDetail(0);
          ((PeerTablePagerEditApp) (eh.getPager())).setBaseSelfUrl(baseUri);
          ((PeerTablePagerEditApp) (eh.getPager())).rebindMasterDetail(linkParams);
        }

        String dettHtml = getHtmlEdit(data, context, eh, params, session, false, extraParams);
        String dettTest = getScriptTest(data, context, eh, params, session, false);

        context.put("dettHtml", dettHtml);
        context.put("dettTest", "\n" + dettTest);
        context.put("edl", eh);
        context.put("dettType", dettType);

        if(eh.isNewEnabled() && RigelUtils.checkPermessiCreazione(data, eh))
          context.put("newEnabledDett", "1");

        // se previsto inserisce header button detail
        if(eh.isHeaderButton())
          context.put("hbutd", makeHeaderButtons(data, eh, baseUri));

        if(SU.isOkStr(eh.getCustomScript()))
          context.put("cscriptd", eh.getCustomScript());
      }
    }
  }

  protected void enableEditSaveNew(CoreRunData data, Context context, Map params, HtmlWrapperBase pwl)
     throws Exception
  {
    if(pwl.isEditEnabled())
      context.put("editEnabled", "1");
    if(pwl.isSaveEnabled() && RigelUtils.checkPermessiScrittura(data, pwl))
      context.put("saveEnabled", "1");
    if(pwl.isNewEnabled() && RigelUtils.checkPermessiCreazione(data, pwl))
      context.put("newEnabled", "1");
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return isAuthorizedOne(data, "modifica_dati_generale");
  }

  @Override
  protected boolean isAuthorizedDelete(CoreRunData data)
     throws Exception
  {
    return SEC.checkAnyPermission(data, "cancella_dati_generale");
  }

  /**
   * Solo per impedire la ridefinizione del metodo.
   * @param data
   * @param context
   * @param bean
   * @throws Exception
   */
  @Override
  final protected void doBuildTemplate2(CoreRunData data, Context context, CoreBaseBean bean)
     throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

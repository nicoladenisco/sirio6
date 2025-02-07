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
package org.sirio6.rigel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ClassUtils;
import org.rigel5.SetupHolder;
import org.rigel5.glue.table.AlternateColorTableAppBase;
import org.rigel5.table.html.AbstractHtmlTablePagerFilter;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.PeerBuilderRicercaGenerica;
import org.rigel5.table.peer.html.PeerTableModel;
import org.rigel5.table.sql.SqlBuilderRicercaGenerica;
import org.rigel5.table.sql.html.SqlTableModel;
import static org.sirio6.CoreConst.HTML_END_CUT;
import static org.sirio6.CoreConst.HTML_START_CUT;
import org.sirio6.modules.screens.rigel.ListaBase5;
import org.sirio6.modules.screens.rigel.ListaInfo;
import org.sirio6.services.localization.INT;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.SU;
import org.sirio6.utils.velocity.VelocityParser;

/**
 * Renderizzatore per Tool Liste.
 * Estende ListaBase (quindi uno screen) per creare un contesto
 * di rendering simile a quello delle pagine principali.
 * Verrà utilizzato dal Tool per generare html ad hoc.
 *
 * @author Nicola De Nisco
 */
public class ToolRenderListeRigel extends ListaBase5
{
  protected final ToolRigelUIManager2 uim = new ToolRigelUIManager2();
  protected final ToolCustomUrlBuilder urb = new ToolCustomUrlBuilder();
  protected String unique = null, funcNameEdit, funcNameSubmit, funcNameSplit, formName, bodyName;
  protected int counter;

  @Override
  public boolean isPopup()
  {
    return false;
  }

  @Override
  public boolean isEditPopup()
  {
    return true;
  }

  @Override
  protected String makeSelfUrl(RunData data, String type)
  {
    unique = "LISTA_" + SU.purge(type) + "_" + counter;
    return data.getContextPath() + "/rigeltool/lista/type/" + type + "/unique/" + unique;
  }

  @Override
  protected void makeContextHtml(HtmlWrapperBase lso, ListaInfo li, CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    uim.setUnique(unique);

    CoreCustomUrlBuilder ub = (CoreCustomUrlBuilder) SetupHolder.getUrlBuilder();
    urb.setBaseMainForm(ub.getBaseMainForm());
    urb.setBaseMainList(ub.getBaseMainList());
    urb.setBasePopupForm(ub.getBasePopupForm());
    urb.setBasePopupList(ub.getBasePopupList());

    context.put("unique", unique);
    funcNameEdit = "rigel.apriEditTool";
    context.put("funcNameEdit", funcNameEdit);
    funcNameSubmit = "submit_" + unique;
    context.put("funcNameSubmit", funcNameSubmit);
    formName = "fo_" + unique;
    context.put("formName", formName);
    bodyName = "body_" + unique;
    context.put("bodyName", bodyName);

    lso.setUim(uim);

    ParameterParser pp = data.getParameters();
    AlternateColorTableAppBase act = (AlternateColorTableAppBase) (lso.getTbl());
    act.setAuthDelete(isAuthorizedDelete(data));
    act.setPopup(SU.checkTrueFalse(pp.getString("popup"), true));
    act.setEditPopup(SU.checkTrueFalse(pp.getString("editPopup"), true));
    act.setAuthSel(SU.checkTrueFalse(pp.getString("authSel"), true));
    act.setPopupEditFunction(funcNameEdit);
    act.setUrlBuilder(urb);
    urb.setFunc(li.func);
    urb.setType(li.type);

    AbstractHtmlTablePagerFilter flt = (AbstractHtmlTablePagerFilter) lso.getPager();
    flt.setFormName(formName);
    //flt.setUim(uim);
    flt.setI18n(new RigelHtmlI18n(data));
    String baseurl = flt.getBaseSelfUrl();
    context.put("selfurl", baseurl);

    if(lso.getPtm() instanceof SqlTableModel)
    {
      SqlTableModel tm = (SqlTableModel) lso.getPtm();
      String nometab = tm.getQuery().getVista();
      flt.setMascheraRicerca(new ToolRicercaListe(new SqlBuilderRicercaGenerica(tm, nometab),
         tm, act.getI18n(), unique, baseurl));
    }
    else if(lso.getPtm() instanceof PeerTableModel)
    {
      PeerTableModel tm = (PeerTableModel) lso.getPtm();
      flt.setMascheraRicerca(new ToolRicercaListe(new PeerBuilderRicercaGenerica(tm, tm.getTableMap()),
         tm, act.getI18n(), unique, baseurl));
    }

    super.makeContextHtml(lso, li, data, context, baseUri);
  }

  public void buildCtx(RunData data, Context ctx)
     throws Exception
  {
    doBuildTemplate2((CoreRunData) data, ctx);
  }

  /**
   * Produce HTML per il Tool delle liste.
   * Questa funzione viene chiamata dalla servlet ajax ToolDirectHtml.
   * @param data dati di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderHtml(RunData data)
     throws Exception
  {
    String ctxUnique = data.getParameters().getString("unique");
    Context ctx = (Context) data.getSession().getAttribute(ctxUnique);
    if(ctx == null)
      throw new Exception(INT.I("Context non presente in sessione; tool non disponibile."));

    String html = renderHtml(data, ctx);
    return cutHtml(html);
  }

  protected String cutHtml(String html)
  {
    // da tutto l'html estrae solo la parte racchiusa da <form></form>
    // il resto non si può toccare

    int pos1, pos2;
    if((pos1 = html.indexOf(HTML_START_CUT)) != -1)
      if((pos2 = html.indexOf(HTML_END_CUT, pos1)) != -1)
        return html.substring(pos1, pos2);

    return html;
  }

  /**
   * Produce HTML per il Tool delle liste.
   * @param data dati di chiamata
   * @param ctx context di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public synchronized String renderHtml(RunData data, Context ctx)
     throws Exception
  {
    boolean suppressEmpty = false;
    String suppressEmptyMessage = "";
    counter = (int) ctx.get("count");

    // recupera parametri del tool e li passa in RunData
    Map<String, String> mp = (Map<String, String>) ctx.get("paramsMap");
    if(mp != null)
    {
      suppressEmpty = SU.checkTrueFalse(mp.get("suppressEmpty"));
      suppressEmptyMessage = SU.okStr(mp.get("suppressEmptyMessage"), suppressEmptyMessage);

      for(Map.Entry<String, String> entry : mp.entrySet())
      {
        String key = entry.getKey();
        String value = entry.getValue();
        data.getParameters().setString(key, value);
      }
    }

    // costruisce tutti i componenti di pagina
    buildCtx(data, ctx);

    // salva il context in sessione per le successive chiamate dalla servlet ajax
    data.getSession().setAttribute(unique, ctx);

    if(suppressEmpty && SU.parse(ctx.get("numrows"), -1) == 0)
      return suppressEmptyMessage;

    StringWriter writer = new StringWriter(512);
    // renderizzazione Velocity con il modello caricato da risorsa
    try(InputStream is = ClassUtils.getResourceAsStream(getClass(), "/ToolLista.vm"))
    {
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");

      VelocityParser vp = new VelocityParser(ctx);
      vp.parseReader(reader, writer, "ToolLista.vm");
    }

    String html = writer.toString();

    // rimaneggia javascript sostituendo submit con funzione specifica
    String url = (String) ctx.get("selfurl");
    return SU.strReplace(html,
       "document." + formName + ".submit();",
       "rigel.submitTool('" + unique + "', '" + url + "')");
  }

  @Override
  public void formatHtmlLista(int filtro, RigelHtmlPage page, Context context)
     throws Exception
  {
    context.put("filtro", filtro);
    context.put("htpage", page);
  }
}

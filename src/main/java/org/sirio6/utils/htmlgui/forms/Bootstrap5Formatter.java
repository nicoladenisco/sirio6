/*
 *  Bootstrap5Formatter.java
 *
 *  Copyright (C) 2024 Dedalus Italia S.p.A
 *
 *  Questo software è proprietà di Dedalus Italia S.p.A
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Dedalus Italia S.p.A
 *  Via di Collodi, 6/C
 *  50141 Firenze
 */
package org.sirio6.utils.htmlgui.forms;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.TurbineServices;
import org.rigel5.RigelI18nInterface;
import org.sirio6.utils.SU;
import org.sirio6.utils.SirioGenericContext;

/**
 * Formattatore di parametri in stile bootstrap 5.
 *
 * @author Nicola De Nisco
 */
public class Bootstrap5Formatter implements ParametriFormatter
{
  private static final Log log = LogFactory.getLog(Bootstrap5Formatter.class);
  protected boolean errosInName, errorsInTag;
  protected final BloccoParametri block;
  protected int mb;

  public Bootstrap5Formatter(BloccoParametri block)
  {
    this.block = block;
  }

  public Bootstrap5Formatter(BloccoParametri block, int mb)
  {
    this.block = block;
    this.mb = mb;
  }

  public int getMb()
  {
    return mb;
  }

  public void setMb(int mb)
  {
    this.mb = mb;
  }

  @Override
  final public void getHtml(int idx, StringBuilder html, SirioGenericContext ctx)
     throws Exception
  {
    formatHtmlParametri(idx, html, ctx);
  }

  public boolean isErrosInName()
  {
    return errosInName;
  }

  public boolean isErrorsInTag()
  {
    return errorsInTag;
  }

  protected void guscioCampo(int idx, StringBuilder html, SirioGenericContext ctx,
     String nome, String campo, String inner, String helpblock, boolean evierror)
  {
    String m = evierror ? " text-danger" : "";
    String fgc = ctx.getAsString("form-group-classes");
    html.append("  <div class=\"form-group ").append(fgc).append("\">\n");
    if(mb > 0)
      html.append("   <div class=\"mb-").append(mb).append("\">");
    html.append("    <label for=\"").append(campo).append("\" class=\"control-label").append(m).append("\">").append(nome).append("</label>\n");

    html.append(inner);

    if(helpblock != null)
      html.append("    <small id=\"help_").append(campo).append("\" class=\"help-block\">").append(helpblock).append("</small>\n");
    if(mb > 0)
      html.append("   </div>");

    html.append("  </div>\n");
  }

  protected void formatCampoTestoNonVuoto(int idx, StringBuilder html, SirioGenericContext ctx,
     String nome, String campo, String valore, String placeholder, String helpblock,
     Collection listaAutoCompleta)
  {
    formatCampoTesto(idx, html, ctx, nome, campo, valore, placeholder, helpblock, !SU.isOkStr(valore), listaAutoCompleta);
  }

  protected void formatCampoTesto(int idx, StringBuilder html, SirioGenericContext ctx,
     String nome, String campo, String valore, String placeholder, String helpblock, boolean evierror,
     Collection listaAutoCompleta)
  {
    String m = evierror ? " is-invalid" : "";
    StringBuilder inner = new StringBuilder();
    inner.append("<input type=\"text\" name=\"").append(campo).append("\" value=\"").append(valore).append("\"");
    inner.append(" class=\"form-control").append(m).append("\" id=\"").append(campo).append("\"\n");

    if(placeholder != null)
      inner.append(" placeholder=\"").append(placeholder).append("\"\n");

    if(helpblock != null)
      inner.append(" aria-describedby=\"help_").append(campo).append("\"\n");

    if(!listaAutoCompleta.isEmpty())
      inner.append(" list=\"").append(campo).append("-datalistOptions\"\n");

    inner.append(">\n");

    if(!listaAutoCompleta.isEmpty())
    {
      inner.append("  <datalist id=\"").append(campo).append("-datalistOptions\">\n");
      listaAutoCompleta.forEach((v) -> inner.append("<option value=\"").append(v.toString()).append("\"/>"));
      inner.append("  <datalist/>");
    }

    guscioCampo(idx, html, ctx, nome, campo, inner.toString(), helpblock, evierror);
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  protected void formatCampoTextArea(int idx, StringBuilder html, SirioGenericContext ctx,
     String nome, String campo, String valore, String placeholder, String helpblock, boolean evierror, int rows)
  {
    String m = evierror ? " is-invalid" : "";
    StringBuilder inner = new StringBuilder();
    inner.append("<textarea rows=\"" + rows + "\" name=\"" + campo + "\"");
    inner.append(" class=\"form-control" + m + "\" id=\"" + campo + "\"");

    if(placeholder != null)
      inner.append(" placeholder=\"" + placeholder + "\"\n");

    if(helpblock != null)
      inner.append(" aria-describedby=\"help_" + campo + "\"\n");

    inner.append(">" + valore + "</textarea>\n");
    guscioCampo(idx, html, ctx, nome, campo, inner.toString(), helpblock, evierror);
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  protected void formatCampoComboString(int idx, StringBuilder html, SirioGenericContext ctx,
     String nome, String campo, String valore, String helpblock, boolean evierror, Map valoriCombo)
  {
    String m = evierror ? " is-invalid" : "";
    StringBuilder inner = new StringBuilder();
    inner.append("<select class=\"form-control" + m + "\" name=\"" + campo + "\"");

    if(helpblock != null)
      inner.append(" aria-describedby=\"help_" + campo + "\"\n");
    inner.append(">\n");

    for(Object okey : valoriCombo.keySet())
    {
      String key = SU.okStr(okey);
      String value = SU.okStr(valoriCombo.get(okey));
      inner.append(SU.generaOptionCombo(key, value, valore)).append("\n");
    }

    inner.append("</select>\n");
    guscioCampo(idx, html, ctx, nome, campo, inner.toString(), helpblock, evierror);
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  protected void formatCampoRadioString(int idx, StringBuilder html, SirioGenericContext ctx,
     String campo, String valore, Map valoriRadio)
  {
    String fgc = ctx.getAsString("form-group-classes");

    int count = 1;
    for(Object okey : valoriRadio.keySet())
    {
      String key = SU.okStr(okey);
      String value = SU.okStr(valoriRadio.get(okey));
      String checked = key.equals(valore) ? "checked" : "";
      String id = campo + count;

      html.append("<div class=\"form-group " + fgc + "\">\n");
      html.append("    <div class=\"radio\">\n");
      html.append("      <label>\n");
      html.append("        <input type=\"radio\" name=\"" + campo + "\" id=\"" + id + "\" value=\"" + key + "\" " + checked + ">&nbsp;").append(value + "\n");
      html.append("      </label>\n");
      html.append("    </div>");
      html.append("</div>");

      count++;
    }
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  protected void formatCampoCheckbox(int idx, StringBuilder html, SirioGenericContext ctx,
     String campo, boolean valore, String commento, String helpblock)
  {
    String fgc = ctx.getAsString("form-group-classes");

    html.append("<div class=\"form-group " + fgc + "\">\n");
    html.append("    <div class=\"checkbox\">\n");
    html.append("      <label>\n");
    html.append("        <input type=\"checkbox\" value=\"1\" name=\"" + campo + "\" ")
       .append(valore ? "checked" : "").append(">&nbsp;").append(commento + "\n");
    html.append("      </label>\n");
    html.append("    </div>");

    if(helpblock != null)
      html.append("    <small id=\"help_" + campo + "\" class=\"help-block\">" + helpblock + "</small>\n");

    html.append("</div>");
  }

  protected String getNomeCampo(int idx, String nome)
  {
    return nome + "_" + idx;
  }

  @Override
  public void saveHtml(int idx, SirioGenericContext ctx)
     throws Exception
  {
    errosInName = errorsInTag = false;
    saveHtmlParametri(idx, ctx);
  }

  @Override
  public void saveData(ParametriValuesMap eSetup, RigelI18nInterface i18n)
     throws Exception
  {
    saveDataParametri(eSetup, i18n);
  }

  @Override
  public void loadData(ParametriValuesMap eSetup, RigelI18nInterface i18n)
     throws Exception
  {
    loadSetupParametri(eSetup, i18n);
  }

  public <T> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  protected void inserisciTestoHelp(String testo, StringBuilder html)
     throws Exception
  {
    html.append("<div style='margin: 20px; padding: 20px; border: 1px solid'>");
    html.append(testo);
    html.append("</div>");
  }

  protected void inserisciTestoHelpCampo(String testo, StringBuilder html, SirioGenericContext ctx)
  {
    String fgc = ctx.getAsString("form-group-classes");

    html.append("<div class=\"form-group ").append(fgc).append("\">\n");
    html.append("  <div class=\"col-sm-offset-2 col-sm-10\">");
    html.append("<div style='margin: 10px; padding: 20px; border: 1px solid'>");
    html.append(testo);
    html.append("</div>");
    html.append("  </div>");
    html.append("</div>");
  }

  protected void die(String msg)
  {
    throw new RuntimeException(msg);
  }

  public void formatHtmlParametri(int idx, StringBuilder html, SirioGenericContext ctx)
     throws Exception
  {
    for(Parametro pd : block.getParametri())
    {
      formatHtmlParametro(idx, pd, html, ctx);
    }
  }

  public void formatHtmlParametro(int idx, Parametro pd, StringBuilder html, SirioGenericContext ctx)
  {
    String campo = getNomeCampo(idx, pd.getCodice());

    switch(pd.getVisualizzazione())
    {
      case TIPO_VIS_TESTO:
        formatCampoTesto(
           idx, html, ctx, ctx.msg(pd.getNome()), campo,
           pd.getValueAsString(),
           ctx.msg(pd.getDescrizione()),
           ctx.msg(pd.getHelpBlock()),
           pd.isError(),
           pd.getListaAutoCompleta());
        break;
      case TIPO_VIS_AREA:
        formatCampoTextArea(
           idx, html, ctx, ctx.msg(pd.getNome()), campo,
           pd.getValueAsString(),
           ctx.msg(pd.getDescrizione()),
           ctx.msg(pd.getHelpBlock()),
           pd.isError(), 4);
        break;
      case TIPO_VIS_CHECK:
        formatCampoCheckbox(
           idx, html, ctx, campo,
           (Boolean) pd.getVal(),
           ctx.msg(pd.getDescrizione()),
           ctx.msg(pd.getHelpBlock()));
        break;
      case TIPO_VIS_RADIO:
        formatCampoRadioString(
           idx, html, ctx, campo,
           pd.getValueAsString(),
           pd.getListaValori());
        break;
      case TIPO_VIS_COMBO:
        formatCampoComboString(
           idx, html, ctx, ctx.msg(pd.getNome()), campo,
           pd.getValueAsString(),
           ctx.msg(pd.getHelpBlock()),
           pd.isError(),
           pd.getListaValori());
        break;
    }
  }

  public void saveHtmlParametri(int idx, SirioGenericContext ctx)
     throws Exception
  {
    for(Parametro pd : block.getParametri())
    {
      saveHtmlParametro(idx, pd, ctx);
    }
  }

  public void saveHtmlParametro(int idx, Parametro pd, SirioGenericContext ctx)
     throws Exception
  {
    String campo = getNomeCampo(idx, pd.getCodice());
    String tmp = ctx.getAsString(campo);
    pd.parseString(tmp);
    if(pd.isError())
      errorsInTag = true;
    log.debug(pd.getCodice() + "=" + pd.getVal());
  }

  public void saveDataParametri(ParametriValuesMap eSetup, RigelI18nInterface i18n)
     throws Exception
  {
    for(Parametro pd : block.getParametri())
    {
      saveDataParametro(eSetup, pd, i18n);
    }
  }

  public void saveDataParametro(ParametriValuesMap eSetup, Parametro pd, RigelI18nInterface i18n)
     throws Exception
  {
    eSetup.put(pd.getCodice(), pd.getValueAsString());
  }

  public void loadSetupParametri(ParametriValuesMap eSetup, RigelI18nInterface i18n)
     throws Exception
  {
    for(Parametro pd : block.getParametri())
    {
      loadSetupParametro(eSetup, pd, i18n);
    }
  }

  public void loadSetupParametro(ParametriValuesMap eSetup, Parametro pd, RigelI18nInterface i18n)
     throws Exception
  {
    pd.parseString(eSetup.get(pd.getCodice()));
  }
}

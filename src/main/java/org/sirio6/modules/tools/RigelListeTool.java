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
package org.sirio6.modules.tools;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.velocity.VelocityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.sirio6.rigel.ToolRenderDatatableRigel;
import org.sirio6.rigel.ToolRenderListeRigel;
import org.sirio6.utils.SU;

/**
 * Tool per l'accesso alle liste xml di Rigel.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class RigelListeTool
   implements ApplicationTool
{
  private final ToolRenderListeRigel renderListe = new ToolRenderListeRigel();
  private final ToolRenderDatatableRigel renderDatatable = new ToolRenderDatatableRigel();
  private static final AtomicInteger counter = new AtomicInteger();
  private VelocityService velocity;

  @Override
  public void init(Object data)
  {
    velocity = (VelocityService) TurbineServices.getInstance().getService(VelocityService.SERVICE_NAME);
  }

  @Override
  public void refresh()
  {
  }

  /**
   * Elabora HTML per la lista rigel richiesta.
   * Questa funzione è deprecata. La selfUrlVM serviva nella versione precedente.
   * Questa versione utilizza ajax per aggiornare i dati visualizzati.
   * @param data dati di richiesta
   * @param lista lista rigel richiesta
   * @param selfUrlVM url della pagina che contiene il tool
   * @return html completo
   * @throws Exception
   * @deprecated usa il metodo html()
   */
  @Deprecated
  public String getHtml(RunData data, String lista, String selfUrlVM)
     throws Exception
  {
    return getHtml(data, lista, selfUrlVM, null);
  }

  /**
   * Elabora HTML per la lista rigel richiesta.
   * Questa funzione è deprecata. La selfUrlVM serviva nella versione precedente.
   * Questa versione utilizza ajax per aggiornare i dati visualizzati.
   * @param data dati di richiesta
   * @param lista lista rigel richiesta
   * @param selfUrlVM url della pagina che contiene il tool
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   * @deprecated usa il metodo html()
   */
  @Deprecated
  public String getHtml(RunData data, String lista, String selfUrlVM, String params)
     throws Exception
  {
    return html(data, lista, params);
  }

  /**
   * Elabora HTML per la lista rigel richiesta.
   * @param data dati di richiesta
   * @param lista lista rigel richiesta
   * @return html completo
   * @throws Exception
   */
  public String html(RunData data, String lista)
     throws Exception
  {
    return html(data, lista, null);
  }

  /**
   * Elabora HTML per la lista rigel richiesta.
   * @param data dati di richiesta
   * @param lista lista rigel richiesta
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   */
  public String html(RunData data, String lista, String params)
     throws Exception
  {
    ParameterParser pp = data.getParameters();
    pp.setString("type", lista);
    Context ctx = velocity.getContext(data);
    ctx.put("count", counter.getAndIncrement());

    // aggiunge i parametri specificati in params
    if(params != null)
    {
      Map<String, String> mp = SU.string2Map(params, ",", true);
      ctx.put("paramsMap", mp);
    }

    return renderListe.renderHtml(data, ctx);
  }

  /**
   * Elabora HTML per una datable agganciata ad una lista rigel.
   * @param data dati di richiesta
   * @param lista lista rigel richiesta
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   */
  public String datatable(RunData data, String lista, String params)
     throws Exception
  {
    return datatableFilter(data, lista, null, params);
  }

  public String datatableFilter(RunData data, String lista, String filter, String params)
     throws Exception
  {
    ParameterParser pp = data.getParameters();
    pp.setString("type", lista);
    Context ctx = velocity.getContext(data);

    // per la datatable questa è 0; se proprio devo inserire nella stessa pagina
    // più liste uguali devo differenziarle con count=1, count=2 in params
    ctx.put("count", 0);

    // aggiunge i parametri specificati in params
    if(params != null)
    {
      Map<String, String> mp = SU.string2Map(params, ",", true);
      if(!mp.isEmpty())
        ctx.put("paramsMap", mp);
    }

    // aggiunge il filtro
    if(filter != null)
      ctx.put("freeFilter", filter);

    return renderDatatable.renderHtml(data, ctx);
  }
}

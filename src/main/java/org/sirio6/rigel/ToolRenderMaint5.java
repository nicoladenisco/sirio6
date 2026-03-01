/*
 * Copyright (C) 2026 Nicola De Nisco
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

import org.apache.turbine.services.velocity.VelocityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.json.JSONObject;
import org.sirio6.modules.screens.rigel.ListaBaseMaint5;
import org.sirio6.utils.CoreRunData;

/**
 * Render per affiancare ListaBaseMaint5.
 * Alcune funzioni di navigazione vengono implementate
 * usando chiamate ajax alla servlet, piuttosto di ricaricare la pagina.
 *
 * @author Nicola De Nisco
 */
public class ToolRenderMaint5 extends ListaBaseMaint5
{
  @Override
  protected String makeSelfUrl(RunData data, String type)
  {
    return data.getContextPath() + "/rigeltool/maint/type/" + type;
  }

  /**
   * Produce JSON per il Tool delle liste.
   * Questa funzione viene chiamata dalla servlet ajax ToolDirectHtml.
   * @param data dati di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderJson(CoreRunData data)
     throws Exception
  {
    if(velocity == null)
      velocity = getService(VelocityService.SERVICE_NAME);
    Context context = velocity.getContext(data);
    buildCtx(data, context);

    String htmlBody = (String) context.get("bodyhtml");
    String idBody = (String) context.get("idbody");
    String htmlNav = (String) context.get("navhtml");
    String idNav = (String) context.get("idnav");

    JSONObject json = new JSONObject();
    json.put("htmlBody", htmlBody);
    json.put("idBody", idBody);
    json.put("htmlNav", htmlNav);
    json.put("idNav", idNav);

    return json.toString();
  }

  public void buildCtx(RunData data, Context ctx)
     throws Exception
  {
    doBuildTemplate2((CoreRunData) data, ctx);
  }
}

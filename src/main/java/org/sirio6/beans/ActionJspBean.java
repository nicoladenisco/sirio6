/*
 * Copyright (C) 2021 Nicola De Nisco
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

import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.turbine.modules.Action;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.assemblerbroker.AssemblerBrokerService;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.services.velocity.VelocityService;
import org.apache.velocity.context.Context;
import org.json.JSONObject;
import org.sirio6.ErrorMessageException;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.SU;

/**
 * Bean di supporto per action.jsp.
 * Permette di eseguire una action in un contesto Json.
 * L'action invocata puo lasciare un oggetto 'data' nel context
 * per riportare sotto forma di json dati al chiamante.
 *
 * @author Nicola De Nisco
 */
public class ActionJspBean
{
  public static final String FILTERED_CONTEXT_DATA_KEY = "data";

  RunDataService rd = (RunDataService) TurbineServices.getInstance().getService(RunDataService.SERVICE_NAME);
  VelocityService vs = (VelocityService) TurbineServices.getInstance().getService(VelocityService.SERVICE_NAME);
  AssemblerBrokerService abs = (AssemblerBrokerService) TurbineServices.getInstance()
     .getService(AssemblerBrokerService.SERVICE_NAME);

  /**
   * Esegue una action ritornando i risultati in JSON.
   * @param request
   * @param response
   * @param config
   * @param out
   * @throws Exception
   */
  public void runAction(
     HttpServletRequest request,
     HttpServletResponse response,
     ServletConfig config, Writer out)
     throws Exception
  {
    JSONObject json = new JSONObject();
    CoreRunData data = (CoreRunData) rd.getRunData(request, response, config);

    try
    {
      // Pull user from session.
      data.populate();

      // usa il servizio di turbine per il caricamento delle action
      // legge il nome della action e usa il caricatore per ricavarne l'istanza
      String nomeAction = request.getParameter("action");
      VelocityAction action = (VelocityAction) abs.getAssembler(VelocityAction.class, nomeAction);
      if(action == null)
        action = (VelocityAction) abs.getAssembler(Action.class, nomeAction);

      if(action == null)
        throw new ErrorMessageException("Non riesco a trovare l'action " + nomeAction);

      // esegue la action
      Context ctx = vs.getContext(data);
      action.doPerform(data, ctx);

      // recupera oggetto dati di ritorno
      Object filteredData = ctx.get(FILTERED_CONTEXT_DATA_KEY);

      if(filteredData == null)
      {
        // trasforma il contenuto del context in oggetto Json
        Object[] keys = ctx.getKeys();
        for(int idx = 0; idx < keys.length; idx++)
        {
          String key = keys[idx].toString();
          json.put(key, ctx.get(key));
        }
      }
      else
      {
        if(filteredData instanceof JSONObject)
        {
          Set<String> ks = ((JSONObject) filteredData).keySet();
          for(String chiave : ks)
            json.put(chiave, ((JSONObject) filteredData).get(chiave));
        }
        else if(filteredData instanceof Map)
        {
          Set keys = ((Map) filteredData).keySet();
          for(Object chiave : keys)
            json.put(chiave.toString(), ((Map) filteredData).get(chiave));
        }
        else if(filteredData instanceof Collection)
        {
          json.put("array", (Collection) filteredData);
        }
        else
        {
          json.put("data", filteredData);
        }
      }

      // recupera l'eventuale messaggio dell'action inserendolo nel ritorno
      if(data.hasMessage())
        json.put("message", data.getMessage());
    }
    catch(ErrorMessageException e)
    {
      json.put("message", SU.okStr(e.getMessage(), "Unknow internal error."));
    }
    catch(Throwable t)
    {
      json.put("ERROR", SU.okStr(t.getMessage(), "Unknow internal error."));
    }
    finally
    {
      rd.putRunData(data);
    }

    out.write(json.toString());
    out.flush();
  }

  /**
   * Elabora uno screen ritornando il risultato in HTML.
   * @param request
   * @param response
   * @param config
   * @param out
   * @throws Exception
   */
  public void runScreenHtml(
     HttpServletRequest request,
     HttpServletResponse response,
     ServletConfig config, Writer out)
     throws Exception
  {
    CoreRunData data = (CoreRunData) rd.getRunData(request, response, config);

    try
    {
      // Pull user from session.
      data.populate();

      // legge il nome della screen e usa il caricatore per ricavarne l'istanza
      String nomeScreen = request.getParameter("screen");

      // usa il servizio di turbine per creare e renderizzare lo screen
      data.getTemplateInfo().setScreenTemplate(nomeScreen + ".vm");
      String results = ScreenLoader.getInstance().eval(data, nomeScreen);
      out.write(results);
    }
    finally
    {
      rd.putRunData(data);
    }

    out.flush();
  }
}

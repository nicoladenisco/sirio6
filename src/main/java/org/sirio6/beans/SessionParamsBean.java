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
package org.sirio6.beans;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import static org.commonlib5.utils.StringOper.okStr;
import static org.sirio6.utils.SU.PATH_INFO;
import static org.sirio6.utils.SU.QUERY_STRING;
import static org.sirio6.utils.SU.SESSION_ID;
import static org.sirio6.utils.SU.isFilePart;

/**
 * Bean per il mantenimento di parametri URL in sessione.
 *
 * @author Nicola De Nisco
 */
public class SessionParamsBean
{
  private static final Log log = LogFactory.getLog(SessionParamsBean.class);
  private String origin, originUri, originQuery, originPathInfo;
  public static final String SESSION_PARAMS_BEAN_SESSION_KEY = "SESSION_PARAMS_BEAN_SESSION_KEY";

  private final HashMap<String, Object> savedParams = new HashMap<>();

  private SessionParamsBean()
  {
  }

  public static SessionParamsBean getFromSession(HttpSession session)
  {
    SessionParamsBean bean = (SessionParamsBean) session.getAttribute(SESSION_PARAMS_BEAN_SESSION_KEY);

    if(bean == null)
    {
      bean = new SessionParamsBean();
      session.setAttribute(SESSION_PARAMS_BEAN_SESSION_KEY, bean);
    }

    return bean;
  }

  public static void removeFromSession(HttpSession session)
  {
    session.removeAttribute(SESSION_PARAMS_BEAN_SESSION_KEY);
  }

  public Map<String, Object> getSavedParams()
  {
    return savedParams;
  }

  /**
   * Costruisce mappa fusione della request e dei parametri salvati.
   * I parametri passati nella request sovrascrivono eventuali parametri omonimi salvati.
   * @param request richiesta http da analizzare
   * @return mappa fusione
   */
  public Map<String, Object> getParMap(HttpServletRequest request)
  {
    HashMap<String, Object> htParam = new HashMap<>(savedParams);

    // estrae i parametri della richiesta (anche i campi di input con nome della form)
    Map<String, String[]> parameterMap = request.getParameterMap();
    for(Map.Entry<String, String[]> entry : parameterMap.entrySet())
    {
      String name = entry.getKey();
      String[] value = entry.getValue();

      if(value == null || value.length == 0)
        continue;

      // se contiene un solo valore lo passa come tale, altrimenti passa l'array dei valori
      if(value.length == 1)
      {
        htParam.put(name, value[0]);
        htParam.put(name.toLowerCase(), value[0]);
      }
      else
      {
        htParam.put(name, value);
        htParam.put(name.toLowerCase(), value);
      }
    }

    // carica i parametri fissi
    htParam.putIfAbsent(SESSION_ID, request.getSession().getId());
    htParam.putIfAbsent(QUERY_STRING, okStr(request.getQueryString()));
    htParam.putIfAbsent(PATH_INFO, okStr(request.getPathInfo()));

    return htParam;
  }

  public void saveParam(String key, Object val)
  {
    if(val == null)
      savedParams.remove(key);
    else
      savedParams.put(key, val);
  }

  public void saveParam(Map<String, Object> params)
  {
    savedParams.putAll(params);
  }

  public void saveParam(String... params)
  {
    if(params.length == 0)
      return;

    if((params.length & 1) == 1)
      throw new RuntimeException("Il parametro 'params' deve essere di lunghezza pari.");

    for(int i = 0; i < params.length; i += 2)
    {
      String key = params[i];
      String val = params[i + 1];

      if(val == null)
        savedParams.remove(key);
      else
        savedParams.put(key, val);
    }
  }

  public Object readParam(String key)
  {
    return savedParams.get(key);
  }

  public Object getParam(String key, Object defval)
  {
    Object rv = readParam(key);
    return rv == null ? defval : rv;
  }

  public Map getParMap(RunData data)
  {
    HashMap htParam = (HashMap) getParMap(data.getRequest());

    ParameterParser pp = data.getParameters();
    Object[] keys = pp.getKeys();
    for(int i = 0; i < keys.length; i++)
    {
      String name = (String) keys[i];
      String[] value = pp.getStrings(name);

      if(value == null || value.length == 0)
        continue;

      // se contiene un solo valore lo passa come tale, altrimenti passa l'array dei valori
      if(value.length == 1)
        htParam.put(name, value[0]);
      else
        htParam.put(name, value);
    }

    try
    {
      for(int i = 0; i < keys.length; i++)
      {
        String name = (String) keys[i];
        Part filePart = data.getRequest().getPart(name);
        if(filePart != null && isFilePart(filePart))
          htParam.put("filepart_" + name, filePart);
      }
    }
    catch(Exception ex)
    {
      if(!ex.getMessage().contains("InvalidContentTypeException"))
        log.error("Error in parsing parts.", ex);
    }

    return htParam;
  }

  public Object removeParam(String key)
  {
    return savedParams.remove(key);
  }

  public void removeAllParams()
  {
    savedParams.clear();
  }

  public String getOrigin()
  {
    return origin;
  }

  public void setOrigin(String origin)
  {
    this.origin = origin;
  }

  public String getOriginUri()
  {
    return originUri;
  }

  public void setOriginUri(String originUri)
  {
    this.originUri = originUri;
  }

  public String getOriginQuery()
  {
    return originQuery;
  }

  public void setOriginQuery(String originQuery)
  {
    this.originQuery = originQuery;
  }

  public String getOriginPathInfo()
  {
    return originPathInfo;
  }

  public void setOriginPathInfo(String originPathInfo)
  {
    this.originPathInfo = originPathInfo;
  }
}

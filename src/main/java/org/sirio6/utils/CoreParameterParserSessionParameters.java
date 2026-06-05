/*
 * Copyright (C) 2025 Nicola De Nisco
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
package org.sirio6.utils;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.sirio6.beans.SessionParamsBean;

/**
 * Estensione di DefaultParameterParser.
 *
 * @author Nicola De Nisco
 */
public class CoreParameterParserSessionParameters extends CoreParameterParser
{
  public CoreParameterParserSessionParameters()
  {
  }

  public CoreParameterParserSessionParameters(String characterEncoding)
  {
    super(characterEncoding);
  }

  @Override
  public void setRequest(HttpServletRequest request)
  {
    super.setRequest(request);
    SessionParamsBean bean = SessionParamsBean.getFromSession(request.getSession());

    getLogger().debug("aggiungo parametri salvati da sessione");

    // aggiunge i parametri salvati in sessione alla mappa parametri
    for(Map.Entry<String, Object> entry : bean.getSavedParams().entrySet())
    {
      String name = entry.getKey();
      Object value = entry.getValue();

      if(value == null)
        continue;

      if(value instanceof String)
        add(name, (String) value);
      else if(value instanceof String[])
        add(name, (String[]) value);
      else
        add(name, value.toString());
    }
  }
}

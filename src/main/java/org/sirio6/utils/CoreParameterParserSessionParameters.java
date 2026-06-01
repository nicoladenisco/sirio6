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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.sirio6.utils.SU.PERM_PAR_KEY;

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

    HashMap<Object, Object> saved = (HashMap) request.getSession().getAttribute(PERM_PAR_KEY);
    if(saved == null || saved.isEmpty())
      return;

    getLogger().debug("aggiungo parametri salvati da sessione");

    // aggiunge i parametri salvati in sessione alla mappa parametri
    for(Map.Entry<Object, Object> entry : saved.entrySet())
    {
      String name = entry.getKey().toString();
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

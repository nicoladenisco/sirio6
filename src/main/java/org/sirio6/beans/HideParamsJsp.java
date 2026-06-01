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

import java.io.Writer;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.commonlib5.utils.StringJoin;
import org.json.JSONObject;
import org.sirio6.utils.SU;

/**
 * Bean per hideParams.jsp.
 * Dalla uri originale salva i parametri nella PERM_PAR_KEY in sessione.
 * Edulcora la uri da tutti i parametri e la restituisce.
 * Usata insieme a CoreParameterParserSessionParameters permette chiamate
 * nascondendo i parametri nella url di chiamata.
 *
 * @author Nicola De Nisco
 */
public class HideParamsJsp
{
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
    HttpSession session = request.getSession();
    JSONObject json = new JSONObject();

    String origin = request.getParameter("origin");
    int pos = origin.indexOf('?');

    String uri = pos == -1 ? origin : origin.substring(0, pos);
    String qrs = pos == -1 ? "" : origin.substring(pos + 1);

    List<String> uriParts = SU.string2List(uri, "/");
    List<String> qrsParts = SU.string2List(qrs, "&");

    // questa è la uri edulcorata da ritornare
    String purgedUri = uri;
    if(!uri.endsWith(".vm"))
    {
      final StringJoin buildurl = StringJoin.build("/");
      for(int i = 0; i < uriParts.size(); i++)
      {
        String up = uriParts.get(i);
        buildurl.add(up);

        if(up.endsWith(".vm"))
        {
          // uri vm con eventuale pathinfo
          purgedUri = "/" + buildurl.join();

          // salva i parametri passati come pathInfo
          for(int j = i + 1; j < uriParts.size(); j += 2)
          {
            if((j + 1) >= uriParts.size())
              break;

            String nome = uriParts.get(j);
            String valore = uriParts.get(j + 1);
            SU.saveParam(session, nome, valore);
          }

          break;
        }
      }
    }

    // salva i parametri della richiesta nella cache parametri
    for(String sq : qrsParts)
    {
      String[] sqs = sq.split("=");
      if(sqs.length == 2)
      {
        String nome = sqs[0];
        String valore = sqs[1];
        SU.saveParam(session, nome, valore);
      }
    }

    json.put("purgedUri", purgedUri);
    out.write(json.toString());
    out.flush();
  }
}

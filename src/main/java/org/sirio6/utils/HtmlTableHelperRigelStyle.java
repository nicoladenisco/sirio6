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
package org.sirio6.utils;

import java.util.Collection;
import org.commonlib5.utils.HtmlTableHelper;

/**
 * Come HtmlTableHelper ma con gli stili tipici di Rigel.
 * Serve a produrre tabelle simili a quelle di rigel.
 *
 * @author Nicola De Nisco
 */
public class HtmlTableHelperRigelStyle extends HtmlTableHelper
{
  /**
   * Formata header della tabella.
   * Viene formattato tutto il tag 'thead'.
   * Usa gli stili di rigel.
   * @param sb accumulatore dell'HTML
   */
  @Override
  public void formatHtmlHeader(StringBuilder sb)
  {
    if(header.isEmpty())
      return;

    sb.append("<thead><tr class='rigel_table_header_row'>\n");
    int col = 0;
    for(Object h : header)
    {
      sb.append("<th class='rigel_table_header_cell'>")
         .append(formatCaption(col, h, "&nbsp;")).append("</th>");
      col++;
    }
    sb.append("\n</tr></thead>\n");
  }

  /**
   * Formata righe della tabella.
   * Viene formattato tutto il tag 'tbody'.
   * @param sb accumulatore dell'HTML
   */
  @Override
  public void formatHtmlRows(StringBuilder sb)
  {
    if(rows.isEmpty())
      return;

    sb.append("<tbody>\n");
    int row = 0;
    for(Collection<? extends Object> valuesrow : rows)
    {
      String style = (row % 2) == 0 ? "rowmenu2" : "rowmenu1";
      sb.append("<tr class='").append(style).append("'>");
      int col = 0;
      for(Object val : valuesrow)
      {
        sb.append("<td>").append(formatValue(row, col, val, "&nbsp;")).append("</td>");
        col++;
      }
      sb.append("</tr>\n");
      row++;
    }
    sb.append("</tbody>\n");
  }

  /**
   * Formattazione completa della tabella.
   * Usa tableHeader e tableFooter per i tag 'table'.
   * @param sb accumulatore dell'HTML
   */
  @Override
  public void formatHtmlTable(StringBuilder sb)
  {
    sb.append("<div class=\"rigel_body\"><div class=\"rigel_htable\">");
    sb.append(tableHeader).append("\n");
    formatHtmlContent(sb);
    sb.append(tableFooter).append("\n");
    sb.append("</div></div>");
  }
}

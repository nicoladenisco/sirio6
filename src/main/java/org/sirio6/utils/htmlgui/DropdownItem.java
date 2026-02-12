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
package org.sirio6.utils.htmlgui;

import org.rigel5.RigelI18nInterface;
import org.sirio6.utils.SU;

/**
 *
 * @author Nicola De Nisco
 */
public class DropdownItem extends HtmlguiElement
{
  public String caption, onclick, link;

  public DropdownItem()
  {
  }

  public DropdownItem(String caption, String onclick)
  {
    this.caption = caption;
    this.onclick = onclick;
  }

  public DropdownItem Caption(String caption)
  {
    this.caption = caption;
    return this;
  }

  public DropdownItem Onclick(String onclick)
  {
    this.onclick = onclick;
    return this;
  }

  public DropdownItem Link(String link)
  {
    this.link = link;
    return this;
  }

  public String onc()
  {
    String onc = SU.okStr(onclick);
    if(!onc.isEmpty())
      return " onclick=\"" + onc + "\"";
    return "";
  }

  public String href()
  {
    String href = SU.okStr(link);
    if(!href.isEmpty())
      return " href=\"" + href + "\"";
    return "href=\"#\"";
  }

  protected String formatCaption(RigelI18nInterface i18n)
  {
    return HtmlguiElement.formatCaption(caption, i18n);
  }

  @Override
  public Button find(String caption)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    sb.append(indent).append("<li><a ").append(onc()).append(" class=\"dropdown-item\" ").append(href())
       .append(">").append(formatCaption(i18n)).append("</a></li>\n");
  }
}

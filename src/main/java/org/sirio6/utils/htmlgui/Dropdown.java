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

import java.util.ArrayList;
import java.util.List;
import org.rigel5.RigelI18nInterface;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapColor;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapComponent;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapSize;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapStyle;
import org.sirio6.utils.htmlgui.bootstrap.CssClassBuilder;

/**
 *
 * @author Nicola De Nisco
 */
public class Dropdown extends Button
{
  public final List<DropdownItem> lsItem = new ArrayList<>();

  public Dropdown()
  {
    component = BootstrapComponent.DROPDOWN;
  }

  public Dropdown(String caption, BootstrapColor color)
  {
    super(caption, color);
    component = BootstrapComponent.DROPDOWN;
  }

  /**
   * Costruttore protetto.
   * Serve solo per la classe derivata DropdownSplit.
   * @param caption
   * @param color
   * @param onclick
   */
  protected Dropdown(String caption, BootstrapColor color, String onclick)
  {
    super(caption, color, onclick);
  }

  public Dropdown addItem(DropdownItem i)
  {
    lsItem.add(i);
    return this;
  }

  public int size()
  {
    return lsItem.size();
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    /*
    == NORMALE ==
    <div class="btn-group" role="group">
      <button type="button" class="btn btn-primary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
        Dropdown
      </button>
      <ul class="dropdown-menu">
        <li><a class="dropdown-item" href="#">Dropdown link</a></li>
        <li><a class="dropdown-item" href="#">Dropdown link</a></li>
      </ul>
    </div>

    == SMALL ==
    <div class="btn-group btn-group-sm" role="group">
      <button class="btn btn-secondary btn-sm dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
        Small button
      </button>
      <ul class="dropdown-menu">
        ...
      </ul>
    </div>
     */

    CssClassBuilder cb = new CssClassBuilder();
    cb.add("btn");
    cb.add(BootstrapStyle.of(BootstrapComponent.BUTTON, color, size, outline));
    cb.addAll(lsMoreClasses);

    String cbclass = "btn-group";
    if(!size.equals(BootstrapSize.NORMAL))
    {
      cbclass += " btn-group-" + size.value();
      cb.add("btn-" + size.value());
    }

    sb.append(indent).append("<div class=\"").append(cbclass).append("\" role=\"group\">\n");

    // il dropdown ignora il valore di onclick
    sb.append(indent).append("  <button class=\"btn ").append(cb.build())
       .append(" dropdown-toggle\" type=\"button\" data-bs-toggle=\"dropdown\">")
       .append(formatCaption(i18n)).append("</button>\n");

    toHtmlListItem(indent + "  ", sb, i18n);
    sb.append(indent).append("</div>\n");
  }

  protected void toHtmlListItem(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    sb.append(indent).append("<ul class=\"dropdown-menu\">\n");
    for(DropdownItem di : lsItem)
      di.toHtml(indent + "  ", sb, i18n);
    sb.append(indent).append("</ul>\n");
  }
}

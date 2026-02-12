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
import org.sirio6.utils.htmlgui.bootstrap.BootstrapColor;
import org.sirio6.utils.htmlgui.bootstrap.CssClassBuilder;

/**
 *
 * @author Nicola De Nisco
 */
public class DropdownSplit extends Dropdown
{
  public DropdownSplit()
  {
  }

  public DropdownSplit(String caption, BootstrapColor color)
  {
    super(caption, color);
  }

  public DropdownSplit(String caption, BootstrapColor color, String onclick)
  {
    super(caption, color, onclick);
  }

  @Override
  public DropdownSplit OnClick(String onClick)
  {
    return (DropdownSplit) super.OnClick(onClick);
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    /*
    <!-- Example split danger button -->
    <div class="btn-group">
      <button type="button" class="btn btn-danger">Danger</button>
      <button type="button" class="btn btn-danger dropdown-toggle dropdown-toggle-split" data-bs-toggle="dropdown" aria-expanded="false">
        <span class="visually-hidden">Toggle Dropdown</span>
      </button>
      <ul class="dropdown-menu">
        <li><a class="dropdown-item" href="#">Action</a></li>
        <li><a class="dropdown-item" href="#">Another action</a></li>
        <li><a class="dropdown-item" href="#">Something else here</a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#">Separated link</a></li>
      </ul>
     </div>
     */

    CssClassBuilder cb = new CssClassBuilder();
    cb.add("btn");
    cb.add("btn-" + color.value());
    cb.addAll(lsMoreClasses);

    sb.append(indent).append("<div class=\"btn-group\">\n");
    sb.append(indent).append("<button ").append(onc()).append(" type=\"button\" class=\"").append(cb.build()).append("\">")
       .append(formatCaption(i18n)).append("</button>\n");
    sb.append(indent).append("<button type=\"button\" class=\"").append(cb.build())
       .append(" dropdown-toggle dropdown-toggle-split\" data-bs-toggle=\"dropdown\">"
          + "<span class=\"visually-hidden\">Toggle Dropdown</span>"
          + "</button>\n");

    toHtmlListItem(indent + "  ", sb, i18n);
    sb.append(indent).append("</div>\n");
  }
}

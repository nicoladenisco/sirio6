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
import org.sirio6.utils.htmlgui.bootstrap.CssClassBuilder;

/**
 *
 * @author Nicola De Nisco
 */
public class ButtonGroup extends HtmlguiElement
{
  public final List<Button> lsButtons = new ArrayList<>();

  public ButtonGroup addButton(Button b)
  {
    lsButtons.add(b);
    return this;
  }

  @Override
  public Button find(String caption)
  {
    return lsButtons.stream().filter((b) -> caption.equals(b.caption)).findFirst().orElse(null);
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    CssClassBuilder cb = new CssClassBuilder();
    cb.add("btn-group");
    cb.add("me-2");
    cb.addAll(lsMoreClasses);

    sb.append(indent).append("<div class=\"").append(cb.build()).append("\" role=\"group\">\n");
    for(Button b : lsButtons)
    {
      b.toHtml(indent + "  ", sb);
    }
    sb.append(indent).append("</div>\n");
  }
}

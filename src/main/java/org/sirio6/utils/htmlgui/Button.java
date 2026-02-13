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
import org.sirio6.utils.htmlgui.bootstrap.BootstrapColor;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapComponent;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapSize;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapStyle;
import org.sirio6.utils.htmlgui.bootstrap.CssClassBuilder;

/**
 *
 * @author Nicola De Nisco
 */
public class Button extends HtmlguiElement
{
  public BootstrapComponent component = BootstrapComponent.BUTTON;
  public BootstrapColor color = BootstrapColor.SECONDARY;
  public String caption = "undefined", onclick = null, link = null;
  public ButtonGroup parent;
  public boolean outline;

  public Button()
  {
  }

  public Button(String caption)
  {
    this.caption = caption;
  }

  public Button(String caption, BootstrapColor color)
  {
    this.caption = caption;
    this.color = color;
  }

  public Button(String caption, BootstrapColor color, String onclick)
  {
    this.caption = caption;
    this.color = color;
    this.onclick = onclick;
  }

  public Button Component(BootstrapComponent component)
  {
    this.component = component;
    return this;
  }

  public Button Color(BootstrapColor color)
  {
    this.color = color;
    return this;
  }

  public Button Caption(String caption)
  {
    this.caption = caption;
    return this;
  }

  public Button OnClick(String onClick)
  {
    this.onclick = onClick;
    return this;
  }

  @Override
  public Button Size(BootstrapSize size)
  {
    return (Button) super.Size(size);
  }

  public Button Outline(boolean outline)
  {
    this.outline = outline;
    return this;
  }

  protected String formatCaption(RigelI18nInterface i18n)
  {
    return HtmlguiElement.formatCaption(caption, i18n);
  }

  @Override
  public Button find(String caption)
  {
    return caption.equals(this.caption) ? this : null;
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    CssClassBuilder cb = new CssClassBuilder();
    cb.add("btn");
    cb.add(BootstrapStyle.of(component, color, size, outline));
    cb.addAll(lsMoreClasses);

    sb.append(indent);
    sb.append("<button ").append(onc()).append(" type=\"button\" class=\"")
       .append(cb.build()).append("\">").append(formatCaption(i18n)).append("</button>\n");
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
}

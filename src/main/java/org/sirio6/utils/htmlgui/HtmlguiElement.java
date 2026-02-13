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
import org.rigel5.DefaultRigelI18nImplementation;
import org.rigel5.RigelI18nInterface;
import org.sirio6.utils.htmlgui.bootstrap.BootstrapSize;

/**
 * Interfaccia di tutti gli elementi gui.
 *
 * @author Nicola De Nisco
 */
abstract public class HtmlguiElement
{
  public static final String NOI18N = "NOI18N:";

  public final List<String> lsMoreClasses = new ArrayList<>();
  public BootstrapSize size = BootstrapSize.NORMAL;

  public HtmlguiElement Size(BootstrapSize size)
  {
    this.size = size;
    return this;
  }

  abstract public Button find(String caption);

  public void toHtml(String indent, StringBuilder sb)
  {
    toHtml(indent, sb, new DefaultRigelI18nImplementation());
  }

  abstract public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n);

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    toHtml("", sb);
    return sb.toString();
  }

  public static String formatCaption(String caption, RigelI18nInterface i18n)
  {
    if(caption == null)
      return "";

    if(caption.startsWith(NOI18N))
      return caption.substring(NOI18N.length(), caption.length());

    return caption.isEmpty() ? "" : i18n.msg(caption);
  }
}

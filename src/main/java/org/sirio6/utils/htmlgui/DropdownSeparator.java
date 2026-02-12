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

/**
 *
 * @author Nicola De Nisco
 */
public class DropdownSeparator extends DropdownItem
{
  public DropdownSeparator()
  {
  }

  @Override
  public void toHtml(String indent, StringBuilder sb, RigelI18nInterface i18n)
  {
    sb.append(indent).append("<li><hr class=\"dropdown-divider\"></li>\n");
  }
}

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
package org.sirio6.utils.htmlgui.forms;

/**
 * Tipi di visualizzazione possibili.
 *
 * 0=Campo semplice 1=Campo area 2=checkbox 3=radio 4=combo
 *
 * @author Nicola De Nisco
 */
public enum ParametroTipoVis
{
  TIPO_VIS_TESTO(0),
  TIPO_VIS_AREA(1),
  TIPO_VIS_CHECK(2),
  TIPO_VIS_RADIO(3),
  TIPO_VIS_COMBO(4);

  private int savetype;

  ParametroTipoVis(int value)
  {
    this.savetype = value;
  }

  public int getParametroTipoVis()
  {
    return savetype;
  }

  public static ParametroTipoVis fromInt(int value)
  {
    for(ParametroTipoVis type : values())
    {
      if(type.savetype == value)
        return type;
    }

    throw new IllegalArgumentException("Tipo di visualizzazione non valido: " + value);
  }
}

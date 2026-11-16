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
 * Tipi di dati possibili.
 *
 * 0=stringa 1=intero 2=double 3=data 4=boolean
 *
 * @author Nicola De Nisco
 */
public enum ParametroTipoDati
{
  TIPO_TESTO(0),
  TIPO_INTERO(1),
  TIPO_FLOAT(2),
  TIPO_DATA(3),
  TIPO_BOOL(4);

  private int savetype;

  ParametroTipoDati(int value)
  {
    this.savetype = value;
  }

  public int getParametroTipoVis()
  {
    return savetype;
  }

  public static ParametroTipoDati fromInt(int value)
  {
    for(ParametroTipoDati type : values())
    {
      if(type.savetype == value)
        return type;
    }

    throw new IllegalArgumentException("Tipo di visualizzazione non valido: " + value);
  }

  public static ParametroTipoDati fromString(String value)
  {
    for(ParametroTipoDati type : values())
    {
      if(type.name().equals(value))
        return type;
    }

    throw new IllegalArgumentException("Tipo di visualizzazione non valido: " + value);
  }
}

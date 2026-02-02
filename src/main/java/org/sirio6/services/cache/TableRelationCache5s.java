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
package org.sirio6.services.cache;

import org.apache.torque.om.Persistent;

/**
 * Versione sicura di TableRelationCache5.
 * Il costruttore obbliga ad usare un descrittore di classe dello stesso tipo dell'oggetto ritornato.<br>
 * Vedi {@see TableRelationCache5}
 *
 * @author Nicola De Nisco
 */
public class TableRelationCache5s<T extends Persistent, O extends Persistent> extends TableRelationCache5<T, O>
{
  public TableRelationCache5s(Class<T> cls)
  {
    super(getCorrectClass(cls));
  }

  private static Class getCorrectClass(Class cls)
  {
    try
    {
      String objClName = cls.getName();
      return Class.forName(objClName + "Peer");
    }
    catch(ClassNotFoundException ex)
    {
      throw new RuntimeException(ex);
    }
  }
}

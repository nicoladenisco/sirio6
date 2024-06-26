/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio6.services.contatori;

/**
 * Classe base eccezioni lock.
 *
 * @author Nicola De Nisco
 */
public class LockException extends Exception
{
  public LockException()
  {
  }

  public LockException(String string)
  {
    super(string);
  }

  public LockException(String string, Throwable thrwbl)
  {
    super(string, thrwbl);
  }

  public LockException(Throwable thrwbl)
  {
    super(thrwbl);
  }

  public LockException(String string, Throwable thrwbl, boolean bln, boolean bln1)
  {
    super(string, thrwbl, bln, bln1);
  }
}

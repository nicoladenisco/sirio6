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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Validatori per i casi più comuni.
 *
 * @author Nicola De Nisco
 */
public class Validatori
{
  public static boolean intNozero(int valore)
  {
    return valore != 0;
  }

  public static boolean intRange(int min, int max, int valore)
  {
    if(valore < min)
      return false;

    if(valore > max)
      return false;

    return true;
  }

  public static final Pattern IPV4 = Pattern.compile(
     "^(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
     + "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
  );

  public static boolean ipV4(String valore)
  {
    if(valore == null)
      return false;

    return IPV4.matcher(valore).matches();
  }

  public static boolean httpUri(String valore)
  {
    if(valore == null)
      return false;

    try
    {
      URI uri = new URI(valore);
      String scheme = uri.getScheme();
      int port = uri.getPort();

      return uri.isAbsolute()
         && ("http".equalsIgnoreCase(scheme)
         || "https".equalsIgnoreCase(scheme))
         && uri.getHost() != null
         && (port == -1 || port <= 65535);
    }
    catch(URISyntaxException e)
    {
      return false;
    }
  }

  // caf916ba-044b-4d9b-b472-60986d738139
  public static final Pattern UUID_PATTERN = Pattern.compile(
     "^[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}$"
  );

  public static boolean uuid(String valore)
  {
    if(valore == null)
      return false;

    return UUID_PATTERN.matcher(valore).matches();
  }
}

/*
 * Copyright (C) 2025 Nicola De Nisco
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
package org.sirio6.utils;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.fulcrum.parser.DefaultParameterParser;

/**
 * Estensione di DefaultParameterParser.
 *
 * @author Nicola De Nisco
 */
public class CoreParameterParser extends DefaultParameterParser
{
  public CoreParameterParser()
  {
  }

  public CoreParameterParser(String characterEncoding)
  {
    super(characterEncoding);
  }

  /**
   * Ridefinita dalla classe base per dare dei default ragionevoli
   * in caso di stringa vuota durante il parsing.
   * @param bean
   * @param prop
   * @throws Exception
   */
  @Override
  protected void setProperty(Object bean, PropertyDescriptor prop)
     throws Exception
  {
    if(prop instanceof IndexedPropertyDescriptor)
    {
      throw new Exception(prop.getName() + " is an indexed property (not supported)");
    }

    Method setter = prop.getWriteMethod();
    if(setter == null)
    {
      throw new Exception(prop.getName() + " is a read only property");
    }

    Class<?> propclass = prop.getPropertyType();
    Object arg = null;

    if(propclass == String.class)
    {
      arg = getString(prop.getName());
    }
    else if(propclass == Byte.class || propclass == Byte.TYPE)
    {
      arg = getByteObject(prop.getName());
    }
    else if(propclass == Integer.class || propclass == Integer.TYPE)
    {
      arg = getIntObject(prop.getName());
    }
    else if(propclass == Long.class || propclass == Long.TYPE)
    {
      arg = getLongObject(prop.getName());
    }
    else if(propclass == Boolean.class || propclass == Boolean.TYPE)
    {
      arg = getBooleanObject(prop.getName(), false);
    }
    else if(propclass == Double.class || propclass == Double.TYPE)
    {
      arg = getDoubleObject(prop.getName());
    }
    else if(propclass == Float.class || propclass == Float.TYPE)
    {
      arg = getFloatObject(prop.getName());
    }
    else if(propclass == BigDecimal.class)
    {
      arg = getBigDecimal(prop.getName());
    }
    else if(propclass == String[].class)
    {
      arg = getStrings(prop.getName());
    }
    else if(propclass == Object.class)
    {
      arg = getObject(prop.getName());
    }
    else if(propclass == int[].class)
    {
      arg = getInts(prop.getName());
    }
    else if(propclass == Integer[].class)
    {
      arg = getIntObjects(prop.getName());
    }
    else if(propclass == Date.class)
    {
      arg = getDate(prop.getName());
    }
    else
    {
      throw new Exception("property "
         + prop.getName()
         + " is of unsupported type "
         + propclass.toString());
    }

    if(arg != null)
      setter.invoke(bean, arg);
  }
}

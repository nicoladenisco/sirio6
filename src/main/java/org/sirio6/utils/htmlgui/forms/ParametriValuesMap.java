/*
 *  ParametriValuesMap.java
 *
 *  Copyright (C) 2025 Dedalus Italia S.p.A
 *
 *  Questo software è proprietà di Dedalus Italia S.p.A
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Dedalus Italia S.p.A
 *  Via di Collodi, 6/C
 *  50141 Firenze
 */
package org.sirio6.utils.htmlgui.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableObject;
import org.commonlib5.utils.DateTime;
import org.commonlib5.utils.StringOper;
import org.sirio6.utils.SU;

/**
 * Mappa speciale per il setup.
 * Converte il carattere ',' in unicode U+1EFF.
 * La virgola è utilizzata come separatore ma può anche essere usata nei campi di input.
 *
 * @author Nicola De Nisco
 */
public class ParametriValuesMap extends HashMap<String, String>
{
  // usa un carattere improbabile per sostituire la virgola
  // U+1EFF Latin Small Letter Y with loop
  public static char replaceVirgola = '\u1Eff';

  public ParametriValuesMap()
  {
  }

  public ParametriValuesMap(String setup)
  {
    super(SU.string2Map(setup, ",\n", true));
  }

  @Override
  public String put(String key, String value)
  {
    String s = SU.okStrNull(value);
    if(s == null)
    {
      remove(key);
      return "";
    }

    return super.put(key, s.replace(',', replaceVirgola));
  }

  public String putObject(String key, Object value)
  {
    String s = SU.okStrNull(value);
    if(s == null)
    {
      remove(key);
      return "";
    }

    return super.put(key, s.replace(',', replaceVirgola));
  }

  @Override
  public String get(Object key)
  {
    String s = SU.okStrNull(super.get(key));
    return s == null ? "" : s.replace(replaceVirgola, ',');
  }

  @Override
  public String getOrDefault(Object key, String defaultValue)
  {
    String s = SU.okStr(super.getOrDefault(key, defaultValue), defaultValue);
    return s == null ? "" : s.replace(replaceVirgola, ',');
  }

  public String getAsString(String key)
  {
    return StringOper.okStr(get(key));
  }

  public MutableObject getAsMutableString(String key)
  {
    return new MutableObject(StringOper.okStr(get(key)));
  }

  public void getAsMutableString(String key, MutableObject obj)
  {
    obj.setValue(StringOper.okStr(get(key)));
  }

  public String getAsStringNull(String key)
  {
    return StringOper.okStrNull(get(key));
  }

  public MutableObject getAsMutableStringNull(String key)
  {
    String s = StringOper.okStrNull(get(key));
    return s == null ? null : new MutableObject(s);
  }

  public void getAsMutableStringNull(String key, MutableObject obj)
  {
    String s = StringOper.okStrNull(get(key));
    if(s != null)
      obj.setValue(s);
  }

  public int getAsInt(String key)
  {
    return getAsInt(key, 0);
  }

  public double getAsDouble(String key)
  {
    return getAsDouble(key, 0.0);
  }

  public boolean getAsBoolean(String key)
  {
    return getAsBoolean(key, false);
  }

  public Date getAsDate(String key)
  {
    return getAsDate(key, null);
  }

  public String getAsString(String key, String defVal)
  {
    return StringOper.okStr(get(key), defVal);
  }

  public int getAsInt(String key, int defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof Number)
      return ((Number) rv).intValue();

    return StringOper.parse(rv, defVal);
  }

  public double getAsDouble(String key, double defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof Number)
      return ((Number) rv).doubleValue();

    return StringOper.parse(rv, defVal);
  }

  public boolean getAsBoolean(String key, boolean defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof Boolean)
      return (Boolean) rv;

    return StringOper.checkTrueFalse(rv, defVal);
  }

  public Date getAsDate(String key, Date defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof Date)
      return (Date) rv;

    return DateTime.parseIsoFull(rv.toString(), defVal);
  }

  public Number getAsNumber(String key, Number defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof Number)
      return (Number) rv;

    return StringOper.parse(rv.toString(), defVal.doubleValue());
  }

  public List getAsList(String key)
  {
    return getAsList(key, Collections.EMPTY_LIST);
  }

  public List getAsList(String key, List defVal)
  {
    Object rv = super.get(key);

    if(rv == null)
      return defVal;

    if(rv instanceof List)
      return (List) rv;

    if(rv instanceof Object[])
      return Arrays.asList((Object[]) rv);

    if(rv instanceof Collection)
      return new ArrayList((Collection) rv);

    return defVal;
  }

  public String join()
  {
    return SU.map2String(this, ",\n", true);
  }
}

/*
 * Copyright (C) 2020 Nicola De Nisco
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

import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import org.apache.torque.om.*;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.DateTime;
import org.commonlib5.utils.StringOper;
import org.sirio6.services.formatter.DataFormatter;
import org.sirio6.services.localization.INT;

/**
 * Classe di utility per la manipolazione dei bean.
 * Offre delle funzioni comode per semplificare la
 * lettura e la scrittura delle informazioni di un bean.
 */
public class BeanWrapper
{
  protected Class beanClass = null;
  protected PropertyDescriptor[] props = null;
  protected PropertyDescriptor prop;
  protected Method getter = null;
  protected Method setter = null;
  protected Class valClass = null;
  protected Object refObject = null;
  protected final HashMap<String, PropertyDescriptor> htProp = new HashMap<>();
  protected final DataFormatter df = (DataFormatter) (TurbineServices.getInstance().
     getService(DataFormatter.SERVICE_NAME));
  public static final String OBJTYPE = "objClassName";

  public BeanWrapper()
  {
  }

  public BeanWrapper(Object bean)
     throws Exception
  {
    setObject(bean);
  }

  public BeanWrapper(Class objClass)
     throws Exception
  {
    makeObject(objClass);
  }

  public void makeObject(Class objClass)
     throws Exception
  {
    beanClass = objClass;
    props = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
    if(refObject == null)
      refObject = beanClass.newInstance();
    makeHash();
  }

  public void setObject(Object bean)
     throws Exception
  {
    refObject = bean;
    if(!bean.getClass().equals(beanClass))
      makeObject(bean.getClass());
  }

  protected void makeHash()
  {
    for(int i = 0; i < props.length; i++)
    {
      htProp.put(props[i].getName().toLowerCase(), props[i]);
    }
  }

  public Object getObject()
  {
    return refObject;
  }

  protected PropertyDescriptor findPropInternal(String propName)
     throws Exception
  {
    PropertyDescriptor pd;
    if((pd = findProp(propName)) == null)
      throw new Exception(INT.I("La proprieta' %s non esiste nell'oggetto!", propName));

    return pd;
  }

  public PropertyDescriptor findProp(String propName)
     throws Exception
  {
    return (PropertyDescriptor) (htProp.get(propName.toLowerCase()));
  }

  public void setPropDescr(String propName)
     throws Exception
  {
    setPropDescr(findPropInternal(propName));
  }

  public void setPropDescr(int idx)
     throws Exception
  {
    setPropDescr(props[idx]);
  }

  public void setPropDescr(PropertyDescriptor Prop)
     throws Exception
  {
    prop = Prop;
    if(prop != null)
    {
      if(prop instanceof IndexedPropertyDescriptor)
        throw new Exception(INT.I("%s e' una proprieta' con indice(non supportata)", prop.getName()));

      setValClass(prop.getPropertyType());

      getter = prop.getReadMethod();
      setter = prop.getWriteMethod();
    }
  }

  public synchronized Object getValue()
     throws Exception
  {
    return getter == null ? null : getter.invoke(refObject);
  }

  public synchronized void setValue(Object value)
     throws Exception
  {
    if(setter == null)
      return;

    setter.invoke(refObject, value);
  }

  public synchronized Object getValue(String propName)
     throws Exception
  {
    setPropDescr(propName);
    return getValue();
  }

  public synchronized Object getValue(int propIdx)
     throws Exception
  {
    setPropDescr(propIdx);
    return getValue();
  }

  public synchronized void setValueFromMap(Map values, String propName)
     throws Exception
  {
    setValue(values.get(propName), propName);
  }

  public synchronized void setValueFromMap(Map values, String mapName, String propName)
     throws Exception
  {
    setValue(values.get(mapName), propName);
  }

  public synchronized void setValueParseFromMap(Map values, String propName)
     throws Exception
  {
    setValueParseFromMap(values, propName, propName);
  }

  public synchronized void setValueParseFromMap(Map values, String mapName, String propName)
     throws Exception
  {
    String value = StringOper.okStrNull(values.get(mapName));

    setPropDescr(propName);
    Class vcl = getValClass();
    setValue(parseValueNull(value, vcl));
  }

  public synchronized void setValue(Object value, String propName)
     throws Exception
  {
    setPropDescr(propName);
    setValue(value);
  }

  public synchronized void setValue(Object value, int propIdx)
     throws Exception
  {
    setPropDescr(propIdx);
    setValue(value);
  }

  public synchronized String getValFormat(String propName)
     throws Exception
  {
    return formatValue(getValue(propName));
  }

  public synchronized String getValFormat(int propIdx)
     throws Exception
  {
    return formatValue(getValue(propIdx));
  }

  public synchronized void setValParse(String value, String propName)
     throws Exception
  {
    setPropDescr(propName);
    setValue(parseValue(value));
  }

  public synchronized void setValParse(String value, int propIdx)
     throws Exception
  {
    setPropDescr(propIdx);
    setValue(parseValue(value));
  }

  public synchronized void setValParse(String value, PropertyDescriptor pd)
     throws Exception
  {
    setPropDescr(pd);
    setValue(parseValue(value));
  }

  public void setValClass(Class cl)
  {
    String clName = cl.getName();

    if(clName.equals("float"))
    {
      valClass = Float.class;
      return;
    }

    if(clName.equals("double"))
    {
      valClass = Double.class;
      return;
    }

    if(clName.equals("int"))
    {
      valClass = Integer.class;
      return;
    }

    if(clName.equals("boolean"))
    {
      valClass = Boolean.class;
      return;
    }

    valClass = cl;
  }

  public Class getValClass()
  {
    return valClass;
  }

  public PropertyDescriptor[] getPropDescriptors()
  {
    return props;
  }

  public Enumeration descriptors()
  {
    return new Enumeration()
    {
      int idx = 0;

      @Override
      public boolean hasMoreElements()
      {
        while(idx < props.length)
        {
          prop = props[idx++];
          if(prop != null)
          {
            if(!(prop instanceof IndexedPropertyDescriptor))
            {
              getter = prop.getReadMethod();
              setter = prop.getWriteMethod();
              if(getter != null && setter != null)
              {
                setValClass(prop.getPropertyType());
                return true;
              }
            }
          }
        }
        return false;
      }

      @Override
      public Object nextElement()
      {
        return prop;
      }
    };
  }

  /**
   * Converte la stringa in ingresso in un tipo
   * compatibile con la proprietà corrente.
   * L'operazione è specualare a formatValue.
   *
   * @param value stringa con la rappresentazione del valore
   * @return valore opportunamente computato
   * @throws Exception
   */
  public Object parseValue(String value)
     throws Exception
  {
    Class vcl = getValClass();
    return parseValue(value, vcl);
  }

  public static Object parseValue(String value, Class vcl)
     throws Exception
  {
    boolean invalid = value == null || value.trim().length() == 0;

    if(vcl.equals(Integer.class))
      return invalid ? Integer.valueOf(0) : Integer.valueOf(value);
    if(vcl.equals(Float.class))
      return invalid ? Float.valueOf(0.0f) : Float.valueOf(value);
    if(vcl.equals(Boolean.class))
      return invalid ? false : StringOper.checkTrueFalse(value, false);
    if(vcl.equals(Double.class))
      return invalid ? Double.valueOf(0.0) : Double.valueOf(value);

    if(vcl.equals(java.sql.Timestamp.class))
    {
      java.util.Date dtmp = invalid ? new java.util.Date() : (java.util.Date) DateTime.parseIsoFull(value, null);
      return new java.sql.Timestamp(dtmp.getTime());
    }

    if(vcl.equals(java.sql.Date.class))
    {
      java.util.Date dtmp = invalid ? new java.util.Date() : (java.util.Date) DateTime.parseIsoFull(value, null);
      return new java.sql.Date(dtmp.getTime());
    }

    if(vcl.equals(java.util.Date.class))
      return invalid ? new java.util.Date() : DateTime.parseIsoFull(value, null);

    return value;
  }

  public static Object parseValueNull(String value, Class vcl)
     throws Exception
  {
    boolean invalid = value == null || value.trim().length() == 0;

    if(vcl.equals(Integer.class))
      return invalid ? Integer.valueOf(0) : Integer.valueOf(value);
    if(vcl.equals(Float.class))
      return invalid ? Float.valueOf(0.0f) : Float.valueOf(value);
    if(vcl.equals(Boolean.class))
      return invalid ? false : StringOper.checkTrueFalse(value, false);
    if(vcl.equals(Double.class))
      return invalid ? Double.valueOf(0.0) : Double.valueOf(value);

    if(invalid)
      return null;

    if(vcl.equals(java.sql.Timestamp.class))
    {
      java.util.Date dtmp = (java.util.Date) DateTime.parseIsoFull(value, null);
      return new java.sql.Timestamp(dtmp.getTime());
    }

    if(vcl.equals(java.sql.Date.class))
    {
      java.util.Date dtmp = (java.util.Date) DateTime.parseIsoFull(value, null);
      return new java.sql.Date(dtmp.getTime());
    }

    if(vcl.equals(java.util.Date.class))
      return DateTime.parseIsoFull(value, null);

    return value;
  }

  /**
   * Formatta come stringa il valore indicato.
   * Il tipo della proprietà corrente determina
   * il valore di ritorno.
   *
   * @param value valore da formattare
   * @return equivalente stringa del dato
   * @throws Exception
   */
  public String formatValue(Object value)
     throws Exception
  {
    Class vcl = getValClass();
    return formatValue(value, vcl);
  }

  public static String formatValue(Object value, Class vcl)
     throws Exception
  {
    boolean invalid = value == null;

    if(vcl.equals(Integer.class))
      return invalid ? "0" : value.toString();
    if(vcl.equals(Float.class))
      return invalid ? "0" : value.toString();
    if(vcl.equals(Boolean.class))
      return invalid ? "false" : value.toString();
    if(vcl.equals(Double.class))
      return invalid ? "0" : value.toString();

    if(vcl.equals(java.util.Date.class))
      return invalid ? "" : DateTime.formatIsoFull((Date) value);
    if(vcl.equals(java.sql.Date.class))
      return invalid ? "" : DateTime.formatIsoFull((Date) value);

    if(vcl.equals(StringKey.class))
      return invalid ? "" : value.toString();
    if(vcl.equals(NumberKey.class))
      return invalid ? "0" : value.toString();

    // comportamento di default
    return invalid ? "" : value.toString();
  }

  /**
   * Ritorna il valore corrispondente a null
   * per il tipo della proprietà corrente.
   *
   * @return
   * @throws Exception
   */
  public Object nullValue()
     throws Exception
  {
    return parseValue(null);
  }

  /**
   * Ritorna il nome del campo per la proprietà richiesta.
   *
   * @param i indice della proprietà del bean
   * @return nome del campo da usare in objExport/objImport
   */
  public String getFldName(int i)
  {
    return "HTV_" + props[i].getDisplayName().toUpperCase();
  }

  /**
   * Popola hashtable con valori del bean.
   * Vengono estratti tutti i valori del bean compatibili
   * con tipi base (vedi checkForType).
   * Nella tabella di viene inserita una voce
   * con chiave OBJTYPE contenente il nome completo
   * della classe dell'bean; può essere utilizzato
   * per creare una nuova istanza dell'oggetto.
   *
   * @param ht tabella di hashing dove accodare valori (può essere null)
   * @param prefix prefisso per le chiavi inserite
   * @return tabella di hashing con chiavi/valore.
   * @throws Exception
   */
  public Map<String, String> objExport(Map<String, String> ht, String prefix)
     throws Exception
  {
    if(ht == null)
      ht = new HashMap<>();

    Object val;
    for(int i = 0; i < props.length; i++)
    {
      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      if((val = getValue()) != null)
        ht.put(prefix + getFldName(i), formatValue(val));
    }

    ht.put(prefix + OBJTYPE, refObject.getClass().getName());
    return ht;
  }

  public Map<String, String> objExportPure(Map<String, String> ht, String prefix)
     throws Exception
  {
    if(ht == null)
      ht = new HashMap<>();

    Object val;
    for(int i = 0; i < props.length; i++)
    {
      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      if((val = getValue()) != null)
      {
        String propName = prefix + props[i].getDisplayName().toUpperCase();
        ht.put(propName, formatValue(val));

        if(val instanceof java.util.Date)
        {
          // caso speciale per la data: oltre alla formattazione FULL
          // aggiunge formattazione solo DATA o solo ORA
          ht.put(propName + "_DATA", df.formatData((java.util.Date) val));
          ht.put(propName + "_ORA", df.formatTime((java.util.Date) val));
          ht.put(propName + "_ORA2", df.formatTimeFull((java.util.Date) val));
        }
      }
    }

    ht.put(prefix + OBJTYPE, refObject.getClass().getName());
    return ht;
  }

  public int objImportPure(Map<String, String> ht, String prefix)
     throws Exception
  {
    String val;
    int count = 0;
    for(int i = 0; i < props.length; i++)
    {
      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      String propName = prefix + props[i].getDisplayName().toUpperCase();
      if((val = ht.get(propName)) != null)
      {
        setValue(parseValue(val));
        count++;
      }
    }

    return count;
  }

  public Set<String> objFieldNamePure(Set<String> st, String prefix)
     throws Exception
  {
    if(st == null)
      st = new HashSet<>();

    for(int i = 0; i < props.length; i++)
    {
      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      String propName = prefix + props[i].getDisplayName().toUpperCase();
      st.add(propName);

      if(getValClass().equals(java.util.Date.class))
      {
        // caso speciale per la data: oltre alla formattazione FULL
        // aggiunge formattazione solo DATA o solo ORA
        st.add(propName + "_DATA");
        st.add(propName + "_ORA");
        st.add(propName + "_ORA2");
      }
    }

    st.add(prefix + OBJTYPE);
    return st;
  }

  /**
   * Popola il bean con i valori della hashtble.
   * Legge i valori dalla Map e li inserisce
   * nelle rispettive proprietà del bean.
   *
   * @param ht tabella di hashing con i valori
   * @param prefix prefisso per le chiavi inserite
   * @throws Exception
   */
  public void objImport(Map<String, String> ht, String prefix)
     throws Exception
  {
    String val;
    for(int i = 0; i < props.length; i++)
    {
      if((val = ht.get(prefix + getFldName(i))) != null)
        setValParse(val, i);
    }
  }

  /**
   * Test sul tipo del valore corrente.
   *
   * @return vero se è un tipo base
   */
  public boolean checkForType()
  {
    return checkForType(getValClass().getName());
  }

  /**
   * Ritorna vero se la classe e' fra quelle di nostro interesse
   * per il trasferimento dei peer.
   * @param clName
   * @return vero se il tipo corrisponde
   */
  static public boolean checkForType(String clName)
  {
    switch(clName)
    {
      case "java.lang.String":
        return true;
      case "java.lang.Integer":
        return true;
      case "java.lang.Float":
        return true;
      case "java.lang.Boolean":
        return true;
      case "java.lang.Double":
        return true;
      case "java.util.Date":
        return true;
      case "org.apache.turbine.om.StringKey":
        return true;
      case "org.apache.turbine.om.NumberKey":
        return true;
    }
    return false;
  }

  /**
   * Esporta le proprietà del bean in un vettore di stringhe.
   * I valori non validi sono passati come null.
   * L'ordine deve essere tassativamente quello
   * delle proprietà (è l'inverso di objImport).
   * @return vettore con le stringhe
   * @throws Exception
   */
  public String[] objExport()
     throws Exception
  {
    String[] rv = new String[props.length];

    Object val;
    for(int i = 0; i < props.length; i++)
    {
      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      if((val = getValue()) != null)
        rv[i] = formatValue(val);
    }

    return rv;
  }

  /**
   * Popola il bean con i valori del vettore.
   * Legge i valori dal vettore e li inserisce
   * nelle rispettive proprietà del bean.
   * L'ordine deve essere tassativamente quello
   * delle proprietà (è l'inverso di objExport).
   * @param v vettore on i dati
   * @throws Exception
   */
  public void objImport(String[] v)
     throws Exception
  {
    for(int i = 0; i < props.length; i++)
    {
      if(v[i] == null)
        continue;

      if(props[i] instanceof IndexedPropertyDescriptor)
        continue;

      setPropDescr(i);
      if(!checkForType())
        continue;

      setValue(parseValue(v[i]));
    }
  }

  /**
   * Copia i valori possibili da un oggetto
   * della stessa classe dell'oggetto corrente.
   * @param obj oggetto da copiare
   * @return numero di campi modificati
   * @throws Exception
   */
  public int copyFrom(Object obj)
     throws Exception
  {
    int count = 0;

    if(obj.getClass().equals(beanClass))
      throw new IllegalArgumentException("Oggetti di classi differenti.");

    for(int i = 0; i < props.length; i++)
    {
      PropertyDescriptor ps = props[i];
      if(!(ps instanceof IndexedPropertyDescriptor))
      {
        Method lgetter = ps.getReadMethod();
        Method lsetter = ps.getWriteMethod();

        try
        {
          if(lgetter != null && lsetter != null)
          {
            Object value = lgetter.invoke(obj, (java.lang.Object[]) null);
            lsetter.invoke(refObject, value);
            count++;
          }
        }
        catch(Throwable t)
        {
        }
      }
    }

    return count;
  }

  /**
   * Importa i valori possibili di un oggetto
   * di classe diversa.
   * Cerca nell'oggetto da importare tutti i
   * parametri con lo stesso nome e cerca di
   * importarne il valore.
   * @param obj oggetto da importare
   * @param ignoreNull se vero ignora i valori null quando letti
   * @return numero di campi modificati
   * @throws Exception
   */
  public int importFrom(Object obj, boolean ignoreNull)
     throws Exception
  {
    return importFrom(obj, ignoreNull, (String[]) null);
  }

  public int importFrom(Object obj, boolean ignoreNull, String... ignoreFields)
     throws Exception
  {
    int count = 0;
    Class objClass = obj.getClass();
    PropertyDescriptor[] pObj = Introspector.getBeanInfo(objClass).getPropertyDescriptors();

    if(ignoreFields != null)
      Arrays.sort(ignoreFields);

    for(int i = 0; i < pObj.length; i++)
    {
      PropertyDescriptor ps = pObj[i];
      if(!(ps instanceof IndexedPropertyDescriptor))
      {
        Method lgetter = ps.getReadMethod();
        if(lgetter != null)
        {
          final String pName = ps.getName().toLowerCase();
          if(ignoreFields != null && Arrays.binarySearch(ignoreFields, pName) >= 0)
            continue;

          PropertyDescriptor po = (PropertyDescriptor) htProp.get(pName);
          if(po != null && !(po instanceof IndexedPropertyDescriptor))
          {
            Method lsetter = po.getWriteMethod();

            try
            {
              if(lgetter != null && lsetter != null)
              {
                Object value = lgetter.invoke(obj);
                if(!(ignoreNull && value == null))
                {
                  lsetter.invoke(refObject, value);
                  count++;
                }
              }
            }
            catch(Throwable t)
            {
            }
          }
        }
      }
    }

    return count;
  }
}

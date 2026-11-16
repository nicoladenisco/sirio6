/*
 *  Parametro.java
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.commonlib5.utils.ArrayMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sirio6.utils.DT;
import org.sirio6.utils.SU;

/**
 * Parametro per una device.
 * Una istanza di questa classe raggruppa un parametro per al device.
 * La gestione di edit, salvataggio, recupero viene automatizzata.
 * Vedi anche ParametroDeviceBlocco.
 *
 * ATTENZIONE: le stringhe non vanno internazionalizzate; lo saranno al momento dell'uso.
 *
 * @author Nicola De Nisco
 * @param <T>
 */
public class Parametro<T>
{
  public static final String VUOTO_PARAMETRO = "";
  public static final String DEFAULT_PARAMETRO = "DEFAULT";

  protected String codice, nome, descrizione, helpBlock;
  protected T val, defval;
  protected ParametroTipoDati tipo;
  protected ParametroTipoVis visualizzazione;
  protected boolean modified, error;
  protected Function<T, Boolean> validatore;

  protected final Map<T, String> listaValori = new ArrayMap<>();
  protected final List<T> listaAutoCompleta = new ArrayList<>();

  /**
   * Costruisce il parametro.
   * @param blocco riferimento al blocco parametri
   * @param codice il codice identificativo
   * @param nome nome breve del parametro
   * @param descrizione la descrizione del parametro
   * @param helpBlock il messaggio di help inserito sotto il campo
   * @param defval il valore di default; <b>non puo essere nullo</b>
   * @param visualizzazione TIPO_VIS_... 0=Campo semplice 1=Campo area 2=checkbox 3=radio 4=combo
   * @param listaValori lista valori possibili (obbligatorio e non vuota se visualizzazione=3/4)
   */
  public Parametro(BloccoParametri blocco, String codice,
     String nome, String descrizione, String helpBlock,
     T defval, ParametroTipoVis visualizzazione, Map<T, String> listaValori)
  {
    this(blocco, codice, nome, descrizione, helpBlock, defval, visualizzazione, listaValori, Collections.EMPTY_LIST, false);
  }

  /**
   * Costruisce il parametro.
   * @param blocco riferimento al blocco parametri
   * @param codice il codice identificativo
   * @param nome nome breve del parametro
   * @param descrizione la descrizione del parametro
   * @param helpBlock il messaggio di help inserito sotto il campo
   * @param defval il valore di default; <b>non puo essere nullo</b>
   * @param visualizzazione TIPO_VIS_... 0=Campo semplice 1=Campo area 2=checkbox 3=radio 4=combo
   * @param listaAutoCompleta lista valori per autocompletamento
   */
  public Parametro(BloccoParametri blocco, String codice,
     String nome, String descrizione, String helpBlock,
     T defval, ParametroTipoVis visualizzazione, List<T> listaAutoCompleta)
  {
    this(blocco, codice, nome, descrizione, helpBlock, defval, visualizzazione, Collections.EMPTY_MAP, listaAutoCompleta, false);
  }

  /**
   * Costruisce il parametro.
   * @param blocco riferimento al blocco parametri
   * @param codice il codice identificativo
   * @param nome nome breve del parametro
   * @param descrizione la descrizione del parametro
   * @param helpBlock il messaggio di help inserito sotto il campo
   * @param defval il valore di default; <b>non puo essere nullo</b>
   * @param visualizzazione TIPO_VIS_... 0=Campo semplice 1=Campo area 2=checkbox 3=radio 4=combo
   */
  public Parametro(BloccoParametri blocco, String codice,
     String nome, String descrizione, String helpBlock,
     T defval, ParametroTipoVis visualizzazione)
  {
    this(blocco, codice, nome, descrizione, helpBlock, defval, visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, false);
  }

  private Parametro(BloccoParametri blocco, String codice,
     String nome, String descrizione, String helpBlock,
     T defval, ParametroTipoVis visualizzazione, Map<T, String> listaValori, List<T> listaAutoCompleta, boolean buildInternal)
  {
    if(defval == null)
      throw new NullPointerException("Il valore di default non puo essere null.");

    if(!buildInternal
       && (visualizzazione == ParametroTipoVis.TIPO_VIS_COMBO || visualizzazione == ParametroTipoVis.TIPO_VIS_RADIO)
       && listaValori.isEmpty())
      throw new IllegalArgumentException("Richiesto combobox/radio ma la lista valori è vuota.");

    this.val = defval;
    this.defval = defval;
    this.codice = codice;
    this.nome = nome;
    this.descrizione = descrizione;
    this.helpBlock = helpBlock;
    this.tipo = checkTipo(defval);
    this.visualizzazione = visualizzazione;
    this.listaValori.putAll(listaValori);
    this.listaAutoCompleta.addAll(listaAutoCompleta);

    if(blocco != null)
      blocco.addParametro(this);
  }

  /**
   * Verifica il tipo del valore passato.
   * @param valore
   * @return 0=stringa 1=intero 2=double 3=data 4=boolean
   */
  public ParametroTipoDati checkTipo(T valore)
  {
    // 0=stringa 1=intero 2=double 3=data 4=boolean
    Object o = valore;

    if(o instanceof String)
      return ParametroTipoDati.TIPO_TESTO;

    if(o instanceof Integer)
      return ParametroTipoDati.TIPO_INTERO;

    if(o instanceof Double)
      return ParametroTipoDati.TIPO_FLOAT;

    if(o instanceof Date)
      return ParametroTipoDati.TIPO_DATA;

    if(o instanceof Boolean)
      return ParametroTipoDati.TIPO_BOOL;

    throw new IllegalArgumentException("Tipo non supportato.");
  }

  public Object convert(ParametroTipoDati tipo, Object value, Object defval)
  {
    // 0=stringa 1=intero 2=double 3=data 4=boolean
    switch(tipo)
    {
      case TIPO_TESTO:
        return SU.okStr(value, (String) defval);
      case TIPO_INTERO:
        return SU.parse(value, (Integer) defval);
      case TIPO_FLOAT:
        return SU.parse(value, (Double) defval);
      case TIPO_DATA:
        return DT.parseIsoAuto((String) value, (Date) defval);
      case TIPO_BOOL:
        return SU.checkTrueFalse(value, (Boolean) defval);
      default:
        return defval;
    }
  }

  public String getValueAsString()
  {
    String s = convert(tipo, val, defval).toString();
    return DEFAULT_PARAMETRO.equals(s) ? "" : s;
  }

  public String getDefaultValueAsString()
  {
    // 0=stringa 1=intero 2=double 3=data 4=boolean
    switch(tipo)
    {
      case TIPO_TESTO:
      case TIPO_INTERO:
      case TIPO_FLOAT:
        return SU.okStr(defval);
      case TIPO_DATA:
        return DT.formatIso((Date) defval, "");
      case TIPO_BOOL:
        return (Boolean) defval ? "1" : "0";
      default:
        return defval.toString();
    }
  }

  public void parseString(String valore)
  {
    error = false;
    val = (T) convert(tipo, valore, defval);

    if(validatore != null)
      error = !validatore.apply(val);
  }

  public boolean isValid()
  {
    return val != null && !DEFAULT_PARAMETRO.equals(val);
  }

  public String getCodice()
  {
    return codice;
  }

  public void setCodice(String codice)
  {
    if(!SU.isEqu(this.codice, codice))
    {
      this.codice = codice;
      modified = true;
    }
  }

  public String getNome()
  {
    return nome;
  }

  public void setNome(String nome)
  {
    if(!SU.isEqu(this.nome, nome))
    {
      this.nome = nome;
      modified = true;
    }
  }

  public String getDescrizione()
  {
    return descrizione;
  }

  public void setDescrizione(String descrizione)
  {
    if(!SU.isEqu(this.descrizione, descrizione))
    {
      this.descrizione = descrizione;
      modified = true;
    }
  }

  public T getVal()
  {
    return val;
  }

  public void setVal(T val)
  {
    if(!SU.isEqu(this.val, val))
    {
      this.val = val;
      modified = true;
    }
  }

  public T getDefval()
  {
    return defval;
  }

  public void setDefval(T defval)
  {
    if(!SU.isEqu(this.defval, defval))
    {
      this.defval = defval;
      modified = true;
    }
  }

  public ParametroTipoDati getTipo()
  {
    return tipo;
  }

  public void setTipo(ParametroTipoDati tipo)
  {
    if(!SU.isEqu(this.tipo, tipo))
    {
      this.tipo = tipo;
      modified = true;
    }
  }

  public ParametroTipoVis getVisualizzazione()
  {
    return visualizzazione;
  }

  public void setVisualizzazione(ParametroTipoVis visualizzazione)
  {
    if(!SU.isEqu(this.visualizzazione, visualizzazione))
    {
      this.visualizzazione = visualizzazione;
      modified = true;
    }
  }

  public List<T> getListaAutoCompleta()
  {
    return listaAutoCompleta;
  }

  public void setListaAutoCompleta(Collection<T> listaAutoCompleta)
  {
    if(!SU.isEqu(this.listaAutoCompleta, listaAutoCompleta))
    {
      this.listaAutoCompleta.clear();
      this.listaAutoCompleta.addAll(listaAutoCompleta);
      modified = true;
    }
  }

  public void addListaAutoCompleta(T valore)
  {
    this.listaAutoCompleta.add(valore);
    modified = true;
  }

  public void addListaAutoCompleta(Collection<T> valori)
  {
    listaAutoCompleta.addAll(valori);
    modified = true;
  }

  public void clearListaAutoCompleta()
  {
    listaAutoCompleta.clear();
  }

  public Map<T, String> getListaValori()
  {
    return listaValori;
  }

  public void setListaValori(Map<T, String> listaValori)
  {
    if(!SU.isEqu(this.listaValori, listaValori))
    {
      this.listaValori.clear();
      this.listaValori.putAll(listaValori);
      modified = true;
    }
  }

  public void addListaValori(T valore, String descrizione)
  {
    this.listaValori.put(valore, descrizione);
    modified = true;
  }

  public void clearListaValori()
  {
    listaValori.clear();
  }

  public String getHelpBlock()
  {
    return helpBlock;
  }

  public void setHelpBlock(String helpBlock)
  {
    if(!SU.isEqu(this.helpBlock, helpBlock))
    {
      this.helpBlock = helpBlock;
      modified = true;
    }
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  public boolean isError()
  {
    return error;
  }

  public void setError(boolean error)
  {
    this.error = error;
  }

  public Function<T, Boolean> getValidatore()
  {
    return validatore;
  }

  public void setValidatore(Function<T, Boolean> validatore)
  {
    this.validatore = validatore;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.codice);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    final Parametro<?> other = (Parametro<?>) obj;
    return Objects.equals(this.codice, other.codice);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Parametro{");
    sb.append("codice=").append(codice);
    sb.append(", nome=").append(nome);
    sb.append(", val=").append(val);
    sb.append('}');
    return sb.toString();
  }

  public static Parametro<?> buildFromJson(JSONObject jo)
     throws Exception
  {
    ParametroTipoDati tipo = ParametroTipoDati.fromString(jo.getString("tipo"));
    ParametroTipoVis visualizzazione = ParametroTipoVis.fromString(jo.getString("visualizzazione"));

    Object val = jo.get("val");
    Object defval = jo.opt("defval");
    String codice = jo.getString("codice");
    String nome = jo.getString("nome");
    String descrizione = jo.getString("descrizione");
    String helpBlock = jo.optString("helpBlock", descrizione);

    Parametro rv = null;

    // 0=stringa 1=intero 2=double 3=data 4=boolean
    switch(tipo)
    {
      case TIPO_TESTO:
        rv = new Parametro<>(null, codice, nome, descrizione, helpBlock, "", visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, true);
        break;
      case TIPO_INTERO:
        rv = new Parametro<>(null, codice, nome, descrizione, helpBlock, 0, visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, true);
        break;
      case TIPO_FLOAT:
        rv = new Parametro<>(null, codice, nome, descrizione, helpBlock, 0.0, visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, true);
        break;
      case TIPO_DATA:
        rv = new Parametro<>(null, codice, nome, descrizione, helpBlock, new Date(), visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, true);
        break;
      case TIPO_BOOL:
        rv = new Parametro<>(null, codice, nome, descrizione, helpBlock, false, visualizzazione, Collections.EMPTY_MAP, Collections.EMPTY_LIST, true);
        break;
      default:
        throw new IllegalArgumentException("Il tipo richiesto non è supportato");
    }

    if(defval != null)
      rv.defval = rv.convert(tipo, defval, rv.defval);

    rv.val = rv.convert(tipo, val, rv.defval);

    JSONArray listaValori = jo.optJSONArray("listaValori");
    if(listaValori != null)
    {
      for(int i = 0; i < listaValori.length(); i++)
      {
        JSONObject jv = listaValori.getJSONObject(i);

        Object valore = jv.get("val");
        String descri = jv.getString("des");

        rv.addListaValori(rv.convert(tipo, valore, rv.defval), descri);
      }
    }

    JSONArray listaAutoCompleta = jo.optJSONArray("listaAutoCompleta");
    if(listaAutoCompleta != null)
    {
      for(int i = 0; i < listaAutoCompleta.length(); i++)
      {
        String valore = listaAutoCompleta.getString(i);
        rv.addListaAutoCompleta(rv.convert(tipo, valore, rv.defval));
      }
    }

    return rv;
  }
}

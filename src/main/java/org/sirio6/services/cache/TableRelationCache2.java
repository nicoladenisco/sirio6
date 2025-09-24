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
package org.sirio6.services.cache;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.SimpleKey;

/**
 * Cache per relazioni fra tabelle.
 * Se ho un array di peer (ES: List[AcAccettazioni]) consente di caricare
 * in una unica query tutti i peer collegati (ES: List[AnAnagrafiche]) e
 * quindi di recuparli attraverso le funzioni findByPrimaryKey.
 * NON utilizza la GlobalCache.
 *
 * @author Nicola De Nisco
 * @param <T> Tipo di oggetti recuperati
 * @param <O> Tipo di oggetti origine
 * @deprecated usa TableRelationCache4 per una implementazione migliore e semplificata
 */
@Deprecated
public class TableRelationCache2<T extends Persistent, O extends Persistent> extends ArrayList<T>
   implements TableRelationLink<T, O>
{
  private final Map<ObjectKey, Persistent> mapValues = new HashMap<>();

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsDettails per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, Collection<O> lsDettails, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add((Integer) getLinkM.invoke(obj));

    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!pks.isEmpty())
      loadDataFromDetail(cls, pks, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsDettails per estrarre la chiave primaria; può contentere sia il nome
   * campo Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure
   * PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, Collection<O> lsDettails, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    if(campoLink.startsWith("PEER:"))
    {
      campoLink = campoLink.substring(5);
      for(O obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByPeerName(campoLink));
      }
    }
    else
    {
      for(O obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByName(campoLink));
      }
    }

    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, pks, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del detail
   * da cercare poi nel master; è la chiave esterna del detail che corrispone alla primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, Collection<O> lsDettails, Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add(fnMap.apply(obj));

    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!pks.isEmpty())
      loadDataFromDetail(cls, pks, con);
  }

  public TableRelationCache2(Class cls, Function<O, ObjectKey> fnMap, Collection<O> lsDettails, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<ObjectKey> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, primaryKeys, con);
  }

  protected void loadDataFromDetail(Class cls, Collection<ObjectKey> primaryKeys, Connection con)
     throws Exception
  {
    if(!cls.getName().endsWith("Peer"))
      throw new RuntimeException("Deve essere un oggetto Peer.");

    // recupera tutti i record collegati attraverso il metodo
    Method getRecords = getMetodPrimary(cls);
    List lsValues = (List) getRecords.invoke(null, primaryKeys, con);
    loadData(lsValues);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsMasters per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, ColumnMap nomeCampo, Collection<O> lsMasters, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(O obj : lsMasters)
      primaryKeys.add((Integer) getLinkM.invoke(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsMasters per estrarre la chiave primaria; può contentere sia il nome campo
   * Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, ColumnMap nomeCampo, Collection<O> lsMasters, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    if(campoLink.startsWith("PEER:"))
    {
      campoLink = campoLink.substring(5);
      for(O obj : lsMasters)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByPeerName(campoLink));
      }
    }
    else
    {
      for(O obj : lsMasters)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByName(campoLink));
      }
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del master
   * da cercare poi nel detail; in genere è la primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, ColumnMap nomeCampo, Collection<O> lsMasters,
     Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(O obj : lsMasters)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Questo costruttore è utile quando ho già selezionato le primary key del master
   * da cercare negli oggetti detail. Questo è conveniente se devo caricare più detail
   * da un unico insieme di master.
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master (nomeTabella.nomeCampo)
   * @param primaryKeysMasters primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache2(Class cls, ColumnMap nomeCampo, Collection<Integer> primaryKeysMasters, Connection con)
     throws Exception
  {
    if(!primaryKeysMasters.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeysMasters, cls, con);
  }

  protected void loadDataFromMaster(ColumnMap nomeCampo, Collection<Integer> primaryKeys, Class cls, Connection con)
     throws Exception
  {
    if(!cls.getName().endsWith("Peer"))
      throw new RuntimeException("Deve essere un oggetto Peer.");

    Criteria c = new Criteria();
    c.whereIn(nomeCampo, primaryKeys);
    Method doSelect = cls.getMethod("doSelect", Criteria.class, Connection.class);
    List lsValues = (List) doSelect.invoke(null, c, con);
    loadData(lsValues);
  }

  protected void loadData(List lsValues)
     throws Exception
  {
    addAll(lsValues);

    for(int i = 0; i < lsValues.size(); i++)
    {
      Persistent val = (Persistent) lsValues.get(i);
      mapValues.put(val.getPrimaryKey(), val);
    }
  }

  /**
   * Recupera il record richiesto.
   * @param id chiave primaria
   * @return oggetto oppure null
   */
  public T findByPrimaryKey(int id)
  {
    return findByPrimaryKey(new NumberKey(id));
  }

  /**
   * Recupera il record richiesto.
   * @param toSearch chiave primaria
   * @return oggetto oppure null
   */
  public T findByPrimaryKey(ObjectKey toSearch)
  {
    return (T) mapValues.get(toSearch);
  }
}

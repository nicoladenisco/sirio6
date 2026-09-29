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
package org.sirio6.services.bus;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sirio6.services.localization.INT;

/**
 * Implementazione del message bus con ascoltatore esplicito.
 * Gli ascoltatori espliciti si registrano con registerEventListnerExplicitPull().
 * Un ascoltatore esplicito non viene servito dal thread di servizio del bus, ma
 * tutti i messaggi a lui destinati finiscono in una coda in attesa che venga
 * chiamato il metodo sendEventExplicitPull() che li libera dalla coda e li recapita
 * all'ascoltatore.<br>
 * Il tipico scenario di uso è quando un ascoltatore vuole ottenere una notifica
 * sincrona de messaggi. Nello stesso thread che chiama sendEventExplicitPull() vengono
 * anche eseguite le chiamate a MessageBusListener.message() rendendo la notifica
 * indipendente dal thread del bus.<br>
 * Ogni ascoltatore ha la sua coda dedicata. La lunghezza massima della coda viene
 * fissata dal parametro di setup maxExplicitQueue (default 100).
 *
 * @author Nicola De Nisco
 */
public class CoreMessageBusExplicitPull extends CoreMessageBus
   implements MessageBus
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreMessageBusExplicitPull.class);
  /** Mappa degli ascoltatori. */
  protected final ConcurrentSkipListMap<String, ExplicitListner> busMapExplicit = new ConcurrentSkipListMap<>();
  /** Dimensione massima coda per ascoltatori espliciti. */
  protected int maxExplicitQueue = 100;

  public class ExplicitListner
  {
    public MessageBusListener listener;
    private final LinkedBlockingQueue<QueueInfo> queue = new LinkedBlockingQueue<>();

    public synchronized void add(QueueInfo qi)
    {
      queue.add(qi);

      // limita la lunghezza della coda distruggendo gli eventi più vecchi
      while(queue.size() > maxExplicitQueue)
      {
        queue.poll();
      }
    }

    public synchronized void sendAll()
       throws Exception
    {
      QueueInfo qi;

      while((qi = queue.poll(100, TimeUnit.MILLISECONDS)) != null)
      {
        // in caso di evento ritardato attende per i millisecondi indicati
        // il tempo di attesa viene misurato dal momento di inserimento nella coda
        if(qi.delay > 0)
        {
          long elapsed, twait;
          while((elapsed = (System.currentTimeMillis() - qi.originTime)) < qi.delay)
          {
            if((twait = (qi.delay - elapsed) >> 1) < 10)
              twait = 10;

            Thread.sleep(twait);
          }
        }

        sendMessageSync(qi.msgID, qi.originator, qi.context);
      }
    }

    public int sendMessageSync(int msgID, Object originator, BusContext context)
    {
      final String nomeListener = listener.getClass().getName();

      try
      {
        return listener.message(msgID, originator, context);
      }
      catch(Throwable ex)
      {
        log.error(INT.I("Errore BUS messaggio %d in %s ", msgID, nomeListener), ex);
      }

      // eventuale azione di post action
      if(context != null && !context.postListener.isEmpty())
        context.postListener.forEach((l) -> l.actionPerformed(msgID, originator, context));

      return 0;
    }
  }

  @Override
  public void init()
  {
    super.init();
    Configuration cfg = getConfiguration();
    maxExplicitQueue = cfg.getInt("maxExplicitQueue", maxExplicitQueue);

    // servizio inizializzato correttamentea
    setInit(true);
  }

  @Override
  public void sendMessageAsync(QueueInfo qi)
  {
    try
    {
      if(!busMapExplicit.isEmpty())
      {
        for(Map.Entry<String, ExplicitListner> entry : busMapExplicit.entrySet())
        {
          //String key = entry.getKey();
          ExplicitListner val = entry.getValue();
          val.add(qi);
        }
      }

      if(busMap.isEmpty())
        return;

      queue.put(qi);
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void registerEventListnerExplicitPull(String name, MessageBusListener listener)
  {
    ExplicitListner el = new ExplicitListner();
    el.listener = listener;
    busMapExplicit.put(name, el);
  }

  public void removeEventListnerExplicitPull(String name)
  {
    busMapExplicit.remove(name);
  }

  public void sendEventExplicitPull(String name)
     throws Exception
  {
    ExplicitListner el = busMapExplicit.get(name);
    if(el != null)
      el.sendAll();
  }
}

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
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sirio6.services.localization.INT;

/**
 * Implementazione del message bus senza ritardi di consegna.
 * A differenza della classe base quando un messaggio ha un ritardo
 * viene spostato alla fine della coda per permettere agli altri messaggi
 * di passare.<br>
 * <b>ATTENZIONE: i messaggi vanno fuori sequenza.</b>
 *
 * @author Nicola De Nisco
 */
public class CoreMessageBusUndelaied extends CoreMessageBus
   implements MessageBus
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreMessageBusUndelaied.class);

  @Override
  public void init()
  {
    super.init();
    Configuration cfg = getConfiguration();
    delayDefaultAsyncMillis = cfg.getLong("delayDefaultAsyncMillis", 150);
  }

  /**
   * Funzione di servizio del thread di spedizione differita.
   * Estrae dalla coda eventi l'evento da inviare sul bus e
   * lo invia ai destinatari.
   */
  @Override
  protected void runAsync()
  {
    while(true)
    {
      try
      {
        QueueInfo qi;
        List<QueueInfo> delayed = new ArrayList<>();

        while((qi = queue.poll(3, TimeUnit.SECONDS)) != null)
        {
          // in caso di evento ritardato lo scarta per reinserirlo successivamente nella coda
          // ATTENZIONE: questo può portare a un cambio di sequenza nel dispach degli eventi
          if(qi.delay > 0)
          {
            long elapsed;
            if((elapsed = (System.currentTimeMillis() - qi.originTime)) < qi.delay)
            {
              delayed.add(qi);

              qi = null;
              Thread.sleep(10);
            }
          }

          if(qi != null)
            sendMessageSync(qi.msgID, qi.originator, qi.context);
        }

        idle();

        if(!delayed.isEmpty())
        {
          // frena un po il thread del bus per non renderlo troppo aggressivo
          Thread.sleep(100);
          queue.addAll(delayed);
        }
      }
      catch(Throwable t)
      {
        log.error(INT.I("Errore di BUS:"), t);
      }
    }
  }
}

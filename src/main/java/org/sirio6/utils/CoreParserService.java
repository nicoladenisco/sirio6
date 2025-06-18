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

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.fulcrum.parser.DefaultParserService;
import org.apache.fulcrum.parser.ParserServiceSupport;
import org.apache.fulcrum.parser.ValueParser;
import org.apache.fulcrum.pool.PoolException;
import org.apache.fulcrum.pool.PoolService;

/**
 * Servizio per generazione parser in rundata.
 * Questa implementazione corregge due bug riscontrati nella
 * versione originale di Turbine 6. <br>
 * Va inserito in roleConfiguration.xml al posto di DefaultParserService.
 * Usa fulcrum PoolService abilitato di default in Turbine 6.
 * @author Nicola De Nisco
 */
public class CoreParserService extends DefaultParserService
{
  private PoolService localFulcrumPoolService;

  @Override
  public <P extends ValueParser> P getParser(Class<P> ppClass)
     throws InstantiationException
  {
    if(localFulcrumPoolService == null)
      return super.getParser(ppClass);

    P vp = null;

    try
    {
      try
      {
        vp = (P) localFulcrumPoolService.getInstance(ppClass);
      }
      catch(PoolException pe)
      {
        throw new InstantiationException("Parser class '" + ppClass + "' is illegal. " + pe.getMessage());
      }

      if(vp != null && vp instanceof ParserServiceSupport)
      {
        ((ParserServiceSupport) vp).setParserService(this);
      }
      else
      {
        throw new InstantiationException("Could not set parser");
      }

      if(vp != null && vp instanceof LogEnabled)
      {
        ((LogEnabled) vp).enableLogging(getLogger().getChildLogger(ppClass.getSimpleName()));
      }
    }
    catch(ClassCastException x)
    {
      throw new InstantiationException("Parser class '" + ppClass + "' is illegal. " + x.getMessage());
    }

    return vp;
  }

  @Override
  public void putParser(ValueParser parser)
  {
    if(localFulcrumPoolService == null)
    {
      super.putParser(parser);
      return;
    }

    parser.clear();
    parser.dispose();
    localFulcrumPoolService.putInstance(parser);
  }

  @Override
  public void service(ServiceManager manager)
     throws ServiceException
  {
    super.service(manager);

    // only for fulcrum  pool, need to call internal service, if role pool service is set
    if(manager.hasService(PoolService.ROLE))
    {
      localFulcrumPoolService = (PoolService) manager.lookup(PoolService.ROLE);
    }
  }
}

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
package org.sirio6.pipeline;

import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.pipeline.Valve;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.sirio6.beans.SessionParamsBean;

/**
 * Valve da inserire in turbine-classic-pipeline.xml per pulire i parametri
 * nascosti presenti in SessionParamasBean.
 *
 * @author Nicola De Nisco
 */
public class ClearSessionParametersValve implements Valve
{
  /** Logging */
  //private static final Log log = LogFactory.getLog(ClearSessionParametersValve.class);

  @Override
  public void invoke(PipelineData pipelineData, ValveContext context)
     throws IOException, TurbineException
  {
    try
    {
      RunData data = pipelineData.getRunData();
      HttpSession session = data.getSession();
      SessionParamsBean.removeFromSession(session);
    }
    catch(Exception e)
    {
      throw new TurbineException(e);
    }

    // Pass control to the next Valve in the Pipeline
    context.invokeNext(pipelineData);
  }
}

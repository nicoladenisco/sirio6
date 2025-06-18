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
package org.sirio6.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.parser.ParameterParser;
import org.commonlib5.utils.Pair;
import org.sirio6.CoreConst;
import org.sirio6.services.cache.FileCacheItem;
import org.sirio6.services.localization.INT;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.CoreRunDataHelper;
import org.sirio6.utils.FU;
import org.sirio6.utils.SU;

/**
 * Servlet per servire i files nella cache.
 *
 * @author Nicola De Nisco
 */
public class FileCacheServlet extends HttpServlet
{
  /** Logging */
  private static final Log pgmlog = LogFactory.getLog(FileCacheServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
  {
    ArrayList<Pair<String, String>> arTickets = new ArrayList<>();

    try(CoreRunDataHelper rundata = new CoreRunDataHelper(req, resp, this))
    {
      CoreRunData data = rundata.getCoreRunData();
      ParameterParser params = data.getParameters();

      if(!data.isAuthorizedAll("uploadfiles"))
        throw new Exception(INT.I("Operazione non consentita."));

      Collection<Part> parts = params.getParts();
      for(Part part : parts)
      {
        String nome = SU.okStr(part.getName(), UUID.randomUUID().toString());
        String tipoMime = SU.okStr(part.getContentType(), CoreConst.MIME_BINARY);

        File tmpFile = File.createTempFile("upload", ".tmp");
        try(InputStream input = part.getInputStream())
        {
          Files.copy(input, tmpFile.toPath());
        }

        String fileName = SU.okStr(part.getSubmittedFileName(), nome);
        String ticket = FileCacheItem.addFileToCache(tmpFile, tipoMime, fileName, false);
        arTickets.add(new Pair<>(fileName, ticket));
      }

      resp.setContentType(CoreConst.MIME_TXT);
      PrintWriter out = resp.getWriter();
      arTickets.forEach((pa) -> out.printf("%s=%s\n", pa.first, pa.second));
      out.flush();
    }
    catch(ServletException | IOException ex)
    {
      pgmlog.error("UPLOAD FAILURE", ex);
      throw ex;
    }
    catch(Exception ex)
    {
      pgmlog.error("UPLOAD FAILURE", ex);
      throw new ServletException(ex);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    try
    {
      // estrae ticket della richiesta
      String sTicket = request.getPathInfo().substring(1);
      FileCacheItem fi = FileCacheItem.getFromCache(sTicket);
      if(fi == null)
        throw new ServletException(INT.I(
           "Richiesta per il ticket %s non possibile: inesitente o scaduto", sTicket));

      pgmlog.info("FileCacheServlet: OK " + fi.getFile().getAbsolutePath());

      // invio del file come risposta
      if(SU.checkTrueFalse(request.getParameter("inline"), false))
        FU.sendFileResponse(request, response, fi.getFile(), fi.getTipoMime(), fi.getFileName(), false);
      else
        FU.sendFile(request, response, fi.getTipoMime(), fi.getFile(), fi.getFileName(), false);
    }
    catch(ServletException ex)
    {
      pgmlog.error("DOWNLOAD FAILURE", ex);
      throw ex;
    }
    catch(Exception ex)
    {
      pgmlog.error("DOWNLOAD FAILURE", ex);
      throw new ServletException(ex);
    }
  }
}

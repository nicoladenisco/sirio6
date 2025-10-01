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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;
import org.sirio6.utils.TR;

/**
 * Accesso XML-RPC diretto da Tomcat.
 * Consente di utilizzare direttamente Tomcat
 * come server XML-RPC dell'applicazione, oltre al tradizionale
 * server dedicato.
 *
 * @author Nicola De Nisco
 */
public class EmbeddedXmlRpc extends HttpServlet
{
  /** Logging */
  private static final Log log = LogFactory.getLog(EmbeddedXmlRpc.class);
  /** Gestore connessioni xmlrpc */
  protected final XmlRpcServletServer server = new XmlRpcServletServer();
  protected boolean enabledForExtensions = true;
  protected boolean contentLengthOptional = false;

  @Override
  public void init()
     throws ServletException
  {
    super.init();
    Configuration conf = getPropertiesConfiguration();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();

    // setup generale dalla configurazione turbine
    enabledForExtensions = conf.getBoolean("enabledForExtensions", enabledForExtensions);
    contentLengthOptional = conf.getBoolean("contentLengthOptional", contentLengthOptional);

    // Check if there are any handlers to register at startup
    for(Iterator keys = conf.getKeys("handler"); keys.hasNext();)
    {
      String handler = (String) keys.next();
      String handlerName = handler.substring(handler.indexOf('.') + 1);
      String handlerClass = conf.getString(handler);

      log.debug("Found Handler " + handler + " as " + handlerName + " / " + handlerClass);

      try
      {
        registerHandler(handlerName, handlerClass, phm);
      }
      catch(ServletException ex)
      {
        log.error("Failed init fandler " + handler + " as " + handlerName + " / " + handlerClass, ex);
        throw new ServletException("Failed init handler " + handler, ex);
      }
    }

    server.setHandlerMapping(phm);

    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
    serverConfig.setEnabledForExtensions(enabledForExtensions);
    serverConfig.setContentLengthOptional(contentLengthOptional);
  }

  protected Configuration getPropertiesConfiguration()
  {
    return TR.getConfiguration("services.XmlRpcService.embedded");
  }

  /**
   * A helper method that tries to initialize a handler and register it.
   * The purpose is to check for all the exceptions that may occur in
   * dynamic class loading and throw an InitializationException on
   * error.
   *
   * @param handlerName The name the handler is registered under.
   * @param handlerClass The name of the class to use as a handler.
   * @param phm
   * @exception ServletException Couldn't instantiate handler.
   */
  public void registerHandler(String handlerName, String handlerClass, PropertyHandlerMapping phm)
     throws ServletException
  {
    Class handlerClazz = null;

    try
    {
      handlerClazz = Class.forName(handlerClass);
      phm.addHandler(handlerName, handlerClazz);
    }
    catch(IllegalStateException ex)
    {
      // i metodi public sono automaticamente esportati; questo implica che se i parametri non sono XML-RPC compatibili solleva errore
      // qui arricchiamo il messaggio d'errore evidenziando tutti i metodi esportati (public)

      // Caused by: java.lang.IllegalStateException: Invalid parameter or result type: org.sirio6.services.token.TokenAuthItem
      // Ottieni tutti i metodi pubblici della classe (inclusi quelli ereditati)
      Method[] methods = handlerClazz.getMethods();
      StringBuilder sb = new StringBuilder();
      sb.append(ex.getMessage()).append("\n");
      sb.append("Metodi pubblici di ").append(handlerClazz.getName()).append(":\n");

      // Itera sui metodi e stampa i nomi
      for(Method method : methods)
      {
        Class<?> ritorno = method.getReturnType();
        Class<?>[] parametri = method.getParameterTypes();

        if(ritorno == null)
          sb.append("void ");
        else
          sb.append(ritorno.getName()).append(" ");
        sb.append(method.getName()).append("(");
        if(parametri != null)
          for(int i = 0; i < parametri.length; i++)
          {
            Class<?> cp = parametri[i];
            if(i > 0)
              sb.append(", ");
            sb.append(cp.getName());
          }
        sb.append(")\n");
      }

      log.error(sb, ex);
    }
    catch(ThreadDeath | OutOfMemoryError t)
    {
      throw t;
    }
    catch(Throwable t)
    {
      throw new ServletException("Failed to instantiate " + handlerClass, t);
    }
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    try
    {
      server.execute(request, response);
    }
    catch(Exception ex)
    {
      log.error("FATAL XML-RPC ERROR", ex);
      throw ex;
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "XML-RPC interface";
  }// </editor-fold>
}

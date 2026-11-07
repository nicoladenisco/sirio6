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
package org.sirio6.utils.xmlrpc;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransport;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactoryImpl;

/**
 * Implementazione di base dei client XML-RPC.
 * Implementa la possibilità di specificare header e autenticazione.
 *
 * @author Nicola De Nisco
 */
public class BaseXmlRpcClientWithHeader extends BaseXmlRpcClient
{
  private final ConcurrentNavigableMap<String, String> headers = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

  public BaseXmlRpcClientWithHeader(String stubName, URL url)
     throws Exception
  {
    super(stubName, url);
    setHeaderTransport();
  }

  public BaseXmlRpcClientWithHeader(String stubName, String server, int port)
     throws Exception
  {
    super(stubName, server, port);
    setHeaderTransport();
  }

  private void setHeaderTransport()
  {
    client.setTransportFactory(new HeaderTransportFactory(client, headers));
  }

  /**
   * Adds or replaces an HTTP request header.
   *
   * @param name header name
   * @param value header value
   */
  public void addHeader(String name, String value)
  {
    if(name == null || name.trim().isEmpty())
      throw new IllegalArgumentException("Il nome dell'header HTTP non può essere vuoto.");
    headers.put(name, Objects.requireNonNull(value, "Il valore dell'header HTTP non può essere null."));
  }

  public void setBearerToken(String token)
  {
    addHeader("Authorization", "Bearer " + Objects.requireNonNull(token, "Il token Bearer non può essere null."));
  }

  public void setBasicAuth(String username, String password)
  {
    String credentials = Objects.requireNonNull(username, "L'username non può essere null.")
       + ":" + Objects.requireNonNull(password, "La password non può essere null.");
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    addHeader("Authorization", "Basic " + encoded);
  }

  public void removeHeader(String name)
  {
    if(name != null)
      headers.remove(name);
  }

  public void clearHeaders()
  {
    headers.clear();
  }

  public boolean hasHeader(String name)
  {
    return name != null && headers.containsKey(name);
  }

  public String getHeader(String name)
  {
    return name == null ? null : headers.get(name);
  }

  private static final class HeaderTransportFactory extends XmlRpcTransportFactoryImpl
  {
    private final Map<String, String> headers;

    private HeaderTransportFactory(XmlRpcClient client, Map<String, String> headers)
    {
      super(client);
      this.headers = headers;
    }

    @Override
    public XmlRpcTransport getTransport()
    {
      return new HeaderTransport(getClient(), headers);
    }
  }

  private static final class HeaderTransport extends XmlRpcSunHttpTransport
  {
    private final Map<String, String> headers;

    private HeaderTransport(XmlRpcClient client, Map<String, String> headers)
    {
      super(client);
      this.headers = headers;
    }

    @Override
    protected void initHttpHeaders(XmlRpcRequest request)
       throws XmlRpcClientException
    {
      super.initHttpHeaders(request);
      headers.forEach(this::setRequestHeader);
    }
  }
}

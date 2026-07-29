/*
 * RigelDispTool.java
 *
 * Created on 2-nov-2011, 17.51.28
 *
 * Copyright (C) 2011 WinSOFT di Nicola De Nisco
 *
 * Questo software è proprietà di Nicola De Nisco.
 * I termini di ridistribuzione possono variare in base
 * al tipo di contratto in essere fra Nicola De Nisco e
 * il fruitore dello stesso.
 *
 * Fare riferimento alla documentazione associata al contratto
 * di committenza per ulteriori dettagli.
 */
package org.sirio6.modules.tools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpSession;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ClassUtils;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.glue.table.PeerAppMaintDispTable;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.peer.html.PeerWrapperFormHtml;
import org.sirio6.services.modellixml.MDL;
import org.sirio6.utils.SU;
import org.sirio6.utils.velocity.VelocityParser;

/**
 * Tool per l'accesso ai disp xml di Rigel.
 * I disp sono come dei form con al posto dei campi di
 * edit i valori corrispettivi dei campi.
 * Servono a visualizzare i dati di un record come se
 * fosse un form non editabile.
 *
 * @author Nicola De Nisco
 */
public class RigelDispTool implements ApplicationTool
{
  private static final AtomicInteger counter = new AtomicInteger();

  @Override
  public void init(Object data)
  {
  }

  @Override
  public void refresh()
  {
  }

  protected PeerWrapperFormHtml getDisp(RunData data, String type)
     throws Exception
  {
    WrapperCacheBase rwc = MDL.getWrapperCache(data);
    PeerWrapperFormHtml pwl = rwc.getDispCache(type);
    return pwl;
  }

  @Deprecated
  public String getHtml(RunData data, String lista, String params)
     throws Exception
  {
    return html(data, lista, params);
  }

  public String html(RunData data, String lista)
     throws Exception
  {
    return html(data, lista, null);
  }

  public synchronized String html(RunData data, String lista, String params)
     throws Exception
  {
    PeerWrapperFormHtml pwl = getDisp(data, lista);
    String formName = "fo" + lista + counter.getAndIncrement();

    // imposta il nome form nel table model
    RigelTableModel rtm = pwl.getPtm();
    rtm.setFormName(formName);

    // aggiunge i parametri specificati in params
    if(params != null)
    {
      ParameterParser pp = data.getParameters();
      Map<String, String> pmap = SU.string2Map(params, ",", true);
      pmap.forEach((k, v) -> pp.setString(k, v));
    }

    Map param = SU.getParMap(data);
    HttpSession sessione = data.getSession();
    Context ctx = VelocityParser.createNewContext();
    String html = getHtmlDisp(data, ctx, pwl, param, sessione);

    ctx.put("type", lista);
    ctx.put("phtml", html);
    ctx.put("pwl", pwl);
    ctx.put("formName", formName);

    StringWriter writer = new StringWriter(512);
    // renderizzazione Velocity con il modello caricato da risorsa
    try(InputStream is = ClassUtils.getResourceAsStream(getClass(), "/ToolDisp.vm"))
    {
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");

      VelocityParser vp = new VelocityParser(ctx);
      vp.parseReader(reader, writer, "ToolDisp.vm");
    }

    return writer.toString();
  }

  protected String getHtmlDisp(RunData data, Context context,
     PeerWrapperFormHtml pwl, Map params, HttpSession session)
     throws Exception
  {
    PeerAppMaintDispTable table = (PeerAppMaintDispTable) (pwl.getTbl());
    synchronized(table)
    {
      table.setPopup(false);
      table.setEditPopup(true);
      String html = table.getHtml(params, session);
      context.put("objInEdit", table.getObjInEdit());
      return html;
    }
  }
}

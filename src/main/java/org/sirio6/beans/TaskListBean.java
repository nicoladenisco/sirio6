/*
 *  TaskListBean.java
 *  Creato il 2 mar 2026, 12:30:01
 *
 *  Copyright (C) 2026 Informatica Medica s.r.l.
 *
 *  Questo software è proprietà di Informatica Medica s.r.l.
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Informatica Medica s.r.l.
 *  Viale dei Tigli, 19
 *  Casalnuovo di Napoli (NA)
 */
package org.sirio6.beans;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.turbine.om.security.User;
import org.apache.velocity.context.Context;
import org.json.JSONObject;
import org.sirio6.services.cache.FileCacheItem;
import org.sirio6.services.security.SEC;
import org.sirio6.services.taskman.TaskManager;
import org.sirio6.services.taskman.tasks.Task;
import org.sirio6.utils.CoreRunData;
import org.sirio6.utils.DT;
import org.sirio6.utils.SU;
import org.sirio6.utils.velocity.VelocityParser;

/**
 * Bean di persistenza per TaskList.vm.
 *
 * @author Nicola De Nisco
 */
public class TaskListBean extends CoreBaseBean
{
  TaskManager ts;

  @Override
  public void init(CoreRunData data)
     throws Exception
  {
    super.init(data);
    ts = getService(TaskManager.SERVICE_NAME);
  }

  public void doCmd_ajax_taskList(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    // prepara oggetti per il ritorno
    Context context = (Context) args[0];
    JSONObject dati = new JSONObject();
    context.put(ActionJspBean.FILTERED_CONTEXT_DATA_KEY, dati);

    StringWriter writer = new StringWriter(512);
    List<Task> lsTasks = ts.getAllTasks();

    for(Task task : lsTasks)
    {
      context.put("t", task);
      context.put("tinizio", DT.formatDataFull(task.gettStart()));

      String stile1 = "panel-default";
      String stile2 = "progress-bar-default";
      switch(task.getTaskState())
      {
        case Task.ST_RUNNING:
          stile1 = "primary";
          stile2 = "progress-bar-primary";
          break;
        case Task.ST_COMPLETED:
          stile1 = "success";
          stile2 = "progress-bar-success";
          context.put("tfine", DT.formatDataFull(task.gettEnd()));
          break;
        case Task.ST_ERROR:
          stile1 = "danger";
          stile2 = "progress-bar-danger";
          context.put("tfine", DT.formatDataFull(task.gettEnd()));
          break;
        case Task.ST_ABORT:
          stile1 = "warning";
          stile2 = "progress-bar-warning";
          context.put("tfine", DT.formatDataFull(task.gettEnd()));
          break;
      }

      context.put("stile1", stile1);
      context.put("stile2", stile2);

      context.put("tempo1", task.getElapsed() / 1000);
      context.put("tempo2", task.getTotalTime() / 1000);

      User user = SEC.getUser(task.getIdUser());
      context.put("nomeUtente", user.getFirstName() + " " + user.getLastName());

      VelocityParser vp = new VelocityParser(context);
      vp.parseResource(writer, "/TaskListItemB5.vm");
    }

    dati.put("html", writer.toString());
  }

  public void doCmd_ajax_taskDownload(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    // prepara oggetti per il ritorno
    Context context = (Context) args[0];
    JSONObject dati = new JSONObject();
    context.put(ActionJspBean.FILTERED_CONTEXT_DATA_KEY, dati);

    int idTask = SU.parseInt(params.get("id"));
    if(idTask == 0)
      throw new Exception("Parametro 'id' mancante nella richiesta.");

    Task task = ts.findTask(idTask);

    // sessun task, ma potrebbe semplicemente essere stato rimosso
    if(task == null)
    {
      dati.put("html", "Il task è scaduto: nessun dato da visualizzare.");
      return;
    }

    if(task.isAlive())
    {
      dati.put("html", "L'elaborazione è ancora in esecuzione: attendere il completamento.");
      return;
    }

    if(!task.haveFiles())
    {
      dati.put("html", "L'elaborazione non ha prodotto files.");
      return;
    }

    List<Map> arInfoTickets = new ArrayList<>();
    List<String> lsTickets = task.getFileCacheTickets();
    context.put("infos", arInfoTickets);

    for(String tik : lsTickets)
    {
      Map it = new HashMap();
      arInfoTickets.add(it);

      it.put("tik", tik);
      FileCacheItem fi = FileCacheItem.getFromCache(tik);

      if(fi != null)
      {
        long nsec = (fi.getCreated() + fi.getExpires() - System.currentTimeMillis()) / 1000;

        it.put("fn", fi.getFileName());
        it.put("fm", fi.getTipoMime());
        it.put("nsec", nsec);
        it.put("link1", data.getContextPath() + "/cache/" + tik);
      }
    }

    StringWriter writer = new StringWriter(512);
    context.put("t", task);
    context.put("bean", this);
    VelocityParser vp = new VelocityParser(context);
    vp.parseResource(writer, "/TaskDownloadB5.vm");
    dati.put("html", writer.toString());
  }

  public void doCmd_ajax_taskError(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    // prepara oggetti per il ritorno
    Context context = (Context) args[0];
    JSONObject dati = new JSONObject();
    context.put(ActionJspBean.FILTERED_CONTEXT_DATA_KEY, dati);

    int idTask = SU.parseInt(params.get("id"));
    if(idTask == 0)
      throw new Exception("Parametro 'id' mancante nella richiesta.");

    Task task = ts.findTask(idTask);

    // sessun task, ma potrebbe semplicemente essere stato rimosso
    if(task == null)
    {
      dati.put("html", "Il task è scaduto: nessun dato da visualizzare.");
      return;
    }

    if(task.isAlive())
    {
      dati.put("html", "Il task è ancora in esecuzione: attendere il completamento.");
      return;
    }

    if(task.getTaskState() != Task.ST_ERROR || task.getRunError() == null)
    {
      dati.put("html", "Il task non ha segnalato errori durante il funzionamento.");
      return;
    }

    StringWriter writer = new StringWriter(512);
    writer.write("<div>" + task.getRunError().getMessage() + "</div>");
    writer.write("<code>");
    task.getRunError().printStackTrace(new PrintWriter(writer));
    writer.write("</code>");
    dati.put("html", writer.toString());
  }
}

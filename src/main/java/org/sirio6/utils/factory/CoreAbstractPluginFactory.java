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
package org.sirio6.utils.factory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonlib5.utils.ClassOper;
import org.commonlib5.utils.Pair;
import org.sirio6.services.CoreServiceException;
import org.sirio6.services.localization.INT;
import org.sirio6.utils.SU;

/**
 * Funzioni di base di una factory di plugin.
 *
 * @author Nicola De Nisco
 * @param <T> tipo del plugin; deve implementare l'intefaccia CoreBasePlugin
 */
public abstract class CoreAbstractPluginFactory<T extends CoreBasePlugin>
{
  private static final Log log = LogFactory.getLog(CoreAbstractPluginFactory.class);

  protected String basePath = null;
  protected String[] vPaths = null;
  protected Configuration cfg = null;
  protected String pluginCfgRadix = null;
  protected HashMap<String, Pair<String, Configuration>> cache = new HashMap<>();
  protected ArrayList<String> arPlugins = new ArrayList<>();
  public static final Pattern patClassname = Pattern.compile("^(.+).classname");

  /**
   * Configura la factory.
   *
   * @param cfg estratto di configurazione
   * @param pluginCfgRadix stringa radice dei plugin di interesse
   */
  public void configure(Configuration cfg, String pluginCfgRadix)
  {
    this.cfg = cfg;
    this.pluginCfgRadix = pluginCfgRadix;

    // carica path di ricerca della class del plugin
    basePath = ClassOper.getClassPackage(this.getClass());
    vPaths = cfg.getStringArray("classpath");
    String[] localPath = cfg.getStringArray(pluginCfgRadix + ".classpath");
    if(localPath.length > 0)
      vPaths = (String[]) ArrayUtils.addAll(vPaths, localPath);

    // costruisce la lista dei plugin
    Configuration subPlg = cfg.subset(pluginCfgRadix);
    Iterator<String> itrKeys = subPlg.getKeys();
    while(itrKeys.hasNext())
    {
      String key = itrKeys.next();
      Matcher m = patClassname.matcher(key);
      if(m.find())
      {
        String pluginName = m.group(1);
        String className = cfg.getString(pluginCfgRadix + "." + pluginName + ".classname");
        if(!SU.isOkStr(className))
          continue;

        arPlugins.add(pluginName);
        cache.put(pluginName, new Pair<>(className, cfg.subset(pluginCfgRadix + "." + pluginName)));
      }
    }

    Collections.sort(arPlugins);
    log.debug(INT.I("trovati %d plugin per %s", arPlugins.size(), pluginCfgRadix));
  }

  /**
   * Cerca ricorsivamente nel package indicato le classi annotate con
   * {@link CorePlugin} compatibili con il tipo della factory.
   * Le classi trovate sono registrate con il nome definito nell'annotation
   * oppure, se non specificato, con il nome semplice della classe.
   * I plugin già dichiarati in configurazione mantengono la precedenza.
   * La scansione avviene in basePath e vPaths.
   *
   * @param pluginType tipo concreto supportato dalla factory
   * @throws IOException in caso di errore nella lettura delle risorse classpath
   */
  public void autoScan(Class<T> pluginType)
     throws IOException
  {
    autoScan(basePath, pluginType);

    for(String path : vPaths)
    {
      if(path != null)
        autoScan(path, pluginType);
    }
  }

  /**
   * Cerca ricorsivamente nel package indicato le classi annotate con
   * {@link CorePlugin} compatibili con il tipo della factory.
   * Le classi trovate sono registrate con il nome definito nell'annotation
   * oppure, se non specificato, con il nome semplice della classe.
   * I plugin già dichiarati in configurazione mantengono la precedenza.
   *
   * @param packageName package radice da cercare
   * @param pluginType tipo concreto supportato dalla factory
   * @throws IOException in caso di errore nella lettura delle risorse classpath
   */
  public void autoScan(String packageName, Class<T> pluginType)
     throws IOException
  {
    if(cfg == null || pluginCfgRadix == null)
      throw new IllegalStateException("La factory deve essere configurata prima dell'autoscan.");

    String normalizedPackage = packageName.replace('/', '.').trim();
    if(normalizedPackage.isEmpty())
      throw new IllegalArgumentException("Il package da scandire non può essere vuoto.");

    String packagePath = normalizedPackage.replace('.', '/');
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if(classLoader == null)
      classLoader = pluginType.getClassLoader();

    Set<String> classNames = new LinkedHashSet<>();
    Enumeration<URL> resources = classLoader.getResources(packagePath);
    while(resources.hasMoreElements())
    {
      URL resource = resources.nextElement();
      if("file".equals(resource.getProtocol()))
      {
        try
        {
          scanDirectory(normalizedPackage, Paths.get(resource.toURI()), classNames);
        }
        catch(URISyntaxException ex)
        {
          throw new IOException("Percorso classpath non valido: " + resource, ex);
        }
      }
      else if("jar".equals(resource.getProtocol()))
      {
        scanJar(packagePath, resource, classNames);
      }
    }

    for(String className : classNames)
      registerScannedPlugin(className, pluginType, classLoader);

    Collections.sort(arPlugins);
  }

  private void scanDirectory(String packageName, Path root, Set<String> classNames)
     throws IOException
  {
    try(Stream<Path> paths = Files.walk(root))
    {
      paths.filter(Files::isRegularFile)
         .map(root::relativize)
         .map(path -> path.toString())
         .filter(path -> path.endsWith(".class"))
         .map(path -> packageName + "." + path.substring(0, path.length() - 6)
            .replace(File.separatorChar, '.'))
         .filter(this::isScannableClassName)
         .forEach(classNames::add);
    }
  }

  private void scanJar(String packagePath, URL resource, Set<String> classNames)
     throws IOException
  {
    URLConnection connection = resource.openConnection();
    if(!(connection instanceof JarURLConnection))
      return;

    JarURLConnection jarConnection = (JarURLConnection) connection;
    jarConnection.setUseCaches(false);
    String prefix = packagePath + "/";
    try(JarFile jar = jarConnection.getJarFile())
    {
      Enumeration<JarEntry> entries = jar.entries();
      while(entries.hasMoreElements())
      {
        String entryName = entries.nextElement().getName();
        if(entryName.startsWith(prefix) && entryName.endsWith(".class"))
        {
          String className = entryName.substring(0, entryName.length() - 6)
             .replace('/', '.');
          if(isScannableClassName(className))
            classNames.add(className);
        }
      }
    }
  }

  private boolean isScannableClassName(String className)
  {
    return !className.endsWith(".package-info") && !className.endsWith(".module-info");
  }

  private void registerScannedPlugin(String className, Class<T> pluginType, ClassLoader classLoader)
  {
    try
    {
      Class<?> candidate = Class.forName(className, false, classLoader);
      if(!pluginType.isAssignableFrom(candidate)
         || !candidate.isAnnotationPresent(CorePlugin.class)
         || candidate.isInterface()
         || Modifier.isAbstract(candidate.getModifiers()))
        return;

      String pluginName = candidate.getAnnotation(CorePlugin.class).name();
      if(!SU.isOkStr(pluginName))
        pluginName = candidate.getSimpleName();
      if(cache.containsKey(pluginName))
        return;

      cache.put(pluginName, new Pair<>(className,
         createPluginConfiguration(pluginName, candidate.getAnnotation(CorePlugin.class))));
      arPlugins.add(pluginName);
    }
    catch(ClassNotFoundException | LinkageError ex)
    {
      log.debug(INT.I("Classe plugin non caricabile durante autoscan: %s", className), ex);
    }
  }

  private Configuration createPluginConfiguration(String pluginName, CorePlugin annotation)
  {
    Configuration fileConfiguration = cfg.subset(pluginCfgRadix + "." + pluginName);
    String[] properties = annotation.properties();
    if(properties.length == 0)
      return fileConfiguration;

    PropertiesConfiguration annotationConfiguration = new PropertiesConfiguration();
    try
    {
      annotationConfiguration.read(new StringReader(String.join("\n", properties)));
    }
    catch(ConfigurationException | IOException ex)
    {
      throw new IllegalArgumentException("Proprietà non valide in @CorePlugin per " + pluginName, ex);
    }

    CombinedConfiguration combined = new CombinedConfiguration(new OverrideCombiner());
    combined.addConfiguration(fileConfiguration);
    combined.addConfiguration(annotationConfiguration);
    return combined;
  }

  /**
   * Ritorna lista plugin.
   * Al momento del configure è stata estratta la lista dei plugin disponibili.
   *
   * @return lista dei plugins
   */
  public List<String> getPluginNames()
  {
    return Collections.unmodifiableList(arPlugins);
  }

  /**
   * Carica plugin.
   *
   * @param pluginName nome del plugin
   * @return istanza del plugin
   * @throws Exception
   */
  public T getPlugin(String pluginName)
     throws Exception
  {
    Pair<String, Configuration> cname = cache.get(pluginName);
    if(cname == null)
      throw new CoreServiceException(
         INT.I("Plugin %s non dichiarato a setup (%s.%s.classname inesistente).", pluginName, pluginCfgRadix,
            pluginName));

    Class cl = loadClass(cname.first);
    T handler = createPlugin(cl);
    preparePlugin(pluginName, handler, cname.second);
    return handler;
  }

  /**
   * Carica plugin.
   * A differenza della gemella questa funzione non solleva eccezione.
   *
   * @param pluginName nome del plugin
   * @return istanza del plugin o null per qualsiasi motivo non riesca a
   * istanziarlo
   */
  public T getPluginQuiet(String pluginName)
  {
    try
    {
      return getPlugin(pluginName);
    }
    catch(Exception e)
    {
      log.warn(INT.I("Errore non fatale recupero plugin %s: %s", pluginName, e.getMessage()));
      return null;
    }
  }

  /**
   * Prepara il plugin per l'esecuzione.
   *
   * @param pluginName nome con cui è stato istanziato
   * @param handler plugin da inizializzare
   * @param cfgLocal estratto della configurazione specifico per questo plugin
   * @throws Exception
   */
  protected void preparePlugin(String pluginName, T handler, Configuration cfgLocal)
     throws Exception
  {
    handler.configure(pluginName, cfgLocal);
  }

  /**
   * Carica la classe del plugin.
   *
   * @param className nome/classe del plugin
   * @return classe del plugin
   * @throws CoreServiceException
   */
  protected Class loadClass(String className)
     throws CoreServiceException
  {
    try
    {
      Class cl = ClassOper.loadClass(className, basePath, vPaths);
      if(cl != null)
        return cl;
    }
    catch(Throwable t)
    {
    }

    throw new CoreServiceException(INT.I("Classe %s non trovata.", className));
  }

  /**
   * Crea una istanza del plugin.
   *
   * @param cp classe del plugin
   * @return istanza del plugin
   * @throws CoreServiceException
   */
  protected T createPlugin(Class cp)
     throws CoreServiceException
  {
    try
    {
      return (T) cp.getConstructor().newInstance();
    }
    catch(Exception ex)
    {
      throw new CoreServiceException(INT.I("Classe %s non istanziabile.", cp.getName()));
    }
  }

  /**
   * Ritorna elenco delle path per la ricerca della classe del plugin.
   *
   * @return array delle path
   */
  public String[] getBasePaths()
  {
    return vPaths;
  }

  /**
   * Imposta elenco delle path per la ricerca della classe del plugin.
   *
   * @param vPaths array delle path
   */
  public void setBasePaths(String[] vPaths)
  {
    this.vPaths = vPaths;
  }

  /**
   * Aggiunge all'elenco delle path per la ricerca della classe del plugin.
   *
   * @param basePath path da aggiungere
   */
  public void addBasePath(String basePath)
  {
    vPaths = (String[]) ArrayUtils.add(vPaths, basePath);
  }
}

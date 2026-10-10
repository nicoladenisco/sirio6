package org.sirio6.utils.factory;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoreAbstractPluginFactoryTest
{
  @Test
  public void autoScanRegistersAnnotatedPluginsFromPackage() throws Exception
  {
    BaseConfiguration configuration = new BaseConfiguration();
    configuration.addProperty("plugins.named-plugin.driver", "file-driver");

    TestFactory factory = new TestFactory();
    factory.configure(configuration, "plugins");

    factory.autoScan("org.sirio6.utils.factory", CoreBasePlugin.class);

    assertTrue(factory.getPluginNames().contains("ScannedPlugin"));
    assertTrue(factory.getPluginNames().contains("named-plugin"));
    assertFalse(factory.getPluginNames().contains("UnmarkedPlugin"));
    assertTrue(factory.getPlugin("ScannedPlugin") instanceof ScannedPlugin);

    NamedPlugin namedPlugin = (NamedPlugin) factory.getPlugin("named-plugin");
    assertEquals("file-driver", namedPlugin.driver);
    assertArrayEquals(new String[]
    { "first", "second" }, namedPlugin.mapping);
  }

  public static class TestFactory extends CoreAbstractPluginFactory<CoreBasePlugin>
  {
  }

  @CorePlugin
  public static class ScannedPlugin implements CoreBasePlugin
  {
    @Override
    public void configure(String pluginName, org.apache.commons.configuration2.Configuration cfg)
    {
    }
  }

  @CorePlugin(name = "named-plugin", properties =
  { "driver=annotation-driver", "mapping=first", "mapping=second" })
  public static class NamedPlugin implements CoreBasePlugin
  {
    String driver;
    String[] mapping;

    @Override
    public void configure(String pluginName, Configuration cfg)
    {
      driver = cfg.getString("driver");
      mapping = cfg.getStringArray("mapping");
    }
  }

  public static class UnmarkedPlugin implements CoreBasePlugin
  {
    @Override
    public void configure(String pluginName, Configuration cfg)
    {
    }
  }
}
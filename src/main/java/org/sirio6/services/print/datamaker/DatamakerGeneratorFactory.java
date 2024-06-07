/*
 * DatamakerGeneratorFactory.java
 *
 * Created on 23-gen-2010, 13.42.44
 *
 * Copyright (C) WinSOFT di Nicola De Nisco
 */
package org.sirio6.services.print.datamaker;

import org.apache.commons.configuration2.Configuration;
import org.sirio6.utils.factory.CoreAbstractPoolPluginFactory;

/**
 * Costruttore dei generatori di pdf.
 * @author Nicola De Nisco
 */
public class DatamakerGeneratorFactory extends CoreAbstractPoolPluginFactory<Datamaker>
{
  private static final DatamakerGeneratorFactory theInstance = new DatamakerGeneratorFactory();

  private DatamakerGeneratorFactory()
  {
  }

  public static DatamakerGeneratorFactory getInstance()
  {
    return theInstance;
  }

  public void configure(Configuration cfg)
  {
    super.configure(cfg, "maker");
  }
}

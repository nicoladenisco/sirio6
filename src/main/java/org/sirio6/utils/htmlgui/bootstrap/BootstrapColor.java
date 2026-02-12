package org.sirio6.utils.htmlgui.bootstrap;

public enum BootstrapColor
{
  PRIMARY("primary"), SECONDARY("secondary"), SUCCESS("success"),
  DANGER("danger"), WARNING("warning"), INFO("info"),
  LIGHT("light"), DARK("dark"), LINK("link");

  private final String value;

  BootstrapColor(String value)
  {
    this.value = value;
  }

  public String value()
  {
    return value;
  }
}

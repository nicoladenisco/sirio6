package org.sirio6.utils.htmlgui.bootstrap;

public enum BootstrapComponent
{
  BUTTON("btn"), ALERT("alert"),
  TEXT("text"), BADGE("badge"), LIST_GROUP_ITEM("list-group-item"),
  BG("bg"), DROPDOWN("dropdown");

  private final String prefix;

  BootstrapComponent(String prefix)
  {
    this.prefix = prefix;
  }

  public String prefix()
  {
    return prefix;
  }
}

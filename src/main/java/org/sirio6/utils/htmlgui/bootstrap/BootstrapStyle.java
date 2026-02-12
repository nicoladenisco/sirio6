package org.sirio6.utils.htmlgui.bootstrap;

public final class BootstrapStyle
{
  private final BootstrapComponent component;
  private final BootstrapColor color;
  private final BootstrapVersion version;

  private BootstrapStyle(BootstrapComponent c, BootstrapColor col, BootstrapVersion v)
  {
    this.component = c;
    this.color = col;
    this.version = v;
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col)
  {
    return of(c, col, BootstrapVersion.V5);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapVersion v)
  {
    return new BootstrapStyle(c, col, v);
  }

  public String cssClass()
  {
    return component.prefix() + "-" + color.value();
  }

  public BootstrapVersion getVersion()
  {
    return version;
  }

  @Override
  public String toString()
  {
    return cssClass();
  }
}

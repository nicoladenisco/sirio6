package org.sirio6.utils.htmlgui.bootstrap;

public final class BootstrapStyle
{
  private final BootstrapComponent component;
  private final BootstrapColor color;
  private final BootstrapVersion version;
  private final BootstrapSize size;
  private boolean outline;

  private BootstrapStyle(BootstrapComponent c, BootstrapColor col, BootstrapSize size, BootstrapVersion v)
  {
    this.component = c;
    this.color = col;
    this.size = size;
    this.version = v;
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col)
  {
    return of(c, col, BootstrapVersion.V5);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapVersion v)
  {
    return new BootstrapStyle(c, col, BootstrapSize.NORMAL, v);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size, BootstrapVersion v)
  {
    return new BootstrapStyle(c, col, size, v);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size)
  {
    return new BootstrapStyle(c, col, size, BootstrapVersion.V5);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size, boolean outline)
  {
    BootstrapStyle b = new BootstrapStyle(c, col, size, BootstrapVersion.V5);
    b.outline = outline;
    return b;
  }

  public String cssClass()
  {
    String ol = outline ? "-outline" : "";

    if(size.equals(BootstrapSize.NORMAL))
      return component.prefix() + ol + "-" + color.value();

    return component.prefix() + ol + "-" + color.value() + " " + component.prefix() + "-" + size.value();
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

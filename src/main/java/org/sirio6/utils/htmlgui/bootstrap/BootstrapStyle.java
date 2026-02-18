package org.sirio6.utils.htmlgui.bootstrap;

public final class BootstrapStyle
{
  private final BootstrapComponent component;
  private final BootstrapColor color;
  private final BootstrapVersion version;
  private final BootstrapSize size;
  private final BootstrapOutline outline;

  private BootstrapStyle(BootstrapComponent c, BootstrapColor col, BootstrapSize size, BootstrapOutline outline, BootstrapVersion v)
  {
    this.component = c;
    this.color = col;
    this.size = size;
    this.version = v;
    this.outline = outline;
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col)
  {
    return of(c, col, BootstrapVersion.V5);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapVersion v)
  {
    return new BootstrapStyle(c, col, BootstrapSize.NORMAL, BootstrapOutline.DEFAULT, v);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size, BootstrapVersion v)
  {
    return new BootstrapStyle(c, col, size, BootstrapOutline.DEFAULT, v);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size)
  {
    return new BootstrapStyle(c, col, size, BootstrapOutline.DEFAULT, BootstrapVersion.V5);
  }

  public static BootstrapStyle of(BootstrapComponent c, BootstrapColor col, BootstrapSize size, BootstrapOutline outline)
  {
    BootstrapStyle b = new BootstrapStyle(c, col, size, outline, BootstrapVersion.V5);
    return b;
  }

  public String cssClass()
  {
    String ol = outline.isOutline() ? "-outline" : "";

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

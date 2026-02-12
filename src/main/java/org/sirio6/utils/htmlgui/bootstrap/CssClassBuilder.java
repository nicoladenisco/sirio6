package org.sirio6.utils.htmlgui.bootstrap;

import java.util.*;

public class CssClassBuilder
{
  private final List<String> classes = new ArrayList<>();

  public CssClassBuilder add(String css)
  {
    classes.add(css);
    return this;
  }

  public CssClassBuilder add(BootstrapStyle s)
  {
    classes.add(s.cssClass());
    return this;
  }

  public CssClassBuilder addAll(Collection<String> css)
  {
    classes.addAll(css);
    return this;
  }

  public String build()
  {
    return String.join(" ", classes);
  }

  @Override
  public String toString()
  {
    return "CssClassBuilder{" + "classes=" + classes + '}';
  }
}

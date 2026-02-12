/*
 * Copyright (C) 2026 Nicola De Nisco
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
package org.sirio6.utils.htmlgui;

import org.sirio6.utils.htmlgui.bootstrap.BootstrapColor;

/**
 *
 * @author Nicola De Nisco
 */
public class HtmlguiElementTest
{

  public HtmlguiElementTest()
  {
  }

  @org.junit.BeforeClass
  public static void setUpClass()
     throws Exception
  {
  }

  @org.junit.AfterClass
  public static void tearDownClass()
     throws Exception
  {
  }

  @org.junit.Before
  public void setUp()
     throws Exception
  {
  }

  @org.junit.After
  public void tearDown()
     throws Exception
  {
  }

  @org.junit.Test
  public void test1()
  {
    System.out.println("test1");

    Toolbar t = new Toolbar();
    t.addButtonGroup(
       new ButtonGroup()
          .addButton(new Button("primo", BootstrapColor.INFO))
          .addButton(new Button("secondo", BootstrapColor.INFO))
          .addButton(new Button("terzo", BootstrapColor.INFO))
    );

    System.out.println("**\n" + t);
  }

  @org.junit.Test
  public void test2()
  {
    System.out.println("test3");

    Toolbar t = new Toolbar();
    t.addButtonGroup(
       new ButtonGroup()
          .addButton(new Button("primo", BootstrapColor.INFO).OnClick("fun1()"))
          .addButton(new Button("secondo", BootstrapColor.INFO).OnClick("fun2()"))
          .addButton(new Button("terzo", BootstrapColor.INFO).OnClick("fun3()"))
          .addButton(new Dropdown("dropdown", BootstrapColor.PRIMARY)
             .addItem(new DropdownItem("item1", "pippo()"))
             .addItem(new DropdownItem("item2", "pluto()"))
             .addItem(new DropdownItem("item3", "paperino()"))
             .addItem(new DropdownSeparator())
             .addItem(new DropdownItem("item4", "minnie()"))
          )
    );

    System.out.println("**\n" + t);
  }

  @org.junit.Test
  public void test3()
  {
    System.out.println("test3");

    Toolbar t = new Toolbar();
    t.addButtonGroup(
       new ButtonGroup()
          .addButton(new Button("primo", BootstrapColor.INFO).OnClick("fun1()"))
          .addButton(new Button("secondo", BootstrapColor.INFO).OnClick("fun2()"))
          .addButton(new Button("terzo", BootstrapColor.INFO).OnClick("fun3()"))
          .addButton(new Dropdown("miodrop", BootstrapColor.PRIMARY)
             .addItem(new DropdownItem("item1", "pippo()"))
             .addItem(new DropdownItem("item2", "pluto()"))
             .addItem(new DropdownItem("item3", "paperino()"))
             .addItem(new DropdownSeparator())
             .addItem(new DropdownItem("item4", "minnie()"))
          )
          .addButton(new Button("primo", BootstrapColor.INFO).OnClick("fun4()"))
          .addButton(new DropdownSplit("miodropsplit", BootstrapColor.DANGER).OnClick("funsplit1()")
             .addItem(new DropdownItem("item1", "pippo()"))
             .addItem(new DropdownItem("item2", "pluto()"))
             .addItem(new DropdownItem("item3", "paperino()"))
             .addItem(new DropdownSeparator())
             .addItem(new DropdownItem("item4", "minnie()"))
          )
    );

    System.out.println("**\n" + t);
  }
}

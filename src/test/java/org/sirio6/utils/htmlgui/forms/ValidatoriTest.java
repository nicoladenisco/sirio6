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
package org.sirio6.utils.htmlgui.forms;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nicola De Nisco
 */
public class ValidatoriTest
{

  public ValidatoriTest()
  {
  }

  @BeforeClass
  public static void setUpClass()
  {
  }

  @AfterClass
  public static void tearDownClass()
  {
  }

  @Before
  public void setUp()
  {
  }

  @After
  public void tearDown()
  {
  }

  @Test
  public void testIntNozero()
  {
    System.out.println("intNozero");
    assertTrue(Validatori.intNozero(1));
    assertFalse(Validatori.intNozero(0));
  }

  @Test
  public void testIntRange()
  {
    System.out.println("intRange");
    assertTrue(Validatori.intRange(1, 100, 1));
    assertTrue(Validatori.intRange(1, 100, 100));
    assertTrue(Validatori.intRange(1, 100, 10));
    assertFalse(Validatori.intRange(1, 100, 0));
    assertFalse(Validatori.intRange(1, 100, 101));
  }

  @Test
  public void testIpV4()
  {
    System.out.println("ipV4");
    assertTrue(Validatori.ipV4("10.10.10.10"));
    assertFalse(Validatori.ipV4("1000.10.10.10"));
    assertFalse(Validatori.ipV4("10.300.10.10"));
  }

  @Test
  public void testHttpUri()
  {
    System.out.println("httpUri");
    assertTrue(Validatori.httpUri("http://localhost/perseo/app/template/ImpostaLink.vm"));
    assertTrue(Validatori.httpUri("http://localhost:8080/perseo/app/template/ImpostaLink.vm"));
    assertTrue(Validatori.httpUri("https://localhost:8080/perseo/app/template/ImpostaLink.vm"));
    assertFalse(Validatori.httpUri("aaaa://localhost:8080/perseo/app/template/ImpostaLink.vm"));
    assertFalse(Validatori.httpUri("http://localhost:88000/perseo/app/template/ImpostaLink.vm"));
  }

  @Test
  public void testUuid()
  {
    System.out.println("uuid");
    assertTrue(Validatori.uuid("caf916ba-044b-4d9b-b472-60986d738139"));
    assertFalse(Validatori.uuid("caf916ba-044b-4d9b-b472-60986d738139qqqqq"));
  }
}

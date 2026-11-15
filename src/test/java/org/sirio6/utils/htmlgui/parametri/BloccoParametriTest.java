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
package org.sirio6.utils.htmlgui.parametri;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rigel5.DefaultRigelI18nImplementation;
import org.sirio6.utils.SirioGenericContext;

/**
 * Test per parametri.
 *
 * @author Nicola De Nisco
 */
public class BloccoParametriTest
{

  public BloccoParametriTest()
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
  public void testGetParametri()
     throws Exception
  {
    System.out.println("getParametri");
    BloccoParametri instance = new BloccoParametri();
    Parametro<String> ps = new Parametro<>(instance, "codice_ps", "nome", "descrizione", "helpBlock", "", ParametroTipoVis.TIPO_VIS_TESTO);
    Parametro<Integer> pi = new Parametro<>(instance, "codice_pi", "nome", "descrizione", "helpBlock", 0, ParametroTipoVis.TIPO_VIS_TESTO);
    Parametro<Double> pd = new Parametro<>(instance, "codice_pd", "nome", "descrizione", "helpBlock", 0.0, ParametroTipoVis.TIPO_VIS_TESTO);
    Parametro<Boolean> pb = new Parametro<>(instance, "codice_pb", "nome", "descrizione", "helpBlock", false, ParametroTipoVis.TIPO_VIS_CHECK);

    StringBuilder html = buildHtml(instance);
    assertTrue(!html.toString().isEmpty());
  }

  private StringBuilder buildHtml(BloccoParametri instance)
     throws Exception
  {
    Bootstrap5Formatter b5f = new Bootstrap5Formatter(instance);
    StringBuilder html = new StringBuilder();
    SirioGenericContext ctx = new SirioGenericContext(new DefaultRigelI18nImplementation());
    b5f.getHtml(0, html, ctx);
    System.out.println(html);
    return html;
  }

  @Test
  public void testAddParametro()
     throws Exception
  {
    System.out.println("addParametro");
    BloccoParametri instance = new BloccoParametri();
    Parametro<String> ps = new Parametro<>(null, "codice_ps", "nome", "descrizione", "helpBlock", "", ParametroTipoVis.TIPO_VIS_TESTO);
    instance.addParametro(ps);
    StringBuilder html = buildHtml(instance);
    assertTrue(!html.toString().isEmpty());
  }

  @Test
  public void testToString()
  {
    System.out.println("toString");
    BloccoParametri instance = new BloccoParametri();
    Parametro<String> ps = new Parametro<>(instance, "codice_ps", "nome", "descrizione", "helpBlock", "", ParametroTipoVis.TIPO_VIS_TESTO);

    String expResult = "BloccoParametri{lsParametri=[Parametro{codice=codice_ps, nome=nome, val=}]}";
    String result = instance.toString();
    assertEquals(expResult, result);
  }
}

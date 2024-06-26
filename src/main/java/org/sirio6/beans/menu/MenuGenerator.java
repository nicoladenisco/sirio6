/* 
 * Copyright (C) 2020 Nicola De Nisco
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
package org.sirio6.beans.menu;

import org.apache.turbine.util.RunData;
import org.sirio6.utils.tree.CoreMenuTreeNode;
import org.jdom2.Element;

/**
 * Definizione di un generatore astratto di menu a runtime.
 *
 * @author Nicola De Nisco
 */
public interface MenuGenerator
{
  /**
   * Creazione dinamica di voci di menu.
   * Consente di aggiungere voci dinamiche al menu
   * indicato come parametro.
   * @param submenugen nome indicato per la generazione nell'XML
   * @param data oggetto rundata al momento della creazione
   * @param livello livello di sottomenu corrente
   * @param elPadre elemento XML che ha generato questa chiamata
   * @param node nodo a cui aggiungere altri sottomenu
   * @throws Exception
   */
  public void creaSottoMenu(String submenugen, RunData data, int livello,
     Element elPadre, CoreMenuTreeNode node)
     throws Exception;
}

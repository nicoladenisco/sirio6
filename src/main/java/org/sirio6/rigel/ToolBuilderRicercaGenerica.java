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
package org.sirio6.rigel;

import org.commonlib5.utils.StringOper;
import org.rigel5.RigelI18nInterface;
import static org.rigel5.table.BuilderRicercaGenerica.IDX_CRITERIA_EQUAL;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.sql.SqlBuilderRicercaGenerica;

/**
 * Costruttore del filtro dei records per le liste
 * che utilizzano query libere.
 * Versione specializzata per l'uso nei tool.
 *
 * @author Nicola De Nisco
 */
public class ToolBuilderRicercaGenerica extends SqlBuilderRicercaGenerica
{
  protected String unique, url;

  public ToolBuilderRicercaGenerica(RigelTableModel Ptm, String nometab, String unique, String url)
     throws Exception
  {
    super(Ptm, nometab);
    this.unique = unique;
    this.url = url;
  }

  @Override
  public String getHtmlComboColonnaMaschera(String formName, String fieldName,
     RigelColumnDescriptor cd, String defVal, RigelI18nInterface i18n)
     throws Exception
  {
    String sOut = "";
    String nomeCombo = "VL" + fieldName;

    // imposta automaticamente il combo del criteria a uguale
    // cambiaTipoRicercaToolCombo(unique, uniqueForm, fieldName, valore)
    String setComboFunjs = "rigel.cambiaTipoRicercaToolCombo("
       + "'" + unique + "', '" + unique + "_full', '" + fieldName + "', " + IDX_CRITERIA_EQUAL
       + ")";
    sOut = "<select name=\"" + nomeCombo + "\" onChange=\"" + setComboFunjs + "\">";

    if(cd.isComboSelf())
      sOut += cd.getHtmlComboColonnaSelf(0, 0, ptm, nomeTabella, cd.getName(), defVal);
    else
      sOut += cd.getHtmlComboColonnaAttached(0, 0, ptm, defVal, i18n, false);
    sOut += "</select>";

    return sOut;
  }

  @Override
  public String getHtmlComboColonnaRicSemplice(String formName, String fieldName,
     RigelColumnDescriptor cd, String defVal, RigelI18nInterface i18n)
     throws Exception
  {
    StringBuilder sOut = new StringBuilder(512);
    String nomeCombo = "VL" + fieldName;

    // submit immediato del form quando cambia il valore
    // submitTool2(uniqueBody, uniqueForm, url)
    String setComboFunjs = "document." + formName + ".submit();";
    setComboFunjs = "rigel.submitTool2("
       + "'" + unique + "', '" + unique + "_simple', '" + url + "'"
       + ")";
    sOut.append("<select name=\"").append(nomeCombo).append("\" onChange=\"").append(setComboFunjs).append("\">");

    // aggiunge il valore per annullare questo campo
    defVal = StringOper.okStrNull(defVal);
    if(defVal == null || defVal.equals("0"))
      sOut.append("<option value=\"\" selected>TUTTI</option>");
    else
      sOut.append("<option value=\"\">TUTTI</option>");

    if(cd.isComboSelf())
      sOut.append(cd.getHtmlComboColonnaSelf(0, 0, ptm, nomeTabella, cd.getName(), defVal));
    else
      sOut.append(cd.getHtmlComboColonnaAttached(0, 0, ptm, defVal, i18n, true));

    sOut.append("</select>");

    return sOut.toString();
  }
}

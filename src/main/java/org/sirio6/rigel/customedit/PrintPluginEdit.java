/*
 *  PrintPluginEdit.java
 *
 *  Copyright (C) 2024 Dedalus Italia S.p.A
 *
 *  Questo software è proprietà di Dedalus Italia S.p.A
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Dedalus Italia S.p.A
 *  Via di Collodi, 6/C
 *  50141 Firenze
 */
package org.sirio6.rigel.customedit;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;
import org.jdom2.Element;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.CustomColumnEdit;
import org.rigel5.table.RigelColumnDescriptor;
import org.sirio6.services.print.plugin.PdfGeneratorFactory;
import org.sirio6.utils.SU;

/**
 * Genera un combo box con i plugin di stampa disponibili a setup.
 *
 * @author Nicola De Nisco
 */
public class PrintPluginEdit implements CustomColumnEdit
{
  private List<String> arPlg;

  @Override
  public void init(Element eleXML)
     throws Exception
  {
  }

  @Override
  public String getHtmlEdit(RigelColumnDescriptor cd, TableModel model,
     int row, int col, String cellText, String cellHtml, String nomeCampo, RigelI18nInterface i18n)
     throws Exception
  {
    if(arPlg == null)
    {
      arPlg = new ArrayList<>(PdfGeneratorFactory.getInstance().getPluginNames());
      arPlg.sort(SU::compare);
    }

    return comboStrings1(nomeCampo, arPlg, cellText, false);
  }
}

/*
 *  BloccoParametri.java
 *
 *  Copyright (C) 2025 Dedalus Italia S.p.A
 *
 *  Questo software è proprietà di Dedalus Italia S.p.A
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Dedalus Italia S.p.A
 *  Via di Collodi, 6/C
 *  50141 Firenze
 */
package org.sirio6.utils.htmlgui.parametri;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocco di parametri.
 *
 * @author Nicola De Nisco
 */
public class BloccoParametri
{
  private final List<Parametro> lsParametri = new ArrayList<>();

  public List<Parametro> getParametri()
  {
    return lsParametri;
  }

  public void addParametro(Parametro p)
  {
    if(lsParametri.stream().anyMatch((pp) -> pp.getCodice().equals(p.getCodice())))
      throw new IllegalArgumentException("Il parametro " + p.getCodice() + " già esiste.");

    lsParametri.add(p);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("BloccoParametri{");
    sb.append("lsParametri=").append(lsParametri);
    sb.append('}');
    return sb.toString();
  }
}

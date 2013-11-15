/*
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contact Information:
 * Dambach-Werke GmbH
 * Elektronische Leitsysteme
 * Fritz-Minhardt-Str. 1
 * 76456 Kuppenheim
 * Phone: +49-7222-402-0
 * Fax: +49-7222-402-200
 * mailto: info@els.dambach.de
 */

package de.bsvrz.vew.syskal.syskal.systemkalendereintrag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Kommentar
 * 
 * @version $Revision: 1.1 $ / $Date: 2009/09/24 12:49:16 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class ListeZustandsWechsel
{

  private Debug _debug = Debug.getLogger();

  /**
   * 
   */
  private SortedMap<Long, Boolean> listeZustandsWechsel = new TreeMap<Long, Boolean>();

  /**
   * @param list
   */
  public void setListeZustandsWechsel(SortedMap<Long, Boolean> list)
  {

    listeZustandsWechsel = list;

  }

  /**
   * @return
   */
  public SortedMap<Long, Boolean> getListeZustandsWechsel()
  {

    if (listeZustandsWechsel != null)
      return listeZustandsWechsel;

    return null;

  }

  /**
   * Berechnet den naechsten zeitlichen Zustandswechsel ausgehend von einem Startzeitpunkt
   * 
   * @param start
   *          Startzeitpunkt der Berechnung
   * @return Map.Entry<Long, Boolean> Zeitpunkt mit Zustand
   */
  protected Map.Entry<Long, Boolean> berechneNaechstenZustandsWechsel(long start)
  {

    Calendar cal1 = new GregorianCalendar();
    Calendar cal2 = new GregorianCalendar();
    Date d = new Date();
    d.setTime(start);
    cal1.setTime(d);

    for (Map.Entry<Long, Boolean> e : listeZustandsWechsel.entrySet())
    {

      cal2.setTimeInMillis(e.getKey());

      if (cal2.after(cal1) || cal2.equals(cal1))
        return e;

    }

    return null;
  }

  /**
   * Berechnet den letzten zeitlichen Zustandswechsel ausgehend von eine Startzeitpunkt
   * 
   * @param ende
   *          Startzeitpunkt der Berechnung
   * @return Map.Entry<Long, Boolean> Zeitpunkt mit Zustand
   */
  protected Map.Entry<Long, Boolean> berechneLetztenZustandsWechsel(long ende)
  {

    Calendar cal1 = new GregorianCalendar();
    Calendar cal2 = new GregorianCalendar();
    Date d = new Date();
    d.setTime(ende);
    cal1.setTime(d);

    Collection col = (Collection)listeZustandsWechsel.keySet();

    List list = new ArrayList();

    list.addAll(col);

    for (ListIterator i = list.listIterator(list.size()); i.hasPrevious();)
    {

      Long l = (Long)i.previous();
      cal2.setTimeInMillis(l);

      if (cal2.before(cal1) || cal2.equals(cal1))
      {
        Boolean v = listeZustandsWechsel.get(l);

        // Dummy-Liste mit nur einem Eintrag wird erstellt, um ein Map.Entry zu bekommen
        HashMap<Long, Boolean> map = new HashMap<Long, Boolean>();
        map.put(l, v);

        for (Map.Entry<Long, Boolean> e : map.entrySet())
        {
          if (l == e.getKey())
            return e;

        }

      }

    }

    return null;
  }

  /**
   * Berechnet alle zeitlichen Zustandswechsel in einen definierten Zeitraum
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * 
   * @return SortedMap<Long, Boolean> zeitlich sortierte Liste mit dem Zeitpunkten der Zustandswechsel und dem Zustand
   * 
   */
  public SortedMap<Long, Boolean> berechneVonBis(long von, long bis)
  {

    Map.Entry<Long, Boolean> start = berechneNaechstenZustandsWechsel(von);
    Map.Entry<Long, Boolean> ende = berechneLetztenZustandsWechsel(bis);

    if (start == null || ende == null)
    {
      return null;
    }

    Date d = new Date();
    d.setTime(start.getKey());

    // Liegt der naechste und der letzte Wechsel innerhalb des
    // vorgegebenen Zeitbereichs
    if (start.getKey() >= von && start.getKey() <= bis && ende.getKey() >= von && ende.getKey() <= bis)
    {

    }
    else
    {

      return null;

    }

    Date d1 = new Date();
    Date d2 = new Date();

    d1.setTime(start.getKey());
    d2.setTime(ende.getKey());

    // Da subMap nur einen halboffenes Intervall (a,b] liefert,
    // muss ein Wert ueber die obere Grenze hinaus angegeben werden
    SortedMap<Long, Boolean> liste = listeZustandsWechsel.subMap(start.getKey(), ende.getKey() + 1);

    if (liste != null)
    {
      return liste;
    }

    return null;
  }

  /**
   * Setzt die zeitliche Gueltigkeit, bei einer Definition ueber Beginn und Ende der zeitl. Gueltigkeit
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return SortedMap<Long, Boolean> zeitlich sortierte Liste mit dem Zeitpunkten der Zustandswechsel´der zeitlichen
   *         Gueltigkeit und dem Zustand
   */
  public SortedMap<Long, Boolean> setGueltigkeitZeit(long von, long bis)
  {

    if (bis <= von)
      return null;

    Calendar cal = new GregorianCalendar();

    Date dtVon = new Date();
    Date dtBis = new Date();

    dtVon.setTime(von);
    dtBis.setTime(bis);

    cal.setTime(dtVon);

    listeZustandsWechsel.put(cal.getTimeInMillis(), true);

    cal.setTime(dtBis);

    listeZustandsWechsel.put(cal.getTimeInMillis(), false);

    return listeZustandsWechsel;
  }

  /**
   * Setzt die verkehrliche Gueltigkeit, bei einer Definition ueber Beginn und Ende der zeitl. Gueltigkeit
   * 
   * @param listZeitl
   *          Liste der zeitlichen Zustandsaenderungen auf welche sich bezogen wird
   * 
   * @param vor
   *          Offset zum Startzeitpunkt
   * @param bezVor
   *          Beziehung des Offsets zum Startzeitpunkt
   * 
   * @param nach
   *          Offset zum Endezeitpunkt
   * @param bezNach
   *          Beziehung des Offsets zum Endezeitpunkt
   * 
   * @return SortedMap<Long, Boolean> zeitlich sortierte Liste mit dem Zeitpunkten der Zustandswechsel der
   *         verkehrlichen Gueltigkeit und dem Zustand
   */
  public SortedMap<Long, Boolean> setGueltigkeitVerkehr(SortedMap<Long, Boolean> listZeitl, long vor, int bezVor,
      long nach, int bezNach)
  {

    Date dtVor = new Date();
    Date dtNach = new Date();

    dtVor.setTime(vor);
    dtNach.setTime(nach);

    for (Map.Entry<Long, Boolean> me : listZeitl.entrySet())
    {

      Long tmp = 0L;
      Long l = me.getKey();
      Boolean gueltig = me.getValue();

      if (gueltig)
      {

        switch (bezVor)
        {
          case 0:
            tmp = l - vor;
            break;
          case 1:
            tmp = l + vor;
            break;
          case 2:
            tmp = l - nach;
            break;
          case 3:
            tmp = l + nach;
            break;

          default:
            _debug.fine("Fehler bezVor");
            // return null;
        }

        listeZustandsWechsel.put(tmp, true);

      }
      else
      {

        switch (bezNach)
        {
          case 0:
            tmp = l - vor;
            break;
          case 1:
            tmp = l + vor;
            break;
          case 2:
            tmp = l - nach;
            break;
          case 3:
            tmp = l + nach;
            break;

          default:
            _debug.fine("Fehler bezNach");
        }

        listeZustandsWechsel.put(tmp, false);
      }

    }

    return listeZustandsWechsel;
  }

}

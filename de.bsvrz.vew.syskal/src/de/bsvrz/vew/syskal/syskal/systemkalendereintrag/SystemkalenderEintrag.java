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

import java.util.SortedMap;

import de.bsvrz.vew.syskal.syskal.erinnerungsfunktion.ErinnerungsFunktion;

/**
 * Schnittstelle zum Erzeugen von SystemKalenderEinträgen. Bietet einen einheitlichen Zugriff auf alle Typen von
 * SystemKalendereinträgen *
 * 
 * @version $Revision: 1.5 $ / $Date: 2010/08/03 07:34:26 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public interface SystemkalenderEintrag
{
  
  /**
   * @param name
   *          setzt den Namen des Eintrags
   */
  public void setName(String name);  
  
  /**
   * Prueft den Eintrag auf Gueltikeit
   * 
   * @return true, wenn die Pruefung erfolgreich war
   */
  public boolean pruefeEintrag();

  /**
   * Berechnet die Zeitstempel der Zustandswechsel
   * 
   * @param jahr
   *          das Jahr für welches die Wechsel berechnet werden
   * @return SortedMap<Long, Boolean> zeitl. sortierte Liste der Zustandswechsel
   */
  public SortedMap<Long, Boolean> berechneZustandsWechsel(int jahr);

  /**
   * Berechnet die Zeitstempel der Zustandswechsel
   * 
   * @param von
   *          Anfangszeitpunkt
   * @param bis
   *          Endezeitpunkt
   * @param jahr
   *          das Jahr für welches die Wechsel berechnet werden
   * @return SortedMap<Long, Boolean> zeitl. sortierte Liste der Zustandswechsel
   */
  public SortedMap<Long, Boolean> berechneZustandsWechsel(Long von, Long bis, int jahr);
  
  /**
   * Berechnet die Zeitstempel der Zustandswechsel
   * 
   * @param von
   *          Anfangszeitpunkt
   * @param bis
   *          Endezeitpunkt
   * @param jahr
   *          das Jahr für welches die Wechsel berechnet werden
   * @return SortedMap<Long, Long> zeitl. sortierte Liste der Gültigkeiten
   */
  public SortedMap<Long, Long> berechneIntervall(Long von, Long bis, int jahr);

  
  /**
   * Berechnet die Zeitstempel der Zustandswechsel für das Versenden des Ereigniszustands
   * 
   * @param von
   *          Anfangszeitpunkt
   * @param bis
   *          Endezeitpunkt
   * @param jahr
   *          das Jahr für welches die Wechsel berechnet werden
   * @return SortedMap<Long, Boolean> zeitl. sortierte Liste der Zustandswechsel
   */
  public SortedMap<Long, Boolean> berechneZustandsWechselZustand(Long von, Long bis, int jahr);
  
  
  /**
   * Getter fuer die Liste der Zeitstempel
   * 
   * @return SortedMap<Long, Boolean> Sortierte Liste der Zustandswechsel
   */
  public SortedMap<Long, Boolean> getListeZustandsWechsel();
  
  /**
   * Getter fuer die Liste der Zeitstempel
   * 
   * @return SortedMap<Long, Boolean> Sortierte Liste der Zustandswechsel
   */
  public void setListeZustandsWechsel(SortedMap<Long, Boolean> liste);
  
  /**
   * Getter fuer das Listeobjekt mit den Zeitstempeln
   * 
   * @return ListeZustandsWechsel Objekt der Liste mit den Zustandswechsel
   */
  public ListeZustandsWechsel getObjektListeZustandsWechsel();

  /**
   * Getter fuer das Listeobjekt mit den Zeitstempeln
   * 
   * @return ListeZustandsWechsel Objekt der Liste mit den Zustandswechsel
   */
  public void setObjektListeZustandsWechsel(ListeZustandsWechsel liste);

  /**
   * Getter fuer die Pid des SystemKalenderEintrags
   * 
   * @return String Pid des SystemKalenderEintrags
   */
  public String getPid();
  
  /**
   * Getter fuer die Pid des SystemKalenderEintrags
   * 
   * @return String Pid des SystemKalenderEintrags
   */
  public void setPid(String pid);

  /**
   * Getter fuer die Pid des SystemKalenderEintrags
   * 
   * @return String Pid des SystemKalenderEintrags
   */
  public void setDefinition(String definition);
  
  /**
   * Getter fuer die Pid des SystemKalenderEintrags
   * 
   * @return String Pid des SystemKalenderEintrags
   */
  public String getDefinition();


  /**
   * Getter fuer die Definition des SystemKalenderEintrags
   * 
   * @return String Definition des SystemKalenderEintrags
   */
  public String getName();

  /**
   * Berechnet ob eine Gueltigkeit vorliegt
   * 
   * @param jetzt
   *          Zeitpunkt der betrachetet werden soll
   * 
   * @return Boolean true, wenn der letzte Zustandwechsel von false nach true war
   */
  public boolean isGueltig(long jetzt);

  /**
   * Berechnet ob eine Gueltigkeit in dem angegeben Intervall vorliegt
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return Boolean true, wenn es einen Zustandwechsel von false nach true im angegebenen Zeitraum gab
   */
  public boolean isGueltigVonBis(long von, long bis);

  /**
   * Berechnet ob eine Gueltigkeit in dem angegeben Intervall vorliegt
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return Boolean true, wenn es einen Zustandwechsel von false nach true im angegebenen Zeitraum gab
   */
  public SortedMap<Long, Boolean> berecheneZustandsWechselVonBis(Long von, Long bis);

  /**
   * Berechnet die Gueltigkeiten, welche in dem angegeben Intervall vorliegen
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return Liste der Gültigkeiten
   */
  public SortedMap<Long, Long> berecheneIntervallVonBis(Long von, Long bis);

  /**
   * setter fuer ein Wecker-Objekt
   * 
   * @param f
   *          wenn true, wird ein Wecker gestellt wenn false geht der Wecker gleich los
   */
  public void setErinnerungsFunktion(Boolean f);

  /**
   * setter fuer ein Wecker-Objekt
   * 
   * @param f
   *          wenn true, wird ein Wecker gestellt wenn false geht der Wecker gleich los
   */
  public ErinnerungsFunktion getErinnerungsFunktion();

}

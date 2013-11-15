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

package de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderArbeiter;
import de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag;

/**
 * Die Klasse zum Versenden der Ereigniszustaende. Erweitert die Klasse TimerTask. Die run() Methode wird ausgefuehrt
 * wenn die im ReminderService eingestellte Zeit abgelaufen ist. Sie implentiert zusätzlich das ClientSenderInterface
 * welches die Methoden zum Versenden der Daten bereitstellt.
 * 
 * @version $Revision: 1.1 $ / $Date: 2009/09/24 12:49:16 $ / ($Author: Pittner $)
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 */
public class BenachrichtigeFunktion extends TimerTask
{
  /**
   * Die Zeit bis die Daten versendet werden sollen
   */
  private SystemkalenderEintrag ske;

  /**
   * Die Zeit bis die Daten versendet werden sollen
   */
  private Long time_now;

  /**
   * Die Pid des Ereignisses
   */
  private String pid;

  /**
   * Der Debugger, stellt verschiedene Protokollierungsfunktionen zur Verfügung
   */
  private Debug _debug;

  private static final BenachrichtigeListenerVerwaltung _multi = new BenachrichtigeListenerVerwaltung();

  /**
   * Konstruktor der Klasse
   * 
   */

  public BenachrichtigeFunktion()
  {

  }

  /**
   * Konstruktor der Klasse, mit Zeitangabe des Zustandswechsels
   * 
   * @param e
   *          das Ereignis, welches den Zustand meldet
   * @param now
   *          Die Zeit bis die Daten versendet werden sollen
   * 
   */
  public BenachrichtigeFunktion(SystemkalenderEintrag ske, Long now)
  {
    this.ske = ske;
    pid = ske.getPid();
    this.time_now = now;
    _debug = SystemkalenderArbeiter.getDebug();

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.TimerTask#run()
   */
  public void run()
  {
    Date d = new Date();
    d.setTime(time_now);

    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    String datum = format.format(d);

    String meldung;
    if (ske.isGueltig(time_now))
    {
      meldung = datum + " " + ske.getPid() + "(" + ske.getName() + ")" + " : gültig";
    }
    else
    {
      meldung = datum + " " + ske.getPid() + "(" + ske.getName() + ")" + " : nicht gültig";
    }

    BenachrichtigeEvent event = new BenachrichtigeEvent(this, meldung);

    _multi.meldeAnAlle(event);

  }

  public static BenachrichtigeListenerVerwaltung getBenachrichtigeListenerVerwaltung()
  {
    return _multi;
  }

}

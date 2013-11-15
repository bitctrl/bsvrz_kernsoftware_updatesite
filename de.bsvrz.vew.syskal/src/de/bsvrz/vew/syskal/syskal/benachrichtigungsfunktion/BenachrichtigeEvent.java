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

import java.util.EventObject;

/**
 * Klasse die ein Benachrichtigungsereignis darstellt
 * 
 * @version $Revision: 1.1 $ / $Date: 2009/09/24 12:49:16 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class BenachrichtigeEvent extends EventObject
{

  protected String _meldung;

  /**
   * Konstruktor
   * 
   * @param source
   */
  public BenachrichtigeEvent(Object source, String meldung)
  {
    super(source);
    _meldung = meldung;
    // TODO Auto-generated constructor stub
  }

  /**
   * Holt die Meldung aus dem Objekt
   * 
   * @return
   */
  public String getMeldung()
  {
    return _meldung;
  }

}

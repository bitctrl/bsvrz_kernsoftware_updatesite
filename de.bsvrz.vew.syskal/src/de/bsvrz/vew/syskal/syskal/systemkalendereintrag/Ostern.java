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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Die Klasse für die Berechnung eines Ostersonntags *
 * 
 * @version $Revision: 1.1 $ / $Date: 2009/09/24 12:49:16 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class Ostern
{
  /**
   * Berechnet Ostersonntag für den gregorianischen Kalender, also für Jahre > 1583. Die Rückgabe enthält das gleiche
   * Jahr und als Monat, entweder März oder April, sowie den korrekten Tag.
   * 
   * @param year >
   *          1583
   * @return Calendar, Ostersonntag.
   */
  public static Calendar Ostersonntag(int jahr)
  {
    int i = jahr % 19;
    int j = jahr / 100;
    int k = jahr % 100;

    int l = (19 * i + j - (j / 4) - ((j - ((j + 8) / 25) + 1) / 3) + 15) % 30;
    int m = (32 + 2 * (j % 4) + 2 * (k / 4) - l - (k % 4)) % 7;
    int n = l + m - 7 * ((i + 11 * l + 22 * m) / 451) + 114;

    int month = n / 31;
    int day = (n % 31) + 1;

    return new GregorianCalendar(jahr, month - 1, day);
  }

  public boolean isOstersonntag(Calendar cal)
  {

    int jahr = cal.get(Calendar.YEAR);

    Calendar calendar = Ostersonntag(jahr);

    if (cal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
        && cal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
        && cal.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
    {

      return true;

    }

    return false;
  }

}
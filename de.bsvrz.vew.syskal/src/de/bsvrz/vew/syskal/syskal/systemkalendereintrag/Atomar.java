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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.omg.CORBA._PolicyStub;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.vew.syskal.syskal.erinnerungsfunktion.ErinnerungsFunktion;

/**
 * Die Klasse erzeugt die atomaren SystemKalenderEintraege: Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag
 * Sonntag, Ostersonntag und Tag *
 * 
 * @version $Revision: 1.8 $ / $Date: 2015/06/08 15:13:12 $ / ($Author: Pittner $)
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 */
public class Atomar implements SystemkalenderEintrag, Cloneable
{
  /**
   * Die Liste der Zeiten der Zustandswechsel fuer die zeitliche Gueltigkeit
   */
  protected ListeZustandsWechsel listeZustandsWechsel = new ListeZustandsWechsel();

  /**
   * Die Pid des Ske
   */
  public String pid;

  /**
   * Definition des Ske
   */
  private String name;

  /**
   * Definition des Ske
   */
  private String definition;

  /**
   * Der Debugger, stellt verschiedene Protokollierungsfunktionen zur Verfügung
   */
  private ErinnerungsFunktion rs;

  /**
   * Der Debugger, stellt verschiedene Protokollierungsfunktionen zur Verfügung
   */
  protected Debug _debug;

  /**
   * Konstruktor der Klasse
   * 
   * @param pid
   *          Die Pid des Ske
   */
  public Atomar(String pid, String definition)
  {

    this.pid = pid;
    this.definition = definition;
    // timeStampList = new TreeMap<Long, Boolean>();
    _debug = Debug.getLogger();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berechneZustandsWechsel(int)
   */
  public SortedMap<Long, Boolean> berechneZustandsWechsel(int jahr)
  {

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
    Date d1 = null;
    Date d2 = null;
    try
    {
      d1 = df.parse("01.01." + jahr + " 00:00:00,000");
      d2 = df.parse("31.12." + jahr + " 23:59:59,999");
    }
    catch (ParseException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return berecheneZustandsWechselVonBis(d1.getTime(), d2.getTime());

    // listeZustandsWechsel = new ListeZustandsWechsel();
    //
    // long time = 0;
    // long days = 0;
    // Calendar cal1 = new GregorianCalendar().getInstance();
    // Calendar cal2 = new GregorianCalendar();
    // Calendar tmp = new GregorianCalendar();
    //
    // SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
    // Date dt = new Date();
    //
    // int diff = jahr - cal1.get(Calendar.YEAR);
    // int temp = cal1.get(Calendar.YEAR) + diff;
    //
    // try
    // {
    //
    // // Ein Jahr beginnt um...
    // dt = df.parse("01.01." + temp + " 00:00:00,000");
    // cal1.setTime(dt);
    // // Ein Jahr endet um...
    // dt = df.parse("31.12." + temp + " 23:59:59,999");
    // cal2.setTime(dt);
    //
    // // Wie viele Tage hat das Jahr?
    // time = cal2.getTime().getTime() - cal1.getTime().getTime();
    // days = Math.round((double)time / (24. * 60. * 60. * 1000.));
    //
    // // Der erste Tag des Jahres endet um...
    // dt = df.parse("01.01." + temp + " 23:59:59,999");
    // cal2.setTime(dt);
    //
    // if (definition.equalsIgnoreCase("ostersonntag"))
    // {
    //
    // Ostern ostersonntag = new Ostern();
    //
    // cal1 = ostersonntag.Ostersonntag(temp);
    //
    // // Der Ostersonntag beginnt um...
    // dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 00:00:00,000");
    // cal1.setTime(dt);
    // // ...und endet um...
    // dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 23:59:59,999");
    // cal2.setTime(dt);
    //
    // // ...er dauert natuerlich auch nur...
    // days = 1;
    //
    // }
    //
    // }
    // catch (ParseException e)
    // {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // // DateFormat df_ger = DateFormat.getDateInstance(DateFormat.FULL, Locale.GERMANY);
    // DateFormat df_ger = DateFormat.getDateInstance(DateFormat.FULL, Locale.GERMANY);
    //
    // for (int i = 0; i < days; i++)
    // {
    //
    // // Man spricht Deutsch!
    // String datum = df_ger.format(cal1.getTime()).toLowerCase();
    //
    // // Wochentag ist hier...
    // String woTag = definition;
    //
    // if (definition.equals("tag"))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
    //
    // }
    // else if (definition.equals("ostersonntag"))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
    //
    // }
    // else if (datum.contains(woTag))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
    //
    // }
    //
    // tmp = cal1;
    // tmp.add(Calendar.DATE, 1);
    // cal1 = tmp;
    //
    // if (definition.equals("tag"))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
    // // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
    //
    // }
    // else if (definition.equals("ostersonntag"))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
    // // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
    //
    // }
    // else if (datum.contains(woTag))
    // {
    //
    // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
    // // listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
    //
    // }
    //
    // tmp = cal2;
    // tmp.add(Calendar.DATE, 1);
    // cal2 = tmp;
    //
    // }
    //
    // return listeZustandsWechsel.getListeZustandsWechsel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berechneZustandsWechsel(java.lang.Long,
   * java.lang.Long, int)
   */
  public SortedMap<Long, Boolean> berechneZustandsWechsel(Long von, Long bis, int jahr)
  {
    // Die Abfrage besitzt eine eigene Zustandsliste
    ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();

    long time = 0;
    long days = 0;
    // Calendar cal1 = new GregorianCalendar().getInstance();
    GregorianCalendar cal1 = new GregorianCalendar();
    GregorianCalendar cal2 = new GregorianCalendar();
    GregorianCalendar tmp = new GregorianCalendar();

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    Date dt = new Date();

    int diff = jahr - cal1.get(Calendar.YEAR);
    int temp = cal1.get(Calendar.YEAR) + diff;

    try
    {

      cal1.setTimeInMillis(von);
      cal2.setTimeInMillis(bis);

      cal1.set(Calendar.YEAR, temp);
      cal2.set(Calendar.YEAR, temp);

      // Die Abfrage beginnt um...
      cal1.set(Calendar.HOUR_OF_DAY, 0);
      cal1.set(Calendar.MINUTE, 0);
      cal1.set(Calendar.SECOND, 0);
      cal1.set(Calendar.MILLISECOND, 0);

      // Die Abfrage endet um...
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      // Wie viele Tage hat das Jahr?
      time = cal2.getTime().getTime() - cal1.getTime().getTime();
      days = Math.round((double)time / (24. * 60. * 60. * 1000.));

      // Der erste Tag der Abfrage endet um...
      cal2.setTimeInMillis(von);
      cal2.set(Calendar.YEAR, temp);
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      if (definition.equalsIgnoreCase("ostersonntag"))
      {

        Ostern ostersonntag = new Ostern();

        cal1 = (GregorianCalendar)ostersonntag.Ostersonntag(temp);

        // Der Ostersonntag beginnt um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 00:00:00,000");
        cal1.setTime(dt);

        // ...und endet um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 23:59:59,999");
        cal2.setTime(dt);

        // ...er dauert natuerlich auch nur...
        days = 1;

      }

    }
    catch (ParseException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    DateFormat df_ger = DateFormat.getDateInstance(DateFormat.FULL, Locale.GERMANY);

    for (int i = 0; i < days; i++)
    {

      // Man spricht Deutsch!
      String datum = df_ger.format(cal1.getTime()).toLowerCase();

      // Wochentag ist hier...
      String woTag = definition;

      if (definition.equals("tag"))
      {
        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      }
      else if (definition.equals("ostersonntag"))
      {
        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      }
      else if (datum.contains(woTag))
      {
        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      }

      tmp = cal1;
      tmp.add(Calendar.DATE, 1);
      cal1 = tmp;

      if (definition.equals("tag"))
      {
        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      }
      else if (definition.equals("ostersonntag"))
      {

        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);

      }
      else if (datum.contains(woTag))
      {
        listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      }

      tmp = cal2;
      tmp.add(Calendar.DATE, 1);
      cal2 = tmp;

    }

    return listeZustandsWechselAbfrage.getListeZustandsWechsel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berechneZustandsWechsel(java.lang.Long,
   * java.lang.Long, int)
   */
  public SortedMap<Long, Boolean> berechneZustandsWechselZustand(Long von, Long bis, int jahr)
  {
    // Die Abfrage besitzt eine eigene Zustandsliste
    // ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();
    listeZustandsWechsel = new ListeZustandsWechsel();

    long time = 0;
    long days = 0;
    Calendar cal1 = new GregorianCalendar().getInstance();
    Calendar cal2 = new GregorianCalendar();
    Calendar tmp = new GregorianCalendar();

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
    Date dt = new Date();

    int diff = jahr - cal1.get(Calendar.YEAR);
    int temp = cal1.get(Calendar.YEAR) + diff;

    try
    {

      cal1.setTimeInMillis(von);
      cal2.setTimeInMillis(bis);

      // cal1.set(Calendar.YEAR, temp);
      // cal2.set(Calendar.YEAR, temp);

      // Die Abfrage beginnt um...
      cal1.set(Calendar.HOUR_OF_DAY, 0);
      cal1.set(Calendar.MINUTE, 0);
      cal1.set(Calendar.SECOND, 0);
      cal1.set(Calendar.MILLISECOND, 0);

      // Die Abfrage endet um...
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      // Wie viele Tage hat das Jahr?
      time = cal2.getTime().getTime() - cal1.getTime().getTime();
      days = Math.round((double)time / (24. * 60. * 60. * 1000.));

      // Der erste Tag der Abfrage endet um...
      cal2.setTimeInMillis(von);
      cal2.set(Calendar.YEAR, temp);
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      if (definition.equalsIgnoreCase("ostersonntag"))
      {

        Ostern ostersonntag = new Ostern();
        cal1 = ostersonntag.Ostersonntag(temp);

        // Der Ostersonntag beginnt um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 00:00:00,000");
        cal1.setTime(dt);

        // ...und endet um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 23:59:59,999");
        cal2.setTime(dt);

        // ...er dauert natuerlich auch nur...
        days = 1;

      }

    }
    catch (ParseException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    DateFormat df_ger = DateFormat.getDateInstance(DateFormat.FULL, Locale.GERMANY);
    for (int i = 0; i < days; i++)
    {

      // Man spricht Deutsch!
      String datum = df_ger.format(cal1.getTime()).toLowerCase();

      // Wochentag ist hier...
      String woTag = definition;

      if (definition.equals("tag"))
      {
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      }
      else if (definition.equals("ostersonntag"))
      {

        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);

      }
      else if (datum.contains(woTag))
      {

        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);

      }

      tmp = cal1;
      tmp.add(Calendar.DATE, 1);
      cal1 = tmp;

      if (definition.equals("tag"))
      {
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      }
      else if (definition.equals("ostersonntag"))
      {

        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);

      }
      else if (datum.contains(woTag))
      {

        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        listeZustandsWechsel.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
        // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);

      }

      tmp = cal2;
      tmp.add(Calendar.DATE, 1);
      cal2 = tmp;

    }

    // return listeZustandsWechselAbfrage.getListeZustandsWechsel();
    return listeZustandsWechsel.getListeZustandsWechsel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#getListeZustandsWechsel()
   */
  public SortedMap<Long, Boolean> getListeZustandsWechsel()
  {

    return listeZustandsWechsel.getListeZustandsWechsel();

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#getObjektListeZustandsWechsel()
   */
  public ListeZustandsWechsel getObjektListeZustandsWechsel()
  {

    return listeZustandsWechsel;

  }

  /**
   * Berechnet ob eine Gueltigkeit vorliegt
   * 
   * @param jetzt
   *          Zeitpunkt der betrachetet werden soll
   * 
   * @return Boolean true, wenn der letzte Zustandwechsel von false nach true war
   */
  public boolean isGueltig(long jetzt)
  {
    Map.Entry<Long, Boolean> me = listeZustandsWechsel.berechneLetztenZustandsWechsel(jetzt);

    if (me != null)
    {
      if (me.getValue())
        return true;

    }

    return false;
  }

  /**
   * Berechnet ob eine Gueltigkeit in dem angegeben Intervall vorliegt
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return Boolean true, wenn es einen Zustandwechsel von false nach true im angegebenen Zeitraum gab
   */
  public boolean isGueltigVonBis(long von, long bis)
  {

    Map<Long, Boolean> map = listeZustandsWechsel.berechneVonBis(von, bis);

    if (map != null)

      for (Map.Entry<Long, Boolean> me : map.entrySet())
      {
        if (me.getValue())
          return true;

      }

    return false;
  }

  /**
   * Berechnet ob eine Gueltigkeit in dem angegeben Intervall vorliegt
   * 
   * @param von
   *          Startzeitpunkt der Berechnung
   * @param bis
   *          Endezeitpunkt der Berechnung
   * @return Boolean true, wenn es einen Zustandwechsel von false nach true im angegebenen Zeitraum gab
   */
  public Map<Long, Boolean> gueltigVonBis(long von, long bis)
  {

    Map<Long, Boolean> map = listeZustandsWechsel.berechneVonBis(von, bis);

    if (map != null)
    {

      return map;

    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#getPid()
   */
  public String getPid()
  {

    return pid;

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#getDefinition()
   */
  public String getDefinition()
  {

    return definition;

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name
   *          setzt den Namen des Eintrags
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * setter fuer ein Wecker-Objekt
   * 
   * @param f
   *          wenn true, wird ein Wecker gestellt wenn false geht der Wecker gleich los
   */
  public void setErinnerungsFunktion(Boolean f)
  {

    if (f)
      rs = new ErinnerungsFunktion(this, true);
    else
      rs = new ErinnerungsFunktion(this, false);

  }

  /**
   * Getter fuer ein Wecker-Objekt
   * 
   * @return ReminderService das Wecker-Objekt
   */
  public ErinnerungsFunktion getErinnerungsFunktion()
  {

    return rs;

  }

  /**
   * Korrigiert das Ergebnis nach Vorgabe BÜ/AG vom 08.10.2009 
   * 
   * @param von
   *          Startzeitpunkt
   * @param bis
   *          Endzeitpunkt
   * @param tmp
   *          Zeitlich sortierte Liste der zeitlichen Gültigkeiten
   * @return
   *          Zeitlich sortierte Liste der verkehrlichen Gültigkeiten
   */
  private SortedMap<Long, Long> korrigiereErgebnis(Long von, Long bis, SortedMap<Long, Long> tmp)
  {
    
    SortedMap<Long, Long> listeGruppe = new TreeMap<Long, Long>();
    
    if (tmp.size() > 0)
    {
      for (Map.Entry<Long, Long> me : tmp.entrySet())
      {
        Long l1 = me.getKey();
        Long l2 = me.getValue();
        if (l1 != null && l1.equals(tmp.firstKey()))
        {
          if (von > l1)
          {
            l1 = von;
          }
        }
        
        if (l1.equals(tmp.lastKey()))
        {
          if (l2 != null && bis < l2)
          {
            l2 = bis;
          }
        }
        
        listeGruppe.put(l1, l2);
      }
    }
    else
    {
      Date d = new Date();
      d.setTime(von);
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(d);

      SortedMap<Long, Boolean> liste = berechneZustandsWechsel(cal.get(Calendar.YEAR));

      ListeZustandsWechsel lzw = new ListeZustandsWechsel();
      lzw.setListeZustandsWechsel(liste);

      Entry<Long, Boolean> l1 = lzw.berechneLetztenZustandsWechsel(von);

      d.setTime(bis);
      cal = new GregorianCalendar();
      cal.setTime(d);

      liste = berechneZustandsWechsel(cal.get(Calendar.YEAR));

      lzw.setListeZustandsWechsel(liste);

      Entry<Long, Boolean> l2 = lzw.berechneNaechstenZustandsWechsel(von+1);

      if (l1 != null && l2 != null)
      {
        if (von >= l1.getKey() && bis <= l2.getKey())
        {
          listeGruppe.put(von, bis);
        }else if (von >= l1.getKey() && bis > l2.getKey()){
          listeGruppe.put(von, l2.getKey());
        }else if (von < l1.getKey() && bis <= l2.getKey()){
          listeGruppe.put(l1.getKey(), bis);
        }else if (von < l1.getKey() && bis > l2.getKey()){
          listeGruppe.put(l1.getKey(), l2.getKey());
        }
      }
      else if (l1 != null && l2 == null){
//          listeGruppe.put(l1.getKey(), bis);
      }
      else if (l1 == null && l2 != null){
//        listeGruppe.put(von, l2.getKey());
      }
    }
    return listeGruppe;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berecheneZustandsWechselVonBis(java.lang
   * .Long, java.lang.Long)
   */
  public SortedMap<Long, Boolean> berecheneZustandsWechselVonBis(Long von, Long bis)
  {
    // TODO Auto-generated method stub
    Calendar cal1 = new GregorianCalendar();
    cal1.setTimeInMillis(von);
    Calendar cal2 = new GregorianCalendar();
    cal2.setTimeInMillis(bis);

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    SortedMap<Long, Boolean> tmp = new TreeMap<Long, Boolean>();
    SortedMap<Long, Boolean> map = new TreeMap<Long, Boolean>();

    for (int i = cal1.get(Calendar.YEAR); i < cal2.get(Calendar.YEAR) + 1; i++)
    {

      if (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) > 0)
      {

        try
        {
          Date anf = df.parse("01.01." + i + " 00:00:00,000");
          Date end = df.parse("31.12." + i + " 23:59:59,999");

          if (i == cal1.get(Calendar.YEAR))
          {

            map = berechneZustandsWechsel(von, end.getTime(), i);
            Date d = new Date();
            d.setTime(von);

          }
          else if (i == cal2.get(Calendar.YEAR))
          {

            map = berechneZustandsWechsel(anf.getTime(), bis, i);
            Date d = new Date();
            d.setTime(bis);

          }
          else
          {

            map = berechneZustandsWechsel(anf.getTime(), end.getTime(), i);

          }

        }
        catch (ParseException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
      else
      {

        map = berechneZustandsWechsel(von, bis, i);

      }

      if (map != null)
      {
        // tmp.putAll(map);
        for (Map.Entry<Long, Boolean> me : map.entrySet())
        {
          if (me.getKey() >= von && me.getKey() <= bis)
            tmp.put(me.getKey(), me.getValue());
        }
      }

    }

    // tmp = korrigiereErgebnis(von, bis, tmp);

    return tmp;
  }

  public void setDefinition(String definition)
  {
    // TODO Auto-generated method stub
    this.definition = definition;
  }

  public void setListeZustandsWechsel(SortedMap<Long, Boolean> liste)
  {
    // TODO Auto-generated method stub
    this.listeZustandsWechsel.setListeZustandsWechsel(liste);
  }

  public void setObjektListeZustandsWechsel(ListeZustandsWechsel liste)
  {
    // TODO Auto-generated method stub
    this.listeZustandsWechsel = liste;
  }

  public void setPid(String pid)
  {
    // TODO Auto-generated method stub
    this.pid = pid;

  }

  public boolean pruefeEintrag()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected Atomar clone()
  {
    // TODO Auto-generated method stub
    Atomar eintrag = null;

    eintrag = new Atomar(pid, definition);

    eintrag.setObjektListeZustandsWechsel(listeZustandsWechsel);

    return eintrag;

  }

  /* (non-Javadoc)
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berecheneIntervallVonBis(java.lang.Long, java.lang.Long)
   */
  @Override
  public SortedMap<Long, Long> berecheneIntervallVonBis(Long von, Long bis)
  {
    // TODO Auto-generated method stub
    Calendar cal1 = new GregorianCalendar();
    cal1.setTimeInMillis(von);
    Calendar cal2 = new GregorianCalendar();
    cal2.setTimeInMillis(bis);

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    SortedMap<Long, Long> tmp = new TreeMap<Long, Long>();
    SortedMap<Long, Long> map = new TreeMap<Long, Long>();

    for (int i = cal1.get(Calendar.YEAR); i < cal2.get(Calendar.YEAR) + 1; i++)
    {

      if (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) > 0)
      {

        try
        {
          Date anf = df.parse("01.01." + i + " 00:00:00,000");
          Date end = df.parse("31.12." + i + " 23:59:59,999");

          if (i == cal1.get(Calendar.YEAR))
          {

            map = berechneIntervall(von, end.getTime(), i);
            Date d = new Date();
            d.setTime(von);

          }
          else if (i == cal2.get(Calendar.YEAR))
          {

            map = berechneIntervall(von, end.getTime(), i);
            Date d = new Date();
            d.setTime(bis);

          }
          else
          {

            map = berechneIntervall(von, end.getTime(), i);

          }

        }
        catch (ParseException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
      else
      {

        map = berechneIntervall(von, bis, i);

      }

      if (map != null)
      {
        // tmp.putAll(map);

        for (Map.Entry<Long, Long> me : map.entrySet())
        {
          if (me.getKey() >= von && me.getKey() <= bis)
            tmp.put(me.getKey(), me.getValue());
          else if (me.getKey() >= von && me.getKey() > bis)
            tmp.put(me.getKey(), bis);
          else if (me.getKey() < von && me.getKey() <= bis)
            tmp.put(von, me.getValue());
          else
          {
            
          }
        }

      }

    }

    tmp = korrigiereErgebnis(von, bis, tmp);

    return tmp;
  }

  /* (non-Javadoc)
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.SystemkalenderEintrag#berechneIntervall(java.lang.Long, java.lang.Long, int)
   */
  @Override
  public SortedMap<Long, Long> berechneIntervall(Long von, Long bis, int jahr)
  {
    // TODO Auto-generated method stub
    // Die Abfrage besitzt eine eigene Zustandsliste
    // ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();
    SortedMap<Long, Long> liste = new TreeMap<Long, Long>();

    long time = 0;
    long days = 0;
    // Calendar cal1 = new GregorianCalendar().getInstance();
    GregorianCalendar cal1 = new GregorianCalendar();
    GregorianCalendar cal2 = new GregorianCalendar();
    GregorianCalendar tmp = new GregorianCalendar();

    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    Date dt = new Date();

    int diff = jahr - cal1.get(Calendar.YEAR);
    int temp = cal1.get(Calendar.YEAR) + diff;

    try
    {

      cal1.setTimeInMillis(von);
      cal2.setTimeInMillis(bis);

      cal1.set(Calendar.YEAR, temp);
      cal2.set(Calendar.YEAR, temp);

      // Die Abfrage beginnt um...
      cal1.set(Calendar.HOUR_OF_DAY, 0);
      cal1.set(Calendar.MINUTE, 0);
      cal1.set(Calendar.SECOND, 0);
      cal1.set(Calendar.MILLISECOND, 0);

      // Die Abfrage endet um...
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      // Wie viele Tage hat das Jahr?
      time = cal2.getTime().getTime() - cal1.getTime().getTime();
      days = Math.round((double)time / (24. * 60. * 60. * 1000.));

      // Der erste Tag der Abfrage endet um...
      cal2.setTimeInMillis(von);
      cal2.set(Calendar.YEAR, temp);
      cal2.set(Calendar.HOUR_OF_DAY, 23);
      cal2.set(Calendar.MINUTE, 59);
      cal2.set(Calendar.SECOND, 59);
      cal2.set(Calendar.MILLISECOND, 999);

      if (definition.equalsIgnoreCase("ostersonntag"))
      {

        Ostern ostersonntag = new Ostern();

        cal1 = (GregorianCalendar)ostersonntag.Ostersonntag(temp);

        // Der Ostersonntag beginnt um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 00:00:00,000");
        cal1.setTime(dt);

        // ...und endet um...
        dt = df.parse(cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + temp + " 23:59:59,999");
        cal2.setTime(dt);

        // ...er dauert natuerlich auch nur...
        days = 1;

      }

    }
    catch (ParseException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    DateFormat df_ger = DateFormat.getDateInstance(DateFormat.FULL, Locale.GERMANY);
    for (int i = 0; i < days; i++)
    {

      // Man spricht Deutsch!
      String datum = df_ger.format(cal1.getTime()).toLowerCase();

      // Wochentag ist hier...
      String woTag = definition;

      Long l1 = null;
      Long l2 = null;

      if (definition.equals("tag") || definition.equals("ostersonntag") || datum.contains(woTag))
      {
        l1 = cal1.getTimeInMillis();
      }

      // if (definition.equals("tag"))
      // {
      //
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      //
      // }
      // else if (definition.equals("ostersonntag"))
      // {
      //
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      //
      // }
      // else if (datum.contains(woTag))
      // {
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal1.getTimeInMillis(), true);
      // }

      tmp = cal1;
      tmp.add(Calendar.DATE, 1);
      cal1 = tmp;

      if (definition.equals("tag") || definition.equals("ostersonntag") || datum.contains(woTag))
      {
        l2 = cal2.getTimeInMillis();
      }

      // if (definition.equals("tag"))
      // {
      //
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
      // // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      //
      // }
      // else if (definition.equals("ostersonntag"))
      // {
      //
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
      // // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      //
      // }
      // else if (datum.contains(woTag))
      // {
      // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis(), false);
      // // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal2.getTimeInMillis()+1, false);
      // }

      tmp = cal2;
      tmp.add(Calendar.DATE, 1);
      cal2 = tmp;

      if (l1 != null && l2 != null)
        liste.put(l1, l2);
      else if (l1 == null && l2 != null)
        liste.put(von, l2);
      else if (l1 != null && l2 == null)
        liste.put(l1, bis);
    }

    return liste;
  }

}

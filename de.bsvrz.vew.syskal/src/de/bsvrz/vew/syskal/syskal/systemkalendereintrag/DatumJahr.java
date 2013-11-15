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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Die Klasse erzeugt SystemKalenderEintraege vom Typ: "01.01.*,*" oder "17.06.1963,1989 03.10.1990,*". Die Berechnung
 * der Zeitpunkte mit Wildcards bezieht sich noch auf das aktuelle Kalenderjahr. Die Vorgehensweise könnte dahingehend
 * sein, dass beim Jahrewechsel die Zustandswechsel neu berechnet werden. Muss also noch geklärt werden *
 * 
 * @version $Revision: 1.6 $ / $Date: 2010/08/03 07:44:21 $ / ($Author: Pittner $)
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 */
public class DatumJahr extends Atomar implements Cloneable
{

  /**
   * Die Jahresliste
   */
  protected List<String[]> jahrVonBis;

  /**
   * Definition des Ske
   */
  protected String definition;

  // protected Debug _debug;

  /**
   * Konstruktor der Klasse
   * 
   * @param pid
   *          Die Pid
   * @param definition
   *          Definition des Ske
   */
  public DatumJahr(String pid, String definition)
  {

    super(pid, definition);
    this.definition = definition;
    jahrVonBis = new ArrayList<String[]>();
    // _debug = Debug.getLogger();

  }

  /**
   * Prueft den Eintrag auf Gueltikeit
   * 
   * @return true, wenn die Pruefung erfolgreich war
   */
  public boolean pruefeEintrag()
  {

    String[] ergebnisse = new String[12];

    StringTokenizer st = new StringTokenizer(definition, ". ,");

    int i = 0;
    while (st.hasMoreTokens())
    {
      String s = st.nextToken();
      
      ergebnisse[i] = s;
      i++;
    }

    int cnt = i / 4;

    for (int j = 0; j < cnt; j++)
    {

      String[] vonbis = new String[2];

      vonbis[0] = ergebnisse[0 + (j * 4)] + "." + ergebnisse[1 + (j * 4)] + "." + ergebnisse[2 + (j * 4)];
      vonbis[1] = ergebnisse[0 + (j * 4)] + "." + ergebnisse[1 + (j * 4)] + "." + ergebnisse[3 + (j * 4)];

      jahrVonBis.add(vonbis);

    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.Atomar#berechneZustandsWechsel(int)
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.Atomar#berechneZustandsWechsel(java.lang.Long,
   *      java.lang.Long, int)
   */
  public SortedMap<Long, Boolean> berechneZustandsWechsel(Long von, Long bis, int jahr)
  {
    
    // Die Abfrage besitzt eine eigene Zustandsliste
    ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();

    Integer temp = jahr;

    Iterator it1 = jahrVonBis.iterator();
    while (it1.hasNext())
    {
      String s1 = null;
      String s2 = null;

      String[] _jahr = (String[])it1.next();

      long jahre = 0;

      try
      {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();
        

        SimpleDateFormat df_jahr = new SimpleDateFormat("dd.MM.yyyy");       
        
        Date dt_jahr = new Date();
        

        s1 = _jahr[0];
        s2 = _jahr[1];

        if (_jahr[0].contains("*"))
        {

          s1 = _jahr[0].replace("*", temp.toString());

        }

        if (_jahr[1].contains("*"))
        {

          s2 = _jahr[1].replace("*", temp.toString());

        }
        
        Long def1 = df_jahr.parse(s1).getTime();
        Long def2 = df_jahr.parse(s2).getTime();
                       
        // Definition von-bis
        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);
        
        String s11 = df_jahr.format(cal1.getTime());
        String s21 = df_jahr.format(cal2.getTime());
        
        int vonDef = cal1.get(Calendar.YEAR);
        int bisDef = cal2.get(Calendar.YEAR);
        
        cal1.setTimeInMillis(von);
        cal2.setTimeInMillis(bis);

        if (jahr < vonDef)
        {

          return listeZustandsWechselAbfrage.getListeZustandsWechsel();

        }

        if (jahr > bisDef)
        {

          return listeZustandsWechselAbfrage.getListeZustandsWechsel();

        }

        SimpleDateFormat df_zeit = new SimpleDateFormat("HH:mm:ss,SSS");

        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);

        cal1.set(Calendar.YEAR, temp);
        cal2.set(Calendar.YEAR, temp);

        String[] split1 = s1.split("\\.");

        if (split1 != null && split1.length >= 3){
          
          s1 = split1[0] + "." + split1[1] + "." + temp;
          s2 = split1[0] + "." + split1[1] + "." + temp;
          
        }else{
          s1 = df_jahr.format(cal1.getTime());
          s2 = df_jahr.format(cal2.getTime());          
        }
        
        jahre = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);

      }
      catch (ParseException e)
      {
        // TODO Auto-generated catch block
        _debug.config(pid + " + konnte nicht geparst werden" + " " + definition);
      }

      // Anzahl der Jahre wird durchlaufen, aber immer eins mehr wie errechnet wurde
      for (int i = 0; i < jahre + 1; i++)
      {

        String[] _zeit = { "00:00:00,000", " 23:59:59,999" };

        try
        {
          Date dt = new Date();

          Calendar cal = new GregorianCalendar();
          Calendar tmp = new GregorianCalendar();

          SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
          df.setLenient(false);
          
          dt = df.parse(s1 + " " + _zeit[0]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;

          listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), true);

          dt = df.parse(s1 + " " + _zeit[1]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;

          listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), false);
          // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis()+1, false);

        }
        catch (ParseException e)
        {
          // TODO Auto-generated catch block
          //e.printStackTrace();
          _debug.fine(pid  + " Konnte nicht geparst werden");
        }

      }

    }

    return listeZustandsWechselAbfrage.getListeZustandsWechsel();
  }
  
 
  /* (non-Javadoc)
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.Atomar#berechneIntervall(java.lang.Long, java.lang.Long, int)
   */
  public SortedMap<Long, Long> berechneIntervall(Long von, Long bis, int jahr)
  {
    
    // Die Abfrage besitzt eine eigene Zustandsliste
//    ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();
    SortedMap<Long, Long> liste = new TreeMap<Long, Long>();
    
    Integer temp = jahr;
    
    Iterator it1 = jahrVonBis.iterator();
    while (it1.hasNext())
    {
      String s1 = null;
      String s2 = null;
      
      String[] _jahr = (String[])it1.next();
      
      long jahre = 0;
      
      try
      {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();
        
        
        SimpleDateFormat df_jahr = new SimpleDateFormat("dd.MM.yyyy");       
        
        Date dt_jahr = new Date();
        
        
        s1 = _jahr[0];
        s2 = _jahr[1];
        
        if (_jahr[0].contains("*"))
        {
          
          s1 = _jahr[0].replace("*", temp.toString());
          
        }
        
        if (_jahr[1].contains("*"))
        {
          
          s2 = _jahr[1].replace("*", temp.toString());
          
        }
        
        Long def1 = df_jahr.parse(s1).getTime();
        Long def2 = df_jahr.parse(s2).getTime();
        
        // Definition von-bis
        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);
        
        String s11 = df_jahr.format(cal1.getTime());
        String s21 = df_jahr.format(cal2.getTime());
        
        int vonDef = cal1.get(Calendar.YEAR);
        int bisDef = cal2.get(Calendar.YEAR);
        
        cal1.setTimeInMillis(von);
        cal2.setTimeInMillis(bis);
        
        if (jahr < vonDef)
        {
          
          return liste;
          
        }
        
        if (jahr > bisDef)
        {
          
          return liste;
          
        }
        
        SimpleDateFormat df_zeit = new SimpleDateFormat("HH:mm:ss,SSS");
        
        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);
        
        cal1.set(Calendar.YEAR, temp);
        cal2.set(Calendar.YEAR, temp);
        
        String[] split1 = s1.split("\\.");
        
        if (split1 != null && split1.length >= 3){
          
          s1 = split1[0] + "." + split1[1] + "." + temp;
          s2 = split1[0] + "." + split1[1] + "." + temp;
          
        }else{
          s1 = df_jahr.format(cal1.getTime());
          s2 = df_jahr.format(cal2.getTime());          
        }
        
        jahre = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        
      }
      catch (ParseException e)
      {
        // TODO Auto-generated catch block
        _debug.config(pid + " + konnte nicht geparst werden" + " " + definition);
      }
      
      // Anzahl der Jahre wird durchlaufen, aber immer eins mehr wie errechnet wurde
      for (int i = 0; i < jahre + 1; i++)
      {
        
        String[] _zeit = { "00:00:00,000", " 23:59:59,999" };
        
        try
        {
          Date dt = new Date();
          
          Calendar cal = new GregorianCalendar();
          Calendar tmp = new GregorianCalendar();
          
          Long l1 = null;
          Long l2 = null;
          
          SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
          df.setLenient(false);
          
          dt = df.parse(s1 + " " + _zeit[0]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;
          
//          listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), true);
          l1 = cal.getTimeInMillis();
          
          
          dt = df.parse(s1 + " " + _zeit[1]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;
          
          l2 = cal.getTimeInMillis();
//          listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), false);
          // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis()+1, false);
          
          if (l1 != null & l2 != null)
            liste.put(l1, l2);
          else if (l1 == null && l2 != null)
            liste.put(von, l2);
          else if (l1 != null && l2 == null)
            liste.put(l1, bis);
 
        }
        catch (ParseException e)
        {
          // TODO Auto-generated catch block
          //e.printStackTrace();
          _debug.fine(pid  + " Konnte nicht geparst werden");
        }
        
      }
      
    }
    
    return liste;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.Atomar#berechneZustandsWechsel(java.lang.Long,
   *      java.lang.Long, int)
   */
  public SortedMap<Long, Boolean> berechneZustandsWechselZustand(Long von, Long bis, int jahr)
  {
    // Die Abfrage besitzt eine eigene Zustandsliste
    //ListeZustandsWechsel listeZustandsWechselAbfrage = new ListeZustandsWechsel();
    listeZustandsWechsel = new ListeZustandsWechsel();

    Integer temp = jahr;

    Iterator it1 = jahrVonBis.iterator();
    while (it1.hasNext())
    {
      String s1 = null;
      String s2 = null;

      String[] _jahr = (String[])it1.next();

      long jahre = 0;

      try
      {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        SimpleDateFormat df_jahr = new SimpleDateFormat("dd.MM.yyyy");

        Date dt_jahr = new Date();

        s1 = _jahr[0];
        s2 = _jahr[1];

        if (_jahr[0].contains("*"))
        {

          s1 = _jahr[0].replace("*", temp.toString());

        }

        if (_jahr[1].contains("*"))
        {

          s2 = _jahr[1].replace("*", temp.toString());

        }

        Long def1 = df_jahr.parse(s1).getTime();
        Long def2 = df_jahr.parse(s2).getTime();

        // Definition von-bis
        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);

        int vonDef = cal1.get(Calendar.YEAR);
        int bisDef = cal2.get(Calendar.YEAR);

        cal1.setTimeInMillis(von);
        cal2.setTimeInMillis(bis);

        if (jahr < vonDef)
        {

          return listeZustandsWechsel.getListeZustandsWechsel();
          //return listeZustandsWechselAbfrage.getListeZustandsWechsel();

        }

        if (jahr > bisDef)
        {

          return listeZustandsWechsel.getListeZustandsWechsel();
          //return listeZustandsWechselAbfrage.getListeZustandsWechsel();

        }

        SimpleDateFormat df_zeit = new SimpleDateFormat("HH:mm:ss,SSS");

        cal1.setTimeInMillis(def1);
        cal2.setTimeInMillis(def2);

        cal1.set(Calendar.YEAR, temp);
        cal2.set(Calendar.YEAR, temp);

        s1 = df_jahr.format(cal1.getTime());
        s2 = df_jahr.format(cal2.getTime());

        jahre = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);

      }
      catch (ParseException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Anzahl der Jahre wird durchlaufen, aber immer eins mehr wie errechnet wurde
      for (int i = 0; i < jahre + 1; i++)
      {

        String[] _zeit = { "00:00:00,000", " 23:59:59,999" };

        try
        {
          Date dt = new Date();

          Calendar cal = new GregorianCalendar();
          Calendar tmp = new GregorianCalendar();

          SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

          dt = df.parse(s1 + " " + _zeit[0]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;

          listeZustandsWechsel.getListeZustandsWechsel().put(cal.getTimeInMillis(), true);
          //listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), true);

          dt = df.parse(s1 + " " + _zeit[1]);
          cal.setTime(dt);
          tmp = cal;
          tmp.add(Calendar.YEAR, i);
          cal = tmp;

          listeZustandsWechsel.getListeZustandsWechsel().put(cal.getTimeInMillis(), false);
          //listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis(), false);
          // listeZustandsWechselAbfrage.getListeZustandsWechsel().put(cal.getTimeInMillis()+1, false);

        }
        catch (ParseException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }

    }

    return listeZustandsWechsel.getListeZustandsWechsel();
    //return listeZustandsWechselAbfrage.getListeZustandsWechsel();
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see de.bsvrz.vew.syskal.syskal.systemkalendereintrag.Atomar#getDefinition()
   */
  public String getDefinition()
  {

    return definition;

  }
  
  @Override
  protected DatumJahr clone()
  {
    // TODO Auto-generated method stub
    DatumJahr eintrag = null;
    
    eintrag = new DatumJahr(pid, definition);
    
    eintrag.setObjektListeZustandsWechsel(listeZustandsWechsel);
   
    return eintrag;
    
    
  }

  

}

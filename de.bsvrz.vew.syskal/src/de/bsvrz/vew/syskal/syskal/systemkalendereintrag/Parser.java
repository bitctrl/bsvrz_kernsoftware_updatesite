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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.SortedMap;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Die Klasse parst SystemKalenderEintraege aller möglichen Typen
 * 
 * @version $Revision: 1.4 $ / $Date: 2015/06/08 15:13:12 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class Parser
{

  /**
   * Der Debugger, stellt verschiedene Protokollierungsfunktionen zur Verfügung
   */
  private Debug _debug;

  /**
   * Konstruktor der Klasse
   */
  public Parser()
  {
    _debug = Debug.getLogger();
  }

  /**
   * Parst die verschieden Typen von SystemKalenderEintraegen
   * 
   * @param pid
   *          Die Pid
   * @param definition
   *          Die definition des Ske
   * @return Boolean true, wenn ein gültiger Ske einem der definierten Typen entspricht
   * @throws IllegalArgumentException
   *           Wenn einer der uebergebenen Argumente einen null-Wert enthaelt.
   */
  public Boolean parseSystemkalenderEintrag(String pid, String name, String definition) throws Exception
  {

    if (pid == null || definition == null)
      throw new IllegalArgumentException("null value");

    pid = pid.toLowerCase();
    name = name.toLowerCase();
    
    definition = definition.toLowerCase();

    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
    Calendar cal = new GregorianCalendar().getInstance();    
    
    Integer jahr = cal.get(Calendar.YEAR);
    
    String s1 = "01.01." + jahr + " 00:00:00,000";   
   
    jahr += 1;
    
    String s2 = "01.01." + jahr + " 00:00:00,000";
    
    Date d1 = format.parse(s1);
    Date d2 = format.parse(s2);


    if (definition.contains("<") || definition.contains("({"))
    {

      // Eintrag erzeugen und zur Liste der Systemkalendereintraege
      // hinzufuegen
      DatumVonBis dvb = new DatumVonBis(pid, definition);
      dvb.setName(name);

      // Es wird geprueft ob ein neuer SystemKalenderEintrag gesendet wurde
      if (!SystemkalenderArbeiter.getSkeList().containsKey(dvb.getPid()))
      {

        if (dvb.pruefeEintrag())
        {
          
          SortedMap<Long, Boolean> sm = dvb.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
          if (sm!= null)
            dvb.getListeZustandsWechsel().putAll(sm);
          
          SystemkalenderArbeiter.getSkeList().put(pid, dvb);

          return true;
        }

      }
      else
      {
        // SystemKalenderEintrag schon vorhanden
        DatumVonBis tmp = (DatumVonBis)SystemkalenderArbeiter.getSkeList().get(dvb.getPid());
        String alt = tmp.getDefinition();
        String neu = dvb.getDefinition();

        // Es wird geprueft ob eine neue Definition gesendet wurde
        if (alt.equals(neu))
        {
          _debug.fine("Definition ist noch die Alte");
          return true;
        }
        else
        {
          if (dvb.pruefeEintrag())
          {

            SortedMap<Long, Boolean> sm = dvb.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
            if (sm!= null)
              dvb.getListeZustandsWechsel().putAll(sm);

            SystemkalenderArbeiter.getSkeList().put(dvb.getPid(), dvb);

            return true;
          }
        }
      }
    }
    else if (definition.contains(".") && definition.contains(","))
    {       
      DatumJahr dtj = new DatumJahr(pid, definition);
      dtj.setName(name);

      // Es wird geprueft ob ein neuer SystemKalenderEintrag gesendet wurde
      if (!SystemkalenderArbeiter.getSkeList().containsKey(dtj.getPid()))
      {
        if (dtj.pruefeEintrag())
        {

          
          SortedMap<Long, Boolean> sm = dtj.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
 
          if (sm!= null)
            dtj.getListeZustandsWechsel().putAll(sm);
 
          SystemkalenderArbeiter.getSkeList().put(pid, dtj);

          return true;
        }

      }
      else
      {    
        // SystemKalenderEintrag schon vorhanden
        DatumJahr tmp = (DatumJahr)SystemkalenderArbeiter.getSkeList().get(dtj.getPid());
        String alt = tmp.getDefinition();
        String neu = dtj.getDefinition();

        // Es wird geprueft ob eine neue Definition gesendet wurde
        if (alt.equals(neu))
        {
          _debug.fine("Definition ist noch die Alte");
          return true;

        }
        else
        {
          if (dtj.pruefeEintrag())
          {

            SystemkalenderArbeiter.getSkeList().remove(dtj.getPid());

            SortedMap<Long, Boolean> sm = dtj.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
            if (sm!= null)
              dtj.getListeZustandsWechsel().putAll(sm);

            SystemkalenderArbeiter.getSkeList().put(dtj.getPid(), dtj);
            
            return true;
          }
        }
      }

    }
    else if (definition.equalsIgnoreCase("montag") || definition.equalsIgnoreCase("dienstag")
        || definition.equalsIgnoreCase("mittwoch") || definition.equalsIgnoreCase("donnerstag")
        || definition.equalsIgnoreCase("freitag") || definition.equalsIgnoreCase("samstag")
        || definition.equalsIgnoreCase("sonntag") || definition.equalsIgnoreCase("ostersonntag")
        || definition.equalsIgnoreCase("tag"))
    {

      // Eintrag erzeugen und zur Liste der Systemkalendereintraege
      // hinzufuegen
      Atomar atm = new Atomar(pid, definition);
      atm.setName(name);

      //SortedMap<Long, Boolean> sm = atm.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
      SortedMap<Long, Boolean> sm = atm.berechneZustandsWechsel(cal.get(Calendar.YEAR));
      if (sm!= null)
        atm.getListeZustandsWechsel().putAll(sm);
      
      SystemkalenderArbeiter.getSkeList().put(pid, atm);

      return true;

    }
    else if ((definition.contains("und") || definition.contains("oder") || definition.contains("nicht")))
    {

      // Eintrag erzeugen und zur Liste der Systemkalendereintraege
      // hinzufuegen
      LogischeVerknuepfung lvk = new LogischeVerknuepfung(SystemkalenderArbeiter.getSkeList(), pid, definition);
      lvk.setName(name);

      // Es wird geprueft ob ein neuer SystemKalenderEintrag gesendet wurde
      if (!SystemkalenderArbeiter.getSkeList().containsKey(lvk.getPid()))
      {

        if (lvk.pruefeEintrag())
        {

          SortedMap<Long, Boolean> sm = lvk.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
          if (sm!= null)
            lvk.getListeZustandsWechsel().putAll(sm);
          else
            _debug.fine("Liste Zustandswechsel ist null: " + lvk.getPid());
          
          SystemkalenderArbeiter.getSkeList().put(pid, lvk);

          return true;
        }

      }
      else
      {
        // SystemKalenderEintrag schon vorhanden
        LogischeVerknuepfung tmp = (LogischeVerknuepfung)SystemkalenderArbeiter.getSkeList().get(lvk.getPid());
        String alt = tmp.getDefinition();
        String neu = lvk.getDefinition();

        // Es wird geprueft ob eine neue Definition gesendet wurde
        if (alt.equals(neu))
        {
          _debug.fine("Definition ist noch die Alte");
          return true;
        }
        else
        {
          if (lvk.pruefeEintrag())
          {

            SortedMap<Long, Boolean> sm = lvk.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
            if (sm!= null)
              lvk.getListeZustandsWechsel().putAll(sm);
            else
              _debug.fine("Liste Zustandswechsel ist null: " + lvk.getPid());
            
            SystemkalenderArbeiter.getSkeList().put(lvk.getPid(), lvk);

            return true;
          }
        }
      }

    }
    else if ((definition.contains("+") || definition.contains("-")))
    {
      DefinierterEintrag def = new DefinierterEintrag(SystemkalenderArbeiter.getSkeList(), pid, definition);
      def.setName(name);

      // Es wird geprueft ob ein neuer SystemKalenderEintrag gesendet wurde
      if (!SystemkalenderArbeiter.getSkeList().containsKey(def.getPid()))
      {
        if (def.pruefeEintrag())
        {
          
          SortedMap<Long, Boolean> sm = def.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));

          if (sm!= null)
            def.getListeZustandsWechsel().putAll(sm);

          SystemkalenderArbeiter.getSkeList().put(pid, def);

          return true;

        }

      }
      else
      {
        // SystemKalenderEintrag schon vorhanden
        DefinierterEintrag tmp = (DefinierterEintrag)SystemkalenderArbeiter.getSkeList().get(def.getPid());
        String alt = tmp.getDefinition();
        String neu = def.getDefinition();

        // Es wird geprueft ob eine neue Definition gesendet wurde
        if (alt.equals(neu))
        {
          if (def.pruefeEintrag())
            return true;
        }
        else
        {
          if (def.pruefeEintrag())
          {

            SortedMap<Long, Boolean> sm = def.berechneZustandsWechsel(d1.getTime(), d2.getTime(), cal.get(Calendar.YEAR));
            if (sm!= null)
              def.getListeZustandsWechsel().putAll(sm);

            SystemkalenderArbeiter.getSkeList().put(def.getPid(), def);

            return true;
          }
        }
      }

    }else{
           
      Map<String, SystemkalenderEintrag> skeList = SystemkalenderArbeiter.getSkeList();
      
      if (skeList.containsKey("ske." + definition)){
        
        
        SystemkalenderEintrag ske = skeList.get("ske." + definition);
        
        if (ske instanceof DatumJahr)
        {
          DatumJahr dtj = (DatumJahr)ske;                    
          SystemkalenderArbeiter.getSkeList().put(pid, dtj);
          return true;
        }
        else if (ske instanceof DatumVonBis)
        {
          DatumVonBis dvb = (DatumVonBis)ske;
          SystemkalenderArbeiter.getSkeList().put(pid, dvb);
          return true;
        }
        else if (ske instanceof LogischeVerknuepfung)
        {
          LogischeVerknuepfung lvk = (LogischeVerknuepfung)ske;
          SystemkalenderArbeiter.getSkeList().put(pid, lvk);
          return true;
        }
        else if (ske instanceof DefinierterEintrag)
        {
          DefinierterEintrag def = (DefinierterEintrag)ske;
          SystemkalenderArbeiter.getSkeList().put(pid, def);
          return true;

        }else{          
          //return false;
          _debug.error("Eintrag konnte nicht verarbeitet werden" + pid);
          
        }
      }
    }

    // Systemkalendereintrag konnte nicht geparst werden!
    return false;

  }

}

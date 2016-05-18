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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.MutableSetChangeListener;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion.BenachrichtigeEvent;
import de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion.BenachrichtigeFunktion;
import de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion.BenachrichtigeListener;

/**
 * Klasse die Methoden bereitstellt, welche die Systemkalender-Bibliothek benutzt. Es kann damit ein Systemkalender
 * aufgebaut werden!
 * 
 * @version $Revision: 1.6 $ / $Date: 2015/06/08 15:40:17 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class SystemkalenderArbeiter implements ClientReceiverInterface, ClientSenderInterface,
    MutableSetChangeListener, BenachrichtigeListener
{

  /**
   * DebugLogger für Debug-Ausgaben
   */
  private static Debug _debug;

  /**
   * Verbindung zum Datenverteiler
   */
  private static ClientDavInterface _connection;

  /**
   * String fuer eine Attributgruppe
   */
  private String OBJECT_ATG;

  /**
   * String einen Aspekt
   */
  private String OBJECT_ASP;

  /**
   * Attributgruppe
   */
  private AttributeGroup _attributgruppe;

  /**
   * Aspekt
   */
  private Aspect _aspekt;

  /**
   * Datenbeschreibung
   */
  private DataDescription _datenbeschreibung;

  /**
   * Datenmodell
   */
  private DataModel _datenmodell;

  /**
   * Simulationsvariante
   */
  private short _simulationsvariante;

  /**
   * Senderrolle
   */
  private SenderRole _senderrolle;

  /**
   * Empfaengeroption
   */
  private ReceiveOptions _empfaengeroptionen;

  /**
   * Empfaengerrrolle
   */
  private ReceiverRole _empfaengerrolle;

  /**
   * Konfigurationsobjekt
   */
  private ConfigurationObject _configObj;

  /**
   * Liste mit den Pid's der SystemKalenderEintraege
   */
  private Map<String, String[]> parseList = new HashMap<String, String[]>();

  /**
   * Die statische Liste der SystemKalenderEintraege
   */
  private static Map<String, SystemkalenderEintrag> skeList = new HashMap<String, SystemkalenderEintrag>();

  /**
   * Zaehler fuer SystemKalenderEintraege
   */
  private int cntSke;

  /**
   * Zaehler fuer geparste Ske
   */
  private int cntParse;

  /**
   * String fuer den Kalender
   */
  private String _kalender;

  /**
   * String fuer den Kalender
   */
  private Boolean _inInit;

  /**
   * Instanz des Singletons
   */
  private static SystemkalenderArbeiter instance = null;

  private static boolean _used = false;

  private SystemkalenderArbeiter(ClientDavInterface connection, String kalender)
  {
    _connection = connection;
    _debug = Debug.getLogger();
    _kalender = kalender;
    _used = true;

  }

  /**
   * Holt die eine Instanz der Klasse. Die Klasse implementiert das Singleton-Pattern, d.h. es gibt immer nur ein
   * Exemplar der Klasse
   * 
   * @param connection
   * @param kalender
   * @return
   */
  public static SystemkalenderArbeiter getInstance(ClientDavInterface connection, String kalender)
  {

    if (instance == null)
    {
      instance = new SystemkalenderArbeiter(connection, kalender);
    }

    return instance;
  }

  /**
   * Hilfsmethode, die formal nicht zur Systemkalenderbibliothek gehört, aber für deren Test notwendig ist. Hierbei
   * werden die Einträge vom DaV gelesen und ausgewertet.
   * 
   * @return Liste der Einträge als HashMap
   */
  public Map<String, SystemkalenderEintrag> starteSystemKalenderArbeiter()
  {
    if (_used)
    {
      _used = false;

      try
      {
        _inInit = true;

        _datenmodell = _connection.getDataModel();

        _senderrolle = SenderRole.source(); // Rolle der Applikation

        _empfaengeroptionen = ReceiveOptions.normal();

        _empfaengerrolle = ReceiverRole.receiver();

        // Hole SystemKalenderEintraege
        OBJECT_ATG = "atg.systemKalenderEintrag";
        OBJECT_ASP = "asp.parameterSoll";

        _attributgruppe = _connection.getDataModel().getAttributeGroup(OBJECT_ATG);
        _aspekt = _connection.getDataModel().getAspect(OBJECT_ASP);
        _simulationsvariante = 0;
        _datenbeschreibung = new DataDescription(_attributgruppe, _aspekt, _simulationsvariante);

        _configObj = (ConfigurationObject)_datenmodell.getObject(_kalender);

        BenachrichtigeFunktion f = new BenachrichtigeFunktion();
        f.getBenachrichtigeListenerVerwaltung().addBenachrichtigeListener(this);

        MutableSet ms = _configObj.getMutableSet("SystemKalenderEinträge");

        if (ms != null)
        {
          _debug.fine("Listener Menge SystemKalenderEinträge angemeldet");

          ms.addChangeListener(this);
        }
        else
          _debug.error("Menge ist null");

        List objSke = readSystemKalenderEintragMenge();

        cntSke = 0;

        subscribeReceiver(objSke);

        synchronized (this)
        {

          while ((cntSke < objSke.size()))
          {
            this.wait();
          }
        }

        parseArbeiter();

        _inInit = false;

      }
      catch (Exception e)
      {
        e.getStackTrace();
      }

    }

    return skeList;

  }

  /**
   * @return
   * @throws Exception
   */
  private List readSystemKalenderEintragMenge() throws Exception
  {
    ConfigurationObject kalender = (ConfigurationObject)_connection.getDataModel().getObject(_kalender);

    ObjectSet objekte = kalender.getObjectSet("SystemKalenderEinträge");

    List listSke = objekte.getElements();

    return listSke;
  }

  /** Anmeldung zum Senden von Daten */
  private void subscribe(List objlist)
  {
    // Anmelden
    try
    {
      _connection.subscribeSender(this, objlist, _datenbeschreibung, _senderrolle);
      // _debug.config("subscribe.");
    }
    catch (OneSubscriptionPerSendData e)
    {
      // _debug.config("Datenidentifikation ist bereits angemeldet.");
    }
  }

  /** Abmeldung vom Senden der Daten */
  private void unsubscribe(List objlist)
  {
    // Abmelden
    _connection.unsubscribeSender(this, objlist, _datenbeschreibung);
    // _debug.config("unsubscribe.");
  }

  /** Anmeldung zum Empfangen von Daten */
  private void subscribeReceiver(List objlist)
  {
    // Anmelden
    _connection.subscribeReceiver(this, objlist, _datenbeschreibung, _empfaengeroptionen, _empfaengerrolle);

  }

  public void update(ResultData[] results)
  {
    // TODO Auto-generated method stub
    for (ResultData data : results)
    {

      // _debug.config("update: " + data.getObject().getPid() + " : " + data.getData());

      if (data.getDataState() == DataState.NO_SOURCE)
        synchronized (this)
        {
          notify();
        }

      if (data.getObject().isOfType("typ.systemKalenderEintrag"))
      {

        if (data.getData() != null)
        {

          String s = data.getData().getItem("Definition").valueToString();

          int pos = s.indexOf(":=");

          String[] str = new String[2];
          if (pos != -1)
          {

            str[0] = s.substring(0, pos);
            str[1] = s.substring(pos + 2);

          }
          else
          {
            str[0] = s;
            str[1] = s;

          }

          parseList.put(data.getObject().getPid(), str);

          cntSke++;

          if (!_inInit)
          {

            parseArbeiter();

          }

          synchronized (this)
          {
            notify();
          }

        }
        else
        {
          cntSke++;
          // _debug.config("Data ist null");
        }

      }
      else
        _debug.fine(data.getObject().getType().toString() + " ist nicht versorgt!");
    }

  }

  public void update(MutableSet set, SystemObject[] addedObjects, SystemObject[] removedObjects)
  {
    // TODO Auto-generated method stub
    try
    {
      // _debug.config("update Menge: " + set.getName());

      if (addedObjects != null)
      {
        List<SystemObject> list = new ArrayList<SystemObject>();

        for (int i = 0; i < addedObjects.length; i++)
        {

          SystemObject so = addedObjects[i];

          // _debug.config(so.getPid());

          _debug.fine("added - " + addedObjects[i]);

          list.add(so);

        }

        subscribeReceiver(list);
      }

      if (removedObjects != null)
      {
        for (int i = 0; i < removedObjects.length; i++)
        {
          SystemObject so = removedObjects[i];

          // _debug.config(so.getPid());

          if (getSkeList().containsKey(so.getPid()))
          {

            getSkeList().remove(so.getPid());
            // _debug.config(so.getPid() + " : Objekt geloescht!");

          }
          else
          {

            // _debug.config(so.getPid() + " : Objekt nicht verhanden!");

          }

          _debug.fine("removed - " + removedObjects[i]);

        }

      }
    }
    catch (RuntimeException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void dataRequest(SystemObject object, DataDescription dataDescription, byte state)
  {
    // TODO Auto-generated method stub

  }

  public boolean isRequestSupported(SystemObject object, DataDescription dataDescription)
  {
    // TODO Auto-generated method stub
    return false;
  }

  public static Debug getDebug()
  {
    return _debug;
  }

  private static void setDebug(Debug debug)
  {
    _debug = debug;
  }

  /**
   * Holt die liste der Systemkalender Einträge
   * 
   * @return Liste der Einträge
   */
  public static Map<String, SystemkalenderEintrag> getSkeList()
  {
    return skeList;
  }

  /**
   * @param skeList
   */
  private static void setSkeList(Map<String, SystemkalenderEintrag> skeList)
  {
    SystemkalenderArbeiter.skeList = skeList;
  }

  /**
   * Parst einen Systemkalendereintrag durch Benutzung der gleichgnamigen Methode der Klasse Parser
   * 
   * @param pid
   *          die Pid des Eintrags
   * @param name
   * @param definiton
   * 
   * @return true, wenn der Eintrag geparst werden konnten
   */
  public static Boolean parseSystemkalenderEintrag(String pid, String name, String definiton)
  {

    Boolean gueltig = null;

    Parser parser = new Parser();

    try
    {
      String s = null;

      int pos = definiton.indexOf(":=");

      if (pos != -1)
      {
        s = definiton.substring(pos + 2);
      }

      if (s != null)
      {
        gueltig = parser.parseSystemkalenderEintrag(pid, name, s);
      }
      else
      {
        gueltig = parser.parseSystemkalenderEintrag(pid, name, definiton);
      }
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      gueltig = false;
    }

    return gueltig;
  }

  // public void berechneGueltigJetzt(Long jetzt)
  // {
  // // Eintragen der Ereignisse und Berechnen der Anzahl
  //
  // Date d = new Date();
  // d.setTime(jetzt);
  //
  // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
  //
  // for (Map.Entry<String, SystemkalenderEintrag> me : getSkeList().entrySet())
  // {
  // SystemkalenderEintrag ske = me.getValue();
  //
  // String datum;
  //
  // datum = format.format(d);
  //
  // if (ske.isGueltig(jetzt))
  // {
  //
  //
  // }
  // else
  // {
  //
  // }
  // }
  //
  // }
  public SortedMap<String, Boolean> berechneGueltigJetzt(Long jetzt)
  {
    // Eintragen der Ereignisse und Berechnen der Anzahl
    SortedMap<String, Boolean> ergebnis = new TreeMap<String, Boolean>();

    Date d = new Date();
    d.setTime(jetzt);

    for (Map.Entry<String, SystemkalenderEintrag> me : getSkeList().entrySet())
    {
      SystemkalenderEintrag ske = me.getValue();

      if (ske.isGueltig(jetzt))
      {
        ergebnis.put(jetzt + "_" + ske.getPid(), true);
      }
      else
      {
        ergebnis.put(jetzt + "_" + ske.getPid(), false);
      }
    }

    return ergebnis;

  }

  // public void berechneGueltigJetzt(String pid, Long jetzt)
  // {
  // Date d = new Date();
  // d.setTime(jetzt);
  //
  // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
  //
  // if (getSkeList().containsKey(pid))
  // {
  //
  // SystemkalenderEintrag ske = getSkeList().get(pid);
  //
  // String datum;
  //
  // datum = format.format(d);
  //
  // if (ske.isGueltig(jetzt))
  // {
  // }
  // else
  // {
  // }
  // }else
  // _debug.fine(pid + " nicht vorhanden!");
  //
  // }

  public Map.Entry<String, Boolean> berechneGueltigJetzt(String pid, Long jetzt)
  {

    SortedMap<String, Boolean> ergebnis = new TreeMap<String, Boolean>();

    Date d = new Date();
    d.setTime(jetzt);

    if (getSkeList().containsKey(pid))
    {
      SystemkalenderEintrag ske = getSkeList().get(pid);

      if (ske.isGueltig(jetzt))
      {
        ergebnis.put(jetzt + "_" + ske.getPid(), true);
      }
      else
      {
        ergebnis.put(jetzt + "_" + ske.getPid(), false);
      }

    }
    else
      return null;

    Map.Entry<String, Boolean> mapEntry = null;

    for (Map.Entry<String, Boolean> me : ergebnis.entrySet())
    {
      mapEntry = me;
      break;
    }

    return mapEntry;

  }

  public void update(BenachrichtigeEvent e)
  {
    // TODO Auto-generated method stub
    // _debug.config("update: " + e.getMeldung());

  }

  private void parseArbeiter()
  {

    try
    {
      // Parsen!!!
      cntParse = 0;

      int cntRekursiv = 0;

      while (parseList.size() > 0)
      {

        if (cntRekursiv > 20)
        {
          getDebug().fine("Maximale Rekursionstiefe erreicht -> Abbruch des Parsens!!!");
          break;
        }

        // getDebug().config("Rekursionstiefe: " + cntRekursiv);

        List<String> delList = new ArrayList<String>();

        Parser parser = new Parser();

        for (Map.Entry<String, String[]> me : parseList.entrySet())
        {
          if (parser.parseSystemkalenderEintrag(me.getKey(), me.getValue()[0], me.getValue()[1]))
          {
            delList.add(me.getKey());
          }
          else
          {
            getDebug().fine(me.getKey() + " konnte nicht geparst werden");
          }

          cntParse++;

          synchronized (this)
          {
            notify();
          }

        }

        for (String s : delList)
        {

          parseList.remove(s);

        }

        cntRekursiv++;

      }

      if (_inInit)
      {

        synchronized (this)
        {

          while ((cntParse < skeList.size()))
          {
            this.wait();
          }
        }
        _debug.fine("Alle SystemKalenderEintraege geparst!");
      }
      else
        _debug.fine("SystemKalenderEintrag geparst!");
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  // public void berechneGueltigVonBis(Long von, Long bis)
  // {
  //
  // // Sortierte Map mit den zu sendenden Daten wird erstellt
  // SortedMap<String, String[]> sendMap = new TreeMap<String, String[]>();
  //
  // int len = 0;
  //
  // // Eintragen der Ereignisse und Berechnen der Anzahl
  // for (Map.Entry<String, SystemkalenderEintrag> me : getSkeList().entrySet())
  // {
  //
  // SystemkalenderEintrag ske = me.getValue();
  //
  // ListeZustandsWechsel olzw = ske.getObjektListeZustandsWechsel();
  //
  // {
  //
  // SortedMap<Long, Boolean> sm = ske.berecheneZustandsWechselVonBis(von, bis);
  //
  // // Es wurde kein Ereigniswechsel im definierten Zeitraum festgestellt
  // if (sm == null)
  // {
  // //_debug.config(me.getKey() + " : kein Wechsel im definierten Zeitraum");
  // return;
  // }
  //
  // for (Map.Entry<Long, Boolean> meAend : sm.entrySet())
  // {
  //
  // Long time = meAend.getKey();
  //
  // Boolean gueltig = meAend.getValue();
  //
  // String[] sf = new String[2];
  //
  // sf[0] = me.getKey();
  //
  // if (gueltig)
  // sf[1] = "gültig";
  // else
  // sf[1] = "noch gültig";
  //          
  // /******************************************************************/
  // // Calendar cal = new GregorianCalendar();
  // // cal.setTimeInMillis(time);
  // // if ((cal.get(Calendar.MILLISECOND) == 999) && !meAend.getValue())
  // // {
  // // time += 1;
  // // }
  // /******************************************************************/
  //
  // // Es wird ein eindeutiger Schluessel erzeugt
  // String uniqueKey = time + "_" + me.getKey();
  //
  // if(time >= von && time <= bis){
  // sendMap.put(uniqueKey, sf);
  // len++;
  // }
  //
  // }
  //
  // }
  //
  // }
  //
  // for (Map.Entry<String, String[]> entry : sendMap.entrySet())
  // {
  //
  // String tmp = entry.getKey();
  //
  // // Zeitinfo wird aus dem Schluessel extrahiert
  // String millis = tmp.substring(0, tmp.indexOf("_"));
  //
  // Long l = Long.decode(millis);
  //
  // Date d = new Date();
  //
  // d.setTime(l);
  //
  // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
  //
  // String datum = format.format(d);
  //
  //
  // }
  //
  // }

  public SortedMap<String, Boolean> berechneGueltigVonBis(Long von, Long bis)
  {

    SortedMap<String, Boolean> ergebnis = new TreeMap<String, Boolean>();

    for (Map.Entry<String, SystemkalenderEintrag> me : getSkeList().entrySet())
    {
      SystemkalenderEintrag ske = me.getValue();
      SortedMap<Long, Boolean> tmp = ske.berecheneZustandsWechselVonBis(von, bis);
      if (tmp == null)
        return null;

      for (Map.Entry<Long, Boolean> m : tmp.entrySet())
      {
        String s = m.getKey() + "_" + ske.getPid();
        ergebnis.put(s, m.getValue());
      }
    }

    return ergebnis;

  }

  public SortedMap<String, Long> berechneIntervallVonBis(Long von, Long bis)
  {

    SortedMap<String, Long> ergebnis = new TreeMap<String, Long>();

    for (Map.Entry<String, SystemkalenderEintrag> me : getSkeList().entrySet())
    {
      SystemkalenderEintrag ske = me.getValue();
      SortedMap<Long, Long> tmp = ske.berecheneIntervallVonBis(von, bis);
      if (tmp == null)
        return null;

      for (Map.Entry<Long, Long> m : tmp.entrySet())
      {
        String s = m.getKey() + "_" + ske.getPid();
        ergebnis.put(s, m.getValue());
      }
    }

    return ergebnis;

  }

  // public void berechneGueltigVonBis(List<SystemObject> list, Long von, Long bis)
  // {
  //
  // // Sortierte Map mit den zu sendenden Daten wird erstellt
  // SortedMap<String, String[]> sendMap = new TreeMap<String, String[]>();
  //
  // int len = 0;
  //
  // // Eintragen der Ereignisse und Berechnen der Anzahl
  // for (SystemObject so : list)
  // {
  //
  // SystemkalenderEintrag ske = getSkeList().get(so.getPid());
  //
  // if (ske == null)
  // _debug.fine("Ske: " + so.getPid() + "nicht vorhanden");
  //
  // ListeZustandsWechsel olzw = ske.getObjektListeZustandsWechsel();
  //
  // // if (olzw.getListeZustandsWechsel() != null)
  // {
  //
  // SortedMap<Long, Boolean> sm = ske.berecheneZustandsWechselVonBis(von, bis);
  //
  // // Es wurde kein Ereigniswechsel im definierten Zeitraum festgestellt
  // if (sm == null)
  // {
  // _debug.fine(ske.getPid() + " : kein Wechsel im definierten Zeitraum");
  // return;
  // }
  //
  // for (Map.Entry<Long, Boolean> meAend : sm.entrySet())
  // {
  //
  // Long time = meAend.getKey();
  //
  // Boolean gueltig = meAend.getValue();
  //
  // String[] sf = new String[2];
  //
  // sf[0] = ske.getPid();
  //
  // if (gueltig)
  // sf[1] = "gültig";
  // else
  // sf[1] = "noch gültig";
  //          
  // /******************************************************************/
  // // Calendar cal = new GregorianCalendar();
  // // cal.setTimeInMillis(time);
  // // if ((cal.get(Calendar.MILLISECOND) == 999) && !meAend.getValue())
  // // {
  // // time += 1;
  // // }
  // /******************************************************************/
  //
  // // Es wird ein eindeutiger Schluessel erzeugt
  // String uniqueKey = time + "_" + ske.getPid();
  //          
  // if(time >= von && time <= bis){
  // sendMap.put(uniqueKey, sf);
  // len++;
  // }
  // }
  //
  // }
  // }
  //
  // for (Map.Entry<String, String[]> entry : sendMap.entrySet())
  // {
  //
  // String tmp = entry.getKey();
  //
  // // Zeitinfo wird aus dem Schluessel extrahiert
  // String millis = tmp.substring(0, tmp.indexOf("_"));
  //
  // Long l = Long.decode(millis);
  //
  // Date d = new Date();
  //
  // d.setTime(l);
  //
  // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
  //
  // String datum = format.format(d);
  //
  //
  // }
  //
  // }

  public SortedMap<String, Boolean> berechneGueltigVonBis(List<SystemObject> list, Long von, Long bis)
  {

    // Sortierte Map mit den zu sendenden Daten wird erstellt
    SortedMap<String, Boolean> ergebnis = new TreeMap<String, Boolean>();

    // Eintragen der Ereignisse und Berechnen der Anzahl
    for (SystemObject so : list)
    {
      SystemkalenderEintrag ske = getSkeList().get(so.getPid());

      if (ske != null)
      {

        SortedMap<Long, Boolean> tmp = ske.berecheneZustandsWechselVonBis(von, bis);

        if (tmp == null)
          return null;

        for (Map.Entry<Long, Boolean> m : tmp.entrySet())
        {
          String s = m.getKey() + "_" + ske.getPid();
          ergebnis.put(s, m.getValue());
        }

      }
      else
      {
        _debug.error(so.getPid() + " ist nicht der Liste der Systemkalendereinträge");
      }
    }
    
    return ergebnis;
  }

  public SortedMap<String, Long> berechneIntervallVonBis(List<SystemObject> list, Long von, Long bis)
  {

    // Sortierte Map mit den zu sendenden Daten wird erstellt
    SortedMap<String, Long> ergebnis = new TreeMap<String, Long>();

    // Eintragen der Ereignisse und Berechnen der Anzahl
    for (SystemObject so : list)
    {
      SystemkalenderEintrag ske = getSkeList().get(so.getPid());

      if (ske != null)
      {

        SortedMap<Long, Long> tmp = ske.berecheneIntervallVonBis(von, bis);

        if (tmp == null)
          return null;

        for (Map.Entry<Long, Long> m : tmp.entrySet())
        {
          String s = m.getKey() + "_" + ske.getPid();
          ergebnis.put(s, m.getValue());
        }

      }
      else
      {
        _debug.error(so.getPid() + " ist nicht der Liste der Systemkalendereinträge");
      }
    }

    return ergebnis;

  }

  // public void berechneGueltigVonBis(String pid, Long von, Long bis)
  // {
  // pid = pid.toLowerCase();
  //
  // // Sortierte Map mit den zu sendenden Daten wird erstellt
  // SortedMap<String, String[]> sendMap = new TreeMap<String, String[]>();
  //
  // int len = 0;
  //
  // if (getSkeList().containsKey(pid))
  // {
  //
  // Calendar cal1 = new GregorianCalendar();
  // Calendar cal2 = new GregorianCalendar();
  // cal1.setTimeInMillis(von);
  // cal2.setTimeInMillis(bis);
  //
  // SystemkalenderEintrag ske = getSkeList().get(pid);
  //
  // ListeZustandsWechsel olzw = ske.getObjektListeZustandsWechsel();
  //
  // // Besitzt das Ereignis auch eine Aenderungs-Liste?
  // // if (olzw.getListeZustandsWechsel() != null)
  // {
  //
  // SortedMap<Long, Boolean> sm = ske.berecheneZustandsWechselVonBis(von, bis);
  //
  // // Es wurde kein Ereigniswechsel im definierten Zeitraum festgestellt
  // if (sm == null)
  // {
  // _debug.fine(ske.getPid() + " : kein Wechsel im definierten Zeitraum");
  // return;
  // }
  //
  // for (Map.Entry<Long, Boolean> meAend : sm.entrySet())
  // {
  //
  // Long time = meAend.getKey();
  //
  // Boolean gueltig = meAend.getValue();
  //
  // String[] sf = new String[2];
  //
  // //sf[0] = ske.getPid();
  // sf[0] = pid;
  //
  // if (gueltig)
  // sf[1] = "gültig";
  // else
  // sf[1] = "noch gültig";
  //          
  // /******************************************************************/
  // // Calendar cal = new GregorianCalendar();
  // // cal.setTimeInMillis(time);
  // // if ((cal.get(Calendar.MILLISECOND) == 999) && !meAend.getValue())
  // // {
  // // time += 1;
  // // }
  // /******************************************************************/
  //          
  //
  // // Es wird ein eindeutiger Schluessel erzeugt
  // String uniqueKey = time + "_" + ske.getPid();
  //
  // if(time >= von && time <= bis){
  // sendMap.put(uniqueKey, sf);
  // len++;
  // }
  //
  // }
  //
  // }
  // // else
  // {
  // // Ereignis hat keine Aenderungs-Liste
  // // _debug.config(ske.getPid() + " hat keine zeitl. Änderungsliste");
  // }
  //
  // }
  // else
  // {
  //
  // _debug.error(pid + "nicht in SkeListe");
  //
  // }
  //
  // for (Map.Entry<String, String[]> entry : sendMap.entrySet())
  // {
  //
  // String tmp = entry.getKey();
  //
  // // Zeitinfo wird aus dem Schluessel extrahiert
  // String millis = tmp.substring(0, tmp.indexOf("_"));
  //
  // Long l = Long.decode(millis);
  //
  // Date d = new Date();
  //
  // d.setTime(l);
  //
  // SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
  //
  // String datum = format.format(d);
  //
  //
  // }

  public SortedMap<String, Boolean> berechneGueltigVonBis(String pid, Long von, Long bis)
  {
    pid = pid.toLowerCase();

    // Sortierte Map mit den zu sendenden Daten wird erstellt
    SortedMap<String, Boolean> ergebnis = new TreeMap<String, Boolean>();

    if (getSkeList().containsKey(pid))
    {

      SystemkalenderEintrag ske = getSkeList().get(pid);

      SortedMap<Long, Boolean> tmp = ske.berecheneZustandsWechselVonBis(von, bis);

      if (tmp == null)
        return null;

      for (Map.Entry<Long, Boolean> m : tmp.entrySet())
      {
        String s = m.getKey() + "_" + ske.getPid();
        ergebnis.put(s, m.getValue());
      }

    }
    else
    {
      _debug.error(pid + " ist nicht der Liste der Systemkalendereinträge");
    }

    return ergebnis;
  }
  
  public SortedMap<String, Long> berechneIntervallVonBis(String pid, Long von, Long bis)
  {
    pid = pid.toLowerCase();
    
    // Sortierte Map mit den zu sendenden Daten wird erstellt
    SortedMap<String, Long> ergebnis = new TreeMap<String, Long>();
    
    if (getSkeList().containsKey(pid))
    {
      
      SystemkalenderEintrag ske = getSkeList().get(pid);
      
      SortedMap<Long, Long> tmp = ske.berecheneIntervallVonBis(von, bis);
      
      if (tmp == null)
        return null;
      
      for (Map.Entry<Long, Long> m : tmp.entrySet())
      {
        String s = m.getKey() + "_" + ske.getPid();
        ergebnis.put(s, m.getValue());
      }
      
    }
    else
    {
      _debug.error(pid + " ist nicht der Liste der Systemkalendereinträge");
    }
    
    return ergebnis;
  }

}

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.dambach.vewdynobj.VerwaltungDynObj;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion.BenachrichtigeEvent;
import de.bsvrz.vew.syskal.syskal.benachrichtigungsfunktion.BenachrichtigeListener;

/**
 * Kommentar
 * 
 * @version $Revision: 1.2 $ / $Date: 2015/06/08 15:13:12 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public class SystemkalenderTest implements StandardApplication,
    BenachrichtigeListener
{
  /**
   * DebugLogger für Debug-Ausgaben
   */
  private static Debug _debug;

  /**
   * -datenverteiler von Kommandozeile
   */
  private String _dav;

  /**
   * -datenverteiler von Kommandozeile
   */
  private String _kalender;

  /**
   * Verbindung zum Datenverteiler
   */
  public ClientDavInterface _connection;

  /**
   * Verbindung zum Datenverteiler
   */
  private DataModel _datenmodell;

  protected SystemObject systemobjekt;

  protected AttributeGroup _attributgruppe;

  protected Aspect _aspekt;

  protected DataDescription _datenbeschreibung;

  protected short _simulationsvariante;

  protected SenderRole _senderrolle;

  protected ConfigurationArea _konfigBereich;

  protected DynamicObjectType _dynObjTyp;

  protected ConfigurationObject _configObj;

  protected VerwaltungDynObj _verwaltung;

  /**
   * Konstruktor.<br>
   * Öffnen der Log-Datei.
   */
  public SystemkalenderTest()
  {
    try
    {
      Thread.sleep(100);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see sys.funclib.application.StandardApplication#parseArguments(sys.funclib.ArgumentList)
   */
  /**
   * Überschriebene Methode von StandardApplication, die die speziellen Startparameter auswertet.<br>
   * Die Liste der Konfigurationsbereiche wird durch Aufspaltung des übergebenen Strings erstellt und die speziellen
   * Startparameter werden in die Log-Datei eingetragen.
   * 
   * @param argumentList
   *          siehe sys.funclib.application.StandardApplication#parseArguments(sys.funclib.ArgumentList)
   */
  public void parseArguments(ArgumentList argumentList) throws Exception
  {
    _debug = Debug.getLogger();

    _debug.config("argumentList = " + argumentList);

    _dav = argumentList.fetchArgument("-datenverteiler=").asNonEmptyString();
    
    _kalender = argumentList.fetchArgument("-kalender=").asNonEmptyString();

    argumentList.fetchUnusedArguments();
  }

  /*
   * (non-Javadoc)
   * 
   * @see sys.funclib.application.StandardApplication#initialize(stauma.dav.clientside.ClientDavInterface)
   */
  /**
   * Überschriebene Methode von StandardApplication, die die Initialisierung durchführt.<br>
   * Entsprechend dem Argument -layer wird die entsprechende Methode aufgerufen und danach die Log-Datei geschlossen.<br>
   * 
   * @param connection
   *          siehe sys.funclib.application.StandardApplication#initialize(stauma.dav.clientside.ClientDavInterface)
   */
  public void initialize(ClientDavInterface connection) throws Exception
  {

    _connection = connection;

//    BenachrichtigeFunktion f = new BenachrichtigeFunktion();
//    f.getBenachrichtigeListenerVerwaltung().addBenachrichtigeListener(this);

    SystemkalenderArbeiter arbeiter = SystemkalenderArbeiter.getInstance(_connection, _kalender);

    arbeiter.starteSystemKalenderArbeiter();
    
    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

    List<SystemObject> list = new ArrayList<SystemObject>();
    
    list.add(_connection.getDataModel().getObject("ske.ostersonntag"));
    list.add(_connection.getDataModel().getObject("ske.leerzeichen"));
    list.add(_connection.getDataModel().getObject("ske.1"));
    list.add(_connection.getDataModel().getObject("ske._"));
    list.add(_connection.getDataModel().getObject("ske.#"));
    list.add(_connection.getDataModel().getObject("ske.^"));
    list.add(_connection.getDataModel().getObject("ske.º"));
    list.add(_connection.getDataModel().getObject("ske.\\"));
    list.add(_connection.getDataModel().getObject("ske./"));
    
    
    _debug.error("------------------->Test1");
    
    Date start = formatter.parse("01.01.2004 00:00:00,000");
    Date end = formatter.parse("31.12.2004 23:59:59,999");
    arbeiter.berechneGueltigVonBis(list, start.getTime(), end.getTime());
    
    
    _debug.error("------------------->Test2");
    
    start = formatter.parse("01.01.2003 00:00:00,000");
    end = formatter.parse("31.12.2004 23:59:59,999");
    //arbeiter.berechneGueltigVonBis("ske./", start.getTime(), end.getTime());
    
    _debug.error("------------------->Test2");

    start = formatter.parse("01.01.2004 00:00:00,000");
    end = formatter.parse("31.12.2004 23:59:59,999");
    //arbeiter.berechneGueltigVonBis("ske.ostermontag", start.getTime(), end.getTime());

    _debug.error("------------------->Test4");
    
    start = formatter.parse("01.01.2008 00:00:00,000");
    end = formatter.parse("31.12.2008 23:59:59,999");
    //arbeiter.berechneGueltigVonBis("ske.osterdienstag", start.getTime(), end.getTime());
    
    _debug.error("------------------->Test5");        
    Date now = new Date();
    //arbeiter.berechneGueltigJetzt("ske.donnerstag", now.getTime());
    
    _debug.error("------------------->Test6");        

    //arbeiter.berechneGueltigJetzt("ske.freitag", now.getTime());
    
    _debug.error("------------------->Test Ende");
    
    start = formatter.parse("01.01.2008 00:00:00,000");
    end = formatter.parse("31.12.2009 23:59:59,999");
    //arbeiter.berechneGueltigVonBis("ske.falsch", start.getTime(), end.getTime());
    
    _debug.error("------------------->Test Ende");
        
  
  }// public void initialize(ClientDavInterface connection)

  /**
   * Programmeinstieg.<br>
   * 
   * @param arguments
   *          Kommandozeilenargumente
   */
  public static void main(String[] arguments)
  {
    StandardApplicationRunner.run(new SystemkalenderTest(), arguments);

  }

  public void update(BenachrichtigeEvent e)
  {
    // TODO Auto-generated method stub
    _debug.error("update: " + e.getMeldung());

  }

}

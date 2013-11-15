package de.bsvrz.vew.syskal.syskal.verwaltung;

import java.util.Map;

import de.bsvrz.dav.daf.main.Data;


/**
 * Kommentar
 * 
 * @version $Revision: 1.1 $ / $Date: 2009/09/24 12:49:16 $ / ($Author: Pittner $)
 * 
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 * 
 */
public interface Verwaltung
{
  /**
   * Erzeugt ein dynamisches Objekt ohne konfigurierende Daten
   * 
   * @param pid
   *        die Pid
   * @param name
   *        der Name
   * @param set
   *        die Pid der Menge der das dynamische Objekt hinzugefuegt wird
   */
  public void erzeuge(String pid, String name, String set);
  
  /**
   * Erzeugt ein dynamisches Objekt mit konfigurierenden Daten
   * 
   * @param pid
   *        die Pid
   * @param name
   *        der Name
   * @param set
   *        die Pid der Menge der das dynamische Objekt hinzugefuegt wird
   * @param data
   *        ein Feld von konfigurierenden Datensätzen
   */
  public void erzeuge(String pid, String name, String set, Data[] data);

  /**
   * Loescht ein dynamisches Objekt
   * 
   * @param pid
   *        die Pid des zu loeschenden Objekts
   * @param set
   *        die Pid der Menge aus welcher das dynamische Objekt entfernt wird
   */
  public void loesche(String pid, String set);
  
  /**
   * Parametriert ein dynamisches Objekt
   * 
   * @param attribut
   *         das Attributname 
   *     
   * @param definition
   *        der Wert des Attributs als String
   */
  public void parametriere(String attribut, String definition);
  
  /**
   * Parametriert ein dynamisches Objekt
   * 
   * @param map
   *        die Attributliste
   */
  public void parametriere(Map<String, String> map);
  
  /**
   * Parametriert ein dynamisches Objekt
   * 
   * @param data
   *        ein Feld von parametrierenden Daten
   */
  public void parametriere(Data[] data);

}

/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.BackupProgressCallback;
import de.bsvrz.dav.daf.main.config.BackupResult;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SimpleBackupResult;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;

import java.io.File;
import java.util.*;

/**
 * Mit Hilfe dieses Interfaces k�nnen Anfragen an die Konfiguration gestellt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8278 $
 */
public interface ConfigurationRequester {

	/**
	 * Liefert das System-Objekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SystemObject getObject(String pid) throws RequestException;

	/**
	 * Liefert das System-Objekt mit der angegebenen Objekt-ID zur�ck.
	 *
	 * @param id Die Objekt-ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen ID gibt.
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SystemObject getObject(long id) throws RequestException;

	/**
	 * Liefert alle Elemente einer Dynamischen Menge. Durch Angabe der Start- und Endzeitpunkte kann eine Periode angegeben werden, in der die Elemente g�ltig
	 * gewesen sein m�ssen. Sind die beiden Zeitpunkte identisch, dann werden die Elemente zur�ckgegeben, die zum angegebenen Zeitpunkt g�ltig waren bzw. sind. Mit
	 * dem letzten Parameter kann angegeben werden, ob die Elemente w�hrend des gesamten Zeitraumes g�ltig gewesen sein m�ssen oder ob sie innerhalb des Zeitraumes
	 * wenigstens einmal g�ltig gewesen sein mussten.
	 *
	 * @param set                     die Dynamische Menge
	 * @param startTime               Startzeitpunkt des zu betrachtenden Zeitraumes
	 * @param endTime                 Endzeitpunkt des zu betrachtenden Zeitraumes
	 * @param validDuringEntirePeriod ob die Elemente w�hrend des gesamten Zeitraumes g�ltig gewesen sein m�ssen
	 *
	 * @return die geforderten Elemente der Dynamischen Menge
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SystemObject[] getElements(MutableSet set, long startTime, long endTime, boolean validDuringEntirePeriod) throws RequestException;

	/**
	 * Mittels dieser Methode lassen sich Mengen ver�ndern. Es k�nnen Elemente hinzugef�gt und/oder entfernt werden.
	 *
	 * @param set            Menge, in der Elemente hinzugef�gt und/oder gel�scht werden k�nnen
	 * @param addElements    Elemente, die der Menge hinzugef�gt werden sollen. Sollen keine Elemente hinzugef�gt werden, muss <code>null</code> �bergeben werden.
	 * @param removeElements Elemente, die aus der Menge entfernt werden sollen. Sollen keine Elemente entfernt werden, muss <code>null</code> �bergeben werden.
	 *
	 * @throws ConfigurationChangeException Wenn eines der �bergebenen Objekte nicht in die Menge aufgenommen werden konnte und noch nicht in der Menge enthalten
	 *                                      war.
	 * @throws RequestException             Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public void changeElements(ObjectSet set, SystemObject[] addElements, SystemObject[] removeElements) throws ConfigurationChangeException, RequestException;

	/**
	 * Meldet die Dynamische Menge bei der Konfiguration an, um sie immer auf dem neuesten Stand zu halten.
	 *
	 * @param set  die Dynamische Menge
	 * @param time Zeitpunkt, ab wann die Dynamische Menge auf �nderungen angemeldet werden soll.
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	public void subscribe(MutableSet set, long time) throws RequestException;

	/**
	 * Meldet die Dynamische Menge bei der Konfiguration auf �nderungen wieder ab.
	 *
	 * @param set die Dynamische Menge
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	public void unsubscribe(MutableSet set) throws RequestException;

	/**
	 * Liefert f�r die angegebenen Systemobjekte jeweils einen konfigurierenden Datensatz der angegebenen Attributgruppenverwendung zur�ck.
	 *
	 * @param systemObject        Array mit Systemobjekten f�r die Datens�tze abgefragt werden sollen.
	 * @param attributeGroupUsage Attributgruppenverwendung, die Attributgruppe und Aspekt des Datensatzes festlegt.
	 *
	 * @return Array das f�r jedes angefragte Systemobjekt einen entsprechenden konfigurierenden Datensatz enth�lt. Ein Datensatz ist entweder ein Byte-Array das
	 *         mit der Serialisiererversion 2 erzeugt wurde, oder null, wenn f�r das jeweilige Systemobjekt kein Datensatz existiert.
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 * @see de.bsvrz.sys.funclib.dataSerializer.SerializingFactory
	 */
	public byte[][] getConfigurationData(SystemObject[] systemObject, AttributeGroupUsage attributeGroupUsage) throws RequestException;

	/**
	 * Legt an einem Objekt einen konfigurierenden Datensatz fest.
	 *
	 * @param attributeGroupUsage Attributgruppenverwendung
	 * @param systemObject        Objekt, f�r das der konfigurierende Datensatz gedacht ist
	 * @param data                Datensatz, der mit einem Serialisierer(Version2) in ein byte-Array zerlegt wurde. Soll ein Datensatz gel�scht werden, wird ein
	 *                            byte-Array der Gr��e 0 �bergeben.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration konnte den Datensatz nicht dem �bergebenen Systemobjekt hinzuf�gen/entfernen. Ein Grund w�re zum
	 *                                      Beispiel, dass der Konfigurationsverantwortliche nicht die Rechte besitzt Datens�tze an dem Objekt zu �ndern.
	 * @throws RequestException             Fehler bei der �bertragung. Entweder konnte der Auftrag nicht verschickt werden oder die Antwort der Konfiguration
	 *                                      konnte nicht entschl�sselt werden.
	 */
	public void setConfigurationData(AttributeGroupUsage attributeGroupUsage, SystemObject systemObject, byte[] data)
			throws ConfigurationChangeException, RequestException;

	/**
	 * Beauftragt die Konfiguration ein Einmal-Passwort zu erzeugen. Der Auftrag muss verschl�sselt �bertragen werden, das Passwort des Auftraggebers
	 * <code>ordererPassword</code> wird zum verschl�sseln der Nachricht benutzt und darf nicht �bertragen werden. In der verschl�sselten Nachricht ist ausserdem
	 * ein Zufallstext enthalten, der von der Konfiguration angefordert werden muss.
	 * <p/>
	 * Damit dieser Auftrag ausgef�hrt werden kann, muss der Auftraggeber <code>orderer</code> besondere Rechte besitzen. Besitzt der Auftraggeber diese Rechte
	 * nicht, wird der Auftrag zwar zur Konfiguration �bertragen, dort aber nicht ausgef�hrt.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers, dies wird zum verschl�sseln benutzt und darf nicht mit �bertragen werden
	 * @param username              Benutzername, dem ein Einmal-Passwort hinzugef�gt werden soll. Der Name ist zu verschl�sseln.
	 * @param singleServingPassword Einmal-Passwort das dem Benutzer, der mit <code>username</code> identifiziert wird, hinzugef�gt wird. Das Einmal-Passwort darf
	 *                              nur verschl�sselt �bertragen werden
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen ein Einmal-Passwort anzulegen oder das Passwort existiert bereits, usw..
	 *                                    Wird der Auftrag mit den richtigen Parametern �bertragen, wird die Konfiguration ihn ausf�hren.
	 */
	public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
			throws ConfigurationTaskException;

	/**
	 * Ermittelt die Anzahl der noch vorhandenen, g�ltigen Einmal-Passw�rter. Jeder Benutzer kann diese Anzahl f�r seinen eigenen Account ermitteln,
	 * ansonsten sind Admin-Rechte notwendig.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers, dies wird zum verschl�sseln benutzt und darf nicht mit �bertragen werden
	 * @param username              Benutzername, dessen Einmalpassw�rter gepr�ft werden sollen.
	 *
	 * @return Anzahl der noch vorhandenen, g�ltigen Einmal-Passw�rter
	 * 
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren.
	 */
	public int getSingleServingPasswordCount(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * L�scht alle Einmalpassw�rter f�r einen angegebenen Benutzer. Es ist jedem Benutzer
	 * gestattet die Passw�rter seines eigenen Accounts zu l�schen. Soll ein fremdes Benutzerkonto ge�ndert werden, sind Admin-Rechte n�tig.
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword Passwort des Auftraggebers, dies wird zum verschl�sseln benutzt und darf nicht mit �bertragen werden
	 * @param username        Benutzername, dessen Einmalpassw�rter gel�scht werden sollen.
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren.
	 */
	public void clearSingleServingPasswords(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto geh�ren ein Benutzername, ein Passwort und die Rechte des Benutzers.
	 * Alle Informationen, die zu einem Benutzerkonto geh�ren, sind mit dem Passwort des Auftraggebers zu verschl�sseln und verschl�sselt zu �bertragen. Das
	 * Passwort des Auftraggebers wird nicht �bertragen.
	 * <p/>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der bestimmte Rechte besitzt. Besitzt der Benutzer diese Rechte nicht, wird der Auftrag
	 * zur Konfiguration �bertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers
	 * @param ordererPassword      Passwort des Auftraggebers. Das Passwort wird nicht �bertragen.
	 * @param newUsername          Benutzername des neuen Benutzers
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") �bergeben, wird dem Benutzer keine explizite Pid zugewiesen
	 * @param newPassword          Passwort des neuen Benutzers
	 * @param adminRights          true = Der Benutzer besitzt spezielle Rechte; false = Der Benutzer besitzt keine speziellen Rechte
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen einen neuen Benutzer anzulegen. Wird der Auftrag mit den richtigen
	 *                                    Parametern �bertragen, wird die Konfiguration ihn ausf�hren.
	 */
	public void createNewUser(
			String orderer, String ordererPassword, String newUsername, String newUserPid, String newPassword, boolean adminRights, String pidConfigurationArea
	) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto geh�ren ein Benutzername, ein Passwort und die Rechte des
	 * Benutzers.
	 * <p/>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der die Rechte eines Administrators besitzt. Besitzt der Benutzer diese Rechte nicht,
	 * wird der Auftrag zur Konfiguration �bertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param newUsername          Benutzername des neuen Benutzers.
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") �bergeben, wird dem Benutzer keine explizite Pid zugewiesen.
	 * @param newPassword          Passwort des neuen Benutzers.
	 * @param adminRights          <code>true</code>, wenn der neue Benutzer die Rechte eines Administrators besitzen soll; <code>false</code>, wenn der Benutzer
	 *                             keine speziellen Rechte besitzen soll.
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll.
	 * @param data                 Konfigurierende Datens�tze mit den dazugeh�rigen Attributgruppenverwendungen, die f�r das neue Benutzer-Objekt gespeichert
	 *                             werden sollen. Wird null �bergeben, werden keine Konfigurationsdaten angelegt.
	 * @see ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber zum Beispiel nicht die
	 *          n�tigen Rechte besitzen einen neuen Benutzer anzulegen.
	 */
	public void createNewUser(
			String orderer,
			String ordererPassword,
			String newUsername,
			String newUserPid,
			String newPassword,
			boolean adminRights,
			String pidConfigurationArea,
			Collection<DataAndATGUsageInformation> data) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein Benutzerkonto zu l�schen. Alle Informationen, die zu einem Benutzerkonto geh�ren, sind mit dem Passwort des
	 * Auftraggebers zu verschl�sseln und verschl�sselt zu �bertragen.
	 * <p/>
	 * Ein Benutzerkonto kann nur durch einen Benutzer gel�scht werden, der die Rechte eines Administrators besitzt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param userToDelete         Benutzername des zu l�schenden Benutzers.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausf�hren.
	 */
	public void deleteUser(
			String orderer, String ordererPassword, String userToDelete) throws ConfigurationTaskException;

	/**
	 * Pr�ft, ob ein angegebener Benutzer Admin-Rechte besitzt. Jeder Benutzer kann diese Aktion ausf�hren. Zur verschl�sselten �bertragung
	 * des Vorgangs ist dennoch die Angabe eines g�ltigen Benutzernamens und Passworts notwendig.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param username             Name des zu pr�fenden Benutzers
	 *
	 * @return true falls der Benutzer Admin-Rechte hat
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausf�hren.
	 */
	public boolean isUserAdmin(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Pr�ft, ob ein angegebener Benutzername g�ltig ist, d.h. ob er ein zugeordnetes Systemobjekt und einen Eintrag in der Benutzerverwaltung.xml hat. Jeder
	 * Benutzer kann diese Aktion ausf�hren. Zur (verschl�sselten) �bertragung des Vorgangs ist dennoch die Angabe eines g�ltigen Benutzernamens und Passworts
	 * notwendig. Mit dieser Funktion kann gepr�ft werden, ob die Benutzernamen, die {@link #subscribeUserChangeListener(de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener)
	 * } liefert, tats�chlichen zu einem g�ltigen Benutzer geh�ren, da subscribeUserChangeListener nur die Systemobjekte ber�cksichtigt.
	 *
	 * @param orderer         Benutzername des Auftraggebers.
	 * @param ordererPassword Passwort des Auftraggebers.
	 * @param username        Name des zu pr�fenden Benutzers
	 *
	 * @return true falls der Benutzer in der Konfiguration gespeichert ist
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausf�hren.
	 */
	public boolean isUserValid(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Erstellt einen Listener der �nderungen an den Benutzern �berwacht und eine aktuelle Liste aller Benutzer zur�ckgibt
	 * @param listener Objekt, an das R�ckmeldungen gesendet werden sollen. <code>null</code>, wenn nur die Liste der aktuellen Benutzer geholt werden soll.
	 * @return Liste der aktuell vorhandenen Benutzer. Es ist eventuell ratsam, mit isUserValid zu pr�fen, ob die Benutzer tats�chlich in der
	 * Benutzerverwaltung.xml abgelegt sind, da hier nur die Systemobjekte ber�cksichtigt werden.
	 */
	public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Entfernt den mit subscribeUserChangeListener erstellten Listener
	 * @param listener Objekt, and das keien R�ckmeldungen mehr gesendet werden sollen.
	 */
	void unsubscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Beauftragt die Konfiguration die Rechte eines Benutzers zu �ndern. Es wird mit dem Passwort des Benutzers, der den Auftrag erteilt, eine verschl�sselte
	 * Nachricht erzeugt, die den Benutzernamen des Benutzers enth�lt dessen Rechte ge�ndert werden sollen und die neuen Rechte des Benutzers. Das Passwort des
	 * Auftraggebers wird nicht �bertragen.
	 * <p/>
	 * Nur ein Benutzer mit speziellen Rechten darf die Rechte anderer Benutzer �ndern. Besitzt ein Benutzer diese Rechte nicht wird der Auftrag an die
	 * Konfiguration verschickt und dort von der Konfiguration abgelehnt.
	 *
	 * @param orderer         Auftraggeber, der die Rechte eines Benuters �ndern m�chte
	 * @param ordererPassword Passwort des Auftraggebers. Das Passwort wird nicht �bertragen
	 * @param user            Benutzer, dessen Rechte ge�ndert werden sollen
	 * @param adminRights     true = Der Benutzer, der mit <code>user</code> identifiziert wird, erh�lt spezielle Rechte; false = Der Benutzer der mit
	 *                        <code>user</code> identifiziert wird, erh�lt normale Rechte
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen die Rechte eines anderen Benutzers zu �ndern. Wird der Auftrag mit den
	 *                                    richtigen Parametern �bertragen, wird die Konfiguration ihn ausf�hren.
	 */
	public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration das Passwort eines Benutzers zu �ndern. Diese Methode kann von jedem Benutzer aufgerufen werden. Es ist jedem Benutzer
	 * gestattet das Passwort seines Benutzerkontos zu �ndern. Soll das Passwort eines fremden Benutzerkontos ge�ndert werden, sind spezielle Rechte n�tig.
	 * <p/>
	 * Der Benutzername des Benutzerkontos, das ge�ndert werden soll, und das neue Passwort sind verschl�sselt zu �bertragen. Als Schl�ssel wird das
	 * Benutzerpasswort <code>ordererPassword</code> des Auftraggebers <code>orderer</code> benutzt. Das Passwort <code>ordererPassword</code> darf nicht
	 * �bertragen werden.
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword derzeit g�ltiges Passwort, falls der Benutzername <code>orderer</code> und <code>user</code> identisch sind. Sind die Parameter nicht
	 *                        identisch, muss der Benutzer, der mit <code>orderer</code> identifiziert wird, spezielle Rechte besitzen und sein Passwort �bergeben
	 * @param user            Benutzername des Benutzerkontos, dessen Passwort ge�ndert werden soll
	 * @param newUserPassword Neues Passwort des Benutzers, der mit <code>user</code> identifiziert wurde
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen das Passwort eines anderen Benutzers zu �ndern oder das Passwort zum
	 *                                    �ndern ist falsch. Wird der Auftrag mit den richtigen Parametern �bertragen, wird die Konfiguration ihn ausf�hren.
	 */
	public void changeUserPassword(String orderer, String ordererPassword, String user, String newUserPassword) throws ConfigurationTaskException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration alle Konfigurationsbereiche einer Konsistenzpr�fung zu unterziehen (siehe TPuK1-138). Diese
	 * Methode kann unabh�ngig von der Aktivierung {@link #activateConfigurationAreas} oder Freigabe {@link #releaseConfigurationAreasForTransfer} stattfinden.
	 *
	 * @param configurationAreas Definiert alle Konfigurationsbereiche, die einer Konsistenzpr�fung unterzogen werden sollen. Der Bereich wird �ber seine Pid
	 *                           identifiziert, zus�tzlich wird die Version angegeben in der der Konfigurationsbereich gepr�ft werden soll. Alle Bereiche der
	 *                           Konfiguration, die nicht angegeben werden, werden in die Pr�fung einbezogen und zwar mit ihrer aktuellen Version und m�ssen somit
	 *                           nicht expliziet angegeben werden.
	 *
	 * @return Ergebnis der Konsistenzpr�fung
	 *
	 * @throws RequestException Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 */
	public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas) throws RequestException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche zu aktivieren (siehe TPuK1-142). Vor der Aktivierung
	 * wird automatisch eine Konsistenzpr�fung durchgef�hrt. Die Bereiche d�rfen nur aktiviert werden, wenn weder lokale noch Interferenzfehler aufgetreten sind.
	 * <p/>
	 * Verlief die Konsistenzpr�fung positiv(weder lokale noch Interferenzfehler), wird beim n�chsten Neustart der Konfiguration jeder angegebene
	 * Konfigurationsbereich mit der angegebenen Version gestartet.
	 * <p/>
	 * Verlief die Konsistenzpr�fung negativ, wird keiner der angegebenen Konfigurationsbereiche aktiviert.
	 * <p/>
	 * Die Implementierung muss dabei ber�cksichtigen, dass nur Konfigurationsbereiche aktiviert werden d�rfen, f�r die die Konfigurations auch verantwortlich
	 * (Konfiguration ist Konfigurationsverantwortlicher des Bereichs) ist oder aber Konfigurationsbereiche die zur Aktivierung durch andere
	 * Konfigurationsverantwortliche freigegeben sind.
	 * <p/>
	 * Die Version, in der ein Konfigurationsbereich aktiviert werden soll, muss gr��er sein als die derzeit aktuelle Version in der der Konfigurationsbereich
	 * l�uft.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version aktiviert werden sollen
	 *
	 * @return Ergebnis der Konsistenzpr�fung. Die Bereiche werden nur aktiviert, wenn es weder zu einem lokalen noch zu einem Interferenzfehler gekommen ist
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht aktiviert werden konnte. <br>Folgende Gr�nde k�nnen die Ursache sein:
	 *                                      <br> Die Konfiguration wollte einen Konfigurationsbereich in einer Version aktivieren, die noch nicht zur Aktivierung
	 *                                      freigegeben war und f�r den sie nicht der Konfigurationsverantwortliche ist.<br> Ein Konfigurationsbereich l�uft in
	 *                                      einer h�heren Version, als die Version in der er aktiviert werden soll.
	 */
	public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche in den Status "Freigabe zur �bernahme" zu �berf�hren
	 * (siehe TPuK1-143). Bevor die angegebenen Bereiche freigegeben werden, wird automatisch eine Konsistenzpr�fung durchgef�hrt.
	 * <p/>
	 * Verlief die Konsistenzpr�fung positiv(keine lokalen Fehler), werden die angegebenen Konfigurationsbereiche mit der angegebenen Version freigegeben.
	 * <p/>
	 * Verlief die Konsistenzpr�fung negativ, wird keiner der angegebenen Konfigurationsbereiche freigegeben.
	 * <p/>
	 * Die Implementierung muss pr�fen ob die Version, in der der Bereich zur �bernahme freigegeben wird, gr��er als die "aktuelle" Version, die zur �bernahme
	 * freigegeben wurde, ist.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version freigegeben werden sollen
	 *
	 * @return Ergebnis der Konsistenzpr�fung. Die Konfigurationsbereiche werden nur freigegeben, wenn kein lokaler Fehler aufgetreten ist.
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur �bernahme freigegeben werden konnte. <br>Folgende Gr�nde k�nnen
	 *                                      die Ursache sein:<br> Die Konfiguration war nicht der Konfigurationsverantwortliche f�r alle angegebenen Bereiche.<br>
	 *                                      Die aktuelle Version, in der ein Bereich bereits zur �bernahme freigegeben wurde, ist gr��er als die Version, in der
	 *                                      der Bereich freigegeben werden soll.<br>Der Datensatz, der die Versionsnummer speichert konnte nicht ver�ndert oder
	 *                                      geschrieben werden.
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angegebenen Konfigurationsbereiche in den Status "Freigabe zur Aktivierung" zu
	 * �berf�hren. Dadurch k�nnen andere Konfigurationen die Konfigurationsbereiche �bernehmen und diese lokal aktivieren.
	 * <p/>
	 * Es findet keine Konsistenzpr�fung statt, da ein Konfigurationsbereich nur dann f�r andere zur Aktivierung freigegeben werden darf, wenn er bereits lokal
	 * aktiviert {@link #activateConfigurationAreas} wurde.
	 * <p/>
	 * Es werden entweder alle angegebenen Konfigurationsbereiche in der jeweils geforderten Version aktiviert oder keiner.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version f�r andere Konfigurationen freigegeben werden sollen
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur Aktivierung freigegeben werden konnte. <br>Folgende Gr�nde k�nnen
	 *                                      die Ursache sein:<br>Die Konfiguration, die die Konfigurationsbereiche freigeben soll, ist nicht der
	 *                                      Konfigurationsverantwortliche f�r den/die Bereich/e.<br>Ein Bereich soll in einer Version freigegeben werden, der noch
	 *                                      nicht durch den Konfigurationsverantwortlichen der Konfiguration lokal aktiviert wurde {@link
	 *                                      #activateConfigurationAreas}.<br>Ein Bereich soll in einer Version zur Aktivierung freigegeben werden, der bereits in
	 *                                      einer h�heren Version zur Aktivierung freigegeben wurde.<br>Der Datensatz, der die Versionsnummer speichert konnte
	 *                                      nicht ver�ndert oder geschrieben werden.
	 */
	public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Beauftragt die Konfiguration die angegebenen Bereiche in den vorgegebenen Version f�r andere Konfiguraiton zur Aktivierung freizugeben obwohl die Bereiche
	 * durch den Konfigurationsverantwortlichen der Bereiche noch nicht lokal aktiviert wurden.
	 * <p/>
	 * Dieses vorgehen ist nur erlaubt, wenn bei der Konsistenzpr�fung kein lokaler Fehler auftritt.
	 *
	 * @param configurationAreas Bereiche und die jeweiligen Versionen, die f�r andere freigegeben werden sollen.
	 *
	 * @return Ergebnis der Konsistenspr�fung
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Die Konfiguration konnte den Auftrag nicht ausf�hren (mangelnde Rechte, usw.)
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Alle Konfigurationsbereichseintr�ge in der Verwaltungsdatei werden zur�ckgegeben. Hierbei ist es unerheblich, ob der Bereich bereits aktiviert wurde oder
	 * noch zu aktivieren ist.
	 *
	 * @return Eine Map, deren Schl�ssel die Pid des Bereichs und der Wert das Objekt des Konfigurationsbereichs ist.
	 *
	 * @throws RequestException Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 */
	public Map<String, ConfigurationArea> getAllConfigurationAreas() throws RequestException;

	/**
	 * Die Implementierung dieser Methode legt einen neuen Konfigurationsbereich in der lokalen Konfiguration an. Hierf�r wird eine entsprechende
	 * Konfigurationsdatei angelegt, die initial das Objekt des Konfigurationsbereichs enth�lt.
	 * <p/>
	 * Zus�tzlich m�ssen die konfigurierenden Datens�tze f�r den Konfigurationsverantwortlichen und f�r die Versionsnummern (aktivierbare und �bernehmbare Version)
	 * angelegt werden.
	 * <p/>
	 * Ein Eintrag in der Verwaltungsdatei wird ebenfalls eingetragen. Er enth�lt die Pid des Konfigurationsbereichs und die Pfadangabe, wo sich die
	 * Konfigurationsdatei nach Erstellung befindet.
	 *
	 * @param name         Name des neuen Konfigurationsbereichs
	 * @param pid          eindeutige Pid des neuen Konfigurationsbereichs
	 * @param authorityPid die Pid des Konfigurationsverantwortlichen des neuen Konfigurationsbereichs
	 *
	 * @return Das Objekt des neuen Konfigurationsbereichs.
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls kein neuer Konfigurationsbereich angelegt werden konnte.
	 */
	public ConfigurationArea createConfigurationArea(String name, String pid, String authorityPid) throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode importiert die Versorgungsdateien der angegebenen Pids vom angegebenen Verzeichnis in die bestehende Konfiguration.
	 * Dadurch k�nnen neue Konfigurationsbereiche angelegt oder bestehende Bereiche ver�ndert werden.
	 * <p/>
	 * Versorgungsdateien k�nnen auch wieder {@link #exportConfigurationAreas exportiert} werden.
	 *
	 * @param importPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu importierenden Konfigurationsbereiche
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls w�hrend des Imports Fehler auftreten. Nach Korrektur des Fehlers kann der Import wiederholt werden.
	 */
	public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode exportiert aus der bestehenden Konfiguration die Versorgungsdateien zu den angegebenen Pids in das angegebene
	 * Verzeichnis. �nderungen k�nnen an den Versorgungsdateien vorgenommen und diese wieder {@link #importConfigurationAreas importiert} werden.
	 *
	 * @param exportPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu exportierenden Konfigurationsbereiche
	 *
	 * @throws RequestException           Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationTaskException Die angegebenen Bereiche konnte nicht exportiert werden. Dies kann mehrere Gr�nde haben (zu einer Pid wurde kein
	 *                                    Konfigurationsbereich gefunden, eine Versorgungsdatei konnte nicht geschrieben werden, usw.).
	 */
	public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationTaskException;

	/**
	 * Veranlasst die Konfiguration, die Konfigurationsdateien zu sichern. Diese Funktion wartet auf das Beenden des Vorgangs, erlaubt der Konfiguration aber,
	 * andere Aufgaben asynchron durchzuf�hren.
	 *
	 * @param targetDirectory Relatives Zielverzeichnis innerhalb des in der Konfiguration (mit dem Parameter -sicherungsVerzeichnis) festgelegten
	 *                        Sicherungsordners. Wird null oder ein Leerstring angegeben, generiert die Konfiguration aus aktuellem Datum und Uhrzeit einen neuen
	 *                        Pfadnamen.
	 * @param callback        Objekt, an das Statusmeldungen gesendet werden
	 *
	 * @return Objekt, das Informationen �ber das Ergebnis des Sicherungsvorgangs enth�lt
	 *
	 * @throws ConfigurationTaskException Der Backup-Vorgang konnte nicht durchgef�hrt werden, beispielsweise weil das Zielverzeichnis falsch war. Falls das
	 *                                    Sichern einzelner Dateien fehlschl�gt wird keine solche Exception geworfen, stattdessen findet man innerhalb vom callback
	 *                                    eventuelle Fehlschl�ge und BackupResult.getFailed ist gr��er 0.
	 * @throws RequestException           Fehler bei der �bertragung der Anfrage oder beim Empfang von Statusmeldungen der Konfiguration. Achtung: Man kann nicht
	 *                                    zwingend darauf schlie�en, dass der Backupvorgang nicht erfolgreich war, wenn eine Exception geworfen wurde. Wenn w�hrend
	 *                                    des Vorgangs beispielsweise die Verbindung zwischen Datenverteiler und Konfiguration abbricht, wird eine Exception
	 *                                    geworfen, aber die Konfiguration wird den Vorgang vermutlich dennoch korrekt beenden.
	 */
	public BackupResult backupConfigurationFiles(String targetDirectory, BackupProgressCallback callback) throws ConfigurationTaskException, RequestException;

	/**
	 * Fordert f�r den angegebenen Bereich die Version an, in der der Bereich aktiv ist.
	 *
	 * @param configurationArea Bereich, dessen aktive Version angefordert werden soll
	 *
	 * @return Version, in der der Bereich derzeit aktiv ist
	 *
	 * @throws RequestException Fehler bei der Daten�bertragung
	 */
	public short getActiveVersion(ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Neue Version eines Bereichs, die weder zur �bernahme freigegeben noch lokal aktiviert ist. Dies ist die in Bearbeitung befindliche Version, auf die sich
	 * versionierte Konfigurations�nderungen beziehen.
	 *
	 * @param configurationArea Bereich f�r den die Version angefordert werden soll
	 *
	 * @return Nummer der Version, die sich in Bearbeitung befindet.
	 *
	 * @throws RequestException Fehler bei der Daten�bertragung
	 * @see "TPuK1-103"
	 */
	public short getModifiableVersion(ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Anfrage, in der alle Objekte, die zu einer Pid geh�ren, zur�ckgegeben werden, die im angegebenen
	 * Zeitbereich g�ltig waren.
	 *
	 * @param pid       Pid der Objekte, die g�ltig sein sollen
	 * @param startTime Startzeitpunkt
	 * @param endTime   Endzeitpunkt
	 *
	 * @return Alle Objekte, die mit der angegebenen Pid im angegebenen Zeitbereich g�ltig waren. Sind keine Objekte vorhanden, wird eine leere Collection
	 *         zur�ckgegeben.
	 *
	 * @throws RequestException Technischer Fehler bei der �betragung der Anfrage
	 */
	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime) throws RequestException;

	/**
	 * Stellt eine Anfrage an eine entfernte Konfiguration und gibt die Objekte zur�ck, die in einem der angegebenen Konfigurationsbereiche sind und deren
	 * Objekttyp in der angegebenen Objekttyp-Menge ist.
	 *
	 * @param configurationAreas      Bereiche, die gepr�ft werden sollen
	 * @param systemObjectTypes       Objekttypen, die zu ber�cksichtigen sind
	 * @param objectTimeSpecification Definiert den zu betrachtenen Zeitbereich
	 *
	 * @return Objekte, die unter der festgelegten Bediengungen g�ltig waren
	 *
	 * @throws RequestException Technisches Problem bei der Anfrage
	 */
	public Collection<SystemObject> getObjects(
			Collection<ConfigurationArea> configurationAreas,
			Collection<SystemObjectType> systemObjectTypes,
			ObjectTimeSpecification objectTimeSpecification
	) throws RequestException;

	/**
	 * Stellt eine Anfrage an eine entferne Konfiguration und fordert die Objekte an, die direkt von den �bergebenen Typen abstammen. Die Anfrage bezieht sich
	 * dabei nicht auf alle Bereiche, sondern nur auf einen bestimmten. Es werden auch nur die �bergebenen Typen betrachtet, nicht eventuelle Supertypen, von denen
	 * die Typen abgeleitet erben.
	 *
	 * @param area              Bereich, in dem Objekte von den �bergebenen Typen gesucht werden sollen
	 * @param systemObjectTypes Typen, von dem die Objekte sein m�ssen, ableitungen werden nicht ber�cksichtigt
	 * @param timeSpecification Zeit, in der die Objekte g�ltig sein m�ssen
	 *
	 * @return Alle Objekte eines Konfigurationsbereichs f�r die oben genannte Parameter �bereinstimmen. Sind keine Objekte vorhanden, wird eine leere Collection
	 *         zur�ckgegeben.
	 *
	 * @throws RequestException Technisches Problem bei der Anfrage
	 */
	public Collection<SystemObject> getDirectObjects(
			ConfigurationArea area, Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification
	) throws RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag ein Konfigurationsobjekt anzulegen. Die Parameter sind unter {@link
	 * de.bsvrz.dav.daf.main.config.ConfigurationArea#createConfigurationObject} beschrieben.
	 *
	 * @param configurationArea Bereich, in dem das Objekt angelegt werden soll
	 * @param type Typ des neuen Objekts.
	 * @param pid Pid des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @param sets Mengen des neuen Objekts.
	 *
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurden
	 *
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException             Technisches Problem bei der �bertragung Anfrage
	 */
	ConfigurationObject createConfigurationObject(
			ConfigurationArea configurationArea, ConfigurationObjectType type, String pid, String name, List<ObjectSet> sets
	) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag ein dynamisches Objekt anzulegen. Die Parameter sind unter {@link
	 * de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String)} beschrieben.
	 *
	 * @param configurationArea Bereich, in dem das Objekt angelegt werden soll
	 * @param type Typ des neuen Objekts.
	 * @param pid Pid des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 *
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurde
	 *
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException             Technisches Problem bei der �bertragung Anfrage
	 */
	DynamicObject createDynamicObject(ConfigurationArea configurationArea, DynamicObjectType type, String pid, String name)
			throws ConfigurationChangeException, RequestException;

	/**
	 * * Verschickt an eine entfernte Konfiguration einen Auftrag ein dynamisches Objekt anzulegen. Die Parameter sind unter {@link
	 * de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType,String,String,java.util.Collection)}
	*  beschrieben.
	 *
	 * @param configurationArea Konfigurationsbereich in dem das neue Objekt erzeugt werden soll.
	 * @param type Typ des neuen Objekts.
	 * @param pid Pid des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @param data Datens�tze, die am neuen Objekt gespeichert werden sollen.
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurde
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	DynamicObject createDynamicObject(
			ConfigurationArea configurationArea, DynamicObjectType type, String pid, String name, List<DataAndATGUsageInformation> data
	) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag die Kopie eines Objekts zu erstellen.
	 *
	 * @param systemObject   Objekt, von dem eine Kopie erstellt werden soll.
	 * @param substitutePids Enth�lt Pids, die durch andere Pids ersetzt werden (Key = zu ersetzende Pid; Value = Pid, die den Key ersetzt)
	 *
	 * @return Kopie
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @throws RequestException             Technisches Problem bei der �bertragung Anfrage
	 */
	SystemObject duplicate(final SystemObject systemObject, final Map<String, String> substitutePids) throws ConfigurationChangeException, RequestException;

	/**
	 * Beauftragt die Konfiguration alle neuen Objekte eine Konfigurationsbereichs zur�ckzugeben.
	 * <p/>
	 * Ein neues Objekte ist weder ung�ltig noch g�ltig.
	 *
	 * @param configurationArea Bereich, aus dem alle neuen Objekte angefordert werden sollen
	 *
	 * @return Liste, die alle neuen Objekte des angegebenen Bereichs enth�lt. Ist kein Objekt vorhanden, so wird eine leere Liste zur�ckgegeben.
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getNewObjects(final ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Verschickt eine Anfrage an die Konfigration alle Elemente einer Menge zur�ckzugeben, die zu einem Zeitpunkt/Zeitbereich aktiv waren.
	 *
	 * @param set                     Menge, dessen Elemente angefordert werden sollen
	 * @param objectTimeSpecification Legt fest ob ein Zeitpunkt, eine Zeitdauer/Bereich angefragt werden soll.
	 *
	 * @return Alle Elemente, die in dem definierten Zeitbereicht <code>objectTimeSpecification</code>, g�ltig waren.
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getSetElements(ObjectSet set, ObjectTimeSpecification objectTimeSpecification) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente zur�ckzugeben, die in der n�chsten Version g�ltig werden.
	 *
	 * @param set Menge, aus der die Elemente angefordert werden sollen.
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInNextVersion(ObjectSet set) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge, die in der angegebenen Version g�ltig gewesen sind, zur�ckzugeben.
	 *
	 * @param set     Menge, aus der die Elemente angefordert werden sollen.
	 * @param version Version, in der die Elemente g�ltig gewesen sein m�ssen.
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInVersion(ObjectSet set, short version) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge zur�ckzugeben, die im angegebenen Versionsbereich durchg�ngig g�ltig gewesen sind.
	 *
	 * @param set         Menge, aus der die Elemente angefordert werden sollen.
	 * @param fromVersion ab Version
	 * @param toVersion   bis zur Version
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInAllVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge zur�ckzugeben, die im angegebnen Versionsbereich g�ltig waren. Dabei k�nnen die
	 * Elemente auch im angegebnen Bereich ung�ltig geworden sein.
	 *
	 * @param set         Menge, aus der die Elemente angefordert werden sollen.
	 * @param fromVersion ab Version
	 * @param toVersion   bis Version
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der �bertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInAnyVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException;

	/**
	 * @param configurationObject Konfigurationsobjekt, an dem eine Menge gel�scht oder eine neue Menge hinzugef�gt werden soll.
	 * @param set                 Menge, die an dem Objekt gel�scht oder hinzugef�gt werden soll.
	 * @param addSet              true = Die �bergebene Menge soll am Objekt hinzugef�gt werden; false = Die �bergebene Menge soll am Objekt entfernt werden.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration f�hrt den Auftrag nicht aus, weil sie zum Beispiel nicht der Konfigurationsverantwortliche f�r das zu
	 *                                      �ndernde Objekt ist.
	 * @throws RequestException
	 *                                      Technisches Problem bei der �bertragung Anfrage
	 */
	void editConfigurationSet(ConfigurationObject configurationObject, ObjectSet set, boolean addSet) throws ConfigurationChangeException, RequestException;

	/**
	 * Setzt den Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten bzgl. �nderungen der Elemente von dynamischen Mengen bzw. dynamischen
	 * Typen.
	 * @param notifyingMutableCollectionChangeListener Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten.
	 */
	void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener);

	/**
	 * Sendet ein Anmeldungstelgramm f�r Aktualisierungsnachrichten bzgl. �nderungen der Elemente von dynamischen Mengen bzw. dynamischen Typen und nimmt ein
	 * Anworttelegramm mit dem aktuellen Stand der dynamischen Zusammenstellung entgegen.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ, dessen �nderungen von Interesse sind.
	 * @param simVariant Simulationsvariante unter der die dynamische Menge oder der dynamische Typ betrachtet werden soll.
	 * @return Aktuelle Elemente der dynamischen Zusammenstellung zum Zeitpunkt der Anmeldung
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	Collection<SystemObject> subscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException;

	/**
	 * Sendet ein Abmeldungstelgramm zu einer vorher get�tigten Anmeldung f�r Aktualisierungsnachrichten bzgl. �nderungen der Elemente von dynamischen Mengen
	 * bzw. dynamischen Typen.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ der zugeh�rigen Anmeldung.
	 * @param simVariant Simulationsvariante der zugeh�rigen Anmeldung.
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	void unsubscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException;

	int subscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException;

	void unsubscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException;
}

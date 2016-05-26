/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Mit Hilfe dieses Interfaces können Anfragen an die Konfiguration gestellt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConfigurationRequester {

	/**
	 * Initialisiert den Requester
	 * @param localApplicationId Eigene Applikations-ID
	 * @throws CommunicationError
	 */
	void init(long localApplicationId) throws CommunicationError;

	/**
	 * Liefert die System-Objekte mit den angegebenen PIDs zurück.
	 *
	 * @param pid Die permanente ID des System-Objekts (oder mehrere Pids)
	 *
	 * @return Liste mit den gewüschten Systemobjekten in der Reihenfolge der übergebenen PIDs. Objekte, die nicht gefunden wurden, werden als null-Werte zurückgegeben. 
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public List<SystemObject> getObjects(String... pid) throws RequestException;

	/**
	 * Liefert die System-Objekte mit den angegebenen Objekt-IDs zurück.
	 *
	 * @param id Die Objekt-ID des System-Objekts (oder mehrere IDs für mehrere Objekte)
	 *
	 * @return Liste mit den gewüschten Systemobjekten in der Reihenfolge der übergebenen IDs. Objekte, die nicht gefunden wurden, werden als null-Werte zurückgegeben.
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public List<SystemObject> getObjects(long... id) throws RequestException;

	/**
	 * Liefert alle Elemente einer Dynamischen Menge. Durch Angabe der Start- und Endzeitpunkte kann eine Periode angegeben werden, in der die Elemente gültig
	 * gewesen sein müssen. Sind die beiden Zeitpunkte identisch, dann werden die Elemente zurückgegeben, die zum angegebenen Zeitpunkt gültig waren bzw. sind. Mit
	 * dem letzten Parameter kann angegeben werden, ob die Elemente während des gesamten Zeitraumes gültig gewesen sein müssen oder ob sie innerhalb des Zeitraumes
	 * wenigstens einmal gültig gewesen sein mussten.
	 *
	 * @param set                     die Dynamische Menge
	 * @param startTime               Startzeitpunkt des zu betrachtenden Zeitraumes
	 * @param endTime                 Endzeitpunkt des zu betrachtenden Zeitraumes
	 * @param validDuringEntirePeriod ob die Elemente während des gesamten Zeitraumes gültig gewesen sein müssen
	 *
	 * @return die geforderten Elemente der Dynamischen Menge
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SystemObject[] getElements(MutableSet set, long startTime, long endTime, boolean validDuringEntirePeriod) throws RequestException;

	/**
	 * Mittels dieser Methode lassen sich Mengen verändern. Es können Elemente hinzugefügt und/oder entfernt werden.
	 *
	 * @param set            Menge, in der Elemente hinzugefügt und/oder gelöscht werden können
	 * @param addElements    Elemente, die der Menge hinzugefügt werden sollen. Sollen keine Elemente hinzugefügt werden, muss <code>null</code> übergeben werden.
	 * @param removeElements Elemente, die aus der Menge entfernt werden sollen. Sollen keine Elemente entfernt werden, muss <code>null</code> übergeben werden.
	 *
	 * @throws ConfigurationChangeException Wenn eines der übergebenen Objekte nicht in die Menge aufgenommen werden konnte und noch nicht in der Menge enthalten
	 *                                      war.
	 * @throws RequestException             Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public void changeElements(ObjectSet set, SystemObject[] addElements, SystemObject[] removeElements) throws ConfigurationChangeException, RequestException;

	/**
	 * Meldet die Dynamische Menge bei der Konfiguration an, um sie immer auf dem neuesten Stand zu halten.
	 *
	 * @param set  die Dynamische Menge
	 * @param time Zeitpunkt, ab wann die Dynamische Menge auf Änderungen angemeldet werden soll.
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	public void subscribe(MutableSet set, long time) throws RequestException;

	/**
	 * Meldet die Dynamische Menge bei der Konfiguration auf Änderungen wieder ab.
	 *
	 * @param set die Dynamische Menge
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	public void unsubscribe(MutableSet set) throws RequestException;

	/**
	 * Liefert für die angegebenen Systemobjekte jeweils einen konfigurierenden Datensatz der angegebenen Attributgruppenverwendung zurück.
	 *
	 * @param systemObject        Array mit Systemobjekten für die Datensätze abgefragt werden sollen.
	 * @param attributeGroupUsage Attributgruppenverwendung, die Attributgruppe und Aspekt des Datensatzes festlegt.
	 *
	 * @return Array das für jedes angefragte Systemobjekt einen entsprechenden konfigurierenden Datensatz enthält. Ein Datensatz ist entweder ein Byte-Array das
	 *         mit der Serialisiererversion 2 erzeugt wurde, oder null, wenn für das jeweilige Systemobjekt kein Datensatz existiert.
	 *
	 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 * @see de.bsvrz.sys.funclib.dataSerializer.SerializingFactory
	 */
	public byte[][] getConfigurationData(SystemObject[] systemObject, AttributeGroupUsage attributeGroupUsage) throws RequestException;

	/**
	 * Legt an einem Objekt einen konfigurierenden Datensatz fest.
	 *
	 * @param attributeGroupUsage Attributgruppenverwendung
	 * @param systemObject        Objekt, für das der konfigurierende Datensatz gedacht ist
	 * @param data                Datensatz, der mit einem Serialisierer(Version2) in ein byte-Array zerlegt wurde. Soll ein Datensatz gelöscht werden, wird ein
	 *                            byte-Array der Größe 0 übergeben.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration konnte den Datensatz nicht dem übergebenen Systemobjekt hinzufügen/entfernen. Ein Grund wäre zum
	 *                                      Beispiel, dass der Konfigurationsverantwortliche nicht die Rechte besitzt Datensätze an dem Objekt zu ändern.
	 * @throws RequestException             Fehler bei der Übertragung. Entweder konnte der Auftrag nicht verschickt werden oder die Antwort der Konfiguration
	 *                                      konnte nicht entschlüsselt werden.
	 */
	public void setConfigurationData(AttributeGroupUsage attributeGroupUsage, SystemObject systemObject, byte[] data)
			throws ConfigurationChangeException, RequestException;

	/**
	 * Beauftragt die Konfiguration ein Einmal-Passwort zu erzeugen. Der Auftrag muss verschlüsselt übertragen werden, das Passwort des Auftraggebers
	 * <code>ordererPassword</code> wird zum verschlüsseln der Nachricht benutzt und darf nicht übertragen werden. In der verschlüsselten Nachricht ist ausserdem
	 * ein Zufallstext enthalten, der von der Konfiguration angefordert werden muss.
	 * <p>
	 * Damit dieser Auftrag ausgeführt werden kann, muss der Auftraggeber <code>orderer</code> besondere Rechte besitzen. Besitzt der Auftraggeber diese Rechte
	 * nicht, wird der Auftrag zwar zur Konfiguration übertragen, dort aber nicht ausgeführt.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers, dies wird zum verschlüsseln benutzt und darf nicht mit übertragen werden
	 * @param username              Benutzername, dem ein Einmal-Passwort hinzugefügt werden soll. Der Name ist zu verschlüsseln.
	 * @param singleServingPassword Einmal-Passwort das dem Benutzer, der mit <code>username</code> identifiziert wird, hinzugefügt wird. Das Einmal-Passwort darf
	 *                              nur verschlüsselt übertragen werden
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen ein Einmal-Passwort anzulegen oder das Passwort existiert bereits, usw..
	 *                                    Wird der Auftrag mit den richtigen Parametern übertragen, wird die Konfiguration ihn ausführen.
	 */
	public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
			throws ConfigurationTaskException;

	/**
	 * Ermittelt die Anzahl der noch vorhandenen, gültigen Einmal-Passwörter. Jeder Benutzer kann diese Anzahl für seinen eigenen Account ermitteln,
	 * ansonsten sind Admin-Rechte notwendig.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers, dies wird zum verschlüsseln benutzt und darf nicht mit übertragen werden
	 * @param username              Benutzername, dessen Einmalpasswörter geprüft werden sollen.
	 *
	 * @return Anzahl der noch vorhandenen, gültigen Einmal-Passwörter
	 * 
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen.
	 */
	public int getSingleServingPasswordCount(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Löscht alle Einmalpasswörter für einen angegebenen Benutzer. Es ist jedem Benutzer
	 * gestattet die Passwörter seines eigenen Accounts zu löschen. Soll ein fremdes Benutzerkonto geändert werden, sind Admin-Rechte nötig.
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword Passwort des Auftraggebers, dies wird zum verschlüsseln benutzt und darf nicht mit übertragen werden
	 * @param username        Benutzername, dessen Einmalpasswörter gelöscht werden sollen.
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen.
	 */
	public void clearSingleServingPasswords(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto gehören ein Benutzername, ein Passwort und die Rechte des Benutzers.
	 * Alle Informationen, die zu einem Benutzerkonto gehören, sind mit dem Passwort des Auftraggebers zu verschlüsseln und verschlüsselt zu übertragen. Das
	 * Passwort des Auftraggebers wird nicht übertragen.
	 * <p>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der bestimmte Rechte besitzt. Besitzt der Benutzer diese Rechte nicht, wird der Auftrag
	 * zur Konfiguration übertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers
	 * @param ordererPassword      Passwort des Auftraggebers. Das Passwort wird nicht übertragen.
	 * @param newUsername          Benutzername des neuen Benutzers
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") übergeben, wird dem Benutzer keine explizite Pid zugewiesen
	 * @param newPassword          Passwort des neuen Benutzers
	 * @param adminRights          true = Der Benutzer besitzt spezielle Rechte; false = Der Benutzer besitzt keine speziellen Rechte
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen einen neuen Benutzer anzulegen. Wird der Auftrag mit den richtigen
	 *                                    Parametern übertragen, wird die Konfiguration ihn ausführen.
	 */
	public void createNewUser(
			String orderer, String ordererPassword, String newUsername, String newUserPid, String newPassword, boolean adminRights, String pidConfigurationArea
	) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto gehören ein Benutzername, ein Passwort und die Rechte des
	 * Benutzers.
	 * <p>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der die Rechte eines Administrators besitzt. Besitzt der Benutzer diese Rechte nicht,
	 * wird der Auftrag zur Konfiguration übertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param newUsername          Benutzername des neuen Benutzers.
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") übergeben, wird dem Benutzer keine explizite Pid zugewiesen.
	 * @param newPassword          Passwort des neuen Benutzers.
	 * @param adminRights          <code>true</code>, wenn der neue Benutzer die Rechte eines Administrators besitzen soll; <code>false</code>, wenn der Benutzer
	 *                             keine speziellen Rechte besitzen soll.
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll.
	 * @param data                 Konfigurierende Datensätze mit den dazugehörigen Attributgruppenverwendungen, die für das neue Benutzer-Objekt gespeichert
	 *                             werden sollen. Wird null übergeben, werden keine Konfigurationsdaten angelegt.
	 * @see ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber zum Beispiel nicht die
	 *          nötigen Rechte besitzen einen neuen Benutzer anzulegen.
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
	 * Beauftragt die Konfiguration ein Benutzerkonto zu löschen. Alle Informationen, die zu einem Benutzerkonto gehören, sind mit dem Passwort des
	 * Auftraggebers zu verschlüsseln und verschlüsselt zu übertragen.
	 * <p>
	 * Ein Benutzerkonto kann nur durch einen Benutzer gelöscht werden, der die Rechte eines Administrators besitzt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param userToDelete         Benutzername des zu löschenden Benutzers.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausführen.
	 */
	public void deleteUser(
			String orderer, String ordererPassword, String userToDelete) throws ConfigurationTaskException;

	/**
	 * Prüft, ob ein angegebener Benutzer Admin-Rechte besitzt. Jeder Benutzer kann diese Aktion ausführen. Zur verschlüsselten Übertragung
	 * des Vorgangs ist dennoch die Angabe eines gültigen Benutzernamens und Passworts notwendig.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param username             Name des zu prüfenden Benutzers
	 *
	 * @return true falls der Benutzer Admin-Rechte hat
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausführen.
	 */
	public boolean isUserAdmin(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Prüft, ob ein angegebener Benutzername gültig ist, d.h. ob er ein zugeordnetes Systemobjekt und einen Eintrag in der Benutzerverwaltung.xml hat. Jeder
	 * Benutzer kann diese Aktion ausführen. Zur (verschlüsselten) Übertragung des Vorgangs ist dennoch die Angabe eines gültigen Benutzernamens und Passworts
	 * notwendig. Mit dieser Funktion kann geprüft werden, ob die Benutzernamen, die {@link #subscribeUserChangeListener(de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener)
	 * } liefert, tatsächlichen zu einem gültigen Benutzer gehören, da subscribeUserChangeListener nur die Systemobjekte berücksichtigt.
	 *
	 * @param orderer         Benutzername des Auftraggebers.
	 * @param ordererPassword Passwort des Auftraggebers.
	 * @param username        Name des zu prüfenden Benutzers
	 *
	 * @return true falls der Benutzer in der Konfiguration gespeichert ist
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausführen.
	 */
	public boolean isUserValid(String orderer, String ordererPassword, String username) throws ConfigurationTaskException;

	/**
	 * Erstellt einen Listener der Änderungen an den Benutzern überwacht und eine aktuelle Liste aller Benutzer zurückgibt
	 * @param listener Objekt, an das Rückmeldungen gesendet werden sollen. <code>null</code>, wenn nur die Liste der aktuellen Benutzer geholt werden soll.
	 * @return Liste der aktuell vorhandenen Benutzer. Es ist eventuell ratsam, mit isUserValid zu prüfen, ob die Benutzer tatsächlich in der
	 * Benutzerverwaltung.xml abgelegt sind, da hier nur die Systemobjekte berücksichtigt werden.
	 */
	public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Entfernt den mit subscribeUserChangeListener erstellten Listener
	 * @param listener Objekt, and das keien Rückmeldungen mehr gesendet werden sollen.
	 */
	void unsubscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Beauftragt die Konfiguration die Rechte eines Benutzers zu ändern. Es wird mit dem Passwort des Benutzers, der den Auftrag erteilt, eine verschlüsselte
	 * Nachricht erzeugt, die den Benutzernamen des Benutzers enthält dessen Rechte geändert werden sollen und die neuen Rechte des Benutzers. Das Passwort des
	 * Auftraggebers wird nicht übertragen.
	 * <p>
	 * Nur ein Benutzer mit speziellen Rechten darf die Rechte anderer Benutzer ändern. Besitzt ein Benutzer diese Rechte nicht wird der Auftrag an die
	 * Konfiguration verschickt und dort von der Konfiguration abgelehnt.
	 *
	 * @param orderer         Auftraggeber, der die Rechte eines Benuters ändern möchte
	 * @param ordererPassword Passwort des Auftraggebers. Das Passwort wird nicht übertragen
	 * @param user            Benutzer, dessen Rechte geändert werden sollen
	 * @param adminRights     true = Der Benutzer, der mit <code>user</code> identifiziert wird, erhält spezielle Rechte; false = Der Benutzer der mit
	 *                        <code>user</code> identifiziert wird, erhält normale Rechte
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen die Rechte eines anderen Benutzers zu ändern. Wird der Auftrag mit den
	 *                                    richtigen Parametern übertragen, wird die Konfiguration ihn ausführen.
	 */
	public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration das Passwort eines Benutzers zu ändern. Diese Methode kann von jedem Benutzer aufgerufen werden. Es ist jedem Benutzer
	 * gestattet das Passwort seines Benutzerkontos zu ändern. Soll das Passwort eines fremden Benutzerkontos geändert werden, sind spezielle Rechte nötig.
	 * <p>
	 * Der Benutzername des Benutzerkontos, das geändert werden soll, und das neue Passwort sind verschlüsselt zu übertragen. Als Schlüssel wird das
	 * Benutzerpasswort <code>ordererPassword</code> des Auftraggebers <code>orderer</code> benutzt. Das Passwort <code>ordererPassword</code> darf nicht
	 * übertragen werden.
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword derzeit gültiges Passwort, falls der Benutzername <code>orderer</code> und <code>user</code> identisch sind. Sind die Parameter nicht
	 *                        identisch, muss der Benutzer, der mit <code>orderer</code> identifiziert wird, spezielle Rechte besitzen und sein Passwort übergeben
	 * @param user            Benutzername des Benutzerkontos, dessen Passwort geändert werden soll
	 * @param newUserPassword Neues Passwort des Benutzers, der mit <code>user</code> identifiziert wurde
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen das Passwort eines anderen Benutzers zu ändern oder das Passwort zum
	 *                                    ändern ist falsch. Wird der Auftrag mit den richtigen Parametern übertragen, wird die Konfiguration ihn ausführen.
	 */
	public void changeUserPassword(String orderer, String ordererPassword, String user, String newUserPassword) throws ConfigurationTaskException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration alle Konfigurationsbereiche einer Konsistenzprüfung zu unterziehen (siehe TPuK1-138). Diese
	 * Methode kann unabhängig von der Aktivierung {@link #activateConfigurationAreas} oder Freigabe {@link #releaseConfigurationAreasForTransfer} stattfinden.
	 *
	 * @param configurationAreas Definiert alle Konfigurationsbereiche, die einer Konsistenzprüfung unterzogen werden sollen. Der Bereich wird über seine Pid
	 *                           identifiziert, zusätzlich wird die Version angegeben in der der Konfigurationsbereich geprüft werden soll. Alle Bereiche der
	 *                           Konfiguration, die nicht angegeben werden, werden in die Prüfung einbezogen und zwar mit ihrer aktuellen Version und müssen somit
	 *                           nicht expliziet angegeben werden.
	 *
	 * @return Ergebnis der Konsistenzprüfung
	 *
	 * @throws RequestException Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 */
	public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas) throws RequestException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche zu aktivieren (siehe TPuK1-142). Vor der Aktivierung
	 * wird automatisch eine Konsistenzprüfung durchgeführt. Die Bereiche dürfen nur aktiviert werden, wenn weder lokale noch Interferenzfehler aufgetreten sind.
	 * <p>
	 * Verlief die Konsistenzprüfung positiv(weder lokale noch Interferenzfehler), wird beim nächsten Neustart der Konfiguration jeder angegebene
	 * Konfigurationsbereich mit der angegebenen Version gestartet.
	 * <p>
	 * Verlief die Konsistenzprüfung negativ, wird keiner der angegebenen Konfigurationsbereiche aktiviert.
	 * <p>
	 * Die Implementierung muss dabei berücksichtigen, dass nur Konfigurationsbereiche aktiviert werden dürfen, für die die Konfigurations auch verantwortlich
	 * (Konfiguration ist Konfigurationsverantwortlicher des Bereichs) ist oder aber Konfigurationsbereiche die zur Aktivierung durch andere
	 * Konfigurationsverantwortliche freigegeben sind.
	 * <p>
	 * Die Version, in der ein Konfigurationsbereich aktiviert werden soll, muss größer sein als die derzeit aktuelle Version in der der Konfigurationsbereich
	 * läuft.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version aktiviert werden sollen
	 *
	 * @return Ergebnis der Konsistenzprüfung. Die Bereiche werden nur aktiviert, wenn es weder zu einem lokalen noch zu einem Interferenzfehler gekommen ist
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht aktiviert werden konnte. <br>Folgende Gründe können die Ursache sein:
	 *                                      <br> Die Konfiguration wollte einen Konfigurationsbereich in einer Version aktivieren, die noch nicht zur Aktivierung
	 *                                      freigegeben war und für den sie nicht der Konfigurationsverantwortliche ist.<br> Ein Konfigurationsbereich läuft in
	 *                                      einer höheren Version, als die Version in der er aktiviert werden soll.
	 */
	public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angebenen Konfigurationsbereiche in den Status "Freigabe zur Übernahme" zu überführen
	 * (siehe TPuK1-143). Bevor die angegebenen Bereiche freigegeben werden, wird automatisch eine Konsistenzprüfung durchgeführt.
	 * <p>
	 * Verlief die Konsistenzprüfung positiv(keine lokalen Fehler), werden die angegebenen Konfigurationsbereiche mit der angegebenen Version freigegeben.
	 * <p>
	 * Verlief die Konsistenzprüfung negativ, wird keiner der angegebenen Konfigurationsbereiche freigegeben.
	 * <p>
	 * Die Implementierung muss prüfen ob die Version, in der der Bereich zur Übernahme freigegeben wird, größer als die "aktuelle" Version, die zur Übernahme
	 * freigegeben wurde, ist.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version freigegeben werden sollen
	 *
	 * @return Ergebnis der Konsistenzprüfung. Die Konfigurationsbereiche werden nur freigegeben, wenn kein lokaler Fehler aufgetreten ist.
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur Übernahme freigegeben werden konnte. <br>Folgende Gründe können
	 *                                      die Ursache sein:<br> Die Konfiguration war nicht der Konfigurationsverantwortliche für alle angegebenen Bereiche.<br>
	 *                                      Die aktuelle Version, in der ein Bereich bereits zur Übernahme freigegeben wurde, ist größer als die Version, in der
	 *                                      der Bereich freigegeben werden soll.<br>Der Datensatz, der die Versionsnummer speichert konnte nicht verändert oder
	 *                                      geschrieben werden.
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode beauftragt die Konfiguration die angegebenen Konfigurationsbereiche in den Status "Freigabe zur Aktivierung" zu
	 * überführen. Dadurch können andere Konfigurationen die Konfigurationsbereiche übernehmen und diese lokal aktivieren.
	 * <p>
	 * Es findet keine Konsistenzprüfung statt, da ein Konfigurationsbereich nur dann für andere zur Aktivierung freigegeben werden darf, wenn er bereits lokal
	 * aktiviert {@link #activateConfigurationAreas} wurde.
	 * <p>
	 * Es werden entweder alle angegebenen Konfigurationsbereiche in der jeweils geforderten Version aktiviert oder keiner.
	 *
	 * @param configurationAreas Konfigurationsbereiche, die in der jeweiligen Version für andere Konfigurationen freigegeben werden sollen
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls mindestens ein Konfigurationsbereich nicht zur Aktivierung freigegeben werden konnte. <br>Folgende Gründe können
	 *                                      die Ursache sein:<br>Die Konfiguration, die die Konfigurationsbereiche freigeben soll, ist nicht der
	 *                                      Konfigurationsverantwortliche für den/die Bereich/e.<br>Ein Bereich soll in einer Version freigegeben werden, der noch
	 *                                      nicht durch den Konfigurationsverantwortlichen der Konfiguration lokal aktiviert wurde {@link
	 *                                      #activateConfigurationAreas}.<br>Ein Bereich soll in einer Version zur Aktivierung freigegeben werden, der bereits in
	 *                                      einer höheren Version zur Aktivierung freigegeben wurde.<br>Der Datensatz, der die Versionsnummer speichert konnte
	 *                                      nicht verändert oder geschrieben werden.
	 */
	public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Beauftragt die Konfiguration die angegebenen Bereiche in den vorgegebenen Version für andere Konfiguraiton zur Aktivierung freizugeben obwohl die Bereiche
	 * durch den Konfigurationsverantwortlichen der Bereiche noch nicht lokal aktiviert wurden.
	 * <p>
	 * Dieses vorgehen ist nur erlaubt, wenn bei der Konsistenzprüfung kein lokaler Fehler auftritt.
	 *
	 * @param configurationAreas Bereiche und die jeweiligen Versionen, die für andere freigegeben werden sollen.
	 *
	 * @return Ergebnis der Konsistensprüfung
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Die Konfiguration konnte den Auftrag nicht ausführen (mangelnde Rechte, usw.)
	 */
	public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws RequestException, ConfigurationChangeException;

	/**
	 * Alle Konfigurationsbereichseinträge in der Verwaltungsdatei werden zurückgegeben. Hierbei ist es unerheblich, ob der Bereich bereits aktiviert wurde oder
	 * noch zu aktivieren ist.
	 *
	 * @return Eine Map, deren Schlüssel die Pid des Bereichs und der Wert das Objekt des Konfigurationsbereichs ist.
	 *
	 * @throws RequestException Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 */
	public Map<String, ConfigurationArea> getAllConfigurationAreas() throws RequestException;

	/**
	 * Die Implementierung dieser Methode legt einen neuen Konfigurationsbereich in der lokalen Konfiguration an. Hierfür wird eine entsprechende
	 * Konfigurationsdatei angelegt, die initial das Objekt des Konfigurationsbereichs enthält.
	 * <p>
	 * Zusätzlich müssen die konfigurierenden Datensätze für den Konfigurationsverantwortlichen und für die Versionsnummern (aktivierbare und übernehmbare Version)
	 * angelegt werden.
	 * <p>
	 * Ein Eintrag in der Verwaltungsdatei wird ebenfalls eingetragen. Er enthält die Pid des Konfigurationsbereichs und die Pfadangabe, wo sich die
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
	 * Dadurch können neue Konfigurationsbereiche angelegt oder bestehende Bereiche verändert werden.
	 * <p>
	 * Versorgungsdateien können auch wieder {@link #exportConfigurationAreas exportiert} werden.
	 *
	 * @param importPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu importierenden Konfigurationsbereiche
	 *
	 * @throws RequestException             Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationChangeException Falls während des Imports Fehler auftreten. Nach Korrektur des Fehlers kann der Import wiederholt werden.
	 */
	public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationChangeException;

	/**
	 * Die Implementierung dieser Methode exportiert aus der bestehenden Konfiguration die Versorgungsdateien zu den angegebenen Pids in das angegebene
	 * Verzeichnis. Änderungen können an den Versorgungsdateien vorgenommen und diese wieder {@link #importConfigurationAreas importiert} werden.
	 *
	 * @param exportPath            Verzeichnis der Versorgungsdateien
	 * @param configurationAreaPids Pids der zu exportierenden Konfigurationsbereiche
	 *
	 * @throws RequestException           Es ist zu einem Kommunikationsfehler bei der Anfrage gekommen.
	 * @throws ConfigurationTaskException Die angegebenen Bereiche konnte nicht exportiert werden. Dies kann mehrere Gründe haben (zu einer Pid wurde kein
	 *                                    Konfigurationsbereich gefunden, eine Versorgungsdatei konnte nicht geschrieben werden, usw.).
	 */
	public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationTaskException;

	/**
	 * Veranlasst die Konfiguration, die Konfigurationsdateien zu sichern. Diese Funktion wartet auf das Beenden des Vorgangs, erlaubt der
	 * Konfiguration aber, andere Aufgaben asynchron durchzuführen.
	 *
	 * @param targetDirectory        Relatives Zielverzeichnis innerhalb des in der Konfiguration (mit dem Parameter -sicherungsVerzeichnis)
	 *                               festgelegten Sicherungsordners. Wird null oder ein Leerstring angegeben, generiert die Konfiguration aus
	 *                               aktuellem Datum und Uhrzeit einen neuen Pfadnamen.
	 * @param configurationAuthority Verantwortlicher, dessen Konfigurationsdateien gesichert werden sollen
	 * @param callback               Objekt, an das Statusmeldungen gesendet werden
	 * @return Objekt, das Informationen über das Ergebnis des Sicherungsvorgangs enthält
	 * @throws ConfigurationTaskException Der Backup-Vorgang konnte nicht durchgeführt werden, beispielsweise weil das Zielverzeichnis falsch
	 *                                    war. Falls das Sichern einzelner Dateien fehlschlägt wird keine solche Exception geworfen,
	 *                                    stattdessen findet man innerhalb vom callback eventuelle Fehlschläge und BackupResult.getFailed ist
	 *                                    größer 0.
	 * @throws RequestException           Fehler bei der Übertragung der Anfrage oder beim Empfang von Statusmeldungen der Konfiguration.
	 *                                    Achtung: Man kann nicht zwingend darauf schließen, dass der Backupvorgang nicht erfolgreich war, wenn
	 *                                    eine Exception geworfen wurde. Wenn während des Vorgangs beispielsweise die Verbindung zwischen
	 *                                    Datenverteiler und Konfiguration abbricht, wird eine Exception geworfen, aber die Konfiguration wird
	 *                                    den Vorgang vermutlich dennoch korrekt beenden.
	 */
	public BackupResult backupConfigurationFiles(String targetDirectory, final ConfigurationAuthority configurationAuthority, BackupProgressCallback callback) throws ConfigurationTaskException, RequestException;

	/**
	 * Fordert für den angegebenen Bereich die Version an, in der der Bereich aktiv ist.
	 *
	 * @param configurationArea Bereich, dessen aktive Version angefordert werden soll
	 *
	 * @return Version, in der der Bereich derzeit aktiv ist
	 *
	 * @throws RequestException Fehler bei der Datenübertragung
	 */
	public short getActiveVersion(ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Neue Version eines Bereichs, die weder zur Übernahme freigegeben noch lokal aktiviert ist. Dies ist die in Bearbeitung befindliche Version, auf die sich
	 * versionierte Konfigurationsänderungen beziehen.
	 *
	 * @param configurationArea Bereich für den die Version angefordert werden soll
	 *
	 * @return Nummer der Version, die sich in Bearbeitung befindet.
	 *
	 * @throws RequestException Fehler bei der Datenübertragung
	 * @see "TPuK1-103"
	 */
	public short getModifiableVersion(ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Anfrage, in der alle Objekte, die zu einer Pid gehören, zurückgegeben werden, die im angegebenen
	 * Zeitbereich gültig waren.
	 *
	 * @param pid       Pid der Objekte, die gültig sein sollen
	 * @param startTime Startzeitpunkt
	 * @param endTime   Endzeitpunkt
	 *
	 * @return Alle Objekte, die mit der angegebenen Pid im angegebenen Zeitbereich gültig waren. Sind keine Objekte vorhanden, wird eine leere Collection
	 *         zurückgegeben.
	 *
	 * @throws RequestException Technischer Fehler bei der Übetragung der Anfrage
	 */
	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime) throws RequestException;

	/**
	 * Stellt eine Anfrage an eine entfernte Konfiguration und gibt die Objekte zurück, die in einem der angegebenen Konfigurationsbereiche sind und deren
	 * Objekttyp in der angegebenen Objekttyp-Menge ist.
	 *
	 * @param configurationAreas      Bereiche, die geprüft werden sollen
	 * @param systemObjectTypes       Objekttypen, die zu berücksichtigen sind
	 * @param objectTimeSpecification Definiert den zu betrachtenen Zeitbereich
	 *
	 * @return Objekte, die unter der festgelegten Bediengungen gültig waren
	 *
	 * @throws RequestException Technisches Problem bei der Anfrage
	 */
	public Collection<SystemObject> getObjects(
			Collection<ConfigurationArea> configurationAreas,
			Collection<SystemObjectType> systemObjectTypes,
			ObjectTimeSpecification objectTimeSpecification
	) throws RequestException;

	/**
	 * Stellt eine Anfrage an eine entferne Konfiguration und fordert die Objekte an, die direkt von den übergebenen Typen abstammen. Die Anfrage bezieht sich
	 * dabei nicht auf alle Bereiche, sondern nur auf einen bestimmten. Es werden auch nur die übergebenen Typen betrachtet, nicht eventuelle Supertypen, von denen
	 * die Typen abgeleitet erben.
	 *
	 * @param area              Bereich, in dem Objekte von den übergebenen Typen gesucht werden sollen
	 * @param systemObjectTypes Typen, von dem die Objekte sein müssen, ableitungen werden nicht berücksichtigt
	 * @param timeSpecification Zeit, in der die Objekte gültig sein müssen
	 *
	 * @return Alle Objekte eines Konfigurationsbereichs für die oben genannte Parameter übereinstimmen. Sind keine Objekte vorhanden, wird eine leere Collection
	 *         zurückgegeben.
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
	 * @throws RequestException             Technisches Problem bei der Übertragung Anfrage
	 */
	ConfigurationObject createConfigurationObject(
			ConfigurationArea configurationArea, ConfigurationObjectType type, String pid, String name, Collection<? extends ObjectSet> sets
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
	 * @throws RequestException             Technisches Problem bei der Übertragung Anfrage
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
	 * @param data Datensätze, die am neuen Objekt gespeichert werden sollen.
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurde
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	DynamicObject createDynamicObject(
			ConfigurationArea configurationArea, DynamicObjectType type, String pid, String name, List<DataAndATGUsageInformation> data
	) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag die Kopie eines Objekts zu erstellen.
	 *
	 * @param systemObject   Objekt, von dem eine Kopie erstellt werden soll.
	 * @param substitutePids Enthält Pids, die durch andere Pids ersetzt werden (Key = zu ersetzende Pid; Value = Pid, die den Key ersetzt)
	 *
	 * @return Kopie
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @throws RequestException             Technisches Problem bei der Übertragung Anfrage
	 */
	SystemObject duplicate(final SystemObject systemObject, final Map<String, String> substitutePids) throws ConfigurationChangeException, RequestException;

	/**
	 * Beauftragt die Konfiguration alle neuen Objekte eine Konfigurationsbereichs zurückzugeben.
	 * <p>
	 * Ein neues Objekte ist weder ungültig noch gültig.
	 *
	 * @param configurationArea Bereich, aus dem alle neuen Objekte angefordert werden sollen
	 *
	 * @return Liste, die alle neuen Objekte des angegebenen Bereichs enthält. Ist kein Objekt vorhanden, so wird eine leere Liste zurückgegeben.
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getNewObjects(final ConfigurationArea configurationArea) throws RequestException;

	/**
	 * Verschickt eine Anfrage an die Konfigration alle Elemente einer Menge zurückzugeben, die zu einem Zeitpunkt/Zeitbereich aktiv waren.
	 *
	 * @param set                     Menge, dessen Elemente angefordert werden sollen
	 * @param objectTimeSpecification Legt fest ob ein Zeitpunkt, eine Zeitdauer/Bereich angefragt werden soll.
	 *
	 * @return Alle Elemente, die in dem definierten Zeitbereicht <code>objectTimeSpecification</code>, gültig waren.
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getSetElements(ObjectSet set, ObjectTimeSpecification objectTimeSpecification) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente zurückzugeben, die in der nächsten Version gültig werden.
	 *
	 * @param set Menge, aus der die Elemente angefordert werden sollen.
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInNextVersion(ObjectSet set) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge, die in der angegebenen Version gültig gewesen sind, zurückzugeben.
	 *
	 * @param set     Menge, aus der die Elemente angefordert werden sollen.
	 * @param version Version, in der die Elemente gültig gewesen sein müssen.
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInVersion(ObjectSet set, short version) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge zurückzugeben, die im angegebenen Versionsbereich durchgängig gültig gewesen sind.
	 *
	 * @param set         Menge, aus der die Elemente angefordert werden sollen.
	 * @param fromVersion ab Version
	 * @param toVersion   bis zur Version
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInAllVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException;

	/**
	 * Verschickt einen Auftrag an die Konfiguration alle Elemente einer Menge zurückzugeben, die im angegebnen Versionsbereich gültig waren. Dabei können die
	 * Elemente auch im angegebnen Bereich ungültig geworden sein.
	 *
	 * @param set         Menge, aus der die Elemente angefordert werden sollen.
	 * @param fromVersion ab Version
	 * @param toVersion   bis Version
	 *
	 * @return Elemente
	 *
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	Collection<SystemObject> getSetElementsInAnyVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException;

	/**
	 * @param configurationObject Konfigurationsobjekt, an dem eine Menge gelöscht oder eine neue Menge hinzugefügt werden soll.
	 * @param set                 Menge, die an dem Objekt gelöscht oder hinzugefügt werden soll.
	 * @param addSet              true = Die übergebene Menge soll am Objekt hinzugefügt werden; false = Die übergebene Menge soll am Objekt entfernt werden.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration führt den Auftrag nicht aus, weil sie zum Beispiel nicht der Konfigurationsverantwortliche für das zu
	 *                                      ändernde Objekt ist.
	 * @throws RequestException
	 *                                      Technisches Problem bei der Übertragung Anfrage
	 */
	void editConfigurationSet(ConfigurationObject configurationObject, ObjectSet set, boolean addSet) throws ConfigurationChangeException, RequestException;

	/**
	 * Setzt den Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten bzgl. Änderungen der Elemente von dynamischen Mengen bzw. dynamischen
	 * Typen.
	 * @param notifyingMutableCollectionChangeListener Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten.
	 */
	void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener);

	/**
	 * Sendet ein Anmeldungstelgramm für Aktualisierungsnachrichten bzgl. Änderungen der Elemente von dynamischen Mengen bzw. dynamischen Typen und nimmt ein
	 * Anworttelegramm mit dem aktuellen Stand der dynamischen Zusammenstellung entgegen.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ, dessen Änderungen von Interesse sind.
	 * @param simVariant Simulationsvariante unter der die dynamische Menge oder der dynamische Typ betrachtet werden soll.
	 * @return Aktuelle Elemente der dynamischen Zusammenstellung zum Zeitpunkt der Anmeldung
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	Collection<SystemObject> subscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException;

	/**
	 * Sendet ein Abmeldungstelgramm zu einer vorher getätigten Anmeldung für Aktualisierungsnachrichten bzgl. Änderungen der Elemente von dynamischen Mengen
	 * bzw. dynamischen Typen.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ der zugehörigen Anmeldung.
	 * @param simVariant Simulationsvariante der zugehörigen Anmeldung.
	 * @throws RequestException Wenn Fehler beim Versand des Telegramms oder bei der Verarbeitung der entsprechenden Antwort aufgetreten sind.
	 */
	void unsubscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException;

	int subscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException;

	void unsubscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException;

	/**
	 * Gibt alle Objekte des angegebenen Typs zurück
	 * @param type Systemobjekt-Typ
	 * @return Alle Objekte dieses Typs oder beliebiger Subtypen
	 * @throws RequestException Fehler bei Verarbeitung des Telegramms
	 */
	List<SystemObject> getObjectsOfType(SystemObjectType type) throws RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag ein Konfigurationsobjekt anzulegen. Die Parameter sind unter {@link
	 * de.bsvrz.dav.daf.main.config.ConfigurationArea#createConfigurationObject} beschrieben. Das Objekt wird im Default-Bereich angelegt.
	 *
	 * @param type Typ des neuen Objekts.
	 * @param pid Pid des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @param sets Mengen des neuen Objekts.
	 *
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurden
	 *
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException             Technisches Problem bei der Übertragung Anfrage
	 */
	ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, Collection<? extends ObjectSet> sets)
			throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt an eine entfernte Konfiguration einen Auftrag ein dynamisches Objekt anzulegen. Die Parameter sind unter {@link
	 * de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType,String,String,java.util.Collection)}
	 *  beschrieben. Das Objekt wird im Default-Bereich angelegt.
	 *
	 * @param type Typ des neuen Objekts.
	 * @param pid Pid des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @return Objekt, das durch die Konfiguration neu erzeugt wurde
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht anlegen
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt einen Auftrag, ein Objekt zu löschen
	 * @param object Objekt
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht löschen
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	void invalidate(SystemObject object) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt einen Auftrag, ein Objekt wiederherzustellen
	 * @param object Objekt
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht wiederherstellen
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	void revalidate(SystemObject object) throws ConfigurationChangeException, RequestException;

	/**
	 * Verschickt einen Auftrag, ein Objekt umzubenennen
	 * @param object Objekt
	 * @param name neuer Name
	 * @throws ConfigurationChangeException Die Konfiguration kann das Objekt nicht umbenennen
	 * @throws RequestException Technisches Problem bei der Übertragung Anfrage
	 */
	void setName(SystemObject object, String name) throws ConfigurationChangeException, RequestException;
}

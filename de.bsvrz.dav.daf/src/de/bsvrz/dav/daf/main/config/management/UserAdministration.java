/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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

package de.bsvrz.dav.daf.main.config.management;

import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.Collection;
import java.util.List;

/**
 * Die Implementation dieses Interfaces erlaubt es, die Benutzer der Kernsoftware zu verwalten. Dies beinhaltet Aktionen wie:<br> <ul> <li>Anlegen neuer
 * Benutzer</li> <li>Ändern von Passwörtern</li> <li>Ändern der Rechte, die ein Benutzer besitzt</li> <li>Erstellung von Einmal-Passwörtern</li> </ul>
 * <p>
 * Alle beschriebenen Aktionen setzen dabei die nötigen Rechte des Benutzers voraus, der die Aktion auslöst. Sind die nötigen Rechte nicht vorhanden, so wird
 * die Aktion nicht durchgeführt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface UserAdministration {

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto gehören ein Benutzername, ein Passwort und die Rechte des Benutzers.
	 * <p>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der die Rechte eines Administrators besitzt. Besitzt der Benutzer diese Rechte nicht, wird
	 * der Auftrag zur Konfiguration übertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param newUsername          Benutzername des neuen Benutzers.
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") übergeben, wird dem Benutzer keine explizite Pid zugewiesen.
	 * @param newPassword          Passwort des neuen Benutzers.
	 * @param adminRights          <code>true</code>, wenn der neue Benutzer die Rechte eines Administrators besitzen soll; <code>false</code>, wenn der Benutzer
	 *                             keine speziellen Rechte besitzen soll.
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber zum Beispiel nicht die
	 *          nötigen Rechte besitzen einen neuen Benutzer anzulegen.
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
	 *                             werden sollen. Oder <code>null</code> falls keine Datensätze angelegt werden sollen.
	 *
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
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
	 * Prüft, ob ein angegebener Benutzer Admin-Rechte besitzt. Jeder Benutzer kann diese Aktion ausführen.
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
	 * Beauftragt die Konfiguration die Rechte eines Benutzers zu ändern.
	 * <p>
	 * Nur ein Benutzer mit den Rechten eines Administrators darf die Rechte anderer Benutzer ändern. Besitzt ein Benutzer diese Rechte nicht wird der Auftrag an
	 * die Konfiguration verschickt und dort von der Konfiguration abgelehnt.
	 *
	 * @param orderer         Auftraggeber, der die Rechte eines Benuters ändern möchte.
	 * @param ordererPassword Passwort des Auftraggebers.
	 * @param user            Benutzer, dessen Rechte geändert werden sollen.
	 * @param adminRights     <code>true</code>, wenn der Benutzer, der mit <code>user</code> identifiziert wird, die Rechte eines Administrators erhalten soll;
	 *                        <code>false</code>, wenn der Benutzer, der mit <code>user</code> identifiziert wird, keine speziellen Rechte erhalten soll.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen die Rechte eines anderen Benutzers zu ändern.
	 */
	public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration das Passwort eines Benutzers zu ändern. Diese Methode kann von jedem Benutzer aufgerufen werden. Es ist jedem Benutzer
	 * gestattet das Passwort seines Benutzerkontos zu ändern. Soll das Passwort eines fremden Benutzerkontos geändert werden, sind die Rechte eines Administrators
	 * nötig.
	 * <p>
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword Derzeit gültiges Passwort, falls der Benutzername <code>orderer</code> und <code>user</code> identisch sind. Sind die Parameter nicht
	 *                        identisch, muss der Benutzer, der mit <code>orderer</code> identifiziert wird, die Rechte eines Administrators besitzen und sein
	 *                        Passwort übergeben
	 * @param user            Benutzername des Benutzerkontos, dessen Passwort geändert werden soll
	 * @param newUserPassword Neues Passwort des Benutzers, der mit <code>user</code> identifiziert wurde
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen das Passwort eines anderen Benutzers zu ändern oder das Passwort zum
	 *                                    ändern ist falsch.
	 */
	public void changeUserPassword(String orderer, String ordererPassword, String user, String newUserPassword) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein Einmal-Passwort zu erzeugen und es einem Benutzer zu zuordnen.
	 * <p>
	 * Damit dieser Auftrag ausgeführt werden kann, muss der Auftraggeber <code>orderer</code> die Rechte eines Administrators besitzen. Besitzt der Auftraggeber
	 * diese Rechte nicht, wird der Auftrag zwar zur Konfiguration übertragen, dort aber abgelehnt.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers.
	 * @param username              Benutzername, dem ein Einmal-Passwort hinzugefügt werden soll.
	 * @param singleServingPassword Einmal-Passwort das dem Benutzer, der mit <code>username</code> identifiziert wird, hinzugefügt wird.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausführen, weil die übergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die nötigen Rechte besitzen ein Einmal-Passwort anzulegen oder das Passwort existierte bereits, usw..
	 */
	public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
			throws ConfigurationTaskException;

	/**
	 * Ermittelt die Anzahl der noch vorhandenen, gültigen Einmal-Passwörter. Jeder Benutzer kann diese Anzahl für seinen eigenen Account ermitteln, 
	 * für fremde Accounts sind Admin-Rechte notwendig.
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
	 * Erstellt einen Listener der Änderungen an den Benutzern überwacht. Gibt eine aktuelle Liste aller Benutzer zurück.
	 * @param listener Objekt, an das Rückmeldungen gesendet werden sollen. <code>null</code>, wenn nur die Liste der aktuellen Benutzer geholt werden soll.
	 * @return Liste der aktuell vorhandenen Benutzer. Es ist eventuell ratsam, mit isUserValid zu prüfen, ob die Benutzer tatsächlich in der
	 * Benutzerverwaltung.xml abgelegt sind, da hier nur die SystemObjekte berücksichtigt werden.
	 */
	public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Entfernt den mit subscribeUserChangeListener erstellten Listener
	 * @param listener Objekt, and das keine Rückmeldungen mehr gesendet werden sollen.
	 */
	public void unsubscribeUserChangeListener(MutableCollectionChangeListener listener);
}

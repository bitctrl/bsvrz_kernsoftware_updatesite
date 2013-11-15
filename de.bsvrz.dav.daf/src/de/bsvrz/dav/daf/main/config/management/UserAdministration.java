/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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

package de.bsvrz.dav.daf.main.config.management;

import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.Collection;
import java.util.List;

/**
 * Die Implementation dieses Interfaces erlaubt es, die Benutzer der Kernsoftware zu verwalten. Dies beinhaltet Aktionen wie:<br> <ul> <li>Anlegen neuer
 * Benutzer</li> <li>�ndern von Passw�rtern</li> <li>�ndern der Rechte, die ein Benutzer besitzt</li> <li>Erstellung von Einmal-Passw�rtern</li> </ul>
 * <p/>
 * Alle beschriebenen Aktionen setzen dabei die n�tigen Rechte des Benutzers voraus, der die Aktion ausl�st. Sind die n�tigen Rechte nicht vorhanden, so wird
 * die Aktion nicht durchgef�hrt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8238 $
 */
public interface UserAdministration {

	/**
	 * Beauftragt die Konfiguration ein neues Benutzerkonto anzulegen. Zu einem Benutzerkonto geh�ren ein Benutzername, ein Passwort und die Rechte des Benutzers.
	 * <p/>
	 * Ein neuer Benutzer kann nur durch einen Benutzer angelegt werden, der die Rechte eines Administrators besitzt. Besitzt der Benutzer diese Rechte nicht, wird
	 * der Auftrag zur Konfiguration �bertragen und dort abgelehnt.
	 *
	 * @param orderer              Benutzername des Auftraggebers.
	 * @param ordererPassword      Passwort des Auftraggebers.
	 * @param newUsername          Benutzername des neuen Benutzers.
	 * @param newUserPid           Pid des neuen Benutzers. Wird der Leerstring ("") �bergeben, wird dem Benutzer keine explizite Pid zugewiesen.
	 * @param newPassword          Passwort des neuen Benutzers.
	 * @param adminRights          <code>true</code>, wenn der neue Benutzer die Rechte eines Administrators besitzen soll; <code>false</code>, wenn der Benutzer
	 *                             keine speziellen Rechte besitzen soll.
	 * @param pidConfigurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber zum Beispiel nicht die
	 *          n�tigen Rechte besitzen einen neuen Benutzer anzulegen.
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
	 *                             werden sollen. Oder <code>null</code> falls keine Datens�tze angelegt werden sollen.
	 *
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
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
	 * Pr�ft, ob ein angegebener Benutzer Admin-Rechte besitzt. Jeder Benutzer kann diese Aktion ausf�hren.
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
	 * Beauftragt die Konfiguration die Rechte eines Benutzers zu �ndern.
	 * <p/>
	 * Nur ein Benutzer mit den Rechten eines Administrators darf die Rechte anderer Benutzer �ndern. Besitzt ein Benutzer diese Rechte nicht wird der Auftrag an
	 * die Konfiguration verschickt und dort von der Konfiguration abgelehnt.
	 *
	 * @param orderer         Auftraggeber, der die Rechte eines Benuters �ndern m�chte.
	 * @param ordererPassword Passwort des Auftraggebers.
	 * @param user            Benutzer, dessen Rechte ge�ndert werden sollen.
	 * @param adminRights     <code>true</code>, wenn der Benutzer, der mit <code>user</code> identifiziert wird, die Rechte eines Administrators erhalten soll;
	 *                        <code>false</code>, wenn der Benutzer, der mit <code>user</code> identifiziert wird, keine speziellen Rechte erhalten soll.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen die Rechte eines anderen Benutzers zu �ndern.
	 */
	public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration das Passwort eines Benutzers zu �ndern. Diese Methode kann von jedem Benutzer aufgerufen werden. Es ist jedem Benutzer
	 * gestattet das Passwort seines Benutzerkontos zu �ndern. Soll das Passwort eines fremden Benutzerkontos ge�ndert werden, sind die Rechte eines Administrators
	 * n�tig.
	 * <p/>
	 *
	 * @param orderer         Benutzername des Auftraggebers
	 * @param ordererPassword Derzeit g�ltiges Passwort, falls der Benutzername <code>orderer</code> und <code>user</code> identisch sind. Sind die Parameter nicht
	 *                        identisch, muss der Benutzer, der mit <code>orderer</code> identifiziert wird, die Rechte eines Administrators besitzen und sein
	 *                        Passwort �bergeben
	 * @param user            Benutzername des Benutzerkontos, dessen Passwort ge�ndert werden soll
	 * @param newUserPassword Neues Passwort des Benutzers, der mit <code>user</code> identifiziert wurde
	 *
	 * @throws ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen das Passwort eines anderen Benutzers zu �ndern oder das Passwort zum
	 *                                    �ndern ist falsch.
	 */
	public void changeUserPassword(String orderer, String ordererPassword, String user, String newUserPassword) throws ConfigurationTaskException;

	/**
	 * Beauftragt die Konfiguration ein Einmal-Passwort zu erzeugen und es einem Benutzer zu zuordnen.
	 * <p/>
	 * Damit dieser Auftrag ausgef�hrt werden kann, muss der Auftraggeber <code>orderer</code> die Rechte eines Administrators besitzen. Besitzt der Auftraggeber
	 * diese Rechte nicht, wird der Auftrag zwar zur Konfiguration �bertragen, dort aber abgelehnt.
	 *
	 * @param orderer               Benutzername des Auftraggebers
	 * @param ordererPassword       Passwort des Auftraggebers.
	 * @param username              Benutzername, dem ein Einmal-Passwort hinzugef�gt werden soll.
	 * @param singleServingPassword Einmal-Passwort das dem Benutzer, der mit <code>username</code> identifiziert wird, hinzugef�gt wird.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Die Konfiguration kann den Auftrag nicht ausf�hren, weil die �bergebenen Parameter falsch sind. So kann der Auftraggeber
	 *                                    zum Beispiel nicht die n�tigen Rechte besitzen ein Einmal-Passwort anzulegen oder das Passwort existierte bereits, usw..
	 */
	public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
			throws ConfigurationTaskException;

	/**
	 * Ermittelt die Anzahl der noch vorhandenen, g�ltigen Einmal-Passw�rter. Jeder Benutzer kann diese Anzahl f�r seinen eigenen Account ermitteln, 
	 * f�r fremde Accounts sind Admin-Rechte notwendig.
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
	 * Erstellt einen Listener der �nderungen an den Benutzern �berwacht. Gibt eine aktuelle Liste aller Benutzer zur�ck.
	 * @param listener Objekt, an das R�ckmeldungen gesendet werden sollen. <code>null</code>, wenn nur die Liste der aktuellen Benutzer geholt werden soll.
	 * @return Liste der aktuell vorhandenen Benutzer. Es ist eventuell ratsam, mit isUserValid zu pr�fen, ob die Benutzer tats�chlich in der
	 * Benutzerverwaltung.xml abgelegt sind, da hier nur die SystemObjekte ber�cksichtigt werden.
	 */
	public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener);

	/**
	 * Entfernt den mit subscribeUserChangeListener erstellten Listener
	 * @param listener Objekt, and das keine R�ckmeldungen mehr gesendet werden sollen.
	 */
	public void unsubscribeUserChangeListener(MutableCollectionChangeListener listener);
}

/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.main.authentication;

import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

/**
 * Dieses Interface stellt Methoden zur Verfügung, mit der sich ein Benutzer Authentifizieren und Verwaltungsaufgaben
 * anstossen kann.
 * <p>
 * Die Methode {@link #isValidUser} prüft ob eine übergebene Benutzer/Passwort kombination gültig ist.
 * <p>
 * Die Methode {@link #processTask} beauftragt die Konfiguration eine der folgenden Aufträge auszuführen:<br> - Neuer
 * Benutzer anlegen<br> - Einmal-Passwort erzeugen<br> - Rechte eines Benutzers ändern<br> - Passwort eines Benuzters
 * ändern<br>
 * <p>
 * Alle Informationen die für die oben genannten Aufgaben benötigt werden, werden verschlüsselt übertragen.
 * <p>
 * Die Methode {@link #getText} liefert einen Zufallstext. Der Zufallstext wird beöntigt um "Reply-Attacken"
 * (verschicken von Kopien bestimmter Telegramme) zu verhindern. Dieser Text muss in allen Telegrammen, die für die
 * {@link #processTask} Methode benötigt werden, verschlüsselt übertragen werden. Danach darf der verschlüsselt
 * übertragenen Text nicht mehr für andere Aufgaben funktionieren.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface Authentication {
	/**
	 * Die Implementierung dieser Methode stellt die Authentifizierung des Benutzers sicher. Dafür wird das original
	 * Passwort mit dem übergebenen <code>authentificationText</code> verschlüsselt und mit dem übergebenen verschlüsselten
	 * Passwort verglichen. Sind beide Passwörter gleich, und der übergebene Benutzername stimmt mit dem Benutzernamen des
	 * original Passworts überein, so war die Authentifkation erfolgreich.
	 * <p>
	 * Konnte das original Passwort nicht benutzt werden, muss geprüft werden, ob es ein Einmal-Passwort gibt. Das
	 * Einmal-Passwort muss das derzeit aktuell gültige sein und muss mit dem übergebenen verschlüsseltem Passwort
	 * übereinstimmen. Gibt es ein entsprechendes Einmal-Passwort, so ist es für immer zu sperren.
	 * <p>
	 * Konnte kein Passwort gefunden werden, wird eine IllegalArgumentException geworfen.
	 *
	 * @param username					Benutzername, der zu dem übergebenen verschlüsselten Passwort gehört
	 * @param encryptedPassword		   Passwort, das mit dem übergebenen Text <code>authentificationText</code>
	 *                                    verschlüsselt wurde
	 * @param authentificationText		Text, der benutzt wurde um das übergebene Passwort <code>encryptedPassword</code>
	 *                                    zu verschlüsseln
	 * @param authentificationProcessName Name des Verschlüsslungsverfahren, das benutzt wurde. Mit diesem Verfahren wird
	 *                                    das Originalpasswort verschlüsselt
	 * @throws Exception				Fehler beim schreiben der neuen Informationen oder ein technisches Problem beim
	 *                                  verschlüsseln der Daten
	 * @throws IllegalArgumentException Dem Benutzernamen konnte das Passwort nicht zugeordnet werden oder der Benutzer war
	 *                                  unbekannt
	 */
	public void isValidUser(String username, byte[] encryptedPassword, String authentificationText, String authentificationProcessName) throws Exception, IllegalArgumentException;

	/**
	 * Bearbeitet eine der folgenden Aufgaben:<br> - Neuer Benutzer anlegen<br> - Einmal-Passwort erzeugen<br> - Rechte
	 * eines Benutzers ändern<br> - Passwort eines Benutzers ändern<br> - Anzahl der Einmalpasswörter ermitteln<br> - Einmalpasswörter löschen<br>
	 * - Benutzer löschen<br> - Abfrage nach Existenz und Adminstatus eines Benutzers
	 *
	 * @param usernameCustomer	  Benutzer, der den Auftrag erteilt
	 * @param encryptedMessage	  verschlüsselte Aufgabe, die ausgeführt werden soll
	 * @param encryptionProcessName Verschlüsslungsverfahren mit der <code>encryptedMessage</code> erstellt wurde
	 *
	 * @return                            Rückmeldung der durchgeführten Aufgabe, beispielsweise die Anzahl der verbleibenden
	 *                                    Einmalpasswörter, falls danach gefragt wurde. -1 bei Aufgaben ohne Rückgabewert.
	 *
	 * @throws ConfigurationTaskException Der Auftrag, der durch die Konfiguration ausgeführt werden sollte, konnte nicht
	 *                                    durchgeführt werden, weil bestimmte Parameter nicht erfüllt waren. Welche
	 *                                    Parameter dies genau sind, hängt vom jeweiligen Auftrag ab, so kann zum Beispiel
	 *                                    ein Passwort fehlerhaft gewesen sein oder der Benutzer besitzt nicht die nötigen
	 *                                    Rechte um einen Auftrag dieser Art anzustoßen. Wenn der Auftrag erneut
	 *                                    übermittelt werden würde, mit den richtigen Parametern, könnte er ausgeführt
	 *                                    werden.
	 *
	 * @throws RequestException		      Der Auftrag konnte aufgrund eines technischen Fehlers nicht ausgeführt werden
	 *                                    (defektes Speichermedium, Fehler im Dateisystem, usw.). Erst wenn dieser Fehler
	 *                                    behoben ist, können weitere Aufträge ausgeführt werden.
	 */
	public int processTask(String usernameCustomer, byte[] encryptedMessage, String encryptionProcessName) throws ConfigurationTaskException, RequestException;

	/**
	 * Erzeugt einen Zufallstext und gibt diesen als Byte-Array zurück.
	 *
	 * @return Zufallstext
	 */
	public byte[] getText();

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren wird. Es ist ein Zustand herzustellen, der es ermöglicht das System wieder zu starten.
	 */
	public void close();
}

/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.puk.config.main.authentication;

import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

/**
 * Dieses Interface stellt Methoden zur Verf�gung, mit der sich ein Benutzer Authentifizieren und Verwaltungsaufgaben
 * anstossen kann.
 * <p/>
 * Die Methode {@link #isValidUser} pr�ft ob eine �bergebene Benutzer/Passwort kombination g�ltig ist.
 * <p/>
 * Die Methode {@link #processTask} beauftragt die Konfiguration eine der folgenden Auftr�ge auszuf�hren:<br> - Neuer
 * Benutzer anlegen<br> - Einmal-Passwort erzeugen<br> - Rechte eines Benutzers �ndern<br> - Passwort eines Benuzters
 * �ndern<br>
 * <p/>
 * Alle Informationen die f�r die oben genannten Aufgaben ben�tigt werden, werden verschl�sselt �bertragen.
 * <p/>
 * Die Methode {@link #getText} liefert einen Zufallstext. Der Zufallstext wird be�ntigt um "Reply-Attacken"
 * (verschicken von Kopien bestimmter Telegramme) zu verhindern. Dieser Text muss in allen Telegrammen, die f�r die
 * {@link #processTask} Methode ben�tigt werden, verschl�sselt �bertragen werden. Danach darf der verschl�sselt
 * �bertragenen Text nicht mehr f�r andere Aufgaben funktionieren.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface Authentication {
	/**
	 * Die Implementierung dieser Methode stellt die Authentifizierung des Benutzers sicher. Daf�r wird das original
	 * Passwort mit dem �bergebenen <code>authentificationText</code> verschl�sselt und mit dem �bergebenen verschl�sselten
	 * Passwort verglichen. Sind beide Passw�rter gleich, und der �bergebene Benutzername stimmt mit dem Benutzernamen des
	 * original Passworts �berein, so war die Authentifkation erfolgreich.
	 * <p/>
	 * Konnte das original Passwort nicht benutzt werden, muss gepr�ft werden, ob es ein Einmal-Passwort gibt. Das
	 * Einmal-Passwort muss das derzeit aktuell g�ltige sein und muss mit dem �bergebenen verschl�sseltem Passwort
	 * �bereinstimmen. Gibt es ein entsprechendes Einmal-Passwort, so ist es f�r immer zu sperren.
	 * <p/>
	 * Konnte kein Passwort gefunden werden, wird eine IllegalArgumentException geworfen.
	 *
	 * @param username					Benutzername, der zu dem �bergebenen verschl�sselten Passwort geh�rt
	 * @param encryptedPassword		   Passwort, das mit dem �bergebenen Text <code>authentificationText</code>
	 *                                    verschl�sselt wurde
	 * @param authentificationText		Text, der benutzt wurde um das �bergebene Passwort <code>encryptedPassword</code>
	 *                                    zu verschl�sseln
	 * @param authentificationProcessName Name des Verschl�sslungsverfahren, das benutzt wurde. Mit diesem Verfahren wird
	 *                                    das Originalpasswort verschl�sselt
	 * @throws Exception				Fehler beim schreiben der neuen Informationen oder ein technisches Problem beim
	 *                                  verschl�sseln der Daten
	 * @throws IllegalArgumentException Dem Benutzernamen konnte das Passwort nicht zugeordnet werden oder der Benutzer war
	 *                                  unbekannt
	 */
	public void isValidUser(String username, byte[] encryptedPassword, String authentificationText, String authentificationProcessName) throws Exception, IllegalArgumentException;

	/**
	 * Bearbeitet eine der folgenden Aufgaben:<br> - Neuer Benutzer anlegen<br> - Einmal-Passwort erzeugen<br> - Rechte
	 * eines Benutzers �ndern<br> - Passwort eines Benutzers �ndern<br> - Anzahl der Einmalpassw�rter ermitteln<br> - Einmalpassw�rter l�schen<br>
	 * - Benutzer l�schen<br> - Abfrage nach Existenz und Adminstatus eines Benutzers
	 *
	 * @param usernameCustomer	  Benutzer, der den Auftrag erteilt
	 * @param encryptedMessage	  verschl�sselte Aufgabe, die ausgef�hrt werden soll
	 * @param encryptionProcessName Verschl�sslungsverfahren mit der <code>encryptedMessage</code> erstellt wurde
	 *
	 * @return                            R�ckmeldung der durchgef�hrten Aufgabe, beispielsweise die Anzahl der verbleibenden
	 *                                    Einmalpassw�rter, falls danach gefragt wurde. -1 bei Aufgaben ohne R�ckgabewert.
	 *
	 * @throws ConfigurationTaskException Der Auftrag, der durch die Konfiguration ausgef�hrt werden sollte, konnte nicht
	 *                                    durchgef�hrt werden, weil bestimmte Parameter nicht erf�llt waren. Welche
	 *                                    Parameter dies genau sind, h�ngt vom jeweiligen Auftrag ab, so kann zum Beispiel
	 *                                    ein Passwort fehlerhaft gewesen sein oder der Benutzer besitzt nicht die n�tigen
	 *                                    Rechte um einen Auftrag dieser Art anzusto�en. Wenn der Auftrag erneut
	 *                                    �bermittelt werden w�rde, mit den richtigen Parametern, k�nnte er ausgef�hrt
	 *                                    werden.
	 *
	 * @throws RequestException		      Der Auftrag konnte aufgrund eines technischen Fehlers nicht ausgef�hrt werden
	 *                                    (defektes Speichermedium, Fehler im Dateisystem, usw.). Erst wenn dieser Fehler
	 *                                    behoben ist, k�nnen weitere Auftr�ge ausgef�hrt werden.
	 */
	public int processTask(String usernameCustomer, byte[] encryptedMessage, String encryptionProcessName) throws ConfigurationTaskException, RequestException;

	/**
	 * Erzeugt einen Zufallstext und gibt diesen als Byte-Array zur�ck.
	 *
	 * @return Zufallstext
	 */
	public byte[] getText();

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren wird. Es ist ein Zustand herzustellen, der es erm�glicht das System wieder zu starten.
	 */
	public void close();
}

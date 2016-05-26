/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Schnittstelle die seitens der Applikation zu implementieren ist, um bei getriggerten Sendeanmeldungen, den Versand von Daten auszulösen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ClientDavInterface#subscribeSender
 */
public interface ClientSenderInterface {

	/** Eine Sendesteuerung mit diesem Status-Wert signalisiert, dass der Versand von Daten gestartet werden soll. */
	public static final byte START_SENDING = 0;

	/**
	 * Eine Sendesteuerung mit diesem Status-Wert signalisiert, dass der Versand von Daten angehalten werden soll, weil momentan kein Abnehmer sich für die Daten
	 * interessiert.
	 */
	public static final byte STOP_SENDING = 1;

	/**
	 * Eine Sendesteuerung mit diesem Status-Wert signalisiert, dass der Versand von Daten angehalten werden soll, weil momentan keine Rechte für den Versand
	 * vorliegen.
	 */
	public static final byte STOP_SENDING_NO_RIGHTS = 2;

	/**
	 * Eine Sendesteuerung mit diesem Status-Wert signalisiert, dass der Versand von Daten angehalten werden soll, weil die entsprechende Anmeldung momentan nicht
	 * gültig ist (z.B. wegen doppelter Quelle).
	 */
	public static final byte STOP_SENDING_NOT_A_VALID_SUBSCRIPTION = 3;

	/**
	 * Sendesteuerung des Datenverteilers an die Applikation. Diese Methode muss von der Applikation implementiert werden, um den Versand von Daten zu starten bzw.
	 * anzuhalten. Der Datenverteiler signalisiert damit einer Quelle oder einem Sender dass mindestens ein Abnehmer bzw. kein Abnehmer mehr für die zuvor
	 * angemeldeten Daten vorhanden ist. Die Quelle wird damit aufgefordert den Versand von Daten zu starten bzw. zu stoppen.
	 *
	 * @param object          Das in der zugehörigen Sendeanmeldung angegebene Objekt, auf das sich die Sendesteuerung bezieht.
	 * @param dataDescription Beschreibende Informationen zu den angemeldeten Daten auf die sich die Sendesteuerung bezieht.
	 * @param state           Status der Sendesteuerung. Kann einen der Werte <code>START_SENDING</code>, <code>STOP_SENDING</code>,
	 *                        <code>STOP_SENDING_NO_RIGHTS</code>, <code>STOP_SENDING_NOT_A_VALID_SUBSCRIPTION</code> enthalten.
	 *
	 * @see #START_SENDING
	 * @see #STOP_SENDING
	 * @see #STOP_SENDING_NO_RIGHTS
	 * @see #STOP_SENDING_NOT_A_VALID_SUBSCRIPTION
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state);

	/**
	 * Diese Methode muss von der Applikation implementiert werden, um zu signalisieren, ob Sendesteuerungen erwünscht sind und mit der Methode
	 * <code>dataRequest</code> verarbeitet werden. In der Implementierung dieser Methode dürfen keine synchronen Aufrufe, die auf Telegramme vom Datenverteiler
	 * warten (wie z.B. Konfigurationsanfragen) durchgeführt werden, da ansonsten ein Deadlock entsteht.
	 *
	 * @param object          Das in der zugehörigen Sendeanmeldung angegebene System-Objekt.
	 * @param dataDescription Die in der zugehörigen Sendeanmeldung angegebenen beschreibenden Informationen der angemeldeten Daten.
	 *
	 * @return <code>true</code>, falls Sendesteuerungen gewünscht sind, sonst <code>false</code>.
	 *
	 * @see #dataRequest
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription);
}



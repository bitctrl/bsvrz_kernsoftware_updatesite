/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;

public enum SubscriptionState {
	/** Anmeldung ans Sender/Quelle erfolgreich, aber keine Empf�nger/Senke vorhanden */
	NoReceiversAvailable,
	/** Anmeldung wegen fehlenden Rechten ung�ltig */
	NotAllowed,
	/** Anmeldung ung�ltig (z.B. bei mehreren Quellen/Senken) */
	InvalidSubscription,
	/** Anmeldung als Empf�nger/Senke erfolgreich, aber keine Quelle/Sender vorhanden */
	NoSendersAvailable,
	/** Anmeldung als Empf�nger/Senke erfolgreich, Quelle/Sender vorhanden */
	SendersAvailable,
	/** Anmeldung ans Sender/Quelle erfolgreich, Empf�nger/Senke vorhanden */
	ReceiversAvailable,
	/** Anmeldung wartet auf Best�tigung von anderen Datenverteilern */
	Waiting,
	/** Entfernter Datenverteiler ist nicht zust�ndig f�r Daten */
	NotResponsible,
	/** Mehrere Datenverteiler sind zust�ndig, Anmeldung gesperrt */
	MultiRemoteLock
}

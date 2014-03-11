/*
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config.management.consistenycheck;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Kommt es bei einer Konsistenzpr�fung zu einem "lokalen Fehler" oder "Interferenzfehler" oder zu einer "Warnung", sammelt dieses Objekt alle Informationen,
 * die ben�tigt werden, um das Verhalten zu analysieren.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 8542 $ / $Date: 2011-01-05 14:40:38 +0100 (Mi, 05 Jan 2011) $ / ($Author: jh $)
 */
public class ConsistencyCheckResultEntry {

	/** lokaler/Interferenzfehler oder Warnung */
	ConsistencyCheckResultEntryType _entryType;

	/** Betroffener Konfigurationsbereich */
	private final ConfigurationArea _configurationArea;

	/** Objekte, die den Fehler, die Warnung, ausgel�st haben */
	private final SystemObject _involvedObjects[];

	/** Text, der die Art des Fehlers genauer beschreibt */
	private final String _errorText;

	/**
	 * @param entryType         lokaler Fehler, Interferenzfehler oder Warnung
	 * @param configurationArea Konfigurationsbereich, in dem der Fehler, die Warnung, aufgetaucht ist
	 * @param involvedObjects   Objekte, die dazu gef�hrt haben, dass es zu einem Fehler oder einer Warnung gekommen ist
	 * @param errorText         Fehlertext, der die Art des Fehlers/Warnung genauer Beschreibt. Der Text kann mit {@link #getErrorText} angefordert werden.
	 */
	public ConsistencyCheckResultEntry(
			ConsistencyCheckResultEntryType entryType, ConfigurationArea configurationArea, SystemObject[] involvedObjects, String errorText
	) {
		_entryType = entryType;
		_configurationArea = configurationArea;
		_involvedObjects = involvedObjects;
		_errorText = errorText;
	}

	/**
	 * @param entryType         lokaler Fehler, Interferenzfehler oder Warnung
	 * @param configurationArea Konfigurationsbereich, in dem der Fehler, die Warnung, aufgetaucht ist
	 * @param involvedObjects   Objekte, die dazu gef�hrt haben, dass es zu einem Fehler oder einer Warnung gekommen ist
	 * @param errorText         Fehlertext, der die Art des Fehlers/Warnung genauer Beschreibt. Der Text kann mit {@link #getErrorText} angefordert werden.
	 */
	public ConsistencyCheckResultEntry(
			ConsistencyCheckResultEntryType entryType, ConfigurationArea configurationArea, List<SystemObject> involvedObjects, String errorText
	) {
		_entryType = entryType;
		_configurationArea = configurationArea;
		_involvedObjects = involvedObjects.toArray(new SystemObject[involvedObjects.size()]);
		_errorText = errorText;
	}

	/**
	 * Lokaler Fehler, Interferenzfehler oder Warnung
	 *
	 * @return s.o.
	 */
	public ConsistencyCheckResultEntryType getEntryType() {
		return _entryType;
	}

	/**
	 * Konfigurationsbereich in dem der Fehler, die Warnung, aufgetreten ist.
	 *
	 * @return Konfiguratiosnbereich
	 */
	public ConfigurationArea getConfigurationArea() {
		return _configurationArea;
	}

	/**
	 * Objekte, die zu dem Fehler, der Warnung gef�hrt haben
	 *
	 * @return s.o.
	 */
	public SystemObject[] getInvolvedObjects() {
		return _involvedObjects;
	}

	/**
	 * Fehlertext, der zu dem Fehler, der Warnung geh�rt. Der Text wurde im Konstruktor �bergeben
	 *
	 * @return s.o.
	 */
	public String getErrorText() {
		return _errorText;
	}

	public String toString() {
		final StringBuffer errorText = new StringBuffer();
		switch(getEntryType()) {
			case WARNING:
				errorText.append("WARNUNG");
				break;
			case INTERFERENCE_ERROR:
				errorText.append("INTERFERENZ_FEHLER");
				break;
			case LOCAL_ERROR:
				errorText.append("LOKALER_FEHLER");
				break;
			default:
				errorText.append(_entryType);
		}
		errorText.append(": ").append(getErrorText()).append(" ");
		errorText.append("BEREICH: ").append(_configurationArea).append(" ");
		if(_involvedObjects != null && _involvedObjects.length != 0) {
			errorText.append("OBJEKTE: ").append(Arrays.asList(_involvedObjects));
		}
		return errorText.toString();
	}
}

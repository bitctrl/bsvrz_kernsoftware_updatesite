/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.operatingMessage;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.logging.Level;

/**
 * Interface zum versenden von Betriebsmeldungen über das {@link OperatingMessageSink}
 *
 * @author Kappich Systemberatung
 */
public interface OperatingMessageInterface {
	
	/** 
	 * Gibt den Meldungstext zurück
	 * @return den Meldungstext
	 */
	String getMessage();

	/** 
	 * Gibt den Grad der Meldung zurück
	 * @return den Grad der Meldung
	 */
	MessageGrade getGrade();

	/** 
	 * Gibt die ID der Meldung zurück, zur Zuordnung von mehreren zusammengehörigen Meldungen (z.B. Erst-Meldung und Gutmeldung)
	 * @return die ID der Meldung
	 * @deprecated Umbenannt in {@link #getMessageId()}
	 */
	@Deprecated
	String getId();

	/** 
	 * Gibt die Art der Meldung zurück
	 * @return die Art der Meldung
	 */
	MessageType getDomain();

	/** 
	 * Gibt den MeldungsTypZusatz zurück
	 * @return den MeldungsTypZusatz
	 */
	String getMessageTypeAddOn();

	/** 
	 * Gibt das Systemobjekt, auf das sich die Meldung bezieht, zurück
	 * @return das Systemobjekt, auf das sich die Meldung bezieht, oder null falls nicht definiert
	 */
	SystemObject getObject();

	/** 
	 * Gibt den Zustand der Meldung zurück
	 * @return den Zustand der Meldung
	 */
	MessageState getState();

	/** 
	 * Gibt die Exception zurück
	 * @return die Exception
	 */
	Throwable getException();

	/**
	 * Gibt die ID der Betriebsmeldung zurück
	 *
	 * @return die ID der Betriebsmeldung, die z. B. dem Zuordnen von Gutmeldung zur Originalmeldung dient und auch Grundlage für die Pid des Meldungs-Objekts ist
	 * kann.
	 */
	default String getMessageId() {
		return getId();
	}

	default Level getLevel() {
		return Debug.INFO;
	}
}

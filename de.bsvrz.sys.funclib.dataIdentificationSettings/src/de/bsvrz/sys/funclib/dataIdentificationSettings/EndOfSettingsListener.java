/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.dataIdentificationSettings.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.dataIdentificationSettings; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.dataIdentificationSettings;

/**
 * Schnittstelle für Beobachter, die informiert werden wollen, wenn alle Einstellungen abgearbeitet wurden. Diese Information wird benötigt, falls zwischen dem
 * Erhalt aller Einstellungen und der Anmeldung beim Datenverteiler unterschieden werden muss.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see SettingsManager#addEndOfSettingsListener(EndOfSettingsListener)
 */
public interface EndOfSettingsListener {

	/** Wird aufgerufen, sobald alle Einstellungen für jede Datenidentifikation durchlaufen wurden. */
	void inform();
}

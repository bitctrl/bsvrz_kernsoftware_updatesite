/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.Data;

/**
 * Schnittstelle für Beobachter der Parametersätze mit Einstellungen die sich auf Datenidentifikationen beziehen.
 * Derartige Parametersätze werden z.B. zur Steuerung des Archivverhaltens (atg.archiv) und der Parametrierung
 * (atg.parametrierung) eingesetzt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface UpdateListener {
	/**
	 * Wird bei Änderung des Parameters für jede Datenidentifikation aufgerufen für die es einen Eintrag gab oder gibt.
	 *
	 * @param dataIdentification Betroffene Datenidentifikation.
	 * @param oldSettings        Zur Datenidentifikation gehörende Einstellungen vor der Änderung oder <code>null</code>
	 *                           wenn es vor der Änderung keinen spezifischen Eintrag gab.
	 * @param newSettings        Zur Datenidentifikation gehörende Einstellungen nach der Änderung oder <code>null</code>
	 *                           wenn es nach der Änderung keinen spezifischen Eintrag mehr gibt.
	 */
	void update(DataIdentification dataIdentification, Data oldSettings, Data newSettings);
}

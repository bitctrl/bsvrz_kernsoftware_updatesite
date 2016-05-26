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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;

/**
 * Eine Instanz dieser Klasse wird im {@link CacheManager} verwaltet und stellt einen gepufferten Datensatz dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CachedObject {

	/** Zeitpunkt, an dem das Objekt im Cache aufgenommen wurde */
	private long actionTime;

	/** Die Basisanmeldeinformationen */
	private BaseSubscriptionInfo baseSubscriptionInfo;

	/** nachgelieferte Daten */
	private boolean delayedDataFlag;

	/** Laufende Nummer des Datensatzes */
	private long dataNumber;

	/** Datenzeit */
	private long dataTime;

	/**
	 * Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden. 3:
	 * Keine Rechte 8: Mehrere Quelle-Senke-Applikationen
	 */
	private byte errorFlag;

	/** Datensatz */
	private Data _data;

	/**
	 * @param _baseSubscriptionInfo Anmeldeinformationen
	 * @param _delayedDataFlag      Sind die Daten nachgeliefert (true = ja)
	 * @param _dataNumber           Datenindex
	 * @param _time                 Datenzeitpunkt
	 * @param _errorFlag            Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2:
	 *                              Quelle nicht vorhanden. 3: Keine Rechte 8: Mehrere Quelle-Senke-Applikationen
	 * @param dataModel             Datenmodell
	 */
	public CachedObject(
			BaseSubscriptionInfo _baseSubscriptionInfo, boolean _delayedDataFlag, long _dataNumber, long _time, byte _errorFlag, DataModel dataModel
	) {
		baseSubscriptionInfo = _baseSubscriptionInfo;
		if(baseSubscriptionInfo == null) {
			throw new IllegalArgumentException("Falsche Übergabeparameter");
		}
		delayedDataFlag = _delayedDataFlag;
		dataNumber = _dataNumber;
		dataTime = _time;
		errorFlag = _errorFlag;
		if(dataModel == null) {
			throw new IllegalArgumentException("Falsche Übergabeparameter");
		}

		_data = null;
	}

	/**
	 * Zeitpunkt, an dem das Objekt im Cache aufgenommen wurde
	 *
	 * @return Zeitpunkt
	 */
	public long getActionTime() {
		return actionTime;
	}

	/**
	 * Setzt den Zeitpunkt an dem das Objekt im Cache aufgenommen wurde.
	 *
	 * @param _actionTime Vergangende Zeit seit 1970 im ms
	 */
	public final void setActionTime(long _actionTime) {
		actionTime = _actionTime;
	}

	/**
	 * Gibt die basisanmeldeinformationen zurück.
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return baseSubscriptionInfo;
	}

	/**
	 * Sind die Daten nachgeliefert.
	 *
	 * @return true: ja, false: nein
	 */
	public final boolean getDelayedDataFlag() {
		return delayedDataFlag;
	}

	/**
	 * Laufende Nummer des Datensatzes
	 *
	 * @return Laufende Nummer des Datensatzes
	 */
	public final long getDataNumber() {
		return dataNumber;
	}

	/**
	 * Gibt die Datenzeit zurück
	 *
	 * @return Datenzeit
	 */
	public final long getDataTime() {
		return dataTime;
	}

	/**
	 * Fehlercode
	 *
	 * @return Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden.
	 *         3: Keine Rechte 8: Mehrere Quelle-Senke-Applikationen
	 */
	public final byte getErrorFlag() {
		return errorFlag;
	}

	/**
	 * Diese Methode wird von {@link CacheManager} aufgerufen und setzt die übergebenen Parameter im Objekt neu.
	 *
	 * @param _attributeIndicator wird nicht mehr unterstützt und muss <code>null</code> sein.
	 * @param data                Neuer Datensatz der mit {@link #getData()} angefordert werden kann
	 * @param _delayedDataFlag    true = Die übergenenen Daten sind nachgeliefert
	 */
	public void update(byte _attributeIndicator[], Data data, boolean _delayedDataFlag) {
		if(_attributeIndicator != null) {
			throw new IllegalArgumentException(
					"Anmeldungen auf einzelne Attribute der Attributgruppe werden nicht unterstützt. " + "data: " + data
			);
		}

		delayedDataFlag = _delayedDataFlag;
		_data = data;
	}

	public final void debug() {
		System.out.println(baseSubscriptionInfo.toString());
		System.out.println("Time: " + dataTime);
		System.out.println("Number: " + dataNumber);
		System.out.println("Delayed: " + delayedDataFlag);
		System.out.println("ErrorFlag: " + errorFlag);
		System.out.println("_data: " + _data);
	}

	/**
	 * Gepufferter Datensatz.
	 *
	 * @return Datensatz
	 */
	public Data getData() {
		return _data;
	}
}

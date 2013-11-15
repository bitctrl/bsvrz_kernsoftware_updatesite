/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;

/**
 * Eine Instanz dieser Klasse wird im {@link CacheManager} verwaltet und stellt einen gepufferten Datensatz dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6010 $
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
			throw new IllegalArgumentException("Falsche �bergabeparameter");
		}
		delayedDataFlag = _delayedDataFlag;
		dataNumber = _dataNumber;
		dataTime = _time;
		errorFlag = _errorFlag;
		if(dataModel == null) {
			throw new IllegalArgumentException("Falsche �bergabeparameter");
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
	 * Gibt die basisanmeldeinformationen zur�ck.
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
	 * Gibt die Datenzeit zur�ck
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
	 * Diese Methode wird von {@link CacheManager} aufgerufen und setzt die �bergebenen Parameter im Objekt neu.
	 *
	 * @param _attributeIndicator wird nicht mehr unterst�tzt und muss <code>null</code> sein.
	 * @param data                Neuer Datensatz der mit {@link #getData()} angefordert werden kann
	 * @param _delayedDataFlag    true = Die �bergenenen Daten sind nachgeliefert
	 */
	public void update(byte _attributeIndicator[], Data data, boolean _delayedDataFlag) {
		if(_attributeIndicator != null) {
			throw new IllegalArgumentException(
					"Anmeldungen auf einzelne Attribute der Attributgruppe werden nicht unterst�tzt. " + "data: " + data
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

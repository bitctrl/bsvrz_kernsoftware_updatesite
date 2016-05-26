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

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps SendDataObject zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SendDataObject {

	/** Die Basisanmeldeinformationen */
	private BaseSubscriptionInfo _baseSubscriptionInfo;

	/** Besagt das die zu sendenden Daten nachgelieferte Daten sind. */
	private boolean _delayedDataFlag;

	/** Laufende Nummer des Datensatzes */
	private long _dataNumber;

	/** Die Zeit der Datensätze */
	private long _dataTime;

	/**
	 * Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden. 3:
	 * Keine Rechte 8: Mehrere Quelle-Senke-Applikationen
	 */
	private byte _errorFlag;

	/** Der Indikator zu Änderungen der einzelnen Attribute der Attributgruppe */
	private byte _attributesIndicator[] = null;

	/** Der zu sendende Bytestrom */
	private byte _data[] = null;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public SendDataObject() {
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param baseSubscriptionInfo Anmeldeinformationen
	 * @param delayedDataFlag      Besagt das die zu sendenden Daten nachgelieferte Daten sind.
	 * @param dataNumber           Laufende Nummer des Datensatzes
	 * @param time                 Die Zeit der Datensätze
	 * @param errorFlag            Fehlerkennung
	 * @param attributesIndicator  Indikator zu der Änderungen der einzelnen Attributen
	 * @param data                 Der zu sendende Bytestrom
	 */
	public SendDataObject(
			BaseSubscriptionInfo baseSubscriptionInfo,
			boolean delayedDataFlag,
			long dataNumber,
			long time,
			byte errorFlag,
			byte attributesIndicator[],
			byte data[]
	) {
		_baseSubscriptionInfo = baseSubscriptionInfo;
		_delayedDataFlag = delayedDataFlag;
		_dataNumber = dataNumber;
		_dataTime = time;
		_errorFlag = errorFlag;
		_attributesIndicator = attributesIndicator;
		_data = data;
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _baseSubscriptionInfo;
	}

	/**
	 * Gibt an, ob Interesse an nachgelieferte oder an aktuelle Datensätze besteht.
	 *
	 * @return true: verspätet, false: pünktlich
	 */
	public final boolean getDalayedDataFlag() {
		return _delayedDataFlag;
	}

	/**
	 * Gibt die Laufende Nummer des Datensatzes zurück.
	 *
	 * @return laufende Nummer des Datensatzes
	 */
	public final long getDataNumber() {
		return _dataNumber;
	}

	/**
	 * Die Zeit der Datensätze.
	 *
	 * @return Zeitstempel
	 */
	public final long getDataTime() {
		return _dataTime;
	}

	/**
	 * Gibt der Fehlercode wenn vorhanden zurück.
	 *
	 * @return Fehlercode
	 */
	public final byte getErrorFlag() {
		return _errorFlag;
	}

	/**
	 * Gibt den Indikator zu den einzelnen Attributen der Attributgruppe zurück.
	 *
	 * @return Indikator der Attributgruppe
	 */
	public final byte[] getAttributesIndicator() {
		return _attributesIndicator;
	}

	/**
	 * Gibt den Datensatz zurrück.
	 *
	 * @return Datensatz
	 */
	public final byte[] getData() {
		return _data;
	}

	/**
	 * Gibt ein String zurrück, der diesen Datensatz beschreibt.
	 *
	 * @return Der String, der diesen Datensatz beschreibt
	 */
	public String parseToString() {
		String str = "Sende Objekt: \n";
		str += _baseSubscriptionInfo.toString() + "\n";
		str += "Nachgelieferte Daten : " + _delayedDataFlag + "\n";
		str += "Sendedaten Nummer    : " + _dataNumber + "\n";
		str += "Sendedaten Zeit      : " + _dataTime + "\n";
		str += "Fehler Information   : " + _errorFlag + "\n";
		if(_attributesIndicator != null) {
			str += "----------Attributsänderungsindikator----------\n";
			for(int i = 0; i < _attributesIndicator.length; ++i) {
				byte b = _attributesIndicator[i];
				int j = i * 8;
				if((b & 0x01) == 0x01) {
					str += "Attribut an der Position " + (j) + " hat sich geändert.";
				}
				if((b & 0x02) == 0x02) {
					str += "Attribut an der Position " + (j + 1) + " hat sich geändert.";
				}
				if((b & 0x04) == 0x04) {
					str += "Attribut an der Position " + (j + 2) + " hat sich geändert.";
				}
				if((b & 0x08) == 0x08) {
					str += "Attribut an der Position " + (j + 3) + " hat sich geändert.";
				}
				if((b & 0x10) == 0x10) {
					str += "Attribut an der Position " + (j + 4) + " hat sich geändert.";
				}
				if((b & 0x20) == 0x20) {
					str += "Attribut an der Position " + (j + 5) + " hat sich geändert.";
				}
				if((b & 0x40) == 0x40) {
					str += "Attribut an der Position " + (j + 6) + " hat sich geändert.";
				}
				if((b & 0x80) == 0x80) {
					str += "Attribut an der Position " + (j + 7) + " hat sich geändert.";
				}
			}
		}
		if(_data != null) {
			str += new String(_data);
		}
		return str;
	}

	/**
	 * Schreiben eines Datensatzes in den gegebenen DataOutputStream
	 *
	 * @param out Ausgabe-Stream
	 *
	 * @throws IOException, wenn beim Schreiben vom Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public final void write(DataOutputStream out) throws IOException {
		_baseSubscriptionInfo.write(out);
		out.writeBoolean(_delayedDataFlag);
		out.writeLong(_dataNumber);
		out.writeLong(_dataTime);
		out.writeByte(_errorFlag);
		if(_attributesIndicator == null) {
			out.writeInt(0);
		}
		else {
			out.writeByte(_attributesIndicator.length);
			if(_attributesIndicator.length > 0) {
				for(int i = 0; i < _attributesIndicator.length; ++i) {
					out.writeByte(_attributesIndicator[i]);
				}
			}
		}
		if(_data == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_data.length);
			if(_data.length > 0) {
				for(int i = 0; i < _data.length; ++i) {
					out.writeByte(_data[i]);
				}
			}
		}
	}

	/**
	 * Lesen eines Datensatzes vom gegebenen DataInputStream
	 *
	 * @param in Eingabe-Stream
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public final void read(DataInputStream in) throws IOException {
		_baseSubscriptionInfo = new BaseSubscriptionInfo();
		_baseSubscriptionInfo.read(in);
		_delayedDataFlag = in.readBoolean();
		_dataNumber = in.readLong();
		_dataTime = in.readLong();
		_errorFlag = in.readByte();
		int size = in.readByte();
		if(size > 0) {
			_attributesIndicator = new byte[size];
			for(int i = 0; i < size; ++i) {
				_attributesIndicator[i] = in.readByte();
			}
		}
		size = in.readInt();
		if(size > 0) {
			_data = new byte[size];
			for(int i = 0; i < size; ++i) {
				_data[i] = in.readByte();
			}
		}
	}

	/**
	 * Gibt die Länge dieses Telegrams zurück
	 *
	 * @return die Länge dieses Telegrams
	 */
	public int getLength() {
		return 37 + (_attributesIndicator == null ? 0 : _attributesIndicator.length) + (_data == null ? 0 : _data.length);
	}
}

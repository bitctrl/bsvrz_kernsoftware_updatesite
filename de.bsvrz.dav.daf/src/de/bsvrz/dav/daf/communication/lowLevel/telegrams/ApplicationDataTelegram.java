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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.communication.lowLevel.TelegramUtility;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Diese Klasse enthält den Teil eines Datensatzes, der zu gross war um komplett versendet zu werden. Es werden alle Informationen gespeichert um dieses Stück
 * des Datensatzes mit den anderen Stücken zu verbinden um den gesamten Datensatz wieder zusammen zu bauen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */

public class ApplicationDataTelegram extends DataTelegram implements DataTelegramInterface {
	/** Objekt für Debugausgaben */
	private static Debug _debug = Debug.getLogger();

	/** Der Index dieses Telegramms */
	private int telegramNumber;

	/** Anzahl der Teiltelegramme, in die das grosse Telegramm zerlegt wurde. */
	private int totalTelegramCount;

	/** Zeitpunkt, an dem der Datensatz erzeugt wurde. */
	private long dataTime;

	/** Die Basisanmeldeinformationen */
	private BaseSubscriptionInfo baseSubscriptionInfo;

	/** Besagt das die zu sendenden Daten nachgelieferte Daten sind. */
	private boolean delayedDataFlag;

	/** Laufende Nummer des Datensatzes */
	private long dataNumber;

	/**
	 * Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden. 3:
	 * Keine Rechte 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 */
	private byte errorFlag;

	/**
	 * Indikator zu den einzelnen Attributen der Attributgruppe. Ein Byte steht für 8 Attribute.
	 *
	 * @return geseztes Bit heißt: entsprechendes Attribut hat sich verändert, ungesetztes Bit heißt: entsprechendes Attribut hat sich nicht verändert
	 */
	private byte attributesIndicator[];

	/** Teildatensatz als Bytestrom */

	private byte data[];

	public ApplicationDataTelegram() {
		type = APPLICATION_DATA_TELEGRAM_TYPE;
	}

	/**
	 * @param _baseSubscriptionInfo Basisanmeldeinformation
	 * @param _dataNumber           Laufende Nummer des Datensatzes
	 * @param _delayedDataFlag      Sind die Daten nachgeliefert
	 * @param _errorFlag            Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2:
	 *                              Quelle nicht vorhanden. 3: Keine Rechte 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 * @param _attributesIndicator  gibt an ob sich ein Attribut geändert hat
	 * @param _priority             Priorität des Telegramms ({@link de.bsvrz.dav.daf.main.impl.CommunicationConstant} )
	 * @param _data                 Teil des Datensatzes als Bytestrom
	 * @param _totalTelegramCount   Anzahl aller Teiltelegramme, in die der gesamte Datensatz zerlegt wurde
	 * @param _telegramNumber       Index des Teiltelegramms
	 * @param _time                 Datatime (Zeitpunkt, an dem der Datensatz erzeugt wurde)
	 */
	public ApplicationDataTelegram(
			BaseSubscriptionInfo _baseSubscriptionInfo,
			long _dataNumber,
			boolean _delayedDataFlag,
			byte _errorFlag,
			byte _attributesIndicator[],
			byte _priority,
			byte[] _data,
			int _totalTelegramCount,
			int _telegramNumber,
			long _time
	) {
		type = APPLICATION_DATA_TELEGRAM_TYPE;
		priority = _priority;
		totalTelegramCount = _totalTelegramCount;
		telegramNumber = _telegramNumber;
		dataTime = _time;
		baseSubscriptionInfo = _baseSubscriptionInfo;
		dataNumber = _dataNumber;
		delayedDataFlag = _delayedDataFlag;
		errorFlag = _errorFlag;
		attributesIndicator = _attributesIndicator;
		data = _data;

		length = 31;
		if(telegramNumber == 0) {
			length += (10 + (attributesIndicator != null ? attributesIndicator.length : 0));
		}
		if(data != null) {
			length += data.length;
		}
		checkConsistency();
	}

	/**
	 * @param _baseSubscriptionInfo Basisanmeldeinformation
	 * @param _dataNumber           Laufende Nummer des Datensatzes
	 * @param _delayedDataFlag      Sind die Daten nachgeliefert
	 * @param _errorFlag            Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2:
	 *                              Quelle nicht vorhanden. 3: Keine Rechte 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 * @param _attributesIndicator  gibt an ob sich ein Attribut geändert hat
	 * @param _data                 Teil des Datensatzes als Bytestrom
	 * @param _totalTelegramCount   Anzahl aller Teiltelegramme, in die der gesamte Datensatz zerlegt wurde
	 * @param _telegramNumber       Index des Teiltelegramms
	 * @param _time                 Datatime (Zeitpunkt, an dem der Datensatz erzeugt wurde)
	 */
	public ApplicationDataTelegram(
			BaseSubscriptionInfo _baseSubscriptionInfo,
			long _dataNumber,
			boolean _delayedDataFlag,
			byte _errorFlag,
			byte _attributesIndicator[],
			byte[] _data,
			int _totalTelegramCount,
			int _telegramNumber,
			long _time
	) {
		type = APPLICATION_DATA_TELEGRAM_TYPE;
		totalTelegramCount = _totalTelegramCount;
		telegramNumber = _telegramNumber;
		dataTime = _time;
		baseSubscriptionInfo = _baseSubscriptionInfo;
		dataNumber = _dataNumber;
		delayedDataFlag = _delayedDataFlag;
		errorFlag = _errorFlag;
		attributesIndicator = _attributesIndicator;
		data = _data;
		priority = TelegramUtility.getPriority(this);

		length = 31;
		if(telegramNumber == 0) {
			length += (10 + (attributesIndicator != null ? attributesIndicator.length : 0));
		}
		if(data != null) {
			length += data.length;
		}
		checkConsistency();
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück.
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return baseSubscriptionInfo;
	}

	/**
	 * Gibt an, Datensätze nachgeliefert worden sind. True-> verspätet, false-> nicht verspätet
	 *
	 * @return True wenn Daten nachgeliefert sind.
	 */
	public final boolean getDelayedDataFlag() {
		return delayedDataFlag;
	}

	/**
	 * Gibt die Laufende Nummer des Datensatzes zurrück.
	 *
	 * @return Fortlaufende Nummer
	 */
	public final long getDataNumber() {
		return dataNumber;
	}

	/**
	 * Der Zeitstempel der Datensätze.
	 *
	 * @return Zeitstempel
	 */
	public final long getDataTime() {
		return dataTime;
	}

	/**
	 * Gibt die Fehlerinformation zurück. Fehlerkennung der Anwendungsdaten:<br> 0: Daten vorhanden (kein fehler).<br> 1: Quelle vorhanden aber Daten noch nicht
	 * lieferbar.<br> 2: Quelle nicht vorhanden.<br> 3: Keine Rechte<br> 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 *
	 * @return Fehlercode
	 */
	public final byte getErrorFlag() {
		return errorFlag;
	}

	/**
	 * Gibt den Indikator zu den einzelnen Attributen der Attributgruppe zurück.
	 *
	 * @return Indikator
	 */
	public final byte[] getAttributesIndicator() {
		return attributesIndicator;
	}

	/**
	 * Setzt den Indikator auf den neuen Wert.
	 *
	 * @param newIndicators enthält Informationen, welche Attribute sich geändert haben.
	 */
	public final void setAttributesIndicator(byte newIndicators[]) {
		int tmp = (newIndicators != null ? newIndicators.length : 0) - (attributesIndicator != null ? attributesIndicator.length : 0);
		attributesIndicator = newIndicators;
		if(telegramNumber == 0) {
			length += tmp;
		}
	}

	/**
	 * Gibt die Gesamtzahl der Teiltelegramme des ursprunglichen Datensatzes zurück.
	 *
	 * @return Gesamte Länge des Telegramms in Teiltelegrammen
	 */
	public final int getTotalTelegramsCount() {
		return totalTelegramCount;
	}

	/**
	 * Gibt die Telegrammnummer dieses Teiltelegramms zurück.
	 *
	 * @return Telegrammnummer
	 */
	public final int getTelegramNumber() {
		return telegramNumber;
	}

	/**
	 * Gibt den Bytestrom dieses Telegramms zurück.
	 *
	 * @return Bytestrom
	 */
	public final byte[] getData() {
		return data;
	}

	/**
	 * Setzt den Datensatz-Index .
	 *
	 * @param _dataNumber Fortlaufende Nummer
	 */
	public final void setDataIndex(long _dataNumber) {
		dataNumber = _dataNumber;
	}

	public String toShortDebugParamString() {
		return "tn: " + telegramNumber + "/" + totalTelegramCount + ", " + " dataNumber: " + (dataNumber >>> 32) + "#" + ((dataNumber & 0xffffffffL) >> 2) + "#"
		       + (dataNumber & 3) + ", ef: " + errorFlag + ", " + baseSubscriptionInfo.toString();
	}


	public final String parseToString() {
		String str = "Anwendungsdatentelegramm : \n";
		str += "Index Dieses Telegramms: " + telegramNumber + "\n";
		str += "Gesamt Anzahl der Telegramme: " + totalTelegramCount + "\n";
		if(baseSubscriptionInfo != null) {
			str += baseSubscriptionInfo.toString() + "\n";
		}
		str += "Sendedaten Nummer: " + dataNumber + "\n";
		str += "Nachgelieferte Daten: " + delayedDataFlag + "\n";
		if(telegramNumber == 0) {
			str += "Datensaetzezeit: " + new java.util.Date(dataTime) + "\n";
			str += "Fehler code: " + errorFlag + "\n";
			if(attributesIndicator != null) {
				str += "Attributsindikator: \n";
				for(int i = 0; i < attributesIndicator.length; ++i) {
					final byte b = attributesIndicator[i];
					final int j = i * 8;
					if((b & 0x01) == 0x01) {
						str += "Attributsbit an der Position " + (j) + " ist gesetzt.\n";
					}
					if((b & 0x02) == 0x02) {
						str += "Attributsbit an der Position " + (j + 1) + " ist gesetzt.\n";
					}
					if((b & 0x04) == 0x04) {
						str += "Attributsbit an der Position " + (j + 2) + " ist gesetzt.\n";
					}
					if((b & 0x08) == 0x08) {
						str += "Attributsbit an der Position " + (j + 3) + " ist gesetzt.\n";
					}
					if((b & 0x10) == 0x10) {
						str += "Attributsbit an der Position " + (j + 4) + " ist gesetzt.\n";
					}
					if((b & 0x20) == 0x20) {
						str += "Attributsbit an der Position " + (j + 5) + " ist gesetzt.\n";
					}
					if((b & 0x40) == 0x40) {
						str += "Attributsbit an der Position " + (j + 6) + " ist gesetzt.\n";
					}
					if((b & 0x80) == 0x80) {
						str += "Attributsbit an der Position " + (j + 7) + " ist gesetzt.\n";
					}
				}
			}
		}
		//str += "Daten: " + new String(data) + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeShort(telegramNumber);
		out.writeShort(totalTelegramCount);
		if(baseSubscriptionInfo != null) {
			baseSubscriptionInfo.write(out);
		}
		out.writeLong(dataNumber);
		out.writeBoolean(delayedDataFlag);

		if(telegramNumber == 0) {
			out.writeLong(dataTime);
			out.writeByte(errorFlag);
			if(attributesIndicator == null) {
				out.writeByte(0);
			}
			else {
				out.writeByte(attributesIndicator.length);
				for(int i = 0; i < attributesIndicator.length; ++i) {
					out.writeByte(attributesIndicator[i]);
				}
			}
		}

		if(data == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(data.length);
			for(int i = 0; i < data.length; i++) {
				byte aData = data[i];
				out.writeByte(aData);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		telegramNumber = in.readShort();
		totalTelegramCount = in.readShort();
		baseSubscriptionInfo = new BaseSubscriptionInfo();
		baseSubscriptionInfo.read(in);
		dataNumber = in.readLong();
		delayedDataFlag = in.readBoolean();
		length = 31;
		int size;
		if(telegramNumber == 0) {
			dataTime = in.readLong();
			errorFlag = in.readByte();
			size = in.readByte();
			length += 10;
			if(size > 0) {
				attributesIndicator = new byte[size];
				for(int i = 0; i < size; ++i) {
					attributesIndicator[i] = in.readByte();
				}
				length += attributesIndicator.length;
			}
		}
		size = in.readInt();
		if(size > 0) {
			data = new byte[size];
			for(int i = 0; i < size; ++i) {
				data[i] = in.readByte();
			}
			length += data.length;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
		priority = TelegramUtility.getPriority(this);
		checkConsistency();
	}

	private void checkConsistency() {
		if(data == null && errorFlag == 0) {
			String message = "Inkonsistentes Applikationstelegramm: " + telegramNumber + "/" + totalTelegramCount + ", " +
			                 " dataNumber: " + (dataNumber >>> 32) + "#" + ((dataNumber & 0xffffffffL) >> 2) + "#" + (dataNumber & 3) + ", " +
			                 baseSubscriptionInfo.toString() + ", errorFlag: " + errorFlag + ", data: " + (data == null ? "null" : String.valueOf(data.length) + " Bytes");
			_debug.warning(message, new Exception("Inkonsistentes Applikationstelegramm"));
		}
	}
}

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
 * Telegramm zur Übertragung der Anwendungsdaten. Mit diesem Telegramm werden die Anwendungsdaten übertragen. Es kann in beide Richtungen benutzt werden. Die
 * Anwendungsdaten werden durch folgende Informationen gekennzeichnet: Konfigurationsobjekt (Objekt-Id), Attributgruppe, Aspekt, Simulationsvariante. Die zu
 * übertragenden Anwendungsdaten werden ab einer bestimmten Größe in mehrere Telegramme zerlegt. Um unnötige Redundanz zu vermeiden, werden die
 * Telegrammelemente Zeitstempel, Fehlerkennung sowie die Angaben zum Attributindikator nur im ersten Telegramm übertragen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterDataTelegram extends DataTelegram implements DataTelegramInterface{
	/** Objekt für Debugausgaben */
	private static Debug _debug = Debug.getLogger();

	/** Der Index dieses Telegramms */
	private int _telegramNumber;

	/** Die gesamte Anzahl der Telegramme im Byte-Array. */
	private int _totalTelegramCount;

	/** Die Zeit der Erzeugung des Datensatzes */
	private long _dataTime;

	/** Die Basisanmeldeinformationen */
	private BaseSubscriptionInfo _baseSubscriptionInfo;

	/** Besagt, dass die zu sendenden Daten nachgelieferte Daten sind. */
	private boolean _delayedDataFlag;

	/** Laufende Nummer des Datensatzes */
	private long _dataNumber;

	/**
	 * Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein Fehler). 1: Quelle vorhanden, aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden. 3:
	 * Keine Rechte 8: Unzulässige Anmeldung(2 Quellen, 2 Senken, 1 Quelle 1 Senke und umgekehrt)
	 */
	private byte _errorFlag;

	/** Der Indikator zu den Änderungen der einzelnen Attribute der Attributgruppe. */
	private byte _attributesIndicator[];

	/** Die Teildatensätze als Bytestrom */
	private byte _data[];

	/** Die Information über die Datenflußrichtung 0: Sender zum Zentraldatenverteiler 1: Zentraldatenverteiler an die Empfänger */
	private byte _direction;

	public TransmitterDataTelegram() {
		type = TRANSMITTER_DATA_TELEGRAM_TYPE;
	}

	/**
	 * Erzeugt neues TransmitterDataTelegram
	 *
	 * @param applicationDataTelegram Zerlegtes ApplicationDataTelegram
	 * @param direction               Information über der Datenflussesrichtung 0: Sender zum Zentraldatenverteiler 1: Zentraldatenverteiler an die Empfänger
	 */
	public TransmitterDataTelegram(ApplicationDataTelegram applicationDataTelegram, byte direction) {
		type = TRANSMITTER_DATA_TELEGRAM_TYPE;
		priority = applicationDataTelegram.getPriority();
		_telegramNumber = applicationDataTelegram.getTelegramNumber();
		_totalTelegramCount = applicationDataTelegram.getTotalTelegramsCount();
		_baseSubscriptionInfo = applicationDataTelegram.getBaseSubscriptionInfo();
		_direction = direction;
		_dataNumber = applicationDataTelegram.getDataNumber();
		_delayedDataFlag = applicationDataTelegram.getDelayedDataFlag();
		_data = applicationDataTelegram.getData();
		if(_telegramNumber == 0) {
			_dataTime = applicationDataTelegram.getDataTime();
			_errorFlag = applicationDataTelegram.getErrorFlag();
			_attributesIndicator = applicationDataTelegram.getAttributesIndicator();
		}

		length = 32;
		if(_telegramNumber == 0) {
			length += (10 + (_attributesIndicator != null ? _attributesIndicator.length : 0));
		}
		if(_data != null) {
			length += _data.length;
		}
		checkConsistency();
	}

	/**
	 * @param baseSubscriptionInfo Basisanmeldeinformation
	 * @param dataNumber           Laufende Nummer des Datensatzes
	 * @param delayedDataFlag      Sind die Daten nachgeliefert
	 * @param errorFlag            Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2:
	 *                             Quelle nicht vorhanden. 3: Keine Rechte 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 * @param attributesIndicator  gibt an ob sich ein Attribut geändert hat
	 * @param data                 Teil des Datensatzes als Bytestrom
	 * @param totalTelegramCount   Anzahl aller Teiltelegramme, in die der gesamte Datensatz zerlegt wurde
	 * @param telegramNumber       Index des Teiltelegramms
	 * @param dataTime             Zeitstempel des Telegramms
	 * @param direction            Information über der Datenflussesrichtung 0: Sender zum Zentraldatenverteiler 1: Zentraldatenverteiler an die Empfänger
	 */
	public TransmitterDataTelegram(
			BaseSubscriptionInfo baseSubscriptionInfo,
			long dataNumber,
			boolean delayedDataFlag,
			byte errorFlag,
			byte attributesIndicator[],
			byte[] data,
			int totalTelegramCount,
			int telegramNumber,
			long dataTime,
			byte direction
	) {
		type = TRANSMITTER_DATA_TELEGRAM_TYPE;
		_telegramNumber = telegramNumber;
		_totalTelegramCount = totalTelegramCount;
		_baseSubscriptionInfo = baseSubscriptionInfo;
		_direction = direction;
		_dataNumber = dataNumber;
		_delayedDataFlag = delayedDataFlag;
		_data = data;
		if(_telegramNumber == 0) {
			_dataTime = dataTime;
			_errorFlag = errorFlag;
			_attributesIndicator = attributesIndicator;
		}
		priority = TelegramUtility.getPriority(this);

		length = 32;
		if(_telegramNumber == 0) {
			length += (10 + (_attributesIndicator != null ? _attributesIndicator.length : 0));
		}
		if(_data != null) {
			length += _data.length;
		}
		checkConsistency();
	}

	/**
	 * @param baseSubscriptionInfo Basisanmeldeinformation
	 * @param dataNumber           Laufende Nummer des Datensatzes
	 * @param delayedDataFlag      Sind die Daten nachgeliefert
	 * @param errorFlag            Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2:
	 *                             Quelle nicht vorhanden. 3: Keine Rechte 8: Unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt)
	 * @param attributesIndicator  gibt an ob sich ein Attribut geändert hat
	 * @param prior                Priorität des Telegramms
	 * @param data                 Teil des Datensatzes als Bytestrom
	 * @param totalTelegramCount   Anzahl aller Teiltelegramme, in die der gesamte Datensatz zerlegt wurde
	 * @param telegramNumber       Index des Teiltelegramms
	 * @param dataTime             Zeitstempel des Telegramms
	 * @param direction            Information über der Datenflussesrichtung 0: Sender zum Zentraldatenverteiler 1: Zentraldatenverteiler an die Empfänger
	 */
	public TransmitterDataTelegram(
			BaseSubscriptionInfo baseSubscriptionInfo,
			long dataNumber,
			boolean delayedDataFlag,
			byte errorFlag,
			byte attributesIndicator[],
			byte prior,
			byte[] data,
			int totalTelegramCount,
			int telegramNumber,
			long dataTime,
			byte direction
	) {
		type = TRANSMITTER_DATA_TELEGRAM_TYPE;
		_telegramNumber = telegramNumber;
		_totalTelegramCount = totalTelegramCount;
		_baseSubscriptionInfo = baseSubscriptionInfo;
		_direction = direction;
		_dataNumber = dataNumber;
		_delayedDataFlag = delayedDataFlag;
		_data = data;
		if(_telegramNumber == 0) {
			_dataTime = dataTime;
			_errorFlag = errorFlag;
			_attributesIndicator = attributesIndicator;
		}
		priority = prior;

		length = 32;
		if(_telegramNumber == 0) {
			length += (10 + (_attributesIndicator != null ? _attributesIndicator.length : 0));
		}
		if(_data != null) {
			length += _data.length;
		}
		checkConsistency();
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück.
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _baseSubscriptionInfo;
	}

	/**
	 * Gibt an, ob Datensätze nachgeliefert worden sind. True-> verspätet, false-> nicht verspätet
	 *
	 * @return True wenn Daten nachgeliefert sind.
	 */
	public final boolean getDelayedDataFlag() {
		return _delayedDataFlag;
	}

	/**
	 * Gibt die Laufende Nummer des Datensatzes zurück.
	 *
	 * @return Laufende Nummer des Datensatzes
	 */
	public final long getDataNumber() {
		return _dataNumber;
	}

	/**
	 * Gibt die Zeit der Datensätze an.
	 *
	 * @return Zeit der Datensätze
	 */
	public final long getDataTime() {
		return _dataTime;
	}

	/**
	 * Gibt die Fehlerinformationen zurück.
	 *
	 * @return Fehlerinformationen
	 */
	public final byte getErrorFlag() {
		return _errorFlag;
	}

	/**
	 * Gibt den Indikator zu den einzelnen Attributen der Attributgruppe zurück.
	 *
	 * @return Indikator
	 */
	public final byte[] getAttributesIndicator() {
		return _attributesIndicator;
	}

	/**
	 * Gibt die Gesamtanzahl der Teiltelegramme des ursprünglichen Datensatzes zurück.
	 *
	 * @return Gesamtanzahl der Teiltelegramme
	 */
	public final int getTotalTelegramsCount() {
		return _totalTelegramCount;
	}

	/**
	 * Gibt die Telegrammnummer dieses Teiltelegramms zurück
	 *
	 * @return Telegrammnummer
	 */
	public final int getTelegramNumber() {
		return _telegramNumber;
	}

	/**
	 * Gibt den Bytestrom dieses Telegramms zurück.
	 *
	 * @return Bytestrom
	 */
	public final byte[] getData() {
		return _data;
	}

	/**
	 * Gibt die Datenflußrichtung dieses Telegramms zurück.
	 *
	 * @return Datenflußrichtung
	 */
	public final byte getDirection() {
		return _direction;
	}

	/**
	 * Setzt die Datenflußrichtung dieses Telegramms auf den neuen Wert.
	 *
	 * @param newDirection neue Richtung
	 */
	public final void setDirection(byte newDirection) {
		_direction = newDirection;
	}

	/**
	 * Setzt den Datensatz-Index
	 *
	 * @param dataNumber Datensatz-Index
	 */
	public final void setDataIndex(long dataNumber) {
		_dataNumber = dataNumber;
	}

	/**
	 * Erstellt ein Applikations-kompatibles Telegramm aus diesem Objekt.
	 *
	 * @return Applikations kompatibles Telegramm
	 */
	public final ApplicationDataTelegram getApplicationDataTelegram() {
		return new ApplicationDataTelegram(
				_baseSubscriptionInfo,
				_dataNumber,
				_delayedDataFlag,
				_errorFlag,
				_attributesIndicator,
				priority,
				_data,
				_totalTelegramCount,
				_telegramNumber,
				_dataTime
		);
	}

	public final String parseToString() {
		String str = "Anwendungsdatentelegramm : \n";
		str += "Index Dieses Telegramms: " + _telegramNumber + "\n";
		str += "Gesamt Anzahl der Telegramme: " + _totalTelegramCount + "\n";
		str += _baseSubscriptionInfo.toString() + "\n";
		if(_direction == 0) {
			str += "Richtung: Vom Sender zum Zentraldatenverteiler.\n";
		}
		else {
			str += "Richtung: Vom Zentraldatenverteiler an die Empfänger.\n";
		}
		str += "Sendedaten Nummer: " + _dataNumber + "\n";
		str += "Nachgelieferte Daten: " + _delayedDataFlag + "\n";
		if(_telegramNumber == 0) {
			str += "Datensätzezeit: " + new java.util.Date(_dataTime) + "\n";
			str += "Fehler code: " + _errorFlag + "\n";
			if(_attributesIndicator != null) {
				str += "Attributsindikator: \n";
				for(int i = 0; i < _attributesIndicator.length; ++i) {
					byte b = _attributesIndicator[i];
					int j = i * 8;
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
		//str += "Daten: " + new String(_data) + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeShort(_telegramNumber);
		out.writeShort(_totalTelegramCount);
		_baseSubscriptionInfo.write(out);
		out.writeByte(_direction);
		out.writeLong(_dataNumber);
		out.writeBoolean(_delayedDataFlag);
		if(_telegramNumber == 0) {
			out.writeLong(_dataTime);
			out.writeByte(_errorFlag);
			if(_attributesIndicator == null) {
				out.writeByte(0);
			}
			else {
				out.writeByte(_attributesIndicator.length);
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
			for(int i = 0; i < _data.length; ++i) {
				out.writeByte(_data[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_telegramNumber = in.readShort();
		_totalTelegramCount = in.readShort();
		_baseSubscriptionInfo = new BaseSubscriptionInfo();
		_baseSubscriptionInfo.read(in);
		_direction = in.readByte();
		_dataNumber = in.readLong();
		_delayedDataFlag = in.readBoolean();
		length = 32;
		int size;
		if(_telegramNumber == 0) {
			_dataTime = in.readLong();
			_errorFlag = in.readByte();
			size = in.readByte();
			length += 10;
			if(size > 0) {
				_attributesIndicator = new byte[size];
				for(int i = 0; i < size; ++i) {
					_attributesIndicator[i] = in.readByte();
				}
				length += _attributesIndicator.length;
			}
		}
		size = in.readInt();
		if(size > 0) {
			_data = new byte[size];
			for(int i = 0; i < size; ++i) {
				_data[i] = in.readByte();
			}
			length += _data.length;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
		priority = TelegramUtility.getPriority(this);
		checkConsistency();
	}

	private void checkConsistency() {
		if(_data == null && _errorFlag == 0) {
			String message = "Inkonsistentes Datenverteilertelegramm: " + _telegramNumber + "/" + _totalTelegramCount + ", " +
			                 " dataNumber: " + (_dataNumber >>> 32) + "#" + ((_dataNumber & 0xffffffffL) >> 2) + "#" + (_dataNumber & 3) + ", " +
			                 _baseSubscriptionInfo.toString() + ", errorFlag: " + _errorFlag + ", data: " + (_data == null ? "null" : String.valueOf(_data.length) + " Bytes") +
					", richtung: " + _direction;
			_debug.warning(message, new Exception("Inkonsistentes Datenverteilertelegramm"));
		}
	}

}

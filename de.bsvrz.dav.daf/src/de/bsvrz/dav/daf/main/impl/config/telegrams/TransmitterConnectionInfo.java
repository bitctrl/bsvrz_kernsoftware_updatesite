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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Diese Klasse speichert die Informationen zur Verbindung zwischen zwei Datenverteilern. Gespeichert werden: <ul> <li> Adressen der zwei DAV</li> <li> Art der
 * verbindung: bidirektional, unidirektional oder Ersatzverbindung</li> <li> Gewichtung der Verbindung</li> <li> Liste der Ersatz-DAV</li> <li> Wartezeit bis
 * zur Etablierung der Ersatzverbindung</li> </ul>
 *
 * @author Kappich Systemberatung
 */
public class TransmitterConnectionInfo {

	/** Der erste Datenverteiler der Verbindung */
	private TransmitterInfo _transmitter_1;

	/** Der zweite Datenverteiler der Verbindung */
	private TransmitterInfo _transmitter_2;

	/** Das Gewicht der Verbindung */
	private short _connectionWeight;

	/**
	 * Der Typ dieser Verbindung 0: Ersatzverbindung 1: Normale Verbindung (Datenverteiler 1 --> Datenverteiler 2) 2: Doppelte Verbindung (Datenverteiler 1 <-->
	 * Datenverteiler 2)
	 */
	private byte _connectionType;

	/** Wenn gesetzt, dann werden bei Ausfall einer Verbindung, die Verbindungen zu den nicht mehr erreichbaren Datenverteiler etabliert. */
	private boolean _autoExchangeTransmitterDetection;

	/** Liste der Ersatz Datenverteiler */
	private TransmitterInfo[] _exchangeTransmitterList;

	/** Wartezeit bis zur Etablierung der Ersatzverbindung */
	private long _connectionTimeThreshold;

	/**
	 * Benutzername mit dem sich der erste Datenverteiler beim zweiten Datenverteiler authentifizieren soll oder leerer String, wenn der Standardbenutzer des
	 * Datenverteilers f�r die Authentifizierung benutzt werden soll.
	 */
	private String _userName;

	/**
	 * Benutzername mit dem sich der zweite Datenverteiler beim ersten Datenverteiler authentifizieren soll oder leerer String, wenn der Standardbenutzer des
	 * Datenverteilers f�r die Authentifizierung benutzt werden soll.
	 */
	private String _remoteUserName;

	/** Erzeugt ein neues Objekt, dessen Eigenschaften anschlie�end mit der read-Methode eingelesen werden sollten. */
	public TransmitterConnectionInfo() {
	}

	/**
	 * Erzeugt ein neues Objekt mit den angegebenen Eigenschaften.
	 *
	 * @param transmitter_1           Der erste Datenverteiler der Verbindung
	 * @param transmitter_2           Der zweite Datenverteiler der Verbindung
	 * @param connectionWeight        Gewicht der Verbindung
	 * @param connectionType          Typ der Verbindung
	 * @param connectionTimeThreshold Wartezeit bis zur Einleitung der Ersatzverbindung
	 * @param autoExchangeTransmitterDetection
	 *                                Ersatzverbindungen automatisch etablieren
	 * @param exchangeTransmitterList Liste der Erssatzverbindungen.
	 * @param userName                Benutzername mit dem sich der erste Datenverteiler beim zweiten Datenverteiler authentifizieren soll oder leerer String, wenn
	 *                                der Standardbenutzer des Datenverteilers f�r die Authentifizierung benutzt werden soll.
	 * @param remoteUserName          Benutzername mit dem sich der zweite Datenverteiler beim ersten Datenverteiler authentifizieren soll oder leerer String, wenn
	 *                                der Standardbenutzer des Datenverteilers f�r die Authentifizierung benutzt werden soll.
	 */
	public TransmitterConnectionInfo(
			TransmitterInfo transmitter_1,
			TransmitterInfo transmitter_2,
			short connectionWeight,
			byte connectionType,
			long connectionTimeThreshold,
			boolean autoExchangeTransmitterDetection,
			TransmitterInfo exchangeTransmitterList[],
			final String userName,
			final String remoteUserName) {
		_userName = userName;
		_remoteUserName = remoteUserName;
		_transmitter_1 = transmitter_1;
		_transmitter_2 = transmitter_2;
		_connectionWeight = connectionWeight;
		_connectionType = connectionType;
		_connectionTimeThreshold = connectionTimeThreshold;
		_autoExchangeTransmitterDetection = autoExchangeTransmitterDetection;
		_exchangeTransmitterList = exchangeTransmitterList;
	}

	/**
	 * Gibt den ersten Datenverteiler der Verbindung zur�ck.
	 *
	 * @return erster DAV
	 */
	public final TransmitterInfo getTransmitter_1() {
		return _transmitter_1;
	}

	/**
	 * Gibt den zweiten Datenverteiler der Verbindung zur�ck
	 *
	 * @return zweiter DAV
	 */
	public final TransmitterInfo getTransmitter_2() {
		return _transmitter_2;
	}

	/**
	 * Gibt die Gewichtung der Verbindung zur�ck
	 *
	 * @return Gewichtung
	 */
	public final short getWeight() {
		return _connectionWeight;
	}

	/**
	 * Diese Methode wir f�r automtisierte Tests ben�tigt.
	 *
	 * @return Art der Verbindung
	 */
	byte getType() {
		return _connectionType;
	}

	/**
	 * Gibt an, ob die Verbindung eine Ersatzverbindung ist.
	 *
	 * @return true-> Verbindung ist Ersatzverbindung.
	 */
	public final boolean isExchangeConnection() {
		return _connectionType == 0;
	}

	/**
	 * Gibt an, ob die Verbindung eine unidrektionale Verbindung von Datenverteiler 1 zum Datenverteiler 2 ist.
	 *
	 * @return true-> unidirektionale verbindung
	 */
	public final boolean isActiveConnection() {
		if(_connectionType < 2) {
			return true;
		}
		return _transmitter_1.getTransmitterId() < _transmitter_2.getTransmitterId();
	}

	/**
	 * Legt fest, ob Ersatzverbindungen automatisch etabliert werden sollen.
	 *
	 * @return True: ja, false: nein
	 */
	public final boolean isAutoExchangeTransmitterDetectionOn() {
		return _autoExchangeTransmitterDetection;
	}

	/**
	 * Gibt die Liste der Ersatzverbindungen zur�ck
	 *
	 * @return Array mit Liste der Ersatzverbindungen.
	 */
	public final TransmitterInfo[] getExchangeTransmitterList() {
		return _exchangeTransmitterList;
	}

	/**
	 * Gibt die Zeit an, ab der eine Ersatzverbindung aufzubauen ist.
	 *
	 * @return Zeit in millisekunden
	 */
	public final long getConnectionTimeThreshold() {
		return _connectionTimeThreshold;
	}

	/**
	 * Bestimmt den Benutzernamen mit dem sich der erste Datenverteiler beim zweiten Datenverteiler authentifizieren soll.
	 *
	 * @return Benutzername f�r die Authentifizierung oder leerer String, wenn der Standardbenutzer des Datenverteilers f�r die Authentifizierung benutzt werden
	 *         soll.
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Bestimmt den Benutzernamen mit dem sich der zweite Datenverteiler beim ersten Datenverteiler authentifizieren soll.
	 *
	 * @return Benutzername f�r die Authentifizierung oder leerer String, wenn der Standardbenutzer des Datenverteilers f�r die Authentifizierung benutzt werden
	 *         soll.
	 */
	public String getRemoteUserName() {
		return _remoteUserName;
	}

	/**
	 * Gibt ein String zur�ck, der diesen Datensatz beschreibt
	 *
	 * @return Beschreibung des Datensatzes
	 */
	public final String parseToString() {
		String str = "Datenverteiler Verbindungs Informationen: \n";
		str += "Von Datenverteiler: ";
		str += _transmitter_1.parseToString();
		str += "Zum Datenverteiler: ";
		str += _transmitter_2.parseToString();
		str += "Gewicht: " + _connectionWeight + "\n";
		str += "Benutzername1: " + _userName + "\n";
		str += "Benutzername2: " + _remoteUserName+ "\n";
		if(_connectionType == 0) {
			str += "Ersatzverbindung.\n";
		}
		else if(_connectionType == 1) {
			str += "Normale Verbindung.\n";
		}
		else {
			str += "Doppelte Verbindung (" + (isActiveConnection() ? "Aktiv" : "Passiv") + ").\n";
		}
		str += "Verbindungstimeout: " + _connectionTimeThreshold + "\n";
		if(_autoExchangeTransmitterDetection) {
			str += "Automatische Ermittlung der nicht mehr erreichbaren Datenverteiler ist eingeschaltet.\n";
		}
		else {
			str += "Automatische Ermittlung der nicht mehr erreichbaren Datenverteiler ist ausgeschaltet.\n";
		}
		if(_exchangeTransmitterList != null) {
			str += "Ersatzdatenverteiler: \n";
			for(int i = 0; i < _exchangeTransmitterList.length; ++i) {
				str += _exchangeTransmitterList[i].parseToString();
			}
		}
		return str;
	}

	/**
	 * Schreibt einen Datensatz in den gegebenen DataOutputStream.
	 *
	 * @param out     DataOutputStream
	 * @param version Version des �bergeordneten Antworttelegramms. Der Wert 0 kennzeichnet, dass nur die urspr�ngliche Version ohne Benutzernamen f�r die
	 *                Authentifizierung �bertragen wird; der Wert 1 kennzeichnet, dass die neue Version mit Benutzernamen f�r die Authentifizierung �bertragen
	 *                wird.
	 *
	 * @throws IOException muss geworfen werden.
	 * @see TransmitterConnectionInfoAnswer
	 */
	public final void write(DataOutputStream out, final long version) throws IOException {
		_transmitter_1.write(out);
		_transmitter_2.write(out);
		out.writeShort(_connectionWeight);
		out.writeByte(_connectionType);
		out.writeLong(_connectionTimeThreshold);
		if(version >= 1 ) {
			out.writeUTF(_userName);
		}
		if(version >= 2 ) {
			out.writeUTF(_remoteUserName);
		}
		out.writeBoolean(_autoExchangeTransmitterDetection);
		if(_exchangeTransmitterList == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(_exchangeTransmitterList.length);
			for(int i = 0; i < _exchangeTransmitterList.length; ++i) {
				_exchangeTransmitterList[i].write(out);
			}
		}
	}

	/**
	 * Liest einen Datensatz aus dem gegebenen DataInputStream
	 *
	 * @param in      DataInputStream
	 * @param version Version des �bergeordneten Antworttelegramms. Der Wert 0 kennzeichnet, dass nur die urspr�ngliche Version ohne Benutzernamen f�r die
	 *                Authentifizierung �bertragen wird; der Wert 1 kennzeichnet, dass die neue Version mit Benutzernamen f�r die Authentifizierung �bertragen
	 *                wird.
	 *
	 * @throws IOException muss geworfen werden
	 * @see TransmitterConnectionInfoAnswer
	 */
	public final void read(DataInputStream in, final long version) throws IOException {
		_transmitter_1 = new TransmitterInfo();
		_transmitter_1.read(in);
		_transmitter_2 = new TransmitterInfo();
		_transmitter_2.read(in);
		_connectionWeight = in.readShort();
		_connectionType = in.readByte();
		_connectionTimeThreshold = in.readLong();
		if(version >= 1) {
			_userName = in.readUTF();
		}
		else {
			_userName = "";
		}
		if(version >= 2) {
			_remoteUserName = in.readUTF();
		}
		else {
			_remoteUserName = "";
		}
		_autoExchangeTransmitterDetection = in.readBoolean();
		int size = in.readShort();
		if(size > 0) {
			_exchangeTransmitterList = new TransmitterInfo[size];
			for(int i = 0; i < size; ++i) {
				TransmitterInfo tmp = new TransmitterInfo();
				tmp.read(in);
				_exchangeTransmitterList[i] = tmp;
			}
		}
	}
}

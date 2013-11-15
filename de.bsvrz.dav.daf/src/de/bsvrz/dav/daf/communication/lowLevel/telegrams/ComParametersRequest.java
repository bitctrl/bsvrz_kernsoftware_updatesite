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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Die Verbindungsparameter zwischen Datenverteiler-Applikationsfunktionen und Datenverteiler (Timeout-Zeiten und Durchsatzpr�fparameter) werden verhandelt.
 * Dabei werden Standardparameter oder von der Applikation vorgegebene Parameter als Vorschl�ge an den Datenverteiler �bertragen. Dieser trifft eine Festlegung
 * der Parameter und �bertr�gt das Ergebnis zur�ck an die Applikation.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class ComParametersRequest extends DataTelegram {

	/** Die Zeit nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in diese Zeit kein Telegramm empfangen wurde. */
	private long keepAliveSendTimeOut;

	/** Die Zeit in der sp�testens ein Telegramm empfangen werden muss, sonst wird die verbindung terminiert. */
	private long keepAliveReceiveTimeOut;

	/** Prozentuale Anteil der Cache, um die Flusscontrole zu starten. */
	public byte cacheThresholdPercentage;

	/** Die Zeit zwichen zwei Durchsatzpr�fungen */
	public short flowControlThresholdTime;

	/** Minimum Verbindungsdurchsatz */
	public int minConnectionSpeed;


	public ComParametersRequest() {
		type = COM_PARAMETER_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * @param _keepAliveSendTimeOut     Keepalivesendezeit
	 * @param _keepAliveReceiveTimeOut  Keepaliveempfangszeit
	 * @param _cacheThresholdPercentage Pufferf�llgrad
	 * @param _flowControlThresholdTime Pr�fintervall
	 * @param _minConnectionSpeed       Mindestdurchsatz
	 */
	public ComParametersRequest(
			long _keepAliveSendTimeOut, long _keepAliveReceiveTimeOut, byte _cacheThresholdPercentage, short _flowControlThresholdTime, int _minConnectionSpeed
	) {
		type = COM_PARAMETER_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		keepAliveSendTimeOut = _keepAliveSendTimeOut;
		keepAliveReceiveTimeOut = _keepAliveReceiveTimeOut;
		cacheThresholdPercentage = _cacheThresholdPercentage;
		flowControlThresholdTime = _flowControlThresholdTime;
		minConnectionSpeed = _minConnectionSpeed;
		length = 23;
	}

	/**
	 * Gibt den prozentualen Anteil der Cache, um die Flusscontrole zu starten, an.
	 *
	 * @return Pufferf�llgrad
	 */
	public final byte getCacheThresholdPercentage() {
		return cacheThresholdPercentage;
	}

	/**
	 * Gibt die Zeit zwichen zwei Durchsatzpr�fungen an.
	 *
	 * @return Pr�fintervall
	 */
	public final short getFlowControlThresholdTime() {
		return flowControlThresholdTime;
	}

	/**
	 * Gibt den minimum Verbindungsdurchsatz an.
	 *
	 * @return Mindestdurchsatz
	 */
	public final int getMinConnectionSpeed() {
		return minConnectionSpeed;
	}

	/**
	 * Gibt die Zeit an, nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in diese Zeit kein Telegramm empfangen wurde.
	 *
	 * @return die Keepalive-Sendezeit
	 */
	public final long getKeepAliveSendTimeOut() {
		return keepAliveSendTimeOut;
	}

	/**
	 * Gibt die Zeit an, in der sp�testens ein Telegramm empfangen werden muss, sonst wird die verbindung terminiert.
	 *
	 * @return die Keepalive-Empfangszeit
	 */
	public final long getKeepAliveReceiveTimeOut() {
		return keepAliveReceiveTimeOut;
	}

	public final String parseToString() {
		String str = "Systemtelegramm setzen der Kommunikationsparametern Anfrage: \n";
		str += "Keep Alive Sendezeit    : " + keepAliveSendTimeOut + "\n";
		str += "Keep Alive Empfangszeit : " + keepAliveReceiveTimeOut + "\n";
		str += "Pufferf�llgrad: " + cacheThresholdPercentage + " % \n";
		str += "Pr�fintervall: " + flowControlThresholdTime + " s \n";
		str += "Mindestdurchsatz: " + minConnectionSpeed + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeLong(keepAliveSendTimeOut);
		out.writeLong(keepAliveReceiveTimeOut);
		out.writeByte(cacheThresholdPercentage);
		out.writeShort(flowControlThresholdTime);
		out.writeInt(minConnectionSpeed);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		keepAliveSendTimeOut = in.readLong();
		keepAliveReceiveTimeOut = in.readLong();
		cacheThresholdPercentage = in.readByte();
		flowControlThresholdTime = in.readShort();
		minConnectionSpeed = in.readInt();
		length = 23;
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}

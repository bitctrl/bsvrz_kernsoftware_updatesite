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

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * In diesem Systemtelegramm werden die vom Datenverteiler festgelegten Verbindungsparameter an die Applikationsfunktionen übertragen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ComParametersAnswer extends DataTelegram {

	/** Zeit, nach der spätestens ein Keep-Alive-Telegramm versendet wird, wenn keine sonstigen Telegramme während dieser Zeit versendet wurden. */
	private long keepAliveSendTimeOut;

	/**
	 * Timeoutzeit, in der spätestens ein Telegramm empfangen werden muss. Wird diese Zeit ohne Empfang von Telegrammen überschritten, wird die Verbindung zum
	 * Kommunikationspartner terminiert.
	 */
	private long keepAliveReceiveTimeOut;

	/** Belegung des Sendepuffers in Prozent ab der zyklisch die Durchsatzprüfung erfolgen soll. */
	public byte cacheThresholdPercentage;

	/**
	 * Messintervall zur Bestimmung des Durchsatzes in Sekunden. Ist der ermittelte Durchsatz kleiner als der MindestDurchsatz wird die Verbindung zum
	 * Datenverteiler terminiert.
	 */
	public short flowControlThresholdTime;

	/** Mindestdurchsatz in Bytes pro Sekunde */
	public int minConnectionSpeed;

	public ComParametersAnswer() {
		type = COM_PARAMETER_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Creates new CommunicationParametersAnswer
	 *
	 * @param _keepAliveSendTimeOut     Keepalive-Sendezeit
	 * @param _keepAliveReceiveTimeOut  Keepalive-Empfangszeit
	 * @param _cacheThresholdPercentage Pufferfüllgrad
	 * @param _flowControlThresholdTime Prüfintervall
	 * @param _minConnectionSpeed       Mindestdurchsatz
	 */
	public ComParametersAnswer(
			long _keepAliveSendTimeOut, long _keepAliveReceiveTimeOut, byte _cacheThresholdPercentage, short _flowControlThresholdTime, int _minConnectionSpeed
	) {
		type = COM_PARAMETER_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		keepAliveSendTimeOut = _keepAliveSendTimeOut;
		keepAliveReceiveTimeOut = _keepAliveReceiveTimeOut;
		cacheThresholdPercentage = _cacheThresholdPercentage;
		flowControlThresholdTime = _flowControlThresholdTime;
		minConnectionSpeed = _minConnectionSpeed;
		length = 23;
	}

	/**
	 * Gibt den prozentualen Anteil der Cache, um die Flusscontrole zu starten, zurück.
	 *
	 * @return Pufferfüllgrad
	 */
	public final byte getCacheThresholdPercentage() {
		return cacheThresholdPercentage;
	}

	/**
	 * Gibt die Zeit zwichen zwei Durchsatzprüfungen zurück.
	 *
	 * @return Prüfintervall
	 */
	public final short getFlowControlThresholdTime() {
		return flowControlThresholdTime;
	}

	/**
	 * Gibt den minimum Verbindungsdurchsatz zurück.
	 *
	 * @return Mindestdurchsatz
	 */
	public final int getMinConnectionSpeed() {
		return minConnectionSpeed;
	}

	/**
	 * Gibt die Zeit zurück, nach der spätestens ein keepalive Telegramm geschickt werden muss, wenn in diese Zeit kein Telegramm empfangen wurde.
	 *
	 * @return die Keepalive-Sendezeit
	 */
	public final long getKeepAliveSendTimeOut() {
		return keepAliveSendTimeOut;
	}

	/**
	 * Gibt die Zeit zurück, in der spätestens ein Telegramm empfangen werden muss, sonst wird die verbindung terminiert.
	 *
	 * @return die Keepalive-Empfangszeit
	 */
	public final long getKeepAliveReceiveTimeOut() {
		return keepAliveReceiveTimeOut;
	}

	public final String parseToString() {
		String str = "Systemtelegramm setzen der Kommunikationsparametern Antwort: \n";
		str += "Keep Alive Sendezeit    : " + keepAliveSendTimeOut + "\n";
		str += "Keep Alive Empfangszeit : " + keepAliveReceiveTimeOut + "\n";
		str += "Pufferfüllgrad: " + cacheThresholdPercentage + " % \n";
		str += "Prüfintervall: " + flowControlThresholdTime + " s \n";
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
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}

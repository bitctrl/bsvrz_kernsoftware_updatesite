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
 * Verhandlung der Verbindungsparameter (Server). In diesem Systemtelegramm werden die vom Datenverteiler festgelegten Verbindungsparameter �bertragen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class TransmitterComParametersAnswer extends DataTelegram {

	/** Die Zeit nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde. */
	private long _keepAliveSendTimeOut;

	/** Die Zeit in der sp�testens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert. */
	private long _keepAliveReceiveTimeOut;

	public TransmitterComParametersAnswer() {
		type = TRANSMITTER_COM_PARAMETER_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues CommunicationParametersAnswer-Telegramm.
	 *
	 * @param keepAliveSendTimeOut    Keepalivesendezeit
	 * @param keepAliveReceiveTimeOut Keepaliveempfangszeit
	 */
	public TransmitterComParametersAnswer(long keepAliveSendTimeOut, long keepAliveReceiveTimeOut) {
		type = TRANSMITTER_COM_PARAMETER_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
		length = 16;
	}

	/**
	 * Gibt die Zeit zur�ck, nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde.
	 *
	 * @return die Keepalivesendezeit
	 */
	public final long getKeepAliveSendTimeOut() {
		return _keepAliveSendTimeOut;
	}

	/**
	 * Gibt die Zeit zur�ck, in der sp�testens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert.
	 *
	 * @return die Keepaliveempfangszeit
	 */
	public final long getKeepAliveReceiveTimeOut() {
		return _keepAliveReceiveTimeOut;
	}

	public final String parseToString() {
		String str = "Systemtelegramm setzen der Kommunikationsparametern Antwort: \n";
		str += "Keep Alive Sendezeit    : " + _keepAliveSendTimeOut + "\n";
		str += "Keep Alive Empfangszeit : " + _keepAliveReceiveTimeOut + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeLong(_keepAliveSendTimeOut);
		out.writeLong(_keepAliveReceiveTimeOut);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_keepAliveSendTimeOut = in.readLong();
		_keepAliveReceiveTimeOut = in.readLong();
		length = 16;
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}

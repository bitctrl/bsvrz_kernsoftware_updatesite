/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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
 * Terminierung der Verbindung In diesem Systemtelegramm teilt der Datenverteiler seinem Kommunikationspartner mit, dass die Verbindung sofort terminiert wird.
 * Die Ursache für den vom Datenverteiler veranlassten Verbindungsabbruch kann als Text mit dem Telegramm übertragen werden. Die Verbindung kann mit diesem
 * Systemtelegramm von beiden Kommunikationspartnern terminiert werden, wenn beispielsweise ein Fehler erkannt wurde, der zum Abruch der Verbindung führt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class TransmitterTerminateOrderTelegram extends DataTelegram {

	/** Die Ursache des Terminierungsbefehls */
	private String terminateOrderCause;

	public TransmitterTerminateOrderTelegram() {
		type = TRANSMITTER_TERMINATE_ORDER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	public TransmitterTerminateOrderTelegram(String _terminateOrderCause) {
		type = TRANSMITTER_TERMINATE_ORDER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		terminateOrderCause = _terminateOrderCause;
		length = 0;
		try {
			if(terminateOrderCause == null) {
				terminateOrderCause = "";
			}
			length += terminateOrderCause.getBytes("UTF-8").length + 2;
		}
		catch(java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage());
		}
	}

	/**
	 * Gibt die Ursache des Terminierungsbefehls zurück
	 *
	 * @return die Ursache des Terminierungsbefehls
	 */
	public final String getCause() {
		return terminateOrderCause;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Terminierungsbefehl: \n";
		str += "Ursache : " + terminateOrderCause + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeUTF(terminateOrderCause);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		terminateOrderCause = in.readUTF();
		length = terminateOrderCause.getBytes("UTF-8").length + 2;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}

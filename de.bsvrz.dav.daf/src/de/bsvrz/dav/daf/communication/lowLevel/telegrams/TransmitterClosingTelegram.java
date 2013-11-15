/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
 * Abmeldung einer Datenverteiler-Datenverteiler-Verbindung. In diesem Systemtelegramm teilt ein Datenverteiler seinem Kommunikationspartner mit, dass die
 * Verbindung zwischen den beiden Datenverteilern abgebaut wird. Dieses Telegramm wird z.B. verwendet, wenn die Verbindung zu einem Ersatznachbardatenverteiler
 * wieder abgebaut werden kann, weil der Nachbardatenverteiler wieder erreichbar ist. Sämtliche in der Datenverteiler-Verbindung gespeicherten Informationen und
 * noch nicht verarbeitete gepufferte Datentelegramme werden gelöscht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class TransmitterClosingTelegram extends DataTelegram {

	/** Erzeugt neues TransmitterClosingTelegram */
	public TransmitterClosingTelegram() {
		type = TRANSMITTER_CLOSING_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		length = 0;
	}

	public final String parseToString() {
		return "Systemtelegramm Schliessung der Datenverteiler: \n";
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		length = 0;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}

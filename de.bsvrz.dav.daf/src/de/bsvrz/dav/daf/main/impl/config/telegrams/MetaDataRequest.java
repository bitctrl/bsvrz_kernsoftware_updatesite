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
 * Diese Klasse stellt ein Metadaten-Anfragetelegramm dar. Es werden die Metadaten angefragt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13141 $
 */
public class MetaDataRequest extends ConfigTelegram {

	/** Version des Kommunikationsprotokolls, das von der Konfiguration unterst�tzt wird */
	private long _protocolVersion;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */

	public MetaDataRequest() {
		_type = META_DATA_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param protocolVersion    Protokollversion des Clients
	 */
	public MetaDataRequest(long protocolVersion) {
		_type = META_DATA_REQUEST_TYPE;
		_protocolVersion = protocolVersion;
	}

	/**
	 * Gibt die Protokollversion des Clients zur�ck. Sehr alte Clients haben hier die Protokollzeit verschickt.
	 *
	 * @return Die Protokollversion des Clients
	 */
	public final long getProtocolVersion() {
		return _protocolVersion;
	}

	public final String parseToString() {
		String str = "Metakonfigurationsanfrage: \n";
		str += "Protokollversion: " + _protocolVersion + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_protocolVersion);
	}

	public final void read(DataInputStream in) throws IOException {
		_protocolVersion = in.readLong();
		if(_protocolVersion > Integer.MAX_VALUE || _protocolVersion < 0){
			_protocolVersion = 0;
		}
	}
}

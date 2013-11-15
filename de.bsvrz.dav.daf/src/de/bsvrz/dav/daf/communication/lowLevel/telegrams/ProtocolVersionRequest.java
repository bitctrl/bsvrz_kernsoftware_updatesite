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
 * Die Datenverteiler-Applikationsfunktionen senden dem Datenverteiler eine priorisierte Liste von unterst�tzten Versionsnummern aus denen der Datenverteiler im
 * Normalfall die Version der h�chsten Priorit�t ausw�hlt, die auch vom Datenverteiler unterst�tzt wird. Diese wird dann zu den
 * Datenverteiler-Applikationsfunktionen gesendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class ProtocolVersionRequest extends DataTelegram {

	/** Die Liste der Versionen */
	private int versions[];

	public ProtocolVersionRequest() {
		type = PROTOCOL_VERSION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Creates new ProtocolVersionRequest
	 *
	 * @param _versions Liste der Versionen
	 */
	public ProtocolVersionRequest(int _versions[]) {
		type = PROTOCOL_VERSION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		versions = _versions;
		length = 4;
		if(versions != null) {
			length += versions.length * 4;
		}
	}

	/**
	 * Gibt die unterst�tzte Versionen zur�ck
	 *
	 * @return Eine Liste der unterst�tzten Versionen
	 */
	public final int[] getVersions() {
		if(versions == null) {
			return null;
		}
		int _versions[] = new int[versions.length];
		System.arraycopy(versions, 0, _versions, 0, versions.length);
		return _versions;
	}

	/**
	 * Gibt die h�hste Version zur�ck
	 *
	 * @return die h�hste Version
	 */
	public final int getPreferredVersion() {
		if(versions == null) {
			return -1;
		}
		return versions[0];
	}

	public final String parseToString() {
		String str = "Systemtelegramm VersionsProtokoll Anfrage: \n";
		str += "Unterst�tzte Versionen     : ";
		if(versions != null) {
			for(int i = 0; i < versions.length; ++i) {
				str += versions[i];
				if(i < versions.length - 1) {
					str += " , ";
				}
			}
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		if(versions == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(versions.length);
			for(int i = 0; i < versions.length; ++i) {
				out.writeInt(versions[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		length = 4;
		int size = in.readInt();
		if(size > 0) {
			versions = new int[size];
			for(int i = 0; i < size; ++i) {
				versions[i] = in.readInt();
			}
			length += versions.length * 4;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}

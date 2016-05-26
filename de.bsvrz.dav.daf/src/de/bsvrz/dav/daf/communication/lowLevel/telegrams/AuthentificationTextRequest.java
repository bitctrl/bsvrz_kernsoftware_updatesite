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
 * Mit diesem Systemtelegramm teilt die Applikation mit, dass sie sich beim Datenverteiler anmelden möchte. Dabei informiert die Applikation den Datenverteiler
 * über ihren Typ und Namen.
 *
 * @author Kappich Systemberatung
 */
public class AuthentificationTextRequest extends DataTelegram {

	/** Der Applikationsname */
	private String applicationName;

	/** Die PID der Applikationstyp */
	private String applicationTypePid;

	/** Die PID der Konfiguration */
	private String configurationPid;

	public AuthentificationTextRequest() {
		type = AUTHENTIFICATION_TEXT_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * @param _applicationName      Applikationsname
	 * @param _applicationTypePid   PID des Applikationstypen
	 * @param _configurationPid     PID der Konfiguration
	 */
	public AuthentificationTextRequest(String _applicationName, String _applicationTypePid, String _configurationPid) {
		type = AUTHENTIFICATION_TEXT_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		applicationName = _applicationName;
		applicationTypePid = _applicationTypePid;
		configurationPid = _configurationPid;
		length = 0;
		try {
			if(applicationName == null) {
				applicationName = "";
			}
			length += applicationName.getBytes("UTF-8").length + 2;
			if(applicationTypePid == null) {
				applicationTypePid = "";
			}
			length += applicationTypePid.getBytes("UTF-8").length + 2;
			if(configurationPid == null) {
				configurationPid = "";
			}
			length += configurationPid.getBytes("UTF-8").length + 2;
		}
		catch(java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage());
		}
	}

	/**
	 * Ermittelt den Applikationsnamen
	 *
	 * @return Applikationsname
	 */
	public final String getApplicationName() {
		return applicationName;
	}

	/**
	 * Ermittelt die PID des Applikationstypen
	 *
	 * @return Applikationstyp PID
	 */
	public final String getApplicationTypePid() {
		return applicationTypePid;
	}

	/**
	 * Ermittelt die Pid der Konfiguration
	 *
	 * @return PID der Konfiguration
	 */
	public final String getConfigurationPid() {
		return configurationPid;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikationsschlussel Anfrage: \n";
		str += "Applikationsname    : " + applicationName + "\n";
		str += "Applikationstyp Pid : " + applicationTypePid + "\n";
		str += "Konfiguration Pid    : " + configurationPid + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		if(applicationTypePid != null) {
			out.writeUTF(applicationTypePid);
		}
		if(applicationName != null) {
			out.writeUTF(applicationName);
		}
		if(configurationPid != null) {
			out.writeUTF(configurationPid);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		applicationTypePid = in.readUTF();
		applicationName = in.readUTF();
		configurationPid = in.readUTF();
		length = 0;
		if(applicationName != null) {
			length += applicationName.getBytes("UTF-8").length + 2;
		}
		if(applicationTypePid != null) {
			length += applicationTypePid.getBytes("UTF-8").length + 2;
		}
		if(configurationPid != null) {
			length += configurationPid.getBytes("UTF-8").length + 2;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}


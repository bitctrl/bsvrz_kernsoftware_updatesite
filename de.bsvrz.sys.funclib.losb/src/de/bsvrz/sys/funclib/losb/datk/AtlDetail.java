/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.Data;

import java.io.Serializable;

/**
 * Attributliste {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atlDetailInformation}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class AtlDetail implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8701829452245689378L;

	public long version;

	public String fileName;

	public byte[] checksum;

	/**
	 * Erzeugt ein Stellvertreter-Objekt für die Attributliste
	 *
	 * @param version  Versionsnummer
	 * @param fileName Dateiname des Skriptquelltexts
	 * @param checksum Checksumme des Skriptquelltexts
	 */
	public AtlDetail(long version, String fileName, byte[] checksum) {
		this.version = version;
		this.fileName = fileName;
		this.checksum = checksum;
	}

	/** Erzeugt ein Stellvertreter-Objekt für die Attributliste mit Standardwerten */
	public AtlDetail() {
		this(1, "", new byte[0]);
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(Data data) {
		data.getUnscaledValue(PidScript.version).set(version);
		data.getTextValue(PidScript.fileName).setText(fileName);

		Data.NumberArray array = data.getUnscaledArray(PidScript.checksum);
		array.set(checksum);
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlDetail getJavaObject(Data data) {
		byte[] checksum = data.getUnscaledArray(PidScript.checksum).getByteArray();
		return new AtlDetail(
				data.getUnscaledValue(PidScript.version).longValue(), data.getTextValue(PidScript.fileName).getText(), checksum
		);
	}

	/** @see java.lang.Object#toString() */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Dateiname auf dem Server: " + fileName);
		sb.append("\nAktuelle Version: " + version);
		return sb.toString();
	}
}

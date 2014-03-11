/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.Data;

import java.io.Serializable;

/**
 * Attributliste {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atlBase}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class AtlBase implements Serializable {

	private static final long serialVersionUID = 4660934449061647024L;

	public String name;

	public String description;

	public String author;

	public String status;

	public String date;

	/**
	 * Erzeugt ein Stellvertreter-Objekt für diese Attributliste
	 *
	 * @param name        Name des Skripts
	 * @param description Beschreibung des Skripts
	 * @param author      Urheber des Skripts
	 * @param status      Status des Skripts
	 * @param date        Erstellungsdatum
	 */
	public AtlBase(String name, String description, String author, String status, String date) {
		this.name = name;
		this.description = description;
		this.author = author;
		this.status = status;
		this.date = date;
	}

	/** Erzeugt ein Stellvertreter-Objekt für diese Attributliste mit Standardwerten. */
	public AtlBase() {
		this("", "", "", "", "");
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(Data data) {
		data.getTextValue(PidScript.name).setText(name);
		data.getTextValue(PidScript.author).setText(author);
		data.getTextValue(PidScript.status).setText(status);
		data.getTextValue(PidScript.description).setText(description);
		data.getTextValue(PidScript.date).setText(date);
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlBase getJavaObject(Data data) {
		return new AtlBase(
				data.getTextValue(PidScript.name).getText(),
				data.getTextValue(PidScript.description).getText(),
				data.getTextValue(PidScript.author).getText(),
				data.getTextValue(PidScript.status).getText(),
				data.getTextValue(PidScript.date).getText()
		);
	}

	/** @see java.lang.Object#toString() */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name:\t" + name);
		sb.append("\nBeschreibung:\t" + description);
		sb.append("\nAutor:\t" + author);
		sb.append("\nDatum:\t" + date);
		sb.append("\nStatus:\t" + status);

		return sb.toString();
	}
}

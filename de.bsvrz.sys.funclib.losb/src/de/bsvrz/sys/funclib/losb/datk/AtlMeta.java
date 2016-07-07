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
import java.util.*;

/**
 * Attributliste {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atlMetaInformation}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class AtlMeta implements Serializable {

	private static final long serialVersionUID = -567304248697867734L;

	public AtlBase atlBase;

	public AtlDetail atlDetail;

	public List<AtlVariable> atlVariables;

	public List<AtlVersion> atlVersions;


	/**
	 * Erzeugt ein Stellvertreter Objekt für diese Attributliste
	 *
	 * @param atlbase      Attributliste {@link PidScript#atlBase}
	 * @param atlDetail    Attributliste {@link PidScript#atlDetailInformation}
	 * @param atlVariables Attributliste {@link PidScript#atlVariable}
	 * @param atlVersions  Attributliste {@link PidScript#atlVersion}
	 */
	public AtlMeta(final AtlBase atlbase, final AtlDetail atlDetail, final List<AtlVariable> atlVariables, final List<AtlVersion> atlVersions) {
		this.atlBase = atlbase;
		this.atlDetail = atlDetail;
		this.atlVariables = atlVariables;
		this.atlVersions = atlVersions;
	}

	/** Erzeugt ein Stellvertreter-Objekt mit Standardwerten. */
	public AtlMeta() {
		this(new AtlBase(), new AtlDetail(), new ArrayList<AtlVariable>(), new ArrayList<AtlVersion>());
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(final Data data) {
		atlBase.build(data.getItem(PidScript.atlBase));
		atlDetail.build(data.getItem(PidScript.atlDetailInformation));

		data.getArray(PidScript.atlVariable).setLength(atlVariables.size());
		int i = 0;
		for(final AtlVariable variable : atlVariables) {
			variable.build(data.getArray(PidScript.atlVariable).getItem(i));
			i++;
		}

		i = 0;
		data.getArray(PidScript.atlVersion).setLength(atlVersions.size());
		for(final AtlVersion version : atlVersions) {
			version.build(data.getArray(PidScript.atlVersion).getItem(i));
			i++;
		}
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten. Es kann eine {@link java.util.NoSuchElementException}geworfen werden.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlMeta getJavaObject(final Data data) {
		AtlMeta atlMeta = new AtlMeta();

		final Data.Array atlVariableArray = data.getArray(PidScript.atlVariable);
		for(int i = 0; i < atlVariableArray.getLength(); i++) {
			atlMeta.atlVariables.add(AtlVariable.getJavaObject(atlVariableArray.getItem(i)));
		}

		final Data.Array atlVersionArray = data.getArray(PidScript.atlVersion);
		for(int i = 0; i < atlVersionArray.getLength(); i++) {
			atlMeta.atlVersions.add(AtlVersion.getJavaObject(atlVersionArray.getItem(i)));
		}

		atlMeta = new AtlMeta(
				AtlBase.getJavaObject(data.getItem(PidScript.atlBase)),
				AtlDetail.getJavaObject(data.getItem(PidScript.atlDetailInformation)),
				atlMeta.atlVariables,
				atlMeta.atlVersions
		);

		return atlMeta;
	}

	/**
	 * Übernimmt die Informationen aus dem übergebenen Meta-Objekt.
	 *
	 * @param meta Informationen über ein Skript
	 */
	public void set(final AtlMeta meta) {
		this.atlBase = meta.atlBase;
		this.atlDetail = meta.atlDetail;
		this.atlVariables = meta.atlVariables;
		this.atlVersions = meta.atlVersions;
	}

	/** @see java.lang.Object#toString() */
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(atlBase.toString()).append("\n");
		sb.append(atlDetail.toString()).append("\n");
		for(final AtlVersion atlVersion : atlVersions) {
			sb.append(atlVersion).append("\n");
		}

		return sb.toString();
	}
}

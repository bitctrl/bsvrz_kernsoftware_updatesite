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
 * Attributliste {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atlVariable}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class AtlVariable implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3335945926687488884L;

	public String objectType;

	public String attributeGroup;

	public String aspect;

	/** Erzeugt ein Stellvertreter Objekt mit Standardwerten. */
	public AtlVariable() {
		this("", "", "");
	}

	/**
	 * Erzeugt ein Stellvertreter-Objekt dieser Attributliste.
	 *
	 * @param objectType     Objekttyp
	 * @param attributeGroup Attributgruppe
	 * @param aspect         Aspekt
	 */
	public AtlVariable(String objectType, String attributeGroup, String aspect) {
		this.objectType = objectType;
		this.attributeGroup = attributeGroup;
		this.aspect = aspect;
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(Data data) {
		data.getTextValue(PidScript.objectType).setText(objectType);
		data.getTextValue(PidScript.attributeGroup).setText(attributeGroup);
		data.getTextValue(PidScript.aspect).setText(aspect);
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlVariable getJavaObject(Data data) {
		return new AtlVariable(
				data.getTextValue(PidScript.objectType).getText(),
				data.getTextValue(PidScript.attributeGroup).getText(),
				data.getTextValue(PidScript.aspect).getText()
		);
	}
}

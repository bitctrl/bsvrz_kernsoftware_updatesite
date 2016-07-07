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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

import java.io.Serializable;

/**
 * Attributgruppe {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atgRequest}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class AtgScriptRequest implements Serializable {

	private static final long serialVersionUID = -3593110168874242787L;

	public AtlBase atlBase = null;

	public AtlExtra atlExtra = null;

	/**
	 * Erzeugt ein Data-Objekt für die Attributgruppe.
	 *
	 * @param dav   Verbindung zum Datenverteiler
	 * @param base  Attributliste {@link PidScript#atlBase}
	 * @param extra Attributliste {@link PidScript#atlExtendedInformation}
	 *
	 * @throws ConfigurationException Falls das Data-Objekt nicht erzeugt werden konnte, weil sie nicht im Datenkatalog eingetragen wurde.
	 * @return Mit den Parameterwerten belegtes Data Objekt
	 */
	public static Data build(ClientDavInterface dav, AtlBase base, AtlExtra extra) throws ConfigurationException {
		Data data = dav.createData(dav.getDataModel().getAttributeGroup(PidScript.atgRequest));

		base.build(data.getItem(PidScript.atlBase));
		extra.build(data.getItem(PidScript.atlExtendedInformation));

		return data;
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtgScriptRequest getJavaObject(Data data) {
		AtgScriptRequest atgRequest = new AtgScriptRequest();

		atgRequest.atlBase = AtlBase.getJavaObject(data.getItem(PidScript.atlBase));
		atgRequest.atlExtra = AtlExtra.getJavaObject(data.getItem(PidScript.atlExtendedInformation));

		return atgRequest;
	}
}

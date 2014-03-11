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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

import java.io.Serializable;

/**
 * Attributgruppe {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atgAnswer}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 8861 $ / $Date: 2011-02-25 14:19:03 +0100 (Fr, 25 Feb 2011) $ / ($Author: jh $)
 */
public class AtgAnswer implements Serializable {

	private static final long serialVersionUID = 6249708435634025800L;

	public AtlExtra atlExtra;

	public AtlMeta atlMeta;

	public String message;

	public AtgAnswer(AtlExtra extra, AtlMeta meta, String message) {
		this.atlExtra = extra;
		this.atlMeta = meta;
		this.message = message;
	}

	/**
	 * Erzeugt ein Data-Objekt für die Attributgruppe.
	 *
	 * @param dav     Verbindung zum Datenverteiler
	 * @param extra   Attributliste {@link PidScript#atlExtendedInformation}
	 * @param meta    Attributliste {@link PidScript#atlMetaInformation}
	 * @param message {@link PidScript#message}
	 *
	 * @throws ConfigurationException Falls das Data-Objekt nicht erzeugt werden konnte, weil sie nicht im Datenkatalog eingetragen wurde.
	 * @return Mit den Parameterwerten belegtes Data Objekt
	 */
	public static Data build(ClientDavInterface dav, AtlExtra extra, AtlMeta meta, String message) throws ConfigurationException {
		if(dav == null) throw new IllegalArgumentException("dav ist null");
		if(extra == null) throw new IllegalArgumentException("extra ist null");
		if(meta == null) throw new IllegalArgumentException("meta ist null");
		if(message == null) throw new IllegalArgumentException("message ist null");

		Data data = dav.createData(dav.getDataModel().getAttributeGroup(PidScript.atgAnswer));

		extra.build(data.getItem(PidScript.atlExtendedInformation));
		meta.build(data.getItem(PidScript.atlMetaInformation));

		data.getTextValue(PidScript.message).setText(message);

		return data;
	}

	public static AtgAnswer getJavaObject(Data data) {
		AtlExtra extra = AtlExtra.getJavaObject(data.getItem(PidScript.atlExtendedInformation));
		AtlMeta meta = AtlMeta.getJavaObject(data.getItem(PidScript.atlMetaInformation));
		String message = data.getTextValue(PidScript.message).getText();
		return new AtgAnswer(extra, meta, message);
	}

	/**
	 * Erzeugt ein passendes Data-Objekt.
	 *
	 * @throws ConfigurationException Falls das Data Objekt nicht erzugt werden konnte.
	 * @return Data Objekt.
	 */
	public Data toData(ClientDavInterface dav) throws ConfigurationException {
		return build(dav, atlExtra, atlMeta, message);
	}
}

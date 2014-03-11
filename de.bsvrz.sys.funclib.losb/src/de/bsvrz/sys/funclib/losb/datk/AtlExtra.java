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
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.io.Serializable;

/**
 * Attributliste {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atlExtendedInformation}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 8433 $ / $Date: 2010-12-13 11:40:26 +0100 (Mo, 13 Dez 2010) $ / ($Author: jh $)
 */
public class AtlExtra implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6210372274506936178L;

	public SystemObject sender;

	public long requestId;

	public int opCode;

	public String source;

	private final AtlDefaults _atlDefaults;

	/**
	 * Erzeugt ein Stellvertreter Objekt für die Attributliste
	 *
	 * @param sender      Absender
	 * @param requestId   Auftragskennung
	 * @param opCode      Operationscode
	 * @param source      Quellcode des Skripts
	 * @param atlDefaults Standardwerte des Skripts (Es bringt nichts, diesen Parameter beim Erzeugen eines Skripts zu füllen, dieser Konstruktor sollte
	 *                    sinnvollerweise von PuA benutzt werden, um dem Client die Standardwerte eines Skriptes zu liefern)
	 */
	public AtlExtra(SystemObject sender, long requestId, int opCode, String source, final AtlDefaults atlDefaults) {
		this.sender = sender;
		this.requestId = requestId;
		this.opCode = opCode;
		this.source = source;
		_atlDefaults = atlDefaults;
	}

	/**
	 * Erzeugt ein Stellvertreter Objekt für die Attributliste
	 *
	 * @param sender    Absender
	 * @param requestId Auftragskennung
	 * @param opCode    Operationscode
	 * @param source    Quellcode des Skripts
	 */
	public AtlExtra(SystemObject sender, long requestId, int opCode, String source) {
		this.sender = sender;
		this.requestId = requestId;
		this.opCode = opCode;
		this.source = source;
		_atlDefaults = null;
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(Data data) {
		data.getReferenceValue(PidScript.sender).setSystemObject(sender);
		data.getUnscaledValue(PidScript.requestId).set(requestId);
		data.getUnscaledValue(PidScript.operationCode).set(opCode);
		data.getTextValue(PidScript.source).setText(source);
		if(_atlDefaults != null){
			final Data.Array array = data.getArray(PidScript.defaults);
			array.setLength(1);
			_atlDefaults.build(array.getItem(0));
		}
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlExtra getJavaObject(Data data) {
		Data defaults = null;
		try{
			final Data.Array array = data.getArray(PidScript.defaults);
			if(array.getLength() == 1){
				defaults = array.getItem(0);
			}
		}
		catch(Exception ignored){
			// Bei altem Datenmodell klappt der zugriff nicht, dann wird defaults einfach auf null gelassen...
		}
		return new AtlExtra(
				data.getReferenceValue(PidScript.sender).getSystemObject(),
				data.getUnscaledValue(PidScript.requestId).longValue(),
				data.getUnscaledValue(PidScript.operationCode).intValue(),
				data.getTextValue(PidScript.source).getText(),
		        defaults == null ? null : AtlDefaults.getJavaObject(defaults)
		);
	}

	/**
	 * Gibt, falls vorhanden, die Standardwerte des Skripts in diesem Datensatz zurück.
	 * @return Die Standardwerte des Skripts oder null.
	 */
	public AtlDefaults getDefaults() {
		return _atlDefaults;
	}
}

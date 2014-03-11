/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.util;

/**
 * Diese Klasse stellt einen Eintrag in der Auflistung der Abhängigkeiten dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11591 $
 */
public class ConfigAreaDependency {

	/** version in der die Abhängigkeit entstand. */
	final private short _dependentVersion;

	/** Benötigte Version. */
	final private short _neededVersion;

	/** Bereich zu dem eine Abhängigkeit besteht. */
	final private String _dependentPid;

	final private String _type;

	/**
	 * Gibt die Version zurück, in der die Abhängigkeit entstand.
	 *
	 * @return Version, in der die Abhängigkeit
	 */
	public short getDependentVersion() {
		return _dependentVersion;
	}

	/**
	 * gibt die benötigte Version zurück.
	 *
	 * @return die benötigte Version
	 */
	public short getNeededVersion() {
		return _neededVersion;
	}

	/**
	 * Pid des Bereiches zu dem eine Abhängigkeit besteht
	 *
	 * @return Bereich zu dem eine Abhängigkeit besteht
	 */
	public String getDependentPid() {
		return _dependentPid;
	}

	/**
	 * Gibt die kennung zurück.
	 *
	 * @return Kennung
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Erstellt einen neuen Eintrag der Abhängigkeiten manuell.
	 *
	 * @param dependentVersion abhängige version
	 * @param neededVersion    benötigte version
	 * @param dependentPid     bereich zu dem eine Abhängigkeit besteht
	 * @param type             Kennung
	 */
	public ConfigAreaDependency(final short dependentVersion, final short neededVersion, final String dependentPid, final byte type) {
		_dependentVersion = dependentVersion;
		_neededVersion = neededVersion;
		_dependentPid = dependentPid;
		if(type == 1) {
			_type = "notwendig";
		}
		else if(type == 2) {
			_type = "optional";
		}
		else {
			_type = null;
		}
	}
}

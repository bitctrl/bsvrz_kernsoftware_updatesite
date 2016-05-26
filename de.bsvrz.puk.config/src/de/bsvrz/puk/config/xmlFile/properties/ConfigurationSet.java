/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Diese Klasse stellt das Element "menge" aus der K2S.dtd dar und speichert alle benötigten Informationen.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationSet {
	private final String _setTypePid;
	private final String _objectSetName;
	private boolean _required = true;
	private SystemObjectInfo _info = null;

	public ConfigurationSet(String setTypePid, String objectSetName) {
		_setTypePid = setTypePid;
		_objectSetName = objectSetName;
	}

	/**
	 * Attribut "erforderlich"
	 * @param required ja/nein
	 */
	public void setRequired(String required) {
		if ("ja".equals(required)) {
			_required = true;
		} else if ("nein".equals(required)) {
			_required = false;
		}
	}

	/**
	 * Attribut "erforderlich"
	 * @param required
	 */
	public void setRequired(boolean required) {
		_required = required;
	}

	/**
	 * Attribut "info"
	 * @param info s.o
	 */
	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	/**
	 * Attribut "pid"
	 * @return s.o.
	 */
	public String getSetTypePid() {
		return _setTypePid;
	}

	/**
	 * Attribut "name"
	 * @return s.o
	 */
	public String getObjectSetName() {
		return _objectSetName;
	}

	/**
	 * Attribut "erforderlich"
	 * @return true = ja
	 */
	public boolean getRequired() {
		return _required;
	}

	/**
	 * Attribut "info"
	 * @return  info ider <code>null</code> falls nicht gesetzt
	 */
	public SystemObjectInfo getInfo() {
		return _info;
	}
}

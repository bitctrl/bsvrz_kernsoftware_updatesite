/*
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
 * Objekt, das eine Attributauswahl nach der K2S.DTD darstellt
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationAttributeChoice {
	private final String _pid;
	private String _name = "";
	private SystemObjectInfo _info = null;

	public ConfigurationAttributeChoice(String pid) {
		_pid = pid;
	}

	/**
	 * Attribut "name"
	 * @param name Name
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Attribut "info"
	 * @param info s.o.
	 */
	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	/**
	 *
	 * @return Pid, die zu diesem Objekt gehört
	 */
	public String getPid() {
		return _pid;
	}

	/**
	 *
	 * @return Attribut "name" oder "" falls der Name nicht gesetzt wurde
	 */
	public String getName() {
		return _name;
	}

	/**
	 *
	 * @return Attribut "info" oder <code>null</code> falls das Attribut nicht gesetzt wurde
	 */
	public SystemObjectInfo getInfo() {
		return _info;
	}
}

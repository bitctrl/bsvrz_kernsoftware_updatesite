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
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SystemObjectProperties {

	protected final String _name;

	protected final String _pid;

	protected final long _id;

	protected String _type;

	private SystemObjectInfo _info;

	public SystemObjectProperties(String name, String pid, long id, String type, SystemObjectInfo info) throws IllegalArgumentException{
		_name = name;
		_pid = pid;
		_id = id;
		_type = type;
		_info = info;
	}

	public void setType(String type) {
		_type = type;
	}

	public String getName() {
		return _name;
	}

	public String getPid() {
		return _pid;
	}

	public long getId() {
		return _id;
	}

	public String getType() {
		return _type;
	}

	public SystemObjectInfo getInfo() {
		return _info;
	}

	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder().append(getType()).append('{');
		appendParamString(stringBuilder);
		return stringBuilder.append('}').toString();
	}

	protected StringBuilder appendParamString(StringBuilder stringBuilder) {
		if(getId() != 0) stringBuilder.append(getId()).append(',');
		stringBuilder.append(getPid()).append(',').append(getName());
		if(getInfo() != SystemObjectInfo.UNDEFINED) stringBuilder.append(',').append(getInfo());
		return stringBuilder;
	}
}

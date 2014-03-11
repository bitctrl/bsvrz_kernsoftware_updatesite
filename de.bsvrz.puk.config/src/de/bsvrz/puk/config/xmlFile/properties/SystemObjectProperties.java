/*
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
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

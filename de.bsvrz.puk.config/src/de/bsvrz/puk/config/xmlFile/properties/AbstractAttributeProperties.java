/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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
 * Default-Implementierung des Interfaces AttributeProperties.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
abstract class AbstractAttributeProperties implements AttributeProperties {

	protected final String _attributeTypePid;

	private int _maxCount = -1;

	private TargetValue _targetValue = TargetValue.FIX;

	private String _name = "";

	private SystemObjectInfo _info = null;

	AbstractAttributeProperties(
			String attributeTypePid) {
		_attributeTypePid = attributeTypePid;
	}

	public void setMaxCount(String maxCount) {
		if (!"".equals(maxCount)) {
			_maxCount = Integer.parseInt(maxCount);
		}
	}

	public void setMaxCount(int maxCount) {
		_maxCount = maxCount;
	}

	public void setTargetValue(String targetValue) {
		if (TargetValue.FIX.getValue().equals(targetValue)) {
			_targetValue = TargetValue.FIX;
		} else if (TargetValue.VARIABLE.getValue().equals(targetValue)) {
			_targetValue = TargetValue.VARIABLE;
		}
	}

	public void setTargetValue(TargetValue targetValue) {
		_targetValue = targetValue;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	public String getAttributeTypePid() {
		return _attributeTypePid;
	}

	public int getMaxCount() {
		return _maxCount;
	}

	public TargetValue getTargetValue() {
		return _targetValue;
	}

	public String getName() {
		return _name;
	}

	public SystemObjectInfo getInfo() {
		return _info;
	}
}

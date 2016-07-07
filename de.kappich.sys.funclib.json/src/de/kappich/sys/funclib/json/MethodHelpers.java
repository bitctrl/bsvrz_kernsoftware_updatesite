/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.kappich.sys.funclib.json.
 * 
 * de.kappich.sys.funclib.json is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.kappich.sys.funclib.json is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.kappich.sys.funclib.json.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.kappich.sys.funclib.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class MethodHelpers {
	
	private static final Map<Class<?>, MethodHelper> _helperMap = new HashMap<Class<?>, MethodHelper>();

	static MethodHelper getHelper(Class<?> clazz) {
		if(!clazz.isInterface()){
			Class<?>[] interfaces = clazz.getInterfaces();
			if(interfaces.length == 1){
				clazz = interfaces[0];
			}
			else {
				throw new IllegalArgumentException("Mehrere Interfaces: " + Arrays.toString(interfaces));
			}
		}
		synchronized(_helperMap) {
			MethodHelper helper = _helperMap.get(clazz);
			if(helper == null) {
				helper = new MethodHelper(clazz);
				_helperMap.put(clazz, helper);
			}
			return helper;
		}
	}
	
}

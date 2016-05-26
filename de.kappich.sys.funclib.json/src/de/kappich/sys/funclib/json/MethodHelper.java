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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class MethodHelper {

	static final Json JSON = new Json.Builder().formatted().build();
	final Map<Method, String> _methodToKey = new HashMap<Method, String>();
	final Map<String, Method> _keyToMethod = new HashMap<String, Method>();
	final Map<Method, Object> _methodToDefault = new HashMap<Method, Object>();
	
	static final Object NO_DEFAULT = new Object();

	public MethodHelper(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for(Method method : methods) {
			if(method.getParameterTypes().length != 0) continue;
			if(method.getReturnType().equals(Void.TYPE)) continue;
			if(method.getDeclaringClass().equals(Object.class)) continue;
			String property = keyForMethod0(method);
			if(property == null) continue;
			if(_methodToKey.put(method, property) != null){
				throw new IllegalArgumentException("Eigenschaft mehrfach definiert: " + property); 
			}
			if(_keyToMethod.put(property, method) != null){
				throw new IllegalArgumentException("Eigenschaft mehrfach definiert: " + property); 
			}
			try {
				_methodToDefault.put(method, getDefault0(method));
			}
			catch(IOException e) {
				throw new IllegalArgumentException("Default-Wert fehlerhaft: " + property, e);
			}
		}
	}


	private static String keyForMethod0(final Method method) {
		String name = method.getName();
		JsonKey jsonKey = method.getAnnotation(JsonKey.class);
		if(jsonKey != null){
			return jsonKey.value();
		}
		if(name.startsWith("get")){
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		}
		else if(name.startsWith("is")){
			return Character.toLowerCase(name.charAt(2)) + name.substring(3);
		}
		return null;
	}


	private static Object getDefault0(final Method method) throws IOException {
		JsonDefault jsonDefault = method.getAnnotation(JsonDefault.class);
		if(jsonDefault == null){
			return NO_DEFAULT;
		}
		return JSON.readObject(method.getReturnType(), JsonReader.fromCharSequence(jsonDefault.value()));
	}

	String propertyForMethod(final Method method) {
		return _methodToKey.get(method);
	}

	Method methodForProperty(final String key) {
		return _keyToMethod.get(key);
	}

	Object getDefault(final Method method) {
		return _methodToDefault.get(method);
	}

	public Set<Method> getMethods() {
		return _methodToKey.keySet();
	}

	public String formatMethod(final Method method) {
		return method.getDeclaringClass().getName() + "." + method.getName() + "()";
	}
}

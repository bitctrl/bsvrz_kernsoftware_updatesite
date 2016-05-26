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

import de.bsvrz.sys.funclib.kappich.annotations.NotNull;
import de.bsvrz.sys.funclib.kappich.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static de.kappich.sys.funclib.json.Json.*;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class JsonObject extends AbstractMap<String, Object> {
	
	private final Map<String, Object> _values;
	private final Json _json;
	
	JsonObject(final JsonReader data, final Json json) throws JsonException {
		_json = json;
		_values = new LinkedHashMap<String, Object>();
		deserializeData(data, _values, json);
	}	
	
	JsonObject(final Map<String, ?> data, final Json json) {
		_json = json;
		_values = new LinkedHashMap<String, Object>(data);
	}

	void deserializeData(final JsonReader data, Map<String, Object> result, final Json json) throws JsonException {
		read(data, '{');
		while(true) {
			if(peek(data) == '}'){
				read(data, '}');
				break;
			}
			String key = readString(data);
			read(data, ':');
			result.put(key, json.readValue(data, null));
			char c = read(data, ',', '}');
			if(c == '}') break;
		}
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final JsonObject that = (JsonObject) o;

		if(!_values.equals(that._values)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return _values.hashCode();
	}

	@Override
	public String toString() {
		return _json.writeObject(this);
	}

	@Override
	public int size() {
		return _values.size();
	}

	@Override
	public boolean isEmpty() {
		return _values.isEmpty();
	}

	@Override
	public boolean containsValue(final Object value) {
		return _values.containsValue(value);
	}

	@Override
	public boolean containsKey(final Object key) {
		return _values.containsKey(key);
	}

	@Override
	public Object get(final Object key) {
		return _values.get(key);
	}

	@NotNull
	@Override
	public Set<String> keySet() {
		return _values.keySet();
	}

	@NotNull
	@Override
	public Collection<Object> values() {
		return _values.values();
	}

	@NotNull
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return _values.entrySet();
	}

	@Override
	public Object put(@NotNull final String key, Object value) {
		if(key == null) throw new NullPointerException();
		return _values.put(key, value);
	}

	@Override
	public Object remove(final Object key) {
		return _values.remove(key);
	}

	@Override
	public void clear() {
		_values.clear();
	}

	@Nullable
	public <T> T getObject(final String key, final Class<T> clazz) throws JsonException {
		return _json.proxy(clazz, get(key));
	}
	
	@Nullable
	public <T> List<T> getList(final String key, final Class<T> clazz) throws JsonException {
		List<?> list = getObject(key, List.class);
		if(list == null) return null;
		final List<T> result = new ArrayList<T>(list.size());
		for(Object o : list) {
			result.add(_json.proxy(clazz, o));
		}
		return result;
	}
	
	@Nullable
	public String getString(final String key) throws JsonException {
		return getObject(key, String.class);
	}
	@Nullable
	public Double getDouble(final String key) throws JsonException {
		return getObject(key, Double.class);
	}
	@Nullable
	public Float getFloat(final String key) throws JsonException {
		return getObject(key, Float.class);
	}
	@Nullable
	public Long getLong(final String key) throws JsonException {
		return getObject(key, Long.class);
	}
	@Nullable
	public Integer getInteger(final String key) throws JsonException {
		return getObject(key, Integer.class);
	}
	@Nullable
	public Short getShort(final String key) throws JsonException {
		return getObject(key, Short.class);
	}
	@Nullable
	public Byte getByte(final String key) throws JsonException {
		return getObject(key, Byte.class);
	}
	@Nullable
	public BigInteger getBigInteger(final String key) throws JsonException {
		return getObject(key, BigInteger.class);
	}

	@Nullable
	public BigDecimal getBigDecimal(final String key) throws JsonException {
		return getObject(key, BigDecimal.class);
	}
}

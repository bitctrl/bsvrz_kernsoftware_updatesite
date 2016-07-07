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

import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Hauptklasse für die Serialisierung und Deserialisierung von JSON-Objekten.
 * <p>
 * Für jedes JSON-Objekt, welches deserialisiert werden soll, muss ein Interface angelegt werden, das Getter für die benötigten JSON-Keys enthält. Dieses Interface wird {@link #readObject(Class, JsonReader)} als erstem Parameter übergeben.
 * <p>
 * Alternativ kann statt einem Interface ein beliebiger anderer einfacher Datentyp (s.u.) übergeben zu werden um beispielsweise eine Zahl, ein Wahrheitswert oder ein String zu deserialisieren. Mit der Übergabe von {@link JsonObject#getClass()} kann ein allgemeines JSON-Objekt
 * deserialisiert werden, bei dem die einzelnen Schlüsselpaare wie bei einer Map abgefragt werden können.
 * <p>
 * Wird ein Interface verwendet, kann jeder Getter des Interfaces mit {@link de.kappich.sys.funclib.json.JsonKey} annotiert werden um den Namen des Keys der Methode zuzuordnen. Fehlt die Annotation wird der Key automatisch gebildet, indem ein "get" oder "is" am Anfang weggelassen
 * wird und der erste Buchstabe in einen Kleinbuchstaben konvertiert wird. "String getFirstName()" wird beispielsweise zu "firstName".
 * <p>
 * Optional kann jedem Getter mit {@link de.kappich.sys.funclib.json.JsonDefault} ein Default-Wert als JSON-String übergeben werden, der verwendet wird, wenn der entsprechende Key fehlt. Ist für eine Methode kein Default-Wert vorgegeben und fehlt der entsprechende Key, dann wird
 * beim Deserialisieren ein Fehler ausgelöst.
 * <p>
 * Unterstützte Datentypen: Boolean, JsonObject, Interfaces (s.o.), Iterable, Map, Byte, Short, Integer, Long, Float, Double, BigInteger, BigDecimal sowie alle primitiven Datentypen außer char.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class Json {

	private static final Json DEFAULT_JSON = new Builder().formatted().build();

	public static class Builder {
		private boolean _strictMath = true;
		private boolean _compact = true;
		private String _indent = "";

		/**
		 * Legt fest, ob verlustbehaftete Umwandlungen von Zahlwerten erlaubt sind. Falls strictMath false ist werden verlustbehaftete Umwandlungen von Zahlen beim Parsen zugelassen. <br/> Folgende Beispiele funktionieren nur bei <code>strictMath = false</code>: <ul> <li>1.3 ->
		 * Long = 1</li> <li>255 -> Byte = -1</li> <li>10000 -> Byte = 16</li> </ul> Die Umwandlungslogik entspricht dann der Implementierung von {@link java.math.BigDecimal#longValue()} (ersetze "long" durch den betreffenden Datentyp) <br/> Falls strictMath true ist, treten beim
		 * Parsen von nicht im Zieltyp darstellbaren Zahlen {@link JsonParseException JsonExceptions} auf. <br/> Bei der Rückgabe von Double- oder Float-Werten werden verlustbehaftete Umwandlungen immer (falls nötig) durchgeführt, um die Zahlen in die jeweilige Darstellung zu
		 * überführen. <br/> Der Standardwert ist true.
		 *
		 * @return this
		 */
		public Builder looseNumbers() {
			_strictMath = false;
			return this;
		}

		/**
		 * Sorgt dafür, dass die Ausgabe bei der Serialisierung von Json-Objekten formatiert wird und daher für Menschwn einfacher lesbar wird.
		 *
		 * @return this
		 */
		public Builder formatted() {
			_compact = false;
			_indent = "    ";
			return this;
		}

		/**
		 * Sorgt dafür, dass die Ausgabe bei der Serialisierung von Json-Objekten formatiert wird und daher für Menschan einfacher lesbar wird.
		 *
		 * @param indent Zeichenkette, welches für die Einrückung verwendert werden soll (z.B. "  " oder "\t"). Das Verwenden einer unsachgemäßen Zeichenkette kann dazu führen, dass das Resultat nicht mehr geparst werden kann.
		 * @return this
		 */
		public Builder formatted(String indent) {
			_compact = false;
			_indent = indent;
			return this;
		}

		public Json build() {
			return new Json(_strictMath, _compact, _indent);
		}
	}

	private final boolean _strictMath;
	private final String _indent;
	private final String _objectStart;
	private final String _objectEnd;
	private final String _objectEmpty;
	private final String _arrayStart;
	private final String _arrayEnd;
	private final String _arrayEmpty;
	private final String _keyValueSeparator;
	private final String _listSeparator;
	private final String _listEnd;

	public static Json getInstance() {
		return DEFAULT_JSON;
	}

	private Json(final boolean strictMath, final boolean compact, final String indent) {
		_strictMath = strictMath;
		_indent = indent;
		if(compact) {
			_objectStart = "{";
			_objectEnd = "}";
			_objectEmpty = "{}";
			_arrayStart = "[";
			_arrayEnd = "]";
			_arrayEmpty = "[]";
			_keyValueSeparator = ":";
			_listSeparator = ",";
			_listEnd = "";
		}
		else {
			_objectStart = "{\n";
			_objectEnd = "}";
			_objectEmpty = "{}";
			_arrayStart = "[\n";
			_arrayEnd = "]";
			_arrayEmpty = "[]";
			_keyValueSeparator = ": ";
			_listSeparator = ",\n";
			_listEnd = "\n";
		}
	}

	public JsonObject newObject() {
		return newObject(Collections.<String, Object>emptyMap());
	}

	public JsonObject newObject(final Map<String, ?> data) {
		return new JsonObject(data, this);
	}
	
	@Nullable
	public <T> T asJson(Class<T> cls, final Object value) throws JsonException {
		return proxy(cls, value);
	}

	@Nullable
	public Object readObject(final CharSequence charSequence) throws JsonException {
		return readValue(new JsonCharSequenceReader(charSequence), JsonObject.class);
	}

	@Nullable
	public Object readObject(final Reader reader) throws JsonException {
		return readValue(new JsonReaderReader(reader), JsonObject.class);
	}

	@Nullable
	public Object readObject(final JsonReader jsonReader) throws JsonException {
		return readValue(jsonReader, JsonObject.class);
	}

	@Nullable
	public <T> T readObject(Class<T> cls, final CharSequence charSequence) throws JsonException {
		return readObject(cls, new JsonCharSequenceReader(charSequence));
	}

	@Nullable
	public <T> T readObject(Class<T> cls, final Reader reader) throws JsonException {
		return readObject(cls, new JsonReaderReader(reader));
	}

	@Nullable
	public <T> T readObject(Class<T> cls, final JsonReader jsonReader) throws JsonException {
		return proxy(cls, readValue(jsonReader, cls));
	}

	@Nullable
	public <T> List<T> readArray(Class<T> cls, final CharSequence charSequence) throws JsonException {
		return readArray(cls, new JsonCharSequenceReader(charSequence));
	}

	@Nullable
	public <T> List<T> readArray(Class<T> cls, final Reader reader) throws JsonException {
		return readArray(cls, new JsonReaderReader(reader));
	}
	
	public <T> List<T> readArray(Class<T> cls, final JsonReader jsonReader) throws JsonException {
		List<Object> objects = readArray(jsonReader, cls);
		final List<T> result = new ArrayList<T>(objects.size());
		for(Object object : objects) {
			T p = proxy(cls, object);
			if(cls.isInstance(p)) {
				result.add(p);
			}
			else {
				throw new JsonParseException(cls.getSimpleName(), jsonReader);
			}
		}
		return result;
	}

	public String writeObject(Object object) {
		StringWriter writer = new StringWriter();
		writeValue(writer, object, 0);
		return writer.toString();
	}

	private void writeValue(final StringWriter writer, @Nullable final Object object, final int indentDepth) {
		if(object == null) {
			writer.write("null");
		}
		else if(Boolean.TRUE.equals(object)) {
			writer.write("true");
		}
		else if(Boolean.FALSE.equals(object)) {
			writer.write("false");
		}
		else if(object instanceof String) {
			writeString(writer, (String) object);
		}
		else if(object instanceof Number) {
			writeNumber(writer, (Number) object);
		}
		else if(object instanceof JsonProxy) {
			writeJsonProxy(writer, (JsonProxy) object, indentDepth);
		}
		else if(object instanceof Map) {
			writeJsonObjectOrMap(writer, (Map<?,?>) object, indentDepth);
		}
		else if(object instanceof Iterable) {
			writeArray(writer, (Iterable<?>) object, indentDepth);
		}
		else {
			writeRawObject(writer, object, indentDepth);
		}
	}

	private static void writeNumber(final StringWriter writer, final Number object) {
		if(object instanceof Byte
				|| object instanceof Short
				|| object instanceof Integer
				|| object instanceof Long
				|| object instanceof Float
				|| object instanceof Double
				|| object instanceof BigInteger
				|| object instanceof BigDecimal) {
			writer.write(String.valueOf(object));
			return;
		}
		double doubleValue = object.doubleValue();
		if(Math.floor(doubleValue) == doubleValue) {
			writer.write(String.valueOf(object.longValue()));
			return;
		}
		writer.write(String.valueOf(doubleValue));
	}

	private void writeRawObject(final StringWriter writer, final Object object, final int indentDepth) {
		MethodHelper helper = MethodHelpers.getHelper(object.getClass());
		Iterator<Method> iterator = helper.getMethods().iterator();
		if(!iterator.hasNext()){
			writer.write(_objectEmpty);
			return;
		}
		writer.write(_objectStart);
		while(iterator.hasNext()) {
			final Method method = iterator.next();
			writeIndent(writer, indentDepth + 1);
			writeString(writer, helper.propertyForMethod(method));
			writer.write(_keyValueSeparator);
			try {
				writeValue(writer, method.invoke(object), indentDepth + 1);
			}
			catch(Exception e) {
				throw new IllegalArgumentException(e);
			}
			if(iterator.hasNext()) {
				writer.write(_listSeparator);
			}
			else {
				writer.write(_listEnd);
			}
		}
		writeIndent(writer, indentDepth);
		writer.write(_objectEnd);
	}
	
	private JsonObject proxyRawObject(final Object object) {
		JsonObject result = newObject();
		MethodHelper helper = MethodHelpers.getHelper(object.getClass());
		for(final Method method : helper.getMethods()) {
			String key = helper.propertyForMethod(method);
			try {
				Object value = method.invoke(object);
				result.put(key, value);
			}
			catch(Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
		return result;
	}

	private void writeArray(final StringWriter writer, final Iterable<?> object, final int indentDepth) {
		Iterator<?> iterator = object.iterator();
		if(!iterator.hasNext()){
			writer.write(_arrayEmpty);
			return;
		}
		writer.write(_arrayStart);
		while(iterator.hasNext()) {
			Object value = iterator.next();
			writeIndent(writer, indentDepth + 1);
			writeValue(writer, value, indentDepth + 1);
			if(iterator.hasNext()) {
				writer.write(_listSeparator);
			}
			else {
				writer.write(_listEnd);
			}
		}
		writeIndent(writer, indentDepth);
		writer.write(_arrayEnd);
	}

	private void writeJsonProxy(final StringWriter writer, final JsonProxy object, final int indentDepth) {
		MethodHelper helper = object._methodHelper;
		Iterator<Method> iterator = helper.getMethods().iterator();
		if(!iterator.hasNext()){
			writer.write(_objectEmpty);
			return;
		}
		writer.write(_objectStart);
		while(iterator.hasNext()) {
			final Method method = iterator.next();
			writeIndent(writer, indentDepth + 1);
			writeString(writer, helper.propertyForMethod(method));
			writer.write(_keyValueSeparator);
			try {
				writeValue(writer, object.execute(method), indentDepth + 1);
			}
			catch(Exception e) {
				throw new IllegalArgumentException(e);
			}
			if(iterator.hasNext()) {
				writer.write(_listSeparator);
			}
			else {
				writer.write(_listEnd);
			}
		}
		writeIndent(writer, indentDepth);
		writer.write(_objectEnd);
	}

	private void writeJsonObjectOrMap(final StringWriter writer, final Map<?, ?> object, final int indentDepth) {
		Iterator<? extends Map.Entry<?, ?>> iterator = object.entrySet().iterator();
		if(!iterator.hasNext()){
			writer.write(_objectEmpty);
			return;
		}
		writer.write(_objectStart);
		while(iterator.hasNext()) {
			final Map.Entry<?, ?> entry = iterator.next();
			writeIndent(writer, indentDepth + 1);
			writeString(writer, entry.getKey().toString());
			writer.write(_keyValueSeparator);
			try {
				writeValue(writer, entry.getValue(), indentDepth + 1);
			}
			catch(Exception e) {
				throw new IllegalArgumentException(e);
			}
			if(iterator.hasNext()) {
				writer.write(_listSeparator);
			}
			else {
				writer.write(_listEnd);
			}
		}
		writeIndent(writer, indentDepth);
		writer.write(_objectEnd);
	}

	private void writeIndent(final StringWriter writer, final int depth) {
		for(int i = 0; i < depth; i++) {
			writer.write(_indent);
		}
	}

	@Nullable
	<T> T proxy(@NotNull Class<T> returnType, @Nullable final Object jsonValue) throws JsonException {
		if(returnType == null) throw new NullPointerException();
		if(jsonValue instanceof JsonProxy) {
			return (T) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType}, (JsonProxy) jsonValue);
		}
		if(jsonValue instanceof JsonObject) {
			if(returnType.equals(JsonObject.class)) return (T) jsonValue;
			return (T) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType}, new JsonProxy((JsonObject) jsonValue, returnType));
		}
		if(returnType.equals(JsonObject.class)){
			if(jsonValue == null) return null;
			return (T) proxyRawObject(jsonValue);
		}
		if(jsonValue instanceof BigDecimal) {
			BigDecimal bigDecimal = (BigDecimal) jsonValue;
			if(_strictMath) {
				try {
					if(returnType == Double.class || returnType == Double.TYPE) {
						return (T) (Double) bigDecimal.doubleValue();
					}
					if(returnType == Float.class || returnType == Float.TYPE) {
						return (T) (Float) bigDecimal.floatValue();
					}
					if(returnType == Long.class || returnType == Long.TYPE) {
						return (T) (Long) bigDecimal.longValueExact();
					}
					if(returnType == Integer.class || returnType == Integer.TYPE) {
						return (T) (Integer) bigDecimal.intValueExact();
					}
					if(returnType == Short.class || returnType == Short.TYPE) {
						return (T) (Short) bigDecimal.shortValueExact();
					}
					if(returnType == Byte.class || returnType == Byte.TYPE) {
						return (T) (Byte) bigDecimal.byteValueExact();
					}
					if(returnType == BigInteger.class) {
						return (T) bigDecimal.toBigIntegerExact();
					}
				}
				catch(ArithmeticException e) {
					throw new JsonException(jsonValue + " kann nicht verlustfrei in ein " + returnType.getSimpleName() + " konvertiert werden", e);
				}
			}
			else {
				if(returnType == Double.class || returnType == Double.TYPE) {
					return (T) (Double) bigDecimal.doubleValue();
				}
				if(returnType == Float.class || returnType == Float.TYPE) {
					return (T) (Float) bigDecimal.floatValue();
				}
				if(returnType == Long.class || returnType == Long.TYPE) {
					return (T) (Long) bigDecimal.longValue();
				}
				if(returnType == Integer.class || returnType == Integer.TYPE) {
					return (T) (Integer) bigDecimal.intValue();
				}
				if(returnType == Short.class || returnType == Short.TYPE) {
					return (T) (Short) bigDecimal.shortValue();
				}
				if(returnType == Byte.class || returnType == Byte.TYPE) {
					return (T) (Byte) bigDecimal.byteValue();
				}
				if(returnType == BigInteger.class) {
					return (T) bigDecimal.toBigInteger();
				}
			}
		}
		if(jsonValue != null && !returnType.isInstance(jsonValue)) {
			if(jsonValue instanceof Number) {
				throw new JsonException("Required type: " + returnType.getSimpleName() + ", but was: " + writeObject(jsonValue) + " (Number)");
			}
			throw new JsonException("Required type: " + returnType.getSimpleName() + ", but was: " + writeObject(jsonValue) + " (" + jsonValue.getClass().getSimpleName() + ")");
		}
		if(jsonValue == null && returnType.isPrimitive()) {
			throw new JsonException("Required type: " + returnType.getSimpleName() + ", but was: : null");
		}
		return (T) jsonValue;
	}

	@Nullable
	Object readValue(final JsonReader data, @Nullable Type resultHint) throws JsonException {
		char s = peek(data);
		switch(s) {
			case '"':
				return readString(data);
			case '{':
				if(resultHint == null || resultHint.equals(JsonObject.class)) {
					return new JsonObject(data, this);
				}
				return proxy((Class<Object>) resultHint, new JsonProxy(data, (Class<?>) resultHint));
			case '[':
				if(resultHint instanceof ParameterizedType) {
					ParameterizedType hint = (ParameterizedType) resultHint;
					if(hint.getRawType().equals(List.class)){
						return readArray(data, (Class<?>) hint.getActualTypeArguments()[0]);
					}
				}
				return readArray(data, null);
			case 't':
				return readConstant(data, "true", Boolean.TRUE);
			case 'f':
				return readConstant(data, "false", Boolean.FALSE);
			case 'n':
				return readConstant(data, "null", null);
			default:
				return readNumber(data);
		}
	}

	private static Object readNumber(final JsonReader data) throws JsonParseException {
		StringBuilder result = new StringBuilder();
		while(true) {
			int c = data.read();
			if(Character.isWhitespace(c) || c == '}' || c == ',' || c == ']' || c == '\0') {
				data.skip(-1);
				break;
			}
			if(c == -1) break;
			result.append((char) c);
		}
		String s = result.toString();
		try {
			return new BigDecimal(s);
		}
		catch(NumberFormatException e) {
			throw new JsonParseException("Number", data, e);
		}
	}

	private List<Object> readArray(final JsonReader data, @Nullable final Class<?> resultHint) throws JsonException {
		final List<Object> result = new ArrayList<Object>();
		read(data, '[');
		while(true) {
			if(peek(data) == ']') {
				read(data, ']');
				break;
			}
			Object value = readValue(data, resultHint);
			result.add(value);
			char c = read(data, ',', ']');
			if(c == ']') break;
		}
		return result;
	}

	@Nullable
	private static Object readConstant(final JsonReader data, final String text, @Nullable final Object result) throws JsonParseException {
		String read = readNumChars(data, text.length());
		if(read.equals(text)) return result;
		data.skip(-text.length());
		throw new JsonParseException(text, data);
	}

	static String readString(final JsonReader data) throws JsonParseException {
		read(data, '"');
		StringBuilder builder = new StringBuilder();
		while(true) {
			readUntil(data, builder, '"', '\\');
			if(peek(data) == '"') {
				read(data, '\"');
				return builder.toString();
			}
			read(data, '\\');
			switch(data.read()) {
				case '"':
					builder.append('"');
					break;
				case '\\':
					builder.append('\\');
					break;
				case '/':
					builder.append('/');
					break;
				case 'b':
					builder.append('\b');
					break;
				case 'f':
					builder.append('\f');
					break;
				case 'n':
					builder.append('\n');
					break;
				case 'r':
					builder.append('\r');
					break;
				case 't':
					builder.append('\t');
					break;
				case 'u':
					String s = readNumChars(data, 4);
					try {
						builder.append((char) Short.parseShort(s, 16));
					}
					catch(NumberFormatException e) {
						data.skip(-6);
						throw new JsonParseException("\\u????", data, e);
					}
					break;
			}
		}
	}

	private static void writeString(final StringWriter writer, final String s) {
		writer.write('"');
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
				case '"':
					writer.append("\\\"");
					break;
				case '\\':
					writer.append("\\\\");
					break;
				case '\b':
					writer.append("\\b");
					break;
				case '\f':
					writer.append("\\f");
					break;
				case '\n':
					writer.append("\\n");
					break;
				case '\r':
					writer.append("\\r");
					break;
				case '\t':
					writer.append("\\t");
					break;
				default:
					if(Character.isISOControl(c)) {
						writer.append(String.format("\\u%04x", c & 0xFFFF));
					}
					else {
						writer.append(c);
					}
			}
		}
		writer.write('"');
	}

	protected String asString(final String s) {
		StringWriter writer = new StringWriter(s.length() + 2);
		writeString(writer, s);
		return writer.toString();
	}

	private static String readNumChars(final JsonReader data, int num) {
		char[] buf = new char[num];
		int off = 0;
		while(num > 0) {
			int read = data.read(buf, off, num);
			off += read;
			num -= read;
		}
		return String.valueOf(buf);
	}

	static char read(final JsonReader data, final char... expected) throws JsonParseException {
		String s = readUntil(data, expected);
		String trim = s.trim();
		if(trim.length() > 0) {
			data.skip(-trim.length());
			throw new JsonParseException(new String(expected), data);
		}
		return (char) data.read();
	}

	static char peek(final JsonReader data) {
		int c;
		while(true) {
			c = data.read();
			if(c > ' ') {
				break;
			}
		}
		data.skip(-1);
		return (char) c;
	}

	private static String readUntil(final JsonReader data, final char... expected) throws JsonParseException {
		StringBuilder result = new StringBuilder();
		readUntil(data, result, expected);
		return result.toString();
	}

	private static void readUntil(final JsonReader data, final StringBuilder result, final char... expected) throws JsonParseException {
		while(true) {
			int c = data.read();
			for(char exp : expected) {
				if(exp == c) {
					data.skip(-1);
					return;
				}
			}
			if(c <= 0) throw new JsonParseException(new String(expected), data);
			result.append((char) c);
		}
	}

	/**
	 * Gibt <tt>true</tt> zurück, wenn verlustbehaftete Umwandlung von Zahlwerten erlaubt ist. Der Standardwert ist true.
	 *
	 * @return <tt>true</tt>, wenn verlustbehaftete Umwandlung von Zahlwerten erlaubt ist, sonst <tt>false</tt>
	 */
	public boolean getStrictMath() {
		return _strictMath;
	}

	static final Method OBJECT_EQUALS = getObjectMethod("equals", Object.class);
	static final Method OBJECT_HASHCODE = getObjectMethod("hashCode");
	static final Method OBJECT_TOSTRING = getObjectMethod("toString");

	private static Method getObjectMethod(String name, Class<?>... types) {
		try {
			return Object.class.getMethod(name, types);
		}
		catch(NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private final class JsonProxy implements InvocationHandler {

		final Map<Method, Object> _properties = new LinkedHashMap<Method, Object>();
		private final MethodHelper _methodHelper;

		public JsonProxy(final JsonReader json, final Class<?> resultHint) throws JsonException {
			if(resultHint == null) throw new IllegalArgumentException("resultHint ist null");
			_methodHelper = MethodHelpers.getHelper(resultHint);
			deserializeData(json, _properties);
			for(Method method : _methodHelper.getMethods()) {
				if(execute(method) == MethodHelper.NO_DEFAULT) {
					throw new JsonException("Kein Wert und kein Default-Wert für " + _methodHelper.propertyForMethod(method));
				}
			}
		}

		public <T> JsonProxy(final JsonObject jsonValue, final Class<T> resultHint) throws JsonException {
			if(resultHint == null) throw new IllegalArgumentException("resultHint ist null");
			_methodHelper = MethodHelpers.getHelper(resultHint);
			deserializeData(jsonValue, _properties);
			for(Method method : _methodHelper.getMethods()) {
				if(execute(method) == MethodHelper.NO_DEFAULT) {
					throw new JsonException("Kein Wert und kein Default-Wert für " + _methodHelper.propertyForMethod(method));
				}
			}
		}

		public void deserializeData(final JsonReader data, Map<Method, Object> result) throws JsonException {
			read(data, '{');
			while(true) {
				if(peek(data) == '}') {
					read(data, '}');
					break;
				}
				String key = readString(data);
				Method method = _methodHelper.methodForProperty(key);
				read(data, ':');
				if(method == null) {
					readValue(data, null);
				}
				else {
					Object value = readValue(data, method.getGenericReturnType());
					try {
						proxy(method.getReturnType(), value);
						result.put(method, value);
					}
					catch(JsonException e) {
						data.skip(-1);
						throw new JsonParseException(method.getReturnType().getSimpleName() + " for " + _methodHelper.formatMethod(method), data, e);
					}
				}
				char c = read(data, ',', '}');
				if(c == '}') break;
			}
		}

		public void deserializeData(final JsonObject data, Map<Method, Object> result) throws JsonException {
			for(Map.Entry<String, Object> entry : data.entrySet()) {
				String key = entry.getKey();
				Method method = _methodHelper.methodForProperty(key);
				result.put(method, entry.getValue());
			}
		}

		@Nullable
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if(OBJECT_EQUALS.equals(method)) {
				return equalsProxy(args[0]);
			}
			if(OBJECT_HASHCODE.equals(method)) {
				return _properties.hashCode();
			}
			if(OBJECT_TOSTRING.equals(method)) {
				return toString();
			}
			return execute(method);
		}

		@Nullable
		protected Object execute(final Method method) throws JsonException {
			if(!_properties.containsKey(method)) {
				return _methodHelper.getDefault(method);
			}
			Object result = _properties.get(method);
			Class<?> returnType = method.getReturnType();
			return proxy(returnType, result);
		}

		private boolean equalsProxy(final Object other) {
			if(other == null) return false;
			if(!Proxy.isProxyClass(other.getClass())) {
				return false;
			}
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(other);
			if(invocationHandler instanceof JsonProxy) {
				JsonProxy otherJson = (JsonProxy) invocationHandler;
				return _properties.equals(otherJson._properties);
			}
			return false;
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			final JsonProxy that = (JsonProxy) o;

			if(!_properties.equals(that._properties)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return _properties.hashCode();
		}

		@Override
		public String toString() {
			return writeObject(this);
		}
	}
}

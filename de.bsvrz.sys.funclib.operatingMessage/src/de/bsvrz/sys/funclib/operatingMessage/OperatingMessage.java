/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.operatingMessage;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate.OperatingMessageParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasse für eine konkrete Betriebsmeldung, die versendet werden kann. Instanzen werden typischerweise über ein
 * {@link MessageTemplate} erzeugt, diese Klasse bietet jedoch auch statische Methoden, mit denen simple Meldungen
 * einfacher erzeugt werden, z. B. {@link #info(MessageType, Object, Object...)} oder {@link #create(MessageGrade, MessageType, Object, Object...)}.
 *
 * @author Kappich Systemberatung
 */
public final class OperatingMessage implements OperatingMessageInterface {

	private static final Object PLURAL = new Object();
	private final Map<String, Object> _variables = new HashMap<>();
	private final MessageTemplate _template;
	private final SystemObject _object;
	private final Set<Object> _ids = new HashSet<>();
	private java.lang.Throwable _exception = null;

	OperatingMessage(final MessageTemplate template, final SystemObject object) {
		_template = template;
		_object = object;
	}

	/**
	 * Erzeugt eine neue Betriebsmeldung mit Level INFO. Der Aufruf erfolgt analog zu 
	 * {@link de.bsvrz.sys.funclib.debug.Debug#info(String, Object)}, es muss aber zusätzlich als ersten Parameter die
	 * Meldungsart angegeben werden. Außerdem erlaubt diese Klasse beliebig viele Parameter, so kann z. B. eine
	 * Exception gleichzeitig zu einem oder mehreren Objekten ausgegeben werden.
	 * </p>
	 * Diese Methode ist eine Convenience-Methode, die anhand der übergebenen Parameter verschiedene Werte der Betriebsmeldung automatisch errät.
	 * So wird beispielsweise das erste übergebene SystemObject als Bezugsobjekt für die Betriebsmeldung interpretiert.
	 * Ist ein anderes Verhalten gewünscht, sollte ein {@link MessageTemplate} verwendet werden.
	 * </p>
	 * Erkannte Objekt-Typen:
	 * <ul>
	 *     <li>{@link MessageTemplate.OperatingMessageParam} siehe {@link MessageTemplate#MessageTemplate(MessageGrade, MessageType, OperatingMessageParam...)}</li>
	 *     <li>SystemObject</li> - Das erste übergebene SystemObject wird als Bezugsobjekt für die Betriebsmeldung verwendet
	 *     <li>Throwable</li> - Exceptions werden ggf. auf Debug-Ausgaben ausgegeben
	 *     <li>Alle anderen Objekte werden mit {@link String#valueOf(Object)} in einen String konvertiert</li>
	 * </ul>
	 * 
	 * @param type Typ
	 * @param message Meldungstext
	 * @param messageAddOn Weiterer Meldungstext
	 * @return Betriebsmeldungsobjekt, welches versendet werden kann
	 */
	public static OperatingMessage info(MessageType type, Object message, Object... messageAddOn){
		return create(MessageGrade.INFORMATION, type, message, messageAddOn);
	}

	/**
	 * Erzeugt eine neue Betriebsmeldung mit Level WARNING. Der Aufruf erfolgt analog zu 
	 * {@link de.bsvrz.sys.funclib.debug.Debug#warning(String)}, es muss aber zusätzlich als ersten Parameter die
	 * Meldungsart angegeben werden. Außerdem erlaubt diese Klasse beliebig viele Parameter, so kann z. B. eine
	 * Exception gleichzeitig zu einem oder mehreren Objekten ausgegeben werden.
	 * </p>
	 * Diese Methode ist eine Convenience-Methode, die anhand der übergebenen Parameter verschiedene Werte der Betriebsmeldung automatisch errät.
	 * So wird beispielsweise das erste übergebene SystemObject als Bezugsobjekt für die Betriebsmeldung interpretiert.
	 * Ist ein anderes Verhalten gewünscht, sollte ein {@link MessageTemplate} verwendet werden.
	 * </p>
	 * Erkannte Objekt-Typen:
	 * <ul>
	 *     <li>{@link MessageTemplate.OperatingMessageParam} siehe {@link MessageTemplate#MessageTemplate(MessageGrade, MessageType, OperatingMessageParam...)}</li>
	 *     <li>SystemObject</li> - Das erste übergebene SystemObject wird als Bezugsobjekt für die Betriebsmeldung verwendet
	 *     <li>Throwable</li> - Exceptions werden ggf. auf Debug-Ausgaben ausgegeben
	 *     <li>Alle anderen Objekte werden mit {@link String#valueOf(Object)} in einen String konvertiert</li>
	 * </ul>
	 *
	 * @param type Typ
	 * @param message Meldungstext
	 * @param messageAddOn Weiterer Meldungstext
	 * @return Betriebsmeldungsobjekt, welches versendet werden kann
	 */
	public static OperatingMessage warning(MessageType type, Object message, Object... messageAddOn){
		return create(MessageGrade.WARNING, type, message, messageAddOn);
	}

	/**
	 * Erzeugt eine neue Betriebsmeldung mit Level ERROR. Der Aufruf erfolgt analog zu 
	 * {@link de.bsvrz.sys.funclib.debug.Debug#error(String)}, es muss aber zusätzlich als ersten Parameter die
	 * Meldungsart angegeben werden. Außerdem erlaubt diese Klasse beliebig viele Parameter, so kann z. B. eine
	 * Exception gleichzeitig zu einem oder mehreren Objekten ausgegeben werden.
	 * </p>
	 * Diese Methode ist eine Convenience-Methode, die anhand der übergebenen Parameter verschiedene Werte der Betriebsmeldung automatisch errät.
	 * So wird beispielsweise das erste übergebene SystemObject als Bezugsobjekt für die Betriebsmeldung interpretiert.
	 * Ist ein anderes Verhalten gewünscht, sollte ein {@link MessageTemplate} verwendet werden.
	 * </p>
	 * Erkannte Objekt-Typen:
	 * <ul>
	 *     <li>{@link MessageTemplate.OperatingMessageParam} siehe {@link MessageTemplate#MessageTemplate(MessageGrade, MessageType, OperatingMessageParam...)}</li>
	 *     <li>SystemObject</li> - Das erste übergebene SystemObject wird als Bezugsobjekt für die Betriebsmeldung verwendet
	 *     <li>Throwable</li> - Exceptions werden ggf. auf Debug-Ausgaben ausgegeben
	 *     <li>Alle anderen Objekte werden mit {@link String#valueOf(Object)} in einen String konvertiert</li>
	 * </ul>
	 *
	 * @param type Typ
	 * @param message Meldungstext
	 * @param messageAddOn Weiterer Meldungstext
	 * @return Betriebsmeldungsobjekt, welches versendet werden kann
	 */
	public static OperatingMessage error(MessageType type, Object message, Object... messageAddOn){
		return create(MessageGrade.ERROR, type, message, messageAddOn);
	}

	/**
	 * Erzeugt eine neue Betriebsmeldung mit Level FATAL. Der Aufruf erfolgt analog zu 
	 * {@link de.bsvrz.sys.funclib.debug.Debug#error(String)}, es muss aber zusätzlich als ersten Parameter die
	 * Meldungsart angegeben werden. Außerdem erlaubt diese Klasse beliebig viele Parameter, so kann z. B. eine
	 * Exception gleichzeitig zu einem oder mehreren Objekten ausgegeben werden.
	 * </p>
	 * Diese Methode ist eine Convenience-Methode, die anhand der übergebenen Parameter verschiedene Werte der Betriebsmeldung automatisch errät.
	 * So wird beispielsweise das erste übergebene SystemObject als Bezugsobjekt für die Betriebsmeldung interpretiert.
	 * Ist ein anderes Verhalten gewünscht, sollte ein {@link MessageTemplate} verwendet werden.
	 * </p>
	 * Erkannte Objekt-Typen:
	 * <ul>
	 *     <li>{@link MessageTemplate.OperatingMessageParam} siehe {@link MessageTemplate#MessageTemplate(MessageGrade, MessageType, OperatingMessageParam...)}</li>
	 *     <li>SystemObject</li> - Das erste übergebene SystemObject wird als Bezugsobjekt für die Betriebsmeldung verwendet
	 *     <li>Throwable</li> - Exceptions werden ggf. auf Debug-Ausgaben ausgegeben
	 *     <li>Alle anderen Objekte werden mit {@link String#valueOf(Object)} in einen String konvertiert</li>
	 * </ul>
	 *
	 * @param type Typ
	 * @param message Meldungstext
	 * @param messageAddOn Weiterer Meldungstext
	 * @return Betriebsmeldungsobjekt, welches versendet werden kann
	 */
	public static OperatingMessage fatal(MessageType type, Object message, Object... messageAddOn){
		return create(MessageGrade.FATAL, type, message, messageAddOn);
	}

	/**
	 * Erzeugt eine neue Betriebsmeldung mit angegebenem Level und Typ.
	 * </p>
	 * Diese Methode ist eine Convenience-Methode, die anhand der übergebenen Parameter verschiedene Werte der Betriebsmeldung automatisch errät.
	 * So wird beispielsweise das erste übergebene SystemObject als Bezugsobjekt für die Betriebsmeldung interpretiert.
	 * Ist ein anderes Verhalten gewünscht, sollte ein {@link MessageTemplate} verwendet werden.
	 * </p>
	 * Erkannte Objekt-Typen:
	 * <ul>
	 *     <li>{@link MessageTemplate.OperatingMessageParam} siehe {@link MessageTemplate#MessageTemplate(MessageGrade, MessageType, OperatingMessageParam...)}</li>
	 *     <li>SystemObject</li> - Das erste übergebene SystemObject wird als Bezugsobjekt für die Betriebsmeldung verwendet
	 *     <li>Throwable</li> - Exceptions werden ggf. auf Debug-Ausgaben ausgegeben
	 *     <li>Alle anderen Objekte werden mit {@link String#valueOf(Object)} in einen String konvertiert</li>
	 * </ul>
	 *
	 * @param messageGrade Schwere der Meldung
	 * @param messageType Typ
	 * @param message Meldungstext
	 * @param messageAddOn Weiterer Meldungstext
	 * @return Betriebsmeldungsobjekt, welches versendet werden kann
	 */
	public static OperatingMessage create(final MessageGrade messageGrade, final MessageType messageType, final Object message, final Object... messageAddOn) {
		final List<OperatingMessageParam> params = new ArrayList<>();
		SystemObject[] object = new SystemObject[1];
		Throwable[] exception = new Throwable[1];
		params.add(toParam(message, object, exception));
		for(Object o : messageAddOn) {
			params.add(toParam(o, object, exception));
		}
		OperatingMessage operatingMessage = new MessageTemplate(messageGrade, messageType, params.toArray(new OperatingMessageParam[params.size()])).newMessage(object[0]);
		operatingMessage.setException(exception[0]);
		return operatingMessage;
	}

	private static OperatingMessageParam toParam(final Object param, final SystemObject[] objectRef, final Throwable[] exceptionRef) {
		if(param instanceof SystemObject && objectRef[0] == null) {
			SystemObject systemObject = (SystemObject) param;
			objectRef[0] = systemObject;
			return MessageTemplate.object();
		}
		else if(param instanceof Throwable) {
			Throwable throwable = (Throwable) param;
			exceptionRef[0] = throwable;
			return MessageTemplate.fixed(throwable.getLocalizedMessage() == null ? "" : throwable.getLocalizedMessage());
		}
		else if(param instanceof OperatingMessageParam) {
			return (OperatingMessageParam) param;
		}
		return MessageTemplate.fixed(String.valueOf(param));
	}

	/**
	 * Hilfsfunktion zur Formatierung einer Menge
	 * @param param Menge
	 * @param joiner Verbinder zwischen dem zweitletzten un dem letzten Eintrag. Sinnvoll sind z. B. ", ", " und ", " sowie ", ...
	 * @param singular Singular-Präfix
	 * @param plural Plural-Präfix
	 * @return Verketteter Text
	 */
	public static CharSequence formatCollection(Collection<?> param, final String joiner, final String singular, final String plural) {
		StringBuilder builder = new StringBuilder();
		if(param.size() == 1){
			builder.append(singular);
		}
		else {
			builder.append(plural);
		}
		int i = 0;
		param = param.stream().filter(o -> o != PLURAL).collect(Collectors.toList());
		for(Object o : param) {
			builder.append(o);
			if(i == param.size() - 2){
				builder.append(joiner);
			}
			else if (i < param.size() - 2) {
				builder.append(", ");
			}
			i++;
		}
		return builder;
	}

	/** 
	 * Gibt den aktuellen Wert einer Variablen zurück
	 * @param variable Variablen-Name
	 * @return den aktuellen Wert einer Variablen
	 */
	public Object get(final String variable) {
		return _variables.get(variable);
	}

	/**
	 * Setzt eine Variable
	 * @param variable Variablen-Name
	 * @param value Wert (wird zur Ausgabe ggf. mit {@link String#valueOf(Object)} in einen String konvertiert)
	 */
	public void put(final String variable, Object value) {
		_variables.put(variable, value);
	}

	/**
	 * Fügt einer Variable, die als Menge verwendet wird, ein Element hinzu.
	 * @param variable Variablen-Name
	 * @param value Wert (wird zur Ausgabe ggf. mit {@link String#valueOf(Object)} in einen String konvertiert)
	 */
	public void add(final String variable, Object value) {
		Object o = get(variable);
		Collection<Object> collection;
		if(o == null || !(o instanceof Collection<?>)) {
			put(variable, collection = new LinkedHashSet<>());
			if(o != null) {
				collection.add(o);
			}
		}
		else {
			collection = (Collection<Object>) o;
		}
		collection.add(value);
	}
	
	/**
	 * Fügt einer Variable, die als Menge verwendet wird, ein Element hinzu.
	 * @param variable Variablen-Name
	 * @param value Wert (wird zur Ausgabe ggf. mit {@link String#valueOf(Object)} in einen String konvertiert)
	 * @param forcePlural Soll die Menge auf jeden Fall im Plural ausgegeben werden? (ist sinnvoll, wenn value bereits mehrere Objekte repräsentiert)
	 */
	public void add(final String variable, Object value, final boolean forcePlural) {
	    add(variable, value);
		if(forcePlural){
			add(variable, PLURAL);
		}
	}

	/**
	 * Fügt eine ID hinzu. Die Anzahl der IDs ist beliebig, eine Betriebsmeldung kann keine, eine oder mehrere
	 * IDs haben. Die IDs werden im MeldungsTypZusatz publiziert. Es handelt sich nicht um die IDs die zur Verknüpfung von
	 * mehreren zusammengehörigen Betriebsmeldungen verwendet werden.
	 * @param value Beliebige ID (wird zur Ausgabe ggf. mit {@link String#valueOf(Object)} in einen String konvertiert)
	 */
	public void addId(Object value){
		_ids.add(value);
	}

	/**
	 * Gibt die mit {@link #addId(Object)} bisher hinzugefügten IDs zurück.
	 * @return Menge mit IDs.
	 */
	public Set<Object> getIds() {
		return _ids;
	}

	/**
	 * Gibt den Text der Betriebsmeldung zurück. Undefinierte Variablen werden durch den String "[Undefiniert]" ersetzt.
	 * @return Betriebsmeldungstext
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for(OperatingMessageParam operatingMessageParam : _template.getParams()) {
			if(operatingMessageParam.isDefined(this)) {
				stringBuilder.append(operatingMessageParam.format(this));
			}
			else {
				stringBuilder.append("[Undefiniert]");
			}
		}
		return stringBuilder.toString();
	}

	/** 
	 * Gibt <tt>true</tt> zurück, wenn alle Variablen und ähnliches definiert sind
	 * @return <tt>true</tt>, wenn alle Variablen und ähnliches definiert sind, sonst <tt>false</tt>
	 */
	public boolean isDefined() {
		for(OperatingMessageParam operatingMessageParam : _template.getParams()) {
			if(!operatingMessageParam.isDefined(this)) return false;
		}
		return true;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	@Override
	public MessageGrade getGrade() {
		return _template.getGrade();
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public MessageType getDomain() {
		return _template.getDomain();
	}

	@Override
	public String getMessageTypeAddOn() {
		return formatCollection(getIds(), ", ", "", "").toString();
	}

	public SystemObject getObject() {
		return _object;
	}

	@Override
	public MessageState getState() {
		return MessageState.MESSAGE;
	}

	/**
	 * Sendet diese Betriebsmeldung einmal (falls sie definiert ist), sonst passiert nichts.
	 */
	public void send(){
		if(isDefined()) {
			OperatingMessageSink.publishEverywhere(this);
		}
	}

	/**
	 * Sendet diese Betriebsmeldung als neue Meldung und gibt ein Objekt zurück, über das die Betriebsmeldung
	 * geändert und ggf. wieder gutgemeldet werden kann.
	 * @return Persistente Betriebsmeldung oder null wenn die Betriebsmeldung nicht definiert ist
	 */
	public PersistentOperatingMessage newPersistentMessage() {
		return newPersistentMessage(UUID.randomUUID().toString());
	}

	/**
	 * Sendet diese Betriebsmeldung als neue Meldung und gibt ein Objekt zurück, über das die Betriebsmeldung
	 * geändert und ggf. wieder gutgemeldet werden kann.
	 * @return Persistente Betriebsmeldung oder null wenn die Betriebsmeldung nicht definiert ist
	 * @param id ID zur Identifizierung der Meldung
	 */
	public PersistentOperatingMessage newPersistentMessage(final String id){
		if(!isDefined()) {
			return null;
		}
		PersistentOperatingMessage message = new PersistentOperatingMessage(
				id,
				this
		);
		OperatingMessageSink.publishEverywhere(message);
		return message;
	}

	/**
	 * Setzt bei Bedarf eine Exception, die die Ursache dieser Betriebsmeldung ist
	 * @param exception Exception
	 */
	public void setException(final Throwable exception) {
		_exception = exception;
	}

	/** 
	 * Gibt die Exception zurück
	 * @return die Exception
	 */
	public Throwable getException() {
		return _exception;
	}
}

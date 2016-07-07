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
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;
import java.util.logging.Level;

/**
 * Diese Klasse erlaubt es, Vorlagen für die Erstellung von Betriebsmeldungs-Texten zu erzeugen. Eine Betriebsmeldungsvorlage
 * besteht aus Textbausteinen, die später mit variablen Anteilen gefüllt werden können. Beispiel:
 *
 *      MessageTemplate template = new MessageTemplate(
 *      MessageGrade.INFORMATION,
 *      MessageType.APPLICATION_DOMAIN,
 *      MessageTemplate.fixed("Der Fahrstreifen "),
 *      MessageTemplate.object(),
 *      MessageTemplate.fixed(" enthält fehlerhafte Daten "),
 *      MessageTemplate.set("attr", " und ", "im Attribut ", "in den Attributen "),
 *      MessageTemplate.fixed(".")
 *      );
 *      
 *      final SystemObject fahrstreifen = ...;
 *      OperatingMessage operatingMessage = template.newMessage(fahrstreifen);
 *      operatingMessage.add("attr", "qKfz");
 *      operatingMessage.add("attr", "qPkw");
 *      operatingMessage.add("attr", "qB");
 *      operatingMessage.send(); 
 * 
 * Mit Ausgabe
 * 
 *      operatingMessage = Der Fahrstreifen fs.test enthält fehlerhafte Daten in den Attributen qKfz, qPkw und qB.
 *      
 * und Versand einer entsprechenden Betriebsmeldung.
 * 
 * Folgende Methoden erzeugen Parameter-Bausteine:
 *
 * -   `fixed(String)`, ein fester String.
 * -   `variable(String)`, eine mit einem beliebigen String oder Objekt befüllbare Variable. (Siehe {@link OperatingMessage#put(String, Object)})
 * -   `set(String, String)`, eine Variable, mit mit einer Menge von Objekten befüllt wird. Die doppelte Angabe eines Objekts wird automatisch
 *     verhindert, Objekte werden in der Reihenfolge ausgegeben, wie sie eingefügt werden. Optional können hier 2 weitere Parameter angegeben
 *     werden, mit denen ein Singular und Plural-Präfix definiert werden kann. (Siehe {@link OperatingMessage#add(String, Object)})
 * -   `object()`, fügt eine Referenz auf das Systemobjekt ein, auf das sich die Meldung bezieht. (Siehe {@link #newMessage(SystemObject)})
 * -   `ids()`, Eine spezielle Mengen-Variable, die Kennungen der Betriebsmeldung enthält. Die IDs werden im MeldungsTypZusatz publiziert.
 *     (Siehe {@link OperatingMessage#addId(Object)})
 * 
 * Diese Klasse sowie die hier definierten OperatingMessageParam-Instanzen sind unveränderlich und threadsafe.
 *
 * @author Kappich Systemberatung
 */
public class MessageTemplate {

	private final OperatingMessageParam[] _text;
	private final MessageGrade _grade;
	private final MessageType _domain;
	private final Level _level;
	private final MessageIdFactory _messageIdFactory;

	/**
	 * Erzeugt eine neue Betriebsmeldungs-Text-Vorlage
	 *
	 * @param grade  Schwere der Meldung
	 * @param domain Art der Meldung
	 * @param text   Textbestandteile, die verkettet werden. Zur Erzeugung der einzelnen {@link OperatingMessageParam}-Instanzen können die statischen Methoden
	 *               in dieser Klasse benutzt werden.
	 */
	public MessageTemplate(final MessageGrade grade, final MessageType domain, OperatingMessageParam... text) {
		this(grade, domain, text, Debug.INFO, (message -> ""));
	}

	/**
	 * Erzeugt eine neue Betriebsmeldungs-Text-Vorlage
	 *
	 * @param grade            Schwere der Meldung
	 * @param domain           Art der Meldung
	 * @param text             Textbestandteile, die verkettet werden. Zur Erzeugung der einzelnen {@link OperatingMessageParam}-Instanzen können die statischen
	 *                         Methoden in dieser Klasse benutzt werden.
	 * @param level            Debug-Level
	 * @param messageIdFactory Funktion, mit dem die IDs generiert werden sollen
	 */
	private MessageTemplate(final MessageGrade grade, final MessageType domain, OperatingMessageParam[] text, final Level level, final MessageIdFactory messageIdFactory) {
		_grade = grade;
		_domain = domain;
		_level = level;
		_text = text;
		_messageIdFactory = messageIdFactory;
	}

	/**
	 * Erstellt einen neuen Platzhalter für eine Menge
	 *
	 * @param variable Variablenname
	 * @param joiner   Verbinder zwischen dem zweitletzen un dem letzten Eintrag. Sinnvoll sind z. B. ", ", " und ", " sowie ", ...
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam set(final String variable, final String joiner) {
		return set(variable, joiner, "", "");
	}

	/**
	 * Erstellt einen neuen Platzhalter für eine Menge
	 *
	 * @param variable Variablenname
	 * @param joiner   Verbinder zwischen dem zweitletzten un dem letzten Eintrag. Sinnvoll sind z. B. ", ", " und ", " sowie ", ...
	 * @param singular Singular-Präfix
	 * @param plural   Plural-Präfix
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam set(final String variable, final String joiner, final String singular, final String plural) {
		return new OperatingMessageParam() {
			@Override
			public CharSequence format(OperatingMessage message) {
				Object o = message.get(variable);
				if(o instanceof Collection<?>) {
					Collection<?> param = (Collection<?>) o;
					return OperatingMessage.formatCollection(param, joiner, singular, plural);
				}
				else {
					return String.valueOf(o);
				}
			}


			@Override
			public boolean isDefined(final OperatingMessage message) {
				return message.get(variable) != null && !((Collection<?>) message.get(variable)).isEmpty();
			}

			@Override
			public String toString() {
				return plural + "<" + variable + ">";
			}
		};
	}

	/**
	 * Erstellt einen festen String-Wert als Textbaustein
	 *
	 * @param s Text
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam fixed(final String s) {
		return new OperatingMessageParam() {
			@Override
			public CharSequence format(final OperatingMessage message) {
				return s;
			}

			@Override
			public boolean isDefined(final OperatingMessage message) {
				return true;
			}

			@Override
			public String toString() {
				return s;
			}
		};
	}

	/**
	 * Erstellt einen neuen Platzhalter für eine einfache Variable
	 *
	 * @param variable Variablenname
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam variable(final String variable) {
		return new OperatingMessageParam() {
			@Override
			public CharSequence format(final OperatingMessage message) {
				return message.get(variable).toString();
			}

			@Override
			public boolean isDefined(final OperatingMessage message) {
				return message.get(variable) != null;
			}

			@Override
			public String toString() {
				return "<" + variable + ">";
			}
		};
	}

	/**
	 * Erstellt einen neuen Platzhalter für das Objekt, auf das sich die Betriebsmeldung bezieht
	 *
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam object() {
		return new OperatingMessageParam() {
			@Override
			public CharSequence format(final OperatingMessage message) {
				return message.getObject().toString();
			}

			@Override
			public boolean isDefined(final OperatingMessage message) {
				return message.getObject() != null;
			}

			@Override
			public String toString() {
				return "<object>";
			}
		};
	}

	/**
	 * Erstellt einen neuen Platzhalter für die der Meldung zugeordneten Kennungen (MeldungsTypZusatz). Nicht identisch mit der Meldungs-ID,
	 * die zur Verknüpfung von Meldungen verwendet wird.
	 *
	 * @return Ein OperatingMessageParam-Objekt das im Konstruktor verwendet werden kann.
	 */
	public static OperatingMessageParam ids() {
		return new OperatingMessageParam() {
			@Override
			public CharSequence format(OperatingMessage message) {
				return OperatingMessage.formatCollection(message.getIds(), ", ", "", "");
			}


			@Override
			public boolean isDefined(final OperatingMessage message) {
				return !message.getIds().isEmpty();
			}

			@Override
			public String toString() {
				return "<ids>";
			}
		};
	}

	/**
	 * Gibt eine Kopie dieses Objekts zurück, welches den übergebenen Lambda-Ausdruck zur Erzeugung einer Betriebsmeldungs-ID verwendet
	 *
	 * @param factory Lambda-Ausdruck o.ä. zur Erzeugung der Meldungs-ID
	 * @return MessageTemplate
	 */
	public MessageTemplate withIdFactory(MessageIdFactory factory) {
		Objects.requireNonNull(factory, "factory == null");
		return new MessageTemplate(_grade, _domain, _text, _level, factory);
	}

	/**
	 * Gibt eine Kopie dieses Objekts zurück, welches den übergebenen Debug-Level für die Debug-Meldung verwendet (standardmäßig INFO)
	 *
	 * @param level Debug-Level
	 * @return MessageTemplate
	 */
	public MessageTemplate withDebugLevel(Level level) {
		Objects.requireNonNull(level, "level == null");
		return new MessageTemplate(_grade, _domain, _text, level, _messageIdFactory);
	}

	/**
	 * Gibt die Textbausteine dieses Objekts zurück
	 *
	 * @return die im Konstruktor übergebenen Textbausteine dieses Objekts
	 */
	public List<OperatingMessageParam> getParams() {
		return Collections.unmodifiableList(Arrays.asList(_text));
	}

	/**
	 * Gibt die Schwere der Meldung zurück
	 *
	 * @return die Schwere der Meldung
	 */
	public MessageGrade getGrade() {
		return _grade;
	}

	/**
	 * Gibt die Art der Meldung zurück
	 *
	 * @return die Art der Meldung
	 */
	public MessageType getDomain() {
		return _domain;
	}

	/**
	 * Erstellt aus der Vorlage ein neues Betriebsmeldungs-Objekt, das mit konkreten Werten gefüllt und dann versand werden kann
	 *
	 * @param systemObject Objekt, auf das sich die Meldung bezieht (null ist erlaubt wodurch es dann kein Bezugsobjekt gibt)
	 * @return Betriebsmeldungs-Objekt
	 */
	public OperatingMessage newMessage(final SystemObject systemObject) {
		OperatingMessage operatingMessage = new OperatingMessage(this, systemObject);
		operatingMessage.setMessageId(_messageIdFactory.generateMessageId(operatingMessage));
		return operatingMessage;
	}

	/**
	 * Gibt den Level zurück, mit dem erzeugte Meldungen beim Versand über die Debug-Funktionen ausgegeben werden sollen.
	 * <p>
	 * Mit {@link Debug#OFF} kann die Ausgabe über Debug deaktiviert werden, dann wird nur die Meldung über den Datenverteiler versendet
	 *
	 * @return den Level
	 */
	public Level getLevel() {
		return _level;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for(OperatingMessageParam operatingMessageParam : getParams()) {
			stringBuilder.append(operatingMessageParam);
		}
		return stringBuilder.toString();
	}

	/**
	 * Abstrakte Klasse für einen Textbaustein
	 */
	public abstract static class OperatingMessageParam {
		/**
		 * Gibt den konkreten Textwert zurück. Wenn {#isDefined} false zurück liefert darf diese Methode eine RuntimeException werfen.
		 *
		 * @param message Konkretes Betriebsmeldungs-Objekt als Quelle von gesetzten Variablen und ähnlichem
		 * @return Text
		 */
		public abstract CharSequence format(OperatingMessage message);

		/**
		 * Gibt zurück ob der Wert dieses Textbausteins definiert ist.
		 *
		 * @param message Konkretes Betriebsmeldungs-Objekt als Quelle von gesetzten Variablen und ähnlichem
		 * @return true wenn alle evtl. benötigen Variablen definiert sind und dadurch ein Text mit {@link #format(OperatingMessage)} erzeugt werden kann
		 */
		public abstract boolean isDefined(final OperatingMessage message);

		@Override
		public abstract String toString();
	}
}

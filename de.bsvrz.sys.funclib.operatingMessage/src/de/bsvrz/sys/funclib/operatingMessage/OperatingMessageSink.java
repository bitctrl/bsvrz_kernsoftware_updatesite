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

import de.bsvrz.sys.funclib.debug.Debug;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static de.bsvrz.sys.funclib.debug.Debug.*;

/**
 * Diese Klasse regelt den Versand von Betriebsmeldungen. Im Gegensatz zum direkten Aufruf von {@link MessageSender#sendMessage}
 * können sich hier weitere Empfänger auf Betriebsmeldungen anmelden (z. B. für Testfälle) und die Betriebsmeldung wird auch über die Debug-Funktion mit
 * dem Level INFO ausgegeben.
 *
 * @author Kappich Systemberatung
 */
public abstract class OperatingMessageSink {

	private static final Debug _debug = Debug.getLogger();
	private static final CopyOnWriteArraySet<OperatingMessageSink> _sinks = new CopyOnWriteArraySet<>();
	private static final AtomicBoolean _initialized = new AtomicBoolean(false);

	/**
	 * Meldet eine neue Klasse an, die zu versendende Betriebsmeldungen entgegen nimmt
	 * @param sink OperatingMessageSink-Objekt mit benutzerdefinierter Implementierung
	 */
	public static void register(OperatingMessageSink sink){
		_sinks.add(sink);
	}
	
	/**
	 * Meldet ein Sink-Objekt wieder ab
	 * @param sink OperatingMessageSink-Objekt mit benutzerdefinierter Implementierung
	 */
	public static void unregister(OperatingMessageSink sink){
		_sinks.remove(sink);
	}

	/**
	 * Veröffentlicht eine Betriebsmeldung über alle registrierten OperatingMessageSink-Objekte. Standardmäßig werden Betriebsmeldungen
	 * über die MessageSender-Klasse und die Debug-Ausgabe veröffentlicht.
	 * @param message Betriebsmeldung, die versendet werden soll
	 */
	public static void publishEverywhere(OperatingMessageInterface message){
		synchronized(_initialized) {
			if(!_initialized.getAndSet(true)) {
				initialize();
			}
		}
		for(OperatingMessageSink sink : _sinks) {
			sink.publish(message);
		}
	}

	private static void initialize() {
		register(new OperatingMessageSink() {
			@Override
			public void publish(final OperatingMessageInterface m) {
				Throwable exception = m.getException();
				Level level = m.getLevel();
				if(level.equals(ERROR)) {
					if(exception != null) {
						_debug.error(m.getMessage(), exception);
					}
					else {
						_debug.error(m.getMessage());
					}
				}
				else if(level.equals(WARNING)) {
					if(exception != null) {
						_debug.warning(m.getMessage(), exception);
					}
					else {
						_debug.warning(m.getMessage());
					}
				}
				else if(level.equals(INFO)) {
					if(exception != null) {
						_debug.info(m.getMessage(), exception);
					}
					else {
						_debug.info(m.getMessage());
					}
				}
				else if(level.equals(CONFIG)) {
					if(exception != null) {
						_debug.config(m.getMessage(), exception);
					}
					else {
						_debug.config(m.getMessage());
					}
				}
				else if(level.equals(FINE)) {
					if(exception != null) {
						_debug.fine(m.getMessage(), exception);
					}
					else {
						_debug.fine(m.getMessage());
					}
				}
				else if(level.equals(FINER)) {
					if(exception != null) {
						_debug.finer(m.getMessage(), exception);
					}
					else {
						_debug.finer(m.getMessage());
					}
				}
				else if(level.equals(FINEST)) {
					if(exception != null) {
						_debug.finest(m.getMessage(), exception);
					}
					else {
						_debug.finest(m.getMessage());
					}
				}
				else if(!level.equals(OFF)) {
					_debug.warning("Nicht unterstützter Level für Betriebsmeldung", level);
				}
			}
		});
		register(new OperatingMessageSink() {
			@Override
			public void publish(final OperatingMessageInterface m) {
				MessageSender.getInstance().sendMessage(m.getMessageId(), m.getDomain(), m.getMessageTypeAddOn(), m.getGrade(), m.getObject(), m.getState(), null, m.getMessage());
			}
		});
	}

	/**
	 * Benutzerdefinierte Methode zum "Versand" einer Betriebsmeldung
	 * @param message Betriebsmeldungs-Objekt
	 */
	public abstract void publish(final OperatingMessageInterface message);

}

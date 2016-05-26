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

import java.util.Objects;

/**
 * Eine Betriebsmeldung mit Zustand, über die beispielsweise zusammengehörige Änderungsmeldungen
 * oder Gutmeldungen verschickt werden können.
 *
 * @author Kappich Systemberatung
 */
public final class PersistentOperatingMessage implements OperatingMessageInterface {
	
	private final String _id;
	private OperatingMessageInterface _operatingMessage;
	private MessageState _state = MessageState.NEW_MESSAGE;
	private String _message = null;

	/** 
	 * Erstellt ein neues PersistentOperatingMessage-Objekt
	 */
	PersistentOperatingMessage(final String id, final OperatingMessageInterface operatingMessage) {
		_id = id;
		_operatingMessage = operatingMessage;
	}

	/**
	 * Setzt eine neue Nachricht. Von den übergebenen Objekt werden alle Informationen außer der Zustand übernommen.
	 * @param operatingMessage Nachricht
	 */
	public void setMessage(OperatingMessageInterface operatingMessage){
		Objects.requireNonNull(operatingMessage, "operatingMessage == null");
		_message = null;
		if(_operatingMessage == this) return;
		_operatingMessage = operatingMessage;
	}

	/**
	 * Setzt einen neuen Betriebsmeldungstext.
	 * @param message Betriebsmeldungstext
	 */
	public void setMessage(final String message) {
		_message = message;
	}

	/** 
	 * Gibt den Betriebsmeldungstext zurück
	 * @return den Betriebsmeldungstext
	 */
	@Override
	public String getMessage() {
		if(_message != null) return _message;
		return _operatingMessage.getMessage();
	}

	@Override
	public MessageGrade getGrade() {
		return _operatingMessage.getGrade();
	}

	@Override
	public String getId() {
		return _id;
	}

	/**
	 * Sendet eine Wiederholungsmeldung
	 */
	public void sendRepeatMessage(){
		if(_state == MessageState.GOOD_MESSAGE) throw new IllegalStateException("Betriebsmeldung wurde bereits gut gemeldet");
		_state = MessageState.REPEAT_MESSAGE;
		OperatingMessageSink.publishEverywhere(this);
	}

	/**
	 * Sendet eine Änderungsmeldung
	 */
	public void sendChangeMessage(){
		if(_state == MessageState.GOOD_MESSAGE) throw new IllegalStateException("Betriebsmeldung wurde bereits gut gemeldet");
		_state = MessageState.CHANGE_MESSAGE;
		OperatingMessageSink.publishEverywhere(this);
	}

	/**
	 * Sendet eine Gutmeldung
	 */
	public void sendGoodMessage(){
		if(_state == MessageState.GOOD_MESSAGE) throw new IllegalStateException("Betriebsmeldung wurde bereits gut gemeldet");
		_state = MessageState.GOOD_MESSAGE;
		OperatingMessageSink.publishEverywhere(this);
	}

	/**
	 * Sendet eine Änderungsmeldung mit dem übergebenen Betriebsmeldungstext
	 * @param message Betriebsmeldungstext
	 */
	public void update(String message){
		setMessage(message);
		sendChangeMessage();
	}

	/**
	 * Sendet eine Gutmeldung mit dem übergebenen Betriebsmeldungstext
	 * @param message Betriebsmeldungstext
	 */
	public void resolve(String message){
		setMessage(message);
		sendGoodMessage();
	}

	/**
	 * Sendet eine Änderungsmeldung mit den übergebenen Betriebsmeldungsdaten
	 * @param message Betriebsmeldung
	 */
	public void update(OperatingMessageInterface message){
		setMessage(message);
		sendChangeMessage();
	}

	/**
	 * Sendet eine Gutmeldung mit den übergebenen Betriebsmeldungsdaten
	 * @param message Betriebsmeldung
	 */
	public void resolve(OperatingMessageInterface message){
		setMessage(message);
		sendGoodMessage();
	}

	@Override
	public MessageType getDomain() {
		return _operatingMessage.getDomain();
	}

	@Override
	public String getMessageTypeAddOn() {
		return _operatingMessage.getMessageTypeAddOn();
	}

	@Override
	public SystemObject getObject() {
		return _operatingMessage.getObject();
	}

	@Override
	public MessageState getState() {
		return _state;
	}

	@Override
	public Throwable getException() {
		if(_message != null) return null;
		return _operatingMessage.getException();
	}

	@Override
	public String toString() {
		return getId() + " " + getState() + ": " + _operatingMessage;
	}
}

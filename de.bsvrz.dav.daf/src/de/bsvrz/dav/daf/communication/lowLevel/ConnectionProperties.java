/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.communication.lowLevel;

/**
 * Speichert die Eigenschaften einer Verbindung zum Datenverteiler.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConnectionProperties {

	/** Die erste Ebene der Kommunikation (TCP/IP...) */
	private LowLevelCommunicationInterface _lowLevelCommunication;

	/** Das Authentifikationsverfahren */
	private AuthentificationProcess _authentificationProcess;

	/** Der Benutzername */
	private String _userName;

	/** Das verschlüsselte Benutzer-Passwort */
	private String _userPassword;

	/** Die Zeit nach der spätestens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde. */
	private long _keepAliveSendTimeOut;

	/** Die Zeit in der spätestens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert. */
	private long _keepAliveReceiveTimeOut;

	/** Die Grösse in Bytes des Sendebuffers */
	private int _sendBufferSize;

	/** Die Grösse in Bytes des Empfangsbuffers */
	private int _receiveBufferSize;


	/**
	 * Dieser Konstruktor wird für Tests benötigt.
	 */
	public ConnectionProperties() {
	}

	public ConnectionProperties(
			LowLevelCommunicationInterface lowLevelCommunication,
			AuthentificationProcess authentificationProcess,
			String userName,
			String userPassword,
			long keepAliveSendTimeOut,
			long keepAliveReceiveTimeOut,
			int sendBufferSize,
			int receiveBufferSize
	) {

		_lowLevelCommunication = lowLevelCommunication;
		_authentificationProcess = authentificationProcess;
		_userName = userName;
		_userPassword = userPassword;
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
		_sendBufferSize = sendBufferSize;
		_receiveBufferSize = receiveBufferSize;
	}

	/**
	 * Gibt die unterste Kommunikationskomponente zurück.
	 *
	 * @return die unterste Kommunikationskomponente
	 */
	public LowLevelCommunicationInterface getLowLevelCommunication() {
		return _lowLevelCommunication;
	}

	/**
	 * Setzt die unterste Kommunikationskomponente.
	 *
	 * @param lowLevelCommunication die neue unterste Kommunikationskomponente
	 */
	public final void setLowLevelCommunication(LowLevelCommunicationInterface lowLevelCommunication) {
		_lowLevelCommunication = lowLevelCommunication;
	}

	/**
	 * Gibt die Authentifikationskomponente zurück.
	 *
	 * @return die Authentifikationskomponente
	 */
	public final AuthentificationProcess getAuthentificationProcess() {
		return _authentificationProcess;
	}

	/**
	 * Setzt die Authentifikationskomponente.
	 *
	 * @param authentificationProcess die Authentifikationskomponente
	 */
	public final void setAuthentificationProcess(AuthentificationProcess authentificationProcess) {
		_authentificationProcess = authentificationProcess;
	}

	/**
	 * Gibt den Benutzernamen zurück.
	 *
	 * @return den Namen des Benutzers
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * Setzt den Benutzernamen auf den neuen Wert.
	 *
	 * @param userName der neue Benutzername
	 */
	public final void setUserName(String userName) {
		_userName = userName;
	}

	/**
	 * Gibt das Benutzerpasswort zurück.
	 *
	 * @return das Benutzerpasswort
	 */
	public String getUserPassword() {
		return _userPassword;
	}

	/**
	 * Setzt das Benutzerpasswort auf den neuen Wert.
	 *
	 * @param userPassword das neue Benutzerpasswort
	 */
	public final void setUserPassword(String userPassword) {
		_userPassword = userPassword;
	}

	/**
	 * Gibt die Zeit zurück, nach der spätestens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde.
	 *
	 * @return die Keepalive-Sendezeit
	 */
	public final long getKeepAliveSendTimeOut() {
		return _keepAliveSendTimeOut;
	}

	/**
	 * Setzt die Zeit, nach der spätestens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde.
	 *
	 * @param keepAliveSendTimeOut die Keepalive-Sendezeit
	 */
	public final void setKeepAliveSendTimeOut(long keepAliveSendTimeOut) {
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
	}

	/**
	 * Gibt die Zeit zurück, in der spätestens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert.
	 *
	 * @return die Keepalive-Empfangszeit
	 */
	public final long getKeepAliveReceiveTimeOut() {
		return _keepAliveReceiveTimeOut;
	}

	/**
	 * Setzt die Zeit, in der spätestens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert.
	 *
	 * @param keepAliveReceiveTimeOut die Keepalive-Empfangszeit
	 */
	public final void setKeepAliveReceiveTimeOut(long keepAliveReceiveTimeOut) {
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
	}

	/**
	 * Gibt die Grösse in Bytes des Sendebuffers zurück.
	 *
	 * @return die Grösse des Sendebuffers
	 */
	public final int getSendBufferSize() {
		return _sendBufferSize;
	}

	/**
	 * Setzt die Grösse des Sendebuffers.
	 *
	 * @param sendBufferSize die neue Grösse des Sendebuffers
	 */
	public final void setSendBufferSize(int sendBufferSize) {
		_sendBufferSize = sendBufferSize;
	}

	/**
	 * Gibt die Grösse in Bytes des Empfangsbuffers zurück.
	 *
	 * @return die Grösse des Empfangsbuffers
	 */
	public final int getReceiveBufferSize() {
		return _receiveBufferSize;
	}

	/**
	 * Setzt die Grösse des Empfangsbuffers.
	 *
	 * @param receiveBufferSize die neue Grösse des Empfangsbuffers
	 */
	public final void setReceiveBufferSize(int receiveBufferSize) {
		_receiveBufferSize = receiveBufferSize;
	}
}

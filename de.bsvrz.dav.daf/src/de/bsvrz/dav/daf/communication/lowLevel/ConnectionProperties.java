/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.communication.lowLevel;

/**
 * Speichert die Eigenschaften einer Verbindung zum Datenverteiler.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public class ConnectionProperties {

	/** Die erste Ebene der Kommunikation (TCP/IP...) */
	private LowLevelCommunicationInterface _lowLevelCommunication;

	/** Das Authentifikationsverfahren */
	private AuthentificationProcess _authentificationProcess;

	/** Der Benutzername */
	private String _userName;

	/** Das verschl�sselte Benutzer-Passwort */
	private String _userPassword;

	/** Die Zeit nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde. */
	private long _keepAliveSendTimeOut;

	/** Die Zeit in der sp�testens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert. */
	private long _keepAliveReceiveTimeOut;

	/** Die Gr�sse in Bytes des Sendebuffers */
	private int _sendBufferSize;

	/** Die Gr�sse in Bytes des Empfangsbuffers */
	private int _receiveBufferSize;


	/**
	 * Dieser Konstruktor wird f�r Tests ben�tigt.
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
	 * Gibt die unterste Kommunikationskomponente zur�ck.
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
	 * Gibt die Authentifikationskomponente zur�ck.
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
	 * Gibt den Benutzernamen zur�ck.
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
	 * Gibt das Benutzerpasswort zur�ck.
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
	 * Gibt die Zeit zur�ck, nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde.
	 *
	 * @return die Keepalive-Sendezeit
	 */
	public final long getKeepAliveSendTimeOut() {
		return _keepAliveSendTimeOut;
	}

	/**
	 * Setzt die Zeit, nach der sp�testens ein keepalive Telegramm geschickt werden muss, wenn in dieser Zeit kein Telegramm empfangen wurde.
	 *
	 * @param keepAliveSendTimeOut die Keepalive-Sendezeit
	 */
	public final void setKeepAliveSendTimeOut(long keepAliveSendTimeOut) {
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
	}

	/**
	 * Gibt die Zeit zur�ck, in der sp�testens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert.
	 *
	 * @return die Keepalive-Empfangszeit
	 */
	public final long getKeepAliveReceiveTimeOut() {
		return _keepAliveReceiveTimeOut;
	}

	/**
	 * Setzt die Zeit, in der sp�testens ein Telegramm empfangen werden muss, sonst wird die Verbindung terminiert.
	 *
	 * @param keepAliveReceiveTimeOut die Keepalive-Empfangszeit
	 */
	public final void setKeepAliveReceiveTimeOut(long keepAliveReceiveTimeOut) {
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
	}

	/**
	 * Gibt die Gr�sse in Bytes des Sendebuffers zur�ck.
	 *
	 * @return die Gr�sse des Sendebuffers
	 */
	public final int getSendBufferSize() {
		return _sendBufferSize;
	}

	/**
	 * Setzt die Gr�sse des Sendebuffers.
	 *
	 * @param sendBufferSize die neue Gr�sse des Sendebuffers
	 */
	public final void setSendBufferSize(int sendBufferSize) {
		_sendBufferSize = sendBufferSize;
	}

	/**
	 * Gibt die Gr�sse in Bytes des Empfangsbuffers zur�ck.
	 *
	 * @return die Gr�sse des Empfangsbuffers
	 */
	public final int getReceiveBufferSize() {
		return _receiveBufferSize;
	}

	/**
	 * Setzt die Gr�sse des Empfangsbuffers.
	 *
	 * @param receiveBufferSize die neue Gr�sse des Empfangsbuffers
	 */
	public final void setReceiveBufferSize(int receiveBufferSize) {
		_receiveBufferSize = receiveBufferSize;
	}
}

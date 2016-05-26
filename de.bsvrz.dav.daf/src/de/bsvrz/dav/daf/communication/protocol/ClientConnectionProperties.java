/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.protocol;

import de.bsvrz.dav.daf.communication.lowLevel.*;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.InitialisationNotCompleteException;

/**
 * Diese Klasse enthält die Eigenschaften, die benötigt werden, um eine Verbindung zum Datenverteiler aufzubauen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ClientConnectionProperties extends ConnectionProperties {

	/** Der Applikationsname */
	private String _applicationName;

	/** Die PId des Applikationstyps */
	private String _applicationTypePid;

	/** Die Pid der Konfiguration */
	private String _configurationPid;

	/** Die Kommunikationsadresse */
	private String _address;

	/** Die Kommunikationssubadresse */
	private int _subAddress;

	/** Enthält die Parameter für die Kommunikation zwischen Applikation und Datenverteiler. */
	private CommunicationParameters _communicationParameters;

	/** Enthält den via Aufrufparameter von Start/Stopp vorgegebenen Inkarnationsnamen oder <code>""</code>, falls das Aufrufargument nicht angegeben wurde. */
	private final String _incarnationName;

	public ClientConnectionProperties(ClientDavParameters clientDavParameters) throws ConnectionException {
		super(
				null,
				null,
				clientDavParameters.getUserName(),
				clientDavParameters.getUserPassword(),
				clientDavParameters.getCommunicationParameters().getSendKeepAliveTimeout(),
				clientDavParameters.getCommunicationParameters().getReceiveKeepAliveTimeout(),
				clientDavParameters.getAdjustedOutputBufferSize(),
				clientDavParameters.getAdjustedInputBufferSize()
		);
		try {
			String comProtocol = clientDavParameters.getLowLevelCommunicationName();
			if(comProtocol == null) {
				throw new InitialisationNotCompleteException("Unbekannter Kommunikationsprotokollname.");
			}
			Class aClass = Class.forName(comProtocol);
			if(aClass == null) {
				throw new InitialisationNotCompleteException("Unbekannter Kommunikationsprotokollname.");
			}
			ConnectionInterface connection = (ConnectionInterface)aClass.newInstance();
			setLowLevelCommunication(
					new LowLevelCommunication(
							connection,
							clientDavParameters.getAdjustedOutputBufferSize(),
							clientDavParameters.getAdjustedInputBufferSize(),
							clientDavParameters.getCommunicationParameters().getSendKeepAliveTimeout(),
							clientDavParameters.getCommunicationParameters().getReceiveKeepAliveTimeout(),
							LowLevelCommunication.HANDLE_CONFIG_RESPONCES_MODE,
							false
					)
			);

			String authentificationName = clientDavParameters.getAuthentificationProcessName();
			if(authentificationName == null) {
				throw new InitialisationNotCompleteException("Unbekanntes Authentifikationsverfahren.");
			}
			aClass = Class.forName(authentificationName);
			if(aClass == null) {
				throw new InitialisationNotCompleteException("Unbekanntes Authentifikationsverfahren.");
			}
			setAuthentificationProcess((AuthentificationProcess)aClass.newInstance());

			_applicationName = clientDavParameters.getApplicationName();
			_incarnationName = clientDavParameters.getIncarnationName();
			_applicationTypePid = clientDavParameters.getApplicationTypePid();
			_configurationPid = clientDavParameters.getConfigurationPid();
			_address = clientDavParameters.getDavCommunicationAddress();
			_subAddress = clientDavParameters.getDavCommunicationSubAddress();
			_communicationParameters = clientDavParameters.getCommunicationParameters();
		}
		catch(ClassNotFoundException ex) {
			throw new InitialisationNotCompleteException("Fehler beim Erzeugen der logischen Verbindung zum Datenverteiler.", ex);
		}
		catch(InstantiationException ex) {
			throw new InitialisationNotCompleteException("Fehler beim Erzeugen der logischen Verbindung zum Datenverteiler.", ex);
		}
		catch(IllegalAccessException ex) {
			throw new InitialisationNotCompleteException("Fehler beim Erzeugen der logischen Verbindung zum Datenverteiler.", ex);
		}
	}

	/**
	 * Gibt die Parameter für die Kommunikation zwischen Applikation und Datenverteiler zurück.
	 *
	 * @return die Parameter für die Kommunikation zwischen Applikation und Datenverteiler
	 */
	public CommunicationParameters getCommunicationParameters() {
		return _communicationParameters;
	}

	/**
	 * Gibt den Namen der Applikation zurück.
	 *
	 * @return der Name der Applikation
	 */
	public final String getApplicationName() {
		return _applicationName;
	}


	/**
	 * Liefert den via Aufrufparameter von Start/Stopp vorgegebenen Inkarnationsnamen.
	 * @return Inkarnationsname oder <code>""</code>, falls das entsprechende Aufrufargument nicht angegeben wurde.
	 */
	public String getIncarnationName() {
		return _incarnationName;
	}

	/**
	 * Setzt den Namen der Applikation.
	 *
	 * @param applicationName Name der Applikation
	 */
	public final void setApplicationName(String applicationName) {
		_applicationName = applicationName;
	}

	/**
	 * Gibt den Typ der Applikation zurück.
	 *
	 * @return Typ der Applikation
	 */
	public final String getApplicationTypePid() {
		return _applicationTypePid;
	}

	/**
	 * Setzt den Typ der Applikation.
	 *
	 * @param applicationTypePid Typ der Applikation
	 */
	public final void setApplicationTypePid(String applicationTypePid) {
		_applicationTypePid = applicationTypePid;
	}

	/**
	 * Gibt die Pid der Konfiguration zurück.
	 *
	 * @return die Pid der Konfiguration
	 */
	public final String getConfigurationPid() {
		return _configurationPid;
	}

	/**
	 * Setzt die Pid der Konfiguration.
	 *
	 * @param configurationPid Pid der Konfiguration
	 */
	public final void setConfigurationPid(String configurationPid) {
		_configurationPid = configurationPid;
	}

	/**
	 * Gibt die Kommunikationsadresse des Datenverteilers zurück.
	 *
	 * @return die Kommunikationsadresse
	 */
	public final String getCommunicationAddress() {
		return _address;
	}

	/**
	 * Setzt die Kommunikationsadresse des Datenverteilers.
	 *
	 * @param address die Kommunikationsadresse des Datenverteilers
	 */
	public final void setCommunicationAddress(String address) {
		_address = address;
	}

	/**
	 * Gibt die Kommunikationssubadresse des Datenverteilers zurück.
	 *
	 * @return die Kommunikationssubadresse
	 */
	public final int getCommunicationSubAddress() {
		return _subAddress;
	}

	/**
	 * Setzt die Kommunikationssubadresse des Datenverteilers.
	 *
	 * @param subAddress die Kommunikationssubadresse
	 */
	public final void setCommunicationSubAddress(int subAddress) {
		_subAddress = subAddress;
	}
}

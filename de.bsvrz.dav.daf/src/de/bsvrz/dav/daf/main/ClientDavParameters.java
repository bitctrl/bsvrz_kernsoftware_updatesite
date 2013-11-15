/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters;
import de.bsvrz.dav.daf.communication.lowLevel.ServerConnectionInterface;
import de.bsvrz.dav.daf.main.impl.ArgumentParser;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.InvalidArgumentException;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Parameter f�r die Datenverteiler-Applikationsfunktionen. Diese Klasse implementiert die Schnittstelle DatenverteilerApplikationsfunktionen-Starter. Beim
 * Erzeugen eines Objekts dieser Klasse werden die Parameter auf die in den Aufrufargumenten der Applikation angegebenen Werte bzw. auf die festgelegten
 * Default-Werte gesetzt. Einzelne Parameter k�nnen mit den entsprechenden Zugriffsmethoden gesetzt und abgefragt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10092 $
 * @verweis sst.DatenverteilerApplikationsfunktionen-Starter Schnittstellenbeschreibung
 */
public class ClientDavParameters {

	/** Der Debug-Logger der Klasse */
	static private final Debug _debug = Debug.getLogger();

	/**
	 * Aufrufargument zur Einstellung der Puffergr��en. Mit diesem Aufrufargument m�ssen drei durch Doppelpunkt getrennte Zahlen angegeben werden:
	 * <p/>
	 * <code>-puffer=sendepuffer:empfangspuffer:auslieferungspuffer</code>
	 * <p/>
	 * Alle Angaben sind als Anzahl von Bytes zu verstehen. <code>sendepuffer</code> und <code>empfangspuffer</code> spezifizieren die Gr��e der Puffer, die bei
	 * der Datenverteilerkommunikation zur Zwischenspeicherung von zu sendenden bzw. empfangenen Telegrammen verwendet werden. <code>auslieferungspuffer</code>
	 * spezifiziert die Gr��e des Auslieferungspuffers, der zur Zwischenspeicherung von an die Applikation auszuliefernden Datens�tzen verwendet wird.
	 */
	private static final String BUFFER_KEY = "-puffer";

	/**
	 * Aufrufargument mit dem Inkarnationsname der von Start/Stopp vorgegeben wird, um eine eindeutige Zuordnung zwischen gestartetem Proze� und dem
	 * entsprechenden Applikationsobjekt herzustellen.
	 */
	private static final String INCARNATION_KEY = "-inkarnationsName=";

	/** Parameter Schl�ssel */
	private static final String ADDRESS_SUBADDRESS_KEY = "-datenverteiler=";

	/** Parameter Schl�ssel */
	private static final String CONFIGURATION_PID_KEY = "-konfigurationsBereich=";

	/** Parameter Schl�ssel */
	private static final String CONFIGURATION_PATH_KEY = "-lokaleSpeicherungKonfiguration=";

	/** Parameter Schl�ssel */
	private static final String USER_NAME_KEY = "-benutzer=";

	/** Parameter Schl�ssel */
	private static final String AUTHENTIFICATION_FILE_KEY = "-authentifizierung=";

	/** Parameter Schl�ssel */
	private static final String AUTHENTIFICATION_PROCESS_KEY = "-authentifizierungsVerfahren=";

	/** Parameter Schl�ssel */
	private static final String SEND_KEEP_ALIVE_TIMEOUT_KEY = "-timeoutSendeKeepAlive=";

	/** Parameter Schl�ssel */
	private static final String RECEIVE_KEEP_ALIVE_TIMEOUT_KEY = "-timeoutEmpfangeKeepAlive=";

	/** Parameter Schl�ssel */
	private static final String ASPECT_REDIRECTION_KEY = "-aspekt=";

	/** Parameter Schl�ssel */
	private static final String SIMULATION_VARIANT_KEY = "-simVariante=";

	/** Parameter Schl�ssel */
	private static final String FLOW_CONTROL_PARAMETERS_KEY = "-durchsatzPruefung=";

	/** Parameter Schl�ssel */
	private static final String PARAMETER_SEPARATOR = ":";

	/** Parameter Schl�ssel */
	private static final String TEST_CONNECTION_KEY = "-anmeldungFuerTestzwecke=";

	/** Die Ressourcen des Clients. */
	private ResourceBundle _resourceBundle = ResourceBundle.getBundle("de.bsvrz.dav.daf.main.impl.clientResourceBundle", Locale.getDefault());

	///////////////////////////////////////////////////////////
	/// �bergabeparameter der main Methode der Applikation ///
	///////////////////////////////////////////////////////////

	/** Datenverteiler-Adresse */
	private String _address;

	/** Die Pid der Konfiguration */
	private String _configurationPid;

	/** Speicherort der lokalen Konfiguration. */
	private String _configurationPath;

	/** Der Name des Benutzers */
	private String _userName;

	/** Der Passwort des Benutzers */
	private String _userPassword;

	/** die Subadresse des Datenverteilers */
	private int _subAddress;

	/** Die Standardm��ig zu verwendende Simulationsvariante. Wenn die Methode nicht aufgerufen wird, wird die Variante <code>0</code> benutzt. */
	private short _simulationVariant;

	/** Tabelle der Informationen �ber Umleitungen der Aspekte */
	private Hashtable<AttributeGroupAspectObject, AttributeGroupAspectObject> _aspectToSubstituteTable = new Hashtable<AttributeGroupAspectObject, AttributeGroupAspectObject>();

	/** Tabelle der Informationen �ber Umleitungen der Aspekte */
	private Hashtable<AttributeGroupAspectObject, AttributeGroupAspectObject> _substituteToAspectTable = new Hashtable<AttributeGroupAspectObject, AttributeGroupAspectObject>();

	////////////////////////////////////////////////////////////////
	/// Parameter, die von der Applikation gesetzt werden k�nnen ///
	////////////////////////////////////////////////////////////////

	/** Der Applikationsname (Default: Testapplikation) */
	private String _applicationName;

	/** Die Pid der Applikationstyp (Default: typ.applikation) */
	private String _applicationTypePid;

	////////////////////////////////////////////////////////////
	/// Parameter, die von der property Datei gelesen werden ///
	////////////////////////////////////////////////////////////

	/** Der Authentifikationsprozessname (Default: HMAC-MD5) */
	private String _authentificationProcessName;

	/** Der Name des Kommunikationsprotokolls (Default: TCP-IP) */
	private String _lowLevelCommunicationName;

	/** Die Gr��e des Sendepuffers in Bytes, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird. */
	private int _outputBufferSize = 1000000;

	/** Die Gr��e des Empfangspuffers in Bytes, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird. */
	private int _inputBufferSize = 1000000;

	/**
	 * Die Gr��e des Auslieferungspuffers in Bytes, der zur Zwischenspeicherung von Datens�tzen genutzt wird, die an einen Empf�nger der Applikation versendet
	 * werden sollen.
	 *
	 * @see ClientReceiverInterface#update(ResultData[])
	 */
	private int _deliveryBufferSize = 2500000;

	/**
	 * Die Verz�gerungszeit zur �bertragung von gepufferten und zu versendenden Telegrammen. Die �bertragung der gesammelten Daten im Sendepuffer findet erst
	 * statt, wenn die hier angegebene Zeit lang keine Daten mehr in der Puffer geschrieben wurden oder der Sendepuffer voll ist.
	 */
	private long _communicationSendFlushDelay;

	/** Die maximale Gr��e von Datentelegrammen. */
	private int _maxTelegramSize;

	/**
	 * Speichert, ob die Verbindung zu Testzwecken aufgebaut wird. true bedeutet "Ja" und bestimmte Tests, die normalerweise die Anmeldung verhindern w�rden,
	 * werden nicht durchgef�hrt; false bedeutet "nein".
	 * <p/>
	 * Der default-Wert ist "nein".
	 */
	private boolean _connectionForTests = false;

	/** Enth�lt die Parameter f�r die Kommunikation zwischen Applikation und Datenverteiler. */
	private final CommunicationParameters _communicationParameters;

	/** Enth�lt den via Aufrufparameter von Start/Stopp vorgegebenen Inkarnationsnamen oder <code>""</code>, falls das Aufrufargument nicht angegeben wurde. */
	private final String _incarnationName;

	/** Enth�lt den Namen der Applikation, der im Namen der lokalen Cache-Datei f�r Konfigurationsdaten verwendet werden soll. Der Applikationsname kann mit
	 * dem Aufrufparameter -lokaleSpeicherungKonfiguration=pfadname:applikationsname von au�en vorgegeben werden.
	 * Wenn das Aufrufargument -lokaleSpeicherungKonfiguration nicht benutzt wurde oder im angegebenen Argument kein mit Doppelpunkt getrennter Name angegeben
	 * wurde, dann enth�lt dieses Field den Wert <code>null</code>.
	 */
	private String _applicationNameForLocalConfigurationCache = null;

	/**
	 * Erzeugt einen neuen Parametersatz mit Defaultwerten f�r die einzelnen Parameter.
	 *
	 * @throws MissingParameterException Bei formalen Fehlern beim Lesen der Defaultwerte.
	 */
	public ClientDavParameters() throws MissingParameterException {
		try {
			_communicationParameters = new CommunicationParameters();

			_configurationPath = null;
			_address = _resourceBundle.getString("Datenverteiler-Adresse");
			_userName = _resourceBundle.getString("Benutzername");
			_userPassword = _resourceBundle.getString("Benutzerpasswort");
			_applicationName = _resourceBundle.getString("Applikationsname");
			_applicationTypePid = _resourceBundle.getString("Applikationstyp-PID");
			_configurationPid = _resourceBundle.getString("KonfigurationsBereich-PID");
			_authentificationProcessName = _resourceBundle.getString("AuthentificationProcessName");
			try {
				Class.forName(_authentificationProcessName);
			}
			catch(ClassNotFoundException ex) {
				throw new MissingParameterException(
						"Die Implementierung des Authentifizierungsverfahrens existiert nicht: " + _authentificationProcessName
				);
			}
			_lowLevelCommunicationName = _resourceBundle.getString("KommunikationProtokollName");
			try {
				Class.forName(_lowLevelCommunicationName);
			}
			catch(ClassNotFoundException ex) {
				throw new MissingParameterException("Die Implementierung der Kommunikationsverfahrensklasse existiert nicht: " + _lowLevelCommunicationName);
			}

			String tmp;
			tmp = _resourceBundle.getString("Datenverteiler-Subadresse");
			_subAddress = Integer.parseInt(tmp);
			if(_subAddress < 0) {
				throw new MissingParameterException("Die Subadresse muss gr�sser gleich 0 sein");
			}

			long sendKeepAliveTimeout = Long.parseLong(_resourceBundle.getString("Keepalive-Sendezeitgrenze"));
			if(sendKeepAliveTimeout < 1000) {
				throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
			}
			_communicationParameters.setSendKeepAliveTimeout(sendKeepAliveTimeout);

			long receiveKeepAliveTimeout = Long.parseLong(_resourceBundle.getString("Keepalive-Empfangszeitgrenze"));
			if(receiveKeepAliveTimeout < 1000) {
				throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
			}
			_communicationParameters.setReceiveKeepAliveTimeout(receiveKeepAliveTimeout);

			try {
				tmp = _resourceBundle.getString("Sendepuffergr�sse");
				_outputBufferSize = Integer.parseInt(tmp);
			}
			catch(MissingResourceException ignore) {
			}
			try {
				tmp = _resourceBundle.getString("Empfangspuffergr�sse");
				_inputBufferSize = Integer.parseInt(tmp);
			}
			catch(MissingResourceException ignore) {
			}
			tmp = _resourceBundle.getString("SimulationVariante");
			_simulationVariant = Short.parseShort(tmp);
			if(_simulationVariant < 0) {
				throw new MissingParameterException("Die Simulationsvariante muss gr�sser gleich 0 sein");
			}
			tmp = _resourceBundle.getString("SendeVerz�gerung");
			_communicationSendFlushDelay = Long.parseLong(tmp);
			if(_communicationSendFlushDelay > 0) {
				CommunicationConstant.MAX_SEND_DELAY_TIME = _communicationSendFlushDelay;
			}
			tmp = _resourceBundle.getString("MaxTelegrammGr�sse");
			_maxTelegramSize = Integer.parseInt(tmp);
			if(_maxTelegramSize > 0) {
				CommunicationConstant.MAX_SPLIT_THRESHOLD = _maxTelegramSize;
			}
			else {
				throw new MissingParameterException("Die maximale Telegraml�nge muss gr�sser 0 sein");
			}
			float throughputControlSendBufferFactor = Integer.parseInt(_resourceBundle.getString("PufferF�llGrad")) * 0.01f;
			_communicationParameters.setThroughputControlSendBufferFactor(throughputControlSendBufferFactor);
			int throughputControlInterval = Integer.parseInt(_resourceBundle.getString("Pr�fIntervall")) * 1000;
			_communicationParameters.setThroughputControlInterval(throughputControlInterval);
			int minimumThroughput = Integer.parseInt(_resourceBundle.getString("MindestDurchsatz"));
			_communicationParameters.setMinimumThroughput(minimumThroughput);
			_incarnationName = "";
		}
		catch(NumberFormatException ex) {
			ex.printStackTrace();
			throw new MissingParameterException("Falsche Eingabe. Eine Zahl wird erwartet!");
		}
		catch(MissingResourceException ex) {
			ex.printStackTrace();
			throw new MissingParameterException(ex.getMessage());
		}
	}

	/**
	 * Erzeugt einen neuen Parametersatz mit Defaultwerten f�r die einzelnen Parameter und setzt die in den �bergebenen Aufrufargumenten angegebenen Parameter mit
	 * den jeweils angegebenen Werten. Der Konstruktor implementiert die Starterschnittstelle der Datenverteilerapplikationsfunktionen. Bekannte Aufrufargumente
	 * werden nach der Umsetzung auf <code>null</code> gesetzt, um der Applikation zu signalisieren, da� diese Argumente bereits interpretiert wurden. Unbekannte
	 * Aufrufargumente werden ignoriert. Es ist Aufgabe der Applikation die verbleibenden Argumente zu interpretieren, bzw. eine ensprechende Fehlermeldung zu
	 * erzeugen, wenn die Argumente nicht interpretiert werden k�nnen.
	 *
	 * @param argumentList Argumentliste mit den beim Programmstart �bergebenen Aufrufargumenten.
	 *
	 * @throws MissingParameterException Bei formalen Fehlern beim Lesen der Aufrufargumente oder der Defaultwerte.
	 */
	public ClientDavParameters(ArgumentList argumentList) throws MissingParameterException {
		this(argumentList.getArgumentStrings());
	}

	/**
	 * Gibt an, ob die Verbindung f�r Testzwecken aufgebaut werden soll.
	 * <p/>
	 * Wird die Verbindung f�r Testzwecken aufgebaut, so k�nnen zum Beispiel Anmeldungen stattfinden, die mit einer normalen Verbindung nicht m�glich sind.
	 * <p/>
	 * Wurde dieser Wert nicht gesetzt (im Konstruktor oder �ber den Setter) so wird immer <code>false</code> zur�ckgegeben.
	 *
	 * @return true = Die Verbindung soll f�r Testzwecken aufgebaut werden; false = Es handelt sich um eine normale Verbindung, es werden alle Pr�fungen
	 *         vollzogen.
	 */
	public boolean isConnectionForTests() {
		return _connectionForTests;
	}

	/**
	 * Legt fest, ob eine Verbindung f�r Testzwecke aufgebaut werden soll.
	 *
	 * @param connectionForTests true = Ja, es handelt sich um eine Verbindung, die nur f�r Testzwecke benutzt wird; false = Es handelt sich um eine normale
	 *                           Verbindung, die zum Datenverteiler aufgebaut werden soll.
	 *
	 * @see #isConnectionForTests()
	 */
	public void setConnectionForTests(final boolean connectionForTests) {
		_connectionForTests = connectionForTests;
	}

	/**
	 * Erzeugt einen neuen Parametersatz mit Defaultwerten f�r die einzelnen Parameter und setzt die in den �bergebenen Aufrufargumenten angegebenen Parameter mit
	 * den jeweils angegebenen Werten. Der Konstruktor implementiert die Starterschnittstelle der Datenverteilerapplikationsfunktionen. Bekannte Aufrufargumente
	 * werden nach der Umsetzung auf <code>null</code> gesetzt, um der Applikation zu signalisieren, da� diese Argumente bereits interpretiert wurden. Unbekannte
	 * Aufrufargumente werden ignoriert. Es ist Aufgabe der Applikation die verbleibenden Argumente zu interpretieren, bzw. eine ensprechende Fehlermeldung zu
	 * erzeugen, wenn die Argumente nicht interpretiert werden k�nnen.
	 *
	 * @param startArguments Die beim Programmstart �bergebenen Aufrufargumente
	 *
	 * @throws MissingParameterException Bei formalen Fehlern beim Lesen der Aufrufargumente oder der Defaultwerte.
	 */
	public ClientDavParameters(String startArguments[]) throws MissingParameterException {
		final ArgumentList argumentList = new ArgumentList(startArguments);
		try {
			_communicationParameters = new CommunicationParameters();
			String tmp, parameter;

			//Adress and subadress
			parameter = getParameter(startArguments, ADDRESS_SUBADDRESS_KEY);
			if(parameter == null) {
				_address = _resourceBundle.getString("Datenverteiler-Adresse");
				tmp = _resourceBundle.getString("Datenverteiler-Subadresse");
				_subAddress = Integer.parseInt(tmp);
			}
			else {
				try {
					String parameters[] = ArgumentParser.getParameters(parameter, ADDRESS_SUBADDRESS_KEY, PARAMETER_SEPARATOR);
					if((parameters != null) && (parameters.length == 2)) {
						_address = parameters[0];
						_subAddress = Integer.parseInt(parameters[1]);
					}
					else {
						throw new MissingParameterException(
								"Datenverteiler-Adresse muss folgende Formatierung besitzen: -datenverteiler=IP-Adresse:Portnummer"
						);
					}
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Datenverteiler-Adresse muss folgende Formatierung besitzen: -datenverteiler=IP-Adresse:Portnummer"
					);
				}
				catch(NumberFormatException ex) {
					throw new MissingParameterException(
							"Datenverteiler-Adresse muss folgende Formatierung besitzen: -datenverteiler=IP-Adresse:Portnummer"
					);
				}
			}
			if(_subAddress < 0) {
				throw new MissingParameterException("Die Subadresse muss gr�sser gleich 0 sein");
			}

			//Configuration PID
			parameter = getParameter(startArguments, CONFIGURATION_PID_KEY);
			if(parameter == null) {
				_configurationPid = _resourceBundle.getString("KonfigurationsBereich-PID");
			}
			else {
				try {
					_configurationPid = ArgumentParser.getParameter(parameter, CONFIGURATION_PID_KEY);
					if((_configurationPid == null) || (_configurationPid.length() == 0)) {
						throw new MissingParameterException(
								"KonfigurationsBereich-Pid-Parameter muss folgende Formatierung besitzen: -konfigurationsBereich=pid"
						);
					}
					if("null".equals(_configurationPid)) {
						_configurationPid = CommunicationConstant.LOCALE_CONFIGURATION_PID_ALIASE;
					}
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"KonfigurationsBereich-Pid-Parameter muss folgende Formatierung besitzen: -konfigurationsBereich=pid"
					);
				}
			}

			// Configuration path
			parameter = getParameter(startArguments, CONFIGURATION_PATH_KEY);
			if(parameter == null) {
				_configurationPath = null;
				_applicationNameForLocalConfigurationCache = null;
			}
			else {
				try {
					String prefix = "";
					String value = ArgumentParser.getParameter(parameter, CONFIGURATION_PATH_KEY);
					if(value.length()>=2) {
						// Wenn im zweiten Zeichen ein Doppelpunkt steht, dann wird dieser als zum Pfad zugeh�rig betrachtet damit das mit absoluten Pfadnamen
						// unter Windows funktioniert ( C:\xyz... )
						prefix = value.substring(0,2);
						value = value.substring(2,value.length());
					}
					final String[] strings = value.split(":", 2);
					_configurationPath = prefix + strings[0];
					_applicationNameForLocalConfigurationCache = (strings.length > 1 ? strings[1] : null);

				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"KonfigurationsLokaleSpeicherungsparameter muss folgende Formatierung besitzen: -lokaleSpeicherungKonfiguration=Zeichenkette"
					);
				}
			}

			//User name
			parameter = getParameter(startArguments, USER_NAME_KEY);
			if(parameter == null) {
				_userName = _resourceBundle.getString("Benutzername");
				_userPassword = _resourceBundle.getString("Benutzerpasswort");
			}
			else {
				try {
					_userName = ArgumentParser.getParameter(parameter, USER_NAME_KEY);
					if((_userName == null) || (_userName.length() == 0)) {
						throw new MissingParameterException(
								"Benutzername-Parameter muss folgende Formatierung besitzen: -benutzer=Zeichenkette"
						);
					}
					_userPassword = null;
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Benutzername-Parameter muss folgende Formatierung besitzen: -benutzer=Zeichenkette"
					);
				}
			}
			//Authentification file name
			if(_userPassword == null) {
				parameter = getParameter(startArguments, AUTHENTIFICATION_FILE_KEY);
				if(parameter == null) {
					throw new MissingParameterException("Authentifizierungsdatei sollte mit -authentifizierung=... angegeben werden");
				}
				else {
					try {
						tmp = ArgumentParser.getParameter(parameter, AUTHENTIFICATION_FILE_KEY);
						if((tmp == null) || (tmp.length() == 0)) {
							throw new MissingParameterException(
									"Authentifizierungsdatei-Parameter muss folgende Formatierung besitzen: -authentifizierung=Zeichenkette"
							);
						}
						else {
							Properties properties = new Properties();
							try {
								properties.load(new BufferedInputStream(new FileInputStream(tmp)));
								_userPassword = properties.getProperty(_userName);
								if((_userPassword == null) || (_userPassword.length() == 0)) {
									throw new MissingParameterException(
											"Das Passwort f�r den Benutzer " + _userName + " ist in der Authentifizierungsdatei " + tmp + " nicht vorhanden"
									);
								}
							}
							catch(IOException ex) {
								throw new MissingParameterException("Spezifizierte Authentifizierungsdatei nicht vorhanden");
							}
						}
					}
					catch(InvalidArgumentException ex) {
						ex.printStackTrace();
					}
				}
			}
			//Authentification process
			parameter = getParameter(startArguments, AUTHENTIFICATION_PROCESS_KEY);
			if(parameter == null) {
				_authentificationProcessName = _resourceBundle.getString("AuthentificationProcessName");
			}
			else {
				try {
					_authentificationProcessName = ArgumentParser.getParameter(parameter, AUTHENTIFICATION_PROCESS_KEY);
					if((_authentificationProcessName == null) || (_authentificationProcessName.length() == 0)) {
						throw new MissingParameterException(
								"Der Parameter f�r die Klasse des Authentifizierungsverfahren muss folgende Formatierung besitzen: -authentifizierungsVerfahren=Zeichenkette"
						);
					}
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Der Parameter f�r die Klasse des Authentifizierungsverfahren muss folgende Formatierung besitzen: -authentifizierungsVerfahren=Zeichenkette"
					);
				}
			}
			try {
				Class.forName(_authentificationProcessName);
			}
			catch(ClassNotFoundException ex) {
				throw new MissingParameterException(
						"Die Implementierung des Authentifizierungsverfahrens existiert nicht: " + _authentificationProcessName
				);
			}

			//Send keep alive timeout
			long sendKeepAliveTimeout;
			parameter = getParameter(startArguments, SEND_KEEP_ALIVE_TIMEOUT_KEY);
			if(parameter == null) {
				sendKeepAliveTimeout = Long.parseLong(_resourceBundle.getString("Keepalive-Sendezeitgrenze"));
			}
			else {
				try {
					tmp = ArgumentParser.getParameter(parameter, SEND_KEEP_ALIVE_TIMEOUT_KEY);
					if((tmp == null) || (tmp.length() == 0)) {
						throw new MissingParameterException(
								"Sende-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutSendeKeepAlive=Zahl"
						);
					}
					sendKeepAliveTimeout = Long.parseLong(tmp) * 1000;
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Sende-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutSendeKeepAlive=Zahl"
					);
				}
				catch(NumberFormatException ex) {
					throw new MissingParameterException(
							"Sende-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutSendeKeepAlive=Zahl"
					);
				}
			}
			if(sendKeepAliveTimeout < 1000) {
				throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
			}
			_communicationParameters.setSendKeepAliveTimeout(sendKeepAliveTimeout);

			//Receive keep alive timeout
			long receiveKeepAliveTimeout;
			parameter = getParameter(startArguments, RECEIVE_KEEP_ALIVE_TIMEOUT_KEY);
			if(parameter == null) {
				receiveKeepAliveTimeout = Long.parseLong(_resourceBundle.getString("Keepalive-Empfangszeitgrenze"));
			}
			else {
				try {
					tmp = ArgumentParser.getParameter(parameter, RECEIVE_KEEP_ALIVE_TIMEOUT_KEY);
					if((tmp == null) || (tmp.length() == 0)) {
						throw new MissingParameterException(
								"Empfang-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutEmpfangeKeepAlive=Zahl"
						);
					}
					receiveKeepAliveTimeout = Long.parseLong(tmp) * 1000;
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Empfang-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutEmpfangeKeepAlive=Zahl"
					);
				}
				catch(NumberFormatException ex) {
					throw new MissingParameterException(
							"Empfang-Keep-Alive-Timeout-Parameter muss folgende Formatierung besitzen: -timeoutEmpfangeKeepAlive=Zahl"
					);
				}
			}
			if(receiveKeepAliveTimeout < 1000) {
				throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
			}
			_communicationParameters.setReceiveKeepAliveTimeout(receiveKeepAliveTimeout);

			//Aspect
			while((parameter = getParameter(startArguments, ASPECT_REDIRECTION_KEY)) != null) {
				try {
					String parameters[] = ArgumentParser.getParameters(parameter, ASPECT_REDIRECTION_KEY, PARAMETER_SEPARATOR);
					if((parameters != null) && (parameters.length == 3) && (parameters[0] != null) && (parameters[0].length() > 0) && (parameters[1] != null)
					   && (parameters[1].length() > 0) && (parameters[2] != null) && (parameters[2].length() > 0)) {
						String attributeGroupPid = parameters[0];
						String aspectPid = parameters[1];
						String aspectSubstitutePid = parameters[2];
						addAspectRedirection(attributeGroupPid, aspectPid, aspectSubstitutePid);
					}
					else {
						throw new MissingParameterException(
								"Aspekt-Parameter muss folgende Formatierung besitzen: -aspekt=Zeichenkette:Zeichenkette:Zeichenkette"
						);
					}
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Aspekt-Parameter muss folgende Formatierung besitzen: -aspekt=Zeichenkette:Zeichenkette:Zeichenkette"
					);
				}
			}
			//Simulationsvariante
			parameter = getParameter(startArguments, SIMULATION_VARIANT_KEY);
			if(parameter != null) {
				try {
					tmp = ArgumentParser.getParameter(parameter, SIMULATION_VARIANT_KEY);
					if((tmp == null) || (tmp.length() == 0)) {
						throw new MissingParameterException(
								"Simulationsvariante-Parameter muss folgende Formatierung besitzen: -simVariante=Zahl"
						);
					}
					_simulationVariant = Short.parseShort(tmp);
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"Simulationsvariante-Parameter muss folgende Formatierung besitzen: -simVariante=Zahl"
					);
				}
				catch(NumberFormatException ex) {
					throw new MissingParameterException(
							"Simulationsvariante-Parameter muss folgende Formatierung besitzen: -simVariante=Zahl"
					);
				}
			}
			if(_simulationVariant < 0) {
				throw new MissingParameterException("Die Simulationsvariante muss gr�sser gleich 0 sein");
			}

			// Puffergr��en
			try {
				tmp = _resourceBundle.getString("Sendepuffergr�sse");
				_outputBufferSize = Integer.parseInt(tmp);
			}
			catch(MissingResourceException ignore) {
			}
			try {
				tmp = _resourceBundle.getString("Empfangspuffergr�sse");
				_inputBufferSize = Integer.parseInt(tmp);
			}
			catch(MissingResourceException ignore) {
			}
			if(argumentList.hasArgument(BUFFER_KEY)) {
				try {
					final String bufferArgs = argumentList.fetchArgument(BUFFER_KEY).asNonEmptyString();
					final String[] bufferArgStrings = bufferArgs.split(PARAMETER_SEPARATOR);
					if(bufferArgStrings.length != 3) throw new IllegalArgumentException("Es sollten drei durch Doppelpunkte getrennte Werte angegeben sein");
					_outputBufferSize = Integer.valueOf(bufferArgStrings[0].trim());
					_inputBufferSize = Integer.valueOf(bufferArgStrings[1].trim());
					_deliveryBufferSize = Integer.valueOf(bufferArgStrings[2].trim());
				}
				catch(Exception e) {
					throw new MissingParameterException(
							"Aufrufargument " + BUFFER_KEY + " sollte folgendes Format haben: " + BUFFER_KEY + "sendepuffer:empfangspuffer:auslieferungspuffer\n"
							+ "Die Werte spezifizieren jeweils die maximalen Puffergr��e als Anzahl von Bytes. " + e
					);
				}
			}

			_incarnationName = argumentList.fetchArgument(INCARNATION_KEY).asString();

			//Durchsatzpr�fung
			float throughputControlSendBufferFactor;
			int throughputControlInterval;
			int minimumThroughput;
			parameter = getParameter(startArguments, FLOW_CONTROL_PARAMETERS_KEY);
			if(parameter == null) {
				throughputControlSendBufferFactor = Integer.parseInt(_resourceBundle.getString("PufferF�llGrad")) * 0.01f;
				throughputControlInterval = Integer.parseInt(_resourceBundle.getString("Pr�fIntervall")) * 1000;
				minimumThroughput = Integer.parseInt(_resourceBundle.getString("MindestDurchsatz"));
			}
			else {
				try {
					String parameters[] = ArgumentParser.getParameters(parameter, FLOW_CONTROL_PARAMETERS_KEY, PARAMETER_SEPARATOR);
					if((parameters != null) && (parameters.length == 3)) {
						throughputControlSendBufferFactor = ((float)Byte.parseByte(parameters[0])) * 0.01f;
						throughputControlInterval = Short.parseShort(parameters[1]) * 1000;
						minimumThroughput = Integer.parseInt(parameters[2]);
					}
					else {
						throw new MissingParameterException(
								"DurchsatzPruefung-Parameter muss folgende Formatierung besitzen: -durchsatzPruefung=Zahl:Zahl:Zahl"
						);
					}
				}
				catch(InvalidArgumentException ex) {
					throw new MissingParameterException(
							"DurchsatzPruefung-Parameter muss folgende Formatierung besitzen: -durchsatzPruefung=Zahl:Zahl:Zahl"
					);
				}
				catch(NumberFormatException ex) {
					throw new MissingParameterException(
							"DurchsatzPruefung-Parameter muss folgende Formatierung besitzen: -durchsatzPruefung=Zahl:Zahl:Zahl"
					);
				}
			}
			_communicationParameters.setThroughputControlSendBufferFactor(throughputControlSendBufferFactor);
			_communicationParameters.setThroughputControlInterval(throughputControlInterval);
			_communicationParameters.setMinimumThroughput(minimumThroughput);

			// Default values from the property file for common parametrs
			_applicationName = _resourceBundle.getString("Applikationsname");
			_applicationTypePid = _resourceBundle.getString("Applikationstyp-PID");
			_lowLevelCommunicationName = _resourceBundle.getString("KommunikationProtokollName");
			try {
				Class.forName(_lowLevelCommunicationName);
			}
			catch(ClassNotFoundException ex) {
				throw new MissingParameterException("Die Kommunikationsverfahrensklasse existiert nicht");
			}
			tmp = _resourceBundle.getString("SendeVerz�gerung");
			_communicationSendFlushDelay = Long.parseLong(tmp);
			if(_communicationSendFlushDelay > 0) {
				CommunicationConstant.MAX_SEND_DELAY_TIME = _communicationSendFlushDelay;
			}
			tmp = _resourceBundle.getString("MaxTelegrammGr�sse");
			_maxTelegramSize = Integer.parseInt(tmp);
			if(_maxTelegramSize > 0) {
				CommunicationConstant.MAX_SPLIT_THRESHOLD = _maxTelegramSize;
			}
			else {
				throw new MissingParameterException("Die maximale Telegramml�nge muss gr�sser 0 sein");
			}

			// Pr�fen, ob die Verbindung im Testmodus aufgebaut werden soll
			parameter = getParameter(startArguments, TEST_CONNECTION_KEY);
			if(parameter != null) {
				try {
					tmp = ArgumentParser.getParameter(parameter, TEST_CONNECTION_KEY);
					if((tmp == null) || (tmp.length() == 0)) {
						throw new MissingParameterException(
								"Der Parameter um eine Verbindung zu Testzwecken anzulegen muss folgende Formatierung besitzen: " + TEST_CONNECTION_KEY
								+ "ja/nein"
						);
					}

					if("ja".equals(tmp.toLowerCase())) {
						_connectionForTests = true;
					}
					else if("nein".equals(tmp.toLowerCase())) {
						_connectionForTests = false;
					}
					else {
						// Die Eingabe war weder ja noch nein
						throw new MissingParameterException(
								"Der Parameter um eine Verbindung zu Testzwecken anzulegen muss folgende Formatierung besitzen: " + TEST_CONNECTION_KEY
								+ " ja/nein. Die gemachte Eingabe: " + tmp + " ist ung�ltig."
						);
					}
				}
				catch(InvalidArgumentException e) {
					throw new MissingParameterException(
							"Der Parameter um eine Verbindung zu Testzwecken anzulegen muss folgende Formatierung besitzen: " + TEST_CONNECTION_KEY + "ja/nein"
					);
				}
			}
		}
		catch(MissingResourceException ex) {
			ex.printStackTrace();
			throw new MissingParameterException(ex.getMessage());
		}
	}

//	/**
//	 * Erzeugt einen neuen Parametersatz und setzt die in dem �bergebenen Aufrufargument angegebenen Parameter mit den jeweils angegebenen Werten. Der Konstruktor
//	 * implementiert die Starterschnittstelle der Datenverteilerapplikationsfunktionen.
//	 *
//	 * @param serverDavParameters die Parameter des Servers
//	 *
//	 * @throws MissingParameterException Bei formalen Fehlern beim Lesen der Aufrufargumente oder der Defaultwerte.
//	 */
//	public ClientDavParameters(ServerDavParameters serverDavParameters) throws MissingParameterException {
//		_communicationParameters = new CommunicationParameters();
//		_configurationPath = null;
//		// If localmode set the pid of the configuration
//		if(serverDavParameters.isLocalMode()) {
//			Object objects[] = serverDavParameters.getLocalModeParameter();
//			if(objects == null) {
//				throw new IllegalStateException("Inkonsistente Parameter.");
//			}
//			_configurationPid = (String)objects[0];
//
//			//try {
//			//	_address = java.net.InetAddress.getLocalHost().getHostAddress();
//			//}
//			//catch (java.net.UnknownHostException ex) {
//			//	throw new IllegalStateException(ex.getMessage());
//			//}
//			// Das obige deaktivierte InetAddress.getLocalHost() lieferte die lokale IP-Adresse des Netzwerkanschlusses,
//			// dies machte Probleme, weil beim Abziehen des Netzwerkkabels in manchen Betriebssystemen (Windows)
//			// das Netzwerkinterface deaktiviert wird und deshalb die interne Verbindung im
//			// Datenverteiler dann nicht mehr funktioniert. Statt dessen wird jetzt f�r die interne Verbindung
//			// das Loopback-Interface mit der festen Adresse 127.0.0.1 benutzt, welches unabh�ngig von realen
//			// Netzwerkinterfaces funktioniert:
//			_address = "127.0.0.1";  // localhost �ber loopback
//
//			_subAddress = serverDavParameters.getApplicationConnectionsSubAddress();
//			_userName = "TransmitterLocalApplication@" + System.currentTimeMillis();
//			_userPassword = "TransmitterLocalApplication";
//			_applicationName = "TransmitterLocalApplication@" + System.currentTimeMillis();
//		}
//		// If remotemode set the adress and sub adress of the destination transmitter
//		else {
//			Object objects[] = serverDavParameters.getRemoteModeParameter();
//			if(objects == null) {
//				throw new IllegalStateException("Inkonsistente Parameter.");
//			}
//			_address = (String)objects[0];
//			_subAddress = ((Integer)objects[1]).intValue();
//			_configurationPid = (String)objects[2];
//			_userName = serverDavParameters.getUserName();
//			_userPassword = serverDavParameters.getUserPassword();
//			_applicationName = "TransmitterRemoteApplication@" + System.currentTimeMillis();
//		}
//
//		if(_subAddress < 0) {
//			throw new MissingParameterException("Die Subadresse muss gr�sser gleich 0 sein");
//		}
//
//		_simulationVariant = 0;
//		_communicationSendFlushDelay = 1000;
//		_applicationTypePid = "typ.applikation";
//		_authentificationProcessName = serverDavParameters.getAuthentificationProcessName();
//		try {
//			Class.forName(_authentificationProcessName);
//		}
//		catch(ClassNotFoundException ex) {
//			throw new IllegalStateException(
//					"Die Implementierung des Authentifizierungsverfahrens existiert nicht:" + _authentificationProcessName
//			);
//		}
//		_maxTelegramSize = serverDavParameters.getMaxDataTelegramSize();
//		long receiveKeepAliveTimeout = serverDavParameters.getReceiveKeepAliveTimeout();
//		if(receiveKeepAliveTimeout < 1000) {
//			throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
//		}
//		_communicationParameters.setReceiveKeepAliveTimeout(receiveKeepAliveTimeout);
//		long sendKeepAliveTimeout = serverDavParameters.getSendKeepAliveTimeout();
//		if(sendKeepAliveTimeout < 1000) {
//			throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
//		}
//		_communicationParameters.setSendKeepAliveTimeout(sendKeepAliveTimeout);
//		_outputBufferSize = serverDavParameters.getCommunicationOutputBufferSize();
//		_inputBufferSize = serverDavParameters.getCommunicationInputBufferSize();
//		_communicationParameters.setThroughputControlSendBufferFactor(CommunicationConstant.FLOW_CONTROL_FACTOR);
//		_communicationParameters.setThroughputControlInterval(CommunicationConstant.THROUGHPUT_CONTROL_INTERVAL);
//		_communicationParameters.setMinimumThroughput(CommunicationConstant.MINIMUM_THROUGHPUT);
//
//		try {
//			String communicationProtocolName = serverDavParameters.getLowLevelCommunicationName();
//			if(communicationProtocolName == null) {
//				throw new IllegalStateException("Inkonsistente Parameter.");
//			}
//			Class aClass = Class.forName(communicationProtocolName);
//			if(aClass == null) {
//				throw new IllegalStateException("Unbekannter Kommunikationsprotokollname.");
//			}
//			ServerConnectionInterface connection = (ServerConnectionInterface)aClass.newInstance();
//			if(connection == null) {
//				throw new IllegalStateException("Unbekannter Kommunikationsprotokollname.");
//			}
//			_lowLevelCommunicationName = connection.getPlainConnectionName();
//		}
//		catch(ClassNotFoundException ex) {
//			throw new IllegalStateException("Unbekannter Kommunikationsprotokollname.");
//		}
//		catch(InstantiationException ex) {
//			throw new IllegalStateException("Unbekannter Kommunikationsprotokollname.");
//		}
//		catch(IllegalAccessException ex) {
//			throw new IllegalStateException("Unbekannter Kommunikationsprotokollname.");
//		}
//	}

	/**
	 * Erzeugt einen neuen Parametersatz mit den angegebenen Werten.
	 *
	 * @param configurationPid Pid der Konfiguration
	 * @param address Kommunikationsadresse des Datenverteilers (IP-Adresse oder Rechnername)
	 * @param subAddress Kommunikationssubadresse des Datenverteilers (TCP-Portnummer)
	 * @param userName Benutzername
	 * @param userPassword Benutzerpasswort
	 * @param applicationName Applikationsname
	 * @param authentificationProcessName Klasse, die zur Authentifizierung genutzt werden soll
	 * @param maxTelegramSize Maximale Telegrammgr��e
	 * @param receiveKeepAliveTimeout KeepAlive-Timeout beim Empfang von Telegrammen in Millisekunden.
	 * @param sendKeepAliveTimeout KeepAlive-Timeout beim Versand von Telegrammen in Millisekunden.
	 * @param outputBufferSize Gr��e des Sendepuffers in Bytes, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 * @param inputBufferSize Gr��e des Empfangspuffers in Bytes, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 * @param communicationProtocolName Klassenname des zu verwendenden Kommunikationsprotokolls
	 * @throws MissingParameterException Bei formalen Fehlern beim Lesen der Aufrufargumente oder der Defaultwerte.
	 */
	public ClientDavParameters(
			String configurationPid, String address, int subAddress, String userName, String userPassword, String applicationName,
			final String authentificationProcessName, final int maxTelegramSize, final long receiveKeepAliveTimeout, final long sendKeepAliveTimeout,
			final int outputBufferSize, final int inputBufferSize, final String communicationProtocolName) throws MissingParameterException {
		_communicationParameters = new CommunicationParameters();
		_configurationPath = null;
		_configurationPid = configurationPid;
		_address = address;
		_subAddress = subAddress;
		_userName = userName;
		_userPassword = userPassword;
		_applicationName = applicationName;

		if(_subAddress < 0) {
			throw new MissingParameterException("Die Subadresse muss gr�sser gleich 0 sein");
		}

		_simulationVariant = 0;
		_communicationSendFlushDelay = 1000;
		_applicationTypePid = "typ.applikation";
		_authentificationProcessName = authentificationProcessName;
		try {
			Class.forName(_authentificationProcessName);
		}
		catch(ClassNotFoundException ex) {
			throw new IllegalStateException(
					"Die Implementierung des Authentifizierungsverfahrens existiert nicht:" + _authentificationProcessName
			);
		}
		_maxTelegramSize = maxTelegramSize;
		if(receiveKeepAliveTimeout < 1000) {
			throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
		}
		_communicationParameters.setReceiveKeepAliveTimeout(receiveKeepAliveTimeout);
		if(sendKeepAliveTimeout < 1000) {
			throw new MissingParameterException("Timeouts m�ssen gr�sser gleich als 1 Sekunde sein");
		}
		_communicationParameters.setSendKeepAliveTimeout(sendKeepAliveTimeout);
		_outputBufferSize = outputBufferSize;
		_inputBufferSize = inputBufferSize;
		_communicationParameters.setThroughputControlSendBufferFactor(CommunicationConstant.FLOW_CONTROL_FACTOR);
		_communicationParameters.setThroughputControlInterval(CommunicationConstant.THROUGHPUT_CONTROL_INTERVAL);
		_communicationParameters.setMinimumThroughput(CommunicationConstant.MINIMUM_THROUGHPUT);
		_incarnationName = "";
		try {
			if(communicationProtocolName == null) {
				throw new IllegalStateException("Kommunikationsprotokollname ist null.");
			}
			Class aClass = Class.forName(communicationProtocolName);
			ServerConnectionInterface connection = (ServerConnectionInterface)aClass.newInstance();
			_lowLevelCommunicationName = connection.getPlainConnectionName();
		}
		catch(ClassNotFoundException ex) {
			throw new IllegalStateException("Unbekannter Kommunikationsprotokollname: " + communicationProtocolName, ex);
		}
		catch(InstantiationException ex) {
			throw new IllegalStateException("Fehler bei der Instanziierung des Kommunikationsprotokolls: " + communicationProtocolName, ex);
		}
		catch(IllegalAccessException ex) {
			throw new IllegalStateException("Fehler bei Zugriff auf Kommunikationsprotokoll: " + communicationProtocolName, ex);
		}
	}

	/**
	 * Sucht in den angegebenen Argumenten nach dem Parameter, der mit dem spezifizierten Schl�ssel anf�ngt.
	 *
	 * @param arguments enth�lt die Parameter f�r den Datenverteiler
	 * @param key       der Schl�ssel
	 *
	 * @return Gibt den Parameter zum angegebenen Schl�ssel zur�ck, oder <code>null</code>, wenn der Parameter nicht existiert oder bereits ausgelesen wurde.
	 */
	private String getParameter(String arguments[], String key) {
		String parameter = null;
		if((arguments == null) || (key == null)) {
			return null;
		}
		for(int i = 0; i < arguments.length; ++i) {
			String tmp = arguments[i];
			if(tmp == null) {
				continue;
			}
			if(tmp.startsWith(key)) {
				parameter = tmp;
				arguments[i] = null;
				break;
			}
		}
		return parameter;
	}

	/**
	 * Bestimmt den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @return F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann aus den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gelesen werden.
	 */
	@Deprecated
	public final float getThroughputControlSendBufferFactor() {
		return _communicationParameters.getThroughputControlSendBufferFactor();
	}

	/**
	 * Bestimmt den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @return F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 *
	 * @deprecated Wird durch {@link #getThroughputControlSendBufferFactor} ersetzt.
	 */
	@Deprecated
	public final float getCacheThresholdPercentage() {
		return _communicationParameters.getThroughputControlSendBufferFactor();
	}

	/**
	 * Definiert den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @param sendBufferFactor F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann in den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gesetzt werden.
	 */
	@Deprecated
	public final void setThroughputControlSendBufferFactor(float sendBufferFactor) {
		_communicationParameters.setThroughputControlSendBufferFactor(sendBufferFactor);
	}

	/**
	 * Definiert den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @param cacheThresholdPercentage F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 *
	 * @deprecated Wird durch {@link #setThroughputControlSendBufferFactor} ersetzt.
	 */
	@Deprecated
	public final void setCacheThresholdPercentage(float cacheThresholdPercentage) {
		_communicationParameters.setThroughputControlSendBufferFactor(cacheThresholdPercentage);
	}

	/**
	 * Bestimmt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @return Intervalldauer in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann aus den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gelesen werden.
	 */
	@Deprecated
	public final long getThroughputControlInterval() {
		return _communicationParameters.getThroughputControlInterval();
	}

	/**
	 * Bestimmt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @return Intervalldauer in Millisekunden.
	 *
	 * @deprecated Wird durch {@link #getThroughputControlInterval} ersetzt.
	 */
	@Deprecated
	public final long getFlowControlThresholdTime() {
		return _communicationParameters.getThroughputControlInterval();
	}

	/**
	 * Setzt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @param interval Intervalldauer in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann in den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gesetzt werden.
	 */
	@Deprecated
	public final void setThroughputControlInterval(long interval) {
		_communicationParameters.setThroughputControlInterval(interval);
	}

	/**
	 * Setzt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @param flowControlThresholdTime Intervalldauer in Millisekunden.
	 *
	 * @deprecated Wird durch {@link #setThroughputControlInterval} ersetzt.
	 */
	@Deprecated
	public final void setFlowControlThresholdTime(long flowControlThresholdTime) {
		_communicationParameters.setThroughputControlInterval(flowControlThresholdTime);
	}

	/**
	 * Bestimmt den minimal erlaubten Verbindungsdurchsatz bei aktivierter Durchsatzpr�fung.
	 *
	 * @return Mindestdurchsatz in Byte pro Sekunde.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann aus den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gelesen werden.
	 */
	@Deprecated
	public final int getMinimumThroughput() {
		return _communicationParameters.getMinimumThroughput();
	}

	/**
	 * Bestimmt der minimum Verbindungsdurchsatz
	 *
	 * @return den minimalen Verbindungsdurchsatz
	 *
	 * @deprecated Wird durch {@link #getMinimumThroughput} ersetzt.
	 */
	@Deprecated
	public final int getMinConnectionSpeed() {
		return _communicationParameters.getMinimumThroughput();
	}

	/**
	 * Setzt den minimal erlaubten Verbindungsdurchsatz bei aktivierter Durchsatzpr�fung.
	 *
	 * @param throughput Mindestdurchsatz in Byte pro Sekunde.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann in den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gesetzt werden.
	 */
	@Deprecated
	public final void setMinimumThroughput(int throughput) {
		_communicationParameters.setMinimumThroughput(throughput);
	}

	/**
	 * Setzt den minimal erlaubten Verbindungsdurchsatz bei aktivierter Durchsatzpr�fung.
	 *
	 * @param minConnectionSpeed Mindestdurchsatz in Byte pro Sekunde.
	 *
	 * @deprecated Wird durch {@link #setMinimumThroughput} ersetzt.
	 */
	@Deprecated
	public final void setMinConnectionSpeed(int minConnectionSpeed) {
		_communicationParameters.setMinimumThroughput(minConnectionSpeed);
	}

	/**
	 * Bestimmt den Namen der Applikation.
	 *
	 * @return Name des zu erzeugenden Applikations-Objekts.
	 *
	 * @see #setApplicationName
	 */
	public final String getApplicationName() {
		return _applicationName;
	}

	/**
	 * Bestimmt den Namen der Applikation, der im Namen der lokalen Cache-Datei f�r Konfigurationsdaten verwendet werden soll. Der Applikationsname kann mit
	 * dem Aufrufparameter -lokaleSpeicherungKonfiguration=pfadname:applikationsname von au�en vorgegeben werden.
	 * Wenn das Aufrufargument -lokaleSpeicherungKonfiguration nicht benutzt wurde oder im angegebenen Argument kein mit Doppelpunkt getrennter Name angegeben
	 * wurde, dann gibt diese Methode den gleichen Wert zur�ck, wie die Methode {@link #getApplicationName()}.
	 *
	 * @return Name der Applikation, der im Namen der lokalen Cache-Datei f�r Konfigurationsdaten verwendet werden soll.
	 */
	public final String getApplicationNameForLocalConfigurationCache() {
		return (_applicationNameForLocalConfigurationCache != null) ? _applicationNameForLocalConfigurationCache : getApplicationName();
	}

	/**
	 * Setzt den Namen der Applikation. Nach erfolgreicher Authentifizierung der Applikation wird vom Datenverteiler ein Applikations-Objekt mit dem angegebenem
	 * Namen erzeugt. Wenn diese Methode nicht aufgerufen wird, dann wird als Default der Name "TestApplikation" benutzt.
	 *
	 * @param applicationName Name des zu erzeugenden Applikations-Objekts
	 *
	 * @see #setApplicationTypePid
	 */
	public final void setApplicationName(String applicationName) {
		if(applicationName != null) {
			_applicationName = applicationName;
		}
	}

	/**
	 * Bestimmt den Typ der Applikation.
	 *
	 * @return PID, die den Typ des zu erzeugenden Applikations-Objekts spezifiziert.
	 *
	 * @see #setApplicationTypePid
	 */
	public final String getApplicationTypePid() {
		return _applicationTypePid;
	}

	/**
	 * Setzt den Typ der Applikation. Nach erfolgreicher Authentifizierung der Applikation wird vom Datenverteiler ein Applikations-Objekt erzeugt. Der Typ dieses
	 * Objekts entspricht dem hier �bergebenen Typ. Wenn diese Methode nicht aufgerufen wird, dann wird als Default der Typ "typ.applikation" benutzt.
	 *
	 * @param applicationTypePid PID, die den Typ des zu erzeugenden Applikations-Objekts spezifiziert.
	 *
	 * @see #setApplicationName
	 */
	public final void setApplicationTypePid(String applicationTypePid) {
		if(applicationTypePid != null) {
			_applicationTypePid = applicationTypePid;
		}
	}

	/**
	 * Bestimmt das bei der Authentifizierung zu verwendende Verfahren.
	 *
	 * @return Klassenname des Authentifizierungs-Verfahrens.
	 */
	public final String getAuthentificationProcessName() {
		return _authentificationProcessName;
	}

	/**
	 * Setzt das bei der Authentifizierung zu verwendende Verfahren. Wird die Methode nicht aufgerufen, dann wird das Verfahren HMAC-MD5 benutzt.
	 *
	 * @param authentificationProcessName Klassenname des Verfahrens
	 */
	public final void setAuthentificationProcessName(String authentificationProcessName) {
		if(authentificationProcessName != null) {
			_authentificationProcessName = authentificationProcessName;
		}
	}

	/**
	 * Bestimmt das auf unterster Ebene einzusetzende Kommunikationsprotokoll f�r die Kommunikation mit dem Datenverteiler.
	 *
	 * @return Klassenname des Kommunikationsverfahrens.
	 */
	public final String getLowLevelCommunicationName() {
		return _lowLevelCommunicationName;
	}

	/**
	 * Setzt das auf unterster Ebene einzusetzende Kommunikationsprotokoll. Wird diese Methode nicht aufgerufen, dann wird das TCP-Protokoll benutzt.
	 *
	 * @param lowLevelCommunicationName Klassenname des Kommunikationsverfahrens.
	 */
	public final void setLowLevelCommunicationName(String lowLevelCommunicationName) {
		if(lowLevelCommunicationName != null) {
			_lowLevelCommunicationName = lowLevelCommunicationName;
		}
	}

	/**
	 * Bestimmt die Kommunikationsaddresse des Datenverteilers, die f�r den {@link ClientDavInterface#connect() Verbindungsaufbau} benutzt werden soll.
	 *
	 * @return Kommunikationsadresse des Datenverteilers.
	 *
	 * @see #setDavCommunicationAddress
	 */
	public final String getDavCommunicationAddress() {
		return _address;
	}

	/**
	 * Setzt die Kommunikationsaddresse des Datenverteilers, die f�r den {@link ClientDavInterface#connect() Verbindungsaufbau} benutzt werden soll. Wenn als
	 * Kommunikationsprotokoll TCP eingesetzt wird, dann kann hier der Rechnername oder die IP-Addresse des Rechners auf dem der Datenverteiler l�uft angegeben
	 * werden.
	 *
	 * @param address Kommunikationsadresse des Datenverteilers (IP-Addresse oder Rechnername bei TCP).
	 */
	public final void setDavCommunicationAddress(String address) {
		if(address != null) {
			_address = address;
		}
	}

	/**
	 * Bestimmt die Kommunikationssubaddresse des Datenverteilers, die f�r den {@link ClientDavInterface#connect() Verbindungsaufbau} benutzt werden soll.
	 *
	 * @return Kommunikationssubadresse des Datenverteilers.
	 *
	 * @see #setDavCommunicationSubAddress
	 */
	public final int getDavCommunicationSubAddress() {
		return _subAddress;
	}

	/**
	 * Setzt die Kommunikationssubaddresse des Datenverteilers, die f�r den {@link ClientDavInterface#connect() Verbindungsaufbau} benutzt werden soll. Wenn als
	 * Kommunikationsprotokoll TCP eingesetzt wird, dann kann hier die TCP-Portnummer, auf der der Datenverteiler Verbindungen entgegennimmt, angegeben werden.
	 *
	 * @param subAddress Kommunikationssubadresse des Datenverteilers.
	 */
	public final void setDavCommunicationSubAddress(int subAddress) {
		if(subAddress > 0) {
			_subAddress = subAddress;
		}
	}

	/**
	 * Bestimmt die PID der zu verwendenden Konfiguration.
	 *
	 * @return PID der zu verwendenden Konfiguration.
	 */
	public final String getConfigurationPid() {
		return _configurationPid;
	}

	/**
	 * Setzt die PID der zu verwendenden Konfiguration. Wenn die Methode nicht aufgerufen wird, dann wird die Standard-Konfiguration des Datenverteilers benutzt.
	 *
	 * @param configurationPid PID der zu verwendenden Konfiguration.
	 */
	public final void setConfigurationPid(String configurationPid) {
		if(configurationPid != null) {
			_configurationPid = configurationPid;
			if("null".equals(_configurationPid)) {
				_configurationPid = CommunicationConstant.LOCALE_CONFIGURATION_PID_ALIASE;
			}
		}
	}

	/**
	 * Bestimmt den Ort zum Zwischenspeichern der Konfiguration.
	 *
	 * @return Pfad im lokalen Dateisystem in dem die Konfigurationsdaten zwischengespeichert werden oder <code>null</code>, wenn die Konfigurationsdaten nicht
	 *         lokal zwischengespeichert werden.
	 */
	public final String getConfigurationPath() {
		return _configurationPath;
	}

	/**
	 * Setzt den Ort zum Zwischenspeichern der Konfiguration. Wenn kein Ort spezifiziert wurde, dann wird die Konfiguration nicht zwischengespeichert.
	 *
	 * @param configurationPath Pfad im lokalen Dateisystem in dem die Konfigurationsdaten zwischengespeichert werden sollen oder <code>null</code>, falls die
	 *                          Konfigurationsdaten nicht lokal zwischengespeichert werden sollen.
	 */
	public final void setConfigurationPath(String configurationPath) {
		_configurationPath = configurationPath;
	}

	/**
	 * Bestimmt den bei der {@link ClientDavInterface#login() Authentifizierung} zu verwendenden Benutzernamen.
	 *
	 * @return Name des Benutzers.
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * Setzt den bei der {@link ClientDavInterface#login() Authentifizierung} zu verwendenden Benutzernamen.
	 *
	 * @param userName Name des Benutzers.
	 */
	public final void setUserName(String userName) {
		if(userName != null) {
			_userName = userName;
		}
	}

	/**
	 * Bestimmt das bei der {@link ClientDavInterface#login() Authentifizierung} zu verwendende Passwort.
	 *
	 * @return Passwort des Benutzers.
	 */
	public final String getUserPassword() {
		return _userPassword;
	}

	/**
	 * Setzt das bei der {@link ClientDavInterface#login() Authentifizierung} zu verwendende Passwort.
	 *
	 * @param userPassword Passwort des Benutzers.
	 */
	public final void setUserPassword(String userPassword) {
		if(userPassword != null) {
			_userPassword = userPassword;
		}
	}

	/**
	 * Bestimmt das Timeout zum Senden von KeepAlive-Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @return Vorschlag f�r das Timeout zum Senden von KeepAlive-Telegrammen in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann aus den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gelesen werden.
	 */
	@Deprecated
	public final long getSendKeepAliveTimeout() {
		return _communicationParameters.getSendKeepAliveTimeout();
	}

	/**
	 * Setzt das Timeout zum Senden von KeepAlive-Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @param timeout Vorschlag f�r das Timeout zum Senden von KeepAlive-Telegrammen in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann in den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gesetzt werden.
	 */
	@Deprecated
	public final void setSendKeepAliveTimeout(long timeout) {
		if(timeout > 0) {
			_communicationParameters.setSendKeepAliveTimeout(timeout);
		}
	}

	/**
	 * Bestimmt das KeepAlive-Timeout beim Empfang von Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu
	 * verwendenden Wert festlegt.
	 *
	 * @return Vorschlag f�r das KeepAlive-Timeout beim Empfang von Telegrammen in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann aus den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gelesen werden.
	 */
	@Deprecated
	public final long getReceiveKeepAliveTimeout() {
		return _communicationParameters.getReceiveKeepAliveTimeout();
	}

	/**
	 * Setzt das KeepAlive-Timeout beim Empfang von Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @param timeout Vorschlag f�r das KeepAlive-Timeout beim Empfang von Telegrammen in Millisekunden.
	 *
	 * @see #getCommunicationParameters()
	 * @deprecated Wert kann in den {@link de.bsvrz.dav.daf.communication.lowLevel.CommunicationParameters} gesetzt werden.
	 */
	@Deprecated
	public final void setReceiveKeepAliveTimeout(long timeout) {
		if(timeout > 0) {
			_communicationParameters.setReceiveKeepAliveTimeout(timeout);
		}
	}

	/**
	 * Bestimmt die Standardm��ig zu verwendende Simulationsvariante.
	 *
	 * @return Zu verwendende Simulationsvariante
	 *
	 * @see #setSimulationVariant
	 */
	public final short getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * Setzt die Standardm��ig zu verwendende Simulationsvariante. Wenn die Methode nicht aufgerufen wird, wird die Variante <code>0</code> benutzt, wenn sie nicht
	 * explizit angegeben wurde.
	 *
	 * @param variant Zu verwendende Simulationsvariante.
	 *
	 * @see DataDescription
	 */
	public final void setSimulationVariant(short variant) {
		if(variant > 0) {
			_simulationVariant = variant;
		}
	}

	/**
	 * Bestimmt die Verz�gerungszeit zur �bertragung von gepufferten und zu versendenden Telegrammen.
	 *
	 * @return Sende-Verz�gerungszeit in Millisekunden.
	 *
	 * @see #setCommunicationSendFlushDelay
	 */
	public final long getCommunicationSendFlushDelay() {
		return _communicationSendFlushDelay;
	}

	/**
	 * Setzt die Verz�gerungszeit zur �bertragung von gepufferten und zu versendenden Telegrammen. Die �bertragung der gesammelten Daten im Sendepuffer findet erst
	 * statt, wenn die hier angegebene Zeit lang keine Daten mehr in der Puffer geschrieben wurden oder der Sendepuffer voll ist.
	 *
	 * @param delay Sende-Verz�gerungszeit in Millisekunden.
	 */
	public final void setCommunicationSendFlushDelay(long delay) {
		if(delay > 0) {
			_communicationSendFlushDelay = delay;
		}
	}

	/**
	 * Bestimmt die Gr��e des Sendepuffers, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 *
	 * @return Gr��e des Sendepuffers in Bytes.
	 */
	public final int getCommunicationOutputBufferSize() {
		return _outputBufferSize;
	}

	/**
	 * Setzt die Gr��e des Sendepuffers, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 *
	 * @param bufferSize Gr��e des Sendepuffers in Bytes.
	 */
	public final void setCommunicationOutputBufferSize(int bufferSize) {
		if(bufferSize > 0) {
			_outputBufferSize = bufferSize;
		}
	}

	/**
	 * Bestimmt die Gr��e des Empfangspuffers, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 *
	 * @return Gr��e des Empfangspuffers in Bytes.
	 */
	public final int getCommunicationInputBufferSize() {
		return _inputBufferSize;
	}

	/**
	 * Setzt die Gr��e des Empfangspuffers, der bei der Kommunikation mit dem Datenverteiler eingesetzt wird.
	 *
	 * @param bufferSize Gr��e des Empfangspuffers in Bytes.
	 */
	public final void setCommunicationInputBufferSize(int bufferSize) {
		if(bufferSize > 0) {
			_inputBufferSize = bufferSize;
		}
	}

	/**
	 * Bestimmt die maximale Gr��e von Datentelegrammen. Gr��ere Telegramme werden in mehrere Telegramme zerlegt.
	 *
	 * @return Maximale Gr��e von versendeten Datentelegrammen als Anzahl von Bytes.
	 */
	public final int getMaxDataTelegramSize() {
		return _maxTelegramSize;
	}

	/**
	 * Setzt die maximale Gr��e von Datentelegrammen. Gr��ere Telegramme werden in mehrere Telegramme zerlegt.
	 *
	 * @param maxTelegramSize Maximale Gr��e von versendeten Datentelegrammen als Anzahl von Bytes.
	 */
	public final void setMaxDataTelegramSize(int maxTelegramSize) {
		if(maxTelegramSize > 0) {
			_maxTelegramSize = maxTelegramSize;
		}
	}

	/**
	 * Gibt die Parameter f�r die Kommunikation zwischen Applikation und Datenverteiler zur�ck.
	 *
	 * @return die Parameter f�r die Kommunikation zwischen Applikation und Datenverteiler
	 */
	public CommunicationParameters getCommunicationParameters() {
		return _communicationParameters;
	}

	/**
	 * Setzt eine Aspektumleitung f�r eine Kombination von Attributgruppe und Aspekt. Ein von der Applikation angegebener Aspekt beim Anmelden, Lesen, Schreiben
	 * und Abmelden einer Attributgruppe, wird durch einen anderen Aspekt ersetzt. Damit besteht die M�glichkeit den Datenflu� einer Applikation zu modifizieren
	 * und damit beispielsweise einen anderen Prozess in eine Bearbeitungskette einzuf�gen.
	 *
	 * @param attributeGroupPid   PID der Attributgruppe f�r die eine Umleitung eingef�gt werden soll.
	 * @param aspectPid           PID des Aspekts f�r den eine Umleitung eingef�gt werden soll.
	 * @param substituteAspectPid PID des statt <code>aspectPid</code> zu verwendenden Aspekts.
	 */
	public final void addAspectRedirection(String attributeGroupPid, String aspectPid, String substituteAspectPid) {
		_debug.info("Aspektumleitung f�r " + attributeGroupPid + " und " + aspectPid + " --> " + substituteAspectPid);
		if((attributeGroupPid == null) || (aspectPid == null) || (substituteAspectPid == null)) {
			throw new IllegalArgumentException("Argument ist null");
		}
		AttributeGroupAspectObject attributeGroupAspect = new AttributeGroupAspectObject(attributeGroupPid, aspectPid);
		AttributeGroupAspectObject attributeGroupAspectSubstitute = new AttributeGroupAspectObject(
				attributeGroupPid, substituteAspectPid
		);
		_aspectToSubstituteTable.put(attributeGroupAspect, attributeGroupAspectSubstitute);
		_substituteToAspectTable.put(attributeGroupAspectSubstitute, attributeGroupAspect);
	}

	/**
	 * Gibt die Aspektumleitung f�r eine Kombination von Attributgruppe und Aspekt zur�ck. Wenn keine entsprechende Aspektumleitung besteht, wird der �bergebene
	 * Original-Aspekt zur�ckgegeben.
	 *
	 * @param attributeGroupPid PID der Attributgruppe.
	 * @param aspectPid         PID des Original-Aspekts.
	 *
	 * @return Pid des Aspekts, der anstelle des angegebenen Aspektes benutzt werden soll. Wenn dieser Aspekt nicht existiert, wird der angegebene Aspekt
	 *         zur�ckgegeben.
	 *
	 * @see #addAspectRedirection
	 */
	public final String aspectToSubstitute(String attributeGroupPid, String aspectPid) {
		if((attributeGroupPid == null) || (aspectPid == null)) {
			throw new IllegalArgumentException("Argument ist null");
		}
		AttributeGroupAspectObject attributeGroupAspect = new AttributeGroupAspectObject(attributeGroupPid, aspectPid);
		AttributeGroupAspectObject attributeGroupAspectSubstitute = (AttributeGroupAspectObject)_aspectToSubstituteTable.get(
				attributeGroupAspect
		);
		if(attributeGroupAspectSubstitute == null) {
			return aspectPid;
		}
		return attributeGroupAspectSubstitute.aspectPid;
	}

	/**
	 * Gibt den Original-Aspekt eines ersetzten Aspekts einer Attributgruppe zur�ck. Wenn keine entsprechende Aspektumleitung besteht, wird der �bergebene Aspekt
	 * unver�ndert zur�ckgegeben.
	 *
	 * @param attributeGroupPid PID der Attributgruppe.
	 * @param aspectPid         PID des ersetzten Aspekts.
	 *
	 * @return Pid des Original-Aspekts.
	 *
	 * @see #addAspectRedirection
	 */
	public final String substituteToAspect(String attributeGroupPid, String aspectPid) {
		if((attributeGroupPid == null) || (aspectPid == null)) {
			throw new IllegalArgumentException("Argument ist null");
		}
		AttributeGroupAspectObject attributeGroupAspectSubstitute = new AttributeGroupAspectObject(
				attributeGroupPid, aspectPid
		);
		AttributeGroupAspectObject attributeGroupAspect = (AttributeGroupAspectObject)_substituteToAspectTable.get(
				attributeGroupAspectSubstitute
		);
		if(attributeGroupAspect == null) {
			return aspectPid;
		}
		return attributeGroupAspect.aspectPid;
	}

	/**
	 * Liefert die Gr��e des Auslieferungspuffers, der zur Zwischenspeicherung von Datens�tzen genutzt wird, die an einen Empf�nger der Applikation versendet
	 * werden sollen.
	 *
	 * @return Gr��e des Auslieferungspuffers in Bytes.
	 */
	public int getDeliveryBufferSize() {
		return _deliveryBufferSize;
	}

	/**
	 * Setzt die Gr��e des Auslieferungspuffers, der zur Zwischenspeicherung von Datens�tzen genutzt wird, die an einen Empf�nger der Applikation versendet werden
	 * sollen.
	 *
	 * @param deliveryBufferSize Gr��e des Auslieferungspuffers in Bytes.
	 */
	public void setDeliveryBufferSize(final int deliveryBufferSize) {
		_deliveryBufferSize = deliveryBufferSize;
	}

	/**
	 * Liefert den via Aufrufparameter von Start/Stopp vorgegebenen Inkarnationsnamen.
	 * @return Inkarnationsname oder <code>""</code>, falls das entsprechende Aufrufargument nicht angegeben wurde.
	 */
	public String getIncarnationName() {
		return _incarnationName;
	}

	class AttributeGroupAspectObject {

		int hashCode;

		public String attributeGroupPid;

		public String aspectPid;

		AttributeGroupAspectObject(String _attributeGroupPid, String _aspectPid) {
			attributeGroupPid = _attributeGroupPid;
			aspectPid = _aspectPid;
			hashCode = 0;
		}

		public final boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(!(obj instanceof AttributeGroupAspectObject)) {
				return false;
			}
			AttributeGroupAspectObject _obj = (AttributeGroupAspectObject)obj;
			return (attributeGroupPid.equals(_obj.attributeGroupPid) && aspectPid.equals(_obj.aspectPid));
		}

		public final int hashCode() {
			if(hashCode == 0) {
				int result = 19;
				result = (41 * result) + attributeGroupPid.hashCode();
				result = (41 * result) + aspectPid.hashCode();
				hashCode = result;
			}
			return hashCode;
		}
	}

	/** Gibt die eingestellten Parameter auf die Standardausgabe aus. */
	public static void printArgumentsList() {
		System.out.println();
		System.out.println("----------Argumente der Applikationsdatenfunktion----------");
		System.out.println();
		System.out.println("-datenverteiler=Datenverteilersadresse(Zeichenkette):Datenverteilerssubadresse(Zahl)");
		System.out.println("-konfigurationsBereich=Konfigurationspid(Zeichenkette)");
		System.out.println("-benutzer=Benutzername(Zeichenkette)");
		System.out.println("-authentifizierung=Authentifizierungsdateiname(Zeichenkette)");
		System.out.println("-authentifizierungsVerfahren=Authentifizierungsverfahren(Zeichenkette)");
		System.out.println("-timeoutSendeKeepAlive=time(Zahl in Sekunden)");
		System.out.println("-timeoutEmpfangeKeepAlive=time(Zahl in Sekunden)");
		System.out.println("-aspekt=Attributesgruppepid(Zeichenkette):Aspektspid(Zeichenkette):Ersatzaspektpid(Zeichenkette)");
		System.out.println("-simVariante=Ersatzsimulationsvariante(Zahl)");
	}
}

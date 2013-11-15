/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.main.communication;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.puk.config.main.authentication.Authentication;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.util.async.AsyncRequestQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 * Diese Klasse übernimmt den gesamten Datenverkehr der Konfiguration. Dies beinhaltet den Empfang von Aufträgen an die Konfiguration bis hin zum versand der
 * Antworten, die von der Konfiguraiton verschickt werden sollen.
 * <p/>
 * Alle Anfragen an die Konfiguration werden an das Datenmodell weitergeleitet {@link de.bsvrz.dav.daf.main.config.DataModel} und falls nötig an die
 * erzeugten Antworten an die anfragende Applikation zurückgeschickt.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConfigurationCommunicator {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final DataModel _dataModel;

	private final Authentication _authentication;

	private final ConfigurationRequesterCommunicator _requesterCommunicator;

	private final ConfigurationQueryManager _configurationQueryManager;

	public ConfigurationCommunicator(
			AsyncRequestQueue asyncRequestQueue,
			DataModel dataModel,
			File userManagementFile,
			ClientDavParameters dafParameters,
			final File foreignObjectCacheFile)
			throws ParserConfigurationException, MissingParameterException, CommunicationError, InterruptedException, InconsistentLoginException, ConnectionException {
		_dataModel = dataModel;

		final ConfigurationAuthority configurationAuthority = dataModel.getConfigurationAuthority();
		if(configurationAuthority == null) {
			throw new IllegalStateException(
					"Der in der Verwaltungsdatei angegebene Konfigurationsverantwortliche wurde nicht gefunden."
			);
		}

		// Damit wird dem DaV mitgeteilt, dass sich die Konfiguration anmeldet
		dafParameters.setApplicationTypePid("typ.konfigurationsApplikation");
		dafParameters.setApplicationName("Konfiguration");
		// An die Pid wird mit Doppelpunkt die ID des Konfigurationsverantwortlichen angehangen. Diese Werte werden vom Datenverteiler als Default an alle
		// Applikationen weitergegeben
		dafParameters.setConfigurationPid(configurationAuthority.getPid() + ":" + configurationAuthority.getId());

		// Verbindung zum Datenverteiler herstellen. Es wird als Konfiguration angemeldet
		_debug.fine("Creating ClientDavConnection...");
		final ClientDavInterface connection = new ClientDavConnection(dafParameters, _dataModel);
		_debug.fine("connecting...");
		try {
			connection.connect();
		}
		catch(ConnectionException e) {
			Thread.sleep(2000);
			_debug.info("Erneuter Verbindungsversuch");
			connection.connect();
		}
		_debug.fine("login...");

		// Die login-Daten wurden per Aufrufparameter übergeben
		connection.login();

		if(configurationAuthority != connection.getLocalConfigurationAuthority()) {
			_debug.error("Datenverteiler liefert nicht den erwarteten Konfigurationsverantwortlichen zurück, sondern", connection.getLocalConfigurationAuthority());
		}

		// Verwaltet alle Benutzer der Konfigration
		_authentication = new ConfigAuthentication(userManagementFile, _dataModel);

		try {
			// übernimmt alle alten Konfigurationsanfragen. Dieses Objekt soll im Laufe der Zeit verschwinden
			_requesterCommunicator = new ConfigurationRequesterCommunicator(asyncRequestQueue, _dataModel, _authentication, connection);

			// übernimmt die neuen Konfigurationsanfragen, später soll dieses Objekt alle Konfigurationsanfragen übernehmen
			_configurationQueryManager = new ConfigurationQueryManager(connection, _dataModel, null, _authentication, foreignObjectCacheFile);
			_requesterCommunicator.setForeignObjectManager(_configurationQueryManager.getForeignObjectManager());
			_configurationQueryManager.start();
		}
		catch(RuntimeException e) {
			// Im Fehlerfall wird die Lockdatei für die Benutzerverwaltungen wieder freigegeben
			_authentication.close();
			throw e;
		}
	}

	/**
	 * Wird aufgerufen, wenn das System heruntergefahren werden soll.
	 */
	public void close()
	{
		_authentication.close();
	}

	/**
	 * Gibt die ConfigAuthentication-Klasse zurück
	 * @return die ConfigAuthentication-Klasse
	 */
	public ConfigAuthentication getAuthentication(){
		return (ConfigAuthentication)_authentication;
	}

	public ForeignObjectManager getForeignObjectManager() {
		return _configurationQueryManager.getForeignObjectManager();
	}
}

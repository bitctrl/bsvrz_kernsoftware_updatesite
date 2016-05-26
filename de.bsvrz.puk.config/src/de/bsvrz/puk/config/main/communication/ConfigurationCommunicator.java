/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main.communication;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.util.cron.CronDefinition;
import de.bsvrz.dav.daf.util.cron.CronScheduler;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.MaintenanceSpec;
import de.bsvrz.puk.config.configFile.datamodel.TimeBasedMaintenanceSpec;
import de.bsvrz.puk.config.configFile.datamodel.TypeHierarchy;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.main.authentication.Authentication;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.util.async.AsyncRequestQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Diese Klasse übernimmt den gesamten Datenverkehr der Konfiguration. Dies beinhaltet den Empfang von Aufträgen an die Konfiguration bis hin zum versand der
 * Antworten, die von der Konfiguraiton verschickt werden sollen.
 * <p>
 * Alle Anfragen an die Konfiguration werden an das Datenmodell weitergeleitet {@link de.bsvrz.dav.daf.main.config.DataModel} und falls nötig an die
 * erzeugten Antworten an die anfragende Applikation zurückgeschickt.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConfigurationCommunicator {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final ConfigDataModel _dataModel;

	private final Authentication _authentication;

	private final ConfigurationRequesterCommunicator _requesterCommunicator;

	private final ConfigurationQueryManager _configurationQueryManager;

	private final CronScheduler _cronScheduler = new CronScheduler(1);

	private ScheduledFuture<?> _restructureTask = null;
	private ScheduledFuture<?> _maintenanceTask = null;
	private final TypeHierarchy _typeHierarchy;

	public ConfigurationCommunicator(
			AsyncRequestQueue asyncRequestQueue,
			ConfigDataModel dataModel,
			File userManagementFile,
			ClientDavParameters dafParameters,
			final File foreignObjectCacheFile)
			throws ParserConfigurationException, MissingParameterException, CommunicationError, InterruptedException, InconsistentLoginException, ConnectionException {
		_dataModel = dataModel;
		_typeHierarchy = new TypeHierarchy(dataModel);

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

			_dataModel.setSimulationHandler(_configurationQueryManager);
			_requesterCommunicator.setSimulationHandler(_configurationQueryManager);

			startCronTasks(connection);
		}
		catch(RuntimeException e) {
			// Im Fehlerfall wird die Lockdatei für die Benutzerverwaltungen wieder freigegeben
			_authentication.close();
			throw e;
		}
	}

	private void startCronTasks(final ClientDavInterface connection) {
		ConfigurationAuthority kv = _dataModel.getConfigurationAuthority();
		AttributeGroup parameterAtg = _dataModel.getAttributeGroup("atg.parameterEndgültigesLöschen");
		if(parameterAtg == null){
			scheduleRestructure(new CronDefinition("0 2 * * Montag"));
		}
		else {
			DataDescription dataDescription = new DataDescription(parameterAtg, _dataModel.getAspect("asp.parameterSoll"));
			try {
				connection.subscribeReceiver(new ParamReceiver(), kv, dataDescription, ReceiveOptions.normal(), ReceiverRole.receiver());
			}
			catch(Exception e){
				_debug.warning("Kann Löschparameter nicht abrufen", e);
			}
		}
	}

	/** Plant einen periodischen Restrukturierungsauftrag */
	private void scheduleRestructure(final CronDefinition cronDefinition) {
		if(_restructureTask != null){
			_restructureTask.cancel(false);
			_restructureTask = null;
		}

		if(cronDefinition != null) {

			_debug.info("Geplante Restrukturierung: " + cronDefinition);

			_restructureTask = _cronScheduler.schedule(
					new Runnable() {
						@Override
						public void run() {
							_dataModel.restructure(ConfigurationAreaFile.RestructureMode.DynamicObjectRestructure);
						}
					}, cronDefinition
			);
		}
	}

	/** Plant einen periodischen Auftrag für das (Vormerken zum) Löschen von historischen dynamischen Objekten und Mengenreferenzen */
	private void scheduleMaintenance(final CronDefinition cronDefinition, final MaintenanceSpec spec) {
		if(_maintenanceTask != null){
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}

		if(cronDefinition != null && spec != null) {

			_debug.info("Geplantes Löschen: " + cronDefinition);

			_maintenanceTask = _cronScheduler.schedule(
					new Runnable() {
						@Override
						public void run() {
							_dataModel.doMaintenance(spec);
						}
					}, cronDefinition
			);
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

	private class ParamReceiver implements ClientReceiverInterface {

		@Override
		public void update(final ResultData[] results) {
			TimeBasedMaintenanceSpec spec = null;
			CronDefinition restructureTime = null;
			CronDefinition deleteTime = null;
			for(ResultData result : results) {
				if(result.hasData()) {
					Data data = result.getData();
					try{
						restructureTime = new CronDefinition(data.getTextValue("IntervallRestrukturierung").getValueText());
					}
					catch(IllegalArgumentException e){
						_debug.warning("Ungültiger Parameter IntervallRestrukturierung", e);
					}
					try{
						deleteTime = new CronDefinition(data.getTextValue("IntervallLöschen").getValueText());
					}
					catch(IllegalArgumentException e){
						_debug.warning("Ungültiger Parameter IntervallLöschen", e);
					}
					final Map<DynamicObjectType, Long> objectKeepTimes = new HashMap<DynamicObjectType, Long>();
					final Map<ObjectSetType, Long> setKeepTimes = new HashMap<ObjectSetType, Long>();
					Long defaultSetKeepTime = null;
					Data dynObj = data.getItem("DynamischeObjekte");
					for(Data objData : dynObj) {
						SystemObject type = objData.getReferenceValue("Objekttyp").getSystemObject();
						long time = objData.getTimeValue("Vorhaltezeitraum").getMillis();
						if(type instanceof DynamicObjectType && time >= 0) {
							DynamicObjectType dynamicObjectType = (DynamicObjectType) type;
							objectKeepTimes.put(dynamicObjectType, time);
						}
					}
					Data sets = data.getItem("DynamischeMengen");
					for(Data setData : sets) {
						SystemObject type = setData.getReferenceValue("Mengentyp").getSystemObject();
						long time = setData.getTimeValue("Vorhaltezeitraum").getMillis();
						if(type instanceof ObjectSetType && time >= 0) {
							ObjectSetType objectSetType = (ObjectSetType) type;
							if(objectSetType.isMutable()) {
								setKeepTimes.put(objectSetType, time);
							}
						}
					}
					Data setDefault = data.getItem("DynamischeMengenStandard");
					for(Data setData : setDefault) {
						defaultSetKeepTime = setData.asTimeValue().getMillis();
					}
					spec = new TimeBasedMaintenanceSpec(
							_typeHierarchy,
					        objectKeepTimes,
					        setKeepTimes,
					        defaultSetKeepTime
					);
				}
				scheduleRestructure(restructureTime);
				scheduleMaintenance(deleteTime, spec);
			}
		}
	}
}

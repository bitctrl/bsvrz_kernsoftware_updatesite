/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;
import de.bsvrz.dav.daf.main.impl.config.telegrams.*;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.*;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * TBD Beschreibung
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigurationManager {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Das Datenmodel */
	private final DataModel _dataModel;

	/** Der Anmeldemanager */
	private SubscriptionManager _subscriptionManager;

	/** Die Pid der Konfiguration */
	private String _configurationPid;

	/** Der Pfad der Konfiguration */
	private String _configurationPath;

	/** Der Name der Applikation */
	private String _applicationName;

	/** Der Index der Konfigurationsendung */
	private int _configSendIndex;

	/** Interne Liste der ankommenden Konfigurationsnachrichten */
	private LinkedList _pendingResponces;

	/** Die ID des Konfigurationsverantwortlichen der Konfiguration. */
	private final long _configurationId;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configurationId   ID des Konfigurationsverantwortlichen
	 * @param configurationPid  Pid der Konfiguration
	 * @param configurationPath Pfad der Konfiguration
	 * @param applicationName   Name der Applikation
	 * @param dataModel         Datenmodel
	 */
	public ConfigurationManager(long configurationId, String configurationPid, String configurationPath, String applicationName, DataModel dataModel) {
		_configurationId = configurationId;
		_configurationPid = configurationPid;
		_configurationPath = configurationPath;
		_applicationName = applicationName;
		_dataModel = dataModel;
		_pendingResponces = new LinkedList();
		_configSendIndex = 0;
	}

	/**
	 * Erzeugt ein Datenmodel und beendet die initialisierungsphase.
	 *
	 * @param subscriptionManager Anmeldemanager
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException wenn Fehler bei Konfigurationsänderungen auftreten.
	 */
	public final void completeInitialisation(SubscriptionManager subscriptionManager) throws ConfigurationException {
		_subscriptionManager = subscriptionManager;
		if(_dataModel instanceof DafDataModel) {
			((DafDataModel)_dataModel).init(this, _configurationId);
		}
		_subscriptionManager.setConfigurationManager(this);
	}

	/**
	 * Gibt die Pid der Konfiguration zurück.
	 *
	 * @return Pid der Konfiguration
	 */
	public final String getConfigurationPid() {
		return _configurationPid;
	}

	/**
	 * Gibt die ID der Konfiguration zurück
	 * @return Id der Konfiguration
	 */
	public long getConfigurationId() {
		return _configurationId;
	}

	/**
	 * Gibt den Pfad der Konfiguration zurück.
	 *
	 * @return Pfad der Konfiguration
	 */
	public final String getConfigurationPath() {
		return _configurationPath;
	}

	/**
	 * Gibt den Name der Applikation zurück.
	 *
	 * @return Name der Applikation
	 */
	public final String getApplicationName() {
		return _applicationName;
	}

	/**
	 * Gibt das Datenmodel zurück.
	 *
	 * @return Datenmodel
	 */
	public final DataModel getDataModel() {
		return _dataModel;
	}

	/**
	 * Diese Methode wird aufgerufen wenn eine neues Konfigurationstelegram erhalten wird.
	 *
	 * @param newData Die neue Konfigurationsdaten.
	 */
	public void update(SendDataObject newData) {
		if(_dataModel instanceof DafDataModel) {
			final DafDataModel dataModel = (DafDataModel)this._dataModel;
			byte b[] = newData.getData();
			if(b == null) {
				return;
			}
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
			try {
				long configurationId = in.readLong();
				if(_configurationId == configurationId) {
//				if (_dataModel.getConfigurationAuthorityId() == configurationId) {
					String info = in.readUTF();
					byte telegramType = in.readByte();
					int length = in.readInt();
					ConfigTelegram telegram = ConfigTelegram.getTelegram(telegramType, dataModel);
					telegram.read(in);
					telegram.setInfo(info);
					switch(telegramType) {
						case(ConfigTelegram.TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE):
						case(ConfigTelegram.AUTHENTIFICATION_ANSWER_TYPE): {
							synchronized(_pendingResponces) {
								_pendingResponces.add(telegram);
								_pendingResponces.notifyAll();
							}
						}
						default: {
							dataModel.update(telegram);
						}
					}
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Sendet eine Konfigurationsanfrage zur Kommunikationsschicht
	 *
	 * @param baseSubscriptionInfo Basisanmeldeinformationen
	 * @param telegram             Kofigurationstelegramm
	 */
	public final void sendConfigData(BaseSubscriptionInfo baseSubscriptionInfo, ConfigTelegram telegram) {
		if((_subscriptionManager == null) || (telegram == null)) {
			return;
		}

		++_configSendIndex;
		if(_configSendIndex > 0x3FFFFFFF) {
			_configSendIndex = 1;
		}
		long dataIndex = ((long)(_configSendIndex << 2) & 0x00000000FFFFFFFCL);
		long index = ((CommunicationConstant.START_TIME | dataIndex) & 0xFFFFFFFFFFFFFFFCL);

		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		try {
			DataOutputStream out = new DataOutputStream(byteBuffer);
			telegram.write(out);
			out.flush();
			byte telegramData[] = byteBuffer.toByteArray();
			byteBuffer.reset();

			out.writeLong(_subscriptionManager.getHighLevelCommunication().getApplicationId());
			out.writeUTF(telegram.getInfo());
			out.writeByte(telegram.getType());
			out.writeInt(telegramData.length);
			for(int i = 0; i < telegramData.length; ++i) {
				out.write(telegramData[i]);
			}
			out.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}

		byte data[] = byteBuffer.toByteArray();
		SendDataObject sendData = new SendDataObject(
				baseSubscriptionInfo, false, index, System.currentTimeMillis(), (byte)0, null, data
		);
		try {
			_subscriptionManager.sendData(sendData);
		}
		catch(DataNotSubscribedException ex) {
			_debug.error("Konfigurationsanfrage konnte nicht versendet werden", ex);
			throw new RuntimeException("Konfigurationsanfrage konnte nicht versendet werden", ex);
		}
	}

	/**
	 * Überprüfft ob die Authentificationsdaten existent und gültig sind. Wenn der Benutzer gültig ist und sein Passwort mit den gegebenen verschlüsselten Passwort
	 * übereinstimmt, dann schickt die Konfiguration die Id des Benutzers zurück, sonst -1
	 *
	 * @param userName          der Benutzername
	 * @param encriptedPassword verschlüsselte Passwort
	 * @param text              der Zufallstext mit den der Passwort verschlüsselt wurde
	 * @param processName       der Authentifikationsvervahren
	 *
	 * @throws ConfigurationException Wenn von der Konfiguration keine Antwort innerhalb eine bestimmten Zeit angekommen ist.
	 */
	public final long isValidUser(String userName, byte encriptedPassword[], String text, String processName) throws ConfigurationException {
		BaseSubscriptionInfo readBaseSubscriptionInfo = new BaseSubscriptionInfo(
				_configurationId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
		);

		ConfigTelegram telegram = new AuthentificationRequest(userName, encriptedPassword, text, processName);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		sendConfigData(readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.AUTHENTIFICATION_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							try {
								AuthentificationAnswer authentificationAnswer = (AuthentificationAnswer)responce;
								return authentificationAnswer.getUserId();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new ConfigurationException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Führt eine Konfigurationsanfrage durch um die versorgte Datenverteilertopologie zu ermitteln.
	 *
	 * @param transmitterId Die Id des Datenverteilers, dessen Verbindungsinformationen bestimmt werden müssen.
	 *
	 * @return Array mit Verbindungsinformationen
	 */
	public final TransmitterConnectionInfo[] getTransmitterConnectionInfo(long transmitterId) {
		BaseSubscriptionInfo readBaseSubscriptionInfo = new BaseSubscriptionInfo(
				_configurationId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
		);

		TransmitterConnectionInfoRequest telegram = new TransmitterConnectionInfoRequest(2, transmitterId);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_debug.finer("Sende Anfrage nach Topologie: " + telegram.parseToString());
		sendConfigData(readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE)
						   && (info.equals(responce.getInfo()))) {
							try {
								TransmitterConnectionInfoAnswer transmitterConnectionInfoAnswer = (TransmitterConnectionInfoAnswer)responce;
								if(transmitterConnectionInfoAnswer.getTransmitterId() == telegram.getTransmitterId()) {
									_iterator.remove();
									_debug.finer("Empfangene Antwort mit Topologie: " + transmitterConnectionInfoAnswer.parseToString());
									return transmitterConnectionInfoAnswer.getTransmitterConnectionInfos();
								}
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}
}

/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.BackupProgressCallback;
import de.bsvrz.dav.daf.main.config.BackupResult;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SimpleBackupResult;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.main.config.UpdateDynamicObjects;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResult;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntry;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntryType;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.dav.daf.main.impl.config.DafDynamicObjectType;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.ConfigurationRequestArea;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.ConfigurationRequestReadData;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.ConfigurationRequestUserAdministration;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.ConfigurationRequestWriteData;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.SenderReceiverCommunication;
import de.bsvrz.sys.funclib.crypt.EncryptDecryptProcedure;
import de.bsvrz.sys.funclib.crypt.encrypt.EncryptFactory;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8301 $
 */
public class RemoteRequestManager implements DavConnectionListener {

	private static final Debug _debug = Debug.getLogger();

	private static final Map<ClientDavInterface, RemoteRequestManager> _connection2ManagerMap = new HashMap<ClientDavInterface, RemoteRequestManager>();

	private final Map<ConfigurationAuthority, ConfigurationRequester> _configAuthority2RequesterMap = new HashMap<ConfigurationAuthority, ConfigurationRequester>();

	private final ClientDavInterface _connection;

	private final DataModel _localConfiguration;

	private SystemObject _localApplication;

	public static RemoteRequestManager getInstance(
			ClientDavInterface connection, DataModel localConfiguration, SystemObject localApplication
	) {
		if(localApplication == null) throw new IllegalArgumentException("Das Applikationsobjekt ist der lokalen Konfiguration unbekannt.");

		synchronized(_connection2ManagerMap) {
			RemoteRequestManager manager = (RemoteRequestManager)_connection2ManagerMap.get(connection);
			if(manager == null) {
				manager = new RemoteRequestManager(connection, localConfiguration, localApplication);
				_connection2ManagerMap.put(connection, manager);
			}
			return manager;
		}
	}

	/**
	 * Erzeugt eine Instanz des RemoteRequestMangagers
	 *
	 * @param connection         Verbindung über die sich angemeldet wird
	 * @param localConfiguration Datenmodell
	 * @param localApplicationId Id des Objekts das zur Anmeldung für Sender/Empfänger benutzt wird
	 *
	 * @return Instanz des RemoteRequestManager´s
	 */
	public static RemoteRequestManager getInstance(
			ClientDavInterface connection, DataModel localConfiguration, long localApplicationId
	) {
		final SystemObject systemObject = localConfiguration.getObject(localApplicationId);
		return RemoteRequestManager.getInstance(connection, localConfiguration, systemObject);
	}

	public void connectionClosed(ClientDavInterface connection) {
		_debug.fine("DatenverteilerVerbindung wurde terminiert");
		connection.removeConnectionListener(this);
		Collection<ConfigurationRequester> requesters = new ArrayList<ConfigurationRequester>(_configAuthority2RequesterMap.values());
		_configAuthority2RequesterMap.clear();
		for(ConfigurationRequester requester : requesters) {
			if(requester instanceof RemoteRequester) {
				RemoteRequester remoteRequester = (RemoteRequester)requester;
				remoteRequester.close();
			}
		}
		_connection2ManagerMap.remove(connection);
	}

	private RemoteRequestManager(
			ClientDavInterface connection, DataModel localConfiguration, SystemObject localApplication
	) {
		try {
			_connection = connection;
			_localConfiguration = localConfiguration;
			_localApplication = localApplication;
			connection.addConnectionListener(this);
		}
		catch(Exception e) {
			e.printStackTrace();
			_debug.warning("Initialisierung des RemoteRequestManager fehlgeschlagen", e);
			throw new RuntimeException(e);
		}
	}

	public ConfigurationRequester getRequester(ConfigurationAuthority remoteConfigurationAuthority) {
		synchronized(_configAuthority2RequesterMap) {
			ConfigurationRequester requester = (ConfigurationRequester)_configAuthority2RequesterMap.get(
					remoteConfigurationAuthority
			);
			if(requester == null) {
				requester = new RemoteRequester(
						_connection, _localConfiguration, remoteConfigurationAuthority, _localApplication
				);
				_configAuthority2RequesterMap.put(remoteConfigurationAuthority, requester);
			}
			return requester;
		}
	}

	/** Klasse, die Anfragen an eine entfernte Konfiguration implementiert. */
	private static class RemoteRequester implements ConfigurationRequester {

		private final DataModel _localConfiguration;

		private EncryptDecryptProcedure _encryptDecryptProcedure = EncryptDecryptProcedure.PBEWithMD5AndDES;

		/**
		 * Objekt, das Konfigurationsanfragen stellt und die Antwort der Konfigurations verarbeitet und zur Verfügung stellt. Es werden nur Konfigurationsanfragen
		 * gestellt, die lesenden Zugriff auf die Konfigurations erlauben.
		 */
		SenderReceiverCommunication _senderReadConfigObjects;

		/**
		 * Objekt, das Konfigurationsanfragen erstellt und die Antwort der Konfiguration zur Verfügung stellt. Es werden Konfigurationsanfragen gestellt, die die
		 * Konfigurations veranlassen Objekt in der Konfiguration zu ändern.
		 */
		SenderReceiverCommunication _senderWriteConfigObjects;

		/** Objekt, das es ermöglicht die Benutzer einer Konfigurations zu verwalten (Benutzer erstellen, Passwörter ändern, usw.). */
		SenderReceiverCommunication _senderUserAdministration;

		/** Beauftragt die Konfiguration bestimmte Bereiche zu modifizieren/prüfen */
		SenderReceiverCommunication _senderConfigAreaTask;

		/** Verbidung zum Datenverteiler. Wird benötigt um die Verbindung zum Datenverteiler abzumelden, falls es bei Anfragen zu schweren Fehlern gekommen ist. */
		private final ClientDavInterface _connection;

		private int _systemModelVersion;

		public RemoteRequester(
				ClientDavInterface connection, DataModel localConfiguration, ConfigurationAuthority configurationAuthority, SystemObject localApplication
		) {
			_connection = connection;
			_localConfiguration = localConfiguration;

			try {

				// Die Kanäle nehmen automatisch die richtige Simulationsvariante
				_senderReadConfigObjects = new ConfigurationRequestReadData(connection, configurationAuthority, localApplication, _localConfiguration);

				if(_localConfiguration instanceof UpdateDynamicObjects) {
					// Aktiviert den Mechanismus mit dem Objekte auf dem aktuellen Stand gehalten werden, wenn die Konfiguration aktuelle
					// Daten für Objekte verschickt (Namen, nicht mehr gültig ab, usw.)
					((ConfigurationRequestReadData)_senderReadConfigObjects).setDynamicObjectUpdater((UpdateDynamicObjects)_localConfiguration);
				}

				_senderWriteConfigObjects = new ConfigurationRequestWriteData(connection, configurationAuthority, localApplication);

				_senderUserAdministration = new ConfigurationRequestUserAdministration(connection, configurationAuthority, localApplication);
				_senderConfigAreaTask = new ConfigurationRequestArea(connection, configurationAuthority, localApplication);
			}
			catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
				oneSubscriptionPerSendData.printStackTrace();
				_debug.error("Anmeldung für Konfigurationsanfragen fehlgeschlagen", oneSubscriptionPerSendData);
				throw new RuntimeException(oneSubscriptionPerSendData);
			}
		}

		/** Teilt einem Request mit, dass er sich beenden soll */
		public void close() {
			_senderConfigAreaTask.close();
			_senderReadConfigObjects.close();
			_senderUserAdministration.close();
			_senderWriteConfigObjects.close();
		}

		private SystemObject getReplyObject(Data reply) throws RequestException {
			SystemObject result;
			Deserializer deserializer = getMessageDeserializer(reply, "ObjektAntwort");
			try {
				switch(deserializer.readByte()) {
					case 0:
						result = null;
						break;
					case 1:
						long id = deserializer.readLong();
						long typeId = deserializer.readLong();
						final byte flag = deserializer.readByte();
						final boolean valid = (flag & 1) != 0;
						String pid = null;
						if((flag & 2) != 0) pid = deserializer.readString();
						String name = null;
						if((flag & 4) != 0) name = deserializer.readString();
						final long validSince = deserializer.readLong();
						final long notValidSince = deserializer.readLong();
						final long configAreaId = deserializer.readLong();
						result = new RemoteDynamicObject(
								_localConfiguration, id, typeId, pid, name, valid, validSince, notValidSince, configAreaId
						);
						break;
					case 2:
						id = deserializer.readLong();
						typeId = deserializer.readLong();
						throw new RequestException("Konfigurationsobjekt wird ignoriert, id: " + id + ", typeId: " + typeId);
					default:
						throw new RequestException("fehlerhafte ObjektAntwort empfangen");
				}
			}
			catch(IOException e) {
				throw new RequestException("fehlerhafte ObjektAntwort empfangen", e);
			}
			return result;
		}

		/**
		 * Prüft ein Data ob es den richtigen Nachrichtentyp enthält. Ist das Data vom richtigen Typ, wird das Byte-Array des Data´s genommen und einem
		 * Deserialisierer übergeben.
		 *
		 * @param reply               Antwort der Konfiguration auf einen Konfigurationsanfrage
		 * @param expectedMessageType Typ des Telegramms, den die Konfiguration verschickt, wenn der Auftrag ohne Probleme bearbeitet werden konnte
		 *
		 * @return Objekt, über das Daten ausgelesen werden können
		 *
		 * @throws RequestException Technischer Fehler auf Seiten der Konfiguration oder auf Seiten des Clients bei der Übertragung des Auftrags. Dieser Fehler ist
		 *                          nicht zu beheben.
		 */
		Deserializer getMessageDeserializer(Data reply, String expectedMessageType) throws RequestException {
			String messageType = reply.getTextValue("nachrichtenTyp").getValueText();
			final byte[] message = reply.getScaledArray("daten").getByteArray();
			final Deserializer deserializer;
			try {
				deserializer = SerializingFactory.createDeserializer(2, new ByteArrayInputStream(message));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			if(messageType.equals(expectedMessageType)) {
				return deserializer;
			}
			else if(messageType.equals("FehlerAntwort")) {
				try {
					String errorMessage = deserializer.readString();
					throw new RequestException(errorMessage);
				}
				catch(IOException e) {
					throw new RequestException("fehlerhafte FehlerAntwort empfangen");
				}
			}
			else {
				throw new RequestException("falsche Antwort empfangen: " + messageType);
			}
		}

		/**
		 * Prüft ein Data ob es den richtigen Nachrichtentyp enthält. Ist das Data vom richtigen Typ, wird das Byte-Array des Data´s genommen und einem
		 * Deserialisierer übergeben.
		 *
		 * @param reply               Antwort der Konfiguration auf einen Konfigurationsanfrage
		 * @param expectedMessageType Typ des Telegramms, den die Konfiguration verschickt, wenn der Auftrag ohne Probleme bearbeitet werden konnte
		 *
		 * @return Objekt, über das Daten ausgelesen werden können
		 *
		 * @throws RequestException             Technischer Fehler auf Seiten der Konfiguration oder auf Seiten des Clients bei der Übertragung des Auftrags. Dieser
		 *                                      Fehler ist nicht zu beheben.
		 * @throws ConfigurationChangeException Der Auftrag wurde von der Konfiguration empfangen, allerdings weigert sich die Konfiguration die Änderung auszuführen.
		 *                                      Dies kann unterschiedliche Gründe haben (mangelnde Rechte, Randbediengungen nicht erfüllt, usw.), aber in allen Fällen
		 *                                      können weitere Anfragen gestellt werden.
		 * @throws ConfigurationTaskException   Der Auftrag wurde von der Konfiguration empfangen, allerdings konnte die Konfiguration den Auftrag nicht ausführen,
		 *                                      weil bestimmte aufgabenspezifische Randbediengungen nicht erfüllt wurde.
		 */
		Deserializer getMessageDeserializer2(Data reply, String expectedMessageType)
				throws RequestException, ConfigurationTaskException, ConfigurationChangeException {
			String messageType = reply.getTextValue("nachrichtenTyp").getValueText();
			final byte[] message = reply.getScaledArray("daten").getByteArray();
			final Deserializer deserializer;
			try {
				deserializer = SerializingFactory.createDeserializer(2, new ByteArrayInputStream(message));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			if(messageType.equals(expectedMessageType)) {
				return deserializer;
			}
			else if(messageType.equals("FehlerAntwort")) {
				try {
					String errorMessage = deserializer.readString();
					throw new RequestException(errorMessage);
				}
				catch(IOException e) {
					throw new RequestException("fehlerhafte FehlerAntwort empfangen");
				}
			}
			else if("KonfigurationsänderungVerweigert".equals(messageType)) {
				// Die Konfiguration verweigert nur den Auftrag, weil diverse Randbediengungen nicht erfüllt sind.
				try {
					final String reason = deserializer.readString();
					throw new ConfigurationChangeException(reason);
				}
				catch(IOException e) {
					// Die Antwort konnte nicht entschlüsselt werden
					throw new RequestException(
							"Die Konfiguration verweigert die Ausführung einer Konfigurationsänderung, aber der Grund konnte nicht entschlüsselt werden: " + e
					);
				}
			}
			else if("KonfigurationsauftragVerweigert".equals(messageType)) {
				// Die Konfiguration verweigert nur den Auftrag, weil diverse Randbediengungen nicht erfüllt sind.
				try {
					final String reason = deserializer.readString();
					throw new ConfigurationTaskException(reason);
				}
				catch(IOException e) {
					// Die Antwort konnte nicht entschlüsselt werden
					throw new RequestException(
							"Die Konfiguration verweigert die Ausführung eines Auftrages, aber der Grund konnte nicht entschlüsselt werden: " + e
					);
				}
			}
			else {
				throw new RequestException("falsche Antwort empfangen: " + messageType);
			}
		}


		public SystemObject getObject(String pid) throws RequestException {
			final int requestIndex;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(pid.length() + 2);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeString(pid);
				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("ObjektAnfrageMitPid", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			SystemObject result;
			result = getReplyObject(reply);
			return result;
		}

		public SystemObject getObject(long id) throws RequestException {
			int requestIndex;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(8);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeLong(id);
				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("ObjektAnfrageMitId", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			SystemObject result;
			result = getReplyObject(reply);
			return result;
		}

		public SystemObject[] getElements(
				MutableSet set, long startTime, long endTime, boolean validDuringEntirePeriod
		) throws RequestException {
			int requestIndex;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(25);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(set);	 // 8 Byte
				serializer.writeLong(startTime);		// 8 Byte
				serializer.writeLong(endTime);			// 8 Byte
				serializer.writeBoolean(validDuringEntirePeriod);	// 1 Byte
				requestIndex = _senderReadConfigObjects.sendData("DynamischeMengeAlleElementeAnfrage", byteArrayStream.toByteArray());
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			// Antwort-Datensatz erhalten - wird jetzt ausgelesen
			Deserializer deserializer = getMessageDeserializer(reply, "DynamischeMengeAlleElementeAntwort");
			SystemObject[] result = null;
			try {
				int size = deserializer.readInt();	// Anzahl der Elemente, die ausgelesen werden müssen
				result = new SystemObject[size];
				for(int i = 1; i <= size; i++) {
					result[i - 1] = deserializer.readObjectReference(_localConfiguration);
				}
			}
			catch(IOException ex) {
				throw new RequestException("Fehlerhafte Antwort - DynamischeMengeAlleElementeAntwort - erhalten", ex);
			}
			return result;
		}

		public void changeElements(
				ObjectSet set, SystemObject[] addElements, SystemObject[] removeElements
		) throws RequestException, ConfigurationException, ConfigurationChangeException {
			int requestIndex;

			// Der erwartete Antworttyp. Bei der Änderung einer dynamischen Menge wird eine andere antwort verschickt als bei der Änderung einer
			// konfigurierenden Menge.
			final String replyType;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(set);	// 8 Byte
				if(addElements == null) {
					serializer.writeInt(0);
				}
				else {
					serializer.writeInt(addElements.length);
					for(int i = 0; i < addElements.length; i++) {
						SystemObject element = addElements[i];
						serializer.writeObjectReference(element);
					}
				}
				if(removeElements == null) {
					serializer.writeInt(0);
				}
				else {
					serializer.writeInt(removeElements.length);
					for(int i = 0; i < removeElements.length; i++) {
						SystemObject element = removeElements[i];
						serializer.writeObjectReference(element);
					}
				}

				// Die Informationen für dynamische und konfigurierende Mengen sind gleich, der Pakettyp entscheidet
				// darüber wie das Paket auf der Konfigurationsseite behandelt wird.

				if(set instanceof MutableSet) {
					requestIndex = _senderWriteConfigObjects.sendData("DynamischeMengeElementeÄndern", byteArrayStream.toByteArray());
					replyType = "DynamischeMengeElementeAntwort";
				}
				else {
					requestIndex = _senderWriteConfigObjects.sendData("KonfigurierendeMengeElementeÄndern", byteArrayStream.toByteArray());
					replyType = "KonfigurierendeMengeElementeAntwort";
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);
			// Antwort erhalten - wird jetzt ausgelesen
			Deserializer deserializer = null;
			try {
				deserializer = getMessageDeserializer2(reply, replyType);
			}
			catch(ConfigurationTaskException e) {
				// Die TaskException wird in eine Change Exception umgewandelt, weil die Konfiguration sich weigert die Konfigurationsdaten zu ändern
				throw new ConfigurationChangeException(e);
			}
			try {
				boolean successful = deserializer.readBoolean();
				if(!successful) {
					String errorMessage = deserializer.readString();
					throw new ConfigurationException(errorMessage);
				}
			}
			catch(IOException ex) {
				throw new RequestException("Fehlerhafte Antwort - DynamischeMengeElementeAntwort - erhalten", ex);
			}
		}

		public void subscribe(MutableSet set, long time) throws RequestException {
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(16);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(set);
				serializer.writeLong(time);

				// Das Senderobjekt kümmert sich um die Antworten auf die Anmeldung und benachrichtigt die Objekte
				_senderReadConfigObjects.sendData("DynamischeMengeBeobachterAnmelden", byteArrayStream.toByteArray(), 0);

			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			// Da keine Antwort erwartet wird, muss auch nicht auf eine Antwort gewartet werden
		}

		public void unsubscribe(MutableSet set) throws RequestException {
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(8);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(set);

				_senderReadConfigObjects.sendData("DynamischeMengeBeobachterAbmelden", byteArrayStream.toByteArray(), 0);

			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			// Da keine Antwort erwartet wird, muss auch nicht auf eine Antwort gewartet werden
		}

		public Collection<SystemObject> subscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException {
			final int requestIndex;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference((SystemObject)mutableCollection);
				serializer.writeShort(simVariant);

				// Das Senderobjekt kümmert sich um die Antworten auf die Anmeldung und benachrichtigt die Objekte
				requestIndex = _senderReadConfigObjects.sendData("DynamischeKollektionAnmeldung", byteArrayStream.toByteArray());
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}

			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			Deserializer deserializer = null;
			deserializer = getMessageDeserializer(reply, "DynamischeKollektionElemente");
			try {
				final SystemObject receivedSystemObject = deserializer.readObjectReference(_localConfiguration);
				final short receivedSimVariant = deserializer.readShort();
				final int elementCount = deserializer.readInt();
				ArrayList<SystemObject> elements = new ArrayList<SystemObject>(elementCount);
				for(int i = 0; i < elementCount; i++) {
					final SystemObject element = deserializer.readObjectReference(_localConfiguration);
					elements.add(element);
				}
				return elements;
			}
			catch(IOException ex) {
				throw new RequestException("Fehlerhafte Antwort - DynamischeKollektionElemente - erhalten", ex);
			}
		}

		public void unsubscribeMutableCollectionChanges(MutableCollection mutableCollection, short simVariant) throws RequestException {
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference((SystemObject)mutableCollection);
				serializer.writeShort(simVariant);

				// Das Senderobjekt kümmert sich um die Antworten auf die Anmeldung und benachrichtigt die Objekte
				_senderReadConfigObjects.sendData("DynamischeKollektionAbmeldung", byteArrayStream.toByteArray(), 0);
			}
			catch(SendSubscriptionNotConfirmed e) {
				_debug.fine("Exception beim Abmelden in RemoteRequester.unsubscribeMutableCollectionChanges wird ignoriert e", e);
			}
			catch(DataNotSubscribedException e) {
				_debug.fine("Exception beim Abmelden in RemoteRequester.unsubscribeMutableCollectionChanges wird ignoriert e", e);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			// Da keine Antwort erwartet wird, muss auch nicht auf eine Antwort gewartet werden
		}

		public int subscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException {
			final int requestIndex;
			ensureSystemModellVersion(19, "Anmeldung auf Änderungen des Kommunikationsstatus kann nicht durchgeführt werden");
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(object);
				requestIndex = _senderReadConfigObjects.sendData("KommunikationszustandAnmeldung", byteArrayStream.toByteArray());
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}

			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			Deserializer deserializer = null;
			deserializer = getMessageDeserializer(reply, "KommunikationszustandRückmeldung");
			try {
				final SystemObject receivedSystemObject = deserializer.readObjectReference(_localConfiguration);
				final int communicationState= deserializer.readByte();
				return communicationState;
			}
			catch(IOException ex) {
				throw new RequestException("Fehlerhafte Antwort - DynamischeKollektionElemente - erhalten", ex);
			}
		}

		public void unsubscribeConfigurationCommunicationChanges(final SystemObject object) throws RequestException {
			ensureSystemModellVersion(19, "Abmeldung auf Änderungen des Kommunikationsstatus kann nicht durchgeführt werden");
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				// Datensatz zusammensetzen
				serializer.writeObjectReference(object);

				_senderReadConfigObjects.sendData("KommunikationszustandAbmeldung", byteArrayStream.toByteArray(), 0);
			}
			catch(SendSubscriptionNotConfirmed e) {
				_debug.fine("Exception beim Abmelden in RemoteRequester.unsubscribeConfigurationCommunicationChanges wird ignoriert e", e);
			}
			catch(DataNotSubscribedException e) {
				_debug.fine("Exception beim Abmelden in RemoteRequester.unsubscribeConfigurationCommunicationChanges wird ignoriert e", e);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RequestException(ex);
			}
			// Da keine Antwort erwartet wird, muss auch nicht auf eine Antwort gewartet werden
		}

		/**
		 * Liefert für die angegebenen Systemobjekte jeweils einen konfigurierenden Datensatz der angegebenen Attributgruppenverwendung zurück.
		 *
		 * @param systemObject        Array mit Systemobjekten für die Datensätze abgefragt werden sollen.
		 * @param attributeGroupUsage Attributgruppenverwendung, die Attributgruppe und Aspekt des Datensatzes festlegt.
		 *
		 * @return Array das für jedes angefragte Systemobjekt einen entsprechenden konfigurierenden Datensatz enthält. Ein Datensatz ist entweder ein Byte-Array das
		 *         mit der Serialisiererversion 2 erzeugt wurde, oder null, wenn für das jeweilige Systemobjekt kein Datensatz existiert.
		 *
		 * @throws RequestException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
		 * @see de.bsvrz.sys.funclib.dataSerializer.SerializingFactory
		 */
		public byte[][] getConfigurationData(
				SystemObject[] systemObject, AttributeGroupUsage attributeGroupUsage
		) throws RequestException {
			int requestIndex;
			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(8);
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeLong(attributeGroupUsage.getId());
				serializer.writeInt(systemObject.length);
				for(int i = 0; i < systemObject.length; i++) {
					serializer.writeLong(systemObject[i].getId());
				}
				requestIndex = _senderReadConfigObjects.sendData("DatensatzAnfrage", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				throw new RequestException("Fehler beim Versand der Anfrage", e);
			}
			Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
			Deserializer deserializer = getMessageDeserializer(reply, "DatensatzAntwort");
			try {
				int numberOfDatasets = deserializer.readInt();
				if(numberOfDatasets != systemObject.length) {
					throw new RequestException("Empfangene Datensatz-Anzahl nicht wie erwartet");
				}
				byte[][] results = new byte[numberOfDatasets][];
				for(int i = 0; i < numberOfDatasets; i++) {
					int numberOfBytes = deserializer.readInt();
					results[i] = numberOfBytes == 0 ? null : deserializer.readBytes(numberOfBytes);
				}
				return results;
			}
			catch(IOException e) {
				throw new RequestException("Fehler beim Auswerten der Antwort", e);
			}
		}

		public void setConfigurationData(AttributeGroupUsage attributeGroupUsage, SystemObject systemObject, byte[] data)
				throws ConfigurationChangeException, RequestException {

			int requestIndex;
			try {
				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Die Daten werden in folgender Reihenfolge geschrieben:
				// Id der Attributgruppenverwendung, long
				// Id des Systemobjekts, long
				// Länge des konfigurienden Datensatzes, int (Länge 0 wird auf der Gegenseite als <code>null</code> interpretiert und führt zum löschen
				// des Datensatzes)
				// Wenn die Länge des Byte-Arrays größer 0 ist:
				// Datensatz als Byte-Array

				serializer.writeLong(attributeGroupUsage.getId());
				serializer.writeLong(systemObject.getId());

				serializer.writeInt(data.length);
				if(data.length > 0) {
					// Es gibt einen Datensatz
					serializer.writeBytes(data);
				}

				requestIndex = _senderWriteConfigObjects.sendData("KonfigurierendenDatensatzFestlegen", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				// Die Anfrage an die Konfiguration konnte nicht verschickt werden
				throw new RequestException("Fehler beim Versand der Anfrage", e);
			}

			// Auf die Antwort warten
			final Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);
			// Antwort erhalten - wird jetzt ausgelesen.
			// Nur der Fehlerfall ist von Interesse. Wurde alles korrekt ausgeführt, wird die Antwort ignoriert.
			// Bei einem Fehler wird die entsprechende Exception geworfen.
			try {
				final Deserializer deserializer = getMessageDeserializer2(reply, "KonfigurierendenDatensatzFestlegenAntwort");
			}
			catch(ConfigurationTaskException e) {
				// Diese Exception kann nicht geworfen werden. Da die Konfiguration mit einer ChangedException ablehnen müßte.
				_debug.error("Unerwarteter Fehler beim Schreiben von konfigurierenden Datensätzen", e);
				throw new ConfigurationChangeException(e);
			}
		}

		/**
		 * Diese Methode verschickt ein Telegramm vom Typ "AuftragBenutzerverwaltung" und wartet anschließend auf die Antwort.
		 *
		 * @param message Nachricht, die verschickt werden soll
		 *
		 * @return Statusmeldung oder Antwort der Benutzerverwaltung auf die Anfrage. -1 falls die Anfrage keinen Rückgabewert liefert.
		 * 
		 * @throws RequestException Fehler bei der Bearbeitung des Telegramms (Der Benutzer hatte nicht die nötigen Rechte diesen Auftrag zu erteilen, usw.)
		 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Fehler bei Bearbeitung des Auftrags auf Konfigurationsseite
		 */
		private int sendUserAdministrationTask(final byte[] message) throws RequestException, ConfigurationTaskException {
			int requestIndex;
			try {
				requestIndex = _senderUserAdministration.sendData("AuftragBenutzerverwaltung", message);

//					Data request = createRequestData("AuftragBenutzerverwaltung", message);
//					requestIndex = request.getScaledValue("anfrageIndex").intValue();
//					_debug.finer("sending request: ", request);
//					_connection.sendData(
//							new ResultData(
//									_configurationAuthority, _requestDescription, System.currentTimeMillis(), request
//							)
//					);
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
			Data reply = _senderUserAdministration.waitForReply(requestIndex);
			// Diese Methode wirft eine Exception, wenn der Auftrag nicht ausgeführt werden konnte.
			// Die Antwort (ein Integer), ist für bestimmte Anfragen von Interesse und wird zurückgegeben
			try {
				return getMessageDeserializer2(reply, "AuftragBenutzerverwaltungAntwort").readInt();
			}
			catch(IOException e) {
				// Falls readInt fehlschlägt, sendet die Konfiguration wohl ein leeres Datenpaket als Antwort. Daraus lässt sich schließen,
				// dass diese noch keine Antworten auf Benutzerverwaltungsaufträge unterstützt und der Rückgabewert daher auf -1 gesetzt werden kann.
				return -1;
			}
			// Andere Exceptions an aufrufende Funktion weitergeben
		}

		/**
		 * Nimmt eine beliebige Exception entgegen und meldet dann die Verbindung zum Datenverteiler ab. Nach der Abmeldung wird eine IllegalStateException geworfen.
		 *
		 * @param e Grund, warum die Verbindung abgebrochen werden muss
		 */
		private void closeConnectionAndThrowException(Exception e) {
			_connection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Kommunikationsprobleme mit der Konfiguration, Verbidung zum Datenverteiler wird abgemeldet. Grund: " + e);
		}

		public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
				throws ConfigurationTaskException {

			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(username);
				serializerParameters.writeString(singleServingPassword);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(1, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
		}

		public int getSingleServingPasswordCount(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(username);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(8, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				return sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			return -1;
		}

		public void clearSingleServingPasswords(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(username);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(6, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
		}

		public void createNewUser(
				String orderer,
				String ordererPassword,
				String newUsername,
				String newUserPid,
				String newPassword,
				boolean adminRights,
				String pidConfigurationArea
		) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(newUsername);
				serializerParameters.writeString(newUserPid);
				serializerParameters.writeString(newPassword);
				serializerParameters.writeBoolean(adminRights);
				serializerParameters.writeString(pidConfigurationArea);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(2, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);

				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
		}

		public void deleteUser(final String orderer, final String ordererPassword, final String userToDelete) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(userToDelete);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(5, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
		}

		public boolean isUserAdmin(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(username);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(7, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				return sendUserAdministrationTask(byteArrayStream.toByteArray()) == 1;
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			return false; //Sollte nicht erreicht werden
		}

		public boolean isUserValid(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(username);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(10, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);
				// Daten verschicken und auf Antwort warten
				return sendUserAdministrationTask(byteArrayStream.toByteArray()) == 1;
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Einmalpasswörtern", e);
				closeConnectionAndThrowException(e);
			}
			return false; //Sollte nicht erreicht werden
		}

		public void createNewUser(
				final String orderer,
				final String ordererPassword,
				final String newUsername,
				final String newUserPid,
				final String newPassword,
				final boolean adminRights,
				final String pidConfigurationArea,
				final Collection<DataAndATGUsageInformation> data) throws ConfigurationTaskException {
			try {

				if(data == null || data.size() == 0) {
					//Wenn keine Konfigurationsdaten mitgeliefert werden sollen, createNewUser-Methode aufrufen, die diese nicht mitüberträgt.
					createNewUser(orderer, ordererPassword, newUsername, newUserPid, newPassword, adminRights, pidConfigurationArea);
					return;
				}

				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(newUsername);
				serializerParameters.writeString(newUserPid);
				serializerParameters.writeString(newPassword);
				serializerParameters.writeBoolean(adminRights);
				serializerParameters.writeString(pidConfigurationArea);
				//DataAndATGUsageInformation serialisieren, es wurde am Anfang der Funktion geprüft, ob data null ist.
				serializerParameters.writeInt(data.size());
				for(DataAndATGUsageInformation dataAndATGUsageInformation : data) {
					serializerParameters.writeObjectReference(dataAndATGUsageInformation.getAttributeGroupUsage());
					serializerParameters.writeData(dataAndATGUsageInformation.getData());
				}
				
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(9, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);

				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Anlegen eines neuen Benutzers", e);
				closeConnectionAndThrowException(e);
			}
		}

		public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener) {
			final DafDynamicObjectType userType = (DafDynamicObjectType)_connection.getDataModel().getType("typ.benutzer");
			if(listener != null){
				userType.addChangeListener((short)0, listener);
			}
			return userType.getElements();
		}


		public void unsubscribeUserChangeListener(final MutableCollectionChangeListener listener) {
			final DafDynamicObjectType userType = (DafDynamicObjectType)_connection.getDataModel().getType("typ.benutzer");
			if(listener != null){
				userType.removeChangeListener((short)0, listener) ;
			}
		}


		public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(user);
				serializerParameters.writeBoolean(adminRights);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(4, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);

				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Benutzerrechten", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern von Benutzerrechten", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern von Benutzerrechten", e);
				closeConnectionAndThrowException(e);
			}
		}

		public void changeUserPassword(String orderer, String ordererPassword, String user, String newPassword) throws ConfigurationTaskException {
			try {
				// Es wird ein Serializer zum serialisieren der übergebenen Parameter benötigt
				final ByteArrayOutputStream parameters = new ByteArrayOutputStream();
				final Serializer serializerParameters = SerializingFactory.createSerializer(parameters);
				serializerParameters.writeString(user);
				serializerParameters.writeString(newPassword);
				// Verschlüsselter Auftrag
				final byte[] encryptedMessage = createTelegramByteArray(3, serializerParameters.getVersion(), parameters.toByteArray(), ordererPassword);

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerParameters.getVersion(), byteArrayStream);

				// Auftraggeber in Klarschrift
				serializer.writeString(orderer);

				// Benutztes Verschlüsselungsverfahren in Klarschrift
				serializer.writeString(_encryptDecryptProcedure.getName());

				// Der verschlüsselte Text
				serializer.writeInt(encryptedMessage.length);
				serializer.writeBytes(encryptedMessage);

				// Daten verschicken und auf Antwort warten
				sendUserAdministrationTask(byteArrayStream.toByteArray());
			}
			catch(IOException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern des Benutzerpassworts", e);
				closeConnectionAndThrowException(e);
			}
			catch(RequestException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Ändern des Benutzerpassworts", e);
				closeConnectionAndThrowException(e);
			}
			catch(NoSuchVersionException e) {
				e.printStackTrace();
				_debug.error("Fehler beim Serialisieren der Konfigurationsanfrage zum Ändern des Benutzerpassworts", e);
				closeConnectionAndThrowException(e);
			}
		}

		/**
		 * Fordert von der Konfiguration ein Zufallstext an, dieser wird dann mit einem Auftrag versendet
		 *
		 * @return Zufallstext, der durch die Konfiguration erzeugt wurde
		 */
		private byte[] getRandomText() throws IOException, RequestException, NoSuchVersionException {
			int requestIndex;
			try {
				requestIndex = _senderUserAdministration.sendData("AuftragZufallstext", new byte[]{1});
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
			final Data reply = _senderUserAdministration.waitForReply(requestIndex);
			// Das ist der Zufallstext
			final Deserializer deserializer = SerializingFactory.createDeserializer(2, new ByteArrayInputStream(reply.getScaledArray("daten").getByteArray()));
			final int sizeOfData = deserializer.readInt();
			return deserializer.readBytes(sizeOfData);
		}

		public Map<String, ConfigurationArea> getAllConfigurationAreas() throws RequestException {
			// Datensatz an die Konfiguration verschicken und auf die Antwort warten.
			// Es wird ein leeres byte-Array mitgeschickt, da keine Daten spezifiziert werden müssen
			final Data data = sendConfigAreaTask("AlleBereicheAnfordern", new byte[0]);
			try {
				return getAllConfigurationAreasResult(getMessageDeserializer(data, "AlleBereicheAnfordernAntwort"));
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
		}

		/**
		 * Ließt aus einem Deserialier die Antwort auf eine Konfigurationsbereichsanfrage aus.
		 *
		 * @param deserializer Objekt, das die serialisierte Antwort enthält
		 *
		 * @return Als Schlüssel dient die Pid, Value ist ein Konfigurationsbereich
		 */
		private Map<String, ConfigurationArea> getAllConfigurationAreasResult(Deserializer deserializer) throws IOException {
			// Anzahl Konfigurationsbereiche. Jeder Eintrag entspricht einer Id
			final int numberOfConfigurationAreas = deserializer.readInt();

			// Ergebnis
			final Map<String, ConfigurationArea> result = new HashMap<String, ConfigurationArea>(numberOfConfigurationAreas);

			for(int nr = 0; nr < numberOfConfigurationAreas; nr++) {
				final long id = deserializer.readLong();
				final SystemObject configurationArea = _localConfiguration.getObject(id);
				result.put(configurationArea.getPid(), (ConfigurationArea)configurationArea);
			}

			return result;
		}

		public ConfigurationArea createConfigurationArea(String name, String pid, String authorityPid) throws RequestException, ConfigurationChangeException {
			try {
				// Datensatz an die Konfiguration verschicken und auf die Antwort warten
				final Data data = sendConfigAreaTask("BereichAnlegen", serializeStrings(name, pid, authorityPid));

				// Die Antwort auswerten
				return getCreateConfigurationAreaResult(getMessageDeserializer2(data, "BereichAnlegenAntwort"));
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				// Dieser Fall kann nicht auftreten, die Methode wirft allerdings diese Exception.
				// In diesem Fall sind die beiden Exceptions gleich und bedeuten "Die Konfiguration führt den Auftrag nicht aus".
				throw new ConfigurationChangeException(e);
			}
		}

		/**
		 * Ließt aus einem Deserialisierer eine Id und erzeugt das dazugehörige Objekt
		 *
		 * @param deserializer Enthält die Id
		 *
		 * @return Konfigurationsbereich, dessen Id ausgelesen wurde
		 *
		 * @throws IOException
		 */
		private ConfigurationArea getCreateConfigurationAreaResult(Deserializer deserializer) throws IOException {
			return (ConfigurationArea)_localConfiguration.getObject(deserializer.readLong());
		}


		/**
		 * Erzeugt mit einem Serializer ein byte-Array, das drei Strings enthält
		 *
		 * @param stringOne   Wird als erstes serialisiert
		 * @param stringTwo   Wird als zweites serialisiert
		 * @param stringThree Wird als drittes serialisiert
		 *
		 * @return byte-Array, das Strings enthält
		 *
		 * @throws IOException
		 * @throws NoSuchVersionException
		 */
		private byte[] serializeStrings(String stringOne, String stringTwo, String stringThree) throws IOException, NoSuchVersionException {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Serializer serializer = SerializingFactory.createSerializer(2, outputStream);
			serializer.writeString(stringOne);
			serializer.writeString(stringTwo);
			serializer.writeString(stringThree);

			return outputStream.toByteArray();
		}

		public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas) throws RequestException {
			try {
				final Data data = sendConfigAreaTask("BereichePrüfen", serializeConfigAndVersion(configurationAreas));

				// Falls es eine Fehlerantwort war, so wird eine RequestException geworfen
				return getConsistencyCheckResult(getMessageDeserializer(data, "BereichePrüfenAntwort"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
		}

		public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas)
				throws RequestException, ConfigurationChangeException {
			try {
				final Data data = sendConfigAreaTask("BereicheAktivieren", serializeConfigAndVersion(configurationAreas));
				return getConsistencyCheckResult(getMessageDeserializer2(data, "BereicheAktivierenAntwort"));
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				throw new ConfigurationChangeException(e);
			}
		}

		public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
				throws RequestException, ConfigurationChangeException {
			try {
				final Data data = sendConfigAreaTask("BereicheFreigabeZurÜbernahme", serializeConfigAndVersion(configurationAreas));
				return getConsistencyCheckResult(getMessageDeserializer2(data, "BereicheFreigabeZurÜbernahmeAntwort"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				throw new ConfigurationChangeException(e);
			}
		}

		public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas)
				throws RequestException, ConfigurationChangeException {
			try {
				final Data data = sendConfigAreaTask("BereicheFreigabeZurAktivierung", serializeConfigAndVersion(configurationAreas));
				// Die Antwort(es hat geklappt) wird nicht benötigt, es wird nur geprüft, ob es zu einem Fehler gekommen ist.
				getConsistencyCheckResult(getMessageDeserializer2(data, "BereicheFreigabeZurAktivierungAntwort"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				throw new ConfigurationChangeException(e);
			}
		}

		public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
				throws RequestException, ConfigurationChangeException {
			try {
				final Data data = sendConfigAreaTask("BereicheFreigabeZurAktivierungOhneKVAktivierung", serializeConfigAndVersion(configurationAreas));
				return getConsistencyCheckResult(getMessageDeserializer2(data, "AntwortBereicheFreigabeZurAktivierungOhneKVAktivierung"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				throw new ConfigurationChangeException(e);
			}
		}

		public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationChangeException {

			try {
				final Data data = sendConfigAreaTask("BereicheImportieren", serializeImportExportTask(importPath, configurationAreaPids));

				// Die Antwort(es hat geklappt) wird nicht benötigt, es wird nur geprüft, ob es zu einem Fehler gekommen ist.
				getConsistencyCheckResult(getMessageDeserializer2(data, "BereicheImportierenAntwort"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(ConfigurationTaskException e) {
				// Dieser Fall kann nicht auftreten, die Methode wirft allerdings diese Exception.
				// In diesem Fall sind die beiden Exceptions gleich und bedeuten "Die Konfiguration führt den Auftrag nicht aus".
				throw new ConfigurationChangeException(e);
			}
		}

		public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws RequestException, ConfigurationTaskException {
			try {
				final Data data = sendConfigAreaTask("BereicheExportieren", serializeImportExportTask(exportPath, configurationAreaPids));

				// Die Antwort(es hat geklappt) wird nicht benötigt, es wird nur geprüft, ob es zu einem Fehler gekommen ist.
				getConsistencyCheckResult(getMessageDeserializer2(data, "BereicheExportierenAntwort"));
			}
			catch(NoSuchVersionException e) {
				throw new RequestException(e);
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
		}


		public BackupResult backupConfigurationFiles(String targetDirectory, BackupProgressCallback callback)
				throws ConfigurationTaskException, RequestException {	
			try {
				return sendConfigAreaBackupTask(serializeBackupTask(targetDirectory), callback);
			}
			catch(IOException e) {
				throw new ConfigurationChangeException(e);
			}
			catch(NoSuchVersionException e) {
				throw new ConfigurationChangeException(e);
			}
		}

		public short getActiveVersion(ConfigurationArea configurationArea) throws RequestException {
			final int requestIndex;
			try {
				// Aufbau des Telegramms:
				// Referenz auf das Objekt

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeObjectReference(configurationArea);
				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("AktiveVersionKonfigurationsbereich", byteArrayStream.toByteArray());
				Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				Deserializer deserializer = getMessageDeserializer(reply, "AntwortAktiveVersionKonfigurationsbereich");
				return deserializer.readShort();
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
		}

		public short getModifiableVersion(ConfigurationArea configurationArea) throws RequestException {
			final int requestIndex;
			try {
				// Aufbau des Telegramms:
				// Referenz auf das Objekt

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeObjectReference(configurationArea);
				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("VersionInArbeitKonfigurationsbereich", byteArrayStream.toByteArray());
				Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				Deserializer deserializer = getMessageDeserializer(reply, "AntwortVersionInArbeitKonfigurationsbereich");
				return deserializer.readShort();
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
		}

		public Collection<SystemObject> getObjects(String pid, long startTime, long endTime) throws RequestException {
			final int requestIndex;
			try {

				// Aufbau des Telegramms:
				// Pid, (String)
				// Startzeitpunkt, long
				// Endzeitpunkt, long

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeString(pid);
				serializer.writeLong(startTime);
				serializer.writeLong(endTime);

				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("ObjekteAnfragenMitPidUndZeitbereich", byteArrayStream.toByteArray());
				// Antwort der Konfiguration, Daten auslesen
				Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				Deserializer deserializer = getMessageDeserializer(reply, "AntwortObjekteAnfragenMitPidUndZeitbereich");

				final Collection<SystemObject> result = new ArrayList<SystemObject>();

				// Anzahl Objekte auslesen
				final int numberOfSystemObjects = deserializer.readInt();

				// Alle Objekte der Collection einlesen
				for(int nr = 0; nr < numberOfSystemObjects; nr++) {
					result.add(deserializer.readObjectReference(_localConfiguration));
				}

				return result;
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
		}

		public Collection<SystemObject> getObjects(
				Collection<ConfigurationArea> configurationAreas,
				Collection<SystemObjectType> systemObjectTypes,
				ObjectTimeSpecification objectTimeSpecification
		) throws RequestException {
			final int requestIndex;
			try {

				// Aufbau des Telegramms:
				// Anzahl Referenzen der Konfigurationsbereiche, int (-99 wird als <code>null</code> interpretiert)
				//      Referenzen der Konfigurationsbereiche
				// Anzahl SystemObjectTypes, int (-99 wird als <code>null</code> interpretiert)
				//      Referenzen auf die Typen
				// Danach wird die ObjectTimeSpezifikation gespeichert, sie besitzt folgenden Aufbau:
				// Der Aufbau unterscheidet sich je nach Typ, Beschreibung siehe unten

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Anzahl Bereiche und die Objekte der Collection (null wird mit -99 dargestellt)
				if(configurationAreas != null) {
					serializer.writeInt(configurationAreas.size());
					for(ConfigurationArea configurationArea : configurationAreas) {
						serializer.writeObjectReference(configurationArea);
					}
				}
				else {
					serializer.writeInt(-99);
				}

				// Anzahl SystemObjectTypes und die Objekte der Collection (null wird mit -99 kodiert)
				if(systemObjectTypes != null) {
					serializer.writeInt(systemObjectTypes.size());
					for(SystemObjectType systemObjectType : systemObjectTypes) {
						serializer.writeObjectReference(systemObjectType);
					}
				}
				else {
					serializer.writeInt(-99);
				}

				// ObjectTimeSpecification, es gibt keine klare Struktur, je nach Typ werden andere Daten gespeichert
				serializeObjectTimeSpezifikation(serializer, objectTimeSpecification);

				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("ObjekteMitBereichUndTypAnfragen", byteArrayStream.toByteArray());
				// Antwort der Konfiguration, Daten auslesen
				Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				Deserializer deserializer = getMessageDeserializer(reply, "AntwortObjekteMitBereichUndTypAnfragen");

				final Collection<SystemObject> result = new ArrayList<SystemObject>();

				// Anzahl Objekte auslesen
				final int numberOfSystemObjects = deserializer.readInt();

				// Alle Objekte der Collection einlesen
				for(int nr = 0; nr < numberOfSystemObjects; nr++) {
					result.add(deserializer.readObjectReference(_localConfiguration));
				}

				return result;
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
		}

		public Collection<SystemObject> getDirectObjects(
				ConfigurationArea area, Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification
		) throws RequestException {
			final int requestIndex;
			try {

				// Aufbau des Telegramms:
				// Referenz auf den Bereich
				// Anzahl SystemObjectTypes, int
				//      Referenzen auf die Typen
				// Danach wird die ObjectTimeSpezifikation gespeichert, sie besitzt folgenden Aufbau:
				// Der Aufbau unterscheidet sich je nach Typ, Beschreibung siehe unten

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Bereich speichern
				serializer.writeObjectReference(area);

				// Typen speichern
				serializer.writeInt(systemObjectTypes.size());
				for(SystemObjectType systemObjectType : systemObjectTypes) {
					serializer.writeObjectReference(systemObjectType);
				}

				// ObjectTimeSpecification, es gibt keine klare Struktur, je nach Typ werden andere Daten gespeichert
				serializeObjectTimeSpezifikation(serializer, timeSpecification);

				// Daten verschicken
				requestIndex = _senderReadConfigObjects.sendData("ObjekteDirekterTyp", byteArrayStream.toByteArray());
				// Antwort der Konfiguration, Daten auslesen
				Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				Deserializer deserializer = getMessageDeserializer(reply, "AntwortObjekteDirekterTyp");

				final Collection<SystemObject> result = new ArrayList<SystemObject>();

				// Anzahl Objekte auslesen
				final int numberOfSystemObjects = deserializer.readInt();

				// Alle Objekte der Collection einlesen
				for(int nr = 0; nr < numberOfSystemObjects; nr++) {
					result.add(deserializer.readObjectReference(_localConfiguration));
				}
				return result;
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
		}

		public ConfigurationObject createConfigurationObject(
				ConfigurationArea configurationArea, ConfigurationObjectType type, String pid, String name, List<ObjectSet> sets
		) throws ConfigurationChangeException, RequestException {

			final int requestIndex;

			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms:
				// Konfigurationsobjekt(true), boolean
				// Konfigurationsbereich, Referenz
				// Pid, string
				// name, string
				// ConfigurationObjectType, Referenz
				// Anzahl Elemente der Liste, int (Wurde null übergeben, wird dies mit -99 codiert)
				//      Referenzen auf die Objekte der Menge

				// Es ist ein Konfigurationsobjekt
				serializer.writeBoolean(true);
				// Bereich, in dem das Objekte angelegt werden soll
				serializer.writeObjectReference(configurationArea);
				// Pid
				serializer.writeString(pid);
				// Name
				serializer.writeString(name);
				// Type des Objekts
				serializer.writeObjectReference(type);

				// Es kann <code>null</code> übergeben werden, dies wird mit -99 kodiert
				if(sets != null) {
					serializer.writeInt(sets.size());
					for(ObjectSet objectSet : sets) {
						serializer.writeObjectReference(objectSet);
					}
				}
				else {
					serializer.writeInt(-99);
				}

				requestIndex = _senderWriteConfigObjects.sendData("ObjektAnlegen", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Erzeugen eines neuen Konfigurationsobjekts", e);
				throw new RequestException(e);
			}

			final Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);

			try {
				final Deserializer deserializer = getMessageDeserializer2(reply, "AntwortObjektAnlegen");
				try {
					return (ConfigurationObject)deserializer.readObjectReference(_localConfiguration);
				}
				catch(IOException e) {
					// Fehler beim auspacken der Daten
					e.printStackTrace();
					_debug.error("Fehler beim Deserialisieren der Konfigurationsantwort beim Erzeugen eines neuen Konfigurationsobjekts", e);
					throw new RequestException(e);
				}
			}
			catch(ConfigurationTaskException e) {
				// Die beiden Fälle können gleich behandlet werden. Die Konfiguration lehnt es ab das Objekt
				// anzulegen.
				e.printStackTrace();
				_debug.error("Konfiguration hat das Erzeugen eines neuen Objekts verweigert", e);
				throw new ConfigurationChangeException(e);
			}
		}

		public DynamicObject createDynamicObject(ConfigurationArea configurationArea, DynamicObjectType type, String pid, String name)
				throws ConfigurationChangeException, RequestException {
			final int requestIndex;

			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms:
				// Konfigurationsobjekt(false), boolean
				// Konfigurationsbereich, Referenz
				// Pid, string
				// name, string
				// DynamicObjectType, Referenz

				// Es ist ein Konfigurationsobjekt
				serializer.writeBoolean(false);
				// Bereich, in dem das Objekte angelegt werden soll
				serializer.writeObjectReference(configurationArea);
				// Pid
				serializer.writeString(pid);
				// Name
				serializer.writeString(name);
				// Type des Objekts
				serializer.writeObjectReference(type);

				requestIndex = _senderWriteConfigObjects.sendData("ObjektAnlegen", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Erzeugen eines neuen dynamischen Objekts", e);
				throw new RequestException(e);
			}

			return waitForResponseAndDeserializeDynamicObject(requestIndex);
		}

		/**
		 * Diese Methode wird aufgerufen, wenn ein dynamiches Objekt angelegt werden soll und die Antwort erwartet wird.
		 * Die Methode blockiert solange, bis die Antwort empfangen wird. Dann wird das dynamische Objekt aus der Antwort ausgelesen und
		 * zurückgegeben.
		 *
		 * @param requestIndex Index der Anfrage, die Antwort wird den selben Index haben.
		 * @return Objekt, das angelegt wurde.
		 * @throws RequestException Technisches Problem beim empfang/Deserialisierung der Antwort
		 * @throws ConfigurationChangeException Die Konfiguration kann den Auftrag nicht durchführen (fehlenden Rechte)
		 */
		private DynamicObject waitForResponseAndDeserializeDynamicObject(final int requestIndex) throws RequestException, ConfigurationChangeException {
			final Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);

			try {
				final Deserializer deserializer = getMessageDeserializer2(reply, "AntwortObjektAnlegen");
				try {
					return (DynamicObject)deserializer.readObjectReference(_localConfiguration);
				}
				catch(IOException e) {
					// Fehler beim auspacken der Daten
					e.printStackTrace();
					_debug.error("Fehler beim Deserialisieren der Konfigurationsantwort beim Erzeugen eines neuen dynamischen Objekts", e);
					throw new RequestException(e);
				}
			}
			catch(ConfigurationTaskException e) {
				// Die beiden Fälle können gleich behandlet werden. Die Konfiguration lehnt es ab das Objekt
				// anzulegen.
				e.printStackTrace();
				_debug.error("Konfiguration hat das Erzeugen eines neuen dynamischen Objekts verweigert", e);
				throw new ConfigurationChangeException(e);
			}
		}

		public DynamicObject createDynamicObject(
				ConfigurationArea configurationArea, DynamicObjectType type, String pid, String name, List<DataAndATGUsageInformation> data
		) throws ConfigurationChangeException, RequestException {
			final int requestIndex;


			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms:
				// Konfigurationsbereich, Referenz
				// Pid, string
				// name, string
				// DynamicObjectType, Referenz
				// Anzahl folgender Datensätze+ATG-Verwendungen, int (Wert 0 bedeutet, dass eine leere Liste oder null übergeben wurde)
				//  ATG-Verwendung, Referenz
				//  Datensatz, serialisierter Datensatz

				serializer.writeObjectReference(configurationArea);
				serializer.writeString(pid);
				serializer.writeString(name);
				serializer.writeObjectReference(type);

				// Nun die Liste
				final int dataLength;
				if(data != null) {
					dataLength = data.size();
				}
				else {
					dataLength = 0;
				}

				serializer.writeInt(dataLength);

				if(dataLength > 0) {
					for(DataAndATGUsageInformation dataAndATGUsageInformation : data) {
						serializer.writeObjectReference(dataAndATGUsageInformation.getAttributeGroupUsage());
						serializer.writeData(dataAndATGUsageInformation.getData());
					}
				}
				requestIndex = _senderWriteConfigObjects.sendData("DynamischesObjektMitKonfigurierendenDatensaetzenAnlegen", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Erzeugen eines neuen dynamischen Objekts", e);
				throw new RequestException("Fehler beim Versand der Daten: " + e);
			}

			// Telegramm wurde verschickt, nun auf die Antwort warten
			return waitForResponseAndDeserializeDynamicObject(requestIndex);
		}

		public SystemObject duplicate(final SystemObject systemObject, final Map<String, String> substitutePids)
				throws ConfigurationChangeException, RequestException {
			final int requestIndex;

			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms:
				// Referenz auf das zu kopierende Objekt
				// Größe der Map (int)
				// Jeder Eintrag in der Map besteht aus:
				// Pid, die ersetzt werden soll, string
				// Pid ersetzende Pid, string

				// Objekt, das kopiert werden soll
				serializer.writeObjectReference(systemObject);

				// Anzahl Elemente
				serializer.writeInt(substitutePids.size());

				final Set<String> allKeys = substitutePids.keySet();

				for(String key : allKeys) {
					final String value = substitutePids.get(key);
					serializer.writeString(key);
					serializer.writeString(value);
				}

				requestIndex = _senderWriteConfigObjects.sendData("ObjektKopieren", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Duplizieren eines Konfigurationsobjekts", e);
				throw new RequestException(e);
			}

			final Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);

			final Deserializer deserializer;
			try {
				deserializer = getMessageDeserializer2(reply, "AntwortObjektKopieren");
			}
			catch(ConfigurationTaskException e) {
				// Da die Konfiguration sich weigert den Auftrag durchzuführen, muss in diesem Fall kein unterschied gemacht werden
				throw new ConfigurationChangeException(e);
			}
			try {
				return deserializer.readObjectReference(_localConfiguration);
			}
			catch(IOException e) {
				// Fehler beim auspacken der Daten
				e.printStackTrace();
				_debug.error("Fehler beim Deserialisieren der Konfigurationsantwort beim Duplizieren eines Konfigurationsobjekts", e);
				throw new RequestException(e);
			}
		}

		public List<SystemObject> getNewObjects(final ConfigurationArea configurationArea) throws RequestException {
			final int requestIndex;

			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms:
				// Referenz auf den Konfigurationsbereich, aus dem die neuen Objekte angefordert werden sollen

				serializer.writeObjectReference(configurationArea);

				requestIndex = _senderReadConfigObjects.sendData("NeueObjekteEinesBereichsAnfordern", byteArrayStream.toByteArray());
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Senden der Konfigurationsanfrage zum Abfragen von noch nicht aktivierten neuen Objekten", e);
				throw new RequestException(e);
			}

			final Data reply = _senderReadConfigObjects.waitForReply(requestIndex);

			final Deserializer deserializer = getMessageDeserializer(reply, "AntwortNeueObjekteEinesBereichsAnfordern");

			try {
				// Daten auslesen
				return readSystemObjectList(deserializer);
			}
			catch(IOException e) {
				// Fehler beim versuch die Daten auszulesen
				e.printStackTrace();
				_debug.error("Fehler beim Deserialisieren der Konfigurationsantwort beim Abfragen von noch nicht aktivierten neuen Objekten", e);
				throw new RequestException(e);
			}
		}

		public Collection<SystemObject> getSetElements(ObjectSet set, ObjectTimeSpecification objectTimeSpecification) throws RequestException {

			try {
				final int requestIndex;

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms
				// 1) Menge (Objektreferenz)
				// 2) Kodierung der Zeit (siehe Kommentar der Methode)

				serializer.writeObjectReference(set);
				serializeObjectTimeSpezifikation(serializer, objectTimeSpecification);

				requestIndex = _senderReadConfigObjects.sendData("ElementeEinerMengeZeit", byteArrayStream.toByteArray());

				// Auf die Antwort warten
				final Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				final Deserializer deserializer = getMessageDeserializer(reply, "AntwortElementeEinerMengeZeit");

				// Antwort auslesen
				return readSystemObjectList(deserializer);
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Abfragen der Elemente einer Menge", e);
				throw new RequestException(e);
			}
		}

		public Collection<SystemObject> getSetElementsInNextVersion(ObjectSet set) throws RequestException {
			// Die Versionen werden nicht gebraucht
			return sendSetElementVersionRequest(set, KindOfVersion.IN_NEXT_VERSION, (short)-99, (short)-99);
		}

		public Collection<SystemObject> getSetElementsInVersion(ObjectSet set, short version) throws RequestException {
			return sendSetElementVersionRequest(set, KindOfVersion.IN_VERSION, version, (short)-1);
		}

		public Collection<SystemObject> getSetElementsInAllVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException {
			return sendSetElementVersionRequest(set, KindOfVersion.IN_ALL_VERSIONS, fromVersion, toVersion);
		}

		public Collection<SystemObject> getSetElementsInAnyVersions(ObjectSet set, short fromVersion, short toVersion) throws RequestException {
			return sendSetElementVersionRequest(set, KindOfVersion.IN_ANY_VERSIONS, fromVersion, toVersion);
		}

		public void editConfigurationSet(ConfigurationObject configurationObject, ObjectSet set, boolean addSet)
				throws RequestException, ConfigurationChangeException {

			try {
				final int requestIndex;

				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms
				// 1) Objekt, dessen Menge geändert werden soll (Objektreferenz)
				// 2) Menge (Objektreferenz)
				// 3) boolean (true = Die Menge soll am Objekt hinzugefügt werden; false = Die Menge soll entfernt werden)

				serializer.writeObjectReference(configurationObject);
				serializer.writeObjectReference(set);
				serializer.writeBoolean(addSet);

				requestIndex = _senderWriteConfigObjects.sendData("ObjektMengenBearbeiten", byteArrayStream.toByteArray());

				// Auf die Antwort warten
				final Data reply = _senderWriteConfigObjects.waitForReply(requestIndex);
				// Beim erzeugen des Deserializers werden eventuelle Exceptions erzeugt (ConfiChangeExcpetion).
				// Wurde auf Konfigurationsseite kein Fehler geworfen, wird der Deserializer nicht weiter benötigt.
				final Deserializer deserializer = getMessageDeserializer2(reply, "AntwortObjektMengenBearbeiten");
			}
			catch(IOException e) {
				throw new RequestException(e);
			}
			catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
				throw new RequestException(sendSubscriptionNotConfirmed);
			}
			catch(ConfigurationTaskException e) {
				// an dieser Stelle kann die Task Excpetion auf Change umgebogen werden. Da nur der Hinweis "Verweigert" ausreicht.
				throw new ConfigurationChangeException(e);
			}
		}

		public void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener) {
			_senderReadConfigObjects.setMutableCollectionChangeListener(notifyingMutableCollectionChangeListener);
		}

		private List<SystemObject> readSystemObjectList(final Deserializer deserializer) throws IOException {
			// Anzahl Systemobjekte, die ausgelesen werden müssen
			final int size = deserializer.readInt();

			final List<SystemObject> typeElementObjects = new ArrayList<SystemObject>(size);

			for(int nr = 0; nr < size; nr++) {
				typeElementObjects.add(deserializer.readObjectReference(_localConfiguration));
			}
			return typeElementObjects;
		}

		/**
		 * Verschickt ein Telegramm an die Konfiguration und ermittelt alle Elemente einer Menge unter Berücksichtigung der übergebenen Versionen.
		 *
		 * @param systemObjectType
		 * @param kindOfVersion
		 * @param fromVersion
		 * @param toVersion
		 *
		 * @return Alle Elemente, die zu einer Menge gehören und in den angegebenen Versionen gültig waren (in Abhängigkeit von <code>kindOfVersion</code>).
		 *
		 * @throws RequestException
		 */
		private List<SystemObject> sendSetElementVersionRequest(
				final ObjectSet systemObjectType, final KindOfVersion kindOfVersion, final short fromVersion, final short toVersion
		) throws RequestException {

			final int requestIndex;

			try {
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);

				// Aufbau des Telegramms
				// 1) Type (Objektreferenz)
				// 2) Danach werden von der Anfrage anhängig die Versionen serialisiert (siehe Methode, dort wird der Aufbau beschrieben)


				serializer.writeObjectReference(systemObjectType);
				serializeKindOfVersion(serializer, kindOfVersion, fromVersion, toVersion);

				requestIndex = _senderReadConfigObjects.sendData("ElementeEinerMengeVersion", byteArrayStream.toByteArray());

				// Auf die Antwort warten
				final Data reply = _senderReadConfigObjects.waitForReply(requestIndex);
				final Deserializer deserializer = getMessageDeserializer(reply, "AntwortElementeEinerMengeVersion");

				// Antwort auslesen
				return readSystemObjectList(deserializer);
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Abfragen der Elemente einer Menge in einer bestimmten Version", e);
				throw new RequestException(e);
			}
		}


		/**
		 * Serialisert die übergebenen Parameter.
		 *
		 * @param serializer
		 * @param kindOfVersion
		 * @param fromVersion
		 * @param toVersion
		 *
		 * @throws IOException
		 */
		private void serializeKindOfVersion(
				final Serializer serializer, final KindOfVersion kindOfVersion, final short fromVersion, final short toVersion
		) throws IOException {

			// Telegrammaufbau:

			// 1) Um was für eine Anfrage handelt es sich (byte)

			// von 1) abhängig ist der Aufbau des Telegramms unterschiedlich:
			//  IN_ALL_VERSIONS und IN_ANY_VERSIONS:
			//      formVersion (short)
			//      toVersion (short)
			//  IN_VERSION:
			//      inVersion (short)
			//  IN_NEXT_VERSION:
			//      keine weiteren Informationen nötig

			serializer.writeByte(kindOfVersion.getCode());

			if(kindOfVersion == KindOfVersion.IN_ALL_VERSIONS || kindOfVersion == KindOfVersion.IN_ANY_VERSIONS) {
				serializer.writeShort(fromVersion);
				serializer.writeShort(toVersion);
			}
			else if(kindOfVersion == KindOfVersion.IN_NEXT_VERSION) {
				// In diesem Fall sind keine weiteren Informationen nötig
			}
			else if(kindOfVersion == KindOfVersion.IN_VERSION) {
				serializer.writeShort(fromVersion);
			}
			else {
				throw new IllegalStateException("Diese Art von Anfragen wird nicht unterstützt: " + kindOfVersion);
			}
		}


		/**
		 * Serialisiert eine ObjectTimeSpecification. Der Aufbau ist vom Typ abhängig.
		 *
		 * @param serializer
		 * @param timeSpecification
		 *
		 * @throws IOException
		 */
		private void serializeObjectTimeSpezifikation(Serializer serializer, ObjectTimeSpecification timeSpecification) throws IOException {
			// wird als erstes benötigt um rauszufinden, welche Werte noch gelesen werden können
			serializer.writeShort(timeSpecification.getType().getCode());

			if(timeSpecification.getType() == TimeSpecificationType.VALID) {
				// Es gibt kein getTime
				// Es gibt kein getStartTime
				// Es gibt kein getEndTime
				// Es muss nichts serialisiert werden
			}
			else if(timeSpecification.getType() == TimeSpecificationType.VALID_AT_TIME) {
				// Es gibt kein getStartTime
				// Es gibt kein getEndTime
				// nur getTime serialisieren
				serializer.writeLong(timeSpecification.getTime());
			}
			else {
				// Es gibt kein getTime
				serializer.writeLong(timeSpecification.getStartTime());
				serializer.writeLong(timeSpecification.getEndTime());
			}
		}


		/**
		 * Serialisiert einen String und eine Collection von Pids
		 *
		 * @param path                  Verzeichnis der Versorgungsdateien
		 * @param configurationAreaPids Pids
		 *
		 * @return byte-Array, das die übergebenen Parameter serialisiert enthält
		 *
		 * @throws NoSuchVersionException
		 * @throws IOException
		 */
		private byte[] serializeImportExportTask(File path, Collection<String> configurationAreaPids) throws NoSuchVersionException, IOException {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(2, outputStream);

			serializer.writeString(path.getAbsolutePath());
			serializer.writeInt(configurationAreaPids.size());
			for(String pid : configurationAreaPids) {
				serializer.writeString(pid);
			}

			return outputStream.toByteArray();
		}

		private byte[] serializeBackupTask(String path) throws NoSuchVersionException, IOException {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(2, outputStream);
			if(path != null){
				serializer.writeString(path);
			}
			else{
				serializer.writeString("");
			}

			return outputStream.toByteArray();
		}

		/**
		 * Erstellt mit einem Serialisierer ein Byte-Array, das Konfigurationsbereiche und dazugehörige Versionen enthält.
		 *
		 * @param configurationAreas s.o
		 *
		 * @return s.o.
		 */
		private byte[] serializeConfigAndVersion(Collection<ConfigAreaAndVersion> configurationAreas) throws NoSuchVersionException, IOException {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Serializer serializer = SerializingFactory.createSerializer(2, outputStream);
			serializer.writeInt(configurationAreas.size());
			for(ConfigAreaAndVersion configAreaAndVersion : configurationAreas) {
				serializer.writeObjectReference(configAreaAndVersion.getConfigArea());
				serializer.writeShort(configAreaAndVersion.getVersion());
			}
			return outputStream.toByteArray();
		}

		/**
		 * Ließt aus einem Deserialier die Antwort auf eine Konfigurationsbereichsanfrage aus.
		 *
		 * @param deserializer Objekt, das die serialisierte Antwort enthält
		 *
		 * @return Objekt, das aus den serialisierten Daten erzeugt wurde
		 */
		private ConsistencyCheckResultInterface getConsistencyCheckResult(Deserializer deserializer) throws IOException {
			// Wird die Antwort enthalten
			final ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();

			// Anzahl lokaler Fehler einlesen. Ist die Zahl 0, so war kein Fehler vorhanden und mögliche Interferenzfehler können
			// eingelesen werden.
			final int numberOfLocalErrors = deserializer.readInt();
			if(numberOfLocalErrors > 0) {
				// Es gab lokale Fehler, einlesen
				for(int nr = 0; nr < numberOfLocalErrors; nr++) {
					consistencyCheckResult.addEntry(getConsistencyCheckResultEntry(deserializer, ConsistencyCheckResultEntryType.LOCAL_ERROR));
				}
			}

			// Interferenzfehler einlesen, falls vorhanden
			final int numberOfInterferenceErrors = deserializer.readInt();
			if(numberOfInterferenceErrors > 0) {
				// Es gab lokale Fehler, einlesen
				for(int nr = 0; nr < numberOfInterferenceErrors; nr++) {
					consistencyCheckResult.addEntry(
							getConsistencyCheckResultEntry(
									deserializer, ConsistencyCheckResultEntryType.INTERFERENCE_ERROR
							)
					);
				}
			}

			// Warnungen einlesen
			final int numberOfWarnings = deserializer.readInt();
			if(numberOfWarnings > 0) {
				// Es gab lokale Fehler, einlesen
				for(int nr = 0; nr < numberOfWarnings; nr++) {
					consistencyCheckResult.addEntry(getConsistencyCheckResultEntry(deserializer, ConsistencyCheckResultEntryType.WARNING));
				}
			}
			return consistencyCheckResult;
		}

		/**
		 * Ließt ein serialisiertes ConsistencyCheckResultEntry aus einem Deserializer aus
		 *
		 * @param deserializer s.o.
		 * @param type         Art des Fehlers/Warnung (lokal, interferenz oder Warnung)
		 *
		 * @return Objekt
		 */
		private ConsistencyCheckResultEntry getConsistencyCheckResultEntry(Deserializer deserializer, ConsistencyCheckResultEntryType type) throws IOException {
			final ConfigurationArea configArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);
			// Text der zum Fehler oder zu der Warnung gehört
			final String errorText = deserializer.readString();
			// Anzahl Objekte, die zu dem Fehler oder der Warnung gehören
			final int numberOfInvolvedObjects = deserializer.readInt();
			final SystemObject[] involvedObjects = new SystemObject[numberOfInvolvedObjects];

			for(int nr = 0; nr < involvedObjects.length; nr++) {
				involvedObjects[nr] = deserializer.readObjectReference(_localConfiguration);
			}

			return new ConsistencyCheckResultEntry(type, configArea, involvedObjects, errorText);
		}

		/**
		 * Diese Methode verschickt ein Telegramm. Der Typ wird über einen Parameter festgelegt.
		 *
		 * @param message Nachricht, die verschickt werden soll
		 *
		 * @return Antwort auf die Anfrage
		 *
		 * @throws RequestException Fehler bei der Bearbeitung des Telegramms (Der Benutzer hatte nicht die nötigen Rechte diesen Auftrag zu erteilen, usw.)
		 */
		private Data sendConfigAreaTask(String messageType, final byte[] message) throws RequestException {
			int requestIndex;
			try {
				requestIndex = _senderConfigAreaTask.sendData(messageType, message);
			}
			catch(Exception e) {
				e.printStackTrace();
				throw new RequestException(e);
			}
			return _senderConfigAreaTask.waitForReply(requestIndex);
		}

		/**
		 * Sendet eine Backup-Anfrage an die Konfiguration und wartet auf das Beenden
		 *
		 * @param message  Serialisierte Nachricht, die den Sicherungs-Pfad enthält
		 * @param callback Objekt, das über Fortschrittsmeldungen informiert werden soll. Darf null sein.
		 *
		 * @return true wenn alle Dateien korrekt gesichert wurden.
		 *
		 * @throws RequestException           Fehler bei der Übertragung
		 * @throws ConfigurationTaskException Der Backup-Vorgang konnte nicht gestartet werden, beispielsweise weil das Zielverzeichnis falsch war. Falls das Sichern
		 *                                    einzelner Dateien fehlschlägt wird keine solche Exception geworfen, stattdessen findet man innerhalb vom callback
		 *                                    eventuelle Fehlschläge und die Funktion gibt false zurück
		 */
		private BackupResult sendConfigAreaBackupTask(final byte[] message, BackupProgressCallback callback)
				throws RequestException, ConfigurationTaskException {
			final int BACKUP_STATE_INITIALIZING = 0;
			final int BACKUP_STATE_IN_PROGRESS = 1;
			final int BACKUP_STATE_FINISHED = 2;

			int requestIndex;
			try {
				requestIndex = _senderConfigAreaTask.sendData("BackupKonfigurationsdaten", message);
			}
			catch(Exception e) {
				throw new RequestException("Konnte Anfrage an Konfiguration nicht senden. Das kann an einem veralteten Datenmodell liegen. Benötigt wird kb.systemModellGlobal in Version 24.", e);
			}
			double fileProgress;
			double totalProgress;
			long total = 0;
			long completed = 0;
			long failed;

			String path = null;

			while(true) {
				final Data data = _senderConfigAreaTask.waitForReply(requestIndex);
				final Deserializer deserializer = getMessageDeserializer2(data, "AntwortBackupKonfigurationsdaten");
				try {
					final int state = deserializer.readInt();

					switch(state) {
						case BACKUP_STATE_INITIALIZING:
							path = deserializer.readString();
							if(callback != null) callback.backupStarted(path);
							break;
						case BACKUP_STATE_IN_PROGRESS:
							completed = deserializer.readLong();
							failed = deserializer.readLong();
							total = deserializer.readLong();
							fileProgress = deserializer.readDouble();
							totalProgress = deserializer.readDouble();
							if(callback != null) callback.backupProgress(completed, failed, total, fileProgress, totalProgress);
							break;
						case BACKUP_STATE_FINISHED:
							completed = deserializer.readLong();
							failed = deserializer.readLong();
							total = deserializer.readLong();
							if(callback != null) {
								callback.backupProgress(completed, failed, total, 1.0, 1.0);
								callback.backupFinished(completed, failed, total);
							}
							return new SimpleBackupResult(completed, failed, path);
					}
				}
				catch(IOException e) {
					throw new RequestException("sendConfigAreaBackupTask: Konnte Antwort nicht deserialisieren", e);
				}
			}
		}

		/**
		 * Erzeugt ein kodiertes Byte-Array, das folgenden Aufbau besitzt:<br> - benutzte Serialisiererversion(Wert ist nicht serialisiert) (ersten 4 Bytes)<br> - Typ
		 * des Pakets (int)<br> - Länge des Zufallstexts (int) - Zufallstext (byte[]) - übergebenes Byte-Array <code>messageCleartext</code>
		 *
		 * @param messageType       Nachrichtentyp
		 * @param serializerVersion Version, mit der die Daten serialisiert werden sollen
		 * @param messageCleartext  Bisher erzeugte Daten, die verschickt werden sollen
		 *
		 * @return verschlüsseltes Byte-Array, das alle oben genannten Daten enthält
		 *
		 * @throws RequestException Alle Fehler die auftauchen werden als RequestException interpretiert. Dies wird gemacht, da eine weitere Übertragung keinen Sinn
		 *                          macht.
		 */
		private byte[] createTelegramByteArray(int messageType, int serializerVersion, byte[] messageCleartext, String encryptionText) throws RequestException {
			try {
				// Zufallstext von der Konfiguration anfordern
				byte[] randomText = getRandomText();

				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(serializerVersion, out);

				serializer.writeInt(messageType);
				serializer.writeInt(randomText.length);
				serializer.writeBytes(randomText);
				// Klartext schreiben. Was ausgelesen werden kann, weiss der Empfänger (longs, ints, ....)
				serializer.writeBytes(messageCleartext);

				final byte[] randomStringAndCleartextMessage = out.toByteArray();

				// Die ersten 4 Bytes enhalten die Serialiszerversion
				final byte[] wholeMessage = new byte[4 + randomStringAndCleartextMessage.length];

				// Das höherwärtigste Byte steht in Zelle 0
				wholeMessage[0] = (byte)((serializerVersion & 0xff000000) >>> 24);
				wholeMessage[1] = (byte)((serializerVersion & 0x00ff0000) >>> 16);
				wholeMessage[2] = (byte)((serializerVersion & 0x0000ff00) >>> 8);
				wholeMessage[3] = (byte)(serializerVersion & 0x000000ff);

				// Array mit Zufallstext kopieren
				System.arraycopy(randomStringAndCleartextMessage, 0, wholeMessage, 4, randomStringAndCleartextMessage.length);

				return EncryptFactory.getEncryptInstance(_encryptDecryptProcedure).encrypt(wholeMessage, encryptionText);
			}
			catch(Exception e) {
				throw new RequestException(e);
			}
		}

		public int getSystemModelVersion() {
			if(_systemModelVersion == 0) {
				_systemModelVersion = _localConfiguration.getConfigurationArea("kb.systemModellGlobal").getActiveVersion();
			}
			return _systemModelVersion;
		}

		private boolean checkSystemModellVersion(final int requiredVersion, final String errorMessage) {
			final boolean versionOk = getSystemModelVersion() >= requiredVersion;
			if(!versionOk) {
				_debug.warning(errorMessage + ". Der Bereich kb.systemModellGlobal wird in Version " + requiredVersion +" oder höher benötigt.");
			}
			return versionOk;
		}

		private void ensureSystemModellVersion(final int requiredVersion, final String errorMessage) throws RequestException {
			final boolean versionOk = getSystemModelVersion() >= requiredVersion;
			if(!versionOk) {
				throw new RequestException(errorMessage + ". Der Bereich kb.systemModellGlobal wird in Version " + requiredVersion +" oder höher benötigt.");
			}
		}
	}
}

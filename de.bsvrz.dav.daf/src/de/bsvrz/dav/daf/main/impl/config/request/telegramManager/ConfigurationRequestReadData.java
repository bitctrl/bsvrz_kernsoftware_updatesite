/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config.request.telegramManager;

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.dav.daf.main.impl.config.request.KindOfUpdateTelegramm;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.UpdateDynamicObjects;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.impl.config.DafMutableSet;
import de.bsvrz.dav.daf.main.impl.config.DafDynamicObject;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Erm�glicht es, Anfragen an die Konfiguration zu stellen. Die Anfragen sind dabei "nur lesend", es werden also keine Daten der Konfiguration ge�ndert.
 * <p/>
 * Das Objekt verwaltet unter anderem auch Anfragen auf dynamische Mengen. Es kann ein Listener angemeldet werden, der benachrichtigt wird, sobald sich eine
 * dynamische Menge �ndert. Die Anmeldung und Verwaltung der Listener wird durch diese Klasse �bernommen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6352 $
 */
public class ConfigurationRequestReadData extends AbstractSenderReceiverCommunication implements SenderReceiverCommunication {

	private final ClientDavInterface _connection;

	private final Aspect _requestAspect;

	private final AttributeGroup _requestAtg;

	private final AttributeGroup _responseAtg;

	private final Aspect _responseAspect;

	private final DataModel _localConfiguration;

	/**
	 * Die Konfiguration verschickt alle �nderungen von Objekten an alle Applikationen. Diese �nderungspakete werden an dieses Objekt weitergereicht.
	 * <p/>
	 * Dieses Objekt h�lt die Objekte aktuell, ist die Variable <code>null</code> so werden die Pakete mit aktuelleren Objekten verworfen.
	 */
	private UpdateDynamicObjects _updateDynamicObjects = null;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten bzgl. �nderungen der Elemente von dynamischen Mengen bzw. dynamischen Typen
	 */
	private MutableCollectionChangeListener _notifyingMutableCollectionChangeListener = null;


	public ConfigurationRequestReadData(
			ClientDavInterface connection, ConfigurationAuthority configurationAuthority, SystemObject localApplication, DataModel localConfiguration
	) throws OneSubscriptionPerSendData {
		super(connection, configurationAuthority, localApplication);
		_connection = connection;
		final DataModel dataModel = connection.getDataModel();

		_requestAspect = dataModel.getAspect("asp.anfrage");
		_requestAtg = dataModel.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleLesend");

		_responseAspect = dataModel.getAspect("asp.antwort");
		_responseAtg = dataModel.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleLesend");

		_localConfiguration = localConfiguration;

		// Es sollen spezielle Telegramme in dieser Klasse verarbeitet werden. Dieser gibt �nderungen
		// an alle weiter, die sich als Listener angemeldet haben.
		AsynchronousAnswerReceiver asynchronousAnswerReceiver = new AsynchronousAnswerReceiver();
		Thread asynchronousAnswerReceiverThread = new Thread(asynchronousAnswerReceiver, "DynamicChangeReceiver");
		asynchronousAnswerReceiverThread.setDaemon(true);
		asynchronousAnswerReceiverThread.start();

		// Sender und Empf�nger anmelden
		init(_requestAtg, _requestAspect, _responseAtg, _responseAspect, asynchronousAnswerReceiver);
	}

	/**
	 * Setz ein Objekt, mit dem dynamische Objekte auf Meta-Seite auf dem aktuellen Stand gehalten werden k�nnen. Wird dieser Setter nicht aufgerufen, so werden
	 * alle Telegramme, die neuere Versionen von Objekten enthalten, verworfen.
	 *
	 * @param updateDynamicObjects Objekt, �ber das dynamische Objekte aktuell gehalten werden
	 */
	public void setDynamicObjectUpdater(final UpdateDynamicObjects updateDynamicObjects) {
		_updateDynamicObjects = updateDynamicObjects;
	}

	private final class AsynchronousAnswerReceiver implements DataListener, Runnable {

		// Speichert alle Telegramme, die nicht automatisch verarbeitet werden sollen, sondern
		// gesondert bearbeitet werden sollen.
		private final UnboundedQueue<Data> _unboundedQueue = new UnboundedQueue<Data>();

		public boolean messageReceived(Data data) {
			final String messageType = data.getTextValue("nachrichtenTyp").getValueText();
			// Was f�r eine Nachricht ist angekommen ?
			if(("DynamischeMengeAktualisierung".equals(messageType))
			   || ("Objektaktualisierung").equals(messageType)
			   || ("DynamischeKollektionAktualisierung").equals(messageType)
			   || ("KommunikationszustandAktualisierung").equals(messageType)
					) {

				_unboundedQueue.put(data);

				// Die Nachricht wurde bereits hier verarbeitet, also braucht es nicht weiter verarbeitet werden
				return false;
			}
			else {
				// Das Telegramm kann ganz normal weiterverarbeitet werden
				return true;
			}
		}

		public void close() {
			// Beendet den Thread, der auf der Queue wartet
			_unboundedQueue.put(null);
		}

		/**
		 * Aktualisiert die entsprechende dynamische Menge.
		 *
		 * @param data Das �bermittelte Data von der Konfiguration.
		 */
		private void actualizeMutableSet(Data data) {
			try {
				Deserializer deserializer = getMessageDeserializer(data, "DynamischeMengeAktualisierung");
				// MutableSet ermitteln
				DafMutableSet set = (DafMutableSet)deserializer.readObjectReference(_localConfiguration);
				int numberOfAddedElements = deserializer.readInt();
				SystemObject[] addedElements = new SystemObject[numberOfAddedElements];
				for(int i = 0; i < numberOfAddedElements; i++) {
					addedElements[i] = deserializer.readObjectReference(_localConfiguration);
				}
				int numberOfRemovedElements = deserializer.readInt();
				SystemObject[] removedElements = new SystemObject[numberOfRemovedElements];
				for(int i = 0; i < numberOfRemovedElements; i++) {
					removedElements[i] = deserializer.readObjectReference(_localConfiguration);
				}
				// Aktualisierung weiterleiten
				set.update(addedElements, removedElements);
			}
			catch(Exception ex) {
				throw new RuntimeException("Empfangenes Datenformat ist nicht das erwartete", ex);
			}
		}

		/**
		 * Aktualisiert die Elemente einer dynamischen Menge oder eines dynamischen Typs.
		 *
		 * @param data Das �bermittelte Data von der Konfiguration.
		 */
		private void actualizeMutableCollection(Data data) {
			try {
				Deserializer deserializer = getMessageDeserializer(data, "DynamischeKollektionAktualisierung");
				final SystemObject systemObject = deserializer.readObjectReference(_localConfiguration);
				final MutableCollection mutableCollection = ((MutableCollection)systemObject);
				short simVariant = deserializer.readShort();
				int numberOfAddedElements = deserializer.readInt();

				ArrayList<SystemObject> addedElements = new ArrayList<SystemObject>(numberOfAddedElements);
				for(int i = 0; i < numberOfAddedElements; i++) {
					final SystemObject addedElement = deserializer.readObjectReference(_localConfiguration);
					addedElements.add(addedElement);
				}
				int numberOfRemovedElements = deserializer.readInt();

				ArrayList<SystemObject> removedElements = new ArrayList<SystemObject>(numberOfRemovedElements);
				for(int i = 0; i < numberOfRemovedElements; i++) {
					final SystemObject removedElement = deserializer.readObjectReference(_localConfiguration);
					removedElements.add(removedElement);
				}
				// Aktualisierung weiterleiten
				if(_notifyingMutableCollectionChangeListener != null) {
					_notifyingMutableCollectionChangeListener.collectionChanged(mutableCollection, simVariant, addedElements, removedElements);				
				}
				else {
					_debug.warning("Aktualisierung einer dynamischen Kollektion kann nicht weitergeleitet werden");
				}
			}
			catch(Exception ex) {
				throw new RuntimeException("Empfangenes Datenformat ist nicht das erwartete", ex);
			}
		}

		/**
		 * Aktualisiert den Kommunikationszustand f�r fremdverwaltete dynamische Mengen und Objekte.
		 *
		 * @param data Das �bermittelte Data von der Konfiguration.
		 */
		private void actualizeConfigurationCommunicationState(Data data) {
			try {
				Deserializer deserializer = getMessageDeserializer(data, "KommunikationszustandAktualisierung");
				final SystemObject systemObject = deserializer.readObjectReference(_localConfiguration);
				final boolean communicationState = deserializer.readBoolean();
				if(systemObject instanceof DafMutableSet) {
					DafMutableSet dafMutableSet = (DafMutableSet)systemObject;
					dafMutableSet.configurationCommunicationChange(communicationState);
				}
				else if(systemObject instanceof DafDynamicObject) {
					DafDynamicObject dafDynamicObject = (DafDynamicObject)systemObject;
					dafDynamicObject.configurationCommunicationChange(communicationState);
				}
				else {
					_debug.warning("KommunikationszustandAktualisierung f�r " + systemObject + " kann nicht verarbeitet werden");
				}
			}
			catch(Exception ex) {
				throw new RuntimeException("Empfangenes Datenformat ist nicht das erwartete", ex);
			}
		}

		/**
		 * Ein Objekt wurde auf Seiten der Konfiguration ver�ndert und die Konfiguration benachrichtigt alle Applikationen. Die Applikationen m�ssen nun ihre Daten
		 * auf den neusten Stand bringen.
		 *
		 * @param data Aktuelle Daten f�r ein Objekt
		 */
		private void actualizeObject(final Data data) {
			// Falls es ein Objekt zum aktualisieren von Objekten gibt wird das Telegramm verarbeitet, sonst wird es verworfen.
			if(_updateDynamicObjects != null) {
				try {
					final Deserializer deserializer = getMessageDeserializer(data, "Objektaktualisierung");
					final KindOfUpdateTelegramm telegramType = KindOfUpdateTelegramm.getInstance(deserializer.readByte());

					if(telegramType == KindOfUpdateTelegramm.UPDATE_NAME) {

						// Ein Telegramm, das den Namen eines Objekts aktualisiert, hat folgenden Aufbau:
						// 1) Id des Objekt, dessen Name ge�ndert werden soll (long)
						// 2) Id des Typs von dem das Objekt ist (long)
						// 3) Der neue Name (String)
						final long objectId = deserializer.readLong();
						final long objectTypeId = deserializer.readLong();
						final String newName = deserializer.readString();

						_updateDynamicObjects.updateName(objectId, objectTypeId, newName);
					}
					else if(telegramType == KindOfUpdateTelegramm.UPDATE_NOT_VALID_SINCE) {

						// Ein Telegramm, das den Zeitpunkt/Version eines Objekts setzt ab dem es nicht mehr g�ltig ist, besitzt folgenden Aufbau.
						// 1) Id des Objekt, dessen Version/Zeitpunkt ge�ndert werden soll (long)
						// 2) Id des Typs von dem das Objekt ist (long)
						// 3) Konfiguration oder dynamisches Objekt (byte, 0 = Konfigurationsobjekt)
						// Der n�chste Wert ist abh�ngig von 3), ist es ein Konfigurationsobjekt, so muss ein short gelesen werden
						// 4a) Version, ab der das Objekt ung�ltig werden wird, short
						// 4b) Zeitpunkt, ab dem das Objekt ung�ltig geworden ist, long

						final long objectId = deserializer.readLong();
						final long objectTypeId = deserializer.readLong();
						final byte kindOfObject = deserializer.readByte();

						if(kindOfObject == 1) {
							// Es handelt sich um ein dynamisches Objekt
							final long notValidSince = deserializer.readLong();
							_updateDynamicObjects.updateNotValidSince(objectId, objectTypeId, notValidSince);
						}
						else {
							// Konfigurationsobjekte werden noch nicht unterst�tzt
						}
					}
					else if(telegramType == KindOfUpdateTelegramm.CREATED) {
						// Ein neues dynamisches Objekt wurde erzeugt. Das Telegramm besitzt folgenden Aufbau
						// 1) Id des Objekts, das erzeugt wurde (long)
						// 2) Id des Typs des Objekts (long)

						final long objectId = deserializer.readLong();
						final long objectTypeId = deserializer.readLong();

						_updateDynamicObjects.newDynamicObjectCreated(objectId, objectTypeId);
					}
				}
				catch(Exception e) {
					final String errorText = "Telegramm zum Aktualisieren von Objekten konnten nicht verarbeitet werden";
					e.printStackTrace();
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}

		private Deserializer getMessageDeserializer(Data reply, String expectedMessageType) throws RequestException {
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

		public void run() {
			_debug.fine(
					"UpdateRunnable wird gestartet f�r " + _responseAspect + " " + _responseAtg
					+ " Nachrichtentyp: DynamischeMengeAktualisierung/ObjektAktualisierung"
			);
			while(true) {
				try {
					Data data = _unboundedQueue.take();
					if(data == null) {
						_debug.fine("UpdateRunnable wird gestoppt " + _responseAspect + " " + _responseAtg);
						return;
					}
					// Pr�fen, was benachrichtigt werden muss
					final String messageType = data.getTextValue("nachrichtenTyp").getValueText();
					if("DynamischeMengeAktualisierung".equals(messageType)) {
						actualizeMutableSet(data);
					}
					else if("Objektaktualisierung".equals(messageType)) {
						actualizeObject(data);
					}
					else if("DynamischeKollektionAktualisierung".equals(messageType)) {
						actualizeMutableCollection(data);
					}
					else if("KommunikationszustandAktualisierung".equals(messageType)) {
						actualizeConfigurationCommunicationState(data);
					}
					else {
						throw new IllegalArgumentException("Unbekannten Auftrag empfangen");
					}
				}
				catch(InterruptedException ex) {
					_debug.error(
							"UpdateThread im RemoteRequestManager wurde unterbrochen " + _responseAspect + " " + _responseAtg
							+ " Nachrichtentyp: DynamischeMengeAktualisierung", ex
					);
				}
				catch(RuntimeException ignore) {
					_debug.warning("Unerwarteter Fehler bei der Verarbeitung von asynchronen Konfiguationsantworten", ignore);
				}
			}
		}
	}

	public void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener) {
		_notifyingMutableCollectionChangeListener = notifyingMutableCollectionChangeListener;
	}
}

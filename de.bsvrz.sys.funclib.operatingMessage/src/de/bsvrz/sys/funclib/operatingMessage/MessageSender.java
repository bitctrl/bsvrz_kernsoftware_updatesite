/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.sys.funclib.operatingMessage;

import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.AttributeGroup;

/**
 * Diese Klasse dient zur Erzeugung von Betriebsmeldungen. Diese Klasse ist als Singleton erstellt. Somit wird nur ein
 * Objekt dieser Klasse angelegt. Durch die {@link #init init-Methode} wird die Verbindung zum Datenverteiler und die
 * Kennung der Applikation an das Sender-Objekt übergeben. Mit Hilfe der verschiedenen {@link #sendMessage
 * sendMessage-Methoden} können Betriebsmeldungen an die Betriebsmeldungsverwaltung abgesetzt werden.<p> Existiert noch
 * keine Verbindung zum Datenverteiler beim Senden einer Betriebsmeldung, so wird eine <i>Warnung</i> zurückgegeben.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5058 $
 * @see #init
 * @see #sendMessage
 */
public class MessageSender {
	/**
	 * Das Singleton wird in jedem Fall erzeugt. Sollte jedoch die Verbindung zum Datenverteiler noch nicht vorhanden sein,
	 * wird bei {@link #sendMessage} eine Warnung ausgegeben!
	 */
	private static MessageSender INSTANCE = new MessageSender();

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static Debug _debug;

	/**
	 * Die Verbindung zum Datenverteiler.
	 */
	private ClientDavInterface _connection = null;

	/**
	 * Das Sende-Objekt für den Versand von Betriebsmeldungen.
	 */
	private OperatingMessageSender _operatingMessageSender;

	/**
	 * Dieses Systemobjekt muss vom typ.betriebsMeldungsVerwaltung sein und wird zum Versenden der Betriebsmeldungen
	 * eingesetzt.
	 */
	private SystemObject _messageObject;

	/**
	 * Die DataDescription zum Senden von Betriebsmeldungen.
	 */
	private DataDescription _dataDescriptionSender;


	/* ############### Attribute der Attributgruppe atg.betriebsMeldung ################### */
	/**
	 * ID der Applikation, die die Informationsmeldung erzeugt hat.
	 */
	private long _applicationID;

	/**
	 * LaufendeNummer: Laufende Nummerierung der durch die Applikation seit dem Applikationsstart erzeugten
	 * Informationsmeldungen. Aus der Kombination von ApplikationsID und LaufendeNummer lässt sich prüfen, ob Meldungen
	 * verloren wurden.
	 */
	private long _consecutiveNumber;

	/**
	 * Typ der Applikation, die die Informationsmeldung erzeugt hat.
	 */
	private SystemObjectType _applicationType;

	/**
	 * Für jede Applikation eindeutige Kennung, die nach einem Neustart gleich bleibt.
	 */
	private String _applicationLabel;

	/**
	 * Die Urlasserinformation, falls keine übergeben wird. Sie besteht aus dem lokalen Benutzer, der sich beim
	 * Datenverteiler angemeldet hat und ohne die Angabe einer Ursache und eines Veranlassers.
	 */
	private MessageCauser _causer;

	/**
	 * Speichert den Klassennamen der Applikation
	 */
	private String _rootName;


	/* ################# Methoden ################# */
	/**
	 * Privater Konstruktor erzeugt ein leeres Objekt dieser Klasse.
	 */
	private MessageSender() {
		_debug = Debug.getLogger();
		_debug.fine("Singleton wurde erstellt", this.getClass().getName());
	}

	/**
	 * Gibt die Instanz dieser Klasse zurück. Ein neues Objekt wird erstellt, falls noch keines vorhanden ist.
	 *
	 * @return Objekt dieser Klasse
	 */
	public static MessageSender getInstance() {
		return INSTANCE;
	}

	/**
	 * Initialisiert das Objekt indem die Verbindung zum Datenverteiler und die Kennung der Applikation übergeben wird.
	 *
	 * @param connection       Verbindung zum Datenverteiler
	 * @param applicationName  Name der Applikation
	 * @param applicationLabel eindeutige Kennung der Applikation
	 */
	public void init(final ClientDavInterface connection, String applicationName, final String applicationLabel) {
		try {
			_messageObject = connection.getLocalConfigurationAuthority();
			if (_messageObject == null || !_messageObject.isOfType("typ.betriebsMeldungsVerwaltung")) {
				_debug.warning("Der lokale Konfigurationsverantworliche besitzt nicht den Typ: BetriebsMeldungsVerwaltung. Betriebsmeldungen können nicht verschickt werden!");
				_messageObject = null;
				return;
			}
			_debug.fine("Zum Senden wird folgendes BetriebsMeldungsVerwaltungsObjekt verwendet", _messageObject);
			_connection = connection;
			final ClientApplication application = _connection.getLocalApplicationObject();

			// Werte der Applikation speichern
			_applicationID = application.getId();
			_consecutiveNumber = 0;
			_applicationType = application.getType();
			_applicationLabel = applicationLabel;
			_rootName = applicationName;

			// Standard-Urlasser, falls keiner übergeben wurde
			_causer = new MessageCauser(_connection.getLocalUser(), "", "");

			// Datenidentifikation ermitteln und Anmeldung als Sender
			final DataModel configuration = _connection.getDataModel();
			final AttributeGroup atg = configuration.getAttributeGroup("atg.betriebsMeldung");
			final Aspect asp = configuration.getAspect("asp.information");
			_dataDescriptionSender = new DataDescription(atg, asp);
			_operatingMessageSender = new OperatingMessageSender();
			_connection.subscribeSender(_operatingMessageSender, _messageObject, _dataDescriptionSender, SenderRole.sender());
			_debug.config("Der MessageSender wurde initialisiert und ist bereit zum Senden von Betriebsmeldungen.");
		} catch (ConfigurationException ex) {
			_debug.error("Fehler", ex);
			throw new RuntimeException(ex);
		} catch (OneSubscriptionPerSendData oneSubscriptionPerSendData) {
			_debug.error("Fehler", oneSubscriptionPerSendData);
			oneSubscriptionPerSendData.printStackTrace();
		}
	}

	/**
	 * Die einfachste Version eine Betriebsmeldung zu verschicken. Die ID bleibt leer, es wird kein KonfigurationsObjekt
	 * angegeben, es handelt sich um eine neue Meldung und als Urlasser wird der angemeldete Benutzer mit leerer Ursache
	 * und Veranlasser verschickt.
	 *
	 * @param type    der Meldungstyp
	 * @param grade   die Meldungsklasse
	 * @param message Text der Meldung
	 */
	public void sendMessage(MessageType type, MessageGrade grade, String message) {
		sendMessage("", type, getCallPosition(new Throwable()), grade, MessageState.MESSAGE, message);
	}

	/**
	 * Bei dieser Betriebsmeldung wird neben dem MeldungsTyp, der MeldungsKlasse und des Meldungstextes noch eine ID und ob
	 * es sich hierbei um eine GutMeldung handelt, übergeben.
	 *
	 * @param id               ID der Meldung. Dieses Attribut kann von der Applikation gesetzt werden, um einen Bezug zu
	 *                         einer vorherigen Meldung herzustellen.
	 * @param type             der MeldungsTyp
	 * @param messageTypeAddOn der MeldungsTypZusatz
	 * @param grade            die MeldungsKlasse
	 * @param state            Gibt den Zustand einer Meldung an.
	 * @param message          Text der Meldung
	 */
	public void sendMessage(String id, MessageType type, String messageTypeAddOn, MessageGrade grade, MessageState state, String message) {
		if (messageTypeAddOn == null || messageTypeAddOn.equals("")) messageTypeAddOn = getCallPosition(new Throwable());
		sendMessage(id, type, messageTypeAddOn, grade, null, state, _causer, message);
	}

	/**
	 * Hierbei handelt es sich um eine Betriebsmeldung, wo nur die ID und die GutMeldung fehlt.
	 *
	 * @param type             der MeldungsTyp
	 * @param messageTypeAddOn der MeldungsTypZusatz
	 * @param grade            die MeldungsKlasse
	 * @param referenceObject  Referenz auf ein beliebiges Konfigurationsobjekt, auf das sich die Meldung bezieht.
	 * @param causer           Urlasserinformation (Referenz auf den Benutzer, der die Meldung erzeugt hat, Angabe einer
	 *                         Ursache für die Meldung und der Veranlasser für die Meldung)
	 * @param message          Text der Meldung
	 */
	public void sendMessage(MessageType type, String messageTypeAddOn, MessageGrade grade, SystemObject referenceObject, MessageCauser causer, String message) {
		if (messageTypeAddOn == null || messageTypeAddOn.equals("")) messageTypeAddOn = getCallPosition(new Throwable());
		sendMessage("", type, messageTypeAddOn, grade, referenceObject, MessageState.MESSAGE, causer, message);
	}

	/**
	 * Die vollständige Betriebsmeldung. Sie enthält alle Parameter, die für eine Betriebsmeldung in Frage kommen können.
	 *
	 * @param id               ID der Meldung. Dieses Attribut kann von der Applikation gesetzt werden, um einen Bezug zu
	 *                         einer vorherigen Meldung herzustellen.
	 * @param type             der MeldungsTyp
	 * @param messageTypeAddOn der MeldungsTypZusatz
	 * @param grade            die MeldungsKlasse
	 * @param referenceObject  Referenz auf ein beliebiges Konfigurationsobjekt, auf das sich die Meldung bezieht.
	 * @param state            Gibt den Zustand einer Meldung an.
	 * @param causer           Urlasserinformation (Referenz auf den Benutzer, der die Meldung erzeugt hat, Angabe einer
	 *                         Ursache für die Meldung und der Veranlasser für die Meldung)
	 * @param message          Text der Meldung
	 */
	public void sendMessage(String id, MessageType type, String messageTypeAddOn, MessageGrade grade, SystemObject referenceObject, MessageState state, MessageCauser causer, String message) {
		if (_messageObject == null) {
			final String errorMessage = "Betriebsmeldungen können nicht verschickt werden. Die Initialisierung ist nicht erfolgt oder war fehlerhaft. Es gibt kein passendes BetriebsMeldungsVerwaltungs-Objekt. Evtl. besitzt der lokale Konfigurationsverantwortliche den Typ Betriebsmeldungsverwaltung nicht.";
			_debug.warning(errorMessage);
			return;

		}
		if (messageTypeAddOn == null || messageTypeAddOn.equals("")) messageTypeAddOn = getCallPosition(new Throwable());
		// versenden der Betriebsmeldung
		final Data data = _connection.createData(_dataDescriptionSender.getAttributeGroup());
		// erst die Werte, die durch die Initialisierung vorgegeben sind
		data.getUnscaledValue("ApplikationsID").set(_applicationID);
		data.getUnscaledValue("LaufendeNummer").set(nextConsecutiveNumber());
		data.getReferenceValue("ApplikationsTyp").setSystemObject(_applicationType);
		data.getTextValue("ApplikationsKennung").setText(_applicationLabel);

		// Werte, die durch sendMessage übergeben wurden
		data.getTextValue("ID").setText(id);
		data.getTextValue("MeldungsTyp").setText(type.getMessageType());
		data.getTextValue("MeldungsTypZusatz").setText(messageTypeAddOn);
		data.getTextValue("MeldungsKlasse").setText(grade.getGrade());
		if (referenceObject == null)
			data.getReferenceArray("Referenz").setLength(0);
		else {
			data.getReferenceArray("Referenz").setLength(1);
			data.getReferenceArray("Referenz").getReferenceValue(0).setSystemObject(referenceObject);
		}
//		data.getTextValue("GutMeldung").setText((goodNews ? "Ja" : "Nein"));
		data.getTextValue("Status").setText(state.getState());
		// der Urlasser benötigt eine Extrabehandlung
		final Data causerData = data.getItem("Urlasser");
		causerData.getReferenceValue("BenutzerReferenz").setSystemObject(causer.getUser());
		causerData.getTextValue("Ursache").setText(causer.getCause());
		causerData.getTextValue("Veranlasser").setText(causer.getCauser());

		data.getTextValue("MeldungsText").setText(message);

		if (_operatingMessageSender.getState() == 0) {
			try {
				final ResultData resultData = new ResultData(_messageObject, _dataDescriptionSender, System.currentTimeMillis(), data);
				_connection.sendData(resultData);
			} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
				_debug.error("Fehler", sendSubscriptionNotConfirmed);
				sendSubscriptionNotConfirmed.printStackTrace();
			} catch (ConfigurationException ex) {
				_debug.error("Fehler", ex);
				throw new RuntimeException(ex);
			}
		} else {
			_debug.warning("Senden ist nicht möglich, da die Sendesteuerung noch keine Sendeerlaubnis gibt! Evtl. fehlt die Betriebsmeldungsverwaltung. " +
					"Folgende Meldung wurde nicht verschickt", data);
		}
	}

	/**
	 * Bestimmt zur Betriebsmeldung die Position im Code, an der die Betriebsmeldung rausgeschickt wurde. Angegeben wird
	 * der Klassenname, der Methodenname und die Zeilennummer.
	 *
	 * @return die Aufrufposition der Betriebsmeldung
	 */
	private String getCallPosition(Throwable t) {
		StringBuilder callPosition = new StringBuilder();
		if (t.getStackTrace().length > 1) {
			StackTraceElement traceElement = t.getStackTrace()[1];
			callPosition.append(_rootName);
			callPosition.append(" - ").append(traceElement.getClassName());
			callPosition.append(".").append(traceElement.getMethodName());
			callPosition.append("(").append(traceElement.getFileName());
			callPosition.append(": ").append(traceElement.getLineNumber());
			callPosition.append(")");
		} else {
			callPosition.append("Aufrufposition nicht ermittelbar!");
		}
		return callPosition.toString();
	}

	/**
	 * Gibt die laufende Nummer zurück und erhöht sie um eins.
	 *
	 * @return die laufende Nummer
	 */
	private long nextConsecutiveNumber() {
		return _consecutiveNumber++;
	}

	/**
	 * Die ApplikationsKennung kann mit dieser Methode nachträglich gesetzt werden.
	 *
	 * @param applicationLabel ApplikationsKennung - eindeutige Kennung dieser Applikation
	 */
	public void setApplicationLabel(String applicationLabel) {
		_applicationLabel = applicationLabel;
	}


	/**
	 * Diese Klasse fragt den aktuellen Zustand der Sendesteuerung ab. Durch Abfrage der Methode {@link #getState()} kann
	 * ermittelt werden, ob eine Betriebsmeldung an die Betriebsmeldungsverwaltung geschickt werden kann, oder nicht.
	 */
	private final class OperatingMessageSender implements ClientSenderInterface {
		private byte _state = 1;

		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			_debug.finest("Änderung der Sendesteuerung", state);
			_state = state;
		}

		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}

		public byte getState() {
			return _state;
		}
	}
}

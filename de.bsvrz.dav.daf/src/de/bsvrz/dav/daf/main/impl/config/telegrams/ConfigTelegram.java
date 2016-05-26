/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import de.bsvrz.dav.daf.main.impl.config.DafDataModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein allgemeines Konfigurationstelegramm dar. Hier werden die weiteren Typen über die Klassenvariablen definiert. Zu jedem "Request", also
 * Anfrage Telegramm, gibt es ein Answer Telegramm.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class ConfigTelegram {

	/** Metadaten-Anfragetelegramm */
	public static final byte META_DATA_REQUEST_TYPE = 1;

	/** generelles Anfragetelegramm */
	public static final byte OBJECT_REQUEST_TYPE = 2;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte VALUE_REQUEST_TYPE = 3;

	/** NewObject-Anfragetelegramm */
	public static final byte NEW_OBJECT_REQUEST_TYPE = 4;

	/** Anfragetelegramm zur Ungültigkeitserklärung */
	public static final byte OBJECT_INVALIDATE_REQUEST_TYPE = 5;

	/** Anfragetelegramm zur Wiedergültigkeitserklärung */
	public static final byte OBJECT_REVALIDATE_REQUEST_TYPE = 6;

	/** Anfragetelegramm  zu Namensänderung */
	public static final byte OBJECT_SET_NAME_REQUEST_TYPE = 7;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte OBJECT_SET_PID_REQUEST_TYPE = 8;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_CHANGES_REQUEST_TYPE = 9;

	/** Anfrage zu Authentifizierung des Benutzers */
	public static final byte AUTHENTIFICATION_REQUEST_TYPE = 10;

	/** Anfragetelegramm zu den Verbindungsinformationen */
	public static final byte TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE = 11;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte CODE_TO_ASPECT_REQUEST_TYPE = 12;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte CODE_TO_ATTRIBUTE_GROUP_REQUEST_TYPE = 13;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte ARCHIVE_REQUEST_TYPE = 14;

	/** Metadaten-Antworttelegramm */
	public static final byte META_DATA_ANSWER_TYPE = 21;

	/** generelles Antworttelegramm */
	public static final byte OBJECT_ANSWER_TYPE = 22;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte VALUE_ANSWER_TYPE = 23;

	/** NewObject-Antworttelegramm */
	public static final byte NEW_OBJECT_ANSWER_TYPE = 24;

	/** Antworttelegramm zur Ungültigkeitserklärung */
	public static final byte OBJECT_INVALIDATE_ANSWER_TYPE = 25;

	/** Antworttelegramm zur Gültigkeitserklärung */
	public static final byte OBJECT_REVALIDATE_ANSWER_TYPE = 26;

	/** Antworttelegramm zur Namesänderung */
	public static final byte OBJECT_SET_NAME_ANSWER_TYPE = 27;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte OBJECT_SET_PID_ANSWER_TYPE = 28;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_CHANGES_ANSWER_TYPE = 29;

	/** Authentifikationsantwort Telegramm */
	public static final byte AUTHENTIFICATION_ANSWER_TYPE = 30;

	/** Antworttelegramm zu den Verbindungsinformationen */
	public static final byte TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE = 31;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte CODE_TO_ASPECT_ANSWER_TYPE = 32;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte CODE_TO_ATTRIBUTE_GROUP_ANSWER_TYPE = 33;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte ARCHIVE_ANSWER_TYPE = 34;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte VALUE_REQUEST_TYPE2 = 35;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_CONFIG_DATA_REQUEST_TYPE = 36;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_CONFIG_DATA_ANSWER_TYPE = 37;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte MUTABLE_CHANGES_TYPE = 100;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte MUTABLE_CHANGES_SUBSCRIPTION_TYPE = 101;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte MUTABLE_CHANGES_UNSUBSCRIPTION_TYPE = 102;

	/** Der Typ dieses Konfigurationstelegrams */
	protected byte _type;

	/** Zusätzliche Informationen dieses Telegrams */
	private String _info = "";

	/**
	 * Erzeugt ein neues Configurationstelegramm ohne Parameter
	 */
	public ConfigTelegram() {
	}

	/** @return Typ des Telegramms */
	public final byte getType() {
		return _type;
	}

	/**
	 * Setzt den Typ des telegramms auf <code>type</code>
	 *
	 * @param type Typ des Telegramms
	 */
	public void setType(byte type) {
		_type = type;
	}

	/**
	 * Gibt die zusätzlichen Informationen des Telegramms zurück
	 *
	 * @return zusätzliche Informationen des Telegrams
	 */
	public final String getInfo() {
		return _info;
	}

	/**
	 * Setzt die zusätzliche Informationen des Telegramms
	 *
	 * @param info zusätzliche Informationen des Telegramms
	 */
	public final void setInfo(String info) {
		if(info != null) {
			_info = info;
		}
	}

	/**
	 * Erzeugt ein neues Objekt des übergebenen Typs und gibt dieses zurück.
	 *
	 * @param telegramType Typ des zu erzeugenden Telegramms
	 * @param dataModel    Applikationsseitige Implementierung der DataModel Schnittstelle
	 *
	 * @return Konfigurationstelegramm vom Typ <code>telegramType</code>
	 */
	public static ConfigTelegram getTelegram(byte telegramType, DafDataModel dataModel) {
		switch(telegramType) {
			case(META_DATA_REQUEST_TYPE): {
				return new MetaDataRequest();
			}
			case(OBJECT_REQUEST_TYPE): {
				return new SystemObjectsRequest();
			}
			case(NEW_OBJECT_REQUEST_TYPE): {
				return new NewObjectRequest();
			}
			case(OBJECT_INVALIDATE_REQUEST_TYPE): {
				return new ObjectInvalidateRequest();
			}
			case(OBJECT_REVALIDATE_REQUEST_TYPE): {
				return new ObjectRevalidateRequest();
			}
			case(OBJECT_SET_NAME_REQUEST_TYPE): {
				return new ObjectSetNameRequest();
			}
			case(AUTHENTIFICATION_REQUEST_TYPE): {
				return new AuthentificationRequest();
			}
			case(TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE): {
				return new TransmitterConnectionInfoRequest();
			}
			case(META_DATA_ANSWER_TYPE): {
				return new MetaDataAnswer(dataModel);
			}
			case(OBJECT_ANSWER_TYPE): {
				return new SystemObjectAnswer(dataModel);
			}
			case(NEW_OBJECT_ANSWER_TYPE): {
				return new NewObjectAnswer(dataModel);
			}
			case(OBJECT_INVALIDATE_ANSWER_TYPE): {
				return new ObjectInvalidateAnswer();
			}
			case(OBJECT_REVALIDATE_ANSWER_TYPE): {
				return new ObjectRevalidateAnswer();
			}
			case(OBJECT_SET_NAME_ANSWER_TYPE): {
				return new ObjectSetNameAnswer();
			}
			case(AUTHENTIFICATION_ANSWER_TYPE): {
				return new AuthentificationAnswer();
			}
			case(TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE): {
				return new TransmitterConnectionInfoAnswer();
			}
		}
		return null;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke zurück.
	 *
	 * @return Beschreibender Text dieses Objekts.
	 */
	public abstract String parseToString();

	/**
	 * Deserialisiert dieses Objekt.
	 *
	 * @param in Stream von dem das Objekt gelesen werden soll.
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void read(DataInputStream in) throws IOException;

	/**
	 * Serialisiert dieses Objekt.
	 *
	 * @param out Stream auf den das Objekt geschrieben werden soll.
	 *
	 * @throws IOException, wenn beim Schreiben auf den Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void write(DataOutputStream out) throws IOException;
}

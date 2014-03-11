/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.losb.exceptions.FailureException;
import de.bsvrz.sys.funclib.losb.exceptions.LoggerException;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;

import java.io.Serializable;

/**
 * Attributgruppe {@link de.bsvrz.sys.funclib.losb.datk.PidScript#atgAnswer}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 11423 $ / $Date: 2013-07-22 12:02:01 +0200 (Mo, 22 Jul 2013) $ / ($Author: jh $)
 */
public class AtgProtocolRequest {

	/** Auftraggeber. */
	public SystemObject client;

	/** Anfrage-Id. */
	public long requestId;

	/** Operationscode. */
	public int opCode;

	/** Auftragsdaten. */
	public byte[] requestData;

	/**
	 * Erzeugt ein Data Objekt für die Attributgruppe
	 *
	 * @param dav         Verbindung zum Datenverteiler
	 * @param sender      Absender
	 * @param protocolId  Protokoll-Id
	 * @param opCode      Operationscode
	 * @param dataRequest Datenteil der Anfrage. Darf <code>null</code> sein. In diesem Fall wird das Feld auf die Länge <code> gesetzt.
	 *
	 * @throws FailureException Falls es zu einem Kommunikationsfehler kommt
	 * @return Data Objekt
	 */
	public static Data build(ClientDavInterface dav, SystemObject sender, long protocolId, int opCode, byte[] dataRequest) throws FailureException {
		Data data = setData(dav, sender, protocolId, opCode);
		data.getUnscaledArray(PidProtocol.data).set(dataRequest);
		return data;
	}


	/**
	 * Erzeugt ein Data Objekt für die Attributgruppe
	 *
	 * @param dav        Verbindung zum Datenverteiler
	 * @param sender     Absender
	 * @param protocolId Protokoll-Id
	 * @param opCode     Operationscode
	 *
	 * @throws FailureException Falls es zu einem Kommunikationsfehler kommt
	 * @return Data Objekt
	 */
	private static Data setData(ClientDavInterface dav, SystemObject sender, long protocolId, int opCode) throws FailureException {
		Data data;
		try {
			data = dav.createData(dav.getDataModel().getAttributeGroup(PidProtocol.atgProtocolRequest));
		}
		catch(ConfigurationException e) {
			throw new FailureException(ErrorMessage.COMMUNICATION, e, LoggerException.ERROR);
		}

		data.getReferenceValue(PidProtocol.sender).setSystemObject(sender);
		data.getUnscaledValue(PidProtocol.protocolId).set(protocolId);
		data.getUnscaledValue(PidProtocol.opCode).set(opCode);
		return data;
	}

	/**
	 * Erzeugt ein Data Objekt für die Attributgruppe. Diese Methode ist nicht Threadsafe für das <code>serializer</code> Objekt. Wird es in mehreren Threads
	 * verwendet, so muss der Aufruf dieser Methode synchronisiert werden.
	 *
	 * @param dav                Verbindung zum Datenverteiler
	 * @param sender             Absender
	 * @param protocolId         Protokoll-Id
	 * @param opCode             Operationscode
	 * @param serializer         Serialisierer
	 * @param serializableObject Objekt das serialisiert und gepackt in den Datenteil der Anfrage geschrieben wird.
	 *
	 * @throws FailureException Falls es zu einem Kommunikationsfehler kommt
	 * @return Data Objekt
	 * @see SerializerUtil#serializeToByteArray(Serializable)
	 */
	public static Data build(
			ClientDavInterface dav, SystemObject sender, long protocolId, int opCode, SerializerUtil serializer, Serializable serializableObject)
			throws FailureException {
		Data data = setData(dav, sender, protocolId, opCode);
		serializer.serializeIntoDataArray(data.getArray(PidProtocol.data), serializableObject);
		return data;
	}

	/**
	 * Erzeugt ein Data Objekt für die Attributgruppe
	 *
	 * @param dav        Verbindung zum Datenverteiler
	 * @param sender     Absender
	 * @param protocolId Protokoll-Id
	 * @param opCode     Operationscode
	 * @param message    Nachricht die serialisiert in den Datenteil der Anfrage geschrieben wird
	 *
	 * @throws FailureException Falls es zu einem Kommunikationsfehler kommt oder die Daten nicht serialisiert werden konnten
	 * @return Data Objekt
	 */
	public static Data build(ClientDavInterface dav, SystemObject sender, long protocolId, int opCode, String message) throws FailureException {
		return build(dav, sender, protocolId, opCode, SerializerUtil.serializeToByteArray(message));
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtgProtocolRequest getJavaObject(Data data) {
		AtgProtocolRequest result = new AtgProtocolRequest();

		result.client = data.getReferenceValue(PidProtocol.sender).getSystemObject();
		result.requestId = data.getUnscaledValue(PidProtocol.protocolId).longValue();
		result.opCode = data.getUnscaledValue(PidProtocol.opCode).intValue();
		result.requestData = data.getUnscaledArray(PidProtocol.data).getByteArray();

		return result;
	}
}

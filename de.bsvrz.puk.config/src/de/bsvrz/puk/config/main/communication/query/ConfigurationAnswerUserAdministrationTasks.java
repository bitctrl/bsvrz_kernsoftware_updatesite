/*
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
package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.AbstractSenderReceiverCommunication;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.SenderReceiverCommunication;

/**
 * Klasse die eine Antwort der Konfiguration auf einen Auftrag zur Benutzerverwaltung zur�ckgibt. Die Klasse meldet
 * sich nur als Sender an.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision: 5079 $ / $Date: 2007-09-02 14:59:08 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationAnswerUserAdministrationTasks extends AbstractSenderReceiverCommunication implements SenderReceiverCommunication {
		private final ClientDavInterface _connection;
	private final Aspect _requestAspect;
	private final AttributeGroup _requestAtg;

	/**
	 * @param connection
	 * @param senderObject  Objekt, zu dem die Daten verschickt werden sollen (wird zum anmelden des Senders gebraucht)
	 * @param ordererObject Objekt von dem die Daten geschickt werden (wird als Absender eingetragen)
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData
	 */
	public ConfigurationAnswerUserAdministrationTasks(ClientDavInterface connection, SystemObject senderObject, SystemObject ordererObject) throws OneSubscriptionPerSendData {
		super(connection, senderObject, ordererObject);
		_connection = connection;
		final DataModel dataModel = connection.getDataModel();

		_requestAspect = dataModel.getAspect("asp.antwort");
		_requestAtg = dataModel.getAttributeGroup("atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle");

		// Sender und Empf�nger anmelden
		init(_requestAtg, _requestAspect, null, null, null);
	}
}

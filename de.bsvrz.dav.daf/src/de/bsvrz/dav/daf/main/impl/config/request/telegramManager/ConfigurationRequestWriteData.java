/*
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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.Aspect;

/**
 * Erm�glicht es, Anfragen an die Konfiguration zu stellen. Die Anfragen sind schreibender Natur und
 * ver�ndern die Daten der Konfiguration.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision: 5060 $ / $Date: 2007-09-01 15:04:35 +0200 (Sa, 01 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationRequestWriteData extends AbstractSenderReceiverCommunication implements SenderReceiverCommunication {
		private final ClientDavInterface _connection;
	private final Aspect _requestAspect;
	private final AttributeGroup _requestAtg;
	private final AttributeGroup _responseAtg;
	private final Aspect _responseAspect;

	public ConfigurationRequestWriteData(ClientDavInterface connection, ConfigurationAuthority configurationAuthority, SystemObject localApplication) throws OneSubscriptionPerSendData {
		super(connection, configurationAuthority, localApplication);
		_connection = connection;
		final DataModel dataModel = connection.getDataModel();

		_requestAspect = dataModel.getAspect("asp.anfrage");
		_requestAtg = dataModel.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleSchreibend");

		_responseAspect = dataModel.getAspect("asp.antwort");
		_responseAtg = dataModel.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleSchreibend");

		// Sender und Empf�nger anmelden
		init(_requestAtg, _requestAspect, _responseAtg, _responseAspect, null);
	}
}

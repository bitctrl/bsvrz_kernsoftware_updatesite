/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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
package de.bsvrz.dav.daf.main.impl.config.request.telegramManager;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.AttributeGroup;

/**
 * Beauftragt die Konfigurations bestimmte Konfigurationsbereiche zu prüfen/modifizieren.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationRequestArea extends AbstractSenderReceiverCommunication implements SenderReceiverCommunication {

	private final ClientDavInterface _connection;
	private final Aspect _requestAspect;
	private final AttributeGroup _requestAtg;
	private final AttributeGroup _responseAtg;
	private final Aspect _responseAspect;

	public ConfigurationRequestArea(ClientDavInterface connection, ConfigurationAuthority configurationAuthority, SystemObject localApplication) throws OneSubscriptionPerSendData {
		super(connection, configurationAuthority, localApplication);
		_connection = connection;
		final DataModel dataModel = connection.getDataModel();

		_requestAspect = dataModel.getAspect("asp.anfrage");
		_requestAtg = dataModel.getAttributeGroup("atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle");

		_responseAspect = dataModel.getAspect("asp.antwort");
		_responseAtg = dataModel.getAttributeGroup("atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle");

		// Sender und Empfänger anmelden
		init(_requestAtg, _requestAspect, _responseAtg, _responseAspect, null);
	}
}

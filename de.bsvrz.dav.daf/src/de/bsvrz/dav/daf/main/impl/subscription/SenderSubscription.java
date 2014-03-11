/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.subscription;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;

/**
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 11279 $
 */
public class SenderSubscription {

	/** Repr�sentant des Emp�ngers */
	private ClientSenderInterface _clientSender;

	/** Der Objekt der Anmeldung */
	private SystemObject _systemObject;

	/** Beschreibende Informationen der zu versendenden Daten */
	private DataDescription _dataDescription;

	/** Senderanmeldeinformationen */
	private SendSubscriptionInfo _sendSubscriptionInfo;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param client                    Repr�sentant des Emp�ngers
	 * @param systemObject              Objekt der Anmeldung
	 * @param dataDescription           Beschreibende Informationen der zu versendenden Daten
	 * @param externalSimulationVariant TBD
	 * @param senderRole TBD
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException wenn Fehler bei Konfigurations�nderungen auftritt.
	 */
	public SenderSubscription(
			ClientSenderInterface client,
			SystemObject systemObject,
			DataDescription dataDescription,
			final short externalSimulationVariant,
			SenderRole senderRole
	) throws ConfigurationException {
		_clientSender = client;
		_systemObject = systemObject;
		_dataDescription = dataDescription;

		BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
				_systemObject.getId(), _dataDescription.getAttributeGroup().getAttributeGroupUsage(_dataDescription.getAspect()), externalSimulationVariant
		);
		// Sender bekommen immer eine Sendesteuerung, auch wenn diese nicht expliziet angefordert wird.
		// Dies erm�glicht zu erkennen, ob bereits eine Sendesteuerung vorliegt (egal ob positiv oder negativ).
		// Falls keine Sendesteuerung vorliegt, kann eine Exception geworfen werden
		boolean requestSupported = isRequestSupported() || senderRole == SenderRole.sender();
		_sendSubscriptionInfo = new SendSubscriptionInfo(baseSubscriptionInfo, senderRole, requestSupported);
	}

	/**
	 * Gibt den Empf�nger zur�ck.
	 *
	 * @return Repr�sentant des Emp�ngers
	 */
	public final ClientSenderInterface getClientSender() {
		return _clientSender;
	}

	/**
	 * Setzt den Repr�sentant des Emp�ngers.
	 *
	 * @param client Repr�sentant des Emp�ngers
	 */
	public final void setClientSender(ClientSenderInterface client) {
		_clientSender = client;
	}

	/**
	 * Gibt der Systemobjekt zur�ck
	 *
	 * @return Systemobjekt
	 */
	public final SystemObject getSystemObject() {
		return _systemObject;
	}

	/**
	 * Gibt die beschreibende Informationen der zu versendenden Daten zur�ck
	 *
	 * @return beschreibende Informationen
	 */
	public final DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Gibt die Basisanmeldeinformationen zur�ck
	 *
	 * @return Basisanmeldeinformation
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _sendSubscriptionInfo.getBaseSubscriptionInfo();
	}

	/**
	 * Gibt die Senderanmeldeinformationen zur�ck
	 *
	 * @return Senderanmeldeinformation
	 */
	public final SendSubscriptionInfo getSendSubscriptionInfo() {
		return _sendSubscriptionInfo;
	}

	/**
	 * Gibt die Information zur�ck, ob die Applikation getriggert werden kann oder nicht.
	 *
	 * @return <code>true: </code>Applikation kann getriggert werden, <code>false:</code>Applikation kann nicht getriggert werden
	 */
	private  boolean isRequestSupported() {
		if(_clientSender == null) {
			return false;
		}
		return _clientSender.isRequestSupported(_systemObject, _dataDescription);
	}

	/**
	 * Gibt an, ob der Status der Applikation der einer Quelle ist.
	 *
	 * @return <code>true: Anmeldung ist Quelle </code>, <code>false:</code> Anmeldung ist keine Quelle
	 */
	public final boolean isSource() {
		return _sendSubscriptionInfo.isSource();
	}

	/**
	 * Gibt an, ob der Status der Applikation der eines Senders ist.
	 *
	 * @return <code>true: Anmeldung ist Sender </code>, <code>false:</code> Anmeldung ist kein Sender
	 */
	public final boolean isSender() {
		return _sendSubscriptionInfo.isSender();
	}
}

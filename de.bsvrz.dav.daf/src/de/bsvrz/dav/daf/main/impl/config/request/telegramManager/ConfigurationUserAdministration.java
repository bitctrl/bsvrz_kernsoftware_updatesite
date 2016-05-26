/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

import java.util.Collection;
import java.util.List;

/**
 * Ermöglicht es von Applikationsseite aus die Benutzer einer Konfiguration zu verwalten. 
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigurationUserAdministration implements UserAdministration {
	private final ConfigurationRequester _requester;


	public ConfigurationUserAdministration(final ConfigurationRequester requester) {
		_requester = requester;
	}

	public void createNewUser(
			String orderer, String ordererPassword, String newUsername, String newUserPid, String newPassword, boolean adminRights, String pidConfigurationArea
	) throws ConfigurationTaskException {
		_requester.createNewUser(orderer, ordererPassword, newUsername, newUserPid, newPassword, adminRights, pidConfigurationArea);
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
		_requester.createNewUser(orderer, ordererPassword, newUsername, newUserPid, newPassword, adminRights, pidConfigurationArea, data);
	}

	public void deleteUser(final String orderer, final String ordererPassword, final String userToDelete) throws ConfigurationTaskException {
		_requester.deleteUser(orderer,ordererPassword, userToDelete);
	}

	public boolean isUserAdmin(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
		return _requester.isUserAdmin(orderer,ordererPassword, username);
	}

	public void changeUserRights(String orderer, String ordererPassword, String user, boolean adminRights) throws ConfigurationTaskException {
		_requester.changeUserRights(orderer, ordererPassword, user, adminRights);
	}

	public void changeUserPassword(String orderer, String ordererPassword, String user, String newUserPassword) throws ConfigurationTaskException {
		_requester.changeUserPassword(orderer, ordererPassword, user, newUserPassword);
	}

	public void createSingleServingPassword(String orderer, String ordererPassword, String username, String singleServingPassword)
			throws ConfigurationTaskException {
		_requester.createSingleServingPassword(orderer, ordererPassword, username, singleServingPassword);
	}

	public int getSingleServingPasswordCount(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
		return _requester.getSingleServingPasswordCount(orderer, ordererPassword, username);
	}

	public void clearSingleServingPasswords(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
		_requester.clearSingleServingPasswords(orderer, ordererPassword, username);
	}

	public boolean isUserValid(final String orderer, final String ordererPassword, final String username) throws ConfigurationTaskException {
		return _requester.isUserValid(orderer, ordererPassword, username);
	}

	public List<SystemObject> subscribeUserChangeListener(MutableCollectionChangeListener listener) {
		return _requester.subscribeUserChangeListener(listener);
	}

	public void unsubscribeUserChangeListener(MutableCollectionChangeListener listener) {
		 _requester.unsubscribeUserChangeListener(listener);
	}

}

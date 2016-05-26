/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class RemoteRequestManager implements DavConnectionListener {

	private static final Debug _debug = Debug.getLogger();

	private static final Map<ClientDavInterface, RemoteRequestManager> _connection2ManagerMap = new HashMap<ClientDavInterface, RemoteRequestManager>();

	private final Map<ConfigurationAuthority, ConfigurationRequester> _configAuthority2RequesterMap = new HashMap<ConfigurationAuthority, ConfigurationRequester>();

	private final ClientDavInterface _connection;

	/**
	 * Erzeugt eine Instanz des RemoteRequestManagers
	 *
	 * @param connection         Verbindung über die sich angemeldet wird
	 * @return Instanz des RemoteRequestManagerŽs
	 */
	public static RemoteRequestManager getInstance(ClientDavInterface connection) {

		synchronized(_connection2ManagerMap) {
			RemoteRequestManager manager = _connection2ManagerMap.get(connection);
			if(manager == null) {
				manager = new RemoteRequestManager(connection);
				_connection2ManagerMap.put(connection, manager);
			}
			return manager;
		}
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
			ClientDavInterface connection
	) {
		try {
			_connection = connection;
			connection.addConnectionListener(this);
		}
		catch(Exception e) {
			e.printStackTrace();
			_debug.warning("Initialisierung des RemoteRequestManager fehlgeschlagen", e);
			throw new RuntimeException(e);
		}
	}

	public ConfigurationRequester getRequester(ConfigurationAuthority remoteConfigurationAuthority, final DafDataModel dafDataModel) {
		synchronized(_configAuthority2RequesterMap) {
			ConfigurationRequester requester = _configAuthority2RequesterMap.get(
					remoteConfigurationAuthority
			);
			if(requester == null) {
				if(dafDataModel.getProtocolVersion() == 0) {
					requester = new RemoteRequesterV0(
							_connection, dafDataModel, remoteConfigurationAuthority
					);
				}
				else {
					requester = new RemoteRequester(
							_connection, dafDataModel, remoteConfigurationAuthority
					);
				}
				_configAuthority2RequesterMap.put(remoteConfigurationAuthority, requester);
			}
			return requester;
		}
	}

}

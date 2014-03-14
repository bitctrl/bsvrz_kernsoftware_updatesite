/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;


import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11467 $
 */
public class ClientSubscriptionInfo {

	private final ClientDavConnection _connection;

	private final List<ClientSendingSubscription> _senderSubscriptions = new ArrayList<ClientSendingSubscription>();

	private final List<ClientReceivingSubscription> _receiverSubscriptions = new ArrayList<ClientReceivingSubscription>();

	private final List<DavInformation> _potentialCentralDavs = new ArrayList<DavInformation>();

	public List<ClientSendingSubscription> getSenderSubscriptions() {
		return Collections.unmodifiableList(_senderSubscriptions);
	}

	public List<ClientReceivingSubscription> getReceiverSubscriptions() {
		return Collections.unmodifiableList(_receiverSubscriptions);
	}

	ClientSubscriptionInfo(final ClientDavConnection connection, final AttributeGroupUsage usage, final byte[] bytes) throws IOException {
		_connection = connection;
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		final DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		try {
			final int numReceivingSubscriptions = dataInputStream.readInt();
			for(int i = 0; i < numReceivingSubscriptions; i++) {
				boolean isLocal = dataInputStream.readBoolean();
				final long applicationId = dataInputStream.readLong();
				final long userId = dataInputStream.readLong();
				final boolean isSource = dataInputStream.readBoolean();
				final boolean isRequestSupported = dataInputStream.readBoolean();
				final int state = dataInputStream.readInt();
				final int constate = dataInputStream.readInt();
				final ClientSendingSubscription clientSendingSubscription = new ClientSendingSubscription( isLocal,
						applicationId, userId, isSource, isRequestSupported, state, constate
				);
				_senderSubscriptions.add(clientSendingSubscription);
			}
			final int numSendingSubscriptions = dataInputStream.readInt();
			for(int i = 0; i < numSendingSubscriptions; i++) {
				boolean isLocal = dataInputStream.readBoolean();
				final long applicationId = dataInputStream.readLong();
				final long userId = dataInputStream.readLong();
				final boolean isDrain = dataInputStream.readBoolean();
				final boolean isDelayed = dataInputStream.readBoolean();
				final boolean isDelta = dataInputStream.readBoolean();
				final int state = dataInputStream.readInt();
				final int constate = dataInputStream.readInt();
				final ClientReceivingSubscription clientReceivingSubscription = new ClientReceivingSubscription(isLocal,
						applicationId, userId, isDrain, isDelayed, isDelta, state, constate
				);
				_receiverSubscriptions.add(clientReceivingSubscription);
			}
			if(dataInputStream.available() > 0){
				int numpotCentralDavs = dataInputStream.readInt();
				for(int i = 0; i < numpotCentralDavs; i++){
					long centralDavId = dataInputStream.readLong();
					long connectionDavId = dataInputStream.readLong();
					int throughputResistance = dataInputStream.readInt();
					long userId = dataInputStream.readLong();
					_potentialCentralDavs.add(new DavInformation(centralDavId, connectionDavId, userId, throughputResistance));
				}
			}
		}
		finally {
			dataInputStream.close();
		}
	}

	public class ClientSendingSubscription {

		private final SystemObject _application;

		private final boolean _local;

		private final long _applicationId;

		private final long _userId;

		private final boolean _source;

		private final boolean _requestSupported;

		private final SubscriptionState _state;

		private final ClientConnectionState _connectionState;

		private SystemObject _user;

		public ClientSendingSubscription(
				final boolean isLocal,
				final long applicationId,
				final long userId,
				final boolean source,
				final boolean requestSupported,
				final int state,
				final int conState) {
			_local = isLocal;
			_applicationId = applicationId;
			_userId = userId;
			_source = source;
			_requestSupported = requestSupported;
			_application = _connection.getDataModel().getObject(applicationId);
			_user = _connection.getDataModel().getObject(userId);
			switch(state) {
				case 1:
					_state = SubscriptionState.ReceiversAvailable;
					break;
				case 2:
					_state = SubscriptionState.NoReceiversAvailable;
					break;
				case 3:
					_state = SubscriptionState.Waiting;
					break;
				case 4:
					_state = SubscriptionState.NotAllowed;
					break;
				case 6:
					_state = SubscriptionState.NotResponsible;
					break;
				case 7:
					_state = SubscriptionState.MultiRemoteLock;
					break;
				default:
					_state = SubscriptionState.InvalidSubscription;
			}
			_connectionState = ClientConnectionState.values()[conState];
		}


		public SystemObject getApplication() {
			return _application;
		}

		public SystemObject getUser() {
			return _user;
		}

		public boolean isSource() {
			return _source;
		}

		public boolean isRequestSupported() {
			return _requestSupported;
		}

		public SubscriptionState getState() {
			return _state;
		}

		public ClientConnectionState getConnectionState() {
			return _connectionState;
		}

		public long getApplicationId() {
			return _applicationId;
		}

		public long getUserId() {
			return _userId;
		}

		@Override
		public String toString() {
			return getApplicationPidOrId();
		}

		public String getApplicationPidOrId() {
			return _application == null ? "[" + _applicationId + "]" : _application.toString();
		}

		public String getUserPidOrId() {
			return _user == null ? "[" + _userId + "]" : _user.toString();
		}

		public boolean isLocal() {
			return _local;
		}
	}


	public class ClientReceivingSubscription {

		private final boolean _local;

		private final long _applicationId;

		private final long _userId;

		private final boolean _drain;

		private final boolean _delayed;

		private final boolean _delta;

		private final SystemObject _application;

		private final SubscriptionState _state;

		private final ClientConnectionState _connectionState;

		private SystemObject _user;

		public ClientReceivingSubscription(
				final boolean isLocal,
				final long applicationId, final long userId, final boolean drain, final boolean delayed, final boolean delta, final int state, final int conState) {
			_local = isLocal;
			_applicationId = applicationId;
			_userId = userId;
			_drain = drain;
			_delayed = delayed;
			_delta = delta;
			if(applicationId == 0){
				_application = _connection.getDataModel().getConfigurationAuthority();
				_user = _connection.getDataModel().getConfigurationAuthority();
			}
			else{
				_application = _connection.getDataModel().getObject(applicationId);
				_user = _connection.getDataModel().getObject(userId);
			}
			switch(state) {
				case 1:
					_state = SubscriptionState.NoSendersAvailable;
					break;
				case 2:
					_state = SubscriptionState.SendersAvailable;
					break;
				case 3:
					_state = SubscriptionState.Waiting;
					break;
				case 4:
					_state = SubscriptionState.NotAllowed;
					break;
				case 6:
					_state = SubscriptionState.NotResponsible;
					break;
				case 7:
					_state = SubscriptionState.MultiRemoteLock;
					break;
				default:
					_state = SubscriptionState.InvalidSubscription;
			}
			_connectionState = ClientConnectionState.values()[conState];
		}

		public boolean isLocal() {
			return _local;
		}

		public boolean isDrain() {
			return _drain;
		}

		public boolean isDelayed() {
			return _delayed;
		}

		public boolean isDelta() {
			return _delta;
		}

		public SystemObject getApplication() {
			return _application;
		}

		public SystemObject getUser() {
			return _user;
		}

		public SubscriptionState getState() {
			return _state;
		}

		public ClientConnectionState getConnectionState() {
			return _connectionState;
		}

		public long getApplicationId() {
			return _applicationId;
		}

		public long getUserId() {
			return _userId;
		}

		@Override
		public String toString() {
			return getApplicationPidOrId();
		}

		public String getApplicationPidOrId() {
			return _application == null ? "[" + _applicationId + "]" : _application.toString();
		}

		public String getUserPidOrId() {
			return _user == null ? "[" + _userId + "]" : _user.toString();
		}
	}

	public List<DavInformation> getPotentialCentralDavs() {
		return Collections.unmodifiableList(_potentialCentralDavs);
	}

	@Override
	public String toString() {
		return "ClientSubscriptionInfo{" + "_receiverSubscriptions=" + _senderSubscriptions + ", _senderSubscriptions=" + _receiverSubscriptions + '}';
	}

	public class DavInformation {
		private final long _centralDavId;
		private final long _connectionDavId;
		private final long _userId;
		private final int _throughputResistance;

		private DavInformation(final long centralDavId, final long connectionDavId, final long userId, final int throughputResistance) {
			_centralDavId = centralDavId;
			_connectionDavId = connectionDavId;
			_userId = userId;
			_throughputResistance = throughputResistance;
		}

		public long getCentralDavId() {
			return _centralDavId;
		}

		public long getConnectionDavId() {
			return _connectionDavId;
		}

		public DavApplication getCentralDav() {
			return (DavApplication) _connection.getDataModel().getObject(_centralDavId);
		}

		public DavApplication getConnectionDav() {
			return (DavApplication) _connection.getDataModel().getObject(_connectionDavId);
		}

		public long getUserId() {
			return _userId;
		}

		public SystemObject getUser() {
			return _connection.getDataModel().getObject(_userId);
		}


		public int getThroughputResistance() {
			return _throughputResistance;
		}


		@Override
		public String toString() {
			return getCentralDavPidOrId();
		}

		public String getCentralDavPidOrId() {
			DavApplication centralDav = getCentralDav();
			return centralDav == null ? "[" + _centralDavId + "]" : centralDav.toString();
		}

		public String getConnectionDavPidOrId() {
			DavApplication connectionDav = getConnectionDav();
			return connectionDav == null ? "[" + _connectionDavId + "]" : connectionDav.toString();
		}

		public String getUserPidOrId() {
			SystemObject user = getUser();
			return user == null ? "[" + _userId + "]" : user.toString();
		}
	}
}

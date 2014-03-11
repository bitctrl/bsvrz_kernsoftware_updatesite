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
import de.bsvrz.dav.daf.main.config.ClientApplication;
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
public class ApplicationSubscriptionInfo {

	private final ClientDavConnection _connection;

	private final List<ApplicationSendingSubscription> _senderSubscriptions = new ArrayList<ApplicationSendingSubscription>();

	private final List<ApplicationReceivingSubscription> _receiverSubscriptions = new ArrayList<ApplicationReceivingSubscription>();

	public List<ApplicationSendingSubscription> getSenderSubscriptions() {
		return Collections.unmodifiableList(_senderSubscriptions);
	}

	public List<ApplicationReceivingSubscription> getReceiverSubscriptions() {
		return Collections.unmodifiableList(_receiverSubscriptions);
	}

	ApplicationSubscriptionInfo(final ClientDavConnection connection, final ClientApplication application, final byte[] bytes) throws IOException {
		_connection = connection;
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		final DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		try {
			final int numReceivingSubscriptions = dataInputStream.readInt();
			for(int i = 0; i < numReceivingSubscriptions; i++) {
				final long objectId = dataInputStream.readLong();
				final long usageId = dataInputStream.readLong();
				final short simVar = dataInputStream.readShort();
				final boolean isSource = dataInputStream.readBoolean();
				final boolean isRequestSupported = dataInputStream.readBoolean();
				final int state = dataInputStream.readInt();
				final ApplicationSendingSubscription applicationSendingSubscription = new ApplicationSendingSubscription(objectId,
						usageId, simVar, isSource, isRequestSupported, state
				);
				_senderSubscriptions.add(applicationSendingSubscription);
			}
			final int numSendingSubscriptions = dataInputStream.readInt();
			for(int i = 0; i < numSendingSubscriptions; i++) {
				final long objectId = dataInputStream.readLong();
				final long usageId = dataInputStream.readLong();
				final short simVar = dataInputStream.readShort();
				final boolean isDrain = dataInputStream.readBoolean();
				final boolean isDelayed = dataInputStream.readBoolean();
				final boolean isDelta = dataInputStream.readBoolean();
				final int state = dataInputStream.readInt();
				final ApplicationReceivingSubscription applicationReceivingSubscription = new ApplicationReceivingSubscription(objectId,
						usageId, simVar, isDrain, isDelayed, isDelta, state
				);
				_receiverSubscriptions.add(applicationReceivingSubscription);
			}
		}
		finally {
			dataInputStream.close();
		}
		Collections.sort(_senderSubscriptions);
		Collections.sort(_receiverSubscriptions);
	}

	public class ApplicationSendingSubscription implements Comparable<ApplicationSendingSubscription>  {

		private final boolean _source;

		private final boolean _requestSupported;

		private final SubscriptionState _state;

		private final long _objectId;

		private final long _usageId;

		private final short _simVar;

		private final SystemObject _object;

		private final AttributeGroupUsage _usage;


		public ApplicationSendingSubscription(
				final long objectId, final long usageId, final short simVar, final boolean source, final boolean requestSupported, final int state) {
			_objectId = objectId;
			_usageId = usageId;
			_simVar = simVar;

			_object = _connection.getDataModel().getObject(objectId);
			_usage = _connection.getDataModel().getAttributeGroupUsage(usageId);

			_source = source;
			_requestSupported = requestSupported;
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
		}

		public long getObjectId() {
			return _objectId;
		}

		public long getUsageId() {
			return _usageId;
		}

		public short getSimVar() {
			return _simVar;
		}

		public SystemObject getObject() {
			return _object;
		}

		public AttributeGroupUsage getUsage() {
			return _usage;
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

		@Override
		public String toString() {
			return _usage.getAttributeGroup().getPidOrNameOrId() + ":" + _usage.getAspect().getPidOrNameOrId() + ":" + _object.getPidOrNameOrId();
		}

		public int compareTo(final ApplicationSendingSubscription o) {
			int tmp;
			tmp = _usage.getAttributeGroup().getPidOrNameOrId().compareTo(o.getUsage().getAttributeGroup().getPidOrNameOrId());
			if(tmp != 0) return tmp;
			tmp = _usage.getAspect().getPidOrNameOrId().compareTo(o.getUsage().getAspect().getPidOrNameOrId());
			if(tmp != 0) return tmp;
			tmp = getObject().getPidOrNameOrId().compareTo(o.getObject().getPidOrNameOrId());
			return tmp;
		}
	}

	public class ApplicationReceivingSubscription implements Comparable<ApplicationReceivingSubscription> {

		private final boolean _drain;

		private final boolean _delayed;

		private final boolean _delta;

		private final SubscriptionState _state;

		private final long _objectId;

		private final long _usageId;

		private final short _simVar;

		private final SystemObject _object;

		private final AttributeGroupUsage _usage;

		public ApplicationReceivingSubscription(
				final long objectId,
				final long usageId,
				final short simVar,
				final boolean drain,
				final boolean delayed,
				final boolean delta,
				final int state) {
			_objectId = objectId;
			_usageId = usageId;
			_simVar = simVar;

			_object = _connection.getDataModel().getObject(objectId);
			_usage = _connection.getDataModel().getAttributeGroupUsage(usageId);
			_drain = drain;
			_delayed = delayed;
			_delta = delta;
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

		public SubscriptionState getState() {
			return _state;
		}

		@Override
		public String toString() {
			return _usage.getAttributeGroup().getPidOrNameOrId() + ":" + _usage.getAspect().getPidOrNameOrId() + ":" + _object.getPidOrNameOrId();
		}

		public long getObjectId() {
			return _objectId;
		}

		public long getUsageId() {
			return _usageId;
		}

		public short getSimVar() {
			return _simVar;
		}

		public SystemObject getObject() {
			return _object;
		}

		public AttributeGroupUsage getUsage() {
			return _usage;
		}

		public int compareTo(final ApplicationReceivingSubscription o) {
			int tmp;
			tmp = _usage.getAttributeGroup().getPidOrNameOrId().compareTo(o.getUsage().getAttributeGroup().getPidOrNameOrId());
			if(tmp != 0) return tmp;
			tmp = _usage.getAspect().getPidOrNameOrId().compareTo(o.getUsage().getAspect().getPidOrNameOrId());
			if(tmp != 0) return tmp;
			tmp = getObject().getPidOrNameOrId().compareTo(o.getObject().getPidOrNameOrId());
			return tmp;
		}
	}

	@Override
	public String toString() {
		return "ApplicationSubscriptions{" + "_receiverSubscriptions=" + _senderSubscriptions + ", _senderSubscriptions=" + _receiverSubscriptions + '}';
	}
}

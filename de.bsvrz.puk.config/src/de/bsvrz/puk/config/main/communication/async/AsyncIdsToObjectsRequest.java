/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main.communication.async;

import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.util.async.AbstractAsyncDataModelRequest;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AsyncIdsToObjectsRequest extends AbstractAsyncDataModelRequest {

	private static final Debug _debug = Debug.getLogger();

	private final long[] _ids;
	private final SystemObject[] _objects;

	private ForeignObjectManager _foreignObjectManager;

	private ArrayList<Long> _toBeRequestedObjectIds;

	private int _completedCount = 0;

	public AsyncIdsToObjectsRequest(DataModel dataModel, final ForeignObjectManager foreignObjectManager, final long[] ids) {
		super(dataModel);
		_foreignObjectManager = foreignObjectManager;
		_ids = ids;
		_objects = new SystemObject[ids.length];
	}

	public void startProcessing() {
		boolean askForeignObjectManager = false;
		for(int i = 0; i < _ids.length; ++i) {
			final long id = _ids[i];
			SystemObject object = getDataModel().getObject(id);
			if(object == null) {
				askForeignObjectManager = true;
			}
			_objects[i] = object;
		}
//		System.out.println("++++++++++ AsyncIdsToObjectsRequest.startProcessing(), calling requestRemoteObjects, " + getProperties());
		if(askForeignObjectManager) {
			_foreignObjectManager.requestRemoteObjects(this);
		}
		else {
//			System.out.println("++++++++++ AsyncIdsToObjectsRequest.startProcessing() calling callAsyncCompletion(), " + getProperties());
			callAsyncCompletion();
		}
	}

	private String getProperties() {
		return "_ids=" + Arrays.toString(_ids)
		       + ", _toBeRequestedObjectIds=" + _toBeRequestedObjectIds
		       + ", _completedCount=" + _completedCount
		       + ", _objects=" + Arrays.toString(_objects);
	}

	public SystemObject[] getObjects() {
		return _objects;
	}

	public long[] getIds() {
		return _ids;
	}

	public void setAsyncObjectRequestIds(final ArrayList<Long> toBeRequestedObjectIds) {
		_toBeRequestedObjectIds = toBeRequestedObjectIds;
		if(toBeRequestedObjectIds.size() == 0) {
//			System.out.println("++++++++++ AsyncIdsToObjectsRequest.setAsyncObjectRequestIds() calling callAsyncCompletion(), " + getProperties());
			callAsyncCompletion();
		}
		else {
//			System.out.println("++++++++++ AsyncIdsToObjectsRequest.setAsyncObjectRequestIds() storing _toBeRequestedObjectIds, " + getProperties());
		}
	}

	public void objectComplete(final long completeId) {
//		Thread.dumpStack();
		final boolean allComplete;
		synchronized(this) {
			allComplete = _toBeRequestedObjectIds.size() == ++_completedCount;
		}
		
		for(int i = 0; i < _ids.length; i++) {
			long id = _ids[i];
			if(completeId == id) {
				SystemObject remoteObject = _foreignObjectManager.getRemoteObject(id);
				if(remoteObject != null) {
					_objects[i] = remoteObject;
					if(remoteObject instanceof DynamicObject && remoteObject.getConfigurationArea() != null) {
						DynamicObject remoteDynamicObject = (DynamicObject)remoteObject;
						_foreignObjectManager.cacheForeignObject(remoteDynamicObject);
					}
				}
			}
		}
		if(allComplete) {
//			System.out.println("++++++++++ AsyncIdsToObjectsRequest.objectComplete() calling callAsyncCompletion(), " + getProperties());
			callAsyncCompletion();
		}
	}

	@Override
	public void callAsyncCompletion() {
		for(int i = 0; i < _ids.length; i++) {
			long id = _ids[i];
			if(_objects[i] == null) {
				_objects[i] = _foreignObjectManager.getCachedForeignObject(id);
			}
		}
		super.callAsyncCompletion();
	}

	@Override
	public String toString() {
		return "AsyncIdsToObjectsRequest{" + "_ids=" + Arrays.toString(_ids) + ", _toBeRequestedObjectIds=" + _toBeRequestedObjectIds + ", _completedCount=" + _completedCount
		       + '}';
	}
}

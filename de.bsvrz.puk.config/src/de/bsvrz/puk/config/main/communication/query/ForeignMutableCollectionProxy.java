/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ForeignMutableCollectionProxy {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private ForeignConfigRequester _foreignConfigRequester;

	private final short _internalSimVariant;

	private final MutableCollection _mutableCollection;

	private final CopyOnWriteArrayList<ExtendedMutableCollectionChangeListener> _listeners = new CopyOnWriteArrayList<ExtendedMutableCollectionChangeListener>();

	private List<SystemObject> _elements = null;

	private ConcurrentLinkedQueue<ForeignObjectTransferListener> _unresolvedQueue = new ConcurrentLinkedQueue<ForeignObjectTransferListener>();


	public ForeignMutableCollectionProxy(
			final ForeignConfigRequester foreignConfigRequester, final short internalSimVariant, final MutableCollection mutableCollection) {
		_foreignConfigRequester = foreignConfigRequester;
		_internalSimVariant = internalSimVariant;
		_mutableCollection = mutableCollection;
	}

	public void addChangeListener(final ExtendedMutableCollectionChangeListener listener) {
		synchronized(this) {
			final boolean notSubscribed = _listeners.isEmpty();
			_debug.fine("ForeignMutableCollectionProxy.addChangeListener, notSubscribed", notSubscribed);
			_listeners.add(listener);
			if(_elements != null) {
				getExecutor().execute(
						new Runnable() {
							public void run() {
								listener.initialState(_mutableCollection, _internalSimVariant, _elements);
							}
						}
				);
			}
			if(notSubscribed) {
				getExecutor().execute(
						new Runnable() {
							public void run() {
								_foreignConfigRequester.subscribe(ForeignMutableCollectionProxy.this);
							}
						}
				);
			}
		}
	}

	public void removeChangeListener(final ExtendedMutableCollectionChangeListener listener) {
		synchronized(this) {
			_listeners.remove(listener);
			if(_listeners.isEmpty()) {
				getExecutor().execute(
						new Runnable() {
							public void run() {
								_foreignConfigRequester.unsubscribe(ForeignMutableCollectionProxy.this);
							}
						}
				);
			}
		}
	}

	void processReceivedElementIds(final long[] elementIds) {
		_unresolvedQueue.clear();
		ArrayList<SystemObject> elements = new ArrayList<SystemObject>(elementIds.length);
		ArrayList<Long> unresolvedElementIds = new ArrayList<Long>();
		int unresolvedElementCount = resolveElements(elementIds, unresolvedElementIds, elements);
		if(unresolvedElementCount > 0) {
			final DelayedSetElementsIds delayedSetElementsIds = new DelayedSetElementsIds(elementIds, unresolvedElementCount);
			_unresolvedQueue.add(delayedSetElementsIds);
			for(Long unresolvedElementId : unresolvedElementIds) {
				_foreignConfigRequester.queryObject(unresolvedElementId, delayedSetElementsIds);
			}
		}
		else {
			setElements(elements);
		}
	}

	void processConnectionTimeout() {
		final ArrayList<SystemObject> elements = new ArrayList<SystemObject>(0);
		synchronized(this) {
			if(_elements == null) {
				_elements = new ArrayList<SystemObject>(0);
				getExecutor().execute(
						new Runnable() {
							public void run() {
								for(ExtendedMutableCollectionChangeListener listener : _listeners) {
									listener.initialState(_mutableCollection, _internalSimVariant, elements);
								}
							}
						}
				);
			}
		}
	}

	void processElementsChanged(final long[] addedElementIds, final long[] removedElementIds) {
		ArrayList<SystemObject> addedElements = new ArrayList<SystemObject>(addedElementIds.length);
		ArrayList<Long> unresolvedAddedElementIds = new ArrayList<Long>();
		int unresolvedAddedElementCount = resolveElements(addedElementIds, unresolvedAddedElementIds, addedElements);
		if(unresolvedAddedElementCount > 0) {
			final DelayedElementChangedIds delayedElementChangedIds = new DelayedElementChangedIds(
					addedElementIds, removedElementIds, unresolvedAddedElementCount
			);
			_unresolvedQueue.add(delayedElementChangedIds);
			for(Long unresolvedAddedElementId : unresolvedAddedElementIds) {
				_foreignConfigRequester.queryObject(unresolvedAddedElementId, delayedElementChangedIds);
			}
		}
		else {
			ArrayList<SystemObject> removedElements = new ArrayList<SystemObject>(removedElementIds.length);
			ArrayList<Long> unresolvedRemovedElementIds = new ArrayList<Long>();
			resolveElements(removedElementIds, unresolvedRemovedElementIds, removedElements);
			elementsChanged(addedElements, removedElements);
		}
	}

	private int resolveElements(final long[] elementIds, final ArrayList<Long> unresolvedElementIds, final ArrayList<SystemObject> elements) {
		final ForeignObjectManager foreignObjectManager = _foreignConfigRequester.getForeignObjectManager();
		final DataModel configuration = foreignObjectManager.getConfiguration();
		for(int i = 0; i < elementIds.length; i++) {
			final long id = elementIds[i];
			SystemObject element = configuration.getObject(id);
			if(element == null) {
				if(foreignObjectManager.hasRemoteObject(id)) {
					element = foreignObjectManager.getRemoteObject(id);
				}
				else {
					unresolvedElementIds.add(id);
				}
			}
			elements.add(element);
		}
		return unresolvedElementIds.size();
	}

	public void processConnectedEvent() {
		_foreignConfigRequester.sendSubscription(this);
	}

	private class DelayedSetElementsIds implements ForeignObjectTransferListener {

		private final long[] _elementIds;

		private int _unresolvedElementCount;

		public DelayedSetElementsIds(final long[] elementIds, final int unresolvedElementCount) {
			_elementIds = elementIds;
			_unresolvedElementCount = unresolvedElementCount;
		}


		public long[] getElementIds() {
			return _elementIds;
		}

		public int getUnresolvedElementCount() {
			return _unresolvedElementCount;
		}

		public void objectComplete() {
			if(--_unresolvedElementCount == 0 && _unresolvedQueue.peek() == this) {
				processDelayedAnswers();
			}
		}
	}

	private class DelayedElementChangedIds implements ForeignObjectTransferListener {

		private final long[] _addedElementIds;

		private final long[] _removedElementIds;

		private int _unresolvedAddedElementCount;

		public DelayedElementChangedIds(
				final long[] addedElementIds, final long[] removedElementIds, final int unresolvedAddedElementCount) {

			_addedElementIds = addedElementIds;
			_removedElementIds = removedElementIds;
			_unresolvedAddedElementCount = unresolvedAddedElementCount;
		}


		public long[] getAddedElementIds() {
			return _addedElementIds;
		}

		public long[] getRemovedElementIds() {
			return _removedElementIds;
		}

		public int getUnresolvedAddedElementCount() {
			return _unresolvedAddedElementCount;
		}

		public void objectComplete() {
			if(--_unresolvedAddedElementCount == 0 && _unresolvedQueue.peek() == this) {
				processDelayedAnswers();
			}
		}
	}

	private void processDelayedAnswers() {
		while(true) {
			final ForeignObjectTransferListener transferListener = _unresolvedQueue.peek();
			if(transferListener == null) return;
			if(transferListener instanceof DelayedSetElementsIds) {
				DelayedSetElementsIds delayedSetElementsIds = (DelayedSetElementsIds)transferListener;
				if(delayedSetElementsIds.getUnresolvedElementCount() == 0) {
					final long[] elementIds = delayedSetElementsIds.getElementIds();
					ArrayList<SystemObject> elements = new ArrayList<SystemObject>(elementIds.length);
					ArrayList<Long> unresolvedElementIds = new ArrayList<Long>();
					resolveElements(elementIds, unresolvedElementIds, elements);
					setElements(elements);
				}
				else {
					return;
				}
			}
			else if(transferListener instanceof DelayedElementChangedIds) {
				DelayedElementChangedIds delayedElementChangedIds = (DelayedElementChangedIds)transferListener;
				if(delayedElementChangedIds.getUnresolvedAddedElementCount() == 0) {
					final long[] addedElementIds = delayedElementChangedIds.getAddedElementIds();
					ArrayList<SystemObject> addedElements = new ArrayList<SystemObject>(addedElementIds.length);
					ArrayList<Long> unresolvedAddedElementIds = new ArrayList<Long>();
					resolveElements(addedElementIds, unresolvedAddedElementIds, addedElements);
					final long[] removedElementIds = delayedElementChangedIds.getRemovedElementIds();
					ArrayList<SystemObject> removedElements = new ArrayList<SystemObject>(removedElementIds.length);
					ArrayList<Long> unresolvedRemovedElementIds = new ArrayList<Long>();
					resolveElements(removedElementIds, unresolvedRemovedElementIds, removedElements);
					elementsChanged(addedElements, removedElements);
				}
				else {
					return;
				}
			}
			_unresolvedQueue.poll();
		}
	}

	void setElements(final List<SystemObject> elements) {
		synchronized(this) {
			if(_elements == null) {
				_elements = new ArrayList<SystemObject>(elements);
				getExecutor().execute(
						new Runnable() {
							public void run() {
								for(ExtendedMutableCollectionChangeListener listener : _listeners) {
									listener.initialState(_mutableCollection, _internalSimVariant, elements);
								}
							}
						}
				);
			}
			else {
				final List<SystemObject> addedElements = new ArrayList<SystemObject>(elements);
				addedElements.removeAll(_elements);
				final List<SystemObject> removedElements = new ArrayList<SystemObject>(_elements);
				removedElements.removeAll(elements);
				_elements.removeAll(removedElements);
				_elements.addAll(addedElements);
				getExecutor().execute(
						new Runnable() {
							public void run() {
								for(ExtendedMutableCollectionChangeListener listener : _listeners) {
									listener.collectionChanged(_mutableCollection, _internalSimVariant, addedElements, removedElements);
								}
							}
						}
				);
			}
		}
	}

	void elementsChanged(final List<SystemObject> addedElements, final List<SystemObject> removedElements) {
		synchronized(this) {
			_elements.removeAll(removedElements);
			_elements.addAll(addedElements);
			getExecutor().execute(
					new Runnable() {
						public void run() {
							for(ExtendedMutableCollectionChangeListener listener : _listeners) {
								listener.collectionChanged(_mutableCollection, _internalSimVariant, addedElements, removedElements);
							}
						}
					}
			);
		}
	}

	Executor getExecutor() {
		return _foreignConfigRequester.getExecutor();
	}


	public MutableCollection getMutableCollection() {
		return _mutableCollection;
	}

	public short getInternalSimVariant() {
		return _internalSimVariant;
	}
}

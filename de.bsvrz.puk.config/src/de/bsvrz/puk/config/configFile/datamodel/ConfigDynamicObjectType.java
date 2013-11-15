/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

import java.util.*;

/**
 * Implementierung des Interfaces f�r den Typ von dynamischen Objekten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8800 $
 */
public class ConfigDynamicObjectType extends ConfigSystemObjectType implements DynamicObjectType {

	/** Objekt zur Verwaltung von Anmeldungen auf �nderung der Elemente dieses Typs. */
	private ConfigMutableCollectionSupport _mutableCollectionSupport = new ConfigMutableCollectionSupport(this);

	/** Enth�lt alle Listener, die informiert werden m�ssen sobald ein Objekt dieses Typs erzeugt wurde. */
	private final Set<DynamicObjectCreatedListener> _createdListener = new HashSet<DynamicObjectCreatedListener>();

	/** Alle Listener, die informiert werden sobald sich der Name eines dynamischen Objekt �ndert */
	private final Set<NameChangeListener> _nameChangedListener = new HashSet<NameChangeListener>();

	/** Alle Listener, die informiert werden wollen sobald ein dynamisches Objekt ung�ltig wird. */
	private final Set<InvalidationListener> _invalidationListener = new HashSet<InvalidationListener>();

	private final DynamicObjectTypePublisher _dynamicObjectTypePublisher;


	/**
	 * Konstruktor erstellt den Typ eines dynamischen Objekts.
	 *
	 * @param configurationArea der Konfigurationsbereich des Typs
	 * @param systemObjectInfo  das korrespondierende Objekt f�r die Dateioperationen des Typ-Objekts
	 * @param dynamicObjectTypePublisher
	 */
	public ConfigDynamicObjectType(
			ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo, final DynamicObjectTypePublisher dynamicObjectTypePublisher) {
		super(configurationArea, systemObjectInfo);
		_dynamicObjectTypePublisher = dynamicObjectTypePublisher;
	}

	public PersistenceMode getPersistenceMode() {
		final Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.dynamischerTypEigenschaften"));
		if(data != null) {
			final int mode = data.getUnscaledValue("persistenzModus").intValue();
			return PersistenceMode.parse(mode);
		}
		else {
			throw new IllegalStateException("Persistenzmodus am '" + getPid() + "' kann nicht ermittelt werden.");
		}
	}

	public void setPersistenceMode(final PersistenceMode mode) throws ConfigurationChangeException {
		final AttributeGroup atg = getDataModel().getAttributeGroup("atg.dynamischerTypEigenschaften");
		final Data data = getConfigurationData(atg);
		if(data != null) {
			data.getUnscaledValue("persistenzModus").set(mode.getIntValue());
			setConfigurationData(atg,data);
		}
		else {
			throw new IllegalStateException("Persistenzmodus am '" + getPid() + "' kann nicht gesetzt werden.");
		}
	}

	public void addObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener) {
		synchronized(_createdListener) {
			_createdListener.add(objectCreatedListener);
		}
	}

	public void removeObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener) {
		synchronized(_createdListener) {
			_createdListener.add(objectCreatedListener);
		}
	}

	public void addNameChangeListener(NameChangeListener nameChangeListener) {
		synchronized(_nameChangedListener) {
			_nameChangedListener.add(nameChangeListener);
		}
	}

	public void removeNameChangeListener(NameChangeListener nameChangeListener) {
		synchronized(_nameChangedListener) {
			_nameChangedListener.remove(nameChangeListener);
		}
	}

	public void addInvalidationListener(InvalidationListener invalidationListener) {
		synchronized(_invalidationListener) {
			_invalidationListener.add(invalidationListener);
		}
	}

	public void removeInvalidationListener(InvalidationListener invalidationListener) {
		synchronized(_invalidationListener) {
			_invalidationListener.remove(invalidationListener);
		}
	}

	/**
	 * Informiert alle Listener, dass ein dynamischens Objekt ung�ltig geworden ist. Ist kein Listener angemeldet, wird nichts gemacht. Die Benachrichtung wird
	 * nicht sofort, sondern asynchron durch den AsyncNotificationThread durchgef�hrt.
	 *
	 * @param newInvalidObject Objekt, das ung�ltig geworden ist.
	 */
	public void informInvalidationListener(DynamicObject newInvalidObject) {
		final boolean isLocalDynamicObject = newInvalidObject instanceof ConfigDynamicObject;
		if(isLocalDynamicObject) handleDeletedElement(newInvalidObject);
		synchronized(_invalidationListener) {
			for(InvalidationListener invalidationListener : _invalidationListener) {
				notifyAsync(new ListenerNotificationInfo(invalidationListener, newInvalidObject, ListenerNotificationInfo.ListenerType.INVALITDATION));
			}
			if(isLocalDynamicObject) {
				// Alle Supertypen benachrichtigen
				final List<SystemObjectType> superTyps = getSuperTypes();
				for(SystemObjectType superTyp : superTyps) {
					((ConfigDynamicObjectType)superTyp).informInvalidationListener(newInvalidObject);
				}
			}
		}
	}

	/**
	 * Informiert alle Listener, die sich auf Namen�nderungen angemeldet haben. Ist kein Listener vorhanden wird nichts gemacht. Die Benachrichtung wird nicht
	 * sofort, sondern asynchron durch den AsyncNotificationThread durchgef�hrt.
	 *
	 * @param newNamedObject Objekt, dessen Name ge�ndert wurde.
	 */
	public void informNameChangedListener(DynamicObject newNamedObject) {
		final boolean isLocalDynamicObject = newNamedObject instanceof ConfigDynamicObject;
		synchronized(_nameChangedListener) {

			for(NameChangeListener nameChangeListener : _nameChangedListener) {
				notifyAsync(new ListenerNotificationInfo(nameChangeListener, newNamedObject, ListenerNotificationInfo.ListenerType.NAMECHANGED));
			}

			if(isLocalDynamicObject) {
				// Alle Supertypen benachrichtigen
				final List<SystemObjectType> superTyps = getSuperTypes();
				for(SystemObjectType superTyp : superTyps) {
					((ConfigDynamicObjectType)superTyp).informNameChangedListener(newNamedObject);
				}
			}
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein neues Objekt angelegt wurde und informiert alle Listener, die auf �nderungen dieser Art angemeldet sind. Ist kein
	 * Listener vorhanden wird nichts gemacht. Die Benachrichtung wird nicht sofort, sondern asynchron durch den AsyncNotificationThread durchgef�hrt.
	 *
	 * @param createdObject     Objekt, das neu erzeugt wurde.
	 * @param simulationVariant Simulationsvariante unter der das Objekt erzeugt wurde.
	 */
	public void informCreateListener(DynamicObject createdObject, final short simulationVariant) {
		handleAddedElement(createdObject, simulationVariant);
		synchronized(_createdListener) {
			for(DynamicObjectCreatedListener dynamicObjectCreatedListener : _createdListener) {
				notifyAsync(new ListenerNotificationInfo(dynamicObjectCreatedListener, createdObject, ListenerNotificationInfo.ListenerType.CREATED));
			}
			// Alle Supertypen benachrichtigen
			final List<SystemObjectType> superTyps = getSuperTypes();
			for(SystemObjectType superTyp : superTyps) {
				((ConfigDynamicObjectType)superTyp).informCreateListener(createdObject, simulationVariant);
			}
		}
	}

	/**
	 * Speichert neue Objekte dieses Typs und st��t die Benachrichtigung der Listener an, die sich auf �nderungen der Elemente angemeldet haben.
	 *
	 * @param createdObject     Neu erzeugtes Objekt
	 * @param simulationVariant Simulationsvariante unter der das Objekt erzeugt wurde.
	 */
	private void handleAddedElement(final DynamicObject createdObject, final short simulationVariant) {
		getAllElements().add(createdObject);
		final List<SystemObject> addedElements = new ArrayList<SystemObject>(1);
		addedElements.add(createdObject);
		final List<SystemObject> removedElements = new ArrayList<SystemObject>();
		((ConfigDataModel)getDataModel()).sendCollectionChangedNotification(_mutableCollectionSupport, simulationVariant, addedElements, removedElements);
	}

	/**
	 * Entfernt ein gel�schtes Element dieses Typs und st��t die Benachrichtigung der Listener an, die sich auf �nderungen der Elemente angemeldet haben.
	 *
	 * @param invalidatedObject gel�schtes Objekt
	 */
	public void handleDeletedElement(final DynamicObject invalidatedObject) {
		getAllElements().remove(invalidatedObject);
		final List<SystemObject> addedElements = new ArrayList<SystemObject>(0);
		final List<SystemObject> removedElements = new ArrayList<SystemObject>(1);
		removedElements.add(invalidatedObject);
		((ConfigDataModel)getDataModel()).sendCollectionChangedNotification(
				_mutableCollectionSupport, ((ConfigDynamicObject)invalidatedObject).getSimulationVariant(), addedElements, removedElements
		);
	}

	public void addChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		getAllElements();
		_mutableCollectionSupport.addChangeListener(simulationVariant, changeListener);
	}

	public void removeChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.removeChangeListener(simulationVariant, changeListener);
	}

	public List<SystemObject> getElements(short simulationVariant) {
		final List<SystemObject> allElements = getAllElements();
		final List<SystemObject> elements = new ArrayList<SystemObject>();
		for(SystemObject element : allElements) {
			if(element instanceof ConfigDynamicObject) {
				ConfigDynamicObject configDynamicObject = (ConfigDynamicObject)element;
				if(configDynamicObject.getSimulationVariant() == simulationVariant) elements.add(element);
			}
		}
		return elements;
	}

	private void notifyAsync(final ListenerNotificationInfo listenerNotificationInfo) {
		_dynamicObjectTypePublisher.update(listenerNotificationInfo);
	}
}



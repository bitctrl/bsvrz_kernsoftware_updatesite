/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

import java.util.*;

/**
 * Implementierung des Interfaces für den Typ von dynamischen Objekten.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigDynamicObjectType extends ConfigSystemObjectType implements DynamicObjectType {

	/** Objekt zur Verwaltung von Anmeldungen auf Änderung der Elemente dieses Typs. */
	private ConfigMutableCollectionSupport _mutableCollectionSupport = new ConfigMutableCollectionSupport(this);

	/** Enthält alle Listener, die informiert werden müssen sobald ein Objekt dieses Typs erzeugt wurde. */
	private final Set<DynamicObjectCreatedListener> _createdListener = new HashSet<DynamicObjectCreatedListener>();

	/** Alle Listener, die informiert werden sobald sich der Name eines dynamischen Objekt ändert */
	private final Set<NameChangeListener> _nameChangedListener = new HashSet<NameChangeListener>();

	/** Alle Listener, die informiert werden wollen sobald ein dynamisches Objekt ungültig wird. */
	private final Set<InvalidationListener> _invalidationListener = new HashSet<InvalidationListener>();

	private final DynamicObjectTypePublisher _dynamicObjectTypePublisher;


	/**
	 * Konstruktor erstellt den Typ eines dynamischen Objekts.
	 *
	 * @param configurationArea der Konfigurationsbereich des Typs
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen des Typ-Objekts
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
	 * Informiert alle Listener, dass ein dynamischens Objekt ungültig geworden ist. Ist kein Listener angemeldet, wird nichts gemacht. Die Benachrichtung wird
	 * nicht sofort, sondern asynchron durch den AsyncNotificationThread durchgeführt.
	 *
	 * @param newInvalidObject Objekt, das ungültig geworden ist.
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
	 * Informiert alle Listener, die sich auf Namenänderungen angemeldet haben. Ist kein Listener vorhanden wird nichts gemacht. Die Benachrichtung wird nicht
	 * sofort, sondern asynchron durch den AsyncNotificationThread durchgeführt.
	 *
	 * @param newNamedObject Objekt, dessen Name geändert wurde.
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
	 * Diese Methode wird aufgerufen, wenn ein neues Objekt angelegt wurde und informiert alle Listener, die auf Änderungen dieser Art angemeldet sind. Ist kein
	 * Listener vorhanden wird nichts gemacht. Die Benachrichtung wird nicht sofort, sondern asynchron durch den AsyncNotificationThread durchgeführt.
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
	 * Speichert neue Objekte dieses Typs und stößt die Benachrichtigung der Listener an, die sich auf Änderungen der Elemente angemeldet haben.
	 *
	 * @param createdObject     Neu erzeugtes Objekt
	 * @param simulationVariant Simulationsvariante unter der das Objekt erzeugt wurde.
	 */
	private void handleAddedElement(final DynamicObject createdObject, final short simulationVariant) {
		// Element-Cache aktualisieren
		addElementToCache(createdObject);

		// Benachrichtigungen
		getDataModel().sendCollectionChangedNotification(
				_mutableCollectionSupport,
				simulationVariant,
				Collections.<SystemObject>singletonList(createdObject),
				Collections.<SystemObject>emptyList()
		);
	}

	/**
	 * Entfernt ein gelöschtes Element dieses Typs und stößt die Benachrichtigung der Listener an, die sich auf Änderungen der Elemente angemeldet haben.
	 *
	 * @param invalidatedObject gelöschtes Objekt
	 */
	public void handleDeletedElement(final DynamicObject invalidatedObject) {
		// Element-Cache aktualisieren
		removeElementFromCache(invalidatedObject);

		// Benachrichtigungen
		short simulationVariant = ((ConfigDynamicObject) invalidatedObject).getSimulationVariant();
		getDataModel().sendCollectionChangedNotification(
				_mutableCollectionSupport,
				simulationVariant,
				Collections.<SystemObject>emptyList(),
				Collections.<SystemObject>singletonList(invalidatedObject)
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
		Collection<SystemObject> allElements = getAllElements();
		final List<SystemObject> elements = new ArrayList<SystemObject>(allElements.size());
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



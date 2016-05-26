/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Klasse, die den Zugriff auf Typen von dynamischen Objekten seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafDynamicObjectType extends DafSystemObjectType implements DynamicObjectType {

	/** Alle Listener, die informiert werden müssen, wenn ein neues Objekt angelegt wurde. */
	private final Set<DynamicObjectCreatedListener> _objectCreatedListener = new HashSet<DynamicObjectCreatedListener>();

	/** Alle Listener, die informatiert werden müssen, wenn sich der Name eines Objekts ändert. */
	private final Set<NameChangeListener> _nameChangedListener = new HashSet<NameChangeListener>();

	/** Alle Listener, die informatiert werden müssen, wenn ein dynamisches Objekt ungültig wurde. */
	private final Set<InvalidationListener> _invalidObjectListener = new HashSet<InvalidationListener>();

	/** Objekt zur Verwaltung von Anmeldungen auf Änderungen der Elemente dieses Typs. */
	private DafMutableCollectionSupport _mutableCollectionSupport = new DafMutableCollectionSupport(this);
	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafDynamicObjectType(DafDataModel dataModel) {
		super(dataModel);
		_internType = DYNAMIC_OBJECT_TYPE;
	}

	@Override
	public boolean isConfigurating() {
		return false;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafDynamicObjectType(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long setIds[],
			boolean hasPermanentName
	) {
		super(
				id,
				pid,
				name,
				typId,
				state,
				error,
				dataModel,
				validFromVersionNumber,
				validToVersionNumber,
				responsibleObjectId,
				setIds,
				hasPermanentName
		);
		_internType = DYNAMIC_OBJECT_TYPE;
	}

	public PersistenceMode getPersistenceMode() {
		String persistenceMode;
		try {
			persistenceMode = getConfigurationData(getDataModel().getAttributeGroup("atg.dynamischerTypEigenschaften")).getTextValue(
					"persistenzModus"
			).getText();
		}
		catch(Exception e) {
			throw new IllegalStateException("Persistenzmodus von '" + getPid() + "' kann nicht bestimmt werden", e);
		}
		return PersistenceMode.parse(persistenceMode);
	}

	public void setPersistenceMode(final PersistenceMode mode) throws ConfigurationChangeException {
		final AttributeGroup atg = getDataModel().getAttributeGroup("atg.dynamischerTypEigenschaften");
		final Data data = getConfigurationData(atg);
		if(data != null) {
			data.getUnscaledValue("persistenzModus").set(mode.getIntValue());
			setConfigurationData(atg, data);
		}
		else {
			throw new IllegalStateException("Persistenzmodus am '" + getPid() + "' kann nicht gesetzt werden.");
		}
	}

	public void addObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener) {
		synchronized(_objectCreatedListener) {
			_objectCreatedListener.add(objectCreatedListener);
		}
	}

	public void removeObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener) {
		synchronized(_objectCreatedListener) {
			_objectCreatedListener.remove(objectCreatedListener);
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
		synchronized(_invalidObjectListener) {
			_invalidObjectListener.add(invalidationListener);
		}
	}

	public void removeInvalidationListener(InvalidationListener invalidationListener) {
		synchronized(_invalidObjectListener) {
			_invalidObjectListener.remove(invalidationListener);
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein dynamisches Objekt erzeugt wurde. Gibt es einen Listener, der informiert werden soll, so wird das neue Objekt von
	 * der Konfiguration angefordert.
	 * <p>
	 * Gibt es keinen Listener, so wird nichts gemacht.
	 *
	 * @param objectId Neues Objekt
	 */
	void updateObjectCreated(final long objectId) {
		// Verhindert, dass die Anmeldung eines Listeners und ein Update verzahnt abläuft und der neue Listener nicht informiert wird
		synchronized(_objectCreatedListener) {

			// Die Supertypen informieren. Diese fordern, falls nötig, das neue Objekt an. Da das Objekt nicht geändert wird,
			// stellt dies kein Problem dar.
			final List unknownSuperTyps = getSuperTypes();
			for(Object unknownSuperTyp : unknownSuperTyps) {
				if(unknownSuperTyp instanceof DafDynamicObjectType) {
					DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)unknownSuperTyp;
					dynamicObjectType.updateObjectCreated(objectId);
				}
			}

			if(hasCreateListener()) {
				// Es müssen Listener informiert werden. Das Objekt muss vollständig angefordert werden.
				// Das Objekt anfordern
				final DafDynamicObject newDynamicObject = (DafDynamicObject)_dataModel.getObject(objectId);
				if(newDynamicObject != null) {
					informCreateListener(newDynamicObject);
				}
			}
		}
	}

	/** @return true = Es gibt Listener, die informiert werden wollen, sobald ein neues Objekt angelegt wird; false=sonst */
	private boolean hasCreateListener() {
		final int numerOfListeners;
		synchronized(_objectCreatedListener) {
			numerOfListeners = _objectCreatedListener.size();
		}
		if(numerOfListeners > 0) return true;
		final List superTypes = getSuperTypes();
		for(Object superType : superTypes) {
			if(superType instanceof DafDynamicObjectType) {
				DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)superType;
				if(dynamicObjectType.hasCreateListener()) return true;
			}
		}
		return false;
	}

	/**
	 * Informiert alle entsprechend angemeldeten Listener, dass ein Objekt erzeugt wurde.
	 *
	 * @param dynamicObject Neu erzeugtes dynamisches Objekt
	 */
	private void informCreateListener(final DafDynamicObject dynamicObject) {
		final List<DynamicObjectCreatedListener> objectCreatedListenersCopy;
		synchronized(_objectCreatedListener) {
			objectCreatedListenersCopy = new ArrayList<DynamicObjectCreatedListener>(_objectCreatedListener);
		}
		for(DynamicObjectCreatedListener dynamicObjectCreatedListener : objectCreatedListenersCopy) {
			dynamicObjectCreatedListener.objectCreated(dynamicObject);
		}
	}

	/**
	 * Setze bei einem Objekt die Zeit wann es ungültig wurde und setzt das Objekt auch die Gültigkeit auf "ungültig".
	 * <p>
	 * <p>
	 * Dies wird ausgeführt wenn das Objekt im Cache vorhanden ist oder ein Listener auf diese Änderungen angemeldet ist.
	 * <p>
	 * Ist dies nicht der Fall, wird nichts gemacht. (Es gibt niemanden, der sich für die Änderungen interssieren würde)
	 *
	 * @param objectId      Id des Objekts, dessen Status auf ungültig gesetzt werden soll und dessen "Nicht mehr gültig ab" Zeit aktualisiert werden soll.
	 * @param notValidSince Zeitpunkt, an dem das Objekt ungültig wurde.
	 */
	void updateNotValidSince(final long objectId, final long notValidSince) {
		// Verhindert, das beim hinzufügen eines Listeners ein Update stattfindet und der neue Listener nicht benachrichtigt wird
		synchronized(_invalidObjectListener) {

			final DafDynamicObject setInvalidObject;
			if(hasInvalidListeners()) {
				// Es gibt Listener, die auf die Gültigkeit von Objekten dieses Typs angemeldet sind. Das Objekt wird angefordert (und zwar von der Konfiguration, falls es sich nicht im Cache befindet)
				setInvalidObject = (DafDynamicObject)_dataModel.getObject(objectId);
			}
			else {
				// Es gibt zwar keine Listener, aber wenn sich das Objekt im Cache befindet, dann muss es aktualisiert werden
				setInvalidObject = (DafDynamicObject)_dataModel.getObjectFromCache(objectId);

				// Das Objekt befindet sich nicht im Cache, also muss nichts auf den aktuellen Stand gebracht werden
				if(setInvalidObject == null) {
					return;
				}
			}

			// Das Objekt ist ungültig geworden -> Objekt auf den neusten Stand bringen und die Listener informieren (sind keine Listener angemeldet, wird niemand informiert)

			// An dieser Stelle kann ein Supertyp auf das Objekt zugreifen. Dieser Wert wird aber schon vom direkten Typen gesetzt.
			if(setInvalidObject.getType() == this) {
				setInvalidObject.storeNotValidSince(notValidSince);
			}
			informInvalidListeners(setInvalidObject);

			// Die Supertypen informieren. Dies geschieht erst hier, damit der direkte Typ die Chance hat den ungültig Wert zu setzen.
			// Tut er das nicht(keine Listener angemeldet, Objekt nicht im Cache), so wird es ein Supertyp machen (falls nötig).
			final List unknownSuperTyps = getSuperTypes();
			for(Object unknownSuperTyp : unknownSuperTyps) {
				if(unknownSuperTyp instanceof DafDynamicObjectType) {
					DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)unknownSuperTyp;
					dynamicObjectType.updateNotValidSince(objectId, notValidSince);
				}
			}
		}
	}

	/**
	 * Informiert alle angemeldeten Listener, dass ein Objekt ungültig wurde. Sind keine Listener angemeldet, so wird nichts gemacht.
	 *
	 * @param invalidObject dynamisches Objekt, das ungültig geworden ist.
	 */
	private void informInvalidListeners(final DafDynamicObject invalidObject) {
		final List<InvalidationListener> invalidObjectListenersCopy;
		synchronized(_invalidObjectListener) {
			invalidObjectListenersCopy = new ArrayList<InvalidationListener>(_invalidObjectListener);
		}
		for(InvalidationListener invalidationListener : invalidObjectListenersCopy) {
			invalidationListener.invalidObject(invalidObject);
		}
	}

	/**
	 * Prüft, ob es einen Listener gibt, der für Objekte von diesem Typ informiert werden möchte, falls ein Objekt ungültig wird.
	 *
	 * @return true = ja es gibt einen Listener
	 */
	private boolean hasInvalidListeners() {
		final int numerOfListeners;
		synchronized(_invalidObjectListener) {
			numerOfListeners = _invalidObjectListener.size();
		}
		if(numerOfListeners > 0) return true;
		final List superTypes = getSuperTypes();
		for(Object superType : superTypes) {
			if(superType instanceof DafDynamicObjectType) {
				DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)superType;
				if(dynamicObjectType.hasInvalidListeners()) return true;
			}
		}
		return false;
	}

	/**
	 * Setzt bei einem Objekt den Namen. Dies wird ausgeführt wenn das Objekt im Cache vorhanden ist oder ein Listener darauf angemeldet ist.
	 * <p>
	 * Wenn der Name gesetzt wurde, werden alle Listener, die sich für Namensänderungen des Typs interssieren, informiert.
	 *
	 * @param objectId Id des Objekts, dessen Name aktualisiert werden soll
	 * @param newName  aktueller Name
	 */
	void updateName(final long objectId, final String newName) {

		// Das synchronized verhindert, das ein Listener angemeldet wird während gerade ein update Ausgeführt wird. Der Listener könnte dann nicht informiert werden
		synchronized(_nameChangedListener) {
			final DafDynamicObject changeNameDynamicObject;
			if(hasNameListeners()) {
				// Es gibt Listener, die auf Namensänderungen dieses Typs angemeldet sind. Das Objekt wird angefordert (und zwar von der Konfiguration, falls es sich nicht im Cache befindet)
				changeNameDynamicObject = (DafDynamicObject)_dataModel.getObject(objectId);
			}
			else {
				// Es gibt zwar keine Listener, aber wenn sich das Objekt im Cache befindet, dann muss es aktualisiert werden
				changeNameDynamicObject = (DafDynamicObject)_dataModel.getObjectFromCache(objectId);

				// Das Objekt befindet sich nicht im Cache, also muss nichts auf den aktuellen Stand gebracht werden
				if(changeNameDynamicObject == null) {
					return;
				}
			}

			// Den Namen des Objekts auf den neusten Stand bringen und die Listener informieren (sind keine Listener angemeldet, wird niemand informiert)

			// An dieser Stelle kann es sich um einen Supertypen handeln. Der neue Wert wird vom direkten Typen gesetzt. Die Supertypen
			// müssen den Wert nicht noch einmal setzen
			if(changeNameDynamicObject.getType() == this) {
				changeNameDynamicObject.storeName(newName);
			}

			informNameListeners(changeNameDynamicObject);

			// Die Supertypen informieren. Dies geschieht erst hier, damit der direkte Typ die Möglichkeit hat den Namen richtig zu setzen.
			final List unknownSuperTyps = getSuperTypes();
			for(Object unknownSuperTyp : unknownSuperTyps) {
				if(unknownSuperTyp instanceof DafDynamicObjectType) {
					DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)unknownSuperTyp;
					dynamicObjectType.updateName(objectId, newName);
				}
			}
		}
	}

	/** @return true = Es gibt Listener, die auf Namensänderungen angemeldet sind */
	private boolean hasNameListeners() {
		final int numerOfListeners;
		synchronized(_nameChangedListener) {
			numerOfListeners = _nameChangedListener.size();
		}
		if(numerOfListeners > 0) return true;
		final List superTypes = getSuperTypes();
		for(Object superType : superTypes) {
			if(superType instanceof DafDynamicObjectType) {
				DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)superType;
				if(dynamicObjectType.hasNameListeners()) return true;
			}
		}
		return false;
	}

	/**
	 * Informiert alle Listener, die auf Namensänderungen dieses Typs angemeldet sind, dass sich der Name eines Objekt geändert hat. Sind keine Listener
	 * angemeldet, wird nichts gemacht.
	 *
	 * @param dynamicObjectWithNewName Objekt, dessen Name geändert wurde
	 */
	private void informNameListeners(final DafDynamicObject dynamicObjectWithNewName) {
		final List<NameChangeListener> nameChangeListenersCopy;
		synchronized(_nameChangedListener) {
			nameChangeListenersCopy = new ArrayList<NameChangeListener>(_nameChangedListener);
		}
		for(NameChangeListener nameChangeListener : nameChangeListenersCopy) {
			nameChangeListener.nameChanged(dynamicObjectWithNewName);
		}
	}

	public void addChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.addChangeListener(simulationVariant, changeListener);
	}

	public void removeChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.removeChangeListener(simulationVariant, changeListener);
	}

	public List<SystemObject> getElements(short simulationVariant) {
		return _mutableCollectionSupport.getElements(simulationVariant);
	}

	/**
	 * Leitet die Aktualisierungsnachrichten bzgl. Änderungen von dynamischen Mengen und dynamischen Typen an das entsprechende Verwaltungsobjekt weiter.
	 * @param simVariant Simulationsvariante der Änderung
	 * @param addedElements Hinzugefügte Elemente der dynamischen Zusammenstellung
	 * @param removedElements Entfernte Elemente der dynamischen Zusammenstellung
	 */
	public void collectionChanged(final short simVariant, final List<SystemObject> addedElements, final List<SystemObject> removedElements) {
		_mutableCollectionSupport.collectionChanged(simVariant, addedElements, removedElements);
	}

	public List<SystemObject> getElements() {
		DataModel dataModel = getDataModel();
		if (dataModel instanceof DafDataModel) {
			DafDataModel model = (DafDataModel) dataModel;
			ClientDavInterface connection = model.getConnection();
			if (connection instanceof ClientDavConnection) {
				ClientDavConnection clientDavConnection = (ClientDavConnection) connection;
				short simulationVariant = clientDavConnection.getClientDavParameters().getSimulationVariant();
				List<SystemObject> elements = getElements(simulationVariant);
				for(SystemObject element : elements) {
					// Systemobjekte cachen falls erforderlich
					if(element instanceof DafSystemObject) {
						DafSystemObject dafSystemObject = (DafSystemObject) element;
						model.updateInternalDataStructure(dafSystemObject, true);
					}
				}
				return elements;
			}
		}
		return super.getElements();
	}

	public List<SystemObject> getObjects() {
		return getElements();
	}

}

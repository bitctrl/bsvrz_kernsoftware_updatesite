/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung, Aachen
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
 * Klasse, die den Zugriff auf Typen von dynamischen Objekten seitens der Datenverteiler-Applikationsfunktionen erm�glicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13141 $
 */
public class DafDynamicObjectType extends DafSystemObjectType implements DynamicObjectType {

	/** Alle Listener, die informiert werden m�ssen, wenn ein neues Objekt angelegt wurde. */
	private final Set<DynamicObjectCreatedListener> _objectCreatedListener = new HashSet<DynamicObjectCreatedListener>();

	/** Alle Listener, die informatiert werden m�ssen, wenn sich der Name eines Objekts �ndert. */
	private final Set<NameChangeListener> _nameChangedListener = new HashSet<NameChangeListener>();

	/** Alle Listener, die informatiert werden m�ssen, wenn ein dynamisches Objekt ung�ltig wurde. */
	private final Set<InvalidationListener> _invalidObjectListener = new HashSet<InvalidationListener>();

	/** Objekt zur Verwaltung von Anmeldungen auf �nderungen der Elemente dieses Typs. */
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
	 * <p/>
	 * Gibt es keinen Listener, so wird nichts gemacht.
	 *
	 * @param objectId Neues Objekt
	 */
	void updateObjectCreated(final long objectId) {
		// Verhindert, dass die Anmeldung eines Listeners und ein Update verzahnt abl�uft und der neue Listener nicht informiert wird
		synchronized(_objectCreatedListener) {

			// Die Supertypen informieren. Diese fordern, falls n�tig, das neue Objekt an. Da das Objekt nicht ge�ndert wird,
			// stellt dies kein Problem dar.
			final List unknownSuperTyps = getSuperTypes();
			for(Object unknownSuperTyp : unknownSuperTyps) {
				if(unknownSuperTyp instanceof DafDynamicObjectType) {
					DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)unknownSuperTyp;
					dynamicObjectType.updateObjectCreated(objectId);
				}
			}

			if(hasCreateListener()) {
				// Es m�ssen Listener informiert werden. Das Objekt muss vollst�ndig angefordert werden.
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
	 * Setze bei einem Objekt die Zeit wann es ung�ltig wurde und setzt das Objekt auch die G�ltigkeit auf "ung�ltig".
	 * <p/>
	 * <p/>
	 * Dies wird ausgef�hrt wenn das Objekt im Cache vorhanden ist oder ein Listener auf diese �nderungen angemeldet ist.
	 * <p/>
	 * Ist dies nicht der Fall, wird nichts gemacht. (Es gibt niemanden, der sich f�r die �nderungen interssieren w�rde)
	 *
	 * @param objectId      Id des Objekts, dessen Status auf ung�ltig gesetzt werden soll und dessen "Nicht mehr g�ltig ab" Zeit aktualisiert werden soll.
	 * @param notValidSince Zeitpunkt, an dem das Objekt ung�ltig wurde.
	 */
	void updateNotValidSince(final long objectId, final long notValidSince) {
		// Verhindert, das beim hinzuf�gen eines Listeners ein Update stattfindet und der neue Listener nicht benachrichtigt wird
		synchronized(_invalidObjectListener) {

			final DafDynamicObject setInvalidObject;
			if(hasInvalidListeners()) {
				// Es gibt Listener, die auf die G�ltigkeit von Objekten dieses Typs angemeldet sind. Das Objekt wird angefordert (und zwar von der Konfiguration, falls es sich nicht im Cache befindet)
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

			// Das Objekt ist ung�ltig geworden -> Objekt auf den neusten Stand bringen und die Listener informieren (sind keine Listener angemeldet, wird niemand informiert)

			// An dieser Stelle kann ein Supertyp auf das Objekt zugreifen. Dieser Wert wird aber schon vom direkten Typen gesetzt.
			if(setInvalidObject.getType() == this) {
				setInvalidObject.storeNotValidSince(notValidSince);
			}
			informInvalidListeners(setInvalidObject);

			// Die Supertypen informieren. Dies geschieht erst hier, damit der direkte Typ die Chance hat den ung�ltig Wert zu setzen.
			// Tut er das nicht(keine Listener angemeldet, Objekt nicht im Cache), so wird es ein Supertyp machen (falls n�tig).
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
	 * Informiert alle angemeldeten Listener, dass ein Objekt ung�ltig wurde. Sind keine Listener angemeldet, so wird nichts gemacht.
	 *
	 * @param invalidObject dynamisches Objekt, das ung�ltig geworden ist.
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
	 * Pr�ft, ob es einen Listener gibt, der f�r Objekte von diesem Typ informiert werden m�chte, falls ein Objekt ung�ltig wird.
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
	 * Setzt bei einem Objekt den Namen. Dies wird ausgef�hrt wenn das Objekt im Cache vorhanden ist oder ein Listener darauf angemeldet ist.
	 * <p/>
	 * Wenn der Name gesetzt wurde, werden alle Listener, die sich f�r Namens�nderungen des Typs interssieren, informiert.
	 *
	 * @param objectId Id des Objekts, dessen Name aktualisiert werden soll
	 * @param newName  aktueller Name
	 */
	void updateName(final long objectId, final String newName) {

		// Das synchronized verhindert, das ein Listener angemeldet wird w�hrend gerade ein update Ausgef�hrt wird. Der Listener k�nnte dann nicht informiert werden
		synchronized(_nameChangedListener) {
			final DafDynamicObject changeNameDynamicObject;
			if(hasNameListeners()) {
				// Es gibt Listener, die auf Namens�nderungen dieses Typs angemeldet sind. Das Objekt wird angefordert (und zwar von der Konfiguration, falls es sich nicht im Cache befindet)
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
			// m�ssen den Wert nicht noch einmal setzen
			if(changeNameDynamicObject.getType() == this) {
				changeNameDynamicObject.storeName(newName);
			}

			informNameListeners(changeNameDynamicObject);

			// Die Supertypen informieren. Dies geschieht erst hier, damit der direkte Typ die M�glichkeit hat den Namen richtig zu setzen.
			final List unknownSuperTyps = getSuperTypes();
			for(Object unknownSuperTyp : unknownSuperTyps) {
				if(unknownSuperTyp instanceof DafDynamicObjectType) {
					DafDynamicObjectType dynamicObjectType = (DafDynamicObjectType)unknownSuperTyp;
					dynamicObjectType.updateName(objectId, newName);
				}
			}
		}
	}

	/** @return true = Es gibt Listener, die auf Namens�nderungen angemeldet sind */
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
	 * Informiert alle Listener, die auf Namens�nderungen dieses Typs angemeldet sind, dass sich der Name eines Objekt ge�ndert hat. Sind keine Listener
	 * angemeldet, wird nichts gemacht.
	 *
	 * @param dynamicObjectWithNewName Objekt, dessen Name ge�ndert wurde
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
	 * Leitet die Aktualisierungsnachrichten bzgl. �nderungen von dynamischen Mengen und dynamischen Typen an das entsprechende Verwaltungsobjekt weiter.
	 * @param simVariant Simulationsvariante der �nderung
	 * @param addedElements Hinzugef�gte Elemente der dynamischen Zusammenstellung
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

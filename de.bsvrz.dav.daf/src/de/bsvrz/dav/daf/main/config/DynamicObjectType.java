/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Definiert Gemeinsamkeiten aller dynamischen Objekt-Typen. Dynamische Typen haben die Eigenschaft, da� beim Erzeugen bzw. L�schen von Objekten dieses Typs
 * diese �nderungen sofort g�ltig werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8551 $
 */
public interface DynamicObjectType extends SystemObjectType, MutableCollection {

	/**
	 * Bestimmt den Persistenzmodus von dynamischen Objekten dieses Typs.
	 *
	 * @return Persistenzmodus von dynamischen Objekten dieses Typs
	 */
	public PersistenceMode getPersistenceMode();

	/**
	 * Setzt den Persistenzmodus von dynamischen Objekten dieses Typs.
	 *
	 * @param mode neuer Persistenzmodus von dynamischen Objekten dieses Typs
	 * @throws ConfigurationChangeException Fehler bei der Konfigurations�nderung
	 */
	public void setPersistenceMode(PersistenceMode mode) throws ConfigurationChangeException;

	/**
	 * F�gt einen Listener zu einer Datenstruktur hinzu. Sobald ein neues dynamisches Objekt mit diesem Typ in der Konfiguration erzeugt wird, wird der Listener
	 * informiert.
	 * <p/>
	 * Der mehrfache Aufruf der Methode mit dem identischen Objekt(==) <code>objectCreatedListener</code> f�gt das Objekt nur einmal der Datenstruktur hinzu.
	 *
	 * @param objectCreatedListener Objekt, das informiert wird sobald ein dynamisches Objekt mit diesem Typ erzeugt wird.
	 */
	public void addObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener);

	/**
	 * Entfernt den Listener aus der Datenstruktur. Nach Aufruf dieser Methode findet keine Benachrichtigung �ber neue Objekte mehr statt.
	 *
	 * @param objectCreatedListener Objekt, das aus der Datenstruktur entfernt werden soll.
	 *
	 * @see #addObjectCreationListener
	 */
	public void removeObjectCreationListener(DynamicObjectCreatedListener objectCreatedListener);

	/**
	 * F�gt einen Listener zu einer Datenstruktur hinzu. Sobald der Name eines dynamischen Objekts dieses Typs ge�ndert wird, wird der Listener informiert.
	 * <p/>
	 * Ein mehrfacher Aufruf dieser Methode mit dem selben Objekten f�gt das �bergene Objekt nur einmal der Datenstruktur hinzu.
	 *
	 * @param nameChangeListener Objekt, das informiert wird, sobald der Name eines Objekts von diesem Typ ge�nder wird.
	 */
	void addNameChangeListener(NameChangeListener nameChangeListener);

	/**
	 * Entfernt den Listener aus der Datenstruktur. Das Objekt wird nicht mehr informiert wenn sich der Name eines Objekts von diesem Typ �ndert.
	 *
	 * @param nameChangeListener Listener, der entfernt wird.
	 *
	 * @see #addNameChangeListener
	 */
	void removeNameChangeListener(NameChangeListener nameChangeListener);

	/**
	 * F�gt einen Listener zu einer Datenstruktur hinzu. Sobald ein dynamisches Objekt dieses Typs ung�ltig wird, wird der Listener informiert.
	 * <p/>
	 * Ein mehrfacher Aufruf dieser Methode mit dem selben Objekten f�gt das �bergene Objekt nur einmal der Datenstruktur hinzu.
	 *
	 * @param invalidationListener Objekt, das informiert wird, sobald ein Objekt des Typs ung�ltig wird.
	 */
	void addInvalidationListener(InvalidationListener invalidationListener);

	/**
	 * Entfernt den Listener aus der Datenstruktur. Das Objekt wird nicht mehr informiert wenn ein Objekt dieses Typs ung�ltig wird.
	 *
	 * @param invalidationListener Listener, der entfernt werden soll.
	 */
	void removeInvalidationListener(InvalidationListener invalidationListener);


	/** Aufz�hlung f�r die verschiedenen Persistenzmodi der dynamischen Objekte eines Typs */
	enum PersistenceMode {

		/** Dynamische Objekte werden nicht persistent gespeichert. */
		TRANSIENT_OBJECTS(1, "transient"),

		/** Dynamische Objekte werden persistent gespeichert. */
		PERSISTENT_OBJECTS(2, "persistent"),

		/** Dynamische Objekte werden persistent gespeichert und beim Neustart auf ung�ltig gesetzt. */
		PERSISTENT_AND_INVALID_ON_RESTART(3, "persistentUndUng�ltigNachNeustart");

		private final int _value;

		private final String _name;

		/**
		 * Gibt den Integer-Wert zur�ck, durch den die PersistenceMode im Datenmodell repr�sentiert wird.
		 * @return Unskalierter Integer-Wert des Attributs im Datenmodell
		 */
		public int getIntValue() {
			return _value;
		}

		/**
		 * Gibt den Text-Wert zur�ck, durch den die PersistenceMode im Datenmodell repr�sentiert wird.
		 * @return Text-Wert des Attributes im Datenmodell
		 */
		public String getStatusName() {
			return _name;
		}

		PersistenceMode(final int value, final String name) {
			_value = value;
			_name = name;
		}

		/**
		 * Erstellt ein PersistenceMode aus einem String.
		 * @param persistenceMode Eingabe-String
		 * @return PersistenceMode
		 * @throws IllegalArgumentException Falls der String keinem Statuswert entspricht.
		 */
		public static PersistenceMode parse(final String persistenceMode) {
			if(persistenceMode.equals(TRANSIENT_OBJECTS.getStatusName())) {
				return TRANSIENT_OBJECTS;
			}
			else if(persistenceMode.equals(PERSISTENT_OBJECTS.getStatusName())) {
				return PERSISTENT_OBJECTS;
			}
			else if(persistenceMode.equals(PERSISTENT_AND_INVALID_ON_RESTART.getStatusName())) {
				return PERSISTENT_AND_INVALID_ON_RESTART;
			}
			else {
				throw new IllegalArgumentException("persistenceMode hat einen ung�ltigen Wert: " + persistenceMode);
			}
		}

		/**
		 * Erstellt ein PersistenceMode aus einem Integer-Wert.
		 * @param persistenceMode Eingabe-Wert
		 * @return PersistenceMode
		 * @throws IllegalArgumentException Falls der eingabewert nicht zugeordnet werden kann
		 */
		public static PersistenceMode parse(final int persistenceMode) {
			switch(persistenceMode) {
				case 1:
					return PersistenceMode.TRANSIENT_OBJECTS;
				case 2:
					return PersistenceMode.PERSISTENT_OBJECTS;
				case 3:
					return PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART;
				default:
					throw new IllegalArgumentException("persistenceMode hat einen ung�ltigen Wert: " + persistenceMode);
			}
		}
	}

	/** Listener, der eine Methode zur Verf�gung stellt, die aufgerufen wird sobald sich der Name eines Objekts �ndert. */
	public static interface NameChangeListener {

		/**
		 * Wird aufgerufen, wenn sich der Name eines dynamischen Objekts �ndert.
		 *
		 * @param newObject dynamisches Objekte, dessen Name sich ge�ndert hat.
		 */
		void nameChanged(DynamicObject newObject);
	}

	/** Listener, dessen Methode benutzt wird wenn ein neues dynamisches Objekt angelegt wurde. */
	public static interface DynamicObjectCreatedListener {

		/**
		 * Diese Methode wird aufgerufen, wenn ein neues Objekt erstellt wurde.
		 *
		 * @param createdObject Objekt, das neu erstellt wurde.
		 */
		public void objectCreated(DynamicObject createdObject);
	}
}


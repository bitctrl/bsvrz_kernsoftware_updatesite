/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

import java.util.Collections;
import java.util.List;

/**
 * Diese Klasse bildet eine typeDefinition ab, die in der K2S.DTD definiert ist
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SystemObjectTypeProperties extends ConfigurationObjectProperties {

	private boolean _objectNamesPermanent = false;

	private PersistenceMode _persistenceMode = PersistenceMode.UNDEFINED;

	/** Speichert alle Pids, aller Objekte, die das Objekt erweitern. Hat das Array die Größe 0, so ist kein Wert vorhanden (gehört zu erweitert) */
	private String[] _extendedPids = new String[0];

	/** Konfigurierend, ja/nein oder "" falls der Wert nicht gesetzt wurde (gehört zu basis) */
	private boolean _configuring;

	/**
	 * Speichert alle Attributgruppen und "Menge" die gelesen wurden. Das Element, das als erste gelesen wurde steht an Position [0]. Wurden keinen Elemten
	 * gelesen, ist das Array leer.
	 */
	private Object[] _atgAndSet = new Object[0];

	/** "defaultParameter"-Objekte */
	private ConfigurationDefaultParameter[] _defaultParameters = new ConfigurationDefaultParameter[0];

	private List<String> _transactions;

	public SystemObjectTypeProperties(String name, String pid, long id, String typePid, SystemObjectInfo info) {
		super(name, pid, id, typePid, info);
	}

	/**
	 * objektNamenPermanent, ja/nein
	 *
	 * @param objectNamesPermanent ja/nein, <code>null</code> wird als "nein" interpretiert
	 */
	public void setObjectNamesPermanent(String objectNamesPermanent) {
		if("ja".equals(objectNamesPermanent)) {
			_objectNamesPermanent = true;
		}
		else if("nein".equals(objectNamesPermanent)) {
			_objectNamesPermanent = false;
		}
		else {
			throw new IllegalArgumentException("Unbkannter Paramter: " + objectNamesPermanent);
		}
	}

	public void setObjectNamesPermanent(boolean objectNamesPermanent) {
		_objectNamesPermanent = objectNamesPermanent;
	}

	/**
	 * objektNamenPermanent, ja/nein
	 *
	 * @return ja = true; nein = false
	 */
	public boolean getObjectNamesPermanent() {
		return _objectNamesPermanent;
	}

	/**
	 * Parameter "persistenzMode"
	 *
	 * @param persistenceMode transient/persistent/persistentUndUngültigNachNeustart. Wird "" übergeben, so wird dies als "undefiniert" interpretiert und gesetzt.
	 */
	public void setPersistenceMode(String persistenceMode) {
		if(!"".equals(persistenceMode)) {
			if(persistenceMode.equals("transient")) {
				_persistenceMode = PersistenceMode.TRANSIENT_OBJECTS;
			}
			else if(persistenceMode.equals("persistent")) {
				_persistenceMode = PersistenceMode.PERSISTENT_OBJECTS;
			}
			else if(persistenceMode.equals("persistentUndUngültigNachNeustart")) {
				_persistenceMode = PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART;
			}
			else {
				throw new IllegalArgumentException("Unbekannter Persistenzmodus: " + persistenceMode);
			}
		}
		else {
			// Der Modus wurde nicht gesetzt
			_persistenceMode = PersistenceMode.UNDEFINED;
		}
	}

	/**
	 * Parameter "persistenzMode"
	 *
	 * @param mode s.o.
	 */
	public void setPersistenceMode(PersistenceMode mode) {
		_persistenceMode = mode;
	}

	/**
	 * Parameter "persistenzMode"
	 *
	 * @return Der gesetzte Persistenzmodus, wurde kein Modus gesetzt (oder "") wird PersistenceMode.UNDEFINED zurückgegeben
	 */
	public PersistenceMode getPersistenceMode() {
		return _persistenceMode;
	}

	/**
	 * Element "erweitert"
	 *
	 * @param extendedPids Alle Pids von Objekten, die im Element "erweiter" angegeben wurden
	 */
	public void setExtendedPids(String[] extendedPids) {
		_extendedPids = extendedPids;
	}

	/**
	 * Alle Pids, die im Element "erweitert" aufgezählt wurden
	 *
	 * @return Array mit Pids oder ein leeres Array
	 */
	public String[] getExtendedPids() {
		return _extendedPids;
	}

	/**
	 * Element "basis"
	 *
	 * @param configuring ja = "konfigurierend=ja"; nein = "konfigurierend=nein"
	 */
	public void setConfiguring(String configuring) {
		if("ja".equals(configuring)) {
			_configuring = true;
		}
		else if("nein".equals(configuring)) {
			_configuring = false;
		}
		else {
			throw new IllegalArgumentException("Konfigurierend wird mit einem ungültigen Wert gesetzt: " + configuring + " gültig sind \"ja\" und \"nein\"");
		}
	}

	/**
	 * Attributwert "konfigurierend" des Elements "basis"
	 *
	 * @return true = ja
	 */
	public boolean getConfiguring() {
		return _configuring;
	}

	/**
	 * Element "basis"
	 *
	 * @param configuring true = "konfigurierend=ja"; false = "konfigurierend=nein"
	 */
	public void setConfiguring(boolean configuring) {
		_configuring = configuring;
	}

	/**
	 * Speichert Attributgruppe und "Menge". Das Array enthält Objekte vom Typ "String", jeder String-Eintrag spiegelt eine Attributgruppe wieder und Objekte vom
	 * Typ ConfigurationSet,dies entspricht einem Eintrag vom Typ "Menge". Das erste Element, das eingelesen wurden, steht an Position [0]. Wurden keine Elemente
	 * gelesen, so ist das Array leer.
	 *
	 * @return Array, das Elemente vom Typ String oder ConfigurationSet enthält. Sind keine Elemente vorhanden, ist das Array leer
	 */
	public Object[] getAtgAndSet() {
		return _atgAndSet;
	}

	/**
	 * @param atgAndSet Speichert Attributgruppe und "Menge". Das Array enthält Objekte vom Typ "String", jeder String-Eintrag spiegelt eine Attributgruppe wieder
	 *                  und Objekte vom Typ ConfigurationSet,dies entspricht einem Eintrag vom Typ "Menge". Das erste Element, das eingelesen wurden, steht an
	 *                  Position [0]. Wurden keine Elemente gelesen, so ist das Array leer.
	 */
	public void setAtgAndSet(Object[] atgAndSet) {
		if(atgAndSet != null) {
			_atgAndSet = atgAndSet;
		}
	}

	/**
	 * Diese Methode gibt die Default-Parameter dieses Objekt-Typs zurück.
	 *
	 * @return die Default-Parameter dieses Objekt-Typs
	 */
	public ConfigurationDefaultParameter[] getDefaultParameters() {
		return _defaultParameters;
	}

	/**
	 * Setzt die Default-Parameter dieses Objekt-Typs.
	 *
	 * @param defaultParameters die Default-Parameter dieses Objekt-Typs
	 */
	public void setDefaultParameters(final ConfigurationDefaultParameter[] defaultParameters) {
		_defaultParameters = defaultParameters;
	}

	/** Transaktionen. Dieses Feld wird derzeit nur beim Export benutzt, da beim Import ATGs und Transaktionen gleich behandelt werden. */
	public void setTransactions(final List<String> transactions) {
		_transactions = transactions;
	}

	public List<String> getTransactions() {
		if(_transactions == null) return Collections.emptyList();
		return Collections.unmodifiableList(_transactions);
	}
}

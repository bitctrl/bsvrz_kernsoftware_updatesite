/*
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

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.main.consistencycheck.RelaxedModelChanges;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces {@link IntegerValueState} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigIntegerValueState extends ConfigConfigurationObject implements IntegerValueState {
	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Speichert die Eigenschaften des Wertezustands.
	 */
	private IntegerValueStateValues _values;

	/**
	 * Konstruktor eines Zustandes eines Ganzzahl-Attribut-Typs.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Zustands
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigIntegerValueState(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	@Override
	public void setName(final String name) throws ConfigurationChangeException {
		// Sind im fehlerhafterweise als änderbar markiert, dürfen aber nur über eine "unversionierte Datenmodelländerung"
		// umbenannt werden
		RelaxedModelChanges relaxedModelChanges = RelaxedModelChanges.getInstance(getDataModel());
		if(relaxedModelChanges.allowChangeValueName(this)){
			super.setName(name);
		}
		else {
			throw new ConfigurationChangeException(
					"Der Name des Objekts (" + getNameOrPidOrId() + ") darf nur über unversionierte Datenmodelländerungen geändert werden. " +
							"Bitte kb.metaModellGlobal in Mindestversion 16 installieren."
			);
		}
	}

	public long getValue() {
		return getIntegerValueStateValues().getValue();
	}

	/**
	 * Gibt das Objekt zurück, welches die Eigenschaften dieses Wertezustands enthält. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Eigenschaften des Wertezustands
	 */
	private synchronized IntegerValueStateValues getIntegerValueStateValues() {
		if (_values == null) {
			_values = new IntegerValueStateValues();
		}
		return _values;
	}

	/**
	 * Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. Erbende Klassen müssen diese
	 * Funktion überschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		_values = null;
		super.invalidateCache();
	}

	/**
	 * Diese Klasse liest die Informationen für diesen Wertezustand mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData ConfigurationData}, da dort Informationen benötigt
	 * werden, die erst hier zur Verfügung gestellt werden.
	 */
	private class IntegerValueStateValues {
		/**
		 * der Wert des Zustands eines Ganzzahl-Attributs
		 */
		private long _value;

		/**
		 * Konstruktor, der die Eigenschaften dieses Wertezustands aus einem konfigurierenden Datensatz ausliest
		 */
		public IntegerValueStateValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.werteZustandsEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_value = deserializer.readLong();

				in.close();
			} catch (Exception ex) {
				final String errorMessage = "Die Eigenschaften des Wertezustands " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt den Wert des Zustands eines Ganzzahl-Attributs zurück.
		 *
		 * @return den Wert des Zustands eines Ganzzahl-Attributs
		 */
		public long getValue() {
			return _value;
		}
	}
}

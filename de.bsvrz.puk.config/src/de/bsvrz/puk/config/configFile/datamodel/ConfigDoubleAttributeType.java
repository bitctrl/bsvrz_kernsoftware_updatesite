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

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces {@link DoubleAttributeType} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigDoubleAttributeType extends ConfigAttributeType implements DoubleAttributeType {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert die Eigenschaften dieses AttributTyps. */
	private DoubleAttributeTypeValues _values;

	/**
	 * Konstruktor eines Fließkomma-Attribut-Typen.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Attribut-Typen
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigDoubleAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public String getUnit() {
		return getDoubleAttributeTypeValues().getUnit();
	}

	public byte getAccuracy() {
		return getDoubleAttributeTypeValues().getAccuracy();
	}

	/**
	 * Gibt das Objekt zurück, welches die Eigenschaften dieses AttributTyps enthält. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Eigenschaften des AttributTyps
	 */
	private synchronized DoubleAttributeTypeValues getDoubleAttributeTypeValues() {
		if(_values == null) {
			_values = new DoubleAttributeTypeValues();
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
	 * Diese Klasse liest die Informationen für diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData ConfigurationData}, da dort Informationen benötigt
	 * werden, die erst hier zur Verfügung gestellt werden.
	 */
	private class DoubleAttributeTypeValues {

		/** Die Einheit dieses Attribut-Typs. */
		private String _unit;

		/** Die Genauigkeit dieses Fließkommazahl-Attribut-Typs. */
		private byte _accuracy;

		/** Konstruktor, der die Eigenschaften dieses AttributTypen aus einem konfigurierenden Datensatz ausliest. */
		public DoubleAttributeTypeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.kommazahlAttributTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_unit = deserializer.readString(64);
				_accuracy = deserializer.readByte();

				in.close();
			}
			catch(Exception ex) {
				final String errorMessage =
						"Die Fließkommazahl-AttributTypEigenschaften des AttributTyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die Einheit dieses Attribut-Typs zurück.
		 *
		 * @return die Einheit dieses Attribut-Typs
		 */
		public String getUnit() {
			return _unit;
		}

		/**
		 * Gibt die Genauigkeit dieses Fließkommazahl-Attribut-Typs zurück.
		 *
		 * @return die Genauigkeit dieses Attribut-Typs
		 */
		public byte getAccuracy() {
			return _accuracy;
		}
	}
}

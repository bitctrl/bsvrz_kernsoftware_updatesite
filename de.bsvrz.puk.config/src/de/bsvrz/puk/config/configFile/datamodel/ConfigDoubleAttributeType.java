/*
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
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Do, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigDoubleAttributeType extends ConfigAttributeType implements DoubleAttributeType {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert die Eigenschaften dieses AttributTyps. */
	private DoubleAttributeTypeValues _values;

	/**
	 * Konstruktor eines Flie�komma-Attribut-Typen.
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
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses AttributTyps enth�lt. Existiert es noch nicht, so wird es erzeugt und der entsprechende
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
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		_values = null;
		super.invalidateCache();
	}

	/**
	 * Diese Klasse liest die Informationen f�r diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData ConfigurationData}, da dort Informationen ben�tigt
	 * werden, die erst hier zur Verf�gung gestellt werden.
	 */
	private class DoubleAttributeTypeValues {

		/** Die Einheit dieses Attribut-Typs. */
		private String _unit;

		/** Die Genauigkeit dieses Flie�kommazahl-Attribut-Typs. */
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
						"Die Flie�kommazahl-AttributTypEigenschaften des AttributTyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die Einheit dieses Attribut-Typs zur�ck.
		 *
		 * @return die Einheit dieses Attribut-Typs
		 */
		public String getUnit() {
			return _unit;
		}

		/**
		 * Gibt die Genauigkeit dieses Flie�kommazahl-Attribut-Typs zur�ck.
		 *
		 * @return die Genauigkeit dieses Attribut-Typs
		 */
		public byte getAccuracy() {
			return _accuracy;
		}
	}
}

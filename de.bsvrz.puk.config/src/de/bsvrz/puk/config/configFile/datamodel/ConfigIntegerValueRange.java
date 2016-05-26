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
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.Aspect;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces von Eigenschaften von Wertebereichen.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigIntegerValueRange extends ConfigConfigurationObject implements IntegerValueRange {
	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Speichert die Eigenschaften dieses Wertebereichs.
	 */
	private IntegerValueRangeValues _values;

	/**
	 * Konstruktor für einen Wertebereich eines {@link de.bsvrz.dav.daf.main.config.IntegerAttributeType Ganzzahl-Attribut-Typs}.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Wertebereichs
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses Wertebereichs
	 */
	public ConfigIntegerValueRange(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public long getMinimum() {
		return getIntegerValueRangeValues().getMinimum();
	}

	public long getMaximum() {
		return getIntegerValueRangeValues().getMaximum();
	}

	public double getConversionFactor() {
		return getIntegerValueRangeValues().getConversionFactor();
	}

	public String getUnit() {
		return getIntegerValueRangeValues().getUnit();
	}

	/**
	 * Gibt das Objekt zurück, welches die Eigenschaften dieses Wertebereichs enthält. Existiert es noch nicht, so wird es erzeugt indem der entsprechende
	 * konfigurierende Datensatz ausgelesen wird.
	 *
	 * @return die Eigenschaften dieses Wertebereichs
	 */
	private synchronized IntegerValueRangeValues getIntegerValueRangeValues() {
		if (_values == null) {
			_values = new IntegerValueRangeValues();
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
	 * Diese Klasse liest die Informationen für diesen Wertebereich mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData}, da dort Informationen benötigt werden, die hier erst zur Verfügung gestellt werden.
	 */
	private class IntegerValueRangeValues {
		/**
		 * Minimum des Wertebereichs.
		 */
		private long _minimum;

		/**
		 * Maximum des Wertebereichs.
		 */
		private long _maximum;

		/**
		 * der Skalierungsfaktur für die internen Werte, um externe Werte zu erhalten
		 */
		private double _conversionFactor;

		/**
		 * die Maßeinheit von Werten dieses Bereichs
		 */
		private String _unit;

		/**
		 * Konstruktor, der die Eigenschaften dieses {@link IntegerValueRange Ganzzahl-Objekts} aus einem konfigurierenden Datensatz ausliest.
		 */
		public IntegerValueRangeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.werteBereichsEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_minimum = deserializer.readLong();
				_maximum = deserializer.readLong();
				_conversionFactor = deserializer.readDouble();
				_unit = deserializer.readString(64);	 // Länge des Strings steht im Datenkatalog att.einheit in kb.metaModellGlobal

				in.close();	// Stream schließen
			} catch (Exception ex) {
				final String errorMessage = "Die WerteBereichsEigenschaften des Wertebereichs " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt das Minimum des Wertebereichs zurück.
		 *
		 * @return das Minimum des Wertebereichs
		 */
		public long getMinimum() {
			return _minimum;
		}

		/**
		 * Gibt das Maximum des Wertebereichs zurück.
		 *
		 * @return das Maximum des Wertebereichs
		 */
		public long getMaximum() {
			return _maximum;
		}

		/**
		 * Gibt des Skalierungsfaktor für die inneren Werte zurück.
		 *
		 * @return der Skalierungsfaktor dieses Wertebereichs
		 */
		public double getConversionFactor() {
			return _conversionFactor;
		}

		/**
		 * Gibt die Maßeinheit von Werten dieses Bereichs zurück.
		 *
		 * @return die Maßeinheit von Werten dieses Bereichs
		 */
		public String getUnit() {
			return _unit;
		}
	}
}

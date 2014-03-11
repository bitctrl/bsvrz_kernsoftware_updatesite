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
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Do, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigIntegerValueRange extends ConfigConfigurationObject implements IntegerValueRange {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Speichert die Eigenschaften dieses Wertebereichs.
	 */
	private IntegerValueRangeValues _values;

	/**
	 * Konstruktor f�r einen Wertebereich eines {@link de.bsvrz.dav.daf.main.config.IntegerAttributeType Ganzzahl-Attribut-Typs}.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Wertebereichs
	 * @param systemObjectInfo  das korrespondierende Objekt f�r die Dateioperationen dieses Wertebereichs
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
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses Wertebereichs enth�lt. Existiert es noch nicht, so wird es erzeugt indem der entsprechende
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
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		_values = null;
		super.invalidateCache();
	}

	/**
	 * Diese Klasse liest die Informationen f�r diesen Wertebereich mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData}, da dort Informationen ben�tigt werden, die hier erst zur Verf�gung gestellt werden.
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
		 * der Skalierungsfaktur f�r die internen Werte, um externe Werte zu erhalten
		 */
		private double _conversionFactor;

		/**
		 * die Ma�einheit von Werten dieses Bereichs
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
				_unit = deserializer.readString(64);	 // L�nge des Strings steht im Datenkatalog att.einheit in kb.metaModellGlobal

				in.close();	// Stream schlie�en
			} catch (Exception ex) {
				final String errorMessage = "Die WerteBereichsEigenschaften des Wertebereichs " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt das Minimum des Wertebereichs zur�ck.
		 *
		 * @return das Minimum des Wertebereichs
		 */
		public long getMinimum() {
			return _minimum;
		}

		/**
		 * Gibt das Maximum des Wertebereichs zur�ck.
		 *
		 * @return das Maximum des Wertebereichs
		 */
		public long getMaximum() {
			return _maximum;
		}

		/**
		 * Gibt des Skalierungsfaktor f�r die inneren Werte zur�ck.
		 *
		 * @return der Skalierungsfaktor dieses Wertebereichs
		 */
		public double getConversionFactor() {
			return _conversionFactor;
		}

		/**
		 * Gibt die Ma�einheit von Werten dieses Bereichs zur�ck.
		 *
		 * @return die Ma�einheit von Werten dieses Bereichs
		 */
		public String getUnit() {
			return _unit;
		}
	}
}

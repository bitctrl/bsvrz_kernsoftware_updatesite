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
 * @version $Revision: 11583 $ / $Date: 2013-08-22 15:59:25 +0200 (Do, 22 Aug 2013) $ / ($Author: jh $)
 */
public class ConfigIntegerValueState extends ConfigConfigurationObject implements IntegerValueState {
	/**
	 * DebugLogger f�r Debug-Ausgaben
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
		// Sind im fehlerhafterweise als �nderbar markiert, d�rfen aber nur �ber eine "unversionierte Datenmodell�nderung"
		// umbenannt werden
		RelaxedModelChanges relaxedModelChanges = RelaxedModelChanges.getInstance(getDataModel());
		if(relaxedModelChanges.allowChangeValueName(this)){
			super.setName(name);
		}
		else {
			throw new ConfigurationChangeException(
					"Der Name des Objekts (" + getNameOrPidOrId() + ") darf nur �ber unversionierte Datenmodell�nderungen ge�ndert werden. " +
							"Bitte kb.metaModellGlobal in Mindestversion 16 installieren."
			);
		}
	}

	public long getValue() {
		return getIntegerValueStateValues().getValue();
	}

	/**
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses Wertezustands enth�lt. Existiert es noch nicht, so wird es erzeugt und der entsprechende
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
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		_values = null;
		super.invalidateCache();
	}

	/**
	 * Diese Klasse liest die Informationen f�r diesen Wertezustand mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData ConfigurationData}, da dort Informationen ben�tigt
	 * werden, die erst hier zur Verf�gung gestellt werden.
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
		 * Gibt den Wert des Zustands eines Ganzzahl-Attributs zur�ck.
		 *
		 * @return den Wert des Zustands eines Ganzzahl-Attributs
		 */
		public long getValue() {
			return _value;
		}
	}
}

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
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces für Attributtypen.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAttributeType extends ConfigConfigurationObject implements AttributeType {
	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Default-Attributwert oder <code>null</code> falls nicht definiert.
	 */
	private String _defaultAttributeValue;

	/**
	 * Konstruktor für Attributtypen.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Attribut-Typs
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses Attribut-Typs
	 */
	public ConfigAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
		loadDefaultAttributeValue();
	}

	/**
	 * Ermittelt den Default-Attributwert dieses Attributtyps.
	 *
	 * @return Default-Attributwert dieses Attributtyps oder <code>null</code> falls kein Defaultwert festgelegt wurde.
	 */
	public synchronized String getDefaultAttributeValue() {
		return _defaultAttributeValue;
	}

	/**
	 * Lädt den Default-Attributwert dieses Attributtyps aus einem konfigurierenden Datensatz.
	 */
	public synchronized void loadDefaultAttributeValue() {
		try {
			final AttributeGroup atg = getDataModel().getAttributeGroup("atg.defaultAttributwert");
			final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
			final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
			byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

			if (bytes == null || bytes.length == 0) {
				_defaultAttributeValue = null;
			} else {

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_defaultAttributeValue = deserializer.readString(32767);

				in.close();	// Stream schließen
			}
		} catch (IllegalArgumentException e) {
			// Datensatz nicht vorhanden
			_defaultAttributeValue = null;
		} catch (Exception ex) {
			final String errorMessage = "Der Default-Attributwert des Attributtyps " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new IllegalStateException(errorMessage, ex);
		}
	}

	/**
	 * Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. Erbende Klassen müssen diese
	 * Funktion überschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		loadDefaultAttributeValue();
	}
}

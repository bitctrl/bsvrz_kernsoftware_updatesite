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
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces f�r Attributtypen.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Thu, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigAttributeType extends ConfigConfigurationObject implements AttributeType {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Default-Attributwert oder <code>null</code> falls nicht definiert.
	 */
	private String _defaultAttributeValue;

	/**
	 * Konstruktor f�r Attributtypen.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Attribut-Typs
	 * @param systemObjectInfo  das korrespondierende Objekt f�r die Dateioperationen dieses Attribut-Typs
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
	 * L�dt den Default-Attributwert dieses Attributtyps aus einem konfigurierenden Datensatz.
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

				in.close();	// Stream schlie�en
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
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		loadDefaultAttributeValue();
	}
}

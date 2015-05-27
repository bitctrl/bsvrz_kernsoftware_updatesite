/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications.CONFIGURATION_ELEMENTS_IN_MUTABLE_SET;

/**
 * Interface um die Speicherung einer dynamischem Menge ({@linkplain de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet})
 * zu realisieren. Diese werden entweder als Konfigurationsdatensatz oder als eigene Datei gespeichert, je nach Implementierung
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13270 $
 */
public class MutableSetConfigDataStorage extends MutableSetStorage {
	
	private static final Debug _debug = Debug.getLogger();

	private ConfigMutableSet _mutableSet;

	public MutableSetConfigDataStorage(final ConfigMutableSet mutableSet) {
		_mutableSet = mutableSet;
	}

	/**
	 * Speichert die Elemente dieser Menge (auch historische) in einem konfigurierenden Datensatz ab.
	 * 
	 * @param mutableElements
	 *            Elemente dieser Menge
	 * 
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *             Falls die Elemente nicht in einem konfigurierenden Datensatz abgespeichert werden k�nnen.
	 */
	@Override
	public void writeElements(final List<MutableElement> mutableElements) throws ConfigurationChangeException {
		// Liste in ein Byte-Array packen und abspeichern
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(_mutableSet.getSerializerVersion(), out);
			for(MutableElement mutableElement : mutableElements) {
				if(mutableElement.getObject() == null) {
					// da hier eh immer alles neu geschrieben wird, k�nnen hier auch gleich ung�ltige Eintr�ge gel�scht werden
					continue;
				}
				serializer.writeLong(mutableElement.getId());
				serializer.writeLong(mutableElement.getStartTime());
				serializer.writeLong(mutableElement.getEndTime());
				serializer.writeShort(mutableElement.getSimulationVariant());
			}
			final byte[] bytes = out.toByteArray();
			_mutableSet._systemObjectInfo.setConfigurationData(CONFIGURATION_ELEMENTS_IN_MUTABLE_SET, bytes);
			// ein Datensatz hat sich ge�ndert -> dem Konfigurationsbereich Bescheid sagen
			_mutableSet.getConfigurationArea().setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der konfigurierende Datensatz mit den Elementen der Menge " + _mutableSet.getNameOrPidOrId() + " konnte nicht geschrieben werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Diese Methode liest den konfigurierenden Datensatz f�r die Elemente dieser Menge ein und gibt sie in einer Liste zur�ck.
	 * 
	 * @return eine Liste von Elementen mit Zeitstempeln, die die Zugeh�rigkeitszeitr�ume repr�sentieren
	 */
	@Override
	public List<MutableElement> readElements() {
		// die eingelesenen Elemente werden nicht alle vorgehalten, da dies auf Dauer zu viele werden k�nnen
		final List<MutableElement> mutableElements = new ArrayList<MutableElement>();
		try {
			byte[] bytes;
			// feste ID f�r die ATG-Verwendung um die Elemente einer Menge zu erhalten
			bytes = _mutableSet._systemObjectInfo.getConfigurationData(CONFIGURATION_ELEMENTS_IN_MUTABLE_SET);
			final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			final Deserializer deserializer = SerializingFactory.createDeserializer(_mutableSet.getSerializerVersion(), in);
			assert bytes.length % 26 == 0 : "Format des Byte-Arrays f�r die Elemente einer Menge " + _mutableSet.getNameOrPidOrId()
			        + " hat sich ge�ndert. L�nge muss durch 26 teilbar sein.";
			int numberOfElements = bytes.length / 26;
			for(int i = 0; i < numberOfElements; i++) {
				long id = deserializer.readLong();
				long startTime = deserializer.readLong(); // Zeit, ab der das Element zur Menge geh�rt
				long endTime = deserializer.readLong(); // Zeit, ab der das Element nicht mehr zur Menge geh�rt
				short simulationVariant = deserializer.readShort(); // Simulationsvariante dieses Objekt, in der es zur Menge hinzugef�gt oder aus der Menge entfernt wurde
				final SystemObject object = _mutableSet.getDataModel().getObject(id);

				if(object == null) {
					_debug.warning("Element mit Id '" + id + "' kann nicht der Menge '" + _mutableSet.getPidOrNameOrId()
					        + "' hinzugef�gt werden, da es kein System-Objekt hierzu gibt.");
				}
				mutableElements.add(new MutableElement(object, startTime, endTime, simulationVariant));
			}
			in.close();
			return mutableElements;
		}
		catch(IllegalArgumentException ex) {
			final String errorMessage = "Elemente der dynamischen Menge '" + _mutableSet.getNameOrPidOrId()
			        + "' konnten nicht ermittelt werden (evtl. wurde die Menge neu angelegt)";
			_debug.finest(errorMessage, ex.getMessage());
		}
		catch(Exception ex) {
			final String errorMessage = "Elemente der dynamischen Menge " + _mutableSet.getNameOrPidOrId() + " konnten nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		return mutableElements;
	}

}

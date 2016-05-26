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

import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Implementierung des Interfaces für Integer-Attribut-Typen.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigIntegerAttributeType extends ConfigAttributeType implements IntegerAttributeType {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert die erlaubten diskreten Zustände eines Attributs. */
	private List<IntegerValueState> _states = null;

	/** Speichert die Eigenschaften dieses AttributTyps. */
	private IntegerAttributeTypeValues _values;

	/** Der undefiniert Wert muss nur einmal angefordert werden. wird true, sobald der Wert angefordert wurde. */
	private boolean _undefinedValueRequested = false;

	/**
	 * Enthält entweder den undefiniert Wert, oder <code>null</code> falls der Wert nicht berechenbar war. Der Wert kann nicht im konstruktor berechnet werden, da
	 * dort die States nicht zur Verfügung stehen.
	 */
	private Long _undefinedValue;

	/**
	 * Konstruktor für Integer-Attribut-Typen.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Typs
	 * @param systemObjectInfo  das korrespondierende Objekt für Dateioperationen dieses Typs
	 */
	public ConfigIntegerAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public short getValueSize() {
		throw new UnsupportedOperationException("Methode IntegerAttributeType.getValueSize() wird nicht mehr unterstützt.");
	}

	public int getByteCount() {
		return getIntegerAttributeTypeValues().getByteCount();
	}

	public IntegerValueRange getRange() {
		return getIntegerAttributeTypeValues().getRange();
	}

	public List<IntegerValueState> getStates() {
		if(_states == null) {
			List<IntegerValueState> states = new ArrayList<IntegerValueState>();
			ObjectSet set = getObjectSet("zustände");
			if(set != null) {
				for(SystemObject systemObject : set.getElements()) {
					IntegerValueState state = (IntegerValueState)systemObject;
					states.add(state);
				}
			}
			_states = states;
		}
		return _states;
	}

	/**
	 * Gibt das Objekt zurück, welches die Eigenschaften dieses AttributTyps enthält. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Eigenschaften dieses AttributTyps
	 */
	private synchronized IntegerAttributeTypeValues getIntegerAttributeTypeValues() {
		if(_values == null) {
			_values = new IntegerAttributeTypeValues();
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
		_undefinedValueRequested = false;
		_undefinedValue = null;
		super.invalidateCache();
	}

	/**
	 * Stellt den undefiniert Wert zur Verfügung. Ist dieser noch nicht vorhanden, wird er angefordert.
	 *
	 * @return undefiniert Wert, <code>null</code> bedeutet, dass es keinen Wert für diesen Attributtyp gibt
	 */
	synchronized private Long getUndefinedValue() {
		if(!_undefinedValueRequested) {
			_undefinedValue = UndefinedValueHandler.getInstance().getUndefinedValueInteger(this);
			_undefinedValueRequested = true;
		}
		return _undefinedValue;
	}

	public void setToUndefined(Data data) {
		UndefinedValueHandler.getInstance().setToUndefinedInteger(data, getUndefinedValue(), this);
	}

	public boolean isDefined(Data data) {
		return UndefinedValueHandler.getInstance().isDefinedInteger(this, data, getUndefinedValue());
	}

	/**
	 * Diese Klasse liest die Informationen für diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link SystemObject#getConfigurationData}, da dort Informationen benötigt werden, die hier erst zur Verfügung gestellt werden.
	 */
	private class IntegerAttributeTypeValues {

		/** Anzahl, der für die Darstellung benötigten Bytes. */
		private byte _byteCount;

		/** Bereich, des Ganzzahl-Attribut-Typs. */
		private IntegerValueRange _range;

		/** Konstruktor, der die Eigenschaften dieses Ganzzahlattributtyps aus einem konfigurierenden Datensatz ausliest. */
		public IntegerAttributeTypeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.ganzzahlAttributTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());
				assert bytes.length == 9
						: "Länge des Byte-Arrays der Attributgruppe atg.ganzzahlAttributTypEigenschaften hat sich geändert. Angenommene Länge = 9.";

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_range = (IntegerValueRange)deserializer.readObjectReference(getDataModel());
				_byteCount = deserializer.readByte();

				in.close();	// Stream schließen
			}
			catch(Exception ex) {
				final String errorMessage = "Die GanzzahlAttributTypEigenschaften des AttributTyps " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die Anzahl der benötigten Bytes zurück, die für die Darstellung benötigt werden.
		 *
		 * @return die Anzahl der benötigten Bytes
		 */
		public byte getByteCount() {
			return _byteCount;
		}

		/**
		 * Gibt den Bereich des Ganzzahl-Attribut-Typs zurück.
		 *
		 * @return der Bereich des Ganzzahl-Attribut-Typs
		 */
		public IntegerValueRange getRange() {
			return _range;
		}
	}
}

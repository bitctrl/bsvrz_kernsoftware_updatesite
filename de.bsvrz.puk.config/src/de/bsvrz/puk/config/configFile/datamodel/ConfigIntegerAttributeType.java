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
 * Implementierung des Interfaces f�r Integer-Attribut-Typen.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Thu, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigIntegerAttributeType extends ConfigAttributeType implements IntegerAttributeType {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert die erlaubten diskreten Zust�nde eines Attributs. */
	private List<IntegerValueState> _states = null;

	/** Speichert die Eigenschaften dieses AttributTyps. */
	private IntegerAttributeTypeValues _values;

	/** Der undefiniert Wert muss nur einmal angefordert werden. wird true, sobald der Wert angefordert wurde. */
	private boolean _undefinedValueRequested = false;

	/**
	 * Enth�lt entweder den undefiniert Wert, oder <code>null</code> falls der Wert nicht berechenbar war. Der Wert kann nicht im konstruktor berechnet werden, da
	 * dort die States nicht zur Verf�gung stehen.
	 */
	private Long _undefinedValue;

	/**
	 * Konstruktor f�r Integer-Attribut-Typen.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Typs
	 * @param systemObjectInfo  das korrespondierende Objekt f�r Dateioperationen dieses Typs
	 */
	public ConfigIntegerAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public short getValueSize() {
		throw new UnsupportedOperationException("Methode IntegerAttributeType.getValueSize() wird nicht mehr unterst�tzt.");
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
			ObjectSet set = getObjectSet("zust�nde");
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
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses AttributTyps enth�lt. Existiert es noch nicht, so wird es erzeugt und der entsprechende
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
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		_values = null;
		super.invalidateCache();
	}

	/**
	 * Stellt den undefiniert Wert zur Verf�gung. Ist dieser noch nicht vorhanden, wird er angefordert.
	 *
	 * @return undefiniert Wert, <code>null</code> bedeutet, dass es keinen Wert f�r diesen Attributtyp gibt
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
	 * Diese Klasse liest die Informationen f�r diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link SystemObject#getConfigurationData}, da dort Informationen ben�tigt werden, die hier erst zur Verf�gung gestellt werden.
	 */
	private class IntegerAttributeTypeValues {

		/** Anzahl, der f�r die Darstellung ben�tigten Bytes. */
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
						: "L�nge des Byte-Arrays der Attributgruppe atg.ganzzahlAttributTypEigenschaften hat sich ge�ndert. Angenommene L�nge = 9.";

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_range = (IntegerValueRange)deserializer.readObjectReference(getDataModel());
				_byteCount = deserializer.readByte();

				in.close();	// Stream schlie�en
			}
			catch(Exception ex) {
				final String errorMessage = "Die GanzzahlAttributTypEigenschaften des AttributTyps " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die Anzahl der ben�tigten Bytes zur�ck, die f�r die Darstellung ben�tigt werden.
		 *
		 * @return die Anzahl der ben�tigten Bytes
		 */
		public byte getByteCount() {
			return _byteCount;
		}

		/**
		 * Gibt den Bereich des Ganzzahl-Attribut-Typs zur�ck.
		 *
		 * @return der Bereich des Ganzzahl-Attribut-Typs
		 */
		public IntegerValueRange getRange() {
			return _range;
		}
	}
}

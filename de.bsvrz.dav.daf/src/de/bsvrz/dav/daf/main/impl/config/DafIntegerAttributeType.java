/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Klasse, die den Zugriff auf Ganzzahl-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafIntegerAttributeType extends DafAttributeType implements IntegerAttributeType {

	/** Liste der Zustände */
	private List<IntegerValueState> _states;

	/** Die Id des Wertebereichobjektes */
	private long _rangeId;

	/** Wertebereichobjekt */
	private DafIntegerValueRange _integerValueRange;

	/** Der undefiniert Wert muss nur einmal angefordert werden. wird true, sobald der Wert angefordert wurde. */
	private boolean _undefinedValueRequested = false;

	/**
	 * Enthält entweder den undefiniert Wert, oder <code>null</code> falls der Wert nicht berechenbar war. Der Wert kann nicht im konstruktor berechnet werden, da
	 * dort die States nicht zur Verfügung stehen.
	 */
	private Long _undefinedValue;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafIntegerAttributeType(DafDataModel dataModel) {
		super(dataModel);
		_internType = INTEGER_ATTRIBUTE_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafIntegerAttributeType(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long[] setIds,
			int mode,
			long rangeId,
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, defaultAttributeValue
		);
		_internType = INTEGER_ATTRIBUTE_TYPE;
		if(mode == BYTE) {
			_dataValueType = DataValue.BYTE_TYPE;
		}
		else if(mode == SHORT) {
			_dataValueType = DataValue.SHORT_TYPE;
		}
		else if(mode == LONG) {
			_dataValueType = DataValue.LONG_TYPE;
		}
		else {
			_dataValueType = DataValue.INTEGER_TYPE;
		}
		_rangeId = rangeId;
	}

	public int getByteCount() {
		if(_dataValueType == DataValue.BYTE_TYPE) {
			return 1;
		}
		else if(_dataValueType == DataValue.SHORT_TYPE) {
			return 2;
		}
		else if(_dataValueType == DataValue.LONG_TYPE) {
			return 8;
		}
		else {
			return 4;
		}
	}

	public short getValueSize() {
		return (short)getByteCount();
	}

	public IntegerValueRange getRange() {
		if(_integerValueRange == null) {
			if(_rangeId != 0) {
				_integerValueRange = (DafIntegerValueRange)_dataModel.getObject(_rangeId);
			}
		}
		return _integerValueRange;
	}

	public List<IntegerValueState> getStates() {
		if((_states == null)) {
			final ArrayList<IntegerValueState> states = new ArrayList<IntegerValueState>();
			ObjectSet statesSet = getObjectSet("zustände");
			List<SystemObject> stateElements = statesSet.getElements();
			for(SystemObject stateElement : stateElements) {
				if(stateElement instanceof IntegerValueState) {
					IntegerValueState integerValueState = (IntegerValueState)stateElement;
					states.add(integerValueState);
				}
			}
			_states = Collections.unmodifiableList(states);
		}
		return _states;
	}

	public final String parseToString() {
		String str = "Ganze Zahl Attribute: \n";
		str += super.parseToString();

		if(_dataValueType == DataValue.BYTE_TYPE) {
			str += "Typ : Byte\n";
		}
		else if(_dataValueType == DataValue.SHORT_TYPE) {
			str += "Typ: Short\n";
		}
		else if(_dataValueType == DataValue.INTEGER_TYPE) {
			str += "Typ: Int\n";
		}
		else {
			str += "Typ: Long\n";
		}

		if(_integerValueRange == null) {
			getRange();
			if(_integerValueRange != null) {
				str += "Minimum: " + _integerValueRange.getMinimum() + "\n";
				str += "Maximum: " + _integerValueRange.getMaximum() + "\n";
				str += "Skalierung: " + _integerValueRange.getConversionFactor() + "\n";
				str += "Einheit: " + _integerValueRange.getUnit() + "\n";
			}
		}

		if(_states == null) {
			List list = getStates();
			if(list != null) {
				for(int i = 0; i < list.size(); ++i) {
					((DafSystemObject)list.get(i)).parseToString();
				}
			}
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeByte(_dataValueType);
		out.writeLong(_rangeId);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_dataValueType = in.readByte();
		_rangeId = in.readLong();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_dataValueType = deserializer.readByte();
		_rangeId = deserializer.readLong();
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
}

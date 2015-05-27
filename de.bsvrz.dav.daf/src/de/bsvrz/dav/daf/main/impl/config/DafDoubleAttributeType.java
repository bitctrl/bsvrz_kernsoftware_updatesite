/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Flieﬂkomma-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermˆglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13141 $
 */
public class DafDoubleAttributeType extends DafAttributeType implements DoubleAttributeType {

	/** Die Maﬂeinheit */
	private String _unit;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafDoubleAttributeType(DafDataModel dataModel) {
		super(dataModel);
		_internType = FLOATING_POINT_NUMBER_ATTRIBUTE_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafDoubleAttributeType(
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
			long setIds[],
			int mode,
			String unit,
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, defaultAttributeValue
		);
		_internType = FLOATING_POINT_NUMBER_ATTRIBUTE_TYPE;
		if(mode == FLOAT) {
			_dataValueType = DataValue.FLOAT_TYPE;
		}
		else {
			_dataValueType = DataValue.DOUBLE_TYPE;
		}
		_unit = unit;
	}

	public final String getUnit() {
		return _unit;
	}

	public final byte getAccuracy() {
		if(_dataValueType == DataValue.FLOAT_TYPE) {
			return FLOAT;
		}
		else {
			return DOUBLE;
		}
	}

	public final String parseToString() {
		String str = "Fliesskomma Zahl Attribute: \n";
		str += super.parseToString();
		if(_dataValueType == FLOAT) {
			str += "Typ : Float\n";
		}
		else {
			str += "Typ: Double\n";
		}
		str += "Einheit: " + _unit + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeByte(_dataValueType);
		out.writeUTF(_unit);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_dataValueType = in.readByte();
		_unit = in.readUTF();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_dataValueType = deserializer.readByte();
		_unit = deserializer.readString();
	}
}

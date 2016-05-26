/*
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
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Zeitstempel-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafTimeAttributeType extends DafAttributeType implements TimeAttributeType {

	/**
	 * Bestimmt die Auflösung der Zeit (Sekunden = 0 oder Millisekunden = 1)
	 *
	 * @see de.bsvrz.dav.daf.main.config.TimeAttributeType#SECONDS
	 * @see de.bsvrz.dav.daf.main.config.TimeAttributeType#MILLISECONDS
	 */
	private byte _mode;

	/** Kennung, die <code>true</code> ist, wenn die Zeit bei Attributen dieses Typs relativ angegeben wird */
	private boolean _isRelative;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafTimeAttributeType(DafDataModel dataModel) {
		super(dataModel);
		_internType = TIME_ATTRIBUTE_TYPE;
	}

	public DafTimeAttributeType(
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
			byte mode,
			boolean isRelative,
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, defaultAttributeValue
		);
		_internType = TIME_ATTRIBUTE_TYPE;
		_mode = mode;
		_dataValueType = _mode == 0 ? DataValue.INTEGER_TYPE : DataValue.LONG_TYPE;
		_isRelative = isRelative;
	}

	public final byte getAccuracy() {
		return _mode;
	}

	public final boolean isRelative() {
		return _isRelative;
	}

	public String parseToString() {
		String str = "Time attribute: \n";
		str += super.parseToString();
		if(_mode == SECONDS) {
			str += "Zeitangabe in Sekunden.\n";
		}
		else {
			str += "Zeitangabe in Millisekunden.\n";
		}
		if(_isRelative) {
			str += "Relative Zeit.\n";
		}
		else {
			str += "Absolute Zeit.\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeByte(_mode);
		out.writeBoolean(_isRelative);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_mode = in.readByte();
		_isRelative = in.readBoolean();
		_dataValueType = _mode == 0 ? DataValue.INTEGER_TYPE : DataValue.LONG_TYPE;
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_mode = deserializer.readByte();
		_isRelative = deserializer.readBoolean();
		_dataValueType = _mode == 0 ? DataValue.INTEGER_TYPE : DataValue.LONG_TYPE;
	}

	public void setToUndefined(Data data) {
		if(isRelative()) {
			UndefinedValueHandler.getInstance().setToUndefinedTimeRelative(data, getAccuracy());
		}
		else {
			UndefinedValueHandler.getInstance().setToUndefinedTimeAbsolute(data);
		}
	}

	public boolean isDefined(Data data) {
		if(isRelative()) {
			return UndefinedValueHandler.getInstance().isDefinedTimeRelative(data, getAccuracy());
		}
		else {
			return UndefinedValueHandler.getInstance().isDefinedTimeAbsolute(data);
		}
	}
}

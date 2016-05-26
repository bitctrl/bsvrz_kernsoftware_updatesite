/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Zeichenketten-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafStringAttributeType extends DafAttributeType implements StringAttributeType {

	/** Maximal erlaubte Länge */
	private int _maxLength;

	/** Die Kodierung der Zeichen */
	private String _encodingName;

	/** Bestimmt ob die Maximale Anzahl von Zeichen beschränkt ist. */
	private boolean _lengthLimited;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafStringAttributeType(DafDataModel dataModel) {
		super(dataModel);
		_internType = STRING_ATTRIBUTE_TYPE;
		_dataValueType = DataValue.STRING_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafStringAttributeType(
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
			int maxLength,
			String encoding,
			boolean lengthLimited,
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, defaultAttributeValue
		);
		_internType = STRING_ATTRIBUTE_TYPE;
		_dataValueType = DataValue.STRING_TYPE;
		_maxLength = maxLength;
		_encodingName = encoding;
		_lengthLimited = lengthLimited;
	}

	public final int getMaxLength() {
		return _maxLength;
	}

	public byte getEncodingValue() {
		String encoding = getEncodingName();
		if(encoding.equals("ISO-8859-1")) {
			return ISO_8859_1;
		}
		else {
			throw new IllegalStateException("Unbekannte Kodierung '" + encoding + "'");
		}
	}

	public String getEncodingName() {
		return _encodingName;
	}

	public final boolean isLengthLimited() {
		return _lengthLimited;
	}

	public final String parseToString() {
		String str = "Zeichenkette Attribute: \n";
		str += super.parseToString();
		str += "Kodierung: " + _encodingName + "\n";
		if(_lengthLimited) {
			str += "Maximale Länge: " + _maxLength + "\n";
		}
		else {
			str += "Maximale Länge: unbegrentzt \n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(_maxLength);
		out.writeUTF(_encodingName);
		out.writeBoolean(_lengthLimited);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_maxLength = in.readInt();
		_encodingName = in.readUTF();
		_lengthLimited = in.readBoolean();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_maxLength = deserializer.readInt();
		_encodingName = deserializer.readString();
		_lengthLimited = deserializer.readBoolean();
	}

	public void setToUndefined(Data data) {
		UndefinedValueHandler.getInstance().setToUndefinedString(data);
	}

	public boolean isDefined(Data data) {
		return UndefinedValueHandler.getInstance().isDefinedString(data);
	}
}

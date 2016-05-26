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

import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Klasse, die den Zugriff auf Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafAttributeType extends DafConfigurationObject implements AttributeType {

	/**
	 * Typkennung des Attributwerts.
	 *
	 * @see de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue
	 */
	protected byte _dataValueType;

	/** Default-Attributwert oder <code>null</code> falls nicht definiert. */
	private String _defaultAttributeValue;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	protected DafAttributeType(DafDataModel dataModel) {
		super(dataModel);
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	protected DafAttributeType(
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
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);

		_defaultAttributeValue = defaultAttributeValue;
	}

	public String getDefaultAttributeValue() {
		return _defaultAttributeValue;
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		if(_defaultAttributeValue == null) {
			out.writeBoolean(false);
		}
		else {
			out.writeBoolean(true);
			out.writeUTF(_defaultAttributeValue);
		}
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		if(in.readBoolean()) {
			_defaultAttributeValue = in.readUTF();
		}
		else {
			_defaultAttributeValue = null;
		}
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		if(deserializer.readBoolean()) {
			_defaultAttributeValue = deserializer.readString();
		}
		else {
			_defaultAttributeValue = null;
		}
	}
}

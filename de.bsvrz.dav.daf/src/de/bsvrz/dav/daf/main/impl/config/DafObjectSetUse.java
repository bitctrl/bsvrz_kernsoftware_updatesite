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

import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Mengenverwendungen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafObjectSetUse extends DafConfigurationObject implements ObjectSetUse {

	/** Der Name der Menge */
	private String _objectSetName;

	/** Die Id des zugeordneten Mengentyps */
	private long _objectSetTypeId;

	/** Der zugeordnete Mengentyp */
	private DafObjectSetType _objectSetType;

	/** Gibt an, ob die Verwendung der Menge bei einem Objekt des jeweiligen Objekt-Typs erforderlich ist. */
	private boolean _required;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafObjectSetUse(DafDataModel dataModel) {
		super(dataModel);
		_internType = OBJECT_SET_USE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafObjectSetUse(
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
			String setName,
			long objectSetTypeId,
			boolean neededFromObjectType
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = OBJECT_SET_USE;
		_objectSetName = setName;
		_objectSetTypeId = objectSetTypeId;
		_required = neededFromObjectType;
	}

	public final String parseToString() {
		String str = "Menge: \n";
		str += super.parseToString();
		str += "MengenName: " + _objectSetName + "\n";
		str += _objectSetType.parseToString();
		str += "Vom Objektstyp erforderlich: " + _required + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeUTF(_objectSetName);
		out.writeLong(_objectSetTypeId);
		out.writeBoolean(_required);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_objectSetName = in.readUTF();
		_objectSetTypeId = in.readLong();
		_required = in.readBoolean();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_objectSetName = deserializer.readString();
		_objectSetTypeId = deserializer.readLong();
		_required = deserializer.readBoolean();
	}

	public final String getObjectSetName() {
		return _objectSetName;
	}

	public final boolean isRequired() {
		return _required;
	}

	public ObjectSetType getObjectSetType() {
		if(_objectSetType == null) {
			_objectSetType = (DafObjectSetType)_dataModel.getObject(_objectSetTypeId);
		}
		return _objectSetType;
	}
}

/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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
 * Klasse zum Zugriff auf die beschreibenden Informationen und Einschr�nkungen von Mengen-Typen. Zu jedem Mengen-Typ wird konfiguriert welcher Name eine Menge
 * dieses Typs haben muss, welche Typen von Objekten enthalten sein d�rfen, wieviele Objekte mindestens und h�chstens enthalten sein m�ssen bzw. d�rfen, ob eine
 * Menge an den entsprechenden Objekten vorhanden sein muss oder darf und ob eine Menge dieses Typs konfigurierend oder dynamisch ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13141 $
 */
public class DafObjectSetType extends DafConfigurationObjectType implements ObjectSetType {

	/** Anzahl von Objekten, die mindestens in der Menge vorhanden sein m�ssen */
	private int _minimumElementCount;

	/** Anzahl von Objekten, die h�chstens in der Menge vorhanden sein d�rfen */
	private int _maximumElementCount;

	/** Flag, das true ist, wenn dieser Typ dynamische Mengen definiert. */
	private boolean _mutable;

	/** Liste von {@link de.bsvrz.dav.daf.main.config.SystemObjectType Objekt-Typen}, die in Mengen dieses Typs verwendet werden k�nnen */
	private List<SystemObjectType> _objectTypes;

	/** Konfigurierender Datensatz mit den wesentlichen Eigenschaften dieses Attributtyps oder <code>null</code>, wenn der Datensatz noch nicht geladen wurde. */
	private Data _data = null;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafObjectSetType(DafDataModel dataModel) {
		super(dataModel);
		_internType = OBJECT_SET_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafObjectSetType(
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
			boolean hasPermanentName,
			long setIds[],
			int minimumSize,
			int maximumSize,
			boolean dynamic
	) {

		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, hasPermanentName
		);
		_internType = OBJECT_SET_TYPE;
		_minimumElementCount = minimumSize;
		_maximumElementCount = maximumSize;
		_mutable = dynamic;
	}

	public final String parseToString() {
		String str = "Mengentyp: \n";
		str += super.parseToString();
		str += "Minimum Anzahl: " + _minimumElementCount + "\n";
		str += "Maximum Anzahl: " + _maximumElementCount + "\n";
		str += "Dynamische Menge: " + _mutable + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(_minimumElementCount);
		out.writeInt(_maximumElementCount);
		out.writeBoolean(_mutable);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_minimumElementCount = in.readInt();
		_maximumElementCount = in.readInt();
		_mutable = in.readBoolean();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_minimumElementCount = deserializer.readInt();
		_maximumElementCount = deserializer.readInt();
		_mutable = deserializer.readBoolean();
	}

	public final int getMinimumElementCount() {
		return _minimumElementCount;
	}

	public final int getMaximumElementCount() {
		return _maximumElementCount;
	}

	public final boolean isMutable() {
		return _mutable;
	}

	/**
	 * L�dt den konfigurierenden Datensatz mit den Eigenschaften dieses Objekts aus der Konfiguration und speichert ihn f�r weitere Aufrufe dieser Methode.
	 *
	 * @return Konfigurierender Datensatz mit den Eigenschaften dieses Objekts.
	 */
	private synchronized Data getProperties() {
		if(_data == null) {
			_data = getConfigurationData(getDataModel().getAttributeGroup("atg.mengenTypEigenschaften"));
			if(_data == null) {
				throw new IllegalStateException(
						"Konfigurierender Datensatz 'atg.mengenTypEigenschaften' am " + "MengenTyp " + getPidOrNameOrId() + " konnte nicht geladen werden"
				);
			}
		}
		return _data;
	}

	public ReferenceType getReferenceType() {
		Data properties = getProperties();
		String referenceType = properties.getTextValue("referenzierungsart").getValueText();
		if(referenceType.equals("Assoziation")) {
			return ReferenceType.ASSOCIATION;
		}
		else if(referenceType.equals("Aggregation")) {
			return ReferenceType.AGGREGATION;
		}
		else if(referenceType.equals("Komposition")) {
			return ReferenceType.COMPOSITION;
		}
		else {
			throw new IllegalStateException("unbekannte Referenzierungsart am " + getPidOrNameOrId() + ": " + referenceType);
		}
	}


	public final List<SystemObjectType> getObjectTypes() {
		if(_objectTypes == null) {
			final ArrayList<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
			ObjectSet objectTypeSet = getObjectSet("ObjektTypen");
			List<SystemObject> objectTypeElements = objectTypeSet.getElements();
			for(SystemObject objectTypeElement : objectTypeElements) {
				if(objectTypeElement instanceof SystemObjectType) {
					SystemObjectType systemObjectType = (SystemObjectType)objectTypeElement;
					objectTypes.add(systemObjectType);
				}
			}
			_objectTypes = Collections.unmodifiableList(objectTypes);
		}
		return _objectTypes;
	}
}

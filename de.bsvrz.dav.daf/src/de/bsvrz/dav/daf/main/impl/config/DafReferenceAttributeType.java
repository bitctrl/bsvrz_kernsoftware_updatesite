/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse zum Zugriff auf die Eigenschaften von Referenz-Attributtypen. Attribute von diesem Attributtyp referenzieren andere Objekte. Der Typ der
 * referenzierten Objekte wird durch den Attributtyp festgelegt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class DafReferenceAttributeType extends DafAttributeType implements ReferenceAttributeType {

	/** Die ID des Typs der referenzierbaren Objekte */
	private long _referencedTypeId;

	/** Typ der referenzierbaren Objekte */
	private DafSystemObjectType _systemObjectType;

	/** Konfigurierender Datensatz mit den wesentlichen Eigenschaften dieses Attributtyps oder <code>null</code>, wenn der Datensatz noch nicht geladen wurde. */
	private Data _data = null;

	/** Kennung, die gesetzt ist, wenn bei Referenzattributen dieses Typs undefinierte Referenzen erlaubt sind. */
	private boolean _isUndefinedAllowed;

	/** Referenzierungsart von Referenzen dieses Typs */
	private ReferenceType _referenceType;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafReferenceAttributeType(DafDataModel dataModel) {
		super(dataModel);
		_internType = REFERENCE_ATTRIBUTE_TYPE;
		_dataValueType = DataValue.LONG_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafReferenceAttributeType(
			long id,
			String pid,
			String name,
			long typeId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long[] setIds,
			long referenceId,
			String defaultAttributeValue,
			boolean isUndefinedAllowed,
			ReferenceType referenceType
	) {
		super(
				id, pid, name, typeId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, defaultAttributeValue
		);
		_internType = REFERENCE_ATTRIBUTE_TYPE;
		_dataValueType = DataValue.LONG_TYPE;
		_referencedTypeId = referenceId;
		_referenceType = referenceType;
		_isUndefinedAllowed = isUndefinedAllowed;
	}

	public SystemObjectType getReferencedObjectType() {
		if(_referencedTypeId == 0) return null;
		if(_systemObjectType == null) {
			_systemObjectType = (DafSystemObjectType)_dataModel.getObject(_referencedTypeId);
		}
		return _systemObjectType;
	}

	public boolean isUndefinedAllowed() {
		return _isUndefinedAllowed;
	}

	public ReferenceType getReferenceType() {
		return _referenceType;
	}

	public final String parseToString() {
		String str = "Objektreferenz: \n";
		str += super.parseToString();
		str += "ID des Typs: " + _referencedTypeId + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeLong(_referencedTypeId);
		out.writeBoolean(_isUndefinedAllowed);
		final int referenceTypeCode;
		switch(_referenceType) {
			case ASSOCIATION:
				referenceTypeCode = 0;
				break;
			case AGGREGATION:
				referenceTypeCode = 1;
				break;
			case COMPOSITION:
				referenceTypeCode = 2;
				break;
			default:
				throw new IllegalStateException("Unbekannte Referenzierungsart beim Attributtyp " + this + ": " + _referenceType);
		}
		out.writeByte(referenceTypeCode);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_referencedTypeId = in.readLong();
		_isUndefinedAllowed = in.readBoolean();
		final int referenceTypeCode = in.readByte();
		switch(referenceTypeCode) {
			case 0:
				_referenceType = ReferenceType.ASSOCIATION;
				break;
			case 1:
				_referenceType = ReferenceType.AGGREGATION;
				break;
			case 2:
				_referenceType = ReferenceType.COMPOSITION;
				break;
			default:
				throw new IllegalStateException("Unbekannte Referenzierungsart beim lesen des Attributtyps " + getPid() + ": " + referenceTypeCode);
		}
	}

	public void setToUndefined(Data data) {
		UndefinedValueHandler.getInstance().setToUndefinedReference(data);
	}

	public boolean isDefined(Data data) {
		return UndefinedValueHandler.getInstance().isDefinedReference(data, this);
	}
}

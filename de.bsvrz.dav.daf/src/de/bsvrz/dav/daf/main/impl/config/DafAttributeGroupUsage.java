/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsageIdentifier;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Attributgruppenverwendungen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafAttributeGroupUsage extends DafConfigurationObject implements AttributeGroupUsage, AttributeGroupUsageIdentifier {

	/** Logger für Debug-Ausgaben. */
	private static final Debug _debug = Debug.getLogger();

	/** Objekt-Id der zugehörigen Attributgruppe */
	private long _attributeGroupId = 0;

	/** Zugehörige Attributgruppe oder <code>null</code> falls Attributgruppe noch nicht geladen wurden */
	private AttributeGroup _attributeGroup = null;

	/** Objekt-Id des zugehörigen Aspekts */
	private long _aspectId = 0;

	/** Zugehöriger Aspekt oder <code>null</code> falls noch nicht geladen */
	private Aspect _aspect = null;

	/** Gibt an, ob diese Attributgruppenverwendung in den Versorgungsdateien explizit spezifiziert wurde. */
	private boolean _explicitDefined = false;

	/** Gibt an, wie die durch diese Attributgruppenverwendung spezifizierte Kombination aus Attributgruppe und Aspekt verwendet werden kann. */
	private Usage _usage = null;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafAttributeGroupUsage(DafDataModel dataModel) {
		super(dataModel);
		_internType = ATTRIBUTE_GROUP_USAGE;
	}

	/** Erzeugt eine neue Attributgruppenverwendung mit den angegebenen Eigenschaften */
	public DafAttributeGroupUsage(
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
			AttributeGroup attributeGroup,
			Aspect aspect,
			boolean explicitDefined,
			Usage usage
	) {
		super(id, pid, name, typeId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds);
		_attributeGroupId = attributeGroup.getId();
		_attributeGroup = attributeGroup;
		_aspectId = aspect.getId();
		_aspect = aspect;
		_explicitDefined = explicitDefined;
		_usage = usage;
		_internType = ATTRIBUTE_GROUP_USAGE;
	}


	public AttributeGroup getAttributeGroup() {
		if(_attributeGroup == null) {
			_attributeGroup = (AttributeGroup)getDataModel().getObject(_attributeGroupId);
		}
		return _attributeGroup;
	}

	public Aspect getAspect() {
		if(_aspect == null) {
			_aspect = (Aspect)getDataModel().getObject(_aspectId);
		}
		return _aspect;
	}

	public boolean isConfigurating() {
		switch(getUsage()) {
			case RequiredConfigurationData:
			case ChangeableRequiredConfigurationData:
			case OptionalConfigurationData:
			case ChangeableOptionalConfigurationData:
				return true;
			default:
				return false;
		}
	}

	public boolean isExplicitDefined() {
		return _explicitDefined;
	}

	public Usage getUsage() {
		return _usage;
	}

	public long getIdentificationForDav() {
		return getId();
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeLong(_attributeGroupId);
		out.writeLong(_aspectId);
		out.writeBoolean(_explicitDefined);
		out.writeByte(_usage.getId());
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		_attributeGroupId = in.readLong();
		_aspectId = in.readLong();
		_explicitDefined = in.readBoolean();
		_usage = Usage.getInstanceWithId(in.readByte());
	}

	public void read(Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_attributeGroupId = deserializer.readLong();
		_aspectId = deserializer.readLong();
		_explicitDefined = deserializer.readBoolean();
		_usage = Usage.getInstanceWithId(deserializer.readByte());
	}
}

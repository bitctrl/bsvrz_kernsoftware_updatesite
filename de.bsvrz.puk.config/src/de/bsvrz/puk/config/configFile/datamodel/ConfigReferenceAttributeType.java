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

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces {@link ReferenceAttributeType} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Do, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigReferenceAttributeType extends ConfigAttributeType implements ReferenceAttributeType {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * die Eigenschaften dieses Referenz-Attribut-Typs
	 */
	private ReferenceAttributeTypeValues _values;

	/**
	 * Konstruktor eines Referenz-Attribut-Typs.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Referenz-Attribut-Typs
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigReferenceAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public SystemObjectType getReferencedObjectType() {
		return getReferenceAttributeTypeValues().getReferencedObjectType();
	}

	public boolean isUndefinedAllowed() {
		return getReferenceAttributeTypeValues().isUndefinedAllowed();
	}

	public ReferenceType getReferenceType() {
		return getReferenceAttributeTypeValues().getReferenceType();
	}

	/**
	 * L�dt die Eigenschaften dieses Referenz-Attribut-Typs aus einem Datensatz ein und speichert diese in einem {@link
	 * #_values Objekt}.
	 *
	 * @return die Eigenschaften dieses Referenz-Attribut-Typs
	 */
	private synchronized ReferenceAttributeTypeValues getReferenceAttributeTypeValues() {
		if (_values == null) {
			_values = new ReferenceAttributeTypeValues();
		}
		return _values;
	}

	/**
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		_values = null;
	}

	public void setToUndefined(Data data) {
		UndefinedValueHandler.getInstance().setToUndefinedReference(data);
	}

	public boolean isDefined(Data data) {
		return UndefinedValueHandler.getInstance().isDefinedReference(data, this);
	}

	/**
	 * Diese Klasse l�dt die Eigenschaften dieses Referenz-Attribut-Typs aus einem Datensatz ein. Mittels spezieller
	 * Zugriffsmethoden k�nnen die eingelesenen Werte abgerufen werden.
	 */
	private class ReferenceAttributeTypeValues {
		/**
		 * der Typ von Objekten, die von Attributen dieses Attribut-Typs referenziert werden k�nnen
		 */
		private SystemObjectType _referencedObjectType;

		/**
		 * gibt an, ob auch undefinierte Referenzen erlaubt sind
		 */
		private boolean _undefinedAllowed;

		/**
		 * die Referenzierungsart
		 */
		private ReferenceType _referenceType;

		/**
		 * L�dt aus einem Datensatz die Eigenschaften dieses Referenz-Attribut-Typs.
		 */
		public ReferenceAttributeTypeValues() {
			try {
				// Das Objekt wird direkt aus dem Byte-Strom gelesen, da der Deserializer dieses Objekt ben�tigt, um ein Data zu erzeugen.
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.objektReferenzAttributTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				// L�nge des Byte-Arrays pr�fen
				if (bytes.length > 10) {
					throw new RuntimeException("Das Format des eingelesenen Byte-Stroms des konfigurierenden Datensatzes des AttributTyps " + getNameOrPidOrId()
							+ " hat sich ge�ndert.");
				}

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_referencedObjectType = (SystemObjectType) deserializer.readObjectReference(getDataModel());
				_undefinedAllowed = deserializer.readBoolean();
				byte type = deserializer.readByte();
				switch (type) {
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
						throw new RuntimeException("Diese Referenzierungsart wird nicht unterst�tzt: " + type);
				}

				in.close();
			} catch (Exception ex) {
				final String errorMessage = "Die Eigenschaften des Attributtyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt den Typ von Objekten, die von Attributen dieses Attribut-Typs referenziert werden k�nnen, zur�ck.
		 *
		 * @return der Typ von Objekten, die von Attributen dieses Attribut-Typs referenziert werden k�nnen
		 */
		public SystemObjectType getReferencedObjectType() {
			return _referencedObjectType;
		}

		/**
		 * Gibt an, ob auch undefinierte Referenzen erlaubt sind.
		 *
		 * @return gibt an, ob auch undefinierte Referenzen erlaubt sind
		 */
		public boolean isUndefinedAllowed() {
			return _undefinedAllowed;
		}

		/**
		 * Gibt die Referenzierungsart zur�ck.
		 *
		 * @return die Referenzierungsart (Assoziation, Aggregation, Komposition)
		 */
		public ReferenceType getReferenceType() {
			return _referenceType;
		}
	}
}

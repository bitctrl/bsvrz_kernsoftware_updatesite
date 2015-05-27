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

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Implementierung des Interfaces {@link ObjectSetType} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Thu, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigObjectSetType extends ConfigConfigurationObjectType implements ObjectSetType {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * speichert die Objekt-Typen, deren Objekte in Mengen dieses Mengen-Typs gespeichert werden d�rfen
	 */
	private List<SystemObjectType> _objectTypes = null;

	/**
	 * Speichert die Eigenschaften dieses MengenTyps.
	 */
	private ObjectSetTypeValues _values;

	/**
	 * Konstruktor eines Mengen-Typs.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Mengen-Typs
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigObjectSetType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public int getMaximumElementCount() {
		return getObjectSetTypeValues().getMaximumElementCount();
	}

	public int getMinimumElementCount() {
		return getObjectSetTypeValues().getMinimumElementCount();
	}

	public List<SystemObjectType> getObjectTypes() {
		if (_objectTypes == null) {
			List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
			ObjectSet set = getObjectSet("ObjektTypen");
			if (set != null) {
				for (SystemObject systemObject : set.getElements()) {
					SystemObjectType objectType = (SystemObjectType) systemObject;
					objectTypes.add(objectType);
				}
			}
			_objectTypes = objectTypes;
		}
		return _objectTypes;
	}

	public boolean isMutable() {
//		_debug.finest("An Objekt " + getPidOrNameOrId() + " wurde isMutable() aufgerufen.");
		// Die folgenden Abfragen sind notwendig, weil bei der Konvertierung der entsprechenden Objekte
		// die Eigenschaft noch nicht gespeichert ist, aber schon ben�tigt wird.
		if (getPid().equals(Pid.SetType.ATTRIBUTEGROUPS)) return false;
		if (getPid().equals(Pid.SetType.ASPECTS)) return false;
		if (getPid().equals(Pid.SetType.ATTRIBUTES)) return false;
		if (getPid().equals(Pid.SetType.ATTRIBUTE_GROUP_USAGES)) return false;

		return getObjectSetTypeValues().isMutable();
	}

	public boolean isNameOfObjectsPermanent() {
		return true;
	}

	public ReferenceType getReferenceType() {
		// f�r Konfigurationsmengen gelten die Referenzierungsarten "gerichtete Assoziation", "Aggregation" oder "Komposition"
		// f�r dynamische Mengen gilt die Referenzierungsart "gerichtete Assoziation"
		if (isMutable()) {	// dynamische Menge
			return ReferenceType.ASSOCIATION;	// hier gibt es nur eine Referenzierungsart
		} else {	// Konfigurationsmengen
			return getObjectSetTypeValues().getReferenceType();
		}
	}

	/**
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses MengenTyps enth�lt. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 */
	private synchronized ObjectSetTypeValues getObjectSetTypeValues() {
		if (_values == null) {
			_values = new ObjectSetTypeValues();
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

	/**
	 * Diese Klasse liest die Informationen f�r diesen MengenTyp mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData ConfigurationData}, da dort Informationen ben�tigt
	 * werden, die erst hier zur Verf�gung gestellt werden.
	 */
	private class ObjectSetTypeValues {
		/**
		 * die maximale Element-Anzahl einer Menge dieses Mengen-Typs
		 */
		private int _maximumElementCount;

		/**
		 * die minimale Element-Anzahl einer Menge dieses Mengen-Typs
		 */
		private int _minimumElementCount;

		/**
		 * gibt an, ob die Menge dynamisch ist, oder nicht
		 */
		private boolean _isMutable;

		/**
		 * die Referenzierungsart dieser Menge (Komposition, Aggregation oder Assoziation)
		 */
		private ReferenceType _referenceType;

		/**
		 * Konstruktor, der die Informationen dieses MengenTyps aus einem konfigurierenden Datensatz ausliest.
		 */
		public ObjectSetTypeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.mengenTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_minimumElementCount = deserializer.readInt();
				_maximumElementCount = deserializer.readInt();
				_isMutable = deserializer.readBoolean();
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
				final String errorMessage = "Die Eigenschaften des MengenTyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die maximale Element-Anzahl einer Menge dieses Mengen-Typs zur�ck.
		 *
		 * @return die maximale Element-Anzahl einer Menge dieses Mengen-Typs
		 */
		public int getMaximumElementCount() {
			return _maximumElementCount;
		}

		/**
		 * Gibt die minimale Element-Anzahl einer Menge dieses Mengen-Typs zur�ck.
		 *
		 * @return die minimale Element-Anzahl einer Menge dieses Mengen-Typs
		 */
		public int getMinimumElementCount() {
			return _minimumElementCount;
		}

		/**
		 * Gibt an, ob die Menge dynamisch ist, oder nicht.
		 *
		 * @return <code>true</code>, die Menge ist eine dynamische Menge, sonst <code>false</code>
		 */
		public boolean isMutable() {
			return _isMutable;
		}

		/**
		 * Gibt die Referenzierungsart dieser Menge (Komposition, Aggregation oder Assoziation) zur�ck.
		 *
		 * @return die Referenzierungsart dieser Menge (Komposition, Aggregation oder Assoziation)
		 */
		public ReferenceType getReferenceType() {
			return _referenceType;
		}
	}
}

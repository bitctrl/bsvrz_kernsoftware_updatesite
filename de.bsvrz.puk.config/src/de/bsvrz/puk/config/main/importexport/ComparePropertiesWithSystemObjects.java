/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

package de.bsvrz.puk.config.main.importexport;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigSystemObject;
import de.bsvrz.puk.config.xmlFile.properties.AspectProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeGroupProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeListProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaChangeInformation;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAspect;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAttributeType;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationConfigurationObject;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationData;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataField;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataList;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataset;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDefaultParameter;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDoubleDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerValueRange;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectElements;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectReference;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationState;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationString;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationTimeStamp;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationValueRange;
import de.bsvrz.puk.config.xmlFile.properties.DatasetElement;
import de.bsvrz.puk.config.xmlFile.properties.ListAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ObjectSetTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.PlainAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.TargetValue;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Diese Klasse vergleicht ein {@link SystemObjectProperties Versorgungsobjekt} mit einem {@link SystemObject System-Objekt}. Diese Klasse dient dem Import von
 * Versorgungsdateien in eine Konfiguration. Mit dem Konstruktor wird ein Objekt der Klasse {@link ConfigurationImport} und das aktuelle {@link de.bsvrz.dav.daf.main.config.DataModel
 * Datenmodell} �bergeben.
 * <p/>
 * Zwei Arten von Methoden werden hier angeboten (<code>isXYDifferent(Objekt1, Objekt2)</code>, <code>isXYProcessable(Objekt1, Objekt2)</code>). Die erste
 * Methodenart vergleicht zwei Objekte miteinander und gibt zur�ck, ob diese unterschiedlich sind. Die zweite Methodenart pr�ft, ob das angegebene System-Objekt
 * im Import weiterverwendet werden darf (R�ckgabewert <code>true</code>). Dies ist der Fall, wenn das Objekt nicht ver�ndert werden muss, weil das
 * Versorgungs-Objekt und das System-Objekt gleich sind und wenn das Objekt ge�ndert werden darf, falls es ge�ndert werden muss. Wenn das Objekt allerdings
 * ge�ndert werden muss, aber nicht ge�ndert werden darf, dann wird <code>false</code> zur�ckgegeben.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
class ComparePropertiesWithSystemObjects {

	/** Debug-Logger */

	private Debug _debug = Debug.getLogger();

	/** Import-Modul der Konfiguration */
	private final ConfigurationImport _configurationImport;

	/** Datenmodell */
	private final DataModel _dataModel;

	/** Objekt, dass dazu dient einen Attributwert zu erkennen, der im speziellen Locationcode-Distance Format angegeben wurde */
	private static Pattern _locationDistancePattern = Pattern.compile("[0-9]{1,5}\\s*-\\s*[0-9]{1,3}");

	/** Objekt, dass dazu dient einen Attributwert zu erkennen und zu parsen, der als Zahl angegeben wurde */
	private static final NumberFormat _parseNumberFormat = NumberFormat.getNumberInstance();

	static {
		_parseNumberFormat.setMinimumIntegerDigits(1);
		_parseNumberFormat.setMaximumIntegerDigits(999);
		_parseNumberFormat.setMinimumFractionDigits(0);
		_parseNumberFormat.setMaximumFractionDigits(999);
		_parseNumberFormat.setGroupingUsed(false);
	}

	/**
	 * Konstruktor erzeugt ein Objekt dieser Klasse und erh�lt das Import-Modul und das Datenmodell. Diese werden f�r die Vergleichsmethoden ben�tigt.
	 *
	 * @param configurationImport das Import-Modul der Konfiguration
	 * @param dataModel           das Datenmodell
	 */
	public ComparePropertiesWithSystemObjects(ConfigurationImport configurationImport, DataModel dataModel) {
		_configurationImport = configurationImport;
		_dataModel = dataModel;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property     ein Versorgungsobjekt
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isObjectProcessable(SystemObjectProperties property, SystemObject systemObject) {
		// bei konfigurierenden Objekten, muss das Objekt noch �berpr�ft werden, ob es in der in Bearbeitung befindlichen Version noch g�ltig ist.
		if(systemObject instanceof ConfigurationObject) {
			ConfigurationObject configObject = (ConfigurationObject)systemObject;
			if(configObject.getNotValidSince() != 0 && configObject.getNotValidSince() < configObject.getConfigurationArea().getModifiableVersion()) {
				return false;
			}
		}
		// f�r alle Properties m�ssen die Pid, der Name und die Info �berpr�ft werden
		if(isPidDifferent(property, systemObject)) return false;
		if(!isNameProcessable(property.getName(), systemObject)) return false;
		if(!isInfoProcessable(property.getInfo(), systemObject.getInfo())) return false;

		if(property instanceof AspectProperties && systemObject instanceof Aspect) {
			return isAspectProcessable((AspectProperties)property, (Aspect)systemObject);
		}
		else if(property instanceof AttributeListProperties && systemObject instanceof AttributeListDefinition) {
			return isAttributeListProcessable((AttributeListProperties)property, (AttributeListDefinition)systemObject);
		}
		else if(property instanceof AttributeTypeProperties && systemObject instanceof AttributeType) {
			return isAttributeTypeProcessable((AttributeTypeProperties)property, (AttributeType)systemObject);
		}
		else if(property instanceof AttributeGroupProperties && systemObject instanceof AttributeGroup) {
			return isAttributeGroupProcessable((AttributeGroupProperties)property, (AttributeGroup)systemObject);
		}
		else if(property instanceof ConfigurationAreaProperties && systemObject instanceof ConfigurationArea) {
			return isConfigurationAreaProcessable((ConfigurationAreaProperties)property, (ConfigurationArea)systemObject);
		}
		else if(property instanceof ObjectSetTypeProperties && systemObject instanceof ObjectSetType) {
			return isObjectSetTypeProcessable((ObjectSetTypeProperties)property, (ObjectSetType)systemObject);
		}
		else if(property instanceof SystemObjectTypeProperties && systemObject instanceof SystemObjectType) {
			return isSystemObjectTypeProcessable((SystemObjectTypeProperties)property, (SystemObjectType)systemObject);
		}
		else {
			return isSystemObjectProcessable(property, systemObject);
		}
	}

	/**
	 * Gibt zur�ck, ob sich die Import-Definition vom System-Objekt unterscheidet.
	 *
	 * @param property     ein Versorgungsobjekt
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls sich die Definition und das System-Objekt unterscheidet<br> <code>false</code>, sonst
	 */
	boolean isObjectDifferent(SystemObjectProperties property, SystemObject systemObject) {
		// f�r alle Objekt muss gelten:
		if(isPidDifferent(property, systemObject) || isNameDifferent(property.getName(), systemObject.getName())) {
			return true;
		}
		if(isInfoDifferent(property.getInfo(), systemObject.getInfo())) {
			_debug.finer("Info ist unterschiedlich von Pid", property.getPid());
			return true;
		}

		if(property instanceof AspectProperties) {
			return isAspectDifferent((AspectProperties)property, (Aspect)systemObject);
		}
		else if(property instanceof AttributeListProperties) {
			return isAttributeListDifferent((AttributeListProperties)property, (AttributeListDefinition)systemObject);
		}
		else if(property instanceof AttributeTypeProperties) {
			return isAttributeTypeDifferent((AttributeTypeProperties)property, (AttributeType)systemObject);
		}
		else if(property instanceof AttributeGroupProperties) {
			return isAttributeGroupDifferent((AttributeGroupProperties)property, (AttributeGroup)systemObject);
		}
		else if(property instanceof ConfigurationAreaProperties) {
			return isConfigurationAreaDifferent((ConfigurationAreaProperties)property, (ConfigurationArea)systemObject);
		}
		else if(property instanceof ObjectSetTypeProperties) {
			return isObjectSetTypeDifferent((ObjectSetTypeProperties)property, (ObjectSetType)systemObject);
		}
		else if(property instanceof SystemObjectTypeProperties) {
			return isSystemObjectTypeDifferent((SystemObjectTypeProperties)property, (SystemObjectType)systemObject);
		}
		else {
			return isSystemObjectDifferent(property, systemObject);
		}
	}

	/* ############### Allgemeine �berpr�fungen ############## */

	/**
	 * Pr�ft, ob die Pid des Versorgungsobjekts und die Pid des System-Objekts unterschiedlich ist.
	 *
	 * @param property     ein Versorgungsobjekt
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls die Pids unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isPidDifferent(SystemObjectProperties property, SystemObject systemObject) {
		return !property.getPid().equals(systemObject.getPid());
	}

	/**
	 * Pr�ft, ob der angegebene Name mit dem des System-Objekts �bereinstimmt. Wenn nicht, wird gepr�ft, ob der Name ver�ndert werden darf.
	 *
	 * @param name         der zu �berpr�fende Name
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls der Name gleich ist, oder falls der Name unterschiedlich ist, ob das System-Objekt ge�ndert werden darf<br>
	 *         <code>false</code>, sonst
	 */
	boolean isNameProcessable(String name, SystemObject systemObject) {
		if(isNameDifferent(name, systemObject.getName()) && systemObject.getType().isNameOfObjectsPermanent()) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die beiden angegebenen Namen unterschiedlich sind.
	 *
	 * @param name1 der erste zu vergleichende Name
	 * @param name2 der zweite zu vergleichende Name
	 *
	 * @return <code>true</code>, falls die Namen unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isNameDifferent(String name1, String name2) {
		return !name1.equals(name2);
	}

	/**
	 * Gibt zur�ck, ob die beiden Info-Objekte gleich sind, oder falls nicht, ob das Info-Objekt ver�ndert werden darf.
	 *
	 * @param info1 das erste Info-Objekt
	 * @param info2 das zweite Info-Objekt
	 *
	 * @return <code>true</code>, falls die Info-Objekte gleich sind, oder falls sie nicht gleich sind, ob der Datensatz ge�ndert werden darf<br>
	 *         <code>false</code>, sonst
	 */
	boolean isInfoProcessable(SystemObjectInfo info1, SystemObjectInfo info2) {
		if(isInfoDifferent(info1, info2) && !isConfigurationDataChangeable("atg.info")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die beiden angegebenen Info-Objekte unterschiedlich sind.
	 *
	 * @param info1 das erste Info-Objekt
	 * @param info2 das zweite Info-Objekt
	 *
	 * @return <code>true</code>, falls die Info-Objekte unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isInfoDifferent(SystemObjectInfo info1, SystemObjectInfo info2) {
		return !(info1.getShortInfoAsXML().equals(info2.getShortInfoAsXML()) && info1.getDescriptionAsXML().equals(info2.getDescriptionAsXML()));
	}

	/* ############### Aspekt ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property die Aspektdefinition einer Versorgungsdatei
	 * @param aspect   ein Aspekt (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAspectProcessable(AspectProperties property, Aspect aspect) {
		// es gibt nichts zu pr�fen
		return true;
	}

	/**
	 * Gibt zur�ck, ob sich die Import-Definition vom System-Objekt unterscheidet.
	 *
	 * @param property die Aspektdefinition einer Versorgungsdatei
	 * @param aspect   ein Aspekt (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Aspektdefinition zum angegebenen Aspekt unterschiedlich ist, sonst <code>false</code>
	 */
	boolean isAspectDifferent(AspectProperties property, Aspect aspect) {
		// nur die Kodierung muss gepr�ft werden - und die gibt es nicht mehr
		return false;
	}

	/* ############### Attributliste ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property die Attributlistendefinition einer Versorgungsdatei
	 * @param atl      eine Attributliste (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeListProcessable(AttributeListProperties property, AttributeListDefinition atl) {
		// hier muss nur das AttributeSet �berpr�ft werden
		return isAttributeSetProcessable(property.getAttributeAndAttributeList(), atl);
	}

	/**
	 * Pr�ft, ob die Attributlistendefinition und die Attributliste unterschiedlich sind.
	 *
	 * @param property die Attributlistendefinition einer Versorgungsdatei
	 * @param atl      eine Attributliste (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und die Attributliste unterschiedlich ist, sonst <code>false</code>
	 */
	boolean isAttributeListDifferent(AttributeListProperties property, AttributeListDefinition atl) {
		// hier muss nur das AttributeSet �berpr�ft werden
		return isAttributeSetDifferent(property.getAttributeAndAttributeList(), atl);
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationAttributes die zu importierenden Attribute einer Attributmenge
	 * @param attributeSet            die zu �berpr�fende Attributmenge
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeSetProcessable(AttributeProperties[] configurationAttributes, AttributeSet attributeSet) {
		// In Bearbeitung befindliche Objekte k�nnen nach belieben ge�ndert werden, deshalb braucht dieser Fall hier nicht ber�cksichtigt werden.
		final ObjectSet objectSet = attributeSet.getObjectSet("Attribute");

		// Eine Menge kann nicht hinzugef�gt werden, wenn sie nicht da ist.
		if(objectSet == null) return false;

		// Stimmen die Mengen �berein?
		final NonMutableSet nonMutableAttributeSet = (NonMutableSet)objectSet;
		final List<SystemObject> attributes = nonMutableAttributeSet.getElementsInVersion(attributeSet.getConfigurationArea().getModifiableVersion());

		if(isAttributeSetDifferent(configurationAttributes, attributeSet) && !isSetChangeable(attributeSet, "Attribute")) {
			// Anzahl der Elemente muss gleich sein	- Annahme: es ist keine dynamische Menge
			if(configurationAttributes.length != attributes.size()) return false;

			// sind die Attribute unterschiedlich?
			int position = 1;
			for(AttributeProperties configurationAttribute : configurationAttributes) {
				String name = "";
				if(configurationAttribute instanceof ListAttributeProperties) {
					ListAttributeProperties attributeList = (ListAttributeProperties)configurationAttribute;
					name = attributeList.getName();
				}
				else {
					PlainAttributeProperties attribute = (PlainAttributeProperties)configurationAttribute;
					name = attribute.getName();
				}
				// Entsprechendes Element aus der bestehenden Liste holen.
				Attribute attribute = null;
				for(SystemObject systemObject : attributes) {
					if(name.equals(systemObject.getName())) {
						attribute = (Attribute)systemObject;
						break;	// passendes Attribut gefunden
					}
				}
				if(attribute == null) {
					return false;
				}
				else if(!isAttributeProcessable(configurationAttribute, attribute, position)) return false;
				position++;
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob die zu importierenden Attribute und die Attribute der angegebenen Attributmenge unterschiedlich sind.
	 *
	 * @param configurationAttributes die zu importierenden Attribute
	 * @param attributeSet            die zu �berpr�fende Attributmenge
	 *
	 * @return <code>true</code>, falls die zu importierenden Attribute nicht mit den Attributen der Menge �bereinstimmen, sonst <code>false</code>
	 */
	boolean isAttributeSetDifferent(AttributeProperties[] configurationAttributes, AttributeSet attributeSet) {
		final ObjectSet objectSet = attributeSet.getObjectSet("Attribute");

		// Ist die Menge vorhanden? Sie muss vorhanden sein!
		if(objectSet == null) {
			_debug.finer("Die Menge ist nicht vorhanden, muss aber vorhanden sein an", attributeSet.getPidOrNameOrId());
			return true;
		}

		// Stimmen die Mengen �berein?
		final NonMutableSet nonMutableAttributeSet = (NonMutableSet)objectSet;
		final List<SystemObject> attributes = nonMutableAttributeSet.getElementsInModifiableVersion();

		// Anzahl der Elemente muss gleich sein	- Annahme: es ist keine dynamische Menge
		if(configurationAttributes.length != attributes.size()) {
			_debug.finer("Die Anzahl der Elemente ist unterschiedlich", attributeSet.getPidOrNameOrId());
			return true;
		}

		// Sind die Attribute der Menge gleich?
		int position = 1;
		for(AttributeProperties configurationAttribute : configurationAttributes) {
			String name = "";
			if(configurationAttribute instanceof ListAttributeProperties) {
				ListAttributeProperties attributeList = (ListAttributeProperties)configurationAttribute;
				name = attributeList.getName();
			}
			else {
				PlainAttributeProperties attribute = (PlainAttributeProperties)configurationAttribute;
				name = attribute.getName();
			}
			_debug.finer("Folgendes Attribut wird untersucht " + name);
			// Entsprechendes Element aus der bestehenden Liste holen.
			Attribute attribute = null;
			for(SystemObject systemObject : attributes) {
				Attribute att = (Attribute)systemObject;
				if(name.equals(att.getName())) {
					attribute = att;
					break;	// passendes Attribut gefunden
				}
			}
			if(isAttributeDifferent(configurationAttribute, attribute, position)) {
				_debug.finer("Attribut ist verschieden");
				return true;
			}
			position++;
		}
		return false;
	}

	/* ############### Attribut ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param attributeProperties zu importierendes Attribut
	 * @param attribute               zu �berpr�fendes Attribut
	 * @param position                die Position des Attributs
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeProcessable(AttributeProperties attributeProperties, Attribute attribute, int position) {
		// Attribut ist gar nicht vorhanden
		if(attribute == null) return false;

		// Attribut auf Gleichheit pr�fen
		String name;
		SystemObjectInfo info;
		int maxCount;
		boolean isCountVariable;
		AttributeType attributeType;
		String aDefault = null;
		if(attributeProperties instanceof ListAttributeProperties) {
			ListAttributeProperties listAttributeProperties = (ListAttributeProperties)attributeProperties;
			name = listAttributeProperties.getName();
			info = listAttributeProperties.getInfo();
			maxCount = listAttributeProperties.getMaxCount();
			isCountVariable = listAttributeProperties.getTargetValue() == TargetValue.VARIABLE;
			attributeType = (AttributeType)getObject(listAttributeProperties.getAttributeTypePid());
		}
		else {
			PlainAttributeProperties configurationAttribute = (PlainAttributeProperties)attributeProperties;
			name = configurationAttribute.getName();
			info = configurationAttribute.getInfo();
			maxCount = configurationAttribute.getMaxCount();
			isCountVariable = configurationAttribute.getTargetValue() == TargetValue.VARIABLE;
			attributeType = (AttributeType)getObject(configurationAttribute.getAttributeTypePid());
			aDefault = configurationAttribute.getDefault();
		}

		if(!isNameProcessable(name, attribute)) {
			_debug.finer(
					"Ist der Name dieses Attributs " + attribute.getName() + " �nderbar? " + attribute.getAttributeType().getType().isNameOfObjectsPermanent()
			);
			_debug.finer("Ist der Name dieses Attributs " + attribute.getName() + " �nderbar? " + attribute.getType().isNameOfObjectsPermanent());
			return false;
		}
		if(!isInfoProcessable(info, attribute.getInfo())) return false;
		if(maxCount != attribute.getMaxCount() || isCountVariable != attribute.isCountVariable() || attributeType != attribute.getAttributeType()
		   || position != attribute.getPosition()) {
			if(!isConfigurationDataChangeable("atg.attributEigenschaften")) {
				return false;
			}
		}

		// Default-Wert �berpr�fen
		if(!isDefaultProcessable(attribute, aDefault)) return false;

		return true;
	}

	/**
	 * Pr�ft, ob das zu importierende Attribut und das zu �berpr�fende Attribut unterschiedlich ist.
	 *
	 * @param attributeProperties das zu importierende Attribut
	 * @param attribute               das zu �berpr�fende Attribut
	 * @param position                die Position des Attributs
	 *
	 * @return <code>true</code>, falls die Attribute unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isAttributeDifferent(AttributeProperties attributeProperties, Attribute attribute, int position) {
		// Attribut ist gar nicht vorhanden
		if(attribute == null) {
			_debug.finer("Attribut ist nicht vorhanden");
			return true;
		}

		// Attribut auf Gleichheit pr�fen
		String name;
		SystemObjectInfo info;
		int maxCount;
		boolean isCountVariable;
		AttributeType attributeType;
		String aDefault = null;
		if(attributeProperties instanceof ListAttributeProperties) {
			ListAttributeProperties listAttributeProperties = (ListAttributeProperties)attributeProperties;
			name = listAttributeProperties.getName();
			info = listAttributeProperties.getInfo();
			maxCount = listAttributeProperties.getMaxCount();
			isCountVariable = listAttributeProperties.getTargetValue() == TargetValue.VARIABLE;
			attributeType = (AttributeType)getObject(listAttributeProperties.getAttributeTypePid());
		}
		else {
			PlainAttributeProperties configurationAttribute = (PlainAttributeProperties)attributeProperties;
			name = configurationAttribute.getName();
			info = configurationAttribute.getInfo();
			maxCount = configurationAttribute.getMaxCount();
			isCountVariable = configurationAttribute.getTargetValue() == TargetValue.VARIABLE;
			attributeType = (AttributeType)getObject(configurationAttribute.getAttributeTypePid());
			aDefault = configurationAttribute.getDefault();
		}

		if(isNameDifferent(name, attribute.getName())) {
			_debug.finer("Der Name des Attributs ist unterschiedlich (alt|neu) (" + attribute.getName() + "|" + name + ")");
			return true;
		}
		if(isInfoDifferent(info, attribute.getInfo())) {
			_debug.finer("Die Info des Attributs ist unterschiedlich", attribute.getNameOrPidOrId());
			return true;
		}
		if(maxCount != attribute.getMaxCount()) return true;
		if(isCountVariable != attribute.isCountVariable()) return true;
		if(attributeType != attribute.getAttributeType()) return true;
		if(position != attribute.getPosition()) return true;

		// Default-Werte �berpr�fen
		if(isDefaultDifferent(attribute, aDefault)) return true;

		return false;
	}

	/* ############### Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property      die Attribut-Typ-Definition einer Versorgungsdatei
	 * @param attributeType ein Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeTypeProcessable(AttributeTypeProperties property, AttributeType attributeType) {
		// Falls der Default nicht ver�ndert werden darf, braucht nicht weiter gepr�ft werden.
		if(!isDefaultProcessable(attributeType, property.getDefault())) return false;

		final ConfigurationAttributeType configurationAttributeType = property.getAttributeType();
		if(attributeType instanceof StringAttributeType && configurationAttributeType instanceof ConfigurationString) {
			return isStringAttributeTypeProcessable((ConfigurationString)configurationAttributeType, (StringAttributeType)attributeType);
		}
		else if(attributeType instanceof IntegerAttributeType && configurationAttributeType instanceof ConfigurationIntegerDef) {
			return isIntegerAttributeTypeProcessable((ConfigurationIntegerDef)configurationAttributeType, (IntegerAttributeType)attributeType);
		}
		else if(attributeType instanceof DoubleAttributeType && configurationAttributeType instanceof ConfigurationDoubleDef) {
			return isDoubleAttributeTypeProcessable((ConfigurationDoubleDef)configurationAttributeType, (DoubleAttributeType)attributeType);
		}
		else if(attributeType instanceof TimeAttributeType && configurationAttributeType instanceof ConfigurationTimeStamp) {
			return isTimeAttributeTypeProcessable((ConfigurationTimeStamp)configurationAttributeType, (TimeAttributeType)attributeType);
		}
		else if(attributeType instanceof ReferenceAttributeType && configurationAttributeType instanceof ConfigurationObjectReference) {
			return isReferenceAttributeTypeProcessable((ConfigurationObjectReference)configurationAttributeType, (ReferenceAttributeType)attributeType);
		}
		return false;
	}

	/**
	 * Pr�ft, ob der zu importierende Attribut-Typ und der zu �berpr�fende Attribut-Typ unterschiedlich sind.
	 *
	 * @param property      die Attribut-Typ-Definition einer Versorgungsdatei
	 * @param attributeType ein Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls der Attribut-Typ sich von der Definition unterscheidet, sonst <code>false</code>
	 */
	boolean isAttributeTypeDifferent(AttributeTypeProperties property, AttributeType attributeType) {
		// Default-Werte �berpr�fen
		if(!isDefaultDifferent(attributeType, property.getDefault())) return false;

		final ConfigurationAttributeType configurationAttributeType = property.getAttributeType();
		if(attributeType instanceof StringAttributeType && configurationAttributeType instanceof ConfigurationString) {
			return isStringAttributeTypeDifferent((ConfigurationString)configurationAttributeType, (StringAttributeType)attributeType);
		}
		else if(attributeType instanceof IntegerAttributeType && configurationAttributeType instanceof ConfigurationIntegerDef) {
			return isIntegerAttributeTypeDifferent((ConfigurationIntegerDef)configurationAttributeType, (IntegerAttributeType)attributeType);
		}
		else if(attributeType instanceof DoubleAttributeType && configurationAttributeType instanceof ConfigurationDoubleDef) {
			return isDoubleAttributeTypeDifferent((ConfigurationDoubleDef)configurationAttributeType, (DoubleAttributeType)attributeType);
		}
		else if(attributeType instanceof TimeAttributeType && configurationAttributeType instanceof ConfigurationTimeStamp) {
			return isTimeAttributeTypeDifferent((ConfigurationTimeStamp)configurationAttributeType, (TimeAttributeType)attributeType);
		}
		else if(attributeType instanceof ReferenceAttributeType && configurationAttributeType instanceof ConfigurationObjectReference) {
			return isReferenceAttributeTypeDifferent((ConfigurationObjectReference)configurationAttributeType, (ReferenceAttributeType)attributeType);
		}
		return true;
	}

	/**
	 * Pr�ft, ob der Datensatz f�r die Default-Werte ge�ndert werden darf, falls sich der Default-Wert des System-Objekts vom zweiten Parameter unterscheidet.
	 *
	 * @param systemObject bestehendes System-Objekt, dessen Default-Wert �berpr�ft werden muss
	 * @param aDefault     zu importierender Default-Wert
	 *
	 * @return <code>true</code>, falls die Default-Werte gleich sind und nicht ver�ndert werden m�ssen und falls sie nicht gleich sind, ob sich der Datensatz
	 *         �ndern l�sst <br> <code>false</code>, falls die Default-Werte unterschiedlich sind und der Datensatz nicht ge�ndert werden darf.
	 */
	boolean isDefaultProcessable(SystemObject systemObject, String aDefault) {
		// Default-Wert ist verschieden, aber nicht �nderbar.
		if(isDefaultDifferent(systemObject, aDefault) && !isConfigurationDataChangeable("atg.defaultAttributwert", "asp.eigenschaften")) return false;
		return true;
	}

	/**
	 * Pr�ft, ob der Default-Wert am System-Objekt gleich mit dem zu importierenden Default-Wert ist.
	 *
	 * @param systemObject bestehendes System-Objekt, dessen Default-Wert �berpr�ft werden muss
	 * @param aDefault     zu importierender Default-Wert
	 *
	 * @return <code>true</code>, falls die Default-Werte unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isDefaultDifferent(SystemObject systemObject, String aDefault) {
		final AttributeGroup atg = getAttributeGroup("atg.defaultAttributwert");
		final Aspect asp = getAspect("asp.eigenschaften");

		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		Data data = _configurationImport.getConfigurationData(systemObject, atgUsage);
//		Data data = systemObject.getConfigurationData(atgUsage);

		// kein Default soll gesetzt werden und es ist kein Default am Typ vorhanden
		if(aDefault == null && data == null) return false;

		// pr�fen, ob die Default-Werte gleich sind
		if(aDefault != null && data != null) {
			if(aDefault.equals(data.getTextValue("wert").getText())) return false;
		}
		return true;
	}

	/* ############### String - Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationString zu importierende Zeichenketten-Attribut-Typ-Definition
	 * @param stringAttributeType ein Zeichenketten-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isStringAttributeTypeProcessable(ConfigurationString configurationString, StringAttributeType stringAttributeType) {
		if(isStringAttributeTypeDifferent(configurationString, stringAttributeType) && !isConfigurationDataChangeable(
				"atg.zeichenkettenAttributTypEigenschaften"
		)) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die Zeichenketten-Attribut-Typ-Definition sich von dem Zeichenketten-Attribut-Typen unterscheidet.
	 *
	 * @param configurationString eine Zeichenketten-Attribut-Typ-Definition
	 * @param stringAttributeType ein Zeichenketten-Attribut-Typ
	 *
	 * @return <code>true</code>, falls sich die Definition vom Attribut-Typen unterscheidet, sonst <code>false</code>
	 */
	boolean isStringAttributeTypeDifferent(ConfigurationString configurationString, StringAttributeType stringAttributeType) {
		// gibt es diesen Datensatz �berhaupt?
		final AttributeGroup atg = getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften");
		final Aspect asp = getAspect("asp.eigenschaften");

		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		if(_configurationImport.getConfigurationData(stringAttributeType, atgUsage) == null) return true;
//		if(stringAttributeType.getConfigurationData(atgUsage) == null) return true;

		// String-AttributTyp vergleichen
		if(configurationString.getLength() != stringAttributeType.getMaxLength()
		   || !configurationString.getStringEncoding().equals(stringAttributeType.getEncodingName())) {
			return true;
		}
		return false;
	}

	/* ############### Integer - Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationIntegerDef eine Ganzzahl-Attribut-Typ-Definition einer Versorgungsdatei
	 * @param integerAttributeType    ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeProcessable(ConfigurationIntegerDef configurationIntegerDef, IntegerAttributeType integerAttributeType) {
		if(isIntegerAttributeTypePropertiesDifferent(configurationIntegerDef, integerAttributeType) && !isIntegerAttributeTypePropertiesProcessble(
				configurationIntegerDef, integerAttributeType
		)) {
			_debug.finer("Ganzzahl-Attribut-Typ-Eigenschaften");
			return false;
		}
		ConfigurationValueRange configurationValueRange = null;
		for(ConfigurationIntegerValueRange integerValueRange : configurationIntegerDef.getValueRangeAndState()) {
			if(integerValueRange instanceof ConfigurationValueRange) {
				configurationValueRange = (ConfigurationValueRange)integerValueRange;
				break;
			}
		}
		if(!isIntegerAttributeTypeValueRangeProcessable(configurationValueRange, integerAttributeType.getRange())) {
			_debug.finer("Ganzzahl-Attribut-Typ-Bereich");
			return false;
		}
		if(!isIntegerAttributeTypeValueStatesProcessable(configurationIntegerDef.getValueRangeAndState(), integerAttributeType)) {
			_debug.finer("Ganzzahl-Attribut-Typ-Zust�nde");
			return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Ganzzahl-Attribut-Typ-Definition vom Ganzzahl-Attribut-Typ (System-Objekt) unterscheidet.
	 *
	 * @param configurationIntegerDef eine Ganzzahl-Attribut-Typ-Definition einer Versorgungsdatei
	 * @param integerAttributeType    ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition vom System-Objekt unterscheidet, <br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeDifferent(ConfigurationIntegerDef configurationIntegerDef, IntegerAttributeType integerAttributeType) {
		if(isIntegerAttributeTypePropertiesDifferent(configurationIntegerDef, integerAttributeType)) return true;
		ConfigurationValueRange configurationValueRange = null;
		for(ConfigurationIntegerValueRange integerValueRange : configurationIntegerDef.getValueRangeAndState()) {
			if(integerValueRange instanceof ConfigurationValueRange) {
				configurationValueRange = (ConfigurationValueRange)integerValueRange;
				break;
			}
		}
		if(isIntegerAttributeTypeValueRangeDifferent(configurationValueRange, integerAttributeType.getRange())) return true;
		if(isIntegerAttributeTypeValueStatesDifferent(configurationIntegerDef.getValueRangeAndState(), integerAttributeType)) return true;
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationIntegerDef eine Ganzzahl-Attribut-Typ-Definition einer Versorgungsdatei
	 * @param integerAttributeType    ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypePropertiesProcessble(ConfigurationIntegerDef configurationIntegerDef, IntegerAttributeType integerAttributeType) {
		if(isIntegerAttributeTypePropertiesDifferent(configurationIntegerDef, integerAttributeType) && !isConfigurationDataChangeable(
				"atg.ganzzahlAttributTypEigenschaften"
		)) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Eigenschaften einer Ganzzahl-Attribut-Typ-Definition von den Eigenschaften eines Ganzzahl-Attribut-Typs (System-Objekt) unterscheidet.
	 *
	 * @param configurationIntegerDef eine Ganzzahl-Attribut-Typ-Definition einer Versorgungsdatei
	 * @param integerAttributeType    ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition vom System-Objekt unterscheidet, <br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypePropertiesDifferent(ConfigurationIntegerDef configurationIntegerDef, IntegerAttributeType integerAttributeType) {
		// Anzahl Bytes ermitteln
		int bitCount = configurationIntegerDef.getBits();
		int byteCount;
		if(bitCount <= 8) {
			byteCount = 1;
		}
		else if(bitCount <= 16) {
			byteCount = 2;
		}
		else if(bitCount <= 32) {
			byteCount = 4;
		}
		else {
			byteCount = 8;
		}
		if(byteCount != integerAttributeType.getByteCount()) return true;
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationValueRange eine Definition eines Wertebereichs eines Ganzzahl-Attribut-Typs
	 * @param integerValueRange       ein Wertebereich eines Ganzzahl-Attribut-Typs (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueRangeProcessable(ConfigurationValueRange configurationValueRange, IntegerValueRange integerValueRange) {
		if(!isConfigurationDataChangeable("atg.ganzzahlAttributTypEigenschaften")) {
			if(configurationValueRange == null && integerValueRange == null) {
				return true;
			}
			else if(configurationValueRange != null && integerValueRange != null) {
				if(!isInfoProcessable(configurationValueRange.getInfo(), integerValueRange.getInfo())) {
					_debug.finer("Info ist unterschiedlich");
					return false;
				}
				if(!isIntegerAttributeTypeValueRangePropertiesProcessable(configurationValueRange, integerValueRange)) {
					_debug.finer("Bereichs-Eigenschaften sind unterschiedlich");
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition eines Wertebereichs eines Ganzzahl-Attribut-Typs vom entsprechenden System-Objekt unterscheidet.
	 *
	 * @param configurationValueRange eine Definition eines Wertebereichs eines Ganzzahl-Attribut-Typs
	 * @param integerValueRange       ein Wertebereich eines Ganzzahl-Attribut-Typs (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt unterschiedlich sind <br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueRangeDifferent(ConfigurationValueRange configurationValueRange, IntegerValueRange integerValueRange) {
		if(configurationValueRange == null && integerValueRange == null) {
			return false;
		}
		else if(configurationValueRange != null && integerValueRange != null) {
			// Info �berpr�fen
			if(isInfoDifferent(configurationValueRange.getInfo(), integerValueRange.getInfo())) return true;
			if(isIntegerAttributeTypeValueRangePropertiesDifferent(configurationValueRange, integerValueRange)) return true;
		}
		else {
			return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationValueRange eine Definition eines Wertebereichs eines Ganzzahl-Attribut-Typs
	 * @param integerValueRange       ein Wertebereich eines Ganzzahl-Attribut-Typs (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueRangePropertiesProcessable(ConfigurationValueRange configurationValueRange, IntegerValueRange integerValueRange) {
		if(isIntegerAttributeTypeValueRangePropertiesDifferent(configurationValueRange, integerValueRange) && !isConfigurationDataChangeable(
				"atg.werteBereichsEigenschaften"
		)) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Eigenschaften eines Wertebereichs eines Ganzzahl-Attribut-Typ vom entsprechenden System-Objekt unterscheidet.
	 *
	 * @param configurationValueRange eine Definition eines Wertebereichs eines Ganzzahl-Attribut-Typs
	 * @param integerValueRange       ein Wertebereich eines Ganzzahl-Attribut-Typs (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt unterschiedlich sind <br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueRangePropertiesDifferent(ConfigurationValueRange configurationValueRange, IntegerValueRange integerValueRange) {
		// gibt es diesen Datensatz �berhaupt?
		final AttributeGroup atg = getAttributeGroup("atg.werteBereichsEigenschaften");
		final Aspect asp = getAspect("asp.eigenschaften");

		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		if(_configurationImport.getConfigurationData(integerValueRange, atgUsage) == null) return true;
//		if(integerValueRange.getConfigurationData(atgUsage) == null) return true;

		// Eigenschaften �berpr�fen
		if(configurationValueRange.getMinimum() != integerValueRange.getMinimum() || configurationValueRange.getMaximum() != integerValueRange.getMaximum()
		   || configurationValueRange.getScale() != integerValueRange.getConversionFactor()
		   || !configurationValueRange.getUnit().equals(integerValueRange.getUnit())) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param rangeAndStates       eine Menge von Definition von Zust�nden eines Ganzzahl-Attribut-Typs
	 * @param integerAttributeType ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueStatesProcessable(ConfigurationIntegerValueRange[] rangeAndStates, IntegerAttributeType integerAttributeType) {
		// Elemente in der Menge sind durch Komposition an die Menge gebunden. D.h. die Menge kann und darf nicht ver�ndert werden
		if(isIntegerAttributeTypeValueStatesDifferent(rangeAndStates, integerAttributeType)) {
			// Anzahl der Zust�nde ermitteln
			int counter = 0;
			for(ConfigurationIntegerValueRange integerValueRange : rangeAndStates) {
				if(integerValueRange instanceof ConfigurationState) {
					counter++;
				}
			}
			if(counter > 0 && integerAttributeType.getObjectSet("zust�nde") == null) {
				_debug.finer("Es sollen Zust�nde vorhanden sein, sind aber nicht", integerAttributeType.getPidOrNameOrId());
				return false;
			}

			// Zust�nde bestimmen
			List<IntegerValueState> states = integerAttributeType.getStates();

			// stimmt die Anzahl?
			if(states.size() != counter && !isSetChangeable(integerAttributeType, "zust�nde")) {
				_debug.finer("Anzahl stimmt nicht", integerAttributeType.getPidOrNameOrId());
				return false;
			}

			for(ConfigurationIntegerValueRange rangeOrState : rangeAndStates) {
				if(rangeOrState instanceof ConfigurationState) {
					ConfigurationState configurationState = (ConfigurationState)rangeOrState;
					// passendes Gegenst�ck raussuchen
					IntegerValueState valueState = null;
					for(IntegerValueState integerValueState : states) {
						if(integerValueState.getName().equals(configurationState.getName())) {
							// richtigen State gefunden
							valueState = integerValueState;
							break;
						}
					}
					if(valueState == null && !isSetChangeable(integerAttributeType, "zust�nde")) {
						_debug.finer("Hier fehlt ein Zustand " + configurationState.getName() + " Typ: " + integerAttributeType.getPidOrNameOrId());
						return false;	// Der State muss vorhanden sein!
					}
					if(valueState != null) {
						if(!isInfoProcessable(configurationState.getInfo(), valueState.getInfo())) {
							_debug.finer("Info eines Zustands ist unterschiedlich", integerAttributeType.getPidOrNameOrId());
							return false;
						}
						if(configurationState.getValue() != valueState.getValue() && !isConfigurationDataChangeable("atg.werteZustandsEigenschaften")) {
							_debug.finer(
									"Wert des Zustands '" + valueState.getName() + "' ist unterschiedlich (alt|neu): (" + valueState.getValue() + "|"
									+ configurationState.getValue() + ")", integerAttributeType.getPidOrNameOrId()
							);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die definierten Zust�nde von den Zust�nden des Ganzzahl-Attribut-Typs (System-Objekt) unterscheiden.
	 *
	 * @param rangeAndStates       eine Menge von Definition von Zust�nden eines Ganzzahl-Attribut-Typs
	 * @param integerAttributeType ein Ganzzahl-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die definierten Zust�nde von den Zust�nden des Ganzzahl-Attribut-Typs unterscheiden <br><code>false</code>, sonst
	 */
	boolean isIntegerAttributeTypeValueStatesDifferent(ConfigurationIntegerValueRange[] rangeAndStates, IntegerAttributeType integerAttributeType) {
		// Elemente in der Menge sind durch Komposition an die Menge gebunden. D.h. die Menge kann und darf nicht ver�ndert werden

		// Anzahl der Zust�nde ermitteln
		int counter = 0;
		for(ConfigurationIntegerValueRange integerValueRange : rangeAndStates) {
			if(integerValueRange instanceof ConfigurationState) {
				counter++;
			}
		}
		if(counter > 0 && integerAttributeType.getObjectSet("zust�nde") == null) {
			_debug.finer("Es sollen Zust�nde vorhanden sein, sind aber nicht", integerAttributeType.getPidOrNameOrId());
			return true;
		}

		// Zust�nde bestimmen
		List<IntegerValueState> states = integerAttributeType.getStates();

		// stimmt die Anzahl?
		if(states.size() != counter) {
			_debug.finer("Anzahl stimmt nicht", integerAttributeType.getPidOrNameOrId());
			return true;
		}

		for(ConfigurationIntegerValueRange rangeOrState : rangeAndStates) {
			if(rangeOrState instanceof ConfigurationState) {
				ConfigurationState configurationState = (ConfigurationState)rangeOrState;
				// passendes Gegenst�ck raussuchen
				IntegerValueState valueState = null;
				for(IntegerValueState integerValueState : states) {
					if(integerValueState.getName().equals(configurationState.getName())) {
						// richtigen State gefunden
						valueState = integerValueState;
					}
				}
				if(valueState == null) {
					_debug.finer("Hier fehlt ein Zustand " + configurationState.getName() + " Typ: " + integerAttributeType.getPidOrNameOrId());
					return true;	// Der State muss vorhanden sein!
				}
				else {
					if(isInfoDifferent(configurationState.getInfo(), valueState.getInfo())) {
						_debug.finer("Info eines Zustands ist unterschiedlich", integerAttributeType.getPidOrNameOrId());
						return true;
					}
					if(configurationState.getValue() != valueState.getValue()) {
						_debug.finer("Wert des Zustands ist unterschiedlich", integerAttributeType.getPidOrNameOrId());
						return true;
					}
				}
			}
		}
		return false;
	}

	/* ############### Double - Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationDoubleDef eine Definition eines Flie�komma-Attribut-Typs
	 * @param doubleAttributeType    ein Flie�komma-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isDoubleAttributeTypeProcessable(ConfigurationDoubleDef configurationDoubleDef, DoubleAttributeType doubleAttributeType) {
		if(isDoubleAttributeTypeDifferent(configurationDoubleDef, doubleAttributeType)
		   && !isConfigurationDataChangeable("atg.kommazahlAttributTypEigenschaften")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition eines Flie�komma-Attribut-Typs von einem Flie�komma-Attribut-Typ (System-Objekt) unterscheidet.
	 *
	 * @param configurationDoubleDef eine Definition eines Flie�komma-Attribut-Typs
	 * @param doubleAttributeType    ein Flie�komma-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, wenn sich die Definition eines Flie�komma-Attribut-Typs von einem Flie�komma-Attribut-Typ (System-Objekt) unterscheidet
	 *         <br><code>false</code>, sonst
	 */
	boolean isDoubleAttributeTypeDifferent(ConfigurationDoubleDef configurationDoubleDef, DoubleAttributeType doubleAttributeType) {
		// gibt es diesen Datensatz �berhaupt?
		final AttributeGroup atg = getAttributeGroup("atg.kommazahlAttributTypEigenschaften");
		final Aspect asp = getAspect("asp.eigenschaften");

		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		if(_configurationImport.getConfigurationData(doubleAttributeType, atgUsage) == null) return true;
//		if(doubleAttributeType.getConfigurationData(atgUsage) == null) return true;

		// Genauigkeit �berpr�fen
		switch(configurationDoubleDef.getAccuracy()) {
			case DOUBLE:
				if(doubleAttributeType.getAccuracy() != 1) return true;
				break;
			case FLOAT:
				if(doubleAttributeType.getAccuracy() != 0) return true;
				break;
			default:
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + configurationDoubleDef.getAccuracy() + "' wird beim Import nicht unterst�tzt");
		}
		// Einheit �berpr�fen
		if(!configurationDoubleDef.getUnit().equals(doubleAttributeType.getUnit())) return true;
		return false;
	}

	/* ############### Time - Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationTimeStamp eine Definition eines Zeitstempel-Attribut-Typs
	 * @param timeAttributeType      ein Zeitstempel-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isTimeAttributeTypeProcessable(ConfigurationTimeStamp configurationTimeStamp, TimeAttributeType timeAttributeType) {
		if(isTimeAttributeTypeDifferent(configurationTimeStamp, timeAttributeType)
		   && !isConfigurationDataChangeable("atg.zeitstempelAttributTypEigenschaften")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition eines Zeitstempel-Attribut-Typs von einem Zeitstempel-Attribut-Typ (System-Objekt) unterscheidet.
	 *
	 * @param configurationTimeStamp eine Definition eines Zeitstempel-Attribut-Typs
	 * @param timeAttributeType      ein Zeitstempel-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition eines Zeitstempel-Attribut-Typs von einem Zeitstempel-Attribut-Typ (System-Objekt) unterscheidet <br>
	 *         <code>false</code>, sonst
	 */
	boolean isTimeAttributeTypeDifferent(ConfigurationTimeStamp configurationTimeStamp, TimeAttributeType timeAttributeType) {
		// gibt es diesen Datensatz �berhaupt?
		final AttributeGroup atg = getAttributeGroup("atg.zeitstempelAttributTypEigenschaften");
		final Aspect asp = getAspect("asp.eigenschaften");

		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		if(_configurationImport.getConfigurationData(timeAttributeType, atgUsage) == null) return true;
//		if(timeAttributeType.getConfigurationData(atgUsage) == null) return true;

		// Genauigkeit �berpr�fen
		switch(configurationTimeStamp.getAccuracy()) {
			case MILLISECONDS:
				if(timeAttributeType.getAccuracy() != 1) return true;
				break;
			case SECONDS:
				if(timeAttributeType.getAccuracy() != 0) return true;
				break;
			default:
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + configurationTimeStamp.getAccuracy() + "' wird beim Import nicht unterst�tzt");
		}
		// Pr�fen, ob relative Zeitangaben benutzt werden, oder nicht.
		if(configurationTimeStamp.getRelative() != timeAttributeType.isRelative()) return true;
		return false;
	}

	/* ############### Reference - Attribut-Typ ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationObjectReference eine Definition eines Referenz-Attribut-Typs
	 * @param referenceAttributeType       ein Referenz-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isReferenceAttributeTypeProcessable(ConfigurationObjectReference configurationObjectReference, ReferenceAttributeType referenceAttributeType) {
		if(isReferenceAttributeTypeDifferent(configurationObjectReference, referenceAttributeType) && !isConfigurationDataChangeable(
				"atg.objektReferenzAttributTypEigenschaften"
		)) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition eines Referenz-Attribut-Typs von einem Referenz-Attribut-Typ (System-Objekt) unterscheidet.
	 *
	 * @param configurationObjectReference eine Definition eines Referenz-Attribut-Typs
	 * @param referenceAttributeType       ein Referenz-Attribut-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition eines Referenz-Attribut-Typs von einem Referenz-Attribut-Typ (System-Objekt) unterscheidet <br>
	 *         <code>false</code>, sonst
	 */
	boolean isReferenceAttributeTypeDifferent(ConfigurationObjectReference configurationObjectReference, ReferenceAttributeType referenceAttributeType) {
		if(_configurationImport.getConfigurationData(referenceAttributeType, getAttributeGroup("atg.objektReferenzAttributTypEigenschaften")) == null) {
			return true;
		}
//		if(referenceAttributeType.getConfigurationData(getAttributeGroup("atg.objektReferenzAttributTypEigenschaften")) == null) return true;
		// Referenzierungsart �berpr�fen
		if(configurationObjectReference.getReferenceType() != referenceAttributeType.getReferenceType()) return true;

		// Referenzierungs-Typ �berpr�fen
		if(configurationObjectReference.getReferenceObjectType().equals("")) {
			if(referenceAttributeType.getReferencedObjectType() != null) return true;
		}
		else {
			if(getType(configurationObjectReference.getReferenceObjectType()) != referenceAttributeType.getReferencedObjectType()) return true;
		}

		// Pr�fe, ob undefinierte Objekte erlaubt sind.
		switch(configurationObjectReference.getUndefined()) {
			case ALLOWED:
				if(!referenceAttributeType.isUndefinedAllowed()) return true;
				break;
			case FORBIDDEN:
				if(referenceAttributeType.isUndefinedAllowed()) return true;
				break;
		}
		return false;
	}

	/*############## Attributgruppe ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property       eine Definition einer Attributgruppe einer Versorgungsdatei
	 * @param attributeGroup eine Attributgruppe (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeGroupProcessable(AttributeGroupProperties property, AttributeGroup attributeGroup) {
		// ATG ein Parameter
		if(property.isParameter() != attributeGroup.isParameter() && !isSetChangeable(attributeGroup, "AttributgruppenVerwendungen")) {
			_debug.finer("Parameter?");
			return false;
		}

		// Attribute und Attributlisten �berpr�fen
		if(!isAttributeSetProcessable(property.getAttributeAndAttributeList(), attributeGroup)) {
			_debug.finer("Attribute");
			return false;
		}

		// Attributgruppenverwendungen �berpr�fen
		if(!isAttributeGroupUsageSetProcessable(property, attributeGroup)) {
			_debug.finer("Attributgruppenverwendungen");
			return false;
		}

		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition von dem System-Objekt unterscheidet.
	 *
	 * @param property       eine Definition einer Attributgruppe einer Versorgungsdatei
	 * @param attributeGroup eine Attributgruppe (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition einer Attributgruppe von der Attributgruppe unterscheidet <br> <code>false</code>, sonst
	 */
	boolean isAttributeGroupDifferent(AttributeGroupProperties property, AttributeGroup attributeGroup) {
		// ATG ein Parameter?
		if(property.isParameter() != attributeGroup.isParameter()) {
			_debug.finer("Ist die Attributgruppe parametrierend? ", attributeGroup.getPid());
			return true;
		}

		// Attribute und Attributlisten �berpr�fen
		if(isAttributeSetDifferent(property.getAttributeAndAttributeList(), attributeGroup)) {
			_debug.finer("Attribute der Attributgruppe sind unterschiedlich", attributeGroup.getPid());
			return true;
		}

		// Attributgruppenverwendungen �berpr�fen
		if(isAttributeGroupUsageSetDifferent(property, attributeGroup)) {
			_debug.finer("Attributgruppenverwendungen sind unterschiedlich", attributeGroup.getPid());
			return true;
		}
		return false;
	}

	/**
	 * Ermittelt zu einer Attributgruppe und einem Aspekt die entsprechende Attributgruppenverwendung.
	 *
	 * @param atgUsages die Attributgruppenverwendungen einer Attributgruppe
	 * @param aspectPid die Pid eines Aspekts
	 *
	 * @return Die Attributgruppenverwendung, die zu der angegebenen Attributgruppe und zu dem angegebenen Aspekt passt.
	 */
	private AttributeGroupUsage getAttributeGroupUsage(List<SystemObject> atgUsages, String aspectPid) {
		for(SystemObject systemObject : atgUsages) {
			final AttributeGroupUsage atgUsage = (AttributeGroupUsage)systemObject;
			if(atgUsage.getAspect().getPid().equals(aspectPid)) return atgUsage;
		}
		return null;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property       eine Definition einer Attributgruppe einer Versorgungsdatei
	 * @param attributeGroup eine Attributgruppe (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Attributgruppenverwendungs-Menge mit der des System-Objekts �bereinstimmt, oder falls nicht, ob sich das System-Objekt
	 *         �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeGroupUsageSetProcessable(AttributeGroupProperties property, AttributeGroup attributeGroup) {
		// Die Menge der Attributgruppenverwendungen
		final NonMutableSet atgUsageSet = attributeGroup.getNonMutableSet("AttributgruppenVerwendungen");
		final List<SystemObject> atgUsages = atgUsageSet.getElementsInModifiableVersion();

		// Aspekte �berpr�fen
		int counter = 0;
		// Sind alle Aspekte vorhanden?
		for(ConfigurationAspect configurationAspect : property.getConfigurationAspect()) {
			final String pid = configurationAspect.getPid();
			if(property.isParameter() && (pid.equals("asp.parameterSoll") || pid.equals("asp.parameterIst") || pid.equals("asp.parameterVorgabe") || pid.equals(
					"asp.parameterDefault"
			))) {
				// diese Aspekte ignorieren - werden durch die Methode isParameter() gepr�ft
			}
			else {
				// passende Attributgruppenverwendung raussuchen
				final AttributeGroupUsage attributeGroupUsage = getAttributeGroupUsage(atgUsages, pid);
				if(attributeGroupUsage != null) {   // Pr�fen, ob es eine ATGV hierzu gibt
					if(isAttributeGroupUsageDifferent(configurationAspect, attributeGroupUsage) && !isAttributeGroupUsageProcessable(
							configurationAspect, attributeGroupUsage
					)) {
						_debug.finer("Attributgruppenverwendung kann nicht ge�ndert werden.");
						return false;
					}
					counter++;
				}
			}
		}
		if(property.isParameter()) {
			// die vier Parameter-Aspekte m�ssen vorhanden sein
			final AttributeGroupUsage atgUsageTarget = getAttributeGroupUsage(atgUsages, "asp.parameterSoll");
			final AttributeGroupUsage atgUsageActual = getAttributeGroupUsage(atgUsages, "asp.parameterIst");
			final AttributeGroupUsage atgUsageDemand = getAttributeGroupUsage(atgUsages, "asp.parameterVorgabe");
			final AttributeGroupUsage atgUsageDefault = getAttributeGroupUsage(atgUsages, "asp.parameterDefault");

			if(atgUsageTarget == null || atgUsageActual == null || atgUsageDemand == null || atgUsageDefault == null) {
				if(!isSetChangeable(attributeGroup, "AttributgruppenVerwendungen")) return false;
			}
			if(!isConfigurationDataChangeable("atg.attributgruppenVerwendung")) {
				if(atgUsageTarget != null && (!atgUsageTarget.isExplicitDefined() || !(
						atgUsageTarget.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
						|| atgUsageTarget.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain))) {
					return false;
				}
				if(atgUsageActual != null && (!atgUsageActual.isExplicitDefined() || !(
						atgUsageActual.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
						|| atgUsageActual.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain))) {
					return false;
				}
				if(atgUsageDemand != null && (!atgUsageDemand.isExplicitDefined() || !(
						atgUsageDemand.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain
						|| atgUsageDemand.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain))) {
					return false;
				}
				if(atgUsageDefault != null && (!atgUsageDefault.isExplicitDefined()
				                               || atgUsageDefault.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData)) {
					return false;
				}
			}
			counter += 4;
		}

		if(!isSetChangeable(attributeGroup, "AttributgruppenVerwendungen")) {
			// Anzahl vergleichen
			if(counter != atgUsages.size()) {
				return false;
			}
			// Mindestens eine Attributgruppenverwendung muss vorhanden sein (z.B. asp.eigenschaften).
			if(atgUsages.size() == 0) {
				return false;
			}
		}
		// Anzahl der Elemente pr�fen
		return true;
	}

	/**
	 * Pr�ft, ob die definierten Attributgruppenverwendungen mit der Menge der Attributgruppenverwendungen der Attributgruppe �bereinstimmen.
	 *
	 * @param property       eine Definition einer Attributgruppe einer Versorgungsdatei
	 * @param attributeGroup eine Attributgruppe (System-Objekt)
	 *
	 * @return <code>true</code>, falls die definierten Attributgruppenverwendungen mit der Menge der Attributgruppenverwendungen der Attributgruppe �bereinstimmen
	 *         <br> <code>false</code>, sonst
	 */
	boolean isAttributeGroupUsageSetDifferent(AttributeGroupProperties property, AttributeGroup attributeGroup) {
		// Die Menge der Attributgruppenverwendungen
		final NonMutableSet atgUsageSet = attributeGroup.getNonMutableSet("AttributgruppenVerwendungen");
		final List<SystemObject> atgUsages = atgUsageSet.getElementsInModifiableVersion();
		// Aspekte �berpr�fen
		int counter = 0;
		// Sind alle Aspekte vorhanden?
		for(ConfigurationAspect configurationAspect : property.getConfigurationAspect()) {
			final String pid = configurationAspect.getPid();
			if(property.isParameter() && (pid.equals("asp.parameterSoll") || pid.equals("asp.parameterIst") || pid.equals("asp.parameterVorgabe") || pid.equals(
					"asp.parameterDefault"
			))) {
				// diese Aspekte ignorieren - werden durch die Methode isParameter() gepr�ft
			}
			else {
				// passende Attributgruppenverwendung raussuchen
				final AttributeGroupUsage attributeGroupUsage = getAttributeGroupUsage(atgUsages, pid);
				if(attributeGroupUsage != null) {
					if(isAttributeGroupUsageDifferent(configurationAspect, attributeGroupUsage)) {
						_debug.finer("Attributgruppenverwendung ist unterschiedlich.");
						return true;
					}
					counter++;
				}
			}
		}
		if(property.isParameter()) {
			// die drei Parameter-Aspekte m�ssen vorhanden sein
			final AttributeGroupUsage atgUsageTarget = getAttributeGroupUsage(atgUsages, "asp.parameterSoll");
			final AttributeGroupUsage atgUsageActual = getAttributeGroupUsage(atgUsages, "asp.parameterIst");
			final AttributeGroupUsage atgUsageDemand = getAttributeGroupUsage(atgUsages, "asp.parameterVorgabe");
			final AttributeGroupUsage atgUsageDefault = getAttributeGroupUsage(atgUsages, "asp.parameterDefault");

			if(atgUsageTarget == null || atgUsageActual == null || atgUsageDemand == null || atgUsageDefault == null || (
					atgUsageTarget.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
					&& atgUsageTarget.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) || (
					atgUsageActual.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
					&& atgUsageActual.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) || (
					atgUsageDemand.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSenderDrain
					&& atgUsageDemand.getUsage() != AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) || (atgUsageDefault.getUsage()
			                                                                                                               != AttributeGroupUsage.Usage
					.ChangeableOptionalConfigurationData)) {
				_debug.finer("Parameter-Attributgruppenverwendungen sind unterschiedlich.");
				return true;
			}
			else {
				counter += 4;
			}
		}

		// Anzahl der Elemente pr�fen
		if(counter != atgUsages.size()) {
			_debug.finer("Die Anzahl der Attributgruppenverwendungen stimmt nicht �berein");
			return true;
		}

		// Mindestens eine Attributgruppenverwendung muss vorhanden sein (asp.eigenschaften oder irgendeine andere)
		if(atgUsages.size() == 0) return true;

		return false;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param configurationAspect Definition eines Aspekts f�r eine Attributgruppenverwendung
	 * @param attributeGroupUsage eine Attributgruppenverwendung (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isAttributeGroupUsageProcessable(ConfigurationAspect configurationAspect, AttributeGroupUsage attributeGroupUsage) {
		if(!configurationAspect.getPid().equals(attributeGroupUsage.getPid())) return false;
		if(!isInfoProcessable(configurationAspect.getInfo(), attributeGroupUsage.getInfo())) return false;
		if(configurationAspect.getUsage() != attributeGroupUsage.getUsage() && !isConfigurationDataChangeable("atg.attributgruppenVerwendung")) return false;
		return true;
	}

	/**
	 * Pr�ft, ob die Definition eines Aspekts zu einer Attributgruppenverwendung unterschiedlich ist.
	 *
	 * @param configurationAspect Definition eines Aspekts f�r eine Attributgruppenverwendung
	 * @param attributeGroupUsage eine Attributgruppenverwendung (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Attributgruppenverwendung und die Definition nicht �bereinstimmen <br> <code>false</code>, sonst
	 */
	boolean isAttributeGroupUsageDifferent(ConfigurationAspect configurationAspect, AttributeGroupUsage attributeGroupUsage) {
		if(attributeGroupUsage == null) {
			_debug.finer("ATGV == null");
			return true;
		}
		if(!configurationAspect.getPid().equals(attributeGroupUsage.getAspect().getPid())) {
			_debug.finer("Aspekt ist unterschiedlich");
			return true;
		}
		// Info �berpr�fen
		if(isInfoDifferent(configurationAspect.getInfo(), attributeGroupUsage.getInfo())) {
			_debug.finer("Info ist unterschiedlich");
			return true;
		}
		// die Verwendung �berpr�fen
		if(configurationAspect.getUsage() != attributeGroupUsage.getUsage()) {
			_debug.finer("Die Verwendung der ATGV ist unterschiedlich");
			return true;
		}
		return false;
	}

	/*############## ConfigurationArea ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property          eine Definition eines Konfigurationsbereichs einer Versorgungsdatei
	 * @param configurationArea ein Konfigurationsbereich (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isConfigurationAreaProcessable(ConfigurationAreaProperties property, ConfigurationArea configurationArea) {
		// den Konfigurationsverantwortlichen �berpr�fen
		if(isConfigurationAreaAuthorityProcessable(property.getAuthority(), configurationArea.getConfigurationAuthority())) {
			return false;
		}
		// Konfigurations�nderung �berpr�fen
		if(isConfigurationAreaChangeInformationProcessable(property.getConfigurationAreaChangeInformation(), configurationArea)) {
			return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob die Definition und das System-Objekt unterschiedlich sind.
	 *
	 * @param property          eine Definition eines Konfigurationsbereichs einer Versorgungsdatei
	 * @param configurationArea ein Konfigurationsbereich (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isConfigurationAreaDifferent(ConfigurationAreaProperties property, ConfigurationArea configurationArea) {
		// den Konfigurationsverantwortlichen �berpr�fen
		if(isConfigurationAreaAuthorityDifferent(property.getAuthority(), configurationArea.getConfigurationAuthority())) {
			return true;
		}
		// Konfigurations�nderung �berpr�fen
		if(isConfigurationAreaChangeInformationDifferent(property.getConfigurationAreaChangeInformation(), configurationArea)) {
			return true;
		}
		return false;
	}

	/**
	 * Pr�ft, ob die Pid zum Konfigurationsverantwortlichen passt und wenn nicht, ob sich der Konfigurationsverantwortliche am Konfigurationsbereich �ndern l�sst.
	 *
	 * @param authority              die Pid des Konfigurationsverantwortlichen aus den Versorgungsdateien
	 * @param configurationAuthority der Konfigurationsverantwortliche des Konfigurationsbereichs
	 *
	 * @return <code>true</code>, falls die Pid zum Konfigurationsverantwortlichen passt und wenn nicht, ob sich der Konfigurationsverantwortliche am
	 *         Konfigurationsbereich �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isConfigurationAreaAuthorityProcessable(String authority, ConfigurationAuthority configurationAuthority) {
		if(isConfigurationAreaAuthorityDifferent(authority, configurationAuthority)
		   && !isConfigurationDataChangeable("atg.konfigurationsBereichEigenschaften")) {
			return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob die Pid zum Konfigurationsverantwortlichen passt.
	 *
	 * @param authority              die Pid des Konfigurationsverantwortlichen aus den Versorgungsdateien
	 * @param configurationAuthority der Konfigurationsverantwortliche des Konfigurationsbereichs
	 *
	 * @return <code>true</code>, falls die Pid nicht zum Konfigurationsverantwortlichen passt, sonst <code>false</code>
	 */
	boolean isConfigurationAreaAuthorityDifferent(String authority, ConfigurationAuthority configurationAuthority) {
		if(!authority.equals(configurationAuthority.getPid())) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Pr�ft, ob die Konfigurations�nderungen aus der Versorgungsdatei mit den �nderungen des Konfigurationsbereichs �bereinstimmen und wenn nicht, ob sich der
	 * Datensatz, der die �nderungen speichert, �ndern l�sst.
	 *
	 * @param configurationAreaChangeInformation
	 *                          die Konfigurations�nderungen aus der Versorgungsdatei
	 * @param configurationArea Konfigurationsbereich
	 *
	 * @return <code>true</code>, falls die Konfigurations�nderungen aus der Versorgungsdatei mit den �nderungen des Konfigurationsbereichs �bereinstimmen und wenn
	 *         nicht, ob sich der Datensatz, der die �nderungen speichert, �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isConfigurationAreaChangeInformationProcessable(
			ConfigurationAreaChangeInformation[] configurationAreaChangeInformation, ConfigurationArea configurationArea
	) {
		if(isConfigurationAreaChangeInformationDifferent(configurationAreaChangeInformation, configurationArea) && !isConfigurationDataChangeable(
				"atg.konfigurations�nderungen"
		)) {
			return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob die Konfigurations�nderungen aus der Versorgungsdatei mit den �nderungen des Konfigurationsbereichs �bereinstimmen.
	 *
	 * @param configurationAreaChangeInformation
	 *                          die Konfigurations�nderungen aus der Versorgungsdatei
	 * @param configurationArea Konfigurationsbereich
	 *
	 * @return <code>true</code>, falls die Konfigurations�nderungen aus der Versorgungsdatei mit den �nderungen des Konfigurationsbereichs nicht �bereinstimmen
	 *         <br> <code>false</code>, sonst
	 */
	boolean isConfigurationAreaChangeInformationDifferent(
			ConfigurationAreaChangeInformation[] configurationAreaChangeInformation, ConfigurationArea configurationArea
	) {
		Data data = _configurationImport.getConfigurationData(configurationArea, getAttributeGroup("atg.konfigurations�nderungen"));
//		Data data = configurationArea.getConfigurationData(getAttributeGroup("atg.konfigurations�nderungen"));
		if(data == null) {
			// diesen Datensatz gibt es noch nicht
			if(configurationAreaChangeInformation.length > 0) {
				// es sollen aber Daten enthalten sein
				return true;
			}
		}
		else {
			// den Datensatz gibt es, also -> Daten vergleichen
			Data.Array array = data.getArray("Konfigurations�nderung");
			if(array.getLength() != configurationAreaChangeInformation.length) return true;
			int i = 0;
			final short lastModifiedVersion = ((ConfigConfigurationArea)configurationArea).getLastModifiedVersion();
			for(ConfigurationAreaChangeInformation information : configurationAreaChangeInformation) {
				Data item = array.getItem(i++);
				// Vergleich: Bestehende Daten <-> zu speichernde Daten
				if(item.getTimeValue("Stand").getMillis() != information.getCondition()) return true;
				if(!item.getTextValue("Autor").getText().equals(information.getAuthor())) return true;
				if(!item.getTextValue("Grund").getText().equals(information.getReason())) return true;
				if(!item.getTextValue("Text").getText().equals(information.getText())) return true;
				if(item.getUnscaledValue("Version").intValue() != information.getVersion()) return true;
				// Beide Versionen sind gleich, aber wenn sie zu gro� ist, dann muss das korrigiert werden
				if(information.getVersion() > lastModifiedVersion) return true;
			}
		}
		return false;
	}

	/*############## ObjectSetType ############## */

	/**
	 * Gibt zur�ck, ob die Mengen-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property      eine Mengendefinition einer Versorgungsdatei
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isObjectSetTypeProcessable(ObjectSetTypeProperties property, ObjectSetType objectSetType) {
		if(!isObjectSetTypePropertiesProcessable(property, objectSetType)) return false;
		if(!isObjectSetTypeObjectTypesProcessable(property.getElements(), objectSetType)) return false;
		if(!isObjectSetTypeSuperTypesProcessable(objectSetType)) return false;
		return true;
	}

	/**
	 * Pr�ft, ob die Mengen-Definition und der Mengen-Typ unterschiedlich sind.
	 *
	 * @param property      eine Mengendefinition einer Versorgungsdatei
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, wenn die Mengen-Definition und der Mengen-Typ unterschiedlich sind, sonst <code>false</code>
	 */
	boolean isObjectSetTypeDifferent(ObjectSetTypeProperties property, ObjectSetType objectSetType) {
		// Eigenschaften �berpr�fen
		if(isObjectSetTypePropertiesDifferent(property, objectSetType)) return true;

		// Objekt-Typen �berpr�fen
		if(isObjectSetTypeObjectTypesDifferent(property.getElements(), objectSetType)) return true;
		if(isObjectSetTypeSuperTypesDifferent(objectSetType)) return true;
		return false;
	}

	boolean isObjectSetTypeSuperTypesProcessable(ObjectSetType objectSetType) {
		if(isObjectSetTypeSuperTypesDifferent(objectSetType) && !isSetChangeable(objectSetType, "SuperTypen")) {
			return false;
		}
		else {
			return true;
		}
	}

	boolean isObjectSetTypeSuperTypesDifferent(ObjectSetType objectSetType) {
		final NonMutableSet nonMutableSet = objectSetType.getNonMutableSet("SuperTypen");
		if(nonMutableSet == null) return true;
		final List<SystemObject> superTypes = nonMutableSet.getElementsInModifiableVersion();
		// es darf nur einen SuperTyp geben.
		if(superTypes.size() > 1) return true;

		if(objectSetType.isMutable()) {
			if(!superTypes.contains(getType(Pid.Type.MUTABLE_SET))) return true;
		}
		else {
			if(!superTypes.contains(getType(Pid.Type.NON_MUTABLE_SET))) return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Eigenschaften der Mengen-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property      eine Mengendefinition einer Versorgungsdatei
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Eigenschaften der Definition mit den Eigenschaften des System-Objekts �bereinstimmen, oder falls nicht, ob sich das
	 *         System-Objekt �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isObjectSetTypePropertiesProcessable(ObjectSetTypeProperties property, ObjectSetType objectSetType) {
		if(isObjectSetTypePropertiesDifferent(property, objectSetType) && !isConfigurationDataChangeable("atg.mengenTypEigenschaften")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die Eigenschaften der Mengen-Definition und die Eigenschaften des Mengen-Typs unterschiedlich sind.
	 *
	 * @param property      eine Mengendefinition einer Versorgungsdatei
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Eigenschaften der Mengen-Definition und die Eigenschaften des Mengen-Typs unterschiedlich sind<br> <code>false</code>,
	 *         sonst
	 */
	boolean isObjectSetTypePropertiesDifferent(ObjectSetTypeProperties property, ObjectSetType objectSetType) {
		if(_configurationImport.getConfigurationData(objectSetType, getAttributeGroup("atg.mengenTypEigenschaften")) == null) return true;
//		if(objectSetType.getConfigurationData(getAttributeGroup("atg.mengenTypEigenschaften")) == null) return true;
		if(property.getMinimum() != objectSetType.getMinimumElementCount()) return true;
		if(property.getMaximum() != objectSetType.getMaximumElementCount()) return true;
		if(property.getMutable() != objectSetType.isMutable()) return true;
		if(property.getReferenceType() != objectSetType.getReferenceType()) return true;
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param elements      Definition der Menge der erlaubten Objekt-Typen
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isObjectSetTypeObjectTypesProcessable(String[] elements, ObjectSetType objectSetType) {
		if(isObjectSetTypeObjectTypesDifferent(elements, objectSetType) && !isSetChangeable(objectSetType, "ObjektTypen")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die Definition der Menge der erlaubten Objekt-Typen mit der Menge des Mengen-Typs unterschiedlich ist.
	 *
	 * @param elements      Definition der Menge der erlaubten Objekt-Typen
	 * @param objectSetType ein Mengen-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition der Menge der erlaubten Objekt-Typen mit der Menge des Mengen-Typs unterschiedlich ist <br>
	 *         <code>false</code>, sonst
	 */
	boolean isObjectSetTypeObjectTypesDifferent(String[] elements, ObjectSetType objectSetType) {
		final NonMutableSet objectTypes = objectSetType.getNonMutableSet("ObjektTypen");
		if(objectTypes == null) return true;
		final List<SystemObject> elementsInVersion = objectTypes.getElementsInVersion(objectSetType.getConfigurationArea().getModifiableVersion());
		// Stimmt die Anzahl
		if(elements.length != elementsInVersion.size()) return true;
		// Stimmen die Elemente �berein
		for(String pid : elements) {
			final SystemObjectType objectType = getType(pid);
			if(objectType == null || !elementsInVersion.contains(objectType)) return true;
		}
		return false;
	}

	/*############## SystemObjectType ############## */

	/**
	 * Gibt zur�ck, ob die Import-Definition mit dem System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property         eine Definition eines Objekt-Typen
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition und das System-Objekt �bereinstimmen, oder falls nicht, ob sich das System-Objekt �ndern
	 *         l�sst<br><code>false</code>, sonst
	 */
	boolean isSystemObjectTypeProcessable(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		// Eigenschaften �berpr�fen
		if(!isSystemObjectTypePropertiesProcessable(property, systemObjectType)) {
			_debug.finer("Objekt-Typ-Eigenschaften");
			return false;
		}
		// SuperTypen �berpr�fen
		if(!isSystemObjectTypeSuperTypesProcessable(property.getExtendedPids(), systemObjectType)) {
			_debug.finer("SuperTypen");
			return false;
		}
		// Bei dynamischen Objekt-Typen den Persistenz-Modus �berpr�fen.
		boolean isConfigurating = property.getConfiguring();
		if(property.getExtendedPids().length > 0) {
			// Anhand der SuperTypen ermitteln, ob es ein dynamischer oder konfigurierender Typ ist.
			final List<SystemObjectType> types = createSuperTypes(property.getExtendedPids());
			if(types.size() > 0) isConfigurating = types.get(0).isConfigurating();
		}
		if(!isConfigurating && !systemObjectType.isConfigurating()) {
			// soll dynamisch sein
			DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObjectType;
			if(!isDynamicTypePersistenceModeProcessable(property, dynamicObjectType.getPersistenceMode())) {
				_debug.finer("Dynamisches Objekt - Persistenz-Modus");
				return false;
			}
		}
		else {
			// Mengenverwendungen �berpr�fen
			if(!isSystemObjectTypeObjectSetUsesProcessable(property, systemObjectType)) {
				_debug.finer("Mengenverwendungen");
				return false;
			}
		}

		// Attributgruppen �berpr�fen
		if(!isSystemObjectTypeAttributeGroupProcessable(property, systemObjectType)) {
			_debug.finer("Attributgruppen");
			return false;
		}

		// Default-Parameter-Datens�tze pr�fen
		if(!isDefaultParameterProcessable(property.getDefaultParameters(), systemObjectType)) {
			_debug.finer("Default-Parameter-Datensatz");
			return false;
		}

		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition und das System-Objekt unterscheiden.
	 *
	 * @param property         eine Definition eines Objekt-Typen
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition und das System-Objekt unterscheiden, sonst <code>false</code>
	 */
	boolean isSystemObjectTypeDifferent(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		// Eigenschaften �berpr�fen
		if(isSystemObjectTypePropertiesDifferent(property, systemObjectType)) {
			_debug.finer("Die Eigenschaften des Objekt-Typen sind unterschiedlich", property.getPid());
			return true;
		}
		// SuperTypen �berpr�fen
		if(isSystemObjectTypeSuperTypesDifferent(property.getExtendedPids(), systemObjectType)) {
			_debug.finer("Die Super-Typen unterscheiden sich bei", property.getPid());
			return true;
		}
		// Bei dynamischen Objekt-Typen den Persistenz-Modus �berpr�fen.
		boolean isConfigurating = property.getConfiguring();
		if(property.getExtendedPids().length > 0) {
			// Anhand der SuperTypen ermitteln, ob es ein dynamischer oder konfigurierender Typ ist.
			final List<SystemObjectType> types = createSuperTypes(property.getExtendedPids());
			if(types.size() > 0) isConfigurating = types.get(0).isConfigurating();
		}
		if(!isConfigurating && !systemObjectType.isConfigurating()) {
			// soll dynamisch sein
			DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObjectType;
			if(isDynamicTypePersistenceModeDifferent(property, dynamicObjectType.getPersistenceMode())) {
				_debug.finer("Der Persistenz-Modus unterscheidet sich bei", property.getPid());
				return true;
			}
		}
		else {
			// Mengenverwendunge �berpr�fen
			if(isSystemObjectTypeObjectSetUsesDifferent(property, systemObjectType)) {
				_debug.finer("Die Mengenverwendungen unterscheiden sich bei", property.getPid());
				return true;
			}
		}

		// Attributgruppen �berpr�fen
		if(isSystemObjectTypeAttributeGroupDifferent(property, systemObjectType)) {
			_debug.finer("Die Menge der Attributgruppen unterscheiden sich am Objekt-Typ", property.getPid());
			return true;
		}

		// Default-Parameter-Datensatz
		if(isDefaultParameterDifferent(property.getDefaultParameters(), systemObjectType)) {
			_debug.finer("Die Default-Parameter-Datens�tze unterscheiden sich am Objekt-Typ", property.getPid());
			return true;
		}

		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition eines Objekt-Typen mit den Eigenschaften eines System-Objekts �bereinstimmt, oder falls nicht, ob das System-Objekt sich
	 * ver�ndern l�sst.
	 *
	 * @param property         eine Definition eines Objekt-Typen
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition der Eigenschaften eines Objekt-Typs und die Eigenschaften des System-Objekts �bereinstimmen, oder falls
	 *         nicht, ob sich das System-Objekt �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isSystemObjectTypePropertiesProcessable(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		if(isSystemObjectTypePropertiesDifferent(property, systemObjectType) && !isConfigurationDataChangeable("atg.typEigenschaften")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition der Eigenschaften eines Objekt-Typs mit den Eigenschaften eines Objekt-Typs unterscheiden.
	 *
	 * @param property         eine Definition eines Objekt-Typen
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition der Eigenschaften eines Objekt-Typs mit den Eigenschaften eines Objekt-Typs unterscheiden <br>
	 *         <code>false</code>, sonst
	 */
	boolean isSystemObjectTypePropertiesDifferent(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		// Objekt-Typ konfigurierend?
		boolean isConfigurating = property.getConfiguring();
		if(property.getExtendedPids().length > 0) {
			// Anhand der SuperTypen ermitteln, ob es ein dynamischer oder konfigurierender Typ ist.
			final List<SystemObjectType> types = createSuperTypes(property.getExtendedPids());
			if(types.size() > 0) isConfigurating = types.get(0).isConfigurating();
		}
		if(isConfigurating != systemObjectType.isConfigurating()) {
			_debug.finer("Konfigurierend");
			return true;
		}
		// speichert, ob die Namen von Objekten dieses Typs permanent sind
		if(property.getObjectNamesPermanent() != systemObjectType.isNameOfObjectsPermanent()) {
			_debug.finer(
					"Name von " + systemObjectType.getPidOrNameOrId() + " �nderbar? (alt|neu): (" + systemObjectType.isNameOfObjectsPermanent() + "|"
					+ property.getObjectNamesPermanent() + ")"
			);
			return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition der Super-Typen mit den Super-Typen des System-Objekts �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern
	 * l�sst.
	 *
	 * @param extendedPids     Definition der Super-Typen eines Objekt-Typs
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition der Super-Typen mit den Super-Typen des System-Objekts �bereinstimmt, oder falls nicht, ob sich das
	 *         System-Objekt �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isSystemObjectTypeSuperTypesProcessable(String[] extendedPids, SystemObjectType systemObjectType) {
		if(isSystemObjectTypeSuperTypesDifferent(extendedPids, systemObjectType) && !isSetChangeable(systemObjectType, "SuperTypen")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition der Super-Typen von den Super-Typen des System-Objekts unterscheidet.
	 *
	 * @param extendedPids     Definition der Super-Typen eines Objekt-Typs
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition der Super-Typen von den Super-Typen des System-Objekts unterscheidet, sonst <code>false</code>
	 */
	boolean isSystemObjectTypeSuperTypesDifferent(String[] extendedPids, SystemObjectType systemObjectType) {
		// Liste der erweiterten Typen ermitteln
		final Set<SystemObjectType> superTypes = new HashSet<SystemObjectType>();

		// Ist es ein Basis-Typ, wenn es einer sein soll?
		if(extendedPids.length == 0 && !systemObjectType.isBaseType()) {
			return true;
		}
		else {
			// Typ soll kein Basis-Typ sein -> alle Super-Typen ermitteln
			superTypes.addAll(createSuperTypes(extendedPids));
		}

		// Menge der Super-Typen �berpr�fen
		final NonMutableSet superTypeSet = systemObjectType.getNonMutableSet("SuperTypen");
		final List<SystemObject> elementsInVersion = superTypeSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());
		// Anzahl miteinander vergleichen
		if(superTypes.size() != elementsInVersion.size()) {
			_debug.finer("Die Anzahl der Super-Typen passt nicht bei", systemObjectType.getPid());
			return true;
		}
		// Elemente miteinander vergleichen
		for(SystemObjectType objectType : superTypes) {
			if(!elementsInVersion.contains((SystemObject)objectType)) {
				_debug.finer("Folgender Super-Typ fehlt", objectType);
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die beiden Persistenzmodi gleich sind, oder falls nicht, ob der Datensatz, welcher die Eigenschaft speichert, ver�ndert werden darf.
	 *
	 * @param property        eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param persistenceMode zu vergleichender Persistenzmodus
	 *
	 * @return <code>true</code>, falls die beiden Persistenzmodi gleich sind, oder falls nicht, ob der Datensatz, welcher die Eigenschaft speichert, ver�ndert
	 *         werden darf <br> <code>false</code>, sonst
	 */
	boolean isDynamicTypePersistenceModeProcessable(SystemObjectTypeProperties property, DynamicObjectType.PersistenceMode persistenceMode) {
		if(isDynamicTypePersistenceModeDifferent(property, persistenceMode) && !isConfigurationDataChangeable("atg.dynamischerTypEigenschaften")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die beiden Persistenzmodi unterscheiden.
	 *
	 * @param property        eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param persistenceMode zu vergleichender Persistenzmodus
	 *
	 * @return <code>true</code>, falls sich die beiden Persistenzmodi unterscheiden <br> <code>false</code>, sonst
	 */
	boolean isDynamicTypePersistenceModeDifferent(SystemObjectTypeProperties property, DynamicObjectType.PersistenceMode persistenceMode) {
		// Liste der erweiterten Typen (SuperTypen)
		final List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
		superTypes.addAll(createSuperTypes(property.getExtendedPids()));

		switch(property.getPersistenceMode()) {
			case TRANSIENT_OBJECTS:
				if(persistenceMode != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) return true;
				break;
			case PERSISTENT_OBJECTS:
				if(persistenceMode != DynamicObjectType.PersistenceMode.PERSISTENT_OBJECTS) return true;
				break;
			case PERSISTENT_AND_INVALID_ON_RESTART:
				if(persistenceMode != DynamicObjectType.PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART) return true;
				break;
			default: // es wurde kein PersistenceMode angegeben
				if(_configurationImport.getSuperTypePersistenceMode(superTypes) != persistenceMode) return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition mit der Menge der Attributgruppen eines Objekt-Typen �bereinstimmt, oder falls nicht, ob der Objekt-Typ sich ver�ndern
	 * l�sst.
	 *
	 * @param property         eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition mit der Menge der Attributgruppen eines Objekt-Typen �bereinstimmt, oder falls nicht, ob der Objekt-Typ sich
	 *         ver�ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectTypeAttributeGroupProcessable(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		if(isSystemObjectTypeAttributeGroupDifferent(property, systemObjectType) && !isSetChangeable(systemObjectType, "Attributgruppen")) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Definition mit der Menge der Attributgruppen eines Objekt-Typen unterscheiden.
	 *
	 * @param property         eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition mit der Menge der Attributgruppen eines Objekt-Typen unterscheiden <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectTypeAttributeGroupDifferent(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		final NonMutableSet nonMutableSet = systemObjectType.getNonMutableSet("Attributgruppen");
		final List<SystemObject> directAttributeGroups = nonMutableSet.getElementsInModifiableVersion();

		// Attributgruppen ermitteln
		final List<AttributeGroup> attributeGroups = new LinkedList<AttributeGroup>();
		for(Object object : property.getAtgAndSet()) {
			if(object instanceof String) {
				String atgPid = (String)object;
				attributeGroups.add(getAttributeGroup(atgPid));
			}
		}

		// Anzahl der Elemente �berpr�fen
		if(attributeGroups.size() != directAttributeGroups.size()) return true;

		// Elemente �berpr�fen
		for(AttributeGroup attributeGroup : attributeGroups) {
			if(!directAttributeGroups.contains(attributeGroup)) return true;
		}

		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition mit den Mengenverwendungen eines Objekt-Typen �bereinstimmen, oder falls nicht, ob das System-Objekt sich ver�ndern l�sst.
	 *
	 * @param property         eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition mit den Mengenverwendungen eines Objekt-Typen �bereinstimmen, oder falls nicht, ob sich das System-Objekt
	 *         �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectTypeObjectSetUsesProcessable(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		boolean setChangeable = isSetChangeable(systemObjectType, "Mengen");
		final NonMutableSet nonMutableSet = systemObjectType.getNonMutableSet("Mengen");
		final List<SystemObject> directObjectSetUses = nonMutableSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());

		// Mengenverwendungen ermitteln
		final List<ConfigurationSet> objectSetUses = new ArrayList<ConfigurationSet>();
		for(Object object : property.getAtgAndSet()) {
			if(object instanceof ConfigurationSet) {
				objectSetUses.add((ConfigurationSet)object);
			}
		}

		// Anzahl �berpr�fen
		if(objectSetUses.size() != directObjectSetUses.size() && !setChangeable) return false;

		// Elemente �berpr�fen
		for(ConfigurationSet configurationSet : objectSetUses) {
			final String objectSetName = configurationSet.getObjectSetName();
			ObjectSetUse checkObjectSetUse = null;
			for(SystemObject systemObject : directObjectSetUses) {
				ObjectSetUse objectSetUse = (ObjectSetUse)systemObject;
				if(objectSetName.equals(objectSetUse.getObjectSetName())) {
					checkObjectSetUse = objectSetUse;
					break;
				}
			}
			if(checkObjectSetUse == null) {
				if(setChangeable) {
					continue;
				}
				else {
					return false;
				}
			}
			else if(!isObjectSetUseProcessable(configurationSet, checkObjectSetUse)) return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition und die Mengenverwendungen eines Objekt-Typen unterscheiden.
	 *
	 * @param property         eine Definition eines Objekt-Typen einer Versorgungsdatei
	 * @param systemObjectType ein Objekt-Typ (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition und die Mengenverwendungen eines Objekt-Typen unterscheiden, sonst <code>false</code>
	 */
	boolean isSystemObjectTypeObjectSetUsesDifferent(SystemObjectTypeProperties property, SystemObjectType systemObjectType) {
		final NonMutableSet nonMutableSet = systemObjectType.getNonMutableSet("Mengen");
		final List<SystemObject> directObjectSetUses = nonMutableSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());

		// Mengenverwendungen ermitteln
		final List<ConfigurationSet> objectSetUses = new ArrayList<ConfigurationSet>();
		for(Object object : property.getAtgAndSet()) {
			if(object instanceof ConfigurationSet) {
				objectSetUses.add((ConfigurationSet)object);
			}
		}

		// Anzahl �berpr�fen
		if(objectSetUses.size() != directObjectSetUses.size()) return true;

		// Elemente �berpr�fen
		for(ConfigurationSet configurationSet : objectSetUses) {
			final String objectSetName = configurationSet.getObjectSetName();
			ObjectSetUse checkObjectSetUse = null;
			for(SystemObject systemObject : directObjectSetUses) {
				ObjectSetUse objectSetUse = (ObjectSetUse)systemObject;
				if(objectSetName.equals(objectSetUse.getObjectSetName())) {
					checkObjectSetUse = objectSetUse;
					break;
				}
			}
			if(isObjectSetUseDifferent(configurationSet, checkObjectSetUse)) return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition einer Mengenverwendung mit dem System-Objekt einer Mengenverwendung �bereinstimmt, oder falls nicht, ob das System-Objekt
	 * sich ver�ndern l�sst.
	 *
	 * @param configurationSet eine Definition einer Mengenverwendung
	 * @param objectSetUse     eine Mengenverwendung (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Definition einer Mengenverwendung mit dem System-Objekt einer Mengenverwendung �bereinstimmt, oder falls nicht, ob sich
	 *         das System-Objekt �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isObjectSetUseProcessable(ConfigurationSet configurationSet, ObjectSetUse objectSetUse) {
		// Info �berpr�fen
		if(!isInfoProcessable(configurationSet.getInfo(), objectSetUse.getInfo())) return false;
		// Eigenschaften �berpr�fen
		if(!isConfigurationDataChangeable("atg.mengenVerwendungsEigenschaften")) {
			if(configurationSet.getRequired() != objectSetUse.isRequired()) return false;
			if(getObjectSetType(configurationSet.getSetTypePid()) != objectSetUse.getObjectSetType()) return false;
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition einer Mengenverwendung und die Mengenverwendung eines System-Objekts unterscheiden.
	 *
	 * @param configurationSet eine Definition einer Mengenverwendung
	 * @param objectSetUse     eine Mengenverwendung (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Definition einer Mengenverwendung und die Mengenverwendung eines System-Objekts unterscheiden <br>
	 *         <code>false</code>, sonst
	 */
	boolean isObjectSetUseDifferent(ConfigurationSet configurationSet, ObjectSetUse objectSetUse) {
		// diese Verwendung gibt es nicht
		if(objectSetUse == null) {
			return true;
		}
		else {
			// Info �berpr�fen
			if(isInfoDifferent(configurationSet.getInfo(), objectSetUse.getInfo())) return true;
			// Eigenschaften �berpr�fen
			if(configurationSet.getRequired() != objectSetUse.isRequired()) return true;
			if(getObjectSetType(configurationSet.getSetTypePid()) != objectSetUse.getObjectSetType()) return true;
		}
		return false;
	}

	/*############## SystemObject ############## */

	/**
	 * Gibt zur�ck, ob die Definition eines Objekts mit dem entsprechenden System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern
	 * l�sst.
	 *
	 * @param property     eine Definition eines Objekts einer Versorgungsdatei
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls die Definition eines Objekts mit dem angegebenen System-Objekt �bereinstimmt, oder falls nicht, ob sich das System-Objekt
	 *         �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectProcessable(SystemObjectProperties property, SystemObject systemObject) {
		final SystemObjectType propertyType = getType(property.getType());
		// Typ vergleichen
		if(propertyType != systemObject.getType()) {
			return false;
		}

		final ConfigurationConfigurationObject configProperty = (ConfigurationConfigurationObject)property;
		final List<ConfigurationDataset> datasets = new LinkedList<ConfigurationDataset>();
		final List<ConfigurationObjectSet> objectSets = new LinkedList<ConfigurationObjectSet>();
		for(ConfigurationObjectElements elements : configProperty.getDatasetAndObjectSet()) {
			if(elements instanceof ConfigurationDataset) {
				datasets.add((ConfigurationDataset)elements);
			}
			else if(elements instanceof ConfigurationObjectSet) {
				objectSets.add((ConfigurationObjectSet)elements);
			}
		}

		// Datens�tze pr�fen
		if(!isDatasetsProcessable(datasets, systemObject)) {
			return false;
		}

		// wenn es sich um ein konfigurierendes Objekt handelt ...
		if(propertyType.isConfigurating()) {
			// Mengen pr�fen
			if(!isSystemObjectSetsProcessable(objectSets, (ConfigurationObject)systemObject)) {
				return false;
			}

			// Default-Parameter-Datensatz pr�fen
			if(!isDefaultParameterProcessable(configProperty.getDefaultParameters(), systemObject)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Definition eines Objekts von einem System-Objekt unterscheidet.
	 *
	 * @param property     eine Definition eines Objekts einer Versorgungsdatei
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls sich die Definition eines Objekts von einem System-Objekt unterscheidet, sonst <code>false</code>
	 */
	boolean isSystemObjectDifferent(SystemObjectProperties property, SystemObject systemObject) {
		final SystemObjectType propertyType = getType(property.getType());

		// Typ vergleichen
		if(propertyType != systemObject.getType()) {
			_debug.finer("Der Typ des konkreten Objekts " + systemObject.getPidOrNameOrId() + " passt nicht", systemObject.getType().getPidOrNameOrId());
			return true;
		}

		final ConfigurationConfigurationObject configProperty = (ConfigurationConfigurationObject)property;

		final List<ConfigurationDataset> datasets = new LinkedList<ConfigurationDataset>();
		final List<ConfigurationObjectSet> objectSets = new LinkedList<ConfigurationObjectSet>();
		for(ConfigurationObjectElements elements : configProperty.getDatasetAndObjectSet()) {
			if(elements instanceof ConfigurationDataset) {
				datasets.add((ConfigurationDataset)elements);
			}
			else if(elements instanceof ConfigurationObjectSet) {
				objectSets.add((ConfigurationObjectSet)elements);
			}
		}
		// Datens�tze pr�fen
		if(isDatasetsDifferent(datasets, systemObject)) {
			_debug.finer("Die Datens�tze am Objekt sind unterschiedlich", systemObject.getPidOrNameOrId());
			return true;
		}

		// wenn es sich um ein konfigurierendes Objekt handelt ...
		if(propertyType.isConfigurating()) {
			// Mengen pr�fen
			if(isSystemObjectSetsDifferent(objectSets, (ConfigurationObject)systemObject)) {
				_debug.finer("Die Mengen am Objekt sind unterschiedlich", systemObject.getPidOrNameOrId());
				return true;
			}

			// Default-Parameter-Datens�tze
			if(isDefaultParameterDifferent(configProperty.getDefaultParameters(), systemObject)) {
				_debug.finer("Die Default-Parameter-Datens�tze sind unterschiedlich", systemObject.getPidOrNameOrId());
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Definition von konfigurierenden Datens�tzen mit den Datens�tzen eines System-Objekts �bereinstimmt, oder falls nicht, ob das
	 * System-Objekt sich ver�ndern l�sst.
	 *
	 * @param datasets     eine Definition von konfigurierenden Datens�tzen einer Versorgungsdatei
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls die Definition von konfigurierenden Datens�tzen mit den Datens�tzen eines System-Objekts �bereinstimmt, oder falls nicht,
	 *         ob sich das System-Objekt �ndern l�sst<br><code>false</code>, sonst
	 */
	boolean isDatasetsProcessable(List<ConfigurationDataset> datasets, SystemObject systemObject) {
		// Hinzuf�gen von Datens�tzen ist kein Problem
		if(systemObject.getType().isConfigurating()) {
			// Wegnehmen von Datens�tzen k�nnte zum Problem f�hren, diese m�ssten gepr�ft werden, ob sie ge�ndert werden d�rfen
			// pr�fen, ob am Objekt Datens�tze stehen, die nicht daran gespeichert werden d�rfen
			final Collection<AttributeGroupUsage> futureAtgUsages = new ArrayList<AttributeGroupUsage>();
			final Collection<AttributeGroupUsage> usedAtgUsages = systemObject.getUsedAttributeGroupUsages();
			// Datens�tze �berp�fen
			for(ConfigurationDataset dataset : datasets) {
				final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
				final Aspect asp = getAspect(dataset.getPidAspect());

				// Attributgruppenverwendung ermitteln
				final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
				if(atgUsage != null) futureAtgUsages.add(atgUsage);

				// ein Datensatz muss �berpr�ft werden
				if(!isDatasetProcessable(dataset, systemObject)) {
					return false;
				}
			}

			for(AttributeGroupUsage atgUsage : usedAtgUsages) {
				if(!futureAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
				                                            || atgUsage.getAttributeGroup().getPid().equals(
						"atg.konfigurationsVerantwortlicherLaufendeNummer"
				))) {

					// ist der Datensatz �nderbar?
					if(!isConfigurationDataChangeable(atgUsage)) return false;
				}
			}
		}
		else {
			final Collection<AttributeGroupUsage> futureAtgUsages = new ArrayList<AttributeGroupUsage>();
			// bei dynamischen Objekten, f�hren notwendige DS zur Neuerstellung des Objekts
			// notwendige und �nderbare DS d�rfen nicht gel�scht werden
			for(ConfigurationDataset dataset : datasets) {
				final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
				final Aspect asp = getAspect(dataset.getPidAspect());
				if(atg == null || asp == null) {
					throw new IllegalStateException(
							"Attributgruppe '" + dataset.getPidATG() + "' oder Aspekt '" + dataset.getPidAspect() + "' ist kein aktuelles Objekt."
					);
				}
				// ATGV ermitteln
//				System.out.println("atg.getValidSince() = " + atg.getValidSince());
//				System.out.println("atg.getNotValidSince() = " + atg.getNotValidSince());
				System.out.println("atg = " + atg);
				System.out.println("asp = " + asp);
				final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
//				System.out.println("atgUsage.getValidSince() = " + atgUsage.getValidSince());
//				System.out.println("atgUsage.getNotValidSince() = " + atgUsage.getNotValidSince());
				if(atgUsage == null) {
					throw new IllegalStateException(
							"Zur ATG '" + dataset.getPidATG() + "' und Aspekt '" + dataset.getPidAspect() + "' gibt es keine Attributgruppenverwendung."
					);
				}
				else {
					futureAtgUsages.add(atgUsage);
				}

				// Datensatz �berpr�fen
				if(isDataDifferent(dataset.getDataAnddataListAndDataField(), _configurationImport.getConfigurationData(systemObject, atgUsage))) {
					// Datensatz ist unterschiedlich
					if(!isConfigurationDataChangeable(atgUsage)) {
						// Datensatz ist nicht �nderbar
						return false;
					}
					else if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
							&& dataset.getDataAnddataListAndDataField().length == 0) {
						// Datensatz ist zwar �nderbar, allerdings soll er gel�scht werden
						return false;
					}
				}
			}

			// nicht mehr verwendete DS - d�rfen sie gel�scht werden?
			for(AttributeGroupUsage atgUsage : systemObject.getUsedAttributeGroupUsages()) {
				if(!futureAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
				                                            || atgUsage.getAttributeGroup().getPid().equals(
						"atg.konfigurationsVerantwortlicherLaufendeNummer"
				))) {
					if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
					   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
						throw new IllegalStateException("Am Objekt " + systemObject + " darf der notwendige Datensatz " + atgUsage.getAttributeGroup().getPid() + ":" + atgUsage.getAspect().getPid() + " nicht gel�scht werden");
					}
					else {
						// ist der Datensatz �nderbar?
						if(!isConfigurationDataChangeable(atgUsage)) return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die konfigurierenden Datens�tze einer Definition eines Objekts von den Datens�tzen eines System-Objekts unterscheiden.
	 *
	 * @param datasets     eine Definition von konfigurierenden Datens�tzen einer Versorgungsdatei
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls sich die konfigurierenden Datens�tze einer Definition eines Objekts von den Datens�tzen eines System-Objekts unterscheiden
	 *         <br> <code>false</code>, sonst
	 */
	boolean isDatasetsDifferent(List<ConfigurationDataset> datasets, SystemObject systemObject) {
		if(systemObject.getType().isConfigurating()) {
			// sind Datens�tze vorhanden, die sp�ter nicht mehr dran sein sollen?
			final Collection<AttributeGroupUsage> futureAtgUsages = new ArrayList<AttributeGroupUsage>();
			final Collection<AttributeGroupUsage> usedAtgUsages = systemObject.getUsedAttributeGroupUsages();

			// Datens�tze und Mengen �berp�fen
			for(ConfigurationDataset dataset : datasets) {
				final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
				final Aspect asp = getAspect(dataset.getPidAspect());

				// Attributgruppenverwendung ermitteln
				final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
				if(atgUsage != null) futureAtgUsages.add(atgUsage);

				// ein Datensatz muss �berpr�ft werden
				if(isDatasetDifferent(dataset, systemObject)) return true;
			}

			for(AttributeGroupUsage atgUsage : usedAtgUsages) {
				if(!futureAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
				                                            || atgUsage.getAttributeGroup().getPid().equals(
						"atg.konfigurationsVerantwortlicherLaufendeNummer"
				))) {
					// es gibt Datens�tze, die nicht mehr an das Objekt geh�ren.
					return true;
				}
			}
		}
		else {
			final Collection<AttributeGroupUsage> futureAtgUsages = new ArrayList<AttributeGroupUsage>();
			// bei dynamischen Objekten, f�hren notwendige DS zur Neuerstellung des Objekts
			// notwendige und �nderbare DS d�rfen nicht gel�scht werden
			for(ConfigurationDataset dataset : datasets) {
				final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
				final Aspect asp = getAspect(dataset.getPidAspect());
				if(atg == null || asp == null) {
					throw new IllegalStateException(
							"Attributgruppe '" + dataset.getPidATG() + "' oder Aspekt '" + dataset.getPidAspect() + "' ist kein aktuelles Objekt."
					);
				}
				// ATGV ermitteln
				final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
				if(atgUsage == null) {
					throw new IllegalStateException(
							"Zur ATG '" + dataset.getPidATG() + "' und Aspekt '" + dataset.getPidAspect() + "' gibt es keine Attributgruppenverwendung."
					);
				}
				else {
					futureAtgUsages.add(atgUsage);
				}

				// Datensatz �berpr�fen
				if(isDataDifferent(dataset.getDataAnddataListAndDataField(), _configurationImport.getConfigurationData(systemObject, atgUsage))) {
					return true;
				}
			}

			// nicht mehr verwendete DS - d�rfen sie gel�scht werden?
			for(AttributeGroupUsage atgUsage : systemObject.getUsedAttributeGroupUsages()) {
				if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
				   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
					// nicht weiter beachten
				}
				else {
					if(!futureAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
					                                            || atgUsage.getAttributeGroup().getPid().equals(
							"atg.konfigurationsVerantwortlicherLaufendeNummer"
					))) {
						// es gibt Datens�tze, die nicht mehr an das Objekt geh�ren.
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob der konfigurierende Datensatz einer Definition mit dem Datensatz eines System-Objekt �bereinstimmt, oder falls nicht, ob das System-Objekt
	 * sich ver�ndern l�sst.
	 *
	 * @param dataset      Definition eines konfigurierenden Datensatzes
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls die Definition eines konfigurierenden Datensatzes mit dem Datensatz eines System-Objekts �bereinstimmt, oder falls nicht,
	 *         ob sich das System-Objekt �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isDatasetProcessable(ConfigurationDataset dataset, SystemObject systemObject) {
		final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
		final Aspect asp = getAspect(dataset.getPidAspect());

		// Attributgruppenverwendung ermitteln
		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);

		if(atgUsage == null || (isDataDifferent(dataset.getDataAnddataListAndDataField(), _configurationImport.getConfigurationData(systemObject, atgUsage))
		         && !isConfigurationDataChangeable(atgUsage))) {
			return false;
		}
		return true;
	}


	/**
	 * Pr�ft, ob der Datensatz �nderbar ist, falls er unterschiedlich zum bestehenden SystemObjekt ist.
	 *
	 * @param defaultParameters zu pr�fender Default-Parameter-Datensatz
	 * @param systemObject      das SystemObjekt mit dem zu vergleichenden Default-Parameter-Datensatz
	 *
	 * @return <code>true</code>, falls der Datensatz des Objekts weiterverarbeitet werden kann, sonst <code>false</code>.
	 */
	boolean isDefaultParameterProcessable(final ConfigurationDefaultParameter[] defaultParameters, final SystemObject systemObject) {
		if(isDefaultParameterDifferent(defaultParameters, systemObject) && !isConfigurationDataChangeable("atg.defaultParameterdatens�tze")) return false;

		return true;
	}

	/**
	 * Pr�ft, ob der zu setzende Default-Parameter-Datensatz sich vom Datensatz des SystemObjekts unterscheidet.
	 *
	 * @param defaultParameters zu pr�fenden Default-Parameter-Datensatz
	 * @param systemObject      das SystemObjekt mit dem zu vergleichenden Default-Parameter-Datensatz
	 *
	 * @return <code>true</code>, wenn sich die Datens�tze unterscheiden, sonst <code>false</code>.
	 */
	boolean isDefaultParameterDifferent(final ConfigurationDefaultParameter[] defaultParameters, final SystemObject systemObject) {
		final Map<String, ConfigurationDefaultParameter> atg2DefaultParameters = new HashMap<String, ConfigurationDefaultParameter>();
		for(ConfigurationDefaultParameter defaultParameter : defaultParameters) {
			atg2DefaultParameters.put(defaultParameter.getPidAtg(), defaultParameter);
		}

		// pr�fen, ob ein Default-Parameter unterschiedlich ist
//		final Data data = systemObject.getConfigurationData(getAttributeGroup("atg.defaultParameterdatens�tze"));
		final Data data = ((ConfigSystemObject)systemObject).getConfigurationData(
				_configurationImport.getAttributeGroupUsage(
						getAttributeGroup("atg.defaultParameterdatens�tze"), getAspect("asp.eigenschaften")
				), _configurationImport
		);
		if(data == null && defaultParameters.length > 0) return true;
		if(data != null) {
			final Data.Array array = data.getArray("Default-Parameterdatensatz");
			if(defaultParameters.length != array.getLength()) return true;

			for(int i = 0; i < array.getLength(); i++) {
				final Data item = array.getItem(i);
				// Attributgruppe pr�fen
				final AttributeGroup atg = (AttributeGroup)item.getReferenceValue("attributgruppe").getSystemObject();
				if(atg == null) {
					throw new IllegalStateException(
							"Ein Default-Parameter-Datensatz darf es nicht ohne Attributgruppe geben. Am Objekt " + systemObject.getPidOrNameOrId()
					);
				}
				final String atgPid = atg.getPid();
				if(getObject(atgPid) != atg) {
					_debug.fine("Attributgruppe " + atgPid + " im Defaultparameter von " + systemObject.getPidOrNameOrId() + " hat sich ge�ndert");
					return true;
				}
				final ConfigurationDefaultParameter defaultParameter = atg2DefaultParameters.get(atgPid);
				if(defaultParameter == null) return true;  // gibt es also noch nicht - Datensatz muss neu geschrieben werden

				// Typ �berpr�fen
				final SystemObject objectType = item.getReferenceValue("typ").getSystemObject();
				if(objectType == null) {
					throw new IllegalStateException("Ein Default-Parameter-Datensatz besitzt keinen Typen. Am Objekt " + systemObject.getPidOrNameOrId());
				}
				String typePid = defaultParameter.getPidType();
				if("".equals(typePid)) {
					// Typ des Objekts wird genommen oder falls es sich um einen Typen handelt, genau dieser
					if(systemObject instanceof SystemObjectType) {
						typePid = systemObject.getPid();
					}
					else {
						typePid = systemObject.getType().getPid();
					}
				}
				if(!typePid.equals(objectType.getPid())) return true;

				try {
					// Datens�tze miteinander vergleichen
					// Datensatz mit der angegebenen ATG erstellen
					final Data defaultData = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
					defaultData.setToDefault();
					_configurationImport.fillData(defaultData, defaultParameter.getDataAnddataListAndDataField());
					// aus dem Data ein byte-Array machen
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final int serializerVersion = item.getScaledValue("serialisierer").intValue();
					final Serializer serializer = SerializingFactory.createSerializer(serializerVersion, out);
					serializer.writeData(defaultData);
					final byte[] bytes = out.toByteArray();

					// byte-Arrays vergleichen
					final Data.Array datasetArray = item.getArray("datensatz");
					for(int j = 0; j < datasetArray.getLength(); j++) {
						if(bytes[j] != datasetArray.getScaledValue(j).byteValue()) return true;
					}
				}
				catch(Exception ex) {
					throw new IllegalStateException(ex);
				}
			}
		}
		return false;
	}

	/**
	 * Pr�ft, ob sich der konfigurierende Datensatz einer Definition von einem Datensatz eines System-Objekts unterscheidet.
	 *
	 * @param dataset      Definition eines konfigurierenden Datensatzes
	 * @param systemObject ein System-Objekt
	 *
	 * @return <code>true</code>, falls sich der konfigurierende Datensatz einer Definition von einem Datensatz eines System-Objekts unterscheidet <br>
	 *         <code>false</code>, sonst
	 */
	boolean isDatasetDifferent(ConfigurationDataset dataset, SystemObject systemObject) {
		final AttributeGroup atg = getAttributeGroup(dataset.getPidATG());
		final Aspect asp = getAspect(dataset.getPidAspect());
		// ATGV ermitteln
		final AttributeGroupUsage atgUsage = _configurationImport.getAttributeGroupUsage(atg, asp);
		if(atgUsage == null) return true; // wenn es noch nicht mal die ATGV gibt, dann kann der Datensatz auch nicht am Objekt stehen

		if(isDataDifferent(dataset.getDataAnddataListAndDataField(), _configurationImport.getConfigurationData(systemObject, atgUsage))) {
			_debug.finer("Die konfigurierenden Datens�tze der ATG '" + atg.getPid() + "' am Objekt sind unterschiedlich", systemObject.getPidOrNameOrId());
			return true;
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob die Mengen einer Objekt-Definition mit den Mengen eines System-Objekts �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern
	 * l�sst.
	 *
	 * @param configurationObjectSets Liste von Mengen einer Objekt-Definition
	 * @param configurationObject     ein konfigurierendes Objekt (System-Objekt)
	 *
	 * @return <code>true</code>, falls die Mengen einer Objekt-Definition mit den Mengen eines System-Objekts �bereinstimmt, oder falls nicht, ob sich das
	 *         System-Objekt �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetsProcessable(List<ConfigurationObjectSet> configurationObjectSets, ConfigurationObject configurationObject) {
		// Anzahl der Mengen �berpr�fen - wenn das Objekt bereits aktiviert wurde (davon gehen wir hier aus) kann sie nicht mehr ver�ndert werden.
		if(configurationObjectSets.size() != configurationObject.getObjectSets().size()) return false;

		// Gibt es auch alle Mengen, die importiert werden sollen?
		for(ConfigurationObjectSet configurationObjectSet : configurationObjectSets) {
			final ObjectSet objectSet = configurationObject.getObjectSet(configurationObjectSet.getName());
			if(objectSet != null) {
				// Elemente der Menge �berpr�fen
				if(!isSystemObjectSetProcessable(configurationObjectSet, objectSet)) return false;
			}
			else {
				// Menge existiert nicht
				return false;
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob sich die Mengen einer Objekt-Definition von den Mengen eines System-Objekts unterscheiden.
	 *
	 * @param configurationObjectSets Liste von Mengen einer Objekt-Definition
	 * @param configurationObject     ein konfigurierendes Objekt (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Mengen einer Objekt-Definition von den Mengen eines System-Objekts unterscheiden <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetsDifferent(List<ConfigurationObjectSet> configurationObjectSets, ConfigurationObject configurationObject) {
		// Anzahl der Mengen �berpr�fen - wenn das Objekt bereits aktiviert wurde (davon gehen wir hier aus) kann sie nicht mehr ver�ndert werden.
		if(configurationObjectSets.size() != configurationObject.getObjectSets().size()) {
			return true;
		}

		// Gibt es auch alle Mengen, die importiert werden sollen?
		for(ConfigurationObjectSet configurationObjectSet : configurationObjectSets) {
			final ObjectSet objectSet = configurationObject.getObjectSet(configurationObjectSet.getName());
			if(objectSet != null) {
				// Elemente der Menge �berpr�fen
				if(isSystemObjectSetDifferent(configurationObjectSet, objectSet)) {
					return true;
				}
			}
			else {
				// Menge existiert nicht
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt zur�ck, ob eine Menge einer Objekt-Definition mit einer Menge eines System-Objekts �bereinstimmt, oder falls nicht, ob das System-Objekt sich ver�ndern
	 * l�sst.
	 *
	 * @param configurationObjectSet Menge einer Objekt-Definition
	 * @param objectSet              Menge eines konfigurierendes Objekts (System-Objekt)
	 *
	 * @return <code>true</code>, falls eine Menge einer Objekt-Definition mit einer Menge eines System-Objekts �bereinstimmt, oder falls nicht, ob sich das
	 *         System-Objekt �ndern l�sst <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetProcessable(ConfigurationObjectSet configurationObjectSet, ObjectSet objectSet) {
		final ObjectSetType objectSetType = objectSet.getObjectSetType();
		final ReferenceType referenceType = objectSetType.getReferenceType();
		boolean isSetChangeable;

		if(isSystemObjectSetPropertiesDifferent(configurationObjectSet, objectSet)) {
			return false;
		}

		if(referenceType == ReferenceType.ASSOCIATION || objectSetType.isMutable()) {
			isSetChangeable = true;
		}
		else {
			isSetChangeable = false;
		}

		if(isSystemObjectSetElementsDifferent(configurationObjectSet, objectSet) && !isSetChangeable) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob sich die Menge einer Objekt-Definition von einer Menge eines System-Objekts unterscheidet.
	 *
	 * @param configurationObjectSet Menge einer Objekt-Definition
	 * @param objectSet              Menge eines konfigurierendes Objekts (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Menge einer Objekt-Definition von einer Menge eines System-Objekts unterscheidet <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetDifferent(ConfigurationObjectSet configurationObjectSet, ObjectSet objectSet) {
		return isSystemObjectSetPropertiesDifferent(configurationObjectSet, objectSet) || isSystemObjectSetElementsDifferent(configurationObjectSet, objectSet);
	}

	/**
	 * Pr�ft, ob sich die Eigenschaften der Menge einer Objekt-Definition von den Eigenschaften einer Menge eines System-Objekts unterscheidet.
	 *
	 * @param configurationObjectSet Menge einer Objekt-Definition
	 * @param objectSet              Menge eines konfigurierendes Objekts (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Menge einer Objekt-Definition von einer Menge eines System-Objekts unterscheidet <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetPropertiesDifferent(ConfigurationObjectSet configurationObjectSet, ObjectSet objectSet) {
		if(objectSet == null) {
			return true;
		}

		if(objectSet instanceof MutableSet) {
			final AttributeGroup dynamicSetPropertiesAtg = getAttributeGroup("atg.dynamischeMenge");
			final String importManagementPid = configurationObjectSet.getManagementPid();
			if(dynamicSetPropertiesAtg == null) {
				if(!importManagementPid.equals("")) {
					_debug.warning("Zugriff auf Verwaltungsinformationen von dynamischen Mengen nicht m�glich, da die eingesetzte Version des Bereichs"
				               + " kb.metaModellGlobal zu alt ist (mindestens Version 10 notwendig).");
				}
			}
			else {
				String managementPid = "";
				final Data data = objectSet.getConfigurationData(dynamicSetPropertiesAtg);
				if(data != null) managementPid = data.getTextValue("verwaltung").getValueText();
				if(!managementPid.equals(importManagementPid)) return true;
			}
		}

		return false;
	}
	/**
	 * Pr�ft, ob sich die Elemente der Menge einer Objekt-Definition von den Elementen einer Menge eines System-Objekts unterscheidet.
	 *
	 * @param configurationObjectSet Menge einer Objekt-Definition
	 * @param objectSet              Menge eines konfigurierendes Objekts (System-Objekt)
	 *
	 * @return <code>true</code>, falls sich die Menge einer Objekt-Definition von einer Menge eines System-Objekts unterscheidet <br> <code>false</code>, sonst
	 */
	boolean isSystemObjectSetElementsDifferent(ConfigurationObjectSet configurationObjectSet, ObjectSet objectSet) {
		if(objectSet == null) {
			return true;
		}
		List<SystemObject> elements = null;
		if(objectSet.getObjectSetType().isMutable()) {
			elements = objectSet.getElements();
		}
		else {
			NonMutableSet nonMutableSet = (NonMutableSet)objectSet;
			elements = nonMutableSet.getElementsInModifiableVersion();
		}
		if(elements == null) {
			return true;
		}

		// Anzahl �berpr�fen
		if(configurationObjectSet.getElements().length != elements.size()) {
			return true;
		}
		// Elemente �berpr�fen
		for(String pid : configurationObjectSet.getElements()) {
			if(!elements.contains(getObject(pid))) {
				return true;
			}
		}
		return false;
	}

	/* ############# Fragen an die Konfiguration bzw. an die neu importierten Objekte. ############## */

	/**
	 * Ermittelt ein System-Objekt anhand der angegebenen Pid. Dabei werden auch die Objekte ber�cksichtigt, die bei diesem Import-Vorgang erstellt wurden.
	 *
	 * @param pid die Pid eines System-Objekts
	 *
	 * @return Das gesuchte System-Objekt, oder <code>null</code>, falls es zur angegebenen Pid kein System-Objekt gibt.
	 */
	private SystemObject getObject(String pid) {
		return _configurationImport.getObject(pid);
	}

	/**
	 * Gibt zur angegebenen Pid die gesuchte Attributgruppe zur�ck.
	 *
	 * @param pid die Pid einer Attributgruppe
	 *
	 * @return Die gesuchte Attributgruppe, oder <code>null</code>, falls es zur angegebenen Pid keine Attributgruppe gibt.
	 */
	private AttributeGroup getAttributeGroup(String pid) {
		final SystemObject systemObject = getObject(pid);
		if(systemObject instanceof AttributeGroup) {
			return (AttributeGroup)systemObject;
		}
		else {
			return null;
		}
	}

	/**
	 * Gibt zur angegebenen Pid den gesuchten Aspekt zur�ck.
	 *
	 * @param pid die Pid des Aspekts
	 *
	 * @return Der gesuchte Aspekt, oder <code>null</code>, falls es zur angegebenen Pid keinen Aspekt gibt.
	 */
	private Aspect getAspect(String pid) {
		final SystemObject systemObject = getObject(pid);
		if(systemObject instanceof Aspect) {
			return (Aspect)systemObject;
		}
		else {
			return null;
		}
	}

	/**
	 * Gibt zur angegebenen Pid den gesuchten Objekt-Typen zur�ck.
	 *
	 * @param pid die Pid eines Objekt-Typen
	 *
	 * @return Den gesuchten Objekt-Typen, oder <code>null</code>, falls es zur angegebenen Pid keinen Objekt-Typen gibt.
	 */
	private SystemObjectType getType(String pid) {
		final SystemObject systemObject = getObject(pid);
		if(systemObject instanceof SystemObjectType) {
			return (SystemObjectType)systemObject;
		}
		else {
			return null;
		}
	}

	/**
	 * Gibt zur angegebenen Pid den gesuchten Mengen-Typ zur�ck.
	 *
	 * @param pid die Pid eines Mengen-Typs
	 *
	 * @return Den gesuchten Mengen-Typs, oder <code>null</code>, falls es zur angegebenen Pid keinen Mengen-Typ gibt.
	 */
	private ObjectSetType getObjectSetType(String pid) {
		final SystemObject systemObject = getObject(pid);
		if(systemObject instanceof ObjectSetType) {
			return (ObjectSetType)systemObject;
		}
		else {
			return null;
		}
	}

	/**
	 * Transformiert aus der angegebenen Menge von Pids (von Super-Typen) eine Liste von Super-Typ-Objekte.
	 *
	 * @param superTypePids Menge von Pids von Super-Typen
	 *
	 * @return Eine Liste von Objekt-Typen.
	 */
	private List<SystemObjectType> createSuperTypes(String[] superTypePids) {
		final List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
		for(String pid : superTypePids) {
			superTypes.add(getType(pid));
		}
		return superTypes;
	}

	/**
	 * Ermittelt, ob sich die definierten Elemente von dem Data-Objekt unterscheiden.
	 *
	 * @param elements definierte Elemente eines Datensatzes
	 * @param data     ein konkreter Datensatz
	 *
	 * @return <code>true</code>, falls sich die Elemente eines definierten Datensatzes von dem konkreten Datensatz unterscheiden <br> <code>false</code>, sonst
	 */
	private boolean isDataDifferent(DatasetElement[] elements, Data data) {
		if(data == null) return true;
		for(int i = 0; i < elements.length; i++) {
			DatasetElement datasetElement = elements[i];

			if(datasetElement instanceof ConfigurationData) {
				ConfigurationData configurationData = (ConfigurationData)datasetElement;
				Data item;
				if(configurationData.getName().equals("-")) {
					item = data.getItem(String.valueOf(i));
				}
				else {
					item = data.getItem(configurationData.getName());
				}
				String value = configurationData.getValue();
				final AttributeType attributeType = item.getAttributeType();

				if(attributeType instanceof StringAttributeType) {
					final Data.TextValue itemTextValue = item.asTextValue();
					if(!itemTextValue.getText().equals(value)) {
						return true;
					}
				}
				else {
					value = value.trim();

					if(attributeType instanceof ReferenceAttributeType) {
						final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)attributeType;
						if(value.equals("") || value.equals("0") || value.equals("null") || value.equals("undefiniert")) {
							if(referenceAttributeType.getReferenceType()== ReferenceType.ASSOCIATION) {
								if(item.asReferenceValue().getSystemObjectPid().length() != 0) return true;
							}
							else {
								if(item.asReferenceValue().getSystemObject() != null) return true;
							}
						}
						else {
							if(referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION) {
								if(!value.equals(item.asReferenceValue().getSystemObjectPid())) return true;
							}
							else {
								final SystemObject systemObject = item.asReferenceValue().getSystemObject();
								if(systemObject == null) return true;
								if(systemObject != getObject(value)) return true;
							}
						}
					}
					else {
						final Data.TextValue itemTextValue = item.asTextValue();
						if(!itemTextValue.getText().equals(value)) {
							// evtl. wurde lediglich die Einheit weggelassen, dann stimmen aber noch die Werte �berein
							if(!itemTextValue.getValueText().equals(value)) {
								boolean isProbablyDifferent = true;
								// Bestimmte Unterschiede werden toleriert, weil sie zum gleichen Ergebnis f�hren w�rden
								if(item.getAttributeType() instanceof IntegerAttributeType) {
									final IntegerAttributeType integerAttributeType = (IntegerAttributeType)item.getAttributeType();
									// Bei GanzzahlAttributen k�nnen die Werte auch im Locationcode-Distance Format angegeben werden.
									// Ist der Text im location-distance Format? Dieses Format wird f�r Knotennummern in der TLS-Versorgung von Ger�ten benutzt.
									// Es enth�lt den Locationcode und mit einem Minuszeichen getrennt eine Distance. Der Locationcode ist eine Zahl zwischen
									// 0 und 65535, Distance ist eine Zahl zwischen 0 und 255. Die beiden Zahlen werden mit folgender Formel in eine Zahl konvertiert:
									// (Locationcode * 256) + Distance
									// Das Ergebnis wird dann mit dem unskalierten Attributwert verglichen
									if(_locationDistancePattern.matcher(value).matches()) {
										String[] locationDistance = value.split("\\s*-\\s*");
										if(locationDistance.length == 2) {
											int location = Integer.parseInt(locationDistance[0]);
											int distance = Integer.parseInt(locationDistance[1]);
											if(location <= 65535 && distance <= 255 && item.asUnscaledValue().longValue() == (location * 256) + distance) {
												isProbablyDifferent = false;
											}
										}
									}
									if(isProbablyDifferent) {
										// Wenn der Attributtyp einen Wertebereich hat dann wird bei Skalierungsfaktor 1 auf Long-Basis ein Vergleich gemacht
										// und bei einem anderen Skalierungsfaktor ein Vergleich auf Double-Basis
										try {
											IntegerValueRange range = integerAttributeType.getRange();
											if(range != null) {
												Number number;
												ParsePosition parsePosition = new ParsePosition(0);
												number = _parseNumberFormat.parse(value.replace('.', ','), parsePosition);
												if(number != null && item.asScaledValue().isNumber()) {
													if(range.getConversionFactor() == 1) {
														if(number.longValue() == item.asScaledValue().longValue()) {
															isProbablyDifferent = false;
														}
													}
													else {
														if(number.doubleValue() == item.asScaledValue().doubleValue()) {
															isProbablyDifferent = false;
														}
													}
												}
											}
										}
										catch(Exception e) {
											// Vergleich auf Zahl-Basis nicht m�glich
										}
									}
								}
								if(isProbablyDifferent) return true;
							}
						}
					}
				}
			}
			else if(datasetElement instanceof ConfigurationDataList) {
				ConfigurationDataList dataList = (ConfigurationDataList)datasetElement;
				Data item;
				if(dataList.getName().equals("-")) {
					item = data.getItem(String.valueOf(i));
				}
				else {
					item = data.getItem(dataList.getName());
				}
				if(isDataDifferent(dataList.getDataAndDataListAndDataField(), item)) return true;
			}
			else if(datasetElement instanceof ConfigurationDataField) {
				ConfigurationDataField dataField = (ConfigurationDataField)datasetElement;
				// Gr��e des Arrays beachten
				Data item = data.getItem(dataField.getName());
				if(item.asArray().getLength() != dataField.getDataAndDataList().length) return true;
				if(isDataDifferent(dataField.getDataAndDataList(), item)) return true;
			}
		}
		return false;
	}

	/**
	 * Diese Methode pr�ft, ob ein konfigurierender Datensatz ver�ndert werden darf. Als Aspekt wird "asp.eigenschaften" angenommen.
	 *
	 * @param attributeGroup Die Attributgruppe des Datensatzes.
	 *
	 * @return <code>true</code>, falls der Datensatz ge�ndert werden darf<br><code>false</code>, sonst
	 *
	 * @see #isConfigurationDataChangeable(String,String)
	 */
	private boolean isConfigurationDataChangeable(String attributeGroup) {
		return isConfigurationDataChangeable(attributeGroup, "asp.eigenschaften");
	}

	/**
	 * Diese Methode pr�ft, ob ein konfigurierender Datensatz ver�ndert werden darf.
	 *
	 * @param attributeGroup Die Attributgruppe des Datensatzes.
	 * @param aspect         Der Aspekt des Datensatzes.
	 *
	 * @return <code>true</code>, falls der Datensatz ge�ndert werden darf<br><code>false</code>, sonst
	 */
	private boolean isConfigurationDataChangeable(String attributeGroup, String aspect) {
		final AttributeGroup atg = _dataModel.getAttributeGroup(attributeGroup);
		final Aspect asp = _dataModel.getAspect(aspect);
		final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(asp);
		return isConfigurationDataChangeable(atgUsage);
	}

	/**
	 * Diese Methode pr�ft, ob ein konfigurierender Datensatz ver�ndert werden darf.
	 *
	 * @param atgUsage Die Attributgruppenverwendung des Datensatzes.
	 *
	 * @return <code>true</code>, falls der Datensatz ge�ndert werden darf<br><code>false</code>, sonst
	 */
	private boolean isConfigurationDataChangeable(final AttributeGroupUsage atgUsage) {
		if(atgUsage.getUsage() == AttributeGroupUsage.Usage.OptionalConfigurationData
		   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Pr�ft, ob die angegebene Menge eines Konfigurationsobjekts �nderbar ist.
	 *
	 * @param object  ein Konfigurationsobjekt
	 * @param setName Name einer Menge
	 *
	 * @return <code>true</code>, falls die Menge �nderbar ist, sonst <code>false</code>
	 */
	private boolean isSetChangeable(ConfigurationObject object, String setName) {
		final ObjectSet objectSet = object.getObjectSet(setName);
		final ObjectSetType objectSetType = objectSet.getObjectSetType();
		final ReferenceType referenceType = objectSetType.getReferenceType();
		return referenceType == ReferenceType.ASSOCIATION;
	}
}

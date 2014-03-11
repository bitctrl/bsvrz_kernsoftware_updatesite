/*
 * Copyright 2013 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.consistencycheck;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.datamodel.ConfigNonMutableSet;
import de.bsvrz.puk.config.configFile.datamodel.ConfigSystemObject;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationValueRange;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hilfsklasse für die Spezialbehandlungen bei unversionierten Datenmodelländerungen.
 *
 * Der Import läuft in zwei Schritten ab. Zurerst wird mit den Processable-Methoden geprüft, ob unversionierte Änderungen
 * möglich sind (true) oder nicht (false). Wenn unversionierte Änderungen möglich sind und auch keine anderen Änderungen dagegen sprechen wird
 * später dann bei den entsprechenden allow...-Methoden nocheinmal geprüft, ob die Änderugnen durchgeführt werden dürfen (die Logik muss
 * natürlich identisch sein). Da die allow...-Methoden direkt vor dem Ändern der Konfigurationsdateien ausgeführt werden, wird in den allow...-
 * Methoden auch der entsprechende Datensatz am Konfigurationsbereich ergänzt, der Informationen über unversionierte Änderungen enthält.
 *
 * Dieser Datensatz darf noch nicht bei den Processable-Methoden ergänzt werden, da dort noch niocht feststeht, dass nicht auch andere
 * Aktionen durchgeführt werden, die trotzdem eine neue Objektversion bewirken würden (z.B. hinzufügen vo nStatuswerten (würde erlaubt werden)
 * mit gleichzeitigem Löschen von Statuswerten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11583 $
 */
public class RelaxedModelChanges {

	private static final Debug _debug = Debug.getLogger();

	private static boolean _isEnabled;
	private final AttributeGroup _attributeGroup;

	public RelaxedModelChanges(final DataModel dataModel) {
		_attributeGroup = dataModel.getAttributeGroup("atg.konfigurationsBereichUnversionierteÄnderungen");
		if(_attributeGroup == null){
			_debug.fine(
					"Unversionierte Änderungen nicht möglich, atg.konfigurationsBereichUnversionierteÄnderungen nicht gefunden. Bitte kb.metaModellGlobal in Mindestversion 16 installieren."
			);
			_isEnabled = false;
		}
		else {
			_debug.fine(
					"Unversionierte Änderungen aktiviert."
			);
			_isEnabled = true;
		}
	}

	/**
	 * Sonderbehandlung für Wertebereiche. Das Ändern, wird erlaubt, wenn der Bereich nicht verkleinert wird und Einheit und Skalierung beibehalten werden.
	 * @param importedValueRange Eingelesene Bereichsdefinition
	 * @param existingValueRange Existierende Bereichsdefinition
	 * @return true: die Änderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean isValueRangeChangeProcessable(final ConfigurationValueRange importedValueRange, final IntegerValueRange existingValueRange) {

		if(!_isEnabled) return false;

		// Skalierung prüfen
		if(importedValueRange.getScale() != existingValueRange.getConversionFactor()) return false;

		// Einheit prüfen
		if(!importedValueRange.getUnit().equals(existingValueRange.getUnit())) return false;

		// Bereich prüfen
		if(importedValueRange.getMinimum() > existingValueRange.getMinimum()) return false;
		if(importedValueRange.getMaximum() < existingValueRange.getMaximum()) return false;

		return true;
	}
	/**
	 * Gibt zurück, ob zu dem angegebenen IntegerAttributeType neue Statuswerte hinzugefügt werden dürfen
	 * @param integerAttributeType IntegerAttributeType
	 * @return true falls neue Statuswerte hinzugefügt werden dürfen, sonst false
	 */
	public boolean isAddStatesProcessable(final IntegerAttributeType integerAttributeType) {
		if(!_isEnabled) return false;
		return true;
	}

	/**
	 * Gibt zurück, ob die maximale Länge eines Feldes geändert werden darf. Vorraussetzung: oldMaxCount != newMaxCount und das
	 * Feld hat eine variable Länge.
	 * @param oldMaxCount Alte Länge
	 * @param newMaxCount Neue Länge
	 * @return Darf die Feldlänge ohne Versionierung geändert werden?
	 */
	public boolean isChangeArrayMaxCountProcessable(final int oldMaxCount, final int newMaxCount) {
		if(!_isEnabled) return false;
		return newMaxCount >= oldMaxCount && getDataType(oldMaxCount) == getDataType(newMaxCount);
	}

	/**
	 * Gibt zurück, ob das Ändern eines Wert-Namens erlaubt sein soll, ohne eine neue Version zu erstellen.
	 * @param integerValueState Statuswert
	 * @return Darf der Name geädnert werden?
	 */
	public boolean isChangeValueNameProcessable(final IntegerValueState integerValueState) {
		if(!_isEnabled) return false;

		// Zugehörigen Attributtyp suchen
		IntegerAttributeType integerAttributeType = null;
		for(SystemObject systemObject : integerValueState.getConfigurationArea().getCurrentObjects()) {
			if(systemObject instanceof IntegerAttributeType) {
				IntegerAttributeType tmp = (IntegerAttributeType) systemObject;
				if (tmp.getStates().contains(integerValueState)){
					integerAttributeType = tmp;
				}
			}
		}

		if(integerAttributeType == null) return false;

		return true;
	}

	/**
	 * Gibt true zurück sofern der Wertebereich bearbeitet werden darf und markiert den zugehörigen Attributtyp als durch unversionierte Änderungen bearbeitet.
	 *
	 * @param configSystemObject
	 * @param oldData Existierende Bereichsdefinition Alter Datensatz
	 * @param newData Eingelesene Bereichsdefinition Neuer Datensatz
	 * @return true: die Änderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean allowChangeValueRange(final ConfigSystemObject configSystemObject, final Data oldData, final Data newData) {

		if(!_isEnabled) return false;

		if(oldData == null || newData == null) return false;

		// Zugehörigen Attributtyp suchen
		IntegerAttributeType integerAttributeType = null;
		for(SystemObject systemObject : configSystemObject.getConfigurationArea().getCurrentObjects()) {
			if(systemObject instanceof IntegerAttributeType) {
				IntegerAttributeType tmp = (IntegerAttributeType) systemObject;
				if (tmp.getRange().equals(configSystemObject)){
					integerAttributeType = tmp;
				}
			}
		}

		if(integerAttributeType == null) return false;

		// Skalierung prüfen
		if(newData.getScaledValue("skalierung").doubleValue() != oldData.getScaledValue("skalierung").doubleValue()) return false;

		// Einheit prüfen
		if(!newData.getTextValue("einheit").getValueText().equals(oldData.getTextValue("einheit").getValueText())) return false;

		// Bereich prüfen
		if(newData.getUnscaledValue("minimum").longValue() > oldData.getUnscaledValue("minimum").longValue()) return false;
		if(newData.getUnscaledValue("maximum").longValue() < oldData.getUnscaledValue("maximum").longValue()) return false;

		if(!markAsUnversionedChanged(integerAttributeType)) return false;
		return true;
	}
	/**
	* Gibt zurück, ob das Ändern eines Wert-Namens erlaubt sein soll und markiert den zugehörigen Attributtyp als durch unversionierte Änderungen bearbeitet.
	* @param integerValueState Statuswert
	* @return Darf der Name geändert werden?
	*/
	public boolean allowChangeValueName(final IntegerValueState integerValueState) {
		if(!_isEnabled) return false;

		// Zugehörigen Attributtyp suchen
		IntegerAttributeType integerAttributeType = null;
		for(SystemObject systemObject : integerValueState.getConfigurationArea().getCurrentObjects()) {
			if(systemObject instanceof IntegerAttributeType) {
				IntegerAttributeType tmp = (IntegerAttributeType) systemObject;
				if (tmp.getStates().contains(integerValueState)){
					integerAttributeType = tmp;
				}
			}
		}

		if(integerAttributeType == null) return false;
		if(!markAsUnversionedChanged(integerAttributeType)) return false;

		return true;
	}


	/**
	 * Gibt true zurück, sofern der Wertezustand hinzugefügt werden darf und markiert den zugehörigen Attributtyp als durch unversionierte Änderungen bearbeitet.
	 * @param objectSet Objekt-Menge
	 * @return
	 */
	public boolean allowObjectSetAdd(final ConfigNonMutableSet objectSet) {

		if(!_isEnabled) return false;

		if(!objectSet.getObjectSetType().getPid().equals("menge.werteZustaende")) {
			return false;
		}
		for(SystemObject systemObject : objectSet.getConfigurationArea().getCurrentObjects()) {
			if(systemObject instanceof IntegerAttributeType) {
				IntegerAttributeType integerAttributeType = (IntegerAttributeType) systemObject;
				if (objectSet.equals(integerAttributeType.getNonMutableSet(objectSet.getName()))){
					return markAsUnversionedChanged(integerAttributeType);
				}
			}
		}
		return false;
	}

	/**
	 * Gibt true zurück, falls die Länge des Daten-Arrays geändert werden darf und markiert den zugehörigen Attributtyp als durch unversionierte Änderungen bearbeitet.
	 *
	 * @param configSystemObject SystemObjekt des Attributes
	 * @param oldData Existierende Attributtypeigenschaften Alter Datensatz
	 * @param newData Eingelesene Attributtypeigenschaften Neuer Datensatz
	 * @return true: die Änderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean allowChangeArrayMaxCount(final ConfigSystemObject configSystemObject, final Data oldData, final Data newData) {
		if(!_isEnabled) return false;

		if(oldData == null || newData == null) return false;

		if(!(configSystemObject instanceof Attribute)) {
			return false;
		}

		Attribute attribute = (Attribute) configSystemObject;
		AttributeType attributeType = attribute.getAttributeType();


		// Position muss übereinstimmen
		if(newData.getScaledValue("position").doubleValue() != oldData.getScaledValue("position").doubleValue()) return false;

		// Anzahl-Variabel muss übereinstimmen...
		if(!newData.getTextValue("anzahlVariabel").getValueText().equals(oldData.getTextValue("anzahlVariabel").getValueText())) return false;
		// ... und auf "Ja" stehen.
		if(!newData.getTextValue("anzahlVariabel").getValueText().equals("Ja")) return false;

		// Attributtyp muss übereinstimmen
		if(!(newData.getReferenceValue("attributTyp").getId() == oldData.getReferenceValue("attributTyp").getId())) return false;

		// Länge darf nur größer werden, muss aber im selben Wertebereich bleiben
		if(!isChangeArrayMaxCountProcessable(oldData.getUnscaledValue("anzahl").intValue(), newData.getUnscaledValue("anzahl").intValue())) {
			return false;
		}

		if(!markAsUnversionedChanged(attributeType)) return false;

		return true;
	}

	private boolean markAsUnversionedChanged(final AttributeType attributeType) {

		if(!_isEnabled) return false;

		ConfigurationArea configurationArea = attributeType.getConfigurationArea();
		Data configurationData = configurationArea.getConfigurationData(_attributeGroup);
		if(configurationData == null){
			_debug.warning(
					"Führe unversionierte Änderung am Attributtyp " + attributeType + " durch. Der Konfigurationsbereich wird kb.metaModellGlobal in Mindestversion 16 voraussetzen."
			);
			configurationData = AttributeBaseValueDataFactory.createAdapter(_attributeGroup, AttributeHelper.getAttributesValues(_attributeGroup));
		}
		else {
			_debug.fine(
					"Führe unversionierte Änderung am Attributtyp " + attributeType + " durch."
			);
		}
		boolean added = false;
		Data versions = configurationData.getItem("versionen");
		for(Data data : versions) {
			if(data.getUnscaledValue("Version").shortValue() == configurationArea.getModifiableVersion()){
				Data.TextArray array = data.getTextArray("AttributTypen");
				Set<String> systemObjects = new TreeSet<String>(Arrays.asList(array.getTextArray()));
				systemObjects.add(attributeType.getPid());
				array.set(systemObjects.toArray(new String[systemObjects.size()]));
				added = true;
			}
		}
		if(!added){
			Data.Array array = versions.asArray();
			array.setLength(array.getLength() + 1);
			Data last = array.getItem(array.getLength() - 1);
			last.getUnscaledValue("Version").set(configurationArea.getModifiableVersion());
			last.getTextArray("AttributTypen").set(attributeType.getPid());
		}
		try {
			configurationArea.setConfigurationData(_attributeGroup, configurationData);
		}
		catch(ConfigurationChangeException e) {
			_debug.warning("Konnte unversionierte Änderung nicht durchführen. Der Datensatz am Konfigurationsbereich konnte nicht geschrieben werden", e);
			return false;
		}
		return true;
	}

	/**
	 * Gibt für jeden Wertebereich (Byte, Short, Integer) eine eindeutige Nummer (0,1,2) zurück.
	 * @param maxCount
	 */
	private static int getDataType(final int maxCount) {
		if(maxCount < 0) throw new IllegalArgumentException();
		if(maxCount < 256) return 0;
		if(maxCount < 65536) return 1;
		return 2;
	}

	public static RelaxedModelChanges getInstance(final DataModel dataModel) {
		// Objekt sehr klein, halten der Objekte in einer HashMap o.ä. vorerst unnötig.
		return new RelaxedModelChanges(dataModel);
	}
}

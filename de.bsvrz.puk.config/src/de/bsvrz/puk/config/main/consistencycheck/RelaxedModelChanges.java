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
 * Hilfsklasse f�r die Spezialbehandlungen bei unversionierten Datenmodell�nderungen.
 *
 * Der Import l�uft in zwei Schritten ab. Zurerst wird mit den Processable-Methoden gepr�ft, ob unversionierte �nderungen
 * m�glich sind (true) oder nicht (false). Wenn unversionierte �nderungen m�glich sind und auch keine anderen �nderungen dagegen sprechen wird
 * sp�ter dann bei den entsprechenden allow...-Methoden nocheinmal gepr�ft, ob die �nderugnen durchgef�hrt werden d�rfen (die Logik muss
 * nat�rlich identisch sein). Da die allow...-Methoden direkt vor dem �ndern der Konfigurationsdateien ausgef�hrt werden, wird in den allow...-
 * Methoden auch der entsprechende Datensatz am Konfigurationsbereich erg�nzt, der Informationen �ber unversionierte �nderungen enth�lt.
 *
 * Dieser Datensatz darf noch nicht bei den Processable-Methoden erg�nzt werden, da dort noch niocht feststeht, dass nicht auch andere
 * Aktionen durchgef�hrt werden, die trotzdem eine neue Objektversion bewirken w�rden (z.B. hinzuf�gen vo nStatuswerten (w�rde erlaubt werden)
 * mit gleichzeitigem L�schen von Statuswerten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11583 $
 */
public class RelaxedModelChanges {

	private static final Debug _debug = Debug.getLogger();

	private static boolean _isEnabled;
	private final AttributeGroup _attributeGroup;

	public RelaxedModelChanges(final DataModel dataModel) {
		_attributeGroup = dataModel.getAttributeGroup("atg.konfigurationsBereichUnversionierte�nderungen");
		if(_attributeGroup == null){
			_debug.fine(
					"Unversionierte �nderungen nicht m�glich, atg.konfigurationsBereichUnversionierte�nderungen nicht gefunden. Bitte kb.metaModellGlobal in Mindestversion 16 installieren."
			);
			_isEnabled = false;
		}
		else {
			_debug.fine(
					"Unversionierte �nderungen aktiviert."
			);
			_isEnabled = true;
		}
	}

	/**
	 * Sonderbehandlung f�r Wertebereiche. Das �ndern, wird erlaubt, wenn der Bereich nicht verkleinert wird und Einheit und Skalierung beibehalten werden.
	 * @param importedValueRange Eingelesene Bereichsdefinition
	 * @param existingValueRange Existierende Bereichsdefinition
	 * @return true: die �nderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean isValueRangeChangeProcessable(final ConfigurationValueRange importedValueRange, final IntegerValueRange existingValueRange) {

		if(!_isEnabled) return false;

		// Skalierung pr�fen
		if(importedValueRange.getScale() != existingValueRange.getConversionFactor()) return false;

		// Einheit pr�fen
		if(!importedValueRange.getUnit().equals(existingValueRange.getUnit())) return false;

		// Bereich pr�fen
		if(importedValueRange.getMinimum() > existingValueRange.getMinimum()) return false;
		if(importedValueRange.getMaximum() < existingValueRange.getMaximum()) return false;

		return true;
	}
	/**
	 * Gibt zur�ck, ob zu dem angegebenen IntegerAttributeType neue Statuswerte hinzugef�gt werden d�rfen
	 * @param integerAttributeType IntegerAttributeType
	 * @return true falls neue Statuswerte hinzugef�gt werden d�rfen, sonst false
	 */
	public boolean isAddStatesProcessable(final IntegerAttributeType integerAttributeType) {
		if(!_isEnabled) return false;
		return true;
	}

	/**
	 * Gibt zur�ck, ob die maximale L�nge eines Feldes ge�ndert werden darf. Vorraussetzung: oldMaxCount != newMaxCount und das
	 * Feld hat eine variable L�nge.
	 * @param oldMaxCount Alte L�nge
	 * @param newMaxCount Neue L�nge
	 * @return Darf die Feldl�nge ohne Versionierung ge�ndert werden?
	 */
	public boolean isChangeArrayMaxCountProcessable(final int oldMaxCount, final int newMaxCount) {
		if(!_isEnabled) return false;
		return newMaxCount >= oldMaxCount && getDataType(oldMaxCount) == getDataType(newMaxCount);
	}

	/**
	 * Gibt zur�ck, ob das �ndern eines Wert-Namens erlaubt sein soll, ohne eine neue Version zu erstellen.
	 * @param integerValueState Statuswert
	 * @return Darf der Name ge�dnert werden?
	 */
	public boolean isChangeValueNameProcessable(final IntegerValueState integerValueState) {
		if(!_isEnabled) return false;

		// Zugeh�rigen Attributtyp suchen
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
	 * Gibt true zur�ck sofern der Wertebereich bearbeitet werden darf und markiert den zugeh�rigen Attributtyp als durch unversionierte �nderungen bearbeitet.
	 *
	 * @param configSystemObject
	 * @param oldData Existierende Bereichsdefinition Alter Datensatz
	 * @param newData Eingelesene Bereichsdefinition Neuer Datensatz
	 * @return true: die �nderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean allowChangeValueRange(final ConfigSystemObject configSystemObject, final Data oldData, final Data newData) {

		if(!_isEnabled) return false;

		if(oldData == null || newData == null) return false;

		// Zugeh�rigen Attributtyp suchen
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

		// Skalierung pr�fen
		if(newData.getScaledValue("skalierung").doubleValue() != oldData.getScaledValue("skalierung").doubleValue()) return false;

		// Einheit pr�fen
		if(!newData.getTextValue("einheit").getValueText().equals(oldData.getTextValue("einheit").getValueText())) return false;

		// Bereich pr�fen
		if(newData.getUnscaledValue("minimum").longValue() > oldData.getUnscaledValue("minimum").longValue()) return false;
		if(newData.getUnscaledValue("maximum").longValue() < oldData.getUnscaledValue("maximum").longValue()) return false;

		if(!markAsUnversionedChanged(integerAttributeType)) return false;
		return true;
	}
	/**
	* Gibt zur�ck, ob das �ndern eines Wert-Namens erlaubt sein soll und markiert den zugeh�rigen Attributtyp als durch unversionierte �nderungen bearbeitet.
	* @param integerValueState Statuswert
	* @return Darf der Name ge�ndert werden?
	*/
	public boolean allowChangeValueName(final IntegerValueState integerValueState) {
		if(!_isEnabled) return false;

		// Zugeh�rigen Attributtyp suchen
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
	 * Gibt true zur�ck, sofern der Wertezustand hinzugef�gt werden darf und markiert den zugeh�rigen Attributtyp als durch unversionierte �nderungen bearbeitet.
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
	 * Gibt true zur�ck, falls die L�nge des Daten-Arrays ge�ndert werden darf und markiert den zugeh�rigen Attributtyp als durch unversionierte �nderungen bearbeitet.
	 *
	 * @param configSystemObject SystemObjekt des Attributes
	 * @param oldData Existierende Attributtypeigenschaften Alter Datensatz
	 * @param newData Eingelesene Attributtypeigenschaften Neuer Datensatz
	 * @return true: die �nderung wird erlaubt, false: keine Sonderbehandlung, hier nicht erlaubt
	 */
	public boolean allowChangeArrayMaxCount(final ConfigSystemObject configSystemObject, final Data oldData, final Data newData) {
		if(!_isEnabled) return false;

		if(oldData == null || newData == null) return false;

		if(!(configSystemObject instanceof Attribute)) {
			return false;
		}

		Attribute attribute = (Attribute) configSystemObject;
		AttributeType attributeType = attribute.getAttributeType();


		// Position muss �bereinstimmen
		if(newData.getScaledValue("position").doubleValue() != oldData.getScaledValue("position").doubleValue()) return false;

		// Anzahl-Variabel muss �bereinstimmen...
		if(!newData.getTextValue("anzahlVariabel").getValueText().equals(oldData.getTextValue("anzahlVariabel").getValueText())) return false;
		// ... und auf "Ja" stehen.
		if(!newData.getTextValue("anzahlVariabel").getValueText().equals("Ja")) return false;

		// Attributtyp muss �bereinstimmen
		if(!(newData.getReferenceValue("attributTyp").getId() == oldData.getReferenceValue("attributTyp").getId())) return false;

		// L�nge darf nur gr��er werden, muss aber im selben Wertebereich bleiben
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
					"F�hre unversionierte �nderung am Attributtyp " + attributeType + " durch. Der Konfigurationsbereich wird kb.metaModellGlobal in Mindestversion 16 voraussetzen."
			);
			configurationData = AttributeBaseValueDataFactory.createAdapter(_attributeGroup, AttributeHelper.getAttributesValues(_attributeGroup));
		}
		else {
			_debug.fine(
					"F�hre unversionierte �nderung am Attributtyp " + attributeType + " durch."
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
			_debug.warning("Konnte unversionierte �nderung nicht durchf�hren. Der Datensatz am Konfigurationsbereich konnte nicht geschrieben werden", e);
			return false;
		}
		return true;
	}

	/**
	 * Gibt f�r jeden Wertebereich (Byte, Short, Integer) eine eindeutige Nummer (0,1,2) zur�ck.
	 * @param maxCount
	 */
	private static int getDataType(final int maxCount) {
		if(maxCount < 0) throw new IllegalArgumentException();
		if(maxCount < 256) return 0;
		if(maxCount < 65536) return 1;
		return 2;
	}

	public static RelaxedModelChanges getInstance(final DataModel dataModel) {
		// Objekt sehr klein, halten der Objekte in einer HashMap o.�. vorerst unn�tig.
		return new RelaxedModelChanges(dataModel);
	}
}

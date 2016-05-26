/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Bildet einen "defaultParameter"-Datensatz ab, der nach der K2S.DTD definiert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigurationDefaultParameter {

	private final String _pidType;

	private final String _pidAtg;

	/** Speichert Datum, Datenliste und Datenfeld in der Reihenfolge wie sie eingelesen wurden. */
	private DatasetElement[] _dataAnddataListAndDataField = new DatasetElement[0];

	/**
	 * Erzeugt ein Objekt "defaultParameter" (siehe K2S.DTD) mit einer bestimmten Attributgruppe.
	 *
	 * @param pidAtg Pid der Attributgruppe, die für den DefaultParameter verwendet werden soll
	 */
	public ConfigurationDefaultParameter(final String pidAtg) {
		this("", pidAtg);
	}

	/**
	 * Erzeugt ein Objekt "defaultParameter" (siehe K2S.DTD) mit einer bestimmten Attributgruppe und einem bestimmten Typen.
	 *
	 * @param pidType Pid des Objekt-Typen, die für den DefaultParameter verwendet werden soll
	 * @param pidAtg  Pid der Attributgruppe, die für den DefaultParameter verwendet werden soll
	 */
	public ConfigurationDefaultParameter(final String pidType, final String pidAtg) {
		_pidType = pidType;
		_pidAtg = pidAtg;
	}

	/**
	 * Pid der Attributgruppe, die der DefaultParameter verwenden soll.
	 *
	 * @return Pid der Attributgruppe
	 */
	public String getPidAtg() {
		return _pidAtg;
	}

	/**
	 * Objekt-Typ der für den DefaultParameter verwendet werden soll.
	 *
	 * @return Pid des Objekt-Typen oder den Leerstring ""
	 */
	public String getPidType() {
		return _pidType;
	}

	/**
	 * Speichert Datum, Datenliste und Datenfeld in der Reihenfolge, wie diese eingelesen wurden. Das Array enthält Objekte der Klassen
	 * <code>ConfigurationData</code>, <code>ConfigurationDataList</code> und <code>ConfigurationDataField</code>.
	 *
	 * @return Ein Array mit oben genannten Objekten oder ein leeres Array, falls keine Objekte vorhanden sind
	 */
	public DatasetElement[] getDataAnddataListAndDataField() {
		return _dataAnddataListAndDataField;
	}

	/**
	 * @param dataAnddataListAndDataField Das Array enthält Objekte der Klassen <code>ConfigurationData</code>, <code>ConfigurationDataList</code> und
	 *                                    <code>ConfigurationDataField</code>. Die Reihenfolge der Elemente bleibt beim speichern erhalten. Sind keine Objekte
	 *                                    vorhanden, so ist das Array leer.
	 */
	public void setDataAndDataListAndDataField(DatasetElement[] dataAnddataListAndDataField) {
		_dataAnddataListAndDataField = dataAnddataListAndDataField;
	}
}

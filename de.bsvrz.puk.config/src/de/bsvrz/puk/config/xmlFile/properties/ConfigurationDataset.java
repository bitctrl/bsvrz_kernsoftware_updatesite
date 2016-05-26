/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Bildet einen "datensatz" ab, der nach der K2S.DTD definiert ist.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Syystemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationDataset implements ConfigurationObjectElements {

	/**
	 * Speichert Datum, Datenliste und Datenfeld in der Reihenfolge wie sie eingelesen wurden.
	 */
	private DatasetElement[] _dataAnddataListAndDataField = new DatasetElement[0];

	private final String _pidATG;

	private String _pidAspect = "asp.eigenschaften";

	/**
	 * Erzeugt ein Objekt "datensatz" (siehe K2S.DTD) mit einer bestimmten Attributgruppe und der festen Pid "asp.eigenschaften" für den Aspekt.
	 *
	 * @param pidATG Pid der Attributgruppe, die für den Datensatz verwendet werden soll
	 */
	public ConfigurationDataset(String pidATG) {
		_pidATG = pidATG;
	}

	/**
	 * Erzeugt ein Objekt "datensatz" (siehe K2S.DTD) mit einer bestimmten Attributgruppe und einem bestimmten Aspekt.
	 *
	 * @param pidATG	Attributgruppe, die für den Datensatz verwendet werden soll
	 * @param pidAspect Aspekt, der für den Datensatz verwendet werden soll. Wird "" übergeben, so wird der Default-Wert "asp.eigenschaften" benutzt.
	 */
	public ConfigurationDataset(String pidATG, String pidAspect) {
		_pidATG = pidATG;
		if (!"".equals(pidAspect)) {
			_pidAspect = pidAspect;
		}
	}

	/**
	 * Pid der Attributgruppe, den der Datensatz verwenden soll.
	 *
	 * @return Pid der Attributgruppe
	 */
	public String getPidATG() {
		return _pidATG;
	}

	/**
	 * Aspekt der für den Datensatz verwendet werden soll.
	 *
	 * @return Aspekt, der über den Konstruktor festegelegt wurde oder "asp.eigenschaften", falls kein Aspekt festgelegt wurde.
	 */
	public String getPidAspect() {
		return _pidAspect;
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

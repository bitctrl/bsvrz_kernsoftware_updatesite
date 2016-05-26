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

import de.bsvrz.sys.funclib.debug.Debug;


/**
 * Stellt eine "datenliste" dar, die nach der K2S.DTD definiert wurde.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationDataList implements DatasetElement {

	/**
	 * Speichert Datum, Datenliste und Datenfeld
	 */
	private final DatasetElement _dataAndDataListAndDataField[];

	private final String _name;

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();


	public ConfigurationDataList(DatasetElement[] dateAndDataListAndDataField, String name) {
		if (dateAndDataListAndDataField != null) {
			_dataAndDataListAndDataField = dateAndDataListAndDataField;
		} else {
			_dataAndDataListAndDataField = new DatasetElement[0];
		}

		if (name != null) {
			_name = name;
		} else {
			_name = "";
			_debug.warning("Einer Datenliste wurde null als Name zugewiesen");
		}
	}

	/**
	 * @return Array mit Objekten vom Typ <code>ConfigurationData</code>, <code>ConfigurationDataList</code> und
	 *         <code>ConfigurationDataField</code>
	 */
	public DatasetElement[] getDataAndDataListAndDataField() {
		return _dataAndDataListAndDataField;
	}

	/**
	 * Name des Objekts
	 *
	 * @return Name oder "" falls im Konstruktor <code>null</code> übergeben wurde
	 */
	public String getName() {
		return _name;
	}
}

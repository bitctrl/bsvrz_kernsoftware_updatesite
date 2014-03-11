/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Diese Klasse stellt ein "datenfeld" nach K2S.DTD dar.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationDataField implements DatasetElement {
	private final String _name;

	/**
	 * Speichert Objekte vom Type <code>ConfigurationData</code> und <code>ConfigurationDataList</code>
	 */
	private final DatasetElement _dataAndDataList[];

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();


	public ConfigurationDataField(String name, DatasetElement dateAndDataList[]) {
		if (name != null) {
			_name = name;
		} else {
			_name = "";
			_debug.warning("Einem Datenfeld wurde null als Name zugewiesen");
		}

		if (dateAndDataList != null) {
			_dataAndDataList = dateAndDataList;
		} else {
			_dataAndDataList = new DatasetElement[0];
		}
	}

	/**
	 * Name des Objekts
	 *
	 * @return Name oder "" falls im Konstruktor <code>null</code> übergeben wurde
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Array, das Objekte von Typ <code>ConfigurationData</code> und <code>ConfigurationDataList</code> enthält.
	 *
	 * @return Array mit Objekte, oder ein leeres Array
	 */
	public DatasetElement[] getDataAndDataList() {
		return _dataAndDataList;
	}
}

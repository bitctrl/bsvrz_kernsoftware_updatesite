/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;

/**
 * Interface zur Definition, wann historische Objekte und Mengenelemente gelöscht werden dürfen
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface MaintenanceSpec {

	/**
	 * Diese Methode gibt zurück, ob ein historisches dynamisches Objekt gelöscht werden darf. Es wurde bereits sichergestellt, dass das
	 * Objekt ungültig ist und nicht mehr referenziert wird.
	 *
	 * @param object Dynamisches Objekt
	 * @return true wenn es gelöscht werden farf, sonst false
	 */
	public boolean canDeleteObject(DynamicObjectInfo object);

	Long getSetKeepTime(ObjectSetType type);
}

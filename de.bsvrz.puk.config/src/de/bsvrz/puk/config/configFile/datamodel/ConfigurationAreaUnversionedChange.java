/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

import java.util.Arrays;

/**
 * Diese Klasse stellt die unversionierten Änderungen in eine KB-Version dar
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11583 $
 */
public class ConfigurationAreaUnversionedChange {

	/** Gibt die Version an, in der die unversionierten Änderungen aktiviert wurden/werden. */
	private final short _configurationAreaVersion;

	/**
	 * Gibt die Pids der in dieser uvnersionierten Änderung geänderten Attribut-Typen zurück.
	 */
	private final String[] _attributeTypePids;

	public ConfigurationAreaUnversionedChange(
			final short configurationAreaVersion, final String[] attributeTypePids) {
		_configurationAreaVersion = configurationAreaVersion;
		_attributeTypePids = attributeTypePids;
	}

	/** @return Version, in der die unversionierten Änderungen aktiviert wurden/werden. */
	public short getConfigurationAreaVersion() {
		return _configurationAreaVersion;
	}

	/** @return Pids der in dieser uvnersionierten Änderung geänderten Attribut-Typen. */
	public String[] getAttributeTypePids() {
		return _attributeTypePids;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(!(o instanceof ConfigurationAreaUnversionedChange)) return false;

		final ConfigurationAreaUnversionedChange that = (ConfigurationAreaUnversionedChange) o;

		if(_configurationAreaVersion != that._configurationAreaVersion) return false;
		if(!Arrays.equals(_attributeTypePids, that._attributeTypePids)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) _configurationAreaVersion;
		result = 31 * result + Arrays.hashCode(_attributeTypePids);
		return result;
	}

	@Override
	public String toString() {
		return "In Version " + _configurationAreaVersion +
				" wurden folgende Attributtypen durch eine unversionierte Datenmodelländerung geändert: " + Arrays.toString(_attributeTypePids) +
				'.';
	}
}

/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.communication;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.datamodel.AbstractConfigSystemObject;

import java.util.Collection;

/**
 * Diese Klasse imitiert ein Systemobjekt und wird bei der Kommunikation mit Anwendungen benutzt, die der lokalen Konfiguration nicht bekannt sind.
 *
 * @author Kappich Systemberatung
 * @version : 0000 $
 */
public class UnknownObject extends AbstractConfigSystemObject implements ClientApplication{

	private final long _id;

	/**
	 * Erstellt ein Dummy-Systemobjekt
	 * @param id Id
	 * @param configurationArea Konfigurationbereich (irgendeiner, wird nicht gebraucht)
	 */
	public UnknownObject(final long id, final ConfigurationArea configurationArea) {
		super(configurationArea);
		_id = id;
	}

	public long getId() {
		return _id;
	}

	public SystemObjectType getType() {
		return getConfigurationArea().getDataModel().getType("typ.applikation");
	}

	public String getPid() {
		return "";
	}

	public String getName() {
		return String.valueOf("Unbekannt{" + _id + "}");
	}

	public void setName(final String name) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public boolean isValid() {
		return false;
	}

	public void invalidate() throws ConfigurationChangeException {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public Data getConfigurationData(final AttributeGroup atg, final Aspect asp) {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public Data getConfigurationData(final AttributeGroupUsage atgUsage) {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public void setConfigurationData(final AttributeGroup atg, final Aspect asp, final Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public void setConfigurationData(final AttributeGroupUsage atgUsage, final Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		throw new UnsupportedOperationException("Nicht implementiert");
	}

	@Override
	public long getValidSince() {
		return 1;
	}

	@Override
	public long getNotValidSince() {
		return 1;
	}

	@Override
	public void addListenerForInvalidation(final InvalidationListener listener) {
	}

	@Override
	public void removeListenerForInvalidation(final InvalidationListener listener) {
	}

	@Override
	public void addConfigurationCommunicationChangeListener(final ConfigurationCommunicationChangeListener listener) {
	}

	@Override
	public void removeConfigurationCommunicationChangeListener(final ConfigurationCommunicationChangeListener listener) {
	}

	@Override
	public boolean isConfigurationCommunicationActive() {
		return false;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final UnknownObject that = (UnknownObject) o;

		if(_id != that._id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (_id ^ (_id >>> 32));
	}
}

/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;

/**
 * Diese abstrakte Klasse implementiert die Methoden des {@link SystemObject}-Interfaces, welche für die verschiedenen Implementierungen eines SystemObjekts
 * immer gleich bleiben.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class AbstractConfigSystemObject implements SystemObject {
	/**
	 * Der Konfigurationsbereich dieses SystemObjekts.
	 */
	private final ConfigConfigurationArea _configurationArea;

	/**
	 * Standardkonstruktor. Speichert den Konfigurationsbereich, zu dem dieses System-Objekt gehört.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses System-Objekts
	 */
	public AbstractConfigSystemObject(ConfigurationArea configurationArea) {
		_configurationArea = (ConfigConfigurationArea)configurationArea;
	}

	public String getNameOrPidOrId() {
		String result = getName();
		if (result.equals("")) result = getPid();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrNameOrId() {
		String result = getPid();
		if (result.equals("")) result = getName();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrId() {
		String result = getPid();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public ConfigConfigurationArea getConfigurationArea() {
		return _configurationArea;
	}

	public ConfigDataModel getDataModel() {
		return _configurationArea.getDataModel();
	}

	public SystemObjectInfo getInfo() {
		final Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.info"));
		if (data != null) {
			return new SystemObjectInfo(data.getTextValue("kurzinfo").getText(), data.getTextValue("beschreibung").getText());
		} else {
			return SystemObjectInfo.UNDEFINED;
		}
	}

	public Data getConfigurationData(AttributeGroup atg) {
		return getConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"));
	}

	public void setConfigurationData(AttributeGroup atg, Data data) throws ConfigurationChangeException {
		setConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"), data);
	}

	public boolean isOfType(String typePid) {
		return isOfType(getDataModel().getType(typePid));
	}

	public boolean isOfType(SystemObjectType type) {
		if (type.equals(getType())) return true;
		return getType().inheritsFrom(type);
	}

	/**
	 * Der Vergleich zweier SystemObjekte (o1, o) erfolgt durch deren ID.
	 *
	 * @param o zu vergleichendes SystemObjekt
	 * @return <code>-1</code>, falls o1.getId() < o.getId()<br> <code> 1</code>, falls o1.getId() > o.getId()<br> <code> 0</code>, falls o1.getId() == o.getId()
	 */
	public int compareTo(Object o) {
		SystemObject otherObject = (SystemObject) o;
		if (getId() < otherObject.getId()) return -1;
		if (getId() > otherObject.getId()) return 1;
		return 0;
	}

	/**
	 * Gibt die String-Repräsentation dieses SystemObjekts zurück.
	 *
	 * @return die String-Repräsentation dieses SystemObjekts
	 */
	public String toString() {
		final StringBuilder text = new StringBuilder();
		final String name = getName();
		final String pid = getPid();
		if(name != null && !name.equals("")) {
			text.append(name).append(" ");
		}
		if(pid != null && !pid.equals("")) {
			text.append("(").append(pid).append(")");
		}
		else {
			text.append("[").append(getId()).append("]");
		}
		return text.toString();
	}
}

/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaDependency;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaUnversionedChange;

import java.util.*;

/**
 * Konfigurationsbereich, der aus einer XML-Versorgungsdatei erzeugt wurde. Das Objekt enth�lt den Konfigurationsverantwortlichen und die eingelesenen Objekte.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 11583 $ / $Date: 2013-08-22 15:59:25 +0200 (Thu, 22 Aug 2013) $ / ($Author: jh $)
 */
public class ConfigurationAreaProperties extends ConfigurationObjectProperties {

	private String _authority;

	private List<SystemObjectProperties> _objectProperties;

	/** Speichert alle gemachten Konfigurations�nderungen. Sind keine vorhanden, so ist die Gr��e des Arrays 0. */
	private ConfigurationAreaChangeInformation[] _configurationAreaChangeInformation = new ConfigurationAreaChangeInformation[0];

	/** Alle Abh�ngigkeiten des Bereichs oder <code>null</code>, falls f�r diesen Bereich noch keine Abh�ngigkeiten gepr�ft wurden. */
	private final Collection<ConfigurationAreaDependency> _areaDependencies;
	private Collection<ConfigurationAreaUnversionedChange> _unversionedChanges;

	/**
	 * @param name
	 * @param pid
	 * @param id
	 * @param authorityPid
	 * @param info
	 * @param objectProperties
	 */
	public ConfigurationAreaProperties(
			String name, String pid, long id, String authorityPid, SystemObjectInfo info, List<SystemObjectProperties> objectProperties
	) {
		this(name, pid, id, authorityPid, info, objectProperties, new LinkedList<ConfigurationAreaDependency>());
	}


	/**
	 * @param name
	 * @param pid
	 * @param id
	 * @param authorityPid
	 * @param info
	 * @param objectProperties
	 * @param areaDependencies  Abh�ngigkeiten des Konfigurationsbereichs zu anderen Bereichen. Wurden die Abh�ngigkeiten noch nicht gepr�ft, so wird
	 *                         <code>null</code> �bergeben.
	 */
	public ConfigurationAreaProperties(
			String name,
			String pid,
			long id,
			String authorityPid,
			SystemObjectInfo info,
			List<SystemObjectProperties> objectProperties,
			Collection<ConfigurationAreaDependency> areaDependencies) {
		super(name, pid, id, "typ.konfigurationsBereich", info);
		_authority = authorityPid;
		_objectProperties = objectProperties;
		_areaDependencies = areaDependencies;
	}

	/** @return Elemente "konfigurationsAenderung", siehe K2S.DTD. Sind keine Elemente vorhanden, so besitzt das Array die Gr��e 0. */
	public ConfigurationAreaChangeInformation[] getConfigurationAreaChangeInformation() {
		return _configurationAreaChangeInformation;
	}

	/**
	 * @param configurationAreaChangeInformation
	 *         Elemente "konfigurationsAenderung", siehe K2S.DTD
	 */
	public void setConfigurationAreaChangeInformation(ConfigurationAreaChangeInformation[] configurationAreaChangeInformation) {
		_configurationAreaChangeInformation = configurationAreaChangeInformation;
	}

	/**
	 * Objekte, die aus einer XML-Versogungsdatei eingelesen wurde.
	 *
	 * @return Liste mit Objekte oder eine leere Liste
	 */
	public List<SystemObjectProperties> getObjectProperties() {
		return _objectProperties;
	}

	/**
	 * @param objectProperties Liste von Objekten, die aus einer XML-Versorgungsdatei eingelesen wurden. Sind keine Objekte vorhanden, so wird eine leere Liste
	 *                         �bergeben
	 */
	public void setObjectProperties(List<SystemObjectProperties> objectProperties) {
		_objectProperties = objectProperties;
	}

	/**
	 * Konfigurationsverantwortlicher
	 *
	 * @return s.o.
	 */
	public String getAuthority() {
		return _authority;
	}


	/**
	 * Gibt alle Abh�ngigkeiten des Bereichs zur�ck, die im Konsturktor �bergeben wurden.
	 *
	 * @return Alle Abh�ngigkeiten, wurden die Abh�ngigkeiten noch nicht gepr�ft, so wird <code>null</code> zur�ckgegeben.
	 */
	public Collection<ConfigurationAreaDependency> getAreaDependencies() {
		return _areaDependencies;
	}

	/**
	 * Gibt alle unversionierten �nderungen des Konfigurationsbereichs zur�ck
	 * @return unversionierte �nderungen
	 */
	public Collection<ConfigurationAreaUnversionedChange> getUnversionedChanges() {
		return _unversionedChanges;
	}

	/**
	 * Setzt alle unversionierte �nderungen des Konfigurationsbereichs
	 * @param unversionedChanges alle unversionierte �nderungen
	 */
	public void setUnversionedChanges(final Collection<ConfigurationAreaUnversionedChange> unversionedChanges) {
		_unversionedChanges = unversionedChanges;
	}

	protected StringBuilder appendParamString(StringBuilder stringBuilder) {
		super.appendParamString(stringBuilder).append(", authority:").append(getAuthority());
		stringBuilder.append(", numberOfIncludedObjects:").append(_objectProperties.size());
		stringBuilder.append('[');
		for(Iterator<SystemObjectProperties> iterator = _objectProperties.iterator(); iterator.hasNext();) {
			stringBuilder.append('\n');
			SystemObjectProperties systemObjectProperties = iterator.next();
			stringBuilder.append(systemObjectProperties);
		}
		stringBuilder.append("\n]");
		return stringBuilder;
	}
}

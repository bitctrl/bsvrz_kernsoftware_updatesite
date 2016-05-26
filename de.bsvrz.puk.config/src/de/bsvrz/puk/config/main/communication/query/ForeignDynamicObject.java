/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Klasse, die für Systemobjekte verwendet wird, die von einer fremden Konfiguration angefordert wurden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ForeignDynamicObject implements DynamicObject {
	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final ForeignConfigRequester _foreignConfigRequester;

	private final DataModel _dataModel;

	private final long _id;

	private final long _typeId;

	private final String _pid;

	private String _name;

	private final boolean _valid;

	private final long _validSince;

	private long _notValidSince;

	private final long _configAreaId;

	private int _waitingDataCount = -1;

	private HashMap<AttributeGroupUsage, Data> _configDatas = new HashMap<AttributeGroupUsage, Data>();

	public ForeignDynamicObject(
			final ForeignConfigRequester foreignConfigRequester, DataModel dataModel, long id, long typeId, String pid, String name, boolean valid, long validSince, long notValidSince,
			long configAreaId) {
		_foreignConfigRequester = foreignConfigRequester;
		_dataModel = dataModel;
		_id = id;
		_typeId = typeId;
		_pid = pid;
		_name = name;
		_valid = valid;
		_validSince = validSince;
		_notValidSince = notValidSince;
		_configAreaId = configAreaId;
		if(getType() ==  null) {
			_debug.warning("Der Typ des fremden dynamischen Objekts ist der lokalen Konfiguration nicht bekannt", this);
		}
		if(getConfigurationArea() == null) {
			_debug.warning("Der Konfigurationsbereich (id: " + _configAreaId + ") des fremden dynamischen Objekts ist der lokalen Konfiguration nicht bekannt", this);
		}
	}

	public long getValidSince() {
		return _validSince;
	}

	public long getNotValidSince() {
		return _notValidSince;
	}

	public void setNotValidSince(final long notValidSince) {
		_notValidSince = notValidSince;
	}

	public void addListenerForInvalidation(InvalidationListener listener) {
		throw new UnsupportedOperationException("Noch nicht implementiert.");
	}

	public void removeListenerForInvalidation(InvalidationListener listener) {
		throw new UnsupportedOperationException("Noch nicht implementiert.");
	}

	public long getId() {
		return _id;
	}

	public SystemObjectType getType() {
		return (SystemObjectType)getDataModel().getObject(_typeId);
	}

	public boolean isOfType(SystemObjectType type) {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public boolean isOfType(String typePid) {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public String getPid() {
		return (_pid == null ? "" : _pid);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getNameOrPidOrId() {
		String result = getName();
		if(result == null || result.equals("")) result = getPid();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrNameOrId() {
		String result = getPid();
		if(result == null || result.equals("")) result = getName();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrId() {
		String result = getPid();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	public boolean isValid() {
		return _valid;
	}

	public void invalidate() {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public DataModel getDataModel() {
		return _dataModel;
	}

	public Data getConfigurationData(AttributeGroup atg) {
		return getConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"));
	}

	public Data getConfigurationData(AttributeGroup atg, Aspect asp) {
		return getConfigurationData(atg.getAttributeGroupUsage(asp));
	}

	public Data getConfigurationData(AttributeGroupUsage atgUsage) {
		return _configDatas.get(atgUsage);
	}

	public void setConfigurationData(AttributeGroup atg, Data data) {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public void setConfigurationData(AttributeGroup atg, Aspect asp, Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		ArrayList<AttributeGroupUsage> attributeGroupUsages = new ArrayList<AttributeGroupUsage>();
		for(Map.Entry<AttributeGroupUsage, Data> attributeGroupUsageDataEntry : _configDatas.entrySet()) {
			Data value = attributeGroupUsageDataEntry.getValue();
			AttributeGroupUsage key = attributeGroupUsageDataEntry.getKey();
			if(value != null && key != null) {
				attributeGroupUsages.add(key);
			}
		}
		return Collections.unmodifiableCollection(attributeGroupUsages);
	}

	public SystemObjectInfo getInfo() {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public ConfigurationArea getConfigurationArea() {
		return (ConfigurationArea)getDataModel().getObject(_configAreaId);
	}

	public int compareTo(Object o) {
		SystemObject other = (SystemObject)o;
		if(getId() < other.getId()) return -1;
		if(getId() > other.getId()) return 1;
		return 0;
	}

	public String toString() {
		return getTypeString() + "{" + getParamString() + "}";
	}

	protected String getParamString() {
		String typeName;
		try {
			typeName = getType().getNameOrPidOrId();
		}
		catch(Exception e) {
			typeName = "<error " + e + ">";
		}
		return "name: '" + getName() + "'" + ", pid: '" + getPid() + "'" + ", id: '" + getId() + "'" + ", typ: '" + typeName + "'";
	}

	protected String getTypeString() {
		return getClass().getName();
	}

	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		throw new UnsupportedOperationException("addConfigurationCommunicationChangeListener nicht implementiert");
	}

	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		throw new UnsupportedOperationException("removeConfigurationCommunicationChangeListener nicht implementiert");
	}

	public boolean isConfigurationCommunicationActive() {
		throw new UnsupportedOperationException("isConfigurationCommunicationActive nicht implementiert");
	}

	int getWaitingDataCount() {
		return _waitingDataCount;
	}

	void setWaitingDataCount(final int waitingDataCount) {
		this._waitingDataCount = waitingDataCount;
	}

	public boolean saveConfigurationData(final AttributeGroupUsage attributeGroupUsage, final Data configData) {
		_configDatas.put(attributeGroupUsage, configData);
		return --_waitingDataCount <= 0;
	}

	public ForeignConfigRequester getForeignConfigRequester() {
		return _foreignConfigRequester;
	}
}

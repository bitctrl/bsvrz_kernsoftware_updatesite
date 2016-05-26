/*
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;

/**
 * Diese Klasse stellt ein Objekt zur Verfügung, das einen Aspekt laut K2S.DTD darstellt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationAspect {
	private final String _pid;

	AttributeGroupUsage.Usage _usage = null;

	private SystemObjectInfo _info;

	/**
	 * @return Modus oder <code>null</code>, falls kein Modus gesetzt wurde
	 */
	public AttributeGroupUsage.Usage getUsage() {
		return _usage;
	}

	public void setUsage(AttributeGroupUsage.Usage usage) {
		_usage = usage;
	}

	/**
	 * konfigurationsModus oder onlineModus
	 * @param usage Modus
	 */
	public void setUsage(String usage) {
		if (AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData;
		} else if (AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData;
		} else if (AttributeGroupUsage.Usage.OnlineDataAsSenderDrain.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.OnlineDataAsSenderDrain;
		} else if (AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver;
		} else if (AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain;
		} else if (AttributeGroupUsage.Usage.OptionalConfigurationData.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.OptionalConfigurationData;
		} else if (AttributeGroupUsage.Usage.RequiredConfigurationData.getValue().equals(usage)) {
			_usage = AttributeGroupUsage.Usage.RequiredConfigurationData;
		}
	}

	/**
	 *
	 * @param pid Pid des Objekts
	 */
	public ConfigurationAspect(String pid) {
		_pid = pid;
	}

	/**
	 *
	 * @param info Info, die zu dem Objekt gehört
	 */
	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	/**
	 *
	 * @return Pid, die zu diesem Objekt gehört
	 */
	public String getPid() {
		return _pid;
	}

	/**
	 *
	 * @return Info, die zu diesem Objekt gehört
	 */
	public SystemObjectInfo getInfo() {
		return _info;
	}
}

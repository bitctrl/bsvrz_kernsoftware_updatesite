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
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

/**
 * Implementierung der Attributgruppenverwendung auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAttributeGroupUsage extends ConfigConfigurationObject implements AttributeGroupUsage, AttributeGroupUsageIdentifier {

	/**
	 * Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler. Der Wert <code>0</code> bedeutet, dass die Identifizierung
	 * noch nicht berechnet wurde.
	 *
	 * @see #getIdentificationForDav
	 */
	private long _identificationForDav = 0;

	/** Cache für Usage */
	private Usage _usage;

	/**
	 * Konstruktor einer Attributgruppenverwendung.
	 *
	 * @param configurationArea der Konfigurationsbereich einer Attributgruppenverwendung
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen der Attributgruppenverwendung
	 */
	public ConfigAttributeGroupUsage(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public AttributeGroup getAttributeGroup() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.attributgruppenVerwendung"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			return (AttributeGroup)data.getReferenceValue("Attributgruppe").getSystemObject();
		}
		else {
			throw new IllegalStateException("Attributgruppe der Attributgruppenverwendung " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
	}

	public Aspect getAspect() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.attributgruppenVerwendung"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			return (Aspect)data.getReferenceValue("Aspekt").getSystemObject();
		}
		else {
			throw new IllegalStateException("Aspekt an der Attributgruppenverwendung " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
	}

	public boolean isConfigurating() {
		switch(getUsage()) {
			case RequiredConfigurationData:
			case ChangeableRequiredConfigurationData:
			case OptionalConfigurationData:
			case ChangeableOptionalConfigurationData:
				return true;
			default:
				return false;
		}
	}

	public boolean isExplicitDefined() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.attributgruppenVerwendung"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			return (data.getUnscaledValue("VerwendungExplizitVorgegeben").intValue() == 1);
		}
		else {
			throw new IllegalStateException(
					"Für die Attributgruppenverwendung " + getNameOrPidOrId()
					+ " konnte nicht ermittelt werden, ob die Verwendung explizit vorgegeben wurde oder ob sie sich implizit aus der Hierarchie der Parameter ergeben hat."
			);
		}
	}

	public synchronized Usage getUsage() {
		Usage result = _usage;
		if(result == null) {
			Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.attributgruppenVerwendung"), getDataModel().getAspect("asp.eigenschaften"));
			if(data != null) {
				int usage = data.getUnscaledValue("DatensatzVerwendung").intValue();
				switch(usage) {
					case 1:
						result = Usage.RequiredConfigurationData;
						break;
					case 2:
						result = Usage.ChangeableRequiredConfigurationData;
						break;
					case 3:
						result = Usage.OptionalConfigurationData;
						break;
					case 4:
						result = Usage.ChangeableOptionalConfigurationData;
						break;
					case 5:
						result = Usage.OnlineDataAsSourceReceiver;
						break;
					case 6:
						result = Usage.OnlineDataAsSenderDrain;
						break;
					case 7:
						result = Usage.OnlineDataAsSourceReceiverOrSenderDrain;
						break;
				}
			}
		}
		if(result == null) {
			throw new IllegalStateException("Verwendungsmöglichkeit der Attributgruppenverwendung " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
		else {
			_usage = result;
		}
		return result;
	}

	/**
	 * Bestimmt die Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler. Bei der ersten Verwendung dieser Methode wird
	 * die Identifizierung berechnet und für weitere Aufrufe zwischengespeichert.
	 *
	 * @return Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler
	 *
	 * @see #calculateDavIdentification()
	 */
	public long getIdentificationForDav() {
		if(_identificationForDav == 0) {
			_identificationForDav = calculateDavIdentification();
		}
		return _identificationForDav;
	}

	/**
	 * Bestimmt die Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler. Die Identifizierung dieser
	 * Attributgruppenverwendung hat für bestimmte vordefinierte Attributgruppen-Aspekt-Kombinationen, die zur Kommunikation zwischen
	 * Datenverteiler-Applikationsfunktionen und Konfiguration benutzt werden, feste vordefinierte Werte und entspricht in allen anderen Fällen
	 * der Objekt-Id dieser Attributgruppenverwendung.
	 *
	 * @return Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler.
	 *
	 * @see #getId()
	 */
	private long calculateDavIdentification() {
		if(isValid()) {
			if(getAspect().getPid().equals("asp.anfrage")) {
				if(getAttributeGroup().getPid().equals("atg.konfigurationsAnfrage")) {
					return AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST;
				}
				else if(getAttributeGroup().getPid().equals("atg.konfigurationsSchreibAnfrage")) {
					return AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST;
				}
			}
			else if(getAspect().getPid().equals("asp.antwort")) {
				if(getAttributeGroup().getPid().equals("atg.konfigurationsAntwort")) {
					return AttributeGroupUsageIdentifications.CONFIGURATION_READ_REPLY;
				}
				else if(getAttributeGroup().getPid().equals("atg.konfigurationsSchreibAntwort")) {
					return AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REPLY;
				}
			}
		}
		return getId();
	}

	@Override
	void invalidateCache() {
		_usage = null;
		super.invalidateCache();
	}
}

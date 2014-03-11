/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsageIdentifier;

/**
 * Implementierung der Attributgruppenverwendung auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigAttributeGroupUsage extends ConfigConfigurationObject implements AttributeGroupUsage, AttributeGroupUsageIdentifier {

	/**
	 * Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler. Der Wert <code>0</code> bedeutet, dass die Identifizierung
	 * noch nicht berechnet wurde.
	 *
	 * @see #getIdentificationForDav
	 */
	private long _identificationForDav = 0;

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

	public Usage getUsage() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.attributgruppenVerwendung"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			int usage = data.getUnscaledValue("DatensatzVerwendung").intValue();
			switch(usage) {
				case 1:
					return Usage.RequiredConfigurationData;
				case 2:
					return Usage.ChangeableRequiredConfigurationData;
				case 3:
					return Usage.OptionalConfigurationData;
				case 4:
					return Usage.ChangeableOptionalConfigurationData;
				case 5:
					return Usage.OnlineDataAsSourceReceiver;
				case 6:
					return Usage.OnlineDataAsSenderDrain;
				case 7:
					return Usage.OnlineDataAsSourceReceiverOrSenderDrain;
			}
		}
		throw new IllegalStateException("Verwendungsmöglichkeit der Attributgruppenverwendung " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
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
}

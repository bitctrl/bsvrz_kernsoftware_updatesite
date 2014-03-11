/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl.config;

/**
 * Enthält Konstanten für die Verwendung von speziellen AttributgruppenVerwendungen.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5060 $, $Date: 2007-09-01 15:04:35 +0200 (Sa, 01 Sep 2007) $, $Author: rs $
 */
public class AttributeGroupUsageIdentifications {

	/** AttributgruppenVerwendung zum Speichern von Mengen an System-Objekten. */
	public static final long CONFIGURATION_SETS = -1;

	/** AttributgruppenVerwendung zum Speichern von Elementen in nicht dynamischen Mengen. */
	public static final long CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET = -2;

	/** AttributgruppenVerwendung zum Speichern von Elementen in dynamischen Mengen. */
	public static final long CONFIGURATION_ELEMENTS_IN_MUTABLE_SET = -3;

	/** AttributgruppenVerwendung für Konfigurationsleseanfragen. */
	public static final long CONFIGURATION_READ_REQUEST = -10;

	/** AttributgruppenVerwendung für Antworten auf Konfigurationsanfragen. */
	public static final long CONFIGURATION_READ_REPLY = -11;

	/** AttributgruppenVerwendung für Konfigurationsschreibanfragen. */
	public static final long CONFIGURATION_WRITE_REQUEST = -12;

	/** AttributgruppenVerwendung für Antworten auf Konfigurationsschreibanfragen. */
	public static final long CONFIGURATION_WRITE_REPLY = -13;

	/**
	 * Prüft ob die Identifikation einer Attributgruppenverwendung für Anfragen an die Konfiguration benutzt wird.
	 *
	 * @param usageIdentification Identifikation einer Attributgruppenverwendung
	 *
	 * @return <code>true</code>, falls die Identifikation für Anfragen an die Konfiguration benutzt wird, sonst <code>false</code>.
	 */
	public static boolean isConfigurationRequest(long usageIdentification) {
		final boolean isConfigurationRequest = usageIdentification == CONFIGURATION_READ_REQUEST || usageIdentification == CONFIGURATION_WRITE_REQUEST;
		return isConfigurationRequest;
	}

	/**
	 * Prüft ob die Identifikation einer Attributgruppenverwendung für Antworten auf Konfigurationsanfragen benutzt wird.
	 *
	 * @param usageIdentification Identifikation einer Attributgruppenverwendung
	 *
	 * @return <code>true</code>, falls die Identifikation für Antworten auf Konfigurationsanfragen benutzt wird, sonst <code>false</code>.
	 */
	public static boolean isConfigurationReply(long usageIdentification) {
		final boolean isConfigurationReply = usageIdentification == CONFIGURATION_READ_REPLY || usageIdentification == CONFIGURATION_WRITE_REPLY;
		return isConfigurationReply;
	}

	/**
	 * Prüft ob die Identifikation einer Attributgruppenverwendung für Konfigurationsanfragen oder Antworten auf Konfigurationsanfragen benutzt wird.
	 *
	 * @param usageIdentification Identifikation einer Attributgruppenverwendung
	 *
	 * @return <code>true</code>, falls die Identifikation für Konfigurationsanfragen oder Antworten auf Konfigurationsanfragen benutzt wird, sonst
	 *         <code>false</code>.
	 */
	public static boolean isUsedForConfigurationRequests(long usageIdentification) {
		final boolean isConfigurationRequest = usageIdentification <= CONFIGURATION_READ_REQUEST && usageIdentification >= CONFIGURATION_WRITE_REPLY;
		return isConfigurationRequest;
	}
}

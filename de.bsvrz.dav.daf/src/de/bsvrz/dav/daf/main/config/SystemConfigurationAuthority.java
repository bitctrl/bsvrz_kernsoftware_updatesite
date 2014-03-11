/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstellenklasse zum Zugriff auf globale Eigenschaften des Systems. Von diesem Typ gibt es ein Objekt,
 * das zum einen die Rolle des Konfigurationsverantwortlichen f�r systemspezifische Konfigurationen �bernimmt
 * und zum anderen den Zugriff auf systemweite Eigenschaften erm�glicht. Dazu geh�ren Vorgaben f�r alle
 * Datenverteiler wie z.B. die Priorit�ten der verschiedenen Telegrammklassen.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5762 $ / $Date: 2008-01-05 03:25:57 +0100 (Sa, 05 Jan 2008) $ / ($Author: rs $)
 * @deprecated Dieses Interface wird mit einem der n�chsten Releases gel�scht 
 */
@Deprecated
public interface SystemConfigurationAuthority extends ConfigurationAuthority {
	/**
	 * Bestimmt die Priorit�t von Systemtelegrammen des Datenverteilers.
	 *
	 * @return Priorit�t von Systemtelegrammen.
	 */
	public byte getSystemTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von Konfigurationstelegrammen. Diese Priorit�t wird vom Segment Datenverteiler bei
	 * Telegrammen mit Konfigurationsanfragen und den dazu geh�renden Konfigurationsantworten benutzt.
	 *
	 * @return Priorit�t von Konfigurationstelegrammen.
	 */
	public byte getConfigurationDataTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von Online-Datentelegrammen. Diese Priorit�t wird vom Segment Datenverteiler bei
	 * Online-Datentelegrammen benutzt.
	 *
	 * @return Priorit�t von Online-Datentelegrammen.
	 */
	public byte getOnlineDataTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von Datentelegrammen mit nachgelieferten Daten.
	 *
	 * @return Priorit�t von Datentelegrammen mit nachgelieferten Daten.
	 */
	public byte getDelayedDataTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von simulierten Datentelegrammen. Diese Priorit�t wird vom Segment Datenverteiler
	 * bei Datentelegrammen, bei denen die Simulationsvariante nicht gleich 0 ist, benutzt.
	 *
	 * @return Priorit�t von simulierten Datentelegrammen.
	 */
	public byte getSimulationDataTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von Archivtelegrammen. Diese Priorit�t wird vom Segment Datenverteiler bei
	 * Telegrammen mit Archivanfragen und den dazu geh�renden Antworten aus dem Archiv benutzt.
	 *
	 * @return Priorit�t von Archivtelegrammen.
	 */
	public byte getArchiveDataTelegramPriority();

	/**
	 * Bestimmt die Priorit�t von Protokoll- und Auswertungstelegrammen. Diese Priorit�t wird vom Segment
	 * Datenverteiler bei Telegrammen mit Protokoll- und Auswertungsanfragen und den dazu geh�renden Antworten
	 * benutzt.
	 *
	 * @return Priorit�t von Protokoll- und Auswertungstelegrammen.
	 */
	public byte getReportDataTelegramPriority();
}

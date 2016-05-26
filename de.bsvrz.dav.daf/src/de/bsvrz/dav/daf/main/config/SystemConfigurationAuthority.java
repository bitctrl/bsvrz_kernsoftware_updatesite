/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstellenklasse zum Zugriff auf globale Eigenschaften des Systems. Von diesem Typ gibt es ein Objekt,
 * das zum einen die Rolle des Konfigurationsverantwortlichen für systemspezifische Konfigurationen übernimmt
 * und zum anderen den Zugriff auf systemweite Eigenschaften ermöglicht. Dazu gehören Vorgaben für alle
 * Datenverteiler wie z.B. die Prioritäten der verschiedenen Telegrammklassen.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 * @deprecated Dieses Interface wird mit einem der nächsten Releases gelöscht 
 */
@Deprecated
public interface SystemConfigurationAuthority extends ConfigurationAuthority {
	/**
	 * Bestimmt die Priorität von Systemtelegrammen des Datenverteilers.
	 *
	 * @return Priorität von Systemtelegrammen.
	 */
	public byte getSystemTelegramPriority();

	/**
	 * Bestimmt die Priorität von Konfigurationstelegrammen. Diese Priorität wird vom Segment Datenverteiler bei
	 * Telegrammen mit Konfigurationsanfragen und den dazu gehörenden Konfigurationsantworten benutzt.
	 *
	 * @return Priorität von Konfigurationstelegrammen.
	 */
	public byte getConfigurationDataTelegramPriority();

	/**
	 * Bestimmt die Priorität von Online-Datentelegrammen. Diese Priorität wird vom Segment Datenverteiler bei
	 * Online-Datentelegrammen benutzt.
	 *
	 * @return Priorität von Online-Datentelegrammen.
	 */
	public byte getOnlineDataTelegramPriority();

	/**
	 * Bestimmt die Priorität von Datentelegrammen mit nachgelieferten Daten.
	 *
	 * @return Priorität von Datentelegrammen mit nachgelieferten Daten.
	 */
	public byte getDelayedDataTelegramPriority();

	/**
	 * Bestimmt die Priorität von simulierten Datentelegrammen. Diese Priorität wird vom Segment Datenverteiler
	 * bei Datentelegrammen, bei denen die Simulationsvariante nicht gleich 0 ist, benutzt.
	 *
	 * @return Priorität von simulierten Datentelegrammen.
	 */
	public byte getSimulationDataTelegramPriority();

	/**
	 * Bestimmt die Priorität von Archivtelegrammen. Diese Priorität wird vom Segment Datenverteiler bei
	 * Telegrammen mit Archivanfragen und den dazu gehörenden Antworten aus dem Archiv benutzt.
	 *
	 * @return Priorität von Archivtelegrammen.
	 */
	public byte getArchiveDataTelegramPriority();

	/**
	 * Bestimmt die Priorität von Protokoll- und Auswertungstelegrammen. Diese Priorität wird vom Segment
	 * Datenverteiler bei Telegrammen mit Protokoll- und Auswertungsanfragen und den dazu gehörenden Antworten
	 * benutzt.
	 *
	 * @return Priorität von Protokoll- und Auswertungstelegrammen.
	 */
	public byte getReportDataTelegramPriority();
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsobjektes. Konfigurationsobjekte haben neben den Eigenschaften aller System-Objekte eine
 * verantwortliche Instanz (Zust�ndiger), eine Version der Konfiguration ab der das Objekt g�ltig ist und eine Version der Konfiguration ab der das Objekt nicht
 * mehr g�ltig ist.<BR/> Die verantwortliche Instanz macht eine Aussage dar�ber in welcher Umgebung Konfigurations�nderungen f�r das Objekt durchgef�hrt werden
 * k�nnen. Dies ist f�r Anwendungsobjekte �blicherweise eine Verkehrsrechnerzentrale oder eine Unterzentrale und f�r Objekte des Datenmodells eine globale
 * Instanz.<BR/> In einer Konfiguration k�nnen Konfigurationsobjekte von verschiedenen verantwortlichen Instanzen gespeichert werden. Jede Konfiguration
 * verwaltet f�r jede verantwortliche Instanz zu der sie Objekte gespeichert hat eine lokal aktivierte Versionsnummer die nicht unbedingt mit der in der
 * verantwortlichen Instanz aktivierten Versionsnummer �bereinstimmen muss. Beispielsweise k�nnen in der Konfiguration einer VRZ die Konfigurationsobjekte einer
 * UZ �bernommen werden. Die VRZ verwaltet dann f�r die �bernommene Konfiguration eine lokale Versionsnummer.<BR/> Die Versionsnummern ab der ein
 * Konfigurationsobjekt g�ltig bzw. nicht mehr g�ltig ist bezieht sich immer auf die lokal aktivierte Versionsnummer der verantwortlichen Instanz des jeweiligen
 * Objekts. Wenn in der VRZ die lokal aktivierte Version der UZ-Konfiguration kleiner als die in der UZ aktivierten Version ist, dann werden z.B. die neusten
 * Objekte der UZ in der VRZ als noch nicht g�ltig angesehen, obwohl sie in der UZ schon g�ltig sind.<BR/> Die Schnittstelle bietet au�erdem M�glichkeiten, um
 * auf die einem Konfigurationsobjekt zugeordneten benannten Objekt-Mengen zuzugreifen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public interface ConfigurationObject extends SystemObject {

	/**
	 * Liefert die Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt g�ltig ist.
	 *
	 * @return Version ab der das Objekt g�ltig ist.
	 */
	public short getValidSince();

	/**
	 * Liefert die Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt nicht mehr g�ltig ist.
	 *
	 * @return Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt nicht mehr g�ltig ist. Wenn das Objekt noch nicht mit der Methode {@link
	 *         SystemObject#invalidate} ung�ltig gemacht worden ist, dann wird der Wert <code>0</code> zur�ckgegeben.
	 */
	public short getNotValidSince();

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete nicht online �nderbare Menge zur�ck.
	 *
	 * @param name Der Name der gew�nschten Menge
	 *
	 * @return Gew�nschte Konfigurationsmenge oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public NonMutableSet getNonMutableSet(String name);

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete online �nderbare Menge zur�ck.
	 *
	 * @param name Der Name der gew�nschten Menge
	 *
	 * @return Gew�nschte dynamische Menge oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public MutableSet getMutableSet(String name);

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete Objekt-Menge zur�ck. Wenn die spezifizierte Objekt-Menge nicht online �nderbar ist, dann unterst�tzt
	 * die zur�ckgegebene Menge die {@link NonMutableSet Schnittstelle f�r nicht online �nderbare Mengen}. Wenn die Menge online �nderbar ist, dann unterst�tzt das
	 * zur�ckgegebene Mengenobjekt die {@link MutableSet Schnittstelle f�r online �nderbare Mengen}.
	 *
	 * @param name Der Name der gew�nschten Menge
	 *
	 * @return Menge von System-Objekten oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public ObjectSet getObjectSet(String name);

	/**
	 * Liefert die Liste aller diesem Konfigurationsobjekt zugeordneten Mengen zur�ck.
	 *
	 * @return Liste aller Mengen dieses Objekts. Die in der Liste enthaltenen Mengen implementieren je nach Art entweder die {@link NonMutableSet Schnittstelle
	 *         f�r nicht online �nderbare Mengen} oder die {@link MutableSet Schnittstelle f�r online �nderbare Mengen}.
	 */
	public List<ObjectSet> getObjectSets();

	/**
	 * Macht ein bereits als ung�ltig markiertes Objekt wieder g�ltig. Wenn ein Konfigurationsobjekt mit der Methode {@link SystemObject#invalidate} f�r eine
	 * zuk�nftige Konfigurationsversion als ung�ltig markiert wurde und diese Konfigurationsversion noch nicht aktiviert wurde, dann kann das Objekt durch Aufruf
	 * dieser Methode wieder g�ltig gemacht werden.
	 *
	 * @throws ConfigurationChangeException Wenn das Objektes nicht wieder g�ltig gemacht werden konnte.
	 */
	public void revalidate() throws ConfigurationChangeException;

	/**
	 * Dupliziert ein Konfigurationsobjekt. Es ist zu beachten, dass Komponenten nicht isoliert dupliziert werden k�nnen, sondern im Sinne der Komposition immer
	 * nur ganze Objekt-Einheiten zusammen dupliziert werden k�nnen, d.h. ausgehend von einem freien Objekt wird das Objekt mit all seinen Komponenten rekursiv
	 * dupliziert. Das Duplikat wird mit Aktivierung der in Bearbeitung befindlichen neuen Version des jeweiligen Konfigurationsbereichs g�ltig. Da die Pids gleich
	 * bleiben, muss zuvor das "alte" Objekt {@link #invalidate() gel�scht} werden.
	 *
	 * @return Das Duplikat dieses Konfigurationsobjekts.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @see #duplicate(java.util.Map)
	 */
	public SystemObject duplicate() throws ConfigurationChangeException;

	/**
	 * Dupliziert ein Konfigurationsobjekt. Es ist zu beachten, dass Komponenten nicht isoliert dupliziert werden k�nnen, sondern im Sinne der Komposition immer
	 * nur ganze Objekt-Einheiten zusammen dupliziert werden k�nnen, d.h. ausgehend von einem freien Objekt wird das Objekt mit all seinen Komponenten rekursiv
	 * dupliziert. Das Duplikat wird mit Aktivierung der in Bearbeitung befindlichen neuen Version des jeweiligen Konfigurationsbereichs g�ltig.
	 * <p/>
	 * Zu ersetzende Pids der Komponenten k�nnen in der Map (altePid, neuePid) �bergeben werden. Nicht ersetzte Pids, deren Objekte aber durch Komposition an die
	 * Objekt-Einheit gebunden sind, m�ssen vor Aktivierung {@link #invalidate() gel�scht} werden.
	 *
	 * @param substitudePids Map, die die Wert-Paare (altePid, neuePid) enth�lt.
	 *
	 * @return Das Duplikat dieses Konfigurationsobjekts.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @see #duplicate()
	 */
	public SystemObject duplicate(Map<String, String> substitudePids) throws ConfigurationChangeException;

	/**
	 * Ordnet dem Konfigurationsobjekt eine weitere Menge zu. Die Zuordnung wird erst mit der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @param set Menge, die dem Konfigurationsobjekt zugeordnet werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn die Menge nicht hinzugef�gt werden konnte.
	 */
	public void addSet(ObjectSet set) throws ConfigurationChangeException;

	/**
	 * Entfernt die Zuordnung von diesem Konfigurationsobjekt zu einer Menge. Die �nderung wird erst mit der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @param set Menge, die entfernt werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn die Menge nicht entfernt werden konnte.
	 */
	public void removeSet(ObjectSet set) throws ConfigurationChangeException;
}


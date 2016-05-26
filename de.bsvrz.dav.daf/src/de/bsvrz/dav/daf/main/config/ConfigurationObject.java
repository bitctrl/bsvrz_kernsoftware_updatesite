/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsobjektes. Konfigurationsobjekte haben neben den Eigenschaften aller System-Objekte eine
 * verantwortliche Instanz (Zuständiger), eine Version der Konfiguration ab der das Objekt gültig ist und eine Version der Konfiguration ab der das Objekt nicht
 * mehr gültig ist.<BR/> Die verantwortliche Instanz macht eine Aussage darüber in welcher Umgebung Konfigurationsänderungen für das Objekt durchgeführt werden
 * können. Dies ist für Anwendungsobjekte üblicherweise eine Verkehrsrechnerzentrale oder eine Unterzentrale und für Objekte des Datenmodells eine globale
 * Instanz.<BR/> In einer Konfiguration können Konfigurationsobjekte von verschiedenen verantwortlichen Instanzen gespeichert werden. Jede Konfiguration
 * verwaltet für jede verantwortliche Instanz zu der sie Objekte gespeichert hat eine lokal aktivierte Versionsnummer die nicht unbedingt mit der in der
 * verantwortlichen Instanz aktivierten Versionsnummer übereinstimmen muss. Beispielsweise können in der Konfiguration einer VRZ die Konfigurationsobjekte einer
 * UZ übernommen werden. Die VRZ verwaltet dann für die übernommene Konfiguration eine lokale Versionsnummer.<BR/> Die Versionsnummern ab der ein
 * Konfigurationsobjekt gültig bzw. nicht mehr gültig ist bezieht sich immer auf die lokal aktivierte Versionsnummer der verantwortlichen Instanz des jeweiligen
 * Objekts. Wenn in der VRZ die lokal aktivierte Version der UZ-Konfiguration kleiner als die in der UZ aktivierten Version ist, dann werden z.B. die neusten
 * Objekte der UZ in der VRZ als noch nicht gültig angesehen, obwohl sie in der UZ schon gültig sind.<BR/> Die Schnittstelle bietet außerdem Möglichkeiten, um
 * auf die einem Konfigurationsobjekt zugeordneten benannten Objekt-Mengen zuzugreifen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConfigurationObject extends SystemObject {

	/**
	 * Liefert die Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt gültig ist.
	 *
	 * @return Version ab der das Objekt gültig ist.
	 */
	public short getValidSince();

	/**
	 * Liefert die Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt nicht mehr gültig ist.
	 *
	 * @return Versionsnummer der Konfiguration ab der dieses Konfigurationsobjekt nicht mehr gültig ist. Wenn das Objekt noch nicht mit der Methode {@link
	 *         SystemObject#invalidate} ungültig gemacht worden ist, dann wird der Wert <code>0</code> zurückgegeben.
	 */
	public short getNotValidSince();

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete nicht online änderbare Menge zurück.
	 *
	 * @param name Der Name der gewünschten Menge
	 *
	 * @return Gewünschte Konfigurationsmenge oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public NonMutableSet getNonMutableSet(String name);

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete online änderbare Menge zurück.
	 *
	 * @param name Der Name der gewünschten Menge
	 *
	 * @return Gewünschte dynamische Menge oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public MutableSet getMutableSet(String name);

	/**
	 * Liefert eine diesem Konfigurations-Objekt zugeordnete Objekt-Menge zurück. Wenn die spezifizierte Objekt-Menge nicht online änderbar ist, dann unterstützt
	 * die zurückgegebene Menge die {@link NonMutableSet Schnittstelle für nicht online änderbare Mengen}. Wenn die Menge online änderbar ist, dann unterstützt das
	 * zurückgegebene Mengenobjekt die {@link MutableSet Schnittstelle für online änderbare Mengen}.
	 *
	 * @param name Der Name der gewünschten Menge
	 *
	 * @return Menge von System-Objekten oder <code>null</code>, wenn die spezifizierte Menge nicht vorhanden ist.
	 */
	public ObjectSet getObjectSet(String name);

	/**
	 * Liefert die Liste aller diesem Konfigurationsobjekt zugeordneten Mengen zurück.
	 *
	 * @return Liste aller Mengen dieses Objekts. Die in der Liste enthaltenen Mengen implementieren je nach Art entweder die {@link NonMutableSet Schnittstelle
	 *         für nicht online änderbare Mengen} oder die {@link MutableSet Schnittstelle für online änderbare Mengen}.
	 */
	public List<ObjectSet> getObjectSets();

	/**
	 * Macht ein bereits als ungültig markiertes Objekt wieder gültig. Wenn ein Konfigurationsobjekt mit der Methode {@link SystemObject#invalidate} für eine
	 * zukünftige Konfigurationsversion als ungültig markiert wurde und diese Konfigurationsversion noch nicht aktiviert wurde, dann kann das Objekt durch Aufruf
	 * dieser Methode wieder gültig gemacht werden.
	 *
	 * @throws ConfigurationChangeException Wenn das Objektes nicht wieder gültig gemacht werden konnte.
	 */
	public void revalidate() throws ConfigurationChangeException;

	/**
	 * Dupliziert ein Konfigurationsobjekt. Es ist zu beachten, dass Komponenten nicht isoliert dupliziert werden können, sondern im Sinne der Komposition immer
	 * nur ganze Objekt-Einheiten zusammen dupliziert werden können, d.h. ausgehend von einem freien Objekt wird das Objekt mit all seinen Komponenten rekursiv
	 * dupliziert. Das Duplikat wird mit Aktivierung der in Bearbeitung befindlichen neuen Version des jeweiligen Konfigurationsbereichs gültig. Da die Pids gleich
	 * bleiben, muss zuvor das "alte" Objekt {@link #invalidate() gelöscht} werden.
	 *
	 * @return Das Duplikat dieses Konfigurationsobjekts.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @see #duplicate(java.util.Map)
	 */
	public SystemObject duplicate() throws ConfigurationChangeException;

	/**
	 * Dupliziert ein Konfigurationsobjekt. Es ist zu beachten, dass Komponenten nicht isoliert dupliziert werden können, sondern im Sinne der Komposition immer
	 * nur ganze Objekt-Einheiten zusammen dupliziert werden können, d.h. ausgehend von einem freien Objekt wird das Objekt mit all seinen Komponenten rekursiv
	 * dupliziert. Das Duplikat wird mit Aktivierung der in Bearbeitung befindlichen neuen Version des jeweiligen Konfigurationsbereichs gültig.
	 * <p>
	 * Zu ersetzende Pids der Komponenten können in der Map (altePid, neuePid) übergeben werden. Nicht ersetzte Pids, deren Objekte aber durch Komposition an die
	 * Objekt-Einheit gebunden sind, müssen vor Aktivierung {@link #invalidate() gelöscht} werden.
	 *
	 * @param substitudePids Map, die die Wert-Paare (altePid, neuePid) enthält.
	 *
	 * @return Das Duplikat dieses Konfigurationsobjekts.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt kein freies Objekt ist und das Duplizieren nicht erlaubt ist oder das Duplikat nicht erstellt werden
	 *                                      konnte.
	 * @see #duplicate()
	 */
	public SystemObject duplicate(Map<String, String> substitudePids) throws ConfigurationChangeException;

	/**
	 * Ordnet dem Konfigurationsobjekt eine weitere Menge zu. Die Zuordnung wird erst mit der nächsten Konfigurationsversion gültig.
	 *
	 * @param set Menge, die dem Konfigurationsobjekt zugeordnet werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn die Menge nicht hinzugefügt werden konnte.
	 */
	public void addSet(ObjectSet set) throws ConfigurationChangeException;

	/**
	 * Entfernt die Zuordnung von diesem Konfigurationsobjekt zu einer Menge. Die Änderung wird erst mit der nächsten Konfigurationsversion gültig.
	 *
	 * @param set Menge, die entfernt werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn die Menge nicht entfernt werden konnte.
	 */
	public void removeSet(ObjectSet set) throws ConfigurationChangeException;
}


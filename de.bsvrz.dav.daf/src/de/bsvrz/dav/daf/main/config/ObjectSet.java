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
 * Schnittstelle zum Zugriff auf die Eigenschaften von Objekt-Mengen. Einem Konfigurationsobjekt können
 * mehrere Mengen zugeordnet sein. Die verschiedenen Mengen werden mit ihrem permanenten Mengen-Namen
 * unterschieden. Eine Menge enthält Verweise auf andere System-Objekte. Bei Objekt-Mengen wird unterschieden
 * zwischen online änderbaren und nicht online änderbaren Mengen. Bei online änderbaren Mengen können zur
 * Laufzeit Objekte hinzugefügt und entfernt werden. Änderungen an nicht online änderbaren Mengen werden erst
 * mit Aktivierung der nächsten Konfigurationsversion aktiv. Der Zugriff auf die speziellen Methoden von
 * online änderbaren Mengen ist mit der Schnittstelleklasse {@link MutableSet} möglich. Der Zugriff auf die
 * speziellen Eigenschaften und Funktionen von nicht online änderbaren Mengen ist mit der Schnittstellenklasse
 * {@link NonMutableSet} möglich. In der vorliegenden Schnittstelle sind die Gemeinsamkeiten beider
 * Mengenarten zusammengefasst.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Fouad
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 * @see MutableSet
 * @see NonMutableSet
 */
public interface ObjectSet extends ConfigurationObject, SystemObjectCollection {
	/**
	 * Bestimmt den Typ dieser Menge. Der Mengentyp enthält Beschränkungen über den Typ und die Anzahl der in
	 * einer Menge enthaltenen Objekte und eine Information darüber, ob Elemente online hinzugefügt oder entfernt
	 * werden dürfen. Die Methode entspricht mit Ausnahme des Rückgabetyps der Methode {@link
	 * SystemObject#getType}.
	 *
	 * @return Typ der Menge
	 */
	public ObjectSetType getObjectSetType();

	/**
	 * Erweitert die Menge um ein weiteres Element. Wenn das angegebene Element schon in der Menge enthalten ist,
	 * dann wird die Menge nicht verändert. Wenn der Typ des angegebenen System-Objekts in der Menge nicht
	 * erlaubt ist, wird die Menge nicht verändert und eine Ausnahme generiert. Wenn bei online änderbaren Mengen
	 * die maximale Anzahl von Objekten bereits erreicht ist, wird die Menge nicht verändert und eine Ausnahme
	 * generiert. Wenn die Menge nicht online änderbar ist, dann wird das neue Element erst mit Aktivierung der
	 * nächsten Konfigurationsversion in die Menge aufgenommen.
	 *
	 * @param object Das System-Objekt, das der Menge hinzugefügt werden soll.
	 * @throws ConfigurationChangeException Wenn das übergebene Objekt nicht in die Menge aufgenommen werden
	 *                                      konnte und noch nicht in der Menge enthalten war.
	 */
	public void add(SystemObject object) throws ConfigurationChangeException;

	/**
	 * Erweitert die Menge um beliebig viele Elemente. Sind angegebene Elemente bereits in der Menge, so werden
	 * sie ignoriert. Ausnahmen werden generiert, u.a. wenn der Typ eines angegebenen System-Objekts in der Menge
	 * nicht erlaubt ist und wenn bei online änderbaren Mengen die maximale Anzahl von Objekten überschritten
	 * wird. Bei Ausnahmen wird die Menge nicht verändert. Wenn die Menge nicht online änderbar ist, dann werden
	 * die neuen Elemente erst mit Aktivierung der nächsten Konfigurationsversion in die Menge aufgenommen.
	 *
	 * @param objects Die System-Objekte, die der Menge hinzugefügt werden sollen.
	 * @throws ConfigurationChangeException Wenn eines der übergebenen Objekte nicht in die Menge aufgenommen
	 *                                      werden konnte und noch nicht in der Menge enthalten war.
	 */
	public void add(SystemObject[] objects) throws ConfigurationChangeException;

	/**
	 * Entfernt ein Element der Menge. Wenn das Element nicht in der Menge enthalten ist, wird eine Ausnahme
	 * generiert. Wenn bei online änderbaren Mengen die minimale Anzahl von Objekten bereits erreicht ist, wird
	 * das Element nicht aus der Menge entfernt und eine Ausnahme generiert. Bei nicht online änderbaren Mengen
	 * wird das gegebene Element erst mit Aktivierung der nächsten Konfigurationsversion aus der Menge entfernt.
	 *
	 * @param object Das System-Objekt, das aus der Menge entfernt werden soll.
	 * @throws ConfigurationChangeException Wenn das übergebene Objekt nicht aus der Menge entfernt werden
	 *                                      konnte.
	 */
	public void remove(SystemObject object) throws ConfigurationChangeException;

	/**
	 * Entfernt beliebige Elemente aus der Menge. Ausnahmen werden generiert, u.a. wenn ein Element nicht in der
	 * Menge enthalten ist und wenn bei online änderbaren Mengen die minimale Anzahl von Objekten bereits
	 * erreicht ist. Bei Ausnahmen wird die Menge nicht verändert. Bei nicht online änderbaren Mengen werden die
	 * angegebenen Elemente erst mit Aktivierung der nächsten Konfigurationsversion aus der Menge entfernt.
	 *
	 * @param objects Die System-Objekte, die aus der Menge entfernt werden sollen.
	 * @throws ConfigurationChangeException Wenn eines der übergebenen Objekte nicht aus der Menge entfernt
	 *                                      werden konnte.
	 */
	public void remove(SystemObject[] objects) throws ConfigurationChangeException;
}


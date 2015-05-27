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
 * Schnittstelle zum Zugriff auf die Eigenschaften von Objekt-Mengen. Einem Konfigurationsobjekt k�nnen
 * mehrere Mengen zugeordnet sein. Die verschiedenen Mengen werden mit ihrem permanenten Mengen-Namen
 * unterschieden. Eine Menge enth�lt Verweise auf andere System-Objekte. Bei Objekt-Mengen wird unterschieden
 * zwischen online �nderbaren und nicht online �nderbaren Mengen. Bei online �nderbaren Mengen k�nnen zur
 * Laufzeit Objekte hinzugef�gt und entfernt werden. �nderungen an nicht online �nderbaren Mengen werden erst
 * mit Aktivierung der n�chsten Konfigurationsversion aktiv. Der Zugriff auf die speziellen Methoden von
 * online �nderbaren Mengen ist mit der Schnittstelleklasse {@link MutableSet} m�glich. Der Zugriff auf die
 * speziellen Eigenschaften und Funktionen von nicht online �nderbaren Mengen ist mit der Schnittstellenklasse
 * {@link NonMutableSet} m�glich. In der vorliegenden Schnittstelle sind die Gemeinsamkeiten beider
 * Mengenarten zusammengefasst.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Fouad
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5084 $ / $Date: 2007-09-03 10:42:50 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 * @see MutableSet
 * @see NonMutableSet
 */
public interface ObjectSet extends ConfigurationObject, SystemObjectCollection {
	/**
	 * Bestimmt den Typ dieser Menge. Der Mengentyp enth�lt Beschr�nkungen �ber den Typ und die Anzahl der in
	 * einer Menge enthaltenen Objekte und eine Information dar�ber, ob Elemente online hinzugef�gt oder entfernt
	 * werden d�rfen. Die Methode entspricht mit Ausnahme des R�ckgabetyps der Methode {@link
	 * SystemObject#getType}.
	 *
	 * @return Typ der Menge
	 */
	public ObjectSetType getObjectSetType();

	/**
	 * Erweitert die Menge um ein weiteres Element. Wenn das angegebene Element schon in der Menge enthalten ist,
	 * dann wird die Menge nicht ver�ndert. Wenn der Typ des angegebenen System-Objekts in der Menge nicht
	 * erlaubt ist, wird die Menge nicht ver�ndert und eine Ausnahme generiert. Wenn bei online �nderbaren Mengen
	 * die maximale Anzahl von Objekten bereits erreicht ist, wird die Menge nicht ver�ndert und eine Ausnahme
	 * generiert. Wenn die Menge nicht online �nderbar ist, dann wird das neue Element erst mit Aktivierung der
	 * n�chsten Konfigurationsversion in die Menge aufgenommen.
	 *
	 * @param object Das System-Objekt, das der Menge hinzugef�gt werden soll.
	 * @throws ConfigurationChangeException Wenn das �bergebene Objekt nicht in die Menge aufgenommen werden
	 *                                      konnte und noch nicht in der Menge enthalten war.
	 */
	public void add(SystemObject object) throws ConfigurationChangeException;

	/**
	 * Erweitert die Menge um beliebig viele Elemente. Sind angegebene Elemente bereits in der Menge, so werden
	 * sie ignoriert. Ausnahmen werden generiert, u.a. wenn der Typ eines angegebenen System-Objekts in der Menge
	 * nicht erlaubt ist und wenn bei online �nderbaren Mengen die maximale Anzahl von Objekten �berschritten
	 * wird. Bei Ausnahmen wird die Menge nicht ver�ndert. Wenn die Menge nicht online �nderbar ist, dann werden
	 * die neuen Elemente erst mit Aktivierung der n�chsten Konfigurationsversion in die Menge aufgenommen.
	 *
	 * @param objects Die System-Objekte, die der Menge hinzugef�gt werden sollen.
	 * @throws ConfigurationChangeException Wenn eines der �bergebenen Objekte nicht in die Menge aufgenommen
	 *                                      werden konnte und noch nicht in der Menge enthalten war.
	 */
	public void add(SystemObject[] objects) throws ConfigurationChangeException;

	/**
	 * Entfernt ein Element der Menge. Wenn das Element nicht in der Menge enthalten ist, wird eine Ausnahme
	 * generiert. Wenn bei online �nderbaren Mengen die minimale Anzahl von Objekten bereits erreicht ist, wird
	 * das Element nicht aus der Menge entfernt und eine Ausnahme generiert. Bei nicht online �nderbaren Mengen
	 * wird das gegebene Element erst mit Aktivierung der n�chsten Konfigurationsversion aus der Menge entfernt.
	 *
	 * @param object Das System-Objekt, das aus der Menge entfernt werden soll.
	 * @throws ConfigurationChangeException Wenn das �bergebene Objekt nicht aus der Menge entfernt werden
	 *                                      konnte.
	 */
	public void remove(SystemObject object) throws ConfigurationChangeException;

	/**
	 * Entfernt beliebige Elemente aus der Menge. Ausnahmen werden generiert, u.a. wenn ein Element nicht in der
	 * Menge enthalten ist und wenn bei online �nderbaren Mengen die minimale Anzahl von Objekten bereits
	 * erreicht ist. Bei Ausnahmen wird die Menge nicht ver�ndert. Bei nicht online �nderbaren Mengen werden die
	 * angegebenen Elemente erst mit Aktivierung der n�chsten Konfigurationsversion aus der Menge entfernt.
	 *
	 * @param objects Die System-Objekte, die aus der Menge entfernt werden sollen.
	 * @throws ConfigurationChangeException Wenn eines der �bergebenen Objekte nicht aus der Menge entfernt
	 *                                      werden konnte.
	 */
	public void remove(SystemObject[] objects) throws ConfigurationChangeException;
}


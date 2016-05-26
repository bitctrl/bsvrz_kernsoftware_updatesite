/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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
 * Dieses Interface stellt Methoden zur Verfügung mit denen dynamische Objekte auf Meta-Seite geändert werden können.
 * <p>
 * Dieser Mechanismus wird benötigt, weil sich dynamische Objekte zur Laufzeit ändern. Die Konfiguration wird diese Änderungen an alle Applikationen
 * propagieren, die sich bei der Konfiguration angemeldet haben.
 * <p>
 * Einige Beispiele für sich ändernde Werte von dynamischen Objekten sind: Name, Gültigkeit des Objekts, Zeitpunkt ab dem das Objekt nicht mehr gültig war,
 * usw..
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface UpdateDynamicObjects {

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration eine Namensänderung eines dynamischen Objekts propagiert hat.
	 * <p>
	 * Auf Meta-Seite muss dann der Name des Objekts aktualisiert werden. Des Weiteren muss dem Typ des Objekts die Namensänderung mitgeteilt werden. Dies ist
	 * nötig, weil am Typ des Objekts Listener für Namensänderungen vorhanden sind.
	 * <p>
	 * Beim setzen des Namens darf nicht die {@link DynamicObject#setName(String)} Methode benutzt werden, weil dadurch erneut eine Anfrage an die Konfiguration
	 * verschickt werden würde.
	 * <p>
	 * Der beschriebene Mechanismus muss nur durchgeführt werden wenn: Sich das Objekt im Cache des Meta-Datamodells befindet oder ein Listener auf
	 * Namensänderungen für den Typ des Objekt angemeldet ist.
	 * <p>
	 * Sind beide Bedingungen nicht erfüllt kann die Namensänderung verworfen werden.
	 *
	 * @param objectId Id des Objekts, dessen Name aktualisiert werden soll
	 * @param typeId   Typ des Objekts, der informiert wird, dass sich der Name eines Objekts geändert hat
	 * @param newName  Neuer Name des Objekts
	 */
	public void updateName(final long objectId, final long typeId, final String newName);

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration propagiert, dass ein dynamisches Objekt ungültig wurde.
	 * <p>
	 * Die Methode muss beim dynamischen Objekt den Zeitpunkt aktualisieren, an dem es ungültig wurde und der Methodenaufruf von {@link
	 * DynamicObject#isValid()} muss false zurück geben. Der Typ des Objekts muss ebenfalls informatiert werden, dass das
	 * Objekt nicht mehr gültig ist. Dies ist nötig, weil eventuell Listener auf diese Änderungen angemeldet sind.
	 * <p>
	 * Der beschriebene Mechanismus muss nur durchgeführt werden wenn: Sich das Objekt im Cache des Meta-Datamodells befindet oder ein Listener auf Invalidation
	 * für den Typ des Objekt angemeldet ist.
	 * <p>
	 * Sind beide Bedingungen nicht erfüllt kann die Änderung verworfen werden.
	 *
	 * @param objectId  Id des Objekts, dessen UngültigAb-Wert aktualisiert werden soll
	 * @param typeId    Typ des Objekts, der informiert wird, dass das Objekt ungültig geworden ist
	 * @param notValidTime Zeitpunkt, an dem das Objekt ungültig wurde
	 */
	public void updateNotValidSince(final long objectId, final long typeId, final long notValidTime);

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration propagiert, dass ein neues dynamisches Objekt erzeugt wurde.
	 * <p>
	 * Diese Information muss an den jeweiligen Typen des Objekt weitergereicht werden. Dies ist nötig, weil vielleicht Listener auf diese Information angemeldet
	 * sind. Ist dies der Fall, so muss das vollständige Objekt aus der Konfiguration angefordert werden.
	 *
	 * @param objectId Objekt, das neu angelegt wurde
	 * @param typeId   Typ des neuen Objekts. Dieser Typ wird darüber informiert, dass ein neues Objekt angelegt wurde.
	 */
	public void newDynamicObjectCreated(final long objectId, final long typeId);
}

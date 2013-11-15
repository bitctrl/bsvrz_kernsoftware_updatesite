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

package de.bsvrz.dav.daf.main.config;

/**
 * Dieses Interface stellt Methoden zur Verf�gung mit denen dynamische Objekte auf Meta-Seite ge�ndert werden k�nnen.
 * <p/>
 * Dieser Mechanismus wird ben�tigt, weil sich dynamische Objekte zur Laufzeit �ndern. Die Konfiguration wird diese �nderungen an alle Applikationen
 * propagieren, die sich bei der Konfiguration angemeldet haben.
 * <p/>
 * Einige Beispiele f�r sich �ndernde Werte von dynamischen Objekten sind: Name, G�ltigkeit des Objekts, Zeitpunkt ab dem das Objekt nicht mehr g�ltig war,
 * usw..
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5623 $
 */
public interface UpdateDynamicObjects {

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration eine Namens�nderung eines dynamischen Objekts propagiert hat.
	 * <p/>
	 * Auf Meta-Seite muss dann der Name des Objekts aktualisiert werden. Des Weiteren muss dem Typ des Objekts die Namens�nderung mitgeteilt werden. Dies ist
	 * n�tig, weil am Typ des Objekts Listener f�r Namens�nderungen vorhanden sind.
	 * <p/>
	 * Beim setzen des Namens darf nicht die {@link DynamicObject#setName(String)} Methode benutzt werden, weil dadurch erneut eine Anfrage an die Konfiguration
	 * verschickt werden w�rde.
	 * <p/>
	 * Der beschriebene Mechanismus muss nur durchgef�hrt werden wenn: Sich das Objekt im Cache des Meta-Datamodells befindet oder ein Listener auf
	 * Namens�nderungen f�r den Typ des Objekt angemeldet ist.
	 * <p/>
	 * Sind beide Bedingungen nicht erf�llt kann die Namens�nderung verworfen werden.
	 *
	 * @param objectId Id des Objekts, dessen Name aktualisiert werden soll
	 * @param typeId   Typ des Objekts, der informiert wird, dass sich der Name eines Objekts ge�ndert hat
	 * @param newName  Neuer Name des Objekts
	 */
	public void updateName(final long objectId, final long typeId, final String newName);

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration propagiert, dass ein dynamisches Objekt ung�ltig wurde.
	 * <p/>
	 * Die Methode muss beim dynamischen Objekt den Zeitpunkt aktualisieren, an dem es ung�ltig wurde und der Methodenaufruf von {@link
	 * DynamicObject#isValid()} muss false zur�ck geben. Der Typ des Objekts muss ebenfalls informatiert werden, dass das
	 * Objekt nicht mehr g�ltig ist. Dies ist n�tig, weil eventuell Listener auf diese �nderungen angemeldet sind.
	 * <p/>
	 * Der beschriebene Mechanismus muss nur durchgef�hrt werden wenn: Sich das Objekt im Cache des Meta-Datamodells befindet oder ein Listener auf Invalidation
	 * f�r den Typ des Objekt angemeldet ist.
	 * <p/>
	 * Sind beide Bedingungen nicht erf�llt kann die �nderung verworfen werden.
	 *
	 * @param objectId  Id des Objekts, dessen Ung�ltigAb-Wert aktualisiert werden soll
	 * @param typeId    Typ des Objekts, der informiert wird, dass das Objekt ung�ltig geworden ist
	 * @param notValidTime Zeitpunkt, an dem das Objekt ung�ltig wurde
	 */
	public void updateNotValidSince(final long objectId, final long typeId, final long notValidTime);

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration propagiert, dass ein neues dynamisches Objekt erzeugt wurde.
	 * <p/>
	 * Diese Information muss an den jeweiligen Typen des Objekt weitergereicht werden. Dies ist n�tig, weil vielleicht Listener auf diese Information angemeldet
	 * sind. Ist dies der Fall, so muss das vollst�ndige Objekt aus der Konfiguration angefordert werden.
	 *
	 * @param objectId Objekt, das neu angelegt wurde
	 * @param typeId   Typ des neuen Objekts. Dieser Typ wird dar�ber informiert, dass ein neues Objekt angelegt wurde.
	 */
	public void newDynamicObjectCreated(final long objectId, final long typeId);
}

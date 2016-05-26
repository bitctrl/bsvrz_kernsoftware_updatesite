/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.configFile.fileaccess;


/**
 * Dieses Interface stellt für Konfigurationsobjekte Informationen zur Verfügung.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ConfigurationObjectInfo extends SystemObjectInformationInterface {
	/**
	 * Diese Methode gibt an, mit welcher Version des Konfigurationsbereichs das Konfigurationsobjekt gültig werden
	 * soll/sollte.
	 *
	 * @return s.o
	 */
	short getFirstValidVersion();

	/**
	 * Diese Methode gibt an, mit welcher Version des Konfigurationsbereichs das Konfigurationsobjekt ungültig werden
	 * soll/sollte. Der Wert "0" zeigt an, dass dieser Wert noch unbekannt ist und das das Objekt auf eine unbestimmte Zeit
	 * gültig bleibt.
	 *
	 * @return s.o
	 */
	short getFirstInvalidVersion();

	/**
	 * Das Konfigurationsobjekt wird mit der nächsten Version des Konfigurationsbereichs ungültig. Ist das Objekt
	 * in der aktuellen Version noch nicht gültig so wird es nicht auf ungültig gesetzt, sondern gelöscht.
	 *
	 * @see #revalidate
	 */
	void invalidate();

	/**
	 * Solange der Konfigurationsbereich noch nicht in eine neue Version überführt wurde, kann ein Konfigurationsobjekt,
	 * welches auf ungültig gesetzt wurde, mit dieser Methode wieder auf gültig gesetzt werden.
	 *
	 * @see #invalidate
	 */
	void revalidate();

	/**
	 * Diese Methode gibt die IdŽs aller Mengen zurück, die an dem Konfigurationsobjekt gespeichert sind.
	 *
	 * @return s.o.
	 */
	long[] getObjectSetIds();

	/**
	 * Diese Methode fügt eine leere Menge an ein Konfigurationsobjekt hinzu.
	 *
	 * @param setId Id der Menge, die zu dem Konfigurationsobjekt hinzugefügt werden soll
	 * @throws IllegalStateException Eine Menge mit der Id existiert bereits an diesem Objekt
	 */
	void addObjectSetId(long setId) throws IllegalStateException;

	/**
	 * Diese Methode gibt zu einer Menge, die zu diesem Konfigurationsobjekt gehört, alle IdŽs der jeweiligen Objekte zurück, die sich in
	 * der Menge befinden.
	 *
	 * @param setId Menge, die zu dem Konigurationsobjekt gehört
	 * @return Objekte, die in der Menge gespeichert sind, die mit der <code>setId</code> identifiziert wurde
	 * @throws IllegalArgumentException Es gibt zu der Id keine Menge
	 */
	long[] getObjectSetObjects(long setId) throws IllegalArgumentException;

	/**
	 * Diese Methode fügt ein Objekt zu einer bestehenden Menge hinzu. Die Menge wird über die Id identifiziert.
	 *
	 * @param setId    Menge, zu der das Objekt hinzugefügt werden soll
	 * @param objectId Objekt, das zu der Menge hinzugefügt werden soll
	 * @throws IllegalArgumentException Die Menge existiert nicht an dem Konfigurationsobjekt
	 * @throws IllegalStateException Ein Objekt mit der Id existiert bereits in der Menge
	 */
	void addObjectSetObject(long setId, long objectId) throws IllegalArgumentException, IllegalStateException;
}

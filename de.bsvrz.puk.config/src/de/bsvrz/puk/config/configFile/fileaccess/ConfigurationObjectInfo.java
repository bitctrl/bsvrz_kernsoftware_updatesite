/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.puk.config.configFile.fileaccess;


/**
 * Dieses Interface stellt f�r Konfigurationsobjekte Informationen zur Verf�gung.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
 */
public interface ConfigurationObjectInfo extends SystemObjectInformationInterface {
	/**
	 * Diese Methode gibt an, mit welcher Version des Konfigurationsbereichs das Konfigurationsobjekt g�ltig werden
	 * soll/sollte.
	 *
	 * @return s.o
	 */
	short getFirstValidVersion();

	/**
	 * Diese Methode gibt an, mit welcher Version des Konfigurationsbereichs das Konfigurationsobjekt ung�ltig werden
	 * soll/sollte. Der Wert "0" zeigt an, dass dieser Wert noch unbekannt ist und das das Objekt auf eine unbestimmte Zeit
	 * g�ltig bleibt.
	 *
	 * @return s.o
	 */
	short getFirstInvalidVersion();

	/**
	 * Das Konfigurationsobjekt wird mit der n�chsten Version des Konfigurationsbereichs ung�ltig. Ist das Objekt
	 * in der aktuellen Version noch nicht g�ltig so wird es nicht auf ung�ltig gesetzt, sondern gel�scht.
	 *
	 * @see #revalidate
	 */
	void invalidate();

	/**
	 * Solange der Konfigurationsbereich noch nicht in eine neue Version �berf�hrt wurde, kann ein Konfigurationsobjekt,
	 * welches auf ung�ltig gesetzt wurde, mit dieser Methode wieder auf g�ltig gesetzt werden.
	 *
	 * @see #invalidate
	 */
	void revalidate();

	/**
	 * Diese Methode gibt die Id�s aller Mengen zur�ck, die an dem Konfigurationsobjekt gespeichert sind.
	 *
	 * @return s.o.
	 */
	long[] getObjectSetIds();

	/**
	 * Diese Methode f�gt eine leere Menge an ein Konfigurationsobjekt hinzu.
	 *
	 * @param setId Id der Menge, die zu dem Konfigurationsobjekt hinzugef�gt werden soll
	 * @throws IllegalStateException Eine Menge mit der Id existiert bereits an diesem Objekt
	 */
	void addObjectSetId(long setId) throws IllegalStateException;

	/**
	 * Diese Methode gibt zu einer Menge, die zu diesem Konfigurationsobjekt geh�rt, alle Id�s der jeweiligen Objekte zur�ck, die sich in
	 * der Menge befinden.
	 *
	 * @param setId Menge, die zu dem Konigurationsobjekt geh�rt
	 * @return Objekte, die in der Menge gespeichert sind, die mit der <code>setId</code> identifiziert wurde
	 * @throws IllegalArgumentException Es gibt zu der Id keine Menge
	 */
	long[] getObjectSetObjects(long setId) throws IllegalArgumentException;

	/**
	 * Diese Methode f�gt ein Objekt zu einer bestehenden Menge hinzu. Die Menge wird �ber die Id identifiziert.
	 *
	 * @param setId    Menge, zu der das Objekt hinzugef�gt werden soll
	 * @param objectId Objekt, das zu der Menge hinzugef�gt werden soll
	 * @throws IllegalArgumentException Die Menge existiert nicht an dem Konfigurationsobjekt
	 * @throws IllegalStateException Ein Objekt mit der Id existiert bereits in der Menge
	 */
	void addObjectSetObject(long setId, long objectId) throws IllegalArgumentException, IllegalStateException;
}

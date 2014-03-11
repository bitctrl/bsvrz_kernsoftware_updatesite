/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Dieses Interface stellt Informationen zur Verfügung, die sowohl ein dynamisches Objekt als auch ein
 * Konfigurationsobjekt zur Verfügung stellen muss. In der Beschreibung wird nicht zwischen dynamischen Objekten und
 * Konfigurationsobjekten unterschieden, beide Arten werden nur als "Objekt" bezeichnet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $ / ($Author: rs $)
 */
public interface SystemObjectInformationInterface {

	/**
	 * Diese Methode gibt die ID des Objekts zurück.
	 *
	 * @return s.o.
	 */
	long getID();

	/**
	 * Diese Methode gibt die Pid des Objekts zurück.
	 *
	 * @return s.o.
	 */
	String getPid();

	/**
	 * Diese Methode gibt den Typ des Objekts als ID zurück.
	 *
	 * @return ID, die den Typ des Objekts wiederspiegelt
	 */
	long getTypeId();

	/**
	 * Diese Methode gibt den Namen des Objekts zurück, wurde kein Name festgelegt wird ein leerer String <code>""</code>
	 * zurückgegeben.
	 *
	 * @return s.o.
	 */
	String getName();

	/**
	 * Diese Methode legt den Namen eines Objekts fest, besitzt das Objekt bereits einen Namen, so wird dieser
	 * überschrieben.
	 *
	 * @param newName Neuer Name des Objekts
	 */
	void setName(String newName);

	/**
	 * Diese Methode gibt die Attributgruppenverwendungs-Id´s aller konfigurierenden Datensätze zurück, die an diesem Objekt mit
	 * {@link #setConfigurationData} abgelegt wurden.
	 *
	 * @return Wenn keine Id´s zur Verfügung stehen, wird ein leeres Array zurückgegeben
	 */
	long[] getConfigurationsDataAttributeGroupUsageIds();

	/**
	 * Diese Methode gibt einen konfigurierenden Datensatz zurück, der am Objekt gespeichert ist. Der Datensatz wird über
	 * die ID seiner Attributgruppenverwendung identifiziert.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung des gewünschten konfigurierenden Datensatzes
	 * @return konfigurierender Datensatz, der am Objekt gespeichert ist
	 * @throws IllegalArgumentException Diese Exception wird geworfen, wenn es keinen konfigurierenden Datensatz zu der
	 *                                  übergebenen attributeGroupUsageId gibt
	 */
	byte[] getConfigurationData(long attributeGroupUsageId) throws IllegalArgumentException;

	/**
	 * Diese Methode speichert einen konfigurierenden Datensatz am Objekt. Ist bereits ein Datensatz unter der
	 * attributeGroupUsageId gespeichert wird dieser überschrieben.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung zu dem der konfigurierende Datensatz gehört
	 * @param data                 serialisierter Datensatz, siehe auch {@link ConfigurationAreaFile#getSerializerVersion}.
	 *                             Das byte-Array kann die Länge 0 habe, das Objekt <code>null</code> ist verboten.
	 */
	void setConfigurationData(long attributeGroupUsageId, byte[] data) throws IllegalStateException;

	/**
	 * Diese Methode entfernt einen konfigurierenden Datensatz, der mit {@link #setConfigurationData} hinzugefügt wurde.
	 *
	 * @param attributeGroupUsageId Id der Attributgruppenverwendung zu dem der konfigurierende Datensatz gehört, der entfernt werden
	 *                             soll
	 */
	void removeConfigurationData(long attributeGroupUsageId) throws IllegalStateException;
}

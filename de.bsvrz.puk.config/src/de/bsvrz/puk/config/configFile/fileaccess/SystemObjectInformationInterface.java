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
 * Dieses Interface stellt Informationen zur Verf�gung, die sowohl ein dynamisches Objekt als auch ein
 * Konfigurationsobjekt zur Verf�gung stellen muss. In der Beschreibung wird nicht zwischen dynamischen Objekten und
 * Konfigurationsobjekten unterschieden, beide Arten werden nur als "Objekt" bezeichnet.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 13128 $ / $Date: 2015-01-27 16:08:07 +0100 (Tue, 27 Jan 2015) $ / ($Author: jh $)
 */
public interface SystemObjectInformationInterface {

	/**
	 * Diese Methode gibt die ID des Objekts zur�ck.
	 *
	 * @return s.o.
	 */
	long getID();

	/**
	 * Diese Methode gibt die Pid des Objekts zur�ck.
	 *
	 * @return s.o.
	 */
	String getPid();

	/**
	 * Diese Methode gibt den Typ des Objekts als ID zur�ck.
	 *
	 * @return ID, die den Typ des Objekts wiederspiegelt
	 */
	long getTypeId();

	/**
	 * Diese Methode gibt den Namen des Objekts zur�ck, wurde kein Name festgelegt wird ein leerer String <code>""</code>
	 * zur�ckgegeben.
	 *
	 * @return s.o.
	 */
	String getName();

	/**
	 * Diese Methode legt den Namen eines Objekts fest, besitzt das Objekt bereits einen Namen, so wird dieser
	 * �berschrieben.
	 *
	 * @param newName Neuer Name des Objekts
	 */
	void setName(String newName);

	/**
	 * Diese Methode gibt die Attributgruppenverwendungs-Id�s aller konfigurierenden Datens�tze zur�ck, die an diesem Objekt mit
	 * {@link #setConfigurationData} abgelegt wurden.
	 *
	 * @return Wenn keine Id�s zur Verf�gung stehen, wird ein leeres Array zur�ckgegeben
	 */
	long[] getConfigurationsDataAttributeGroupUsageIds();

	/**
	 * Diese Methode gibt einen konfigurierenden Datensatz zur�ck, der am Objekt gespeichert ist. Der Datensatz wird �ber
	 * die ID seiner Attributgruppenverwendung identifiziert.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung des gew�nschten konfigurierenden Datensatzes
	 * @return konfigurierender Datensatz, der am Objekt gespeichert ist
	 * @throws IllegalArgumentException Diese Exception wird geworfen, wenn es keinen konfigurierenden Datensatz zu der
	 *                                  �bergebenen attributeGroupUsageId gibt
	 */
	byte[] getConfigurationData(long attributeGroupUsageId) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt einen konfigurierenden Datensatz zur�ck, der am Objekt gespeichert ist. Der Datensatz wird �ber
	 * die ID seiner Attributgruppenverwendung identifiziert.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung des gew�nschten konfigurierenden Datensatzes
	 * @return konfigurierender Datensatz, der am Objekt gespeichert ist oder null falls kein Datensatz vorhanden
	 */
	byte[] getConfigurationDataOptional(long attributeGroupUsageId);

	/**
	 * Diese Methode speichert einen konfigurierenden Datensatz am Objekt. Ist bereits ein Datensatz unter der attributeGroupUsageId
	 * gespeichert wird dieser �berschrieben.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung zu dem der konfigurierende Datensatz geh�rt
	 * @param data                  serialisierter Datensatz, siehe auch {@link ConfigurationAreaFile#getSerializerVersion}. Wenn das
	 *                              byte-Array die L�nge 0 hat oder null ist wird der Datensatz gel�scht.
	 * @throws java.lang.IllegalStateException Falls objekt {@link #isDeleted() bereits gel�scht}.
	 */
	void setConfigurationData(long attributeGroupUsageId, byte[] data) throws IllegalStateException;

	/**
	 * Gibt zur�ck, ob das Objekt bereits gel�scht wurde. Danach sind beispielsweise �nderungen an den Konfigurationsdaten verboten,
	 * da das Objekt dann unerlaubterweise aus den NGA-Bl�cken (oder dem NgDyn-Block) in die Mischmenge wandern w�rde.
	 * @return true: wurde schon gel�scht, false: Objekt ist g�ltig oder wird in Zukunft g�ltig (Objekt befindet sich sicher in der Mischmenge)
	 */
	boolean isDeleted();

	/**
	 * Diese Methode entfernt einen konfigurierenden Datensatz, der mit {@link #setConfigurationData} hinzugef�gt wurde.
	 *
	 * @param attributeGroupUsageId Id der Attributgruppenverwendung zu dem der konfigurierende Datensatz geh�rt, der entfernt werden
	 *                             soll
	 */
	void removeConfigurationData(long attributeGroupUsageId) throws IllegalStateException;

	/**
	 * Gibt die zugeh�rige Konfigurationsdatei zur�ck
	 * @return die zugeh�rige Konfigurationsdatei
	 */
	ConfigAreaFile getConfigAreaFile();
}

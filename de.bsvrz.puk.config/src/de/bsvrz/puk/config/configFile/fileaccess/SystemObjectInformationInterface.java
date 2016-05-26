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
 * Dieses Interface stellt Informationen zur Verfügung, die sowohl ein dynamisches Objekt als auch ein
 * Konfigurationsobjekt zur Verfügung stellen muss. In der Beschreibung wird nicht zwischen dynamischen Objekten und
 * Konfigurationsobjekten unterschieden, beide Arten werden nur als "Objekt" bezeichnet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
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
	 * Diese Methode gibt die Attributgruppenverwendungs-IdŽs aller konfigurierenden Datensätze zurück, die an diesem Objekt mit
	 * {@link #setConfigurationData} abgelegt wurden.
	 *
	 * @return Wenn keine IdŽs zur Verfügung stehen, wird ein leeres Array zurückgegeben
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
	 * Diese Methode gibt einen konfigurierenden Datensatz zurück, der am Objekt gespeichert ist. Der Datensatz wird über
	 * die ID seiner Attributgruppenverwendung identifiziert.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung des gewünschten konfigurierenden Datensatzes
	 * @return konfigurierender Datensatz, der am Objekt gespeichert ist oder null falls kein Datensatz vorhanden
	 */
	byte[] getConfigurationDataOptional(long attributeGroupUsageId);

	/**
	 * Diese Methode speichert einen konfigurierenden Datensatz am Objekt. Ist bereits ein Datensatz unter der attributeGroupUsageId
	 * gespeichert wird dieser überschrieben.
	 *
	 * @param attributeGroupUsageId ID der Attributgruppenverwendung zu dem der konfigurierende Datensatz gehört
	 * @param data                  serialisierter Datensatz, siehe auch {@link ConfigurationAreaFile#getSerializerVersion}. Wenn das
	 *                              byte-Array die Länge 0 hat oder null ist wird der Datensatz gelöscht.
	 * @throws java.lang.IllegalStateException Falls objekt {@link #isDeleted() bereits gelöscht}.
	 */
	void setConfigurationData(long attributeGroupUsageId, byte[] data) throws IllegalStateException;

	/**
	 * Gibt zurück, ob das Objekt bereits gelöscht wurde. Danach sind beispielsweise Änderungen an den Konfigurationsdaten verboten,
	 * da das Objekt dann unerlaubterweise aus den NGA-Blöcken (oder dem NgDyn-Block) in die Mischmenge wandern würde.
	 * @return true: wurde schon gelöscht, false: Objekt ist gültig oder wird in Zukunft gültig (Objekt befindet sich sicher in der Mischmenge)
	 */
	boolean isDeleted();

	/**
	 * Diese Methode entfernt einen konfigurierenden Datensatz, der mit {@link #setConfigurationData} hinzugefügt wurde.
	 *
	 * @param attributeGroupUsageId Id der Attributgruppenverwendung zu dem der konfigurierende Datensatz gehört, der entfernt werden
	 *                             soll
	 */
	void removeConfigurationData(long attributeGroupUsageId) throws IllegalStateException;

	/**
	 * Gibt die zugehörige Konfigurationsdatei zurück
	 * @return die zugehörige Konfigurationsdatei
	 */
	ConfigAreaFile getConfigAreaFile();
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1.ConcreteDataFactory;

/**
 * Klasse, die zum Erzeugen von Datens�tzen aus einem Byte-Array mit verschiedenen Serialisiererversionen verwendet werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public abstract class DataFactory {

	/**
	 * Liefert eine konkrete DataFactory-Implementierung f�r eine vorgegebene Serialisierungsversion zur�ck.
	 * @param version Version der gew�nschten Serialisierung. Momentan wird hier nur die Version 1 unterst�tzt.
	 * @return Konkrete DataFactory-Implementierung f�r die angegebene Serialisierungsversion.
	 */
	public static DataFactory forVersion(int version) {
		switch(version) {
			case 1:
				return ConcreteDataFactory.getInstance();
			default:
				throw new IllegalArgumentException("Serialisierungsversion wird nicht unterst�tzt: " + version);
		}
	}

	/**
	 * L�scht die f�r eine Datenverteilerverbindung bzw. eine DataModel-Implementierung zwischengespeicherten Datenstrukturen. Diese Methode sollte
	 * aufgerufen werden, wenn eine Datenverteilerverbindung bzw. eine DataModel-Implementierung nicht mehr zur Verf�gung steht und die zwischengespeicherten
	 * Datenstrukturen freigegeben werden sollen.
	 * @param dataModel DataModel
	 */
	public static void forget(DataModel dataModel) {
		ConcreteDataFactory.forgetDataModel(dataModel);
	}

	/**
	 * Erzeugt ein nicht modifizierbares Data-Objekt einer vorgegebenen Attributgruppe aus einem serialisierten Datensatz.
	 * @param atg Attributgruppe des Datensatzes
	 * @param bytes Serialisierter Datensatz
	 * @return Nicht modifizierbares Data-Objekt zum Zugriff auf den Datensatz.
	 */
	public abstract Data createUnmodifiableData(AttributeGroup atg, byte[] bytes);

	/**
	 * Erzeugt ein modifizierbares Data-Objekt einer vorgegebenen Attributgruppe aus einem serialisierten Datensatz.
	 * @param atg Attributgruppe des Datensatzes
	 * @param bytes Serialisierter Datensatz
	 * @return Modifizierbares Data-Objekt zum Zugriff auf den Datensatz.
	 */
	public abstract Data createModifiableData(AttributeGroup atg, byte[] bytes);

	





}

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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.DataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.StreamFetcher;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;

import java.util.List;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Klasse, die zum Erzeugen von Datens�tzen aus einem Byte-Array f�r die Serialisiererversion 1 verwendet wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5063 $
 */
public final class ConcreteDataFactory extends DataFactory {
	private static DataFactory _Instance = new ConcreteDataFactory();

	/**
	 * L�scht die f�r eine Datenverteilerverbindung bzw. eine DataModel-Implementierung zwischengespeicherten Datenstrukturen. Diese Methode sollte
	 * aufgerufen werden, wenn eine Datenverteilerverbindung bzw. eine DataModel-Implementierung nicht mehr zur Verf�gung steht und die zwischengespeicherten
	 * Datenstrukturen freigegeben werden sollen.
	 * @param dataModel DataModel
	 */
	public static void forgetDataModel(DataModel dataModel) {
		AttributeGroupInfo.forgetDataModel(dataModel);
		AbstractAttributeDefinitionInfo.forgetDataModel(dataModel);
	}

	/**
	 * Liefert die einzige Objekt dieser Klasse zur�ck.
	 * @return Einziges Objekt dieser Klasse.
	 */
	public static DataFactory getInstance() {
		return _Instance;
	}

	private ConcreteDataFactory() {
	}

	public Data createUnmodifiableData(AttributeGroup atg, byte[] bytes) {
		AttributeInfo info = AttributeGroupInfo.forAttributeGroup(atg);
		return ByteArrayData.create(bytes, info);
	}

	public Data createModifiableData(AttributeGroup atg, byte[] bytes) {
		try {
			// Erzeugt eine Liste von AttributeBaseValue Objekte, f�r jedes Attribut auf oberstem Level der ATG jeweils
			// ein Objekt. Der Quasi-Datensatz enth�lt zwar schon Struktur aber keine Werte.
			final List attributeBaseValues = AttributeHelper.getAttributesValues(atg);

			// Stream �ber den der Datensatz im Byte-Array gelesen wird.
			final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

			// Jetzt werden die einzelnen Attribut-Werte des Datensatzes aus dem Stream gelesen.
			final DataValue[] values = StreamFetcher.getInstance().getDataValuesFromStream(
			        atg.getDataModel(),
			        atg,
			        in
			);

			// Dann werden die gelesenen Attribut-Werte in den Quasi-Datensatz eingetragen.
			final int size = attributeBaseValues.size();
			for (int i = 0; i < size; ++i) {
				final AttributeBaseValue attributeBaseValue = (AttributeBaseValue)attributeBaseValues.get(i);
				attributeBaseValue.setValue(values[i]);
			}
			return AttributeBaseValueDataFactory.createAdapter(atg, attributeBaseValues);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e); 
		}

	}

}

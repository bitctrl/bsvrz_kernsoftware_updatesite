/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import de.bsvrz.dav.daf.main.impl.config.DafDataModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Antworttelegramm für Objekte gleichen Typs dar. Dem Typ der Objekte ist eine eindeutige ID zugeordnet. In diesem Telegramm wird eine
 * Liste der Objekte der gleichen Typen gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TypeIdsToObjectsAnswer extends SystemObjectAnswerInfo {

	/** Eine Liste der Kontainer der Objekte eines Typs */
	private ObjectsList _objectsOfTypes[];

	/** Das DataModel */
	private DafDataModel _dataModel;

	/** @param dataModel Datenmodel */
	public TypeIdsToObjectsAnswer(DafDataModel dataModel) {
		_answerType = TYPE_IDS_TO_OBJECTS_TYPE;
		_dataModel = dataModel;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param objectsOfTypes Liste der Typen
	 * @param dataModel      Datenmodel
	 */
	public TypeIdsToObjectsAnswer(ObjectsList[] objectsOfTypes, DafDataModel dataModel) {
		_answerType = TYPE_IDS_TO_OBJECTS_TYPE;
		_objectsOfTypes = objectsOfTypes;
		_dataModel = dataModel;
	}

	/**
	 * Gibt die Liste der kontainer der Objekte eines Typs zurück
	 *
	 * @return kontainer der Objekte eines Typs
	 */
	public final ObjectsList[] getObjectsOfTypes() {
		return _objectsOfTypes;
	}

	public final String parseToString() {
		String str = "Kontainer der Objekte eines Typs: \n";
		if(_objectsOfTypes != null) {
			for(int i = 0; i < _objectsOfTypes.length; ++i) {
				str += _objectsOfTypes[i].parseToString();
			}
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(_objectsOfTypes == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_objectsOfTypes.length);
			for(int i = 0; i < _objectsOfTypes.length; ++i) {
				_objectsOfTypes[i].write(out);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int size = in.readInt();
		if(size > 0) {
			_objectsOfTypes = new ObjectsList[size];
			for(int i = 0; i < size; ++i) {
				_objectsOfTypes[i] = new ObjectsList(_dataModel);
				_objectsOfTypes[i].read(in);
			}
		}
	}
}

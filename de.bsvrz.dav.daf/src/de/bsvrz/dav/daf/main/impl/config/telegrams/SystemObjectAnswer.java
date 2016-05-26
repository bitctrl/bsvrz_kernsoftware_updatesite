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
 * Diese Klasse stellt ein generelles Antworttelegramm auf Anfrage nach Objekten dar. In einer Fallunterscheidung des Anfragetyps werden die Telegramme nach den
 * entsprechenden Anforderungen erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SystemObjectAnswer extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long _configTime;

	/** Informationen zur Antwort */
	private SystemObjectAnswerInfo _systemObjectAnswerInfo;

	/** Das Datenmodel */
	private DafDataModel _dataModel;

	/**
	 * Erzeugt ein neues Objekt mit generalisiertem Parameter. Die spezifischen Parameter werden zu einem späteren Zeitpunkt über die read-Methode eingelesen.
	 *
	 * @param dataModel Datenmodel
	 */
	public SystemObjectAnswer(DafDataModel dataModel) {
		_type = OBJECT_ANSWER_TYPE;
		_dataModel = dataModel;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configTime             Konfigurationszeit
	 * @param systemObjectAnswerInfo Informationen zur Antwort
	 * @param dataModel              Datenmodel
	 */
	public SystemObjectAnswer(long configTime, SystemObjectAnswerInfo systemObjectAnswerInfo, DafDataModel dataModel) {
		_type = OBJECT_ANSWER_TYPE;
		_configTime = configTime;
		_systemObjectAnswerInfo = systemObjectAnswerInfo;
		_dataModel = dataModel;
	}

	/**
	 * Gibt die Konfigurationszeit zurück
	 *
	 * @return Die Konfigurationszeit
	 */
	public final long getConfigTime() {
		return _configTime;
	}

	/**
	 * Gibt die Information zur Antwort zurück
	 *
	 * @return Die Information zur Antwort
	 */
	public final SystemObjectAnswerInfo getSystemObjectAnswerInfo() {
		return _systemObjectAnswerInfo;
	}

	public final String parseToString() {
		String str = "Objektsantwort: \n";
		str += "Konfigurationszeit: " + _configTime + "\n";
		if(_systemObjectAnswerInfo != null) {
			str += _systemObjectAnswerInfo.parseToString();
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_configTime);
		out.writeByte(_systemObjectAnswerInfo.getAnswerType());
		_systemObjectAnswerInfo.write(out);
	}

	public final void read(DataInputStream in) throws IOException {
		_configTime = in.readLong();
		byte answerType = in.readByte();
		switch(answerType) {
			case(SystemObjectAnswerInfo.IDS_TO_OBJECTS_TYPE): {
				_systemObjectAnswerInfo = new IdsToObjectsAnswer(_dataModel);
				break;
			}
			case(SystemObjectAnswerInfo.PIDS_TO_OBJECTS_TYPE): {
				_systemObjectAnswerInfo = new PidsToObjectsAnswer(_dataModel);
				break;
			}
			case(SystemObjectAnswerInfo.TYPE_IDS_TO_OBJECTS_TYPE): {
				_systemObjectAnswerInfo = new TypeIdsToObjectsAnswer(_dataModel);
				break;
			}
		}
		if(_systemObjectAnswerInfo != null) {
			_systemObjectAnswerInfo.read(in);
		}
	}
}

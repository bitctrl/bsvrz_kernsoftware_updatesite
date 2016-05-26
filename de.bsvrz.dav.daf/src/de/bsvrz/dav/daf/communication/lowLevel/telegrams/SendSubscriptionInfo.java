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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.SenderRole;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SendSubscriptionInfo {

	/** Die Basisanmeldeinformationen */
	private BaseSubscriptionInfo _baseSubscriptionInfo;

	/** Das Sende-Quelle Flag: <br>- 0 signalisiert Sender <br>- 1 signalisiert Quelle */
	private byte _source;

	/** Das Triggerflag: <br>- 0 Applikation will keine Sendeanforderungen erhalten <br>- 1 Applikation will Sendeanforderungen erhalten */
	private byte _requestSupport;

	/** Der letzte zustand des Triggerflags */
	private byte _lastTriggerRequest = -1;

	/** Erzeugt neues Objekt ohne Parameter. Diese werden später gesetzt */
	public SendSubscriptionInfo() {
	}

	/**
	 * Erzeugt neues Objekt mit gegebenen Parametern.
	 *
	 * @param baseSubscriptionInfo Basisannmeldeinformaion
	 * @param senderRole           Senderrolle
	 * @param requestSupport       Triggerflag
	 */
	public SendSubscriptionInfo(
			BaseSubscriptionInfo baseSubscriptionInfo, SenderRole senderRole, boolean requestSupport
	) {
		_baseSubscriptionInfo = baseSubscriptionInfo;
		_source = senderRole.isSource() ? (byte)1 : (byte)0;
		_requestSupport = requestSupport ? (byte)1 : (byte)0;
	}

	/**
	 * Gibt den letzten Zustand des Triggerflags zurück.
	 *
	 * @return letzter Zustand des Triggerflags
	 */
	public final byte getLastTriggerRequest() {
		return _lastTriggerRequest;
	}

	/**
	 * Setzt den letzten Zustand des Triggerflags.
	 *
	 * @param b Zusatnd
	 */
	public final void setLastTriggerRequest(byte b) {
		_lastTriggerRequest = b;
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück
	 *
	 * @return Basisanmeldeinformation
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _baseSubscriptionInfo;
	}

	/**
	 * Gibt an, ob der Status der Applikation Sender ist und ob getriggert werden kann.
	 *
	 * @return <code>ture:</code>  Applikation ist ein Sender und es darf getriggert werden, <code>false:</code> Applikation ist kein Sender oder es darf nicht
	 *         getriggert werden.
	 */
	public final boolean isSenderRequestSupported() {
		return ((_source == 0) && (_requestSupport == 1));
	}

	/**
	 * Gibt an, ob der Status der Applikation Sender ist und ob nicht getriggert werden kann.
	 *
	 * @return <code>ture:</code>  Applikation ist ein Sender und es darf nicht getriggert werden, <code>false:</code> Applikation ist kein Sender oder es darf
	 *         getriggert werden.
	 */
	public final boolean isSenderRequestNotSupported() {
		return ((_source == 0) && (_requestSupport == 0));
	}

	/**
	 * Gibt an, ob der Status der Applikation Quelle ist und ob getriggert werden kann.
	 *
	 * @return <code>ture:</code>  Applikation ist eine Quelle und es darf getriggert werden, <code>false:</code> Applikation ist keine Quelle oder es darf nicht
	 *         getriggert werden.
	 */
	public final boolean isSourceRequestSupported() {
		return ((_source == 1) && (_requestSupport == 1));
	}

	/**
	 * Gibt an, ob der Status der Applikation Quelle ist und ob getriggert werden kann.
	 *
	 * @return <code>ture:</code>  Applikation ist eine Quelle und es darf nicht getriggert werden, <code>false:</code> Applikation ist keine Quelle oder es darf
	 *         getriggert werden.
	 */

	public final boolean isSourceRequestNotSupported() {
		return ((_source == 1) && (_requestSupport == 0));
	}

	/**
	 * Gibt an, ob der Status der Applikation Quelle ist.
	 *
	 * @return <code>true:</code> Applikation ist Quelle, <code>false:</code> Applikation ist keine Quelle
	 */
	public final boolean isSource() {
		return (_source == 1);
	}

	/**
	 * Gibt an, ob der Status der Applikation Sender ist.
	 *
	 * @return <code>true:</code> Applikation ist Sender, <code>false:</code> Applikation ist kein Sender
	 */
	public final boolean isSender() {
		return (_source == 0);
	}

	/**
	 * Gibt an, ob die Applikation getriggert werden kann.
	 *
	 * @return <code>true:</code>  Applikation kann getriggert werden, <code>false:</code> Applikation kann nicht getriggert werden
	 */

	public final boolean isRequestSupported() {
		return (_requestSupport == 1);
	}

	/**
	 * Gibt ein String zurrück, der diesen Datensatz beschreibt
	 *
	 * @return Der String, der diesen Datensatz beschreibt
	 */
	public final String parseToString() {
		String str = "Sendeanmeldeinformationen:";
		str += _baseSubscriptionInfo.toString() + "\n";
		if(_source == 1) {
			str += "Quelle ";
		}
		else {
			str += "Sender ";
		}
		if(_requestSupport == 1) {
			str += "will getriggert werden.\n";
		}
		else {
			str += "will nicht getriggert werden.\n";
		}
		return str;
	}

	/**
	 * Schreiben eines Datensatzes in das gegebene DataOutputStream
	 *
	 * @param out DataOutputStream
	 *
	 * @throws IOException wenn beim Schreiben in den  Outputstream ein Fehler auftritt
	 */
	public final void write(DataOutputStream out) throws IOException {
		_baseSubscriptionInfo.write(out);
		out.writeByte(_source);
		out.writeByte(_requestSupport);
	}

	/**
	 * Lesen eines Datensatzes vom gegebenen DataInputStream
	 *
	 * @param in DataInputStream
	 *
	 * @throws IOException wenn beim Lesen des Inputstream ein Fehler auftritt
	 */
	public final void read(DataInputStream in) throws IOException {
		_baseSubscriptionInfo = new BaseSubscriptionInfo();
		_baseSubscriptionInfo.read(in);
		_source = in.readByte();
		_requestSupport = in.readByte();
	}

	/**
	 * Gibt die Länge des Telegramms zurück
	 *
	 * @return die Länge des Telegramms
	 */
	public short getLength() {
		return 16;
	}

	@Override
	public String toString() {
		return "SendSubscriptionInfo{" + "_baseSubscriptionInfo=" + _baseSubscriptionInfo + ", _source=" + _source + ", _requestSupport=" + _requestSupport
		       + ", _lastTriggerRequest=" + _lastTriggerRequest + '}';
	}
}

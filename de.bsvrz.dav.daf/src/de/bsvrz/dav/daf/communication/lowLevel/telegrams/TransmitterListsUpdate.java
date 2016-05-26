/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implementierung von Anmeldelistentelegrammen, die zwischen Datenverteilern zum Austausch von Informationen über angemeldete Objekte, Attributgruppen und
 * Aspekte verwendet werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterListsUpdate extends DataTelegram {

	/** Objekt-ID des Datenverteilers, zu dem Informationen für die Objekt- und Attributgruppenliste übertragen wird. */
	private long transmitterId;

	/**
	 * Deltaindikator, der anzeigt, ob die komplette Objekt- und Attributgruppenliste für den Datenverteiler übermittelt wird [0] oder ob nur Änderungen zu den
	 * Listen übertragen werden [1].
	 */
	private boolean delta;

	/** Die hinzugekommenden Objekte */
	private long objectsToAdd[];

	/** Die zu entfernenden Objekte */
	private long objectsToRemove[];

	/** Die hinzugekommenden Kombinationen aus Attributgruppen und Aspekten */
	private AttributeGroupAspectCombination attributeGroupAspectsToAdd[];

	/** Die zu entfernenden Kombinationen aus Attributgruppen und Aspekten */
	private AttributeGroupAspectCombination attributeGroupAspectsToRemove[];

	private static Debug _debug = Debug.getLogger();

	/**
	 * Returns a string representation of the object. In general, the <code>toString</code> method returns a string that "textually represents" this object. The
	 * result should be a concise but informative representation that is easy for a person to read. It is recommended that all subclasses override this method.
	 * <p>
	 * The <code>toString</code> method for class <code>Object</code> returns a string consisting of the name of the class of which the object is an instance, the
	 * at-sign character `<code>@</code>', and the unsigned hexadecimal representation of the hash code of the object. In other words, this method returns a string
	 * equal to the value of: <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "TransmitterListsUpdate{dav:" + transmitterId + ", " + (delta ? "delta" : "initial")
		       + (objectsToAdd == null ? "" : (", +obj: " + objectsToAdd.length)) + (objectsToRemove == null ? "" : (", -obj:" + objectsToRemove.length))
		       + (attributeGroupAspectsToAdd == null ? "" : (", +atgAsp:" + attributeGroupAspectsToAdd.length))
		       + (attributeGroupAspectsToRemove == null ? "" : (", -atgAsp:" + attributeGroupAspectsToRemove.length)) + "}";
	}

	public TransmitterListsUpdate(byte type) {
		this.type = type;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	public TransmitterListsUpdate(
			long _transmitterId,
			boolean _delta,
			long _objectsToAdd[],
			long _objectsToRemove[],
			AttributeGroupAspectCombination _attributeGroupAspectsToAdd[],
			AttributeGroupAspectCombination _attributeGroupAspectsToRemove[]
	) {
		
		type = TRANSMITTER_LISTS_UPDATE_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;

		delta = _delta;
		transmitterId = _transmitterId;
		objectsToAdd = _objectsToAdd;
		objectsToRemove = _objectsToRemove;
		attributeGroupAspectsToAdd = _attributeGroupAspectsToAdd;
		attributeGroupAspectsToRemove = _attributeGroupAspectsToRemove;
		length = 17;
		if(objectsToAdd != null) {
			length += (8 * objectsToAdd.length);
		}
		if(objectsToRemove != null) {
			length += (8 * objectsToRemove.length);
		}
		if(attributeGroupAspectsToAdd != null) {
			if(type == TRANSMITTER_LISTS_UPDATE_2_TYPE) {
				length += (8 * attributeGroupAspectsToAdd.length);
			}
			else {
				length += (4 * attributeGroupAspectsToAdd.length);
			}
		}
		if(attributeGroupAspectsToRemove != null) {
			if(type == TRANSMITTER_LISTS_UPDATE_2_TYPE) {
				length += (8 * attributeGroupAspectsToRemove.length);
			}
			else {
				length += (4 * attributeGroupAspectsToRemove.length);
			}
		}
	}

	/**
	 * Gibt die Id des lieferanten Datenverteilers zurück
	 *
	 * @return die Id des lieferanten Datenverteilers
	 */
	public final long getTransmitterId() {
		return transmitterId;
	}

	/**
	 * Gibt zurück, ob nur die Änderungen oder alle Informationen übertragen werden
	 *
	 * @return <code>true</code>, wenn nur Änderungen, <code>false</code>, wenn alle Informationen übertragen werden
	 */
	public final boolean isDeltaMessage() {
		return delta;
	}

	/**
	 * Gibt die hinzugekommenden Objekten zurück
	 *
	 * @return die hinzugekommenden Objekten
	 */
	public final long[] getObjectsToAdd() {
		return objectsToAdd;
	}

	/**
	 * Gibt die zu entfernenden Objekten zurück
	 *
	 * @return die zu entfernenden Objekten
	 */
	public final long[] getObjectsToRemove() {
		return objectsToRemove;
	}

	/**
	 * Gibt die hinzugekommenden Kombinationen der Attributesgruppen und Aspkten zurück
	 *
	 * @return die hinzugekommenden Kombinationen
	 */
	public final AttributeGroupAspectCombination[] getAttributeGroupAspectsToAdd() {
		return attributeGroupAspectsToAdd;
	}

	/**
	 * Gibt die zu entfernenden Kombinationen der Attributesgruppen und Aspkten zurück
	 *
	 * @return die zu entfernenden Kombinationen
	 */
	public final AttributeGroupAspectCombination[] getAttributeGroupAspectsToRemove() {
		return attributeGroupAspectsToRemove;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Datenverteileranmeldelisten Aktuallisierung:\n";
		str += "Lieferant Datenverteiler-Id: " + transmitterId + "\n";
		str += "Änderungen Flage: " + delta + "\n";
		if(objectsToAdd != null) {
			str += "Hinzugekommende Objekte: [ ";
			for(int i = 0; i < objectsToAdd.length; ++i) {
				str += " " + objectsToAdd[i] + " ";
			}
			str += " ]\n";
		}
		if(objectsToRemove != null) {
			str += "Gelöschte Objekte: [ ";
			for(int i = 0; i < objectsToRemove.length; ++i) {
				str += " " + objectsToRemove[i] + " ";
			}
			str += " ]\n";
		}
		if(attributeGroupAspectsToAdd != null) {
			str += "Hinzugekommende Kombinationen der AG-A: [ ";
			for(int i = 0; i < attributeGroupAspectsToAdd.length; ++i) {
				str += " " + attributeGroupAspectsToAdd[i] + " ";
			}
			str += " ]\n";
		}
		if(attributeGroupAspectsToRemove != null) {
			str += "Gelöschte Kombinationen der AG-A: [ ";
			for(int i = 0; i < attributeGroupAspectsToRemove.length; ++i) {
				str += " " + attributeGroupAspectsToRemove[i] + " ";
			}
			str += " ]\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		_debug.fine("Anmeldelistentelegramm versenden: ", this);
		out.writeShort(length);
		out.writeBoolean(delta);
		out.writeLong(transmitterId);
		if(objectsToAdd == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(objectsToAdd.length);
			for(int i = 0; i < objectsToAdd.length; ++i) {
				out.writeLong(objectsToAdd[i]);
			}
		}
		if(objectsToRemove == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(objectsToRemove.length);
			for(int i = 0; i < objectsToRemove.length; ++i) {
				out.writeLong(objectsToRemove[i]);
			}
		}
		if(attributeGroupAspectsToAdd == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(attributeGroupAspectsToAdd.length);
			for(int i = 0; i < attributeGroupAspectsToAdd.length; ++i) {
				attributeGroupAspectsToAdd[i].write(out);
			}
		}
		if(attributeGroupAspectsToRemove == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(attributeGroupAspectsToRemove.length);
			for(int i = 0; i < attributeGroupAspectsToRemove.length; ++i) {
				attributeGroupAspectsToRemove[i].write(out);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		delta = in.readBoolean();
		transmitterId = in.readLong();
		length = 17;
		int size = in.readShort();
		if(size > 0) {
			objectsToAdd = new long[size];
			for(int i = 0; i < size; ++i) {
				objectsToAdd[i] = in.readLong();
			}
			length += (8 * size);
		}
		size = in.readShort();
		if(size > 0) {
			objectsToRemove = new long[size];
			for(int i = 0; i < size; ++i) {
				objectsToRemove[i] = in.readLong();
			}
			length += (8 * size);
		}
		size = in.readShort();
		if(size > 0) {
			attributeGroupAspectsToAdd = new AttributeGroupAspectCombination[size];
			for(int i = 0; i < size; ++i) {
				attributeGroupAspectsToAdd[i] = new AttributeGroupAspectCombination();
				attributeGroupAspectsToAdd[i].read(in);
			}
			if(type == TRANSMITTER_LISTS_UPDATE_2_TYPE) {
				length += (8 * size);
			}
			else {
				length += (4 * size);
			}
		}
		size = in.readShort();
		if(size > 0) {
			attributeGroupAspectsToRemove = new AttributeGroupAspectCombination[size];
			for(int i = 0; i < size; ++i) {
				attributeGroupAspectsToRemove[i] = new AttributeGroupAspectCombination();
				attributeGroupAspectsToRemove[i].read(in);
			}
			if(type == TRANSMITTER_LISTS_UPDATE_2_TYPE) {
				length += (8 * size);
			}
			else {
				length += (4 * size);
			}
		}
		_debug.fine("Anmeldelistentelegramm empfangen: ", this);
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge, erwartet " + length + " empfangen " + _length);
		}
	}
}

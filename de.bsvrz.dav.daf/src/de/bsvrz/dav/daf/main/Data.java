/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Schnittstelle zum generischen Zugriff auf Attributwerte in beliebig strukturierten Attributgruppen. Zum Erzeugen eines neuen Datensatzes kann die Methode
 * {@link ClientDavInterface#createData} benutzt werden. Nach dem Empfang von Daten kann mit der Methode {@link ResultData#getData} der im Ergebnis enthaltene
 * Datensatz abgefragt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8339 $
 */
public interface Data extends Iterable<Data> {

	/**
	 * Liefert eine modifizierbare Kopie eines Datensatzes zur�ck. Diese Methode kann verwendet werden, um eine Kopie von einem empfangenen (nicht modifizierbaren)
	 * Datensatz zu erstellen und die Kopie mit eventuell ver�nderten Attributwerten wieder zu versenden. Die Methode funktioniert i.a. nur auf ganzen Datens�tzen.
	 * Bei Data-Objekten, die nur Teile oder einzelne Attributwerte repr�sentieren wird eine IllegalStateException generiert.
	 *
	 * @return Ver�nderbare Kopie des Datensatzes.
	 *
	 * @throws IllegalStateException, wenn das Data-Objekt keinen ganzen Datensatz enth�lt.
	 */
	Data createModifiableCopy();

	/**
	 * Liefert eine nicht modifizierbare Kopie eines Datensatzes zur�ck. Die Methode funktioniert i.a. nur auf ganzen Datens�tzen. Bei Data-Objekten, die nur Teile
	 * oder einzelne Attributwerte repr�sentieren wird eine IllegalStateException generiert.
	 *
	 * @return Nicht �nderbare Kopie des Datensatzes.
	 *
	 * @throws IllegalStateException, wenn das Data-Objekt keinen ganzen Datensatz enth�lt.
	 */
	Data createUnmodifiableCopy();

	/**
	 * Liefert den Namen eines Datums zur�ck.
	 *
	 * @return Name des Datums
	 */
	String getName();

	/**
	 * Liefert eine textliche Darstellung des Werts eines Datums zur�ck.
	 *
	 * @return Wert des Datums
	 */
	public String valueToString();

	/**
	 * Liefert eine textliche Darstellung des Datums mit Name und Wert.
	 *
	 * @return Name und Wert des Datums
	 */
	public String toString();

	/**
	 * Liefert den Attribut-Typ eines Datums zur�ck.
	 *
	 * @return Attribut-Typ des Datums
	 */
	AttributeType getAttributeType();

	/**
	 * Pr�ft, ob das Datum �ber den Datenverteiler verschickt werden kann. Die Methode gibt <code>true</code> zur�ck, wenn jedes Attribut einen Wert besitzt, der
	 * ungleich dem "undefiniert" Wert ist. Ist das Datum ein Array oder eine Liste, so wird jedes Element gepr�ft ob es einen g�ltigen Wert besitzt. Die Pr�fung
	 * findet auf alle "Sub-Daten" des Datums statt.
	 *
	 * @return true = Das Datum enth�lt g�ltige Werte und kann �ber den Datenverteiler verschickt werden; false = Das Datum enth�lt mindestens ein Attribut, das
	 *         einen undefinierten Wert besitzt und kann somit nicht �ber den Datenverteiler verschickt werden
	 */
	boolean isDefined();

	/**
	 * Setzt bei einem Datum alle Werte auf die definierte Default-Werte. Wurde weder beim Attribut noch beim Attributtyp ein Default-Wert definiert so wird der
	 * "undefiniert" Wert gesetzt. Ist das Datum eine Liste oder ein Array, so wird bei jedem Element der Default-Wert gesetzt. Der Vorgang wird solange
	 * fortgesetzt, bis alle "Sub-Daten" mit einem Default-Wert/Undefiniert-Wert definiert sind.
	 *
	 * @see #isDefined()
	 */
	void setToDefault();

	/**
	 * Pr�ft, ob das Datum eine Liste ist, d.h. aus Sub-Daten besteht.
	 *
	 * @return <code>true</code>, falls das Datum eine Liste ist, sonst <code>false</code>.
	 */
	boolean isList();

	/**
	 * Pr�ft, ob das Datum ein Array ist.
	 *
	 * @return <code>true</code>, falls das Datum ein Array ist, sonst <code>false</code>.
	 */
	boolean isArray();

	/**
	 * Pr�ft, ob das Datum ein einfaches Datum ohne untergeordnete Sub-Daten ist.
	 *
	 * @return <code>true</code>, falls das Datum eine einfaches Datum ist, sonst <code>false</code>.
	 */
	boolean isPlain();

	/**
	 * Bestimmt das Sub-Datum dieser Liste mit dem angegebenen Namen.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Gew�nschtes Sub-Datum.
	 */
	Data getItem(String itemName);

	/**
	 * Liefert eine Text-Ansicht auf dieses einfache Datum zur�ck.
	 *
	 * @return Text-Ansicht des Datums.
	 */
	Data.TextValue asTextValue();

	/**
	 * Liefert eine Text-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Text-Ansicht des Sub-Datums.
	 */
	Data.TextValue getTextValue(String itemName);

	/**
	 * Liefert eine Zeit-Ansicht auf dieses einfache Datum zur�ck.
	 *
	 * @return Zeit-Ansicht des Datums.
	 */
	Data.TimeValue asTimeValue();

	/**
	 * Liefert eine Zeit-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Zeit-Ansicht des Sub-Datums.
	 */
	Data.TimeValue getTimeValue(String itemName);

	/**
	 * Liefert eine Skalierte-Ansicht auf dieses einfache Datum zur�ck.
	 *
	 * @return Skalierte-Ansicht des Datums.
	 */
	Data.NumberValue asScaledValue();

	/**
	 * Liefert eine Skalierte-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Skalierte-Ansicht des Sub-Datums.
	 */
	Data.NumberValue getScaledValue(String itemName);

	/**
	 * Liefert eine Unskalierte-Ansicht auf dieses einfache Datum zur�ck.
	 *
	 * @return Unskalierte-Ansicht des Datums.
	 */
	Data.NumberValue asUnscaledValue();

	/**
	 * Liefert eine Unskalierte-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Unskalierte-Ansicht des Sub-Datums.
	 */
	Data.NumberValue getUnscaledValue(String itemName);

	/**
	 * Liefert eine Referenz-Ansicht auf dieses einfache Datum zur�ck.
	 *
	 * @return Referenz-Ansicht des Datums.
	 */
	Data.ReferenceValue asReferenceValue();

	/**
	 * Liefert eine Referenz-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Referenz-Ansicht des Sub-Datums.
	 */
	Data.ReferenceValue getReferenceValue(String itemName);

	/**
	 * Liefert eine Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Array-Ansicht des Datums.
	 */
	Data.Array asArray();

	/**
	 * Liefert eine Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Array-Ansicht des Sub-Datums.
	 */
	Data.Array getArray(String itemName);

	/**
	 * Liefert eine Text-Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Text-Array-Ansicht des Datums.
	 */
	Data.TextArray asTextArray();

	/**
	 * Liefert eine Text-Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Text-Array-Ansicht des Sub-Datums.
	 */
	Data.TextArray getTextArray(String itemName);

	/**
	 * Liefert eine Time-Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Time-Array-Ansicht des Datums.
	 */
	Data.TimeArray asTimeArray();

	/**
	 * Liefert eine Time-Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Time-Array-Ansicht des Sub-Datums.
	 */
	Data.TimeArray getTimeArray(String itemName);

	/**
	 * Liefert eine Skalierte-Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Skalierte-Array-Ansicht des Datums.
	 */
	Data.NumberArray asScaledArray();

	/**
	 * Liefert eine Skalierte-Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Skalierte-Array-Ansicht des Sub-Datums.
	 */
	Data.NumberArray getScaledArray(String itemName);

	/**
	 * Liefert eine Unskalierte-Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Unskalierte-Array-Ansicht des Datums.
	 */
	Data.NumberArray asUnscaledArray();

	/**
	 * Liefert eine Unskalierte-Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Unskalierte-Array-Ansicht des Sub-Datums.
	 */
	Data.NumberArray getUnscaledArray(String itemName);

	/**
	 * Liefert eine Referenz-Array-Ansicht auf dieses Array zur�ck.
	 *
	 * @return Referenz-Array-Ansicht des Datums.
	 */
	Data.ReferenceArray asReferenceArray();

	/**
	 * Liefert eine Referenz-Array-Ansicht auf das Sub-Datum dieser Liste mit dem angegebenen Namen zur�ck.
	 *
	 * @param itemName Name des gew�nschten Sub-Datums.
	 *
	 * @return Referenz-Array-Ansicht des Sub-Datums.
	 */
	Data.ReferenceArray getReferenceArray(String itemName);

	/**
	 * Liefert einen Iterator zur�ck, der �ber die Sub-Daten dieser Liste oder die Element-Daten dieses Arrays iteriert.
	 *
	 * @return Iterator �ber die Sub-Daten dieser Liste oder die Element-Daten dieses Arrays.
	 */
	Iterator<Data> iterator();

	/** Interface-Klasse f�r die Text-Ansicht eines Datums. */
	public static interface TextValue {

		/**
		 * Bestimmt den Wert des Datums als Text einschlie�lich Zusatz (wie zum Beispiel Einheit).
		 *
		 * @return Wert des Datums mit Zusatz (wie zum Beispiel Einheit) als Text.
		 */
		String getText();

		/**
		 * Bestimmt den Wert des Datums als Text ohne Zusatz (wie zum Beispiel Einheit).
		 *
		 * @return Wert des Datums ohne Zusatz (wie zum Beispiel Einheit) als Text.
		 */
		String getValueText();

		/**
		 * Bestimmt den Zusatztext (wie zum Beispiel Einheit) des Datums.
		 *
		 * @return Zusatztext des Datums (wie zum Beispiel Einheit).
		 */
		String getSuffixText();

		/**
		 * Setzt den Wert des Datums auf den im �bergebenen Text enthalten Wert.
		 *
		 * @param text Text mit dem zu setzenden Wert.
		 */
		void setText(String text);
	}

	/** Interface-Klasse f�r die Zeit-Ansicht eines Datums. */
	public static interface TimeValue extends TextValue {

		/**
		 * Bestimmt die im Datum enthaltene Zeit in Sekunden.
		 *
		 * @return Zeit in Sekunden
		 */
		long getSeconds();

		/**
		 * Bestimmt die im Datum enthaltene Zeit in Millisekunden.
		 *
		 * @return Zeit in Millisekunden
		 */
		long getMillis();

		/**
		 * Setzt die im Datum enthaltene Zeit auf den angegebenen Wert.
		 *
		 * @param seconds Zeit in Sekunden
		 */
		void setSeconds(long seconds);

		/**
		 * Setzt die im Datum enthaltene Zeit auf den angegebenen Wert.
		 *
		 * @param milliSeconds Zeit in Milliekunden
		 */
		void setMillis(long milliSeconds);
	}

	/** Interface-Klasse f�r die Referenz-Ansicht eines Datums. */
	public static interface ReferenceValue extends TextValue {

		/**
		 * Bestimmt die Objekt-Id der im Datum enthaltene Referenz.
		 *
		 * @return Objekt-Id des referenzierten Objekts oder der Wert 0, wenn kein Objekt referenziert wird.
		 */
		long getId();

		/**
		 * Bestimmt das durch dieses Datum referenzierte System-Objekt.
		 *
		 * @return Stellvertreter-Objekt des referenzierten System-Objekts oder <code>null</code>, wenn kein Objekt referenziert wird.
		 */
		SystemObject getSystemObject();

		/**
		 * Setzt das durch dieses Datum referenzierte System-Objekt.
		 *
		 * @param object Stellvertreter-Objekt des referenzierten System-Objekts oder <code>null</code>, wenn kein Objekt referenziert werden soll.
		 */
		void setSystemObject(SystemObject object);

		/**
		 * Setzt das durch dieses Datum referenzierte System-Objekt.
		 *
		 * @param objectPid Pid des referenzierten Objekts oder "", falls kein Objekt referenziert werden soll.
		 * @param datamodel DataModel-Objekt mit dem das Systemobjekt aus der Pid bestimmt werden soll.
		 */
		void setSystemObjectPid(String objectPid, ObjectLookup datamodel);

		/**
		 * Setzt das durch dieses Datum referenzierte System-Objekt.
		 *
		 * @param objectPid Pid des referenzierten Objekts oder "", falls kein Objekt referenziert werden soll.
		 */
		void setSystemObjectPid(String objectPid);		

		/**
		 * Bestimmt die Pid des durch dieses Datum referenzierten System-Objekts.
		 *
		 * @return Pid des referenzierten Objekts oder "" falls kein Objekt referenziert wird.
		 */
		String getSystemObjectPid();
	}

	/** Interface-Klasse f�r die Skalierte- und Unskalierte-Ansicht eines Datums. */
	public static interface NumberValue extends TextValue {

		/**
		 * Bestimmt, ob der aktuelle Wert des Datums durch einen Zahlwert dargestellt werden kann. In einer unskalierten Ansicht kann jeder g�ltige Wert des Datums
		 * als Zahlwert dargestellt werden. In einer skalierten Ansicht von Ganzzahlattributen werden nur Werte innerhalb des definierten Wertebereichs als Zahlwerte
		 * entsprechend des Skalierungsfaktors dargestellt.
		 *
		 * @return <code>true</code>, wenn der Wert aktuelle Wert des Datums durch einen Zahlwert dargestellt werden kann, sonst <code>false</code>.
		 */
		boolean isNumber();

		/**
		 * Bestimmt, ob der aktuelle Wert des Datums durch einen Wertezustand abgebildet werden kann.
		 *
		 * @return <code>true</code>, wenn der Wert aktuelle Wert des Datums durch einen Wertezustand abgebildet werden kann, sonst <code>false</code>.
		 */
		boolean isState();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>byte</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		byte byteValue();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>short</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		short shortValue();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>int</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		int intValue();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>long</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		long longValue();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>float</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		float floatValue();

		/**
		 * Liefert den Zahl-Wert des Datums in einem <code>double</code> zur�ck.
		 *
		 * @return Wert des Datums
		 */
		double doubleValue();

		/**
		 * Liefert den Wert eines Datums als Werte-Zustand zur�ck.
		 *
		 * @return Werte-Zustand oder <code>null</code>, wenn der Wert keinem Wertezustand entspricht.
		 */
		IntegerValueState getState();

		/**
		 * Setzt das Datum auf den im Werte-Zustand definierten Wert.
		 *
		 * @param state Werte-Zustand
		 */
		void setState(IntegerValueState state);

		/**
		 * Setzt den Wert des Datum auf den angegebenen Wert.
		 *
		 * @param value Zu setzender Wert
		 */
		void set(int value);

		/**
		 * Setzt den Wert des Datum auf den angegebenen Wert.
		 *
		 * @param value Zu setzender Wert
		 */
		void set(long value);

		/**
		 * Setzt den Wert des Datum auf den angegebenen Wert.
		 *
		 * @param value Zu setzender Wert
		 */
		void set(float value);

		/**
		 * Setzt den Wert des Datum auf den angegebenen Wert.
		 *
		 * @param value Zu setzender Wert
		 */
		void set(double value);
	}

	/** Interface-Klasse f�r die Array-Ansicht eines Datums. */
	public static interface Array {

		/**
		 * Bestimmt, ob die Gr��e dieses Arrays durch eine Obergrenze beschr�nkt ist.
		 *
		 * @return <code>true</code>, wenn die Anzahl der Werte beschr�nkt ist;<br/> <code>false</code>, wenn die Anzahl der Werte nicht beschr�nkt ist.
		 */
		public boolean isCountLimited();

		/**
		 * Bestimmt, ob die Gr��e dieses Arrays variieren kann.
		 *
		 * @return <code>true</code>, wenn die Anzahl der Werte dieses Arrays mit jedem Datensatz variieren kann;<br/> <code>false</code>, wenn die Anzahl der Werte
		 *         fix ist.
		 */
		public boolean isCountVariable();

		/**
		 * Bestimmt, die maximale Gr��e dieses Arrays. Wenn die Gr��e des Arrays nicht beschr�nkt ist, wird der Wert 0 zur�ckgegeben. Wenn die Gr��e nicht variabel
		 * als fest ist, wird die erforderliche Gr��e zur�ckgegeben.
		 *
		 * @return Maximale oder erforderliche Gr��e des Arrays oder 0, wenn die Gr��e nicht beschr�nkt ist.
		 */
		public int getMaxCount();

		/**
		 * Liefert die Anzahl der im Array enthaltenen Elemente zur�ck.
		 *
		 * @return Anzahl der enthaltenen Elemente.
		 */
		int getLength();

		/**
		 * Definiert die Anzahl der im Array enthaltenen Elemente.
		 *
		 * @param newLength Neue Anzahl Elemente im Array.
		 */
		void setLength(int newLength);

		/**
		 * Bestimmt das Sub-Datum dieses Arrays mit dem angegebenen Index.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Gew�nschtes Sub-Datum.
		 */
		Data getItem(int itemIndex);

		/**
		 * Liefert eine Text-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Text-Ansicht des Sub-Datums.
		 */
		Data.TextValue getTextValue(int itemIndex);

		/**
		 * Liefert ein Array von Text-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Text-Ansichten der enthaltene Elemente.
		 */
		Data.TextValue[] getTextValues();

		/**
		 * Liefert eine Text-Array-Ansicht dieses Arrays zur�ck.
		 *
		 * @return Text-Array-Ansicht des Arrays.
		 */
		Data.TextArray asTextArray();

		/**
		 * Liefert eine Zeit-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Zeit-Ansicht des Sub-Datums.
		 */
		Data.TimeValue getTimeValue(int itemIndex);

		/**
		 * Liefert ein Array von Zeit-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Zeit-Ansichten der enthaltene Elemente.
		 */
		Data.TimeValue[] getTimeValues();

		/**
		 * Liefert eine Zeit-Array-Ansicht dieses Arrays zur�ck.
		 *
		 * @return Zeit-Array-Ansicht des Arrays.
		 */
		Data.TimeArray asTimeArray();

		/**
		 * Liefert eine Skalierte-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Skalierte-Ansicht des Sub-Datums.
		 */
		Data.NumberValue getScaledValue(int itemIndex);

		/**
		 * Liefert ein Array von Skalierte-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Skalierte-Ansichten der enthaltene Elemente.
		 */
		Data.NumberValue[] getScaledValues();

		/**
		 * Liefert eine Skalierte-Array-Ansicht dieses Arrays zur�ck.
		 *
		 * @return Skalierte-Array-Ansicht des Arrays.
		 */
		Data.NumberArray asScaledArray();

		/**
		 * Liefert eine Unskalierte-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Unskalierte-Ansicht des Sub-Datums.
		 */
		Data.NumberValue getUnscaledValue(int itemIndex);

		/**
		 * Liefert ein Array von Unskalierte-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Unskalierte-Ansichten der enthaltene Elemente.
		 */
		Data.NumberValue[] getUnscaledValues();

		/**
		 * Liefert eine Unskalierte-Array-Ansicht dieses Arrays zur�ck.
		 *
		 * @return Unskalierte-Array-Ansicht des Arrays.
		 */
		Data.NumberArray asUnscaledArray();

		/**
		 * Liefert eine Referenz-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Referenz-Ansicht des Sub-Datums.
		 */
		Data.ReferenceValue getReferenceValue(int itemIndex);

		/**
		 * Liefert ein Array von Referenz-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Referenz-Ansichten der enthaltene Elemente.
		 */
		Data.ReferenceValue[] getReferenceValues();

		/**
		 * Liefert eine Referenz-Array-Ansicht dieses Arrays zur�ck.
		 *
		 * @return Referenz-Array-Ansicht des Arrays.
		 */
		Data.ReferenceArray asReferenceArray();


	}

	/** Interface-Klasse f�r die Text-Array-Ansicht eines Datums. */
	public static interface TextArray {

		/**
		 * Liefert die Anzahl der im Array enthaltenen Elemente zur�ck.
		 *
		 * @return Anzahl der enthaltenen Elemente.
		 */
		int getLength();

		/**
		 * Definiert die Anzahl der im Array enthaltenen Elemente.
		 *
		 * @param newLength Neue Anzahl Elemente im Array.
		 */
		void setLength(int newLength);

		/**
		 * Liefert eine Text-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Text-Ansicht des Sub-Datums.
		 */
		Data.TextValue getTextValue(int itemIndex);

		/**
		 * Liefert ein Array von Text-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Text-Ansichten der enthaltene Elemente.
		 */
		Data.TextValue[] getTextValues();

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param strings Array mit den zu �bernehmenden Werten
		 */
		void set(String... strings);		

		/**
		 * Bestimmt den Wert des Sub-Datums dieses Arrays mit dem angegebenen Index als Text einschlie�lich Zusatz (wie zum Beispiel Einheit).
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums mit Zusatz (wie zum Beispiel Einheit) als Text.
		 */
		String getText(int itemIndex);

		/**
		 * Liefert ein Array mit den Werten aller Elemente dieses Arrays als Text einschlie�lich Zusatz (wie zum Beispiel Einheit).
		 *
		 * @return Array von Textwerten einschlie�lich Zusatz (wie zum Beispiel Einheit).
		 */
		String[] getTextArray();
	}

	/** Interface-Klasse f�r die Zeit-Array-Ansicht eines Datums. */
	public static interface TimeArray {

		/**
		 * Liefert die Anzahl der im Array enthaltenen Elemente zur�ck.
		 *
		 * @return Anzahl der enthaltenen Elemente.
		 */
		int getLength();

		/**
		 * Definiert die Anzahl der im Array enthaltenen Elemente.
		 *
		 * @param newLength Neue Anzahl Elemente im Array.
		 */
		void setLength(int newLength);

		/**
		 * Liefert eine Zeit-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Zeit-Ansicht des Sub-Datums.
		 */
		Data.TimeValue getTimeValue(int itemIndex);

		/**
		 * Liefert ein Array von Zeit-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Zeit-Ansichten der enthaltene Elemente.
		 */
		Data.TimeValue[] getTimeValues();

		/**
		 * Bestimmt den Wert des Sub-Datums dieses Arrays mit dem angegebenen Index als Zeit in Sekunden.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums als Zeit in Sekunden.
		 */
		long getSeconds(int itemIndex);

		/**
		 * Bestimmt den Wert des Sub-Datums dieses Arrays mit dem angegebenen Index als Zeit in Millisekunden.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums als Zeit in Millisekunden.
		 */
		long getMillis(int itemIndex);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 *
		 * @param millis Array mit den zu �bernehmenden Werten
		 */
		void setMillis(long... millis);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 *
		 * @param seconds Array mit den zu �bernehmenden Werten
		 */
		void setSeconds(long... seconds);

		/**
		 * Liefert ein Array mit den Werten aller Elemente dieses Arrays als Zeit in Sekunden zur�ck.
		 *
		 * @return Array von Zeitwerten in Sekunden.
		 */
		long[] getSecondsArray();

		/**
		 * Liefert ein Array mit den Werten aller Elemente dieses Arrays als Zeit in Millisekunden zur�ck.
		 *
		 * @return Array von Zeitwerten in Millisekunden.
		 */
		long[] getMillisArray();
	}

	/** Interface-Klasse f�r die Referenz-Array-Ansicht eines Datums. */
	public static interface ReferenceArray {

		/**
		 * Liefert die Anzahl der im Array enthaltenen Elemente zur�ck.
		 *
		 * @return Anzahl der enthaltenen Elemente.
		 */
		int getLength();

		/**
		 * Definiert die Anzahl der im Array enthaltenen Elemente.
		 *
		 * @param newLength Neue Anzahl Elemente im Array.
		 */
		void setLength(int newLength);

		/**
		 * Liefert eine Referenz-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Referenz-Ansicht des Sub-Datums.
		 */
		Data.ReferenceValue getReferenceValue(int itemIndex);

		/**
		 * Liefert ein Array von Referenz-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Referenz-Ansichten der enthaltene Elemente.
		 */
		Data.ReferenceValue[] getReferenceValues();

		/**
		 * Bestimmt das durch das Sub-Datum dieses Arrays mit dem angegebenen Index referenzierte System-Objekt.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Stellvertreter-Objekt des referenzierten System-Objekts oder <code>null</code>, wenn kein Objekt referenziert wird.
		 */
		SystemObject getSystemObject(int itemIndex);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param systemObjects Array mit den zu �bernehmenden Werten
		 */
		void set(SystemObject... systemObjects);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param systemObjectPids Array mit den zu �bernehmenden Werten
		 */
		void set(String... systemObjectPids);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param dataModel Objekt das zur Ermittlung der Systemobjekte benutzt wird
		 * @param systemObjectPids Array mit den zu �bernehmenden Werten
		 */
		void set(ObjectLookup dataModel, String... systemObjectPids);

		/**
		 * Liefert die durch die Elemente dieses Arrays referenzierten System-Objekte zur�ck.
		 *
		 * @return Array mit Stellvertreter-Objekten der referenzierten System-Objekte.
		 */
		SystemObject[] getSystemObjectArray();
	}

	/** Interface-Klasse f�r die Skalierte- und Unskalierte-Array-Ansicht eines Datums. */
	public static interface NumberArray {

		/**
		 * Liefert die Anzahl der im Array enthaltenen Elemente zur�ck.
		 *
		 * @return Anzahl der enthaltenen Elemente.
		 */
		int getLength();

		/**
		 * Definiert die Anzahl der im Array enthaltenen Elemente.
		 *
		 * @param newLength Neue Anzahl Elemente im Array.
		 */
		void setLength(int newLength);

		/**
		 * Liefert eine Zahl-Ansicht auf das Sub-Datum dieses Arrays mit dem angegebenen Index zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Zahl-Ansicht des Sub-Datums.
		 */
		Data.NumberValue getValue(int itemIndex);

		/**
		 * Liefert ein Array von Zahl-Ansichten auf alle in diesem Array enthaltenen Elemente zur�ck.
		 *
		 * @return Skalierte-Ansichten der enthaltene Elemente.
		 */
		Data.NumberValue[] getValues();

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>byte</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		byte byteValue(int itemIndex);

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>short</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		short shortValue(int itemIndex);

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>int</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		int intValue(int itemIndex);

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>long</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		long longValue(int itemIndex);

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>float</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		float floatValue(int itemIndex);

		/**
		 * Liefert den Wert des Sub-Datum dieses Arrays mit dem angegebenen Index in einem <code>double</code> zur�ck.
		 *
		 * @param itemIndex Index des gew�nschten Sub-Datums.
		 *
		 * @return Wert des Sub-Datums
		 */
		double doubleValue(int itemIndex);

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>byte</code> zur�ck.
		 *
		 * @return Array mit <code>byte</code>-Werten.
		 */
		byte[] getByteArray();

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param bytes Array mit den zu �bernehmenden Werten
		 */
		void set(byte... bytes);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param shorts Array mit den zu �bernehmenden Werten
		 */
		void set(short... shorts);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param ints Array mit den zu �bernehmenden Werten
		 */
		void set(int... ints);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param longs Array mit den zu �bernehmenden Werten
		 */
		void set(long... longs);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param floats Array mit den zu �bernehmenden Werten
		 */
		void set(float... floats);

		/**
		 * Setzt die L�nge und die Werte dieses Arrays auf die L�nge und Werte des �bergebenen Arrays
		 * @param doubles Array mit den zu �bernehmenden Werten
		 */
		void set(double... doubles);

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>short</code> zur�ck.
		 *
		 * @return Array mit <code>short</code>-Werten.
		 */
		short[] getShortArray();

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>int</code> zur�ck.
		 *
		 * @return Array mit <code>int</code>-Werten.
		 */
		int[] getIntArray();

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>long</code> zur�ck.
		 *
		 * @return Array mit <code>long</code>-Werten.
		 */
		long[] getLongArray();

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>float</code> zur�ck.
		 *
		 * @return Array mit <code>float</code>-Werten.
		 */
		float[] getFloatArray();

		/**
		 * Liefert die Werte aller Elemente dieses Arrays als <code>double</code> zur�ck.
		 *
		 * @return Array mit <code>double</code>-Werten.
		 */
		double[] getDoubleArray();


	}

	/** Ausnahme, die ein fehlerhaften Zugriff oder eine nicht zugelassene Konvertierung von Elementen eines Datensatzes signalisiert. */
	public static class FormatException extends RuntimeException {

		/**
		 * Erzeugt eine neue Ausnahme mit der angegebenen Detailmeldung.
		 *
		 * @param message Detailmeldung der Ausnahme.
		 */
		FormatException(String message) {
			super(message);
		}
	}
}


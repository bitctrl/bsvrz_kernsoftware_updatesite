/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.dav.daf.util.WeakIdentityHashMap;

import java.util.*;

/**
 * Diese Klasse enthält für jeden Attributtyp den sogenannten "undefiniert Wert" und stellt Methoden zur Verfügung diesen Wert zu setzen oder zu prüfen ob ein
 * Attribut undefiniert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class UndefinedValueHandler {

	final private static UndefinedValueHandler _undefinedValueHandler = new UndefinedValueHandler();

	/** Undefiniert Wert für StringAttributeType */
	private static final String _undefinedValueString = "_Undefiniert_";

	/** Undfiniert Wert für ReferenceAttributeType */
	private static final long _undefinedValueReference = 0;

	/** Undefinierter Wert für TimeAttributeType abselute Zeitwerte. */
	private static final long _undefinedValueTimeAbsolute = 0;

	/**
	 * Dieses Objekt wird gespeichert, wenn ein undfined berechnet werden sollte, aber der Wert nicht berechnet werden konnte. Damit bei einem weiteren Durchlauf
	 * nicht erneut versucht den Wert zu berechnen, wird dieses Objekt gespeichert.
	 */
	private final Long _marker = new Long(4711);

	/** Speichert zu jedem IntegerAttributeType den dazugehörigen undefiniert Wert. So muss dieser nicht ständig neu berechnet werden. */
	private final Map<IntegerAttributeType, Long> _integerUndefinedValues = new WeakIdentityHashMap<IntegerAttributeType, Long>();

	/**
	 * Gibt eine Instanz der Klasse zurück. Es existiert nur eine Instanz der Klasse, ein erneuter Aufruf stellt das selbe Objekt zur Verfügung.
	 *
	 * @return Instanz der Klasse (Singleton)
	 */
	public static final UndefinedValueHandler getInstance() {
		return _undefinedValueHandler;
	}

	/**
	 * Erzeugt ein neues Objekt ohne Parameter
	 */
	private UndefinedValueHandler() {
	}

	/**
	 *
	 * @param data Attributwerte
	 * @param value Werte
	 */
	private void writeValue(Data data, String value) {
		data.asTextValue().setText(value);
	}

	/**
	 * Setzt das Attribut auf den sogenannten "undefiniert Wert".
	 *
	 * @param data Attribut, dessen Wert gesetzt werden soll
	 *
	 * @throws IllegalArgumentException Der Attributtyp ist nicht <code>StringAttributeType</code>
	 */
	public void setToUndefinedString(Data data) {
		if(data.getAttributeType() instanceof StringAttributeType) {
			writeValue(data, _undefinedValueString);
		}
		else {
			throw new IllegalArgumentException("StringAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Prüft ob das übergebene Attribut einen Wert ungleich dem "undefiniert Wert" besitzt.
	 *
	 * @param data Attribut
	 *
	 * @return true = Der Wert des Attributes ist ungelich dem "undefiniert Wert"; false = sonst
	 *
	 * @throws IllegalArgumentException Der Attributtyp ist nicht <code>StringAttributeType</code>
	 */
	public boolean isDefinedString(Data data) {
		if(data.getAttributeType() instanceof StringAttributeType) {
			return !_undefinedValueString.equals(data.asTextValue().getValueText());
		}
		else {
			throw new IllegalArgumentException("StringAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Stellt den String zur Verfügung, der den "undefiniert Wert" eines <code>StringAttributeType</code> darstellt.
	 *
	 * @return s.o.
	 */
	public String getUndefinedValueString() {
		return _undefinedValueString;
	}

	/**
	 * Setzt ein Attribut vom Typ ReferenceAttributeType auf den "undefiniert Wert".
	 *
	 * @param data Attribut, dessen Wert auf "undefiniert" gesetzt werden soll
	 */
	public void setToUndefinedReference(Data data) {
		if(data.getAttributeType() instanceof ReferenceAttributeType) {
			writeValue(data, Long.toString(_undefinedValueReference));
		}
		else {
			throw new IllegalArgumentException("ReferenceAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Prüft ob der Wert eines Attributes vom Typ "ReferenceAttributeType" undefiniert ist und somit nicht verschickt werden kann.
	 *
	 * @param data                   Attribut, dessen Wert geprüft werden soll
	 * @param referenceAttributeType Bestimmt, ob die Referenz "0" als undefiniert Wert gilt, oder als normale Referenz
	 *
	 * @return true = Das Attribut ist definiert und kann verschickt werden; false = Der Wert des Attributes ist gleich dem "undefiniert Wert" und kann somit nicht
	 *         verschickt werden
	 */
	public boolean isDefinedReference(Data data, ReferenceAttributeType referenceAttributeType) {
		if(data.getAttributeType() instanceof ReferenceAttributeType) {
			if(referenceAttributeType.isUndefinedAllowed()) {
				// Undefinierte Referenzen sind erlaubt, also kann die "0" nicht zum erkennen des "undefiniert Wert" benutzt werden.
				// Also ist jeder Wert erlaubt (da jeder Wert mindestens eine undefinierte Referenz darstellt).
				return true;
			}
			else {
				// Undefinierte Referenzen sind nicht erlaubt. Also ist die 0 der "undefiniert Wert
				if(_undefinedValueReference == data.asReferenceValue().getId()) {
					// Die Id ist gleich dem "undefiniert Wert", also ist das Attribut nicht definiert
					return false;
				}
				else {
					return true;
				}
			}
		}
		else {
			throw new IllegalArgumentException("ReferenceAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Gibt eine Zahl zurück, die entweder den größten Wert darstellt, der mit den angegebenen Bytes darzustellen ist oder aber die kleinste Zahl. Die Zahl ist
	 * entweder größer als requiredMaxValue oder kleiner als requiredMinValue.
	 *
	 * @param integerAttributeType Enthält den ByteCount, die States und die Min/Max
	 *
	 * @return Zahl die größer als requiredMaxValue ist oder aber kleiner als requiredMinValue. Kann diese Zahl nicht gefunden werden, wird <code>null</code>
	 *         zurückgegeben.
	 */
	public Long getUndefinedValueInteger(IntegerAttributeType integerAttributeType) {

		synchronized(_integerUndefinedValues) {
			// Prüfen, ob der Wert vielleicht schon berechnet wurde

			// Dies kann entweder ein undefinert Wert (Long) sein oder ein Marker(Object) oder <code>null</code>
			final Long undefinedValueOrMarkerOrNull = _integerUndefinedValues.get(integerAttributeType);

			if(undefinedValueOrMarkerOrNull != null) {
				// Entweder ein Marker oder ein undefiniert Wert
				if(undefinedValueOrMarkerOrNull == _marker) {
					// Das Objekt ist der "Marker", also wurde der Wert bereits berechnet und nicht gefunden.
					// Es kann kein undefiniert Wert berechnet werden.
					return null;
				}
				else {
					// Es ist ein undefiniert berechnet worden
					return undefinedValueOrMarkerOrNull;
				}
			}
			else {
				// Es gibt noch keinen Wert oder Marker

				// Der Wert muss erst berechnet werden
				List<IntegerValueState> integerValueStates = integerAttributeType.getStates();
				final int byteCount = integerAttributeType.getByteCount();
				final long rangeMinimum;
				final long rangeMaximum;
				final IntegerValueRange integerValueRange = integerAttributeType.getRange();
				if(integerValueRange != null) {
					rangeMinimum = integerValueRange.getMinimum();
					rangeMaximum = integerValueRange.getMaximum();
				}
				else {
					// Es wurde kein Wertebereich angegeben. Also müssen
					// nur die Zustände berücksichtigt werden.
					rangeMinimum = 0;
					rangeMaximum = 0;
				}

				// Der undefiniert Wert wie folgt gefunden:

				// Start: Der kleinste darstellbare Wert wird als "undefiniert Wert" genommen
				// 1) Prüfen ob der undefiniert Wert nicht innerhalb des geforderten Wertebereichs liegt (wenn ja, dann über den Wert hinweg setzen).
				//    Ist der undefiniert Wert größer als das darstellebare Maximum -> Abbruch und Exception werfen
				// 2) Ist der undefiniert Wert als Zustand nicht vorhanden -> Wert gefunden und Ende
				// 3) undefiniert Wert um eins erhöhen und bei 1) weitermachen

				// Speichert jeden Zustand als Long. Dies ermöglicht die Frage "Ist der ungültig Wert gleich einem Zustand" in O(1) zu beantworten.
				Set<Long> states = new HashSet<Long>(integerValueStates.size());
				for(IntegerValueState integerValueState : integerValueStates) {
					states.add(new Long(integerValueState.getValue()));
				}

				// Größer positiver Wert, der dargestellt werden kann
				final long max;
				// kleinste Wert, der dargestellt werden kann
				final long min;
				switch(byteCount) {
					case IntegerAttributeType.BYTE:
						max = Byte.MAX_VALUE;
						min = Byte.MIN_VALUE;
						break;
					case IntegerAttributeType.SHORT:
						max = Short.MAX_VALUE;
						min = Short.MIN_VALUE;
						break;
					case IntegerAttributeType.INT:
						max = Integer.MAX_VALUE;
						min = Integer.MIN_VALUE;
						break;
					case IntegerAttributeType.LONG:
						max = Long.MAX_VALUE;
						min = Long.MIN_VALUE;
						break;
					default:
						throw new IllegalStateException("Ganzzahlattributtyp " + integerAttributeType + " mit nicht unterstützter Anzahl Bytes: " + byteCount);
				}

				// undefiniert Wert, der gesucht wird
				long undefinedValue = min;

				while(true) {
					if(integerValueRange != null && undefinedValue == rangeMinimum) {
						// Stossen von unten an die untere Grenze, also über den geforderten Bereich (rangeMinimum bis rangeMaximum) hinwegsetzen
						undefinedValue = rangeMaximum;
					}
					else if(!states.contains(new Long(undefinedValue))) {
						// Es gibt den Wert nicht im Zustand und er ist bestimmt ausserhalb
						// des angegebenen Wertebereichs. Also wurde der undefiniert Wert gefunden.
						_integerUndefinedValues.put(integerAttributeType, new Long(undefinedValue));
						return undefinedValue;
					}
					if(undefinedValue >= max) {
						// Es konnte kein Wert berechnet werden, damit die Berechung nicht erneut durchgeführt werden muss,
						// wird dieses "Ergebnis" gespeichert.
						_integerUndefinedValues.put(integerAttributeType, _marker);
						return null;
					}
					undefinedValue++;
				}
			}
		} // synch map
	}

	/**
	 * @param data                 Attribut, in das der "undefiniert Wert" eingetragen wird
	 * @param undefinedValue       Undefiniert Wert des Attributtyps. <code>null</code> wird als "es gibt keinen undefiniert Wert" interpretiert.
	 * @param integerAttributeType Dient nur dazu, um bei einem Fehler die Pid des fehlehaften Typs anzugeben
	 *
	 * @throws IllegalStateException Es kann kein "undefiniert Wert" ermittelt werden, da alle Byte-Kombinationen gebraucht werden um die angegebenen Werte
	 *                               darzustellen. Dieser Fall darf eigentlich nicht auftreten, da in diesen Fällen ein default-Wert definiert sein muss.
	 */
	public void setToUndefinedInteger(Data data, Long undefinedValue, IntegerAttributeType integerAttributeType) {
		if(data.getAttributeType() instanceof IntegerAttributeType) {
			// Als unskaliert schreiben. Bei unskaliert wird geprüft ob der zu schreibende Wert der undefiniert Wert ist

			if(undefinedValue != null) {
				data.asUnscaledValue().set(undefinedValue);
			}
			else {
				// Der Wert konnte nicht berechnet werden
				throw new IllegalStateException(
						"Für ein Attribut konnte kein undefiniert-Wert berechnet werden: Pid des Typs " + integerAttributeType.getPidOrNameOrId()
				);
			}
		}
		else {
			throw new IllegalArgumentException("IntegerAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Prüft ob ein Attribut vom Typ IntegerAttributeType definiert ist und damit verschickt werden kann.
	 *
	 * @param attributeType  Ganzzahl-Attributtyp
	 * @param data           Attribut, dessen Wert geprüft werden soll
	 * @param undefinedValue enthält den undefiniert Wert des Attributtyps. Wird <code>null</code> übergeben, so wird dies als "kein undefiniert festgelegt"
	 *                       interpretiert.
	 *
	 * @return true = Das Attribut ist definiert und kann verschickt werden; false = Der Wert des Attributes ist gleich dem "undefiniert Wert" und darf somit nicht
	 *         verschickt werden
	 */
	public boolean isDefinedInteger(final IntegerAttributeType attributeType, Data data, Long undefinedValue) {
		if(!(data.getAttributeType() instanceof IntegerAttributeType)) {
			throw new IllegalArgumentException("Attributtyp des übergebenen Data-Objekts ist kein IntegerAttributeType sondern: " + data.getAttributeType());
		}
		final long unscaledValue = data.asUnscaledValue().longValue();
		// Unabhängig vom Undefined-Wert wird hier geprüft, ob der Wert des Attributs im Wertebereich liegt oder ein Aufzählungszustand ist.
		final IntegerValueRange range = attributeType.getRange();
		if(range != null) {
			// Wertebereich angegeben, liegt der Wert drin?
			if(range.getMinimum() <= unscaledValue && unscaledValue <= range.getMaximum()) return true;
		}
		final List<IntegerValueState> states = attributeType.getStates();
		for(IntegerValueState state : states) {
			if(state.getValue() == unscaledValue) return true;
		}
		return false;
	}

	/**
	 * Setzt den Wert des Attributes auf "undefiniert". Dieser Wert entspricht bei abseluten Zeiten 0.
	 *
	 * @param data Attribute, das auf den undefinierten Wert gesetzt werden soll
	 */
	public void setToUndefinedTimeAbsolute(Data data) {
		if(data.getAttributeType() instanceof TimeAttributeType) {
			data.asTimeValue().setMillis(_undefinedValueTimeAbsolute);
		}
		else {
			throw new IllegalArgumentException("TimeAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Prüft, ob der Wert eines Attributes definiert ist. Der Attributwert wird als abselute Zeitangabe interpretiert.
	 *
	 * @param data Attribute
	 *
	 * @return true = Der Wert ist definiert; false = Der Wert ist nicht definiert
	 */
	public boolean isDefinedTimeAbsolute(Data data) {
		if(data.getAttributeType() instanceof TimeAttributeType) {
			if(_undefinedValueTimeAbsolute == data.asTimeValue().getMillis()) {
				// Der gesetze Wert entspricht dem undefiniert Wert. Also ist der Wert nicht definiert
				return false;
			}
			else {
				return true;
			}
		}
		else {
			throw new IllegalArgumentException("TimeAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Gibt den "undefiniert Wert" eines TimeAttribute (abeslute Angaben) zurück.
	 *
	 * @return Undefiniert Wert für eine abselute Zeitangabe
	 */
	public long getUndefinedValueTimeAbselute() {
		return _undefinedValueTimeAbsolute;
	}

	/**
	 * Setzt den "undefiniert Wert" bei einem Attribut vom Typ TimeAttributeType (relative Zeitangabe). Der "undefiniert Wert" ist abhängig der gewünschten
	 * Genauigkeit. Bei Millisekunden wird <code>Long.MIN_VALUE</code> benutzt, bei Sekunden <code>Integer.MIN_VALUE</code>.
	 *
	 * @param data     Attribut
	 * @param accuracy Sekunden oder Millisekunden {@link TimeAttributeType}
	 */
	public void setToUndefinedTimeRelative(Data data, byte accuracy) {
		if(data.getAttributeType() instanceof TimeAttributeType) {
			if(accuracy == TimeAttributeType.MILLISECONDS) {
				data.asTimeValue().setMillis(Long.MIN_VALUE);
			}
			else {
				data.asTimeValue().setSeconds(Integer.MIN_VALUE);
			}
		}
		else {
			throw new IllegalArgumentException("TimeAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}

	/**
	 * Prüft, ob eine relative Zeitangabe definiert ist.
	 *
	 * @param data     Attribut vom Typ TimeAttributeType
	 * @param accuracy Sekunden oder Millisekunden {@link TimeAttributeType}
	 *
	 * @return true = Das Attribut ist definiert und kann verschickt werden; false = Das Attribut hat als Wert den "undefiniert Wert" und kann nicht verschickt
	 *         werden
	 */
	public boolean isDefinedTimeRelative(Data data, byte accuracy) {
		if(data.getAttributeType() instanceof TimeAttributeType) {

			if(accuracy == TimeAttributeType.MILLISECONDS) {
				if(data.asTimeValue().getMillis() == Long.MIN_VALUE) {
					return false;
				}
				else {
					return true;
				}
			}
			else {
				if(data.asTimeValue().getSeconds() == Integer.MIN_VALUE) {
					return false;
				}
				else {
					return true;
				}
			}
		}
		else {
			throw new IllegalArgumentException("TimeAttributeType, Klasse des übergebenen Objekts: " + data.getAttributeType());
		}
	}
}

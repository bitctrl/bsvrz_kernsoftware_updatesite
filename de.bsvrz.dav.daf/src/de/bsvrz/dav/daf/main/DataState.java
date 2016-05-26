/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main;

/**
 * Objekte dieser Klasse repräsentieren die verschiedenen Zustände von Datensätzen (Datensatztyp) (siehe Datensatztyp in
 * den Technische Anforderungen zum Archivsystem).
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class DataState {

	/**
	 * Datensatztyp für Datensätze die Nutzdaten enthalten (siehe Technische Anforderungen Archivsystem).
	 */
	public static final DataState DATA = new DataState("Nutzdaten", 1);

	/**
	 * Datensatztyp für leere Datensätze, die von der Quelle ohne Attributwerte versendet wurden (siehe Technische
	 * Anforderungen Archivsystem).
	 */
	public static final DataState NO_DATA = new DataState("Keine Daten", 2);

	/**
	 * Datensatztyp für leere Datensätze, die vom Datenverteiler generiert wurden, weil keine Quelle für die entsprechenden
	 * Daten existiert. (siehe Technische Anforderungen Archivsystem).
	 */
	public static final DataState NO_SOURCE = new DataState("Keine Quelle", 3);

	/**
	 * Datensatztyp für leere Datensätze, die vom Datenverteiler generiert wurden, weil nicht die erforderlichen Rechte zum
	 * Empfang der Daten vorliegen. (siehe Technische Anforderungen Archivsystem).
	 */
	public static final DataState NO_RIGHTS = new DataState("Keine Rechte", 4);

	/**
	 * Datensatztyp für leere Datensätze, die vom Archivsystem generiert wurden, um eine potentielle Datenlücke zu
	 * markieren. (siehe Technische Anforderungen Archivsystem).
	 */
	public static final DataState POSSIBLE_GAP = new DataState("Potentielle Datenlücke", 5);

	/**
	 * Datensatztyp für leere Datensätze, die vom Archivsystem in jeden Datensatzstrom eingefügt werden, um das Ende eines
	 * Datensatzstroms einer Teilanfrage zu markieren. Der Datenzeitstempel bei Verwendung dieses Typs gibt an, bis wann
	 * der letzte reguläre Datensatz gültig ist, d.h. der Datenzeitstempel enthält den Datenzeitstempel des Datensatzes,
	 * der dem letzten übertragenen regulären Datensatz folgen würde, oder falls es noch keinen Nachfolger gibt den
	 * Datenzeitstempel des letzten übertragenen regulären Datensatzes. (Siehe auch Technische Anforderungen
	 * Archivsystem).
	 *
	 * @see de.bsvrz.dav.daf.main.impl.archive.PersistentDataStreamSupplier
	 * @see de.bsvrz.dav.daf.main.impl.archive.PersistenceModule#getArchiveDataStreams
	 */
	public static final DataState END_OF_ARCHIVE = new DataState("Ende Archivdaten", 6);

	/**
	 * Datensatztyp für leere Datensätze, die vom Archivsystem in den Antwort-Datensatzstrom von Teilanfragen eingefügt
	 * wird, um Bereiche zu markieren, die gelöscht (und nicht gesichert) wurden. Der Datenzeitstempel bei Verwendung
	 * dieses Typs enthält den Datenzeitstempel des ersten gelöschten Datensatz im gelöschten Bereich. (Siehe auch
	 * Technische Anforderungen Archivsystem).
	 */
	public static final DataState DELETED_BLOCK = new DataState("Gelöschter Bereich", 7);

	/**
	 * Datensatztyp für leere Datensätze, die vom Archivsystem in den Antwort-Datensatzstrom von Teilanfragen eingefügt
	 * wird, um Bereiche zu markieren, die ausgelagert (d.h. gesichert und gelöscht) wurden. Der Datenzeitstempel bei
	 * Verwendung dieses Typs enthält den Datenzeitstempel des ersten gelöschten Datensatz im ausgelagerten Bereich. (Siehe
	 * auch Technische Anforderungen Archivsystem).
	 */
	public static final DataState UNAVAILABLE_BLOCK = new DataState("Ausgelagerter Bereich", 8);

	/**
	 * Datensatztyp für leere Datensätze, die vom Datenverteiler versendet werden können, wenn eine Anmeldung von Daten im
	 * Konflikt mit anderen Anmeldungen steht (z.B. mehrere Senken für die gleichen Daten).
	 */
	public static final DataState INVALID_SUBSCRIPTION = new DataState("Ungültige Anmeldung", 9);

	/**
	 * Liefert einen zur angegebenen Kodierung korrespondierenden Datensatztyp zurück. Die Kodierung des Datensatztyps kann
	 * mit der Methode {@link #getCode()} bestimmt werden.
	 *
	 * @param code Kodierung des gewünschten Datensatztyps.
	 * @return Zur angegebenen Kodierung korrespondierender Datensatztyp.
	 * @throws IllegalArgumentException Wenn eine ungültige Kodierung übergeben wurde.
	 * @see #getCode
	 */
	public static final DataState getInstance(int code) {
		switch (code) {
			case 1:
				return DATA;
			case 2:
				return NO_DATA;
			case 3:
				return NO_SOURCE;
			case 4:
				return NO_RIGHTS;
			case 5:
				return POSSIBLE_GAP;
			case 6:
				return END_OF_ARCHIVE;
			case 7:
				return DELETED_BLOCK;
			case 8:
				return UNAVAILABLE_BLOCK;
			case 9:
				return INVALID_SUBSCRIPTION;
			default:
				throw new IllegalArgumentException("Undefinierte Objektkodierung: " + code);
		}
	}

	/**
	 * Bestimmt die Kodierung dieses Datensatztyps. Die Kodierung ist innerhalb der Klasse eindeutig. Das entsprechende
	 * Objekt kann aus der Kodierung mit Hilfe der Methode {@link #getInstance(int)} wiederhergestellt werden.
	 *
	 * @return Eindeutige Kodierung dieses Datensatztyps.
	 * @see #getInstance(int)
	 */
	public int getCode() {
		return _code;
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those
	 * provided by <code>java.util.Hashtable</code>.
	 * <p>
	 * The general contract of <code>hashCode</code> is: <ul> <li>Whenever it is invoked on the same object more than once
	 * during an execution of a Java application, the <tt>hashCode</tt> method must consistently return the same integer,
	 * provided no information used in <tt>equals</tt> comparisons on the object is modified. This integer need not remain
	 * consistent from one execution of an application to another execution of the same application. <li>If two objects are
	 * equal according to the <tt>equals(Object)</tt> method, then calling the <code>hashCode</code> method on each of the
	 * two objects must produce the same integer result. <li>It is <em>not</em> required that if two objects are unequal
	 * according to the {@link Object#equals(Object)} method, then calling the <tt>hashCode</tt> method on each of the two
	 * objects must produce distinct integer results.  However, the programmer should be aware that producing distinct
	 * integer results for unequal objects may improve the performance of hashtables. </ul>
	 * <p>
	 * As much as is reasonably practical, the hashCode method defined by class <tt>Object</tt> does return distinct
	 * integers for distinct objects. (This is typically implemented by converting the internal address of the object into
	 * an integer, but this implementation technique is not required by the Java<font size="-2"><sup>TM</sup></font>
	 * programming language.)
	 *
	 * @return a hash code value for this object.
	 * @see Object#equals(Object)
	 * @see java.util.Hashtable
	 */
	public int hashCode() {
		return _code;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Datensatztyps zurück. Das genaue Format ist nicht festgelegt und kann
	 * sich ändern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return _name;
	}

	private final String _name;
	private final int _code;

	private DataState(String name, int code) {
		_name = name;
		_code = code;
	}
}

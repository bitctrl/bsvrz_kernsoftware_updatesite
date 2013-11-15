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

package de.bsvrz.dav.daf.main.archive;

/**
 * Klasse die die vier verschiedenen Datensatzarten des Archivsystem repr�sentieren kann
 * (siehe Technische Anforderungen Archivsystem).
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public final class ArchiveDataKind {

	/**
	 * Datensatzart f�r online erhaltene aktuelle Datens�tze
	 * (siehe Technische Anforderungen Archivsystem).
	 */
	public static final ArchiveDataKind ONLINE= new ArchiveDataKind("aktuelle Daten", 1, false, false);

	/**
	 * Datensatzart f�r online erhaltene als nachgelieferte markierte Datens�tze
	 * (siehe Technische Anforderungen Archivsystem).
	 */
	public static final ArchiveDataKind ONLINE_DELAYED= new ArchiveDataKind("nachgelieferte Daten", 2, false, true);

	/**
	 * Datensatzart f�r nachgeforderte aktuelle Datens�tze
	 * (siehe Technische Anforderungen Archivsystem).
	 */
	public static final ArchiveDataKind REQUESTED= new ArchiveDataKind("nachgefordert-aktuelle Daten", 3, true, false);

	/**
	 * Datensatzart f�r nachgeforderte als nachgeliefert markierte Datens�tze
	 * (siehe Technische Anforderungen Archivsystem).
	 */
	public static final ArchiveDataKind REQUESTED_DELAYED= new ArchiveDataKind("nachgefordert-nachgelieferte Daten", 4, true, true);

	/**
	 * Liefert eine zur angegebenen Kodierung korrespondierende Datensatzart zur�ck.
	 * Die Kodierung einer Datensatzart kann mit der Methode {@link #getCode()} bestimmt werden.
	 * @param code  Kodierung der gew�nschten Datensatzart.
	 * @return Zur angegebenen Kodierung korrespondierende Datensatzart.
	 * @throws IllegalArgumentException Wenn eine ung�ltige Kodierung �bergeben wurde.
	 * @see #getCode
	 */
	public static final ArchiveDataKind getInstance(int code) {
		switch(code) {
		case 1:
			return ONLINE;
		case 2:
			return ONLINE_DELAYED;
		case 3:
			return REQUESTED;
		case 4:
			return REQUESTED_DELAYED;
		default:
			throw new IllegalArgumentException("Undefinierte Objektkodierung");
		}
	}

	/**
	 * Bestimmt die Kodierung dieser Datensatzart. Die Kodierung ist innerhalb der Klasse eindeutig. Das entsprechende
	 * Objekt kann aus der Kodierung mit Hilfe der Methode {@link #getInstance(int)} wiederhergestellt werden.
	 * @return Eindeutige Kodierung dieser Datensatzart.
	 * @see #getInstance(int)
	 */
	public int getCode() {
		return _code;
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those
	 * provided by <code>java.util.Hashtable</code>.
	 * <p/>
	 * The general contract of <code>hashCode</code> is: <ul> <li>Whenever it is invoked on the same object more than once
	 * during an execution of a Java application, the <tt>hashCode</tt> method must consistently return the same integer,
	 * provided no information used in <tt>equals</tt> comparisons on the object is modified. This integer need not remain
	 * consistent from one execution of an application to another execution of the same application. <li>If two objects are
	 * equal according to the <tt>equals(Object)</tt> method, then calling the <code>hashCode</code> method on each of the
	 * two objects must produce the same integer result. <li>It is <em>not</em> required that if two objects are unequal
	 * according to the {@link Object#equals(Object)} method, then calling the <tt>hashCode</tt> method on each of the two
	 * objects must produce distinct integer results.  However, the programmer should be aware that producing distinct
	 * integer results for unequal objects may improve the performance of hashtables. </ul>
	 * <p/>
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
	 * Bestimmt, ob diese Datensatzart nachgeforderte Datens�tze enthalten kann.
	 * @return <code>true</code> f�r die Datensatzarten {@link #REQUESTED} und {@link #REQUESTED_DELAYED}, sonst <code>false</code>.
	 */
	public boolean isRequested() {
		return _requested;
	}

	/**
	 * Bestimmt, ob diese Datensatzart als nachgeliefert gekennzeichnete Datens�tze enthalten kann.
	 * @return <code>true</code> f�r die Datensatzarten {@link #ONLINE_DELAYED} und {@link #REQUESTED_DELAYED}, sonst <code>false</code>.
	 */
	public boolean isDelayed() {
		return _delayed;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieser Datensatzart zur�ck. Das genaue Format ist nicht
	 * festgelegt und kann sich �ndern.
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return _name;
	}

	private final String _name;
	private final int _code;
	private final boolean _requested;
	private final boolean _delayed;


	private ArchiveDataKind(String name, int code, boolean requested, boolean delayed) {
		_name= name;
		_code = code;
		_requested = requested;
		_delayed = delayed;
	}

}

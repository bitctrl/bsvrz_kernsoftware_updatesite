/*
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config.management.consistenycheck;

import java.util.*;

/**
 * Klasse, die das Ergebnis einer Konsistenzprüfung enthält.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConsistencyCheckResult implements ConsistencyCheckResultInterface {

	/** Enthält die Liste aller Fehler, die lokal aufgetreten sind */
	private final List<ConsistencyCheckResultEntry> _localErrors = Collections.synchronizedList(new ArrayList<ConsistencyCheckResultEntry>());

	/** Liste aller Interferenz-Fehler */
	private final List<ConsistencyCheckResultEntry> _interferenceErrors = Collections.synchronizedList(new ArrayList<ConsistencyCheckResultEntry>());

	/** Warnungen */
	private final List<ConsistencyCheckResultEntry> _warnings = Collections.synchronizedList(new ArrayList<ConsistencyCheckResultEntry>());

	/**
	 * Speichert einen lokalen Fehler, Interferenzfehler oder eine Warnung in die entsprechende Datenstruktur ab.
	 * @param entry Fehler oder Warnung, die gespeichert werden soll.
	 */
	public void addEntry(ConsistencyCheckResultEntry entry){
		if(entry.getEntryType() == ConsistencyCheckResultEntryType.LOCAL_ERROR){
			_localErrors.add(entry);
		}else if(entry.getEntryType() == ConsistencyCheckResultEntryType.INTERFERENCE_ERROR){
			_interferenceErrors.add(entry);
		}else if(entry.getEntryType() == ConsistencyCheckResultEntryType.WARNING){
			_warnings.add(entry);
		}else
		{
			throw new IllegalArgumentException("Unbekannter Fehler/Warnungs Typ: " + entry);
		}
	}

	/**
	 * Speichert einen lokalen Fehler in der Liste bisher aufgetretener Fehler. Die Reihenfolge der Fehler bleibt dabei erhalten. Der erste gemeldete Fehler ist
	 * das erste Element der Liste.
	 *
	 * @param localError Fehlertext, der gespeichert werden soll
	 * @deprecated Wurde durch {@link #addEntry(ConsistencyCheckResultEntry)} ersetzt
	 */
	public void addLocalError(ConsistencyCheckResultEntry localError) {
		addEntry(localError);
	}

	/**
	 * Speichert einen Interfernz-Fehler in der Liste bisher aufgetretener Fehler. Die Reihenfolge der Fehler bleibt dabei erhalten. Der erste gemeldete Fehler ist
	 * das erste Element der Liste.
	 *
	 * @param interferenceError Fehlertext, der gespeichert werden soll
	 * @deprecated Wurde durch {@link #addEntry(ConsistencyCheckResultEntry)} ersetzt
	 */
	public void addInteferenceError(ConsistencyCheckResultEntry interferenceError) {
		addEntry(interferenceError);
	}

	/**
	 * Speichert eine Warnung in der Liste bisher aufgetretener Warnungen. Die Reihenfolge der Warnungen bleibt dabei erhalten. Die erste gemeldete Warnung ist das
	 * erste Element der Liste.
	 *
	 * @param warning Text der Warnung, die gespeichert werden soll
	 * @deprecated Wurde durch {@link #addEntry(ConsistencyCheckResultEntry)} ersetzt
	 */
	public void addWarning(ConsistencyCheckResultEntry warning) {
		addEntry(warning);
	}

	/**
	 * Methode, die <code>true</code> zurückgibt, wenn ein lokaler Fehler aufgetreten ist, siehe TPuK1-139.
	 *
	 * @return true = es ist mindestens ein lokaler Fehler bei der Konsistenzprüfung aufgetreten; false = es ist kein lokaler Fehler bei der Konsistenzprüfung
	 *         aufgetreten
	 */
	public boolean localError() {
		if(_localErrors.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Methode, die <code>true</code> zurückgibt, wenn ein Interferenz-Fehler aufgetreten ist, siehe TPuK1-140.
	 *
	 * @return true = es ist mindestens ein Interferenz-Fehler bei der Konsistenzprüfung aufgetreten; false = es ist kein Interferenz-Fehler bei der
	 *         Konsistenzprüfung aufgetreten
	 */
	public boolean interferenceErrors() {
		if(_interferenceErrors.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/** @return true = Es gab Warnings; false = Es gab keine Warnings */
	public boolean warnings() {
		if(_warnings.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Lokale Fehler-Texte, die aufgetreten sind
	 *
	 * @return Liste mit lokalen Fehler-Texten. Sind keine Fehler aufgetreten, ist die Liste leer.
	 */
	public List<ConsistencyCheckResultEntry> getLocalErrors() {
		return _localErrors;
	}

	/**
	 * Interferenz-Fehler-Texte, die aufgetreten sind
	 *
	 * @return Liste mit Interferenz-Fehler-Texten. Sind keine Fehler aufgetreten, ist die Liste leer
	 */
	public List<ConsistencyCheckResultEntry> getInterferenceErrors() {
		return _interferenceErrors;
	}

	/**
	 * Warnungen, die erzeugt wurden
	 *
	 * @return Liste mit Warnungen. Sind keine Warnungen vorhanden, ist die Liste leer.
	 */
	public List<ConsistencyCheckResultEntry> getWarnings() {
		return _warnings;
	}


	public String toString() {
		StringBuffer out = new StringBuffer("Ergebnis Konsistenzprüfung: \n");

		out.append("  Anzahl Warnungen: " + _warnings.size() + "\n");
		for(ConsistencyCheckResultEntry entry : _warnings) {
			out.append("    " + entry + "\n");
		}

		out.append("  Anzahl Interferenzfehler: " + _interferenceErrors.size() + "\n");
		for(ConsistencyCheckResultEntry entry : _interferenceErrors) {
			out.append("    " + entry + "\n");
		}

		out.append("  Anzahl lokale Fehler: " + _localErrors.size() + "\n");
		for(ConsistencyCheckResultEntry entry : _localErrors) {
			out.append("    " + entry + "\n");
		}

		return out.toString();
	}
}

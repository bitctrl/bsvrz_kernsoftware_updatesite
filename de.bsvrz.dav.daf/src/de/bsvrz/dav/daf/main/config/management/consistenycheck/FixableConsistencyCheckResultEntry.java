/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Stellt einen behebbaren Fehler dar. Instanzen dieser klasse sind um Gegensatz zu ConsistencyCheckResultEntry nicht immutable, daher kann sich insbesondere
 * die Einordnung in die verschiedenen Kategorien von ConsistencyCheckResult ändern. Daher sind nach dem aufrufen von fix() entsprechende Einordungen in ein
 * ConsistencyCheckResult unbrauchbar. In {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#checkConsistencyAndFixErrors(java.util.Collection)}
 * wird das dadurch korrigiert, dass die Einträge in eine neue ConsistencyCheckResult-Instanz kopiert werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class FixableConsistencyCheckResultEntry extends ConsistencyCheckResultEntry {

	private String _errorTextOverride = null;

	private boolean _isFixed = false;

	private boolean _isError = false;

	/**
	 * @param configurationArea Konfigurationsbereich, in dem der Fehler, die Warnung, aufgetaucht ist
	 * @param involvedObjects   Objekte, die dazu geführt haben, dass es zu einem Fehler oder einer Warnung gekommen ist
	 * @param errorText         Fehlertext, der die Art des Fehlers/Warnung genauer beschreibt.
	 */
	public FixableConsistencyCheckResultEntry(
			final ConfigurationArea configurationArea, final List<SystemObject> involvedObjects, final String errorText) {
		super(ConsistencyCheckResultEntryType.WARNING, configurationArea, involvedObjects, errorText);
	}


	/**
	 * Ändert den Status dieser behebbaren Fehlermeldung
	 *
	 * @param text      Text der dem Benutzer angezeigt wird, null wenn der alte Text beibehalten werden soll.
	 * @param hasFailed Ist das Beheben des Problems fehlgeschlagen? Wenn true wird die Konsistenzprüfung einen Fehler ergeben. Wenn false gilt der Fehler aus
	 *                  behoben.
	 */
	protected final void update(final String text, final boolean hasFailed) {
		_isFixed = true;
		_errorTextOverride = text;
		_isError = hasFailed;
	}

	/**
	 * Gibt den Fehlertext zurück
	 *
	 * @return s.o.
	 */
	@Override
	public String getErrorText() {
		if(!_isError && !_isFixed) {
			return "Behebbares Problem: " + getPlainText();
		}
		else if(_isFixed) {
			return "Behobenes Problem: " + getPlainText();
		}
		else {
			return getPlainText();
		}
	}

	private String getPlainText() {
		if(_errorTextOverride != null) return _errorTextOverride;
		return super.getErrorText();
	}

	/**
	 * Gibt zurück, ob es sich um einen Fehler handelt.
	 *
	 * @return LOCAL_ERROR wenn das Problem nicht automatisch behoben werden konnte, sonst WARNING
	 */
	@Override
	public ConsistencyCheckResultEntryType getEntryType() {
		if(_isError) return ConsistencyCheckResultEntryType.LOCAL_ERROR;
		return super.getEntryType();
	}

	/**
	 * Fordert die Klasse auf den Fehler zu beheben. Diese Funktion sollte {@link #update(String, boolean)} aufrufen, um über den Erfolg der Fehlerkorrektur zu
	 * informieren.
	 *
	 * @throws Exception Fehler
	 */
	protected abstract void fix() throws Exception;

	/** Behebt den Fehler, den dieses Objekt repräsentiert */
	public final void fixError() {
		try {
			fix();
			if(!_isFixed) throw new IllegalStateException("Ungültiger Zustand, update() wurde nicht aufgerufen.");
		}
		catch(Exception e) {
			// Exception mit Stacktrace in String umwandeln

			String result = null;
			if(e != null) {
				StringWriter stringWriter = new StringWriter();
				PrintWriter printWriter = new PrintWriter(stringWriter);
				e.printStackTrace(printWriter);
				printWriter.flush();
				result = stringWriter.toString();
			}

			// und Fehlermeldung setzen
			update(
					super.getErrorText() + " Beim Beheben des Problems trat ein unerwarteter Fehler auf: " + result, true
			);
		}
	}

	/**
	 * Gibt zurück ob das Problem behoben ist
	 *
	 * @return true wenn behoben
	 */
	public boolean isFixed() {
		return _isFixed;
	}

	/**
	 * Gibt zurück, ob beim beheben des Fehlers ein Problem auftrat
	 *
	 * @return true wenn ein Fehler auftrat
	 */
	public boolean isError() {
		return _isError;
	}
}

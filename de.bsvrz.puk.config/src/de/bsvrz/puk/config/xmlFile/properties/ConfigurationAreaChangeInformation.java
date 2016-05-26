/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.sys.funclib.debug.Debug;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Diese Klasse stellt ein Element "konfigurationsAenderung" dar, siehe K2S.dtd.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationAreaChangeInformation {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Zeitpunkt */
	private final long _condition;

	/** Version */
	private final int _version;

	/** Autor der Änderung */
	private final String _author;

	/** Grund der Änderung. Der Leerstring deutet an, das der Grund nicht angegeben wurde. */
	private final String _reason;

	/** Text, den der Autor zusätzlich geschrieben hat. */
	private final String _text;

	/**
	 * @param condition Zeitpunkt
	 * @param version   Version, ist dieser Wert unbekannt muss "-1" gesetzt werden
	 * @param author    Autor
	 * @param reason    Grund der Änderunge, ist der Grund unbekannt, muss der Leerstring "" gesetzt werden
	 */
	public ConfigurationAreaChangeInformation(long condition, int version, String author, String reason, String text) {
		_condition = condition;
		_version = version;
		_author = author;
		_reason = reason;
		_text = text;
	}

	/**
	 * @param condition Zeitpunkt, die Zeichenkette wird in ein "Date" umgewandelt. Das erzeugte Date enthält "Tag, Monat, Jahr".
	 * @param version   Version, ist diese unbekannt, wird der Leerstring "" übergeben. Dies wird dann als -1 gespeichert
	 * @param author    Autor der Änderungen
	 * @param reason    Grund der Änderungen. Ist der Grund unbekannt, wird "" übergeben und auch so am Objekt gespeichert
	 */
	public ConfigurationAreaChangeInformation(String condition, String version, String author, String reason, String text) {

		final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		try {
			_condition = dateFormat.parse(condition).getTime();
		}
		catch(ParseException e) {
			e.printStackTrace();
			_debug.error("Der String " + condition + " kann nicht in einen Zeitpunkt mit dem Aufbau dd.MM.yyyy umgewandelt werden", e);
			throw new IllegalArgumentException("Der String " + condition + " kann nicht in einen Zeitpunkt mit dem Aufbau dd.MM.yyyy umgewandelt werden");
		}

		if(!"".equals(version)) {
			// Es wurde nicht der Leerstring übergeben
			_version = Integer.parseInt(version);
		}
		else {
			// Der Leerstring muss als -1 interpretiert werden
			_version = -1;
		}
		_author = author;
		_reason = reason;
		_text = text;
	}

	/** @return Zeitpunkt */
	public long getCondition() {
		return _condition;
	}

	/** @return Version, oder -1 falls der Wert nicht gesetzt war */
	public int getVersion() {
		return _version;
	}

	/**
	 * #
	 *
	 * @return Autor der Änderungen
	 */
	public String getAuthor() {
		return _author;
	}

	/** @return Grund der Änderungen. Wurden keine Änderungen eingetragen, so wird der Leerstring "" zurückgegeben */
	public String getReason() {
		return _reason;
	}

	/** @return Text, der zusätzlich eingegeben wurde */
	public String getText() {
		// trim() schneidet die vorherigen Leerzeichen ab
		return _text.trim();
	}
}

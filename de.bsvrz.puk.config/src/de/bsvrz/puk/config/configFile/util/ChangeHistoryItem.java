/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.util;

import java.util.Date;

/**
 * Diese Klasse stellt einen Eintrag im Log der �nderugshistorie dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11591 $
 */
public class ChangeHistoryItem {

	/** Autor der �nderung */
	final private String _author;

	/** Grund der �nderung */
	final private String _reason;

	/** Beschreibung der �nderung */
	final private String _text;

	/** Version */
	final private short _version;

	/** Das Datum der �nderung. */
	final private Date _timeStamp;

	/**
	 * Gibt den Autor der �nderung zur�ck.
	 *
	 * @return Autor der �nderung
	 */
	public String getAuthor() {
		return _author;
	}

	/**
	 * Gibt den Grund der �nderung zur�ck.
	 *
	 * @return Grund der �nderung
	 */
	public String getReason() {
		return _reason;
	}

	/**
	 * Gibt die Beschreibung der �nderung zur�ck.
	 *
	 * @return Beschreibung der �nderung
	 */
	public String getText() {
		return _text;
	}

	public short getVersion() {
		return _version;
	}

	/**
	 * Gibt das Datum der �nderung zur�ck.
	 *
	 * @return Datum der �nderung
	 */
	public Date getTimeStamp() {
		return _timeStamp;
	}

	/**
	 * Konstruktor zur manuellen Erstellung eines �nderungseintrages.
	 * @param timestamp Zeitstempel in ms
	 * @param author Autor
	 * @param version versio
	 * @param reason Grund der �nderung
	 * @param text weiterer Text
	 */
	public ChangeHistoryItem(final long timestamp, final String author, final short version, final String reason, final String text) {
		_author = author;
		_reason = reason;
		_text = text;
		_version = version;
		_timeStamp = new Date(timestamp);
	}
}

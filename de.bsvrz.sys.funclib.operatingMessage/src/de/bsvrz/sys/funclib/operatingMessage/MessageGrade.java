/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.operatingMessage;

/**
 * Diese Klasse stellt die Meldungsklasse für die Betriebsmeldungen dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5007 $
 */
public enum MessageGrade {
	FATAL ("Fatal"),
	ERROR ("Fehler"),
	WARNING ("Warnung"),
	INFORMATION ("Information");

	private final String _grade;

	private MessageGrade(String grade) {
		_grade = grade;
	}

	public String getGrade() {
		return _grade;
	}

	public String toString() {
		return _grade;
	}
}

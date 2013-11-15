/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.xmlSupport.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.xmlSupport; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter;

/**
 * Abstrakte Basisklasse für Ereignis-Objekte, die vom SaxPullAdapter geliefert werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5009 $
 */
public abstract class Event {
	private final EventType _type;

	public Event(EventType type) {
		_type = type;
	}

	public EventType getType() {
		return _type;
	}

	public String toString() {
		return new StringBuilder().append(_type.toString()).append('{').append(paramString()).append('}').toString();
	}

	abstract String paramString();
}

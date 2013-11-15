/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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
 * Ereignisobjekt, das vom SaxPullAdapter geliefert wird, wenn das Ende eines XML-Element gelesen wurde.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5009 $
 */
public class EndElementEvent extends Event {
	private final String _localName;

	public EndElementEvent(final String localName) {
		super(EventType.END_ELEMENT);
		_localName = localName;
	}

	public String getLocalName() {
		return _localName;
	}

	String paramString() {
		return getLocalName();
	}
}

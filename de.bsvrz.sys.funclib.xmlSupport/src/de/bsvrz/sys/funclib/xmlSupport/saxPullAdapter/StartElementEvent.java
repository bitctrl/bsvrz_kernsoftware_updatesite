/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.xmlSupport.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.xmlSupport; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter;

import org.xml.sax.Attributes;

/**
 * Ereignisobjekt, das vom SaxPullAdapter geliefert wird, wenn der Beginn eines XML-Element gelesen wurde.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StartElementEvent extends Event {
	private final String _localName;
//		private final Attributes _attributes;
//		private final Map<String, String> _attributes;
	private final AttributeMap _attributes;

	public StartElementEvent(final String localName, final Attributes attributes) {
		super(EventType.START_ELEMENT);
		_localName = localName;
//			_attributes = new HashMap<String, String>(numberOfAttributes);
		_attributes = new AttributeMap(attributes);
	}

	public String getLocalName() {
		return _localName;
	}

	public AttributeMap getAttributes() {
		return _attributes;
	}

	String paramString() {
		return getLocalName() + getAttributes();
	}
}

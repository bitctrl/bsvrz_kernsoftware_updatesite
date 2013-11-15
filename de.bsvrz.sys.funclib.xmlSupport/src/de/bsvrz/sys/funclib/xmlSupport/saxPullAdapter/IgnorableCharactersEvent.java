/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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
 * Ereignisobjekt, das vom SaxPullAdapter geliefert wird, wenn Text gelesen wurde, der normalerweise beim Parsen ignoriert werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5009 $
 */
public class IgnorableCharactersEvent extends Event {
	private final String _text;

	public IgnorableCharactersEvent(final char[] chars, final int start, final int length) {
		super(EventType.IGNORABLE_CHARACTERS);
		_text = String.valueOf(chars, start, length);
	}

	public String getText() {
		return _text;
	}

	String paramString() {
		return getText();
	}
}

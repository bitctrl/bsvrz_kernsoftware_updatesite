/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.ringbuffer;

import de.bsvrz.dav.daf.main.ResultData;

public class ArchiveDataBufferXT {

	private Elem first, last;

	public synchronized void append(ResultData rd, long archiveTime) {
		Elem elem = new Elem();
		elem.rd = rd;
		elem.at = archiveTime;

		if(last != null) {
			last.next = elem;
			elem.prev = last;
			last = elem;
		}
		else {
			last = first = elem;
		}
	}

	public synchronized void remove(Elem e) {
		if(e.prev != null) e.prev.next = e.next;
		if(e.next != null) e.next.prev = e.prev;
		if(e == first) first = e.next;
		if(e == last) last = e.prev;
	}

	public Elem first() {
		return first;
	}

	public static class Elem {

		private Elem next, prev;

		private ResultData rd;

		private long at;

		public ResultData getResultData() {
			return rd;
		}

		public long getArchiveTime() {
			return at;
		}

		public Elem next() {
			return next;
		}
	}
}

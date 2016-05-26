/*
 * Copyright 2013 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Hilfsklasse zur Unwandlong von Long-Arrays in Long-Collections und umgekehrt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 0000 $
 */
public class Longs {

	public static List<Long> asList(final long[] longs) {
		if(longs == null) return Collections.emptyList();
		return new AbstractList<Long>() {
			@Override
			public Long get(final int index) {
				return longs[index];
			}

			@Override
			public int size() {
				return longs.length;
			}
		};
	}

	public static long[] asArray(final Collection<Long> longs) {
		long[] result = new long[longs.size()];
		int i = 0;
		for(Long l : longs) {
			result[i] = l;
			i++;
		}
		return result;
	}

}

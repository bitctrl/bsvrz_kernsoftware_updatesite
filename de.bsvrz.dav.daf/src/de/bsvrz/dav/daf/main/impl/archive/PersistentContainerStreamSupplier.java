/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.impl.archive;

import java.io.IOException;

/**
 * Diese Schnittstelle wird vom Persistenzmodul implementiert und stellt alle Container, die sich im direkten Zugriff
 * befinden, zur Verfügung.
 * Die Container werden dabei nicht zufällig, sondern streng monoton steigend nach ihrer ContainerId zurückgegeben, beginnend
 * mit der kleinsten ContainerId.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface PersistentContainerStreamSupplier {

	/**
	 * Diese Methode wird vom Archivsystem benutzt um einen Container aus der Persistenz {@link PersistenceModule} anzufordern.
	 *
	 * @return Container, der sich im direkten Zugriff der Persistenz befindet oder <code>null</code> falls kein Container
	 *         mehr vorhanden ist. Die Container sind streng monoton steigend nach ihrer ContainerId zurückzugeben.
	 *
	 * @throws IOException Der Container konnte nicht vom Speichermedium geladen werden
	 */
	PersistentDataContainer fetchNextContainer() throws IOException;

	/**
	 * Diese Methode wird vom Archivsystem aufgerufen, wenn keine weiteren Container mehr benötigt werden.
	 */
	void cancel();
}

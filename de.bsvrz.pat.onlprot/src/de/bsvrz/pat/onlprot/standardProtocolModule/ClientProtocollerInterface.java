/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.onlprot.
 * 
 * de.bsvrz.pat.onlprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.onlprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.onlprot.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.onlprot.standardProtocolModule;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;

/**
 * Schnittstelle die seitens Protokollierungsapplikationen zu implementieren ist, um Protokolle von empfangenen Telegrammen zu erzeugen.
 * <p>
 * Protokolle haben üblicherweise einen Kopf und einen Fuß, welche über die hier zur Verfügung gestellten Methoden erzeugt werden können. Zur Ausgabe von
 * Datentelegrammen wird {@link de.bsvrz.dav.daf.main.ClientReceiverInterface#update update} verwendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ClientProtocollerInterface extends ClientReceiverInterface {

	/** Gibt einen Fuß aus, d. h. abschließende, einmalige Informationen. */
	public void writeFooter();

	/**
	 * Gibt einen Kopf aus, d. h. einleitende, einmalige Informationen.
	 *
	 * @param args String[] mit den Kommandozeilenargumenten
	 */
	public void writeHeader(String[] args);
}

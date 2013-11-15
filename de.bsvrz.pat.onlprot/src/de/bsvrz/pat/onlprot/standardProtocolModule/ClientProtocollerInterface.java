/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.onlprot.
 * 
 * de.bsvrz.pat.onlprot is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.onlprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.onlprot; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.onlprot.standardProtocolModule;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;

/**
 * Schnittstelle die seitens Protokollierungsapplikationen zu implementieren ist, um Protokolle von empfangenen Telegrammen zu erzeugen.
 * <p/>
 * Protokolle haben �blicherweise einen Kopf und einen Fu�, welche �ber die hier zur Verf�gung gestellten Methoden erzeugt werden k�nnen. Zur Ausgabe von
 * Datentelegrammen wird {@link de.bsvrz.dav.daf.main.ClientReceiverInterface#update update} verwendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public interface ClientProtocollerInterface extends ClientReceiverInterface {

	/** Gibt einen Fu� aus, d. h. abschlie�ende, einmalige Informationen. */
	public void writeFooter();

	/**
	 * Gibt einen Kopf aus, d. h. einleitende, einmalige Informationen.
	 *
	 * @param args String[] mit den Kommandozeilenargumenten
	 */
	public void writeHeader(String[] args);
}

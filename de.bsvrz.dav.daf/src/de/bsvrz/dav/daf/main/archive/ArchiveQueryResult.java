/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.dav.daf.main.archive;

/**
 * Schnittstelle zum Zugriff auf Ergebnisse von Archivanfragen. Diese Schnittstelle wird von Applikationen benutzt, um
 * auf Ergebnisse von Archivanfragen zuzugreifen, die mit verschiedenen Methoden des Interfaces {@link
 * ArchiveRequestManager} gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben einer Methode mit der
 * geprüft werden kann, ob die entsprechende Anfrage erfolgreich war oder nicht, eine Methode, mit der auf eine
 * eventuelle Fehlermeldung zugegriffen kann, zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 * @see ArchiveRequestManager
 */
public interface ArchiveQueryResult {

	/**
	 * Bestimmt, ob die Archivanfrage erfolgreich war. Die Methode wird von einer Applikation aufgerufen um festzustellen,
	 * ob die Anfrage erfolgreich war und weitere anfragespezifische Ergebnisse aus den verschiedenen abgeleiteten
	 * Interfaces abgerufen werden können oder ob die Anfrage nicht erfolgreich war und eine entsprechende Fehlermeldung
	 * über die Methode {@link #getErrorMessage} abgerufen werden kann.
	 *
	 * @return <code>true</code>, falls die Anfrage erfolgreich war, sonst <code>false</code>.
	 * @throws InterruptedException Falls der aufrufende Thread unterbrochen wurde, während auf die entsprechende
	 *                              Antwortnachricht aus dem Archivsystem gewartet wurde.
	 */
	boolean isRequestSuccessful() throws InterruptedException;

	/**
	 * Bestimmt eine Fehlernachricht, falls die entsprechende Archivanfrage nicht erfolgreich war.
	 *
	 * @return Fehlernachricht, wenn die entsprechende Archivanfrage nicht erfolgreich, sonst leerer String.
	 * @throws InterruptedException Falls der aufrufende Thread unterbrochen wurde, während auf die entsprechende
	 *                              Antwortnachricht aus dem Archivsystem gewartet wurde.
	 */
	String getErrorMessage() throws InterruptedException;

}

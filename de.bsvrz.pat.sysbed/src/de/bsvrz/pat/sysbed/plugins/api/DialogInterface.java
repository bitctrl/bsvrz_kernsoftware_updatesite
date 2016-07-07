/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.plugins.api;

/**
 * Das Interface dient als Schnittstelle zwischen dem Dialog eines Moduls und der {@link ButtonBar unteren Buttonleiste} dieses Dialogs. Falls einer der drei
 * Buttons gedrückt wird, wird eine der Methoden dieses Interfaces aufgerufen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface DialogInterface {

	/**
	 * Falls der "OK" - Button der {@link ButtonBar Buttonleiste} betätigt wird, wird diese Methode aufgerufen, um das Modul mit den eingestellten Parametern des
	 * Dialogs aufzurufen.
	 */
	public void doOK();

	/** Falls der "Abbrechen" - Button der {@link ButtonBar Buttonleiste} betätigt wird, wird diese Methode aufgerufen, um den Dialog zu schließen. */
	public void doCancel();

	/**
	 * Falls der "Speichern unter ..." - Button der {@link ButtonBar Buttonleiste} betätigt wird, wird diese Methode aufgerufen und ein Name für die eingestellten
	 * Parameter des Dialogs übergeben. Diese Einstellungen des Dialogs können hier gespeichert werden.
	 *
	 * @param title der Name für die Einstellungen des Dialogs
	 */
	public void doSave(String title);
}

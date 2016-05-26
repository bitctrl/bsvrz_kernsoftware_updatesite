/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.concurrent;

/**
 * Definiert die Schnittstelle eines Listeners, der beim Auslösen und Schließen eines Triggers benachricht wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see de.bsvrz.sys.funclib.concurrent.Trigger
 * @see de.bsvrz.sys.funclib.concurrent.DelayedTrigger
 */
public interface TriggerTarget {

	/** Wird beim Auslösen eines Triggers aufgerufen */
	public void shot();

	/** Wird beim Schließen eines Listeners aufgerufen */
	public void close();
}

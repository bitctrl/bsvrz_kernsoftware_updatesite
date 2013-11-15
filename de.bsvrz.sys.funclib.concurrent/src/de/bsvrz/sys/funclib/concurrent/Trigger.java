/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.concurrent;

/**
 * Definiert die Schnittstelle zum An- und Abmelden von Targets bei einem Trigger
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5888 $
 * @see de.bsvrz.sys.funclib.concurrent.TriggerTarget
 * @see de.bsvrz.sys.funclib.concurrent.DelayedTrigger
 */
public interface Trigger {

	/**
	 * F�gt dem Trigger ein neues TriggerTarget, das beim Ausl�sen und Schlie�en des Triggers benachrichtigt werden soll, hinzu.
	 *
	 * @param triggerTarget Listener-Objekt, das zuk�nftig beim Ausl�sen oder Schlie�en des Triggers benachrichtigt werden soll.
	 */
	void addTriggerTarget(TriggerTarget triggerTarget);

	/**
	 * Entfernt ein vorher hinzugef�gtes TriggerTarget.
	 *
	 * @param triggerTarget Listener-Objekt, das entfernt werden soll.
	 */
	void removeTriggerTarget(TriggerTarget triggerTarget);
}

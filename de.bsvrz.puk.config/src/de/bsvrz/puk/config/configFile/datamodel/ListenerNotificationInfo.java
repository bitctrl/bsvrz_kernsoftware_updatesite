/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.DynamicObject;

/** Stellt eine Klasse dar, welche einen Listener, mit Parameter f�r die Listener-Methode und den Typen des Listeners speichert. */
class ListenerNotificationInfo {

	/** Zu benachrichtigendes Listener-Objekt. */
	private Object _listener;

	/** Parameter f�r die Methode des Listeners. */
	private DynamicObject _parameter;

	/** Typ des Listeners. */
	private ListenerType _listenerType;

	/**
	 * Legt ein ListenerNotificationInfo-Objekt an, welches einen Listener, einen Parameter und den Typ des Listeners �bergeben bekommt.
	 *
	 * @param listener     Listener
	 * @param parameter    Parameter, welcher der Listener-Methode �bergeben wird.
	 * @param listenerType Der Typ, um welchen Listener es sich handelt.
	 */
	public ListenerNotificationInfo(Object listener, DynamicObject parameter, ListenerType listenerType) {
		_listener = listener;
		_parameter = parameter;
		_listenerType = listenerType;
	}

	/** @return Gibt den Paramter f�r die Listener-Methode zur�ck. */
	public DynamicObject getParameter() {
		return _parameter;
	}

	/** @return Gibt den Typ des Listeners zur�ck. */
	public ListenerType getListenerType() {
		return _listenerType;
	}

	/** @return Gibt den Listener zur�ck. */
	public Object getListener() {
		return _listener;
	}

	/** Das Enum enth�lt alle Typen, die ein Listener in dieser Klasse annehmen kann. */
	enum ListenerType {

		/** Der verwendete Listener ist der DynamicObjectCreatedListener. */
		CREATED,
		/** Der verwendete Listener ist der NameChangeListener. */
		NAMECHANGED,
		/** Der verwendete Listener ist der InvalidationListener. */
		INVALITDATION
	}
}

/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.DynamicObject;

/** Stellt eine Klasse dar, welche einen Listener, mit Parameter für die Listener-Methode und den Typen des Listeners speichert. */
class ListenerNotificationInfo {

	/** Zu benachrichtigendes Listener-Objekt. */
	private Object _listener;

	/** Parameter für die Methode des Listeners. */
	private DynamicObject _parameter;

	/** Typ des Listeners. */
	private ListenerType _listenerType;

	/**
	 * Legt ein ListenerNotificationInfo-Objekt an, welches einen Listener, einen Parameter und den Typ des Listeners übergeben bekommt.
	 *
	 * @param listener     Listener
	 * @param parameter    Parameter, welcher der Listener-Methode übergeben wird.
	 * @param listenerType Der Typ, um welchen Listener es sich handelt.
	 */
	public ListenerNotificationInfo(Object listener, DynamicObject parameter, ListenerType listenerType) {
		_listener = listener;
		_parameter = parameter;
		_listenerType = listenerType;
	}

	/** @return Gibt den Paramter für die Listener-Methode zurück. */
	public DynamicObject getParameter() {
		return _parameter;
	}

	/** @return Gibt den Typ des Listeners zurück. */
	public ListenerType getListenerType() {
		return _listenerType;
	}

	/** @return Gibt den Listener zurück. */
	public Object getListener() {
		return _listener;
	}

	/** Das Enum enthält alle Typen, die ein Listener in dieser Klasse annehmen kann. */
	enum ListenerType {

		/** Der verwendete Listener ist der DynamicObjectCreatedListener. */
		CREATED,
		/** Der verwendete Listener ist der NameChangeListener. */
		NAMECHANGED,
		/** Der verwendete Listener ist der InvalidationListener. */
		INVALITDATION
	}
}

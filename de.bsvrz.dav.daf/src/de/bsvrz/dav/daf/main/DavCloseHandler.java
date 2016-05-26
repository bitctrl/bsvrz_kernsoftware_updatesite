/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;

/**
 * Diese Klasse kapselt einen vom Benutzer übergebenen CloseHandler um sicherzustellen,
 * dass auch bei Verwendung von 2 Verbindungen jeweils nur eine Benachrichtigung verschickt wird,
 * selbst wenn beide Verbindungen gleichzeitig terminieren.
 *
 * Außerdem stellt diese Klasse sicher, dass die ClientDavConnection bei einem Fehler in jedem Fall terminiert,
 */
class DavCloseHandler implements ApplicationCloseActionHandler {
	private final ApplicationCloseActionHandler _closer;
	private final ClientDavConnection _clientDavConnection;
	private boolean _hasFired = false;

	public DavCloseHandler(final ApplicationCloseActionHandler closer, final ClientDavConnection clientDavConnection) {
		_closer = closer;
		_clientDavConnection = clientDavConnection;
	}

	@Override
	public void close(final String error) {
		synchronized(this) {
			if(_hasFired) return;
			_hasFired = true;
		}
		_closer.close(error);
		if(_clientDavConnection.isConnected()) {
			_clientDavConnection.disconnect(true, error);
		}
	}

	/**
	 * Setzt den Close Handler zurück
	 */
	public void reset(){
		synchronized(this) {
			_hasFired = false;
		}
	}
}

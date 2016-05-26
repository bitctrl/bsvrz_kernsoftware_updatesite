/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.tcpCommunication;

import de.bsvrz.dav.daf.communication.lowLevel.ConnectionInterface;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * TCP/IP-Implementierung des Interfaces {@link de.bsvrz.dav.daf.communication.lowLevel.ConnectionInterface}.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TCP_IP_Communication implements ConnectionInterface {

	/** Der Debug-Logger. */
	private static final Debug _debug = Debug.getLogger();

	/** Das Socket-Objekt dieser Verbindung. */
	private Socket _socket;

	/**
	 * Erzeugt ein Objekt dieser Klasse. Dieser Konstruktor wird von der Client-Seite benutzt. Der Socket wird in diesem Falle erst erzeugt, nachdem die {@link
	 * #connect(String,int) connect}-Methode aufgerufen wurde.
	 */
	public TCP_IP_Communication() {
	}

	/**
	 * Erzeugt ein Objekt dieser Klasse und hält eine Referenz auf den übergebenen Socket fest. Dieser Konstruktor wird von der Server-Seite benutzt.
	 *
	 * @param socket ein Socket
	 */
	public TCP_IP_Communication(Socket socket) {
		_socket = socket;
	}

	public void connect(String mainAdress, int subAdressNumber) throws ConnectionException {
		try {
			


			_socket = new Socket(InetAddress.getByName(mainAdress), subAdressNumber);
			_debug.info("TCP-Verbindung aktiv  aufgebaut, " + _socket.getLocalSocketAddress() + " --> " + _socket.getRemoteSocketAddress());
		}
		catch(java.net.UnknownHostException ex) {
			String error = "Fehler beim Verbindungsaufbau: Unbekannter Rechnername: " + mainAdress;
			_debug.error(error);
			throw new ConnectionException(error);
		}
		catch(java.net.NoRouteToHostException ex) {
			String error = "Fehler beim Verbindungsaufbau: Angegebener Rechner ist nicht erreichbar: " + mainAdress;
			_debug.error(error);
			throw new ConnectionException(error);
		}
		catch(java.net.ConnectException ex) {
			String error = "Fehler beim Verbindungsaufbau: Verbindung zum Rechner " + mainAdress + " auf TCP-Port " + subAdressNumber + " nicht möglich";
			_debug.error(error);
			throw new ConnectionException(error);
		}
		catch(IllegalArgumentException ex) {
			String error = "Fehler beim Verbindungsaufbau zum Rechner " + mainAdress + " auf TCP-Port " + subAdressNumber + ": Ungültiges Argument";
			_debug.error(error);
			throw new ConnectionException(error);
		}
		catch(IOException ex) {
			_debug.error("Fehler beim aktiven Verbindungsaufbau zum Rechner " + mainAdress + " auf TCP-Port " + subAdressNumber, ex);
			throw new ConnectionException(ex.getLocalizedMessage());
		}
	}

	public void disconnect() {
		try {
			final Socket mySocket = _socket;
			if(mySocket != null) {
//				try {
//					Thread.sleep(1000);
//				}
//				catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				_debug.info("TCP-Verbindung wird terminiert,  " + mySocket.getLocalSocketAddress() + " -|- " + mySocket.getRemoteSocketAddress());
				mySocket.shutdownInput();
				mySocket.shutdownOutput();
				mySocket.close();
			}
		}
		catch(IOException ex) {
			_debug.info("Fehler beim Terminieren der TCP-Verbindung", ex);
		}
	}

	public InputStream getInputStream() {
		if(_socket != null) {
			try {
				return _socket.getInputStream();
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public OutputStream getOutputStream() {
		if(_socket != null) {
			try {
				return _socket.getOutputStream();
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public String getMainAdress() {
		if(_socket != null) {
			return _socket.getInetAddress().getCanonicalHostName();
		}
		return null;
	}

	public int getSubAdressNumber() {
		if(_socket != null) {
			return _socket.getPort();
		}
		return -1;
	}

	public int getLocalSubAdressNumber() {
		if(_socket != null) {
			return _socket.getLocalPort();
		}
		return -1;
	}

	public boolean isConnected() {
		return _socket != null && _socket.isConnected() && !_socket.isClosed();
	}
}

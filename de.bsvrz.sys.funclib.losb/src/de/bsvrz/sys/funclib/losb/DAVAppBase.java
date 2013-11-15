/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb;

import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.kernsoftware.ConnectionManager;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;


/**
 * Basisklasse für alle Los-B Applikationen.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 7731 $ / $Date: 2010-05-27 20:41:19 +0200 (Thu, 27 May 2010) $ / ($Author: rs $)
 */
public class DAVAppBase implements ApplicationCloseActionHandler {

	private static final String P_CONFIG_AUTH = "-konfigurationsVerantwortlicher";

	protected Debug logger;

	protected String applicationName = "<none>";

	protected StringBuilder applicationLabel = new StringBuilder();

	protected ArgumentList argList;

	private String cfgAuthParam;

	private ClientDavParameters davParams;

	private ClientDavConnection davConnection;

	private ConfigurationAuthority configAuth;

	private DavDisconnectHandlerThread _davDisconnectHandlerThread;

	private DisconnectHandler _disconnectHandler;



	/**
	 * Initialisiert die Applikation. Geht die Verbindung zum Dav verloren, so wird versucht, die Verbindung wieder aufzubauen.
	 *
	 * @param arguments Startparameter
	 * @param appName   Name der APplikation
	 *
	 * @throws Exception Fehler
	 * @see #reconnectHandler()
	 */
	public DAVAppBase(String[] arguments, String appName) throws Exception {
		this(arguments, appName, true);
	}

	/**
	 * @param arguments Aufrufparameter
	 * @param appName   Name der Applikation
	 * @param reconnect <code>true</code> falls die Applikation versuchen soll, die Verbindung zum Dav wieder aufzunehmen, falls diese verloren ging.
	 *
	 * @throws Exception Fehler
	 */
	public DAVAppBase(String[] arguments, String appName, boolean reconnect) throws Exception {
		init(arguments, appName);
		if(reconnect) {
			_disconnectHandler = new DisconnectHandler() {
				public void handleDisconnect() throws MissingParameterException, InconsistentLoginException, CommunicationError, ConnectionException {
					connectToDav();
					reconnectHandler();
				}
			};
			_davDisconnectHandlerThread.start();
		}

	}

	/**
	 * @param arguments Aufrufparameter
	 * @param appName   Name der Applikation
	 * @param disconnectHandler Objekt, dass über den Verbindungsverlust zum Dav informiert werden soll, oder <code>null</code>, wenn keine Benachrichtigung
	 * stattfinden soll. Im Falle einer Benachrichtigung wird die entsprechende Methode von einem eigenen Thread aufgerufen.
	 *
	 * @throws Exception Fehler
	 */
	public DAVAppBase(String[] arguments, String appName, DisconnectHandler disconnectHandler) throws Exception {
		init(arguments, appName);
		setDisconnectHandler(disconnectHandler);
	}

	public void setDisconnectHandler(final DisconnectHandler disconnectHandler) {
		_disconnectHandler = disconnectHandler;
		if(_disconnectHandler != null && !_davDisconnectHandlerThread.isAlive()) {
			_davDisconnectHandlerThread.start();
		}
	}

	private void init(final String[] arguments, final String appName) throws MissingParameterException {
		argList = new ArgumentList(arguments);
		applicationName = appName;
		for(int i = 0; i < arguments.length; i++) {
			applicationLabel.append(arguments[i]);
		}
		Debug.init(applicationName, argList);
		logger = Debug.getLogger();		// sollte in der abgeleiteten Klasse nochmal gemacht werden, damit der
		// Klassenname stimmt.

		cfgAuthParam = argList.hasArgument(P_CONFIG_AUTH) ? argList.fetchArgument(P_CONFIG_AUTH).asString() : null;

		davParams = new ClientDavParameters(argList);
		davParams.setApplicationName(applicationName);
		_davDisconnectHandlerThread = new DavDisconnectHandlerThread();
	}

	public interface DisconnectHandler {
		void handleDisconnect() throws MissingParameterException, InconsistentLoginException, CommunicationError, ConnectionException;
	}

	public void close(String error) {
		logger.warning("Verbindung zum DAV wurde unterbrochen: " + error);
		if(_davDisconnectHandlerThread != null) _davDisconnectHandlerThread.activate();
	}

	/**
	 * Diese Methode wird nach erfolgreichem Reconnect zum DAV aufgerufen. Falls diese Methode ueberschrieben wird sollte stets
	 * <code>super.reconnectHandler()</code> aufgerufen werden, weil hier der {@link ConnectionManager} zurueckgesetzt wird.
	 */
	protected void reconnectHandler() {
		ConnectionManager.resetSubscriptionMarkers(davConnection);
	}

	public void connectToDav() throws MissingParameterException, CommunicationError, ConnectionException, InconsistentLoginException, ConfigurationException {
		davConnection = new ClientDavConnection(davParams);
		davConnection.connect();
		davConnection.login();
		davConnection.setCloseHandler(this);
		configAuth = cfgAuthParam != null ? (ConfigurationAuthority)getObj(cfgAuthParam) : getDavCon().getLocalConfigurationAuthority();
		MessageSender.getInstance().init(
				davConnection, applicationName, davConnection.getLocalConfigurationAuthority().getPid() + applicationLabel.toString()
		);
	}

	/** Beendet zuerst den DavReconnecter und dann die Verbindung zum DAV. */
	public void disconnectFromDav() {
		if(_davDisconnectHandlerThread != null) _davDisconnectHandlerThread.terminateTask();
		if(davConnection != null) {
			davConnection.setCloseHandler(null);
			davConnection.disconnect(false, "");
		}
		_davDisconnectHandlerThread = null;
		davConnection = null;
	}

	public DataModel getDataModel() {
		return davConnection.getDataModel();
	}

	public ClientDavInterface getDavCon() {
		return davConnection;
	}

	public AttributeGroup getAtg(String pid) throws ConfigurationException {
		return getDataModel().getAttributeGroup(pid);
	}

	public AttributeGroup getAtg(long id) throws ConfigurationException {
		return (AttributeGroup)getDataModel().getObject(id);
	}

	public Aspect getAsp(String pid) throws ConfigurationException {
		return getDataModel().getAspect(pid);
	}

	public Aspect getAsp(long id) throws ConfigurationException {
		return (Aspect)getDataModel().getObject(id);
	}

	public SystemObject getObj(long id) throws ConfigurationException {
		return getDataModel().getObject(id);
	}

	public SystemObject getObj(String pid) throws ConfigurationException {
		return getDataModel().getObject(pid);
	}

	public DataDescription getDD(String atgPid, String aspPid) throws ConfigurationException {
		return new DataDescription(getAtg(atgPid), getAsp(aspPid));
	}

	public ConfigurationAuthority getConfigAuth() {
		return configAuth;
	}

	private class DavDisconnectHandlerThread extends Thread {

		private boolean terminated = false, activated = false;

		public void run() {
			while(!terminated) {
				try {
					synchronized(this) {
						while(!activated) wait();
					}
					activated = false;

					while(true) {
						try {
							sleep(2000);
						}
						catch(InterruptedException e) {
							terminated = true;
							return;
						}
						try {
							if(_disconnectHandler != null) _disconnectHandler.handleDisconnect();
							break;
						}
						catch(Exception e) {
							logger.error("Es konnte keine Verbindung zum Datenverteiler aufgebaut werden.\n" + e.getMessage());
						}
					}
				}
				catch(InterruptedException e) {
					terminated = true;
					return;
				}
			}
		}

		public synchronized void activate() {
			activated = true;
			notify();
		}

		public void terminateTask() {
			terminated = true;
			interrupt();
		}
	}
}

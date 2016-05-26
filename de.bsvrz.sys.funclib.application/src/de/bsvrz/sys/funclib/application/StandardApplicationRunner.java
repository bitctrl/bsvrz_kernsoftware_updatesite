/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.application;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;

/**
 * Diese Klasse initialisiert die Klasse für die Debug-Ausgaben, stellt eine Verbindung zum Datenverteiler her und ruft die Methoden des {@link
 * StandardApplication}-Interfaces auf.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class StandardApplicationRunner {

	/** Der Debug-Logger */
	private static Debug _debug;

	/**
	 * Die ApplikationsKennung wird hier gespeichert. Sie besteht aus den Aufrufargumenten, dem Klassennamen (wie beim Debug) und der Pid des lokalen
	 * Verantwortlichen.
	 */
	private static StringBuilder _applicationLabel;

	/** Der Name der Applikation, die den StandardApplicationRunner nutzt. */
	private static String _applicationName = "";

	/**
	 * Diese Methode erstellt eine Verbindung zum Datenverteiler anhand der Standard-Parameter her.
	 *
	 * @param application Applikation, die eine Verbindung zum Datenverteiler benötigt.
	 * @param args        Aufrufargumente der Applikation
	 */
	public static void run(StandardApplication application, String[] args) {
		run(application, "typ.applikation", args);
	}

	/**
	 * Diese Methode erstellt eine Verbindung zum Datenverteiler anhand der Standard-Parameter her.
	 *
	 * @param application Applikation, die eine Verbindung zum Datenverteiler benötigt.
	 * @param applicationTypePid Pid des Applikationstyps. Der Datenverteiler erzeugt für die Appliaktion ein Objekt dieses
	 *      Typs. Der Applikationstyp sollte "typ.applikation" sein oder davon abgeleitet sein.
	 * @param args        Aufrufargumente der Applikation
	 */
	public static void run(StandardApplication application, String applicationTypePid, String[] args) {
		createApplicationLabel(args);
		final ArgumentList argumentList = new ArgumentList(args);
		initializeDebug(application, argumentList);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(argumentList));
		try {
			// ArgumentListe wird in ClientDavParameters konvertiert
			final ClientDavParameters parameters = new ClientDavParameters(argumentList);
			parameters.setApplicationTypePid(applicationTypePid);
			parameters.setApplicationName(_applicationName);
			// zuerst darf die Applikation die ArgumentListe durcharbeiten
			application.parseArguments(argumentList);
			argumentList.ensureAllArgumentsUsed();
			final ClientDavInterface connection = new ClientDavConnection(parameters);
			// Fertigmeldung für Start/Stop wird explizit selbst übernommen
			establishConnection(connection);
			_applicationLabel.append(connection.getLocalConfigurationAuthority().getPid());	// ApplikationsKennung
			MessageSender.getInstance().init(connection, _applicationName, _applicationLabel.toString());
			application.initialize(connection);
			// Fertigmeldung wird gesendet
			connection.sendApplicationReadyMessage();
		}
		catch(Exception ex) {
			_debug.error("Fehler", ex);
			System.exit(1);
		}
	}

	/**
	 * Diese Methode startet einen Login-Dialog und meldet sich anhand der eingetragenen IP-Adresse, Portnummer, Benutzername und Passwort beim Datenverteiler an.
	 *
	 * @param application Applikation, die eine Verbindung zum Datenverteiler benötigt.
	 * @param args        Aufrufargumente der Applikation
	 */
	public static void run(GUIApplication application, String[] args) {
		run(application, "typ.applikation", args);
	}

	/**
	 * Diese Methode startet einen Login-Dialog und meldet sich anhand der eingetragenen IP-Adresse, Portnummer, Benutzername und Passwort beim Datenverteiler an.
	 *
	 * @param application Applikation, die eine Verbindung zum Datenverteiler benötigt.
	 * @param applicationTypePid Pid des Applikationstyps. Der Datenverteiler erzeugt für die Appliaktion ein Objekt dieses
	 *      Typs. Der Applikationstyp sollte "typ.applikation" sein oder davon abgeleitet sein.
	 * @param args        Aufrufargumente der Applikation
	 */
	public static void run(GUIApplication application, String applicationTypePid, String[] args) {
		createApplicationLabel(args);
		final ArgumentList argumentList = new ArgumentList(args);
		initializeDebug(application, argumentList);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(argumentList));
		try {
			// soll der Login-Dialog überhaupt angezeigt werden?
			boolean autoLogin = argumentList.fetchArgument("-autologin=false").booleanValue();
			if(autoLogin) {
				if(!(argumentList.hasArgument("-benutzer") && argumentList.hasArgument("-authentifizierung"))) {
					if(argumentList.hasArgument("-benutzer")) argumentList.fetchArgument("-benutzer");
					if(argumentList.hasArgument("-authentifizierung")) {
						argumentList.fetchArgument("-authentifizierung");
					}
					_debug.warning(
							"Der Login-Dialog wird trotz des Aufrufparameters '-autologin' aufgerufen, da die Parameter '-benutzer' und '-authentifizierung' "
							+ "unvollständig oder nicht korrekt angegeben wurden."
					);
					autoLogin = false;
				}
			}

			// ArgumentListe wird in ClientDavParameters konvertiert
			final ClientDavParameters parameters = new ClientDavParameters(argumentList);
			// zuerst darf die Applikation die ArgumentListe durcharbeiten
			parameters.setApplicationTypePid(applicationTypePid);
			parameters.setApplicationName(_applicationName);
			application.parseArguments(argumentList);
			argumentList.ensureAllArgumentsUsed();

//			System.out.println("IP: " + parameters.getDavCommunicationAddress() + " Port: " + parameters.getDavCommunicationSubAddress());
			ClientDavInterface connection = null;
			if(autoLogin) {
				connection = new ClientDavConnection(parameters);
				// Verbindung aufbauen (inkl. Anmelden zur Fertigmeldung für Start/Stop)
				establishConnection(connection);
			}
			else {
				// Login-Dialog aufrufen, der eine Verbindung zum Datenverteiler aufbaut und zurückgibt.
				connection = application.connect(parameters);

			}
			if(connection != null) {
				_applicationLabel.append(connection.getLocalConfigurationAuthority().getPid());	// ApplikationsKennung
				MessageSender.getInstance().init(connection, _applicationName, _applicationLabel.toString());
				application.initialize(connection);
				// Fertigmeldung senden
				connection.sendApplicationReadyMessage();
			}
			else {
				throw new RuntimeException("Die Verbindung zum Datenverteiler konnte nicht hergestellt werden.");
			}
		}
		catch(Exception ex) {
			_debug.error("Fehler in der Applikation", ex);
			System.exit(1);
		}
	}

	/**
	 * Diese Methode baut die Verbindung zum Datenverteiler auf und teilt mit, dass sich die Applikation selbst um die Fertigmeldung für Start/Stop kümmert.
     * @param connection aufzubauende Verbindung
     * @throws CommunicationError
     * @throws ConnectionException
     * @throws InconsistentLoginException
     */
    private static void establishConnection(final ClientDavInterface connection) throws CommunicationError, ConnectionException, InconsistentLoginException {
	    connection.enableExplicitApplicationReadyMessage();
	    connection.connect();
	    connection.login();
    }

	/**
	 * Diese Methode initialisiert den Debug-Logger.
	 *
	 * @param application Applikations-Objekt
	 * @param argumentList Aufrufargumente
	 */
	private static void initializeDebug(Object application, ArgumentList argumentList) {
		// Der Klassenname wird für die Initialisierung des Debug benötigt.
//		System.out.println("application.getClass().getName() = " + application.getClass().getName());
		final String[] classNameParts = application.getClass().getName().split("[.]");
//		System.out.println("classNameParts.length = " + classNameParts.length);
		final int lastPartIndex = classNameParts.length - 1;
		final String name;
		if(lastPartIndex < 0) {
			name = "StandardApplication";
		}
		else {
			name = classNameParts[lastPartIndex];
		}
		_applicationName = name;
		_applicationLabel.append(name);
		Debug.init(name, argumentList);
		_debug = Debug.getLogger();
	}

	/**
	 * Diese Methode wandelt die Aufrufargumente in einen String für die ApplikationsKennung um.
	 *
	 * @param args Aufrufargumente
	 */
	private static void createApplicationLabel(String[] args) {
		_applicationLabel = new StringBuilder();
		for(String arg : args) {
			_applicationLabel.append(arg);
		}
	}

	/**
	 * Implementierung eines UncaughtExceptionHandlers, der bei nicht abgefangenen Exceptions und Errors entsprechende Ausgaben macht und im Falle eines Errors den
	 * Prozess terminiert.
	 */
	private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		/**
		 * Strenger Fehlerbehandlungsmodus. Falls true wird bei jedem unbehandelten Laufzeitfehler terminiert, sonst nur bei Fehlern,
		 * die von {@link Error} abgeleitet sind (z.B. OutOfMemoryError).
		 */
		private final boolean _strictMode;
		
		/** Speicherreserve, die freigegeben wird, wenn ein Error auftritt, damit die Ausgaben nach einem OutOfMemoryError funktionieren */
		private volatile byte[] _reserve = new byte[20000];

		public UncaughtExceptionHandler(final ArgumentList argumentList) {
			_strictMode = argumentList.fetchArgument("-terminierenBeiException=nein").booleanValue();
		}

		public void uncaughtException(Thread t, Throwable e) {
			if(e instanceof Error) {
				// Speicherreserve freigeben, damit die Ausgaben nach einem OutOfMemoryError funktionieren
				_reserve = null;
				try {
					System.err.println("Schwerwiegender Laufzeitfehler: Ein Thread hat sich wegen eines Errors beendet, Prozess wird terminiert");
					System.err.println(t);
					e.printStackTrace(System.err);
					_debug.error("Schwerwiegender Laufzeitfehler: " + t + " hat sich wegen eines Errors beendet, Prozess wird terminiert", e);
				}
				catch(Throwable ignored) {
					// Weitere Fehler während der Ausgaben werden ignoriert, damit folgendes exit() auf jeden Fall ausgeführt wird.
				}
				System.exit(1);
			}
			else if(_strictMode){
				try {
					System.err.println("Schwerwiegender Laufzeitfehler: Ein Thread hat sich wegen einer Exception beendet, Prozess wird terminiert");
					System.err.println(t);
					e.printStackTrace(System.err);
					_debug.error("Schwerwiegender Laufzeitfehler: " + t + " hat sich wegen einer Exception beendet, Prozess wird terminiert", e);
				}
				catch(Throwable ignored) {
					// Weitere Fehler während der Ausgaben werden ignoriert, damit folgendes exit() auf jeden Fall ausgeführt wird.
				}
				System.exit(1);
			}
			else {
				System.err.println("Laufzeitfehler: Ein Thread hat sich wegen einer Exception beendet:");
				System.err.println(t);
				e.printStackTrace(System.err);
				_debug.error("Laufzeitfehler: " + t + " hat sich wegen einer Exception beendet", e);
			}
		}
	}
	
}

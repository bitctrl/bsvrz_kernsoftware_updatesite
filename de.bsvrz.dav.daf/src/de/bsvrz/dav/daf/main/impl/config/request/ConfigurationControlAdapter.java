/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.File;
import java.util.*;

// Kein Zugriff über ClientDavInterface, DataModel, das Objekt direkt erzeugen
/**
 * Diese Klasse stellt Methoden zur Verfügung mit denen Konfigurationsbereiche verwaltet werden können. Jeder Auftrag wird an die Konfiguration übertragen und
 * anschließend auf eine Antwort gewartet.
 * <p>
 * Ein Objekt der Klasse kann mit Hilfe einer ClientDaVConnection erzeugt werden.
 * <p>
 * Kommt es bei der Bearbeitung der Aufträge zu einer Exception entscheidet diese Klasse ob die Exception zur aufrufenden Instanz weitergeleitet wird oder ob
 * der Fehler so schwerwiegend ist, dass die Verbindung zum Datenverteiler abgebrochen werden muss.
 * <p>
 * Eine {@link de.bsvrz.dav.daf.main.config.ConfigurationChangeException} wird zum Anwender der Klasse weitergereicht.
 * <p>
 * Eine {@link RequestException} deutet auf einen schwerern Fehler innerhalb der Kommunikation hin, der nicht mehr behoben werden kann und führt zu einer
 * Abmeldung beim Datenverteiler.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationControlAdapter implements ConfigurationControl {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Verbindung zum Datenverteiler. Dies ist nicht das Interface, sondern die konkrete Implementierung. Das Objekt wird zum anfordern eines Requesters und zur
	 * Abmeldung beim Datenverteiler bei schweren Fehlern gebraucht.
	 */
	private final ClientDavConnection _clientDaVConnection;

	/** Über dieses Objekt findet die Anfrage an die Konfiguration statt. Schwerwiegende Fehler führen zu einer Terminierung der Verbindung. */
	private ConfigurationRequester _requester;

	public ConfigurationControlAdapter(ClientDavConnection clientDaVConnection) {
		_clientDaVConnection = clientDaVConnection;
		_requester = ((DafDataModel)_clientDaVConnection.getDataModel()).getRequester();
	}


	public Map<String, ConfigurationArea> getAllConfigurationAreas() {
		try {
			return _requester.getAllConfigurationAreas();
		}
		catch(RequestException e) {
			_debug.error("Alle Bereiche anfordern", e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public ConfigurationArea createConfigurationArea(String name, String pid, String authorityPid) throws ConfigurationChangeException {
		try {
			return _requester.createConfigurationArea(name, pid, authorityPid);
		}
		catch(RequestException e) {
			_debug.error("Erzeugen eines neuen Konfigurationsberichs fehgeschlagen, Name " + name + " Pid " + pid + " AuthorityPid " + authorityPid, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas) {
		try {
			return _requester.checkConsistency(configurationAreas);
		}
		catch(RequestException e) {
			_debug.error("Betroffene Bereiche " + configurationAreas, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException {
		try {
			return _requester.activateConfigurationAreas(configurationAreas);
		}
		catch(RequestException e) {
			_debug.error("Betroffene Bereiche " + configurationAreas, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException {
		try {
			return _requester.releaseConfigurationAreasForTransfer(configurationAreas);
		}
		catch(RequestException e) {
			_debug.error("Betroffene Bereiche " + configurationAreas, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException {
		try {
			_requester.releaseConfigurationAreasForActivation(configurationAreas);
		}
		catch(RequestException e) {
			_debug.error("Betroffene Bereiche " + configurationAreas, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws ConfigurationChangeException {
		try {
			_requester.importConfigurationAreas(importPath, configurationAreaPids);
		}
		catch(RequestException e) {
			_debug.error("Importverzeichnis " + importPath.getPath() + " Betroffene Bereiche " + configurationAreaPids, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws ConfigurationTaskException {
		try {
			_requester.exportConfigurationAreas(exportPath, configurationAreaPids);
		}
		catch(RequestException e) {
			_debug.error("Exportverzeichnis " + exportPath.getPath() + " Betroffene Bereiche " + configurationAreaPids, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}

	public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException {
		try {
			return _requester.releaseConfigurationAreasForActivationWithoutCAActivation(configurationAreas);
		}
		catch(RequestException e) {
			_debug.error("Betroffene Bereiche " + configurationAreas, e);
			_clientDaVConnection.disconnect(true, e.getMessage());
			throw new IllegalStateException("Die Verbindung zum Datenverteiler wurde terminiert, da es zu einem Fehler in der Kommunikation gekommen ist.");
		}
	}
}

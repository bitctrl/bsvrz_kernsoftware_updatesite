/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;

import java.util.*;

import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.InitialisationNotCompleteException;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValue;

/**
 * Klasse zum Zugriff auf den Zeitfluß einer Simulation. Nach dem Erzeugen eines Objekts dieser Klasse kann mit der Methode {@link #getTime()} auf die
 * simulierte Zeit der jeweiligen Simulation zugegriffen werden und mit den Methoden {@link #sleep} und {@link #sleepUntil} kann der aufrufende Thread für eine
 * bestimmte Zeit im Zeitfluß der Simulation blockiert werden. Die Methode {@link #close()} sollte aufgerufen werden, wenn das Objekt zum Zugriff auf den
 * Zeitfluß einer Simulation nicht mehr benötigt wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SimulationTime {

	private short _simulationVariant;

	private ClientDavConnection _connection;

	private boolean _onlineModus = true;

	private SystemObject _simObject;

	private AttributeGroup _simAttributeGroup;

	private Aspect _simAspect;

	private long _time = -1;

	private final Integer _timeNotification = new Integer(hashCode());

	private InternalReceiver _internalReceiver;

	/**
	 * Erzeugt ein neues Objekt zum Zugriff auf den Zeitfluß einer bestimmten Simulation. Im Falle einer Offline-Simulation findet eine Anmeldung auf die
	 * Simulationszeit der entsprechenden Simulationsvariante statt, die vom Simulationsdatengenerator zur Verfügung gestellt wird.
	 *
	 * @param simulationVariant Simulationsvariante der zu betrachtenden Simulation.
	 * @param connection        Datenverteilerverbindung über die die Kommunikation mit dem Simulationsdatengenerator durchgeführt wird.
	 *
	 * @throws ConfigurationException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SimulationTime(
			short simulationVariant, ClientDavConnection connection
	) throws ConfigurationException {

		if(connection == null) {
			throw new IllegalArgumentException("Inkonsistente Parameter");
		}
		_simulationVariant = simulationVariant;
		_connection = connection;
		DataModel dataModel = connection.getDataModel();
		if(dataModel == null) {
			throw new InitialisationNotCompleteException("Keine fertig initialisierte Verbindung zum Datenverteiler.");
		}
		_internalReceiver = new InternalReceiver();
		if(simulationVariant > 0) {
			String pid = null;
			if(simulationVariant < 10) {
				pid = "simulation.00" + simulationVariant;
			}
			else if(simulationVariant < 100) {
				pid = "simulation.0" + simulationVariant;
			}
			else {
				pid = "simulation." + simulationVariant;
			}
			_simObject = dataModel.getObject(pid);
			if(_simObject != null) {
				SystemObjectType simObjectType = _simObject.getType();
				if("typ.offlineSimulation".equals(simObjectType.getPid())) {
					_onlineModus = false;
				}
			}
		}
		if(!_onlineModus) {
			_simAttributeGroup = (AttributeGroup)dataModel.getObject("atg.simulationsZeit");
			_simAspect = (Aspect)dataModel.getObject("asp.standard");
			if((_simAttributeGroup == null) || (_simAspect == null)) {
				throw new ConfigurationException("Fehlerhafte Konfigurationsdaten.");
			}
			// atg.simulationsZeit:asp.standard immer mit Simulationsvariante 0 verwenden 
			connection.subscribeReceiver(
					_internalReceiver, _simObject,
					new DataDescription(_simAttributeGroup, _simAspect, (short)0),
					ReceiveOptions.normal(),
					ReceiverRole.receiver()
			);
		}
	}

	/**
	 * Bestimmt die Zeit einer Simulation. Bei einer Online-Simulation wird die aktuelle Zeit zurückgegeben und bei einer Offline-Simulation die simulierte Zeit.
	 *
	 * @return Zeitpunkt in Millisekunden seit 1970.
	 *
	 * @throws IllegalStateException Wenn der simulierte Zeitfluss im Falle einer Offline-Simulation nicht bestimmt werden kann.
	 * @see de.bsvrz.dav.daf.main.ClientDavInterface#getTime
	 */
	public final long getTime() {
		if(_onlineModus) {
			return System.currentTimeMillis();
		}
		if(_time == -1) {
			long waitingTime = 0, startTime = System.currentTimeMillis();
			long sleepTime = 10;
			while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
				try {
					synchronized(_timeNotification) {
						_timeNotification.wait(sleepTime);
						if(sleepTime < 1000) sleepTime *= 2;
					}
					if(_time != -1) {
						break;
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
				catch(InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		if(_time == -1) {
			throw new IllegalStateException("Die Zeit kann nicht bestimmt werden.");
		}
		return _time;
	}

	/**
	 * Blockiert den aufrufenden Thread für die spezifizierte Zeit. Die angegebene Dauer der Pause wird im Falle einer Online-Simulation in Realzeit und im Falle
	 * einer Offline-Simulation im Zeitfluss der Simulation berücksichtigt.
	 *
	 * @param timeToSleep Wartezeit in Millisekunden seit 1970.
	 *
	 * @throws IllegalStateException Wenn der simulierte Zeitfluss im Falle einer Offline-Simulation nicht bestimmt werden kann.
	 * @see de.bsvrz.dav.daf.main.ClientDavInterface#sleep
	 */
	public final void sleep(long timeToSleep) {
		if(_onlineModus) {
			try {
				Thread.currentThread().sleep(timeToSleep);
			}
			catch(InterruptedException ex) {
			}
		}
		else {
			long timeToWakeUp = getTime() + timeToSleep;
			synchronized(_timeNotification) {
				while(getTime() < timeToWakeUp) {
					try {
						_timeNotification.wait();
					}
					catch(InterruptedException ex) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Blockiert den aufrufenden Thread bis die spezifizierte Zeit erreicht ist. Der angegebene Zeitpunkt wird im Falle einer Online-Simulation in Realzeit und im
	 * Falle einer Offline-Simulation im Zeitfluss der Simulation berücksichtigt.
	 *
	 * @param absoluteTime Abzuwartender Zeitpunkt in Millisekunden seit 1970.
	 *
	 * @throws IllegalStateException Wenn der simulierte Zeitfluss im Falle einer Offline-Simulation nicht bestimmt werden kann.
	 * @see de.bsvrz.dav.daf.main.ClientDavInterface#sleepUntil
	 */
	public final void sleepUntil(long absoluteTime) {
		if(_onlineModus) {
			while(System.currentTimeMillis() < absoluteTime) {
				try {
					Thread.currentThread().sleep(100);
				}
				catch(InterruptedException ex) {
				}
			}
		}
		else {
			synchronized(_timeNotification) {
				while(getTime() < absoluteTime) {
					try {
						_timeNotification.wait();
					}
					catch(InterruptedException ex) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Terminiert dieses Objekt und veranlasst bei einer Offline-Simulation die notwendige Abmeldung der Simulationszeiten.
	 *
	 * @throws ConfigurationException Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public final void close() throws ConfigurationException {
		if(!_onlineModus) {
			// atg.simulationsZeit:asp.standard immer mit Simulationsvariante 0 verwenden
			_connection.unsubscribeReceiver(
					_internalReceiver, _simObject, new DataDescription(_simAttributeGroup, _simAspect, (short)0)
			);
		}
	}

	private class InternalReceiver implements ClientReceiverInterface {

		InternalReceiver() {
		}

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird. Diese Methode muss
		 * von der Applikation zur Verarbeitung der empfangenen Datensätze implementiert werden.
		 *
		 * @param results Feld mit den empfangenen Ergebnisdatensätzen.
		 */
		public final void update(ResultData results[]) {
			if(results == null) {
				return;
			}
			if(!_onlineModus) {
				for(int i = 0; i < results.length; ++i) {
					if(results[i] != null) {
						DataDescription dataDescription = results[i].getDataDescription();
						if(dataDescription != null) {
							if(!_simAttributeGroup.equals(dataDescription.getAttributeGroup())) {
								continue;
							}
							if(!_simAspect.equals(dataDescription.getAspect())) {
								continue;
							}
							final Data data = results[i].getData();
							if(data != null) {
								_time = data.getTimeValue("Zeit").getMillis();
							}
						}
					}
				}
				synchronized(_timeNotification) {
					_timeNotification.notifyAll();
				}
			}
		}
	}
}

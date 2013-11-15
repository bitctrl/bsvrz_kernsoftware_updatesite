/*
 * Copyright 2009 by Kappich Systemberatung Aachen 
 * Copyright 2006 by Kappich Systemberatung Aachen 
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

package de.bsvrz.puk.config.main.simulation;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Diese Klasse stellt den Zustand "Bereit" der Konfiguration f�r eine Simulation dar. Es sind alle Zustands�berg�nge vorhanden um in die Zust�nde NichtBereit
 * und Gel�scht zu wechseln.
 * <p/>
 * Einige Zustands�berg�nge, zu Beispiel das erneute Aufrufen von Start, f�hren zu keinem Zustandswechsel.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class Ready implements SimulationStates {

	/**
	 * Sobald die Methode {@link #noSource()} aufgerufen wird, wird ein Timer gestartet. Dieser Timer l�uft parametrierbare Zeitspanne, l�uft der Timer ab, wird
	 * der Zustand "NotReady" gewechselt. Wird die Methode {@link #noSource()} erneut aufgerufen, wird kein neuer Timer gestartet, da dies zum erneuten Wechsel
	 * f�hren k�nnte.
	 */
	private static final Timer _noSourceTimer = new Timer("ConfigurationSimulationNoSourceTimer");

	private TimerTask _timer = new NoSourceEvent();

	private boolean _timerIsRunning = false;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Bestimmt wie lang gewartet wird, sobald die Verbindung zur Simulationssteuerung nicht mehr zur Verf�gung steht. L�uft die Zeit ab, wird in den Zustand
	 * "nicht bereit" gewechselt.
	 */
	private long _timeout = 30000;

	/** Wird benutzt um die Variable <code>_timeout</code> zu sperren, wenn lesend oder schreibend auf den Wert zugegriffen werden soll. */
	private final Object _lockTimeOut = new Object();

	private final ConfigSimulationObject _simulationObject;

	private final Util _util;

	/**
	 * Erzeugt das Objekt und meldet sich auf einen Parameter an.
	 * @param simulationObject
	 * @param util
	 * @param connection
	 */
	public Ready(final ConfigSimulationObject simulationObject, Util util, ClientDavInterface connection) {
		_simulationObject = simulationObject;
		_util = util;

		TimeOutValueReceiver timeOutValueReceiver = new TimeOutValueReceiver();

		final DataModel dataModel = connection.getDataModel();

		final AttributeGroup atg = dataModel.getAttributeGroup("atg.simulationsTimeoutKeineQuelleKonfiguration");
		final Aspect aspect = dataModel.getAspect("asp.parameterSoll");
		final DataDescription dataDescription = new DataDescription(atg, aspect);

		



	}

	public void preStart() {
		// Der Zustand wird nicht ge�ndert
	}

	public void start() {
		// Der Zustand wird nicht ge�ndert
	}

	public void stop() {
		try {
			_util.doStop();
			_simulationObject.setState(_simulationObject.getNotReadyState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error(
					"Die Konfiguration kann m�gliche Applikationen nicht benachrichtigen, dass f�r eine Simuation ein Zustandwechsel stattgefunden hat.",
					sendSubscriptionNotConfirmed
			);
		}
	}

	public void pause() {
		// Der Zustand wird nicht ge�ndert
	}

	public void delete() {
		try {
			_util.sendNotReady();
			_util.doDelete();
			// Die Konfiguration f�r die Simulation in den Zustand "gel�scht" �berf�hren
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer  Simulation in den Zustand L�schen ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public void noSource() {
		if(!_timerIsRunning) {
			// Einen neuen Timer anlegen, vielleicht wurde der alte Bereits durchgef�hrt oder terminiert.
			_timer = new NoSourceEvent();
			_noSourceTimer.schedule(_timer, getTimeout());
		}
	}

	private long getTimeout() {
		synchronized(_lockTimeOut) {
			return _timeout;
		}
	}

	private void setTimeout(final long timeout) {
		synchronized(_lockTimeOut) {
			_timeout = timeout;
		}
	}

	public void removedFromSet() {
		try {
			_util.sendNotReady();
			// Alle Daten der Simulation l�schen
			_util.doDelete();
			// Die Konfiguration f�r die Simulation in den Zustand "gel�scht" �berf�hren
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Entfernen einer  Simulation ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public String toString()
	{
		return "Zustand: Bereit";
	}

	/** Wird ausgef�hrt, wenn eine parametrierbare Zeitspanne abgelaufen ist. Ist dies der Fall, wird der Zustand "nicht bereit" angenommen */
	private final class NoSourceEvent extends TimerTask {

		public void run() {
			stop();
			// erlaubt es andere Timer zu erzeugen
			_timerIsRunning = false;
		}
	}

	/** Empf�ngt einen Datenatz von der Parametrierung in dem die Zeit gespeichert ist. */
	private class TimeOutValueReceiver implements ClientReceiverInterface {

		public void update(ResultData results[]) {
			synchronized(_lockTimeOut) {
				for(int i = 0; i < results.length; i++) {
					final ResultData result = results[i];
					final Data data = result.getData();

					if(data != null) {

						final long newTimeOut = data.getTimeValue("Wartezeit").getMillis();
						final long oldValue = getTimeout();
						setTimeout(newTimeOut);
						_debug.info("Alter Timeout f�r keine Quelle: " + oldValue + " neuer Wert: " + getTimeout());
					}
				}
			}
		}
	}
}

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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Definiert einen Trigger mit verz�gerter Ausl�sung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5897 $
 */
public class DelayedTrigger implements Trigger {

	/** Listener, die beim Ausl�sen und Schlie�en des Triggers benachrichtigt werden sollen. */
	private final List<TriggerTarget> _triggerTargets = new CopyOnWriteArrayList<TriggerTarget>();

	/** Anzahl der Trigger-Aufrufe, nach der die sofortige Ausl�sung angesto�en wird. */
	private int _maximumDelayedTriggerCount;

	/** Aktuelle Anzahl der Trigger-Aufrufe. */
	private int _delayedTriggerCount = 0;

	/** Verz�gerungszeit in der nach einem Trigger-Aufrufe auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige Ausl�sung angesto�en wird. */
	private long _delayDuration;

	/**
	 * Zeitpunkt an dem die sofortige Ausl�sung angesto�en wird, wenn nicht vorher ein weiterer Trigger-Aufruf stattfindet. Mit jedem neuen Trigger-Aufruf wird
	 * dieses Feld auf die aktuelle Zeit plus <code>_delayDuration</code> gesetzt.
	 */
	private long _triggeringTime;

	/**
	 * Maximale Verz�gerungszeit in der nach dem jeweils ersten Trigger-Aufruf auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige Ausl�sung angesto�en
	 * wird.
	 */
	private long _maximumDelayDuration;

	/**
	 * Zeitpunkt an dem die sofortige Ausl�sung angesto�en wird. Mit dem jeweils ersten verz�gerten Ansto� wird dieses Feld auf die aktuelle Zeit plus
	 * <code>_maximumDelayDuration</code> gesetzt.
	 */
	private long _maximumTriggeringTime;

	/** <code>true</code>, wenn die close-Methode aufgerufen wurde; sonst <code>false</code>. */
	private boolean _closed = false;

	/**
	 * Erzeugt ein neues Triggerobjekt mit den angegebenen Eigenschaften
	 *
	 * @param threadName                 Name des Threads f�r die asynchrone Ausl�sung des Triggers.
	 * @param maximumDelayedTriggerCount Anzahl der Trigger-Aufrufe, nach der die sofortige Ausl�sung angesto�en wird.
	 * @param delayDuration              Verz�gerungszeit in der nach einem Trigger-Aufrufe auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige
	 *                                   Ausl�sung angesto�en wird.
	 * @param maximumDelayDuration       Maximale Verz�gerungszeit in der nach dem jeweils ersten Trigger-Aufruf auf weitere Trigger-Aufrufe gewartet wird, bevor
	 *                                   die sofortige Ausl�sung angesto�en wird.
	 */
	public DelayedTrigger(final String threadName, final int maximumDelayedTriggerCount, final long delayDuration, final long maximumDelayDuration) {
		_maximumDelayDuration = maximumDelayDuration;
		_delayDuration = delayDuration;
		_maximumDelayedTriggerCount = maximumDelayedTriggerCount;
		final Thread asyncTriggerThread = new Thread(new AsyncTriggerThreadRunnable(), threadName);
		asyncTriggerThread.setDaemon(true);
		asyncTriggerThread.start();
	}

	/** F�hrt zu einer verz�gerten Ausl�sung des Triggers mit einer asynchronen Benachrichtigung aller angemeldeten TriggerTargets */
	public void trigger() {
		synchronized(this) {
			if(_closed) throw new IllegalStateException("Trigger wurde bereits geschlossen");
			long now = System.currentTimeMillis();
			if((_delayedTriggerCount++) == 0) {
				_maximumTriggeringTime = now + _maximumDelayDuration;
				this.notifyAll();
			}
			_triggeringTime = now + _delayDuration;
			if(_delayedTriggerCount >= _maximumDelayedTriggerCount) this.notifyAll();
		}
	}

	/** F�hrt zu einer sofortigen Ausl�sung des Triggers mit einer asynchronen Benachrichtigung aller angemeldeten TriggerTargets */
	public void shoot() {
		synchronized(this) {
			trigger();
			_maximumTriggeringTime = System.currentTimeMillis();
			this.notifyAll();
		}
	}

	/**
	 * Schlie�t den Trigger. Die sofortige Ausl�sung des Triggers wird angesto�en, falls vorherige verz�gerte Trigger-Aufrufe noch nicht zu einer Ausl�sung des
	 * Triggers gef�hrt haben. Alle angemeldeten TriggerTargets werden anschlie�end �ber das Schlie�en des Triggers benachrichtigt. Der Thread zur asynchronen
	 * Benachrichtigung der TriggerTargets wird beendet.
	 */
	public void close() {
		synchronized(this) {
			_closed = true;
			this.notifyAll();
		}
	}

	/**
	 * Wartet bis der Trigger ausgel�st werden soll oder der Trigger geschlossen wurde.
	 *
	 * @return <code>false</code>, wenn der Trigger ausgel�st werden kann; <code>true</code>, wenn der Trigger geschlossen wurde.
	 *
	 * @throws InterruptedException Wenn der Thread vor oder w�hrend des Wartens unterbrochen wurde.
	 */
	private boolean awaitTrigger() throws InterruptedException {
		synchronized(this) {
			while(_delayedTriggerCount == 0 && !_closed) wait();
			if(_delayedTriggerCount == 0 && _closed) return true;
			while(true) {
				long now = System.currentTimeMillis();
				if(_closed || _delayedTriggerCount >= _maximumDelayedTriggerCount || now >= _triggeringTime || now >= _maximumTriggeringTime) {
					_delayedTriggerCount = 0;
					return false;
				}
				long delay;
				if(_maximumTriggeringTime < _triggeringTime) {
					delay = _maximumTriggeringTime - now;
				}
				else {
					delay = _triggeringTime - now;
				}
				if(delay>0)wait(delay);
			}
		}
	}

	public void addTriggerTarget(TriggerTarget triggerTarget) {
		_triggerTargets.add(triggerTarget);
	}

	public void removeTriggerTarget(TriggerTarget triggerTarget) {
		_triggerTargets.remove(triggerTarget);
	}

	/** Benachrichtigt alle TriggerTargets �ber das Ausl�sen des Triggers */
	private void shootTriggerTargets() {
		for(TriggerTarget triggerTarget : _triggerTargets) {
			triggerTarget.shot();
		}
	}

	/** Benachrichtigt alle TriggerTargets �ber das Ausl�sen des Triggers */
	private void closeTriggerTargets() {
		for(TriggerTarget triggerTarget : _triggerTargets) {
			triggerTarget.close();
		}
	}

	/** Runnable mit der run-Methode, die in einem eigenen Thread ausgef�hrt wird und die TriggerTargets asynchron benachrichtigt. */
	private class AsyncTriggerThreadRunnable implements Runnable {

		public void run() {
			try {
				while(!Thread.interrupted()) {
					final boolean closed = awaitTrigger();
					if(closed) return;
					shootTriggerTargets();
				}
			}
			catch(InterruptedException e) {
				// Thread beendet sich
			}
			finally {
				closeTriggerTargets();
			}
		}
	}
}

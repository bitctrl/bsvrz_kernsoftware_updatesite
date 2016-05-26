/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.concurrent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Definiert einen Trigger mit verzögerter Auslösung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DelayedTrigger implements Trigger {

	/** Listener, die beim Auslösen und Schließen des Triggers benachrichtigt werden sollen. */
	private final List<TriggerTarget> _triggerTargets = new CopyOnWriteArrayList<TriggerTarget>();

	/** Anzahl der Trigger-Aufrufe, nach der die sofortige Auslösung angestoßen wird. */
	private int _maximumDelayedTriggerCount;

	/** Aktuelle Anzahl der Trigger-Aufrufe. */
	private int _delayedTriggerCount = 0;

	/** Verzögerungszeit in der nach einem Trigger-Aufrufe auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige Auslösung angestoßen wird. */
	private long _delayDuration;

	/**
	 * Zeitpunkt an dem die sofortige Auslösung angestoßen wird, wenn nicht vorher ein weiterer Trigger-Aufruf stattfindet. Mit jedem neuen Trigger-Aufruf wird
	 * dieses Feld auf die aktuelle Zeit plus <code>_delayDuration</code> gesetzt.
	 */
	private long _triggeringTime;

	/**
	 * Maximale Verzögerungszeit in der nach dem jeweils ersten Trigger-Aufruf auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige Auslösung angestoßen
	 * wird.
	 */
	private long _maximumDelayDuration;

	/**
	 * Zeitpunkt an dem die sofortige Auslösung angestoßen wird. Mit dem jeweils ersten verzögerten Anstoß wird dieses Feld auf die aktuelle Zeit plus
	 * <code>_maximumDelayDuration</code> gesetzt.
	 */
	private long _maximumTriggeringTime;

	/** <code>true</code>, wenn die close-Methode aufgerufen wurde; sonst <code>false</code>. */
	private boolean _closed = false;

	/**
	 * Erzeugt ein neues Triggerobjekt mit den angegebenen Eigenschaften
	 *
	 * @param threadName                 Name des Threads für die asynchrone Auslösung des Triggers.
	 * @param maximumDelayedTriggerCount Anzahl der Trigger-Aufrufe, nach der die sofortige Auslösung angestoßen wird.
	 * @param delayDuration              Verzögerungszeit in der nach einem Trigger-Aufrufe auf weitere Trigger-Aufrufe gewartet wird, bevor die sofortige
	 *                                   Auslösung angestoßen wird.
	 * @param maximumDelayDuration       Maximale Verzögerungszeit in der nach dem jeweils ersten Trigger-Aufruf auf weitere Trigger-Aufrufe gewartet wird, bevor
	 *                                   die sofortige Auslösung angestoßen wird.
	 */
	public DelayedTrigger(final String threadName, final int maximumDelayedTriggerCount, final long delayDuration, final long maximumDelayDuration) {
		_maximumDelayDuration = maximumDelayDuration;
		_delayDuration = delayDuration;
		_maximumDelayedTriggerCount = maximumDelayedTriggerCount;
		final Thread asyncTriggerThread = new Thread(new AsyncTriggerThreadRunnable(), threadName);
		asyncTriggerThread.setDaemon(true);
		asyncTriggerThread.start();
	}

	/** Führt zu einer verzögerten Auslösung des Triggers mit einer asynchronen Benachrichtigung aller angemeldeten TriggerTargets */
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

	/** Führt zu einer sofortigen Auslösung des Triggers mit einer asynchronen Benachrichtigung aller angemeldeten TriggerTargets */
	public void shoot() {
		synchronized(this) {
			trigger();
			_maximumTriggeringTime = System.currentTimeMillis();
			this.notifyAll();
		}
	}

	/**
	 * Schließt den Trigger. Die sofortige Auslösung des Triggers wird angestoßen, falls vorherige verzögerte Trigger-Aufrufe noch nicht zu einer Auslösung des
	 * Triggers geführt haben. Alle angemeldeten TriggerTargets werden anschließend über das Schließen des Triggers benachrichtigt. Der Thread zur asynchronen
	 * Benachrichtigung der TriggerTargets wird beendet.
	 */
	public void close() {
		synchronized(this) {
			_closed = true;
			this.notifyAll();
		}
	}

	/**
	 * Wartet bis der Trigger ausgelöst werden soll oder der Trigger geschlossen wurde.
	 *
	 * @return <code>false</code>, wenn der Trigger ausgelöst werden kann; <code>true</code>, wenn der Trigger geschlossen wurde.
	 *
	 * @throws InterruptedException Wenn der Thread vor oder während des Wartens unterbrochen wurde.
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

	/** Benachrichtigt alle TriggerTargets über das Auslösen des Triggers */
	private void shootTriggerTargets() {
		for(TriggerTarget triggerTarget : _triggerTargets) {
			triggerTarget.shot();
		}
	}

	/** Benachrichtigt alle TriggerTargets über das Auslösen des Triggers */
	private void closeTriggerTargets() {
		for(TriggerTarget triggerTarget : _triggerTargets) {
			triggerTarget.close();
		}
	}

	/** Runnable mit der run-Methode, die in einem eigenen Thread ausgeführt wird und die TriggerTargets asynchron benachrichtigt. */
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

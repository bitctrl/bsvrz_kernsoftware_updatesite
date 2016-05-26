/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

/**
 * Diese Klasse implementiert ein Semaphor, das zur Synchronisation von Threads verwendet werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class Semaphore {

	/**
	 * Diese Variable gibt an, wie oft eine Sperre geholt werden darf (wie oft ein kritischer Abschnitt betreten werden
	 * kann). Ist die untere Grenze (0) erreicht legt jeder Thread sich automatsich schlafen(wait), der versucht auf dieses
	 * Objekt ein <code> acquire </code> auszuführen.
	 */
	private int _permits;

	/**
	 * Wie viele Threads dürfen maximal in den kritischen Abschnitt. Diese Variable verhindert, dass das die Variable
	 * _permits beliebig mit <code> release </code> erhöht werden kann. Diese Variable kann mit set-Methode erhöht werden.
	 */
	private int _upperBoundPermit;

	/**
	 * Einen Semaphor erzeugen, der <code> permit </code> viele <code> acquire </code> Zugriffe zuläßt, bis er jeden
	 * weiteren Thread, der <code> acquire </code> aufruft, mit wait "schlafen" legt.
	 *
	 * @param permits Wie viele Threads dürfen in den kritischen Abschnitt
	 */

	public Semaphore(int permits) {
		_permits = permits;
		_upperBoundPermit = permits;
	}

	/**
	 * Eine Sperre des Semaphores anfordern, sind keine Sperren mehr zu vergeben, dann wird der Thread mit wait()
	 * angehalten. Ein anderer Thread, der seine Sperre wieder aufgibt, kann den wartenden Thread wieder befreien.
	 */
	public void acquire() {
		// Es wird über das gesamte Objekt synchronisiert
		synchronized (this) {
			while (_permits == 0) {
				// Solange schlafen, bis ein permit (Sperre) frei ist
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// einmal sperren
			_permits--;
		}
	}

	/**
	 * Eine Sperre des Semaphores freigeben, dies befreit andere Threads, die auf eine Sperre warten.
	 */
	public void release() {
		synchronized (this) {
			assert _permits < _upperBoundPermit : "_permits = " + (_permits + 1) + " _upperBoundPermit = " + _upperBoundPermit;
			// Einen anderen Thread Zugriff auf einen kritischen Abschnitt erlauben
			if (_permits < _upperBoundPermit) {
				_permits++;
			}
			// Einen Thread, der mit wait warten könnte, freilassen.
			this.notifyAll();
		}
	}

	/**
	 * Diese Methode setzt die Anzahl der Sperren neu. Es ist somit zur Laufzeit möglich, dem Semaphor neue Sperren zu
	 * geben (entweder Sperren entziehen oder neue Sperren freischalten). Gibt es neue Sperren, dann werden sofort alle
	 * Threads, die warten, mit notifyAll darauf hingewiesen.
	 * <p>
	 * TBD Dies ist keine "normale" Funktion, die auf Semaphoren angewandt wird. Ist diese Methode hier in Ordnung ?
	 *
	 * @param upperBoundPermit Neue Obergrenze, soviele Sperren stehen nun zur Verfügung
	 */
	public void setUpperBoundPermit(int upperBoundPermit) {

		synchronized (this) {
			if (upperBoundPermit > _upperBoundPermit) {
				// Es wurden neue Sperren vergeben, somit könnten wartende Threads aufgeweckt werden.
				_upperBoundPermit = upperBoundPermit;
				this.notifyAll();
			} else {
				// die neue Grenze liegt niedriger, somit können keine Threads aufgeweckt werden.
				_upperBoundPermit = upperBoundPermit;
			}
		}
	}
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.filelock.
 * 
 * de.bsvrz.sys.funclib.filelock is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.filelock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.filelock; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.filelock;

import java.io.File;
import java.io.IOException;

/**
 * Diese Klasse stellt einen Mechanismus zur Verf�gung, mit dem gepr�ft werden kann, ob sich eine Datei im Zugriff einer anderen Applikation befindet, die
 * ebenfalls diesen Mechansimus verwendet.
 * <p/>
 * Damit oben beschriebene Aufgabe realisiert werden kann, wird im Konstruktor der Klasse eine Datei angegeben, die gegen unerlaubten Zugriff gesch�tzt werden
 * soll. Soll die �bergebene Datei gesch�tzt werden, wird eine entsprechende Methode aufgerufen. Dieser Methodenaufruf pr�ft, ob eine Datei mit der Endung
 * ".lock" existiert, der restliche Pfad ist gleich der zu sch�tzenden Datei.
 * <p/>
 * Ist so eine Datei vorhanden, wird eine Exception geworfen, da sich zu sch�tzende Datei bereits im Zugriff befindet.
 * <p/>
 * Kann eine Datei angelegt werden, wird der Methodenaufruf keine weiteren Auswirkungen habe. Legt eine zweite Applikation eine Instanz dieser Klasse an und
 * �bergibt die gleiche Datei im Konstruktor und ruft die Methode zum sperren der Datei auf, wird automatisch eine Exception geworfen.
 * <p/>
 * Die Klasse enth�lt ebenfalls eine Methode zum l�schen der Datei mit der Endung ".lock". Wurde die Datei gel�scht, k�nnen andere Applikationen die Datei
 * wieder sperren.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5010 $
 */
public class FileLock {

	/** Datei, die �ber den lock-Mechanismus gesch�tzt werden soll. */
	private final File _protectedFile;

	private final File _lockFile;

	/** Endung, die an die Datei geh�ngt wird, die anzeigt, dass sich die zu sch�tzende Datei im Zugriff befindet. */
	private static final String _lockFileEnding = ".lock";

	/**
	 * Legt ein Objekt an, die �bergebene Datei wird nicht gegen den Zugriff gesichert. Dies muss expliziet mit dem Aufruf von {@link #lock()} geschehen.
	 *
	 * @param protectedFile Datei, die gesperrt werden soll.
	 */
	public FileLock(final File protectedFile) {
		_protectedFile = protectedFile;
		_lockFile = new File(_protectedFile.getParent(), (_protectedFile.getName() + _lockFileEnding));
	}

	/**
	 * Zeigt an, dass die im Konstruktor �bergebene Datei im Zugriff einer Applikation ist. Jeder weitere Aufruf ohne vorher {@link #unlock()} aufzurufen wird zu
	 * einer IllegalStateException f�hren.
	 *
	 * @throws IllegalStateException Die Datei wurde bereits gesperrt und befindet sich somit im Zugriff.
	 */
	public void lock() throws IOException {
		if(!_lockFile.createNewFile())
		{
			// Die Datei war bereits vorhanden. Also wird die Datei bereits durch diesen Mechansimus gesch�tzt und befindet sich im Zugriff.
			throw new IllegalStateException("Die Datei " + _protectedFile.getAbsolutePath() + " ist bereits gesperrt worden und kann nicht erneut gesperrt werden.");
		}else
		{
//			System.out.println("Erzeuge Lock: " + _lockFile.getAbsolutePath());
		}
	}

	/** Gibt die Datei, die im Konstruktor �bergeben wurde wieder frei. Nach Aufruf dieser Methode kann die Methode {@link #lock()} wieder aufgerufen werden. */
	public void unlock() {
		_lockFile.delete();
	}
}

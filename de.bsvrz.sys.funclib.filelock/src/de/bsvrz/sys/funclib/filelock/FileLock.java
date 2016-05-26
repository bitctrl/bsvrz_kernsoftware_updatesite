/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.filelock.
 * 
 * de.bsvrz.sys.funclib.filelock is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.filelock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.filelock; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.filelock;

import java.io.File;
import java.io.IOException;

/**
 * Diese Klasse stellt einen Mechanismus zur Verfügung, mit dem geprüft werden kann, ob sich eine Datei im Zugriff einer anderen Applikation befindet, die
 * ebenfalls diesen Mechansimus verwendet.
 * <p>
 * Damit oben beschriebene Aufgabe realisiert werden kann, wird im Konstruktor der Klasse eine Datei angegeben, die gegen unerlaubten Zugriff geschützt werden
 * soll. Soll die übergebene Datei geschützt werden, wird eine entsprechende Methode aufgerufen. Dieser Methodenaufruf prüft, ob eine Datei mit der Endung
 * ".lock" existiert, der restliche Pfad ist gleich der zu schützenden Datei.
 * <p>
 * Ist so eine Datei vorhanden, wird eine Exception geworfen, da sich zu schützende Datei bereits im Zugriff befindet.
 * <p>
 * Kann eine Datei angelegt werden, wird der Methodenaufruf keine weiteren Auswirkungen habe. Legt eine zweite Applikation eine Instanz dieser Klasse an und
 * übergibt die gleiche Datei im Konstruktor und ruft die Methode zum sperren der Datei auf, wird automatisch eine Exception geworfen.
 * <p>
 * Die Klasse enthält ebenfalls eine Methode zum löschen der Datei mit der Endung ".lock". Wurde die Datei gelöscht, können andere Applikationen die Datei
 * wieder sperren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class FileLock {

	/** Datei, die über den lock-Mechanismus geschützt werden soll. */
	private final File _protectedFile;

	private final File _lockFile;

	/** Endung, die an die Datei gehängt wird, die anzeigt, dass sich die zu schützende Datei im Zugriff befindet. */
	private static final String _lockFileEnding = ".lock";

	/**
	 * Legt ein Objekt an, die übergebene Datei wird nicht gegen den Zugriff gesichert. Dies muss expliziet mit dem Aufruf von {@link #lock()} geschehen.
	 *
	 * @param protectedFile Datei, die gesperrt werden soll.
	 */
	public FileLock(final File protectedFile) {
		_protectedFile = protectedFile;
		_lockFile = new File(_protectedFile.getParent(), (_protectedFile.getName() + _lockFileEnding));
	}

	/**
	 * Zeigt an, dass die im Konstruktor übergebene Datei im Zugriff einer Applikation ist. Jeder weitere Aufruf ohne vorher {@link #unlock()} aufzurufen wird zu
	 * einer IllegalStateException führen.
	 *
	 * @throws IllegalStateException Die Datei wurde bereits gesperrt und befindet sich somit im Zugriff.
	 */
	public void lock() throws IOException {
		if(!_lockFile.createNewFile())
		{
			// Die Datei war bereits vorhanden. Also wird die Datei bereits durch diesen Mechansimus geschützt und befindet sich im Zugriff.
			throw new IllegalStateException("Die Datei " + _protectedFile.getAbsolutePath() + " ist bereits gesperrt worden und kann nicht erneut gesperrt werden.");
		}else
		{
//			System.out.println("Erzeuge Lock: " + _lockFile.getAbsolutePath());
		}
	}

	/** Gibt die Datei, die im Konstruktor übergeben wurde wieder frei. Nach Aufruf dieser Methode kann die Methode {@link #lock()} wieder aufgerufen werden. */
	public void unlock() {
		_lockFile.delete();
	}
}

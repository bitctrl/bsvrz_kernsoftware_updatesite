/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.dav.daf.main.archive;

import java.util.List;

/**
 * Schnittstelle zum Zugriff auf Ergebnisse von Archivinformationsanfragen. Diese Schnittstelle wird von Applikationen
 * benutzt, um auf Ergebnisse von Archivinformationsanfragen zuzugreifen, die mit den Methoden {@link
 * ArchiveRequestManager#requestInfo} gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben den
 * Methoden des übergeordneten Interfaces {@link ArchiveQueryResult} eine Methode zur Verfügung, mit der auf die
 * angefragten Informationen zugegriffen werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 * @see ArchiveRequestManager#requestInfo(ArchiveDataSpecification)
 * @see ArchiveRequestManager#requestInfo(java.util.List)
 */
public interface ArchiveInfoQueryResult extends ArchiveQueryResult {
	/**
	 * Eine Implementation dieser Methode gibt eine Liste mit ArchiveInformationResults zurück.
	 * @return Objekt, das Informationen über die angefragten Daten enthält
	 */
	List<ArchiveInformationResult> getArchiveInfoQueryResult();
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.archive;

/**
 * Diese Klasse ermöglicht es in einer Archivanfrage eine Kombination von mehreren {@link ArchiveDataKind} zu erzeugen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ArchiveDataKindCombination {

	/**
	 * ArchiveDataKind ONLINE
	 */
	private final boolean _online;
	/**
	 * ArchiveDataKind ONLINE_DELAYED
	 */
	private final boolean _onlineDelayed;
	/**
	 * ArchiveDataKind REQUESTED
	 */
	private final boolean _requested;
	/**
	 * ArchiveDataKind REQUESTED_DELAYED
	 */
	private final boolean _requestedDelayed;

	/**
	 * Dieser Construktor kann eine Kombination von vier ArchiveDataKind Objekten verarbeiten und ein entsprechendes Objekt
	 * zur Verfügung stellen.
	 *
	 * @param dataKind1
	 * @param dataKind2
	 * @param dataKind3
	 * @param dataKind4
	 */
	public ArchiveDataKindCombination(ArchiveDataKind dataKind1,
									  ArchiveDataKind dataKind2,
									  ArchiveDataKind dataKind3,
									  ArchiveDataKind dataKind4) {
		_online = (dataKind1 == ArchiveDataKind.ONLINE || dataKind2 == ArchiveDataKind.ONLINE || dataKind3 == ArchiveDataKind.ONLINE || dataKind4 == ArchiveDataKind.ONLINE);
		_onlineDelayed = (dataKind1 == ArchiveDataKind.ONLINE_DELAYED || dataKind2 == ArchiveDataKind.ONLINE_DELAYED || dataKind3 == ArchiveDataKind.ONLINE_DELAYED || dataKind4 == ArchiveDataKind.ONLINE_DELAYED);
		_requested = (dataKind1 == ArchiveDataKind.REQUESTED || dataKind2 == ArchiveDataKind.REQUESTED || dataKind3 == ArchiveDataKind.REQUESTED || dataKind4 == ArchiveDataKind.REQUESTED);
		_requestedDelayed = (dataKind1 == ArchiveDataKind.REQUESTED_DELAYED || dataKind2 == ArchiveDataKind.REQUESTED_DELAYED || dataKind3 == ArchiveDataKind.REQUESTED_DELAYED || dataKind4 == ArchiveDataKind.REQUESTED_DELAYED);
	}

	/**
	 * Siehe Konstruktor mit 4 Eingabeparametern, dieser ist identisch nur mit 3 Objekten.
	 *
	 * @param dataKind1
	 * @param dataKind2
	 * @param dataKind3
	 */
	public ArchiveDataKindCombination(ArchiveDataKind dataKind1, ArchiveDataKind dataKind2, ArchiveDataKind dataKind3) {
		this(dataKind1, dataKind2, dataKind3, dataKind3);
	}

	/**
	 * Siehe Konstruktor mit 4 Eingabeparametern, dieser ist identisch nur mit 2 Objekten.
	 *
	 * @param dataKind1
	 * @param dataKind2
	 */
	public ArchiveDataKindCombination(ArchiveDataKind dataKind1, ArchiveDataKind dataKind2) {
		this(dataKind1, dataKind2, dataKind2, dataKind2);
	}

	/**
	 * Siehe Konstruktor mit 4 Eingabeparametern, dieser ist identisch nur mit einem Objekt.
	 *
	 * @param dataKind1
	 */
	public ArchiveDataKindCombination(ArchiveDataKind dataKind1) {
		this(dataKind1, dataKind1, dataKind1, dataKind1);
	}

	/**
	 * Diese Methode gibt an, ob <code>ArchiveDataKind.ONLINE</code> im Konstruktor übergeben wurde
	 *
	 * @return true = <code>ArchiveDataKind.ONLINE</code> wurde gewählt;
	 */
	public boolean isOnline() {
		return _online;
	}

	/**
	 * Diese Methode gibt an, ob <code>ArchiveDataKind.ONLINE_DELAYED</code> im Konstruktor übergeben wurde
	 *
	 * @return true = <code>ArchiveDataKind.ONLINE_DELAYED</code> wurde gewählt;
	 */
	public boolean isOnlineDelayed() {
		return _onlineDelayed;
	}

	/**
	 * Diese Methode gibt an, ob <code>ArchiveDataKind.REQUESTED</code> im Konstruktor übergeben wurde
	 *
	 * @return true = <code>ArchiveDataKind.REQUESTED</code> wurde gewählt;
	 */
	public boolean isRequested() {
		return _requested;
	}

	/**
	 * Diese Methode gibt an, ob <code>ArchiveDataKind.REQUESTED_DELAYED</code> im Konstruktor übergeben wurde
	 *
	 * @return true = <code>ArchiveDataKind.REQUESTED_DELAYED</code> wurde gewählt;
	 */
	public boolean isRequestedDelayed() {
		return _requestedDelayed;
	}


	public String toString() {
		String returnMessage = "ArchiveDataKindCombination{";
		if (_online) returnMessage += ArchiveDataKind.ONLINE.toString() + " ";
		if (_onlineDelayed) returnMessage += ArchiveDataKind.ONLINE_DELAYED.toString() + " ";
		if (_requested) returnMessage += ArchiveDataKind.REQUESTED.toString() + " ";
		if (_requestedDelayed) returnMessage += ArchiveDataKind.REQUESTED_DELAYED.toString();
		returnMessage += "}";
		return returnMessage;
	}
}

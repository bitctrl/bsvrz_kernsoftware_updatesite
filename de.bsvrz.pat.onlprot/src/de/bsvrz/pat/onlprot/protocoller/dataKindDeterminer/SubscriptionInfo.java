/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.onlprot.
 * 
 * de.bsvrz.pat.onlprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.onlprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.onlprot.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */


package de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.SenderRole;

import java.util.List;

/**
 * Anmeldeinfos
 *
 * @author Kappich Systemberatung
 * @version $Revision:5031 $
 */
public class SubscriptionInfo {

	/** Untere Grenze des Wertebereichs, in dem die Intervalllänge zufällig variiert
	 * wird.
	 */
	private final long						intervalLowerBound;
	/** Obere Grenze des Wertebereichs, in dem die Intervalllänge zufällig variiert
	 * wird.
	 */
	private final long						intervalUpperBound;
	/** Zähler für die Anzahl der gesendeten Intervalle */
	private int								intervalCount;
	/** Streuungsgrad für das Versenden der Datentelegramme in einem Intervall */
	private final int						spread;
	/** Zeitstempel-Option: Gibt an, ob der in der Datei vorgefundene Zeitstempel
	 * verwendet wird oder die Daten in die Jetztzeit verschoben werden
	 */
	private final int						timeStampOption;
	/** Liste der Objekte, die angemeldet werden sollen */
	private List objects= null;

    /**
	 * Rolle
	 */
	private final String					role;

	/**
	 * Empfangsoption
	 */
	private final ReceiveOptions			options;

	/**
	 * Objektspezifikation
	 */
	private final String					objectSpec;

	/**
	 * Datenspezifikation
	 */
	private final String					dataSpec;


	/** Erzeugt ein neues Objekt der Klasse <code>SubscriptionInfo</code> mit
	 * Senderinformationen
	 * @param intervalCount int mit Anzahl der zu sendenden Intervalle
	 * @param spread int mit Streuung
	 * @param timeStampOption int mit Zeitstempel-Option
	 * @param role String mit der Rolle des Anmeldungsobjekts
	 * @param objectSpec String mit den Objekten des
	 * 								Anmeldungsobjekts
	 * @param dataSpec String mit der Spezifikation der Daten des
	 * 								Anmeldungsobjekts
	 * @param intervalLowerBound Untere Grenze des Wertebereichs, in dem die Intervalllänge variiert wird
	 * @param intervalUpperBound Obere Grenze des Wertebereichs, in dem die Intervalllänge variiert wird
	 */
	SubscriptionInfo(long intervalLowerBound, long intervalUpperBound,
					 int intervalCount, int spread,  int timeStampOption,
					 String role, String objectSpec, String dataSpec) {
		this.intervalLowerBound = intervalLowerBound;
		this.intervalUpperBound = intervalUpperBound;
		this.intervalCount = intervalCount;
		this.spread = spread;
		this.timeStampOption = timeStampOption;
		this.role = role;
		options = null;
		this.objectSpec = objectSpec;
		this.dataSpec = dataSpec;
	}

	/**
	 * Erzeugt ein neues Objekt der Klasse <code>SubscriptionInfo</code> mit
	 * Empfängerinformationen
	 *
	 * @param	role		String mit der Rolle des Anmeldungsobjekts
	 * @param	options		{@link ReceiveOptions} des Anmeldungsobjekts
	 * @param	objectSpec	String mit den Objekten des Anmeldungsobjekts
	 * @param	dataSpec	String mit der Spezifikation der Daten des
	 *						Anmeldungsobjekts
	 */
	SubscriptionInfo(String role, ReceiveOptions options, String objectSpec,
					 String dataSpec) {
		intervalLowerBound = 0;
		intervalUpperBound = 0;
		intervalCount = 0;
		spread = 0;
		timeStampOption = 0;
		this.role = role;
		this.options = options;
		this.objectSpec = objectSpec;
		this.dataSpec = dataSpec;
	}

	/**
	 * Dekrementiert die Anzahl der zu sendenden Zykel
	 */
	public void decIntervalCount() {
		intervalCount = intervalCount - 1;
	}

	/**
	 * Gibt die untere Grenze des Intervallbereichs zurück
	 *
	 * @return	long mit der unteren Grenze des Intervallbereichs
	 */
	public long getIntervalLowerBound() {
		return intervalLowerBound;
	}

	/**
	 * Gibt die Anzahl der zu sendenden Zykel zurück
	 *
	 * @return	int mit der Anzahl der zu sendenden Zyklen
	 */
	public int getIntervalCount() {
		return intervalCount;
	}

	/**
	 * Gibt die obere Grenze des Intervallbereichs zurück
	 *
	 * @return	long mit der oberen Grenze des Intervallbereichs
	 */
	public long getIntervalUpperBound() {
		return intervalUpperBound;
	}

	/**
	 * Gibt die anzumeldenden Objekte zurück
	 *
	 * @return	List mit den Objekten
	 */
	public List getObjects() {
		return objects;
	}

	/**
	 * Gibt die Objektspezifikation zurück
	 *
	 * @return	String	mit der Objektspezifikation
	 */
	public String getObjectSpec() {
		return objectSpec;
	}

	/**
	 * Gibt die Optionen des Anmeldungsobjekts zurück
	 *
	 * @return	{@link ReceiveOptions} des Anmeldungsobjekts
	 */
	public ReceiveOptions getOptions() {
		return options;
	}

	/**
	 * Gibt eine zufällige Intervalllänge im eingestellten Bereich zurück
	 *
	 * @return	long mit einer Intervalllänge, die zwischen
	 *			{@link #intervalLowerBound} und {@link #intervalUpperBound} liegt
	 */
	public long getRandomInterval() {
		long num = (int)(Math.random() * (intervalUpperBound - intervalLowerBound)) + intervalLowerBound;
		return num;
	}

	/**
	 * Gibt die Rolle des Anmeldungsobjekts zurück
	 *
	 * @return	{@link ReceiverRole} des Anmeldungsobjekts
	 */
	public ReceiverRole getReceiverRole() {
		if (role.equals("Senke")) {
			return ReceiverRole.drain();
		} else if (role.equals("Empfänger")) {
			return ReceiverRole.receiver();
		}
		throw new IllegalArgumentException("Empfänger-Rolle existiert nicht");
	}

	/**
	 * Gibt die Rolle des Anmeldungsobjekts zurück
	 *
	 * @return	{@link SenderRole} des Anmeldungsobjekts
	 */
	public SenderRole getSenderRole() {
		if (role.equals("Quelle")) {
			return SenderRole.source();
		} else if (role.equals("Sender")) {
			return SenderRole.sender();
		}
		throw new IllegalArgumentException("Sender-Rolle existiert nicht");
	}

	/**
	 * Gibt die Datenspezifikationen dieses Anmeldungsobjekts in einem Feld
	 * zurück. Diese bestehen aus den drei Teilen Attributgruppe, Aspekt und
	 * Simulationsvariante.
	 *
	 * @param	separator	String: das zu verwendende Trennzeichen. Die
	 *						Zeichenkette wird als Liste durch
	 *						<code>separator</code> getrennter Zeichenketten
	 *						interpretiert.
	 * @param	count		Anzahl der Teilstrings
	 * @return				String[] der getrennten Objekte
	 */
	public String[] getSplittedData(String separator, int count) {
		return dataSpec.split(separator, count);
	}

	/**
	 * Gibt die Objekte dieses Anmeldungsobjekts in einem Feld zurück
	 *
	 * @param	separator	String: das zu verwendende Trennzeichen. Die
	 *						Zeichenkette wird als Liste durch
	 *						<code>separator</code> getrennter Zeichenketten
	 *						interpretiert.
	 * @return				String[] der getrennten Objekte
	 */
	public String[] getSplittedObjects(String separator) {
		return objectSpec.split(separator);
	}

	/**
	 * Gibt die Streuung zurück.
	 *
	 * @return	long, welches die Streuung enthält
	 */
	public int getSpread() {
		return spread;
	}

	/**
	 * Gibt die Zeitstempel-Option des Anmeldungsobjekts zurück
	 *
	 * @return	int mit Zeitstempel-Option des Anmeldungsobjekts
	 */

	public int getTimeStampOption() {
		return timeStampOption;
	}

	/**
	 * Inkrementiert die Anzahl der zu sendenden Zykel
	 */
	public void incIntervalCount() {
		intervalCount = intervalCount + 1;
	}

	/** Trägt Datenbeschreibungen ein
	 * @param dd DataDescription mit den einzutragenden
	 * 					Datenbeschreibungen
	 */
	public void setDataDescription(DataDescription dd) {
    }

	/**
	 * Setzt die Liste der Objekte
	 *
	 * @param	objects	List mit den einzutragenden Objekten
	 */
	public void setObjects(List objects) {
		this.objects = objects;
	}

	/** String-Darstellung eines <code>SubscriptionInfo</code>-Objekts
	 * @return String mit der String-Darstellung des Objekts
	 */
	public String toString() {
		return "SubscriptionInfo(" + intervalLowerBound + ", "
				+ intervalUpperBound + ", " + intervalCount +	", " + spread
				+ ", " + timeStampOption + ", " + role + ", " + options + ", "
				+ objectSpec + ", " + dataSpec + ")";
	}
}

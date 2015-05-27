/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

/**
 * Diese Klasse bildet die Attributgruppe atg.archivEinstellung ab.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Tue, 10 Mar 2009) $ / ($Author: rs $)
 */
public class ArchiveSettings {

	public static final String ATTR_GROUP = "atg.archivEinstellung";

	/** Siehe atg.archivEinstellung::TMax */
	public long maxATimeLeap;

	/** Siehe atg.archivEinstellung::Sicherungsintervall */
	public String sichernIntervall;

	/** Siehe atg.archivEinstellung::L�schintervall */
	public String loeschIntervall;

	/** Siehe atg.archivEinstellung::Nachforderungsintervall */
	public String nachfIntervall;

	/** Siehe atg.archivEinstellung::Kapazit�tscheck */
	public long capaCheckIntervall;

	/** Siehe atg.archivEinstellung::Restkapazit�t */
	public long remainingCapa;

	/** Siehe atg.archivEinstellung::L�schschutzverl�ngerung */
	public long loeschutzVerl;

	/** Siehe atg.archivEinstellung::L�schschutzverl�ngerungMax */
	public long loeschutzVerlMax;

	/** Siehe atg.archivEinstellung::AnzahlBytes */
	public int queryAnzBytes;

	/** Siehe atg.archivEinstellung::AnzahlBlocks */
	public int queryAnzBlocks;

	/** Siehe atg.archivEinstellung::AnzahlHohePriorit�t */
	public int anzHohePrio;

	/** Siehe atg.archivEinstellung::AnzahlMittlerePriorit�t */
	public int anzMittPrio;

	/** Siehe atg.archivEinstellung::AnzahlNiedrigePriorit�t */
	public int anzNiedPrio;

	/** Siehe atg.archivEinstellung::ZeitSpanneNeuerSteuerbefehlNachKeineQuelle */
	public long timeoutSteuerbefehl;

	public ArchiveSettings() {
	}

	/**
	 * Erzeugt ein Objekt aus einem DAV-Datum.
	 *
	 * @param d Datenobjekt
	 */
	public ArchiveSettings(Data d) {
		maxATimeLeap = d.getTimeValue("TMax").getSeconds();
		sichernIntervall = d.getTextValue("Sicherungsintervall").getValueText();
		loeschIntervall = d.getTextValue("L�schintervall").getValueText();
		nachfIntervall = d.getTextValue("Nachforderungsintervall").getValueText();
		capaCheckIntervall = d.getTimeValue("Kapazit�tscheck").getMillis();
		remainingCapa = d.getUnscaledValue("Restkapazit�t").longValue();
		loeschutzVerl = d.getTimeValue("L�schschutzverl�ngerung").getSeconds();
		loeschutzVerlMax = d.getTimeValue("L�schschutzverl�ngerungMax").getSeconds();
		queryAnzBytes = d.getUnscaledValue("AnzahlBytes").intValue();
		queryAnzBlocks = d.getUnscaledValue("AnzahlBlocks").intValue();
		anzHohePrio = d.getUnscaledValue("AnzahlHohePriorit�t").intValue();
		anzMittPrio = d.getUnscaledValue("AnzahlMittlerePriorit�t").intValue();
		anzNiedPrio = d.getUnscaledValue("AnzahlNiedrigePriorit�t").intValue();
		timeoutSteuerbefehl = d.getTimeValue("ZeitSpanneNeuerSteuerbefehlNachKeineQuelle").getMillis();
	}

	/**
	 * Erzeugt aus den ContainerSettings ein Data-Objekt.
	 *
	 * @param davCon Verbindung zum DAV
	 *
	 * @return Datenobjekt
	 *
	 * @throws ConfigurationException
	 */
	public Data createData(ClientDavConnection davCon) throws ConfigurationException {
		Data data = davCon.createData(davCon.getDataModel().getAttributeGroup(ATTR_GROUP));
		data.getItem("TMax").asTimeValue().setSeconds(maxATimeLeap);
		data.getItem("Sicherungsintervall").asTextValue().setText(sichernIntervall);
		data.getItem("L�schintervall").asTextValue().setText(loeschIntervall);
		data.getItem("Nachforderungsintervall").asTextValue().setText(nachfIntervall);
		data.getItem("Kapazit�tscheck").asTimeValue().setMillis(capaCheckIntervall);
		data.getItem("Restkapazit�t").asUnscaledValue().set(remainingCapa);
		data.getItem("L�schschutzverl�ngerung").asTimeValue().setSeconds(loeschutzVerl);
		data.getItem("L�schschutzverl�ngerungMax").asTimeValue().setSeconds(loeschutzVerlMax);
		data.getItem("AnzahlBytes").asUnscaledValue().set(queryAnzBytes);
		data.getItem("AnzahlBlocks").asUnscaledValue().set(queryAnzBlocks);
		data.getItem("AnzahlHohePriorit�t").asUnscaledValue().set(anzHohePrio);
		data.getItem("AnzahlMittlerePriorit�t").asUnscaledValue().set(anzMittPrio);
		data.getItem("AnzahlNiedrigePriorit�t").asUnscaledValue().set(anzNiedPrio);
		data.getItem("ZeitSpanneNeuerSteuerbefehlNachKeineQuelle").asTimeValue().setMillis(timeoutSteuerbefehl);
		return data;
	}
}

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
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Diese Klasse bildet die Attributgruppe atg.archivContainer ab.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Tue, 10 Mar 2009) $ / ($Author: rs $)
 */
public class ContainerSettings {

	public static final String ATTR_GROUP = "atg.archivContainer";

	/** Siehe atg.archivContainer::ContainerAbschlu�Parameter.Standardeinstellung */
	public CloseCondition stdCloseConditions;

	/** Siehe atg.archivContainer::ContainerAbschlu�Parameter.Ausnahmen */
	public List<ContSettingsExc> exceptions = new ArrayList<ContSettingsExc>();

	public ContainerSettings() {
		stdCloseConditions = new CloseCondition();
	}

	public ContainerSettings(Data d) {
		Data capParams = d.getItem("ContainerAbschlu�Parameter");
		stdCloseConditions = new CloseCondition(capParams.getItem("Standardeinstellung").getItem("Einstellungen"));

		Data.Array ausnahmen = capParams.getItem("Ausnahmen").asArray();
		for(int i = 0; i < ausnahmen.getLength(); i++) {
			Data.Array atgs = ausnahmen.getItem(i).getItem("Attributgruppen").asArray();
			ContSettingsExc ex = new ContSettingsExc();
			for(int j = 0; j < atgs.getLength(); j++) {
				ex.addAtg((AttributeGroup)atgs.getItem(j).getReferenceValue("Attributgruppe").getSystemObject());
			}
			ex.excCloseConditions = new CloseCondition(ausnahmen.getItem(i).getItem("Einstellungen"));
			exceptions.add(ex);
		}
	}

	/**
	 * Durchlaeuft die Parameter und setzt die Werte fuer maximale Datensatzzahl, Groesse und Zeitspanne auf die angegebenen Minimumwerte falls notwendig.
	 *
	 * @param minMaxDS   Minimum der maximalen Datensatzzahl pro Container
	 * @param minMaxSize Minimum der maximalen Containergroesse
	 * @param minMaxTime Minimum der maximalen Zeitspanne pro Container
	 *
	 * @return Wahr falls etwas veraendert wurde, falsch sonst
	 */
	public boolean ensureMinimums(int minMaxDS, int minMaxSize, long minMaxTime) {
		boolean corrected = false;
		if(stdCloseConditions.maxContAnzDS < minMaxDS) {
			corrected = true;
			stdCloseConditions.maxContAnzDS = minMaxDS;
		}
		if(stdCloseConditions.maxContSize < minMaxSize) {
			corrected = true;
			stdCloseConditions.maxContSize = minMaxSize;
		}
		if(stdCloseConditions.maxContTime < minMaxTime) {
			corrected = true;
			stdCloseConditions.maxContTime = minMaxTime;
		}
		for(ContSettingsExc exc : exceptions) {
			if(exc.excCloseConditions.maxContAnzDS < minMaxDS) {
				corrected = true;
				exc.excCloseConditions.maxContAnzDS = minMaxDS;
			}
			if(exc.excCloseConditions.maxContSize < minMaxSize) {
				corrected = true;
				exc.excCloseConditions.maxContSize = minMaxSize;
			}
			if(exc.excCloseConditions.maxContTime < minMaxTime) {
				corrected = true;
				exc.excCloseConditions.maxContTime = minMaxTime;
			}
		}
		return corrected;
	}

	/**
	 * Liefert die Ausnahmeeinstellungen falls vorhanden (siehe atg.archivContainer::ContainerAbschlu�Parameter.Ausnahmen). Die Liste der Ausnahmen wird von hinten
	 * durchlaufen, damit stets die letzte Einstellung gueltig ist.
	 *
	 * @param dd Datenidentifikation
	 *
	 * @return Einstellungen fuer die gegebene Attributgruppe oder <code>null</code> falls keine Ausnahmeeinstellungen dafuer vorliegen.
	 */
	public CloseCondition getExceptionSettings(AttributeGroup atg) {
		for(int i = exceptions.size() - 1; i >= 0; i--) {
			if(exceptions.get(i).containsAtg(atg)) return exceptions.get(i).excCloseConditions;
		}
		return null;
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
		Data capParams = data.getItem("ContainerAbschlu�Parameter");
		Data einst = capParams.getItem("Standardeinstellung").getItem("Einstellungen");
		einst.getItem("MaxAnzahlArchivdatens�tze").asUnscaledValue().set(stdCloseConditions.maxContAnzDS);
		einst.getItem("MaxContainergr��e").asUnscaledValue().set(stdCloseConditions.maxContSize);
		einst.getItem("MaxZeitspanneContainer").asTimeValue().setSeconds(stdCloseConditions.maxContTime);

		Data.Array ausnahmen = capParams.getItem("Ausnahmen").asArray();
		ausnahmen.setLength(exceptions.size());
		for(int i = 0; i < exceptions.size(); i++) {
			Data.Array atgs = ausnahmen.getItem(i).getItem("Attributgruppen").asArray();
			atgs.setLength(exceptions.get(i).atgSet.size());
			int j = 0;
			for(Iterator iter = exceptions.get(i).atgSet.iterator(); iter.hasNext();) {
				SystemObject excAtg = davCon.getDataModel().getObject((Long)iter.next());
				atgs.getItem(j++).getItem("Attributgruppe").asReferenceValue().setSystemObject(excAtg);
			}
			einst = ausnahmen.getItem(i).getItem("Einstellungen");
			einst.getItem("MaxAnzahlArchivdatens�tze").asUnscaledValue().set(exceptions.get(i).excCloseConditions.maxContAnzDS);
			einst.getItem("MaxContainergr��e").asUnscaledValue().set(exceptions.get(i).excCloseConditions.maxContSize);
			einst.getItem("MaxZeitspanneContainer").asTimeValue().setSeconds(exceptions.get(i).excCloseConditions.maxContTime);
		}
		return data;
	}

	/** Abschlusskriterien. Siehe atg.archivContainer */
	public static class CloseCondition {

		/** Siehe atg.archivContainer::MaxAnzahlArchivdatens�tze */
		public int maxContAnzDS;

		/** Siehe atg.archivContainer::MaxContainergr��e (in Byte) */
		public int maxContSize;

		/** Siehe atg.archivContainer::MaxZeitspanneContainer (in Sekunden) */
		public long maxContTime;

		public CloseCondition() {
		}

		private CloseCondition(Data d) {
			maxContAnzDS = d.getUnscaledValue("MaxAnzahlArchivdatens�tze").intValue();
			maxContSize = d.getUnscaledValue("MaxContainergr��e").intValue();
			maxContTime = d.getTimeValue("MaxZeitspanneContainer").getSeconds();
		}
	}

	/** Abschlusskriterien fuer eine Liste von Ausnahmen. Siehe atg.archivContainer */
	public static class ContSettingsExc {

		public HashSet<Long> atgSet = new HashSet<Long>();

		public CloseCondition excCloseConditions;

		public void addAtg(AttributeGroup atg) {
			atgSet.add(atg.getId());
		}

		public boolean containsAtg(AttributeGroup atg) {
			return atgSet.contains(atg.getId());
		}
	}
}

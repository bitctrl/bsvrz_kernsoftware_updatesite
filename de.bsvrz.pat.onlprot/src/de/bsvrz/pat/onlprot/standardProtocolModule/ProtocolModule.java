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

package de.bsvrz.pat.onlprot.standardProtocolModule;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

import java.io.PrintWriter;

/**
 * Abstrakte Klasse, die Funktionalität für Protokollierungsmodule zur Verfügung stellt.
 * <p>
 * Folgende Methoden werden zur Vergügung gestellt: <ul> <li> <code>initProtocol</code>	- Initialisierung <li> <code>update</code>			- Ausgabe der empfangenen
 * Daten <li> <code>closeProtocol</code>	- Abschluß der Protokollierung </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class ProtocolModule implements ProtocolModuleInterface {

	/** Name der Datei, in der die protokollierten Datensequenzen gespeichert werden. */
	private PrintWriter protocolFileWriter = null;

	/** Der Protokollierer, der das gewünschte Protokoll erstellt. */
	private ClientProtocollerInterface protocoller = null;


	/** Einziger Konstruktor. (Für den üblicherweise impliziten Aufruf durch abgeleitete Klassen.) */
	protected ProtocolModule() {
	}

	/** Führt Aufräumarbeiten nach Beendigung des Protokollierens aus. */
	public abstract void closeProtocol();

	/**
	 * Gibt Information über die Aufrufparameter des Protokollierungsmoduls zurück
	 *
	 * @return String mit der Beschreibung der erlaubten Aufrufparameter und deren erwartetes Format
	 */
	public abstract String getHelp();

	/**
	 * Zugriff auf den <code>protocolFileWriter</code>.
	 *
	 * @return {@link PrintWriter} mit Namen der Protokolldatei
	 */
	public PrintWriter getProtocolFileWriter() {
		return protocolFileWriter;
	}

	/**
	 * Zugriff auf den <code>protocoller</code>.
	 *
	 * @return den registrierten Protokollierer.
	 */
	public ClientProtocollerInterface getProtocoller() {
		return protocoller;
	}

	/**
	 * Führt die Initialisierungsschritte des Protokollierungsmoduls aus.
	 *
	 * @return ClientReceiverInterface-Handle auf den benutzten Protokollierer
	 *
	 * @param	argumentList	{@link ArgumentList} der noch nicht ausgewerteten Aufrufparameter der Applikation
	 * @param	protocolFile	PrintWriter der protokollierten	Datensequenzen
	 * @param	args			String[] mit den Aufrufparametern der Applikation
	 */
	public ClientReceiverInterface initProtocol(ArgumentList argumentList, PrintWriter protocolFile, String[] args) {
		return protocoller;
	}

	/**
	 * Protokolldatei setzen.
	 *
	 * @param	pfw	{@link PrintWriter} mit Beschreibung der Protokolldatei
	 */
	public void setProtocolFileWriter(PrintWriter pfw) {
		protocolFileWriter = pfw;
	}

	/**
	 * Protokollierer setzen.
	 *
	 * @param	cpi	Ausgewählter Protokollierer
	 */
	public void setProtocoller(ClientProtocollerInterface cpi) {
		protocoller = cpi;
	}

	/**
	 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird.
	 *
	 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen.
	 * @see				de.bsvrz.dav.daf.main.ClientReceiverInterface#update
	 */
	public abstract void update(ResultData[] results);
}

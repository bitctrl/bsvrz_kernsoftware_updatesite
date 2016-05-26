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
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

import java.io.PrintWriter;

/**
 * Festlegung der Funktionalität von Protokollierungsmodulen
 * <p>
 * Ein Protokollierungsmodul muß folgende Methoden zur Vergügung stellen <ul> <li> <code>initProtocol</code>	- Initialisierung <li> <code>closeProtocol</code>	-
 * Abschluß der Protokollierung </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ProtocolModuleInterface {

	/**
	 * Gibt Information über die Aufrufparameter des Protokollierungsmoduls zurück
	 *
	 * @return String mit der Beschreibung der erlaubten Aufrufparameter und deren erwartetes Format
	 */
	public abstract String getHelp();

	/**
	 * Führt die Initialisierungsschritte des Protokollierungsmoduls aus.
	 *
	 * @return ClientReceiverInterface-Handle auf den benutzten Protokollierer
	 *
	 * @param	argumentList	{@link de.bsvrz.sys.funclib.commandLineArgs.ArgumentList} der noch nicht ausgewerteten Aufrufparameter der Applikation
	 * @param	protocolFile	PrintWriter der protokollierten	Datensequenzen
	 * @param	args			String[] mit den Aufrufparametern der Applikation
	 */
	public ClientReceiverInterface initProtocol(ArgumentList argumentList, PrintWriter protocolFile, String[] args);

	/** Führt Aufräumarbeiten nach Beendigung des Protokollierens aus. */
	public abstract void closeProtocol();
}

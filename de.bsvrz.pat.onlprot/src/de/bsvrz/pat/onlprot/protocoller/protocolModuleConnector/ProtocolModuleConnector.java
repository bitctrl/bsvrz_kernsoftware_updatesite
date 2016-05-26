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

package de.bsvrz.pat.onlprot.protocoller.protocolModuleConnector;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.pat.onlprot.standardProtocolModule.ProtocolModule;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Modul zur Verwaltung beliebiger Protokollierungsmodule, die die Schnittstelle
 * {@link de.bsvrz.pat.onlprot.standardProtocolModule.ClientProtocollerInterface} erfüllen.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5031 $
 */
public class ProtocolModuleConnector {
	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/**
	 * Der Protokollierer, der für die Ausgabe der Telegramme zuständig ist
	 */
	private final ClientReceiverInterface	protocoller;

	/**
	 * Testobjekt zum Testen auf Beschreibbarkeit einer existierenden Datei
	 */
	private File							protocolFile = null;

	/**
	 * Ausgabe-<i>Stream</i> für die Protokolle
	 */
	private PrintWriter						protocolFileWriter = null;

	/**
	 * Das verwendete Protokollierungsmodul
	 */
	private ProtocolModule					protocolModule = null;

	/**
	 * Name des verwendeten Protokollierungsmoduls
	 */
	private String							protocolModuleName = "";


	/** Erzeugt ein neues Objekt der Klasse <code>ProtocolModuleConnector</code>
	 * @param argumentList {@link de.bsvrz.sys.funclib.commandLineArgs.ArgumentList} der noch nicht
	 * 									ausgewerteten Aufrufparameter der
	 * 									Applikation
	 * @param args String[] mit den Aufrufparametern der
	 * 									Applikation
	 * @throws ClassNotFoundException wenn die Klasse des angegebenen
	 * 									Protokollierungsmoduls nicht gefunden
	 * 									wird
	 * @throws IllegalAccessException wenn {@link de.bsvrz.sys.funclib.commandLineArgs.ArgumentList#fetchArgument}
	 * 									keinen Wert für das gewünschte Argument
	 * 									ermitteln konnte
	 * @throws InstantiationException in {@link #setProtocolModule}
	 * @throws IOException wenn bei der Initialisierung E/A-Probleme auftreten.
	 */
	public ProtocolModuleConnector(ArgumentList argumentList, String[] args)
	throws ClassNotFoundException, IllegalAccessException,
		   InstantiationException, IOException {
		setProtocolModule(argumentList
				.fetchArgument("-protModul=de.bsvrz.pat.onlprot.standardProtocolModule.StandardProtocoller")
				.asString());
		try {

			/*
			 * Zwischenspeicher für den Dateinamen, für den Aufruf von
			 * <code>asWritableFile</code>
			 */
			ArgumentList.Argument dummy = argumentList.fetchArgument("-datei=");
			if (dummy.hasValue() && (dummy.getValue().length() > 0)) {
				protocolFile = dummy.asWritableFile(true);
                protocolFileWriter = new PrintWriter(new BufferedWriter(new
                        OutputStreamWriter(new FileOutputStream(
                                protocolFile.getPath()),
                                Charset.forName("ISO-8859-1"))), true);
			} else {
				protocolFileWriter = new PrintWriter(System.out, true);
			}
		} catch (IOException e) {
			debug.error("Fehler beim Anlegen oder Öffnen der Protokolldatei");
			throw e;
		}
		protocoller
		= protocolModule.initProtocol(argumentList, protocolFileWriter, args);

		Runtime.getRuntime().addShutdownHook(new Thread(
				new Runnable() {
					public void run() {
						try {
							debug.info("Programm wird beendet... ");
							cleanUp();
						} catch(Exception e) {
							debug.error("Fehler beim Beenden des Programms: "
									 + e);
						}
					}
				}));

	}

	/**
	 * Aufräumen nach Beenden des Protokollierens
	 */
	public void cleanUp() {
		protocolModule.closeProtocol();
	}

	/**
	 * Gibt Information über die erlaubten Aufrufparameter des verwendeten
	 * Protokollierungsmoduls zurück
	 *
	 * @return	String mit der Beschreibung der erlaubten Aufrufparameter und
	 *			deren erwartetes Format
	 */
	public String getHelp() {
		return protocolModule.getHelp();
	}

	/**
	 * Verwendeten Protokollierer ausgeben
	 *
	 * @return	{@link ClientReceiverInterface} mit dem verwendeten
	 *			Protokollierer
	 */
	public ClientReceiverInterface getProtocoller() {
		return protocoller;
	}

	/** Trägt das verwendete Protokollierungsmodul ein. Dazu wird überprüft, ob
	 * eine Klasse mit dem übergebenen Namen existiert.
	 * @param protocolModuleName String mit dem Namen des
	 * 										Protokollierungsmoduls
	 * @throws ClassNotFoundException wenn unter dem angegebenen Namen
	 * 										kein Protokollierungsmodul zu finden
	 * 										ist
	 * @throws IllegalAccessException wenn eine leere Zeichenkette
	 * 										übergeben wurde
	 * @throws InstantiationException wenn Fehler in
	 * 										{@link Class#newInstance} auftritt
	 */
	private final void setProtocolModule(String protocolModuleName)
	throws ClassNotFoundException, IllegalAccessException,
		   InstantiationException {
		if (protocolModuleName != null) {
			this.protocolModuleName = protocolModuleName;
		} else {
			debug.error("Kein Protokollierungsmodul angegeben");
			System.exit(1);
		}
		try {
			Class protocolModuleClass = Class.forName(protocolModuleName);
			if (protocolModuleClass == null) {
				debug.error("Unbekanntes Protokollierungsmodul");
				System.exit(1);
			}
			protocolModule = (ProtocolModule) protocolModuleClass.newInstance();
		} catch (ClassNotFoundException e) {
			debug.error("Protokollierungsmodul nicht gefunden: " + e.getMessage());
			throw new ClassNotFoundException("-protModul");
		}
	}

    /** Zugriffsmethode auf den Protokollierungsmodulnamen
     * @return  Namen des verwendeten Protokollierungsmoduls
     */
    public String getProtocolModuleName() {
        return protocolModuleName;
    }
}

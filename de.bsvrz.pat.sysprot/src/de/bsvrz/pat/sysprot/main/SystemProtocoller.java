/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
 * Copyright 2006 by beck et al. projects GmbH
 * 
 * This file is part of de.bsvrz.pat.sysprot.
 * 
 * de.bsvrz.pat.sysprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysprot.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.pat.sysprot.main;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.archive.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.onlprot.protocoller.protocolModuleConnector.ProtocolModuleConnector;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.configObjectAcquisition.ConfigurationHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementiert den SystemProtokollierer um Anfragen an das Archivsystem zu stellen.
 *
 * Aufruf: java de.bsvrz.pat.sysprot.main.SystemProtocoller
 *      -datenverteiler=localhost:8083
 *      -benutzer=Tester
 *      -authentifizierung=D:\Projects\VRZ3\Kernsoftware\skripte-dosshell\passwd
 *      &lt;PROTOKOLLIERUNGSMODUL&gt;
 *      -von="01.12.2005 00:00:00"
 *      -bis="31.12.2006 23:59:59"
 *      -objekte=235479
 *      -daten=atg.verkehrGeschwindigkeit:asp.fuzzy:0
 *
 * Andere ProtokollierungsModule:
 *   SQL Protokollierer -protModul=vrz3.export.prot.DatabaseProtocoller
 *   XML Protokollierer -protModul=de.bsvrz.pat.onlprot.standardProtocolModule.StandardProtocoller -ausgabe=xml
 *   Daten Protokollierer -protModul=de.bsvrz.pat.onlprot.standardProtocolModule.StandardProtocoller -ausgabe=daten
 *   Kopf Protokollierer -protModul=de.bsvrz.pat.onlprot.standardProtocolModule.StandardProtocoller -ausgabe=kopf
 *   Protokollierer für zeilenweise Ausgabe -protModul=vrz3.export.prot.FlatProtocoller
 */
public class SystemProtocoller implements StandardApplication {
    /**
     * Wie lange maximal auf das Archiv gewartet werden soll
     */
    public final int ARC_MAX_WAIT = 5;

    private String[] originalArguments;
    private ClientDavInterface connection;
    private ProtocolModuleConnector pmc;
    private ArchiveRequestManager archive;

    private String objects = null;
    private String data    = null;
    private long from      = 0;
    private long until     = 0;


    /**
     * Startet die Applikation
     *
     * @param args Die Übergebenen Argumente als String-Array
     */
    public static void main(String[] args) {
        StandardApplicationRunner.run(new SystemProtocoller(args), args);
        System.exit(0); // Sonst bleiben Threads übrig...
    }


    /**
     * Konstruktor
     *
     * @param originalArguments die unveränderten Aufrufargumente
     */
    public SystemProtocoller(String[] originalArguments) {
        this.originalArguments = new String[originalArguments.length];
        System.arraycopy(originalArguments, 0, this.originalArguments, 0, originalArguments.length);
    }


    /**
     * Verarbeitet die Kommandozeilenargumente
     *
     * @param argumentList die Argumente
     * @throws Exception bei Fehlenden Argumenten ohne Defaultwert
     */
    public void parseArguments(ArgumentList argumentList) throws Exception {
        // Allgemeine Argumente
        pmc = new ProtocolModuleConnector(argumentList, originalArguments);

        // Die eigentliche Anfrage
        from    = argumentList.fetchArgument("-von").asTime();
        until   = argumentList.fetchArgument("-bis").asTime();
        objects = argumentList.fetchArgument("-objekte").asString();
        data    = argumentList.fetchArgument("-daten").asString();
    }



    /**
     *
     * @param connection DAV-Verbindung
     * @throws Exception wenn keine Verbindung aufgebaut werden kann.
     */
    public void initialize(ClientDavInterface connection) throws Exception {
        this.connection = connection;
        this.archive    = connection.getArchive();
        fetchData();
    }


    /**
     *
     */
    private void fetchData() {
        // Vorarbeit
        ClientReceiverInterface protocoller = pmc.getProtocoller();
        ArchiveQueryPriority archiveQueryPriority = ArchiveQueryPriority.MEDIUM;
        ArchiveTimeSpecification archiveTimeSpecification = new ArchiveTimeSpecification(TimingType.DATA_TIME, false, from, until);
        ArchiveDataKindCombination archiveDataKindCombination = new ArchiveDataKindCombination(ArchiveDataKind.ONLINE, ArchiveDataKind.ONLINE_DELAYED,
                ArchiveDataKind.REQUESTED, ArchiveDataKind.REQUESTED_DELAYED);
        ArchiveOrder archiveOrder = ArchiveOrder.BY_DATA_TIME;
        ArchiveRequestOption archiveRequestOption = ArchiveRequestOption.NORMAL;
        ArchiveDataStream[] archiveDataStream = null;

        // Prüfen, ob Archiv da ist
        try {
            for (int i = 1; i <= ARC_MAX_WAIT; i++) {
                if (!archive.isArchiveAvailable()) {
                    System.out.println("Warte auf Archiv... (" + i + " von " + ARC_MAX_WAIT + " Versuchen)");
                    synchronized (this) {
                        this.wait(1000);
                    }
                    if (i == ARC_MAX_WAIT) {
                        System.out.println("Konnte keine Verbindung zum Archivsystem aufbauen. Ende.");
                        return; // Kein Archiv -> Ende
                    }
                } else {
                    break; // Archiv ist da
                }
            }

            // Anfrage zusammenbauen
            Collection<SystemObject> sysObjects = ConfigurationHelper.getObjects(objects, connection.getDataModel());
            String[] dataSpecs = data.split(":", 3);
            AttributeGroup attributeGroup = connection.getDataModel().getAttributeGroup(dataSpecs[0]);
            if (attributeGroup==null)
                System.out.println("ATG gibts net...");
            Aspect aspect = connection.getDataModel().getAspect(dataSpecs[1]);
            if (aspect==null)
                System.out.println("ASP gibts net");
	        short simVariant = 0;
	        if(dataSpecs.length > 2) {
		        try {
			        simVariant = Short.parseShort(dataSpecs[2]);
		        }
		        catch(NumberFormatException e) {
			        System.out.println(e);
			        System.out.println("Simulationsvariante ist nicht interpretierbar: " + dataSpecs[2]);
		        }
	        }

            List<ArchiveDataSpecification> archiveDataSpecifications = new LinkedList<ArchiveDataSpecification>();
            DataDescription dataDescription = new DataDescription(attributeGroup, aspect, simVariant);
            for (SystemObject so : sysObjects) {
	            ArchiveDataSpecification ads = new ArchiveDataSpecification(
			            archiveTimeSpecification, archiveDataKindCombination, archiveOrder,
			            archiveRequestOption, dataDescription, so
	            );
	            try{
		            ads.setQueryWithPid();
	            }
	            catch(NoSuchMethodError e){
		            System.err.println("Archivanfrage kann historische Objekte nicht berücksichtigen, bitte DAF-Bibliothek aktualisieren.");
	            }
	            archiveDataSpecifications.add(ads);
            }
            ArchiveDataQueryResult queryResult = archive.request(archiveQueryPriority, archiveDataSpecifications);

            // Anfrage verarbeiten
            DateFormat outputFormat= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("Zeitspanne:     " + outputFormat.format(new Date(from)) + " - " + outputFormat.format(new Date(until)));
            System.out.println("Attributgruppe: " + attributeGroup);
            System.out.println("Aspekt:         " + aspect);
            System.out.println("SimVar:         " + simVariant);
            System.out.print("Objekte:        ");
            for (SystemObject so : sysObjects) {
                System.out.print(so.getName() + ", ");
            }
            System.out.println("\n------------------------------------------------------------------------------");
            if (queryResult.isRequestSuccessful()) {
                archiveDataStream = queryResult.getStreams();

                for (ArchiveDataStream stream : archiveDataStream) {
                    ArchiveData ad = stream.take();
                    while (ad != null) {
                        if (ad.getDataType() != DataState.END_OF_ARCHIVE) {
                            ResultData rd[] = new ResultData[1];
                            rd[0] = new ResultData(ad.getObject(), ad.getDataDescription(), ad.getDataKind().isDelayed(), ad.getDataIndex(), ad.getDataTime(),
                                                   ((byte)(ad.getDataType().getCode() - 1)), ad.getData() );
	                        protocoller.update(rd);
                        }
                        ad = stream.take();
                    }
                }
            } else {
                System.out.println("Eine Archivanfrage konnte nicht bearbeitet werden, Fehler: " + queryResult.getErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

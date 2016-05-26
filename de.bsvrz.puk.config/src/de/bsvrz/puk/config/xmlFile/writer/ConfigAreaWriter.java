/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.xmlFile.writer;

import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaDependency;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaUnversionedChange;
import de.bsvrz.puk.config.xmlFile.properties.*;

import java.io.*;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Diese Klasse schreibt einen Konfigurationsbereich in eine XML-Datei, dabei wird die K2S.DTD berücksichtigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigAreaWriter {

	/** Enthält den gesamten Konfigurationsbereich */
	final ConfigurationAreaProperties _area;

	/** Einrückungszeichen, wird pro Ebene einen _emptyString nach rechts eingerückt. Ein Objekt auf Ebene 2 würde also 2 "_emptyString" eingerückt. */
	final String _emptyString = "\t";

	public ConfigAreaWriter(ConfigurationAreaProperties area) {
		_area = area;
	}

	/**
	 * Schreibt die im Konstruktor übergebenen Objekte als XML-Datei, als Grundlage dient die K2S.DTD.
	 *
	 * @param file Datei, in der die Objekte gespeichert werden sollen. Ist die Datei nicht vorhanden, so wird sie angelegt. Ist sie vorhanden, wird sie gelöscht
	 *             und neu erzeugt.
	 *
	 * @throws IOException Falls es einen Fehler beim erstellen der Versorgungsdatei gab.
	 */
	public void writeConfigAreaAsXML(final File file) throws IOException {
		if(file.exists()) {
			if(!file.delete()) {
				throw new IllegalArgumentException("Die Datei " + file + " konnte nicht gelöscht werden.");
			}
		}

		if(!file.createNewFile()) {
			throw new IllegalArgumentException("Die Datei " + file + " konnte nicht angelegt werden.");
		}

		// Die Datei steht zur Verfügung
		OutputStream fileOutput = new FileOutputStream(file);
		try {
			writeConfigAreaAsXML(fileOutput);
		}
		finally {
			fileOutput.close();
		}
	}

	/**
	 * Schreibt die im Konstruktor übergebenen Objekte als XML in einen OutputStream, als Grundlage dient die K2S.DTD.
	 *
	 * @param outputStream OutputStream, in der die Objekte gespeichert werden sollen.
	 *
	 * @throws IOException Falls es einen Fehler beim erstellen der Versorgungsdatei gab.
	 */
	public void writeConfigAreaAsXML(final OutputStream outputStream) throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, Charset.forName("ISO-8859-1")));
		try {

			pw.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			pw.println("<!DOCTYPE konfigurationsBereich PUBLIC \"-//K2S//DTD Dokument//DE\" \"K2S.dtd\">");

			pw.print("<konfigurationsBereich");
			writeAttributeAndValueCheckedValue(" pid", _area.getPid(), pw);
			writeAttributeAndValueCheckedValue(" name", _area.getName(), pw);
			writeAttributeAndValueCheckedValue(" verantwortlich", _area.getAuthority(), pw);

			pw.println(">");

			writeInfo(_area.getInfo(), pw, 1);

			// konfigurationsAenderung schreiben (und zwar alle)
			writeConfigAreaChanges(_area.getConfigurationAreaChangeInformation(), pw, 1);

			// Schreibt alle Abhängigkeiten als Kommentar in die Datei
			writeDependencies(_area.getAreaDependencies(), pw, 1);

			// Schreibt alle Unversionierten Änderungen als Kommentar in die Datei
			writeUnversionedChanges(_area.getUnversionedChanges(), pw, 1);

			// Anzahl Tabs, bis das modell-Tag beginnt
			final int numberOfTabs = 1;

			final String modelEmptyString = createEmptyString(numberOfTabs);

			// Liste der Objekte nach deren Pid sortieren
			final List<SystemObjectProperties> objectProperties = new ArrayList<SystemObjectProperties>(_area.getObjectProperties());
			Collections.sort(
					objectProperties, new Comparator<SystemObjectProperties>() {
				public int compare(final SystemObjectProperties o1, final SystemObjectProperties o2) {
					Collator deCollator = Collator.getInstance(Locale.GERMAN);
					return deCollator.compare(o1.getPid(), o2.getPid());
				}
			}
			);

			// Anzahl Tabs um eins erhöhen
			final int nextNumberOfTabs = numberOfTabs + 1;

			pw.print(modelEmptyString);
			pw.println("<modell>");

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof SystemObjectTypeProperties) {
					writeTypeDefinition((SystemObjectTypeProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof AttributeGroupProperties && !(property instanceof TransactionProperties)) {
					writeAttributeGroupDefinition((AttributeGroupProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof TransactionProperties) {
					writeTransactionDefinition((TransactionProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof AspectProperties) {
					writeAspectDefinition((AspectProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof AttributeTypeProperties) {
					writeAttributeDefinition((AttributeTypeProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof AttributeListProperties) {
					writeAttributeListDefinition((AttributeListProperties)property, pw, nextNumberOfTabs);
				}
			}

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof ObjectSetTypeProperties) {
					writeSetDefinition((ObjectSetTypeProperties)property, pw, nextNumberOfTabs);
				}
			}

			pw.print(modelEmptyString);
			pw.println("</modell>");

			pw.print(modelEmptyString);
			pw.println("<objekte>");

			for(SystemObjectProperties property : objectProperties) {
				if(property instanceof ConfigurationConfigurationObject) {
					writeConfigurationObject((ConfigurationConfigurationObject)property, pw, nextNumberOfTabs);
				}
			}

			pw.print(modelEmptyString);
			pw.println("</objekte>");

			pw.println("</konfigurationsBereich>");
		}
		finally {
			// Steams schliessen
			pw.close();
		}
	}

	/**
	 * Schreibt alle Abhängigkeiten des Bereichs zu anderen Bereichen als Kommentar in den übergebenen Writer
	 *
	 * @param areaDependencies Abhängigkeiten. Wird <code>null</code> übergeben, wird dies als "die Abhängigkeiten wurden noch nicht geprüft" interpretiert und nichts gemacht.
	 * @param writer           Writer, in den die Abhängigkeiten geschrieben werden
	 * @param textDepth        Einrückungstiefe
	 */
	private void writeDependencies(Collection<ConfigurationAreaDependency> areaDependencies, final PrintWriter writer, final int textDepth) {

		if(areaDependencies != null) {
			final String initialBlanks = createEmptyString(textDepth);

			{
				// Die Abhängigkeiten sollen, wie im Datensatz gespeichert, ausgegeben werden

				writer.print(initialBlanks);

				// Kommentartag (öffnen)
				final String commentStringOpen = "<!--";
				// Kommentartag (schliessen)
				final String commentStringClose = "-->";

				if(areaDependencies.isEmpty() == true) {
					writer.println(commentStringOpen + "Der Bereich " + _area.getPid() + " benötigt keine anderen Bereiche." + commentStringClose);
				}
				else {

					// Kommentar in XML öffnen
					writer.println(commentStringOpen);
					writer.println(initialBlanks + "Abhängigkeiten des Bereichs " + _area.getPid());
					// Tabellenüberschrift erzeugen
					writeHeaderForDependencies(writer, initialBlanks, "      ");

					for(ConfigurationAreaDependency areaDependency : areaDependencies) {
						writer.print(initialBlanks);
						writer.printf(
								"%-24s %-25s %-36s %-35s",
								areaDependency.getDependencyOccurredAtVersion(),
								areaDependency.getKind().getValue(),
								areaDependency.getNeededVersion(),
								areaDependency.getDependantArea()
						);
						// Zeilenumbruch
						writer.println();
					}
					// Kommentar wieder schließen
					writer.println(initialBlanks + commentStringClose);
				}
			}

//			{
//				// Die Abhängigkeiten sortieren
//
//				// In dieser Map liegen alle Abhängigkeiten sortiert vor.
//				final Map<String, List<ConfigurationAreaDependency>> sortedDependencies = new HashMap<String, List<ConfigurationAreaDependency>>();
//
//				for(ConfigurationAreaDependency areaDependency : areaDependencies) {
//					List<ConfigurationAreaDependency> dependencyList = sortedDependencies.get(areaDependency.getDependantArea());
//					if(dependencyList == null) {
//						dependencyList = new ArrayList<ConfigurationAreaDependency>();
//						sortedDependencies.put(areaDependency.getDependantArea(), dependencyList);
//					}
//					dependencyList.add(areaDependency);
//				}
//
//				// Alle Listen sortieren
//				Collection<List<ConfigurationAreaDependency>> unsortedDependencyLists = sortedDependencies.values();
//
//				for(List<ConfigurationAreaDependency> unsortedDependencies : unsortedDependencyLists) {
//					// Die Liste sortieren
//					Collections.sort(
//							unsortedDependencies, new Comparator<ConfigurationAreaDependency>() {
//						public int compare(final ConfigurationAreaDependency o1, final ConfigurationAreaDependency o2) {
//							final Short shortO1 = new Short(o1.getDependencyOccurredAtVersion());
//							final Short shortO2 = new Short(o2.getDependencyOccurredAtVersion());
//							return shortO1.compareTo(shortO2);
//						}
//					}
//					);
//				}//for, über alle Listen, die sortiert werden müssen
//
//				// Bereiche, von dem der Bereich abhängig ist. Die Bereiche werden über die Pid identifiziert.
//				final Set<String> areasPid = sortedDependencies.keySet();
//
//
//				writer.print(initialBlanks);
//
//				// Kommentartag (öffnen)
//				final String commentStringOpen = "<!--";
//				// Kommentartag (schliessen)
//				final String commentStringClose = "-->";
//
//				if(areasPid.isEmpty() == false) {
//
//					// Kommentar in XML öffnen
//					writer.println(commentStringOpen);
//
//					writer.println(initialBlanks + "Abhängigkeiten des Bereichs " + _area.getPid());
//					// Tabellenüberschrift erzeugen
//					writeHeaderForDependencies(writer, initialBlanks, "      ");
//
//					for(String areaPid : areasPid) {
//						final List<ConfigurationAreaDependency> dependencies = sortedDependencies.get(areaPid);
//						// Alle Abhängigkeiten von diesem einen Bereich. Die letzte Abhängigkeite, die gefunden wurde, ist am Ende der Liste gespeichert.
//						// So wurde die Liste eben sortiert.
//
//						// Die als letztes gefundene Abhängigkeit wird als erstes ausgegeben.
//						for(int nr = dependencies.size() - 1; nr >= 0; nr--) {
//
//							final ConfigurationAreaDependency dependency = dependencies.get(nr);
//							writer.print(initialBlanks);
//							writer.printf(
//									"%-24s %-25s %-36s %-35s",
//									dependency.getDependencyOccurredAtVersion(),
//									dependency.getKind().getValue(),
//									dependency.getNeededVersion(),
//									dependency.getDependantArea()
//							);
//							// Zeilenumbruch
//							writer.println();
//						}
//					}
//
//					// Kommentar wieder schließen
//					writer.println(initialBlanks + commentStringClose);
//				}
//				else {
//					writer.println(commentStringOpen + "Der Bereich " + _area.getPid() + " benötigt keine anderen Bereiche." + commentStringClose);
//				}
//			}
		}
	}

	/**
	 * Schreibt die Tabellenüberschrift für die Abhängigkeiten eines Bereichs zu anderen Bereichen
	 *
	 * @param writer                   Objekt, zum schreiben der Daten
	 * @param initialBlanks            Einrückungstiefe der Überschrift
	 * @param spacesBetweenTitleHeader Abstand zwischen den Spaltenüberschriften
	 */
	private void writeHeaderForDependencies(final PrintWriter writer, final String initialBlanks, final String spacesBetweenTitleHeader) {

		writer.print(initialBlanks);
		writer.print("Abhängig ab Version");
		writer.print(spacesBetweenTitleHeader);
		writer.print("Art der Abhängigkeit");
		writer.print(spacesBetweenTitleHeader);
		writer.print("Version des benötigten Bereichs");
		writer.print(spacesBetweenTitleHeader);
		writer.print("Benötigter Bereich");
		writer.println();
	}


	/**
	 * Schreibt alle unversionierten Änderungen des Konfigurationsbereichs in den übergebenen Writer
	 *
	 * @param unversionedChanges Unversionierte Änderungen. Wird <code>null</code> übergeben, wird nichts gemacht.
	 * @param writer           Writer, in den die Abhängigkeiten geschrieben werden
	 * @param textDepth        Einrückungstiefe
	 */
	private void writeUnversionedChanges(Collection<ConfigurationAreaUnversionedChange> unversionedChanges, final PrintWriter writer, final int textDepth) {

		if(unversionedChanges != null && !unversionedChanges.isEmpty()) {
			final String initialBlanks = createEmptyString(textDepth);

			// Die Abhängigkeiten sollen, wie im Datensatz gespeichert, ausgegeben werden

			writer.print(initialBlanks);

			// Kommentartag (öffnen)
			final String commentStringOpen = "<!--";
			// Kommentartag (schliessen)
			final String commentStringClose = "-->";


			// Kommentar in XML öffnen
			writer.println(commentStringOpen);
			writer.println(initialBlanks + "Unversionierte Änderungen des Bereichs " + _area.getPid());
			// Tabellenüberschrift erzeugen

			writer.print(initialBlanks);
			writer.print("Aktiv ab Version");
			writer.print("      ");
			writer.print("Geänderte Attribut-Typen");
			writer.println();

			for(ConfigurationAreaUnversionedChange unversionedChange : unversionedChanges) {
				writer.print(initialBlanks);
				String[] attributeTypePids = unversionedChange.getAttributeTypePids();
				String first = "Keine";
				if(attributeTypePids.length > 0) {
					first = attributeTypePids[0];
				}
				writer.printf(
						"%-21s %s",
						unversionedChange.getConfigurationAreaVersion(),
						first
				);
				for(int i = 1; i < attributeTypePids.length; i++) {
					// Zeilenumbruch
					writer.println();
					writer.print(initialBlanks);
					String attributeTypePid = attributeTypePids[i];
					writer.printf(
							"%-21s %s",
							"",
							attributeTypePid
					);
				}
				writer.println();
			}
			// Kommentar wieder schließen
			writer.println(initialBlanks + commentStringClose);
		}
	}

	/**
	 * Schreibt eine mengenDefinition (siehe K2S.DTD) als XML in einen Stream.
	 *
	 * @param objectSetTypeProperties Objekt, das alle Informationen einer mengenDefinition (siehe K2S.DTD) enthält
	 * @param writer                  Stream, in den das Objekt geschrieben wird
	 * @param textDepth               Einrückungstiefe des Textes
	 */
	private void writeSetDefinition(final ObjectSetTypeProperties objectSetTypeProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<mengenDefinition");

		writeAttributeAndValueUncheckedValue(" pid", objectSetTypeProperties.getPid(), writer);

		final String[] elements = objectSetTypeProperties.getElements();

		// Attribut elemente schreiben, es ist vom Typ !NMTOKENS!
		writer.print(" elemente=\"");
		for(int nr = 0; nr < elements.length; nr++) {
			if(nr != 0) {
				// Vor dem ersten darf kein Leerzeichen stehen, vor allen anderen schon.
				// Dadurch wird das elemente=String0 String1 String2 ..... erzeugt
				writer.print(" ");
			}
			writer.print(elements[nr]);
		}

		writer.print("\"");

		writeJaNein(" aenderbar", objectSetTypeProperties.getMutable(), writer);
		writeAttributeAndValueCheckedValue(" mindestens", Integer.toString(objectSetTypeProperties.getMinimum()), writer);
		writeAttributeAndValueCheckedValue(" hoechstens", Integer.toString(objectSetTypeProperties.getMaximum()), writer);
		if(objectSetTypeProperties.getReferenceType() != null) {
			writeAttributeAndValueCheckedValue(" referenzierungsart", objectSetTypeProperties.getReferenceType().getValue(), writer);
		}

		writer.println(">");

		writeInfo(objectSetTypeProperties.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</mengenDefinition>");
	}

	/**
	 * Schreibt eine attributListenDefinition (siehe K2S.DTD) in einen Stream
	 *
	 * @param attributeListProperties Objekt, das ein attributListenDefinition darstellt
	 * @param writer                  Stream in den das Objekt geschrieben wird
	 * @param textDepth               Einrückungstiefe des Textes
	 */
	private void writeAttributeListDefinition(final AttributeListProperties attributeListProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<attributlistenDefinition");

		writeAttributeAndValueUncheckedValue(" pid", attributeListProperties.getPid(), writer);
		writeAttributeAndValueCheckedValue(" name", attributeListProperties.getName(), writer);
		writer.println(">");

		// info
		writeInfo(attributeListProperties.getInfo(), writer, textDepth + 1);

		// (attribut|attributliste)+
		final Object[] attributeAndAttributeList = attributeListProperties.getAttributeAndAttributeList();
		for(int nr = 0; nr < attributeAndAttributeList.length; nr++) {
			if(attributeAndAttributeList[nr] instanceof PlainAttributeProperties) {
				writeAttribute((PlainAttributeProperties)attributeAndAttributeList[nr], writer, textDepth + 1);
			}
			else {
				writeAttributeList((ListAttributeProperties)attributeAndAttributeList[nr], writer, textDepth + 1);
			}
		}

		writer.print(initialBlanks);
		writer.println("</attributlistenDefinition>");
	}

	/**
	 * Erzeugt textDepth viele Tabs in einem String und gibt diesen leeren String zurück.
	 *
	 * @param textDepth s.o.
	 *
	 * @return s.o.
	 */
	private String createEmptyString(final int textDepth) {
		final StringBuffer initialBlanksStringbuffer = new StringBuffer();
		for(int nr = 0; nr < textDepth; nr++) {
			initialBlanksStringbuffer.append(_emptyString);
		}
		return initialBlanksStringbuffer.toString();
	}

	private void writeTypeDefinition(final SystemObjectTypeProperties typeProperties, final PrintWriter writer, final int textDepth) {

		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<typDefinition");

		// Pid
		writeAttributeAndValueUncheckedValue(" pid", typeProperties.getPid(), writer);
		// Name
		writeAttributeAndValueCheckedValue(" name", typeProperties.getName(), writer);

		// objektNamenPermanent
		writeJaNein(" objektNamenPermanent", typeProperties.getObjectNamesPermanent(), writer);

		if(typeProperties.getPersistenceMode() != PersistenceMode.UNDEFINED) {
			// persistenzModus
			if(typeProperties.getPersistenceMode() == PersistenceMode.TRANSIENT_OBJECTS) {
				writeAttributeAndValueCheckedValue(" persistenzModus", "transient", writer);
			}
			else if(typeProperties.getPersistenceMode() == PersistenceMode.PERSISTENT_OBJECTS) {
				writeAttributeAndValueCheckedValue(" persistenzModus", "persistent", writer);
			}
			else if(typeProperties.getPersistenceMode() == PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART) {
				writeAttributeAndValueCheckedValue(" persistenzModus", "persistentUndUngültigNachNeustart", writer);
			}
		}
		writer.println(">");

		// Alle Attribute wurden geschrieben

		// Elemente schreiben

		// Info
		writeInfo(typeProperties.getInfo(), writer, textDepth + 1);

		// (erweitert+ | basis)

		if(typeProperties.getExtendedPids().length > 0) {
			// Erweitert wurde gesetzt

			// Alle Pids, die geschrieben werden müssen
			final String[] extendedPids = typeProperties.getExtendedPids();

			// Jeder Eintrag muss um eins nach rechts versetzt werden
			final String extendedBlanks = initialBlanks + _emptyString;

			for(int nr = 0; nr < extendedPids.length; nr++) {
				writer.print(extendedBlanks);
				writer.print("<erweitert");
				writeAttributeAndValueUncheckedValue(" pid", extendedPids[nr], writer);
				writer.println("/>");
			}
		}
		else {
			// basis schreiben

			// Jeder Eintrag muss um eins nach rechts versetzt werden
			final String baseBlanks = initialBlanks + _emptyString;
			writer.print(baseBlanks);
			writer.print("<basis");

			writeJaNein(" konfigurierend", typeProperties.getConfiguring(), writer);
			writer.println("/>");
		}

		// (attributgruppe | menge)*

		// Die Reihenfolge der Elemente wird eingehalten

		final Object[] atgAndSet = typeProperties.getAtgAndSet();

		final List<String> transactionList = new LinkedList<String>(typeProperties.getTransactions());
		// vor dem Sortieren erst in ATGs und Mengen unterteilen
		final List<String> atgList = new LinkedList<String>();
		final List<ConfigurationSet> setList = new LinkedList<ConfigurationSet>();
		for(Object object : atgAndSet) {
			if(object instanceof String) {
				final String pidAtg = (String)object;
				atgList.add(pidAtg);
			}
			else {
				final ConfigurationSet set = (ConfigurationSet)object;
				setList.add(set);
			}
		}

		// Mengen und Attributgruppen sortieren
		Collections.sort(
				atgList, new Comparator<String>() {
			public int compare(final String pid1, final String pid2) {
				final Collator deCollator = Collator.getInstance(Locale.GERMAN);
				return deCollator.compare(pid1, pid2);
			}
		}
		);
		Collections.sort(
				transactionList, new Comparator<String>() {
			public int compare(final String pid1, final String pid2) {
				final Collator deCollator = Collator.getInstance(Locale.GERMAN);
				return deCollator.compare(pid1, pid2);
			}
		}
		);
		Collections.sort(
				setList, new Comparator<ConfigurationSet>() {
			public int compare(final ConfigurationSet o1, final ConfigurationSet o2) {
				final Collator deCollator = Collator.getInstance(Locale.GERMAN);
				return deCollator.compare(o1.getSetTypePid(), o2.getSetTypePid());
			}
		}
		);

		// Attributgruppen und Mengen werden rausgeschrieben
		for(String pidATG : atgList) {
			// Einrücken
			writer.print(initialBlanks + _emptyString);
			writer.print("<attributgruppe");
			writeAttributeAndValueUncheckedValue(" pid", pidATG, writer);
			writer.println("/>");
		}

		for(ConfigurationSet configurationSet : setList) {
			// Es handelt sich um eine Menge
			writeSet(configurationSet, writer, textDepth + 1);
		}

		for(String pidATG : transactionList) {
			// Einrücken
			writer.print(initialBlanks + _emptyString);
			writer.print("<transaktion");
			writeAttributeAndValueUncheckedValue(" pid", pidATG, writer);
			writer.println("/>");
		}

		// defaultParameter rausschreiben
		for(ConfigurationDefaultParameter defaultParameter : typeProperties.getDefaultParameters()) {
			// Einrücken
			writeDefaultParameter(defaultParameter, writer, textDepth + 1);
		}

		// Endtag typeProperties
		writer.print(initialBlanks);
		writer.println("</typDefinition>");
	}

	/**
	 * Schreibt eine "attributgruppenDefinition" (siehe K2S.DTD) mit allen Tags in eine Datei
	 *
	 * @param attributeGroupProperties Objekt, das in der Datei gespeichert werden soll
	 * @param writer                   Objekt, das die Datei darstellt, in die geschrieben werden soll
	 * @param textDepth                Einrüdckungstiefe
	 */
	private void writeAttributeGroupDefinition(final AttributeGroupProperties attributeGroupProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<attributgruppenDefinition");

		// Attribute des Elements schreiben
		writeAttributeAndValueUncheckedValue(" pid", attributeGroupProperties.getPid(), writer);
		writeAttributeAndValueCheckedValue(" name", attributeGroupProperties.getName(), writer);
		// Attribut konfigurierend wird nicht mehr benötigt.
//		writeJaNein(" konfigurierend", attributeGroupProperties.getConfiguring(), writer);
		writeJaNein(" parametrierend", attributeGroupProperties.isParameter(), writer);
		// Der Code wird nicht mehr benötigt
//		writeAttributeAndValueCheckedValue(" code", Short.toString(attributeGroupProperties.getCode()), writer);
		writer.println(">");

		// Elemente schreiben

		//info
		writeInfo(attributeGroupProperties.getInfo(), writer, textDepth + 1);

		// aspekt*
		final ConfigurationAspect[] aspects = attributeGroupProperties.getConfigurationAspect();
		Arrays.sort(
				aspects, new Comparator<ConfigurationAspect>() {
			public int compare(final ConfigurationAspect o1, final ConfigurationAspect o2) {
				return o1.getPid().compareTo(o2.getPid());
			}
		}
		);
		for(int nr = 0; nr < aspects.length; nr++) {

			writeAspect(aspects[nr], writer, textDepth + 1);
		}

		// (Attribut|Attributliste)+
		Object[] attributeAndAttributeList = attributeGroupProperties.getAttributeAndAttributeList();
		for(int nr = 0; nr < attributeAndAttributeList.length; nr++) {
			final Object object = attributeAndAttributeList[nr];

			if(object instanceof PlainAttributeProperties) {
				writeAttribute((PlainAttributeProperties)object, writer, textDepth + 1);
			}
			else if(object instanceof ListAttributeProperties) {
				writeAttributeList((ListAttributeProperties)object, writer, textDepth + 1);
			}
		}

		writer.print(initialBlanks);
		writer.println("</attributgruppenDefinition>");
	}

	/**
	 * Schreibt eine "transaktionsDefinition" (siehe K2S.DTD) mit allen Tags in eine Datei
	 *
	 * @param transactionProperties Objekt, das in der Datei gespeichert werden soll
	 * @param writer                Objekt, das die Datei darstellt, in die geschrieben werden soll
	 * @param textDepth             Einrüdckungstiefe
	 */
	private void writeTransactionDefinition(final TransactionProperties transactionProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<transaktionsDefinition");

		// Attribute des Elements schreiben
		writeAttributeAndValueUncheckedValue(" pid", transactionProperties.getPid(), writer);
		writeAttributeAndValueCheckedValue(" name", transactionProperties.getName(), writer);

		writer.println(">");

		// Elemente schreiben

		//info
		writeInfo(transactionProperties.getInfo(), writer, textDepth + 1);

		// aspekt*
		final ConfigurationAspect[] aspects = transactionProperties.getConfigurationAspect();
		Arrays.sort(
				aspects, new Comparator<ConfigurationAspect>() {
					public int compare(final ConfigurationAspect o1, final ConfigurationAspect o2) {
						return o1.getPid().compareTo(o2.getPid());
					}
				}
		);
		for(final ConfigurationAspect aspect : aspects) {
			writeAspect(aspect, writer, textDepth + 1);
		}

		// akzeptiert?
		final List<TransactionProperties.DataIdentification> possibleDids = transactionProperties.getPossibleDids();
		if(possibleDids != null && possibleDids.size() > 0) writeDids("akzeptiert", possibleDids, writer, textDepth + 1);

		// benötigt?
		final List<TransactionProperties.DataIdentification> requiredDids = transactionProperties.getRequiredDids();
		if(requiredDids != null && requiredDids.size() > 0) writeDids("benötigt", requiredDids, writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</transaktionsDefinition>");
	}

	private void writeDids(
			final String type, final List<TransactionProperties.DataIdentification> possibleDids, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);
		writer.print(initialBlanks);
		writer.print("<");
		writer.print(type);
		writer.println(">");
		for(final TransactionProperties.DataIdentification identification: possibleDids){
			writeDid(identification, writer, textDepth+1);
		}
		writer.print(initialBlanks);
		writer.println("</" + type + ">");
	}

	private void writeDid(final TransactionProperties.DataIdentification identification, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);
		writer.print(initialBlanks);
		writer.print("<transaktionsEinschränkung");
		writeAttributeAndValueCheckedValue(" nurTransaktionsObjekt", identification.isOnlyTransactionObject() ? "ja" : "", writer);
		writeAttributeAndValueCheckedValue(" objektTyp", identification.getObjectType(), writer);
		writeAttributeAndValueCheckedValue(" attributGruppe", identification.getAttributeGroup(), writer);
		writeAttributeAndValueCheckedValue(" aspekt", identification.getAspect(), writer);
		writer.println("/>");
	}

	/**
	 * Schreibt eine "aspektDefinition" (siehe K2S.DTD) in eine Datei.
	 *
	 * @param aspectProperties Objekt, das gespeichert werden soll
	 * @param writer           Stellt die Datei dar
	 * @param textDepth        Einrückungstiefe
	 */
	private void writeAspectDefinition(final AspectProperties aspectProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<aspektDefinition");

		writeAttributeAndValueUncheckedValue(" pid", aspectProperties.getPid(), writer);
		writeAttributeAndValueCheckedValue(" name", aspectProperties.getName(), writer);
		// Code wird nicht mehr benötigt
//		writeAttributeAndValueCheckedValue(" code", Short.toString(aspectProperties.getCode()), writer);
		writer.println(">");

		writeInfo(aspectProperties.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</aspektDefinition>");
	}

	/**
	 * Schreibt eine "attributDefinition" (siehe K2S.DTD) in eine Datei.
	 *
	 * @param attributeTypeProperties Objekt, das gespeichert werden soll
	 * @param writer                  Stellt die Datei dar
	 * @param textDepth               Einrückungstiefe
	 */
	private void writeAttributeDefinition(final AttributeTypeProperties attributeTypeProperties, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<attributDefinition");

		writeAttributeAndValueUncheckedValue(" pid", attributeTypeProperties.getPid(), writer);
		writeAttributeAndValueCheckedValue(" name", attributeTypeProperties.getName(), writer);
		writer.println(">");

		writeInfo(attributeTypeProperties.getInfo(), writer, textDepth + 1);

		// Attributtyp
		writeAttributeType(attributeTypeProperties.getAttributeType(), writer, textDepth + 1);

		// default
		writeDefault(attributeTypeProperties.getDefault(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</attributDefinition>");
	}

	/**
	 * Schreibt ...attribute="value"... in den übergebenen Stream. Ist attribute oder value <code>null</code> oder "", so wird nichts geschrieben
	 * @param attribute s.o.
	 * @param value     s.o.
	 * @param writer    Stream, in dem die Daten geschrieben werden
	 */
	private void writeAttributeAndValueCheckedValue(final String attribute, final String value, final PrintWriter writer) {
		if(!"".equals(attribute) && !"".equals(value) && value != null && attribute != null) {
			writeAttributeAndValueUncheckedValue(attribute, value, writer);
		}
	}

	/**
	 * StringBuilder aus Performancegründen wiederverwnden
	 */
	private StringBuilder _builder = new StringBuilder();

	private String xmlText(final String text) {
		// alter Code (ineffizient):
		// return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");

		int length = text.length();
		for(int i = 0; i < length; i++){
			char c = text.charAt(i);
			switch(c){
				case '&': _builder.append("&amp;"); break;
				case '<': _builder.append("&lt;"); break;
				case '>': _builder.append("&gt;"); break;
				case '"': _builder.append("&quot;"); break;
				case '\'': _builder.append("&apos;"); break;
				default: _builder.append(c);
			}
		}
		String result = _builder.toString();
		_builder.setLength(0);
		return result;
	}


	/**
	 * Schreibt ...attribute="value"... in den übergebenen Stream. Weder attribute noch value werden auf <code>null</code> oder "" geprüft.
	 *
	 * @param attribute s.o.
	 * @param value     s.o.
	 * @param writer    Stream, in dem die Daten geschrieben werden
	 */
	private void writeAttributeAndValueUncheckedValue(final String attribute, final String value, final PrintWriter writer) {
		writer.print(attribute);
		writer.print("=\"");
		writer.print(xmlText(value));
		writer.print("\"");
	}

	/**
	 * Schreibt folgenden String "attributeName=ja" oder " attributeName=nein" in den übergebenen Stream.
	 *
	 * @param attributeName Name des Attributes, der vor dem = stehen soll
	 * @param value         Wert, der hinter dem = stehen soll
	 * @param writer        Steam, in den die Daten eingetragen werden
	 */
	private void writeJaNein(final String attributeName, final boolean value, final PrintWriter writer) {

		writer.print(attributeName);

		if(value) {
			writer.print("=\"ja\"");
		}
		else {
			writer.print("=\"nein\"");
		}
	}

	/**
	 * Schreibt das Element "default" (siehe K2S.DTD) in einen Stream
	 *
	 * @param defaultValue Wert, der geschrieben soll
	 * @param writer       Objekt mit dem die Menge gespeichert werden kann
	 * @param textDepth    Einrückungstiefe ab dem der Text in der Datei erscheinen soll
	 */
	private void writeDefault(final String defaultValue, final PrintWriter writer, final int textDepth) {

		// !"null".equals(defaultValue)
		if(defaultValue != null) {
			// soviele Leerzeichen stehen mindestens vor einem Eintrag
			final String initialBlanks = createEmptyString(textDepth);

			writer.print(initialBlanks);
			writer.print("<default");

			writeAttributeAndValueUncheckedValue(" wert", defaultValue, writer);
			writer.println("/>");
		}
	}

	/**
	 * Schreibt das Element "konfigurationsAenderung". Das Element kann mehrfach geschrieben werden
	 *
	 * @param configurationAreaChangeInformations
	 *                  Alle Änderungen, die gespeichert werden sollen
	 * @param writer    Objekt mit dem die Menge gespeichert werden kann
	 * @param textDepth Einrückungstiefe ab dem der Text in der Datei erscheinen soll
	 */
	private void writeConfigAreaChanges(
			final ConfigurationAreaChangeInformation[] configurationAreaChangeInformations, final PrintWriter writer, final int textDepth
	) {
		// soviele Leerzeichen stehen mindestens vor einem Eintrag
		final String initialBlanks = createEmptyString(textDepth);

		for(ConfigurationAreaChangeInformation areaChangeInformation : configurationAreaChangeInformations) {
			// Starttag schreiben
			writer.print(initialBlanks);
			writer.print("<konfigurationsAenderung");

			final DateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy");

			writeAttributeAndValueUncheckedValue(" stand", timeFormat.format(new Date(areaChangeInformation.getCondition())), writer);
			final String version;

			if(areaChangeInformation.getVersion() >= 0) {
				version = Integer.toString(areaChangeInformation.getVersion());
			}
			else {
				// wurde die Version nicht gesetzt, so wird "" geschrieben
				version = "";
			}
			writeAttributeAndValueUncheckedValue(" version", version, writer);

			writeAttributeAndValueUncheckedValue(" autor", areaChangeInformation.getAuthor(), writer);

			writeAttributeAndValueUncheckedValue(" grund", areaChangeInformation.getReason(), writer);
			writer.print(">");

			// Zusatztext, den jemand geschrieben haben kann (eine Stufe tiefer einrücken, als die tags)
//			writer.print(initialBlanks + _emptyString);
			writer.print(areaChangeInformation.getText());
//			System.out.println("Text2:" + areaChangeInformation.getText() + ":Ende2");

			// Endtag
//			writer.print(initialBlanks);
			writer.println("</konfigurationsAenderung>");
		}
	}

	/**
	 * Schreibt das Element "info" in den übergebenen Stream
	 *
	 * @param info      Objekt, das in eine XML-Datei geschrieben werden soll
	 * @param writer    Objekt mit dem die Menge gespeichert werden kann
	 * @param textDepth Einrückungstiefe ab dem der Text in der Datei erscheinen soll
	 */
	private void writeInfo(final SystemObjectInfo info, final PrintWriter writer, final int textDepth) {

		// Gibt es eine Kurzinfo. Bei manchen Objekte kann dies optional angegeben werden, darum muss
		// diese abfrage sein.
//		if (info != null && info.getShortInfo() != null && !"".equals(info.getShortInfo())) {
//
//			// soviele Leerzeichen stehen mindestens vor einem Eintrag
//			final String initialBlanks = createEmptyString(textDepth);
//
//			// Starttag schreiben
//			writer.print(initialBlanks);
//			writer.println("<info>");
//
//			// soviel muss immer eingerückt werden
//			StringBuffer shortInfo = new StringBuffer(initialBlanks);
//			// eins mehr einrücken als den Rest
//			shortInfo.append(_emptyString);
//
//			shortInfo.append("<kurzinfo>");
//			shortInfo.append(info.getShortInfo());
//			shortInfo.append("</kurzinfo>");
//			writer.println(shortInfo);
//
//			// Beschreibung, falls vorhanden
//			if (!"".equals(info.getDescription())) {
//				StringBuffer description = new StringBuffer(initialBlanks);
//				description.append(_emptyString);
//				description.append("beschreibung");
//				shortInfo.append(info.getDescription());
//				description.append("/beschreibung");
//				writer.println(description);
//			}
//
//			// Endtag schreiben
//			writer.print(initialBlanks);
//			writer.println("</info>");
//		}


		if(info != null && info.getShortInfo() != null && !"".equals(info.getShortInfo())) {

			// soviele Leerzeichen stehen mindestens vor einem Eintrag
			final String initialBlanks = createEmptyString(textDepth);

			// Starttag schreiben
			writer.print(initialBlanks);
			writer.println("<info>");

			// Soviele Leerzeichen wird ShortInfo und Beschreibung eingerückt
			final String blanksShortInfoAndDescription = createEmptyString(textDepth + 1);
			writer.print(blanksShortInfoAndDescription);
			writer.print("<kurzinfo>");
			writer.print(info.getShortInfoAsXML());
			writer.println("</kurzinfo>");

			if(!"".equals(info.getDescription())) {
				writer.print(blanksShortInfoAndDescription);
				writer.print("<beschreibung>");
				writer.print(info.getDescriptionAsXML());
				writer.println("</beschreibung>");
			}

			// Endtag schreiben
			writer.print(initialBlanks);
			writer.println("</info>");
		}
	}

	/**
	 * Speichert eine "menge" (siehe K2S.DTD) in einer XML-Datei
	 *
	 * @param set       Menge, die gespeichert werden soll
	 * @param writer    Objekt mit dem die Menge gespeichert werden kann
	 * @param textDepth Einrückungstiefe ab dem der Text in der Datei erscheinen soll
	 */
	private void writeSet(final ConfigurationSet set, final PrintWriter writer, final int textDepth) {
		// soviele Leerzeichen stehen mindestens vor einem Eintrag
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<menge");
		writeAttributeAndValueUncheckedValue(" pid", set.getSetTypePid(), writer);
		writeAttributeAndValueUncheckedValue(" name", set.getObjectSetName(), writer);
		writeJaNein(" erforderlich", set.getRequired(), writer);
		writer.println(">");

		writeInfo(set.getInfo(), writer, textDepth + 1);
		writer.print(initialBlanks);
		writer.println("</menge>");
	}

	/**
	 * Speicher einen Default-Parameter-Datensatz (siehe K2S.DTD) in einer XML-Datei.
	 *
	 * @param defaultParameter der Default-Parameter-Datensatz
	 * @param writer           Objekt, mit dem der Datensatz gespeichert werden kann
	 * @param textDepth        Einrückungstiefe, ab dem der Text in der Datei erscheinen soll
	 */
	private void writeDefaultParameter(final ConfigurationDefaultParameter defaultParameter, final PrintWriter writer, final int textDepth) {
		// soviele Leerzeichen stehen mindestens vor einem Eintrag
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<defaultParameter");
		writeAttributeAndValueUncheckedValue(" attributgruppe", defaultParameter.getPidAtg(), writer);
		writeAttributeAndValueCheckedValue(" typ", defaultParameter.getPidType(), writer);
		writer.println(">");

		// hier kommt der Datensatz hin
		writeDatasetElements(defaultParameter.getDataAnddataListAndDataField(), writer, textDepth);

		writer.print(initialBlanks);
		writer.println("</defaultParameter>");
	}

	/**
	 * Speichert einen "aspekt" (siehe K2S.DTD)
	 *
	 * @param configurationAspect Objekt, das gespeichert werden soll
	 * @param writer              Stellt die Datei dar
	 * @param textDepth           Einrückungstiefe
	 */
	private void writeAspect(final ConfigurationAspect configurationAspect, final PrintWriter writer, final int textDepth) {
		// soviele Leerzeichen stehen mindestens vor einem Eintrag
		final String initialBlanks = createEmptyString(textDepth);
		writer.print(initialBlanks);
		writer.print("<aspekt");

		writeAttributeAndValueUncheckedValue(" pid", configurationAspect.getPid(), writer);

		// Entweder konfigurationsModus oder onlineModus
		final AttributeGroupUsage.Usage usage = configurationAspect.getUsage();

		if(usage != null) {
			if(usage == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain || usage == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
			   || usage == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) {
				// onlineModus muss geschrieben werden
				writeAttributeAndValueCheckedValue(" onlineModus", configurationAspect.getUsage().getValue(), writer);
			}
			else {
				// konfigurationsModus muss geschrieben werden
				writeAttributeAndValueCheckedValue(" konfigurationsModus", configurationAspect.getUsage().getValue(), writer);
			}
		}

		writer.println(">");

		writeInfo(configurationAspect.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</aspekt>");
	}

	/**
	 * Speichert einen "attribut" (siehe K2S.DTD)
	 *
	 * @param attribute Objekt, das gespeichert werden soll
	 * @param writer    Stellt die Datei dar
	 * @param textDepth Einrückungstiefe
	 */
	private void writeAttribute(PlainAttributeProperties attribute, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);
		writer.print(initialBlanks);
		writer.print("<attribut");

		writeAttributeAndValueUncheckedValue(" pid", attribute.getAttributeTypePid(), writer);
		writeAttributeAndValueCheckedValue(" anzahl", Integer.toString(attribute.getMaxCount()), writer);
		writeAttributeAndValueCheckedValue(" anzahlIst", attribute.getTargetValue().getValue(), writer);
		writeAttributeAndValueCheckedValue(" name", attribute.getName(), writer);
		writer.println(">");

		writeInfo(attribute.getInfo(), writer, textDepth + 1);

		writeDefault(attribute.getDefault(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</attribut>");
	}

	/**
	 * Speichert einen "attributliste" (siehe K2S.DTD)
	 *
	 * @param attributeList Objekt, das gespeichert werden soll
	 * @param writer        Stellt die Datei dar
	 * @param textDepth     Einrückungstiefe
	 */
	private void writeAttributeList(ListAttributeProperties attributeList, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);
		writer.print(initialBlanks);
		writer.print("<attributliste");

		writeAttributeAndValueUncheckedValue(" pid", attributeList.getAttributeTypePid(), writer);
		writeAttributeAndValueCheckedValue(" anzahl", Integer.toString(attributeList.getMaxCount()), writer);
		writeAttributeAndValueCheckedValue(" anzahlIst", attributeList.getTargetValue().getValue(), writer);
		writeAttributeAndValueCheckedValue(" name", attributeList.getName(), writer);
		writer.println(">");

		writeInfo(attributeList.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</attributliste>");
	}

	/**
	 * Diese Methode schreibt einen Attributtyp. Dieser kann vom Typ zeichenkette, ganzzahl, zeitstempel, objektReferenz oder kommazahl sein.
	 *
	 * @param unknownAttributeType Objekt, das folgende Typen besitzen kann: ConfigurationString, ConfigurationIntegerDef, ConfigurationTimeStamp,
	 *                             ConfigurationObjectReference, ConfigurationDoubleDef
	 * @param writer               Stream, in den das Objekt geschrieben wird
	 * @param textDepth            Einrückgungstiefe des Textes
	 */
	private void writeAttributeType(Object unknownAttributeType, final PrintWriter writer, final int textDepth) {

		final String initialBlanks = createEmptyString(textDepth);

		if(unknownAttributeType instanceof ConfigurationString) {
			// zeichenkette schreiben
			final ConfigurationString string = (ConfigurationString)unknownAttributeType;

			writer.print(initialBlanks);
			writer.print("<zeichenkette");

			writeAttributeAndValueCheckedValue(" laenge", Integer.toString(string.getLength()), writer);
			writeAttributeAndValueCheckedValue(" kodierung", string.getStringEncoding(), writer);
			writer.println("/>");
		}
		else if(unknownAttributeType instanceof ConfigurationIntegerDef) {
			// ganzzahl schreiben

			final ConfigurationIntegerDef integer = (ConfigurationIntegerDef)unknownAttributeType;

			writer.print(initialBlanks);
			writer.print("<ganzzahl");

			writeAttributeAndValueCheckedValue(" bits", Integer.toString(integer.getBits()), writer);
			writer.println(">");

			// (bereich|zustand)+
			final Object[] regionAndState = integer.getValueRangeAndState();

			for(int nr = 0; nr < regionAndState.length; nr++) {
				if(regionAndState[nr] instanceof ConfigurationValueRange) {
					// Das Objekt ist vom Typ bereich
					writeRegion((ConfigurationValueRange)regionAndState[nr], writer, textDepth + 1);
				}
				else {
					// Das Objekt ist vom Typ zustand
					writeState((ConfigurationState)regionAndState[nr], writer, textDepth + 1);
				}
			}

			writer.print(initialBlanks);
			writer.println("</ganzzahl>");
		}
		else if(unknownAttributeType instanceof ConfigurationTimeStamp) {
			// zeitstempel schreiben
			writeTimeStamp((ConfigurationTimeStamp)unknownAttributeType, writer, textDepth);
		}
		else if(unknownAttributeType instanceof ConfigurationObjectReference) {
			// objektReferenz
			writeObjectReference((ConfigurationObjectReference)unknownAttributeType, writer, textDepth);
		}
		else if(unknownAttributeType instanceof ConfigurationDoubleDef) {
			// kommazahl schreiben
			writeFloatingPointNumber((ConfigurationDoubleDef)unknownAttributeType, writer, textDepth);
		}
	}

	/**
	 * Schreibt eine kommazahl (siehe K2S.DTD) als XML Text in einen Stream.
	 *
	 * @param doubleDef Objekt, das eine kommazahl(siehe K2S.DTD) darstellt
	 * @param writer    Stream, in den der Text geschrieben wird
	 * @param textDepth Einrückkungstiefe des Textes
	 */
	private void writeFloatingPointNumber(final ConfigurationDoubleDef doubleDef, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<kommazahl");

		writeAttributeAndValueCheckedValue(" einheit", doubleDef.getUnit(), writer);
		writeAttributeAndValueCheckedValue(" genauigkeit", doubleDef.getAccuracy().getValue(), writer);

		writer.println("/>");
	}

	/**
	 * Schreibt eine objektReferenz (siehe K2S.DTD) in einen Stream
	 *
	 * @param objectReference Objekt, das eine objektReferenz nach K2S.DTD darstellt
	 * @param writer          Stream, in den das Objekt als XML Text geschrieben wird
	 * @param textDepth       Einrückungstiefe des Textes
	 */
	private void writeObjectReference(final ConfigurationObjectReference objectReference, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<objektReferenz");
		writeAttributeAndValueCheckedValue(" typ", objectReference.getReferenceObjectType(), writer);
		if(objectReference.getUndefined() != null) {
			writeAttributeAndValueCheckedValue(" undefiniert", objectReference.getUndefined().getValue(), writer);
		}
		final ReferenceType referenceType = objectReference.getReferenceType();

		if(referenceType == ReferenceType.AGGREGATION) {
			writeAttributeAndValueCheckedValue(" referenzierungsart", "aggregation", writer);
		}
		else if(referenceType == ReferenceType.ASSOCIATION) {
			writeAttributeAndValueCheckedValue(" referenzierungsart", "assoziation", writer);
		}
		else if(referenceType == ReferenceType.COMPOSITION) {
			writeAttributeAndValueCheckedValue(" referenzierungsart", "komposition", writer);
		}

		writer.println("/>");
	}

	/**
	 * Schreibt einen "zeitstempel" (siehe K2S.dtd) in eine Datei.
	 *
	 * @param timeStamp Objekt, das gespeichert werden soll
	 * @param writer    Stellt die Datei dar
	 * @param textDepth Einrückungstiefe
	 */
	private void writeTimeStamp(final ConfigurationTimeStamp timeStamp, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<zeitstempel");

		writeJaNein(" relativ", timeStamp.getRelative(), writer);
		writeAttributeAndValueCheckedValue(" genauigkeit", timeStamp.getAccuracy().getValue(), writer);
		writer.println("/>");
	}

	/**
	 * Schreibt ein Element vom Typ bereich (siehe K2S.DTD) in eine XML Datei.
	 *
	 * @param configurationValueRange Objekt, dass das Element bereich darstellt
	 * @param writer                  Stream
	 * @param textDepth               Einrückungstiefe des Textes
	 */
	private void writeRegion(final ConfigurationValueRange configurationValueRange, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<bereich");

		writeAttributeAndValueCheckedValue(" skalierung", configurationValueRange.getScaleAsString(), writer);
		writeAttributeAndValueUncheckedValue(" minimum", Long.toString(configurationValueRange.getMinimum()), writer);
		writeAttributeAndValueCheckedValue(" maximum", Long.toString(configurationValueRange.getMaximum()), writer);
		writeAttributeAndValueCheckedValue(" einheit", configurationValueRange.getUnit(), writer);

		writer.println(">");

		writeInfo(configurationValueRange.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</bereich>");
	}

	/**
	 * Schreibt ein Element vom Typ "zustand" (siehe K2S.DTD) in eine XML Datei.
	 *
	 * @param configurationState Objekt, dass das Element zustand darstellt
	 * @param writer             Stream
	 * @param textDepth          Einrückungstiefe des Textes
	 */
	private void writeState(final ConfigurationState configurationState, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<zustand");

		writeAttributeAndValueUncheckedValue(" name", configurationState.getName(), writer);
		writeAttributeAndValueUncheckedValue(" wert", Long.toString(configurationState.getValue()), writer);
		writer.println(">");

		writeInfo(configurationState.getInfo(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</zustand>");
	}

	/**
	 * Schreibt ein konfigurationsObjekt (siehe K2S.DTD) als XML Text in einen Stream.
	 *
	 * @param configurationObject Objekt, das alle Informationen eines konfigurationsObjekt (siehe K2S.DTD) enthält
	 * @param writer              Stream, in dem das Objekt als XML Text gespeichert wird
	 * @param textDepth           Einrückungstiefe des Textes
	 */
	private void writeConfigurationObject(final ConfigurationConfigurationObject configurationObject, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<konfigurationsObjekt");

		writeAttributeAndValueUncheckedValue(" pid", configurationObject.getPid(), writer);
		// Es werden keine IdŽs geschrieben
//		writeAttributeAndValueCheckedValue(" id", Long.toString(configurationObject.getId()), writer);
		writeAttributeAndValueCheckedValue(" name", configurationObject.getName(), writer);
		writeAttributeAndValueUncheckedValue(" typ", configurationObject.getType(), writer);
		writer.println(">");

		// info
		writeInfo(configurationObject.getInfo(), writer, textDepth + 1);

		// (datensatz|objektMenge)*
		final Object[] datasetAndObjectSet = configurationObject.getDatasetAndObjectSet();

		for(int nr = 0; nr < datasetAndObjectSet.length; nr++) {
			if(datasetAndObjectSet[nr] instanceof ConfigurationObjectSet) {
				// objektMenge
				writeObjectSet((ConfigurationObjectSet)datasetAndObjectSet[nr], writer, textDepth + 1);
			}
			else if(datasetAndObjectSet[nr] instanceof ConfigurationDataset) {
				// datensatz
				writeDataset((ConfigurationDataset)datasetAndObjectSet[nr], writer, textDepth + 1);
			}
			else {
				throw new IllegalArgumentException("Unbekannte Klasse: " + datasetAndObjectSet[nr].getClass());
			}
		}
		// defaultParameter rausschreiben
		for(ConfigurationDefaultParameter defaultParameter : configurationObject.getDefaultParameters()) {
			writeDefaultParameter(defaultParameter, writer, textDepth + 1);
		}

		writer.print(initialBlanks);
		writer.println("</konfigurationsObjekt>");
	}

	/**
	 * Speichert einen datensatz (siehe K2S.DTD) als XML Text. Das Attribut "pid" wird automatisch durch die Kombination "attributgruppe" und "aspekt" ersetzt.
	 *
	 * @param dataset   Objekt, das alle Informationen über einen datensatz (siehe K2S.DTD) enthält
	 * @param writer    Stream, in dem das Objekt als XML Text gespeichert wird
	 * @param textDepth Einrückungstiefe des XML-Strings
	 */
	private void writeDataset(final ConfigurationDataset dataset, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<datensatz");

		writeAttributeAndValueCheckedValue(" attributgruppe", dataset.getPidATG(), writer);
		writeAttributeAndValueCheckedValue(" aspekt", dataset.getPidAspect(), writer);
		writer.println(">");

		final DatasetElement[] dateAnddataListAndDataField = dataset.getDataAnddataListAndDataField();

		writeDatasetElements(dateAnddataListAndDataField, writer, textDepth);

		writer.print(initialBlanks);
		writer.println("</datensatz>");
	}

	/**
	 * Speichert den Inhalt eines Datensatzes.
	 *
	 * @param dateAnddataListAndDataField Inhalt des Datensatzes
	 * @param writer                      Stream, in dem das Objekt als XML Text gespeichert wird
	 * @param textDepth                   Einrückungstiefe des XML-Strings
	 */
	private void writeDatasetElements(final DatasetElement[] dateAnddataListAndDataField, final PrintWriter writer, final int textDepth) {
		for(int nr = 0; nr < dateAnddataListAndDataField.length; nr++) {
			if(dateAnddataListAndDataField[nr] instanceof ConfigurationData) {
				writeDate((ConfigurationData)dateAnddataListAndDataField[nr], writer, textDepth + 1);
			}
			else if(dateAnddataListAndDataField[nr] instanceof ConfigurationDataList) {
				writeDataList((ConfigurationDataList)dateAnddataListAndDataField[nr], writer, textDepth + 1);
			}
			else {
				writeDataField((ConfigurationDataField)dateAnddataListAndDataField[nr], writer, textDepth + 1);
			}
		}
	}

	/**
	 * Schreibt ein datum (siehe K2S.DTD)
	 *
	 * @param data      Objekt, das alle Informationen eines "datum" enthält
	 * @param writer    Stream, in dem die Daten gespeichert werden
	 * @param textDepth Einrückungstiefe
	 */
	private void writeDate(final ConfigurationData data, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<datum");
		writeAttributeAndValueUncheckedValue(" name", data.getName(), writer);
		writeAttributeAndValueUncheckedValue(" wert", data.getValue(), writer);
		writer.println("/>");
	}

	/**
	 * Schreibt eine datenliste (siehe K2S.DTD)
	 *
	 * @param dataList  Objekt, das alle Informationen für eine datenliste (siehe K2S.DTD) enthält
	 * @param writer    Stream, auf dem das Objekt als XML Text gespeichert wird
	 * @param textDepth Einrückungstiefe
	 */
	private void writeDataList(final ConfigurationDataList dataList, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<datenliste");
		writeAttributeAndValueUncheckedValue(" name", dataList.getName(), writer);
		writer.println(">");

		// (datum|datenliste|datenfeld)+
		final Object[] dateAndDataListAndDataField = dataList.getDataAndDataListAndDataField();
		for(int nr = 0; nr < dateAndDataListAndDataField.length; nr++) {
			if(dateAndDataListAndDataField[nr] instanceof ConfigurationData) {
				writeDate((ConfigurationData)dateAndDataListAndDataField[nr], writer, textDepth + 1);
			}
			else if(dateAndDataListAndDataField[nr] instanceof ConfigurationDataList) {
				writeDataList((ConfigurationDataList)dateAndDataListAndDataField[nr], writer, textDepth + 1);
			}
			else if(dateAndDataListAndDataField[nr] instanceof ConfigurationDataField) {
				writeDataField((ConfigurationDataField)dateAndDataListAndDataField[nr], writer, textDepth + 1);
			}
		}

		writer.print(initialBlanks);
		writer.println("</datenliste>");
	}

	/**
	 * Schreibt ein datenfeld (siehe K2S.DTD)
	 *
	 * @param dataField Objekt, das alle Informationen für ein datenfeld (siehe K2S.DTD) enthält
	 * @param writer    Stream, auf dem das Objekt als XML Text gespeichert wird
	 * @param textDepth Einrückungstiefe
	 */
	private void writeDataField(final ConfigurationDataField dataField, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<datenfeld");
		writeAttributeAndValueUncheckedValue(" name", dataField.getName(), writer);
		writer.println(">");

		// (datum|datenliste)*
		final Object[] dateAndDataList = dataField.getDataAndDataList();
		for(int nr = 0; nr < dateAndDataList.length; nr++) {
			if(dateAndDataList[nr] instanceof ConfigurationData) {
				writeDate((ConfigurationData)dateAndDataList[nr], writer, textDepth + 1);
			}
			else {
				writeDataList((ConfigurationDataList)dateAndDataList[nr], writer, textDepth + 1);
			}
		}

		writer.print(initialBlanks);
		writer.println("</datenfeld>");
	}

	/**
	 * Schreibt ein Element objektMenge (siehe K2S.DTD)
	 *
	 * @param objectSet Objekt, das alle Informationen eines Elements objektMenge (siehe K2S.DTD) enthält
	 * @param writer    Stream, mit dem das Objekt als XML Text gespeichert wird
	 * @param textDepth Einrückungstiefe des Textes
	 */
	private void writeObjectSet(final ConfigurationObjectSet objectSet, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);

		writer.print(initialBlanks);
		writer.print("<objektMenge");

		writeAttributeAndValueUncheckedValue(" name", objectSet.getName(), writer);
		writeAttributeAndValueCheckedValue(" verwaltung", objectSet.getManagementPid(), writer);
		writer.println(">");

		writeElements(objectSet.getElements(), writer, textDepth + 1);

		writer.print(initialBlanks);
		writer.println("</objektMenge>");
	}

	/**
	 * Speichert Elemente vom Typ element, siehe K2S.DTD.
	 *
	 * @param elementPids Array mit Pids. Jede Pid entspricht einem element (siehe K2S.DTD)
	 * @param writer      Stream, auf dem die Daten geschrieben werden
	 * @param textDepth   Einrückungstiefe
	 */
	private void writeElements(final String[] elementPids, final PrintWriter writer, final int textDepth) {
		final String initialBlanks = createEmptyString(textDepth);
		for(int nr = 0; nr < elementPids.length; nr++) {
			writer.print(initialBlanks);
			writer.print("<element");
			writeAttributeAndValueUncheckedValue(" pid", elementPids[nr], writer);
			writer.println("/>");
		}
	}
}

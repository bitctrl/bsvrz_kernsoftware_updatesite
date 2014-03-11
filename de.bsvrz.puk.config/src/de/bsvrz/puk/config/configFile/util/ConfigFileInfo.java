/*
 * Copyright 2013 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.util;

import de.bsvrz.dav.daf.util.HashBagMap;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileManager;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.puk.config.xmlFile.resolver.SilentK2SEntityResolver;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Diese Klasse bestimmt Informationen über einen Konfigurationsbereich anhand einer Config-Datei. Über die main()-Methode lässt sich das
 * Modul als Kommandozeilenprogramm nutzen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11678 $
 */
public class ConfigFileInfo {

	private static final Debug _debug = Debug.getLogger();

	/**
	 * ID der atgv.atg.konfigurationsBereichÜbernahmeInformationen.asp.eigenschaften
	 */
	private static final long VERSIONS_ID = 239615L;

	/**
	 * ID der atgv.atg.konfigurationsÄnderungen.asp.eigenschaften
	 */
	private static final long CHANGES_ID = 282967L;

	/**
	 * ID der atgv.atg.konfigurationsBereichAbhängigkeiten.asp.eigenschaften
	 */
	private static final long DEPENDENCIES_ID = 14636698789434546L;

	/**
	 * ID der atgv.atg.info.asp.eigenschaften
	 */
	private static final long INFO_ID = 239538L;

	private String _pid;
	private short _transferableVersion;
	private short _activatableVersion;
	private final List<ConfigAreaDependency> _dependencies;
	private final List<ChangeHistoryItem> _changeHistoryLog;
	private String _shortInfo = null;
	private String _description = null;

	public ConfigFileInfo(final File file) throws IOException, NoSuchVersionException {
		String pid = file.getName().split(".config$")[0];
		ConfigFileManager configFileManager = new ConfigFileManager();
		configFileManager.addAreaFile(pid, file.getParentFile(), (short) 1, new ArrayList<VersionInfo>());
		final ConfigurationAreaFile[] configurationAreas = configFileManager.getConfigurationAreas();
		final ConfigAreaFile areaFile = (ConfigAreaFile) configurationAreas[0];
		try {
			String headerpid = areaFile.getConfigAreaPid();
			if(!pid.equals(headerpid)) {
				System.err
						.println(
								"##### ACHTUNG ##### : Der Name der Datei " + file + " wurde falsch gebildet. Richtig wäre:  '" + headerpid + ".config'"
						);
			}
			_pid = areaFile.getConfigAreaPid();
			final SystemObjectInformationInterface informationInterface = configFileManager.getActiveObject(_pid);
			try {
				final byte[] configurationData = informationInterface.getConfigurationData(VERSIONS_ID);
				Deserializer deserializer = SerializingFactory.createDeserializer(new ByteArrayInputStream(configurationData));
				_activatableVersion = deserializer.readShort();
				_transferableVersion = deserializer.readShort();
			}
			catch(IllegalArgumentException e) {
				_debug.fine("Kann Versionen nicht lesen",e);
				_activatableVersion = -1;
				_transferableVersion = -1;
			}
			try {
				final byte[] configurationData = informationInterface.getConfigurationData(INFO_ID);

				Deserializer deserializer = SerializingFactory.createDeserializer(new ByteArrayInputStream(configurationData));
				_shortInfo = deserializer.readString(32767).trim();
				_shortInfo = readXmlString(_shortInfo);
				_description = deserializer.readString(32767);
				_description = readXmlString(_description);
			}
			catch(IllegalArgumentException e) {
				_debug.fine("Kann Info nicht lesen",e);
			}
			_dependencies = fetchDependencies(informationInterface);
			_changeHistoryLog = fetchHistoryItems(informationInterface);
		}
		finally {
			areaFile.close();
		}
	}


	/**
	 * Diese Methode liest aus dem SystemObjectInformationInterface die ÄnderungsHistorie. Identifiziert wird der Datensatz anhand der
	 * Konstanten CHANGES_ID.
	 *
	 * @param informationInterface Informationen die Dynamisches und Konfigurierendes Objekt gemeinsamm haben
	 * @return ÄnderungsHistorie
	 * @throws IOException falls beim lesen des Übergabeparametrs in Fehler auftritt
	 */
	private static List<ChangeHistoryItem> fetchHistoryItems(final SystemObjectInformationInterface informationInterface) throws IOException {
		final List<ChangeHistoryItem> changeHistoryLog = new ArrayList<ChangeHistoryItem>();
		try {
			final byte[] changesHistoryByteArray = informationInterface.getConfigurationData(CHANGES_ID);
			final Deserializer deserializer = SerializingFactory.createDeserializer(new ByteArrayInputStream(changesHistoryByteArray));
			int changesArrayCounter = deserializer.readInt();
			for(int i = 0; i < changesArrayCounter; i++) {
				final long timestamp = deserializer.readLong();
				final String author = deserializer.readString(32767);
				final short version = deserializer.readShort();
				final String reason = readXmlString(deserializer.readString(32767));
				final String text = readXmlString(deserializer.readString(32767));
				changeHistoryLog.add(new ChangeHistoryItem(timestamp, author, version, reason, text));
			}
		}
		catch(IllegalArgumentException ignored) {
			System.out.print("");
		}
		return changeHistoryLog;
	}

	/**
	 * Diese Methode liest aus dem SystemObjectInformationInterface die Abängigkeiten. Identifiziert wird der Datensatz anhand der Konstanten
	 * DEPENDENCIES_ID.
	 *
	 * @param informationInterface Informationen die Dynamisches und Konfigurierendes Objekt gemeinsamm haben
	 * @return Abhängigkeiten
	 * @throws IOException falls beim lesen des Übergabeparametrs in Fehler auftritt
	 */
	private static List<ConfigAreaDependency> fetchDependencies(final SystemObjectInformationInterface informationInterface) throws IOException {
		final List<ConfigAreaDependency> dependencies = new ArrayList<ConfigAreaDependency>();
		try {
			final byte[] dependenciesByteArray = informationInterface.getConfigurationData(DEPENDENCIES_ID);
			final Deserializer deserializer = SerializingFactory.createDeserializer(new ByteArrayInputStream(dependenciesByteArray));
			int dependenciesArrayCount = deserializer.readInt();
			for(int i = 0; i < dependenciesArrayCount; i++) {
				final short dependentVersion = deserializer.readShort();
				final String dependentPid = deserializer.readString(32767);
				final short neededVersion = deserializer.readShort();
				final byte type = deserializer.readByte();

				dependencies.add(new ConfigAreaDependency(dependentVersion, neededVersion, dependentPid, type));
			}
		}
		catch(IllegalArgumentException ignored) {
			System.out.print("");
		}
		return dependencies;
	}

	/**
	 * Entfernt XML-Tags und überflüssigen Whitespace aus dem übergebenem Text, wandelt XML-Entitäten um.
	 *
	 * @param stringToBeTrimmed zu formatierender String
	 * @return formatierter String
	 */
	public static String readXmlString(String stringToBeTrimmed) {
		// XML-Klasse braucht ein Root-Element, also Dummy hier einfügen.
		String str = "<x>" + stringToBeTrimmed + "</x>";

		try {
			SaxPullAdapter saxPullAdapter = new SaxPullAdapter(new SilentK2SEntityResolver());
			PullableEventStream stream = saxPullAdapter.start(
					new ByteArrayInputStream(str.getBytes(Charset.forName("UTF-8"))), null
			);
			StringBuilder stringBuilder = new StringBuilder();
			while(true) {
				Event event = stream.pullAnyEvent();
				if(event instanceof EndOfInputEvent) {
					break;
				}
				else if(event instanceof CharactersEvent) {
					String text = ((CharactersEvent) event).getText();
					String[] split = text.split("\n");
					for(String s : split) {
						String trim = s.trim();
						if(trim.length() > 0) {
							stringBuilder.append(trim);
							stringBuilder.append(" ");
						}
					}
				}
				else if(event instanceof StartElementEvent) {
					StartElementEvent elementEvent = (StartElementEvent) event;
					if(elementEvent.getLocalName().equals("absatz")) {
						trimChars(stringBuilder);
						stringBuilder.append('\n');
					}
					else if(elementEvent.getLocalName().equals("titel")) {
						trimChars(stringBuilder);
						stringBuilder.append('[');
					}
				}
				else if(event instanceof EndElementEvent) {
					EndElementEvent elementEvent = (EndElementEvent) event;
					if(elementEvent.getLocalName().equals("absatz")) {
						trimChars(stringBuilder);
						stringBuilder.append('\n');
					}
					else if(elementEvent.getLocalName().equals("titel")) {
						trimChars(stringBuilder);
						stringBuilder.append(']');
					}
				}
			}
			return stringBuilder.toString().trim();
		}
		catch(Exception ignored) {
			return str.trim();
		}
	}

	/**
	 * Entfernt Leerzeichen an Ende
	 * @param stringBuilder StringBuilder
	 */
	private static void trimChars(final StringBuilder stringBuilder) {
		while(stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == ' ') {
			stringBuilder.setLength(stringBuilder.length() - 1);
		}
	}

	@Override
	public String toString() {
		return _pid;
	}

	public String getShortInfo() {
		return _shortInfo;
	}

	public String getDescription() {
		return _description;
	}

	public short getTransferableVersion() throws IOException {
		return _transferableVersion;
	}

	public short getActivatableVersion() {
		return _activatableVersion;
	}

	public List<ConfigAreaDependency> getDependencies() {
		return Collections.unmodifiableList(_dependencies);
	}

	public List<ChangeHistoryItem> getChanges() {
		return Collections.unmodifiableList(_changeHistoryLog);
	}

	public String getPid() {
		return _pid;
	}

	public static void main(String[] args) {
		ArgumentList argumentList = new ArgumentList(args);
		boolean displayShortInfo = argumentList.fetchArgument("-kurzInfo=nein").booleanValue();
		boolean displayDescription = argumentList.fetchArgument("-beschreibung=nein").booleanValue();
		boolean displayTransferVersion = argumentList.fetchArgument("-uebernahmeVersion=nein").booleanValue()
				|| argumentList.fetchArgument("-übernahmeVersion=nein").booleanValue();
		boolean displayActivatableVersion = argumentList.fetchArgument("-aktivierungVersion=ja").booleanValue();
		boolean displayDependencies = argumentList.fetchArgument("-abhaengigkeiten=nein").booleanValue()
				|| argumentList.fetchArgument("-abhängigkeiten=nein").booleanValue();
		boolean displayChanges = argumentList.fetchArgument("-aenderungen=nein").booleanValue()
				|| argumentList.fetchArgument("-änderungen=nein").booleanValue();
		boolean dependencySummary = argumentList.fetchArgument("-zusammenfassung=ja").booleanValue();
		boolean displayAll = argumentList.fetchArgument("-alles=nein").booleanValue();
		if(displayAll) {
			dependencySummary = displayShortInfo = displayDescription = displayTransferVersion = displayActivatableVersion = displayDependencies = displayChanges = true;
		}
		boolean verbose = argumentList.fetchArgument("-ausführlich=nein").booleanValue()
				|| argumentList.fetchArgument("-ausfuehrlich=nein").booleanValue();
		int terminalWidth = argumentList.fetchArgument("-terminalBreite=80").intValueBetween(80, Integer.MAX_VALUE);

		final List<File> files = new ArrayList<File>();
		ArgumentList.Argument[] arguments = argumentList.fetchUnusedArguments();
		if(arguments != null) {
			for(ArgumentList.Argument argument : arguments) {
				String name = argument.getName();
				if(argument.hasValue()) {
					usage("Ungültiges Argument: " + name + "=" + argument.getValue());
				}
				File file = new File(name);
				if(!file.exists()) {
					usage("Datei nicht vorhanden: " + file.getAbsolutePath());
				}
				if(file.isDirectory()) {
					addRecursiveFiles(files, file);
				}
				else {
					files.add(file);
				}
			}
		}
		if(files.isEmpty()) {
			usage("Keine Dateien angegeben");
		}

		ConfigFileInfoWriter configFileInfoWriter = new ConfigFileInfoWriter(
				displayShortInfo, displayDescription, displayTransferVersion, displayActivatableVersion, displayDependencies,
				displayChanges,
				terminalWidth
		);

		if(verbose) {
			configFileInfoWriter.setVerboseMode(true);
		}

		Collections.sort(files);

		HashBagMap<String, Short> requiredAreas = new HashBagMap<String, Short>();
		HashBagMap<String, Short> optionalAreas = new HashBagMap<String, Short>();
		HashMap<String, Short> existingAreas = new HashMap<String, Short>();

		for(File file : files) {
			try {
				ConfigFileInfo configFileInfo = new ConfigFileInfo(file);
				configFileInfoWriter.writeInfo(configFileInfo);
				existingAreas.put(configFileInfo.getPid(), configFileInfo.getActivatableVersion());
				for(ConfigAreaDependency dependency : configFileInfo.getDependencies()) {
					if(dependency.getType().equals("notwendig")) {
						requiredAreas.add(dependency.getDependentPid(), dependency.getNeededVersion());
					}
					else {
						optionalAreas.add(dependency.getDependentPid(), dependency.getNeededVersion());
					}
				}
			}
			catch(Exception e) {
				System.err.println("Warnung: Datei nicht lesbar: " + file);
				e.printStackTrace();
			}
		}

		configFileInfoWriter.setVerboseMode(false);

		if(dependencySummary && files.size() > 1) {
			for(Map.Entry<String, Short> entry : existingAreas.entrySet()) {
				String pid = entry.getKey();
				Short existingVersion = entry.getValue();
				Collection<Short> requiredVersions = requiredAreas.get(pid);
				Collection<Short> optionalVersions = optionalAreas.get(pid);
				if(requiredVersions.size() != 0) {
					Short requiredVersion = Collections.max(requiredVersions);
					if(requiredVersion <= existingVersion) {
						requiredAreas.removeAll(pid);
					}
				}
				if(optionalVersions.size() != 0) {
					Short optionalVersion = Collections.max(optionalVersions);
					if(optionalVersion <= existingVersion) {
						optionalAreas.removeAll(pid);
					}
				}
			}
			for(Map.Entry<String, Collection<Short>> entry : requiredAreas.entrySet()) {
				String pid = entry.getKey();
				Short existingVersion = Collections.max(entry.getValue());
				Collection<Short> optionalVersions = optionalAreas.get(pid);
				if(optionalVersions.size() != 0) {
					Short optionalVersion = Collections.max(optionalVersions);
					if(optionalVersion <= existingVersion) {
						optionalAreas.removeAll(pid);
					}
				}
			}
			configFileInfoWriter.writeDependencySummary(hashBagMapToList(requiredAreas), hashBagMapToList(optionalAreas));
		}
	}

	private static List<ConfigAreaDependency> hashBagMapToList(final HashBagMap<String, Short> requiredAreas) {
		final List<ConfigAreaDependency> requiredDependencies = new ArrayList<ConfigAreaDependency>();
		for(Map.Entry<String, Collection<Short>> entry : requiredAreas.entrySet()) {
			requiredDependencies.add(new ConfigAreaDependency((short) -1, Collections.max(entry.getValue()), entry.getKey(), (byte) 1));
		}
		Collections.sort(
				requiredDependencies, new Comparator<ConfigAreaDependency>() {
			@Override
			public int compare(final ConfigAreaDependency o1, final ConfigAreaDependency o2) {
				return o1.getDependentPid().compareTo(o2.getDependentPid());
			}
		}
		);
		return requiredDependencies;
	}

	private static void addRecursiveFiles(final List<File> files, final File dir) {
		File[] tmp = dir.listFiles();
		if(tmp == null) {
			System.err.println("Warnung: Verzeichnis nicht lesbar: " + dir);
			return;
		}
		for(File file : tmp) {
			if(file.isFile()) {
				if(file.getName().toLowerCase().endsWith(".config")) {
					files.add(file);
				}
				else {
//					System.err.println("Warnung: Ignoriere Datei: " + file);
				}
			}
			else if(file.isDirectory()) {
				addRecursiveFiles(files, file);
			}
		}
	}

	private static void usage(final String... illegalArguments) {
		for(String illegalArgument : illegalArguments) {
			System.err.println(illegalArgument);
		}
		System.err.println();
		System.err.println("Verwendung:");
		System.err.print("java " + ConfigFileInfo.class.getCanonicalName() + " ");
		System.err.println("[Optionen]... [Konfigurationsdateien/Ordner]...");
		System.err.println("");
		System.err.println("Gültige Optionen:");
		System.err.println("\t-kurzInfo=ja              Kurzinfos ausgeben");
		System.err.println("\t-beschreibung=ja          Beschreibungen ausgeben");
		System.err.println("\t-übernahmeVersion=ja      Zur Übernahme freigegebene Version ausgeben");
		System.err.println("\t-aktivierungVersion=nein  Zur Aktivierung freigegebene Version nicht ausgeben");
		System.err.println("\t-abhängigkeiten=ja        Abhängigkeiten ausgeben");
		System.err.println("\t-änderungen=ja            Änderungsvermerke ausgeben");
		System.err.println(
				"\t-zusammenfassung=nein     Zusammenfassung über benötige Konfigurationsbereiche nicht ausgeben " +
						"(nur bei mehreren Dateien)"
		);
		System.err.println("\t-alles=ja                 Alles oben genannte ausgeben");
		System.err.println("\t-terminalBreite=80        Breite der Ausgabe anpassen");
		System.err.println("\t-ausführlich=ja           Ausführliche Ausgabe der Abhängigkeiten und Änderungsvermerke");
		System.err.println("(Umlaute können bei Bedarf durch ae, oe, ue ersetzt werden)");
		System.exit(1);
	}
}

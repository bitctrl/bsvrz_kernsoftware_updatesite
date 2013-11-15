/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.main.managementfile;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.filelock.FileLock;
import de.bsvrz.sys.funclib.xmlSupport.CountingErrorHandler;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Diese Klasse speichert die Verwaltungsdaten der Konfiguration in einer XML-Datei ab. (siehe auch die Technischen Anforderungen des Segments Parametrierung
 * und Konfiguration)
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ManagementFile implements ConfigurationManagementFile {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Repr�sentiert die vollst�ndige XML-Datei. */
	private final Document _xmlDocument;

	/** Datei, wo die Verwaltungsdaten abgespeichert werden sollen. */
	private final File _xmlFile;

	/** Speichert die Eintr�ge zu den Verwaltungsdaten in dieser Liste. */
	private final List<ConfigurationAreaManagementInfo> _areaManagementInfos;

	/** Das Format des Zeitstempels f�r die Versionsaktivierung. */
	private DateFormat _dateFormat;

	/** Speichert die Basis der Verzeichnisse f�r die Konfigurationsbereiche. */
	private URI _uriBase;

	private final FileLock _managementFileLock;

	public File getForeignObjectCacheFile() {
		final File foreignObjectCacheFile = new File(_objectSetBaseDirectory, "fremdObjekte-" + getConfigurationAuthority());
		return foreignObjectCacheFile;
	}

	private File _objectSetBaseDirectory;

	/**
	 * Der Konstruktor l�dt die angegebene XML-Datei mit den Verwaltungsdaten der Konfiguration, validiert gegen die dazugeh�rende DTD bzw. erzeugt eine neue
	 * Datei.
	 *
	 * @param xmlFile die Verwaltungsdatei
	 */
	public ManagementFile(File xmlFile) {
		if(xmlFile == null) throw new IllegalArgumentException("Es wurde kein File-Objekt angegeben.");

		try {
			_xmlFile = xmlFile.getCanonicalFile();
		}
		catch(IOException e) {
			throw new IllegalArgumentException(e);
		}

		// Pr�fen, ob sich die Datei bereits im Zugriff befindet, wenn nicht wird diese f�r andere gesperrt
		_managementFileLock = new FileLock(_xmlFile);
		try {
			_managementFileLock.lock();
		}
		catch(IOException e) {
			final String errorMessage = "Die lock-Datei f�r die Datei " + _xmlFile.getAbsolutePath() + " konnte nicht erzeugt werden";
			e.printStackTrace();
			_debug.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}

		try {
			// Verwaltungsdaten einladen!
			final CountingErrorHandler errorHandler = new CountingErrorHandler();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			// die Validierung der XML-Datei anhand der DTD durchf�hren
			factory.setValidating(true);
			factory.setAttribute("http://xml.org/sax/features/validation", Boolean.TRUE);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				if(_xmlFile.exists()) {
					if(!_xmlFile.isFile()) {
						throw new IllegalArgumentException("Das �bergebene File-Objekt ist keine Datei: " + _xmlFile.toString());
					}
					_debug.config("Datei wird eingelesen", _xmlFile);
					try {
						builder.setErrorHandler(errorHandler);
						builder.setEntityResolver(new ManagementEntityResolver());
						_xmlDocument = builder.parse(_xmlFile);	// evtl. mittels BufferedInputStream cachen
						errorHandler.printSummary();
						if(errorHandler.getErrorCount() > 0) {
							throw new IllegalStateException(errorHandler.getErrorCount() + " Fehler beim Parsen der XML-Datei " + _xmlFile.toString());
						}
					}
					catch(Exception ex) {
						final String errorMessage = "Die Verwaltungdaten der Konfiguration konnten nicht eingelesen werden: " + _xmlFile.toString();
						_debug.error(errorMessage, ex);
						throw new RuntimeException(errorMessage, ex);
					}
				}
				else {
					_xmlDocument = builder.getDOMImplementation().createDocument(null, "verwaltungsdaten", null);
					save(new BufferedOutputStream(new FileOutputStream(_xmlFile)));
				}
				_objectSetBaseDirectory = _xmlFile.getParentFile();
				_uriBase = _objectSetBaseDirectory.toURI();
				_debug.config("Verzeichnisbasis f�r die Konfigurationsbereiche", _uriBase.toString());
			}
			catch(ParserConfigurationException ex) {
				final String errorMessage = "Die Verwaltungdaten der Konfiguration konnten nicht eingelesen werden: " + _xmlFile.toString();
				_debug.error(errorMessage);
				throw new RuntimeException(errorMessage, ex);
			}
			catch(IOException ex) {
				final String errorMessage = "Es konnte keine neue Verwaltungsdatei erstellt werden";
				_debug.error(errorMessage, ex);
				throw new RuntimeException(errorMessage, ex);
			}

			// Konfigurationsbereiche eingelesen
			_areaManagementInfos = getAllManagementInfos();

			_debug.config("Verwaltungsdaten der Konfiguration wurden vollst�ndig eingelesen.");
		}
		catch(RuntimeException e) {
			// Falls im Konstruktor nach dem Lock der Verwaltungsdatei eine Exception auftritt, dann wird ein Unlock ausgef�hrt und die Exception weitergegeben.
			_managementFileLock.unlock();
			throw e;
		}
	}

	/**
	 * Interne Methode, die die Verwaltungseintr�ge zu den Konfigurationsbereichen einl�dt und gesammelt in einer Liste zur�ckgibt.
	 *
	 * @return eine Liste mit den Verwaltungseintr�gen der Konfigurationsbereiche
	 */
	private List<ConfigurationAreaManagementInfo> getAllManagementInfos() {
		List<ConfigurationAreaManagementInfo> resultList = new LinkedList<ConfigurationAreaManagementInfo>();
		synchronized(_xmlDocument) {
			Element xmlRoot = _xmlDocument.getDocumentElement();
			NodeList entryList = xmlRoot.getElementsByTagName("konfigurationsbereich");
			for(int i = 0; i < entryList.getLength(); i++) {
				Element element = (Element)entryList.item(i);
				ConfigAreaManagementInfo info = new ConfigAreaManagementInfo(element.getAttribute("pid"), false);
				// Versionen einlesen
				NodeList versionList = element.getElementsByTagName("version");
				for(int j = 0; j < versionList.getLength(); j++) {
					Element version = (Element)versionList.item(j);
					int nr = Integer.parseInt(version.getAttribute("nr"));
					String time = version.getAttribute("zeitpunkt");
					if(time.equals("")) {
						// diese Version wird aktiviert und erh�lt die aktuelle Uhrzeit als Zeitstempel
						if(_dateFormat == null) {
							_dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						}
						time = _dateFormat.format(new Date(System.currentTimeMillis()));
						version.setAttribute("zeitpunkt", time);
						info.setNewVersionActivated(true);
					}
					final VersionInformation versionInformation = new VersionInformation((short)nr, time);
					// letzte Version ist die aktive Version
					if(j == versionList.getLength() - 1) info.setActiveVersion(versionInformation);
					info.addVersionInfo(versionInformation);
				}
				resultList.add(info);
			}
		}
		return resultList;
	}

	/**
	 * Gibt die Pid des Konfigurationsverantwortlichen der Konfiguration zur�ck.
	 *
	 * @return die Pid des Konfigurationsverantwortlichen der Konfiguration
	 */
	public String getConfigurationAuthority() {
		synchronized(_xmlDocument) {
			Element xmlRoot = _xmlDocument.getDocumentElement();
			NodeList authorityList = xmlRoot.getElementsByTagName("konfigurationsverantwortlicher");
			if(authorityList.getLength() > 0) {
				Element element = (Element)authorityList.item(0);
				return element.getAttribute("pid");
			}
			else {
				return "";
			}
		}
	}

	/**
	 * Speichert die Pid des Konfigurationsverantwortlichen der Konfiguration ab.
	 *
	 * @param pid die Pid des Konfigurationsverantwortlichen
	 */
	public void setConfigurationAuthority(String pid) {
		synchronized(_xmlDocument) {
			Element xmlRoot = _xmlDocument.getDocumentElement();
			NodeList authorityList = xmlRoot.getElementsByTagName("konfigurationsverantwortlicher");
			if(authorityList.getLength() > 0) {
				// es gibt einen Eintrag
				Element element = (Element)authorityList.item(0);
				element.setAttribute("pid", pid);
			}
			else {
				// ein neuer Eintrag muss hinzugef�gt werden
				Element element = _xmlDocument.createElement("konfigurationsverantwortlicher");
				element.setAttribute("pid", pid);
				_xmlDocument.getDocumentElement().appendChild(element);
			}
		}
	}

	/**
	 * Gibt alle Eintr�ge �ber Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge aus den Verwaltungsdaten zur�ck.
	 *
	 * @return alle Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge
	 */
	public List<ConfigurationAreaManagementInfo> getAllConfigurationAreaManagementInfos() {
		return _areaManagementInfos;
	}

	/**
	 * Gibt einen Eintrag aus den Verwaltungsdaten zu der angegebenen Pid eines Konfigurationsbereichs zur�ck. Falls es keinen Eintrag gibt wird <code>null</code>
	 * zur�ckgegeben.
	 *
	 * @param configurationAreaPid Pid eines Konfigurationsbereichs
	 *
	 * @return Eintrag aus den Verwaltungsdaten zu einem Konfigurationsbereich oder <code>null</code>, falls kein Eintrag vorhanden ist
	 */
	public ConfigurationAreaManagementInfo getConfigurationAreaManagementInfo(String configurationAreaPid) {
		synchronized(_areaManagementInfos) {
			for(ConfigurationAreaManagementInfo configurationAreaManagementInfo : _areaManagementInfos) {
				if(configurationAreaManagementInfo.getPid().equals(configurationAreaPid)) {
					return configurationAreaManagementInfo;
				}
			}
		}
		return null;	// kein Eintrag vorhanden
	}

	/**
	 * F�gt einen neuen Eintrag eines Konfigurationsbereichs ans Ende der Verwaltungsdatei ein.
	 *
	 * @param pid Pid des Konfigurationsbereichs, welcher zu den Verwaltungsdaten hinzugef�gt werden soll
	 *
	 * @return der Eintrag in den Verwaltungsdaten zu einem Konfigurationsbereich
	 */
	public ConfigurationAreaManagementInfo addConfigurationAreaManagementInfo(String pid) {
		// Objekt erzeugen, in XML einf�gen und in die Liste eintragen
		// erst pr�fen, ob zu dieser Pid bereits ein Eintrag vorhanden ist
		synchronized(_areaManagementInfos) {
			for(ConfigurationAreaManagementInfo configurationAreaManagementInfo : _areaManagementInfos) {
				if(configurationAreaManagementInfo.getPid().equals(pid)) {
					throw new IllegalArgumentException("Zu dieser Pid '" + pid + "' gibt es bereits einen Eintrag in den Verwaltungsdaten.");
				}
			}
		}
		final ConfigurationAreaManagementInfo info = new ConfigAreaManagementInfo(pid, true);
		synchronized(_areaManagementInfos) {
			_areaManagementInfos.add(info);
		}
		return info;
	}

	/** Speichert die Verwaltungsdaten ab. */
	public void save() throws IOException {
		synchronized(_xmlFile){
			// Verwaltungsdaten in einer tempor�ren Datei speichern
			final File parentFile = _xmlFile.getParentFile();
			File newFile = File.createTempFile("verwaltungsdaten", ".xml", parentFile);
			try {
				save(new BufferedOutputStream(new FileOutputStream(newFile)));
			}
			catch(IOException ex) {
				_debug.error("Fehler beim Speichern der Verwaltungsdaten in der tempor�ren Datei", newFile);
				newFile.delete();
				throw new IOException("Fehler beim Speichern der Verwaltungsdaten " + ex.getMessage());
			}
			// Backup der bisherigen Datei erstellen
			File backupFile = new File(parentFile, _xmlFile.getName() + ".old");
			// l�scht eine evtl. bestehende alte Datei
			if(backupFile.exists() && !backupFile.delete()) {
				_debug.warning("Alte Backup-Datei " + backupFile.getAbsolutePath() + " konnte nicht gel�scht werden.");
			};
			if(!_xmlFile.exists()) {
				_debug.warning("Bisherige Verwaltungsdatei " + _xmlFile + " existiert nicht");
			};
			if(!_xmlFile.exists() || _xmlFile.renameTo(backupFile)) {
				// tempor�re Datei umbenennen zur Verwaltungsdatei
				if(!newFile.renameTo(_xmlFile)) {
					final String errorMessage = "Tempor�re Verwaltungsdatei konnte nicht in " + _xmlFile.getName() + " umbenannt werden.";
					_debug.error(errorMessage);
					newFile.delete();
					throw new IOException(errorMessage);
				}
				else {
					if(newFile.exists()) {
						_debug.warning("Datei " + newFile + " existiert immer noch, obwohl sie eigentlich umbenannt wurde.");
					}
				}
			}
			else {
				final String errorMessage = "Beim Sichern der Verwaltungsdaten konnte das Backup der bisherigen Verwaltungsdatei nicht angelegt werden.";
				_debug.error(errorMessage);
				newFile.delete();
				throw new IOException(errorMessage);
			}
		}
	}

	/**
	 * Sichert die Verwaltungsdatei in das angegebene Zielverzeichnis
	 *
	 * @param targetDirectory Zielverzeichnis
	 *
	 * @throws IOException IO-Fehler
	 */
	public void createBackupFile(File targetDirectory) throws IOException {
		synchronized(_xmlFile){
			final String fileName = _xmlFile.getName();
			save();

			// Datei kopieren
			final FileOutputStream fileOutputStream = new FileOutputStream(new File(targetDirectory, fileName));
			try {
				final FileInputStream inputStream = new FileInputStream(_xmlFile);
				try {

					byte[] buf = new byte[1024];
					int len;
					while((len = inputStream.read(buf)) > 0) {
						fileOutputStream.write(buf, 0, len);
					}
				}
				finally {
					inputStream.close();
				}
			}
			finally {
				fileOutputStream.close();
			}
		}
	}

	public void close() throws IOException {
		try {
			save();
		}
		finally {
			// lock-Datei wieder freigeben
			_managementFileLock.unlock();
		}
	}

	public File getObjectSetDirectory() {
		final File objectSetDirectory = new File(_objectSetBaseDirectory, "mengen-" + getConfigurationAuthority());
		if(!objectSetDirectory.isDirectory()) {
			if(!objectSetDirectory.mkdir()) {
				throw new RuntimeException("Verzeichnis f�r die Speicherung von dynamischen Mengen konnte nicht erzeugt werden: " + objectSetDirectory.getPath());
			}
		}
		return objectSetDirectory;
	}

	/**
	 * String-Repr�sentation (Verzeichnis und Dateiname der Verwaltungsdatei) dieses Objekts.
	 *
	 * @return String-Repr�sentation (Verzeichnis und Dateiname der Verwaltungsdatei) dieses Objekts.
	 */
	public String toString() {
		return _xmlFile.toString();
	}

	/**
	 * Speichert die Verwaltungsdaten im angegebenen Datenstrom ab.
	 *
	 * @param outputStream der Stream, in dem die Verwaltungsdaten abgespeichert werden sollen
	 *
	 * @throws IOException Falls ein Fehler beim Speichern der Verwaltungsdaten aufgetreten ist.
	 */
	private void save(OutputStream outputStream) throws IOException {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); // ISO-Kodierung f�r westeurop�ische Sprachen
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");	   // DTD ist in einer separaten Datei

			synchronized(_xmlDocument) {
				// DOCTYPE bestimmen
				final DocumentType documentType = _xmlDocument.getDoctype();
				String publicID = null;
				String systemID = null;
				if(documentType != null) {
					publicID = _xmlDocument.getDoctype().getPublicId();
					systemID = _xmlDocument.getDoctype().getSystemId();
				}
				if(publicID != null) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicID);
				}
				else {
					// DOCTYPE PUBLIC_ID ist nicht vorhanden -> erstellen
					transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//K2S//DTD Verwaltung//DE");
				}
				if(systemID != null) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemID);
				}
				else {
					// DOCTYPE SYSTEM_ID ist nicht vorhanden -> erstellen
					transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "verwaltungsdaten.dtd");
				}

				DOMSource source = new DOMSource(_xmlDocument);
	//			StreamResult result = new StreamResult(System.out);  // System.out -> gibt die XML-Struktur in einer Shell aus.
				StreamResult result = new StreamResult(outputStream);	// gibt die XML-Struktur in einem Stream (Datei) aus
				transformer.transform(source, result);
			}
			outputStream.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Ein Fehler beim Speichern der Verwaltungsdaten ist aufgetreten";
			_debug.error(errorMessage, ex);
			throw new IOException(errorMessage);
		}
	}

	/** Diese Implementierung ist f�r die Konfigurationsbereiche der Verwaltungsdaten zust�ndig. */
	private class ConfigAreaManagementInfo implements ConfigurationAreaManagementInfo {

		/** Die Pid des Konfigurationsbereichs ist unver�nderlich und wird hier gespeichert. */
		private String _pid;

		/** Enth�lt alle Versionseintr�ge eines Konfigurationsbereichs. */
		private List<VersionInfo> _versionInfos;

		/** Gibt an, ob eine neue Version aktiviert wurde, oder nicht. */
		private boolean _newVersionActivated = false;

		/** Die aktuelle und aktivierte Version des Konfigurationsbereichs. */
		private VersionInfo _versionInfo = new VersionInformation((short)0, "");

		/**
		 * Konstruktor erstellt einen Eintrag in der XML-Datenstruktur f�r einen Konfigurationsbereich.
		 *
		 * @param pid    die Pid des Konfigurationsbereichs
		 * @param create gibt an, ob ein Eintrag in der XML-Datenstruktur hinzugef�gt werden soll
		 */
		private ConfigAreaManagementInfo(String pid, boolean create) {
			_versionInfos = new LinkedList<VersionInfo>();
			_pid = pid;
			if(create) {
				// neuer Eintrag in xml
				synchronized(_xmlDocument) {
					Element xmlRoot = _xmlDocument.getDocumentElement();
					Element element = _xmlDocument.createElement("konfigurationsbereich");
					element.setAttribute("pid", pid);
					final URI xmlDir = _xmlFile.getParentFile().toURI();
					URI uri = relativize(_uriBase, xmlDir);
					element.setAttribute("verzeichnis", uri.toString());
					xmlRoot.appendChild(element);
				}
			}
		}

		/**
		 * Gibt die Pid des Konfigurationsbereichs eines Verwaltungseintrages zur�ck.
		 *
		 * @return die Pid des Konfigurationsbereichs
		 */
		public String getPid() {
			return _pid;
		}

		/**
		 * Gibt den Speicherort (Verzeichnis) der Datei des Konfigurationsbereichs zur�ck.
		 *
		 * @return der Speicherort (Verzeichnis) der Konfigurationsbereichsdatei
		 */
		public File getDirectory() {
			synchronized(_xmlDocument) {
				Element xmlRoot = _xmlDocument.getDocumentElement();
				NodeList configurationAreaList = xmlRoot.getElementsByTagName("konfigurationsbereich");
				for(int i = 0; i < configurationAreaList.getLength(); i++) {
					Element element = (Element)configurationAreaList.item(i);
					if(element.getAttribute("pid").equals(_pid)) {
						// passenden Eintrag gefunden
						try {
							URI uri = new URI(element.getAttribute("verzeichnis"));
							if(!uri.isAbsolute()) {
								uri = _uriBase.resolve(uri);
							}
							File file = new File(uri);
							if(file.isDirectory()) {
								return file;
							}
							else {
								throw new IllegalStateException("Das ausgelesene Verzeichnis ist keines oder existiert nicht: " + file.toString());
							}
						}
						catch(URISyntaxException ex) {
							String errorMessage = "Speicherort des Konfigurationsbereichs '" + _pid + "' kann nicht ermittelt werden";
							_debug.warning(errorMessage, ex);
							throw new IllegalStateException(errorMessage, ex);
						}
					}
				}
			}
			throw new IllegalStateException("Der Konfigurationsbereich mit dieser Pid '" + _pid + "' ist nicht in den Verwaltungsdaten vorhanden.");
		}

		/**
		 * Speichert den Speicherort (Verzeichnis) der Datei des Konfigurationsbereichs. Stimmt ein Teil des angegebenen Verzeichnisses mit dem vollst�ndigen
		 * Verzeichnis der Verwaltungsdatei �berein, so wird die Pfadangabe relativ zum Verzeichnis der Verwaltungsdatei abgespeichert.
		 *
		 * @param directory der Speicherort (Verzeichnis) der Konfigurationsbereichsdatei
		 */
		public void setDirectory(File directory) {
			synchronized(_xmlDocument) {
				Element xmlRoot = _xmlDocument.getDocumentElement();
				NodeList configurationAreaList = xmlRoot.getElementsByTagName("konfigurationsbereich");
				for(int i = 0; i < configurationAreaList.getLength(); i++) {
					Element element = (Element)configurationAreaList.item(i);
					if(element.getAttribute("pid").equals(_pid)) {
						// passenden Eintrag gefunden
						if(directory.isDirectory()) {
							URI uri = relativize(_uriBase, directory.toURI());
							element.setAttribute("verzeichnis", uri.toString());
						}
						else {
							throw new IllegalArgumentException("Der angegebene Speicherort ist kein Verzeichnis oder existiert nicht: " + directory.toString());
						}
						break;
					}
				}
			}
		}

		public void setNextActiveVersion(short nextActiveVersion) {
			if(nextActiveVersion < 1) {
				throw new IllegalArgumentException("Die n�chste zu aktivierende Version muss gr��er gleich 1 sein.");
			}
			synchronized(_xmlDocument) {
				Element xmlRoot = _xmlDocument.getDocumentElement();
				NodeList configList = xmlRoot.getElementsByTagName("konfigurationsbereich");
				for(int i = 0; i < configList.getLength(); i++) {
					Element element = (Element)configList.item(i);
					if(element.getAttribute("pid").equals(_pid)) {
						// richtigen Konfigurationsbereich gefunden
						NodeList versionList = element.getElementsByTagName("version");
						// pr�fen, ob die Versionsnummer gr��er ist, als die bisher aktivierte
						if(versionList.getLength() > 0) {
							// es gibt mindestens einen Versionseintrag
							Element version = (Element)versionList.item(versionList.getLength() - 1);
							short nr = Short.parseShort(version.getAttribute("nr"));
							String time = version.getAttribute("zeitpunkt");
							// wurde die letzte Versionsnummer bereits aktiviert?
							if(time.equals("")) {
								// letzte Versionsnummer wurde noch nicht aktiviert
								// gibt es einen Wert davor?
								if(versionList.getLength() > 1) {
									Element preVersion = (Element)versionList.item(versionList.getLength() - 2);
									short preNr = Short.parseShort(preVersion.getAttribute("nr"));
									if(nextActiveVersion > preNr) {
										// �ndere die geschriebene Versionsnummer
										version.setAttribute("nr", String.valueOf(nextActiveVersion));
									}
									else {
										// Exception
										final StringBuilder errorMessage = new StringBuilder();
										errorMessage.append("Die n�chste zu aktivierende Version ").append(nextActiveVersion);
										errorMessage.append(" ist kleiner oder gleich der aktiven Version ").append(preNr);
										_debug.error(errorMessage.toString());
										throw new IllegalArgumentException(errorMessage.toString());
									}
								}
								else {
									version.setAttribute("nr", String.valueOf(nextActiveVersion));
								}
							}
							else {
								// letzte Versionsnummer wurde bereits aktiviert -> neuen Eintrag erstellen
								if(nextActiveVersion > nr) {
									// neuen Eintrag erstellen
									createNextVersion((Node)element, nextActiveVersion);
								}
								else {
									// Exception
									final StringBuilder errorMessage = new StringBuilder();
									errorMessage.append("Die n�chste zu aktivierende Version ").append(nextActiveVersion);
									errorMessage.append(" ist kleiner oder gleich der aktiven Version ").append(nr);
									_debug.error(errorMessage.toString());
									throw new IllegalArgumentException(errorMessage.toString());
								}
							}
						}
						else {
							// es gibt noch keinen Versionseintrag -> neuen Eintrag erstellen
							createNextVersion((Node)element, nextActiveVersion);
						}
					}
				}
			}
		}

		/**
		 * Erstellt einen neuen Versionseintrag in der Verwaltungsdatei.
		 *
		 * @param node    KonfigurationsBereichs-Knoten, an dem der Versionseintrag gespeichert werden soll
		 * @param version zu speichernde Versionsnummer
		 */
		private void createNextVersion(Node node, short version) {
			// neuen Versionsknoten anlegen
			Element nextVersion = _xmlDocument.createElement("version");
			nextVersion.setAttribute("nr", String.valueOf(version));

			// Knoten hinzuf�gen
			node.appendChild(nextVersion);
		}

		public boolean isNewVersionActivated() {
			return _newVersionActivated;
		}

		/**
		 * Setzt den Parameter, ob dieser Konfigurationsbereich in eine neue Version �berf�hrt wurde.
		 *
		 * @param newVersionActivated ob dieser Konfigurationsbereich in eine neue Version �berf�hrt wurde
		 */
		private void setNewVersionActivated(boolean newVersionActivated) {
			_newVersionActivated = newVersionActivated;
		}

		/**
		 * Gibt die aktive Version des Konfigurationsbereichs und ihren Aktivierungszeitpunkt zur�ck.
		 *
		 * @return die aktive Version und ihren Aktivierungszeitpunkt des Konfigurationsbereichs
		 */
		public VersionInfo getActiveVersion() {
			return _versionInfo;
		}

		/**
		 * Interne Methode, die die aktive Version des Konfigurationsbereichs am Verwaltungseintrag speichert.
		 *
		 * @param versionInfo der Versionseintrag mit der aktiven Version
		 */
		private void setActiveVersion(VersionInfo versionInfo) {
			_versionInfo = versionInfo;
		}

		/**
		 * Gibt alle Versionseintr�ge zu diesem Konfigurationsbereich in einer Liste zur�ck.
		 *
		 * @return eine Liste aller Versionseintr�ge zu diesem Konfigurationsbereich
		 */
		public List<VersionInfo> getVersions() {
			return _versionInfos;
		}

		/**
		 * Gibt die Position innerhalb aller Konfigurationsbereiche in den Verwaltungsdaten zur�ck. (siehe auch TPuK1-99 Reihenfolge der Bereiche)
		 *
		 * @return Position innerhalb der Konfigurationsbereiche
		 */
		public int getPosition() {
			synchronized(_xmlDocument) {
				Element xmlRoot = _xmlDocument.getDocumentElement();
				NodeList nodeList = xmlRoot.getElementsByTagName("konfigurationsbereich");
				for(int i = 0; i < nodeList.getLength(); i++) {
					Element element = (Element)nodeList.item(i);
					if(element.getAttribute("pid").equals(_pid)) return i + 1;
				}
			}
			throw new IllegalStateException("Der Konfigurationsbereich mit dieser Pid '" + _pid + "' ist nicht in den Verwaltungsdaten vorhanden.");
		}

		/**
		 * Setzt die Position innerhalb der Reihenfolge der Konfigurationsbereiche. (siehe auch TPuK1-99 Reihenfolge der Bereiche)
		 *
		 * @param position Position innerhalb der Konfigurationsbereiche
		 */
		public void setPosition(int position) {
			if(position <= 0) {
				throw new IllegalArgumentException("Ung�ltige Position! Die erste Position ist die 1.");
			}
			// Eintrag in der xml-Datei verschieben! -> passenden Eintrag finden und woanders hin kopieren
			synchronized(_xmlDocument) {
				Element xmlRoot = _xmlDocument.getDocumentElement();
				// erst das Element rausl�schen
				NodeList configurationAreaList = xmlRoot.getElementsByTagName("konfigurationsbereich");
				if(position > configurationAreaList.getLength()) {
					throw new IllegalArgumentException("Ung�ltige Position! Es gibt nur " + configurationAreaList.getLength() + " Eintr�ge.");
				}

				Node nodeToMove = null;
				for(int i = 0; i < configurationAreaList.getLength(); i++) {
					Element element = (Element)configurationAreaList.item(i);
					if(element.getAttribute("pid").equals(_pid)) {
						nodeToMove = configurationAreaList.item(i);
						break;
					}
				}
				if(nodeToMove != null) {
					xmlRoot.removeChild(nodeToMove);
					// dann das Element einf�gen
					NodeList newList = xmlRoot.getElementsByTagName("konfigurationsbereich");
					xmlRoot.insertBefore(nodeToMove, newList.item(position - 1));
				}
			}
		}

		/**
		 * Interne Methode, die die Versionseintr�ge einliest und in einer Liste speichert.
		 *
		 * @param versionInfo ein Versionseintrag
		 */
		private void addVersionInfo(VersionInfo versionInfo) {
			_versionInfos.add(versionInfo);
		}
	}

	/**
	 * Erstellt eine URI, die m�glichst einen Pfad von uri relativ zu dir darstellt. Verh�lt sich �hnlich zu {@link URI#relativize(java.net.URI, java.net.URI)}.
	 * Workaround wegen <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6226081">Bug 6226081</a>.
	 *
	 * @param dir Pfad, zu dem uri relativ dargestellt werden soll
	 * @param uri Ort der URI
	 *
	 * @return eine URI, die mit dem Parameter dir kombiniert wieder die Ursprungs-URI ergibt.
	 * @see URI#relativize(java.net.URI, java.net.URI)
	 */
	static URI relativize(final URI dir, final URI uri) {
		if(dir.getScheme() != null && !dir.getScheme().equals(uri.getScheme())) return uri;
		final String dirStr = dir.normalize().getPath();
		final String uriStr = uri.normalize().getPath();

		try {
			return new URI(relativizePath(dirStr, uriStr));
		}
		catch(URISyntaxException ignored) {
			return uri;
		}
	}

	private static String relativizePath(final String dirStr, final String uriStr) {
		// Pfad an Schr�gstrichen auftrennen, mehrere aufeinander folgende werden zusammengefasst. (/bla////foo ist gleich /bla/foo)
		final String[] dir = dirStr.split("/+", -1);
		final String[] uri = uriStr.split("/+", -1);

		int commonIndex = 0;
		for(int i = 0; i < uri.length && i < dir.length; i++) {
			if(!uri[i].equals(dir[i])) {
				break;
			}
			commonIndex++;
		}

		if(commonIndex <= 1) {
			// Nicht bis zum root zur�ckwandern, das w�re sinnlos. Stattdessen absoluten Pfad zur�ckgeben.
			return uriStr;
		}

		final StringBuilder result = new StringBuilder(uriStr.length());

		// Von dir r�ckw�rts wandern, bis an gemeinsamen Pfad angelangt.
		for(int i = dir.length - 1; i > commonIndex; i--) {
			result.append("../");
		}
		// und jetzt in Richtung uri wandern
		for(int i = commonIndex; i < uri.length; i++) {
			result.append(uri[i]).append('/');
		}
		if(result.length() == 0) {
			return "";
		}
		result.setLength(result.length() - 1);
		return result.toString();
	}

	/** Repr�sentiert einen Versionseintrag in den Verwaltungsdaten zu einem Konfigurationsbereich. */
	private class VersionInformation implements VersionInfo {

		/** Versionsnummer */
		private short _version = 0;

		/** Aktivierungszeit dieses Versionseintrages */
		private String _activationTime = "";

		public VersionInformation(short version, String activationTime) {
			_version = version;
			_activationTime = activationTime;
		}

		/**
		 * Gibt die Versionsnummer dieses Versionseintrages zur�ck.
		 *
		 * @return die Versionsnummer
		 */
		public short getVersion() {
			return _version;
		}

		/**
		 * Gibt den Zeitpunkt der Aktivierung dieser Version zur�ck.
		 *
		 * @return den Aktivierungszeitpunkt
		 */
		public long getActivationTime() {
			try {
				if(_dateFormat == null) _dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
				Date date = _dateFormat.parse(_activationTime);
				return date.getTime();
			}
			catch(ParseException ex) {
				final String errorMessage =
						"Die Aktivierungszeit " + _activationTime + " konnte nicht eingelesen werden. Folgendes Format verwenden 'dd.MM.yyyy HH:mm:ss,SSS'.";
				_debug.error(errorMessage);
				throw new IllegalStateException(errorMessage, ex);
			}
		}
	}

	/**
	 * Implementierung eines EntityResolvers, der Referenzen auf den Public-Identifier "-//K2S//DTD Verwaltung//DE" ersetzt durch die verwaltungsdaten.dtd
	 * Resource-Datei in diesem Package.
	 */
	private class ManagementEntityResolver implements EntityResolver {

		/**
		 * L�st Referenzen auf external entities wie z.B. DTD-Dateien auf.
		 * <p/>
		 * Angegebene Dateien werden, falls sie im Suchverzeichnis gefunden werden, von dort geladen. Ansonsten wird der normale Mechanismus zum Laden von externen
		 * Entities benutzt.
		 *
		 * @param publicId Der public identifier der externen Entity oder null falls dieser nicht verf�gbar ist.
		 * @param systemId Der system identifier aus dem XML-Dokument.
		 *
		 * @return F�r Referenzen im Suchverzeichnis wird ein InputSource-Objekt, das mit der entsprechenden Datei im Suchverzeichnis verbunden ist, zur�ckgegeben.
		 *
		 * @throws SAXException Bei Fehlern beim Zugriff auf externe Entities.
		 * @throws IOException
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if(publicId != null && publicId.equals("-//K2S//DTD Verwaltung//DE")) {
				URL url = this.getClass().getResource("verwaltungsdaten.dtd");
				assert url != null : this.getClass();
				return new InputSource(url.toExternalForm());
			}
			return null;
		}
	}
}

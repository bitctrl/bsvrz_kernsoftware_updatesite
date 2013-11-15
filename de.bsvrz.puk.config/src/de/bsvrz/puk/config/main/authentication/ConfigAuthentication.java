/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.authentication;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.crypt.EncryptDecryptProcedure;
import de.bsvrz.sys.funclib.crypt.decrypt.DecryptFactory;
import de.bsvrz.sys.funclib.crypt.encrypt.EncryptFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.xmlSupport.CountingErrorHandler;
import de.bsvrz.sys.funclib.filelock.FileLock;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Diese Klasse stellt alle Methoden zur Verf�gung, um die Benutzer eines Datenverteilers eindeutig zu identifizieren. Es werden weitere Methoden zur Verf�gung
 * gestellt, um die Benutzer zu verwalten (anlegen neuer Benutzer, Passw�rter �ndern, usw.).
 * <p/>
 * Die Klasse verwaltet selbstst�ndig die Datei, in der die Benutzer mit ihrem Passw�rtern (normales Passwort und Einmal-Passw�rter) und ihren Rechten
 * gespeichert sind.
 * <p/>
 * Der Klasse werden nur verschl�sselte Auftr�ge �bergeben und sie entschl�sselt diese automatisch und f�hrt die Auftr�ge aus, falls der Benutzer die n�tigen
 * Rechte besitzt.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class ConfigAuthentication implements Authentication {

	/** Als Schl�ssel dient der Benutzername (String) als Value werden alle Informationen, die zu einem Benutzer gespeichert wurden, zur�ckgegeben. */
	private final Map<String, UserAccount> _userAccounts = new HashMap<String, UserAccount>();

	/** XML-Datei, wird zum anlagen einer Sicherheitskopie gebraucht */
	private final File _xmlFile;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Repr�sentiert die vollst�ndige XML-Datei. */
	private final Document _xmlDocument;

	/** Speichert die Basis der Verzeichnisse f�r die Konfigurationsbereiche. */
	private URI _uriBase;

	/**
	 * Diese Liste speichert alle Texte, die mit {@link #getText} erzeugt wurden. Die Texte werden immer an das Ender der Liste eingef�gt. Wird ein Text empfangen,
	 * wird dieser aus der Liste gel�scht.
	 * <p/>
	 * Erreicht die eine bestimmte Gr��e, wird das erste Element gel�scht, da das erste Element am l�ngsten in der Liste vorhanden ist.
	 * <p/>
	 * Die Liste ist nicht synchronisiert.
	 */
	private final LinkedList<String> _randomText = new LinkedList<String>();

	/** Wird ben�tigt um bei den entsprechenden Konfigurationsbereichen neue Benutzer anzulegen */
	private DataModel _dataModel;

	private final FileLock _lockAuthenticationFile;

	/**
	 * L�dt alle Informationen aus der angegebenen Datei. Ist die Datei nicht vorhanden, wird eine Datei mit allen Grundeinstellungen erzeugt.
	 *
	 * @param userFile XML-Datei, in der alle Benutzer gespeichert sind.
	 */
	public ConfigAuthentication(File userFile, DataModel dataModel) throws ParserConfigurationException {

		// Die Datei gegen doppelten Zugriff sichern
		_lockAuthenticationFile = new FileLock(userFile);
		try {
			_lockAuthenticationFile.lock();
		}
		catch(IOException e) {
			final String errorMessage =
					"IOException beim Versuch die lock-Datei zu schreiben. Datei, die gesichert werden sollte " + userFile.getAbsolutePath();
			e.printStackTrace();
			_debug.error(errorMessage, e);
			throw new RuntimeException(errorMessage);
		}

		_xmlFile = userFile;
		_dataModel = dataModel;

		// Es gibt die Datei, also Daten auslesen
		final CountingErrorHandler errorHandler = new CountingErrorHandler();
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		// die Validierung der XML-Datei anhand der DTD durchf�hren
		factory.setValidating(true);
		factory.setAttribute("http://xml.org/sax/features/validation", Boolean.TRUE);
		DocumentBuilder builder = factory.newDocumentBuilder();

		_debug.config("Datei wird eingelesen", _xmlFile);
		try {
			builder.setErrorHandler(errorHandler);
			builder.setEntityResolver(new ConfigAuthenticationEntityResolver());
			_xmlDocument = builder.parse(_xmlFile);	// evtl. mittels BufferedInputStream cachen
			errorHandler.printSummary();
			if(errorHandler.getErrorCount() > 0) {
				throw new ConfigurationException(errorHandler.getErrorCount() + " Fehler beim Parsen der XML-Datei " + _xmlFile.toString());
			}
		}
		catch(Exception ex) {
			final String errorMessage = "Die Benutzerdaten der Konfiguration konnten nicht eingelesen werden: " + _xmlFile.toString();
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		_uriBase = _xmlFile.getParentFile().toURI();
		_debug.config("Verzeichnisbasis f�r die Benutzer der Konfiguration", _uriBase.toString());
		// Daten aus der XML-Datei einlesen
		readUserAccounts();
		_debug.config("Benutzerdaten der Konfiguration wurden vollst�ndig eingelesen.");
	}

	/**
	 * Lie�t alle Benutzer aus der XML-Datei ein und erzeugt entsprechende Java-Objekte. Diese werden dann in der in der Hashtable gespeichert. Die Methode ist
	 * private, weil diese Funktionalit�t nur an dieser Stelle zur Verf�gung gestellt werden soll.
	 */
	private void readUserAccounts() {
		synchronized(_xmlDocument) {
			Element xmlRoot = _xmlDocument.getDocumentElement();

			NodeList entryList = xmlRoot.getElementsByTagName("benutzeridentifikation");
			for(int i = 0; i < entryList.getLength(); i++) {
				final Element element = (Element)entryList.item(i);

				final String userName = element.getAttribute("name");
				// Passwort, aus der XML-Datei. Das ist nicht in Klarschrift
				final String xmlPassword = element.getAttribute("passwort");

				// Hat der Benutzer Admin-Rechte
				final boolean admin;

				if("ja".equals(element.getAttribute("admin").toLowerCase())) {
					admin = true;
				}
				else {
					admin = false;
				}

				// Alle Einmal-Passw�rter des Accounts (auch die schon benutzen)
				final List<SingleServingPassword> allSingleServingPasswords = new ArrayList<SingleServingPassword>();

				// Einmal-Passwort Liste
				final NodeList xmlSingleServingPasswordList = element.getElementsByTagName("autorisierungspasswort");

				for(int nr = 0; nr < xmlSingleServingPasswordList.getLength(); nr++) {
					// Einmal-Passwort als XML-Objekt
					final Element xmlSingleServingPassword = (Element)xmlSingleServingPasswordList.item(nr);

					// Einmal-Passwort, das aus der XML Datei eingelesen wurde, keine Klarschrift
					final String xmlSingleServingPasswort = xmlSingleServingPassword.getAttribute("passwort");
					// Index des Passworts (Integer)
					final int index = Integer.parseInt(xmlSingleServingPassword.getAttribute("passwortindex"));
					// Ist das Passwort noch g�ltig (ja oder nein)
					final boolean valid;
					if("ja".equals(xmlSingleServingPassword.getAttribute("gueltig").toLowerCase())) {
						valid = true;
					}
					else {
						valid = false;
					}
					allSingleServingPasswords.add(new SingleServingPassword(xmlSingleServingPasswort, index, valid, xmlSingleServingPassword));
				} // Alle Einmal-Passw�rter

				// Alle Einmal-Passw�rter wurden eingelesen

				// Alle Infos stehen zur Verf�gung, das Objekt kann in die Map eingetargen werden
				final UserAccount userAccount = new UserAccount(userName, xmlPassword, admin, allSingleServingPasswords, element);

				if(_userAccounts.containsKey(userAccount.getUsername())) {
					// Einfach das erste vorkommen �berschreiben. Dieser Fall kann nur vorkommen, wenn die XML-Datei von Hand erzeugt
					// wurde.
					_debug.warning("Der Benutzername " + userAccount.getUsername() + " ist bereits in der Benutzerdatei vorhanden");
				}
				_userAccounts.put(userAccount.getUsername(), userAccount);
			} // Alle Accounts durchgehen
		}
	}

	public void isValidUser(final String username, final byte[] encryptedPassword, final String authentificationText, final String authentificationProcessName)
			throws Exception {

		// Es wird eine IllegalArgumException geworfen, wenn das Verfahren unbekannt ist oder das Verfahren nicht benutzt werden darf
		final EncryptDecryptProcedure usedDecryptProcedure = isEncryptDecryptProcedureAllowed(authentificationProcessName);

		if(_userAccounts.containsKey(username)) {
			final byte[] originalEncryptedPassword = EncryptFactory.getEncryptInstance(usedDecryptProcedure).encrypt(
					_userAccounts.get(username).getPassword(), authentificationText
			);

			//Pr�fen, ob das Passwort �bereinstimmt
			if(!Arrays.equals(encryptedPassword, originalEncryptedPassword)) {
				// Da es nicht �bereinstimmt versuchen ein Einmalpasswort zu benutzen. Wenn es ein Passwort gibt, wird es benutzt und gesperrt (XML-Datei wird
				// aktualisiert). Gibt es kein Passwort, wird eine Exception geworfen.
				_userAccounts.get(username).useSingleServingPassword(encryptedPassword, authentificationText, authentificationProcessName);
			}
		}
		else {
			// Es gibt zu dem Benutzernamen keine Informationen, also gibt es diesen Benutzer nicht
			_debug.warning("Zu dem Benutzer '" + username + "' existiert in der benutzerverwaltung.xml keine Benutzeridentifikation");
			throw new IllegalArgumentException("Benutzername/Passwort ist falsch");
		}
	}

	public byte[] getText() {
		final Random rand = new Random();
		final Long randomLong = new Long(rand.nextLong());

		synchronized(_randomText) {
			// Der Wert 100 wurde willk�rlich gew�hlt
			if(_randomText.size() == 100) {
				// Die Liste wird zu gross, siehe Kommentar der Liste.
				_randomText.removeFirst();
			}
			_randomText.addLast(randomLong.toString());
			return randomLong.toString().getBytes();
		} // synch
	}

	public void close() {
		try {
			try {
				saveXMLFile();
			}
			catch(TransformerException e) {
				final String errorText = "Fehler beim Speichern der Benutzerdateien, es wird weiter versucht weitere Daten zu sichern";
				e.printStackTrace();
				_debug.error(errorText, e);
			}
			catch(FileNotFoundException e) {
				final String errorText = "Fehler beim Speichern der Benutzerdateien, es wird weiter versucht weitere Daten zu sichern";
				e.printStackTrace();
				_debug.error(errorText, e);
			}
		}
		finally {
			_lockAuthenticationFile.unlock();
		}
	}

	/**
	 * Pr�ft, ob der �bergebene Text in der Liste der zuf�llig erzeugten Texte <code>_randomText</code> vorhanden ist. Kann der Text nicht gefunden werden, wird
	 * eine Exception geworfen. Konnte der Text gefunden werden, wird der Text aus der Liste entfernt.
	 *
	 * @param randomText Text, der in der Liste der verschickten Texte zu finden sein muss
	 *
	 * @throws ConfigurationTaskException Der �bergebene Text konnte in der Liste der verschickten Texte nicht gefunden werden
	 */
	private void checkRandomText(byte[] randomText) throws ConfigurationTaskException {
		final String randomTextString = new String(randomText);
		synchronized(_randomText) {

			if(!_randomText.remove(randomTextString)) {
				// Der �bergebene Text befindet sich nicht in den verschickten Texten.
				// Dies ist ein Fehler.
				throw new ConfigurationTaskException("Annahme verweigert");
			}
		}
	}

	/**
	 * F�hrt einen Auftrag der Benutzerverwaltung aus und entschl�sselt dabei das �bergebene Byte-Array
	 * @param usernameCustomer	  Benutzer, der den Auftrag erteilt
	 * @param encryptedMessage	  verschl�sselte Aufgabe, die ausgef�hrt werden soll
	 * @param authentificationProcessName  Entschl�sselungsverfahren
	 * @return Die R�ckgabe des ausgef�hrten Tasks (beispielsweise die Anzahl der verbleibenden Einmalpassw�rter, falls danach gefragt wurde.
	 * {@link de.bsvrz.puk.config.main.authentication.ConfigAuthentication.UserAccount#NO_RESULT} (-1) falls die Aufgabe keine R�ckgabe liefert.
	 * @throws RequestException Fehler in der Anfrage
	 * @throws ConfigurationTaskException Fehler beim Ausf�hren der Anweisung
	 */
	public int processTask(String usernameCustomer, byte[] encryptedMessage, String authentificationProcessName)
			throws RequestException, ConfigurationTaskException {
		if(_userAccounts.containsKey(usernameCustomer)) {

			// Verschl�sselten Text entschl�sseln

			// F�ngt alle Exceptions des Deserialisierers und wandelt sie in RequestExceptions um. Request und ConfigurationsTaskException werden durchgelassen
			try {
				// Verfahren, das benutzt werden kann. Ist das geforderte Verfahren nicht bekannt, wird eine Exception geworfen
				final EncryptDecryptProcedure encryptDecryptProcedure = isEncryptDecryptProcedureAllowed(authentificationProcessName);

				byte[] decryptedMessage;
				try {
					decryptedMessage = DecryptFactory.getDecryptInstance(encryptDecryptProcedure).decrypt(
							encryptedMessage, _userAccounts.get(usernameCustomer).getPassword()
					);
				}
				catch(Exception e) {
					// Die Nachricht konnte nicht entschl�sselt werden, z.b. weil das Passwort falsch ist
					_debug.fine("Fehler beim Entschl�sseln der Nachricht", e);

					throw new ConfigurationTaskException("Die Nachricht konnte nicht entschl�sselt werden (Passwort, Benutzername falsch?)");
				}

				// Serializerversion auslesen, dies steht in den ersten 4 Bytes
				final int serializerVersion = getSerializerVersion(decryptedMessage);
				decryptedMessage = removeFirst4Bytes(decryptedMessage);

				final InputStream in = new ByteArrayInputStream(decryptedMessage);
				final Deserializer deserializer = SerializingFactory.createDeserializer(serializerVersion, in);

				// In den ersten 4 Bytes steht der Nachrichtentyp
				final int messageType = deserializer.readInt();

				// In den n�chsten Bytes steht ein Zufallstext, der vorher zu Applikation geschickt wurde.
				// Dieser Text muss der Konfiguration bekannt sein. Ist der Text unbekannt
				// wird eine Exception geworfen und die Verarbeitung des Pakets abgelehnt.

				// Gr��e der Byte-Arrays
				final int sizeOfRandomText = deserializer.readInt();
				checkRandomText(deserializer.readBytes(sizeOfRandomText));

				// Was f�r ein Auftrag muss ausgef�hrt werden
				switch(messageType) {
					case 1: {
						// Einmal-Passwort erzeugen
						createSingleServingPassword(usernameCustomer, deserializer.readString(), deserializer.readString());
						return UserAccount.NO_RESULT;
					}
					case 2: {
						// Neuer Benutzer
						createNewUser(
								usernameCustomer,
								deserializer.readString(),
								deserializer.readString(),
								deserializer.readString(),
								deserializer.readBoolean(),
								deserializer.readString(),
						        null
						);
						return UserAccount.NO_RESULT;
					}
					case 3: {
						// Passwort �ndern
						changeUserPassword(usernameCustomer, deserializer.readString(), deserializer.readString());
						return UserAccount.NO_RESULT;
					}
					case 4: {
						// Benutzerrechte �ndern
						changeUserRights(usernameCustomer, deserializer.readString(), deserializer.readBoolean());
						return UserAccount.NO_RESULT;
					}
					case 5: {
						// Benutzer l�schen
						deleteUser(usernameCustomer, deserializer.readString());
						return UserAccount.NO_RESULT;
					}
					case 6: {
						// Einmalpassw�rter l�schen
						clearSingleServingPasswords(usernameCustomer, deserializer.readString());
						return UserAccount.NO_RESULT;
					}
					case 7: {
						// Abfrage nach Adminstatus
						return isUserAdmin(usernameCustomer, deserializer.readString()) ? 1 : 0;
					}
					case 8: {
						// Abfrage nach Anzahl der verbleibenden Einmalpassw�rtern
						return countRemainingSingleServingPasswords(usernameCustomer, deserializer.readString());
					}
					case 9: {
						//  Neuer Benutzer inklusive konfigurierender Datens�tze
						createNewUser(
								usernameCustomer,
								deserializer.readString(),
								deserializer.readString(),
								deserializer.readString(),
								deserializer.readBoolean(),
								deserializer.readString(),
						        readDataAndATGUsageInformation(deserializer)
						);
						return UserAccount.NO_RESULT;
					}
					case 10: {
						// Abfrage nach Existenz
						final String userToCheck = deserializer.readString();
						return (_userAccounts.containsKey(userToCheck) && userHasObject(userToCheck, "")) ? 1 : 0;
					}					
					default: {
						// unbekannter Auftrag
						throw new ConfigurationTaskException("Unbekannte Anweisung");
					}
				}
			}
			catch(IOException e) {
				_debug.error("Fehler im Deserialisierer", e);
				throw new RequestException(e);
			}
			catch(NoSuchVersionException e) {
				_debug.error("Unbekannte Version", e);
				throw new RequestException(e);
			}
		}
		else {
			// Der Benutzer ist unbekannt
			throw new ConfigurationTaskException("Benutzer/Passwortkombination ist falsch");
		}
	}

	/**
	 * L�scht f�r einen angegebenen Benutzer alle Einmalpassw�rter bzw. markiert diese als ung�ltig. Nur ein Admin und der Benutzer selbst darf diese Aktion ausf�hren.
	 * @param orderer Der Auftraggeber der Aktion
	 * @param username Der Benutzer, dessen Einmalpassw�rter gel�scht werden sollen
	 * @throws FileNotFoundException
	 * @throws ConfigurationTaskException
	 */
	private void clearSingleServingPasswords(final String orderer, final String username)
			throws FileNotFoundException, ConfigurationTaskException {
		// pr�fen, ob der Benutzer diese Aktion durchf�hren darf
		if(isAdmin(orderer) || orderer.equals(username)) {

			if(_userAccounts.containsKey(username)) {
				try {
					_userAccounts.get(username).clearSingleServingPasswords();
				}
				catch(TransformerException e) {
					throw new ConfigurationChangeException("Konnte Einmalpassw�rter nicht l�schen", e);
				}
			}
			else {
				throw new ConfigurationTaskException("Unbekannter Benutzer");
			}
		}
		else {
			throw new ConfigurationTaskException("Benutzer verf�gt nicht �ber die ben�tigten Rechte");
		}
	}

	/**
	 * Z�hlt die verbleibenden Einmalpassw�rter f�r einen angegeben Benutzer. Nur ein Admin und der Benutzer selbst darf diese Aktion ausf�hren.
	 * @param orderer Der Auftraggeber der Aktion
	 * @param username Der Benutzer, dessen Einmalpassw�rter gez�hlt werden sollen
	 * @return Die Anzahl der verbliebenen Einmalpassw�rter
	 * @throws FileNotFoundException
	 * @throws ConfigurationTaskException
	 */
	private int countRemainingSingleServingPasswords(final String orderer, final String username)
			throws FileNotFoundException, ConfigurationTaskException {
		// pr�fen, ob der Benutzer diese Aktion durchf�hren darf
		if(isAdmin(orderer) || orderer.equals(username)) {

			if(_userAccounts.containsKey(username)) {
				return _userAccounts.get(username).countSingleServingPasswords();
			}
			else {
				throw new ConfigurationTaskException("Unbekannter Benutzer");
			}
		}
		else {
			throw new ConfigurationTaskException("Benutzer verf�gt nicht �ber die ben�tigten Rechte");
		}
	}

	/**
	 * Pr�ft ob ein Benutzer Adminrechte hat. Jeder Benutzer darf diese Aktion ausf�hren.
	 * @param orderer Der Auftraggeber der Aktion. Wird in dieser Funktion derzeit nicht ber�cksichtigt, da jeder diese Abfrage ausf�hren darf
	 * @param userToCheck Der Benutzer, dessen Rechte gepr�ft werden sollen.
	 * @return True falls der Benutzer ein Admin ist
	 * @throws ConfigurationTaskException Der Auftrag kann nicht ausgef�hrt werden, weil der Benutzer nicht existiert
	 */
	private boolean isUserAdmin(final String orderer, final String userToCheck) throws ConfigurationTaskException {
		if(_userAccounts.containsKey(userToCheck)) {
			return _userAccounts.get(userToCheck).isAdmin();
		}
		throw new ConfigurationTaskException("Unbekannter Benutzer");
	}

	/**
	 * Hilfsmethode die eine <code>Collection&lt;DataAndATGUsageInformation&gt;</code> aus einem Deserializer deserialisiert.
	 * @param deserializer Quelle der Daten
	 * @return Eine <code>Collection&lt;DataAndATGUsageInformation&gt;</code> mit den Daten aus dem Deserializer
	 * @throws IOException
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
	 */
	private Collection<DataAndATGUsageInformation> readDataAndATGUsageInformation(final Deserializer deserializer) throws IOException {
		final int numberOfPackets = deserializer.readInt();
		final ArrayList<DataAndATGUsageInformation> result = new ArrayList<DataAndATGUsageInformation>(numberOfPackets);

		for(int i = 0; i < numberOfPackets; i++){
			final AttributeGroupUsage attributeGroupUsage = (AttributeGroupUsage)deserializer.readObjectReference(_dataModel);
			Data data = deserializer.readData(attributeGroupUsage.getAttributeGroup());
			result.add(new DataAndATGUsageInformation(attributeGroupUsage, data));
		}

		return result;
	}

	/**
	 * @param username                      Benutzer, der den Auftrag angestossen hat
	 * @param usernameSingleServingPasswort Benutzer f�r den das Einmal-Passwort gedacht ist
	 * @param passwortSingleServingPasswort Einmal-Passwort
	 *
	 * @throws RequestException           Technischer Fehler, der Auftrag konnte nicht bearbeitet werden.
	 * @throws ConfigurationTaskException Die Konfiguration weigert sich den Auftrag auszuf�hren weil z.b. das Passwort falsch war, der Benutzer nicht die n�tigen
	 *                                    Rechte besitzt usw..
	 */
	private void createSingleServingPassword(String username, String usernameSingleServingPasswort, String passwortSingleServingPasswort)
			throws RequestException, ConfigurationTaskException {
		// pr�fen, ob der Benutzer �berhaupt ein Einmal-Passwort erzeugen darf
		if(isAdmin(username)) {
			// Der Benutzer darf ein Einmal-Passwort anlegen, also Nachricht entschl�sseln

			// Einmal-Passwort erzeugen
			if(_userAccounts.containsKey(usernameSingleServingPasswort)) {
				_userAccounts.get(usernameSingleServingPasswort).createNewSingleServingPassword(passwortSingleServingPasswort);
			}
			else {
				// Der Benutzer, f�r den das Passwort angelegt werden soll, existiert nicht
				throw new ConfigurationTaskException("Unbekannter Benutzer");
			}
		}
		else {
			throw new ConfigurationTaskException("Benutzer verf�gt nicht �ber die ben�tigten Rechte");
		}
	}

	/**
	 * Pr�ft, ob das Verfahren, das zum ver/entschl�sseln benutzt wurde, zugelassen ist. Ist das Verfahren nicht zugelassen oder es kann nicht zugeordnet werden,
	 * wird eine ConfigurationTaskException geworfen.
	 *
	 * @param usedEncryptDecryptProcedure Benutztes Verfahren als String
	 *
	 * @return Verfahren
	 */
	private EncryptDecryptProcedure isEncryptDecryptProcedureAllowed(String usedEncryptDecryptProcedure) throws ConfigurationTaskException {
		// Es wird eine IllegalArgumException geworfen, wenn das Verfahren unbekannt ist
		final EncryptDecryptProcedure usedProcedure = EncryptDecryptProcedure.valueOf(usedEncryptDecryptProcedure);

		// Sollen weitere Verschl�sslungsverfahren benutzt werden, muss die Factory erweitert werden und die If-Abfrage um
		// die entsprechenden zugelassenen Verfahren erweitert werden
		if((usedProcedure != EncryptDecryptProcedure.HmacMD5) && (usedProcedure != EncryptDecryptProcedure.PBEWithMD5AndDES)) {
			// Das gew�hlte Verschl�ssungsverfahren wird nicht unterst�tzt
			throw new ConfigurationTaskException("Das Verfahren wird nicht unterst�tzt: " + usedEncryptDecryptProcedure);
		}

		return usedProcedure;
	}

	/**
	 * Pr�ft ob der Benutzer Admin-Rechte besitzt.
	 *
	 * @param username Benutzername, der gepr�ft werden soll ob Admin-Rechte vorhanden sind
	 *
	 * @return true = Der Benutzer darf die Eigenschaften anderer Benutzer �ndern und Einmal-Passw�rter anlegen; false = Der Benutzer darf nur sein eigenes
	 *         Passwort �ndern
	 */
	private boolean isAdmin(String username) {
		if(_userAccounts.containsKey(username)) {
			return _userAccounts.get(username).isAdmin();
		}
		else {
			return false;
		}
	}

	/**
	 * Legt einen neuen Benutzer mit den �bergebenen Parametern an.
	 *
	 * @param username          Benutzer, der den Auftrag erteilt
	 * @param newUserName       Name des neuen Benutzers
	 * @param newUserPassword   Passwort des neuen Benutzers
	 * @param admin             Rechte des neuen Benutzers (true = Adminrechte; false = normaler Benutzerrechte)
	 * @param newUserPid        Pid, die der neue Benutzer erhalten soll. Wird ein Leerstring ("") �bergeben, so bekommt der Benutzer keine expliziete Pid
	 * @param configurationArea Pid des Konfigurationsbereichs, in dem der neue Benutzer angelegt werden soll
	 * @param data              Konfigurierende Datens�tze, die angelegt werden sollen (falls leere Liste oder <code>null</code> werden keine Daten angelegt)
	 *
	 * @throws ConfigurationTaskException Der neue Benutzer durfte nicht anglegt werden (Keine Rechte, Bentuzer bereits vorhanden)
	 * @throws RequestException           technischer Fehler beim Zugriff auf die XML-Datei
	 *
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject(de.bsvrz.dav.daf.main.config.DynamicObjectType, String, String, java.util.Collection)
	 */
	private void createNewUser(String username, String newUserName, String newUserPid, String newUserPassword, boolean admin, String configurationArea, Collection<DataAndATGUsageInformation> data)
			throws ConfigurationTaskException, RequestException {
		if(isAdmin(username)) {

			// Es werden 4 F�lle betrachtet
			// Fall 1: Es gibt weder ein Objekt, das den Benutzer in der Konfiguration darstellt, noch einen Eintrag in der XML-Datei (Objekt erzeugen und XML-Eintrag erzeugen (Normalfall))
			// Fall 2: Es gibt einen Eintrag in der XML-Datei aber kein Objekt das den Benutzer in der Konfiguration darstellt (Objekt erzeugen und gegebenfalls XML-Datei anpassen)
			// Fall 3: Es gibt ein Objekt, aber keinen Eintrag in der XML-Datei (Eintrag in die XML-Datei, Objekt nicht �ndern)
			// Fall 4: Es gibt ein Objekt und einen Eintrag in der XML-Datei (Fehlerfall)

			// Speichert, ob es zu einem Benutzer ein g�ltiges Objekt gibt
			final boolean userHasObject = userHasObject(newUserName, newUserPid);

			if((!userHasObject) && (!_userAccounts.containsKey(newUserName))) {
				try {
					// Fall 1: Eintrag XML und Objekt erzeugen

					// Es wird erst das Objekt angelegt, da es passieren kann, dass der Benutzer keine Rechte daf�r besitzt.
					// Dann w�rde eine Exception geworfen und es muss auch kein Eintrag in die XML-Datei gemacht werden.
					// Darf der Benutzer Objekt anlegen und es kommt beim schreiben der XML-datei zu einem Fehler, so kann
					// die Methode erneut aufgerufen werden und es wird automatisch Fall 3 abgearbeitet.
					createUserObject(configurationArea, newUserName, newUserPid, data);
					createUserXML(newUserName, newUserPassword, admin);
				}
				catch(Exception e) {
					_debug.error("Neuen Benutzer anlegen, XML und Objekt", e);
					throw new RequestException(e);
				}
			}
			else if(!userHasObject) {
				// Fall 2, das Objekt fehlt
				createUserObject(configurationArea, newUserName, newUserPid, data);
			}
			else if(!_userAccounts.containsKey(newUserName)) {
				try {
					// Fall 3, der Eintrag in der XML-Datei fehlt
					createUserXML(newUserName, newUserPassword, admin);
				}
				catch(Exception e) {
					_debug.error("Neuen Benutzer anlegen, XML", e);
					throw new RequestException(e);
				}
			}
			else {
				// Fall 4, es ist alles vorhanden. Ein bestehender Benutzer soll �berschrieben werden. Das ist ein
				// Fehler.
				throw new ConfigurationTaskException("Der Benutzername ist bereits vergeben");
			}
		}
		else {
			throw new ConfigurationTaskException("Der Benutzer hat nicht die n�tigen Rechte");
		}
	}

	/**
	 * Erzeugt ein Objekt vom Typ "typ.Benutzer".
	 *
	 * @param pidConfigurationArea Pid des Konfiguratinsbereichs, in dem der neue Benutzer angelegt werden soll
	 * @param username             Name des Objekts
	 * @param pid                  Pid des Objekts
	 * @param data                 Konfigurierende Datens�tze, die angelegt werden sollen, oder <code>null</code> falls keine angelgt werden sollen
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Fehler beim Erzeugen des neuen Benutzers
	 */
	private void createUserObject(String pidConfigurationArea, String username, String pid, Collection<DataAndATGUsageInformation> data) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = _dataModel.getConfigurationArea(pidConfigurationArea);
		if(configurationArea == null) {
			final String message = "Das Erzeugen eines neuen Benutzerobjekts ist fehlgeschlagen, weil der angegebene Konfigurationsbereich mit der PID "
			                       + pidConfigurationArea + " nicht gefunden wurde.";
			_debug.error(message);
			throw new ConfigurationChangeException(message);
		}
		final SystemObjectType systemObjectType = _dataModel.getType("typ.benutzer");
		if(systemObjectType instanceof DynamicObjectType) {
			DynamicObjectType type = (DynamicObjectType)systemObjectType;
			configurationArea.createDynamicObject(type, pid, username, data);
		}
		else {
			final String message = "Das Erzeugen eines neuen Benutzerobjekts ist fehlgeschlagen, weil der typ.benutzer nicht gefunden wurde oder kein dynamischer Typ ist";
			_debug.error(message);
			throw new ConfigurationChangeException(message);
		}
	}

	/**
	 * Erzeugt einen neuen Benutzer im speicher und speichert diesen in einer XML-Datei.
	 *
	 * @param newUserName     Benutzername
	 * @param newUserPassword Passwort
	 * @param admin           Adminrechte ja/nein
	 *
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 */
	private void createUserXML(String newUserName, String newUserPassword, boolean admin) throws FileNotFoundException, TransformerException {
		// F�r XML-Datei
		final String newUserRightsString;
		if(admin) {
			newUserRightsString = "ja";
		}
		else {
			newUserRightsString = "nein";
		}
		final Element xmlObject = createXMLUserAccount(newUserName, newUserPassword, newUserRightsString);

		final UserAccount newUser = new UserAccount(newUserName, newUserPassword, admin, new ArrayList<SingleServingPassword>(), xmlObject);

		synchronized(_xmlDocument) {
			// Das neue Objekt in die Liste der bestehenden einf�gen
			_xmlDocument.getDocumentElement().appendChild(xmlObject);
		}

		// Speichern
		saveXMLFile();
		_userAccounts.put(newUser.getUsername(), newUser);
	}

	/**
	 * Pr�ft, ob es zu der Kombination Benutzername und Pid ein g�ltiges Objekt gibt. Ein g�ltiges Objekt bedeutet, dass Pid und Benutzername �bereinstimmen.
	 *
	 * @param username Benutzername
	 * @param pid      Pid des Benutzers
	 *
	 * @return true = Es gibt ein aktuell g�ltiges Objekt; false = Es gibt kein aktuell g�ltiges Objekt
	 *
	 * @throws IllegalStateException Es gibt ein Objekt mit der angegebenen Pid, aber der Name des Objekts ist anders, als der �bergebene Name
	 */
	private boolean userHasObject(final String username, final String pid) {

		if(!"".equals(pid)) {
			// Es wurde eine Pid �bergeben, gibt es zu der Pid ein Objekt
			final SystemObject user = _dataModel.getObject(pid);
			if(user != null) {
				// Es gibt ein Objekt zur angegebenen Pid
				if((username.equals(user.getName()))) {
					// Pid und Benutzername stimmen mit dem gefundenen Objekt �berein. Das Objekt wurde gefunden
					return true;
				}
				else {
					// Es gibt zwar ein Objekt mit der Pid, aber der Name des Objekts ist anders
					throw new IllegalStateException("Es darf zu einer Pid nur einen Benutzernamen geben");
				}
			}
			else {
				// Zur angegebenen Pid konnte kein Objekt gefunden werden
				return false;
			}
		}
		else {
			// Es wurde keine Pid angegeben, es m�ssen alle Benutzer betrachtet werden
			Iterator i = _dataModel.getType("typ.benutzer").getObjects().iterator();
			while(i.hasNext()) {
				// Objekt, das in der Konfiguration gespeichert ist und einen Benutzer darstellt
				final SystemObject configUser = (SystemObject)i.next();
				if(configUser.getName().equals(username)) {
					return true;
				}
			} // while �ber alle Benutzer
			return false;
		}
	}

	/**
	 * Lie�t aus einem Byte-Array die ersten 4 Bytes aus und erzeugt daraus die benutztes Serializerversion
	 *
	 * @param message Nachricht, die ersten 4 Bytes werden ausgelesen
	 *
	 * @return Integer, der aus den ersten 4 Bytes der Nachricht ausgelesen wird
	 */
	private final int getSerializerVersion(final byte[] message) {
		return ((message[0] << 24) & 0xff000000) | ((message[1] << 16) & 0x00ff0000) | ((message[2] << 8) & 0x0000ff00) | (message[3] & 0x000000ff);
	}

	/**
	 * Entfernt die ersten 4 Bytes eines Byte-Arrays und gibt ein neues Array zur�ck, bei dem genau die ersten 4 Bytes fehlen.
	 *
	 * @param byteArray Array, aus dem die ersten 4 Bytes entfernt werden
	 *
	 * @return Array, bei dem die ersten 4 Bytes des Ursprungs-Arrays fehlen
	 */
	private final byte[] removeFirst4Bytes(byte[] byteArray) {
		final byte shortArray[] = new byte[byteArray.length - 4];
		System.arraycopy(byteArray, 4, shortArray, 0, shortArray.length);
		return shortArray;
	}

	/**
	 * Setzt bei einem Benutzer das Passwort neu. Dies kann entweder ein Admin bei einem anderen Benutzerkonto oder ein Benutzer bei seinem eigenen Benutzerkonto.
	 * <p/>
	 * Ist f�r einen Benutzer nur das Objekt des Benutzers in der Konfiguration vorhanden, aber das Benutzerkonto fehlt, wird das Benutzerkonto mit {@link
	 * #createNewUser} angelegt. Das neue Benutzerkonto besitzt dabei keine Adminrechte. Das neue Benutzerkonto wird dabei das Passwort erhalten, das neu gesetzt
	 * werden sollte.
	 * <p/>
	 * Gibt es zwar ein Benutzerkonto, aber kein Objekt in der Konfiguration, wird ein Fehler ausgegeben.
	 * <p/>
	 * Sind weder Objekt noch Benutzerkonto vorhanden wird ein Fehler ausgegeben.
	 *
	 * @param username                  Benutzer, der den Auftrag zum �ndern des Passworts erteilt hat
	 * @param userNameForPasswordChange Benutzer, dessen Passwort ge�ndert werden soll
	 * @param newPassword               neues Passwort
	 *
	 * @throws ConfigurationTaskException Der Benutzer ist unbekannt oder es gibt zu dem Benutzer kein entsprechendes Objekt oder der Benutzer darf das Passwort
	 *                                    nicht �ndern (kein Admin oder der Besitzer des Passwords).
	 * @throws RequestException           Fehler beim Zugriff auf die XML-Datei
	 */
	private void changeUserPassword(String username, String userNameForPasswordChange, String newPassword) throws ConfigurationTaskException, RequestException {

		// Die Pid des Benutzers ist unbekannt, darum ""
		final boolean hasUserObject = userHasObject(userNameForPasswordChange, "");

		synchronized(_userAccounts) {
			if(hasUserObject && _userAccounts.containsKey(userNameForPasswordChange)) {
				// Das Objekt ist vorhanden und es gibt Benutzerdaten zu dem Benutzer, der ge�ndert werden soll (Das ist der Normalfall)

				// Der Benutzername steht zur Verf�gung, nun kann gepr�ft werden wer versucht das Passwort zu �ndern.
				// Ist es ein Admin
				// oder
				// versucht der Besitzer des Accounts das Passwort zu �ndern

				if(((isAdmin(username)) || (username.equals(userNameForPasswordChange)))) {
					try {
						_userAccounts.get(userNameForPasswordChange).setPassword(newPassword);
					}
					catch(Exception e) {
						_debug.error("Passwort �ndern", e);
						throw new RequestException(e);
					}
				}
				else {
					// Der Benutzer hat nicht das Recht das Passwort zu �ndern
					throw new ConfigurationTaskException("Passwort�nderung verworfen");
				}
			}
			else if(hasUserObject && (!_userAccounts.containsKey(userNameForPasswordChange))) {
				// Es gibt ein Objekt, aber kein Benutzerkonto. Falls der Benutzer ein Admin ist
				// wird ein neues Benutzerkonto erzeugt
				if(isAdmin(username)) {
					try {
						// Der Benutzer darf neue Konten erzeugen. Also wird ein neues Benutzerkonto ohne Adminrechte angelegt
						createUserXML(userNameForPasswordChange, newPassword, false);
					}
					catch(Exception e) {
						_debug.error("Passwort �ndern", e);
						throw new RequestException(e);
					}
				}
				else {
					// Der Benutzer hat nicht das Recht neue Benutzerkonten zu erzeugen
					throw new ConfigurationTaskException("Passwort�nderung verworfen, da ben�tigte Rechte fehlen");
				}
			}
			else {
				// Es ist ein Fehler aufgetreten, der Fehler wird nun genauer spezifiziert
				if(!hasUserObject) {
					// Es gibt kein Objekt
					throw new ConfigurationTaskException("Kein Benutzerobjekt vorhanden");
				}
				else if(!_userAccounts.containsKey(userNameForPasswordChange)) {
					// Das Benutzerkonto fehlt
					throw new ConfigurationTaskException("Unbekannter Benutzer");
				}
			}
		} // synchronized (_userAccounts)
	}

	/**
	 * @param username             Benutzer, der den Auftrag erteilt hat (dieser muss Adminrechte besitzen)
	 * @param usernameChangeRights Benutzer, dessen Rechte ge�ndert werden soll
	 * @param newUserRights        Neue Rechte des Benutzers (true = Admin-Rechte, false = normaler Benutzerrechte
	 *
	 * @throws ConfigurationTaskException Der Benutzer ist unbekannt oder der Auftraggeber besitzt nicht die n�tigen Rechte
	 * @throws RequestException           Fehler beim Zugriff auf die XML-Datei
	 */
	private void changeUserRights(String username, String usernameChangeRights, boolean newUserRights) throws ConfigurationTaskException, RequestException {
		if(isAdmin(username)) {
			// Admin versucht die Rechte zu �ndern
			if(_userAccounts.containsKey(usernameChangeRights)) {
				// Der Benutzer existiert
				try {
					_userAccounts.get(usernameChangeRights).setAdminRights(newUserRights);
				}
				catch(Exception e) {
					_debug.error("Benutzerrechte �ndern", e);
					throw new RequestException(e);
				}
			}
			else {
				throw new ConfigurationTaskException("Unbekannter Benutzer");
			}
		}
		else {
			// Der Benutzer besitzt nicht die n�tigen Rechte
			throw new ConfigurationTaskException("Der Benutzer besitzt nicht die n�tgen Rechte");
		}
	}

	/**
	 * Erzeugt einen Desirialisierer auf den mit den �blichen Methoden zugegriffen werden kann. Daf�r wird der �bergebene, verschl�sselte Text entschl�sselt.
	 *
	 * @param encryptedMessage            Verschl�sselte Nachricht, diese wird entschl�sselt
	 * @param decryptenText               Text, mit dem die verschl�sselte Nachricht entschl�sselt wird
	 * @param authentificationProcessName Verfahren, mit dem die Nachricht verschl�sselt wurde
	 *
	 * @return Deserialisierer
	 *
	 * @throws Exception Fehler beim entschl�sseln oder beim erstellen des Desirialisierers
	 */
	private final Deserializer getDeserializer(byte[] encryptedMessage, String decryptenText, String authentificationProcessName) throws Exception {
		final byte[] decryptedMessage = DecryptFactory.getDecryptInstance(isEncryptDecryptProcedureAllowed(authentificationProcessName)).decrypt(
				encryptedMessage, decryptenText
		);

		final int serializerVersion = getSerializerVersion(decryptedMessage);

		InputStream in = new ByteArrayInputStream(removeFirst4Bytes(decryptedMessage));

		//deserialisieren
		return SerializingFactory.createDeserializer(serializerVersion, in);
	}

	/**
	 * L�scht einen angegebenen Benutzer. Diese Aktion kann nur von Administratoren ausgef�hrt werden.
	 * @param username Veranlasser der Aktion
	 * @param userToDelete Benutzername des Benutzers, der gel�scht werden soll
	 * @throws RequestException Das L�schen kann aufgrund eines Problems nicht durchgef�hrt werden
	 * @throws ConfigurationTaskException Die Anfrage ist fehlerhaft weil der Veranlasser nicht die n�tigen Rechte hat oder der zu l�schende Benutzer nicht existiert
	 */
	private void deleteUser(String username, String userToDelete) throws  RequestException , ConfigurationTaskException{
		if(isAdmin(username)) {
			final boolean userHasObject = userHasObject(userToDelete, "");

			if((userHasObject) && (_userAccounts.containsKey(userToDelete))) {
				try {
					deleteUserObject(userToDelete);
					deleteUserXML(userToDelete);
				}
				catch(Exception e) {
					_debug.error("Benutzer l�schen, XML und Objekt", e);
					throw new RequestException(e);
				}
			}
			else if(userHasObject) {
				// Fall 2, das Objekt fehlt
				deleteUserObject(userToDelete);
			}
			else if(_userAccounts.containsKey(userToDelete)) {
				try {
					// Fall 3, der Eintrag in der XML-Datei fehlt
					deleteUserXML(userToDelete);
				}
				catch(Exception e) {
					_debug.error("Benutzer l�schen, XML", e);
					throw new RequestException(e);
				}
			}
			else {
				throw new ConfigurationTaskException("Der Benutzer existiert nicht");
			}
		}
		else {
			throw new ConfigurationTaskException("Der Benutzer hat nicht die n�tigen Rechte");
		}
	}

	/**
	 * L�scht einen Benutzer aus der XML-Datei
	 * @param userToDelete Benutzer, der gel�scht werden soll
	 * @throws TransformerException Fehler beim XML-Zugriff
	 * @throws FileNotFoundException XMl-Datei nciht gefunden
	 */
	private void deleteUserXML(final String userToDelete) throws TransformerException, FileNotFoundException {
		try{
			synchronized(_xmlDocument) {
				final NodeList childNodes = _xmlDocument.getDocumentElement().getChildNodes();
				for(int i = 0; i < childNodes.getLength(); i++) {
					final Node node = childNodes.item(i);
					if(node.hasAttributes()) {
						final NamedNodeMap attributes = node.getAttributes();
						final Node name = attributes.getNamedItem("name");
						if(name != null && name.getNodeValue().equals(userToDelete)) {
							_xmlDocument.getDocumentElement().removeChild(node);
							saveXMLFile();
							return;
						}
					}
				}
			}
			_debug.error("deleteUserXML: Konnte Benutzer nicht aus XML-Datei l�schen. Knoten wurde nicht gefunden.", userToDelete);
		}
		finally
		{
			_userAccounts.keySet().remove(userToDelete) ;
		}
	}

	/**
	 * Entfernt das dynamische Benutzerobjekt aus dem Datenmodell
	 * @param userToDelete Benutzer, der gel�scht werden soll
	 * @throws ConfigurationChangeException Fehler beim durchf�hren der Aktion
	 */
	private void deleteUserObject(final String userToDelete) throws ConfigurationChangeException {
		Iterator i = _dataModel.getType("typ.benutzer").getObjects().iterator();
		while(i.hasNext()) {
			final SystemObject configUser = (SystemObject)i.next();
			if(configUser.getName().equals(userToDelete)) {
				configUser.invalidate();
			}
		}
	}

	/**
	 * Speichert alle Benutzerdaten in einer XML-Datei.
	 *
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	private void saveXMLFile() throws TransformerException, FileNotFoundException {

		

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
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//K2S//DTD Authentifizierung//DE");
			}
			if(systemID != null) {
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemID);
			}
			else {
				// DOCTYPE SYSTEM_ID ist nicht vorhanden -> erstellen
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "authentication.dtd");
			}

			DOMSource source = new DOMSource(_xmlDocument);

			final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(_xmlFile));
			try {
				StreamResult result = new StreamResult(outputStream);	// gibt die XML-Struktur in einem Stream (Datei) aus
				transformer.transform(source, result);
			}
			finally {
				try {
					outputStream.close();
				}
				catch(IOException e) {
					
				}
			}
		}
	}

	/**
	 * Sicher die Benutzerverwaltungsdatei in das angegebene Verzeichnis
	 * @param targetDirectory Zielverzeichnis
	 * @throws IOException IO-Fehler
	 */
	public void createBackupFile(File targetDirectory) throws IOException {
			final String fileName = _xmlFile.getName();

		try {
			saveXMLFile();
		}
		catch(TransformerException e) {
			e.printStackTrace();
			throw new IOException("Konnte XML-Datei nicht sichern: " + e.getMessage()); // wg. java 1.5
		}

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

	/**
	 * Erzeugt ein XML Objekt, das einem Einmal-Passwort entspricht.
	 *
	 * @param newPassword   Passwort des neuen Einmal-Passworts
	 * @param passwortindex Index des Einmal-Passworts
	 * @param usable        ja = Das Einmal-Passwort ist noch zu benutzen; nein = Das Einmal-Passwort kann nicht mehr benutzt werden
	 *
	 * @return XML-Objekt, das einem Einmal-Passwort entspricht
	 */
	private Element createXMLSingleServingPasswort(String newPassword, int passwortindex, String usable) {
		synchronized(_xmlDocument) {
			Element xmlSingleServingPassword = _xmlDocument.createElement("autorisierungspasswort");
			xmlSingleServingPassword.setAttribute("passwort", newPassword);
			xmlSingleServingPassword.setAttribute("passwortindex", String.valueOf(passwortindex));
			xmlSingleServingPassword.setAttribute("gueltig", usable);
			return xmlSingleServingPassword;
		}
	}

	/**
	 * Erzeugt ein XML Objekt, das einem Benutzerkonto entspricht. Einmal-Passw�rter m�ssen mit der entsprechenden Methode erzeugt werden.
	 *
	 * @param name     Name des Benutzers
	 * @param password Passwort des Benutzers (in Klarschrift)
	 * @param admin    ja = Der Benutzer besitzt Admin-Rechte; nein = Der Benutzer besitzt keine Admin-Rechte
	 *
	 * @return XML Objekt, das einem Benutzerkonto entspricht
	 */
	private synchronized Element createXMLUserAccount(String name, String password, String admin) {
		synchronized(_xmlDocument) {
			Element xmlSingleServingPassword = _xmlDocument.createElement("benutzeridentifikation");
			xmlSingleServingPassword.setAttribute("name", name);
			xmlSingleServingPassword.setAttribute("passwort", password);
			xmlSingleServingPassword.setAttribute("admin", admin);
			return xmlSingleServingPassword;
		}
	}

	/**
	 * Diese Klasse Speichert alle Informationen, die zu Benutzerkonto geh�ren. Dies beinhaltet:
	 * <p/>
	 * Benutzername
	 * <p/>
	 * Benutzerpasswort
	 * <p/>
	 * Adminrechte
	 * <p/>
	 * Liste von Einmal-Passw�rtern (siehe TPuK1-130)
	 * <p/>
	 * Sollen �nderungen an einem dieser Informationen vorgenommen werden, f�hrt dies erst dazu, dass die Daten persistent in einer XML-Datei gespeichert werden.
	 * Ist dies erfolgreich, wird die �nderung auch an den Objekten durchgef�hrt. Kann die �nderungen nicht gespeichert werden, wird ein entsprechender Fehler
	 * ausgegeben und die �nderung nicht durchgef�hrt
	 */
	private final class UserAccount {

		/** Benutzername des Accounts */
		private final String _username;

		/** Passwort des Accounts in Klarschrift */
		private String _password;

		/** true = Der Benutzer ist ein Admin und darf Einstellungen bei anderen Benutzern vornehmen */
		private boolean _admin;

		/**
		 * Liste, die alle benutzbaren Einmalpassw�rter enth�lt. An Index 0 steht immer das als n�chstes zu benutzende Passwort. Am Ende der Liste wird jedes neue
		 * Passwort eingef�gt. Wird ein Passwort benutzt, wird das Passwort vom Anfang der Liste entfernt (FIFO).
		 */
		private final LinkedList<SingleServingPassword> _usableSingleServingPasswords = new LinkedList<SingleServingPassword>();

		/**
		 * Speichert alle Passw�rter der Einmal-Passw�rter (Als Schl�ssel dient das Passwort in Klarschrift). Soll ein neues Einmal-Passwort erzeugt werden, und das
		 * Passwort befindet sich bereits in dieser Menge, dann darf das neue Einmal-Passwort nicht angelegt werden.
		 */
		private final Set<String> _allSingleServingPasswords = new HashSet<String>();

		/**
		 * Speichert den gr��ten Index, der bisher f�r ein Einmal-Passwort benutzt wurde. Das n�chste Einmal-Passwort h�tte als Index
		 * "_greatestSingleServingPasswordIndex++".
		 * <p/>
		 * Wird mit -1 initialisiert. Das erste Passwort erh�lt also Index 0.
		 * <p/>
		 * Der Wert wird im Konstruktor, falls Einmal-Passw�rter vorhanden sind, auf den gr��ten vergebenen Index gesetzt.
		 */
		private int _greatestSingleServingPasswordIndex = -1;

		/** XML-Objekt, dieses muss zuerst ver�ndert und gespeichert werden, bevor die Objekte im Speicher ge�ndert werden */
		private final Element _xmlObject;

		private static final int NO_RESULT = -1;

		/**
		 * @param username                  Benutzername
		 * @param xmlPassword               Passwort, wie es in der XML-Datei gespeichert wurde
		 * @param admin                     Ob der Benutzer Admin_Rechte hat
		 * @param allSingleServingPasswords Alle Einmal-Passw�rter
		 * @param xmlObject                 XML-Objekt, aus dem die obigen Daten ausgelesen wurden
		 */
		public UserAccount(String username, String xmlPassword, boolean admin, List<SingleServingPassword> allSingleServingPasswords, Element xmlObject) {
			_username = username;
			_password = xmlPassword;
			_xmlObject = xmlObject;
			_admin = admin;

			for(SingleServingPassword singleServingPassword : allSingleServingPasswords) {
				// Damit dieses Passwort nicht noch einmal vergeben werden kann
				_allSingleServingPasswords.add(singleServingPassword.getPassword());

				if(singleServingPassword.getIndex() > _greatestSingleServingPasswordIndex) {
					_greatestSingleServingPasswordIndex = singleServingPassword.getIndex();
				}

				if(singleServingPassword.isPasswordUsable()) {
					// Das Passwort kann noch benutzt werden
					_usableSingleServingPasswords.add(singleServingPassword);
				}
			}
		}

		/**
		 * Benutzername
		 *
		 * @return s.o.
		 */
		public String getUsername() {
			return _username;
		}

		/**
		 * Unverschl�sseltes Passwort des Benutzers
		 *
		 * @return s.o.
		 */
		public String getPassword() {
			return _password;
		}

		/**
		 * �ndert das Passwort und speichert das neue Passwort in einer XML-Datei
		 *
		 * @param password Neues Passwort
		 */
		public void setPassword(String password) throws FileNotFoundException, TransformerException {
			_xmlObject.setAttribute("passwort", password);
			saveXMLFile();

			// Erst nach dem das neue Passwort gespeichert wurde, wird die �nderung im Speicher �bernommen
			_password = password;
		}

		/** @return true = Der Benutzer darf die Eigenschaften anderer Benutzer �ndern; false = Der Benutzer darf nur sein Passwort �ndern */
		public boolean isAdmin() {
			return _admin;
		}

		/**
		 * Legt fest, ob ein Benutzer Admin-Rechte besitzt. Die �nderung wird sofort in der XML-Datei gespeichert.
		 *
		 * @param adminRights true = Der Benutzer besitzt Admin Rechte; false = Der Benutzer besitzt keine Admin-Rechte
		 */
		public void setAdminRights(boolean adminRights) throws FileNotFoundException, TransformerException {
			if(adminRights) {
				_xmlObject.setAttribute("admin", "ja");
			}
			else {
				_xmlObject.setAttribute("admin", "nein");
			}

			saveXMLFile();

			_admin = adminRights;
		}

		/**
		 * Erzeugt ein neues Einmal-Passwort. Der Index wird automatisch angepasst.
		 *
		 * @param newPassword Passwort des Einmal-Passworts
		 *
		 * @throws RequestException           Fehler beim Speichern des neuen Passworts, das Passwort wurde nicht angelegt.
		 * @throws ConfigurationTaskException Das Passwort wurde bereits vergeben, es wurde kein neues Passwort angelegt.
		 */
		synchronized public void createNewSingleServingPassword(final String newPassword) throws ConfigurationTaskException, RequestException {
			if(!_allSingleServingPasswords.contains(newPassword)) {
				// Das Passwort wurde noch nicht vergeben.

				// An das XML-Objekt ein neues Element h�ngen
				final Element xmlSingleServingPassword = createXMLSingleServingPasswort(newPassword, ((_greatestSingleServingPasswordIndex + 1)), "ja");
				_xmlObject.appendChild(xmlSingleServingPassword);

				// XML Datei neu speichern
				try {
					saveXMLFile();

					// Das Speichern hat geklappt, nun alle Objekte im Speicher �ndern

					// Jetzt wird es gesperrt, damit es nicht noch einmal vergeben
					// werden kann.
					_allSingleServingPasswords.add(newPassword);
					_usableSingleServingPasswords.addLast(
							new SingleServingPassword(
									newPassword, _greatestSingleServingPasswordIndex + 1, true, xmlSingleServingPassword
							)
					);
					_greatestSingleServingPasswordIndex++;
				}
				catch(Exception e) {
					// Das Passwort wurde nicht angelegt
					_debug.error("Fehler beim anlegen eines Einmal-Passworts", e);
					throw new RequestException(e);
				}
			}
			else {
				// Das Passwort wurde bereits vergeben
				throw new ConfigurationTaskException("Das Passwort wurde bereits vergeben");
			}
		}

		/**
		 * Versucht ein Einmal-Passwort zu benutzen. Ist dies m�glich, wird das Einmal-Passwort als gebraucht markiert. Wurde eine falsches Passwort �bergeben,
		 * so wird eine Exception geworfen.
		 *
		 * @param encryptedPassword           Einmal-Passwort, das vom Benutzer eingegeben wurde
		 * @param authentificationText        Text mit dem das Einmal-Passwort verschl�sselt wurde
		 * @param authentificationProcessName Name des benutzten Verschl�sslungsverfahrens
		 *
		 * @throws IllegalArgumentException     Falsches Einmal-Passwort
		 * @throws NoSuchAlgorithmException     Unbekantes Verschl�sslungsverfahren
		 * @throws UnsupportedEncodingException
		 * @throws InvalidKeyException
		 * @throws FileNotFoundException
		 * @throws TransformerException
		 */
		synchronized public void useSingleServingPassword(byte[] encryptedPassword, String authentificationText, String authentificationProcessName)
				throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, FileNotFoundException, TransformerException {
			for(SingleServingPassword singleServingPassword : _usableSingleServingPasswords) {
				final SecretKey secretKey = new SecretKeySpec(singleServingPassword.getPassword().getBytes("ISO-8859-1"), authentificationProcessName);
				Mac mac = Mac.getInstance("HmacMD5");
				mac.init(secretKey);

				if(Arrays.equals(encryptedPassword, mac.doFinal(authentificationText.getBytes("ISO-8859-1")))) {
					// Das Passwort ist g�ltig. Also das Passwort sperren
					singleServingPassword.setPasswortInvalid();
					// Speichern war erfolgreich, also kann das Passwort entfernt werden
					_usableSingleServingPasswords.remove(singleServingPassword);
					return;
				}
			}
			_debug.warning("Authentifizierungsversuch eines registrierten Benutzers fehlgeschlagen, Benutzername", getUsername());
			throw new IllegalArgumentException("Benutzername/Passwort ist falsch");
		}

		/**
		 * L�scht alle Einmalpassw�rter eines Benutzers und markiert diese als ung�ltig
		 * @throws TransformerException
		 * @throws FileNotFoundException
		 */
		public void clearSingleServingPasswords() throws TransformerException, FileNotFoundException {
			for(SingleServingPassword singleServingPassword : _usableSingleServingPasswords) {
				singleServingPassword.setPasswortInvalid();
			}
			_usableSingleServingPasswords.clear();
		}

		/**
		 * Gibt die Anzahl der verbleidenden, g�ltigen Einmalpassw�rter zur�ck
		 * @return die Anzahl der verbleidenden, g�ltigen Einmalpassw�rter
		 */
		public int countSingleServingPasswords() {
			return _usableSingleServingPasswords.size();
		}
	}

	/** Speichert alle Informationen zu einem "Einmal-Passwort" (Passwort, Index, "schon gebraucht") */
	private final class SingleServingPassword {

		/** Passwort in Klarschrift */
		private final String _password;

		/** Index des Passworts */
		private final int _index;

		/** Wurde das Passwort schon einmal benutzt */
		private boolean _passwordUsable;

		/** XML Objekt, das die Daten speichert */
		private final Element _xmlObject;

		/**
		 * @param password       Password des Einmal-Passworts, ausgelesen aus der XML-Datei
		 * @param index          Index des Passworts
		 * @param passwordUsable Kann das Passwort noch benutzt werden. true = es kann noch benutzt werden; false = es wurde bereits benutzt und kann nicht noch
		 *                       einmal benutzt werden
		 * @param xmlObject      XML-Objekt, das dem Einmal-Passwort entspricht
		 */
		public SingleServingPassword(String password, int index, boolean passwordUsable, Element xmlObject) {
			_password = password;
			_index = index;
			_passwordUsable = passwordUsable;
			_xmlObject = xmlObject;
		}

		/**
		 * Passwort des Einmal-Passworts
		 *
		 * @return s.o
		 */
		public String getPassword() {
			return _password;
		}

		/**
		 * Index des Einmal-Passworts
		 *
		 * @return s.o
		 */
		public int getIndex() {
			return _index;
		}

		/**
		 * Kann das Passwort noch benutzt werden.
		 *
		 * @return true = ja; false = nein, es wurde bereits benutzt und darf nicht noch einmal benutzt werden
		 */
		synchronized public boolean isPasswordUsable() {
			return _passwordUsable;
		}

		/** Setzt ein Einmal-Passwort auf ung�ltig und speichert diese Information in der XML-Datei (erst speichern, dann Objekte im Speicher �ndern) */
		synchronized public void setPasswortInvalid() throws FileNotFoundException, TransformerException {
			_xmlObject.setAttribute("gueltig", "nein");
			saveXMLFile();
			_passwordUsable = false;
		}
	}

	/**
	 * Implementierung eines EntityResolvers, der Referenzen auf den Public-Identifier "-//K2S//DTD Verwaltung//DE" ersetzt durch die verwaltungsdaten.dtd
	 * Resource-Datei in diesem Package.
	 */
	private class ConfigAuthenticationEntityResolver implements EntityResolver {

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
		 * @throws org.xml.sax.SAXException Bei Fehlern beim Zugriff auf externe Entities.
		 * @throws java.io.IOException
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if(publicId != null && publicId.equals("-//K2S//DTD Authentifizierung//DE")) {
				URL url = this.getClass().getResource("authentication.dtd");
				assert url != null : this.getClass();
				return new InputSource(url.toExternalForm());
			}
			return null;
		}
	}
}

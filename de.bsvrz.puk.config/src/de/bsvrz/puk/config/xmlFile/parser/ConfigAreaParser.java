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

package de.bsvrz.puk.config.xmlFile.parser;

import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.puk.config.xmlFile.properties.AspectProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeGroupProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeListProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaChangeInformation;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAspect;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationConfigurationObject;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationData;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataField;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataList;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataset;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDefaultParameter;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDoubleDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerValueRange;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectElements;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectReference;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationState;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationString;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationTimeStamp;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationValueRange;
import de.bsvrz.puk.config.xmlFile.properties.DatasetElement;
import de.bsvrz.puk.config.xmlFile.properties.ListAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ObjectSetTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.PlainAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.TargetValue;
import de.bsvrz.puk.config.xmlFile.properties.TransactionProperties;
import de.bsvrz.puk.config.xmlFile.resolver.K2SEntityResolver;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.xmlSupport.CountingErrorHandler;
import de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter.AttributeMap;
import de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter.PullableEventStream;
import de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter.SaxPullAdapter;
import de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter.StartElementEvent;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Diese Klasse erzeugt aus einer XML-Versorgungsdatei Objekte, die in die Konfiguration per Import eingebracht werden können. Die XML-Datei wird mit der
 * K2S.dtd bearbeitet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigAreaParser {

	private static final Debug _debug = Debug.getLogger();

	final SaxPullAdapter _saxPullAdapter;

	PullableEventStream _xmlStream;

	public ConfigAreaParser() throws ParserConfigurationException, SAXException {
		_saxPullAdapter = new SaxPullAdapter(new K2SEntityResolver());
	}

	/**
	 * @param areaFile Versorungsdatei
	 *
	 * @return Objekte, die aus der Versorungsdatei erzeugt wurden und mit Hilfe des Imports in die Konfiguration importiert werden können
	 *
	 * @throws SAXException             Fehler beim parsen der Versorgungsdatei
	 * @throws IllegalArgumentException Der Dateiname (ohne die Endung ".xml"), der die Pid des zu importierenden Bereichs darstellt, stimmt nicht mit dem Bereich
	 *                                  überein, der durch die Datei importiert werden soll.
	 */
	public ConfigurationAreaProperties parse(File areaFile) throws SAXException {
		CountingErrorHandler errorHandler = new CountingErrorHandler();
		SAXException exception = null;
		ConfigurationAreaProperties configurationArea = null;
		try {
			_xmlStream = _saxPullAdapter.start(areaFile, errorHandler);
			configurationArea = parseConfigurationArea();
			_saxPullAdapter.stop();
		}
		catch(IllegalStateException e) {
			_debug.error("Ungültiger Zustand beim Parsen " + _xmlStream.getLocationHint(), e);
			exception = new SAXException(e);
		}
		catch(InterruptedException e) {
			_debug.error("Unterbrechung beim Parsen " + _xmlStream.getLocationHint(), e);
			exception = new SAXException(e);
		}
		catch(RuntimeException e) {
			_debug.error("Laufzeitfehler beim Parsen " + _xmlStream.getLocationHint(), e.toString());
			exception = new SAXException(e);
		}

		if(errorHandler.getWarningCount() > 0) {
			final String message = errorHandler.getWarningCount() + " Warnung" + (errorHandler.getWarningCount() > 1 ? "en" : "") + " in " + areaFile;
			_debug.error(message);
			exception = new SAXException(message);
		}
		if(errorHandler.getErrorCount() > 0) {
			final String message = errorHandler.getErrorCount() + " Fehler in " + areaFile;
			_debug.error(message);
			exception = new SAXException(message);
		}
		if(exception != null) throw exception;

		// Die Pid des Konfigurationsbereichs muss gleich dem Dateinamen sein.
		// Der Dateiname hat die Endung ".xml", diese wird in dem Vergleich ignoriert.

		// Die Pid des Konfigurationsbereichs(Objekt)
		final String pidConfigArea = configurationArea.getPid();
		// Die Pid, die aus dem Dateinamen gefiltert wurde
		final String pidFile = areaFile.getName().split(".xml")[0];

		if(!pidConfigArea.equals(pidFile)) {
			// Der Dateinamen spiegelt nicht die Pid des Bereichs wieder, der durch die Datei importiert werden soll
			throw new IllegalArgumentException(
					"Der Bereich, der importiert werden sollte, besitzt die Pid: " + pidConfigArea + ". Der Name, der zu importierenden Datei lautet aber: "
					+ areaFile.getName() + " (" + pidFile + "). Der Dateiname paßt also nicht zur Pid des zu importierenden Bereichs."
			);
		}

		return configurationArea;
	}

	/**
	 * @param inputStream InputStream mit XML-Inhalt. Wird für Tests und andere Fälle benutzt, um nicht unnötigerweise temporäre Dateien anlegen zu müssen.
	 *
	 * @return Objekte, die aus den XML-Daten erzeugt wurden und mit Hilfe des Imports in die Konfiguration importiert werden können
	 *
	 * @throws SAXException             Fehler beim parsen der XML-Daten
	 */
	public ConfigurationAreaProperties parse(InputStream inputStream) throws SAXException {
		CountingErrorHandler errorHandler = new CountingErrorHandler();
		SAXException exception = null;
		ConfigurationAreaProperties configurationArea = null;
		try {
			_xmlStream = _saxPullAdapter.start(inputStream, errorHandler);
			configurationArea = parseConfigurationArea();
			_saxPullAdapter.stop();
		}
		catch(IllegalStateException e) {
			_debug.error("Ungültiger Zustand beim Parsen " + _xmlStream.getLocationHint(), e);
			exception = new SAXException(e);
		}
		catch(InterruptedException e) {
			_debug.error("Unterbrechung beim Parsen " + _xmlStream.getLocationHint(), e);
			exception = new SAXException(e);
		}
		catch(RuntimeException e) {
			_debug.error("Laufzeitfehler beim Parsen " + _xmlStream.getLocationHint(), e.toString());
			exception = new SAXException(e);
		}

		if(errorHandler.getWarningCount() > 0) {
			final String message = errorHandler.getWarningCount() + " Warnung" + (errorHandler.getWarningCount() > 1 ? "en" : "") + " in " + inputStream;
			_debug.error(message);
			exception = new SAXException(message);
		}
		if(errorHandler.getErrorCount() > 0) {
			final String message = errorHandler.getErrorCount() + " Fehler in " + inputStream;
			_debug.error(message);
			exception = new SAXException(message);
		}
		if(exception != null) throw exception;

		return configurationArea;
	}

	/**
	 * Beginnt die XML-Versorgungsdatei zu parsen und erstellt die benötigten Objekte
	 *
	 * @return Objekte, die aus der XML-Datei erzeugt wurden und in die Konfiguration importiert werden können
	 *
	 * @throws SAXException         Fehler beim parsen
	 * @throws InterruptedException Thread wurde mit Interrupt unterbrochen
	 */
	private ConfigurationAreaProperties parseConfigurationArea() throws SAXException, InterruptedException {
		// Pullt das Startelement mit allen Attributen der Typdefinition
		AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();
		// Attribute des Elementtyps einlesen
		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");
		final String authority = attributes.getValue("verantwortlich");
		// Inhalt "Info" einlesen
		final SystemObjectInfo info = parseInfo();

		// Inhalt "konfigurationsAenderung" einlesen
		final ConfigurationAreaChangeInformation[] configurationAreaChangeInformationses = parseConfigurationChanges();

		// Liste, die die Inhalte des Elements Name: konfigurationsBereich speichert
		final List<SystemObjectProperties> objects = new ArrayList<SystemObjectProperties>();
		final ConfigurationAreaProperties configurationAreaProperties = new ConfigurationAreaProperties(name, pid, 0, authority, info, objects);
		configurationAreaProperties.setConfigurationAreaChangeInformation(configurationAreaChangeInformationses);

		_debug.fine("Einlesen von", configurationAreaProperties);
		if(_xmlStream.matchStartElement("modell")) {
			_xmlStream.pullStartElement();
			while(_xmlStream.matchStartElement()) {
				if(_xmlStream.matchStartElement("typDefinition")) {
//					System.out.println("typDefinition");
					objects.add(parseTypeDefinition());
				}
				else if(_xmlStream.matchStartElement("attributgruppenDefinition")) {
//					System.out.println("attributgruppenDefinition");
					objects.add(parseAttributegroupDefinition());
				}
				else if(_xmlStream.matchStartElement("transaktionsDefinition")) {
//					System.out.println("transaktionsDefinition");
					objects.add(parseTransactionDefinition());
				}
				else if(_xmlStream.matchStartElement("aspektDefinition")) {
//					System.out.println("aspektDefinition");
					objects.add(parseAspectDefinition());
				}
				else if(_xmlStream.matchStartElement("attributDefinition")) {
//					System.out.println("attributDefinition");
					objects.add(parseAttributDefinition());
				}
				else if(_xmlStream.matchStartElement("attributlistenDefinition")) {
//					System.out.println("attributlistenDefinition");
					objects.add(parseAttributeListDefinition());
				}
				else if(_xmlStream.matchStartElement("attributauswahlDefinition")) {
//					System.out.println("attributauswahlDefinition");
					// Wird nicht unterstützt
					ignoreElementStructureAndWarn();
				}
				else if(_xmlStream.matchStartElement("mengenDefinition")) {
//					System.out.println("mengenDefinition");
					objects.add(parseSetDefinition());
				}
				else {
					ignoreElementStructureAndWarn();
				}
			}
			_xmlStream.pullEndElement();
		}
		if(_xmlStream.matchStartElement("objekte")) {
			_xmlStream.pullStartElement();
			while(_xmlStream.matchStartElement()) {
				if(_xmlStream.matchStartElement("konfigurationsObjekt")) {
					objects.add(parseConfigurationObject());
				}
				else {
					ignoreElementStructureAndWarn();
				}
			}
			_xmlStream.pullEndElement();
		}

		_xmlStream.pullEndElement();
		return configurationAreaProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "typDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei eingelesen um
	 * ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseTypeDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();
		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");
		final String objectNamePermanent = attributes.getValue("objektNamenPermanent");
		final String persitenceMode = attributes.getValue("persistenzModus");
		final SystemObjectInfo info = parseInfo();

		final SystemObjectTypeProperties typeProperties = new SystemObjectTypeProperties(name, pid, 0, "", info);

		typeProperties.setObjectNamesPermanent(objectNamePermanent);
		typeProperties.setPersistenceMode(persitenceMode);

		if(_xmlStream.matchStartElement("erweitert")) {
			final List<String> extendedPids = new ArrayList<String>();

			while(_xmlStream.matchStartElement("erweitert")) {
				final AttributeMap attributesExtends = _xmlStream.pullStartElement().getAttributes();
				// Pid aus Attribut Erweitert einlesen
				extendedPids.add(attributesExtends.getValue("pid"));
				_xmlStream.pullEndElement();
			}
			// Es wurden alle Werte eingelesen
			typeProperties.setExtendedPids(extendedPids.toArray(new String[extendedPids.size()]));
		}
		else if(_xmlStream.matchStartElement("basis")) {
			// Konfigurierend setzen
			typeProperties.setConfiguring(_xmlStream.pullStartElement().getAttributes().getValue("konfigurierend"));
			_xmlStream.pullEndElement();
		}
		else {
			ignoreElementStructureAndWarn();
		}

		// Speichert ATG und Menge (ATG = Pid, Set = ConfigurationSet)
		final List<Object> setAndATG = new ArrayList<Object>();
		// speichert alle Default-Parameter
		final List<ConfigurationDefaultParameter> defaultParameters = new ArrayList<ConfigurationDefaultParameter>();
		while(_xmlStream.matchStartElement("menge") || _xmlStream.matchStartElement("attributgruppe") || _xmlStream.matchStartElement("transaktion")
		      || _xmlStream.matchStartElement("defaultParameter")) {

			if(_xmlStream.matchStartElement("attributgruppe") || _xmlStream.matchStartElement("transaktion")) {
				// Verweise auf Transaktionen werden genau wie Verweise auf Attributgruppen behandelt.
				// Eigentlich ist eine Transaktionsdefinition eine Attributgruppe.
				setAndATG.add(_xmlStream.pullStartElement().getAttributes().getValue("pid"));
				_xmlStream.pullEndElement();
			}
			else if(_xmlStream.matchStartElement("menge")) {
				final AttributeMap setAttributes = _xmlStream.pullStartElement().getAttributes();
				final ConfigurationSet set = new ConfigurationSet(setAttributes.getValue("pid"), setAttributes.getValue("name"));
				set.setRequired(setAttributes.getValue("erforderlich"));
				set.setInfo(parseInfo());
				setAndATG.add(set);
				_xmlStream.pullEndElement();
			}
			else if(_xmlStream.matchStartElement("defaultParameter")) {
				final AttributeMap defaultAttributes = _xmlStream.pullStartElement().getAttributes();
				final String pidType = defaultAttributes.getValue("typ");
				final String pidAtg = defaultAttributes.getValue("attributgruppe");

				final List<DatasetElement> dateAndDataListAndDateField = parseDatasetElements();
				// Default-Parameter-Datensatz zusammenstellen
				final ConfigurationDefaultParameter defaultParameter = new ConfigurationDefaultParameter(pidType, pidAtg);
				defaultParameter.setDataAndDataListAndDataField(dateAndDataListAndDateField.toArray(new DatasetElement[dateAndDataListAndDateField.size()]));
				defaultParameters.add(defaultParameter);
				_xmlStream.pullEndElement();
			}
		} // while (_xmlStream.matchStartElement("menge") || _xmlStream.matchStartElement("attributgruppe")) || _xmlStream.matchStartElement("defaultParameter"))

		// Die gelesenen Objekte dem Objekt "typeProperties" zuweisen
		typeProperties.setAtgAndSet(setAndATG.toArray(new Object[setAndATG.size()]));
		typeProperties.setDefaultParameters(defaultParameters.toArray(new ConfigurationDefaultParameter[defaultParameters.size()]));

		_xmlStream.pullEndElement();
		return typeProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "attributgruppenDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei
	 * eingelesen um ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseAttributegroupDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();

		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");

		// Attribut konfigurierend wird nicht mehr benötigt und deshalb ignoriert.
		final String configuring = attributes.getValue("konfigurierend");
		final String parametrierend = attributes.getValue("parametrierend");
		// Code wird nicht mehr benötigt und darum ignoriert
//		final String code = attributes.getValue("code");

		final SystemObjectInfo info = parseInfo();

		AttributeGroupProperties attributeGroupProperties = new AttributeGroupProperties(name, pid, 0, "", info);

		attributeGroupProperties.setConfiguring(configuring);
		attributeGroupProperties.setParameter(parametrierend);
		final boolean isParameter = attributeGroupProperties.isParameter();

		// Code wird nicht mehr benötigt und darum ignoriert
//		attributeGroupProperties.setCode(code);

		if(_xmlStream.matchStartElement("aspekt")) {
			List<ConfigurationAspect> aspects = new ArrayList<ConfigurationAspect>();
			while(_xmlStream.matchStartElement("aspekt")) {
				final AttributeMap attributesAspect = _xmlStream.pullStartElement().getAttributes();
				ConfigurationAspect configurationAspect = new ConfigurationAspect(attributesAspect.getValue("pid"));
				configurationAspect.setInfo(parseInfo());

				// Es muss ein konfigurationsModus oder ein onlineModus gesetzt sein.
				// Ist beides gesetzt, muss ein Fehler ausgegeben werden.
				// Ist beides nicht gesetzt muss ein Fehler ausgegeben werden.

				final String configurationMode = attributesAspect.getValue("konfigurationsModus");
				final String onlineMode = attributesAspect.getValue("onlineModus");

				if(!"".equals(configurationMode) && "".equals(onlineMode)) {
					// Nur der konfigurationsModus wurde gesetzt
					configurationAspect.setUsage(configurationMode);
				}
				else if("".equals(configurationMode) && !"".equals(onlineMode)) {
					// Nur der onlineModus wurde gesetzt
					configurationAspect.setUsage(onlineMode);
				}
				else if("".equals(configurationMode) && "".equals(onlineMode)) {
					// Beide Werte wurden nicht gesetzt - der Wert kann nicht mehr hergeleitet werden, da das Attribut konfigurierend nicht mehr existiert.
					throw new SAXException(
							"In der Attributgruppendefinition, pid " + pid
							+ " wurde weder der konfigurationsModus noch der onlineModus angegeben. Betroffener Aspekt, pid " + attributesAspect.getValue("pid")
					);

					// Wenn konfigurierend auf "ja" gesetzt wurde, wird datensatzOptional gewählt.
					// Wenn konfigurierend auf "nein" gesetzt wurde (oder der default-Wert benutzt wird), dann wird quelleUndSenke benutzt
//					if (attributeGroupProperties.getConfiguring()) {
//						// entspricht "ja"
//						configurationAspect.setUsage(AttributeGroupUsage.Usage.OptionalConfigurationData);
//					} else {
//						// entspricht "nein"
//						configurationAspect.setUsage(AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain);
//					}
				}
				else {
					// Es wurden zwei Werte angegeben
					throw new SAXException(
							"In der Attributgruppendefinition, pid " + pid
							+ " wurde sowohl für den konfigurationsModus als auch für den onlineModus ein Wert festgelegt festgelegt. Betroffener Aspekt, pid "
							+ attributesAspect.getValue("pid")
					);
				}
				_xmlStream.pullEndElement();
				aspects.add(configurationAspect);
			}
			attributeGroupProperties.setConfigurationAspect(aspects.toArray(new ConfigurationAspect[aspects.size()]));
		}

		// Speichert Attribute und Attributlisten
		final ArrayList<AttributeProperties> attributeAndAttributeList = new ArrayList<AttributeProperties>();
		boolean hasSeenInitiatorAndCauseAttribute = false;
		while(_xmlStream.matchStartElement("attribut") || _xmlStream.matchStartElement("attributliste")) {
			final AttributeProperties attributeProperties;
			if(_xmlStream.matchStartElement("attribut")) {
				attributeProperties = parseAttribute();
			}
			else {
				attributeProperties = parseAttributeList();
			}
			if(isParameter && "Urlasser".equals(attributeProperties.getName())) {
				hasSeenInitiatorAndCauseAttribute = true;
				if(!"atl.urlasser".equals(attributeProperties.getAttributeTypePid())) {
					_debug.warning("Urlasser Attribut der Attributgruppe " + attributeGroupProperties.getPid() + " ist nicht vom Typ atl.urlasser");
				}
				if(attributeProperties.getMaxCount() != 1 || attributeProperties.getTargetValue() != TargetValue.FIX) {
					_debug.warning("Urlasser Attribut der Attributgruppe " + attributeGroupProperties.getPid() + " ist ein Array");
				}
			}
			attributeAndAttributeList.add(attributeProperties);
		}

		// Bei Parameterattributgruppen wird falls noch nicht vorhanden automatisch ein Urlasser-Attribut als erstes Attribut ergänzt
		if(isParameter && !hasSeenInitiatorAndCauseAttribute) {
			_debug.info("Urlasser-Attributliste wird in der Attributgruppe " + attributeGroupProperties.getPid() + " automatisch an erster Stelle eingefügt");
			final AttributeProperties syntheticInitiatorAndCauseAttribute = new ListAttributeProperties("atl.urlasser");
			syntheticInitiatorAndCauseAttribute.setName("Urlasser");
			syntheticInitiatorAndCauseAttribute.setMaxCount(1);
			syntheticInitiatorAndCauseAttribute.setTargetValue(TargetValue.FIX);
			syntheticInitiatorAndCauseAttribute.setInfo(SystemObjectInfo.UNDEFINED);
			attributeAndAttributeList.add(0, syntheticInitiatorAndCauseAttribute);
		}

		attributeGroupProperties.setAttributeAndAttributeList(attributeAndAttributeList.toArray(new AttributeProperties[attributeAndAttributeList.size()]));

		_xmlStream.pullEndElement();

		return attributeGroupProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "transaktionsDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei
	 * eingelesen um ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim Parsen der Datei
	 */
	private ConfigurationObjectProperties parseTransactionDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();

		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");

		final SystemObjectInfo info = parseInfo();

		final TransactionProperties transactionProperties = new TransactionProperties(name, pid, 0, "", info);

		if(_xmlStream.matchStartElement("aspekt")) {
			final List<ConfigurationAspect> aspects = new ArrayList<ConfigurationAspect>();
			while(_xmlStream.matchStartElement("aspekt")) {
				final AttributeMap attributesAspect = _xmlStream.pullStartElement().getAttributes();
				final ConfigurationAspect configurationAspect = new ConfigurationAspect(attributesAspect.getValue("pid"));
				configurationAspect.setInfo(parseInfo());

				final String configurationMode = attributesAspect.getValue("konfigurationsModus");
				final String onlineMode = attributesAspect.getValue("onlineModus");

				if(!"".equals(configurationMode)) {
					throw new SAXException(
							"In der Transaktionsdefinition, pid " + pid
							+ " wurde ein konfigurationsModus angegeben. Transaktionen können nur im onlineModus definiert werden."
							+ " Betroffener Aspekt, pid " + attributesAspect.getValue("pid")
					);
				}
				else if("".equals(onlineMode)) {
					throw new SAXException(
							"In der Transaktionsdefinition, pid " + pid
							+ " wurde kein onlineModus angegeben. Betroffener Aspekt, pid " + attributesAspect.getValue("pid")
					);
				}
				else {
					configurationAspect.setUsage(onlineMode);
				}
				_xmlStream.pullEndElement();
				aspects.add(configurationAspect);
			}
			transactionProperties.setConfigurationAspect(aspects.toArray(new ConfigurationAspect[aspects.size()]));
		}

		if(_xmlStream.matchStartElement("akzeptiert")) {
			_xmlStream.pullStartElement();
			// Alle Elemente(Pids) einlesen
			final List<TransactionProperties.DataIdentification> dids = parseTransactionConstraint();
			transactionProperties.setPossibleDids(dids);
			_xmlStream.pullEndElement();
		}

		if(_xmlStream.matchStartElement("benötigt")) {
			_xmlStream.pullStartElement();
			// Alle Elemente(Pids) einlesen
			final List<TransactionProperties.DataIdentification> dids = parseTransactionConstraint();
			transactionProperties.setRequiredDids(dids);
			_xmlStream.pullEndElement();
		}

		_xmlStream.pullEndElement();

		return transactionProperties;
	}

	private List<TransactionProperties.DataIdentification> parseTransactionConstraint() throws InterruptedException, SAXException {
		final List<TransactionProperties.DataIdentification> dids = new ArrayList<TransactionProperties.DataIdentification>();

		while(_xmlStream.matchStartElement("transaktionsEinschränkung")) {
			final AttributeMap attributesElement = _xmlStream.pullStartElement().getAttributes();
			dids.add(
					new TransactionProperties.DataIdentification(
							attributesElement.getValue("objektTyp"),
							attributesElement.getValue("attributGruppe"),
							attributesElement.getValue("aspekt"),
							attributesElement.getValue("nurTransaktionsObjekt")
					)
			);
			_xmlStream.pullEndElement();
		}
		return dids;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "aspektDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei eingelesen um
	 * ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseAspectDefinition() throws InterruptedException, SAXException {
		final AttributeMap aspectDefinitionAttributes = _xmlStream.pullStartElement().getAttributes();

		final String pid = aspectDefinitionAttributes.getValue("pid");
		final String name = aspectDefinitionAttributes.getValue("name");
		// Code wird nicht mehr benötigt
//		final String code = aspectDefinitionAttributes.getValue("code");
		final SystemObjectInfo info = parseInfo();
		AspectProperties aspectProperties = new AspectProperties(name, pid, 0, "", info);
		// Code wird nicht mehr benötigt
//		aspectProperties.setCode(code);
		_xmlStream.pullEndElement();

		return aspectProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "attributDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei eingelesen
	 * um ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseAttributDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();

		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");

		final SystemObjectInfo info = parseInfo();

//		System.out.println("pid: " + pid + " name " + name + " info " + info);


		AttributeTypeProperties attributeTypeProperties = new AttributeTypeProperties(name, pid, 0, "", info);

		if(_xmlStream.matchStartElement("zeichenkette")) {
			final AttributeMap attributesConfigString = _xmlStream.pullStartElement().getAttributes();
			ConfigurationString configurationString = new ConfigurationString(attributesConfigString.getValue("laenge"));
			
			_xmlStream.pullEndElement();
			attributeTypeProperties.setAttributeType(configurationString);
		}
		else if(_xmlStream.matchStartElement("ganzzahl")) {

			final AttributeMap attributesInteger = _xmlStream.pullStartElement().getAttributes();
			final ConfigurationIntegerDef integerDef = new ConfigurationIntegerDef();
			integerDef.setBits(attributesInteger.getValue("bits"));

			final int definedBitCount = integerDef.getBits();
			// Liste, die alle Bereiche und Zustände speichert
			final List<ConfigurationIntegerValueRange> regionAndState = new ArrayList<ConfigurationIntegerValueRange>();

			long maximum = 0;
			long minimum = 0;

			// Wenn die Anzahl Bits nicht vorgegeben ist, wird minimum und maximum aus den Zuständen und Bereichen ermittelt und die Anzahl Bits berechnet
			// Wenn die Anzahl Bits vorgegeben ist, wird das Maximum und Minimum berechnet und die definierten Zuständen und Wertebereich werden gegen diese
			// Werte geprüft.
			if(definedBitCount > 0) {
				maximum = Long.MAX_VALUE >> (64 - definedBitCount);
				minimum = Long.MIN_VALUE >> (64 - definedBitCount);
			}

			while(_xmlStream.matchStartElement()) {
				if(_xmlStream.matchStartElement("bereich")) {
					final AttributeMap configRegionAttributes = _xmlStream.pullStartElement().getAttributes();
					ConfigurationValueRange configurationValueRange = new ConfigurationValueRange();

					configurationValueRange.setScale(configRegionAttributes.getValue("skalierung"));
					if("".equals(configRegionAttributes.getValue("minimum"))) {
						// Es gibt keinen Wert für das Minimum. Also kann dieser Wert aus dem Attribut bits berechnet werden

						if(definedBitCount > 0) {
							configurationValueRange.setMinimum(minimum);
						}
						else {
							// Es wurde kein Wert angegeben, also kann nichts sinnvolles Berechnet werden
							throw new SAXException(
									"Für ein Element attributDefinition pid " + pid
									+ " kann für das Element ganzzahl kein gültiges minimum berechnet werden, da das Attribut bits nicht sinnvoll gesetzt wurde"
							);
						}
					}
					else {
						// Der Wert ist vorhanden
						configurationValueRange.setMinimum(configRegionAttributes.getValue("minimum"));
					}

					if("".equals(configRegionAttributes.getValue("maximum"))) {
						// Es gibt keinen Wert für das Maximum. Also kann dieser Wert aus dem Attribut bits berechnet werden

						if(definedBitCount > 0) {
							// Das Maximum kann berechnet werden. 2^(Bits-1) - 1
							configurationValueRange.setMaximum(maximum);
						}
						else {
							// Es wurde kein Wert angegeben, also kann nichts sinnvolles Berechnet werden
							throw new SAXException(
									"Für ein Element attributDefinition pid " + pid
									+ " kann für das Element ganzzahl kein gültiges maximum berechnet werden, da das Attribut bits nicht sinnvoll gesetzt wurde"
							);
						}
					}
					else {
						// Der Wert ist vorhanden
						configurationValueRange.setMaximum(configRegionAttributes.getValue("maximum"));
					}
					configurationValueRange.setUnit(configRegionAttributes.getValue("einheit"));
					configurationValueRange.setInfo(parseInfo());

					_xmlStream.pullEndElement();

					if(configurationValueRange.getMinimum() < minimum) {
						if(definedBitCount >= 0) {
							throw new SAXException(
									"Für das Element attributDefinition pid " + pid + " ist das vorgegebene Minimum (" + configurationValueRange.getMinimum()
									+ ") kleiner als der kleinste Wert (" + minimum + "), der mit der definierten Anzahl Bits (" + definedBitCount
									+ ") dargestellt werden kann"
							);
						}
						else {
							minimum = configurationValueRange.getMinimum();
						}
					}

					if(configurationValueRange.getMaximum() > maximum) {
						if(definedBitCount >= 0) {
							throw new SAXException(
									"Für das Element attributDefinition pid " + pid + " ist das vorgegebene Maximum (" + configurationValueRange.getMaximum()
									+ ") größer als der größte Wert (" + maximum + "), der mit der definierten Anzahl Bits (" + definedBitCount
									+ ") dargestellt werden kann"
							);
						}
						else {
							maximum = configurationValueRange.getMaximum();
						}
					}
					regionAndState.add(configurationValueRange);
				}
				else if(_xmlStream.matchStartElement("zustand")) {
					final AttributeMap configConditionsAttributes = _xmlStream.pullStartElement().getAttributes();
					ConfigurationState configurationState = new ConfigurationState(
							configConditionsAttributes.getValue("name"), configConditionsAttributes.getValue("wert")
					);
					configurationState.setInfo(parseInfo());

					if(configurationState.getValue() < minimum) {
						if(definedBitCount >= 0) {
							throw new SAXException(
									"Für das Element attributDefinition pid " + pid + " ist der Wertezustand " + configurationState.getName() + " ("
									+ configurationState.getValue() + ") kleiner als der kleinste Wert (" + minimum + "), der mit der definierten Anzahl Bits ("
									+ definedBitCount + ") dargestellt werden kann"
							);
						}
						else {
							minimum = configurationState.getValue();
						}
					}

					if(configurationState.getValue() > maximum) {
						if(definedBitCount >= 0) {
							throw new SAXException(
									"Für das Element attributDefinition pid " + pid + " ist der Wertezustand " + configurationState.getName() + " ("
									+ configurationState.getValue() + ") größer als der größte Wert (" + maximum + "), der mit der definierten Anzahl Bits ("
									+ definedBitCount + ") dargestellt werden kann"
							);
						}
						else {
							maximum = configurationState.getValue();
						}
					}

					_xmlStream.pullEndElement();
					regionAndState.add(configurationState);
				}
			} // while(_xmlStream.matchStartElement())

			// Falls bits nicht gesetzt wurde, stehen in den Variablen minimum und maximum Werte, mit denen bits
			// berechnet werden können
			if(definedBitCount <= 0) {
				if(minimum < Integer.MIN_VALUE || maximum > Integer.MAX_VALUE) {
					integerDef.setBits(8 * 8);
				}
				else if(minimum < Short.MIN_VALUE || maximum > Short.MAX_VALUE) {
					integerDef.setBits(4 * 8);
				}
				else if(minimum < Byte.MIN_VALUE || maximum > Byte.MAX_VALUE) {
					integerDef.setBits(2 * 8);
				}
				else {
					integerDef.setBits(1 * 8);
				}
			}

			// Ende für Attribute der Ganzzahl
			_xmlStream.pullEndElement();

			integerDef.setValueRangeAndState(regionAndState.toArray(new ConfigurationIntegerValueRange[regionAndState.size()]));

			// Ganzahl an die Attributdefinition
			attributeTypeProperties.setAttributeType(integerDef);
		}
		else if(_xmlStream.matchStartElement("zeitstempel")) {
			final AttributeMap attributesTimeStamp = _xmlStream.pullStartElement().getAttributes();
			ConfigurationTimeStamp timeStamp = new ConfigurationTimeStamp();
			timeStamp.setRelative(attributesTimeStamp.getValue("relativ"));
			timeStamp.setAccuracy(attributesTimeStamp.getValue("genauigkeit"));
			_xmlStream.pullEndElement();
			attributeTypeProperties.setAttributeType(timeStamp);
		}
		else if(_xmlStream.matchStartElement("objektReferenz")) {
			final AttributeMap attributesObjectReference = _xmlStream.pullStartElement().getAttributes();
			ConfigurationObjectReference objectReference = new ConfigurationObjectReference();
			objectReference.setReferenceObjectType(attributesObjectReference.getValue("typ"));
			objectReference.setUndefinedReferences(attributesObjectReference.getValue("undefiniert"));
			objectReference.setReferenceType(attributesObjectReference.getValue("referenzierungsart"));

			attributeTypeProperties.setAttributeType(objectReference);
			_xmlStream.pullEndElement();
		}
		else if(_xmlStream.matchStartElement("kommazahl")) {
			final AttributeMap attributesFloatingPointNumber = _xmlStream.pullStartElement().getAttributes();
			ConfigurationDoubleDef doubleDef = new ConfigurationDoubleDef();
			doubleDef.setUnit(attributesFloatingPointNumber.getValue("einheit"));
			doubleDef.setAccuracy(attributesFloatingPointNumber.getValue("genauigkeit"));
			_xmlStream.pullEndElement();
			attributeTypeProperties.setAttributeType(doubleDef);
		}
		else {
			

		}

		if(_xmlStream.matchStartElement("default")) {
			final AttributeMap attributesDefault = _xmlStream.pullStartElement().getAttributes();
			attributeTypeProperties.setDefault(attributesDefault.getValue("wert"));
			_xmlStream.pullEndElement();
		}

		_xmlStream.pullEndElement();
		return attributeTypeProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "attributlistenDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei
	 * eingelesen um ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseAttributeListDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();
		final String pid = attributes.getValue("pid");
		final String name = attributes.getValue("name");

		final SystemObjectInfo info = parseInfo();

		AttributeListProperties attributeListProperties = new AttributeListProperties(name, pid, 0, "", info);

		// Liste, die alle Attribute und Attributlisten enthält
		final List<AttributeProperties> attributesAndAttributeLists = new ArrayList<AttributeProperties>();

		while(_xmlStream.matchStartElement()) {
			if(_xmlStream.matchStartElement("attribut")) {
				attributesAndAttributeLists.add(parseAttribute());
			}
			else if(_xmlStream.matchStartElement("attributliste")) {
				attributesAndAttributeLists.add(parseAttributeList());
			}
			else {
				
				ignoreElementStructureAndWarn();
			}
		} // while (_xmlStream.matchStartElement())

		_xmlStream.pullEndElement();
		attributeListProperties.setAttributeAndAttributeList(attributesAndAttributeLists.toArray(new AttributeProperties[attributesAndAttributeLists.size()]));
		return attributeListProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "mengenDefinition" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei eingelesen um
	 * ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseSetDefinition() throws InterruptedException, SAXException {
		final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();

		final String pid = attributes.getValue("pid");
		final String elements = attributes.getValue("elemente");
		final String changeable = attributes.getValue("aenderbar");
		final String atLeast = attributes.getValue("mindestens");
		final String atMost = attributes.getValue("hoechstens");
		final String referenceType = attributes.getValue("referenzierungsart");

		final SystemObjectInfo info = parseInfo();

		ObjectSetTypeProperties objectSetTypeProperties = new ObjectSetTypeProperties("", pid, 0, "", info, elements);

		objectSetTypeProperties.setMutable(changeable);
		objectSetTypeProperties.setMinimum(atLeast);
		objectSetTypeProperties.setMaximum(atMost);
		objectSetTypeProperties.setReferenceType(referenceType);

		// Die Referenzierungsart muss bei Konfigurationsmengen angegeben sein, bei dynamischen Mengen wird sie auf
		// Assoziation gesetzt (dies wird in PuK gefordert).

		if(objectSetTypeProperties.getMutable()) {
			// Die Menge ist änderbar, somit ist sie automatisch eine dynamische Menge
			if(objectSetTypeProperties.getReferenceType() == null) {
				objectSetTypeProperties.setReferenceType(ReferenceType.ASSOCIATION);
			}
			else if(objectSetTypeProperties.getReferenceType() != ReferenceType.ASSOCIATION) {
				// Fehler
				throw new IllegalArgumentException(
						"Referenzierungsart bei der Mengendefinitaion einer dynamischen Menge ist fehlerhaft, erlaubt Assoziation, gewählt wurde "
						+ objectSetTypeProperties.getReferenceType() + " Pid " + pid
				);
			}
		}
		else {
			// Eine Konfigurationsmenge, hier muss die Referenzierungsart angegeben werden
			if(objectSetTypeProperties.getReferenceType() == null) {
				// Die Referenzierungsart wurde nicht gesetzt -> Fehler

				



				// Derzeit wird eine Ref-Art gesetzt und weitergemacht.
				_debug.warning("An einer Mengendefinition wurde keine Referenzierungsart gesetzt, Pid: " + pid);
				objectSetTypeProperties.setReferenceType(ReferenceType.ASSOCIATION);
			}
		}

		_xmlStream.pullEndElement();

		return objectSetTypeProperties;
	}

	/**
	 * Liest aus einer XML-Versorgungsdatei eine "konfigurationsObjekt" aus. Es wird das Anfangs und Endtag entfernt und alle Informationen aus der Datei
	 * eingelesen um ein Java-Objekt zu erzeugen.
	 *
	 * @return Objekt, das mit Import in die Konfiguration importiert werden kann
	 *
	 * @throws InterruptedException
	 * @throws SAXException         Fehler beim parsen der Datei
	 */
	private ConfigurationObjectProperties parseConfigurationObject() throws InterruptedException, SAXException {
		AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();
		final String pid = attributes.getValue("pid");
		final String id = attributes.getValue("id");
		long idValue = 0;
		final String trimmedId = id.trim();
		if(!trimmedId.equals("")) idValue = Long.valueOf(trimmedId);
		final String name = attributes.getValue("name");
		final String type = attributes.getValue("typ");
		final SystemObjectInfo info = parseInfo();
		final ConfigurationConfigurationObject configurationObjectProperties = new ConfigurationConfigurationObject(name, pid, idValue, type, info);

		_debug.fine("+++Bearbeiten von", configurationObjectProperties);
		final List<ConfigurationObjectElements> dataSetAndObjectSet = new ArrayList<ConfigurationObjectElements>();
		// speichert alle Default-Parameter
		final List<ConfigurationDefaultParameter> defaultParameters = new ArrayList<ConfigurationDefaultParameter>();
		while(_xmlStream.matchStartElement()) {
			if(_xmlStream.matchStartElement("datensatz")) {

				final AttributeMap attributesDataset = _xmlStream.pullStartElement().getAttributes();

				// Es gibt 2 Möglichkeiten einen Datensatz zu definieren.
				// 1) Über die Pid der ATG (Attribut "pid") (veraltet)
				// 2) Über die Attribute "attributgruppe" und "aspekt" (neu)

				// Pid der Attributgruppe, dies wird in beiden Fällen benötigt, aber unterschiedlich eingelesen
				String pidDataSet = attributesDataset.getValue("attributgruppe");
				// Pid des zu nutzenden Aspekts
				String pidAspect = attributesDataset.getValue("aspekt");

				
				if("".equals(pidDataSet)) {
					// Die Attributgruppe soll über den alten Weg 1) definiert werden.
					// Der Aspekt muss nicht eingelesen werden
					pidDataSet = attributesDataset.getValue("pid");
				}

				final List<DatasetElement> dateAndDataListAndDateField = parseDatasetElements();

				// Ende für Datensatz
				_xmlStream.pullEndElement();
				final ConfigurationDataset configurationDataset = new ConfigurationDataset(pidDataSet, pidAspect);
				// eingelesene Elemente
				configurationDataset.setDataAndDataListAndDataField(dateAndDataListAndDateField.toArray(new DatasetElement[dateAndDataListAndDateField.size()]));
				// Das Element "datensatz" hinzufügen
				dataSetAndObjectSet.add(configurationDataset);
			}
			else if(_xmlStream.matchStartElement("defaultParameter")) {
				final AttributeMap defaultAttributes = _xmlStream.pullStartElement().getAttributes();
				final String pidType = defaultAttributes.getValue("typ");
				final String pidAtg = defaultAttributes.getValue("attributgruppe");

				final List<DatasetElement> dateAndDataListAndDateField = parseDatasetElements();
				// Default-Parameter-Datensatz zusammenstellen
				final ConfigurationDefaultParameter defaultParameter = new ConfigurationDefaultParameter(pidType, pidAtg);
				defaultParameter.setDataAndDataListAndDataField(dateAndDataListAndDateField.toArray(new DatasetElement[dateAndDataListAndDateField.size()]));
				defaultParameters.add(defaultParameter);
				_xmlStream.pullEndElement();
			}
			else if(_xmlStream.matchStartElement("objektMenge")) {
				final AttributeMap attributesObjectSet = _xmlStream.pullStartElement().getAttributes();
				final String nameObjectSet = attributesObjectSet.getValue("name");
				final String managementPid = attributesObjectSet.getValue("verwaltung");

				// Alle Elemente(Pids) einlesen
				final List<String> elementPids = new ArrayList<String>();

				while(_xmlStream.matchStartElement("element")) {
					final AttributeMap attributesElement = _xmlStream.pullStartElement().getAttributes();
					elementPids.add(attributesElement.getValue("pid"));
					_xmlStream.pullEndElement();
				}

				_xmlStream.pullEndElement();
				final ConfigurationObjectSet configurationObjectSet = new ConfigurationObjectSet(
						nameObjectSet, elementPids.toArray(new String[elementPids.size()]), managementPid
				);
				dataSetAndObjectSet.add(configurationObjectSet);
			}
			else {
				ignoreElementStructureAndWarn();
			}
		}
		configurationObjectProperties.setDatasetAndObjectSet(dataSetAndObjectSet.toArray(new ConfigurationObjectElements[dataSetAndObjectSet.size()]));
		configurationObjectProperties.setDefaultParameters(defaultParameters.toArray(new ConfigurationDefaultParameter[defaultParameters.size()]));

		_xmlStream.pullEndElement();
		return configurationObjectProperties;
	}

	private List<DatasetElement> parseDatasetElements() throws InterruptedException, SAXException {
		final List<DatasetElement> dateAndDataListAndDateField = new ArrayList<DatasetElement>();

		while(_xmlStream.matchStartElement()) {
			if(_xmlStream.matchStartElement("datum")) {
				dateAndDataListAndDateField.add(parseDate());
			}
			else if(_xmlStream.matchStartElement("datenliste")) {
				dateAndDataListAndDateField.add(parseDataList());
			}
			else if(_xmlStream.matchStartElement("datenfeld")) {
				dateAndDataListAndDateField.add(parseDataField());
			}
			else {
				ignoreElementStructureAndWarn();
			}
		}
		return dateAndDataListAndDateField;
	}

	/**
	 * Liest ein Element "info" ein, siehe K2S.DTD. Falls in der Beschreibung Tags zur Textformatierung vorhanden  sind, werden diese als String interpretiert und
	 * wie normaler Text eingelesen.
	 *
	 * @return Objekt, das ein Element "info" darstellt
	 *
	 * @throws SAXException         Fehler beim parsen
	 * @throws InterruptedException
	 */
	private SystemObjectInfo parseInfo() throws SAXException, InterruptedException {
		if(_xmlStream.matchStartElement("info")) {
			_xmlStream.pullStartElement();
			final String shortInfo = parseInfoText();
			final String description;

			if(_xmlStream.matchStartElement("beschreibung")) {

				// mit tags parsen
				description = parseInfoText();
			}
			else {
				description = "";
			}
			_xmlStream.pullEndElement();
			// Die Info wurde komplett ausgelesen, aber die Kurzinfo darf nicht leer sein.
			if("".equals(shortInfo)) throw new IllegalArgumentException("Zu einer Info ist nur eine leere Kurzinfo vorhanden");
			return new SystemObjectInfo(shortInfo, description);
		}
		else {
			return SystemObjectInfo.UNDEFINED;
		}
	}

	/**
	 * Liest die "konfigurationsAenderung" aus einer XML-Versorgungsdatei ein (mit Start und Endtag).
	 *
	 * @return Objekt, das mittels Import in die Konfiguration übernommen werden kann
	 *
	 * @throws SAXException         Fehler beim parsen
	 * @throws InterruptedException
	 */
	private ConfigurationAreaChangeInformation[] parseConfigurationChanges() throws SAXException, InterruptedException {

		final List<ConfigurationAreaChangeInformation> areaChangeInformations = new LinkedList<ConfigurationAreaChangeInformation>();

		while(_xmlStream.matchStartElement("konfigurationsAenderung")) {
			// Startelement
			final AttributeMap attributes = _xmlStream.pullStartElement().getAttributes();

			// Elemente auslesen, dies ist Text. Es wird solange alles eingelesen, bis das Endetag </konfigurationsAenderung> auftaucht
			final StringBuilder textBuffer = new StringBuilder();
			while(true) {
				if(_xmlStream.matchStartElement()) {
					StartElementEvent startElementEvent = _xmlStream.pullStartElement();
					textBuffer.append("<" + startElementEvent.getLocalName());
					final AttributeMap attributeMap = startElementEvent.getAttributes();
					if(attributeMap.size() > 0) {
						// Es gibt Attribute
						String attributeNames[] = attributeMap.getNames();
						for(int nr = 0; nr < attributeNames.length; nr++) {
							textBuffer.append(" " + attributeNames[nr] + "=\"" + attributeMap.getValue(attributeNames[nr]) + "\"");
						}
					}
					textBuffer.append(">");
				}
				else if(_xmlStream.matchCharacters()) {
					String text = _xmlStream.pullCharacters().getText();
//					text = Pattern.compile("\n").matcher(text).replaceAll("");
//					text = Pattern.compile("  ").matcher(text).replaceAll("");
//					System.out.println("geparster Text:" + text + ":Ende");
					textBuffer.append(text);
				}
				else if(_xmlStream.matchEndElement()) {
					if(_xmlStream.matchEndElement("konfigurationsAenderung")) {
						break;
					}
					else {
						// Ein anderes EndTag gelesen
						textBuffer.append("</" + _xmlStream.pullEndElement().getLocalName() + ">");
					}
				}
			}
			// Ende konfigurationsAenderung
			_xmlStream.pullEndElement();

			// Es stehen alle Informationen zur Verfügung um ein Objekt zu erzeugen
			areaChangeInformations.add(
					new ConfigurationAreaChangeInformation(
							attributes.getValue("stand"),
							attributes.getValue("version"),
							attributes.getValue("autor"),
							attributes.getValue("grund"),
							textBuffer.toString()
					)
			);
		}// while

		return areaChangeInformations.toArray(new ConfigurationAreaChangeInformation[areaChangeInformations.size()]);
	}


	/**
	 * Liest die Kurzinformation oder die Beschreibung ein. Alles zwischen dem Starttag <code>&lt;kurzinfo&gt; bzw. &lt;beschreibung&gt;</code> und dem Endtag
	 * <code>&lt;/kurzinfo&gt; bzw. &lt;/beschreibung&gt;</code> wird als Text behandelt und steht im erzeugten String.
	 *
	 * @return String, der auch HTML/XML-Tags enthalten kann
	 *
	 * @throws SAXException
	 * @throws InterruptedException
	 */
	private String parseInfoText() throws SAXException, InterruptedException {

		_xmlStream.pullStartElement();
		final StringBuilder textBuffer = new StringBuilder();

		while(true) {
			if(_xmlStream.matchIgnorableCharacters()) {
				String text = _xmlStream.pullIgnorableCharacters().getText();
				text = Pattern.compile("&").matcher(text).replaceAll("&amp;");
				text = Pattern.compile("<").matcher(text).replaceAll("&lt;");
				text = Pattern.compile(">").matcher(text).replaceAll("&gt;");
				textBuffer.append(text);
			}
			else if(_xmlStream.matchStartElement()) {
				StartElementEvent startElementEvent = _xmlStream.pullStartElement();
				textBuffer.append("<" + startElementEvent.getLocalName());
				final AttributeMap attributeMap = startElementEvent.getAttributes();
				if(attributeMap.size() > 0) {
					// Es gibt Attribute
					String attributeNames[] = attributeMap.getNames();
					for(int nr = 0; nr < attributeNames.length; nr++) {
						textBuffer.append(" " + attributeNames[nr] + "=\"" + attributeMap.getValue(attributeNames[nr]) + "\"");
					}
				}
				textBuffer.append(">");
			}
			else if(_xmlStream.matchCharacters()) {
				String text = _xmlStream.pullCharacters().getText();
				text = Pattern.compile("&").matcher(text).replaceAll("&amp;");
				text = Pattern.compile("<").matcher(text).replaceAll("&lt;");
				text = Pattern.compile(">").matcher(text).replaceAll("&gt;");
				textBuffer.append(text);
//				textBuffer.append(_xmlStream.pullCharacters().getText());
			}
			else if(_xmlStream.matchEndElement()) {
				if(_xmlStream.matchEndElement("beschreibung") || _xmlStream.matchEndElement("kurzinfo")) {
					break;
				}
				else {
					// Ein anderes EndTag gelesen
					textBuffer.append("</" + _xmlStream.pullEndElement().getLocalName() + ">");
				}
			}
		}

		//</beschreibung> oder </kurzinfo>
		_xmlStream.pullEndElement();

		return textBuffer.toString();
	}

	/**
	 * Ignoriert ein Element der XML-Versorgungsdatei.
	 *
	 * @return
	 *
	 * @throws SAXException         Fehler beim parsen
	 * @throws InterruptedException
	 */
	private StartElementEvent ignoreElementStructure() throws SAXException, InterruptedException {
		final StartElementEvent startElement = _xmlStream.pullStartElement();
		while(true) {
			if(_xmlStream.matchStartElement()) {
				ignoreElementStructure();
			}
			else if(_xmlStream.matchCharacters()) {
				_xmlStream.pullCharacters();
			}
			else if(_xmlStream.matchEndElement()) {
				break;
			}
		}
		_xmlStream.pullEndElement();
		return startElement;
	}

	/**
	 * Ignoriert ein Element der XML-Versorgungsdatei und gibt eine Warnung aus.
	 *
	 * @throws SAXException         Fehler beim parsen
	 * @throws InterruptedException
	 */
	private void ignoreElementStructureAndWarn() throws SAXException, InterruptedException {
		final StartElementEvent startElement = ignoreElementStructure();
		_debug.warning(
				"Element <" + startElement.getLocalName() + "> mit Attributen " + startElement.getAttributes() + " wird nicht unterstützt und ignoriert"
		);
	}

/*
	private String parseSimpleTextElement() throws InterruptedException, SAXException {
		final StringBuilder textBuffer = new StringBuilder();
		_xmlStream.pullStartElement();
		while(_xmlStream.matchCharacters()) {
			textBuffer.append(_xmlStream.pullCharacters().getText());
		}
		_xmlStream.pullEndElement();
		return textBuffer.toString();
	}

	private String parseTextStructure() throws InterruptedException, SAXException {
		final StringBuilder textBuffer = new StringBuilder();
		parseTextStructure(textBuffer);
		return textBuffer.toString();
	}

	private void parseTextStructure(StringBuilder textBuffer) throws SAXException, InterruptedException {
		_xmlStream.pullStartElement();
		while(true) {
			if(_xmlStream.matchCharacters()) {
				textBuffer.append(_xmlStream.pullCharacters().getText());
			}
			else if(_xmlStream.matchStartElement()) {
				parseTextStructure(textBuffer);
			}
			else {
				break;
			}
		}
		_xmlStream.pullEndElement();
	}

	private String parseStyledTextStructure() throws InterruptedException, SAXException {
		final StringBuilder textBuffer = new StringBuilder();
		if(parseStyledTextStructure(textBuffer)) {
			// textBuffer enthält HTML-Formatierungen, deshalb mit "<html><body>" und "</body></html>" einklammern
			textBuffer.insert(0, "<html><body>").append("</body></html>");
		}
		return textBuffer.toString();
	}

	private boolean parseStyledTextStructure(StringBuilder textBuffer) throws SAXException, InterruptedException {
		// rs: K2S Textauszeichnungen in entsprechende HTML Formatierungen umsetzen
		boolean hasFormatting = false;
		_xmlStream.pullStartElement();
		while(true) {
			if(_xmlStream.matchCharacters()) {
				textBuffer.append(_xmlStream.pullCharacters().getText());
			}
			else if(_xmlStream.matchStartElement()) {
				if(_xmlStream.matchStartElement("wichtig")) {
					textBuffer.append("<b>");
					parseStyledTextStructure(textBuffer);
					textBuffer.append("</b>");
					hasFormatting = true;
				}
				else {
					hasFormatting |= parseStyledTextStructure(textBuffer);
				}
			}
			else {
				break;
			}
		}
		_xmlStream.pullEndElement();
		return hasFormatting;
	}
*/

	/**
	 * Liest ein "datum" aus der XML-Datei
	 *
	 * @return "datum" als Java-Objekt
	 */
	private ConfigurationData parseDate() throws SAXException, InterruptedException {
		final AttributeMap attributesDate = _xmlStream.pullStartElement().getAttributes();
		final String nameDate = attributesDate.getValue("name").trim();
		final String valueDate = attributesDate.getValue("wert");
		final ConfigurationData configurationData = new ConfigurationData(nameDate, valueDate);
		_xmlStream.pullEndElement();
		return configurationData;
	}

	/**
	 * Liest ein "datenliste" Objekt aus der XML-Datei und gibt es als Java-Objekt zurück.
	 *
	 * @return s.o.
	 */
	private ConfigurationDataList parseDataList() throws SAXException, InterruptedException {
		final AttributeMap attributesDatalist = _xmlStream.pullStartElement().getAttributes();
		final String nameDataList = attributesDatalist.getValue("name").trim();

		// Elemente des Objekts laden
		final List<DatasetElement> datalistElements = parseDatasetElements();
		_xmlStream.pullEndElement();
		return new ConfigurationDataList(datalistElements.toArray(new DatasetElement[datalistElements.size()]), nameDataList);
	}

	/**
	 * Liest ein "datenfeld"-Objekt aus einer XML-Datei und erzeugt daraus ein Java-Objekt.
	 *
	 * @return s.o.
	 */
	private ConfigurationDataField parseDataField() throws SAXException, InterruptedException {
		final AttributeMap attributesDataField = _xmlStream.pullStartElement().getAttributes();
		final String nameDataField = attributesDataField.getValue("name");

		final List<DatasetElement> dataFieldElements = new ArrayList<DatasetElement>();
		while(_xmlStream.matchStartElement()) {
			if(_xmlStream.matchStartElement("datum")) {
				dataFieldElements.add(parseDate());
			}
			else if(_xmlStream.matchStartElement("datenliste")) {
				dataFieldElements.add(parseDataList());
			}
			else {
				ignoreElementStructureAndWarn();
			}
		}
		_xmlStream.pullEndElement();

		return new ConfigurationDataField(nameDataField, dataFieldElements.toArray(new DatasetElement[dataFieldElements.size()]));
	}

	/**
	 * Erzeugt ein attribut Objekt, siehe K2S.DTD. Das Start/End-Tag werden entfernt und alle Attribute und Elemente ausgewertet.
	 *
	 * @return Objekt, das einem attribut (siehe K2S.DTD) entspricht
	 */
	private PlainAttributeProperties parseAttribute() throws SAXException, InterruptedException {
		final AttributeMap attributesAttribute = _xmlStream.pullStartElement().getAttributes();

		final PlainAttributeProperties attribute = new PlainAttributeProperties(attributesAttribute.getValue("pid"));
		// Ein String, da der Wert auch nicht gesetzt sein muss
		final String maxCountString = attributesAttribute.getValue("anzahl");
		// Die Variable ist immer gesetzt, da der default-Wert "fest" ist
		attribute.setTargetValue(attributesAttribute.getValue("anzahlIst"));

		// Wie das attribut "anzahl" gesetzt wird, hängt davon ab, ob es bereits einen Wert enthält oder
		// ob kein Wert gesetzt wurde. Wurde kein Wert gesetzt, so wird der Wert mit dem Attribut "anzahlIst"
		// bestimmt.

		if(!"".equals(maxCountString)) {
			// Es wurde ein Wert festgelegt
			attribute.setMaxCount(maxCountString);
		}
		else {
			// Es wurde kein Wert festgelegt
			if(attribute.getTargetValue() == TargetValue.FIX) {
				attribute.setMaxCount(1);
			}
			else {
				// 0 steht für unbegrenzt
				attribute.setMaxCount(0);
			}
		}

		attribute.setName(attributesAttribute.getValue("name"));

		// Elemente einlesen
		attribute.setInfo(parseInfo());

		if(_xmlStream.matchStartElement("default")) {
			final AttributeMap attributesDefault = _xmlStream.pullStartElement().getAttributes();
			attribute.setDefault(attributesDefault.getValue("wert"));
			_xmlStream.pullEndElement();
		}
		_xmlStream.pullEndElement();
		return attribute;
	}

	/**
	 * Erzeugt ein attributListe Objekt, siehe K2S.DTD. Das Start/End-Tag werden entfernt und alle Attribute und Elemente ausgewertet.
	 *
	 * @return Objekt, das einem attributListe (siehe K2S.DTD) entspricht
	 */
	private ListAttributeProperties parseAttributeList() throws SAXException, InterruptedException {
		final AttributeMap attributeListAttributes = _xmlStream.pullStartElement().getAttributes();
		final ListAttributeProperties oneAttributeList = new ListAttributeProperties(attributeListAttributes.getValue("pid"));

		// Ein String, da der Wert auch nicht gesetzt sein muss
		final String maxCountString = attributeListAttributes.getValue("anzahl");
		// Die Variable ist immer gesetzt, da der default-Wert "fest" ist
		oneAttributeList.setTargetValue(attributeListAttributes.getValue("anzahlIst"));

		// Wie das attribut "anzahl" gesetzt wird, hängt davon ab, ob es bereits einen Wert enthält oder
		// ob kein Wert gesetzt wurde. Wurde kein Wert gesetzt, so wird der Wert mit dem Attribut "anzahlIst"
		// bestimmt.

		if(!"".equals(maxCountString)) {
			// Es wurde ein Wert festgelegt
			oneAttributeList.setMaxCount(maxCountString);
		}
		else {
			// Es wurde kein Wert festgelegt
			if(oneAttributeList.getTargetValue() == TargetValue.FIX) {
				oneAttributeList.setMaxCount(1);
			}
			else {
				// 0 steht für unbegrenzt
				oneAttributeList.setMaxCount(0);
			}
		}

		oneAttributeList.setName(attributeListAttributes.getValue("name"));
		oneAttributeList.setInfo(parseInfo());
		_xmlStream.pullEndElement();
		return oneAttributeList;
	}
}

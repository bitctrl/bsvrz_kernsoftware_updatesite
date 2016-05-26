/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Intefaces {@link de.bsvrz.dav.daf.main.config.TimeAttributeType} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigTimeAttributeType extends ConfigAttributeType implements TimeAttributeType {
	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Speichert die Eigenschaften des ZeitstempelAttributTypen.
	 */
	private TimeAttributeTypeValues _values;

	/**
	 * Konstruktor eines Zeitstempel-Attribut-Typs.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Zeitstempel-Attribut-Typs
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigTimeAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public boolean isRelative() {
		return getTimeAttributeTypeValues().isRelative();
	}

	public byte getAccuracy() {
		return getTimeAttributeTypeValues().getAccuracy();
	}

	/**
	 * Gibt das Objekt zurück, welches die Eigenschaften dieses AttributTyps enthält. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Eigenschaften des AttributTyps
	 */
	private synchronized TimeAttributeTypeValues getTimeAttributeTypeValues() {
		if (_values == null) {
			_values = new TimeAttributeTypeValues();
		}
		return _values;
	}

	/**
	 * Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. Erbende Klassen müssen diese
	 * Funktion überschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		_values = null;
	}

	public void setToUndefined(Data data) {
		if(isRelative())
		{
			UndefinedValueHandler.getInstance().setToUndefinedTimeRelative(data, getAccuracy());
		}else
		{
			UndefinedValueHandler.getInstance().setToUndefinedTimeAbsolute(data);
		}
	}

	public boolean isDefined(Data data) {
		if(isRelative())
		{
			return UndefinedValueHandler.getInstance().isDefinedTimeRelative(data, getAccuracy());
		}else
		{
			return UndefinedValueHandler.getInstance().isDefinedTimeAbsolute(data);
		}
	}

	/**
	 * Diese Klasse liest die Informationen für diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus und
	 * verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData}, da dort Informationen benötigt werden, die hier erst zur Verfügung gestellt werden.
	 */
	private class TimeAttributeTypeValues {
		/**
		 * gibt an, ob die Zeitangabe relativ ist, oder nicht
		 */
		private boolean _isRelative;

		/**
		 * die Genauigkeit der zeitlichen Auflösung dieses Zeitstempel-Attribut-Typs
		 */
		private byte _accuracy;

		/**
		 * Konstruktor, der die Eigenschaften dieses ZeitstempelAttributTyps aus einem konfigurierenden Datensatz ausliest.
		 */
		public TimeAttributeTypeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.zeitstempelAttributTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_isRelative = deserializer.readBoolean();
				_accuracy = deserializer.readByte();

				in.close();
			} catch (Exception ex) {
				final String errorMessage = "Die ZeitstempelAttributTypEigenschaften des AttributTyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt an, ob die Zeitangabe relativ ist, oder nicht.
		 *
		 * @return <code>true</code>, wenn die Zeitangabe relativ ist, sonst <code>false</code>
		 */
		public boolean isRelative() {
			return _isRelative;
		}

		/**
		 * Gibt die Genauigkeit der zeitlichen Auflösung dieses Zeitstempel-Attribut-Typs zurück.
		 *
		 * @return die Genauigkeit der zeitlichen Auflösung dieses Zeitstempel-Attribut-Typs
		 */
		public byte getAccuracy() {
			return _accuracy;
		}
	}
}

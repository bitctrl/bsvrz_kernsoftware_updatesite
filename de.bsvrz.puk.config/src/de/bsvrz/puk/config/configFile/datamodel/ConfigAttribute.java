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
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;

/**
 * Implementierung eines Attribut-Objekts auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAttribute extends ConfigConfigurationObject implements Attribute {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert die Attributeigenschaften dieses Attributs. */
	private AttributeValues _values = null;

	/** Default-Attributwert oder <code>null</code> falls nicht definiert. */
	private String _defaultAttributeValue;

	/**
	 * Konstruktor für ein KonfigurationsObjekt.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses KonfigurationsObjekts
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses KonfigurationsObjekts
	 */
	public ConfigAttribute(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
		loadDefaultAttributeValue();
	}

	public int getPosition() {
		return getAttributeValues().getPosition();
	}

	public boolean isCountLimited() {
		return getAttributeValues().getMaxCount() != 0;
	}

	public boolean isCountVariable() {
		return getAttributeValues().isCountVariable();
	}

	public int getMaxCount() {
		return getAttributeValues().getMaxCount();
	}

	public boolean isArray() {
		return getAttributeValues().isCountVariable() || (getAttributeValues().getMaxCount() != 1);
	}

	public AttributeType getAttributeType() {
		return getAttributeValues().getAttributeType();
	}

	/**
	 * Ermittelt den Default-Attributwert dieses Attribut.
	 *
	 * @return Default-Attributwert dieses Attribut oder <code>null</code> falls kein Defaultwert festgelegt wurde.
	 */
	public String getDefaultAttributeValue() {
		return _defaultAttributeValue;
	}

	/** Lädt den Default-Attributwert dieses Attributs aus einem konfigurierenden Datensatz. */
	public void loadDefaultAttributeValue() {
		// Default-Attributwert bestimmen und cachen
		try {
			final AttributeGroup atg = getDataModel().getAttributeGroup("atg.defaultAttributwert");
			final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
			final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
			byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

			if(bytes == null || bytes.length == 0) {
				_defaultAttributeValue = null;
			}
			else {
				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_defaultAttributeValue = deserializer.readString(32767);

				in.close();	// Stream schließen
			}
		}
		catch(IllegalArgumentException e) {
			// Datensatz nicht vorhanden
			_defaultAttributeValue = null;
		}
		catch(Exception ex) {
			final String errorMessage = "Der Default-Attributwert des Attributs " + getPidOrNameOrId() + " konnte nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new IllegalStateException(errorMessage, ex);
		}
	}

	/**
	 * Gibt das Objekt zurück, welches die Attributeigenschaften dieses Attributs enthält. Existiert es noch nicht, so wird es erzeugt und der entsprechende
	 * konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Attributeigenschaften
	 */
	private synchronized AttributeValues getAttributeValues() {
		if(_values == null) {
			_values = new AttributeValues();
		}
		return _values;
	}

	/** Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		_values = null;
	}

	/**
	 * Diese Klasse liest die Informationen für das Attribut mit Hilfe des Deserializers direkt aus dem Byte-Array des konfigurierenden Datensatzes aus. Dies geht
	 * nicht mit der Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData}, da dort Informationen benötigt werden, die hier erst zur Verfügung gestellt werden.
	 */
	private class AttributeValues {

		/** die Position eines Attributs oder einer Attributliste in der übergeordneten Attributmenge (Attributgruppe bzw. Attributliste) */
		private short _position;

		/** die maximale Feldgröße dieses Attributs */
		private int _maxCount;

		/** gibt an, ob die Feldgröße dieses Attributs variieren kann */
		private boolean _isCountVariable;

		/** der Typ des Attributs */
		private AttributeType _attributeType;

		/** Konstruktor, der die Attributeigenschaften aus einem konfigurierenden Datensatz ausliest. */
		public AttributeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.attributEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());
				assert bytes.length == 15 : "Länge des Byte-Arrays der Attributgruppe atg.attributEigenschaften hat sich geändert. Angenommene Länge = 15.";

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_position = deserializer.readShort();
				_maxCount = deserializer.readInt();
				_isCountVariable = deserializer.readBoolean();
				_attributeType = (AttributeType)deserializer.readObjectReference(getDataModel());

				in.close();	// Stream schließen
			}
			catch(Exception ex) {
				final String errorMessage = "Die Attributeigenschaften des Attributs " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die Position eines Attributs oder einer Attributliste in der übergeordneten Attributmenge (Attributgruppe bzw. Attributliste) zurück.
		 *
		 * @return die Position eines Attributs oder einer Attributliste
		 */
		public int getPosition() {
			return (int)_position;
		}

		/**
		 * Gibt die maximale Feldgröße dieses Attributs zurück.
		 *
		 * @return die maximale Feldgröße dieses Attributs
		 */
		public int getMaxCount() {
			return _maxCount;
		}

		/**
		 * Gibt zurück, ob die Feldgröße dieses Attributs variieren kann.
		 *
		 * @return <code>true</code>, wenn die Feldgröße dieses Attributs variieren kann, sonst <code>false</code>
		 */
		public boolean isCountVariable() {
			return _isCountVariable;
		}

		/**
		 * Gibt den Typ dieses Attributs zurück.
		 *
		 * @return der Typ dieses Attributs
		 */
		public AttributeType getAttributeType() {
			return _attributeType;
		}
	}
}

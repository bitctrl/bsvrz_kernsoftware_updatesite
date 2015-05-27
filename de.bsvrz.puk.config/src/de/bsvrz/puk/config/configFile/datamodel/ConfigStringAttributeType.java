/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.Aspect;

import java.io.ByteArrayInputStream;

/**
 * Implementierung des Interfaces {@link de.bsvrz.dav.daf.main.config.StringAttributeType} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 8550 $ / $Date: 2011-01-06 10:48:12 +0100 (Thu, 06 Jan 2011) $ / ($Author: jh $)
 */
public class ConfigStringAttributeType extends ConfigAttributeType implements StringAttributeType {
	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Speichert die Einstellungen f�r den ZeichenkettenAttributTyp.
	 */
	private StringAttributeTypeValues _values;

	/**
	 * Konstruktor eines Zeichenketten-Attribut-Typen.
	 *
	 * @param configurationArea Konfigurationsbereich dieses Zeichenketten-Attribut-Typen
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigStringAttributeType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public int getMaxLength() {
		return getStringAttributeTypeValues().getMaxLength();
	}

	public boolean isLengthLimited() {
		return getMaxLength() != 0;
	}

	public String getEncodingName() {
		byte encodingValue = getEncodingValue();
		switch (encodingValue) {
			case ISO_8859_1:
				return "ISO-8859-1";
			default:
				throw new IllegalStateException("Der Name der Kodierung (" + encodingValue + ") kann nicht ermittelt werden.");
		}
	}

	public byte getEncodingValue() {
		return getStringAttributeTypeValues().getEncodingValue();
	}

	/**
	 * Gibt das Objekt zur�ck, welches die Eigenschaften dieses AttributTyps enth�lt. Existiert es noch nicht, so wird es
	 * erzeugt und der entsprechende konfigurierende Datensatz ausgelesen.
	 *
	 * @return die Eigenschaften des AttributTyps
	 */
	private synchronized StringAttributeTypeValues getStringAttributeTypeValues() {
		if (_values == null) {
			_values = new StringAttributeTypeValues();
		}
		return _values;
	}

	public void setToUndefined(Data data) {
		UndefinedValueHandler.getInstance().setToUndefinedString(data);
	}

	public boolean isDefined(Data data) {
		return UndefinedValueHandler.getInstance().isDefinedString(data);
	}

	/**
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		_values = null;
	}

	/**
	 * Diese Klasse liest die Informationen f�r diesen Attributtypen mit Hilfe des Deserializers direkt aus dem Byte-Array
	 * des konfigurierenden Datensatzes aus und verwendet nicht die Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData
	 * ConfigurationData}, da dort Informationen ben�tigt werden, die erst hier zur Verf�gung gestellt werden.
	 */
	private class StringAttributeTypeValues {
		/**
		 * die maximale L�nge dieses Zeichenketten-Attribut-Typs
		 */
		private int _maxLength;

		/**
		 * die Kodierung der Zeichen dieses Zeichenketten-Attribut-Typs
		 */
		private byte _encodingValue;

		/**
		 * Konstruktor, der die Eigenschaften dieses ZeichenkettenAttributTyps aus einem konfigurierenden Datensatz ausliest.
		 */
		public StringAttributeTypeValues() {
			try {
				final AttributeGroup atg = getDataModel().getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften");
				final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
				final AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				byte[] bytes = _systemObjectInfo.getConfigurationData(attributeGroupUsage.getId());

				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

				_maxLength = deserializer.readInt();
				_encodingValue = deserializer.readByte();

				in.close();
			} catch (Exception ex) {
				final String errorMessage = "Die ZeichenkettenAttributTypEigenschaften des AttributTyps " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
				_debug.error(errorMessage, ex);
				throw new IllegalStateException(errorMessage, ex);
			}
		}

		/**
		 * Gibt die maximale L�nge dieses Zeichenketten-Attribut-Typs zur�ck.
		 *
		 * @return die maximale L�nge dieses Zeichenketten-Attribut-Typs
		 */
		public int getMaxLength() {
			return _maxLength;
		}

		/**
		 * Gibt die Kodierung der Zeichen dieses Zeichenketten-Attribut-Typs zur�ck.
		 *
		 * @return die Kodierung der Zeichen dieses Zeichenketten-Attribut-Typs
		 */
		public byte getEncodingValue() {
			return _encodingValue;
		}
	}
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Ergebnisdatensatz zum Zugriff auf ein empfangenes Datentelegramm und zum Erzeugen eines zu versendenden Datentelegramms. Neben den konkreten Attributwerten
 * der jeweiligen Attributgruppe enthält ein Ergebnisdatensatz Headerinformationen, die in jedem Datentelegramm enthalten sind wie z.B Fehlerstatus, laufende
 * Datensatznummer, Zeitstempel, Systemobjekt und Datenbeschreibung des Datensatzes sowie eine Kennzeichnung ob der Datensatz aktuell oder nachgeliefert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ResultData implements Dataset {

	/** Der Index dieses Datensatzes */
	private long dataIndex;

	/** Die Zeit dieses Datensatzes */
	private long time;

	/** Das System-Objekt zu dem die Daten gehören */
	private SystemObject object;

	/** Die 'Beschreibende Information' der zu versendenden Daten. */
	private DataDescription dataDescription;

	/** Gibt an, ob es sich um nachgelieferte Daten handelt. */
	private boolean delayedData;

	/** Liste der Attribute und Attributlisten-Werte */
	private List attributeValues;

	/** Der Datensatz. */
	private Data _data;

	/** Fehlerkennung der Anwendungsdaten. 0: Daten vorhanden (kein fehler). 1: Quelle vorhanden aber Daten noch nicht lieferbar. 2: Quelle nicht vorhanden. */
	private byte errorFlag;

	/**
	 * Erzeugt ein neues Ergebnis mit den übergebenen Eigenschaften.
	 *
	 * @param object          Das System-Objekt zu dem die Daten gehören.
	 * @param dataDescription Beschreibende Informationen der zu versendenden Daten
	 * @param delayedData     <code>true</code>, wenn der im Ergebnis enthaltene Datensatz als nachgeliefert gekennzeichnet werden soll.
	 * @param time            Zeitstempel dieses Datensatzes in Millisekunden seit 1970.
	 * @param attributeValues Liste mit den Attributwerten des Ergebnisses oder <code>null</code>, wenn kein Datensatz zur Verfügung gestellt werden kann.
	 *
	 * @deprecated Wurde ersetzt durch {@link #ResultData(SystemObject,DataDescription,long,Data,boolean)} bzw. durch {@link
	 *             #ResultData(de.bsvrz.dav.daf.main.config.SystemObject,DataDescription,long,Data)}
	 */
	@Deprecated
	public ResultData(SystemObject object, DataDescription dataDescription, boolean delayedData, long time, List attributeValues) {
		this(object, dataDescription, delayedData, time, attributeValues, null, null);
	}

	private ResultData(
			SystemObject object, DataDescription dataDescription, boolean delayedData, long time, List attributeValues, Data data, DataState dataState
	) {
		this.object = object;
		this.dataDescription = dataDescription;
		this.delayedData = delayedData;
		this.time = time;
		dataIndex = 0;
		if(attributeValues == null && data == null) {
			if(dataState == null) {
				errorFlag = 1;
			}
			else {
				errorFlag = (byte)(dataState.getCode() - 1);
			}
		}
		else {
			errorFlag = 0;
		}
		this.attributeValues = attributeValues;
		_data = data;
	}

	/**
	 * Erzeugt ein neues Ergebnis mit den übergebenen Eigenschaften. Die konkreten Attributwerte müssen in einem Data-Objekt zur Verfügung gestellt werden. Ein
	 * solches Datensatzobjekt kann mit der Methode {@link de.bsvrz.dav.daf.main.ClientDavInterface#createData} erzeugt werden.
	 *
	 * @param object          Das System-Objekt zu dem die Daten gehören.
	 * @param dataDescription Beschreibende Informationen der zu versendenden Daten
	 * @param time            Zeitstempel dieses Datensatzes in Millisekunden seit 1970.
	 * @param data            Datensatz mit den Attributwerten des Ergebnisses oder <code>null</code>, wenn kein Datensatz zur Verfügung gestellt werden kann.
	 * @param delayedData     <code>true</code>, wenn der im Ergebnis enthaltene Datensatz als nachgeliefert gekennzeichnet werden soll.
	 *
	 * @see de.bsvrz.dav.daf.main.ClientDavInterface#createData
	 */
	public ResultData(SystemObject object, DataDescription dataDescription, long time, Data data, boolean delayedData) {
		this(
				object,
				dataDescription,
				delayedData,
				time,
				data instanceof AttributeBaseValueDataFactory.AttributeGroupAdapter
				? ((AttributeBaseValueDataFactory.AttributeGroupAdapter)data)._attributeBaseValueList
				: null,
				data,
				null
		);
	}

	/**
	 * Erzeugt ein neues Ergebnis mit den übergebenen Eigenschaften. Die konkreten Attributwerte müssen in einem Data-Objekt zur Verfügung gestellt werden. Ein
	 * solches Datensatzobjekt kann mit der Methode {@link de.bsvrz.dav.daf.main.ClientDavInterface#createData} erzeugt werden.
	 *
	 * @param object          Das System-Objekt zu dem die Daten gehören.
	 * @param dataDescription Beschreibende Informationen der zu versendenden Daten
	 * @param time            Zeitstempel dieses Datensatzes in Millisekunden seit 1970.
	 * @param data            Datensatz mit den Attributwerten des Ergebnisses oder <code>null</code>, wenn kein Datensatz zur Verfügung gestellt werden kann.
	 * @param delayedData     <code>true</code>, wenn der im Ergebnis enthaltene Datensatz als nachgeliefert gekennzeichnet werden soll.
	 * @param dataState       Gibt den Zustand des Datensatzes an.
	 *
	 * @see de.bsvrz.dav.daf.main.ClientDavInterface#createData
	 */
	public ResultData(SystemObject object, DataDescription dataDescription, long time, Data data, boolean delayedData, DataState dataState) {
		this(
				object,
				dataDescription,
				delayedData,
				time,
				data instanceof AttributeBaseValueDataFactory.AttributeGroupAdapter
				? ((AttributeBaseValueDataFactory.AttributeGroupAdapter)data)._attributeBaseValueList
				: null,
				data,
				dataState
		);
	}

	/**
	 * Erzeugt ein neues Ergebnis mit den übergebenen Eigenschaften. Die konkreten Attributwerte müssen in einem Data-Objekt zur Verfügung gestellt werden. Ein
	 * solches Datensatzobjekt kann mit der Methode {@link de.bsvrz.dav.daf.main.ClientDavInterface#createData} erzeugt werden. Der erzeugte Datensatz wird nicht als nachgeliefert
	 * markiert.
	 *
	 * @param object          Das System-Objekt zu dem die Daten gehören.
	 * @param dataDescription Beschreibende Informationen der zu versendenden Daten
	 * @param time            Zeitstempel dieses Datensatzes in Millisekunden seit 1970.
	 * @param data            Datensatz mit den Attributwerten des Ergebnisses oder <code>null</code>, wenn kein Datensatz zur Verfügung gestellt werden kann.
	 */
	public ResultData(SystemObject object, DataDescription dataDescription, long time, Data data) {
		this(object, dataDescription, time, data, false);
	}

	/**
	 * Erzeugt ein neues Ergebnis mit den übergebenen Eigenschaften.
	 *
	 * @param object          Das System-Objekt zu dem die Daten gehören.
	 * @param dataDescription Beschreibende Informationen der zu versendenden Daten
	 * @param delayedData     <code>true</code>, wenn der im Ergebnis enthaltene Datensatz als nachgeliefert gekennzeichnet werden soll.
	 * @param dataIndex       die laufende Nummer des Datensatzes
	 * @param time            Zeitstempel dieses Datensatzes in Millisekunden seit 1970.
	 * @param errorFlag       Fehlerkennung der Anwendungsdaten
	 * @param data            Datensatz mit den Attributwerten des Ergebnisses oder <code>null</code>, wenn kein Datensatz zur Verfügung gestellt werden kann.
	 */
	public ResultData(
			SystemObject object,
			DataDescription dataDescription,
			boolean delayedData,
			long dataIndex,
			long time,
			byte errorFlag,
			Data data
	) {
		this.object = object;
		this.dataDescription = dataDescription;
		this.delayedData = delayedData;
		this.dataIndex = dataIndex;
		this.time = time;
		this.errorFlag = errorFlag;
		this.attributeValues = null;
		if(errorFlag == 0 && data == null) {
			throw new IllegalArgumentException("Attributargument ist leer");
		}
		_data = data;
	}

	/**
	 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten gehören.
	 *
	 * @return System-Objekt die enthaltenen Daten
	 */
	public final SystemObject getObject() {
		return object;
	}

	/**
	 * Bestimmt die Beschreibung der im Ergebnis enthaltenen Daten.
	 *
	 * @return Beschreibung der Daten
	 */
	public final DataDescription getDataDescription() {
		return dataDescription;
	}

	/**
	 * Bestimmt  ob es sich bei den im Ergebnis enthaltenen Daten um nachgelieferte Daten handelt.
	 *
	 * @return <code>true</code> bei Nachgelieferten Daten, sonst <code>false</code>.
	 */
	public final boolean isDelayedData() {
		return delayedData;
	}

	/**
	 * Bestimmt ob im Ergebnis ein Datensatz enthalten ist.
	 *
	 * @return <code>true</code> wenn ein Datensatz enthalten ist, sonst <code>false</code>.
	 */
	public final boolean hasData() {
		return (errorFlag == 0);
	}

	/**
	 * Bestimmt ob das Ergebnis von der Quelle generiert wurde.
	 *
	 * @return <code>true</code> wenn das Ergebnis von der Quelle generiert wurde, sonst <code>false</code>.
	 */
	public final boolean isSourceAvailable() {
		return errorFlag < 2;
	}

	/**
	 * Bestimmt den Zeitstempel dieses Datensatzes.
	 *
	 * @return Absolute Zeitangabe in Millisekunden seit dem 01.01.1970
	 */
	public final long getDataTime() {
		return time;
	}

	/**
	 * Setzt den Zeitstempel dieses Datensatzes.
	 *
	 * @param time Absolute Zeitangabe in Millisekunden seit dem 01.01.1970
	 */
	public final void setDataTime(long time) {
		this.time = time;
	}

	/**
	 * Bestimmt die laufende Nummer dieses Datensatzes. Mit der laufenden Nummer wird ein Datensatz eindeutig (je Objekt und Datenbeschreibung) identifiziert.
	 *
	 * @return Laufende Nummer des Datensatzes.
	 */
	public final long getDataIndex() {
		return dataIndex;
	}

	/**
	 * Bestimmt die Liste der Attributwerte des Ergebnisdatensatzes.
	 *
	 * @return Liste der Attributwerte oder <code>null</code>, wenn kein Datensatz im Ergebnis enthalten ist.
	 * @deprecated Wurde ersetzt durch {@link #getData()}
	 */
	@Deprecated
	public final List getAttributeValueList() {
		if(attributeValues != null) return attributeValues;
		if(_data == null) {
			return null;
		}
		Data data = _data.createModifiableCopy();
		return ((AttributeBaseValueDataFactory.AttributeGroupAdapter)data)._attributeBaseValueList;
	}

	/**
	 * Bestimmt den im Ergebnis enthaltenen Datensatz.
	 *
	 * @return Datensatz oder <code>null</code>, wenn kein Datensatz im Ergebnis enthalten ist.
	 */
	public final Data getData() {
		if(_data != null) return _data;
		if(attributeValues == null) return null;
		return AttributeBaseValueDataFactory.createAdapter(dataDescription.getAttributeGroup(), attributeValues);
	}

	/**
	 * Bestimmt ob keine Daten enthalten sind, weil die Quelle keine Daten ermitteln konnte.
	 *
	 * @return <code>true</code> wenn keine Daten vorliegen, weil die Quelle keine Daten ermitteln konnte, sonst <code>false</code>
	 */
	public final boolean isNoDataAvailable() {
		return errorFlag == 1;
	}

	/**
	 * Bestimmt ob keine Daten enthalten sind, weil die Quelle nicht verfügbar ist.
	 *
	 * @return <code>true</code> wenn keine Daten vorliegen, weil die Quelle nicht verfügbar ist, sonst <code>false</code>
	 */
	public final boolean isNoSourceAvailable() {
		return errorFlag == 2;
	}

	/**
	 * Bestimmt ob keine Daten enthalten sind, weil keine Rechte zum Zugriff vorlagen.
	 *
	 * @return <code>true</code> wenn keine Daten vorliegen, weil keine Rechte zum Zugriff vorlagen, sonst <code>false</code>
	 */
	public final boolean isNoRightsAvailable() {
		return errorFlag == 3;
	}

	/**
	 * Bestimmt ob keine Daten enthalten sind, weil die Anmeldung der Daten im Konflikt mit anderen Anmeldungen steht (z.B. mehrere Senken für die gleichen
	 * Daten).
	 *
	 * @return <code>true</code> wenn keine Daten vorliegen, weil die Anmeldung im Konflikt mit anderen Anmeldungen steht, sonst <code>false</code>
	 */
	public final boolean isNoValidSubscription() {
		return errorFlag == 8;
	}

	/**
	 * Gibt die Fehlerkennung der Anwendungsdaten zurück.
	 *
	 * @return <ul> <li>0: Daten vorhanden (Kein Fehler).</li> <li>1: Quelle vorhanden aber Daten (noch) nicht lieferbar.</li> <li>2: Quelle nicht vorhanden.</li>
	 *         <li>3: Keine Zugriffsrechte.</li> <li>8: Keine eindeutige Quelle vorhanden.</li> </ul>
	 */
	final byte getErrorFlag() {
		return errorFlag;
	}

	/**
	 * Liefert den Datensatzzustand dieses Datensatzes.
	 *
	 * @return Datensatzzustand dieses Datensatzes.
	 */
	public final DataState getDataState() {
		return DataState.getInstance(errorFlag + 1);
	}

	/**
	 * Liefert die Kodierung des Datensatztyps zurück. Ein dem Datensatztyp entsprechendes Objekt kann z.B. mit der Methode {@link
	 * de.bsvrz.dav.daf.main.DataState#getInstance(int)} erzeugt werden.
	 *
	 * @return Kodierung des Datensatztyps
	 *
	 * @deprecated Wurde ersetzt durch Methode {@link #getDataState()}.
	 */
	public int getDataTypeCode() {
		return errorFlag + 1;
	}

	/** Diese Methode gibt eine String-Repräsentation in die Standardausgabe aus. */
	public final void debug() {
		System.out.println("Object: " + object);
		System.out.println("AttributeGroup: " + dataDescription.getAttributeGroup());
		System.out.println("Aspect: " + dataDescription.getAspect());
		System.out.println("Simulation variant: " + dataDescription.getSimulationVariant());
		System.out.println("Time: " + time);
		System.out.println("Delayed: " + delayedData);
		System.out.println("ErrorFlag: " + errorFlag);
		if(attributeValues == null) {
			System.out.println("AttributeValues Null");
		}
		else {
			System.out.println("AttributeValues size: " + attributeValues.size());
			for(int i = 0; i < attributeValues.size(); ++i) {
				AttributeBaseValue value = (AttributeBaseValue)attributeValues.get(i);
				if(value != null) {
					System.out.println("Attribute: " + value.getName() + " -> " + ((DataValue)value.getValue()).parseToString());
				}
			}
		}
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück. Das genaue Format ist nicht festgelegt und kann sich ändern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		DateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
		return "ResultData{" + "dataIndex=" + (dataIndex >> 32) + "#" + ((dataIndex >> 2) & 0x3fffffff) + "#" + (dataIndex & 0x3)
		       + ", time=" + timeFormat.format(new Date(time)) + ", object=" + object + ", dataDescription=" + dataDescription + ", delayedData=" + delayedData
		       + ", errorFlag=" + errorFlag + ",\n data=" + getData() + "}";
	}

	/**
	 * Bestimmt den Datensatztyp dieses Datensatzes.
	 *
	 * @return Datensatztyp.
	 */
	public DataState getDataType() {
		return getDataState();
	}


	/**
	 * Bestimmt die Datensatzart dieses Datensatzes.
	 *
	 * @return Datensatzart.
	 */
	public ArchiveDataKind getDataKind() {
		return delayedData ? ArchiveDataKind.ONLINE_DELAYED : ArchiveDataKind.ONLINE;
	}
}

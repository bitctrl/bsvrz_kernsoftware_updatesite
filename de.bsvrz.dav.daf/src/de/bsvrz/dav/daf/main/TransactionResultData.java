/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.DataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Klasse, die einen Transaktionsdatensatz kapselt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransactionResultData implements Transaction {

	private final TransactionDataDescription _dataDescription;

	private final List<TransactionDataset> _data;

	private final DataState _dataState;

	private final ArchiveDataKind _dataKind;

	private final long _dataTime;

	private final long _dataIndex;

	/**
	 * Bestimmt die Beschreibung der im Ergebnis enthaltenen Daten.
	 *
	 * @return Beschreibung der Daten
	 */
	public TransactionDataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Bestimmt den Datenzeitstempel des Datensatzes.
	 *
	 * @return Datenzeitstempel in Millisekunden seit 1970
	 */
	public long getDataTime() {
		return _dataTime;
	}

	/**
	 * Bestimmt den je Datenidentifikation eindeutigen vom Datenverteiler vergebenen Datensatzindex dieses Datensatzes.
	 *
	 * @return Datensatzindex
	 */
	public long getDataIndex() {
		return _dataIndex;
	}

	public final DataState getDataType() {
		return _dataState;
	}

	/**
	 * Bestimmt die Datensatzart des Datensatzes.
	 *
	 * @return Datensatzart
	 */
	public ArchiveDataKind getDataKind() {
		return _dataKind;
	}

	public List<TransactionDataset> getData() {
		return _data == null ? Collections.<TransactionDataset>emptyList() : Collections.unmodifiableList(_data);
	}


	public final boolean hasData() {
		return (getDataType().equals(DataState.DATA));
	}

	/**
	 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten gehören.
	 *
	 * @return System-Objekt der enthaltenen Daten
	 */
	public SystemObject getObject() {
		return _dataDescription.getObject();
	}

	/**
	 * Erstellt einen neuen Transaktionsdatensatz aus einem ResultData. Das ResultData sollte ein Datensatz der Attributgruppe atg.transaktion sein.
	 * @param result ResultData
	 */
	public TransactionResultData(final ResultData result) {
		if(result == null) throw new IllegalArgumentException("result ist null");
		
		_dataState = result.getDataState();
		_dataKind = result.getDataKind();
		_dataDescription = new TransactionDataDescription(result.getObject(), result.getDataDescription());
		_dataTime = result.getDataTime();
		_dataIndex = result.getDataIndex();
		if(result.hasData()) {
			_data = parseData(result.getData());
		}
		else {
			_data = null;
		}
	}

	/**
	 * Erstellt einen neuen Transaktionsdatensatz aus einer Transaktionsdatenidentifikation und einer Liste mit inneren Datensätzen
	 * @param dataDescription Datenidentifikation
	 * @param data Datensätze
	 * @param dataTime
	 */
	public TransactionResultData(final TransactionDataDescription dataDescription, final Collection<ResultData> data, final long dataTime) {
		this(dataDescription, data, true, dataTime);
	}


	/**
	 * Erstellt einen neuen Transaktionsdatensatz aus einer Transaktionsdatenidentifikation und einer Liste mit inneren Datensätzen
	 *
	 * @param dataDescription      Datenidentifikation
	 * @param data                 Datensätze
	 * @param wasSentAsTransaction Kann auf false gesetzt werden um zu signalisieren, dass die Datensätze in data nicht als Transaktionsdatensatz verwendet wurden,
	 *                             sondern nachher vom Zentraldatenverteiler in einen neuen Transaktionsdatensatz eingefügt worden sind.
	 * @param dataTime
	 */
	public TransactionResultData(
			final TransactionDataDescription dataDescription, final Collection<ResultData> data, final boolean wasSentAsTransaction, final long dataTime) {

		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		if(data == null) throw new IllegalArgumentException("data ist null");

		_dataDescription = dataDescription;
		_data = new ArrayList<TransactionDataset>();
		_dataState = DataState.DATA;
		_dataKind = ArchiveDataKind.ONLINE;
		_dataTime = dataTime;
		_dataIndex = 0;
		for(final ResultData resultData : data) {
			_data.add(new TransactionInnerData(resultData, wasSentAsTransaction));
		}
	}

	/**
	 * Konvertiert den Transaktionsdatensatz in ein ResultData zum Versenden über den Datenverteiler. Die inneren Datensätze werden dabei serialisiert.
	 * @param connection Verbindung
	 * @return ResultData
	 */
	public ResultData getResultData(final ClientDavInterface connection) {
		if(connection == null) throw new IllegalArgumentException("connection ist null");
		
		return new ResultData(
				_dataDescription.getObject(),
				_dataDescription.getDataDescription(),
				_dataTime,
				makeData(_data, _dataDescription.getAttributeGroup(), connection)
		);
	}

	private List<TransactionDataset> parseData(final Data data) {
		final List<TransactionDataset> result = new ArrayList<TransactionDataset>(data.getItem("Transaktion").getArray("Datensatz").getLength());
		for(final Data dataset : data.getItem("Transaktion").getItem("Datensatz")) {
			result.add(readFromData(dataset));
		}
		return result;
	}

	private Data makeData(final Collection<TransactionDataset> data, final AttributeGroup attributeGroup, final ClientDavInterface connection) {
		final Data result = connection.createData(attributeGroup);
		final Data.Array innerDataArray = result.getItem("Transaktion").getArray("Datensatz");
		innerDataArray.setLength(data.size());
		int i = 0;
		for(final TransactionDataset dataset : data) {
			writeToData(dataset, innerDataArray.getItem(i));
			i++;
		}
		return result;
	}


	private TransactionDataset readFromData(final Data data) {
		final SystemObject object = data.getItem("Datenidentifikation").getReferenceValue("Objekt").getSystemObject();
		final AttributeGroup attributeGroup = (AttributeGroup)data.getItem("Datenidentifikation").getReferenceValue("Attributgruppe").getSystemObject();
		final Aspect aspect = (Aspect)data.getItem("Datenidentifikation").getReferenceValue("Aspekt").getSystemObject();
		final Data dataObject = deserializeData(data.getUnscaledArray("Daten").getByteArray(), attributeGroup);
		return new TransactionInnerData(
				object,
				attributeGroup,
				aspect,
				_dataDescription.getSimulationVariant(),
				data.getTimeValue("Datenzeit").getMillis(),
				data.getUnscaledValue("Datenindex").longValue(),
				getDataKind().isDelayed(),
				(dataObject != null) ? DataState.DATA : DataState.NO_DATA,
				data.getTextValue("AusTransaktion").getValueText().equals("Ja"),
				dataObject
		);
	}

	private void writeToData(final TransactionDataset transactionData, final Data data) {
		data.getItem("Datenidentifikation").getReferenceValue("Objekt").setSystemObject(transactionData.getObject());
		data.getItem("Datenidentifikation").getReferenceValue("Attributgruppe").setSystemObject(transactionData.getDataDescription().getAttributeGroup());
		data.getItem("Datenidentifikation").getReferenceValue("Aspekt").setSystemObject(transactionData.getDataDescription().getAspect());
		data.getTimeValue("Datenzeit").setMillis(transactionData.getDataTime());
		data.getUnscaledValue("Datenindex").set(transactionData.getDataIndex());
		data.getTextValue("AusTransaktion").setText(transactionData.wasSentAsTransaction()? "Ja" : "Nein");
		if(transactionData.getData() != null) {
			data.getUnscaledArray("Daten").set(serializeData(transactionData.getData()));
		}
	}


	private Data deserializeData(final byte[] dataBytes, final AttributeGroup attributeGroup) {
		if(dataBytes.length == 0) return null;
		return DataFactory.forVersion(1).createUnmodifiableData(attributeGroup, dataBytes);
	}

	private byte[] serializeData(final Data data) {
		return ((ByteArrayData)data.createUnmodifiableCopy()).getBytes();
	}

	@Override
	public String toString() {
		return "TransactionResultData{" + "dataDescription=" + _dataDescription + ", dataIndex=" + (_dataIndex >> 32) + "#" + ((_dataIndex >> 2) & 0x3fffffff)
		       + "#" + (_dataIndex & 0x3) + '}';
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final TransactionResultData that = (TransactionResultData)o;

		if(_dataIndex != that._dataIndex) return false;
		if(_dataTime != that._dataTime) return false;
		if(_data != null ? !_data.equals(that._data) : that._data != null) return false;
		if(_dataDescription != null ? !_dataDescription.equals(that._dataDescription) : that._dataDescription != null) return false;
		if(_dataKind != null ? !_dataKind.equals(that._dataKind) : that._dataKind != null) return false;
		if(_dataState != null ? !_dataState.equals(that._dataState) : that._dataState != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = _dataDescription != null ? _dataDescription.hashCode() : 0;
		result = 31 * result + (_data != null ? _data.hashCode() : 0);
		result = 31 * result + (_dataState != null ? _dataState.hashCode() : 0);
		result = 31 * result + (_dataKind != null ? _dataKind.hashCode() : 0);
		result = 31 * result + (int)(_dataTime ^ (_dataTime >>> 32));
		result = 31 * result + (int)(_dataIndex ^ (_dataIndex >>> 32));
		return result;
	}

	/**
	 * Ein innerer Datensatz. Entspricht einem ResultData mit der zusätzlichen Eigenschaft, ob der Datensatz als Transaktion versendet wurde oder nicht.
	 *
	 * @author Kappich Systemberatung
	 * @version $Revision$
	 */
	static class TransactionInnerData implements TransactionDataset {

		private final ResultData _resultData;

		private final boolean _wasSentAsTransaction;

		/**
		 * Konstruktor
		 * @param resultData ResultData
		 * @param wasSentAsTransaction Als Transaktion gesendet?
		 */
		public TransactionInnerData(final ResultData resultData, final boolean wasSentAsTransaction) {
			_resultData = resultData;
			_wasSentAsTransaction = wasSentAsTransaction;
		}

		/**
		 * Konstruktor
		 * @param object Objekt
		 * @param attributeGroup ATG
		 * @param aspect ASP
		 * @param simulationVariant SV
		 * @param time ms seit 1970
		 * @param dataIndex Datenindex
		 * @param delayedData nachgeliefert?
		 * @param dataState  Status
		 * @param wasSentAsTransaction Als Transaktion gesendet?
		 * @param data Daten
		 */
		public TransactionInnerData(
				final SystemObject object,
				final AttributeGroup attributeGroup,
				final Aspect aspect,
				final short simulationVariant,
				final long time,
				final long dataIndex,
				final boolean delayedData,
				final DataState dataState,
				final boolean wasSentAsTransaction,
				final Data data) {
			_resultData = new ResultData(
					object, new DataDescription(attributeGroup, aspect, simulationVariant), delayedData, dataIndex, time, (byte)(dataState.getCode() - 1), data
			);
			_wasSentAsTransaction = wasSentAsTransaction;
		}

		public boolean wasSentAsTransaction() {
			return _wasSentAsTransaction;
		}

		/**
		 * Bestimmt den Datenzeitstempel des Datensatzes.
		 *
		 * @return Datenzeitstempel in Millisekunden seit 1970
		 */
		public long getDataTime() {
			return _resultData.getDataTime();
		}

		/**
		 * Bestimmt den je Datenidentifikation eindeutigen vom Datenverteiler vergebenen Datensatzindex dieses Datensatzes.
		 *
		 * @return Datensatzindex
		 */
		public long getDataIndex() {
			return _resultData.getDataIndex();
		}

		/**
		 * Bestimmt den Datensatztyp des Datensatzes.
		 *
		 * @return Datensatztyp
		 */
		public DataState getDataType() {
			return _resultData.getDataType();
		}

		/**
		 * Bestimmt die Datensatzart des Datensatzes.
		 *
		 * @return Datensatzart
		 */
		public ArchiveDataKind getDataKind() {
			return _resultData.getDataKind();
		}

		/**
		 * Bestimmt den eigentlichen Datensatzes mit den von der jeweiligen Attributgruppe definierten Attributwerten dieses Datensatzes.
		 *
		 * @return Datensatz mit Attributwerten oder <code>null</code> im Falle eines leeren Datensatzes.
		 */
		public Data getData() {
			return _resultData.getData();
		}

		/**
		 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten gehören.
		 *
		 * @return System-Objekt der enthaltenen Daten
		 */
		public SystemObject getObject() {
			return _resultData.getObject();
		}

		/**
		 * Bestimmt die Beschreibung der im Ergebnis enthaltenen Daten.
		 *
		 * @return Beschreibung der Daten
		 */
		public DataDescription getDataDescription() {
			return _resultData.getDataDescription();
		}

		@Override
		public String toString() {
			DateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
			final long dataIndex = getDataIndex();
			return "TransactionInnerData{" + "dataIndex=" + (dataIndex >> 32) + "#" + ((dataIndex >> 2) & 0x3fffffff) + "#" + (dataIndex & 0x3)
		       + ", time=" + timeFormat.format(new Date(getDataTime())) + ", object=" + getObject() + ", dataDescription=" + getDataDescription() + ", delayedData=" + getDataKind().isDelayed()
		       + ", dataType=" + getDataType() + ", wasSendAsTranscation=" + wasSentAsTransaction() + ",\n data=" + getData() + "}";
		}
	}
}

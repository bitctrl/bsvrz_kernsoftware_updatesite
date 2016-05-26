/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.xmlFile.properties;

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

import java.util.List;

/**
 * Dieses Objekt spiegelt eine Transaktionsdefininition wieder, die in der K2S.DTD definiert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransactionProperties extends AttributeGroupProperties {

	private List<DataIdentification> _possibleDids;

	private List<DataIdentification> _requiredDids;

	/**
	 * Erzegt eine neue Transaktionsdefinition
	 * @param name Name
	 * @param pid Pid
	 * @param id
	 * @param typePid
	 * @param info
	 */
	public TransactionProperties(String name, String pid, long id, String typePid, SystemObjectInfo info) {
		super(name, pid, id, typePid, info);
	}

	/**
	 * Setzt die Datenindentifikationen für die die Transaktion erlaubt ist
	 * @param possibleDids Datenidentifikationen (leere Liste = alle Erlaubt)
	 */
	public void setPossibleDids(final List<DataIdentification> possibleDids) {
		_possibleDids = possibleDids;
	}

	/**
	 * Setzt die Datenidentifikationen die für diese Transaktion notwendig sind
	 * @param requiredDids Erforderliche Datenidentifikationen
	 */
	public void setRequiredDids(final List<DataIdentification> requiredDids) {
		_requiredDids = requiredDids;
	}

	public List<DataIdentification> getPossibleDids() {
		return _possibleDids;
	}

	public List<DataIdentification> getRequiredDids() {
		return _requiredDids;
	}

	/**
	 * Transaktionsattributgruppen enthalten die feste Attributliste "atl.transaktion"
	 */
	@Override
	public AttributeProperties[] getAttributeAndAttributeList() {
		final ListAttributeProperties atl = new ListAttributeProperties("atl.transaktion");
		atl.setInfo(SystemObjectInfo.UNDEFINED);
		atl.setName("Transaktion");
		atl.setMaxCount(1);
		return new AttributeProperties[]{atl};
	}

	/**
	 * Transaktionsattributgruppen enthalten die feste Attributliste "atl.transaktion"
	 */
	@Override
	public void setAttributeAndAttributeList(final AttributeProperties[] attributeAndAttributeList) {
		throw new UnsupportedOperationException("Das Setzen von Attributen für eine Transaktion ist nicht vorgesehen.");
	}

	/**
	 * Datenidentifikation für import/Export von Transaktionen. Statt Systemobjekten werden Strings benutzt
	 */
	public static class DataIdentification {
		private final String _objectType;

		private final String _attributeGroup;

		private final String _aspect;

		private final boolean _onlyTransactionObject;

		public DataIdentification(final String objectType, final String attributeGroup, final String aspect, final String onlyTransactionObject) {
			_objectType = objectType;
			_attributeGroup = attributeGroup;
			_aspect = aspect;
			if(!"ja".equals(onlyTransactionObject) && !"nein".equals(onlyTransactionObject)){
				throw new IllegalArgumentException("Keine gültige Angabe für NurTransaktionsObjekt: " + onlyTransactionObject);
			}
			_onlyTransactionObject = "ja".equals(onlyTransactionObject);
		}

		public String getObjectType() {
			return _objectType;
		}

		public String getAttributeGroup() {
			return _attributeGroup;
		}

		public String getAspect() {
			return _aspect;
		}

		public boolean isOnlyTransactionObject() {
			return _onlyTransactionObject;
		}
	}
}

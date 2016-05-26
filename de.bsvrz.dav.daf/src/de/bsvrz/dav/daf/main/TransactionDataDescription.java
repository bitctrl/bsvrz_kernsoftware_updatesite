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

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Kapselt eine Transaktions-Datenidentifikation bestehend aus Objekt, Attributgruppe, Aspekt und  evtl. Simulationsvariante.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class TransactionDataDescription {

	private final SystemObject _object;

	private final DataDescription _dataDescription;

	/**
	 * Erstellt eine neue Transaktions-Datenidentifikation
	 * @param transactionObject Objekt
	 * @param dataDescription DataDescription
	 */
	public TransactionDataDescription(final SystemObject transactionObject, final DataDescription dataDescription) {
		// Generiert Exception wenn einer der Parameter null ist
		if(transactionObject == null) throw new IllegalArgumentException("transactionObject ist null");
		_object = transactionObject;
		_dataDescription = dataDescription;
		if(!getAttributeGroup().getType().getPid().equals(Pid.Type.TRANSACTION)) {
			throw new IllegalArgumentException("Die angegebene Transaktionsattributgruppe ist nicht vom typ.transaktion");
		}
	}

	/**
	 * Erstellt eine neue Transaktions-Datenidentifikation
	 *
	 * @param transactionObject         Objekt
	 * @param transactionAttributeGroup Attributgruppe
	 * @param transactionAspect Aspekt
	 */
	public TransactionDataDescription(final SystemObject transactionObject, final AttributeGroup transactionAttributeGroup, final Aspect transactionAspect) {
		this(transactionObject, new DataDescription(transactionAttributeGroup, transactionAspect));
	}

	/**
	 * Erstellt eine neue Transaktions-Datenidentifikation
	 *
	 * @param transactionObject         Objekt
	 * @param transactionAttributeGroup Attributgruppe
	 * @param transactionAspect Aspekt
	 * @param simulationVariant Simulationsvariante
	 */
	public TransactionDataDescription(
			final SystemObject transactionObject,
			final AttributeGroup transactionAttributeGroup,
			final Aspect transactionAspect,
			final short simulationVariant) {
		this(transactionObject, new DataDescription(transactionAttributeGroup, transactionAspect, simulationVariant));
	}

	/**
	 * Liefert die Datenbeschreibung zurück.
	 *
	 * @return DataDescription dieser Datenbeschreibung (ohne Objekt)
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Liefert das Objekt dieser Datenbeschreibung zurück.
	 *
	 * @return Objekt dieser Datenbeschreibung
	 */
	public SystemObject getObject() {
		return _object;
	}

	/**
	 * Liefert die Attributgruppe dieser Datenbeschreibung zurück.
	 *
	 * @return Attributgruppe dieser Datenbeschreibung
	 */
	public final AttributeGroup getAttributeGroup() {
		return _dataDescription.getAttributeGroup();
	}

	/**
	 * Liefert den Aspekt dieser Datenbeschreibung zurück.
	 *
	 * @return Aspekt dieser Datenbeschreibung
	 */
	public final Aspect getAspect() {
		return _dataDescription.getAspect();
	}

	/**
	 * Liefert die Simulationsvariante dieser Datenbeschreibung zurück.
	 *
	 * @return Simulationsvariante dieser Datenbeschreibung oder {@link DataDescription#NO_SIMULATION_VARIANT_SET}, wenn die Simulationsvariante nicht explizit
	 *         spezifiziert ist.
	 */
	public final short getSimulationVariant() {
		return _dataDescription.getSimulationVariant();
	}

	@Override
	public String toString() {
		return getObject().getPidOrId() + ":" + getAttributeGroup().getPidOrId() + ":" + getAspect().getPidOrId()
		       + (getSimulationVariant() == DataDescription.NO_SIMULATION_VARIANT_SET ? "" : ":" + getSimulationVariant());
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final TransactionDataDescription that = (TransactionDataDescription)o;

		if(!_dataDescription.equals(that._dataDescription)) return false;
		if(!_object.equals(that._object)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = _object.hashCode();
		result = 31 * result + _dataDescription.hashCode();
		return result;
	}


}

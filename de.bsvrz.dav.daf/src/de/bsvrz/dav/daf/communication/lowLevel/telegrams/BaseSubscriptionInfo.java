/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsageIdentifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Verwaltet eine Datenidentifikation bestehend aus Systemobjekt, Attributgruppenverwendung und Simulationsvariante.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5052 $
 */
public final class BaseSubscriptionInfo {

	/** Hashcode des Objekts. */
	private int _hashCode = 0;

	/** Identifikation des Objektes, zu dem Daten geschrieben werden. */
	private long _objectID = -1;

	/**
	 * Identifizierung der Attributgruppenverwendung
	 */
	private long _usageIdentification = -1;

	/** Simulationsvariante unter dem die Daten angefordert werden. */
	private short _simulationVariant;

	/**
	 * Diese Methode setzt einen String zusammen, welchen die <code>_objectID</code>, <code>_simulationVariant</code> und die <code>_usageInformation</code>
	 * enth�lt.
	 *
	 * @return String, wie beschrieben aufgebaut.
	 */
	public final String toString() {
		return "BaseSubscriptionInfo[objectID: " + _objectID + /*", attributeGroupCode:" + _attributeGroupCode + ", " + "aspectCode: " + _aspectCode
		       + */ ", simulationVariant: " + _simulationVariant + ", atgu: " + _usageIdentification + "]";
	}

	public BaseSubscriptionInfo() {
		_objectID = -1;
		_simulationVariant = -1;
	}

	/**
	 * Erzeugt neues <code>BaseSubscriptionInfo</code> Objekt.
	 *
	 * @param objectID            ID des Objektes
	 * @param attributeGroupUsage Objekt-ID der Attributgruppenverwendung.
	 * @param simulationVariant   Simulationsvariante
	 */
	public BaseSubscriptionInfo(long objectID, AttributeGroupUsage attributeGroupUsage, short simulationVariant) {
		_objectID = objectID;
		_usageIdentification = ((AttributeGroupUsageIdentifier)attributeGroupUsage).getIdentificationForDav();
		_simulationVariant = simulationVariant;
		calculateHashCode();
	}

	/**
	 * Erzeugt neues <code>BaseSubscriptionInfo</code> Objekt.
	 *
	 * @param objectID          ID des Objektes
	 * @param attributeGroupUsageIdentification
	 *                          Objekt-ID der Attributgruppenverwendung.
	 * @param simulationVariant Simulationsvariante
	 */
	public BaseSubscriptionInfo(long objectID, long attributeGroupUsageIdentification, short simulationVariant) {
		_usageIdentification = attributeGroupUsageIdentification;
		_objectID = objectID;
		_simulationVariant = simulationVariant;
		calculateHashCode();
	}

	/**
	 * Diese Methode ermittelt die ObjektID
	 *
	 * @return objectID
	 */
	public final long getObjectID() {
		return _objectID;
	}

	/**
	 * Diese Methode ermittelt die <code>_simulationVariat</code>e.
	 *
	 * @return Simulationsvariate
	 */
	public final short getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * Diese Methode schreibt Daten in den Ausgabestrom.
	 *
	 * @param out Ausgabestrom
	 *
	 * @throws IOException Falls Fehler im Ausgabestrom auftritt
	 */
	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_objectID);
		out.writeLong(_usageIdentification);
		out.writeShort(_simulationVariant);
	}

	/**
	 * Diese Methode liest Daten aus dem Eingabestrom
	 *
	 * @param in Eingabestrom
	 *
	 * @throws IOException  Falls Fehler im Ausgabestrom auftritt
	 */
	public final void read(DataInputStream in) throws IOException {
		_objectID = in.readLong();
		_usageIdentification = in.readLong();
		_simulationVariant = in.readShort();
		calculateHashCode();
	}

	/**
	 * Diese Methode �berpr�ft auf Gleichheit des �bergabeparameters mit baseSubscriptionInfo.
	 * @param other Instanz von BaseSubscrptionInfo
	 *
	 * @return <code>true</code> wenn �bergabeparameter Instanz von baseSubscriptionInfo
	 */
	public final boolean equals(final Object other) {
		if(!(other instanceof BaseSubscriptionInfo)) {
			return false;
		}
		BaseSubscriptionInfo otherInfo = (BaseSubscriptionInfo)other;
		return ((_objectID == otherInfo.getObjectID()) && (_usageIdentification == otherInfo._usageIdentification) && (_simulationVariant
		                                                                                                               == otherInfo.getSimulationVariant()));
	}


	public final int hashCode() {
		return _hashCode;
	}

	private void calculateHashCode() {
		int result = _simulationVariant;
		result = result + (((int)_usageIdentification) ^ ((int)(_usageIdentification >>> 32)));
		_hashCode = result * 1721 + (((int)_objectID) ^ ((int)(_objectID >>> 32)));
	}

	/**
	 * Gibt die Identifizierung der Attributgruppenverwendung an.
	 *
	 * @return  Aspektcode*/
	public long getUsageIdentification() {
		return _usageIdentification;
	}
}

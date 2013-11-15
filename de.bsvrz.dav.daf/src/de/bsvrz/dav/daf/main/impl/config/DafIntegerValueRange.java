/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.IntegerValueRange;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Wertebereiche von Ganzzahl-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class DafIntegerValueRange extends DafConfigurationObject implements IntegerValueRange {

	/** Skalierungsfaktor mit dem interne Werte multipliziert werden, um die externe Darstellung zu erhalten */
	private double _conversionFactor;

	/** Der größte zugelassene unskalierte Wert im Wertebereich */
	private long _maximum;

	/** Der kleinst zugelassene unskalierte Wert im Wertebereich */
	private long _minimum;

	/** Die Einheit von skalierten Werten im Bereich */
	private String _unit;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafIntegerValueRange(DafDataModel dataModel) {
		super(dataModel);
		_internType = INTEGER_VALUE_RANGE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafIntegerValueRange(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long[] setIds,
			double scale,
			long maximum,
			long minimum,
			String unit
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = INTEGER_VALUE_RANGE;
		_conversionFactor = scale;
		_maximum = maximum;
		_minimum = minimum;
		_unit = unit;
	}

	public final double getConversionFactor() {
		return _conversionFactor;
	}

	public final long getMaximum() {
		return _maximum;
	}

	public final long getMinimum() {
		return _minimum;
	}

	public final String getUnit() {
		return _unit;
	}

	public final String parseToString() {
		String str = "Bereicheigenschaften: \n";
		str += super.parseToString();
		str += "Skalierung: " + _conversionFactor + "\n";
		str += "Minimum: " + _minimum + "\n";
		str += "Maximum: " + _maximum + "\n";
		str += "Einheit: " + _unit + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeDouble(_conversionFactor);
		out.writeLong(_minimum);
		out.writeLong(_maximum);
		out.writeUTF(_unit);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_conversionFactor = in.readDouble();
		_minimum = in.readLong();
		_maximum = in.readLong();
		_unit = in.readUTF();
	}
}

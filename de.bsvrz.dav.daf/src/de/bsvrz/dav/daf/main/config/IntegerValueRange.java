/*
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Wertebereichen. Wertebereiche werden in {@link
 * IntegerAttributeType Ganzzahl-Attributtypen} benutzt.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface IntegerValueRange extends ConfigurationObject {
	/**
	 * Bestimmt den minimal erlaubten Wert dieses Bereichs.
	 *
	 * @return Minimum dieses Bereichs.
	 */
	public long getMinimum();

	/**
	 * Bestimmt den maximal erlaubten Wert dieses Bereichs.
	 *
	 * @return Maximum dieses Bereichs
	 */
	public long getMaximum();

	/**
	 * Bestimmt den Skalierungsfaktor mit dem interne Werte multipliziert werden, um die externe Darstellung zu
	 * erhalten.
	 *
	 * @return Skalierungsfaktor dieses Bereichs.
	 */
	public double getConversionFactor();

	/**
	 * Bestimmt die Maﬂeinheit von Werten dieses Bereichs nach der Skalierung in die externe Darstellung.
	 *
	 * @return Maﬂeinheit dieses Bereichs.
	 */
	public String getUnit();
}


/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
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

/**
 *
 * @author Kappich Systemberatung
 */
public interface TransmitterSubscriptionsConstants {

	public static final byte NEGATIV_RECEIP = 0;
	public static final byte POSITIV_RECEIP = 1;
	public static final byte POSITIV_RECEIP_NO_RIGHT = 2;
	public static final byte MORE_THAN_ONE_POSITIV_RECEIP = 3;
	public static final byte SENDER_SUBSCRIPTION = 0;
	public static final byte RECEIVER_SUBSCRIPTION = 1;

}


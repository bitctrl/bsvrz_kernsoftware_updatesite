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
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften eines Applikations-Objekts, das nach erfolgreicher
 * Authentifizierung einer Applikation vom Datenverteiler erzeugt wird. Der Name des Objekts und der konkrete
 * Typ wird von der Applikation beim Aufbau der logischen Verbindung zum Datenverteiler festgelegt.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5055 $ / $Date: 2007-09-01 11:31:09 +0200 (Sa, 01 Sep 2007) $ / ($Author: rs $)
 * @see de.bsvrz.dav.daf.main.ClientDavInterface#login
 */
public interface ClientApplication extends DynamicObject {
}


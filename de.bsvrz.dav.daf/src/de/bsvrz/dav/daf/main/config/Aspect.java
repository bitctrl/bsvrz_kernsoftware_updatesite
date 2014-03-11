/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Aspekts. Ein Aspekt dient zur Unterscheidung von Online-Datensätzen eines Objekts mit gleichem
 * strukturellem Aufbau aber unterschiedlicher Bedeutung. Beispielsweise gibt es bei Parameterattributgruppen den Aspekt "soll" und den Aspekt "ist" um zwischen
 * dem aktuellen und durch die Parametrierung geprüften Parametersatz und der noch nicht geprüften Vorgabe eines Bedieners zu unterscheiden.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface Aspect extends ConfigurationObject {

}


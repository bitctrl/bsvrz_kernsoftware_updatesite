/*
 * Copyright 2012 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.kappich.
 * 
 * de.bsvrz.sys.funclib.kappich is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.kappich is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.kappich; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.kappich.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Zeigt an, dass eine Funktion, Variable oder Feld den Wert <code>null</code> enthalten bzw. (unerwartet) zurückgeben darf.
 * Lässt sich in den IntelliJ-Idea-Inspections unter @Nullable/@NotNull einrichten um entsprechende Warnungen über mögliche
 * NullPointerExceptions zu erhalten. Dient außerdem der Code-Dokumentation.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target(value={FIELD, LOCAL_VARIABLE, PARAMETER, METHOD, TYPE_USE})
public @interface Nullable{

}

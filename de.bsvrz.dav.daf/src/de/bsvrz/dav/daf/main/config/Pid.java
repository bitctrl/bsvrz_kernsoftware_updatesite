/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.config;

/**
 * @author rs
 */
public class Pid {

	public static class Type {
		public static final String TYPE = "typ.typ";
		public static final String DYNAMIC_TYPE = "typ.dynamischerTyp";
		public static final String ATTRIBUTE_GROUP = "typ.attributgruppe";
		public static final String ATTRIBUTE_GROUP_USAGE = "typ.attributgruppenVerwendung";
		public static final String ATTRIBUTE_TYPE = "typ.attributTyp";
		public static final String STRING_ATTRIBUTE_TYPE = "typ.zeichenketteAttributTyp";
		public static final String INTEGER_ATTRIBUTE_TYPE = "typ.ganzzahlAttributTyp";
		public static final String DOUBLE_ATTRIBUTE_TYPE = "typ.kommazahlAttributTyp";
		public static final String TIME_ATTRIBUTE_TYPE = "typ.zeitstempelAttributTyp";
		public static final String REFERENCE_ATTRIBUTE_TYPE = "typ.objektReferenzAttributTyp";
		public static final String ASPECT = "typ.aspekt";
		public static final String CONFIGURATION_OBJECT = "typ.konfigurationsObjekt";
		public static final String OBJECT_SET_USE = "typ.mengenVerwendung";
		public static final String OBJECT_SET_TYPE = "typ.mengenTyp";
		public static final String ATTRIBUTE = "typ.attribut";
		public static final String ATTRIBUTE_LIST_DEFINITION = "typ.attributListenDefinition";
		public static final String NON_MUTABLE_SET = "typ.konfigurationsMenge";
		public static final String MUTABLE_SET = "typ.dynamischeMenge";
		public static final String INTEGER_VALUE_RANGE = "typ.werteBereich";
		public static final String INTEGER_VALUE_STATE = "typ.werteZustand";
		public static final String DAV_APPLICATION = "typ.datenverteiler";
		public static final String CLIENT_APPLICATION = "typ.applikation";
		public static final String DYNAMIC_OBJECT = "typ.dynamischesObjekt";
		public static final String CONFIGURATION_AUTHORITY = "typ.konfigurationsVerantwortlicher";
		public static final String SYSTEM_CONFIGURATION_AUTHORITY = "typ.system";
		public static final String ATTRIBUTE_SET = "typ.attributMenge";
		public static final String CONFIGURATION_AREA = "typ.konfigurationsBereich";
		public static final String TRANSACTION = "typ.transaktion";
	}

	public static class SetType {
		public static final String ATTRIBUTEGROUPS = "menge.attributgruppen";
		public static final String OBJECT_TYPES = "menge.objektTypen";
		public static final String OBJECT_SET_USES = "menge.mengenVerwendungen";
		public static final String ASPECTS = "menge.aspekte";
		public static final String ATTRIBUTES = "menge.attribute";
		public static final String INTEGER_VALUE_STATES = "menge.werteZustaende";
		public static final String ATTRIBUTE_GROUP_USAGES = "menge.attributgruppenVerwendungen";
	}

	public static class Object {
	}
}

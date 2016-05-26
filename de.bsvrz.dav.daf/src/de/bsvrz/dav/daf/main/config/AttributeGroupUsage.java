/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung, Aachen
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
 * Schnittstelle zum Zugriff auf die Attributgruppenverwendung.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 * @see "TPuK1-77"
 */
public interface AttributeGroupUsage extends ConfigurationObject {
	/**
	 * Bestimmt die Attributgruppe, die in Datensätzen dieser Attributgruppenverwendung benutzt wird.
	 *
	 * @see "TPuK1-78"
	 */
	AttributeGroup getAttributeGroup();

	/**
	 * Bestimmt den Aspekt, der in Datensätzen dieser Attributgruppenverwendung benutzt wird.
	 *
	 * @see "TPuK1-79"
	 */
	Aspect getAspect();

	/**
	 * Bestimmt, ob die durch diese Attributgruppenverwendung festgelegte Kombination von Attributgruppe und Aspekt für
	 * konfigurierende oder für Online-Datensätze benutzt wird.
	 *
	 * @return <code>true</code>, wenn diese Attributgruppenverwendung für konfigurierende Datensätze benutzt wird oder
	 *         <code>false</code>, wenn diese Attributgruppenverwendung für Online-Datensätze benutzt wird.
	 * @see "TPuK1-80"
	 */
	boolean isConfigurating();

	/**
	 * Bestimmt, ob diese Attributgruppenverwendung explizit definiert wurde oder ob sie sich implizit aus der Hierarchie
	 * der Parameter ergeben hat.
	 *
	 * @return <code>true</code>, wenn diese Attributgruppenverwendung explizit definiert wurde oder <code>false</code>
	 *         wenn sie sich implizit aus der Hierarchie der Parameter ergeben hat.
	 * @see "TPuK1-81"
	 */
	boolean isExplicitDefined();

	/**
	 * Bestimmt die Verwendungsmöglichkeiten von Online- oder konfigurierenden Datensätzen dieser
	 * Atributgruppenverwendung.
	 *
	 * @return Verwendungsmöglichkeiten dieser Attributgruppenverwendung.
	 * @see "TPuk1-82 und TPuK1-83"
	 */
	Usage getUsage();

	/**
	 * Aufzählung der verschiedenen Verwendungsmöglichkeiten einer Attributgruppenverwendung.
	 *
	 * @see AttributeGroupUsage#getUsage
	 */
	public enum Usage {
		/**
		 * Verwendung für Konfigurierende Datensätze, die notwendigerweise versorgt werden müssen und nach Aktivierung nicht
		 * mehr geändert werden dürfen.
		 */
		RequiredConfigurationData("datensatzNotwendig", 1),

		/**
		 * Verwendung für Konfigurierende Datensätze, die notwendigerweise versorgt werden müssen und auch nach Aktivierung
		 * geändert werden dürfen.
		 */
		ChangeableRequiredConfigurationData("datensatzNotwendigUndÄnderbar", 2),

		/**
		 * Verwendung für Konfigurierende Datensätze, die nicht notwendigerweise versorgt werden müssen und nach Aktivierung
		 * nicht mehr geändert werden dürfen.
		 */
		OptionalConfigurationData("datensatzOptional", 3),

		/**
		 * Verwendung für Konfigurierende Datensätze, die nicht notwendigerweise versorgt werden müssen und nach Aktivierung
		 * geändert werden dürfen.
		 */
		ChangeableOptionalConfigurationData("datensatzOptionalUndÄnderbar", 4),

		/**
		 * Verwendung für Online-Datensätze, die nur als Quelle oder einfacher Empfänger angemeldet werden dürfen.
		 */
		OnlineDataAsSourceReceiver("quelle", 5),

		/**
		 * Verwendung für Online-Datensätze, die nur als einfacher Sender oder Senke angemeldet werden dürfen.
		 */
		OnlineDataAsSenderDrain("senke", 6),

		/**
		 * Verwendung für Online-Datensätze, die mit beliebigen Rollen angemeldet werden dürfen.
		 */
		OnlineDataAsSourceReceiverOrSenderDrain("quelleUndSenke", 7);

		private final String _value;

		private int _id;

		private Usage(String value, int id) {
			_value = value;
			_id = id;
		}

		public String getValue() {
			return _value;
		}

		public int getId() {
			return _id;
		}

		public static Usage getInstanceWithId(int id) {
			switch(id) {
				case 1: return RequiredConfigurationData;
				case 2: return ChangeableRequiredConfigurationData;
				case 3: return OptionalConfigurationData;
				case 4: return ChangeableOptionalConfigurationData;
				case 5: return OnlineDataAsSourceReceiver;
				case 6: return OnlineDataAsSenderDrain;
				case 7: return OnlineDataAsSourceReceiverOrSenderDrain;
				default: throw new IllegalArgumentException("Keine Verwendung mit id=" + id + " definiert.");
			}
		}
	}
}

/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung, Aachen
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
 * Schnittstelle zum Zugriff auf die Attributgruppenverwendung.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 * @see "TPuK1-77"
 */
public interface AttributeGroupUsage extends ConfigurationObject {
	/**
	 * Bestimmt die Attributgruppe, die in Datens�tzen dieser Attributgruppenverwendung benutzt wird.
	 *
	 * @see "TPuK1-78"
	 */
	AttributeGroup getAttributeGroup();

	/**
	 * Bestimmt den Aspekt, der in Datens�tzen dieser Attributgruppenverwendung benutzt wird.
	 *
	 * @see "TPuK1-79"
	 */
	Aspect getAspect();

	/**
	 * Bestimmt, ob die durch diese Attributgruppenverwendung festgelegte Kombination von Attributgruppe und Aspekt f�r
	 * konfigurierende oder f�r Online-Datens�tze benutzt wird.
	 *
	 * @return <code>true</code>, wenn diese Attributgruppenverwendung f�r konfigurierende Datens�tze benutzt wird oder
	 *         <code>false</code>, wenn diese Attributgruppenverwendung f�r Online-Datens�tze benutzt wird.
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
	 * Bestimmt die Verwendungsm�glichkeiten von Online- oder konfigurierenden Datens�tzen dieser
	 * Atributgruppenverwendung.
	 *
	 * @return Verwendungsm�glichkeiten dieser Attributgruppenverwendung.
	 * @see "TPuk1-82 und TPuK1-83"
	 */
	Usage getUsage();

	/**
	 * Aufz�hlung der verschiedenen Verwendungsm�glichkeiten einer Attributgruppenverwendung.
	 *
	 * @see AttributeGroupUsage#getUsage
	 */
	public enum Usage {
		/**
		 * Verwendung f�r Konfigurierende Datens�tze, die notwendigerweise versorgt werden m�ssen und nach Aktivierung nicht
		 * mehr ge�ndert werden d�rfen.
		 */
		RequiredConfigurationData("datensatzNotwendig", 1),

		/**
		 * Verwendung f�r Konfigurierende Datens�tze, die notwendigerweise versorgt werden m�ssen und auch nach Aktivierung
		 * ge�ndert werden d�rfen.
		 */
		ChangeableRequiredConfigurationData("datensatzNotwendigUnd�nderbar", 2),

		/**
		 * Verwendung f�r Konfigurierende Datens�tze, die nicht notwendigerweise versorgt werden m�ssen und nach Aktivierung
		 * nicht mehr ge�ndert werden d�rfen.
		 */
		OptionalConfigurationData("datensatzOptional", 3),

		/**
		 * Verwendung f�r Konfigurierende Datens�tze, die nicht notwendigerweise versorgt werden m�ssen und nach Aktivierung
		 * ge�ndert werden d�rfen.
		 */
		ChangeableOptionalConfigurationData("datensatzOptionalUnd�nderbar", 4),

		/**
		 * Verwendung f�r Online-Datens�tze, die nur als Quelle oder einfacher Empf�nger angemeldet werden d�rfen.
		 */
		OnlineDataAsSourceReceiver("quelle", 5),

		/**
		 * Verwendung f�r Online-Datens�tze, die nur als einfacher Sender oder Senke angemeldet werden d�rfen.
		 */
		OnlineDataAsSenderDrain("senke", 6),

		/**
		 * Verwendung f�r Online-Datens�tze, die mit beliebigen Rollen angemeldet werden d�rfen.
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

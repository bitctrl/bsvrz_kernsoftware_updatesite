/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import java.util.List;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Typ-Objekts. Klassen, die diese Schnittstelle
 * implementieren, m�ssen auch die Schnittstelle f�r {@link SystemObjectCollection Zusammenstellungen von
 * System-Objekten} implementieren, �ber die der Zugriff auf alle Objekte des jeweiligen Typs m�glich ist. Bei
 * Objekt-Typen, die {@link #isConfigurating konfigurierend} sind, wird dar�berhinaus die Schnittstelle f�r
 * {@link ConfigurationObjectType Konfigurierende Typen} und damit auch die Schnittstelle f�r {@link
 * NonMutableCollection nicht online �nderbare Zusammenstellungen} implementiert.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */

public interface SystemObjectType extends ConfigurationObject, SystemObjectCollection {
	/**
	 * Liefert eine Liste der Typ-Objekte die diesen Typ erweitern, also direkt von diesem Typ abgeleitet sind.
	 *
	 * @return Liste von {@link SystemObjectType Typ-Objekten}
	 */
	public List<SystemObjectType> getSubTypes();

	/**
	 * Liefert eine Liste der Typ-Objekte die von diesem Typ-Objekt erweitert werden. Zur�ckgegeben werden die
	 * Typen, von denen dieser Typ gewisse Eigenschaften (wie z.B. verwendbare Mengen und Attributgruppen) erbt.
	 *
	 * @return Liste von {@link SystemObjectType Typ-Objekten}
	 */
	public List<SystemObjectType> getSuperTypes();

	/**
	 * Pr�ft, ob der im Parameter angegebene Typ in der Typhierarchie oberhalb dieses Typs vorkommt. Dies ist
	 * dann der Fall, wenn dieser Typ direkt oder indirekt den angegebenen Typ erweitert und damit dessen
	 * Eigenschaften erbt.
	 *
	 * @param other Zu pr�fender Typ
	 * @return <code>true</code> wenn dieser Typ vom angegebenen Typ erbt, sonst <code>false</code>.
	 */
	public boolean inheritsFrom(SystemObjectType other);

	/**
	 * Ermittelt, ob dieser Typ ein Basis-Typ ist.
	 *
	 * @return <code>true</code>, wenn der Typ ein Basis-Typ ist;<br/> <code>false</code>, wenn der Typ einen
	 *         anderen Typ erweitert.
	 */
	public boolean isBaseType();

	/**
	 * Ermittelt, ob dieser Typ ein konfigurierender Typ ist. Bei einem Basistyp ist festgelegt, ob Objekte des
	 * Typs konfigurierend oder nicht konfigurierend (dynamisch) sind. Bei Typen, die einen oder mehrere andere
	 * Typen erweitern, wird diese Eigenschaft durch die Super-Typen festgelegt. Eine Mischung von
	 * konfigurierenden und dynamischen Super-Typen ist nicht zugelassen. Konfigurierende Typen implementieren
	 * die Schnittstellenklasse {@link ConfigurationObjectType}. Bei dynamischen Typen k�nnen Objekte online
	 * erzeugt und gel�scht werden. Wenn bei konfigurierenden Typen Objekte erzeugt bzw. gel�scht werden, dann
	 * wird die jeweilige �nderung erst mit Aktivierung der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @return <code>true</code>, wenn Objekte des Typs konfigurierend sind;<br/> <code>false</code>, wenn
	 *         Objekte des Typs dynamisch sind.
	 *
	 * @see #isBaseType()
	 * @see #getSuperTypes()
	 * @see ConfigurationObjectType
	 * @see ConfigurationObject
	 */
	public boolean isConfigurating();

	/**
	 * Bestimmt, ob der Name eines Objekts dieses Typs (nachdem er einmal vergeben wurde) nochmal ge�ndert werden
	 * kann oder nicht. Bei bestimmten Objekten ist die Zugriffsm�glichkeit �ber den Namen des Objektes
	 * vorgesehen (z.B. Mengen eines Objekts oder Attribute einer Attributgruppe). Da mit der �nderung des
	 * Objektnamens bei diesen Objekten quasi eine �nderung der Konfigurationsstruktur durchgef�hrt wird und dies
	 * Auswirkungen auf SW-Einheiten haben kann, wird bei diesen Objekten die �nderbarkeit des Objekt-Namens
	 * eingeschr�nkt.
	 *
	 * @return <code>true</code>, wenn der Name nicht ge�ndert werden kann.<br/> <code>false</code>, wenn der
	 *         Name ge�ndert werden kann.
	 */
	public boolean isNameOfObjectsPermanent();

	/**
	 * Liefert die Liste aller System-Objekte dieses Typs zur�ck. Zu beachten ist, das auch Objekte eines Typs,
	 * der diesen Typ erweitert, zur�ckgegeben werden.
	 *
	 * @return Liste von {@link SystemObject System-Objekten}
	 */
	public List<SystemObject> getObjects();

	/**
	 * Liefert eine Liste aller Attributgruppen, die von System-Objekten dieses Typs verwendet werden k�nnen und
	 * nicht von einem Supertyp geerbt wurden, zur�ck.
	 *
	 * @return Liste von {@link AttributeGroup Attributgruppen}
	 */
	public List<AttributeGroup> getDirectAttributeGroups();

	/**
	 * Liefert eine Liste aller Attributgruppen, die von System-Objekten dieses Typs verwendet werden k�nnen,
	 * zur�ck. Enthalten sind auch die Attributgruppen, die von den Supertypen dieses Typs geerbt wurden.
	 *
	 * @return Liste von {@link AttributeGroup Attributgruppen}
	 */
	public List<AttributeGroup> getAttributeGroups();

	/**
	 * Liefert eine Liste von Mengen-Verwendungen dieses Typs ohne die Mengen-Verwendungen, die von Supertypen
	 * geerbt werden. Die Mengen-Verwendungen enthalten Informationen zu den mit diesem Typ verwendbaren Mengen.
	 *
	 * @return Liste von {@link ObjectSetUse Mengen-Verwendungen}
	 */
	public List<ObjectSetUse> getDirectObjectSetUses();

	/**
	 * Liefert eine Liste von Mengen-Verwendungen, die Informationen zu den mit diesem Typ verwendbaren Mengen
	 * enthalten. In der zur�ckgegebenen Liste sind auch die Mengen-Verwendungen, die von Supertypen geerbt
	 * wurden, enthalten.
	 *
	 * @return Liste von {@link ObjectSetUse Mengen-Verwendungen}
	 */
	public List<ObjectSetUse> getObjectSetUses();
}




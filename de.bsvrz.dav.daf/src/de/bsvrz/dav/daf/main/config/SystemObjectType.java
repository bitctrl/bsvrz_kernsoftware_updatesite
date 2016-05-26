/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import java.util.List;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Typ-Objekts. Klassen, die diese Schnittstelle
 * implementieren, müssen auch die Schnittstelle für {@link SystemObjectCollection Zusammenstellungen von
 * System-Objekten} implementieren, über die der Zugriff auf alle Objekte des jeweiligen Typs möglich ist. Bei
 * Objekt-Typen, die {@link #isConfigurating konfigurierend} sind, wird darüberhinaus die Schnittstelle für
 * {@link ConfigurationObjectType Konfigurierende Typen} und damit auch die Schnittstelle für {@link
 * NonMutableCollection nicht online änderbare Zusammenstellungen} implementiert.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */

public interface SystemObjectType extends ConfigurationObject, SystemObjectCollection {
	/**
	 * Liefert eine Liste der Typ-Objekte die diesen Typ erweitern, also direkt von diesem Typ abgeleitet sind.
	 *
	 * @return Liste von {@link SystemObjectType Typ-Objekten}
	 */
	public List<SystemObjectType> getSubTypes();

	/**
	 * Liefert eine Liste der Typ-Objekte die von diesem Typ-Objekt erweitert werden. Zurückgegeben werden die
	 * Typen, von denen dieser Typ gewisse Eigenschaften (wie z.B. verwendbare Mengen und Attributgruppen) erbt.
	 *
	 * @return Liste von {@link SystemObjectType Typ-Objekten}
	 */
	public List<SystemObjectType> getSuperTypes();

	/**
	 * Prüft, ob der im Parameter angegebene Typ in der Typhierarchie oberhalb dieses Typs vorkommt. Dies ist
	 * dann der Fall, wenn dieser Typ direkt oder indirekt den angegebenen Typ erweitert und damit dessen
	 * Eigenschaften erbt.
	 *
	 * @param other Zu prüfender Typ
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
	 * die Schnittstellenklasse {@link ConfigurationObjectType}. Bei dynamischen Typen können Objekte online
	 * erzeugt und gelöscht werden. Wenn bei konfigurierenden Typen Objekte erzeugt bzw. gelöscht werden, dann
	 * wird die jeweilige Änderung erst mit Aktivierung der nächsten Konfigurationsversion gültig.
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
	 * Bestimmt, ob der Name eines Objekts dieses Typs (nachdem er einmal vergeben wurde) nochmal geändert werden
	 * kann oder nicht. Bei bestimmten Objekten ist die Zugriffsmöglichkeit über den Namen des Objektes
	 * vorgesehen (z.B. Mengen eines Objekts oder Attribute einer Attributgruppe). Da mit der Änderung des
	 * Objektnamens bei diesen Objekten quasi eine Änderung der Konfigurationsstruktur durchgeführt wird und dies
	 * Auswirkungen auf SW-Einheiten haben kann, wird bei diesen Objekten die Änderbarkeit des Objekt-Namens
	 * eingeschränkt.
	 *
	 * @return <code>true</code>, wenn der Name nicht geändert werden kann.<br/> <code>false</code>, wenn der
	 *         Name geändert werden kann.
	 */
	public boolean isNameOfObjectsPermanent();

	/**
	 * Liefert die Liste aller System-Objekte dieses Typs zurück. Zu beachten ist, das auch Objekte eines Typs,
	 * der diesen Typ erweitert, zurückgegeben werden.
	 *
	 * @return Liste von {@link SystemObject System-Objekten}
	 */
	public List<SystemObject> getObjects();

	/**
	 * Liefert eine Liste aller Attributgruppen, die von System-Objekten dieses Typs verwendet werden können und
	 * nicht von einem Supertyp geerbt wurden, zurück.
	 *
	 * @return Liste von {@link AttributeGroup Attributgruppen}
	 */
	public List<AttributeGroup> getDirectAttributeGroups();

	/**
	 * Liefert eine Liste aller Attributgruppen, die von System-Objekten dieses Typs verwendet werden können,
	 * zurück. Enthalten sind auch die Attributgruppen, die von den Supertypen dieses Typs geerbt wurden.
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
	 * enthalten. In der zurückgegebenen Liste sind auch die Mengen-Verwendungen, die von Supertypen geerbt
	 * wurden, enthalten.
	 *
	 * @return Liste von {@link ObjectSetUse Mengen-Verwendungen}
	 */
	public List<ObjectSetUse> getObjectSetUses();
}




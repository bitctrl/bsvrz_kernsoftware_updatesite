/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config;


import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.AttributeGroup;

import java.util.*;

/**
 * Klasse, die den Zugriff auf Attributgruppen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafAttributeGroup extends DafAttributeSet implements AttributeGroup {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Die Aspekte dieser Attributgruppe */
	private ArrayList<Aspect> aspects;

	/** Sperrt die Map _atgUsageMap */
	private final Object _lockAtgUsageMap = new Object();

	/**
	 * Speichert alle Attributgruppenverwendungen. Als Schlüssel dient der Aspekt. Die Map wird erst initialisiert, wenn das erste mal auf eine
	 * Attributgruppenverwendungen zugegriffen wird.
	 */
	private Map<Aspect, AttributeGroupUsage> _atgUsageMap = null;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafAttributeGroup(DafDataModel dataModel) {
		super(dataModel);
		_internType = ATTRIBUTE_GROUP;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafAttributeGroup(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long setIds[]
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = ATTRIBUTE_GROUP;
	}

	public final String parseToString() {
		String str = "Attributesgruppe: \n";
		str += super.parseToString();
		if(aspects == null) {
			getAspects();
		}
		if(aspects != null) {
			str += "Aspekte: \n";
			for(int i = 0; i < aspects.size(); ++i) {
				str += ((DafAspect)aspects.get(i)).parseToString();
			}
		}
		return str;
	}

	public final boolean isConfigurating() {
		final AttributeGroupUsage attributeGroupUsage = getAttributeGroupUsage(getDataModel().getAspect("asp.eigenschaften"));
		return attributeGroupUsage != null && attributeGroupUsage.isConfigurating();
	}

	public boolean isParameter() {
		final Collection<Aspect> aspects = getAspects();
		final Aspect asp01 = getDataModel().getAspect("asp.parameterSoll");
		final Aspect asp02 = getDataModel().getAspect("asp.parameterVorgabe");
		if(aspects.contains(asp01) && aspects.contains(asp02)) {
			return true;
		}
		else {
			return false;
		}
	}

	/** Initialisiert die Map mit Attributgruppenverwendungen */
	private void createAtgUsageMap() {
		synchronized(_lockAtgUsageMap) {
			_atgUsageMap = new HashMap<Aspect, AttributeGroupUsage>();
			final ObjectSet usageSet = getObjectSet("AttributgruppenVerwendungen");
			Collection<SystemObject> atgUsage = usageSet.getElements();
			for(SystemObject systemObject : atgUsage) {
				final AttributeGroupUsage usage = (AttributeGroupUsage)systemObject;
				_atgUsageMap.put(usage.getAspect(), usage);
			}
		}
	}

	public Collection<AttributeGroupUsage> getAttributeGroupUsages() {
		synchronized(_lockAtgUsageMap) {
			if(_atgUsageMap == null) {
				// Die Map wurde noch nicht angelegt, also erzeugen
				createAtgUsageMap();
			}
			return _atgUsageMap.values();
		}
	}

	public AttributeGroupUsage getAttributeGroupUsage(Aspect asp) {
		synchronized(_lockAtgUsageMap) {
			if(_atgUsageMap == null) {
				createAtgUsageMap();
			}
			AttributeGroupUsage attributeGroupUsage = _atgUsageMap.get(asp);
			if(attributeGroupUsage == null) {
				_debug.fine("Attributgruppenverwendung für " + this.getPidOrNameOrId() + " und " + asp.getPidOrNameOrId() + " nicht gefunden");
			}
			return attributeGroupUsage;
		}
	}

	public final List<Aspect> getAspects() {
		synchronized(_lockAtgUsageMap) {
			if((aspects == null) || (aspects.size() == 0)) {
				aspects = new ArrayList<Aspect>();
				// Die Aspekte müssen über die Attributgruppenverwendung gefunden werden
				final Collection<AttributeGroupUsage> atgUsages;
				atgUsages = getAttributeGroupUsages();
				for(AttributeGroupUsage attributeGroupUsage : atgUsages) {
					aspects.add(attributeGroupUsage.getAspect());
				}
			}
		} // synch
		return Collections.unmodifiableList(aspects);
	}
}

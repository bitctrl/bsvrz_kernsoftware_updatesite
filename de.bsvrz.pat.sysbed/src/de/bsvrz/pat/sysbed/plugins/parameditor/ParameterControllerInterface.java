/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.plugins.parameditor;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Stellt all die Methoden zur Verfügung, die der ParameterEditor benötigt, damit er mit der neuen Parametrierung zusammenarbeiten kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ParameterControllerInterface {
	void setConnection(final ClientDavInterface connection);
	void setParameterChangeInformer(final ParameterChangeInformer parameterChangeInformer);

	public void setParameterInfo(final SystemObject systemObject, final AttributeGroup attributeGroup, final short simulationVariant);

//	public Parameter getParameter();

	public void setParameter(final Data data);

	public void addListener();

	public void removeListener();

	public void actualizeSource();

	public SystemObject getSourceObject();

}

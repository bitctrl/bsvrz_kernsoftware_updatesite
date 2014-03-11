/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.fileaccess;

/**
 * Schnittstelle, die noch zu dokumentieren ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5074 $, $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $, $Author: rs $
 */
public interface ConfigurationAreaFileInformationReader {

	public SystemObjectInformationInterface getObject(long filePosition);
	
	public ConfigFileHeaderInfo getHeader();


}

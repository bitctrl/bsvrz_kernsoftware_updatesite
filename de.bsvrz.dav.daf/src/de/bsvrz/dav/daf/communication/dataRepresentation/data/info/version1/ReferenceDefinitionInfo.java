/*
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ReferenceDefinitionInfo extends AttributeTypeDefinitionInfo {
	public ReferenceDefinitionInfo(ReferenceAttributeType referenceAttributeType) {
		super(referenceAttributeType);
	}

	public boolean isSizeFixed() {
		return true;
	}

	public int getFixedSize() {
		return 8;
	}

	public String getValueText(byte[] bytes, int offset) {
		final long id = getId(bytes, offset);
		try {
			SystemObject object = getSystemObject(id);
			if(object == null) return "0";
			String pid= object.getPid();
			if(pid!=null && !pid.equals("")) return pid;
		}
		catch(Exception e) {
			//Fehler beim Lesen der pid -> weiter mit Rückgabe der id
		}
		try {
			return String.valueOf(id);
		}
		catch(Exception ee) {
			return "<<" + ee.getMessage() + ">>";
		}
	}

	public String getSuffixText(byte[] bytes, int offset) {
		try {
			String name= null;
			String pid= null;
			String exceptionMessage= null;
			StringBuffer suffix= new StringBuffer();
			try {
				SystemObject object = getSystemObject(bytes, offset);
				if(object == null) return "id (null)";
				name = object.getName();
				pid = object.getPid();
			}
			catch(Exception e) {
				exceptionMessage= " " + e.getLocalizedMessage();
			}
			if(pid == null || pid.equals("")) suffix.append("id");
			else suffix.append("pid");
			if(name!=null && !name.equals("")) suffix.append(" (Name: ").append(name).append(")");
			if(exceptionMessage!=null) suffix.append(" ").append(exceptionMessage);
			return suffix.toString();
		}
		catch(Exception ee) {
			return "<<" + ee.getMessage() + ">>";
		}
	}

	public boolean isReferenceAttribute() {
		return true;
	}

	public long getId(byte[] bytes, int offset) {
		final long id =
		        (long)(bytes[offset + 0] & 0xff) << 56 |
		        (long)(bytes[offset + 1] & 0xff) << 48 |
		        (long)(bytes[offset + 2] & 0xff) << 40 |
		        (long)(bytes[offset + 3] & 0xff) << 32 |
		        (long)(bytes[offset + 4] & 0xff) << 24 |
		        (bytes[offset + 5] & 0xff) << 16 |
		        (bytes[offset + 6] & 0xff) << 8 |
		        (bytes[offset + 7] & 0xff) << 0;
		return id;
	}

	public SystemObject getSystemObject(final byte[] bytes, final int offset) {
		final long id = getId(bytes, offset);
		return getSystemObject(id);
	}

	private SystemObject getSystemObject(final long id) {
		try {
			if(id == 0) return null;
			SystemObject object = getAttributeType().getDataModel().getObject(id);
			if(object == null) throw new IllegalStateException("Ungültiges Objekt mit id " + id);
			return object;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

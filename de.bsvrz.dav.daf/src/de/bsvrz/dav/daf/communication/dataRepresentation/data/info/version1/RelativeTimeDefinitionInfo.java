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

import de.bsvrz.dav.daf.main.config.TimeAttributeType;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class RelativeTimeDefinitionInfo extends AttributeTypeDefinitionInfo {

	protected RelativeTimeDefinitionInfo(TimeAttributeType att) {
		super(att);
	}

	public boolean isSizeFixed() {
		return true;
	}

	public String getValueText(byte[] bytes, int offset) {
		try {
			StringBuffer text = new StringBuffer();
			long val = getMillis(bytes, offset);
			//Beispiel= "234 Tage 12 Stunden 34 Minuten 33 Sekunden 443 Millisekunden"
			int millis = (int)(val % 1000);
			val /= 1000;
			int seconds = (int)(val % 60);
			val /= 60;
			int minutes = (int)(val % 60);
			val /= 60;
			int hours = (int)(val % 24);
			val /= 24;
			long days = val;
			if(days != 0) {
				if(days == 1) {
					text.append("1 Tag ");
				}
				else if(days == -1) {
					text.append("-1 Tag ");
				}
				else {
					text.append(days).append(" Tage ");
				}
			}
			if(hours != 0) {
				if(hours == 1) {
					text.append("1 Stunde ");
				}
				else if(hours == -1) {
					text.append("-1 Stunde ");
				}
				else {
					text.append(hours).append(" Stunden ");
				}
			}
			if(minutes != 0) {
				if(minutes == 1) {
					text.append("1 Minute ");
				}
				else if(minutes == -1) {
					text.append("-1 Minute ");
				}
				else {
					text.append(minutes).append(" Minuten ");
				}
			}
			if(seconds != 0 || (days == 0 && hours == 0 && minutes == 0 && millis == 0)) {
				if(seconds == 1) {
					text.append("1 Sekunde ");
				}
				else if(seconds == -1) {
					text.append("-1 Sekunde ");
				}
				else {
					text.append(seconds).append(" Sekunden ");
				}
			}
			if(millis != 0) {
				if(millis == 1) {
					text.append("1 Millisekunde ");
				}
				else if(millis == -1) {
					text.append("-1 Millisekunde ");
				}
				else {
					text.append(millis).append(" Millisekunden ");
				}
			}
			text.setLength(text.length() - 1);
			return text.toString();
		}
		catch(Exception e) {
			return "<<" + e.getMessage() + ">>";
		}
	}

	public String getSuffixText(byte[] bytes, int offset) {
		return "";
	}


	public boolean isTimeAttribute() {
		return true;
	}
}

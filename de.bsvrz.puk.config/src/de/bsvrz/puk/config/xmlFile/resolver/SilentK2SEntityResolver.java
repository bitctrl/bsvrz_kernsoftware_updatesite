/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.xmlFile.resolver;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * Implementierung eines EntityResolvers, der Referenzen auf den Public-Identifier "-//K2S//DTD Dokument//DE" ersetzt
 * durch die K2S.dtd Resource-Datei in diesem Package.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SilentK2SEntityResolver implements EntityResolver {

	/**
	 * Löst Referenzen auf external entities wie z.B. DTD-Dateien auf.
	 * <p>
	 * Angegebene Dateien werden, falls sie im Suchverzeichnis gefunden werden, von dort geladen. Ansonsten wird der
	 * normale Mechanismus zum Laden von externen Entities benutzt. Das Suchverzeichnis kann bei Bedarf im @{link
	 * SEDataModel#SEDataModel Konstruktor} spezifiziert werden.
	 *
	 * @param publicId Der public identifer der externen Entity oder null falls dieser nicht verfügbar ist.
	 * @param systemId Der system identifier aus dem XML-Dokument.
	 * @return Für Referenzen die im Suchverzeichnis wird ein InputSource-Objekt, das mit der entsprechenden Datei im
	 *         Suchverzeichnis verbunden ist zurückgegeben. Ansonsten wird null für den normalen Suchmechanismus
	 *         zurückgegeben.
	 * @throws org.xml.sax.SAXException Bei Fehlern beim Zugriff auf externe Entities.
	 * @see org.xml.sax.EntityResolver#resolveEntity
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if(publicId != null && publicId.equals("-//K2S//DTD Dokument//DE")) {
			URL url = this.getClass().getResource("K2S.dtd");
			assert url != null : this.getClass();
			return new InputSource(url.toExternalForm());
		}
		return null;
	}

}

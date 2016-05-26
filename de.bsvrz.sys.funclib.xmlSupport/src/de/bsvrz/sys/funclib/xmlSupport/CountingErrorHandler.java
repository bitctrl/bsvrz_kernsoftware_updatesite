/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.xmlSupport.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.xmlSupport; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.sys.funclib.xmlSupport;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Implementierung eines ErrorHandlers, der mit XML-Parsern benutzt werden kann, um evtl. auftretende Fehler beim Parsen einer XML-Datei zu zählen und mit
 * entsprechenden Debug-Ausgaben zu dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CountingErrorHandler implements ErrorHandler {
	private static final Debug _debug = Debug.getLogger();

	private int _errorCount;

	private int _numberOfWarnings;

	/**
	 * Receive notification of a parser warning.
	 * <p>
	 * <p>The default implementation does nothing.  Application writers
	 * may override this method in a subclass to take specific actions
	 * for each warning, such as inserting the message in a log file or
	 * printing it to the console.</p>
	 *
	 * @param e The warning information encoded as an exception.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ErrorHandler#warning
	 * @see org.xml.sax.SAXParseException
	 */
	public void warning(SAXParseException e) throws SAXException {

		_debug.warning("Warnung: " + toString(e));
		_numberOfWarnings++;
	}

	/**
	 * Receive notification of a recoverable parser error.
	 * <p>
	 * <p>The default implementation does nothing.  Application writers
	 * may override this method in a subclass to take specific actions
	 * for each error, such as inserting the message in a log file or
	 * printing it to the console.</p>
	 *
	 * @param e The warning information encoded as an exception.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ErrorHandler#warning
	 * @see org.xml.sax.SAXParseException
	 */
	public void error(SAXParseException e) throws SAXException {
		_debug.error("Fehler: " + toString(e));
		_errorCount++;
	}

	/**
	 * Report a fatal XML parsing error.
	 * <p>
	 * <p>The default implementation throws a SAXParseException.
	 * Application writers may override this method in a subclass if
	 * they need to take specific actions for each fatal error (such as
	 * collecting all of the errors into a single report): in any case,
	 * the application must stop all regular processing when this
	 * method is invoked, since the document is no longer reliable, and
	 * the parser may no longer report parsing events.</p>
	 *
	 * @param e The error information encoded as an exception.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ErrorHandler#fatalError
	 * @see org.xml.sax.SAXParseException
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		_debug.error("Fehler: " + toString(e));
		_errorCount++;
		throw e;
	}

	private static String toString(SAXParseException e) {
		return
		        e.getSystemId() + (e.getPublicId() == null ? "" : " (" + e.getPublicId() + ")") +
		        " Zeile " + e.getLineNumber() + " Spalte " + e.getColumnNumber() + ":\n  " +
		        e.getLocalizedMessage();
	}

	public int getErrorCount() {
		return _errorCount;
	}

	public int getWarningCount() {
		return _numberOfWarnings;
	}

	public void printSummary() {
		if(getWarningCount()>0 || getErrorCount()>0) {
			if(getWarningCount()>0) {
				_debug.info(getWarningCount() + (getWarningCount()==1?" Warnung":" Warnungen") + " beim Parsen der XML-Datei");
			}
			if(getErrorCount()>0) {
				_debug.info(getErrorCount() + " Fehler beim Parsen der XML-Datei");
			}
		}
		else {
			_debug.fine("Keine Probleme beim Parsen der XML-Datei");
		}
	}

}

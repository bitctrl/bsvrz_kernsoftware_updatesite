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

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

/**
 * Implementierung eines SAX-Contenthandlers, der Default-Implementierungen aller Callback-Aufrufe des
 * SAX-Parsers enthält.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SilentContentHandler implements ContentHandler {

	public int _count = 0;

	/**
	 * Receive a Locator object for document events.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass if they wish to store the locator for use
	 * with other document events.</p>
	 *
	 * @param locator A locator for all SAX document events.
	 * @see org.xml.sax.ContentHandler#setDocumentLocator
	 * @see org.xml.sax.Locator
	 */
	public void setDocumentLocator(Locator locator) {
		++_count;
	}

	/**
	 * Receive notification of the beginning of the document.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the beginning
	 * of a document (such as allocating the root node of a tree or
	 * creating an output file).</p>
	 *
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#startDocument
	 */
	public void startDocument() throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of the end of the document.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the end
	 * of a document (such as finalising a tree or closing an output
	 * file).</p>
	 *
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#endDocument
	 */
	public void endDocument() throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of the start of a Namespace mapping.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the start of
	 * each Namespace prefix scope (such as storing the prefix mapping).</p>
	 *
	 * @param prefix The Namespace prefix being declared.
	 * @param uri    The Namespace URI mapped to the prefix.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#startPrefixMapping
	 */
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of the end of a Namespace mapping.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the end of
	 * each prefix mapping.</p>
	 *
	 * @param prefix The Namespace prefix being declared.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#endPrefixMapping
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of the start of an element.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the start of
	 * each element (such as allocating a new tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri The Namespace URI, or the empty string if the
	 *        element has no Namespace URI or if Namespace
	 *        processing is not being performed.
	 * @param localName The local name (without prefix), or the
	 *        empty string if Namespace processing is not being
	 *        performed.
	 * @param qName The qualified name (with prefix), or the
	 *        empty string if qualified names are not available.
	 * @param attributes The attributes attached to the element.  If
	 *        there are no attributes, it shall be an empty
	 *        Attributes object.
	 * @exception org.xml.sax.SAXException Any SAX exception, possibly
	 *            wrapping another exception.
	 * @see org.xml.sax.ContentHandler#startElement
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of the end of an element.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the end of
	 * each element (such as finalising a tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri The Namespace URI, or the empty string if the
	 *        element has no Namespace URI or if Namespace
	 *        processing is not being performed.
	 * @param localName The local name (without prefix), or the
	 *        empty string if Namespace processing is not being
	 *        performed.
	 * @param qName The qualified name (with prefix), or the
	 *        empty string if qualified names are not available.
	 * @exception org.xml.sax.SAXException Any SAX exception, possibly
	 *            wrapping another exception.
	 * @see org.xml.sax.ContentHandler#endElement
	 */
 	public void endElement(String uri, String localName, String qName) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of character data inside an element.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method to take specific actions for each chunk of character data
	 * (such as adding the data to a node or buffer, or printing it to
	 * a file).</p>
	 *
	 * @param ch     The characters.
	 * @param start  The start position in the character array.
	 * @param length The number of characters to use from the
	 *               character array.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#characters
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of ignorable whitespace in element content.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method to take specific actions for each chunk of ignorable
	 * whitespace (such as adding data to a node or buffer, or printing
	 * it to a file).</p>
	 *
	 * @param ch     The whitespace characters.
	 * @param start  The start position in the character array.
	 * @param length The number of characters to use from the
	 *               character array.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace
	 */
	public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of a processing instruction.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions for each
	 * processing instruction, such as setting status variables or
	 * invoking other methods.</p>
	 *
	 * @param target The processing instruction target.
	 * @param data   The processing instruction data, or null if
	 *               none is supplied.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#processingInstruction
	 */
	public void processingInstruction(String target, String data) throws SAXException {
		++_count;
	}

	/**
	 * Receive notification of a skipped entity.
	 * <p>
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions for each
	 * processing instruction, such as setting status variables or
	 * invoking other methods.</p>
	 *
	 * @param name The name of the skipped entity.
	 * @throws org.xml.sax.SAXException Any SAX exception, possibly
	 *                                  wrapping another exception.
	 * @see org.xml.sax.ContentHandler#processingInstruction
	 */
	public void skippedEntity(String name) throws SAXException {
		++_count;
	}
}

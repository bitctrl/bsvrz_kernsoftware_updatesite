/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.xmlSupport.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.xmlSupport; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter;


import de.bsvrz.sys.funclib.concurrent.BufferedQueue;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.xmlSupport.SilentContentHandler;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementierung eines SAX-basierten Pull-Adapters zum Parsen von XML-Dateien, der die Verwendung des SAX-XML-Parsers durch Umkehrung des Kontrollflusses
 * vereinfacht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9302 $
 */
public class SaxPullAdapter {

	private static final Debug _debug = Debug.getLogger();

	private SAXParser _saxParser;

	private XMLReader _xmlReader;

	private BufferedQueue<Event> _eventQueue;

	Thread _eventProducerThread;

	public SaxPullAdapter(EntityResolver resolver) throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			factory.setXIncludeAware(true);
		}
		catch(UnsupportedOperationException e) {
			final String message = "Der eingesetzte XML-Parser unterstützt nicht die neusten Schnittstellen. " +
			                       "Die Xerces XML-Parser mit Versionen kleiner oder gleich 2.6.2 sollten aus dem CLASSPATH " +
			                       "entfernt werden. Die in Java 5 enthaltenen XML-Parser sind ausreichend.";
			_debug.error(message);
			throw new LinkageError(message);
		}

		_saxParser = factory.newSAXParser();
		_debug.fine("SaxPullAdapter: eingesetzter SAX-Parser: " + _saxParser.toString());

		_xmlReader = _saxParser.getXMLReader();
		_xmlReader.setEntityResolver(resolver);
		_xmlReader.setDTDHandler(null);
	}

	public SaxPullAdapter(final URL schemaURL) throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		SchemaFactory dtd = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = dtd.newSchema(schemaURL);
		factory.setSchema(schema);
		try {
			factory.setXIncludeAware(true);
		}
		catch(UnsupportedOperationException e) {
			final String message = "Der eingesetzte XML-Parser unterstützt nicht die neusten Schnittstellen. " +
			                       "Die Xerces XML-Parser mit Versionen kleiner oder gleich 2.6.2 sollten aus dem CLASSPATH " +
			                       "entfernt werden. Die in Java 5 enthaltenen XML-Parser sind ausreichend.";
			_debug.error(message);
			throw new LinkageError(message);
		}

		_saxParser = factory.newSAXParser();
		_debug.fine("SaxPullAdapter: eingesetzter SAX-Parser: " + _saxParser.toString());

		_xmlReader = _saxParser.getXMLReader();
		_xmlReader.setDTDHandler(null);
	}

	public PullableEventStream start(final InputStream inputStream, final ErrorHandler errorHandler) throws SAXException, InterruptedException {
	synchronized(_saxParser) {
			_eventQueue = new BufferedQueue<Event>(5000);

			_xmlReader.setErrorHandler(errorHandler);
			final SilentContentHandler contentHandler = new EventPushingContentHandler();
			_xmlReader.setContentHandler(contentHandler);

			final Runnable eventProducerRunnable = new Runnable() {
				public void run() {
					try {
						_saxParser.parse(inputStream, (DefaultHandler)null);
					}
					catch(SAXParseException e) {
						// ignoriert, weil vom ErrorHandler schon bearbeitet
					}
					catch(SAXException e) {
						_debug.error("Fehler beim Parsen von " + inputStream + ": " + e);
					}
					catch(IOException e) {
						_debug.error("Fehler beim Parsen von " + inputStream + ": " + e);
					}
					finally {
						try {
							_eventQueue.put(new EndOfInputEvent());
							_eventQueue.flush();
							synchronized(SaxPullAdapter.this) {
								_eventProducerThread = null;
							}
						}
						catch(InterruptedException e) {
							_debug.error("Fehler beim Parsen von " + inputStream + ": " + e);
						}
					}
				}
			};
			_eventProducerThread = new Thread(eventProducerRunnable);
			_eventProducerThread.start();
			return new PullableEventStream(_eventQueue);
		}
	}

	public synchronized PullableEventStream start(final File file, ErrorHandler errorHandler) throws SAXException, InterruptedException {
		synchronized(_saxParser) {
			_eventQueue = new BufferedQueue<Event>(5000);

			_xmlReader.setErrorHandler(errorHandler);
			final SilentContentHandler contentHandler = new EventPushingContentHandler();
			_xmlReader.setContentHandler(contentHandler);

			final Runnable eventProducerRunnable = new Runnable() {
				public void run() {
					try {
						_saxParser.parse(file, (DefaultHandler)null);
					}
					catch(SAXParseException e) {
						// ignoriert, weil vom ErrorHandler schon bearbeitet
					}
					catch(SAXException e) {
						_debug.error("Fehler beim Parsen von " + file + ": " + e);
					}
					catch(IOException e) {
						_debug.error("Fehler beim Parsen von " + file + ": " + e);
					}
					finally {
						try {
							_eventQueue.put(new EndOfInputEvent());
							_eventQueue.flush();
							synchronized(SaxPullAdapter.this) {
								_eventProducerThread = null;
							}
						}
						catch(InterruptedException e) {
							_debug.error("Fehler beim Parsen von " + file + ": " + e);
						}
					}
				}
			};
			_eventProducerThread = new Thread(eventProducerRunnable);
			_eventProducerThread.start();
			return new PullableEventStream(_eventQueue);
		}
	}

	public synchronized void stop() {
		if(_eventProducerThread != null) _eventProducerThread.interrupt();
	}

	private class EventPushingContentHandler extends SilentContentHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//			System.out.println("localName = " + localName);
			final Event event = new StartElementEvent(qName.equals("") ? localName : qName, attributes);
			pushEvent(event);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			final Event event = new EndElementEvent(localName);
			pushEvent(event);
		}

		public void characters(char ch[], int start, int length) throws SAXException {
			final Event event = new CharactersEvent(ch, start, length);
			pushEvent(event);
		}


		/**
		 * Receive notification of ignorable whitespace in element content.
		 * <p/>
		 * <p>By default, do nothing.  Application writers may override this method to take specific actions for each chunk of ignorable whitespace (such as adding
		 * data to a node or buffer, or printing it to a file).</p>
		 *
		 * @param ch     The whitespace characters.
		 * @param start  The start position in the character array.
		 * @param length The number of characters to use from the character array.
		 *
		 * @throws org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
		 * @see org.xml.sax.ContentHandler#ignorableWhitespace
		 */
		public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
			final Event event = new IgnorableCharactersEvent(ch, start, length);
			pushEvent(event);
		}

		//		private void pushEvent(final Object event) throws SAXException {
		private void pushEvent(final Event event) throws SAXException {
			try {
				_eventQueue.put(event);
			}
			catch(InterruptedException e) {
				throw new SAXException(e); 
			}
		}
	}
}

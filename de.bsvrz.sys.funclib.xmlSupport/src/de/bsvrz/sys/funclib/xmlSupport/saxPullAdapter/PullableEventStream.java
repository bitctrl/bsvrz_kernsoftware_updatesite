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
package de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter;

import de.bsvrz.sys.funclib.concurrent.BufferedQueue;
import org.xml.sax.SAXException;

/**
 * Klasse, zum Zugriff auf die vom SaxPullAdapter beim Parsen einer XML-Datei erzeugten Ereignisse.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class PullableEventStream {
	private final BufferedQueue<Event> _queue;
	private Event _currentEvent;
	private StartElementEvent _lastStartElement;


	public PullableEventStream(BufferedQueue<Event> queue) throws InterruptedException {
		_queue = queue;
		_currentEvent = _queue.take();
	}

	public Event pullAnyEvent() throws InterruptedException {
		Event pulledEvent = _currentEvent;
		if(pulledEvent.getType() != EventType.END_OF_INPUT) {
			_currentEvent = _queue.take();
			if(pulledEvent.getType() == EventType.START_ELEMENT) {
				_lastStartElement = (StartElementEvent) pulledEvent;
			}
		}
		return pulledEvent;
	}

	public Event pullNonIgnorableEvent() throws InterruptedException {
		ignoreIgnorableCharacters();
		return pullAnyEvent();
	}

	public StartElementEvent pullStartElement() throws InterruptedException, SAXException {
		Event event = pullNonIgnorableEvent();
		if(event.getType() == EventType.START_ELEMENT) return (StartElementEvent)event;
		throw new SAXException("Element erwartet, aber " + event + " erhalten.");
	}

	public StartElementEvent pullStartElement(String tag) throws InterruptedException, SAXException {
		Event event = pullNonIgnorableEvent();
		if(event.getType() == EventType.START_ELEMENT) {
			StartElementEvent startElementEvent = (StartElementEvent)event;
			if(tag.equals(startElementEvent.getLocalName())) return startElementEvent;
		}
		throw new SAXException("Element <" + tag + "> erwartet, aber " + event + " erhalten.");
	}

	public EndElementEvent pullEndElement() throws InterruptedException, SAXException {
		Event event = pullNonIgnorableEvent();
		if(event.getType() == EventType.END_ELEMENT) return (EndElementEvent)event;
		throw new SAXException("Ende Element erwartet, aber " + event + " erhalten.");
	}

	public EndElementEvent pullEndElement(String tag) throws InterruptedException, SAXException {
		Event event = pullNonIgnorableEvent();
		if(event.getType() == EventType.END_ELEMENT) {
			EndElementEvent endElementEvent = (EndElementEvent)event;
			if(tag.equals(endElementEvent.getLocalName())) return endElementEvent;
		}
		throw new SAXException("Ende Element </" + tag + "> erwartet, aber " + event + " erhalten.");
	}

	public CharactersEvent pullCharacters() throws InterruptedException, SAXException {
		Event event = pullNonIgnorableEvent();
		if(event.getType() == EventType.CHARACTERS) return (CharactersEvent)event;
		throw new SAXException("Text erwartet, aber " + event + " erhalten.");
	}

	public IgnorableCharactersEvent pullIgnorableCharacters() throws InterruptedException, SAXException {
		Event event = pullAnyEvent();
		if(event.getType() == EventType.IGNORABLE_CHARACTERS) return (IgnorableCharactersEvent)event;
		throw new SAXException("Text erwartet, aber " + event + " erhalten.");
	}

	private void ignoreIgnorableCharacters() throws InterruptedException {
		while(_currentEvent.getType() == EventType.IGNORABLE_CHARACTERS) pullAnyEvent();
	}

	public boolean matchStartElement() throws InterruptedException {
		ignoreIgnorableCharacters();
		return _currentEvent.getType() == EventType.START_ELEMENT;
	}

	public boolean matchStartElement(String tag) throws InterruptedException {
		ignoreIgnorableCharacters();
		return _currentEvent.getType() == EventType.START_ELEMENT && tag.equals(((StartElementEvent)_currentEvent).getLocalName());
	}

	public boolean matchEndElement() throws InterruptedException {
		ignoreIgnorableCharacters();
		return _currentEvent.getType() == EventType.END_ELEMENT;
	}

	public boolean matchEndElement(String tag) throws InterruptedException {
		ignoreIgnorableCharacters();
		return _currentEvent.getType() == EventType.END_ELEMENT && tag.equals(((EndElementEvent)_currentEvent).getLocalName());
	}

	public boolean matchCharacters() throws InterruptedException {
		ignoreIgnorableCharacters();
		return _currentEvent.getType() == EventType.CHARACTERS;
	}

	public boolean matchIgnorableCharacters() throws InterruptedException {
		return _currentEvent.getType() == EventType.IGNORABLE_CHARACTERS;
	}

	public StartElementEvent getLastStartElement() {
		return _lastStartElement;
	}

	public String getLocationHint() {
		final StartElementEvent lastStartElement = getLastStartElement();
		if(lastStartElement == null) return "am Dateianfang";
		return "in der Nähe von " + lastStartElement.toString();
	}
}

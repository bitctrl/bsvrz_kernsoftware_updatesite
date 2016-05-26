/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.sys.funclib.dataSerializer;


import java.io.OutputStream;
import java.io.InputStream;

/**
 * Factory-Klasse zum Erzeugen von Objekten zur Serialisierung und Deserialisierung von Datensätzen. Es werden mehrere
 * verschiedene Versionen von Serialisierung und Deserialisierung unterstützt. Dies ist vorgesehen, um nach Änderungen
 * der Serialisierung eine reibungslose Migration der Software und des eventuell persistent gehaltenen Datenbestandes zu
 * ermöglichen. Dies ist im Falle des Deserialisierens insbesondere dann wichtig, wenn Daten, die mit einer alten
 * Version serialisiert wurden, wieder deserialisiert werden sollen. Im Falle des Serialisierens ist die Unterstützung
 * von mehreren Versionen insbesondere auch dann wichtig, wenn mit einem Softwareupdate nicht notwendigerweise auch die
 * Software aktualisiert wird, die die Daten wieder deserialisert.
 * <p>
 * Daten, die mit {@link Serializer} einer bestimmten Version serialisiert wurden, können mit einem {@link
 * Deserializer Deserialisierer} der gleichen Version wieder deserialisiert werden.
 * <p>
 * Mit den Klassen-Methoden {@link #createSerializer} und {@link #createDeserializer} können Objekte zur Serialisierung
 * bzw. zur Deserialisierung erzeugt werden.
 * <p>
 * Eine bestimmte Version der Serialisierung kann über einen entsprechenden Parameter beim Aufruf der {@link
 * #createSerializer(int,OutputStream)} Methode angefordert werden. Die {@link #createSerializer(OutputStream)} Methode
 * ohne Versions-Parameter liefert einen Serialisierer in der aktuellen Standardversion zurück (dies muss nicht
 * zwangsweise die neueste Version sein).
 * <p>
 * Eine bestimmte Version der Deserialisierung kann über einen entsprechenden Parameter beim Aufruf der {@link
 * #createDeserializer(int, InputStream)} Methode angefordert werden. Die {@link #createDeserializer(InputStream)}
 * Methode ohne Versions-Parameter liefert einen Deserialisierer in der aktuellen Standardversion zurück (dies muss
 * nicht zwangsweise die neueste Version sein).
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class SerializingFactory {
	private static final int DEFAULT_VERSION = 2;

	/**
	 * Erzeugt einen Serialisierer in der aktuellen Standardversion.
	 *
	 * @param outputStream Ausgabe-Stream auf den der zu erzeugende Serialisierer ausgeben soll.
	 * @return Serialisierer der aktuellen Standardversion.
	 */
	public static Serializer createSerializer(final OutputStream outputStream) {
		try {
			return SerializingFactory.createSerializer(DEFAULT_VERSION, outputStream);
		}
		catch(NoSuchVersionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt einen Serialisierer einer bestimmten Version.
	 *
	 * @param version      Gewünschte Version des Serialisierers.
	 * @param outputStream Ausgabe-Stream auf den der zu erzeugende Serialisierer ausgeben soll.
	 *
	 * @return Serialisierer der gewünschten Version.
	 *
	 * @throws NoSuchVersionException Wenn die gewünschte Version des Serialisierers nicht verfügbar ist.
	 */
	public static Serializer createSerializer(final int version, final OutputStream outputStream) throws NoSuchVersionException {
		final Serializer serializer;
		switch(version) {
			case 2:
			case 3:
				serializer = new SerializerImplementationA(version, outputStream);
				break;
			default:
				throw new NoSuchVersionException("Serialisierer mit der gewünschten Version " + version + " nicht verfügbar.");
		}
		if(serializer.getVersion() != version) {
			throw new RuntimeException("Serialisierer liefert falsche Version.");
		}
		return serializer;
	}

	/**
	 * Gibt die default-Version des Serializers zurück
	 *
	 * @return default-Version des Serializers
	 */
	public static int getDefaultVersion() {
		return DEFAULT_VERSION;
	}

	/**
	 * Erzeugt einen Deserialisierer in der aktuellen Standardversion.
	 *
	 * @param inputStream Eingabe-Stream von dem der zu erzeugende Deserialisierer einlesen soll.
	 * @return Deserialisierer der aktuellen Standardversion.
	 */
	public static Deserializer createDeserializer(final InputStream inputStream) {
		try {
			return SerializingFactory.createDeserializer(DEFAULT_VERSION, inputStream);
		}
		catch(NoSuchVersionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt einen Deserialisierer einer bestimmten Version.
	 *
	 * @param version     Gewünschte Version des Deserialisierers.
	 * @param inputStream Eingabe-Stream von dem der zu erzeugende Deserialisierer einlesen soll.
	 *
	 * @return Deserialisierer der gewünschten Version.
	 *
	 * @throws NoSuchVersionException Wenn die gewünschte Version des Deserialisierers nicht verfügbar ist.
	 */
	public static Deserializer createDeserializer(final int version, final InputStream inputStream) throws NoSuchVersionException {
		final Deserializer deserializer;
		switch(version) {
			case 2:
			case 3:
				deserializer = new DeserializerImplementationA(version, inputStream);
				break;
			default:
				throw new NoSuchVersionException("Deserialisierer mit der gewünschten Version " + version + " nicht verfügbar.");
		}
		if(deserializer.getVersion() != version) {
			throw new RuntimeException("Deserialisierer liefert falsche Version.");
		}
		return deserializer;
	}


	/**
	 * Konstruktur ist nicht öffenlich, weil keine Objekte der Klasse benötigt werden.
	 */
	private SerializingFactory() {
	}


}

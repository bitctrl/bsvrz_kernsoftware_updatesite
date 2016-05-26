/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.sys.funclib.communicationStreams;

/**
 * Dieses Interface muss implementiert werden, wenn ein StreamMultiplexer mit einem StreamDemultiplexer Nutzdatenpakete austauschen soll.
 * Die Methoden ermöglichen es, dass der StreamMultiplexer Nutzdaten von der Sendeapplikation anfordern und diese
 * dann an den StreamDemultiplexer weitergeben kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface StreamMultiplexerDirector {

	/**
	 * Diese Methode wird von einem StreamMultiplexer aufgerufen, sobald dieser Daten an einen StreamDemultiplexer
	 * verschicken darf. Die Methode sollte ein Nutzdatenpaket zur Verfügung stellen, das dann über den StreamMultiplexer
	 * und StreamDemultiplexer der Empfängerapplikation beim Aufruf der {@link StreamDemultiplexer#take} bereit gestellt
	 * wird.
	 *
	 * @param indexOfStream Stream über den die Nutzdaten übertragen werden sollen
	 * @return Nutzdaten, die von der Sendeapplikation zur Empfängerapplikation geschickt werden sollen. Sobald die
	 *         Sendeapplikation keine Nutzdaten mehr für die Empfängerapplikation hat, wird ein <code>null</code> zurück
	 *         gegeben.
	 */
	public byte[] take(int indexOfStream);

	/**
	 * Diese Methode wird von einem StreamMultiplexer aufgerufen und wenn ein Byte-Array an den entsprechenden
	 * StreamDemultiplexer gesendet werden soll. Empfangsseitig sollte die Methode {@link
	 * StreamDemultiplexer#receivedDataFromSender} aufgerufen werden.
	 *
	 * Das Byte-Array enthält kodiert den Index des Streams, den Index des Pakets, die Größe des Byte-Arrays in dem
	 * die Nutzdaten gespeichert sind und die Nutzdaten.
	 *
	 * @param streamDataPacket Ein Nutzdatenpaket vom StreamMultiplexer zum StreamDemultiplexer
	 */
	public void sendData(byte[] streamDataPacket);

	/**
	 * Wenn die Empfängerapplikation keine Nutzdaten mehr verarbeiten kann (aus welchem Grund auch immer), wird sie den
	 * Stream auf Empfängerseite (StreamDemultiplexer) mit abort beenden. Mit diesem Methodenaufruf wird die Senderapplikation
	 * benachrichtigt, dass sie alle Daten, die für einen Stream bereitgestellt wurden nicht mehr benötigt wurden und das
	 * sie auch in Zukunft keine Daten mehr für diesen Stream zur Verfügung stellen muss. Der StreamMultiplexer wird diese
	 * Funktion aufrufen, sobald er von dem StreamDemultiplexer benachrichtigt wurde, dass ein Stream abgebrochen werden kann.
	 *
	 * @param indexOfStream Index des Streams, dessen Nutzdaten in der Senderapplikation verworfen werden können
	 */
	public void streamAborted(int indexOfStream);

}

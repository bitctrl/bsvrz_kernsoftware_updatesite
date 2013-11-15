/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.communicationStreams.
 * 
 * de.bsvrz.sys.funclib.communicationStreams is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.communicationStreams is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.communicationStreams; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.sys.funclib.communicationStreams;

/**
 * Dieses Interface muss implementiert werden, wenn ein StreamMultiplexer mit einem StreamDemultiplexer Nutzdatenpakete austauschen soll.
 * Die Aufgabe dieses Interfaces ist es, den StreamMultiplexer zu benachrichtigen, dass er neue Nutzdatenpakete verschicken darf.
 *
 * Dieses Interface wird vom StreamDemultiplexer benötigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5019 $
 */
public interface StreamDemultiplexerDirector {

	/**
	 * Diese Methode schickt einem StreamMultiplexer ein streamTicketPaket(Byte-Array), dieses wird mit dem Methodenaufruf
	 * {@link StreamMultiplexer#setMaximumStreamTicketIndexForStream} an den StreamMultiplexer übergeben.
	 * Das Paket enthält den Index eines Streams und den neuen Index bis zu dem der Stream streamDataPackets verschicken darf.
	 * Beide Informationen werden vom StreamDemultiplexer in einem Byte-Array kodiert.
	 *
	 * Die Methode wird in einem StreamDemultilexer aufgerufen, wenn die gespeicherten Nutzdatenpakete
	 * eine gewisse Marke unterschreiten. Dadurch erhält der StreamMultiplexer die Erlaubnis weiter Nutdatenpakete zu verschicken.
	 *
	 * @param streamTicketPacket Dieses Paket wird vom StreamDemultiplexer zum StreamMultiplexer geschickt. Es hat den Index des Stream
	 * und den neuen maximalen Index, bis zu dem der StreamMultiplexer Pakete verschicken kann, als Inhalt.
	 */
	public void sendNewTicketIndexToSender(byte[] streamTicketPacket);
}

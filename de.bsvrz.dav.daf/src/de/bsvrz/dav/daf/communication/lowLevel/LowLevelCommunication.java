/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.KeepAliveTelegram;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.hexdump.HexDumper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Klasse zur Verwaltung der unteren Ebene von Datenverteilerverbindungen.
 * <p/>
 * Die Aufgabe dieser Klasse ist es Telegramme zu versenden, zu empfangen und KeepAlive-Mechanismus zu verwalten.
 * <p/>
 * Jede dieser Aufgaben wird durch mindestens einen Thread �bernommen:
 * <p/>
 * Empfang von Daten: ReceivingChannel (nimmt die Daten entgegen und legt diese in _receivingTable ab. Bei Konfigurationsanfragen kann es zur Zerst�cklung von
 * Telegrammen kommen, diese Teiltelgramme werden in _splittedTelegramsTable abgelegt und dort mit jedem empfang eines Teiltelegramms langsam zusammen gebaut
 * und nach Vollendung an ClientHighLevelCommunication durchgereicht (nicht �ber den Worker-Thread). Beim empfang von Daten wird der KeepAlive-Thread
 * benachrichtigt.), WorkerThread (bearbeitet die _receivingTable und reicht empfangene Telegramme an ClientHighLevelCommunication weiter).
 * <p/>
 * Versand von Daten: SendingChannel (verschickt Telegramme aus der _sendingTable und benachrichtigt den KeepAlive Thread, sobald Daten verschickt wurden. Pr�ft
 * die Laufzeit der einzelnen Telegramme und unterbricht die Verbindung sobald diese einen kritischen Wert unterschreitet).
 * <p/>
 * KeepAlive-Mechanismus: KeepAliveThread (Verwaltet ob ein "KeepAlive" Telegramm verschickt werden muss. Dies ist abh�ngig davon, ob eine Telegramm
 * versendet/empfangen wurde. Wurde eine bestimmte Anzahl Keepalive Telegramme versendet, so wird die Verbindung abgebaut. Wird nach dem versand eines KeepAlive
 * Telegramms ein normales Telegramm empfangen/gesendet, so wird der KeepAlive-Telegrammz�hler wieder auf 0 gesetzt. Anmerkung: Das KeepAlive Paket wird nicht
 * �ber den SendingChannel-Thread verschickt, sondern direkt �ber den Outputstream.)
 * <p/>
 * Alle vollst�ndigen Telegramme werden in zwei TelegramQueue-Objekten gespeichert (eine f�r den Empfang, eine f�r den Versand). Die oben beschriebenen Threads (ausser
 * der KeepAlive-Thread) arbeiten und synchronisieren sich auf den jeweiligen "PriorityTables".
 * <p/>
 * <p/>
 * Begriffsdefinitionen: Protokollsteuerung DaV-DAF (Klasse ClientHighLevelCommunication), Datenrepr�sentation (Klassen DataInputStream und DataOutputStream),
 * TCP-Kommunikationskanal (Klasse TCP_IP_Communication)
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13424 $
 */
public class LowLevelCommunication implements LowLevelCommunicationInterface {

	/** Wert f�r den <code>mode</code>-Parameter des Konstruktors, der bewirkt, dass Konfigurationsantworten wie normale Datentelegramme verarbeitet werden. */
	public static final byte NORMAL_MODE = 0;

	/**
	 * Wert f�r den <code>mode</code>-Parameter des Konstruktors, der bewirkt, dass Konfigurationsantworten ausgewertet und entsprechend an die h�here
	 * Kommunikationsebene weitergegeben werden.
	 */
	public static final byte HANDLE_CONFIG_RESPONCES_MODE = 1;

	private static final Debug _debug = Debug.getLogger();

	/** Die Kommunikationskomponente (TCP-IP...) */
	private ConnectionInterface _connection;

	/** Der Inputstream dieser Verbindung */
	private DataInputStream _inStream;

	/** Der Outputstream dieser Verbindung */
	private DataOutputStream _outStream;

	/** Der Empfangsthread dieser Kommunikation */
	private ReceivingChannel _receivingChannel;

	/** Der Sendethread dieser Kommunikation */
	private SendingChannel _sendingChannel;

	/** Der Aktuallisierungsthread */
	private WorkerThread _updater;

	/** Queue, in der zu versendende Telegramme zwischengespeichert werden. */
	private TelegramQueue<DataTelegram> _sendQueue;

	/** Queue, in der empfangene Telegramme zwischengespeichert werden. */
	private TelegramQueue<DataTelegram> _receiveQueue;

	/** Der Komponente die benachrichtigt werden soll, wenn eine neues Telegramm ankommt. */
	private HighLevelCommunicationCallbackInterface _highLevelComponent;

	/** Der KeepAlivethread */
	private KeepAliveThread _keepAliveThread;

	/** Die Zeit nach der sp�testens ein KeepAlive-Telegramm gesendet werden muss, wenn in dieser Zeit kein sonstiges Telegramm gesendet wurde. */
	private long _keepAliveSendTimeOut;

	/** Die Zeit in der sp�testens ein Telegramm empfangen werden muss. Wenn diese Zeit dreimal hintereinander abgelaufen ist, wird die Verbindung terminiert. */
	private long _keepAliveReceiveTimeOut;

	/** Verbindungsstatusinformation */
	private volatile boolean _disconnected;

	/**
	 * Der Modus dieser Verbindung
	 *
	 * @see #HANDLE_CONFIG_RESPONCES_MODE
	 * @see #NORMAL_MODE
	 */
	private int _mode;

	/** Tempor�re Liste der zerst�ckelten Telegramme */
	private SplittedApplicationTelegramsTable _splittedTelegramsTable;

	/** Kennung, die <code>true</code> ist, wenn ein Verbindungsterminierungstelegramm beim Schlie�en der Verbindung versendet werden soll. */
	private boolean _sendTerminationTelegramWhenClosing = true;

	private DataTelegram _terminationTelegram = null;

	private ThroughputChecker _throughputChecker;

	private volatile boolean _waitingForSendingChannel = false;

	private String _remotePrefix = "";

	private String _remoteName = "";

	private String _remoteAddress = "[-:-]";

	/**
	 * @param connection              Verbindungsobjekt �ber dass die Kommunikation mit dem Kommunikationspartner realisiert wird.
	 * @param sendBufferSize          Sendetabellenkapazit�t (in Byte)
	 * @param receiveBufferSize       Empfangstabellenkapazit�t (in Byte)
	 * @param keepAliveSendTimeOut    Zeitspanne in ms. Wird solange kein Telegramm verschickt, wird ein KeepAlive-Telegramm verschickt.
	 * @param keepAliveReceiveTimeOut Zeitspanne in ms. Wird solange keine Telegramm empfangen, wird ein Z�hler herabgesetzt. Erreicht der Z�hler 0 wird die
	 *                                Verbindung terminiert. Wird zwischendurch ein Telegramm empfangen, wird der Z�hler auf das maximum gesetzt.
	 * @param mode                    Modus f�r das Konfigurationsdatenverhalten. Falls hier der Wert {@link #HANDLE_CONFIG_RESPONCES_MODE} �bergeben wird, dann
	 *                                werden Konfigurationsantworten ausgewertet und entsprechend an die h�here Kommunikationsebene weitergegeben. Falls hier der
	 *                                Wert {@link #NORMAL_MODE} �bergeben wird, dann werden Konfigurationsantworten wie normale Datentelegramme verarbeitet.
	 * @param connected               Information, ob die Verbindung bereits erfolgt ist oder nicht (connected)
	 *
	 * @throws de.bsvrz.dav.daf.main.ConnectionException Wenn das Verbindungsobjekt sich nicht im erwarteten Zustand befindet.
	 */
	public LowLevelCommunication(
			ConnectionInterface connection,
			int sendBufferSize,
			int receiveBufferSize,
			long keepAliveSendTimeOut,
			long keepAliveReceiveTimeOut,
			byte mode,
			boolean connected) throws ConnectionException {
		if(connection == null) {
			throw new ConnectionException("Keine Kommunikationskomponente vorhanden.");
		}
		_connection = connection;
		_mode = mode;
		if(_mode == HANDLE_CONFIG_RESPONCES_MODE) {
			_splittedTelegramsTable = new SplittedApplicationTelegramsTable();
		}
		_sendQueue = new TelegramQueue<DataTelegram>(sendBufferSize, CommunicationConstant.MAX_PRIORITY);
		_throughputChecker = new ThroughputChecker();
		_receiveQueue = new TelegramQueue<DataTelegram>(receiveBufferSize, CommunicationConstant.MAX_PRIORITY);
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
		if(connected) {
			OutputStream outputStream = _connection.getOutputStream();
			if(outputStream == null) {
				throw new ConnectionException("Inkonsistente Kommunikationskomponente.");
			}
			_outStream = new DataOutputStream(new BufferedOutputStream(outputStream));

			InputStream inputStream = _connection.getInputStream();
			if(inputStream == null) {
				throw new ConnectionException("Inkonsistente Kommunikationskomponente.");
			}
			_inStream = new DataInputStream(new BufferedInputStream(inputStream));
			_disconnected = false;
			setRemoteAddress(_connection.getMainAdress(), _connection.getSubAdressNumber());
		}
		else {
			_disconnected = true;
		}
	}

	/**
	 * Diese Methode dient nur f�r automatisierte Tests und ist nicht f�r den realen Einsatz gedacht.
	 *
	 * @return KeepAlive-Thread oder <code>null</code> wenn dieser noch nicht aktiviert wurde.
	 */
	KeepAliveThread getKeepAliveThread() {
		return _keepAliveThread;
	}

	/**
	 * Diese Methode dient nur f�r automatisierte Tests und ist nicht f�r den realen Einsatz gedacht.
	 *
	 * @return ReceivingChannel-Thread oder <code>null</code> wenn dieser noch nicht aktiviert wurde.
	 */
	ReceivingChannel getReceivingChannel() {
		return _receivingChannel;
	}

	/**
	 * Diese Methode dient nur f�r automatisierte Tests und ist nicht f�r den realen Einsatz gedacht.
	 *
	 * @return SendingChannel-Thread oder <code>null</code> wenn dieser noch nicht aktiviert wurde.
	 */
	SendingChannel getSendingChannel() {
		return _sendingChannel;
	}

	/**
	 * Diese Methode dient nur f�r automatisierte Tests und ist nicht f�r den realen Einsatz gedacht.
	 *
	 * @return Updater-Thread oder <code>null</code> wenn dieser noch nicht aktiviert wurde.
	 */
	WorkerThread getUpdaterThread() {
		return _updater;
	}

	public final ConnectionInterface getConnectionInterface() {
		return _connection;
	}

	public final void setHighLevelComponent(HighLevelCommunicationCallbackInterface highLevelComponent) {
		if(highLevelComponent == null) throw new IllegalArgumentException("highLevelComponent darf nicht null sein");
		_highLevelComponent = highLevelComponent;

		_updater = new WorkerThread();
		_receivingChannel = new ReceivingChannel();
		_sendingChannel = new SendingChannel();
		_keepAliveThread = new KeepAliveThread();

		_updater.start();
		_receivingChannel.start();
		_sendingChannel.start();
		_keepAliveThread.start();
	}

	public HighLevelCommunicationCallbackInterface getHighLevelComponent() {
		return _highLevelComponent;
	}

	public final void updateKeepAliveParameters(long keepAliveSendTimeOut, long keepAliveReceiveTimeOut) {
		_debug.finer(getRemotePrefix() + "updateKeepAliveParameters keepAliveSendTimeOut", keepAliveSendTimeOut);
		_debug.finer(getRemotePrefix() + "updateKeepAliveParameters keepAliveReceiveTimeOut", keepAliveReceiveTimeOut);
		_keepAliveSendTimeOut = keepAliveSendTimeOut;
		_keepAliveReceiveTimeOut = keepAliveReceiveTimeOut;
		_keepAliveThread.timeoutsChanged();
	}

	public final void updateThroughputParameters(float throughputControlSendBufferFactor, long throughputControlInterval, int minimumThroughput) {
		_throughputChecker.setThroughputParameters(throughputControlSendBufferFactor, throughputControlInterval, minimumThroughput);
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Nach dem Verbindungsaufbau werden die Referenzen auf den Sende- und den Empfangskanal festgehalten. Diese werden von den Threads benutzt um Daten zu senden
	 * oder empfangen.
	 */
	public final void connect(String mainAddress, int subAddress) throws ConnectionException {
		if(_connection == null) {
			throw new ConnectionException("Keine Kommunikationskomponente vorhanden.");
		}
		_connection.connect(mainAddress, subAddress);

		OutputStream outputStream = _connection.getOutputStream();
		if(outputStream == null) {
			throw new ConnectionException("Inkonsistente Kommunikationskomponente.");
		}
		_outStream = new DataOutputStream(new BufferedOutputStream(outputStream));

		InputStream inputStream = _connection.getInputStream();
		if(inputStream == null) {
			throw new ConnectionException("Inkonsistente Kommunikationskomponente.");
		}
		_inStream = new DataInputStream(new BufferedInputStream(inputStream));

		setRemoteAddress(_connection.getMainAdress(), _connection.getSubAdressNumber());

		_disconnected = false;
	}

	public final boolean isNotConnected() {
		return _disconnected || (!_connection.isConnected());
	}

	/**
	 * Diese Methode wird von der Protokollschicht DaV-DAF aufgerufen, wenn ein Telegramm gesendet werden soll.
	 * <p/>
	 * F�gt ein Telegramm in die Sendetabelle ein.
	 *
	 * @param telegram Das zu versendende Telegramm.
	 */
	public final void send(DataTelegram telegram) {

		//_debug.finer("Sendeauftrag", telegram.toShortDebugString());
		try {
			_sendQueue.put(telegram);
			_throughputChecker.queuedTelegram();
		}
		catch(InterruptedException ignored) {
		}
	}

	public final void send(DataTelegram telegrams[]) {
		if(telegrams == null) {
			return;
		}
		for(int i = 0; i < telegrams.length; ++i) {
			if(telegrams[i] != null) {
				send(telegrams[i]);
			}
		}
	}

	/**
	 * Diese Methode wird von der Protokollschicht DaV-DAF aufgerufen, wenn die Kommunikationskan�le geschlossen werden sollen.
	 * <p/>
	 * Zun�chst wird der Empfangsthread geschlossen; dadurch werden keine Daten mehr empfangen. Danach wird der Keep-alive-Thread geschlossen. Daraufhin wird
	 * gewartet, bis der Sendethread alle Telegramme aus der Sendetabelle gesendet hat, damit keine Daten verloren gehen. Anschlie�end wird der Sendethread
	 * beendet. Zuletzt werden die Kommunikationskan�le geschlossen.
	 *
	 * @param error   Besagt, ob es sich um eine Terminierung mit Fehler handelt. Falls <code>true</code> werden s�mtliche noch zum Versand gepufferten Telegramme
	 * verworfen; falls <code>false</code> wird versucht, s�mtliche zum Versand gepufferten Telegramme zu versenden.
	 * @param message Ursache der Terminierung im Fehlerfall.
	 * @param terminationTelegram Das Telegramm, dass als letztes Telegramm vor dem Schlie�en der Verbindung versendet werden soll oder <code>null</code>, falls
	 * kein abschlie�endes Telegramm versendet werden soll.
	 */
	public final void disconnect(boolean error, String message, final DataTelegram terminationTelegram) {
		if(_waitingForSendingChannel) {
			_sendQueue.abort();
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
			}
			if(_sendingChannel != null) _sendingChannel.interrupt();
			return;
		}
		synchronized(this) {
			if(_disconnected) {
				return;
			}
			_disconnected = true;
		}
		_debug.fine(getRemotePrefix() + "LowLevelCommunication.disconnect: Verbindung wird geschlossen, error=" + error + ", message=" + message);
		if(_sendTerminationTelegramWhenClosing) {
			_sendTerminationTelegramWhenClosing = false;
			_terminationTelegram = terminationTelegram;
		}

		if(error) {
			_sendQueue.abort();
		}
		else {
			_sendQueue.close();
		}

		try {
			_waitingForSendingChannel = true;
			while(_sendQueue.getSize() > 0 && _connection.isConnected() && _sendingChannel != null && _sendingChannel.isAlive()) {
				_debug.fine("Warte auf den Versand von gepufferten Telegrammen");
				Thread.sleep(200);
			}
			_waitingForSendingChannel = false;
		}
		catch(InterruptedException e) {
			_debug.info(getRemotePrefix() + Thread.currentThread().getName() + " wurde beim Senden von gepufferten Telegrammen beim Verbindungsabbau unterbrochen");
		}

		if(_sendingChannel != null) {
			try {
				_sendingChannel.join(2000);
			}
			catch(InterruptedException ignored) {
			}
			_sendingChannel.interrupt();
		}

		if(_keepAliveThread != null) {
			_keepAliveThread.interrupt();
		}

		if(_receivingChannel != null) {
			_receivingChannel.interrupt();
		}

		if(error) {
			_receiveQueue.abort();
		}
		else {
			_receiveQueue.close();
		}
		try {
			while(_receiveQueue.getSize() > 0 && _connection.isConnected()) {
				_debug.fine("Warte auf die Verarbeitung von gepufferten empfangenen Telegrammen");
				Thread.sleep(200);
			}
		}
		catch(InterruptedException e) {
			_debug.info(getRemotePrefix() + Thread.currentThread().getName() + " wurde beim Verarbeiten von gepufferten Telegrammen beim Verbindungsabbau unterbrochen");
		}

		_connection.disconnect();

		try {
			if(_inStream != null) {
				_inStream.close();
			}
		}
		catch(IOException ex) {
		}
		try {
			if(_outStream != null) {
				_outStream.close();
			}
		}
		catch(IOException ex) {
		}
	}

	private void handleAbnormalBehaviour(boolean sendTermination, final String message) {
		if(!sendTermination) _sendTerminationTelegramWhenClosing = false;
		if(_highLevelComponent != null) {
			final Runnable runnable = new Runnable() {

				public void run() {
					_highLevelComponent.disconnected(true, message);
				}
			};
			Thread disconnectNotifierThread = new Thread(runnable, "LowLevelCommunication-disconnectNotifier");
			disconnectNotifierThread.setDaemon(true);
			disconnectNotifierThread.start();
		}
	}

	public String getSendBufferState() {
		try {
			return _throughputChecker.getSendBufferState();
		}
		catch(Exception e) {
			_debug.fine(getRemotePrefix() + "Fehler", e);
			return "?";
		}
	}

	public void setRemoteName(final String name) {
		_remoteName = name;
		setRemotePrefix();
	}

	public void setRemoteAddress(final String remoteAddress, int remotePort) {
		_remoteAddress = "[" + remoteAddress + ":" + remotePort + "]";
		setRemotePrefix();
	}

	private void setRemotePrefix() {
		_remotePrefix = _remoteName + _remoteAddress + ": ";
	}

	private String getRemotePrefix() {
		return _remotePrefix;
	}

	/**
	 * Dieser Thread verschickt Telegramme mittels einer Datenverteilerverbindung (Outputstream der Verbindung). Der Thread synchronisiert sich auf der
	 * Datenstruktur <code>_sendingTable</code>, die alle zu versendenden Telegramme enth�lt.
	 */
	class SendingChannel extends LowLevelThread {

		public SendingChannel() {
			super("SendingChannel");
		}

		public final void run() {
			_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.SendingChannel startet");
			try {
				DataTelegram telegram = null;
				while(!interrupted() && (telegram = _sendQueue.take()) != null) {
					synchronized(_outStream) {
//						_debug.info(">>>>>Telegram wird gesendet", telegram);
						_outStream.writeByte(telegram.getType());
						telegram.write(_outStream);
						_outStream.flush();
					}
					_keepAliveThread.sentTelegram();
					_throughputChecker.sentTelegram(telegram.getSize());
				}
				if(_terminationTelegram != null) {
					_debug.info(getRemotePrefix() + Thread.currentThread().getName() + " sendet ein Terminierungstelegramm, weil die Sende-Queue geschlossen wurde");
					_outStream.writeByte(_terminationTelegram.getType());
					_terminationTelegram.write(_outStream);
					_outStream.flush();
				}
				_debug.info(getRemotePrefix() + Thread.currentThread().getName() + " beendet sich jetzt weil die Sende-Queue geschlossen wurde");
			}
			catch(InterruptedException e) {
				_debug.info(getRemotePrefix() + Thread.currentThread().getName() + " wurde unterbrochen und beendet sich jetzt");
				_sendQueue.abort();
			}
			catch(IOException ex) {
				handleAbnormalBehaviour(
						false, "Verbindung wird wegen eines Kommunikationsfehlers beim Senden terminiert: " + ex.getMessage()
				);
			}
			finally {
				_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.SendingChannel beendet sich");
			}
			return;
		}

		@Override
		public LowLevelCommunication getLowLevelCommunication() {
			return LowLevelCommunication.this;
		}
	}

	class ReceivingChannel extends LowLevelThread {

		private ReceivingChannel() {
			super("ReceivingChannel");
		}

		/**
		 * Liest ein Telegramm von der Kommunikationsverbindung ein.
		 *
		 * @return gelesenes Telegramm
		 *
		 * @throws IOException              Wenn beim Lesen Fehler aufgetreten sind.
		 * @throws IllegalArgumentException Wenn ein Telegramm mit einem unbekannten Typ empfangen wurde
		 */
		private DataTelegram readNextTelegram() throws IOException {
			byte type = _inStream.readByte();
			DataTelegram telegram = DataTelegram.getTelegram(type);
			if(telegram == null) {
				int availableBytes = _inStream.available();
				byte[] bytes = new byte[availableBytes];
				_inStream.readFully(bytes);
				_debug.warning(getRemotePrefix() + "Telegramm mit unbekanntem Typ " + type + " empfangen:\n" + HexDumper.toString(bytes));
				throw new IllegalArgumentException(getRemotePrefix() + "Telegramm mit unbekanntem Typ empfangen: " + type);
			}
			telegram.read(_inStream);
			return telegram;
		}

		/**
		 * Verarbeitung von Telegrammen, die vorrangig ber�cksichtigt werden m�ssen.
		 *
		 * @param telegram Zu verarbeitendes Telegramm.
		 *
		 * @return <code>true</code>, falls das Telegramm verarbeitet wurde; <code>false</code> falls das Telegramm nicht verarbeitet wurde.
		 */
		private boolean handleWithoutQueueing(final DataTelegram telegram) {
			if(_mode == HANDLE_CONFIG_RESPONCES_MODE) {
				if(telegram.getType() == DataTelegram.APPLICATION_DATA_TELEGRAM_TYPE) {
					ApplicationDataTelegram applicationDataTelegram = (ApplicationDataTelegram)telegram;
					BaseSubscriptionInfo info = applicationDataTelegram.getBaseSubscriptionInfo();
					if(info != null) {
						if(AttributeGroupUsageIdentifications.isConfigurationReply(info.getUsageIdentification())) {
							SendDataObject receivedData = null;
							int maxTelegramNumber = applicationDataTelegram.getTotalTelegramsCount();
							if(maxTelegramNumber == 1) {
								receivedData = TelegramUtility.getSendDataObject(applicationDataTelegram);
							}
							else {
								ApplicationDataTelegram telegramArray[] = _splittedTelegramsTable.put(applicationDataTelegram);
								if(telegramArray != null) {
									receivedData = TelegramUtility.getSendDataObject(telegramArray);
								}
							}
							if(receivedData != null) {
								_highLevelComponent.updateConfigData(receivedData);
							}
							return true;
						}
					}
				}
			}
			return false;
		}

		/** Empf�ngt Telegramme von der Kommunikationsverbindung und gibt sie zur Verarbeitung weiter */
		public final void run() {
			_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.ReceivingChannel startet");
			try {
				while(!interrupted()) {
					if(_inStream == null) {
						return;
					}
					try {
						_keepAliveThread.startReceiving();
						DataTelegram telegram = readNextTelegram();
						_keepAliveThread.receivedTelegram();
						if(handleWithoutQueueing(telegram)) continue;
						_receiveQueue.put(telegram);
					}
					catch(EOFException ex) {
						_debug.fine(getRemotePrefix() + "EOFException beim Lesen eines Telegramms", ex);
						if(!_disconnected && !interrupted()) {
							try {
								Thread.sleep(2000);
							}
							catch(InterruptedException ignored) {
							}
						}
						if(!_disconnected && !interrupted()) handleAbnormalBehaviour(false, "TCP-Verbindung wurde von der Gegenseite geschlossen: " + ex);
						return;
					}
					catch(IOException ex) {
						_debug.fine(getRemotePrefix() + "IOException beim Lesen eines Telegramms", ex);
						if(!_disconnected) handleAbnormalBehaviour(false, "Kommunikationsfehler beim Lesen eines Telegramms: " + ex);
						return;
					}
					catch(IllegalArgumentException ex) {
						// Unbekannter Telegrammtyp gelesen
						if(!_disconnected) handleAbnormalBehaviour(true, ex.getMessage());
						return;
					}
					catch(InterruptedException e) {
						return;
					}
				}
			}
			finally {
				_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.ReceivingChannel beendet sich");
			}
		}

		@Override
		public LowLevelCommunication getLowLevelCommunication() {
			return LowLevelCommunication.this;
		}
	}

	class WorkerThread extends LowLevelThread {

		private WorkerThread() {
			super("LLWorker");
		}

		/** The run loop method of this _connection thread */
		public final void run() {
			_debug.fine("Thread LowLevelCommunication.WorkerThread startet");
			try {
				DataTelegram telegram = null;
				while(!interrupted() && (telegram = _receiveQueue.take()) != null) {
					try {
						_highLevelComponent.update(telegram);
					}
					catch(RuntimeException e) {
						_debug.warning(getRemotePrefix() + "Ausnahme bei der Verarbeitung eines empfangenen Telegramms: " + telegram, e);
						e.printStackTrace();
					}
				}
			}
			catch(InterruptedException e) {
				return;
			}
			finally {
				_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.WorkerThread beendet sich");
			}
		}

		@Override
		public LowLevelCommunication getLowLevelCommunication() {
			return LowLevelCommunication.this;
		}
	}

	private enum ThroughputCheckerState {

		EMPTY_BUFFER,
		FULL_BUFFER,
		CHECKING_THROUGHPUT
	}

	private class ThroughputChecker {

		/** Anzahl Bytes im Sendepuffer, ab dem die Durchsatzpr�fung gestartet wird. */
		private int _buffersizeThreshold;

		/** Die Zeit zwischen zwei Messungen der Durchsatzpr�fung in Millisekunden */
		private long _controlInterval;

		/** Minimaler Sendedurchsatz f�r die Durchsatzpr�fung in Bytes pro Sekunde */
		private int _minimumThroughput;

		ThroughputCheckerState _state;

		private long _stateChangeTime;


		private int _numberOfBytesSent;

		private long _lastCheckedThroughput;

		public ThroughputChecker() {
			setThroughputParameters(
					CommunicationConstant.FLOW_CONTROL_FACTOR, CommunicationConstant.THROUGHPUT_CONTROL_INTERVAL, CommunicationConstant.MINIMUM_THROUGHPUT
			);
			setState(ThroughputCheckerState.EMPTY_BUFFER);
		}

		/**
		 * Diese Methode setzt die Parameter f�r die Durchsatzpr�fung.
		 *
		 * @param throughputControlSendBufferFactor
		 *                                  F�llungsgrad des Sendepuffers als Faktor zwischen 0 und 1, ab dem die Durchsatzpr�fung anf�ngt zu arbeiten.
		 * @param throughputControlInterval Zeit zwischen zwei Durchsatzpr�fungen in Millisekunden
		 * @param minimumThroughput         Minimal zul�ssiger Verbindungsdurchsatz in Bytes pro Sekunde
		 */
		public synchronized final void setThroughputParameters(float throughputControlSendBufferFactor, long throughputControlInterval, int minimumThroughput) {
			if(throughputControlInterval <= 0) {
				throw new IllegalArgumentException(getRemotePrefix() + "Pr�fintervall f�r Durchsatzpr�fung ist zu klein: " + throughputControlInterval + " ms");
			}
			if(minimumThroughput <= 0) {
				throw new IllegalArgumentException(getRemotePrefix() + "Minimal Durchsatz f�r Durchsatzpr�fung ist zu klein: " + _controlInterval + " Byte/s");
			}
			if(throughputControlSendBufferFactor <= 0.0) {
				throw new IllegalArgumentException(getRemotePrefix() + "Pufferf�llgrad f�r Durchsatzpr�fung ist zu klein: " + throughputControlSendBufferFactor);
			}
			if(throughputControlSendBufferFactor >= 1.0) {
				throw new IllegalArgumentException(getRemotePrefix() + "Pufferf�llgrad f�r Durchsatzpr�fung ist zu gro�: " + throughputControlSendBufferFactor);
			}
			_buffersizeThreshold = (int)(throughputControlSendBufferFactor * _sendQueue.getCapacity());
			_controlInterval = throughputControlInterval;
			_minimumThroughput = minimumThroughput;
		}

		private void setState(final ThroughputCheckerState state) {
			_debug.fine(getRemotePrefix() + "Zustand der Durchsatzpr�fung", state);
			_debug.fine(getRemotePrefix() + "noch zu versendende Daten ", _sendQueue.getSize() + " Byte, Grenze: " + _buffersizeThreshold + " Byte");
			_state = state;
			_stateChangeTime = System.currentTimeMillis();
			_numberOfBytesSent = 0;
			_lastCheckedThroughput = -1;
		}

		public synchronized void queuedTelegram() {
			switch(_state) {
				case EMPTY_BUFFER:
					if(_sendQueue.getSize() > _buffersizeThreshold) {
						setState(ThroughputCheckerState.FULL_BUFFER);
					}
					break;
				case FULL_BUFFER:
					// fall through
				case CHECKING_THROUGHPUT:
					if(_sendQueue.getSize() < _buffersizeThreshold) {
						setState(ThroughputCheckerState.EMPTY_BUFFER);
					}
					break;
			}
		}

		public synchronized void sentTelegram(int telegramSize) {
			switch(_state) {
				case EMPTY_BUFFER:
					break;
				case FULL_BUFFER:
					if(_sendQueue.getSize() < _buffersizeThreshold) {
						setState(ThroughputCheckerState.EMPTY_BUFFER);
					}
				case CHECKING_THROUGHPUT:
					if(_sendQueue.getSize() < _buffersizeThreshold) {
						setState(ThroughputCheckerState.EMPTY_BUFFER);
					}
					else {
						_numberOfBytesSent += telegramSize;
					}
					break;
			}
		}

		/**
		 * Pr�ft den Durchsatz und liefert die Zeit bis zur n�chsten Pr�fung zur�ck.
		 *
		 * @return Zeit bis zur n�chsten Pr�fung in Millisekunden
		 *
		 * @throws IllegalStateException wenn ein zu geringer Durchsatz festgestellt wurde.
		 */
		public synchronized long checkThroughput() {
			switch(_state) {
				case EMPTY_BUFFER:
					break;
				case FULL_BUFFER:
					long bufferfullTimeout = _controlInterval - (System.currentTimeMillis() - _stateChangeTime);
					if(bufferfullTimeout > 0) {
						return bufferfullTimeout;
					}
					setState(ThroughputCheckerState.CHECKING_THROUGHPUT);
					break;
				case CHECKING_THROUGHPUT:
					long checkingTime = System.currentTimeMillis() - _stateChangeTime;
					long checkingTimeout = _controlInterval - checkingTime;
					if(checkingTimeout > 0) {
						return checkingTimeout;
					}
					final long throughput = (long)_numberOfBytesSent * 1000L / checkingTime;
					_debug.info(getRemotePrefix() + "Sendedurchsatz: " + throughput + " Byte/s");
					if(throughput < _minimumThroughput) {
						_lastCheckedThroughput = throughput;
						throw new IllegalStateException(getRemotePrefix() + "Sendedurchsatz war in den letzten " + checkingTime + " ms zu gering: " + throughput + " Byte/s");
					}
					setState(ThroughputCheckerState.CHECKING_THROUGHPUT);
					_lastCheckedThroughput = throughput;
					break;
			}
			return _controlInterval;
		}

		/**
		 * Liefert einen beschreibenden Text mit dem Zustand des Sendepuffers
		 * @return Zustand des Sendepuffers
		 */
		public synchronized String getSendBufferState() {
			final StringBuilder text = new StringBuilder();
			text.append(_sendQueue.getSize()).append(" Byte");
			switch(_state) {
				case EMPTY_BUFFER:
					break;
				case FULL_BUFFER:
					text.append(", Puffer voll");
					break;
				case CHECKING_THROUGHPUT:
					text.append(", Durchsatzpr�fung");
					if(_lastCheckedThroughput >= 0) text.append(" ").append(_lastCheckedThroughput).append("Byte/s"); 
					break;
			}
			return text.toString();
		}
	}

	/**
	 * Dieser Thread verschickt Keepalive Telegramme und baut die Verbindung ab, wenn dreimal nacheinander eine bestimmte Zeit lang keine Daten mehr empfangen
	 * wurden.
	 * <p/>
	 * KeepAlive-Thread Senden: Wurde einen bestimmten Zeitraum kein Telegramm mehr verschickt, wird ein Keep-Alive Telegramm verschickt.
	 * <p/>
	 * KeepAlive-Thread Empfangen: Wurde dreimal nacheinander eine bestimmte Zeit kein Telegramm empfangen, wird die Verbindung abgebaut.
	 */
	class KeepAliveThread extends LowLevelThread {

		/** Maximale Anzahl f�r das Ablaufen des Empfangstimeouts bevor die Verbindung terminiert wird. */
		private static final byte MAX_SOULS = 3;

		/** Aktuelle noch verbleibende Anzahl f�r das Ablaufen des Empfangstimeouts bevor die Verbindung terminiert wird. */
		private int _souls;

		/** Zeitpunkt, an dem zuletzt angefangen wurde auf den Empfang von Daten zu warten */
		private long _lastStartReceivingTime;

		/** Zeit des letzten Empfangs von Daten */
		private long _lastReceivingTime;

		/** Zeit des letzten Versands von Daten */
		private long _lastSendingTime;

		/** Zeit des letzten zum Versand eingetragenen KeepAlive-Telegramms */
		private long _lastQueuedKeepAliveTime;

		/**
		 * Um diesen Faktor wird die Anzahl der Versuche vor der Terminierung erh�ht, wenn gar nicht versucht wird, Keep-Alive-Telegramme (oder andere) zu laden
		 */
		private static final int NOT_RECEIVING_MULTIPLIER = 3;

		private boolean _waitingForData;

		public KeepAliveThread() {
			super("KeepAlive");
			_souls = MAX_SOULS;
			final long now = System.currentTimeMillis();
			_lastReceivingTime = now;
			_lastSendingTime = now;
			_lastQueuedKeepAliveTime = now;
		}

		public void startReceiving() {
			synchronized(this) {
				_lastStartReceivingTime = System.currentTimeMillis();
				_waitingForData = true;
				_souls = MAX_SOULS;
			}
		}

		public void receivedTelegram() {
			synchronized(this) {
				_lastReceivingTime = System.currentTimeMillis();
				_waitingForData = false;
				_souls = MAX_SOULS * NOT_RECEIVING_MULTIPLIER;
			}
		}

		public void sentTelegram() {
			synchronized(this) {
				_lastSendingTime = System.currentTimeMillis();
			}
		}

		public void timeoutsChanged() {
			synchronized(this) {
				// run-Methode s.u. soll das wait vorzeitig verlassen und die neuen Einstellungen ber�cksichtigen
				this.notifyAll();
			}
		}

		/**
		 * Diese Methode dient dazu JUnit-Tests zu unterst�tzen.
		 *
		 * @return Anzahl Versuche, bis die Verbindung terminiert wird. Sobald ein Telegramm verschickt wird, wird <code>_souls</code> wieder auf den maximalen Wert
		 *         gesetzt.
		 */
		int getSouls() {
			synchronized(this) {
				return _souls;
			}
		}

		public final void run() {
			_debug.fine(getRemotePrefix(), "Thread LowLevelCommunication.KeepAliveThread startet");
			try {
				while(!interrupted()) {
					synchronized(this) {
						try {
							long lastSendOrQueuedTime = _lastSendingTime < _lastQueuedKeepAliveTime ? _lastQueuedKeepAliveTime : _lastSendingTime;
							long sendingRemainingTime = _keepAliveSendTimeOut - (System.currentTimeMillis() - lastSendOrQueuedTime);
							if(sendingRemainingTime <= 0) {
								// Wenn noch ein Telegramm in der sendQueue ist, dann wird das KeepAliveTelegramm unterdr�ckt, weil �berfl�ssig
								if(_connection.isConnected() && (_outStream != null) && _sendQueue.getSize() == 0) {
									DataTelegram telegram = new KeepAliveTelegram();
									_sendQueue.put(telegram);
									_throughputChecker.queuedTelegram();
								}
								_lastQueuedKeepAliveTime = System.currentTimeMillis();
								sendingRemainingTime = _keepAliveSendTimeOut;
							}

							long receivingRemainingTime = _keepAliveReceiveTimeOut - (System.currentTimeMillis() - _lastReceivingTime);

							if(receivingRemainingTime <= 0) {
								final long now = System.currentTimeMillis();
								final long deltaSinceLastReceive = now - _lastReceivingTime;
								final long deltaSinceLastStartReceive = now - _lastStartReceivingTime;
								_lastReceivingTime = now;
								receivingRemainingTime = _keepAliveReceiveTimeOut;
								--_souls;
								if(_waitingForData) {
									_debug.fine(
											getRemotePrefix() + "Seit " + deltaSinceLastReceive + " ms wurden keine Telegramme mehr empfangen, verbleibende Versuche: " + _souls
									);
								}
								else {
									_debug.fine(
											getRemotePrefix() + "Seit " + deltaSinceLastStartReceive
											+ " ms konnten keine Telegramme mehr empfangen werden, weil die Empfangswarteschlange voll ist, verbleibende Versuche: "
											+ _souls
									);
								}
							}
							if(_souls <= 0) {
								_debug.error(getRemotePrefix() + "Die Verbindung wird terminiert, weil keine Telegramme mehr empfangen werden.");
								if(_waitingForData) {
									handleAbnormalBehaviour(
											false,
											"Es wurden " + MAX_SOULS + " mal in Folge f�r jeweils " + (_keepAliveReceiveTimeOut / 1000)
											+ " Sekunden keine KeepAlive- oder sonstige Telegramme empfangen"
									);
								} else {
									handleAbnormalBehaviour(
											false,
											"Es wurden " + (MAX_SOULS * NOT_RECEIVING_MULTIPLIER) + " mal in Folge f�r jeweils " + (_keepAliveReceiveTimeOut / 1000)
											+ " Sekunden keine KeepAlive- oder sonstige Telegramme empfangen, weil die Empfangswarteschlange voll ist"
									);
								}
								return;
							}
							long waitTime = sendingRemainingTime < receivingRemainingTime ? sendingRemainingTime : receivingRemainingTime;

							// Durchsatzpr�fung durchf�hren und Zeit bis zur n�chsten Pr�fung ermitteln
							try {
								final long throughputCheckWaitTime = _throughputChecker.checkThroughput();
								if(throughputCheckWaitTime < waitTime) waitTime = throughputCheckWaitTime;
							}
							catch(IllegalStateException e) {
								// Durchsatz zu gering
								_debug.error(getRemotePrefix() + "Die Verbindung wird terminiert: " + e.getMessage());
								handleAbnormalBehaviour(false, e.getMessage());
								return;
							}

							// Hier wird gewartet, bis eine der Restzeiten abgelaufen ist.
							// Wenn die Timeoutparameter sich �ndern, wird durch ein notify aus der Methode timeoutsChanged
							// das wait() vorzeitig beendet.
							if(waitTime > 0) this.wait(waitTime);
						}
						catch(InterruptedException e) {
							return;
						}
					}
				}
			}
			catch(Exception e) {
				_debug.error(getRemotePrefix() + "unerwartete Ausnahme im KeepAliveThread", e);
			}
			finally {
				_debug.fine(getRemotePrefix() + "Thread LowLevelCommunication.KeepAliveThread beendet sich");
			}
		}

		@Override
		public LowLevelCommunication getLowLevelCommunication() {
			return LowLevelCommunication.this;
		}
	}
}

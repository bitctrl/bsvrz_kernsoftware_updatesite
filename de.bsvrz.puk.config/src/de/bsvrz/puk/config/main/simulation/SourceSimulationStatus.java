/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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

package de.bsvrz.puk.config.main.simulation;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Diese Klasse meldet sich als Quelle für die Attributgruppe "atg.simulationsStatusKonfiguration" an und stellt verschiedene Methoden zur Verfügung mit denen
 * festgelegte Datensätze verschickt werden können.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class SourceSimulationStatus {

	private final ClientDavInterface _connection;

	private final DataDescription _dataDescription;

	private final Object _stateLock = new Object();

	private final ClientSender _clientSender;

	SystemObject _simulationObject;

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	boolean _subscribed = false;

	/**
	 * Erzeugt das Objekt und meldt sich als Sender für die Attributgruppe "atg.simulationsStatusKonfiguration" als Quelle an.
	 *
	 * @param connection        Verbidnung, auf der sich angemeldet wird
	 * @param simulationObject  Objekt, mit dem der Sender angemeldet wird
	 * @param simulationVariant Simulationsvariante, mit der sich der Sender anmeldet
	 *
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData
	 *
	 */
	public SourceSimulationStatus(final ClientDavInterface connection, SystemObject simulationObject, short simulationVariant)
			throws OneSubscriptionPerSendData {
		_connection = connection;
		_simulationObject = simulationObject;

		final DataModel dataModel = _connection.getDataModel();

		final AttributeGroup attributeGroup = dataModel.getAttributeGroup("atg.simulationsStatusKonfiguration");

		final Aspect aspect = dataModel.getAspect("asp.zustand");

		// atg.simulationsStatusKonfiguration:asp.zustand wird immer mit Simulationsvariante 0 versendet
		_dataDescription = new DataDescription(attributeGroup, aspect, (short)0);
		_clientSender = new ClientSender();

		_connection.subscribeSender(_clientSender, simulationObject, _dataDescription, SenderRole.source());
		_subscribed = true;
	}

	public void sendReady() throws SendSubscriptionNotConfirmed {
		if(_subscribed) {
			final Data data = _connection.createData(_dataDescription.getAttributeGroup());
			data.getUnscaledValue("KonfigurationZustand").setText("bereit");
			sendData(data);
		}
	}

	public void sendNotReady() throws SendSubscriptionNotConfirmed {
		if(_subscribed) {
			final Data data = _connection.createData(_dataDescription.getAttributeGroup());
			data.getUnscaledValue("KonfigurationZustand").setText("nicht bereit");
			sendData(data);
		}
	}

	/** Verschickt einen Datensatz, der den Empfänger mitteiler, dass das Simulationobjekt gelöscht werden kann (TPuK1-126). */
	public void sendDelete() throws SendSubscriptionNotConfirmed {
		if(_subscribed) {
			final Data data = _connection.createData(_dataDescription.getAttributeGroup());
			data.getUnscaledValue("KonfigurationZustand").setText("gelöscht");
			sendData(data);
		}
	}

	/**
	 * Meldet den Sender ab. Wurde der Sender bereits abgemeldet, wurd nichts gemacht. Wird nach Abmeldung weiter versucht Daten zu verschicken, werden diese nicht
	 * verschickt.
	 */
	public void unsubscribe() {
		if(_subscribed) {
			_connection.unsubscribeSender(_clientSender, _simulationObject, _dataDescription);
			_subscribed = false;
		}
	}

	private void sendData(final Data data) throws SendSubscriptionNotConfirmed {
		final ResultData result = new ResultData(_simulationObject, _dataDescription, System.currentTimeMillis(), data);
		_connection.sendData(result);
	}


	private final class ClientSender implements ClientSenderInterface {

		private byte _state = ClientSenderInterface.STOP_SENDING;

		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			// Da als Quelle gesendet wird, kann dieser Mechanismus ausgelassen werden
			synchronized(_stateLock) {
				_state = state;
				_stateLock.notifyAll();
			}
		}

		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			// Es wurde als Quelle angemeldet, die Sendesteuerung muss nicht beachtet werden
			return false;
		}

		public byte getState() {
			synchronized(_stateLock) {
				return _state;
			}
		}
	}
}

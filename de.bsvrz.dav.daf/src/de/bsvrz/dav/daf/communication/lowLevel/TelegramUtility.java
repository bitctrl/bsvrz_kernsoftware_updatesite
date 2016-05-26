/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataTelegram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Diese Hilfsklasse stellt Methoden zur Verfügung, mit denen DatenTelegramme zerlegt und wieder zusammengefügt werden können.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TelegramUtility {

	private TelegramUtility() {

	}

	/**
	 * Diese Methode zerteilt ein Daten Telegramm, wenn es eine maximale Größe überschreitet in mehrere Teiltelegramme. Wenn ein Datensatz == null ist, wird ein
	 * <code>ApplictionDataTelegram</code> ohne Nutzdaten erzeugt und zurückgegeben.
	 *
	 * @param dataToSend Der zusendende Datensatz
	 *
	 * @return Gibt ein Array mit den Teiltelegrammen zurück.
	 *
	 * @see #getSendDataObject(de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram[])
	 */
	public static ApplicationDataTelegram[] splitToApplicationTelegrams(SendDataObject dataToSend) {
		byte data[] = dataToSend.getData();
		// erzeugt leeres Telegramm, da keine Nutzdaten vorhanden, beendet Methode
		if(data == null) {
			ApplicationDataTelegram telegram = new ApplicationDataTelegram(
					dataToSend.getBaseSubscriptionInfo(),
					dataToSend.getDataNumber(),
					dataToSend.getDalayedDataFlag(),
					dataToSend.getErrorFlag(),
					null,
					getPriority(dataToSend),
					null,
					1,
					0,
					dataToSend.getDataTime()
			);
			return new ApplicationDataTelegram[]{telegram};
		}

		int i = 0, telegramNumber = 0;
		//offset entweder 0 oder 1 (Wenn die Zahl, die geteilt wird, einen Rest hat. Dann muss dieser Rest ebenfalls verschickt werden. Dafür wird allerdings ein Paket benötigt)
		final int offset = (data.length % CommunicationConstant.MAX_SPLIT_THRESHOLD) == 0 ? 0 : 1;
		final int maxTelegrams = offset + (data.length / CommunicationConstant.MAX_SPLIT_THRESHOLD);
		final byte priority = getPriority(dataToSend);
		final long time = dataToSend.getDataTime();
		final ApplicationDataTelegram telegrams[] = new ApplicationDataTelegram[maxTelegrams];

		while(telegramNumber < maxTelegrams) {
			int position = telegramNumber * CommunicationConstant.MAX_SPLIT_THRESHOLD;
			int stillToBeProcessed = data.length - position;
			int byteLength = stillToBeProcessed < CommunicationConstant.MAX_SPLIT_THRESHOLD ? stillToBeProcessed : CommunicationConstant.MAX_SPLIT_THRESHOLD;
			byte bytes[] = new byte[byteLength];
			System.arraycopy(data, position, bytes, 0, byteLength);
			telegrams[i++] = new ApplicationDataTelegram(
					dataToSend.getBaseSubscriptionInfo(),
					dataToSend.getDataNumber(),
					dataToSend.getDalayedDataFlag(),
					dataToSend.getErrorFlag(),
					dataToSend.getAttributesIndicator(),
					priority,
					bytes,
					maxTelegrams,
					telegramNumber++,
					time
			);
		}
		return telegrams;
	}


	/**
	 * Diese Methode baut aus dem Übergabeparameter ein vollständiges Datentelegramm und gibt dieses zurück.
	 * <p>
	 * Das Datentelegram wurde zuvor in Teilstücke zerlegt, da es eine maximale Größe überschritten hatte.
	 *
	 * @param receivedData Array, bestehend aus Teilstücken des Datentelegramms
	 *
	 * @return gibt einen vollständigen Datensatz zurück. Gibt null zurück wenn <code>IOException</code> geworfen wurde
	 *
	 * @throws IllegalArgumentException die Exception wird geworfen wenn:<br> - Übergabeparameter ist eine Referenz auf <code>null </code><br> - Die Länge des
	 *                                  Übergabeparametrs stimmt nicht mit der erwarteten Länge überein.<br> - wenn ein Element des Arrays eien Referenz auf
	 *                                  <code>null</code> ist.<br> - wenn ein Element an der falschen Position im Array steht.  <br>
	 * @see #splitToApplicationTelegrams(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject)
	 */
	public static SendDataObject getSendDataObject(ApplicationDataTelegram[] receivedData) {
		if(receivedData == null) {
			throw new IllegalArgumentException("Die übergebenen Daten sind leer\n");
		}
		if(receivedData[0].getTotalTelegramsCount() != receivedData.length) {
			throw new IllegalArgumentException("Die übergebenen Daten sind nicht vollständig\n");
		}
		SendDataObject sendDataObject = null;
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		try {
			ApplicationDataTelegram telegram;
			for(int i = 0; i < receivedData.length; ++i) {
				telegram = receivedData[i];
				if(telegram == null) {
					throw new IllegalArgumentException("Datum an Position " + i + " ist leer\n");
				}
				if(telegram.getTelegramNumber() != i) {
					throw new IllegalArgumentException("Datum an Position " + i + " hat einen falschen Index: " + receivedData[i].getTelegramNumber() + "\n");
				}
				byte byteArray[] = telegram.getData();
				if(byteArray != null) {
					byteBuffer.write(byteArray);
				}
			}
			telegram = receivedData[0];
			sendDataObject = new SendDataObject(
					telegram.getBaseSubscriptionInfo(),
					telegram.getDelayedDataFlag(),
					telegram.getDataNumber(),
					telegram.getDataTime(),
					telegram.getErrorFlag(),
					telegram.getAttributesIndicator(),
					byteBuffer.toByteArray()
			);
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		return sendDataObject;
	}

	/**
	 * Diese Methode übersetzt das Telegramm in ein <code>sendDataObject</code>.
	 *
	 * @param telegram Das vollständige DatenTelegramm.
	 *
	 * @return gibt ein <code>sendDataObject</code> zurück.
	 *
	 * @throws IllegalArgumentException die Exception wird geworfen wenn:<br> - Übergabeparameter ist eine Referenz auf <code>null </code><br> - Die Zahl der
	 *                                  enthaltene Telegramme größer als 1 ist<br>
	 */
	public static SendDataObject getSendDataObject(ApplicationDataTelegram telegram) {
		if(telegram == null) {
			throw new IllegalArgumentException("Das übergebene Datum ist leer\n");
		}
		if(telegram.getTotalTelegramsCount() > 1) {
			throw new IllegalArgumentException("Das übergebene Datum ist inkonsistent (>1)\n");
		}

		return new SendDataObject(
				telegram.getBaseSubscriptionInfo(),
				telegram.getDelayedDataFlag(),
				telegram.getDataNumber(),
				telegram.getDataTime(),
				telegram.getErrorFlag(),
				telegram.getAttributesIndicator(),
				telegram.getData()
		);
	}

	/**
	 * Diese Methode liefert die Priorität eines Telegramms.
	 *
	 * @param applicationData Telegramm, dessen Priorität zu ermitteln ist.
	 *
	 * @return Priorität des Telegramms. Ist der Übergabeparameter eine Refernz auf null wird -1 zurückgegeben.
	 */
	public static byte getPriority(SendDataObject applicationData) {
		if(applicationData == null) {
			return -1;
		}
		BaseSubscriptionInfo baseSubscriptionInfo = applicationData.getBaseSubscriptionInfo();
		if(baseSubscriptionInfo.getSimulationVariant() > 0) {
			return CommunicationConstant.SIMULATION_DATA_TELEGRAM_PRIORITY;
		}
		if(applicationData.getDalayedDataFlag()) {
			return CommunicationConstant.DELAYED_DATA_TELEGRAM_PRIORITY;
		}
		if(AttributeGroupUsageIdentifications.isUsedForConfigurationRequests(baseSubscriptionInfo.getUsageIdentification())) {
			return CommunicationConstant.CONFIGURATION_DATA_TELEGRAM_PRIORITY;
		}
		return CommunicationConstant.ONLINE_DATA_TELEGRAM_PRIORITY;
	}

	/**
	 * Diese Methode liefert die Priorität eines Telegramms.
	 *
	 * @param telegram Datentelegramm, dessen Priorität zu ermitteln ist.
	 *
	 * @return Priorität des Telegramms. Ist der Übergabeparameter eine Refernz auf null wird -1 zurückgegeben.
	 */
	public static byte getPriority(ApplicationDataTelegram telegram) {
		if(telegram == null) {
			return -1;
		}
		BaseSubscriptionInfo baseSubscriptionInfo = telegram.getBaseSubscriptionInfo();
		if(baseSubscriptionInfo.getSimulationVariant() > 0) {
			return CommunicationConstant.SIMULATION_DATA_TELEGRAM_PRIORITY;
		}
		if(telegram.getDelayedDataFlag()) {
			return CommunicationConstant.DELAYED_DATA_TELEGRAM_PRIORITY;
		}
		if(AttributeGroupUsageIdentifications.isUsedForConfigurationRequests(baseSubscriptionInfo.getUsageIdentification())) {
			return CommunicationConstant.CONFIGURATION_DATA_TELEGRAM_PRIORITY;
		}
		return CommunicationConstant.ONLINE_DATA_TELEGRAM_PRIORITY;
	}

	/**
	 * Diese Methode liefert die Priorität eines Telegramms.
	 *
	 * @param telegram Transmissionstelegramm, dessen Priorität zu ermitteln ist.
	 *
	 * @return Priorität des Telegramms. Ist der Übergabeparameter eine Referenz auf null wird -1 zurückgegeben.
	 */
	public static byte getPriority(TransmitterDataTelegram telegram) {
		if(telegram == null) {
			return -1;
		}
		BaseSubscriptionInfo baseSubscriptionInfo = telegram.getBaseSubscriptionInfo();
		if(baseSubscriptionInfo.getSimulationVariant() > 0) {
			return CommunicationConstant.SIMULATION_DATA_TELEGRAM_PRIORITY;
		}
		if(telegram.getDelayedDataFlag()) {
			return CommunicationConstant.DELAYED_DATA_TELEGRAM_PRIORITY;
		}
		if(AttributeGroupUsageIdentifications.isUsedForConfigurationRequests(baseSubscriptionInfo.getUsageIdentification())) {
			return CommunicationConstant.CONFIGURATION_DATA_TELEGRAM_PRIORITY;
		}
		return CommunicationConstant.ONLINE_DATA_TELEGRAM_PRIORITY;
	}
}

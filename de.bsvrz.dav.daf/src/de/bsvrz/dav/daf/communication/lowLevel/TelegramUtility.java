/*
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataTelegram;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Diese Hilfsklasse stellt Methoden zur Verf�gung, mit denen DatenTelegramme zerlegt und wieder zusammengef�gt werden k�nnen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11572 $
 */
public class TelegramUtility {

	private TelegramUtility() {

	}

	/**
	 * Diese Methode zerteilt ein Daten Telegramm, wenn es eine maximale Gr��e �berschreitet in mehrere Teiltelegramme. Wenn ein Datensatz == null ist, wird ein
	 * <code>ApplictionDataTelegram</code> ohne Nutzdaten erzeugt und zur�ckgegeben.
	 *
	 * @param dataToSend Der zusendende Datensatz
	 *
	 * @return Gibt ein Array mit den Teiltelegrammen zur�ck.
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
		//offset entweder 0 oder 1 (Wenn die Zahl, die geteilt wird, einen Rest hat. Dann muss dieser Rest ebenfalls verschickt werden. Daf�r wird allerdings ein Paket ben�tigt)
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
	 * Diese Methode baut aus dem �bergabeparameter ein vollst�ndiges Datentelegramm und gibt dieses zur�ck.
	 * <p/>
	 * Das Datentelegram wurde zuvor in Teilst�cke zerlegt, da es eine maximale Gr��e �berschritten hatte.
	 *
	 * @param receivedData Array, bestehend aus Teilst�cken des Datentelegramms
	 *
	 * @return gibt einen vollst�ndigen Datensatz zur�ck. Gibt null zur�ck wenn <code>IOException</code> geworfen wurde
	 *
	 * @throws IllegalArgumentException die Exception wird geworfen wenn:<br> - �bergabeparameter ist eine Referenz auf <code>null </code><br> - Die L�nge des
	 *                                  �bergabeparametrs stimmt nicht mit der erwarteten L�nge �berein.<br> - wenn ein Element des Arrays eien Referenz auf
	 *                                  <code>null</code> ist.<br> - wenn ein Element an der falschen Position im Array steht.  <br>
	 * @see #splitToApplicationTelegrams(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject)
	 */
	public static SendDataObject getSendDataObject(ApplicationDataTelegram[] receivedData) {
		if(receivedData == null) {
			throw new IllegalArgumentException("Die �bergebenen Daten sind leer\n");
		}
		if(receivedData[0].getTotalTelegramsCount() != receivedData.length) {
			throw new IllegalArgumentException("Die �bergebenen Daten sind nicht vollst�ndig\n");
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
	 * Diese Methode �bersetzt das Telegramm in ein <code>sendDataObject</code>.
	 *
	 * @param telegram Das vollst�ndige DatenTelegramm.
	 *
	 * @return gibt ein <code>sendDataObject</code> zur�ck.
	 *
	 * @throws IllegalArgumentException die Exception wird geworfen wenn:<br> - �bergabeparameter ist eine Referenz auf <code>null </code><br> - Die Zahl der
	 *                                  enthaltene Telegramme gr��er als 1 ist<br>
	 */
	public static SendDataObject getSendDataObject(ApplicationDataTelegram telegram) {
		if(telegram == null) {
			throw new IllegalArgumentException("Das �bergebene Datum ist leer\n");
		}
		if(telegram.getTotalTelegramsCount() > 1) {
			throw new IllegalArgumentException("Das �bergebene Datum ist inkonsistent (>1)\n");
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
	 * Diese Methode liefert die Priorit�t eines Telegramms.
	 *
	 * @param applicationData Telegramm, dessen Priorit�t zu ermitteln ist.
	 *
	 * @return Priorit�t des Telegramms. Ist der �bergabeparameter eine Refernz auf null wird -1 zur�ckgegeben.
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
	 * Diese Methode liefert die Priorit�t eines Telegramms.
	 *
	 * @param telegram Datentelegramm, dessen Priorit�t zu ermitteln ist.
	 *
	 * @return Priorit�t des Telegramms. Ist der �bergabeparameter eine Refernz auf null wird -1 zur�ckgegeben.
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
	 * Diese Methode liefert die Priorit�t eines Telegramms.
	 *
	 * @param telegram Transmissionstelegramm, dessen Priorit�t zu ermitteln ist.
	 *
	 * @return Priorit�t des Telegramms. Ist der �bergabeparameter eine Referenz auf null wird -1 zur�ckgegeben.
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

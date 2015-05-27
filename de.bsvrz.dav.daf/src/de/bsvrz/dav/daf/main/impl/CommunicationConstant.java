/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

/**
 * Enth�lt Konstanten, die f�r die Kommunikation mit dem Datenverteiler ben�tigt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13144 $
 */
public class CommunicationConstant {

	/** Pid der lokalen Konfiguration. */
	public static final String LOCALE_CONFIGURATION_PID_ALIASE = "Lokale_Konfiguration";

	/** Pid der Konfiguration. */
	public static final String CONFIGURATION_TYPE_PID = "typ.konfigurationsApplikation";

	/** Pid der Parametrierung. */
	public static final String PARAMETER_TYPE_PID = "typ.parametrierungsApplikation";

	/** Die maximale Wartezeit auf eine synchrone Antwort. */
	public static long MAX_WAITING_TIME_FOR_SYNC_RESPONCE = 600000;// Millisekunden

	/** Die maximale Wartezeit auf eine Konfigurationsanmeldung beim Datenverteiler. */
	public static long MAX_WAITING_TIME_FOR_CONNECTION = 10000000; // Millisekunden

	/** Die Wartezeit zwichen zwei Konfigurationsanmeldungsversuchen beim Datenverteiler. */
	public static long SLEEP_TIME_WAITING_FOR_CONNECTION = 10000; // 10 second

	/** Prozentualer Anteil des Cache, um die Flusskontrolle zu starten. */
	public static float FLOW_CONTROL_FACTOR = 0.75f;

	/** Die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung. */
	public static long THROUGHPUT_CONTROL_INTERVAL = 60000;// 1 minute

	/** Minimale Verbindungsdurchsatz */
	public static int MINIMUM_THROUGHPUT = 3000;// 3Kb/s

	/** Die maximale Anzahl in Bytes, die ein Teiltelegramm an Daten haben kann. */
	public static int MAX_SPLIT_THRESHOLD = 3000;

	/** Die Verz�gerungszeit zur �bertragung von gepufferten und zu versendenden Telegrammen. */
	public static long MAX_SEND_DELAY_TIME = 0;

	/** Der maximale Priorit�tswert in diesem System */
	public static final byte MAX_PRIORITY = 7;

	/** Die Priorit�t f�r bestimmte Systemtelegramme. Der Wert ist h�her als der von {@link #SYSTEM_TELEGRAM_PRIORITY}. */
	public static byte SYSTEM_HIGH_TELEGRAM_PRIORITY = 7;

	/** Die Priorit�t der Systemtelegramme. */
	public static byte SYSTEM_TELEGRAM_PRIORITY = 5;

	/** Die Priorit�t der Konfigurationsanfrage. */
	public static byte CONFIGURATION_DATA_TELEGRAM_PRIORITY = 5;

	/** Die Priorit�t der Onlinedatentelegramme. */
	public static byte ONLINE_DATA_TELEGRAM_PRIORITY = 5;

	/** Die Priorit�t der nachgelieferte Datentelegramme. */
	public static byte DELAYED_DATA_TELEGRAM_PRIORITY = 5;

	/** Die Priorit�t der Simulationsdatentelegramme. */
	public static byte SIMULATION_DATA_TELEGRAM_PRIORITY = 5;

	/** Die Startzeit der Applikation. */
	public static final long START_TIME = (((System.currentTimeMillis() / 1000L) << 32) & 0xFFFFFFFF00000000L);
}

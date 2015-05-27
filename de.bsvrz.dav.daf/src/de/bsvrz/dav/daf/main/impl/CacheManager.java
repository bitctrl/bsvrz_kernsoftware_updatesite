/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.DataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Die interne Subkomponente Cache-Manager ist f�r das Speichern der ankommenden Daten und f�r die Bereitstellung bereits gespeicherte Daten zust�ndig. Die
 * gespeicherten Daten werden nach ihrem Index sortiert festgehalten. Ein im Hintergrund laufender Thread, der CacheCleaner, sorgt daf�r, dass die Daten, nach
 * ihrer beim Anmelden angegebenen Verweilzeit, aus dem Cache gel�scht werden. Diese Subkomponente wird von ClientDavConnection erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13457 $
 */
public class CacheManager {

	private static final Debug _debug = Debug.getLogger();

	/** Der Anmeldemanager */
	private SubscriptionManager subscriptionManager;

	/** Der Konfigurationsmanager */
//	private ConfigurationManager configurationManager;

	private final DataModel _dataModel;

	/** Der Datens�tzecache. Als Key dient die BaseSubscriptionInfo, der Value ist eine Liste von CachedObject-Objekten. */
	private Hashtable cache;

	/** Der Verwalter der Datens�tze im Cache */
	private CacheCleaner cleaner;

	/**
	 * Dieser Konstruktor erzeugt eine Instanz und h�lt eine Referenz auf die Subkomponenten Anmeldemanager und Konfigurationsmanager fest. Auch eine Instanz des
	 * CacheCleaners wird gestartet.
	 *
	 * @param _subscriptionManager  Im Konstruktor wird die Methode {@link SubscriptionManager#setCacheManager(CacheManager)} aufgerufen und
	 *                              dieses Objekt �bergeben.
	 * @param _configurationManager Wird zum anfordern des Datenmodells ben�tigt.
	 */
	public CacheManager(SubscriptionManager _subscriptionManager, ConfigurationManager _configurationManager) {
		this(_subscriptionManager, _configurationManager.getDataModel());
	}

	/**
	 * Dieser Konstruktor erzeugt eine Instanz und h�lt eine Referenz auf die Subkomponenten Anmeldemanager und Konfigurationsmanager fest. Auch eine Instanz des
	 * CacheCleaners wird gestartet.
	 *
	 * @param _subscriptionManager Im Konstruktor wird die Methode {@link SubscriptionManager#setCacheManager(CacheManager)} aufgerufen und
	 *                             dieses Objekt �bergeben.
	 * @param dataModel            Datenmodell
	 */
	CacheManager(final SubscriptionManager _subscriptionManager, final DataModel dataModel) {
		subscriptionManager = _subscriptionManager;
		_dataModel = dataModel;

		subscriptionManager.setCacheManager(this);
		cache = new Hashtable();
		cleaner = new CacheCleaner();
		cleaner.start();
	}

	/**
	 * Diese Methode wird von der Protokollsteuerung DaV-DAF aufgerufen, wenn ein aktuelles Datum angekommen ist. Die Datens�tze, die als Bytestrom ankommen,
	 * werden zuerst in Attribute umgewandelt, und ein neues CachedObject wird gebildet. Dieses wird nach dem Datensatzindex sortiert in den Cache eingef�gt, und
	 * die SubscriptionManager- Subkomponente wird �ber das Ankommen des aktuellen Datums benachrichtigt.
	 *
	 * @param newData Neuer Datensatz
	 * @throws InterruptedException Wenn der Thread w�hrend eines blockierenden Aufrufs unterbrochen wurde
	 */
	public void update(SendDataObject newData) throws InterruptedException {
		if(newData == null) {
			throw new IllegalArgumentException("Falsche �bergabeparameter");
		}
		BaseSubscriptionInfo baseSubscriptionInfo = newData.getBaseSubscriptionInfo();
		if(baseSubscriptionInfo == null) {
			return;
		}
		// Get the Datavalues out of the stream
		byte attributesIndicator[] = newData.getAttributesIndicator();
//		DataValue values[] = null;
		final byte[] dataBytes = newData.getData();
		final Data data;
//		final DataModel configuration = configurationManager.getDataModel();
		if(dataBytes == null) {
			data = null;
		}
		else {
			try {
				final AttributeGroup atg = _dataModel.getAttributeGroupUsage(baseSubscriptionInfo.getUsageIdentification()).getAttributeGroup();
				if(attributesIndicator != null) {
					throw new RuntimeException(
							"Anmeldungen auf einzelne Attribute der Attributgruppe werden nicht unterst�tzt. " + "atg: " + atg.getPid()
					);
				}
				data = DataFactory.forVersion(1).createUnmodifiableData(atg, dataBytes);

//				DataInputStream in = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
//				values = StreamFetcher.getInstance().getDataValuesFromStream(
//				        configurationManager.getDataModel(),
//				        baseSubscriptionInfo.getAttributeGroupCode(),
//				        in,
//				        attributesIndicator
//				);
			}
//			catch(IOException ex) {
//				ex.printStackTrace();
//				return;
//			}
			catch(ConfigurationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		boolean delayedDataFlag = newData.getDalayedDataFlag();
		byte errorFlag = newData.getErrorFlag();
		long dataIndex = newData.getDataNumber();
		boolean dataInside = ((errorFlag == 0x00) && ((dataIndex & 0x0000000000000003) == 0) && (data != null));
		LinkedList list = (LinkedList)cache.get(baseSubscriptionInfo);
//		System.out.println("++++++++++++list:" + (list == null ? "null" : list.size() + " entries") + "+++++++++++");
		CachedObject cachedObject = null;
		if(list == null) {
			// Im Cache war noch kein Eintrag f�r diese Datenidentifikation enthalten

			list = new LinkedList();
			cachedObject = new CachedObject(
					baseSubscriptionInfo, delayedDataFlag, dataIndex, newData.getDataTime(), errorFlag, _dataModel
			);
			if(dataInside) {
				cachedObject.update(attributesIndicator, data, delayedDataFlag);
			}
			cachedObject.setActionTime(System.currentTimeMillis());
			list.add(cachedObject);
			cache.put(baseSubscriptionInfo, list);
		}
		else {
			// list enth�lt die schon im Cache gespeicherten Datens�tze dieser Datenidentifikation
			synchronized(list) {
				ListIterator _iterator = list.listIterator(list.size());
				while(_iterator.hasPrevious()) {
					CachedObject _cachedObject = (CachedObject)_iterator.previous();
					long result = _cachedObject.getDataNumber() - dataIndex;
					if(result == 0) {
						// Der letzte Datensatz im Cache hatte die gleiche Telegrammnummer

						cachedObject = new CachedObject(
								baseSubscriptionInfo, delayedDataFlag, dataIndex, newData.getDataTime(), errorFlag, _dataModel
						);
						if(dataInside) {
							cachedObject.update(attributesIndicator, data, delayedDataFlag);
						}
						cachedObject.setActionTime(System.currentTimeMillis());
						_iterator.set(cachedObject);
						break;
					}
					else if(result < 0) {
						// Der letzte Datensatz im Cache hatte eine kleinere Telegrammnummer als der neue Datensatz

						cachedObject = new CachedObject(
								baseSubscriptionInfo, delayedDataFlag, dataIndex, newData.getDataTime(), errorFlag, _dataModel
						);
						if(dataInside) {
							cachedObject.update(attributesIndicator, data, delayedDataFlag);
						}
						cachedObject.setActionTime(System.currentTimeMillis());

						// Wenn der Vorhaltezeitraum der Daten 0 ist und der Datensatz nicht nachgeliefert ist, dann
						// wird der bisher aktuelle  Datensatz im Cache durch den gerade empfangenen ersetzt.
						if(!delayedDataFlag && (subscriptionManager.getTimeInCache(baseSubscriptionInfo) == 0)) {
							_iterator.set(cachedObject);
						}
						else {
							_iterator.next();
							_iterator.add(cachedObject);
						}
						break;
					}
					else {
						// Ein R�cksprung des Datensatzindex ist zugelassen, wenn die Verbindung zur Quelle zeitweise unterbrochen war und nach dem leeren
						// Datensatz (zur Markierung des Ausfalls mit einem um 1 erh�hten Datensatzindex) nach erneutem Verbindungsaufbau wieder der (i.a.)
						// bereits vorher empfangene aktuelle Datensatz mit einem um 1 erniedrigten Datensatzindex empfangen wird.
						if(((dataIndex & 0x0000000000000003L) == 0) && (_cachedObject.getDataNumber() != (dataIndex + 1))) {
							final SystemObject object = _dataModel.getObject(baseSubscriptionInfo.getObjectID());
							String objectName = (object == null) ? "null" : object.getPidOrNameOrId();

							final long usageIdentification = baseSubscriptionInfo.getUsageIdentification();
							AttributeGroupUsage atgUsage = _dataModel.getAttributeGroupUsage(usageIdentification);
							final long oldIndex = _cachedObject.getDataNumber();

							_debug.error(
									"Empfangener Datensatz hat ung�ltigen Datensatzindex, Objekt: " + objectName + ", Attributgruppenverwendung: "
									+ (atgUsage == null ? String.valueOf(usageIdentification) : atgUsage.getPid()) + ", letzter Index: " + (oldIndex >>> 32)
									+ "#" + ((oldIndex & 0xffffffffL) >> 2) + "#" + (oldIndex & 3) + ", aktueller Index: " + (dataIndex >>> 32) + "#"
									+ ((dataIndex & 0xffffffffL) >> 2) + "#" + (dataIndex & 3)
							);
							//_cachedObject.getData()
						}
						else {
							cachedObject = new CachedObject(
									baseSubscriptionInfo, delayedDataFlag, dataIndex, newData.getDataTime(), errorFlag, _dataModel
							);
							if(dataInside) {
								cachedObject.update(attributesIndicator, data, delayedDataFlag);
							}
							cachedObject.setActionTime(System.currentTimeMillis());

							// Wenn der Vorhaltezeitraum der Daten 0 ist und der Datensatz nicht nachgeliefert ist, dann
							// wird der bisher aktuelle  Datensatz im Cache durch den gerade empfangenen ersetzt.
							if(!delayedDataFlag && (subscriptionManager.getTimeInCache(baseSubscriptionInfo) == 0)) {
								_iterator.set(cachedObject);
							}
							else {
								_iterator.next();
								_iterator.add(cachedObject);
							}
							break;
						}
						break;
					}
				}
			}
		}
		if(cachedObject != null) subscriptionManager.actualDataUpdate(cachedObject);
	}

	/** Schliesst diese Komponente und beendet den Thread <code>CacheCleaner</code> */
	public final void close() {
		if(cleaner != null) {
			cleaner.interrupt();
		}
	}

	/**
	 * Diese Methode wird f�r JUnit-Tests ben�tigt.
	 *
	 * @return Thread, der den Cache aufr�umt.
	 */
	Thread getCleaner() {
		return cleaner;
	}

	/**
	 * Diese Methode wird vom SubscriptionManager aufgerufen. Wenn keine Anmeldung der spezifizierten Daten (mehr) vorliegt, bewirkt der Aufruf dieser Methode,
	 * dass die entsprechenden Datens�tze aus dem Cache entfernt werden.
	 *
	 * @param baseSubscriptionInfo Alle Daten, die zu dieser Anmeldeinformationen vorhanden sind, werden aus dem Cache entfernt.
	 */
	final void cleanCache(BaseSubscriptionInfo baseSubscriptionInfo) {
		cache.remove(baseSubscriptionInfo);
	}

	/**
	 * Gibt den aktuellen Datensatz der spezifizierten Daten zur�ck. Wenn der Datensatz kein nachgelieferter sein darf, dann wird der letzte nicht nachgeliefert
	 * Datensatz zur�ckgegeben. Ist kein passender Datensatz vorhanden, so wird <code>null</code> zur�ckgegeben.
	 *
	 * @param baseSubscriptionInfo Anmeldeinformation eines Datensatzes
	 * @param delayedDataFlag      Nachgelieferte Daten
	 *
	 * @return Datensatz, der die Parameter erf�llt oder <code>null</code>, falls kein Datensatz vorhanden ist.
	 */
	public final CachedObject getLastValueOfCachedData(BaseSubscriptionInfo baseSubscriptionInfo, boolean delayedDataFlag) {
		if(baseSubscriptionInfo == null) {
			return null;
		}
		LinkedList list = (LinkedList)cache.get(baseSubscriptionInfo);
		if(list == null) {
			return null;
		}
		synchronized(list) {
			ListIterator _iterator = list.listIterator(list.size());
			while(_iterator.hasPrevious()) {
				CachedObject _cachedObject = (CachedObject)_iterator.previous();
				if(_cachedObject != null) {
					if(!delayedDataFlag && _cachedObject.getDelayedDataFlag()) {
						continue;
					}
					_cachedObject.setActionTime(System.currentTimeMillis());
					return _cachedObject;
				}
			}
		}
		return null;
	}

	/**
	 * Diese Methode iteriert durch den Cache-Inhalt und sammelt die letzen n Datens�tze, vom aktuellen Datensatz startend in die Vergangenheit. Wenn keine
	 * nachgelieferten erw�nscht sind, so werden alle nachgelieferte Daten �bersprungen. Ist kein passender Datensatz vorhanden, so wird null zur�ckgegeben. Sind
	 * weniger als n Datens�tze vorhanden, so werden diese zur�ckgegeben.
	 *
	 * @param baseSubscriptionInfo Anmeldeinformation eines Datensatzes
	 * @param delayedDataFlag      Nachgelieferte Daten
	 * @param n                    Anzahl der g�ltigen Versionen eines Datensatzes
	 *
	 * @return Datens�tze/satz oder <code>null</code>, falls kein Datensatz vorhanden ist. Der Datensatz, der als letztes gecasht wurde steht an Index 0.
	 */
	public final List getCachedData(
			BaseSubscriptionInfo baseSubscriptionInfo, boolean delayedDataFlag, int n
	) {
		if(baseSubscriptionInfo == null) {
			return null;
		}
		LinkedList list = (LinkedList)cache.get(baseSubscriptionInfo);
		if(list == null) {
			return null;
		}
		ArrayList arrayList = new ArrayList();
		int index = 0;
		synchronized(list) {
			ListIterator _iterator = list.listIterator(list.size());
			while(_iterator.hasPrevious()) {
				CachedObject _cachedObject = (CachedObject)_iterator.previous();
				long number = _cachedObject.getDataNumber();
				byte error = (byte)(number & 0x0000000000000003);
				if(error > 0) {
					arrayList.add(_cachedObject);
					_cachedObject.setActionTime(System.currentTimeMillis());
					++index;
				}
				else {
					if(!delayedDataFlag && _cachedObject.getDelayedDataFlag()) {
						continue;
					}
					else {
						arrayList.add(_cachedObject);
						_cachedObject.setActionTime(System.currentTimeMillis());
						++index;
					}
				}
				if(index == n) {
					break;
				}
			}
		}
		return arrayList;
	}

	/**
	 * Diese Methode iteriert durch den Cache-Inhalt und sammelt die Datens�tze, deren Zeitstempel zwischen den spezifizierten Zeiten liegt. Wenn keine
	 * nachgelieferten erw�nscht sind, so werden alle nachgelieferte Daten �bersprungen. Ist kein passender Datensatz vorhanden, so wird <code>null</code>
	 * zur�ckgegeben.
	 *
	 * @param baseSubscriptionInfo Anmeldeinformation eines Datensatzes
	 * @param delayedDataFlag      Nachgelieferte Daten
	 * @param fromTime             Start Zeitintervall. Wird der Wert -1 �bergeben, so wird die Datenzeit des zuletzt gecachten Objekts benutzt.
	 * @param toTime               Ende Zeitintervall. Wird der Wert -1 �bergeben, so wird die Datenzeit des zuletzt gecachten Objekts benutzt.
	 *
	 * @return Datens�tze/Datensatz f�r den die �bergebenen Parameter erf�llt sind oder <code>null</code> wenn kein Datensatz vorhanden ist
	 */
	public final List getCachedData(
			BaseSubscriptionInfo baseSubscriptionInfo, boolean delayedDataFlag, long fromTime, long toTime
	) {
		if(baseSubscriptionInfo == null) {
			return null;
		}
		LinkedList list = (LinkedList)cache.get(baseSubscriptionInfo);
		if(list == null) {
			return null;
		}
		ArrayList arrayList = new ArrayList();
		synchronized(list) {
			CachedObject lastCachedObject = (CachedObject)list.getLast();
			if(lastCachedObject == null) {
				return null;
			}
			if(fromTime == -1) {
				fromTime = lastCachedObject.getDataTime();
			}
			if(toTime == -1) {
				toTime = lastCachedObject.getDataTime();
			}
			if(toTime < fromTime) {
				long t = fromTime;
				fromTime = toTime;
				toTime = fromTime;
			}
			ListIterator _iterator = list.listIterator(0);
			while(_iterator.hasNext()) {
				CachedObject _cachedObject = (CachedObject)_iterator.next();
				long number = _cachedObject.getDataNumber();
				byte error = (byte)(number & 0x0000000000000003);
				if(error > 0) {
					long time = _cachedObject.getDataTime();
					if((time >= fromTime) && (time <= toTime)) {
						arrayList.add(_cachedObject);
						_cachedObject.setActionTime(System.currentTimeMillis());
					}
				}
				else {
					if(!delayedDataFlag && _cachedObject.getDelayedDataFlag()) {
						continue;
					}
					else {
						long time = _cachedObject.getDataTime();
						if((time >= fromTime) && (time <= toTime)) {
							arrayList.add(_cachedObject);
							_cachedObject.setActionTime(System.currentTimeMillis());
						}
					}
				}
			}
		}
		return arrayList;
	}

	class CacheCleaner extends Thread {

		CacheCleaner() {
			super("CacheCleaner");
		}

		public void run() {
			int loopCount = 0;
			final String debugEnabledSetting = System.getProperty("de.bsvrz.dav.daf.main.impl.CacheManager.CacheCleaner.run.debug", "nein").trim().toLowerCase();
			final boolean debugEnabled;
			if(debugEnabledSetting.startsWith("n")) {
				debugEnabled = false;
			}
			else {
				debugEnabled = true;
			}
			while(!interrupted()) {
				try {
					sleep(10000);
					final long startTime = System.currentTimeMillis();
					// Alle Daten, die sich im Cache befinden (Jedes Element der Liste entspricht den gecachten Daten(Liste) einer BaseSubscriptionInfo)
					ArrayList list = new ArrayList(cache.values());
					final int numberOfSubscription = list.size();
					int numberOfCheckedDatasets = 0;
					int numberOfDeletedDatasets = 0;

					for(int i = list.size() - 1; i > -1; --i) {
						// Alle Daten im Cache, die zu einer BaseSubscriptionInfo geh�ren
						LinkedList allCachedDataOfABaseSubscriptionInfo = (LinkedList)list.get(i);
						if(allCachedDataOfABaseSubscriptionInfo != null) {
							synchronized(allCachedDataOfABaseSubscriptionInfo) {

								// Mit diesem Iterator wird zuerst das letzte Element genommen. Dann wird der Iterator erneut initialisiert, diesmal
								// wird vorw�rts �ber die Liste gelaufen.
								ListIterator _iterator = allCachedDataOfABaseSubscriptionInfo.listIterator(allCachedDataOfABaseSubscriptionInfo.size());
								if(_iterator.hasPrevious()) {
									CachedObject _cachedObject = (CachedObject)_iterator.previous();
									if(_cachedObject != null) {
										// Wie lange darf sich ein Objekte im Cache befinden, bevor es gel�scht wird
										final long timeInHistory = subscriptionManager.getTimeInCache(_cachedObject.getBaseSubscriptionInfo());
										// Zeitpunkt, ab dem Objekte gel�scht werden m�ssen (Dieser Zeitpunkt wird ausgehend vom letzten Objekte im Cache ausgehend berechnet) 
										final long thresholdTime = _cachedObject.getActionTime() - timeInHistory;

										// Der Iterator wird vorw�rts durchlaufen
										_iterator = allCachedDataOfABaseSubscriptionInfo.listIterator(0);
										while(_iterator.hasNext()) {
											_cachedObject = (CachedObject)_iterator.next();
											if(_cachedObject != null) {
												numberOfCheckedDatasets ++;
												if(_cachedObject.getActionTime() < thresholdTime) {
													// Die If-Abfrage verhindert, dass das letzte Element gel�scht wird
													if(_iterator.hasNext()) {
														numberOfDeletedDatasets ++;
														_iterator.remove();
													}
												}
												else {
													break;
												}
											}
										}
									}
								}
							}
						}
					}
					final long endTime = System.currentTimeMillis();
					if(debugEnabled) {
						long duration = endTime - startTime;
						_debug.info("CacheCleanerlauf hat in " + duration + "ms " + numberOfSubscription + " Anmeldungen mit " + numberOfCheckedDatasets + " gespeicherten Datens�tzen gepr�ft und " + numberOfDeletedDatasets + " Datens�tze gel�scht");
					}
//					// Garbage Collection nur nach jedem 10. Durchlauf aufrufen.
//					if((++loopCount % 10) == 0) {
//						long t0 = System.currentTimeMillis();
//						System.gc();
//						long t1 = System.currentTimeMillis();
//						_debug.finer("CacheCleaner garbage collection used " + (t1 - t0) + "ms");
//					}
					yield();
				}
				catch(InterruptedException ex) {
					return;
				}
			}
		}
	}
}

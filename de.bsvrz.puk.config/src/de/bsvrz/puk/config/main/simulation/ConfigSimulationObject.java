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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet;
import de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dieses Objekt stellt eine Simulation aus Sicht der Konfiguration dar. Das Objekt meldet sich auf alle nötigen Attributgruppen an und verschickt die
 * benötigten Datensätze.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class ConfigSimulationObject {

	private static final Debug _debug = Debug.getLogger();

	private final ClientDavInterface _connection;

	private SimulationStates _simulationState;

	/** Datenidentifikation für Statusänderungen */
	private final DataDescription _stateDataDescription;

	private final StateChangeReceiver _stateChangeReceiver;

	private final SimulationStates _newSimulation;

	private final SimulationStates _ready;

	private final SimulationStates _notReady;

	private final SimulationStates _deleted;

	/**
	 * true = Es handlet sich um eine Offline-Simulation. Es stehen Zeitstempel zur Verfügung. false = Es handelt sich um eine Online-Simulation, es stehen keine
	 * Zeitstempel zur Verfügung.
	 */
	private final boolean _offlineSimulation;

//	/** Startzeitpunkt der Simulation. Die initiale Wert zeigt an, das der Wert noch nicht gesetzt wurde. */
//	private long _simulationBeginOfflineSimulation = Long.MIN_VALUE;
//
//	/** Endzeitpunkt der Simulation. Die initiale Wert zeigt an, das der Wert noch nicht gesetzt wurde. */
//	private long _simulationEndOfflineSimulation = Long.MIN_VALUE;

	/** Dieses Objekt kann den Zustand der Simulation propagieren. Es findet eine Quellenanmeldung/Abmeldung statt. */
	private final SourceSimulationStatus _senderSimulationStatus;

	/**
	 * Auf dieses Objekt wird synchronisiert, sobald mit der Variablen <code>_simulationState</code> gearbeitet wird. Es kann nicht auf die Variable selbst gelockt
	 * werden, da sich diese mit jeder Zustandänderung ändert.
	 */
	private final Object _stateLock = new Object();

	/**
	 * Speichert alle Mengentypen, die bei Simulationen speziell behandelt werden müssen.
	 * <p>
	 * Bei speziellen Mengentypen muss beim hinzufügen von Elementen zu dieser Art von Mengen das Element in der Simulationsvariante der Simulation gespeichert
	 * werden.
	 * <p>
	 * Beim lesenden Zugriff werden nur Elemente der Simulationsvariante angefordert.
	 * <p>
	 * Alle Mengen, zu denen Elemente hinzugefügt wurden, werden um diese Elemte bereinigt wenn die dynamischen Objekte gelöscht werden (Vorstart, löschen der
	 * Simulation).
	 */
	private Set<ObjectSetType> _specialObjectSetTypes = new HashSet<ObjectSetType>();

	/**
	 * Enthält alle dynamischen Typen, deren Objekte gesondert behandelt werden müssen. Soll ein dynamisches Objekt mit einem dieser Typen erzeugt werden, so muss
	 * die Simulationsvariante berücksichtigt werden.
	 */
	private Set<DynamicObjectType> _specialDynamicTypes = new HashSet<DynamicObjectType>();

	/** Simulationsvariante der Simulation */
	private short _simulationVariant;

	final private SystemObject _simulationObject;

	/**
	 * Das Objekt spiegelt die Simulationsstrecke wieder. Von diesem Objekte können die spezielle zu behandelnden Mengen/Objekte angefordert werden. Siehe auch
	 * <code>_simulationsStreckenBeschreibung</code>.
	 */
	final private SystemObject _simulationsStreckenObjekt;

	/**
	 * Alle Eigenschaften der Simulation, die Mengen und Objekte betreffen, die speziell behandelt werden sollen. Das Simulationsobjekt wird beim Neustart und
	 * Vorstart den aktuellen Datensatz anfordern und die Datenstrukturen entsprechend anpassen.
	 */
	private DataDescription _simulationsStreckenBeschreibung;

	/**
	 * Dieses Objekt stellt aus Sicht der Konfiguration eine Simulation dar. Das Objekt meldet sich als Empfänger für die Zustände der Simulation an und als
	 * Sender
	 *
	 * @param connection
	 * @param simulationObject          SystemObject vom Typ typ.simulation.
	 * @param configurationQueryManager Objekt, mit denen das Objekt die Konfiguration beauftragen kann, einen Empfänger für Konfigurationsanfragen für diese
	 *                                  Simulationsvariante anzumelden/abzumelden
	 *
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData
	 *
	 */
	public ConfigSimulationObject(
			ClientDavInterface connection, SystemObject simulationObject, ConfigurationQueryManager configurationQueryManager)
			throws OneSubscriptionPerSendData {

		_connection = connection;
		_simulationObject = simulationObject;

		final DataModel dataModel = _connection.getDataModel();
		final AttributeGroup atgSimulationProperties = dataModel.getAttributeGroup("atg.simulationsEigenschaften");

		// Welche Simulationsvariante wird benutzt
		final Data configurationData = _simulationObject.getConfigurationData(atgSimulationProperties);
		if(configurationData == null) {
			throw new IllegalStateException(
					"Die Simulationsvariante des Simulationsobjekts " + _simulationObject.getPid()
					+ " wurde nicht in einem konfigurierendem Datensatz gespeichert"
			);
		}

		_simulationVariant = configurationData.getUnscaledValue("SimulationsVariante").shortValue();

		if(_simulationVariant <= 0) {
			throw new IllegalArgumentException(
					"Eine Simulations sollte mit einer Simulationsvariante " + _simulationVariant + " angemeldet werden. Simulationsobjekt: "
					+ simulationObject.getPidOrNameOrId()
			);
		}

		// Online oder Offline Simulation
		if(_simulationObject.isOfType("typ.onlineSimulation")) {
			_offlineSimulation = false;
		}
		else {
			_offlineSimulation = true;
		}

		// atg.simulationsStreckenBeschreibung:asp.parameterSoll wird immer mit Simulationsvariante 0 versendet
		_simulationsStreckenBeschreibung = new DataDescription(
				dataModel.getAttributeGroup("atg.simulationsStreckenBeschreibung"), dataModel.getAspect("asp.parameterSoll"), (short)0
		);

		// Das Objekt der Simulationsstrecke anfordern
		_simulationsStreckenObjekt = _simulationObject.getConfigurationData(
				dataModel.getAttributeGroup("atg.simulationsEigenschaften"), dataModel.getAspect("asp.eigenschaften")
		).getReferenceValue("SimulationsStreckenReferenz").getSystemObject();

		if(_simulationsStreckenObjekt == null || !_simulationsStreckenObjekt.isValid()) {
			throw new IllegalStateException("Simulationsstrecke der Simulation " + _simulationObject + " nicht vorhanden oder ungültig: " + _simulationsStreckenObjekt);
		}

		// Datenstrukturen initialisieren, dafür wird die <code>_simulationsStreckenBeschreibung</code> benötigt.
		getSpecialTypes();

		final AttributeGroup atg;
		if(!_offlineSimulation) {
			atg = dataModel.getAttributeGroup("atg.simulationsSteuerungOnline");
		}
		else {
			atg = dataModel.getAttributeGroup("atg.simulationsSteuerungOffline");
			

		}

		_senderSimulationStatus = new SourceSimulationStatus(_connection, _simulationObject, _simulationVariant);

		final Util helper = new Util(_senderSimulationStatus, (ConfigDataModel)_connection.getDataModel(), _simulationVariant, configurationQueryManager, this);
		_newSimulation = new NewSimulation(this, helper);
		_ready = new Ready(this, helper, _connection);
		_notReady = new NotReady(this, helper);
		_deleted = new Deleted();

		// Es wird im Zustand "neue Simulation" gestartet
		setState(_newSimulation);

		final Aspect aspekt = dataModel.getAspect("asp.zustand");

		// atg.simulationsSteuerungOnline:asp.zustand und atg.simulationsSteuerungOffline:asp.zustand werden immer mit Simulationsvariante 0 angemeldet
		_stateDataDescription = new DataDescription(atg, aspekt, (short)0);
		_stateChangeReceiver = new StateChangeReceiver();
		// Vor dieser Anmeldung müssen die States initialisiert sein, da sofort ein "noSource" Datensatz gesendet werden kann -> nullPointerException
//		System.out.println("Melde empfänger für state an: " + _stateDataDescription.getAttributeGroup() + " " + _stateDataDescription.getAspect() + " Simvar " + _simulationVariant + " Objekt: " + _simulationObject.getPid());
		_connection.subscribeReceiver(_stateChangeReceiver, _simulationObject, _stateDataDescription, ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	void setState(SimulationStates state) {
		synchronized(_stateLock) {
//			System.out.println("State-Wechsel nach: " + state);
			// Die If-Abfrage soll verhindern, dass ein Thread(ConfigurationsQueryManager-Thread) simulationRemovedFromSet() abarbeitet
			// (und den Zustand auf delete setzt), der andere Thread(der für die Simulation zuständig ist) aber
			// in diesem synchronized hängt und sobald er freikommt den Zustand falsch setzt.
			// In allen anderen Fällen wird der Zustand immer nur durch einen Thread(Simulationsthread) gesetzt.
			if(_simulationState != _deleted) {
				_simulationState = state;
			}
		}
	}

	SimulationStates getNotReadyState() {
		return _notReady;
	}

	SimulationStates getReadyState() {
		return _ready;
	}

	SimulationStates getDeletedState() {
		return _deleted;
	}

	/**
	 * Fordert einen Datensatz an und ließt alle Typen von Mengen aus, die speziell behandelt werden sollen. Das gleiche gilt für Typen von dynamischen Objekten.
	 * <p>
	 * Die Datenstrukturen, die diese Informationen speichern, werden dadurch auf den neusten Stand gebracht.
	 * <p>
	 * Diese Methode sollte beim Neustart einer Simulation (Konstruktor) und beim Vorstart aufgerufen werden.
	 */
	synchronized void getSpecialTypes() {
		// Den neusten Datensatz von der Parametrierung anfordern
		final Data simulationsStreckenBeschreibungen = _connection.getData(_simulationsStreckenObjekt, _simulationsStreckenBeschreibung, 10000).getData();

		// Die Datenstrukturen neu setzen. Ist kein Datensatz vorhanden, so bleiben die Strukturen leer und die Simulation behandelt keine Menge, kein Objekt
		// gesondert.

		_specialDynamicTypes = new HashSet<DynamicObjectType>();
		_specialObjectSetTypes = new HashSet<ObjectSetType>();

//		System.out.println("Lese neue Datentypen ein: " + simulationsStreckenBeschreibungen);

		if(simulationsStreckenBeschreibungen != null) {

//			System.out.println("Daten ungeleich null");
			// Alle parameterSatzSimulationsdaten
			Data item;
			try {
				// Der Name dieses Attributs hat sich geändert
				item = simulationsStreckenBeschreibungen.getItem("ParameterSatz");
			}
			catch(IllegalArgumentException oldDataModel) {
				// Wenn der neue Name "ParameterSatz" nicht funktioniert, dann mit dem alten Namen "Simulationsdaten" versuchen
				item = simulationsStreckenBeschreibungen.getItem("Simulationsdaten");
			}
			final Data.Array alleParameterSatzSimulationsdaten = item.asArray();

			for(int i = 0; i < alleParameterSatzSimulationsdaten.getLength(); i++) {
				// Endlich am Ziel, hier sind die Mengen und Typen verpackt, die gesondert behandelt werden sollen
				final Data parameterSatzSimulationsdatum = alleParameterSatzSimulationsdaten.getItem(i);

				// Alle dynamischen Mengen, die gesondert zu behandeln sind
				final Data.Array mengenTypen = parameterSatzSimulationsdatum.getItem("Simulationsmengen").asArray();
				for(int i2 = 0; i2 < mengenTypen.getLength(); i2++) {
					// Typ der Menge, die speziell behandelt werden soll
					_specialObjectSetTypes.add((ObjectSetType)mengenTypen.getItem(i2).asReferenceValue().getSystemObject());
				}

				// Alle Typen von dynamischen Objekten, die gesondert behandelt werden sollen
				final Data.Array objektTypen = parameterSatzSimulationsdatum.getItem("DynamischeSimulationsObjekte").asArray();

				for(int i3 = 0; i3 < objektTypen.getLength(); i3++) {
					_specialDynamicTypes.add((DynamicObjectType)objektTypen.getItem(i3).asReferenceValue().getSystemObject());
				}
			}
		}
	}

	/**
	 * Gibt das Systemobject zurück, das aus Sicht des Datenverteilers eine Simulation darstellt.
	 *
	 * @return s.o.
	 */
	public SystemObject getSimulationObject() {
		return _simulationObject;
	}

	/**
	 * Diese Methode wird aufgerufen sobald die Simulation, die zu diesem Objekt gehört, aus der Menge der Simulationen entfernt wird. Dieser Aufruf kann jederzeit
	 * von außen geschehen.
	 * <p>
	 * Alle Mengen, die speziell behandelt werden sollen, werden aufgeräumt.
	 */
	public void simulationRemovedFromSet() {
		synchronized(_stateLock) {
			_simulationState.removedFromSet();
			_senderSimulationStatus.unsubscribe();
			_connection.unsubscribeReceiver(_stateChangeReceiver, _simulationObject, _stateDataDescription);
		}
	}

	/**
	 * Der Aufruf dieser Methode bewirkt, dass alle Mengen der im Konstruktor übergebenen Mengentypen, bearbeitet werden. Dabei werden alle Elemente, die durch die
	 * Simulation angelegt werden, aus den jeweiligen Mengen entfernt.
	 */
	synchronized void cleanUpSets() {

		for(ObjectSetType objectSetType : _specialObjectSetTypes) {
			// Alle Mengen, die zu diesem Typ gehören, anfordern
			final List<ObjectSet> objectSets = objectSetType.getObjectSets();
			for(ObjectSet objectSet : objectSets) {
				if(objectSet instanceof ConfigMutableSet) {
					ConfigMutableSet mutableSet = (ConfigMutableSet)objectSet;
					try {
						mutableSet.deleteElements(_simulationVariant);
					}
					catch(ConfigurationChangeException e) {
						// Dieser Fall kann vorkommen, weil alle Mengen zurückgegeben werden. Die Konfiguration muss nicht für alle Mengen die nötigen
						// Rechte besitzen.
					}
				}
			}
		}
	}

	public short getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * @return true = Es handelt sich um eine Offline-Simulation. Es stehen der Anfangs und der Endzeitpunkt der Simulation zur Verfügung. false = Es handelt sich
	 *         um eine Online-Simulation, Beginn und Endzeitpunkt stehen nicht zur Verfügung.
	 */
	public boolean isOfflineSimulation() {
		return _offlineSimulation;
	}

	/**
	 * Prüft, ob der Typ einer Menge zu den Typen gehört, die an Simulationsstrecke festgelegt wurden. Ist dies der Fall, müssen die Mengen dieses Typs speziell
	 * behandlet werden (lesender und schreibender Zugriff auf die Menge über die Simulationsvariante).
	 *
	 * @param setType Mengentyp, der geprüft werden soll. Vererbung wird nicht berücksichtigt.
	 *
	 * @return true = Der übergebene Typ befindet sich in der Menge der speziell zu behandelnden Mengetypen; false = sonst
	 */
	synchronized public boolean isSpecialTreatedSetType(ObjectSetType setType) {
		return _specialObjectSetTypes.contains(setType);
	}

	/**
	 * Diese Methode prüft ob der übergeben Typ speziell behandelt werden soll. Die ist der Fall, wenn der Typ an einer Simulationsstrecke eingetragen ist.
	 * <p>
	 * Die Auswirkungen sind, dass neue Objekte, die vom übergebnen Typ sein sollen, mit der Simulationsvariante der Simulation angelegt werden müssen, usw..
	 *
	 * @param dynamicObjectType dynamischer Typ, der geprüft werden soll
	 *
	 * @return true = Der übergebene Typ befindet sich in der Liste der speziell zu behandelnden Typen (kb.tmVewSimulationGlobal: DynamischeSimulationsObjekte)
	 */
	synchronized public boolean isSpecialTreatedDynamicObjectType(DynamicObjectType dynamicObjectType) {
		return _specialDynamicTypes.contains(dynamicObjectType);
	}

//	/**
//	 * Diese Methode gibt den Startzeitpunkt der Offline-Simulation zurück.
//	 *
//	 * @return Startzeitpunkt der Simulation (ms seit 1970)
//	 *
//	 * @throws IllegalStateException    Diese Methode darf nur bei Offline-Simulationen aufgerufen werden ({@link #isOfflineSimulation()}.
//	 * @throws IllegalArgumentException Der Zeitpunkt wurde noch nicht gesetzt.
//	 */
//	public long getSimulationStartTime() throws IllegalStateException, IllegalArgumentException {
//		if(isOfflineSimulation()) {
//			if(_simulationBeginOfflineSimulation != Long.MIN_VALUE) {
//				return _simulationBeginOfflineSimulation;
//			}
//			else {
//				// Der Wert wurde noch nicht initialisiert
//				throw new IllegalArgumentException("Der Startzeitpunkt der Simulation wurde noch nicht initialisiert");
//			}
//		}
//		else {
//			throw new IllegalStateException("Es handlet sich um eine Online-Simulation. Es steht kein Zeitpunkt für den Simulationsbeginn zur Verfügung.");
//		}
//	}

	private final class StateChangeReceiver implements ClientReceiverInterface {

		public StateChangeReceiver() {
		}

		public void update(ResultData results[]) {
			for(ResultData resultData : results) {
				try {
					final Data data = resultData.getData();

					final DataState dataState = resultData.getDataState();
					synchronized(_stateLock) {
						if(dataState == DataState.NO_SOURCE) {
							_simulationState.noSource();
						}

						if(data != null) {
							// Die Simulation wechselt den Status
							final SimulationState newState = SimulationState.getInstance(data.getScaledValue("SimulationsZustand").getValueText());

							if(newState == SimulationState.PRESTART) {
								_simulationState.preStart();
							}
							else if(newState == SimulationState.START) {
								_simulationState.start();
							}
							else if(newState == SimulationState.STOP) {
								_simulationState.stop();
							}
							else if(newState == SimulationState.DELETED) {
								_simulationState.delete();
							}
							else if(newState == SimulationState.PAUSE) {
								_simulationState.pause();
							}
						} // synch
					}
				}
				catch(RuntimeException e) {
					_debug.warning("Fehler beim Verarbeiten eines Datensatzes(" + resultData + ") mit einem Zustandswechsel einer Simulation", e);
				}
			}
		}
	}
}

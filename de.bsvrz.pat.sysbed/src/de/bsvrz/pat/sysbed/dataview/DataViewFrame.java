/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.dataview;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

/**
 * Ein Dialog, welcher die gelieferten Online- oder Archivdaten in Tabellenform dargestellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8369 $
 * @see DataViewPanel
 */
public class DataViewFrame implements PrintFrame {

	/** hängt einen neuen Datensatz unten an die Dargestellten an */
	public static final int ADD_BELOW = 0;

	/** fügt einen neuen Datensatz oben vor die Dargestellten ein */
	public static final int ADD_ABOVE = 1;

	/** stellt nur den neuesten Datensatz dar */
	public static final int ONLY_LATEST = 2;

	private final Debug _debug = Debug.getLogger();

	private final UnsubscribingJFrame _frame;

	private final DataViewModel _dataViewModel;

	private final DataViewPanel _dataViewPanel;

	private final ClientDavInterface _connection;

	private final List<SystemObject> _objects;

	private final AttributeGroup _attributeGroup;

	private final Aspect _aspect;

	private final DataDescription _dataDescription;

	private ReceiveOptions _receiveOptions = ReceiveOptions.normal();

	private ReceiverRole _receiverRole = ReceiverRole.receiver();

	/**
	 * Konstruktor, der anhand der Datenidentifikation sich beim Datenverteiler anmeldet und die Daten in Tabellenform darstellt.
	 *
	 * @param connection        Verbindung zum Datenverteiler
	 * @param objects           die zu betrachtenden Systemobjekte
	 * @param attributeGroup    die zu betrachtende Attributgruppe
	 * @param aspect            der zu betrachtende Aspekt
	 * @param simulationVariant die Simulationsvariante
	 */
	public DataViewFrame(
			final ClientDavInterface connection,
			final SystemObject[] objects,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			int simulationVariant) {
		this(connection, Arrays.asList(objects), attributeGroup, aspect, simulationVariant);
	}

	/**
	 * Konstruktor, der anhand der Datenidentifikation sich beim Datenverteiler anmeldet und die Daten in Tabellenform darstellt.
	 *
	 * @param connection        Verbindung zum Datenverteiler
	 * @param objects           die zu betrachtenden Systemobjekte
	 * @param attributeGroup    die zu betrachtende Attributgruppe
	 * @param aspect            der zu betrachtende Aspekt
	 * @param simulationVariant die Simulationsvariante
	 */
	public DataViewFrame(
			final ClientDavInterface connection,
			final List<SystemObject> objects,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			int simulationVariant) {
		_connection = connection;
		_objects = objects;
		_attributeGroup = attributeGroup;
		_aspect = aspect;

		if(simulationVariant != -1) {
			_dataDescription = new DataDescription(_attributeGroup, _aspect, (short)simulationVariant);
		}
		else {
			_dataDescription = new DataDescription(_attributeGroup, _aspect);
		}

		System.getProperties().put("apple.laf.useScreenMenuBar", "true");

		_dataViewModel = new DataViewModel(_attributeGroup);
		_dataViewPanel = new DataViewPanel(_dataViewModel);
		_dataViewModel.addDataViewListener(_dataViewPanel);

		_frame = new UnsubscribingJFrame(_connection, _objects, _dataDescription);
		_frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		CreateMenuBar createMenuBar = new CreateMenuBar(_frame, _dataViewModel, _attributeGroup, _aspect, _dataViewPanel, _connection, _dataDescription, false);
		createMenuBar.createMenuBar(_dataViewPanel.getSelectionManager());

		Container pane = _frame.getContentPane();
		pane.setLayout(new BorderLayout());

		pane.add(_dataViewPanel, BorderLayout.CENTER);
		_frame.setSize(1000, 300);
	}

	/**
	 * Ein Konstruktor, der nur das allernötigste liefert; er ist private, weil er nur in initPrintFrame zur Anwendung kommt. Das resultierende Objekt kennt keine
	 * Dynamik und keine Änderung der Selektion.
	 *
	 * @param connection        die Datenverteiler-Verbindung
	 * @param attributeGroup    die Attributgruppe
	 * @param aspect            der Aspekt
	 * @param simulationVariant die Simualtionsvariante
	 */
	public DataViewFrame(
			final ClientDavInterface connection, final AttributeGroup attributeGroup, final Aspect aspect, int simulationVariant) {
		_connection = connection;
		_objects = new ArrayList<SystemObject>();
		_attributeGroup = attributeGroup;
		_aspect = aspect;
		if(simulationVariant != -1) {
			_dataDescription = new DataDescription(_attributeGroup, _aspect, (short)simulationVariant);
		}
		else {
			_dataDescription = new DataDescription(_attributeGroup, _aspect);
		}

		System.getProperties().put("apple.laf.useScreenMenuBar", "true");

		_dataViewModel = new DataViewModel(_attributeGroup);
		_dataViewPanel = new DataViewPanel(_dataViewModel);
		_dataViewModel.addDataViewListener(_dataViewPanel);
		_dataViewPanel.getSelectionManager().removeSelectionListeners();

		_frame = new UnsubscribingJFrame(_connection, _objects, _dataDescription);
		_frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final Container pane = _frame.getContentPane();
		pane.setLayout(new BorderLayout());
	}

	/**
	 * Gibt das UnsubscribingJFrame-Objekt zurück.
	 *
	 * @return das UnsubscribingJFrame-Objekt
	 */
	public UnsubscribingJFrame getFrame() {
		return _frame;
	}

	/**
	 * Gibt das DataViewPanel zurück.
	 *
	 * @return das DataViewPanel
	 */
	public DataViewPanel getDataViewPanel() {
		return _dataViewPanel;
	}

	/**
	 * Setzt die Empfängeroptionen.
	 *
	 * @param receiveOptions die Empfängeroptionen
	 */
	public void setReceiveOptions(final ReceiveOptions receiveOptions) {
		_receiveOptions = receiveOptions;
	}

	/**
	 * Setzt die Empfängerrolle.
	 *
	 * @param receiverRole die Empfängerrolle
	 */
	public void setReceiverRole(final ReceiverRole receiverRole) {
		_receiverRole = receiverRole;
	}

	/**
	 * Zeigt die in der Liste übergebenen konfigurierenden Daten an.
	 *
	 * @param configuringData die konfigurierenden Daten
	 */
	public void showConfigurationData(final List<DataTableObject> configuringData) {
		_frame.setTitle("Konfigurierende Daten (Attributegruppe: " + _attributeGroup.getNameOrPidOrId() + ", Aspekt: " + _aspect.getNameOrPidOrId() + ")");
		_frame.setVisible(true);
		_dataViewModel.setDatasets(configuringData);
	}

	/**
	 * Zeigt die Onlinedaten der angemeldeten Datenidentifikation an. Der Parameter gibt an, an welcher Stelle neue Daten eingefügt werden sollen. Zur Auswahl
	 * stehen: <ul> <li>0: unten einfügen</li> <li>1: oben einfügen</li> <li>2: nur neueste Daten anzeigen</li> </ul>
	 *
	 * @param displayOptions gibt an, wie neue Daten dargestellt werden sollen
	 */
	public void showOnlineData(int displayOptions) {
		_frame.setTitle("Onlinetabelle (Attributegruppe: " + _attributeGroup.getNameOrPidOrId() + ", Aspekt: " + _aspect.getNameOrPidOrId() + ")");
		_frame.setVisible(true);
		ClientReceiverInterface receiver = new DataViewReceiver(displayOptions);
		try {
			_connection.subscribeReceiver(receiver, _objects, _dataDescription, _receiveOptions, _receiverRole);
			_frame.setReceiver(receiver);
		}
		catch(RuntimeException ex) {
			_frame.setVisible(false);
			_frame.dispose();
			_debug.error(
					"Beim Öffnen einer neuen Onlinetabelle ist bei der Anmeldung der gewünschten Datenidentifikationen ein Fehler aufgetreten (siehe Exception)",
					ex
			);
			throw new IllegalStateException(ex.getMessage());
		}
	}

	/** Die Klasse verarbeitet die Daten, die vom Datenverteiler gesandt werden. */
	private class DataViewReceiver implements ClientReceiverInterface {

		private int _displayOptions = 0;

		/**
		 * Mit dem Konstruktor wird ein Parameter übergeben, der angibt, an welcher Stelle neue Daten eingefügt werden sollen. Zur Auswahl stehen: <ul> <li>0: unten
		 * einfügen</li> <li>1: oben einfügen</li> <li>2: nur neueste Daten anzeigen</li> </ul>
		 *
		 * @param displayOptions gibt an, wie neue Daten dargestellt werden sollen
		 */
		public DataViewReceiver(int displayOptions) {
			_displayOptions = displayOptions;
		}

		public void update(ResultData results[]) {
			// aktueller Speicherverbrauch wird ausgegeben
			//			final LapStatistic statistic = new LapStatistic();
			//			statistic.printLapResultWithGc("UpdateDatasets (" + _counter++ + ")");

			if(_displayOptions == ADD_ABOVE) {
				for(int i = 0; i < results.length; i++) {
					_dataViewModel.addDatasetAbove(new DataTableObject(results[i]));
				}
			}
			else if(_displayOptions == ONLY_LATEST) { 
				List<DataTableObject> dataTableObjects = new LinkedList<DataTableObject>();
				for(int i = 0; i < results.length; i++) {
					dataTableObjects.add(new DataTableObject(results[i]));
				}
				_dataViewModel.updateDatasets(dataTableObjects);
			}
			else {
				List<DataTableObject> dataTableObjects = new LinkedList<DataTableObject>();
				for(int i = 0; i < results.length; i++) {
					dataTableObjects.add(new DataTableObject(results[i]));
				}
				_dataViewModel.addDatasetsBelow(dataTableObjects);
			}
		}
	}
}

/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.dataview;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.main.GenericTestMonitorApplication;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Erstellt ein Fenster mit der {@link DataViewPanel OnlineTabelle}. Übergebene Daten werden angezeigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #addDataset(DataTableObject)
 */
public class ArchiveDataTableView implements PrintFrame {

	private final DataViewModel _dataViewModel;

	private boolean _isDisposed = false;

	private DataViewPanel _dataViewPanel;

	private UnsubscribingJFrame _unsubscribingJFrame;

	private ClientDavInterface _connection;

	private DataDescription _dataDescription;

	private AttributeGroup _attributeGroup;

	private Aspect _aspect;

	private List<SystemObject> _objects;

	public ArchiveDataTableView(final SettingsData settingsData, ClientDavInterface connection, DataDescription dataDescription) {
		_connection = connection;
		_dataDescription = dataDescription;
		_attributeGroup = settingsData.getAttributeGroup();
		_dataViewModel = new DataViewModel(_attributeGroup);
		_dataViewPanel = new DataViewPanel(_dataViewModel);
		_dataViewModel.addDataViewListener(_dataViewPanel);
		_aspect = settingsData.getAspect();
		_objects = settingsData.getObjects();

		System.getProperties().put("apple.laf.useScreenMenuBar", "true");

		_unsubscribingJFrame = new UnsubscribingJFrame(connection, settingsData.getObjects(), dataDescription);
		final JFrame frame = new JFrame(GenericTestMonitorApplication.getTitle("Streambasierte Archivanfrage (Attributgruppe: " + _attributeGroup.getNameOrPidOrId() + ")", _connection));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(_dataViewPanel, BorderLayout.CENTER);

		frame.addWindowListener(
				new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						_isDisposed = true;          // damit ein Thread abfragen kann, ob das Fenster geschlossen wurde und keine weiteren Daten mehr braucht!
					}
				}
		);

		CreateMenuBar createMenuBar = new CreateMenuBar(frame, _dataViewModel, _attributeGroup, _aspect, _dataViewPanel, connection, dataDescription, true);
		createMenuBar.createMenuBar(_dataViewPanel.getSelectionManager());

		frame.setSize(1000, 400);
		frame.setVisible(true);
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
	public ArchiveDataTableView(
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

		_unsubscribingJFrame = new UnsubscribingJFrame(_connection, _objects, _dataDescription);
		_unsubscribingJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final Container pane = _unsubscribingJFrame.getContentPane();
		pane.setLayout(new BorderLayout());
	}


	/**
	 * Fügt das DataTableObject am Ende an.
	 *
	 * @param dataTableObject
	 */
	public void addDataset(final DataTableObject dataTableObject) {
		_dataViewModel.addDatasetBelow(dataTableObject);
	}

	/**
	 * Gibt false zurück, wenn das Fenster geschlossen wurde.
	 *
	 * @return
	 */
	public boolean isDisposed() {
		return _isDisposed;
	}

	public DataViewPanel getDataViewPanel() {
		return _dataViewPanel;
	}

	public UnsubscribingJFrame getFrame() {
		return _unsubscribingJFrame;
	}
}

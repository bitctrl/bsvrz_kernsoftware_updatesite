/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.main;

import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModule;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsListener;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionPanel;
import de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;

/**
 * Diese Klasse erstellt das Fenster der Anwendung mit der {@link de.bsvrz.pat.sysbed.preselection.panel.PreselectionPanel Datenauswahl}, beliebigen {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Modulen} und dem Panel,
 * welches die Einstellungen der Module verwaltet.
 * <p/>
 * Damit die Module auch mit der Applikation kommunizieren können, implementiert diese Klasse das {@link ApplicationInterface}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 7962 $
 */
public class GenericTestMonitorApplication implements ApplicationInterface {

	/** Das Hauptfenster der Anwendung. */
	private final JFrame _frame;

	/** Die Verbindung zum Datenverteiler. */
	private final ClientDavInterface _connection;

	/** das Panel, worauf die Module angeordnet sind */
	private final JPanel _modulePanel;

	/** speichert die Bildschirmgröße */
	private final Dimension _screenSize;

	/** speichert den PreselectionPanel */
	private final PreselectionPanel _preselectionPanel;

	/** speichert die ArgumentListe, die beim Aufruf der Applikation übergeben wurde */
	private List _argumentList;

	/** das Panel, worauf das Logo angeordnet ist */
	private final JPanel _logoPanel;

	/** speichert den Splitpane, welches das Fenster in der Horizontalen teilt */
	private final JSplitPane _splitPane;

	/** speichert das Panel mit den Einstellungen */
	private final SettingsHandler _settingsHandler;

	/** speichert das Panel mit der Datenidentifikationsauswahl und den Modulen */
	private final JPanel _dataSelectionPanel;

	/** Speichert die Objekte der Module. Anhand des Keys (Klassenname des Moduls) kann das Objekt ermittelt werden. */
	private final Map<String, ExternalModule> _moduleMap;
	
	public PreselectionLists getPreselectionLists() {
		return _preselectionPanel.getPreselectionLists();
	}


	/**
	 * Konstruktor. Die Applikation erhält eine {@link de.bsvrz.dav.daf.main.ClientDavInterface Verbindung zum Datenverteiler} und den für den {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree Filterbaum}
	 * benötigten Parameter zur Spezifizierung der Vorauswahl, bestehend aus System- und {@link de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject Knotenobjekten}.
	 *
	 * @param title      der Titel des Fensters
	 * @param connection Verbindung zum Datenverteiler
	 * @param treeNodes  bestehend aus System- und {@link TreeNodeObject Knotenobjekten}
	 */
	public GenericTestMonitorApplication(String title, final ClientDavInterface connection, final Collection<Object> treeNodes) {
		_moduleMap = new HashMap<String, ExternalModule>();

		_connection = connection;
		_screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		_frame = new JFrame();
		String address = connection.getClientDavParameters().getDavCommunicationAddress();
		int port = connection.getClientDavParameters().getDavCommunicationSubAddress();
		_frame.setTitle(title + " - " + address + ":" + port);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						_connection.disconnect(false, "Applikation wurde beendet.");
					}
				}
		);

		Container pane = _frame.getContentPane();
		pane.setLayout(new BorderLayout());

		// hier werden PreselectionPanel, Logo und Modul-Buttons zusammengestellt
		_preselectionPanel = new PreselectionPanel(_connection, treeNodes);
		_preselectionPanel.showSimulationVariant();
		try {
			_preselectionPanel.getPreselectionTree().setSelectedTreePath("alles");
		}
		catch(Exception ignored) {
		}

		JPanel buttonAndLogoPanel = new JPanel(new BorderLayout());
		_logoPanel = new JPanel(new BorderLayout());
		buttonAndLogoPanel.add(_logoPanel, BorderLayout.NORTH);

		_modulePanel = new JPanel();
		_modulePanel.setLayout(new BoxLayout(_modulePanel, BoxLayout.Y_AXIS));
		_modulePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		buttonAndLogoPanel.add(_modulePanel, BorderLayout.CENTER);

		_dataSelectionPanel = new JPanel(new BorderLayout());
		_dataSelectionPanel.add(_preselectionPanel, BorderLayout.CENTER);
		_dataSelectionPanel.add(buttonAndLogoPanel, BorderLayout.EAST);

		// dataSelectionPanel mit settingsPanel in ein SplitPane
		_settingsHandler = new SettingsHandler(this, _preselectionPanel);
		_settingsHandler.setMaximumNumberOfLastUsedSettings(20);
		_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, _dataSelectionPanel, _settingsHandler.getSettingsPanel());
		_splitPane.setOneTouchExpandable(true);
		pane.add(_splitPane, BorderLayout.CENTER);
	}

	/**
	 * Mit dieser Methode wird ein {@link ExternalModule Modul} zur Applikation hinzugefügt.
	 *
	 * @param module ein Modul
	 */
	public void addModule(ExternalModule module) {
		// Eintrag in die Hashmap
		_moduleMap.put(module.getClass().getName(), module);

		ModuleButton button = new ModuleButton(module);
		button.setMaximumSize(new Dimension(_screenSize.width, button.getMaximumSize().height));
		_modulePanel.add(button);
		_modulePanel.add(Box.createVerticalStrut(5));
		_preselectionPanel.addPreselectionListener(button);
		_frame.validate();
	}

	/** Fügt zwischen die Buttons, mit denen die Module gestartet werden können, einen optischen Abstandshalter ein. */
	public void addSeparator() {
		_modulePanel.add(Box.createVerticalStrut(15));
	}

	/**
	 * Fügt über die Buttons, mit denen die Module gestartet werden können, ein Logo ein.
	 *
	 * @param icon das Logo
	 */
	public void addLogo(final Icon icon) {
		JLabel label = new JLabel(icon);
		label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		JPanel borderPanel = new JPanel(new BorderLayout());
		borderPanel.add(label, BorderLayout.CENTER);
		borderPanel.setBorder(BorderFactory.createEtchedBorder());
		_logoPanel.add(borderPanel);
		_logoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/** Stellt die Anwendung dar und lädt die Einstellungen aus den Preferences. */
	public void start() {
		_frame.pack();
		_frame.setLocation((_screenSize.width - _frame.getSize().width) / 2, (_screenSize.height - _frame.getSize().height) / 2);
		_frame.setVisible(true);
		try {
			_settingsHandler.loadAllSettings();
		}
		catch(BackingStoreException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gibt die Verbindung zum Datenverteiler zurück.
	 *
	 * @return die Verbindung zum Datenverteiler
	 */
	public ClientDavInterface getConnection() {
		return _connection;
	}

	/**
	 * Nimmt die Einstellungsdaten der Module entgegen und übergibt sie an das Panel, welches die Einstellungen verwaltet.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void saveSettings(final SettingsData settingsData) {
		_settingsHandler.saveSettings(settingsData);
	}

	/**
	 * Gibt die Argumente zurück, die beim Aufruf der <code>main</code>-Methode übergeben wurden.
	 *
	 * @return die Liste der Argumente
	 */
	public List getArgumentList() {
		return _argumentList;
	}

	/**
	 * Es wird die Argumentliste gesetzt, die beim Starten der Anwendung als Parameter angegeben wurde.
	 *
	 * @param argumentList die Argumentliste
	 */
	public void setArgumentList(List argumentList) {
		_argumentList = argumentList;
	}

	/**
	 * Gibt die Parameter für die Vorauswahl (Baum) zurück. Die Collection enthält Systemobjekte und Knotenobjekte. Anhand der Objekte wird der Baum für die
	 * Vorauswahl erzeugt.
	 *
	 * @return die Sammlung von System- und Knotenobjekten
	 */
	public Collection getTreeNodes() {
		return _preselectionPanel.getPreselectionTree().getTreeNodes();
	}

	/**
	 * Ermittelt anhand des Modulnamens das zugehörige Objekt und gibt es zurück.
	 *
	 * @param moduleName der Name des gesuchten Moduls
	 *
	 * @return das Objekt des gesuchten Moduls, <code>null</code>, wenn kein passendes Modul gespeichert ist
	 */
	public ExternalModule getExternalModule(String moduleName) {
		return _moduleMap.get(moduleName);
	}

	/**
	 * Fügt ein weiteres Modul in die Liste der Applikation ein.
	 *
	 * @param externalModule neues Modul
	 */
	public void setExternalModule(final ExternalModule externalModule) {
		externalModule.setApplication(this);
		String className = externalModule.getClass().getName();
		if(_moduleMap.containsKey(className)) {
			_moduleMap.put(className, externalModule);
		}
	}

	/**
	 * Diese Klasse erstellt für ein {@link ExternalModule Modul} einen Button für die Applikation. Der Button erhält vom Modul einen Text und einen Tooltip.
	 * Weiterhin wird das Modul über Änderungen in der {@link PreselectionLists PreselectionList} informiert.
	 */
	private class ModuleButton extends JButton implements PreselectionListsListener {

		/** speichert das Modul */
		private ExternalModule _module;

		/** speichert die Datenidentifikation (Objekttypen, Attributgruppe, Aspekt und Objekte) in den Einstellungsdaten */
		private SettingsData _settingsData;

		private PreselectionLists _preselectionLists;

		/**
		 * Konstruktor. Erstellt anhand der Informationen aus dem {@link ExternalModule Modul} einen Button mit Tooltip.
		 *
		 * @param module ein Modul
		 */
		public ModuleButton(final ExternalModule module) {
			_module = module;
			_module.setApplication(GenericTestMonitorApplication.this);
			setText(module.getButtonText());
			setToolTipText(module.getTooltipText());
			addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							_settingsData.setTreePath(_preselectionPanel.getPreselectionTree().getSelectedTreePath());
							_settingsData.setSimulationVariant(_preselectionLists.getSimulationVariant());
							_module.startModule(_settingsData);
						}
					}
			);
		}

		/**
		 * Verarbeitet die in der {@link de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists PreselectionList} ausgewählten Elemente.
		 *
		 * @param preselectionLists die Auswahllisten für die Datenidentifikation
		 */
		public void listSelectionChanged(final PreselectionLists preselectionLists) {
			_preselectionLists = preselectionLists;
			List<SystemObjectType> objectTypes = preselectionLists.getSelectedObjectTypes();
			List<AttributeGroup> atgs = preselectionLists.getSelectedAttributeGroups();
			AttributeGroup atg = null;
			if(atgs.size() == 1) {
				atg = atgs.get(0);
			}
			List<Aspect> asps = preselectionLists.getSelectedAspects();
			Aspect asp = null;
			if(asps.size() == 1) {
				asp = asps.get(0);
			}
			List<SystemObject> objects = preselectionLists.getSelectedObjects();
			_settingsData = new SettingsData(objectTypes, atg, asp, objects);
			setEnabled(_module.isPreselectionValid(_settingsData));
			setToolTipText(_module.getTooltipText());
		}
	}
}

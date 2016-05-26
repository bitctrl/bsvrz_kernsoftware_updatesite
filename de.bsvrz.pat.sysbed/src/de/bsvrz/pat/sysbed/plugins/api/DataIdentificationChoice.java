/*
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

package de.bsvrz.pat.sysbed.plugins.api;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.preselection.lists.SystemObjectList;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse dient zur Darstellung der Datenidentifikationsauswahl. Es können eine ausgewählte Attributgruppe, ein ausgewählter Aspekt und beliebig viele
 * Objekte angezeigt werden. Außerdem gibt es einen "Ändern" - Button, welches einen Dialog öffnet, um seine Auswahl zu ändern.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DataIdentificationChoice extends JPanel {

	/* ############## Variablen ########## */
	/** speichert den Label "Attributgruppe" */
	private final JLabel _atgLabel = new JLabel("Attributgruppe");

	/** speichert den Label "Aspekt" */
	private final JLabel _aspLabel = new JLabel("Aspekt");

	/** speichert den Label "Simulationsvariante" */
	private final JLabel _simLabel = new JLabel("Simulationsvariante");

	/** speichert den Label "Objekte" */
	private final JLabel _objLabel = new JLabel("Objekte");

	/** zeigt die ausgewählte Attributgruppe an */
	private final JTextField _atgTextField = new JTextField();

	/** zeigt den ausgewählten Aspekt an */
	private final JTextField _aspTextField = new JTextField();

	/** zeigt die Simulationsvariante an */
	private JTextField _simTextField = new JTextField();

	/** zeigt die ausgewählten Objekte an */
	private final JList _objList = new SystemObjectList();

	/** speichert den "Ändern..." - Button */
	private final JButton _changeButton = new JButton("Ändern...");

	/** speichert die ausgewählten Objekttypen */
	private List<SystemObjectType> _objectTypes = new LinkedList<SystemObjectType>();

	/** speichert die ausgewählte Attributgruppe */
	private final List<AttributeGroup> _attributeGroups = new LinkedList<AttributeGroup>();

	/** speichert den ausgewählten Aspekt */
	private final List<Aspect> _aspects = new LinkedList<Aspect>();

	/** speichert die ausgewählten Objekte */
	private final List<SystemObject> _objects = new LinkedList<SystemObject>();

	/** speichert den Änderndialog */
	private PreselectionDialog _preselectionDialog;

	/** speichert das Layout des Panels */
	private final GridBagLayout _gridBagLayout;

	/** merkt sich, wieviele Aspekte beim PreselectionDialog ausgewählt werden müssen */
	private int _numberOfSelectedAspects = 0;
	
	/** merkt sich, wieviele Attribute beim PreselectionDialog ausgewählt werden müssen */
	private int _numberOfSelectedAttributeGroups = 0;

	/** gibt an, wieviele Objekte beim PreselectionDialog mindestens ausgewählt werden müssen */
	private int _minimumSelectedObjects = 1;

	/** gibt an, wieviele Objekte beim PreselectionDialog maximal ausgewählt sein dürfen */
	private int _maximumSelectedObjects = Integer.MAX_VALUE;

	/** speichert die Knoten im PreselectionTree */
	private Collection _treeNodes = null;

	/** speichert die Verbindung zum Datenverteiler */
	private ClientDavInterface _connection = null;

	/** speichert den zu selektierenden Pfad im PreselectionDialog */
	private String _treePath;

	/** speichert die Simulationsvariante der Datenidentifikation */
	private int _simulationVariant = -1;


	/* ############# Methoden ############### */
	/**
	 * Dem Konstruktor können Filter für den {@link PreselectionDialog Änderndialog} übergeben werden.
	 *
	 * @param listsFilter ein Objekt, welches die Listen des Änderndialogs filtert
	 * @param filterType  Typ der Objekte, die zur Auswahl angeboten werden sollen
	 */
	public DataIdentificationChoice(final PreselectionListsFilter listsFilter, final SystemObjectType filterType) {
		this(listsFilter, new SystemObjectType[]{filterType});
	}

	/**
	 * Dem Konstruktor können Filter für den {@link de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog Änderndialog} übergeben werden.
	 *
	 * @param listsFilter ein Objekt, welches die Listen des Änderndialogs filtert
	 * @param filterTypes Typen der Objekte, die zur Auswahl angeboten werden sollen
	 */
	public DataIdentificationChoice(final PreselectionListsFilter listsFilter, final SystemObjectType[] filterTypes) {
		this(listsFilter, Arrays.asList(filterTypes));
	}

	/**
	 * Dem Konstruktor können Filter für den {@link PreselectionDialog Änderndialog} übergeben werden.
	 *
	 * @param listsFilter ein Objekt, welches die Listen des Änderndialogs filtert
	 * @param filterTypes Typen der Objekte, die zur Auswahl angeboten werden sollen
	 */
	public DataIdentificationChoice(final PreselectionListsFilter listsFilter, final List filterTypes) {
		_gridBagLayout = new GridBagLayout();
		setLayout(_gridBagLayout);
		setBorder(BorderFactory.createTitledBorder("Datenidentifikation"));

		//Tooltip vergeben
//		_simLabel.setToolTipText("Simulationsvariante");   wird nicht mehr benötigt

		// zuordnen der Label zu den Feldern
		_atgLabel.setLabelFor(_atgTextField);
		_aspLabel.setLabelFor(_aspTextField);
		_simLabel.setLabelFor(_simTextField);
		_objLabel.setLabelFor(_objList);

		// Felder sind nicht editierbar
		_atgTextField.setEditable(false);
		_atgTextField.setFocusable(false);
		_aspTextField.setEditable(false);
		_aspTextField.setFocusable(false);
		_simTextField.setEditable(false);
		_simTextField.setFocusable(false);
		_objList.setFocusable(false);

		// Ändern - Button implementieren
		_changeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(_preselectionDialog == null) {
							if(_treeNodes == null || _connection == null) {
								_preselectionDialog = new PreselectionDialog("Datenidentifikationsauswahl", _changeButton, listsFilter, filterTypes);
							}
							else {
								_preselectionDialog = new PreselectionDialog(
										"Datenidentifikationsauswahl", _changeButton, listsFilter, filterTypes, _treeNodes, _connection
								);
								_preselectionDialog.setSelectedPath(_treePath);
							}
							_preselectionDialog.setMaximumSelectedAttributeGroups(1);
							_preselectionDialog.setMinimumSelectedAttributeGroups(1);
							if(_simulationVariant != -1) {
								_preselectionDialog.showSimulationVariant();
								_preselectionDialog.setSimulationVariant(_simulationVariant);
							}
							_preselectionDialog.setMaximumSelectedAspects(_numberOfSelectedAspects);
							_preselectionDialog.setMinimumSelectedAspects(_numberOfSelectedAspects);
							_preselectionDialog.setMinimumSelectedAttributeGroups(_numberOfSelectedAttributeGroups);
							_preselectionDialog.setMaximumSelectedObjects(_maximumSelectedObjects);
							_preselectionDialog.setMinimumSelectedObjects(_minimumSelectedObjects);
						}
						_preselectionDialog.setSelectedObjectTypes(_objectTypes);
						_preselectionDialog.setSelectedAttributeGroups(_attributeGroups);
						_preselectionDialog.setSelectedAspects(_aspects);
						_preselectionDialog.setSelectedObjects(_objects);
						if(_preselectionDialog.show()) {      // OK-Button wurde gedrückt
							// Werte übernehmen
							setObjectTypes(_preselectionDialog.getSelectedObjectTypes());
							setAttributeGroups(_preselectionDialog.getSelectedAttributeGroups());
							setAspects(_preselectionDialog.getSelectedAspects());
							setObjects(_preselectionDialog.getSelectedObjects());
							setSimulationVariant(_preselectionDialog.getSimulationVariant());
							_treePath = _preselectionDialog.getSelectedTreePath();
						}
					}
				}
		);
		createAndShowGui();
	}

	/** stellt das Datenidentifikationsauswahl-Panel zusammen */
	private void createAndShowGui() {
		GridBagConstraints gbc;

		// Zeile für Zeile hinzufügen
		// Attributgruppe
		gbc = makegbc(0, 0, 1, 1);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		_gridBagLayout.setConstraints(_atgLabel, gbc);
		add(_atgLabel);

		gbc = makegbc(1, 0, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		_gridBagLayout.setConstraints(_atgTextField, gbc);
		add(_atgTextField);

		// Aspekte
		gbc = makegbc(0, 1, 1, 1);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		_gridBagLayout.setConstraints(_aspLabel, gbc);
		add(_aspLabel);

		gbc = makegbc(1, 1, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		_gridBagLayout.setConstraints(_aspTextField, gbc);
		add(_aspTextField);

		// Simulationsvariante
		gbc = makegbc(0, 2, 1, 1);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		_gridBagLayout.setConstraints(_simLabel, gbc);
		add(_simLabel);

		gbc = makegbc(1, 2, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		_gridBagLayout.setConstraints(_simTextField, gbc);
		add(_simTextField);

		// Objekte
		gbc = makegbc(0, 3, 1, 1);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		_gridBagLayout.setConstraints(_objLabel, gbc);
		add(_objLabel);

		JScrollPane scrollPane = new JScrollPane(_objList);
		gbc = makegbc(1, 3, 1, 1);
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		_gridBagLayout.setConstraints(scrollPane, gbc);
		add(scrollPane);

		// Ändern - Button
		gbc = makegbc(2, 0, 1, 4);
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		_gridBagLayout.setConstraints(_changeButton, gbc);
		add(_changeButton);
	}

	/**
	 * Übergibt die Objekttypen an die Datenauswahl. Sie werden benötigt, damit eine Vorauswahl beim "Änder"-Button getroffen wird.
	 *
	 * @param objectTypes die vorauszuwählenden Objekttypen
	 */
	private void setObjectTypes(final List<SystemObjectType> objectTypes) {
		_objectTypes.clear();
		if(objectTypes != null) {
			_objectTypes.addAll(objectTypes);
		}
	}

	/**
	 * Fragt die ausgewählten Objekttypen ab.
	 *
	 * @return Liste der Objekttypen
	 */
	public List<SystemObjectType> getObjectTypes() {
		return _objectTypes;
	}

	/**
	 * Gibt die Simulationsvariante zurück.
	 *
	 * @return die Simulationsvariante
	 */
	public int getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * Methode, um die ausgewählten Attributgruppen zu übergeben.
	 *
	 * @param attributeGroups ausgewählte Attributgruppen
	 */
	private void setAttributeGroups(List<AttributeGroup> attributeGroups) {
		_attributeGroups.clear();
		if(attributeGroups != null && attributeGroups.size() >= 1) {
			_attributeGroups.addAll(attributeGroups);
			AttributeGroup atg = (AttributeGroup)_attributeGroups.get(0);
			_atgTextField.setText(atg.getNameOrPidOrId());
			_atgTextField.setCaretPosition(0);
		}
		else {
			remove(_atgLabel);
			remove(_atgTextField);
			_numberOfSelectedAttributeGroups = 0;
		}
	}

	/**
	 * Methode, um die angezeigten Attributgruppen abzufragen.
	 *
	 * @return die angezeigten Attributgruppen
	 */
	public List<AttributeGroup> getAttributeGroups() {
		return _attributeGroups;
	}

	/**
	 * Gibt die ausgewählte Attributgruppe zurück.
	 *
	 * @return die ausgewählte Attributgruppe
	 */
	public AttributeGroup getAttributeGroup() {
		if(!_attributeGroups.isEmpty()) {
	        return (AttributeGroup)_attributeGroups.get(0);
        }
		else {
			return null;
		}
	}

	/**
	 * Methode, um die ausgewählten Aspekte zu übergeben.
	 *
	 * @param aspects die ausgewählten Aspekte
	 */
	private void setAspects(List<Aspect> aspects) {
		_aspects.clear();
		if(aspects != null && aspects.size() == 1) {
			_aspects.addAll(aspects);
			Aspect aspect = (Aspect)_aspects.get(0);
			_aspTextField.setText(aspect.getNameOrPidOrId());
			_aspTextField.setCaretPosition(0);
			_numberOfSelectedAspects = 1;
		}
		else {
			remove(_aspLabel);
			remove(_aspTextField);
			_numberOfSelectedAspects = 0;
		}
	}

	/**
	 * Methode, um die angezeigten Aspekte abzufragen.
	 *
	 * @return die angezeigten Aspekte
	 */
	public List<Aspect> getAspects() {
		return _aspects;
	}

	/**
	 * Gibt den ausgewählten Aspekt zurück.
	 *
	 * @return den ausgewählten Aspekt
	 */
	public Aspect getAspect() {
		if(!_aspects.isEmpty()) {
			return (Aspect)_aspects.get(0);
		}
		else {
			return null;
		}
	}

	/**
	 * Methode, um die ausgewählten Objekte zu übergeben.
	 *
	 * @param objects die ausgewählten Objekte
	 */
	private void setObjects(List<SystemObject> objects) {
		_objects.clear();
		_objects.addAll(objects);
		if(_objects.size() >= 1) {
			DefaultListModel defaultListModel = new DefaultListModel();
			for(Iterator iterator = _objects.iterator(); iterator.hasNext();) {
				defaultListModel.addElement(iterator.next());
			}
			_objList.setModel(defaultListModel);
		}
	}

	/**
	 * Methode, um die angezeigten Objekte zurückzugeben.
	 *
	 * @return die angezeigten Objekte
	 */
	public List<SystemObject> getObjects() {
		return _objects;
	}

	/**
	 * Gibt das oberste Systemobjekt zurück.
	 *
	 * @return das oberste Systemobjekt
	 */
	public SystemObject getObject() {
		return (SystemObject)_objects.get(0);
	}

	/**
	 * Die vollständige Datenidentifikation, bestehend aus Objekttypen, Attributgruppe, Aspekt und Objekte können übergeben werden.
	 *
	 * @param objectTypes       die ausgewählten Objekttypen
	 * @param attributeGroups   die ausgewählten Attributgruppen
	 * @param aspects           die ausgewählten Aspekte
	 * @param objects           die ausgewählten Objekte
	 * @param simulationVariant die Simulationsvariante
	 */
	public void setDataIdentification(
			final List<SystemObjectType> objectTypes,
			final List<AttributeGroup> attributeGroups,
			final List<Aspect> aspects,
			final List<SystemObject> objects,
			int simulationVariant
	) {
		setObjectTypes(objectTypes);
		setAttributeGroups(attributeGroups);
		setAspects(aspects);
		setSimulationVariant(simulationVariant);
		setObjects(objects);
	}

	/**
	 * Die Datenidentifikation kann hiermit übergeben werden.
	 *
	 * @param objectTypes       die Objekttypen
	 * @param attributeGroup    die Attributgruppe
	 * @param aspect            der Aspekt
	 * @param objects           die Objekte
	 * @param simulationVariant die Simulationsvariante
	 */
	public void setDataIdentification(
			final List<SystemObjectType> objectTypes,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			final List<SystemObject> objects,
			int simulationVariant
	) {
		List<AttributeGroup> atgList = null;
		if(attributeGroup != null) {
			atgList = new LinkedList<AttributeGroup>();
			atgList.add(attributeGroup);
		}
		List<Aspect> aspList = null;
		if(aspect != null) {
			aspList = new LinkedList<Aspect>();
			aspList.add(aspect);
		}
		setDataIdentification(objectTypes, atgList, aspList, objects, simulationVariant);
	}

	/**
	 * Setzt die Simulationsvariante. Ist die Simulationsvariante "-1", wird sie nicht angezeigt.
	 *
	 * @param simulationVariant die Simulationsvariante
	 */
	private void setSimulationVariant(int simulationVariant) {
		_simulationVariant = simulationVariant;
		if(_simulationVariant == -1) { // Simulationsvariante nicht anzeigen
			remove(_simLabel);
			remove(_simTextField);
		}
		else {
			_simTextField.setText(String.valueOf(simulationVariant));
		}
	}

	/**
	 * Gibt an, wieviele Objekte mindestens im {@link PreselectionDialog} ausgewählt sein müssen.
	 *
	 * @param min Anzahl der Objekte, die mindestens ausgewählt sein müssen
	 */
	public void setMinimumSelectedObjects(int min) {
		_minimumSelectedObjects = min;
	}

	/**
	 * Gibt an, wieviele Objekte maximal im {@link PreselectionDialog} ausgewählt sein dürfen.
	 *
	 * @param max Anzahl der Objekte, die maximal ausgewählt sein dürfen
	 */
	public void setMaximumSelectedObjects(int max) {
		_maximumSelectedObjects = max;
	}

	/**
	 * Gibt an, wieviele Objekte minimal und maximal im {@link PreselectionDialog} ausgewählt werden müssen / dürfen.
	 *
	 * @param minimum Anzahl der Objekte, die minimal ausgewählt sein müssen
	 * @param maximum Anzahl der Objekte, die maximal ausgewählt sein dürfen
	 */
	public void setNumberOfSelectedObjects(final int minimum, final int maximum) {
		_minimumSelectedObjects = minimum;
		_maximumSelectedObjects = maximum;
	}

	/**
	 * Soll der Baum im {@link de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog} angezeigt werden, dann muss diese Methode
	 * aufgerufen werden. Hierfür werden die Parameter <code>treeNodes</code> und <code>connection</code> auf jeden Fall
	 * benötigt.
	 */
	/**
	 * @param treeNodes  Gibt an, welche Knoten im Baum dargestellt werden sollen. Knoten vom Typ <code>SystemObject</code> oder <code>TreeNodeObject</code>.
	 * @param connection Verbindung zum Datenverteiler
	 * @param treePath   optional - gibt an, welcher Pfad im Baum vorausgewählt sein soll, sonst <code>null</code> falls kein Pfad ausgewählt werden soll
	 */
	public void showTree(final Collection treeNodes, final ClientDavInterface connection, final String treePath) {
		_treeNodes = treeNodes;
		_connection = connection;
		if(treePath.equals("")) {
			_treePath = null;
		}
		else {
			_treePath = treePath;
		}
	}

	/**
	 * Gibt den selektierten Pfad des Baumes zurück, der evtl. durch den {@link PreselectionDialog} geändert wurde.
	 *
	 * @return den selektierten Pfad des Baumes
	 */
	public String getTreePath() {
		return _treePath;
	}

	/**
	 * Hilfsmethode für das GridBagLayout zur Positionierung der Elemente.
	 *
	 * @param x      die x-Position im Grid
	 * @param y      die y-Position im Grid
	 * @param width  gibt die Anzahl der Spalten an, die die Komponente nutzen soll
	 * @param height gibt die Anzahl der Zeilen an, die die Komponente nutzen soll
	 *
	 * @return die Rahmenbedingungen für eine Komponente
	 */
	private GridBagConstraints makegbc(int x, int y, int width, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.insets = new Insets(1, 5, 1, 1);
		return gbc;
	}
}

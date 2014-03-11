/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.preselection.lists;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.main.TooltipAndContextUtil;
import de.bsvrz.pat.sysbed.preselection.tree.PreselectionTreeListener;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

/**
 * Die Klasse <code>PreselectionLists</code> ist ein Teil der Datenidentifikationsauswahl. Sie stellt die konkreten Auswahloptionen anhand von Listen zur
 * Verfügung. Folgende Listen helfen dem Anwender dabei: Objekttyp, Attributgruppe, Aspekt und Objekt. Außerdem kann die Simulationsvariante angegeben werden.
 * <p/>
 * Der Konstruktor <code>PreselectionLists</code> erstellt das Panel und mit der Methode <code>setObjects</code> werden die Listen gefüllt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11925 $
 * @see #PreselectionLists
 * @see #setObjects
 */
public class PreselectionLists extends JPanel implements PreselectionTreeListener {

	/** Der Debugg-Logger */
	private static final Debug _debug = Debug.getLogger();

	/** Der PreselectionListsHandler verwaltet die Daten und aktualisiert die Listen. */
	private final PreselectionListsHandler _preselectionListsHandler;

	/** Diese Liste wird für den Listener benötigt. */
	private List<PreselectionListsListener> _listenerList = new LinkedList<PreselectionListsListener>();

	/** speichert ein Objekt zum Filtern der anzuzeigenden Listen */
	private PreselectionListsFilter _listsFilter = null;

	/** speichert die Liste Objekttyp */
	private SystemObjectList _objtypList;

	/** speichert die Liste Attributgruppe */
	private SystemObjectList _atgList;

	/** speichert die Liste Aspekt */
	private SystemObjectList _aspList;

	/** speichert die Liste Objekt */
	private SystemObjectList _objList;

	/** speichert die linke Seite des Splitpane */
	private final Box _leftBox = Box.createVerticalBox();

	/** Gibt an, ob die Objekt-Typen angezeigt werden sollen. */
	private boolean _showObjectTypes = true;

	/** Gibt an, ob Attributgruppen angezeigt werden sollen. */
	private boolean _showAttributeGroups = true;

	/** Gibt an, ob die Aspekte angezeigt werden sollen. */
	private boolean _showAspects = true;

	/** zeigt die Anzahl der selektierten Elemente der Liste Objekt an */
	private JLabel _numberOfSelectedObjects;

	/** zeigt die Anzahl der selektierten Elemente der Liste Objekttyp an */
	private JLabel _numberOfSelectedObjectTypes;

	/** zeigt die Anzahl der selektierten Elemente der Liste Attributgruppe an */
	private JLabel _numberOfSelectedAtgs;

	/** zeigt die Anzahl der selektierten Elemente der Liste Aspekt an */
	private JLabel _numberOfSelectedAsps;

	/** Schalter zum Deselektieren zuvor selektierter Elemente der Liste Objekttyp */
	private JButton _deselectObjectTypes;

	/** Schalter zum Deselektieren zuvor selektierter Elemente der Liste Attributgruppe */
	private JButton _deselectAtgs;

	/** Schalter zum Deselektieren zuvor selektierter Elemente der Liste Aspekt */
	private JButton _deselectAsps;

	/** Schalter zum Deselektieren zuvor selektierter Elemente der Liste Objekt */
	private JButton _deselectObjects;

	/** Icon für die Schalter zum Deselektieren */
	private final Icon _deselectIcon = new ImageIcon(PreselectionListsHandler.class.getResource("active-close-button.png"));

	/** speichert die zur Vorauswahl bestimmten Elemente der Liste Objekttyp */
	private final Collection<SystemObjectType> _preselectedObjectTypes = new LinkedList<SystemObjectType>();

	/** speichert die zur Vorauswahl bestimmten Elemente der Liste Attributgruppe */
	private final Collection<AttributeGroup> _preselectedAttributeGroups = new LinkedList<AttributeGroup>();

	/** speichert die zur Vorauswahl bestimmten Elemente der Liste Aspekt */
	private final Collection<Aspect> _preselectedAspects = new LinkedList<Aspect>();

	/** speichert die zur Vorauswahl bestimmten Elemente der Liste Objekt */
	private final Collection<SystemObject> _preselectedObjects = new LinkedList<SystemObject>();

	/** speichert die Simulationsvariante */
	private int _simulationsVariant = -1;

	/** speichert den JSpinner zum Anzeigen und Ändern der Simulationsvariante */
	private JSpinner _simulationVariantSpinner;

	/** Speichert die Darstellung der Objekt-Typen. */
	private Box _objectTypeBox;

	/** Speichert die Darstellung der Attributgruppen. */
	private Box _atgBox;

	/** Speichert die Darstellung der Aspekte. */
	private Box _aspBox;

	private JSplitPane _divideLists;

	/* ################### Methoden ###################### */

	/**
	 * Konstruktor, der ein Objekt der Klasse <code>PreselectionLists</code> erstellt.
	 *
	 * @see #createAndShowGui()
	 */
	public PreselectionLists() {
		_preselectionListsHandler = new PreselectionListsHandler(this);
		createAndShowGui();
	}

	/**
	 * @return Liste der Objekte
	 */
	public JList getObjList() {
		return _objList;
	}



	/**
	 * Mit dieser Methode werden zur Initialisierung Objekte (z.B. vom {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree} übergeben. Aus diesen Werten
	 * werden dann die Objekttypen, Attributgruppen und Aspekte rekonstruiert und dargestellt.
	 *
	 * @param systemObjects die darzustellenden Systemobjekte
	 */
	public void setObjects(Collection<SystemObject> systemObjects) {
		notifyListSelectionChanged();
		_preselectionListsHandler.setObjects(systemObjects);
	}

	/**
	 * Diese Methode gibt an, ob die Objekt-Typen angezeigt werden sollen.
	 *
	 * @param flag gibt an, ob die Objekt-Typen angezeigt werden sollen
	 *
	 * @throws IllegalStateException Falls die Objekt-Typen ausgeblendet werden sollen und die Attributgruppen und Aspekte nicht angezeigt werden.
	 */
	public void showObjectTypes(boolean flag) {
		if(!flag && !_showAttributeGroups && !_showAspects) {
			throw new IllegalStateException("Mindestens eine der drei Listen (Objekt-Typen, Attributgruppen oder Aspekte) muss angezeigt bleiben.");
		}
		_showObjectTypes = flag;
		if(flag) {
			// Objekt-Typen anzeigen
			_leftBox.add(_objectTypeBox, 0);
		}
		else {
			_leftBox.remove(_objectTypeBox);
		}
		revalidate(); // Falls das PreselectionLists-Panel bereits angezeigt wird, muss es neu gezeichnet werden.
	}

	/**
	 * Diese Methode gibt an, ob die Attributgruppen angezeigt werden sollen.
	 *
	 * @param flag gibt an, ob die Attributgruppen angezeigt werden sollen
	 *
	 * @throws IllegalStateException Falls die Attributgruppen ausgeblendet werden sollen und die Objekt-Typen und Aspekte nicht angezeigt werden.
	 */
	public void showAttributeGroups(boolean flag) {
		if(!flag && !_showObjectTypes && !_showAspects) {
			throw new IllegalStateException("Mindestens eine der drei Listen (Objekt-Typen, Attributgruppen oder Aspekte) muss angezeigt bleiben.");
		}
		_showAttributeGroups = flag;
		if(flag) {
			int position = 0;
			if(_showObjectTypes) {
				position = 1;
			}
			_leftBox.add(_atgBox, position);
		}
		else {
			_leftBox.remove(_atgBox);
		}
		revalidate(); // Falls das PreselectionLists-Panel bereits angezeigt wird, muss es neu gezeichnet werden.
	}

	/**
	 * Diese Methode gibt an, ob die Aspekte angezeigt werden sollen.
	 *
	 * @param flag gibt an, ob die Aspekte angezeigt werden sollen
	 *
	 * @throws IllegalStateException Falls die Aspekte ausgeblendet werden sollen und die Objekt-Typen und Attributgruppen nicht angezeigt werden.
	 */
	public void showAspects(boolean flag) {
		if(!flag && !_showAttributeGroups && !_showObjectTypes) {
			throw new IllegalStateException("Mindestens eine der drei Listen (Objekt-Typen, Attributgruppen oder Aspekte) muss angezeigt bleiben.");
		}
		_showAspects = flag;
		if(flag) {
			int position = 0;
			if(_showObjectTypes) position++;
			if(_showAttributeGroups) position++;
			_leftBox.add(_aspBox, position);
		}
		else {
			_leftBox.remove(_aspBox);
		}
		revalidate(); // Falls das PreselectionLists-Panel bereits angezeigt wird, muss es neu gezeichnet werden.
	}

	/**
	 * Methode, um die Simulationsvariante anzuzeigen. Default-Wert ist "0". Soll ein anderer Wert voreingestellt sein, dann ist die Methode {@link
	 * #setSimulationVariant} aufzurufen.
	 */
	public void showSimulationVariant() {
		JPanel simPanel = new JPanel();
		simPanel.setLayout(new BoxLayout(simPanel, BoxLayout.X_AXIS));
		if(_simulationsVariant == -1) {
			_simulationsVariant = 0;
		}
		SpinnerModel spinnerModel = new SpinnerNumberModel(_simulationsVariant, 0, 999, 1);
		_simulationVariantSpinner = new JSpinner(spinnerModel);
		_simulationVariantSpinner.addChangeListener(
				new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						Integer simulationsVariant = (Integer)_simulationVariantSpinner.getValue();
						_simulationsVariant = simulationsVariant.intValue();
					}
				}
		);
		JLabel simLabel = new JLabel("Simulationsvariante: ");
		simLabel.setLabelFor(_simulationVariantSpinner);
		simPanel.add(simLabel);
		simPanel.add(_simulationVariantSpinner);
		_leftBox.add(Box.createRigidArea(new Dimension(0, 3)));
		_leftBox.add(simPanel);
	}

	/**
	 * Mit dieser Methode kann die Simulationsvariante gesetzt werden.
	 *
	 * @param value neuer Wert der Simulationsvariante
	 */
	public void setSimulationVariant(int value) {
		_simulationsVariant = value;
		if(_simulationVariantSpinner != null) {
			_simulationVariantSpinner.setValue(new Integer(value));
		}
	}

	/**
	 * Die Methode wird vom Konstruktor aufgerufen und stellt konkrete Auswahloptionen für die Datenidentifikationsauswahl in Form von Auswahllisten für
	 * Objekttypen, Attributgruppen, Aspekte und Objekte zur Verfügung.
	 */
	private void createAndShowGui() {
		setPreferredSize(new Dimension(500, 350));

		final Dimension dimOfIcon = new Dimension(20, 18); // Dimension des Buttons mit dem Icon

		// Speichert die Titelzeile der Objekt-Typ-Liste
		final Box objecttypeHeadlineBox = Box.createHorizontalBox();
		// Speichert die Titelzeile der Attributguppen-Liste
		final Box atgHeadlineBox = Box.createHorizontalBox();
		// Speichert die Titelzeile der Aspekt-Liste
		final Box aspHeadlineBox = Box.createHorizontalBox();

		// Label für die Anzahl der selektierten Objekttypen
		_numberOfSelectedObjectTypes = new JLabel("0 / 0");
		_numberOfSelectedObjectTypes.setToolTipText("Anzahl der selektierten Objekttypen");
		_numberOfSelectedObjectTypes.setBorder(new EtchedBorder());
		// Button zum deselektieren der Objekttypen
		_deselectObjectTypes = new JButton(_deselectIcon);
		_deselectObjectTypes.setToolTipText("alle Objekttypen deselektieren");
		_deselectObjectTypes.setEnabled(false);
		_deselectObjectTypes.setPreferredSize(dimOfIcon);
		_deselectObjectTypes.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_objtypList.clearSelection();
						_preselectedObjectTypes.clear();
					}
				}
		);

		objecttypeHeadlineBox.add(new JLabel("Objekttyp"));
		objecttypeHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		objecttypeHeadlineBox.add(Box.createHorizontalGlue());
		objecttypeHeadlineBox.add(_numberOfSelectedObjectTypes);
		objecttypeHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		objecttypeHeadlineBox.add(_deselectObjectTypes);

		// Liste der Objekttypen
		_objtypList = new SystemObjectList();
		_objtypList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_objtypList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object obj : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType)obj);
							}
							int number = objectTypes.size();
							if(number > 0) {
								_deselectObjectTypes.setEnabled(true);
								_preselectedObjectTypes.clear();
								_preselectedObjectTypes.addAll(objectTypes);
							}
							else {
								_deselectObjectTypes.setEnabled(false);
							}
							String text = number + " / " + _objtypList.getModel().getSize();
							_numberOfSelectedObjectTypes.setText(text);
							final List<AttributeGroup> atgs = new LinkedList<AttributeGroup>();
							for(Object o : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup)o);
							}
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect)o);
							}

							_preselectionListsHandler.objectsDependOnObjectType(objectTypes, atgs, asps);
						}
					}
				}
		);
		_objtypList.addMouseMotionListener(
				new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
					}

					public void mouseMoved(MouseEvent e) {
						int index = _objtypList.locationToIndex(e.getPoint());
						if(index >= 0) {
							Object object = _objtypList.getModel().getElementAt(index);
							if(object != null) {
								try {
									SystemObjectType systemObjecttype = (SystemObjectType)object;
									String tooltip = TooltipAndContextUtil.getTooltip(systemObjecttype);
									_objtypList.setToolTipText(tooltip);
								}
								catch(Exception ex) {
									_debug.fine("Tooltip kann nicht angezeigt werden.");
									_debug.finer(ex.toString());
								}
							}
							else {
								_objtypList.setToolTipText(null);
							}
						}
						else {
							_objtypList.setToolTipText(null);
						}
					}
				}
		);
		_objtypList.addKeyListener(
				new KeyListener() {
					public void keyPressed(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
					}

					public void keyTyped(KeyEvent e) {
						_objtypList.ensureIndexIsVisible(_objtypList.getSelectedIndex());
					}
				}
		);
		final JScrollPane objecttypeScrollPane = new JScrollPane(_objtypList);

		// Attributgruppe
		// Label Anzahl selektierter Attributgruppen
		_numberOfSelectedAtgs = new JLabel("0 / 0");
		_numberOfSelectedAtgs.setBorder(new EtchedBorder());
		_numberOfSelectedAtgs.setToolTipText("Anzahl der selektierten Attributgruppen");
		// Button zum deselektieren von Attributgruppen
		_deselectAtgs = new JButton(_deselectIcon);
		_deselectAtgs.setToolTipText("alle Attributgruppen deselektieren");
		_deselectAtgs.setEnabled(false);
		_deselectAtgs.setPreferredSize(dimOfIcon);
		_deselectAtgs.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_atgList.clearSelection();
						_preselectedAttributeGroups.clear();
					}
				}
		);

		atgHeadlineBox.add(new JLabel("Attributgruppe"));
		atgHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		atgHeadlineBox.add(Box.createHorizontalGlue());
		atgHeadlineBox.add(_numberOfSelectedAtgs);
		atgHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		atgHeadlineBox.add(_deselectAtgs);

		// Liste der Attributgruppen
		_atgList = new SystemObjectList(); // List
		_atgList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_atgList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<AttributeGroup> atgs = new ArrayList<AttributeGroup>();
							for(Object obj : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup)obj);
							}
							int number = atgs.size();
							if(number > 0) {
								_deselectAtgs.setEnabled(true);
								_preselectedAttributeGroups.clear();
								_preselectedAttributeGroups.addAll(atgs);
							}
							else {
								_deselectAtgs.setEnabled(false);
							}
							String text = number + " / " + _atgList.getModel().getSize();
							_numberOfSelectedAtgs.setText(text);
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object o : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType)o);
							}
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect)o);
							}

							_preselectionListsHandler.objectsDependOnAtg(objectTypes, atgs, asps);
						}
					}
				}
		);
		_atgList.addMouseMotionListener(
				new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
					}

					public void mouseMoved(MouseEvent e) {
						int index = _atgList.locationToIndex(e.getPoint());
						if(index >= 0) {
							Object object = _atgList.getModel().getElementAt(index);
							if(object != null) {
								try {
									AttributeGroup attributeGroup = (AttributeGroup)object;
									String tooltip = TooltipAndContextUtil.getTooltip(attributeGroup);
									_atgList.setToolTipText(tooltip);
								}
								catch(Exception ex) {
									_debug.fine("Tooltip kann nicht angezeigt werden.");
									_debug.fine(ex.toString());
								}
							}
							else {
								_atgList.setToolTipText(null);
							}
						}
						else {
							_atgList.setToolTipText(null);
						}
					}
				}
		);
		_atgList.addKeyListener(
				new KeyListener() {
					public void keyPressed(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
					}

					public void keyTyped(KeyEvent e) {
						_atgList.ensureIndexIsVisible(_atgList.getSelectedIndex());
					}
				}
		);
		final JScrollPane atgScrollPane = new JScrollPane(_atgList);

		// Aspekt
		// Label Anzahl selektierter Aspekte
		_numberOfSelectedAsps = new JLabel("0 / 0");
		_numberOfSelectedAsps.setBorder(new EtchedBorder());
		_numberOfSelectedAsps.setToolTipText("Anzahl der selektierten Aspekte");
		// Button deselektieren von Aspekten
		_deselectAsps = new JButton(_deselectIcon);
		_deselectAsps.setToolTipText("alle Aspekte deselektieren");
		_deselectAsps.setEnabled(false);
		_deselectAsps.setPreferredSize(dimOfIcon);
		_deselectAsps.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_aspList.clearSelection();
						_preselectedAspects.clear();
					}
				}
		);

		aspHeadlineBox.add(new JLabel("Aspekt"));
		aspHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		aspHeadlineBox.add(Box.createHorizontalGlue());
		aspHeadlineBox.add(_numberOfSelectedAsps);
		aspHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		aspHeadlineBox.add(_deselectAsps);

		// Liste der Aspekte
		_aspList = new SystemObjectList(); // List
		_aspList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_aspList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect)o);
							}
							int number = asps.size();
							if(number > 0) {
								_deselectAsps.setEnabled(true);
								_preselectedAspects.clear();
								_preselectedAspects.addAll(asps);
							}
							else {
								_deselectAsps.setEnabled(false);
							}
							String text = number + " / " + _aspList.getModel().getSize();
							_numberOfSelectedAsps.setText(text);
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object o : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType)o);
							}
							final List<AttributeGroup> atgs = new LinkedList<AttributeGroup>();
							for(Object o : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup)o);
							}
							_preselectionListsHandler.objectsDependOnAsp(objectTypes, atgs, asps);
						}
					}
				}
		);
		_aspList.addMouseMotionListener(
				new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
					}

					public void mouseMoved(MouseEvent e) {
						int index = _aspList.locationToIndex(e.getPoint());
						if(index >= 0) {
							Object object = _aspList.getModel().getElementAt(index);
							if(object != null) {
								try {
									Aspect aspect = (Aspect)object;
									String tooltip = TooltipAndContextUtil.getTooltip(aspect);
									_aspList.setToolTipText(tooltip);
								}
								catch(Exception ex) {
									_debug.fine("Tooltip kann nicht angezeigt werden.");
									_debug.fine(ex.toString());
								}
							}
							else {
								_aspList.setToolTipText(null);
							}
						}
						else {
							_aspList.setToolTipText(null);
						}
					}
				}
		);

		_aspList.addKeyListener(
				new KeyAdapter() {
					public void keyTyped(KeyEvent e) {
						_aspList.ensureIndexIsVisible(_aspList.getSelectedIndex());
					}
				}
		);
		final JScrollPane aspScrollPane = new JScrollPane(_aspList);

		_objectTypeBox = Box.createVerticalBox();
		_objectTypeBox.add(objecttypeHeadlineBox);
		_objectTypeBox.add(Box.createRigidArea(new Dimension(0, 3)));
		_objectTypeBox.add(objecttypeScrollPane);
		_objectTypeBox.add(Box.createRigidArea(new Dimension(0, 5)));

		_atgBox = Box.createVerticalBox();
		_atgBox.add(atgHeadlineBox);
		_atgBox.add(Box.createRigidArea(new Dimension(0, 3)));
		_atgBox.add(atgScrollPane);
		_atgBox.add(Box.createRigidArea(new Dimension(0, 5)));

		_aspBox = Box.createVerticalBox();
		_aspBox.add(aspHeadlineBox);
		_aspBox.add(Box.createRigidArea(new Dimension(0, 3)));
		_aspBox.add(aspScrollPane);

		_leftBox.add(_objectTypeBox);
		_leftBox.add(_atgBox);
		_leftBox.add(_aspBox);
		_leftBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		_leftBox.setMinimumSize(new Dimension(0, 0));

		// Rechte Seite des SplitPane wird gefüllt
		final Box rightBox = Box.createVerticalBox();
		final Box objectHeadlineBox = Box.createHorizontalBox();

		// Label Anzahl der selektierten Objekte
		_numberOfSelectedObjects = new JLabel("0 / 0");
		_numberOfSelectedObjects.setBorder(new EtchedBorder());
		_numberOfSelectedObjects.setToolTipText("Anzahl der selektierten Objekte");
		// Button deselektieren
		_deselectObjects = new JButton(_deselectIcon);
		_deselectObjects.setToolTipText("alle Objekte deselektieren");
		_deselectObjects.setPreferredSize(dimOfIcon);
		_deselectObjects.setEnabled(false);
		_deselectObjects.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_objList.clearSelection();
						_preselectedObjects.clear();
					}
				}
		);

		objectHeadlineBox.add(new JLabel("Objekte"));
		objectHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		objectHeadlineBox.add(Box.createHorizontalGlue());
		objectHeadlineBox.add(_numberOfSelectedObjects);
		objectHeadlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		objectHeadlineBox.add(_deselectObjects);
		// Liste der Objekte
		_objList = new SystemObjectList();
		_objList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_objList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<SystemObject> systemObjects = new LinkedList<SystemObject>();
							for(Object o : _objList.getSelectedValues()) {
								systemObjects.add((SystemObject)o);
							}
							int number = systemObjects.size();
							if(number > 0) {
								_deselectObjects.setEnabled(true);
								_preselectedObjects.clear();
								_preselectedObjects.addAll(systemObjects);
							}
							else {
								_deselectObjects.setEnabled(false);
							}
							String text = number + " / " + _objList.getModel().getSize();
							_numberOfSelectedObjects.setText(text);
						}
					}
				}
		);
		_objList.addMouseMotionListener(
				new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
					}

					public void mouseMoved(MouseEvent e) {
						int index = _objList.locationToIndex(e.getPoint());
						if(index >= 0) {
							Object object = _objList.getModel().getElementAt(index);
							if(object != null) {
								try {
									SystemObject systemObject = (SystemObject)object;
									String tooltip = TooltipAndContextUtil.getTooltip(systemObject);
									_objList.setToolTipText(tooltip);
								}
								catch(Exception ex) {
									_debug.fine("Tooltip kann nicht angezeigt werden.");
									_debug.fine(ex.toString());
								}
							}
							else {
								_objList.setToolTipText(null);
							}
						}
						else {
							_objList.setToolTipText(null);
						}
					}
				}
		);
		_objList.addKeyListener(
				new KeyListener() {
					public void keyPressed(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
					}

					public void keyTyped(KeyEvent e) {
						_objList.ensureIndexIsVisible(_objList.getSelectedIndex());
					}
				}
		);

		rightBox.add(objectHeadlineBox);
		rightBox.add(Box.createRigidArea(new Dimension(0, 3)));
		rightBox.add(new JScrollPane(_objList));
		rightBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		rightBox.setMinimumSize(new Dimension(0, 0));

		_divideLists = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		_divideLists.setContinuousLayout(true);
		_divideLists.setOneTouchExpandable(true);
		_divideLists.setResizeWeight(0.60);
		_divideLists.setLeftComponent(_leftBox);
		_divideLists.setRightComponent(rightBox);

		// fügt den SplitPane zum JPanel PreselectionLists
		setLayout(new BorderLayout());
		add(_divideLists, BorderLayout.CENTER);
	}

	/**
	 * Mit dieser Methode können Objekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param preselectedObjectTypes Objekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setPreselectedObjectTypes(final List<SystemObjectType> preselectedObjectTypes) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectedObjectTypes.clear();
						if(preselectedObjectTypes == null) {
							_objtypList.clearSelection();
						}
						else {
							_preselectedObjectTypes.addAll(preselectedObjectTypes);
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
							if(_objtypList.getModel().getSize() > 0) {
								_objtypList = selectElements(_objtypList, _preselectedObjectTypes.toArray());
							}
						}
					}
				}
		);
	}

	/**
	 * Mit dieser Methode können Attributgruppen angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param preselectedAttributeGroups Attributgruppen, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setPreselectedAttributeGroups(final List<AttributeGroup> preselectedAttributeGroups) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectedAttributeGroups.clear();
						if(preselectedAttributeGroups == null) {
							_atgList.clearSelection();
						}
						else {
							_preselectedAttributeGroups.addAll(preselectedAttributeGroups);
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
							if(_atgList.getModel().getSize() > 0) {
								_atgList = selectElements(_atgList, _preselectedAttributeGroups.toArray());
							}
						}
					}
				}
		);
	}

	/**
	 * Mit dieser Methode können Aspekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param preselectedAspects Aspekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setPreselectedAspects(final List<Aspect> preselectedAspects) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectedAspects.clear();
						if(preselectedAspects == null) {
							_aspList.clearSelection();
						}
						else {
							_preselectedAspects.addAll(preselectedAspects);
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
							if(_aspList.getModel().getSize() > 0) {
								_aspList = selectElements(_aspList, _preselectedAspects.toArray());
							}
						}
					}
				}
		);
	}

	/**
	 * Mit dieser Methode können Objekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param preselectedObjects Objekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setPreselectedObjects(final List<SystemObject> preselectedObjects) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectedObjects.clear();
						if(preselectedObjects == null) {
							_objList.clearSelection();
						}
						else {
							_preselectedObjects.addAll(preselectedObjects);
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
							if(_objList.getModel().getSize() > 0) {
								_objList = selectElements(_objList, _preselectedObjects.toArray());
							}
						}
					}
				}
		);
	}

	/**
	 * Selektiert die gewünschten Objekte in der übergebenen Liste, falls sie vorhanden sind.
	 *
	 * @param list    die Liste, in der die Objekte selektiert sein sollen
	 * @param objects die zu selektierenden Objekte
	 *
	 * @return die Liste incl. Selektion der Objekte
	 */
	private SystemObjectList selectElements(SystemObjectList list, Object[] objects) {
		list.getSelectionModel().setValueIsAdjusting(true);
		list.clearSelection();
		DefaultListModel defaultListModel = (DefaultListModel)list.getModel();
		for(int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			int position = defaultListModel.indexOf(object);
			if(position >= 0) {
				list.addSelectionInterval(position, position);
			}
		}
		list.getSelectionModel().setValueIsAdjusting(false);
		list.ensureIndexIsVisible(list.getSelectedIndex());
		return list;
	}

	/**
	 * Aktualisiert die Liste mit den Objekten und wendet ggf. einen Filter der Anwendung an.
	 *
	 * @param objectList Die Liste mit den Objekten.
	 */
	void setObjectList(List objectList) {
		if(_listsFilter != null) {
			// Filter von der Anwendung anwenden
			objectList = applyFilter(PreselectionListsFilter.OBJECT_LIST, objectList);
			if(objectList != null) {
				for(Iterator iterator = objectList.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if(object != null) {
						if(!(object instanceof SystemObject)) {
							_debug.error("Element " + object.toString() + " ist kein Systemobjekt! Es wurde entfernt!");
							iterator.remove();
						}
					}
					else {
						_debug.error(
								"Bei der Bearbeitung der darzustellenden Objekte durch ein Filter-Plugin wurde in der Ergebnisliste statt einem SystemObjekt eine Null-Referenz zurückgeliefert."
						);
						iterator.remove();
					}
				}
			}
			else {
				_debug.error(
						"Bei der Bearbeitung der darzustellenden Objekte durch ein Filter-Plugin wurde statt der Ergebnisliste eine Null-Referenz zurückgeliefert."
				);
				objectList = new LinkedList();
			}
		}
		String text = "0 / " + objectList.size();
		_numberOfSelectedObjects.setText(text);
		DefaultListModel defaultListModel = makeListModel(objectList);
		_objList.setModel(defaultListModel);
		_objList = selectElements(_objList, _preselectedObjects.toArray());
	}

	/**
	 * Aktualisiert die Liste mit den Objekttypen und wendet ggf. einen Filter der Anwendung an.
	 *
	 * @param objecttypeList Die Liste mit den Objekttypen.
	 */
	void setObjectTypeList(List objecttypeList) {
		if(_listsFilter != null) {
			// Filter von der Anwendung anwenden
			objecttypeList = applyFilter(PreselectionListsFilter.OBJECTTYPE_LIST, objecttypeList);
			if(objecttypeList != null) {
				for(Iterator iterator = objecttypeList.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if(object != null) {
						if(!(object instanceof SystemObjectType)) {
							_debug.error("Element " + object.toString() + " ist kein Objekttyp! Es wurde entfernt!");
							iterator.remove();
						}
					}
					else {
						_debug.error(
								"Bei der Bearbeitung der darzustellenden Objekttypen durch ein Filter-Plugin wurde in der Ergebnisliste statt einem Objekttypen eine Null-Referenz zurückgeliefert."
						);
						iterator.remove();
					}
				}
			}
			else {
				_debug.error(
						"Bei der Bearbeitung der darzustellenden Objekttypen durch ein Filter-Plugin wurde statt der Ergebnisliste eine Null-Referenz zurückgeliefert."
				);
				objecttypeList = new LinkedList();
			}
		}
		String text = "0 / " + objecttypeList.size();
		_numberOfSelectedObjectTypes.setText(text);
		DefaultListModel defaultListModel = makeListModel(objecttypeList);
		_objtypList.setModel(defaultListModel);
		_objtypList = selectElements(_objtypList, _preselectedObjectTypes.toArray());
	}

	/**
	 * Aktualisiert die Liste mit den Attributgruppen und wendet ggf. einen Filter der Anwendung an.
	 *
	 * @param atgList Die Liste mit den Attributgruppen.
	 */
	void setAtgList(List atgList) {
		if(_listsFilter != null) {
			// Filter von der Anwendung anwenden
			atgList = applyFilter(PreselectionListsFilter.ATTRIBUTEGROUP_LIST, atgList);
			if(atgList != null) {
				for(Iterator iterator = atgList.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if(object != null) {
						if(!(object instanceof AttributeGroup)) {
							_debug.error("Element " + object.toString() + " ist keine Attributgruppe! Es wurde entfernt!");
							iterator.remove();
						}
					}
					else {
						_debug.error(
								"Bei der Bearbeitung der darzustellenden Attributgruppen durch ein Filter-Plugin wurde in der Ergebnisliste statt einer Attributgruppe eine Null-Referenz zurückgeliefert."
						);
						iterator.remove();
					}
				}
			}
			else {
				_debug.error(
						"Bei der Bearbeitung der darzustellenden Attributgruppen durch ein Filter-Plugin wurde statt der Ergebnisliste eine Null-Referenz zurückgeliefert."
				);
				atgList = new LinkedList();
			}
		}
		String text = "0 / " + atgList.size();
		_numberOfSelectedAtgs.setText(text);
		DefaultListModel defaultListModel = makeListModel(atgList);
		_atgList.setModel(defaultListModel);
		_atgList = selectElements(_atgList, _preselectedAttributeGroups.toArray());
	}

	/**
	 * Aktualisiert die Liste mit den Aspekten und wendet ggf. einen Filter der Anwendung an.
	 *
	 * @param aspList Die Liste mit den Aspekten.
	 */
	void setAspList(List aspList) {
		if(_listsFilter != null) {
			// Filter von der Anwendung anwenden
			aspList = applyFilter(PreselectionListsFilter.ASPECT_LIST, aspList);
			if(aspList != null) {
				for(Iterator iterator = aspList.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if(object != null) {
						if(!(object instanceof Aspect)) {
							_debug.error("Element " + object.toString() + " ist kein Aspekt! Es wurde entfernt!");
							iterator.remove();
						}
					}
					else {
						_debug.error(
								"Bei der Bearbeitung der darzustellenden Aspekte durch ein Filter-Plugin wurde in der Ergebnisliste statt einem Aspekt eine Null-Referenz zurückgeliefert."
						);
						iterator.remove();
					}
				}
			}
			else {
				_debug.error(
						"Bei der Bearbeitung der darzustellenden Aspekte durch ein Filter-Plugin wurde statt der Ergebnisliste eine Null-Referenz zurückgeliefert."
				);
				aspList = new LinkedList();
			}
		}
		String text = "0 / " + aspList.size();
		_numberOfSelectedAsps.setText(text);
		DefaultListModel defaultListModel = makeListModel(aspList);
		_aspList.setModel(defaultListModel);
		_aspList = selectElements(_aspList, _preselectedAspects.toArray());
	}

	/**
	 * Erzeugt aus einer Liste von Objekten ein DefaultListModel zum Anzeigen der Objekte in einer JList.
	 *
	 * @param list Liste, die in einer JList angezeigt werden sollen
	 *
	 * @return DefaultListModel, welches in einer JList angezeigt werden kann
	 */
	private DefaultListModel makeListModel(List list) {
		DefaultListModel dlm = new DefaultListModel();
		for(Iterator iterator = list.iterator(); iterator.hasNext();) {
			dlm.addElement(iterator.next());
		}
		return dlm;
	}

	/**
	 * Legt Einfach- oder Mehrfachauswahl für die Liste Objekttyp fest.
	 *
	 * @param selectionMode Als Argument kann eine der folgenden Konstanten übergeben werden:<br/> {@link ListSelectionModel#SINGLE_SELECTION} {@link
	 *                      ListSelectionModel#SINGLE_INTERVAL_SELECTION} {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
	 */
	public void setObjectTypeSelectionMode(int selectionMode) {
		_objtypList.setSelectionMode(selectionMode);
	}

	/**
	 * Legt Einfach- oder Mehrfachauswahl für die Liste Attributgruppe fest.
	 *
	 * @param selectionMode Als Argument kann eine der folgenden Konstanten übergeben werden:<br/> {@link ListSelectionModel#SINGLE_SELECTION} {@link
	 *                      ListSelectionModel#SINGLE_INTERVAL_SELECTION} {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
	 */
	public void setAtgSelectionMode(int selectionMode) {
		_atgList.setSelectionMode(selectionMode);
	}

	/**
	 * Legt Einfach- oder Mehrfachauswahl für die Liste Aspekt fest.
	 *
	 * @param selectionMode Als Argument kann eine der folgenden Konstanten übergeben werden:<br/> {@link ListSelectionModel#SINGLE_SELECTION} {@link
	 *                      ListSelectionModel#SINGLE_INTERVAL_SELECTION} {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
	 */
	public void setAspSelectionMode(int selectionMode) {
		_aspList.setSelectionMode(selectionMode);
	}

	/**
	 * Legt Einfach- oder Mehrfachauswahl für die Liste Objekte fest.
	 *
	 * @param selectionMode Als Argument kann eine der folgenden Konstanten übergeben werden:<br/> {@link ListSelectionModel#SINGLE_SELECTION} {@link
	 *                      ListSelectionModel#SINGLE_INTERVAL_SELECTION} {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
	 */
	public void setObjectSelectionMode(int selectionMode) {
		_objList.setSelectionMode(selectionMode);
	}

	/**
	 * Gibt die selektierten Objekte zurück.
	 *
	 * @return die selektierten Objekte
	 */
	public List<SystemObject> getSelectedObjects() {
		if(!_objList.isSelectionEmpty()) {
			final List<SystemObject> systemObjects = new LinkedList<SystemObject>();
			for(Object object : _objList.getSelectedValues()) {
				if(object instanceof SystemObject) {
					SystemObject systemObject = (SystemObject)object;
					systemObjects.add(systemObject);
				}
				else {
					_debug.error("Ausgewähltes Objekt ist kein System-Objekt", object);
				}
			}
			//			Object[] objects = _objList.getSelectedValues();
			//			return Collections.unmodifiableList(Arrays.asList(objects));
			return Collections.unmodifiableList(systemObjects);
		}
		return new LinkedList<SystemObject>();
	}

	/**
	 * Gibt die selektierten Objekttypen zurück.
	 *
	 * @return die selektierten Objekttypen
	 */
	public List<SystemObjectType> getSelectedObjectTypes() {
		if(!_objtypList.isSelectionEmpty()) {
			final List<SystemObjectType> systemObjectTypes = new LinkedList<SystemObjectType>();
			for(Object object : _objtypList.getSelectedValues()) {
				if(object instanceof SystemObjectType) {
					SystemObjectType objectType = (SystemObjectType)object;
					systemObjectTypes.add(objectType);
				}
				else {
					_debug.error("Ausgewähltes Objekt ist kein Objekt-Type", object);
				}
			}
			//			Object[] objects = _objtypList.getSelectedValues();
			//			return Collections.unmodifiableList(Arrays.asList(objects));
			return Collections.unmodifiableList(systemObjectTypes);
		}
		return new LinkedList<SystemObjectType>();
	}

	/**
	 * Gibt die selektierten Attributgruppen zurück.
	 *
	 * @return die selektierten Attributgruppen
	 */
	public List<AttributeGroup> getSelectedAttributeGroups() {
		if(!_atgList.isSelectionEmpty()) {
			final List<AttributeGroup> atgGroups = new LinkedList<AttributeGroup>();
			for(Object object : _atgList.getSelectedValues()) {
				if(object instanceof AttributeGroup) {
					AttributeGroup attributeGroup = (AttributeGroup)object;
					atgGroups.add(attributeGroup);
				}
				else {
					_debug.error("Ausgewähltes Objekt ist keine Attributgruppe", object);
				}
			}
			//			Object[] objects = _atgList.getSelectedValues();
			//			return Collections.unmodifiableList(Arrays.asList(objects));
			return Collections.unmodifiableList(atgGroups);
		}
		return new LinkedList<AttributeGroup>();
	}

	/**
	 * Gibt die selektierten Aspekte zurück.
	 *
	 * @return die selektierten Aspekte
	 */
	public List<Aspect> getSelectedAspects() {
		if(!_aspList.isSelectionEmpty()) {
			final List<Aspect> aspects = new LinkedList<Aspect>();
			for(Object object : _aspList.getSelectedValues()) {
				if(object instanceof Aspect) {
					Aspect aspect = (Aspect)object;
					aspects.add(aspect);
				}
				else {
					_debug.error("Ausgewähltes Objekt ist kein Aspekt", object);
				}
			}
			//			Object[] objects = _aspList.getSelectedValues();
			//			return Collections.unmodifiableList(Arrays.asList(objects));
			return Collections.unmodifiableList(aspects);
		}
		return new LinkedList<Aspect>();
	}

	/**
	 * Gibt die eingestellte Simulationsvariante zurück.
	 *
	 * @return die eingestellte Simulationsvariante
	 */
	public int getSimulationVariant() {
		return _simulationsVariant;
	}

	/**
	 * Fügt einen <code>PreselectionListsListener</code> hinzu.
	 *
	 * @param listener der hinzuzufügende PreselectionListsListener
	 */
	public void addPreselectionListener(PreselectionListsListener listener) {
		_listenerList.add(listener);
	}

	/**
	 * Entfernt einen <code>PreselectionListsListener</code>.
	 *
	 * @param listener der zu entfernende PreselectionListsListener
	 */
	public void removePreselectionListener(PreselectionListsListener listener) {
		_listenerList.remove(listener);
	}

	/** Gibt dem Listener Bescheid, ob bei der Selektion einer der vier Listen des PreselectionLists-Panels eine Änderung eingetreten ist. */
	private void notifyListSelectionChanged() {
		for(PreselectionListsListener preselectionListsListener : _listenerList) {
			preselectionListsListener.listSelectionChanged(this);
		}
	}

	/**
	 * Setzt einen Filter, welcher vor Anzeige der Listen diese bei Bedarf filtert. Ist der Übergabeparameter <code>null</code>, dann wird das gesetzte
	 * Filter-Objekt gelöscht.
	 *
	 * @param listsFilter der Filter
	 */
	public void setPreselectionListsFilter(PreselectionListsFilter listsFilter) {
		_listsFilter = listsFilter;
		// Filter anwenden, wenn Elemente in der Liste angezeigt werden
		if(_objList.getModel().getSize() > 0) {
			DefaultListModel defaultListModel = (DefaultListModel)_objList.getModel();
			Object[] objects = defaultListModel.toArray();
			setObjectList(Arrays.asList(objects));
		}
		if(_objtypList.getModel().getSize() > 0) {
			DefaultListModel defaultListModel = (DefaultListModel)_objtypList.getModel();
			Object[] objects = defaultListModel.toArray();
			setObjectTypeList(Arrays.asList(objects));
		}
		if(_atgList.getModel().getSize() > 0) {
			DefaultListModel defaultListModel = (DefaultListModel)_atgList.getModel();
			Object[] objects = defaultListModel.toArray();
			setAtgList(Arrays.asList(objects));
		}
		if(_aspList.getModel().getSize() > 0) {
			DefaultListModel defaultListModel = (DefaultListModel)_aspList.getModel();
			Object[] objects = defaultListModel.toArray();
			setAspList(Arrays.asList(objects));
		}
	}

	/**
	 * Ruft die Methode <code>applyFilter</code> des {@link PreselectionListsFilter} Interfaces auf.
	 *
	 * @param whichList Konstante, die angibt, welche der vier Listen übergeben wird
	 * @param list      die zu filternde Liste
	 *
	 * @return die gefilterte Liste
	 */
	private List applyFilter(int whichList, List list) {
		return _listsFilter.applyFilter(whichList, list);
	}

	/**
	 * Mit dieser Methode können die maximal anzuzeigenden Objekt-Typen der entsprechenden Liste eingeschränkt werden.
	 *
	 * @param objectTypes die maximal anzuzeigenden Objekt-Typen
	 */
	public void setObjectTypeFilter(final Collection<SystemObjectType> objectTypes) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectionListsHandler.setObjectTypeFilter(objectTypes);
					}
				}
		);
	}

	/**
	 * Mit dieser Methode können die maximal anzuzeigenden Attributgruppen der entsprechenden Liste eingeschränkt werden.
	 *
	 * @param attributeGroups die maximal anzuzeigenden Attributgruppen
	 */
	public void setAttributeGroupFilter(final Collection<AttributeGroup> attributeGroups) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectionListsHandler.setAttributeGroupFilter(attributeGroups);
					}
				}
		);
	}

	/**
	 * Mit dieser Methode können die maximal anzuzeigenden Aspekte der entsprechenden Liste eingeschränkt werden.
	 *
	 * @param aspects die maximal anzuzeigenden Aspekte
	 */
	public void setAspectFilter(final Collection<Aspect> aspects) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						_preselectionListsHandler.setAspectFilter(aspects);
					}
				}
		);
	}

	/**
	 * Mit dieser Methode, wird nur die Liste Objekt angezeigt.
	 *
	 * @param schowOnlyObjList gibt an, ob nur die Liste Objekt angezeigt werden soll
	 */
	public void setOnlyObjectListVisible(boolean schowOnlyObjList) {
		if(schowOnlyObjList) {
			_divideLists.setDividerLocation(0.0);
		}
		else {
			_divideLists.setDividerLocation(_divideLists.getWidth() / 2);
		}
	}
}

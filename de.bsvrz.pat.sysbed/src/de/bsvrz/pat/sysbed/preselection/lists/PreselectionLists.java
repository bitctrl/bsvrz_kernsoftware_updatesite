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
import de.bsvrz.pat.sysbed.preselection.tree.PreselectionTreeListener;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.*;

/**
 * Die Klasse <code>PreselectionLists</code> ist ein Teil der Datenidentifikationsauswahl. Sie stellt die konkreten Auswahloptionen anhand von Listen zur
 * Verfügung. Folgende Listen helfen dem Anwender dabei: Objekttyp, Attributgruppe, Aspekt und Objekt. Außerdem kann die Simulationsvariante angegeben werden.
 * <p/>
 * Der Konstruktor <code>PreselectionLists</code> erstellt das Panel und mit der Methode <code>setObjects</code> werden die Listen gefüllt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 12610 $
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
	private SystemObjectSelectionList _objtypList;

	/** speichert die Liste Attributgruppe */
	private SystemObjectSelectionList _atgList;

	/** speichert die Liste Aspekt */
	private SystemObjectSelectionList _aspList;

	/** speichert die Liste Objekt */
	private SystemObjectSelectionList _objList;

	/** speichert die linke Seite des Splitpane */
	private final Box _leftBox = Box.createVerticalBox();

	/** Gibt an, ob die Objekt-Typen angezeigt werden sollen. */
	private boolean _showObjectTypes = true;

	/** Gibt an, ob Attributgruppen angezeigt werden sollen. */
	private boolean _showAttributeGroups = true;

	/** Gibt an, ob die Aspekte angezeigt werden sollen. */
	private boolean _showAspects = true;

	/** speichert die Simulationsvariante */
	private int _simulationsVariant = -1;

	/** speichert den JSpinner zum Anzeigen und Ändern der Simulationsvariante */
	private JSpinner _simulationVariantSpinner;

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
	public SystemObjectSelectionList getObjList() {
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
			_leftBox.add(_objtypList, 0);
		}
		else {
			_leftBox.remove(_objtypList);
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
			_leftBox.add(_atgList, position);
		}
		else {
			_leftBox.remove(_atgList);
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
			_leftBox.add(_aspList, position);
		}
		else {
			_leftBox.remove(_aspList);
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
		simPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
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

		_objtypList = new SystemObjectSelectionList("Objekttyp", "Objekttypen");
		_atgList = new SystemObjectSelectionList("Attributgruppe", "Attributgruppen");
		_aspList = new SystemObjectSelectionList("Aspekt", "Aspekte");


		_leftBox.add(_objtypList);
		_leftBox.add(_atgList);
		_leftBox.add(_aspList);
//		_leftBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		_leftBox.setMinimumSize(new Dimension(0, 0));

		// Rechte Seite des SplitPane wird gefüllt
		_objList = new SystemObjectSelectionList("Objekte", "Objekte");
		_objtypList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object obj : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType) obj);
							}
							final List<AttributeGroup> atgs = new LinkedList<AttributeGroup>();
							for(Object o : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup) o);
							}
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect) o);
							}

							_preselectionListsHandler.objectsDependOnObjectType(objectTypes, atgs, asps);
						}
					}
				}
		);
		_atgList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<AttributeGroup> atgs = new ArrayList<AttributeGroup>();
							for(Object obj : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup) obj);
							}
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object o : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType) o);
							}
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect) o);
							}

							_preselectionListsHandler.objectsDependOnAtg(objectTypes, atgs, asps);
						}
					}
				}
		);
		_aspList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
							final List<Aspect> asps = new LinkedList<Aspect>();
							for(Object o : _aspList.getSelectedValues()) {
								asps.add((Aspect) o);
							}
							final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
							for(Object o : _objtypList.getSelectedValues()) {
								objectTypes.add((SystemObjectType) o);
							}
							final List<AttributeGroup> atgs = new LinkedList<AttributeGroup>();
							for(Object o : _atgList.getSelectedValues()) {
								atgs.add((AttributeGroup) o);
							}
							_preselectionListsHandler.objectsDependOnAsp(objectTypes, atgs, asps);
						}
					}
				}
		);
		_objList.addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							notifyListSelectionChanged();
						}
					}
				}
		);

		_divideLists = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		_divideLists.setContinuousLayout(true);
		_divideLists.setOneTouchExpandable(true);
		_divideLists.setResizeWeight(0.60);
		_divideLists.setLeftComponent(_leftBox);
		_divideLists.setRightComponent(_objList);

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
						if(preselectedObjectTypes == null) {
							_objtypList.clearSelection();
						}
						else {
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
								_objtypList.selectElements( preselectedObjectTypes);
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
						if(preselectedAttributeGroups == null) {
							_atgList.clearSelection();
						}
						else {
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
								_atgList.selectElements(preselectedAttributeGroups);
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
						if(preselectedAspects == null) {
							_aspList.clearSelection();
						}
						else {
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
								_aspList.selectElements(preselectedAspects);
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
						if(preselectedObjects == null) {
							_objList.clearSelection();
						}
						else {
							// falls schon Elemente in der Liste sind -> versuchen die Objekte zu selektieren
							_objList.selectElements(preselectedObjects);
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
		_objList.setElements(objectList);
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
		_objtypList.setElements(objecttypeList);
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
		_atgList.setElements(atgList);
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
		_aspList.setElements(aspList);
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
		return _objList.getSelectedValues();
	}

	/**
	 * Gibt die selektierten Objekttypen zurück.
	 *
	 * @return die selektierten Objekttypen
	 */
	public List<SystemObjectType> getSelectedObjectTypes() {
			final List<SystemObjectType> systemObjectTypes = new ArrayList<SystemObjectType>();
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

	/**
	 * Gibt die selektierten Attributgruppen zurück.
	 *
	 * @return die selektierten Attributgruppen
	 */
	public List<AttributeGroup> getSelectedAttributeGroups() {
			final List<AttributeGroup> atgGroups = new ArrayList<AttributeGroup>();
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

	/**
	 * Gibt die selektierten Aspekte zurück.
	 *
	 * @return die selektierten Aspekte
	 */
	public List<Aspect> getSelectedAspects() {
			final List<Aspect> aspects = new ArrayList<Aspect>();
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
		if(_objList.getElements().size() > 0) {
			setObjectList(_objList.getElements());
		}
		if(_objtypList.getElements().size() > 0) {
			setObjectTypeList(_objtypList.getElements());
		}
		if(_atgList.getElements().size() > 0) {
			setAtgList(_atgList.getElements());
		}
		if(_aspList.getElements().size() > 0) {
			setAspList(_aspList.getElements());
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

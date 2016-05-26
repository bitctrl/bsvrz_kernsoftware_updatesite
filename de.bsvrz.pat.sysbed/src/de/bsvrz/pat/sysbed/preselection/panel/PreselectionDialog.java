/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.preselection.panel;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.main.GenericTestMonitorApplication;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsListener;
import de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;


/**
 * Klasse die einen modalen Dialog zur Objektauswahl anzeigt. Im Dialog wird neben einem {@link PreselectionLists Panel zur Objektauswahl} und einem optionalem
 * {@link PreselectionTree Baum mit Filtermöglichkeiten} (wird im Konstruktor festgelegt) auch ein OK-Button und ein Cancel-Button angezeigt. Über entsprechende
 * Methoden kann eingestellt werden, wieviele Objekte, Typen, Attributgruppen und Aspekte mindestens bzw. höchstens ausgewählt werden müssen respektive können.
 * Außerdem kann durch die Vorgabe einer Liste von zu unterstützenden Aspekten die Wahl der Attributgruppen eingeschränkt werden. Der OK-Button wird nur
 * aktiviert, wenn alle Einschränkungen erfüllt sind. Vorzuselektierende Objekte, Typen, Attributgruppen und Aspekte können mit ensprechenden Methoden übergeben
 * werden. Mit der Methode {@link #show()} wird der Dialog im Modalen Zustand angezeigt und danach können mit entsprechenden Abfragemethoden die selektierten
 * Objekte, Typen, Attributgruppen und Aspekte abgefragt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see PreselectionPanel
 * @see PreselectionLists
 * @see de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree
 */
public class PreselectionDialog {

	private final JDialog _dialog;

	private final PreselectionLists _preselectionLists;

	private final PreselectionTree _preselectionTree;

	private int _minimumSelectedObjects = 1;

	private int _maximumSelectedObjects = Integer.MAX_VALUE;

	private int _minimumSelectedObjectTypes = 0;

	private int _maximumSelectedObjectTypes = Integer.MAX_VALUE;

	private int _minimumSelectedAttributeGroups = 0;

	private int _maximumSelectedAttributeGroups = Integer.MAX_VALUE;

	private int _minimumSelectedAspects = 0;

	private int _maximumSelectedAspects = Integer.MAX_VALUE;

	private boolean _okButtonPressed = false;

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Es werden nur Objekte eines vorgegebenen Typs zur Auswahl im Dialog angeboten.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterType      Typ der Objekte, die zur Auswahl angeboten werden sollen.
	 */
	public PreselectionDialog(
			final String title, final Component parentComponent, final PreselectionListsFilter listsFilter, final SystemObjectType filterType
	) {
		this(title, parentComponent, listsFilter, new SystemObjectType[]{filterType}, null, null);
	}

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Die Typen der im Dialog zur Auswahl angebotenen Objekte können vorgegeben werden.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterTypes     Typen der Objekte, die zur Auswahl angeboten werden sollen.
	 */
	public PreselectionDialog(
			final String title, final Component parentComponent, final PreselectionListsFilter listsFilter, final SystemObjectType[] filterTypes
	) {
		this(title, parentComponent, listsFilter, Arrays.asList(filterTypes), null, null);
	}

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Die Typen der im Dialog zur Auswahl angebotenen Objekte können vorgegeben werden.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterTypes     Typen der Objekte, die zur Auswahl angeboten werden sollen.
	 */
	public PreselectionDialog(final String title, final Component parentComponent, final PreselectionListsFilter listsFilter, final List filterTypes) {
		this(title, parentComponent, listsFilter, filterTypes, null, null);
	}

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Es werden nur Objekte eines vorgegebenen Typs zur Auswahl im Dialog angeboten. Es besteht die Möglichkeit
	 * einen {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree Baum mit Filtermöglichkeiten} anzuzeigen.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterType      Typ der Objekte, die zur Auswahl angeboten werden sollen.
	 * @param treeNodes       ein Parameter zur Spezifizierung der Vorauswahl (Baum), bestehend aus Systemobjekten und {@link de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject Knotenobjekten}
	 * @param connection      Verbindung zum Datenverteiler
	 */
	public PreselectionDialog(
			final String title,
			final Component parentComponent,
			final PreselectionListsFilter listsFilter,
			final SystemObjectType filterType,
			final Collection treeNodes,
			final ClientDavInterface connection
	) {
		this(title, parentComponent, listsFilter, new SystemObjectType[]{filterType}, treeNodes, connection);
	}

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Die Typen der im Dialog zur Auswahl angebotenen Objekte können vorgegeben werden. Es besteht die
	 * Möglichkeit einen {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree Baum mit Filtermöglichkeiten} anzuzeigen.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterTypes     Typen der Objekte, die zur Auswahl angeboten werden sollen.
	 * @param treeNodes       ein Parameter zur Spezifizierung der Vorauswahl (Baum), bestehend aus Systemobjekten und {@link de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject Knotenobjekten}
	 * @param connection      Verbindung zum Datenverteiler
	 */
	public PreselectionDialog(
			final String title,
			final Component parentComponent,
			final PreselectionListsFilter listsFilter,
			final SystemObjectType[] filterTypes,
			final Collection treeNodes,
			final ClientDavInterface connection
	) {
		this(title, parentComponent, listsFilter, Arrays.asList(filterTypes), treeNodes, connection);
	}

	/**
	 * Erzeugt einen neuen modalen Objektauswahldialog. Die Typen der im Dialog zur Auswahl angebotenen Objekte können vorgegeben werden. Es besteht die
	 * Möglichkeit einen {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree Baum mit Filtermöglichkeiten} anzuzeigen.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente in einem Fenster oder Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn er unabhängig von
	 *                        anderen Fenstern erzeugt und positioniert werden soll.
	 * @param listsFilter     Objekt zum Filtern der Objektauswahlmenge
	 * @param filterTypes     Typen der Objekte, die zur Auswahl angeboten werden sollen.
	 * @param treeNodes       ein Parameter zur Spezifizierung der Vorauswahl (Baum), bestehend aus Systemobjekten und {@link de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject Knotenobjekten}
	 * @param connection      Verbindung zum Datenverteiler
	 */
	public PreselectionDialog(
			final String title,
			final Component parentComponent,
			final PreselectionListsFilter listsFilter,
			final List filterTypes,
			final Collection treeNodes,
			final ClientDavInterface connection
	) {
		_dialog = createDialog(GenericTestMonitorApplication.getTitle(title, connection), parentComponent);
		final JPanel preselectionPanel = new JPanel(new BorderLayout());
		if(treeNodes == null || connection == null) {	// ohne PreselectionTree anzeigen
			_preselectionLists = new PreselectionLists();
			_preselectionTree = null;
			if(listsFilter != null) {
				_preselectionLists.setPreselectionListsFilter(listsFilter);
			}
			preselectionPanel.add(_preselectionLists, BorderLayout.CENTER);
		}
		else {	// mit PreselectionTree anzeigen
			final PreselectionPanel preselection = new PreselectionPanel(connection, treeNodes);
			_preselectionLists = preselection.getPreselectionLists();
			_preselectionTree = preselection.getPreselectionTree();
			preselectionPanel.add(preselection, BorderLayout.CENTER);
		}
		_dialog.getContentPane().add(preselectionPanel, BorderLayout.CENTER);

		// untere Buttons einfügen
		final Box buttonBox = createButtonBox();
		_dialog.getContentPane().add(buttonBox, BorderLayout.SOUTH);

		List<SystemObject> objects = new LinkedList<SystemObject>();
		for(Iterator iterator = filterTypes.iterator(); iterator.hasNext();) {
			SystemObjectType systemObjectType = (SystemObjectType)iterator.next();
			objects.addAll(systemObjectType.getElements());
		}
		_preselectionLists.setObjects(objects);
	}

	/**
	 * Erzeugt eine Box mit OK-Button und Abbrechen-Button für den Dialog. Hier findet eine Anmeldung auf Änderungen der Auwahl in den Listen statt. Abhängig von
	 * der Auswahl und den eingestellten Einschränkungen wird der OK-Button enabled oder mit einer entsprechenden Begründung im Tooltip des Buttons disabled.
	 *
	 * @return Box mit OK-Button und Abbrechen-Button.
	 */
	private Box createButtonBox() {
		final Box buttonBox = Box.createHorizontalBox();
		final JButton okButton = new JButton("OK");
		final JButton cancelButton = new JButton("Abbrechen");

		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(okButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(cancelButton);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		okButton.setEnabled(false);
		_dialog.getRootPane().setDefaultButton(okButton);

		final ActionListener buttonActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOkButtonPressed(e.getSource() == okButton);
				_dialog.setVisible(false);
			}
		};

		cancelButton.addActionListener(buttonActionListener);
		okButton.addActionListener(buttonActionListener);

		_preselectionLists.addPreselectionListener(
				new PreselectionListsListener() {
					
					public void listSelectionChanged(PreselectionLists preselectionLists) {
						final List selectedObjects = getSelectedObjects();
						final List selectedObjectTypes = getSelectedObjectTypes();
						final List selectedAttributeGroups = getSelectedAttributeGroups();
						final List selectedAspects = getSelectedAspects();
						final boolean enable;
						String disableCause = "Deaktiviert, weil ";
						if(selectedObjects.size() < getMinimumSelectedObjects()) {
							enable = false;
							if(getMinimumSelectedObjects() == 1) {
								disableCause += "mindestens ein Objekt ausgewählt werden muss";
							}
							else {
								disableCause += "mindestens " + getMinimumSelectedObjects() + " Objekte ausgewählt werden müssen";
							}
						}
						else if(selectedObjectTypes.size() < getMinimumSelectedObjectTypes()) {
							enable = false;
							if(getMinimumSelectedObjectTypes() == 1) {
								disableCause += "mindestens ein Typ ausgewählt werden muss";
							}
							else {
								disableCause += "mindestens " + getMinimumSelectedObjectTypes() + " Typen ausgewählt werden müssen  ";
							}
						}
						else if(selectedAttributeGroups.size() < getMinimumSelectedAttributeGroups()) {
							enable = false;
							if(getMinimumSelectedAttributeGroups() == 1) {
								disableCause += "mindestens eine Attributgruppe ausgewählt werden muss";
							}
							else {
								disableCause += "mindestens " + getMinimumSelectedObjects() + " Attributgruppen ausgewählt werden müssen";
							}
						}
						else if(selectedAspects.size() < getMinimumSelectedAspects()) {
							enable = false;
							if(getMinimumSelectedAspects() == 1) {
								disableCause += "mindestens ein Aspekt ausgewählt werden muss";
							}
							else {
								disableCause += "mindestens " + getMinimumSelectedAspects() + " Aspekte ausgewählt werden müssen";
							}
						}
						else if(selectedObjects.size() > getMaximumSelectedObjects()) {
							enable = false;
							if(getMaximumSelectedObjects() == 1) {
								disableCause += "höchstens ein Objekt ausgewählt werden darf";
							}
							else {
								disableCause += "höchstens " + getMaximumSelectedObjects() + " Objekte ausgewählt werden dürfen";
							}
						}
						else if(selectedObjectTypes.size() > getMaximumSelectedObjectTypes()) {
							enable = false;
							if(getMaximumSelectedObjectTypes() == 1) {
								disableCause += "höchstens ein Typ ausgewählt werden muss";
							}
							else {
								disableCause += "höchstens " + getMaximumSelectedObjectTypes() + " Typen ausgewählt werden dürfen";
							}
						}
						else if(selectedAttributeGroups.size() > getMaximumSelectedAttributeGroups()) {
							enable = false;
							if(getMaximumSelectedAttributeGroups() == 1) {
								disableCause += "höchstens eine Attributgruppe ausgewählt werden darf";
							}
							else {
								disableCause += "höchstens " + getMaximumSelectedAttributeGroups() + " Attributgruppen ausgewählt werden dürfen";
							}
						}
						else if(selectedAspects.size() > getMaximumSelectedAspects()) {
							enable = false;
							if(getMaximumSelectedAspects() == 1) {
								disableCause += "höchstens ein Aspekt ausgewählt werden darf";
							}
							else {
								disableCause += "höchstens " + getMaximumSelectedAspects() + " Aspekte ausgewählt werden dürfen";
							}
						}
						else {
							enable = true;
						}
						okButton.setEnabled(enable);
						if(enable) {
							okButton.setToolTipText("Auswahl übernehmen");
						}
						else {
							disableCause += ".";
							okButton.setToolTipText(disableCause);
						}
					}
				}
		);
		return buttonBox;
	}

	/**
	 * Erzeugt das {@link JDialog}-Objekt, das für den Dialog benutzt werden soll. Es erhält einen entsprechenden Titel und wird relativ zur Bezugskomponente aus
	 * einem anderen Fenster dargestellt.
	 *
	 * @param title           Titel des Dialogfensters
	 * @param parentComponent Bezugskomponente im einem Fenster oder einem Dialog auf das sich dieser Dialog beziehen soll oder <code>null</code>, wenn der Dialog
	 *                        unabhängig von anderen Fenstern erzeugt und positioniert werden soll.
	 *
	 * @return Neu erzeugtes JDialog-Objekt, dass relativ zur Bezugskomponente positioniert ist.
	 */
	private static JDialog createDialog(String title, Component parentComponent) {
		final Window window = getWindowForComponent(parentComponent);
		final JDialog dialog;
		if(window instanceof Frame) {
			dialog = new JDialog((Frame)window, title, true);
		}
		else if(window instanceof Dialog) {
			dialog = new JDialog((Dialog)window, title, true);
		}
		else {
			throw new RuntimeException("window: " + window);
		}
		if(parentComponent != null) dialog.setLocationRelativeTo(parentComponent);
		return dialog;
	}

	/**
	 * Bestimmt die minimale Anzahl auszuwählender Objekttypen.
	 *
	 * @return Minimale Anzahl auszuwählender Objekttypen.
	 */
	public int getMinimumSelectedObjectTypes() {
		return _minimumSelectedObjectTypes;
	}

	/**
	 * Setzt die minimale Anzahl auszuwählender Objekttypen. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert 0 benutzt.
	 *
	 * @param minimumSelectedObjectTypes Minimale Anzahl auszuwählender Objekttypen.
	 */
	public void setMinimumSelectedObjectTypes(int minimumSelectedObjectTypes) {
		_minimumSelectedObjectTypes = minimumSelectedObjectTypes;
	}

	/**
	 * Bestimmt die maximale Anzahl auszuwählender Objekttypen.
	 *
	 * @return Maximale Anzahl auszuwählender Objekttypen.
	 */
	public int getMaximumSelectedObjectTypes() {
		return _maximumSelectedObjectTypes;
	}

	/**
	 * Setzt die maximale Anzahl auszuwählender Objekttypen. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert {@link Integer#MAX_VALUE} benutzt.
	 *
	 * @param maximumSelectedObjectTypes Maximale Anzahl auszuwählender Objekttypen.
	 */
	public void setMaximumSelectedObjectTypes(int maximumSelectedObjectTypes) {
		_maximumSelectedObjectTypes = maximumSelectedObjectTypes;
		_preselectionLists.setObjectTypeSelectionMode(
				_maximumSelectedObjectTypes > 1 ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION
		);
	}

	/**
	 * Bestimmt die minimale Anzahl auszuwählender Objekte.
	 *
	 * @return Minimale Anzahl auszuwählender Objekte.
	 */
	public int getMinimumSelectedObjects() {
		return _minimumSelectedObjects;
	}

	/**
	 * Setzt die minimale Anzahl auszuwählender Objekte. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert 1 benutzt.
	 *
	 * @param minimumSelectedObjects Minimale Anzahl auszuwählender Objekte.
	 */
	public void setMinimumSelectedObjects(int minimumSelectedObjects) {
		_minimumSelectedObjects = minimumSelectedObjects;
	}

	/**
	 * Bestimmt die maximale Anzahl auszuwählender Objekte.
	 *
	 * @return Maximale Anzahl auszuwählender Objekte.
	 */
	public int getMaximumSelectedObjects() {
		return _maximumSelectedObjects;
	}

	/**
	 * Setzt die maximale Anzahl auszuwählender Objekte. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert {@link Integer#MAX_VALUE} benutzt.
	 *
	 * @param maximumSelectedObjects Maximale Anzahl auszuwählender Objekte.
	 */
	public void setMaximumSelectedObjects(int maximumSelectedObjects) {
		_maximumSelectedObjects = maximumSelectedObjects;
		_preselectionLists.setObjectSelectionMode(
				_maximumSelectedObjects > 1 ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION
		);
	}

	/**
	 * Bestimmt die minimale Anzahl auszuwählender Attributgruppen.
	 *
	 * @return Minimale Anzahl auszuwählender Attributgruppen.
	 */
	public int getMinimumSelectedAttributeGroups() {
		return _minimumSelectedAttributeGroups;
	}

	/**
	 * Setzt die minimale Anzahl auszuwählender Attributgruppen. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert 0 benutzt.
	 *
	 * @param minimumSelectedAttributeGroups Minimale Anzahl auszuwählender Attributgruppen.
	 */
	public void setMinimumSelectedAttributeGroups(int minimumSelectedAttributeGroups) {
		_minimumSelectedAttributeGroups = minimumSelectedAttributeGroups;
	}

	/**
	 * Bestimmt die maximale Anzahl auszuwählender Attributgruppen.
	 *
	 * @return Maximale Anzahl auszuwählender Attributgruppen.
	 */
	public int getMaximumSelectedAttributeGroups() {
		return _maximumSelectedAttributeGroups;
	}

	/**
	 * Setzt die maximale Anzahl auszuwählender Attributgruppen. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert {@link Integer#MAX_VALUE}
	 * benutzt.
	 *
	 * @param maximumSelectedAttributeGroups Maximale Anzahl auszuwählender Attributgruppen.
	 */
	public void setMaximumSelectedAttributeGroups(int maximumSelectedAttributeGroups) {
		if(_maximumSelectedAttributeGroups == 0 && maximumSelectedAttributeGroups != 0) _preselectionLists.showAttributeGroups(true);
		if(_maximumSelectedAttributeGroups != 0 && maximumSelectedAttributeGroups == 0) _preselectionLists.showAttributeGroups(false);
		_maximumSelectedAttributeGroups = maximumSelectedAttributeGroups;
		_preselectionLists.setAtgSelectionMode(
				_maximumSelectedAttributeGroups > 1 ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION
		);
	}

	/**
	 * Bestimmt die minimale Anzahl auszuwählender Aspekte.
	 *
	 * @return Minimale Anzahl auszuwählender Aspekte.
	 */
	public int getMinimumSelectedAspects() {
		return _minimumSelectedAspects;
	}

	/**
	 * Setzt die minimale Anzahl auszuwählender Aspekte. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert 0 benutzt.
	 *
	 * @param minimumSelectedAspects Minimale Anzahl auszuwählender Aspekte.
	 */
	public void setMinimumSelectedAspects(int minimumSelectedAspects) {
		_minimumSelectedAspects = minimumSelectedAspects;
	}

	/**
	 * Bestimmt die maximale Anzahl auszuwählender Aspekte.
	 *
	 * @return Maximale Anzahl auszuwählender Aspekte.
	 */
	public int getMaximumSelectedAspects() {
		return _maximumSelectedAspects;
	}

	/**
	 * Setzt die maximale Anzahl auszuwählender Aspekte. Wenn die Methode nicht aufgerufen wird, dann wird als Defaultwert {@link Integer#MAX_VALUE} benutzt.
	 *
	 * @param maximumSelectedAspects Maximale Anzahl auszuwählender Aspekte.
	 */
	public void setMaximumSelectedAspects(int maximumSelectedAspects) {
		if(_maximumSelectedAspects == 0 && maximumSelectedAspects != 0) _preselectionLists.showAspects(true);
		if(_maximumSelectedAspects != 0 && maximumSelectedAspects == 0) _preselectionLists.showAspects(false);
		_maximumSelectedAspects = maximumSelectedAspects;
		_preselectionLists.setAspSelectionMode(
				_maximumSelectedAspects > 1 ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION
		);
	}

	/**
	 * Gibt die selektierten Objekte zurück.
	 *
	 * @return die selektierten Objekte
	 */
	public List<SystemObject> getSelectedObjects() {
		return _preselectionLists.getSelectedObjects();
	}

	/**
	 * Gibt die selektierten Objekt-Typen zurück.
	 *
	 * @return die selektierten Objekt-Typen
	 */
	public List<SystemObjectType> getSelectedObjectTypes() {
		return _preselectionLists.getSelectedObjectTypes();
	}

	/**
	 * Gibt die selektierten Attributgruppen zurück.
	 *
	 * @return die selektierten Attributgruppen
	 */
	public List<AttributeGroup> getSelectedAttributeGroups() {
		return _preselectionLists.getSelectedAttributeGroups();
	}

	/**
	 * Gibt die selektierten Aspekte zurück.
	 *
	 * @return die selektierten Aspekte
	 */
	public List<Aspect> getSelectedAspects() {
		return _preselectionLists.getSelectedAspects();
	}

	/**
	 * Gibt den selektierten Pfad des Baums als kommaseparierten String zurück. Jedes Objekt wird durch eine PID repräsentiert.
	 *
	 * @return Pfad des Baums als kommaseparierten String
	 */
	public String getSelectedTreePath() {
		return _preselectionTree.getSelectedTreePath();
	}

	/**
	 * Mit dieser Methode können Objekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param objects Objekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedObjects(List<SystemObject> objects) {
		_preselectionLists.setPreselectedObjects(objects);
	}

	/**
	 * Mit dieser Methode können Objekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param types Objekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedObjectTypes(List<SystemObjectType> types) {
		_preselectionLists.setPreselectedObjectTypes(types);
	}

	/**
	 * Mit dieser Methode können Attributgruppen angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param attributeGroups Attributgruppen, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedAttributeGroups(List<AttributeGroup> attributeGroups) {
		_preselectionLists.setPreselectedAttributeGroups(attributeGroups);
	}

	/**
	 * Mit dieser Methode können Aspekte angegeben werden, die beim Füllen der Listen vorselektiert sein sollen.
	 *
	 * @param aspects Aspekte, die vorselektiert sein sollen. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedAspects(List<Aspect> aspects) {
		_preselectionLists.setPreselectedAspects(aspects);
	}

	/**
	 * Mit dieser Methode kann ein Objekt angegeben werden, welches beim Füllen der Listen vorselektiert sein soll.
	 *
	 * @param object Objekt, welches vorselektiert sein soll. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedObject(SystemObject object) {
		setSelectedObjects(Arrays.asList(object));
	}

	/**
	 * Mit dieser Methode kann ein Objekt-Typ angegeben werden, welcher beim Füllen der Listen vorselektiert sein soll.
	 *
	 * @param type Objekt-Typ, welcher vorselektiert sein soll. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedObjectType(SystemObjectType type) {
		setSelectedObjectTypes(Arrays.asList(type));
	}

	/**
	 * Mit dieser Methode kann eine Attributgruppe angegeben werden, welche beim Füllen der Listen vorselektiert sein soll.
	 *
	 * @param attributeGroup Attributgruppe, welche vorselektiert sein soll. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedAttributeGroup(AttributeGroup attributeGroup) {
		setSelectedAttributeGroups(Arrays.asList(attributeGroup));
	}

	/**
	 * Mit dieser Methode kann ein Aspekt angegeben werden, welcher beim Füllen der Listen vorselektiert sein soll.
	 *
	 * @param aspect Aspekt, welcher vorselektiert sein soll. Wird <code>null</code> übergeben, wird die Selektion gelöscht.
	 */
	public void setSelectedAspect(Aspect aspect) {
		setSelectedAspects(Arrays.asList(aspect));
	}

	/**
	 * Kommaseparierte PIDs werden als String übergeben, die einen Pfad im Baum des PreselectionTrees darstellen. Ist der Pfad vorhanden, dann wird er selektiert.
	 *
	 * @param treePath Pfad des Baums als kommaseparierten String
	 */
	public void setSelectedPath(final String treePath) {
		_preselectionTree.setSelectedTreePath(treePath);
	}

	/** Mit dieser Methode kann bestimmt werden, ob die Simulationsvariante angezeigt werden soll. */
	public void showSimulationVariant() {
		_preselectionLists.showSimulationVariant();
	}

	/**
	 * Gibt die Simulationsvariante zurück.
	 *
	 * @return die Simulationsvariante
	 */
	public int getSimulationVariant() {
		return _preselectionLists.getSimulationVariant();
	}

	/**
	 * Setzt den Wert der Simulationsvariante.
	 *
	 * @param value neuer Wert der Simulationsvariante
	 */
	public void setSimulationVariant(int value) {
		_preselectionLists.setSimulationVariant(value);
	}

	public boolean show() {
		_dialog.pack();
		_dialog.setVisible(true);
		_dialog.dispose();
		return isOkButtonPressed();
	}

	public boolean isOkButtonPressed() {
		return _okButtonPressed;
	}

	private void setOkButtonPressed(boolean ok) {
		_okButtonPressed = ok;
	}

	private static Window getWindowForComponent(Component component) {
		if(component == null) return JOptionPane.getRootFrame();
		if(component instanceof Dialog || component instanceof Frame) return (Window)component;
		return getWindowForComponent(component.getParent());
	}
}

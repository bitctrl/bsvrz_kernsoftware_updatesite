/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.preselection.tree;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.pat.sysbed.preselection.treeFilter.standard.Filter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Die Klasse <code>PreselectionTree</code> ist ein Teil der Datenidentifikationsauswahl. Sie stellt die spezifizierte Vorauswahl in Form eines Baumes zur
 * Verfügung.
 * <p/>
 * Durch die spezifizierte Vorauswahl wird die Anzahl der durch den Benutzer auswählbaren Datenidentifikationen durch verschiedene Filter eingeschränkt.<p/> Die
 * Objekte werden nach der Filterung wieder zur Verfügung gestellt und können beispielsweise mit Hilfe der Klasse {@link de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists} dargestellt und
 * weiter eingeschränkt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6199 $
 * @see #PreselectionTree
 * @see #addPreselectionListener
 */
public class PreselectionTree extends JPanel {

	/** speichert ein Objekt der Klasse <code>PreselectionTreeHandler</code> */
	private PreselectionTreeHandler _preselectionTreeHandler;

	/** speichert einen JTree */
	private JTree _tree;

	/** speichert angemeldete Listener-Objekte */
	private List<PreselectionTreeListener> _listenerList = new LinkedList<PreselectionTreeListener>();

	/** Speichert alle Systemobjekte. Die Collection wird automatisch beim Erzeugen und Löschen von dynamischen Objekten aktualisiert. */
	private Collection<SystemObject> _systemObjects;

	/** Speichert den Stand der zuletzt für die Filterung verwendeten Collection der Systemobjekte. Referenz auf das Objekt, das bei der letzten Filterung in
	 * der Variablen {@link #_systemObjects} enthalten war.
	 */
	private Collection<SystemObject> _lastUsedSystemObjects;

	/** speichert die gefilterten Systemobjekte */
	private Collection<SystemObject> _filterObjects;

	/** speichert kommaseparierte PIDs, die den Pfad im Baum angeben */
	private String _treePath;

	private TreePath _selectedTreePath;

	private final DataModel _dataModel;

	private JButton _updateButton;

	/**
	 * Der Konstruktor erstellt ein Objekt der Klasse <code>PreselectionTree</code>.
	 *
	 * @param connection Verbindung zum Datenverteiler
	 * @param treeNodes  ein Parameter zur Spezifizierung der Vorauswahl (Baum), bestehend aus Systemobjekten und {@link TreeNodeObject Knotenobjekten}
	 *
	 * @see #createAndShowGui()
	 */
	public PreselectionTree(ClientDavInterface connection, Collection<Object> treeNodes) {
		_dataModel = connection.getDataModel();
		_preselectionTreeHandler = new PreselectionTreeHandler(this, connection);
		createAndShowGui();
		// erst die Oberfläche erstellen, dann die Anmeldung beim DaV und das Darstellen der Knoten
		registerDynamicObjectType();
		_preselectionTreeHandler.setTreeNodes(treeNodes);
	}

	/**
	 * Die Methode wird vom Konstruktor aufgerufen und stellt einen JTree für die spezifizierte Vorauswahl zur Verfügung. Bei Auswahl eines Knotens im Baum werden
	 * alle Filter auf dem Pfad von der Wurzel bis zum Knoten auf die Systemobjekte angewendet.
	 */
	private void createAndShowGui() {
		_updateButton = new JButton("Aktualisieren");
		_updateButton.setEnabled(false);
		_updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_updateButton.setEnabled(false);
				filterObjects(_selectedTreePath);
				notifyTreeSelectionChanged();
				updateUpdateButton();
			}
		});
		_tree = new JTree();
		_tree.setModel(new DefaultTreeModel(null));
		_tree.setEditable(false);
		_tree.setRootVisible(false);
		_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//		_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		_tree.setShowsRootHandles(true);
		_tree.addTreeSelectionListener(
				new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent e) {
						/*
						{
							_filterObjects = _systemObjects;
							// einen Knoten ausgewählt:
							//  -> Filter von jedem Knoten auf dem selektierten Pfad auf die Objekte anwenden
							//     (brauche ich dazu erst alle Objekte? Oder gibt mir
							//     der Filter vor, welche Objekte ich laden soll?
							//  -> _systemObjects auf neuen Wert setzen
							//  -> notifyTreeSelectionChanged aufrufen

							// 1. Objekte vom Pfad mit Filter holen
							TreePath tp = e.getNewLeadSelectionPath();
//				long start = System.currentTimeMillis();
							if(tp != null) {
								Object[] objects = tp.getPath();
								//_debug.finest(" Selektiert: " + tp.toString());
								for(int i = 0; i < objects.length; i++) {
									TreeNodeObject treeNodeObject = (TreeNodeObject)objects[i];
									//System.out.println("Name: " + treeNodeObject.getName() + "   Pid: " + treeNodeObject.getPid());
									//System.out.println("Knoten [" + i + "]: " + treeNodeObject.getName());
									// nehme Objektmenge und wende Filter darauf an
									Collection filters = treeNodeObject.getFilters();
									if(!filters.isEmpty()) {	   // Filter anwenden
										for(Iterator iterator = filters.iterator(); iterator.hasNext();) {
											Filter filter = (Filter)iterator.next();
											// hier wird der Filter angewandt
											_filterObjects = filter.filterObjects(_filterObjects);
										}
									}
								}
//					_debug.finest("neue Objekte bestimmen: " + (System.currentTimeMillis() - start));
								// Filter anwenden
								notifyTreeSelectionChanged();
							}
						}
						*/
						filterObjects(e.getNewLeadSelectionPath());
						notifyTreeSelectionChanged();
					}
				}
		);
		JScrollPane treeScrollPane = new JScrollPane(_tree);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(0, 0));
		setPreferredSize(new Dimension(200, 200));
		add(treeScrollPane, BorderLayout.CENTER);
		add(_updateButton, BorderLayout.SOUTH);
	}

	private void registerDynamicObjectType() {
		final String pid = Pid.Type.DYNAMIC_OBJECT;
		final DynamicObjectType dynamicObjectType = (DynamicObjectType)_dataModel.getType(pid);
		final DynamicObjectTypeListener listener = new DynamicObjectTypeListener(this);
		dynamicObjectType.addInvalidationListener(listener);
		dynamicObjectType.addObjectCreationListener(listener);
	}

	private final class DynamicObjectTypeListener implements InvalidationListener, DynamicObjectType.DynamicObjectCreatedListener {

		private PreselectionTree _preselectionTree;

		public DynamicObjectTypeListener(PreselectionTree preselectionTree) {
			_preselectionTree = preselectionTree;
		}

		public void invalidObject(DynamicObject dynamicObject) {
			// Objekt entfernen
			if(_systemObjects != null) {
				Set<SystemObject> allSystemObjects = new HashSet<SystemObject>(_systemObjects);
				allSystemObjects.remove(dynamicObject);
				_systemObjects = allSystemObjects;
			}
			updateUpdateButton();
			//callUpdate();
		}

		public void objectCreated(DynamicObject createdObject) {
			// Objekt hinzufügen
			if(_systemObjects != null) {
				final List<SystemObject> newList = new LinkedList<SystemObject>(_systemObjects);
				newList.add(createdObject);
				_systemObjects = newList;
			}
			updateUpdateButton();
			//callUpdate();
		}

//		private void callUpdate() {
//			_preselectionTree.filterObjects(_preselectionTree._selectedTreePath);
//			_preselectionTree.notifyTreeSelectionChanged();
//		}
	}

	/**
	 * Filtert die Objekte nach der Auswahl im Auswahlbaum.
	 *
	 * @param tp selektierter Pfad im Auswahlbaum
	 */
	private void filterObjects(final TreePath tp) {
		_lastUsedSystemObjects = _systemObjects;
		_selectedTreePath = tp;
		// einen Knoten ausgewählt:
		//  -> Filter von jedem Knoten auf dem selektierten Pfad auf die Objekte anwenden
		//     (brauche ich dazu erst alle Objekte? Oder gibt mir
		//     der Filter vor, welche Objekte ich laden soll?
		//  -> _systemObjects auf neuen Wert setzen
		//  -> notifyTreeSelectionChanged aufrufen

		// 1. Objekte vom Pfad mit Filter holen
		if(tp != null) {
			_filterObjects = _lastUsedSystemObjects;
			Object[] objects = tp.getPath();
			for(int i = 0; i < objects.length; i++) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)objects[i];
				// nehme Objektmenge und wende Filter darauf an
				final Collection filters = treeNodeObject.getFilters();
				if(!filters.isEmpty()) {	   // Filter anwenden
					for(Iterator iterator = filters.iterator(); iterator.hasNext();) {
						Filter filter = (Filter)iterator.next();
						// hier wird der Filter angewandt
						_filterObjects = filter.filterObjects(_filterObjects);
					}
				}
			}
		}
		else {
			_filterObjects = new LinkedList();
		}
		updateUpdateButton();
	}

	private void updateUpdateButton() {
		final boolean objectsUpToDate = (_selectedTreePath == null) || (_systemObjects == _lastUsedSystemObjects);
		_updateButton.setEnabled(!objectsUpToDate);
	}

	/**
	 * Ändert den aktuellen Baum in der JTree-Komponente.
	 *
	 * @param newModel das TreeModel, welches angezeigt werden soll
	 */
	void setTreeData(TreeModel newModel) {
//		_debug.finest("setTreeData");
//		long start = System.currentTimeMillis();
		_tree.setModel(newModel);
//		_debug.finest("ModelTime: " + (System.currentTimeMillis() - start));
		_preselectionTreeHandler.initDataLists();
//		_debug.finest("initDataLists: " + (System.currentTimeMillis() - start));
		_systemObjects = _preselectionTreeHandler.getAllObjects();
		_filterObjects = new LinkedList<SystemObject>();
		try {
			selectTreePath();
		}
		catch(Exception ignore) {
		}
//		_debug.finest("selectTreePath(): " + (System.currentTimeMillis() - start));
		notifyTreeSelectionChanged();
		updateUpdateButton();
//		_debug.finest("notify: " + (System.currentTimeMillis() - start));
	}

	/** Selektiert anhand des Strings _treePath (enthält kommaseparierte PIDs) den Pfad im Baum. */
	private void selectTreePath() {
		if(_treePath != null) {
			String[] paths = _treePath.split(",");
			List<TreeNodeObject> treeNodeObjects = new ArrayList<TreeNodeObject>();
			TreeNodeObject root = (TreeNodeObject)_tree.getModel().getRoot();
			treeNodeObjects.add(root);
			TreeNodeObject node = root;
			for(int j = 0, m = paths.length; j < m; j++) {
				for(int i = 0, n = node.getChildCount(); i < n; i++) {
					TreeNodeObject nodeObject = node.getChild(i);
					if(nodeObject.getPid().equals(paths[j])) {
						treeNodeObjects.add(nodeObject);
						node = nodeObject;
						break;
					}
				}
			}
			_tree.setSelectionPath(new TreePath(treeNodeObjects.toArray()));
			_treePath = null;
		}
	}

	/**
	 * Gibt die Parameter für die Vorauswahl (Baum) zurück. Die Collection enthält Systemobjekte und {@link TreeNodeObject Knotenobjekte}. Anhand der Objekte wird
	 * der Baum für die Vorauswahl erzeugt.
	 *
	 * @return die Sammlung von System- und Knotenobjekten
	 */
	public Collection<Object> getTreeNodes() {
		return Collections.unmodifiableCollection(_preselectionTreeHandler.getTreeNodes());
	}

	/**
	 * Gibt den selektierten Pfad des Baums als kommaseparierten String zurück. Jedes Objekt wird durch eine PID repräsentiert.
	 *
	 * @return Pfad des Baums als kommaseparierten String
	 */
	public String getSelectedTreePath() {
		TreePath treePath = _tree.getSelectionPath();
		String path = "";
		if(treePath != null) {
			Object[] objects = treePath.getPath();
			for(int i = 0; i < objects.length; i++) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)objects[i];
				String pid = treeNodeObject.getPid();
				if("Wurzel".equals(treeNodeObject.getName())) continue;
				if(path.length() == 0 && i == 1) {
					path += pid;
				}
				else {
					path += "," + pid;
				}
			}
		}
		return path;
	}

	/**
	 * Kommaseparierte PIDs werden als String übergeben, die einen Pfad im Baum des PreselectionTrees darstellen. Ist der Pfad vorhanden, dann wird er selektiert.
	 *
	 * @param treePath Pfad des Baums als kommaseparierten String
	 */
	public void setSelectedTreePath(final String treePath) {
//		_debug.finest("treePath = " + treePath);
		_treePath = treePath;
		if(treePath != null && !treePath.equals("")) {
			try {
				selectTreePath();
			}
			catch(Exception ignore) {
			}
		}
		else {	// Selektion aufheben
			_tree.clearSelection();
		}
	}

	/**
	 * Fügt einen <code>PreselectionTreeListener</code> hinzu.
	 *
	 * @param listener ein Objekt, welches den Listener implementiert
	 */
	public void addPreselectionListener(PreselectionTreeListener listener) {
		_listenerList.add(listener);
	}

	/**
	 * Entfernt einen <code>PreselectionTreeListener</code>.
	 *
	 * @param listener ein Objekt, welches den Listener implementiert
	 */
	public void removePreselectionListener(PreselectionTreeListener listener) {
		_listenerList.remove(listener);
	}

	/** Gibt dem Listener-Objekt bekannt, ob ein Koten im Baum angewählt wurde. Die gefilterten Systemobjekte werden dann an das Listener-Objekt übergeben. */
	private void notifyTreeSelectionChanged() {
		final Collection<SystemObject> unmodifiableCollection = Collections.unmodifiableCollection(_filterObjects);
		for(Iterator iterator = _listenerList.iterator(); iterator.hasNext();) {
			PreselectionTreeListener preselectionTreeListener = (PreselectionTreeListener)iterator.next();
			preselectionTreeListener.setObjects(unmodifiableCollection);
		}
	}
}

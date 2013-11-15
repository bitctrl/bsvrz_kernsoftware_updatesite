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
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.preselection.util.SortUtil;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.pat.sysbed.preselection.treeFilter.standard.Filter;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * Die Klasse <code>PreselectionTreeHandler</code> verarbeitet die Daten des Panels {@link PreselectionTree}.<p/> Mit dem Konstruktor wird das Panel
 * <code>PreselectionTree</code>, ein <code>ClientDavInterface</code> und ein <code>Systemobjekt</code> übergeben.<br/> Mittels des
 * <code>ClientDavInterfaces</code> und des Objektes werden Daten aus dem Datenverteiler geholt, die im <code>PreselectionTree</code> dargestellt werden sollen.
 * Außerdem werden alle Konfigurationsobjekte und alle dynamische Objekte zur späteren Bearbeitung geholt.
 * <p/>
 * Bei Anwahl eines Knotens werden die geholten Objekte ggf. durch zum Knoten gehörende Filter eingeschränkt. Die Liste dieser Objekte wird dann an das {@link
 * de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists}-Panel weitergereicht, wo weiter eingeschränkt werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8155 $
 */
class PreselectionTreeHandler {

	/** Der Debug-Logger der Klasse */
	private static Debug _debug = Debug.getLogger();

	/** speichert ein Objekt der Klasse <code>PreselectionTree</code> */
	private PreselectionTree _preselectionTree;

	/** speichert das ClientDavInterface */
	private ClientDavInterface _connection;

	/** speichert das aktuelle DataModel */
	private DataModel _configuration;

	/** speichert ein Objekt der Klasse <code>Receiver</code> */
	private Receiver _receiver;

	/** speichert alle Systemobjekte in einer Liste */
	private Collection<SystemObject> _allObjects = new ArrayList<SystemObject>();

	/** speichert die Parameter für den Vorauswahldialog (Baum) */
	private Collection<Object> _treeNodes;

	/**
	 * Ein Objekt dieser Klasse wird erstellt.
	 *
	 * @param preselectionTree das Panel, wo die Baum dargestellt werden soll
	 * @param connection       Verbindung zum Datenverteiler
	 */
	PreselectionTreeHandler(PreselectionTree preselectionTree, ClientDavInterface connection) {
		_preselectionTree = preselectionTree;
		_connection = connection;
		_configuration = _connection.getDataModel();
	}

	/**
	 * Die Systemobjekte werden beim Datenverteiler angemeldet und mitsamt den Baumobjekten im <code>PreselectionTree</code> angezeigt. Die benötigten Parameter
	 * (Systemobjekte) werden an den DaV übergeben, um mit den empfangenen Daten die spezifizierte Vorauswahl (Bäume) zu erstellen.
	 *
	 * @param treeNodes enthält die darzustellenden Systemobjekte und die implementierten Baumobjekte
	 */
	public void setTreeNodes(Collection<Object> treeNodes) {
		_treeNodes = treeNodes;
		// Systemobjekte rausfiltern
		List<SystemObject> objectList = new LinkedList<SystemObject>();
		for(Iterator iterator = treeNodes.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if(object instanceof SystemObject) {
				SystemObject systemObject = (SystemObject)object;
				objectList.add(systemObject);
			}
		}
		if(!objectList.isEmpty()) {
			try {
				getData(objectList);
			}
			catch(DataNotSubscribedException e) {
				e.printStackTrace();
			}
		}
		else {    // es gibt keine Objekte
//			System.out.println("es wurden keine Objekte übergeben!");
			TreeNodeObject rootNode = new TreeNodeObject("Wurzel", "");
			for(Iterator iterator = _treeNodes.iterator(); iterator.hasNext();) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)iterator.next();
				rootNode.addChild(treeNodeObject);
			}
			TreeModel treeModel = new DataTreeModel(rootNode);
			_preselectionTree.setTreeData(treeModel);
		}
	}

	/**
	 * Auf Grundlage eines ClientDavInterfaces wird ein {@link DataModel} erstellt und an das {@link ClientReceiverInterface} (implementiert durch die Klasse
	 * {@link Receiver}) weitergegeben.
	 *
	 * @param objectList Objektliste, die beim Datenverteiler angemeldet werden soll
	 */
	private void getData(List<SystemObject> objectList) {
		AttributeGroup atg = _configuration.getAttributeGroup("atg.datenAuswahlMenü");
		Aspect aspect = _configuration.getAspect("asp.parameterSoll");
		DataDescription dataDescription = new DataDescription(atg, aspect);
		_receiver = new Receiver();
		_connection.subscribeReceiver(_receiver, objectList, dataDescription, ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * Liefert alle Konfigurations- und dynamischen Objekte.
	 *
	 * @return alle Konfigurations- und dynamischen Objekte
	 */
	Collection<SystemObject> getAllObjects() {
		return Collections.unmodifiableCollection(_allObjects);
	}

	/**
	 * Holt auf Grundlage des {@link de.bsvrz.dav.daf.main.config.DataModel DataModels} alle Konfigurations- und dynamischen Objekte und speichert sie in einer Collection. Diese kann mittels
	 * {@link #getAllObjects} geholt werden.
	 */
	void initDataLists() {
		Set<SystemObject> set = new HashSet<SystemObject>();
		List confObjs = _configuration.getType("typ.konfigurationsObjekt").getObjects();
		for(Iterator iterator = confObjs.iterator(); iterator.hasNext();) {
			SystemObject systemObject = (SystemObject)iterator.next();
			set.add(systemObject);
		}
		List dynObjs = _configuration.getType("typ.dynamischesObjekt").getObjects();
		for(Iterator iterator = dynObjs.iterator(); iterator.hasNext();) {
			SystemObject systemObject = (SystemObject)iterator.next();
			set.add(systemObject);
		}

		_allObjects.addAll(SortUtil.sortCollection(set));
	}

	/**
	 * Gibt die Parameter für die Vorauswahl (Baum) zurück. Die Collection enthält Systemobjekte und {@link TreeNodeObject Knotenobjekte}. Anhand der Objekte wird
	 * der Baum für die Vorauswahl erzeugt.
	 *
	 * @return die Sammlung von System- und Knotenobjekten
	 */
	Collection<Object> getTreeNodes() {
		return _treeNodes;
	}


	/**
	 * Die Klasse <code>Receiver</code> implementiert das Interface <code>ClientReceiverInterface</code> und dient somit als Schnittstelle, um Aktualisierungen von
	 * Daten, die zum Empfang angemeldet sind, zu verarbeiten.
	 *
	 * @see Receiver#update(de.bsvrz.dav.daf.main.ResultData[])
	 */
	private class Receiver implements ClientReceiverInterface {

		/** speichert ein empfangenes Menü-Data-Objekt */
		private List<TreeNodeObject> _tree = new ArrayList<TreeNodeObject>();

		/**
		 * Diese Methode erhält die Daten vom Datenverteiler. Die empfangenen Daten werden in einen Baum umgewandelt und dem Panel {@link PreselectionTree}
		 * übermittelt.
		 *
		 * @param results die Daten vom Datenverteiler
		 */
		public void update(ResultData[] results) {
			final TreeModel treeModel;
			treeModel = makeTreeModel(results);     // Baum wird erstellt
			SwingUtilities.invokeLater(
					new Runnable() {
						public void run() {
							// Baum wird an PreselectionTree übermittelt
							_preselectionTree.setTreeData(treeModel);
						}
					}
			);
		}

		/**
		 * Erstellt aus einer Collection von Systemobjekten und <code>TreeNodeObject</code>s einen Baum. Für jedes Systemobjekt wird der dazugehörige Datensatz vom
		 * Datenverteiler in einen Baum umgewandelt. Die so erstellten Bäume und die <code>TreeNodeObject</code>s werden an eine Pseudo-Wurzel gehangen und in ein
		 * <code>TreeModel</code> umgewandelt.
		 *
		 * @param results Daten vom Datenverteiler zu den Systemobjekten
		 *
		 * @return den Baum für das <code>PreselectionTree</code>
		 *
		 * @see TreeNodeObject
		 * @see PreselectionTree
		 */
		private TreeModel makeTreeModel(ResultData[] results) {
			// Pseudo-Wurzel erstellen
			TreeNodeObject rootNode = new TreeNodeObject("Wurzel", "");
			int position = 0;
			for(Iterator iterator = _treeNodes.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				TreeNodeObject treeNodeObject = null;
				if(object instanceof SystemObject) {           // vom Typ SystemObject
					SystemObject systemObject = (SystemObject)object;
					// resultListe nach passendem SystemObjekt durchsuchen
					for(int i = 0; i < results.length; i++) {  // falls es eins gibt -> nehme dieses
						ResultData result = results[i];
						if(systemObject == result.getObject()) {       // passendes gefunden
							treeNodeObject = makeRootNode(result);
						}
					}
				}
				else if(object instanceof TreeNodeObject) {  // vom Typ TreeNodeObject
					treeNodeObject = (TreeNodeObject)object;
				}
				if(treeNodeObject != null) {
					if(position < _tree.size()) {
						_tree.set(position, treeNodeObject);
					}
					else {
						_tree.add(treeNodeObject);
					}
				}
				position++;
			}
			for(Iterator iterator = _tree.iterator(); iterator.hasNext();) {   // DataTreeModel erzeugen
				TreeNodeObject treeNodeObject = (TreeNodeObject)iterator.next();
				rootNode.addChild(treeNodeObject);
			}
			return new DataTreeModel(rootNode);
		}

		/**
		 * Erstellt zu einem einzelnen Datensatz vom Datenverteiler einen Baum und gibt den Wurzelknoten ({@link TreeNodeObject}) zurück.
		 *
		 * @param result Datensatz vom Datenverteiler
		 *
		 * @return den Wurzelknoten
		 */
		private TreeNodeObject makeRootNode(ResultData result) {
			// erste Knoten ist Wurzelknoten
			String rootPid = "";
			// alle erzeugten TreeNodeObjekte werden in einer HashMap gespeichert
			Map<String, TreeNodeObject> hashMap = new HashMap<String, TreeNodeObject>();
			// erst werden alle TreeNodeObjekte ohne Untermenüeinträge erstellt
			if(!result.hasData()) {
				_debug.warning("Zum Systemobject " + result.getObject().getName() + " gibt es keine Daten!");
				return new TreeNodeObject("");
			}
			Data data = result.getData();
			Data menu = data.getItem("Menü");
			Data.Array menuArray = menu.asArray();
			for(int i = 0; i < menuArray.getLength(); i++) {
				Data menuData = menuArray.getItem(i);   // den i-ten Eintrag
				String pid = menuData.getTextValue("Pid").getText();
				String name = menuData.getTextValue("Name").getText();
				TreeNodeObject treeNode = new TreeNodeObject(name, pid);
				Data.Array filterArray = menuData.getArray("Filter");
				for(int j = 0; j < filterArray.getLength(); j++) {
					Data filter = filterArray.getItem(j);
					String criteria = filter.getTextValue("Kriterium").getText();
					String[] values = filter.getTextArray("Wert").getTextArray();
					treeNode.addFilter(new Filter(criteria, values, _connection));
				}
				// speichern der Knoten in einer HashMap
				hashMap.put(pid, treeNode);
				// Wurzelknoten merken
				if(i == 0) {
					rootPid = pid;
				}
			}
			// Untermenü-Verweise werden erstellt.
			for(int i = 0; i < menuArray.getLength(); i++) {
				Data menuData = menuArray.getItem(i);
				String[] subItems = menuData.getArray("UnterMenü").asTextArray().getTextArray();
				if(subItems.length > 0) {
					String pid = menuData.getTextValue("Pid").getText();
					TreeNodeObject treeNode = hashMap.get(pid);    // hierzu Untermenüs erstellen
					for(int j = 0; j < subItems.length; j++) {
						String subItem = subItems[j];
						// gibt es zu dieser Pid überhaupt einen Eintrag?
						if(hashMap.containsKey(subItem)) {
							treeNode.addChild(hashMap.get(subItem));
						}
						else {
							_debug.warning("Zur Untermenü-Pid " + subItem + " gibt es kein Data-Objekt!");
						}
					}
				}
			}
			TreeNodeObject rootNode = hashMap.get(rootPid);
			if(rootNode != null) {
				return rootNode;
			}
			else {
				return new TreeNodeObject("");
			}
		}
	}

	/** Die Klasse <code>DataTreeModel</code> repräsentiert ein {@link TreeModel}. */
	private static class DataTreeModel implements TreeModel {

		/** speichert den Wurzelknoten */
		private TreeNodeObject _rootObject;

		/**
		 * Konstruktor
		 *
		 * @param rootObject der Wurzelknoten
		 */
		public DataTreeModel(TreeNodeObject rootObject) {
			_rootObject = rootObject;
		}

		/**
		 * Gibt des Wurzelknoten zurück.
		 *
		 * @return den Wurzelknoten
		 */
		public Object getRoot() {
			return _rootObject;
		}

		/**
		 * Gibt zurück, wieviele Nachfolger ein Knoten hat.
		 *
		 * @param parent ein Knoten des Baums
		 *
		 * @return Anzahl der Nachfolger des Knotens
		 */
		public int getChildCount(Object parent) {
			if(parent instanceof TreeNodeObject) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)parent;
				return treeNodeObject.getChildCount();
			}
			return 0;
		}

		/**
		 * Gibt zurück, ob ein Knoten ein Blatt ist.
		 *
		 * @param node ein Knoten des Baums
		 *
		 * @return ob ein Knoten ein Blatt ist (true/false)
		 */
		public boolean isLeaf(Object node) {
			if(node instanceof TreeNodeObject) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)node;
				if(treeNodeObject.getChildCount() == 0) {
					return true;
				}
				else {
					return false;
				}
			}
			return false;
		}

		public void addTreeModelListener(TreeModelListener l) {
			// wird hier nicht benötigt
		}

		public void removeTreeModelListener(TreeModelListener l) {
			// wird hier nicht benötigt
		}

		/**
		 * Gibt zu einem Knoten im Baum einen bestimmten Nachfolger zurück.
		 *
		 * @param parent ein Knoten im Baum
		 * @param index  der wievielte Nachfolger
		 *
		 * @return den gewünschten Nachfolger
		 */
		public Object getChild(Object parent, int index) {
			if(parent instanceof TreeNodeObject) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)parent;
				return treeNodeObject.getChild(index);
			}
			return null;
		}

		/**
		 * Gibt zu einem Nachfolger eines Knotens seine Position innerhalb alles Nachfolger dieses Knotens zurück.
		 *
		 * @param parent ein Knoten im Baum
		 * @param child  ein Nachfolger des Knotens
		 *
		 * @return Position des Nachfolgers
		 */
		public int getIndexOfChild(Object parent, Object child) {
			if(parent instanceof TreeNodeObject) {
				TreeNodeObject treeNodeObject = (TreeNodeObject)parent;
				return treeNodeObject.indexOfChild((TreeNodeObject)child);
			}
			return 0;
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			// wird hier nicht benötigt
		}
	}
}

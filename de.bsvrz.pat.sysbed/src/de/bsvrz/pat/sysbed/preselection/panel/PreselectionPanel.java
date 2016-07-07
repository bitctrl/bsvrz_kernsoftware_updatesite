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
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsListener;
import de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Die Klasse <code>PreselectionPanel</code> stellt die gesamte Datenidentifikationsauswahl zur Verfügung. Hierzu gehören die Klassen
 * <code>PreselectionTree</code> und <code>PreselectionLists</code>.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree
 * @see PreselectionLists
 */
public class PreselectionPanel extends JPanel {

	/** speichert ein Objekt der Klasse <code>PreselectionLists</code> */
	private final PreselectionLists _preselectionLists;

	/** speicher ein Objekt der Klasse <code>PreselectionTree</code> */
	private final PreselectionTree _preselectionTree;

	/** speichert die Verbindung zum Datenverteiler */
	private final ClientDavInterface _connection;

	/** speichert die Parameter für die Vorauswahl */
	private final Collection<Object> _treeNodes;

	/**
	 * Konstruktor, der ein Objekt der Klasse <code>PreselectionPanel</code> erzeugt.
	 *
	 * @param connection die Verbindung zum Datenverteiler
	 * @param treeNodes  ein Parameter zur Spezifizierung der Vorauswahl
	 *
	 * @see #createAndShowGui()
	 */
	public PreselectionPanel(ClientDavInterface connection, Collection<Object> treeNodes) {
		_connection = connection;
		_treeNodes = treeNodes;
		_preselectionLists = new PreselectionLists();
		_preselectionTree = new PreselectionTree(_connection, _treeNodes);
		createAndShowGui();
	}

	/**
	 * Die Methode wird vom Konstruktor aufgerufen und stellt die spezifizierte Vorauswahl durch die Komponente <code>PreselectionTree</code> und die konkrete
	 * Auswahl durch die Komponente <code>PreselectionLists</code> dar. 
	 *
	 * @see PreselectionTree
	 * @see PreselectionLists
	 */
	private void createAndShowGui() {
		setLayout(new BorderLayout());

		_preselectionTree.addPreselectionListener(_preselectionLists);

		// teilt Tree und Lists voneinander mittels SplitPane
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setLeftComponent(_preselectionTree);
		splitPane.setRightComponent(_preselectionLists);
		splitPane.setResizeWeight(0.25);

		add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * Delegiert das Hinzufügen des <code>PreselectionListsListener</code>s weiter an die Klasse {@link PreselectionLists}
	 *
	 * @param listener der anzumeldende Listener
	 */
	public void addPreselectionListener(PreselectionListsListener listener) {
		_preselectionLists.addPreselectionListener(listener);
	}

	/**
	 * Delegiert das Entfernen des <code>PreselectionListsListener</code>s weiter an die Klasse {@link PreselectionLists}
	 *
	 * @param listener der zu entfernende Listener
	 */
	public void removePreselectionListener(PreselectionListsListener listener) {
		_preselectionLists.removePreselectionListener(listener);
	}

	/**
	 * Delegiert an die Klasse <code>PreselectionLists</code>, ob die Attributgruppen angezeigt werden sollen.
	 *
	 * @param showAttributeGroups gibt an, ob die Attributgruppen angezeigt werden sollen
	 */
	public void showAttributeGroups(boolean showAttributeGroups) {
		_preselectionLists.showAttributeGroups(showAttributeGroups);
	}

	/**
	 * Delegiert an die Klasse <code>PreselectionLists</code>, ob die Aspekte angezeigt werden sollen.
	 *
	 * @param showAspects gibt an, ob die Aspekte angezeigt werden sollen
	 */
	public void showAspects(boolean showAspects) {
		_preselectionLists.showAspects(showAspects);
	}

	/**
	 * Zeigt die Simulationsvariante an. Default-Wert ist "0". Soll ein anderer Wert voreingestellt sein, dann ist die Methode {@link #setSimulationVariant}
	 * aufzurufen.
	 */
	public void showSimulationVariant() {
		_preselectionLists.showSimulationVariant();
	}

	/**
	 * Gibt die eingestellte Simulationsvariante zurück.
	 *
	 * @return die eingestellte Simulationsvariante
	 */
	public int getSimulationVariant() {
		return _preselectionLists.getSimulationVariant();
	}

	/**
	 * Mit dieser Methode kann die Simulationsvariante gesetzt werden.
	 *
	 * @param value neuer Wert der Simulationsvariante
	 */
	public void setSimulationVariant(int value) {
		_preselectionLists.setSimulationVariant(value);
	}

	/**
	 * Mit dieser Methode kann man sich das Objekt der Klasse <code>PreselectionLists</code> holen, welches auch vom <code>PreselectionPanel</code> benutzt wird,
	 * um auf seine <code>public</code>-Methoden zugreifen zu können.
	 *
	 * @return ein Objekt der Klasse <code>PreselectionLists</code>
	 */
	public PreselectionLists getPreselectionLists() {
		return _preselectionLists;
	}

	/**
	 * Mit dieser Methode kann man sich das Objekt der Klasse <code>PreselectionTree</code> holen, welches vom <code> PreselectionPanel</code> benutzt wird, um auf
	 * seine <code>public</code>-Methoden zugreifen zu können.
	 *
	 * @return ein Objekt der Klasse <code>PreselectionPanel</code>
	 */
	public PreselectionTree getPreselectionTree() {
		return _preselectionTree;
	}

	/**
	 * Delegiert an die Klasse <code>PreselectionLists</code>, ob und welcher Filter zusätzlich die Listen filtern soll.
	 *
	 * @param listsFilter der Filter
	 */
	public void setPreselectionListsFilter(PreselectionListsFilter listsFilter) {
		_preselectionLists.setPreselectionListsFilter(listsFilter);
	}
}

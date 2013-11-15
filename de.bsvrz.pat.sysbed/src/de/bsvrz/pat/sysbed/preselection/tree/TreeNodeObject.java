/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import de.bsvrz.pat.sysbed.plugins.parameditor.ParameterEditor;
import de.bsvrz.pat.sysbed.preselection.treeFilter.standard.Filter;

import java.util.*;

/**
 * Speichert die Informationen zu einem Knoten/Blatt im Datenbaum des Panels {@link PreselectionTree}. Dieses Objekt wird f�r die Klasse {@link
 * PreselectionTreeHandler.DataTreeModel} benutzt.
 * <p/>
 * Dieses Objekt entspricht den Eintr�gen, die im {@link ParameterEditor Parametereditor} gesetzt werden/wurden. Es enth�lt einen Namen, eine Pid,
 * UnterMen�-Eintr�ge und Objekte vom Typ {@link Filter}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5019 $
 */
public class TreeNodeObject {

	/** speichert den Namen des Objektes */
	private String _name;

	/** speichert die Pid des Objektes */
	private String _pid;

	/** speichert die UnterMen�-Eintr�ge */
	private final List<TreeNodeObject> _subMenu;

	/** speichert die Filter-Objekte */
	private final List<Filter> _filters;

	/**
	 * Konstruktor, um ein <code>TreeNodeObject</code> zu erstellen.
	 *
	 * @param name der Name des Objekts
	 * @param pid  die Pid des Objekts
	 */
	public TreeNodeObject(String name, String pid) {
		_subMenu = new ArrayList<TreeNodeObject>();
		_filters = new ArrayList<Filter>();
		_name = name;
		_pid = pid;
	}

	/**
	 * Konstruktor, um ein <code>TreeNodeObject</code> zu erstellen.
	 *
	 * @param pid die Pid des Objekts
	 */
	public TreeNodeObject(String pid) {
		_subMenu = new ArrayList<TreeNodeObject>();
		_filters = new ArrayList<Filter>();
		_pid = pid;
	}

	/**
	 * Setzt den Namen des Objekts.
	 *
	 * @param name der Name des Objekts
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Gibt des Namen des Objekts zur�ck.
	 *
	 * @return der Name des Objekts
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Setzt die Pid des Objekts.
	 *
	 * @param pid die Pid des Objekts
	 */
	public void setPid(String pid) {
		_pid = pid;
	}

	/**
	 * Gibt die Pid des Objekts zur�ck.
	 *
	 * @return die Pid des Objekts
	 */
	public String getPid() {
		return _pid;
	}

	/**
	 * Gibt die Anzahl der UnterMen�-Eintr�ge zur�ck.
	 *
	 * @return Anzahl der UnterMen�s
	 */
	public int getChildCount() {
		return _subMenu.size();
	}

	/**
	 * F�gt einen UnterMen�-Eintrag zum Objekt hinzu.
	 *
	 * @param child der hinzuzuf�gende UnterMen�-Eintrag
	 */
	public void addChild(TreeNodeObject child) {
		_subMenu.add(child);
	}

	/**
	 * F�gt einen Filter zum Objekt hinzu.
	 *
	 * @param filter der hinzuzuf�gende Filter
	 */
	public void addFilter(Filter filter) {
		_filters.add(filter);
	}

	/**
	 * Gibt die Liste der Filter zur�ck.
	 *
	 * @return die Liste mit den Filtern
	 */
	public List getFilters() {
		return _filters;
	}

	/**
	 * Gibt zu einer bestimmten Index-Position den UnterMen�-Eintrag zur�ck
	 *
	 * @param index Position des gew�nschten Eintrages
	 *
	 * @return UnterMen�-Eintrag
	 */
	public TreeNodeObject getChild(int index) {
		return _subMenu.get(index);
	}

	/**
	 * Gibt zu einem UnterMen�-Eintrag die Position in der Liste zur�ck.
	 *
	 * @param child der UnterMen�-Eintrag
	 *
	 * @return die Position des Eintrages
	 */
	public int indexOfChild(TreeNodeObject child) {
		return _subMenu.indexOf(child);
	}

	/**
	 * Besitzt das Objekt einen Namen, wird dieser zur�ckgeben, ansonsten wird die Pid zur�ckgegeben.
	 *
	 * @return Name oder Pid des Objekts
	 */
	public String toString() {
		if(_name == null) {
			return _pid;
		}
		else {
			return _name;
		}
	}
}

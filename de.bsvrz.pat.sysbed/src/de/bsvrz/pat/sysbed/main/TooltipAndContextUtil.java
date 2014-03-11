/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.SystemObject;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 0000 $
 */
public class TooltipAndContextUtil {

	private static final ClipboardOwner owner = new ClipboardOwner(){

		@Override
		public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
		}
	};

	public static void createComponentPopupMenu(final SelectionModel systemObjectList) {
		final JPopupMenu popup = new JPopupMenu();
		popup.add(new ContextAction("ID kopieren", "IDs kopieren") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				StringBuilder builder = new StringBuilder();
				for(SystemObject o : systemObjectList.getSelectedObjects()) {
					builder.append(o.getId()).append('\n');
				}
				if(builder.length() > 0) builder.setLength(builder.length()-1);
				StringSelection stringSelection = new StringSelection(builder.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, owner);
			}
		});
		popup.add(new ContextAction("Pid kopieren", "Pids kopieren") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				StringBuilder builder = new StringBuilder();
				for(SystemObject o : systemObjectList.getSelectedObjects()) {
					builder.append(o.getPid()).append('\n');
				}
				if(builder.length() > 0) builder.setLength(builder.length()-1);
				StringSelection stringSelection = new StringSelection(builder.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, owner);
			}
		});
		popup.add(new ContextAction("Name kopieren", "Namen kopieren") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				StringBuilder builder = new StringBuilder();
				for(SystemObject o : systemObjectList.getSelectedObjects()) {
					builder.append(o.getName()).append('\n');
				}
				if(builder.length() > 0) builder.setLength(builder.length()-1);
				StringSelection stringSelection = new StringSelection(builder.toString());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, owner);
			}
		});
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				int length = systemObjectList.getSelectedObjects().length;
				Component[] subElements = popup.getComponents();
				for(Component menuElement : subElements) {
					if(menuElement instanceof JMenuItem) {
						JMenuItem element = (JMenuItem) menuElement;
						element.setEnabled(length > 0);
						if(element.getAction() != null){
							element.setText(((ContextAction) element.getAction()).getName(length));
						}
					}
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}
		});
		systemObjectList.setComponentPopupMenu(popup);
	}

	public static String getTooltip(final SystemObject systemObject) {
		String tooltip = "<html>";
		String info = null;
		String pidAndID = "";
		String typ = null;
		String kb = null;
		if(systemObject.getInfo().getShortInfo().length() > 0) {
			info = "Info: " + systemObject.getInfo().getShortInfo();
		}
		String pid = systemObject.getPid();
		if(pid != null && !pid.equals("")) {
			pidAndID = "Pid: " + pid + "<br>";
		}
		pidAndID += "ID: " + systemObject.getId();
		typ = "Typ: " + systemObject.getType().getNameOrPidOrId();
		kb = "KB: " + systemObject.getConfigurationArea().getNameOrPidOrId();
		if(info != null) {
			tooltip += info + "<br>" + pidAndID;
		}
		else {
			tooltip += pidAndID;
		}
		tooltip += "<br>" + typ;
		tooltip += "<br>" + kb;
		tooltip += "</html>";
		return tooltip;
	}

	private static abstract class ContextAction extends AbstractAction {
		private static final long serialVersionUID = 3780727574100059084L;
		private final String _singularName;
		private final String _pluralName;

		public ContextAction(final String singularName, final String pluralName) {
			_singularName = singularName;
			_pluralName = pluralName;
		}

		public String getName(final int length) {
			if(length == 1)
				return _singularName;
			return _pluralName;
		}
	}
}

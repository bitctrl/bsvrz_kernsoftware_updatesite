/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.plugins.api;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Diese Klasse stellt ein Panel mit den Buttons "Speichern unter ...", "Abbrechen" und "OK" dar. Eine Klasse, welche das Interface {@link DialogInterface}
 * implementiert, implementiert dadurch die Funktionalität dieser drei Buttons.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5001 $
 * @see DialogInterface
 */
public class ButtonBar extends JPanel {

	/** speichert den "Speichern..." - Button */
	private final JButton _saveButton;

	/** speichert den "Abbrechen" - Button */
	private final CancelButton _cancelButton;

	/** speichert den "OK" - Button */
	private final JButton _acceptButton;

	/** speichert ein Objekt, welches das Interface {@link DialogInterface} implementiert hat */
	private final DialogInterface _dialog;

	/**
	 * Dem Konstruktor wird ein Objekt übergeben, welches das Interface {@link DialogInterface} implementiert hat. Dieses Objekt liefert die Funktionalität der
	 * Buttons "Speichern...", "Abbrechen" und "OK" dieser Klasse.
	 *
	 * @param dialog ein Objekt, welches das Interface <code>DialogInterface</code> implementiert
	 */
	public ButtonBar(DialogInterface dialog) {
		_dialog = dialog;
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		_saveButton = new JButton("Speichern unter ...");
		_saveButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String title = JOptionPane.showInputDialog("Bitte einen Namen vergeben: ");
						if(title != null) {
							_dialog.doSave(title);
						}
					}
				}
		);
		_cancelButton = new CancelButton("Abbrechen");
		_cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_dialog.doCancel();
					}
				}
		);
		_acceptButton = new JButton("OK");          // bei Return aktivieren
		_acceptButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_dialog.doOK();
					}
				}
		);

		add(_saveButton);
		add(_cancelButton);
		add(_acceptButton);
	}

	/**
	 * Gibt den "OK" - Button des Panels zurück.
	 *
	 * @return der "OK" - Button
	 */
	public JButton getAcceptButton() {
		return _acceptButton;
	}

	/** Die Klasse <code>CancelButton</code> erweitert den <code>JButton</code>, damit dieser auf die Taste ESC reagiert. */
	private class CancelButton extends JButton {

		/**
		 * Dem Konstruktor wird ein Titel des Buttons übergeben. Zusätzlich wird die ESC-Taste an diesen Button gebunden. D.h. wenn die ESC-Taste betätigt wird, wird
		 * dieser Button ausgeführt.
		 *
		 * @param title Titel des Buttons
		 */
		public CancelButton(String title) {
			super(title);
			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String cmd = event.getActionCommand();
					if(cmd.equals("PressedESCAPE")) {
						doClick();
					}
				}
			};
			registerKeyboardAction(actionListener, "PressedESCAPE", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JButton.WHEN_IN_FOCUSED_WINDOW);
		}
	}
}

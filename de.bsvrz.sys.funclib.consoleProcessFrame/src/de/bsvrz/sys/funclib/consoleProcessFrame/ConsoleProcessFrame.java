/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.consoleProcessFrame.
 * 
 * de.bsvrz.sys.funclib.consoleProcessFrame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.consoleProcessFrame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.consoleProcessFrame; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.consoleProcessFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * Implementiert ein Fenster, das mit einem externen Java-Prozess verbunden ist. Der Prozess wird beim erzeugen des Fensters gestartet und beim Schließen des
 * Fensters beendet. Die Textausgaben des Prozesses werden im Fenster dargestellt.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConsoleProcessFrame extends JFrame {

	private final ConsoleProcessPanel _consoleProcessPanel;

	protected ConsoleProcessFrame(String title, ConsoleProcessPanel consoleProcessPanel) throws IOException {
		super(title);
		_consoleProcessPanel = consoleProcessPanel;
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						_consoleProcessPanel.killProcess();
					}
				}
		);
		getContentPane().add(_consoleProcessPanel, BorderLayout.CENTER);
		pack();
		setVisible(true);
		_consoleProcessPanel.start();
	}

	public static ConsoleProcessFrame createJavaProcessFrame(String title, String className, String[] arguments, String[] environment, File workingDirectory)
			throws IOException {
		ConsoleProcessPanel panel = ConsoleProcessPanel.createJavaProcessPanel(className, arguments, environment, workingDirectory);
		return new ConsoleProcessFrame(title, panel);
	}

	public static void main(String[] args) {
		try {
			createJavaProcessFrame("Test", "rstest.ExecutionTest", null, null, null);
		}
		catch(Exception e) {
			System.err.println("Fehler:" + e);
		}
	}
}

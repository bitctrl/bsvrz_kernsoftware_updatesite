/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
import java.io.File;

/**
 * Diese Klasse stellt die Ausgabeoptionen eines Moduls als Panel dar. Die Optionen bestehen aus einem Detaillierungsgrad und der M�glichkeit die Ausgabe in
 * eine Datei umzuleiten. Es kann zwischen folgenden Detaillierungsgraden gew�hlt werden: <ul> <li>keine Ausgabe</li> <li>Aktualisierung</li>
 * <li>Kopfinformationen</li> <li>Daten</li> <li>XML</li> </ul> Mittels der Getter- und Setter-Methoden k�nnen die Elemente auf dem Panel voreingestellt
 * werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5001 $
 */
public class OutputOptionsPanel extends JPanel {

	/** Detaillierungsgrad: keine Ausgabe */
	public static final int NO_OUTPUT = 0;

	/** Detaillierungsgrad: Aktualisierung */
	public static final int UPDATING = 1;

	/** Detaillierungsgrad: Kopfinformationen */
	public static final int HEADER_INFORMATION = 2;

	/** Detaillierungsgrad: Daten */
	public static final int DATA = 3;

	/** Detaillierungsgrad: XML */
	public static final int XML = 4;


	/** gibt an, welche Detailierungsgrade zur Verf�gung stehen */
	private final String[] _detailUnit = {"keine Ausgabe", "Aktualisierung", "Kopfinformationen", "Daten", "XML"};

	/** speichert die Auswahlbox f�r den Detaillierungsgrad */
	private final JComboBox _detailLevelCombo;

	/** gibt an, in welche Datei die Daten geschrieben werden */
	private final JTextField _outputTextField;

	/** gibt an, ob die Daten in eine Datei geschrieben werden */
	private final JCheckBox _outputCheckBox;

	/** Dateidialog, damit eine Datei zum Speichern der Daten ausgew�hlt werden kann */
	private final JFileChooser _fileChooser = new JFileChooser();

	/** speichert die ausgew�hlte Datei */
	private File _outputFile = null;


	/**
	 * Die Ausgabeoptionen eines Moduls werden auf einem Panel dargestellt. Die Optionen bestehen aus einem Detaillierungsgrad und der M�glichkeit die Ausgabe in
	 * eine Datei umzuleiten.
	 */
	public OutputOptionsPanel() {
		setBorder(BorderFactory.createTitledBorder("Ausgabeoptionen"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel levelPanel = new JPanel();
		levelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel detailLevelLabel = new JLabel("Detaillierungsgrad: ");
		_detailLevelCombo = new JComboBox(_detailUnit);
		levelPanel.add(detailLevelLabel);
		levelPanel.add(_detailLevelCombo);
		add(levelPanel);

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
		_outputTextField = new JTextField();
		_outputTextField.setMaximumSize(new Dimension(_outputTextField.getMaximumSize().width, _outputTextField.getPreferredSize().height));
		_outputTextField.setEditable(false);

		_outputCheckBox = new JCheckBox("Ausgabe in Datei speichern: ", false);
		_outputCheckBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(_outputCheckBox.isSelected()) {
							_outputTextField.setEnabled(true);
						}
						else {
							_outputTextField.setEnabled(false);
						}
					}
				}
		);
		_fileChooser.setMultiSelectionEnabled(false);
		final JButton outputButton = new JButton("...");
		outputButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(_outputFile != null) {
							_fileChooser.setSelectedFile(_outputFile);
						}
						if(_fileChooser.showSaveDialog(outputButton) == JFileChooser.APPROVE_OPTION) {
							_outputFile = _fileChooser.getSelectedFile();
							_outputTextField.setText(_outputFile.getName());
							_outputTextField.setEnabled(true);
							_outputCheckBox.setSelected(true);
						}
					}
				}
		);
		filePanel.add(_outputCheckBox);
		filePanel.add(_outputTextField);
		filePanel.add(Box.createHorizontalStrut(5));
		filePanel.add(outputButton);

		add(filePanel);
	}

	/**
	 * Mit dieser Methode kann der Detaillierungsgrad gesetzt werden.
	 *
	 * @param level der Detaillierungsgrad
	 */
	public void setDetailLevel(int level) {
		_detailLevelCombo.setSelectedIndex(level);
	}

	/**
	 * Mit dieser Methode kann der Detaillierungsgrad gesetzt werden.
	 *
	 * @param level der Detaillierungsgrad
	 */
	public void setDetailLevel(final String level) {
		int index = 0;
		if(level.equals("nein")) {
			index = 0;
		}
		else if(level.equals("aktualisierung")) {
			index = 1;
		}
		else if(level.equals("kopf")) {
			index = 2;
		}
		else if(level.equals("daten")) {
			index = 3;
		}
		else if(level.equals("xml")) {
			index = 4;
		}
		_detailLevelCombo.setSelectedIndex(index);
	}

	/**
	 * Gibt den ausgew�hlten Detaillierungsgrad zur�ck.
	 *
	 * @return Detaillierungsgrad
	 */
	public String getDetailLevel() {
		int index = _detailLevelCombo.getSelectedIndex();
		String level = "";
		switch(index) {
			case 0:
				level = "nein";
				break;
			case 1:
				level = "aktualisierung";
				break;
			case 2:
				level = "kopf";
				break;
			case 3:
				level = "daten";
				break;
			case 4:
				level = "xml";
				break;
		}
		return level;
	}

	/**
	 * Gibt zu der ausgew�hlten Datei den gesamten Pfad zur�ck.
	 *
	 * @return den Pfad der ausgew�hlten Datei
	 */
	public String getFileName() {
		if(_outputCheckBox.isSelected()) {
			return _outputFile.getPath();
		}
		else {
			return null;
		}
	}

	/**
	 * Mit dieser Methode kann die Datei bestimmt werden, welche f�r die Ausgabe genutzt werden soll.
	 *
	 * @param fileName Pfad incl. Dateiname f�r die Ausgabe
	 */
	public void setFileName(final String fileName) {
		_outputCheckBox.setSelected(true);
		_outputFile = new File(fileName);
		_outputTextField.setText(_outputFile.getName());
	}
}

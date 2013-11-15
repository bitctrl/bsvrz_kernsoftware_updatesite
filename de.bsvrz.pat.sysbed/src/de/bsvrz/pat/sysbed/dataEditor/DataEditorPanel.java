/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.dataEditor;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse stellt einen Datensatz in strukturierter Form (entsprechend der Attributgruppe) in einem JPanel dar. Die einzelnen Attribute sind entsprechend
 * der Einschränkungen des Datenmodells editierbar. Bei Bedarf kann auch eine nicht editierbar Form gewählt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10125 $
 * @see #DataEditorPanel(de.bsvrz.dav.daf.main.ClientDavInterface)
 * @see #setData(de.bsvrz.dav.daf.main.Data)
 * @see #setResultData(de.bsvrz.dav.daf.main.ResultData)
 */
public class DataEditorPanel extends JPanel {

	/** ein Zahlenformat */
	private static final NumberFormat _integerNumberFormat = NumberFormat.getNumberInstance();

	/** Genauigkeit des Zahlenformats */
	private static final NumberFormat _precisionTestNumberFormat;

	/** Hintergrundfarbe eines Attributwertes, der nicht definiert ist und nicht über den Datenvertiler verschickt werden kann. */
	private static final Color _backgroundUndefinedValue = new Color(Color.HSBtoRGB(0f, 0.75f, 1.0f));

	/** Hintergrundfarbe eines Attributwertes, der über den Datenvertiler verschickt werden kann. */
	private static final Color _backgroundColorDefinedValue = Color.WHITE;

	/** String der ausgegeben wird, wenn der Attributwert "undefiniert" ist. Der String entspricht dabei nicht dem wahren undefiniert Wert. */
	private static final String _undefinedString = "_Undefiniert_";

	/** String, der in Comboboxen angezeigt wird und anzeigt, dass der Default-Wert benutzt werden soll. */
	private static final String _defaultValueString = "Default-Wert";

	/**
	 * Das Zahlenformat erhält eine Formatierung.
	 */
	static {
		_integerNumberFormat.setMinimumIntegerDigits(1);
		_integerNumberFormat.setMaximumIntegerDigits(999);
		_integerNumberFormat.setGroupingUsed(false);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		_precisionTestNumberFormat = new DecimalFormat("0.#", symbols);
		_precisionTestNumberFormat.setMaximumFractionDigits(999);
	}


	/** der Debug-Logger */
	private final Debug _debug = Debug.getLogger();

	/** die Verbindung zum Datenverteiler */
	private final ClientDavInterface _connection;

	/** speichert die aktuellen Daten */
	private Data _data;

	/** Grafische Komponente zum Darstellen der Daten */
	private final Box _dataPane;

	/** gibt an, ob die dargestellten Felder editierbar sein sollen */
	private boolean _editable = true;

	/* #################### public Methoden #################### */

	/**
	 * Der Konstruktor nimmt die aktuelle Verbindung zum Datenverteiler entgegen und stellt initial einen leeren Datensatz dar.
	 *
	 * @param connection Verbindung zum Datenverteiler
	 */
	public DataEditorPanel(final ClientDavInterface connection) {
		_connection = connection;
		_dataPane = Box.createVerticalBox();
		JScrollPane scrollPane = new JScrollPane(_dataPane);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Diese Methode erstellt generisch eine Ansicht der Daten.
	 *
	 * @param data darzustellende Daten
	 */
	public void setData(final Data data) {
		_debug.finer("data = " + data);
		_data = data;
		_dataPane.removeAll();
		if(data == null) {
			_dataPane.add(new JLabel("keine Daten"));
		}
		else {
			Box box = createBox(_data);
			box.setMaximumSize(new Dimension(box.getMaximumSize().width, box.getPreferredSize().height));
			_dataPane.add(box);
		}
		_dataPane.add(Box.createVerticalGlue());
		_dataPane.revalidate();
		_dataPane.repaint();
	}

	/**
	 * Diese Methode erstellt generisch eine Ansicht der Daten.
	 *
	 * @param resultData Ergebnisdatensatz, welcher die darzustellenden Daten enthält
	 */
	public void setResultData(final ResultData resultData) {
		final Data data = resultData.getData();
		_data = (data == null ? null : data.createModifiableCopy());
		_debug.finer("data = " + _data);
		_dataPane.removeAll();
		if(_data == null) {
			String label = resultData.getDataState().toString();
			_dataPane.add(new JLabel(label));
		}
		else {
			Box box = createBox(_data);
			box.setMaximumSize(new Dimension(box.getMaximumSize().width, box.getPreferredSize().height));
			_dataPane.add(box);
		}
		_dataPane.add(Box.createVerticalGlue());
		_dataPane.revalidate();
		_dataPane.repaint();
	}

	/**
	 * Hierüber kann bestimmt werden, ob die angezeigten Textfelder, etc. editierbar sind, oder nicht.
	 *
	 * @param editable gibt an, ob die angezeigten Komponenten editierbar sein sollen
	 */
	public void setEditable(final boolean editable) {
		_editable = editable;
	}

	/**
	 * Gibt die Daten zurück, die aktuell angezeigt werden.
	 *
	 * @return die aktuellen Daten
	 */
	public Data getData() {
		return _data;
	}

	/* ################ Private Methoden ################# */

	/**
	 * An dieser Stelle wird eine Komponente generisch zusammengestellt, die die übergebenen Daten darstellt.
	 *
	 * @param data die darzustellenden Daten
	 *
	 * @return die Daten darstellende Komponente
	 */
	private Box createBox(final Data data) {
		final Box box;
		if(data.isPlain()) {
			box = Box.createHorizontalBox();
			JLabel labelBox = new JLabel(data.getName() + ": ");
			box.add(Box.createRigidArea(new Dimension(5, 5)));
			final JLabel suffixBox = new JLabel(data.asTextValue().getSuffixText());

			JButton optionalButton = null;

			JComponent valueBox;
			if(data.getAttributeType() instanceof ReferenceAttributeType) {
				ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
				SystemObjectType objectType = att.getReferencedObjectType();
				final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
				if(objectType == null) {
					DataModel configuration = _connection.getDataModel();
					types.add(configuration.getType("typ.konfigurationsObjekt"));
					types.add(configuration.getType("typ.dynamischesObjekt"));
				}
				else {
					types.add(objectType);
				}

				final JTextField textBox;

				if(data.isDefined()) {
					textBox = new JTextField(data.asTextValue().getValueText());
				}
				else {
					textBox = new JTextField(_undefinedString);
					textBox.setBackground(_backgroundUndefinedValue);
				}

				textBox.setEditable(false);
				valueBox = textBox;
				if(_editable) {
					final JButton changeButton = new JButton("Referenz ändern");
					changeButton.addActionListener(
							new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									PreselectionDialog dialog = new PreselectionDialog("Objektauswahl", changeButton, null, types);
									dialog.setMaximumSelectedObjects(1);
									dialog.setMaximumSelectedAspects(0);
									dialog.setMaximumSelectedAttributeGroups(0);
									try {
										dialog.setSelectedObject(data.asReferenceValue().getSystemObject());
									}
									catch(Exception ignore) {
									}
									if(dialog.show()) {
										data.asReferenceValue().setSystemObject((SystemObject)dialog.getSelectedObjects().get(0));
										if(data.isDefined()) {
											textBox.setText(data.asTextValue().getValueText());
											suffixBox.setText(data.asTextValue().getSuffixText());
											textBox.setBackground(_backgroundColorDefinedValue);
										}
										else {
											textBox.setText(_undefinedString);
											textBox.setBackground(_backgroundUndefinedValue);
										}
									}
								}
							}
					);
					optionalButton = changeButton;
				}
			}
			else if(data.getAttributeType() instanceof IntegerAttributeType) {
				if(_editable) {		// ist true -> ComboBox
					IntegerAttributeType att = (IntegerAttributeType)data.getAttributeType();
					final JComboBox comboBox = new JComboBox();
					List states = att.getStates();
					for(Iterator statesIterator = states.iterator(); statesIterator.hasNext();) {
						IntegerValueState state = (IntegerValueState)statesIterator.next();
						comboBox.addItem(state.getName());
					}
					IntegerValueRange range = att.getRange();
					if(range != null) {
						comboBox.setEditable(true);
						final long unscaledMinimum = range.getMinimum();
						final long unscaledMaximum = range.getMaximum();
						final double conversionFactor = range.getConversionFactor();
						comboBox.addItem(getScaledValueText(unscaledMinimum, conversionFactor));
						comboBox.addItem(getScaledValueText(unscaledMaximum, conversionFactor));
					}

					// Wert, der gesetzt werden muss
					if(data.isDefined()) {
						// Der Wert des Attributes ist definiert
						comboBox.setSelectedItem(data.asTextValue().getValueText());
					}
					else {
						// Damit wenn kein Range vorhanden ist, eine Eingabe möglich ist
						comboBox.setEditable(true);
						comboBox.getEditor().getEditorComponent().setBackground(_backgroundUndefinedValue);
						comboBox.setSelectedItem(_undefinedString);
					}
					comboBox.addItem(_defaultValueString);

					comboBox.addFocusListener(
							new FocusAdapter() {
								public void focusLost(FocusEvent e) {
									comboBoxAttributeModified(comboBox, suffixBox, data);
								}
							}
					);
					comboBox.addActionListener(
							new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									comboBoxAttributeModified(comboBox, suffixBox, data);
								}
							}
					);

					valueBox = comboBox;
				}
				else {			// ist false -> nicht editierbares Textfeld
					final JTextField textField = new JTextField();
					textField.setText(data.asTextValue().getValueText());
					textField.setEditable(false);
					valueBox = textField;
				}
			}
			else {
				final JTextField textBox;

				if(data.isDefined()) {
					textBox = new JTextField(data.asTextValue().getValueText());
				}
				else {
					textBox = new JTextField(_undefinedString);
					textBox.setBackground(_backgroundUndefinedValue);
				}

				textBox.setEditable(_editable);	   // gibt an, ob dieses Feld die Daten nur anzeigt
				textBox.addFocusListener(
						new FocusAdapter() {
							public void focusLost(FocusEvent e) {
								//						System.out.println("ParameterEditor$EditorFrame.focusLost+");
								textBoxAttributeModified(textBox, suffixBox, data);
								//						System.out.println("ParameterEditor$EditorFrame.focusLost-");
							}
						}
				);
				textBox.addActionListener(
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//						System.out.println("ParameterEditor$EditorFrame.actionPerformed+");
								textBoxAttributeModified(textBox, suffixBox, data);
								//						System.out.println("ParameterEditor$EditorFrame.actionPerformed-");
							}
						}
				);
				valueBox = textBox;
			}

			box.add(labelBox);
			box.add(Box.createRigidArea(new Dimension(5, 5)));
			box.add(valueBox);
			box.add(Box.createRigidArea(new Dimension(5, 5)));
			box.add(suffixBox);
			if(optionalButton != null) {
				box.add(Box.createRigidArea(new Dimension(5, 5)));
				box.add(optionalButton);
			}
			box.add(Box.createHorizontalGlue());
		}
		else {
			box = Box.createVerticalBox();
			box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(data.getName() + ": "), new EmptyBorder(5, 20, 5, 5)));
			if(data.isArray()) {
				final Box arrayHeaderBox = Box.createHorizontalBox();
				arrayHeaderBox.add(new JLabel("Arraygröße: "));
				Data.Array array = data.asArray();
				if(array.isCountVariable() && _editable) {
					SpinnerNumberModel spinnerModel = new SpinnerNumberModel(array.getLength(), 0, Integer.MAX_VALUE, 1);
					if(array.isCountLimited()) spinnerModel.setMaximum(new Integer(array.getMaxCount()));
					final JSpinner arraySizeBox = new JSpinner(spinnerModel);
					arraySizeBox.addChangeListener(
							new ChangeListener() {
								public void stateChanged(ChangeEvent e) {
									data.asArray().setLength(((Number)arraySizeBox.getValue()).intValue());
									box.removeAll();
									box.add(arrayHeaderBox);
									Iterator iterator = data.iterator();
									while(iterator.hasNext()) {
										Data subData = (Data)iterator.next();
										box.add(createBox(subData));
									}
									box.revalidate();
									box.repaint();
								}
							}
					);
					arrayHeaderBox.add(arraySizeBox);
				}
				else {
					if(!array.isCountVariable()) {
						array.setLength(array.getMaxCount());
					}
					arrayHeaderBox.add(new JLabel(String.valueOf(array.getLength())));
				}
				arrayHeaderBox.add(Box.createHorizontalGlue());
				box.add(arrayHeaderBox);
			}
			Iterator iterator = data.iterator();
			while(iterator.hasNext()) {
				Data subData = (Data)iterator.next();
				box.add(createBox(subData));
			}
		}
		return box;
	}

	private String getScaledValueText(final long unscaledValue, double conversionFactor) {
		
		if(conversionFactor == 1) {
			return String.valueOf(unscaledValue);
		}
		else {
			int precision = 0;
			synchronized(_integerNumberFormat) {
				String formatted = _precisionTestNumberFormat.format(conversionFactor);
				int kommaPosition = formatted.lastIndexOf(',');
				if(kommaPosition >= 0) precision = formatted.length() - kommaPosition - 1;
				_integerNumberFormat.setMinimumFractionDigits(precision);
				_integerNumberFormat.setMaximumFractionDigits(precision);
				return _integerNumberFormat.format(unscaledValue * conversionFactor);
			}
		}
	}

	/**
	 * Überprüft, ob der eingegebene Wert auch ein zulässiger Wert ist.
	 *
	 * @param comboBox  Komponente, wo der neue Wert eingegeben wurde
	 * @param suffixBox das Feld mit dem Suffix
	 * @param data      die modifizierten Daten
	 */
	private void comboBoxAttributeModified(JComboBox comboBox, JLabel suffixBox, Data data) {
		String text = comboBox.getSelectedItem().toString();
		try {
			if(text.equals(_defaultValueString) || text.equals(_undefinedString)) {
				data.setToDefault();
			}
			else {
				data.asTextValue().setText(text);
			}

			if(!data.isDefined()) {
				comboBox.getEditor().getEditorComponent().setBackground(_backgroundUndefinedValue);
				comboBox.setSelectedItem(_undefinedString);
			}
			else {
				comboBox.setSelectedItem(data.asTextValue().getValueText());
				suffixBox.setText(data.asTextValue().getSuffixText());
				comboBox.getEditor().getEditorComponent().setBackground(_backgroundColorDefinedValue);
			}
		}
		catch(Exception ex) {
			_debug.error("In einem Dateneingabedialog (z.B. Parametereditor) wurde in einer Combobox ein Wert eingegeben, der nicht im gültigen Wertebereich des jeweiligen Attributtyps liegt.",ex);
		}
	}

	/**
	 * Überprüft, ob der eingegebene Wert auch ein zulässiger Wert ist.
	 *
	 * @param textBox   Komponente, wo der neue Wert eingegeben wurde
	 * @param suffixBox das Feld mit dem Suffix
	 * @param data      die modifizierten Daten
	 */
	private void textBoxAttributeModified(final JTextField textBox, final JLabel suffixBox, final Data data) {
		String text = textBox.getText();
		try {
			if(text.equals(_defaultValueString) || text.equals(_undefinedString)) {
				data.setToDefault();
			}
			else {
				data.asTextValue().setText(text);
			}

			if(!data.isDefined()) {
				textBox.setBackground(_backgroundUndefinedValue);
				textBox.setText(_undefinedString);
			}
			else {
				textBox.setText(data.asTextValue().getValueText());
				suffixBox.setText(data.asTextValue().getSuffixText());
				textBox.setBackground(_backgroundColorDefinedValue);
			}
		}
		catch(Exception ex) {
			// Wenn beim setzen des Textes ein Fehler Auftritt, dann wird der Wert auf "undefiniert" gesetzt
			// und der Benutzer muss sich Gedanken um den Wert machen
			textBox.setBackground(_backgroundUndefinedValue);
			textBox.setText(_undefinedString);
			_debug.error("In einem Dateneingabedialog (z.B. Payrametereditor) wurde in einem Textfeld ein Wert eingegeben, der nicht im gültigen Wertebereich des jeweiligen Attributtyps liegt." + ex);
		}
	}
}

/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.dataEditor;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.*;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Diese Klasse stellt einen Datensatz in strukturierter Form (entsprechend der Attributgruppe) in einem JPanel dar. Die einzelnen Attribute sind entsprechend
 * der Einschränkungen des Datenmodells editierbar. Bei Bedarf kann auch eine nicht editierbar Form gewählt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #DataEditorPanel(de.bsvrz.dav.daf.main.ClientDavInterface)
 * @see #setData(de.bsvrz.dav.daf.main.Data)
 * @see #setResultData(de.bsvrz.dav.daf.main.ResultData)
 */
public class DataEditorPanel extends AbstractEditorPanel {

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
	private static ImageIcon _iconAdd;

	private static ImageIcon _iconFolder;

	private static ImageIcon _iconAddFolder;

	private static ImageIcon _iconRemove;

	private static ImageIcon _iconCopy;

	static {
		_integerNumberFormat.setMinimumIntegerDigits(1);
		_integerNumberFormat.setMaximumIntegerDigits(999);
		_integerNumberFormat.setGroupingUsed(false);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		_precisionTestNumberFormat = new DecimalFormat("0.#", symbols);
		_precisionTestNumberFormat.setMaximumFractionDigits(999);

		_iconAdd = new ImageIcon(DataEditorPanel.class.getResource("add.png"));
		_iconFolder = new ImageIcon(DataEditorPanel.class.getResource("folder.png"));
		_iconAddFolder = new ImageIcon(DataEditorPanel.class.getResource("addFolder.png"));
		_iconRemove = new ImageIcon(DataEditorPanel.class.getResource("remove.png"));
		_iconCopy = new ImageIcon(DataEditorPanel.class.getResource("copy.png"));
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

	private boolean _overrideComplexityWarning = false;

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
	@Override
	public void setData(final Data data) {
		_debug.finer("data" , data);
		_data = data;
		_dataPane.removeAll();
		if(_data == null) {
			_dataPane.add(new JLabel("keine Daten"));
		}
		else {
			showData();
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
	@Override
	public void setResultData(final ResultData resultData) {
		final Data data = resultData.getData();
		_data = (data == null ? null : data.createModifiableCopy());
		_debug.finer("data" , _data);
		_dataPane.removeAll();
		if(_data == null) {
			String label = resultData.getDataState().toString();
			_dataPane.add(new JLabel(label));
		}
		else {
			showData();
		}
		_dataPane.add(Box.createVerticalGlue());
		_dataPane.revalidate();
		_dataPane.repaint();
	}

	private void showData() {
		int dataComplexity = getDataComplexity(_data);
		_debug.info("DataComplex", dataComplexity);
		if(!_overrideComplexityWarning && dataComplexity > 1000){
			_dataPane.add(createComplexityWarningPanel());
			return;
		}
		Box box = createBox(_data);
		box.setMaximumSize(new Dimension(box.getMaximumSize().width, box.getPreferredSize().height));
		_dataPane.add(box);
	}

	private Component createComplexityWarningPanel() {
		JPanel jPanel = new JPanel();
		jPanel.add(new JLabel("Der Datensatz ist sehr komplex und kann zu Problemen bei der Darstellung führen."));
		JButton button = new JButton("Trotzdem anzeigen");
		jPanel.add(button);
		jPanel.setPreferredSize(new Dimension(400, 400));
		button.addActionListener(new ActionListener() {
			                         @Override
			                         public void actionPerformed(final ActionEvent e) {
				                         _overrideComplexityWarning = true;
				                         _dataPane.removeAll();
				                         showData();
				                         _dataPane.revalidate();
				                         _dataPane.repaint();
			                         }
		                         });
		return jPanel;
	}

	private static int getDataComplexity(final Data data) {
		if(data.isPlain()){
			return 1;
		}
		int num = 1;
		for(Data subData : data) {
			num += getDataComplexity(subData);
		}
		return num;
	}

	/**
	 * Hierüber kann bestimmt werden, ob die angezeigten Textfelder, etc. editierbar sind, oder nicht.
	 *
	 * @param editable gibt an, ob die angezeigten Komponenten editierbar sein sollen
	 */
	@Override
	public void setEditable(final boolean editable) {
		_editable = editable;
	}

	/**
	 * Gibt die Daten zurück, die aktuell angezeigt werden.
	 *
	 * @return die aktuellen Daten
	 */
	@Override
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

			Collection<JButton> optionalButtons = new LinkedList<JButton>();

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

				textBox.addActionListener(
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setReferenceText(data, textBox, suffixBox);
							}
						});
				textBox.addFocusListener(new FocusAdapter() {
					                         @Override
					                         public void focusLost(final FocusEvent e) {
						                         setReferenceText(data, textBox, suffixBox);
					                         }
				                         });
				valueBox = textBox;
				if(_editable) {
					final JButton changeButton = new JButton(_iconFolder);
					styleIconButton(changeButton);
					changeButton.setToolTipText("Referenz ändern");
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
										refreshReferenceValue(data, textBox, suffixBox);
									}
								}
							}
					);
					optionalButtons.add(changeButton);
					if(att.isUndefinedAllowed()){
						final JButton removeButton = new JButton(_iconRemove);
						styleIconButton(removeButton);
						removeButton.setToolTipText("Eintrag entfernen");
						removeButton.addActionListener(
								new ActionListener() {
									@Override
									public void actionPerformed(final ActionEvent e) {
										data.asReferenceValue().setSystemObject(null);
										refreshReferenceValue(data, textBox, suffixBox);
									}
								}
						);
						optionalButtons.add(removeButton);
					}
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
			for(JButton optionalButton : optionalButtons) {
				box.add(Box.createRigidArea(new Dimension(5, 5)));
				box.add(optionalButton);
			}
			box.add(Box.createHorizontalGlue());
		}
		else {
			box = Box.createVerticalBox();
			box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(data.getName() + ": "), new EmptyBorder(5, 20, 5, 5)));
			if(data.isArray()) {
				final JPanel arrayHeaderBox = new JPanel(new FlowLayout(FlowLayout.LEFT,3,0));
				arrayHeaderBox.add(new JLabel("Arraygröße:"));
				arrayHeaderBox.setOpaque(false);
				final Data.Array array = data.asArray();
				if(array.isCountVariable() && _editable) {
					SpinnerNumberModel spinnerModel = new SpinnerNumberModel(array.getLength(), 0, 9999, 1);
					if(array.isCountLimited()) spinnerModel.setMaximum(array.getMaxCount());
					final JSpinner arraySizeBox = new JSpinner(spinnerModel);
					arraySizeBox.addChangeListener(
							new ChangeListener() {
								public void stateChanged(ChangeEvent e) {
									setArrayLength(data.asArray(), ((Number)arraySizeBox.getValue()).intValue());
									box.removeAll();
									box.add(arrayHeaderBox);
									for(int i = 0; i < array.getLength(); i++) {
										Data d = array.getItem(i);
										box.add(createBoxWithArrayButtons(d, i, array, arraySizeBox));
									}
									box.revalidate();
									box.repaint();
								}
							}
					);
					arrayHeaderBox.add(arraySizeBox);
					final JButton addEntryButton = new JButton(_iconAdd);
					addEntryButton.addActionListener(
								new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										setArrayLength(array, array.getLength()+1);
										for(int i = array.getLength() - 2; i >= 0; i--) {
											copyData(array.getItem(i), array.getItem(i+1));
										}
										array.getItem(0).setToDefault();
										arraySizeBox.setValue(((Number)arraySizeBox.getValue()).intValue() + 1);
									}
								});

					styleIconButton(addEntryButton);
					arrayHeaderBox.add(addEntryButton);
					if(data.getAttributeType() instanceof ReferenceAttributeType) {
						final JButton addMultipleEntriesButton = new JButton(_iconAddFolder);
						addMultipleEntriesButton.setToolTipText("Objekte hinzufügen");
						ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
						SystemObjectType objectType = att.getReferencedObjectType();
						final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
						final String title;
						if(objectType == null) {
							DataModel configuration = _connection.getDataModel();
							types.add(configuration.getType("typ.konfigurationsObjekt"));
							types.add(configuration.getType("typ.dynamischesObjekt"));
							title = "Beliebige Objekte hinzufügen";
						}
						else {
							types.add(objectType);
							title = "Objekte vom Typ " + objectType.getNameOrPidOrId() + " hinzufügen";
						}
						addMultipleEntriesButton.addActionListener(
								new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										PreselectionDialog dialog = new PreselectionDialog(title, addMultipleEntriesButton, null, types);
										dialog.setMaximumSelectedAspects(0);
										dialog.setMaximumSelectedAttributeGroups(0);
										if(array.isCountLimited()) {
											final int objectsToChoose = array.getMaxCount() - array.getLength();
											if(objectsToChoose <= 0)
											{
												JOptionPane.showMessageDialog(addMultipleEntriesButton, "Das Array kann keine zusätzlichen Objekte mehr aufnehmen.");
												return;
											}
											dialog.setMaximumSelectedObjects(objectsToChoose);
										}
										if(dialog.show()) {

											final List<SystemObject> objects = dialog.getSelectedObjects();

											final int oldLength = array.getLength();
											setArrayLength(array, oldLength + objects.size());

											//Objekte nach hinten verschieben
											for(int i = array.getLength() - 1 - objects.size(); i >= 0; i--) {
												copyData(array.getItem(i), array.getItem(i + objects.size()));
											}


											for(int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
												final SystemObject object = objects.get(i);
												array.asReferenceArray().getReferenceValue(i).setSystemObject(object);
											}
											arraySizeBox.setValue(array.getLength());
										}
									}
								}
						);
						styleIconButton(addMultipleEntriesButton);
						arrayHeaderBox.add(addMultipleEntriesButton);
					}

					arrayHeaderBox.add(Box.createHorizontalGlue());
					box.add(arrayHeaderBox);
					final Data.Array arr = data.asArray();
					for(int i = 0; i < arr.getLength(); i++) {
						Data d = arr.getItem(i);
						box.add(createBoxWithArrayButtons(d, i, arr, arraySizeBox));
					}
				}
				else {
					if(!array.isCountVariable()) {
						setArrayLength(array, array.getMaxCount());
					}
					arrayHeaderBox.add(new JLabel(String.valueOf(array.getLength())));
					arrayHeaderBox.add(Box.createHorizontalGlue());
					box.add(arrayHeaderBox);
					final Data.Array arr = data.asArray();
					for(int i = 0; i < arr.getLength(); i++) {
						Data d = arr.getItem(i);
						box.add(createBox(d));
					}
				}
			}
			else {
				Iterator iterator = data.iterator();
				while(iterator.hasNext()) {
					Data subData = (Data)iterator.next();
					box.add(createBox(subData));
				}
			}
		}
		return box;
	}

	protected void setReferenceText(final Data data, final JTextField textBox, final JLabel suffixBox) {
		Data.ReferenceValue referenceValue = data.asReferenceValue();
		try {
			referenceValue.setText(textBox.getText());
		}
		catch(Exception ignored){
			referenceValue.setSystemObject(null);
		}
		refreshReferenceValue(data, textBox, suffixBox);
	}

	private void refreshReferenceValue(final Data data, final JTextField textBox, final JLabel suffixBox) {
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

	/**
	 * Erstellt eine Box für Daten in einem Array, bei denen zusätzlich Buttons für Kopieren, Löschen, einfügen usw. vorhanden sind
	 * @param data Daten-Objekt für das die Box erstellt werden soll
	 * @param index Index im Array
	 * @param array Array
	 * @param scrollbox Steuerelement, das für die Arrayeinträge zuständig ist
	 * @return Die erstellte Box
	 */
	private Box createBoxWithArrayButtons(final Data data, final int index, final Data.Array array, final JSpinner scrollbox) {
		final Box box;
		box = createBox(data);

		final JPanel contextPanel = new JPanel();
		contextPanel.setLayout(new FlowLayout(FlowLayout.LEFT,3,0));
		contextPanel.setPreferredSize(new Dimension(100,22));
		contextPanel.setOpaque(false);

		// Button zum einfügen von Elementen
		final JButton insertButton = new JButton(_iconAdd);
		styleIconButton(insertButton);
		insertButton.setToolTipText("Eintrag einfügen");
		insertButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setArrayLength(array, array.getLength()+1);
						for(int i = array.getLength() - 2; i > index; i--) {
							copyData(array.getItem(i), array.getItem(i+1));
						}
						array.getItem(index+1).setToDefault();
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() + 1);
					}
				}
		);
		contextPanel.add(insertButton);

		// Button zum Einfügen von mehreren (Referenz-)Elementen
		if(data.getAttributeType() instanceof ReferenceAttributeType) {
			final JButton insertMultipleButton = new JButton(_iconAddFolder);
			styleIconButton(insertMultipleButton);
			insertMultipleButton.setToolTipText("Objekte hinzufügen");
			insertMultipleButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
							SystemObjectType objectType = att.getReferencedObjectType();
							final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
							final String title;
							if(objectType == null) {
								DataModel configuration = _connection.getDataModel();
								types.add(configuration.getType("typ.konfigurationsObjekt"));
								types.add(configuration.getType("typ.dynamischesObjekt"));
								title = "Beliebige Objekte hinzufügen";
							}
							else {
								types.add(objectType);
								title = "Objekte vom Typ " + objectType.getNameOrPidOrId() + " hinzufügen";
							}
							PreselectionDialog dialog = new PreselectionDialog(title, insertMultipleButton, null, types);
							dialog.setMaximumSelectedAspects(0);
							dialog.setMaximumSelectedAttributeGroups(0);
							if(array.isCountLimited()) {
								final int objectsToChoose = array.getMaxCount() - array.getLength();
								if(objectsToChoose <= 0) {
									JOptionPane.showMessageDialog(insertMultipleButton, "Das Array kann keine zusätzlichen Objekte mehr aufnehmen.");
									return;
								}
								dialog.setMaximumSelectedObjects(objectsToChoose);
							}
							if(dialog.show()) {

								final List<SystemObject> objects = dialog.getSelectedObjects();

								final int oldLength = array.getLength();
								setArrayLength(array, oldLength + objects.size());

								//Objekte nach hinten verschieben
								for(int i = array.getLength() - 1 - objects.size(); i > index; i--) {
									copyData(array.getItem(i), array.getItem(i + objects.size()));
								}


								for(int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
									final SystemObject object = objects.get(i);
									final int newPosition = i + index + 1;
									array.asReferenceArray().getReferenceValue(newPosition).setSystemObject(object);
								}
								scrollbox.setValue(array.getLength());
							}
						}
					}
			);
			contextPanel.add(insertMultipleButton);
		}

		// Button zum Klonen von Einträgen
		final JButton cloneButton = new JButton(_iconCopy);
		styleIconButton(cloneButton);
		cloneButton.setToolTipText("Eintrag duplizieren");
		cloneButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setArrayLength(array, array.getLength()+1);
						for(int i = array.getLength() - 2; i >= index; i--) {
							copyData(array.getItem(i), array.getItem(i+1));
						}
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() + 1);
					}
				}
		);
		contextPanel.add(cloneButton);

		//Button zum Löschen von Einträgen
		final JButton removeButton = new JButton(_iconRemove);
		styleIconButton(removeButton);
		removeButton.setToolTipText("Eintrag entfernen");
		removeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for(int i = index; i < array.getLength() - 1; i++) {
							copyData(array.getItem(i+1), array.getItem(i));
						}
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() - 1);
					}
				}
		);
		contextPanel.add(removeButton);
		box.add(contextPanel);
		return box;
	}

	/**
	 * Hilfsmethode zum setzen einer Arraylänge. Damit keine unschönen Runtime-Exceptions entstehen wird hier der Bereich noch einmal geprüft.
	 * @param array Array
	 * @param newLength neue Länge
	 */
	private void setArrayLength(final Data.Array array, final int newLength) {
		if(array.isCountVariable() && array.getMaxCount() > 0){
			if(array.getMaxCount() < newLength || newLength < 0){
				JOptionPane.showMessageDialog(this, "Array-Länge " + newLength + " außerhalb des gültigen Bereichs: 0 - " + array.getMaxCount());

				
			}
		}
		array.setLength(newLength);
	}

	private void styleIconButton(JButton button){
		button.setPreferredSize(new Dimension(22,22));
		//button.setBorderPainted(true);
		//button.setBackground(new Color(0, true));
		button.setContentAreaFilled(false);
	}

	/**
	 * 	Kopiert die Inhalte von einem Data-Objekt zu einem anderen.
	 *
	 * @param from Quelle
	 * @param to Ziel
	 */
	private static void copyData(final Data from, final Data to) {
		if(from.isPlain()) {
			if(from.getAttributeType() instanceof IntegerAttributeType) {
				to.asUnscaledValue().set(from.asUnscaledValue().longValue());
			}
			else if(from.getAttributeType() instanceof DoubleAttributeType) {
				to.asUnscaledValue().set(from.asUnscaledValue().doubleValue());
			}
			else if(from.getAttributeType() instanceof TimeAttributeType) {
				to.asTimeValue().setMillis(from.asTimeValue().getMillis());
			}
			else if(from.getAttributeType() instanceof ReferenceAttributeType) {
				to.asReferenceValue().setSystemObject(from.asReferenceValue().getSystemObject());
			}
			else {
				to.asTextValue().setText(from.asTextValue().getText());
			}
		}
		else if(from.isArray()) {
			final Data.Array toArray = to.asArray();
			final Data.Array fromArray = from.asArray();
			toArray.setLength(fromArray.getLength());
			for(int i = 0; i < toArray.getLength(); i++) {
				copyData(fromArray.getItem(i), toArray.getItem(i));
			}
		}
		else if(from.isList()) {
			Iterator toIterator = to.iterator();
			Iterator fromIterator = from.iterator();
			while(toIterator.hasNext() && fromIterator.hasNext()) {
				copyData((Data)fromIterator.next(), (Data)toIterator.next());
			}
		}
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
			_debug.error("In einem Dateneingabedialog (z.B. Parametereditor) wurde in einem Textfeld ein Wert eingegeben, der nicht im gültigen Wertebereich des jeweiligen Attributtyps liegt." + ex);
		}
	}
}

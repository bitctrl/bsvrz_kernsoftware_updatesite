/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.plugins.parameditor;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.dataEditor.AbstractEditorPanel;
import de.bsvrz.pat.sysbed.dataEditor.DataEditorPanel;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.kappich.sys.funclib.json.Json;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse implementiert einen Dialog zum editieren von Parametern. Die Klasse kann als eigenständige Applikation gestartet oder in andere Applikationen
 * integriert werden. Als eigenständige Applikation werden die Aufrufargumente -objekt=... und -atg=... unterstützt mit denen spezifiziert werden kann, für
 * welches Objekt und für welche Attributgruppe ein Parameterdatensatz angezeigt werden soll.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ParameterEditor implements StandardApplication {

	/** Logger für Debug-Ausgaben */
	private static Debug _debug;

	/** Pid des via Aufrufargument angegebenen Objekts. */
	private String _objectPid;

	/** Pid der via Aufrufargument angegebenen Attributgruppe. */
	private String _atgPid;

	/**
	 * Wird zum Start der Applikation aufgerufen.
	 *
	 * @param args Aufrufargumente der Applikation
	 */
	public static void main(String[] args) {
		StandardApplicationRunner.run(new ParameterEditor(), args);
	}

	public void parseArguments(ArgumentList argumentList) {
		_debug = Debug.getLogger();
		_debug.fine("argumentList = " + argumentList);
		_objectPid = argumentList.fetchArgument("-objekt=").asString();
		_atgPid = argumentList.fetchArgument("-atg=").asString();
	}

	public void initialize(final ClientDavInterface connection) throws Exception {
		_debug.fine("connection = " + connection);
		final DataModel configuration = connection.getDataModel();
		final SystemObject object = configuration.getObject(_objectPid);
		final AttributeGroup atg = configuration.getAttributeGroup(_atgPid);
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						try {
							JFrame.setDefaultLookAndFeelDecorated(true);
							JDialog.setDefaultLookAndFeelDecorated(true);
							short simulationVariant = -1;
							EditorFrame editor = new EditorFrame(connection, object, atg, simulationVariant);
							editor.setWindowCloseOperation(JFrame.EXIT_ON_CLOSE);
							editor.start();
						}
						catch(Exception e) {
							e.printStackTrace();
							//throw new RuntimeException(e);
							System.exit(0);
						}
					}
				}
		);
	}

	/** Der Standardkonstruktor wird für den Aufruf durch die {@link #main(String[]) main} Methode benötigt. */
	private ParameterEditor() {
	}

	/**
	 * Konstruktor, um den Parametereditor von einer anderen Applikation aus zu starten. Wird als Simulationsvariante -1 angegeben, wird sie nicht weiter
	 * beachtet.
	 *
	 * @param connection        Verbindung zum Datenverteiler
	 * @param object            anzuzeigendes Objekt
	 * @param attributeGroup    anzuzeigende Attributgruppe
	 * @param simulationVariant die Simulationsvariante
	 */
	public ParameterEditor(ClientDavInterface connection, SystemObject object, AttributeGroup attributeGroup, short simulationVariant) {
		// Standardkonstruktor wird für die main-Methode verwendet -> keinen Standardkonstruktor verwenden
		_debug = Debug.getLogger();
		try {
			EditorFrame editor = new EditorFrame(connection, object, attributeGroup, simulationVariant);
			editor.setWindowCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			editor.start();
		}
		catch(Exception e) {
			_debug.error("Fehler bei der Initialisierung des Parametereditors", e);
		}
	}


	private static class EditorFrame {

		private final ClientDavInterface _connection;

		private SystemObject _object;

		private AttributeGroup _atg;

		private final JFrame _frame;

		private final Aspect _receiveAspect;

		private final Aspect _sendAspect;

		private final AbstractEditorPanel _editorPanel;

		private final JButton _sendButton;

		private JButton _actualDataButton;

		private ResultData _receivedResultData = null;

		private List<Aspect> _neededAspects;

		private boolean _usePreselectionDialog;

		private DataDescription _receiveDataDescription;

		private DataDescription _sendDataDescription;

		private final short _simulationVariant;

		private JLabel _labelSourceObject = null;

		private JLabel _labelSourceType = null;

		private JButton _editSourceButton;

		private boolean _newParametrisationAvailable = false;

		private ParameterEditorReceiverSender _parameterEditorReceiverSender;

		private ParameterControllerInterface _parameterController;

		PreselectionDialog askForObjectAndAttributeGroup(Component parentComponent) {
			final DataModel configuration = _connection.getDataModel();
			PreselectionDialog dialog = null;
			PreselectionListsFilter listFilter = new PreselectionListsFilter() {
				public List applyFilter(int whichList, List list) {
					final List<SystemObject> filteredList = new ArrayList<SystemObject>(list.size());
					for(Iterator iterator = list.iterator(); iterator.hasNext();) {
						SystemObject object = (SystemObject)iterator.next();
						if(object.getName() == null || object.getName().equals("")) continue;
						if(whichList == PreselectionListsFilter.ATTRIBUTEGROUP_LIST) {
							if(!((AttributeGroup)object).getAspects().containsAll(_neededAspects)) continue;
						}
						filteredList.add(object);
					}
					return filteredList;
				}
			};
			dialog = new PreselectionDialog(
					"Zu parametrierende Attributgruppe und Objekt auswählen", parentComponent, listFilter, configuration.getType("typ.konfigurationsObjekt")
			);
			dialog.setMinimumSelectedAttributeGroups(1);
			dialog.setMaximumSelectedAttributeGroups(1);
			dialog.setMaximumSelectedObjects(1);
			dialog.setMaximumSelectedAspects(0);
			if(_atg != null) dialog.setSelectedAttributeGroup(_atg);
			if(_object != null) dialog.setSelectedObject(_object);
			dialog.show();
			return dialog;
		}

		public EditorFrame(ClientDavInterface connection, SystemObject object, AttributeGroup atg, short simulationVariant) {
			_connection = connection;
			final DataModel configuration = connection.getDataModel();
			// Simulationsvariante setzen, bevor setSelection aufgerufen wird. Diese wird dort gebraucht.
			_simulationVariant = simulationVariant;

			try {
				// prüfen, ob die neue Parametrierung vorhanden ist
				Class.forName("de.bsvrz.puk.param.lib.MethodenBibliothek");
				final Class<ParameterControllerInterface> parameterControllerClass = (Class<ParameterControllerInterface>)Class.forName("pat.paramedi.ParameterController");
				_parameterController = parameterControllerClass.newInstance();
				_debug.info("Die neue Parametrierung de.bsvrz.puk.param.lib.MethodenBibliothek wird verwendet.");
				final ParameterChangeInformer parameterChangeInformer = new ParameterChangeInformation(this);
				_parameterController.setConnection(connection);
				_parameterController.setParameterChangeInformer(parameterChangeInformer);
				_newParametrisationAvailable = true;
			}
			catch(Exception ex) {
				_debug.info("Die neue Parametrierung de.bsvrz.puk.param.lib.MethodenBibliothek ist nicht vorhanden oder kann nicht verwendet werden!");
				_newParametrisationAvailable = false;
				// alte Parametrierung nutzen
				_parameterEditorReceiverSender = new ParameterEditorReceiverSender(this);
			}

			setSelection(object, atg);

			try {
				// Wenn die Klasse PreselectionDialog gefunden wird, dann soll sie auch zur Objektauswahl benutzt werden
				Class.forName("de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog");
				_usePreselectionDialog = true;
			}
			catch(ClassNotFoundException e) {
				_usePreselectionDialog = false;
			}


			_receiveAspect = configuration.getAspect("asp.parameterSoll");
			_sendAspect = configuration.getAspect("asp.parameterVorgabe");
			_neededAspects = new LinkedList<Aspect>();
			_neededAspects.add(_receiveAspect);
			_neededAspects.add(_sendAspect);

			if(_object == null || _atg == null) {
				if(!_usePreselectionDialog) {
					if(_object == null) {
						throw new IllegalArgumentException("Kein gültiges Objekt angegeben");
					}
					else {
						throw new IllegalArgumentException("Keine gültige Attributgruppe angegeben");
					}
				}

				PreselectionDialog dialog = askForObjectAndAttributeGroup(null);
				if(dialog.isOkButtonPressed()) {
					setSelection((SystemObject)dialog.getSelectedObjects().get(0), (AttributeGroup)dialog.getSelectedAttributeGroups().get(0));
				}
				else {
					throw new IllegalArgumentException("Objekt- und Attributgruppenauswahl wurde abgebrochen");
				}
			}

			createDataDescriptions();

			_frame = new JFrame("ParameterEditor") {
				public Dimension getPreferredSize() {
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					Dimension preferredSize = super.getPreferredSize();
					Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
					int visibleScreenHeight = screenSize.height - insets.bottom - insets.top;
					int visibleScreenWidth = screenSize.width - insets.left - insets.right;
					if(preferredSize.height > visibleScreenHeight) {
						preferredSize.height = visibleScreenHeight;
						JScrollPane scrollPane = new JScrollPane();   // wird nur für die Breite der ScrollBar benötigt
						preferredSize.width += scrollPane.getVerticalScrollBar().getSize().width;
					}
					if(preferredSize.width > visibleScreenWidth) {
						preferredSize.width = visibleScreenWidth;
					}
					return preferredSize;
				}
			};

			// Header erstellen
			final Box headerPane = Box.createHorizontalBox();
			headerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			// Auswahl-Feld dem Header hinzufügen
			headerPane.add(createSelectionComponent());
			headerPane.add(Box.createHorizontalStrut(10));
			headerPane.add(createSourceComponent());
			headerPane.add(Box.createHorizontalStrut(10));
			headerPane.add(Box.createHorizontalGlue());

			_editorPanel = new DataEditorPanel(connection);
			_editorPanel.setEditable(true);

			_frame.getContentPane().add(headerPane, BorderLayout.NORTH);
			_frame.getContentPane().add(_editorPanel, BorderLayout.CENTER);

			Box buttonPane = Box.createHorizontalBox();
			_actualDataButton = new JButton("aktueller Datensatz");
			_actualDataButton.setEnabled(false);
			_actualDataButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							setResultData(_receivedResultData);
						}
					}
			);
			JButton createButton = new JButton("Datensatz erzeugen");
			createButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateData(_connection.createData(_atg));
						}
					}
			);

			final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

			JButton copyJson = new JButton("Kopieren");
			copyJson.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							final Data data = _editorPanel.getData();
							Object json = JsonSerializer.serializeData(data);
							systemClipboard.setContents(
									new StringSelection(Json.getInstance().writeObject(json)), new ClipboardOwner() {
										@Override
										public void lostOwnership(final Clipboard clipboard, final Transferable contents) {

										}
									}
							);
						}
					}
			);

			final JButton pasteJson = new JButton("Einfügen");
			pasteJson.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								Data tmp = _editorPanel.getData();
								if(tmp == null){
									tmp = _connection.createData(_atg);
								}
								Data d = tmp.createModifiableCopy();
								String data = (String) systemClipboard.getData(DataFlavor.stringFlavor);
								Object json = Json.getInstance().readObject(data);
								JsonSerializer.deserializeData(json, d);
								updateData(d);
							}
							catch(Exception e1){
								e1.printStackTrace();
								String message = e1.getMessage();
								if(message != null) {
									JOptionPane.showMessageDialog(_frame, message);
								}
							}
						}
					}
			);
			
			systemClipboard.addFlavorListener(new FlavorListener() {
				                                  @Override
				                                  public void flavorsChanged(final FlavorEvent e) {
					                                  pasteJson.setEnabled(Arrays.asList(systemClipboard.getAvailableDataFlavors()).contains(DataFlavor.stringFlavor));
				                                  }
			                                  });
			

			JButton deleteButton = new JButton("Datensatz löschen");
			deleteButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateData(null);
						}
					}
			);
			_sendButton = new JButton("Senden");
			if(!_newParametrisationAvailable) {
				_sendButton.setEnabled(false);
			}

			_sendButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								sendCurrentData();
							}
							catch(Exception ex) {
								_debug.error("Fehler beim Versand des Datensatzes", ex);
								ex.printStackTrace();
								JOptionPane.showMessageDialog(_frame, ex.toString().substring(0,200), "Fehler beim Versand des Datensatzes", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
			);


			buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPane.add(_actualDataButton);
			buttonPane.add(Box.createHorizontalStrut(10));
			buttonPane.add(createButton);
			buttonPane.add(Box.createHorizontalStrut(10));
			buttonPane.add(deleteButton);
			buttonPane.add(Box.createHorizontalStrut(10));
			buttonPane.add(copyJson);
			buttonPane.add(Box.createHorizontalStrut(10));
			buttonPane.add(pasteJson);

			buttonPane.add(Box.createHorizontalStrut(10));
			buttonPane.add(_sendButton);
			_frame.getContentPane().add(buttonPane, BorderLayout.SOUTH);


			_frame.addWindowListener(
					new WindowAdapter() {
						public void windowClosing(WindowEvent event) {
							unsubscribe();
						}

						/** Invoked when a window has been opened. */
						public void windowOpened(WindowEvent e) {
							super.windowOpened(e);
						}

						/** Invoked when a window is activated. */
						public void windowActivated(WindowEvent e) {
							super.windowActivated(e);
						}

						/**
						 * Invoked when the Window is set to be the focused Window, which means that the Window, or one of its subcomponents, will receive keyboard events.
						 *
						 * @since 1.4
						 */
						public void windowGainedFocus(WindowEvent e) {
							super.windowGainedFocus(e);
						}
					}
			);
		}

		/**
		 * Erstellt eine Swing-Komponente zur Anzeige der Auswahl für die Parametrierung.
		 *
		 * @return Swing-Komponente, die die Auswahl der Parametrierung anzeigt
		 */
		private JComponent createSelectionComponent() {
			// Auswahl-Box erstellen
			final Box selectionInfoPane = Box.createVerticalBox();
			final JLabel objectLabel = new JLabel("Objekt: " + _object.getNameOrPidOrId());
			selectionInfoPane.add(objectLabel);
			selectionInfoPane.add(Box.createVerticalStrut(10));
			final JLabel atgLabel = new JLabel("Attributgruppe: " + _atg.getNameOrPidOrId());
			selectionInfoPane.add(atgLabel);
			selectionInfoPane.add(Box.createVerticalStrut(10));

			final Box selectionPane = Box.createHorizontalBox();
			selectionPane.setBorder(BorderFactory.createTitledBorder("Auswahl"));
			selectionPane.add(selectionInfoPane);

			if(_usePreselectionDialog) {
				selectionPane.add(Box.createHorizontalStrut(10));
				final JButton changeSelectionButton = new JButton("Auswahl ändern");
				selectionPane.add(changeSelectionButton);
				selectionPane.add(Box.createHorizontalStrut(10));
				changeSelectionButton.addActionListener(
						new ActionListener() {
							public void actionPerformed(ActionEvent event) {
								PreselectionDialog dialog = askForObjectAndAttributeGroup(selectionPane);
								if(dialog.isOkButtonPressed()) {
									unsubscribe();
									setSelection((SystemObject)dialog.getSelectedObjects().get(0), (AttributeGroup)dialog.getSelectedAttributeGroups().get(0));
									// neue DataDescriptions erzeugen
									createDataDescriptions();

									objectLabel.setText("Objekt: " + _object.getNameOrPidOrId());
									atgLabel.setText("Attributgruppe: " + _atg.getNameOrPidOrId());
									refreshSourcePanel();
									subscribe();
								}
							}
						}
				);
			}

			return selectionPane;
		}

		/**
		 * Erstellt eine Swing-Komponente zur Anzeige der Quelle der ausgewählten Objekt/Attributgruppe-Kombination.
		 *
		 * @return eine Swing-Komponente zur Anzeige der Quelle
		 */
		private JComponent createSourceComponent() {
			final Box sourcePane = Box.createHorizontalBox();
			// prüfen, ob die neue Parametrierung vorhanden ist
			if(_newParametrisationAvailable) {
				sourcePane.setBorder(BorderFactory.createTitledBorder("Quelle"));
				final SystemObject sourceObject = _parameterController.getSourceObject();
				if(sourceObject != null) {
					// Panel zusammensetzen
					final Box sourceInfoPane = Box.createVerticalBox();
					_labelSourceObject = new JLabel();
					sourceInfoPane.add(_labelSourceObject);
					sourceInfoPane.add(Box.createVerticalStrut(10));
					_labelSourceType = new JLabel();
					sourceInfoPane.add(_labelSourceType);
					sourceInfoPane.add(Box.createVerticalStrut(10));

					sourcePane.add(sourceInfoPane);
					sourcePane.add(Box.createHorizontalStrut(10));

					// Button hinzufügen
					_editSourceButton = new JButton("Quelle editieren");
					sourcePane.add(_editSourceButton);
					sourcePane.add(Box.createHorizontalStrut(10));
					_editSourceButton.addActionListener(
							new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									new ParameterEditor(_connection, sourceObject, _atg, _simulationVariant);
								}
							}
					);

					// Labels mit Daten füllen
					refreshSourcePanel();
				}
			}

			return sourcePane;
		}

		/** Falls die Anzeige der Quelle aktualisiert werden soll, muss diese Methode aufgerufen werden. */
		private void refreshSourcePanel() {
			// prüfen, ob die neue Parametrierung vorhanden ist
			if(_newParametrisationAvailable) {
				_parameterController.actualizeSource();
			}
		}

		/**
		 * Setzt den Text des Quellenobjekts.
		 *
		 * @param text Text des Quellenobjekts
		 */
		public void setSourceObject(final String text) {
			_labelSourceObject.setText("Objekt: " + text);
		}

		/**
		 * Setzt den Text des Quellentyps.
		 *
		 * @param text Text des Quellentyps
		 */
		public void setSourceType(final String text) {
			_labelSourceType.setText("Quellentyp: " + text);
		}

		/**
		 * Enabled oder disabled die Schaltfläche zum editieren des Quellenobjekts.
		 *
		 * @param enable gibt an, ob die Schaltfläche aktiviert sein soll, oder nicht
		 */
		public void showSourceButton(final boolean enable) {
			String toolTipText = null;
			if(!enable) {
				toolTipText = "Auswahl-Objekt und Quell-Objekt sind identisch.";
			}
			else {
				toolTipText = "";
			}
			_editSourceButton.setEnabled(enable);
			_editSourceButton.setToolTipText(toolTipText);
		}

		/** Erstellt die DataDescription für die Anmeldung beim Datenverteiler. */
		private void createDataDescriptions() {
			if(_simulationVariant != -1) {
				_receiveDataDescription = new DataDescription(_atg, _receiveAspect, _simulationVariant);
				_sendDataDescription = new DataDescription(_atg, _sendAspect, _simulationVariant);
			}
			else {
				_receiveDataDescription = new DataDescription(_atg, _receiveAspect);
				_sendDataDescription = new DataDescription(_atg, _sendAspect);
			}
		}

		/**
		 * Setzt die zu verwendenden Werte innerhalb einer Methode.
		 *
		 * @param object neu ausgewähltes Objekt
		 * @param atg    neu ausgewählte Attributgruppe
		 */
		private void setSelection(final SystemObject object, final AttributeGroup atg) {
			_object = object;
			_atg = atg;
			if(_newParametrisationAvailable) {
				_parameterController.setParameterInfo(object, atg, _simulationVariant);
			}
		}

		/** Startet den Parametereditor, indem die Daten bei der Parametrierung angemeldet werden und der Editor angezeigt wird. */
		public void start() {
			subscribe();
			_frame.pack();
			_frame.setVisible(true);
		}

		/**
		 * Stellt die Daten im Fenster dar.
		 *
		 * @param data darzustellende Daten
		 */
		private void updateData(final Data data) {
			_editorPanel.setData(data);
			_frame.pack();
		}

		/**
		 * Gibt an, ob nur das Fenster geschlossen werden soll, oder die ganze Anwendung.
		 *
		 * @param operation siehe Konstanten der Klasse <class>JFrame</class>
		 */
		private void setWindowCloseOperation(int operation) {
			_frame.setDefaultCloseOperation(operation);
		}

		/** Meldet die Daten bei der Parametrierung an. */
		private void subscribe() {
			if(_newParametrisationAvailable) {
				// bei der neuen Parametrierung anmelden
				_parameterController.addListener();
			}
			else {
				try {
					_connection.subscribeReceiver(
							_parameterEditorReceiverSender, _object, _receiveDataDescription, ReceiveOptions.normal(), ReceiverRole.receiver()
					);
					_connection.subscribeSender(_parameterEditorReceiverSender, _object, _sendDataDescription, SenderRole.sender());
				}
				catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
					_debug.error("Ausnahme, die bei einer Sendeanmeldung generiert wird, wenn bereits eine lokale Sendeanmeldung für die gleichen Daten von einem anderen Anwendungsobjekt vorliegt (siehe fehlermeldung)", oneSubscriptionPerSendData);
					throw new RuntimeException(oneSubscriptionPerSendData);
				}
			}
		}

		/** Meldet die angemeldeten Daten bei der Parametrierung ab. */
		private void unsubscribe() {
			if(_newParametrisationAvailable) {
				_parameterController.removeListener();
			}
			else {
				_connection.unsubscribeReceiver(_parameterEditorReceiverSender, _object, _receiveDataDescription);
				_connection.unsubscribeSender(_parameterEditorReceiverSender, _object, _sendDataDescription);
			}
		}

		/**
		 * Der Sende-Schaltknopf wird enabled oder disabled.
		 *
		 * @param enable <code>true</code>, ob der Button enabled werden soll, sonst <code>false</code>
		 */
		public void enableSendButton(final boolean enable) {
			_sendButton.setEnabled(enable);
		}

		/**
		 * Setzt den Ergebnisdatensatz der Parametrierung und stellt diesen im Parametereditor dar.
		 *
		 * @param resultData Ergebnisdatensatz
		 */
		public void setResultData(final ResultData resultData) {
			_receivedResultData = resultData;
			_actualDataButton.setEnabled(true);
			_editorPanel.setResultData(resultData);
			refreshSourcePanel(); // evtl. hat sich die Quelle verändert
			_frame.pack();
		}

		/**
		 * Sendet die eingegebenen Daten an die Parametrierung.
		 *
		 * @throws de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed Falls die Daten nicht gesendet werden konnten.
		 */
		public void sendCurrentData() throws SendSubscriptionNotConfirmed {
			if(_newParametrisationAvailable) {
				_parameterController.setParameter(_editorPanel.getData());
			}
			else {
				_connection.sendData(new ResultData(_object, _sendDataDescription, System.currentTimeMillis(), _editorPanel.getData()));
			}
		}


		/**
		 * Wird für die alte Parametrierung benötigt, um die Sendesteuerung zu aktivieren und den Sende-Button zu aktivieren, bzw. zu deaktivieren. Außerdem wird hier
		 * der Empfang neuer oder geänderten Datensätze verarbeitet.
		 */
		private static class ParameterEditorReceiverSender implements ClientReceiverInterface, ClientSenderInterface {

			final EditorFrame _editorFrame;

			public ParameterEditorReceiverSender(final EditorFrame editorFrame) {
				_editorFrame = editorFrame;
			}

			public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
				_editorFrame.enableSendButton(state == START_SENDING);
			}

			public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
				return true;
			}

			public void update(ResultData results[]) {
				_debug.fine("results.length = " + results.length);
				// falls mehrere Datensätze übertragen werden, nur den letzten Datensatz verwenden, alle anderen ignorieren
				_editorFrame.setResultData(results[results.length - 1]);
			}
		}
	}

	public static class ParameterChangeInformation implements ParameterChangeInformer {

		private final EditorFrame _editorFrame;

		public ParameterChangeInformation(final EditorFrame editorFrame) {
			_editorFrame = editorFrame;
		}

		public void setResultData(final ResultData resultdata) {
			_editorFrame.setResultData(resultdata);
		}

		public void setSourceObject(final String name) {
			_editorFrame.setSourceObject(name);
		}

		public void setSourceType(final String type) {
			_editorFrame.setSourceType(type);
		}

		public void showSourceButton(boolean show) {
			_editorFrame.showSourceButton(show);
		}
	}
}

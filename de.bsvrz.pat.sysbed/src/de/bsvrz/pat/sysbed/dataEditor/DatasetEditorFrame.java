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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.main.GenericTestMonitorApplication;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Diese Klasse kann anhand einer Datenidentifikation (Attributgruppe, Aspekt und Objekt) das {@link #startShowCurrentData aktuelle Objekt} vom Datenverteiler
 * darstellen, ein {@link #startSendCurrentData neues Objekt} erstellen und an den Datenverteiler senden oder den {@link #startParameterEditor Parametereditor}
 * starten.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see DataEditorPanel
 */
public class DatasetEditorFrame extends JFrame {

	/** Der Debug-Logger */
	private final static Debug _debug = Debug.getLogger();

	/** die Verbindung zum Datenverteiler */
	private final ClientDavInterface _connection;

	/** speichert die Datenbeschreibung für die Anmeldung beim Datenverteiler */
	private final DataDescription _dataDescription;

	/** speichert die aktuelle Attributgruppe */
	private final AttributeGroup _attributeGroup;

	/** speichert das aktuelle Aspekt */
	private final Aspect _aspect;

	/** speichert das aktuelle Systemobjekt */
	private final SystemObject _systemObject;

	/** speichert die aktuellen Systemobjekte für die Anmeldung beim Datenverteiler */
	private final SystemObject[] _systemObjects;

	/** speichert den {@link DataEditorPanel} */
	private final AbstractEditorPanel _dataEditorPanel;

	/** Speichert die Rolle für die Anmeldung beim Datenverteiler. Voreingestellt auf "Empfänger". */
	private ReceiverRole _receiverRole = ReceiverRole.receiver();

	/** Speichert die Empfängeroptionen für die Anmeldung beim Datenverteiler. Voreingestellt auf "Online (normal)". */
	private ReceiveOptions _receiveOptions = ReceiveOptions.normal();

	/** Speichert die Rolle für die Anmeldung beim Datenverteiler. Voreingestellt auf "Sender". */
	private SenderRole _senderRole = SenderRole.sender();

	/** speichert das Objekt, welches die Daten vom Datenverteiler empfängt */
	private DatasetReceiver _receiver;

	/** speichert das Objekt, welches überprüft, ob gesendet werden darf */
	private DatasetSender _sender;

	/** Speichert die ContentPane des Fensters. Dort werden die Panel angeordnet. */
	private final Container _pane;

	/** merkt sich den Button zum Senden eines Datensatzes */
	private JButton _sendButton;


	/* ##################### public - Methoden ################### */
	/**
	 * Der Konstruktor erstellt ein Fenster, welches die aktuellen Daten zu einer ausgewählten Datenidentifikation anzeigt. Erhält der Datenverteiler neue Daten
	 * für diese Datenidentifikation, dann wird das Fenster aktualisiert. <br>Wird als Simulationsvariante der Wert -1 übergeben, wird so verfahren, als ob keine
	 * Simulationsvariante übergeben wurde.
	 *
	 * @param connection        Verbindung zum Datenverteiler
	 * @param title             Titel des Fensters
	 * @param attributeGroup    Attributgruppe zur Anmeldung beim Datenverteiler
	 * @param aspect            Aspekt zur Anmeldung beim Datenverteiler
	 * @param systemObject      Systemobjekt zur Anmeldung beim Datenverteiler
	 * @param simulationVariant gibt die Simulationsvariante an, -1 entspricht keiner Simulationsvariante
	 */
	public DatasetEditorFrame(
			final ClientDavInterface connection,
			final String title,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			final SystemObject systemObject,
			int simulationVariant
	) {
		_connection = connection;
		_attributeGroup = attributeGroup;
		_aspect = aspect;
		_systemObject = systemObject;
//		System.out.println("simulationVariant = " + simulationVariant);
		if(simulationVariant != -1) {
			_dataDescription = new DataDescription(attributeGroup, aspect, (short)simulationVariant);
		}
		else {
			_dataDescription = new DataDescription(attributeGroup, aspect);
		}
		_systemObjects = new SystemObject[]{systemObject};
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(GenericTestMonitorApplication.getTitle(title, connection));

		_dataEditorPanel = new DataEditorPanel(connection);

		_pane = getContentPane();
		_pane.setLayout(new BorderLayout());
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Datenidentifikation nur angezeigt werden soll. Die {@link #setReceiveOptions Empfangsoptionen} ist auf "Online" und
	 * die {@link #setReceiverRole Empfängerrolle} auf "Empfänger" voreingestellt.
	 */
	public void startShowCurrentData() {
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent event) {
						// Wenn das Fenster geschlossen wird, meldet sich die Anwendung mit dieser Datenidentifikation beim Datenverteiler wieder ab.
						_connection.unsubscribeReceiver(_receiver, _systemObjects, _dataDescription);
						System.out.println("Daten (ShowCurrentData) wurden abgemeldet.");
					}
				}
		);
		_dataEditorPanel.setEditable(false);
		_pane.add(getHeaderPanel(_attributeGroup, _aspect, _systemObject), BorderLayout.NORTH);
		_pane.add(_dataEditorPanel, BorderLayout.CENTER);
		pack();

		try {
			registerReceiver();
		}
		catch(IllegalStateException ex) {
			dispose();
			throw new IllegalStateException(ex.getMessage());
		}
		setVisible(true);
	}

	/**
	 * Diese Methode wird aufgerufen, wenn für eine Datenidentifikation ein neuer Datensatz erzeugt/erstellt und gesendet werden soll. Die {@link #setSenderRole
	 * Senderrolle} ist auf "Sender" voreingestellt.
	 *
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData
	 *          Ausnahme, die bei einer Sendeanmeldung generiert wird, wenn bereits eine lokale Sendeanmeldung für die gleichen Daten von einem anderen
	 *          Anwendungsobjekt vorliegt.
	 */
	public void startSendCurrentData() throws OneSubscriptionPerSendData {
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent event) {
						// Wenn das Fenster geschlossen wird, meldet sich die Anwendung mit dieser Datenidentifikation beim Datenverteiler ab.
						_connection.unsubscribeSender(_sender, _systemObjects, _dataDescription);
					}
				}
		);
		_dataEditorPanel.setEditable(true);
		_pane.add(getHeaderPanel(_attributeGroup, _aspect, _systemObject), BorderLayout.NORTH);
		_pane.add(_dataEditorPanel, BorderLayout.CENTER);
		_pane.add(getSenderButtons(), BorderLayout.SOUTH);
		_dataEditorPanel.setData(_connection.createData(_attributeGroup));
		pack();
		registerSender();
		setVisible(true);
	}

	/** TBD wird noch implementiert */
	public void startParameterEditor() {
	}

	/**
	 * Mit dieser Methode kann die Rolle des Senders geändert werden. Die Default-Einstellung ist "Sender".
	 *
	 * @param senderRole die Rolle ist Sender
	 */
	public void setSenderRole(final SenderRole senderRole) {
		_senderRole = senderRole;
	}

	/**
	 * Setzt die Rolle des Empfängers. Diese wird für den Datenverteiler benötigt. Die Default-Einstellung ist "Empfänger".
	 *
	 * @param receiverRole die Rolle des Empfängers
	 */
	public void setReceiverRole(final ReceiverRole receiverRole) {
		_receiverRole = receiverRole;
	}

	/**
	 * Setzt die Empfangsoption (Online, nur geänderte Datensätze, auch nachgelieferte Datensätze). Die Default-Einstellung ist "Online (normal)".
	 *
	 * @param receiveOptions die Empfangsoption
	 */
	public void setReceiveOptions(final ReceiveOptions receiveOptions) {
		_receiveOptions = receiveOptions;
	}

	/**
	 * Die Methode <class>getPreferredSize()</class> wird überschrieben, da sonst Teile des Fensters hinter der Taskbar liegen und somit nicht zu sehen sind.
	 *
	 * @return die Größe des Fensters
	 */
	public Dimension getPreferredSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension preferredSize = super.getPreferredSize();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
		int visibleScreenHeight = screenSize.height - insets.bottom - insets.top;
		int visibleScreenWidth = screenSize.width - insets.left - insets.right;
		if(preferredSize.height > visibleScreenHeight) {
			preferredSize.height = visibleScreenHeight;
			JScrollPane scrollPane = new JScrollPane();
			preferredSize.width += scrollPane.getVerticalScrollBar().getPreferredSize().width;
		}
		if(preferredSize.width > visibleScreenWidth) {
			preferredSize.width = visibleScreenWidth;
		}
		return preferredSize;
	}


	/* ########### Private Methoden ########### */
	/**
	 * Stellt die ausgewählte Datenidentifikation dar.
	 *
	 * @param attributeGroup die darzustellende Attributgruppe
	 * @param aspect         den darzustellenden Aspekt
	 * @param systemObject   das darzustellende Systemobjekt
	 *
	 * @return die ausgewählte Datenidentifikation als JPanel
	 */
	private JPanel getHeaderPanel(final AttributeGroup attributeGroup, final Aspect aspect, final SystemObject systemObject) {
		JLabel atgLabel = new JLabel("Attributgruppe: ");
		JLabel aspLabel = new JLabel("Aspekt: ");
		JLabel objLabel = new JLabel("Objekt: ");

		JTextField atgTextField = new JTextField(attributeGroup.getNameOrPidOrId());
		atgTextField.setEditable(false);
		atgTextField.setFocusable(false);
		JTextField aspTextField = new JTextField(aspect.getNameOrPidOrId());
		aspTextField.setEditable(false);
		aspTextField.setFocusable(false);
		JTextField objTextField = new JTextField(systemObject.getNameOrPidOrId());
		objTextField.setEditable(false);
		objTextField.setFocusable(false);

		// anordnen der Komponenten
		GridBagConstraints gbc;
		GridBagLayout gbl = new GridBagLayout();
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(gbl);

		// Attributgruppe
		gbc = makegbc(0, 0, 1, 1);
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(atgLabel, gbc);
		headerPanel.add(atgLabel);

		gbc = makegbc(1, 0, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(atgTextField, gbc);
		headerPanel.add(atgTextField);

		// Aspekt
		gbc = makegbc(0, 1, 1, 1);
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(aspLabel, gbc);
		headerPanel.add(aspLabel);

		gbc = makegbc(1, 1, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(aspTextField, gbc);
		headerPanel.add(aspTextField);

		// Objekt
		gbc = makegbc(0, 2, 1, 1);
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(objLabel, gbc);
		headerPanel.add(objLabel);

		gbc = makegbc(1, 2, 1, 1);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(objTextField, gbc);
		headerPanel.add(objTextField);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Datenidentifikationsauswahl"));
		panel.add(headerPanel, BorderLayout.WEST);

		return panel;
	}

	/**
	 * In dieser Methode werden die Buttons angeordnet, die zum Erzeugen, Löschen und Senden von Datensätze benötigt werden.
	 *
	 * @return die auf einem Panel angeordneten Buttons
	 */
	private JPanel getSenderButtons() {
		JButton createButton = new JButton("Datensatz erzeugen");
		createButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_dataEditorPanel.setData(_connection.createData(_attributeGroup));
					}
				}
		);
		JButton deleteButton = new JButton("Datensatz löschen");
		deleteButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_dataEditorPanel.setData(null);
					}
				}
		);
		_sendButton = new JButton("Datensatz senden");
		_sendButton.setEnabled(false);
		_sendButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Data data = _dataEditorPanel.getData();
						ResultData result = new ResultData(_systemObject, _dataDescription, System.currentTimeMillis(), data);
						try {
							_connection.sendData(result);
						}
						catch(Exception ex) {
							_debug.error("Fehler beim Versand des Datensatzes: " + ex);
							JOptionPane.showMessageDialog(
									DatasetEditorFrame.this,
									"Daten konnten nicht gesandt werden!",
									"Fehler beim Versand des Datensatzes",
									JOptionPane.ERROR_MESSAGE
							);
						}
					}
				}
		);

		_sendButton.addMouseMotionListener(
				new MouseMotionAdapter() {
					@Override
					public void mouseMoved(final MouseEvent e) {
						if(e.isAltDown() && _senderRole.equals(SenderRole.source()) && _sender.getState() == 1) {
							_sendButton.setEnabled(true);
						}
					}
				}
		);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(Box.createHorizontalGlue());
		panel.add(createButton);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(deleteButton);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(_sendButton);

		return panel;
	}

	/**
	 * Meldet sich mit der Datenidentifikation als Empfänger beim Datenverteiler an.
	 *
	 * @throws de.bsvrz.dav.daf.main.DataNotSubscribedException
	 *          Daten-Nicht-Angemeldet-Ausnahme, die beim Senden von Datensätzen ohne entsprechende Sendeanmeldungen generiert wird.
	 */
	private void registerReceiver() throws DataNotSubscribedException {
		_receiver = new DatasetReceiver();
		_connection.subscribeReceiver(_receiver, _systemObjects, _dataDescription, _receiveOptions, _receiverRole);
	}

	/**
	 * Meldet sich mit der Datenidentifikation als Sender beim Datenverteiler an.
	 *
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData
	 *          Ausnahme, die bei einer Sendeanmeldung generiert wird, wenn bereits eine lokale Sendeanmeldung für die gleichen Daten von einem anderen
	 *          Anwendungsobjekt vorliegt.
	 */
	private void registerSender() throws OneSubscriptionPerSendData {
		_sender = new DatasetSender();
		_connection.subscribeSender(_sender, _systemObjects, _dataDescription, _senderRole);
	}

	/**
	 * Hilfsmethode für das GridBagLayout zur Positionierung der Elemente.
	 *
	 * @param x      die x-Position im Grid
	 * @param y      die y-Position im Grid
	 * @param width  gibt die Anzahl der Spalten an, die die Komponente nutzen soll
	 * @param height gibt die Anzahl der Zeilen an, die die Komponente nutzen soll
	 *
	 * @return die Rahmenbedingungen für eine Komponente
	 */
	private GridBagConstraints makegbc(int x, int y, int width, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.insets = new Insets(1, 5, 1, 1);
		return gbc;
	}


	/* ################# Klasse DatasetReceiver ############## */
	/** Diese Klasse empfängt die Daten vom Datenverteiler, worauf die {@link DatasetEditorFrame Anwendung} sich vorher angemeldet hat. */
	private class DatasetReceiver implements ClientReceiverInterface {
		/**
		 * speichert den aktuell empfangenen Datensatz
		 */
//		private Data _receivedData;

		/**
		 * Diese Methode erhält immer die aktuellsten Daten zu der angemeldeten Datenidentifikation vom Datenverteiler.
		 *
		 * @param results aktuelle Daten vom Datenverteiler
		 */
		public void update(ResultData results[]) {
			_debug.fine("results.length = " + results.length);
//			_receivedData = results[results.length - 1].getData();
			_dataEditorPanel.setResultData(results[results.length - 1]);
//			_dataEditorPanel.setData(_receivedData == null ? null : _receivedData.createModifiableCopy());
			pack();   // ermittelt für das Fenster zum Darstellen der Daten die optimale Größe
		}
	}

	/** Diese Klasse überprüft mit Hilfe der Sendesteuerung, ob gesendet werden kann oder nicht. */
	private class DatasetSender implements ClientSenderInterface {

		private byte _state = (byte)-1;

		/**
		 * Falls gesendet werden kann, wird der Sende-Button aktiviert.
		 *
		 * @param object          das Objekt, das beim Datenverteiler angemeldet wurde
		 * @param dataDescription Information der angemeldeten Daten
		 * @param state           Status der Sendesteuerung
		 */
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			setState(state);
			_sendButton.setEnabled(state == ClientSenderInterface.START_SENDING);
			final String message;
			if(state == STOP_SENDING_NO_RIGHTS) {
				message = "Es liegen momentan keine Rechte für den Versand vor.";
			}
			else if(state == STOP_SENDING_NOT_A_VALID_SUBSCRIPTION) {
				message = "Die getätigte Anmeldung ist momentan nicht erlaubt.";
			}
			else if(state == STOP_SENDING) {
				message = "Es sind keine Empfänger vorhanden." + (_senderRole.equals(SenderRole.source()) ? " (Alt-Taste drücken um trotzdem zu senden)" : "");
			}
			else{
				message = null;
			}
			_sendButton.setToolTipText(message);
		}

		/**
		 * Diese Methode gibt an, ob Sendesteuerung erwünscht ist.
		 *
		 * @param object          das Objekt, das beim Datenverteiler angemeldet wurde
		 * @param dataDescription Information der angemeldeten Daten
		 *
		 * @return gibt zurück, ob Sendesteuerung erwünscht ist
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}

		public byte getState() {
			return _state;
		}

		public void setState(final byte state) {
			_state = state;
		}
	}
}

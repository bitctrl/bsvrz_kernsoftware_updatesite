/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.application;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.sys.funclib.debug.Debug;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Diese abstrakte Klasse ist eine Beispielimplementierung des Interfaces {@link GUIApplication}. Durch Aufruf der Methode {@link #connect} wird ein
 * Login-Dialog dargestellt, wo die TCP/IP-Adresse zum Datenverteiler, Benutzername und Passwort eingegeben werden m�ssen. Die letzten 20 erfolgreichen
 * Login-Versuche werden lokal auf dem Rechner gespeichert.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8142 $
 */
public abstract class AbstractGUIApplication implements GUIApplication {
	
	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();
	
	private JDialog _dialog;
	
	private ClientDavInterface _connection;
	
	private ClientDavParameters _parameters;
	
	private PreferencesModel _preferencesModel;
	
	public ClientDavInterface connect(ClientDavParameters parameters) {
		_preferencesModel = new PreferencesModel(Preferences.userRoot().node("/login"), 100);
		
		_parameters = parameters;
		_dialog = createDialog(getApplicationName());
		
		if(isDavConnected()) {
			_debug.finest("OK-Button gedr�ckt.");
			return getConnection();
		}
		else {
			_debug.finest("Beenden-Button gedr�ckt.");
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * Wird von der Anwendung implementiert und liefert den Namen der Applikation.
	 * 
	 * @return Name der Applikation
	 */
	protected abstract String getApplicationName();
	
	private JDialog createDialog(String title) {
		// Dialog erstellen
		final JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setTitle(title + " - Login");
		
		final PortComboBoxModel portComboBoxModel = new PortComboBoxModel(_preferencesModel);
		final IpComboBoxModel ipComboBoxModel = new IpComboBoxModel(_preferencesModel, portComboBoxModel);
		
		// Komponenten des Dialogs erstellen
		final JLabel ipLabel = new JLabel("Domainname / IP-Adresse: ");
		final JComboBox ipComboBox = new JComboBox(ipComboBoxModel);
		ipComboBox.setEditable(true);
		
		final JLabel portLabel = new JLabel("TCP-Portnummer: ");
		final JComboBox portComboBox = new JComboBox(portComboBoxModel);
		portComboBox.setEditable(true);
		
		final JLabel userNameLabel = new JLabel("Benutzername: ");
		final JTextField userNameTextField = new JTextField();
		userNameLabel.setLabelFor(userNameTextField);
		
		// Der Cursor soll in das Textfeld, in dem der Benutzername eingetragen werden soll, positioniert werden 
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				userNameTextField.requestFocusInWindow();
			}
		});
		
		final JLabel passwordLabel = new JLabel("Passwort: ");
		final JPasswordField passwordField = new JPasswordField();
		passwordLabel.setLabelFor(passwordField);
		
		// Elemente auf dem Dialog anordnen
		final GridBagLayout gridBagLayout = new GridBagLayout();
		final JPanel inputPanel = new JPanel(gridBagLayout);
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc;
		gbc = makegbc(0, 0, 1, 1);
		gridBagLayout.setConstraints(ipLabel, gbc);
		inputPanel.add(ipLabel);
		
		gbc = makegbc(1, 0, 1, 1);
		gridBagLayout.setConstraints(ipComboBox, gbc);
		inputPanel.add(ipComboBox);
		
		gbc = makegbc(2, 0, 1, 1);
		gridBagLayout.setConstraints(portLabel, gbc);
		inputPanel.add(portLabel);
		
		gbc = makegbc(3, 0, 1, 1);
		gridBagLayout.setConstraints(portComboBox, gbc);
		inputPanel.add(portComboBox);
		
		gbc = makegbc(0, 1, 1, 1);
		gridBagLayout.setConstraints(userNameLabel, gbc);
		inputPanel.add(userNameLabel);
		
		gbc = makegbc(1, 1, 3, 1);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(userNameTextField, gbc);
		inputPanel.add(userNameTextField);
		
		gbc = makegbc(0, 2, 1, 1);
		gridBagLayout.setConstraints(passwordLabel, gbc);
		inputPanel.add(passwordLabel);
		
		gbc = makegbc(1, 2, 3, 1);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridBagLayout.setConstraints(passwordField, gbc);
		inputPanel.add(passwordField);
		
		// OK- und Beenden-Schaltfl�che einf�gen
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		final JButton okButton = new JButton("OK");
		dialog.getRootPane().setDefaultButton(okButton);
		final JButton cancelButton = new JButton("Beenden");
		final ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == okButton) {
					_dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					ClientDavInterface connection = null;
					
					String ip = (String)ipComboBox.getSelectedItem();
					String port = (String)portComboBox.getSelectedItem();
					String userName = userNameTextField.getText();
					String password = "";
					char[] passwd = passwordField.getPassword();
					for(int i = 0; i < passwd.length; i++) {
						password += passwd[i];
					}
					try {
						if(ip != null) {
							ip = ip.trim();
							//						System.out.println("IP-Adresse: " + ip);
							_parameters.setDavCommunicationAddress(ip);
						}
						if(port != null && !port.equals("")) {
							port = port.trim();
							//						System.out.println("Portnummer: " + port);
							_parameters.setDavCommunicationSubAddress(Integer.parseInt(port));
						}
						if(userName != null) {
							userName = userName.trim();
							//						System.out.println("Benutzer: " + userName);
						}
						if(password != null) {
							//						System.out.println("Passwort: " + password);
						}
						
						connection = new ClientDavConnection(_parameters);
						// Fertigmeldung f�r SWE Start/Stop wird explizit �bernommen
						connection.enableExplicitApplicationReadyMessage();
						
						connection.connect();
						connection.login(userName, password);
					}
					catch(Exception ex) {
						String message;
						if(ex instanceof NumberFormatException) {
							message = "Format der Portnummer ist fehlerhaft";
						}
						else if(ex instanceof IllegalArgumentException) {
							message = "Fehlerhaftes Passwort";
						}
						else {
							message = ex.getMessage();
						}
						// Die Verbindung konnte nicht aufgebaut werden. Es muss auch die TCP/IP Verbindung beendet werden.
						try {
	                        connection.disconnect(false, ex.getLocalizedMessage());
                        }
                        catch(RuntimeException e1) {
	                        // Wird ignoriert
                        }
						JOptionPane.showMessageDialog(_dialog, message, "Fehler beim Verbindungsaufbau", JOptionPane.ERROR_MESSAGE);
						connection = null;
					}
					_dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					
					// wenn die Verbindung zum Datenverteiler hergestellt werden konnte und die Login-Daten stimmen, wird die Verbindung zur�ckgegeben
					if(connection != null) {
						setConnection(connection);
						_preferencesModel.addSelectedConnection();
						_dialog.setVisible(false); // hierdurch wird dann auch _dialog.dispose() aufgerufen bzw. setVisible(true) wird beendet
					}
				}
				if(e.getSource() == cancelButton) {
					setConnection(null);
					_dialog.setVisible(false);
				}
			}
		};
		okButton.addActionListener(actionListener);
		cancelButton.addActionListener(actionListener);
		
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalStrut(20));
		buttonPanel.add(cancelButton);
		
		final JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(inputPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		// Panel im Fenster anordnen
		final Container loginPane = dialog.getContentPane();
		loginPane.setLayout(new BorderLayout());
		loginPane.add(mainPanel, BorderLayout.CENTER);
		
		return dialog;
	}
	
	public boolean isDavConnected() {
		// Fenster auf dem Bildschirm positionieren und darstellen
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		_dialog.pack();
		_dialog.setLocation((screenSize.width - _dialog.getSize().width) / 2, (screenSize.height - _dialog.getSize().height) / 2);
		_dialog.setVisible(true);
		_dialog.dispose();
		return (getConnection() != null);
	}
	
	public ClientDavInterface getConnection() {
		return _connection;
	}
	
	public void setConnection(ClientDavInterface connection) {
		_connection = connection;
	}
	
	/**
	 * Dies ist eine Hilfsmethode, die f�r den GridBagLayout-Manager ben�tigt wird. Sie vereinfacht die Angabe der wesentlichen Constraints des Layout-Managers.
	 * 
	 * @param x
	 *            x-Position
	 * @param y
	 *            y-Position
	 * @param width
	 *            Anzahl Spalten, die die Komponente benutzen soll
	 * @param height
	 *            Anzahl Zeilen, die die Komponente benutzen soll
	 * 
	 * @return Die Constraints f�r den Layout-Manager.
	 */
	private static GridBagConstraints makegbc(int x, int y, int width, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		return gbc;
	}
	
	private class IpComboBoxModel extends AbstractListModel implements ComboBoxModel {
		
		private final PreferencesModel _preferencesModel;
		
		private final PortComboBoxModel _portComboBoxModel;
		
		public IpComboBoxModel(final PreferencesModel preferencesModel, PortComboBoxModel portComboBoxModel) {
			_preferencesModel = preferencesModel;
			_portComboBoxModel = portComboBoxModel;
		}
		
		public int getSize() {
			return _preferencesModel.getIpSize();
		}
		
		public Object getElementAt(int index) {
			return _preferencesModel.getIpElementAt(index);
		}
		
		public void setSelectedItem(Object anItem) {
			_preferencesModel.setSelectedIp((String)anItem);
			_portComboBoxModel.setSelectedItem(_portComboBoxModel.getSelectedItem());
		}
		
		public Object getSelectedItem() {
			return _preferencesModel.getSelectedIp();
		}
	}
	
	private class PortComboBoxModel extends AbstractListModel implements ComboBoxModel {
		
		private final PreferencesModel _preferencesModel;
		
		public PortComboBoxModel(PreferencesModel preferencesModel) {
			_preferencesModel = preferencesModel;
		}
		
		public int getSize() {
			return _preferencesModel.getPortSize();
		}
		
		public Object getElementAt(int index) {
			return _preferencesModel.getPortElementAt(index);
		}
		
		public void setSelectedItem(Object anItem) {
			_preferencesModel.setSelectedPort((String)anItem);
			super.fireContentsChanged(anItem, 0, 0);
		}
		
		public Object getSelectedItem() {
			return _preferencesModel.getSelectedPort();
		}
	}
	
	private class PreferencesModel {
		
		private final Preferences _preferencesRoot;
		
		private final int _numberOfIpsToStore;
		
		private final List<String> _ipList = new LinkedList<String>();
		
		private String _selectedIp = "localhost";
		
		private String _selectedPort = "8083";
		
		/**
		 * Konstruktor liest alle relevanten Eintr�ge in den Preferences ein und setzt den Wert, der in der ComboBox direkt zu sehen ist. In den Listen stehen
		 * die Werte, die �ber die ComboBoxen ausgew�hlt werden k�nnen.
		 * 
		 * @param preferencesRoot
		 * @param numberOfIpsToStore
		 */
		public PreferencesModel(Preferences preferencesRoot, int numberOfIpsToStore) {
			_preferencesRoot = preferencesRoot;
			_numberOfIpsToStore = numberOfIpsToStore;
			// gibt es Eintr�ge in den Preferences zu IP und Port ? Wenn nein -> Default-Werte localhost:8083
			try {
				String[] children = _preferencesRoot.childrenNames();
				if(children.length > 0) { // es gibt abgespeicherte Werte
					Preferences lastUsedNode = _preferencesRoot.node(children[0]); // zuletzt abgespeicherter Wert
					_selectedIp = lastUsedNode.get("ip", "localhost");
					_selectedPort = lastUsedNode.get("port", "8083");
					synchronized(_ipList) {
						for(int i = 0; i < children.length; i++) {
							String child = children[i];
							Preferences childPrefs = _preferencesRoot.node(child);
							String ip = childPrefs.get("ip", null);
							if(ip != null && !_ipList.contains(ip)) _ipList.add(ip);
						}
					}
				}
			}
			catch(BackingStoreException ex) {
				ex.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
			}
			
			/**
			 * Falls sich etwas an den Knoten �ndert, muss die Liste ggf. auch ge�ndert werden.
			 */
			_preferencesRoot.addNodeChangeListener(new NodeChangeListener() {
				public void childAdded(NodeChangeEvent evt) {
					actualizeIpList();
				}
				
				public void childRemoved(NodeChangeEvent evt) {
					actualizeIpList();
				}
				
				public void actualizeIpList() {
					try {
						synchronized(_ipList) {
							_ipList.clear();
							String[] children = _preferencesRoot.childrenNames();
							for(int i = 0; i < children.length; i++) {
								String child = children[i];
								Preferences childPrefs = _preferencesRoot.node(child);
								String ip = childPrefs.get("ip", null);
								if(ip != null && !_ipList.contains(ip)) _ipList.add(ip);
							}
						}
					}
					catch(BackingStoreException ex) {
						_debug.warning("Die Liste mit den IP-Adressen konnte nicht aktualisiert werden", ex);
					}
				}
			});
		}
		
		/**
		 * Erstellt anhand der Felder <code>_selectedIp</code> und <code>_selectedPort</code> einen neuen IP/Port-Eintrag in den Einstellungen. Unterhalb von
		 * <code>_preferencesRoot</code> werden neue Knoten angelegt, deren Bezeichnung bei <code>"2000000000"</code> beginnt. F�r jeden weiteren Eintrag wird dort
		 * eins heruntergez�hlt. Sind mehr Knoten vorhanden, als <code>_numberOfIpsToStore</code> festlegt, wird der �lteste Eintrag (der mit der h�chsten Nummer)
		 * entfernt.
		 */
		public void addSelectedConnection() {
			try {
				final String ZEROS = "0000000000";
				final int initialNumber = 2000000000;
				String[] children = _preferencesRoot.childrenNames();

				if(children.length > 0) {
					//Falls der letzte Eintrag bereits die aktuellen Daten enth�lt, ist nichts zu tun
					Preferences childPrefs = _preferencesRoot.node(getNewestEntry(children));
					if(childPrefs.get("ip", "").equals(_selectedIp) && childPrefs.get("port", "").equals(_selectedPort)) {
						return;
					}
					// ist der neue Eintrag bereits (irgendwo anders) vorhanden -> alten Eintrag l�schen
					for(int i = 0; i < children.length; i++) {
						String child = children[i];
						childPrefs = _preferencesRoot.node(child);
						if(childPrefs.get("ip", "").equals(_selectedIp) && childPrefs.get("port", "").equals(_selectedPort)) {
							childPrefs.removeNode();
							children = _preferencesRoot.childrenNames();
							break;
						}
					}
				}

				final int nextNumber;
				// Neue zu vergebene Nummer ermitteln
				if(children.length > 0) {
					// Vorhandene letzte Nummer nehmen
					final String newestChild = getNewestEntry(children);
					// und eins entfernen
					nextNumber = Integer.parseInt(newestChild) - 1;
				}
				else {
					// Noch keine Einstellungen vorhanden
					nextNumber = initialNumber;
				}

				// Neue Knoten-Nummer ermitteln
				String newNode = String.valueOf(nextNumber);

				// Nummer links mit Nullen auff�llen
				final String padding = ZEROS.substring(0, ZEROS.length() - newNode.length());
				newNode = padding + newNode;

				// Neuen Knoten erstellen
				Preferences nextConnection = _preferencesRoot.node(newNode);
				nextConnection.put("ip", _selectedIp);
				nextConnection.put("port", _selectedPort);

				// Kindknoten neu einlesen
				children = _preferencesRoot.childrenNames();

				// Anzahl der Eintr�ge begrenzen, �lteste Eintr�ge l�schen
				while(children.length > _numberOfIpsToStore) {
					final String oldestChild = getOldestEntry(children);
					_preferencesRoot.node(oldestChild).removeNode();

					// Kindknoten neu einlesen
					children = _preferencesRoot.childrenNames();
				}
			}
			catch(BackingStoreException ex) {
				_debug.warning("�berz�hlige IP-Adressen konnten nicht gel�scht werden", ex);
			}
		}

		/** Gibt den neuesten Eintrag aus einem children-Array zur�ck. Hilfsfunktion von addSelectedConnection.
		 *
 		 * @param children Ein Array der Form <code>{"2000000000","1999999999","1999999998"}</code>
		 * @return den niedrigsten Wert im Array. Zum Beispiel <code>"1999999998"</code>
		 */
		private String getNewestEntry(final String[] children) {
			String newestChild = children[0];
			for(int i = 1; i < children.length; i++) {
				final String child = children[i];
				if(newestChild.compareTo(child) > 0) newestChild = child;
			}
			return newestChild;
		}

		/** Gibt den �ltesten Eintrag aus einem children-Array zur�ck. Hilfsfunktion von addSelectedConnection.
		 *
 		 * @param children Ein Array der Form <code>{"2000000000","1999999999","1999999998"}</code>
		 * @return den h�chsten Wert im Array. Zum Beispiel <code>"2000000000"</code>
		 */
		private String getOldestEntry(final String[] children) {
			String oldestChild = children[0];
			for(int i = 1; i < children.length; i++) {
				final String child = children[i];
				if(oldestChild.compareTo(child) < 0) oldestChild = child;
			}
			return oldestChild;
		}
		
		/**
		 * Gibt die Anzahl der IPs zur�ck.
		 * 
		 * @return die Anzahl der IPs
		 */
		public int getIpSize() {
			int size = 0;
			synchronized(_ipList) {
				size = _ipList.size();
			}
			return size;
		}
		
		/**
		 * Gibt die IP an einer bestimmten Position zur�ck.
		 * 
		 * @param index
		 *            die Position
		 * 
		 * @return die IP an einer bestimmten Position
		 */
		public String getIpElementAt(int index) {
			String result = "localhost";
			synchronized(_ipList) {
				result = _ipList.get(index);
			}
			return result;
		}
		
		/**
		 * Setzt die IP, die ausgew�hlt wurde, bzw. eingegeben wurde und bestimmt auch die zugeh�rigen Ports.
		 * 
		 * @param selectedIp
		 *            die ausgew�hlte IP
		 */
		public void setSelectedIp(String selectedIp) {
			_selectedIp = selectedIp;
			if(getPortSize() > 0) setSelectedPort(getPortElementAt(0));
		}
		
		/**
		 * Gibt die ausgew�hlte IP zur�ck.
		 * 
		 * @return die ausgew�hlte IP
		 */
		public String getSelectedIp() {
			return _selectedIp;
		}
		
		/**
		 * Gibt die Anzahl der Ports zur�ck.
		 * 
		 * @return die Anzahl der Ports
		 */
		public int getPortSize() {
			int numberOfRelevantPorts = 0;
			try {
				// Anzahl Ports anhand der ausgew�hlten IP ermitteln
				String[] children = _preferencesRoot.childrenNames();
				for(int i = 0; i < children.length; i++) {
					String child = children[i];
					Preferences childPrefs = _preferencesRoot.node(child);
					if(childPrefs.get("ip", "").equals(_selectedIp)) {
						numberOfRelevantPorts++;
					}
				}
			}
			catch(BackingStoreException ex) {
				_debug.warning("Anzahl der Ports, die zur ausgew�hlten IP-Adresse geh�ren, konnte nicht ermittelt werden", ex);
			}
			return numberOfRelevantPorts;
		}
		
		/**
		 * Gibt den Port an einer bestimmten Position in der ComboBox zur�ck.
		 * 
		 * @param index
		 *            die Position
		 * 
		 * @return den Port an der angegebenen Position
		 */
		public String getPortElementAt(int index) {
			try {
				final List<String> portList = new LinkedList<String>();
				String[] children = _preferencesRoot.childrenNames();
				for(int i = 0; i < children.length; i++) {
					String child = children[i];
					Preferences childPrefs = _preferencesRoot.node(child);
					if(childPrefs.get("ip", "").equals(_selectedIp)) {
						String port = childPrefs.get("port", null);
						if(port != null) portList.add(port);
					}
				}
				return portList.get(index);
			}
			catch(BackingStoreException ex) {
				_debug.warning("Portnummer zum Index " + index + " konnte nicht ermittelt werden. Es wird '8083' zur�ckgegeben", ex);
				return "8083";
			}
		}
		
		/**
		 * Setzt den ausgew�hlten oder editierten Port.
		 * 
		 * @param selectedPort
		 *            der ausgew�hlte Port
		 */
		public void setSelectedPort(String selectedPort) {
			_selectedPort = selectedPort;
		}
		
		/**
		 * Gibt den ausgew�hlten Port zur�ck.
		 * 
		 * @return der ausgew�hlte Port
		 */
		public String getSelectedPort() {
			return _selectedPort;
		}
	}
}

/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 * Diese abstrakte Klasse stellt eine grafische Benutzeroberfl�che zum ausf�hren
 * von Klassen zur Verf�gung. Die Klasse stellt ein Menu und ein Fenster, das
 * Fehler und Logs darstellt, zur Verf�gung. Das Menu erlaubt dem Benutzer die
 * Logs zu speichern und das Programm zu beenden. Der Benutzer kann eigene
 * Fenster in die Oberfl�che integrieren, indem er Methoden dieser Klasse
 * �berschreibt, falls er die Klassen nicht �berschreibt wird ein
 * Standardfenster zur Verf�gung gestellt.
 * <p/>
 * Der Benutzer kann mehrere Aufrufparamter �bergeben, die ausgewertet werden:
 * <br> -gui= ja/nein (soll die grafische Benutzeroberfl�che angezeigt
 * werden)<br> -prozessname= Klasse, die ausgef�hrt werden soll<br>
 * -arbeitsverzeichnis= Arbeitsverzeichnis, das genutzt werden soll<br>
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5005 $
 */
public abstract class SimpleApplicationGUI {

	/**
	 * Font f�r Logs
	 */
	final static Font _font = new Font("Monospaced", Font.PLAIN, 11);

	/**
	 * Panel, das alle Componenten enth�lt, die angezeigt werden sollen
	 */
	private final JFrame _mainFrame = new JFrame();

	/**
	 * Menu, das vom Anwender der abstrakten Klasse modifiziert werden kann
	 */
	private final JMenuBar _menuBar = new JMenuBar();

	/**
	 * Dieses Panel wird vom Benutzer zur Verf�gung gestellt
	 */
	private JPanel _userPanel = null;

	/**
	 * Dieses Fenster wird in der historie angezeigt und erscheint zwischen den
	 * beiden Logos auf der linken Seite
	 */
	private JPanel _legendPanel = null;

	/**
	 * In diesem Fenster werden alle DEbugausgaben dargestellt
	 */
	private JTextArea _debugTextArea = new JTextArea();

	/**
	 * In diesem Fenster werden alle "Standart Error Messages" dargestellt
	 */
	private JTextArea _errorTextArea = new JTextArea();

	/**
	 * Prozess, der das Script ausf�hrt
	 */
	private Process _process = null;

	/**
	 * Gibt Errormessages aus
	 */
	private Thread _errorThread = null;

	/**
	 * Gibt Debugmessages aus
	 */
	private Thread _debugThread = null;

	private final String _saveErrorLogText = "Fehlerausgabe speichern";
	private final String _saveDebugLogText = "Standardausgabe speichern";
	private final String _clearErrorLogText = "Fehlerausgabe l�schen";
	private final String _clearDebugLogText = "Standardausgabe l�schen";
	private final String _endText = "Beenden";

	/**
	 * Speichert das zuletzt gew�hlte Verzeichnis in einem FileChooser
	 */
	private File _fileChooserDirectory = null;

	/**
	 * Speichert die Argumente, mit denen der Prozoess aufgerufen wurde
	 */
	private String[] _processArguments;

	/**
	 * Speichert die Umgebungsvariablen, mit denen der Prozoess aufgerufen wurde
	 */
	private String[] _processEnvironment;

	/**
	 * Speichert das Arbeitsverzeichniss des Prozesses
	 */
	private File _processWorkingDirectory;

	/**
	 * Klasse, die gestartet wird
	 */
	private String _className = "";

	/**
	 * Falls der StandardUserPanel benutzt wird, wird hier der Name des Buttons zum
	 * stoppen und neustarten des Prozesses festgelegt.
	 */
	final String _standardUserPanelButtonName = "Prozess stoppen und neu starten";

	/**
	 * Argumentliste, die in der start-Methode �bergeben wurde, es fehlen die
	 * Argumente:<br> -gui<br> -prozessname<br> -arbeitsverzeichnis<br>
	 */
	private String[] _modifiedArgumentlist;

	protected SimpleApplicationGUI() {

		ActionListener actionProcCmd = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JMenuItem source = (JMenuItem) (e.getSource());

				if (source.getText().equals(_saveErrorLogText)) {

					final JFileChooser fc;
					if (_fileChooserDirectory != null) {
						// Es ist ein Verzeichnis bekannt
						fc = new JFileChooser(_fileChooserDirectory);
					}
					else {
						fc = new JFileChooser();
					}

					fc.setDialogType(JFileChooser.SAVE_DIALOG);
					fc.setMultiSelectionEnabled(false);

					fc.setDialogTitle(_saveErrorLogText);

					int returnVal = fc.showSaveDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						_fileChooserDirectory = file.getParentFile();
						try {
							saveLog(file, _errorTextArea.getText());
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				else if (source.getText().equals(_saveDebugLogText)) {

					final JFileChooser fc;
					if (_fileChooserDirectory != null) {
						// Es ist ein Verzeichnis bekannt
						fc = new JFileChooser(_fileChooserDirectory);
					}
					else {
						fc = new JFileChooser();
					}

					fc.setDialogType(JFileChooser.SAVE_DIALOG);
					fc.setMultiSelectionEnabled(false);

					fc.setDialogTitle(_saveDebugLogText);

					int returnVal = fc.showSaveDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						_fileChooserDirectory = file.getParentFile();
						try {
							saveLog(file, _debugTextArea.getText());
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				else if (source.getText().equals(_clearErrorLogText)) {
					_errorTextArea.replaceRange("", 0, _errorTextArea.getText().length());
				}
				else if (source.getText().equals(_clearDebugLogText)) {
					_debugTextArea.replaceRange("", 0, _debugTextArea.getText().length());
				}
				else if (source.getText().equals(_endText)) {
					System.exit(0);
				}
			}
		};

		// Wenn das Hauptfenster geschlossen wird, dann muss auch der laufende
		// Prozess beendet werden
		_mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				endProcess();
				System.exit(0);
			}
		});

		// Menueintr�ge erzeugen
		final JMenu file = new JMenu("Datei");
		file.setMnemonic(KeyEvent.VK_D);
		final JMenuItem endProgramm = new JMenuItem(_endText);
		endProgramm.addActionListener(actionProcCmd);

		file.add(endProgramm);
		_menuBar.add(file);
		final JMenu logs = new JMenu("Ausgaben");
		logs.setMnemonic(KeyEvent.VK_L);

		final JMenuItem saveDebug = new JMenuItem(_saveDebugLogText, KeyEvent.VK_G);
		saveDebug.setAccelerator(KeyStroke.getKeyStroke('G', Event.ALT_MASK));
		saveDebug.addActionListener(actionProcCmd);

		final JMenuItem saveError = new JMenuItem(_saveErrorLogText, KeyEvent.VK_E);
		saveError.setAccelerator(KeyStroke.getKeyStroke('E', Event.ALT_MASK));
		saveError.addActionListener(actionProcCmd);

		final JMenuItem clearDebug = new JMenuItem(_clearDebugLogText, KeyEvent.VK_1);
		clearDebug.setAccelerator(KeyStroke.getKeyStroke('1', Event.ALT_MASK));
		clearDebug.addActionListener(actionProcCmd);

		final JMenuItem clearError = new JMenuItem(_clearErrorLogText, KeyEvent.VK_2);
		clearError.setAccelerator(KeyStroke.getKeyStroke('2', Event.ALT_MASK));
		clearError.addActionListener(actionProcCmd);

		logs.add(saveDebug);
		logs.add(saveError);
		logs.addSeparator();
		logs.add(clearDebug);
		logs.add(clearError);
		_menuBar.add(logs);

		// Menu einf�gen
		_mainFrame.setJMenuBar(_menuBar);
	}

	/**
	 * Diese Methode erzeugt einen Prozess, der ausgef�hrt wird. Alle Ausgaben
	 * werden in der Oberfl�che dargestellt. Falls bereits ein Prozess existiert,
	 * wird dieser abgebrochen {@link #endProcess}  und ein neuer Prozess erzeugt.
	 * Der neue Prozess benutzt die �bergebenen Parameter.
	 *
	 * @param arguments        �bergabeparameter, die benutzt werden sollen oder
	 *                         null
	 * @param environment      Umgebungsvariablen oder null
	 * @param workingDirectory Arbeitsverzeichnis oder null
	 *
	 * @throws IOException siehe {@link Process} Fehlerbeschreibung
	 */
	public final void processScript(String[] arguments, String[] environment, File workingDirectory) throws IOException {

		// Die Klasse/Argumente/Umgebungsvariablen/Arbeitsverzeichnis werden gespeichert, falls der Prozess mit dem Standardpanel
		// neu gestartet werden soll. Wird ein Panel des Benutzers benutzt, werden diese Variablen nicht ben�tigt, da
		// das Panel des Benutzers daf�r sorgen muss, dass die richtigen Argumente �bergeben werden.
		_processArguments = arguments;
		_processEnvironment = environment;
		_processWorkingDirectory = workingDirectory;

		String fileSeparator = System.getProperty("file.separator");
		String javaHome = System.getProperty("java.home");
		String classPath = System.getProperty("java.class.path");

		List commandList = new LinkedList();
		commandList.add(javaHome + fileSeparator + "bin" + fileSeparator + "java");
		commandList.add("-Dfile.encoding=ISO-8859-1");
		commandList.add("-Xms32m");
		commandList.add("-Xmx712m");
		commandList.add("-cp");
		commandList.add(classPath);
		commandList.add(_className);
		if (arguments != null) commandList.addAll(Arrays.asList(arguments));

		final String[] commandArray = (String[]) commandList.toArray(new String[0]);

		//		StringBuffer attributeString = new StringBuffer();
		//
		//		attributeString.append("Der Prozess wird gestartet:" + "\n");
		//		attributeString.append("Klasse: " + _className + "\n");
		//
		//		if (arguments != null) {
		//			attributeString.append("Liste der �bergebenen Argumente: " + "\n");
		//			for (int nr = 0; nr < commandArray.length; nr++) {
		//				attributeString.append(commandArray[nr] + "\n");
		//			}
		//		} else {
		//			attributeString.append("Liste der �bergebenen Argumente ist leer" + "\n");
		//		}
		//
		//		if (environment != null) {
		//			attributeString.append("Liste der Umgebungsargumente: " + "\n");
		//			for (int nr = 0; nr < environment.length; nr++) {
		//				attributeString.append(environment[nr] + "\n");
		//			}
		//
		//		} else {
		//			attributeString.append("Liste der Umgebungsargumente ist leer" + "\n");
		//		}
		//
		//		if (workingDirectory != null) {
		//			attributeString.append("Arbeitsverzeichnis: " + workingDirectory.getAbsolutePath());
		//		} else {
		//			attributeString.append("Ein Arbeitsverzeichnis wurde nicht festgelegt" + "\n");
		//		}
		//
		//		// Zeilenumbruch
		//		attributeString.append("\n");
		//
		//		_debugTextArea.append(attributeString.toString());

		if (_process != null) {
			// Derzeit l�uft bereits ein Prozess, dieser wird abgebrochen
			endProcess();
			_errorThread.interrupt();
			_debugThread.interrupt();

			_process = Runtime.getRuntime().exec(commandArray, environment, workingDirectory);
			_errorThread = new Thread(new StreamReaderThread(_errorTextArea, _process.getErrorStream()));
			_debugThread = new Thread(new StreamReaderThread(_debugTextArea, _process.getInputStream()));

			// Die Threads f�r die Ausgabe werden gestartet (dies ist jetzt n�tig, da die Oberfl�che bereits
			// gestartet wurde)
			_errorThread.start();
			_debugThread.start();
		}
		else {
			_process = Runtime.getRuntime().exec(commandArray, environment, workingDirectory);
			_errorThread = new Thread(new StreamReaderThread(_errorTextArea, _process.getErrorStream()));
			_debugThread = new Thread(new StreamReaderThread(_debugTextArea, _process.getInputStream()));
			// Die Threads f�r die Ausgabe werden mit der Erstellung der Oberfl�che gestartet
			_debugThread.start();
			_errorThread.start();
		}

		// Falls das Arbeitsverzeichnis zum speichern der Protokolle noch nicht gesetzt wurde,
		// wird es auf das Arbeitsverzeichnis des Prozesses gesetzt.
		if (_fileChooserDirectory == null) {
			_fileChooserDirectory = workingDirectory;
		}
	}

	/**
	 * Diese Methode wertet die �bergebenen Argumente aus und zeigt falls gew�nscht
	 * die Oberfl�che an. Der zu startende Prozess kann entweder sofort gestartet
	 * werden oder aber erst sp�ter. Falls der Prozess nicht sofort gestartet wird,
	 * weil zum Beispiel auf eine Eingabe des Benutzer gewartet wird, muss der
	 * Prozess durch den Aufruf {@link #processScript} gestartet werden.
	 *
	 * @param args         Argumente, mit denen der Prozess gestartet werden soll.
	 *                     Die Argumente enthalten ausserdem die Information, ob
	 *                     die Oberfl�che angezeigt werden soll oder nicht
	 * @param startProcess true = Der in den Argumenten festgelegte Prozess wird
	 *                     sofort gestartet; false = Der in den Argumenten
	 *                     festgelegte Prozess wird nicht gestartet und muss mit
	 *                     dem Aufruf {@link #processScript} gestartet werden
	 */
	public final void start(String[] args, boolean startProcess) {
		ArgumentList argumentList = new ArgumentList(args);

		// Ist der Parameter vorhanden
		if (argumentList.hasArgument("-gui") == false) {
			final String errorString = "Der Parameter -gui= wurde nicht angegeben";
			JOptionPane.showMessageDialog(null, errorString, "Parameter -gui= fehlt", JOptionPane.ERROR_MESSAGE);

			// Es macht keinen Sinn weiterzumachen
			return;
		}

		// Wurde eine Klasse angegeben ? Falls ja, wird diese Ausgelesen
		if (argumentList.hasArgument("-prozessname") == false) {
			// Der Prozessname wurde nicht angegeben
			final String errorString = "Der Parameter -prozessname= wurde nicht angegeben";
			JOptionPane.showMessageDialog(null, errorString, "Parameter -prozessname= fehlt", JOptionPane.ERROR_MESSAGE);

			// Es macht keinen Sinn weiterzumachen
			return;
		}
		_className = argumentList.fetchArgument("-prozessname=").asString();

		// WorkingDirecotry auslesen
		File workingDirectory = null;

		if (argumentList.hasArgument("-arbeitsverzeichnis")) {
			// Es wurde ein Arbeitsverzeichnis angegeben
			workingDirectory = new File(argumentList.fetchArgument("-arbeitsverzeichnis").asString());
		}

		// Soll die Oberfl�che angezeigt werden (dies wird so gemacht da ein if (argumentList.fetchArgument("-gui=").booleanValue()))
		// zwar funktioniert hat, aber das Argument nicht aus der Liste entfernt wurde !
		final boolean showGUI = argumentList.fetchArgument("-gui=").booleanValue();

		// Es wurden alle Daten ausgelesen, die ben�tigt werden um einen Prozess zu starten.
		// Da beim rausfiltern der Argumente die urspr�ngeliche Argumenteliste ver�ndert wurde (es gibt
		// nun Eintr�ge die <code>null</code> sind, wird diese Liste bereinigt.
		_modifiedArgumentlist = cleanedArgumentArray(args);

		if (showGUI) {
			// Die Oberfl�che soll angezeigt werden

			if (startProcess) {
				// Den Prozess starten
				try {
					processScript(_modifiedArgumentlist, null, workingDirectory);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				showGUI();
			}
			else {
				// Es soll nur die GUI angezeigt werden, der Prozess wird nicht gestartet
				showGUI();
			}

		}
		else {
			// Die Oberfl�che soll nicht angezeigt werden

			if (startProcess) {
				// Der Prozess soll sofort gestartet werden
				try {
					processScript(_modifiedArgumentlist, null, workingDirectory);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				final String infoMessage = "Es soll keine Oberfl�che angezeigt werden und der in den Parametern " +
				        "festgelegte Prozess wird nicht gestartet. Es besteht keine M�glichkeit den Prozess nachtr�glich " +
				        "zu starten ! Bitte starten Sie den Prozess direkt, falls sie keine GUI sehen m�chten.";
				JOptionPane.showMessageDialog(null, infoMessage, "Prozess wird auf Wunsch des Benutzers nicht gestartet", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/**
	 * Stellt die GUI dar
	 */
	private void showGUI() {

		// Menu erstellen, ohne Hilfe
		JMenu entries[] = newMenuEntries();
		for (int nr = 0; nr < entries.length; nr++) {
			_menuBar.add(entries[nr]);
		}

		// Hilfemenu erzeugen
		_menuBar.add(createHelpMenuEntry());


		// Es wurde kein Benutzerpanel gesetzt, also wird ein Standardpanel erzeugt
		_userPanel = createUserPanel();

		_debugTextArea.setFont(_font);
		_errorTextArea.setFont(_font);

		// Split f�r die beiden Logs
		final JSplitPane logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(_debugTextArea), new JScrollPane(_errorTextArea));
		logSplit.setOneTouchExpandable(true);
		logSplit.setContinuousLayout(true);
		logSplit.setDividerLocation(200);

		// Split der das UserPanel und die Logs enth�lt
		final JSplitPane userPanelLogPanelSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _userPanel, logSplit);
		userPanelLogPanelSplit.setOneTouchExpandable(true);
		userPanelLogPanelSplit.setContinuousLayout(true);
		userPanelLogPanelSplit.setDividerLocation(_userPanel.getPreferredSize().height);

		// Split, der die Legende und UserPanel+Logs enth�lt
		_legendPanel = createLegendPanel();

		final JSplitPane legendPanelAndRest = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _legendPanel, userPanelLogPanelSplit);
		legendPanelAndRest.setOneTouchExpandable(true);
		legendPanelAndRest.setContinuousLayout(true);

		_mainFrame.getContentPane().add(legendPanelAndRest);

		_mainFrame.setTitle("K2S Prozess�berwachung: " + _className);
		_mainFrame.setSize(new Dimension(800, 600));
		_mainFrame.setVisible(true);
	}

	/**
	 * Dieses Panel wird auf der linken Seite der Oberfl�che dargestellt. Wird
	 * diese Methode nicht �berschrieben wird ein Standartpanel erzeugt.
	 *
	 * @return Panel, das auf der linken Seite der Oberfl�che dargestellt wird
	 */
	public JPanel createLegendPanel() {
		JPanel legendPanel = new JPanel(new BorderLayout());

		final Image k2sImage = new ImageIcon(SimpleApplicationGUI.class.getResource("K2S-LogoGross.png")).getImage();
		// Das neue Seitenverh�ltnis des Logos berechnen
		final int k2sHigh = 100;
		final ImageIcon k2sLogo = new ImageIcon(k2sImage.getScaledInstance(((k2sHigh * new ImageIcon(k2sImage).getIconWidth()) / new ImageIcon(k2sImage).getIconHeight()), k2sHigh, Image.SCALE_AREA_AVERAGING));

		legendPanel.add(new JLabel(k2sLogo), BorderLayout.NORTH);
		legendPanel.add(new JLabel(k2sLogo), BorderLayout.SOUTH);

		return legendPanel;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn der Benutzer kein eigenes Panel benutzen
	 * m�chte. Es wird das K2S Logo angezeigt und ein Button um den Prozess neu zu
	 * starten.
	 *
	 * @return Standard Panel, Funktionen s.o.
	 */
	public JPanel createUserPanel() {
		final JPanel standardPanel = new JPanel(new BorderLayout());

		final Image k2sImage = new ImageIcon(SimpleApplicationGUI.class.getResource("K2S-LogoGross.png")).getImage();
		// Das neue Seitenverh�ltnis des Logos berechnen
		final int k2sHigh = 200;
		final ImageIcon k2sLogo = new ImageIcon(k2sImage.getScaledInstance(((k2sHigh * new ImageIcon(k2sImage).getIconWidth()) / new ImageIcon(k2sImage).getIconHeight()), k2sHigh, Image.SCALE_AREA_AVERAGING));

		standardPanel.add(new JLabel(k2sLogo), BorderLayout.CENTER);

		// Knopf erzeugen und in das Panel einf�gen
		final JButton button = new JButton(_standardUserPanelButtonName);

		ActionListener actionProcCmd = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (e.getActionCommand().equals(_standardUserPanelButtonName)) {
					try {
						processScript(_processArguments, _processEnvironment, _processWorkingDirectory);
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};

		// Der Knopf soll in der Mitte platziert werden
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createGlue());
		buttonPanel.add(button);
		buttonPanel.add(Box.createGlue());
		button.addActionListener(actionProcCmd);

		standardPanel.add(buttonPanel, BorderLayout.SOUTH);
		return standardPanel;
	}

	/**
	 * Diese Methode gibt ein Array mit neuen Menueintr�gen zur�ck. Diese Eintr�ge
	 * werden dann sp�ter in das bestehende Menu eingef�gt. Die Menueintr�ge m�ssen
	 * komplett implementiert sein (Aktions, Shortcuts, usw.) da sie lediglich
	 * eingef�gt und nicht mehr modifiziert werden.
	 *
	 * @return Menueintr�ge, die in ein bestehendes Menu eingef�gt werden sollen
	 */
	public JMenu[] newMenuEntries() {
		return new JMenu[0];
	}

	private JMenu createHelpMenuEntry() {

		// �berschrift des Menus
		final String helpMenuName = "Hilfe";
		// Eintr�ge im Menu
		final String helpItemName = "Hilfe...";
		final String aboutItemName = "�ber";

		final JMenu newMenu = new JMenu(helpMenuName);

		ActionListener actionProcCmd = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JMenuItem source = (JMenuItem) (e.getSource());

				if (source.getText().equals(aboutItemName)) {
					showAboutBox();
				}
				else if (source.getText().equals(helpItemName)) {
					showHelp();
				}
			}
		};

		final JMenuItem helpEntry = new JMenuItem(helpItemName);
		// Wenn keine Hilfedatei vorhanden ist, wird der Menueintrag ausgegraut dargestellt

		// Es wird die Klasse benutzt, die das Abstract benutzt um an die Hilfedatei zu kommen
		if (getClass().getResource("Hilfe.html") == null) {
			helpEntry.setEnabled(false);
		}
		helpEntry.addActionListener(actionProcCmd);
		newMenu.add(helpEntry);
		newMenu.addSeparator();
		final JMenuItem aboutEntry = new JMenuItem(aboutItemName);
		aboutEntry.addActionListener(actionProcCmd);
		newMenu.add(aboutEntry);
		return newMenu;
	}

	/**
	 * Zeigt eine Hilfedatei(HTML) an
	 */
	private void showHelp() {
		final JFrame helpMainframe = new JFrame();

		final JPanel helpPanel = new JPanel(new BorderLayout());

		JEditorPane editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);

		try {
			// Es wird die Klasse benutzt, die das Abstract benutzt um an die Hilfedatei zu kommen
			editorPane.setPage(getClass().getResource("Hilfe.html"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		helpPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);

		helpMainframe.getContentPane().add(helpPanel);
		helpMainframe.setTitle("Hilfe");
		helpMainframe.setSize(new Dimension(300, 300));
		helpMainframe.setVisible(true);

	}

	/**
	 * Diese Methode zeigt ein "�ber" Fenster als JFrame.
	 */
	public void showAboutBox() {
		final JFrame aboutMainframe = new JFrame();
		final JPanel aboutPanel = new JPanel(new BorderLayout());
		final JPanel textAndLogo = new JPanel(new BorderLayout());

		// Dies wird gleichzeitig der Titel des Fensters
		final String productName = "";

		final String aboutText = "<HTML><small>Copyright 2005 Kappich+Kni� Systemberatung Aachen (K2S) <br> <b> ALL RIGHTS RESERVED </b></small></HTML>";

		final Image k2sImage = new ImageIcon(SimpleApplicationGUI.class.getResource("K2S-LogoGross.png")).getImage();
		// Das neue Seitenverh�ltnis des Logos berechnen

		// H�he des Bildes, die Breite wird angepa�t
		final int k2sHigh = 100;
		final ImageIcon k2sLogo = new ImageIcon(k2sImage.getScaledInstance(((k2sHigh * new ImageIcon(k2sImage).getIconWidth()) / new ImageIcon(k2sImage).getIconHeight()), k2sHigh, Image.SCALE_AREA_AVERAGING));

		textAndLogo.add(new JLabel(productName), BorderLayout.NORTH);
		textAndLogo.add(new JLabel(aboutText), BorderLayout.CENTER);
		textAndLogo.add(new JLabel(k2sLogo), BorderLayout.SOUTH);

		textAndLogo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		aboutPanel.add(textAndLogo, BorderLayout.CENTER);

		final String okButtonName = "Schliessen";

		final JButton okButton = new JButton(okButtonName);

		ActionListener actionProcCmd = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (e.getActionCommand().equals(okButtonName)) {
					aboutMainframe.setVisible(false);
					aboutMainframe.dispose();
				}
			}
		};

		okButton.addActionListener(actionProcCmd);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createGlue());
		buttonPanel.add(okButton);

		aboutPanel.add(buttonPanel, BorderLayout.SOUTH);

		aboutMainframe.getContentPane().add(aboutPanel);
		// Das Fenster bekommt den Namen des Produkts
		aboutMainframe.setTitle(productName);
		aboutMainframe.pack();
		aboutMainframe.setResizable(false);
		aboutMainframe.setVisible(true);

	}

	/**
	 * Der Aufruf dieser Methode gibt den Prozess zur�ck, der derzeit ausgef�hrt
	 * wird. Wurde noch kein Prozess gestartet, wird <code>null</code>
	 * zur�ckgegeben.
	 *
	 * @return Prozess, der ausgef�hrt wird oder <code>null</code> falls noch kein
	 *         Prozess gestartet wurde
	 */
	public final Process getProcess() {
		return _process;
	}

	/**
	 * Diese Methode wird aufgerufen, falls ein Prozess beendet werden soll. Falls
	 * diese Methode �berschrieben werden soll, muss zuerst der aktuelle Prozess
	 * mit {@link #getProcess} angefordert werden um das Objekt des aktuellen
	 * Prozzeses zu erhalten.
	 */
	public void endProcess() {
		final Process process = getProcess();
		if (process != null) {
			process.destroy();
		}
	}

	/**
	 * Diese Methode speichert einen �bergebenen String in eine Datei, die
	 * ebenfalls �bergeben wird
	 *
	 * @param file Datei, in die geschrieben werden soll
	 * @param log  Text, der geschrieben werden soll
	 */
	private void saveLog(File file, String log) throws IOException {
		RandomAccessFile randomFile = null;
		try {
			randomFile = new RandomAccessFile(file, "rw");
			randomFile.writeBytes(log);
		}
		finally {
			if (randomFile != null) {
				randomFile.close();
			}
		}
	}

	/**
	 * Diese Methode gibt eine �berarbeitete Argumentliste zur�ck. Die �berarbeitet
	 * Argumentliste ist dabei aus der Argumentliste entstanden, die in der Methode
	 * {@link #start} �bergeben wurde. Es wurden folgende Argumente entfernt:<br>
	 * -gui<br> -prozessname<br> -arbeitsverzeichnis<br> Falls ein Prozess diese
	 * Argumentliste mit {@link de.bsvrz.sys.funclib.commandLineArgs.ArgumentList#fetchArgument} bearbeitet,
	 * werden die Elemente aus der Liste entfernt und nicht wieder eingef�gt, auch
	 * die enstehenden <code>null</code> Eintr�ge bleiben in dieser Argumentliste
	 * bestehen!
	 *
	 * @return modifizierte Argumentliste
	 */
	public final String[] getArguments() {
		return _modifiedArgumentlist;
	}

	/**
	 * Diese Methode filtert aus einer Argumentenliste alle Eintr�ge heraus, die
	 * <code>null</code> sind und gibt ein bereinigtes Array zur�ck.
	 *
	 * @param argumentList Array mit Argumenten, es k�nnen ebenfalls Eintr�ge
	 *                     <code>null</code> sein
	 *
	 * @return Array mit Argumenten, es gibt keine Eintr�ge, die <code>null</code>
	 *         sind
	 */
	private String[] cleanedArgumentArray(final String[] argumentList) {
		// In dieser Liste werden nu Argumente eingetragen, die nicht null sind
		final LinkedList<String> cleanedArgumentList = new LinkedList<String>();
		for (int nr = 0; nr < argumentList.length; nr++) {
			if (argumentList[nr] != null) {
				cleanedArgumentList.add(argumentList[nr]);
			}
		}
		return (String[]) cleanedArgumentList.toArray(new String[0]);
	}

	/**
	 * Diese Klasse stellt einen Stream in einem daf�r vorgesehenem Fenster dar.
	 */
	private static final class StreamReaderThread implements Runnable {

		private final JTextArea _textPane;
		private final InputStreamReader _inputReader;
		private static final int MAX_TEXT_LENGTH = 1000000;

		public StreamReaderThread(JTextArea textPane, InputStream inputStream) {
			_textPane = textPane;
			try {
				_inputReader = new InputStreamReader(inputStream, "ISO-8859-1");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new RuntimeException(e); 
			}
		}

		public void run() {
			try {
				while (Thread.currentThread().isInterrupted() == false) {
					char[] buffer = new char[1000];
					int got = _inputReader.read(buffer);
					if (got < 0) {
						//System.err.println("EOF");
						break;
					}
					//                    System.out.println("got = " + got);
					append(new String(buffer, 0, got));
					//					String slowAppendResult = _textPane.getText() + new String(buffer);
					//					_textPane.setText(slowAppendResult);
				}
			}
			catch (IOException e) {
				//                e.printStackTrace();
				//                System.err.println("I/O Error");
			}
		}

		private void append(final String string) {
			invokeAndWait(new Runnable() {
				public void run() {
					int textLength = _textPane.getDocument().getLength();
					int selectionStart = _textPane.getSelectionStart();
					int selectionEnd = _textPane.getSelectionEnd();
					if (textLength > MAX_TEXT_LENGTH) {
						// Zuviel Text => obere H�lfte l�schen
						final int deleteLength = textLength / 2;
						_textPane.select(0, deleteLength);
						_textPane.replaceSelection("(Anfang gel�scht)...");
						final int newLength = _textPane.getDocument().getLength();
						final int deletedLength = textLength - newLength;
						selectionStart -= deletedLength;
						selectionEnd -= deletedLength;
						textLength = newLength;
						// wenn Selektion im gel�schten Bereich lag, dann Selektion auf Ende setzen
						if (selectionStart < 0 || selectionEnd < 0) {
							selectionStart = textLength;
							selectionEnd = textLength;
						}
					}
					//	                System.out.println("textLength = " + textLength);
					//	                System.out.println("selectionStart = " + selectionStart);
					//	                System.out.println("selectionEnd = " + selectionEnd);
					_textPane.select(textLength, textLength);
					_textPane.replaceSelection(string);
					if (textLength != selectionStart || textLength != selectionEnd) {
						_textPane.select(selectionStart, selectionEnd);
					}
				}
			});
		}

		private void invokeAndWait(Runnable runnable) {
			if (SwingUtilities.isEventDispatchThread()) {
				runnable.run();
			}
			else {
				try {
					SwingUtilities.invokeAndWait(runnable);
				}
				catch (InvocationTargetException e) {
					e.printStackTrace();
					throw new RuntimeException(e); 
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

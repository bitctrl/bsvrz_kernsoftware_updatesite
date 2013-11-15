/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.main;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModule;
import de.bsvrz.pat.sysbed.plugins.archiverequest.StreamBasedArchiveRequestModule;
import de.bsvrz.pat.sysbed.plugins.configdata.ConfigurationDataModule;
import de.bsvrz.pat.sysbed.plugins.datareceiver.ShowCurrentDataModule;
import de.bsvrz.pat.sysbed.plugins.datasender.SendCurrentDataModule;
import de.bsvrz.pat.sysbed.plugins.datgen.DatGenModule;
import de.bsvrz.pat.sysbed.plugins.onlinetable.OnlineTableModule;
import de.bsvrz.pat.sysbed.plugins.onlprot.OnlineProtocolModule;
import de.bsvrz.pat.sysbed.plugins.parameditor.ParameterEditorModule;
import de.bsvrz.pat.sysbed.plugins.sysprot.SystemProtocolModule;
import de.bsvrz.pat.sysbed.preselection.tree.TreeNodeObject;
import de.bsvrz.sys.funclib.application.AbstractGUIApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Mit dieser Klasse wird die Anwendung "Generischer Test Monitor" gestartet. Die {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Module}, die zu der
 * Applikation gehören sollen, können hier hinzugefügt werden. Der Auswahlbaum wird hier für den {@link de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree
 * PreselectionTree} erstellt und an die Applikation übergeben. Außerdem wird die Verbindung zum Dateverteiler und die Argumentliste, welche beim Aufruf der
 * <code>main</code>-Methode angegeben wurde, übergeben.
 * <p/>
 * Der Login-Dialog, welcher es ermöglicht, eine IP-Adresse mit Port, Benutzernamen und Passwort einzugeben, kann über den Aufrufparameter
 * <code>-autologin</code> ausgeschaltet werden. Allerdings werden dann die Parameter <code>-benutzer</code> und <code>-authentifizierung</code> benötigt.
 * 
 * @author Kappich Systemberatung
 */
//public class GenericTestMonitor implements StandardApplication {
public class GenericTestMonitor extends AbstractGUIApplication {
	
	/** Der Debug-Logger */
	private static final Debug _debug = Debug.getLogger();
	
	/** speichert das "Logo" */
	private Icon _logo = null;
	
	/** speichert die Argumente, die beim Aufruf übergeben wurden */
	private static final List<String> _argumentList = new LinkedList<String>();
	
	/** speichert die Objekt-Pids der Argumente */
	private String[] _objects;
	
	/** speichert die Plugins */
	private String[] _plugins;
	
	/* ############### Methoden ################## */

	/**
	 * Die Applikation "Generischer Test Monitor" wird über die <code>main</code>-Methode gestartet.
	 * 
	 * @param args
	 *            Argumente, die beim Start der Applikation übergeben wurden
	 */
	public static void main(String[] args) {
		for(int i = 0; i < args.length; i++) { // vor dem Aufruf von run - da sonst ein Teil der Argumente fehlen
			String arg = args[i];
			if(arg.startsWith("-autologin") || arg.startsWith("-plugins")) {
				// do nothing - Aufrufparameter wird hier ausgewertet und aus der Argumentliste rausgefiltert
			}
			else {
				_argumentList.add(arg);
			}
		}
		
		
		StandardApplicationRunner.run(new GenericTestMonitor(), args);
	}
	
	/** Öffentlichen Konstruktor dieser Klasse überschrieben, damit kein Objekt dieser Klasse erstellt werden kann. */
	private GenericTestMonitor() {
	}
	
	public String getApplicationName() {
		return "Kappich Systemberatung - Generischer Testmonitor";
	}
	
	/**
	 * Mit der <code>main</code>-Methode übergebene Parameter können hier ausgewertet werden.
	 * 
	 * @param argumentList
	 *            die modifizierte Argumentliste von der Standardapplikation
	 *
	 * @throws Exception
	 *             Falls ein ungültiges Argument gefunden wurde.
	 */
	public void parseArguments(ArgumentList argumentList) throws Exception {
		// der ConfigurationHelper könnte hier genutzt werden, allerdings wird dort das "," als Separator verwendet.
		String argument = argumentList.fetchArgument("-objekt=datenAuswahl.TestMenü01").asNonEmptyString();
		
		String plugins = argumentList.fetchArgument("-plugins=").asString();
		
		_plugins = plugins.split(",");
		_objects = argument.split(";");
	}
	
	/**
	 * Die Applikation wird erstellt, Module und Logo hinzugefügt und der Baum für die {@link de.bsvrz.pat.sysbed.preselection.panel.PreselectionPanel
	 * Datenidentifikationsauswahl} wird erstellt. Anschließend wird die Anwendung gestartet.
	 * 
	 * @param connection
	 *            Verbindung zum Datenverteiler
	 * 
	 * @throws Exception
	 *             Falls es zu einer unerwarteten Ausnahme kommt.
	 */
	public void initialize(final ClientDavInterface connection) throws Exception {
		// die eingegebenen Werte des Login-Dialogs werden ausgelesen und die entsprechenden Aufrufargumente ersetzt.
		final ClientDavParameters davParameters = connection.getClientDavParameters();
		final String davString = "-datenverteiler=" + davParameters.getDavCommunicationAddress() + ":" + davParameters.getDavCommunicationSubAddress();
		final String userName = "-benutzer=" + connection.getLocalUser().getName();
		for(String s : _argumentList) {
			if(s.startsWith("-datenverteiler=")) {
				final int i = _argumentList.indexOf(s);
				_argumentList.set(i, davString);
			}
			else if(s.startsWith("-benutzer=")) {
				final int i = _argumentList.indexOf(s);
				_argumentList.set(i, userName);
			}
		}
		
		_debug.info("Durch Login-Dialog geänderte Aufrufargumente", _argumentList);
		
		try {
			_logo = new ImageIcon(GenericTestMonitorApplication.class.getResource("kappich-logo.png"));
		}
		catch(Exception ex) {
			_debug.warning("Logo konnte nicht geladen werden!");
		}
		
		final Collection<Object> treeNodes = createTreeNodeObjects(connection);
		startGenericTestMonitor(connection, treeNodes);
	}
	
	/**
	 * Erstellt anhand der Aufrufparameter den Vorauswahlbaum.
	 * 
	 * @param connection
	 *            Verbindung zum Datenverteiler
	 * 
	 * @return der Vorauswahlbaum
	 */
	private Collection<Object> createTreeNodeObjects(final ClientDavInterface connection) {
		DataModel dataModel = connection.getDataModel();
		
		TreeNodeObject treeNodeObject01 = new TreeNodeObject("Alles", "alles");
		
//		TreeNodeObject treeNodeObject02 = new TreeNodeObject("mq.a10.0000");
//		Filter filter04 = new Filter(Filter.OBJECT, new String[]{"mq.a10.0000"}, connection);
//		treeNodeObject02.addFilter(filter04);
		
		// Collection darf TreeNodeObject und SystemObject enthalten
		final Collection<Object> treeNodes = new LinkedList<Object>();
		treeNodes.add(treeNodeObject01);
//		treeNodes.add(treeNodeObject02);
		
		// Objekte des Aufrufparameters einfügen
		for(int i = 0; i < _objects.length; i++) {
			String object = _objects[i].trim();
			SystemObject systemObject = dataModel.getObject(object);
			if(systemObject != null) {
				treeNodes.add(systemObject);
			}
		}
		return treeNodes;
	}

	private static class ProgressViewer {

		private JDialog _dialog = null;

		private JLabel _label;

		public void showProgress(final String progressMessage) {
			System.out.println("Bitte warten: " + progressMessage);
			if(!GraphicsEnvironment.isHeadless()) {
				if(_dialog == null) {
					_dialog = new JDialog();
					_label = new JLabel(progressMessage, UIManager.getIcon("OptionPane.informationIcon"), SwingConstants.LEFT);
					_label.setBorder(new EmptyBorder(10, 10, 10, 10));
					final Container contentPane = _dialog.getContentPane();
					contentPane.add(_label, BorderLayout.CENTER);
					final JProgressBar bar = new JProgressBar();
					bar.setBorder(new EmptyBorder(10, 10, 10, 10));
					bar.setIndeterminate(true);
					contentPane.add(bar, BorderLayout.SOUTH);
					_dialog.pack();
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					Rectangle abounds = _dialog.getBounds();
					_dialog.setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
					_dialog.show();
				}
				else {
					_label.setText(progressMessage);
					_label.revalidate();
				}
			}
		}

		public void close() {
			if(_dialog != null) _dialog.setVisible(false);
		}
	}


	/**
	 * Startet den Generischen TestMonitor und übergibt die Verbindung zum Datenverteiler und den Vorauswahlbaum.
	 * 
	 * @param connection
	 *            Verbindung zum Datenverteiler
	 * @param treeNodes
	 *            Knoten des Vorauswahlbaumes
	 */
	private void startGenericTestMonitor(final ClientDavInterface connection, final Collection<Object> treeNodes) {

		final ProgressViewer progressViewer = new ProgressViewer();
		progressViewer.showProgress("Konfigurationsobjekte werden geladen");
		connection.getDataModel().getType("typ.konfigurationsObjekt").getObjects();
//		progressViewer.showProgress("Dynamische Objekte werden geladen");
//		connection.getDataModel().getType("typ.dynamischesObjekt").getObjects();
		progressViewer.close();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Toolkit.getDefaultToolkit().setDynamicLayout(true);
					final GenericTestMonitorApplication application = new GenericTestMonitorApplication(getApplicationName(), connection, treeNodes);
					application.setArgumentList(_argumentList);
					if(_logo != null) {
						application.addLogo(_logo);
					}
					else {
						_debug.warning("Logo wurde nicht gefunden!");
					}
					
					application.addSeparator();
					application.addModule(new ParameterEditorModule());
					application.addModule(new OnlineTableModule());
					application.addModule(new ConfigurationDataModule());
					application.addModule(new ShowCurrentDataModule());
					application.addModule(new SendCurrentDataModule());
					application.addModule(new StreamBasedArchiveRequestModule());
					application.addSeparator();
					application.addModule(new SystemProtocolModule());
					application.addModule(new OnlineProtocolModule());
					application.addModule(new DatGenModule());
					
					// Plugins ermitteln und hinzufügen
					for(String plugin : _plugins) {
						if(!plugin.equals("")) {
							try {
	                            ExternalModule externalModule = ((Class<ExternalModule>)Class.forName(plugin)).newInstance();
	                            application.addModule(externalModule);
                            }
                            catch(ClassNotFoundException e) {
                            	JOptionPane.showMessageDialog(null,
                            		    "Die von Ihnen angegebene Plugin-Klasse " + plugin +  " existiert nicht.",
                            		    "Warnung",
                            		    JOptionPane.ERROR_MESSAGE);

                            }
						}
					}
					
					application.start();
				}
				catch(Exception ex) {
					_debug.error("In der Initialisierungsphase ist eine unerwartete Ausnahme im GTM aufgetreten (siehe exception). Der GTM wird beendet.", ex);
					ex.printStackTrace(); // Falls ein Fehler vorkommt, kann die Applikation abgebrochen werden.
					System.exit(1);
				}
			}
		});
	}
}

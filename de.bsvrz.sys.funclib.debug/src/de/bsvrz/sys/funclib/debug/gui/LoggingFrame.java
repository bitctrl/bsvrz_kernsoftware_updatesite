/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.debug.
 * 
 * de.bsvrz.sys.funclib.debug is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.debug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.debug; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.debug.gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.logging.*;


public class LoggingFrame extends JFrame implements TreeSelectionListener, ItemListener {

    Logger myLogger = null;

    public LoggingFrame() {
        // Logging für DIESE Klasse aufsetzen
        myLogger = Logger.getLogger(this.getClass().getPackage().getName());
        myLogger.setLevel(Level.ALL);
        myLogger.fine("Initialisiere den Log-Frame");

        // Komponenten initialisieren
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jSplitPane1 = new JSplitPane();
        jScrollPane1 = new JScrollPane();
        treeLogger = new JTree();
        jPanel1 = new JPanel();
        labelName = new JLabel();
        labelFilter = new JLabel();
        labelLevel = new JLabel();
        labelHandler = new JLabel();
        inputName = new JTextField();
        inputFilter = new JTextField();
        inputLevel = new JComboBox();
        jScrollPane2 = new JScrollPane();
        listHandler = new JList();
        jPanel2 = new JPanel();
        refresh = new JButton();
        close = new JButton();

        setTitle("Verwaltung Logger");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(5);
        jScrollPane1.setMinimumSize(new Dimension(205, 22));
        jScrollPane1.setPreferredSize(new Dimension(205, 363));
        treeLogger.setMaximumSize(new Dimension(1000, 1000));
        treeLogger.setMinimumSize(new Dimension(200, 0));
        treeLogger.setPreferredSize(new Dimension(200, 72));
        treeLogger.setModel(generateLoggerTreeModel());
        treeLogger.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeLogger.addTreeSelectionListener(this);
        treeLogger.setCellRenderer(new LoggerTreeCellRenderer());
        jScrollPane1.setViewportView(treeLogger);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jPanel1.setLayout(new GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EmptyBorder(new Insets(4, 4, 0, 4)), new javax.swing.border.TitledBorder("Logger Details")));
        labelName.setText("Name:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 5, 0, 5);
        jPanel1.add(labelName, gridBagConstraints);

        labelFilter.setText("Filter:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 5, 0, 5);
        jPanel1.add(labelFilter, gridBagConstraints);

        labelLevel.setText("Log-Level:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 5, 0, 5);
        jPanel1.add(labelLevel, gridBagConstraints);

        labelHandler.setText("Handler:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(3, 5, 3, 5);
        jPanel1.add(labelHandler, gridBagConstraints);

        inputName.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(3, 0, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(inputName, gridBagConstraints);

        inputFilter.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 0, 0, 5);
        jPanel1.add(inputFilter, gridBagConstraints);

        inputLevel.setEnabled(false);
        inputLevel.addItem("<nicht gesetzt>");
        inputLevel.addItem(Level.ALL);
        inputLevel.addItem(Level.FINEST);
        inputLevel.addItem(Level.FINER);
        inputLevel.addItem(Level.FINE);
        inputLevel.addItem(Level.CONFIG);
        inputLevel.addItem(Level.INFO);
        inputLevel.addItem(Level.WARNING);
        inputLevel.addItem(Level.SEVERE);
        inputLevel.addItem(Level.OFF);
        inputLevel.setSelectedItem(null);
        inputLevel.addItemListener(this);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 0, 0, 5);
        jPanel1.add(inputLevel, gridBagConstraints);

        listHandler.setModel(new DefaultComboBoxModel());

        jScrollPane2.setViewportView(listHandler);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(3, 0, 5, 5);
        jPanel1.add(jScrollPane2, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel1);

        getContentPane().add(jSplitPane1, BorderLayout.CENTER);

        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

        refresh.setMnemonic('A');
        refresh.setText("Aktualisieren");
        refresh.setActionCommand("refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                treeLogger.setModel(generateLoggerTreeModel());
            }
        });

        close.setMnemonic('S');
        close.setText("Schließen");
        close.setActionCommand("close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        jPanel2.add(refresh);
        jPanel2.add(close);
        getContentPane().add(jPanel2, BorderLayout.SOUTH);

        pack();
    }

    // Deklaration der Variablen
    private JButton close;
    private JPanel jPanel2;
    private JPanel jPanel1;
    private JLabel labelHandler;
    private JTextField inputName;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane1;
    private JLabel labelLevel;
    private JTextField inputFilter;
    private JLabel labelName;
    private JButton refresh;
    private JComboBox inputLevel;
    private JSplitPane jSplitPane1;
    private JTree treeLogger;
    private JList listHandler;
    private JLabel labelFilter;
    // End of variables declaration

    private TreeModel generateLoggerTreeModel() {

        myLogger.fine("Erzeuge Tree-Model mit allen Loggern");
        // Das Tree-Model mitsamt Root-Knoten anlegen
        TreeNodeObject tno = new TreeNodeObject("Alle Logger");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tno);
        DefaultTreeModel tree = new DefaultTreeModel(root);

        // Alle Logger in den Tree schreiben
        Enumeration loggerNames = LogManager.getLogManager().getLoggerNames();
        while(loggerNames.hasMoreElements()) {
            String name = (String)loggerNames.nextElement();

            myLogger.fine("Neuer Logger: " + name);
            if (name.length() == 0)
                continue;
            // Den Baum "Teil"-weise durchlaufen und ggf.
            // neue Knoten anlegen.
            String[] parts = name.split("\\.");
            DefaultMutableTreeNode currentNode = root;
            DefaultMutableTreeNode childNode;

            for (int i=0; i<parts.length; i++) {

                int childCount = currentNode.getChildCount();

                // Testen, ob es diesen Knoten bereits gibt.
                boolean setFlag = false;
                for (int j=0; j<childCount; j++) {
                    childNode = (DefaultMutableTreeNode)currentNode.getChildAt(j);
                    TreeNodeObject content = (TreeNodeObject)childNode.getUserObject();
                    // Der getestet Knoten liegt alphabetisch hinter dem
                    // neuen Knoten. Daher wird er jetzt an diesem Index
                    // angelegt.
                    if (content.getName().compareTo(parts[i]) > 0) {
                        myLogger.fine("Logger wird in Liste eingefügt");
                        childNode = createNode(parts, i);
                        currentNode.insert(childNode, j);
                        currentNode = childNode;
                        setFlag = true;
                        break;
                    }
                    else if (content.getName().equals(parts[i])) {
                        myLogger.fine("Pfad für Logger existiert bereits");
                        checkNode(parts, i, childNode);
                        currentNode = childNode;
                        setFlag = true;
                        break;
                    }
                }

                // Noch hier? Dann muss der Knoten angelegt werden
                if (!setFlag) {
                    myLogger.fine("Logger wird an Liste angehängt");
                    childNode = createNode(parts, i);
                    currentNode.add(childNode);
                    currentNode = childNode;
                }
            }
        }
        return tree;
    }

    private DefaultMutableTreeNode createNode(String[] parts, int index) {
        TreeNodeObject object = null;
        if (index == parts.length-1) {
            String name = "";
            for (int i=0; i<parts.length; i++) {
                if (i!=0)
                    name += ".";
                name += parts[i];
            }
            object = new TreeNodeObject(Logger.getLogger(name), parts[index]);
        }
        else
            object = new TreeNodeObject(parts[index]);
        return new DefaultMutableTreeNode(object);
    }

    private void checkNode(String[] parts, int index, DefaultMutableTreeNode node) {
        if (index == parts.length-1) {
            String name = "";
            for (int i=0; i<parts.length; i++) {
                if (i!=0)
                    name += ".";
                name += parts[i];
            }
            node.setUserObject(new TreeNodeObject(Logger.getLogger(name), parts[index]));
        }
    }

    public void valueChanged(TreeSelectionEvent evt) {
        // Die Combobox mit den Leveln wird evtl. verändert. Das wollen wir aber nicht wissen.
        inputLevel.removeItemListener(this);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)evt.getPath().getLastPathComponent();
        TreeNodeObject object = (TreeNodeObject)node.getUserObject();
        if (object.isLogger()) {
            myLogger.fine("Ein Logger wurde selektiert");
            Logger logger = object.getLogger();
            inputName.setText(logger.getName());
            inputFilter.setText(logger.getFilter()!=null?logger.getFilter().getClass().getName():"<kein Filter>");
            if (logger.getLevel() == null)
                inputLevel.setSelectedIndex(0);
            else
                inputLevel.setSelectedItem(logger.getLevel());
            inputLevel.setEnabled(true);
            DefaultComboBoxModel model = (DefaultComboBoxModel)listHandler.getModel();
            model.removeAllElements();
            Handler[] hndl = logger.getHandlers();
            for (int i=0; i<hndl.length; i++)
                model.addElement(hndl[i].getClass().getName() + " / " + hndl[i].getLevel());
        }
        else {
            myLogger.fine("Ein Knoten (kein Logger) wurde selektiert");
            inputName.setText("");
            inputFilter.setText("");
            inputLevel.setSelectedItem(null);
            inputLevel.setEnabled(false);
            DefaultComboBoxModel model = (DefaultComboBoxModel)listHandler.getModel();
            model.removeAllElements();
        }

        // Jetzt wieder Nachrichten von der Combobox abhören
        inputLevel.addItemListener(this);
    }

    public void itemStateChanged(ItemEvent evt) {
        // Wenn nur eine Auswahl rückgängig gemacht wurde, interessiert uns das nicht.
        if (evt.getStateChange() == ItemEvent.DESELECTED)
            return;
        Object newLevel = evt.getItem();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeLogger.getSelectionPath().getLastPathComponent();
        TreeNodeObject object = (TreeNodeObject)node.getUserObject();
        if (object.isLogger()) {
            myLogger.fine("Der Logger bekommt einen neuen Log-Level");
            if (newLevel instanceof Level)
                object.getLogger().setLevel((Level)newLevel);
            else
                object.getLogger().setLevel(null);
            repaint();
        }
    }

}




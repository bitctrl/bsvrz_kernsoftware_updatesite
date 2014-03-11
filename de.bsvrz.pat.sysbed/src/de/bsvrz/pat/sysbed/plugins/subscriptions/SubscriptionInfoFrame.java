/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.plugins.subscriptions;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.SystemObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11390 $
 */
public class SubscriptionInfoFrame extends JFrame{

	private final JComboBox _davObjSelection;

	private Container _pane;

	private final ClientDavInterface _connection;

	private final SystemObject _systemObject;

	private final AttributeGroup _attributeGroup;

	private final Aspect _aspect;

	private final int _simulationVariant;

	private SubscriptionsPanel _subscriptionsPanel;

	public SubscriptionInfoFrame(
			final ClientDavInterface connection,
			final SystemObject systemObject,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			final int simulationVariant) {
		_connection = connection;
		_systemObject = systemObject;
		_attributeGroup = attributeGroup;
		_aspect = aspect;
		_simulationVariant = simulationVariant;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Anmeldungsinfo");

		_pane = getContentPane();
		_pane.setLayout(new BorderLayout());

		_pane.add(getHeaderPanel(_attributeGroup, _aspect, _systemObject), BorderLayout.NORTH);
		_subscriptionsPanel = new SubscriptionsPanel(
				(ClientDavConnection)_connection, systemObject, _attributeGroup.getAttributeGroupUsage(_aspect), (short)simulationVariant, _connection.getLocalDav()
		);
		_pane.add(_subscriptionsPanel, BorderLayout.CENTER);

		final JPanel buttonpanel = new JPanel();
		buttonpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JButton refreshButton = new JButton("Aktualisieren");
		refreshButton.addActionListener(new RefreshListener());
		java.util.List<SystemObject> davObjects = _connection.getDataModel().getType(Pid.Type.DAV_APPLICATION).getObjects();
		Object[] objects = davObjects.toArray(new Object[davObjects.size()]);
		Arrays.sort(objects);
		_davObjSelection = new JComboBox(objects);
		_davObjSelection.setEditable(false);
		_davObjSelection.setSelectedItem(_connection.getLocalDav());
		buttonpanel.add(_davObjSelection);
		buttonpanel.add(refreshButton);
		_pane.add(buttonpanel, BorderLayout.SOUTH);
	}

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
		panel.setBorder(BorderFactory.createTitledBorder("Datenidentifikation"));
		panel.add(headerPanel, BorderLayout.WEST);

		return panel;
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

	public void start(){
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}


	private class RefreshListener implements ActionListener {

		public void actionPerformed(final ActionEvent e) {
			final SubscriptionsPanel subscriptionsPanel = new SubscriptionsPanel(
					(ClientDavConnection)_connection, _systemObject, _attributeGroup.getAttributeGroupUsage(_aspect), (short)_simulationVariant,
					(DavApplication)_davObjSelection.getSelectedItem()
			);
			_pane.remove(_subscriptionsPanel);
			_pane.add(subscriptionsPanel, BorderLayout.CENTER);
			_subscriptionsPanel = subscriptionsPanel;
			pack();
		}
	}

}

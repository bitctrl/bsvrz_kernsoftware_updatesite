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

import de.bsvrz.dav.daf.main.ApplicationSubscriptionInfo;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.DavApplication;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static de.bsvrz.pat.sysbed.plugins.subscriptions.SubscriptionsPanel.stateToString;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11467 $
 */
public class AppSubscriptionsPanel extends JPanel {

	private final JSplitPane _jSplitPane;

	private final JEditorPane _label;

	private final JList _senderList;

	private final JList _receiverList;


	public AppSubscriptionsPanel(
			final ClientDavConnection connection, final ClientApplication clientApplication, final DavApplication dav) {
		super(new BorderLayout());
		_jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		ApplicationSubscriptionInfo subscriptionInfo;
		try {
			subscriptionInfo = connection.getSubscriptionInfo(
					dav, clientApplication
			);
		}
		catch(IOException e) {
			subscriptionInfo = null;
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Konnte die Anmeldungen nicht auflisten. " + e.getMessage());
		}
		final TitledBorder sendBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sende-Anmeldungen");
		final TitledBorder receiveBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Empfangs-Anmeldungen");
		final TitledBorder labelBorder = BorderFactory.createTitledBorder("Details");
		final JComponent paneSend = new JPanel(new BorderLayout());
		_senderList = new JList(new MyListModel(subscriptionInfo == null ? Collections.emptyList() : subscriptionInfo.getSenderSubscriptions()));
		paneSend.add(new JScrollPane(_senderList), BorderLayout.CENTER);
		final JComponent paneReceive = new JPanel(new BorderLayout());
		_receiverList = new JList(new MyListModel(subscriptionInfo == null ? Collections.emptyList() : subscriptionInfo.getReceiverSubscriptions()));
		paneReceive.add(new JScrollPane(_receiverList), BorderLayout.CENTER);
		paneSend.setBorder(sendBorder);
		paneReceive.setBorder(receiveBorder);
		_jSplitPane.setLeftComponent(paneSend);
		_jSplitPane.setRightComponent(paneReceive);
		_jSplitPane.setResizeWeight(0.5);
		_senderList.addMouseListener(new MyMouseListener(_senderList));
		_receiverList.addMouseListener(new MyMouseListener(_receiverList));
		_senderList.setFocusable(false);
		_receiverList.setFocusable(false);
		this.add(_jSplitPane, BorderLayout.CENTER);
		_label = new JEditorPane("text/html", "");
		_label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		_label.setFont(_label.getFont().deriveFont(Font.PLAIN));
		_label.setBorder(labelBorder);
		_label.setEditable(false);
		final JScrollPane pane = new JScrollPane(_label);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(400, 160));
		this.add(pane, BorderLayout.SOUTH);
	}

	private class MyListModel extends AbstractListModel{

		private final List _senderSubscriptions;

		public MyListModel(final List senderSubscriptions) {
			_senderSubscriptions = senderSubscriptions;
		}

		public int getSize() {
			return _senderSubscriptions.size();
		}

		public Object getElementAt(final int index) {
			return _senderSubscriptions.get(index);
		}
	}

	private class MyMouseListener extends MouseAdapter{


		private final JList _list;

		public MyMouseListener(final JList list) {

			_list = list;
		}

		@Override
		public void mouseClicked(final MouseEvent e) {

			final int index = _list.locationToIndex(e.getPoint());
			if(index == -1){
				return;
			}
			final Object element = _list.getModel().getElementAt(index);
			if(element == null) return;
			if(element instanceof ApplicationSubscriptionInfo.ApplicationSendingSubscription) {
				showSubscriptionInfo((ApplicationSubscriptionInfo.ApplicationSendingSubscription)element);
			}
			if(element instanceof ApplicationSubscriptionInfo.ApplicationReceivingSubscription) {
				showSubscriptionInfo((ApplicationSubscriptionInfo.ApplicationReceivingSubscription)element);
			}
		}
	}

	private void showSubscriptionInfo(final ApplicationSubscriptionInfo.ApplicationReceivingSubscription clientReceivingSubscription) {
		_senderList.clearSelection();
		_label.setText(
				"<html>" +
				"<b>Objekt: </b>" + clientReceivingSubscription.getObject() + "<br>" +
				"<b>Attributgruppe: </b>" + clientReceivingSubscription.getUsage().getAttributeGroup() + "<br>" +
				"<b>Aspekt: </b>" + clientReceivingSubscription.getUsage().getAspect() + "<br>" +
				"<b>Simulationsvariante: </b>" + clientReceivingSubscription.getSimVar() + "<br>" +
				"<b>Typ: </b>" + (clientReceivingSubscription.isDrain() ? "Senke" : "Empfänger") + "<br>" +
				"<b>Nachgelieferte Daten: </b>" + (clientReceivingSubscription.isDelayed() ? "Ja" : "Nein") + "<br>" +
				"<b>Nur Änderungen: </b>" + (clientReceivingSubscription.isDelta() ? "Ja" : "Nein") + "<br>" +
				"<b>Status: </b>" + stateToString(clientReceivingSubscription.getState())
		);
	}

	private void showSubscriptionInfo(final ApplicationSubscriptionInfo.ApplicationSendingSubscription clientSendingSubscription) {
		_receiverList.clearSelection();
		_label.setText(
				"<html>" +
				"<b>Objekt: </b>" + clientSendingSubscription.getObject() + "<br>" +
				"<b>Attributgruppe: </b>" + clientSendingSubscription.getUsage().getAttributeGroup() + "<br>" +
				"<b>Aspekt: </b>" + clientSendingSubscription.getUsage().getAspect() + "<br>" +
				"<b>Simulationsvariante: </b>" + clientSendingSubscription.getSimVar() + "<br>" +
				"<b>Typ: </b>" + (clientSendingSubscription.isSource() ? "Quelle" : "Sender") + "<br>" +
				"<b>Unterstützt Sendesteuerung: </b>" + (clientSendingSubscription.isRequestSupported() ? "Ja" : "Nein") + "<br>" +
				"<b>Status: </b>" + stateToString(clientSendingSubscription.getState())
		);
	}
}

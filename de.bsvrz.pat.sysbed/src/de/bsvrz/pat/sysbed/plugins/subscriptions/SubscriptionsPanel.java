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

import de.bsvrz.dav.daf.main.ClientConnectionState;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientSubscriptionInfo;
import de.bsvrz.dav.daf.main.SubscriptionState;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.SystemObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11467 $
 */
public class SubscriptionsPanel extends JPanel {

	private final JSplitPane _jSplitPaneH;

	private final JSplitPane _jSplitPaneV;

	private final JLabel _label;

	private final JList _senderList;

	private final JList _receiverList;

	private final JList _potDavList;


	public SubscriptionsPanel(
			final ClientDavConnection connection,
			final SystemObject object,
			final AttributeGroupUsage usage,
			final short simulationVariant,
			final DavApplication dav) {
		super(new BorderLayout());
		_jSplitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		_jSplitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		ClientSubscriptionInfo subscriptionInfo;
		try {
			subscriptionInfo = connection.getSubscriptionInfo(
					dav, object, usage, simulationVariant
			);
		}
		catch(IOException e) {
			subscriptionInfo = null;
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Konnte die Anmeldungen nicht auflisten. " + e.getMessage());
		}
		final TitledBorder sendBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sende-Anmeldungen");
		final TitledBorder receiveBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Empfangs-Anmeldungen");
		final TitledBorder potDavBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Potentielle Zentraldatenverteiler");
		final TitledBorder labelBorder = BorderFactory.createTitledBorder("Details");

		final JComponent paneSend = new JPanel(new BorderLayout());
		_senderList = new JList(new MyListModel(subscriptionInfo == null ? Collections.emptyList() : subscriptionInfo.getSenderSubscriptions()));
		_senderList.setCellRenderer(new MyListCellRenderer());
		paneSend.add(new JScrollPane(_senderList), BorderLayout.CENTER);
		paneSend.setBorder(sendBorder);

		final JComponent paneReceive = new JPanel(new BorderLayout());
		_receiverList = new JList(new MyListModel(subscriptionInfo == null ? Collections.emptyList() : subscriptionInfo.getReceiverSubscriptions()));
		_receiverList.setCellRenderer(new MyListCellRenderer());
		paneReceive.add(new JScrollPane(_receiverList), BorderLayout.CENTER);
		paneReceive.setBorder(receiveBorder);

		final JComponent panePotDav = new JPanel(new BorderLayout());
		_potDavList = new JList(new MyListModel(subscriptionInfo == null ? Collections.emptyList() : subscriptionInfo.getPotentialCentralDavs()));
		_potDavList.setCellRenderer(new MyListCellRenderer());
		panePotDav.add(new JScrollPane(_potDavList), BorderLayout.CENTER);
		panePotDav.setBorder(potDavBorder);

		_jSplitPaneH.setLeftComponent(paneSend);
		_jSplitPaneH.setRightComponent(paneReceive);
		_jSplitPaneH.setResizeWeight(0.5);
		_jSplitPaneV.setTopComponent(_jSplitPaneH);
		_jSplitPaneV.setBottomComponent(panePotDav);
		_jSplitPaneV.setResizeWeight(0.5);
		_senderList.addMouseListener(new MyMouseListener(_senderList));
		_receiverList.addMouseListener(new MyMouseListener(_receiverList));
		_potDavList.addMouseListener(new MyMouseListener(_potDavList));
		_senderList.setFocusable(false);
		_receiverList.setFocusable(false);
		this.add(_jSplitPaneV, BorderLayout.CENTER);
		_label = new JLabel();
		_label.setFont(_label.getFont().deriveFont(Font.PLAIN));
		_label.setBorder(labelBorder);
		_label.setVerticalAlignment(SwingConstants.TOP);
		final JScrollPane pane = new JScrollPane(_label);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(400, 130));
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
			if(element instanceof ClientSubscriptionInfo.ClientSendingSubscription) {
				showSubscriptionInfo((ClientSubscriptionInfo.ClientSendingSubscription)element);
			}
			if(element instanceof ClientSubscriptionInfo.ClientReceivingSubscription) {
				showSubscriptionInfo((ClientSubscriptionInfo.ClientReceivingSubscription)element);
			}
			if(element instanceof ClientSubscriptionInfo.DavInformation){
				showDavInfo((ClientSubscriptionInfo.DavInformation) element);
			}
		}
	}

	private void showSubscriptionInfo(final ClientSubscriptionInfo.ClientReceivingSubscription clientReceivingSubscription) {
		_senderList.clearSelection();
		_potDavList.clearSelection();
		_label.setText(
				"<html>" +
				"<b>Verbindung mit: </b>" + clientReceivingSubscription.getApplicationPidOrId() + "<br>" +
				"<b>Benutzer: </b>" + clientReceivingSubscription.getUserPidOrId() + "<br>" +
				"<b>Typ: </b>" + (clientReceivingSubscription.isDrain() ? "Senke" : "Empfänger") + "<br>" +
				"<b>Nachgelieferte Daten: </b>" + (clientReceivingSubscription.isDelayed() ? "Ja" : "Nein") + "<br>" +
				"<b>Nur Änderungen: </b>" + (clientReceivingSubscription.isDelta() ? "Ja" : "Nein") + "<br>" +
				"<b>Status: </b>" + stateToString(clientReceivingSubscription.getState()) + "<br>" +
				"<b>Verbindung: </b>" + stateToString(clientReceivingSubscription.getConnectionState())
		);
	}

	private void showSubscriptionInfo(final ClientSubscriptionInfo.ClientSendingSubscription clientSendingSubscription) {
		_receiverList.clearSelection();
		_potDavList.clearSelection();
		_label.setText(
				"<html>" +
				"<b>Verbindung mit: </b>" + clientSendingSubscription.getApplicationPidOrId() + "<br>" +
				"<b>Benutzer: </b>" + clientSendingSubscription.getUserPidOrId() + "<br>" +
				"<b>Typ: </b>" + (clientSendingSubscription.isSource() ? "Quelle" : "Sender") + "<br>" +
				"<b>Unterstützt Sendesteuerung: </b>" + (clientSendingSubscription.isRequestSupported() ? "Ja" : "Nein") + "<br>" +
				"<b>Status: </b>" + stateToString(clientSendingSubscription.getState()) + "<br>" +
				"<b>Verbindung: </b>" + stateToString(clientSendingSubscription.getConnectionState())
		);
	}	
	private void showDavInfo(final ClientSubscriptionInfo.DavInformation davInfo) {
		_receiverList.clearSelection();
		_senderList.clearSelection();
		_label.setText(
				"<html>" +
				"<b>Zentraldatenverteiler: </b>" + davInfo.getCentralDavPidOrId() + "<br>" +
				"<b>Verbindung über: </b>" + davInfo.getConnectionDavPidOrId()
		);
	}

	public static String stateToString(final SubscriptionState state) {
		switch(state) {
			case NoSendersAvailable:
				return "Kein Sender";
			case NotAllowed:
				return "Keine Rechte";
			case InvalidSubscription:
				return "Ungültige Anmeldung";
			case ReceiversAvailable:
				return "Bereit zum Senden";
			case NoReceiversAvailable:
				return "Kein Empfänger";
			case SendersAvailable:
				return "Bereit zum Empfangen";
			case Waiting:
				return "Warte auf andere Datenveteiler";
			case NotResponsible:
				return "Nicht zuständig";
			case MultiRemoteLock:
				return "Ungültige Anmeldung, Mehrere Zentraldatenverteiler";
		}
		return "Ungültiger Wert: " + state;
	}

	public static String stateToString(final ClientConnectionState state) {
		switch(state) {
			case FromLocalOk:
				return "Lokale Anmeldung";
			case FromRemoteOk:
				return "Eingehende Anmeldung";
			case ToRemoteWaiting:
				return "Warte auf Antwort";
			case ToRemoteOk:
				return "Ausgehende Anmeldung: Erfolgreich";
			case ToRemoteNotResponsible:
				return "Ausgehende Anmeldung: Nicht verantwortlich";
			case ToRemoteNotAllowed:
				return "Ausgehende Anmeldung: Keine Rechte";
			case ToRemoteMultiple:
				return "Ausgehende Anmeldung: Mehrere Zentraldatenverteiler";
		}
		return "Ungültiger Wert: " + state;
	}

	class MyListCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list,
												  Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus)
		{
			Component c = super.getListCellRendererComponent(
				list,value,index,isSelected,cellHasFocus);
			if(value instanceof ClientSubscriptionInfo.ClientSendingSubscription) {
				ClientSubscriptionInfo.ClientSendingSubscription subscription = (ClientSubscriptionInfo.ClientSendingSubscription)value;
//				if(subscription.isSource()){
//					c.setForeground(new Color(95, 0, 66));
//				}
//				if(!subscription.isLocal()){
//					c.setForeground(new Color(0, 95, 66));
//				}
			}
			if(value instanceof ClientSubscriptionInfo.ClientReceivingSubscription) {
				ClientSubscriptionInfo.ClientReceivingSubscription subscription = (ClientSubscriptionInfo.ClientReceivingSubscription)value;
//				if(subscription.isDrain()){
//					c.setForeground(new Color(95, 0, 66));
//				}
//				if(!subscription.isLocal()){
//					c.setForeground(new Color(0, 95, 66));
//				}
			}
			return c;
		}
	}
}

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
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11467 $
 */
public class CompressedAppSubscriptionsPanel extends JPanel {

	private final JSplitPane _jSplitPane;

	private final JEditorPane _senderList;

	private final JEditorPane _receiverList;


	public CompressedAppSubscriptionsPanel(
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
		final JComponent paneSend = new JPanel(new BorderLayout());
		if(subscriptionInfo == null) {
			JOptionPane.showMessageDialog(this, "Konnte die Anmeldungen nicht auflisten. Möglicherweise unterstützt der Datenverteiler diese Funktion nicht.");
		}
		_senderList =  new JEditorPane("text/html", "");
		_senderList.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		_senderList.setText(formatSendSubscriptions(subscriptionInfo.getSenderSubscriptions()));
		paneSend.add(new JScrollPane(_senderList), BorderLayout.CENTER);
		final JComponent paneReceive = new JPanel(new BorderLayout());
		_receiverList =  new JEditorPane("text/html", "");
		_receiverList.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		_receiverList.setText(formatReceiveSubscriptions(subscriptionInfo.getReceiverSubscriptions()));
		paneReceive.add(new JScrollPane(_receiverList), BorderLayout.CENTER);
		paneSend.setBorder(sendBorder);
		paneReceive.setBorder(receiveBorder);
		_jSplitPane.setLeftComponent(paneSend);
		_jSplitPane.setRightComponent(paneReceive);
		_jSplitPane.setResizeWeight(0.5);
		_jSplitPane.setMinimumSize(new Dimension(600, 200));
		_senderList.setEditable(false);
		_receiverList.setEditable(false);
		this.add(_jSplitPane, BorderLayout.CENTER);
	}

	private String formatSendSubscriptions(final List<ApplicationSubscriptionInfo.ApplicationSendingSubscription> senderSubscriptions) {
		final Collection<DataSubscription> dataSubscriptions = new HashSet<DataSubscription>();
		for(ApplicationSubscriptionInfo.ApplicationSendingSubscription senderSubscription : senderSubscriptions) {
			dataSubscriptions.add(
					new DataSubscription(
							senderSubscription.getUsage().getAttributeGroup(),
							senderSubscription.getUsage().getAspect(),
							senderSubscription.getSimVar(),
							senderSubscription.getObject().getType()
					)
			);
		}
		return formatSubscriptions(dataSubscriptions);
	}

	private String formatReceiveSubscriptions(final List<ApplicationSubscriptionInfo.ApplicationReceivingSubscription> receivingSubscriptions) {
		final Collection<DataSubscription> dataSubscriptions = new HashSet<DataSubscription>();
		for(ApplicationSubscriptionInfo.ApplicationReceivingSubscription receivingSubscription : receivingSubscriptions) {
			dataSubscriptions.add(
					new DataSubscription(
							receivingSubscription.getUsage().getAttributeGroup(),
							receivingSubscription.getUsage().getAspect(),
							receivingSubscription.getSimVar(),
							receivingSubscription.getObject().getType()
					)
			);
		}
		return formatSubscriptions(dataSubscriptions);
	}

	private String formatSubscriptions(final Collection<DataSubscription> dataSubscriptions) {
		ArrayList<DataSubscription> list = new ArrayList<DataSubscription>(dataSubscriptions);
		Collections.sort(list, new Comparator<DataSubscription>() {
			public int compare(
					final DataSubscription o1, final DataSubscription o2) {
				int i = o1.getType().compareTo(o2.getType());
				if(i != 0) return i;
				i = o1.getAttributeGroup().compareTo(o2.getAttributeGroup());
				if(i != 0) return i;
				i = o1.getAspect().compareTo(o2.getAspect());
				if(i != 0) return i;
				return o1.getSimulationVariant()-o2.getSimulationVariant();
			}
		});
		StringBuilder stringBuilder = new StringBuilder("<html>");
		for(DataSubscription dataSubscription : list) {
			stringBuilder.append("<b>");
			stringBuilder.append(dataSubscription.getType().getNameOrPidOrId());
			stringBuilder.append("</b>:");
			stringBuilder.append(dataSubscription.getAttributeGroup().getPidOrNameOrId());
			stringBuilder.append(":");
			stringBuilder.append(dataSubscription.getAspect().getPidOrNameOrId());
			if(dataSubscription.getSimulationVariant() != 0){
				stringBuilder.append(":");
				stringBuilder.append(dataSubscription.getSimulationVariant());
			}
			stringBuilder.append("<br>");
		}
		return stringBuilder.toString();
	}

	private final class DataSubscription{
		private final AttributeGroup _attributeGroup;
		private final Aspect _aspect;
		private final short _simulationVariant;
		private final SystemObjectType _type;

		private DataSubscription(final AttributeGroup attributeGroup, final Aspect aspect, final short simulationVariant, final SystemObjectType type) {
			_attributeGroup = attributeGroup;
			_aspect = aspect;
			_simulationVariant = simulationVariant;
			_type = type;
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(!(o instanceof DataSubscription)) return false;

			final DataSubscription that = (DataSubscription)o;

			if(_simulationVariant != that._simulationVariant) return false;
			if(!_aspect.equals(that._aspect)) return false;
			if(!_attributeGroup.equals(that._attributeGroup)) return false;
			if(!_type.equals(that._type)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = _attributeGroup.hashCode();
			result = 31 * result + _aspect.hashCode();
			result = 31 * result + (int)_simulationVariant;
			result = 31 * result + _type.hashCode();
			return result;
		}

		private AttributeGroup getAttributeGroup() {
			return _attributeGroup;
		}

		private Aspect getAspect() {
			return _aspect;
		}

		private short getSimulationVariant() {
			return _simulationVariant;
		}

		private SystemObjectType getType() {
			return _type;
		}
	}
}

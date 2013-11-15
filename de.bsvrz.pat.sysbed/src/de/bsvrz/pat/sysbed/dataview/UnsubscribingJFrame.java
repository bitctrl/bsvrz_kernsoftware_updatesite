/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.dataview;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;

/**
 * Klasse, welche das Printable implementiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8369 $
 */
public class UnsubscribingJFrame extends JFrame implements Printable {

	/** Wird gefordert. */
	private static final long serialVersionUID = 1L;

	/** Der Receiver, der abgemeldet werden soll. */
	private ClientReceiverInterface _receiver = null;

	private ClientDavInterface _connection;

	private java.util.List<SystemObject> _objects;

	private DataDescription _dataDescription;

	public UnsubscribingJFrame(ClientDavInterface connection, List<SystemObject> objects, DataDescription dataDescription) {
		_connection = connection;
		_objects = objects;
		_dataDescription = dataDescription;
	}

	/**
	 * Setzt den Receiver
	 *
	 * @param receiver der abgemeldet werden soll.
	 */
	protected final void setReceiver(final ClientReceiverInterface receiver) {
		_receiver = receiver;
	}

	/** Überschreibt die geerbte Methode und fügt bei Schließen des Fensters die Abmeldung des Receivers hinzu. */
	@Override
	protected void processWindowEvent(final WindowEvent e) {
		super.processWindowEvent(e);

		if(e.getID() == WindowEvent.WINDOW_CLOSING && _receiver != null) {
			_connection.unsubscribeReceiver(_receiver, _objects, _dataDescription);
		}
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if(pageIndex >= 1) {
			return NO_SUCH_PAGE;
		}

		Graphics2D g2d = (Graphics2D)g;
		// Seitenformat anpassen
		final Rectangle bounds = getBounds();
		double scaleWidth = pageFormat.getImageableWidth() / bounds.width;
		double scaleHeight = pageFormat.getImageableHeight() / bounds.height;
		double scale = Math.min(scaleWidth, scaleHeight);

		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		g2d.scale(scale, scale);
		disableDoubleBuffering();
		paint(g2d);
		enableDoubleBuffering();

		return PAGE_EXISTS;
	}

	public void disableDoubleBuffering() {
		RepaintManager currentManager = RepaintManager.currentManager(this);
		currentManager.setDoubleBufferingEnabled(false);
	}

	public void enableDoubleBuffering() {
		RepaintManager currentManager = RepaintManager.currentManager(this);
		currentManager.setDoubleBufferingEnabled(true);
	}
}

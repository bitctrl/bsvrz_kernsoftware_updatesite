/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.preselection.lists;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.FocusEvent;

/**
 * Testfeld, dass im unfocussierten Zustand wie ein Label aussieht
 *
 * @author Kappich Systemberatung
 * @version $Revision: 000 $
 */
public class FilterTextField extends JTextField {

	private final String _placeholder;

	public FilterTextField(final String placeholder) {
		_placeholder = placeholder;
		Dimension maxSize = getPreferredSize();
		maxSize.width = Integer.MAX_VALUE;
		setMaximumSize(maxSize);
		setOpaque(false);
		setLayout( new BorderLayout() );
		add(new JLabel(placeholder));
	}

	@Override
	protected void paintBorder(final Graphics g) {
		if (_placeholder.length() == 0 || getText().length() > 0 || hasFocus()) {
			super.paintBorder(g);
		}
	}

	@Override
	public void paint(final Graphics g) {
		if (_placeholder.length() == 0 || getText().length() > 0 || hasFocus()) {
			super.paint(g);
			return;
		}
		super.paintChildren(g);
	}

	@Override
	protected void paintChildren(final Graphics g) {
	}

	@Override
	protected void processFocusEvent(final FocusEvent e) {
		super.processFocusEvent(e);
		repaint();
	}
}

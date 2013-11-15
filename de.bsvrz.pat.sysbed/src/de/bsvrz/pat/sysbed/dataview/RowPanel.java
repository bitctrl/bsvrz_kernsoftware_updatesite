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

import javax.swing.*;
import java.awt.*;



/**
 * Die Klasse wird f�r die Panels, die die "Keine-Daten-Zeilen" darstellen, verwendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8211 $
 *
 */
@SuppressWarnings("serial")
public class RowPanel extends JPanel {

	private final JLabel _label = new JLabel();

	private final Color _color;

	private static final Font SELECTED_FONT = new Font( "Dialog", Font.BOLD, 12);
	private static final Font UNSELECTED_FONT = new Font( "Dialog", Font.BOLD, 12);

	/* ########### Konstruktor ############# */
	/**
	 * Erstellt eine Zelle der {@link DataViewPanel OnlineTabelle}.
	 *
	 * @param text Inhalt der Zelle
	 */
	public RowPanel(final String text, final Color color) {
		_color = color;
		_label.setText(text);
		
		GridBagConstraints gbc = makeGBC(0, 0, 1, 1, 100., 100.);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		GridBagLayout gbl = new GridBagLayout();
		gbl.setConstraints(_label, gbc);
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(new Color(166, 166, 166), 1));
		add(_label, BorderLayout.CENTER);
		
		setBackground(color);
		setForeground(color);
		_label.setBackground(color);
	}
	
	/**
	 * Gibt den angezeigten Text zur�ck.
	 * 
	 * @return den angezeigten Text
	 */
	public String getText() {
		return _label.getText();
	}
	
	/**
	 * Hier wird ein Unterschied zwischen einer selektierten und einer nicht selektierten Zelle
	 * festgelegt. Allerdings darf der Unterschied keinen Einflu� auf die H�he nehmen!
	 * 
	 * @param isSelected is die Zelle selektiert?
	 */
	public void setSelectionBorder(final boolean isSelected) {
		




		if ( isSelected) {
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			_label.setFont( SELECTED_FONT);
			_label.setForeground( Color.black);
			final Color darkerColor = _color.darker();
			setBackground(darkerColor);
			_label.setBackground(darkerColor);
		} else {
			setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			_label.setFont( UNSELECTED_FONT);
			_label.setForeground( Color.gray);
			setBackground(_color);
			_label.setBackground(_color);
		}
	}
	
	/**
	 * Hilfsfunktion zur Konstruktion des Panels. Hier�ber werden die Bedingungen f�r die Anordnung der Elemente gesetzt.
	 * 
	 * @param gridx
	 *            Spaltennummer
	 * @param gridy
	 *            Zeilennummer
	 * @param gridwidth
	 *            Anzahl der Spalten �ber die das Element reicht
	 * @param gridheight
	 *            Anzahl der Zeilen �ber die das Element reicht
	 * @param weightx
	 *            Verteilung von zur Verf�gung stehendem Platz (horizontal)
	 * @param weighty
	 *            Verteilung von zur Verf�gung stehendem Platz (vertikal)
	 * 
	 * @return die Bedingungen f�r die Anordnung des Elements
	 */
	private GridBagConstraints makeGBC(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		return gbc;
	}
}

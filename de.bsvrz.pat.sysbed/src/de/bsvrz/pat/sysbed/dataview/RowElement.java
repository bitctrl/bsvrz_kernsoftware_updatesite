/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Diese Klasse repräsentiert die Zelle eines Blattes in der hierarchischen Struktur {@link RowData}.
 * Für die "Keine-Daten-Datensätze" gibt es eine eigene, wenngleich sehr ähnliche Klasse {@link RowPanel}.
 * <p>
 * Die erste Implementation dieser Klasse erweiterte JComponent. Um die Hintergrundfarbe bei Selektion
 * ändern zu können, wurde statt des JLabel ein JPanel mit dem JLabel mit add hinzugefügt. Als 
 * Seiteneffekt ging die linksbündige Darstellung der Texte verloren. Jetzt erweitert diese Klasse 
 * (wie auch RowPanel) JPanel. 
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8211 $
 */
@SuppressWarnings("serial")
public class RowElement extends JPanel {
	// Wenn ein leerer Text in den Konstruktor gesteckt wird, ist die präferrierte Höhe mal 10,
	// mal 12, aber der ZeilenHeader ist hat mindestens die Höhe 18. Damit die Zeile nicht nur
	// teilweise dargestellt wird, wird im Konstruktor mindestens der Minimalwert gesetzt.
	// Der leere Text kam unter anderem dann vor, wenn eine Attributgruppe auf oberster Ebene
	// ein Array ist, und dieses Array im Datensatz leer ist (kurz: leere Arrays). Dieser
	// Fall wird im Moment in RowData durch RowPanel erledigt.

	









	private final static int _minimumPreferredHeight = 18;
	
	private final JLabel _label = new JLabel();

	private static final Font SELECTED_FONT = new Font( "Dialog", Font.PLAIN, 10);
	private static final Font UNSELECTED_FONT = new Font( "Dialog", Font.PLAIN, 10);

	/* ########### Konstruktor ############# */
	/**
	 * Erstellt eine Zelle der {@link DataViewPanel OnlineTabelle}.
	 *
	 * @param text Inhalt der Zelle
	 */
	public RowElement(String text) {
		_label.setText(text);
		setLayout(new BorderLayout());
		add( _label, BorderLayout.CENTER);
		setBorder(BorderFactory.createLineBorder(Color.white,0)); // mit 1 anstatt von 0 gibt es ein Artefakt
		
		if ( getPreferredSize().height < _minimumPreferredHeight) {
			setPreferredSize( new Dimension( getPreferredSize().width, _minimumPreferredHeight));
		}
	}
	
	/**
	 * Gibt den dargestellten Text der Zelle zurück.
	 * 
	 * @return dargestellten Text
	 */
	public String getText() {
		return _label.getText();
	}
	
	/**
	 * Diese Methode definiert den Unterschied zwischen der Darstellung einer ausgewählten und einer
	 * nicht ausgewählten Zelle; dieser Unterschied darf sich ausschließlich auf Farben beziehen,
	 * keinesfalls auf Rahmen oder Fonts oder irgendetwas anderes, das die Größe verändert.
	 * 
	 * @param isSelected ist die Zelle selektiert?
	 */
	public void setSelectionColors(final boolean isSelected) {
		




		if ( isSelected) {
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			setBackground( Color.white.darker());
			_label.setForeground( Color.black);
			_label.setFont(SELECTED_FONT);
		} else {
			setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			setBackground(Color.white);
			_label.setForeground( Color.black);
			_label.setFont(UNSELECTED_FONT);
		}
	}
	
}

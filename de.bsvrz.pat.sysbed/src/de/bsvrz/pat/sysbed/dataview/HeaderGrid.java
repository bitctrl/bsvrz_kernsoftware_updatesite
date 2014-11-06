/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse <code>HeaderGrid</code> dient zur Darstellung einer hierarchischen Struktur (z.B. Baum).
 * Die Darstellung kann dazu verwendet werden, einen verschachtelten Tabellenkopf einer Tabelle zu
 * erstellen. Ein Objekt dieser Klasse besteht aus einem Vater-Element und beliebig vielen Nachfolgern.
 * Bei der Darstellung werden die Nachfolger mittels eines {@link HeaderGrid.Splitter Schiebereglers}
 * voneinander getrennt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8084 $
 */
public class HeaderGrid {

	/** der Debug-Logger */
	private final static Debug _debug = Debug.getLogger();

	/* ############# Variablen ############## */
	/** speichert das Objekt des Schiebereglers */
	private final Splitter _splitter;

	/** speichert die Attributliste oder das Attribut, welches dargestellt werden soll */
	private final HeaderElement _headerElement;

	/** Liste aller Nachfolger vom Vater-Element */
	private final List<HeaderGrid> _successors;

	/** speichert alle angemeldeten Objekte, u.a. zwecks Gr��enanpassung */
	private final List<ColumnWidthChangeListener> _columnWidthListeners;

	/** speichert ein Exemplar des DataViewPanels */
	private final DataViewPanel _dataViewPanel;

	/** Gibt an, ob die mouseDragged-Methode des Splitters von einem Vaterknoten aus aufgerufen wurde. */
	private boolean _fromParent = false;

	/** Gibt an, ob die Spalte vom Benutzer nicht geschlossen wurde. */
	private boolean _notClosedByUser = true;

	/** Gibt die prozentuale Ver�nderung des Vater-Elements beim Vergr��ern bzw. Verkleinern an. */
	private int _percent = 0;

	/** Speichert die Spaltenbreite, bevor die Spalte verkleinert wird. */
	private int _lastColumnWidth = 0;

	/** Speichert zu jedem Element die Anzahl der Spalten unter sich. */
	private int _numberOfColumns = 0;

	/** Speichert den Vaterknoten. */
	private HeaderGrid _parent = null;

	/** Verhindert gegebenenfalls das Neuanlegen von MouseListenern. */
	private boolean _noNewMouseListener = false;

	/* ########### Konstruktor ############ */

	/**
	 * Es wird ein Objekt der Klasse <code>HeaderGrid</code> erzeugt.
	 *
	 * @param parent        Vater-Knoten, dieses Knotens
	 * @param node          eine Attributgruppe oder ein Attribut
	 * @param dataViewPanel Komponente, in der die Datens�tze dargestellt werden
	 */
	public HeaderGrid(HeaderGrid parent, Object node, DataViewPanel dataViewPanel) {
		_parent = parent;
		_dataViewPanel = dataViewPanel;
		_splitter = new Splitter();
		_headerElement = new HeaderElement(node);
		_successors = new ArrayList<HeaderGrid>();
		_columnWidthListeners = new ArrayList<ColumnWidthChangeListener>();
	}

	/* #### Zeilenobjekte melden sich beim Header an, damit u.a. Gr��en�nderung weitergereicht werden k�nnen ### */
	/**
	 * Objekte, die sich hier anmelden, werden benachrichtigt, sobald im Spaltenheader ein Schieberegler bewegt wird.
	 *
	 * @param rowListener anzumeldendes Objekt
	 */
	public void addColumnWidthChangeListener(ColumnWidthChangeListener listener) {
		_columnWidthListeners.add( listener);
	}

	/**
	 * Objekt, welches beim Listener wieder abgemeldet werden soll.
	 *
	 * @param rowListener abzumeldendes Objekt
	 */
	public void removeColumnWidthChangeListener(ColumnWidthChangeListener rowListener) {
		_columnWidthListeners.remove(rowListener);
	}

	/**
	 * An alle angemeldeten Objekte wird die neue Breite der ver�nderten Spalte �bergeben.
	 *
	 * @param width neue Spaltenbreite
	 */
	public void setRowWidth(int width) {
		for(ColumnWidthChangeListener rowListener : _columnWidthListeners) {
			rowListener.setWidth(width);
		}
	}

	/**
	 * Liefert die optimale Spaltenbreite, so dass alle Eintr�ge zu sehen sind.
	 *
	 * @return optimale Spaltenbreite
	 */
	public int getOptimalColumnWidth() {
		int optimalColumnWidth = 0;
		for(ColumnWidthChangeListener rowListener : _columnWidthListeners) {
			int width = rowListener.getOptimalColumnWidth();
			if(width > optimalColumnWidth) optimalColumnWidth = width;
		}
		return optimalColumnWidth;
	}


	/* ############# Get-/ Set- und Add-Methoden ############# */
	/**
	 * F�gt einen Nachfolger hinzu.
	 *
	 * @param headerGrid ein Nachfolger
	 */
	public void addHeaderSuccessor(HeaderGrid headerGrid) {
		_successors.add(headerGrid);
	}

	/**
	 * Gibt die Nachfolger des Grids zur�ck.
	 *
	 * @return die Nachfolger
	 */
	public List<HeaderGrid> getHeaderSuccessors() {
		return _successors;
	}

	/**
	 * Gibt das Vater-Element zur�ck.
	 *
	 * @return Vater-Element
	 */
	public HeaderElement getHeaderElement() {
		return _headerElement;
	}

	/**
	 * Gibt den Schieberegler zur�ck.
	 *
	 * @return Schieberegler
	 */
	public Splitter getSplitter() {
		return _splitter;
	}

	/**
	 * Gibt die Anzahl der Spalten zur�ck, die sich unterhalb dieses Grids befinden.
	 *
	 * @return Anzahl der Spalten
	 */
	public int getNumberOfColumns() {
		return _numberOfColumns;
	}

	/**
	 * Setzt die Anzahl der Spalten, die sich unterhalb dieses Grids befinden.
	 *
	 * @param numberOfColumns Anzahl der Spalten
	 */
	public void setNumberOfColumns(int numberOfColumns) {
		_numberOfColumns = numberOfColumns;
	}


	/* ########### Konstruktion des Headers ############# */
	/**
	 * Erstellt ein Panel aus den im Objekt gesammelten Daten. Dieses Objekt besteht aus einem Element
	 * und seinen Nachfolgern. Wird f�r den Aufbau eines hierarchischen Tabellenkopfes genutzt.
	 *
	 * @return das Panel des Objekts
	 */
	public JPanel createHeader() {
		int column = 0;
		int row = 0;

		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);
		// es gibt genau einen Header (nicht mehr und nicht weniger)
		int number = _successors.size();
		if(number == 0) number = 1;
		double weighty = 0;            // die Y-Ausdehnung des Headers in Abh�ngigkeit, ob er Nachfolger hat oder nicht
		if(_successors.size() == 0) {
			weighty = 100;
		}
		GridBagConstraints gbc = makeGBC(column, row++, number, 1, 100, weighty);
		gbc.fill = GridBagConstraints.BOTH;
		JPanel headerPanel = createHeaderElement();
		gbl.setConstraints(headerPanel, gbc);
		panel.add(headerPanel);

		// Nachfolger ...
		for(HeaderGrid headerGrid : _successors) {
			gbc = makeGBC(column++, row, 1, 1, 100, 100);
			gbc.fill = GridBagConstraints.BOTH;

			JPanel gridPanel = headerGrid.createHeader();
			gbl.setConstraints(gridPanel, gbc);
			panel.add(gridPanel);
		}
		return panel;
	}

	/**
	 * Erstellt ein Panel, welches ein Element mit zugeh�rigem Schieberegler auf der rechten Seite darstellt.
	 *
	 * @return das Panel des Objekts
	 */
	private JPanel createHeaderElement() {
		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel();
		panel.setLayout(gbl);
		GridBagConstraints gbc = makeGBC(0, 0, 1, 1, 100, 100);
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_headerElement, gbc);
		panel.add(_headerElement);

		// Splitter einf�gen
		gbc = makeGBC(1, 0, 1, 1, 0, 0);
		gbc.fill = GridBagConstraints.VERTICAL;
		gbl.setConstraints(_splitter, gbc);
		panel.add(_splitter);

		return panel;
	}

	/**
	 * Hilfsfunktion zur Konstruktion des Panels. Hier�ber werden die Bedingungen f�r die Anordnung der Elemente gesetzt.
	 *
	 * @param gridx      Spaltennummer
	 * @param gridy      Zeilennummer
	 * @param gridwidth  Anzahl der Spalten �ber die das Element reicht
	 * @param gridheight Anzahl der Zeilen �ber die das Element reicht
	 * @param weightx    Verteilung von zur Verf�gung stehendem Platz (horizontal)
	 * @param weighty    Verteilung von zur Verf�gung stehendem Platz (vertikal)
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




	/**
	 * Setzt bei allen Komponenten den Cursor, sobald der Schieberegler bet�tigt wird.
	 *
	 * @param grid   das Grid, welches den Cursor zugewiesen bekommt
	 * @param cursor den Wert f�r den Cursor
	 *
	 * @see Cursor
	 * @see Component#setCursor(java.awt.Cursor)
	 */
	private void setHeaderCursors(HeaderGrid grid, int cursor) {
		grid.getHeaderElement().setCursor(new Cursor(cursor));
		// alle Nachfolger
		final List<HeaderGrid> successors = grid.getHeaderSuccessors();
		for(HeaderGrid headerGrid : successors) {
			setHeaderCursors(headerGrid, cursor);
		}
	}

	/* ############## Innere Klasse Splitter von der Klasse HeaderGrid ############## */

	/**
	 * Die Klasse implementiert einen Schieberegler. Mit diesem Regler wird eine links benachbarte Spalte durch Benutzung der Maus vergr��ert bzw. verkleinert,
	 * indem der Schieberegler nach links bzw. rechts verschoben wird.
	 *
	 * @see HeaderElement
	 * @see RowListener
	 */
	@SuppressWarnings("serial")
	class Splitter extends JPanel implements MouseListener, MouseMotionListener {

		/* ######### Variablen ######### */
		/** speichert die Breite des Schiebereglers */
		private final int _splitterWidth = 5;

		/** speichert bei bet�tigen der Maustaste die aktuelle Mausposition innerhalb des Schiebereglers */
		private int _posInSplitter;

		/** speichert den nach rechts gerichteten Pfeil incl. seiner Funktionalit�t */
		private Arrow _arrow = null;

		/* ######### Konstruktor ########## */
		/** Erzeugt ein Objekt der Klasse <code>Splitter</code>. */
		public Splitter() {
			// Mauscursor setzen
			setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));

			setBorder(new EtchedBorder());
			setPreferredSize(new Dimension(_splitterWidth, 1));

			if ( !_noNewMouseListener) {
				addMouseListener(this);
				addMouseMotionListener(this);
			}
		}

		/* ############# Public-Methoden ############## */
		/**
		 * Setzt die Variable im gesamten untergeordneten Grid, welche angibt, ob die mouseDragged-Methode des Schiebereglers von einem Vaterknoten aus aufgerufen
		 * wurde.
		 *
		 * @param grid       wo die Variable gesetzt werden soll
		 * @param fromParent neuer Wert der Variable
		 */
		private void setFromParent(HeaderGrid grid, boolean fromParent) {
			final List<HeaderGrid> successors = grid.getHeaderSuccessors();
			for(HeaderGrid headerGrid : successors) {
				headerGrid._fromParent = false;
				headerGrid.getSplitter().setFromParent(headerGrid, fromParent);
			}
		}

		/** F�gt den Pfeil zum Aufklappen in den Schieberegler ein. */
		public void addRightArrow() {
			if(_arrow == null) {
				_arrow = new Arrow(SwingConstants.EAST);
				if ( !_noNewMouseListener) {
					_arrow.addMouseListener(
							new MouseAdapter() {
								/**
								 * Die urspr�ngliche Breite wird wieder hergestellt und die Pfeile in den betroffenen Schiebereglern werden entfernt.
								 *
								 * @param e Maus-Ereignis
								 */
								@Override
								public void mouseClicked(MouseEvent e) {
									_notClosedByUser = true;
									undoColumnWidth();
									removeUpperArrows();
									removeLowerArrows();
								}

								/**
								 * Falls die Spalte durch ziehen wieder verbreitert wurde, dann werden die Pfeile in den betroffenen Schiebereglern entfernt.
								 *
								 * @param e Maus-Ereignis
								 */
								@Override
								public void mouseReleased(MouseEvent e) {
									int elementWidth = getHeaderElement().getSize().width;
									if(elementWidth > ((_numberOfColumns - 1) * _splitterWidth)) {
										_fromParent = false;
										_notClosedByUser = true;
										removeLowerArrows();
										removeUpperArrows();
										setFromParent(_dataViewPanel.getHeaderGrid(), false);
									}
								}
							}
					);
					_arrow.addMouseMotionListener(_splitter);
				}
			}
			setBorder(BorderFactory.createEmptyBorder());
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setLayout(new BorderLayout());
			add(_arrow, BorderLayout.CENTER);
		}

		/** Setzt die optimale Spaltenbreite, so dass alle Daten vollst�ndig zu sehen sind. */
		public void setOptimalColumnWidth() {
			if(_successors.isEmpty()) {    // Blattknoten
				if(_notClosedByUser) {
					int width = getOptimalColumnWidth();
					width += 10; // ein klein wenig Rand um die Komponente freilassen
					setColumnWidth(width);
				}
			}
			else {    // Nachfolger bestimmen
				for(HeaderGrid  headerGrid: _successors) {
					headerGrid.getSplitter().setOptimalColumnWidth();
				}
			}
		}


		/* ######### MouseListener und MouseMotionListener - Methoden ############ */
		/** Beim Doppelklick wird die optimale Spaltenbreite in Abh�ngigkeit der angezeigten Daten
		 * ermittelt und gesetzt. */
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() > 1) {         // Doppelklick
				setOptimalColumnWidth();
				return;
			}
		}

		/** wird hier nicht ben�tigt */
		public void mouseEntered(MouseEvent e) {
		}

		/** wird hier nicht ben�tigt */
		public void mouseExited(MouseEvent e) {
		}

		/** wird hier nicht ben�tigt */
		public void mouseMoved(MouseEvent e) {
		}

		/**
		 * Beim Bet�tigen der linken Maustaste wird die aktuelle Position der Maus innerhalb des
		 * Schiebereglers abgespeichert. Ist dies der erste Aufruf, dann werden die Gr��en der
		 * Elemente mittels der Methode {@link DataViewPanel#setHeaderSizes(HeaderGrid,int)}
		 * initialisiert.
		 *
		 * @param e Mausereignis
		 */
		public void mousePressed(MouseEvent e) {
			if(_dataViewPanel != null) {
				if(_dataViewPanel.getFirstRun()) {
					_dataViewPanel.initHeaderSize();
					_dataViewPanel.setFirstRun(false);
				}
			}

			// Position im Splitter
			_posInSplitter = e.getX();

			// Aussehen des Cursors setzen
			setHeaderCursors(_dataViewPanel.getHeaderGrid(), Cursor.E_RESIZE_CURSOR);
			_dataViewPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));

			// urspr�ngliche Breite merken
			setLastColumnWidth();
		}

		/** Speichert in allen untergeordneten Spalten, die vorher noch nicht geschlossen wurden, die Spaltenbreite. */
		private void setLastColumnWidth() {
			if(_notClosedByUser) { // nur hier die Variable _lastColumnWidth �ndern
				if(_successors.isEmpty()) {
					int elementWidth = getHeaderElement().getSize().width;  // aktuelle Breite merken
					_lastColumnWidth = elementWidth + _splitterWidth;
				}
				else {
					for(HeaderGrid headerGrid : _successors) {
						headerGrid.getSplitter().setLastColumnWidth();
					}
				}
			}
		}

		/**
		 * Beim Loslassen der linken Maustaste werden ggf. gesetzte Werte zur�ckgesetzt.
		 *
		 * @param e Mausereignis
		 */
		public void mouseReleased(MouseEvent e) {
			int elementWidth = getHeaderElement().getSize().width;
			if(elementWidth == (_numberOfColumns - 1) * _splitterWidth) {
				getHeaderElement().addUpperArrows();
			}
			addRightArrows();

			setHeaderCursors(_dataViewPanel.getHeaderGrid(), Cursor.DEFAULT_CURSOR);
			_dataViewPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		/** F�gt bei darunter liegenden Elementen einen Pfeil ein, falls noch keiner vorhanden ist. */
		private void addRightArrows() {
			_fromParent = false;
			int elementWidth = getHeaderElement().getSize().width;
			if(elementWidth == (_numberOfColumns - 1) * _splitterWidth) {
				addRightArrow();
			}
			for(HeaderGrid headerGrid : _successors) {
				headerGrid.getSplitter().addRightArrows();
			}
		}

		/**
		 * Bei gedr�ckter linker Maustaste und ziehen der Maus, wird links stehende Spalte vergr��ert
		 * bzw. verkleinert. Der Layoutmanager macht sein �briges und passt alle anderen Elemente ggf. an.
		 *
		 * @param e Mausereignis
		 */
		public void mouseDragged(MouseEvent e) {
			if(_successors.size() > 0) {   // Nachfolger? -> Nachfolger benachrichtigen
				if(!_fromParent) {         // Methode wurde direkt aufgerufen
					// prozentuale Ver�nderung ermitteln
					Dimension size = _headerElement.getSize();
					int elementX = size.width;
					int deltaX = e.getX() - _posInSplitter;
					if(elementX > 0) {
						_percent = (int)((deltaX * 100) / elementX);
					}
					else {
						_percent = 0;
					}
					// falls die Gr��e des Elements gleich null ist, es aber vergr��ert werden soll
					if(deltaX > 0 && _percent == 0) {
						_percent = 1;
					}
				}
				// prozentuale Ver�nderung an Nachfolger weiterleiten
				for(HeaderGrid headerGrid : _successors) {
					headerGrid._percent = _percent;
					headerGrid._fromParent = true;
					headerGrid._splitter.mouseDragged(e);
				}
			}
			else {                // keine Nachfolger -> Blattelement, Gr��e anpassen
				// neue Breite ermitteln und anwenden
				Dimension size = _headerElement.getSize();
				int elementX = size.width;
				int elementY = size.height;
				if(_fromParent) {
					// Methode wurde von einem �bergeordneten Element aufgerufen.
					if(_notClosedByUser) {     // falls closedFromAbove == false -> muss Element geschlossen bleiben
						if(_percent > 0 && elementX == 0) {
							elementX = 1;
						}
						elementX += (int)((elementX * _percent) / 100);
						if(elementX <= 0) elementX = 0;
					}
				}
				else {
					// Methode wurde direkt aufgerufen
					elementX += e.getX() - _posInSplitter;
					if(elementX <= 0) elementX = 0;
				}
				size = new Dimension(elementX, elementY);
				_headerElement.setPreferredSize(size);
				_headerElement.revalidate();
				setRowWidth(size.width + _splitterWidth);
			}
		}


		/* ############ Private-Methoden ############# */
		/**
		 * Setzt die Spaltenbreite auf einen beliebigen Wert.
		 *
		 * @param width die neue Spaltenbreite
		 */
		private void setColumnWidth(int width) {
			_headerElement.setPreferredSize(new Dimension(width - _splitterWidth, _headerElement.getPreferredSize().height));
			_headerElement.revalidate();
			setRowWidth(width);
		}

		/** Entfernt den Pfeil zum Aufklappen aus dem Schieberegler. */
		private void removeRightArrow() {
			remove(_arrow);
			setBorder(new EtchedBorder());
			setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			_arrow = null;	// Wenn das fehlt, so erscheinen beim Drucken alte Rechtspfeile. Unverstanden.
		}

		/** Alle dar�ber liegenden Elemente werden �berpr�ft, ob der Pfeil aus dem Schieberegler entfernt werden kann. Falls ja, dann wird er entfernt. */
		private void removeUpperArrows() {
			if(_parent != null) {      // ist parent == null -> fertig
				int count = _parent.getSplitter().getComponentCount();
				if(count > 0) {    // hat Parent einen Pfeil? -> entfernen und weiter nach oben gehen, sonst fertig
					_parent.getSplitter().removeRightArrow();
					_parent._notClosedByUser = true;
					_parent.getSplitter().removeUpperArrows();  // einen h�her
				}
			}
		}

		/** Bei allen darunter liegenden Elementen werden die Pfeile aus den Schiebereglern entfernt. */
		private void removeLowerArrows() {
			if(_notClosedByUser) {
				removeRightArrow();
				for(HeaderGrid headerGrid : _successors) {
					headerGrid.getSplitter().removeLowerArrows();
				}
			}
		}

		/** Die Spaltenbreite wird wieder auf den urspr�nglichen Wert gesetzt. */
		private void undoColumnWidth() {
			if(_notClosedByUser) {
				if(_successors.isEmpty()) {
					setColumnWidth(_lastColumnWidth);
				}
				else {
					for(HeaderGrid headerGrid : _successors) {
						headerGrid.getSplitter().undoColumnWidth();
					}
				}
			}
		}
	}


	/**
	 * Diese Klasse repr�sentiert eine Zelle des hierarchischen {@link HeaderGrid Spaltenheaders}.
	 *
	 * */
	@SuppressWarnings("serial")
	public class HeaderElement extends JComponent {

		/* ########### Variablen ############ */
		/** speichert das Objekt, welches im Header dargestellt werden soll */
		private final Object _headerObject;

		/** speichert den darzustellenden Text */
		private final String _text;

		/** speichert die Pid der Attributgruppe bzw. des Attributes */
		private final String _pid;


		/* ############ Konstruktor ################ */
		/**
		 * Konstruktor. Zeichnet ein HeaderElement.
		 *
		 * @param headerElement vom Typ <code>AttributeGroup</code> oder <code>Attribute</code>
		 */
		public HeaderElement(Object headerElement) {
			_headerObject = headerElement;
			if(_headerObject instanceof Attribute) {
				Attribute attribute = (Attribute)_headerObject;
				if(isAttributeArray(attribute)) {
					_text = attribute.getNameOrPidOrId() + " [..]";
				}
				else {
					_text = attribute.getNameOrPidOrId();
				}
				_pid = attribute.getPid();
			}
			else if(_headerObject instanceof AttributeGroup) {
				AttributeGroup attributeGroup = (AttributeGroup)_headerObject;
				_text = attributeGroup.getNameOrPidOrId();
				_pid = attributeGroup.getPid();
			}
			else {
				_debug.error("Unerlaubtes Objekt - muss Attributgruppe oder Attribut sein!");
				_text = "";
				_pid = null;
			}
			createHeaderElement();
		}

		/** Diese Methode ist f�r das Aussehen und die Funktionalit�t des HeaderElements verantwortlich. */
		private void createHeaderElement() {
			JLabel label = new JLabel(_text);
			label.setHorizontalAlignment(SwingConstants.CENTER);

			Arrow icon = new Arrow(SwingConstants.WEST);
			if ( !_noNewMouseListener) {
				icon.addMouseListener(
						new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								// �berschrift einklappen
								addLowerArrows();
								_notClosedByUser = false;       // den, den ich direkt schlie�e, auf false setzen
								addUpperArrows();
							}
						}
				);
			}
			setBorder(new EtchedBorder());
			setLayout(new BorderLayout());
			add(label, BorderLayout.CENTER);
			add(icon, BorderLayout.EAST);
		}


		/* ############# Hilfsmethoden ############ */
		/** F�gt bei hierarchisch h�her liegenden Elementen Pfeile hinzu, wenn sie auch komplett geschlossen sind. */
		private void addUpperArrows() {
			if(_parent != null) {
				int parentWidth = _parent.getHeaderElement().getSize().width;
				int closedColumnsWidth = (_parent.getNumberOfColumns() - 1) * getSplitter()._splitterWidth;
				int elementWidth = getHeaderElement().getSize().width;
				if((parentWidth - closedColumnsWidth) == elementWidth || parentWidth == elementWidth) {
					// Pfeil hinzuf�gen
					_parent.getSplitter().addRightArrow();
					_parent._notClosedByUser = false;
					_notClosedByUser = true;
					_parent.getHeaderElement().addUpperArrows();
				}
			}
		}

		/**
		 * Beim Bet�tigen des Pfeils zum Einklappen der Spalte, wird dieser Spalte und allen
		 * darunterliegenden der Pfeil zum Aufklappen zur Verf�gung gestellt, sowie
		 * die Spalte reduziert auf ein Minimum.
		 */
		private void addLowerArrows() {
			if(_notClosedByUser) {
				getSplitter().addRightArrow();
				if(_successors.isEmpty()) {
					_lastColumnWidth = getSize().width + getSplitter().getWidth();  // alte Breite merken
					getSplitter().setColumnWidth(getSplitter().getWidth()); // auf Splitter-Gr��e setzen
				}
				else {
					for(HeaderGrid headerGrid : _successors) {
						headerGrid.getHeaderElement().addLowerArrows(); // alle untergeordneten Elemente bedenken
					}
				}
			}
		}

		/**
		 * Pr�ft, ob es sich bei dem Attribut um ein Array handelt, oder nicht.
		 *
		 * @param attribute das zu pr�fende Attribut
		 *
		 * @return ist das Attribut ein Array oder nicht
		 */
		private boolean isAttributeArray(Attribute attribute) {
			return attribute.isArray();
		}


		/* ########### Get-Methoden ############ */
		/**
		 * Gibt das Objekt des HeaderElements zur�ck.
		 *
		 * @return das Objekt des HeaderElements
		 */
		public Object getObject() {
			return _headerObject;
		}

		/**
		 * Gibt den Text des HeaderElements zur�ck, welcher dargestellt wird.
		 *
		 * @return den Text
		 */
		public String getText() {
			return _text;
		}

		/**
		 * Gibt die Pid des HeaderElements zur�ck. Besitzt es keine Pid wird null zur�ckgegeben.
		 *
		 * @return die Pid
		 */
		public String getPid() {
			if(_pid == null || _pid.equals("")) {
				return _text;
			}
			else {
				return _pid;
			}
		}
	}

	/**
	 * Erstellt einen Pfeil in einer angegebenen Richtung und Farbe der Gr��e 5 x 5 Pixel.
	 * Die Richtung wird mit den Swing-Konstanten angegeben:<p> SwingConstants.WEST<br> SwingConstants.EAST
	 */
	@SuppressWarnings("serial")
	class Arrow extends JPanel implements SwingConstants {

		/* ############ Variablen ############# */
		/** Richtung des Pfeils */
		private final int _direction;

		/** Farbe des Pfeils */
		private final Color _arrowColor;


		/* ############## Konstruktoren ############### */
		/**
		 * Erstellt einen Pfeil mit der angegebenen Richtung in der Farbe grau.
		 *
		 * @param direction von SwingConstants (WEST, EAST)
		 */
		public Arrow(int direction) {
			this(direction, Color.gray);
		}

		/**
		 * Erstellt einen Pfeil mit der angegebenen Richtung und der angegebenen Farbe.
		 *
		 * @param direction  von SwingConstants (WEST, EAST)
		 * @param arrowColor eine beliebige Farbe
		 */
		public Arrow(int direction, Color arrowColor) {
			_direction = direction;
			_arrowColor = arrowColor;
		}


		/* ################### Public-Methoden ################# */

		@Override
		public void paint(Graphics g) {
			// Zeichnet ein Dreieck auf das Panel.
			int w, h;

			w = getSize().width;
			h = getSize().height;

			g.setColor(getBackground());
			g.fillRect(0, 0, w, h);     // ohne Rand

			int mid = (h / 2);
			// Dreieck zeichnen
			g.setColor(_arrowColor);
			switch(_direction) {
				case WEST:
					final Polygon westArrow = new Polygon(new int[] {0,4,4}, new int[] {mid,mid-4,mid+4}, 3);
					g.fillPolygon( westArrow);
					break;
				case EAST:
					final Polygon eastArrow = new Polygon(new int[] {0,0,4}, new int[] {mid-4,mid+4,mid}, 3);
					g.fillPolygon( eastArrow);
					break;
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(getSplitter().getWidth() + 1, 5);
		}
	}

	/**
	 * Macht dieses HeaderGrid dem �bergebenen �hnlich.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	public void makeSimilar( final HeaderGrid otherHeaderGrid) {
		setColumnWidths(otherHeaderGrid);
		setClosingStates(otherHeaderGrid);
		setLastColumnWidths(otherHeaderGrid);
		setNumberOfColumns(otherHeaderGrid);
		setPositions(otherHeaderGrid);
		setFromParentValues(otherHeaderGrid);
		setPercentValues(otherHeaderGrid);
		setArrowIcons(otherHeaderGrid);
	}

	/**
	 *	Setzt die Breiten aller Spalten auf die Werte des �bergebenen HeaderGrids.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setColumnWidths( final HeaderGrid otherHeaderGrid) {
		_headerElement.setPreferredSize( otherHeaderGrid._headerElement.getPreferredSize());
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setColumnWidths!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setColumnWidths(otherSuccessor);
		}
	}

	/**
	 *	Setzt die Offen/Geschlossen-Stati auf die Werte des �bergebenen HeaderGrids.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setClosingStates( final HeaderGrid otherHeaderGrid) {
		_notClosedByUser = otherHeaderGrid._notClosedByUser;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setClosingStates!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setClosingStates(otherSuccessor);
		}
	}

	/**
	 *	Setzt Arrows wie im �bergebenen HeaderGrid.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setArrowIcons( final HeaderGrid otherHeaderGrid) {
		if ( otherHeaderGrid._splitter._arrow == null) {
			_splitter._arrow = null;
		} else {
			if ( otherHeaderGrid._splitter._arrow._direction == SwingConstants.EAST) {
				_splitter.addRightArrow();
			} else {
				throw new RuntimeException("Implementation unvollst�ndig!");
			}
		}
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setArrowIcons!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setArrowIcons(otherSuccessor);
		}
	}

	/**
	 * Setzt die Anzahl der Spalten im Splitter
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setNumberOfColumns( final HeaderGrid otherHeaderGrid) {
		_numberOfColumns = otherHeaderGrid._numberOfColumns;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setNumberOfColumns!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setNumberOfColumns(otherSuccessor);
		}
	}

	/**
	 * Setzt die Position des Mauszeigers im Splitter
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setPositions( final HeaderGrid otherHeaderGrid) {
		_splitter._posInSplitter = otherHeaderGrid._splitter._posInSplitter;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setPositions!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setPositions(otherSuccessor);
		}
	}

	/**
	 * Setzt die FromParent-Werte.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setFromParentValues( final HeaderGrid otherHeaderGrid) {
		_fromParent = otherHeaderGrid._fromParent;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setFromParentValues!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setFromParentValues(otherSuccessor);
		}
	}

	/**
	 * Setzt die Percent-Werte.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setPercentValues( final HeaderGrid otherHeaderGrid) {
		_percent = otherHeaderGrid._percent;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setPercentValues!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setPercentValues(otherSuccessor);
		}
	}

	/**
	 * Setzt die Last-Column-Width-Werte.
	 *
	 * @param otherHeaderGrid das andere HeaderGrid
	 */
	private void setLastColumnWidths( final HeaderGrid otherHeaderGrid) {
		_lastColumnWidth = otherHeaderGrid._lastColumnWidth;
		final int size = _successors.size();
		if ( size != otherHeaderGrid._successors.size()) {
			_debug.error("Interner Fehler in HeaderElement.setLastColumnWidths!");
			return;
		}
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			final HeaderGrid otherSuccessor = otherHeaderGrid._successors.get(index);
			ownSuccessor.setLastColumnWidths(otherSuccessor);
		}
	}

	/**
	 * Entfernt alle MouseListener, MouseMotianListener und MouseWheelListener der Komponenten.
	 */
	public void removeAllMouseListeners() {
		_noNewMouseListener = true;
		removeAllMouseListeners( _splitter);
		_splitter.setEnabled(false);
		removeAllMouseListeners( _headerElement);
		removeAllMouseListeners( _splitter._arrow);
		final int size = _successors.size();
		for ( int index = 0; index < size; index++) {
			final HeaderGrid ownSuccessor = _successors.get(index);
			ownSuccessor.removeAllMouseListeners();
		}
		final Component[] components = _headerElement.getComponents();
		for ( Component component : components) {
			removeAllMouseListeners(component);
		}
	}

	private void removeAllMouseListeners( final Component component) {
		if ( component != null) {
			final MouseListener[] mouseListeners = component.getMouseListeners();
			for ( MouseListener mouseListener : mouseListeners) {
				component.removeMouseListener( mouseListener);
			}
			final MouseMotionListener[] mouseMotionListeners = component.getMouseMotionListeners();
			for ( MouseMotionListener mouseMotionListener : mouseMotionListeners) {
				component.removeMouseMotionListener( mouseMotionListener);
			}
			final MouseWheelListener[] mouseWheelListeners = component.getMouseWheelListeners();
			for ( MouseWheelListener mouseWheelListener : mouseWheelListeners) {
				component.removeMouseWheelListener( mouseWheelListener);
			}
		}
	}
}

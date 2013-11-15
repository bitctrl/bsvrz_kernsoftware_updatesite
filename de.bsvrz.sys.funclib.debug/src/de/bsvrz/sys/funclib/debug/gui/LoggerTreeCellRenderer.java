/*
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.debug.
 * 
 * de.bsvrz.sys.funclib.debug is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.debug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.debug; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.debug.gui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;
import java.util.logging.Level;

public class LoggerTreeCellRenderer extends DefaultTreeCellRenderer {

    Icon loggerNoLevel;
    Icon loggerWithLevel;
    Icon noLoggerIcon;

    public LoggerTreeCellRenderer() {
        super();
        //ClassLoader loader = this.getClass().getClassLoader();
        URL url = getClass().getResource("notset.gif");
        loggerNoLevel = new ImageIcon(url);
        url = getClass().getResource("set.gif");
        loggerWithLevel = new ImageIcon(url);
        noLoggerIcon = super.getDefaultOpenIcon();
    }
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        TreeNodeObject nodeObject = (TreeNodeObject)node.getUserObject();
        if (nodeObject.isLogger()) {
            Level level = nodeObject.getLogger().getLevel();
            if (level == null) {
                super.setClosedIcon(loggerNoLevel);
                super.setOpenIcon(loggerNoLevel);
                super.setLeafIcon(loggerNoLevel);
            }
            else {
                super.setClosedIcon(loggerWithLevel);
                super.setOpenIcon(loggerWithLevel);
                super.setLeafIcon(loggerWithLevel);
            }
        }
        else {
            super.setClosedIcon(noLoggerIcon);
            super.setOpenIcon(noLoggerIcon);
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded,  leaf, row, hasFocus);
    }
}

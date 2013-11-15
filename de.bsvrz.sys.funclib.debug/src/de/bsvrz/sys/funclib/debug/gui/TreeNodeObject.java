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

import java.util.logging.*;

class TreeNodeObject {
    private Logger logger;
    private String name;

    public TreeNodeObject(Logger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public TreeNodeObject(String name) {
        this.logger = null;
        this.name = name;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }

    public boolean isLogger() {
        return (logger!= null);
    }

    public String toString() {
        return name;
    }
}

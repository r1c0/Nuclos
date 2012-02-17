// Copyright (C) 2011 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui.layoutml;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * LayoutMLButtonActionListener are used to allow enabling/disabling parent button from the action listener
 */
public abstract class LayoutMLButtonActionListener implements ActionListener {
	
	private final Map<JComponent, String> mpParentComps;
	
	public LayoutMLButtonActionListener() {
		this.mpParentComps = new HashMap<JComponent, String>();
	}
	
	public void setParentComponent(JComponent parent, String sActionCommand) {
		this.mpParentComps.put(parent, sActionCommand);
	}
	
	public void setComponentsEnabled(boolean enabled) {
		for (JComponent parent : this.mpParentComps.keySet()) {
			parent.setEnabled(enabled);
		}
	}
	
	public void fireComponentEnabledStateUpdate() {
		for (Map.Entry<JComponent, String> entry : this.mpParentComps.entrySet()) {
			entry.getKey().setEnabled(enableParentComponent(entry.getValue()));
		}
	}
	
	public abstract boolean enableParentComponent(String sActionCommand);
}

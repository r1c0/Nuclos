//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
/**
 * 
 */
package org.nuclos.client.processmonitor;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * @author Marc.Finke
 * 
 * Editor Panel represents the Details Panel
 * holds:
 * the header panel
 * the panel for drawing the processmodel with shapes
 *
 */
public class InstanceViewPanel extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel pnlInstanceViewer = new JPanel(new BorderLayout());
	
	final InstanceViewHeaderPanel pnlHeader = new InstanceViewHeaderPanel();
	private final InstanceViewer instanceViewer = new InstanceViewer();

	public InstanceViewPanel() {
		super(new BorderLayout());

		final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, pnlInstanceViewer, new JPanel());
		splitpn.setResizeWeight(1.0);
		splitpn.setDividerSize(0);
		this.add(splitpn, BorderLayout.CENTER);

		pnlInstanceViewer.add(pnlHeader, BorderLayout.NORTH);
		pnlInstanceViewer.add(instanceViewer, BorderLayout.CENTER);
	}

	/*
	 * returns the proper paint viewer
	 */
	public InstanceViewer getInstanceViewer() {
		return this.instanceViewer;
	}

}

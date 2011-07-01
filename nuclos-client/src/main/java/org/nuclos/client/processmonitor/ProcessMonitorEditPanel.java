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

import javax.swing.JComponent;
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
public class ProcessMonitorEditPanel extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPanel pnlProcessMonitorModelEditor = new JPanel(new BorderLayout());
	
	final ProcessMonitorEditHeaderPanel pnlHeader = new ProcessMonitorEditHeaderPanel();
	private final ProcessMonitorEditor processmonitoreditor = new ProcessMonitorEditor();

	public ProcessMonitorEditPanel(JComponent subformUsages) {
		super(new BorderLayout());

		final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, pnlProcessMonitorModelEditor, new JPanel());
		splitpn.setResizeWeight(1.0);
		splitpn.setDividerSize(0);
		this.add(splitpn, BorderLayout.CENTER);

		pnlProcessMonitorModelEditor.add(pnlHeader, BorderLayout.NORTH);
		pnlProcessMonitorModelEditor.add(processmonitoreditor, BorderLayout.CENTER);
	}

	/*
	 * returns the proper paint editor
	 */
	public ProcessMonitorEditor getProcessMonitorEditor() {
		return this.processmonitoreditor;
	}

}

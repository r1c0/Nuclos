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
package org.nuclos.client.statemodel.admin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.nuclos.client.statemodel.StateModelEditor;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Details edit panel for state model administration. Contains the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class StateModelEditPanel extends JPanel {
	private final JPanel pnlStateModelEditor = new JPanel(new BorderLayout());
	final StateModelHeaderPanel pnlHeader = new StateModelHeaderPanel();
	private final StateModelEditor statemodeleditor = new StateModelEditor();
	
	public final JSplitPane splitpn;
	public final JSplitPane splitpnMain;

	public StateModelEditPanel(JComponent subformUsages) {
		super(new BorderLayout());

		splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, pnlStateModelEditor, newUsagePanel(subformUsages));
		splitpn.setOneTouchExpandable(true);
		splitpn.setResizeWeight(1d);
		splitpnMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitpn, statemodeleditor.getPropertiesPanel());
		splitpnMain.setOneTouchExpandable(true);
		splitpnMain.setResizeWeight(1d);
		
		this.add(splitpnMain, BorderLayout.CENTER);
		
		statemodeleditor.getPropertiesPanel().getStatePropertiesPanel().getStateDependantRightsPanel().setActionListenerForWidthChanged(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ev) {
				splitpnMain.setDividerLocation(getSize().width
					- UIManager.getInt("SplitPane.dividerSize")
					- 2 // maybe a border
					- statemodeleditor.getPropertiesPanel().getStatePropertiesPanel().getStateDependantRightsPanel().LEFT_BORDER
					- statemodeleditor.getPropertiesPanel().getStatePropertiesPanel().getStateDependantRightsPanel().getPreferredSize().width);
			}
		});

		pnlStateModelEditor.add(pnlHeader, BorderLayout.NORTH);
		pnlStateModelEditor.add(statemodeleditor, BorderLayout.CENTER);
	}

	private static JPanel newUsagePanel(JComponent subformUsages) {
		final JPanel pnlUsages = new JPanel(new BorderLayout());
		final JLabel labUsages = new JLabel(CommonLocaleDelegate.getMessage("StateModelEditPanel.1","Verwendungen"));
		labUsages.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		pnlUsages.add(labUsages, BorderLayout.NORTH);
		pnlUsages.add(subformUsages, BorderLayout.CENTER);
		return pnlUsages;
	}

	public StateModelEditor getStateModelEditor() {
		return this.statemodeleditor;
	}

}	// class StateModelEditPanel

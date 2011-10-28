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
package org.nuclos.client.processmonitor;

import java.awt.CardLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.nuclos.client.statemodel.panels.NotePropertiesPanel;
import org.nuclos.client.statemodel.panels.TransitionRolesPanel;
import org.nuclos.client.statemodel.panels.TransitionRulesPanel;

/**
 * Panel containing the properties for the various process model editor elements.
 * 
 * at the moment:
 * Properties Panel for Subprocess
 * Properties Panel for Transitions between Subprocess's
 * Empty Panel for nothing is selected
 * A Panel for notes
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */
public class ProcessMonitorEditorPropertiesPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final SubProcessPropertiesPanel pnlSubProcessProperties = new SubProcessPropertiesPanel();
	private final SubProcessTransitionPropertiesPanel pnlSubProcessTransitionProperties = new SubProcessTransitionPropertiesPanel();
	private final NotePropertiesPanel pnlNote = new NotePropertiesPanel();
	private final TransitionRulesPanel pnlTransitionRules = new TransitionRulesPanel();
	private final TransitionRolesPanel pnlTransitionRoles = new TransitionRolesPanel();
	private final JPanel pnlEmpty = new JPanel();


	public ProcessMonitorEditorPropertiesPanel(ProcessMonitorEditor parent) {
		this.init();
	}
	
	private void init() {
		this.setLayout(cardLayout);

		final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpn.setResizeWeight(0.5d);
		splitpn.setTopComponent(pnlTransitionRules);
		splitpn.setBottomComponent(pnlTransitionRoles);

		this.add(pnlSubProcessProperties, "SubProcess");
		this.add(pnlSubProcessTransitionProperties, "Transition");
		this.add(pnlNote, "Note");
		this.add(pnlEmpty, "None");
	}

	/*
	 * bring the given panel to front  
	 */
	public void setPanel(String sName) {
		cardLayout.show(this, sName);
	}
	
	public SubProcessPropertiesPanel getSubProcessPropertiesPanel() {
		return pnlSubProcessProperties;
	}
	
	public SubProcessTransitionPropertiesPanel getSubProcessTransitionPropertiesPanel() {
		return pnlSubProcessTransitionProperties;
	}

	public TransitionRulesPanel getTransitionRulePanel() {
		return pnlTransitionRules;
	}

	public TransitionRolesPanel getTransitionRolePanel() {
		return pnlTransitionRoles;
	}

	public NotePropertiesPanel getNotePanel() {
		return pnlNote;
	}
}	// class StateModelEditorPropertiesPanel

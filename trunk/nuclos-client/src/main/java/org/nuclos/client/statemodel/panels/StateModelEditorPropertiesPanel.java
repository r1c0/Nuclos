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
package org.nuclos.client.statemodel.panels;

import java.awt.CardLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.nuclos.client.statemodel.StateModelEditor;
import org.nuclos.client.statemodel.controller.StatePropertiesController;
import org.nuclos.client.statemodel.controller.TransitionPropertiesController;
import org.nuclos.client.ui.UIUtils;

/**
 * Panel containing the properties for the various state model editor elements.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateModelEditorPropertiesPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final StatePropertiesPanel pnlStateProperties = new StatePropertiesPanel();
	private final NotePropertiesPanel pnlNote = new NotePropertiesPanel();
	private final TransitionRulesPanel pnlTransitionRules = new TransitionRulesPanel();
	private final TransitionRolesPanel pnlTransitionRoles = new TransitionRolesPanel();
	private final JPanel pnlEmpty;

	// @SuppressWarnings("unused") // created for constructor side-effects
	private final TransitionPropertiesController ctlTransitionProperties;
	// @SuppressWarnings({"deprecation", "unused"})
	private final StatePropertiesController ctlStateProperties;

	public StateModelEditorPropertiesPanel(StateModelEditor parent) {
		pnlEmpty = new StatePropertiesPanel();
		UIUtils.disableComponentsInContainer(pnlEmpty);

		/** @todo this doesn't seem the right place to setup the controllers */
		ctlTransitionProperties = new TransitionPropertiesController(parent, pnlTransitionRules, pnlTransitionRoles);
		ctlStateProperties = new StatePropertiesController(pnlStateProperties);

		this.init();
	}

	private void init() {
		this.setLayout(cardLayout);

		final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpn.setResizeWeight(0.5d);
		splitpn.setTopComponent(pnlTransitionRules);
		splitpn.setBottomComponent(pnlTransitionRoles);

		this.add(pnlStateProperties, "State");
		this.add(splitpn, "Transition");
		this.add(pnlNote, "Note");
		this.add(pnlEmpty, "None");
	}
	
	public void setPanel(String sName) {
		cardLayout.show(this, sName);
	}

	public StatePropertiesPanel getStatePropertiesPanel() {
		return pnlStateProperties;
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

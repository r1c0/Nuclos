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
package org.nuclos.client.statemodel.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.masterdata.SortableRuleTableModel;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.StateModelEditor;
import org.nuclos.client.statemodel.models.StateRoleTableModel;
import org.nuclos.client.statemodel.panels.TransitionRolesPanel;
import org.nuclos.client.statemodel.panels.TransitionRulesPanel;
import org.nuclos.client.statemodel.shapes.StateTransition;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Controller for transition properties
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TransitionPropertiesController {
	private class RulesActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			rulesActionPerformed(ev);
		}
	}

	private class RolesActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			rolesActionPerformed(ev);
		}
	}

	private final TransitionRulesPanel pnlTransitionRules;
	private final TransitionRolesPanel pnlTransitionRoles;
	private final StateModelEditor parent;

	private final RulesActionListener alRules = new RulesActionListener();
	private final RolesActionListener alRoles = new RolesActionListener();

	public TransitionPropertiesController(StateModelEditor parent, TransitionRulesPanel pnlTransitionRules, TransitionRolesPanel pnlTransitionRoles) {
		this.parent = parent;
		this.pnlTransitionRules = pnlTransitionRules;
		this.pnlTransitionRoles = pnlTransitionRoles;

		this.pnlTransitionRules.getBtnAdd().addActionListener(alRules);
		this.pnlTransitionRules.getBtnDelete().addActionListener(alRules);
		this.pnlTransitionRules.getBtnUp().addActionListener(alRules);
		this.pnlTransitionRules.getBtnDown().addActionListener(alRules);
		this.pnlTransitionRules.getBtnAutomatic().addActionListener(alRules);
		this.pnlTransitionRules.getBtnDefault().addActionListener(alRules);

		this.pnlTransitionRules.getBtnDelete().setEnabled(false);
		this.pnlTransitionRules.getBtnUp().setEnabled(false);
		this.pnlTransitionRules.getBtnDown().setEnabled(false);

		this.pnlTransitionRules.getTblRules().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				rulesSelectionChanged();
			}
		});

		this.pnlTransitionRoles.getBtnAdd().addActionListener(alRoles);
		this.pnlTransitionRoles.getBtnDelete().addActionListener(alRoles);

		this.pnlTransitionRoles.getBtnDelete().setEnabled(false);

		this.pnlTransitionRoles.getTblRoles().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				rolesSelectionChanged();
			}
		});
		
		this.pnlTransitionRules.getModel().addValueChangedListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				alRules.actionPerformed(new ActionEvent(TransitionPropertiesController.this.pnlTransitionRules, 0, "update"));
			}
		});
	}

	private void rulesSelectionChanged() {
		final int iSelIndex = pnlTransitionRules.getTblRules().getSelectedRow();
		pnlTransitionRules.getBtnDown().setEnabled(
				iSelIndex >= 0 && iSelIndex < pnlTransitionRules.getTblRules().getRowCount() - 1);
		pnlTransitionRules.getBtnUp().setEnabled(iSelIndex > 0);
		pnlTransitionRules.getBtnDelete().setEnabled(iSelIndex >= 0);
	}

	private void rolesSelectionChanged() {
		final int iSelIndex = pnlTransitionRoles.getTblRoles().getSelectedRow();
		pnlTransitionRoles.getBtnDelete().setEnabled(iSelIndex >= 0);
	}

	private void rulesActionPerformed(ActionEvent ev) {
		final SortableRuleTableModel model = pnlTransitionRules.getModel();
		try {
			if (ev.getActionCommand().equals("add")) {
				final InsertRuleController controller = new InsertRuleController(parent);
				if (controller.run(CommonLocaleDelegate.getInstance().getMessage(
						"TransitionPropertiesController.1","Liste der verf\u00fcgbaren Regeln"), pnlTransitionRules.getModel().getRules())) {
					for (int i = 0; i < controller.getRulesPanel().getTblRules().getSelectedRowCount(); i++) {
						parent.addRule(controller.getRulesPanel().getRow(controller.getRulesPanel().getTblRules().getSelectedRows()[i]));
					}
					pnlTransitionRules.getModel().fireTableDataChanged();
				}
			}
			else if (ev.getActionCommand().equals("remove")) {
				for (int i = 0; i < pnlTransitionRules.getTblRules().getSelectedRowCount(); i++) {
					parent.removeRule(pnlTransitionRules.getModel().getRow(pnlTransitionRules.getTblRules().getSelectedRows()[i]));
				}
				pnlTransitionRules.getModel().fireTableDataChanged();

			}
			else if (ev.getActionCommand().equals("update")) {
				for (int i = 0; i < pnlTransitionRules.getTblRules().getSelectedRowCount(); i++) {
					SortedRuleVO ruleVO = pnlTransitionRules.getModel().getRow(pnlTransitionRules.getTblRules().getSelectedRows()[i]);
					parent.updateRule(ruleVO);
				}
			}
			else if (ev.getActionCommand().equals("moveUp")) {
				final StateTransition st = (StateTransition) (parent.getViewer().getModel().getSelection().iterator().next());
				final int iSelIndex = pnlTransitionRules.getTblRules().getSelectedRow();
				if (iSelIndex >= 1) {
					model.moveRowUp(iSelIndex);
					pnlTransitionRules.getTblRules().setRowSelectionInterval(iSelIndex - 1, iSelIndex - 1);
					parent.getViewer().getModel().fireModelChanged();
					st.getStateTransitionVO().getRuleIdsWithRunAfterwards().clear();
					for (SortedRuleVO sortedrulevo : model.getRules()) {
						st.getStateTransitionVO().getRuleIdsWithRunAfterwards().add(new Pair<Integer, Boolean>(sortedrulevo.getId(), sortedrulevo.isRunAfterwards()));
					}
				}
			}
			else if (ev.getActionCommand().equals("moveDown")) {
				final StateTransition st = (StateTransition) (parent.getViewer().getModel().getSelection().iterator().next());
				final int iSelIndex = pnlTransitionRules.getTblRules().getSelectedRow();
				if (iSelIndex < pnlTransitionRules.getTblRules().getRowCount() - 1) {
					model.moveRowDown(iSelIndex);
					pnlTransitionRules.getTblRules().setRowSelectionInterval(iSelIndex + 1, iSelIndex + 1);
					parent.getViewer().getModel().fireModelChanged();
					st.getStateTransitionVO().getRuleIdsWithRunAfterwards().clear();
					for (SortedRuleVO sortedrulevo : model.getRules()) {
						st.getStateTransitionVO().getRuleIdsWithRunAfterwards().add(new Pair<Integer, Boolean>(sortedrulevo.getId(), sortedrulevo.isRunAfterwards()));
					}
				}
			}
			else if (ev.getActionCommand().equals("setAuto")) {
				final StateTransition statetransition = (StateTransition) (parent.getViewer().getModel().getSelection().iterator().next());
				statetransition.getStateTransitionVO().setAutomatic(pnlTransitionRules.getBtnAutomatic().isSelected());
				parent.getViewer().getModel().fireModelChanged();
				((Component) parent.getViewer()).repaint();
			}
			else if (ev.getActionCommand().equals("setDefault")) {
				final StateTransition statetransition = (StateTransition) (parent.getViewer().getModel().getSelection().iterator().next());
				statetransition.getStateTransitionVO().setDefault(pnlTransitionRules.getBtnDefault().isSelected());
				parent.getBtnDefaultTransition().setSelected(pnlTransitionRules.getBtnDefault().isSelected());
				
				parent.getViewer().getModel().fireModelChanged();
				((Component) parent.getViewer()).repaint();
			}
		}
		catch (RemoteException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex.getMessage(), ex);
		}
	}

	private void rolesActionPerformed(ActionEvent ev) {
		final StateRoleTableModel model = pnlTransitionRoles.getModel();
		try {
			if (ev.getActionCommand().equals("add")) {
				final InsertRoleController controller = new InsertRoleController(parent);
				if (controller.run(CommonLocaleDelegate.getInstance().getMessage(
						"TransitionPropertiesController.2","Verf\u00fcgbare Benutzergruppen"), model.getRoles())) {
					for (int i = 0; i < controller.pnlRoles.getTblRoles().getSelectedRowCount(); i++) {
						parent.addRole(controller.pnlRoles.getRow(controller.pnlRoles.getTblRoles().getSelectedRows()[i]));
					}
					model.fireTableDataChanged();
				}
			}
			else if (ev.getActionCommand().equals("remove")) {
				for (int i = 0; i < pnlTransitionRoles.getTblRoles().getSelectedRowCount(); i++) {
					parent.removeRole(model.getRow(pnlTransitionRoles.getTblRoles().getSelectedRows()[i]));
				}
				model.fireTableDataChanged();
			}
		}
		catch (RemoteException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex.getMessage(), ex);
		}
	}

}  // class TransitionPropertiesController

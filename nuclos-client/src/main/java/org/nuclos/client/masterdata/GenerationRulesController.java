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
package org.nuclos.client.masterdata;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import javax.ejb.CreateException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.masterdata.ui.GenerationRulesPanel;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.controller.InsertRuleController;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.common.valueobject.GeneratorRuleVO;

/**
 * Controller for management of rules in object generation.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe Allner</a>
 * @version 01.00.00
 */
public class GenerationRulesController {

	private class RulesActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			rulesActionPerformed(ev);
		}
	}

	private final GenerationRulesPanel pnlGenerationRules;
	private final RulesActionListener alRules = new RulesActionListener();
	private final Component parent;
	private final GenerationCollectController controller;

	public GenerationRulesController(GenerationRulesPanel pnlGenerationRules, Component parent, GenerationCollectController controller) {
		this.pnlGenerationRules = pnlGenerationRules;
		this.parent = parent;
		this.controller = controller;

		this.pnlGenerationRules.getBtnAdd().addActionListener(alRules);
		this.pnlGenerationRules.getBtnDelete().addActionListener(alRules);
		this.pnlGenerationRules.getBtnUp().addActionListener(alRules);
		this.pnlGenerationRules.getBtnDown().addActionListener(alRules);

		this.pnlGenerationRules.getBtnDelete().setEnabled(false);
		this.pnlGenerationRules.getBtnUp().setEnabled(false);
		this.pnlGenerationRules.getBtnDown().setEnabled(false);

		this.pnlGenerationRules.getTblRules().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				rulesSelectionChanged();
			}
		});
		
		this.pnlGenerationRules.getModel().addValueChangedListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GenerationRulesController.this.controller.detailsChanged(GenerationRulesController.this.pnlGenerationRules);
			}
		});
	}

	/**
	 * get rule usages in there order
	 * @return <Integer>Collection rule ids in the right order
	 */
	public List<GeneratorRuleVO> getRuleUsages() {
		final SortableRuleTableModel tblmodel = (SortableRuleTableModel) this.pnlGenerationRules.getTblRules().getModel();

		return CollectionUtils.transform(tblmodel.getRules(), new Transformer<SortedRuleVO, GeneratorRuleVO>() {
			@Override
			public GeneratorRuleVO transform(SortedRuleVO sortedrulevo) {
				return new GeneratorRuleVO(sortedrulevo.getId(), sortedrulevo.getName(), sortedrulevo.getDescription(), sortedrulevo.getOrder(), sortedrulevo.isRunAfterwards());
			}
		});
	}

	public void setRuleUsages(Collection<GeneratorRuleVO> collRuleUsages) {
		final List<SortedRuleVO> lstsortedrulevo = CollectionUtils.transform(collRuleUsages, new Transformer<GeneratorRuleVO, SortedRuleVO>() {
			@Override
			public SortedRuleVO transform(GeneratorRuleVO generatorrulevo) {
				return new SortedRuleVO(generatorrulevo.getId(), generatorrulevo.getName(), generatorrulevo.getDescription(), generatorrulevo.getOrder(), generatorrulevo.isRunAfterwards());
			}
		});

		final SortableRuleTableModel tblmodel = (SortableRuleTableModel) this.pnlGenerationRules.getTblRules().getModel();
		tblmodel.setRules(lstsortedrulevo);
	}

	private void rulesSelectionChanged() {
		final int iSelIndex = pnlGenerationRules.getTblRules().getSelectedRow();
		pnlGenerationRules.getBtnDown().setEnabled(
				iSelIndex >= 0 && iSelIndex < pnlGenerationRules.getTblRules().getRowCount() - 1);
		pnlGenerationRules.getBtnUp().setEnabled(iSelIndex > 0);
		pnlGenerationRules.getBtnDelete().setEnabled(iSelIndex >= 0);
	}

	private void rulesActionPerformed(ActionEvent ev) {
		final SortableRuleTableModel model = pnlGenerationRules.getModel();
		try {
			if (ev.getActionCommand().equals("add")) {
				final InsertRuleController controller = new InsertRuleController(parent);
				if (controller.run(CommonLocaleDelegate.getMessage("GenerationRulesController.1", "Liste der verf\u00fcgbaren Regeln"), pnlGenerationRules.getModel().getRules())) {
					for (int i = 0; i < controller.getRulesPanel().getTblRules().getSelectedRowCount(); i++) {
						model.addRow(controller.getRulesPanel().getRow(controller.getRulesPanel().getTblRules().getSelectedRows()[i]));
					}
				}
			}
			else if (ev.getActionCommand().equals("remove")) {
				for (int i = 0; i < pnlGenerationRules.getTblRules().getSelectedRowCount(); i++) {
					model.removeRow(pnlGenerationRules.getTblRules().getSelectedRows()[i]);
				}
			}
			else if (ev.getActionCommand().equals("moveUp")) {
				final int iSelIndex = pnlGenerationRules.getTblRules().getSelectedRow();
				if (iSelIndex >= 1) {
					model.moveRowUp(iSelIndex);
					pnlGenerationRules.getTblRules().setRowSelectionInterval(iSelIndex - 1, iSelIndex - 1);
				}
			}
			else if (ev.getActionCommand().equals("moveDown")) {
				final int iSelIndex = pnlGenerationRules.getTblRules().getSelectedRow();
				if (iSelIndex < pnlGenerationRules.getTblRules().getRowCount() - 1) {
					model.moveRowDown(iSelIndex);
					pnlGenerationRules.getTblRules().setRowSelectionInterval(iSelIndex + 1, iSelIndex + 1);
				}
			}
			this.controller.detailsChanged(parent);
		}
		catch (CreateException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex.getMessage(), ex);
		}
		catch (RemoteException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex.getMessage(), ex);
		}
	}
}

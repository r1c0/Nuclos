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
package org.nuclos.client.rule.admin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.node.rule.RuleTreeModel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.rule.TimelimitRuleDelegate;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.PointerException;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosCompileException;

/**
 * CollectController for Entity timelimitrule.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */

public class TimelimitRuleCollectController extends MasterDataCollectController {

	private final TimelimitRuleDelegate timelimitDelegate = TimelimitRuleDelegate.getInstance();
	// Note: pnlEdit cannot be initialized here resp. in this constructor because it is used
	// by setupEditPanelForDetailsTab (which, sadly, is during super constructor evaluation)
	private RuleEditPanel pnlEdit;

	private final Action actCheckRuleSource = new CommonAbstractAction(Icons.getInstance().getIconValidate16(), 
			getSpringLocaleDelegate().getMessage("RuleCollectController.2", "Quelltext pr\u00fcfen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdCheckRuleSource();
		}
	};

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public TimelimitRuleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.TIMELIMITRULE, tabIfAny, false);
		this.setupDetailsToolBar();
		this.getCollectStateModel().addCollectStateListener(new RuleCollectStateListener());
	}

	@Override
	protected void close() {
		super.close();
	}

	private void setupDetailsToolBar() {
		final JButton btnCheckRuleSource = new JButton(this.actCheckRuleSource);
		btnCheckRuleSource.setName("btnCheckRuleSource");
		this.getDetailsPanel().addToolBarComponent(btnCheckRuleSource);
		this.getDetailsPanel().addToolBarComponent(btnPointer);
	}

	@Override
	protected void cmdJumpToTree() {
		UIUtils.runCommand(this.getFrame(), new Runnable() {
			@Override
			public void run() {
				final Integer iRuleId = (Integer) getSelectedCollectableId();
				getExplorerController().cmdShowRuleUsage(iRuleId, RuleTreeModel.FRIST_NODE_LABEL);
			}
		});
	}

	@Override
	protected void cmdShowResultInExplorer() {
		UIUtils.runCommand(this.getFrame(), new Runnable() {
			@Override
			public void run() {
				Main.getInstance().getMainController().getExplorerController().cmdShowRuleUsage(null, null);
			}
		});
	}

	private ExplorerController getExplorerController() {
		return Main.getInstance().getMainController().getExplorerController();
	}

	private void cmdCheckRuleSource() {
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final TimelimitRuleDelegate ruleDelegate = TimelimitRuleDelegate.getInstance();

				// get the actual (edited) rule code
				String sJavaCode = pnlEdit.getJavaEditorPanel().getText();

				CollectableMasterDataWithDependants clct = TimelimitRuleCollectController.this.getCompleteSelectedCollectable();
				if (clct == null) {
					clct = TimelimitRuleCollectController.this.newCollectable();
				}
				TimelimitRuleCollectController.this.readValuesFromEditPanel(clct, false);

				MasterDataVO mdvo = clct.getMasterDataCVO();
				mdvo.setField("source", sJavaCode);

				TimelimitRuleCollectController.this.pnlEdit.clearMessages();

				try {
					ruleDelegate.check(mdvo);
					JOptionPane.showMessageDialog(TimelimitRuleCollectController.this.getFrame(), getSpringLocaleDelegate().getMessage(
							"CodeCollectController.compiledsuccessfully", "Quellcode erfolgreich kompiliert."));
				}
				catch (NuclosCompileException ex) {
					TimelimitRuleCollectController.this.pnlEdit.setMessages(ex.getErrorMessages());
				}
			}
		});
	}

	/**
	 * @return false
	 */
	@Override
	protected boolean isMultiEditAllowed() {
		return false;
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		pnlEdit.addChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public  void removeAdditionalChangeListenersForDetails() {
		pnlEdit.removeChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 *
	 * @param clctmd
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void unsafeFillDetailsPanel(CollectableMasterDataWithDependants clctmd) throws NuclosBusinessException {
		// fill the textfields:
		super.unsafeFillDetailsPanel(clctmd);

		pnlEdit.setEntityname(getEntityName());
		if (clctmd.getId() == null) {
			pnlEdit.getJavaEditorPanel().setText(timelimitDelegate.getClassTemplate());
			pnlEdit.getJavaEditorPanel().setCaretPosition(0);
		}
		else {
			pnlEdit.getJavaEditorPanel().setText((String) clctmd.getValue("source"));
			pnlEdit.getJavaEditorPanel().setCaretPosition(0);
			pnlEdit.setId(((Integer)clctmd.getId()).longValue());
		}
	}

	/**
	 *
	 * @throws CommonBusinessException
	 */
	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		try {
			this.timelimitDelegate.remove(clct.getMasterDataCVO());
		}
		catch (NuclosCompileException ex) {
			TimelimitRuleCollectController.this.pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.6");
		}
	}

	@Override
	protected String getEntityLabel() {
		return getSpringLocaleDelegate().getMessage("TimelimitRuleCollectController.1", "Fristenregeln");
	}

	/**
	 * @param clctCurrent
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	protected CollectableMasterDataWithDependants updateCurrentCollectable(CollectableMasterDataWithDependants clctCurrent) throws CommonBusinessException {
		return this.updateCollectable(clctCurrent, null);
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		try {
			final MasterDataVO mdcvoUpdated = this.timelimitDelegate.update(clct.getMasterDataCVO());

			pnlEdit.clearMessages();
			return CollectableMasterDataWithDependants.newInstance(clct.getCollectableEntity(), mdcvoUpdated);
		}
		catch (NuclosCompileException ex) {
			pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.4");
			//throw new CommonBusinessException(SpringLocaleDelegate.getMessage("RuleCollectController.4", "Fehler beim \u00dcbersetzen des Quellcodes.\nBitte \u00fcberpr\u00fcfen Sie die Meldungen."));
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage(), ex);
		}
	}

	/**
	 *
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		try {
			final MasterDataVO mdcvoInserted = this.timelimitDelegate.create(clctNew.getMasterDataCVO());

			pnlEdit.clearMessages();
			return CollectableMasterDataWithDependants.newInstance(clctNew.getCollectableEntity(), mdcvoInserted);
		}
		catch (NuclosCompileException ex) {
			pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.4");
			//throw new CommonBusinessException(SpringLocaleDelegate.getMessage("RuleCollectController.4", "Fehler beim \u00dcbersetzen des Quellcodes.\nBitte \u00fcberpr\u00fcfen Sie die Meldungen."));
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage(), ex);
		}
	}

	@Override
	public void setupEditPanelForDetailsTab() {
		super.setupEditPanelForDetailsTab();
		//this.getDetailsPanel().add(pnlEdit, BorderLayout.CENTER);

		if (pnlEdit == null) {
			pnlEdit = new RuleEditPanel(null);
		}

		JPanel editPanel = (JPanel) UIUtils.findJComponent(this.getDetailsPanel(), "editPanel");
		if (editPanel != null) {
			editPanel.removeAll();
			editPanel.setLayout(new BorderLayout());
			editPanel.add(pnlEdit, BorderLayout.CENTER);
		}
	}

	@Override
	protected void readValuesFromEditPanel(CollectableMasterDataWithDependants clct, boolean bSearchTab) throws CollectableValidationException {
		super.readValuesFromEditPanel(clct, bSearchTab);
		clct.setField("source", new CollectableValueField(pnlEdit.getJavaEditorPanel().getText()));
	}

	@Override
	public void refreshCurrentCollectable() throws CommonBusinessException {
		super.refreshCurrentCollectable();
		pnlEdit.clearMessages();
	}

	private class RuleCollectStateListener extends CollectStateAdapter {

		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws NuclosBusinessException {
			final boolean bWriteAllowed = SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
			actCheckRuleSource.setEnabled(bWriteAllowed);
		}

	}
}  // class TimelimitRuleCollectController

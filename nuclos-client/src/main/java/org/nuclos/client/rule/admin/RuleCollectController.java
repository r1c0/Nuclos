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

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.SubFormController;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.console.NuclosConsole;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.node.rule.RuleTreeModel;
import org.nuclos.client.genericobject.valuelistprovider.ProcessCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.StatusNumeralCollectableFieldsProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.masterdata.datatransfer.RuleCVOTransferable;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataEntityCollectableFieldsProvider;
import org.nuclos.client.masterdata.valuelistprovider.RuleEventsCollectableFieldsProvider;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.EditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.ClearAction;
import org.nuclos.client.ui.collect.SubForm.Column;
import org.nuclos.client.ui.collect.SubForm.RefreshValueListAction;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.PointerException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * <code>CollectController</code> for entity "rule".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 * @todo extend from MasterDataCollectController
 * @todo + CollectableJavaEditor (implements CollectableComponent)
 */
public class RuleCollectController extends EntityCollectController<CollectableRule> {

	private static final Logger LOG = Logger.getLogger(RuleCollectController.class);

	private final CollectPanel<CollectableRule> pnlCollect = new RuleCollectPanel(false);
	private final MainFrameTab ifrm;
	private final RuleDelegate ruledelegate = RuleDelegate.getInstance();
	private SubForm subform = new SubForm(NuclosEntity.RULEUSAGE.getEntityName(), JToolBar.VERTICAL);
	private final RuleEditPanel pnlEdit = new RuleEditPanel(subform);
	private final MasterDataSubFormController subformctlUsage;

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
	public RuleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, CollectableRule.clcte);
		ifrm = tabIfAny!=null ? tabIfAny : newInternalFrame(getSpringLocaleDelegate().getMessage(
				"RuleCollectController.1", "Regelwerke verwalten"));

		//this.transferhandler = new RuleTransferHandler(parent);

		this.initialize(this.pnlCollect);

		ifrm.setLayeredComponent(pnlCollect);

		this.setupResultToolBar();
		this.setupDetailsToolBar();

		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.getHeaderPanel().newCollectableComponentsProvider()));

		this.setupShortcutsForTabs(ifrm);

		this.setInternalFrame(ifrm, tabIfAny==null);
		this.setupDataTransfer();
		this.getCollectStateModel().addCollectStateListener(new RuleCollectStateListener());

		// initialize subform at the end of the constructor, because the method 'setInternalFrame(...)' some lines above
		// reorganize the subform and clears its filterpanel
		Column entityColumn = new Column("entity", null, new CollectableComponentType(CollectableComponentTypes.TYPE_COMBOBOX, null), true, true, false, null, null);
		entityColumn.setValueListProvider(new MasterDataEntityCollectableFieldsProvider());
		entityColumn.getValueListProvider().setParameter("module", true);
		
		Column eventColumn = new Column("event", null, new CollectableComponentType(CollectableComponentTypes.TYPE_COMBOBOX, null), true, true, false, null, null);
		eventColumn.setValueListProvider(new RuleEventsCollectableFieldsProvider());

		Column processColumn = new Column("process", null, new CollectableComponentType(CollectableComponentTypes.TYPE_COMBOBOX, null), true, true, false, null, null);
		processColumn.setValueListProvider(new ProcessCollectableFieldsProvider());

		Column statusColumn = new Column("state", null, new CollectableComponentType(CollectableComponentTypes.TYPE_COMBOBOX, null), true, true, false, null, null);
		statusColumn.setValueListProvider(new StatusNumeralCollectableFieldsProvider());
		statusColumn.getValueListProvider().setParameter("provideIdFields", "true");

		entityColumn.addClearAction(new ClearAction("process"));
		entityColumn.addClearAction(new ClearAction("state"));
		processColumn.addClearAction(new ClearAction("state"));
		processColumn.addRefreshValueListAction(new RefreshValueListAction("process", NuclosEntity.RULEUSAGE.getEntityName(), "entity", "entityName"));
		statusColumn.addRefreshValueListAction(new RefreshValueListAction("state", NuclosEntity.RULEUSAGE.getEntityName(), "entity", "entityName"));
		statusColumn.addRefreshValueListAction(new RefreshValueListAction("state", NuclosEntity.RULEUSAGE.getEntityName(), "process", "process"));
		
		this.subform.addColumn(entityColumn);
		this.subform.addColumn(eventColumn);
		this.subform.addColumn(processColumn);
		this.subform.addColumn(statusColumn);
		
		this.subformctlUsage = new MasterDataSubFormController(getFrame(), parent, this.getDetailsPanel().getEditModel(), getEntityName(),
				subform, this.getPreferences(), this.getEntityPreferences(), valueListProviderCache);
	}

	public final MainFrameTab getMainFrameTab() {
		return ifrm;
	}

	@Override
	public void close() {
		super.close();
		this.subformctlUsage.close();
		
		// close Subform support
		subform.close();
		subform = null;
	}

	private void setupResultToolBar() {
		this.getResultPanel().addPopupExtraMenuItem(new JMenuItem(new AbstractAction(getSpringLocaleDelegate().getMessage(
				"MasterDataCollectController.6","Ergebnis in Explorer anzeigen"), Icons.getInstance().getIconTree16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.getInstance().getMainController().getExplorerController().cmdShowRuleUsage(null, null);
			}
		}));
	}

	private void setupDetailsToolBar() {
		final JMenuItem btnMakeTreeRoot = new JMenuItem();
		btnMakeTreeRoot.setIcon(Icons.getInstance().getIconMakeTreeRoot16());
		btnMakeTreeRoot.setText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.9","In Explorer anzeigen"));
		btnMakeTreeRoot.setToolTipText(getSpringLocaleDelegate().getMessage("DatasourceCollectController.14", "In Explorer anzeigen"));
		btnMakeTreeRoot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdJumpToTree();
			}
		});

		this.getDetailsPanel().addPopupExtraMenuItem(btnMakeTreeRoot);

		final JButton btnCheckRuleSource = new JButton(this.actCheckRuleSource);
		btnCheckRuleSource.setName("btnCheckRuleSource");
		this.getDetailsPanel().addToolBarComponent(btnCheckRuleSource);
		this.getDetailsPanel().addToolBarComponent(btnPointer);
	}

	private void cmdJumpToTree() {
		UIUtils.runCommand(this.getFrame(), new Runnable() {
			@Override
			public void run() {
				final Integer iRuleId = (Integer) getSelectedCollectableId();
				getExplorerController().cmdShowRuleUsage(iRuleId, RuleTreeModel.ALL_RULES_NODE_LABEL);
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
				final RuleDelegate ruleDelegate = RuleDelegate.getInstance();

				// get the actual (edited) rule code
				String sJavaCode = pnlEdit.getJavaEditorPanel().getText();

				CollectableRule clctRule = RuleCollectController.this.getCompleteSelectedCollectable();
				if (clctRule == null) {
					clctRule = RuleCollectController.this.newCollectable();
				}
				RuleCollectController.this.readValuesFromEditPanel(clctRule, false);

				clctRule.setField(CollectableRule.FIELDNAME_RULESOURCE, new CollectableValueField(sJavaCode));

				RuleCollectController.this.pnlEdit.clearMessages();

				try {
					ruleDelegate.compile(clctRule.getRuleVO());
					JOptionPane.showMessageDialog(RuleCollectController.this.getFrame(), 
							getSpringLocaleDelegate().getMessage(
									"CodeCollectController.compiledsuccessfully", "Quellcode erfolgreich kompiliert."));
				}
				catch (NuclosCompileException ex) {
					RuleCollectController.this.pnlEdit.setMessages(ex.getErrorMessages());
				}
			}
		});
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		this.pnlEdit.addChangeListener(this.changelistenerDetailsChanged);
		this.subformctlUsage.getSubForm().addChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		this.pnlEdit.removeChangeListener(this.changelistenerDetailsChanged);
		this.subformctlUsage.getSubForm().removeChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @param clct
	 */
	@Override
	protected void unsafeFillDetailsPanel(CollectableRule clct) throws CommonBusinessException {
		// fill the textfields:
		super.unsafeFillDetailsPanel(clct);

		final RuleVO rulevo = clct.getRuleVO();

		this.pnlEdit.setEntityname(getEntityName());
		if (rulevo.getId() == null) {
			this.pnlEdit.getJavaEditorPanel().setText(RuleDelegate.getInstance().getClassTemplate());
			this.pnlEdit.getJavaEditorPanel().setCaretPosition(0);
		}
		else {
			this.pnlEdit.getJavaEditorPanel().setText(rulevo.getSource());
			this.pnlEdit.getJavaEditorPanel().setCaretPosition(0);
			this.pnlEdit.setId(((Integer)clct.getId()).longValue());
		}

		// fill usage panel:
		this.subformctlUsage.fillSubForm(clct.getId());
	}

	@Override
	public CollectableRule findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return new CollectableRule(this.ruledelegate.get((Integer) oId));
	}

	@Override
	protected CollectableRule findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}

	/**
	 * @throws NuclosBusinessException
	 */
	@Override
	protected void deleteCollectable(CollectableRule clct) throws CommonBusinessException {
		try {
			this.ruledelegate.remove(clct.getRuleVO());
		}
		catch (NuclosCompileException ex) {
			RuleCollectController.this.pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.6");
		}
	}

	/**
	 * @return
	 */
	@Override
	protected boolean isNewAllowed() {
		return SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
	}

	/**
	 * @return
	 */
	@Override
	protected boolean isSaveAllowed() {
		return SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed(){
		//return this.getCollectableEntity().getMasterDataMetaCVO().isRemoved() &&
		return SecurityCache.getInstance().isDeleteAllowedForMasterData(sEntity);
	}
	/**
	 * @param clct
	 * @param bSearchTab
	 */
	@Override
	protected void readValuesFromEditPanel(CollectableRule clct, boolean bSearchTab) throws CollectableValidationException {
		// if we have a search tab here, this method must be extended.
		assert !bSearchTab;

		super.readValuesFromEditPanel(clct, bSearchTab);

		clct.getRuleVO().setSource(this.pnlEdit.getJavaEditorPanel().getText());
	}

	@Override
	protected String getEntityLabel() {
		return getSpringLocaleDelegate().getMessage("RuleCollectController.3", "Regelwerke");
	}

	@Override
	public CollectableRule newCollectable() {
		return new CollectableRule(new RuleVO(null, null, null, null, false));
	}

	@Override
	protected CollectableRule updateCurrentCollectable(CollectableRule clctCurrent) throws CommonBusinessException {
//		return this.updateCollectable(clctCurrent, this.subformctlUsage.getAllCollectables());
		return this.updateCollectable(clctCurrent, this.getAllSubFormData(clctCurrent.getId()));
	}

	@Override
	protected CollectableRule updateCollectable(CollectableRule clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantMasterDataMap mpDependants = (oAdditionalData == null) ? null : ((DependantCollectableMasterDataMap) oAdditionalData).toDependantMasterDataMap();

		try {
			this.pnlEdit.clearMessages();
			final RuleVO rulevoUpdate = this.ruledelegate.update(clct.getRuleVO(), mpDependants);
			return new CollectableRule(rulevoUpdate);
		}
		catch (NuclosCompileException ex) {
			this.pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.4");
			//throw new CommonBusinessException(SpringLocaleDelegate.getMessage("RuleCollectController.4", "Fehler beim \u00dcbersetzen des Quellcodes.\nBitte \u00fcberpr\u00fcfen Sie die Meldungen."));
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage(), ex);
		}
	}

	@Override
	protected CollectableRule insertCollectable(CollectableRule clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		// save (insert) rule:
		final RuleVO rulevoNew = clctNew.getRuleVO();

		// We have to clear the ids for cloned objects:
		/** @todo eliminate this workaround - this is the wrong place. The right place is the Clone action! */
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		try {
			this.pnlEdit.clearMessages();
			final RuleVO rulevoInserted = this.ruledelegate.create(rulevoNew, mpmdvoDependants);
			return new CollectableRule(rulevoInserted);
		}
		catch (NuclosCompileException ex) {
			this.pnlEdit.setMessages(ex.getErrorMessages());
			throw new PointerException("RuleCollectController.4");
			//throw new CommonBusinessException(SpringLocaleDelegate.getMessage("RuleCollectController.4", "Fehler beim \u00dcbersetzen des Quellcodes.\nBitte \u00fcberpr\u00fcfen Sie die Meldungen."));
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage(), ex);
		}
	}

	@Override
	protected DependantCollectableMasterDataMap getAdditionalDataForMultiUpdate(CollectableRule clct) throws CommonBusinessException {
		return this.getAllSubFormData(clct.getId());
	}

	/**
	 * gathers the data from the subform. All rows are gathered, even the removed ones.
	 * @param oParentId set as the parent id for each subform row.
	 * @return Collection<MasterDataVO>. the data from the subform
	 */
	private DependantCollectableMasterDataMap getAllSubFormData(Object oParentId) throws CommonValidationException {
		return new DependantCollectableMasterDataMap(subformctlUsage.getSubForm().getEntityName(), subformctlUsage.getAllCollectables(oParentId, null, true, null));
	}

	/**
	 * enables drag and copy from rows.
	 */
	protected void setupDataTransfer() {
		// enable drag&drop:
		final JTable tbl = this.getResultTable();
		tbl.setDragEnabled(true);
		tbl.setTransferHandler(new TransferHandler() {

			@Override
			public int getSourceActions(JComponent comp) {
				int result = NONE;
				if (comp == tbl) {
					final Collectable clctSelected = getSelectedCollectable();
					if (clctSelected != null) {
						result = COPY;
					}
				}
				return result;
			}

			@Override
			protected Transferable createTransferable(JComponent comp) {
				Transferable result = null;
				if (comp == tbl) {
					final List<CollectableRule> lstclctruleSelected = getSelectedCollectables();
					if (CollectionUtils.isNonEmpty(lstclctruleSelected)) {
						final List<RuleAndRuleUsageEntity> tranferableList = new ArrayList<RuleAndRuleUsageEntity>(lstclctruleSelected.size());
						for (CollectableRule clctrule : lstclctruleSelected) {
							tranferableList.add(new RuleAndRuleUsageEntity(clctrule.getRuleVO(), null, null));
						}
						result = new RuleCVOTransferable(tranferableList);
					}
				}
				return result;
			}
		});
	}

	/**
	 * inner class TransferHandler. Handles drag&drop, copy&paste for the explorer trees.
	 */
	/* mtj: unused?
	private class RuleTransferHandler extends javax.swing.TransferHandler {
		private final Component parent;

		RuleTransferHandler(Component parent) {
			this.parent = parent;
		}

		@Override
		public int getSourceActions(JComponent comp) {
			int result = NONE;
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				result = this.getSelectedTreeNode(tree).getDataTransferSourceActions();
			}
			return result;
		}

		@Override
		protected Transferable createTransferable(JComponent comp) {
			Transferable result = null;
			if (comp instanceof JTreeTable) {
				final JTreeTable treeTable = (JTreeTable) comp;
				result = this.getSelectedTreeNode(treeTable.getTree()).createTransferable();
			}
			return result;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] aflavors) {
			log.debug("canImport");
			// Unfortunately, this method is not called for each node, so we only can say yes or no
			// for the whole tree here. We must say yes to enable drop at all.

			return true;

//			class IsIdOrCVO implements CollectionUtils.UnaryPredicate {
//				public boolean evaluate(Object o) {
//					final DataFlavor flavor = (DataFlavor) o;
//					return (flavor instanceof GenericObjectIdModuleProcess.DataFlavor);
//				}
//			}
//			final Object oFlavor = CollectionUtils.findFirst(Arrays.asList(aflavors), new IsIdOrCVO());
//
//			return (oFlavor != null);
		}

		@Override
		public boolean importData(JComponent comp, Transferable transferable) {
			boolean result = false;

			if (comp instanceof JTreeTable) {
				final JTreeTable treetbl = (JTreeTable) comp;

				try {
					result = this.getSelectedTreeNode(treetbl.getTree()).importTransferData(parent, transferable, treetbl.getTree());
				}
				catch (UnsupportedFlavorException ex) {
					JOptionPane.showMessageDialog(parent, "Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt.");
				}
				catch (IOException ex) {
					throw new NuclosFatalException(ex);
				}
			}

			return result;
		}

		private ExplorerNode getSelectedTreeNode(final JTree tree) {
			return (ExplorerNode) tree.getSelectionPath().getLastPathComponent();
		}
	}
   /unused */

//	protected boolean stopEditing() {
//		return super.stopEditingInDetails() & this.subformctlUsage.stopEditing();
//	}

	private class RuleCollectStateListener extends CollectStateAdapter {

		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws NuclosBusinessException {
			RuleCollectController rcc = RuleCollectController.this;
			final boolean bWriteAllowed = SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
			actCheckRuleSource.setEnabled(bWriteAllowed);

			rcc.pnlEdit.getHeaderPanel().clcttfDescription.setEnabled(bWriteAllowed);
			rcc.pnlEdit.getHeaderPanel().clcttfName.setEnabled(bWriteAllowed);
			rcc.pnlEdit.getHeaderPanel().clctchkbxActive.setEnabled(bWriteAllowed);
			rcc.pnlEdit.getJavaEditorPanel().setEditable(bWriteAllowed);
			rcc.subform.setEnabled(bWriteAllowed);
		}

	}

	private class RuleCollectPanel extends CollectPanel<CollectableRule> {

		RuleCollectPanel(boolean bSearchPanelAvailable) {
			super(bSearchPanelAvailable);
		}

		@Override
		public ResultPanel<CollectableRule> newResultPanel() {
			return new NuclosResultPanel<CollectableRule>() {

				@Override
				protected void postXMLImport(final CollectController<CollectableRule> clctctl) {
					// initialize attribute cache on server side
					try {
						NuclosConsole.getInstance().parseAndInvoke(new String[]{NuclosConsole.getInstance().CMD_INVALIDATE_RULECACHE}, false);
					}
					catch(Exception e) {
						throw new NuclosFatalException(getSpringLocaleDelegate().getMessage(
								"RuleCollectController.5", "Der serverseitige RuleCache konnte nicht invalidiert werden!"), e);
					}
					super.postXMLImport(clctctl);
				}
			};
		}

		@Override
		public DetailsPanel newDetailsPanel() {
			return new DetailsPanel(false);
		}


	}

	@Override
	protected Map<String, DetailsSubFormController<CollectableEntityObject>> getMapOfSubFormControllersInDetails() {
		Map<String, DetailsSubFormController<CollectableEntityObject>> result = new HashMap<String, DetailsSubFormController<CollectableEntityObject>>();
		result.put(NuclosEntity.RULEUSAGE.getEntityName(), subformctlUsage);
		return result;
	}

	@Override
	protected Collection<? extends SubFormController> getSubFormControllers(boolean bSearchTab) {
		ArrayList<SubFormController> result = new ArrayList<SubFormController>();
		if (!bSearchTab) {
			result.add(this.subformctlUsage);
		}
		return result;
	}

	@Override
	public EditView newSearchEditView(LayoutRoot layoutroot) {
		return null;
	}

	@Override
	protected void setupSubFormController(Map<String, SubForm> mpSubForm, Map<String, ? extends SubFormController> mpSubFormController) {

	}

	@Override
	protected LayoutRoot getInitialLayoutMLDefinitionForSearchPanel() {
		return null;
	}

	@Override
	public Map<String, DetailsSubFormController<CollectableEntityObject>> getDetailsSubforms() {
		return null;
	}

	@Override
	public void cmdGenerateObject(GeneratorActionVO generatoractionvo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GeneratorActionVO> getGeneratorActions() {
		return Collections.emptyList();
	}
}	// class RuleCollectController

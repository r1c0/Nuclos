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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.gef.DefaultShapeViewer;
import org.nuclos.client.gef.ShapeModel;
import org.nuclos.client.gef.shapes.TextShape;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.valuelistprovider.ProcessCollectableFieldsProvider;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.locale.LocaleCollectController;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.statemodel.StateModelEditor;
import org.nuclos.client.statemodel.panels.StateModelEditorPropertiesPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.RefreshValueListAction;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.client.valuelistprovider.EntityCollectableIdFieldsProvider;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * Controller for collecting state models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class StateModelCollectController extends NuclosCollectController<CollectableStateModel> {

	private static final Logger LOG = Logger.getLogger(StateModelCollectController.class);

	private final CollectPanel<CollectableStateModel> pnlCollect = new StateModelCollectPanel(false);

	private final MasterDataSubFormController subformctlUsages;
	private final StateModelEditPanel pnlEdit;

	private class StateModelCollectStateListener extends CollectStateAdapter {
		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {

			StateModelCollectController smcc = StateModelCollectController.this;
			final boolean bWriteAllowed = SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);

			smcc.pnlEdit.getHeader().getDescriptionField().setEnabled(bWriteAllowed);
			smcc.pnlEdit.getHeader().getNameField().setEnabled(bWriteAllowed);

			StateModelEditorPropertiesPanel smepp = smcc.pnlEdit.getStateModelEditor().getStateModelEditorPropertiesPanel();
			smepp.getTransitionRolePanel().getTblRoles().setEnabled(bWriteAllowed);
			smepp.getTransitionRolePanel().getBtnAdd().setEnabled(bWriteAllowed);
			smepp.getTransitionRolePanel().getBtnDelete().setEnabled(bWriteAllowed);
			smepp.getTransitionRulePanel().getTblRules().setEnabled(bWriteAllowed);
			smepp.getTransitionRulePanel().getBtnAdd().setEnabled(bWriteAllowed);
			smepp.getTransitionRulePanel().getBtnDelete().setEnabled(bWriteAllowed);
			smepp.getTransitionRulePanel().getBtnAutomatic().setEnabled(bWriteAllowed);
			smepp.getTransitionRulePanel().getBtnDefault().setEnabled(bWriteAllowed);
			// smepp.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformRoles().setEnabled(bWriteAllowed);
			// smepp.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformAttributeGroups().setEnabled(bWriteAllowed);

			smcc.subformctlUsages.getSubForm().setEnabled(bWriteAllowed);

		}

	}

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public StateModelCollectController(MainFrameTab tabIfAny) {
		super(CollectableStateModel.clcte, tabIfAny);

		// getSearchStrategy().setCompleteCollectablesStrategy(new CompleteCollectableStateModelsStrategy(this));

		this.initialize(this.pnlCollect);

		final SubForm subformUsages = new SubForm(NuclosEntity.STATEMODELUSAGE.getEntityName(), JToolBar.VERTICAL, "statemodel");

		// the value list for process is to contain the processes belonging to the selected module:
		subformUsages.addColumn(new SubForm.Column("process"));
		subformUsages.addColumn(new SubForm.Column("nuclos_module"));
		subformUsages.getColumn("process").setValueListProvider(new ProcessCollectableFieldsProvider());
		subformUsages.getColumn("process").addRefreshValueListAction(new RefreshValueListAction("process", NuclosEntity.STATEMODELUSAGE.getEntityName(), NuclosEntity.MODULE.getEntityName(), "moduleId"));
		EntityCollectableIdFieldsProvider moduleProvider = new EntityCollectableIdFieldsProvider();
		moduleProvider.setParameter("restriction", moduleProvider.ENTITIES_WITH_STATEMODEL_ONLY);
		subformUsages.getColumn("nuclos_module").setValueListProvider(moduleProvider);
		this.subformctlUsages = new MasterDataSubFormController(getTab(), this.getDetailsEditView().getModel(),
				this.getEntityName(), subformUsages, this.getPreferences(), this.getEntityPreferences(), valueListProviderCache);

		pnlEdit = new StateModelEditPanel(subformUsages);

		getTab().setLayeredComponent(pnlCollect);

		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.getHeader().newCollectableComponentsProvider()));

		setupShortcutsForTabs(getTab());

		this.pnlEdit.getStateModelEditor().addPrintEventListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				print(ev);
			}
		});

		this.getCollectStateModel().addCollectStateListener(new StateModelCollectStateListener());

		this.setupDetailsToolBar();

		// dividerlocations
		UIUtils.readSplitPaneStateFromPrefs(getPreferences(), getDetailsPanel());
		PropertyChangeListener dividerChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				UIUtils.writeSplitPaneStateToPrefs(getPreferences(), getDetailsPanel());
			}
		};
		pnlEdit.splitpnMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, dividerChangeListener);
		pnlEdit.splitpn.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, dividerChangeListener);
	}

	@Override
	public void close() {
		subformctlUsages.close();
		pnlEdit.getStateModelEditor().getStateModelEditorPropertiesPanel()
			.getStatePropertiesPanel().getStateDependantRightsPanel().close();
		super.close();
	}

	private void setupDetailsToolBar(){
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TRANSLATION)) {
			/*final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();
			JButton btnLocale = new JButton();
			btnLocale.setIcon(Icons.getInstance().getIconRelate());
			btnLocale.setToolTipText(SpringLocaleDelegate.getMessage("MetaDataCollectController.27", "\u00dcbersetzungstool"));
			btnLocale.setAction(new CommonAbstractAction(btnLocale) {
				@Override
                public void actionPerformed(ActionEvent ev) {
					runLocaleCollectControllerFor();
				}
			});

			toolbarCustomDetails.add(Box.createHorizontalStrut(5));
			toolbarCustomDetails.add(btnLocale);
			toolbarCustomDetails.add(Box.createHorizontalGlue());
			this.getDetailsPanel().setCustomToolBarArea(toolbarCustomDetails);*/
		}

	}

	public void runLocaleCollectControllerFor() {
		UIUtils.runCommandForTabbedPane(this.getTabbedPane(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				Collection<String> collres = new ArrayList<String>();
				Integer iModelId = (Integer)getSelectedCollectableId();
				if (iModelId != null) {
					StateGraphVO stateGraph = StateDelegate.getInstance().getStateGraph(iModelId);
					for (StateVO statevo : stateGraph.getStates()) {
						String sResourceIdForLabel = StateDelegate.getInstance().getResourceSIdForName(statevo.getId());
						String sResourceIdForDescription = StateDelegate.getInstance().getResourceSIdForDescription(statevo.getId());
						if (sResourceIdForLabel != null) collres.add(sResourceIdForLabel);
						if (sResourceIdForDescription != null) collres.add(sResourceIdForDescription);

					}
				}
				final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
				LocaleCollectController cntr = factory.newLocaleCollectController(collres, null);
				cntr.runViewSingleCollectableWithId(LocaleDelegate.getInstance().getDefaultLocale());
			}
		});

	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		this.pnlEdit.getStateModelEditor().addChangeListener(this.changelistenerDetailsChanged);
		this.subformctlUsages.getSubForm().addChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		if (this.pnlEdit != null && this.pnlEdit.getStateModelEditor() != null)
			this.pnlEdit.getStateModelEditor().removeChangeListener(this.changelistenerDetailsChanged);
		if (this.subformctlUsages != null && this.subformctlUsages.getSubForm() != null)
			this.subformctlUsages.getSubForm().removeChangeListener(this.changelistenerDetailsChanged);
	}

	@Override
	public CollectableStateModel findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		return new CollectableStateModel(StateDelegate.getInstance().getStateGraph((Integer) oId));
	}

	@Override
	protected CollectableStateModel findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}

	@Override
	protected void deleteCollectable(CollectableStateModel clct) throws CommonBusinessException {
		StateDelegate.getInstance().removeStateModel(clct.getStateModelVO());
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableStateModel clct) throws CommonBusinessException {
		// fill the textfields:
		super.unsafeFillDetailsPanel(clct);

		this.subformctlUsages.getSubForm().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pnlEdit.getStateModelEditor().setUsages(subformctlUsages.getCollectables());
			}
		});

		// fill the state model editor:
		final StateModelVO statemodelvo = clct.getStateModelVO();
		if (statemodelvo.getId() == null) {
			// @todo this should also be done via setStateGraph:
			this.pnlEdit.getStateModelEditor().createNewStateModel(statemodelvo);
		}
		else {
			this.pnlEdit.getStateModelEditor().setStateGraph(clct.getStateGraphVO());
		}

		this.subformctlUsages.fillSubForm(statemodelvo.getId());


		parseLayout();
	}

	private void parseLayout() {
		List<CollectableEntityObject> lstSub = this.subformctlUsages.getCollectables();
		for(CollectableEntityObject md : lstSub) {

			Integer iModule =  md.getEntityObjectVO().getFieldIds().get("nuclos_module").intValue();

			Integer iProcess = (md.getEntityObjectVO().getFieldId("process") == null) ? null : md.getEntityObjectVO().getFieldId("process").intValue();
			Integer iStatus = (md.getEntityObjectVO().getFieldId("state") == null) ? null : md.getEntityObjectVO().getFieldId("state").intValue();
			try {
				String sModuleName = Modules.getInstance().getEntityNameByModuleId(iModule);
	
				CollectableGenericObjectEntity e = new CollectableGenericObjectEntity(sModuleName, sModuleName, Collections.singletonList(""));
				LayoutRoot lRoot = GenericObjectLayoutCache.getInstance().getLayout(e
					, new UsageCriteria(iModule, iProcess, iStatus), false, null, valueListProviderCache);
	
				JComponent jcomp = lRoot.getRootComponent();
	
				List<JTabbedPane> lst = new ArrayList<JTabbedPane>();
	
				searchTabbedPanes(jcomp, lst);
	
				Map<String, String> mp = getTabbedPaneNames(lst);
	
				this.pnlEdit.getStateModelEditor().getStateModelEditorPropertiesPanel().getStatePropertiesPanel().getModel().setTabModelList(mp);
			} catch (NoSuchElementException e) {
				// ignore.
			}
			break;

		}
	}

	private Map<String, String> getTabbedPaneNames(List<JTabbedPane> lst) {
		Map<String, String> mp = new HashMap<String, String>();

		for(JTabbedPane tab : lst) {
			for(int i = 0; i < tab.getTabCount(); i++) {
				if(tab.getComponentAt(i).getName() != null)
					mp.put(tab.getComponent(i).getName(), tab.getTitleAt(i));
			}
		}

		return mp;
	}

	private void searchTabbedPanes(JComponent comp, List<JTabbedPane> lst) {
		if(comp instanceof JTabbedPane) {
			lst.add((JTabbedPane)comp);
		}
		if(comp.getComponents().length == 0)
			return;
		for(Component c : comp.getComponents()) {
			if(c instanceof JComponent){
				searchTabbedPanes((JComponent)c, lst);
			}
		}
	}

	@Override
	protected void readValuesFromEditPanel(CollectableStateModel clct, boolean bSearchTab) throws CollectableValidationException {
		// This controller has no search tab:
		assert !bSearchTab;

		// read the text fields:
		super.readValuesFromEditPanel(clct, bSearchTab);
	}

	@Override
	public CollectableStateModel newCollectable() {
		return new CollectableStateModel(new StateGraphVO(new StateModelVO()));
	}

	@Override
	protected CollectableStateModel updateCollectable(CollectableStateModel clct, Object oAdditionalData) throws CommonBusinessException {
		throw new CommonFatalException(getSpringLocaleDelegate().getMessage(
				"StateModelCollectController.2","Sammelbearbeitung ist hier noch nicht m\u00f6glich."));
	}

	@Override
	protected CollectableStateModel updateCurrentCollectable(CollectableStateModel clctEdited) throws CommonBusinessException {
		final StateModelEditor statemodeleditor = this.pnlEdit.getStateModelEditor();

		final Integer iUpdatedStateModelId = saveStateModelAndUsages(statemodeleditor, clctEdited.getStateModelVO());

		// reread the updated state model:
		StateDelegate.getInstance().invalidate();
		return new CollectableStateModel(StateDelegate.getInstance().getStateGraph(iUpdatedStateModelId));
	}

	@Override
	protected CollectableStateModel insertCollectable(CollectableStateModel clctNew) throws CommonBusinessException {
		final StateModelEditor statemodeleditor = this.pnlEdit.getStateModelEditor();

		final Integer iInsertedModelId = saveStateModelAndUsages(statemodeleditor, clctNew.getStateModelVO());

		// reread the inserted state model:
		StateDelegate.getInstance().invalidate();
		return new CollectableStateModel(StateDelegate.getInstance().getStateGraph(iInsertedModelId));
	}

	private Integer saveStateModelAndUsages(StateModelEditor statemodeleditor, StateModelVO statemodelvo) throws CommonBusinessException {
		// prepare the statemodeleditor for saving:
		final StateGraphVO stategraphvo = statemodeleditor.prepareForSaving(statemodelvo);

		final DependantMasterDataMap mpDependants = new DependantCollectableMasterDataMap(NuclosEntity.STATEMODELUSAGE.getEntityName(),
				statemodelvo.getId() == null ? subformctlUsages.getCollectables(true, true, true)
						: subformctlUsages.getAllCollectables(statemodelvo.getId(), null, true, null)).toDependantMasterDataMap();

		Integer intid = StateDelegate.getInstance().setStateGraph(stategraphvo, mpDependants);

		// invalidate client and server caches
		try {
			StateDelegate.getInstance().invalidateCache();
			SecurityDelegate.getInstance().invalidateCache();
			SecurityCache.getInstance().revalidate();
		}
		catch(Exception e) {
			throw new CommonBusinessException(getSpringLocaleDelegate().getMessage(
					"StateModelCollectController.1","Der serverseitige Cache konnte nicht invalidiert werden!"), e);
		}

		return intid;
	}

	/**
	 * Clone doesn't work (yet) for state models.
	 * @return false
	 */
	@Override
	protected boolean isCloneAllowed() {
		return false;
	}
	@Override
	protected boolean isSaveAllowed(){
		return SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity);
	}
	@Override
	protected boolean isDeleteSelectedCollectableAllowed(){
		return SecurityCache.getInstance().isDeleteAllowedForMasterData(sEntity);
	}
	@Override
	protected boolean isNewAllowed(){
		return isSaveAllowed();
	}

	@Override
	protected boolean stopEditingInDetails() {
		return this.pnlEdit.getStateModelEditor().stopEditing() && this.subformctlUsages.stopEditing();
	}

	@Override
	protected String getEntityLabel() {
		return getSpringLocaleDelegate().getMessage("StateModelCollectController.3","Statusmodell");
	}

	/**
	 * The printing of a statusmodel implemented by adding a title to the model
	 * and then the model is printed.
	 * @param ev ActionEvent
	 */
	private void print(ActionEvent ev) {
		final Object oSource = ev.getSource();
		if (!(oSource instanceof StateModelEditor)) {
			return;
		}

		// the changes to the model made here will be ignored
		final boolean bWasDetailsChangedIgnored = isDetailsChangedIgnored();
		setDetailsChangedIgnored(true);

		final StateModelEditor editor = (StateModelEditor) ev.getSource();
		final DefaultShapeViewer pnlShapeViewer = editor.getPnlShapeViewer();
		final ShapeModel model = pnlShapeViewer.getModel();

		final PrinterJob printerjob = PrinterJob.getPrinterJob();
		final PageFormat pageformat = printerjob.defaultPage();
		pageformat.setOrientation(PageFormat.LANDSCAPE);

		final TextShape shapeTitle = new TextShape();
		final Rectangle2D rectDim = model.getShapeDimension();
		final int x = 50;
		final int y = 0;
		final int iWidth = Math.max(50, (int) rectDim.getWidth() - 60);
		final int iHeight = 20;
		shapeTitle.setDimension(new Rectangle2D.Double(x, y, iWidth, iHeight));

		// the screenvalues of the name and the description are used
		final String sName = pnlEdit.getHeader().getNameField().getJTextField().getText();
		final String sDescription = pnlEdit.getHeader().getDescriptionField().getJTextField().getText();

		shapeTitle.setText(sName + " " + sDescription);
		shapeTitle.setBorderSize(0);
		shapeTitle.setColor(model.getView().getBgColor());

		// The title is temporary added to the model
		model.addShape(shapeTitle);

		printerjob.setPrintable(pnlShapeViewer, pageformat);
		if (printerjob.printDialog()) {
			try {
				printerjob.print();
			}
			catch (PrinterException ex) {
				Errors.getInstance().showExceptionDialog(null, ex.getMessage(), ex);
			}
		}
		model.removeShape(shapeTitle);
		setDetailsChangedIgnored(bWasDetailsChangedIgnored);

	}

	private class StateModelCollectPanel extends CollectPanel<CollectableStateModel> {

		StateModelCollectPanel(boolean bSearchPanelAvailable) {
			super(bSearchPanelAvailable, ClientParameterProvider.getInstance().isNuclosUIDetailsOverlay(getEntity()));
		}

		@Override
		public ResultPanel<CollectableStateModel> newResultPanel() {
			return new NuclosResultPanel<CollectableStateModel>();
		}

		@Override
		public DetailsPanel newDetailsPanel() {
			return new DetailsPanel(false);
		}


	}
}	// class StateModelCollectController

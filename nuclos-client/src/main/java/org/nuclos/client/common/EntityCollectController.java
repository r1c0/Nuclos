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
package org.nuclos.client.common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.nuclos.api.context.ScriptContext;
import org.nuclos.client.common.AbstractDetailsSubFormController.DetailsSubFormTableModel;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.genericobject.GenerationController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.scripting.context.CollectControllerScriptContext;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.EditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.SearchEditModel;
import org.nuclos.client.ui.collect.result.ResultActionCollection;
import org.nuclos.client.ui.collect.result.ResultController;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.common.PointerCollection;
import org.nuclos.common.PointerException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.validation.FieldValidationError;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

public abstract class EntityCollectController<Clct extends Collectable> extends NuclosCollectController<Clct> {

	private static final Logger LOG = Logger.getLogger(EntityCollectController.class);

	private final static String loadingLabelText = SpringLocaleDelegate.getInstance().getMessage("entity.collect.controller.loading.label", "Ladevorgang...");
	private final static String notLoadingLabelText = "              ";
	protected JLabel loadingLabel;
	protected SubFormsLoader subFormsLoader;

	private static final Icon iconPointer = Icons.getInstance().getIconAbout16();
	protected final JButton btnPointer = new JButton(getPointerAction());
	private PointerCollection pointerCollection = null;
	private NuclosBusinessRuleException pointerException = null;
	private List<ActionListener> lstPointerChangeListener = new ArrayList<ActionListener>();

	protected Map<String, SearchConditionSubFormController> mpsubformctlSearch;
	
	private ResultActionsWorker runningResultActionsWorker;

	/**
	 * Don't make this public!
	 *
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected EntityCollectController(String sEntityName, MainFrameTab tabIfAny) {
		this(NuclosCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName), tabIfAny);
	}

	/**
	 * Don't make this public!
	 *
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected EntityCollectController(CollectableEntity clcte, MainFrameTab tabIfAny) {
		super(clcte, tabIfAny);
		this.loadingLabel = new JLabel(notLoadingLabelText);
		this.loadingLabel.setName("loadingLabel");
		subFormsLoader = new SubFormsLoader();
		this.addPointerChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPointer.setEnabled(btnPointer.getAction().isEnabled());
			}
		});
		btnPointer.addMouseListener(getPointerContextListener());
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
	protected EntityCollectController(String sEntityName, MainFrameTab tabIfAny, ResultController<Clct> rc) {
		this(NuclosCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName), tabIfAny, rc);
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
	protected EntityCollectController(CollectableEntity clcte, MainFrameTab tabIfAny, ResultController<Clct> rc) {
		super(clcte, tabIfAny, rc);
		this.loadingLabel = new JLabel(notLoadingLabelText);
		this.loadingLabel.setName("loadingLabel");
		subFormsLoader = new SubFormsLoader();
		this.addPointerChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPointer.setEnabled(btnPointer.getAction().isEnabled());
			}
		});
		btnPointer.setName("btnPointer");
		btnPointer.addMouseListener(getPointerContextListener());
	}

	@Override
	public void close() {
		super.close();
		if (mpsubformctlSearch != null) {
			for (SearchConditionSubFormController search: mpsubformctlSearch.values()) {
				search.close();
			}
			mpsubformctlSearch.clear();
		}
		mpsubformctlSearch = null;
	}

	protected void showLoading(boolean loading){
		synchronized (getTab()) {
			String sTitle = getTab().getTitle();
			if(loading){
				this.loadingLabel.setText(loadingLabelText);
				this.loadingLabel.revalidate();
				//setTitle((sTitle != null ? sTitle+" " : "") +loadingLabelText);
			} else {
				this.loadingLabel.setText(notLoadingLabelText);
				this.loadingLabel.revalidate();
				if(sTitle != null){
					StringBuffer sbTitle = new StringBuffer(sTitle);
					int loadingLabelTextPosition = sbTitle.indexOf(loadingLabelText);
					if(loadingLabelTextPosition >=0){
						//setTitle(sbTitle.substring(0,loadingLabelTextPosition));
					}
				}
			}
		}
	}

	/**
	 * creates a searchable subform ctl for each subform. If the subform is disabled, the controller will be disabled.
	 * @param mpSubForms
	 */
	protected Map<String, SearchConditionSubFormController> newSearchConditionSubFormControllers(Map<String, SubForm> mpSubForms) {
		final String sParentEntityName = this.getEntityName();

		final SearchEditModel editmodelSearch = getSearchPanel().getEditModel();

		return CollectionUtils.transformMap(mpSubForms, new Transformer<SubForm, SearchConditionSubFormController>() {
			@Override
			public SearchConditionSubFormController transform(SubForm subform) {
				return newSearchConditionSubFormController(subform, sParentEntityName, editmodelSearch);
			}
		});
	}

	/**
	 * @todo maybe move to CollectController?
	 * @param subform
	 * @param clctcompmodelprovider
	 */
	protected SearchConditionSubFormController newSearchConditionSubFormController(SubForm subform, String sParentEntityName,
		CollectableComponentModelProvider clctcompmodelprovider) {

		final String sControllerType = subform.getControllerType();
		if (sControllerType != null && !sControllerType.equals("default"))
			LOG.warn("Kein spezieller SearchConditionSubFormController f?r Controllertyp " + sControllerType + " vorhanden.");
		return _newSearchConditionSubFormController(clctcompmodelprovider, sParentEntityName, subform);
	}

	protected SearchConditionSubFormController _newSearchConditionSubFormController(CollectableComponentModelProvider clctcompmodelprovider,
		String sParentEntityName, SubForm subform) {

		// if parent of subform is another subform, change given parent entity name
		String sParentSubForm = subform.getParentSubForm();
		if (sParentSubForm != null)
			sParentEntityName = sParentSubForm;

		return new SearchConditionSubFormController(getTab(), clctcompmodelprovider, sParentEntityName, subform,
			getPreferences(), getEntityPreferences(), MasterDataCollectableFieldsProviderFactory.newFactory(null, valueListProviderCache));
	}

	@Override
	protected void initialize(CollectPanel<Clct> pnlCollect) {
		super.initialize(pnlCollect);
		this.getCollectStateModel().addCollectStateListener(new EntityCollectStateListener());
		this.addCollectableEventListener(new PointerResetChangeEventListener());
		this.getResultPanel().getResultTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					getResultPanel().loadingResultActions();
					Collection<Clct> selectedCollectablesFromResult = getResultController().getSelectedCollectablesFromTableModel();
					ResultActionsWorker worker = new ResultActionsWorker(selectedCollectablesFromResult);
					worker.execute();
				}
			}
		});
	}
	
	protected List<ResultActionCollection> getResultActionsMultiThreaded(Collection<Clct> selectedCollectablesFromResult) {
		List<ResultActionCollection> result = new ArrayList<ResultActionCollection>();
		ResultActionCollection rac = new ResultActionCollection(SpringLocaleDelegate.getInstance().getMessage("ResultPanel.12","Arbeitsschritte"));
		result.add(rac);
		
		List<GeneratorActionVO> actions = getGeneratorActions(selectedCollectablesFromResult);
		if (actions!=null && !actions.isEmpty()) {
			for(final GeneratorActionVO actionVO : getGeneratorActions(selectedCollectablesFromResult)) {
				AbstractAction act = new AbstractAction(actionVO.toString(), 
						actionVO.getButtonIcon()==null?null:ResourceCache.getInstance().getIconResource(actionVO.getButtonIcon().getId())) {
							@Override
							public void actionPerformed(ActionEvent e) {
								cmdGenerateObject(actionVO);
							}
						};
				rac.addAction(act);
			}
		}
		return result;
	}

	protected void initSearchSubforms() {
		if(!this.isSearchPanelAvailable())
			return;
		final LayoutRoot layoutrootSearch = getInitialLayoutMLDefinitionForSearchPanel();
		layoutrootSearch.getRootComponent().setFocusCycleRoot(true);
		getSearchPanel().setEditView(newSearchEditView(layoutrootSearch));
		Map<String, SubForm> mpSubForm = layoutrootSearch.getMapOfSubForms();
		mpsubformctlSearch = newSearchConditionSubFormControllers(mpSubForm);
		setupSubFormController(mpSubForm, mpsubformctlSearch);
	}

	protected Map<String, SearchConditionSubFormController> getMapOfSubFormControllersInSearch() {
		return mpsubformctlSearch;
	}

	public abstract EditView newSearchEditView(LayoutRoot layoutroot);


	protected abstract void setupSubFormController(Map<String, SubForm> mpSubForm, Map<String, ? extends SubFormController> mpSubFormController);

	protected abstract LayoutRoot getInitialLayoutMLDefinitionForSearchPanel();


	public SubFormsLoader getSubFormsLoader() {
		return subFormsLoader;
	}

	protected void initSubFormsLoader(){
		getSubFormsLoader().initLoaderStates();
	}

	protected abstract Map<String, DetailsSubFormController<CollectableEntityObject>> getMapOfSubFormControllersInDetails();

	/**
	 * @postcondition result != null
	 */
	protected Collection<DetailsSubFormController<CollectableEntityObject>> getSubFormControllersInDetails() {
		return CollectionUtils.valuesOrEmptySet(this.getMapOfSubFormControllersInDetails());
	}

	@Override
	protected void unsafeFillMultiEditDetailsPanel(Collection<Clct> collclct) throws CommonBusinessException {
		super.unsafeFillMultiEditDetailsPanel(collclct);

		for (DetailsSubFormController<CollectableEntityObject> subformctl : this.getSubFormControllersInDetails()) {
			if (subformctl instanceof MasterDataSubFormController) {
				for (MasterDataSubFormController child : ((MasterDataSubFormController)subformctl).getChildSubFormController()) {
					child.getSubForm().setEnabled(false);
				}
			}

			subformctl.setMultiEdit(true);
		}
	}

	@Override
	protected boolean isSaveAllowed() {
		return super.isSaveAllowed() && isNotLoadingSubForms();
	}

	protected boolean isNotLoadingSubForms() {
		return !getSubFormsLoader().isLoadingSubForms();
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed() {
		return super.isDeleteSelectedCollectableAllowed() && isNotLoadingSubForms();
	}

	@Override
	protected boolean isCloneAllowed() {
		return super.isCloneAllowed() && isNotLoadingSubForms();
	}

	public MasterDataSubFormController newDetailsSubFormController(SubForm subform,
			String sParentEntityName, CollectableComponentModelProvider clctcompmodelprovider,
			MainFrameTab tab, JComponent compDetails, Preferences prefs, EntityPreferences entityPrefs) {
		//subform.setLockedLayer();
		MasterDataSubFormController controller = NuclosCollectControllerFactory.getInstance().newDetailsSubFormController(subform,
				sParentEntityName, clctcompmodelprovider, tab, compDetails, prefs, entityPrefs, valueListProviderCache);
		controller.setParentController((EntityCollectController<CollectableEntityObject>) this);
		return controller;
	}

	public abstract Map<String, DetailsSubFormController<CollectableEntityObject>> getDetailsSubforms();

	public Collection<String> getAdditionalLoaderNames(){
		List<String> emptyList = Collections.emptyList();
		return emptyList;
	}

	/**
	 * switches to "New" mode and fills the Details panel with the contents of the selected Collectable.
	 * @throws CommonBusinessException
	 */
	@Override
	protected void cloneSelectedCollectable() throws CommonBusinessException {
		final Clct clctBackup = this.getCompleteSelectedCollectable();
		//all subform workers will be interrupted and subforms will be stay disabled. see EntityCollectStateListener.detailsModeLeft.
		this.enterNewChangedMode();

		//the second switch to DETAILSMODE_NEW_CHANGED is needed, to activate the right Buttons !
		this.subFormsLoader.setAfterLoadingRunnable(new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				// load all child subform data
				for (DetailsSubFormController<CollectableEntityObject> subforms : getSubFormControllersInDetails()) {
					if (subforms instanceof MasterDataSubFormController) {
						final String sEntityName = subforms.getEntityAndForeignKeyFieldName().getEntityName();
						EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(sEntityName);
						if (meta.isDynamic() || meta.isVirtual() || meta.isStateModel()) {
							((MasterDataSubFormController)subforms).clear();
						}
						else {
							((MasterDataSubFormController)subforms).fillAllChildSubForms();
							subforms.selectFirstRow();
						}
					}
				}

				setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			}
		});
		this.safeFillDetailsPanel(clctBackup);
	}

	/**
	 * switches to New changed mode
	 */
	protected void enterNewChangedMode() {
		if (this.stopEditingInDetails()) {
			try {
				this.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(this.getTab(), ex);
			}
		}
	}

	/**
	 * save scroll position and selection of subforms an restore after reloading has finished (NUCLOSINT-851)
	 */
	@Override
	public void refreshCurrentCollectable() throws CommonBusinessException {
		this.refreshCurrentCollectable(true);
	}

	/**
	 * save scroll position and selection of subforms an restore after reloading has finished (NUCLOSINT-851)
	 */
	@Override
    public void refreshCurrentCollectable(boolean withMultiThreader) throws CommonBusinessException {
		final Map<String, Rectangle> visibleRectangles = new HashMap<String, Rectangle>();
		final Map<String, Object> selectedRows = new HashMap<String, Object>();

		for (DetailsSubFormController<?> controller : getSubFormControllersInDetails()) {
			visibleRectangles.put(controller.getEntityAndForeignKeyFieldName().getEntityName(), controller.getSubForm().getJTable().getVisibleRect());
			if (controller.getSelectedCollectable() != null) {
				selectedRows.put(controller.getEntityAndForeignKeyFieldName().getEntityName(), controller.getSelectedCollectable().getId());
			}
		}
		this.getSubFormsLoader().setAfterLoadingRunnable(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				for (String entity : visibleRectangles.keySet()) {
					for (DetailsSubFormController<?> controller : getSubFormControllersInDetails()) {
						if (entity.equals(controller.getEntityAndForeignKeyFieldName().getEntityName())) {
							controller.getJTable().scrollRectToVisible(visibleRectangles.get(entity));
							if (selectedRows.containsKey(entity)) {
								DetailsSubFormTableModel<?> subformtablemodel = (DetailsSubFormTableModel<?>) controller.getSubFormTableModel();
								int index = subformtablemodel.findRowById(selectedRows.get(entity));
								if (index > -1) {
									int viewindex = controller.getJTable().convertRowIndexToView(index);
									controller.getJTable().getSelectionModel().setSelectionInterval(viewindex, viewindex);
								}
							}
						}
					}
				}
				getSubFormsLoader().setAfterLoadingRunnable(null);
			}
		});
	    super.refreshCurrentCollectable(withMultiThreader);
    }

	// =============== Additional ChangeListeners for search ===============

	/**
	 * @deprecated Move to SearchController specialization.
	 */
	@Override
	public void addAdditionalChangeListenersForSearch() {
		this.addAdditionalChangeListeners(true);
	}

	/**
	 * @deprecated Move to SearchController specialization.
	 */
	protected void addAdditionalChangeListeners(boolean bSearchable) {
		for (SubFormController subformctl : getSubFormControllers(bSearchable)) {
			subformctl.getSubForm().addChangeListener(getChangeListener(bSearchable));
		}
	}

	protected abstract Collection<? extends SubFormController> getSubFormControllers(boolean bSearchTab);

	protected ChangeListener getChangeListener(boolean bSearchable) {
		return bSearchable ? this.changelistenerSearchChanged : this.changelistenerDetailsChanged;
	}

	// =============== inner classes ===============

	/* private inner class SubFormsLoader */
	protected class SubFormsLoader {
		private boolean loading;
		private boolean suspended;

		private Map<String, Boolean> subFormsLoadState;
		private Map<String, SubFormsInterruptableClientWorker> subFormsClientWorker;

		private Map<String, Boolean> subFormsSuspendedState;

		private CommonRunnable afterLoadingRunnable;

		public SubFormsLoader(){
			this.loading = false;
			this.suspended = false;
			this.subFormsLoadState = new HashMap<String, Boolean>();
			this.subFormsClientWorker = new HashMap<String, SubFormsInterruptableClientWorker>();
			this.subFormsSuspendedState = new HashMap<String, Boolean>();
			this.afterLoadingRunnable = null;
		}

		private void initLoaderStates() {
			if(!getCollectState().isDetailsModeMultiViewOrEdit()){
				synchronized (this) {
					// only a keys from MapOfSubFormControllersInDetails as entity name is not enough here because of m:n enities
					for (DetailsSubFormController<CollectableEntityObject> subformctl : EntityCollectController.this.getMapOfSubFormControllersInDetails().values()) {
						if (StringUtils.isNullOrEmpty(subformctl.getSubForm().getParentSubForm())) {
							this.subFormsLoadState.put(subformctl.getCollectableEntity().getName(), new Boolean(false));
						}
					}
					//additional loader (calculated attributes)
					for (String addLoader : EntityCollectController.this.getAdditionalLoaderNames()) {
						this.subFormsLoadState.put(addLoader, new Boolean(false));
					}
					this.loading = false;
				}
			}
		}

		public void startLoading(){
			if(!getCollectState().isDetailsModeMultiViewOrEdit()){
				synchronized (this) {
					//if(!this.suspended){
					//	this.suspendedForms.clear();
					//}
					resetLoaderStates();
					if(!this.subFormsLoadState.isEmpty()){
						this.loading = true;
						EntityCollectController.this.disableToolbarButtons();
						EntityCollectController.this.showLoading(true);
						LOG.debug("loading started");
					}
				}
			}
		}
		public void startLoading(String subFormName){
			if(!getCollectState().isDetailsModeMultiViewOrEdit()){
				synchronized (this) {
					//if(!this.suspended){
					//	this.suspendedForms.clear();
					//}
					setSubFormLoaded(subFormName, false);
					
					if(!this.subFormsLoadState.isEmpty()){
						this.loading = true;
						EntityCollectController.this.disableToolbarButtons();
						EntityCollectController.this.showLoading(true);
						LOG.debug("loading started");
					}
					
					SubFormsInterruptableClientWorker clientWorker = this.subFormsClientWorker.get(subFormName);
					if (clientWorker != null) {
						addSubFormClientWorker(subFormName, clientWorker);
					}
				}
			}
		}

		public void finishLoading() {
			if(!getCollectState().isDetailsModeMultiViewOrEdit()){
				if(!this.subFormsLoadState.isEmpty()){
					this.loading = false;
					EntityCollectController.this.showLoading(false);
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							EntityCollectController.this.enableToolbarButtonsForDetailsMode(CollectState.DETAILSMODE_VIEW);
						}
					});
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							ScriptContext ctx = new CollectControllerScriptContext(EntityCollectController.this, new ArrayList<DetailsSubFormController<?>>(getSubFormControllersInDetails()));
							for (CollectableComponent c : getDetailsPanel().getEditView().getCollectableComponents()) {
								c.setComponentState(ctx, null);
							}
						}
					});
					if(this.afterLoadingRunnable != null){
						try {
							this.afterLoadingRunnable.run();
						} catch (CommonBusinessException e) {
							LOG.info("finishedLoading failed: " + e, e);
						} finally {
							this.afterLoadingRunnable = null;
						}
					}
				}
			}
		}

		public void resetLoaderStates(){
			this.subFormsLoadState.clear();
			initLoaderStates();
		}

		public boolean isLoadingSubForms(){
			return this.loading;
		}

		public boolean haveUnloadedSubforms(){
			return this.subFormsLoadState.containsValue(new Boolean(false));
		}

		public boolean isSubFormLoaded(String subFormName){
			return this.subFormsLoadState.get(subFormName);
		}

		public void setSubFormLoaded(String subFormName, boolean loaded){
			synchronized (this) {
				this.subFormsLoadState.put(subFormName, loaded);
				/*if(loaded){ // we want to reuse the clientworkers.
					this.subFormsClientWorker.remove(subFormName);
				}*/
				if(!this.haveUnloadedSubforms()){
					this.finishLoading();
				}
			}
		}

		public void addSubFormClientWorkerInSuspendedMode(String subFormName, final SubFormsInterruptableClientWorker worker){
			if(this.subFormsSuspendedState.containsKey(subFormName)){
				if(this.subFormsSuspendedState.get(subFormName).booleanValue()){
					setSubFormLoaded(subFormName, true);
				} else {
					startRunningWorker(subFormName, worker);
				}
			} else {
				startRunningWorker(subFormName, worker);
			}
		}

		public void addSubFormClientWorker(String subFormName, final SubFormsInterruptableClientWorker worker){
			if(!this.subFormsSuspendedState.isEmpty()){
				addSubFormClientWorkerInSuspendedMode(subFormName, worker);
			} else {
				startRunningWorker(subFormName, worker);
			}
		}

		private void startRunningWorker(String subFormName, final SubFormsInterruptableClientWorker worker) {
			this.subFormsClientWorker.put(subFormName, worker);
			CommonMultiThreader.getInstance().execute(worker);
		}

		public void suspendRunningClientWorkers(){
			synchronized (this) {
				if(!this.suspended){
					this.suspended = true;
					List<String> notLoadedFoms = getSubFormsWithState(false);
					for(String notLoadedFom : notLoadedFoms){
						SubFormsInterruptableClientWorker notLoadedWorker = this.subFormsClientWorker.get(notLoadedFom);
						if(notLoadedWorker != null){
							notLoadedWorker.interrupt();
						}
					}
					this.subFormsSuspendedState.putAll(this.subFormsLoadState);
				}
			}
		}

		public void addSuspendedClientWorker(String workerName){
			this.subFormsSuspendedState.put(workerName, false);
		}

		public boolean hasSuspendedClientWorkers(){
			synchronized (this) {
				return this.subFormsSuspendedState.containsValue(false);
			}
		}

		public void resume(){
			synchronized (this) {
				this.subFormsSuspendedState.clear();
				this.suspended = false;
			}
		}

		public void interruptAllClientWorkers(){
			synchronized (this) {
				if(this.suspended){
					return;
				}
				for (SubFormsInterruptableClientWorker worker : this.subFormsClientWorker.values()) {
					worker.interrupt();
				}
				this.subFormsClientWorker.clear();
				this.initLoaderStates();
				EntityCollectController.this.showLoading(false);
			}
		}

		public List<String> getSubFormsWithState(boolean loaded){
			List<String> subFormNames = new ArrayList<String>();
			for (String subformctlName : this.subFormsLoadState.keySet()) {
				if(this.subFormsLoadState.get(subformctlName).booleanValue() == loaded){
					subFormNames.add(subformctlName);
				}
			}
			return subFormNames;
		}

		public void setAfterLoadingRunnable(CommonRunnable pAfterLoadingRunnable) {
			this.afterLoadingRunnable = pAfterLoadingRunnable;
		}
	} // inner class SubFormsLoader

	/* private abstract inner class SubFormsInterruptableClientWorker */
	protected abstract class SubFormsInterruptableClientWorker implements CommonClientWorker {
		protected volatile boolean interrupted = false;

		public void interrupt(){
			this.interrupted = true;
		}

		@Override
        public abstract void init() throws CommonBusinessException;

		@Override
        public abstract void work() throws CommonBusinessException;

		@Override
        public abstract void paint() throws CommonBusinessException;

		@Override
        public abstract JComponent getResultsComponent();

		@Override
        public abstract void handleError(Exception ex);
	} // inner class SubFormsInterruptableClientWorker

	/* private inner class EntityCollectStateListener */
	private class EntityCollectStateListener extends CollectStateAdapter {
		@Override
		public void resultModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			//EntityCollectController.this.getSubFormsLoader().interruptAllClientWorkers();
			if(!(ev.getNewCollectState().isDetailsMode() && CollectState.isDetailsModeViewOrEdit(ev.getNewCollectState().getInnerState())) && !getCollectState().isDetailsModeMultiViewOrEdit()){
				EntityCollectController.this.getSubFormsLoader().interruptAllClientWorkers();
			}
		}

		@Override
		public void detailsModeLeft(CollectStateEvent ev) {
			if(!(ev.getNewCollectState().isDetailsMode() && ((CollectState.isDetailsModeViewOrEdit(ev.getNewCollectState().getInnerState()) || (CollectState.DETAILSMODE_NEW_CHANGED == ev.getNewCollectState().getInnerState())))) && !getCollectState().isDetailsModeMultiViewOrEdit()){
			//if(!(ev.getNewCollectState().isDetailsMode() && CollectState.isDetailsModeEdit(ev.getNewCollectState().getInnerState())) && !getCollectState().isDetailsModeMultiViewOrEdit()){
				EntityCollectController.this.getSubFormsLoader().interruptAllClientWorkers();
			}

		// NUCLEUSINT-1047 reload selected collectable
			final int iDetailsMode = ev.getOldCollectState().getInnerState();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (isClosed()) {
							return;
						}

						if(iDetailsMode == CollectState.DETAILSMODE_EDIT || iDetailsMode == CollectState.DETAILSMODE_VIEW) {
							if(getResultTable().getSelectedRow() >= 0) {
								getResultController().replaceCollectableInTableModel(readSelectedCollectable());
							}
						}
					}
					catch(CommonBusinessException ex) {
						throw new CommonFatalException(ex);
					}
				}
			});
		}

	}	// inner class DefaultCollectStateListener

	/**
	 *
	 * handle reset of <code>PointerCollection</code>
	 */
	private class PointerResetChangeEventListener implements CollectableEventListener {
		@Override
		public void handleCollectableEvent(Collectable collectable,	org.nuclos.client.ui.collect.CollectController.MessageType messageType) {
			switch(messageType) {
				case REFRESH_DONE_DIRECTLY :
				case EDIT_DONE :
				case STATECHANGE_DONE :
				case DELETE_DONE :
				case NEW_DONE :
				case CLCT_LEFT :
					setPointerInformation(null, null);
					break;
			}
		}
	}

	protected class PointerContextListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {

			if (SwingUtilities.isRightMouseButton(e)) {
				if (pointerException != null) {
					final JPopupMenu menu = new JPopupMenu();
					final String itemLabel = getSpringLocaleDelegate().getMessage("EntityCollectController.1", "Herkunft anzeigen") + "...";
					final JMenuItem originItem = new JMenuItem(itemLabel, iconPointer);
					originItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Errors.getInstance().showDetailedExceptionDialog(EntityCollectController.this.getTab(), pointerException);
						}
					});
					menu.add(originItem);
					menu.show(btnPointer, 0, btnPointer.getPreferredSize().height);
				}
			}
		}

	}

	/**
	 *
	 * displays <code>PointerCollection</code> in a JPopupMenu
	 */
	protected class PointerAction extends AbstractAction {

		public PointerAction() {
			super(null, iconPointer);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final PointerCollection pc = EntityCollectController.this.pointerCollection;
			if (pc != null) {
				final JPopupMenu menu = new JPopupMenu();

				if (pc.getMainPointer().message != null) {
					final String itemLabel = getSpringLocaleDelegate().getMessage("EntityCollectController.2", "Hinweis");
					final JMenuItem mainItem = new JMenuItem(itemLabel, iconPointer);
					mainItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							showPointerBubble(btnPointer, pc.getLocalizedMainPointer());
						}
					});
					menu.add(mainItem);
					menu.addSeparator();
				}

				final String fieldNotFoundLabel = getSpringLocaleDelegate().getMessage("EntityCollectController.3", "unbekanntes Attribut");
				final List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
				for (final String field : pc.getFields()) {
					EntityFieldMetaDataVO efMeta;
					try {
						efMeta = MetaDataClientProvider.getInstance().getEntityField(getEntity(), field);
					} catch (Exception ex) {
						efMeta = null;
					}
					final boolean fieldNotFound = efMeta == null;
					final String fieldLabel = fieldNotFound ? (field + " (" + fieldNotFoundLabel + ")")
							: getSpringLocaleDelegate().getLabelFromMetaFieldDataVO(efMeta);
					final JMenuItem fieldItem = new JMenuItem(fieldLabel);

					fieldItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (fieldNotFound) {
								showPointerBubble(btnPointer, getHtmlList(pc.getLocalizedFieldPointers(field)));
							} else {
								boolean fieldNotInLayout = true;
								for (final CollectableComponent clctcomp : getDetailCollectableComponentsFor(field)) {
									if (getDetailsPanel().ensureComponentIsVisible(clctcomp.getControlComponent())) {
										fieldNotInLayout = false;
										if (pc.hasFieldPointers(field))
											showPointerBubble(clctcomp.getControlComponent(), getHtmlList(pc.getLocalizedFieldPointers(field)));
										else {
											getDetailsPanel().spotComponent(clctcomp.getControlComponent());
										}
										clctcomp.getControlComponent().requestFocusInWindow();
										break;
									}
								}
								if (fieldNotInLayout) {
									showPointerBubble(btnPointer, getSpringLocaleDelegate().getMessage(
											"EntityCollectController.4", "Attribut nicht im Layout gefunden!") +
										"<br/>" + getHtmlList(pc.getLocalizedFieldPointers(field)));
								}
							}
						}
					});
					menuItems.add(fieldItem);
				}

				for (JMenuItem menuItem : CollectionUtils.sorted(menuItems, new Comparator<JMenuItem>() {
					@Override
					public int compare(JMenuItem o1, JMenuItem o2) {
						return o1.getText().compareToIgnoreCase(o2.getText());
					}}))
					menu.add(menuItem);
				menu.show(btnPointer, 0, btnPointer.getPreferredSize().height);
			}
		}

		@Override
		public void setEnabled(boolean b) {
			// ignore
		}

		@Override
		public boolean isEnabled() {
			return EntityCollectController.this.pointerCollection != null;
		}

	} // inner class PointerAction

	/**
	 *
	 * @param comp
	 * @param htmlMessage
	 */
	protected void showPointerBubble(final JComponent comp, final String htmlMessage) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					(new Bubble(comp, "<html>"+htmlMessage.replaceAll("\n", "<br/>")+"</html>", 8, Bubble.Position.SW_CONTAINER_RELATIVE)).setVisible(true);
				} catch (IllegalComponentStateException e) {
					// do nothing. it is not shown
				}
			}
		});
	}

	/**
	 *
	 * @param pc
	 */
	protected void showPointers(final PointerCollection pc) {
		if (pc != null) {
			if (pc.getMainPointer().message != null) {
				showPointerBubble(btnPointer, pc.getLocalizedMainPointer());
			}
			for (final String field : pc.getFields()) {
				if (pc.hasFieldPointers(field)) {
					for (final CollectableComponent clctcomp : getDetailCollectableComponentsFor(field)) {
						if (getDetailsPanel().ensureComponentIsVisible(clctcomp.getControlComponent())) {
							showPointerBubble(clctcomp.getControlComponent(), getHtmlList(pc.getLocalizedFieldPointers(field)));
							break;
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * @param lstMessages
	 * @return
	 */
	private String getHtmlList(List<String> lstMessages) {
		String htmlMessage = "";

		for (int i = 0; i < lstMessages.size(); i++) {
			String message = lstMessages.get(i);
			if (lstMessages.size() > 1)
				htmlMessage = htmlMessage + "- ";
			htmlMessage = htmlMessage + message;
			if (i < lstMessages.size()-1)
				htmlMessage = htmlMessage + "<br/>";
		}

		return htmlMessage;
	}

	/**
	 *
	 * @return
	 */
	protected Action getPointerAction() {
		return new PointerAction();
	}

	/**
	 *
	 * @return
	 */
	protected MouseListener getPointerContextListener() {
		return new PointerContextListener();
	}

	/**
	 *
	 * @return
	 */
	public PointerCollection getPointerCollection() {
		return pointerCollection;
	}

	/**
	 *
	 * @param pc
	 */
	public void setPointerInformation(PointerCollection pc, NuclosBusinessRuleException nbrex) {
		this.pointerCollection = pc;
		this.pointerException = nbrex;

		showPointers(pc);

		for (ActionListener al : this.lstPointerChangeListener) {
			al.actionPerformed(new ActionEvent(EntityCollectController.this, 0, "setPointerCollection"));
		}
	}

	/**
	 *
	 * @param al
	 */
	public void addPointerChangeListener(ActionListener al) {
		this.lstPointerChangeListener.add(al);
	}

	/**
	 *
	 * @param al
	 */
	public void removePointerChangeListener(ActionListener al) {
		this.lstPointerChangeListener.remove(al);
	}

	/**
	 * shows <code>NuclosBusinessRuleException</code>
	 *   and <code>PointerException<code>
	 *    in "Pointer-Style"...
	 * @param ex
	 * @return true if exception handled
	 */
	public boolean handlePointerException(Exception ex) {
		if (getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_VIEW ||
			getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_EDIT ||
			getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_NEW_CHANGED) {
			final PointerException pex = PointerException.extractPointerExceptionIfAny(ex);
			final NuclosBusinessRuleException nbrex = NuclosBusinessRuleException.extractNuclosBusinessRuleExceptionIfAny(ex);
			if (pex != null) {
				final PointerCollection pc = pex.getPointerCollection();

				if (ex.getMessage() != null && pc.getMainPointer() == null) { // if pointer collection has no main information set exception message
					pc.setMainPointer(ex.getMessage());
				}
				if (ex instanceof NuclosBusinessRuleException) {
					setPointerInformation(pc, (NuclosBusinessRuleException) ex);
				} else {
					setPointerInformation(pc, pex);
				}
				LOG.error(ex);
				return true;
			} else if (nbrex != null) {
				String exceptionMessage;
				String originMessage = NuclosBusinessRuleException.extractOriginFromNuclosBusinessRuleExceptionIfAny(nbrex);
				if (originMessage != null) {
					exceptionMessage = originMessage;
				} else {
					exceptionMessage = Errors.getReasonableMessage(nbrex);
				}

				if (exceptionMessage != null) {
					final PointerCollection pc = new PointerCollection(Errors.formatErrorForBubble(exceptionMessage));
					setPointerInformation(pc, nbrex);
					LOG.error(ex);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected boolean handleSpecialException(Exception ex) {
		if (handlePointerException(ex)) {
			return true;
		}
		else if (handleCommonValidationException(ex)) {
			return true;
		}
		else {
			return super.handleSpecialException(ex);
		}
	}

	protected boolean handleCommonValidationException(Exception ex) {
		CommonValidationException cve = Errors.getCause(ex, CommonValidationException.class);
		if (cve != null) {
			List<String> messages = new ArrayList<String>();
			messages.add(getSpringLocaleDelegate().getText("common.exception.novabitvalidationexception"));

			if (cve.getErrors() != null) {
				for (String error : cve.getErrors()) {
					messages.add(getSpringLocaleDelegate().getMessageFromResource(error));
				}
			}

			if (cve.getFieldErrors() != null) {
				for (FieldValidationError error : cve.getFieldErrors()) {
					if (!getEntityName().equals(error.getEntity())) {
						messages.add(getSpringLocaleDelegate().getMessage("EntityCollectController.Subform", "Subform \"{0}\": ", error.getEntity())
								+ getSpringLocaleDelegate().getMessageFromResource(error.getMessage()));
					}
					else {
						messages.add(getSpringLocaleDelegate().getMessageFromResource(error.getMessage()));
					}
				}
			}

			if (messages.size() == 1) {
				messages.add(getSpringLocaleDelegate().getText(cve.getMessage()));
			}

			String message = StringUtils.concatHtml(messages.toArray(new String[messages.size()]));
			setPointerInformation(new PointerCollection(message), null);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected void handleCollectableValidationException(CollectableValidationException ex, String sMessage1) {
		PointerCollection pc = null;
		final String exceptionMessage = Errors.getReasonableMessage(ex);

		if (exceptionMessage != null) {
			pc = new PointerCollection(Errors.formatErrorForBubble(exceptionMessage));
		}

		final CollectableEntityField clctefInvalid = ex.getCollectableEntityField();
		if (clctefInvalid != null) {
			if (pc == null) pc = new PointerCollection(sMessage1);
			pc.addEmptyFieldPointer(clctefInvalid.getName());
		}

		if (pc != null) {
			setPointerInformation(pc, null);
			final Collection<CollectableComponent> collclctcomp = getDetailsPanel().getEditView().getCollectableComponentsFor(clctefInvalid.getName());
			if (!collclctcomp.isEmpty()) {
				final CollectableComponent clctcomp = collclctcomp.iterator().next();
					clctcomp.getControlComponent().requestFocusInWindow();
			}
		} else {
			super.handleCollectableValidationException(ex, sMessage1);
		}
	}

	protected void addGeneratorActions(boolean bEnableButtons, List<Component> components) {
		final List<GeneratorActionVO> lstActions = getGeneratorActions();
		if (lstActions.size() > 0) {
			final JComboBox cmbbxActions = new JComboBox(lstActions.toArray());
			cmbbxActions.setSelectedItem(null);
			cmbbxActions.setEnabled(bEnableButtons);
			//toolbarCustomActionsDetails.addSeparator();
			// workaround for cellrenderer
			int w = cmbbxActions.getPreferredSize().width;
			if(w < 100)
				w = 100;
			cmbbxActions.setPreferredSize(new Dimension(w, cmbbxActions.getPreferredSize().height));
			UIUtils.setMaximumSizeToPreferredSize(cmbbxActions);

			components.add(cmbbxActions);
			if (cmbbxActions.isEnabled())
				cmbbxActions.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						UIUtils.runCommandLater(getTab(), new CommonRunnable() {
							@Override
							public void run() {
								if (cmbbxActions.getSelectedItem() instanceof GeneratorActionVO)
									try {
										cmdGenerateObject((GeneratorActionVO) cmbbxActions.getSelectedItem());
									}
								finally {
									cmbbxActions.setSelectedItem(null);
								}
							}
						});
					}
				});
			final ListCellRenderer originalRenderer = cmbbxActions.getRenderer();
			cmbbxActions.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList jlst, Object oValue, int iIndex, boolean bSelected,
					boolean bHasFocus) {
					final JLabel result = (JLabel) originalRenderer.getListCellRendererComponent(jlst, oValue, iIndex, bSelected,
						bHasFocus);
					result.setOpaque(false);

					if (oValue == null) {
						result.setText(getSpringLocaleDelegate().getMessage("GenericObjectCollectController.9","Arbeitsschritt"));
						result.setToolTipText(getSpringLocaleDelegate().getMessage(
								"GenericObjectCollectController.17","Bitte w\u00e4hlen Sie einen Arbeitsschritt aus."));
					}
					else {
						assert oValue instanceof GeneratorActionVO;
						final GeneratorActionVO actvo = (GeneratorActionVO) oValue;
						final String sSourceModuleName = GenerationController.getModuleLabel(actvo.getSourceModuleId());
						final String sTargetModuleName = GenerationController.getModuleLabel(actvo.getTargetModuleId());
						result.setToolTipText(getSpringLocaleDelegate().getMessage(
								"GenericObjectCollectController.46","Erzeugt einen {0} aus dem/der aktuellen {1}", sTargetModuleName, sSourceModuleName));
					}
					return result;
				}
			});
		}
	}

	public abstract void cmdGenerateObject(GeneratorActionVO generatoractionvo);

	public abstract List<GeneratorActionVO> getGeneratorActions();
	
	public abstract List<GeneratorActionVO> getGeneratorActions(Collection<Clct> selectedCollectablesFromResult);

	protected void setupResultContextMenuGeneration() {
		this.getResultPanel().popupmenuRow.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				UIUtils.runCommand(getTab(), new Runnable() {
					
					@Override
					public void run() {
						try {
							final List<GeneratorActionVO> lstActions = getGeneratorActions();
							JMenu mi = getResultPanel().miGenerations;
							mi.setVisible(lstActions.size() != 0);
							for(final GeneratorActionVO actionVO : lstActions) {
								JMenuItem action = new JMenuItem(new AbstractAction(actionVO.toString()) {
		
									@Override
									public void actionPerformed(ActionEvent e) {
										cmdGenerateObject(actionVO);
									}
		
								});
								mi.add(action);
							}
						}
						catch (Exception e1) {
							getResultPanel().miGenerations.setVisible(false);
							LOG.warn("popupMenuWillBecomeVisible failed: " + e1 + ", setting it invisible");
						}
					}
				});
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				clearGeneratorMenu();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				clearGeneratorMenu();
			}

			private void clearGeneratorMenu() {
				 getResultPanel().miGenerations.removeAll();
			}
		});
	}
	
	private class ResultActionsWorker extends SwingWorker<List<ResultActionCollection>, Object> {

		final Collection<Clct> selectedCollectablesFromResult;
		
		boolean cancelWorker = false;
		
		public ResultActionsWorker(Collection<Clct> selectedCollectablesFromResult) {
			try {
				runningResultActionsWorker.cancelWorker();
			} catch (Exception ex) {
				//ipgnore
			}
			runningResultActionsWorker = this;
			this.selectedCollectablesFromResult = selectedCollectablesFromResult;
		}
		
		@Override
		protected List<ResultActionCollection> doInBackground() throws Exception {
			try {
				// looks better ;-)
				Thread.sleep(500);
				return getResultActionsMultiThreaded(selectedCollectablesFromResult);
			}
			catch (Exception ex) {
				LOG.error("ResultActionsWorker failed: " + ex, ex);
			}
			return null;
 		}
		
		public void cancelWorker() {
			cancelWorker = true;
			cancel(true);
		}
		
		@Override
		protected void done() {
			try {
				List<ResultActionCollection> result = get();
				if (!cancelWorker) {
					runningResultActionsWorker = null;
					getResultPanel().setResultActions(result);
				}
			} catch (Exception e) {
				if (!cancelWorker) {
					if (getResultPanel() != null)
						getResultPanel().setResultActions(null);
				}
			} 
		}	
		
	}
}

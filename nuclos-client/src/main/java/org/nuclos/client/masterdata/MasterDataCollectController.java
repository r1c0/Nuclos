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

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.LafParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.MultiUpdateOfDependants;
import org.nuclos.client.common.NuclosCollectableListOfValues;
import org.nuclos.client.common.NuclosFocusTraversalPolicy;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.SearchConditionSubFormController;
import org.nuclos.client.common.SubFormController;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.ExplorerDelegate;
import org.nuclos.client.genericobject.GenerationController;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.i18n.ui.TranslationsController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.masterdata.datatransfer.MasterDataVOTransferable;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.scripting.context.CollectControllerScriptContext;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilter;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.LayoutComponentUtils;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.EditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.ParameterChangeListener;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DetailsEditModel;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.result.NuclosResultController;
import org.nuclos.client.ui.collect.result.NuclosSearchResultStrategy;
import org.nuclos.client.ui.collect.result.ResultController;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.client.ui.collect.search.ISearchStrategy;
import org.nuclos.client.ui.collect.search.SearchPanel;
import org.nuclos.client.ui.layoutml.LayoutMLEditView;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.LafParameter;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.XStreamSupport;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.navigation.treenode.GroupSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Controller for collecting master data. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataCollectController extends EntityCollectController<CollectableMasterDataWithDependants> {

	private static final Logger LOG = Logger.getLogger(MasterDataCollectController.class);

	private static int iFilter;

	private static final String PREFS_KEY_FILTERNAME = "filterName";

	public static final String FIELDNAME_ACTIVE = "active";
	public static final String FIELDNAME_VALIDFROM = "validFrom";
	public static final String FIELDNAME_VALIDUNTIL = "validUntil";

	protected final JMenuItem btnShowResultInExplorer = new JMenuItem();
	protected final JMenuItem btnMakeTreeRoot = new JMenuItem();
	protected final JButton btnPrintResults = new JButton();
	private final JMenuItem btnExecuteRule = new JMenuItem();
	private final boolean detailsWithScrollbar;

	protected final MasterDataDelegate mddelegate = MasterDataDelegate.getInstance();

	private Map<String, DetailsSubFormController<CollectableEntityObject>> mpsubformctlDetails;

	private MultiUpdateOfDependants multiupdateofdependants;

	private TranslationsController ctlTranslations;

	/**
	 * the global search filter (if any) used for the recent search
	 */
	// private GlobalSearchFilter globalsearchfilterForRecentSearch;

	private final List<Component> toolbarCustomActionsDetails = new ArrayList<Component>();
	private int toolbarCustomActionsDetailsIndex = -1;

	/**
	 * Use <code>newMasterDataCollectController()</code> to create an instance
	 * of this class.
	 *
	 * @param parent
	 * @param sEntityName
	 *
	 *            You should use
	 *            {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 *            to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public MasterDataCollectController(NuclosEntity systemEntity, MainFrameTab tabIfAny, String customUsage) {
		this(systemEntity.getEntityName(), tabIfAny, customUsage);
	}

	/**
	 * You should use
	 * {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} to
	 * get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected MasterDataCollectController(NuclosEntity systemEntity, MainFrameTab tabIfAny, boolean detailsWithScrollbar, String customUsage) {
		this(systemEntity.getEntityName(), tabIfAny, detailsWithScrollbar, customUsage);
	}

	/**
	 * You should use
	 * {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} to
	 * get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public MasterDataCollectController(String sEntityName, MainFrameTab tabIfAny, String customUsage) {
		this(sEntityName, tabIfAny, true, customUsage);
	}

	/**
	 * You should use
	 * {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} to
	 * get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected MasterDataCollectController(String sEntityName, MainFrameTab tabIfAny, boolean detailsWithScrollbar, String customUsage) {
		this(sEntityName, tabIfAny, detailsWithScrollbar, new NuclosResultController<CollectableMasterDataWithDependants>(sEntityName, new NuclosSearchResultStrategy<CollectableMasterDataWithDependants>()), customUsage);
	}

	/**
	 * You should use
	 * {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} to
	 * get an instance.
	 *
	 * Use <code>newMasterDataCollectController()</code> to create an instance
	 * of this class.
	 *
	 * @param parent
	 * @param sEntityName
	 */
	protected MasterDataCollectController(NuclosEntity systemEntity, MainFrameTab tabIfAny, ResultController<CollectableMasterDataWithDependants> rc, String customUsage) {
		this(systemEntity.getEntityName(), tabIfAny, true, rc, customUsage);
	}

	/**
	 * You should use
	 * {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} to
	 * get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected MasterDataCollectController(String sEntityName, MainFrameTab tabIfAny, boolean detailsWithScrollbar, ResultController<CollectableMasterDataWithDependants> rc, String customUsage) {
		super(sEntityName, tabIfAny, rc, customUsage);
		// getSearchStrategy().setCompleteCollectablesStrategy(new
		// CompleteCollectableMasterDataStrategy(this));
		final boolean bSearchPanelAvailable = this.mddelegate.getMetaData(sEntityName).isSearchable();
		this.detailsWithScrollbar = detailsWithScrollbar;
		final Long entityId = MetaDataClientProvider.getInstance().getEntity(sEntityName).getId();
		final CollectPanel<CollectableMasterDataWithDependants> pnlCollect = new MasterDataCollectPanel(entityId, bSearchPanelAvailable, LafParameterProvider.getInstance().getValue(LafParameter.nuclos_LAF_Details_Overlay, entityId));
		getTab().setLayeredComponent(pnlCollect);
		this.initialize(pnlCollect);
		this.setupEditPanelForDetailsTab();
		this.setupShortcutsForTabs(getTab());

		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void resultModeEntered(CollectStateEvent ev) {
				if (ev.getOldCollectState().getOuterState() != CollectState.OUTERSTATE_RESULT) {
					setupChangeListenerForResultTableVerticalScrollBar();
				}
			}

			@Override
			public void resultModeLeft(CollectStateEvent ev) {
				if (ev.getNewCollectState().getOuterState() != CollectState.OUTERSTATE_RESULT) {
					removePreviousChangeListenersForResultTableVerticalScrollBar();
				}
			}
		});
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeLeft(CollectStateEvent ev)
					throws CommonBusinessException {
				getLayoutMLButtonsActionListener().clearInputMapForParentPanel(getCollectPanel());
				getLayoutMLButtonsActionListener().clearInputMapForParentPanel(getDetailsPanel().getLayoutRoot().getRootComponent());
				
				setupShortcutsForTabs(getTab());
			}
			@Override
			public void detailsModeEntered(CollectStateEvent ev) {
				int iDetailsMode = ev.getNewCollectState().getInnerState();
				final boolean bViewingExistingRecord = (iDetailsMode == CollectState.DETAILSMODE_VIEW);
				UIUtils.invokeOnDispatchThread(new Runnable() {
					@Override
					public void run() {
						btnMakeTreeRoot.setEnabled(bViewingExistingRecord);
						btnExecuteRule.setEnabled(bViewingExistingRecord);
						btnExecuteRule.setVisible(mddelegate.getUsesRuleEngine(getEntityName()));
					}
				});

				final Collection<SubForm> collsubform = new HashSet<SubForm>();
				for (SubFormController subformctl : getSubFormControllersInDetails()) {
					collsubform.add(subformctl.getSubForm());
				}
				// current state, subsequent states and custom actions:

				switch (iDetailsMode) {
				case CollectState.DETAILSMODE_VIEW:
					getLayoutMLButtonsActionListener().fireComponentEnabledStateUpdate(false);
					break;
				case CollectState.DETAILSMODE_EDIT:
					getLayoutMLButtonsActionListener().fireComponentEnabledStateUpdate(true);
					break;
				case CollectState.DETAILSMODE_NEW:
				case CollectState.DETAILSMODE_NEW_CHANGED:
				case CollectState.DETAILSMODE_NEW_SEARCHVALUE:
				case CollectState.DETAILSMODE_MULTIVIEW:
				case CollectState.DETAILSMODE_MULTIEDIT:
					getLayoutMLButtonsActionListener().setComponentsEnabled(false);
					break;
				default:
				} // switch

				respectRights(getDetailsPanel().getEditView().getCollectableComponents(), collsubform, ev.getNewCollectState());

				getLayoutMLButtonsActionListener().clearInputMapForParentPanel(getCollectPanel());
				getLayoutMLButtonsActionListener().clearInputMapForParentPanel(getDetailsPanel().getLayoutRoot().getRootComponent());
				
				getLayoutMLButtonsActionListener().setInputMapForParentPanel(getCollectPanel(), getDetailsPanel().getLayoutRoot().getRootComponent());
				
				showCustomActions(iDetailsMode);
			}
		});
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				if (ev.hasOuterStateChanged()) {
					setInitialComponentFocusInDetailsTab();
				}
			}

			@Override
			public void searchModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				setInitialComponentFocusInSearchTab();

				for (CollectableComponent clctcomp : getSearchPanel().getEditView().getCollectableComponents()) {
					clctcomp.setEnabled(true);
				}
			}
		});
	}

	/**
	 * TODO: Make this protected.
	 */
	public void init() {
		super.init();

		if (this.isSearchPanelAvailable()) {
			initSearchSubforms();
			this.setupSearchToolBar();
		}
		this.setupResultToolBar();
		this.setupDetailsToolBar();
		refreshFastFilter();

		initSubFormsLoader();
		setupDataTransfer();
		getDetailsPanel().getLayoutRoot().getRootComponent().setFocusCycleRoot(true);
		getDetailsPanel().getLayoutRoot().getRootComponent().setFocusTraversalPolicyProvider(true);
		getDetailsPanel().getLayoutRoot().getRootComponent().setFocusTraversalPolicy(new NuclosFocusTraversalPolicy(getDetailsPanel().getLayoutRoot()));

		setupResultContextMenuGeneration();

		if (NuclosEntity.getByName(getEntityName()) != null) {
			ctlTranslations = new TranslationsController(this);
			getCollectStateModel().addCollectStateListener(ctlTranslations);
		}
	}

	@Override
	protected void setupSearchToolBar() {
		// final JToolBar result = newCustomSearchToolBar();
		super.setupSearchToolBar();
		refreshFilterView();
		// result.add(Box.createGlue());

		// this.getSearchPanel().setCustomToolBarArea(result);
	}
	
	/**
	 * This method is called by <code>cmdClearSearchFields</code>, that is when the user clicks
	 * the "Clear Search Fields" button. This implementation selects the default search filter.
	 */
	@Override
	protected void clearSearchCondition() {
		super.clearSearchCondition();
		// select the default filter (the first entry):
		selectDefaultFilter();
	}

	@Override
	protected String getTitle(int iTab, int iMode) {
		final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
		final String[] asTabs = { localeDelegate.getMessage("MasterDataCollectController.17", "Suche"), localeDelegate.getMessage("MasterDataCollectController.5", "Ergebnis"),
				localeDelegate.getMessage("MasterDataCollectController.2", "Details") };
		final String[] asDetailsMode = { localeDelegate.getMessage("MasterDataCollectController.18", "Undefiniert"), localeDelegate.getMessage("MasterDataCollectController.3", "Details"),
				localeDelegate.getMessage("MasterDataCollectController.1", "Bearbeiten"), localeDelegate.getMessage("MasterDataCollectController.10", "Neueingabe"),
				localeDelegate.getMessage("MasterDataCollectController.12", "Neueingabe (Ge\u00e4ndert)"), localeDelegate.getMessage("MasterDataCollectController.15", "Sammelbearbeitung"),
				localeDelegate.getMessage("MasterDataCollectController.16", "Sammelbearbeitung (Ge\u00e4ndert)"), localeDelegate.getMessage("MasterDataCollectController.11", "Neueingabe (\u00dcbernahme Suchwerte)") };

		String sPrefix;
		String sSuffix = "";
		final String sMode;

		switch (iTab) {
		case CollectState.OUTERSTATE_DETAILS:
			sPrefix = ""; // this.getEntityLabel();
			sMode = asDetailsMode[iMode];
			if (CollectState.isDetailsModeViewOrEdit(iMode)) {
				MasterDataMetaVO metaDataVO = MasterDataDelegate.getInstance().getMetaData(this.getEntityName());
				String sIdentifier = localeDelegate.getTreeViewLabel(this.getSelectedCollectable(), getEntity(), MetaDataClientProvider.getInstance());
				if (sIdentifier == null) {
					sIdentifier = this.getSelectedCollectable() != null ? this.getSelectedCollectable().getIdentifierLabel() : "<>";
				}
				sPrefix += sIdentifier;
			}
			else if (CollectState.isDetailsModeMultiViewOrEdit(iMode)) {
				sSuffix = localeDelegate.getMessage("MasterDataCollectController.19", " von {0} Objekten", this.getSelectedCollectables().size());
			}
			break;
		default:
			sPrefix = this.getEntityLabel();
			sMode = asTabs[iTab];
		}

		return sPrefix + (sPrefix.length() > 0 ? " - " : "") + sMode + sSuffix;
	}

	@Override
	protected String getLabelForStartTab() {
		String result = null;

		boolean buildTreeView = false;
		switch (getCollectState().getOuterState()) {
		case CollectState.OUTERSTATE_DETAILS:
			buildTreeView = this.getCollectState().isDetailsModeViewOrEdit();
			break;
		case CollectState.OUTERSTATE_RESULT:
			buildTreeView = this.getSelectedCollectables().size() == 1;
			break;
		}

		if (buildTreeView) {
			result = getSpringLocaleDelegate().getTreeViewLabel(this.getSelectedCollectable(), getEntity(), MetaDataClientProvider.getInstance());
		}

		if (result == null) {
			return super.getLabelForStartTab();
		}
		else {
			return result.trim();
		}
	}

	/**
	 * adjusts the visibility and "enability" ;) of the fields according to the
	 * user rights.
	 *
	 * @param collclctcomp
	 * @param collectstate
	 */
	protected void respectRights(Collection<CollectableComponent> collclctcomp, Collection<SubForm> collsubform, CollectState collectstate) {

		if (!SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity) || !getCollectableEntity().getMasterDataMetaCVO().isEditable()) {
			for (CollectableComponent clctcomp : collclctcomp) {
				clctcomp.setReadOnly(true);
			} // for
		}

		// adjust subforms:
		for (SubForm subform : collsubform) {
			// In the Search panel, subforms are always enabled:
			// @todo
			// subform.setEnabled(collectstate.isSearchMode() ||
			// (subform.isEnabled() && this.isCurrentRecordWritable()));

			// a subform may be explicitly disabled in the LayoutML definition.
			boolean editable = (collectstate.isSearchMode() || (SecurityCache.getInstance().isWriteAllowedForMasterData(sEntity) && MasterDataDelegate.getInstance().getMetaData(subform.getEntityName()).isEditable() && getCollectableEntity()
					.getMasterDataMetaCVO().isEditable()));
			subform.setReadOnly(!editable);
		}
	}

	protected void setupResultToolBar() {
		// additional functionality in Result panel:
		// final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		this.getDetailsPanel().btnDelete.setEnabled(SecurityCache.getInstance().isDeleteAllowedForMasterData(sEntity));

		btnShowResultInExplorer.setIcon(Icons.getInstance().getIconTree16());
		btnShowResultInExplorer.setText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.6", "Ergebnis in Explorer anzeigen"));
		btnShowResultInExplorer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdShowResultInExplorer();
			}
		});

		// toolbar.add(btnShowResultInExplorer);
		// toolbar.add(btnPrintResults);
		// toolbar.add(Box.createHorizontalGlue());
		this.getResultPanel().addPopupExtraSeparator();
		this.getResultPanel().addPopupExtraMenuItem(btnShowResultInExplorer);
		this.getResultPanel().addToolBarComponent(btnPrintResults);

		this.btnPrintResults.setIcon(Icons.getInstance().getIconPrintReport16());

		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_PRINT_SEARCHRESULT)) {
			this.btnPrintResults.setEnabled(true);
			this.btnPrintResults.setToolTipText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.7", "Ergebnisliste drucken"));
			// action: Print results
			this.btnPrintResults.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdPrint();
				}
			});
		}
		else {
			this.btnPrintResults.setEnabled(false);
			this.btnPrintResults.setToolTipText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.8", "Ergebnisliste drucken - Sie verf\u00fcgen nicht \u00fcber ausreichende Rechte."));
		}

		// this.getResultPanel().setCustomToolBarArea(toolbar);
	}

	private void setupDetailsToolBar() {
		// additional functionality in Details panel:
		// final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		btnMakeTreeRoot.setIcon(Icons.getInstance().getIconMakeTreeRoot16());
		btnMakeTreeRoot.setText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.9", "In Explorer anzeigen"));
		btnMakeTreeRoot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdJumpToTree();
			}
		});
		// toolbar.add(btnMakeTreeRoot);
		this.getDetailsPanel().addPopupExtraMenuItem(btnMakeTreeRoot);

		// Execute rule by user only for authorized personnel
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_EXECUTE_RULE_BY_USER)) {
			this.btnExecuteRule.setName("btnExecuteRule");
			this.btnExecuteRule.setText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.14", "Regeln ausf\u00fchren"));
			this.btnExecuteRule.setIcon(Icons.getInstance().getIconExecuteRule16());
			this.btnExecuteRule.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdExecuteRuleByUser(MasterDataCollectController.this.getTab(), MasterDataCollectController.this.getEntityName(), MasterDataCollectController.this.getSelectedCollectable());
				}
			});
			// toolbar.add(this.btnExecuteRule);
			this.getDetailsPanel().addPopupExtraMenuItem(btnExecuteRule);
			this.btnExecuteRule.setEnabled(SecurityCache.getInstance().isActionAllowed(Actions.ACTION_EXECUTE_RULE_BY_USER));
		}

		toolbarCustomActionsDetailsIndex = this.getDetailsPanel().getToolBarNextIndex();
		// this.getDetailsPanel().setCustomToolBarArea(toolbar);
		// toolbar.add(Box.createHorizontalGlue());

		// toolbar.add(loadingLabel);
		// toolbar.add(btnPointer);
		this.getDetailsPanel().addToolBarComponent(btnPointer);
	}

	private void showCustomActions(int iDetailsMode) {
		final boolean bSingle = CollectState.isDetailsModeViewOrEdit(iDetailsMode);
		final boolean bMulti = CollectState.isDetailsModeMultiViewOrEdit(iDetailsMode);
		final boolean bViewOrEdit = bSingle || bMulti;
		final boolean bView = bViewOrEdit && !CollectState.isDetailsModeChangesPending(iDetailsMode);

		this.getDetailsPanel().removeToolBarComponents(toolbarCustomActionsDetails);
		toolbarCustomActionsDetails.clear();
		if (toolbarCustomActionsDetailsIndex == -1) {
			return;
		}

		if (bViewOrEdit) {
			addGeneratorActions(bView, toolbarCustomActionsDetails);
		}

		this.getDetailsPanel().addToolBarComponents(toolbarCustomActionsDetails, toolbarCustomActionsDetailsIndex);
	}

	private static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		String searchFilterName;
	}

	private static String toXML(RestorePreferences rp) {
		final XStreamSupport xs = XStreamSupport.getInstance();
		final XStream xstream = xs.getXStream();
		try {
			return xstream.toXML(rp);
		}
		finally {
			xs.returnXStream(xstream);
		}
	}

	private static RestorePreferences fromXML(String xml) {
		final XStreamSupport xs = XStreamSupport.getInstance();
		final XStream xstream = xs.getXStream();
		try {
			return (RestorePreferences) xstream.fromXML(xml);
		}
		finally {
			xs.returnXStream(xstream);
		}
	}

	@Override
	protected void storeInstanceStateToPreferences(Map<String, String> inheritControllerPreferences) {
		RestorePreferences rp = new RestorePreferences();

		SearchFilter filter = getSelectedSearchFilter();
		rp.searchFilterName = (filter == null || filter.isDefaultFilter()) ? null : filter.getName();

		inheritControllerPreferences.put(MasterDataCollectController.class.getName(), toXML(rp));
		super.storeInstanceStateToPreferences(inheritControllerPreferences);
	}
	
	@Override
	protected void restoreInstanceStateFromPreferences(Map<String, String> inheritControllerPreferences) {
		String prefXml = inheritControllerPreferences.get(MasterDataCollectController.class.getName());
		if (prefXml != null) {
			RestorePreferences rp = fromXML(prefXml);
	
			// Restore the settings for the chosen search filter in this module window (may override the global settings)
			if (rp.searchFilterName == null)
				selectDefaultFilter();
			else
				// find filter by name:
				for (int i = 1; i < getSearchFilterComboBox().getItemCount(); ++i)
					if (((SearchFilter) getSearchFilterComboBox().getItemAt(i)).getName().equals(rp.searchFilterName)) {
						getSearchFilterComboBox().setSelectedIndex(i);
						break;
					}
		}
		super.restoreInstanceStateFromPreferences(inheritControllerPreferences);
	}

	/**
	 * @param prefs
	 * @throws CommonBusinessException
	 * @precondition this.isSearchPanelVisible()
	 */
	@Override
	@Deprecated
	protected void restoreSearchCriteriaFromPreferences(Preferences prefs) throws CommonBusinessException {
		if (!isSearchPanelAvailable())
			throw new IllegalStateException("!isSearchPanelVisible()");

		// Restore the settings for the chosen search filter in this module window (may override the global settings)
		restoreSelectedSearchFilterFromPreferences(prefs);

		super.restoreSearchCriteriaFromPreferences(prefs);
	}

	@Deprecated
	private void restoreSelectedSearchFilterFromPreferences(Preferences prefs) {
		// restore search filter:
		final String sFilterName = prefs.get(PREFS_KEY_FILTERNAME, null);
		if (sFilterName == null)
			selectDefaultFilter();
		else
			// find filter by name:
			for (int i = 1; i < getSearchFilterComboBox().getItemCount(); ++i)
				if (((SearchFilter) getSearchFilterComboBox().getItemAt(i)).getName().equals(sFilterName)) {
					getSearchFilterComboBox().setSelectedIndex(i);
					break;
				}
	}

	@Override
	public void executeBusinessRules(final List<RuleVO> lstRuleVO, final boolean bSaveAfterRuleExecution) throws CommonBusinessException {
		invoke(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				try {
					setDetailsChangedIgnored(true);

					// get the edited state
					CollectableMasterDataWithDependants clct = getCollectStateModel().getEditedCollectable();
					if (clct == null) {
						// .. or the selected state if the details have not been
						// edited.
						clct = getSelectedCollectable();
					}
					readValuesFromEditPanel(clct, false);
					prepareCollectableForSaving(clct, getCollectableEntityForDetails());
					final MasterDataWithDependantsVO md = new MasterDataWithDependantsVO(clct.getMasterDataCVO(), getAllSubFormData(clct.getId()).toDependantMasterDataMap());
					mddelegate.executeBusinessRules(getEntityName(), lstRuleVO, md, bSaveAfterRuleExecution, getCustomUsage());

					broadcastCollectableEvent(clct, MessageType.EDIT_DONE);
				}
				finally {
					setDetailsChangedIgnored(false);
				}
			}
		});
	}

	@Override
	protected EntitySearchFilter getCurrentSearchFilterFromSearchPanel() throws CommonBusinessException {
		final EntitySearchFilter result = (EntitySearchFilter) super.getCurrentSearchFilterFromSearchPanel();

		result.setVisibleColumns(this.getSelectedFields());
		return result;
	}

	protected void cmdJumpToTree() {
		UIUtils.runCommand(this.getTab(), new CommonRunnable() {
			@Override
			public void run() throws CommonFinderException, CommonPermissionException {
				final Integer iId = (Integer) getSelectedCollectableId();
				final String sEntity = getSelectedCollectable().getCollectableEntity().getName();
				if (iId != null) {
					TreeNode treenode = null;
					// @todo create a MasterdataTreeNodeFactory and get the node
					// from the factory
					if (NuclosEntity.GROUP.checkEntityName(sEntity)) {
						treenode = ExplorerDelegate.getInstance().getGroupTreeNode(iId);
					}
					else if (NuclosEntity.NUCLET.checkEntityName(sEntity)) {
						treenode = ExplorerDelegate.getInstance().getNucletTreeNode(iId);
					}
					else {
						treenode = ExplorerDelegate.getInstance().getMasterDataTreeNode(iId, sEntity);
					}
					getExplorerController().showInOwnTab(treenode);
				}
			}
		});
	}

	private ExplorerController getExplorerController() {
		return getMainController().getExplorerController();
	}

	protected void cmdShowResultInExplorer() {
		UIUtils.runCommand(this.getTab(), new CommonRunnable() {
			@Override
			public void run() throws CollectableFieldFormatException {
				final String sFilterName = getCollectableEntity().getLabel() + Integer.toString(++iFilter);
				final String sEntity = getCollectableEntity().getName();
				final ISearchStrategy<CollectableMasterDataWithDependants> ss = getSearchStrategy();
				// final String sFilterName = "Gruppe " +
				// Integer.toString(++iFilter);
				// @todo create a MasterdataTreeNodeFactory and get the node
				// from the factory
				if (NuclosEntity.GROUP.checkEntityName(sEntity)) {
					getExplorerController().showInOwnTab(new GroupSearchResultTreeNode(ss.getCollectableSearchCondition(), sFilterName));
				}
				else {
					getExplorerController().showInOwnTab(new MasterDataSearchResultTreeNode(sEntity, ss.getCollectableSearchCondition(), sFilterName));
				}
			}
		});
	}

	@Override
	public void close() {
		super.close();
		this.closeSubFormControllers();
		if (mpsubformctlDetails != null) {
			mpsubformctlDetails.clear();
		}
		mpsubformctlDetails = null;
	}

	/**
	 * @return the collectable (master data) entity for this controller.
	 *
	 *         TODO: Make this protected again.
	 */
	@Override
	public CollectableMasterDataEntity getCollectableEntity() {
		return (CollectableMasterDataEntity) super.getCollectableEntity();
	}

	/**
	 * @deprecated Move to SearchController and make protected again.
	 */
	@Override
	public CollectableFieldsProviderFactory getCollectableFieldsProviderFactoryForSearchEditor() {
		return valueListProviderCache.makeCachingFieldsProviderFactory(new MasterDataCollectableFieldsProviderFactory(this.getCollectableEntity().getName()));
	}

	/**
	 * runs this controller, starting with the search panel or with the result
	 * panel, depending on the parameter.
	 *
	 * @param bStartWithSearchPanel
	 */
	@Override
	protected void run(boolean bStartWithSearchPanel) throws CommonBusinessException {
		if (bStartWithSearchPanel) {
			final boolean bCanBeFiltered = isFilteringAppropriate();
			this.getSearchPanel().chkbxHideInvalid.setEnabled(bCanBeFiltered);
			this.getSearchPanel().chkbxHideInvalid.setSelected(bCanBeFiltered);
			filterByValidity(bCanBeFiltered);

			this.runSearch();
		}
		else {
			this.runViewAll();
		}
	}

	@Deprecated
	protected void setupEditPanelForDetailsTab() {
		closeSubFormControllers();

		// create a controller for each subform:
		Map<String, SubForm> mpSubForm = this.getDetailsPanel().getLayoutRoot().getMapOfSubForms();
		mpsubformctlDetails = newDetailsSubFormControllers(mpSubForm);
      
		Map<SubForm, MasterDataSubFormController> mpSubFormController = new HashMap<SubForm, MasterDataSubFormController>();

		// create a map of subforms and their controllers that exists in the
		// layout
      final ParameterChangeListener changeListener = new ParameterChangeListener() {
		@Override
		public void stateChanged(final ChangeEvent e) {
			if (e != null && e.getSource() instanceof SubForm) {
			   	UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							SubForm subform = (SubForm)e.getSource();
							MasterDataCollectController.this.getSubFormsLoader().startLoading(subform.getEntityName());
						}
					});
			}
		}
  	  };
      for (String sSubFormEntityName : mpsubformctlDetails.keySet()) {
         DetailsSubFormController<CollectableEntityObject> subformcontroller = mpsubformctlDetails.get(sSubFormEntityName);
         SubForm subform = subformcontroller.getSubForm();
         if (subformcontroller instanceof MasterDataSubFormController) {
        	subform.addParameterListener(changeListener);
            subformcontroller.setCollectController(MasterDataCollectController.this);
            mpSubFormController.put(subform, (MasterDataSubFormController)subformcontroller);
         }
      }

		// add child subforms to their parents
		for (SubForm subform : mpSubFormController.keySet()) {
			SubForm parentsubform = mpSubForm.get(subform.getParentSubForm());
			if (parentsubform != null) {
				MasterDataSubFormController subformcontroller = mpSubFormController.get(parentsubform);
				subformcontroller.addChildSubFormController(mpSubFormController.get(subform));
			}
		}
	}

	@Override
	public EditView newSearchEditView(LayoutRoot layoutroot) {
		return DefaultEditView.newSearchEditView(layoutroot.getRootComponent(), layoutroot, layoutroot.getInitialFocusEntityAndFieldName());
	}

	private LayoutRoot newLayoutRoot(boolean bSearch) {
		final LayoutMLParser parser = new LayoutMLParser();

		final CollectableMasterDataEntity clcte = this.getCollectableEntity();
		final String sEntityName = clcte.getName();
		LayoutRoot result;

		try {
			final Reader reader = MasterDataLayoutHelper.getLayoutMLReader(sEntityName, bSearch, getCustomUsage());

			final InputSource isrc = new InputSource(new BufferedReader(reader));

			try {
				result = parser.getResult(isrc, clcte, bSearch, getLayoutMLButtonsActionListener(), MasterDataCollectableFieldsProviderFactory.newFactory(sEntityName, valueListProviderCache), CollectableComponentFactory.getInstance());

				if (bSearch) {
					for (CollectableComponent comp : result.getCollectableComponents()) {
						comp.getControlComponent().addFocusListener(collectableComponentSearchFocusListener);
					}
				}

				// Clear default keymaps of all JSplitPanes for consistent
				// hotkey handling:
				UIUtils.clearJComponentKeymap(result.getRootComponent(), JSplitPane.class);

				result.getRootComponent().setFocusCycleRoot(true);
			}
			catch (LayoutMLException ex) {
				throw new NuclosFatalException(ex);
			}
			catch (IOException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		catch (NuclosBusinessException ex) {
			Errors.getInstance().showExceptionDialog(getTab(), ex);
			result = LayoutRoot.newEmptyLayoutRoot(true);
		}

		return result;
	}

	@Override
	protected void selectTab() {
		super.selectTab();

		setInitialComponentFocusInSearchTab();
	}

	private void setInitialComponentFocusInSearchTab() {
		if (this.isSearchPanelAvailable()) {
			Utils.setInitialComponentFocus(this.getSearchEditView(), mpsubformctlDetails);
		}
	}

	private void setInitialComponentFocusInDetailsTab() {
		Utils.setInitialComponentFocus(this.getDetailsEditView(), mpsubformctlDetails);
	}

	/**
	 * closes all subform controllers.
	 */
	private void closeSubFormControllers() {
		for (DetailsSubFormController<?> subformctl : this.getSubFormControllersInDetails()) {
			subformctl.close();
		}
	}

	@Override
	public MasterDataSearchPanel getSearchPanel() {
		return (MasterDataSearchPanel) super.getSearchPanel();
	}

	@Override
	public NuclosResultPanel<CollectableMasterDataWithDependants> getResultPanel() {
		return (NuclosResultPanel<CollectableMasterDataWithDependants>) super.getResultPanel();
	}

	@Override
	public MasterDataDetailsPanel getDetailsPanel() {
		return (MasterDataDetailsPanel) super.getDetailsPanel();
	}

	@Override
	protected void prepareCollectableForSaving(CollectableMasterDataWithDependants clctCurrent, org.nuclos.common.collect.collectable.CollectableEntity clcteCurrent) {
		super.prepareCollectableForSaving(clctCurrent, clcteCurrent);
	}

	/**
	 * creates subform controllers for all given subforms.
	 *
	 * @param mpSubForms
	 * @return Map<String, DetailsSubFormController>
	 */
	protected Map<String, DetailsSubFormController<CollectableEntityObject>> newDetailsSubFormControllers(Map<String, SubForm> mpSubForms) {
		final DetailsEditModel editmodelDetails = this.getDetailsPanel().getEditModel();
		Map<String, DetailsSubFormController<CollectableEntityObject>> result = CollectionUtils.transformMap(mpSubForms, new Transformer<SubForm, DetailsSubFormController<CollectableEntityObject>>() {
			@Override
			public DetailsSubFormController<CollectableEntityObject> transform(SubForm subform) {
				return newDetailsSubFormController(subform, getEntityName(), editmodelDetails);
			}
		});
		getDetailsConroller().setSubFormControllers(result.values());
		return result;
	}

	/**
	 * Create a subform controller for a given subform
	 *
	 * @param subform
	 * @param sParentEntityName
	 * @param clctcompmodelprovider
	 * @return the <code>DetailsSubFormController</code> belonging to the given
	 *         entity, if any.
	 * @postcondition result != null
	 */
	protected DetailsSubFormController<CollectableEntityObject> newDetailsSubFormController(SubForm subform, String sParentEntityName, CollectableComponentModelProvider clctcompmodelprovider) {

		// if parent of subform is another subform, change given parent entity
		// name
		String sParentSubForm = subform.getParentSubForm();
		if (sParentSubForm != null) {
			sParentEntityName = sParentSubForm;
		}

		return newDetailsSubFormController(subform, sParentEntityName, clctcompmodelprovider, this.getTab(), this.getDetailsPanel(), this.getPreferences(), this.getEntityPreferences());
	}

	/**
	 * @return the <code>DetailsSubFormController</code> belonging to the given
	 *         entity, if any.
	 */
	protected DetailsSubFormController<CollectableEntityObject> getSubFormController(String sEntityName) {
		return this.getMapOfSubFormControllersInDetails().get(sEntityName);
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		for (DetailsSubFormController<CollectableEntityObject> subformctl : this.getSubFormControllersInDetails()) {
			subformctl.getSubForm().addChangeListener(this.changelistenerDetailsChanged);
		}
	}

	/**
	 * @deprecated Move to DetailsController and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		for (DetailsSubFormController<CollectableEntityObject> subformctl : this.getSubFormControllersInDetails()) {
			subformctl.getSubForm().removeChangeListener(this.changelistenerDetailsChanged);
		}
	}

	@Override
	protected boolean stopEditingInDetails() {
		boolean result = super.stopEditingInDetails();
		if (result) {
			for (SubFormController subformctl : this.getSubFormControllersInDetails()) {
				result = result && subformctl.stopEditing();
			}
		}
		return result;
	}

	protected void updateLoadedSubFormData(CollectableMasterDataWithDependants clct, Collection<EntityObjectVO> collmdvo) {
		// override in subclasses
	}

	@Override
	protected void unsafeFillDetailsPanel(final CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		/** @todo use super.unsafeFillDetailsPanel */

		for (String sFieldName : this.getDetailsPanel().getLayoutRoot().getOrderedFieldNames()) {
			LOG.debug("sFieldName = " + sFieldName);

			// iterate over the models rather than over the components:
			final CollectableComponentModel clctcompmodel = this.getDetailsPanel().getLayoutRoot().getCollectableComponentModelFor(sFieldName);
			clctcompmodel.setFieldInitial(clct.getField(sFieldName));
		}

		// fill subforms:
		if (clct.getId() != null) {
			UIUtils.invokeOnDispatchThread(new Runnable() {
				@Override
				public void run() {
					MasterDataCollectController.this.getSubFormsLoader().startLoading();
				}
			});
		}
		for (final DetailsSubFormController<CollectableEntityObject> subformctl : this.getSubFormControllersInDetails()) {
			// by object generation
			DependantMasterDataMap dependants = clct.getDependantMasterDataMap();
			if (clct.getId() == null && dependants.getAllData().size() != 0) {
				final MasterDataSubFormController mdsubformctl = (MasterDataSubFormController) subformctl;
				for (String entity : dependants.getEntityNames())
					if (entity.equals(mdsubformctl.getCollectableEntity().getName())) {
						mdsubformctl.fillSubForm(null, dependants.getData(entity));
						mdsubformctl.getSubForm().setNewEnabled(new CollectControllerScriptContext(MasterDataCollectController.this, new ArrayList<DetailsSubFormController<?>>(getSubFormControllersInDetails())));
					}
			}
			else if (clct.getId() == null) {
				final MasterDataSubFormController mdsubformctl = (MasterDataSubFormController) subformctl;
				mdsubformctl.clear();
				mdsubformctl.fillSubForm(null, new ArrayList<EntityObjectVO>());
				mdsubformctl.getSubForm().setNewEnabled(new CollectControllerScriptContext(MasterDataCollectController.this, new ArrayList<DetailsSubFormController<?>>(getSubFormControllersInDetails())));
			}
			else {
				if (((MasterDataSubFormController) subformctl).isChildSubForm())
					continue;
				
				SubFormsInterruptableClientWorker sfClientWorker = new SubFormsInterruptableClientWorker() {
					Collection<EntityObjectVO> collmdvo;
					Integer iParentId;
					MasterDataSubFormController mdsubformctl = (MasterDataSubFormController) subformctl;

					@Override
					public void init() throws CommonBusinessException {
						if (!interrupted) {
							mdsubformctl.clear();
							mdsubformctl.fillSubForm(null, new ArrayList<EntityObjectVO>());
							mdsubformctl.getSubForm().setLockedLayer();
						}
					}
				    @Override
				    public void work() throws NuclosBusinessException {
					   if (interrupted || isClosed()) {
						   return;
					   }
					   iParentId = (Integer) clct.getId();
					   collmdvo = (clct.getId() == null) ?
							   new ArrayList<EntityObjectVO>() :
								   MasterDataDelegate.getInstance().getDependantMasterData(mdsubformctl.getCollectableEntity().getName(), mdsubformctl.getForeignKeyFieldName(), clct.getId(), mdsubformctl.getSubForm().getMapParams());
				    }
					@Override
					public void handleError(Exception ex) {
						if (!interrupted) {
							Errors.getInstance().showExceptionDialog(getResultsComponent(), ex);
						}
					}

					@Override
					public JComponent getResultsComponent() {
						return mdsubformctl.getSubForm();
					}

					@Override
					public void paint() throws CommonBusinessException {
						// if this worker is interrupted - it is a worker for an
						// "old" subform/main object.
						// The data schould not be published to sub form!
						// otherwise we will see a sub form data of another
						// object!
						if (!interrupted && !isClosed()) {
							synchronized (MasterDataCollectController.this) {
								final boolean bWasDetailsChangedIgnored = MasterDataCollectController.this.isDetailsChangedIgnored();
								MasterDataCollectController.this.setDetailsChangedIgnored(true);
								try {
									mdsubformctl.getSubForm().getJTable().setBackground(Color.WHITE);
									mdsubformctl.fillSubForm(iParentId, collmdvo);
									mdsubformctl.getSubForm().setNewEnabled(new CollectControllerScriptContext(MasterDataCollectController.this, new ArrayList<DetailsSubFormController<?>>(getSubFormControllersInDetails())));

									updateLoadedSubFormData(clct, collmdvo);
								}
								finally {
									if (!bWasDetailsChangedIgnored) {
										MasterDataCollectController.this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
									}
								}

								MasterDataCollectController.this.getSubFormsLoader().setSubFormLoaded(mdsubformctl.getCollectableEntity().getName(), true);
								mdsubformctl.getSubForm().forceUnlockFrame();
							}
						}
						else {
							return;
						}
					}
				};

				MasterDataCollectController.this.getSubFormsLoader().addSubFormClientWorker(subformctl.getCollectableEntity().getName(), sfClientWorker);

				getLayoutMLButtonsActionListener().fireComponentEnabledStateUpdate(false);
			}
		}
	}

	@Override
	protected void unsafeFillMultiEditDetailsPanel(Collection<CollectableMasterDataWithDependants> collclct) throws CommonBusinessException {
		// fill the Details panel with the common values:
		super.unsafeFillMultiEditDetailsPanel(collclct);

		// begin multi-update of dependants:
		this.multiupdateofdependants = new MultiUpdateOfDependants(this.getSubFormControllersInDetails(), collclct);

		getLayoutMLButtonsActionListener().setComponentsEnabled(false);
	}

	@Override
	public CollectableMasterDataWithDependants newCollectable() {
		final CollectableMasterDataEntity clctmde = this.getCollectableEntity();
		return CollectableMasterDataWithDependants.newInstance(clctmde, new MasterDataVO(clctmde.getMasterDataMetaCVO(), false));
	}

	/**
	 * Some master data entities may never be written to. Others require the
	 * right to write.
	 */
	@Override
	protected boolean isSaveAllowed() {
		return this.getCollectableEntity().getMasterDataMetaCVO().isEditable() && SecurityCache.getInstance().isWriteAllowedForMasterData(this.getEntityName()) && isNotLoadingSubForms();
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed() {
		// return this.getCollectableEntity().getMasterDataMetaCVO().isRemoved()
		// &&
		return SecurityCache.getInstance().isDeleteAllowedForMasterData(this.getEntityName()) && MasterDataDelegate.getInstance().getMetaData(getEntityName()).isEditable();
	}

	/**
	 * @return Is the "Read" action for the given Collectable allowed? May be
	 *         overridden by subclasses.
	 * @precondition clct != null
	 */
	@Override
	protected boolean isReadAllowed(CollectableMasterDataWithDependants clct) {
		return SecurityCache.getInstance().isReadAllowedForMasterData(getEntityName());
	}

	/**
	 * @return Is the "Read" action for the given set of Collectables allowed?
	 *         May be overridden by subclasses.
	 * @precondition clct != null
	 */
	@Override
	protected boolean isReadAllowed(List<CollectableMasterDataWithDependants> lsClct) {
		return lsClct.isEmpty() || SecurityCache.getInstance().isReadAllowedForMasterData(getEntityName());
	}

	/**
	 * @return true
	 */
	@Override
	protected boolean isMultiEditAllowed() {
		return true;
	}

	private boolean isFilteringAppropriate() {
		return this.hasActiveSign() || this.hasValidityDate();
	}

	public boolean hasValidityDate() {
		final Collection<String> collFieldNames = this.getCollectableEntity().getFieldNames();
		return (collFieldNames.contains(FIELDNAME_VALIDFROM) && collFieldNames.contains(FIELDNAME_VALIDUNTIL));
	}

	private boolean hasActiveSign() {
		return this.getCollectableEntity().getFieldNames().contains(FIELDNAME_ACTIVE);
	}

	public boolean isFilteringDesired() {
		return this.getSearchPanel().chkbxHideInvalid.isSelected() && isFilteringAppropriate();
	}

	@Override
	public void runLookupCollectable(ICollectableListOfValues clctlovSource) throws CommonBusinessException {
		final Boolean bbFilterValidity = (Boolean) clctlovSource.getProperty(NuclosCollectableListOfValues.PROPERTY_FILTER_VALIDITY);
		if (LangUtils.defaultIfNull(bbFilterValidity, false)) {
			// When opened as a search box and the component does not forbid it,
			// valid filter is always active and checkbox is not shown
			hideInvalidityFilter();
		}

		super.runLookupCollectable(clctlovSource);
	}

	/**
	 * Activate or deactivate filtering by active sign/validFrom - validUntil.
	 *
	 * @param bFilter
	 */
	public void filterByValidity(boolean bFilter) {
		if (this.isSearchPanelAvailable()) {
			// chkbxHideInvalid.setSelected(bFilter);
			clearAndToggleEnableComponentsInSearchTab("validFrom", bFilter);
			clearAndToggleEnableComponentsInSearchTab("validUntil", bFilter);
			clearAndToggleEnableComponentsInSearchTab("active", bFilter);
		}
	}

	/**
	 * @param sFieldName
	 * @param bFilter
	 * @precondition this.isSearchPanelAvailable()
	 */
	private void clearAndToggleEnableComponentsInSearchTab(String sFieldName, boolean bFilter) {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("Search panel is not available.");
		}
		for (CollectableComponent clctcomp : this.getSearchPanel().getEditView().getCollectableComponentsFor(sFieldName)) {
			if (bFilter) {
				clctcomp.clear();
			}
			clctcomp.setEnabled(!bFilter);
		}
	}

	protected void hideInvalidityFilter() {
		if (this.isSearchPanelAvailable()) {
			this.filterByValidity(true);
			this.getSearchPanel().chkbxHideInvalid.setSelected(true);
			this.getSearchPanel().chkbxHideInvalid.setVisible(false);
		}
	}

	/**
	 * @return a specific table model, with support for chunkwise reading.
	 *
	 * @deprecated Move to ResultController hierarchy.
	 */
	@Override
	protected SortableCollectableTableModel<CollectableMasterDataWithDependants> newResultTableModel() {
		final SortableCollectableTableModel<CollectableMasterDataWithDependants> result = super.newResultTableModel();

		// clicking a column header is to cause a new search on the server:
		TableUtils.removeMouseListenersForSortingFromTableHeader(this.getResultTable());
		TableUtils.addMouseListenerForSortingToTableHeader(this.getResultTable(), result, new CommonRunnable() {
			@Override
			public void run() {
				getResultController().getSearchResultStrategy().cmdRefreshResult();
			}
		});

		return result;
	}

	/**
	 * sets up the change listener for the vertical scrollbar of the result
	 * table, only if the proxy list has been set (that is not before the first
	 * search).
	 */
	public void setupChangeListenerForResultTableVerticalScrollBar() {
		final ProxyList<? extends Collectable> lstclct = getSearchStrategy().getCollectableProxyList();
		if (lstclct != null) {
			this.getResultPanel().setupChangeListenerForResultTableVerticalScrollBar(lstclct, this.getTab());
		}
	}

	public void removePreviousChangeListenersForResultTableVerticalScrollBar() {
		final JScrollBar scrlbarVertical = this.getResultPanel().getResultTableScrollPane().getVerticalScrollBar();
		final DefaultBoundedRangeModel model = (DefaultBoundedRangeModel) scrlbarVertical.getModel();
		NuclosResultPanel.removePreviousChangeListenersForResultTableVerticalScrollBar(model);
	}

	/**
	 * gathers the data from all enabled subforms. All rows are gathered, even
	 * the removed ones.
	 *
	 * @param oParentId
	 *            set as the parent id for each subform row.
	 * @return the data from all subforms
	 */
	protected final DependantCollectableMasterDataMap getAllSubFormData(Object oParentId) throws CommonValidationException {
		final DependantCollectableMasterDataMap result = new DependantCollectableMasterDataMap();

		for (DetailsSubFormController<CollectableEntityObject> subformctl : this.getSubFormControllersInDetails()) {
			EntityMetaDataVO eMeta = MetaDataClientProvider.getInstance().getEntity(subformctl.getCollectableEntity().getName());
			if (!eMeta.isEditable()) {
				continue;
			}
			// if (subformctl.getSubForm().isEnabled() &&
			// subformctl.getSubForm().getParentSubForm() == null) {
			// NUCLEUSINT-1119
			if (subformctl.getSubForm().getParentSubForm() == null) {
				result.addValues(subformctl.getSubForm().getEntityName(), subformctl.getAllCollectables(oParentId, this.getSubFormControllersInDetails(), true, null));
			}
		}

		return result;
	}

	@Override
	public CollectableMasterDataWithDependants findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		assert this.getCollectableEntity() != null;

		final MasterDataWithDependantsVO mdwdcvo = this.mddelegate.getWithDependants(sEntity, oId, this.getEntityAndForeignKeyFieldNamesFromSubForms());
		final CollectableMasterDataWithDependants result = new CollectableMasterDataWithDependants(this.getCollectableEntity(), mdwdcvo);
		assert getSearchStrategy().isCollectableComplete(result);
		return result;
	}
	
	@Override
	protected CollectableMasterDataWithDependants findCollectableById(String sEntity, Object oId, Collection<EntityFieldMetaDataVO> fields) throws CommonBusinessException {
		if (fields == null) {
			return findCollectableById(sEntity, oId);
		}
		Collection<EntityObjectVO> eovos = EntityObjectDelegate.getInstance().getEntityObjectsMore(
				MetaDataClientProvider.getInstance().getEntity(sEntity).getId(), 
				Collections.singletonList(IdUtils.toLongId(oId)), 
				fields, getCustomUsage());
		if (eovos.isEmpty()) {
			return null;
		} else {
			return new CollectableMasterDataWithDependants(this.getCollectableEntity(), DalSupportForMD.getMasterDataWithDependantsVO(eovos.iterator().next()));
		}
	}

	@Override
	protected CollectableMasterDataWithDependants findCollectableByIdWithoutDependants(String sEntity, Object oId) throws CommonBusinessException {
		assert this.getCollectableEntity() != null;

		final MasterDataVO mdcvo = this.mddelegate.get(sEntity, oId);
		final CollectableMasterDataWithDependants result = CollectableMasterDataWithDependants.newInstance(this.getCollectableEntity(), mdcvo);
		assert getSearchStrategy().isCollectableComplete(result);
		return result;
	}

	@Override
	public Integer getVersionOfCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		if (sEntity == null) {
			throw new IllegalArgumentException("sEntityName");
		}
		if (oId == null) {
			throw new IllegalArgumentException("oId");
		}

		return this.mddelegate.getVersion(sEntity, oId);
	}

	/**
	 * @todo move to DetailsPanel/DetailsController
	 */
	private List<EntityAndFieldName> getEntityAndForeignKeyFieldNamesFromSubForms() {
		return CollectionUtils.transform(this.getSubFormControllersInDetails(), new GetEntityAndForeignKeyFieldName());
	}

	/** @todo this is just a workaround */
	protected DependantMasterDataMap readDependants(Object oId) {
		return this.mddelegate.getDependants(oId, this.getEntityAndForeignKeyFieldNamesFromSubForms());
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(final CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		// We have to clear the ids for cloned objects:
		/**
		 * @todo eliminate this workaround - this is the wrong place. The right
		 *       place is the Clone action!
		 */
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		final AtomicReference<MasterDataVO> mdvoInserted = new AtomicReference<MasterDataVO>();
		invoke(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				mdvoInserted.set(mddelegate.create(getEntityName(), clctNew.getMasterDataCVO(), mpmdvoDependants, getCustomUsage()));
			}
		});

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted.get(), this.readDependants(mdvoInserted.get().getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCurrentCollectable(CollectableMasterDataWithDependants clctCurrent) throws CommonBusinessException {
		return this.updateCollectable(clctCurrent, this.getAllSubFormData(clctCurrent.getId()));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(final CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oAdditionalData;

		final AtomicReference<Object> oId = new AtomicReference<Object>();
		invoke(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				oId.set(mddelegate.update(getEntityName(), clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap(), getCustomUsage()));
			}
		});

		final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId.get());
		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	@Override
	protected Object getAdditionalDataForMultiUpdate(CollectableMasterDataWithDependants clct) throws CommonValidationException {
		return multiupdateofdependants.getDependantCollectableMapForUpdate(this.getSubFormControllersInDetails(), clct);
	}

	@Override
	protected void deleteCollectable(final CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		invoke(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				mddelegate.remove(getEntityName(), clct.getMasterDataCVO(), getCustomUsage());
			}
		});
	}

	@Override
	protected String getEntityLabel() {
		return this.getCollectableEntity().getLabel();
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

			private MasterDataIdAndEntity getTransferableObject(final JTable tbl, int iSelectedRow, final CollectableMasterDataWithDependants clct) {
				return new MasterDataIdAndEntity(clct.getMasterDataCVO().getIntId(), MasterDataCollectController.this.getEntityName(), clct.getIdentifierLabel());
			}

			@Override
			protected Transferable createTransferable(JComponent comp) {
				Transferable result = null;
				if (comp == tbl) {
					final int[] aiSelectedRows = tbl.getSelectedRows();
					final List<MasterDataIdAndEntity> lstimp = new ArrayList<MasterDataIdAndEntity>(aiSelectedRows.length);
					for (int iSelectedRow : aiSelectedRows) {
						final CollectableMasterDataWithDependants clct = getResultTableModel().getCollectable(iSelectedRow);
						MasterDataIdAndEntity transferableObject = getTransferableObject(tbl, iSelectedRow, clct);
						lstimp.add(transferableObject);
					}
					if (!lstimp.isEmpty())
						result = new MasterDataVOTransferable(lstimp, null);
				}
				return result;
			}
		});
	}

	@Override
	protected void setupSubFormController(Map<String, SubForm> mpSubForm, Map<String, ? extends SubFormController> mpSubFormController) {
		Map<SubForm, MasterDataSubFormController> mpSubFormController_tmp = new HashMap<SubForm, MasterDataSubFormController>();

		// create a map of subforms and their controllers
		for (String sSubFormEntityName : mpSubFormController.keySet()) {
			SubFormController subformcontroller = mpSubFormController.get(sSubFormEntityName);
			SubForm subform = subformcontroller.getSubForm();
			if (subformcontroller instanceof DetailsSubFormController<?>) {
				((DetailsSubFormController<CollectableMasterDataWithDependants>) subformcontroller).setCollectController(this);
				mpSubFormController_tmp.put(subform, (MasterDataSubFormController) subformcontroller);
			}
			// disable child subforms in searchpanel, because it's not possible
			// to search for data in those subforms
			else if (subformcontroller instanceof SearchConditionSubFormController)
				if (subform.getParentSubForm() != null)
					subform.setEnabled(false);
		}

		// assign child subforms to their parents
		for (SubForm subform : mpSubFormController_tmp.keySet()) {
			SubForm parentsubform = mpSubForm.get(subform.getParentSubForm());
			if (parentsubform != null) {
				MasterDataSubFormController subformcontroller = mpSubFormController_tmp.get(parentsubform);
				subformcontroller.addChildSubFormController(mpSubFormController_tmp.get(subform));
			}
		}
	}

	@Override
	protected Collection<? extends SubFormController> getSubFormControllers(boolean bSearchTab) {
		return bSearchTab ? mpsubformctlSearch.values() : this.getSubFormControllersInDetails();
	}

	@Override
	protected Map<String, DetailsSubFormController<CollectableEntityObject>> getMapOfSubFormControllersInDetails() {
		return this.mpsubformctlDetails;
	}

	/**
	 * @postcondition result != null
	 */
	@Override
	protected Collection<DetailsSubFormController<CollectableEntityObject>> getSubFormControllersInDetails() {
		return CollectionUtils.valuesOrEmptySet(this.getMapOfSubFormControllersInDetails());
	}

	private class GetEntityAndForeignKeyFieldName implements Transformer<SubFormController, EntityAndFieldName> {
		@Override
		public EntityAndFieldName transform(SubFormController subformctl) {
			return subformctl.getEntityAndForeignKeyFieldName();
		}
	}

	class MasterDataCollectPanel extends CollectPanel<CollectableMasterDataWithDependants> {

		MasterDataCollectPanel(Long entityId, boolean bSearchPanelAvailable, boolean bDetailsInOverlay) {
			super(entityId, bSearchPanelAvailable, bDetailsInOverlay);
		}

		@Override
		public SearchPanel newSearchPanel(Long entityId) {
			/** @todo creating an empty search panel is just a workaround! */
			return new MasterDataSearchPanel(entityId, this.containsSearchPanel() ? newLayoutRoot(true) : LayoutRoot.newEmptyLayoutRoot(true));
		}

		@Override
		public DetailsPanel newDetailsPanel(Long entityId) {
			return LayoutComponentUtils.setPreferences(getEntityPreferences(), new MasterDataDetailsPanel(entityId, detailsWithScrollbar));
		}

		@Override
		public ResultPanel<CollectableMasterDataWithDependants> newResultPanel(Long entityId) {
			return new NuclosResultPanel<CollectableMasterDataWithDependants>(entityId);
		}
	}

	protected class MasterDataSearchPanel extends SearchPanel {

		private final JCheckBoxMenuItem chkbxHideInvalid = new JCheckBoxMenuItem();

		MasterDataSearchPanel(Long entityId) {
			this(entityId, newLayoutRoot(true));
		}

		MasterDataSearchPanel(Long entityId, LayoutRoot layoutroot) {
			super(entityId);

			/** @todo this could be done in super(...) */
			this.setEditView(new LayoutMLEditView(layoutroot));

			// this.setCustomToolBarArea(newToolBar());
			initToolBar();
		}

		private void initToolBar() {
			// additional functionality in Search panel:
			// final JToolBar result = UIUtils.createNonFloatableToolBar();

			if (isFilteringAppropriate()) {
				// Generate checkbox for filtering of inactive/invalid entries
				chkbxHideInvalid.setText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.13", "Nur g\u00fcltige und aktive Eintr\u00e4ge"));
				chkbxHideInvalid.setToolTipText(getSpringLocaleDelegate().getMessage("MasterDataCollectController.4", "Eintr\u00e4ge beschr\u00e4nken (g\u00fcltig/ung\u00fcltig)"));
				// chkbxHideInvalid.setMaximumSize(chkbxHideInvalid.getMinimumSize());

				chkbxHideInvalid.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						if (ev.getSource() instanceof JCheckBox)
							filterByValidity(((JCheckBox) ev.getSource()).isSelected());
						if (ev.getSource() instanceof JCheckBoxMenuItem)
							filterByValidity(((JCheckBoxMenuItem) ev.getSource()).isSelected());

						cmdDisplayCurrentSearchConditionInSearchPanelStatusBar();
					}
				});

				// result.add(Box.createHorizontalStrut(5));
				// result.add(chkbxHideInvalid);
				addPopupExtraMenuItem(chkbxHideInvalid);
			}

			// result.add(Box.createGlue());

			// return result;
		}

	} // inner class MasterDataSearchPanel

	protected class MasterDataDetailsPanel extends DetailsPanel {

		@Deprecated
		private final LayoutRoot layoutroot;

		public MasterDataDetailsPanel(Long entityId) {
			this(entityId, newLayoutRoot(false));
		}

		public MasterDataDetailsPanel(Long entityId, boolean withScrollbar) {
			this(entityId, newLayoutRoot(false), withScrollbar);
		}

		@Deprecated
		private MasterDataDetailsPanel(Long entityId, LayoutRoot layoutroot) {
			this(entityId, layoutroot, true);
		}

		@Deprecated
		private MasterDataDetailsPanel(Long entityId, LayoutRoot layoutroot, boolean withScrollbar) {
			super(entityId, withScrollbar);

			this.layoutroot = layoutroot;

			this.setEditView(new LayoutMLEditView(layoutroot));
		}

		public LayoutRoot getLayoutRoot() {
			return this.layoutroot;
		}

		/**
		 * @param compRoot
		 *            the edit component according to the LayoutML
		 * @return the edit component to be used in the Details panel. Default
		 *         is <code>compRoot</code> itself. Successors may build their
		 *         own component/panel out of compRoot.
		 * @todo pull down to SearchOrDetailsPanel and/or change signature into
		 *       EditView newEditView()
		 */
		public JComponent newEditComponent(JComponent compRoot) {
			return compRoot;
		}

	} // inner class MasterDataDetailsPanel

	private List<CollectableEntityFieldWithEntity> getSelectedFields() {
		List<CollectableEntityFieldWithEntity> lst = new ArrayList<CollectableEntityFieldWithEntity>();
		for (CollectableEntityField cef : getFields().getSelectedFields()) {
			lst.add(new CollectableEntityFieldWithEntity(this.getCollectableEntity(), cef.getName()));
		}
		return lst;
		// return (List<CollectableEntityFieldWithEntity>)
		// this.fields.getSelectedFields();
	}

	/**
	 * invokes dialog for export of result table list. If no lines are selected,
	 * just exports the result list. If one or more lines are selected, asks
	 * whether to export result list or to print appropriate reports for
	 * selection
	 *
	 * @precondition getCollectStateModel().getOuterState() ==
	 *               CollectState.OUTERSTATE_RESULT
	 */
	private void cmdPrint() {
		assert getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;

		UIUtils.runCommand(getTab(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				new ReportController(getTab()).export(MasterDataCollectController.this.getResultTable(), null);
			}
		});
	}

	private static class ParameterTransformer implements Transformer<String, String> {

		private final MasterDataVO mdvo;

		public ParameterTransformer(MasterDataVO mdvo) {
			this.mdvo = mdvo;
		}

		@Override
		public String transform(String rid) {
			// the first element is the key; all other are flags separated by
			// ':'
			String[] elems = rid.split(":");

			String resIfNull = "";
			for (int i = 1; i < elems.length; i++) {
				if (elems[i].startsWith("ifnull="))
					resIfNull = elems[i].substring(7);
			}

			final Object value = mdvo.getField(elems[0]);
			return value != null ? value.toString() : resIfNull;
		}
	}

	/**
	 * refresh valuelists after reloading current collectable (NUCLOSINT-851)
	 */
	@Override
	public void refreshCurrentCollectable() throws CommonBusinessException {
		super.refreshCurrentCollectable();
		for (CollectableComponent c : getDetailsEditView().getCollectableComponents()) {
			if (c instanceof CollectableComboBox) {
				final CollectableComboBox comboBox = (CollectableComboBox) c;
				comboBox.refreshValueList(false);
			}
		}
	}
	
	@Override
	public void enableToolbarButtonsForDetailsMode(final int iDetailsMode) {
		super.enableToolbarButtonsForDetailsMode(iDetailsMode);
		if (iDetailsMode == CollectState.DETAILSMODE_VIEW) {
			setInitialComponentFocusInDetailsTab(); // @see NUCLOS-1027
		}
	}
	
	@Override
	protected LayoutRoot getInitialLayoutMLDefinitionForSearchPanel() {
		LayoutRoot layoutRoot = newLayoutRoot(true);
		getLayoutMLButtonsActionListener().setComponentsEnabled(false);
		return layoutRoot;
	}

	@Override
	protected void _clearSearchFields() {
		super._clearSearchFields();

		if (this.isSearchPanelAvailable()) {
			for (SubFormController subformctl : getSubFormControllers(true)) {
				if (subformctl instanceof SearchConditionSubFormController) {
					((SearchConditionSubFormController) subformctl).clear();
				}
			}
		}
	}

	@Override
	protected void _setSearchFieldsAccordingToSubCondition(CollectableSubCondition cond) throws CommonBusinessException {
		final String sEntityNameSub = cond.getSubEntityName();
		final SearchConditionSubFormController subformctl = mpsubformctlSearch.get(sEntityNameSub);
		if (subformctl == null)
			throw new NuclosFatalException(getSpringLocaleDelegate().getMessage("GenericObjectCollectController.40", "Ein Unterformular f\u00fcr die Entit\u00e4t {0} ist in der Suchbedingung, aber nicht im aktuellen Layout enthalten.",
					sEntityNameSub));
		subformctl.setCollectableSearchCondition(cond.getSubCondition());
	}

	/**
	 * @param bMakeConsistent
	 * @return the search condition contained in the search panel's fields
	 *         (including the subforms' search fields).
	 * @precondition this.isSearchPanelAvailable()
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 *
	 * @deprecated Move to SearchController or SearchPanel and make protected
	 *             again.
	 */
	@Override
	public CollectableSearchCondition getCollectableSearchConditionFromSearchFields(boolean bMakeConsistent) throws CollectableFieldFormatException {
		if (!isSearchPanelAvailable())
			throw new IllegalStateException("!this.isSearchPanelAvailable()");
		final CollectableSearchCondition cond = super.getCollectableSearchConditionFromSearchFields(bMakeConsistent);

		final CompositeCollectableSearchCondition condAnd = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		if (cond != null)
			condAnd.addOperand(cond);

		assert getMapOfSubFormControllersInSearch() != null;
		for (SearchConditionSubFormController subformctl : getMapOfSubFormControllersInSearch().values()) {
			subformctl.getSubForm().getSubformTable().getModel();

			for (CollectableSearchCondition subCond : subformctl.getCollectableSubformSearchConditions()) {
				if (subCond != null)
					condAnd.addOperand(new CollectableSubCondition(subformctl.getCollectableEntity().getName(), subformctl.getForeignKeyFieldName(), subCond));
			}
			/*
			 * Subcondition or old code do not delete please final
			 * CollectableSearchCondition condSub =
			 * subformctl.getCollectableSearchCondition(); if (condSub != null)
			 * condAnd.addOperand(new
			 * CollectableSubCondition(subformctl.getCollectableEntity
			 * ().getName(), subformctl.getForeignKeyFieldName(), condSub));
			 */

		}

		final CollectableSearchCondition result = SearchConditionUtils.simplified(condAnd);
		assert result == null || result.isSyntacticallyCorrect();
		return result;
	}

	@Override
	public void makeConsistent(boolean bSearchTab) throws CollectableFieldFormatException {
		super.makeConsistent(bSearchTab);

		if (!stopEditing(bSearchTab))
			/** @todo we need to give a better error message here. */
			throw new CollectableFieldFormatException(getSpringLocaleDelegate().getMessage("GenericObjectCollectController.95", "Ung\u00fcltige Eingabe in Unterformular."));
	}

	/**
	 * @todo move to CollectController after renaming stopEditing() to
	 *       stopEditingInDetails()
	 * @param bSearchTab
	 * @return Has the editing been stopped?
	 */
	protected boolean stopEditing(boolean bSearchTab) {
		return bSearchTab ? stopEditingInSearch() : stopEditingInDetails();
	}

	/**
	 * stops editing in the Search panel. Derived classes may stop editing on
	 * fields, TableCellEditors etc. here
	 *
	 * @return Has the editing been stopped?
	 *
	 *         TODO: Make this protected again.
	 */
	@Override
	public boolean stopEditingInSearch() {
		if (getMapOfSubFormControllersInSearch() != null) {
			for (SearchConditionSubFormController subformctl : getMapOfSubFormControllersInSearch().values()) {
				subformctl.stopEditing();
			}
		}
		return true;
	}

	@Override
	public Map<String, DetailsSubFormController<CollectableEntityObject>> getDetailsSubforms() {
		return this.mpsubformctlDetails;
	}

	@Override
	public void cmdGenerateObject(GeneratorActionVO generatoractionvo) {
		Map<Long, UsageCriteria> sources = new HashMap<Long, UsageCriteria>();
		for (CollectableMasterDataWithDependants clct : getSelectedCollectables()) {
			sources.put(IdUtils.toLongId(clct.getId()), null);
		}
		GenerationController controller = new GenerationController(sources, generatoractionvo, this, getTab());
		controller.generateGenericObject();
	}

	@Override
	public List<GeneratorActionVO> getGeneratorActions() {
		if (getSelectedCollectableId() == null) {
			return Collections.emptyList();
		}
		Integer entityId = IdUtils.unsafeToId(MetaDataClientProvider.getInstance().getEntity(getEntity()).getId());
		return GeneratorActions.getActions(entityId, null, null);
	}
	
	@Override
	public List<GeneratorActionVO> getGeneratorActions(Collection<CollectableMasterDataWithDependants> selectedCollectablesFromResult) {
		return getGeneratorActions();
	}

	@Override
	public Collection<RuleVO> getUserRules() {
		Integer entityId = IdUtils.unsafeToId(MetaDataClientProvider.getInstance().getEntity(getEntity()).getId());
		UsageCriteria uc = new UsageCriteria(entityId, null, null, getCustomUsage());

		final Collection<RuleVO> collRules = RuleDelegate.getInstance().findRulesByUsageAndEvent(RuleEventUsageVO.USER_EVENT, uc);
		// remove inactive rules
		CollectionUtils.removeAll(collRules, new Predicate<RuleVO>() {
			@Override
			public boolean evaluate(RuleVO rulevo) {
				return !rulevo.isActive();
			}
		});
		return collRules;
	}

	@Override
	protected void readValuesFromEditPanel(CollectableMasterDataWithDependants clct, boolean bSearchTab) throws CollectableValidationException {
		super.readValuesFromEditPanel(clct, bSearchTab);
		if (ctlTranslations != null) {
			clct.getMasterDataCVO().setResources(ctlTranslations.getResources());
		}
	}

} // class MasterDataCollectController

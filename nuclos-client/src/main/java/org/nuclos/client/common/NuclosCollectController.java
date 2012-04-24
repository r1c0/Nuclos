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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.datasource.NuclosSearchConditionUtils;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.main.mainframe.workspace.TabRestoreController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SaveFilterController;
import org.nuclos.client.searchfilter.SearchFilter;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.client.ui.BlackLabel;
import org.nuclos.client.ui.CenteringPanel;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateConstants;
import org.nuclos.client.ui.collect.detail.DetailsPanel;
import org.nuclos.client.ui.collect.result.NuclosResultController;
import org.nuclos.client.ui.collect.result.NuclosSearchResultStrategy;
import org.nuclos.client.ui.collect.result.ResultController;
import org.nuclos.client.ui.collect.search.ISearchStrategy;
import org.nuclos.client.ui.layoutml.LayoutMLButtonActionListener;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.attribute.BadGenericObjectException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Configurable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Base class for all <code>CollectController</code>s in Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class NuclosCollectController<Clct extends Collectable> extends CollectController<Clct> {

	private static final Logger LOG = Logger.getLogger(NuclosCollectController.class);

	protected String sEntity;
	protected static final String PREFS_KEY_OUTERSTATE = "outerState";
	protected static final String PREFS_KEY_ENTITY = "entity";
	protected static final String PREFS_KEY_COLLECTABLEID = "collectableId";
	protected static final String PREFS_NODE_SEARCHCONDITION = "searchCondition";

	/**
	 * Listener for search filter
	 */
	private final ActionListener alSearchFilter = new ActionListener() {
		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdSetCollectableSearchConditionAccordingToFilter();
		}
	};


	/**
	 * <code>ActionListener</code> defining actions for buttons in the LayoutML definition.
	 */
	private LayoutMLButtonActionListener alLayoutMLButtons;

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
	protected NuclosCollectController(String sEntityName, MainFrameTab tabIfAny) {
		this(NuclosCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName), tabIfAny);
		this.sEntity = sEntityName;
	}

	/**
	 * Don't make this public!
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected NuclosCollectController(CollectableEntity clcte, MainFrameTab tabIfAny) {
		super(clcte, tabIfAny, new NuclosResultController<Clct>(clcte, new NuclosSearchResultStrategy<Clct>()));
		this.sEntity = clcte.getName();
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
	protected NuclosCollectController(String sEntityName, MainFrameTab tabIfAny, ResultController<Clct> rc) {
		this(NuclosCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName), tabIfAny, rc);
		this.sEntity = sEntityName;
	}

	protected NuclosCollectController(CollectableEntity clcte, MainFrameTab tabIfAny, ResultController<Clct> rc) {
		super(clcte, tabIfAny, rc);
		this.sEntity = clcte.getName();
	}

	public String getEntity() {
		return sEntity;
	}

//	The following is a trial to encapsulate the data access for specific controllers. @see CollectModel
//	/**
//	* @return the model used in this controller.
//	* @todo make abstract and move to CollectController
//	*/
//	public CollectModel<Clct> getModel() {
//	return null;
//	}
//
//	@Deprecated
//	protected Clct newCollectable() {
//	return this.getModel().newCollectable();
//	}
//
//	@Deprecated
//	protected Clct insertCollectable(Clct clctNew) throws CommonBusinessException {
//	return this.getModel().insertCollectable(clctNew);
//	}
//
//	@Deprecated
//	protected Clct updateCollectable(Clct clct, Object oAdditionalData) throws CommonBusinessException {
//	return this.getModel().updateCollectable(clct, oAdditionalData);
//	}
//
//	@Deprecated
//	protected void deleteCollectable(Clct clct) throws CommonBusinessException {
//	this.getModel().deleteCollectable(clct);
//	}

	/**
	 * @todo this method is misused - it sets shortcuts for many things other than tabs...
	 * @param frame
	 */
	@Override
	protected void setupShortcutsForTabs(MainFrameTab frame) {
		final CollectPanel<Clct> pnlCollect = this.getCollectPanel();
		final DetailsPanel pnlDetails = this.getDetailsPanel();

		final Action actSelectSearchTab = new AbstractAction() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				if (pnlCollect.isTabbedPaneEnabledAt(CollectPanel.TAB_SEARCH)) {
					pnlCollect.setTabbedPaneSelectedComponent(getSearchPanel());
				}
			}
		};
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.ACTIVATE_SEARCH_PANEL_1, actSelectSearchTab, pnlCollect);
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.ACTIVATE_SEARCH_PANEL_2, actSelectSearchTab, pnlCollect);

		//TODO This is a workaround. The detailpanel should keep the focus
		final Action actGrabFocus = new AbstractAction() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				pnlDetails.grabFocus();
			}
		};

		/**
		 * A <code>ChainedAction</code> is an action composed of a primary and a secondary action.
		 * It behaves exactly like the primary action, except that additionally, the secondary action is performed
		 * after the primary action.
		 */
		class ChainedAction implements Action {
			private final Action actPrimary;
			private final Action actSecondary;

			public ChainedAction(Action actPrimary, Action actSecondary) {
				this.actPrimary = actPrimary;
				this.actSecondary = actSecondary;
			}

			@Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
				actPrimary.addPropertyChangeListener(listener);
			}

			@Override
            public Object getValue(String sKey) {
				return actPrimary.getValue(sKey);
			}

			@Override
            public boolean isEnabled() {
				return actPrimary.isEnabled();
			}

			@Override
            public void putValue(String sKey, Object oValue) {
				actPrimary.putValue(sKey, oValue);
			}

			@Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
				actPrimary.removePropertyChangeListener(listener);
			}

			@Override
            public void setEnabled(boolean bEnabled) {
				actPrimary.setEnabled(bEnabled);
			}

			@Override
            public void actionPerformed(ActionEvent ev) {
				actPrimary.actionPerformed(ev);
				actSecondary.actionPerformed(ev);
			}
		}

		//final Action actRefresh = new ChainedAction(this.getRefreshCurrentCollectableAction(), actGrabFocus);

		this.getCollectPanel().setTabbedPaneToolTipTextAt(CollectPanel.TAB_SEARCH, getSpringLocaleDelegate().getMessage(
				"NuclosCollectController.13","Suche (F7) (Strg+F)"));
		this.getCollectPanel().setTabbedPaneToolTipTextAt(CollectPanel.TAB_RESULT, getSpringLocaleDelegate().getMessage(
				"NuclosCollectController.7","Ergebnis (F8)"));
		this.getCollectPanel().setTabbedPaneToolTipTextAt(CollectPanel.TAB_DETAILS, getSpringLocaleDelegate().getMessage(
				"NuclosCollectController.3","Details (F2)"));

		// the search action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.START_SEARCH, this.getSearchAction(), pnlCollect);
		KeyBinding keybinding = KeyBindingProvider.REFRESH;

		// the refresh action
		pnlDetails.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		pnlDetails.getActionMap().put(keybinding.getKey(), this.getRefreshCurrentCollectableAction());
		getResultPanel().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		getResultPanel().getActionMap().put(keybinding.getKey(), getResultPanel().btnRefresh.getAction());


		// the new action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.NEW, this.getNewAction(), pnlDetails);

		// the new with search values action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.NEW_SEARCHVALUE, this.getNewWithSearchValuesAction(), pnlCollect);

		// the save action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.SAVE_1, this.getSaveAction(), pnlCollect);
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.SAVE_2, this.getSaveAction(), pnlCollect);

		// first the navigation actions are performed and then the focus is grabbed:
		final Action actFirst = new ChainedAction(this.getFirstAction(), actGrabFocus);
		final Action actLast = new ChainedAction(this.getLastAction(), actGrabFocus);
		final Action actPrevious = new ChainedAction(this.getPreviousAction(), actGrabFocus);
		final Action actNext = new ChainedAction(this.getNextAction(), actGrabFocus);

		// the first action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.FIRST, actFirst, pnlDetails);
		pnlDetails.btnFirst.setAction(actFirst);

		// the last action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.LAST, actLast, pnlDetails);
		pnlDetails.btnLast.setAction(actLast);

		// the previous action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.PREVIOUS_1, actPrevious, pnlDetails);
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.PREVIOUS_2, actPrevious, pnlDetails);
		pnlDetails.btnPrevious.setAction(actPrevious);

		// the next action
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.NEXT_1, actNext, pnlDetails);
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.NEXT_2, actNext, pnlDetails);
		pnlDetails.btnNext.setAction(actNext);

		Action actClose = new AbstractAction() {
			@Override
            public void actionPerformed(ActionEvent e) {
				getTab().dispose();
			}
		};
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.CLOSE_CHILD, actClose, pnlCollect);

		if (getResultPanel() != null && getResultTable() != null) {
			final JButton btnEdit = getResultPanel().btnEdit;
			KeyBindingProvider.bindActionToComponent(KeyBindingProvider.EDIT_1, btnEdit.getAction(), getResultTable());
			KeyBindingProvider.bindActionToComponent(KeyBindingProvider.EDIT_2, btnEdit.getAction(), getResultTable());
		}
	}

	@Override
	protected Preferences getUserPreferencesRoot() {
		return org.nuclos.common2.ClientPreferences.getUserPreferences();
	}

	@Override
	public void lockFrame(boolean bLock) {
		super.lockFrame(bLock);
	}

	/**
	 * @return false (default)
	 */
	@Override
	protected boolean isMultiEditAllowed() {
		return false;
	}

	/**
	 * @deprecated Move to ResultController hierarchy and make protected again.
	 *   It would be far better, if the class (hierarchy) would known that search
	 *   should be single- or multi-threaded.
	 */
	@Override
	public boolean isMultiThreadingEnabled() {
		// enable/disable multithreading here:
//		return false;
		return true;
	}

	/**
	 * @return Is this <code>CollectController</code> restorable from preferences? Default: true.
	 */
	public boolean isRestorableFromPreferences() {
		return true;
	}

	private static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		String entity;
		Integer iCollectState;
		Long objectId;
		CollectableSearchCondition searchCondition;
		Map<String, String> inheritControllerPreferences = new HashMap<String, String>(1);
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	protected void storeInstanceStateToPreferences(Map<String, String> inheritControllerPreferences) {}

	protected void restoreInstanceStateFromPreferences(Map<String, String> inheritControllerPreferences) {}

	public static class NuclosCollectTabStoreController implements ITabStoreController {

		private WeakReference<NuclosCollectController<?>> refCtl;

		public NuclosCollectTabStoreController(NuclosCollectController<?> ctl) {
			this.refCtl = new WeakReference<NuclosCollectController<?>>(ctl);
		}

		@Override
		public String getPreferencesXML() {
			final NuclosCollectController<?> ctl = refCtl.get();
			if (ctl == null) {
				throw new NullPointerException("Controller has already been gc'ed");
			}
			
			final RestorePreferences rp = new RestorePreferences();
			rp.entity = ctl.getEntity();
			try {
				if (ctl.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_DETAILS) {
					rp.iCollectState = CollectState.OUTERSTATE_DETAILS;
					ctl.writeCurrentCollectableToPreferences(rp);
				}
				ctl.writeStateToPreferences(rp);
			} catch(Exception e) {
				LOG.warn("Preferences not completely stored.", e);
			}

			ctl.storeInstanceStateToPreferences(rp.inheritControllerPreferences);
			return toXML(rp);
		}

		@Override
		public Class<?> getTabRestoreControllerClass() {
			return NuclosCollectTabRestoreController.class;
		}

	}

	public static class NuclosCollectTabRestoreController extends TabRestoreController {

		@Override
		public void restoreFromPreferences(String preferencesXML, MainFrameTab tab) throws Exception {
			RestorePreferences rp = fromXML(preferencesXML);

			NuclosCollectController<?> ctl = createFromPreferences(rp, tab);
			final MainController mc = Main.getInstance().getMainController();
			
			mc.initMainFrameTab(ctl, tab);
			// Main.getMainController().addMainFrameTab would be called from listener inside of initMainFrameTab, but only when tab added.
			// During restore the tabs are already added, so we need to do this manually.
			mc.addMainFrameTab(tab, ctl);

			ctl.restoreInstanceStateFromPreferences(rp.inheritControllerPreferences);
		}

	}

	/**
	 * @param prefs
	 * @param parent
	 * @return a new <code>NuclosCollectController</code> with the state read from the preferences.
	 */
	@Deprecated
	public static NuclosCollectController<?> createFromPreferences(final Preferences prefs)  throws PreferencesException {
		final String sEntity = prefs.get(PREFS_KEY_ENTITY, null);
		if (sEntity == null) {
			throw new PreferencesException(SpringLocaleDelegate.getInstance().getMessage(
					"NuclosCollectController.6","Entit\u00e4t {0} fehlt in den Benutzereinstellungen.", sEntity));
		}

		boolean allowed;
		allowed = SecurityCache.getInstance().isReadAllowedForModule(sEntity, null) || SecurityCache.getInstance().isReadAllowedForMasterData(sEntity);

		if(!allowed) {
			throw new PreferencesException(SpringLocaleDelegate.getInstance().getMessage(
					"NuclosCollectController.17", "Sie haben kein Recht die Entit\u00e4t ''{0}'' zu verwenden.", sEntity));
		}

		try {
			final NuclosCollectController<?> result = NuclosCollectControllerFactory.getInstance().newCollectController(sEntity, null);

			final int iCollectState = result.restoreStateFromPreferences(prefs);

			UIUtils.runCommandLaterForTabbedPane(MainFrame.getPredefinedEntityOpenLocation(sEntity), new CommonRunnable() {
				// This must be done later as reloading the layout in restoreStateFromPreferences is done later also:
				@Override
				public void run() throws CommonBusinessException {
					switch (iCollectState) {
					case CollectState.OUTERSTATE_SEARCH:
						result.runSearch(false);
						break;
					case CollectState.OUTERSTATE_RESULT:
						result.runViewAll(false);
						break;
					case CollectState.OUTERSTATE_DETAILS:
						final Collectable clct = result.getCurrentCollectableFromPreferences(prefs);
						if (clct == null) {
							result.runNew(false);
						}
						else {
							((NuclosCollectController) result).runViewSingleCollectable(clct, false);
						}
						break;
					default:
						throw new PreferencesException(SpringLocaleDelegate.getInstance().getMessage(
								"NuclosCollectController.14","Ung\u00fcltiger Erfassungsstatus in den Benutzereinstellungen: {0}", iCollectState));
					}
				}
			});

			return result;
		}
		catch (CommonBusinessException ex) {
			throw new PreferencesException(ex.getMessage(),ex);
		}
	}

	/**
	 *
	 * @param rp
	 * @param tabIfAny
	 * @return
	 * @throws CommonBusinessException
	 */
	public static NuclosCollectController<?> createFromPreferences(final RestorePreferences rp, MainFrameTab tabIfAny) throws CommonBusinessException{
		boolean allowed;
		allowed = SecurityCache.getInstance().isReadAllowedForModule(rp.entity, null) || SecurityCache.getInstance().isReadAllowedForMasterData(rp.entity);

		if(!allowed) {
			throw new PreferencesException(SpringLocaleDelegate.getInstance().getMessage(
					"NuclosCollectController.17", "Sie haben kein Recht die Entit\u00e4t ''{0}'' zu verwenden.", rp.entity));
		}

		final NuclosCollectController<?> result = NuclosCollectControllerFactory.getInstance().newCollectController(rp.entity, tabIfAny);

		final int cs = result.restoreStateFromPreferences(rp.iCollectState, rp.searchCondition);

		UIUtils.runCommandLater(tabIfAny != null? tabIfAny : Main.getInstance().getMainFrame().getHomePane().getComponentPanel(), new CommonRunnable() {
			// This must be done later as reloading the layout in restoreStateFromPreferences is done later also:
			@Override
			public void run() throws CommonBusinessException {
				switch (cs) {
				case CollectState.OUTERSTATE_SEARCH:
					result.runSearch(false);
					break;
				case CollectState.OUTERSTATE_RESULT:
					result.runViewAll(false);
					break;
				case CollectState.OUTERSTATE_DETAILS:
					if (rp.objectId == null) {
						result.runNew(false);
					} else {
						((NuclosCollectController) result).runViewSingleCollectableWithId(rp.objectId.intValue(), false);
					}
					break;
				default:
					throw new PreferencesException(SpringLocaleDelegate.getInstance().getMessage(
							"NuclosCollectController.14","Ung\u00fcltiger Erfassungsstatus in den Benutzereinstellungen: {0}", rp.iCollectState));
				}
			}
		});

		return result;
	}

	/**
	 * restores the state of this <code>CollectController</code> from the preferences.
	 * @param iCollectState
	 * @param cond
	 * @return
	 * @throws CommonBusinessException
	 */
	protected int restoreStateFromPreferences(Integer iCollectState, CollectableSearchCondition cond) throws CommonBusinessException {
		if (this.isSearchPanelAvailable()) {
			assert this.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_UNDEFINED;
			// Always set Search mode initially, to restore the search criteria.
			// Later, the previous state is restored.
			this.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED);
			this.restoreSearchCriteriaFromPreferences(cond);
		}
		// restore collect state:
		return LangUtils.defaultIfNull(iCollectState, CollectState.OUTERSTATE_UNDEFINED);
	}

	/**
	 * restores the state of this <code>CollectController</code> from the preferences.
	 * @param prefs
	 * @return the stored collect state.
	 * @throws NuclosBusinessException
	 */
	@Deprecated
	protected int restoreStateFromPreferences(Preferences prefs) throws CommonBusinessException {
		if (this.isSearchPanelAvailable()) {
			assert this.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_UNDEFINED;
			// Always set Search mode initially, to restore the search criteria.
			// Later, the previous state is restored.
			this.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED);
			this.restoreSearchCriteriaFromPreferences(prefs);
		}
		// restore collect state:
		return prefs.getInt(PREFS_KEY_OUTERSTATE, CollectState.OUTERSTATE_UNDEFINED);
	}

	/**
	 * @param prefs
	 * @throws CollectableFieldFormatException
	 * @throws PreferencesException
	 * @precondition this.isSearchPanelVisible()
	 */
	protected void writeSearchCriteriaToPreferences(RestorePreferences rp) throws CommonBusinessException {
		// write current search criteria (which may differ from the current search filter's criteria):

		// We don't call makeConsistent (see below), but we want to get the last input of a table cell editor, if any:
		this.stopEditingInSearch();

		// Note that makeConsistent is not called here. If the search condition is incomplete, the differences in the view
		// to the model are ignored. This seems to be better than throwing an exception here and not storing the search condition at all:
		final CollectableSearchCondition cond = this.getCollectableSearchConditionFromSearchPanel(false);
		rp.searchCondition = cond;
		LOG.debug("Wrote searchcondition to prefs: " + LangUtils.toString(cond));

	}

	/**
	 *
	 * @param cond
	 * @throws CommonBusinessException
	 * @precondition this.isSearchPanelVisible()
	 */
	protected void restoreSearchCriteriaFromPreferences(CollectableSearchCondition cond) throws CommonBusinessException {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("!isSearchPanelVisible()");
		}

		this.setCollectableSearchConditionInSearchPanel(NuclosSearchConditionUtils.restorePlainSubConditions(cond));

		try {
			LOG.debug("restored searchcondition from prefs: " + LangUtils.toString(getSearchStrategy().getCollectableSearchCondition()));
		}
		catch (CollectableFieldFormatException ex) {
			LOG.debug("Exception thrown in log statement", ex);
		}
	}

	/**
	 * @param prefs
	 * @throws CommonBusinessException
	 * @precondition this.isSearchPanelVisible()
	 */
	@Deprecated
	protected void restoreSearchCriteriaFromPreferences(Preferences prefs) throws CommonBusinessException {
		if (!this.isSearchPanelAvailable()) {
			throw new IllegalStateException("!isSearchPanelVisible()");
		}
		// restore search condition:
		final CollectableSearchCondition cond = SearchConditionUtils.getSearchCondition(
				prefs.node(PREFS_NODE_SEARCHCONDITION), this.getCollectableEntity().getName());
		this.restoreSearchCriteriaFromPreferences(cond);
	}

	/**
	 * writes the state of this <code>CollectController</code> to the preferences.
	 * @param prefs
	 * @throws CommonBusinessException
	 * @throws PreferencesException
	 */
	protected void writeStateToPreferences(RestorePreferences rp) throws CommonBusinessException {
		// write collect state:
		rp.iCollectState = this.getCollectStateModel().getOuterState();

		if (this.isSearchPanelAvailable()) {
			this.writeSearchCriteriaToPreferences(rp);
		}
	}

	/**
	 * writes the current collectable to the preferences.
	 * @param prefs
	 */
	protected void writeCurrentCollectableToPreferences(RestorePreferences rp) {
		this.writeSerializableCurrentCollectableIdToPreferences(rp);
	}

	/**
	 * reads the current collectable from the preferences.
	 * @param prefs
	 * @return the current collectable, if any.
	 */
	protected Clct getCurrentCollectableFromPreferences(Preferences prefs) throws PreferencesException,
	CommonBusinessException {
		final Object oId = PreferencesUtils.getSerializable(prefs, PREFS_KEY_COLLECTABLEID);
		return (oId == null) ? null : this.findCollectableById(this.getEntityName(), oId);
	}

	/**
	 * makes the given Collectable complete, if it isn't already.
	 * @param clct
	 * @return
	 * @throws CommonBusinessException
	 * @postcondition isCollectableComplete(result)
	 * @postcondition isCollectableComplete(clct) --> result == clct
	 * @todo pull down to CollectController?
	 */
	protected final Clct getCompleteCollectable(Clct clct) throws CommonBusinessException {
		final ISearchStrategy<Clct> ss = getSearchStrategy();
		final Clct result = ss.isCollectableComplete(clct) ? clct : this.readCollectable(clct);
		assert ss.isCollectableComplete(result);
		assert !ss.isCollectableComplete(clct) || result == clct;
		return result;
	}

	/**
	 * runs the controller. If there is a search panel for this entity, starts with the search panel. Otherwise,
	 * starts with the result panel.
	 */
	public void run() throws CommonBusinessException {
		this.run(this.isSearchPanelAvailable());
	}

	/**
	 * runs this controller, starting with the search panel or with the result panel, depending on the parameter.
	 * @param bStartWithSearchPanel
	 */
	protected void run(boolean bStartWithSearchPanel) throws CommonBusinessException {
		if (bStartWithSearchPanel) {
			this.runSearch();
		}
		else {
			this.runViewAll();
		}
	}

	/**
	 * writes the current collectable id to the preferences. The collectable id must be serializable.
	 * @param prefs
	 * @throws PreferencesException
	 */
	protected void writeSerializableCurrentCollectableIdToPreferences(RestorePreferences rp) {
		final Object oId = this.getSelectedCollectableId();
		LOG.debug("writeSerializableCurrentCollectableIdToPreferences: oId = " + oId);
		if ((oId != null) && !(oId instanceof Serializable)) {
			throw new NuclosFatalException("The CollectableId is not serializable");//Die CollectableId ist nicht serialisierbar.
		}
		rp.objectId = new Long(((Integer)oId).longValue());
	}

	@Override
	protected void handleSaveException(CommonBusinessException ex, String sMessage1) throws CommonBusinessException {
		if (ex instanceof CollectableValidationException) {
			handleCollectableValidationException((CollectableValidationException) ex, sMessage1);
		} else {
			try {
				throw ex;
			}
			catch (BadGenericObjectException ex2) {
				Errors.getInstance().showExceptionDialog(this.getTab(), null, ex2);
			}
			catch (NuclosUpdateException ex2) {
				/** @todo this is a workaround. @see GenericObjectDelegate.update */
				Errors.getInstance().showExceptionDialog(this.getTab(), ex2);
			}
			catch (NuclosBusinessRuleException ex2) {
				Errors.getInstance().showExceptionDialog(this.getTab(),
						getSpringLocaleDelegate().getMessage("NuclosCollectController.1","{0}, da das Speichern eine Gesch\u00e4ftsregel verletzen w\u00fcrde.", sMessage1), ex2);
			}
		}
	}

	/**
	 * @todo refactor viewAll()!
	 *
	 * @deprecated Move to ResultController hierarchy.
	 */
	@Override
	protected void viewAll() throws CommonBusinessException {
		getResultController().getSearchResultStrategy().refreshResult();
	}

	/**
	 * translates Boolean fields by mapping <code>null</code> to <code>false</code> as there are no nullable
	 * booleans in Nucleus.
	 * @param clctCurrent
	 * @param clcteCurrent
	 * @precondition clctCurrent != null
	 * @precondition isCollectableComplete(clctCurrent)
	 * @precondition clcteCurrent != null
	 */
	@Override
	protected void prepareCollectableForSaving(Clct clctCurrent, CollectableEntity clcteCurrent) {
		if (clctCurrent == null) {
			throw new NullArgumentException("clctCurrent");
		}
		if (!getSearchStrategy().isCollectableComplete(clctCurrent)) {
			throw new IllegalArgumentException("clctCurrent");
		}
		if (clcteCurrent == null) {
			throw new NullArgumentException("clcteCurrent");
		}
		super.prepareCollectableForSaving(clctCurrent, clcteCurrent);

		Utils.prepareCollectableForSaving(clctCurrent, clcteCurrent);
	}
	
	private static class MyLayoutMLButtonActionListener<Clct extends Collectable> extends LayoutMLButtonActionListener {
		
		private WeakReference<CollectController<Clct>> refCc;
		
		private MyLayoutMLButtonActionListener(CollectController<Clct> cc) {
			this.refCc = new WeakReference<CollectController<Clct>>(cc);
		}
		
		@Override
		public void actionPerformed(ActionEvent ev) {
			final CollectController<Clct> cc = refCc.get();
			if (cc == null) {
				return;
			}
			
			final String sActionCommand = ev.getActionCommand();
			//NUCLOSINT-743 State and Rule Button Action
			String targetState = "_targetState=";
			String ruletoexecute = "_ruletoexecute=";
			String generatortoexecute = "_generatortoexecute=";
			try {
				if (sActionCommand.contains(STATIC_BUTTON.STATE_CHANGE_ACTION + targetState)) {
					Properties stateProperties = new Properties();
					String state = sActionCommand.substring(sActionCommand.indexOf(targetState) + targetState.length(), sActionCommand.length());
					stateProperties.put("targetState", state);
					((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.STATE_CHANGE_ACTION).newInstance()).run(cc, stateProperties);
				} else if (sActionCommand.contains(STATIC_BUTTON.EXECUTE_RULE_ACTION + ruletoexecute)) {
					Properties ruleProperties = new Properties();
					String ruleId = sActionCommand.substring(sActionCommand.indexOf(ruletoexecute) + ruletoexecute.length(), sActionCommand.length());
					ruleProperties.put("ruletoexecute", ruleId);
					((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.EXECUTE_RULE_ACTION).newInstance()).run(cc, ruleProperties);
				} else if (sActionCommand.contains(STATIC_BUTTON.GENERATOR_ACTION + generatortoexecute)) {
					Properties generatorProperties = new Properties();
					String generatorId = sActionCommand.substring(sActionCommand.indexOf(generatortoexecute) + generatortoexecute.length(), sActionCommand.length());
					generatorProperties.put("generatortoexecute", generatorId);
					((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.GENERATOR_ACTION).newInstance()).run(cc, generatorProperties);
				} else {
					((CollectActionAdapter<Clct>) Class.forName(sActionCommand).newInstance()).run(cc, new Properties());
				}

			}
			catch (InstantiationException ex) {
				throw new CommonFatalException(ex);
			}
			catch (IllegalAccessException ex) {
				throw new CommonFatalException(ex);
			}
			catch (ClassNotFoundException ex) {
				throw new CommonFatalException(ex);
			}
		}
		
		@Override
		public boolean enableParentComponent(String sActionCommand) {
			if (sActionCommand == null)
				return false;
			
			final CollectController<Clct> cc = refCc.get();
			if (cc == null) {
				return false;
			}
			
			boolean result = false;
			//NUCLOSINT-743 State and Rule Button Action
			String targetState = "_targetState=";
			String ruletoexecute = "_ruletoexecute=";
			String generatortoexecute = "_generatortoexecute=";
			try {
				if (sActionCommand.contains(STATIC_BUTTON.STATE_CHANGE_ACTION + targetState)) {
					Properties stateProperties = new Properties();
					String state = sActionCommand.substring(sActionCommand.indexOf(targetState) + targetState.length(), sActionCommand.length());
					stateProperties.put("targetState", state);
					result = ((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.STATE_CHANGE_ACTION).newInstance()).isRunnable(cc, stateProperties);
				} else if (sActionCommand.contains(STATIC_BUTTON.EXECUTE_RULE_ACTION + ruletoexecute)) {
					Properties ruleProperties = new Properties();
					String ruleId = sActionCommand.substring(sActionCommand.indexOf(ruletoexecute) + ruletoexecute.length(), sActionCommand.length());
					ruleProperties.put("ruletoexecute", ruleId);
					result = ((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.EXECUTE_RULE_ACTION).newInstance()).isRunnable(cc, ruleProperties);
				} else if (sActionCommand.contains(STATIC_BUTTON.GENERATOR_ACTION + generatortoexecute)) {
					Properties generatorProperties = new Properties();
					String generatorId = sActionCommand.substring(sActionCommand.indexOf(generatortoexecute) + generatortoexecute.length(), sActionCommand.length());
					generatorProperties.put("generatortoexecute", generatorId);
					result = ((CollectActionAdapter<Clct>) Class.forName(STATIC_BUTTON.GENERATOR_ACTION).newInstance()).isRunnable(cc, generatorProperties);
				} else {
					result = ((CollectActionAdapter<Clct>) Class.forName(sActionCommand).newInstance()).isRunnable(cc, new Properties());
				}
			}
			catch (InstantiationException ex) {
				throw new CommonFatalException(ex);
			}
			catch (IllegalAccessException ex) {
				throw new CommonFatalException(ex);
			}
			catch (ClassNotFoundException ex) {
				throw new CommonFatalException(ex);
			}			
			return result;
		}
	}

	/**
	 * @return <code>ActionListener</code> defining actions for buttons in the LayoutML definition. May be <code>null</code>.
	 */
	protected LayoutMLButtonActionListener getLayoutMLButtonsActionListener() {
		if (alLayoutMLButtons == null) {
			alLayoutMLButtons = new MyLayoutMLButtonActionListener<Clct>(NuclosCollectController.this);
		}
		return alLayoutMLButtons;
	}


	protected void setupSearchToolBar() {

		final Action actSaveFilter
			= new CommonAbstractAction(getSpringLocaleDelegate().getMessage("NuclosCollectController.9","Filter speichern"),
						Icons.getInstance().getIconSave16(),
						getSpringLocaleDelegate().getMessage("NuclosCollectController.5","Eingestelltes Suchkriterium als Filter speichern")) {
			@Override
            public void actionPerformed(ActionEvent ev) {
				cmdSaveFilter();
			}
		};

		final Action actRemoveFilter
			= new CommonAbstractAction(getSpringLocaleDelegate().getMessage("NuclosCollectController.8","Filter l\u00f6schen"),
						Icons.getInstance().getIconDelete16(),
						getSpringLocaleDelegate().getMessage("NuclosCollectController.2","Ausgew\u00e4hlten Filter l\u00f6schen")) {
			@Override
            public void actionPerformed(ActionEvent ev) {
				cmdRemoveFilter();
			}
		};

		//result.add(Box.createHorizontalStrut(5));

		CenteringPanel cpSearchFilter = new CenteringPanel(getSearchPanel().cmbbxSearchFilter) {

			@Override
			public Dimension getMinimumSize() {
				return this.getCenteredComponent().getMinimumSize();
			}

			@Override
			public Dimension getMaximumSize() {
				return this.getCenteredComponent().getPreferredSize();
			}

		};
		cpSearchFilter.setOpaque(false);
		BlackLabel bl = new BlackLabel(cpSearchFilter, getSpringLocaleDelegate().getMessage("CollectController.Search.Filter","Filter"));
		bl.setName("blChooseFilter");
		this.getSearchPanel().addToolBarComponent(bl);

		//result.add(cpSearchFilter);
		//result.add(Box.createHorizontalStrut(2));

		this.getSearchPanel().addPopupExtraSeparator();
		JMenuItem btnSaveFilter = this.getSearchPanel().addPopupExtraMenuItem(new JMenuItem(actSaveFilter));
		JMenuItem btnRemoveFilter = this.getSearchPanel().addPopupExtraMenuItem(new JMenuItem(actRemoveFilter));
		//final JButton btnSaveFilter = result.add(actSaveFilter);
		//final JButton btnRemoveFilter = result.add(actRemoveFilter);

		// disable the remove filter action initially:
		actRemoveFilter.setEnabled(false);

		btnSaveFilter.setName("btnSaveFilter");
		btnRemoveFilter.setName("btnRemoveFilter");
		getSearchPanel().cmbbxSearchFilter.setName("cmbbxSearchFilter");
		getSearchPanel().cmbbxSearchFilter.setToolTipText(getSpringLocaleDelegate().getMessage(
				"NuclosCollectController.15","W\u00e4hlen Sie hier einen Suchfilter aus"));
		getSearchPanel().cmbbxSearchFilter.addActionListener(this.alSearchFilter);

		// set tool tips dynamically:
		getSearchPanel().cmbbxSearchFilter.setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList lst, Object oValue, int index, boolean bSelected,
					boolean bCellHasFocus) {
				final JComponent result = (JComponent) super.getListCellRendererComponent(lst, oValue, index, bSelected,
						bCellHasFocus);
				String sToolTip = null;
				if (oValue != null) {
					final SearchFilter filter = (SearchFilter) oValue;

					if (result instanceof JLabel && !StringUtils.isNullOrEmpty(filter.getLabelResourceId())) {
						((JLabel) result).setText(getSpringLocaleDelegate().getTextFallback(
								filter.getLabelResourceId(), filter.getName()));
					}

					if (!StringUtils.isNullOrEmpty(filter.getDescriptionResourceId())) {
						sToolTip = getSpringLocaleDelegate().getTextFallback(
								filter.getDescriptionResourceId(), filter.getDescriptionResourceId());
					}
					else {
						sToolTip = filter.getDescription();
					}

					if (filter.getOwner() != null && !(filter.getOwner().equals(Main.getInstance().getMainController().getUserName()))) {
						sToolTip = sToolTip + " (" + filter.getOwner() + ")";
					}

					result.setToolTipText(sToolTip);
				}
				return result;
			}
		});

		// set the tool tip for the combobox also, as the tool tip for the renderer seems to be
		// taken in dropped down items only:
		getSearchPanel().cmbbxSearchFilter.addItemListener(new ItemListener() {
			@Override
            public void itemStateChanged(ItemEvent ev) {
				final boolean bSelected = (ev.getStateChange() == ItemEvent.SELECTED);
				boolean bRegularFilterSelected = bSelected;

				String sToolTip = null;
				if (bSelected) {
					final SearchFilter filter = (SearchFilter) ev.getItem();
					assert filter != null;
					sToolTip = filter.getDescription();
					LOG.debug("Filter selected: " + filter.getName());
					bRegularFilterSelected = !filter.isDefaultFilter();
				}
				getSearchPanel().cmbbxSearchFilter.setToolTipText(sToolTip);

				// enable/disable remove filter action - the empty filter cannot be removed:
				actRemoveFilter.setEnabled(bRegularFilterSelected);
			}
		});

		//return result;
	}

	/**
	 * Command: save filter
	 */
	protected void cmdSaveFilter() {
		UIUtils.runCommand(this.getTab(), new CommonRunnable() {
			@Override
            public void run() throws CommonValidationException {
				if (!stopEditingInSearch()) {
					throw new CommonValidationException(getSpringLocaleDelegate().getMessage(
							"NuclosCollectController.4","Die eingegebene Suchbedingung ist ung\u00fcltig bzw. unvollst\u00e4ndig."));
				}
				try {
					final SearchFilter filterSelected = getSelectedSearchFilter();
					SearchFilter filterCurrent = getCurrentSearchFilterFromSearchPanel();
					final DefaultComboBoxModel model = (DefaultComboBoxModel) getSearchFilterComboBox().getModel();
					final SaveFilterController.Command cmd = new SaveFilterController(getTab(), getSearchFilters()).runSave(filterSelected, filterCurrent);
					switch (cmd) {
						case None:
							// do nothing
							break;

						case Overwrite:
							selectDefaultFilter();
							getSearchFilterComboBox().removeItem(filterSelected);
							// note that there is no "break" here!

						case New:
							filterCurrent = SearchFilterCache.getInstance().getSearchFilter(filterCurrent.getName(), filterCurrent.getOwner());
							model.addElement(filterCurrent);
							UIUtils.ensureMinimumSize(getTab());
							getSearchFilterComboBox().setSelectedItem(filterCurrent);
							break;

						default:
							assert false;
					}
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(getTab(), ex);
				}
			}
		});
	}


	/**
	 * command: remove filter
	 */
	protected void cmdRemoveFilter() {
		final SearchFilter filter = getSelectedSearchFilter();
		if (filter != null) {
			if (JOptionPane.showConfirmDialog(this.getTab(), getSpringLocaleDelegate().getMessage(
					"NuclosCollectController.16","Wollen Sie den Filter \"{0}\" wirklich l\u00f6schen?", filter.getName()), "Filter l\u00f6schen", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				UIUtils.runCommand(this.getTab(), new CommonRunnable() {
					@Override
                    public void run() throws NuclosBusinessException{
						getSearchFilters().remove(filter);
						refreshFilterView();
					}
				});
			}
		}
	}



	/**
	 * @return empty search filter to be used as default
	 */
	private SearchFilter newDefaultFilter() {
		final SearchFilter result = getSearchFilters().newDefaultFilter();

		return result;
	}

	/**
	 * refreshes the combobox containing the filters
	 */
	protected void refreshFilterView() {
		// remember selected filter, if any:
		final SearchFilter filterSelected = getSelectedSearchFilter();

		final List<? extends SearchFilter> lstFilters;
		try {
			lstFilters = getSearchFilters().getAll();
		}
		catch (PreferencesException ex) {
			throw new NuclosFatalException(ex);
		}
		final DefaultComboBoxModel model = (DefaultComboBoxModel) this.getSearchFilterComboBox().getModel();

		// don't fire changed events here:
		this.getSearchFilterComboBox().removeActionListener(this.alSearchFilter);
		try {
			model.removeAllElements();
			model.addElement(newDefaultFilter());
			for (SearchFilter filter : lstFilters) {
				model.addElement(filter);
			}

			// try to restore the previously selected filter, if any:
			if (filterSelected != null) {
				setSelectedSearchFilter(filterSelected);
			}
		}
		finally {
			getSearchFilterComboBox().addActionListener(this.alSearchFilter);
		}

		// perform alSearchFilter's action manually:
		this.alSearchFilter.actionPerformed(null);
	}

	protected SearchFilters getSearchFilters() {
		return SearchFilters.forEntity(this.getEntityName());
	}

	protected SearchFilter getCurrentSearchFilterFromSearchPanel() throws CommonBusinessException  {
		final EntitySearchFilter result = new EntitySearchFilter();

		result.setSearchCondition(this.getCollectableSearchConditionFromSearchPanel(true));
		result.setEntityName(this.getEntityName());

		/** @todo set sorting column names */

		return result;
	}

	public JComboBox getSearchFilterComboBox() {
		return this.getSearchPanel().cmbbxSearchFilter;
	}

	/**
	 * @return the search filter (if any) selected in the combo box
	 */
	public final SearchFilter getSelectedSearchFilter() {
		return (SearchFilter) getSearchFilterComboBox().getSelectedItem();
	}

	/**
	 * selects the given search filter in the search filters combo box.
	 * @param filter
	 * @postcondition LangUtils.equals(this.getSelectedSearchFilter(), filter)
	 */
	public final void setSelectedSearchFilter(SearchFilter filter) {
		this.getSearchFilterComboBox().setSelectedItem(filter);
	}

	/**
	 * selects the default filter (the first entry in the combobox).
	 */
	public void selectDefaultFilter() {
		this.getSearchFilterComboBox().setSelectedIndex(0);
	}

	protected void cmdSetCollectableSearchConditionAccordingToFilter() {
		assert this.isSearchPanelAvailable();

		UIUtils.runShortCommand(this.getTab(), new CommonRunnable() {
			@Override
            public void run() {
				try {
					final SearchFilter filterSelected = getSelectedSearchFilter();
					// filterSelected may be null here as an ActionEvent is triggered by Swing even after
					// DefaultComboBoxModel.removeAllElements() is called.
					setCollectableSearchConditionInSearchPanel(filterSelected == null ? null : filterSelected.getSearchCondition());
					if (filterSelected == null || filterSelected.isDefaultFilter()) {
						clearSearchCondition();
					}
					if (filterSelected != null && filterSelected.getSearchDeleted() != null) {
						setSearchDeleted(filterSelected.getSearchDeleted());
					}

				}
				catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(getTab(), getSpringLocaleDelegate().getMessage(
							"NuclosCollectController.12","Suchbedingung kann nicht in der Suchmaske dargestellt werden."), ex);
				}
			}
		});
	}

	public void setSearchDeleted(Integer iSearchDeleted) {
		//override in GenericObjectCollectController
	}

	public void cmdExecuteRuleByUser(MainFrameTab iFrame, final String sEntityName, Clct clct) {
		if (this.getCollectState().getInnerState() != CollectStateConstants.DETAILSMODE_VIEW) {
			throw new IllegalStateException("view mode");
		}
		if (clct != null) {
			final SelectController controller = new SelectController(iFrame);
			UIUtils.runCommand(this.getTab(), new CommonRunnable() {
				@Override
                public void run() throws CommonBusinessException {
					final Collection<RuleVO> collRules = getUserRules();
					
					final ChoiceList<RuleVO> ro = new ChoiceList<RuleVO>();
					ro.set(collRules,
							new Comparator<RuleVO>() {
								@Override
								public int compare(RuleVO o1, RuleVO o2) {
									return o1.getRule().compareTo(o2.getRule());
								}
							});
					controller.setModel(ro);
					if (controller.run(getSpringLocaleDelegate().getMessage("NuclosCollectController.11","Regeln ausf\u00fchren"))) {
						//execute the selected Rules
						final List<RuleVO> lstRuleToExecute = CollectionUtils.typecheck(controller.getSelectedObjects(), RuleVO.class);
						if (lstRuleToExecute != null && !lstRuleToExecute.isEmpty()) {
							try {
								executeBusinessRules(lstRuleToExecute, controller.getMySelectObjectsPanel().getSaveAfterExec().isSelected());
							} catch (CommonBusinessException ex) {
								controller.setBlnExceptionOnWork(true);
								if (!handleSpecialException(ex))
									throw ex;
							}
						}
					}
				}
			});
			//refresh the current object if it has been saved
			if (controller.getMySelectObjectsPanel().getSaveAfterExec().isSelected() && !controller.getSelectedObjects().isEmpty() && !controller.isBlnExceptionOnWork()) {
				this.cmdRefreshCurrentCollectable();
			}
		}
	}

	public void executeBusinessRules(List<RuleVO> lstRuleVO, boolean bSaveAfterRuleExecution) throws CommonBusinessException {
		//override in MasterDataCollectController and GenericObjectCollectController
	}

	/**
	 * is entity of this controller transferable, that means that collectables
	 * of this entity can be exported and imported.
	 *
	 * TODO: Make this protected again.
	 */
	@Override
	public boolean isTransferable() {
		Boolean bTransferable = false;
		String sEntityName = getEntityName();

		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_XML_EXPORT_IMPORT)) {
			if (Modules.getInstance().isModuleEntity(sEntityName)) {
				Object obj = Modules.getInstance().getModuleByEntityName(sEntityName).getField("importexport");
				bTransferable = (obj != null) ? (Boolean)obj : false;
			}
			else {
				bTransferable = MasterDataDelegate.getInstance().getMetaData(sEntityName).getIsImportExport();
			}
		}
		return bTransferable;
	}

	@Configurable
	private static class MySelectObjectsPanel<T> extends DefaultSelectObjectsPanel<T> {

		private final JCheckBox cbxSaveAfterRuleExecution = new JCheckBox();

		MySelectObjectsPanel() {
		}

		@PostConstruct
		@Override
		protected void init() {
			super.init();
			cbxSaveAfterRuleExecution.setText(SpringLocaleDelegate.getInstance().getMessage(
					"NuclosCollectController.10","Objekt nach Regelausf\u00fchrung speichern"));
			this.pnlMain.add(cbxSaveAfterRuleExecution, new GridBagConstraints(2, 2, 1, 1, 1.0, 1.0
					, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		JCheckBox getSaveAfterExec() {
			return cbxSaveAfterRuleExecution;
		}
	}

	/**
	 *  helper class for cmdExecuteRulesByUser
	 */
	private static class SelectController extends SelectObjectsController<RuleVO> {

		private boolean blnExceptionOnWork;

		public SelectController(Component parent) {
			super(parent, new MySelectObjectsPanel());
			final MySelectObjectsPanel panel = (MySelectObjectsPanel) getPanel();
			panel.btnUp.setEnabled(true);
			panel.btnDown.setEnabled(true);
			panel.btnUp.setVisible(true);
			panel.btnDown.setVisible(true);
			blnExceptionOnWork = false;
		}



		public boolean isBlnExceptionOnWork() {
			return blnExceptionOnWork;
		}



		public void setBlnExceptionOnWork(boolean blnExceptionOnWork) {
			this.blnExceptionOnWork = blnExceptionOnWork;
		}



		MySelectObjectsPanel getMySelectObjectsPanel() {
			return (MySelectObjectsPanel) getPanel();
		}

	}

	@Override
	protected boolean isDetailsModeViewLoadingWithoutDependants() {
		return false;
	}

	/**
	 * @return the user preferences node for this
	 */
	@Override
	public Preferences getPreferences() {
		return super.getPreferences();
	}

	@Override
	public EntityPreferences getEntityPreferences() {
		WorkspaceUtils.validatePreferences(super.getEntityPreferences());
		return super.getEntityPreferences();
	}

	@Override
	protected Clct findCollectableById(String sEntity, Object oId)
			throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Clct findCollectableByIdWithoutDependants(String sEntity,
			Object oId) throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Clct updateCollectable(Clct clct, Object oAdditionalData)
			throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Clct insertCollectable(Clct clctNew)
			throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void deleteCollectable(Clct clct) throws CommonBusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getEntityLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Clct newCollectable() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<RuleVO> getUserRules() {
		return Collections.EMPTY_LIST;
	}

	@Override
	protected final void initTab() {
		super.initTab();
		getTab().setTabStoreController(new NuclosCollectController.NuclosCollectTabStoreController(this));
	}

	@Override
	public ImageIcon getIcon() {
		return Main.getInstance().getMainFrame().getEntityIcon(getEntityName());
	}
}	// class NuclosCollectController

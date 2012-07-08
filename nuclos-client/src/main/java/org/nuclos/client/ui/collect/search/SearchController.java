//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.search;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CommonController;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.collect.searcheditor.SearchEditorController;
import org.nuclos.client.ui.collect.searcheditor.SearchEditorPanel;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.InvalidCollectableSearchConditionException;
import org.nuclos.common.collect.collectable.searchcondition.ToHumanReadablePresentationVisitor;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Controller for the Search panel.
 */
public class SearchController<Clct extends Collectable> extends CommonController<Clct> {
	
	private static final Logger LOG = Logger.getLogger(SearchController.class);
	
	//
	
	/**
	 * Action for showing/hiding the search editor.
	 */
	private class ToggleSearchEditorAction extends CommonAbstractAction {

		/**
		 * avoids recursion
		 */
		private boolean bLocked;

		ToggleSearchEditorAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("CollectController.28","Sucheditor"), 
					null, SpringLocaleDelegate.getInstance().getMessage("CollectController.29","Sucheditor anzeigen/verbergen"));
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			if (!bLocked) {
				try {
					final CollectController<Clct> cc = getCollectController();
					bLocked = true;
					final SearchPanel pnlSearch = cc.getSearchPanel();
					boolean bSelected = pnlSearch.btnSearchEditor.isSelected();
					assert bSelected != cc.getSearchPanel().isSearchEditorVisible();
					try {
						if (bSelected) {
							// search fields -> SearchEditor:
							cc.stopEditingInSearch();
							/** TODO It would be better to call getCollectableSearchConditionFromSearchFields(true) here
							 * so bad or inconsistent view is detected. In that case, the exception must be caught here. */
							cc.getSearchPanel().getSearchEditorPanel().setSortedSearchCondition(cc.getCollectableSearchConditionFromSearchFields(false));

							actNewWithSearchValues.setEnabled(false);
						}
						else {
							final CollectableSearchCondition cond = cc.getSearchPanel().getSearchEditorPanel().getSearchCondition();
							if (cond != null && !cond.isSyntacticallyCorrect()) {
								bSelected = true;
								throw new InvalidCollectableSearchConditionException(getSpringLocaleDelegate().getMessage(
										"CollectController.17","Die Suchbedingung ist unvollst\u00e4ndig."));
							}
							else if (!cc.getSearchPanel().canDisplayConditionInFields(cond)) {
								bSelected = true;
								throw new InvalidCollectableSearchConditionException(getSpringLocaleDelegate().getMessage(
										"CollectController.18","Eine zusammengesetzte Suchbedingung kann nur im Sucheditor, nicht in der Suchmaske dargestellt werden."));
							}
							else {
								// SearchEditor -> search fields:
								cc.setSearchFieldsAccordingToSearchCondition(cond, true);
							}
						}
					}
					catch (InvalidCollectableSearchConditionException ex) {
						// undo button press:
						bSelected = !bSelected;
						Errors.getInstance().showExceptionDialog(cc.getTab(), ex);
					}
					catch (Exception ex) {
						// TODO don't catch and wrap RuntimeExceptions here!
						final String sMessage = getSpringLocaleDelegate().getMessage(
								"CollectController.16","Diese Suchbedingung kann nur im Sucheditor, nicht in der Suchmaske dargestellt werden.") + "\n" + ex.getMessage();
						// undo button press:
						bSelected = !bSelected;
						Errors.getInstance().showExceptionDialog(cc.getTab(), new InvalidCollectableSearchConditionException(sMessage, ex));
					}
					cc.getSearchPanel().setSearchEditorVisible(bSelected);
				}
				finally {
					bLocked = false;
				}
			}
			assert !bLocked;
		}
	}
	
	/**
	 * action: New with search values
	 */
	private final Action actNewWithSearchValues = new CommonAbstractAction("Neu", Icons.getInstance().getIconNewWithSearchValues16(),
			getSpringLocaleDelegate().getMessage(
					"CollectController.31","\u00dcbernahme der Suchkriterien in den neuen Datensatz")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			final CollectController<Clct> cc = getCollectController();
			cc.askAndSaveIfNecessary(new ResultListener<Boolean>() {
				@Override
				public void done(Boolean result) {
					if (Boolean.TRUE.equals(result)) {
						cc.cmdEnterNewModeWithSearchValues();
					}
				}
			});
		}
	};

	/**
	 * action: Search
	 * 
	 * TODO: Move to ResultController???
	 */
	private final Action actSearch = new CommonAbstractAction("Suchen", Icons.getInstance().getIconFind16(), 
			getSpringLocaleDelegate().getMessage("CollectController.30","Suche starten")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			final CollectController<Clct> cc = getCollectController();
			if(cc.getCollectPanel().getTabbedPaneSelectedIndex() == CollectPanel.TAB_SEARCH)
				cc.getResultController().getSearchResultStrategy().cmdSearch();
		}
	};

	/**
	 * action: Clear Search Condition
	 */
	private final AbstractAction actClearSearchCondition = new CommonAbstractAction(Icons.getInstance().getIconClearSearch16(),
			getSpringLocaleDelegate().getMessage("CollectController.27","Suchbedingung leeren")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			getCollectController().cmdClearSearchCondition();
		}
	};

	/**
	 * Listener for searchcondition changes.
	 * 
	 * Cannot be final because set to null in close(). (tp)
	 */
	private CollectableComponentModelListener ccmlistener = new CollectableComponentModelAdapter() {
		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			final CollectController<Clct> cc = getCollectController();
			assert cc.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_SEARCH;

			// Note that we want to call "searchChanged()" on every change, not only valid changes:
			cc.searchChanged(ev.getSearchComponentModel());
		}
	};

	public SearchController(CollectController<Clct> cc) {
		super(cc);
	}
	
	public final Action getNewWithSearchValuesAction() {
		return actNewWithSearchValues;
	}
	
	public final Action getSearchAction() {
		return actSearch;
	}

	/**
	 * TODO: Make this private again.
	 */
	public void setupSearchPanel() {
		final CollectController<Clct> cc = getCollectController();
		final SearchPanel pnlSearch = cc.getSearchPanel();
		// action: Search
		pnlSearch.btnSearch.setAction(this.actSearch);

		// action: Clear Search Condition
		pnlSearch.btnClearSearchCondition.setAction(this.actClearSearchCondition);

		// action: New
		pnlSearch.btnNew.setAction(cc.getNewAction());

		// action: New with search values
		pnlSearch.btnNewWithSearchValues.setAction(cc.getNewWithSearchValuesAction());
		pnlSearch.btnNewWithSearchValues.getAction().setEnabled(false);

		final Action actToggleSearchEditor = new ToggleSearchEditorAction();

		pnlSearch.btnSearchEditor.setAction(actToggleSearchEditor);
		// In order to enable the search editor, a CollectableFieldsProviderFactory needs to be defined.
		actToggleSearchEditor.setEnabled(cc.getCollectableFieldsProviderFactoryForSearchEditor() != null);

		UIUtils.readSplitPaneStateFromPrefs(cc.getPreferences(), pnlSearch);
	}

	@Override
	protected boolean isSearchPanel() {
		return true;
	}

	/**
	 * TODO: Make protected again.
	 */
	@Override
	public Collection<SearchComponentModel> getCollectableComponentModels() {
		final CollectController<Clct> cc = getCollectController();
		return cc.getSearchPanel().getEditModel().getCollectableComponentModels();
	}

	@Override
	protected CollectableComponentModelListener getCollectableComponentModelListener() {
		return this.ccmlistener;
	}

	@Override
	protected void addAdditionalChangeListeners() {
		getCollectController().addAdditionalChangeListenersForSearch();
	}

	@Override
	protected void removeAdditionalChangeListeners() {
		getCollectController().removeAdditionalChangeListenersForSearch();
	}

	/**
	 * TODO: Make this private again.
	 */
	public void setupSearchEditor() {
		final CollectController<Clct> cc = getCollectController();
		final SearchEditorPanel pnlSearchEditor = cc.getSearchPanel().getSearchEditorPanel();
		new SearchEditorController(cc.getTab(), pnlSearchEditor, cc.getCollectableEntity(),
				cc.getCollectableFieldsProviderFactoryForSearchEditor(),
				cc.getAdditionalSearchFields()
		);

		pnlSearchEditor.getTreeModel().addTreeModelListener(new TreeModelListener() {
			@Override
            public void treeNodesChanged(TreeModelEvent ev) {
				changed(ev);
			}

			@Override
            public void treeNodesInserted(TreeModelEvent ev) {
				changed(ev);
			}

			@Override
            public void treeNodesRemoved(TreeModelEvent ev) {
				changed(ev);
			}

			@Override
            public void treeStructureChanged(TreeModelEvent ev) {
				changed(ev);
			}

			private void changed(TreeModelEvent ev) {
				this.adjustSearchEditorButton();
				getCollectController().searchChanged(ev.getPath());
			}

			private void adjustSearchEditorButton() {
				final CollectController<Clct> cc = getCollectController();
				cc.getSearchPanel().btnSearchEditor.setEnabled(cc.getSearchPanel().canDisplayConditionInFields(
						cc.getSearchPanel().getSearchEditorPanel().getSearchCondition()));
			}
		});
	}

	/**
	 * releases the resources (esp. listeners) for this controller.
	 */
	@Override
	public void close() {
		if (!isClosed()) {
			final CollectController<Clct> cc = getCollectController();
			final SearchPanel pnlSearch = getSearchPanel();
			pnlSearch.btnSearch.setAction(null);
			pnlSearch.btnSearchEditor.setAction(null);
			pnlSearch.btnClearSearchCondition.setAction(null);
			pnlSearch.btnNew.setAction(null);
	
			UIUtils.writeSplitPaneStateToPrefs(cc.getPreferences(), getSearchPanel());
			ccmlistener = null;
			
			super.close();
		}
	}

	/**
	 * displays the current search condition in the Search panel's status bar.
	 * 
	 * TODO: Make this private again.
	 */
	public void displayCurrentSearchConditionInSearchPanelStatusBar() {
		String sSearchCondition;
		String addStatusMsg = "";
		final CollectController<Clct> cc = getCollectController();
		try {
			final CollectableSearchCondition searchcond = cc.getCollectableSearchConditionToDisplay();
			if (searchcond == null) {
				sSearchCondition = getSpringLocaleDelegate().getMessage(
						"CollectController.2","<Alle> (Keine Einschr\u00e4nkung)");
			} else {
				sSearchCondition = searchcond.accept(new ToHumanReadablePresentationVisitor());
			}
			if(displayMixedSearchCondition()){
				addStatusMsg = getSpringLocaleDelegate().getMessage("CollectController.22","Kombinierte ");
			}
		}
		catch (CollectableFieldFormatException ex) {
			sSearchCondition = getSpringLocaleDelegate().getMessage("CollectController.3","<Ung\u00fcltig>");
		}
		getSearchPanel().setStatusBarText(addStatusMsg + getSpringLocaleDelegate().getMessage(
				"CollectController.26","Suchbedingung: ") + sSearchCondition);
	}

	private boolean displayMixedSearchCondition = false;

	public boolean displayMixedSearchCondition() {
		return displayMixedSearchCondition;
	}

	public void setDisplayMixedSearchCondition(boolean displayMixedSearchCondition) {
		this.displayMixedSearchCondition = displayMixedSearchCondition;
	}
	
	private final SearchPanel getSearchPanel() {
		return getCollectController().getSearchPanel();
	}

}	// class SearchController


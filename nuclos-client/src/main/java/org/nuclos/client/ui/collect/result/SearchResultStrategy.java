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
package org.nuclos.client.ui.collect.result;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import javax.swing.JScrollBar;
import javax.swing.JViewport;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.search.SearchWorker;
import org.nuclos.client.ui.message.MessageExchange;
import org.nuclos.client.ui.message.MessageExchange.MessageExchangeListener;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;

public class SearchResultStrategy<Clct extends Collectable> implements ISearchResultStrategy<Clct> {
	
	private static final Logger LOG = Logger.getLogger(SearchResultStrategy.class);

	private ResultController<Clct> rc;
	
	public SearchResultStrategy() {
	}
	
	public void setResultController(ResultController<Clct> rc) {
		this.rc = rc;
	}

	private final CollectController<Clct> getCollectController() {
		return rc.getCollectController();
	}

	// search stuff
	
	/**
	 * @deprecated Use multithreaded search for new applications.
	 */
	public void refreshResult() throws CommonBusinessException {
		getCollectController().getSearchStrategy().search(true);
	}
	
	/**
	 * Command: refresh search result.
	 * Repeats the current search.
	 */
	public final void cmdRefreshResult() {
		this.cmdSearch(true);
	}

	public final void cmdRefreshResult(List<Observer> lstObservers) {
		this.cmdObservableMultiThreadingSearch(lstObservers);
	}

	/**
	 * Command: search.
	 * Common implementation for cmdSearch() and cmdRefreshResult().
	 * @param bRefreshOnly Refresh only? (false: perform a new search)
	 */
	@SuppressWarnings("deprecation")
	private void cmdSearch(boolean bRefreshOnly) {
		final CollectController<Clct> cc = getCollectController();
		LOG.debug("START cmdSearch");
		// save search search terms for autocompletion
		saveSearchTerms();
		// TODO call getSearchWorker(bRefreshOnly)
		final SearchWorker<Clct> searchWorker = cc.getSearchStrategy().getSearchWorker();
		if (cc.isMultiThreadingEnabled() && (searchWorker != null)) {
			this.cmdSearchMultiThreaded(searchWorker, bRefreshOnly);
		}
		else {
			this.cmdSearchSingleThreaded(bRefreshOnly);
		}
		LOG.debug("FINISHED cmdSearch");
	}

	/**
	 * Command: search.
	 * Observable implementation for cmdSearch() and cmdRefreshResult().
	 * @param lstObservers "search finished" Observers
	 */
	private void cmdObservableMultiThreadingSearch(List<Observer> lstObservers) {
		LOG.debug("START cmdObservableMultiThreadingSearch");
		final SearchWorker<Clct> searchWorker = getCollectController().getSearchStrategy().getSearchWorker(lstObservers);
		if (searchWorker != null) {
			this.cmdSearchMultiThreaded(searchWorker, true);
		}
		LOG.debug("FINISHED cmdObservableMultiThreadingSearch");
	}

	/**
	 * Command: search.
	 * Performs a search, according to the current search condition, if any.
	 */
	public final void cmdSearch() {
		this.cmdSearch(false);
	}

	/**
	 * @param searchworker
	 * @precondition searchworker != null
	 */
	private void cmdSearchMultiThreaded(final SearchWorker<Clct> searchworker, final boolean bRefreshOnly) {
		final CollectController<Clct> cc = getCollectController();
		UIUtils.runShortCommand(cc.getFrame(), new CommonRunnable() {
			@Override
            public void run() throws CommonValidationException {
				if (searchworker == null) {
					throw new NullArgumentException("searchworker");
				}

				if (!cc.stopEditingInSearch()) {
					throw new CommonValidationException("Die eingegebene Suchbedingung ist ung\u00fcltig bzw. unvollst\u00e4ndig.");
				}
				else {
					// TODO remove - painting isn't necessary here:
					UIUtils.paintImmediately(cc.getSearchPanel().tfStatusBar);
					rc.writeSelectedFieldsAndWidthsToPreferences();
					final List<Clct> selected = cc.getSelectedCollectables();
					adjustVerticalScrollBarForSearch(bRefreshOnly);

					CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<Clct>(cc) {
						private volatile List<Clct> lstclctResult;

						@Override
						public void init() throws CommonBusinessException {
							super.init();

							searchworker.startSearch();
						}

						@Override
						public void work() throws CommonBusinessException {
							this.lstclctResult = searchworker.getResult();
						}

						@Override
						public void paint() throws CommonBusinessException {
							if (this.lstclctResult != null) {
								searchworker.finishSearch(this.lstclctResult);

								if (cc.isSearchPanelAvailable()) {
									// TODO On refresh, it's "counter intuitive" to leave result mode here.
									cc.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_SYNCHED);
								}
								if(selected != null && !selected.isEmpty()) {
									selected.clear();
								}
								cc.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);
							}

							super.paint();
							if(cc.getResultPanel() != null){
								cc.getResultPanel().requestFocusInWindow();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Command: search.
	 * Performs a search, according to the current search condition, if any.
	 * @param bRefreshOnly Refresh only? (false: perform a new search)
	 * @deprecated always search multi threaded
	 */
	@Deprecated
	private void cmdSearchSingleThreaded(final boolean bRefreshOnly) {
		final CollectController<Clct> cc = getCollectController();
		UIUtils.runCommand(cc.getFrame(), new Runnable() {
			@Override
            public void run() {
				try {
					if (!cc.stopEditingInSearch()) {
						throw new CommonValidationException("Die eingegebene Suchbedingung ist ung\u00fcltig bzw. unvollst\u00e4ndig.");
					}
					else {
						// update the status bar before performing the search:
						UIUtils.paintImmediately(cc.getSearchPanel().tfStatusBar);

						// Write the column widths to preferences, so they can be restored after searching is finished
						rc.writeSelectedFieldsAndWidthsToPreferences();
						adjustVerticalScrollBarForSearch(bRefreshOnly);
						cc.getSearchStrategy().search(bRefreshOnly);

						if (cc.isSearchPanelAvailable()) {
							// TODO On refresh, it's "counter intuitive" to leave result mode here.
							cc.setCollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_SYNCHED);
						}
						cc.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);

						// Searching is finished, the result columns have been replaced in the model, so retore the previous widths
						rc.setColumnWidths(cc.getResultTable());
						if(cc.getResultPanel() != null){
							cc.getResultPanel().requestFocusInWindow();
						}
					}
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(cc.getFrame(), ex);
				}
			}
		});
	}

	private void saveSearchTerms() {
		final CollectController<Clct> cc = getCollectController();
		try {
			CollectableSearchCondition cond = cc.getSearchStrategy().getCollectableSearchCondition();
			Map<String, CollectableField> m = SearchConditionUtils.getAtomicFieldsMap(cond);

			if(cond != null) {
				if (cond instanceof AtomicCollectableSearchCondition) {
	   			if (cond instanceof CollectableLikeCondition) {
	   				m.put(((CollectableLikeCondition) cond).getFieldName(), new CollectableValueField(((CollectableLikeCondition)cond).getLikeComparand()));
	   			}
	   		}

				for(String key : m.keySet()) {
					ArrayList<String> l = PreferencesUtils.getStringList(cc.getPreferences().node("fields"), key);
					CollectableField field = m.get(key);

					if(!field.isNull() && field.getValue() != null) {
						String s = field.getValue().toString();
						if(l.contains(s))
							l.remove(s);

						l.add(0, s);
					}
					while(l.size() > 10)
						l.remove(l.size()-1);

					PreferencesUtils.putStringList(cc.getPreferences().node("fields"), key, l);

					MessageExchange.send(
						new Pair<String, String>(cc.getEntityName(), key),
						MessageExchangeListener.ObjectType.TEXTFIELD,
						MessageExchangeListener.MessageType.REFRESH);
				}
			}
		}
		catch(CollectableFieldFormatException e1) {
		}
		catch(PreferencesException e) {
		}
	}

	private void adjustVerticalScrollBarForSearch(boolean bRefreshOnly) {
		final CollectController<Clct> cc = getCollectController();
		final JViewport viewport = cc.getResultPanel().getResultTableScrollPane().getViewport();
		if (bRefreshOnly) {
			final Rectangle rect = cc.getResultTable().getCellRect(0, 0, true);
			final Rectangle viewRect = viewport.getViewRect();
			// There seem to be different opinions about what scrollRectToVisible has to do at SUN and everywhere else...
			rect.setLocation(viewRect.x, viewRect.y);//rect.x - viewRect.x, rect.y - viewRect.y);
			viewport.scrollRectToVisible(rect);
		}
		else {
			Point viewPosition = viewport.getViewPosition();
			viewport.setViewPosition(new Point(viewPosition.x, 0));
		}
		final JScrollBar scrlbarVertical = cc.getResultPanel().getResultTableScrollPane().getVerticalScrollBar();
		scrlbarVertical.setValue(scrlbarVertical.getMinimum());
	}
}

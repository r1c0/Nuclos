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
package org.nuclos.client.ui.collect;

import org.nuclos.client.ui.Errors;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.SpringLocaleDelegate;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;

/**
 * State model for collecting data. The state consists of an outer state that reflects the tab
 * we are currently in, and an inner state that defines the mode of interaction inside a specific outer state or tab.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @todo use CollectState here
 */

public final class CollectStateModel <Clct extends Collectable> implements CollectStateConstants {
	
	private static final Logger LOG = Logger.getLogger(CollectStateModel.class);

	/**
	 * the current search mode (inner state)
	 */
	private int iSearchMode = SEARCHMODE_UNDEFINED;

	/**
	 * the current result mode (inner state)
	 */
	private int iResultMode = RESULTMODE_UNDEFINED;

	/**
	 * the current details mode (inner state)
	 */
	private int iDetailsMode = DETAILSMODE_UNDEFINED;

	/**
	 * the currently displayed tab (outer state)
	 */
	private int iTab = OUTERSTATE_UNDEFINED;

	/**
	 * the corresponding <code>CollectPanel</code>
	 */
	private final CollectPanel<Clct> pnlCollect;

	private final CollectController<Clct> ctlCollect;

	private final transient List<CollectStateListener> lstCollectStateListeners = new LinkedList<CollectStateListener>();

	/**
	 * the currently edited Collectable, if any
	 */
	private Clct clctEdited;

	/**
	 * the tab change listener that is used to sync the outer state with the selected tab.
	 */
	private final ChangeListener tabChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent ev) {
			final int iTab = CollectStateModel.this.pnlCollect.getTabbedPaneSelectedIndex();
			try {
				// sync outer state, but don't adjust the UI (this would cause an endless recursion):
				CollectStateModel.this.setOuterState(iTab);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(ctlCollect.getTab(), ex);
				// back up to neutral mode - search mode:
				try {
					CollectStateModel.this.setCollectState(OUTERSTATE_SEARCH, SEARCHMODE_UNSYNCHED);
				}
				catch (Exception ex2) {
					// even this did not work - close the collect frame:
					Errors.getInstance().showExceptionDialog(ctlCollect.getTab(), ex2);
					ctlCollect.getTab().dispose();
				}
			}
		}
	};

	/**
	 * creates a new <code>CollectStateModel</code>.
	 * @param pnlCollect the corresponding <code>CollectPanel</code>
	 * @param ctlCollect the corresponding <code>CollectController</code>
	 */
	public CollectStateModel(CollectPanel<Clct> pnlCollect, CollectController<Clct> ctlCollect) {
		this.pnlCollect = pnlCollect;
		this.ctlCollect = ctlCollect;

		this.addTabChangeListener();
	}

	/**
	 * @return Are user changes pending (need to be saved)? This is always true in the
	 * <code>DETAILSMODE_EDIT</code>, <code>DETAILSMODE_MULTIEDIT</code> and <code>DETAILSMODE_NEW_CHANGED</code> modes.
	 */
	boolean changesArePending() {
		return (this.getTab() == OUTERSTATE_DETAILS) && CollectState.isDetailsModeChangesPending(this.getDetailsMode());
	}

	/**
	 * @return the outer state (the currently selected tab)
	 */
	public int getOuterState() {
		return this.getTab();
	}

	/**
	 * @return the current inner state
	 */
	private int getInnerState() {
		final int result;

		switch (this.getOuterState()) {
			case OUTERSTATE_SEARCH:
				result = this.getSearchMode();
				break;
			case OUTERSTATE_RESULT:
				result = this.getResultMode();
				break;
			case OUTERSTATE_DETAILS:
				result = this.getDetailsMode();
				break;
			case OUTERSTATE_UNDEFINED:
				result = INNERSTATE_UNDEFINED;
				break;
			default:
				throw new IllegalStateException("outer state");
		}
		return result;
	}

	/**
	 * @return the current <code>CollectState</code>
	 */
	public CollectState getCollectState() {
		return new CollectState(this.getOuterState(), this.getInnerState());
	}

	/**
	 * sets the current state, adjusting the tab selection accordingly.
	 * @param iTab the current tab (outer state)
	 * @param iMode the current mode (inner state)
	 */
	public void setCollectState(int iTab, int iMode) throws CommonBusinessException {
		this.setCollectState(iTab, iMode, true);

		// this.fireStateChanged();
	}

	/**
	 * sets the outer state, remembering the last inner state. Does not change the tab selection.
	 * @param iOuterState the outer state
	 */
	private void setOuterState(int iOuterState) throws CommonBusinessException {
		final int iMode;
		// remember inner state (history in the UML state diagram):
		/** @todo check if each outer state should have a history */
		switch (iOuterState) {
			case OUTERSTATE_SEARCH:
				iMode = this.getSearchMode();
				break;
			case OUTERSTATE_RESULT:
				// The selection might have changed while we were in a different state.
				// We have to adjust the result mode (inner state) to the current selection:
				this.iResultMode = getResultModeFromSelectionModel(this.ctlCollect.getResultTable().getSelectionModel());
				iMode = this.getResultMode();
				break;
			case OUTERSTATE_DETAILS:
				iMode = this.getDetailsMode();
				break;
			default:
				throw new IllegalArgumentException("iOuterState");
		}  // switch

		this.setCollectState(iOuterState, iMode, false);
	}

	public static int getResultModeFromSelectionModel(ListSelectionModel selectionmodel) {
		final int result;
		final int iMinSelectionIndex = selectionmodel.getMinSelectionIndex();
		if (iMinSelectionIndex == -1) {
			result = RESULTMODE_NOSELECTION;
		}
		else {
			final int iMaxSelectionIndex = selectionmodel.getMaxSelectionIndex();
			if (iMaxSelectionIndex > iMinSelectionIndex) {
				result = RESULTMODE_MULTISELECTION;
			}
			else {
				result = RESULTMODE_SINGLESELECTION;
			}
		}
		return result;
	}

	/**
	 * sets the current state
	 * @param iTab the outer state
	 * @param iMode the inner state
	 * @param bChangeTabUI Change the tab selection accordingly?
	 */
	private void setCollectState(int iTab, int iMode, boolean bChangeTabUI) throws CommonBusinessException {
		LOG.debug("CollectStateModel.setState: Tab " + iTab + " - Mode " + iMode);

		final CollectState collectstateOld = this.getCollectState();
		final CollectState collectstateNew = new CollectState(iTab, iMode);

		/** @todo exception handling: revert state on exceptions */
//		try {
		switch (this.getOuterState()) {
			case OUTERSTATE_SEARCH:
				this.fireSearchModeLeft(collectstateOld, collectstateNew);
				break;
			case OUTERSTATE_RESULT:
				this.fireResultModeLeft(collectstateOld, collectstateNew);
				break;
			case OUTERSTATE_DETAILS:
				this.fireDetailsModeLeft(collectstateOld, collectstateNew);
				break;
		}

		if (this.getTab() != iTab) {
			this.setTab(iTab, bChangeTabUI);
		}

		switch (iTab) {
			case OUTERSTATE_SEARCH:
				this.setSearchMode(iMode);
				this.fireSearchModeEntered(collectstateOld, collectstateNew);
				break;
			case OUTERSTATE_RESULT:
				this.setResultMode(iMode);
				this.fireResultModeEntered(collectstateOld, collectstateNew);
				break;
			case OUTERSTATE_DETAILS:
				this.setDetailsMode(iMode);
				this.fireDetailsModeEntered(collectstateOld, collectstateNew);
				break;
			default:
				assert false;
		}

		// ensure the new tab is enabled:
		this.pnlCollect.setTabbedPaneEnabledAt(iTab, true);
		this.ctlCollect.setTitle(iTab, iMode);
	}

	/**
	 * @return the currently selected tab (the current outer state)
	 */
	private int getTab() {
		return this.iTab;
	}

	/**
	 * selects a tab
	 * @param iTab the tab to select
	 * @param bChangeUI Change the tab selection accordingly?
	 */
	private void setTab(int iTab, boolean bChangeUI) {
		this.iTab = iTab;

		if (bChangeUI) {
			// temporarily remove the tab change listeners so we don't run into an endless recursion:
			this.removeTabChangeListener();

			this.pnlCollect.setTabbedPaneSelectedIndex(iTab);

			// restore the tab change listeners:
			this.addTabChangeListener();
		}
	}

	private void addTabChangeListener() {
		this.pnlCollect.addTabbedPaneChangeListener(this.tabChangeListener);
	}

	private void removeTabChangeListener() {
		this.pnlCollect.removeTabbedPaneChangeListener(this.tabChangeListener);
	}

	/**
	 * @return the current search mode
	 */
	public int getSearchMode() {
		return this.iSearchMode;
	}

	/**
	 * sets the current search mode
	 * @param iSearchMode
	 */
	private void setSearchMode(int iSearchMode) {
		this.iSearchMode = iSearchMode;

		switch (iSearchMode) {
			case SEARCHMODE_UNSYNCHED:
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_RESULT, false);  // LOGIC
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_DETAILS, false);  // LOGIC
				break;
		}
	}

	/**
	 * @return the current result mode
	 */
	public int getResultMode() {
		return this.iResultMode;
	}

	/**
	 * sets the current result mode
	 * @param iResultMode
	 */
	private void setResultMode(int iResultMode) {
		this.iResultMode = iResultMode;

//		switch (iResultMode) {
//			case RESULTMODE_NOSELECTION:
//				// do nothing here
//				break;
//			case RESULTMODE_SINGLESELECTION:
//				// do nothing here
//				break;
//			case RESULTMODE_MULTISELECTION:
//				// do nothing here
//				break;
//		}
	}

	/**
	 * @return the current details mode
	 */
	public int getDetailsMode() {
		return this.iDetailsMode;
	}

	/**
	 * sets the current details mode
	 * @param iDetailsMode
	 */
	private void setDetailsMode(int iDetailsMode) {
		// enter new state:
		switch (iDetailsMode) {
			case DETAILSMODE_VIEW:
				this.setEditedCollectable(null);
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_SEARCH, true);
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_RESULT, true);
				this.iDetailsMode = iDetailsMode;
				break;

			case DETAILSMODE_EDIT:
				// assert this.getDetailsMode() == DETAILSMODE_VIEW;
				/** @todo check why this assertion fails. */

				/** @todo perform version check in the listener? */
				this.performVersionCheck(iDetailsMode);
				break;

			case DETAILSMODE_NEW:
			case DETAILSMODE_NEW_SEARCHVALUE:
			case DETAILSMODE_MULTIVIEW:
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_SEARCH, true);
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_RESULT, true);
				this.iDetailsMode = iDetailsMode;
				break;

			case DETAILSMODE_NEW_CHANGED:
			case DETAILSMODE_MULTIEDIT:
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_SEARCH, false);
				this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_RESULT, false);
				this.iDetailsMode = iDetailsMode;
				break;

			default:
				this.iDetailsMode = iDetailsMode;
		}  // switch

	}  // setDetailsMode

	/**
	 * @todo This method needs refactoring. It shouldn't enable the tabbed pane itself - just perform a version check.
	 * @param iDetailsMode
	 */
	private void performVersionCheck(int iDetailsMode) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		try {
			final Clct clctOld = this.ctlCollect.getSelectedCollectable();
			final Integer iNewVersion;
			try {
				iNewVersion = this.ctlCollect.getVersionOfCollectableById(this.ctlCollect.getEntityName(), clctOld.getId());
			}
			catch (CommonFinderException ex) {
				final String sMessage = localeDelegate.getMessage(
						"CollectStateModel.7","Der Datensatz wurde zwischenzeitlich gel\u00f6scht.");
				/** @todo performVersionCheck should throw CommonFinderException! */
				throw new CommonFatalException(sMessage, ex);
			}
			catch (CommonBusinessException ex) {
				final String sMessage = localeDelegate.getMessage(
						"CollectStateModel.3","Beim Pr\u00fcfen der Version des Datensatzes ist ein Fehler aufgetreten.");
				throw new CommonFatalException(sMessage, ex);
				// this must not happen
			}
			
			final int iOldVersion = clctOld.getVersion();
			LOG.debug("performVersionCheck: Old version: " + iOldVersion + " - New version: " + iNewVersion);
			
			if (iNewVersion == iOldVersion) {
				this.setEditedCollectable(clctOld);
				// we don't want to use the new object so the user needn't retype the first keystroke
				// that caused the status change.
			}
			else {
				if (iNewVersion < iOldVersion) {
					throw new CommonFatalException(localeDelegate.getMessage(
							"CollectStateModel.9","Neuere Version erwartet."));
				}
				assert iNewVersion > iOldVersion;
				
				String sMessage = localeDelegate.getMessage(
						"CollectStateModel.6","Der Datensatz wurde zwischenzeitlich ge\u00e4ndert. Soll der Datensatz neu geladen werden?");
				int result = JOptionPane.showConfirmDialog(this.ctlCollect.getTab(), sMessage, 
						localeDelegate.getMessage("CollectStateModel.5","Datensatz ge\u00e4ndert"),
						JOptionPane.YES_NO_OPTION);
				
				if (result == JOptionPane.YES_OPTION) {
					final Clct clctNew;
					
					try {
						clctNew = this.ctlCollect.readSelectedCollectable();
					}
					catch (CommonFinderException ex) {
						sMessage = localeDelegate.getMessage(
								"CollectStateModel.8","Der Datensatz wurde zwischenzeitlich gel\u00f6scht.");
						/** @todo performVersionCheck should throw CommonFinderException! */
						throw new CommonFatalException(sMessage, ex);
					}
					catch (CommonBusinessException ex) {
						sMessage = localeDelegate.getMessage(
								"CollectStateModel.2","Beim Laden des Datensatzes ist ein Fehler aufgetreten.");
						throw new CommonFatalException(sMessage, ex);
						// this must not happen
					}
					
					this.setEditedCollectable(clctNew);
					
					try {
						this.ctlCollect.safeFillDetailsPanel(clctNew);
					}
					catch (CommonBusinessException ex) {
						sMessage = localeDelegate.getMessage(
								"CollectStateModel.1","Beim erneuten F\u00fcllen der Maske ist ein Fehler aufgetreten.");
						throw new CommonFatalException(sMessage, ex);
						// this will be caught further down.
					}
					
					this.ctlCollect.getResultController().replaceSelectedCollectableInTableModel(clctNew);
				}
				else {
					this.setEditedCollectable(clctOld);
				}
				
				/* Note that there is a little flaw here. At this point, we should go back to the view mode,
				 * as the user's change is reverted. Then, however, the object would be reread on the next
				 * keystroke, which is unnecessary. We don't want to make things more complicated than
				 * necessary, so we stay in edit mode here. Conflicts will be rare, anyway.
				 */
			}
			
			/** @todo enable tabbed pane outside of this method */
			this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_SEARCH, false);
			this.pnlCollect.setTabbedPaneEnabledAt(OUTERSTATE_RESULT, false);
			
			this.iDetailsMode = iDetailsMode;
		}
		catch (RuntimeException ex) {
			final String sErrorMsg = localeDelegate.getMessage(
					"CollectStateModel.4","Das Bearbeiten dieses Datensatzes ist zur Zeit nicht m\u00f6glich.");
			Errors.getInstance().showExceptionDialog(this.pnlCollect, sErrorMsg, ex);
			/** @todo Treat "Datensatz wurde zwischenzeitlich gel\u00f6scht" as a special case. */
			assert this.getDetailsMode() == DETAILSMODE_VIEW;
			// stay in View Mode
		}
	}

	public void addCollectStateListener(CollectStateListener listener) {
		lstCollectStateListeners.add(listener);
	}

	public void removeCollectStateListener(CollectStateListener listener) {
		lstCollectStateListeners.remove(listener);
	}

	private void fireSearchModeEntered(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.searchModeEntered(ev);
		}
	}

	private void fireSearchModeLeft(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.searchModeLeft(ev);
		}
	}

	private void fireResultModeEntered(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.resultModeEntered(ev);
		}
	}

	private void fireResultModeLeft(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.resultModeLeft(ev);
		}
	}

	private void fireDetailsModeEntered(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.detailsModeEntered(ev);
		}
	}

	private void fireDetailsModeLeft(CollectState collectstateOld, CollectState collectstateNew)
			throws CommonBusinessException {
		final CollectStateEvent ev = new CollectStateEvent(this, collectstateOld, collectstateNew);
		for (CollectStateListener listener : lstCollectStateListeners) {
			listener.detailsModeLeft(ev);
		}
	}

	/**
	 * @return the currently edited object
	 * @postcondition (result != null) <--> (this.getDetailsMode() == DETAILSMODE_EDIT)
	 */
	public Clct getEditedCollectable() {
		final Clct result = this.clctEdited;

		assert (result != null) == (this.getDetailsMode() == DETAILSMODE_EDIT);

		return result;
	}

	private void setEditedCollectable(Clct clct) {
		this.clctEdited = clct;
	}

}  // class CollectStateModel

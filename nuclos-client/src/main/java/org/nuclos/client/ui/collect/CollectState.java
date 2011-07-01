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

/**
 * A possible state in the <code>CollectStateModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectState implements CollectStateConstants {

	private final int iOuterState;
	private final int iInnerState;

	public CollectState(int iOuterState, int iInnerState) {
		this.iOuterState = iOuterState;
		this.iInnerState = iInnerState;
	}

	/**
	 * @return the outer state, corresponding to a tab (Search/Result/Details)
	 */
	public int getOuterState() {
		return iOuterState;
	}

	/**
	 * @return the inner state, refining the outer state.
	 */
	public int getInnerState() {
		return iInnerState;
	}

	public boolean isDefinedState() {
		return (this.getOuterState() != CollectState.OUTERSTATE_UNDEFINED && this.getInnerState() != CollectState.INNERSTATE_UNDEFINED);
	}

	@Override
	public boolean equals(Object oValue) {
		if (this == oValue) {
			return true;
		}
		if (!(oValue instanceof CollectState)) {
			return false;
		}
		final CollectState that = (CollectState) oValue;

		return (this.iOuterState == that.iOuterState) && (this.iInnerState == that.iInnerState);
	}

	@Override
	public int hashCode() {
		return (iOuterState << 4) & iInnerState;
	}

	@Override
	public String toString() {
		return "outer state: " + iOuterState + " - inner state: " + iInnerState;
	}

	/**
	 * @param iResultMode
	 * @return Are rows (at least one row) selected in the result list?
	 */
	public static boolean isResultModeSelected(int iResultMode) {
		return iResultMode == RESULTMODE_SINGLESELECTION || iResultMode == RESULTMODE_MULTISELECTION;
	}

	/**
	 * @param iDetailsMode
	 * @return Is the user entering a new record?
	 */
	public static boolean isDetailsModeNew(int iDetailsMode) {
		return iDetailsMode == DETAILSMODE_NEW || iDetailsMode == DETAILSMODE_NEW_CHANGED || iDetailsMode == DETAILSMODE_NEW_SEARCHVALUE;
	}

	/**
	 * @param iDetailsMode
	 * @return Is the user viewing or editing a single record?
	 */
	public static boolean isDetailsModeViewOrEdit(int iDetailsMode) {
		return iDetailsMode == DETAILSMODE_VIEW || iDetailsMode == DETAILSMODE_EDIT;
	}

	/**
	 * @param iDetailsMode
	 * @return Is the user about to change multiple records?
	 */
	public static boolean isDetailsModeMultiViewOrEdit(int iDetailsMode) {
		return iDetailsMode == DETAILSMODE_MULTIVIEW || iDetailsMode == DETAILSMODE_MULTIEDIT;
	}

	/**
	 * @param iDetailsMode
	 * @return Are changes pending?
	 */
	public static boolean isDetailsModeChangesPending(int iDetailsMode) {
		return iDetailsMode == DETAILSMODE_EDIT || iDetailsMode == DETAILSMODE_NEW_CHANGED || iDetailsMode == DETAILSMODE_MULTIEDIT;
	}

	public boolean isSearchMode() {
		return this.getOuterState() == OUTERSTATE_SEARCH;
	}

	public boolean isResultMode() {
		return this.getOuterState() == OUTERSTATE_RESULT;
	}

	public boolean isDetailsMode() {
		return this.getOuterState() == OUTERSTATE_DETAILS;
	}

	public boolean isDetailsModeNew() {
		return this.isDetailsMode() && isDetailsModeNew(this.getInnerState());
	}

	public boolean isDetailsModeViewOrEdit() {
		return this.isDetailsMode() && isDetailsModeViewOrEdit(this.getInnerState());
	}

	public boolean isDetailsModeMultiViewOrEdit() {
		return this.isDetailsMode() && isDetailsModeMultiViewOrEdit(this.getInnerState());
	}

}  // class CollectState

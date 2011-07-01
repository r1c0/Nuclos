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
 * CollectState constants.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectStateConstants {
	int OUTERSTATE_UNDEFINED = -1;
	int INNERSTATE_UNDEFINED = -1;
	/**
	 * Outer state: Search (tab)
	 */
	int OUTERSTATE_SEARCH = CollectPanel.TAB_SEARCH;
	/**
	 * Outer state: Result (tab)
	 */
	int OUTERSTATE_RESULT = CollectPanel.TAB_RESULT;
	/**
	 * Outer state: Details (tab)
	 */
	int OUTERSTATE_DETAILS = CollectPanel.TAB_DETAILS;
	/**
	 * Undefined search mode.
	 */
	int SEARCHMODE_UNDEFINED = INNERSTATE_UNDEFINED;
	/**
	 * Unsynched mode: The search result doesn't match the searchcondition.
	 */
	int SEARCHMODE_UNSYNCHED = 1;
	/**
	 * Synched mode: The search result matches the searchcondition.
	 */
	int SEARCHMODE_SYNCHED = 2;
	/**
	 * Undefined result mode.
	 */
	int RESULTMODE_UNDEFINED = INNERSTATE_UNDEFINED;
	/**
	 * Nothing is selected in the result list.
	 */
	int RESULTMODE_NOSELECTION = 1;
	/**
	 * A single row is selected in the result list.
	 */
	int RESULTMODE_SINGLESELECTION = 2;
	/**
	 * More than one row is selected in the result list.
	 */
	int RESULTMODE_MULTISELECTION = 3;
	/**
	 * Undefined details mode.
	 */
	int DETAILSMODE_UNDEFINED = INNERSTATE_UNDEFINED;
	/**
	 * View mode: A single record is viewed, but has not been changed by the user (yet).
	 */
	int DETAILSMODE_VIEW = 1;
	/**
	 * Edit mode: A single record is edited by the user. Changes are pending.
	 */
	int DETAILSMODE_EDIT = 2;
	/**
	 * New mode: A single new (not stored yet) record is about to be entered by the user.
	 */
	int DETAILSMODE_NEW = 3;
	/**
	 * NewChanged mode: A single new (not stored yet) record is entered by the user. Changes are pending.
	 */
	int DETAILSMODE_NEW_CHANGED = 4;
	/**
	 * MultiEdit mode: Multiple records are about to be changed.
	 */
	int DETAILSMODE_MULTIVIEW = 5;
	/**
	 * MultiEditChanged mode: Multiple records are to be changed. Changes are pending.
	 */
	int DETAILSMODE_MULTIEDIT = 6;
	/**
	 * New mode: A single new (not stored yet) record is about to be entered by the user, but filled with data of the search panel.
	 */
	int DETAILSMODE_NEW_SEARCHVALUE = 7;

}  // interface CollectStateConstants

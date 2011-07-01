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
package org.nuclos.client.ui.collect.component.model;

/**
 * Listener for <code>CollectableComponentModelEvent</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectableComponentModelListener
    extends java.util.EventListener {
	/**
	 * A <code>CollectableField</code> changed in a <code>CollectableComponentModel</code>.
	 * Typically, the corresponding <code>CollectController</code> gets notified.
	 * Note that this method may be invoked even if there wasn't really a change.
	 * <code>ev.collectableFieldHasChanged()</code> can be used to determine if the value really changed.
	 * @param ev
	 */
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev);

	/**
	 * the search condition changed in the model.
	 * @param ev
	 * @precondition ev.getCollectableComponentModel().isSearchModel()
	 */
	public void searchConditionChangedInModel(SearchComponentModelEvent ev);

	/**
	 * the valueToBeChanged property in an DetailsComponentModel changed.
	 * @todo should be called valueToBeChangedPropertyChangedInModel
	 * @param ev
	 */
	public void valueToBeChanged(DetailsComponentModelEvent ev);

}  // interface CollectableComponentModelListener

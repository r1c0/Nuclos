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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nuclos.common.collect.collectable.CollectableField;

/**
 * Helper class for handling <code>CollectableComponentModelEvent</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableComponentModelHelper {

	private final transient List<CollectableComponentModelListener> lstListeners = new LinkedList<CollectableComponentModelListener>();
	
	public CollectableComponentModelHelper() {
	}

	public synchronized void removeCollectableComponentModelListener(CollectableComponentModelListener l) {
		this.lstListeners.remove(l);
	}

	public synchronized void addCollectableComponentModelListener(CollectableComponentModelListener l) {
		this.lstListeners.add(l);
	}

	public void fireCollectableFieldChanged(CollectableComponentModel clctcompmodel, CollectableField clctfOldValue,
			CollectableField clctfNewValue) {
		this.fireCollectableFieldChangedEvent(new CollectableComponentModelEvent(clctcompmodel, clctfOldValue, clctfNewValue));
	}

	public void fireSearchConditionChanged(SearchComponentModel clctcompmodel) {
		this.fireSearchConditionChangedEvent(new SearchComponentModelEvent(clctcompmodel));
	}

	public void fireValueToBeChanged(DetailsComponentModel clctcompmodel, boolean bValueToBeChanged) {
		this.fireValueToBeChangedEvent(new DetailsComponentModelEvent(clctcompmodel, bValueToBeChanged));
	}

	private void fireCollectableFieldChangedEvent(CollectableComponentModelEvent ev) {
		for (CollectableComponentModelListener listener : new ArrayList<CollectableComponentModelListener>(lstListeners)) {
			listener.collectableFieldChangedInModel(ev);
		}
	}

	private void fireSearchConditionChangedEvent(SearchComponentModelEvent ev) {
		for (CollectableComponentModelListener listener : new ArrayList<CollectableComponentModelListener>(lstListeners)) {
			listener.searchConditionChangedInModel(ev);
		}
	}

	private void fireValueToBeChangedEvent(DetailsComponentModelEvent ev) {
		for (CollectableComponentModelListener listener : new ArrayList<CollectableComponentModelListener>(lstListeners)) {
			listener.valueToBeChanged(ev);
		}
	}

}	// class CollectableComponentModelHelper

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

package org.nuclos.client.ui.resplan;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractResPlanModel<R, T extends Comparable<? super T>, E, L> implements ResPlanModel<R, T, E, L> {

	protected final List<ResPlanModelListener> listeners;
	
	public AbstractResPlanModel() {
		this.listeners = new ArrayList<ResPlanModelListener>();
	}
		
	@Override
	public ResPlanModelListener[] getResPlanModelListeners() {
		return listeners.toArray(new ResPlanModelListener[listeners.size()]);
	}
	
	@Override
	public void addResPlanModelListener(ResPlanModelListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeResPlanModelListener(ResPlanModelListener listener) {
		listeners.remove(listener);
	}

	protected void fireResourcesChanged() {
		for (ResPlanModelListener listener : listeners) {
			listener.resourcesChanged(new ResPlanModelEvent(this, null, null, null, null));
		}
	}

	protected void fireResourceEntriesChanged() {
		for (ResPlanModelListener listener : listeners) {
			listener.resourceEntriesChanged(new ResPlanModelEvent(this, null, null, null, null));
		}
	}
	
	protected void fireEntryChanged(R resource) {
		for (ResPlanModelListener listener : listeners) {
			listener.entryChanged(new ResPlanModelEvent(this, resource, null, null, null));
		}
	}

	protected void fireEntryChanged(R resource, E entry, Interval<T> interval) {
		for (ResPlanModelListener listener : listeners) {
			listener.entryChanged(new ResPlanModelEvent(this, resource, entry, interval, null));
		}
	}
}

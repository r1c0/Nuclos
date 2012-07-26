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

import java.util.List;

/**
 * @param <R> resource
 * @param <T> time unit
 * @param <V> value
 * @param <L> relation
 */
public interface ResPlanModel<R, T extends Comparable<? super T>, E, L> {

	/**
	 * Returns the concrete entry type of this model. 
	 */
	public abstract Class<E> getEntryType();

	/**
	 * Retrieves all resources.
	 */
	public abstract List<? extends R> getResources();

	/**
	 * Retrieves all entries to the given resource.
	 */
	public abstract List<? extends E> getEntries(R resource);

	/**
	 * Retrieves the interval for the given entry.
	 */
	public abstract Interval<T> getInterval(E entry);

	/**
	 * Creates a new entry for the given resource and interval.
	 * Note that ehis method returns intentionally void. It is up to the model to decide
	 * what to do and to propagte the changes (if any) as events.
	 */
	public abstract void createEntry(R resource, Interval<T> interval, Object value);
	
	/**
	 * Updates the given entry.
	 */
	public abstract void updateEntry(E entry, R resource, Interval<T> interval);

	/**
	 * Removes the given entry.
	 */
	public abstract void removeEntry(E entry);
	
	public abstract void createRelation(E entryFrom, E entryTo);
	
	public abstract void removeRelation(L relation);

	public abstract Object getEntryId(E entry);
	
	public abstract Object getRelationId(L relation);
	
	public abstract boolean isMilestone(E entry);
	
	public abstract boolean isCreateEntryAllowed();

	public abstract boolean isUpdateEntryAllowed(E entry);
	
	public abstract boolean isRemoveEntryAllowed(E entry);
	
	public abstract boolean isCreateRelationAllowed();

	public abstract boolean isUpdateRelationAllowed(L relation);
	
	public abstract boolean isRemoveRelationAllowed(L relation);
	
	public abstract List<? extends L> getAllRelations();
	
	public abstract List<? extends L> getRelations(E entry);
	
	public abstract Object getRelationFromId(L relation);
	
	public abstract Object getRelationToId(L relation);
	
	public abstract R getResourceFromEntry(E entry);
	
	public abstract void addResPlanModelListener(ResPlanModelListener listener);
	
	public abstract void removeResPlanModelListener(ResPlanModelListener listener);

	public abstract ResPlanModelListener[] getResPlanModelListeners();
}

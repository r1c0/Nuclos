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
package org.nuclos.client.searchfilter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;

/**
 * A set of search filters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class SearchFilters {

	private final List<ChangeListener> lstChangeListeners = new LinkedList<ChangeListener>();

	/**
	 * Set of module specific search filters (for a given module).
	 */
	public static class EntitySearchFilters extends SearchFilters {
		private final String sEntityName;

		public EntitySearchFilters(String sEntityName) {
			this.sEntityName = sEntityName;
		}

		/**
		 * reads the search filter with the given name from the preferences.
		 * @param sFilterName
		 * @return
		 * @throws NoSuchElementException if there is no filter with the given name.
		 * @postcondition result != null
		 */
		@Override
		public EntitySearchFilter get(String sFilterName, String sOwner) throws PreferencesException {
			return SearchFilterCache.getInstance().getEntitySearchFilter(sFilterName, sOwner);
		}

		/**
		 * @return the module specific search filters for the currently logged-in user and the module specified in the constructor.
		 * @postcondition result != null
		 */
		@Override
		public List<EntitySearchFilter> getAll() throws PreferencesException {
			List<EntitySearchFilter> result = CollectionUtils.sorted(
				SearchFilterCache.getInstance().getEntitySearchFilterByEntity(this.sEntityName), 
				new Comparator<EntitySearchFilter>() {

					@Override
					public int compare(EntitySearchFilter o1,
						EntitySearchFilter o2) {
						return LangUtils.compare(o1.getName(), o2.getName());
					}});
			
			assert result != null;
			return result;
		}

		/**
		 * @return a new default filter for this set of search filters.
		 * @postcondition result != null
		 */
		@Override
		public EntitySearchFilter newDefaultFilter() {
			final EntitySearchFilter result = EntitySearchFilter.newDefaultFilter();			
			result.setEntityName(this.sEntityName);
			
			if (Modules.getInstance().isModuleEntity(sEntityName)) {
				result.setSearchDeleted(CollectableGenericObjectSearchExpression.SEARCH_UNDELETED);
			}

			return result;
		}

	}	// inner class EntitySearchFilters

	/**
	 * @return the search filters for the module with the given id.
	 */
	public static EntitySearchFilters forEntity(String sEntity) {
		return new EntitySearchFilters(sEntity);
	}
	
	public static EntitySearchFilters forAllEntities() {
		return forEntity(null);
	}
	
	/**
	 * @return the list of search filters.
	 * @throws PreferencesException
	 */
	public abstract List<? extends SearchFilter> getAll() throws PreferencesException;

	/**
	 * @return a new default filter
	 * @postcondition result != null
	 */
	public abstract SearchFilter newDefaultFilter();

	/**
	 * @return the names of all search filters.
	 * @throws PreferencesException
	 */
	List<String> getFilterNames() throws PreferencesException {
		final List<String> result;

		final Transformer<EntitySearchFilter, String> decodeDecodeFilterName = new Transformer<EntitySearchFilter, String>() {
			@Override
			public String transform(EntitySearchFilter searchFilter) {
				return SearchFilter.decoded(searchFilter.getName());
			}
		};
		result = CollectionUtils.transform(SearchFilterCache.getInstance().getAllEntitySearchFilters(), decodeDecodeFilterName);

		return result;
	}

	/**
	 * @param sFilterName
	 * @return Is the given filter name already used for a personal search filter?
	 */
	public boolean contains(String sFilterName, String sOwner) {
		return (SearchFilterCache.getInstance().getEntitySearchFilter(sFilterName, sOwner)) == null ? false : true;
	}

	/**
	 * reads the search filter with the given name from the preferences.
	 * @param sSearchFilter
	 * @param sOwner
	 * @return
	 * @throws NoSuchElementException if there is no filter with the given name.
	 * @postcondition result != null
	 */
	public abstract SearchFilter get(String sSearchFilter, String sOwner) throws PreferencesException;

	/**
	 * stores the given search filter in the preferences.
	 * @param filter
	 * @throws IllegalArgumentException if the filter (name) is empty or invalid
	 */
	public void put(SearchFilter filter) throws PreferencesException, NuclosBusinessException {
		SearchFilterDelegate.getInstance().insertSearchFilter(filter);
		this.fireChangedEvent();
	}

	/**
	 * removes the filter with the given name from the personal filters
	 * @param sFilterName
	 * @todo refactor: SearchFilter.remove()
	 */
	public void remove(SearchFilter searchFilter) throws NuclosBusinessException {
		SearchFilterDelegate.getInstance().removeSearchFilter(searchFilter);
		this.fireChangedEvent();
	}

	/**
	 * adds the given change listener.
	 * @param cl is notified when the search filters have changed.
	 */
	public synchronized void addChangeListener(ChangeListener cl) {
		this.lstChangeListeners.add(cl);
	}

	/**
	 * removes the given change listener.
	 * @param cl
	 */
	public synchronized void removeChangeListener(ChangeListener cl) {
		this.lstChangeListeners.remove(cl);
	}

	/**
	 * notifies the change listeners that the search filters have changed.
	 */
	private synchronized void fireChangedEvent() {
		for (ChangeListener cl : this.lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
	}

}	// class SearchFilters

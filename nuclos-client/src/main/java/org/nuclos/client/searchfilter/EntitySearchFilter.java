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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import org.nuclos.client.datasource.NuclosSearchConditionUtils;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * An (module or masterdata) entity specific search filter, containing a user defined search condition, selection of visible columns
 * and sorting relevant columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @author  <a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 * @todo a ModuleSearchFilter should contain a CollectableSearchExpression rather than a CollectableSearchCondition (along with other field's already contained in the expression)
 */
public class EntitySearchFilter extends SearchFilter {

	/**
	 * list of visible columns
	 */
	private List<? extends CollectableEntityField> lstclctefweVisible;

	/**
	 * @todo This should be replaced by a List<CollectableSorting>, as the direction (ascending/descending) is missing here.
	 * @todo Or better: the filter itself should contain a [GenericObject]CollectableSearchExpression
	 */
	private List<String> lstSortingColumnNames;

	public EntitySearchFilter() {
		super();

		this.lstSortingColumnNames = Collections.emptyList();
	}

	/**
	 * creates the default search filter with a specific name and without searchcondition.
	 * @return new default search filter
	 * @postcondition result.isDefaultFilter()
	 * @postcondition result.getSearchCondition() == null
	 */
	public static EntitySearchFilter newDefaultFilter() {
		final EntitySearchFilter result = new EntitySearchFilter() {
			@Override
			public boolean isDefaultFilter() {
				return true;
			}
		};
		result.setName(CommonLocaleDelegate.getMessage("EntitySearchFilter.1","<Alle>"));
		result.setDescription(CommonLocaleDelegate.getMessage("EntitySearchFilter.3","Standardfilter (Keine Einschr\u00e4nkung)"));		
		
		assert result.isDefaultFilter();
		assert result.getSearchCondition() == null;

		return result;
	}
	
	public String getEntityName() {
		return getSearchFilterVO().getEntity();
	}
	
	public void setEntityName(String sEntityName) {
		getSearchFilterVO().setEntity(sEntityName);
	}

	/**
	 * @return the internal search condition that is to be used for the actual search.
	 */
	@Override
	public CollectableSearchCondition getInternalSearchCondition() {
		if (Modules.getInstance().isModuleEntity(this.getEntityName())) {
			return GenericObjectClientUtils.getInternalSearchCondition(Modules.getInstance().getModuleIdByEntityName(this.getEntityName()), this.getSearchCondition());
		}
		else {
			return this.getSearchCondition();
		}
	}

	/**
	 * @return the columns to be shown in the search result.
	 */
	public List<? extends CollectableEntityField> getVisibleColumns() {
		return this.lstclctefweVisible;
	}

	/**
	 * @param lstclctefweVisible the columns to be shown in the search result.
	 */
	public void setVisibleColumns(List<? extends CollectableEntityField> lstclctefweVisible) {
		this.lstclctefweVisible = lstclctefweVisible;
	}

	/**
	 * @return List<String> the names of the columns defining the sorting of the result.
	 * Note that these columns must belong to the main entity.
	 * @deprecated The direction (ascending/descending) is missing here.
	 */
	@Deprecated
	public List<String> getSortingColumnNames() {
		return this.lstSortingColumnNames;
	}

	/**
	 * @todo This is a workaround - the Search filter should contain the sorting order, not just the column names.
	 */
	public List<CollectableSorting> getSortingOrder() {
		return CollectionUtils.transform(this.getSortingColumnNames(), new Transformer<String, CollectableSorting>() {
			@Override
			public CollectableSorting transform(String sFieldName) {
				return new CollectableSorting(sFieldName, true);
			}
		});
	}

	/**
	 * @param lstSortingColumnNames List<String> the names of the columns defining the sorting of the result.
	 * Note that these columns must belong to the main entity.
	 * @deprecated The direction (ascending/descending) is missing here.
	 */
	@Deprecated
	public void setSortingColumnNames(List<String> lstSortingColumnNames) {
		this.lstSortingColumnNames = lstSortingColumnNames;
	}

	/**
	 * reads the search filter with the given name from the preferences.
	 * @param sFilterName
	 * @param prefsParent the parent node from where to read
	 * @return
	 * @throws NoSuchElementException if there is no filter with the given name.
	 * @postcondition result != null
	 */
	
	static EntitySearchFilter get(String sFilterName, String sOwner) {
		EntitySearchFilter result = SearchFilterCache.getInstance().getEntitySearchFilter(sFilterName, sOwner);

		if (result == null) {
			throw new NoSuchElementException(CommonLocaleDelegate.getMessage("EntitySearchFilter.2","Es existiert kein Suchfilter mit dem Namen {0}.", sFilterName));
		}
		
//		if (result == null) {
//			result = new EntitySearchFilter();
//			
//			result.setEntityName(Modules.getInstance().getEntityNameByModuleId(0));
//			result.setSearchDeleted(CollectableGenericObjectSearchExpression.SEARCH_UNDELETED);
//			result.setName(sFilterName);
//		}
		
		assert result != null;
		return result;
	}

	@Override
	void put() throws NuclosBusinessException {
		super.put();
	}
	
	public static void writeCollectableEntityFieldsToPreferences(Preferences prefs, List<? extends CollectableEntityField> lstclctefweSelected, String sPrefsNodeFields, String sPrefsNodeEntities) throws PreferencesException {
		PreferencesUtils.putStringList(prefs, sPrefsNodeFields, CollectableUtils.getFieldNamesFromCollectableEntityFields(lstclctefweSelected));

		final List<String> lstEntityNames = CollectionUtils.transform(lstclctefweSelected, new CollectableEntityField.GetEntityName());
		PreferencesUtils.putStringList(prefs, sPrefsNodeEntities, lstEntityNames);
	}
	
	/**
	 * @deprecated This is an evil copy of 
	 * 		org.nuclos.client.genericobject.GenericObjectClientUtils.readCollectableEntityFieldsFromPreferences(Preferences, CollectableEntity)
	 * 		But it also works on MasterData. Thus it is difficult to get rid of it!
	 */
	public static List<CollectableEntityField> readCollectableEntityFieldsFromPreferences(Preferences prefs, CollectableEntity clcte, String sPrefsNodeFields, String sPrefsNodeEntities) {
		List<String> lstSelectedFieldNames;
		List<String> lstSelectedEntityNames;
		try {
			lstSelectedFieldNames = PreferencesUtils.getStringList(prefs, sPrefsNodeFields);
			lstSelectedEntityNames = PreferencesUtils.getStringList(prefs, sPrefsNodeEntities);
		}
		catch (PreferencesException ex) {
			lstSelectedFieldNames = new ArrayList<String>();
			lstSelectedEntityNames = new ArrayList<String>();
			// no exception is thrown here.
		}
		assert lstSelectedFieldNames != null;
		assert lstSelectedEntityNames != null;

		// ensure backwards compatibility:
		if (lstSelectedEntityNames.isEmpty() && !lstSelectedFieldNames.isEmpty()) {
			lstSelectedEntityNames = Arrays.asList(new String[lstSelectedFieldNames.size()]);
			assert lstSelectedEntityNames.size() == lstSelectedFieldNames.size();
		}

		if (lstSelectedFieldNames.size() != lstSelectedEntityNames.size()) {
			lstSelectedFieldNames = new ArrayList<String>();
			lstSelectedEntityNames = new ArrayList<String>();
		}

		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		final CollectableEntityProvider clcteprovider = DefaultCollectableEntityProvider.getInstance();
		for (int i = 0; i < lstSelectedFieldNames.size(); i++) {
			final String sFieldName = lstSelectedFieldNames.get(i);
			final String sEntityName = lstSelectedEntityNames.get(i);
			try {
				final CollectableEntity clcteForField = (sEntityName == null) ? clcte : clcteprovider.getCollectableEntity(sEntityName);
				if (Modules.getInstance().isModuleEntity(clcteForField.getName())) {
					result.add(GenericObjectClientUtils.getCollectableEntityFieldForResult(clcteForField, sFieldName, clcte));
				}
				else {
					result.add(new CollectableEntityFieldWithEntityForExternal(clcteForField, sFieldName, false, true));
				}		
			}
			catch (Exception ex) {
				// ignore unknown fields
			}
		}

		return result;
	}

	/**
	 * @return this filter's search condition
	 */
	@Override
	public CollectableSearchCondition getSearchCondition() {
		return NuclosSearchConditionUtils.restorePlainSubConditions(super.getSearchCondition());
	}
	
	public Boolean isForced() {
		return getSearchFilterVO().isForced();
	}
	
	public void setForced(Boolean isForced) {
		getSearchFilterVO().setForced(isForced);
	}
}	// class ModuleSearchFilter

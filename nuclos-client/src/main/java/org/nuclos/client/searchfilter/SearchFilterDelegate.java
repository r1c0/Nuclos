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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.visit.PutSearchConditionToPrefsVisitor;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeRemote;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;

/**
 * Business Delegate for <code>SearchFilterFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 01.00.00
 */
public class SearchFilterDelegate {

	private static final Logger LOG = Logger.getLogger(SearchFilterDelegate.class);

	private static final String PREFS_NODE_SEARCHFILTERS = "searchFilters";
	private static final String PREFS_NODE_GLOBALSEARCHFILTERS = "globalSearchFilters";

	private static final String PREFS_NODE_SEARCHCONDITION = "searchCondition";
	private static final String PREFS_NODE_VISIBLECOLUMNS = "visibleColumns";
	private static final String PREFS_NODE_VISIBLECOLUMNENTITIES = "visibleColumnEntities";
	
	/**
	 * New way to save/load sorting column prefs: As {@link org.nuclos.common.collect.collectable.CollectableSorting}.
	 */
	public static final String PREFS_NODE_COLLECTABLESORTING = "collectableSorting";
	
	/**
	 * @deprecated Old way to save/load sorting column prefs: only sorting columns *names* (String).
	 */
	private static final String PREFS_NODE_SORTINGCOLUMNS = "sortingColumns";
	
	private static SearchFilterDelegate INSTANCE;
	
	//
	
	// Spring injection

	private SpringLocaleDelegate localeDelegate;

	private SearchFilterFacadeRemote searchFilterFacadeRemote;
	
	// end of Spring injection

	SearchFilterDelegate() {
		INSTANCE = this;
	}
	
	public final void setSearchFilterFacadeRemote(SearchFilterFacadeRemote searchFilterFacadeRemote) {
		this.searchFilterFacadeRemote = searchFilterFacadeRemote;
	}
	
	public final void setSpringLocaleDelegate(SpringLocaleDelegate springLocaleDelegate) {
		this.localeDelegate = springLocaleDelegate;
	}

	/**
	 * @return the one (and only) instance of SearchFilterDelegate
	 */
	public static SearchFilterDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	public Object update(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants, List<TranslationVO> resources) throws CommonBusinessException {
		try {
			return this.searchFilterFacadeRemote.modify(sEntityName, mdvo, mpDependants, resources);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * inserts the given searchfilter
	 * @param filter
	 */
	public void insertSearchFilter(SearchFilter filter) {
		try {
			SearchFilter searchFilter = makeSearchFilter(searchFilterFacadeRemote.createSearchFilter(insertOrUpdateFilter(filter).getSearchFilterVO()));
			SearchFilterCache.getInstance().addFilter(searchFilter);
		}
		catch (Exception e) {
			String sMessage = localeDelegate.getMessage("SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			LOG.error(sMessage);
			throw new NuclosFatalException(sMessage, e);
		}
	}

	/**
	 * updates the searchfilter with the given oldFilterIdentifier
	 * @param newFilter
	 * @param oldFilterIdentifier
	 */
	public void updateSearchFilter(SearchFilter newFilter, String sOldFilterName, String sOwner) throws NuclosBusinessException {
		SearchFilter oldSearchFilter = SearchFilterCache.getInstance().getSearchFilter(sOldFilterName, sOwner);

		if (!oldSearchFilter.getSearchFilterVO().isEditable()) {
			throw new NuclosBusinessException(localeDelegate.getMessage(
					"SearchFilterDelegate.2", "Der Suchfilter darf von Ihnen nicht ge\u00e4ndert werden."));
		}

		assert newFilter.getSearchFilterVO().getFilterPrefs() != null;

		try {
			SearchFilter searchFilter = makeSearchFilter(searchFilterFacadeRemote.modifySearchFilter(insertOrUpdateFilter(newFilter).getSearchFilterVO()));
			SearchFilterCache.getInstance().removeFilter(sOldFilterName, sOwner);
			SearchFilterCache.getInstance().addFilter(searchFilter);
		}
		catch (CommonStaleVersionException e) {
			String sMessage = localeDelegate.getMessage(
					"SearchFilterDelegate.4", "Der Suchfilter wurde zwischenzeitlich von einem anderen Benutzer ge\u00e4ndert. Bitte initialisieren Sie den Client.");
			LOG.info(sMessage);
			throw new NuclosBusinessException(sMessage, e);
		}
		catch (Exception e) {
			String sMessage = localeDelegate.getMessage(
					"SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			LOG.error(sMessage);
			throw new NuclosFatalException(sMessage, e);
		}
	}

	private SearchFilter insertOrUpdateFilter(SearchFilter filter) {
		try {
			Preferences prefs = Preferences.userRoot().node("org/nuclos/client");
			if (filter instanceof EntitySearchFilter) {
				prefs = prefs.node(PREFS_NODE_SEARCHFILTERS);
			}
			else {
				prefs = prefs.node(PREFS_NODE_GLOBALSEARCHFILTERS);
			}
			prefs = prefs.node(filter.getName());

			PutSearchConditionToPrefsVisitor.putSearchCondition(prefs.node(PREFS_NODE_SEARCHCONDITION), filter.getSearchCondition());
			if (filter instanceof EntitySearchFilter) {
				final EntitySearchFilter f = (EntitySearchFilter) filter;
				EntitySearchFilter.writeCollectableEntityFieldsToPreferences(prefs, f.getVisibleColumns(), 
						PREFS_NODE_VISIBLECOLUMNS, PREFS_NODE_VISIBLECOLUMNENTITIES);
				PreferencesUtils.putSerializableListXML(prefs, PREFS_NODE_COLLECTABLESORTING, f.getSortingOrder());
			}

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			prefs.exportSubtree(baos);
			filter.getSearchFilterVO().setFilterPrefs(baos.toString("UTF-8"));
			return filter;
		}
		catch (Exception e) {
			String sMessage = localeDelegate.getMessage(
					"SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			LOG.error(sMessage);
			throw new NuclosFatalException(sMessage, e);
		}
	}

	/**
	 * removes the given search filter
	 * @param filter
	 * @throws NuclosBusinessException
	 */
	public void removeSearchFilter(SearchFilter filter) throws NuclosBusinessException {
		if (!filter.getSearchFilterVO().isEditable()) {
			throw new NuclosBusinessException(localeDelegate.getMessage(
					"SearchFilterDelegate.5", "Der Suchfilter darf von Ihnen nicht gel\u00f6scht werden."));
		}

		try {
			searchFilterFacadeRemote.removeSearchFilter(filter.getSearchFilterVO());
			SearchFilterCache.getInstance().removeFilter(filter);
		}
		catch (CommonStaleVersionException e) {
			throw new NuclosBusinessException(localeDelegate.getMessage(
					"SearchFilterDelegate.7", "Ein Suchfilter konnte nicht gel\u00f6scht werden, da er zwischenzeitlich von einem anderen Benutzer ge\u00e4ndert wurde.\n" +
					"Bitte initialisieren Sie die Client und versuchen es erneut."));
		}
		catch (Exception e) {
			throw new NuclosFatalException(localeDelegate.getMessage(
					"SearchFilterDelegate.8", "Ein Suchfilter konnte nicht gel\u00f6scht werden"), e);
		}
	}

	/**
	 * get all entity searchfilter for the given user
	 * @param sUser
	 * @return Collection<EntitySearchFilter>
	 */
	public Collection<SearchFilter> getAllSearchFilterByUser(String sUser) {
		Collection<SearchFilter> collSearchFilter = new ArrayList<SearchFilter>();

		try {
			for (SearchFilterVO filterVO : searchFilterFacadeRemote.getAllSearchFilterByUser(sUser)) {
				try {
					SearchFilter sf = makeSearchFilter(filterVO);
					if (sf != null)
						collSearchFilter.add(sf);
				}
				catch (Exception e) {
					LOG.error(localeDelegate.getMessage(
							"SearchFilterDelegate.9", "Ein Suchfilter konnte nicht geladen werden"));
				}
			}
		}
		catch (Exception e) {
			LOG.error(localeDelegate.getMessage(
					"SearchFilterDelegate.9", "Ein Suchfilter konnte nicht geladen werden"));
		}

		return collSearchFilter;
	}

	/**
	 * transforms a SearchFilterVO to a SearchFilter
	 * @param searchFilterVO
	 * @return SearchFilter
	 */
	private SearchFilter makeSearchFilter(SearchFilterVO searchFilterVO) {
		SearchFilter result = null;
		try {
			result = new EntitySearchFilter();
			result.setSearchFilterVO(searchFilterVO);

			final ByteArrayInputStream is = new ByteArrayInputStream(searchFilterVO.getFilterPrefs().getBytes("UTF-8"));
			Preferences.importPreferences(is);
			Preferences prefs = Preferences.userRoot().node("org/nuclos/client");
			final Preferences prefsSearchfilter;
			if (prefs.nodeExists(PREFS_NODE_SEARCHFILTERS)) {
				prefsSearchfilter = prefs.node(PREFS_NODE_SEARCHFILTERS);
			}
			else {
				return null;
			}

			if (prefsSearchfilter.nodeExists(searchFilterVO.getFilterName())) {
				prefs = prefsSearchfilter.node(searchFilterVO.getFilterName());
			}
			else {
				return null;
			}

			final String sEntityName = searchFilterVO.getEntity();
			// SearchFilter properties
			result.setSearchCondition(SearchConditionUtils.getSearchCondition(prefs.node(PREFS_NODE_SEARCHCONDITION),sEntityName));
			// EntitySearchFilter properties
			final EntitySearchFilter f = (EntitySearchFilter) result;
			f.setVisibleColumns(EntitySearchFilter.readCollectableEntityFieldsFromPreferences(prefs,
					DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName),
					PREFS_NODE_VISIBLECOLUMNS, PREFS_NODE_VISIBLECOLUMNENTITIES));
			
			if (PreferencesUtils.nodeExists(prefs, PREFS_NODE_COLLECTABLESORTING)) {
				f.setSortingOrder((List<CollectableSorting>) PreferencesUtils.getSerializableListXML(prefs, PREFS_NODE_COLLECTABLESORTING, true));
			}
			// backward compatibility
			else {
				final List<CollectableSorting> sorting = new ArrayList<CollectableSorting>();
				for (String n: PreferencesUtils.getStringList(prefs, PREFS_NODE_SORTINGCOLUMNS)) {
					sorting.add(new CollectableSorting(SystemFields.BASE_ALIAS, sEntityName, true, n, true));
				}
				f.setSortingOrder(sorting);
			}
			
			prefsSearchfilter.removeNode();
		}
		catch (Exception e) {
			LOG.error(localeDelegate.getMessage("SearchFilterDelegate.11", "Fehler beim Transformieren des Filters")
					+ ": " + e, e);
			if (result != null) {
				result.setValid(false);
				result.setName((result.getName() != null ? (result.getName()+" - ") : "") 
						+ localeDelegate.getMessage("SearchFilterDelegate.12", "Filter ist ung\u00fcltig"));
			}
		}

		return result;
	}

	public List<TranslationVO> getResources(Integer id) throws CommonBusinessException {
		return searchFilterFacadeRemote.getResources(id);
	}
	
}

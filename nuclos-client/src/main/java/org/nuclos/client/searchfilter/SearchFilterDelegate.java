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
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
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

	private static final Logger log = Logger.getLogger(SearchFilterDelegate.class);

	private static final String PREFS_NODE_SEARCHFILTERS = "searchFilters";
	private static final String PREFS_NODE_GLOBALSEARCHFILTERS = "globalSearchFilters";

	private static final String PREFS_NODE_SEARCHCONDITION = "searchCondition";
	private static final String PREFS_NODE_VISIBLECOLUMNS = "visibleColumns";
	private static final String PREFS_NODE_VISIBLECOLUMNENTITIES = "visibleColumnEntities";
	private static final String PREFS_NODE_SORTINGCOLUMNS = "sortingColumns";

	private static SearchFilterDelegate singleton;

	private final SearchFilterFacadeRemote facade;

	private SearchFilterDelegate() {
		try {
			this.facade = ServiceLocator.getInstance().getFacade(SearchFilterFacadeRemote.class);
		}
		catch (RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * @return the one (and only) instance of SearchFilterDelegate
	 */
	public static synchronized SearchFilterDelegate getInstance() {
		if (singleton == null) {
			singleton = new SearchFilterDelegate();
		}
		return singleton;
	}
	
	public Object update(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants) 
							throws CommonBusinessException {
		try {
			return this.facade.modify(sEntityName, mdvo, mpDependants);
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
			SearchFilter searchFilter = makeSearchFilter(facade.createSearchFilter(insertOrUpdateFilter(filter).getSearchFilterVO()));
			SearchFilterCache.getInstance().addFilter(searchFilter);
		}
		catch (Exception e) {
			String sMessage = CommonLocaleDelegate.getMessage("SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			log.error(sMessage);
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
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SearchFilterDelegate.2", "Der Suchfilter darf von Ihnen nicht ge\u00e4ndert werden."));
		}

		assert newFilter.getSearchFilterVO().getFilterPrefs() != null;

		try {
			SearchFilter searchFilter = makeSearchFilter(facade.modifySearchFilter(insertOrUpdateFilter(newFilter).getSearchFilterVO()));
			SearchFilterCache.getInstance().removeFilter(sOldFilterName, sOwner);
			SearchFilterCache.getInstance().addFilter(searchFilter);
		}
		catch (CommonStaleVersionException e) {
			String sMessage = CommonLocaleDelegate.getMessage("SearchFilterDelegate.4", "Der Suchfilter wurde zwischenzeitlich von einem anderen Benutzer ge\u00e4ndert. Bitte initialisieren Sie den Client.");
			log.info(sMessage);
			throw new NuclosBusinessException(sMessage, e);
		}
		catch (Exception e) {
			String sMessage = CommonLocaleDelegate.getMessage("SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			log.error(sMessage);
			throw new NuclosFatalException(sMessage, e);
		}
	}

	@SuppressWarnings("deprecation")
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

			SearchConditionUtils.putSearchCondition(prefs.node(PREFS_NODE_SEARCHCONDITION), filter.getSearchCondition());

			if (filter instanceof EntitySearchFilter) {
				EntitySearchFilter.writeCollectableEntityFieldsToPreferences(prefs, ((EntitySearchFilter)filter).getVisibleColumns(), PREFS_NODE_VISIBLECOLUMNS, PREFS_NODE_VISIBLECOLUMNENTITIES);
				PreferencesUtils.putStringList(prefs, PREFS_NODE_SORTINGCOLUMNS, ((EntitySearchFilter)filter).getSortingColumnNames());
			}
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			prefs.exportSubtree(baos);
			filter.getSearchFilterVO().setFilterPrefs(baos.toString("UTF-8"));

			return filter;
		}
		catch (Exception e) {
			String sMessage = CommonLocaleDelegate.getMessage("SearchFilterDelegate.1", "Ein Fehler beim Speichern des Suchfilters ist aufgetreten!");
			log.error(sMessage);
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
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SearchFilterDelegate.5", "Der Suchfilter darf von Ihnen nicht gel\u00f6scht werden."));
		}

		if (filter.getSearchFilterVO().isForced()) {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SearchFilterDelegate.6", "Der Suchfilter darf von Ihnen nicht gel\u00f6scht werden, da er bei Ihnen in der Aufgabenleiste (explizit) fixiert ist."));
		}

		try {
			facade.removeSearchFilter(filter.getSearchFilterVO());
			SearchFilterCache.getInstance().removeFilter(filter);
		}
		catch (CommonStaleVersionException e) {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SearchFilterDelegate.7", "Ein Suchfilter konnte nicht gel\u00f6scht werden, da er zwischenzeitlich von einem anderen Benutzer ge\u00e4ndert wurde.\n" +
					"Bitte initialisieren Sie die Client und versuchen es erneut."));
		}
		catch (Exception e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("SearchFilterDelegate.8", "Ein Suchfilter konnte nicht gel\u00f6scht werden"), e);
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
			for (SearchFilterVO filterVO : facade.getAllSearchFilterByUser(sUser)) {
				try {
					SearchFilter sf = makeSearchFilter(filterVO);
					if (sf != null)
						collSearchFilter.add(sf);
				}
				catch (Exception e) {
					log.error(CommonLocaleDelegate.getMessage("SearchFilterDelegate.9", "Ein Suchfilter konnte nicht geladen werden"));
				}
			}
		}
		catch (Exception e) {
			log.error(CommonLocaleDelegate.getMessage("SearchFilterDelegate.9", "Ein Suchfilter konnte nicht geladen werden"));
		}

		return collSearchFilter;
	}

	/**
	 * transforms a SearchFilterVO to a SearchFilter
	 * @param searchFilterVO
	 * @return SearchFilter
	 */
	@SuppressWarnings("deprecation")
	private SearchFilter makeSearchFilter(SearchFilterVO searchFilterVO) {
		SearchFilter result = null;

		try {
			result = new EntitySearchFilter();

			result.setSearchFilterVO(searchFilterVO);

			final ByteArrayInputStream is = new ByteArrayInputStream(searchFilterVO.getFilterPrefs().getBytes("UTF-8"));
			Preferences.importPreferences(is);

			Preferences prefs = Preferences.userRoot().node("org/nuclos/client");

			Preferences prefsSearchfilter;

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

			String sEntityName = searchFilterVO.getEntity();
			
			// SearchFilter properties
			result.setSearchCondition(SearchConditionUtils.getSearchCondition(prefs.node(PREFS_NODE_SEARCHCONDITION),sEntityName));
			
			// EntitySearchFilter properties
			((EntitySearchFilter)result).setVisibleColumns(EntitySearchFilter.readCollectableEntityFieldsFromPreferences(prefs,
					DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName),
					PREFS_NODE_VISIBLECOLUMNS, PREFS_NODE_VISIBLECOLUMNENTITIES));
			((EntitySearchFilter)result).setSortingColumnNames(PreferencesUtils.getStringList(prefs, PREFS_NODE_SORTINGCOLUMNS));

			prefsSearchfilter.removeNode();
		}
		catch (Exception e) {
			log.error(CommonLocaleDelegate.getMessage("SearchFilterDelegate.11", "Fehler beim Transformieren des Filters"));
			if (result != null) {
				result.setValid(false);
				result.setName((result.getName() != null ? (result.getName()+" - ") : "") + CommonLocaleDelegate.getMessage("SearchFilterDelegate.12", "Filter ist ung\u00fcltig"));
			}
		}

		return result;
	}
}

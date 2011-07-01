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
package org.nuclos.client.genericobject.resulttemplate;

import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;


import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Search result template for storing of selected set of result columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 01.00.00
 */
public class SearchResultTemplate {

	private static final String PREFS_KEY_DESCRIPTION = "description";
	private static final String PREFS_NODE_VISIBLECOLUMNS = "visibleColumns";
	private static final String PREFS_NODE_SORTINGCOLUMNS = "sortingColumns";
	private static final String PREFS_KEY_MODULEID = "moduleId";
	private static final String PREFS_NODE_VISIBLECOLUMNSFIXED = "visibleFixedColumns";
	private static final String PREFS_NODE_VISIBLECOLUMNSWITHS = "visibleColumnsWiths";

	private String sName;

	private String sDescription;
	
	/**
	 * list of visible columns
	 */
	private List<String> lstclctefweVisible;
	
	private List<String> lstSortingColumnNames;

	private Map<String, Integer> lstColumnsWidths;
	
	private List<String> lstColumnsFixed;
	
	private Integer iModuleId;
	
	
	
	public SearchResultTemplate(){
	}

	/*public boolean equals(SearchResultTemplate template){
		return ((this.getModuleId() == null && template.getModuleId() == null) || 
					(this.getModuleId() != null && this.getModuleId().equals(template.getModuleId()))) 
				&& ((this.getName() == null && template.getName() == null) || 
						(this.getName() != null && this.getName().equals(template.getName())));
	}*/
	
	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * @return this template's name
	 */
	public String getName() {
		return this.sName;
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * @return a description of this template
	 */
	public String getDescription() {
		return this.sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/**
	 * @todo Doesn't this belong in the search condition?
	 * @return the id of the module, if any
	 */
	public Integer getModuleId() {
		return this.iModuleId;
	}

	public void setModuleId(Integer iModuleId) {
		this.iModuleId = iModuleId;
	}

	/**
	 * @return the columns to be shown in the search result.
	 */
	public List<String> getVisibleColumns() {
		return this.lstclctefweVisible;
	}

	/**
	 * @param lstclctefweVisible the columns to be shown in the search result.
	 */
	public void setVisibleColumns(List<String> lstclctefweVisible) {
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
	 * @todo This is a workaround - the search result template should contain the sorting order, not just the column names.
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

	public Map<String, Integer> getListColumnsWidths() {
		return lstColumnsWidths;
	}

	public void setListColumnsWidths(Map<String, Integer> lstColumnsWidths) {
		this.lstColumnsWidths = lstColumnsWidths;
	}	

	public List<String> getListColumnsFixed() {
		return lstColumnsFixed;
	}

	public void setListColumnsFixed(List<String> lstColumnsFixed) {
		this.lstColumnsFixed = lstColumnsFixed;
	}	
	
	/**
	 * Two <code>SearchResultTemplate</code>s are equal iff their names are equal.
	 * @param o
	 * @return {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SearchResultTemplate)) {
			return false;
		}

		return LangUtils.equals(this.getName(), ((SearchResultTemplate) o).getName());
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.getName());
	}

	public void validate() throws IllegalStateException {
		/** @todo use custom business exception */
		SearchResultTemplate.validate(this.getName());
	}

	public static void validate(String sTemplateName) throws IllegalStateException {
		if (StringUtils.isNullOrEmpty(sTemplateName)) {
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("SearchResultTemplate.3","Der Name darf nicht leer sein."));
		}
		if (sTemplateName.matches(".*\\\\.*")) {
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("SearchResultTemplate.2","Der Name darf keinen Backslash (\"\\\") enthalten."));
		}
	}	

	/**
	 * @param sTemplateName
	 * @return
	 * @precondition sTemplateName != null
	 */
	public static String encoded(String sTemplateName) {
		return sTemplateName.replace('/', '\\');
	}

	/**
	 * @param sTemplateName
	 * @return
	 * @precondition sTemplateName != null
	 */
	public static String decoded(String sTemplateName) {
		return sTemplateName.replace('\\', '/');
	}

	public void put(Preferences prefsParent) throws PreferencesException {
		try {
			this.validate();
		}
		catch (Exception ex) {
			throw new PreferencesException(ex);
		}

		final Preferences prefs = prefsParent.node(encoded(this.getName()));
		final String sDescription = this.getDescription();
		if (StringUtils.isNullOrEmpty(sDescription)) {
			prefs.remove(PREFS_KEY_DESCRIPTION);
		}
		else {
			prefs.put(PREFS_KEY_DESCRIPTION, sDescription);
		}

		PreferencesUtils.putStringList(prefs, PREFS_NODE_VISIBLECOLUMNS, this.getVisibleColumns());

		PreferencesUtils.putStringList(prefs, PREFS_NODE_SORTINGCOLUMNS, this.getSortingColumnNames());

		PreferencesUtils.putStringList(prefs, PREFS_NODE_VISIBLECOLUMNSFIXED, lstColumnsFixed);

		final Integer iModuleId = this.getModuleId();
		final int iModuleIdOr0 = (iModuleId == null) ? 0 : iModuleId.intValue();
		prefs.putInt(PREFS_KEY_MODULEID, iModuleIdOr0);

		PreferencesUtils.putSerializable(prefs, PREFS_NODE_VISIBLECOLUMNSWITHS, lstColumnsWidths);
		
		
		/** @todo prefs.flush */
	}	
	
	/**
	 * reads the search result template with the given name from the preferences.
	 * @param sTemplateName
	 * @param prefsParent the parent node from where to read
	 * @return
	 * @throws NoSuchElementException if there is no template with the given name.
	 * @postcondition result != null
	 */
	@SuppressWarnings("unchecked")
	public static SearchResultTemplate get(Preferences prefsParent, String sTemplateName) throws PreferencesException {
		final String sEncodedTemplateName = encoded(sTemplateName);
		if (!PreferencesUtils.nodeExists(prefsParent, sEncodedTemplateName)) {
			throw new NoSuchElementException(CommonLocaleDelegate.getMessage("SearchResultTemplate.4","Es existiert keine Suchergebnisvorlage mit dem Namen {0}.", sTemplateName));
		}

		final SearchResultTemplate result = new SearchResultTemplate();
		final Preferences prefs = prefsParent.node(sEncodedTemplateName);

		final int iModuleIdOr0 = prefs.getInt(PREFS_KEY_MODULEID, 0);
		final Integer iModuleId = (iModuleIdOr0 == 0) ? null : new Integer(iModuleIdOr0);
		result.setModuleId(iModuleId);

		result.setName(sTemplateName);
		result.setDescription(prefs.get(PREFS_KEY_DESCRIPTION, null));

		result.setVisibleColumns(PreferencesUtils.getStringList(prefs, PREFS_NODE_VISIBLECOLUMNS));

		result.setSortingColumnNames(PreferencesUtils.getStringList(prefs, PREFS_NODE_SORTINGCOLUMNS));

		result.setListColumnsWidths((Map<String,Integer>)PreferencesUtils.getSerializable(prefs, PREFS_NODE_VISIBLECOLUMNSWITHS));
		
		result.setListColumnsFixed(PreferencesUtils.getStringList(prefs, PREFS_NODE_VISIBLECOLUMNSFIXED));
		
		assert result != null;
		return result;
	}

	/**
	 * creates the default search result template with a specific name.
	 * @return new default search result template
	 * @postcondition result.isDefaultTemplate()
	 */
	public static SearchResultTemplate newDefaultTemplate() {
		final SearchResultTemplate result = new SearchResultTemplate() {
			@Override
			public boolean isDefaultTemplate() {
				return true;
			}
		};
		result.setName(CommonLocaleDelegate.getMessage("SearchResultTemplate.1","<Alle>"));
		result.setDescription(CommonLocaleDelegate.getMessage("SearchResultTemplate.5","Standard Suchergebnisvorlage"));
		assert result.isDefaultTemplate();
		return result;
	}

	/**
	 * @return Is this search result template a default template?
	 */
	public boolean isDefaultTemplate() {
		return this.getName() != null && SearchResultTemplate.newDefaultTemplate().getName().equals(this.getName());
	}

}

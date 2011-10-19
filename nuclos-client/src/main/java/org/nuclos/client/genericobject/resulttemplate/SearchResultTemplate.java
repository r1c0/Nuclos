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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Search result template for storing of selected set of result columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: This looks *very* similiar to
 * {@link org.nuclos.client.searchfilter.EntitySearchFilter}!
 * Perhaps we could unify both classes.
 * </p>
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 01.00.00
 */
public class SearchResultTemplate {

	private static final String PREFS_KEY_DESCRIPTION = "description";
	private static final String PREFS_KEY_MODULEID = "moduleId";
	private static final String PREFS_NODE_VISIBLECOLUMNSWITHS = "visibleColumnsWiths";

	/**
	 * New way to save/load column prefs: as {@link org.nuclos.common.collect.collectable.CollectableEntityField}.
	 */
	private static final String PREFS_NODE_VISIBLEENTITYFIELDS = "visibleEntityFields";

	/**
	 * New way to save/load sorting column prefs: as {@link org.nuclos.common.collect.collectable.CollectableSorting}.
	 */
	private static final String PREFS_NODE_COLLECTABLESORTING = "collectableSorting";

	/**
	 * New way to save/load fixed column prefs: as {@link org.nuclos.common.collect.collectable.CollectableEntityField}.
	 */
	private static final String PREFS_NODE_FIXEDENTITYFIELDS = "fixedEntityFields";

	/**
	 * @deprecated Old way to save/load prefs: only columns *names* (String).
	 */
	private static final String PREFS_NODE_VISIBLECOLUMNS = "visibleColumns";

	/**
	 * @deprecated Old way to save/load prefs: only sorting columns *names* (String).
	 */
	private static final String PREFS_NODE_SORTINGCOLUMNS = "sortingColumns";

	/**
	 * @deprecated Old way to save/load prefs: only fixed columns *names* (String).
	 */
	private static final String PREFS_NODE_VISIBLECOLUMNSFIXED = "visibleFixedColumns";

	//

	private String sName;

	private String sDescription;

	/**
	 * list of visible columns
	 */
	private List<CollectableEntityField> visibleFields;

	private List<CollectableSorting> sortingOrder;

	private Map<String, Integer> lstColumnsWidths;

	private List<CollectableEntityField> fixedColumns;

	private final int moduleId;



	public SearchResultTemplate(int moduleId){
		this.moduleId = moduleId;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return this template's name
	 */
	public String getName() {
		return sName;
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
		return moduleId;
	}

	/**
	 * @return the columns to be shown in the search result.
	 */
	public List<CollectableEntityField> getVisibleColumns() {
		return visibleFields;
	}

	/**
	 * @param lstclctefweVisible the columns to be shown in the search result.
	 */
	public void setVisibleColumns(List<CollectableEntityField> lstclctefweVisible) {
		// Make a defensive copy (because collection is unmodifiable). (tp)
		this.visibleFields = new ArrayList<CollectableEntityField>(lstclctefweVisible);
	}

	public List<CollectableSorting> getSortingOrder() {
		return sortingOrder;
	}

	/**
	 * @param lstSortingColumnNames List<String> the names of the columns defining the sorting of the result.
	 * Note that these columns must belong to the main entity.
	 */
	public void setSortingOrder(List<CollectableSorting> lstSortingColumnNames) {
		this.sortingOrder = lstSortingColumnNames;
	}

	public Map<String, Integer> getListColumnsWidths() {
		return lstColumnsWidths;
	}

	public void setListColumnsWidths(Map<String, Integer> lstColumnsWidths) {
		this.lstColumnsWidths = lstColumnsWidths;
	}

	public List<CollectableEntityField> getListColumnsFixed() {
		return fixedColumns;
	}

	public void setFixedColumns(List<CollectableEntityField> fixedColumns) {
		this.fixedColumns = fixedColumns;
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

		PreferencesUtils.putSerializableListXML(prefs, PREFS_NODE_VISIBLEENTITYFIELDS, getVisibleColumns());
		PreferencesUtils.putSerializableListXML(prefs, PREFS_NODE_COLLECTABLESORTING, getSortingOrder());
		PreferencesUtils.putSerializableListXML(prefs, PREFS_NODE_FIXEDENTITYFIELDS, getListColumnsFixed());

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
	public static SearchResultTemplate get(Preferences prefsParent, String sTemplateName) throws PreferencesException {
		final String sEncodedTemplateName = encoded(sTemplateName);
		if (!PreferencesUtils.nodeExists(prefsParent, sEncodedTemplateName)) {
			throw new NoSuchElementException(CommonLocaleDelegate.getMessage(
					"SearchResultTemplate.4","Es existiert keine Suchergebnisvorlage mit dem Namen {0}.", sTemplateName));
		}

		final Preferences prefs = prefsParent.node(sEncodedTemplateName);
		final int iModuleIdOr0 = prefs.getInt(PREFS_KEY_MODULEID, 0);
		final SearchResultTemplate result = new SearchResultTemplate(iModuleIdOr0);

		final MetaDataProvider mdProv = MetaDataClientProvider.getInstance();
		final Integer iModuleId;
		final EntityMetaDataVO entityVO;
		final String entity;
		final CollectableEOEntity cEntity;
		if (iModuleIdOr0 == 0) {
			iModuleId = null;
			entityVO = null;
			entity = null;
			cEntity = null;
		}
		else {
			iModuleId = Integer.valueOf(iModuleIdOr0);
			entityVO = mdProv.getEntity(IdUtils.toLongId(iModuleIdOr0));
			entity = entityVO.getEntity();
			cEntity = new CollectableEOEntity(entityVO, mdProv.getAllEntityFieldsByEntity(entity));
		}

		result.setName(sTemplateName);
		result.setDescription(prefs.get(PREFS_KEY_DESCRIPTION, null));

		if (PreferencesUtils.nodeExists(prefs, PREFS_NODE_VISIBLEENTITYFIELDS)) {
			result.setVisibleColumns((List<CollectableEntityField>) PreferencesUtils.getSerializableListXML(prefs, PREFS_NODE_VISIBLEENTITYFIELDS, true));
		}
		// backward compatibility
		else {
			final List<CollectableEntityField> visible = new ArrayList<CollectableEntityField>();
			for (String n: PreferencesUtils.getStringList(prefs, PREFS_NODE_VISIBLECOLUMNS)) {
				final Pair<String,String> p = StringUtils.getDot(n);
				final CollectableEntityFieldWithEntityForExternal field;
				try {
					if (p.getX().equals("") || p.getX().equals(entity)) {
						field = new CollectableEntityFieldWithEntityForExternal(cEntity, p.getY(), false, true);
					}
					else {
						final EntityMetaDataVO vo = mdProv.getEntity(p.getX());
						final CollectableEOEntity e = new CollectableEOEntity(vo, mdProv.getAllEntityFieldsByEntity(p.getX()));
						field = new CollectableEntityFieldWithEntityForExternal(e, p.getY(), false, true);
					}
					visible.add(field);
				}
				catch (IllegalArgumentException r) {
					// CollectableEntityFieldWithEntityForExternal can throw IllegalArgumentException
					// if the field does not exist any more. In this case the field is not added to
					// visible. (tp)
				}
			}
			result.setVisibleColumns(visible);
		}

		if (PreferencesUtils.nodeExists(prefs, PREFS_NODE_COLLECTABLESORTING)) {
			final List<CollectableSorting> sorting = 
				(List<CollectableSorting>) PreferencesUtils.getSerializableListXML(prefs, PREFS_NODE_COLLECTABLESORTING, true);
			CollectionUtils.removeDublicates(sorting);
			result.setSortingOrder(sorting);
		}
		// backward compatibility
		else {
			final List<CollectableSorting> sorting = new ArrayList<CollectableSorting>();
			for (String n: PreferencesUtils.getStringList(prefs, PREFS_NODE_SORTINGCOLUMNS)) {
				sorting.add(new CollectableSorting(entity, true, n, true));
			}
			result.setSortingOrder(sorting);
		}

		if (PreferencesUtils.nodeExists(prefs, PREFS_NODE_FIXEDENTITYFIELDS)) {
			result.setFixedColumns((List<CollectableEntityField>) PreferencesUtils.getSerializableListXML(prefs, PREFS_NODE_FIXEDENTITYFIELDS, true));
		}
		// backward compatibility
		else {
			final List<CollectableEntityField> fixed = new ArrayList<CollectableEntityField>();
			for (String n: PreferencesUtils.getStringList(prefs, PREFS_NODE_VISIBLECOLUMNSFIXED)) {
				final Pair<String,String> p = StringUtils.getDot(n);
				final CollectableEntityFieldWithEntityForExternal field;
				try {
					if (p.getX().equals("") || p.getX().equals(entity)) {
						field = new CollectableEntityFieldWithEntityForExternal(cEntity, p.getY(), false, true);
					}
					else {
						final EntityMetaDataVO vo = mdProv.getEntity(p.getX());
						final CollectableEOEntity e = new CollectableEOEntity(vo, mdProv.getAllEntityFieldsByEntity(p.getX()));
						field = new CollectableEntityFieldWithEntityForExternal(e, p.getY(), false, true);
					}
					fixed.add(field);
				}
				catch (IllegalArgumentException r) {
					// CollectableEntityFieldWithEntityForExternal can throw IllegalArgumentException
					// if the field does not exist any more. In this case the field is not added to
					// fixed. (tp)
				}
			}
			result.setFixedColumns(fixed);
		}

		result.setListColumnsWidths((Map<String,Integer>)PreferencesUtils.getSerializable(prefs, PREFS_NODE_VISIBLECOLUMNSWITHS));
		assert result != null;
		return result;
	}

	/**
	 * creates the default search result template with a specific name.
	 * @return new default search result template
	 * @postcondition result.isDefaultTemplate()
	 */
	public static SearchResultTemplate newDefaultTemplate(int moduleId) {
		final SearchResultTemplate result = new SearchResultTemplate(moduleId) {
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
		return this.getName() != null && SearchResultTemplate.newDefaultTemplate(moduleId).getName().equals(this.getName());
	}

}

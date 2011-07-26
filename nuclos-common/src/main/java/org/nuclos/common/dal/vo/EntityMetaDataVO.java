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
package org.nuclos.common.dal.vo;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;


/**
 * Entity meta data vo
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik.Stueker</a>
 * @version 01.00.00
 */
public class EntityMetaDataVO extends AbstractDalVOWithVersion<EntityFieldMetaDataVO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String entity;
	private String dbEntity;
	
	private String systemIdPrefix;
	private String menuShortcut;
	
	private Boolean editable;
	private Boolean stateModel;
	private Boolean logBookTracking;
	private Boolean cacheable;
	private Boolean searchable;
	private Boolean treeRelation;
	private Boolean treeGroup;
	private Boolean importExport;
	private Boolean fieldValueEntity;
	private boolean dynamic;

	private String accelerator;
	private Integer acceleratorModifier;
	private String fieldsForEquality;
	private Integer resourceId;
	private String nuclosResource;
	private String localeResourceIdForLabel;
	private String localeResourceIdForDescription;
	private String localeResourceIdForMenuPath;
	private String localeResourceIdForTreeView;
	private String localeResourceIdForTreeViewDescription;
	
	private Collection<Set<String>> uniqueFieldCombinations;
	private Collection<Set<String>> logicalUniqueFieldCombinations;
	
	private String documentPath;
	private String reportFilename;

	public EntityMetaDataVO() {
	    super();
    }
	
	public EntityMetaDataVO(EntityObjectVO eo) {
	    // super(eo);
	    this.setId(eo.getId());
	    this.setEntity(eo.getField("entity", String.class));
	    this.setDbEntity(eo.getField("dbentity", String.class));
	    
	    this.setSystemIdPrefix(eo.getField("systemidprefix", String.class));
	    this.setMenuShortcut(eo.getField("menushortcut", String.class));
	    
	    this.setEditable(eo.getField("editable", Boolean.class));
	    this.setStateModel(eo.getField("usessatemodel", Boolean.class));
	    this.setLogBookTracking(eo.getField("logbooktracking", Boolean.class));
	    this.setCacheable(eo.getField("cacheable", Boolean.class));
	    this.setSearchable(eo.getField("searchable", Boolean.class));
	    this.setTreeRelation(eo.getField("treerelation", Boolean.class));
	    this.setTreeGroup(eo.getField("treegroup", Boolean.class));
	    this.setImportExport(eo.getField("importexport", Boolean.class));
	    this.setFieldValueEntity(eo.getField("fieldvalueentity", Boolean.class));
	    
	    this.setAccelerator(eo.getField("accelerator", String.class));
	    this.setAcceleratorModifier(eo.getField("acceleratormodifier", Integer.class));
	    this.setFieldsForEquality(eo.getField("fieldsforequality", String.class));
	    this.setResourceId(eo.getFieldId("resource") != null ? eo.getFieldId("resource").intValue() : null);
	    this.setLocaleResourceIdForLabel(eo.getField("localeresourcel", String.class));
	    this.setLocaleResourceIdForDescription(eo.getField("localeresourced", String.class));
	    this.setLocaleResourceIdForMenuPath(eo.getField("localeresourcem", String.class));
	    this.setLocaleResourceIdForTreeView(eo.getField("localeresourcetw", String.class));
	    this.setLocaleResourceIdForTreeViewDescription(eo.getField("localeresourcett", String.class));
	    
	    this.setDocumentPath(eo.getField("documentPath", String.class));
	    this.setReportFilename(eo.getField("reportFilename", String.class));
    }

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setDbEntity(String dbEntity) {
		this.dbEntity = dbEntity;
	}

	public void setSystemIdPrefix(String systemIdPrefix) {
		this.systemIdPrefix = systemIdPrefix;
	}

	public void setMenuShortcut(String menuShortcut) {
		this.menuShortcut = menuShortcut;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public void setStateModel(Boolean stateModel) {
		this.stateModel = stateModel;
	}

	public void setLogBookTracking(Boolean logBookTracking) {
		this.logBookTracking = logBookTracking;
	}

	public void setCacheable(Boolean cacheable) {
		this.cacheable = cacheable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public void setTreeRelation(Boolean treeRelation) {
		this.treeRelation = treeRelation;
	}

	public void setTreeGroup(Boolean treeGroup) {
		this.treeGroup = treeGroup;
	}

	public void setImportExport(Boolean importExport) {
		this.importExport = importExport;
	}
	
	public void setFieldValueEntity(Boolean fieldValueEntity) {
		this.fieldValueEntity = fieldValueEntity;
	}

	public void setAccelerator(String accelerator) {
		this.accelerator = accelerator;
	}

	public void setAcceleratorModifier(Integer acceleratorModifier) {
		this.acceleratorModifier = acceleratorModifier;
	}

	public void setFieldsForEquality(String fieldsForEquality) {
		this.fieldsForEquality = fieldsForEquality;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public void setNuclosResource(String nuclosResource) {
		this.nuclosResource = nuclosResource;
	}

	public void setLocaleResourceIdForLabel(String localeResourceIdForLabel) {
		this.localeResourceIdForLabel = localeResourceIdForLabel;
	}

	public void setLocaleResourceIdForDescription(
		String localeResourceIdForDescription) {
		this.localeResourceIdForDescription = localeResourceIdForDescription;
	}

	public void setLocaleResourceIdForMenuPath(String localeResourceIdForMenuPath) {
		this.localeResourceIdForMenuPath = localeResourceIdForMenuPath;
	}

	public void setLocaleResourceIdForTreeView(String localeResourceIdForTreeView) {
		this.localeResourceIdForTreeView = localeResourceIdForTreeView;
	}

	public void setLocaleResourceIdForTreeViewDescription(
		String localeResourceIdForTreeViewDescription) {
		this.localeResourceIdForTreeViewDescription = localeResourceIdForTreeViewDescription;
	}

	public String getEntity() {
		return entity;
	}

	public String getDbEntity() {
		return dbEntity;
	}

	public String getSystemIdPrefix() {
		return systemIdPrefix;
	}

	public String getMenuShortcut() {
		return menuShortcut;
	}

	public Boolean isEditable() {
		return editable;
	}

	public Boolean isStateModel() {
		return stateModel;
	}

	public Boolean isLogBookTracking() {
		return logBookTracking;
	}

	public Boolean isCacheable() {
		return cacheable;
	}

	public Boolean isSearchable() {
		return searchable;
	}

	public Boolean isTreeRelation() {
		return treeRelation;
	}

	public Boolean isTreeGroup() {
		return treeGroup;
	}

	public Boolean isImportExport() {
		return importExport;
	}
	
	public Boolean isFieldValueEntity() {
		return fieldValueEntity;
	}

	public String getAccelerator() {
		return accelerator;
	}

	public Integer getAcceleratorModifier() {
		return acceleratorModifier;
	}

	public String getFieldsForEquality() {
		return fieldsForEquality;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public String getNuclosResource() {
		return nuclosResource;
	}

	public String getLocaleResourceIdForLabel() {
		return localeResourceIdForLabel;
	}

	public String getLocaleResourceIdForDescription() {
		return localeResourceIdForDescription;
	}

	public String getLocaleResourceIdForMenuPath() {
		return localeResourceIdForMenuPath;
	}

	public String getLocaleResourceIdForTreeView() {
		return localeResourceIdForTreeView;
	}

	public String getLocaleResourceIdForTreeViewDescription() {
		return localeResourceIdForTreeViewDescription;
	}
	
	public Collection<Set<String>> getLogicalUniqueFieldCombinations() {
		return logicalUniqueFieldCombinations;
	}

	public void setLogicalUniqueFieldCombinations(
		Collection<Set<String>> logicalUniqueFieldCombinations) {
		this.logicalUniqueFieldCombinations = logicalUniqueFieldCombinations;
	}
	
	public Collection<Set<String>> getUniqueFieldCombinations() {
		return uniqueFieldCombinations;
	}

	public void setUniqueFieldCombinations(
		Collection<Set<String>> uniqueFieldCombinations) {
		this.uniqueFieldCombinations = uniqueFieldCombinations;
	}

	public String getDocumentPath() {
		return documentPath;
	}

	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public String getReportFilename() {
		return reportFilename;
	}

	public void setReportFilename(String reportFilename) {
		this.reportFilename = reportFilename;
	}

	@Override
	public String toString() {
		return getEntity();
	}
	
	public boolean isDynamic() {
    	return dynamic;
    }

	public void setDynamic(boolean dynamic) {
    	this.dynamic = dynamic;
    }

	@Override
	public int hashCode() {
		return this.getEntity().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EntityMetaDataVO) {
			EntityMetaDataVO that = (EntityMetaDataVO)obj;
			return StringUtils.equals(this.getEntity(), that.getEntity());
		}
			
		return false;
	}
	
	
	
}

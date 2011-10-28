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
package org.nuclos.server.masterdata.valueobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.Localizable;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Generic value object representing meta information for master data entities.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 * @deprecated use EntityMetaDataVO
 */
@Deprecated
public class MasterDataMetaVO extends NuclosValueObject implements Localizable {

	private final String sEntityName;
	private final String sDBEntityName;
	private final String sMenuPath;
	private final boolean bSearchable;
	private final boolean bEditable;
	private final String sLabel;
	private final Map<String, ? extends MasterDataMetaFieldVO> mpFields;
	private final boolean bDynamic;
	private final Collection<String> collFieldsForEquality;
	private final boolean bCacheable;
	private final String sTreeView;
	private final String sDescription;
	private final boolean bSystemEntity;
	private final String sResourceName;
	private final String sNuclosResource;
	private final boolean bImportExport;
	private final String sLabelPlural;
	private final Integer iAcceleratorModifier;
	private final String sAccelerator;
	
	private String sResourceIdForLabel;
	private String sResourceIdForLabelPlural;
	private String sResourceIdForMenuPath;
	private String sResourceIdForTreeView;
	private String sResourceIdForTreeViewDescription;
	
	public static final String DYNAMIC_ENTITY_VIEW_PREFIX = "V_DE_";//if you change this value, change the exception text <datasource.validation.dynamic.entity.name.1> too.
	public static final String DYNAMIC_ENTITY_PREFIX = "dyn_";

	/**
	 * constructor to be called by server only
	 * @param iId primary key of underlying database record
	 * @param sEntityName name of master data entity
	 * @param sDBEntityName name of underlying table
	 * @param sMenuPath menu path of underlying table
	 * @param bSearchable should a search screen be shown?
	 * @param bEditable should screen be editable
	 * @param sLabel label of master data entity
	 * @param dateCreatedAt creation date of underlying database record
	 * @param sCreatedBy creator of underlying database record
	 * @param dateChangedAt last changed date of underlying database record
	 * @param sChangedBy last changer of underlying database record
	 * @precondition sEntityName != null
	 */
	public MasterDataMetaVO(Integer iId, String sEntityName, String sDBEntityName, String sMenuPath, boolean bSearchable,
			boolean bEditable, String sLabel, Collection<String> collFieldsForEquality, boolean bCacheable,
			java.util.Date dateCreatedAt, String sCreatedBy, java.util.Date dateChangedAt, String sChangedBy, Integer iVersion,
			Map<String, ? extends MasterDataMetaFieldVO> mpFields, String sTreeView, String sDescription, 
			boolean bSystemEntity, String sResourceName, String sNuclosResource, boolean bImportExport, String sLabelPlural,
			Integer iAccModifier, String accelerator, String sResourceIdForLabel, String sResourceIdForMenuPath, 
			String sResourceIdForLabelPlural, String sResourceIdForTreeView, String sResourceIdForTreeViewDescription) {
		super(iId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		if (sEntityName == null) {
			throw new NullArgumentException("sEntityName");
		}
		this.sEntityName = sEntityName;
		this.sDBEntityName = sDBEntityName;
		this.sMenuPath = sMenuPath;
		this.bSearchable = bSearchable;
		this.bEditable = bEditable;
		this.sLabel = sLabel;
		this.mpFields = mpFields;
		this.bDynamic = false;
		this.collFieldsForEquality = CollectionUtils.<String>emptyIfNull(collFieldsForEquality);
		this.bCacheable = bCacheable;
		this.sTreeView = sTreeView;
		this.sDescription = sDescription;
		this.bSystemEntity = bSystemEntity;
		this.sResourceName = sResourceName;
		this.sNuclosResource = sNuclosResource;
		this.bImportExport = bImportExport;
		this.sLabelPlural = sLabelPlural;
		this.iAcceleratorModifier = iAccModifier;
		this.sAccelerator = accelerator;
		
		this.sResourceIdForLabel = sResourceIdForLabel;
		this.sResourceIdForLabelPlural = sResourceIdForLabelPlural;
		this.sResourceIdForMenuPath = sResourceIdForMenuPath;
		this.sResourceIdForTreeView = sResourceIdForTreeView;
		this.sResourceIdForTreeViewDescription = sResourceIdForTreeViewDescription;
	}

	/**
	 * Constructor used for dynamic entities (by the server only).
	 * @param iId
	 * @param sDBEntityName
	 * @param mpFields
	 */
	public MasterDataMetaVO(Integer iId, String sDBEntityName, Map<String, ? extends MasterDataMetaFieldVO> mpFields) {
		super(iId, null, null, null, null, 1);
		this.sEntityName = getEntityNameFromViewName(sDBEntityName);
		this.sDBEntityName = sDBEntityName;
		this.sMenuPath = null;
		this.bSearchable = false;
		this.bEditable = false;
		this.sLabel = this.sEntityName;
		this.mpFields = mpFields;
		this.bDynamic = true;
		this.collFieldsForEquality = Collections.emptySet();
		this.bCacheable = false;
		this.sTreeView = null;
		this.sDescription = null;
		this.bSystemEntity = false;
		this.sResourceName = null;
		this.sNuclosResource = null;
		this.bImportExport = true;
		this.sLabelPlural = null;
		this.iAcceleratorModifier = 0;
		this.sAccelerator = null;
	}

	public static String getEntityNameFromViewName(String sView) {
		return DYNAMIC_ENTITY_PREFIX + sView.substring(DYNAMIC_ENTITY_VIEW_PREFIX.length()).toLowerCase();
	}

	/**
	 * @return entity name
	 * @postcondition result != null
	 */
	public String getEntityName() {
		return this.sEntityName;
	}

	/**
	 * @return table name
	 */
	public String getDBEntity() {
		return this.sDBEntityName;
	}

	/**
	 * @return TreeView label
	 */
	public String getTreeView() {
		return this.sTreeView;
	}
	
	/**
	 * @return TreeView description
	 */
	public String getDescription(){
		return this.sDescription;
	}
	
	/**
	 * @return TreeView resource name
	 */
	public String getResourceName() {
		return this.sResourceName;
	}
	
	public String getNuclosResource() {
		return this.sNuclosResource;
	}
	
	public boolean getIsImportExport() {
		return this.bImportExport;
	}
	
	/**
	 * @return menu path May be null.
	 */
	public String getMenuPath() {
		return this.sMenuPath;
//		final List<String> result;
//		if (this.sMenuPath == null) {
//			result = null;
//		}
//		else {
//			result = new ArrayList<String>();
//			final StringTokenizer stMenupath = new StringTokenizer(this.sMenuPath, "\\");
//			while (stMenupath.hasMoreTokens()) {
//				result.add(stMenupath.nextToken());
//			}
//		}
//		return result;
	}

	/**
	 * @return Should a search screen be shown for this entity?
	 */
	public boolean isSearchable() {
		return this.bSearchable;
	}

	/**
	 * @return Is this entity editable - that means: Is updating the data allowed?
	 */
	public boolean isEditable() {
		return this.bEditable;
	}

	/**
	 * @return the label for this entity.
	 */
	public String getLabel() {
		return this.sLabel;
	}

	public Collection<String> getFieldsForEquality() {
		if (this.collFieldsForEquality == null || this.collFieldsForEquality.size() == 0) {
			return this.getFieldNames();
		}
		return this.collFieldsForEquality;
	}

	/**
	 * @return is this entity a system entity?
	 */
	public boolean isSystemEntity() {
		return this.bSystemEntity;
	}
	
	public String getLabelPlural() {
		return this.sLabelPlural;
	}
	
	/**
	 * get meta data for field name
	 * @param sFieldName field name
	 * @return master data meta field vo
	 */
	public MasterDataMetaFieldVO getField(String sFieldName) {
		return this.mpFields.get(sFieldName);
	}

	/**
	 * get meta data for field id
	 * @param iFieldId field name
	 * @return master data meta field vo
	 */
	public MasterDataMetaFieldVO getFieldById(Integer iFieldId) {
		for (String sFieldName : mpFields.keySet()) {
			final MasterDataMetaFieldVO result = mpFields.get(sFieldName);

			if (iFieldId.equals(result.getId())) {
				return result;
			}
		}
		return null;
	}


	public MasterDataMetaFieldVO getFieldReferencing(String entity) {
		MasterDataMetaFieldVO refField = null;
		for (MasterDataMetaFieldVO field : mpFields.values()) {
			if (entity.equals(field.getForeignEntity())) {
				if (refField != null) {
					throw new IllegalArgumentException("Not unique");
				}
				refField = field;
			}
		}
		return refField;
	}	

	/**
	 * get all fields for master data meta record
	 * @return sorted list of fields
	 */
	public List<MasterDataMetaFieldVO> getFields() {
		final List<MasterDataMetaFieldVO> result = new ArrayList<MasterDataMetaFieldVO>(mpFields.values());
		Collections.sort(result, new Comparator<MasterDataMetaFieldVO>() {
			@Override
			public int compare(MasterDataMetaFieldVO vo1, MasterDataMetaFieldVO vo2) {
				Integer i1 = new Integer(0);
				Integer i2 = new Integer(0);
				if(vo1.getId() != null) {
					i1 = new Integer(vo1.getId());
				}
				if(vo2.getId() != null) {
					i2 = new Integer(vo2.getId());
				}
				return  i1.compareTo(i2);
			}
		});
		return result;
	}

	/**
	 * get the fields for master data meta record, which are marked for logging.
	 * @return list of fields, possibly empty
	 */
	public List<MasterDataMetaFieldVO> getFieldsForLogging() {
		final List<MasterDataMetaFieldVO> result = new ArrayList<MasterDataMetaFieldVO>();
		for (String sFieldName : mpFields.keySet()) {
			final MasterDataMetaFieldVO mdmetafieldvo = mpFields.get(sFieldName);
			if (mdmetafieldvo.getLogToLogbook()) {
				result.add(mdmetafieldvo);
			}
		}

		return result;
	}

	/**
	 * get the fields for master data meta record, which are marked for logging.
	 * @return list of fields, possibly empty
	 */
	public List<MasterDataMetaFieldVO> getInvariantFields() {
		final List<MasterDataMetaFieldVO> result = new ArrayList<MasterDataMetaFieldVO>();
		for (MasterDataMetaFieldVO field : mpFields.values()) {
			if (field.isInvariant()) {
				result.add(field);
			}
		}

		return result;
	}

	/**
	 * get all field names
	 * @return collection of field names
	 */
	public Set<String> getFieldNames() {
		return new HashSet<String>(mpFields.keySet());
	}
	
	/**
	 * get all field names which make a masterdata record unique
	 * @return collection of field names
	 */
	public Set<String> getUniqueFieldNames() {
		Set<String> stUniqueField = new HashSet<String>();
		for (String sFieldName : mpFields.keySet()) {
			if (mpFields.get(sFieldName).isUnique()) {
				stUniqueField.add(sFieldName);
			}
		}
		
		return stUniqueField;
	}

	public boolean isDynamic() {
		return bDynamic;
	}

	/**
	 * @return Is this entity cacheable?
	 */
	public boolean isCacheable() {
		return this.bCacheable;
	}
	
	
	public String getResourceSIdForLabel() {
		return this.sResourceIdForLabel;
	}
	
	public void setResourceSIdForLabel(String sResourceId) {
		this.sResourceIdForLabel = sResourceId;
	}
	
	public String getResourceSIdForLabelPlural() {
		return this.sResourceIdForLabelPlural;
	}
	
	public void setResourceSIdForLabelPlural(String sResourceId) {
		this.sResourceIdForLabelPlural = sResourceId;
	}
	
	public String getResourceSIdForMenuPath() {
		return this.sResourceIdForMenuPath;
	}
	
	public void setResourceSIdForMenuPath(String sResourceId) {
		this.sResourceIdForMenuPath = sResourceId;
	}
	
	public String getResourceSIdForTreeView() {
		return this.sResourceIdForTreeView;
	}
	
	public void setResourceSIdForTreeView(String sResourceId) {
		this.sResourceIdForTreeView = sResourceId;
	}
	
	public String getResourceSIdForTreeViewDescription() {
		return this.sResourceIdForTreeViewDescription;
	}
	
	public void setResourceSIdForTreeViewDescription(String sResourceId) {
		this.sResourceIdForTreeViewDescription = sResourceId;
	}
	
	public Integer getAcceleratorModifier() {
		return this.iAcceleratorModifier;		
	}
	
	public String getAccelerator() {
		return this.sAccelerator;
	}

	@Override
	public String toString() {		
		return getEntityName() != null ? getEntityName() : getLabel();
	}

	@Override // from Localizable
	public String getResourceId() {
		return getResourceSIdForLabel();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MasterDataMetaVO) {
			MasterDataMetaVO that = (MasterDataMetaVO)obj;
			return StringUtils.equals(sEntityName, that.getEntityName());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return sEntityName.hashCode() + 3971;
	}

}	// class MasterDataMetaVO

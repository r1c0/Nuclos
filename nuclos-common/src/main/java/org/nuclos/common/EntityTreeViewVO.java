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
package org.nuclos.common;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common2.LangUtils;

/**
 * VO object used for entity object tree view representation persistence.
 * <p>
 * There is one instance of this class for every subform of a (base) entity.
 * This means that this class is used to customize subform entities only.
 * </p><p>
 * On the server side persistence will be performed by 
 * {@link org.nuclos.server.masterdata.ejb3.MetaDataFacadeBean#createOrModifyEntity}.
 * DB table used is 't_md_entity_subnodes'.
 * </p><p>
 * On the client side, the configuration is done with help of
 * {@link org.nuclos.client.wizard.steps.NuclosEntityTreeValueStep}.
 * </p><p>
 * TODO: This representation does not contain the Nuclos standard fields like
 * NuclosEOField.CHANGEDBY. 
 * </p>
 * @author Thomas Pasch (javadocs)
 */
public class EntityTreeViewVO implements Serializable, Comparable<EntityTreeViewVO>, HasId<Long> {
	
	public static final String INTID = "intid";
	
	public static final String ENTITY_FIELD = "originentityid";
	
	public static final String SUBFORM_ENTITY_FIELD = "entity";
	
	public static final String SUBFORM2ENTITY_REF_FIELD = "field";
	
	public static final String FOLDERNAME_FIELD = "foldername";
	
	public static final String ACTIVE_FIELD = "active";
	
	public static final String SORTORDER_FIELD = "sortOrder";
	
	//
	
	public static final String SUBNODES_TABLE = "T_MD_ENTITY_SUBNODES";
	
	public static final String ENTITY_COLUMN = "INTID_T_MD_ENTITY";
	
	public static final String SUBFORM_ENTITY_COLUMN = "STRENTITY";
	
	public static final String SUBFORM2ENTITY_REF_COLUMN = "STRFIELD";
	
	public static final String FOLDERNAME_COLUMN = "STR_LOCALERESOURCE_FN";
	
	public static final String ACTIVE_COLUMN = "BLNACTIVE";
	
	public static final String SORTORDER_COLUMN = "INTSORTORDER";
	
	/**
	 * Primary key (intid) in DB table t_md_entity.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.2.0 
	 */
	private Long intId;
	
	/**
	 * Reference to (user defined) entity meta data in DB table t_md_entity of the 
	 * (base) entity this subform tree view representation is defined for.
	 */
	private Long originentityid;
	
	/**
	 * Name of the subform entity the (base) entity is linked to.
	 */
	private String entity;
	
	/**
	 * Name of the field in the <em>subform</em> entity that denotes the reference to
	 * the base entity.
	 */
	private String field;
	
	/**
	 * Display name ('Ordnername') in the <em>base</em> entity that
	 * is displayed as parent node for all subform entities. If null, this node is 
	 * omitted (and the subform entities are displayed as direct subnodes of the base
	 * entity). 
	 * <p>
	 * At present this field cannot be localized, i.e. it is always displayed in
	 * verbatim (no translation)!
	 * </p>
	 */
	private String foldername;
	
	/**
	 * Flag indicating that this subform entities are displayed in the tree view.
	 * 
	 * @author Thomas Pasch
	 * @since 3.1.01, NUCLOSINT-1176
	 */
	private Boolean active;
	
	/**
	 * Sort order between different subforms. 
	 * 
	 * @author Thomas Pasch
	 * @since 3.1.01, NUCLOSINT-1176
	 */
	private Integer sortOrder;
	
	public EntityTreeViewVO(Long intId, Long originentityid, String entity, String field,
		String foldername, Boolean active, Integer sortOrder) {
		this.intId = intId;
		this.originentityid = originentityid;
		this.entity = LangUtils.nullIfBlank(entity);
		this.field = LangUtils.nullIfBlank(field);
		this.foldername = LangUtils.nullIfBlank(foldername);		
		this.active = active;
		this.sortOrder = sortOrder;
		
		makeConsistent();
	}

	/**
	 * @deprecated For serialization only.
	 */
	public EntityTreeViewVO() {
	}
	
	public final void makeConsistent() {
		// active and sortOrder are new fields. 
		// Hence we need a way to default them. (Thomas Pasch)
		if (active == null) {
			active = Boolean.valueOf(!StringUtils.isBlank(field) && !StringUtils.isBlank(foldername));
		}
		
		// ensure that only sensible instances are active
		if (StringUtils.isBlank(entity) || StringUtils.isBlank(field)) {
			active = Boolean.FALSE;
		}
	}
	
	public Long getId() {
		return intId;
	}
	
	public void setId(Long intId) {
		this.intId = intId;
	}
	
	public Long getOriginentityid() {
		return originentityid;
	}
	
	public void setOriginentityid(Long originentityid) {
		this.originentityid = originentityid;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = LangUtils.nullIfBlank(entity);
	}
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = LangUtils.nullIfBlank(field);
	}
	
	public String getFoldername() {
		return foldername;
	}
	
	public void setFoldername(String foldername) {
		this.foldername = LangUtils.nullIfBlank(foldername);
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	@Override
	public String toString() {
		final ToStringBuilder b = new ToStringBuilder(this);
		b.append(intId).append(originentityid).append(entity).append(foldername);
		b.append(active).append(sortOrder);
		return b.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EntityTreeViewVO)) return false;
		final EntityTreeViewVO other = (EntityTreeViewVO) o;
		return originentityid.equals(other.originentityid) 
			&& ObjectUtils.equals(entity, other.entity) 
			&& ObjectUtils.equals(field, other.field);
	}
	
	@Override 
	public int hashCode() {
		int result = 6261;
		result += 7 * originentityid.hashCode();
		if (entity != null) result += 11 * entity.hashCode();
		if (field != null) result += 13 * field.hashCode();
		return result;
	}

	/**
	 * This implementation is <em>not</em> consistent with equals.
	 * You have been warned!
	 */
	@Override
	public int compareTo(EntityTreeViewVO o) {
		int result = 0;
		result = LangUtils.compareComparables(sortOrder, o.sortOrder);
		if (result == 0) {
			result = LangUtils.compareComparables(originentityid, o.originentityid);
			if (result == 0) {
				result = LangUtils.compareComparables(entity, o.entity);
				if (result == 0) {
					result = LangUtils.compareComparables(field, o.field);
				}
			}
		}
		return result;
	}
	
}

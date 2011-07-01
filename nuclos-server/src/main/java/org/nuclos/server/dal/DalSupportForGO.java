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
package org.nuclos.server.dal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

public class DalSupportForGO {

	private static GenericObjectFacadeLocal goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);

	/**
	 *
	 * @param genericObjectId
	 * @return
	 */
	public static JdbcEntityObjectProcessor getEntityObjectProcesserForGenericObject(Integer genericObjectId) {
		try {
			String sEntityName = Modules.getInstance().getEntityNameByModuleId(goFacade.getModuleContainingGenericObject(genericObjectId));
			return NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);
		} catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 *
	 * @param moduleId
	 * @return
	 */
	public static JdbcEntityObjectProcessor getEntityObjectProcessor(Integer moduleId) {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(moduleId)).getEntity());
	}

	/**
	 *
	 * @param genericObjectId
	 * @return
	 */
	public static EntityObjectVO getEntityObject(Integer genericObjectId) {
		return getEntityObjectProcesserForGenericObject(genericObjectId).getByPrimaryKey(genericObjectId.longValue());
	}

	/**
	 *
	 * @param goVO
	 * @param module
	 * @return
	 */
	public static EntityObjectVO getEntityObject(Integer genericObjectId, Integer moduleId) {
		return getEntityObjectProcessor(moduleId).getByPrimaryKey(LangUtils.convertId(genericObjectId));
	}

	/**
	 *
	 * @param genericObjectId
	 * @param module
	 * @return
	 */
	public static GenericObjectVO getGenericObject(Integer genericObjectId, Integer moduleId) {
		return getGenericObjectVO(getEntityObject(genericObjectId, moduleId));
	}

	/**
	 *
	 * @param genericObjectId
	 * @return
	 */
	public static GenericObjectVO getGenericObject(Integer genericObjectId) {
		return getGenericObjectVO(getEntityObject(genericObjectId));
	}

	/**
	 *
	 * @param attributeId
	 * @return
	 */
	public static String getEntityFieldFromAttribute(Integer attributeId) {
		return AttributeCache.getInstance().getAttribute(attributeId).getName();
	}

	/**
	 *
	 * @param attributeId
	 * @return
	 */
	public static Class<?> getClassFromAttribute(Integer attributeId) {
		return AttributeCache.getInstance().getAttribute(attributeId).getJavaClass();
	}

	/**
	 *
	 * @param eo
	 * @param field
	 * @return
	 */
	public static DynamicAttributeVO getDynamicAttributeVO(EntityObjectVO eo, String field) {
		Integer iAttributeId = MetaDataServerProvider.getInstance().getEntityField(eo.getEntity(), field).getId().intValue();
		return new DynamicAttributeVO(iAttributeId, LangUtils.convertId(eo.getFieldIds().get(field)), eo.getFields().get(field));
	}

	/**
	 *
	 * @param attributeId
	 * @return
	 */
	public static boolean isEntityFieldForeign(Integer attributeId) {
		 return AttributeCache.getInstance().getAttribute(attributeId).getExternalEntity()!=null;
	}

	/**
	 *
	 * @param attributeId
	 * @param value
	 * @return
	 */
	public static String convertToCanonicalAttributeValue(Integer attributeId, Object value) {
		return convertToCanonicalAttributeValue(getClassFromAttribute(attributeId), value);
	}

	/**
	 *
	 * @param clzz
	 * @param value
	 * @return
	 */
	public static String convertToCanonicalAttributeValue(Class<?> clzz, Object value) {
		return DynamicAttributeVO.getCanonicalFormat(clzz).format(value);
	}

	/**
	 *
	 * @param attributeId
	 * @param sCanonicalValue
	 * @return
	 */
	public static Object convertFromCanonicalAttributeValue(Integer attributeId, String sCanonicalValue) {
		return convertFromCanonicalAttributeValue(getClassFromAttribute(attributeId), sCanonicalValue);
	}

	/**
	 *
	 * @param javaClass
	 * @param sCanonicalValue
	 * @return
	 */
	public static Object convertFromCanonicalAttributeValue(Class<?> clzz, String sCanonicalValue) {
		try {
			return DynamicAttributeVO.getCanonicalFormat(clzz).parse(sCanonicalValue);
		} catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 *
	 * @param go
	 * @return
	 */
	public static EntityObjectVO wrapGenericObjectVO(GenericObjectVO go) {
		EntityObjectVO eo = new EntityObjectVO();

		eo.setEntity(Modules.getInstance().getEntityNameByModuleId(go.getModuleId()));

		eo.setId(LangUtils.convertId(go.getId()));
		eo.setCreatedBy(go.getCreatedBy());
		eo.setCreatedAt(InternalTimestamp.toInternalTimestamp(go.getCreatedAt()));
		eo.setChangedBy(go.getChangedBy());
		eo.setChangedAt(InternalTimestamp.toInternalTimestamp(go.getChangedAt()));
		eo.setVersion(go.getVersion());

		eo.initFields(go.getAttributes().size(), go.getAttributes().size());
		for (DynamicAttributeVO attr : go.getAttributes()) {
			final String field = AttributeCache.getInstance().getAttribute(attr.getAttributeId()).getName();
			if (attr.isRemoved()) {
				eo.getFields().remove(field);
				eo.getFieldIds().remove(field);
			} else {
				eo.getFields().put(field, attr.getValue());
				if (attr.getValueId() != null) {
					eo.getFieldIds().put(field, LangUtils.convertId(attr.getValueId()));
				}
			}
		}
		eo.getFields().put(NuclosEOField.LOGGICALDELETED.getMetaData().getField(), go.isDeleted());

		return eo;
	}

	/**
	 *
	 * @param eo
	 * @return
	 */
	public static GenericObjectVO getGenericObjectVO(EntityObjectVO eo) {

		Set<Integer> setLoadedAttributeIds = null;//new HashSet<Integer>();
//		for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eo.getEntity()).values()) {
//			setLoadedAttributeIds.add(DalUtils.convertId(efMeta.getId()));
//		}

		final NuclosValueObject nvo;
		if (eo.getId() != null) {
			nvo = new NuclosValueObject(LangUtils.convertId(eo.getId()), eo.getCreatedAt(), eo.getCreatedBy(), eo.getChangedAt(), eo.getChangedBy(), eo.getVersion());
		}
		else {
			nvo = new NuclosValueObject();
		}
		final GenericObjectVO go = new GenericObjectVO(nvo, Modules.getInstance().getModuleIdByEntityName(eo.getEntity()), null, null, setLoadedAttributeIds, (Boolean) eo.getFields().get(NuclosEOField.LOGGICALDELETED.getMetaData().getField()));

		Collection<DynamicAttributeVO> attrVOList = new ArrayList<DynamicAttributeVO>();
		
		Set<String> setFields = new HashSet<String>();
		setFields.addAll(eo.getFields().keySet());
		setFields.addAll(eo.getFieldIds().keySet());

		for (String field : setFields) {
			Object fieldValue = eo.getFields().get(field);
			Long idValue = eo.getFieldIds().get(field);
			if(MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eo.getEntity()).containsKey(field)) {
				DynamicAttributeVO attr = new DynamicAttributeVO(LangUtils.convertId(MetaDataServerProvider.getInstance().getEntityField(eo.getEntity(), field).getId()),
					LangUtils.convertId(idValue), fieldValue);
	
				attrVOList.add(attr);
			}
		}

		attrVOList.add(new DynamicAttributeVO(NuclosEOField.CREATEDAT.getMetaData().getId().intValue(), null, eo.getCreatedAt()));
		attrVOList.add(new DynamicAttributeVO(NuclosEOField.CREATEDBY.getMetaData().getId().intValue(), null, eo.getCreatedBy()));
		attrVOList.add(new DynamicAttributeVO(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue(), null, eo.getChangedAt()));
		attrVOList.add(new DynamicAttributeVO(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue(), null, eo.getChangedBy()));

		go.setAttributes(attrVOList);

		return go;
	}

	/**
	 *
	 * @param efMeta
	 * @param permissions
	 * @param values
	 * @return
	 */
	public static AttributeCVO getAttributeCVO(EntityFieldMetaDataVO efMeta, Map<Integer, Permission> permissions) {

		Object defaultValue;

		try {
			defaultValue = DynamicAttributeVO.getCanonicalFormat(Class.forName(efMeta.getDataType())).parse(efMeta.getDefaultValue());
		}
		catch (ClassNotFoundException ex) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("vofactory.exception.1", efMeta.getDataType()), ex);//"Unbekannter Datentyp: " + dbo.getDataType() + ".", ex);
		}
		catch (CommonValidationException ex) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("vofactory.exception.2", efMeta.getField(), efMeta.getDefaultValue()), ex);//"Ung\u00fcltiger Wert f\u00fcr Attribut " + dbo.getName() + ": " + dbo.getDefaultValue() + ".", ex);
		}

		final AttributeCVO vo = new AttributeCVO(
			new NuclosValueObject(
				LangUtils.convertId(efMeta.getId()),
				efMeta.getCreatedAt(),
				efMeta.getCreatedBy(),
				efMeta.getChangedAt(),
				efMeta.getChangedBy(),
				efMeta.getVersion()),
			LangUtils.convertId(efMeta.getFieldGroupId()),
			null /** TODO: FieldGroupName*/,
			efMeta.getCalcFunction(),
			efMeta.getField(),
			efMeta.getFallbacklabel(),
			null /** TODO: Description*/,
			efMeta.getDataType(),
			efMeta.getScale(),
			efMeta.getPrecision(),
			efMeta.getFormatInput(),
			efMeta.getFormatOutput(),
			efMeta.isNullable(),
			efMeta.isSearchable(),
			efMeta.isModifiable(),
			efMeta.isInsertable(),
			efMeta.isLogBookTracking(),
			NuclosEOField.getByField(efMeta.getField()) != null,
			efMeta.isShowMnemonic(),
			(efMeta.getForeignEntity() != null ? LangUtils.convertId(efMeta.getDefaultForeignId()) : null),
			defaultValue,
			efMeta.getSortorderASC(),
			efMeta.getSortorderDESC(),
			efMeta.getForeignEntity(),
			efMeta.getForeignEntityField(),
			(permissions == null ? new HashMap<Integer, Permission>() : permissions),
			efMeta.getLocaleResourceIdForLabel(),
			efMeta.getLocaleResourceIdForDescription());

		return vo;
	}
}

//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.dal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * Client-side functions to transform a GenericObjectVO into a EntityObjectVO and vice versa.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class DalSupportForGO {
	
	private static final Logger LOG = Logger.getLogger(DalSupportForGO.class);

	private DalSupportForGO() {
		// Never invoked.
	}

	public static GenericObjectWithDependantsVO getGenericObjectWithDependantsVO(EntityObjectVO eo, CollectableEOEntity meta) {
		final GenericObjectVO base = getGenericObjectVO(eo, meta);
		final GenericObjectWithDependantsVO result = new GenericObjectWithDependantsVO(base, eo.getDependants());
		return result;
	}
	
	public static GenericObjectVO getGenericObjectVO(EntityObjectVO eo, CollectableEOEntity meta) {
		Set<Integer> setLoadedAttributeIds = null;
		final NuclosValueObject nvo;
		if (eo.getId() != null) {
			nvo = new NuclosValueObject(LangUtils.convertId(eo.getId()), eo.getCreatedAt(), eo.getCreatedBy(),
					eo.getChangedAt(), eo.getChangedBy(), eo.getVersion());
		} else {
			nvo = new NuclosValueObject();
		}
		final GenericObjectVO go = new GenericObjectVO(nvo, IdUtils.unsafeToId(meta.getMeta().getId()), null, null,
				setLoadedAttributeIds, (Boolean) eo.getFields().get(
						NuclosEOField.LOGGICALDELETED.getMetaData().getField()));

		Collection<DynamicAttributeVO> attrVOList = new ArrayList<DynamicAttributeVO>();

		Set<String> setFields = new HashSet<String>();
		setFields.addAll(eo.getFields().keySet());
		setFields.addAll(eo.getFieldIds().keySet());

		final MetaDataClientProvider mdcp = MetaDataClientProvider.getInstance();
		for (String field : setFields) {
			Object fieldValue = eo.getFields().get(field);
			Long idValue = eo.getFieldIds().get(field);
			if (mdcp.getAllEntityFieldsByEntity(eo.getEntity()).containsKey(field)) {
				DynamicAttributeVO attr = new DynamicAttributeVO(LangUtils.convertId(mdcp.getEntityField(
						eo.getEntity(), field).getId()), LangUtils.convertId(idValue), fieldValue);
				attrVOList.add(attr);
			}
		}
		attrVOList.add(new DynamicAttributeVO(
				NuclosEOField.CREATEDAT.getMetaData().getId().intValue(), null, eo.getCreatedAt()));
		attrVOList.add(new DynamicAttributeVO(
				NuclosEOField.CREATEDBY.getMetaData().getId().intValue(), null, eo.getCreatedBy()));
		attrVOList.add(new DynamicAttributeVO(
				NuclosEOField.CHANGEDAT.getMetaData().getId().intValue(), null, eo.getChangedAt()));
		attrVOList.add(new DynamicAttributeVO(
				NuclosEOField.CHANGEDBY.getMetaData().getId().intValue(), null, eo.getChangedBy()));
		go.setAttributes(attrVOList);
		return go;
	}
	
	public static EntityObjectVO wrapGenericObjectVO(GenericObjectVO go, CollectableEOEntity meta) {
		final EntityMetaDataVO metaVo = meta.getMeta();
		final EntityObjectVO eo = new EntityObjectVO();
		eo.setEntity(metaVo.getEntity());
		eo.setId(LangUtils.convertId(go.getId()));
		eo.setCreatedBy(go.getCreatedBy());
		eo.setCreatedAt(InternalTimestamp.toInternalTimestamp(go.getCreatedAt()));
		eo.setChangedBy(go.getChangedBy());
		eo.setChangedAt(InternalTimestamp.toInternalTimestamp(go.getChangedAt()));
		eo.setVersion(go.getVersion());

		eo.initFields(go.getAttributes().size(), go.getAttributes().size());
		for (DynamicAttributeVO attr : go.getAttributes()) {
			final Long fieldId = IdUtils.toLongId(attr.getId());
			LOG.info("fieldId " + fieldId + " for " + attr + " (id=" + attr.getId() + ", attrId=" 
					+ attr.getAttributeId() + ") in " + meta.getMeta().getEntity());
			// TODO: ???
			if (fieldId == null) continue;
			final String field = MetaDataClientProvider.getInstance().getEntityField(
					metaVo.getEntity(), fieldId).getField();
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
	
}

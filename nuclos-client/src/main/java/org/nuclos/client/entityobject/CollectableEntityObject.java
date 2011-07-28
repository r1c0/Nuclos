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
package org.nuclos.client.entityobject;

import java.util.Map;

import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Removable;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class CollectableEntityObject extends CollectableMasterData implements Removable {

	private CollectableEOEntity ceoe;
	
	private final Map<String, CollectableField> mpFields = CollectionUtils.newHashMap();

	private EntityObjectVO vo;

	public CollectableEntityObject(CollectableMasterDataEntity clcte, MasterDataVO mdvo) {
		super(clcte, mdvo);
	}

	public CollectableEntityObject(CollectableEOEntity cee, EntityObjectVO vo) {
		super(null, null);
		this.ceoe = cee;
		this.vo = vo;
	}

	public CollectableEOEntity getCollectableEOEntity(){
		return ceoe;
	}

	@Override
	public Object getValue(String sFieldName) {
		return this.vo.getField(sFieldName, Object.class);
	}

	@Override
	public Object getValueId(String sFieldName) {
		return this.vo.getFieldIds().get(sFieldName);
	}

	@Override
	public CollectableField getField(String sFieldName)	throws CommonFatalException {
		CollectableField field = mpFields.get(sFieldName);
		if(field == null) {
			field = new CollectableEntityObjectField(sFieldName, this);
			mpFields.put(sFieldName, field);
		}

		return field;
	}

	@Override
	public Object getId() {
		if(vo.getId() == null)
			return null;
		return vo.getId().intValue();
	}

	@Override
	public String getIdentifierLabel() {
		return ceoe.getIdentifierFieldName();
	}

	@Override
	public int getVersion() {
		return vo.getVersion().intValue();
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		mpFields.put(sFieldName, clctfValue);
		vo.getFields().put(sFieldName, clctfValue.getValue());
		if(clctfValue.isIdField()) {
			vo.getFieldIds().put(sFieldName, IdUtils.toLongId(clctfValue.getValueId()));
		}
		vo.flagUpdate();
	}

	@Override
	public boolean isMarkedRemoved() {
		return vo.isFlagRemoved();
	}

	@Override
	public void markRemoved() {
		vo.flagRemove();
	}

	public EntityObjectVO getEntityObjectVO() {
		return this.vo;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getEntityObjectVO());
		result.append(",meta=").append(getCollectableEOEntity());
		result.append(",mdVo=").append(getMasterDataCVO());
		result.append(",dep=").append(getDependantMasterDataMap());
		result.append(",cdep=").append(getDependantCollectableMasterDataMap());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	/**
	 * inner class MakeCollectable: makes a <code>MasterDataVO</code> <code>Collectable</code>.
	 */
	public static class MakeCollectable implements Transformer<EntityObjectVO, CollectableEntityObject> {
		final CollectableEOEntity clctmde;

		public MakeCollectable(CollectableEntityProvider clcteprovider, String sEntityName) {
			this((CollectableEOEntity) clcteprovider.getCollectableEntity(sEntityName));
		}

		public MakeCollectable(CollectableEOEntity clctmde) {
			this.clctmde = clctmde;
		}

		@Override
		public CollectableEntityObject transform(EntityObjectVO mdvo) {
			return new CollectableEntityObject(this.clctmde, mdvo);
		}

	}


	public static class ExtractAbstractCollectableVO implements Transformer<AbstractCollectable, EntityObjectVO> {
		@Override
		public EntityObjectVO transform(AbstractCollectable clctmd) {
			if(clctmd instanceof CollectableEntityObject){
				CollectableEntityObject ceo = (CollectableEntityObject)clctmd;
				DependantMasterDataMap depmdmp = ceo.getDependantMasterDataMap();
				EntityObjectVO mdVO = ceo.getEntityObjectVO();

				mdVO.setDependants(depmdmp);

				return mdVO;
			}
			else{
				return new EntityObjectVO();
			}

		}
	}

	/**
	 * inner class ExtractMasterDataVO: the inverse operation of <code>MakeCollectable</code>.
	 */
	public static class ExtractMasterDataVO implements Transformer<CollectableEntityObject, EntityObjectVO> {
		@Override
		public EntityObjectVO transform(CollectableEntityObject clctmd) {
			DependantMasterDataMap depmdmp = clctmd.getDependantMasterDataMap();
			EntityObjectVO mdVO = clctmd.getEntityObjectVO();

			mdVO.setDependants(depmdmp);
			return mdVO;
		}
	}

}

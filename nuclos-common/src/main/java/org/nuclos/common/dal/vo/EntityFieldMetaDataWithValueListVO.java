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

import java.util.HashSet;
import java.util.Set;

import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
 * Entity field meta data vo
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik.Stueker</a>
 * @version 01.00.00
 */
public class EntityFieldMetaDataWithValueListVO extends EntityFieldMetaDataVO {

	
	Set<MasterDataVO> setValueList;
	
	public EntityFieldMetaDataWithValueListVO() {
		super();
		setValueList = new HashSet<MasterDataVO>();
	}
	
	public EntityFieldMetaDataWithValueListVO(EntityFieldMetaDataVO vo) {
		this();
		this.setCalcFunction(vo.getCalcFunction());
		this.setChangedAt(vo.getChangedAt());
		this.setChangedBy(vo.getChangedBy());
		this.setCreatedAt(vo.getCreatedAt());
		this.setCreatedBy(vo.getCreatedBy());
		this.setDataType(vo.getDataType());
		this.setDbColumn(vo.getDbColumn());
		this.setDefaultForeignId(vo.getDefaultForeignId());
		this.setDefaultValue(vo.getDefaultValue());
		this.setEntityId(vo.getEntityId());
		this.setField(vo.getField());
		this.setFieldGroupId(vo.getFieldGroupId());
		this.setForeignEntity(vo.getForeignEntity());
		this.setForeignEntityField(vo.getForeignEntityField());
		this.setFormatInput(vo.getFormatInput());
		this.setFormatOutput(vo.getFormatOutput());
		this.setId(vo.getId());
		this.setInsertable(vo.isInsertable());
		this.setLocaleResourceIdForDescription(vo.getLocaleResourceIdForDescription());
		this.setLocaleResourceIdForLabel(vo.getLocaleResourceIdForLabel());
		this.setLogBookTracking(vo.isLogBookTracking());
		this.setModifiable(vo.isModifiable());
		this.setNullable(vo.isNullable());
		this.setPrecision(vo.getPrecision());
		this.setScale(this.getScale());
		this.setSearchable(vo.isSearchable());
		this.setShowMnemonic(vo.isShowMnemonic());
		this.setSortorderASC(vo.getSortorderASC());
		this.setSortorderDESC(vo.getSortorderDESC());
		this.setUnique(vo.isUnique());
		this.setIndexed(vo.isIndexed());
		this.setVersion(vo.getVersion());
		this.setFallbacklabel(vo.getFallbacklabel());
		this.processor(vo.processor());
		setValueList = new HashSet<MasterDataVO>();
	}

	public Set<MasterDataVO> getValueList() {
		return setValueList;
	}

	public void setValueList(Set<MasterDataVO> setValueList) {
		this.setValueList = setValueList;
	}
	
	
}

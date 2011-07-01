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
package org.nuclos.server.dbtransfer.content;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.server.dbtransfer.NucletContentMap;

public interface INucletContent {
	
	public NuclosEntity getEntity();
	
	public NuclosEntity getParentEntity();
	
	public boolean canUpdate();
	
	public boolean canDelete();
	
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
	
	public String getIdentifierField();
	
	public boolean checkValidity(EntityObjectVO ncObject, ValidityType validity, NucletContentMap importContentMap, Set<Long> existingNucletIds, ValidityLogEntry log, TransferOption.Map transferOptions);
	
	public NucletContentUID.Map getUIDMap(Set<Long> nucletIds, TransferOption.Map transferOptions);
	
	public List<EntityObjectVO> getUIDObjects(Set<Long> nucletIds, TransferOption.Map transferOptions);
	
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions);
	
	public List<DalCallResult> setNcObjectFieldNull(Long id, String field);
	
	public List<DalCallResult> insertOrUpdateNcObject(EntityObjectVO ncObject, boolean isNuclon);
	
	public List<DalCallResult> deleteNcObject(Long id);
	
	public Set<EntityMetaDataVO> getForeignEntities();
	
	public Collection<EntityFieldMetaDataVO> getFieldDependencies();
	
	public Collection<String> getAdditionalValuesForUnreferencedForeignCheck(String unreferencedForeignEntityField, NucletContentMap importContentMap);

}

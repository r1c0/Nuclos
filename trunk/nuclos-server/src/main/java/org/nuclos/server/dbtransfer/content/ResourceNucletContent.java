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

import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.resource.ejb3.ResourceFacadeBean;
import org.nuclos.server.resource.valueobject.ResourceFile;

public class ResourceNucletContent extends DefaultNucletContent {
	
	public static final String BYTE_ARRAY_FIELD_NAME = "ResourceNucletContent.byteField";

	public ResourceNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.RESOURCE, null, contentTypes);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions); 
		for (EntityObjectVO ncObject : result) {
			ResourceFile resfile = ncObject.getField("file", ResourceFile.class);
			if (resfile != null) {
				resfile.getContents();
			}
		}
		return result;
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		ResourceFacadeBean.removeResource(ResourceCache.getInstance().getResourceById(ncObject.getId().intValue()).getFileName());
		super.deleteNcObject(result, ncObject);
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) {
		ResourceFile resfile = ncObject.getField("file", ResourceFile.class);
		if (resfile != null) {
			ResourceFacadeBean.storeResource(resfile);
		}
		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}
	
	
}

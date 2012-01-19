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
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;

public class ImportFileNucletContent extends DefaultNucletContent {

	public ImportFileNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.IMPORTFILE, null, contentTypes, true);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions); 
		for (EntityObjectVO ncObject : result) {
			GenericObjectDocumentFile docfile = ncObject.getField("name", GenericObjectDocumentFile.class);
			if (docfile != null) {
				docfile.getContents();
			}
			GenericObjectDocumentFile doclogfile = ncObject.getField("log", GenericObjectDocumentFile.class);
			if (doclogfile != null) {
				doclogfile.getContents();
			}
		}
		return result;
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		GenericObjectDocumentFile docfile = ncObject.getField("name", GenericObjectDocumentFile.class);
		if (docfile != null) {
			MasterDataFacadeHelper.remove(ncObject.getId().intValue(), docfile.getFilename(), NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
		}
		GenericObjectDocumentFile doclogfile = ncObject.getField("log", GenericObjectDocumentFile.class);
		if (doclogfile != null) {
			MasterDataFacadeHelper.remove(ncObject.getId().intValue(), doclogfile.getFilename(), NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
		}
		super.deleteNcObject(result, ncObject);
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) {
		MasterDataFacadeHelper.storeFiles(getEntity().getEntityName(), ncObject);
		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}	
}

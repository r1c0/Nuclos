package org.nuclos.server.dbtransfer.content;

import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;

public class WebserviceNucletContent extends DefaultNucletContent {

	public WebserviceNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.WEBSERVICE, null, contentTypes);
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions); 
		for (EntityObjectVO ncObject : result) {
			GenericObjectDocumentFile docfile = ncObject.getField("wsdl", GenericObjectDocumentFile.class);
			if (docfile != null) {
				docfile.getContents();
			}
		}
		return result;
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		GenericObjectDocumentFile docfile = NucletDalProvider.getInstance().getEntityObjectProcessor(getEntity()).getByPrimaryKey(ncObject.getId()).getField("wsdl", GenericObjectDocumentFile.class);
		if (docfile != null) {
			MasterDataFacadeHelper.remove(ncObject.getId().intValue(), docfile.getFilename(), NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
		}
		super.deleteNcObject(result, ncObject);
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) {
		MasterDataFacadeHelper.storeFiles(getEntity().getEntityName(), ncObject);
		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}
}

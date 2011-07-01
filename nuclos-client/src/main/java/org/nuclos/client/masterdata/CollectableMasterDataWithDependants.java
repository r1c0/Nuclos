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
package org.nuclos.client.masterdata;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * A collectable master data (record) along with its dependants.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableMasterDataWithDependants extends CollectableMasterData implements CollectableWithDependants {

	/**
	 * @param clcte
	 * @param mdvo
	 * @return
	 * @precondition govo != null
	 */
	public static CollectableMasterDataWithDependants newInstance(CollectableMasterDataEntity clcte, MasterDataVO mdvo) {
		return new CollectableMasterDataWithDependants(clcte, new MasterDataWithDependantsVO(mdvo, new DependantMasterDataMap()));
	}

	public CollectableMasterDataWithDependants(CollectableMasterDataEntity clcte, MasterDataWithDependantsVO mdwdcvo) {
		super(clcte, mdwdcvo);
	}

	public MasterDataWithDependantsVO getMasterDataWithDependantsCVO() {
		return (MasterDataWithDependantsVO) this.getMasterDataCVO();
	}

	@Override
	public boolean isComplete() {
		return this.getMasterDataWithDependantsCVO().isComplete();
	}

	/**
	 * @param sSubEntityName
	 * @return a copy of the contained dependants for the subentity with the given name.
	 * @postcondition result != null
	 */
	@Override
	public Collection<CollectableEntityObject> getDependants(String sSubEntityName) {
		CollectableEntity ce = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
		if(ce instanceof CollectableMasterDataEntity){
			EntityMetaDataVO metaVO = MetaDataClientProvider.getInstance().getEntity(sSubEntityName);
			Map<String, EntityFieldMetaDataVO> mpFields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(sSubEntityName);
			ce = new CollectableEOEntity(metaVO, mpFields);
		}
		final CollectableEOEntity clctmde = (CollectableEOEntity)ce; 
		final Collection<EntityObjectVO> collmdvoDependants = this.getMasterDataWithDependantsCVO().getDependants().getData(sSubEntityName);
		final List<CollectableEntityObject> result = CollectionUtils.transform(collmdvoDependants, new CollectableEntityObject.MakeCollectable(clctmde));
		assert result != null;
		return result;
	}

	/**
	 * inner class MakeCollectable
	 */
	public static class MakeCollectable implements Transformer<MasterDataWithDependantsVO, CollectableMasterDataWithDependants> {
		private final CollectableMasterDataEntity clctmde;

		public MakeCollectable(CollectableMasterDataEntity clctmde) {
			this.clctmde = clctmde;
		}

		@Override
		public CollectableMasterDataWithDependants transform(MasterDataWithDependantsVO mdwdcvo) {
			return new CollectableMasterDataWithDependants(clctmde, mdwdcvo);
		}
	}

}	// class CollectableMasterDataWithDependants

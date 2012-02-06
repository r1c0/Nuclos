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
package org.nuclos.server.genericobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToEntityTreeViewVO;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides information about the modules contained in a Nucleus application.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Modules extends ModuleProvider {
	
	private static Modules INSTANCE;
	
	//

	/**
	 * The specfied subnodes of the module used in the treenode 
	 */
	private Map<String, List<MasterDataVO>> mpModuleSubnodes;
	
	private Map<String, List<EntityTreeViewVO>> subNodes;
	
	private MasterDataFacadeHelper masterDataFacadeHelper;
	
	//

	Modules() {
		INSTANCE = this;
	}
	
	@PostConstruct
	void init() {
		setModules(getModules());
	}
	
	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.masterDataFacadeHelper = masterDataFacadeHelper;
	}

	public static Modules getInstance() {
		return INSTANCE;
	}

	@Override
	public Collection<MasterDataVO> getModules() {
		try {
			if (NuclosLocalServerSession.getCurrentUser() == null)
				NuclosLocalServerSession.loginAsSuperUser();

			List<MasterDataVO> colModules = new ArrayList<MasterDataVO>();
			for (EntityMetaDataVO eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
				if (eMeta.isStateModel()) {
					colModules.add(DalSupportForMD.wrapEntityMetaDataVOInModule(eMeta));
				}
			}

			mpModuleSubnodes = new HashMap<String, List<MasterDataVO>>();
			subNodes = new HashMap<String, List<EntityTreeViewVO>>();
			for (MasterDataVO mdcvo : colModules) {
				Collection<EntityObjectVO> colVO = masterDataFacadeHelper.getDependantMasterData(
						NuclosEntity.ENTITYSUBNODES.getEntityName(), EntityTreeViewVO.ENTITY_FIELD, mdcvo.getIntId(), "(Modules cache)");
				mpModuleSubnodes.put((String)mdcvo.getField("entity"), CollectionUtils.transform(colVO,
					new EntityObjectToMasterDataTransformer()));
				subNodes.put((String)mdcvo.getField("entity"), CollectionUtils.transform(colVO,
					new EntityObjectToEntityTreeViewVO()));
			}

			return colModules;
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * @deprecated Todo: Get rid of it! (Thomas Pasch)
	 */
	public List<MasterDataVO> getSubnodesMD(final String sEntity) {
		return mpModuleSubnodes.get(sEntity);
	}
	
	public List<EntityTreeViewVO> getSubnodesETV(final String sEntity) {
		return subNodes.get(sEntity);
	}

}	// class Modules

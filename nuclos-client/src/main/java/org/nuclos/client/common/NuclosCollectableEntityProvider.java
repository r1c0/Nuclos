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
package org.nuclos.client.common;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.rule.admin.CollectableRule;
import org.nuclos.client.rule.admin.CollectableRuleEventUsage;
import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.statemodel.admin.CollectableStateRole;
import org.nuclos.client.statemodel.admin.CollectableStateRoleAttributeGroup;
import org.nuclos.client.statemodel.admin.CollectableStateRoleSubForm;
import org.nuclos.client.task.CollectableTask;
import org.nuclos.client.task.CollectableTaskOwner;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <code>CollectableEntityProvider</code> for all Nucleus-specific entities.
 * This class is designed for client use only.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo move this class to nucleus.common so it's accessible from the server -
 * or implement a separate server version.
 */
@Component
public class NuclosCollectableEntityProvider implements CollectableEntityProvider {

	private static NuclosCollectableEntityProvider INSTANCE;
	
	//

	private Map<String, CollectableEntity> mpSpecialEntities = new HashMap<String, CollectableEntity>(12);
	
	private AttributeCache attributeCache;

	public static NuclosCollectableEntityProvider getInstance() {
		return INSTANCE;
	}

	private NuclosCollectableEntityProvider() {
		INSTANCE = this;
	}
	
	public final void init() {
		attributeCache.fill();

		this.mpSpecialEntities.put(CollectableRule.clcte.getName(), CollectableRule.clcte);
		this.mpSpecialEntities.put(CollectableRuleEventUsage.clcte.getName(), CollectableRuleEventUsage.clcte);
		this.mpSpecialEntities.put(CollectableStateModel.clcte.getName(), CollectableStateModel.clcte);
		this.mpSpecialEntities.put(CollectableTask.clcte.getName(), CollectableTask.clcte);
		this.mpSpecialEntities.put(CollectableStateRole.clcte.getName(), CollectableStateRole.clcte);
		this.mpSpecialEntities.put(CollectableStateRoleAttributeGroup.clcte.getName(), CollectableStateRoleAttributeGroup.clcte);
		this.mpSpecialEntities.put(CollectableStateRoleSubForm.clcte.getName(), CollectableStateRoleSubForm.clcte);
		this.mpSpecialEntities.put(CollectableTaskOwner.clcte.getName(), CollectableTaskOwner.clcte);
	}
	
	@Autowired
	void setAttributeCache(AttributeCache attributeCache) {
		this.attributeCache = attributeCache;
	}

	@Override
    public CollectableEntity getCollectableEntity(String sEntityName) throws NoSuchElementException {

			final CollectableEntity result;
	
			// 1. special entities:
			final CollectableEntity clcte = this.mpSpecialEntities.get(sEntityName);
			if (clcte != null) {
				result = clcte;
			}
			else {
				// 2. generic object entities (modules):
				Integer iModuleId;
				try {
					iModuleId = Modules.getInstance().getModuleIdByEntityName(sEntityName);
					assert iModuleId != null;
				}
				catch (NoSuchElementException ex) {
					iModuleId = null;
				}

				if (iModuleId != null) {
					result = CollectableGenericObjectEntity.getByModuleId(iModuleId);
				}
				else {
					// 3. MasterData entities:
					final MasterDataMetaVO mdmetavo;
					mdmetavo = MasterDataDelegate.getInstance().getMetaData(sEntityName);

					result = new CollectableMasterDataEntity(mdmetavo);
				
				}
			
			}
			assert result != null;
			assert result.getName().equals(sEntityName);
			return result;
	}

	@Override
    public boolean isEntityDisplayable(String sEntityName) throws NoSuchElementException {
		return Modules.getInstance().isModuleEntity(sEntityName) ||
				MasterDataLayoutHelper.isLayoutMLAvailable(sEntityName, false);
	}

}	// class NuclosCollectableEntityProvider

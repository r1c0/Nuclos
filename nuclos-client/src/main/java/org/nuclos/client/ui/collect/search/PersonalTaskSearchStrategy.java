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
package org.nuclos.client.ui.collect.search;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

public class PersonalTaskSearchStrategy extends MasterDataSearchStrategy {

	public PersonalTaskSearchStrategy() {
	}

	@Override
	public CollectableSearchCondition getCollectableSearchCondition() throws CollectableFieldFormatException {
		String sUser = Main.getInstance().getMainController().getUserName();
		Integer iUser = SecurityDelegate.getInstance().getUserId(sUser);
		CompositeCollectableSearchCondition taskCondition = new CompositeCollectableSearchCondition(LogicalOperator.OR);
		CollectableSearchCondition delegatorCondition = SearchConditionUtils.newMDReferenceComparison(MetaDataCache
				.getInstance().getMetaData(NuclosEntity.TASKLIST), "taskdelegator", iUser, sUser);
		CollectableSearchCondition ownerCondition = SearchConditionUtils.newMDReferenceComparison(MetaDataCache
				.getInstance().getMetaData(NuclosEntity.TASKOWNER), "user", iUser, sUser);
		CollectableSubCondition ownerSubCondition = new CollectableSubCondition(NuclosEntity.TASKOWNER.getEntityName(),
				"tasklist", ownerCondition);
		taskCondition.addOperand(delegatorCondition);
		taskCondition.addOperand(ownerSubCondition);
		if (super.getCollectableSearchCondition() != null) {
			if (SecurityCache.getInstance().isSuperUser()) {
				return super.getCollectableSearchCondition();
			} else {
				CompositeCollectableSearchCondition combinedCondition = new CompositeCollectableSearchCondition(
						LogicalOperator.AND);
				combinedCondition.addOperand(taskCondition);
				combinedCondition.addOperand(super.getCollectableSearchCondition());
				return combinedCondition;
			}
		} else {
			return taskCondition;
		}
	}
}

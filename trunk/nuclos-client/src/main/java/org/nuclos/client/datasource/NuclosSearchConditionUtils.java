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
package org.nuclos.client.datasource;

import java.util.List;
import java.util.Map;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.genericobject.DataSourceSearchImporter;
import org.nuclos.server.report.valueobject.DatasourceVO;

public class NuclosSearchConditionUtils {

	private NuclosSearchConditionUtils(){}
	
	public static CollectableSearchCondition restorePlainSubConditions(CollectableSearchCondition cond) {
		CollectableSearchCondition resultCond = cond;
		try {
			if(resultCond instanceof PlainSubCondition){
				checkAndRestoreSQL(resultCond);
			} else {
				if(resultCond instanceof CompositeCollectableSearchCondition){
					List<CollectableSearchCondition> cOperands = ((CompositeCollectableSearchCondition)cond).getOperands();
					for(CollectableSearchCondition operand : cOperands){
						if(operand instanceof PlainSubCondition){
							checkAndRestoreSQL(operand);
						}
					}
				}
			}
		} catch (CommonBusinessException e) {
			throw new RuntimeException(e);
		}
		return resultCond;
	}

	private static void checkAndRestoreSQL(CollectableSearchCondition pPlainSubCondition) throws CommonBusinessException {
		PlainSubCondition plainSubCondition = (PlainSubCondition)pPlainSubCondition;
		if(!pPlainSubCondition.isSyntacticallyCorrect()){
			DatasourceVO dvo = DatasourceDelegate.getInstance().getDatasourceByName((plainSubCondition).getConditionName());
			if(dvo != null){
				PlainSubCondition pSub = DataSourceSearchImporter.buildPlainSubConditionFromDatasource(dvo);
				((PlainSubCondition)pPlainSubCondition).setPlainSQL(pSub.getPlainSQL());
			}
		}
	}
	
	public static PlainSubCondition initPlainSubCondition(String plainSubConditionName, Map<String,Object> params) throws CommonBusinessException {
		PlainSubCondition pSub = null;
		DatasourceVO dvo = DatasourceDelegate.getInstance().getDatasourceByName(plainSubConditionName);
		if(dvo != null){
			pSub = DataSourceSearchImporter.buildPlainSubConditionFromDatasource(dvo, params);
		}
		return pSub;
	}
}

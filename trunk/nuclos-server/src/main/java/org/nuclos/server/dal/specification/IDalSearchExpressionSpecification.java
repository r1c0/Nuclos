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
package org.nuclos.server.dal.specification;

import java.util.Collection;
import java.util.List;

import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

public interface IDalSearchExpressionSpecification<DalVO extends IDalVO> {
	
	List<DalVO> getBySearchExpression(CollectableSearchExpression clctexpr);
	
	List<DalVO> getBySearchExpressionAndPrimaryKeys(CollectableSearchExpression clctexpr, List<Long> ids);
	
	List<DalVO> getBySearchExpression(CollectableSearchExpression clctexpr, boolean bSortResult);
	
	List<DalVO> getBySearchExpression(CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult);
	
	/**
	 * @deprecated fields doesn't support fields from joined entities, hence we need something better... 
	 */
	List<DalVO> getBySearchExpression(Collection<String> fields, CollectableSearchExpression clctexpr, Integer iMaxRowCount, boolean bSortResult, boolean bSearchInDMLSource);
	
	List<Long> getIdsBySearchExpression(CollectableSearchExpression clctexpr);
	
	List<Long> getIdsBySearchExprUserGroups(CollectableSearchExpression searchExpression, Long moduleId, String user);
}

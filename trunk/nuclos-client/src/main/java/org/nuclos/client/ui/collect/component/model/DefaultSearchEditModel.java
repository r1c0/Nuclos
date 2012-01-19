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
package org.nuclos.client.ui.collect.component.model;

import java.util.Collection;

import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;

/**
 * Default implementation of <code>SearchEditModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class DefaultSearchEditModel
		extends AbstractEditModel<SearchComponentModel>
		implements SearchEditModel {

	public DefaultSearchEditModel(Collection<CollectableComponent> collclctcomp) {
		super(collclctcomp);
	}

	@Override
	public CollectableSearchCondition getSearchCondition() {
		return getCollectableSearchConditionFrom(this.getCollectableComponentModels());
	}

	/**
	 * @param collclctcompmodel Collection<CollectableComponentModel>
	 * @return the search condition contained in the given <code>SearchComponentModel</code>s.
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 * @todo make private
	 */
	public static CollectableSearchCondition getCollectableSearchConditionFrom(Collection<SearchComponentModel> collclctcompmodel) {
		final CompositeCollectableSearchCondition condAnd = new CompositeCollectableSearchCondition(LogicalOperator.AND);

		for (SearchComponentModel clctcompmodel : collclctcompmodel) {
			final CollectableSearchCondition cond = clctcompmodel.getSearchCondition();
			if (cond != null) {
				condAnd.addOperand(cond);
			}
		}	// for

		return SearchConditionUtils.simplified(condAnd);
	}

}	// class DefaultSearchEditModel

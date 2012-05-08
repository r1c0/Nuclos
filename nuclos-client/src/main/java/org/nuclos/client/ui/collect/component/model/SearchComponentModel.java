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

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;

/**
 * A <code>CollectableComponentModel</code> for searchable components.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SearchComponentModel extends CollectableComponentModel {

	private CollectableSearchCondition cond;

	/**
	 * @param clctef
	 * @postcondition this.isSearchModel()
	 */
	public SearchComponentModel(CollectableEntityField clctef) {
		super(clctef);

		assert this.isSearchModel();
	}

	@Override
	public boolean isSearchModel() {
		return true;
	}

	@Override
	public boolean isMultiEditable() {
		return false;
	}

	public CollectableSearchCondition getSearchCondition() {
		return this.cond;
	}

	public void setSearchCondition(CollectableSearchCondition cond) {
		final CollectableField clctfOldValue = this.getField();
		this.cond = cond;

		final CollectableField clctfComparand = getComparand(cond);

		final CollectableField clctfNewValue = (clctfComparand != null) ? clctfComparand : this.getEntityField().getNullField();

		super.setField(clctfNewValue, false);

		this.fireSearchConditionChanged();

		this.fireFieldChanged(clctfOldValue, clctfNewValue);
	}

	public void setSearchCondition(CollectableSearchCondition cond, boolean bDirty) {
		this.setSearchCondition(cond, true, bDirty);
	}

	public void setSearchCondition(CollectableSearchCondition cond, boolean bNotifyListeners, boolean bDirty) {
		final CollectableField clctfOldValue = this.getField();

		this.cond = cond;

		final CollectableField clctfNewValue;
		final CollectableField clctfComparand = getComparand(cond);
		if (clctfComparand == null && cond != null) {
			final AtomicCollectableSearchCondition atomiccond = (AtomicCollectableSearchCondition) cond;
			clctfNewValue = CollectableUtils.newCollectableFieldForValue(atomiccond.getEntityField(), atomiccond.getComparandAsString());
		}
		else {
			clctfNewValue = (clctfComparand != null) ? clctfComparand : this.getEntityField().getNullField();
		}
		super.setField(clctfNewValue, false, bDirty);

		if (bNotifyListeners) {
			this.fireSearchConditionChanged();
			this.fireFieldChanged(clctfOldValue, clctfNewValue);
		}
	}

	private void fireSearchConditionChanged() {
		getModelHelper().fireSearchConditionChanged(this);
	}

	/**
	 * sets the searchcondition of this searchable collectable component and at the same time
	 * the value (implicitly).
	 * @param clctfValue
	 */
	@Override
	public void setField(CollectableField clctfValue) {
		this.setSearchCondition(this.getDefaultSearchCondition(clctfValue));
	}

	/**
	 * sets the searchcondition of this searchable collectable component and at the same time
	 * the value (implicitly).
	 * @param clctfValue
	 */
	@Override
	public void setField(CollectableField clctfValue, boolean bDirty) {
		this.setSearchCondition(this.getDefaultSearchCondition(clctfValue), bDirty);
	}

	/**
	 * sets the searchcondition of this searchable collectable component and at the same time
	 * the value (implicitly).
	 * @param clctfValue
	 */
	public void _setCollectableField(CollectableField clctfValue, boolean bDirty) {
		this.setSearchCondition(this.getDefaultSearchCondition(clctfValue), false, bDirty);
	}

	/**
	 * clears the model and notifies the listeners.
	 * @postcondition this.getField().isNull()
	 */
	@Override
	public void clear() {
		this.setSearchCondition(null);

		assert this.getField().isNull();
	}

	private CollectableComparison getDefaultSearchCondition(CollectableField clctfValue) {
		return getDefaultSearchCondition(clctfValue, this.getEntityField());
	}

	public static CollectableComparison getDefaultSearchCondition(CollectableField clctfValue, CollectableEntityField clctef) {
		return clctfValue.isNull() ? null : new CollectableComparison(clctef, ComparisonOperator.EQUAL, clctfValue);
	}

	/**
	 * @param cond
	 * @return the <code>CollectableField</code> contained as comparand in <code>SearchCondition</code>, if any.
	 */
	private static CollectableField getComparand(CollectableSearchCondition cond) {
		CollectableField result = null;
		if (cond != null) {
			if (cond instanceof CollectableComparison) {
				final CollectableComparison comparison = (CollectableComparison) cond;
				result = comparison.getComparand();
			}
			else if(cond instanceof CompositeCollectableSearchCondition) {
				final CompositeCollectableSearchCondition composite = (CompositeCollectableSearchCondition) cond;
				final CollectableSearchCondition condFirstOperand = composite.getOperands().get(0);
				result = getComparand(condFirstOperand);
			}
		}
		return result;
	}

}	// class SearchComponentModel

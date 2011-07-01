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
package org.nuclos.common.collect.collectable.searchcondition;

import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition.AtomicVisitor;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition.CompositeVisitor;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition.Visitor;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Visitor for creating a human readable string representation of a search
 * condition.
 * Due to localization, this visitor can only be used on client
 * side.
 * 
 * <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @version 01.00.00
 */
public class ToHumanReadablePresentationVisitor implements Visitor<String, RuntimeException>, CompositeVisitor<String, RuntimeException>, AtomicVisitor<String, RuntimeException> {

	//
	// Visitor
	//
		
	@Override
	public String visitAtomicCondition(AtomicCollectableSearchCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		final StringBuilder sb = new StringBuilder(cond.getFieldLabel());
		sb.append(' ');		
		ComparisonOperator compop = cond.getComparisonOperator();
		sb.append(CommonLocaleDelegate.getMessage(compop.getResourceIdForLabel(), null));
		if (compop.getOperandCount() > 1) {
			sb.append(' ');
			// the cast is needed for the correct method dispatch
			sb.append(cond.accept((AtomicVisitor<String, RuntimeException>) this));
		}
		return sb.toString();
	}

	@Override
	public String visitCompositeCondition(CompositeCollectableSearchCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		final StringBuilder sb = new StringBuilder();
		final String sOperator = CommonLocaleDelegate.getMessage(cond.getLogicalOperator().getResourceIdForLabel(), null);
		switch (cond.getOperandCount()) {
			case 0:
			case 1:
				sb.append(sOperator);
				sb.append(" (");
				if (cond.getOperandCount() == 1) {
					sb.append(cond.getOperands().get(0).accept(this));
				}
				sb.append(")");
				break;
			default:
				// first operand:
				sb.append("(");
				sb.append(cond.getOperands().get(0).accept(this));
				sb.append(")");

				// following operands:
				for (int iOperand = 1; iOperand < cond.getOperandCount(); ++iOperand) {
					sb.append(" ");
					sb.append(sOperator);
					sb.append(" (");
					sb.append(cond.getOperands().get(iOperand).accept(this));
					sb.append(")");
				}
				break;
		}
		return sb.toString();
	}
	

	@Override
	public String visitIdCondition(CollectableIdCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		return "id = " + cond.getId();
	}

	@Override
	public String visitReferencingCondition(ReferencingCollectableSearchCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		CollectableEntity referencedEntity = DefaultCollectableEntityProvider.getInstance()
			.getCollectableEntity(cond.getReferencedEntityName());	
		String referenedEntityLabel = referencedEntity.getLabel();
		CollectableSearchCondition condSub = cond.getSubCondition();
		if (condSub != null) {
			return CommonLocaleDelegate.getMessage("searchCondition.referencesWith", null, 
				referenedEntityLabel, condSub.accept(this));
		} else {
			return CommonLocaleDelegate.getMessage("searchCondition.references", null,
				referenedEntityLabel);
		}
	}

	@Override
	public String visitSubCondition(CollectableSubCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		String subEntityLabel = cond.getSubEntity().getLabel();
		CollectableSearchCondition condSub = cond.getSubCondition();
		if (condSub != null) {
			return CommonLocaleDelegate.getMessage("searchCondition.existsWith", null, 
				subEntityLabel, condSub.accept(this));
		} else {
			return CommonLocaleDelegate.getMessage("searchCondition.exists", null,
				subEntityLabel);
		}
	}

	@Override
	public String visitTrueCondition(TrueCondition truecond) {
		if(truecond.getConditionName() != null)
			return truecond.getConditionName();
		
		return CommonLocaleDelegate.getMessage("searchCondition.true", null);
	}

	//
	// CompositeVisitor
	//
	
	@Override
	public String visitPlainSubCondition(PlainSubCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		final StringBuilder sb = new StringBuilder();
		if (cond.getPlainSQL() != null) {
			sb.append(" INTID IN (").append(cond.getPlainSQL()).append(")");
		}
		if(sb.length() > 200){
			sb.setLength(200);
			sb.append("...");
		}
		return sb.toString();
	}

	@Override
	public String visitSelfSubCondition(CollectableSelfSubCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		final StringBuilder sb = new StringBuilder();
		final CollectableSearchCondition condSub = cond.getSubCondition();
		if (condSub != null) {
			sb.append(" INTID IN (").append(condSub.accept(this)).append(")");
		}
		return sb.toString();
	}

	//
	// AtomicVisitor
	//
	
	@Override
	public String visitComparison(CollectableComparison comp) {
		if(comp.getConditionName() != null)
			return comp.getConditionName();
		
		final boolean bSurroundByQuotes = comp.getEntityField().getJavaClass().equals(String.class);
		final String result;
		if (bSurroundByQuotes) {
			result = "'" + comp.getComparandAsString() + "'";
		}
		else {
			result = comp.getComparandAsString();
		}
		return result;
	}

	@Override
	public String visitComparisonWithParameter(CollectableComparisonWithParameter comp) {
		if(comp.getConditionName() != null)
			return comp.getConditionName();
		return "<" + CommonLocaleDelegate.getText(comp.getParameter()) + ">";
	}

	@Override
	public String visitComparisonWithOtherField(CollectableComparisonWithOtherField comp) {
		if(comp.getConditionName() != null)
			return comp.getConditionName();
		
		return comp.getOtherField().getLabel();
	}

	@Override
	public String visitIsNullCondition(CollectableIsNullCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		return null;
	}

	@Override
	public String visitLikeCondition(CollectableLikeCondition cond) {
		if(cond.getConditionName() != null)
			return cond.getConditionName();
		
		final StringBuilder sb = new StringBuilder();
		sb.append('\'');
		sb.append(cond.getLikeComparand());
		sb.append('\'');
		return sb.toString();
	}

	@Override
    public String visitIdListCondition(
        CollectableIdListCondition collectableIdListCondition)
        throws RuntimeException {
		if(collectableIdListCondition.getConditionName() != null)
			return collectableIdListCondition.getConditionName();
		
		return "ids = " + collectableIdListCondition.getIds();
    }
}

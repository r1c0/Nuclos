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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Composite collectable search condition. This class is not immutable.
 * @todo clearly state whether operands may be null or not!
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CompositeCollectableSearchCondition extends AbstractCollectableSearchCondition {

	public static final int UNDEFINED = -1;

	private transient LogicalOperator logicalOperator;

	private final List<CollectableSearchCondition> lstOperands;

	public CompositeCollectableSearchCondition(LogicalOperator op) {
		this(op, null);
	}

	/**
	 * @param collOperands must be a Collection<CollectableSearchCondition>
	 * @precondition lstOperands != null
	 */
	public CompositeCollectableSearchCondition(LogicalOperator logicalOperator, Collection<? extends CollectableSearchCondition> collOperands) {
		this.logicalOperator = logicalOperator;

		this.lstOperands = (collOperands == null) ? new LinkedList<CollectableSearchCondition>() : new LinkedList<CollectableSearchCondition>(collOperands);
	}

	/**
	 * adds <code>operand</code> to the list of operands
	 * @param operand
	 */
	public void addOperand(CollectableSearchCondition operand) {
		this.lstOperands.add(operand);
	}

	public void removeOperand(CollectableSearchCondition operand) {
		this.lstOperands.remove(operand);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_COMPOSITE;
	}

	public LogicalOperator getLogicalOperator() {
		return this.logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator op) {
		this.logicalOperator = op;
	}

	/**
	 * @return the operands of this node (as unmodifiable list)
	 */
	public List<CollectableSearchCondition> getOperands() {
		return Collections.unmodifiableList(this.lstOperands);
	}

	/**
	 * adds all operands from the given list to this.
	 * @param lstOperands
	 */
	public void addAllOperands(List<CollectableSearchCondition> lstOperands) {
		this.lstOperands.addAll(lstOperands);
	}

	/**
	 * @return the number of operands
	 */
	public int getOperandCount() {
		return this.lstOperands.size();
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		final int iOperandCount = this.getOperandCount();
		boolean result = (iOperandCount >= this.getLogicalOperator().getMinOperandCount()) && (iOperandCount <= this.getLogicalOperator().getMaxOperandCount());
		if (result) {
			for (CollectableSearchCondition searchcond : this.lstOperands) {
				if (!searchcond.isSyntacticallyCorrect()) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CompositeCollectableSearchCondition)) {
			return false;
		}

		final CompositeCollectableSearchCondition that = (CompositeCollectableSearchCondition) o;

		return (this.logicalOperator == that.logicalOperator) && this.lstOperands.equals(that.lstOperands);
	}

	@Override
	public int hashCode() {
		return this.logicalOperator.hashCode() ^ this.lstOperands.hashCode();
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex{
		return visitor.visitCompositeCondition(this);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// LogicalOperator is not serializable:
		oos.writeInt(this.logicalOperator.getIntValue());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// LogicalOperator is not serializable:
		this.logicalOperator = LogicalOperator.getInstance(ois.readInt());
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + logicalOperator + ":" + 
			lstOperands;
	}
	
}  // class CompositeCollectableSearchCondition

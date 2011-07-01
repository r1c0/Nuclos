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

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common2.LangUtils;

/**
 * A collectable subcondition which represents a sub select statement like "where intid in (select intid from ...)"
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 * @todo refactor this condition and standard subcondition to avoid redundance.
 */
public class CollectableSelfSubCondition extends AbstractCollectableSearchCondition {

	private final String sSubEntityName;
	private final String sForeignKeyFieldName;
	private final CollectableSearchCondition condSub;

	/**
	 * @param sForeignKeyFieldName
	 * @param condSub
	 * @param sSubEntityName
	 * @precondition sForeignKeyFieldName != null
	 */
	public CollectableSelfSubCondition(String sForeignKeyFieldName, CollectableSearchCondition condSub, String sSubEntityName) {
		if(sForeignKeyFieldName == null) {
			throw new NullArgumentException("sForeignKeyFieldName");
		}
		this.sForeignKeyFieldName = sForeignKeyFieldName;
		this.condSub = condSub;
		this.sSubEntityName = sSubEntityName;
	}
	
	/**
	 * @return
	 * @postcondition result != null
	 */
	public String getForeignKeyFieldName() {
		return this.sForeignKeyFieldName;
	}

	/**
	 * @return the subcondition, if any.
	 */
	public CollectableSearchCondition getSubCondition() {
		return this.condSub;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_SUB;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		final CollectableSearchCondition condSub = this.getSubCondition();
		return condSub != null && condSub.isSyntacticallyCorrect();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableSubCondition)) {
			return false;
		}
		final CollectableSelfSubCondition that = (CollectableSelfSubCondition) o;

		return this.sForeignKeyFieldName.equals(that.sForeignKeyFieldName) &&
				LangUtils.equals(this.condSub, that.condSub);
	}

	@Override
	public int hashCode() {
		return this.sForeignKeyFieldName.hashCode() ^ LangUtils.hashCode(this.condSub);
	}

	public <O, Ex extends Exception> O accept(CompositeVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitSelfSubCondition(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		if(visitor instanceof CompositeVisitor<?, ?>){
			return accept((CompositeVisitor<O, Ex>)visitor);
		}
		return null;
	}

	public String getSubEntityName() {
		return sSubEntityName;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + condSub + ":" + 
			sSubEntityName + ":" + sForeignKeyFieldName;
	}
	
}

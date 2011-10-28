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

import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common2.LangUtils;

/**
 * A collectable subcondition which represents an expression like this: "Does a Collectable in the given dependant subentity
 * exist that is joined to the main entity by the given foreign key field and that matches the criteria given in the subcondition?".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectableSubCondition extends AbstractCollectableSearchCondition {

	private final String sSubEntityName;
	private final String sForeignKeyFieldName;
	private final CollectableSearchCondition condSub;

	/**
	 * @param sSubEntityName
	 * @param sForeignKeyFieldName
	 * @param condSub
	 * @precondition sSubEntityName != null
	 * @precondition sForeignKeyFieldName != null
	 */
	public CollectableSubCondition(String sSubEntityName, String sForeignKeyFieldName, CollectableSearchCondition condSub) {
		if(sSubEntityName == null) {
			throw new NullArgumentException("sSubEntityName");
		}
		if(sForeignKeyFieldName == null) {
			throw new NullArgumentException("sForeignKeyFieldName");
		}
		this.sSubEntityName = sSubEntityName;
		this.sForeignKeyFieldName = sForeignKeyFieldName;
		this.condSub = condSub;
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	public CollectableEntity getSubEntity() {
		return DefaultCollectableEntityProvider.getInstance().getCollectableEntity(this.getSubEntityName());
	}

	public String getSubEntityName() {
		return this.sSubEntityName;
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
		final CollectableSubCondition that = (CollectableSubCondition) o;

		return this.sSubEntityName.equals(that.sSubEntityName) &&
				this.sForeignKeyFieldName.equals(that.sForeignKeyFieldName) &&
				LangUtils.equals(this.condSub, that.condSub);
	}

	@Override
	public int hashCode() {
		return this.sSubEntityName.hashCode() ^ this.sForeignKeyFieldName.hashCode() ^ LangUtils.hashCode(this.condSub);
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitSubCondition(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + condSub + ":" + 
			sSubEntityName + ":" + sForeignKeyFieldName;
	}
	
}  // class CollectableSubCondition

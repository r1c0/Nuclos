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

import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;
import org.nuclos.common2.LangUtils;

/**
 * <code>CollectableSearchCondition</code> that compares the <code>Collectable</code>'s id.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectableIdCondition extends AbstractCollectableSearchCondition {

	private final Object oId;

	public CollectableIdCondition(Object oId) {
		this.oId = oId;
	}

	public Object getId() {
		return this.oId;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_ID;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CollectableIdCondition)) {
			return false;
		}
		final CollectableIdCondition that = (CollectableIdCondition) o;

		return LangUtils.equals(this.oId, that.oId);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.oId);
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitIdCondition(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + oId;
	}
	
}  // class CollectableIdCondition

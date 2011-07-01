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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;

/**
 * <code>CollectableSearchCondition</code> that compares the <code>Collectable</code>'s id's.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public final class CollectableIdListCondition extends AbstractCollectableSearchCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<Object> oIds;

	public CollectableIdListCondition(List<Object> oIds) {
		this.oIds = oIds;
	}

	public List<Object> getIds() {
		return this.oIds;
	}
	
	public List<Long> getLongIds() {
		return org.nuclos.common.collection.CollectionUtils.transform(getIds(), new Transformer<Object, Long>() {
			@Override
            public Long transform(Object i) {
	            return Long.valueOf(i.toString());
            }});
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
		if (!(o instanceof CollectableIdListCondition)) {
			return false;
		}
		final CollectableIdListCondition that = (CollectableIdListCondition) o;
		
		return CollectionUtils.isEqualCollection(this.oIds, that.oIds);
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(this.oIds);
	}

	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitIdListCondition(this);
	}
	
	/**
	 * 
	 * @param longIds
	 * @return
	 */
	public static CollectableIdListCondition newCollectableIdListCondition(List<Long> longIds) {
		return new CollectableIdListCondition(org.nuclos.common.collection.CollectionUtils.transform(longIds, new Transformer<Long, Object>() {
			@Override
			public Object transform(Long i) {
				return i;
			}}));
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + oIds;
	}
	
}  // class CollectableIdCondition

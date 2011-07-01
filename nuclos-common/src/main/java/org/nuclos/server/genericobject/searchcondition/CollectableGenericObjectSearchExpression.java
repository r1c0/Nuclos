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
package org.nuclos.server.genericobject.searchcondition;

import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;

import java.util.List;

/**
 * Special case of a search expression for leased objects, respecting deletion and protection markers.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectSearchExpression extends CollectableSearchExpression {
	private static final long serialVersionUID = 4723065116073085786L;

	// @todo replace int with Boolean (preferred) or typesafe enum
	public static final int SEARCH_UNDELETED = 0;
	public static final int SEARCH_DELETED = 1;
	public static final int SEARCH_BOTH = 2;

	private Integer searchDeleted = SEARCH_UNDELETED;

	/**
	 * creates a search expression with the given search condition.
	 * @param clctcond
	 * @postcondition this.getSearchCondition() == clctcond
	 */
	public CollectableGenericObjectSearchExpression(CollectableSearchCondition clctcond, Integer iSearchDeleted) {
		super(clctcond);
		this.searchDeleted = iSearchDeleted;
	}

	/**
	 * creates a search expression with the given search condition and sorting order.
	 * @param clctcond
	 * @param lstSortingOrder
	 * @postcondition this.getSearchCondition() == clctcond
	 */
	public CollectableGenericObjectSearchExpression(CollectableSearchCondition clctcond, List<CollectableSorting> lstSortingOrder) {
		super(clctcond, lstSortingOrder);
	}

	/**
	 * creates a search expression with the given search condition and sorting order.
	 * @param clctcond
	 * @param lstSortingOrder
	 * @postcondition this.getSearchCondition() == clctcond
	 */
	public CollectableGenericObjectSearchExpression(CollectableSearchCondition clctcond, List<CollectableSorting> lstSortingOrder, Integer iSearchDeleted) {
		super(clctcond, lstSortingOrder);
		this.searchDeleted = iSearchDeleted;
	}

	public Integer getSearchDeleted() {
		return searchDeleted;
	}

}	// class CollectableGenericObjectSearchExpression

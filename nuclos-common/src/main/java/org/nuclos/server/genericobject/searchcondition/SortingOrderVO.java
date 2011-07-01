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
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.AttributeProvider;
import java.io.Serializable;

/**
 * Value object representing a search expression's sorting order.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 * @deprecated Replace with CollectableSorting.
 */
@Deprecated
public class SortingOrderVO implements Serializable {
	private static final long serialVersionUID = -6410624664766551555L;

	public static final String ASCENDING = "asc";
	public static final String DESCENDING = "desc";

	private final int iAttributeId;
	private final String sDirection;

	public SortingOrderVO(int iAttributeId, String sDirection) {
		this.iAttributeId = iAttributeId;
		this.sDirection = sDirection;
	}

	/**
	 * @return attribute id
	 */
	public int getAttributeId() {
		return iAttributeId;
	}

	/**
	 * @return order direction (asc/desc)
	 */
	public String getDirection() {
		return sDirection;
	}

	public static SortingOrderVO fromCollectableSorting(CollectableSorting clctsorting, AttributeProvider attrprovider, String sEntity) {
		final int iAttributeId = attrprovider.getAttribute(sEntity, clctsorting.getFieldName()).getId();
		final String sDirection = clctsorting.isAscending() ? ASCENDING : DESCENDING;
		return new SortingOrderVO(iAttributeId, sDirection);
	}

	public static CollectableSorting toCollectableSorting(SortingOrderVO sortingordervo, AttributeProvider attrprovider) {
		final String sFieldName = attrprovider.getAttribute(sortingordervo.getAttributeId()).getName();
		final boolean bAscending = SortingOrderVO.ASCENDING.equals(sortingordervo.getDirection());
		return new CollectableSorting(sFieldName, bAscending);
	}

	public static class FromCollectableSorting implements Transformer<CollectableSorting, SortingOrderVO> {
		private final AttributeProvider attrprovider;
		private final String sEntity;

		public FromCollectableSorting(AttributeProvider attrprovider, String sEntity) {
			this.attrprovider = attrprovider;
			this.sEntity = sEntity;
		}

		@Override
		public SortingOrderVO transform(CollectableSorting clctsorting) {
			return fromCollectableSorting(clctsorting, attrprovider, this.sEntity);
		}
	}

	public static class ToCollectableSorting implements Transformer<SortingOrderVO, CollectableSorting> {
		private final AttributeProvider attrprovider;

		public ToCollectableSorting(AttributeProvider attrprovider) {
			this.attrprovider = attrprovider;
		}

		@Override
		public CollectableSorting transform(SortingOrderVO sortingordervo) {
			return toCollectableSorting(sortingordervo, attrprovider);
		}
	}

	public static class FromAttributeName implements Transformer<String, SortingOrderVO> {
		final AttributeProvider attrprovider;
		private final String sDirection;
		private final String sEntity;

		public FromAttributeName(AttributeProvider attrprovider, String sEntity, String sDirection) {
			this.attrprovider = attrprovider;
			this.sDirection = sDirection;
			this.sEntity = sEntity;
		}

		@Override
		public SortingOrderVO transform(String sAttributeName) {
			return new SortingOrderVO(attrprovider.getAttribute(this.sEntity, sAttributeName).getId(), sDirection);
		}
	}
}	// class SortingOrderVO

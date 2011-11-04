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
package org.nuclos.common.genericobject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.TransformerUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;

/**
 * Utility methods for leased objects, client and server usage.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class GenericObjectUtils {

	private GenericObjectUtils() {
	}

	/**
	 * @param collmdvo May be null.
	 * @param sFieldName
	 * @return the concatenated values of the field with the given name, for each element of the given collection.
	 * @todo refactor using CollectionUtils.getSeparatedList(Iterable<?>, java.lang.String)
	 */
	public static String getConcatenatedValue(Collection<EntityObjectVO> collmdvo, String sFieldName) {
		final StringBuffer sb = new StringBuffer();
		if (collmdvo != null) {
			for (Iterator<EntityObjectVO> iter = collmdvo.iterator(); iter.hasNext();) {
				final EntityObjectVO mdvo = iter.next();
				sb.append(new CollectableValueField(mdvo.getField(sFieldName, Object.class)).toString());
				if (iter.hasNext()) {
					sb.append(" ");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @param lstclctefwe
	 * @param sMainEntityName name of the main entity
	 * @return the attribute ids of the selected fields that belong to the main entity
	 * @postcondition result != null
	 * 
	 * @deprecated As AttributeProvider is deprecated, this is deprecated as well.
	 */
	public static List<Integer> getAttributeIds(List<? extends CollectableEntityField> lstclctefwe, String sMainEntityName, AttributeProvider ap) {
		final List<? extends CollectableEntityField> lstclctefMain = CollectionUtils.select(lstclctefwe, new CollectableEntityField.HasEntity(sMainEntityName));
		return CollectionUtils.transform(lstclctefMain, TransformerUtils.chained(new CollectableEntityField.GetName(), new AttributeProvider.GetAttributeIdByName(sMainEntityName, ap)));
	}

}	// class GenericObjectUtils

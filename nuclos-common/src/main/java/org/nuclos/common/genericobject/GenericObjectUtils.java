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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.SpringApplicationContextHolder;
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
	/**
	 * the suffix for the name of the "virtual" field that refers to the parent object for submodules.
	 * The <code>parentId</code> is a property of GenericObjectVO. As we don't have such a thing in <code>CollectableEntity</code>,
	 * we need to map this property to a "virtual" <code>CollectableEntityField</code>.
	 */
	private static final String FIELDNAMESUFFIX_PARENTOBJECT = "_parentObject";

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

	/**
	 * @param iModuleId
	 * @param sSubEntitySuffix
	 * @return the name of the subentity with the given suffix belonging to the main entity with the given module id.
	 */
	public static String getSubEntityName(Integer iModuleId, String sSubEntitySuffix) {
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		return modules.getEntityNameByModuleId(iModuleId) + sSubEntitySuffix;
	}

	/**
	 * @param collclctefwe
	 * @param sMainEntityName name of the main entity
	 * @return the names of subentities contained in the given collection
	 * @postcondition result != null
	 */
	public static Set<String> getSubEntityNames(Collection<? extends CollectableEntityField> collclctefwe, String sMainEntityName, ModuleProvider mp) {
		final Set<String> result = new HashSet<String>(CollectionUtils.transform(collclctefwe, new CollectableEntityField.GetEntityName()));
		// remove the names of the main entity and the parent entity, if any:
		result.remove(sMainEntityName);
		result.remove(mp.getParentEntityName(sMainEntityName));
		assert result != null;
		return result;
	}

	/**
	 * @param clctefweSelected
	 * @param sParentEntityName name of the parent entity. May be <code>null</code>.
	 * @return Does the given list contain a field of the parent entity?
	 * @precondition clctefweSelected != null
	 * @postcondition (sParentEntityName == null) --> !result
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public static boolean containsParentField(List<? extends CollectableEntityField> clctefweSelected, String sParentEntityName) {
		return (sParentEntityName != null) && CollectionUtils.exists(clctefweSelected, new CollectableEntityField.HasEntity(sParentEntityName));
	}

	/**
	 * @param sSubEntityName
	 * @return the name of the "virtual" parent object field for the subentity with the given name.
	 * The "parentObject" property of <code>GenericObjectVO</code> is mapped to a corresponding
	 * <code>CollectableEntityField</code> with the name returned by this method.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public static String getParentObjectFieldName(String sSubEntityName) {
		return sSubEntityName + FIELDNAMESUFFIX_PARENTOBJECT;
	}

}	// class GenericObjectUtils

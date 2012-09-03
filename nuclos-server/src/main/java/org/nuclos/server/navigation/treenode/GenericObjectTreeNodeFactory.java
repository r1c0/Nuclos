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
package org.nuclos.server.navigation.treenode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;

/**
 * Factory that creates <code>GenericObjectTreeNode</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericObjectTreeNodeFactory {

	private static GenericObjectTreeNodeFactory singleton;

	public static synchronized GenericObjectTreeNodeFactory getInstance() {
		if (singleton == null) {
			singleton = newFactory();
		}
		return singleton;
	}

	private static GenericObjectTreeNodeFactory newFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getGenericObjectTreeNodeFactoryClassName(),
					GenericObjectTreeNodeFactory.class.getName());

			return (GenericObjectTreeNodeFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("GenericObjectTreeNodeFactory cannot be created.", ex);
		}
	}

	/**
	 * creates a GenericObjectTreeNode.
	 * @param gowdvo
	 * @param attrprovider
	 * @param paramprovider
	 * @return a new GenericObjectTreeNode
	 * @postcondition result != null
	 */
	public GenericObjectTreeNode newTreeNode(GenericObjectWithDependantsVO gowdvo,
			AttributeProvider attrprovider, ParameterProvider paramprovider,
			Integer iRelationId, SystemRelationType relationtype, RelationDirection direction,
			String sUserName, Integer parentId) {
		String label = getIdentifier(gowdvo, attrprovider, sUserName);
		String description = getDescription(gowdvo, attrprovider, gowdvo.getChangedAt(), sUserName);
		return new GenericObjectTreeNode(gowdvo, attrprovider, iRelationId, relationtype, direction, sUserName, label, description, parentId, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	}

	/**
	 * get the representation of this node in the tree
	 * @param gowdvo
	 * @param attrprovider
	 * @return
	 */
	protected String getIdentifier(GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, String username) {
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
		Map<String, Object> values = getReadableAttributes(username, gowdvo, attrprovider);
		return SpringLocaleDelegate.getInstance().getTreeViewLabel(
				values, modules.getEntityNameByModuleId(gowdvo.getModuleId()), metaprovider);
	}

	/**
	 * get the description of the representation of this node in the tree
	 * @param gowdvo
	 * @param attrprovider
	 * @param dateChangedAt
	 * @return
	 */
	public String getDescription(GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, Date dateChangedAt, String username){
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
		Map<String, Object> values = getReadableAttributes(username, gowdvo, attrprovider);
		return SpringLocaleDelegate.getInstance().getTreeViewDescription(
				values, modules.getEntityNameByModuleId(gowdvo.getModuleId()), metaprovider);
	}

	private Map<String, Object> getReadableAttributes(String sUserName, GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider) {
		String sResIfNoPermission = StringUtils.defaultIfNull(
				SpringApplicationContextHolder.getBean(ParameterProvider.class).getValue(ParameterProvider.KEY_BLUR_FILTER),"***");
		Map<String, Object> values = new HashMap<String, Object>();
		for (DynamicAttributeVO att : gowdvo.getAttributes()) {
			String attname = attrprovider.getAttribute(att.getAttributeId()).getName();
			if (isReadAllowedForAttribute(sUserName, attname, gowdvo, attrprovider)) {
				values.put(attname, att.getValue());
			}
			else {
				values.put(attname, sResIfNoPermission);
			}
		}
		return values;
	}

	/**
	 * check whether the data of the attribute is readable for current user
	 *
	 * @param sUserName
	 * @param sKey
	 * @param gowdvo
	 * @param attrprovider
	 * @return true, if attribute data is readable, otherwise false
	 */
	public boolean isReadAllowedForAttribute(String sUserName, String sKey, GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider) {
		Integer iAttributeGroupId = SpringApplicationContextHolder.getBean(GenericObjectMetaDataCache.class).getAttribute(gowdvo.getModuleId(), sKey).getAttributegroupId();
		Permission permission = SecurityCache.getInstance().getAttributeGroup(sUserName, iAttributeGroupId).get(gowdvo.getStatusId());

		return (permission == null) ? false : permission.includesReading();
	}
}	// class GenericObjectTreeNodeFactory

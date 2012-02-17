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
package org.nuclos.server.attribute.ejb3;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

/**
 * Attribute facade bean encapsulating access functions for dynamic attributes.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Stateless
// @Local(AttributeFacadeLocal.class)
// @Remote(AttributeFacadeRemote.class)
@Transactional
public class AttributeFacadeBean extends NuclosFacadeBean implements AttributeFacadeLocal, AttributeFacadeRemote {
	
	private AttributeCache attributeCache;
	
	public AttributeFacadeBean() {
	}
	
	@Autowired
	void setAttributeCache(AttributeCache attributeCache) {
		this.attributeCache = attributeCache;
	}

	/**
	 * @return a collection containing all dynamic attributes
	 */
	@Override
	@RolesAllowed("Login")
	public Collection<AttributeCVO> getAttributes(Integer iGroupId) {
		final Collection<AttributeCVO> result = new HashSet<AttributeCVO>();

		final SecurityCache securitycache = SecurityCache.getInstance();

		for (AttributeCVO attrcvo : attributeCache.getAttributes()) {
			if (iGroupId == null || iGroupId.equals(attrcvo.getAttributegroupId())) {
				final AttributeCVO attrcvoClone = attrcvo.clone();
				attrcvoClone.setPermissions(securitycache.getAttributeGroup(this.getCurrentUserName(), attrcvoClone.getAttributegroupId()));
				result.add(attrcvoClone);
			}
		}
		return result;
	}

	/**
	 * @param iAttributeId id of attribute
	 * @return the attribute value object for the attribute with the given id
	 * @throws CommonPermissionException
	 * @precondition iAttributeId != null
	 */
	@Override
	public AttributeCVO get(Integer iAttributeId) throws CommonFinderException, CommonPermissionException {
//		this.checkReadAllowed(NuclosEntity.ATTRIBUTE);
		if (iAttributeId == null) {
			throw new NullArgumentException("iAttributeId");
		}
		final AttributeCVO result;
		try {
			result = attributeCache.getAttribute(iAttributeId);
		}
		catch (NuclosAttributeNotFoundException ex) {
			throw new CommonFinderException(ex);
		}
		return result;
	}

	/**
	 * invalidates the attribute cache (console function)
	 */
	@Override
	@RolesAllowed("Login")
	public void invalidateCache() {
		this.invalidateCache(null);
	}

	/**
	 * invalidates the attribute cache
	 */
	@Override
	public void invalidateCache(Integer iAttributeId) {
		GenericObjectMetaDataCache.getInstance().attributeChanged(iAttributeId);
	}

	/**
	 * @return the available calculation functions for calculated attributes
	 */
	@Override
	@RolesAllowed("Login")
	public Collection<String> getCalculationFunctions() {
		return CollectionUtils.applyFilter(dataBaseHelper.getDbAccess().getCallableNames(), new Predicate<String>() {
			@Override public boolean evaluate(String name) {
				return StringUtils.toUpperCase(name).startsWith("CA");
			};
		});
	}

	/**
	 *
	 * @param sAttributeName
	 * @return the layouts that contained this attribute
	 */
	@Override
	@RolesAllowed("Login")
	public Set<String> getAttributeLayouts(String sAttributeName){
		Set<String> sLayoutsName = new HashSet<String>();
		LayoutMLParser parser = new LayoutMLParser();
		Map<Integer, String> mLayoutMap = GenericObjectMetaDataCache.getLayoutMap();
		Iterator<Integer> iLayoutsIds = mLayoutMap.keySet().iterator();
		while(iLayoutsIds.hasNext()){
			Integer iId = iLayoutsIds.next();
			try{
				if (mLayoutMap.get(iId) != null) {
					Set<String> set = parser.getCollectableFieldNames(new InputSource(new StringReader(mLayoutMap.get(iId))));
					if(set.contains(sAttributeName))
						sLayoutsName.add(GenericObjectMetaDataCache.getLayoutName(iId));
				}
			}
			catch(LayoutMLException e){
				throw new CommonFatalException(e);
			}
		}
		return sLayoutsName;
	}
	
	@Override
	@RolesAllowed("Login")
	public Set<String> getAttributeForModule(String sModuleId) {
		
		Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(sModuleId);
		
		Set<String> setAttributes = new HashSet<String>();
		Map<Integer, String> mLayoutMap = GenericObjectMetaDataCache.getLayoutMap();
		List<Integer> lstLayoutIds = GenericObjectMetaDataCache.getInstance().getLayoutIdsForModule(iModuleId);
		for(Integer iLayoutId : lstLayoutIds) {
			LayoutMLParser parser = new LayoutMLParser();
			try{
				Set<String> set = parser.getCollectableFieldNames(new InputSource(new StringReader(mLayoutMap.get(iLayoutId))));
				setAttributes.addAll(set);
			}
			catch(LayoutMLException e){
				throw new CommonFatalException(e);
			}
		}	
		return setAttributes;
	}
	
}

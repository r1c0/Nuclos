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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.masterdata.ejb3.MetaDataFacadeLocal;

/**
 * Singleton class for getting attribute value objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class AttributeCache implements AttributeProvider {

	private static AttributeCache singleton;

	private Map<Integer, AttributeCVO> mpAttributesById;

	private Map<String, List<AttributeCVO>> mpAttributesByExternalEntity;

	public static synchronized AttributeCache getInstance() {
		return (AttributeCache) SpringApplicationContextHolder.getBean("attributeProvider");
	}

	protected AttributeCache() {
		this.validate();
	}

	public synchronized void update(AttributeCVO attrcvo) {
		this.validate();

		mpAttributesById.put(attrcvo.getId(), attrcvo);

		if(!StringUtils.isNullOrEmpty(attrcvo.getExternalEntity())) {
			mpAttributesByExternalEntity.get(attrcvo.getExternalEntity()).add(attrcvo);
		}
	}

	public synchronized void remove(AttributeCVO attrcvo) {
		this.validate();

		this.mpAttributesById.remove(attrcvo.getId());
		this.mpAttributesByExternalEntity.get(attrcvo.getExternalEntity()).remove(attrcvo);
	}

	/**
	 * @postcondition result != null
	 */
	@Override
	public synchronized AttributeCVO getAttribute(int iAttributeId) {
		this.validate();

		final AttributeCVO result = this.mpAttributesById.get(iAttributeId);
		if (result == null) {
			throw new NuclosAttributeNotFoundException(iAttributeId);
		}
		assert result != null;
		return result;
	}

	public synchronized boolean contains(int iAttribute) {
		this.validate();

		return this.mpAttributesById.containsKey(iAttribute);
	}


	@Override
	public AttributeCVO getAttribute(Integer iEntityId, String sAttributeName)
		throws NuclosAttributeNotFoundException {
		return getAttribute(Modules.getInstance().getEntityNameByModuleId(iEntityId), sAttributeName);
	}

	@Override
	public synchronized AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.validate();

		try {
			final AttributeCVO result = getAttribute(LangUtils.convertId(MetaDataServerProvider.getInstance().getEntityField(sEntity, sAttributeName).getId()));
			if (result == null) {
				throw new NuclosAttributeNotFoundException(sAttributeName);
			}
			assert result != null;
			return result;
		} catch (CommonFatalException ex) {
			throw new NuclosAttributeNotFoundException(sAttributeName);
		}
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException {
		return MetaDataServerProvider.getInstance().getEntityField(entity, field);
	}


	@Override
	public synchronized Collection<AttributeCVO> getAttributes() {
		this.validate();
		return new HashSet<AttributeCVO>(mpAttributesById.values());
	}

	public synchronized void revalidate() {
		this.invalidate();
		this.validate();
	}

	public synchronized void invalidate() {
		mpAttributesById = null;
		mpAttributesByExternalEntity = null;
	}

	private synchronized void validate() {
		if (mpAttributesById == null || mpAttributesByExternalEntity == null) {

			try {
				final Logger log = Logger.getLogger(this.getClass());
				log.debug("START building attribute cache.");
				mpAttributesById = Collections.synchronizedMap(new HashMap<Integer, AttributeCVO>());
				mpAttributesByExternalEntity = Collections.synchronizedMap(new HashMap<String, List<AttributeCVO>>());

//				DboFacadeLocal dboFacade = ServiceLocator.getInstance().getFacade(DboFacadeLocal.class);
				MetaDataFacadeLocal metaFacade = ServiceLocator.getInstance().getFacade(MetaDataFacadeLocal.class);

				for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
					if (DalUtils.isNuclosProcessor(eMeta) || !eMeta.isStateModel()) {
						continue;
					}
					for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values()) {
						Map<Integer, Permission> permissions = SecurityCache.getInstance().getAttributeGroup("", LangUtils.convertId(efMeta.getFieldGroupId()));
						AttributeCVO attrCVO = DalSupportForGO.getAttributeCVO(efMeta, permissions);

//				for (Attribute a : dboFacade.getAttributes()) {
//					Map<Integer, Permission> permissions = SecurityCache.getInstance().getAttributeGroup("", a.getAttributegroupId());
//					AttributeCVO attrCVO = VOFactory.FACTORY.createAttributeCVO(a, permissions, values.get(a.getId()));

					mpAttributesById.put(attrCVO.getId(), attrCVO);

					if(!StringUtils.isNullOrEmpty(attrCVO.getExternalEntity())) {
						if(mpAttributesByExternalEntity.containsKey(attrCVO.getExternalEntity())) {
							mpAttributesByExternalEntity.get(attrCVO.getExternalEntity()).add(attrCVO);
						}
						else {
							List<AttributeCVO> lstAttributeCVO = new ArrayList<AttributeCVO>();
							lstAttributeCVO.add(attrCVO);
							mpAttributesByExternalEntity.put(attrCVO.getExternalEntity(), lstAttributeCVO);
						}
					}
//				}

				}}


				log.debug("FINISHED building attribute cache.");
			}
			catch(Exception e) {
				throw new CommonFatalException(e);
			}
		}
	}

	/**
	 * @param sEntity
	 * @return a list of AttributeCVO which contains a reference to another entity
	 */
	public synchronized Collection<AttributeCVO> getReferencingAttributes(String sEntity) {
		this.validate();
		return CollectionUtils.emptyIfNull(mpAttributesByExternalEntity.get(sEntity));
	}

}	// class AttributeCache
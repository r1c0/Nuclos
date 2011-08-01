//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.common.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.entityobject.EntityObjectProxyList;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.springframework.transaction.annotation.Transactional;


/**
 * Server implementation of the EntityObjectFacadeRemote interface.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
@Transactional
public class EntityObjectFacadeBean extends NuclosFacadeBean implements EntityObjectFacadeRemote {
	
	private static final Logger LOG = Logger.getLogger(EntityObjectFacadeBean.class);
	
	public EntityObjectFacadeBean() {
	}

	@Override
	public List<Long> getEntityObjectIds(Long id, CollectableSearchExpression cse) {
		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(id);
		final List<Long> ids = NucletDalProvider.getInstance().getEntityObjectProcessor(
				eMeta.getEntity()).getIdsBySearchExprUserGroups(
						appendRecordGrants(cse, eMeta.getEntity()),	id, getCurrentUserName());
		return ids;
	}

	@Override
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr,
			Set<Long> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects,
			boolean bIncludeSubModules) {
		return new EntityObjectProxyList(id, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, bIncludeParentObjects);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds,
			Set<Long> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects) {
		final EntityMetaDataVO eMeta = MetaDataServerProvider.getInstance().getEntity(id);
		final List<EntityObjectVO> eos = NucletDalProvider.getInstance().getEntityObjectProcessor(
				eMeta.getEntity()).getBySearchExpressionAndPrimaryKeys(
						appendRecordGrants(new CollectableSearchExpression(), eMeta), lstIds);
		for (EntityObjectVO eo : eos) {
			try {
				fillDependants(eo, stRequiredSubEntityNames);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
		}
		return eos;
	}

	/**
	 * fills the dependants of <code>lowdcvo</code> with the data from the required sub entities.
	 * @param base
	 * @param stRequiredSubEntityNames
	 * @param subEntities
	 * @precondition stRequiredSubEntityNames != null
	 */
	private void fillDependants(EntityObjectVO base, Set<String> stRequiredSubEntityNames) 
			throws CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new NullArgumentException("stRequiredSubEntityNames");
		}
		// final String username = getCurrentUserName();
		final DependantMasterDataMap dmdm = base.getDependants();
		for (String s: stRequiredSubEntityNames) {
			final String refField = findRefField(base, s);
			if (refField == null) {
				LOG.warn("Can't find ref field from " + s + " to " + base.getEntity());
				continue;
			}
			final Collection<EntityObjectVO> col = getDependantEntityObjects(s, refField, base.getId());
			dmdm.addAllData(s, col);
		}
		base.setDependants(dmdm);
	}
	
	/**
	 * TODO: Enhance this to 'generic' searchdocuments etc.
	 */
	private String findRefField(EntityObjectVO base, String subform) {
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final Map<String, EntityFieldMetaDataVO> fields = mdProv.getAllEntityFieldsByEntity(subform);
		for (String f: fields.keySet()) {
			final EntityFieldMetaDataVO mdField = fields.get(f);
			final String fEntity = mdField.getForeignEntity();
			if (base.getEntity().equals(fEntity) || NuclosEntity.GENERICOBJECT.getEntityName().equals(fEntity)) {
				return f;
			}
		}
		return null;
	}

	@Override
	public Collection<EntityObjectVO> getDependantEntityObjects(String subform, String field, Long relatedId) {
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final CollectableComparison cond = SearchConditionUtils.newEOidComparison(
				subform, field, ComparisonOperator.EQUAL, relatedId, mdProv);
		return NucletDalProvider.getInstance().getEntityObjectProcessor(subform).getBySearchExpression(
				new CollectableSearchExpression(cond));
	}
	
}

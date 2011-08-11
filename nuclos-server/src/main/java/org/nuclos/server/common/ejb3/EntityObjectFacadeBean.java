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
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PivotJoinCondition;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
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
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr, Collection<EntityFieldMetaDataVO> fields) {
		return new EntityObjectProxyList(id, clctexpr, fields);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds, Collection<EntityFieldMetaDataVO> fields) {
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO eMeta = mdProv.getEntity(id);
			
		JdbcEntityObjectProcessor eop = NucletDalProvider.getInstance().getEntityObjectProcessor(
				eMeta.getEntity());
		// 
		eop = (JdbcEntityObjectProcessor) eop.clone();
		for (EntityFieldMetaDataVO f: fields) {
			if (f.getPivotInfo() != null) {
				eop.addToColumns(f);
			}
		}
		
		final List<EntityObjectVO> eos = eop.getBySearchExpressionAndPrimaryKeys(
				appendRecordGrants(getSearchExpression(fields), eMeta), lstIds);
		/*
		// TODO: join table...
		final Set<String> subforms = new HashSet<String>();
		final Collection<EntityFieldMetaDataVO> pivots = new ArrayList<EntityFieldMetaDataVO>();
		for (EntityFieldMetaDataVO f: fields) {
			final PivotInfo pinfo = f.getPivotInfo();
			if (pinfo != null) {
				pivots.add(f);
			}
			else {
				final String subform = mdProv.getEntity(f.getEntityId()).getEntity();
				subforms.add(subform);
			}
		}
		for (EntityObjectVO eo : eos) {
			try {
				fillDependants(eo, subforms);
				fillPivots(eo, pivots);
			}
			catch(CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
		}
		 */
		return eos;
	}
	
	private CollectableSearchExpression getSearchExpression(Collection<EntityFieldMetaDataVO> fields) {
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		// TODO: more than one pivot...
		CollectableSearchCondition join = null;
		for (EntityFieldMetaDataVO f: fields) {
			if (f.getPivotInfo() != null) {
				final EntityMetaDataVO subEntity = mdProv.getEntity(f.getEntityId());
				join = new PivotJoinCondition(subEntity, f);
				break;
			}
		}
		if (join == null) {
			join = TrueCondition.TRUE;
		}
		return new CollectableSearchExpression(join);
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
			final Collection<EntityObjectVO> col = getDependentEntityObjects(s, refField, base.getId());
			dmdm.addAllData(s, col);
		}
		// base.setDependants(dmdm);
	}
	
	private void fillPivots(EntityObjectVO base, Collection<EntityFieldMetaDataVO> pivots) 
			throws CommonFinderException {

		if (pivots == null) {
			return;
		}
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		// final String username = getCurrentUserName();
		final DependantMasterDataMap dmdm = base.getDependants();
		for (EntityFieldMetaDataVO p : pivots) {
			final EntityMetaDataVO mdEntity = mdProv.getEntity(p.getEntityId());
			final String subform = mdEntity.getEntity();
			final String refField = findRefField(base, subform);
			if (refField == null) {
				LOG.warn("Can't find ref field from " + p + " to " + base.getEntity());
				continue;
			}
			final Collection<EntityObjectVO> col = getDependentPivotEntityObjects(p, refField, base.getId());
			dmdm.addAllData(subform, col);
		}
		// base.setDependants(dmdm);
	}
	
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
	public Collection<EntityObjectVO> getDependentEntityObjects(String subform, String field, Long relatedId) {
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final CollectableSearchCondition cond = SearchConditionUtils.newEOidComparison(
				subform, field, ComparisonOperator.EQUAL, relatedId, mdProv);
		return NucletDalProvider.getInstance().getEntityObjectProcessor(subform).getBySearchExpression(
				new CollectableSearchExpression(cond));
	}

	@Override
	public Collection<EntityObjectVO> getDependentPivotEntityObjects(EntityFieldMetaDataVO pivot, String sForeignKeyField,
			Long oRelatedId) {
		
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdEntity = mdProv.getEntity(pivot.getEntityId());
		final PivotInfo info = pivot.getPivotInfo();
		final String subform = info.getSubform();
		assert subform.equals(mdEntity.getEntity());
		final CollectableSearchCondition condJoin = SearchConditionUtils.newEOidComparison(
				subform, sForeignKeyField, ComparisonOperator.EQUAL, oRelatedId, mdProv);
		final CollectableSearchCondition condKey = new CollectableComparison(
				new CollectableEOEntityField(mdProv.getEntityField(subform, info.getKeyField()), subform), 
				ComparisonOperator.EQUAL, 
				new CollectableValueField(pivot.getField()));
		final CompositeCollectableSearchCondition composite = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		composite.addOperand(condJoin);
		composite.addOperand(condKey);
		return NucletDalProvider.getInstance().getEntityObjectProcessor(subform).getBySearchExpression(
				new CollectableSearchExpression(composite));
	}
	
}

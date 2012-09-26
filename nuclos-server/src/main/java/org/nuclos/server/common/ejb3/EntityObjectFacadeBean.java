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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
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
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.entityobject.EntityObjectProxyList;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
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
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class EntityObjectFacadeBean extends NuclosFacadeBean implements EntityObjectFacadeRemote {

	private LayoutFacadeLocal layoutFacade;

	private static final Logger LOG = Logger.getLogger(EntityObjectFacadeBean.class);

	public EntityObjectFacadeBean() {
	}
	
	private LayoutFacadeLocal getLayoutFacade() {
		if (layoutFacade == null)
			layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
		return layoutFacade;
	}

	@Override
	public EntityObjectVO get(String entity, Long id) throws CommonPermissionException {
		EntityMetaDataVO meta = MetaDataServerProvider.getInstance().getEntity(entity);
		if (meta.isStateModel()) {
			checkReadAllowedForModule(IdUtils.unsafeToId(meta.getId()), IdUtils.unsafeToId(id));
		}
		else {
			checkReadAllowed(entity);
		}

		getRecordGrantUtils().checkInternal(entity, id);
		JdbcEntityObjectProcessor eop = NucletDalProvider.getInstance().getEntityObjectProcessor(entity);
		return eop.getByPrimaryKey(id);
	}

	@Override
	public EntityObjectVO getReferenced(String referencingEntity, String referencingEntityField, Long id) throws CommonBusinessException {
//		checkWriteAllowed(referencingEntity);

		EntityFieldMetaDataVO fieldmeta = MetaDataServerProvider.getInstance().getEntityField(referencingEntity, referencingEntityField);
		if (fieldmeta.getForeignEntity() == null && fieldmeta.getLookupEntity() == null) {
			throw new NuclosFatalException("Field " + referencingEntity + "." + referencingEntityField + " is not a reference or lookup field.");
		}
		else {
			JdbcEntityObjectProcessor eop = NucletDalProvider.getInstance().getEntityObjectProcessor(fieldmeta.getForeignEntity() != null ? fieldmeta.getForeignEntity() : fieldmeta.getLookupEntity());
			return eop.getByPrimaryKey(id);
		}
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
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr, Collection<EntityFieldMetaDataVO> fields, String customUsage) {
		final CollectableSearchCondition search = getSearchCondition(clctexpr.getSearchCondition(), fields);
		clctexpr.setSearchCondition(search);
		return new EntityObjectProxyList(id, clctexpr, fields, customUsage);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds, Collection<EntityFieldMetaDataVO> fields, String customUsage) {
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO eMeta = mdProv.getEntity(id);

		JdbcEntityObjectProcessor eop = NucletDalProvider.getInstance().getEntityObjectProcessor(
				eMeta.getEntity());
		//
		final ProcessorFactorySingleton processorFac = ProcessorFactorySingleton.getInstance();
		eop = (JdbcEntityObjectProcessor) eop.clone();
		for (EntityFieldMetaDataVO f: fields) {
			if (f.getPivotInfo() != null) {
				processorFac.addToColumns(eop, f);
			}
		}

		final CollectableSearchExpression search = new CollectableSearchExpression(getSearchCondition(null, fields));
		final List<EntityObjectVO> eos = eop.getBySearchExpressionAndPrimaryKeys(
				search /*appendRecordGrants(search, eMeta) - No check of recordgrants here*/, lstIds);

		final Set<String> subforms = new HashSet<String>();
		// final Collection<EntityFieldMetaDataVO> pivots = new ArrayList<EntityFieldMetaDataVO>();
		for (EntityFieldMetaDataVO f: fields) {
			final PivotInfo pinfo = f.getPivotInfo();
			if (pinfo != null) {
				// do nothing: now handled by join
				// pivots.add(f);
			}
			else {
				final String subform = mdProv.getEntity(f.getEntityId()).getEntity();
				subforms.add(subform);
			}
		}

		// fill in (dependent) subforms
		for (EntityObjectVO eo : eos) {
			try {
				fillDependants(eo, subforms, customUsage);
				// fillPivots(eo, pivots);
			}
			catch (CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
		}
		return eos;
	}

	private CollectableSearchCondition getSearchCondition(CollectableSearchCondition constrain, Collection<EntityFieldMetaDataVO> fields) {
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final List<CollectableSearchCondition> join = new ArrayList<CollectableSearchCondition>();
		for (EntityFieldMetaDataVO f: fields) {
			if (f.getPivotInfo() != null) {
				final EntityMetaDataVO subEntity = mdProv.getEntity(f.getEntityId());
				join.add(new PivotJoinCondition(subEntity, f));
			}
		}
		final int size = join.size();
		if (size == 0) {
			if (constrain == null) {
				constrain = TrueCondition.TRUE;
			}
		}
		else if (size > 1) {
			// TODO: Is this all right? What to do with more than one join?
			if (constrain != null) {
				join.add(constrain);
			}
			constrain = new CompositeCollectableSearchCondition(LogicalOperator.AND, join);
		}
		// size == 1
		else {
			if (constrain == null) {
				constrain = join.iterator().next();
			}
			else {
				join.add(constrain);
				constrain = new CompositeCollectableSearchCondition(LogicalOperator.AND, join);
			}
		}
		return constrain;
	}

	/**
	 * fills the dependants of <code>lowdcvo</code> with the data from the required sub entities.
	 * @param base
	 * @param stRequiredSubEntityNames
	 * @param subEntities
	 * @precondition stRequiredSubEntityNames != null
	 */
	private void fillDependants(EntityObjectVO base, Set<String> stRequiredSubEntityNames, String customUsage)
			throws CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new NullArgumentException("stRequiredSubEntityNames");
		}
		// final String username = getCurrentUserName();
		final DependantMasterDataMap dmdm = base.getDependants();
		final boolean log = LOG.isDebugEnabled();
		for (String s: stRequiredSubEntityNames) {
			final String refField = findRefField(base, s, customUsage);
			if (refField == null) {
				if (log) {
					LOG.debug("Can't find ref field from " + s + " to " + base.getEntity());
				}
				continue;
			}
			final Collection<EntityObjectVO> col = getDependentEntityObjects(s, refField, base.getId());
			dmdm.addAllData(s, col);
		}
		// base.setDependants(dmdm);
	}

	/**
	 * @deprecated Not in use any more.
	 */
	private void fillPivots(EntityObjectVO base, Collection<EntityFieldMetaDataVO> pivots, String customUsage)
			throws CommonFinderException {

		if (pivots == null) {
			return;
		}
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		// final String username = getCurrentUserName();
		final DependantMasterDataMap dmdm = base.getDependants();
		final boolean log = LOG.isDebugEnabled();
		for (EntityFieldMetaDataVO p : pivots) {
			final EntityMetaDataVO mdEntity = mdProv.getEntity(p.getEntityId());
			final String subform = mdEntity.getEntity();
			final String refField = findRefField(base, subform, customUsage);
			if (refField == null) {
				if (log) {
					LOG.warn("Can't find ref field from " + p + " to " + base.getEntity());
				}
				continue;
			}
			final Collection<EntityObjectVO> col = getDependentPivotEntityObjects(p, refField, base.getId());
			dmdm.addAllData(subform, col);
		}
		// base.setDependants(dmdm);
	}

	private String findRefField(EntityObjectVO base, String subform, String customUsage) throws CommonFinderException {
		final Map<EntityAndFieldName, String> collSubEntities;
		if (Modules.getInstance().isModuleEntity(base.getEntity()))
			collSubEntities = getLayoutFacade().getSubFormEntityAndParentSubFormEntityNamesByGO(DalSupportForGO.getGenericObjectVO(base).getUsageCriteria(AttributeCache.getInstance(), customUsage));
		else
			collSubEntities = getLayoutFacade().getSubFormEntityAndParentSubFormEntityNamesMD(base.getEntity(), false);
		
		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
			String sSubEntityName = eafn.getEntityName();
			// care only about subforms which are on the highest level and in the given set of entity names
			if (collSubEntities.get(eafn) == null && (subform.equals(sSubEntityName) || NuclosEntity.GENERICOBJECT.getEntityName().equals(sSubEntityName))) {
				String sForeignKeyField = eafn.getFieldName();
				if (sForeignKeyField == null) {
					sForeignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
				}
				return sForeignKeyField;
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

	@Override
	public void removeEntity(String name, Long id) throws CommonPermissionException {
		final Integer intid = IdUtils.unsafeToId(id);
		final String user = getCurrentUserName();
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdEntity = mdProv.getEntity(name);
		final SecurityCache sc = SecurityCache.getInstance();

		if (mdEntity.isStateModel().booleanValue()) {
			if (!sc.isDeleteAllowedForModule(user, name, intid, true)) {
				throw new CommonPermissionException("User " + user + " has no permission to delete module instance of " + name);
			}
		}
		else {
			if (!sc.isDeleteAllowedForMasterData(user, name)) {
				throw new CommonPermissionException("User " + user + " has no permission to delete md instance of " + name);
			}
		}

		final JdbcEntityObjectProcessor processor = NucletDalProvider.getInstance().getEntityObjectProcessor(name);
		processor.delete(id);
	}

	@Override
	public void remove(EntityObjectVO entity) throws CommonPermissionException {
		removeEntity(entity.getEntity(), entity.getId());
	}

	@Override
	public void createOrUpdatePlain(EntityObjectVO entity) throws CommonPermissionException {
		final String name = entity.getEntity();
		final String user = getCurrentUserName();
		final Integer intid = IdUtils.unsafeToId(entity.getId());
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdEntity = mdProv.getEntity(name);
		final SecurityCache sc = SecurityCache.getInstance();

		if (mdEntity.isStateModel().booleanValue()) {
			if (intid != null) {
				if (!sc.isWriteAllowedForModule(user, name, intid)) {
					throw new CommonPermissionException("User " + user + " has no permission to write module instance of " + name);
				}
				entity.flagUpdate();
			}
			else {
				if (!sc.isNewAllowedForModule(user, IdUtils.unsafeToId(mdEntity.getId()))) {
					throw new CommonPermissionException("User " + user + " has no permission to create module instance of " + name);
				}
				entity.flagNew();
			}
		}
		else {
			if (intid != null) {
				if (!sc.isWriteAllowedForMasterData(user, name)) {
					throw new CommonPermissionException("User " + user + " has no permission to write md instance of " + name);
				}
				entity.flagUpdate();
			}
			else {
				if (!sc.isNewAllowedForModule(user, IdUtils.unsafeToId(mdEntity.getId()))) {
					throw new CommonPermissionException("User " + user + " has no permission to create md instance of " + name);
				}
				entity.flagNew();
			}
		}

		final JdbcEntityObjectProcessor processor = NucletDalProvider.getInstance().getEntityObjectProcessor(name);
		processor.insertOrUpdate(entity);
	}

	@Override
	public Integer getVersion(String entity, Long id) throws CommonPermissionException {
		checkReadAllowed(entity);
		return getNucletDalProvider().getEntityObjectProcessor(entity).getVersion(id);
	}
}

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
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
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
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
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
		
		// load the system attribute nuclosProcess for this genericobject (if not already loaded);
		// this is necessary for getting the dependant subforms in the next step
		
//		Integer iAttributeId = NuclosEOField.PROCESS.getMetaData().getId().intValue();
//		if(!lowdcvo.wasAttributeIdLoaded(iAttributeId)) {
//			lowdcvo.addAttribute(iAttributeId);
//
//			Set<Integer> stAttributeId = new HashSet<Integer>();
//			stAttributeId.add(iAttributeId);
//
//			CollectableSearchExpression clctSearchExpression = new CollectableSearchExpression(new CollectableIdCondition(lowdcvo.getId()));
//			List<GenericObjectWithDependantsVO> lsgowdvo = getGenericObjects(lowdcvo.getModuleId(), clctSearchExpression, Collections.<String>emptySet(), username);
//			if (lsgowdvo.size() == 1 && lsgowdvo.get(0).getAttribute(iAttributeId) != null) {
//				lowdcvo.setAttribute(lsgowdvo.get(0).getAttribute(iAttributeId));
//			}
//		}
//
//		final Map<EntityAndFieldName, String> collSubEntities = getLayoutFacade().getSubFormEntityAndParentSubFormEntityNames(
//				Modules.getInstance().getEntityNameByModuleId(lowdcvo.getModuleId()),lowdcvo.getId(),false);
//
//
//		for (EntityAndFieldName eafn : collSubEntities.keySet()) {
//			String sSubEntityName = eafn.getEntityName();
//			// care only about subforms which are on the highest level and in the given set of entity names
//			if (collSubEntities.get(eafn) == null && stRequiredSubEntityNames.contains(sSubEntityName)) {
//				String sForeignKeyField = eafn.getFieldName();
//				if (sForeignKeyField == null) {
//					sForeignKeyField = ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME;
//				}
//				final Collection<EntityObjectVO> collmdvo = MasterDataFacadeHelper.getDependantMasterData(sSubEntityName, sForeignKeyField, lowdcvo.getId(), username);
//				if (CollectionUtils.isNonEmpty(collmdvo)) {
//					lowdcvo.getDependants().addAllData(sSubEntityName, collmdvo);
//				}
//			}
//		}
	}
	
	/**
	 * TODO: Enhance this to 'generic' searchdocuments etc.
	 */
	private String findRefField(EntityObjectVO base, String subform) {
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdVo = mdProv.getEntity(subform);
		if (mdVo.hasFields()) {
			for (String f: mdVo.getFieldIds().keySet()) {
				// final Long id = mdVo.getFieldIds().get(f);
				final EntityFieldMetaDataVO mdField = mdProv.getEntityField(subform, f);
				if (base.getEntity().equals(mdField.getForeignEntity())) return f;
			}
		}
		return null;
	}

	@Override
	public Collection<EntityObjectVO> getDependantEntityObjects(String subform, String field, Long relatedId) {
		/*
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdVo = mdProv.getEntity(subform);
		final DbAccess dbAccess = DataBaseHelper.getDbAccess();
		final DbQueryBuilder builder = dbAccess.getQueryBuilder();
		final String dbView = EntityObjectMetaDbHelper.getViewName(mdVo);
		// final DbTable mdTable = dbAccess.getTableMetaData(dbView);
		
		final DbQuery<DbTuple> query = builder.createQuery(DbTuple.class);
		final DbFrom t = query.from(dbView).alias("t");
		query.multiselect(
			t.column("INTID_T_MD_STATE", Integer.class),
			t.column("BLNREADWRITE", Boolean.class));
		query.where(builder.and(
			t.column("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()),
			builder.equal(t.column("INTID_T_MD_ATTRIBUTEGROUP", Integer.class), iAttributeGroupId)));
		 */
		final MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		final CollectableComparison cond = SearchConditionUtils.newEOidComparison(
				subform, field, ComparisonOperator.EQUAL, relatedId, mdProv);
		return NucletDalProvider.getInstance().getEntityObjectProcessor(subform).getBySearchExpression(
				new CollectableSearchExpression(cond));
	}
	
	private void selectAll(DbQuery<DbTuple> query, DbFrom from, EntityMetaDataVO meta) {
		/*
		final Map<String,EntityFieldMetaDataVO> columns = (Map<String,EntityFieldMetaDataVO>) meta.getFields();
		final List<DbSelection<?>> selection = new ArrayList<DbSelection<?>>(columns.size());
		for (String c: columns.keySet()) {
			final EntityFieldMetaDataVO mdField = columns.get(c);
			selection.add(from.column(c, mdField.getDataType()));
		}
		 */
	}
	
}

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
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.entityobject.EntityObjectProxyList;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
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
	 * @param lowdcvo
	 * @param stRequiredSubEntityNames
	 * @param subEntities
	 * @precondition stRequiredSubEntityNames != null
	 * @deprecated This method doesn't respect the foreign key field name. Replace with fillDependants().
	 */
	@Deprecated
	private void fillDependants(EntityObjectVO lowdcvo,
			Set<String> stRequiredSubEntityNames) throws CommonFinderException {

		if (stRequiredSubEntityNames == null) {
			throw new NullArgumentException("stRequiredSubEntityNames");
		}
		final String username = getCurrentUserName();
		
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
	
}

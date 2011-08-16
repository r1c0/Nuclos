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
package org.nuclos.server.genericobject.ejb3;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for GenericObjectGroup.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(GenericObjectGroupFacadeLocal.class)
@Remote(GenericObjectGroupFacadeRemote.class)
@Transactional
@RolesAllowed("Login")
public class GenericObjectGroupFacadeBean extends NuclosFacadeBean implements GenericObjectGroupFacadeLocal, GenericObjectGroupFacadeRemote {

	/**
	 * @param iGenericObjectId
	 * @return the ids of the object group, the genericobject is assigned to
	 */
	@Override
	public Set<Integer> getObjectGroupId(Integer iGenericObjectId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_GROUP").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_UD_GROUP", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_UD_GENERICOBJECT", Integer.class), iGenericObjectId));
		return new HashSet<Integer>(DataBaseHelper.getDbAccess().executeQuery(query.distinct(true)));
	}

	/**
	 * @param iGenericObjectId
	 * @return the group name
	 */
	@Override
	public String getObjectGroupName(Integer iGenericObjectId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_UD_GROUP").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.baseColumn("STRGROUP", String.class));
		query.where(builder.equal(t.baseColumn("INTID", Integer.class), iGenericObjectId));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	/**
	 * @param iGroupId
	 * @return the ids of the genericobjects, which are assigned to the given objectgroup
	 */
	@Override
	public Set<Integer> getGenericObjectIdsForGroup(Integer iModuleId, Integer iGroupId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		
		if (iGroupId == 0) {
			DbFrom t = query.from("T_UD_GENERICOBJECT").alias(ProcessorFactorySingleton.BASE_ALIAS);
			query.select(t.baseColumn("INTID", Integer.class));
			query.where(builder.equal(t.baseColumn("INTID_T_MD_MODULE", Integer.class), iModuleId));
		} else {
			DbFrom t = query.from("T_UD_GO_GROUP").alias(ProcessorFactorySingleton.BASE_ALIAS);
			query.select(t.baseColumn("INTID", Integer.class));
			query.where(builder.equal(t.baseColumn("INTID_T_UD_GROUP", Integer.class), iGroupId));
		}
		
		return new HashSet<Integer>(DataBaseHelper.getDbAccess().executeQuery(query.distinct(true)));
	}

	/**
	 * removes the generic object with the given id from the group with the given id.
	 *
	 * @return
	 * @param iGenericObjectId generic object id to be removed. Must be a main module object.
	 * @param iGroupId id of group to remove generic object from
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws NuclosBusinessRuleException, CommonFinderException, CommonRemoveException,
			CommonStaleVersionException, CommonPermissionException, CommonCreateException
	{
		MasterDataMetaVO mdmetavo = getMasterDataFacade().getMetaData(NuclosEntity.GENERICOBJECTGROUP.getEntityName());
		Integer intId = findIntId(iGenericObjectId, iGroupId);
		MasterDataVO mdvo = MasterDataFacadeHelper.getMasterDataCVOById(mdmetavo, intId);
		getMasterDataFacade().remove(NuclosEntity.GENERICOBJECTGROUP.getEntityName(), mdvo, false);
	}

	private static Integer findIntId(int genericObjectId, int groupId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_GO_GROUP").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_UD_GENERICOBJECT", Integer.class), genericObjectId),
			builder.equal(t.baseColumn("INTID_T_UD_GROUP", Integer.class), groupId)));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	/**
	 * adds the generic object with the given id to the group with the given id.
	 *
	 * @return
	 * @param iGenericObjectId generic object id to be grouped.  Must be a main module object.
	 * @param iGroupId id of group to add generic object to
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
	public void addToGroup(int iGenericObjectId, int iGroupId)
		throws NuclosBusinessRuleException, CommonCreateException, CommonPermissionException
	{
		MasterDataMetaVO mdmetavo = getMasterDataFacade().getMetaData(NuclosEntity.GENERICOBJECTGROUP.getEntityName());
		MasterDataVO mdvo = new MasterDataVO(mdmetavo, true);
		mdvo.setField("groupId", iGroupId);
		mdvo.setField("genericObjectId", iGenericObjectId);
		mdvo = getMasterDataFacade().create(NuclosEntity.GENERICOBJECTGROUP.getEntityName(), mdvo, null);
	}
}

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
package org.nuclos.server.masterdata.ejb3;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

import org.nuclos.common.CommonMetaDataProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common.transport.vo.EntityMetaDataTO;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

/**
 * Remote facade for accessing meta data information from the
 * client side.
 */
@Remote
public interface MetaDataFacadeRemote extends CommonMetaDataProvider {

	@RolesAllowed("Login")
	Object modifyEntityMetaData(EntityMetaDataVO metaVO, List<EntityFieldMetaDataTO> lstFields);

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	void remove(String sEntityName, MasterDataVO mdvo,
		boolean bRemoveDependants) throws NuclosBusinessRuleException,
		CommonPermissionException, CommonStaleVersionException,
		CommonRemoveException, CommonFinderException;

	@RolesAllowed("Login")
	public String createOrModifyEntity(EntityMetaDataVO oldMDEntity, EntityMetaDataTO updatedMDEntity, MasterDataVO voEntity, List<EntityFieldMetaDataTO> lstFields, boolean blnExecute, String user, String password) throws NuclosBusinessException;

	@RolesAllowed("Login")
	public void invalidateServerMetadata();

	@RolesAllowed("Login")
	public Collection<MasterDataVO> hasEntityFieldInImportStructure(String sEntity, String sField);

	@RolesAllowed("Login")
	public boolean hasEntityRows(EntityMetaDataVO voEntity);

	@RolesAllowed("Login")
	public boolean hasEntityLayout(Long id);

	public String getResourceSIdForEntityFieldLabel(Integer iId);

	public String getResourceSIdForEntityFieldDescription(Integer iId);

	/**
	 * @return Script (with results if selected)
	 */
	@RolesAllowed("Login")
	public List<String> getDBTables();

	/**
	 * @return Script (with results if selected)
	 */
	@RolesAllowed("Login")
	public Map<String, MasterDataVO> getColumnsFromTable(String sTable);

	/**
	 * @return Script (with results if selected)
	 */
	@RolesAllowed("Login")
	public List<String> getTablesFromSchema(String url, String user, String password, String schema);

	/**
	 * @return Script (with results if selected)
	 */
	@RolesAllowed("Login")
	public MasterDataMetaVO transferTable(String url, String user, String password, String schema, String table, String sEntity);

	/**
	 * @return Script (with results if selected)
	 */
	@RolesAllowed("Login")
	public List<MasterDataVO> transformTable(String url, String user, String password, String schema, String table);

	/**
	 * force to change internal entity name
	 */
	@RolesAllowed("Login")
	public void changeEntityName(String newName, Integer id);

	/**
	 * force to change internal entity name
	 */
	@RolesAllowed("Login")
	public EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO vo);

	/**
	 * force to change internal entity name
	 */
	@RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToNotNullableAllowed(String sEntity, String field);

	/**
	 * force to change internal entity name
	 */
	@RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToUniqueAllowed(String sEntity, String field);

	@RolesAllowed("Login")
	public Collection<EntityMetaDataVO> getAllEntities();

	@RolesAllowed("Login")
	public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity);

	@RolesAllowed("Login")
	public Map<String, Map<String, EntityFieldMetaDataVO>> getAllEntityFieldsByEntitiesGz(Collection<String> entities);

	@RolesAllowed("Login")
	public Collection<EntityMetaDataVO> getNucletEntities();

	@RolesAllowed("Login")
	public void removeEntity(EntityMetaDataVO voEntity, boolean dropLayout) throws CommonBusinessException;

	@RolesAllowed("Login")
	public boolean hasEntityImportStructure(Long id) throws CommonBusinessException;

	@RolesAllowed("Login")
	public boolean hasEntityWorkflow(Long id) throws CommonBusinessException;

	@RolesAllowed("Login")
	public Long getEntityIdByName(String sEntity);

	@RolesAllowed("Login")
	public EntityMetaDataVO getEntityByName(String sEntity);

	@RolesAllowed("Login")
	public EntityMetaDataVO getEntityById(Long id);

	@RolesAllowed("Login")
	public List<String> getVirtualEntities();

	@RolesAllowed("Login")
	public List<EntityFieldMetaDataVO> getVirtualEntityFields(String virtualentity);

	@RolesAllowed("Login")
	public void tryVirtualEntitySelect(EntityMetaDataVO virtualentity) throws NuclosBusinessException;

	@RolesAllowed("Login")
	public void tryRemoveProcess(EntityObjectVO process) throws NuclosBusinessException;
}

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
package org.nuclos.client.masterdata;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common.transport.vo.EntityMetaDataTO;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MetaDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class MetaDataDelegate {
	private static MetaDataDelegate singleton;

	public static final String ENTITYNAME_ENTITY = "entity";

	private final MetaDataFacadeRemote facade;

	private final MasterDataFacadeRemote mdfacade;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	private MetaDataDelegate() {
		this.facade = ServiceLocator.getInstance().getFacade(MetaDataFacadeRemote.class);
		this.mdfacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
	}

	public static synchronized MetaDataDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new MetaDataDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}
	
	public Collection<MasterDataVO> hasEntityFieldInImportStructure(String sEntity, String sField) {
		return getMetaDataFacade().hasEntityFieldInImportStructure(sEntity, sField);
	}
	
	
	public void invalidateServerMetadata() {
		getMetaDataFacade().invalidateServerMetadata();
	}
	
	public Long getEntityIdByName(String sEntity) {
		return getMetaDataFacade().getEntityIdByName(sEntity);
	}

	public MetaDataFacadeRemote getMetaDataFacade() {
		return this.facade;
	}

	public MasterDataFacadeRemote getMasterDataFacade() {
		return this.mdfacade;
	}
	
	public boolean hasEntityRows(EntityMetaDataVO voEntity) {
		return this.facade.hasEntityRows(voEntity);
	}
	
	public boolean hasEntityLayout(Long id) {
		return this.facade.hasEntityLayout(id);
	}

//	private synchronized MetaDataCache getMetaDataCache() {
//		return MetaDataCache.getInstance();
//	}
	
	public  Object modifyEntityMetaData(EntityMetaDataVO metaVO, List<EntityFieldMetaDataTO> lstFields) {
		return this.getMetaDataFacade().modifyEntityMetaData(metaVO, lstFields);
	}
	
	public String getResourceSIdForEntityFieldLabel(Integer iId) {
		try {
			return this.getMetaDataFacade().getResourceSIdForEntityFieldLabel(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public String getResourceSIdForEntityFieldDescription(Integer iId) {
		try {
			return this.getMetaDataFacade().getResourceSIdForEntityFieldDescription(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(String sEntityName, MasterDataVO mdvo)
				throws CommonBusinessException{
		try {
			this.getMetaDataFacade().remove(sEntityName, mdvo, true);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	 * @param sOutputFileName the name of the csv file to which the results are written.
	 */
	public void checkMasterDataValues(String sOutputFileName) {
		try {
			this.getMasterDataFacade().checkMasterDataValues(sOutputFileName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * checks that all dependants (if any) have a <code>null</code> id.
	 * @param mpDependants may be <code>null</code>.
	 */
	public static void checkDependantsAreNew(DependantMasterDataMap mpDependants) {
		if (mpDependants != null && !mpDependants.areAllDependantsNew()) {
			throw new IllegalArgumentException("mpDependants");
		}
	}

	public String createOrModifyEntity(EntityMetaDataVO oldMDEntity, EntityMetaDataTO updatedMDEntity, MasterDataVO voEntity, List<EntityFieldMetaDataTO> lstFields, boolean blnExecute, String user, String password) throws NuclosBusinessException {
		try {
			return getMetaDataFacade().createOrModifyEntity(oldMDEntity, updatedMDEntity, voEntity, lstFields, blnExecute, user, password);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public List<String> getDBTables() {
		return getMetaDataFacade().getDBTables();
	}
	
	public Map<String, MasterDataVO> getColumnsFromTable(String sTable) {
		return getMetaDataFacade().getColumnsFromTable(sTable);
	}
	
	public List<String> getTablesFromSchema(String url, String user, String password, String schema) {
		return getMetaDataFacade().getTablesFromSchema(url, user, password, schema);
	}
	
	public MasterDataMetaVO transferTable(String url, String user, String password, String schema, String table, String sEntity) {
		return getMetaDataFacade().transferTable(url, user, password, schema, table, sEntity);
	}
	
	public List<MasterDataVO> transformTable(String url, String user, String password, String schema, String table) {
		return getMetaDataFacade().transformTable(url, user, password, schema, table);
	}
	
	public void changeEntityName(String newName, Integer id) {
		getMetaDataFacade().changeEntityName(newName, id);
	}
	
	public EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO vo) {
		return getMetaDataFacade().getEntityRelationshipModelVO(vo);
	}

	public boolean isChangeDatabaseColumnToNotNullableAllowed(String sEntity, String field) {
		return getMetaDataFacade().isChangeDatabaseColumnToNotNullableAllowed(sEntity, field);
	}
	
	public boolean isChangeDatabaseColumnToUniqueAllowed(String sEntity, String field) {
		return getMetaDataFacade().isChangeDatabaseColumnToUniqueAllowed(sEntity, field);
	}
	
	/**
	 * uses Server Cache
	 * @return
	 */
	public Collection<EntityMetaDataVO> getAllEntities() {
		return getMetaDataFacade().getAllEntities();
	}
	
	/**
	 * uses Server Cache
	 * @param entity
	 * @return
	 */
	public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		return getMetaDataFacade().getAllEntityFieldsByEntity(entity);
	}

	public Map<String, Map<String, EntityFieldMetaDataVO>> getAllEntityFieldsByEntitiesGz(List<String> entities) {
		return getMetaDataFacade().getAllEntityFieldsByEntitiesGz(entities);
    }
	
	public EntityFieldMetaDataVO getEntityField(String entity, String field) {
		for(EntityFieldMetaDataVO voField : this.getAllEntityFieldsByEntity(entity).values()) {
			if(voField.getField().equals(field))
				return voField;
		}
		throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
	}
	
	/**
	 * uses Server Cache
	 * @return
	 */
	public Collection<EntityMetaDataVO> getNucletEntities() {
		return getMetaDataFacade().getNucletEntities();
	}
	
	public void removeEntity(EntityMetaDataVO voEntity, boolean dropLayout) throws CommonBusinessException{
		getMetaDataFacade().removeEntity(voEntity, dropLayout);
	}

	public boolean hasEntityImportStructure(Long id) throws CommonBusinessException {
		return getMetaDataFacade().hasEntityImportStructure(id);
	}
	
	public boolean hasEntityWorkflow(Long id) throws CommonBusinessException {
		return getMetaDataFacade().hasEntityWorkflow(id);
	}
	
	public EntityMetaDataVO getEntityByName(String sEntity) {
		return getMetaDataFacade().getEntityByName(sEntity);
	}
	
	public EntityMetaDataVO getEntityById(Long id) {
		return getMetaDataFacade().getEntityById(id);
	}
}

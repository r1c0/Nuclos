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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.LafParameterMap;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.StaticMetaDataProvider;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common.transport.vo.EntityMetaDataTO;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dal.provider.SystemEntityMetaDataVO;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
* Facade bean for all meta data management functions (server side).
* <p>
* Uses the MetaDataServerProvider as implementation.
* </p>
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Transactional(noRollbackFor= {Exception.class})
public class MetaDataFacadeBean extends NuclosFacadeBean implements MetaDataFacadeRemote {

	private static final Logger LOG = Logger.getLogger(MetaDataFacadeBean.class);

	private MasterDataFacadeHelper helper;

	private final static String ENTITYFIELD_TABLE = "t_ad_masterdata_field";

	private ProcessorFactorySingleton processorFactory;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private DataSource dataSource;
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private LocaleFacadeLocal localeFacade;
	
	public MetaDataFacadeBean() {
	}

	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.helper = masterDataFacadeHelper;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	@Qualifier("nuclos")
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ProcessorFactorySingleton getProcessorFactory() {
		return processorFactory;
	}

	@Autowired
	void setProcessorFactory(ProcessorFactorySingleton processorFactory) {
		this.processorFactory = processorFactory;
	}

	@Autowired
	final void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}
	
	private final GenericObjectFacadeLocal getGenericObjectFacade() {
		return genericObjectFacade;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final LocaleFacadeLocal getLocaleFacade() {
		return localeFacade;
	}

	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}
	
	@Override
    @RolesAllowed("Login")
	public Collection<EntityMetaDataVO> getAllEntities() {
		return MetaDataServerProvider.getInstance().getAllEntities();
	}

	@Override
    @RolesAllowed("Login")
	public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		return MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(entity);
	}

	@Override
	public Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info) {
		return MetaDataServerProvider.getInstance().getAllPivotEntityFields(info);
	}

	@Override
    @RolesAllowed("Login")
	public Map<String, Map<String, EntityFieldMetaDataVO>> getAllEntityFieldsByEntitiesGz(Collection<String> entities) {
		return MetaDataServerProvider.getInstance().getAllEntityFieldsByEntitiesGz(entities);
	}

	@Override
    @RolesAllowed("Login")
	public Collection<EntityMetaDataVO> getNucletEntities() {
		return MetaDataServerProvider.getInstance().getAllEntities();
	}

	@Override
    public Object modifyEntityMetaData(EntityMetaDataVO metaVO, List<EntityFieldMetaDataTO> toFields) {

		metaVO = MetaDataServerProvider.getInstance().getEntity(metaVO.getEntity());

		List<EntityFieldMetaDataVO> lstFields = new ArrayList<EntityFieldMetaDataVO>();

		for(EntityFieldMetaDataTO to : toFields) {
			if(!to.getEntityFieldMeta().isFlagRemoved()) {
				lstFields.add(to.getEntityFieldMeta());
			}
		}
		
		EntityMetaDataVO voIst = MetaDataServerProvider.getInstance().getEntity(metaVO.getEntity());

		EntityObjectMetaDbHelper dbHelperIst = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
		DbTable tableIst = dbHelperIst.getDbTable(metaVO);

		StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider();
		staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(metaVO.getEntity()));

		Collection<EntityFieldMetaDataVO> colFields = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(metaVO.getEntity()).values();
		for(EntityFieldMetaDataVO vo : colFields) {
			boolean addField = true;
			for(EntityFieldMetaDataTO to : toFields) {
				if(to.getEntityFieldMeta().getField().equals(vo.getField())) {
					if(to.getEntityFieldMeta().isFlagRemoved()){
						addField = false;
					}
				}
				if(to.getEntityFieldMeta().getId() != null) {
					if(to.getEntityFieldMeta().getId().equals(vo.getId())) {
						addField = false;
					}
				}

			}
			if(addField) {
				staticMetaData.addEntityField(metaVO.getEntity(), vo);
				if(vo.getForeignEntity() != null) {
					vo.setReadonly(false);
					staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(vo.getForeignEntity()));
					for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(vo.getForeignEntity()).values()) {
						staticMetaData.addEntityField(vo.getForeignEntity(), voForeignField);
					}
				}
			}
		}
		for(EntityFieldMetaDataTO to : toFields) {
			EntityFieldMetaDataVO vo = to.getEntityFieldMeta();
			if(vo.getForeignEntity() != null) {
				if(!vo.isFlagRemoved()) {
					vo.setReadonly(false);
					staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(vo.getForeignEntity()));
					for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(vo.getForeignEntity()).values()) {
						staticMetaData.addEntityField(vo.getForeignEntity(), voForeignField);
					}
				}
			}
			if(!vo.isFlagRemoved()) {
				staticMetaData.addEntityField(metaVO.getEntity(), vo);
			}
		}
		EntityObjectMetaDbHelper dbHelperSoll = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		DbTable tableSoll = dbHelperSoll.getDbTable(metaVO);

		List<DbStructureChange> lstStructureChanges = null;

		if(voIst.getId() != null) {
			lstStructureChanges = SchemaUtils.modify(tableIst, tableSoll);
		}
		else {
			lstStructureChanges = SchemaUtils.create(tableSoll);
		}


		for(DbStructureChange ds : lstStructureChanges) {
			dataBaseHelper.getDbAccess().execute(ds);
		}

		for(EntityFieldMetaDataTO metaFieldTO : toFields) {
			EntityFieldMetaDataVO metaFieldVO = metaFieldTO.getEntityFieldMeta();
			if(metaFieldVO.getId() == null) {
				metaFieldVO.setId(DalUtils.getNextId());
				metaFieldVO.setEntityId(voIst.getId());
				metaFieldVO.flagNew();
			}
			else {
				if(!metaFieldVO.isFlagRemoved()) {
					metaFieldVO.flagUpdate();
					metaFieldVO.setEntityId(voIst.getId());
				}
			}
			if(metaFieldVO.isFlagRemoved()) {
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(metaFieldVO.getId());
			}
			else {
				DalUtils.updateVersionInformation(metaFieldVO, getCurrentUserName());
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().insertOrUpdate(metaFieldVO);
				createResourceIdForEntityField("T_MD_ENTITY_FIELD", metaFieldTO, 
						IdUtils.unsafeToId(metaFieldTO.getEntityFieldMeta().getId()));
			}
		}
		MetaDataServerProvider.getInstance().revalidate(true);
		return null;
	}


	@Override
    public String getResourceSIdForEntityFieldLabel(Integer iId) {
		return LocaleUtils.getResourceIdForField(ENTITYFIELD_TABLE, iId, LocaleUtils.ENTITYFIELD_LABEL);
	}

	@Override
    public String getResourceSIdForEntityFieldDescription(Integer iId) {
		return LocaleUtils.getResourceIdForField(ENTITYFIELD_TABLE, iId, LocaleUtils.ENTITYFIELD_DESCRIPTION);
	}

	private void createResourceIdForEntity(String table, EntityMetaDataTO mdvo, Integer id) {

		Map<String, String> mp = new HashMap<String, String>();

		mp.put(TranslationVO.labelsEntity[0], LocaleUtils.FIELD_LABEL);
		mp.put(TranslationVO.labelsEntity[1], LocaleUtils.FIELD_MENUPATH);
		mp.put(TranslationVO.labelsEntity[2], LocaleUtils.FIELD_TREEVIEW);
		mp.put(TranslationVO.labelsEntity[3], LocaleUtils.FIELD_TREEVIEWDESCRIPTION);

		for(String key : mp.keySet()) {
			String sResId = null;
			Collection<LocaleInfo> colLocaleInfo = getLocaleFacade().getAllLocales(false);
			for(LocaleInfo li : colLocaleInfo) {
				for(TranslationVO vo : mdvo.getTranslation()) {
					if(vo.getLanguage().equals(li.language)) {
						if(vo.getLabels().get(key) != null && vo.getLabels().get(key).length() > 0) {
							if(sResId == null)
								sResId = getResourceIdFromMetaDataVO(mdvo.getEntityMetaVO(), mp.get(key));
							sResId = getLocaleFacade().setResourceForLocale(sResId, li, vo.getLabels().get(key));
							LocaleUtils.setResourceIdForField(table, id, mp.get(key), sResId);
							break;
						}
					}
				}
			}
		}
		getLocaleFacade().flushInternalCaches();

	}

	private void createResourceIdForEntityField(String table, EntityFieldMetaDataTO mdvo, Integer id) {

		Map<String, String> mp = new HashMap<String, String>();

		mp.put(TranslationVO.labelsField[0], LocaleUtils.FIELD_LABEL);
		mp.put(TranslationVO.labelsField[1], LocaleUtils.FIELD_DESCRIPTION);

		for(String key : mp.keySet()) {
			String sResId = null;
			Collection<LocaleInfo> colLocaleInfo = getLocaleFacade().getAllLocales(false);
			for(LocaleInfo li : colLocaleInfo) {
				if(mdvo.getTranslation() == null)
					continue;
				for(TranslationVO vo : mdvo.getTranslation()) {
					if(vo.getLanguage().equals(li.language)) {
						if(vo.getLabels().get(key) != null && vo.getLabels().get(key).length() > 0) {
							if(sResId == null)
								sResId = getResourceIdFromMetaDataVO(mdvo.getEntityFieldMeta(), mp.get(key));
							sResId = getLocaleFacade().setResourceForLocale(sResId, li, vo.getLabels().get(key));
							LocaleUtils.setResourceIdForField(table, id, mp.get(key), sResId);
							break;
						}
					}
				}
			}
		}
		getLocaleFacade().flushInternalCaches();

	}


	private static String getResourceIdFromMetaDataVO(EntityFieldMetaDataVO metavo, String resFieldName) {
		if (resFieldName.equals(LocaleUtils.FIELD_LABEL)) {
			return metavo.getLocaleResourceIdForLabel();
		} else if (resFieldName.equals(LocaleUtils.FIELD_DESCRIPTION)) {
			return metavo.getLocaleResourceIdForDescription();
		}
		return null;
	}

	private static String getResourceIdFromMetaDataVO(EntityMetaDataVO metavo, String resFieldName) {
		if (resFieldName.equals(LocaleUtils.FIELD_LABEL)) {
			return metavo.getLocaleResourceIdForLabel();
		} else if (resFieldName.equals(LocaleUtils.FIELD_MENUPATH)) {
			return metavo.getLocaleResourceIdForMenuPath();
		} else if (resFieldName.equals(LocaleUtils.FIELD_LABELPLURAL)) {
			return metavo.getLocaleResourceIdForDescription();
		} else if (resFieldName.equals(LocaleUtils.FIELD_TREEVIEW)) {
			return metavo.getLocaleResourceIdForTreeView();
		} else if (resFieldName.equals(LocaleUtils.FIELD_TREEVIEWDESCRIPTION)) {
			return metavo.getLocaleResourceIdForTreeViewDescription();
		}
		return null;
	}

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@Override
    @RolesAllowed("Login")
	public void remove(String sEntityName, MasterDataVO mdvo, boolean bRemoveDependants) throws NuclosBusinessRuleException, CommonPermissionException,
								CommonStaleVersionException, CommonRemoveException, CommonFinderException {

		LocaleFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		MasterDataMetaVO metavo = MasterDataMetaCache.getInstance().getMetaData((String)mdvo.getField("entity"));
		facade.deleteResource(metavo.getResourceSIdForLabel());
		facade.deleteResource(metavo.getResourceSIdForTreeView());
		facade.deleteResource(metavo.getResourceSIdForTreeViewDescription());
		facade.deleteResource(metavo.getResourceSIdForLabelPlural());
		facade.deleteResource(metavo.getResourceSIdForMenuPath());

		for (MasterDataMetaFieldVO metafieldvo : metavo.getFields()) {
			facade.deleteResource(metafieldvo.getResourceSIdForLabel());
			facade.deleteResource(metafieldvo.getResourceSIdForDescription());
		}

		masterDataFacade.remove(sEntityName, mdvo, bRemoveDependants, null);

		MasterDataFacadeHelper.invalidateCaches(sEntityName, mdvo);
	}

	@RolesAllowed("Login")
	@Override
	public boolean hasEntityImportStructure(Long id) throws CommonBusinessException {
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_IMPORT").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("INTID", Integer.class).alias("INTID"));
		columns.add(from.baseColumn("INTID_T_AD_MASTERDATA", Integer.class).alias("INTID_T_AD_MASTERDATA"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("INTID_T_AD_MASTERDATA", Integer.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(id)));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}

	@Override
	@RolesAllowed("Login")
	public boolean hasEntityWorkflow(Long id) throws CommonBusinessException {
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_GENERATION").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("INTID", Integer.class).alias("INTID"));
		columns.add(from.baseColumn("INTID_T_MD_MODULE_TARGET", Integer.class).alias("INTID_T_MD_MODULE_TARGET"));
		columns.add(from.baseColumn("INTID_T_MD_MODULE_SOURCE", Integer.class).alias("INTID_T_MD_MODULE_SOURCE"));
		query.multiselect(columns);

		DbCondition cond1 = dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("INTID_T_MD_MODULE_TARGET", Integer.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(id));
		DbCondition cond2 = dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("INTID_T_MD_MODULE_SOURCE", Integer.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(id));

		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().or(cond1, cond2));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}

	@Override
    public void removeEntity(EntityMetaDataVO voEntity, boolean dropLayout) throws CommonBusinessException {
		final MetaDataProvider mdProvider = MetaDataServerProvider.getInstance();
		if (!voEntity.isVirtual()) {
			if(hasEntityRows(voEntity)) {
				if(voEntity.isStateModel()) {
					GenericObjectFacadeLocal local = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
					for(Integer iId : local.getGenericObjectIds(IdUtils.unsafeToId(voEntity.getId()), new CollectableSearchExpression())) {
						Set<String> setNames = new HashSet<String>();
						try {
							GenericObjectWithDependantsVO vo = local.getWithDependants(iId, setNames, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
							local.remove(vo, true, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}

				}
				else {
					MasterDataFacadeLocal local = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
					for(MasterDataVO vo : local.getMasterData(voEntity.getEntity(), null, true)) {
						try {
							local.remove(voEntity.getEntity(), vo, false, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						catch(NuclosBusinessRuleException e) {
							throw new NuclosFatalException(e);
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}

				}
			}
		}

		final EntityObjectMetaDbHelper helper = new EntityObjectMetaDbHelper(mdProvider);
		final DbTable table = helper.getDbTable(voEntity);

		List<DbStructureChange> lstChanges = SchemaUtils.drop(table);
		for(DbStructureChange db : lstChanges) {
			dataBaseHelper.getDbAccess().execute(db);
		}

		// delete workflow subentity
		CollectableComparison compWorkflowSubEntity1 = SearchConditionUtils.newEOComparison("nuclos_generationSubentity", "entitySource", ComparisonOperator.EQUAL, voEntity.getEntity(), MetaDataServerProvider.getInstance());
		CollectableComparison compWorkflowSubEntity2 = SearchConditionUtils.newEOComparison("nuclos_generationSubentity", "entityTarget", ComparisonOperator.EQUAL, voEntity.getEntity(), MetaDataServerProvider.getInstance());
		CompositeCollectableSearchCondition searchWorkflowSubEntity = SearchConditionUtils.or(compWorkflowSubEntity1, compWorkflowSubEntity2);
		Collection<MasterDataVO> colWorkflowSubEntity = masterDataFacade.getMasterData("nuclos_generationSubentity", searchWorkflowSubEntity, true);
		for(MasterDataVO voWorkflowSubEntity : colWorkflowSubEntity) {
			masterDataFacade.remove("nuclos_generationSubentity", voWorkflowSubEntity, true, null);
		}

		// delete workflow
		CollectableComparison compWorkflow1 = SearchConditionUtils.newEOComparison("nuclos_generation", "sourceModule", ComparisonOperator.EQUAL, voEntity.getEntity(), MetaDataServerProvider.getInstance());
		CollectableComparison compWorkflow2 = SearchConditionUtils.newEOComparison("nuclos_generation", "targetModule", ComparisonOperator.EQUAL, voEntity.getEntity(), MetaDataServerProvider.getInstance());
		CompositeCollectableSearchCondition searchWorkflow = SearchConditionUtils.or(compWorkflow1, compWorkflow2);
		Collection<MasterDataVO> colWorkflow = masterDataFacade.getMasterData("nuclos_generation", searchWorkflow, true);
		for(MasterDataVO voWorkflow : colWorkflow) {
			CollectableComparison compWorkflowRule = SearchConditionUtils.newEOComparison("nuclos_rulegeneration", "generation", ComparisonOperator.EQUAL, voWorkflow.getId(), MetaDataServerProvider.getInstance());
			for(MasterDataVO voWorkflowRule : masterDataFacade.getMasterData("nuclos_rulegeneration", compWorkflowRule, true)) {
				masterDataFacade.remove("nuclos_rulegeneration", voWorkflowRule, true, null);
			}
			masterDataFacade.remove("nuclos_generation", voWorkflow, true, null);
		}

		// delete import structure
		CollectableComparison comp = SearchConditionUtils.newEOComparison("nuclos_import", "entity", ComparisonOperator.EQUAL, voEntity.getEntity(), MetaDataServerProvider.getInstance());
		Collection<MasterDataVO> colImportStructure = masterDataFacade.getMasterData("nuclos_import", comp, true);
		for(MasterDataVO voImportStructure : colImportStructure) {
			masterDataFacade.remove("nuclos_import", voImportStructure, true, null);
		}

		// delete statemodel
		Map<String, Object> mpDelStatemodel = new HashMap<String, Object>();
		mpDelStatemodel.put("INTID_T_MD_MODULE", voEntity.getId());
		DbDeleteStatement delStatemodel = new DbDeleteStatement("T_MD_STATEMODELUSAGE", mpDelStatemodel);
		dataBaseHelper.getDbAccess().execute(delStatemodel);

		// delete userrights
		Map<String, Object> mpDelRoleMasterdata = new HashMap<String, Object>();
		mpDelRoleMasterdata.put("STRMASTERDATA", voEntity.getEntity());
		DbDeleteStatement delMasterdata = new DbDeleteStatement("T_MD_ROLE_MASTERDATA", mpDelRoleMasterdata);
		dataBaseHelper.getDbAccess().execute(delMasterdata);

		Map<String, Object> mpDelRoleModule = new HashMap<String, Object>();
		mpDelRoleModule.put("INTID_T_MD_MODULE", voEntity.getId());
		DbDeleteStatement delModule = new DbDeleteStatement("T_MD_ROLE_MODULE", mpDelRoleModule);
		dataBaseHelper.getDbAccess().execute(delModule);

		// delete entity subnodes (NUCLOSINT-1127)
		final NuclosEntity subnodes = NuclosEntity.ENTITYSUBNODES;
		final EntityMetaDataVO subnodesVO = mdProvider.getEntity(subnodes);
		final String subnodesTable = EntityObjectMetaDbHelper.getTableName(subnodesVO);
		final Map<String, Object> snWhere = new HashMap<String, Object>();
		// delete subnodes from entities which are deleted
		snWhere.put(EntityTreeViewVO.ENTITY_COLUMN, voEntity.getId());
		final DbDeleteStatement snDel1 = new DbDeleteStatement(subnodesTable, snWhere);
		dataBaseHelper.getDbAccess().execute(snDel1);
		// delete subnodes representation of entity embedded in other entities (as subform)
		snWhere.clear();
		snWhere.put(EntityTreeViewVO.SUBFORM_ENTITY_COLUMN, voEntity.getEntity());
		final DbDeleteStatement snDel2 = new DbDeleteStatement(subnodesTable, snWhere);
		dataBaseHelper.getDbAccess().execute(snDel2);

		// delete layouts
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_LAYOUTUSAGE").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("STRENTITY", String.class).alias("STRENTITY"));
		columns.add(from.baseColumn("INTID", Integer.class).alias("INTID"));
		columns.add(from.baseColumn("INTID_T_MD_LAYOUT", Integer.class).alias("INTID_T_MD_LAYOUT"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("STRENTITY", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(voEntity.getEntity())));

		List<Integer> lstDeleteIds = new ArrayList<Integer>();
		List<DbTuple> usages = dataBaseHelper.getDbAccess().executeQuery(query);

		for(DbTuple tuple : usages) {
		   Integer idLayout = tuple.get("INTID_T_MD_LAYOUT", Integer.class);
		   Integer id = tuple.get("INTID", Integer.class);

		   lstDeleteIds.add(idLayout);
		   Map<String, Object> mpDelLayout = new HashMap<String, Object>();
			mpDelLayout.put("INTID", id);
			DbDeleteStatement delLayout = new DbDeleteStatement("T_MD_LAYOUTUSAGE", mpDelLayout);
			dataBaseHelper.getDbAccess().execute(delLayout);
		}

		if(dropLayout) {
			for(Integer idLayout : lstDeleteIds) {
				Map<String, Object> mpDelLayout = new HashMap<String, Object>();
				mpDelLayout.put("INTID", idLayout);
				DbDeleteStatement delLayout = new DbDeleteStatement("T_MD_LAYOUT", mpDelLayout);
				dataBaseHelper.getDbAccess().execute(delLayout);
			}
		}

		// delete fields
		for(EntityFieldMetaDataVO voField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getEntity()).values()) {
			NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(voField.getId());
	    }
		// delete entity
		NucletDalProvider.getInstance().getEntityMetaDataProcessor().delete(voEntity.getId());

		MetaDataServerProvider.getInstance().revalidate(true);
	}

	@Override
    @RolesAllowed("Login")
	public boolean hasEntityRows(EntityMetaDataVO voEntity) {

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from(voEntity.getDbEntity()).alias(SystemFields.BASE_ALIAS);
		query.select(builder.count(t.baseColumn("INTID", Integer.class)));

		return dataBaseHelper.getDbAccess().executeQuerySingleResult(query) > 0L;
	}

	@Override
    public String createOrModifyEntity(EntityMetaDataVO oldMDEntity, EntityMetaDataTO updatedTOEntity,
    		MasterDataVO voEntity, List<EntityFieldMetaDataTO> toFields, boolean blnExecute,
    		String user, String password) throws NuclosBusinessException {
		String resultMessage = null;
		EntityMetaDataVO updatedMDEntity = updatedTOEntity.getEntityMetaVO();

		String sOldPath = "";

		if(updatedMDEntity.getId() != null) {
			sOldPath = MetaDataServerProvider.getInstance().getEntity(updatedMDEntity.getEntity()).getDocumentPath();
			sOldPath = StringUtils.emptyIfNull(sOldPath);
		}

		StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider();
		staticMetaData.addEntity(updatedMDEntity);
		List<EntityFieldMetaDataVO> lstFields = new ArrayList<EntityFieldMetaDataVO>();

		List<EntityFieldMetaDataVO> lstSystemFields = new ArrayList<EntityFieldMetaDataVO>();
		DalUtils.addNucletEOSystemFields(lstSystemFields, updatedMDEntity, true);

		for(EntityFieldMetaDataTO to : toFields) {
			if(!to.getEntityFieldMeta().isFlagRemoved()) {
				lstFields.add(to.getEntityFieldMeta());
				staticMetaData.addEntityField(updatedMDEntity.getEntity(), to.getEntityFieldMeta());
				if(updatedMDEntity.getEntity().equals(to.getEntityFieldMeta().getForeignEntity())) {
					continue;
					// NUCLOSINT-697
				}
				if(to.getEntityFieldMeta().getForeignEntity() != null) {
					staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(to.getEntityFieldMeta().getForeignEntity()));
					for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(to.getEntityFieldMeta().getForeignEntity()).values()) {
						staticMetaData.addEntityField(to.getEntityFieldMeta().getForeignEntity(), voForeignField);
					}
				}
			}
		}
		for(EntityFieldMetaDataVO voSystemField : lstSystemFields) {
			staticMetaData.addEntityField(updatedMDEntity.getEntity(), voSystemField);
			if(voSystemField.getForeignEntity() != null) {
				staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(voSystemField.getForeignEntity()));
				for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(voSystemField.getForeignEntity()).values()) {
					staticMetaData.addEntityField(voSystemField.getForeignEntity(), voForeignField);
				}
			}
		}
		
		EntityObjectMetaDbHelper dbHelperIst = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
		DbTable tableIst = dbHelperIst.getDbTable(updatedMDEntity);

		EntityObjectMetaDbHelper dbHelperSoll = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		DbTable tableSoll = dbHelperSoll.getDbTable(updatedMDEntity);

		List<DbStructureChange> lstStructureChanges = null;

		if(updatedMDEntity.getId() != null) {
			lstStructureChanges = SchemaUtils.modify(tableIst, tableSoll);
		}
		else {
			lstStructureChanges = SchemaUtils.create(tableSoll);
		}

		List<DbStructureChange> lstDbChangesOkay = new ArrayList<DbStructureChange>();
		List<DbStructureChange> lstDbChangesNotOkay = new ArrayList<DbStructureChange>();
		boolean dbchangeOkay = true;
		for(DbStructureChange ds : lstStructureChanges) {
			try {
				dataBaseHelper.getDbAccess().execute(ds);
				lstDbChangesOkay.add(ds);
			}
			catch(DbException e) {
				LOG.info("createOrModifyEntity failed: " + e);
				dbchangeOkay = false;
				lstDbChangesNotOkay.add(ds);
			}
		}

		// Error handling
		if(!dbchangeOkay) {
			final DbAccess dbAccess = dataBaseHelper.getDbAccess();
			if(updatedMDEntity.getId() != null) {
				rollBackDBChanges(updatedTOEntity, toFields);
				final StringBuffer sb = new StringBuffer();
				List<String> lstStrings = Collections.emptyList();
				try {
					final IBatch batch = dbAccess.getBatchFor(lstDbChangesNotOkay.get(0));
					lstStrings = dbAccess.getStatementsForLogging(batch);
				}
				catch (SQLException e) {
					sb.append("Failed on getPreparedSqlFor(" + lstDbChangesNotOkay.get(0) + "): " + e);
				}
				sb.append("Entit\u00e4t " + updatedMDEntity.getEntity() + " konnte nicht ver\u00e4ndert werden.\n");
				sb.append("Grund:\n");
				sb.append(lstStrings.get(0));

				resultMessage = sb.toString();
			}
			else {
				EntityObjectMetaDbHelper helper = new EntityObjectMetaDbHelper(MetaDataServerProvider.getInstance());
				DbTable table = helper.getDbTable(updatedMDEntity);

				List<DbStructureChange> lstChanges = SchemaUtils.drop(table);
				for(DbStructureChange db : lstChanges) {
					try {
						dataBaseHelper.getDbAccess().execute(db);
					}
					catch(DbException e)  {
						// ignore
						LOG.info("createOrModifyEntity: " + e);
					}
				}
				StringBuffer sb = new StringBuffer();
				List<String> lstStrings = Collections.emptyList();
				try {
					final IBatch batch = dbAccess.getBatchFor(lstDbChangesNotOkay.get(0));
					lstStrings = dbAccess.getStatementsForLogging(batch);
				}
				catch (SQLException e) {
					sb.append("Failed on getPreparedSqlFor(" + lstDbChangesNotOkay.get(0) + "): " + e);
				}
				sb.append("Entit\u00e4t " + updatedMDEntity.getEntity() + " konnte nicht angelegt werden.\n");
				sb.append("Grund:\n");
				sb.append(lstStrings.get(0));
				resultMessage = sb.toString();
			}

			return resultMessage;
		}

		try {
			final String user2 = getCurrentUserName();
			DalUtils.updateVersionInformation(updatedMDEntity, user2);

			if(updatedMDEntity.getId() != null) {
				updatedMDEntity.flagUpdate();

				insertOrUpdateEntityMetaData(updatedMDEntity);
				createResourceIdForEntity("T_MD_ENTITY", updatedTOEntity, IdUtils.unsafeToId(updatedMDEntity.getId()));
				setSystemValuesAndParent(lstFields, updatedMDEntity);
				insertOrUpdateEntityFieldMetaData(toFields);
				for(EntityFieldMetaDataTO toField : toFields) {
					if(!toField.getEntityFieldMeta().isFlagRemoved())
						createResourceIdForEntityField("T_MD_ENTITY_FIELD", toField, 
								IdUtils.unsafeToId(toField.getEntityFieldMeta().getId()));
				}
				MetaDataServerProvider.getInstance().revalidate(true);
				updatedMDEntity = MetaDataServerProvider.getInstance().getEntity(updatedMDEntity.getEntity());
			}
			else {
				updatedMDEntity.flagNew();
				updatedMDEntity.setId(DalUtils.getNextId());
				insertOrUpdateEntityMetaData(updatedMDEntity);
				createResourceIdForEntity("T_MD_ENTITY", updatedTOEntity, IdUtils.unsafeToId(updatedMDEntity.getId()));
				setSystemValuesAndParent(lstFields, updatedMDEntity);
				insertOrUpdateEntityFieldMetaData(toFields);
				for(EntityFieldMetaDataTO toField : toFields) {
					createResourceIdForEntityField("T_MD_ENTITY_FIELD", toField, IdUtils.unsafeToId(toField.getEntityFieldMeta().getId()));
				}
				MetaDataServerProvider.getInstance().revalidate(true);
			}

			if (updatedTOEntity.getProcesses() != null) {
				JdbcEntityObjectProcessor processor = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PROCESS);
				for (EntityObjectVO process : updatedTOEntity.getProcesses()) {
					if (process.getId() != null && process.getId() < 0L) {
						process.setId(null);
					}
					if (process.isFlagNew() || process.isFlagUpdated()) {
						if (process.getId() == null || process.isFlagNew()) {
							process.flagNew();
							process.setId(DalUtils.getNextId());
							DalUtils.updateVersionInformation(process, user2);
						}
						process.getFieldIds().put("module", updatedMDEntity.getId());
						processor.insertOrUpdate(process);
					}
					else if (process.getId() != null && process.isFlagRemoved()) {
						processor.delete(process.getId());
					}
				}
			}

			if (updatedTOEntity.getMenus() != null) {
				JdbcEntityObjectProcessor menuProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITYMENU);
				for (EntityObjectVO menu : updatedTOEntity.getMenus()) {
					if (menu.isFlagNew() || menu.isFlagUpdated()) {
						if (menu.getId() == null || menu.isFlagNew()) {
							menu.flagNew();
							menu.setId(DalUtils.getNextId());
							DalUtils.updateVersionInformation(menu, user2);
						}
						menu.getFieldIds().put("entity", updatedMDEntity.getId());
						if (menu.getField("process") != null && menu.getFieldId("process") != null && menu.getFieldId("process") < 0L) {
							String processname = menu.getField("process", String.class);
							for (EntityObjectVO process : updatedTOEntity.getProcesses()) {
								if (processname.equals(process.getField("name"))) {
									menu.getFieldIds().put("process", process.getId());
								}
							}
						}
						String resourceId = menu.getField("menupath", String.class);
						for (LocaleInfo li : getLocaleFacade().getAllLocales(false)) {
							resourceId = getLocaleFacade().setResourceForLocale(resourceId, li, menu.getField("menupath_" + li.getTag(), String.class));
						}
						menu.getFields().put("menupath", resourceId);
						menuProcessor.insertOrUpdate(menu);

					}
					else if (menu.getId() != null && menu.isFlagRemoved()) {
						menuProcessor.delete(menu.getId());
						getLocaleFacade().deleteResource(menu.getField("menupath", String.class));
					}
				}
			}

			for(EntityTreeViewVO voTreeView : updatedTOEntity.getTreeView()) {

				if(voTreeView.getField() == null){
					continue;
				}

				Map<String, Object> conditionMap = new HashMap<String, Object>();
				conditionMap.put(EntityTreeViewVO.SUBFORM_ENTITY_COLUMN, voTreeView.getEntity());
				conditionMap.put(EntityTreeViewVO.ENTITY_COLUMN, voTreeView.getOriginentityid());
				dataBaseHelper.getDbAccess().execute(new DbDeleteStatement(EntityTreeViewVO.SUBNODES_TABLE, conditionMap));

				Map<String, Object> m = new HashMap<String, Object>();
				// EntityTreeViewVO specific fields
				m.put(EntityTreeViewVO.SUBFORM2ENTITY_REF_COLUMN, voTreeView.getField());
				m.put(EntityTreeViewVO.SUBFORM_ENTITY_COLUMN, voTreeView.getEntity());
				m.put(EntityTreeViewVO.ENTITY_COLUMN, voTreeView.getOriginentityid());
				// if(voTreeView.getFoldername() != null)
				m.put(EntityTreeViewVO.FOLDERNAME_COLUMN, voTreeView.getFoldername());
				m.put(EntityTreeViewVO.ACTIVE_COLUMN, voTreeView.isActive());
				m.put(EntityTreeViewVO.SORTORDER_COLUMN, voTreeView.getSortOrder());

				// Standard nuclos fields
				m.put("INTID", DalUtils.getNextId());
				m.put("DATCREATED", new Date(System.currentTimeMillis()));
				m.put("STRCREATED", user2);
				m.put("DATCHANGED", new Date(System.currentTimeMillis()));
				m.put("STRCHANGED", user2);
				m.put("INTVERSION", 1);

				dataBaseHelper.getDbAccess().execute(new DbInsertStatement(EntityTreeViewVO.SUBNODES_TABLE, DbNull.escapeNull(m)));
			}

			changeModuleDirectory(sOldPath, updatedMDEntity.getDocumentPath(), updatedMDEntity);
			
			genericObjectFacade.updateGenericObjectEntries(updatedMDEntity.getEntity());

		}
		catch (CommonFatalException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}

		return resultMessage;
	}

	private void changeModuleDirectory(String sOldPath, String sNewPath, EntityMetaDataVO mdvo) throws CommonPermissionException, CommonFinderException, CommonCreateException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, NuclosBusinessException, IOException {
		if(!mdvo.isStateModel())
			return;

		sOldPath = StringUtils.emptyIfNull(sOldPath);
		sNewPath = StringUtils.emptyIfNull(sNewPath);

		if(org.apache.commons.lang.StringUtils.equals(sOldPath, sNewPath)) {
			return;
		}

		Integer iModuleId = IdUtils.unsafeToId(mdvo.getId());
		CollectableSearchExpression exp = new CollectableSearchExpression();

		for(Integer iId : getGenericObjectFacade().getGenericObjectIds(iModuleId, exp)) {
			GenericObjectWithDependantsVO vo = getGenericObjectFacade().getWithDependants(iId, Collections.singleton("nuclos_generalsearchdocument"), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
			if(vo.getDependants().getAllData().size() == 0)
				continue;
			boolean bModify = false;

			String oldPath = getPath(StringUtils.emptyIfNull(sOldPath), vo);
			String newPath = getPath(StringUtils.emptyIfNull(sNewPath), vo);

			DependantMasterDataMap mp = vo.getDependants();
			for(EntityObjectVO voDocument : mp.getData("nuclos_generalsearchdocument")) {
				voDocument.getFields().put("path", newPath);
				bModify = true;
				String sBaseDir = NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH);

				GenericObjectDocumentFile docFile = (GenericObjectDocumentFile)voDocument.getField("file", GenericObjectDocumentFile.class);
				String sFilename = voDocument.getId() + "." + docFile.getFilename();

				File file = new File(sBaseDir +"/" + oldPath + "/" + sFilename);
				File newDir = new File(sBaseDir +"/" + newPath);
				newDir.mkdirs();
				IOUtils.copyFile(file, new File(sBaseDir +"/" + newPath + "/" + sFilename));
			}
			if(bModify) {
				getGenericObjectFacade().modify(vo, mp, false, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
			}
			for(EntityObjectVO voDocument : mp.getData("nuclos_generalsearchdocument")) {
				voDocument.getFields().put("path", newPath);
				bModify = true;
				String sBaseDir = NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH);
				GenericObjectDocumentFile docFile = (GenericObjectDocumentFile)voDocument.getField("file", GenericObjectDocumentFile.class);
				String sFilename = voDocument.getId() + "." + docFile.getFilename();
				File file = new File(sBaseDir +"/" + oldPath + "/" + sFilename);
				file.delete();
			}

		}

	}

	private String getPath(String path, final GenericObjectWithDependantsVO oParent) {
		final String entity = Modules.getInstance().getEntityNameByModuleId(oParent.getModuleId());
		String rPath = new String(path);
		if (rPath.contains("${")){
			rPath = StringUtils.replaceParameters(rPath, new FormattingTransformer() {
				@Override
				protected Object getValue(String field) {
					return oParent.getAttribute(field, AttributeCache.getInstance()).getValue();
				}

				@Override
				protected String getEntity() {
					return entity;
				}
			});
		}
		return rPath;
	}


	private void rollBackDBChanges(EntityMetaDataTO updatedTOEntity,
		List<EntityFieldMetaDataTO> toFields) {
		EntityMetaDataVO updatedMDEntity = updatedTOEntity.getEntityMetaVO();

		EntityObjectMetaDbHelper dbHelperIst = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
		DbTable tableIst = dbHelperIst.getDbTable(updatedMDEntity);

		StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider();
		staticMetaData.addEntity(updatedMDEntity);
		List<EntityFieldMetaDataVO> lstFields = new ArrayList<EntityFieldMetaDataVO>();

		List<EntityFieldMetaDataVO> lst = new ArrayList<EntityFieldMetaDataVO>();

		for(EntityFieldMetaDataVO field : lst) {
			staticMetaData.addEntityField(updatedMDEntity.getEntity(), field);
			if(field.getForeignEntity() != null) {
				staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(field.getForeignEntity()));
				for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(field.getForeignEntity()).values()) {
					staticMetaData.addEntityField(field.getForeignEntity(), voForeignField);
				}
			}
		}
		List<EntityFieldMetaDataVO> lstSystemFields = new ArrayList<EntityFieldMetaDataVO>();
		DalUtils.addNucletEOSystemFields(lstSystemFields, updatedMDEntity);
		for(EntityFieldMetaDataTO to : toFields) {
			lstFields.add(to.getEntityFieldMeta());
			staticMetaData.addEntityField(updatedMDEntity.getEntity(), to.getEntityFieldMeta());
			if(to.getEntityFieldMeta().getForeignEntity() != null) {
				staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(to.getEntityFieldMeta().getForeignEntity()));
				for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(to.getEntityFieldMeta().getForeignEntity()).values()) {
					staticMetaData.addEntityField(to.getEntityFieldMeta().getForeignEntity(), voForeignField);
				}
			}
		}
		for(EntityFieldMetaDataVO voSystemField : lstSystemFields) {
			staticMetaData.addEntityField(updatedMDEntity.getEntity(), voSystemField);
			if(voSystemField.getForeignEntity() != null) {
				staticMetaData.addEntity(MetaDataServerProvider.getInstance().getEntity(voSystemField.getForeignEntity()));
				for(EntityFieldMetaDataVO voForeignField : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(voSystemField.getForeignEntity()).values()) {
					staticMetaData.addEntityField(voSystemField.getForeignEntity(), voForeignField);
				}
			}
		}
		EntityObjectMetaDbHelper dbHelperSoll = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		DbTable tableSoll = dbHelperSoll.getDbTable(updatedMDEntity);

		List<DbStructureChange> lstStructureChanges = null;

		if(updatedMDEntity.getId() != null) {
			lstStructureChanges = SchemaUtils.modify(tableSoll, tableIst);
		}
		else {
			lstStructureChanges = new ArrayList<DbStructureChange>();
		}


		for(DbStructureChange ds : lstStructureChanges) {
			try {
				dataBaseHelper.getDbAccess().execute(ds);
			}
			catch(DbException e) {
				// ignore
				LOG.info("rollBackDBChanges: " + e);
			}
		}
	}

	private void setSystemValuesAndParent(List<EntityFieldMetaDataVO> lstFields, EntityMetaDataVO voParent) {
		for(EntityFieldMetaDataVO voField : lstFields) {
			voField.setEntityId(voParent.getId());
			if(voField.isFlagNew()) {
				DalUtils.updateVersionInformation(voField,getCurrentUserName());
				voField.setId(new Long(dataBaseHelper.getNextIdAsInteger(SpringDataBaseHelper.DEFAULT_SEQUENCE)));
			}
			else if(voField.isFlagUpdated()){
				DalUtils.updateVersionInformation(voField, getCurrentUserName());
			}
		}
	}


	private void insertOrUpdateEntityMetaData(EntityMetaDataVO vo) {
		NucletDalProvider.getInstance().getEntityMetaDataProcessor().insertOrUpdate(vo);
	}

	private DalCallResult insertOrUpdateEntityFieldMetaData(List<EntityFieldMetaDataTO> lstFields) {

		// first remove fields
		for(EntityFieldMetaDataTO vo : lstFields) {
			EntityFieldMetaDataVO v = vo.getEntityFieldMeta();
			DalUtils.updateVersionInformation(v, getCurrentUserName());
			if(v.isFlagRemoved() && v.getId() != null) {
				// NUCLOSINT-714: remove dependants generation attributes
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_GENERATION_ATTRIBUTE",
					"INTID_T_MD_ATTRIBUTE_SOURCE", v.getId()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_GENERATION_ATTRIBUTE",
					"INTID_T_MD_ATTRIBUTE_TARGET", v.getId()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_IMPORTATTRIBUTE",
					"STRATTRIBUTE", v.getField()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_IMPORTIDENTIFIER",
					"STRATTRIBUTE", v.getField()));
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(v.getId());
			}
			else
				continue;
		}

		for(EntityFieldMetaDataTO vo : lstFields) {
			EntityFieldMetaDataVO v = vo.getEntityFieldMeta();
			DalUtils.updateVersionInformation(v, getCurrentUserName());
			if(v.isFlagRemoved()) {
				continue;
			}
			else {
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().insertOrUpdate(v);
			}
		}

		return null;
	}

	@Override
    public List<String> getDBTables() {
		return new ArrayList<String>(dataBaseHelper.getDbAccess().getTableNames(DbTableType.TABLE));
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public Map<String, MasterDataVO> getColumnsFromTable(String sTable) {
		Map<String, MasterDataVO> mp = new HashMap<String, MasterDataVO>();
		DbTable table = dataBaseHelper.getDbAccess().getTableMetaData(sTable);
		for (DbColumn column : table.getTableColumns()) {
			MasterDataVO vo = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.DATATYP), false);
			DbColumnType type = column.getColumnType();
			String javatyp = "java.lang.String";
			String name = "Text";
			int scale = 0, precision = 0;
			boolean blnAddColumn = true;
			if (type.getGenericType() != null) {
				javatyp = type.getGenericType().getPreferredJavaType().getName();
				switch (type.getGenericType()) {
				case NUMERIC:
					name = "Kommazahl";
					scale = (type.getPrecision() != null ? type.getPrecision() : 0);
					precision = (type.getScale() != null ? type.getScale() : 0);
					// Nuclos maps integer to number(x,0), hence we map these back to Integer
					if (precision == 0) {
						name = "Ganzzahl";
						javatyp = "java.lang.Integer";
					}
					break;
				case BOOLEAN:
					name = "Boolean";
					break;
				case VARCHAR:
					name = "Text";
					scale = (type.getLength() != null ? type.getLength() : 0);
					break;
				case DATE:
				case DATETIME:
					name = "Datum";
					break;
				default:
					// Column type nuclos don't supported
					blnAddColumn = false;
					break;
				}
			}
			if(blnAddColumn) {
				vo.setField("name", name);
				vo.setField("javatyp", javatyp);
				vo.setField("scale", scale);
				vo.setField("precision", precision);

				mp.put(column.getColumnName(), vo);
			}
		}
		return mp;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public List<String> getTablesFromSchema(String url, String user, String password, String schema) {
		List<String> lstTables = new ArrayList<String>();
		Connection connect = null;
		try {
			connect = DriverManager.getConnection(url, user, password);
			DatabaseMetaData dbmeta = connect.getMetaData();
			ResultSet rsTables = dbmeta.getTables(null, schema.toUpperCase(), "%", new String [] {"TABLE"});
			while(rsTables.next()) {
				lstTables.add(rsTables.getString("TABLE_NAME"));
			}
			rsTables.close();
		}
		catch(SQLException e) {
			throw new CommonFatalException(e);
		}
		finally {
			if(connect != null)
				try {
					connect.close();
				}
				catch(SQLException e) {
					// do noting here
					LOG.info("getTablesFromSchema: " + e);
				}
		}

		return lstTables;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public List<MasterDataVO> transformTable(String url, String user, String password, String schema, String table) {

		List<MasterDataVO> lstFields = new ArrayList<MasterDataVO>();

		Connection connect = null;
		try {
			connect = DriverManager.getConnection(url, user, password);
			DatabaseMetaData dbmeta = connect.getMetaData();
			ResultSet rsCols = dbmeta.getColumns(null, schema.toUpperCase(), table, "%");
			while(rsCols.next()) {
				String colName = rsCols.getString("COLUMN_NAME");
				int colsize = rsCols.getInt("COLUMN_SIZE");
				int postsize = rsCols.getInt("DECIMAL_DIGITS");
				int columsType = rsCols.getInt("DATA_TYPE");
				String sJavaType = getBestJavaType(columsType);
				if(postsize > 0)
					sJavaType = "java.lang.Double";

				MasterDataMetaVO metaFieldVO = masterDataFacade.getMetaData(NuclosEntity.ENTITYFIELD.getEntityName());
				MasterDataVO mdFieldVO = new MasterDataVO(metaFieldVO, false);

				mdFieldVO.setField("foreignentityfield", null);
				mdFieldVO.setField("unique", Boolean.FALSE);
				mdFieldVO.setField("logbook", Boolean.FALSE);
				mdFieldVO.setField("entity", NuclosEntity.ENTITYFIELD.getEntityName());
				mdFieldVO.setField("formatinput", null);
				mdFieldVO.setField("entityId", null);
				mdFieldVO.setField("datascale", colsize);
				mdFieldVO.setField("label", org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setField("nullable", Boolean.TRUE);
				mdFieldVO.setField("dataprecision", postsize);
				mdFieldVO.setField("dbfield", colName.toLowerCase());
				mdFieldVO.setField("description", org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setField("name", colName.toLowerCase());
				mdFieldVO.setField("entityfieldDefault", null);
				mdFieldVO.setField("foreignentity", null);
				mdFieldVO.setField("formatoutput", null);
				mdFieldVO.setField("datatype", sJavaType);
				mdFieldVO.setField("searchable", Boolean.FALSE);
				mdFieldVO.setField("foreignentity", null);
				mdFieldVO.setField("foreignentityfield", null);
				lstFields.add(mdFieldVO);
			}
			rsCols.close();
		}
		catch(Exception e) {
			LOG.info("transformTable: " + e, e);
		}
		finally {
			try {
				if (connect != null) {
					connect.close();
				}
			}
			catch(Exception e) {
				LOG.info("transformTable: " + e, e);
			}
		}
		return lstFields;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public MasterDataMetaVO transferTable(String url, String user, String password, String schema, String table, String sEntity) {

		MasterDataMetaVO metaNew = null;

		Connection connect = null;
		try {
			DependantMasterDataMap dependMap = new DependantMasterDataMapImpl();
			List<String> lstFields = new ArrayList<String>();
			connect = DriverManager.getConnection(url, user, password);
			DatabaseMetaData dbmeta = connect.getMetaData();
			ResultSet rsCols = dbmeta.getColumns(null, schema.toUpperCase(), table, "%");
			while(rsCols.next()) {
				String colName = rsCols.getString("COLUMN_NAME");
				int colsize = rsCols.getInt("COLUMN_SIZE");
				int postsize = rsCols.getInt("DECIMAL_DIGITS");
				int columsType = rsCols.getInt("DATA_TYPE");
				String sJavaType = getBestJavaType(columsType);
				if(postsize > 0)
					sJavaType = "java.lang.Double";

				MasterDataMetaVO metaFieldVO = masterDataFacade.getMetaData(NuclosEntity.ENTITYFIELD.getEntityName());
				MasterDataVO mdFieldVO = new MasterDataVO(metaFieldVO, false);

				mdFieldVO.setField("foreignentityfield", null);
				mdFieldVO.setField("unique", Boolean.FALSE);
				mdFieldVO.setField("logbook", Boolean.FALSE);
				mdFieldVO.setField("entity", NuclosEntity.ENTITYFIELD.getEntityName());
				mdFieldVO.setField("formatinput", null);
				mdFieldVO.setField("entityId", null);
				mdFieldVO.setField("datascale", colsize);
				mdFieldVO.setField("label", org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setField("nullable", Boolean.TRUE);
				mdFieldVO.setField("dataprecision", postsize);
				mdFieldVO.setField("dbfield", colName.toLowerCase());
				mdFieldVO.setField("description", org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setField("name", colName.toLowerCase());
				mdFieldVO.setField("entityfieldDefault", null);
				mdFieldVO.setField("foreignentity", null);
				mdFieldVO.setField("formatoutput", null);
				mdFieldVO.setField("datatype", sJavaType);
				mdFieldVO.setField("searchable", Boolean.FALSE);
				mdFieldVO.setField("foreignentity", null);
				mdFieldVO.setField("foreignentityfield", null);

				final String entity = NuclosEntity.ENTITYFIELD.getEntityName();
				dependMap.addData(entity, DalSupportForMD.getEntityObjectVO(entity, mdFieldVO));
				lstFields.add(colName);
			}

			rsCols.close();

			metaNew = masterDataFacade.getMetaData(sEntity);

			String sqlSelect = "select * from " + schema + "." + table;
			Statement stmt = connect.createStatement();
			ResultSet rsSelect =  stmt.executeQuery(sqlSelect);
			while(rsSelect.next()) {
				List<Object> lstValues = new ArrayList<Object>();
				for(String sColname : lstFields) {
					lstValues.add(rsSelect.getObject(sColname));
				}

				StringBuffer sb = new StringBuffer();
				sb.append("insert into " + metaNew.getDBEntity());
				sb.append(" values(?");
				for(int i = 0; i < lstValues.size(); i++) {
					sb.append(",?");
				}
				sb.append(",?,?,?,?,?)");

				int col = 1;
				PreparedStatement pst = dataSource.getConnection().prepareStatement(sb.toString());
				pst.setInt(col++, dataBaseHelper.getNextIdAsInteger(SpringDataBaseHelper.DEFAULT_SEQUENCE));
				for(Object object : lstValues) {
					pst.setObject(col++, object);
				}
				pst.setDate(col++, new java.sql.Date(System.currentTimeMillis()));
				pst.setString(col++, "Wizard");
				pst.setDate(col++, new java.sql.Date(System.currentTimeMillis()));
				pst.setString(col++, "Wizard");
				pst.setInt(col++, 1);

				pst.executeUpdate();
				pst.close();

			}
			rsSelect.close();
			stmt.close();

		}
		catch(SQLException e) {
			LOG.info("transferTable: " + e, e);
		}
		finally {
			if(connect != null)
				try {
					connect.close();
				}
				catch(SQLException e) {
					// do noting here
					LOG.info("transferTable: " + e);
				}
		}
		return metaNew;
	}

	private String getBestJavaType(int colType) {
		String sType = "java.lang.String";
		switch(colType) {
		case Types.VARCHAR:
			return sType;
		case Types.CHAR:
			return sType;
		case Types.NCHAR:
			return sType;
		case Types.NVARCHAR:
			return sType;
		case Types.LONGNVARCHAR:
			return sType;
		case Types.LONGVARCHAR:
			return sType;
		case Types.LONGVARBINARY:
			return sType;
		case Types.NUMERIC:
			return "java.lang.Integer";
		case Types.DECIMAL:
			return "java.lang.Double";
		case Types.BOOLEAN:
			return "java.lang.Integer";
		case Types.DATE:
			return "java.util.Date";
		case Types.TIME:
			return "java.util.Date";
		case Types.TIMESTAMP:
			return "java.util.Date";

		default:

			return sType;
		}
	}

	/**
	 * force to change internal entity name
	 */
	@Override
    @RolesAllowed("Login")
	public void changeEntityName(String newName, Integer id) {
		// @TODO GOREF: Update auf T_AD_MASTERDATA ???
		// TODO_AUTOSYNC: Exception, der Name sollte jetzt nicht mehr aenderbar sein...
		dataBaseHelper.execute(DbStatementUtils.updateValues("T_AD_MASTERDATA",
			"STRENTITY", newName).where("INTID", id));
	}

	@Override
    @RolesAllowed("Login")
	public EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO vo) {
		return MasterDataWrapper.getEntityRelationshipModelVO(vo);
	}

	/**
	 * force to change internal entity name
	 */
	@Override
    @RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToNotNullableAllowed(String sEntity, String field) {
		String sTable = MasterDataMetaCache.getInstance().getMetaData(sEntity).getDBEntity();
		MasterDataMetaFieldVO mdmfVO = MasterDataMetaCache.getInstance().getMetaData(sEntity).getField(field);
		String sColumn = mdmfVO.getDBFieldName();

		try {
			// @TODO GOREF: maybe this should be delegated to the (JDBC)-EntityObjectProcessor ?
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Long> query = builder.createQuery(Long.class);
			DbFrom t = query.from(sTable).alias(SystemFields.BASE_ALIAS);
			DbColumnExpression<?> c = t.baseColumn(sColumn, DalUtils.getDbType(mdmfVO.getJavaClass()));
			query.select(builder.countRows());
			query.where(c.isNull());

			Long count = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			return count == 0L;
		}
		catch(Exception e) {
			LOG.info("isChangeDatabaseColumnToNotNullableAllowed: " + e);
			return false;
		}
	}

	/**
	 * force to change internal entity name
	 */
	@Override
    @RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToUniqueAllowed(String sEntity, String field) {
		final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
		final EntityMetaDataVO mdEntitiy = mdProv.getEntity(sEntity);
		String sTable = mdEntitiy.getDbEntity();
		final EntityFieldMetaDataVO mdField = mdProv.getEntityField(sEntity, field);
		final String sColumn;
		// We don't need a join to the referenced field here, instead we simply need th
		// (raw) field name in the database. (tp)
		if (mdField.getForeignEntity() != null && mdField.getForeignEntityField() != null) {
			sColumn = EntityObjectMetaDbHelper.getDbRefColumn(mdField);
		}
		else {
			sColumn = mdField.getDbColumn();
		}
		try {
			// @TODO GOREF: maybe this should be delegated to the (JDBC)-EntityObjectProcessor ?
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Long> query = builder.createQuery(Long.class);
			DbFrom t = query.from(sTable).alias(SystemFields.BASE_ALIAS);
			DbColumnExpression<?> c = t.baseColumn(sColumn, DalUtils.getDbType(mdField.getDataType()));
			query.select(builder.countRows());
			query.groupBy(c);
			query.having(builder.greaterThan(builder.countRows(), builder.literal(1L)));
			query.maxResults(2);

			List<Long> result = dataBaseHelper.getDbAccess().executeQuery(query);
			return result.isEmpty();
		}
		catch(Exception e) {
			LOG.error("isChangeDatabaseColumnToUniqueAllowed: " + e, e);
			return false;
		}
	}

	@Override
	@RolesAllowed("Login")
	public Collection<MasterDataVO> hasEntityFieldInImportStructure(String sEntity, String sField) {
		CollectableComparison comp = SearchConditionUtils.newEOComparison("nuclos_import", "entity", ComparisonOperator.EQUAL, sEntity, MetaDataServerProvider.getInstance());
		Collection<MasterDataVO> colImportStructure = masterDataFacade.getMasterData("nuclos_import", comp, true);
		for(MasterDataVO voImportStructure : colImportStructure) {
			String sImport = (String)voImportStructure.getField("name");
			CollectableComparison compAttribute1 = SearchConditionUtils.newEOComparison("nuclos_importattribute", "import", ComparisonOperator.EQUAL, sImport, MetaDataServerProvider.getInstance());
			CollectableComparison compAttribute2 = SearchConditionUtils.newEOComparison("nuclos_importattribute", "attribute", ComparisonOperator.EQUAL, sField, MetaDataServerProvider.getInstance());
			CompositeCollectableSearchCondition search = SearchConditionUtils.and(compAttribute1, compAttribute2);
			Collection<MasterDataVO> colImportAttribute = masterDataFacade.getMasterData("nuclos_importattribute", search, true);
			if(colImportAttribute.size() > 0)
				return colImportStructure;
		}

		return new ArrayList<MasterDataVO>();
	}

	@Override
	@RolesAllowed("Login")
	public boolean hasEntityLayout(Long id) {
		final String sEntity = MetaDataServerProvider.getInstance().getEntity(id).getEntity();
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_LAYOUTUSAGE").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("INTID", Integer.class).alias("INTID"));
		columns.add(from.baseColumn("STRENTITY", String.class).alias("STRENTITY"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("STRENTITY", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(sEntity)));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}

	@Override
    @RolesAllowed("Login")
	public Long getEntityIdByName(String sEntity) {
		return MetaDataServerProvider.getInstance().getEntity(sEntity).getId();
	}

	@Override
    @RolesAllowed("Login")
	public EntityMetaDataVO getEntityByName(String sEntity) {
		return MetaDataServerProvider.getInstance().getEntity(sEntity);
	}

	@Override
    @RolesAllowed("Login")
	public EntityMetaDataVO getEntityById(Long id) {
		return MetaDataServerProvider.getInstance().getEntity(id);
	}

	@Override
	@RolesAllowed("Login")
    public void invalidateServerMetadata() {
	    MetaDataServerProvider.getInstance().revalidate(true);
    }

	@Override
	@RolesAllowed("Login")
	public List<String> getVirtualEntities() {
		List<String> result = new ArrayList<String>();
		result.addAll(dataBaseHelper.getDbAccess().getTableNames(DbTableType.TABLE));
		result.addAll(dataBaseHelper.getDbAccess().getTableNames(DbTableType.VIEW));
		for (EntityMetaDataVO meta : getAllEntities()) {
			result.remove(meta.getDbEntity());
			result.remove("T_" + meta.getDbEntity().substring(2));
		}
		return result;
	}

	@Override
	@RolesAllowed("Login")
	public List<String> getPossibleIdFactories() {
		return MetaDataServerProvider.getInstance().getPossibleIdFactories();
	}

	@Override
	@RolesAllowed("Login")
	public List<EntityFieldMetaDataVO> getVirtualEntityFields(String virtualentity) {
		List<EntityFieldMetaDataVO> result = new ArrayList<EntityFieldMetaDataVO>();
		DbTable tableMetaData = dataBaseHelper.getDbAccess().getTableMetaData(virtualentity);

		for (DbColumn column : tableMetaData.getTableColumns()) {
			EntityFieldMetaDataVO field = DalUtils.getFieldMeta(column);
			field.setDbColumn(column.getColumnName().toUpperCase());
			field.setField(field.getField().toLowerCase());
			field.setFallbacklabel(field.getField());
			result.add(field);
		}
		return result;
	}

	@Override
	public void tryVirtualEntitySelect(EntityMetaDataVO virtualentity) throws NuclosBusinessException {
		JdbcEntityObjectProcessor processor = getProcessorFactory().newEntityObjectProcessor(virtualentity, new ArrayList<EntityFieldMetaDataVO>(), true);
		try {
			processor.getBySearchExpression(new CollectableSearchExpression(new CollectableIdCondition(new Integer(0))));
		}
		catch (Exception ex) {
			error(ex);
			throw new NuclosBusinessException(StringUtils.getParameterizedExceptionMessage("MetaDataFacade.tryVirtualEntitySelect.error", ex.getMessage()));
		}
	}

	@Override
	public void tryRemoveProcess(EntityObjectVO process) throws NuclosBusinessException {
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PROCESS).delete(process.getId());
		}
		catch (DbException e) {
			throw new NuclosBusinessException("tryRemoveProcess failed", e);
		}
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}

	@Override
	public List<EntityObjectVO> getEntityMenus() {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITYMENU).getAll();
	}

	@Override
	public Collection<SystemEntityMetaDataVO> getSystemMetaData() {
		return SystemMetaDataProvider.getInstance().getAllEntities();
	}

	@Override
	public Map<Long, LafParameterMap> getLafParameters() {
		return MetaDataServerProvider.getInstance().getAllLafParameters();
	}
}

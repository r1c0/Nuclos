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
package org.nuclos.client.ui.collect;

import java.util.Collection;

import org.nuclos.client.customcode.CodeCollectController;
import org.nuclos.client.datasource.admin.CollectableDataSource;
import org.nuclos.client.datasource.admin.DatasourceCollectController;
import org.nuclos.client.datasource.admin.DynamicEntityCollectController;
import org.nuclos.client.datasource.admin.DynamicTasklistCollectController;
import org.nuclos.client.datasource.admin.RecordGrantCollectController;
import org.nuclos.client.datasource.admin.ValuelistProviderCollectController;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectImportCollectController;
import org.nuclos.client.job.JobControlCollectController;
import org.nuclos.client.layout.admin.GenericObjectLayoutCollectController;
import org.nuclos.client.ldap.LdapServerCollectController;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.DbObjectCollectController;
import org.nuclos.client.masterdata.DbSourceCollectController;
import org.nuclos.client.masterdata.GenerationCollectController;
import org.nuclos.client.masterdata.GenericObjectImportStructureCollectController;
import org.nuclos.client.masterdata.GroupCollectController;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.masterdata.NucletCollectController;
import org.nuclos.client.masterdata.RelationTypeCollectController;
import org.nuclos.client.masterdata.SearchFilterCollectController;
import org.nuclos.client.masterdata.locale.LocaleCollectController;
import org.nuclos.client.masterdata.user.UserCollectController;
import org.nuclos.client.masterdata.wiki.WikiCollectController;
import org.nuclos.client.processmonitor.CollectableInstanceModel;
import org.nuclos.client.processmonitor.CollectableProcessMonitorModel;
import org.nuclos.client.processmonitor.InstanceCollectController;
import org.nuclos.client.processmonitor.ProcessMonitorCollectController;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.relation.EntityRelationshipModel;
import org.nuclos.client.report.admin.ReportCollectController;
import org.nuclos.client.report.admin.ReportExecutionCollectController;
import org.nuclos.client.resource.admin.ResourceCollectController;
import org.nuclos.client.rule.admin.CollectableRule;
import org.nuclos.client.rule.admin.RuleCollectController;
import org.nuclos.client.rule.admin.TimelimitRuleCollectController;
import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.statemodel.admin.StateModelCollectController;
import org.nuclos.client.task.PersonalTaskCollectController;
import org.nuclos.client.transfer.ExportImportCollectController;
import org.nuclos.client.ui.collect.search.DatasourceSearchStrategy;
import org.nuclos.client.ui.collect.search.DynamicEntitySearchStrategy;
import org.nuclos.client.ui.collect.search.DynamicTasklistSearchStrategy;
import org.nuclos.client.ui.collect.search.EntityRelationShipSearchStrategy;
import org.nuclos.client.ui.collect.search.GenericObjectViaEntityObjectSearchStrategy;
import org.nuclos.client.ui.collect.search.ISearchStrategy;
import org.nuclos.client.ui.collect.search.InstanceSearchStrategy;
import org.nuclos.client.ui.collect.search.LayoutSearchStrategy;
import org.nuclos.client.ui.collect.search.MasterDataSearchStrategy;
import org.nuclos.client.ui.collect.search.PersonalTaskSearchStrategy;
import org.nuclos.client.ui.collect.search.ProcessMonitorSearchStrategy;
import org.nuclos.client.ui.collect.search.RecordGrantSearchStrategy;
import org.nuclos.client.ui.collect.search.ReportExecutionSearchStrategy;
import org.nuclos.client.ui.collect.search.ReportSearchStrategy;
import org.nuclos.client.ui.collect.search.RuleSearchStrategy;
import org.nuclos.client.ui.collect.search.StateModelSearchStrategy;
import org.nuclos.client.ui.collect.search.ValueListProviderSearchStrategy;
import org.nuclos.client.ui.collect.strategy.CompleteCollectableMasterDataStrategy;
import org.nuclos.client.ui.collect.strategy.CompleteCollectableStateModelsStrategy;
import org.nuclos.client.ui.collect.strategy.CompleteGenericObjectsStrategy;
import org.nuclos.client.wizard.DataTypeCollectController;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.IdUtils;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

public class CollectControllerFactorySingleton {
	
	private static final CollectControllerFactorySingleton INSTANCE = new CollectControllerFactorySingleton();
	
	//
	
	private CollectControllerFactorySingleton() {
	}
	
	public static CollectControllerFactorySingleton getInstance() {
		return INSTANCE;
	}
	
	// MasterDataCollectController
	
	private void initMasterData(MasterDataCollectController mdcc) {
		mdcc.getSearchStrategy().setCompleteCollectablesStrategy(new CompleteCollectableMasterDataStrategy(mdcc));
		mdcc.init();
	}
	
	/**
	 * @deprecated
	 */
	public DataTypeCollectController newDataTypeCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DataTypeCollectController result = new DataTypeCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public WikiCollectController newWikiCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final WikiCollectController result = new WikiCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public SearchFilterCollectController newSearchFilterCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final SearchFilterCollectController result = new SearchFilterCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ExportImportCollectController newExportImportCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final ExportImportCollectController result = new ExportImportCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ResourceCollectController newResourceCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final ResourceCollectController result = new ResourceCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public UserCollectController newUserCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final UserCollectController result = new UserCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public RelationTypeCollectController newRelationTypeCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final RelationTypeCollectController result = new RelationTypeCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GroupCollectController newGroupCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GroupCollectController result = new GroupCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenerationCollectController newGenerationCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenerationCollectController result = new GenerationCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LdapServerCollectController newLdapServerCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LdapServerCollectController result = new LdapServerCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public DbSourceCollectController newDbSourceCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DbSourceCollectController result = new DbSourceCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public DbObjectCollectController newDbObjectCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DbObjectCollectController result = new DbObjectCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public NucletCollectController newNucletCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final NucletCollectController result = new NucletCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LocaleCollectController newLocaleCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LocaleCollectController result = new LocaleCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LocaleCollectController newLocaleCollectController(Collection<String> collresids, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LocaleCollectController result = new LocaleCollectController(collresids, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public JobControlCollectController newJobControlCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final JobControlCollectController result = new JobControlCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public TimelimitRuleCollectController newTimelimitRuleCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final TimelimitRuleCollectController result = new TimelimitRuleCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenericObjectImportStructureCollectController newGenericObjectImportStructureCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenericObjectImportStructureCollectController result = new GenericObjectImportStructureCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenericObjectImportCollectController newGenericObjectImportCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenericObjectImportCollectController result = new GenericObjectImportCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public CodeCollectController newCodeCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final CodeCollectController result = new CodeCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	//
	
	/**
	 * @deprecated
	 */
	public ReportExecutionCollectController newReportExecutionCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new ReportExecutionSearchStrategy();
		final ReportExecutionCollectController result = new ReportExecutionCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ReportCollectController newReportCollectController(NuclosEntity entity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new ReportSearchStrategy(entity);
		final ReportCollectController result = new ReportCollectController(entity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public PersonalTaskCollectController newPersonalTaskCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new PersonalTaskSearchStrategy();
		final PersonalTaskCollectController result = new PersonalTaskCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public MasterDataCollectController newMasterDataCollectController(NuclosEntity systemEntity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final MasterDataCollectController result = new MasterDataCollectController(systemEntity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public MasterDataCollectController newMasterDataCollectController(String entity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final MasterDataCollectController result = new MasterDataCollectController(entity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}
	
	// End of MasterDataCollectController
	
	// GenericObjectCollectController
	
	/**
	 * @deprecated
	 */
	public GenericObjectCollectController newGenericObjectCollectController(Integer iModuleId, boolean bAutoInit, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableGenericObjectWithDependants> ss;
		final MetaDataDelegate md = MetaDataDelegate.getInstance();
		final EntityMetaDataVO mdvo = md.getEntityById(IdUtils.toLongId(iModuleId));
		final CollectableEntityProvider cep = CollectableEOEntityClientProvider.getInstance();
		ss = new GenericObjectViaEntityObjectSearchStrategy((CollectableEOEntity) cep.getCollectableEntity(mdvo.getEntity()));
		// Old (pre-pivot) search strategy
		// ss = new GenericObjectSearchStrategy();
		final GenericObjectCollectController result = new GenericObjectCollectController(iModuleId, false, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		ss.setCompleteCollectablesStrategy(new CompleteGenericObjectsStrategy());
		if (bAutoInit) {
			result.init();
		}
		return result;
	}
	
	// LayoutCollectController
	
	public GenericObjectLayoutCollectController newGenericObjectLayoutCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new LayoutSearchStrategy(); 
		final GenericObjectLayoutCollectController result = new GenericObjectLayoutCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	// AbstractDatasourceCollectController
	
	public ValuelistProviderCollectController newValuelistProviderCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource<ValuelistProviderVO>> ss = new ValueListProviderSearchStrategy(); 
		final ValuelistProviderCollectController result = new ValuelistProviderCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	public DatasourceCollectController newDatasourceCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource<DatasourceVO>> ss = new DatasourceSearchStrategy(); 
		final DatasourceCollectController result = new DatasourceCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	public DynamicEntityCollectController newDynamicEntityCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource<DynamicEntityVO>> ss = new DynamicEntitySearchStrategy(); 
		final DynamicEntityCollectController result = new DynamicEntityCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	public RecordGrantCollectController newRecordGrantCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource<RecordGrantVO>> ss = new RecordGrantSearchStrategy(); 
		final RecordGrantCollectController result = new RecordGrantCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	public DynamicTasklistCollectController newDynamicTasklistCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource<DynamicTasklistVO>> ss = new DynamicTasklistSearchStrategy();
		final DynamicTasklistCollectController result = new DynamicTasklistCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	// EntityRelationShipCollectController
	
	/**
	 * @deprecated
	 */
	public EntityRelationShipCollectController newEntityRelationShipCollectController(MainFrame mf, MainFrameTab tabIfAny) {
		final ISearchStrategy<EntityRelationshipModel> ss = new EntityRelationShipSearchStrategy();
		final EntityRelationShipCollectController result = new EntityRelationShipCollectController(mf, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public InstanceCollectController newInstanceCollectController(String entity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableInstanceModel> ss = new InstanceSearchStrategy();
		final InstanceCollectController result = new InstanceCollectController(entity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public ProcessMonitorCollectController newProcessMonitorCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableProcessMonitorModel> ss = new ProcessMonitorSearchStrategy();
		final ProcessMonitorCollectController result = new ProcessMonitorCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public RuleCollectController newRuleCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableRule> ss = new RuleSearchStrategy();
		final RuleCollectController result = new RuleCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		result.init();
		return result;
	}
	
	// End of EntityRelationShipCollectController
	
	/**
	 * @deprecated
	 */
	public StateModelCollectController newStateModelCollectController(MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableStateModel> ss = new StateModelSearchStrategy();
		final StateModelCollectController result = new StateModelCollectController(tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		ss.setCompleteCollectablesStrategy(new CompleteCollectableStateModelsStrategy(result));
		result.init();
		return result;
	}

}

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

import javax.swing.JComponent;

import org.nuclos.client.customcode.CodeCollectController;
import org.nuclos.client.datasource.admin.CollectableDataSource;
import org.nuclos.client.datasource.admin.DatasourceCollectController;
import org.nuclos.client.datasource.admin.DynamicEntityCollectController;
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
import org.nuclos.client.ui.collect.search.EntityRelationShipSearchStrategy;
import org.nuclos.client.ui.collect.search.GenericObjectSearchStrategy;
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
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.IdUtils;

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
	public DataTypeCollectController newDataTypeCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DataTypeCollectController result = new DataTypeCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public WikiCollectController newWikiCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final WikiCollectController result = new WikiCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public SearchFilterCollectController newSearchFilterCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final SearchFilterCollectController result = new SearchFilterCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ExportImportCollectController newExportImportCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final ExportImportCollectController result = new ExportImportCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ResourceCollectController newResourceCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final ResourceCollectController result = new ResourceCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public UserCollectController newUserCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final UserCollectController result = new UserCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public RelationTypeCollectController newRelationTypeCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final RelationTypeCollectController result = new RelationTypeCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GroupCollectController newGroupCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GroupCollectController result = new GroupCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenerationCollectController newGenerationCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenerationCollectController result = new GenerationCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LdapServerCollectController newLdapServerCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LdapServerCollectController result = new LdapServerCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public DbSourceCollectController newDbSourceCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DbSourceCollectController result = new DbSourceCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public DbObjectCollectController newDbObjectCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final DbObjectCollectController result = new DbObjectCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public NucletCollectController newNucletCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final NucletCollectController result = new NucletCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LocaleCollectController newLocaleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LocaleCollectController result = new LocaleCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public LocaleCollectController newLocaleCollectController(JComponent parent, Collection<String> collresids, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final LocaleCollectController result = new LocaleCollectController(parent, collresids, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public JobControlCollectController newJobControlCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final JobControlCollectController result = new JobControlCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public TimelimitRuleCollectController newTimelimitRuleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final TimelimitRuleCollectController result = new TimelimitRuleCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenericObjectImportStructureCollectController newGenericObjectImportStructureCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenericObjectImportStructureCollectController result = new GenericObjectImportStructureCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public GenericObjectImportCollectController newGenericObjectImportCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final GenericObjectImportCollectController result = new GenericObjectImportCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public CodeCollectController newCodeCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final CodeCollectController result = new CodeCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	//
	
	/**
	 * @deprecated
	 */
	public ReportExecutionCollectController newReportExecutionCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new ReportExecutionSearchStrategy();
		final ReportExecutionCollectController result = new ReportExecutionCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public ReportCollectController newReportCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new ReportSearchStrategy();
		final ReportCollectController result = new ReportCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}

	/**
	 * @deprecated
	 */
	public PersonalTaskCollectController newPersonalTaskCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new PersonalTaskSearchStrategy();
		final PersonalTaskCollectController result = new PersonalTaskCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public MasterDataCollectController newMasterDataCollectController(JComponent parent, NuclosEntity systemEntity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final MasterDataCollectController result = new MasterDataCollectController(parent, systemEntity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		initMasterData(result);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public MasterDataCollectController newMasterDataCollectController(JComponent parent, String entity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new MasterDataSearchStrategy();
		final MasterDataCollectController result = new MasterDataCollectController(parent, entity, tabIfAny);
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
	public GenericObjectCollectController newGenericObjectCollectController(JComponent parent, Integer iModuleId, boolean bAutoInit, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableGenericObjectWithDependants> ss;
		final MetaDataDelegate md = MetaDataDelegate.getInstance();
		final EntityMetaDataVO mdvo = md.getEntityById(IdUtils.toLongId(iModuleId));
		final CollectableEntityProvider cep = CollectableEOEntityClientProvider.getInstance();
		ss = new GenericObjectViaEntityObjectSearchStrategy((CollectableEOEntity) cep.getCollectableEntity(mdvo.getEntity()));
		// Old (pre-pivot) search strategy
		// ss = new GenericObjectSearchStrategy();
		final GenericObjectCollectController result = new GenericObjectCollectController(parent, iModuleId, false, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		ss.setCompleteCollectablesStrategy(new CompleteGenericObjectsStrategy());
		if (bAutoInit) {
			result.init();
		}
		return result;
	}
	
	// LayoutCollectController
	
	public GenericObjectLayoutCollectController newGenericObjectLayoutCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableMasterDataWithDependants> ss = new LayoutSearchStrategy(); 
		final GenericObjectLayoutCollectController result = new GenericObjectLayoutCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	// AbstractDatasourceCollectController
	
	public ValuelistProviderCollectController newValuelistProviderCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource> ss = new ValueListProviderSearchStrategy(); 
		final ValuelistProviderCollectController result = new ValuelistProviderCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	public DatasourceCollectController newDatasourceCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource> ss = new DatasourceSearchStrategy(); 
		final DatasourceCollectController result = new DatasourceCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	public DynamicEntityCollectController newDynamicEntityCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource> ss = new DynamicEntitySearchStrategy(); 
		final DynamicEntityCollectController result = new DynamicEntityCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	public RecordGrantCollectController newRecordGrantCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableDataSource> ss = new RecordGrantSearchStrategy(); 
		final RecordGrantCollectController result = new RecordGrantCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	// EntityRelationShipCollectController
	
	/**
	 * @deprecated
	 */
	public EntityRelationShipCollectController newEntityRelationShipCollectController(JComponent parent, MainFrame mf, MainFrameTab tabIfAny) {
		final ISearchStrategy<EntityRelationshipModel> ss = new EntityRelationShipSearchStrategy();
		final EntityRelationShipCollectController result = new EntityRelationShipCollectController(parent, mf, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public InstanceCollectController newInstanceCollectController(JComponent parent, String entity, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableInstanceModel> ss = new InstanceSearchStrategy();
		final InstanceCollectController result = new InstanceCollectController(parent, entity, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public ProcessMonitorCollectController newProcessMonitorCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableProcessMonitorModel> ss = new ProcessMonitorSearchStrategy();
		final ProcessMonitorCollectController result = new ProcessMonitorCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	/**
	 * @deprecated
	 */
	public RuleCollectController newRuleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableRule> ss = new RuleSearchStrategy();
		final RuleCollectController result = new RuleCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		return result;
	}
	
	// End of EntityRelationShipCollectController
	
	/**
	 * @deprecated
	 */
	public StateModelCollectController newStateModelCollectController(JComponent parent, MainFrameTab tabIfAny) {
		final ISearchStrategy<CollectableStateModel> ss = new StateModelSearchStrategy();
		final StateModelCollectController result = new StateModelCollectController(parent, tabIfAny);
		ss.setCollectController(result);
		result.setSearchStrategy(ss);
		ss.setCompleteCollectablesStrategy(new CompleteCollectableStateModelsStrategy(result));
		return result;
	}

}

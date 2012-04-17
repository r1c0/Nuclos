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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToEntityTreeViewVO;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.TruncatableCollectionDecorator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.customcode.codegenerator.WsdlCodeGenerator;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dal.provider.SystemEntityFieldMetaDataVO;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.MasterDataProxyList;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all master data management functions. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class MasterDataFacadeBean extends NuclosFacadeBean implements MasterDataFacadeRemote {

	private static final Logger LOG = Logger.getLogger(MasterDataFacadeBean.class);
	
	//

	private MasterDataFacadeHelper helper;

	//warum auskommentiert?
	//final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_METADATACACHE);

	private boolean bServerValidatesMasterDataValues;
	
	private ServerParameterProvider serverParameterProvider;
	
	public MasterDataFacadeBean() {
	}
	
	@Autowired
	final void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.helper = masterDataFacadeHelper;
	}
	
	@Autowired
	final void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	protected final MasterDataFacadeHelper getMasterDataFacadeHelper() {
		return helper;
	}

	@PostConstruct
	@RolesAllowed("Login")
	public void postConstruct() {
		this.bServerValidatesMasterDataValues = "1".equals(serverParameterProvider.getValue(
			ParameterProvider.KEY_SERVER_VALIDATES_MASTERDATAVALUES));
	}

	/**
	 * @return Is the server supposed to validate master data values before
	 *         storing them?
	 */
	private boolean getServerValidatesMasterDataValues() {
		return this.bServerValidatesMasterDataValues;
	}

	/**
	 * @return the masterdata meta information for the all entities.
	 */
    @RolesAllowed("Login")
	public Collection<MasterDataMetaVO> getAllMetaData() {
		return MasterDataMetaCache.getInstance().getAllMetaData();
	}

	/**
	 * method to get meta information for a master data entity
	 *
	 * @param sEntityName name of entity to get meta data for
	 * @return master data meta value object
	 * @postcondition result != null
	 * @throws NuclosFatalException if there is not metadata for the given
	 *            entity.
	 */
    public MasterDataMetaVO getMetaData(String sEntityName) {
		final MasterDataMetaVO result = MasterDataMetaCache.getInstance().getMetaData(
			sEntityName);
		assert result != null;
		return result;
	}

	protected MasterDataMetaVO getMetaData(NuclosEntity entity) {
		return getMetaData(entity.getEntityName());
	}

	/**
	 * method to get meta information for a master data entity
	 *
	 * @param iEntityId id of entity to get meta data for
	 * @return master data meta value object
	 * @postcondition result != null
	 * @throws ElisaFatalException if there is not metadata for the given entity.
	 */
    public MasterDataMetaVO getMetaData(Integer iEntityId) {
		final MasterDataMetaVO result = MasterDataMetaCache.getInstance().getMasterDataMetaById(
			iEntityId);
		if(result == null) {
			throw new NuclosFatalException(
				"Masterdata meta information for entity " + iEntityId
					+ " is not available.");
		}
		assert result != null;
		return result;
	}

	/**
	 * @param iModuleId the id of the module whose subentities we are looking for
	 * @return Collection<MasterdataMetaCVO> the masterdata meta information for
	 *         all entities having foreign keys to the given module.
	 */
    public Collection<MasterDataMetaVO> getMetaDataByModuleId(Integer iModuleId) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom m = query.from("T_AD_MASTERDATA").alias("m");
		DbFrom mf = m.join("T_AD_MASTERDATA_FIELD", JoinType.INNER).alias("mf").on("INTID", "INTID_T_AD_MASTERDATA", Integer.class);
		DbFrom p = mf.join("T_AD_MASTERDATA", JoinType.INNER).alias("p").on("STRFOREIGNENTITY", "STRENTITY", String.class);
		// @TODO GOREF
		DbFrom go = p.join("T_MD_MODULE", JoinType.INNER).alias("go").on("STRENTITY", "STRENTITY", String.class);
		query.select(m.baseColumn("STRENTITY", String.class));
		query.where(builder.equal(go.baseColumn("INTID", Integer.class), iModuleId));
		query.orderBy(builder.asc(m.baseColumn("STRENTITY", String.class)));

		final MasterDataMetaCache mdmetacache = MasterDataMetaCache.getInstance();
		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<String, MasterDataMetaVO>() {
			@Override
			public MasterDataMetaVO transform(String ent) {
				return mdmetacache.getMetaData(ent);
			}
		});
	}

	/**
	 * @param sEntityName
	 * @param clctexpr
	 * @return a proxy list containing the search result for the given search
	 *         expression.
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
	public ProxyList<MasterDataWithDependantsVO> getMasterDataProxyList(
		String sEntityName, CollectableSearchExpression clctexpr) {
		List<EntityAndFieldName> lstEafn = Collections.emptyList();
		return new MasterDataProxyList(sEntityName, clctexpr, lstEafn);
	}

	/**
	 * method to get master data records for a given entity and search condition
	 *
	 * @param sEntityName name of the entity to get master data records for
	 * @param cond search condition
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @postcondition result != null
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
	public TruncatableCollection<MasterDataVO> getMasterData(String sEntityName,
		CollectableSearchCondition cond, boolean bAll) {
		NuclosEntity nuclosEntity = NuclosEntity.getByName(sEntityName);
		if (nuclosEntity == NuclosEntity.REPORT || nuclosEntity == NuclosEntity.REPORTEXECUTION) {
			bAll = true;
		}

		final TruncatableCollection<MasterDataVO> result;

		if (NuclosEntity.MODULE.getEntityName().equals(sEntityName)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + sEntityName + " are not supported.");
			}
			Collection<MasterDataVO> colResult = new ArrayList<MasterDataVO>();
			for (EntityMetaDataVO eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
				if (eMeta.isStateModel()) {
					colResult.add(DalSupportForMD.wrapEntityMetaDataVOInModule(eMeta));
				}
			}
			result = new TruncatableCollectionDecorator<MasterDataVO>(colResult, false, colResult.size());
		} else if (NuclosEntity.MASTERDATA.getEntityName().equals(sEntityName)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + sEntityName + " are not supported.");
			}
			Collection<MasterDataVO> colResult = new ArrayList<MasterDataVO>();
			for (EntityMetaDataVO eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
				if (!eMeta.isStateModel()) {
					colResult.add(MasterDataWrapper.wrapMasterDataMetaVO(DalSupportForMD.wrapEntityMetaDataVOInMasterData(eMeta, NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity()))));
				}
			}
			result = new TruncatableCollectionDecorator<MasterDataVO>(colResult, false, colResult.size());
		} else if (NuclosEntity.ENTITYFIELDGROUP.getEntityName().equals(sEntityName)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + sEntityName + " are not supported.");
			}
			Collection<MasterDataVO> colResult = new ArrayList<MasterDataVO>();
			for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITYFIELDGROUP.getEntityName()).getBySearchExpression(appendRecordGrants(new CollectableSearchExpression(cond), sEntityName))) {
				colResult.add(DalSupportForMD.wrapEntityObjectVO(eo));
			}
			result = new TruncatableCollectionDecorator<MasterDataVO>(colResult, false, colResult.size());
		} else {
			TruncatableCollection<MasterDataVO> truncoll = helper.getGenericMasterData(sEntityName, cond, bAll);
			// permissions on reports and forms are given explicitly on a record per
			// record basis
			if (nuclosEntity == NuclosEntity.REPORT || nuclosEntity == NuclosEntity.REPORTEXECUTION) {
				result = filterReports(truncoll,
					SecurityCache.getInstance().getReadableReports(
						this.getCurrentUserName()));
			} else {
				result = truncoll;
			}
		}

		assert result != null;
		return result;
	}

	/**
	 * gets the ids of all masterdata objects that match a given search
	 * expression (ordered, when necessary)
	 *
	 * @param cond condition that the masterdata objects to be found must satisfy
	 * @return List<Integer> list of masterdata ids
	 */
    @RolesAllowed("Login")
	public List<Object> getMasterDataIds(String sEntityName, CollectableSearchExpression cse) {
		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);

		List<Long> eoIds = eoProcessor.getIdsBySearchExpression(appendRecordGrants(cse, sEntityName));
		List<Object> masterDataIds = CollectionUtils.transform(eoIds, new Transformer<Long, Object>() {
			@Override public Object transform(Long l) { return l.intValue(); }
		});

		boolean bAdditionalSorting = false;
		if (cse != null && cse.isIncludingSystemData()) {
			Collection<Object> systemObjects = XMLEntities.getSystemObjectIds(sEntityName, cse.getSearchCondition());
			masterDataIds.addAll(systemObjects);
			bAdditionalSorting = !systemObjects.isEmpty();
		}

		if (cse.getSortingOrder() != null && !cse.getSortingOrder().isEmpty() && bAdditionalSorting) {
			final String fieldForSorting = cse.getSortingOrder().get(0).getFieldName();
			this.sortIdList(masterDataIds, sEntityName, fieldForSorting, cse.getSortingOrder().get(0).isAscending());
		}

		return masterDataIds;
	}

	/**
	 * WORKAROUND for XML entities and sorting of mixed lists with DB records
	 * Better: Send fields for sorting to DA-Layer. He has already read the DB records!
	 * @param list
	 * @param sEntityName
	 * @param sEntityFieldForSorting
	 * @param bAsc
	 */
	private void sortIdList(List<Object> list, final String sEntityName, final String sEntityFieldForSorting, final boolean bAsc) {
		final JdbcEntityObjectProcessor proc = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);
		final Collection<MasterDataVO> systemObjects = XMLEntities.getSystemObjects(sEntityName, null);
		final Collection<Object> systemObjectIds = this.getIds(systemObjects);
		Collections.sort(list, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				final boolean o1_isSystem = systemObjectIds.contains(o1);
				final boolean o2_isSystem = systemObjectIds.contains(o2);

				final Object o1_value;
				final Object o2_value;

				if (o1_isSystem) {
					o1_value = getMDVOFromList(systemObjects, o1).getField(sEntityFieldForSorting);
				} else {
					EntityObjectVO eo = null;
					try {
						eo = proc.getByPrimaryKey(((Integer)o1).longValue());
					} catch (Exception e) {}
					o1_value = (eo==null) ? null : eo.getFields().get(sEntityFieldForSorting);
				}

				if (o2_isSystem) {
					o2_value = getMDVOFromList(systemObjects, o2).getField(sEntityFieldForSorting);
				} else {
					EntityObjectVO eo = null;
					try {
						eo = proc.getByPrimaryKey(((Integer)o2).longValue());
					} catch (Exception e) {}
					o2_value = (eo==null) ? null : eo.getFields().get(sEntityFieldForSorting);
				}

				if (o1_value != null && o2_value != null && o1_value instanceof String && o2_value instanceof String) {
					return ((String)o1_value).compareToIgnoreCase((String) o2_value) * (bAsc?1:(-1));
				} else
					return LangUtils.compare(o1_value, o2_value) * (bAsc?1:(-1));
			}});
	}

	private Collection<Object> getIds(Collection<MasterDataVO> list) {
		Collection<Object> result = new ArrayList<Object>();
		if (list != null) {
			for (MasterDataVO mdvo : list) {
				result.add(mdvo.getIntId());
			}
		}

		return result;
	}

	private MasterDataVO getMDVOFromList(Collection<MasterDataVO> list, Object id) {
		if (list != null) {
			for (MasterDataVO mdvo : list) {
				if (mdvo.getId().equals(id)) {
					return mdvo;
				}
			}
		}

		return null;
	}

	/**
	 * gets the ids of all masterdata objects
	 *
	 * @return List<Integer> list of masterdata ids
	 */
    @RolesAllowed("Login")
	public List<Object> getMasterDataIds(String sEntityName) {
		final MasterDataMetaVO mdmetacvo = this.getMetaData(sEntityName);

		String dbEntity = mdmetacvo.getDBEntity();

		if(!dataBaseHelper.isObjectAvailable(dbEntity)) {
			throw new CommonFatalException(
				StringUtils.getParameterizedExceptionMessage(
					"masterdata.error.missing.table", dbEntity,
					mdmetacvo.getEntityName()));// "Die Basistabelle/-view '"+dbEntity+"' der Entit\u00e4t '"+mdmetacvo.getEntityName()+"' existiert nicht!");
		}

		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);

		List<Long> eoIds = eoProcessor.getAllIds();
		List<Object> masterDataIds = CollectionUtils.transform(eoIds, new Transformer<Long, Object>() {
			@Override public Object transform(Long l) { return l.intValue(); }
		});
		masterDataIds.addAll(XMLEntities.getSystemObjectIds(sEntityName, null));

		return masterDataIds;
	}

	/**
	 * @param sEntityName
	 * @param lstIntIds
	 * @param lstRequiredSubEntities
	 * @return the next chunk of the search result for a proxy list.
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
	public List<MasterDataWithDependantsVO> getMasterDataMore(
		String sEntityName, final List<?> lstIntIds,
		final List<EntityAndFieldName> lstRequiredSubEntities) {

		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(
			sEntityName);

		return CollectionUtils.transform(lstIntIds,
			new Transformer<Object, MasterDataWithDependantsVO>() {
				@Override
                public MasterDataWithDependantsVO transform(Object oId) {
					try {
						DependantMasterDataMap dmdm = null;
						for(EntityAndFieldName eafn : lstRequiredSubEntities) {
							final String entity = eafn.getEntityName();
							Collection<MasterDataVO> collmdvo = getDependantMasterData(
								entity, eafn.getFieldName(), oId);
							if(!collmdvo.isEmpty()) {
								if(dmdm == null) {
									dmdm = new DependantMasterDataMap();
								}
								dmdm.addAllData(entity, CollectionUtils.transform(collmdvo, 
										new MasterDataToEntityObjectTransformer(entity)));
							}
						}
						return new MasterDataWithDependantsVO(helper.getMasterDataCVOById(mdmetavo, oId), dmdm);
					}
					catch(CommonFinderException ex) {
						// This may never occur inside of a "repeatable read"
						// transaction:
						throw new CommonFatalException(ex);
					}
				}
			});
	}

	/**
	 * convinience function to get all reports or forms used in
	 * AllReportsCollectableFieldsProvider.
	 *
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @throws CommonFinderException if a row was deleted in the time between
	 *            executing the search and fetching the single rows.
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
	public TruncatableCollection<MasterDataVO> getAllReports() throws CommonFinderException, CommonPermissionException {
		this.checkReadAllowed(NuclosEntity.ROLE);
		return helper.getGenericMasterData(NuclosEntity.REPORT.getEntityName(), null, true);
	}

	/**
	 * filter MasterDataVO records from collmdvoReports where the id is not in
	 * collIds
	 *
	 * @param collmdvoReports
	 * @param collIds Collection<MasterDataVO>
	 * @return filtered Collection<MasterDataVO>
	 * @postcondition result != null
	 * @postcondition !result.isTruncated()
	 */
	private TruncatableCollection<MasterDataVO> filterReports(
		Collection<MasterDataVO> collmdvoReports,
		final Map<ReportType, Collection<Integer>> mpReports) {
		final Collection<MasterDataVO> collmdvoResult = CollectionUtils.select(
			collmdvoReports, new Predicate<MasterDataVO>() {
				@Override
                public boolean evaluate(MasterDataVO mdvo) {
					for(ReportType rt : mpReports.keySet()) {
						if(mpReports.get(rt).contains(mdvo.getIntId())) {
							return true;
						}
					}
					return false;
				}
			});
		final TruncatableCollection<MasterDataVO> result = new TruncatableCollectionDecorator<MasterDataVO>(
			collmdvoResult, false, collmdvoResult.size());
		assert result != null;
		assert !result.isTruncated();
		return result;
	}

	/**
	 * gets the dependant master data records for the given entity, using the
	 * given foreign key field and the given id as foreign key.
	 *
	 * @param sEntityName name of the entity to get all dependant master data
	 *           records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
	public Collection<MasterDataVO> getDependantMasterData(String sEntityName,
		String sForeignKeyField, Object oRelatedId) {
		Collection<MasterDataVO> result = CollectionUtils.transform(helper.getDependantMasterData(sEntityName,
			sForeignKeyField, oRelatedId, this.getCurrentUserName()), new EntityObjectToMasterDataTransformer());
		return result;
	}


	@RolesAllowed("Login")
	public Collection<EntityTreeViewVO> getDependantSubnodes(
		String sEntityName, String sForeignKeyField, Object oRelatedId) {
		Collection<EntityTreeViewVO> result = CollectionUtils.transform(helper.getDependantMasterData(sEntityName,
			sForeignKeyField, oRelatedId, this.getCurrentUserName()), new EntityObjectToEntityTreeViewVO());
		return result;
	}


	/**
	 * method to get a master data value object for given primary key id
	 *
	 * @param sEntityName name of the entity to get record for
	 * @param oId primary key id of master data record
	 * @return master data value object
	 * @throws CommonPermissionException
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
	public MasterDataVO get(String sEntityName, Object oId)
		throws CommonFinderException, CommonPermissionException {
		// @todo This doesn't work for entities with composite primary keys
		checkReadAllowed(sEntityName);
		
		getRecordGrantUtils().checkInternal(sEntityName, IdUtils.toLongId(oId));

		Long longId = null;
		if (oId instanceof Integer) {
			longId = IdUtils.toLongId(oId);
		} else if (oId instanceof Long) {
			longId = (Long) oId;
		}

		if ("attributegroup".equals(sEntityName) || NuclosEntity.ENTITYFIELDGROUP.getEntityName().equals(sEntityName)) {
			/**
			 * @TODO auch NuclosDalProvider? z.B. fuer Grunddaten...
			 */
			EntityObjectVO eo = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName).getByPrimaryKey(longId);
			return DalSupportForMD.wrapEntityObjectVO(eo);
		} else {
			return helper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(sEntityName), oId);
		}
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @return the version of the given masterdata id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
    @RolesAllowed("Login")
	public Integer getVersion(String sEntityName, Object oId)
		throws CommonFinderException, CommonPermissionException {
		return this.get(sEntityName, oId).getVersion();
	}

	/**
	 * create a new master data record
	 *
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) -->
	 *               mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
	public MasterDataVO create(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException {
		try {
			if(mdvo.getId() != null) {
				throw new IllegalArgumentException("mdvo.getId()");
			}

			checkWriteAllowed(sEntityName);

			NuclosEntity nuclosEntity = NuclosEntity.getByName(sEntityName);

			final boolean useRuleEngineSave = this.getUsesRuleEngine(sEntityName, RuleEventUsageVO.SAVE_EVENT);
			if(useRuleEngineSave) {
				// In the create case the changes from the rules must be reflected.
				// This is the same as in modify. (tp)
				final RuleObjectContainerCVO roccvoResult = fireSaveEvent(Event.CREATE_BEFORE, 
						sEntityName, mdvo, mpDependants, false);
				mdvo = roccvoResult.getMasterData();
				mpDependants = roccvoResult.getDependants(true);
			}

			if (nuclosEntity == NuclosEntity.RELATIONTYPE) {
				LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
				LocaleInfo localeInfo = localeFacade.getUserLocale();
				String sText = mdvo.getField("name", String.class);
				String sResourceId = localeFacade.setResourceForLocale(null, localeInfo, sText);
				mdvo.setField("labelres", sResourceId);
				if (!localeFacade.getUserLocale().equals(localeFacade.getDefaultLocale())) {
					localeFacade.setDefaultResource(sResourceId, sText);
				}
			}

			// create the row:
			final Integer iId = helper.createSingleRow(sEntityName, mdvo,
				this.getCurrentUserName(),
				this.getServerValidatesMasterDataValues(), null);
			MasterDataVO result;
			try {
				result = helper.getMasterDataCVOById(
					MasterDataMetaCache.getInstance().getMetaData(sEntityName), iId);
			}
			catch(CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}

			if(mpDependants != null && !mpDependants.isEmpty()) {
				if(!mpDependants.areAllDependantsNew()) {
					throw new IllegalArgumentException(
						"Dependants must be new (must have empty ids).");
				}

				LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
					LayoutFacadeLocal.class);
				Map<EntityAndFieldName, String> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
					sEntityName, iId, false);

				// create dependant rows:
				// Note that this currently works for intids only, not for composite
				// primary keys:
				final Integer iParentId = result.getIntId();
				helper.createDependants(mpDependants, sEntityName, iParentId,
					this.getCurrentUserName(),
					this.getServerValidatesMasterDataValues(), null,
					mpEntityAndParentEntityName);
			}
			
			if (NuclosEntity.getByName(sEntityName) != null && mdvo.getResources() != null) {
				LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
				localeFacade.setResources(sEntityName, mdvo);
			}

			if (NuclosEntity.WEBSERVICE.getEntityName().equals(sEntityName)) {
				try {
					NuclosJavaCompiler.check(new WsdlCodeGenerator(mdvo), false);
				}
				catch(NuclosCompileException e) {
					throw new CommonCreateException(e);
				}
			} else if (NuclosEntity.NUCLET.getEntityName().equals(sEntityName)) {
				ServerServiceLocator.getInstance().getFacade(TransferFacadeLocal.class).checkCircularReference(
						IdUtils.toLongId(mdvo.getIntId()));
			}

			boolean useRuleEngineSaveAfter = this.getUsesRuleEngine(sEntityName, RuleEventUsageVO.SAVE_AFTER_EVENT);
			if(useRuleEngineSaveAfter) {
				try {
					mpDependants = reloadDependants(sEntityName, result, true);
					fireSaveEvent(Event.CREATE_AFTER, sEntityName, result, mpDependants, true);
					result = helper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(sEntityName), iId);
				}
				catch (CommonFinderException ex) {
					throw new CommonFatalException(ex);
				}
			}

			MasterDataFacadeHelper.invalidateCaches(sEntityName, mdvo);
			if (nuclosEntity == NuclosEntity.DYNAMICENTITY || nuclosEntity == NuclosEntity.ROLE)
				notifyClients(nuclosEntity);

			return result;
		}
		catch(CommonValidationException ex) {
			throw new CommonCreateException(ex.getMessage(), ex);
		}
	}

	/**
	 * modifies an existing master data record.
	 *
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
	public Object modify(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException {

		checkWriteAllowed(sEntityName);
		getRecordGrantUtils().checkWriteInternal(sEntityName, IdUtils.toLongId(mdvo.getId()));

		NuclosEntity nuclosEntity = NuclosEntity.getByName(sEntityName);

		final boolean useRuleEngineSave = this.getUsesRuleEngine(sEntityName, RuleEventUsageVO.SAVE_EVENT);
		if(useRuleEngineSave) {
			this.debug("Modifying (Start rules)");
			// In the modify case the changes from the rules must be reflected.
			// This is the same as in create. (tp)
			final RuleObjectContainerCVO roccvoResult = this.fireSaveEvent(Event.MODIFY_BEFORE,
				sEntityName, mdvo, mpDependants, false);
			mdvo = roccvoResult.getMasterData();
			mpDependants = roccvoResult.getDependants(true);
		}

		if(nuclosEntity == NuclosEntity.ROLE
			&& SecurityCache.getInstance().isReadAllowedForMasterData(
				this.getCurrentUserName(), NuclosEntity.ROLE.getEntityName())) {
			if(hasUserRole(this.getCurrentUserName(), mpDependants)) {
				helper.validateRoleDependants(mpDependants);

				for(EntityObjectVO mdvo_dep : mpDependants.getData(NuclosEntity.ROLEMASTERDATA.getEntityName())) {
					if(mdvo_dep.isFlagRemoved() && NuclosEntity.ROLE.checkEntityName(mdvo_dep.getField("entity", String.class))) {
						throw new CommonFatalException("masterdata.error.role.permission");// "Sie d\u00fcrfen sich selber keine Rechte entziehen.");
					}
				}
			}
		}
		if (nuclosEntity == NuclosEntity.RELATIONTYPE) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			LocaleInfo localeInfo = localeFacade.getUserLocale();
			String sResourceId = mdvo.getField("labelres", String.class);
			String sText = mdvo.getField("name", String.class);
			sResourceId = localeFacade.setResourceForLocale(sResourceId, localeInfo, sText);
			mdvo.setField("labelres", sResourceId);
		}

		// modify the row itself:
		final Object result;
//		if (DalConstants.ENTITY_NAME_FIELDGROUP.equals(sEntityName)) {
//			EntityObjectVO eo = DalSupportForMD.getEntityObjectVO(mdvo);
//			DalUtils.handleVersionUpdate(NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName), eo, getCurrentUserName());
//			NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName).insertOrUpdate(eo);
//			result = mdvo.getId();
//		} else {
			result = helper.modifySingleRow(sEntityName, mdvo, this.getCurrentUserName(), this.getServerValidatesMasterDataValues());
//		}

		if (NuclosEntity.getByName(sEntityName) != null && mdvo.getResources() != null) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			localeFacade.setResources(sEntityName, mdvo);
		}
			
		if(mpDependants != null) {

			modifyDependants(sEntityName, mdvo.getIntId(), mdvo.isRemoved(),
				mpDependants);
		}

		if (NuclosEntity.WEBSERVICE.getEntityName().equals(sEntityName)) {
			try {
				NuclosJavaCompiler.check(new WsdlCodeGenerator(mdvo), false);
				RuleCache.getInstance().invalidate();
			}
			catch(NuclosCompileException e) {
				throw new CommonCreateException(e);
			}
		} else if (NuclosEntity.NUCLET.getEntityName().equals(sEntityName)) {
			ServerServiceLocator.getInstance().getFacade(TransferFacadeLocal.class).checkCircularReference(
					IdUtils.toLongId(mdvo.getIntId()));
		}

		final boolean useRuleEngineSaveAfter = this.getUsesRuleEngine(sEntityName, RuleEventUsageVO.SAVE_AFTER_EVENT);
		if(useRuleEngineSaveAfter) {
			try {
				this.debug("Modifying (Start rules after save)");
				MasterDataVO updated = get(sEntityName, result);
				mpDependants = reloadDependants(sEntityName, updated, true);
				this.fireSaveEvent(Event.MODIFY_AFTER, sEntityName, mdvo, mpDependants, true);
			}
			catch (CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}
		}

		MasterDataFacadeHelper.invalidateCaches(sEntityName, mdvo);
		if (nuclosEntity == NuclosEntity.DYNAMICENTITY || nuclosEntity == NuclosEntity.ROLE)
			notifyClients(nuclosEntity);

		return result;
	}

	/**
	 * notifies clients that the contents of an entity has changed.
	 *
	 * @param sCachedEntityName name of the cached entity.
	 * @precondition sCachedEntityName != null
	 */
	public void notifyClients(String sCachedEntityName) {
		helper.notifyClients(sCachedEntityName);
	}

	/**
	 * notifies clients that the meta data has changed, so they can invalidate their local caches.
	 * <p>
	 * TODO: Why on hell does this method sends to TOPICNAME_METADATACACHE but the above <code>notifyClients</code>
	 * sends to TOPICNAME_MASTERDATACACHE???
	 * </p>
	 */
	protected void notifyClients(NuclosEntity entity) {
		LOG.info("JMS send: notify clients that entity " + entity.getEntityName() + " changed:" + this);
		NuclosJMSUtils.sendOnceAfterCommitDelayed(entity.getEntityName(), JMSConstants.TOPICNAME_METADATACACHE);
	}

	private boolean hasUserRole(String sUser, DependantMasterDataMap mpDependants) {
		if(mpDependants != null) {
			for(EntityObjectVO mdvo : mpDependants.getData(NuclosEntity.ROLEUSER.getEntityName())) {
				if(mdvo.getField("user", String.class).equals(sUser)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * method to delete an existing master data record
	 *
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only
	 *           given (single) mdvo record this is helpful for entities which
	 *           have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
	public void remove(String sEntityName, MasterDataVO mdvo,
		boolean bRemoveDependants) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException {

		checkDeleteAllowed(sEntityName);
		getRecordGrantUtils().checkDeleteInternal(sEntityName, IdUtils.toLongId(mdvo.getId()));

		NuclosEntity nuclosEntity = NuclosEntity.getByName(sEntityName);

		mdvo.remove();
		
		this.fireDeleteEvent(sEntityName, mdvo, mdvo.getDependants(), false);
		
		if(bRemoveDependants) {
			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			Map<EntityAndFieldName, String> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				sEntityName, mdvo.getIntId(), false);
			DependantMasterDataMap mdp = this.readAllDependants(sEntityName, mdvo.getIntId(),
				mdvo.getDependants(), mdvo.isRemoved(), null, mpEntityAndParentEntityName);
			helper.removeDependants(mdp);
		}

		if (NuclosEntity.WEBSERVICE.getEntityName().equals(sEntityName)) {
			try {
				NuclosJavaCompiler.check(new WsdlCodeGenerator(mdvo), true);
			}
			catch(NuclosCompileException e) {
				throw new CommonRemoveException(e);
			}
			RuleCache.getInstance().invalidate();
		}

		helper.removeSingleRow(sEntityName, mdvo);
		// Note that the dependants are removed via cascading delete in the
		// database.
		helper.removeDependantTaskObjects(mdvo.getIntId()); //explicit delete, because it is not a reference, so no db constraint available

		if (nuclosEntity == NuclosEntity.RELATIONTYPE) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			String sResourceId = mdvo.getField("labelres", String.class);
			localeFacade.deleteResource(sResourceId);
		}

		this.fireDeleteEvent(sEntityName, mdvo, mdvo.getDependants(), true);

		MasterDataFacadeHelper.invalidateCaches(sEntityName, mdvo);
		if (nuclosEntity == NuclosEntity.DYNAMICENTITY || nuclosEntity == NuclosEntity.ROLE)
			notifyClients(nuclosEntity);

		if(isInfoEnabled()) {
			final Object oValue = mdvo.getField("name");
			final String sMessage = (oValue != null) ? "Der Eintrag " + oValue
				+ " (Id: " + mdvo.getId() + ") in der Entit\u00e4t " + sEntityName
				+ " wurde gel\u00f6scht." : "Der Eintrag mit der Id " + mdvo.getId()
				+ " in der Entit\u00e4t " + sEntityName + " wurde gel\u00f6scht.";
			info(sMessage);
		}
	}

	/**
	 * fires a Save event, executing the corresponding business rules.
	 *
	 * @param mdvo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	private RuleObjectContainerCVO fireSaveEvent(Event event, String sEntityName,
		MasterDataVO mdvo, DependantMasterDataMap mpDependants, boolean after)
		throws NuclosBusinessRuleException {

		RuleObjectContainerCVO ruleContainer = ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class).fireRule(
			sEntityName,
			after ? RuleEventUsageVO.SAVE_AFTER_EVENT : RuleEventUsageVO.SAVE_EVENT,
			new RuleObjectContainerCVO(event, mdvo, mpDependants != null
				? mpDependants
				: new DependantMasterDataMap()));

		return ruleContainer;
	}

	/**
	 * fires a Delete event, executing the corresponding business rules.
	 *
	 * @param mdvo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void fireDeleteEvent(String sEntityName, MasterDataVO mdvo,
		DependantMasterDataMap mpDependants, boolean after) throws NuclosBusinessRuleException {

		ServerServiceLocator.getInstance().getFacade(RuleEngineFacadeLocal.class).fireRule(
			sEntityName,
			after ? RuleEventUsageVO.DELETE_AFTER_EVENT : RuleEventUsageVO.DELETE_EVENT,
			new RuleObjectContainerCVO(after ? Event.DELETE_AFTER : Event.DELETE_BEFORE, mdvo, mpDependants != null
				? mpDependants
				: new DependantMasterDataMap()));
	}

	/**
	 * @param sEntityName
	 * @return Does the entity with the given name use the rule engine?
	 */
    @RolesAllowed("Login")
	public boolean getUsesRuleEngine(String sEntityName) {
		return this.getUsesRuleEngine(sEntityName, RuleEventUsageVO.USER_EVENT);
	}

	private boolean getUsesRuleEngine(String sEntityName, String event) {

		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from("T_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn("INTID", Integer.class).alias("intid"));
		query.multiselect(columns);

		List<DbCondition> lstDbCondition = new ArrayList<DbCondition>();
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("STRMASTERDATA", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(sEntityName)));
		lstDbCondition.add(dataBaseHelper.getDbAccess().getQueryBuilder().equal(from.baseColumn("STREVENT", String.class),
			dataBaseHelper.getDbAccess().getQueryBuilder().literal(event)));

		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().and(lstDbCondition.toArray(new DbCondition[0])));

		List<DbTuple> usages = dataBaseHelper.getDbAccess().executeQuery(query);

		return (usages.size() > 0);
	}

	/**
	 * execute a list of rules for the given Object
	 *
	 * @param lstRuleVO
	 * @param mdvo
	 * @param bSaveAfterRuleExecution
	 * @throws CommonBusinessException
	 * @todo restrict permission - check module id!
	 */
    @RolesAllowed("ExecuteRulesManually")
	public void executeBusinessRules(String sEntityName, List<RuleVO> lstRuleVO,
		MasterDataWithDependantsVO mdvo, boolean bSaveAfterRuleExecution)
		throws CommonBusinessException {
		final RuleObjectContainerCVO loccvo = ServerServiceLocator.getInstance().getFacade(
			RuleEngineFacadeLocal.class).executeBusinessRules(lstRuleVO,
			new RuleObjectContainerCVO(Event.USER, mdvo, mdvo.getDependants()), false);
		if(bSaveAfterRuleExecution) {
			this.modify(sEntityName, loccvo.getMasterData(),
				loccvo.getDependants(true));
		}
	}

	/**
	 * Get all subform entities of a masterdata entity
	 *
	 * @param entityName
	 */
    @RolesAllowed("Login")
	public Set<EntityAndFieldName> getSubFormEntitiesByMasterDataEntity(String entityName) {
		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);

		if(MasterDataMetaCache.getInstance().exist(entityName)
			&& !StringUtils.isNullOrEmpty(layoutFacade.getMasterDataLayout(entityName))) {
			Map<EntityAndFieldName, String> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityName, MasterDataMetaCache.getInstance().getMetaData(
					entityName).getId(), false);
			return new HashSet<EntityAndFieldName>(mpEntityAndParentEntityName.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * read all dependant masterdata recursively if necessary, mark the read data
	 * as removed
	 *
	 * @param sEntityName
	 * @param mdvo
	 */
    @RolesAllowed("Login")
	public DependantMasterDataMap readAllDependants(String sEntityName,
		Integer iId, DependantMasterDataMap mpDependants, Boolean bRemoved,
		String sParentEntity,
		Map<EntityAndFieldName, String> mpEntityAndParentEntityName) {
		Collection<EntityObjectVO> collmdvo = Collections.<EntityObjectVO>emptyList();

		// last subform in hierarchie found
		if(mpEntityAndParentEntityName.containsValue(sParentEntity)) {
			for(EntityAndFieldName eafn : mpEntityAndParentEntityName.keySet()) {
				// first subform in hierarchie found or
				// child subfrom found
				final String entity = eafn.getEntityName();
				if((mpEntityAndParentEntityName.get(eafn) == null && sParentEntity == null)
					|| (mpEntityAndParentEntityName.get(eafn) != null 
					&& mpEntityAndParentEntityName.get(eafn).equals(sParentEntity))) {
					if(!mpDependants.getData(entity).isEmpty()) {
						collmdvo = CollectionUtils.emptyIfNull(mpDependants.getData(entity));
					}
					else {
						if(iId != null) {
							Collection<EntityObjectVO> col = CollectionUtils.transform(getDependantMasterData(
								entity,
								helper.getForeignKeyFieldName(sEntityName, entity, mpEntityAndParentEntityName), 
								iId), 
								new MasterDataToEntityObjectTransformer(entity));

							collmdvo = CollectionUtils.emptyIfNull(col);
							mpDependants.addAllData(entity, collmdvo);
						}
					}

					for(EntityObjectVO dmdvo : collmdvo) {
						if(bRemoved) {
							dmdvo.flagRemove();
						}
						dmdvo.setDependants(readAllDependants(eafn.getEntityName(),
								IdUtils.unsafeToId(dmdvo.getId()), dmdvo.getDependants(),
							dmdvo.isFlagRemoved(), eafn.getEntityName(),
							mpEntityAndParentEntityName));
					}
				}
			}
		}
		return mpDependants;
	}

	/**
	 * modifies the given dependants (local use only).
	 *
	 * @param dependants
	 * @precondition mpDependants != null
	 */
    public void modifyDependants(String entityName, Integer id, Boolean removed,
		DependantMasterDataMap dependants) throws CommonCreateException,
		CommonFinderException, CommonRemoveException, CommonPermissionException,
		CommonStaleVersionException {
		// todo: check and clean thrown exception types to necessary minimum

		if(dependants == null) {
			throw new NullArgumentException("dependants");
		}

		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);
		Map<EntityAndFieldName, String> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			entityName, id, false);
		readAllDependants(entityName, id, dependants, removed, null,
			mpEntityAndParentEntityName);

		helper.removeDependants(dependants);

		try {
			helper.createOrModifyDependants(dependants, entityName,
				this.getCurrentUserName(),
				this.getServerValidatesMasterDataValues(), null,
				mpEntityAndParentEntityName);
		}
		catch(CommonValidationException ex) {
			// @todo check this exception handling
			throw new CommonCreateException(ex.getMessage(), ex);
		}
	}

	/**
	 * revalidates the cache. This may be used for development purposes only, in
	 * order to rebuild the cache after metadata entries in the database were
	 * changed.
	 */
    @RolesAllowed("UseManagementConsole")
	public void revalidateMasterDataMetaCache() {
		MasterDataMetaCache.getInstance().revalidate();
	}

	/**
	 * value list provider function (get processes by usage)
	 *
	 * @param iModuleId module id of usage criteria
	 * @param bSearchMode when true, validity dates and/or active sign will not
	 *           be considered in the search.
	 * @return collection of master data value objects
	 */
    @RolesAllowed("Login")
	public java.util.List<org.nuclos.common.collect.collectable.CollectableField> getProcessByUsage(
		Integer iModuleId, boolean bSearchMode) {
		// @todo Try to replace with getDependantMasterData

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom process = query.from("T_MD_PROCESS").alias("process");
		query.multiselect(process.baseColumn("INTID", Integer.class), process.baseColumn("STRPROCESS", String.class));
		DbCondition condition = builder.equal(process.baseColumn("INTID_T_MD_MODULE", Integer.class), iModuleId);
		if (!bSearchMode) {
			DbColumnExpression<Date> datValidFrom = process.baseColumn("DATVALIDFROM", Date.class);
			DbColumnExpression<Date> datValidUntil = process.baseColumn("DATVALIDUNTIL", Date.class);
			condition = builder.and(
				condition,
				builder.or(builder.lessThanOrEqualTo(datValidFrom, builder.currentDate()), datValidFrom.isNull()),
				builder.or(builder.greaterThanOrEqualTo(datValidUntil, builder.currentDate()), datValidUntil.isNull()));
		}
		query.where(condition);

		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, CollectableField>() {
			@Override
			public CollectableField transform(DbTuple t) {
				return new CollectableValueIdField(t.get(0, Integer.class), t.get(1, String.class));
			}
		});
	}

	/**
	 * @param iModuleId the id of the module whose subentities we are looking for
	 * @return Collection<MasterDataMetaVO> the masterdata meta information for
	 *         all entities having foreign keys to the given module.
	 */
    public List<org.nuclos.common.collect.collectable.CollectableField> getSubEntities(Integer iModuleId) {

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom m = query.from("T_MD_ENTITY").alias("m");
		DbFrom mf = m.join("T_MD_ENTITY_FIELD", JoinType.INNER).alias("mf").on("INTID", "INTID_T_MD_ENTITY", Integer.class);
		DbFrom p = mf.join("T_MD_ENTITY", JoinType.INNER).alias("p").on("STRFOREIGNENTITY", "STRENTITY", String.class);
		query.multiselect(m.baseColumn("INTID", Integer.class),	m.baseColumn("STRENTITY", String.class));
		query.where(builder.equal(p.baseColumn("INTID", Integer.class), iModuleId));
		query.orderBy(builder.asc(m.baseColumn("STRENTITY", String.class)));

		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, CollectableField>() {
			@Override
			public CollectableField transform(DbTuple t) {
				return new CollectableValueIdField(t.get(0, Integer.class), t.get(1, String.class));
			}
		});
	}

    public Map<String, String> getRuleEventsWithLocaleResource() {
		Map<String, String> mp = CollectionUtils.newHashMap();
		// TODO_AUTOSYNC: Re-merge with database, add resourceId to metadata
		for(MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.EVENT).getAll()) {
			mp.put(mdvo.getField("name", String.class), mdvo.getField(
				"labelres", String.class));
		}
		return mp;
	}

	/**
	 * Write changes to dependant masterdata into logbook table if specified for
	 * the field.
	 *
	 * @param iGenericObjectId id of the leased object
	 * @param mpDependants the dependant map of the leased object
	 * @param stExcluded a set of record ids to exclude from writing; i.e. which
	 *           have been written already
	 * @param bOnlyNewEntries notifies this method that only newly created
	 *           records are to be processed, which have no old value
	 * @throws CommonPermissionException
	 * @todo restrict permissions by entity name / module id
	 */
    @RolesAllowed("Login")
	public void protocolDependantChanges(Integer iGenericObjectId,
		DependantMasterDataMap mpDependants, Set<Integer> stExcluded,
		boolean bOnlyNewEntries) throws CommonPermissionException {
		for(String sDependantEntityName : mpDependants.getEntityNames()) {
			// get the metadata for the entity which have to be logged and iterate
			// them
			final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(
				sDependantEntityName);
			final List<MasterDataMetaFieldVO> lstmdmetafieldvo = mdmetavo.getFieldsForLogging();

			if(!lstmdmetafieldvo.isEmpty()) {
				for(final EntityObjectVO mdvoDependant : mpDependants.getData(sDependantEntityName)) {
					final Integer iRecordId = IdUtils.unsafeToId(mdvoDependant.getId());

					// Change only what has been changed
					if(iRecordId != null && !mdvoDependant.isFlagUpdated()
						&& !mdvoDependant.isFlagRemoved()) {
						stExcluded.add(iRecordId);
						continue;
					}

					// Differ between change types and record the changes
					if(iRecordId != null && !stExcluded.contains(iRecordId)) {
						// Determine what has actually been done to the record
						final String sAction;
						if(mdvoDependant.isFlagRemoved()) {
							sAction = "D";
						}
						else if(bOnlyNewEntries) {
							sAction = "C";
						}
						else {
							sAction = "M";
						}
						for(MasterDataMetaFieldVO mdmetafieldvo : lstmdmetafieldvo) {
							// Only if the record has not been deleted we may have a
							// new value
							final String sNewValue;
							if(!sAction.equals("D")) {
								sNewValue = MasterDataFacadeHelper.getProtocolValue(mdvoDependant.getFields().get(mdmetafieldvo.getFieldName()));
							}
							else {
								sNewValue = "";
							}

							// Only if the record has not been freshly created we are
							// able to retrieve an old value for it
							final String sOldValue;
							if(!sAction.equals("C")) {
								try {
									sOldValue = MasterDataFacadeHelper.getProtocolValue(get(
										sDependantEntityName, iRecordId).getField(
										mdmetafieldvo.getFieldName()));
								}
								catch(CommonFinderException ex) {
									// We seem to have a problem here when we modify a
									// nonexisting record...
									throw new NuclosFatalException(
										"Trying to modify a nonexisting record..", ex);
								}
							}
							else {
								sOldValue = "";
							}

							// track only actually changed values (regardless if they
							// have been trimmed or not)
							if(!sOldValue.trim().equals(sNewValue)) {
								final GenericObjectFacadeLocal genericObjectFacade 
									= ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
								genericObjectFacade.createLogbookEntry(
										iGenericObjectId, null, mdmetavo.getId(),
										mdmetafieldvo.getId(), iRecordId, sAction,
										null, null, sOldValue, null, null, sNewValue);
							}
							stExcluded.add(iRecordId); // Remember that it is already
																// written
						}
					}
				}
			}
		}
	}

	/**
	 * Validate all masterdata entries against their meta information (length,
	 * format, min, max etc.). The transaction type is "not supported" here in
	 * order to avoid a transaction timeout, as the whole operation may take some
	 * time.
	 *
	 * @param sOutputFileName the name of the csv file to which the results are
	 *           written.
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED, noRollbackFor= {Exception.class})
	@RolesAllowed("UseManagementConsole")
	public void checkMasterDataValues(String sOutputFileName) {
		final PrintStream ps;
		try {
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(
				sOutputFileName)), true);
		}
		catch(FileNotFoundException ex) {
			throw new NuclosFatalException(
				StringUtils.getParameterizedExceptionMessage(
					"masterdata.error.missing.file", sOutputFileName), ex);
		}

		ps.println("Entit\u00e4t; ID; Fehlermeldung");
		for(MasterDataMetaVO mdmcvo : MasterDataMetaCache.getInstance().getAllMetaData()) {
			final String sEntityName = mdmcvo.getEntityName();
			try {
				for(MasterDataVO mdvo : helper.getGenericMasterData(sEntityName, null, true)) {
					try {
						// validate each record
						mdvo.validate(mdmcvo);
					}
					catch(CommonValidationException ex) {
						final StringBuilder sbResult = new StringBuilder();
						sbResult.append(sEntityName);
						sbResult.append(";");
						sbResult.append(mdvo.getId());
						sbResult.append(";");
						sbResult.append(ex.getMessage());
						ps.println(sbResult.toString());
					}
				}
			}
			catch(Exception e) {
				LOG.error("checkMasterDataValues failed: " + e, e);
				error("Error while validating entity " + sEntityName);
			}
		}
		if(ps != null) {
			ps.close();
		}
		if(ps != null && ps.checkError()) {
			throw new NuclosFatalException("Failed to close PrintStream.");
		}
	}

	/**
	 * gets master data vo for a given master data id, along with its dependants.
	 *
	 * @param iObjectId id of master data
	 * @return master data vo with dependants
	 * @throws CommonFinderException if no such object was found.
	 * @throws NuclosBusinessException
	 */
    public RuleObjectContainerCVO getRuleObjectContainerCVO(Event event, String sEntityName,
		Integer iObjectId) throws CommonPermissionException,
		CommonFinderException, NuclosBusinessException {

		final MasterDataWithDependantsVO mdvo = this.getWithDependants(
			sEntityName, iObjectId);

		return new RuleObjectContainerCVO(event, mdvo, mdvo.getDependants());
	}

	/**
	 * @param sEntityName
	 * @param iId the object's id (primary key)
	 * @return the masterdata object with the given entity and id.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
    public MasterDataWithDependantsVO getWithDependants(String sEntityName,
		Integer iId) throws CommonFinderException, NuclosBusinessException,
		CommonPermissionException {
		if(iId == null) {
			throw new NullArgumentException("iId");
		}
		List<EntityAndFieldName> lsteafn = new ArrayList<EntityAndFieldName>();

		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);
		for(EntityAndFieldName eafn : layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			sEntityName, iId, false).keySet()) {
			lsteafn.add(eafn);
		}

		return new MasterDataWithDependantsVO(this.get(sEntityName, iId),
			this.getDependants(iId, lsteafn));
	}

	/**
	 * @param sEntityName
	 * @param cond search condition
	 * @return the masterdata objects for the given entityname and search
	 *         condition.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
    public Collection<MasterDataWithDependantsVO> getWithDependantsByCondition(
		String sEntityName, CollectableSearchCondition cond) {
		Collection<MasterDataWithDependantsVO> result = new ArrayList<MasterDataWithDependantsVO>();

		for(MasterDataVO mdVO : getMasterData(sEntityName, cond, true)) {
			List<EntityAndFieldName> lsteafn = new ArrayList<EntityAndFieldName>();

			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			for(EntityAndFieldName eafn : layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				sEntityName, mdVO.getIntId(), false).keySet()) {
				lsteafn.add(eafn);
			}
			result.add(new MasterDataWithDependantsVO(mdVO, this.getDependants(
				mdVO.getIntId(), lsteafn)));
		}

		return result;
	}

	public DependantMasterDataMap reloadDependants(String entityname, MasterDataVO mdvo, boolean bAll) throws CommonFinderException {
		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

		final Map<EntityAndFieldName, String> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(entityname, mdvo.getIntId(), false);
		return getDependants(mdvo.getIntId(), new ArrayList<EntityAndFieldName>(collSubEntities.keySet()));
	}

	public DependantMasterDataMap getDependants(Object oId,
		List<EntityAndFieldName> lsteafn) {
		final DependantMasterDataMap result = new DependantMasterDataMap();
		for(EntityAndFieldName eafn : lsteafn) {
			final String entity = eafn.getEntityName();
			Collection<EntityObjectVO> col = CollectionUtils.transform(this.getDependantMasterData(
				entity, eafn.getFieldName(), oId), new MasterDataToEntityObjectTransformer(entity));

			result.addAllData(eafn.getEntityName(), col);
		}
		return result;
	}

	/**
	 * gets the file content of a generic object document
	 *
	 * @param iGenericObjectDocumentId generic object document id
	 * @return generic object document file content
	 * @todo restrict permission - check module id!
	 */
    @RolesAllowed("Login")
	public byte[] loadContent(Integer iGenericObjectDocumentId, String sFileName, String sPath)
		throws CommonFinderException {
		final java.io.File documentDir = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH) + "/" +
			StringUtils.emptyIfNull(sPath) + "/");
		if(iGenericObjectDocumentId == null) {
			throw new NuclosFatalException("godocumentfile.invalid.id");// "Die Id des Dokumentanhangs darf nicht null sein");
		}
		java.io.File file = new java.io.File(documentDir, iGenericObjectDocumentId + "." + sFileName);

		try {
			return IOUtils.readFromBinaryFile(file);
		}
		catch(IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * @param user - the user for which to get subordinated users
	 * @return List<MasterDataVO> list of masterdata valueobjects
	 */
	public List<MasterDataVO> getUserHierarchy(String user) {
		boolean isSuperUser = SecurityCache.getInstance().isSuperUser(user);
		if (!isSuperUser) {
			List<Object> roles = new ArrayList<Object>();
			roles.addAll(getRolesHierarchyForUser(user));
			return getUsersForRoles(roles);
		} else {
			return new ArrayList<MasterDataVO>(this.getMasterData(NuclosEntity.USER.getEntityName(), null, false));
		}
	}

	private Set<Integer> getRolesHierarchyForUser(String user) {
		Set<Integer> roles = new HashSet<Integer>();
		Collection<MasterDataVO> userRoles = this.getMasterData(NuclosEntity.ROLEUSER.getEntityName(), SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLEUSER), "user", ComparisonOperator.EQUAL, user), false);
		for (MasterDataVO voRole : userRoles) {
			roles.add(voRole.getField("roleId", Integer.class));
			addSubordinateRoles(voRole.getField("roleId", Integer.class), roles);
		}
		return roles;
	}

	private void addSubordinateRoles(Integer role, Set<Integer> alreadyCollectedRoles) {
		Set<Integer> roles = new HashSet<Integer>();
		Collection<MasterDataVO> subordinateRoles = this.getMasterData(NuclosEntity.ROLE.getEntityName(), SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLE.getEntityName()), "parentrole", role), false);
		for (MasterDataVO voRole : subordinateRoles) {
			if (!alreadyCollectedRoles.contains(voRole.getIntId())) {
				roles.add(voRole.getIntId());
				addSubordinateRoles(voRole.getIntId(), roles);
			}
		}
		alreadyCollectedRoles.addAll(roles);
	}

	private List<MasterDataVO> getUsersForRoles(List<Object> roles) {
		CollectableEntityField entityFieldRole = new CollectableMasterDataEntity(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.ROLEUSER.getEntityName())).getEntityField("role");
		ReferencingCollectableSearchCondition refCond = new ReferencingCollectableSearchCondition(entityFieldRole, new CollectableIdListCondition(roles));
		Collection<MasterDataVO> users = this.getMasterData(NuclosEntity.ROLEUSER.getEntityName(), refCond, false);
		return CollectionUtils.transform(new HashSet<MasterDataVO>(users), new Transformer<MasterDataVO, MasterDataVO>() {
			@Override
			public MasterDataVO transform(MasterDataVO roleuser) {
				return DalSupportForMD.wrapEntityObjectVO(NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.USER).getByPrimaryKey(roleuser.getField("userId", Integer.class).longValue()));
			}
		});
	}
}

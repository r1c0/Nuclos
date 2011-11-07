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
package org.nuclos.server.dbtransfer;
import static org.nuclos.server.dbtransfer.TransferUtils.checkValidity;
import static org.nuclos.server.dbtransfer.TransferUtils.getDependencies;
import static org.nuclos.server.dbtransfer.TransferUtils.getEntities;
import static org.nuclos.server.dbtransfer.TransferUtils.getEntity;
import static org.nuclos.server.dbtransfer.TransferUtils.getIdentifier;
import static org.nuclos.server.dbtransfer.TransferUtils.getIds;
import static org.nuclos.server.dbtransfer.TransferUtils.getUserEntityFields;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.StaticMetaDataProvider;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.PreviewPart;
import org.nuclos.common.dbtransfer.Transfer;
import org.nuclos.common.dbtransfer.TransferNuclet;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.LockedTabProgressNotifier;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.NuclosDataSources;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbObjectHelper;
import org.nuclos.server.dblayer.DbObjectHelper.DbObject;
import org.nuclos.server.dblayer.DbObjectHelper.DbObjectType;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;
import org.nuclos.server.dbtransfer.content.ActionNucletContent;
import org.nuclos.server.dbtransfer.content.CustomComponentNucletContent;
import org.nuclos.server.dbtransfer.content.DefaultNucletContent;
import org.nuclos.server.dbtransfer.content.EntityFieldNucletContent;
import org.nuclos.server.dbtransfer.content.EntityMenuNucletContent;
import org.nuclos.server.dbtransfer.content.EntityNucletContent;
import org.nuclos.server.dbtransfer.content.EntitySubnodesNucletContent;
import org.nuclos.server.dbtransfer.content.EventNucletContent;
import org.nuclos.server.dbtransfer.content.INucletContent;
import org.nuclos.server.dbtransfer.content.ImportFileNucletContent;
import org.nuclos.server.dbtransfer.content.JobControllerNucletContent;
import org.nuclos.server.dbtransfer.content.RelationTypenucletContent;
import org.nuclos.server.dbtransfer.content.ResourceNucletContent;
import org.nuclos.server.dbtransfer.content.RuleNucletContent;
import org.nuclos.server.dbtransfer.content.SearchFilterNucletContent;
import org.nuclos.server.dbtransfer.content.StateNucletContent;
import org.nuclos.server.dbtransfer.content.UserNucletContent;
import org.nuclos.server.dbtransfer.content.ValidityType;
import org.nuclos.server.dbtransfer.content.WorkspaceNucletContent;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


// @Stateless
// @Remote(TransferFacadeRemote.class)
// @Local(TransferFacadeLocal.class)
@Transactional
@RolesAllowed("UseManagementConsole")
public class TransferFacadeBean extends NuclosFacadeBean
	implements TransferFacadeRemote, TransferFacadeLocal
{

	private static final Logger LOG = Logger.getLogger(TransferFacadeBean.class);

	private static final Integer TRANSFER_VERSION = 1;

	private static final String ROOT_ENTRY_NAME = "nuclet_exportParameter.xml";

	private static final String UID = "nuclet_contentuid.xml";

	private static final String TABLE_ENTRY_SUFFIX = ".eo.xml";

	/**
	 *
	 * @param transferOptions
	 * @return
	 */
	private static List<INucletContent> getNucletContentInstances(TransferOption.Map transferOptions) {
		List<INucletContent> contents = new ArrayList<INucletContent>();

		INucletContent userNC = new UserNucletContent(contents);
		INucletContent ldapServerNC = new DefaultNucletContent(NuclosEntity.LDAPSERVER, null, contents, true);
		INucletContent ldapMappingNC = new DefaultNucletContent(NuclosEntity.LDAPMAPPING, NuclosEntity.LDAPSERVER, contents, true);
		INucletContent importFileNC = new ImportFileNucletContent(contents);
		INucletContent importUsageNC = new DefaultNucletContent(NuclosEntity.IMPORTUSAGE, NuclosEntity.IMPORTFILE, contents, true);
		if (transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE)) {
			userNC.setEnabled(transferOptions.containsKey(TransferOption.INCLUDES_USER));
			ldapServerNC.setEnabled(transferOptions.containsKey(TransferOption.INCLUDES_LDAP));
			ldapMappingNC.setEnabled(transferOptions.containsKey(TransferOption.INCLUDES_LDAP));
			importFileNC.setEnabled(transferOptions.containsKey(TransferOption.INCLUDES_IMPORTFILE));
			importUsageNC.setEnabled(transferOptions.containsKey(TransferOption.INCLUDES_IMPORTFILE));
		} else {
			userNC.setEnabled(false);
			ldapServerNC.setEnabled(false);
			ldapMappingNC.setEnabled(false);
			importFileNC.setEnabled(false);
			importUsageNC.setEnabled(false);
		}

		contents.add(new DefaultNucletContent(NuclosEntity.NUCLET, null, contents, true));
		contents.add(new DefaultNucletContent(NuclosEntity.NUCLETDEPENDENCE, NuclosEntity.NUCLET, contents, true));

		contents.add(new ResourceNucletContent(contents));
		contents.add(new RelationTypenucletContent(contents));
		contents.add(new ActionNucletContent(contents));
		contents.add(new EventNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DATATYP, null, contents));

		contents.add(new EntityNucletContent(contents));
		contents.add(new EntityFieldNucletContent(contents));
		contents.add(new EntitySubnodesNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ENTITYFIELDGROUP, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ENTITYRELATION, null, contents));
		contents.add(new EntityMenuNucletContent(contents));

		contents.add(new DefaultNucletContent(NuclosEntity.GROUPTYPE, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.GROUP, NuclosEntity.GROUPTYPE, contents));
		contents.add(userNC);

		contents.add(new DefaultNucletContent(NuclosEntity.PARAMETER, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DBOBJECT, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DBSOURCE, NuclosEntity.DBOBJECT, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.PROCESS, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.GENERATION, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.GENERATIONATTRIBUTE, NuclosEntity.GENERATION, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.GENERATIONSUBENTITY, NuclosEntity.GENERATION, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.GENERATIONUSAGE, NuclosEntity.GENERATION, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.RULEGENERATION, NuclosEntity.GENERATION, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.STATEMODEL, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.STATEMODELUSAGE, NuclosEntity.STATEMODEL, contents));
		contents.add(new StateNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.STATEMANDATORYFIELD, NuclosEntity.STATE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.STATEMANDATORYCOLUMN, NuclosEntity.STATE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.STATETRANSITION, NuclosEntity.STATE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.RULETRANSITION, NuclosEntity.STATETRANSITION, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.CODE, null, contents));
		contents.add(new RuleNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.RULEUSAGE, NuclosEntity.RULE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.TIMELIMITRULE, null, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.IMPORT, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.IMPORTATTRIBUTE, NuclosEntity.IMPORT, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.IMPORTIDENTIFIER, NuclosEntity.IMPORT, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.IMPORTFEIDENTIFIER, NuclosEntity.IMPORTATTRIBUTE, contents));
		contents.add(importFileNC);
		contents.add(importUsageNC);
		contents.add(ldapServerNC);
		contents.add(ldapMappingNC);

		contents.add(new JobControllerNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.JOBDBOBJECT, NuclosEntity.JOBCONTROLLER, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.JOBRULE, NuclosEntity.JOBCONTROLLER, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.LAYOUT, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.LAYOUTUSAGE, NuclosEntity.LAYOUT, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.DATASOURCE, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DATASOURCEUSAGE, NuclosEntity.DATASOURCE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.VALUELISTPROVIDER, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.VALUELISTPROVIDERUSAGE, NuclosEntity.VALUELISTPROVIDER, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DYNAMICENTITY, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.DYNAMICENTITYUSAGE, NuclosEntity.DYNAMICENTITY, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.RECORDGRANT, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.RECORDGRANTUSAGE, NuclosEntity.RECORDGRANT, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.WEBSERVICE, null, contents));
		contents.add(new WorkspaceNucletContent(contents));
		contents.add(new CustomComponentNucletContent(contents));

		contents.add(new DefaultNucletContent(NuclosEntity.REPORT, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.REPORTOUTPUT, NuclosEntity.REPORT, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.SUBREPORT, NuclosEntity.REPORTOUTPUT, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.FORMUSAGE, NuclosEntity.REPORT, contents));

		contents.add(new DefaultNucletContent(NuclosEntity.ROLE, null, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEUSER, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEACTION, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLETRANSITION, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEENTITYFIELD, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEATTRIBUTEGROUP, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEMASTERDATA, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEMODULE, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLESUBFORM, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLESUBFORMCOLUMN, NuclosEntity.ROLESUBFORM, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEREPORT, NuclosEntity.ROLE, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.ROLEWORKSPACE, NuclosEntity.ROLE, contents));

		contents.add(new SearchFilterNucletContent(contents));
		contents.add(new DefaultNucletContent(NuclosEntity.SEARCHFILTERUSER, NuclosEntity.SEARCHFILTER, contents));
		contents.add(new DefaultNucletContent(NuclosEntity.SEARCHFILTERROLE, NuclosEntity.SEARCHFILTER, contents));

		return contents;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<TransferNuclet> getAvaiableNuclets() {
		List<TransferNuclet> result = new ArrayList<TransferNuclet>();
		for (EntityObjectVO nucletObject : getProcessor(NuclosEntity.NUCLET).getAll()) {
			result.add(new TransferNuclet(nucletObject.getId(), nucletObject.getField("name", String.class)));
		}

		return CollectionUtils.sorted(result, new Comparator<TransferNuclet>() {
			@Override
			public int compare(TransferNuclet o1, TransferNuclet o2) {
				return LangUtils.compare(o1.getLabel(), o2.getLabel());
			}});
	}

	/**
	 * creates a file for configuration transfer.
	 *
	 * @return the file content as byte array
	 * @throws NuclosBusinessException
	 */
	@Override
	public byte[] createTransferFile(Long nucletId, TransferOption.Map exportOptions) throws NuclosBusinessException {
		if (nucletId == null)
			exportOptions.put(TransferOption.IS_NUCLOS_INSTANCE, null);

		info("CREATE Transfer (nucletId=" + nucletId + ")");
		LockedTabProgressNotifier jmsNotifier = new LockedTabProgressNotifier(Transfer.TOPIC_CORRELATIONID_CREATE);
		jmsNotifier.notify("read nuclet contents", 0);

		info("get nuclet content instances");
		List<INucletContent> 	contentTypes = getNucletContentInstances(exportOptions);
		Set<Long> 				existingNucletIds = getExistingNucletIds(nucletId);
		List<EntityObjectVO> 	uidObjects = getUIDObjects(existingNucletIds, contentTypes, exportOptions);
		ByteArrayOutputStream 	bout = new ByteArrayOutputStream(16348);
		ZipOutput 				zout = new ZipOutput(bout);
		String					nucletUID = null;

		final String writeContent = "write nuclet contents to file";
		double progressPerContent = 80/contentTypes.size();
		double progressCurrent = 10;
		for (INucletContent nc : contentTypes) {
			info("read content for nuclos entity: " + nc.getEntity());
			jmsNotifier.notify(writeContent, Double.valueOf(progressCurrent).intValue());
			progressCurrent += progressPerContent;

			List<EntityObjectVO> ncObjects = nc.getNcObjects(existingNucletIds, exportOptions);
			updateUIDObjectVersion(nc, ncObjects, uidObjects);
			createMissingUIDs(ncObjects, uidObjects);
			info("add content to zip");
			zout.addEntry(nc.getEntity().getEntityName()+TABLE_ENTRY_SUFFIX, toXML(ncObjects));
		}

		jmsNotifier.notify("safe file", 90);

		info("find nuclet uid");
		for (EntityObjectVO uidObject : uidObjects) {
			if (LangUtils.equals(uidObject.getField("nuclosentity", String.class), NuclosEntity.NUCLET.getEntityName()) &&
				LangUtils.equals(uidObject.getField("objectid", Long.class), nucletId))
				nucletUID = uidObject.getField("uid", String.class);
		}
		if (nucletId != null && nucletUID == null) {
			throw new NuclosFatalException("Nuclet UID not found/created");
		}

		cleanupUIDs();

		info("add root to zip");
		zout.addEntry(ROOT_ENTRY_NAME, toXML(buildMetaDataRoot(nucletUID, exportOptions)));
		info("add uid's to zip");
		zout.addEntry(UID, toXML(uidObjects));

		zout.close();
		byte[] bytes = bout.toByteArray();
		jmsNotifier.notify("finish", 100);
		return bytes;
	}

	/**
	 *
	 * @param List<EntityObjectVO> ncObjects
	 * @param List<EntityObjectVO> uidObjects
	 */
	private void createMissingUIDs(List<EntityObjectVO> ncObjects, List<EntityObjectVO> uidObjects) {
		NucletContentUID.Map uidMap = new NucletContentUID.HashMap();
		uidMap.addAll(uidObjects);
		for (EntityObjectVO ncObject : ncObjects) {
			if (!uidMap.containsKey(new NucletContentUID.Key(NuclosEntity.getByName(ncObject.getEntity()), ncObject.getId()))) {
				EntityObjectVO uidObject = createUIDRecordForNcObject(ncObject);
				info("create missind uid " + uidObject.getFields());
				uidObjects.add(uidObject);
				uidMap.add(uidObject);
			}
		}
	}

	/**
	 *
	 * @param INucletContent nc
	 * @param List<EntityObjectVO> ncObjects
	 * @param List<EntityObjectVO> uidObjects
	 */
	private void updateUIDObjectVersion(INucletContent nc, List<EntityObjectVO> ncObjects, List<EntityObjectVO> uidObjects) {
		for (EntityObjectVO ncObject : ncObjects) {
			for (EntityObjectVO uidObject : uidObjects) {
				if (LangUtils.equals(uidObject.getField("nuclosentity", String.class), nc.getEntity().getEntityName()) &&
					LangUtils.equals(uidObject.getField("objectid", Long.class), ncObject.getId())) {
					if (!LangUtils.equals(uidObject.getField("objectversion", Integer.class), ncObject.getVersion())) {
						uidObject.getFields().put("objectversion", ncObject.getVersion());
						DalUtils.updateVersionInformation(uidObject, getCurrentUserName());
						uidObject.flagUpdate();
						info("uid object version \"" + uidObject.getField("objectversion", Integer.class) + "\" differs from object version \"" + ncObject.getVersion() + "\" --> update version in existing uid");
						getProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(uidObject);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param bytes
	 * @return Map<TransferOption, Serializable>
	 * @throws NuclosBusinessException
	 */
	public TransferOption.Map getOptions(byte[] bytes) throws NuclosBusinessException {
		MetaDataRoot root = readBytes(bytes, null, false, null, null, null);
		String error = matchMetaDataRoot(root);
		if (error != null)
			throw new NuclosBusinessException(error);
		return root.exportOptions;
	}

	/**
	 * @param bytes the content of a transfer file
	 * @return a <code>Transfer</code> object describing how the
	 * current configuration would change if the transfer is executed
	 * @throws NuclosBusinessException
	 */
	@Override
	public Transfer prepareTransfer(boolean isNuclon, byte[] bytes) throws NuclosBusinessException
	{
		cleanupUIDs();
		info("PREPARE Transfer (isNuclon=" + isNuclon + ")");
		LockedTabProgressNotifier jmsNotifier = new LockedTabProgressNotifier(Transfer.TOPIC_CORRELATIONID_PREPARE);
		NucletContentUID.Map uidImportMap = new NucletContentUID.HashMap();

		Collection<EntityObjectVO> parameter = new ArrayList<EntityObjectVO>();
		Map<String, List<EntityObjectVO>> importData = new HashMap<String, List<EntityObjectVO>>();

		jmsNotifier.notify("read file", 0);
		MetaDataRoot root = readBytes(bytes, parameter, false, null, importData, uidImportMap);

		if (isNuclon && !root.exportOptions.containsKey(TransferOption.IS_NUCLON_IMPORT_ALLOWED))
			throw new IllegalArgumentException("isNuclon");

		jmsNotifier.notify("load existing nuclets and uid's", 10);
		info("get nuclet content instances");
		List<INucletContent> contentTypes = getNucletContentInstances(root.exportOptions);
		Set<Long> existingNucletIds = isNuclon?new HashSet<Long>():getExistingNucletIds(root.nucletUID);
		info("existing nuclet ids: " + existingNucletIds);
		info("get uid map");
		NucletContentUID.Map uidExistingMap = getUIDMap(existingNucletIds, contentTypes, root.exportOptions);
		info("get new user count");
		int newUserCount = getNewUserCount(importData.get(NuclosEntity.USER.getEntityName()));
		List<PreviewPart> previewParts = new ArrayList<PreviewPart>();
		NucletContentUID.Map uidLocalizedMap;

		Transfer t = new Transfer(isNuclon, bytes, newUserCount, parameter, root.exportOptions, previewParts);

		// TODO remove importData
		NucletContentMap importContentMap = new NucletContentHashMap();
		for (String sEntity : importData.keySet()) {
			NuclosEntity entity = NuclosEntity.getByName(sEntity);
			if (entity != null) {
				importContentMap.addAllValues(entity, importData.get(sEntity));
			}
		}

		DbAccess dbAccess = DataBaseHelper.getDbAccess();
		info("get all foreign key contraints and drop");
		final List<DbForeignKeyConstraint> fkConstraints = getForeignKeyConstraints(getEntities(contentTypes), new EntityObjectMetaDbHelper(dbAccess, MetaDataServerProvider.getInstance()));
		try {
			dbAccess.execute(SchemaUtils.drop(fkConstraints));
			Object savepoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
			info("delete content");
			deleteContent(existingNucletIds, uidExistingMap, uidImportMap, importContentMap, contentTypes, t, false,
				new TransferNotifierHelper(jmsNotifier, "prepare delete obsolete content", 30, 50));
			info("localize content");
			uidLocalizedMap = localizeContent(uidExistingMap, uidImportMap, importContentMap, contentTypes, t,
				new TransferNotifierHelper(jmsNotifier, "prepare localize content", 50, 55));
			info("localize new content for insert");
			localizeNewContentForInsert(importContentMap, uidLocalizedMap, contentTypes, true,
				new TransferNotifierHelper(jmsNotifier, "prepare localize new content for insert", 55, 60));
			info("insert or update content");
			insertOrUpdateContent(existingNucletIds, uidLocalizedMap, importContentMap, contentTypes, t, false,
				new TransferNotifierHelper(jmsNotifier, "prepare insert or update content", 60, 80));
			TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savepoint);
		} catch (Exception ex) {
			if (ex instanceof NuclosBusinessException)
				throw (NuclosBusinessException)ex;
			throw new NuclosFatalException(ex);
		} finally {
			info("recreate foreign key contraints");
			dbAccess.execute(SchemaUtils.create(fkConstraints));
		}

		try {
			info("preview changes");
			final String notifyPreviewString = "creating preview of db changes";
			jmsNotifier.notify(notifyPreviewString, 80);
			previewParts.addAll(previewChanges(DataBaseHelper.getDbAccess(), existingNucletIds, contentTypes, importData, root.exportOptions,
				new TransferNotifierHelper(jmsNotifier, notifyPreviewString, 80, 100)));
		} catch (Exception ex) {
			if (t.result.hasCriticals()) t.result.sbCritical.append("<br />");
			t.result.sbCritical.append("Preview of Changes impossible: ");
			t.result.sbCritical.append(ex.getMessage());
		}

		t.setImportData(importData);
		t.setExistingNucletIds(existingNucletIds);
		t.setUidExistingMap(uidExistingMap);
		t.setUidImportMap(uidImportMap);
		t.setUidLocalizedMap(uidLocalizedMap);
		jmsNotifier.notify("finished", 100);

		return t;
	}

	private MetaDataProvider getMetaDataProvider(List<EntityObjectVO> entities, List<EntityObjectVO> fields) {
		StaticMetaDataProvider result = new StaticMetaDataProvider();
		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			// only system entities...
			if (NuclosEntity.getByName(eMeta.getEntity()) != null || eMeta.getId() < 0) {
				result.addEntity(eMeta);
				result.addEntityFields(eMeta.getEntity(), MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values());
			}
		}

		for (EntityObjectVO eoEntity : entities) {
			EntityMetaDataVO eMeta = new EntityMetaDataVO(eoEntity);
			List<EntityFieldMetaDataVO> fieldsMeta = new ArrayList<EntityFieldMetaDataVO>();
			DalUtils.addNucletEOSystemFields(fieldsMeta, eMeta);

			result.addEntity(eMeta);
			result.addEntityFields(eMeta.getEntity(), fieldsMeta);
		}

		for (EntityObjectVO eoField : fields)
			result.addEntityField(new EntityFieldMetaDataVO(eoField));

		return result;
	}

	private List<PreviewPart> previewChanges(
		DbAccess dbAccess,
		Set<Long> existingNucletIds,
		List<INucletContent> contentTypes,
		Map<String, List<EntityObjectVO>> mpImportData,
		TransferOption.Map transferOptions,
		TransferNotifierHelper notifierHelper) throws SQLException {

		Map<String, PreviewPart> preview = new HashMap<String, PreviewPart>();

		List<EntityObjectVO> currentEntities = TransferUtils.getContentType(contentTypes, NuclosEntity.ENTITY).getNcObjects(existingNucletIds, transferOptions);
		List<EntityObjectVO> currentFields = TransferUtils.getContentType(contentTypes, NuclosEntity.ENTITYFIELD).getNcObjects(existingNucletIds, transferOptions);
		MetaDataProvider currentProvider = getMetaDataProvider(currentEntities, currentFields);
		Map<String, DbTable> currentSchema = (new EntityObjectMetaDbHelper(dbAccess, currentProvider)).getSchema();

		List<EntityObjectVO> transferredEntities = mpImportData.get(NuclosEntity.ENTITY.getEntityName());
		List<EntityObjectVO> transferredFields = mpImportData.get(NuclosEntity.ENTITYFIELD.getEntityName());
		MetaDataProvider transferredProvider = getMetaDataProvider(transferredEntities, transferredFields);
		Map<String, DbTable> transferredSchema = (new EntityObjectMetaDbHelper(dbAccess, transferredProvider)).getSchema();

		List<DbStructureChange> dbChangeStmts = SchemaUtils.modify(currentSchema.values(), transferredSchema.values());

		notifierHelper.setSteps(dbChangeStmts.size());
		for (DbStructureChange dbChangeStmt : dbChangeStmts) {
			notifierHelper.notifyNextStep();
			PreviewPart pp = new PreviewPart();

			MetaDataProvider provForEntityName = null;
			DbTable table = null;
			DbTableArtifact artifact = null;
			DbSimpleView view = null;
			switch (dbChangeStmt.getType()) {
			case CREATE:
				pp.setTypeOnlyOneTime(PreviewPart.NEW);
				if(dbChangeStmt.getArtifact2() instanceof DbUniqueConstraint) {
					DbUniqueConstraint dbu = (DbUniqueConstraint)dbChangeStmt.getArtifact2();
					checkIfNewUniqueConstraintIsAllowed(dbChangeStmt, pp);
					resetUniqueFields(transferredFields, transferredProvider, pp, dbu);
				}
			case MODIFY:
				pp.setTypeOnlyOneTime(PreviewPart.CHANGE);
				pp.setTable(dbChangeStmt.getArtifact2().getSimpleName());
				provForEntityName = transferredProvider;
				if (dbChangeStmt.getArtifact2() instanceof DbTableArtifact)
					artifact = (DbTableArtifact) dbChangeStmt.getArtifact2();
				if (dbChangeStmt.getArtifact2() instanceof DbTable)
					table = (DbTable) dbChangeStmt.getArtifact2();
				if (dbChangeStmt.getArtifact2() instanceof DbSimpleView)
					view = (DbSimpleView) dbChangeStmt.getArtifact2();
				break;
			case DROP:
				pp.setTypeOnlyOneTime(PreviewPart.DELETE);
				pp.setTable(dbChangeStmt.getArtifact1().getSimpleName());
				provForEntityName = MetaDataServerProvider.getInstance();
				if (dbChangeStmt.getArtifact1() instanceof DbTableArtifact)
					artifact = (DbTableArtifact) dbChangeStmt.getArtifact1();
				if (dbChangeStmt.getArtifact1() instanceof DbTable)
					table = (DbTable) dbChangeStmt.getArtifact1();
				if (dbChangeStmt.getArtifact1() instanceof DbSimpleView)
					view = (DbSimpleView) dbChangeStmt.getArtifact1();
				break;
			default:
				continue;
			}

			if (view != null) {
				// we want to see no views.
				continue;
			} else if (table != null) {
				if (pp.getType() == PreviewPart.CHANGE || pp.getType() == PreviewPart.DELETE) {
					String entity = getEntityNameFromTable(pp.getTable(), provForEntityName);
					if (entity != null)
						pp.setDataRecords(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).count(new CollectableSearchExpression()));
					else
						pp.setDataRecords(-1);
				}
				preview.put(pp.getTable().toUpperCase(), pp);
			} else if (artifact != null) {
				if (preview.get(artifact.getTableName().toUpperCase()) == null) {
					// table modified...
					pp.setType(PreviewPart.CHANGE);
					pp.setTable(artifact.getTableName());
					preview.put(artifact.getTableName().toUpperCase(), pp);
				} else {
					pp = preview.get(artifact.getTableName().toUpperCase());
				}
			} else {
				continue;
			}

			pp.setEntity(getEntityNameFromTable(pp.getTable(), provForEntityName));

			for (PreparedString ps : dbAccess.getPreparedSqlFor(dbChangeStmt)) {
				pp.addStatement(ps.toString());
			}
		}

		List<PreviewPart> result = new ArrayList<PreviewPart>();
		result.addAll(preview.values());
		return CollectionUtils.sorted(result, new Comparator<PreviewPart>() {
			@Override
            public int compare(PreviewPart o1, PreviewPart o2) {
	            return o1.toString().compareToIgnoreCase(o2.toString());
            }});
	}

	private void resetUniqueFields(List<EntityObjectVO> transferredFields,
		MetaDataProvider transferredProvider, PreviewPart pp,
		DbUniqueConstraint dbu) {
		if(pp.getWarning() > 0) {
			for(EntityMetaDataVO vo : transferredProvider.getAllEntities()) {
				String sTable = vo.getDbEntity();
				sTable = sTable.replaceFirst("V_", "T_");
				if(dbu.getTableName().equals(sTable)) {
					Integer iId = vo.getId().intValue();
					for(EntityObjectVO voField : transferredFields) {
						Integer entityId = voField.getFieldId("entity").intValue();
						if(entityId.equals(iId)) {
							EntityFieldMetaDataVO voFieldCurrent = MetaDataServerProvider.getInstance().getEntityField(vo.getEntity(), voField.getId());
							voField.getFields().put("unique", voFieldCurrent.isUnique());
						}
					}
				}
			}
		}
	}

	private void checkIfNewUniqueConstraintIsAllowed(DbStructureChange dbChangeStmt, PreviewPart pp) {
		try {
			DbUniqueConstraint artifactConstraint = (DbUniqueConstraint)dbChangeStmt.getArtifact2();
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Long> query = builder.createQuery(Long.class);
			DbFrom t = query.from(artifactConstraint.getTableName()).alias(SystemFields.BASE_ALIAS);
			List<DbExpression<?>> lstDBSelection = new ArrayList<DbExpression<?>>();
			for(String sColumn : artifactConstraint.getColumnNames()) {
				DbColumnExpression<?> c = t.baseColumn(sColumn, DalUtils.getDbType(artifactConstraint.getClass()));
				lstDBSelection.add(c);
			}

			query.select(builder.countRows());
			query.groupBy(lstDBSelection);
			query.having(builder.greaterThan(builder.countRows(), builder.literal(1L)));
			query.maxResults(2);

			List<Long> result = DataBaseHelper.getDbAccess().executeQuery(query);
			if(!result.isEmpty()) {
				pp.setWarning(pp.WARNING);
				pp.addStatement("Unique Constraint kann nicht gesetzt werden!");
			}
		}
		catch (Exception e) {
			LOG.warn("checkIfNewUniqueConstraintIsAllowed: " + e, e);
		}
	}

	private String getEntityNameFromTable(String table, MetaDataProvider prov) {
		for (EntityMetaDataVO eMeta : prov.getAllEntities())
			if (EntityObjectMetaDbHelper.getTableName(eMeta).equalsIgnoreCase(table))
				return eMeta.getEntity();
		return null;
	}

	private MetaDataRoot readBytes(byte[] bytes,
			Collection<EntityObjectVO> parameter,
			boolean protectParameter,
			@Deprecated Set<Long> nextId,
			Map<String, List<EntityObjectVO>> importData,
			NucletContentUID.Map uidImportMap) throws NuclosBusinessException {
		MetaDataRoot root = null;

		ZipInput zin = new ZipInput(new ByteArrayInputStream(bytes));
		try {
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				String name = ze.getName();
				if (ze.getSize() > Integer.MAX_VALUE)
					throw new IllegalArgumentException();
				if (ROOT_ENTRY_NAME.equals(name)) {
					String xml = zin.readStringEntry();
					root = (MetaDataRoot) fromXML(xml);
				} else if (UID.equals(name)) {
					String xml = zin.readStringEntry();
					if (uidImportMap != null) {
						uidImportMap.addAll((List<EntityObjectVO>) fromXML(xml));
					}
				} else if (name.endsWith(TABLE_ENTRY_SUFFIX)) {
					String xml = zin.readStringEntry();
					if (importData != null)
						importData.put(name.substring(0, name.length()-TABLE_ENTRY_SUFFIX.length()), (List<EntityObjectVO>) fromXML(xml));
				} else
					throw new IllegalArgumentException(name);

			}
		} finally {
			zin.close();
		}
		// sanity checks:
		if (root == null)
			throw new IllegalArgumentException("root data are missing");

		if (!TRANSFER_VERSION.equals(root.transferVersion)) {
			throw new NuclosBusinessException("Import of this Nuclet is impossible (Current Nuclos import version = " + TRANSFER_VERSION + " and Nuclet export version = " + root.transferVersion + ")");
		}

		if (!protectParameter){
			if (parameter != null)
				parameter.addAll(importData.get(NuclosEntity.PARAMETER.getEntityName()));
		}

		return root;
	}

	private String matchMetaDataRoot(MetaDataRoot root) {
		String error = null;
		ApplicationProperties p = ApplicationProperties.getInstance();
		if (!p.getNuclosVersion().equals(root.version)) {
			error = StringUtils.getParameterizedExceptionMessage("dbtransfer.problem.version.mismatch", root.version, root.version);
		}
		return error;
	}

	private static String toXML(Object o) {
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		return xstream.toXML(o);
	}

	private static Object fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		return xstream.fromXML(xml);
	}

	private MetaDataRoot buildMetaDataRoot(String nucletUID, TransferOption.Map exportOptions) {

		List<EntityObjectVO> nuclets = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLET).getAll();

		/*
		 * Workaround for Nuclet Name
		 */
		String nucletName = nuclets.isEmpty()?"NUCLOS":nuclets.get(0).getField("entity", String.class);

		return new MetaDataRoot(
			TRANSFER_VERSION,
			nucletUID,
			nucletName,
			ApplicationProperties.getInstance().getNuclosVersion(),
			getDatabaseType(),
			new Date(),
			exportOptions);
	}

	private static int getNewUserCount(List<EntityObjectVO> importUsers) {
		if (importUsers == null) {
			return 0;
		}

		Set<Integer> userIds = CollectionUtils.transformIntoSet(importUsers, new Transformer<EntityObjectVO, Integer>() {
			@Override
            public Integer transform(EntityObjectVO i) {
	            return LangUtils.convertId(i.getId());
            }});

		MasterDataFacadeRemote mdFacade =
			ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
		userIds.removeAll(mdFacade.getMasterDataIds(NuclosEntity.USER.getEntityName()));
		return userIds.size();
	}

	private List<DbForeignKeyConstraint> getForeignKeyConstraints(List<String> entities, EntityObjectMetaDbHelper helper) {
		List<DbForeignKeyConstraint> result = new ArrayList<DbForeignKeyConstraint>();
		for (String entity : entities) {
			result.addAll(helper.getDbTable(MetaDataServerProvider.getInstance().getEntity(entity)).getTableArtifacts(DbForeignKeyConstraint.class));
		}
		return result;
	}

	/**
	 * execute a transfer
	 *
	 * @param transfer
	 * @return a message object informing the client about success or failure
	 * @throws NuclosBusinessException (only for pre database changes)
	 */
	@Override
	public synchronized Transfer.Result runTransfer(final Transfer t) throws NuclosBusinessException {
		t.result = new Transfer.Result();

		info("RUN Transfer (isNuclon=" + t.isNuclon() + ")");
		LockedTabProgressNotifier jmsNotifier = new LockedTabProgressNotifier(Transfer.TOPIC_CORRELATIONID_RUN);
		info("get nuclet content instances");
		List<INucletContent> contentTypes = getNucletContentInstances(t.getTransferOptions());
		readBytes(t.getTransferFile(), t.getParameter(), true, null, new HashMap<String, List<EntityObjectVO>>(), null);

		info("get all dynamic entities");
		Collection<DynamicEntityVO> oldDynamicEntities = DatasourceCache.getInstance().getAllDynamicEntities();

		Map<String, String> config = DataBaseHelper.getDbAccess().getConfig();
		if (t.getTransferOptions().containsKey(TransferOption.DBADMIN)) {
			config.put(DbAccess.USERNAME, (String) t.getTransferOptions().get(TransferOption.DBADMIN));
		}
		if (t.getTransferOptions().containsKey(TransferOption.DBADMIN_PASSWORD)) {
			config.put(DbAccess.PASSWORD, (String) t.getTransferOptions().get(TransferOption.DBADMIN_PASSWORD));
		}
		info("create db access");
		DbAccess dbAccess = DataBaseHelper.getDbAccess().getDbType().createDbAccess(NuclosDataSources.getDefaultDS(), config);

		//** safe current configuration
		jmsNotifier.notify("read current schema", 0);
		info("read current schema");
		List<EntityObjectVO> currentEntities = TransferUtils.getContentType(contentTypes, NuclosEntity.ENTITY).getNcObjects(t.getExistingNucletIds(), t.getTransferOptions());
		List<EntityObjectVO> currentFields = TransferUtils.getContentType(contentTypes, NuclosEntity.ENTITYFIELD).getNcObjects(t.getExistingNucletIds(), t.getTransferOptions());
		MetaDataProvider currentProvider = getMetaDataProvider(currentEntities, currentFields);
		EntityObjectMetaDbHelper currentHelper = new EntityObjectMetaDbHelper(dbAccess, currentProvider);
		Map<String, DbTable> currentSchema = currentHelper.getSchema();

		//** safe current db objects
		DbObjectHelper dboHelper = new DbObjectHelper(dbAccess);
		Map<DbObject, Pair<DbStatement, DbStatement>> currentUserDefinedDbObjects = dboHelper.getAllDbObjects(null);


		// TODO remove importData
		NucletContentMap importContentMap = new NucletContentHashMap();
		for (String sEntity : t.getImportData().keySet()) {
			NuclosEntity entity = NuclosEntity.getByName(sEntity);
			if (entity != null) {
				importContentMap.addAllValues(entity, t.getImportData().get(sEntity));
			}
		}

		info("get all foreign key contraints and drop");
		final List<DbForeignKeyConstraint> fkConstraints = getForeignKeyConstraints(getEntities(contentTypes), new EntityObjectMetaDbHelper(dbAccess, MetaDataServerProvider.getInstance()));
		try {
			dbAccess.execute(SchemaUtils.drop(fkConstraints));
			// main config data transfer...
			info("delete content");
			deleteContent(t.getExistingNucletIds(), t.getUidExistingMap(), t.getUidImportMap(), importContentMap, contentTypes, t, false,
				new TransferNotifierHelper(jmsNotifier, "delete obsolete content", 10, 20));
			info("localize content");
			localizeNewContentForInsert(importContentMap, t.getUidLocalizedMap(), contentTypes, false,
				new TransferNotifierHelper(jmsNotifier, "localize content", 20, 30));
			info("insert or update content");
			insertOrUpdateContent(t.getExistingNucletIds(), t.getUidLocalizedMap(), importContentMap, contentTypes, t, false,
				new TransferNotifierHelper(jmsNotifier, "insert and update content", 30, 45));

			info("update parameter");
			jmsNotifier.notify("update parameter", 45);
			this.updateParameter(t.getParameter());

			info("generate new schema");
			jmsNotifier.notify("generate new schema", 50);
			MetaDataProvider transferredProvider = getMetaDataProvider(importContentMap.getValues(NuclosEntity.ENTITY), importContentMap.getValues(NuclosEntity.ENTITYFIELD));
			EntityObjectMetaDbHelper transferredHelper = new EntityObjectMetaDbHelper(dbAccess, transferredProvider);
			Map<String, DbTable> transferredSchema = transferredHelper.getSchema();

			//** new transferred database objects
			info("get all db objects");
			Map<DbObject, Pair<DbStatement, DbStatement>> transferredUserDefinedDbObjects = dboHelper.getAllDbObjects(null);

			info("update schema");
			jmsNotifier.notify("update schema", 60);
			//** update calculated attributes which are used by entities
			updateUserdefinedDbObjects(dbAccess, currentUserDefinedDbObjects, transferredUserDefinedDbObjects, DbObjectsPredicate.getOnlyUsedCalcAttr(transferredProvider), true, t.result.script, t.result.sbWarning);
			updateDB(dbAccess, currentSchema.values(), transferredSchema.values(), true, t.result.script, t.result.sbWarning);
			updateUserdefinedDbObjects(dbAccess, currentUserDefinedDbObjects, transferredUserDefinedDbObjects, DbObjectsPredicate.getAllWithoutUsedCalcAttr(transferredProvider), true, t.result.script, t.result.sbWarning);

			//** update schemacache after creating new tables, but before changing dynamic entities
			SchemaCache.getInstance().invalidate();
			DatasourceCache.getInstance().invalidate();
			//** update dynamic entities
			ServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class).processChangingDynamicEntities(
			DatasourceCache.getInstance().getAllDynamicEntities(), oldDynamicEntities, true, t.result.script);

			/** compile all custom code artifacts
			 */
			RuleCache.getInstance().invalidate();
			try {
				jmsNotifier.notify("compile rules", 80);
				NuclosJavaCompiler.compile();
			}
			catch(NuclosCompileException e) {
				LOG.info("runTransfer: " + e);
				t.result.sbWarning.append("\nError compiling rule: " + e.getMessage());
				if (e.getErrorMessages() != null) {
					for (ErrorMessage em : e.getErrorMessages()) {
						t.result.sbWarning.append("\n" + em.toString());
					}
				}
			}

			info("reload caches");
			jmsNotifier.notify("reload caches", 90);
			this.revalidateCaches();
			jmsNotifier.notify("finished", 100);
			return t.result;

		} catch (Exception ex) {
			if (ex instanceof NuclosBusinessException)
				throw (NuclosBusinessException)ex;
			throw new NuclosFatalException(ex);
		} finally {
			info("recreate foreign key contraints");
			dbAccess.execute(SchemaUtils.create(fkConstraints));
		}
	}

	private void revalidateCaches() {
		RuleCache.getInstance().invalidate();
		ResourceCache.getInstance().invalidate();
		ServerParameterProvider.getInstance().revalidate();
		ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).flushInternalCaches();

		MetaDataServerProvider.getInstance().revalidate();
		SecurityCache.getInstance().invalidate();

		StateCache.getInstance().invalidate();
		StateModelUsagesCache.getInstance().revalidate();

		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);
	}

	private void logDalCallResult(List<DalCallResult> dcrs, StringBuffer sbErrorMessage) {
		for (DalCallResult dcr : dcrs)
			logDalCallResult(dcr, sbErrorMessage);
	}

	private void logDalCallResult(DalCallResult dcr, StringBuffer sbErrorMessage) {
		if (dcr.hasException()) {
			for (DbException dbe : dcr.getExceptions()) {
				final List<String> statements;
				if (dbe instanceof DbException) {
					statements = ((DbException) dbe).getStatements();
				}
				else {
					statements = Collections.emptyList();
				}
				logDMLError(sbErrorMessage, dbe.getMessage(), statements);
			}
		}
	}

	private static class DbObjectsPredicate implements Predicate<DbObject>{

		private static final int MODE_ONLY_USED_CALC_ATTR = 1;
		private static final int MODE_ALL_WITHOUT_USED_CALC_ATTR = 2;

		private final int mode;
		private final MetaDataProvider metaProvider;

		private DbObjectsPredicate(int mode, MetaDataProvider metaProvider) {
			this.mode = mode;
			this.metaProvider = metaProvider;
		}

		public static DbObjectsPredicate getOnlyUsedCalcAttr(MetaDataProvider metaProvider) {
			return new DbObjectsPredicate(MODE_ONLY_USED_CALC_ATTR, metaProvider);
		}

		public static DbObjectsPredicate getAllWithoutUsedCalcAttr(MetaDataProvider metaProvider) {
			return new DbObjectsPredicate(MODE_ALL_WITHOUT_USED_CALC_ATTR, metaProvider);
		}

		@Override
        public boolean evaluate(DbObject dbObject) {
	        switch (mode) {
	        case MODE_ONLY_USED_CALC_ATTR:
	        	return dbObject.getType() == DbObjectType.FUNCTION && DbObjectHelper.isUsedAsCalculatedAttribute(dbObject.getName(), metaProvider);
	        case MODE_ALL_WITHOUT_USED_CALC_ATTR:
	        	return !(dbObject.getType() == DbObjectType.FUNCTION && DbObjectHelper.isUsedAsCalculatedAttribute(dbObject.getName(), metaProvider));
	        default:
	        	return false;
	        }
        }

	}

	/**
	 *
	 * @param dbAccess
	 * @param currentSchema
	 * @param transferredSchema
	 * @param script
	 * @param bExecuteDDL
	 * @param sbResultMessage
	 */
	private void updateDB(DbAccess dbAccess, Collection<DbTable> currentSchema, Collection<DbTable> transferredSchema,
			boolean bExecuteDDL, List<String> script, StringBuffer sbResultMessage) throws SQLException {
		for (DbStructureChange dbChangeStmt : SchemaUtils.modify(currentSchema, transferredSchema)) {
			logScript(script, dbAccess.getPreparedSqlFor(dbChangeStmt));
			if (bExecuteDDL)
				try {
					dbAccess.execute(dbChangeStmt);
				} catch (Exception ex) {
					logDDLError(sbResultMessage, ex.getMessage(), dbAccess.getPreparedSqlFor(dbChangeStmt));
				}
		}
	}

	/**
	 *
	 * @param dbAccess
	 * @param currentObjects
	 * @param transferredObjects
	 * @param dbObjectsPredicate
	 * @param script
	 * @param bExecuteDDL
	 * @param sbResultMessage
	 * @return
	 */
	private void updateUserdefinedDbObjects(DbAccess dbAccess,
		Map<DbObject, Pair<DbStatement, DbStatement>> currentObjects,
		Map<DbObject, Pair<DbStatement, DbStatement>> transferredObjects,
		DbObjectsPredicate dbObjectsPredicate,
		boolean bExecuteDDL, List<String> script, StringBuffer sbResultMessage) {

		for (DbObject dbObject : CollectionUtils.select(currentObjects.keySet(), dbObjectsPredicate)) {
			logScript(script, Collections.singletonList(currentObjects.get(dbObject).y));
			if (bExecuteDDL)
				try {
					dbAccess.execute(currentObjects.get(dbObject).y);
				} catch (Exception ex) {
					logDDLError(sbResultMessage, ex.getMessage(), Collections.singletonList(currentObjects.get(dbObject).y));
				}
		}

		for (DbObject dbObject : CollectionUtils.select(transferredObjects.keySet(), dbObjectsPredicate)) {
			logScript(script, Collections.singletonList(transferredObjects.get(dbObject).x));
			if (bExecuteDDL)
				try {
					dbAccess.execute(transferredObjects.get(dbObject).x);
				} catch (Exception ex) {
					logDDLError(sbResultMessage, ex.getMessage(), Collections.singletonList(transferredObjects.get(dbObject).x));
				}
		}
	}

	private void logDMLError(StringBuffer sbErrorMessage, String error, List<?> statements) {
		this.logError(sbErrorMessage, error, statements, "DML");
	}

	private void logDDLError(StringBuffer sbErrorMessage, String error, List<?> statements) {
		this.logError(sbErrorMessage, error, statements, "DDL");
	}

	private void logError(StringBuffer sbErrorMessage, String error, List<?> statements, String errorType) {
		sbErrorMessage.append("<br />-------------------- "+ errorType + " Error --------------------");
		sbErrorMessage.append("<br />" + error);
		if (statements == null)
			return;
		sbErrorMessage.append("<br />"+ errorType + " Statement(s) --------------------");
		for (Object sql : statements)
			sbErrorMessage.append("<br />" + sql);
	}

	private void logScript(List<String> script, List<?> statements) {
		for (Object sql : statements)
			if (sql != null && !StringUtils.looksEmpty(sql.toString()))
				script.add(sql.toString());
	}

	private NucletContentUID.Map localizeContent(
		NucletContentUID.Map uidExistingMap,
		NucletContentUID.Map uidImportMap,
		NucletContentMap importContentMap,
		List<INucletContent> contentTypes,
		Transfer t,
		TransferNotifierHelper notifierHelper) {

		NucletContentUID.Map result = new NucletContentUID.HashMap();
		Long id = -1l;

		notifierHelper.setSteps(contentTypes.size());
		NucletContentIdChanger idChanger = new NucletContentIdChanger(importContentMap);

		for (INucletContent nc : contentTypes) {
			info("localize content for nuclos entity: " + nc.getEntity());
			notifierHelper.notifyNextStep();
			for (EntityObjectVO importEO : importContentMap.getValues(nc.getEntity())) {
				info("import eo: " + getIdentifier(nc, importEO));
				NucletContentUID importUID = uidImportMap.getUID(importEO);
				info("import uid: " + importUID);
				if (importUID == null) {
					throw new NuclosFatalException("UID for import content not found");
				}

				boolean foundUID = false;

				if (!t.isNuclon()) {
					for (NucletContentUID.Key key : uidExistingMap.keySet()) {
						NucletContentUID existingUID = uidExistingMap.get(key);
						if (LangUtils.equals(existingUID.uid, importUID.uid)) {
							// check entity
							if (key.entity == nc.getEntity()) {
								info("uid found");
								if (foundUID) {
									throw new NuclosFatalException("Duplicate UID for entity \"" + key.entity + "\" found:\n" + existingUID.uid);
								}
								foundUID = true;
								info("change id: " + importEO.getId() + " --> " + key.id + " in import content map");
								idChanger.add(nc, importEO, key.id);
								result.put(key, existingUID);
							}
						}
					}
				} else {
					info("is nuclon import. do not localize anything");
				}

				if (!foundUID) {
					info("uid not found, change id: " + importEO.getId() + " --> " + id + " in import content map");
					idChanger.add(nc, importEO, id);
					result.put(new NucletContentUID.Key(nc.getEntity(), id), importUID.copy());
					id--;
				}
			}
		}

		idChanger.changeIds();

		return result;
	}

	private void localizeNewContentForInsert(
		NucletContentMap importContentMap,
		NucletContentUID.Map uidLocalizedMap,
		List<INucletContent> contentTypes,
		boolean testMode,
		TransferNotifierHelper notifierHelper) {

		if (importContentMap == null)
			return;

		notifierHelper.setSteps(contentTypes.size());
		NucletContentIdChanger idChanger = new NucletContentIdChanger(importContentMap);

		for (INucletContent nc : contentTypes) {
			info("localize new content for insert nuclos entity: " + nc.getEntity());
			notifierHelper.notifyNextStep();
			for (EntityObjectVO importEO : importContentMap.getValues(nc.getEntity())) {
				info("import eo: " + getIdentifier(nc, importEO));
				NucletContentUID.Key uidKey = new NucletContentUID.Key(nc.getEntity(), importEO.getId());
				info("uid key: " + uidKey);
				NucletContentUID uid = uidLocalizedMap.get(uidKey);
				info("localized uid: " + uid);
				if (uid == null) {
					throw new NuclosFatalException("UID for import content not found");
				}

				if (importEO.getId() < 0) {
					info("id < 0 --> flag new");
					importEO.flagNew();
					if (!testMode) {
						Long nextId = DalUtils.getNextId();
						info("new id: " + nextId);
						info("change id: " + importEO.getId() + " --> " + nextId + " in import content map");
						idChanger.add(nc, importEO, nextId);
						info("remove uid key from localized map");
						uidLocalizedMap.remove(uidKey);
						NucletContentUID.Key newKey = new NucletContentUID.Key(nc.getEntity(), nextId);
						info("add uid with new key: " + newKey + " to localized map");
						uidLocalizedMap.put(newKey, uid);
					}
				} else {
					info("id >= 0 --> flag for update");
					importEO.flagUpdate();
				}
			}
		}

		idChanger.changeIds();
	}

	private void insertOrUpdateContent(
		Set<Long> existingNucletIds,
		NucletContentUID.Map uidLocalizedMap,
		NucletContentMap importContentMap,
		List<INucletContent> contentTypes,
		Transfer t,
		boolean testMode,
		TransferNotifierHelper notifierHelper) {

		if (importContentMap == null)
			return;

		notifierHelper.setSteps(contentTypes.size());
		NucletContentMap contentSkipped = new NucletContentHashMap();
		Collection<NucletContentProcessor> contentProcessors = new ArrayList<NucletContentProcessor>();

		for (INucletContent nc : contentTypes) {
			info("insert or update content for nuclos entity: " + nc.getEntity());
			notifierHelper.notifyNextStep();
			if (!nc.isEnabled()) {
				info("nuclet content is disabled. adding all to untouched content map");
				contentSkipped.addAll(getDependencies(importContentMap, contentTypes, nc, null));
			} else {
				for (EntityObjectVO importEO : importContentMap.getValues(nc.getEntity())) {
					info("import eo: " + getIdentifier(nc, importEO));
					NucletContentUID uid = uidLocalizedMap.get(new NucletContentUID.Key(nc.getEntity(), importEO.getId()));
					info("localized uid: " + uid);

					NucletContentProcessor ncp = new NucletContentProcessor(nc, importEO);

					if (!contentSkipped.getValues(nc.getEntity()).contains(importEO)) {
						info("skipped content dos not contains import eo");
						if (importEO.isFlagNew()) {
							info("import eo is flagged new");
							if (!checkValidity(nc, importEO, ValidityType.INSERT, importContentMap, existingNucletIds, t.getTransferOptions(), t.result)) {
								info("validation for INSERT is false --> adding to skipped content (dependencies also)");
								contentSkipped.add(importEO);
								contentSkipped.addAll(getDependencies(importContentMap, contentTypes, nc, importEO));
								continue;
							}

							if (!t.isNuclon()) {
								ncp.createUIDRecord(uid);
							}
							info("--> add to processor");
							contentProcessors.add(ncp);
						} else {
							info("import eo is flagged for update");
							if (t.isNuclon())
								throw new IllegalArgumentException("update content for nuclon import");

							if (nc.canUpdate()) {
								if (!checkValidity(nc, importEO, ValidityType.UPDATE, importContentMap, existingNucletIds, t.getTransferOptions(), t.result)) {
									info("validation for UPDATE is false --> adding to skipped content (dependencies also)");
									contentSkipped.add(importEO);
									contentSkipped.addAll(getDependencies(importContentMap, contentTypes, nc, importEO));
									continue;
								}
//								if (!LangUtils.equals(uid.version, importEO.getVersion())) {
									ncp.updateUIDRecord(uid);
									info("--> add to processor");
									contentProcessors.add(ncp);
//								} else {
//									info("no version update --> adding to skipped content (dependencies also)");
//									contentSkipped.add(importEO);
//									contentSkipped.addAll(getDependencies(importContentMap, contentTypes, nc, importEO));
//									continue;
//								}
							} else {
								info("update of nuclet content is not allowed --> adding to skipped content (dependencies also)");
								contentSkipped.add(importEO);
								contentSkipped.addAll(getDependencies(importContentMap, contentTypes, nc, importEO));
								continue;
							}

						}
					}
				}
			}
		}

		info("process nuclet content");
		final DalCallResult result = new DalCallResult();
		for (NucletContentProcessor ncp : contentProcessors) {
			info(ncp.getEntity().getEntityName() + " import eo: " + getIdentifier(ncp.getNC(), ncp.getNcObject()));
			if (!contentSkipped.getValues(ncp.getEntity()).contains(ncp.getNcObject())) {
				info("skipped content dos not contains import eo");

				if (ncp.isCreateUID()) {
					info("is not nuclon --> store uid");
					if (!testMode) {
						try {
							createUIDRecord(ncp.getUID(), ncp.getEntity(), ncp.getNcObject().getId());
						}
						catch (DbException e) {
							result.addBusinessException(e);
						}
						// logDalCallResult(, t.result.sbWarning);
					}
				}

				else if (ncp.isUpdateUID()) {
					info("uid version \"" + ncp.getUID().version + "\" differs from import eo version \"" + ncp.getNcObject().getVersion() + "\" --> update version in existing uid");
					if (!testMode) {
						try {
							updateUIDRecord(ncp.getUID().id, ncp.getNcObject().getVersion());
						}
						catch (DbException e) {
							result.addBusinessException(e);
						}
						// logDalCallResult(, t.result.sbWarning);
					}

					Integer existingEOversion = getProcessor(ncp.getEntity()).getVersion(ncp.getNcObject().getId());
					if (!LangUtils.equals(ncp.getUID().version, existingEOversion)) {
						info("uid version \"" + ncp.getUID().version + "\" differs from existing object version \"" + existingEOversion + "\" --> add overwrite information for user");
						t.result.newWarningLine("Overwriting " + ncp.getEntity().getEntityName() + ": " + getIdentifier(ncp.getNC(), ncp.getNcObject()));
					}
				}

				info("insert or update");
				if (!testMode) {
					// no update of version information here
					ncp.getNC().insertOrUpdateNcObject(result, ncp.getNcObject(), t.isNuclon());
					// logDalCallResult(, t.result.sbWarning);
				}
			}
		}
		logDalCallResult(result, t.result.sbWarning);
	}

	private void deleteContent(
		Set<Long> existingNucletIds,
		NucletContentUID.Map uidExistingMap,
		NucletContentUID.Map uidImportMap,
		NucletContentMap importContentMap,
		List<INucletContent> contentTypes,
		Transfer t,
		boolean testMode,
		TransferNotifierHelper notifierHelper) {

		if (t.isNuclon()) {
			info("is nuclon import. do not delete anything");
			return;
		}

		notifierHelper.setSteps(contentTypes.size());
		NucletContentMap contentUntouched = new NucletContentHashMap();
		Collection<NucletContentProcessor> contentProcessors = new ArrayList<NucletContentProcessor>();

		List<INucletContent> contentTypesReversedOrder = new ArrayList<INucletContent>(contentTypes);
		Collections.reverse(contentTypesReversedOrder);
		for (INucletContent nc : contentTypesReversedOrder) {
			info("delete content for nuclos entity: " + nc.getEntity());
			notifierHelper.notifyNextStep();
			Collection<EntityFieldMetaDataVO> fieldDependencies = nc.getFieldDependencies();
			info("field dependencies: " + TransferUtils.getEntityFieldPresentations(fieldDependencies));
			// user entities referencing on
			Collection<EntityFieldMetaDataVO> userFieldDependencies = getUserEntityFields(nc.getFieldDependencies());
			info("user field denpendencies: " + TransferUtils.getEntityFieldPresentations(userFieldDependencies));

			List<EntityObjectVO> existingContent = nc.getNcObjects(existingNucletIds, t.getTransferOptions());
			if (!nc.isEnabled() || !nc.canDelete()) {
				info("nuclet content is disabled or delete is not allowed. adding all to untouched content map");
				contentUntouched.addAllValues(nc.getEntity(), existingContent);
			} else {
				for (EntityObjectVO existingEO : existingContent) {
					info("existing eo: " + getIdentifier(nc, existingEO));

					boolean delete = false;
					NucletContentUID existingUID = uidExistingMap.getUID(existingEO);
					info("existing uid: " + existingUID);

					if (t.getTransferOptions().containsKey(TransferOption.IS_NUCLOS_INSTANCE)) {
						if (existingUID == null) {
							info("existing uid does not exists and is nuclos instance import --> check if in use and validate");
						} else {
							contentUntouched.add(existingEO);
							continue;
						}
					} else {
						if ((existingUID != null && !uidImportMap.containsUID(existingUID, nc.getEntity()))) {
							info("existing uid exists but is not in uid import map --> check if in use and validate");
						} else {
							contentUntouched.add(existingEO);
							continue;
						}
					}

					boolean isInUse = false;
					// check if in use by user entity
					for (EntityFieldMetaDataVO efMeta : userFieldDependencies) {
						if (existsReference(efMeta, existingEO.getId())) {
							info("exists reference from user field dependence. entity id=" + efMeta.getEntityId() + " field=" + efMeta);

							// if entity could not be deleted but field is nullable set it null
							if (!nc.canDelete() && efMeta.isNullable()) {
								info("delete of nuclet content is not allowed but user field dependence is nullable --> set it null");
								final DalCallResult result = new DalCallResult();
								nc.setNcObjectFieldNull(result, existingEO.getId(), efMeta.getField());
								// TODO: What to do in case of errors?
								logDalCallResult(result, t.result.sbWarning);
							} else {
								Pair<String, String> ref = new Pair<String, String>(
									getEntity(efMeta),
									getIdentifier(nc, existingEO));
								info("log to user result: found reference " + ref);
								t.result.foundReferences.add(ref);

								info("existing eo --> is in use");
								isInUse = true;
							}
						}
					}

					if (isInUse || !checkValidity(nc, existingEO, ValidityType.DELETE, importContentMap, existingNucletIds, t.getTransferOptions(), t.result)) {
						info("is in use or validation is false --> add to untouched content");
						contentUntouched.add(existingEO);
					} else {
						info("not in use");
						delete = true;
					}

					if (delete) {
						info("--> add to processor");
						NucletContentProcessor ncp = new NucletContentProcessor(nc, existingEO);
						contentProcessors.add(ncp);
						if (existingUID != null) {
							ncp.deleteUIDRecord(existingUID);
						}
					} else {
						info("--> do not delete");
					}
				}
			}
		}

		info("check for references");
		int oldUntouchedContentSize;
		do {
			oldUntouchedContentSize = contentUntouched.getAllValues().size();
			NucletContentMap tmpMap = new NucletContentHashMap();

			// check if untouched content is referencing on
			for (NucletContentProcessor ncp : contentProcessors) {
				info("existing eo: " + getIdentifier(ncp.getNC(), ncp.getNcObject()));
				if (getIds(contentUntouched.getValues(ncp.getEntity())).contains(ncp.getNcObject().getId())) {
					info(ncp.getEntity().getEntityName() + " existing eo is already untouched");
					continue;
				}

				Collection<EntityFieldMetaDataVO> fieldDependencies = ncp.getNC().getFieldDependencies();
				for (NuclosEntity untouchedContentEntity : contentUntouched.keySet()) {
					for (EntityFieldMetaDataVO efMeta : fieldDependencies) {
						if (LangUtils.equals(untouchedContentEntity.getEntityName(), MetaDataServerProvider.getInstance().getEntity(efMeta.getEntityId()).getEntity())) {
							for (EntityObjectVO untouchedContent : contentUntouched.getValues(untouchedContentEntity)) {
								if (LangUtils.equals(untouchedContent.getFieldId(efMeta.getField()), ncp.getNcObject().getId())) {
									info("untouched content references on existing eo --> add existing eo to untouched content");
									tmpMap.add(ncp.getNcObject());
								}
							}
						}
					}
				}
			}
			contentUntouched.addAll(tmpMap);

		} while (oldUntouchedContentSize != contentUntouched.getAllValues().size());

		info("process nuclet content");
		final DalCallResult result = new DalCallResult();
		for (NucletContentProcessor ncp : contentProcessors) {
			info(ncp.getEntity().getEntityName() + " existing eo: " + getIdentifier(ncp.getNC(), ncp.getNcObject()));
			if (getIds(contentUntouched.getValues(ncp.getEntity())).contains(ncp.getNcObject().getId())) {
				info("existing is untouched");
			} else {
				info("--> delete existing eo");
				if (!testMode) {
					ncp.getNC().deleteNcObject(result, ncp.getNcObject());
				}
				if (ncp.isDeleteUID()) {
					info("delete existing uid");
					if (!testMode) {
						uidExistingMap.remove(uidExistingMap.getKey(ncp.getNcObject()));
						getProcessor(NuclosEntity.NUCLETCONTENTUID).delete(ncp.getUID().id);
					}
				}
			}
		}
		logDalCallResult(result, t.result.sbWarning);
	}

	private void cleanupUIDs() {
		info("cleanup uid's");
		for (EntityObjectVO uidObject : getProcessor(NuclosEntity.NUCLETCONTENTUID).getAll()) {
			if (!NuclosEntity.isNuclosEntity(uidObject.getField("nuclosentity", String.class)) || // maybe nuclos entity does not exists any more
				getProcessor(uidObject.getField("nuclosentity", String.class)).getByPrimaryKey(uidObject.getField("objectid", Long.class)) == null) {
				info("cleanup uid " + uidObject.getId() + " " + uidObject.getFields());
				getProcessor(NuclosEntity.NUCLETCONTENTUID).delete(uidObject.getId());
			}
		}
	}

	private boolean existsReference(EntityFieldMetaDataVO efMeta, Long idReferenceToCheck) {
		try {
			String sEntity = MetaDataServerProvider.getInstance().getEntity(efMeta.getEntityId()).getEntity();
			Integer count = getProcessor(sEntity).count(new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
				sEntity, efMeta.getField(),
				ComparisonOperator.EQUAL, idReferenceToCheck,
				MetaDataServerProvider.getInstance())));
			if (count > 0)
				return true;
		} catch (Exception e) {}

		return false;
	}

	@Override
	public String getDatabaseType(){
		return DataBaseHelper.getDbAccess().getDbType().toString();
	}

	private Boolean freezeConfiguration = null;

	private boolean freezeConfiguration() {
		if (freezeConfiguration == null)
			freezeConfiguration = readConfigParameter();
		return freezeConfiguration.booleanValue();
	}

	private static Boolean readConfigParameter() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_AD_PARAMETER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRVALUE", String.class));
		query.where(builder.equal(t.baseColumn("STRPARAMETER", String.class), PARAM_NAME));
		List<String> v = DataBaseHelper.getDbAccess().executeQuery(query);
		return (v.size() == 0) ? Boolean.FALSE : Boolean.valueOf(v.get(0));
	}

	private static final int PARAM_INTID = 40000275;
	private static final String PARAM_NAME = "Freeze configuration";
	private static final String PARAM_DESCRIPTION = PARAM_NAME;

	public synchronized void setFreezeConfiguration(boolean freeze) {
		if (freeze == freezeConfiguration()) return;

		DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_AD_PARAMETER",
			"INTID", PARAM_INTID));

		String user = getCurrentUserName();

		DataBaseHelper.execute(DbStatementUtils.insertInto("T_AD_PARAMETER",
			"INTID", PARAM_INTID,
			"STRPARAMETER", PARAM_NAME,
			"STRDESCRIPTION", PARAM_DESCRIPTION,
			"STRVALUE", (freeze ? "true" : "false"),
			"DATCREATED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCREATED", user,
			"DATCHANGED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCHANGED", user,
			"INTVERSION", 1));

		freezeConfiguration = Boolean.valueOf(freeze);

		// SecurityCache.getInstance().invalidate();
		// wird nicht ueber den Cache geprueft...
	}

	@Override
	@RolesAllowed("Login")
	public synchronized boolean isFrozenEntity(String entityName) {
		return false;
	}

	/**
	 *
	 * @param String nucletUID
	 * @return
	 */
	private Set<Long> getExistingNucletIds(String nucletUID) {
		Set<Long> result = new HashSet<Long>();
		if (nucletUID == null)
			return result;
		Long nucletId = getNcObjectIdFromNucletContentUID(NuclosEntity.NUCLET, nucletUID);
		if (nucletId == null)
			return result;
		result.addAll(getExistingNucletIds(nucletId));
		return result;
	}

	/**
	 *
	 * @param Long nucletId
	 * @return
	 */
	private Set<Long> getExistingNucletIds(Long nucletId) {
		Set<Long> result = new HashSet<Long>();
		if (nucletId != null) {
			for (Long dependenceNucletId : CollectionUtils.transformIntoSet(getProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
					NuclosEntity.NUCLETDEPENDENCE.getEntityName(), "nuclet",
					ComparisonOperator.EQUAL, nucletId,
					MetaDataServerProvider.getInstance()))),
				new TransferUtils.NucletDependenceTransformer())) {
				result.addAll(getExistingNucletIds(dependenceNucletId));
			}
			result.add(nucletId);
		}
		return result;
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @param String uid
	 * @return
	 */
	private Long getNcObjectIdFromNucletContentUID(NuclosEntity entity, String uid) {
		List<EntityObjectVO> result = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).getBySearchExpression(new CollectableSearchExpression(SearchConditionUtils.and(
			SearchConditionUtils.newEOComparison(
				NuclosEntity.NUCLETCONTENTUID.getEntityName(), "nuclosentity",
				ComparisonOperator.EQUAL, entity.getEntityName(),
				MetaDataServerProvider.getInstance()),
			SearchConditionUtils.newEOComparison(
				NuclosEntity.NUCLETCONTENTUID.getEntityName(), "uid",
				ComparisonOperator.EQUAL, uid,
				MetaDataServerProvider.getInstance()))));

		switch (result.size()) {
		case 0: return null;
		case 1: return result.get(0).getField("objectid", Long.class);
		default: throw new NuclosFatalException("Nuclet content UID is not unique [" + entity.getEntityName() + ", " + uid + "]");
		}
	}

	/**
	 *
	 * @param Set<Long> existingNucletIds
	 * @param List<INucletContent> contentTypes
	 * @return
	 */
	private NucletContentUID.Map getUIDMap(Set<Long> existingNucletIds, List<INucletContent> contentTypes, TransferOption.Map transferOptions) {
		NucletContentUID.Map result = new NucletContentUID.HashMap();
		for (INucletContent nc : contentTypes) {
			result.putAll(nc.getUIDMap(existingNucletIds, transferOptions));
		}
		return result;
	}

	/**
	 *
	 * @param Set<Long>existingNucletIds
	 * @param List<INucletContent> contentTypes
	 * @return
	 */
	private List<EntityObjectVO> getUIDObjects(Set<Long> existingNucletIds, List<INucletContent> contentTypes, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (INucletContent nc : contentTypes) {
			result.addAll(nc.getUIDObjects(existingNucletIds, transferOptions));
		}
		return result;
	}

	/**
	 *
	 * @param EntityObjectVO ncObject
	 * @return
	 */
	private EntityObjectVO createUIDRecordForNcObject(EntityObjectVO ncObject) {
		EntityObjectVO uidObject = createUIDObject(new NucletContentUID(ncObject), NuclosEntity.getByName(ncObject.getEntity()), ncObject.getId());
		getProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(uidObject);
		return uidObject;
	}

	/**
	 *
	 * @param NucletContentUID uid
	 * @param NuclosEntity entity
	 * @param Long objectId
	 * @param Integer objectVersion
	 * @return
	 */
	private void createUIDRecord(NucletContentUID uid, NuclosEntity entity, Long objectId) {
		getProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(createUIDObject(uid, entity, objectId));
	}

	/**
	 *
	 * @param NucletContentUID uid
	 * @param NuclosEntity entity
	 * @param Long objectId
	 * @return
	 */
	private EntityObjectVO createUIDObject(NucletContentUID uid, NuclosEntity entity, Long objectId) {
		if (uid == null) {
			throw new IllegalArgumentException("uid must not be null");
		}
		if (entity == null) {
			throw new IllegalArgumentException("entity must not be null");
		}
		if (objectId == null) {
			throw new IllegalArgumentException("objectId must not be null");
		}
		if (uid.uid == null) {
			throw new IllegalArgumentException("uid.uid must not be null");
		}
		if (uid.version == null) {
			throw new IllegalArgumentException("uid.version must not be null");
		}
		if (uid.id != null) {
			throw new IllegalArgumentException("uid.id != null");
		}
		EntityObjectVO result = new EntityObjectVO();
		result.setEntity(NuclosEntity.NUCLETCONTENTUID.getEntityName());
		result.initFields(4, 0);
		result.getFields().put("uid", uid.uid);
		result.getFields().put("nuclosentity", entity.getEntityName());
		result.getFields().put("objectid", objectId);
		result.getFields().put("objectversion", uid.version);
		result.flagNew();
		result.setId(DalUtils.getNextId());
		DalUtils.updateVersionInformation(result, getCurrentUserName());
		return result;
	}

	/**
	 *
	 * @param Long id
	 * @param Integer version
	 * @return
	 */
	private void updateUIDRecord(Long id, Integer version) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}
		if (version == null) {
			throw new IllegalArgumentException("version must not be null");
		}
		EntityObjectVO uidEO = getProcessor(NuclosEntity.NUCLETCONTENTUID).getByPrimaryKey(id);
		uidEO.getFields().put("objectversion", version);
		uidEO.flagUpdate();
		DalUtils.updateVersionInformation(uidEO, getCurrentUserName());
		getProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(uidEO);
	}

	/*
	 *
	 * @param newParameterVOs
	 * @throws NuclosBusinessException
	 */
	private void updateParameter(Collection<EntityObjectVO> newParameterVOs) throws NuclosBusinessException {
		ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);

		List<EntityObjectVO> existingParameterVOs = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PARAMETER).getAll();
		Set<String> existingParameter = new HashSet<String>();

		// aktualisere oder l\u00f6sche vorhandene Parameter
		for (EntityObjectVO existingParameterVO : existingParameterVOs) {
			String existingParameterName = existingParameterVO.getField("name", String.class);
			String existingParameterValue = existingParameterVO.getField("value", String.class);
			String existingParameterDescription = existingParameterVO.getField("description", String.class);

			existingParameter.add(existingParameterName);
			boolean removeParameter = true;

			for (EntityObjectVO newParameterVO : newParameterVOs) {
				String newParameterName = newParameterVO.getField("name", String.class);
				String newParameterValue = newParameterVO.getField("value", String.class);
				String newParameterDescription = newParameterVO.getField("description", String.class);

				if (newParameterName.equals(existingParameterName)) {
					// Parameter existiert, vergleiche...
					removeParameter = false;
					boolean modifyParameter = false;
					if ((existingParameterValue != null && !existingParameterValue.equals(newParameterValue))
						|| (existingParameterValue == null && newParameterValue != null)) {
						modifyParameter = true;
					}
					if ((existingParameterDescription != null && !existingParameterDescription.equals(newParameterDescription))
						|| (existingParameterDescription == null && newParameterDescription != null)) {
						modifyParameter = true;
					}
					if (modifyParameter) {
						// Parameter aktualisieren
						existingParameterVO.getFields().put("value", newParameterValue);
						existingParameterVO.getFields().put("description", newParameterDescription);

						existingParameterVO.flagUpdate();
						DalUtils.updateVersionInformation(existingParameterVO, getCurrentUserName());
						NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PARAMETER).insertOrUpdate(existingParameterVO);
					}
				}
			}
			if (removeParameter) {
				NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PARAMETER).delete(existingParameterVO.getId());
			}
		}

		// f\u00fcge neue Parameter hinzu
		for (EntityObjectVO newParameterVO : newParameterVOs) {
			String newParameterName = newParameterVO.getField("name", String.class);
			String newParameterValue = newParameterVO.getField("value", String.class);
			String newParameterDescription = newParameterVO.getField("description", String.class);

			if (!existingParameter.contains(newParameterName)) {
				EntityObjectVO createParameterVO = new EntityObjectVO();
				createParameterVO.initFields(3, 0);
				createParameterVO.getFields().put("name", newParameterName);
				createParameterVO.getFields().put("value", newParameterValue);
				createParameterVO.getFields().put("description", newParameterDescription);

				createParameterVO.flagNew();
				createParameterVO.setId(DalUtils.getNextId());
				DalUtils.updateVersionInformation(createParameterVO, getCurrentUserName());
				NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.PARAMETER).insertOrUpdate(createParameterVO);
			}
		}
	}

	/**
	 *
	 * @param nucletId
	 * @throws NuclosBusinessException
	 */
	@Override
	public void checkCircularReference(Long nucletId) throws CommonValidationException {
		CollectableSearchCondition clctcondUP = SearchConditionUtils.newEOidComparison(
			NuclosEntity.NUCLETDEPENDENCE.getEntityName(),
			"nucletDependence",
			ComparisonOperator.EQUAL,
			nucletId,
			MetaDataServerProvider.getInstance());
		CollectableSearchCondition clctcondDOWN = SearchConditionUtils.newEOidComparison(
			NuclosEntity.NUCLETDEPENDENCE.getEntityName(),
			"nuclet",
			ComparisonOperator.EQUAL,
			nucletId,
			MetaDataServerProvider.getInstance());
		if (checkCircularReferenceUp(nucletId, getProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(new CollectableSearchExpression(clctcondUP))) ||
			checkCircularReferenceDown(nucletId, getProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(new CollectableSearchExpression(clctcondDOWN)))) {
			throw new CommonValidationException("nuclet.circular.reference.found");
		}

	}

	private boolean checkCircularReferenceUp(Long nucletId, Collection<EntityObjectVO> nucletDependences) {
		for (EntityObjectVO nucletDependence : nucletDependences) {
			if (LangUtils.equals(nucletId, nucletDependence.getFieldId("nuclet"))) {
				return true;
			}

			CollectableSearchCondition clctcond = SearchConditionUtils.newEOidComparison(
				NuclosEntity.NUCLETDEPENDENCE.getEntityName(),
				"nucletDependence",
				ComparisonOperator.EQUAL,
				nucletDependence.getFieldId("nuclet"),
				MetaDataServerProvider.getInstance());
			return checkCircularReferenceUp(nucletId, getProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(new CollectableSearchExpression(clctcond)));
		}

		return false;
	}

	private boolean checkCircularReferenceDown(Long nucletId, Collection<EntityObjectVO> nucletDependences) {
		for (EntityObjectVO nucletDependence : nucletDependences) {
			if (LangUtils.equals(nucletId, nucletDependence.getFieldId("nucletDependence"))) {
				return true;
			}

			CollectableSearchCondition clctcond = SearchConditionUtils.newEOidComparison(
				NuclosEntity.NUCLETDEPENDENCE.getEntityName(),
				"nuclet",
				ComparisonOperator.EQUAL,
				nucletDependence.getFieldId("nucletDependence"),
				MetaDataServerProvider.getInstance());
			return checkCircularReferenceDown(nucletId, getProcessor(NuclosEntity.NUCLETDEPENDENCE).getBySearchExpression(new CollectableSearchExpression(clctcond)));
		}

		return false;
	}

}

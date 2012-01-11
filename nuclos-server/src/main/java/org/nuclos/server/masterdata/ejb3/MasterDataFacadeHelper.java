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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.transport.GzipList;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.TruncatableCollectionDecorator;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeLocal;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.NuclosPerformanceLogger;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbObjectHelper;
import org.nuclos.server.dblayer.DbObjectHelper.DbObjectType;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbStructureChange.Type;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.resource.ResourceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for the MasterDataFacade.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
@Component
public class MasterDataFacadeHelper {
	
	private static final Logger LOG = Logger.getLogger(MasterDataFacadeHelper.class);

	private static final List<String> lstSystemDbFieldNames =
			Arrays.asList("intid", "datcreated", "strcreated", "datchanged", "strchanged", "intversion");

	//final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_MASTERDATACACHE);

	static final int MAXROWS = 100;

	public static enum RoleDependant {

		ROLE_ACTION(NuclosEntity.ROLEACTION, "22292", "action", null),
		ROLE_MODULE(NuclosEntity.ROLEMODULE, "22293", "module", "group"),
		ROLE_MASTERDATA(NuclosEntity.ROLEMASTERDATA, "22294", "entity", null),
		ROLE_USER(NuclosEntity.ROLEUSER, "22290", "user", null),
		ROLE_REPORT(NuclosEntity.ROLEREPORT, "22295", "report", null);

		private final NuclosEntity entity;
		private final String resourceId;
		private final String entityFieldName;
		private final String subFieldName;

		private RoleDependant(NuclosEntity entity, String resourceId, String entityFieldName, String subFieldName) {
			this.entity = entity;
			this.resourceId = resourceId;
			this.entityFieldName = entityFieldName;
			this.subFieldName = subFieldName;
		}

		public NuclosEntity getEntity() {
			return entity;
		}

		public String getResourceId() {
			return resourceId;
		}

		public String getEntityFieldName() {
			return entityFieldName;
		}

		public String getSubFieldName() {
			return subFieldName;
		}

		public static RoleDependant getByEntityName(String entityName) {
	      for(RoleDependant u : RoleDependant.class.getEnumConstants())
	         if(u.getEntity().checkEntityName(entityName))
	            return u;
	      return null;
	   }
	}
	
	//
	
	private AttributeFacadeLocal attributeFacade;

	private RecordGrantUtils grantUtils;
	
	public MasterDataFacadeHelper() {
	}
	
	@Autowired
	void setRecordGrantUtils(RecordGrantUtils grantUtils) {
		this.grantUtils = grantUtils;
	}

	void close() {
	}

	void notifyClients(String sCachedEntityName) {
		if (sCachedEntityName == null) {
			throw new NullArgumentException("sCachedEntityName");
		}
		NuclosJMSUtils.sendMessage(sCachedEntityName, JMSConstants.TOPICNAME_MASTERDATACACHE);
	}

	public MasterDataVO getMasterDataCVOById(final MasterDataMetaVO mdmetavo, final Object oId) throws CommonFinderException {
		MasterDataVO mdVO = XMLEntities.getSystemObjectById(mdmetavo.getEntityName(), oId);
		if (mdVO != null) {
			return mdVO;
		}

		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(mdmetavo.getEntityName());
		EntityObjectVO eoResult = eoProcessor.getByPrimaryKey(IdUtils.toLongId(oId));
		try {
			grantUtils.checkInternal(mdmetavo.getEntityName(), IdUtils.toLongId(oId));
        }
        catch(CommonPermissionException e) {
        	throw new CommonFinderException(e);
        }
		if (eoResult != null) {
			mdVO = DalSupportForMD.wrapEntityObjectVO(eoResult);
		}

		if (mdVO == null) {
			throw new CommonFinderException();
		}

		return mdVO;
	}

	/**
	 * gets the dependant master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param sEntityName name of the entity to get all dependant master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	public Collection<EntityObjectVO> getDependantMasterData(String sEntityName, String sForeignKeyField, Object oRelatedId, String username) {
		if (oRelatedId == null) {
			throw new NullArgumentException("oRelatedId");
		}
		LOG.debug("Getting dependant masterdata for entity " + sEntityName + " with foreign key field " + sForeignKeyField + " and related id " + oRelatedId);

		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sEntityName);
		Date startDate = new Date();

		Collection<MasterDataVO> result = mdmetavo.isDynamic() ?
				getDependantMasterDataBySQL(oRelatedId, mdmetavo) :
					getDependantMasterDataByBean(sEntityName, sForeignKeyField, oRelatedId);

		Collection<EntityObjectVO> colEntityObject = CollectionUtils.transform(result, new MasterDataToEntityObjectTransformer());

		Date endate = new Date();
		NuclosPerformanceLogger.performanceLog(
				startDate.getTime(),
				endate.getTime(),
				username,
				oRelatedId,
				sEntityName,
				"Reading the master data entity for an objekt if type "+sEntityName+" ("+(mdmetavo.isDynamic() ? " dynamic " : " static ") +")",
				"",
				"");

		return colEntityObject;
	}

	Collection<MasterDataVO> getDependantMasterDataByBean(String sEntityName, String sForeignKeyFieldName, Object oRelatedId) {
		final CollectableEntityField clctef = SearchConditionUtils.newMasterDataEntityField(MasterDataMetaCache.getInstance().getMetaData(sEntityName), sForeignKeyFieldName);
		final CollectableSearchCondition cond = new CollectableComparison(clctef, ComparisonOperator.EQUAL, new CollectableValueIdField(oRelatedId, null));
		return getGenericMasterData(sEntityName, cond, true);
	}

	static Collection<MasterDataVO> getDependantMasterDataBySQL(Object oRelatedId, final MasterDataMetaVO mdmetavo) {
		final List<MasterDataMetaFieldVO> collFields = mdmetavo.getFields();
		final int fieldCount = collFields.size();

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from(mdmetavo.getDBEntity()).alias(SystemFields.BASE_ALIAS);
		DbColumnExpression<Integer> goColumn = t.baseColumn("INTID_T_UD_GENERICOBJECT", Integer.class);
		List<DbSelection<?>> selection = new ArrayList<DbSelection<?>>();
		for (MasterDataMetaFieldVO field : collFields) {
			String fieldName = field.getFieldName();
			if (fieldName.equals(ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME)) {
				selection.add(goColumn);
			} else {
				Class<?> javaType = DalUtils.getDbType(field.getJavaClass());
				selection.add(t.baseColumnCaseSensitive(fieldName, javaType, false));
			}
		}
		selection.add(t.baseColumn("INTID", Integer.class));
		query.multiselect(selection);
		query.where(builder.equal(goColumn, oRelatedId));

		return DataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, MasterDataVO>() {
			@Override
            public MasterDataVO transform(DbTuple tuple) {
				MasterDataVO result = new MasterDataVO(mdmetavo, false);
				result.setId(tuple.get(fieldCount, Integer.class));
				for (int i = 0; i < fieldCount; i++) {
					MasterDataMetaFieldVO field = collFields.get(i);
					String fieldName = field.getFieldName();
					Object value = tuple.get(i);
					if (fieldName.equals(ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME)) {
						result.setField(fieldName + "Id", value);
					} else {
						result.setField(fieldName, value);
					}
				}
				return result;
			};
		});
	}

	static String getProtocolValue(Object oValue) {
		String result = "";
		if (oValue != null) {
			if (oValue instanceof Date) {
				result = new SimpleDateFormat("dd.MM.yyyy").format(((Date) oValue));
			}
			else {
				result = oValue.toString();
			}
		}
		return result;
	}

	static void invalidateCaches(String sEntityName, MasterDataVO mdvo) {
		NuclosEntity nuclosEntity = NuclosEntity.getByName(sEntityName);
		if (nuclosEntity != null) {
			switch (nuclosEntity) {
			case ROLE:
			case ACTION:
			case REPORT:
				SecurityCache.getInstance().invalidate();
				break;
			case USER:
				SecurityCache.getInstance().invalidate(mdvo.getField("name", String.class));
				break;
			case LAYOUT:
				MetaDataServerProvider.getInstance().revalidate();
				break;
			case LAYOUTUSAGE:
				MetaDataServerProvider.getInstance().revalidate();
				break;
			case RESOURCE:
				ResourceCache.getInstance().invalidate();
				break;
			case PARAMETER:
				ServerParameterProvider.getInstance().revalidate();
				break;
			}
		}
	}

// @todo unify validation mechanisms
	private static void validateCVO(String sEntityName, MasterDataVO mdvo) throws CommonValidationException {
		mdvo.validate(MasterDataMetaCache.getInstance().getMetaData(sEntityName));
	}

	/**
	 * Called after an entity was changed - that is, a row was inserted, updated or deleted.
	 * @param mdmetavo the entity that was changed.
	 * @param mdvoChanged the object that was inserted, updated or deleted.
	 */
	private void entityChanged(MasterDataMetaVO mdmetavo, MasterDataVO mdvoChanged) {
		NuclosEntity nuclosEntity = NuclosEntity.getByName(mdmetavo.getEntityName());


		if (nuclosEntity == NuclosEntity.ROLEUSER) {
			// Rights are reloaded the next time the user logs in - we don't need to do anything here
			// @todo To make these changes visible immediately, however, we could notify the client and change the roles dynamically in the server.
			// But can we do that in a J2EE conformant way?
//			NucleusSecurityProxy.invalidateMethodRightsForUser(mdvoChanged.getField("user", String.class));
		}
		else if (nuclosEntity == NuclosEntity.ROLEACTION) {
//			NucleusSecurityProxy.invalidateMethodRightsForAllUsers();
		}

		if (mdmetavo.isCacheable()) {
			this.notifyClients(mdmetavo.getEntityName());
		}
	}

	/**
	 * Returns the name of the user-writable
	 * @param mdmetavo
	 * @return
	 */
	public static String getUserWritableDbEntityName(MasterDataMetaVO mdmetavo) {
		String table = mdmetavo.getDBEntity();
		return table.startsWith("V_") ? "T_" + table.substring(2) : table;
	}

	/**
	 * @param mdmetavo
	 * @return the names of all user writable database fields.
	 */
	public static List<String> getUserWritableDbFieldNames(MasterDataMetaVO mdmetavo) {
		final List<String> result = CollectionUtils.transform(mdmetavo.getFields(), new Transformer<MasterDataMetaFieldVO, String>() {
			@Override
            public String transform(MasterDataMetaFieldVO mdmetafieldvo) {
				return (mdmetafieldvo.getForeignEntity() == null) ? mdmetafieldvo.getDBFieldName() : mdmetafieldvo.getDBIdFieldName();
			}
		});
		// remove all system fields from the result:
		for (Iterator<String> iter = result.iterator(); iter.hasNext();) {
			if (lstSystemDbFieldNames.contains(iter.next().toLowerCase())) {
				iter.remove();
			}
		}
		return result;
	}

	/**
	 * performs a stale version check.
	 * @param mdvo
	 * @throws CommonStaleVersionException
	 */
	MasterDataVO checkForStaleVersion(MasterDataMetaVO mdMetaVO, MasterDataVO mdvo) throws CommonStaleVersionException, CommonPermissionException, CommonFinderException {
		final MasterDataVO mdvoInDataBase = getMasterDataCVOById(mdMetaVO, mdvo.getIntId());
		if (mdvo.getVersion() != mdvoInDataBase.getVersion()) {
			throw new CommonStaleVersionException();
		}
		if (mdvo.isSystemRecord()) {
			throw new CommonPermissionException();
		}

		return mdvoInDataBase;
	}

	static void checkInvariantFields(MasterDataMetaVO mdMetaVO, MasterDataVO mdvo, MasterDataVO mdvoInDataBase) throws CommonValidationException {
		for (MasterDataMetaFieldVO mdMetaFieldVO : mdMetaVO.getInvariantFields()) {
			String fieldName = mdMetaFieldVO.getFieldName();
			if (!ObjectUtils.equals(mdvo.getField(fieldName), mdvoInDataBase.getField(fieldName))) {
				// TODO_AUTOSYNC: translation
				throw new CommonValidationException(MessageFormat.format("Field \"{0}\" cannot be changed because it is declared as invariant", fieldName));
			}
		}
	}

	/**
	 * removes a single masterdata row.
	 * @param sEntityName
	 * @param mdvo
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 * @precondition sEntityName != null
	 */
	void removeSingleRow(String sEntityName, final MasterDataVO mdvo)
			throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException {

		if (sEntityName == null) {
			throw new NullArgumentException("sEntityName");
		}

		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sEntityName);

		// prevent removal if dependant dynamic attributes exist:
		final Object oExternalId = mdvo.getId();
		if (oExternalId == null) {
			throw new NuclosFatalException("mdhelper.error.invalid.id");//"Der Datensatz hat eine leere Id.");
		}

		checkForStaleVersion(mdmetavo, mdvo);


		// @todo refactor: make this easier to write:
		try {
			DataBaseHelper.execute(DbStatementUtils.deleteFrom(getUserWritableDbEntityName(mdmetavo),
				"INTID", mdvo.getIntId()));
		}
		catch (CommonFatalException ex) {
			throw new CommonRemoveException(ex);
		}
		//remove documents

		for (MasterDataMetaFieldVO field : MasterDataMetaCache.getInstance().getMetaData(sEntityName).getFields()) {
			if(field.getJavaClass().equals(GenericObjectDocumentFile.class)) {
				String sExtendedPath = "";
				if (sEntityName.equals("nuclos_" + NuclosEntity.GENERALSEARCHDOCUMENT)) {
					GenericObjectDocumentFile docFile = (GenericObjectDocumentFile)mdvo.getField("file");
					sExtendedPath = StringUtils.emptyIfNull(docFile.getDirectoryPath());
				}
				File file = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH) + "/" + sExtendedPath);
				remove(mdvo.getIntId(), null, NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
			}
		}

		NuclosEntity nucEntity = NuclosEntity.getByName(sEntityName);
		if (nucEntity != null)
			switch (nucEntity) {
			case DBSOURCE:
				try {
		            updateDbObject(DalSupportForMD.getEntityObjectVO(mdvo), null, false);
	            }
	            catch(NuclosBusinessException e) {
		            throw new CommonRemoveException(e.getMessage(), e);
	            }
	            break;
			case DBOBJECT:
				for (EntityObjectVO source : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.DBSOURCE).getBySearchExpression(
					appendRecordGrants(new CollectableSearchExpression(
						SearchConditionUtils.newEOComparison(NuclosEntity.DBSOURCE.getEntityName(), "dbobject", ComparisonOperator.EQUAL, mdvo.getField("name"), MetaDataServerProvider.getInstance())
						), sEntityName)
					))
					this.removeSingleRow(NuclosEntity.DBSOURCE.getEntityName(), DalSupportForMD.wrapEntityObjectVO(source));
				break;
			}

		this.entityChanged(mdmetavo, mdvo);
	}

	/**
	 * modifies a single masterdata row.
	 * @param sEntityName
	 * @param mdvo
	 * @param sUserName
	 * @param bValidate
	 * @return
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	Object modifySingleRow(String sEntityName, MasterDataVO mdvo, String sUserName, boolean bValidate)
			throws CommonCreateException, CommonFinderException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {

		if (bValidate) {
			validateCVO(sEntityName, mdvo);
		}

		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sEntityName);

		final MasterDataVO mdvoInDB = checkForStaleVersion(mdmetavo, mdvo);

		checkInvariantFields(mdmetavo, mdvo, mdvoInDB);

		validateUniqueConstraintWithJson(mdmetavo, mdvo);

		if(NuclosEntity.USER.getEntityName().equals(sEntityName)
			&& sUserName.equalsIgnoreCase(mdvoInDB.getField("name", String.class))
			&& !mdvoInDB.getField("name", String.class).equalsIgnoreCase(mdvo.getField("name", String.class))) {
			throw new CommonPermissionException("masterdata.error.change.own.user.name");
		}

		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(mdmetavo.getEntityName());
		EntityObjectVO eoVO = DalSupportForMD.getEntityObjectVO(mdvo);
		DalUtils.updateVersionInformation(eoVO, sUserName);
		eoVO.flagUpdate();

		try {
			eoProcessor.insertOrUpdate(eoVO);
		} catch (DbException e) {
			throw new CommonCreateException(e.getMessage(), e);
		}

		storeFiles(sEntityName, eoVO);

		NuclosEntity nucEntity = NuclosEntity.getByName(sEntityName);
		if (nucEntity != null)
			switch (nucEntity) {
			case DBSOURCE:
				try {
		            updateDbObject(DalSupportForMD.getEntityObjectVO(mdvoInDB), eoVO, false);
	            }
	            catch(NuclosBusinessException e) {
		            throw new CommonCreateException(e.getMessage(), e);
	            }
	            break;
			}

		this.entityChanged(mdmetavo, mdvo);

		return mdvo.getId();
	}

	/**
	 * creates a single masterdata row.
	 * @param sEntityName
	 * @param mdvoToCreate
	 * @param sUserName
	 * @param bValidate
	 * @return the new id of the created row
	 * @precondition mdvo.getId() == null
	 */
	Integer createSingleRow(String sEntityName, MasterDataVO mdvoToCreate, String sUserName, boolean bValidate, Integer intid) throws
		CommonCreateException, CommonValidationException {
		if (mdvoToCreate.getId() != null) {
			throw new IllegalArgumentException("mdvoToCreate.getId()");
		}

		if (bValidate) {
			validateCVO(sEntityName, mdvoToCreate);
		}

		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sEntityName);
		final EntityMetaDataVO entityMeta = MetaDataServerProvider.getInstance().getEntity(sEntityName);

		validateUniqueConstraintWithJson(mdmetavo, mdvoToCreate);

		// @todo optimize: use idfactory.nextval for insert

		final Integer result;
		if (intid != null) {
			result = intid;
		} else {
			final String idFactory = entityMeta.getIdFactory();
			if (idFactory == null) {
				result = DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);
			} else {
				result = DataBaseHelper.getDbAccess().executeFunction(idFactory, Integer.class);
			}
		}
		mdvoToCreate.setId(result);

		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(mdmetavo.getEntityName());
		EntityObjectVO eoVO = DalSupportForMD.getEntityObjectVO(mdvoToCreate);
		DalUtils.updateVersionInformation(eoVO, sUserName);
		eoVO.flagNew();

		try {
			eoProcessor.insertOrUpdate(eoVO);
		} catch (DbException e) {
			throw new CommonCreateException(e.toString());
		}

		storeFiles(sEntityName, eoVO);

		NuclosEntity nucEntity = NuclosEntity.getByName(sEntityName);
		if (nucEntity != null)
			switch (nucEntity) {
			case DBSOURCE:
				try {
		            updateDbObject(null, eoVO, false);
	            }
	            catch(NuclosBusinessException e) {
		            throw new CommonCreateException(e.getMessage(), e);
	            }
	            break;
			}

		this.entityChanged(mdmetavo, mdvoToCreate);

		return result;
	}

	private void validateUniqueConstraintWithJson(MasterDataMetaVO mdmetavo, MasterDataVO mdvoToCreate) throws CommonValidationException {
		if (!XMLEntities.hasSystemData(mdmetavo.getEntityName())) {
			return;
		}
		CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		for (MasterDataMetaFieldVO field : mdmetavo.getFields()) {
			if (field.isUnique()) {
				String fieldname = field.getFieldName();
				if (mdvoToCreate.getField(fieldname) != null) {
					if (field.getForeignEntity() != null) {
						cond.addOperand(SearchConditionUtils.newMDReferenceComparison(mdmetavo, fieldname, mdvoToCreate.getField(fieldname + "Id", Integer.class)));
					}
					else {
						cond.addOperand(SearchConditionUtils.newMDComparison(mdmetavo, field.getFieldName(), ComparisonOperator.EQUAL, mdvoToCreate.getField(field.getFieldName())));
					}
				}
				else {
					cond.addOperand(SearchConditionUtils.newMDIsNullCondition(mdmetavo, fieldname));
				}
			}
		}

		if (cond.getOperandCount() > 0) {
			final Collection<MasterDataVO> systemObjects = XMLEntities.getSystemObjects(mdmetavo.getEntityName(), cond);
			if (!systemObjects.isEmpty()) {
				throw new CommonValidationException("nuclos.validation.systementity.unique");
			}
		}
	}

	/**
	 *
	 * @param eoVO
	 */
	public static void storeFiles(String sEntity, EntityObjectVO eoVO) {
		for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sEntity).values()) {
			final Object oValue = eoVO.getFields().get(efMeta.getField());
			final String sClzz = efMeta.getDataType();

			if (GenericObjectDocumentFile.class.getName().equals(sClzz)) {
				GenericObjectDocumentFile documentFile = (GenericObjectDocumentFile) oValue;
				if(documentFile != null) {
					if(documentFile.getContents() != null) {
					  storeFile(new GenericObjectDocumentFile(
						   documentFile.getFilename(),
						   IdUtils.unsafeToId(eoVO.getId()),
						   documentFile.getContents(),
						   documentFile.getDirectoryPath()),
					  		NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
					}
				}
			}
		}
	}

	/**
	 * removes the given dependants.
	 * @param mpDependants
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 */
	public void removeDependants(DependantMasterDataMap mpDependants)
			throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException {
		for (String sDependantEntityName : mpDependants.getEntityNames()) {
			if (!MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName).isDynamic()
				&& !MetaDataServerProvider.getInstance().getEntity(sDependantEntityName).isStateModel()) {
				for (EntityObjectVO mdvoDependant : mpDependants.getData(sDependantEntityName)) {

					removeDependants(mdvoDependant.getDependants());
					if(MetaDataServerProvider.getInstance().getEntity(sDependantEntityName).isStateModel()) {
						try {
							mdvoDependant.setEntity(sDependantEntityName);
							GenericObjectVO govo = DalSupportForGO.getGenericObjectVO(mdvoDependant);
							GenericObjectFacadeLocal goLocal = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
							goLocal.remove(new GenericObjectWithDependantsVO(govo, mdvoDependant.getDependants()), true);
						}
						catch(CommonCreateException ex) {
							throw new NuclosFatalException(ex);
						}
						catch(NuclosBusinessException ex) {
							throw new NuclosFatalException(ex);
						}
					}
					else {
						if (MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName).isEditable()
							&& mdvoDependant.isFlagRemoved() && mdvoDependant.getId() != null) {
							// remove the row:
							MasterDataVO voDependant = DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
							removeSingleRow(sDependantEntityName, voDependant);
						}
					}
				}
			}
		}
	}

	/**
	 * creates/modifies the given dependants.
	 * @param mpDependants
	 * @param sEntityName
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 */
	void createOrModifyDependants(DependantMasterDataMap mpDependants, String sEntityName, String sUserName, boolean bValidate, Map<MasterDataVO, Integer> mpDependantsWithId, Map<EntityAndFieldName, String> mpEntityAndParentEntityName)
			throws CommonCreateException, CommonValidationException, CommonFinderException, CommonStaleVersionException, CommonPermissionException {

		for (String sDependantEntityName : mpDependants.getEntityNames()) {
			for (EntityObjectVO mdvoDependant : mpDependants.getData(sDependantEntityName)) {
				// create/modify the row:
				Integer intid = null;
				if (mpDependantsWithId != null && !mpDependantsWithId.isEmpty()) {
					intid = mpDependantsWithId.get(mdvoDependant);
				}
				if(MetaDataServerProvider.getInstance().getEntity(sDependantEntityName).isStateModel()) {
					try {
						mdvoDependant.setEntity(sDependantEntityName);
						GenericObjectVO govo = DalSupportForGO.getGenericObjectVO(mdvoDependant);
						GenericObjectFacadeLocal goLocal = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
						if(mdvoDependant.isFlagNew()) {
							goLocal.create(new GenericObjectWithDependantsVO(govo, mdvoDependant.getDependants()));
						}
						else if(mdvoDependant.isFlagRemoved()) {
							goLocal.remove(new GenericObjectWithDependantsVO(govo, mdvoDependant.getDependants()), true);
						}
						else if (mdvoDependant.isFlagUpdated()) {
							goLocal.modify(govo, mdvoDependant.getDependants(), false);
						}
					}
					catch(NuclosBusinessException ex) {
						throw new NuclosFatalException(ex);
					}
					catch(CommonRemoveException ex) {
						throw new NuclosFatalException(ex);
					}
				}
				else {
					MasterDataVO voDependant = DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
					Integer id = createOrModify(sDependantEntityName, voDependant, sEntityName, sUserName, bValidate, intid, mpEntityAndParentEntityName);
					mdvoDependant.setId(IdUtils.toLongId(id));
				}
			}
		}
	}

	/**
	 * creates the given dependant row, if it is new or updates it, if it has changed.
	 * @param sDependantEntityName
	 * @param mdvoDependant
	 * @param sEntityName
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 */
	private Integer createOrModify(String sDependantEntityName, MasterDataVO mdvoDependant, String sEntityName, String sUserName, boolean bValidate, Integer intid, Map<EntityAndFieldName, String> mpEntityAndParentEntityName)
			throws CommonCreateException, CommonValidationException, CommonFinderException, CommonStaleVersionException, CommonPermissionException {

		final String sIdFieldName = getForeignKeyFieldName(sEntityName, sDependantEntityName, mpEntityAndParentEntityName) + "Id";
		if (!mdvoDependant.isRemoved() && !mdvoDependant.isEmpty(sIdFieldName) && !MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName).isDynamic()) {
			// validate the row
			if(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SERVER_VALIDATES_MASTERDATAVALUES).equals("1")) {
				mdvoDependant.validate(MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName));
			}

			Integer iReferenceId;

			if (mdvoDependant.getId() == null) {
				iReferenceId = this.createSingleRow(sDependantEntityName, mdvoDependant, sUserName, bValidate, intid);
			}
			else {
				iReferenceId = (Integer) mdvoDependant.getId();
				if (MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName).isEditable() && mdvoDependant.isChanged()) {
					this.modifySingleRow(sDependantEntityName, mdvoDependant, sUserName, bValidate);
				}
				else {
					LOG.debug("Dependant row " + mdvoDependant.getId() + " has not changed. Will not be updated.");
				}
			}

			for (String sDependantMasterDataEntityName : mdvoDependant.getDependants().getEntityNames()) {
				String sForeignKeyFieldName = getForeignKeyFieldName(sEntityName, sDependantMasterDataEntityName, mpEntityAndParentEntityName);

				if (sForeignKeyFieldName != null) {
					for (EntityObjectVO mdvo : mdvoDependant.getDependants().getData(sDependantMasterDataEntityName)) {
						if(mdvo.getFieldIds().get(sForeignKeyFieldName) == null)
							mdvo.getFieldIds().put(sForeignKeyFieldName, new Long(iReferenceId));
					}
				}
				else {
					final String sMessage = StringUtils.getParameterizedExceptionMessage("mdhelper.error.missing.foreignkey.field", sDependantMasterDataEntityName, sDependantEntityName);
						//"Es existiert kein Fremdschl\u00fcsselfeld der Entit\u00e4t "+sDependantMasterDataEntityName+", das auf die \u00fcbergeordnete Entit\u00e4t "+sDependantEntityName+" referenziert";
					throw new NuclosFatalException(sMessage);
				}
			}

			//create or modify dependant data
			createOrModifyDependants(mdvoDependant.getDependants(), sDependantEntityName, sUserName, bValidate, null, mpEntityAndParentEntityName);
			return iReferenceId;
		}
		else {
			return null;
		}
	}


	public static String getForeignKeyFieldName(String sEntityName, String sDependantEntityName, Map<EntityAndFieldName, String> mpEntityAndParentEntityName) {
		String result = null;

		/**
		 * search in layout...
		 */
		for (EntityAndFieldName eafn : mpEntityAndParentEntityName.keySet()) {
			if (eafn.getEntityName().equals(sDependantEntityName)) {
				final String sFieldName = eafn.getFieldName();
				if (result == null) {
					// this is the foreign key field:
					result = sFieldName;
				}
				else {
					final String sMessage = StringUtils.getParameterizedExceptionMessage("mdhelper.error.more.foreignkey.field", result, sFieldName);
						//"Es gibt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t referenziert:\n" + "\t" + result + "\n" + "\t" + sFieldName;
					throw new NuclosFatalException(sMessage);
				}
			}
		}

		/**
		 * if no information from layout is accessible try to get it from meta data...
		 */
		if (result == null) {
			final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName);

			// Old Nucleus instance namend the foreign key field "genericObject"
			// and it could be that more than column refers on the parent entity, so
			// the underlying search could not find the referencing column.
			for (MasterDataMetaFieldVO mdmetafieldvo : mdmetavo.getFields()) {
				if ("genericObject".equalsIgnoreCase(mdmetafieldvo.getFieldName())) {
					return mdmetafieldvo.getFieldName();
				}
			}

			// Default: field referencing the parent entity has the same name as the parent entity
			if (mdmetavo.getFieldNames().contains(sEntityName)) {
				return sEntityName;
			}

			// If no such field is present, it must be a (the) field referencing the parent entity
			for (MasterDataMetaFieldVO mdmetafieldvo : mdmetavo.getFields()) {
				if (sEntityName.equals(mdmetafieldvo.getForeignEntity())) {
					final String sFieldName = mdmetafieldvo.getFieldName();
					if (result == null) {
						// this is the foreign key field:
						result = sFieldName;
					}
					else {
						final String sMessage = StringUtils.getParameterizedExceptionMessage("mdhelper.error.more.foreignkey.field", result, sFieldName);
							//"Es gibt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t referenziert:\n" + "\t" + result + "\n" + "\t" + sFieldName;
						throw new NuclosFatalException(sMessage);
					}
				}
			}
		}

		return result;
	}

	/**
	 * creates the given dependants.
	 * @param mpDependants
	 * @param sEntityName
	 * @param iParentId
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @precondition mpDependants != null
	 * @precondition sForeignIdFieldName != null
	 * @precondition iParentId != null
	 */
	public void createDependants(DependantMasterDataMap mpDependants, String sEntityName, Integer iParentId, String sUserName, boolean bValidate, Map<MasterDataVO,Integer> mpDependantsWithId, Map<EntityAndFieldName, String> mpEntityAndParentEntityName)
			throws CommonCreateException, CommonValidationException{

		for (String sDependantEntityName : mpDependants.getEntityNames()) {
			for (EntityObjectVO mdvoDependant : mpDependants.getData(sDependantEntityName)) {
				final String sForeignIdFieldName = getForeignKeyFieldName(sEntityName, sDependantEntityName, mpEntityAndParentEntityName) + "Id";
				if (!mdvoDependant.isFlagRemoved()/* && !mdvoDependant.isEmpty(sForeignIdFieldName)*/) {
					// @todo eliminate this workaround:
					// set the id of the foreign key field to the id of the parent:
					//mdvoDependant.setField(sForeignIdFieldName, iParentId);
					mdvoDependant.getFields().put(sForeignIdFieldName, iParentId);
					mdvoDependant.getFieldIds().put(sForeignIdFieldName.substring(0, sForeignIdFieldName.length()-2), 
							IdUtils.toLongId(iParentId));

					// create dependant row:
					Integer iId;
					MasterDataVO voDependant = DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
					if(mpDependantsWithId != null && !mpDependantsWithId.isEmpty())
						iId = createSingleRow(sDependantEntityName, voDependant, sUserName, bValidate, mpDependantsWithId.get(mdvoDependant));
					else
						iId = createSingleRow(sDependantEntityName, voDependant, sUserName, bValidate, null);

					createDependants(mdvoDependant.getDependants(), sDependantEntityName, iId, sUserName, bValidate, null, mpEntityAndParentEntityName);
				}
			}
		}
	}

	/**
	 * gets master data records for a given entity and search condition (generic mechanism)
	 * @param sEntityName name of the entity to get master data records for
	 * @param cond search condition value object
	 * @return TruncatableCollection<MasterDataVO> collection of master data value objects
	 * @postcondition result != null
	 */
	public TruncatableCollection<MasterDataVO> getGenericMasterData(String sEntityName, final CollectableSearchCondition cond, final boolean bAll) {
		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);

		CollectableSearchExpression clctexpr = new CollectableSearchExpression(cond);
		List<EntityObjectVO> eoResult = eoProcessor.getBySearchExpression(appendRecordGrants(clctexpr, sEntityName), bAll ? null : MAXROWS + 1, false);
		if(sEntityName.equals(NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName())){
			for(EntityObjectVO voEntity : eoResult) {
				String sPath = voEntity.getField("path", String.class);
				voEntity.getField("file", GenericObjectDocumentFile.class).setDirectoryPath(sPath);
			}
		}

		boolean truncated = false;
		int recordCount = eoResult.size();
		if (!bAll && recordCount >= MAXROWS) {
//			recordCount = eoProcessor.count(clctexpr);
			eoResult.subList(MAXROWS, recordCount).clear();
			truncated = true;
		}

		List<MasterDataVO> result = CollectionUtils.transform(eoResult, new Transformer<EntityObjectVO, MasterDataVO>() {
			@Override
			public MasterDataVO transform(EntityObjectVO eo) { return DalSupportForMD.wrapEntityObjectVO(eo); }
		});
		result = new GzipList<MasterDataVO>(result);

		final Collection<MasterDataVO> systemObjects = XMLEntities.getSystemObjects(sEntityName, cond);
		if (!systemObjects.isEmpty()) {
			recordCount += systemObjects.size();
			result.addAll(systemObjects);
		}

		return new TruncatableCollectionDecorator<MasterDataVO>(result, truncated, recordCount);
	}


	/**
	 * create or replace a file attachement in the file system
	 * @param documentFile
	 */
	private static void storeFile(GenericObjectDocumentFile documentFile, java.io.File dir) {
		File directory = dir;
		if(documentFile.getDirectoryPath() != null && documentFile.getDirectoryPath().length() > 0) {

			directory = new File(dir.getAbsolutePath() + "/" + documentFile.getDirectoryPath());
			directory.mkdirs();
		}

		remove(documentFile.getDocumentFileId(), documentFile.getFilename(), directory);

		try {
			String sPath = getPathName(documentFile);
			//sPath += "/" + StringUtils.emptyIfNull(documentFile.getDirectoryPath()) + "/";

			IOUtils.writeToBinaryFile(new java.io.File(sPath), documentFile.getContents());
		}
		catch (java.io.IOException e) {
			// logger.error("File content cannot be updated for new file", e);
			throw new NuclosFatalException("File content cannot be updated for new file (" + e.getMessage() + ").");
		}
	}

/**
 * deletes the file with the given id
 * @param iFileId
 */
	public static void remove(Integer iFileId, String sFilename, java.io.File dir) {
		if(dir.isDirectory()) {
			for (String sFileName : dir.list()) {
				if(sFileName.startsWith(iFileId + "." + (sFilename != null ? sFilename : ""))) {
					new java.io.File(dir.getAbsolutePath() + java.io.File.separator + sFileName).delete();
				}
			}
		}
	}

	private static String getPathName(GenericObjectDocumentFile documentFile) {
		try {
			if(documentFile == null) {
				throw new CommonFatalException("godocumentfile.invalid.file");//"Der Parameter documentFile darf nicht null sein.");
			}
			// @todo introduce symbolic constant
			if (documentFile.getDocumentFileId() == null) {
				throw new NuclosFatalException("godocumentfile.invalid.id");//"Die Id des Dokumentanhangs darf nicht null sein");
			}
			java.io.File documentDir;
			if(documentFile.getDirectoryPath() != null && documentFile.getDirectoryPath().length() > 0) {
				documentDir = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH)+ "/" + documentFile.getDirectoryPath());
			}
			else {
				documentDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH);
			}


			java.io.File file = new java.io.File(documentDir, documentFile.getDocumentFileId() + "." + documentFile.getFilename());
			LOG.debug("Calculated path for document attachment: " + file.getCanonicalPath());
			return file.getCanonicalPath();
		}
		catch (java.io.IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	public static void validateRoleDependants(DependantMasterDataMap mpDependants) throws CommonValidationException {

		for (String entityName : mpDependants.getEntityNames()) {
			RoleDependant dependant = RoleDependant.getByEntityName(entityName);
			if (dependant != null) {
				List<String> names = CollectionUtils.transform(
					mpDependants.getData(dependant.getEntity().getEntityName()), new EntityObjectVO.GetTypedField<String>(dependant.getEntityFieldName(),String.class));

				for (String name : names) {
					if (Collections.frequency(names, name) > 1) {
						if (dependant.getSubFieldName() != null) {
							List<String> subFieldNames = new ArrayList<String>();

							for (EntityObjectVO mdVO : mpDependants.getData(dependant.getEntity().getEntityName()))
								if (mdVO.getField(dependant.getEntityFieldName(), String.class).equals(name))
									subFieldNames.add(mdVO.getField(dependant.getSubFieldName(), String.class));

							for (String subName : subFieldNames) {
								if (Collections.frequency(subFieldNames, subName) > 1) {
									throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage(
										"role.error.validation.dependant.sub", name, subName, dependant.getResourceId()));
								}
							}
						}
						else {
							throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage(
								"role.error.validation.dependant", name, dependant.getResourceId()));
						}
					}
				}
			}
		}
	}

	private AttributeFacadeLocal getAttributeFacade() {
		if (attributeFacade == null) {
			attributeFacade = ServiceLocator.getInstance().getFacade(AttributeFacadeLocal.class);
		}
		return attributeFacade;
	}

//	static MetaDataProvider getSingleDbMetaDataProviderFor(MasterDataMetaVO mdMeta) {
//		String entityName = mdMeta.getEntityName();
//		Long entityId = mdMeta.getId().longValue();
//		StaticMetaDataProvider provider = new StaticMetaDataProvider();
//		// Wrap entity metadata
//		EntityMetaDataVO entityMeta = new EntityMetaDataVO();
//		entityMeta.setId(entityId);
//		entityMeta.setEntity(entityName);
//		entityMeta.setDbEntity(mdMeta.getDBEntity());
//		provider.addEntity(entityMeta);
//		for (MasterDataMetaFieldVO mdFieldMeta : mdMeta.getFields()) {
//			EntityFieldMetaDataVO fieldMeta = new EntityFieldMetaDataVO();
//			fieldMeta.setId(mdFieldMeta.getId().longValue());
//			fieldMeta.setField(mdFieldMeta.getFieldName());
//			fieldMeta.setDbColumn(mdFieldMeta.getDBFieldName());
//			fieldMeta.setDataType(mdFieldMeta.getJavaClass().getName());
//			fieldMeta.setScale(mdFieldMeta.getDataScale());
//			fieldMeta.setPrecision(mdFieldMeta.getDataPrecision());
//			fieldMeta.setNullable(mdFieldMeta.isNullable());
//			fieldMeta.setForeignEntity(mdFieldMeta.getForeignEntity());
//			fieldMeta.setForeignEntityField(mdFieldMeta.getForeignEntityField());
//			fieldMeta.setEntityId(entityId);
//			fieldMeta.setIndexed(mdFieldMeta.isIndexed());
//			provider.addEntityField(entityName, fieldMeta);
//		}
//		return provider;
//	}

	private void updateDbObject(EntityObjectVO oldSource, EntityObjectVO newSource, boolean isRollback) throws NuclosBusinessException {
		if (oldSource == null && newSource == null) {
			throw new NuclosFatalException("oldSource and newSource must not be null.");
		} else if (oldSource != null && newSource != null && !oldSource.getField("dbobject", String.class).equals(newSource.getField("dbobject", String.class))) {
			throw new NuclosFatalException("oldSource and newSource not from same object.");
		} else if (oldSource != null && newSource != null && !oldSource.getField("dbtype", String.class).equals(newSource.getField("dbtype", String.class))) {
			throw new NuclosFatalException("Dbtype of oldSource and dbtype of newSource have to be equal.");
		}

		String dbtype = (oldSource != null) ? oldSource.getField("dbtype", String.class) : newSource.getField("dbtype", String.class);

		final DbAccess dbAccess = DataBaseHelper.getDbAccess();

		if (!dbAccess.getDbType().equals(DbType.getFromName(dbtype))) {
			return;
		}

		final DbObjectHelper dboHelper = new DbObjectHelper(dbAccess);

		boolean isUsedAsCalculatedAttribute = false;
		boolean isEntityView = false;
		EntityMetaDataVO eMetaUsingThisView = null;

		final String objectName = oldSource != null? oldSource.getField("dbobject", String.class) : newSource.getField("dbobject", String.class);
		final List<EntityObjectVO> dbObject = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.DBOBJECT).getBySearchExpression(
			new CollectableSearchExpression(SearchConditionUtils.newEOComparison(NuclosEntity.DBOBJECT.getEntityName(),
				"name", ComparisonOperator.EQUAL, objectName, MetaDataServerProvider.getInstance())));
		if (dbObject.isEmpty())
			throw new NuclosFatalException("Database object with name \"" + objectName + "\" does not exists");

		DbObjectType type = DbObjectType.getByName(dbObject.get(0).getField("dbobjecttype", String.class));
		switch (type) {
		case FUNCTION:
			/**
			 * look if function is used as calculated attribute
			 */
			isUsedAsCalculatedAttribute = DbObjectHelper.isUsedAsCalculatedAttribute(objectName, MetaDataServerProvider.getInstance());
			break;
		case VIEW:
			/**
			 * look if view is replacing an entity object view
			 */
			for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities())
				if (objectName.equals(eMeta.getDbEntity())) {
					eMetaUsingThisView = eMeta;
					isEntityView = true;
				}
		}

		/**
		 * check before any DML is executed,
		 * otherwise oracle commits the transaction and it doesn't matter if throw an exception ot not.
		 */
		if (newSource == null || !newSource.getField("active", Boolean.class)) {
			if (isUsedAsCalculatedAttribute) {
				/**
				 * if in use no deactivation/delete allowed
				 */
				throw new NuclosBusinessException("masterdata.error.dbobject.isinuse.calcattr");
			}
		}

		try {
			if (oldSource != null && oldSource.getField("active", Boolean.class)) {
				/** drop '.y' old object */
				dbAccess.execute(dboHelper.getStatements(oldSource, type.getName()).y);
			} else {
				if (isEntityView && !isRollback) {
					/**
					 * drop generic entity object view
					 */
					dbAccess.execute(new DbStructureChange(Type.DROP, new DbSimpleView(null, objectName, new ArrayList<DbSimpleViewColumn>())));
				}
			}
		} catch (DbException dbex) {
			// ignore and try to create new object. maybe the object does not exists any more.
		}

		try {
			if (newSource != null && newSource.getField("active", Boolean.class)) {
				try {
					/** create '.x' new object */
					dbAccess.execute(dboHelper.getStatements(newSource, type.getName()).x);
				} catch (DbException dbex) {
					/**
					 * back to previous version
					 */
					if (oldSource != null) {
						this.updateDbObject(null, oldSource, true);
					}
				}
			} else {
				if (isEntityView) {
					/**
					 * back to generic entity object view
					 */
					EntityObjectMetaDbHelper eoHelper = new EntityObjectMetaDbHelper(dbAccess, MetaDataServerProvider.getInstance());
					List<DbSimpleView> genericView = eoHelper.getDbTable(eMetaUsingThisView).getTableArtifacts(DbSimpleView.class);
					if (genericView.isEmpty())
						return;
					try {
						dbAccess.execute(SchemaUtils.create(genericView.get(0)));
					}
					catch (DbException dbex) {
						throw new NuclosBusinessException(StringUtils.getParameterizedExceptionMessage("masterdata.error.dbobject.restore.generic.view", dbex.getMessage()));
					}
				}
			}
		}
		catch (Exception ex) {
			throw new NuclosBusinessException(ex.getMessage(), ex);
		}
	}

	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 * 
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, String entity) {
		return grantUtils.append(expr, entity);
	}

	/**
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression getRecordGrantExpression(Long id, String entity) {
		return appendRecordGrants(new CollectableSearchExpression(new CollectableIdCondition(id)), entity);
	}

	public void removeDependantTaskObjects(Integer entityId) {
		DbStatement stmt = DbStatementUtils.deleteFrom("T_UD_TODO_OBJECT",
			"INTID_T_UD_GENERICOBJECT", entityId);
		DataBaseHelper.getDbAccess().execute(stmt);
	}

}	// class MasterDataFacadeHelper

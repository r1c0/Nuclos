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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.Actions;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.security.Permission;
import org.nuclos.common.security.PermissionKey;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeLocal;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.mbean.SecurityCacheMBean;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.nuclos.server.statemodel.valueobject.StateModelUsages.StateModelUsage;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Singleton class for getting permissions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class SecurityCache implements SecurityCacheMBean {

	private static SecurityCache singleton;

	private final Logger log = Logger.getLogger(this.getClass());

	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_SECURITYCACHE);

	private SecurityFacadeLocal securityfacade;
	private GenericObjectGroupFacadeLocal genericobjectgroupfacade;

	private Map<PermissionKey.AttributePermissionKey, Permission> mpAttributePermission = new HashMap<PermissionKey.AttributePermissionKey, Permission>();

	private static class UserRights {

		private final String sUserName;

		private ModulePermissions modulepermissions;
		private MasterDataPermissions masterdatapermissions;
		private Collection<Integer> collTransitionIds;
		private Map<ReportType,Collection<Integer>> result;
		private Collection<Integer> collWritableReportIds;
		private Collection<Integer> collReadableDataSourceIds;
		private Collection<Integer> collWritableDataSourceIds;
		private Collection<CompulsorySearchFilter> collCompulsorySearchFilters;
		private Set<String> actions;
		private Set<Integer> roleIds;
		private Integer userId;
		private Boolean isSuperUser;

		UserRights(String sUserName) {
			this.sUserName = sUserName;
		}

		public synchronized boolean isSuperUser() {
			if (NuclosLocalServerSession.STATIC_SUPERUSER.equals(this.sUserName)) {
				isSuperUser = true;
			}
			else if (isSuperUser == null) {
				readUserData();
			}
			return isSuperUser;
		}

		private synchronized Integer getUserId() {
			if (userId == null) {
				readUserData();
			}
			return userId;
		}

		public synchronized Set<String> getAllowedActions() {
			if (actions == null) {
				actions = readActions();
			}
			return actions;
		}

		public synchronized ModulePermissions getModulePermissions() {
			if (modulepermissions == null) {
				modulepermissions = readModulePermissions();
			}
			return modulepermissions;
		}

		public synchronized MasterDataPermissions getMasterDataPermissions() {
			if (masterdatapermissions == null) {
				masterdatapermissions = readMasterDataPermissions();
			}
			return masterdatapermissions;
		}

		public synchronized Collection<Integer> getTransitionIds() {
			if (collTransitionIds == null) {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<Integer> query = builder.createQuery(Integer.class);

				if (isSuperUser()) {
					DbFrom t = query.from("T_MD_STATE_TRANSITION").alias(SystemFields.BASE_ALIAS);
					query.select(t.baseColumn("INTID", Integer.class));
				} else {
					DbFrom t = query.from("T_MD_ROLE_TRANSITION").alias(SystemFields.BASE_ALIAS);
					query.select(t.baseColumn("INTID_T_MD_STATE_TRANSITION", Integer.class));
					query.where(t.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()));
				}

				collTransitionIds = DataBaseHelper.getDbAccess().executeQuery(query.distinct(true));
			}
			return collTransitionIds;
		}

		public synchronized Map<ReportType,Collection<Integer>> getReadableReports() {
			if (result == null) {
				result = readReports(false);
			}
			return result;
		}

		public synchronized Collection<Integer> getWritableReportIds() {
			// @todo refactor: It doesn't make sense to calculate the writable and readable reports in two independent queries.
			if (collWritableReportIds == null) {
				Map<ReportType, Collection<Integer>> reports = readReports(true);
				collWritableReportIds = CollectionUtils.concatAll(reports.values());
			}
			return collWritableReportIds;
		}

		private synchronized Map<ReportType,Collection<Integer>> readReports(boolean readWrite) {
			Map<ReportType,Collection<Integer>> result = new HashMap<ReportType,Collection<Integer>>();

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom t = query.from("T_UD_REPORT").alias(SystemFields.BASE_ALIAS);
			query.multiselect(t.baseColumn("INTID", Integer.class), t.baseColumn("INTTYPE", Integer.class));
			if (!isSuperUser()) {
				DbFrom rr = t.join("T_MD_ROLE_REPORT", JoinType.LEFT).alias("rr").on("INTID", "INTID_T_UD_REPORT", Integer.class);
				DbColumnExpression<Integer> rrc = rr.baseColumn("INTID_T_MD_ROLE", Integer.class);
				DbCondition readWriteCond = readWrite
					? builder.equal(rr.baseColumn("BLNREADWRITE", Boolean.class), true)
					: builder.alwaysTrue();
				query.where(builder.or(
					getNameCondition(query, t, "STRCREATED"),
					builder.and(rrc.in(getRoleIds()), readWriteCond)));
			}

			for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
				Integer id = tuple.get(0, Integer.class);
				ReportType type = KeyEnum.Utils.findEnum(ReportType.class, tuple.get(1, Integer.class));
				if (result.containsKey(type))
					result.get(type).add(id);
				else {
					Collection<Integer> ids = new HashSet<Integer>();
					ids.add(id);
					result.put(type, ids);
				}
			}

			return result;
		}

		public synchronized Collection<Integer> getReadableDataSourceIds() {
			if (collReadableDataSourceIds == null) {
				collReadableDataSourceIds = readDataSourceIds(false);
			}
			return collReadableDataSourceIds;
		}

		public synchronized Collection<Integer> getWritableDataSourceIds() {
			if (collWritableDataSourceIds == null) {
				collWritableDataSourceIds = readDataSourceIds(true);
			}
			return collWritableDataSourceIds;
		}

		public synchronized Set<Integer> getCompulsorySearchFilterIds(String entity) {
			if (collCompulsorySearchFilters == null) {
				collCompulsorySearchFilters = readCompulsorySearchFilterIds();
			}
			Date now = DateUtils.getPureDate(DateUtils.now());
			Set<Integer> result = new HashSet<Integer>();
			for (CompulsorySearchFilter f : collCompulsorySearchFilters) {
				if (f.entity.equals(entity) && f.isValid(now))
					result.add(f.id);
			}
			return result;
		}

		private synchronized Collection<Integer> readDataSourceIds(boolean readWrite) {
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom t = query.from("T_UD_DATASOURCE").alias(SystemFields.BASE_ALIAS);
			query.select(t.baseColumn("INTID", Integer.class));

			if (!isSuperUser()) {
				DbFrom ro = t.join("T_UD_REPORTOUTPUT", JoinType.LEFT).alias("ro").on("INTID", "INTID_T_UD_DATASOURCE", Integer.class);
				List<Integer> reportIds = CollectionUtils.concatAll(readReports(readWrite).values());
				query.where(builder.or(
					getNameCondition(query, t, "STRCREATED"),
					ro.baseColumn("INTID_T_UD_REPORT", Integer.class).in(reportIds)));
			}

			return DataBaseHelper.getDbAccess().executeQuery(query.distinct(true));
		}

		private DbCondition getNameCondition(DbQuery<?> q, DbFrom t, String column) {
			DbQueryBuilder builder = q.getBuilder();
			return builder.equal(builder.upper(t.baseColumn(column, String.class)), builder.upper(builder.literal(sUserName)));
		}

		public synchronized Set<Integer> getRoleIds() {
			if (roleIds == null) {
				roleIds = readUserRoleIds();
			}
			return roleIds;
		}

		private ModulePermissions readModulePermissions() {
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();

			final ModuleProvider moduleprovider = Modules.getInstance();
			final Map<Pair<String, Integer>, ModulePermission> mpByEntityName = new HashMap<Pair<String, Integer>, ModulePermission>();
			final Map<Pair<Integer, Integer>, ModulePermission> mpByModuleId = new HashMap<Pair<Integer, Integer>, ModulePermission>();
			final Map<String, Boolean> mpNewAllowedByEntityName = new HashMap<String, Boolean>();
			final Map<Integer, Boolean> mpNewAllowedByModuleId = new HashMap<Integer, Boolean>();
			final Map<String, Set<Integer>> mpNewAllowedProcessesByEntityName = new HashMap<String, Set<Integer>>();
			final Map<Integer, Set<Integer>> mpNewAllowedProcessesByModuleId = new HashMap<Integer, Set<Integer>>();

			if (isSuperUser()) {
				DbQuery<String> query = builder.createQuery(String.class);
				DbFrom t = query.from("T_MD_ENTITY").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("STRENTITY", String.class));
				query.where(builder.equal(t.baseColumn("BLNUSESSTATEMODEL", Boolean.class), true));
				for (String entityName : DataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
					ModulePermission permission = ModulePermission.DELETE_PHYSICALLY;
					final Integer iModuleId = moduleprovider.getModuleIdByEntityName(entityName);

					mpByEntityName.put(new Pair<String, Integer>(entityName, null), permission);
					mpByModuleId.put(new Pair<Integer, Integer>(iModuleId, null), permission);
					mpNewAllowedByEntityName.put(entityName, Boolean.TRUE);
					mpNewAllowedByModuleId.put(iModuleId, Boolean.TRUE);

					Set<Integer> newAllowedProcesses = new HashSet<Integer>();
					for (Integer iStateModelId : StateModelUsagesCache.getInstance().getStateUsages().getStateModelIdsByModuleId(iModuleId)) {
						StateTransitionVO initialTransitionVO = StateCache.getInstance().getInitialTransistionByModel(iStateModelId);
						if (iStateModelId != null && initialTransitionVO != null) {
							for (UsageCriteria uc : StateModelUsagesCache.getInstance().getStateUsages().getUsageCriteriaByStateModelId(iStateModelId)) {
								newAllowedProcesses.add(uc.getProcessId());
							}
						}
					}
					mpNewAllowedProcessesByEntityName.put(entityName, newAllowedProcesses);
					mpNewAllowedProcessesByModuleId.put(iModuleId, newAllowedProcesses);
				}
			} else {
				DbQuery<DbTuple> query = builder.createTupleQuery();
				DbFrom m = query.from("T_MD_ENTITY").alias("m");
				DbFrom re = m.join("T_MD_ROLE_MODULE", JoinType.INNER).alias("re").on("INTID", "INTID_T_MD_MODULE", Integer.class);
				query.multiselect(
					m.baseColumn("STRENTITY", String.class),
					re.baseColumn("INTID_T_UD_GROUP", Integer.class),
					re.baseColumn("INTPERMISSION", Integer.class));
				query.where(builder.and(
					re.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()),
					builder.equal(m.baseColumn("BLNUSESSTATEMODEL", Boolean.class), true)));
				for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
					String entityName = tuple.get(0, String.class);
					Integer group = tuple.get(1, Integer.class);
					ModulePermission permission = ModulePermission.getInstance(tuple.get(2, Integer.class));
					permission = permission.max(mpByEntityName.get(new Pair<String, Integer>(entityName, group)));
					final Integer iModuleId = moduleprovider.getModuleIdByEntityName(entityName);

					mpByEntityName.put(new Pair<String, Integer>(entityName, group), permission);
					mpByModuleId.put(new Pair<Integer, Integer>(iModuleId, group), permission);

					/** is new allowed is defined in initial state model. */
					if (!mpNewAllowedByModuleId.containsKey(iModuleId)) {
						StateModelUsage smu = StateModelUsagesCache.getInstance().getStateUsages().getStateModelUsage(new UsageCriteria(iModuleId, null));
						Boolean isNewAllowed = false;
						if (smu != null) {
							Integer iStateModelId = smu.getStateModelId();
							StateTransitionVO initialTransitionVO = iStateModelId!=null? StateCache.getInstance().getInitialTransistionByModel(iStateModelId) : null;
							isNewAllowed = iStateModelId != null && initialTransitionVO != null &&
								!CollectionUtils.intersection(initialTransitionVO.getRoleIds(), getRoleIds()).isEmpty();
						}
						mpNewAllowedByEntityName.put(entityName, isNewAllowed);
						mpNewAllowedByModuleId.put(iModuleId, isNewAllowed);
					}

					/** is new process allowed is defined in state model. */
					if (!mpNewAllowedProcessesByModuleId.containsKey(iModuleId)) {
						Set<Integer> newAllowedProcesses = new HashSet<Integer>();

						for (Integer iStateModelId : StateModelUsagesCache.getInstance().getStateUsages().getStateModelIdsByModuleId(iModuleId)) {
							StateTransitionVO initialTransitionVO = StateCache.getInstance().getInitialTransistionByModel(iStateModelId);
							final Boolean isNewAllowed = iStateModelId != null && initialTransitionVO != null &&
								!CollectionUtils.intersection(initialTransitionVO.getRoleIds(), getRoleIds()).isEmpty();
							if (isNewAllowed) {
								for (UsageCriteria uc : StateModelUsagesCache.getInstance().getStateUsages().getUsageCriteriaByStateModelId(iStateModelId)) {
									newAllowedProcesses.add(uc.getProcessId());
								}
							}
						}
						mpNewAllowedProcessesByEntityName.put(entityName, newAllowedProcesses);
						mpNewAllowedProcessesByModuleId.put(iModuleId, newAllowedProcesses);
					}
				}
			}

			return new ModulePermissions(mpByEntityName, mpByModuleId, mpNewAllowedByEntityName, mpNewAllowedByModuleId, mpNewAllowedProcessesByEntityName, mpNewAllowedProcessesByModuleId);
		}

		private MasterDataPermissions readMasterDataPermissions() {
			final Map<String, MasterDataPermission> mpByEntityName = new HashMap<String, MasterDataPermission>();

			if (isSuperUser()) {
				for (MasterDataMetaVO metaVO : MasterDataMetaCache.getInstance().getAllMetaData()) {
					mpByEntityName.put(metaVO.getEntityName(), MasterDataPermission.DELETE);
				}
			} else {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<DbTuple> query = builder.createTupleQuery();
				DbFrom t = query.from("T_MD_ROLE_MASTERDATA").alias(SystemFields.BASE_ALIAS);
				query.multiselect(t.baseColumn("STRMASTERDATA", String.class), t.baseColumn("INTPERMISSION", Integer.class));
				query.where(t.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()));
				for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
					String entity = tuple.get(0, String.class);
					MasterDataPermission mp = MasterDataPermission.getInstance(tuple.get(1, Integer.class));
					mpByEntityName.put(entity, mp.max(mpByEntityName.get(entity)));
				}
				if (getAllowedActions().contains(Actions.ACTION_TASKLIST)){
					mpByEntityName.put(NuclosEntity.TASKLIST.getEntityName(), MasterDataPermission.DELETE);
				}
			}

			return new MasterDataPermissions(mpByEntityName);
		}

		private Set<Integer> readUserRoleIds() {
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Integer> query = builder.createQuery(Integer.class);
			DbFrom t = query.from("T_MD_ROLE_USER").alias(SystemFields.BASE_ALIAS);
			query.select(t.baseColumn("INTID_T_MD_ROLE", Integer.class));
			query.where(builder.equal(t.baseColumn("INTID_T_MD_USER", Integer.class), getUserId()));
			return new HashSet<Integer>(DataBaseHelper.getDbAccess().executeQuery(query.distinct(true)));
		}

		private Set<String> readActions() {
			if (isSuperUser()) {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<String> query = builder.createQuery(String.class);
				DbFrom t = query.from("T_AD_ACTION").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("STRACTION", String.class));
				Set<String> actions = new HashSet<String>(DataBaseHelper.getDbAccess().executeQuery(query));

				for(MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.ACTION).getAll()) {
					actions.add(mdvo.getField("action", String.class));
				}
				return actions;
			}

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<String> query = builder.createQuery(String.class);
			DbFrom t = query.from("T_MD_ROLE_ACTION").alias(SystemFields.BASE_ALIAS);
			query.select(t.baseColumn("STRACTIONNAME", String.class));
			query.where(t.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()));
			return new HashSet<String>(DataBaseHelper.getDbAccess().executeQuery(query));
		}

		public synchronized Map<Integer, Permission> getSubFormPermissions(String sEntityName) {
			final Map<Integer, Permission> result = new HashMap<Integer, Permission>();
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();

			if (isSuperUser()) {
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("T_MD_STATE").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID", Integer.class));
				for (Integer state : DataBaseHelper.getDbAccess().executeQuery(query)) {
					result.put(state, Permission.READWRITE);
				}
			} else {
				DbQuery<DbTuple> query = builder.createQuery(DbTuple.class);
				DbFrom t = query.from("T_MD_ROLE_SUBFORM").alias(SystemFields.BASE_ALIAS);
				query.multiselect(
					t.baseColumn("INTID_T_MD_STATE", Integer.class),
					t.baseColumn("BLNREADWRITE", Boolean.class));
				query.where(builder.and(
					t.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()),
					builder.equal(t.baseColumn("STRENTITY", String.class), sEntityName)));
				for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
					Integer state = tuple.get(0, Integer.class);
					Permission permission = Boolean.TRUE.equals(tuple.get(1)) ? Permission.READWRITE : Permission.READONLY;
					result.put(state, permission.max(result.get(state)));
				}
			}
			return result;
		}

		public synchronized Map<Integer, Permission> getAttributeGroupPermissions(Integer iAttributeGroupId) {
			final Map<Integer, Permission> result = new HashMap<Integer, Permission>();
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();

			if (isSuperUser()) {
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("T_MD_STATE").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID", Integer.class));
				for (Integer state : DataBaseHelper.getDbAccess().executeQuery(query)) {
					result.put(state, Permission.READWRITE);
				}
			} else {
				DbQuery<DbTuple> query = builder.createQuery(DbTuple.class);
				DbFrom t = query.from("T_MD_ROLE_ATTRIBUTEGROUP").alias(SystemFields.BASE_ALIAS);
				query.multiselect(
					t.baseColumn("INTID_T_MD_STATE", Integer.class),
					t.baseColumn("BLNREADWRITE", Boolean.class));
				query.where(builder.and(
					t.baseColumn("INTID_T_MD_ROLE", Integer.class).in(getRoleIds()),
					builder.equal(t.baseColumn("INTID_T_MD_ATTRIBUTEGROUP", Integer.class), iAttributeGroupId)));
				for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
					Integer state = tuple.get(0, Integer.class);
					Permission permission = Boolean.TRUE.equals(tuple.get(1)) ? Permission.READWRITE : Permission.READONLY;
					result.put(state, permission.max(result.get(state)));
				}
			}
			return result;
		}

		private Collection<CompulsorySearchFilter> readCompulsorySearchFilterIds() {
			if (isSuperUser())
				return Collections.emptySet();
			List<CompulsorySearchFilter> list = new ArrayList<CompulsorySearchFilter>();
			list.addAll(readCompulsorySearchFilterIdsImpl("T_UD_SEARCHFILTER_USER", "INTID_T_MD_USER", Collections.singleton(getUserId())));
			list.addAll(readCompulsorySearchFilterIdsImpl("T_UD_SEARCHFILTER_ROLE", "INTID_T_MD_ROLE", getRoleIds()));
			return list;
		}

		private List<CompulsorySearchFilter> readCompulsorySearchFilterIdsImpl(String subtable, String condcolumn, Collection<Integer> condids) {
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();

			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom t = query.from("T_UD_SEARCHFILTER").alias(SystemFields.BASE_ALIAS);
			DbFrom u = t.join(subtable, JoinType.INNER).alias("u").on("INTID", "INTID_T_UD_SEARCHFILTER", Integer.class);
			query.multiselect(
				t.baseColumn("INTID", Integer.class), t.baseColumn("STRENTITY", String.class),
				u.baseColumn("DATVALIDFROM", Date.class), u.baseColumn("DATVALIDUNTIL", Date.class));
			query.where(builder.and(
				builder.equal(u.baseColumn("BLNCOMPULSORYFILTER", Boolean.class), true),
				u.baseColumn(condcolumn, Integer.class).in(condids)));
			query.distinct(true);
			return DataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, CompulsorySearchFilter>() {
				@Override
				public CompulsorySearchFilter transform(DbTuple t) {
					return new CompulsorySearchFilter(
						t.get(0, Integer.class), t.get(1, String.class), t.get(2, Date.class), t.get(3, Date.class));
				}
			});
		}

		private void readUserData() {
			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
			query.multiselect(
				t.baseColumn("INTID", Integer.class),
				t.baseColumn("BLNSUPERUSER", Boolean.class));
			query.where(getNameCondition(query, t, "STRUSER"));
			DbTuple tuple = CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
			if (tuple != null) {
				userId = tuple.get(0, Integer.class);
				isSuperUser = Boolean.TRUE.equals(tuple.get(1, Boolean.class));
			} else {
				// No user found
				userId = 0;
				isSuperUser = false;
			}
		}
	}	// class UserRights

	/**
	 * defines a unique key to get the permission for an attributegroup
	 */
	private static class UserAttributeGroup {
		private String sUserName;
		private Integer iAttributeGroupId;

		UserAttributeGroup(String sUserName, Integer iAttributeGroupId) {
			this.sUserName = sUserName;
			this.iAttributeGroupId = iAttributeGroupId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final UserAttributeGroup that = (UserAttributeGroup) o;
			return LangUtils.equals(this.sUserName, that.sUserName) && LangUtils.equals(this.iAttributeGroupId, that.iAttributeGroupId);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.sUserName) ^ LangUtils.hashCode(this.iAttributeGroupId);
		}

	}	// class UserAttributeGroup

	/**
	 * defines a unique key to get the permission for a subform used in an entity
	 */
	private static class UserSubForm {
		private String sUserName;
		private String sEntityName;

		UserSubForm(String sUserName, String sEntityName) {
			this.sUserName = sUserName;
			this.sEntityName = sEntityName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final UserSubForm that = (UserSubForm) o;
			return LangUtils.equals(this.sUserName, that.sUserName) && LangUtils.equals(this.sEntityName, that.sEntityName);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.sUserName) ^ LangUtils.hashCode(this.sEntityName);
		}
	} // class UserSubForm

	private static class CompulsorySearchFilter {
		private Integer id;
		private String entity;
		private Date validFrom;
		private Date validUntil;

		CompulsorySearchFilter(Integer id, String entity, Date validFrom, Date validUntil) {
			this.entity = entity;
			this.id = id;
			this.validFrom = validFrom;
			this.validUntil = validUntil;
		}

		boolean isValid(Date date) {
			return (validFrom == null || validFrom.before(date))
				&& (validUntil == null || validUntil.after(date));
		}
	}

	private final Map<String, UserRights> mpUserRights = new HashMap<String, UserRights>();

	private final Map<UserAttributeGroup, Map<Integer, Permission>> mpAttributeGroups;
	private final Map<UserSubForm, Map<Integer, Permission>> mpSubForms;

	public static synchronized SecurityCache getInstance() {
		if (singleton == null) {
			singleton = new SecurityCache();
			// register this cache as MBean
			//MBeanAgent.registerCache(singleton, SecurityCacheMBean.class);
		}

		return singleton;
	}

	private SecurityCache() {
		/** @todo OPTIMIZE: These maps needn't be synchronized */
		this.mpAttributeGroups = Collections.synchronizedMap(new HashMap<UserAttributeGroup, Map<Integer, Permission>>());
		this.mpSubForms = Collections.synchronizedMap(new HashMap<UserSubForm, Map<Integer, Permission>>());
	}

	public Set<String> getAllowedActions(String sUserName) {
		return this.getUserRights(sUserName).getAllowedActions();
	}

	public ModulePermissions getModulePermissions(String sUserName) {
		return this.getUserRights(sUserName).getModulePermissions();
	}

	public MasterDataPermissions getMasterDataPermissions(String sUserName){
		return this.getUserRights(sUserName).getMasterDataPermissions();
	}

	public boolean isReadAllowedForModule(String sUserName, Integer iModuleId, Integer iGenericObjectId) {
		return this.isReadAllowedForModule(sUserName, Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId);
	}

	public boolean isReadAllowedForModule(String sUserName, String sEntityName, Integer iGenericObjectId) {
		return this.getUserRights(sUserName).isSuperUser() || ModulePermission.includesReading(this.getModulePermissions(sUserName).getMaxPermissionForGenericObject(sEntityName, iGenericObjectId));
	}

	public boolean isReadAllowedForMasterData(String sUserName, Integer iMasterDataId) {
		return this.isReadAllowedForMasterData(sUserName, MasterDataMetaCache.getInstance().getMetaDataById(iMasterDataId).getEntityName());
	}

	public boolean isReadAllowedForMasterData(String sUserName, String sEntityName) {
		if (NuclosEntity.REPORTEXECUTION.checkEntityName(sEntityName)) {
			return this.getUserRights(sUserName).getAllowedActions().contains(Actions.ACTION_EXECUTE_REPORTS);
		}
		return this.getUserRights(sUserName).isSuperUser() || MasterDataPermission.includesReading(this.getMasterDataPermissions(sUserName).get(sEntityName));
	}

	public boolean isNewAllowedForModule(String sUserName, Integer iModuleId) {
		return this.getModulePermissions(sUserName).getNewAllowedByModuleId().get(iModuleId);
	}

	public boolean isWriteAllowedForModule(String sUserName, Integer iModuleId, Integer iGenericObjectId) {
		return this.isWriteAllowedForModule(sUserName, Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId);
	}

	public boolean isWriteAllowedForModule(String sUserName, String sEntityName, Integer iGenericObjectId) {
		return ModulePermission.includesWriting(this.getModulePermissions(sUserName).getMaxPermissionForGenericObject(sEntityName, iGenericObjectId));
	}

	public boolean isWriteAllowedForMasterData(String sUserName, Integer iMasterDataId) {
		return this.isWriteAllowedForMasterData(sUserName, MasterDataMetaCache.getInstance().getMetaDataById(iMasterDataId).getEntityName());
	}

	public boolean isWriteAllowedForMasterData(String sUserName, String sEntityName) {
		return MasterDataPermission.includesWriting(this.getMasterDataPermissions(sUserName).get(sEntityName));
	}

	public boolean isWriteAllowedForObjectGroup(String sUserName, Integer iModuleId, Integer iObjectGroupId) {
		return this.isWriteAllowedForObjectGroup(sUserName, Modules.getInstance().getEntityNameByModuleId(iModuleId), iObjectGroupId);
	}

	public boolean isWriteAllowedForObjectGroup(String sUserName, String sEntityName, Integer iObjectGroupId) {
		return this.getUserRights(sUserName).isSuperUser() || ModulePermission.includesWriting(this.getModulePermissions(sUserName).getMaxPermissionForObjectGroup(sEntityName, iObjectGroupId));
	}

	public boolean isDeleteAllowedForModule(String sUserName, Integer iModuleId, Integer iGenericObjectId, boolean bPhysically) {
		return this.isDeleteAllowedForModule(sUserName, Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId, bPhysically);
	}

	public boolean isDeleteAllowedForModule(String sUserName, String sEntityName, Integer iGenericObjectId, boolean bPhysically) {
		if (this.getUserRights(sUserName).isSuperUser())
			return true;
		final ModulePermission modulepermission = this.getModulePermissions(sUserName).getMaxPermissionForGenericObject(sEntityName, iGenericObjectId);
		return bPhysically ?
				ModulePermission.includesDeletingPhysically(modulepermission) :
				ModulePermission.includesDeletingLogically(modulepermission);

	}

	public boolean isDeleteAllowedForMasterData(String sUserName, Integer iMasterDataId) {
		return this.isDeleteAllowedForMasterData(sUserName, MasterDataMetaCache.getInstance().getMetaDataById(iMasterDataId).getEntityName());
	}

	public boolean isDeleteAllowedForMasterData(String sUserName, String sEntityName) {
		return this.getUserRights(sUserName).isSuperUser() || MasterDataPermission.includesDeleting(this.getMasterDataPermissions(sUserName).get(sEntityName));
	}

	public boolean isSuperUser(String sUserName) {
		return this.getUserRights(sUserName).isSuperUser();
	}

	/**
	 * @param sUserName
	 * @return Collection<Integer iModuleId> the ids of all modules that can be read by the user with the given name.
	 */
	public Set<Integer> getReadableModuleIds(String sUserName) {
		final Set<Integer> result = new HashSet<Integer>();
		for (Entry<Pair<Integer,Integer>, ModulePermission> userRights : this.getModulePermissions(sUserName).getEntries()) {
			if (ModulePermission.includesReading(userRights.getValue())) {
				result.add(userRights.getKey().getX());
			}
		}
		return result;
	}

	/**
	 * @param sUserName
	 * @param iModuleId
	 * @return a set of genericobject ids on which the given user has read-permission or
	 * 		   null if the given user has read-permission on all genericobjects of the given module
	 */
	public Set<Integer> getReadableGenericObjectIdsByModule(String sUserName, Integer iModuleId) {
		Set<Integer> stResult = new HashSet<Integer>();

		ModulePermissions modulePermissions = getModulePermissions(sUserName);

		for (Pair<Integer, Integer> pairByModuleId : modulePermissions.getPermissionsByModuleId().keySet()) {
			Integer iPairModuleId = pairByModuleId.getX();
			Integer iPairGroupId = pairByModuleId.getY();

			// that means, the given user has read-permission on every genericobject of the given module
			if (iModuleId != null && iPairModuleId.compareTo(iModuleId) == 0 && iPairGroupId == 0) {
				return null;
			}

			ModulePermission modulePermission = modulePermissions.getPermissionsByModuleId().get(pairByModuleId);

			// iModuleId is null e.g. in case of general search, then the ids if all genericobjects are read
			// on which the user has read-permissions, otherwise only the genericobject ids for the given module are read
			if ((iModuleId == null || iPairModuleId.compareTo(iModuleId) == 0) && ModulePermission.includesReading(modulePermission)) {
				stResult.addAll(getGenericObjectGroupFacade().getGenericObjectIdsForGroup(iPairModuleId, iPairGroupId));
			}
		}
		return stResult;
	}

	private GenericObjectGroupFacadeLocal getGenericObjectGroupFacade() {
		if (genericobjectgroupfacade == null) {
			genericobjectgroupfacade = ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
		}
		return genericobjectgroupfacade;
	}

	public Collection<Integer> getTransitionIds(String sUserName) {
		return this.getUserRights(sUserName).getTransitionIds();
	}

	/**
	 * Get the ids of all reports/forms the user has the right to read.
	 * Read rights are given when the user
	 * - has created the report/form himself or
	 * - has access to it via the roles he belongs to.
	 * @param sUserName
	 * @return Map<ReportType,Collection<Integer>> contains a map of all reports/forms that may be read by the given user.
	 */
	public Map<ReportType,Collection<Integer>> getReadableReports(String sUserName) {
		return this.getUserRights(sUserName).getReadableReports();
	}

	/**
	 * Get the ids of all reports/forms the user has the right to write (and read).
	 * Write rights are given when the user
	 * - has created the report/form himself or
	 * - has writable access to it via the roles he belongs to.
	 * @param sUserName
	 * @return Collection<Integer> contains the ids of all reports/forms that may be (read and) written by the given user.
	 */
	public Collection<Integer> getWritableReportIds(String sUserName) {
		return this.getUserRights(sUserName).getWritableReportIds();
	}

	/**
	 * Get the ids of all datasources the user has the right to read.
	 * A user has right to read a datasource, when
	 * - the user has created it himself or
	 * - it is used in a report the user has access to (via the user's roles)
	 * @param sUserName
	 * @return Collection<Integer> contains the ids of all datasources that may be read by the given user.
	 */
	public Collection<Integer> getReadableDataSourceIds(String sUserName) {
		return this.getUserRights(sUserName).getReadableDataSourceIds();
	}

	/**
	 * Get the ids of all datasources the user has the right to read.
	 * A user has right to read a datasource, when
	 * - the user has created it himself or
	 * - it is used in a report the user has access to (via the user's roles)
	 * @param sUserName
	 * @return Collection<Integer> contains the ids of all datasources that may be read by the given user.
	 */
	public Collection<Integer> getReadableDataSources(String sUserName) {
		return this.getUserRights(sUserName).getReadableDataSourceIds();
	}

	/**
	 * Get the ids of all datasources the user has the right to write (and read).
	 * A user has right to write a datasource when
	 * - the user created it himself or
	 * - it is used in a report the user has writable access to (via the user's roles)
	 * @param sUserName
	 * @return Collection<Integer> contains the ids of all datasources that may be (read and) written by the given user.
	 */
	public Collection<Integer> getWritableDataSourceIds(String sUserName) {
		return this.getUserRights(sUserName).getWritableDataSourceIds();
	}

	/**
	 * @param sUserName
	 * @param iAttributeGroupId
	 * @return maps a state id to a permission.
	 */
	public synchronized Map<Integer, Permission> getAttributeGroup(String sUserName, Integer iAttributeGroupId) {
		final UserAttributeGroup userattrgroup = new UserAttributeGroup(sUserName, iAttributeGroupId);
		if (!mpAttributeGroups.containsKey(userattrgroup)) {
			mpAttributeGroups.put(userattrgroup, newAttributeGroup(sUserName, iAttributeGroupId));
		}

		/** @todo It's not necessary to return a copy here - an unmodifiable wrapper would be better (see below).
		 * We want to go sure for now and optimize later. */
		return new HashMap<Integer, Permission>(mpAttributeGroups.get(userattrgroup));

		/** @todo use this optimized version */
//		return Collections.unmodifiableMap((Map) mapAttributegroups.get(sUserName));
	}

	/**
	 * @param sUserName
	 * @param iSubFormId
	 * @return maps a state id to a permission
	 */
	public synchronized Map<Integer, Permission> getSubForm(String sUserName, String sEntityName) {
		final UserSubForm usersubform = new UserSubForm(sUserName, sEntityName);
		if(!this.mpSubForms.containsKey(usersubform)) {
			this.mpSubForms.put(usersubform, getUserRights(sUserName).getSubFormPermissions(sEntityName));
		}
		return new HashMap<Integer, Permission>(mpSubForms.get(usersubform));
	}

	private Map<Integer, Permission> newAttributeGroup(String sUserName, Integer iAttributeGroupId) {
		if (IdUtils.unsafeToId(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getFieldGroupId()).equals(iAttributeGroupId)) {
			Map<Integer, Permission> systemFieldPermissions = new HashMap<Integer, Permission>();

			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			for (MasterDataVO state : mdFacade.getMasterData(NuclosEntity.STATE.getEntityName(), null, true)) {
				systemFieldPermissions.put(state.getIntId(), Permission.READONLY);
			}

			return systemFieldPermissions;
		} else if (IdUtils.unsafeToId(NuclosEOField.PROCESS.getMetaData().getFieldGroupId()).equals(iAttributeGroupId)) {
			Map<Integer, Permission> systemFieldPermissions = new HashMap<Integer, Permission>();

			MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			for (MasterDataVO state : mdFacade.getMasterData(NuclosEntity.STATE.getEntityName(), null, true)) {
				systemFieldPermissions.put(state.getIntId(), Permission.READWRITE);
			}

			return systemFieldPermissions;
		}


		return getUserRights(sUserName).getAttributeGroupPermissions(iAttributeGroupId);
	}

	public synchronized Set<Integer> getCompulsorySearchFilterIds(String userName, String entity) {
		return getUserRights(userName).getCompulsorySearchFilterIds(entity);
	}

	@Override
	public synchronized void invalidate() {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCommit() {
				log.debug("Invalidating security cache...");

				mpUserRights.clear();
				mpAttributeGroups.clear();
				mpSubForms.clear();

				mpAttributePermission.clear();

				notifyClients();
			}
		});
	}

	public synchronized void invalidate(final String username) {
		if (username != null) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCommit() {
					log.debug("Invalidating security cache for user " + username + "...");
					if (mpUserRights.containsKey(username)) {
						mpUserRights.remove(username);
					}
					for (UserSubForm usf : new ArrayList<UserSubForm>(mpSubForms.keySet())) {
						if (username.equals(usf.sUserName)) {
							mpSubForms.remove(usf);
						}
					}
					for (UserAttributeGroup uag : new ArrayList<UserAttributeGroup>(mpAttributeGroups.keySet())) {
						if (username.equals(uag.sUserName)) {
							mpAttributeGroups.remove(uag);
						}
					}
					notifyUser(username);
				}
			});
		}
		else {
			invalidate();
		}
	}

	public boolean hasUserRight(String sActioName) {
		return getSecurityFacade().getAllowedActions().contains(sActioName);
	}

	public Integer getUserId(String sUserName){
		UserRights userRights = getUserRights(sUserName);
		return userRights != null ? userRights.getUserId() : null;
	}

	public Set<Integer> getUserRoles(String sUserName) {
		return getUserRights(sUserName).getRoleIds();
	}

	private synchronized UserRights getUserRights(String sUserName) {
		UserRights result = mpUserRights.get(sUserName);
		if (result == null) {
			result = new UserRights(sUserName);
			mpUserRights.put(sUserName, result);
		}
		return result;
	}

	/**
	 * get the permission for an attribute within the given status numeral
	 * @param sAttributeName
	 * @param iStateId
	 * @return Permission
	 */
	public Permission getAttributePermission(String sEntity, String sAttributeName, Integer iStateId) {
		PermissionKey.AttributePermissionKey attributePermissionKey = new PermissionKey.AttributePermissionKey(sEntity, sAttributeName, iStateId);
		if (!mpAttributePermission.containsKey(attributePermissionKey)) {
			Permission permission = getSecurityFacade().getAttributePermission(sEntity, sAttributeName, iStateId);
			mpAttributePermission.put(attributePermissionKey, permission);
		}
		return mpAttributePermission.get(attributePermissionKey);
	}

	private SecurityFacadeLocal getSecurityFacade() {
		if (securityfacade == null) {
			securityfacade = ServiceLocator.getInstance().getFacade(SecurityFacadeLocal.class);
		}
		return securityfacade;
	}

	@Override
	public int getAttributeGroupsCount() {
		return this.mpAttributeGroups != null ? this.mpAttributeGroups.size() : 0;
	}

	@Override
	public int getSubFormCount() {
		return this.mpSubForms != null ? this.mpSubForms.size() : 0;
	}

	@Override
	public int getUserRightsCount() {
		return this.mpUserRights != null ? this.mpUserRights.size() : 0;
	}

	/**
	 * notifies clients that the leased object meta data has changed, so they can invalidate their local caches.
	 */
	private void notifyClients() {
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_SECURITYCACHE);
		log.debug("Notified clients that leased object meta data changed.");
	}

	private void notifyUser(String username) {
		NuclosJMSUtils.sendMessage(username, JMSConstants.TOPICNAME_SECURITYCACHE);
		log.debug("Notified user " + username + " that security data has changed.");
	}
}	// class SecurityCache

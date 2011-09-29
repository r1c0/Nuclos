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
package org.nuclos.server.common.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for managing private tasks in the todolist.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(TaskFacadeLocal.class)
@Remote(TaskFacadeRemote.class)
@Transactional
@RolesAllowed("Login")

public class TaskFacadeBean extends NuclosFacadeBean implements TaskFacadeRemote, TaskFacadeLocal {


   /**
    * get all tasks (or only unfinished tasks)
    * @param sOwner task owner to get tasks for
    * @param bUnfinishedOnly get only unfinished tasks
    * @return collection of task value objects
    */
   @Override
   public Collection<TaskVO> getTasksByOwner(String sOwner, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
	   	final Collection<TaskVO> result = new HashSet<TaskVO>();
		Long userId = getUserId(sOwner);
		try {
			for (TaskVO taskVO : getTaskVOsByCondition(null)) {
				if (existOwnerForTask(IdUtils.toLongId(taskVO.getId()), userId)) {
					if ((!bUnfinishedOnly || taskVO.getCompleted() == null) && (iPriority.equals(0) || iPriority.equals(taskVO.getPriority()))){
						result.add(augmentDisplayNames(taskVO));
					}
				}
			}
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessException();
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessException();
		}

		return result;
	}

   /**
    * get all delegated tasks (or only own tasks)
    * @param sDelegator task delegator to get tasks for
    * @param bDelegatedOnly get only delegated tasks
    * @return collection of task value objects
    */
   @Override
   public Collection<TaskVO> getTasksByDelegator(String sDelegator, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
	   	final Collection<TaskVO> result = new HashSet<TaskVO>();
		Long delegatorId = getUserId(sDelegator);

		try {
			CollectableComparison cond = SearchConditionUtils.newMDReferenceComparison(
				MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TASKLIST),"taskdelegator", IdUtils.unsafeToId(delegatorId));

			for (TaskVO taskVO : getTaskVOsByCondition(cond)) {
				if ((!bUnfinishedOnly || taskVO.getCompleted() == null) && (iPriority.equals(0) || iPriority.equals(taskVO.getPriority()))){
					result.add(augmentDisplayNames(taskVO));
				}
			}
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessException();
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessException();
		}
		return result;
	}

   /**
    * get all tasks for specified visibility and owners
    *
    * @param owners - task owners to get tasks for
    * @param visibility
    * @return collection of task value objects
    */
   @Override
   public Collection<TaskVO> getTasksByVisibilityForOwners(List<String> owners, Integer visibility, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
      final Collection<TaskVO> result = new HashSet<TaskVO>();

//      List<Object> owners = CollectionUtils.transform(ownerIds, new Transformer<Integer,Object>(){
//		@Override
//		public Object transform(Integer i) {
//			return i;
//		}}
//      );
//
//	  CollectableIdListCondition idListCond = new CollectableIdListCondition(owners);
//	  final CollectableEntityField entityFieldDelegator = SearchConditionUtils.newMasterDataEntityField(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TASKLIST.getEntityName()), "delegator");
//	  ReferencingCollectableSearchCondition refCond = new ReferencingCollectableSearchCondition(entityFieldDelegator, idListCond);
//
	  CompositeCollectableSearchCondition condDelegator = new CompositeCollectableSearchCondition(LogicalOperator.OR);
	  for(String owner : owners){
		  condDelegator.addOperand(SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TASKLIST.getEntityName()),"delegator",ComparisonOperator.EQUAL,owner));
	  }
	  CollectableComparison condVisibility = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.TASKLIST.getEntityName()),"visibility",ComparisonOperator.EQUAL,visibility);
	  CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.AND);
	  cond.addOperand(condDelegator);
	  cond.addOperand(condVisibility);

  	  try {
		for (TaskVO taskVO : getTaskVOsByCondition(cond)) {
			if ((!bUnfinishedOnly || taskVO.getCompleted() == null) && (iPriority.equals(0) || iPriority.equals(taskVO.getPriority())))
				result.add(augmentDisplayNames(taskVO));
		}
	  }
	  catch (CommonPermissionException ex) {
		throw new NuclosBusinessException();
	  }
	  catch (CommonFinderException ex) {
		throw new NuclosBusinessException();
	  }
	  return result;
	}

   /**
    * get all tasks (or only unfinished tasks)
    * @param sOwner task owner/delegator to get tasks for
    * @param bUnfinishedOnly get only unfinished tasks
    * @return collection of task value objects
    */
   @Override
   public Collection<TaskVO> getTasks(String sUser, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
      final Collection<TaskVO> colltaskvobyowner = this.getTasksByOwner(sUser, bUnfinishedOnly, iPriority);
      final Collection<TaskVO> colltaskvobydelegator = this.getTasksByDelegator(sUser, bUnfinishedOnly, iPriority);

      Set<TaskVO> result = CollectionUtils.union(colltaskvobyowner, colltaskvobydelegator);

      final Collection<TaskVO> colltaskvobyvisibility = getPublicTasks(sUser, bUnfinishedOnly, iPriority);
      result = CollectionUtils.union(result, colltaskvobyvisibility);

      return result;
   }

   private Collection<TaskVO> getPublicTasks(String sUser, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
	  Collection<TaskVO> result = new HashSet<TaskVO>();
	  List<MasterDataVO> allowedUsersForRolesHierarchy = getMasterDataFacade().getUserHierarchy(sUser);
      List<String> allowedUsersForRolesHierarchyIds = CollectionUtils.transform(allowedUsersForRolesHierarchy, new Transformer<MasterDataVO,String>(){
		@Override
		public String transform(MasterDataVO mvo) {
			return mvo.getField("name", String.class);
		}}
      );
      result = this.getTasksByVisibilityForOwners(allowedUsersForRolesHierarchyIds, TaskVO.TaskVisibility.PUBLIC.getValue(), bUnfinishedOnly, iPriority);
	  return result;
   }

   /**
    * create a new task in the database
    * @param taskvo containing the task data
    * @return same task as value object
    */
   @Override
   public TaskVO create(TaskVO taskvo, Set<Long> stOwners) throws CommonValidationException, NuclosBusinessException, CommonPermissionException {
      TaskVO dbTaskVO = null;
		taskvo.validate();
		if(stOwners.size() == 0){
			Long userId = IdUtils.toLongId(SecurityCache.getInstance().getUserId(getCurrentUserName()));
			if(userId != null){
				stOwners.add(userId);
			}
		}
		if (taskvo.getDelegatorId() == null) {
			taskvo.setDelegator(getCurrentUserName());
		}
		try {
			MasterDataWithDependantsVO mdvo = MasterDataWrapper.wrapTaskVO(taskvo);
			MasterDataVO newMdvo = getMasterDataFacade().create(NuclosEntity.TASKLIST.getEntityName(), mdvo, mdvo.getDependants());
			dbTaskVO = getCompleteTaskVO(IdUtils.toLongId(newMdvo.getIntId()));
			setOwnersForTask(IdUtils.toLongId(dbTaskVO.getId()), stOwners);
		} catch (CommonCreateException ex) {
			throw new CommonFatalException(ex);
		} catch (CommonFinderException ex) {
			throw new CommonFatalException(ex);
		}
		return dbTaskVO;
	}

	/**
     * create a new task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task ids
	 */
	 @Override
	 public Collection<TaskVO> create(TaskVO taskvo, Set<Long> stOwners, boolean splitforowners) throws CommonValidationException, NuclosBusinessException, CommonPermissionException {
		List<TaskVO> listTaskVO = new ArrayList<TaskVO>();
		if(!splitforowners){
			listTaskVO.add(create(taskvo, stOwners));
		} else {
			for(Long currentOwnerId : stOwners){
				Set<Long> currentOwnerSet = new HashSet<Long>();
				currentOwnerSet.add(currentOwnerId);
				listTaskVO.add(create(taskvo, currentOwnerSet));
			}
		}
		return listTaskVO;
	 }

   /**
    * modify an existing task in the database
    * @param taskvo containing the task data
    * @return new task id
    */
   @Override
   public TaskVO modify(TaskVO taskvo, Set<Long> collOwners) throws CommonFinderException, CommonStaleVersionException, CommonValidationException, NuclosBusinessException {
		TaskVO newTaskVO = null;
		taskvo.validate();

		if (collOwners != null) {
			if ((collOwners.size() > 1) || (collOwners.size() == 1 && !getUserNamesById(collOwners).contains(getCurrentUserName()))) {
				taskvo.setDelegator(getCurrentUserName());
			}
		}
		try {
			MasterDataWithDependantsVO mdvo = MasterDataWrapper.wrapTaskVO(taskvo);
			getMasterDataFacade().modify(NuclosEntity.TASKLIST.getEntityName(), mdvo, mdvo.getDependants());
			Long iTaskId = IdUtils.toLongId(taskvo.getId());
			newTaskVO = getCompleteTaskVO(iTaskId);
			if (collOwners != null) {
				Set<Long> stNewOwners = new HashSet<Long>();
				for (Long iUserId : collOwners) {
					if (!this.getOwnerIdsByTask(iTaskId).contains(iUserId)) {
						stNewOwners.add(iUserId);
					}
				}
				setOwnersForTask(iTaskId, collOwners);
			}
		} catch (CommonPermissionException ex) {
			throw new NuclosBusinessException(ex);
		} catch(CommonCreateException ex) {
			throw new NuclosBusinessException(ex);
		} catch(CommonRemoveException ex) {
			throw new NuclosBusinessException(ex);
		}
		return newTaskVO;
	}

	/**
	 * modify an existing task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task ids
	 */
	 @Override
	 public Collection<TaskVO> modify(TaskVO taskvo, Set<Long> collOwners, boolean splitforowners) throws CommonFinderException, CommonStaleVersionException, CommonValidationException, NuclosBusinessException {
		List<TaskVO> listTaskVO = new ArrayList<TaskVO>();
		if(!splitforowners){
			listTaskVO.add(modify(taskvo, collOwners));
		} else {
			Long firstOwnerId = null;
			for(Long currentOwnerId : collOwners){
				Set<Long> currentOwnerSet = new HashSet<Long>();
				currentOwnerSet.add(currentOwnerId);
				if(firstOwnerId != null){
					try {
						listTaskVO.add(create(taskvo.cloneTaskVO(), currentOwnerSet));
					}
					catch(CommonPermissionException e) {
						throw new NuclosBusinessException(e);
					}
				} else {
					listTaskVO.add(modify(taskvo, currentOwnerSet));
					firstOwnerId = currentOwnerId;
				}
			}
		}
		return listTaskVO;
	}

	/**
	 * delete task from database
	 * @param taskvo containing the task data
	 */
	@Override
	public void remove(TaskVO taskvo) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, NuclosBusinessException {
		try {
			deleteOwnersForTask(taskvo.getId());
			getMasterDataFacade().remove(NuclosEntity.TASKLIST.getEntityName(),MasterDataWrapper.wrapTaskVO(taskvo),true);
		} catch (CommonPermissionException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	/**
	 * add owners fot the given task
	 * @param iTaskId
	 * @param stUserId
	 */
	private void setOwnersForTask(Long iTaskId, Set<Long> stUserId) {
		if (stUserId != null && stUserId.isEmpty()) {
			stUserId.add(getUserId(this.getCurrentUserName()));
		}
		for (Long iUserId : stUserId) {
			if (!this.existOwnerForTask(iTaskId, iUserId)) {
				DataBaseHelper.execute(DbStatementUtils.insertInto("T_UD_TASKOWNER",
					"INTID", new DbId(),
					"INTID_T_MD_USER", DbNull.escapeNull(iUserId, Long.class),
					"INTID_T_UD_TODOLIST", DbNull.escapeNull(iTaskId, Long.class),
					"DATCREATED", DbCurrentDateTime.CURRENT_DATETIME,
					"STRCREATED", getCurrentUserName(),
					"DATCHANGED", DbCurrentDateTime.CURRENT_DATETIME,
					"STRCHANGED", getCurrentUserName(),
					"INTVERSION", 1));
			}
		}
		for (Long iUserId : this.getOwnerIdsByTask(iTaskId)) {
			if (!stUserId.contains(iUserId)) {
				DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_UD_TASKOWNER", "INTID_T_UD_TODOLIST", iTaskId, "INTID_T_MD_USER", iUserId));
			}
		}
	}

	private void deleteOwnersForTask(Integer iTaskId) {
		DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_UD_TASKOWNER", "INTID_T_UD_TODOLIST", iTaskId));
	}


	@Override
	public List<String> getOwnerNamesByTask(TaskVO taskvo) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_UD_TASKOWNER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_MD_USER", Integer.class));
		query.where(builder.equal(t.baseColumn("INTID_T_UD_TODOLIST", Integer.class), taskvo.getId()));
		List<String> lstOwners = new ArrayList<String>();
		for (Integer intIdUser : DataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				lstOwners.add((String)getMasterDataFacade().get(NuclosEntity.USER.getEntityName(), intIdUser).getField("name"));
			} catch(Exception ex) {
				throw new CommonFatalException(ex);
			}
		}
		return lstOwners;
	}

	@Override
	public Set<Long> getOwnerIdsByTask(final Long iTaskId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_UD_TASKOWNER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID_T_MD_USER", Long.class));
		query.where(builder.equal(t.baseColumn("INTID_T_UD_TODOLIST", Integer.class), iTaskId));
		return new HashSet<Long>(DataBaseHelper.getDbAccess().executeQuery(query));
	}

	private boolean existOwnerForTask(Long iTaskId, Long iOwnerId) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_UD_TASKOWNER").alias(SystemFields.BASE_ALIAS);
		query.select(builder.count(t.baseColumn("INTID", Long.class)));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_USER", Long.class), iOwnerId),
			builder.equal(t.baseColumn("INTID_T_UD_TODOLIST", Long.class), iTaskId)));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query) > 0L;
	}

   @Override
   public Long getUserId(String sUserName) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Long.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		try {
			return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException ex) {
			error(ex);
			return null;
		}
	}

   @Override
   public List<String> getUserNamesById(Set<Long> stUserIds) {
	   List<String> lstUserNames = new ArrayList<String>();
	   for (Long iUserId : stUserIds) {
		   DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		   DbQuery<String> query = builder.createQuery(String.class);
		   DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		   query.select(t.baseColumn("STRUSER", String.class));
		   query.where(builder.equal(t.baseColumn("INTID", Integer.class), iUserId));
		   lstUserNames.addAll(DataBaseHelper.getDbAccess().executeQuery(query));
	   }
	   return lstUserNames;
   }

   @Override
   	public MasterDataVO getUserAsVO(Object oId) throws CommonFinderException{
	   return MasterDataFacadeHelper.getMasterDataCVOById(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), oId);
	}

	private Collection<TaskVO> getTaskVOsByCondition(CollectableSearchCondition cond) throws CommonFinderException, NuclosBusinessException, CommonPermissionException {
		return CollectionUtils.transform(getMasterDataFacade().getWithDependantsByCondition(NuclosEntity.TASKLIST.getEntityName(), cond),
			new Transformer<MasterDataWithDependantsVO, TaskVO>() {
			@Override
			public TaskVO transform(MasterDataWithDependantsVO mdvo) {
				return MasterDataWrapper.getTaskVO(mdvo, getObjectIdentifier(mdvo));
			}
		});
	}

	private TaskVO getCompleteTaskVO(Long iId) throws CommonFinderException, CommonPermissionException, NuclosBusinessException {
		MasterDataWithDependantsVO mdwdVO = getMasterDataFacade().getWithDependants(NuclosEntity.TASKLIST.getEntityName(), IdUtils.unsafeToId(iId));
		return augmentDisplayNames(MasterDataWrapper.getTaskVO(mdwdVO, getObjectIdentifier(mdwdVO)));
	}

	private TaskVO augmentDisplayNames(TaskVO taskVO) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		if (taskVO.getDelegator() != null) {
			DbQuery<DbTuple> queryOwner = builder.createTupleQuery();
			DbFrom userOwner = queryOwner.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
			DbFrom owner = userOwner.join("T_UD_TASKOWNER", JoinType.INNER).alias("t2").on("INTID", "INTID_T_MD_USER", Integer.class);
			queryOwner.where(builder.equal(owner.baseColumn("INTID_T_UD_TODOLIST", Integer.class), taskVO.getId()));
			queryOwner.multiselect(userOwner.baseColumn("STRFIRSTNAME", String.class), userOwner.baseColumn("STRLASTNAME", String.class));
			taskVO.setAssignees(StringUtils.join("; ", DataBaseHelper.getDbAccess().executeQuery(queryOwner, new UserDisplayNameTransformer())));
		}
		return taskVO;
	}

	private static class UserDisplayNameTransformer implements Transformer<DbTuple, String> {
		@Override
		public String transform(DbTuple t) {
			String firstName = t.get(0, String.class);
			String lastName = t.get(1, String.class);
			return lastName + (firstName != null ? ", " + firstName : "");
		}
	}

	private Map<Long, String> getObjectIdentifier(MasterDataWithDependantsVO mdwdVO) {
		Map<Long, String> result = new HashMap<Long, String>();
		for (EntityObjectVO md : mdwdVO.getDependants().getData(NuclosEntity.TASKOBJECT.getEntityName())) {
			final Long iObjectId = IdUtils.toLongId(md.getField("entityId", Integer.class));
			String entity = md.getField("entity", String.class);
			if (entity == null) {
				// backwards compatibility
				// TODO implement migration that inserts entity names to task-object-table and remove this
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<DbTuple> queryOwner = builder.createTupleQuery();
				DbFrom userOwner = queryOwner.from("T_UD_GENERICOBJECT").alias(SystemFields.BASE_ALIAS);
				queryOwner.multiselect(userOwner.baseColumn("INTID_T_MD_MODULE", Integer.class).alias("id"));
				queryOwner.where(builder.equal(userOwner.baseColumn("INTID", Integer.class).alias("moduleId"), iObjectId));
				List<DbTuple> resultsList = DataBaseHelper.getDbAccess().executeQuery(queryOwner.maxResults(2));
				if (resultsList.size() == 1) {
					Integer iModuleId = (Integer) resultsList.get(0).get("id");
					entity = MetaDataServerProvider.getInstance().getEntity(iModuleId.longValue()).getEntity();
				}
				else {
					DataBaseHelper.execute(DbStatementUtils.deleteFrom("T_UD_TODO_OBJECT", "INTID_T_UD_GENERICOBJECT", iObjectId));
				}
			}

			EntityObjectVO eObject = null;
			if (entity != null) {
				eObject = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(iObjectId.longValue());
			}
			if (eObject != null) {
				String identifier = (String) eObject.getFields().get(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField());
				if (identifier == null) {
					identifier = (String) eObject.getFields().get(MasterDataVO.FIELDNAME_NAME);
					if (identifier == null) {
						identifier = eObject.getEntity() + ": " + eObject.getId();
					}
				}
				result.put(iObjectId, identifier);
			} else {
				result.put(iObjectId, null);
			}
		}
		return result;
	}

	@Override
	public Collection<TaskVO> create(MasterDataWithDependantsVO mdvo, Set<Long> stOwners,
		boolean splitforowners) throws CommonValidationException,
		NuclosBusinessException, CommonPermissionException {
		return create(MasterDataWrapper.getTaskVO(mdvo, null), stOwners, splitforowners);
	}

	@Override
	public Collection<TaskVO> modify(MasterDataWithDependantsVO mdvo,
		Set<Long> collOwners, boolean splitforowners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException {
		return modify(MasterDataWrapper.getTaskVO(mdvo, null), collOwners, splitforowners);
	}
}

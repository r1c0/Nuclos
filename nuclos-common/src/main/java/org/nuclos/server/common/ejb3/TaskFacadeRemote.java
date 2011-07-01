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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

@Remote
public interface TaskFacadeRemote {

	/**
	 * get all tasks (or only unfinished tasks)
	 * @param sOwner task owner to get tasks for
	 * @param bUnfinishedOnly get only unfinished tasks
	 * @return collection of task value objects
	 */
	public abstract Collection<TaskVO> getTasksByOwner(String sOwner,
		boolean bUnfinishedOnly, Integer iPriority)
		throws NuclosBusinessException;

	/**
	 * get all delegated tasks (or only own tasks)
	 * @param sDelegator task delegator to get tasks for
	 * @param bDelegatedOnly get only delegated tasks
	 * @return collection of task value objects
	 */
	public abstract Collection<TaskVO> getTasksByDelegator(String sDelegator,
		boolean bUnfinishedOnly, Integer iPriority)
		throws NuclosBusinessException;

	/**
	 * get all tasks (or only unfinished tasks)
	 * @param sOwner task owner/delegator to get tasks for
	 * @param bUnfinishedOnly get only unfinished tasks
	 * @return collection of task value objects
	 */
	public abstract Collection<TaskVO> getTasks(String sUser,
		boolean bUnfinishedOnly, Integer iPriority)
		throws NuclosBusinessException;

//	/**
//	 * get task vo for a given task id
//	 * @param iId id of task
//	 * @return task value object
//	 */
//	public abstract TaskVO get(Integer iId) throws CommonFinderException,
//		CommonPermissionException, NuclosBusinessException;

	/**
	 * create a new task in the database
	 * @param taskvo containing the task data
	 * @return same task as value object
	 */
	public abstract TaskVO create(TaskVO taskvo, Set<Long> stOwners)
		throws CommonValidationException, NuclosBusinessException,
		CommonPermissionException;

	/**
	 * create a new task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task ids
	 */
	public abstract Collection<TaskVO> create(TaskVO taskvo, Set<Long> stOwners, boolean splitforowners)
		throws CommonValidationException, NuclosBusinessException,
		CommonPermissionException;

	/**
	 * create a new task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task ids
	 */
	public abstract Collection<TaskVO> create(MasterDataWithDependantsVO mdvo, Set<Long> stOwners, boolean splitforowners)
		throws CommonValidationException, NuclosBusinessException,
		CommonPermissionException;

	/**
	 * modify an existing task in the database
	 * @param mdvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task id
	 */
	public abstract Collection<TaskVO> modify(TaskVO taskvo, Set<Long> collOwners, boolean splitforowners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;

	/**
	 * modify an existing task in the database
	 * @param mdvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task id
	 */
	public abstract Collection<TaskVO> modify(MasterDataWithDependantsVO mdvo, Set<Long> collOwners, boolean splitforowners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;

	/**
	 * modify an existing task in the database
	 * @param taskvo containing the task data
	 * @return new task id
	 */
	public abstract TaskVO modify(TaskVO taskvo, Set<Long> collOwners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;

	/**
	 * delete task from database
	 * @param taskvo containing the task data
	 */
	public abstract void remove(TaskVO taskvo) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		NuclosBusinessException;

	public abstract List<String> getOwnerNamesByTask(TaskVO taskvo);

	public abstract Set<Long> getOwnerIdsByTask(final Long iTaskId);

	/**
	 * get user id by user name
	 * @param sUserName
	 * @return id user id
	 */
	public abstract List<String> getUserNamesById(Set<Long> stUserIds);

	/**
	 * get user as MasterDataVO
	 * @param oId user id
	 */
	public abstract MasterDataVO getUserAsVO(Object oId)
		throws CommonFinderException;

   /**
    * get all tasks for specified visibility and owners
    *
    * @param owners - task owners to get tasks for
    * @param visibility
    * @return collection of task value objects
    */
	public Collection<TaskVO> getTasksByVisibilityForOwners(List<String> owners,
		Integer visibility, boolean bUnfinishedOnly, Integer iPriority)
		throws NuclosBusinessException;

	/**
	 * get user id by user name
	 * @param sUserName
	 * @return id user id
	 */
	public abstract Long getUserId(String sUserName);

}

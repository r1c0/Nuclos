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

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.common.valueobject.TaskVO;

// @Local
public interface TaskFacadeLocal {

	/**
	 * create a new task in the database
	 * @param taskvo containing the task data
	 * @return same task as value object
	 */
	TaskVO create(TaskVO taskvo, Set<Long> stOwners)
		throws CommonValidationException, NuclosBusinessException,
		CommonPermissionException;

	/**
	 * create a new task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task ids
	 */
	Collection<TaskVO> create(TaskVO taskvo, Set<Long> stOwners, boolean splitforowners)
		throws CommonValidationException, NuclosBusinessException,
		CommonPermissionException;
	
	/**
	 * modify an existing task in the database
	 * @param taskvo containing the task data
	 * @return new task id
	 */
	TaskVO modify(TaskVO taskvo, Set<Long> collOwners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;

	/**
	 * modify an existing task in the database
	 * @param taskvo containing the task data
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return new task id
	 */
	Collection<TaskVO> modify(TaskVO taskvo, Set<Long> collOwners, boolean splitforowners)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;
	
	/**
	 * get user id by user name
	 * @param sUserName
	 * @return id user id
	 */
	Long getUserId(String sUserName);

	/**
	 * get all tasks for specified visibility and owners
	 * 
	 * @param owners - task owners to get tasks for
	 * @param visibility 
	 * @return collection of task value objects
	 */
	Collection<TaskVO> getTasksByVisibilityForOwners(List<String> owners, Integer visibility, boolean bUnfinishedOnly,
			Integer iPriority) throws NuclosBusinessException;

}

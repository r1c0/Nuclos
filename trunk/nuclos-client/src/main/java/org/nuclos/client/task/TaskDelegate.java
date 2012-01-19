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
package org.nuclos.client.task;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.common.ejb3.TaskFacadeRemote;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Business Delegate for <code>TaskFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class TaskDelegate {
	private TaskFacadeRemote facade;

	private TaskFacadeRemote getTaskFacade() throws NuclosFatalException {
		if (this.facade == null) {
			try {
				facade = ServiceLocator.getInstance().getFacade(TaskFacadeRemote.class);
			} catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return this.facade;
	}

	/**
	 * @return All / unfinished tasks for current owner / all owners (unsorted)
	 * @param sOwner
	 * @param bUnfinishedOnly
	 */
	public Collection<TaskVO> getOwnTasks(String sOwner, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
		try {
			return this.getTaskFacade().getTasksByOwner(sOwner, bUnfinishedOnly, iPriority);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return All / delegated tasks for current user (unsorted)
	 * @param sOwner
	 * @param bUnfinishedOnly
	 */
	public Collection<TaskVO> getDelegatedTasks(String sDelegator, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
		try {
			return this.getTaskFacade().getTasksByDelegator(sDelegator, bUnfinishedOnly, iPriority);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return All / delegated tasks for current user (unsorted)
	 * @param sOwner
	 * @param bUnfinishedOnly
	 */
	public Collection<TaskVO> getAllTasks(String sOwner, boolean bUnfinishedOnly, Integer iPriority) throws NuclosBusinessException {
		try {
			return this.getTaskFacade().getTasks(sOwner, bUnfinishedOnly, iPriority);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * creates a new task
	 * @param taskvo
	 * @return the created task
	 * @throws NuclosFatalException
	 */
	public TaskVO create(TaskVO taskvo, Set<Long> stOwners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().create(taskvo, stOwners);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * creates a new task
	 * @param taskvo
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return created tasks
	 * @throws NuclosFatalException
	 */
	public Collection<TaskVO> create(TaskVO taskvo, Set<Long> stOwners, boolean splitforowners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().create(taskvo, stOwners, splitforowners);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * creates a new task
	 * @param mdvo
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return created tasks
	 * @throws NuclosFatalException
	 */
	public Collection<TaskVO> create(MasterDataWithDependantsVO mdvo, Set<Long> stOwners, boolean splitforowners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().create(mdvo, stOwners, splitforowners);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * updates the given task
	 * @param taskvo
	 * @return the updated task
	 */
	public TaskVO update(TaskVO taskvo, Set<Long> stOwners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().modify(taskvo, stOwners);
		} catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	/**
	 * updates the given task and splits it (if true)
	 * @param taskvo
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return updated/created tasks
	 */
	public Collection<TaskVO> update(TaskVO taskvo, Set<Long> stOwners, boolean splitforowners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().modify(taskvo, stOwners, splitforowners);
		} catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	/**
	 * updates the given task and splits it (if true)
	 * @param mdvo
	 * @param splitforowners true/false - shows if this task will be transformed to tasks for each owner
	 * @return updated/created tasks
	 */
	public Collection<TaskVO> update(MasterDataWithDependantsVO mdvo, Set<Long> stOwners, boolean splitforowners) throws CommonBusinessException {
		try {
			return this.getTaskFacade().modify(mdvo, stOwners, splitforowners);
		} catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	/**
	 * removes the given task from the database
	 * @param taskvo
	 */
	public void remove(TaskVO taskvo) throws CommonBusinessException {
		try {
			this.getTaskFacade().remove(taskvo);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * marks the given task as finished by setting the "completed" property to the current date.
	 * @param taskvo
	 */
	public TaskVO complete(TaskVO taskvo) throws CommonBusinessException {
		final Date dateToday = new Date();
		taskvo.setCompleted(dateToday);
		return this.update(taskvo, null);
	}

	/**
	 * marks the given task as unfinished by clearing the "completed" property.
	 * @param taskvo
	 */
	public TaskVO uncomplete(TaskVO taskvo) throws CommonBusinessException {
		taskvo.setCompleted(null);
		return this.update(taskvo, null);
	}

	public List<String> getOwnerNamesByTask(TaskVO taskvo) {
		try {
			return this.getTaskFacade().getOwnerNamesByTask(taskvo);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public Set<Long> getOwnerIdsByTask(Long iTaskId) {
		try {
			return this.getTaskFacade().getOwnerIdsByTask(iTaskId);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public Long getUserId(String sUserName) throws CommonFinderException{
		try {
			return this.getTaskFacade().getUserId(sUserName);
		} catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}

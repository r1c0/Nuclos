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

import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;

@Remote
public interface TimelimitTaskFacadeRemote {

	/**
	 * get all timelimit tasks (or only unfinished tasks)
	 * @param bOnlyUnfinishedTasks get only unfinished tasks
	 * @return collection of task value objects
	 */
	public abstract Collection<TimelimitTaskVO> getTimelimitTasks(
		boolean bOnlyUnfinishedTasks);

	/**
	 * create a new TimelimitTask in the database
	 * @return same task as value object
	 */
	public abstract TimelimitTaskVO create(TimelimitTaskVO voTimelimitTask)
		throws CommonValidationException, NuclosBusinessException;

	/**
	 * modify an existing TimelimitTask in the database
	 * @param voTimelimitTask containing the task data
	 * @return new task id
	 */
	public abstract TimelimitTaskVO modify(TimelimitTaskVO voTimelimitTask)
		throws CommonFinderException, CommonStaleVersionException,
		CommonValidationException, NuclosBusinessException;

	/**
	 * delete TimelimitTask from database
	 * @param voTimelimitTask containing the task data
	 */
	public abstract void remove(TimelimitTaskVO voTimelimitTask)
		throws CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, NuclosBusinessException;

}

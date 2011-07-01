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
package org.nuclos.client.timelimit;

import java.util.Collection;
import java.util.Date;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.common.ejb3.TimelimitTaskFacadeRemote;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;

/**
 * Business Delegate for <code>TimelimitTaskFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */

public class TimelimitTaskDelegate {
	private TimelimitTaskFacadeRemote facade = null;

	private TimelimitTaskFacadeRemote getTimelimitTaskFacade() throws NuclosFatalException {
		if (this.facade == null)
			facade = ServiceLocator.getInstance().getFacade(TimelimitTaskFacadeRemote.class);
		return this.facade;
	}

	/**
	 * @return Collection&lt;TimelimitTaskVO&gt;. All / unfinished tasks for current owner / all owners (unsorted)
	 * @param bUnfinishedOnly
	 */
	public Collection<TimelimitTaskVO> getTimelimitTasks(boolean bUnfinishedOnly) {
		try {
			return this.getTimelimitTaskFacade().getTimelimitTasks(bUnfinishedOnly);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * creates a new task
	 * @param taskvo
	 * @return the created task
	 * @throws org.nuclos.common.NuclosFatalException
	 */
	public TimelimitTaskVO create(TimelimitTaskVO taskvo) throws CommonBusinessException {
		try {
			return this.getTimelimitTaskFacade().create(taskvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * updates the given task
	 * @param taskvo
	 * @return the updated task
	 */
	public TimelimitTaskVO update(TimelimitTaskVO taskvo) throws CommonBusinessException {
		try {
			return this.getTimelimitTaskFacade().modify(taskvo);
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(ex);
		}
	}

	/**
	 * removes the given task from the database
	 * @param taskvo
	 */
	public void remove(TimelimitTaskVO taskvo) throws CommonBusinessException {
		try {
			this.getTimelimitTaskFacade().remove(taskvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * marks the given task as finished by setting the "completed" property to the current date.
	 * @param taskvo
	 */
	public TimelimitTaskVO finish(TimelimitTaskVO taskvo) throws CommonBusinessException {
		final Date dateToday = new Date();
		taskvo.setCompleted(dateToday);
		return this.update(taskvo);
	}

	/**
	 * marks the given task as unfinished by clearing the "completed" property.
	 * @param taskvo
	 */
	public TimelimitTaskVO unfinish(TimelimitTaskVO taskvo) throws CommonBusinessException {
		taskvo.setCompleted(null);
		return this.update(taskvo);
	}

}	// class TaskDelegate

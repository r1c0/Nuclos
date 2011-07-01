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
package org.nuclos.client.report.reportrunner;

import java.util.Date;
import java.util.Observable;
import java.util.concurrent.Future;

/**
 * provides information about a background process.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public interface BackgroundProcessInfo {

	public static enum Status {
		NOTRUNNING,
		RUNNING,
		DONE,
		CANCELLED,
		ERROR;

		/**
		 * @return Is this state representing a finished process?
		 */
		public boolean isFinished() {
			return this == DONE || this == ERROR  || this == CANCELLED;
		}

	}	// enum State

	/**
	 * @return the time when the process was started.
	 */
	Date getStartedAt();

	/**
	 * @return the process' status (see constants above).
	 */
	Status getStatus();

	/**
	 * @return the name of the job.
	 */
	String getJobName();

	/**
	 * @return a message returned from the process (describing its result).
	 */
	String getMessage();

	/**
	 * @return the process future.
	 */
	Future<?> getProcessFuture();
	
	/**
	 * try to cancel current process - delegates cancel to Future.
	 */
	void cancelProzess();

	/**
	 * @param Observable object for notifing observers.
	 */
	void addObservable(Observable observable);
	
}	// interface BackgroundProcessInfo

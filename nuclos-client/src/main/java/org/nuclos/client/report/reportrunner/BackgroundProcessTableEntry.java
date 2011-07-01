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

import org.apache.log4j.Logger;

/**
 * class which represents an entry in the BackgroundProcessStatusPanel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class BackgroundProcessTableEntry implements BackgroundProcessInfo {

	private final static Logger log = Logger.getLogger(BackgroundProcessTableEntry.class);

	private final String sJobName;
	private final Date dateStartedAt;
	private Status status;
	private String sMessage;
	private Future<?> processFuture;
	private Observable observable = null;

	public BackgroundProcessTableEntry(String sJobName, Status status, Date dateStartedAt) {
		this.sJobName = sJobName;
		this.dateStartedAt = dateStartedAt;
		this.status = status;
		this.processFuture = null;
	}

	public BackgroundProcessTableEntry(String sJobName, Status status, Date dateStartedAt, Future<?> processFuture) {
		this.sJobName = sJobName;
		this.dateStartedAt = dateStartedAt;
		this.status = status;
		this.processFuture = processFuture;
	}
	
	@Override
	public Date getStartedAt() {
		return dateStartedAt;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		if(this.observable != null){
			this.observable.notifyObservers();
		}
	}

	@Override
	public String getJobName() {
		return sJobName;
	}

	@Override
	public String getMessage() {
		return sMessage;
	}

	public void setMessage(String sMessage) {
		this.sMessage = sMessage;
	}

	@Override
	public Future<?> getProcessFuture() {
		return processFuture;
	}

	@Override
	public void cancelProzess(){
		if(this.processFuture != null){
			boolean cancelled = this.processFuture.cancel(true);
			log.debug("cancelProzess>>>>>>>>>> cancelled future: "+cancelled);
		} 
	}

	@Override
	public void addObservable(Observable observable){
		this.observable = observable;
	}
	
}	// class BackgroundProcessTableEntry

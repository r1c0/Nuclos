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
package org.nuclos.common;

import java.io.Serializable;

/**
 * General class for progress notifications (used in file import for instance)
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ProgressNotification implements Serializable {

	public final static int RUNNING = 0;
	public final static int STOPPED = 1;
	public final static int FINISHED = 2;

	private final String message;

	private final Integer progressMinimum;

	private final Integer progressMaximum;

	private final Integer value;

	private final int state;

	public ProgressNotification(String message, Integer progressMinimum, Integer progressMaximum, Integer value) {
	    this(message, progressMinimum, progressMaximum, value, ProgressNotification.RUNNING);
    }

	public ProgressNotification(String message, Integer progressMinimum, Integer progressMaximum, Integer value, int state) {
		this.message = message;
	    this.progressMinimum = progressMinimum;
	    this.progressMaximum = progressMaximum;
	    this.value = value;
	    this.state = state;
    }

	public String getMessage() {
    	return message;
    }

	public Integer getProgressMinimum() {
    	return progressMinimum;
    }

	public Integer getProgressMaximum() {
    	return progressMaximum;
    }

	public Integer getValue() {
    	return value;
    }

	public int getState() {
		return this.state;
	}

	@Override
    public String toString() {
	    return "ProgressNotification [message=" + message
	        + ", progressMaximum=" + progressMaximum + ", progressMinimum="
	        + progressMinimum + ", value=" + value + "]";
    }
}

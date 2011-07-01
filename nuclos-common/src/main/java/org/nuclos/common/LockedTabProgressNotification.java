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
 * class for progress notifications on locked tabs
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class LockedTabProgressNotification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message;
	private final Integer percent;

	public LockedTabProgressNotification(String message, Integer percent) {
		this.message = message;
		this.percent = percent;
    }

	public String getMessage() {
    	return message;
	}

	public Integer getPercent() {
		return percent;
	}

	@Override
    public String toString() {
	    return "LockedTabProgressNotification [message=" + message + " " + percent + "%]";
    }
}

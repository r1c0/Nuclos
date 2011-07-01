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
package org.nuclos.server.common;

/**
 * 
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 00.01.000
 */
public interface InstanceConstants {
	
	public final static int STATUS_NOT_STARTED = -1;
	public final static int STATUS_RUNNING_INTIME = 1;
	public final static int STATUS_RUNNING_DELAYED = 3;
	public final static int STATUS_ENDED_INTIME = 2;
	public final static int STATUS_ENDED_DELAYED = 4;
	
	public final static int STATE_IS_CURRENT = 100;
	public final static int STATE_IS_NOT_CURRENT = 101;
}

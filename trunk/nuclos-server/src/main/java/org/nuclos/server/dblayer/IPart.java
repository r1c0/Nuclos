//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer;

import org.nuclos.common.dal.DalCallResult;


/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public interface IPart {
	
	public static enum NextPartHandling {
		
		ONLY_IF_THIS_FAILS,
		ONLY_IF_THIS_SUCCEEDS,
		ALWAYS;
		
	}
	
	EBatchType getBatchType();
	
	NextPartHandling getNextPartHandling();

	/**
	 * Returns if the execution of the last statement was successful.
	 */
	boolean process(DalCallResult result, IPreparedStringExecutor ex);
	
}

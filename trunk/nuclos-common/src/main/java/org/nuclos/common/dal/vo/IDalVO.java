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
package org.nuclos.common.dal.vo;

import org.nuclos.common.HasId;

/**
 * Base interface for all value objects (VOs) transfered between client and server. 
 * <p>
 * On the server side, these VOs get persisted into the DB.
 * </p> 
 */
public interface IDalVO extends HasId<Long> {
	
	int STATE_NEW = 1;
	
	int STATE_UPDATED = 2;
	
	int STATE_REMOVED = 3;
	
		
	void setId(Long id);
	
	
	void flagNew();
	
	void flagUpdate();
	
	void flagRemove();

	boolean isFlagNew();

	boolean isFlagUpdated();

	boolean isFlagRemoved();
	
	
	String processor();
	
	void processor(String p);
	
}

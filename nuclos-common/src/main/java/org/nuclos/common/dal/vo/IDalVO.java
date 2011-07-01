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

import java.util.Map;


public interface IDalVO {
	
	public static final int STATE_NEW = 1;
	
	public static final int STATE_UPDATED = 2;
	
	public static final int STATE_REMOVED = 3;
	
	
	public Long getId();
	
	public void setId(Long id);
	
	
	public void flagNew();
	
	public void flagUpdate();
	
	public void flagRemove();

	public boolean isFlagNew();

	public boolean isFlagUpdated();

	public boolean isFlagRemoved();
	
	
	public boolean hasFields();
	
	public void initFields(int maxFieldCount, int maxFieldIdCount);
	
	public Map<String, Object> getFields();
	
	public Map<String, Long> getFieldIds();
	
	public String processor();
	
	public void processor(String p);
	
}

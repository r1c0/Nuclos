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
package org.nuclos.server.dbtransfer;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.server.dbtransfer.content.INucletContent;

public class NucletContentProcessor {
	
	private final INucletContent nc;
	private final EntityObjectVO ncObject;
	
	private NucletContentUID uid;
	private boolean createUID = false;
	private boolean updateUID = false;
	private boolean deleteUID = false;
	
	public NucletContentProcessor(INucletContent nc, EntityObjectVO ncObject) {
		super();
		this.nc = nc;
		this.ncObject = ncObject;
	}
	
	public void createUIDRecord(NucletContentUID uid) {
		createUID = true;
		this.uid = uid;
	}
	
	public void updateUIDRecord(NucletContentUID uid) {
		updateUID = true;
		this.uid = uid;
	}
	
	public void deleteUIDRecord(NucletContentUID uid) {
		deleteUID = true;
		this.uid = uid;
	}

	public INucletContent getNC() {
		return nc;
	}

	public EntityObjectVO getNcObject() {
		return ncObject;
	}

	public NucletContentUID getUID() {
		return uid;
	}

	public boolean isCreateUID() {
		return createUID;
	}

	public boolean isUpdateUID() {
		return updateUID;
	}
	
	public boolean isDeleteUID() {
		return deleteUID;
	}

	public NuclosEntity getEntity() {
		return nc.getEntity();
	}
	
}

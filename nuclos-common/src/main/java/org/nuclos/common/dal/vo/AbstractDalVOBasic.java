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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractDalVOBasic implements IDalVO, Serializable {
	private static final long serialVersionUID = 1012920874365879641L;
	
	private String processor;
	private int state;
	
	private Long id;
	
	public AbstractDalVOBasic() {
		super();
	}
	
	public AbstractDalVOBasic(AbstractDalVOBasic dalVO) {
		super();
		this.id = dalVO.getId();
	}

	@Override
	public final String processor() {
		return processor;
	}
	
	@Override
	public final void processor(String p) {
		processor = p;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
		
	@Override
	public final void flagNew() {
		this.state = STATE_NEW;
	}
	
	@Override
	public final void flagUpdate() {
		this.state = STATE_UPDATED;
	}
	
	@Override
	public final void flagRemove() {
		this.state = STATE_REMOVED;
	}

	@Override
	public final boolean isFlagNew() {
		return this.state == STATE_NEW;
	}

	@Override
	public final boolean isFlagUpdated() {
		return this.state == STATE_UPDATED;
	}

	@Override
	public final boolean isFlagRemoved() {
		return this.state == STATE_REMOVED;
	}	
	
}

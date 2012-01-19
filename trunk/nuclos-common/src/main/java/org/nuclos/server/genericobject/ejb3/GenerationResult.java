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
package org.nuclos.server.genericobject.ejb3;

import java.io.Serializable;
import java.util.Collection;

import org.nuclos.common.dal.vo.EntityObjectVO;

public class GenerationResult implements Serializable {

	private static final long serialVersionUID = -7029927784968415450L;

	private final Collection<Long> sourceIds;
	private final EntityObjectVO generatedObject;
	private final String error;

	public GenerationResult(Collection<Long> sourceIds, EntityObjectVO generatedObject, String error) {
		super();
		this.sourceIds = sourceIds;
		this.generatedObject = generatedObject;
		this.error = error;
	}

	public Collection<Long> getSourceIds() {
		return sourceIds;
	}

	public EntityObjectVO getGeneratedObject() {
		return generatedObject;
	}

	public String getError() {
		return error;
	}
}

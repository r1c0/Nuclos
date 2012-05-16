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
package org.nuclos.common.validation;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FieldValidationError implements Serializable {

	private final String entity;
	private final String field;
	private final String message;
	
	public FieldValidationError(String entity, String field, String message) {
		super();
		this.entity = entity;
		this.field = field;
		this.message = message;
	}

	public String getEntity() {
		return entity;
	}

	public String getField() {
		return field;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldValidationError other = (FieldValidationError) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		}
		else if (!entity.equals(other.entity))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		}
		else if (!field.equals(other.field))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		}
		else if (!message.equals(other.message))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldValidationError [entity=" + entity + ", field=" + field + ", message=" + message + "]";
	}
}

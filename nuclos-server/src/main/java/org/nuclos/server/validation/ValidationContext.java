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
package org.nuclos.server.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nuclos.common.validation.FieldValidationError;

public class ValidationContext {
	
	private final Set<String> errors = new HashSet<String>();
	
	private final Set<FieldValidationError> fielderrors = new HashSet<FieldValidationError>();
	
	public void addError(String error) {
		errors.add(error);
	}

	public void addFieldError(String entity, String field, String error) {
		fielderrors.add(new FieldValidationError(entity, field, error));
	}
	
	public boolean hasErrors() {
		return errors.size() > 0 || fielderrors.size() > 0;
	}
	
	public Set<String> getErrors() {
		return Collections.unmodifiableSet(errors);
	}
	
	public Set<FieldValidationError> getFieldErrors() {
		return Collections.unmodifiableSet(fielderrors);
	}
}

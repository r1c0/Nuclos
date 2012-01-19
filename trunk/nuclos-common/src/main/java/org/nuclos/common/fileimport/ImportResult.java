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
package org.nuclos.common.fileimport;

import org.nuclos.common2.KeyEnum;

/**
 * Importresults for file imports.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public enum ImportResult implements KeyEnum<String> {

	/** No errors, everything imported */
	OK("Ok"),

	/** Errors occured, but import was started without transaction */
	INCOMPLETE("Incomplete"),

	/** Error occured and import was stopped */
	ERROR("Error");

	private final String value;

	private ImportResult(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}	// enum ImportMode

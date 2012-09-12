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
import org.nuclos.common2.Localizable;

/**
 * Import charsets for file imports.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public enum ImportChartset implements KeyEnum<String>, Localizable {

	US_ASCII("US-ASCII", "import.charset.us_ascii"),
	ISO_8859_1("ISO-8859-1", "import.charset.iso_8859_1"),
	CP_1252("CP-1252", "import.charset.cp_1252"),
	UTF_8("UTF-8", "import.charset.utf_8"),
	UTF_16BE("UTF-16BE", "import.charset.utf_16be"),
	UTF_16LE("UTF-16LE", "import.charset.utf_16le"),
	UTF_16("UTF-16", "import.charset.utf_16");

	private final String value;
	private final String resourceId;

	private ImportChartset(String value, String resourceId) {
		this.value = value;
		this.resourceId = resourceId;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}
}	// enum ImportMode

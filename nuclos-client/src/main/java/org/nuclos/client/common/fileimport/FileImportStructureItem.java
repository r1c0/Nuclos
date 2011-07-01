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
package org.nuclos.client.common.fileimport;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.fileimport.CommonParseException;
import org.nuclos.common2.fileimport.parser.FileImportParserFactory;
import org.nuclos.common.NuclosFatalException;

/**
 * This class represents a field of an import structure definition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class FileImportStructureItem {
	private final String sFieldName;
	private final Class<?> cls;
	private final String sFormat;

	public FileImportStructureItem(String sFieldName, String sClassName, String sFormat) {
		this.sFieldName = sFieldName;
		try {
			this.cls = Class.forName(sClassName);
		}
		catch (ClassNotFoundException ex) {
			throw new NuclosFatalException(ex);
		}
		this.sFormat = sFormat;
	}

	public String getFieldName() {
		return sFieldName;
	}

	public Class<?> getJavaClass() {
		return this.cls;
	}

	public Object parse(String sValue) throws CommonParseException {
		try {
			return FileImportParserFactory.getInstance().parse(this.getJavaClass(), sValue, sFormat);
		}
		catch (CommonParseException ex) {
			throw new CommonParseException(StringUtils.getParameterizedExceptionMessage("FileImportStructureItem.1", this.getFieldName(), sValue), ex);
				//"Beim Parsen des Felds \"" + this.getFieldName() + "\" ist ein Fehler aufgetreten.\nDer Wert \"" + sValue + "\" ist ung\u00fcltig.", ex);
		}
	}

}	// class FileImportStructureItem

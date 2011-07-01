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
package org.nuclos.common2;

import org.nuclos.common2.exception.CommonFatalException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Property file reader.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 * @deprecated Use ResourceBundle.getBundle()
 * @see ResourceBundle#getBundle(String)
 */
@Deprecated
public class PropertyList {
	private PropertyResourceBundle bundle = null;

	public PropertyList(String sFileName) throws CommonFatalException {
		try {
			FileInputStream fis = new FileInputStream(sFileName);
			this.bundle = new PropertyResourceBundle(fis);
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getProperty(String sName) {
		return this.bundle.getString(sName);
	}
}

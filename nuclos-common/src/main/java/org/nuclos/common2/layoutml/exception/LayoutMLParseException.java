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
package org.nuclos.common2.layoutml.exception;

import java.io.Serializable;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Exception occured during parsing of a LayoutML document.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LayoutMLParseException extends LayoutMLException {

	/**
	 * @deprecated Use constructor with locator instead. (Thomas Pasch)
	 */
	public LayoutMLParseException(SAXParseException ex) {
		super(ex, new MyLocator(ex));
	}
	
	public LayoutMLParseException(SAXParseException ex, Locator loc) {
		super(ex, loc);
	}

	private static class MyLocator implements Locator, Serializable {
		private final SAXParseException ex;

		MyLocator(SAXParseException ex) {
			this.ex = ex;
		}

		@Override
		public String getPublicId() {
			return ex.getPublicId();
		}

		@Override
		public String getSystemId() {
			return ex.getSystemId();
		}

		@Override
		public int getColumnNumber() {
			return ex.getColumnNumber();
		}

		@Override
		public int getLineNumber() {
			return ex.getLineNumber();
		}
	}

}  // class LayoutMLParseException

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

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * An exception concerning the LayoutML. Generally nonfatal.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LayoutMLException extends CommonBusinessException implements Locator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Locator locator;
	
	private static class MyLocator implements Locator, Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final String publicId;
		
		private final String systemId;
		
		private final int lineNumber;
		
		private final int columnNumber;
		
		public MyLocator(Locator locator) {
			this.publicId = locator.getPublicId();
			this.systemId = locator.getSystemId();
			this.lineNumber = locator.getLineNumber();
			this.columnNumber = locator.getColumnNumber();
		}

		@Override
		public String getPublicId() {
			return publicId;
		}

		@Override
		public String getSystemId() {
			return systemId;
		}

		@Override
		public int getLineNumber() {
			return lineNumber;
		}

		@Override
		public int getColumnNumber() {
			return columnNumber;
		}
		
	}

	public LayoutMLException(Throwable tCause, Locator locator) {
		super(tCause.toString(), tCause);
		if (locator instanceof Serializable) {
			this.locator = locator;
		}
		else {
			this.locator = new MyLocator(locator);
		}
	}

	/**
	 * @return a user-friendly error message
	 */
	@Override
	public String getMessage() {
		return this.getLocatorMessage() + (this.getCause() != null ? this.getCause().getMessage() : "");
	}

	/**
	 * @return a localized user-friendly error message
	 */
	@Override
	public String getLocalizedMessage() {
		return this.getLocatorMessage() + (this.getCause() != null ? this.getCause().getLocalizedMessage() : "");
	}

	/**
	 * @return the line number at which the error occured
	 */
	@Override
	public int getLineNumber() {
		return this.locator.getLineNumber();
	}

	/**
	 * @return the column number at which the error occured
	 */
	@Override
	public int getColumnNumber() {
		return this.locator.getColumnNumber();
	}

	/**
	 * @return the public id of the DTD
	 */
	@Override
	public String getPublicId() {
		return this.locator.getPublicId();
	}

	/**
	 * @return the system id of the DTD
	 */
	@Override
	public String getSystemId() {
		return this.locator.getSystemId();
	}

	/**
	 * @return a user-friendly error message containing information from the given locator
	 */
	private static String getLocatorMessage(Locator locator) {
		final StringBuilder result = new StringBuilder();
		result.append("Error while parsing the LayoutML in row ");
		result.append(locator.getLineNumber());

		if (locator.getColumnNumber() >= 0) {
			result.append(", column ");
			result.append(locator.getColumnNumber());
		}
		result.append(":\n");
		if (locator.getSystemId() != null) {
			result.append("System ID: ");
			result.append(locator.getSystemId());
			result.append("\n");
		}
		if (locator.getPublicId() != null) {
			result.append("Public ID: ");
			result.append(locator.getPublicId());
			result.append("\n");
		}
		return result.toString();
	}

	private String getLocatorMessage() {
		return getLocatorMessage(locator);
	}
	
}  // class LayoutMLException

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
package org.nuclos.common;

import org.nuclos.common2.StringUtils;

/**
 * Exception: An attribute with the given name was not found in the application.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class NuclosAttributeNotFoundException extends NuclosFatalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param sAttributeName
	 */
	public NuclosAttributeNotFoundException(String sAttributeName) {
		super(StringUtils.getParameterizedExceptionMessage("nucleus.atribute.not.found.exception.1", sAttributeName));
			//"Ein Attribut mit dem Namen \"" + sAttributeName + "\" ist nicht vorhanden.");
	}

	/**
	 * @param iAttributeId
	 */
	public NuclosAttributeNotFoundException(Integer iAttributeId) {
		super(StringUtils.getParameterizedExceptionMessage("nucleus.atribute.not.found.exception.2", iAttributeId));
			//"Ein Attribut mit der Id " + iAttributeId + " ist nicht vorhanden.");
	}

}	// class NuclosAttributeNotFoundException

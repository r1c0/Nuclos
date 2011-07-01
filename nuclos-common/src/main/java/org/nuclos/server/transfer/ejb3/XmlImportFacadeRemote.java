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
package org.nuclos.server.transfer.ejb3;

import java.io.IOException;

import javax.ejb.CreateException;
import javax.ejb.Remote;

import org.dom4j.DocumentException;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common.NuclosBusinessException;

@Remote
public interface XmlImportFacadeRemote {

	/**
	 * Import Method
	 *
	 * @param importFile
	 *            zipfile with content to import
	 * @throws IOException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws CreateException
	 * @throws CommonFinderException
	 * @throws ElisaBusinessException
	 * @jboss.method-attributes read-only = "true"
	 */
	public abstract void xmlImport(String sEntityName,
		org.nuclos.common2.File importFile) throws IOException, DocumentException,
		CommonCreateException, CommonPermissionException, CreateException,
		NuclosBusinessException, CommonFinderException;

}

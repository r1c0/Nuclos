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
package org.nuclos.server.fileimport.ejb3;

import javax.ejb.Remote;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Remote business interface for import execution control.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
@Remote
public interface ImportFacadeRemote {

	/**
	 * Create an import structure.
	 *
	 * @param importStructure The import structure as <code>MasterDataVO</code>
	 * @return The created import structure as <code>MasterDataVO</code>
	 */
	MasterDataVO createImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException;

	/**
	 * Modify an existing import structure.
	 *
	 * @param importStructure The import structure as <code>MasterDataVO</code>
	 * @return The import structure's id.
	 */
	Object modifyImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException;

	/**
	 * Remove an existing import structure.
	 *
	 * @param job The import structure to remove.
	 */
	void removeImportStructure(MasterDataVO importStructure) throws CommonBusinessException;

	/**
	 * Create an file import definition.
	 *
	 * @param fileImport The file import definition as <code>MasterDataVO</code>
	 * @return The created file import definition as <code>MasterDataVO</code>
	 */
	MasterDataVO createFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException;

	/**
	 * Modify an existing file import definition.
	 *
	 * @param fileImport The file import definition as <code>MasterDataVO</code>
	 * @return The file import definition's id.
	 */
	Object modifyFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException;

	/**
	 * Remove an existing file import definition.
	 *
	 * @param fileImport The file import definition to remove.
	 */
	void removeFileImport(MasterDataVO fileImport) throws CommonBusinessException;

	/**
	 * Start an import from clientside.
	 *
	 * @param importfileId
	 * @return
	 * @throws NuclosFileImportException
	 */
	public String doImport(Integer importfileId) throws NuclosFileImportException;

	/**
	 * Returns the correlation id of an import (if running)
	 *
	 * @param importfileId
	 * @return
	 */
	public String getImportCorrelationId(Integer importfileId);

	/**
	 * Stop/Interrupt a running import
	 *
	 * @param importfileId
	 * @throws NuclosFileImportException
	 */
	public void stopImport(Integer importfileId) throws NuclosFileImportException;

}

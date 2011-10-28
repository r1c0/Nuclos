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
/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:hartmut.beckschulze@novabit.de">Hartmut Beckschulze</a>
 * @version 01.00.00
 * NUCLEUSINT-1160 needed for accessing via rule interface
 */
package org.nuclos.server.fileimport.ejb3;

import java.util.List;

import org.nuclos.common.fileimport.ImportResult;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.fileimport.FileImportResult;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.fileimport.AbstractImport;
import org.nuclos.server.fileimport.ImportContext;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Local business interface for management and executing file imports.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Local
public interface ImportFacadeLocal {

	/**
	 * Create an file import definition.
	 *
	 * @param fileImport The file import definition as <code>MasterDataVO</code>
	 * @return The created file import definition as <code>MasterDataVO</code>
	 */
	MasterDataVO createFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException;

	/**
	 * Main method to start a file import.
	 *
	 * @param context context information with import file id, correlation id and user information
	 * @return
	 * @throws NuclosFileImportException
	 */
	List<FileImportResult> doImport(ImportContext context) throws NuclosFileImportException;

	/**
	 * Internal use only (used by <code>doImport(ImportContext context)</code> for transaction control).
	 *
	 * @param instance
	 * @return
	 * @throws NuclosFileImportException
	 */
	List<FileImportResult> doAtomicImport(AbstractImport instance) throws NuclosFileImportException;

	/**
	 * Internal use only (used by <code>doImport(ImportContext context)</code> for transaction control).
	 *
	 * @param instance
	 * @return
	 * @throws NuclosFileImportException
	 */
	List<FileImportResult> doNonAtomicImport(AbstractImport instance) throws NuclosFileImportException;

	void setImportResult(Integer importfileId, ImportResult result, String summary);

}

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
package org.nuclos.client.fileimport;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.fileimport.ejb3.ImportFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Delegate class for import management and execution.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportDelegate {

	private static ImportDelegate singleton;

	private final ImportFacadeRemote importExecutionFacade;

	private ImportDelegate() {
		try {
			this.importExecutionFacade = ServiceLocator.getInstance().getFacade(ImportFacadeRemote.class);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public static synchronized ImportDelegate getInstance() {
		if (singleton == null) {
			singleton = new ImportDelegate();
		}
		return singleton;
	}

	public MasterDataVO createImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		return importExecutionFacade.createImportStructure(importStructure);
	}

	public Object modifyImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		return importExecutionFacade.modifyImportStructure(importStructure);
	}

	public void removeImportStructure(MasterDataVO importStructure) throws CommonBusinessException {
		importExecutionFacade.removeImportStructure(importStructure);
	}

	public MasterDataVO createFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		return importExecutionFacade.createFileImport(fileImport);
	}

	public Object modifyFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		return importExecutionFacade.modifyFileImport(fileImport);
	}

	public void removeFileImport(MasterDataVO fileImport) throws CommonBusinessException {
		importExecutionFacade.removeFileImport(fileImport);
	}

	public String doImport(Integer importfileId) throws NuclosFileImportException {
		try {
			return importExecutionFacade.doImport(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public String getImportCorrelationId(Integer importfileId) throws NuclosFileImportException {
		try {
			return importExecutionFacade.getImportCorrelationId(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public void stopImport(Integer importfileId) throws NuclosFileImportException {
		try {
			importExecutionFacade.stopImport(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}
}

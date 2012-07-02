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
import org.nuclos.common2.exception.CommonBusinessException;
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

	private static ImportDelegate INSTANCE;

	// Spring injection
	
	private ImportFacadeRemote importFacadeRemote;
	
	// end of Spring injection

	ImportDelegate() {
		INSTANCE = this;
	}

	public static ImportDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setImportFacadeRemote(ImportFacadeRemote importFacadeRemote) {
		this.importFacadeRemote = importFacadeRemote;
	}

	public MasterDataVO createImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		return importFacadeRemote.createImportStructure(importStructure);
	}

	public Object modifyImportStructure(MasterDataWithDependantsVO importStructure) throws CommonBusinessException {
		return importFacadeRemote.modifyImportStructure(importStructure);
	}

	public void removeImportStructure(MasterDataVO importStructure) throws CommonBusinessException {
		importFacadeRemote.removeImportStructure(importStructure);
	}

	public MasterDataVO createFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		return importFacadeRemote.createFileImport(fileImport);
	}

	public Object modifyFileImport(MasterDataWithDependantsVO fileImport) throws CommonBusinessException {
		return importFacadeRemote.modifyFileImport(fileImport);
	}

	public void removeFileImport(MasterDataVO fileImport) throws CommonBusinessException {
		importFacadeRemote.removeFileImport(fileImport);
	}

	public String doImport(Integer importfileId) throws NuclosFileImportException {
		try {
			return importFacadeRemote.doImport(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public String getImportCorrelationId(Integer importfileId) throws NuclosFileImportException {
		try {
			return importFacadeRemote.getImportCorrelationId(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public void stopImport(Integer importfileId) throws NuclosFileImportException {
		try {
			importFacadeRemote.stopImport(importfileId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}
}

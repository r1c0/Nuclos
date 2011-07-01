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
package org.nuclos.server.genericobject.valueobject;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.valueobject.DocumentFileBase;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;

/**
 * Specific file class used for leased object documents.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class GenericObjectDocumentFile extends DocumentFileBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String sModuleDirectoryPath;

	public GenericObjectDocumentFile(String sFileName, Integer iDocumentFileId) {
		super(sFileName, iDocumentFileId);
	}

	public GenericObjectDocumentFile(String sFileName, byte[] abContents) {
		super(sFileName, abContents);
	}
	
	public GenericObjectDocumentFile(NuclosFile file) {
		super(file.getFileName(), file.getFileContents());
	}

	public GenericObjectDocumentFile(String sFileName, Integer iDocumentId, byte[] abContents) {
		super(sFileName, iDocumentId, abContents);
	}
	
	public GenericObjectDocumentFile(String sFileName, Integer iDocumentId, byte[] abContents, String path) {
		super(sFileName, iDocumentId, abContents);
		this.sModuleDirectoryPath = path;
	}
	
	public void setDirectoryPath(String sPath) {
		this.sModuleDirectoryPath = sPath;
	}
	
	public String getDirectoryPath() {
		return sModuleDirectoryPath;
	}

	@Override
	protected byte[] getStoredContents() {
		try {
			MasterDataFacadeRemote mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
			return mdFacade.loadContent(this.getDocumentFileId(), this.getFilename(), StringUtils.emptyIfNull(this.sModuleDirectoryPath));
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

}	// class GenericObjectDocumentFile

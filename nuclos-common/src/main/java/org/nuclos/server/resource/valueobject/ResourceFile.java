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
package org.nuclos.server.resource.valueobject;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.valueobject.DocumentFileBase;
import org.nuclos.server.resource.ejb3.ResourceFacadeRemote;

public class ResourceFile extends DocumentFileBase {

	public ResourceFile(String sFileName, Integer iDocumentFileId) {
		super(sFileName, iDocumentFileId);
	}

	public ResourceFile(String sFileName, byte[] abContents) {
		super(sFileName, abContents);
	}

	public ResourceFile(String sFileName, Integer iDocumentId, byte[] abContents) {
		super(sFileName, iDocumentId, abContents);
	}

	@Override
	protected byte[] getStoredContents() {
		try {
			final ResourceFacadeRemote facade = ServiceLocator.getInstance().getFacade(ResourceFacadeRemote.class);
			return facade.loadResource(this.getDocumentFileId(), this.getFilename());
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

}

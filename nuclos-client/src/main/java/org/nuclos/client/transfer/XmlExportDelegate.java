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
package org.nuclos.client.transfer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.File;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.transfer.ejb3.XmlExportFacadeRemote;

/**
 * Business Delegate for <code>XmlExportFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:fabian.kastlunger@novabit.de">Fabian Kastlunger</a>
 * @version 01.00.00
 */
public class XmlExportDelegate {

	private static XmlExportDelegate INSTANCE;

	// Spring injection
	
	private XmlExportFacadeRemote xmlExportFacadeRemote;
	
	// end of Spring injection

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	XmlExportDelegate() throws RemoteException {
		INSTANCE = this;
	}

	public static XmlExportDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setXmlExportFacadeRemote(XmlExportFacadeRemote xmlExportFacadeRemote) {
		this.xmlExportFacadeRemote = xmlExportFacadeRemote;
	}

	private XmlExportFacadeRemote getXmlExportFacade() {
		return this.xmlExportFacadeRemote;
	}

	public File xmlExport(Map<Integer, String> exportEntities, boolean deepexport, String sFileName) throws NuclosBusinessException, NuclosFatalException {
		try {
			return getXmlExportFacade().xmlExport(exportEntities, deepexport, sFileName);
		}
		catch (CommonFinderException e) {
			throw new NuclosBusinessException(e.getMessage(), e);
		}
		catch (CommonPermissionException e) {
			throw new NuclosBusinessException(e.getMessage(), e);
		}
		catch (RemoteException e) {
			throw new NuclosFatalException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new NuclosFatalException(e.getMessage(), e);
		}
		catch (Exception e) {
			throw new NuclosFatalException(e.getMessage(), e);
		}
	}

}

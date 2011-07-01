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

import javax.ejb.CreateException;

import org.dom4j.DocumentException;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.transfer.ejb3.XmlImportFacadeRemote;

/**
 * Business Delegate for <code>XmlImportFacadeBean</code>. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:fabian.kastlunger@novabit.de">Fabian Kastlunger</a>
 * @version 01.00.00
 */

public class XmlImportDelegate {

	private static XmlImportDelegate singleton;

	//private final Logger log = Logger.getLogger(this.getClass());

	private final XmlImportFacadeRemote facade;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	private XmlImportDelegate() throws RemoteException, CreateException {
		this.facade = ServiceLocator.getInstance().getFacade(XmlImportFacadeRemote.class);
	}

	public static synchronized XmlImportDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new XmlImportDelegate();
			} catch (RemoteException ex) {
				throw new CommonRemoteException(ex);
			} catch (CreateException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return singleton;
	}

	public XmlImportFacadeRemote getXmlImportFacade() {
		return this.facade;
	}

	public void xmlImport(String sEntityName, org.nuclos.common2.File importFile) throws NuclosFatalException, NuclosBusinessException, CommonFinderException {
		try {
			this.getXmlImportFacade().xmlImport(sEntityName, importFile);
		}
		catch (RemoteException e) {
			throw new NuclosFatalException(e);
		}
		catch (IOException e) {
			throw new NuclosFatalException(e);
		}
		catch (DocumentException e) {
				throw new NuclosFatalException(e);
		}
		catch (CommonCreateException e) {
			throw new NuclosFatalException(e);
		}
		catch (CommonPermissionException e) {
			throw new NuclosFatalException(e);
		}
		catch (CreateException e) {
			throw new NuclosFatalException(e);
		}
		catch (NuclosBusinessRuleException e) {
			throw new NuclosFatalException(e);
		}
	}
}

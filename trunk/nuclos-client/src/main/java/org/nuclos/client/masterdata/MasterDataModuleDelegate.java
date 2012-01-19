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
package org.nuclos.client.masterdata;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MasterDataModuleFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class MasterDataModuleDelegate {
	private static MasterDataModuleDelegate singleton;

	public static final String ENTITYNAME_ENTITY = "entity";

	private final MasterDataModuleFacadeRemote facade;

	private final MasterDataFacadeRemote mdfacade;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	private MasterDataModuleDelegate() {
		this.facade = ServiceLocator.getInstance().getFacade(MasterDataModuleFacadeRemote.class);
		this.mdfacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
	}

	public static synchronized MasterDataModuleDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new MasterDataModuleDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}

	public MasterDataModuleFacadeRemote getFacade() {
		return this.facade;
	}

	public MasterDataFacadeRemote getMasterDataFacade() {
		return this.mdfacade;
	}

	/**
	 * creates the given object, along with its dependants (if any).
	 * @param sEntityName
	 * @param mdvo must have an empty (<code>null</code>) id.
	 * @return the created object
	 * @throws NuclosBusinessException
	 * @precondition mdvo.getId() != null
	 * @precondition mpDependants != null --> for(m : mpDependants.values()) { m.getId() == null }
	 */
	public MasterDataVO create(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonBusinessException {
		try {
			return this.getFacade().create(sEntityName, mdvo, mpDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * updates the given object, along with its dependants.
	 * @param sEntityName
	 * @param mdvo
	 * @param mpDependants May be <code>null</code>.
	 * @return the id of the updated object
	 * @throws CommonBusinessException
	 * @precondition mdvo.getId() != null
	 */
	public Object update(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonBusinessException {
		try {
			return this.getFacade().modify(sEntityName, mdvo, mpDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(String sEntityName, MasterDataVO mdvo)
				throws CommonBusinessException{
		try {
			this.getFacade().remove(sEntityName, mdvo, true);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

//	public Map<String, Integer> getResourcesByModuleId(Integer iId) {
//		try {
//			return this.getFacade().getResourcesByModuleId(iId);
//		}
//		catch (RuntimeException ex) {
//			throw new CommonFatalException(ex);
//		}
//	}

	public String getResourceSIdForLabel(Integer iId) {
		try {
			return this.getFacade().getResourceSIdForLabel(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForDescription(Integer iId) {
		try {
			return this.getFacade().getResourceSIdForDescription(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForTreeView(Integer iId) {
		try {
			return this.getFacade().getResourceSIdForTreeView(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForTreeViewDescription(Integer iId) {
		try {
			return this.getFacade().getResourceSIdForTreeViewDescription(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForMenuPath(Integer iId) {
		try {
			return this.getFacade().getResourceSIdForMenuPath(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}

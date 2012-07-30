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
package org.nuclos.client.common.security;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;

/**
 * Business Delegate for <code>SecurityFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
// @Component
public class SecurityDelegate {
	
	private static final Logger LOG = Logger.getLogger(SecurityDelegate.class);
	
	private static SecurityDelegate INSTANCE;

	private SecurityFacadeRemote facade;

	SecurityDelegate() {
		INSTANCE = this;
	}

	public static SecurityDelegate getInstance() {
		return INSTANCE;
	}
	
	// @Autowired
	public final void setSecurityFacadeRemote(SecurityFacadeRemote facade) {
		this.facade = facade;
	}

	/**
	 * gets the facade once for this object and stores it in a member variable.
	 */
	private SecurityFacadeRemote getSecurityFacade() throws NuclosFatalException {
		return facade;
	}

	public ApplicationProperties.Version getCurrentApplicationVersionOnServer() {
		try {
			return this.getSecurityFacade().getCurrentApplicationVersionOnServer();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getCurrentApplicationInfoOnServer() {
		try {
			return this.getSecurityFacade().getCurrentApplicationInfoOnServer();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @return the allowed actions for the current user
	 */
	public Set<String> getAllowedActions() {
		try {
			return this.getSecurityFacade().getAllowedActions();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public ModulePermissions getModulePermissions() {
		try {
			return this.getSecurityFacade().getModulePermissions();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public MasterDataPermissions getMasterDataPermissions() {
		try {
			return this.getSecurityFacade().getMasterDataPermissions();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * get permissions for a subform
	 * @param iSubformId
	 * @param sEntityName
	 * @return a map of state id's and the corresponding permission
	 */
	public Map<Integer, Permission> getSubFormPermission(String sEntityName) {
		try {
			return this.getSecurityFacade().getSubFormPermission(sEntityName);
		}catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * read attribute permissions for all attributes of a given entity in a
	 * given state
	 *
	 * @param entity  the entity
	 * @param stateId the state id
	 * @return a map from attribute names to permission objects
	 */
	public Map<String, Permission> getAttributePermissionsByEntity(String entity, Integer stateId) {
		try {
			return getSecurityFacade().getAttributePermissionsByEntity(entity, stateId);
		}
		catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void invalidateCache() {
		try {
			facade.invalidateCache();
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 *
	 * @return
	 */
	public Boolean isSuperUser() {
		try {
			return this.getSecurityFacade().isSuperUser();
		}
		catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}


	public Map<String, Object> getInitialSecurityData() {
		try {
			return getSecurityFacade().getInitialSecurityData();
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Integer getUserId(String sUserName) {
		try {
			return getSecurityFacade().getUserId(sUserName);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Boolean isLdapAuthenticationActive() {
		return getSecurityFacade().isLdapAuthenticationActive();
	}

	public Boolean isLdapSynchronizationActive() {
		return getSecurityFacade().isLdapSynchronizationActive();
	}

	public Date getPasswordExpiration() throws CommonPermissionException {
		return getSecurityFacade().getPasswordExpiration();
	}

}	// class SecurityDelegate

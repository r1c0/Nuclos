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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.security.Permission;
import org.nuclos.common.security.PermissionKey;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.MasterDataPermission;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermission;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;

/**
 * caches client rights.
 * @todo move some code to the server side.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SecurityCache {
	private static SecurityCache singleton;

	private String username;
	private Boolean superUser;
	private Set<String> stAllowedActions;

	private ModulePermissions modulepermissions;
	private MasterDataPermissions masterdatapermissions;

	private Map<PermissionKey.AttributePermissionKey, Permission> mpAttributePermission = new HashMap<PermissionKey.AttributePermissionKey, Permission>();
	private Map<PermissionKey.SubFormPermissionKey, Map<Integer, Permission>> mpSubFormPermission = new HashMap<PermissionKey.SubFormPermissionKey, Map<Integer, Permission>>();

	private Map<PermissionKey.ModulePermissionKey, ModulePermission> modulePermissionsCache = new HashMap<PermissionKey.ModulePermissionKey, ModulePermission>();
	private Map<PermissionKey.MasterDataPermissionKey, MasterDataPermission> masterDataPermissionsCache = new HashMap<PermissionKey.MasterDataPermissionKey, MasterDataPermission>();

	private final MessageListener listener = new MessageListener() {
		@Override
        public void onMessage(Message msg) {
			boolean clearcache = false;
			if (msg instanceof TextMessage) {
				TextMessage tm = (TextMessage) msg;
				try {
					if (StringUtils.isNullOrEmpty(tm.getText()) || tm.getText().equals(username)) {
						clearcache = true;
					}
				} catch (JMSException e) {
					clearcache = true;
				}
			}
			if (clearcache) {
				SecurityCache.this.revalidate();
				AttributeCache.getInstance().revalidate();
				UIUtils.runCommandLater(Main.getMainFrame(), new CommonRunnable() {
					@Override
	                public void run() throws CommonBusinessException {
						Main.getMainController().refreshMenus();
					}
				});
			}
		}
	};


	/**
	 * May optionally be called to explicitly initialize the cache. The cache is implicitly initialized by
	 * the first call to <code>getInstance()</code> anyway.
	 */
	public static void initialize() {
		getInstance();
	}

	/**
	 * @return the one (and only) instance of SecurityCache
	 */
	public static synchronized SecurityCache getInstance() {
		if (singleton == null) {
			singleton = new SecurityCache();
		}
		return singleton;
	}

	/**
	 * creates the cache. Fills in all the attributes from the database.
	 */
	private SecurityCache() {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_SECURITYCACHE, listener);

		this.validate();
	}

	public synchronized boolean isActionAllowed(String sActionName) {
		return this.stAllowedActions.contains(sActionName);
	}

	private ModulePermission getModulePermission(String sEntityName, Integer iGenericObjectId) {
		PermissionKey.ModulePermissionKey modulePermissionKey = new PermissionKey.ModulePermissionKey(iGenericObjectId, sEntityName);
		if (!modulePermissionsCache.containsKey(modulePermissionKey)) {
			ModulePermission permission = modulepermissions.getMaxPermissionForGenericObject(sEntityName, iGenericObjectId);
			modulePermissionsCache.put(modulePermissionKey, permission);
		}
		return modulePermissionsCache.get(modulePermissionKey);
	}

	private MasterDataPermission getMasterDataPermission(String sEntityName) {
		PermissionKey.MasterDataPermissionKey masterDataPermissionKey = new PermissionKey.MasterDataPermissionKey(sEntityName);
		if (!masterDataPermissionsCache.containsKey(masterDataPermissionKey)) {
			MasterDataPermission permission = masterdatapermissions.get(sEntityName);
			masterDataPermissionsCache.put(masterDataPermissionKey, permission);
		}
		return masterDataPermissionsCache.get(masterDataPermissionKey);
	}

	public synchronized boolean isReadAllowedForModule(String sModuleEntity, Integer iGenericObjectId) {
		return ModulePermission.includesReading(this.getModulePermission(sModuleEntity, iGenericObjectId));
	}

	public synchronized boolean isReadAllowedForMasterData(String sEntity) {
		return MasterDataPermission.includesReading(this.getMasterDataPermission(sEntity));
	}

	/**
	 * Check, whether reading is allowed for any type of entity
	 * @param entityName  the name
	 * @return boolean
	 */
	public synchronized boolean isReadAllowedForEntity(String entityName) {
		return isReadAllowedForMasterData(entityName)
		   ||  isReadAllowedForModule(entityName, null);
	}

	public synchronized boolean isReadAllowedForMasterData(NuclosEntity entity) {
		return isReadAllowedForMasterData(entity.getEntityName());
	}

	public synchronized boolean isNewAllowedForModule(String sModuleEntity) {
		return this.modulepermissions.getNewAllowedByEntityName().get(sModuleEntity);
	}

	public synchronized boolean isNewAllowedForModuleAndProcess(Integer iModuleId, Integer iProcessId) {
		return this.modulepermissions.getNewAllowedProcessesByModuleId().containsKey(iModuleId) &&
			this.modulepermissions.getNewAllowedProcessesByModuleId().get(iModuleId).contains(iProcessId);
	}

	public synchronized boolean isWriteAllowedForModule(String sModuleEntity, Integer iGenericObjectId) {
		return ModulePermission.includesWriting(this.getModulePermission(sModuleEntity, iGenericObjectId));
	}

	public synchronized boolean isWriteAllowedForMasterData(String sEntity) {
		return MasterDataPermission.includesWriting(this.getMasterDataPermission(sEntity));
	}

	public synchronized boolean isWriteAllowedForMasterData(NuclosEntity entity) {
		return isWriteAllowedForMasterData(entity.getEntityName());
	}

	public synchronized boolean isDeleteAllowedForModule(String sModuleEntity, Integer iGenericObjectId, boolean physically) {
		ModulePermission modulePermission = this.getModulePermission(sModuleEntity, iGenericObjectId);
		return physically
			? ModulePermission.includesDeletingPhysically(modulePermission)
			: ModulePermission.includesDeletingLogically(modulePermission);
	}

	public synchronized boolean isDeleteAllowedForMasterData(String sEntity) {
		return MasterDataPermission.includesDeleting(this.getMasterDataPermission(sEntity));
	}

	/**
	 * revalidates this cache: clears it, then fills in all the attributes from the server again.
	 */
	public synchronized void revalidate() {
		this.superUser = Boolean.FALSE;
		this.stAllowedActions = null;
		this.modulepermissions = null;
		this.masterdatapermissions = null;
		this.mpAttributePermission.clear();
		this.mpSubFormPermission.clear();
		modulePermissionsCache.clear();
		masterDataPermissionsCache.clear();
		validate();
	}

	/**
	 * fills this cache.
	 * @throws NuclosFatalException
	 */
    private void validate() throws NuclosFatalException {
		Map<String, Object> iniData = SecurityDelegate.getInstance().getInitialSecurityData();
		this.username = (String) iniData.get(SecurityFacadeRemote.USERNAME);
		this.superUser = (Boolean) iniData.get(SecurityFacadeRemote.IS_SUPER_USER);
		this.stAllowedActions = (Set<String>) iniData.get(SecurityFacadeRemote.ALLOWED_ACTIONS);
		this.modulepermissions = (ModulePermissions) iniData.get(SecurityFacadeRemote.MODULE_PERMISSIONS);
		this.masterdatapermissions = (MasterDataPermissions) iniData.get(SecurityFacadeRemote.MASTERDATA_PERMISSIONS);
	}

	/**
	 * get permissions for a subform
	 * NOTE: this method makes only sense for subforms placed in modules
	 * @param iSubformId
	 * @param sEntityName
	 * @return a map of state id's and the corresponding permission
	 */
	public synchronized Map<Integer, Permission> getSubFormPermission(String sEntityName) {
		PermissionKey.SubFormPermissionKey subFormPermissionKey = new PermissionKey.SubFormPermissionKey(sEntityName);
		if (!mpSubFormPermission.containsKey(subFormPermissionKey)) {
			Map<Integer, Permission> permission = SecurityDelegate.getInstance().getSubFormPermission(sEntityName);
			mpSubFormPermission.put(subFormPermissionKey, permission);
		}
		return mpSubFormPermission.get(subFormPermissionKey);
	}

	/**
	 * get the permission for a subform within the given state id
	 * NOTE: this method makes only sense for subforms placed in modules
	 * @param iSubformId
	 * @param sEntityName
	 * @param iState
	 * @return Permission
	 */
	public synchronized Permission getSubFormPermission(String sEntityName, Integer iState) {
		return getSubFormPermission(sEntityName).get(iState);
	}

	/**
	 * get the permission for an attribute within the given stateId
	 * @param entity         the entity
	 * @param attributeName  the attribute
	 * @param stateId        the state
	 * @return Permission
	 */
	public synchronized Permission getAttributePermission(String entity, String attributeName, Integer stateId) {
		PermissionKey.AttributePermissionKey key = new PermissionKey.AttributePermissionKey(entity, attributeName, stateId);
		if(!mpAttributePermission.containsKey(key)) {
			Map<String, Permission> attrPermissions
				= SecurityDelegate.getInstance().getAttributePermissionsByEntity(entity, stateId);
			for(Map.Entry<String, Permission> e : attrPermissions.entrySet())
				mpAttributePermission.put(
					new PermissionKey.AttributePermissionKey(entity, e.getKey(), stateId),
					e.getValue());
		}
		return mpAttributePermission.get(key);
	}


	public Boolean isSuperUser() {
		return superUser;
	}
}	// class SecurityCache

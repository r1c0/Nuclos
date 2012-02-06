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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.security.Permission;
import org.nuclos.common.security.PermissionKey;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.MasterDataPermission;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.ModulePermission;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Component;

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
@Component
@Lazy
public class SecurityCache {
	
	private static final Logger LOG = Logger.getLogger(SecurityCache.class);
	
	private static SecurityCache INSTANCE;
	
	//
	
	private String username;
	private Boolean superUser;
	private Set<String> stAllowedActions;

	private ModulePermissions modulepermissions;
	private MasterDataPermissions masterdatapermissions;

	private Map<PermissionKey.AttributePermissionKey, Permission> mpAttributePermission 
		= new ConcurrentHashMap<PermissionKey.AttributePermissionKey, Permission>();
	private Map<PermissionKey.SubFormPermissionKey, Map<Integer, Permission>> mpSubFormPermission 
		= new ConcurrentHashMap<PermissionKey.SubFormPermissionKey, Map<Integer, Permission>>();

	private Map<PermissionKey.ModulePermissionKey, ModulePermission> modulePermissionsCache 
		= new ConcurrentHashMap<PermissionKey.ModulePermissionKey, ModulePermission>();
	private Map<PermissionKey.MasterDataPermissionKey, MasterDataPermission> masterDataPermissionsCache 
		= new ConcurrentHashMap<PermissionKey.MasterDataPermissionKey, MasterDataPermission>();
	
	private TopicNotificationReceiver tnr;
	
	private SecurityDelegate securityDelegate;
	
	private AttributeCache attributeCache;

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
				Log.info("JMS trigger clearcache in " + this);
				SecurityCache.this.revalidate();
				attributeCache.revalidate();
				UIUtils.runCommandLater(Main.getInstance().getMainFrame(), new CommonRunnable() {
					@Override
	                public void run() throws CommonBusinessException {
						Main.getInstance().getMainController().refreshMenus();
					}
				});
			}
		}
	};


	/**
	 * @return the one (and only) instance of SecurityCache
	 */
	public static SecurityCache getInstance() {
		return INSTANCE;
	}

	/**
	 * creates the cache. Fills in all the attributes from the database.
	 */
	SecurityCache() {
		INSTANCE = this;
	}
	
	@PostConstruct
	void init() {
		tnr.subscribe(JMSConstants.TOPICNAME_SECURITYCACHE, listener);
		validate();
		this.securityDelegate = SecurityDelegate.getInstance();
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	@Autowired
	void setSecurityDelegate(SecurityDelegate securityDelegate) {
		this.securityDelegate = securityDelegate;
	}
	
	@Autowired
	void setAttributeCache(AttributeCache attributeCache) {
		this.attributeCache = attributeCache;
	}
	
	public synchronized boolean isActionAllowed(String sActionName) {
		return stAllowedActions.contains(sActionName);
	}

	private ModulePermission getModulePermission(String sEntityName, Integer iGenericObjectId) {
		PermissionKey.ModulePermissionKey modulePermissionKey = new PermissionKey.ModulePermissionKey(iGenericObjectId, sEntityName);
		if (!modulePermissionsCache.containsKey(modulePermissionKey)) {
			ModulePermission permission = modulepermissions.getMaxPermissionForGO(sEntityName, iGenericObjectId);
			modulePermissionsCache.put(modulePermissionKey, permission);
		}
		final ModulePermission result = modulePermissionsCache.get(modulePermissionKey);
		if (result == ModulePermission.NO) {
			return null;
		}
		return result;
	}

	/**
	 * Must be synchronized because masterdatapermissions might be null.
	 */
	private synchronized MasterDataPermission getMasterDataPermission(String sEntityName) {
		PermissionKey.MasterDataPermissionKey masterDataPermissionKey = new PermissionKey.MasterDataPermissionKey(sEntityName);
		if (!masterDataPermissionsCache.containsKey(masterDataPermissionKey)) {
			MasterDataPermission permission = masterdatapermissions.get(sEntityName);
			if (permission == null) {
				permission = MasterDataPermission.NO;
			}
			masterDataPermissionsCache.put(masterDataPermissionKey, permission);
		}
		final MasterDataPermission result = masterDataPermissionsCache.get(masterDataPermissionKey);
		if (result == MasterDataPermission.NO) {
			return null;
		}
		return result;
	}

	public boolean isReadAllowedForModule(String sModuleEntity, Integer iGenericObjectId) {
		return ModulePermission.includesReading(getModulePermission(sModuleEntity, iGenericObjectId));
	}

	public boolean isReadAllowedForMasterData(String sEntity) {
		if (NuclosEntity.REPORTEXECUTION.checkEntityName(sEntity)) {
			return isActionAllowed(Actions.ACTION_EXECUTE_REPORTS);
		}
		return MasterDataPermission.includesReading(getMasterDataPermission(sEntity));
	}

	/**
	 * Check, whether reading is allowed for any type of entity
	 * @param entityName  the name
	 * @return boolean
	 */
	public boolean isReadAllowedForEntity(String entityName) {
		return isReadAllowedForMasterData(entityName)
		   ||  isReadAllowedForModule(entityName, null);
	}

	public boolean isReadAllowedForMasterData(NuclosEntity entity) {
		return isReadAllowedForMasterData(entity.getEntityName());
	}

	public synchronized boolean isNewAllowedForModule(String sModuleEntity) {
		return LangUtils.defaultIfNull(modulepermissions.getNewAllowedByEntityName().get(sModuleEntity), false);
	}

	public synchronized boolean isNewAllowedForModuleAndProcess(Integer iModuleId, Integer iProcessId) {
		return modulepermissions.getNewAllowedProcessesByModuleId().containsKey(iModuleId) &&
			modulepermissions.getNewAllowedProcessesByModuleId().get(iModuleId).contains(iProcessId);
	}

	public boolean isWriteAllowedForModule(String sModuleEntity, Integer iGenericObjectId) {
		return ModulePermission.includesWriting(getModulePermission(sModuleEntity, iGenericObjectId));
	}

	public boolean isWriteAllowedForMasterData(String sEntity) {
		return MasterDataPermission.includesWriting(getMasterDataPermission(sEntity));
	}

	public boolean isWriteAllowedForMasterData(NuclosEntity entity) {
		return isWriteAllowedForMasterData(entity.getEntityName());
	}

	public boolean isDeleteAllowedForModule(String sModuleEntity, Integer iGenericObjectId, boolean physically) {
		ModulePermission modulePermission = getModulePermission(sModuleEntity, iGenericObjectId);
		return physically
			? ModulePermission.includesDeletingPhysically(modulePermission)
			: ModulePermission.includesDeletingLogically(modulePermission);
	}

	public boolean isDeleteAllowedForMasterData(String sEntity) {
		return MasterDataPermission.includesDeleting(getMasterDataPermission(sEntity));
	}

	/**
	 * revalidates this cache: clears it, then fills in all the attributes from the server again.
	 * <p>
	 * Must be synchronized because masterdatapermissions is set to null (via validate).
	 * </p>
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
		LOG.info("Cleared cache " + this);
		validate();
	}

	/**
	 * fills this cache.
	 * 
	 * @throws NuclosFatalException
	 */
    private void validate() throws NuclosFatalException {
		Map<String, Object> iniData = securityDelegate.getInitialSecurityData();
		this.username = (String) iniData.get(SecurityFacadeRemote.USERNAME);
		this.superUser = (Boolean) iniData.get(SecurityFacadeRemote.IS_SUPER_USER);
		this.stAllowedActions = (Set<String>) iniData.get(SecurityFacadeRemote.ALLOWED_ACTIONS);
		this.modulepermissions = (ModulePermissions) iniData.get(SecurityFacadeRemote.MODULE_PERMISSIONS);
		this.masterdatapermissions = (MasterDataPermissions) iniData.get(SecurityFacadeRemote.MASTERDATA_PERMISSIONS);
		LOG.info("Validated (filled) cache " + this);
	}

	/**
	 * get permissions for a subform
	 * NOTE: this method makes only sense for subforms placed in modules
	 * @param iSubformId
	 * @param sEntityName
	 * @return a map of state id's and the corresponding permission
	 */
	public Map<Integer, Permission> getSubFormPermission(String sEntityName) {
		PermissionKey.SubFormPermissionKey subFormPermissionKey = new PermissionKey.SubFormPermissionKey(sEntityName);
		if (!mpSubFormPermission.containsKey(subFormPermissionKey)) {
			Map<Integer, Permission> permission = securityDelegate.getSubFormPermission(sEntityName);
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
	public Permission getSubFormPermission(String sEntityName, Integer iState) {
		return getSubFormPermission(sEntityName).get(iState);
	}

	/**
	 * get the permission for an attribute within the given stateId
	 * @param entity         the entity
	 * @param attributeName  the attribute
	 * @param stateId        the state
	 * @return Permission
	 */
	public Permission getAttributePermission(String entity, String attributeName, Integer stateId) {
		PermissionKey.AttributePermissionKey key = new PermissionKey.AttributePermissionKey(entity, attributeName, stateId);
		if(!mpAttributePermission.containsKey(key)) {
			Map<String, Permission> attrPermissions
				= securityDelegate.getAttributePermissionsByEntity(entity, stateId);
			for(Map.Entry<String, Permission> e : attrPermissions.entrySet()) {
				Permission perm = e.getValue();
				if (perm == null) {
					perm = Permission.NONE;
				}
				mpAttributePermission.put(
					new PermissionKey.AttributePermissionKey(entity, e.getKey(), stateId),
					perm);
			}
		}
		final Permission result = mpAttributePermission.get(key);
		if (result == Permission.NONE) {
			return null;
		}
		return result;
	}


	public Boolean isSuperUser() {
		return superUser;
	}
	
}	// class SecurityCache

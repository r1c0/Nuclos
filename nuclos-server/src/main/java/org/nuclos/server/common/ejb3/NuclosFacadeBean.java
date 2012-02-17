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
package org.nuclos.server.common.ejb3;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.NuclosRemoteContextHolder;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class NuclosFacadeBean implements NuclosFacadeLocal {
	
	private static final Logger LOG = Logger.getLogger(NuclosFacadeBean.class);

	private GenericObjectFacadeLocal goFacade;
	private MasterDataFacadeLocal mdFacade;
	private TransferFacadeLocal transferFacade;
	private LocaleFacadeLocal localFacade;
	
	private boolean local;
	
	private NuclosRemoteContextHolder remoteCtx;
	
	private RecordGrantUtils grantUtils;
	
	protected SpringDataBaseHelper dataBaseHelper;
	
	private SecurityCache securityCache;
	
	private Modules modules;
	
	private MasterDataMetaCache masterDataMetaCache;
	
	private NucletDalProvider nucletDalProvider;
	
	public NuclosFacadeBean() {
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setSecurityCache(SecurityCache securityCache) {
		this.securityCache = securityCache;
	}
	
	@Autowired
	void setModules(Modules modules) {
		this.modules = modules;
	}
	
	@Autowired
	void setMasterDataMetaCache(MasterDataMetaCache masterDataMetaCache) {
		this.masterDataMetaCache = masterDataMetaCache;
	}
	
	@Autowired
	void setNucletDalProvider(NucletDalProvider nucletDalProvider) {
		this.nucletDalProvider = nucletDalProvider;
	}

	@Autowired
	void setNuclosRemoteContextHolder(NuclosRemoteContextHolder remoteCtx) {
		this.remoteCtx = remoteCtx;
	}
	
	@Autowired
	final void setRecordGrantUtils(RecordGrantUtils grantUtils) {
		this.grantUtils = grantUtils;
	}
	
	protected final RecordGrantUtils getRecordGrantUtils() {
		return grantUtils;
	}
	
	/**
	 * @return the name of the current user. Shortcut for <code>this.getSessionContext().getCallerPrincipal().getName()</code>.
	 */
	@Override
	public final String getCurrentUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}

	public boolean isCalledRemotely() {
		return remoteCtx.peek();
	}
	
	public void setLocal(boolean bln) {
		this.local = bln;
	}
	
	/**
	 * @return a logger for the class of this object.
	 * @deprecated
	 */
	public Logger getLogger() {
		return this.LOG;
	}

	/**
	 * @deprecated
	 */
	protected void debug(Object o) {
		this.log(Level.DEBUG, o);
	}

	/**
	 * @deprecated
	 */
	protected void info(Object o) {
		this.log(Level.INFO, o);
	}

	/**
	 * @deprecated
	 */
	protected void warn(Object o) {
		this.log(Level.WARN, o);
	}

	/**
	 * @deprecated
	 */
	protected void error(Object o) {
		this.log(Level.ERROR, o);
	}

	/**
	 * @deprecated
	 */
	protected void fatal(Object o) {
		this.log(Level.FATAL, o);
	}

	/**
	 * @deprecated
	 */
	protected void log(Priority priority, Object oMessage, Throwable t) {
		this.getLogger().log(priority, oMessage, t);
	}

	/**
	 * @deprecated
	 */
	protected void log(Priority priority, Object oMessage) {
		this.getLogger().log(priority, oMessage);
	}

	/**
	 * @deprecated
	 */
	protected boolean isInfoEnabled() {
		return LOG.isInfoEnabled();
	}

	/**
	 * checks if it is allowed to read a genericobject
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @throws CommonPermissionException if reading of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkReadAllowedForModule (Integer iModuleId, Integer iGenericObjectId) throws CommonPermissionException {
		this.checkReadAllowedForModule(modules.getEntityNameByModuleId(iModuleId), iGenericObjectId);
	}

	/**
	 * checks if it is allowed to read a genericobject
	 * @param sEntityName
	 * @param iGenericObjectId
	 * @precondition iGenericObjectId != null
	 * @throws CommonPermissionException if reading of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkReadAllowedForModule (String sEntityName, Integer iGenericObjectId) throws CommonPermissionException {
		if (this.isCalledRemotely()) {
			if (iGenericObjectId == null) {
				throw new NullArgumentException("iGenericObjectId");
			}

			if (!securityCache.isReadAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId)) {
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.1", 
					this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), modules.getEntityLabelByModuleName(sEntityName)));
			}
		}
	}

	/**
	 * checks if it is allowed to write a genericobject
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @throws CommonPermissionException if writing of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkWriteAllowedForModule (Integer iModuleId, Integer iGenericObjectId) throws CommonPermissionException {
		this.checkWriteAllowedForModule(modules.getEntityNameByModuleId(iModuleId), iGenericObjectId);
	}

	/**
	 * checks if it is allowed to write a genericobject
	 * @param sEntityName
	 * @param iGenericObjectId
	 * @precondition iGenericObjectId != null
	 * @throws CommonPermissionException if writing of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkWriteAllowedForModule(String sEntityName, Integer iGenericObjectId) throws CommonPermissionException {
		checkFrozenEntities(sEntityName);
		if (this.isCalledRemotely()) {
			if (iGenericObjectId == null) {
				throw new NullArgumentException("iGenericObjectId");
			}

			if (!securityCache.isWriteAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId)) {
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.2",
					this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), modules.getEntityLabelByModuleName(sEntityName)));
			}
		}
	}

	/**
	 * checks if it is allowed to write in an objectgroup
	 * @param iModuleId
	 * @param iObjectGroupId
	 * @throws CommonPermissionException if writing to the entity and the objectgroup is not allowed for the current user.
	 */
	protected void checkWriteAllowedForObjectGroup (Integer iModuleId, Integer iObjectGroupId) throws CommonPermissionException {
		this.checkWriteAllowedForObjectGroup(modules.getEntityNameByModuleId(iModuleId), iObjectGroupId);
	}

	/**
	 * checks if it is allowed to write in an objectgroup
	 * @param sEntityName
	 * @param iObjectGroupId, may be null
	 * @throws CommonPermissionException if writing to the entity and the objectgroup is not allowed for the current user.
	 */
	protected void checkWriteAllowedForObjectGroup(String sEntityName, Integer iObjectGroupId) throws CommonPermissionException {
		if (this.isCalledRemotely()) {
			if (!securityCache.isWriteAllowedForObjectGroup(this.getCurrentUserName(), sEntityName, iObjectGroupId)) {
				if (iObjectGroupId == null) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.3",
						this.getCurrentUserName(), modules.getEntityLabelByModuleName(sEntityName)));
				}
				else {
					GenericObjectGroupFacadeLocal facade = ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.4",
						this.getCurrentUserName(), facade.getObjectGroupName(iObjectGroupId)));
				}
			}
		}
	}

	/**
	 * checks if it is allowed to delete a genericobject
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param bDeletePhysically
	 * @throws CommonPermissionException if deleting of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkDeleteAllowedForModule (Integer iModuleId, Integer iGenericObjectId, boolean bDeletePhysically) throws CommonPermissionException {
		this.checkDeleteAllowedForModule(modules.getEntityNameByModuleId(iModuleId), iGenericObjectId, bDeletePhysically);
	}

	/**
	 * checks if it is allowed to delete a genericobject
	 * @param sEntityName
	 * @param iGenericObjectId
	 * @param bDeletePhysically
	 * @precondition iGenericObjectId != null
	 * @throws CommonPermissionException if deleting of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkDeleteAllowedForModule(String sEntityName, Integer iGenericObjectId, boolean bDeletePhysically) throws CommonPermissionException {
		checkFrozenEntities(sEntityName);
		if (this.isCalledRemotely()) {
			if (iGenericObjectId == null) {
				throw new NullArgumentException("iGenericObjectId");
			}
			String sMessage;
			if (!securityCache.isDeleteAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId, bDeletePhysically)) {
				if (!bDeletePhysically) {
					sMessage = StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.5",
						this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), modules.getEntityLabelByModuleName(sEntityName));
				}
				else {
					sMessage = StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.6",
						this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), modules.getEntityLabelByModuleName(sEntityName));
				}
				throw new CommonPermissionException(sMessage);
			}
		}
	}

	protected void checkReadAllowed(NuclosEntity...entities) throws CommonPermissionException {
		checkReadAllowed(NuclosEntity.getEntityNames(entities));
	}
	
	/**
	 * checks if the current user is allowed to read at least one of the given entities (masterdata or genericobject)
	 * @param sEntityName
	 * @throws CommonPermissionException if reading of the entity (masterdata or genericobject) is not allowed for the current user.
	 */
	protected void checkReadAllowed(String... sEntityName) throws CommonPermissionException {
		if (this.isCalledRemotely()) {
			boolean isReadAllowed = false;
			for (int i = 0; i < sEntityName.length; i++) {
				String sEntityLabel;
				if(modules.isModuleEntity(sEntityName[i])) {
					sEntityLabel = modules.getEntityLabelByModuleName(sEntityName[i]);
					if(securityCache.isReadAllowedForModule(this.getCurrentUserName(), sEntityName[i], null)) {
					  isReadAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(masterDataMetaCache.getMetaData(sEntityName[i]));
					//masterDataMetaCache.getMetaData(sEntityName[i]).getLabel();
					if(securityCache.isReadAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isReadAllowed = true;
					  break;
					}
				}
				if (!isReadAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.7", this.getCurrentUserName(), sEntityLabel));
				}
			}

		}
	}

	protected void checkWriteAllowed(NuclosEntity...entities) throws CommonPermissionException {
		checkWriteAllowed(NuclosEntity.getEntityNames(entities));
	}
	
	/**
	 * checks if the current user is allowed to write at least one of the given entities (masterdata or genericobject)
	 * @param sEntityName
	 * @throws CommonPermissionException if writing of the entity (masterdata or genericobject) is not allowed for the current user.
	 */
	protected void checkWriteAllowed(String... sEntityName) throws CommonPermissionException {
		checkFrozenEntities(sEntityName);
		if (this.isCalledRemotely()) {
			boolean isWriteAllowed = false;
			for (int i = 0; i < sEntityName.length; i++) {
				String sEntityLabel;
				if (modules.isModuleEntity(sEntityName[i])) {
					sEntityLabel = modules.getEntityLabelByModuleName(sEntityName[i]);
					if(securityCache.isWriteAllowedForModule(this.getCurrentUserName(), sEntityName[i], null)) {
					  isWriteAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(masterDataMetaCache.getMetaData(sEntityName[i]));
					//masterDataMetaCache.getMetaData(sEntityName[i]).getLabel();
					if(securityCache.isWriteAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isWriteAllowed = true;
					  break;
					}
				}
				if (!isWriteAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.8", this.getCurrentUserName(), sEntityLabel));
				}
			}
		}
	}

	protected void checkDeleteAllowed(NuclosEntity...entities) throws CommonPermissionException {
		checkDeleteAllowed(NuclosEntity.getEntityNames(entities));
	}
	
	/**
	 * checks if it is allowed to delete an entity (masterdata or genericobject)
	 * @param sEntityName
	 * @throws CommonPermissionException if deleting of the entity (masterdata or genericobject) is not allowed for the current user.
	 */
	protected void checkDeleteAllowed(String... sEntityName) throws CommonPermissionException {
		checkFrozenEntities(sEntityName);
		if (this.isCalledRemotely()) {
			boolean isDeleteAllowed = false;
			for (int i = 0; i < sEntityName.length; i++) {
				String sEntityLabel;
				if(modules.isModuleEntity(sEntityName[i])) {
					sEntityLabel = modules.getEntityLabelByModuleName(sEntityName[i]);
					if(securityCache.isDeleteAllowedForModule(this.getCurrentUserName(), sEntityName[i], null, true)) {
					  isDeleteAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(masterDataMetaCache.getMetaData(sEntityName[i]));
					//masterDataMetaCache.getMetaData(sEntityName[i]).getLabel();
					if(securityCache.isDeleteAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isDeleteAllowed = true;
					  break;
					}
				}
				if (!isDeleteAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.9", this.getCurrentUserName(), sEntityLabel));
				}
			}
		}
	}

	protected boolean isInRole(String sRoleName) {
		for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			if (ga.getAuthority().equals(sRoleName)) {
				return true;
			}
		}
		return false;
	}

	private String getSystemIdentifier(Integer iGenericObjectId) throws CommonPermissionException {
		try {
			return getGenericObjectFacade().get(iGenericObjectId).getSystemIdentifier();
		}
		catch (CommonFinderException e) {
			throw new CommonFatalException(e);
		}
	}

	protected <T> T getFacade(Class<T> c) {
		return ServiceLocator.getInstance().getFacade(c);
	}

	protected GenericObjectFacadeLocal getGenericObjectFacade() {
		if (goFacade == null)
			goFacade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
   	return goFacade;
	}

	protected MasterDataFacadeLocal getMasterDataFacade() {
		return this.getFacade(MasterDataFacadeLocal.class);
   }
	
	protected LocaleFacadeLocal getLocaleFacade() {
		if (localFacade == null) {
			localFacade = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
		}
   	return localFacade;
	}

	private TransferFacadeLocal getTransferFacade() {
		if (transferFacade == null)
			transferFacade = ServiceLocator.getInstance().getFacade(TransferFacadeLocal.class);
		return transferFacade;
	}

	private void checkFrozenEntities(String... entityName) throws CommonPermissionException {
		for (String s : entityName)
			if (getTransferFacade().isFrozenEntity(s)) {
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.10", s));
			}
	}
	
	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 * 
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, String entity) {
		return grantUtils.append(expr, entity);
	}
	
	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 * 
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, EntityMetaDataVO entity) {
		return grantUtils.append(expr, entity != null? entity.getEntity(): null);
	}
	
	/**
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression getRecordGrantExpression(Long id, String entity) {
		return appendRecordGrants(new CollectableSearchExpression(new CollectableIdCondition(id)), entity);
	}
	
	/**
	 * append record grant(s) to cond for given entity.
	 * 
	 * @param expr
	 * @param entity
	 * @return new AND condition if any record grant(s) found, otherwise cond
	 *         is returned.
	 *         
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchCondition appendRecordGrants(CollectableSearchCondition cond, String entity) {
		return grantUtils.append(cond, entity);
	}
	
	/**
	 * @deprecated Use Spring injection instead.
	 */
	protected JdbcEntityObjectProcessor getProcessor(String entity) {
		return nucletDalProvider.getEntityObjectProcessor(entity);
	}
	
	/**
	 * @deprecated Use Spring injection instead.
	 */
	protected JdbcEntityObjectProcessor getProcessor(NuclosEntity entity) {
		return nucletDalProvider.getEntityObjectProcessor(entity);
	}
}

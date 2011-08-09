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
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

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
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Stateless
public abstract class NuclosFacadeBean implements NuclosFacadeLocal {
	private Logger log;

	private GenericObjectFacadeLocal goFacade;
	private MasterDataFacadeLocal mdFacade;
	private TransferFacadeLocal transferFacade;
	private LocaleFacadeLocal localFacade;
	
	private boolean local;
	
	//@Resource
	private SessionContext sctx;
	
	@PostConstruct
	@RolesAllowed("Login")
	public void postConstruct() {
		initLogger();
	}

	@PreDestroy
	public void preDestroy() {
		this.log = null;
	}
	
	public NuclosFacadeBean() {
	}

	/**
	 * @return the name of the current user. Shortcut for <code>this.getSessionContext().getCallerPrincipal().getName()</code>.
	 */
	@Override
	public final String getCurrentUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}

	public boolean isCalledRemotely() {
		return NuclosRemoteContextHolder.peek();
	}
	
	public void setLocal(boolean bln) {
		this.local = bln;
	}
	
	

	protected void initLogger() {
		this.log = Logger.getLogger(this.getClass());
	}

	/**
	 * @return a logger for the class of this object.
	 */
	public Logger getLogger() {
		return this.log;
	}

	protected void debug(Object o) {
		this.log(Level.DEBUG, o);
	}

	protected void info(Object o) {
		this.log(Level.INFO, o);
	}

	protected void warn(Object o) {
		this.log(Level.WARN, o);
	}

	protected void error(Object o) {
		this.log(Level.ERROR, o);
	}

	protected void fatal(Object o) {
		this.log(Level.FATAL, o);
	}

	protected void log(Priority priority, Object oMessage, Throwable t) {
		this.getLogger().log(priority, oMessage, t);
	}

	protected void log(Priority priority, Object oMessage) {
		this.getLogger().log(priority, oMessage);
	}

	protected boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	/**
	 * checks if it is allowed to read a genericobject
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @throws CommonPermissionException if reading of the entity and the special genericobject is not allowed for the current user.
	 */
	protected void checkReadAllowedForModule (Integer iModuleId, Integer iGenericObjectId) throws CommonPermissionException {
		this.checkReadAllowedForModule(Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId);
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

			if (!SecurityCache.getInstance().isReadAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId)) {
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.1", 
					this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), Modules.getInstance().getEntityLabelByModuleName(sEntityName)));
					//"Der Benutzer " + this.getCurrentUserName() + " darf das Object " + getSystemIdentifier(iGenericObjectId) + " des Moduls " +
						//Modules.getInstance().getEntityLabelByModuleName(sEntityName) + " nicht lesen.");
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
		this.checkWriteAllowedForModule(Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId);
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

			if (!SecurityCache.getInstance().isWriteAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId)) {
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.2",
					this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), Modules.getInstance().getEntityLabelByModuleName(sEntityName)));
//					"Der Benutzer " + this.getCurrentUserName() + " darf das Object " + getSystemIdentifier(iGenericObjectId) + " des Moduls " +
//						Modules.getInstance().getEntityLabelByModuleName(sEntityName) + " nicht schreiben.");
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
		this.checkWriteAllowedForObjectGroup(Modules.getInstance().getEntityNameByModuleId(iModuleId), iObjectGroupId);
	}

	/**
	 * checks if it is allowed to write in an objectgroup
	 * @param sEntityName
	 * @param iObjectGroupId, may be null
	 * @throws CommonPermissionException if writing to the entity and the objectgroup is not allowed for the current user.
	 */
	protected void checkWriteAllowedForObjectGroup(String sEntityName, Integer iObjectGroupId) throws CommonPermissionException {
		if (this.isCalledRemotely()) {
			if (!SecurityCache.getInstance().isWriteAllowedForObjectGroup(this.getCurrentUserName(), sEntityName, iObjectGroupId)) {
				if (iObjectGroupId == null) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.3",
						this.getCurrentUserName(), Modules.getInstance().getEntityLabelByModuleName(sEntityName)));
//						"Der Benutzer " + this.getCurrentUserName() + " darf das Object des Moduls " +
//							Modules.getInstance().getEntityLabelByModuleName(sEntityName) + " nicht schreiben.");
				}
				else {
					GenericObjectGroupFacadeLocal facade = ServiceLocator.getInstance().getFacade(GenericObjectGroupFacadeLocal.class);
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.4",
						this.getCurrentUserName(), facade.getObjectGroupName(iObjectGroupId)));
//						"Der Benutzer " + this.getCurrentUserName() + " darf nicht in der Objektgruppe " +
//						facade.getObjectGroupName(iObjectGroupId) + " schreiben.");
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
		this.checkDeleteAllowedForModule(Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId, bDeletePhysically);
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
			if (!SecurityCache.getInstance().isDeleteAllowedForModule(this.getCurrentUserName(), sEntityName, iGenericObjectId, bDeletePhysically)) {
				if (!bDeletePhysically) {
					sMessage = StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.5",
						this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), Modules.getInstance().getEntityLabelByModuleName(sEntityName));
				}
				else {
					sMessage = StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.6",
						this.getCurrentUserName(), getSystemIdentifier(iGenericObjectId), Modules.getInstance().getEntityLabelByModuleName(sEntityName));
				}
//				final StringBuilder sb = new StringBuilder();
//				sb.append("Der Benutzer ").append(this.getCurrentUserName());
//				sb.append(" darf das Object " + getSystemIdentifier(iGenericObjectId) + " des Moduls ");
//				sb.append(Modules.getInstance().getEntityLabelByModuleName(sEntityName)).append(" nicht ");
//				if (bDeletePhysically) {
//					sb.append("physikalisch ");
//				}
//				sb.append("l\u00f6schen.");
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
				if(Modules.getInstance().isModuleEntity(sEntityName[i])) {
					sEntityLabel = Modules.getInstance().getEntityLabelByModuleName(sEntityName[i]);
					if(SecurityCache.getInstance().isReadAllowedForModule(this.getCurrentUserName(), sEntityName[i], null)) {
					  isReadAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]));//MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]).getLabel();
					if(SecurityCache.getInstance().isReadAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isReadAllowed = true;
					  break;
					}
				}
				if (!isReadAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.7", this.getCurrentUserName(), sEntityLabel));
//						"Der Benutzer " + this.getCurrentUserName() + " darf in der Entit\u00e4t \"" +
//							sEntityLabel + "\" nicht lesen.");
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
				if (Modules.getInstance().isModuleEntity(sEntityName[i])) {
					sEntityLabel = Modules.getInstance().getEntityLabelByModuleName(sEntityName[i]);
					if(SecurityCache.getInstance().isWriteAllowedForModule(this.getCurrentUserName(), sEntityName[i], null)) {
					  isWriteAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]));//MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]).getLabel();
					if(SecurityCache.getInstance().isWriteAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isWriteAllowed = true;
					  break;
					}
				}
				if (!isWriteAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.8", this.getCurrentUserName(), sEntityLabel));
//						"Der Benutzer " + this.getCurrentUserName() + " darf in der Entit\u00e4t \"" +
//							sEntityLabel + "\" nicht schreiben.");
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
				if(Modules.getInstance().isModuleEntity(sEntityName[i])) {
					sEntityLabel = Modules.getInstance().getEntityLabelByModuleName(sEntityName[i]);
					if(SecurityCache.getInstance().isDeleteAllowedForModule(this.getCurrentUserName(), sEntityName[i], null, true)) {
					  isDeleteAllowed = true;
					  break;
					}
				}
				else {
					sEntityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]));//MasterDataMetaCache.getInstance().getMetaData(sEntityName[i]).getLabel();
					if(SecurityCache.getInstance().isDeleteAllowedForMasterData(this.getCurrentUserName(), sEntityName[i])) {
					  isDeleteAllowed = true;
					  break;
					}
				}
				if (!isDeleteAllowed) {
					throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.9", this.getCurrentUserName(), sEntityLabel));
//						"Der Benutzer " + this.getCurrentUserName() + " darf in der Entit\u00e4t \"" +
//							sEntityLabel + "\" nicht l\u00f6schen.");
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
//		if (mdFacade == null) {
//			mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
//		}
//   	return mdFacade;
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
//				String msg = String.format("Die Konfigurationsdaten sind zur Zeit gesperrt. "
//					+ "Die Entit\u00e4t \"%s\" kann nicht modifiziert werden.", s);
				throw new CommonPermissionException(StringUtils.getParameterizedExceptionMessage("nucleus.facade.permission.exception.10", s));
			}
	}
	
	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, String entity) {
		return RecordGrantUtils.append(expr, entity);
	}
	
	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, EntityMetaDataVO entity) {
		return RecordGrantUtils.append(expr, entity != null? entity.getEntity(): null);
	}
	
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
	 */
	protected static CollectableSearchCondition appendRecordGrants(CollectableSearchCondition cond, String entity) {
		return RecordGrantUtils.append(cond, entity);
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	protected JdbcEntityObjectProcessor getProcessor(String entity) {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(entity);
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	protected JdbcEntityObjectProcessor getProcessor(NuclosEntity entity) {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(entity);
	}
}

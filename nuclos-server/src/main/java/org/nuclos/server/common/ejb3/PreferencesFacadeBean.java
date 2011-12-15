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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.Actions;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.Frame;
import org.nuclos.common.WorkspaceDescription.MutableContent;
import org.nuclos.common.WorkspaceDescription.NestedContent;
import org.nuclos.common.WorkspaceDescription.Split;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.WorkspaceDescription.Tab;
import org.nuclos.common.WorkspaceDescription.Tabbed;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.preferences.PreferencesConverter;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.IWorkspaceProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for storing user preferences.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Stateless
// @Remote(PreferencesFacadeRemote.class)
@Transactional
public class PreferencesFacadeBean extends NuclosFacadeBean implements PreferencesFacadeRemote {

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getUserPreferences()
	 */
	@Override
	@RolesAllowed("Login")
	public PreferencesVO getUserPreferences() throws CommonFinderException {
		// print out the default encoding on this platform:
		debug("PreferencesFacadeBean.getUserPreferences: Default character encoding on this platform:");
		debug("  System.getProperty(\"file.encoding\") = " + System.getProperty("file.encoding"));
		// debug("  sun.io.Converters.getDefaultEncodingName() = " + sun.io.Converters.getDefaultEncodingName());
		debug("  java.nio.charset.Charset.defaultCharset().name() = " + java.nio.charset.Charset.defaultCharset().name());

		return getUserPreferences(this.getCurrentUserName().toLowerCase());
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getUserPreferences()
	 */
	@Override
	@RolesAllowed("Login")
	public PreferencesVO getTemplateUserPreferences() throws NuclosBusinessException, CommonFinderException{
		PreferencesVO templateUserPrefs = null;
		String templateUser = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_TEMPLATE_USER);
		if (templateUser != null)
			templateUserPrefs = getUserPreferences(templateUser);

		return templateUserPrefs;
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#modifyUserPreferences(org.nuclos.server.common.valueobject.PreferencesVO)
	 */
	@Override
	@RolesAllowed("Login")
	public void modifyUserPreferences(PreferencesVO prefsvo) throws CommonFinderException {
		setPreferencesForUser(getCurrentUserName(),prefsvo);
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#setPreferencesForUser(java.lang.String, org.nuclos.server.common.valueobject.PreferencesVO)
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void setPreferencesForUser(String sUserName, PreferencesVO prefsvo) throws CommonFinderException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));

		Integer userId = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		if (userId != null) {
			byte[] data = prefsvo == null ? new byte[]{} : convertToBadBytes(prefsvo.getPreferencesBytes());
			DataBaseHelper.getDbAccess().execute(DbStatementUtils.updateValues("T_MD_USER",
				"OBJPREFERENCES", data).where("INTID", userId));
		}
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getPreferencesForUser(java.lang.String)
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public PreferencesVO getPreferencesForUser(String sUserName) throws CommonFinderException {
		return getUserPreferences(sUserName);
	}

	@Override
	@RolesAllowed("UseManagementConsole")
	public void mergePreferencesForUser(String targetUser, Map<String, Map<String, String>> preferencesToMerge) throws CommonFinderException {
		try {
			NavigableMap<String, Map<String, String>> preferencesMap;
			PreferencesVO preferencesVO = getUserPreferences(targetUser);
			if (preferencesVO != null && preferencesVO.getPreferencesBytes().length > 0) {
				preferencesMap = PreferencesConverter.loadPreferences(
					new ByteArrayInputStream(preferencesVO.getPreferencesBytes()));
			} else {
				preferencesMap = new TreeMap<String, Map<String, String>>();
			}

			// Merge preferences
			preferencesMap.putAll(preferencesToMerge);

			// Save merged preferences
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PreferencesConverter.writePreferences(baos, preferencesMap, true);
			setPreferencesForUser(targetUser, new PreferencesVO(baos.toByteArray()));
		} catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private void handleWorkspaceBusinessExceptions(DbException ex) throws CommonBusinessException {
		boolean throwBusiness = false;
		if ("Workspace.name.in.use".equals(ex.getMessage())) {
			throwBusiness = true;
		} else if ("Workspace.not.found".equals(ex.getMessage())) {
			throwBusiness = true;
		}

		if (throwBusiness)
			throw new CommonBusinessException(ex.getMessage());
		else
			throw ex;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Collection<WorkspaceVO> getWorkspaceHeaderOnly() {
		List<WorkspaceVO> result = new ArrayList<WorkspaceVO>();
		result.addAll(getWorkspaceProcessor().getByUser(getCurrentUserName()));
		List<WorkspaceVO> newAssigned = getWorkspaceProcessor().getNewAssigned(getCurrentUserName());

		// remove first user workspace if:
		// 1. only one user workspace exists
		// 2. workspace is not assigned
		// 3. user has no right to create new workspaces
		// 1+2+3 = Initial start workspace
		// 4. at least one assigned workspace is new
		if (result.size() == 1
		 && result.get(0).getAssignedWorkspace() == null
		 && !SecurityCache.getInstance().getAllowedActions(getCurrentUserName()).contains(Actions.ACTION_WORKSPACE_CREATE_NEW)
		 && !newAssigned.isEmpty()) {
			removeWorkspace(result.get(0).getId());
			result.clear();
		}

		for (WorkspaceVO assignedWorkspace : newAssigned) {
			WorkspaceVO customizedWorkspace = new WorkspaceVO();
			customizedWorkspace.importHeader(assignedWorkspace.getWoDesc());
			customizedWorkspace.setClbworkspace(assignedWorkspace.getClbworkspace());
			customizedWorkspace.setAssignedWorkspace(assignedWorkspace.getId());
			try {
				result.add(storeWorkspace(customizedWorkspace));
			} catch (CommonBusinessException e) {
				// ignore. may be workspace name is in use by private workspace.
			}
		}

		// header only
		for (WorkspaceVO wovo : result) {
			wovo.getWoDesc().getFrames().clear();
			wovo.getWoDesc().removeAllEntityPreferences();
		}

		return result;
	}

	/**
	 *
	 * @param wovo
	 * @throws CommonBusinessException
	 */
	@Override
	public void storeWorkspaceHeaderOnly(WorkspaceVO wovo) throws CommonBusinessException {
		if (wovo.getAssignedWorkspace() != null && !isAssignWorkspaceAllowed()) {
			throw new CommonFatalException("Edit of assignable workspaces is not allowed!");
		}
		WorkspaceVO wovoDB = getWorkspaceProcessor().getByPrimaryKey(wovo.getId());

		wovoDB.importHeader(wovo.getWoDesc());

		DalUtils.updateVersionInformation(wovoDB, getCurrentUserName());
		wovoDB.flagUpdate();
		try {
			getWorkspaceProcessor().insertOrUpdate(wovoDB);
		} catch (DbException ex) {
			handleWorkspaceBusinessExceptions(ex);
		}
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@Override
	public WorkspaceVO getWorkspace(Long id) throws CommonBusinessException {
		WorkspaceVO wovo = getWorkspaceProcessor().getByPrimaryKey(id);
		if (wovo == null) {
			throw new CommonBusinessException("Workspace.not.found");
		}
		if (wovo.getAssignedWorkspace() != null) {
			if (!SecurityCache.getInstance().getAllowedActions(getCurrentUserName()).contains(Actions.ACTION_WORKSPACE_ASSIGN)) {
				return mergeWorkspaces(getWorkspaceProcessor().getByPrimaryKey(wovo.getAssignedWorkspace()), wovo);
			}
		}
		return wovo;
	}

	/**
	 *
	 * @param wovo
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	public WorkspaceVO storeWorkspace(WorkspaceVO wovo) throws CommonBusinessException {
		DalUtils.updateVersionInformation(wovo, getCurrentUserName());
		if (wovo.getId() == null) {
			wovo.setUser(SecurityCache.getInstance().getUserId(getCurrentUserName()).longValue());
			wovo.flagNew();
			wovo.setId(DalUtils.getNextId());
		} else {
			wovo.flagUpdate();
		}
		try {
			getWorkspaceProcessor().insertOrUpdate(wovo);
		} catch (DbException ex) {
			handleWorkspaceBusinessExceptions(ex);
		}
		return wovo;
	}

	/**
	 *
	 * @param wovo
	 * @return
	 * @throws CommonBusinessException
	 */
	private WorkspaceVO storeAssignableWorkspace(WorkspaceVO wovo) throws CommonBusinessException {
		DalUtils.updateVersionInformation(wovo, getCurrentUserName());
		wovo.setUser(null);
		if (wovo.getId() == null) {
			wovo.flagNew();
			wovo.setId(DalUtils.getNextId());
		} else {
			wovo.flagUpdate();
		}
		try {
			getWorkspaceProcessor().insertOrUpdate(wovo);
		} catch (DbException ex) {
			handleWorkspaceBusinessExceptions(ex);
		}
		return wovo;
	}

	/**
	 *
	 * @param id
	 */
	@Override
	public void removeWorkspace(Long id) {
		getWorkspaceProcessor().delete(id);
	}

	/**
	 *
	 * @param wovo (private or customized workspace)
	 * @param roleIds
	 * @throws CommonBusinessException
	 * return private or customized workspace
	 */
	@Override
	public WorkspaceVO assignWorkspace(WorkspaceVO wovo, Collection<Long> roleIds) throws CommonBusinessException {
		if (!isAssignWorkspaceAllowed()) {
			throw new CommonFatalException("Edit of assignable workspaces is not allowed!");
		}

		if (wovo.getAssignedWorkspace() == null) {
			// workspace is private, make it assignable

			if (!roleIds.isEmpty()) {
				WorkspaceVO assignableWovo = getWorkspace(wovo.getId());;
				WorkspaceVO customizedWovo = splitWorkspace(assignableWovo);

				storeAssignableWorkspace(assignableWovo);
				storeWorkspace(customizedWovo);

				modifyWorkspaceAssignments(roleIds, assignableWovo.getId());

				// transfer header only
				WorkspaceDescription customizedWoDesc = customizedWovo.getWoDesc();
				customizedWovo.setWoDesc(new WorkspaceDescription());
				customizedWovo.importHeader(customizedWoDesc);

				return customizedWovo;
			} else {
				return wovo;
			}

		} else {
			// assigned workspace
			if (roleIds.isEmpty()) {
				// revert assignment, make it private again

				Long assignableWovoId = wovo.getAssignedWorkspace();

				WorkspaceVO privateWovo = getWorkspace(wovo.getId());
				privateWovo.setAssignedWorkspace(null);
				storeWorkspace(privateWovo);

				removeWorkspace(assignableWovoId);

				// transfer header only
				WorkspaceDescription privateWoDesc = privateWovo.getWoDesc();
				privateWovo.setWoDesc(new WorkspaceDescription());
				privateWovo.importHeader(privateWoDesc);

				return privateWovo;
			} else {
				// modify assignments only
				modifyWorkspaceAssignments(roleIds, wovo.getAssignedWorkspace());

				return wovo; // workspace is customized
			}
		}
	}

	/**
	 *
	 * @param roleIds
	 * @param assignedWorkspace
	 */
	private void modifyWorkspaceAssignments(Collection<Long> roleIds, Long assignedWorkspace) {
		JdbcEntityObjectProcessor rowoProc = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ROLEWORKSPACE);

		List<EntityObjectVO> grantedRoles = rowoProc.getBySearchExpression(new CollectableSearchExpression(
				SearchConditionUtils.newEOidComparison(NuclosEntity.ROLEWORKSPACE.getEntityName(),
						"workspace",
						ComparisonOperator.EQUAL,
						assignedWorkspace, MetaDataServerProvider.getInstance())));

		// remove assignments
		for (EntityObjectVO grantedRole : grantedRoles) {
			if (!roleIds.contains(grantedRole.getFieldId("role"))) {
				rowoProc.delete(grantedRole.getId());

				// remove customized workspaces
				getWorkspaceProcessor().deleteByAssigned(assignedWorkspace);
			}
		}

		// add new assignments
		for (Long roleId : roleIds) {
			boolean alreadyGranted = false;
			if (grantedRoles != null) {
				for (EntityObjectVO grantedRole : grantedRoles) {
					if (roleId.equals(grantedRole.getFieldId("role"))) {
						alreadyGranted = true;
					}
				}
			}

			if (!alreadyGranted) {
				// assign role
				EntityObjectVO newGrant = EntityObjectVO.newObject(NuclosEntity.ROLEWORKSPACE.getEntityName());
				newGrant.setId(DalUtils.getNextId());
				DalUtils.updateVersionInformation(newGrant, getCurrentUserName());
				newGrant.getFieldIds().put("role", roleId);
				newGrant.getFieldIds().put("workspace", assignedWorkspace);
				rowoProc.insertOrUpdate(newGrant);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public Collection<EntityObjectVO> getAssignableRoles() {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ROLE).getAll();
	}

	/**
	 *
	 * @param assignedWorkspaceId
	 * @return
	 */
	public Collection<Long> getAssignedRoleIds(Long assignedWorkspaceId) {
		if (assignedWorkspaceId == null) {
			return new ArrayList<Long>();
		}

		List<EntityObjectVO> assignedRoles = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ROLEWORKSPACE).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
						NuclosEntity.ROLEWORKSPACE.getEntityName(),
						"workspace",
						ComparisonOperator.EQUAL,
						assignedWorkspaceId,
						MetaDataServerProvider.getInstance())));
		return CollectionUtils.transform(assignedRoles, new Transformer<EntityObjectVO, Long>() {
			@Override
			public Long transform(EntityObjectVO eovo) {
				return eovo.getFieldId("role");
			}

		});
	}

	/**
	 *
	 * @param assignableWovo
	 * @return customizedWovo
	 * @throws CommonBusinessException
	 */
	private WorkspaceVO splitWorkspace(WorkspaceVO assignableWovo) throws CommonBusinessException {
		WorkspaceVO customizedWorkspace = new WorkspaceVO();

		transferWorkspaceContent(assignableWovo, customizedWorkspace);
		removeNonMainFrames(assignableWovo);
		removeClosableTabs(assignableWovo.getWoDesc());
		flagTabsAssigned(assignableWovo.getWoDesc());

		customizedWorkspace.setAssignedWorkspace(assignableWovo.getId());
		assignableWovo.setUser(null);

		return customizedWorkspace;
	}

	/**
	 * Only EntityPreferences (TablePrefs etc.), no SubFormPreferences!
	 * @param customizedWovo
	 * @param ep
	 * @throws CommonBusinessException
	 */
	@Override
	public void publishEntityPreferences(WorkspaceVO customizedWovo, EntityPreferences ep) throws CommonBusinessException {
		if (!isAssignWorkspaceAllowed()) {
			throw new CommonFatalException("Edit of assignable workspaces is not allowed!");
		}

		final Long assignedWorkspaceId = customizedWovo.getAssignedWorkspace();
		if (assignedWorkspaceId != null) {
			WorkspaceVO assignableWovo = getWorkspaceProcessor().getByPrimaryKey(assignedWorkspaceId);
			// transfer SubFormPreferences
			List<SubFormPreferences> transferSfp = assignableWovo.getWoDesc().getEntityPreferences(ep.getEntity()).getSubFormPreferences();
			ep.removeAllSubFormPreferences();
			ep.addAllSubFormPreferences(transferSfp);
			assignableWovo.getWoDesc().removeEntityPreferences(ep);
			assignableWovo.getWoDesc().addEntityPreferences(ep);
			storeWorkspace(assignableWovo);
		} else {
			throw new IllegalArgumentException("Workspace is not assigned, publish not possible!");
		}
	}

	/**
	 *
	 * @param customizedWovo
	 * @param entity
	 * @param sfp
	 * @throws CommonBusinessException
	 */
	@Override
	public void publishSubFormPreferences(WorkspaceVO customizedWovo, String entity, SubFormPreferences sfp) throws CommonBusinessException {
		if (!isAssignWorkspaceAllowed()) {
			throw new CommonFatalException("Edit of assignable workspaces is not allowed!");
		}

		final Long assignedWorkspaceId = customizedWovo.getAssignedWorkspace();
		if (assignedWorkspaceId != null) {
			WorkspaceVO assignableWovo = getWorkspaceProcessor().getByPrimaryKey(assignedWorkspaceId);
			EntityPreferences assignableEp = assignableWovo.getWoDesc().getEntityPreferences(entity);
			assignableEp.removeSubFormPreferences(sfp);
			assignableEp.addSubFormPreferences(sfp);
			storeWorkspace(assignableWovo);
		} else {
			throw new IllegalArgumentException("Workspace is not assigned, publish not possible!");
		}
	}

	/**
	 *
	 * @param customizedWovo
	 * @param isPublishStructureChange
	 * @param isPublishStructureUpdate
	 * @param isPublishStarttabConfiguration
	 * @param isPublishToolbarConfiguration
	 * @param isPublishTableColumnConfiguration
	 * @throws CommonBusinessException
	 */
	@Override
	public void publishWorkspaceChanges(WorkspaceVO customizedWovo, boolean isPublishStructureChange, boolean isPublishStructureUpdate, boolean isPublishStarttabConfiguration, boolean isPublishTableColumnConfiguration, boolean isPublishToolbarConfiguration) throws CommonBusinessException {
		if (!isAssignWorkspaceAllowed()) {
			throw new CommonFatalException("Edit of assignable workspaces is not allowed!");
		}

		WorkspaceVO customizedWovoFromDb = getWorkspace(customizedWovo.getId());
		WorkspaceVO assignableWovo = getWorkspace(customizedWovoFromDb.getAssignedWorkspace());

		/**
		 * STRUCTURE CHANGE - implements STRUCTURE UPDATE, STARTTAB CONFIGURATIONs
		 */
		if (isPublishStructureChange) {
			transferWorkspaceContent(customizedWovoFromDb, assignableWovo);
			removeNonMainFrames(assignableWovo);
			removeClosableTabs(assignableWovo.getWoDesc());
			flagTabsAssigned(assignableWovo.getWoDesc());
		} else {
			// check before publish
			if (isStructureChanged(assignableWovo.getWoDesc().getMainFrame().getContent(), customizedWovoFromDb.getWoDesc().getMainFrame().getContent())) {
				throw new CommonFatalException("Workspace structure differs");
			}

			WorkspaceVO assignableBackupWovo = getWorkspace(customizedWovoFromDb.getAssignedWorkspace());

			/**
			 * STRUCTURE UPDATE - implements STARTTAB CONFIGURATIONs
			 */
			if (isPublishStructureUpdate) {
				transferWorkspaceContent(customizedWovoFromDb, assignableWovo);
				removeNonMainFrames(assignableWovo);
				removeClosableTabs(assignableWovo.getWoDesc());
				flagTabsAssigned(assignableWovo.getWoDesc());

				if (!isPublishStarttabConfiguration) {
					/**
					 * NO STARTTAB CONFIGURATIONs - restore from backup
					 */
					transferStarttabConfiguration(assignableBackupWovo.getWoDesc().getMainFrame().getContent(), assignableWovo.getWoDesc().getMainFrame().getContent());
				}
			} else {
				/**
				 * STARTTAB CONFIGURATIONs without STRUCTURE UPDATE
				 */
				if (isPublishStarttabConfiguration) {
					transferStarttabConfiguration(customizedWovoFromDb.getWoDesc().getMainFrame().getContent(), assignableWovo.getWoDesc().getMainFrame().getContent());
				}
			}
		}

		/**
		 * TABLE COLUMNs
		 */
		if (isPublishTableColumnConfiguration) {
			assignableWovo.getWoDesc().removeAllEntityPreferences();
			assignableWovo.getWoDesc().addAllEntityPreferences(customizedWovoFromDb.getWoDesc().getEntityPreferences());
		}

		/**
		 * TOOLBARs
		 */
		if (isPublishToolbarConfiguration) {
			// TODO implement
		}

		storeWorkspace(assignableWovo);
	}

	/**
	 *
	 * @param ncSource
	 * @param ncTarget
	 */
	private void transferStarttabConfiguration(NestedContent ncSource, NestedContent ncTarget) {
		if (ncSource instanceof MutableContent) {
			transferStarttabConfiguration(((MutableContent) ncSource).getContent(), ((MutableContent) ncTarget).getContent());
		} else if (ncSource instanceof Split) {
			transferStarttabConfiguration(((Split) ncSource).getContentA(), ((Split) ncTarget).getContentA());
			transferStarttabConfiguration(((Split) ncSource).getContentB(), ((Split) ncTarget).getContentB());
		} else if (ncSource instanceof Tabbed) {
			final Tabbed tbbSource = (Tabbed) ncSource;
			final Tabbed tbbTarget = (Tabbed) ncTarget;

			tbbTarget.getPredefinedEntityOpenLocations().clear();
			tbbTarget.addAllPredefinedEntityOpenLocations(new ArrayList<String>(tbbSource.getPredefinedEntityOpenLocations()));

			tbbTarget.getReducedStartmenus().clear();
			tbbTarget.addAllReducedStartmenus(tbbSource.getReducedStartmenus());

			tbbTarget.setAlwaysHideBookmark(tbbSource.isAlwaysHideBookmark());
			tbbTarget.setAlwaysHideHistory(tbbSource.isAlwaysHideHistory());
			tbbTarget.setAlwaysHideStartmenu(tbbSource.isAlwaysHideStartmenu());
			tbbTarget.setNeverHideBookmark(tbbSource.isNeverHideBookmark());
			tbbTarget.setNeverHideHistory(tbbSource.isNeverHideHistory());
			tbbTarget.setNeverHideStartmenu(tbbSource.isNeverHideStartmenu());
			tbbTarget.setShowAdministration(tbbSource.isShowAdministration());
			tbbTarget.setShowConfiguration(tbbSource.isShowConfiguration());
			tbbTarget.setShowEntity(tbbSource.isShowEntity());

			tbbTarget.setDesktop(tbbSource.getDesktop());
		} else {
			throw new UnsupportedOperationException("Unknown NestedContent type: " + ncSource.getClass());
		}
	}

	/**
	 *
	 * @param customizedWovo
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	public WorkspaceVO restoreWorkspace(WorkspaceVO customizedWovo) throws CommonBusinessException {
		if (customizedWovo.getAssignedWorkspace() == null) {
			throw new CommonFatalException("Workspace is not assigned!");
		}

		WorkspaceVO assignableWovo = getWorkspace(customizedWovo.getAssignedWorkspace());

		if (customizedWovo.getWoDesc().isAlwaysReset()) {
			customizedWovo.getWoDesc().getFrames().clear();
		}

		transferWorkspaceContent(assignableWovo, customizedWovo);
		storeWorkspace(customizedWovo);

		// only header
		customizedWovo.getWoDesc().getFrames().clear();
		customizedWovo.getWoDesc().removeAllEntityPreferences();

		return customizedWovo;
	}

	/**
	 *
	 * @param sourceWovo
	 * @param targetWovo
	 * @throws CommonBusinessException
	 */
	private void transferWorkspaceContent(WorkspaceVO sourceWovo, WorkspaceVO targetWovo) throws CommonBusinessException {
		targetWovo.importHeader(sourceWovo.getWoDesc());

		// clone WorkspaceDescription
		targetWovo.setClbworkspace(sourceWovo.getClbworkspace());
	}

	/**
	 *
	 * @param wovo
	 * @throws CommonBusinessException
	 */
	private void removeNonMainFrames(WorkspaceVO wovo) throws CommonBusinessException {
		// only main frame in target
		Frame mainFrame = wovo.getWoDesc().getMainFrame();
		wovo.getWoDesc().getFrames().clear();
		wovo.getWoDesc().addFrame(mainFrame);

		// check for home and tree tabbed in main frame
		wovo.getWoDesc().getHomeTabbed();
		wovo.getWoDesc().getHomeTreeTabbed();
	}

	/**
	 *
	 * @param wd
	 */
	private void flagTabsAssigned(WorkspaceDescription wd) {
		for (Frame frame : wd.getFrames()) {
			for (Tab tab : getTabs(frame.getContent())) {
				tab.setFromAssigned(true);
			}
		}
	}

	/**
	 *
	 * @param wd
	 */
	private void removeClosableTabs(WorkspaceDescription wd) {
		for (Frame frame : wd.getFrames()) {
			removeClosableTabs(frame.getContent());
		}
	}

	/**
	 *
	 * @param nc
	 */
	private void removeClosableTabs(NestedContent nc) {
		if (nc instanceof MutableContent) {
			removeClosableTabs(((MutableContent) nc).getContent());
		} else if (nc instanceof Split) {
			removeClosableTabs(((Split) nc).getContentA());
			removeClosableTabs(((Split) nc).getContentB());
		} else if (nc instanceof Tabbed) {
			for (Tab tab : ((Tabbed) nc).getTabs()) {
				if (!tab.isNeverClose()) {
					((Tabbed) nc).removeTab(tab);
				}
			}
		} else {
			throw new UnsupportedOperationException("Unknown NestedContent type: " + nc.getClass());
		}
	}

	/**
	 *
	 * @param ncAssigned
	 * @param ncCustomized
	 */
	private void mergeTabbeds(NestedContent ncAssigned, NestedContent ncCustomized) {
		if (ncAssigned instanceof MutableContent) {
			mergeTabbeds(((MutableContent) ncAssigned).getContent(), ((MutableContent) ncCustomized).getContent());
		} else if (ncAssigned instanceof Split) {
			mergeTabbeds(((Split) ncAssigned).getContentA(), ((Split) ncCustomized).getContentA());
			mergeTabbeds(((Split) ncAssigned).getContentB(), ((Split) ncCustomized).getContentB());
		} else if (ncAssigned instanceof Tabbed) {
			final Tabbed tbbAssigned = (Tabbed) ncAssigned;
			final Tabbed tbbCustomized = (Tabbed) ncCustomized;

			// add never closable tabs
			final List<Tab> tabsAssigned = tbbAssigned.getTabs();
			final List<Tab> tabsCustomized = tbbCustomized.getTabs();
			for (Tab tab : tabsAssigned) {
				if (!tabsCustomized.contains(tab)) {
					((Tabbed) ncCustomized).addTab(tab);
				}
			}

			// start tab configuration
			if (!SecurityCache.getInstance().getAllowedActions(getCurrentUserName()).contains(Actions.ACTION_WORKSPACE_CUSTOMIZE_STARTTAB)) {
				tbbCustomized.getPredefinedEntityOpenLocations().clear();
				tbbCustomized.addAllPredefinedEntityOpenLocations(new ArrayList<String>(tbbAssigned.getPredefinedEntityOpenLocations()));

				tbbCustomized.getReducedStartmenus().clear();
				tbbCustomized.addAllReducedStartmenus(tbbAssigned.getReducedStartmenus());

				tbbCustomized.setAlwaysHideBookmark(tbbAssigned.isAlwaysHideBookmark());
				tbbCustomized.setAlwaysHideHistory(tbbAssigned.isAlwaysHideHistory());
				tbbCustomized.setAlwaysHideStartmenu(tbbAssigned.isAlwaysHideStartmenu());
				tbbCustomized.setNeverHideBookmark(tbbAssigned.isNeverHideBookmark());
				tbbCustomized.setNeverHideHistory(tbbAssigned.isNeverHideHistory());
				tbbCustomized.setNeverHideStartmenu(tbbAssigned.isNeverHideStartmenu());
				tbbCustomized.setShowAdministration(tbbAssigned.isShowAdministration());
				tbbCustomized.setShowConfiguration(tbbAssigned.isShowConfiguration());
				tbbCustomized.setShowEntity(tbbAssigned.isShowEntity());

				tbbCustomized.setDesktop(tbbAssigned.getDesktop());
			}
		} else {
			throw new UnsupportedOperationException("Unknown NestedContent type: " + ncAssigned.getClass());
		}
	}

	/**
	 *
	 * @param assignableWovo
	 * @param customizedWovo
	 * @return
	 * @throws CommonBusinessException
	 */
	private WorkspaceVO mergeWorkspaces(WorkspaceVO assignableWovo, WorkspaceVO customizedWovo) throws CommonBusinessException {
		final WorkspaceVO mergedWovo = customizedWovo;
		final WorkspaceDescription wdm = mergedWovo.getWoDesc();
		final WorkspaceDescription wda = assignableWovo.getWoDesc();
		wdm.importHeader(wda);

		// list of frames is ordered by use (last on top)
		// --> merge only mainframe
		if (isStructureChanged(wdm.getMainFrame(), wda.getMainFrame())) {

			// backup open tabs
			final List<Tab> tabsToMove = getTabs(wdm.getMainFrame().getContent());
			final List<Tab> tabsSource = getTabs(wda.getMainFrame().getContent());

			// transfer structure
			wdm.getMainFrame().getContent().setContent(wda.getMainFrame().getContent().getContent());
			final Tabbed homeTabbed = wdm.getHomeTabbed();

			// move tabs
			for (Tab ttm : tabsToMove) {
				if (!tabsSource.contains(ttm)) {
					homeTabbed.addTab(ttm);
				}
			}

			// remove home tabbed in other frames
			for (Frame f : wdm.getFrames()) {
				if (!f.isMainFrame()) {
					removeHomeTabbedFlags(f.getContent());
				}
			}
		}

		// same structure in merged and assigned
		// transfer assigned never closable tabs, start tab configuration...
		mergeTabbeds(wda.getMainFrame().getContent(), wdm.getMainFrame().getContent());

		// column preferences
		if (!SecurityCache.getInstance().getAllowedActions(getCurrentUserName()).contains(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)) {
			wdm.removeAllEntityPreferences();
			wdm.addAllEntityPreferences(wda.getEntityPreferences());
		}

		return mergedWovo;
	}

	/**
	 *
	 * @param id1
	 * @param id2
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
	public boolean isWorkspaceStructureChanged(Long id1, Long id2) throws CommonBusinessException {
		return isStructureChanged(
				getWorkspaceProcessor().getByPrimaryKey(id1).getWoDesc().getMainFrame(),
				getWorkspaceProcessor().getByPrimaryKey(id2).getWoDesc().getMainFrame());
	}

	private boolean isStructureChanged(Frame f1, Frame f2) {
		return isStructureChanged(f1.getContent(), f2.getContent());
	}

	private boolean isStructureChanged(NestedContent nc1, NestedContent nc2) {
		if (equalsContent(nc1, nc2)) {
			if (nc1 instanceof MutableContent) {
				return isStructureChanged(((MutableContent) nc1).getContent(), ((MutableContent) nc2).getContent());
			} else if (nc1 instanceof Split) {
				return isStructureChanged(((Split) nc1).getContentA(), ((Split) nc2).getContentA())
						|| isStructureChanged(((Split) nc1).getContentB(), ((Split) nc2).getContentB());
			} else if (nc1 instanceof Tabbed) {
				return false;
			} else {
				throw new UnsupportedOperationException("Unknown NestedContent type: " + nc1.getClass());
			}
		} else {
			return true;
		}
	}

	/**
	 *
	 * @param nc1
	 * @param nc2
	 * @return
	 */
	private boolean equalsContent(NestedContent nc1, NestedContent nc2) {
		if (nc1 instanceof MutableContent) {
			return (nc2 instanceof MutableContent);
		} else if (nc1 instanceof Split) {
			return (nc2 instanceof Split);
		} else if (nc1 instanceof Tabbed) {
			return (nc2 instanceof Tabbed);
		} else {
			throw new UnsupportedOperationException("Unknown NestedContent type: " + nc1.getClass());
		}
	}

	/**
	 *
	 * @param nc
	 * @return
	 */
	private List<Tab> getTabs(NestedContent nc) {
		List<Tab> result = new ArrayList<Tab>();
		if (nc instanceof MutableContent) {
			result.addAll(getTabs(((MutableContent) nc).getContent()));
		} else if (nc instanceof Split) {
			result.addAll(getTabs(((Split) nc).getContentA()));
			result.addAll(getTabs(((Split) nc).getContentB()));
		} else if (nc instanceof Tabbed) {
			result.addAll(((Tabbed) nc).getTabs());
		}
		return result;
	}

	/**
	 *
	 * @param nc
	 */
	private void removeHomeTabbedFlags(NestedContent nc) {
		if (nc instanceof MutableContent) {
			removeHomeTabbedFlags(((MutableContent) nc).getContent());
		} else if (nc instanceof Split) {
			removeHomeTabbedFlags(((Split) nc).getContentA());
			removeHomeTabbedFlags(((Split) nc).getContentB());
		} else if (nc instanceof Tabbed) {
			((Tabbed) nc).setHome(false);
			((Tabbed) nc).setHomeTree(false);
		}
	}

	/**
	 *
	 * @return
	 */
	private boolean isAssignWorkspaceAllowed() {
		return SecurityCache.getInstance().getAllowedActions(getCurrentUserName()).contains(Actions.ACTION_WORKSPACE_ASSIGN);
	}

	/**
	 * @param sUserName
	 * @return the entity bean corresponding to the user with the given name
	 * @throws CommonFinderException
	 */
	private PreferencesVO getUserPreferences(String sUserName) throws CommonFinderException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<byte[]> query = builder.createQuery(byte[].class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("OBJPREFERENCES", byte[].class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));

		try {
			byte[] b = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			if (b != null) {
				return new PreferencesVO(convertFromBadBytes(b));
			}
		} catch (DbInvalidResultSizeException e) {
			throw new CommonFinderException("There are no stored preferences for the current user.");
		}
		return null;
	}

	// Previously, the preferences were transferred as Strings.  This is obviously wrong because they
	// are bytes (Java's Preferences export/import API works with byte streams).  Moreover, client and
	// servers used different charsets to encode/decode the bytes (UTF-8 on the client side, and the
	// server's native encoding in this Facade)
	// => the stored BLOBs are broken (invalid XML w.r.t encoding)!
	//
	// The following code imitates the old behavior to work around these issues.  If DB migrations are
	// possible, please migrate all BLOBs once using convertFromBadBytes (from native->UTF-8)
	//
	// For a test case, use preferences with some umlauts!

	@Deprecated
	private static byte[] convertFromBadBytes(byte[] b) {
		// interpret bytes as native encoded string and convert them back using UTF-8
		return new String(b).getBytes(UTF_8);
	}

	@Deprecated
	private static byte[] convertToBadBytes(byte[] b) {
		// interpret bytes as UTF-8 string and convert them back using the native encoding
		return new String(b, UTF_8).getBytes();
	}

	private final static Charset UTF_8 = Charset.forName("UTF-8");

	private IWorkspaceProcessor getWorkspaceProcessor() {
		return NucletDalProvider.getInstance().getWorkspaceProcessor();
	}
}

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
package org.nuclos.client.updatejobs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.ejb.CreateException;

import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeRemote;

public class MigrateSearchFilterPreferences {

	private static final String PREFS_NODE_SEARCHFILTERS = "searchFilters";
	private static final String PREFS_NODE_GLOBALSEARCHFILTERS = "globalSearchFilters";

	private static final String PREFS_KEY_DESCRIPTION = "description";
	private static final String PREFS_KEY_MODULEID = "moduleId";
	private static final String PREFS_KEY_SEARCHDELETED = "searchDeleted";

	private static final String PREFS_NODE_MAINFRAME = "mainFrame";

	private static final String PREFS_KEY_GLOBALSEARCHFILTERNAME = "globalSearchFilterName";

	private static final String PREFS_NODE_TASKPANEL = "taskPanel";
	private static final String PREFS_NODE_TASKPANELFILTERS = "filters";

	private static Integer iUserId;
	private static String sUserName;

	public static void migrate(String sMigrationUser) throws RemoteException, CreateException {
		if(StringUtils.isNullOrEmpty(sMigrationUser)) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.1", "Der Benutzer, der f\u00fcr die Migration der Suchfilter verwendet wird ist nicht gesetzt!"));
		}

		// remember preferences of migration user
		PreferencesVO preferencesOfMigrationUser = null;
		MasterDataVO mdVOOfMigrationUser = null;
		try {
			preferencesOfMigrationUser = getPreferencesFacade().getPreferencesForUser(sMigrationUser);
		}
		catch (CommonFinderException e) {
			// no prefs found for user
		}

		for (MasterDataVO mdVO_user : getMasterDataFacade().getMasterData(NuclosEntity.USER.getEntityName(), null, true)) {
			try {
				iUserId = mdVO_user.getIntId();
				sUserName = (String)mdVO_user.getField("name");

				if (sMigrationUser.equals(sUserName)) {
					mdVOOfMigrationUser = mdVO_user;
					continue;
				}

				System.out.println("Migrate Searchfilter for User: "+ sUserName);

				PreferencesVO prefsVO = null;
				try {
					prefsVO = getPreferencesFacade().getPreferencesForUser((String)mdVO_user.getField("name"));
				}
				catch (CommonFinderException e) {
					// no prefs found for user
					continue;
				}

				migrate(mdVO_user, prefsVO);
			}
			catch (Exception e) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.2", "Es ist ein Fehler beim Verschieben der Suchfilter aufgetreten!"), e);
			}
		}

		System.out.println("Migrate Searchfilter for Migration User: "+ sMigrationUser);
		// migrate preferences of migration user
		if (mdVOOfMigrationUser != null && preferencesOfMigrationUser != null) {
			migrate(mdVOOfMigrationUser, preferencesOfMigrationUser);
		}
	}

	private static void migrate(MasterDataVO mdVO_user, PreferencesVO prefsVO) {
		try {
			final ByteArrayInputStream is = new ByteArrayInputStream(prefsVO.getPreferencesBytes());
			Preferences prefs = Preferences.userRoot().node("org/nuclos/client");
			prefs.removeNode();
			Preferences.importPreferences(is);
			prefs = Preferences.userRoot().node("org/nuclos/client");


			migrateSearchFilters(prefs, PREFS_NODE_SEARCHFILTERS);
			migrateSearchFilters(prefs, PREFS_NODE_GLOBALSEARCHFILTERS);
			migrateGlobalSearchFilter(prefs);
			migrateTaskPanelFilters(prefs);

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			prefs.exportSubtree(os);
			byte[] bytes = os.toByteArray();

			getPreferencesFacade().setPreferencesForUser((String)mdVO_user.getField("name"), new PreferencesVO(bytes));

			prefs.removeNode();
		}
		catch (Exception e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.2", "Es ist ein Fehler beim Verschieben der Suchfilter aufgetreten!"), e);
		}
	}

	private static void migrateSearchFilters(Preferences prefs, String sPrefsFilter) {
		try {
			if (!prefs.nodeExists(sPrefsFilter)) {
				return;
			}

			final Preferences prefsSearchFilters = prefs.node(sPrefsFilter);

			for (String sFilterName : prefsSearchFilters.childrenNames()) {
				System.out.println("Migrate Searchfilter: "+ sFilterName);

				String sDescription = "";
				Integer iEntity = null;
				Integer iSearchDeleted = null;

				Preferences prefsSearchFilter = prefsSearchFilters.node(sFilterName);
				iEntity = prefsSearchFilter.getInt(PREFS_KEY_MODULEID, 0);

				sDescription = prefsSearchFilter.get(PREFS_KEY_DESCRIPTION, "");
				iSearchDeleted = new Integer(prefsSearchFilter.getInt(PREFS_KEY_SEARCHDELETED, 0));

//				// remove preferences properties
//				prefsSearchFilter.remove(PREFS_KEY_MODULEID);
//				prefsSearchFilter.remove(PREFS_KEY_DESCRIPTION);
//				prefsSearchFilter.remove(PREFS_KEY_SEARCHDELETED);

				// special handling for generalsearch
				if (iEntity == null) {
					iEntity = -1;
				}
				else {
					iEntity = MetaDataCache.getInstance().getMetaData(Modules.getInstance().getEntityNameByModuleId(iEntity)).getId();
				}

				// write entity 'searchfilter'
				MasterDataVO mdVO_searchfilter = new MasterDataVO(MetaDataCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTER), true);
				mdVO_searchfilter.setField("name", sFilterName);
				mdVO_searchfilter.setField("description", sDescription);

				if (iEntity != null) {
					mdVO_searchfilter.setField("entityId", iEntity);
				}

				// SEARCH_UNDELETED = 0;
				// SEARCH_DELETED = 1;
				// SEARCH_BOTH = 2;

//				switch (iSearchDeleted) {
//				case 0:
//				sSearchDeleted = "Nur ungel\u00f6schte anzeigen";
//				break;
//				case 1:
//				sSearchDeleted = "Nur gel\u00f6schte anzeigen";
//				break;
//				case 2:
//				sSearchDeleted = "Ungel\u00f6schte und gel\u00f6schte anzeigen";
//				break;
//				}

				mdVO_searchfilter.setField("searchDeleted", iSearchDeleted);

				if (PREFS_NODE_GLOBALSEARCHFILTERS.equals(sPrefsFilter)) {
					mdVO_searchfilter.setField("globalSearch", Boolean.TRUE);
				}

				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				prefsSearchFilter.exportSubtree(baos);
				final String sXml = baos.toString("UTF-8");

				mdVO_searchfilter.setField("clbsearchfilter", sXml);

				DependantMasterDataMap dmdm = new DependantMasterDataMap();

				// write entity 'searchfilter_user'
				MasterDataVO mdVO_searchfilter_user = new MasterDataVO(MetaDataCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTERUSER), true);

				mdVO_searchfilter_user.setField("userId", iUserId);
				mdVO_searchfilter_user.setField("forcefilter", Boolean.FALSE);
				mdVO_searchfilter_user.setField("editable", Boolean.TRUE);
				mdVO_searchfilter_user.setField("validFrom", null);
				mdVO_searchfilter_user.setField("validUntil", null);

				dmdm.addData(NuclosEntity.SEARCHFILTERUSER.getEntityName(), DalSupportForMD.getEntityObjectVO(mdVO_searchfilter_user));

				Integer iId = MasterDataDelegate.getInstance().create(NuclosEntity.SEARCHFILTER.getEntityName(), mdVO_searchfilter, dmdm).getIntId();

				getSearchFilterFacade().changeCreatedUser(iId, sUserName);

				prefsSearchFilter.removeNode();

//				final ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
//				Preferences prefs = new DummyPreferences();
//				prefs.importPreferences(is);
//				CollectableSearchCondition cond = SearchConditionUtils.getSearchCondition(prefs, sEntity);
			}

			prefsSearchFilters.removeNode();
		}
		catch (Exception e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.2", "Es ist ein Fehler beim Verschieben der Suchfilter aufgetreten!"));
		}
	}

	private static void migrateGlobalSearchFilter(Preferences prefs) {
		try {
			if (!prefs.nodeExists(PREFS_NODE_MAINFRAME)) {
				return;
			}

			final Preferences prefsGlobalSearchFilter = prefs.node(PREFS_NODE_MAINFRAME);
			final String sFilterName = prefsGlobalSearchFilter.get(PREFS_KEY_GLOBALSEARCHFILTERNAME, "");

			if (sFilterName != null && !sFilterName.equals(CommonLocaleDelegate.getMessage("GlobalSearchFilter.1", "<Alle>"))) {
				System.out.println("Migrate Global Searchfilter: "+ sFilterName);

				Integer iId = getSearchFilterId(sFilterName, sUserName);
				if (iId != null) {
					prefsGlobalSearchFilter.putInt(PREFS_KEY_GLOBALSEARCHFILTERNAME, iId);
				}
			}

		}
		catch (Exception e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.3", "Es ist ein Fehler bei der Migration des globalen Suchfilters aufgetreten!"));
		}
	}

	private static void migrateTaskPanelFilters(Preferences prefs) {
		try {
			if (!prefs.nodeExists(PREFS_NODE_TASKPANEL)) {
				return;
			}

			final Preferences prefsTaskPanel = prefs.node(PREFS_NODE_TASKPANEL);

			if (!prefsTaskPanel.nodeExists(PREFS_NODE_TASKPANELFILTERS)) {
				return;
			}

			final Preferences prefsTaskPanelFilters = prefsTaskPanel.node(PREFS_NODE_TASKPANELFILTERS);

			for (String sKey : prefsTaskPanelFilters.keys()) {
				if ("size".equals(sKey)) {
					continue;
				}

				String sFilter = prefsTaskPanelFilters.get(sKey, null);

				if (StringUtils.isNullOrEmpty(sFilter)) {
					continue;
				}

				boolean initialRun = false;

				Integer moduleID = null;
				String filterName = null;
				int sepIndex = sFilter.lastIndexOf("_");
				if (sepIndex >= 0) {
					try {
						moduleID = new Integer(sFilter.substring(
								sFilter.lastIndexOf("_") + 1, sFilter
								.length()));
						filterName = sFilter.substring(0, sepIndex);
						if (moduleID == null) {
							initialRun = true;
						}
					}
					catch (RuntimeException e) {
						sFilter.substring(sFilter.lastIndexOf("_")+1, sFilter.length());
						filterName = sFilter.substring(0, sepIndex);
						// TODO Auto-generated catch block
						initialRun = true;
					}
				}
				else {
					initialRun = true;
				}
				if (initialRun) {
					System.out.println("Migrate TaslPanel Searchfilter: "+ filterName);

					Integer iId = getSearchFilterId(filterName, sUserName);
					if (iId != null) {
						prefsTaskPanelFilters.putInt(sKey, iId);
					}
				}
//				else if (moduleID.intValue() == -1) {
//				if (filterName.toLowerCase().indexOf("fristen") >= 0) {
//				pnlTasks.getTabbedPane().addTab("Fristen",
//				pnlTasks.getTimelimitTaskView());
//				}
//				else {
//				pnlTasks.getTabbedPane().addTab("Meine Aufgaben",
//				pnlTasks.getPersonalTaskView());
//				}
//				}
				else {
					System.out.println("Migrate TaslPanel Searchfilter: "+ filterName);

					Integer iId = getSearchFilterId(filterName, sUserName);
					if (iId != null) {
						prefsTaskPanelFilters.putInt(sKey, iId);
					}
				}
			}
		}
		catch (Exception e) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("MigrateSearchFilterPreferences.3", "Es ist ein Fehler bei der Migration des globalen Suchfilters aufgetreten!"));
		}
	}

	private static Integer getSearchFilterId(String sFilter, String sUserName) throws CreateException, RemoteException {
		if (StringUtils.isNullOrEmpty(sFilter) || StringUtils.isNullOrEmpty(sUserName)) {
			System.out.println();
		}

		ArrayList<CollectableSearchCondition> conditions = new ArrayList<CollectableSearchCondition>();
		conditions.add(SearchConditionUtils.newMDComparison(MetaDataCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTER), "name", ComparisonOperator.EQUAL, sFilter));
		conditions.add(SearchConditionUtils.newMDComparison(MetaDataCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTER), "createdUser", ComparisonOperator.EQUAL, sUserName));

		TruncatableCollection<MasterDataVO> collmdvo = getMasterDataFacade().getMasterData(NuclosEntity.SEARCHFILTER.getEntityName(), new CompositeCollectableSearchCondition(LogicalOperator.AND, conditions), true);

		assert collmdvo.size() <= 1;

		if (collmdvo.isEmpty()) {
			return null;
		}

		return collmdvo.iterator().next().getIntId();

	}

	private static MasterDataFacadeRemote getMasterDataFacade() {
		try {
			return ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
		}
		catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	private static PreferencesFacadeRemote getPreferencesFacade() throws CreateException, RemoteException {
		try {
			return ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	private static SearchFilterFacadeRemote getSearchFilterFacade() throws CreateException, RemoteException {
		try {
			return ServiceLocator.getInstance().getFacade(SearchFilterFacadeRemote.class);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}
}

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
package org.nuclos.client.common;

import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.DynamicEntitySubFormController;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.ImportAttributeSubFormController;
import org.nuclos.client.masterdata.ImportForeignEntityIdentifierSubFormController;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;

/**
 * Factory that creates any <code>NuclosCollectController</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosCollectControllerFactory {
	private static NuclosCollectControllerFactory singleton;

	protected NuclosCollectControllerFactory() {
	}

	public static synchronized NuclosCollectControllerFactory getInstance() {
		if (singleton == null) {
			singleton = newFactory();
		}
		return singleton;
	}

	private static NuclosCollectControllerFactory newFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getCollectControllerFactoryClassName(),
					NuclosCollectControllerFactory.class.getName());

			return (NuclosCollectControllerFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("CollectControllerFactory could not be created.", ex);//CollectControllerFactory kann nicht erzeugt werden
		}
	}

	/**
	 * All specialized <code>CollectController</code>s that do not extend
	 * <code>MasterDataCollectController</code> or <code>GenericObjectCollectController</code>
	 * must be taken care of here.
	 * @param parent
	 * @param sEntityName
	 * @return
	 * @throws org.nuclos.common.NuclosBusinessException
	 * @throws CommonFatalException
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */	
	public NuclosCollectController<?> newCollectController(String sEntityName, MainFrameTab tabIfAny) throws NuclosBusinessException, CommonPermissionException, CommonFatalException {
		if (sEntityName == null) {
			throw new NullArgumentException("sEntityName");
		}

		// check if the entity is a generic object entity:
		Integer iModuleId = null;
		try {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(sEntityName);
		}
		catch (NoSuchElementException ex) {
			iModuleId = null;
		}

		if (iModuleId != null) {
			Statemodel sm = StateDelegate.getInstance().getStatemodel(new UsageCriteria(iModuleId, null, null));
			if(sm == null) {
				JOptionPane.showMessageDialog(
					Main.getInstance().getMainFrame(),
					SpringLocaleDelegate.getInstance().getMessage(
							"NuclosCollectControllerFactory.2", "Es ist kein Statusmodell definiert. Das Modul kann nicht geöffnet werden.\nDie Statusmodellverwaltung finden Sie im Menü Konfiguration."),
					sEntityName,
					JOptionPane.WARNING_MESSAGE);
				return null;
			}
			return newGenericObjectCollectController(iModuleId, tabIfAny);
		} else {
			if(SecurityCache.getInstance().isReadAllowedForMasterData(sEntityName)) {

				MasterDataLayoutHelper.checkLayoutMLExistence(sEntityName);

				NuclosEntity systemEntity = NuclosEntity.getByName(sEntityName);
				if (systemEntity != null) {
					final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
					switch (systemEntity) {
					case DATASOURCE:
						return factory.newDatasourceCollectController(tabIfAny);
					case DYNAMICENTITY:
						return factory.newDynamicEntityCollectController(tabIfAny);
					case VALUELISTPROVIDER:
						return factory.newValuelistProviderCollectController(tabIfAny);
					case RECORDGRANT:
						return factory.newRecordGrantCollectController(tabIfAny);
					case STATEMODEL:
						return factory.newStateModelCollectController(tabIfAny);
					case PROCESSMONITOR:
						return factory.newProcessMonitorCollectController(tabIfAny);
					case INSTANCE:
						return factory.newInstanceCollectController(sEntityName, tabIfAny);
					case RULE:
						return factory.newRuleCollectController(tabIfAny);
					case IMPORT:
						return factory.newGenericObjectImportStructureCollectController(tabIfAny);
					case IMPORTFILE:
						return factory.newGenericObjectImportCollectController(tabIfAny);
					case TIMELIMITRULE:
						return factory.newTimelimitRuleCollectController(tabIfAny);
					case JOBCONTROLLER:
						return factory.newJobControlCollectController(tabIfAny);
					case LOCALE:
						return factory.newLocaleCollectController(tabIfAny);
					case NUCLET:
						return factory.newNucletCollectController(tabIfAny);
					case DBOBJECT:
						return factory.newDbObjectCollectController(tabIfAny);
					case DBSOURCE:
						return factory.newDbSourceCollectController(tabIfAny);
					case ENTITYRELATION:
						return factory.newEntityRelationShipCollectController(Main.getInstance().getMainFrame(), tabIfAny);
					case LDAPSERVER:
						return factory.newLdapServerCollectController(tabIfAny);
					case CODE:
						return factory.newCodeCollectController(tabIfAny);
					}
				}

				// try general masterdata factory method:
				// if there is no MasterDataCollectController for this entity, an exception will be thrown here.
				return newMasterDataCollectController(sEntityName, tabIfAny);
			} else {
				throw new CommonPermissionException(SpringLocaleDelegate.getInstance().getMessage(
						"NuclosCollectControllerFactory.1", "Sie haben kein Recht in der Entit\u00e4t ''{0}'' zu lesen.", 
						SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(
								MasterDataDelegate.getInstance().getMetaData(sEntityName))));
					//"Sie haben kein Recht in der Entit\u00e4t \"" + SpringLocaleDelegate.getLabelFromMetaDataVO(MasterDataDelegate.getInstance().getMetaData(sEntityName)) + "\" zu lesen.");
			}
		}
	}

	/**
	 * @param parent
	 * @param iModuleId may be <code>null</code>
	 * @return a new GenericObjectCollectController for the given module id.
	 * @postcondition result != null
	 */
	public GenericObjectCollectController newGenericObjectCollectController(Integer iModuleId, MainFrameTab tabIfAny) {
		final GenericObjectCollectController result;
		final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
		result = factory.newGenericObjectCollectController(iModuleId, true, tabIfAny);

		assert result != null;
		return result;
	}

	/**
	 * @param parent the parent component of this controller
	 * @param sEntityName the masterdata entity to collect
	 * @return a new MasterDataCollectController for the given entity.
	 * @postcondition result != null
	 */
	public MasterDataCollectController newMasterDataCollectController(String sEntityName, MainFrameTab tabIfAny) {
		final NuclosEntity systemEntity = NuclosEntity.getByName(sEntityName);
		final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
		if (systemEntity != null) {
			switch (systemEntity) {
			case GENERATION:
				return factory.newGenerationCollectController(tabIfAny);
			case LAYOUT:
				return factory.newGenericObjectLayoutCollectController(tabIfAny);
			case GROUP:
				return factory.newGroupCollectController(tabIfAny);
			case RELATIONTYPE:
				return factory.newRelationTypeCollectController(tabIfAny);
			case REPORT:
				return factory.newReportCollectController(systemEntity, tabIfAny);
			case FORM:
				return factory.newReportCollectController(systemEntity, tabIfAny);
			case REPORTEXECUTION:
				return factory.newReportExecutionCollectController(tabIfAny);
			case USER:
				return factory.newUserCollectController(tabIfAny);
			case RESOURCE:
				return factory.newResourceCollectController(tabIfAny);
			case IMPORTEXPORT:
				return factory.newExportImportCollectController(tabIfAny);
			case SEARCHFILTER:
				return factory.newSearchFilterCollectController(tabIfAny);
			case WIKI:
				return factory.newWikiCollectController(tabIfAny);
			case DATATYP:
				return factory.newDataTypeCollectController(tabIfAny);
			case TASKLIST:
				return factory.newPersonalTaskCollectController(tabIfAny);
			}
		}
		return factory.newMasterDataCollectController(sEntityName, tabIfAny);
	}

	/**
	 * @param subform
	 * @param sParentEntityName
	 * @param clctcompmodelprovider
	 * @param tab
	 * @param compDetails
	 * @param prefs
	 * @return a new DetailsSubFormController for use in an NuclosCollectController
	 * @postcondition result != null
	 */
	public MasterDataSubFormController newDetailsSubFormController(SubForm subform,
			String sParentEntityName, CollectableComponentModelProvider clctcompmodelprovider,
			MainFrameTab tab, JComponent compDetails, Preferences prefs,
			EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {

		final MasterDataSubFormController result;

		final String sControllerType = subform.getControllerType();
		if (sControllerType == null || sControllerType.equals("default")) {
			if (MasterDataDelegate.getInstance().getMetaData(subform.getEntityName()).isDynamic()) {
				result = new DynamicEntitySubFormController(tab, clctcompmodelprovider, sParentEntityName, subform, prefs, entityPrefs, valueListProviderCache);
			}
			else {
				result = new MasterDataSubFormController(tab, clctcompmodelprovider, sParentEntityName, subform, prefs, entityPrefs, valueListProviderCache);
			}
		}
		else if (sControllerType.equals("importforeignentityidentifier")) {
			result = new ImportForeignEntityIdentifierSubFormController(tab, clctcompmodelprovider, sParentEntityName, subform, prefs, entityPrefs, valueListProviderCache);
		}
		else if (sControllerType.equals("importattribute")) {
			result = new ImportAttributeSubFormController(tab, clctcompmodelprovider, sParentEntityName, subform, prefs, entityPrefs, valueListProviderCache);
		}
		else {
			throw new NuclosFatalException("Unknown Controllertype for subform:" + sControllerType);//Unbekannter Controllertyp f\u00fcr Unterformular:
		}
		assert result != null;
		return result;
	}

}	// class NuclosCollectControllerFactory

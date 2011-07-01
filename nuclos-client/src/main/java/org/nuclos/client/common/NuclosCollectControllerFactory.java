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
import org.nuclos.client.customcode.CodeCollectController;
import org.nuclos.client.datasource.admin.DatasourceCollectController;
import org.nuclos.client.datasource.admin.DynamicEntityCollectController;
import org.nuclos.client.datasource.admin.RecordGrantCollectController;
import org.nuclos.client.datasource.admin.ValuelistProviderCollectController;
import org.nuclos.client.genericobject.DynamicEntitySubFormController;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectImportCollectController;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.job.JobControlCollectController;
import org.nuclos.client.layout.admin.GenericObjectLayoutCollectController;
import org.nuclos.client.ldap.LdapServerCollectController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.DbObjectCollectController;
import org.nuclos.client.masterdata.DbSourceCollectController;
import org.nuclos.client.masterdata.GenerationCollectController;
import org.nuclos.client.masterdata.GenericObjectImportStructureCollectController;
import org.nuclos.client.masterdata.GroupCollectController;
import org.nuclos.client.masterdata.ImportAttributeSubFormController;
import org.nuclos.client.masterdata.ImportForeignEntityIdentifierSubFormController;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.NucletCollectController;
import org.nuclos.client.masterdata.RelationTypeCollectController;
import org.nuclos.client.masterdata.SearchFilterCollectController;
import org.nuclos.client.masterdata.locale.LocaleCollectController;
import org.nuclos.client.masterdata.user.UserCollectController;
import org.nuclos.client.masterdata.wiki.WikiCollectController;
import org.nuclos.client.processmonitor.InstanceCollectController;
import org.nuclos.client.processmonitor.ProcessMonitorCollectController;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.report.admin.ReportCollectController;
import org.nuclos.client.report.admin.ReportExecutionCollectController;
import org.nuclos.client.resource.admin.ResourceCollectController;
import org.nuclos.client.rule.admin.RuleCollectController;
import org.nuclos.client.rule.admin.TimelimitRuleCollectController;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.statemodel.admin.StateModelCollectController;
import org.nuclos.client.task.PersonalTaskCollectController;
import org.nuclos.client.transfer.ExportImportCollectController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.client.wizard.DataTypeCollectController;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.statemodel.Statemodel;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
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
	public NuclosCollectController<?> newCollectController(JComponent parent, String sEntityName, MainFrameTab tabIfAny) throws NuclosBusinessException, CommonPermissionException, CommonFatalException {
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
			Statemodel sm = StateDelegate.getInstance().getStatemodel(new UsageCriteria(iModuleId, null));
			if(sm == null) {
				JOptionPane.showMessageDialog(
					parent,
					CommonLocaleDelegate.getMessage("NuclosCollectControllerFactory.2", "Es ist kein Statusmodell definiert. Das Modul kann nicht geöffnet werden.\nDie Statusmodellverwaltung finden Sie im Menü Administration."),
					sEntityName,
					JOptionPane.WARNING_MESSAGE);
				return null;
			}
			return newGenericObjectCollectController(parent, iModuleId, tabIfAny);
		} else {
			if(SecurityCache.getInstance().isReadAllowedForMasterData(sEntityName)) {

				MasterDataLayoutHelper.checkLayoutMLExistence(sEntityName);

				NuclosEntity systemEntity = NuclosEntity.getByName(sEntityName);
				if (systemEntity != null) {
					switch (systemEntity) {
					case DATASOURCE:
						return new DatasourceCollectController(parent, tabIfAny);
					case DYNAMICENTITY:
						return new DynamicEntityCollectController(parent, tabIfAny);
					case VALUELISTPROVIDER:
						return new ValuelistProviderCollectController(parent, tabIfAny);
					case RECORDGRANT:
						return new RecordGrantCollectController(parent, tabIfAny);
					case STATEMODEL:
						return new StateModelCollectController(parent, tabIfAny);
					case PROCESSMONITOR:
						return new ProcessMonitorCollectController(parent, tabIfAny);
					case INSTANCE:
						return new InstanceCollectController(parent, sEntityName, tabIfAny);
					case RULE:
						return new RuleCollectController(parent, tabIfAny);
					case IMPORT:
						return new GenericObjectImportStructureCollectController(parent, tabIfAny);
					case IMPORTFILE:
						return new GenericObjectImportCollectController(parent, tabIfAny);
					case TIMELIMITRULE:
						return new TimelimitRuleCollectController(parent, tabIfAny);
					case JOBCONTROLLER:
						return new JobControlCollectController(parent, tabIfAny);
					case LOCALE:
						return new LocaleCollectController(parent, tabIfAny);
					case NUCLET:
						return new NucletCollectController(parent, tabIfAny);
					case DBOBJECT:
						return new DbObjectCollectController(parent, tabIfAny);
					case DBSOURCE:
						return new DbSourceCollectController(parent, tabIfAny);
					case ENTITYRELATION:
						return new EntityRelationShipCollectController(parent, Main.getMainFrame(), tabIfAny);
					case LDAPSERVER:
						return new LdapServerCollectController(parent, tabIfAny);
					case CODE:
						return new CodeCollectController(parent, tabIfAny);
					}
				}

				// try general masterdata factory method:
				// if there is no MasterDataCollectController for this entity, an exception will be thrown here.
				return newMasterDataCollectController(parent, sEntityName, tabIfAny);
			} else {
				throw new CommonPermissionException(CommonLocaleDelegate.getMessage("NuclosCollectControllerFactory.1", "Sie haben kein Recht in der Entit\u00e4t ''{0}'' zu lesen.", CommonLocaleDelegate.getLabelFromMetaDataVO(MasterDataDelegate.getInstance().getMetaData(sEntityName))));
					//"Sie haben kein Recht in der Entit\u00e4t \"" + CommonLocaleDelegate.getLabelFromMetaDataVO(MasterDataDelegate.getInstance().getMetaData(sEntityName)) + "\" zu lesen.");
			}
		}
	}

	/**
	 * @param parent
	 * @param iModuleId may be <code>null</code>
	 * @return a new GenericObjectCollectController for the given module id.
	 * @postcondition result != null
	 */
	public GenericObjectCollectController newGenericObjectCollectController(JComponent parent, Integer iModuleId, MainFrameTab tabIfAny) {
		final GenericObjectCollectController result;

		result = new GenericObjectCollectController(parent, iModuleId, true, tabIfAny);

		assert result != null;
		return result;
	}

	/**
	 * @param parent the parent component of this controller
	 * @param sEntityName the masterdata entity to collect
	 * @return a new MasterDataCollectController for the given entity.
	 * @postcondition result != null
	 */
	public MasterDataCollectController newMasterDataCollectController(JComponent parent, String sEntityName, MainFrameTab tabIfAny) {
		final NuclosEntity systemEntity = NuclosEntity.getByName(sEntityName);
		if (systemEntity != null) {
			switch (systemEntity) {
			case GENERATION:
				return new GenerationCollectController(parent, tabIfAny);
			case LAYOUT:
				return new GenericObjectLayoutCollectController(parent, tabIfAny);
			case GROUP:
				return new GroupCollectController(parent, tabIfAny);
			case RELATIONTYPE:
				return new RelationTypeCollectController(parent, tabIfAny);
			case REPORT:
				return new ReportCollectController(parent, tabIfAny);
			case REPORTEXECUTION:
				return new ReportExecutionCollectController(parent, tabIfAny);
			case USER:
				return new UserCollectController(parent, tabIfAny);
			case RESOURCE:
				return new ResourceCollectController(parent, tabIfAny);
			case IMPORTEXPORT:
				return new ExportImportCollectController(parent, tabIfAny);
			case SEARCHFILTER:
				return new SearchFilterCollectController(parent, tabIfAny);
			case WIKI:
				return new WikiCollectController(parent, tabIfAny);
			case DATATYP:
				return new DataTypeCollectController(parent, tabIfAny);
			case TASKLIST:
				return new PersonalTaskCollectController(parent, tabIfAny);
			}
		}
		return new MasterDataCollectController(parent, sEntityName, tabIfAny);
	}

	/**
	 * @param subform
	 * @param sParentEntityName
	 * @param clctcompmodelprovider
	 * @param ifrmParent
	 * @param parent
	 * @param compDetails
	 * @param prefs
	 * @return a new DetailsSubFormController for use in an NuclosCollectController
	 * @postcondition result != null
	 */
	public MasterDataSubFormController newDetailsSubFormController(SubForm subform,
			String sParentEntityName, CollectableComponentModelProvider clctcompmodelprovider,
			MainFrameTab ifrmParent, JComponent parent, JComponent compDetails, Preferences prefs,
			CollectableFieldsProviderCache valueListProviderCache) {

		final MasterDataSubFormController result;

		final String sControllerType = subform.getControllerType();
		if (sControllerType == null || sControllerType.equals("default")) {
			if (MasterDataDelegate.getInstance().getMetaData(subform.getEntityName()).isDynamic()) {
				result = new DynamicEntitySubFormController(ifrmParent, parent, clctcompmodelprovider, sParentEntityName, subform, prefs, valueListProviderCache);
			}
			else {
				result = new MasterDataSubFormController(ifrmParent, parent, clctcompmodelprovider, sParentEntityName, subform, prefs, valueListProviderCache);
			}
		}
		else if (sControllerType.equals("importforeignentityidentifier")) {
			result = new ImportForeignEntityIdentifierSubFormController(parent, ifrmParent, clctcompmodelprovider, sParentEntityName, subform, prefs, valueListProviderCache);
		}
		else if (sControllerType.equals("importattribute")) {
			result = new ImportAttributeSubFormController(parent, ifrmParent, clctcompmodelprovider, sParentEntityName, subform, prefs, valueListProviderCache);
		}
		else {
			throw new NuclosFatalException("Unknown Controllertype for subform:" + sControllerType);//Unbekannter Controllertyp f\u00fcr Unterformular:
		}
		assert result != null;
		return result;
	}

}	// class NuclosCollectControllerFactory

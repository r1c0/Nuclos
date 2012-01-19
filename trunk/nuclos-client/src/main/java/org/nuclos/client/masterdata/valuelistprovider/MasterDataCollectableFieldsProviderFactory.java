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
package org.nuclos.client.masterdata.valuelistprovider;

import java.lang.reflect.Constructor;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.DatasourceBasedCollectableFieldsProvider;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.valuelistprovider.AttributeCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.GenericObjectCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.ModuleSubEntityCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.ProcessCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.StatusNumeralCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.generation.GenerationAttributeCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.generation.GenerationSourceTypeCollectableFieldsProvider;
import org.nuclos.client.genericobject.valuelistprovider.generation.GenerationSubEntityCollectableFieldsProvider;
import org.nuclos.client.job.valuelistprovider.JobDBObjectCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.AttributeGroupFunctionCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.DBObjectCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.DBObjectTypeCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.DBTypeCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.EntityCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.EntityCollectableIdFieldsProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * <code>CollectableFieldsProviderFactory</code> for masterdata.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class MasterDataCollectableFieldsProviderFactory implements CollectableFieldsProviderFactory {
	private final CollectableEntity clcte;

	public MasterDataCollectableFieldsProviderFactory(String sEntityName) {
		this.clcte = (sEntityName == null) ? null : DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
	}

	@Override
    public CollectableFieldsProvider newDefaultCollectableFieldsProvider(String sEntityName, String sFieldName) {
		if (sEntityName == null) {
			sEntityName = this.clcte.getName();
		}
		return newMasterDataCollectableFieldsProvider(sEntityName, sFieldName);
	}

	@Override
    public CollectableFieldsProvider newDependantCollectableFieldsProvider(String sEntityName, String sFieldName) {
		final CollectableEntity clcte = (sEntityName == null) ? this.clcte : DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
		final CollectableEntityField clctef = clcte.getEntityField(sFieldName);

		if (!clctef.isReferencing()) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectableFieldsProviderFactory.1",
				"Das Feld {0} in der Entit\u00e4t {1} referenziert keine andere Entit\u00e4t.", clctef.getName(), clcte.getName()));
		}
		return new DependantMasterDataCollectableFieldsProvider(clcte.getName(), sFieldName);
	}

	@Override
    public CollectableFieldsProvider newCustomCollectableFieldsProvider(String sCustomType, String sEntity, String sFieldName) {
		final CollectableFieldsProvider result;
		if (sCustomType.equals("default")) {
			result = newDefaultCollectableFieldsProvider(sEntity, sFieldName);
		}
		else if (sCustomType.equals("entity")) {
			result = new EntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("attributeGroupFunction")) {
			result = new AttributeGroupFunctionCollectableFieldsProvider();
		}
		else if (sCustomType.equals("entityId")) {
			result = new EntityCollectableIdFieldsProvider();
		}
		else if (sCustomType.equals("process")) {
			result = new ProcessCollectableFieldsProvider();
		}
		else if (sCustomType.equals("attribute")) {
			result = new AttributeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("generationSourceType")) {
			result = new GenerationSourceTypeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("generationAttribute")) {
			result = new GenerationAttributeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("generationSubEntity")) {
			result = new GenerationSubEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("modulesubentity")) {
			result = new ModuleSubEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("allreports")) {
			result = new AllReportsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("masterDataEntity")) {
			result = new MasterDataEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("masterDataEntityFields")) {
			result = new MasterDataEntityFieldsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("parameters")) {
			result = new ParametersCollectableFieldsProvider();
		}
		else if (sCustomType.equals("selectiveattribute")) {
			result = new SelectiveAttributeCollectableFieldProvider();
		}
		else if (sCustomType.equals("modulesubformfield")) {
			result = new AllCollectableEntityFieldsCollectableFieldsProvider();
		}
		else if(sCustomType.equals("datasource") || sCustomType.endsWith(ValuelistProviderVO.SUFFIX)) {
			result = new DatasourceBasedCollectableFieldsProvider(sCustomType.endsWith(ValuelistProviderVO.SUFFIX));
		}
		else if (sCustomType.equals("subform")) {
			result = new SubformEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("masterdatasubforms")) {
			result = new MasterDataSubformEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("foreignentityfields")) {
			result = new ForeignEntityFieldsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("dbobjects")) {
			result = new JobDBObjectCollectableFieldsProvider();
		}
		else if (sCustomType.equals("entityfields")) {
			result = new EntityFieldsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("fieldOrAttribute")) {
			result = new FieldOrAttributeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("dependants")) {
			result = new DependantsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("statusNumeral")) {
			result = new StatusNumeralCollectableFieldsProvider();
		}
		else if (sCustomType.equals("importentities")) {
			result = new ImportEntityCollectableFieldsProvider();
		}
		else if (sCustomType.equals("importfields")) {
			result = new ImportEntityFieldsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("referencedEntityField")) {
			result = new ReferencedEntityFieldCollectableFieldsProvider();
		}
		else if (sCustomType.equals("importStructures")) {
			result = new ImportStructureCollectableFieldsProvider();
		}
		else if (sCustomType.equals("subformfields")) {
			result = new SubFormFieldsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("actions")) {
			result = new RoleActionsCollectableFieldsProvider();
		}
		else if (sCustomType.equals("generic")) {
			result = new GenericCollectableFieldsProvider();
		}
		else if (sCustomType.equals("enum")) {
			result = new EnumCollectableFieldsProvider();
		}
		else if (sCustomType.equals("dbtype")) {
			result = new DBTypeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("dbobjecttype")) {
			result = new DBObjectTypeCollectableFieldsProvider();
		}
		else if (sCustomType.equals("dbobject")) {
			result = new DBObjectCollectableFieldsProvider();
		}
		else if (sCustomType.equals("rolehierarchyusers")) {
			result = new RoleHierarchyUsersCollectableFieldsProvider();
		}
		else if (sCustomType.equals("readableusers")) {
			result = new UserCollectableFieldsProvider();
		}
		else if (sCustomType.equals("locales")) {
			result = new LocaleCollectableFieldsProvider();
		}
		else if (sCustomType.equals("assignableworkspaces")) {
			result = new AssignableWorkspaceCollectableFieldsProvider();
		}
		else {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectableFieldsProviderFactory.2",
				"Unbekannter valuelist-provider Typ: {0}", sCustomType));
		}
		return result;
	}

	/**
	 * helper method (also used in GenericObjectCollectableFieldsProviderFactory)
	 * @param sEntityName
	 * @param sFieldName
	 * @return
	 * @precondition sEntityName != null
	 */
	public static CollectableFieldsProvider newMasterDataCollectableFieldsProvider(String sEntityName, String sFieldName) {
		if (sEntityName == null) {
			throw new NullArgumentException("sEntityName");
		}
		final CollectableEntity clcte = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
		final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
		if (!clctef.isReferencing()) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectableFieldsProviderFactory.1",
				"Das Feld {0} in der Entit\u00e4t {1} referenziert keine andere Entit\u00e4t.", sFieldName, sEntityName));
		}

		if (Modules.getInstance().isModuleEntity(clctef.getReferencedEntityName())) {
			return new GenericObjectCollectableFieldsProvider(clctef);
		} else {
			MasterDataCollectableFieldsProvider mdcfp = new MasterDataCollectableFieldsProvider(clctef.getReferencedEntityName());
			mdcfp.setParameter("fieldName", clctef.getReferencedEntityFieldName());
			return mdcfp;
		}
	}

	public static CollectableFieldsProviderFactory newFactory(String sEntityName) {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getMasterDataCollectableFieldsProviderFactoryClassName(),
					MasterDataCollectableFieldsProviderFactory.class.getName());

			final Class<? extends MasterDataCollectableFieldsProviderFactory> clsmdcfpf = Class.forName(sClassName).asSubclass(MasterDataCollectableFieldsProviderFactory.class);
			final Constructor<? extends MasterDataCollectableFieldsProviderFactory> ctor = clsmdcfpf.getConstructor(String.class);

			return ctor.newInstance(sEntityName);
		}
		catch (Exception ex) {
			throw new CommonFatalException("MasterDataCollectableFieldsProviderFactory cannot be created.", ex);
		}
	}

	public static CollectableFieldsProviderFactory newFactory(String entityName, CollectableFieldsProviderCache cache) {
		CollectableFieldsProviderFactory factory = newFactory(entityName);
		return (cache != null) ? cache.makeCachingFieldsProviderFactory(factory) : factory;
	}
}	// class MasterDataCollectableFieldsProviderFactory

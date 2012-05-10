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
package org.nuclos.client.genericobject.valuelistprovider;

import java.lang.reflect.Constructor;

import org.apache.commons.lang.NotImplementedException;
import org.nuclos.client.common.DatasourceBasedCollectableFieldsProvider;
import org.nuclos.client.masterdata.valuelistprovider.DependantMasterDataCollectableFieldsProvider;
import org.nuclos.client.masterdata.valuelistprovider.GenericCollectableFieldsProvider;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.masterdata.valuelistprovider.ParametersCollectableFieldsProvider;
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
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * Factory that creates <code>CollectableFieldProvider</code>s for <code>CollectableGenericObject</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectCollectableFieldsProviderFactory implements CollectableFieldsProviderFactory {

	private final CollectableEntity clcte;

	public GenericObjectCollectableFieldsProviderFactory(String sEntityName) {
		this.clcte = (sEntityName == null) ? null : DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
	}

	@Override
	public CollectableFieldsProvider newDefaultCollectableFieldsProvider(String sEntityName, String sFieldName) {
		final CollectableFieldsProvider result;
		if (sEntityName == null || sEntityName.equals(clcte.getName())) {
			result = new GenericObjectCollectableFieldsProvider(clcte.getEntityField(sFieldName));
		}
		else {
			result = MasterDataCollectableFieldsProviderFactory.newMasterDataCollectableFieldsProvider(sEntityName, sFieldName);
		}
		return result;
	}

	@Override
	public CollectableFieldsProvider newDependantCollectableFieldsProvider(String sEntityName, String sFieldName) {
		final CollectableFieldsProvider result;
		if (sEntityName == null || sEntityName.equals(clcte.getName())) {
			final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
			if (!clctef.isReferencing()) {
				throw new NuclosFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectCollectableFieldsProviderFactory.1",
						"Das Feld {0} in der Entit\u00e4t {1} referenziert keine andere Entit\u00e4t.", 
						clctef.getName(), clcte.getName()));
			}
			result = new DependantMasterDataCollectableFieldsProvider(clcte.getName(), sFieldName);
		}
		else {
			/** @todo implement */
			throw new NotImplementedException("subentity");
		}
		return result;
	}

	@Override
	public CollectableFieldsProvider newCustomCollectableFieldsProvider(String sCustomType, String sEntityName, String sFieldName) {
		final CollectableFieldsProvider result;
		if (sCustomType.equals("entity")) {
			result = new EntityCollectableFieldsProvider();
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
		else if(sCustomType.equals("datasource") || sCustomType.endsWith(ValuelistProviderVO.SUFFIX)) {
				result = new DatasourceBasedCollectableFieldsProvider(sCustomType.endsWith(ValuelistProviderVO.SUFFIX));
		}
		else if (sCustomType.equals("parameters")) {
			result = new ParametersCollectableFieldsProvider();
		}
		else if (sCustomType.equals("status")) {
			result = new StatusCollectableFieldsProvider();
		}
		else if (sCustomType.equals("generic")) {
			result = new GenericCollectableFieldsProvider();
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
		else {
			throw new NuclosFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"GenericObjectCollectableFieldsProviderFactory.2", "Unbekannter valuelist-provider Typ: {0}", sCustomType));
		}
		return result;
	}

	public static CollectableFieldsProviderFactory newFactory(String sEntityName) {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getGenericObjectCollectableFieldsProviderFactoryClassName(),
					GenericObjectCollectableFieldsProviderFactory.class.getName());

			final Class<? extends GenericObjectCollectableFieldsProviderFactory> clsgocfpf = Class.forName(sClassName).asSubclass(GenericObjectCollectableFieldsProviderFactory.class);
			final Constructor<? extends GenericObjectCollectableFieldsProviderFactory> ctor = clsgocfpf.getConstructor(String.class);

			return ctor.newInstance(sEntityName);
		}
		catch (Exception ex) {
			throw new CommonFatalException("GenericObjectCollectableFieldsProviderFactory cannot be created.", ex);
		}
	}
	
	public static CollectableFieldsProviderFactory newFactory(String entityName, CollectableFieldsProviderCache cache) {
		CollectableFieldsProviderFactory factory = newFactory(entityName);
		return (cache != null) ? cache.makeCachingFieldsProviderFactory(factory) : factory;
	}
}	// class GenericObjectCollectableFieldsProviderFactory

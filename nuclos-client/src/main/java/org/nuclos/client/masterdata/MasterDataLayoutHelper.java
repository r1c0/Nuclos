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
package org.nuclos.client.masterdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.Set;

import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.xml.sax.InputSource;

/**
 * Helper class for master data layouts.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataLayoutHelper {

	private static Set<NuclosEntity> simpleLayoutEntities = EnumSet.of(
		NuclosEntity.CHART,		
		NuclosEntity.DATASOURCE,
		NuclosEntity.DYNAMICENTITY,
		NuclosEntity.VALUELISTPROVIDER,
		NuclosEntity.RECORDGRANT,
		NuclosEntity.DYNAMICTASKLIST,
		NuclosEntity.STATEMODEL,
		NuclosEntity.RULE,
		NuclosEntity.TIMELIMITRULE,
		NuclosEntity.JOBCONTROLLER,
		NuclosEntity.PROCESSMONITOR,
		NuclosEntity.INSTANCE,
		NuclosEntity.ENTITYRELATION);

	private MasterDataLayoutHelper() {
	}

	public static Reader getLayoutMLReader(String sEntityName, boolean bSearch) throws NuclosBusinessException {
		checkLayoutMLExistence(sEntityName);
		return new StringReader(MasterDataDelegate.getInstance().getLayoutML(sEntityName, bSearch));
	}

	public static LayoutRoot newLayoutRoot(String sEntityName, boolean bSearch) throws NuclosBusinessException {
		final LayoutMLParser parser = new LayoutMLParser();

		final CollectableMasterDataEntity clcte = new CollectableMasterDataEntity(
				MetaDataCache.getInstance().getMetaData(sEntityName));
		LayoutRoot result;

		final Reader reader = MasterDataLayoutHelper.getLayoutMLReader(sEntityName, bSearch);

		final InputSource isrc = new InputSource(new BufferedReader(reader));

		try {
			result = parser.getResult(isrc, clcte, bSearch, null, MasterDataCollectableFieldsProviderFactory.newFactory(sEntityName, new CollectableFieldsProviderCache()), CollectableComponentFactory.getInstance());
		}
		catch (LayoutMLException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}

		return result;
	}

	public static boolean isLayoutMLAvailable(String sEntityName, boolean bSearch) {
		return MasterDataDelegate.getInstance().getLayoutML(sEntityName, bSearch) != null;
	}

	public static boolean hasSpecialLayout(String sEntityName) {
		return simpleLayoutEntities.contains(NuclosEntity.getByName(sEntityName));
	}

	public static void checkLayoutMLExistence(String sEntityName) throws NuclosBusinessException {
		boolean searchable = MasterDataDelegate.getInstance().getMetaData(sEntityName).isSearchable();
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		if (!isLayoutMLAvailable(sEntityName,searchable) && !hasSpecialLayout(sEntityName)) {
			String form = searchable ? localeDelegate.getResource("R00011933", "Suchmaske") 
					: localeDelegate.getResource("R00022868", "Eingabemaske");
			String entityName = localeDelegate.getLabelFromMetaDataVO(
					MasterDataDelegate.getInstance().getMetaData(sEntityName));
			throw new NuclosBusinessException(localeDelegate.getMessage("masterdata.error.layout.missing",
				"Das Layout f\u00fcr die {0} der Entit\u00e4t '{1}' fehlt", form, entityName));
		}
	}

}	// class MasterDataLayoutHelper

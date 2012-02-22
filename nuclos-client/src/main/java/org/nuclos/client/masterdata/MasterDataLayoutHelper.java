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

import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.Set;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;

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
		NuclosEntity.DATASOURCE,		
		NuclosEntity.DATASOURCE,
		NuclosEntity.DYNAMICENTITY,
		NuclosEntity.VALUELISTPROVIDER,
		NuclosEntity.RECORDGRANT,
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

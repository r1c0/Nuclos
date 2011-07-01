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

import java.util.Collections;
import java.util.List;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.common.Utils;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;

/**
 * <code>ValueListProvider</code> for "all reports". This is used in the role administration dialog where all reports and forms
 * must be displayed, regardless of user rights.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class AllReportsCollectableFieldsProvider implements CollectableFieldsProvider {

	private String sEntityName;

	@Override
	public void setParameter(String sName, Object oValue) {
		if ("entity".equals(sName)) {
			this.sEntityName = (String) oValue;
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		if (sEntityName == null) {
			throw new IllegalStateException("sEntityName");
		}

		final List<CollectableField> result = Utils.getCollectableFieldsByName(sEntityName, MasterDataDelegate.getInstance().getAllReports(), CollectableMasterData.FIELDNAME_NAME, true);
		Collections.sort(result);
		return result;
	}

}	// class AllReportsCollectableFieldsProvider

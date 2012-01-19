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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.valuelistprovider.cache.CacheableCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.cache.ManagedCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A <code>CollectableFieldsProvider</code> for master data entities in Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MasterDataCollectableFieldsProvider extends ManagedCollectableFieldsProvider implements CacheableCollectableFieldsProvider {

	private final String sEntityName;
	private boolean bSearchMode = false;
	private String sFieldName;

	/**
	 * @precondition sEntityName != null
	 * @param sEntityName
	 */
	public MasterDataCollectableFieldsProvider(String sEntityName) {
		if (sEntityName == null) {
			throw new NullArgumentException("sEntityName");
		}
		this.sEntityName = sEntityName;
	}

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"_searchmode" = collectable in search mask?</li>
	 *   <li>"fieldName" = referenced field name. Default is "name".</li>
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("_searchmode")) {
			bSearchMode = (Boolean) oValue;
		}
		else if (sName.equals("fieldName")) {
			this.sFieldName = (String) oValue;
		}
		else if (sName.equals("ignoreValidity")) {
			this.setIgnoreValidity(Boolean.valueOf(oValue.toString()));
		}
		else {
			// ignore
		}
	}

	@Override
	public Object getCacheKey() {
		return Arrays.<Object>asList(
			sEntityName,
			sFieldName,
			bSearchMode,
			this.getIgnoreValidity());
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		return (sFieldName == null) ?
				getCollectableFields(this.sEntityName, !this.getIgnoreValidity() && !bSearchMode) :
				getCollectableFieldsByName(this.sEntityName, sFieldName, !this.getIgnoreValidity() && !bSearchMode);
	}

	public List<CollectableField> getCollectableFields(boolean bValid) throws CommonBusinessException {
		return getCollectableFields(this.sEntityName, !this.getIgnoreValidity() && bValid && !bSearchMode);
	}

	public List<CollectableField> getCollectableFieldsByName(String sFieldName, boolean bValid) throws CommonBusinessException {
		return getCollectableFieldsByName(this.sEntityName, sFieldName, !this.getIgnoreValidity() && bValid && !bSearchMode);
	}

	private static synchronized List<CollectableField> getCollectableFields(String sEntityName, boolean bValid) throws CommonBusinessException {
		List<CollectableField> result = MasterDataCache.getInstance().getCollectableFields(sEntityName, bValid);
		Collections.sort(result);

		return result;
	}

	private static synchronized List<CollectableField> getCollectableFieldsByName(String sEntityName, String sFieldName, boolean bValid) throws CommonBusinessException {
		List<CollectableField> result =  MasterDataCache.getInstance().getCollectableFieldsByName(sEntityName, sFieldName, bValid);
		Collections.sort(result);

		return result;
	}

}	// class MasterDataCollectableFieldsProvider

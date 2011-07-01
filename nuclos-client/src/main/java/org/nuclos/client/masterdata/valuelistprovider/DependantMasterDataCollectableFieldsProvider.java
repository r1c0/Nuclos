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

import org.nuclos.client.common.Utils;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.valuelistprovider.cache.CacheableCollectableFieldsProvider;
import org.nuclos.client.valuelistprovider.cache.ManagedCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * <code>CollectableFieldsProvider</code> for dependant masterdata in Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class DependantMasterDataCollectableFieldsProvider extends ManagedCollectableFieldsProvider implements CacheableCollectableFieldsProvider {

	private final String sEntityName;
	private final boolean checkValidity;
	private final String sForeignKeyFieldName;
	private String sFieldToDisplay;
	private final String sForeignEntityName;
	private Object oRelatedId;
	private boolean bSearchMode = false;

	public DependantMasterDataCollectableFieldsProvider(String sEntityName, String sForeignKeyFieldName) {
		this.sEntityName = sEntityName;
		this.sForeignKeyFieldName = sForeignKeyFieldName;
		final CollectableEntityProvider clcteprovider = DefaultCollectableEntityProvider.getInstance();
		this.sForeignEntityName = clcteprovider.getCollectableEntity(sEntityName).getEntityField(sForeignKeyFieldName).getReferencedEntityName();
		this.sFieldToDisplay = clcteprovider.getCollectableEntity(sForeignEntityName).getIdentifierFieldName();
		this.checkValidity = Utils.hasValidOrActiveField(sEntityName);
	}

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"relatedId" = related id</li>
	 *   <li>"fieldToDisplay" = name of the field to display</li>
	 *   <li>"_searchmode" = collectable in search mask?</li>
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("relatedId")) {
			this.oRelatedId = oValue;
		}
		else if (sName.equals("fieldToDisplay")) {
			this.sFieldToDisplay = (String) oValue;
		}
		else if (sName.equals("_searchmode")) {
			bSearchMode = (Boolean) oValue;
		}
		else {
			// ignored
		}
	}
	
	private boolean shouldCheckValidity() {
		// validity check only in detail mode (and only if the entity supports these fields)
		return !this.getIgnoreValidity() && !bSearchMode && checkValidity;
	}

	@Override
	public Object getCacheKey() {
		return Arrays.<Object>asList(
			oRelatedId,
			sEntityName,
			sForeignKeyFieldName,
			sForeignEntityName,
			sFieldToDisplay,
			shouldCheckValidity());
	}
	
	@Override
	public List<CollectableField> getCollectableFields() {
		if (this.sFieldToDisplay == null) {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("DependantMasterDataCollectableFieldsProvider.1",
				"Die Entit\u00e4t {0} hat kein identifizierendes Feld.", sForeignEntityName));
		}

		if (this.oRelatedId == null) {
			return Collections.<CollectableField>emptyList();
		} else {
			final List<CollectableField> result = EntityObjectDelegate.getInstance().getCollectableFieldsByName(sEntityName, sForeignKeyFieldName, checkValidity);
			Collections.sort(result);
			return result;
		}
	}

}	// class DependantMasterDataCollectableFieldsProvider

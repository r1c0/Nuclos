//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.Utils;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * inner class <code>CompleteGenericObjectsStrategy</code>
 */
public class CompleteGenericObjectsStrategy implements
		CompleteCollectablesStrategy<CollectableGenericObjectWithDependants> {

	@Override
	public boolean isComplete(CollectableGenericObjectWithDependants clct) {
		/** @todo add "contains all required dependants */
		return clct.isComplete();
	}

	@Override
	public boolean getCollectablesInResultAreAlwaysComplete() {
		return false;
	}

	/**
	 * reads a bunch of <code>CollectableGenericObjectWithDependants</code> from the database.
	 * @param collclctlo Collection<Collectable>
	 * @return Collection<Collectable> contains the read <code>CollectableGenericObjectWithDependants</code>.
	 * @throws CommonBusinessException
	 * @precondition collclctlo != null
	 * @postcondition result != null
	 * @postcondition result.size() == collclct.size()
	 */
	@Override
	public Collection<CollectableGenericObjectWithDependants> getCompleteCollectables(
			Collection<CollectableGenericObjectWithDependants> collclctlo) throws CommonBusinessException {
		if (collclctlo == null)
			throw new NullArgumentException("collclctlo");
		final Collection<CollectableGenericObjectWithDependants> result = new ArrayList<CollectableGenericObjectWithDependants>();

		final Collection<CollectableGenericObject> collclctIncomplete = new ArrayList<CollectableGenericObject>();
		CollectionUtils.split(collclctlo, new Collectable.IsComplete(), result, collclctIncomplete);

		if (!collclctIncomplete.isEmpty()) {
			final Collection<Object> collIds = CollectionUtils.transform(collclctIncomplete, new Collectable.GetId());
			final CollectableSearchCondition cond = SearchConditionUtils.getCollectableSearchConditionForIds(collIds);

			final Integer iCommonModuleId = getCommonModuleId(collclctlo);

			final Set<String> stRequiredSubEntityNames = (iCommonModuleId == null) ? Collections.<String> emptySet()
					: GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(iCommonModuleId);

			final List<GenericObjectWithDependantsVO> lstlowdcvo = GenericObjectDelegate.getInstance()
					.getCompleteGenericObjectsWithDependants(iCommonModuleId, cond, stRequiredSubEntityNames);
			result.addAll(CollectionUtils.transform(lstlowdcvo,
					new CollectableGenericObjectWithDependants.MakeCollectable()));
		}

		assert result != null;
		assert result.size() == collclctlo.size();
		return result;
	}

	/**
	 * @param collclctlo Collection<CollectableGenericObject>
	 * @return the common module id, if any, of the given leased objects.
	 */
	private static Integer getCommonModuleId(Collection<? extends CollectableGenericObject> collclctlo) {
		return Utils.getCommonObject(CollectionUtils.transform(collclctlo,
				new Transformer<CollectableGenericObject, Integer>() {
					@Override
					public Integer transform(CollectableGenericObject clctlo) {
						return clctlo.getGenericObjectCVO().getModuleId();
					}
				}));
	}

	/**
	 * @return the identifier, status, status numeral and all quintuple fields.
	 */
	@Override
	public Set<String> getRequiredFieldNamesForResult() {
		final Set<String> result = new HashSet<String>();
		result.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField());
		result.add(NuclosEOField.STATE.getMetaData().getField());
		result.add(NuclosEOField.STATENUMBER.getMetaData().getField());
		result.add(NuclosEOField.STATEICON.getMetaData().getField());
		result.addAll(GenericObjectCollectController.getUsageCriteriaFieldNames());
		return result;
	}

} // inner class CompleteGenericObjectsStrategy

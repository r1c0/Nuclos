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
package org.nuclos.common.collect.collectable;

import java.util.List;
import java.util.Set;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A decorator that merges duplicate values provided by a given CollectableFieldsProvider.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class MergingValuesCollectableFieldsProviderDecorator implements CollectableFieldsProvider {

	private final CollectableFieldsProvider delegate;

	public MergingValuesCollectableFieldsProviderDecorator(CollectableFieldsProvider decorated) {
		this.delegate = decorated;
	}

	@Override
	public void setParameter(String sName, Object oValue) {
		this.delegate.setParameter(sName, oValue);
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		return decorated(this.delegate.getCollectableFields());
	}

	private static List<CollectableField> decorated(List<CollectableField> lst) {
		final Set<Object> stValues = CollectionUtils.transformIntoSet(lst, new CollectableField.GetValue());
		return CollectionUtils.transform(stValues, new MakeValueIdField());
	}

	private static class MakeValueIdField implements Transformer<Object, CollectableField> {
		@Override
		public CollectableField transform(Object o) {
			return new CollectableValueIdField(null, o);
		}
	}

}	// class MergingValuesCollectableFieldsProviderDecorator

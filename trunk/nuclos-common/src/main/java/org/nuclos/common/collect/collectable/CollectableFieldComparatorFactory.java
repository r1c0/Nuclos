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

import java.util.Comparator;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.ApplicationProperties;

/**
 * Factory that creates a <code>Comparator</code> for CollectableFields.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Martin.Weber@novabit.de">Martin.Weber</a>
 * @version 01.00.00
 */
public class CollectableFieldComparatorFactory <Clct extends CollectableField> {

	private static CollectableFieldComparatorFactory<?> singleton;

	protected CollectableFieldComparatorFactory() {
	}

	public static synchronized <T extends CollectableField> CollectableFieldComparatorFactory<?> getInstance() {
		if (singleton == null) {
			try {
				final String sClassName = LangUtils.defaultIfNull(
						ApplicationProperties.getInstance().getCollectableFieldComparatorFactoryClassName(),
						CollectableFieldComparatorFactory.class.getName());

				singleton = (CollectableFieldComparatorFactory<?>) Class.forName(sClassName).newInstance();
			}
			catch (Exception ex) {
				throw new CommonFatalException("Cannot create CollectableFieldComparatorFactory.", ex);
			}
		}
		return singleton;
	}

	/**
	 * @param sEntityName
	 * @param clctEntityField
	 * @return comparator for given entity and entityfield
	 * @precondition clctEntityField != null
	 */
	public <T extends CollectableField> Comparator<T> newCollectableFieldComparator(CollectableEntityField clctEntityField) {
		return (Comparator<T>)CollectableComparator.getFieldComparator(clctEntityField);
	}
}

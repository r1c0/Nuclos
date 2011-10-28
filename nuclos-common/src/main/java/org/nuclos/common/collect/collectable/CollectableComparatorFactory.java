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

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.ApplicationProperties;

/**
 * Factory that creates a <code>Comparator</code> for Collectables.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Martin.Weber@novabit.de">Martin.Weber</a>
 * @version 01.00.00
 */
public class CollectableComparatorFactory <Clct extends Collectable> {

	private static CollectableComparatorFactory<?> singleton;

	protected CollectableComparatorFactory() {
	}

	public static synchronized CollectableComparatorFactory<?> getInstance() {
		if (singleton == null) {
			try {
				final String sClassName = LangUtils.defaultIfNull(
						ApplicationProperties.getInstance().getCollectableComparatorFactoryClassName(),
						CollectableComparatorFactory.class.getName());

				singleton =  (CollectableComparatorFactory<?>) Class.forName(sClassName).newInstance();
			}
			catch (Exception ex) {
				throw new CommonFatalException("Cannot create CollectableComparatorFactory.", ex);
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
	public Comparator<Clct> newCollectableComparator(String sEntityName, CollectableEntityField clctEntityField) {
		if (clctEntityField == null) {
			throw new NullArgumentException("clctEntityField");
		}
		
		return new CollectableComparator<Clct>(clctEntityField);
	}


	/**
	 * @return comparator that sorts by id field
	 * @precondition clctEntityField != null
	 */
	public Comparator<Clct> newCollectableIdComparator() {		
		return new Comparator<Clct>() {

			@Override
			public int compare(Clct clct1, Clct clct2) {
				int result = 0;

				Object oId1 = clct1.getId();
				Object oId2 = clct2.getId();
				if(oId1 instanceof Comparable<?> && oId2 instanceof Comparable<?>) {
					Comparable<Object> comp1 = (Comparable<Object>) oId1;
					Comparable<Object> comp2 = (Comparable<Object>) oId2;
					result = comp1 == null ? 1 : comp2 == null ? -1 : comp1.compareTo(oId2);
				}
				return result;
			}
		};
	}
}

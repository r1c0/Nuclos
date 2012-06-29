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

import java.text.Collator;
import java.util.Comparator;

import org.nuclos.common2.LangUtils;

/**
 * A <code>Comparator</code> for <code>CollectableFields</code> containing a <code>String</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectableStringFieldComparator implements Comparator<CollectableField> {
	/**
	 * use default ("modern" German, DIN 5007) collation
	 */
	private final Collator collator = LangUtils.getDefaultCollator();
	{
		
	}

	@Override
	public int compare(CollectableField clctf1, CollectableField clctf2) {
		Object value1 = getValue(clctf1);
		Object value2 = getValue(clctf2);
		return LangUtils.compare(value1, value2, this.collator);
	}

	private static Object getValue(CollectableField clctf) {
		return (clctf instanceof LocalizedCollectableValueField) ? ((LocalizedCollectableValueField) clctf).toLocalizedString() : clctf.getValue();
	}
	
}  // class CollectableStringFieldComparator

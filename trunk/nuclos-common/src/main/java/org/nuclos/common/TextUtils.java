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
package org.nuclos.common;

import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * Utility methods for the <code>java.text</code> package.
 * The NumberFormat returned by <code>NumberFormat.getInstance()</code> isn't as useful as it should be. It doesn't parse
 * the given String completely and has issues with grouping.
 * A workaround is already provided in <code>CollectableFieldFormat.getInstance(Double.class)</code>. On the other
 * hand, the code used in CollectableFieldFormat could be placed in this more general class.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @deprecated PENDING: It's not clear yet what will happen to this class.
 * @see CollectableFieldFormat
 */
@Deprecated
public class TextUtils {

	private TextUtils() {
	}

	/**
	 * tries to parse the given value completely.
	 * @param sValue
	 * @return sValue as a number, if it could be parsed completely. <code>null</code> otherwise.
	 * @deprecated See above.
	 */
	@Deprecated
	public static Double parseCompleteDouble(String sValue) {
		Double result;
		try {
			result = (Double) CollectableFieldFormat.getInstance(Double.class).parse(null, sValue);
		}
		catch (CollectableFieldFormatException ex) {
			result = null;
		}
		return result;
	}

}	// class TextUtils

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
package org.nuclos.common2;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;

/**
 * Utility functions for formatting the output of entity fields.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class FormatOutputUtils {
	
	private FormatOutputUtils() {
	}
	
	public static String formatOutput(String formatOutput, Object value) {
		final String result;
		if (value == null) {
			result = "";
		}
		else if (value instanceof Number) { 
			if (formatOutput != null && !formatOutput.equals("")) {
				final DecimalFormat df =   new DecimalFormat(formatOutput);
				result = df.format(value);
			}
			else {
				result = value.toString();
			}
		}
		else {
			result = value.toString();
		}
		return result;
	}
	
	public static String formatOutput(String formatOutput, Collection<Object> values, CharSequence separator) {
		final StringBuilder result = new StringBuilder();
		if (values != null) {
			for (Iterator<?> it = values.iterator(); it.hasNext(); ) {
				final Object o = it.next();
				result.append(formatOutput(formatOutput, o));
				if (it.hasNext()) {
					result.append(separator);
				}
			}
		}
		return result.toString();
	}
	
	public static CollectableField format(CollectableEntityField meta, EntityObjectVO value) {
		return new CollectableValueField(formatOutput(meta.getFormatOutput(), value.getField(meta.getName())));
	}

	public static CollectableField format(CollectableEntityField meta, Collection<EntityObjectVO> values, CharSequence separator) {
		final String format = meta.getFormatOutput();
		final String fieldName = meta.getName();
		final StringBuilder sb = new StringBuilder();
		if (values != null) {
			for (Iterator<EntityObjectVO> it = values.iterator(); it.hasNext();) {
				final EntityObjectVO v = it.next();
				sb.append(formatOutput(format, v.getField(fieldName)));
				if (it.hasNext()) {
					sb.append(separator);
				}
			}
		}
		return new CollectableValueField(sb.toString());
	}
	
	public static CollectableField format(CollectableEntityField meta, Collectable value) {
		return new CollectableValueField(formatOutput(meta.getFormatOutput(), value.getField(meta.getName())));
	}
	
}

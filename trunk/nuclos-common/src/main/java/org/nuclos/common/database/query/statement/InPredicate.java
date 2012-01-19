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
package org.nuclos.common.database.query.statement;

import java.util.List;

/**
 * IN predicate for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class InPredicate extends Predicate {

	private final List<?> valueList;

	/**
	 * Constructor for in predicate like <code>operand in (1,2,...,n)</code>
	 * @param list
	 */
	public InPredicate(Operand operand, List<?> list) {
		super(operand);
		this.valueList = list;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(this.getOperand().toString());
		sb.append(" in (");
		for (Object oValue : valueList) {
			sb.append(oValue.toString());
			sb.append(',');
		}

		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(')');
		return sb.toString();
	}

}	// class InPredicate

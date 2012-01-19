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

/**
 * BETWEEN predicate for database queries.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class BetweenPredicate extends Predicate {

	private final Object lowerLimit;
	private final Object upperLimit;

	public BetweenPredicate(Operand operand, Object lower, Object upper) {
		super(operand);
		lowerLimit = lower;
		upperLimit = upper;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(this.getOperand().toString());
		sb.append(" between ");
		sb.append(lowerLimit.toString());
		sb.append(" and ");
		sb.append(upperLimit.toString());
		return sb.toString();
	}

}	// class BetweenPredicate

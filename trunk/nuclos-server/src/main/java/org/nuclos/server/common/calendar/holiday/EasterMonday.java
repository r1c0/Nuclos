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
package org.nuclos.server.common.calendar.holiday;

import org.nuclos.server.common.calendar.BigDate;

/**
 * Holiday calculator for easter monday.
 */

public class EasterMonday extends Holiday {

	@Override
	public String getAuthority() {
		return
				"Felix Gursky\'s interpretation of:\n"
						+ " 1. Christophorus Clavius: Calendarium Gregorianum Perpetuum.\n"
						+ "    Cum Summi Pontificis Et Aliorum Principum. Romae, Ex Officina\n"
						+ "    Dominicae Basae, MDLXXXII, Cum Licentia Superiorum.\n"
						+ " 2. Christophorus Clavius: Romani Calendarii A Gregorio XIII.\n"
						+ "    Pontifice Maximo Restituti Explicatio. Romae, MDCIII.;\n";
	}

	@Override
	public int getFirstYear(int base) {
		return 1583 /* our calculations only work this far back. */;
	}

	@Override
	public String getName() {
		return "Easter Monday";
	}

	@Override
	public String getRule() {
		return "Monday after Easter.";
	}

	@Override
	public int when(int year, boolean shift, int base) {
		if (!isYearValid(year, base)) {
			return BigDate.NULL_ORDINAL;
		}
		return new EasterSunday().when(year, false) + 1;

	} // end when.
}

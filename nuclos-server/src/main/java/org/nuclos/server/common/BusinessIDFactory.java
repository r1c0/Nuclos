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
package org.nuclos.server.common;

import java.text.DecimalFormat;
import java.util.Calendar;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.genericobject.Modules;

/**
 * This class generates the business keys for nucleus purposes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public class BusinessIDFactory {

	private BusinessIDFactory() {
	}

	/**
	 * this method generates a link id
	 * a link id consists of four parts and is unique
	 * part one represents a module identifier (BD,AN,AT,MO,RM)
	 * part two represents the month and year in the format jjmm
	 * part three represents a five-digit auto number (based on a database sequence)
	 * @param iModuleId id of module to generate a link id for
	 * @return link id
	 * @postcondition result != null
	 */
	public static String generateSystemIdentifier(int iModuleId) {
		final String sModuleMnemonic = Modules.getInstance().getSystemIdentifierMnemonic(iModuleId);

		// get year and month (second part):
		final String sYear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR)).substring(2);
		final Integer iYear = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String sMonth = iYear.toString();
		sMonth = sMonth.length() == 2 ? sMonth : "0" + sMonth;
		final String sDate = sYear + sMonth;

		// get auto number (third part):
		final String sSequentialNumber = new DecimalFormat("00000").format(getNextSequentialNumber(iModuleId));

		// assemble all parts:
		return sModuleMnemonic + sDate + "#" + sSequentialNumber;
	}

	private static Integer getNextSequentialNumber(int iModuleId) {
		try {
			return SpringDataBaseHelper.getInstance().getNextSequentialNumber(iModuleId);
		}
		catch (DbException ex) {
			final String sModuleLabel = Modules.getInstance().getEntityLabelByModuleId(iModuleId);
			throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("idfactory.exception", sModuleLabel));
				//"F\u00fcr das Modul \"" + sModuleLabel + "\" konnte die laufende Nummer nicht ermittelt werden.");
		}
	}

}	// class BusinessIDFactory

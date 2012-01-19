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
package org.nuclos.server.report.valueobject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A column in a ResultVO.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 * @see ResultVO
 */
public class ResultColumnVO implements java.io.Serializable {

	private static final long serialVersionUID = 4534816554304031434L;

	private static final NumberFormat numberformat = NumberFormat.getNumberInstance(Locale.getDefault());
	private static final DateFormat dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	private String sColumnLabel;
	private String sColumnClass;

	public ResultColumnVO() {
		numberformat.setGroupingUsed(false);
	}

	/**
	 * @return the column's label (header).
	 */
	public String getColumnLabel() {
		return sColumnLabel;
	}

	public void setColumnLabel(String sLabel) {
		this.sColumnLabel = sLabel;
	}

	/**
	 * @return the name of the Java class for this column.
	 */
	public String getColumnClassName() {
		return sColumnClass;
	}

	public void setColumnClassName(String sClassName) {
		this.sColumnClass = sClassName;
	}
	
	public Class<?> getColumnClass() {
		try {
			return Class.forName(sColumnClass);
		} catch (ClassNotFoundException e) {
			return Object.class;
		}
	}

	@Override
	public String toString() {
		return this.sColumnLabel;
	}

	public String format(Object oValue) {
		if (oValue == null) {
			return null;
		}
		if (oValue instanceof java.lang.String) {
			return (String) oValue;
		}
		else if (oValue instanceof java.lang.Number) {
			numberformat.setGroupingUsed(false);
			return numberformat.format(oValue);
		}
		else if (oValue instanceof java.util.Date) {
			return dateformat.format((Date) oValue);
		}
		else if (oValue instanceof java.sql.Timestamp) {
			// DateTime.DATE_TIME_FORMAT.format(...)
			return dateformat.format((Timestamp) oValue);
		}
		else if (oValue instanceof Boolean) {
			return ((Boolean) oValue).booleanValue() ? "ja" : "nein";
		}
		return oValue.toString();
	}

}	// class ResultColumnVO

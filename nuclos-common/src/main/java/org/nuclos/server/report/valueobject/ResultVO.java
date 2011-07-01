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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object representing a query result. To construct a ResultVO, first add the columns, then the rows.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ResultVO implements Serializable {

	private static final long serialVersionUID = -7636635429425501791L;

	private final List<Object[]> lstRows = new ArrayList<Object[]>();

	private final List<ResultColumnVO> lstColumns = new ArrayList<ResultColumnVO>();

	public ResultVO() {
	}

	public void addColumn(ResultColumnVO column) {
		lstColumns.add(column);
	}

	public List<ResultColumnVO> getColumns() {
		return lstColumns;
	}

	public int getColumnCount() {
		return lstColumns.size();
	}

	public void addRow(Object[] aoRow) {
		lstRows.add(aoRow);
	}

	public List<Object[]> getRows() {
		return lstRows;
	}

	public int getRowCount() {
		return lstRows.size();
	}

}	// class ResultVO

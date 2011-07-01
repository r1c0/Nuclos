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
package org.nuclos.client.datasource.querybuilder.gui;

import org.nuclos.client.datasource.querybuilder.QueryBuilderConstants;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonLocaleDelegate;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceParameterValuelistproviderVO;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class ParameterModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_MESSAGE = 2;
	public static final int COLUMN_VLP = 3;

	public static final String[] captions = {CommonLocaleDelegate.getMessage("ParameterModel.7","Parametername"), CommonLocaleDelegate.getMessage("ParameterModel.2","Datentyp"), CommonLocaleDelegate.getMessage("ParameterModel.6","Anzeigename"), CommonLocaleDelegate.getMessage("ParameterModel.9","Valuelistprovider")};
	public static final Class<?>[] classes = {String.class, String.class};
	
	private final boolean blnWithValuelistProviderColumn;
	private final boolean blnWithParameterLabelColumn;

	protected static final ParameterDataType[] adatatype = {
			new ParameterDataType(QueryBuilderConstants.PARAMETER_TYPE_STRING, CommonLocaleDelegate.getMessage("ParameterModel.8","Text")),
			new ParameterDataType(QueryBuilderConstants.PARAMETER_TYPE_INTEGER, CommonLocaleDelegate.getMessage("ParameterModel.5","Integer")),
			new ParameterDataType(QueryBuilderConstants.PARAMETER_TYPE_DOUBLE, CommonLocaleDelegate.getMessage("ParameterModel.4","Double")),
			new ParameterDataType(QueryBuilderConstants.PARAMETER_TYPE_BOOLEAN, CommonLocaleDelegate.getMessage("ParameterModel.1","Boolean")),
			new ParameterDataType(QueryBuilderConstants.PARAMETER_TYPE_DATE, CommonLocaleDelegate.getMessage("ParameterModel.3","Datum"))
	};

	private final List<DatasourceParameterVO> lstParams = new ArrayList<DatasourceParameterVO>();

	public ParameterModel(boolean blnWithValuelistProviderColumn,
			boolean blnWithParameterLabelColumn) {
		this.blnWithValuelistProviderColumn = blnWithValuelistProviderColumn;
		this.blnWithParameterLabelColumn = blnWithParameterLabelColumn;
	}

	@Override
	public int getRowCount() {
		int iResult = 0;
		if (lstParams != null) {
			iResult = lstParams.size();
		}
		return iResult;
	}

	@Override
	public int getColumnCount() {
		int result = 2;
		if (blnWithValuelistProviderColumn)
			result++;
		if (blnWithParameterLabelColumn)
			result++;
		return result;
	}

	@Override
	public String getColumnName(int column) {
		return captions[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object oResult = null;

		final DatasourceParameterVO paramvo = lstParams.get(rowIndex);
		if (paramvo != null) {
			switch (columnIndex) {
				case COLUMN_NAME:
					oResult = paramvo.getParameter();
					break;
				case COLUMN_TYPE:
					oResult = getDataType(paramvo.getDatatype());
					break;
				case COLUMN_MESSAGE:
					oResult = paramvo.getDescription();
					break;
				case COLUMN_VLP:
					oResult = paramvo.getValueListProvider();
					break;
			}
		}
		return oResult;
	}

	@Override
	public void setValueAt(Object oValue, int iRow, int iColumn) {
		final DatasourceParameterVO paramvo = lstParams.get(iRow);
		switch (iColumn) {
			case COLUMN_NAME:
				paramvo.setParameter((String) oValue);
				break;
			case COLUMN_TYPE:
				if (oValue != null) {
					paramvo.setDatatype(((ParameterDataType) oValue).getDataType());
				}
				break;
			case COLUMN_MESSAGE:
				paramvo.setDescription((String) oValue);
				break;
			case COLUMN_VLP:
				paramvo.setValueListProvider((DatasourceParameterValuelistproviderVO) oValue);
				break;
		}
		fireTableCellUpdated(iRow, iColumn);
	}

	public void setInternalValueAt(Object aValue, int iRow, int iColumn) {
		final DatasourceParameterVO vo = lstParams.get(iRow);
		switch (iColumn) {
			case COLUMN_NAME:
				vo.setParameter((String) aValue);
				break;
			case COLUMN_TYPE:
				vo.setDatatype(((ParameterDataType) aValue).getDataType());
				break;
			case COLUMN_MESSAGE:
				vo.setDescription((String) aValue);
				break;
			case COLUMN_VLP:
				vo.setValueListProvider((DatasourceParameterValuelistproviderVO) aValue);
				break;
		}
	}

	public void removeEntry(DatasourceParameterVO vo) {
		lstParams.remove(vo);
		fireTableDataChanged();
	}

	public void removeEntry(int iIndex) {
		if (iIndex > -1 && iIndex < lstParams.size()) {
			lstParams.remove(iIndex);
			fireTableRowsDeleted(iIndex, iIndex);
			fireTableDataChanged();
		}
	}

	public void addEntry(DatasourceParameterVO vo) {
		lstParams.add(vo);
		//NUCLEUSINT-182
		sortValues(lstParams);
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				fireTableDataChanged();
			}
		});
	}

	public List<DatasourceParameterVO> getParameters() {
		return lstParams;
	}

	public void clear() {
		lstParams.clear();
		fireTableDataChanged();
	}

	private Object getDataType(String datatype) {
		for (ParameterDataType dataType : Arrays.asList(adatatype)) {
			if (dataType.getDataType().equals(datatype)) {
				return dataType;
			}
		}
		return null;
	}

	//NUCLEUSINT-182
	private void sortValues(List<DatasourceParameterVO> incoming) {
		Collections.sort(incoming, new Comparator<DatasourceParameterVO>() {
			@Override
			public int compare(DatasourceParameterVO o1, DatasourceParameterVO o2) {
				if (o1 != null && o2 != null)
					if (o1.getParameter() != null && o2.getParameter() != null)
						return o1.getParameter().compareToIgnoreCase(o2.getParameter());
				return 0;
			}
		});
	}

}	// class ParameterModel

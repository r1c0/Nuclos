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
package org.nuclos.client.customcomp.wizard;

import static org.nuclos.common2.CommonLocaleDelegate.getText;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.TranslationVO;

public class CustomComponentTranslationTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	List<TranslationVO> lstRows;
	static String[] labels = TranslationVO.labelsCustomComponent;

	public CustomComponentTranslationTableModel() {
		lstRows = new ArrayList<TranslationVO>();
	}

	public void setRows(List<TranslationVO> rows) {
		lstRows = rows;
		this.fireTableDataChanged();
	}

	public List<TranslationVO> getRows() {
		return lstRows;
	}

	public TranslationVO getTranslationByName(String sName) {
		for(TranslationVO translation : lstRows) {
			if(translation.getLanguage().equals(sName))
				return translation;
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return lstRows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return lstRows.get(rowIndex).getCountry();
			case 1:
				return lstRows.get(rowIndex).getLabels().get(labels[0]);
			case 2:
				return lstRows.get(rowIndex).getLabels().get(labels[1]);
			default:
				break;
		}
		return "";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 1:
				lstRows.get(rowIndex).getLabels().put(labels[0], (String)aValue);
				break;
			case 2:
				lstRows.get(rowIndex).getLabels().put(labels[1], (String)aValue);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex != 0;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
			case 0:
				return getText("nuclos.resplan.l10n.language");
			case 1:
				return getText("nuclos.resplan.l10n.label");
			case 2:
				return getText("nuclos.resplan.l10n.menupath");
			default:
				return "";
		}
	}
}

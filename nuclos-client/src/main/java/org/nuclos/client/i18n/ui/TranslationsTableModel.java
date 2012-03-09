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
package org.nuclos.client.i18n.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.TranslationVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.dal.provider.SystemEntityFieldMetaDataVO;

public class TranslationsTableModel extends AbstractTableModel {

	private final Map<String, SystemEntityFieldMetaDataVO> entityfields;
	
	private final List<String> fields;
	
	private List<TranslationVO> lstRows;

	public TranslationsTableModel(Map<String, SystemEntityFieldMetaDataVO> entityfields) {
		this.entityfields = entityfields;
		this.fields = new ArrayList<String>();
		for (SystemEntityFieldMetaDataVO field : entityfields.values()) {
			if (field.isResourceField()) {
				this.fields.add(field.getField());
			}
		}
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
		for (TranslationVO translation : lstRows) {
			if (translation.getLanguage().equals(sName))
				return translation;
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return fields.size() + 1;
	}

	@Override
	public int getRowCount() {
		return lstRows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return lstRows.get(rowIndex).getCountry();
			default:
				return lstRows.get(rowIndex).getLabels().get(fields.get(columnIndex - 1));
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		lstRows.get(rowIndex).getLabels().put(fields.get(columnIndex - 1), (String) aValue);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex != 0;
	}

	@Override
	public String getColumnName(int column) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		switch (column) {
			case 0:
				return localeDelegate.getMessage("wizard.step.entitytranslationstable.3", "Sprache");
			default:
				return localeDelegate.getText(entityfields.get(fields.get(column - 1)).getLocaleResourceIdForLabel());
		}
	}
}
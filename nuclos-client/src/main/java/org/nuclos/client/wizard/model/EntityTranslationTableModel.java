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
package org.nuclos.client.wizard.model;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.TranslationVO;

public class EntityTranslationTableModel extends AbstractTableModel {

	List<TranslationVO> lstRows;
	static String[] labels = TranslationVO.labelsEntity;
	
	public EntityTranslationTableModel() {
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
		return 5;
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
		case 2:
		case 3:
		case 4:
			return lstRows.get(rowIndex).getLabels().get(labels[columnIndex-1]);
		}
		return "";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 1:
		case 2:
		case 3:
		case 4:
			lstRows.get(rowIndex).getLabels().put(labels[columnIndex-1], (String)aValue);
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
			return getMessage("wizard.step.entitytranslationstable.3", "Sprache");
		case 1:
			return getMessage("wizard.step.entitytranslationstable.5", "Beschriftung Label");		
		case 2:
			return getMessage("wizard.step.entitytranslationstable.6", "Men\u00fcpfad");
		case 3:
			return getMessage("wizard.step.entitytranslationstable.7", "Anzeige Knotendarstellung");
		case 4:
			return getMessage("wizard.step.entitytranslationstable.8", "Bezeichnung des Knoten Tooltips");
		default:
			return "";
		}
	}
}

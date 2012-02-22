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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.SpringLocaleDelegate;

public class EntityAttributeSelectTableModel extends AbstractTableModel {
	
	private List<Attribute> lstAttribute;
	
	public EntityAttributeSelectTableModel() {
		super();
		lstAttribute = new ArrayList<Attribute>();
	}
	
	public void addAttribute(Attribute attribute) {
		lstAttribute.add(attribute);
		this.fireTableDataChanged();
	}
	
	public List<Attribute> getAttributes() {
		return lstAttribute;
	}

	@Override
	public int getColumnCount() {		
		return 6;
	}

	@Override
	public int getRowCount() {
		return lstAttribute.size();
	}
	
	public void removeRow(int row) {
		lstAttribute.remove(row);
		this.fireTableDataChanged();
	}
	
	
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public Attribute getObject(int row) {
		return lstAttribute.get(row);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		switch (columnIndex) {
		case 0:
			return Boolean.class;
	
		default:
			break;
		}
		
		return super.getColumnClass(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Attribute attribute = lstAttribute.get(rowIndex);		 
		
		switch (columnIndex) {
		case 0:
			 return attribute.isForResume();
		case 1:
			return attribute.getLabel();
		case 2:
			return attribute.getDescription();
		case 3:
			return attribute.getDatatyp();
		case 4:
			return attribute.getDatatyp().getScale();
		case 5:
			return attribute.getDatatyp().getPrecision();						
		default:			
			return null;
		}		
	}	
	

	@Override
	public String getColumnName(int column) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		switch (column) {
		case 0:
			return localeDelegate.getMessage("wizard.datatype.7", "\u00dcbernehmen");
		case 1:
			return localeDelegate.getMessage("wizard.step.attributeproperties.1", "Anzeigename");
		case 2:
			return localeDelegate.getMessage("wizard.step.attributeproperties.2", "Beschreibung");
		case 3:
			return localeDelegate.getMessage("wizard.step.attributeproperties.3", "Datentyp");		
		case 4:
			return localeDelegate.getMessage("wizard.datatype.3", "Feldbreite");
		case 5:
			return localeDelegate.getMessage("wizard.datatype.4", "Nachkommastellen");

		default:			
			return "";
		}		
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		if(columnIndex == 0) {
			Attribute attr = lstAttribute.get(rowIndex);
			attr.setResume((Boolean)aValue);
		}
		
		
	}
	
	

}

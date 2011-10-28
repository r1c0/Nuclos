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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.TranslationVO;
import org.nuclos.common2.StringUtils;

public class EntityAttributeTableModel extends AbstractTableModel {
	
	private List<Attribute> lstAttribute;
	private List<Attribute> lstRemovedAttribute;
	private Map<Attribute, List<TranslationVO>> mpTranslation;
	private boolean blnStateModel;
	
	public EntityAttributeTableModel() {
		lstAttribute = new ArrayList<Attribute>();
		lstRemovedAttribute = new ArrayList<Attribute>();
		mpTranslation = new HashMap<Attribute, List<TranslationVO>>();
	}
	
	public void setAttributeAt(Attribute attribute, int row) {
		if(row > lstAttribute.size()){
			row = lstAttribute.size();
		}
		
		lstAttribute.set(row, attribute);
		this.fireTableDataChanged();
	}
	
	public void addAttribute(Attribute attribute) {
		lstAttribute.add(attribute);
		this.fireTableDataChanged();
	}
	
	public boolean hasAttributeChangeName() {
		for(Attribute attr : getAttributes()) {
			if(attr.hasInternalNameChanged())
				return true;
		}
		return false;
	}
	
	
	@Override
	public void fireTableDataChanged() {
		super.fireTableDataChanged();
	}

	public List<Attribute> getAttributes() {
		return lstAttribute;
	}
	
	public void addTranslation(Attribute attr, List<TranslationVO> lstTranslation) {
		mpTranslation.put(attr, lstTranslation);
	}
	
	public Map<Attribute, List<TranslationVO>> getTranslation () {
		return mpTranslation;
	}
	
	public Collection<Attribute> getAttributesByGroup(String sGroup) {
		String group = StringUtils.emptyIfNull(sGroup);
		Collection<Attribute> col = new ArrayList<Attribute>();
		for(Attribute attr : getAttributes()) {
			if(group.equals(attr.getAttributeGroup()))
				col.add(attr);
		}
		
		return col;
	}
	
	public Map<String, List<Attribute>> getAttributeMap() {
		Map<String, List<Attribute>> mp = new HashMap<String, List<Attribute>>();
		
		for(Attribute attr : getAttributes()) {
			if(mp.get(attr.getAttributeGroup()) == null) {
				mp.put(attr.getAttributeGroup(), Collections.synchronizedList(new ArrayList<Attribute>()));
			}
			mp.get(attr.getAttributeGroup()).add(attr);
		}
		
		return mp;
	}

	@Override
	public int getColumnCount() {		
		return Attribute.quantity;
	}

	@Override
	public int getRowCount() {
		return lstAttribute.size();
	}
	
	public void removeRow(int row, boolean addToList) {
		Attribute attr = lstAttribute.remove(row);
		if(addToList)
			lstRemovedAttribute.add(attr);
		this.fireTableDataChanged();
	}
	
	public Attribute getObject(int row) {
		return lstAttribute.get(row);
	}
	
	public List<Attribute> getRemoveAttributes() {
		return lstRemovedAttribute;
	}
	

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		switch (columnIndex) {
		case 5:
			return Boolean.class;
		case 6:
			return Boolean.class;
		case 7:
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
			return attribute.getLabel();
		case 1:
			return attribute.getDescription();
		case 2:
			return attribute.getDatatyp();
		case 3:
			return attribute.getDatatyp().getScale();
		case 4:			
			return attribute.getDatatyp().getPrecision();
		case 5:
			return new Boolean(attribute.isDistinct());
		case 6:
			return new Boolean(attribute.isLogBook());
		case 7:
			return new Boolean(attribute.isMandatory());
		case 8:
			return attribute.getInternalName();
		case 9:
			return attribute.getAttributeGroup();

		default:			
			return null;
		}		
	}	
	

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return getMessage("wizard.step.attributeproperties.1", "Anzeigename");
		case 1:
			return getMessage("wizard.step.attributeproperties.2", "Beschreibung");
		case 2:
			return getMessage("wizard.step.attributeproperties.3", "Datentyp");		
		case 3:
			return getMessage("wizard.datatype.3", "Feldbreite");
		case 4:
			return getMessage("wizard.datatype.4", "Nachkommastellen");
		case 5:
			return getMessage("wizard.step.attributeproperties.7", "Eindeutig");
		case 6:
			return getMessage("wizard.step.attributeproperties.8", "Logbuch");
		case 7:
			return getMessage("wizard.step.attributeproperties.9", "Pflichtfeld");
		case 8:
			return getMessage("wizard.step.attributeproperties.10", "Feldname");
		case 9:
			return "Attributegruppe"; //getMessage("wizard.step.attributeproperties.10", "Attributegruppe");

		default:			
			return "";
		}		

	}
	
	public void setStatemodel(boolean bStatemodel) {
		this.blnStateModel = bStatemodel;
	}
	
	public boolean isStatemodel() {
		return blnStateModel;
	}

}

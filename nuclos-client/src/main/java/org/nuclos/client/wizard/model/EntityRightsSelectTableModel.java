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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;
import org.nuclos.server.common.MasterDataPermission;
import org.nuclos.server.common.ModulePermission;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class EntityRightsSelectTableModel extends AbstractTableModel {
	
	private List<MasterDataVO> lstUserRights;
	private Map<String, Integer> mapPermission;
	
	private int iType;
	
	public static int TYPE_STATEMODEL = 0;
	public static int TYPE_MASTERDATA = 1;
	
	public static int COLUMN_COUNT = 2;
	
	public EntityRightsSelectTableModel() {
		super();
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		lstUserRights = new ArrayList<MasterDataVO>();
		mapPermission = new HashMap<String, Integer>();
		mapPermission.put(localeDelegate.getMessage(
				"masterdata.permission.read", "Lesen"), MasterDataPermission.READONLY.getValue());
		mapPermission.put(localeDelegate.getMessage(
				"masterdata.permission.write", "Lesen/Schreiben"), MasterDataPermission.READWRITE.getValue());
		mapPermission.put(localeDelegate.getMessage(
				"masterdata.permission.delete", "Lesen/Schreiben/L\u00f6schen"), MasterDataPermission.DELETE.getValue());	
	}
	
	public void setType(int type) {
		this.iType = type;
		if(iType == TYPE_STATEMODEL) {
			mapPermission.put(SpringLocaleDelegate.getInstance().getMessage(
					"module.permission.delete.physical", "Lesen/Schreiben/Physikalisch L\u00f6schen"), 15);
		}
		this.fireTableDataChanged();
	}
	
	public void addRole(MasterDataVO role) {
		boolean add = true;
//		for(MasterDataVO vo : lstUserRights) {
//			if(vo.getField("role").equals(role.getField("name"))) {
//				add = false;
//			}
//		}
//		if(add) {
			lstUserRights.add(role);
			this.fireTableDataChanged();
//		}
	}
	
	public List<MasterDataVO> getUserRights() {
		return lstUserRights;
	}
	
	public void clear() {
		lstUserRights = new ArrayList<MasterDataVO>();
		this.fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {		
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return lstUserRights.size();
	}
	
	public void removeRow(int row) {
		lstUserRights.remove(row);
		this.fireTableDataChanged();
	}
	
	
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public MasterDataVO getObject(int row) {
		return lstUserRights.get(row);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		MasterDataVO attribute = lstUserRights.get(rowIndex);		 
		
		switch (columnIndex) {
		case 0:
			 return attribute.getField("role");
		case 1:
			Integer i = null;
			if(attribute.getField("masterdatapermission") != null) {
				i = (Integer)attribute.getField("masterdatapermission");
			}
			else if(attribute.getField("modulepermission") != null) {
				i = (Integer)attribute.getField("modulepermission");
			}
			else {
				return "";
			}

			if(i.intValue() != ModulePermission.DELETE_PHYSICALLY.getValue().intValue()) {
				MasterDataPermission permission = MasterDataPermission.getInstance(i);
				Object value = (permission instanceof KeyEnum) ? ((KeyEnum<?>) permission).getValue() : permission.name();
				String text = (permission instanceof Localizable) ? SpringLocaleDelegate.getInstance().getText(
						(Localizable) permission) : permission.toString();
				CollectableField cf = new LocalizedCollectableValueField(value, text);			
				return cf;
			}
			else {
				CollectableField cf = new LocalizedCollectableValueField("module.permission.delete.physical", "Lesen/Schreiben/Physikalisch L\u00f6schen");			
				return cf;
			}
		
		default:			
			return null;
		}		
	}	
	

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return SpringLocaleDelegate.getInstance().getMessage("wizard.step.entityrightstable.1", "Gruppe");
		case 1:
			return SpringLocaleDelegate.getInstance().getMessage("wizard.step.entityrightstable.2", "Rechte");
		default:			
			return "";
		}		
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);
		if(columnIndex == 1) {
			if(aValue instanceof LocalizedCollectableValueField) {
				LocalizedCollectableValueField field = (LocalizedCollectableValueField)aValue;
				MasterDataVO attr = lstUserRights.get(rowIndex);
				if(iType == TYPE_STATEMODEL) {
					attr.setField("modulepermission", field.getValue());
				}
				else {
					attr.setField("masterdatapermission", field.getValue());
				}
			}
			else if(aValue instanceof String) {
				MasterDataVO attr = lstUserRights.get(rowIndex);
				if(iType == TYPE_STATEMODEL) {
					attr.setField("modulepermission", null);
				}
				else {
					attr.setField("masterdatapermission", null);
				}
			}
		}
	}

}

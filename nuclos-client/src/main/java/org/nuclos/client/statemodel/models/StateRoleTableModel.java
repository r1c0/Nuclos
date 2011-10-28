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
package org.nuclos.client.statemodel.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Table model for the roles permitted to do state transitions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateRoleTableModel extends DefaultTableModel {

	class RoleOrderComparator implements Comparator<MasterDataVO> {
		@Override
		public int compare(MasterDataVO mdvo1, MasterDataVO mdvo2) {
			return ((String) mdvo1.getField("name")).compareTo((String) mdvo2.getField("name"));
		}
	}

	private static String[] asColumnNames = {CommonLocaleDelegate.getMessage("StateRoleTableModel.1","Benutzergruppe"), CommonLocaleDelegate.getMessage("StateRoleTableModel.2","Beschreibung")};
	private final List<MasterDataVO> lstRoles = new ArrayList<MasterDataVO>();

	public StateRoleTableModel() {
		super();
	}

	public void setRoles(List<MasterDataVO> roles) {
		lstRoles.clear();
		lstRoles.addAll(roles);
		Collections.sort(lstRoles, new RoleOrderComparator());
		this.fireTableDataChanged();
	}

	public List<MasterDataVO> getRoles() {
		return lstRoles;
	}

	@Override
	public int getRowCount() {
		int result = 0;
		if (lstRoles != null) {
			result = lstRoles.size();
		}
		return result;
	}

	public MasterDataVO getRow(int iRow) {
		return lstRoles.get(iRow);
	}

	@Override
	public int getColumnCount() {
		return asColumnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return asColumnNames[column];
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		Object result = null;
		switch (iColumn) {
			case 0:
				result = getRow(iRow).getField("name");
				break;
			case 1:
				result = getRow(iRow).getField("description");
				break;
		}
		return result;
	}
	
	public void addRow(MasterDataVO vo) {
		lstRoles.add(vo);
		fireTableDataChanged();
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if(aValue instanceof MasterDataVO) {
			MasterDataVO vo = (MasterDataVO)aValue;
			lstRoles.set(row, vo);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return true;
		return false;
	}

}	// class StateRoleTableModel

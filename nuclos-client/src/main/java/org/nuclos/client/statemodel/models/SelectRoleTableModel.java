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

import org.nuclos.common2.CommonLocaleDelegate;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.client.statemodel.RoleRepository;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Table model for selection of roles in the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class SelectRoleTableModel extends AbstractTableModel {

	protected static String[] columnNames = {CommonLocaleDelegate.getMessage("SelectRoleTableModel.1","Benutzergruppe"), CommonLocaleDelegate.getMessage("SelectRoleTableModel.2","Beschreibung")};
	protected List<MasterDataVO> lstRoles = null;
	protected List<MasterDataVO> lstExcludeRoles = new ArrayList<MasterDataVO>();

	public SelectRoleTableModel() throws RemoteException {
		RoleRepository.getInstance().updateRoles();
	}

	@Override
	public int getRowCount() {
		return lstRoles != null ? lstRoles.size() : 0;
	}

	public MasterDataVO getRow(int iRow) {
		return lstRoles.get(iRow);
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
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

	public List<MasterDataVO> getExcludeRoles() {
		return lstExcludeRoles;
	}

	public void setExcludeRoles(List<MasterDataVO> lstExcludeRoles) throws RemoteException {
		lstRoles = RoleRepository.getInstance().filterRolesByVO(lstExcludeRoles);
	}

	public List<MasterDataVO> getRoles() {
		return lstRoles;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}

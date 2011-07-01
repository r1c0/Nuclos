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
import java.util.List;

import javax.ejb.CreateException;
import javax.swing.table.AbstractTableModel;

import org.nuclos.client.statemodel.RuleRepository;
import org.nuclos.client.statemodel.SortedRuleVO;

/**
 * Table model for selection of rules for state transitions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class SelectRuleTableModel extends AbstractTableModel {

	private static final String[] asColumnNames = {CommonLocaleDelegate.getMessage("SelectRuleTableModel.2","Regel"), CommonLocaleDelegate.getMessage("SelectRuleTableModel.1","Beschreibung")};

	private List<SortedRuleVO> lstRules;

	public SelectRuleTableModel() throws CreateException, RemoteException {
		RuleRepository.getInstance().updateRules();
	}

	@Override
	public int getRowCount() {
		return lstRules != null ? lstRules.size() : 0;
	}

	public SortedRuleVO getRow(int iRow) {
		return lstRules.get(iRow);
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
				result = getRow(iRow).getName();
				break;
			case 1:
				result = getRow(iRow).getDescription();
				break;
		}
		return result;
	}

	public void setExcludeRules(List<SortedRuleVO> lstExcludeRules) throws CreateException, RemoteException {
		this.lstRules = RuleRepository.getInstance().filterRulesByVO(lstExcludeRules);
	}

	public List<SortedRuleVO> getRules() {
		return lstRules;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}  // class SelectRuleTableModel

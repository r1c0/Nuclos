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

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.client.eventsupport.EventSupportDelegate;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.client.statemodel.RuleRepository;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;

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

	private static final String[] asColumnNames = {
		SpringLocaleDelegate.getInstance().getMessage("SelectRuleTableModel.2","Regel"), 
		SpringLocaleDelegate.getInstance().getMessage("SelectRuleTableModel.1","Beschreibung")};

	private List<SortedRuleVO> lstRules;

	public SelectRuleTableModel() throws RemoteException {
		RuleRepository.getInstance().updateRules();
		EventSupportRepository.getInstance().updateEventSupports();
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

	public void setExcludeRules(List<SortedRuleVO> lstExcludeRules) throws RemoteException {
		this.lstRules = RuleRepository.getInstance().filterRulesByVO(lstExcludeRules);
		// now add to all rules the eventsupports that have been added via EventSupportManagement
		String[] sEventSupportaSupported = new String[] {
				"org.nuclos.api.eventsupport.StateChangeSupport",
				"org.nuclos.api.eventsupport.StateChangeFinalSupport"};

		List<EventSupportSourceVO> eventSupportsByType = 
				EventSupportRepository.getInstance().getEventSupportsByTypes(sEventSupportaSupported);
		
		for (EventSupportSourceVO esVO : eventSupportsByType) {
			SortedRuleVO srVO = new SortedRuleVO(esVO, 0, false);		
			boolean toExclude = false;
			for (SortedRuleVO srVOExlcude : lstExcludeRules) {
				if (srVO.getClassname().equals(srVOExlcude.getClassname())) {
					toExclude = true;
					break;
				}
			}
			
			if (!toExclude)
				this.lstRules.add(srVO);
		}
	}

	public List<SortedRuleVO> getRules() {
		return lstRules;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}  // class SelectRuleTableModel

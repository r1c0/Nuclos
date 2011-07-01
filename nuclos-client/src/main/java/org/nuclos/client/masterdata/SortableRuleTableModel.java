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
package org.nuclos.client.masterdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Table model for sorted and sortable rules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class SortableRuleTableModel extends DefaultTableModel {

	class RuleOrderComparator implements Comparator<SortedRuleVO> {
		@Override
		public int compare(SortedRuleVO vo1, SortedRuleVO vo2) {
			return vo1.getOrder().compareTo(vo2.getOrder());
		}
	}

	public static final String[] asColumnNames = {CommonLocaleDelegate.getMessage("SortableRuleTableModel.2","Regel"), CommonLocaleDelegate.getMessage("SortableRuleTableModel.1","Beschreibung"), CommonLocaleDelegate.getMessage("SortableRuleTableModel.3","Ausführung im Anschluss")};

	private final List<SortedRuleVO> lstRules = new ArrayList<SortedRuleVO>();
	
	private final List<ActionListener> lstValueChangedListener = new ArrayList<ActionListener>();

	public SortableRuleTableModel() {
		super();
	}

	public void setRules(List<SortedRuleVO> rules) {
		lstRules.clear();
		lstRules.addAll(rules);
		Collections.sort(lstRules, new RuleOrderComparator());
		this.fireTableDataChanged();
	}

	public List<SortedRuleVO> getRules() {
		return lstRules;
	}

	@Override
	public int getRowCount() {
		int iReturn = 0;
		if (lstRules != null) {
			iReturn = lstRules.size();
		}
		return iReturn;
	}

	public SortedRuleVO getRow(int iRow) {
		return lstRules.get(iRow);
	}

	public void addRow(SortedRuleVO sortedrulevo) {
		lstRules.add(sortedrulevo);
		sortedrulevo.setOrder(lstRules.size());
		fireTableDataChanged();
	}

	@Override
	public void removeRow(int iRow) {
		lstRules.remove(iRow);
		for (int i = iRow; i < lstRules.size(); i++) {
			lstRules.get(i).setOrder(lstRules.get(i).getOrder() - 1);
		}
		fireTableDataChanged();
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
			case 2:
				result = getRow(iRow).isRunAfterwards();
				break;
		}
		return result;
	}
	
	

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if	(aValue instanceof SortedRuleVO) {
			SortedRuleVO vo = (SortedRuleVO)aValue;
			lstRules.set(row, vo);
		} else if (aValue instanceof Boolean && column == 2) {
			lstRules.get(row).setRunAfterwards((Boolean) aValue);
			for (ActionListener al : lstValueChangedListener) {
				al.actionPerformed(new ActionEvent(this, lstRules.get(row).getId(), "RunAfterwards_changed"));
			}
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		if(columnIndex == 0 || columnIndex == 2)
			return true;
		
		return false;
	}

	public void moveRowUp(int iIndex) {
		final SortedRuleVO vo = getRow(iIndex);
		final SortedRuleVO prevVo = getRow(iIndex - 1);
		if (vo != null && prevVo != null) {
			prevVo.setOrder(vo.getOrder());
			vo.setOrder(vo.getOrder() - 1);
			Collections.sort(lstRules, new RuleOrderComparator());
			fireTableDataChanged();
		}
	}

	public void moveRowDown(int iIndex) {
		final SortedRuleVO vo = getRow(iIndex);
		final SortedRuleVO nextVo = getRow(iIndex + 1);
		if (vo != null && nextVo != null) {
			nextVo.setOrder(vo.getOrder());
			vo.setOrder(vo.getOrder() + 1);
			Collections.sort(lstRules, new RuleOrderComparator());
			fireTableDataChanged();
		}
	}
	
	public void addValueChangedListener(ActionListener al) {
		this.lstValueChangedListener.add(al);
	}
	
	public void removeValueChangedListener(ActionListener al) {
		this.lstValueChangedListener.remove(al);
	}

}	// class SortableRuleTableModel

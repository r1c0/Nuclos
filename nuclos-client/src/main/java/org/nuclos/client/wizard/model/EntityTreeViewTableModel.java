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
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.nuclos.client.ui.dnd.IReorderable;
import org.nuclos.common.EntityTreeViewVO;

/**
 * Table model for the subforms used in the entity layout.
 * <p>
 * Used in {@link org.nuclos.client.wizard.model.EntityTreeViewTableModel}.
 * </p>
 * @author Thomas Pasch (javadocs)
 */
public class EntityTreeViewTableModel extends AbstractTableModel implements IReorderable {

	private List<EntityTreeViewVO> lstRows;
	
	private IReorderable editor;
	
	public EntityTreeViewTableModel(IReorderable editor) {
		this.lstRows = new ArrayList<EntityTreeViewVO>();
		this.editor = editor;
	}	
	
	public void setRows(Collection<EntityTreeViewVO> rows) {
		// Make defensive copy. (Thomas Pasch)
		lstRows = new ArrayList<EntityTreeViewVO>(rows);
		this.fireTableDataChanged();
	}
	
	public List<EntityTreeViewVO> getRows() {
		return lstRows;
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
		final EntityTreeViewVO treeView = lstRows.get(rowIndex);
		switch(columnIndex) {
		case 0:
			return treeView.getEntity();
		case 1:
			return treeView.getField();
		case 2:
			return treeView.getFoldername();
		case 3:
			return treeView.isActive();
		case 4:
			return treeView.getSortOrder();
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		final EntityTreeViewVO treeView = lstRows.get(rowIndex);
		switch(columnIndex) {
		case 0:
			treeView.setEntity((String)aValue);
			break;
		case 1:
			treeView.setField((String)aValue);
			break;
		case 2:
			treeView.setFoldername((String)aValue);
			break;
		case 3:
			treeView.setActive(((Boolean) aValue));
			break;
		case 4:
			treeView.setSortOrder(((Integer) aValue));
			break;
		default:
			throw new IllegalArgumentException();
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
	@Override
    public Class<?> getColumnClass(int columnIndex) {
		final Class<?> result;
		switch(columnIndex) {
		case 0:
		case 1:
		case 2:
			result = String.class;
			break;
		case 3:
			result = Boolean.class;
			break;
		case 4:
			result = Integer.class;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return result;
    }
    
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex < 1 || columnIndex >= 4) ? false : true;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case 0:
			return "Unterformular"; //getMessage("wizard.step.entitytranslationstable.3", "Sprache");
		case 1:
			return "Feldname"; //getMessage("wizard.step.entitytranslationstable.5", "Beschriftung Label");		
		case 2:
			return "Ordnername"; //getMessage("wizard.step.entitytranslationstable.6", "Men\u00fcpfad");
		case 3:
			return "Aktiv";
		case 4:
			return "Reihenfolge";
		default:			
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void reorder(int fromModel, int toModel) {
		// swap the 2 rows (but don't swap the sort order)
		final EntityTreeViewVO from = lstRows.get(fromModel);
		final Integer fromSO = from.getSortOrder();
		final EntityTreeViewVO to = lstRows.get(toModel);
		from.setSortOrder(to.getSortOrder());
		to.setSortOrder(fromSO);
		lstRows.set(fromModel, to);
		lstRows.set(toModel, from);
		
		// ensure that every row has an distinct sort order value
		int so = -99999;
		for (EntityTreeViewVO vo: lstRows) {
			int cso = vo.getSortOrder() == null ? -99999 : vo.getSortOrder().intValue();
			if (cso <= so) {
				vo.setSortOrder(++so);
			}
			else {
				so = cso;
			}
		}
		
		// swap editor as well
		editor.reorder(fromModel, toModel);
		
		// fireTableRowsUpdated(fromModel, fromModel);
		// fireTableRowsUpdated(toModel, toModel);
		fireTableDataChanged();
	}

}


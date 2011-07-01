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
package org.nuclos.tools.translation.translationdata;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;

import org.nuclos.tools.translation.translationdata.TranslationData.TranslationAction;
import org.nuclos.tools.translation.translationdata.columns.TranslationDataColumn;

@SuppressWarnings("serial")
public class TranslationDataTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox comboBox = new JComboBox();

	public TranslationDataTable(TranslationDataTableModel model) {
		super(model);
		setPreferredScrollableViewportSize(new Dimension(500, 70));
		setAutoCreateRowSorter(true);
		((JLabel)getDefaultRenderer(Integer.class)).setHorizontalAlignment(SwingConstants.LEFT);
		setComboBoxCellEditor();
		setColumnWidths();
	}

	public void addActionColumnListener(ActionListener listener) {
		if (comboBox != null)
			comboBox.addActionListener(listener);
	}

	private void setComboBoxCellEditor() {
		for (TranslationAction action : TranslationAction.values())
			comboBox.addItem(action.getName());
		getColumnModel().getColumn(TranslationDataColumn.COLUMN_INDEX_ACTION).setCellEditor(new DefaultCellEditor(comboBox));
	}

	private void setColumnWidths() {
		for (int i = 0; i < getColumnCount(); i++) {
			DefaultTableColumnModel colModel  = (DefaultTableColumnModel) getColumnModel();
			colModel.getColumn(i).setPreferredWidth(TranslationDataColumn.makeTranslationDataColumn(i).getPreferredColumnWidth());
		}
	}

	public List<TranslationData> getSelectedTranslationData() {
		List<TranslationData> result = new ArrayList<TranslationData>();
		if (getSelectedRowCount() > 0) {
			for (int index : getSelectedRows()) {
				result.add(((TranslationDataTableModel)getModel()).getData().get(convertRowIndexToModel(index)));
			}
		}
		return result;
	}

	public List<TranslationData> getTranslationData(int[] indicies) {
		List<TranslationData> result = new ArrayList<TranslationData>();
		if (getSelectedRowCount() > 0) {
			for (int index : indicies) {
				//result.add(((TranslationDataTableModel)getModel()).getData().get(convertRowIndexToModel(index)));
				result.add(((TranslationDataTableModel)getModel()).getData().get(index));
			}
		}
		return result;
	}

	@Override
	public String getToolTipText(MouseEvent event) {

		int rowIndex = rowAtPoint(event.getPoint());
		int colIndex = convertColumnIndexToModel(columnAtPoint(event.getPoint()));

		if (colIndex == TranslationDataColumn.COLUMN_INDEX_FILE)
			return ((TranslationDataTableModel)getModel()).getData().get(convertRowIndexToModel(rowIndex)).getFilePath();

		return super.getToolTipText(event);
	}

}

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
package org.nuclos.client.datasource.querybuilder.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.nuclos.client.datasource.querybuilder.QueryBuilderIcons;
import org.nuclos.client.datasource.querybuilder.controller.QueryBuilderController;
import org.nuclos.client.datasource.querybuilder.shapes.gui.ConstraintColumn;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.TransformerUtils;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Constraint;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * Column selection table.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ColumnSelectionTable extends CommonJTable {

	private static class CheckBoxCellRenderer extends DefaultTableCellRenderer {
		private JCheckBox checkBox = new JCheckBox();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			checkBox.setHorizontalAlignment(SwingConstants.CENTER);
			checkBox.setSelected((Boolean) value);
			if (isSelected) {
				checkBox.setBackground(Color.LIGHT_GRAY);
			}
			else {
				checkBox.setBackground(Color.WHITE);
			}
			return checkBox;
		}
	}
	
	
	/**
	 * cell renderer, that sets a distance of 5 pixel to the left side of the text in the cells,
	 * so that the user can read the contents better
	 */
	protected class ColumnSelectionTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (comp instanceof JLabel) {
				JLabel label = (JLabel)comp;
				Insets insets = label.getInsets();
				insets.left = 5;
				label.setBorder(new EmptyBorder(insets));
				return label;
			}

			return comp;
		}
	}


	private static class ComboBoxEditor extends DefaultCellEditor {
		ComboBoxEditor(JComboBox cb) {
			super(cb);
			cb.setEditable(false);
			setClickCountToStart(1);
		}
	}

	private static class TextFieldEditor extends DefaultCellEditor {
		final JTextField textField;
		TextFieldEditor(JTextField textField) {
			super(textField);
			setClickCountToStart(1);
			this.textField = textField;
		}
		
		public JTextField getTextField() {
			return this.textField;
		}
		
		private static class MyDocumentListener implements DocumentListener {
			private final ColumnSelectionTable table;

			MyDocumentListener(ColumnSelectionTable table) {
				this.table = table;
			}

			@Override
			public void changedUpdate(DocumentEvent ev) {
				// this is never called.
				assert false;
			}

			@Override
			public void insertUpdate(final DocumentEvent ev) {
				//if (ev.getLength() > 0) {
					((ColumnSelectionTableModel)table.getModel()).fireTableDataChanged();
				//}				
			}
			@Override
			public void removeUpdate(final DocumentEvent ev) {
				//if (ev.getLength() > 0) {
					((ColumnSelectionTableModel)table.getModel()).fireTableDataChanged();
				//}				
			}
		}
		
	}

	private class HeaderRenderer extends DefaultTableCellRenderer {
		private final JLabel label = new JLabel(QueryBuilderIcons.iconEmpty16);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			if (ColumnSelectionTable.this.iMarkedColumn == column) {
				label.setBackground(new Color(128, 128, 255));
				label.setIcon(QueryBuilderIcons.iconDown16);
			}
			else {
				label.setIcon(QueryBuilderIcons.iconEmpty16);
				if (isSelected) {
					label.setBackground(Color.GRAY);
				}
				else {
					label.setBackground(Color.LIGHT_GRAY);
				}
			}
			return label;
		}
	}

	private ComboBoxEditor tableEditor;
	private DefaultCellEditor visibleEditor;
	private ComboBoxEditor groupByEditor;
	private ComboBoxEditor orderByEditor;
	private TextFieldEditor conditionEditor;

	protected QueryBuilderController controller;

	protected CheckBoxCellRenderer checkBoxCellRenderer = new CheckBoxCellRenderer();
	protected ColumnSelectionTableCellRenderer cellRenderer = new ColumnSelectionTableCellRenderer();
	protected DropTarget dropTarget;
	protected int iMarkedColumn = -1;

	/**
	 * @param controller
	 */
	public ColumnSelectionTable(QueryBuilderController controller) {
		setCellSelectionEnabled(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.controller = controller;

		tableEditor = new ComboBoxEditor(new JComboBox());

		final JCheckBox chkbxVisible = new JCheckBox();
		visibleEditor = new DefaultCellEditor(chkbxVisible);
		chkbxVisible.setHorizontalAlignment(SwingConstants.CENTER);
		groupByEditor = new ComboBoxEditor(new JComboBox(CollectionUtils.transform(Arrays.asList(DatasourceVO.GroupBy.values()), TransformerUtils.toStringTransformer()).toArray()));
		orderByEditor = new ComboBoxEditor(new JComboBox(CollectionUtils.transform(Arrays.asList(DatasourceVO.OrderBy.values()), TransformerUtils.toStringTransformer()).toArray()));
		conditionEditor = new TextFieldEditor(new JTextField());
		conditionEditor.getTextField().getDocument().addDocumentListener(new TextFieldEditor.MyDocumentListener(this));

		((JComboBox) tableEditor.getComponent()).addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					final Table table = (Table) ev.getItem();
					if (table != null) {
//			       initColumns(table);
					}
				}
			}
		});

		dropTarget = new DropTarget(this, DnDConstants.ACTION_LINK, controller.getDropTargetListener());

		getTableHeader().setDefaultRenderer(new HeaderRenderer());
	}

	/**
	 *
	 * @param dataModel
	 */
	@Override
	public void setModel(TableModel dataModel) {
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		super.setModel(dataModel);
	}

	/**
	 *
	 * @param row
	 * @param column
	 * @return table cell editor
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		TableCellEditor result = null;
		switch (row) {
			case ColumnSelectionTableModel.TABLE_ROW:
				result = tableEditor;
				break;
			case ColumnSelectionTableModel.COLUMN_ROW:
				final Table table = (Table) getValueAt(ColumnSelectionTableModel.TABLE_ROW, column);
				final JComboBox cb = new JComboBox(new DefaultComboBoxModel(getColumns(table).toArray()));
				final TableCellEditor cellEditor = new DefaultCellEditor(cb);
				cb.setEditable(true);
				result = cellEditor;
				break;
			case ColumnSelectionTableModel.VISIBLE_ROW:
				result = visibleEditor;
				break;
			case ColumnSelectionTableModel.GROUPBY_ROW:
				result = groupByEditor;
				break;
			case ColumnSelectionTableModel.ORDERBY_ROW:
				result = orderByEditor;
				break;
			default:
				result = conditionEditor;
		}
		return result;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (row == ColumnSelectionTableModel.VISIBLE_ROW) {
			return checkBoxCellRenderer;
		}
		return cellRenderer;//super.getCellRenderer(row, column);
	}

	/**
	 *
	 * @param tables
	 */
	public void updateTables(List<Table> tables) {
		final JComboBox comboBox = ((JComboBox) tableEditor.getTableCellEditorComponent(this, null, false, 0, 0));
		comboBox.setModel(new DefaultComboBoxModel(tables.toArray()));
	}

	/**
	 *
	 */
	private List<ConstraintColumn> getColumns(Table table) {
		if (table != null) {
			final Map<String, ConstraintColumn> mpconstraintcolumn = CollectionUtils.newHashMap();
			final List<ConstraintColumn> lstconstraintcolumn = new ArrayList<ConstraintColumn>();
			for (Column column : table.getColumns()) {
				final ConstraintColumn constraintcolumn = new ConstraintColumn(column.getTable(), column.getName(), column.getType(),
					column.getLength(), column.getPrecision(), column.getScale(), column.isNullable());
				mpconstraintcolumn.put(constraintcolumn.getName(), constraintcolumn);
				lstconstraintcolumn.add(constraintcolumn);
			}

			for (Constraint constraint : (table.getConstraints())) {
				for (Column column : constraint.getColumns()) {
					final ConstraintColumn constraintcolumn = mpconstraintcolumn.get(column.getName());
					if (constraintcolumn != null) {
						constraintcolumn.addConstraint(constraint);
					}
				}
			}
			
			Collections.sort(lstconstraintcolumn, new Comparator<ConstraintColumn>() {

				@Override
				public int compare(ConstraintColumn col1, ConstraintColumn col2) {					
					return col1.getName().compareTo(col2.getName());
				}
				
				
			});
			
			
			return lstconstraintcolumn;
		}
		return Collections.emptyList();
	}

	/**
	 *
	 * @return drop target
	 */
	@Override
	public DropTarget getDropTarget() {
		return dropTarget;
	}

	/**
	 *
	 * @param dtde
	 */
	public void dragOver(DropTargetDragEvent dtde) {
		final Point2D p = dtde.getLocation();
		final int iOldIndex = iMarkedColumn;
		iMarkedColumn = getColumnModel().getColumnIndexAtX((int) p.getX());
		if (iOldIndex != iMarkedColumn) {
			getTableHeader().repaint();
		}
	}

	/**
	 *
	 * @param dtde
	 */
	public void drop(DropTargetDropEvent dtde) {
		iMarkedColumn = -1;
		getTableHeader().repaint();
	}

	/**
	 *
	 * @return marked index
	 */
	public int getMarkedIndex() {
		return iMarkedColumn;
	}

	/**
	 *
	 * @return next available index
	 */
	public int getNextAvailableIndex() {
		for (int i = 0; i < ColumnSelectionTableModel.COLUMN_COUNT; i++) {
			final ColumnEntry entry = ((ColumnSelectionTableModel) getModel()).getColumn(i);
			if (entry.getTable() == null && entry.getColumn() == null) {
				return i;
			}
		}
		return -1; // unlikely
	}

}	// class ColumnSelectionTable

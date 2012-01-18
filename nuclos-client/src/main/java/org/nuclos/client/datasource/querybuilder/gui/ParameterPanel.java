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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.valuelistprovidereditor.ValueListProviderEditor;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.RowIndicatorTable;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceParameterValuelistproviderVO;

/**
 * Panel for DatasourceParameters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class ParameterPanel extends JPanel {

	private final JScrollPane scrlpn = new JScrollPane();
	private final JTable tblParams = new CommonJTable();
	private final JTable rowindicatortbl = new RowIndicatorTable(null, tblParams);
	private ParameterModel parameterModel;
	private final JComboBox cmbbxTypes = new JComboBox(ParameterModel.adatatype);

	private final Action actNew = new NewParameterAction(CommonLocaleDelegate.getInstance().getMessage(
			"ParameterPanel.1","Neuer Parameter"), Icons.getInstance().getIconNew16());
	private final Action actDelete = new DeleteParameterAction(CommonLocaleDelegate.getInstance().getMessage(
			"ParameterPanel.2","Parameter l\u00f6schen"), Icons.getInstance().getIconDelete16());

	private final boolean blnWithValuelistProviderColumn;
	private final boolean blnWithParameterLabelColumn;
	
	public ParameterPanel(boolean blnWithValuelistProviderColumn,
			boolean blnWithParameterLabelColumn) {
		super(new BorderLayout());
		this.blnWithValuelistProviderColumn = blnWithValuelistProviderColumn;
		this.blnWithParameterLabelColumn = blnWithParameterLabelColumn;
		parameterModel = new ParameterModel(blnWithValuelistProviderColumn, blnWithParameterLabelColumn);
		this.init();
	}

	protected void init() {
		scrlpn.getViewport().setBackground(tblParams.getBackground());
		tblParams.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		scrlpn.getViewport().setView(tblParams);
		scrlpn.setRowHeaderView(rowindicatortbl);
		final JLabel labCorner = new JLabel();
		labCorner.setEnabled(false);
		labCorner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
		labCorner.setBackground(Color.LIGHT_GRAY);
		scrlpn.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, labCorner);

		tblParams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowindicatortbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// ensure that the model of the row indicator table is the same as that of the regular table.
		/** @todo move this to row indicator table? */
		tblParams.addPropertyChangeListener("model", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent ev) {
				rowindicatortbl.setModel(tblParams.getModel());
				rowindicatortbl.setSelectionModel(tblParams.getSelectionModel());
			}
		});

		tblParams.setModel(parameterModel);
		tblParams.setRowHeight(22);
		rowindicatortbl.setRowHeight(tblParams.getRowHeight());
		scrlpn.setViewportView(tblParams);
		this.add(scrlpn, BorderLayout.CENTER);
		final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		toolbar.add(actNew);
		toolbar.add(actDelete);
		this.add(toolbar, BorderLayout.NORTH);
		tblParams.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (tblParams.getSelectedRow() != -1) {
					actDelete.setEnabled(true);
				}
				else {
					actDelete.setEnabled(false);
				}
			}
		});
/*
		tblParams.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
			}
		});
*/
		DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField());
		cellEditor.setClickCountToStart(1);
		tblParams.getColumnModel().getColumn(ParameterModel.COLUMN_NAME).setCellEditor(cellEditor);
		if (blnWithParameterLabelColumn) {
			tblParams.getColumnModel().getColumn(ParameterModel.COLUMN_MESSAGE).setCellEditor(cellEditor);
		}
		cellEditor = new DefaultCellEditor(cmbbxTypes);
		cellEditor.setClickCountToStart(1);
		tblParams.getColumnModel().getColumn(ParameterModel.COLUMN_TYPE).setCellEditor(cellEditor);

		if (blnWithValuelistProviderColumn) {
			ValuelistproviderEditor editor = new ValuelistproviderEditor();
			tblParams.getColumnModel().getColumn(ParameterModel.COLUMN_VLP).setCellEditor(editor);
			tblParams.getColumnModel().getColumn(ParameterModel.COLUMN_VLP).setCellRenderer(editor);
		}

		actDelete.setEnabled(false);
	}

	public class NewParameterAction extends AbstractAction {

		public NewParameterAction(String name, Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			newParameter();
		}
	}

	public class DeleteParameterAction extends AbstractAction {

		public DeleteParameterAction(String name, Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			deleteParameter();
		}
	}

	public void newParameter() {
		parameterModel.addEntry(new DatasourceParameterVO(null, null, null, null));
		tblParams.getSelectionModel().setLeadSelectionIndex(parameterModel.getParameters().size() - 1);
	}

	public void deleteParameter() {
		final int iSelectedRow = tblParams.getSelectedRow();
		if (iSelectedRow > -1) {
			if (tblParams.isEditing()) {
				tblParams.getCellEditor().stopCellEditing();
			}
			parameterModel.removeEntry(tblParams.getSelectionModel().getLeadSelectionIndex());
			tblParams.getSelectionModel().setLeadSelectionIndex(iSelectedRow < parameterModel.getRowCount() ? iSelectedRow : iSelectedRow - 1);
		}
	}

	public void addParameter(Integer iDatasourceId, String sParameter, String sDatatype, String sDescription) {
		parameterModel.addEntry(new DatasourceParameterVO(iDatasourceId, sParameter, sDatatype, sDescription));
	}

	public ParameterModel getParameterModel() {
		return parameterModel;
	}

	public void setParameterModel(ParameterModel model) {
		this.parameterModel = model;
		init();
	}

	public Action getNewParameterAction(){
		return this.actNew;
	}
	public Action getDeleteParameterAction(){
		return this.actDelete;
	}

	public JTable getParameterTable() {
		return tblParams;
	}

	public void cancelEditing() {
		if (tblParams.isEditing() && tblParams.getRowCount() > 0) {
			tblParams.getCellEditor().cancelCellEditing();
		}
	}

	public boolean stopEditing() {
		boolean bResult = true;

		if (tblParams.isEditing() && tblParams.getRowCount() > 0) {
			bResult = tblParams.getCellEditor().stopCellEditing();
		}
		return bResult;
	}

	private class ValuelistproviderEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		private JLabel valuelistprovider = null;
		private DatasourceParameterValuelistproviderVO vo;

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			vo = (DatasourceParameterValuelistproviderVO) value;
			return getComponent(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			vo = (DatasourceParameterValuelistproviderVO) value;
			return getComponent(false);
		}

		private Component getComponent(boolean editable) {
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setBackground(Color.WHITE);
			panel.setLayout(new TableLayout(new double[][]{
					{
						InterfaceGuidelines.CELL_MARGIN_LEFT,
						TableLayout.FILL,
						TableLayout.PREFERRED,
						InterfaceGuidelines.MARGIN_RIGHT
					},
					{
						InterfaceGuidelines.CELL_MARGIN_TOP,
						TableLayout.PREFERRED,
						InterfaceGuidelines.CELL_MARGIN_BOTTOM
					}
			}));

			valuelistprovider = new JLabel();

			if (vo != null && !StringUtils.isNullOrEmpty(vo.getType())) {
				valuelistprovider.setText(vo.getType());
			}
			else {
				valuelistprovider.setText(CommonLocaleDelegate.getInstance().getMessage(
						"ParameterPanel.4", "Kein ValueListprovider definiert"));
			}

			TableLayoutConstraints constraint = new TableLayoutConstraints(1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER);
			panel.add(valuelistprovider, constraint);

			JButton launchEditor = new JButton("...");
			launchEditor.setPreferredSize(new Dimension(30, InterfaceGuidelines.CELL_BUTTON_MAXHEIGHT));
			launchEditor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					launchEditor();
 				}
			});
			constraint = new TableLayoutConstraints(2, 1);
			panel.add(launchEditor, constraint);
			return panel;
		}

		private final void launchEditor(){
			if (vo == null) {
				vo = new DatasourceParameterValuelistproviderVO(null);
			}
			WYSIWYGValuelistProvider vlp = new WYSIWYGValuelistProvider(vo.getType());

			for (Map.Entry<String, String> param : vo.getParameters().entrySet()) {
				vlp.addWYSIYWYGParameter(new WYSIYWYGParameter(param.getKey(), param.getValue()));
			}

			vlp = ValueListProviderEditor.showEditor(vlp);

			if (vlp != null && !StringUtils.isNullOrEmpty(vlp.getType())) {
				valuelistprovider.setText(vlp.getType());

				vo.setType(vlp.getType());

				Map<String, String> params = new HashMap<String, String>();
				for (WYSIYWYGParameter param : vlp.getAllWYSIYWYGParameter()) {
					params.put(param.getParameterName(), param.getParameterValue());
				}

				vo.setParameters(params);
				stopCellEditing();
			}
			else {
				valuelistprovider.setText(CommonLocaleDelegate.getInstance().getMessage("ParameterPanel.4", "Kein ValueListprovider definiert"));
				vo = null;
			}
		}

		@Override
		public Object getCellEditorValue() {
			return vo;
		}
	}

}	// class ParameterPanel

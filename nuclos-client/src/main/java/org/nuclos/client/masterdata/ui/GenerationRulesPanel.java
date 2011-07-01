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
package org.nuclos.client.masterdata.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.masterdata.SortableRuleTableModel;
import org.nuclos.client.ui.Icons;

/**
 * Panel for selection of rules for object generation; modified copy of TransitionRulePanel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class GenerationRulesPanel extends JPanel {
	private final JToolBar toolbar = new JToolBar();
	private final JButton btnAdd = new JButton();
	private final JButton btnDelete = new JButton();
	private final JButton btnUp = new JButton();
	private final JButton btnDown = new JButton();
	private final JTable tblRules = new JTable();
	private final JScrollPane scrlpn = new JScrollPane(tblRules);
	private final SortableRuleTableModel model = new SortableRuleTableModel();

	public GenerationRulesPanel() {
		super(new BorderLayout());
		this.init();
	}

	private void init() {
		btnAdd.setIcon(Icons.getInstance().getIconNew16());
		btnAdd.setToolTipText(CommonLocaleDelegate.getMessage("GenerationRulesPanel.1", "Neue Regel zuordnen"));
		btnAdd.setActionCommand("add");
		btnDelete.setIcon(Icons.getInstance().getIconDelete16());
		btnDelete.setToolTipText(CommonLocaleDelegate.getMessage("GenerationRulesPanel.2", "Zuordnung aufheben"));
		btnDelete.setActionCommand("remove");
		btnUp.setIcon(Icons.getInstance().getIconUp16());
		btnUp.setToolTipText(CommonLocaleDelegate.getMessage("GenerationRulesPanel.3", "Nach oben verschieben"));
		btnUp.setActionCommand("moveUp");
		btnDown.setIcon(Icons.getInstance().getIconDown16());
		btnDown.setToolTipText(CommonLocaleDelegate.getMessage("GenerationRulesPanel.4", "Nach unten verschieben"));
		btnDown.setActionCommand("moveDown");

		toolbar.setFloatable(false);
		toolbar.add(btnAdd, null);
		toolbar.add(btnDelete, null);
		toolbar.addSeparator();
		toolbar.add(btnDown);
		toolbar.add(btnUp);
		toolbar.addSeparator();
		this.add(toolbar, BorderLayout.NORTH);
		tblRules.setBorder(BorderFactory.createLoweredBevelBorder());
		tblRules.setModel(model);
		tblRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblRules.setRowMargin(4);
		tblRules.setRowHeight(18);
		tblRules.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tblRules.setIntercellSpacing(new Dimension(3, 3));
		this.add(scrlpn, BorderLayout.CENTER);
		
		tblRules.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JCheckBox result = new JCheckBox();
				if (value != null && value instanceof Boolean)
					result.setSelected((Boolean) value);
				else
					result.setSelected(false);
				return result;
			}
		});
		tblRules.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JCheckBox()));
	}

	public JToolBar getToolBar() {
		return toolbar;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public JButton getBtnDelete() {
		return btnDelete;
	}

	public JTable getTblRules() {
		return tblRules;
	}

	public SortableRuleTableModel getModel() {
		return model;
	}

	public JButton getBtnUp() {
		return btnUp;
	}

	public JButton getBtnDown() {
		return btnDown;
	}

}	// class GenerationRulesPanel

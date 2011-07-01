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
package org.nuclos.client.statemodel.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.models.SelectRuleTableModel;

/**
 * Panel for adding a rule to a transition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class InsertRulePanel extends JPanel {
	private final JTable tblRules = new JTable();

	public InsertRulePanel() {
		super(new BorderLayout());
		this.init();
	}

	public void setModel(SelectRuleTableModel model) {
		tblRules.setModel(model);
		tblRules.setRowSorter(new TableRowSorter<TableModel>(model));
		tblRules.getRowSorter().toggleSortOrder(0);
	}

	private void init() {
		tblRules.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblRules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final JScrollPane scrlpn = new JScrollPane();
		scrlpn.getViewport().add(tblRules);
		add(scrlpn, BorderLayout.CENTER);
	}

	public JTable getTblRules() {
		return tblRules;
	}

	public SortedRuleVO getRow(int index) {
		return ((SelectRuleTableModel) tblRules.getModel()).getRow(tblRules.convertRowIndexToModel(index));
	}
}

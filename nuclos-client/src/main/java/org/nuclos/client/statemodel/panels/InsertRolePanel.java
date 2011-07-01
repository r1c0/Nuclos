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

import org.nuclos.client.statemodel.models.SelectRoleTableModel;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Panel for adding a role to a transition.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class InsertRolePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTable tblRoles = new JTable();

	public InsertRolePanel() {
		super(new BorderLayout());
		this.init();
	}

	public void setModel(SelectRoleTableModel model) {
		tblRoles.setModel(model);
		tblRoles.setRowSorter(new TableRowSorter<TableModel>(model));
	}

	private void init() {
		tblRoles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblRoles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tblRoles);
		add(scrollPane, BorderLayout.CENTER);
	}

	public JTable getTblRoles() {
		return tblRoles;
	}

	public MasterDataVO getRow(int index) {
		return ((SelectRoleTableModel) tblRoles.getModel()).getRow(tblRoles.convertRowIndexToModel(index));
	}
}

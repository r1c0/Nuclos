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
package org.nuclos.client.genericobject.statehistory;

import java.awt.BorderLayout;
import java.text.DateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;

/**
 * A panel that displays the state history for a leased object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class StateHistoryPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JScrollPane scrlpn = new JScrollPane();
	final JTable tbl = new JTable();

	public StateHistoryPanel(List<StateHistoryVO> lstHistory) {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.add(this.scrlpn, BorderLayout.CENTER);

		this.scrlpn.getViewport().add(this.tbl);

		final TableModel model = new TableModel(lstHistory);

		this.tbl.setModel(model);

		this.tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * inner class TableModel
	 */
	static class TableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int COLUMN_STATE = 0;
		private static final int COLUMN_CREATEDAT = 1;
		private static final int COLUMN_CREATEDBY = 2;

		private List<StateHistoryVO> lstHistory;
		private static String[] asColumnNames = {
			CommonLocaleDelegate.getMessage("StateHistoryPanel.1", "Status"), 
			CommonLocaleDelegate.getMessage("StateHistoryPanel.2", "Datum"),
			CommonLocaleDelegate.getMessage("StateHistoryPanel.3", "Benutzer")};
		//private final DateFormat dateformat = DateFormat.getDateInstance();
		private final DateFormat dateformat = CommonLocaleDelegate.getDateTimeFormat(); // new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		public TableModel(List<StateHistoryVO> lstHistory) {
			this.lstHistory = lstHistory;
		}

		public StateHistoryVO getRow(int iRow) {
			return this.lstHistory.get(iRow);
		}

		@Override
		public String getColumnName(int iColumn) {
			return asColumnNames[iColumn];
		}

		@Override
		public int getColumnCount() {
			return asColumnNames.length;
		}

		@Override
		public int getRowCount() {
			return this.lstHistory.size();
		}

		@Override
		public Object getValueAt(int iRow, int iColumn) {
			Object result;
			StateHistoryVO shvo = this.lstHistory.get(iRow);
			switch (iColumn) {
				case COLUMN_STATE:
					result = shvo.getStateName();
					break;
				case COLUMN_CREATEDAT:
					result = dateformat.format(shvo.getCreatedAt());
					break;
				case COLUMN_CREATEDBY:
					result = shvo.getCreatedBy();
					break;
				default:
					throw new IllegalArgumentException("iColumn");
			}
			return result;
		}

	}	// inner class TableModel

}	// class StateHistoryPanel

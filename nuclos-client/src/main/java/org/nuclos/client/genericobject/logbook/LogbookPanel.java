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
package org.nuclos.client.genericobject.logbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;

/**
 * Panel for displaying the logbook.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class LogbookPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LogbookTableModel tblmdl;
	private final JToolBar toolbarDefault = UIUtils.createNonFloatableToolBar();
	private final JScrollPane scrlpn = new JScrollPane();
	final JTable tbl = new CommonJTable();
	private final AttributeCVO attrcvoHeader;

	public LogbookPanel(Collection<LogbookVO> colllogbookvo) {
		this.attrcvoHeader = getHeaderAttribute();

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.toolbarDefault.setFloatable(false);
		this.add(this.toolbarDefault, BorderLayout.NORTH);
		this.add(this.scrlpn, BorderLayout.CENTER);

		this.tbl.setAutoCreateRowSorter(true);
		this.tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.scrlpn.getViewport().add(this.tbl);

		this.refreshTableModel(colllogbookvo);

		this.tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	private void initiallySetColumnWidths() {
		for (int iColumn = 0; iColumn < tbl.getColumnCount(); iColumn++) {
			final TableColumn column = tbl.getColumnModel().getColumn(iColumn);
			final int iPreferredCellWidth = LogbookTableModel.getPreferredColumnWidth(iColumn);
			column.setPreferredWidth(iPreferredCellWidth);
			column.setWidth(iPreferredCellWidth);
		}
		tbl.revalidate();
	}

	public JToolBar getToolbar() {
		return this.toolbarDefault;
	}
	
	public LogbookTableModel getLogbookTableModel() {
		return this.tblmdl;
	}
	
	private static AttributeCVO getHeaderAttribute() {
		AttributeCVO result;
		try {
			result = AttributeCache.getInstance().getAttribute(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
		}
		catch (Exception ex) {
			result = null;
		}
		return result;
	}

	public void refreshTableModel(Collection<LogbookVO> colllogbookvo) {
		if (colllogbookvo != null) {
			this.tblmdl = new LogbookTableModel(colllogbookvo, attrcvoHeader);
			this.tbl.setModel(tblmdl);
			this.tbl.getColumn(CommonLocaleDelegate.getMessage("LogbookController.8", "Feld")).setCellRenderer(new LogbookRenderer());
			this.tbl.getRowSorter().setSortKeys(Arrays.asList(
				new SortKey(LogbookTableModel.COLUMN_CHANGEDAT, SortOrder.DESCENDING),
				new SortKey(LogbookTableModel.COLUMN_LABEL, SortOrder.ASCENDING)));
			initiallySetColumnWidths();
		}
	}

	private class LogbookRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			final JLabel result = (JLabel) super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			final String sText = (String) oValue;
			result.setText(sText);
			if (!bSelected) {
				if (attrcvoHeader != null && sText.equals(CommonLocaleDelegate.getLabelFromAttributeCVO(attrcvoHeader))) {
					result.setBackground(new Color(200, 200, 200));
				}
				else {
					result.setBackground(tbl.getBackground());
				}
			}
			return result;
		}
	}

}	// class LogbookPanel

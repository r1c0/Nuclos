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
package org.nuclos.client.genericobject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.JXPanel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;

/**
 * Panel for selection of possible output formats for a given report
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ReportSelectionPanel extends JXPanel {

	protected class ReportSelectionTableModel extends AbstractTableModel {

		protected final String[] captions = {
			SpringLocaleDelegate.getInstance().getMessage("ReportSelectionPanel.1", "Formular"),
			SpringLocaleDelegate.getInstance().getMessage("R00011618", "Vorlage"),
			SpringLocaleDelegate.getInstance().getMessage("ReportSelectionPanel.2", "Format"), 
			SpringLocaleDelegate.getInstance().getMessage("ReportSelectionPanel.3", "Ausgabemedium")};
		
		protected ArrayList<ReportEntry> lstReports = new ArrayList<ReportEntry>();

		@Override
		public int getColumnCount() {
			return captions.length;
		}

		@Override
		public int getRowCount() {
			return lstReports.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			ReportEntry entry = lstReports.get(rowIndex);
			if (entry != null) {
				switch (columnIndex) {
					case 0:
						return entry.getReport().getName();
					case 1:
						return entry.getOutput() != null ? entry.getOutput().getDescription() : "";
					case 2:
						return entry.getOutput() != null ? entry.getOutput().getFormat() : "";
					case 3:
						String sResult = "";
						if (entry.getOutput() != null) {
							sResult += SpringLocaleDelegate.getInstance().getText(entry.getOutput().getDestination());
							if (entry.getOutput().getParameter() != null &&
									entry.getOutput().getParameter().length() > 0) {
								sResult += (" (" + entry.getOutput().getParameter() + ")");
							}
						}
						return sResult;
				}
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return captions[column];
		}

		public void add(ReportEntry entry) {
			lstReports.add(entry);
			fireTableDataChanged();
		}

		public ReportEntry getEntry(int iRow) {
			ReportEntry result = null;

			if (iRow < lstReports.size()) {
				result = lstReports.get(iRow);
			}
			return result;
		}
	}

	public class ReportEntry {
		protected ReportVO report;
		protected ReportOutputVO output;

		public ReportEntry(ReportVO report, ReportOutputVO output) {
			this.report = report;
			this.output = output;
		}

		public ReportOutputVO getOutput() {
			return output;
		}

		public void setOutput(ReportOutputVO output) {
			this.output = output;
		}

		public ReportVO getReport() {
			return report;
		}

		public void setReport(ReportVO report) {
			this.report = report;
		}
	}

	private ReportSelectionTableModel model = new ReportSelectionTableModel();
	private JTable tblReports = new JTable(model);
	private JScrollPane scrReports = new JScrollPane(tblReports);
	private JCheckBox cbAttachReport = new JCheckBox(SpringLocaleDelegate.getInstance().getMessage(
			"ReportSelectionPanel.4", "Dokument anh\u00e4ngen"));

	public ReportSelectionPanel() {
		this(false);
	}

	public ReportSelectionPanel(boolean bShowAttachReport) {
		super(new BorderLayout());
		init(bShowAttachReport);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		Dimension dimTable = tblReports.getPreferredSize();
		result.width = Math.max(dimTable.width, result.width);
		result.width = Math.max(800, result.width);

		return result;
	}

	private void init(boolean bShowAttachReport) {
		scrReports.setPreferredSize(new Dimension(500, 150));
		this.setScrollableTracksViewportWidth(true);
		this.setScrollableTracksViewportHeight(true);
		add(scrReports, BorderLayout.CENTER);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		tblReports.getColumnModel().getColumn(1).setCellRenderer(renderer);
		tblReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cbAttachReport.setSelected(false);
		if (bShowAttachReport) {
			add(cbAttachReport, BorderLayout.SOUTH);
			cbAttachReport.setSelected(true); // as default
		}
		tblReports.setAutoCreateRowSorter(true);
	}
	
	public JTable getReportsTable() {
		return tblReports;
	}
	
	public void addDoubleClickListener(MouseListener l)
	{
		tblReports.addMouseListener(l);
	}
	
	public void addReport(ReportVO reportVO, ReportOutputVO formatVO) {
		ReportEntry entry = new ReportEntry(reportVO, formatVO);
		if (entry != null) {
			model.add(entry);
		}
	}

	public ReportEntry getSelectedReport() {
		ReportEntry result = null;
		if (tblReports.getSelectedRow() >= 0) {
			result = model.getEntry(tblReports.getSelectedRow());
		}
		return result;
	}

	public void selectFirstReport() {
		tblReports.setRowSelectionInterval(0, 0);
	}

	public void setPreferredColumnWidth(int iMaxRowsToConsider, int iInsets) {
		if (tblReports != null) {
			TableUtils.setPreferredColumnWidth(tblReports, iMaxRowsToConsider, iInsets);
		}
	}

	public boolean getAttachReport() {
		return cbAttachReport.isSelected();
	}
}

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
package org.nuclos.client.report.reportrunner;

import org.nuclos.common2.CommonLocaleDelegate;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import org.nuclos.client.report.reportrunner.BackgroundProcessInfo.Status;
import org.nuclos.client.ui.Icons;

/**
 * Status Panel for all executed reports and forms.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class BackgroundProcessStatusPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final BackgroundProcessStatusTableModel tblmodel = new BackgroundProcessStatusTableModel();

	private final JTable tblStatus = new JTable(tblmodel);

	final JButton btnClear = new JButton(CommonLocaleDelegate.getMessage("BackgroundProcessStatusPanel.4","Zur\u00fccksetzen"));
	final JButton btnStopProcess = new JButton(CommonLocaleDelegate.getMessage("BackgroundProcessStatusPanel.2","Prozess beenden"));

	public BackgroundProcessStatusPanel() {
		super(new BorderLayout());
		this.init();

		/** @todo this belongs in a controller, but we have to refactor some things here first... */
		this.btnClear.addActionListener(new ActionListenerClear());
	}

	private void init() {
		final JPanel pnlButtons = new JPanel();

		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

		final JScrollPane scrlpn = new JScrollPane(this.tblStatus);
		scrlpn.getViewport().setBackground(this.tblStatus.getBackground());
		this.add(scrlpn, BorderLayout.CENTER);
		this.add(pnlButtons, BorderLayout.SOUTH);

		btnStopProcess.setToolTipText(CommonLocaleDelegate.getMessage("BackgroundProcessStatusPanel.3","Prozesse beenden"));
		btnStopProcess.setEnabled(false);
		pnlButtons.add(btnStopProcess);
		btnClear.setToolTipText(CommonLocaleDelegate.getMessage("BackgroundProcessStatusPanel.1","Beendete Prozesse aus der Liste entfernen"));
		btnClear.setEnabled(false);
		pnlButtons.add(btnClear);

		final TableColumn column0 = this.tblStatus.getColumnModel().getColumn(0);
		final int iColumn0Width = 40;
		column0.setPreferredWidth(iColumn0Width);
		column0.setMaxWidth(iColumn0Width);
		column0.setCellRenderer(new StatusRenderer());

		final TableColumn column2 = tblStatus.getColumnModel().getColumn(2);
		final int iColumn2Width = 80;
		column2.setPreferredWidth(iColumn2Width);
		column2.setMaxWidth(iColumn2Width);
	}

	public BackgroundProcessStatusTableModel getModel() {
		return this.tblmodel;
	}

	void repaintTable() {
		this.tblStatus.repaint();
	}

	private static class StatusRenderer implements TableCellRenderer {
		private final Logger log = Logger.getLogger(this.getClass());

		private final Icon iconRun = Icons.getInstance().getIconJobRunning();
		private final Icon iconOK = Icons.getInstance().getIconJobSuccessfulAlt();
		private final Icon iconError = Icons.getInstance().getIconJobError();

		private final JLabel iconLabel = new JLabel();

		StatusRenderer() {
			this.iconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			this.iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object oValue, boolean isSelected, boolean hasFocus, int iRow, int iColumn) {
			final Status status = (Status) oValue;
			final Icon icon;
			switch (status) {
				case RUNNING:
					icon = this.iconRun;
					break;
				case DONE:
					icon = this.iconOK;
					break;
				case CANCELLED:
					icon = this.iconError;
					break;
				case ERROR:
					icon = this.iconError;
					break;
				default:
					icon = null;
					log.error("Invalid status for ReportRunner.");
			}
			this.iconLabel.setIcon(icon);

			return this.iconLabel;
		}
	}	// inner class StatusRenderer

	private class ActionListenerClear implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ev) {
			this.clear();
		}

		private void clear() {
			synchronized (tblmodel) {
				for (int iRow = tblmodel.getRowCount() - 1; iRow >= 0; --iRow) {
					final BackgroundProcessInfo bpi = tblmodel.getRow(iRow);
					if (bpi != null && isFinished(bpi.getStatus())) {
						tblmodel.removeEntry(iRow);
					}
				}
			}
		}

		private boolean isFinished(Status status) {
			return (status != null) && status.isFinished();
		}

	}	// inner class ActionListenerClear

	public JButton getButtonStopProcess() {
		return btnStopProcess;
	}

	public JButton getButtonClear() {
		return btnClear;
	}
	
	public JTable getProcessTable() {
		return tblStatus;
	}

}	// class BackgroundProcessStatusPanel

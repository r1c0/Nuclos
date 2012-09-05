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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Controller for BackgroundProcessStatusPanel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class BackgroundProcessStatusController {
	//private final static Logger log = Logger.getLogger(BackgroundProcessStatusController.class);

	private static BackgroundProcessStatusDialog frmStatus = null;

	/** @todo let's hope this method gets always called with the same frame */
	public static synchronized BackgroundProcessStatusDialog getStatusDialog(Frame frame) {
		if (frmStatus == null) {
			frmStatus = new BackgroundProcessStatusDialog(frame);
			init();
		}
		return frmStatus;
	}

	public BackgroundProcessStatusController() {
	}

	public synchronized void run(JFrame frame) {
		getStatusDialog(frame).setVisible(true);
	}
	
	private static void init(){
		frmStatus.getStatusPanel().getButtonStopProcess().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdStopProcess();
			}
		});
		frmStatus.getStatusPanel().getProcessTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				cmdCheckButtonStopProcess();
			}

		});
		frmStatus.getStatusPanel().getModel().addTableModelListener(new TableModelListener () {
			@Override
			public void tableChanged(TableModelEvent e) {
				frmStatus.getStatusPanel().getButtonClear().setEnabled(((BackgroundProcessStatusTableModel)e.getSource()).hasFinishedProcess());
				cmdCheckButtonStopProcess();
				frmStatus.getStatusPanel().repaintTable();
		    }
		});
	}
	
	private static void cmdCheckButtonStopProcess() {
		BackgroundProcessInfo bpInfo = getSelectedProcessFromTableModel();
		if(bpInfo != null){
			synchronized (bpInfo) {
				frmStatus.getStatusPanel().getButtonStopProcess().setEnabled(!bpInfo.getStatus().isFinished());
			}
		}
	}

	private static void cmdStopProcess(){
		BackgroundProcessInfo bpInfo = getSelectedProcessFromTableModel();
		if(bpInfo != null){
			bpInfo.cancelProzess();
		}
	}

	private static BackgroundProcessInfo getSelectedProcessFromTableModel() {
		final int iSelectedRow = frmStatus.getStatusPanel().getProcessTable().getSelectedRow();
		return (iSelectedRow == -1) ? null : frmStatus.getStatusPanel().getModel().getRow(iSelectedRow);
	}	
}

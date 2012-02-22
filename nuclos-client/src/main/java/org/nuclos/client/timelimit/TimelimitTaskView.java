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
package org.nuclos.client.timelimit;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import org.nuclos.client.task.TaskView;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.TableUtils;


/**
 * View on a time limit task list.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */

public class TimelimitTaskView extends TaskView {

	private final JToolBar toolbar = UIUtils.createNonFloatableToolBar();
	private final JButton btnRefresh = new JButton();
	private final JMenuItem btnPerform = new JMenuItem();
	private final JMenuItem btnRemove = new JMenuItem();
	private final JMenuItem btnPrint = new JMenuItem();
	private final JToggleButton btnFinish = new JToggleButton() {

		/**
		 * JToggleButton doesn't respect the "hideActionText" client property.
		 * @param a
		 */
		@Override
		public void setAction(Action a) {
			super.setAction(a);
			this.setText(null);
		}
	};

	final JCheckBoxMenuItem btnShowAllTasks = new JCheckBoxMenuItem();

	private final JScrollPane scrlpn = new JScrollPane();
	private final JTable tblTasks = new CommonJTable();
	
	private final PopupButton popupExtras = new PopupButton(getSpringLocaleDelegate().getMessage(
			"PopupButton.Extras","Extras"));

	public TimelimitTaskView() {
		this.init();
	} // ctor

	private void init() {
		this.setLayout(new BorderLayout());
		this.add(this.toolbar, BorderLayout.NORTH);
		this.add(this.scrlpn, BorderLayout.CENTER);

		this.toolbar.add(btnRefresh);
		this.toolbar.add(btnFinish);
		
		this.popupExtras.add(btnPerform);
		this.popupExtras.add(btnPrint);
		this.popupExtras.add(btnRemove);
		this.popupExtras.addSeparator();
		this.popupExtras.add(btnShowAllTasks);
		
		super.addRefreshIntervalsToPopupButton(popupExtras);
		this.toolbar.add(popupExtras);

		// don't show text, only the icon:
		btnRefresh.putClientProperty("hideActionText", Boolean.TRUE);
		btnFinish.putClientProperty("hideActionText", Boolean.TRUE);

		this.btnShowAllTasks.setText(getSpringLocaleDelegate().getMessage(
				"TimelimitTaskView.1","Alle Fristen"));
		this.btnShowAllTasks.setToolTipText(getSpringLocaleDelegate().getMessage(
				"TimelimitTaskView.2","Auch erledigte Fristen anzeigen"));

		this.scrlpn.getViewport().setView(tblTasks);
		this.tblTasks.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.tblTasks.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		this.scrlpn.setBackground(Color.WHITE);
		this.tblTasks.setBackground(Color.WHITE);
		UIUtils.setupCopyAction(this.tblTasks);
	}

	/**
	 * @return the table containing time limit tasks
	 */
	JTable getTable() {
		return tblTasks;
	}
	
	JButton getRefreshButton() {
		return btnRefresh;
	}
	
	JMenuItem getPerformMenuItem() {
		return btnPerform;
	}
	
	JMenuItem getPrintMenuItem() {
		return btnPrint;
	}
	
	JMenuItem getRemoveMenuItem() {
		return btnRemove;
	}
	
	JToggleButton getFinishButton() {
		return btnFinish;
	}

	/**
	 * sets the model for this view
	 * @param model
	 */
	public void setTimelimitTaskTableModel(TimelimitTaskTableModel model) {
		this.tblTasks.setModel(model);
		TableUtils.addMouseListenerForSortingToTableHeader(this.tblTasks, model);
	}

	/**
	 * @return the model for this view
	 */
	public TimelimitTaskTableModel getTimelimitTaskTableModel() {
		return (TimelimitTaskTableModel) this.tblTasks.getModel();
	}

}	// class TimelimitTaskView

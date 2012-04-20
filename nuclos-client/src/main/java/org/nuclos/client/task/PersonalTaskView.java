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
package org.nuclos.client.task;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.TableUtils;

/**
 * View on a personal task list.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class PersonalTaskView extends TaskView {

	private final JButton btnNew = new JButton();
	private final JMenuItem btnEdit = new JMenuItem();
	private final JMenuItem btnPerform = new JMenuItem();
	private final JMenuItem btnRemove = new JMenuItem();
	private final JMenuItem btnPrint = new JMenuItem();

	private final JToggleButton btnComplete = new JToggleButton() {

		// JToggleButton doesn't respect the "hideActionText" client property.
		@Override
		public void setAction(Action a) {
			super.setAction(a);
			this.setText(null);
		}
	};
	
	final ButtonGroup bgPriority = new ButtonGroup();
	final JRadioButtonMenuItem[] rbPrio = new JRadioButtonMenuItem[] {
		new JRadioButtonMenuItem(getSpringLocaleDelegate().getMessage("PersonalTaskView.1","<Alle>")),
		new JRadioButtonMenuItem("1"),
		new JRadioButtonMenuItem("2"),
		new JRadioButtonMenuItem("3"),
		new JRadioButtonMenuItem("4"),
		new JRadioButtonMenuItem("5")
	};
	
	final ButtonGroup bgShowTasks = new ButtonGroup();
	final JRadioButtonMenuItem[] rbShowTask = new JRadioButtonMenuItem[] {
		new JRadioButtonMenuItem(getSpringLocaleDelegate().getMessage("PersonalTaskView.8","Eigene Aufgaben")),
		new JRadioButtonMenuItem(getSpringLocaleDelegate().getMessage("PersonalTaskView.6","Delegierte Aufgaben")),
		new JRadioButtonMenuItem(getSpringLocaleDelegate().getMessage("PersonalTaskView.3","Eigene und delegierte Aufgaben"))
	};
	
	final JCheckBoxMenuItem ckbShowCompletedTasks = new JCheckBoxMenuItem();

	private final JTable tblTasks = new CommonJTable();

	@Override
	public void init() {
		super.init();
		this.tblTasks.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.tblTasks.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.tblTasks.setBackground(Color.WHITE);
		this.tblTasks.setColumnSelectionAllowed(true);
		this.tblTasks.setRowHeight(SubForm.MIN_ROWHEIGHT);
		this.tblTasks.setTableHeader(new TaskViewTableHeader(tblTasks.getColumnModel()));
		UIUtils.setupCopyAction(this.tblTasks);
	}

	@Override
	public JTable getTable() {
		return tblTasks;
	}
	
	JToggleButton getCompleteButton() {
		return btnComplete;
	}
	
	JMenuItem getEditMenuItem() {
		return btnEdit;
	}
	
	JButton getNewButton() {
		return btnNew;
	}
	
	JMenuItem getPrintMenuItem() {
		return btnPrint;
	}
	
	JMenuItem getRemoveMenuItem() {
		return btnRemove;
	}
	
	JMenuItem getPerformMenuItem() {
		return btnPerform;
	}

	public void setPersonalTaskTableModel(PersonalTaskTableModel model) {
		this.tblTasks.setModel(model);
		TableUtils.addMouseListenerForSortingToTableHeader(this.tblTasks, model);
	}

	public PersonalTaskTableModel getPersonalTaskTableModel() {
		return (PersonalTaskTableModel) this.tblTasks.getModel();
	}

	@Override
	protected List<JComponent> getToolbarComponents() {
		List<JComponent> result = new ArrayList<JComponent>();
		btnNew.putClientProperty("hideActionText", Boolean.TRUE);
		btnComplete.putClientProperty("hideActionText", Boolean.TRUE);
		result.add(btnNew);
		result.add(btnComplete);
		return result;
	}

	@Override
	protected List<JComponent> getExtrasMenuComponents() {
		List<JComponent> result = new ArrayList<JComponent>();
		this.ckbShowCompletedTasks.setText(getSpringLocaleDelegate().getMessage(
				"PersonalTaskView.9", "Auch erledigte anzeigen"));
		this.ckbShowCompletedTasks.setToolTipText(getSpringLocaleDelegate().getMessage(
				"PersonalTaskView.10", "Erledigte und nicht erledigte Aufgaben anzeigen"));
		result.add(btnPerform);
		result.add(btnEdit);
		result.add(btnPrint);
		result.add(btnRemove);
		result.add(ckbShowCompletedTasks);
		result.add(new JPopupMenu.Separator());
		result.add(new JLabel("<html><b>"+getSpringLocaleDelegate().getMessage(
				"PersonalTaskView.7","Filter nach Priorit\u00e4t")+"</b></html>"));
		for (int i = 0; i < rbPrio.length; i++) {
			this.bgPriority.add(rbPrio[i]);
			result.add(rbPrio[i]);
		}
		result.add(new JPopupMenu.Separator());
		result.add(new JLabel("<html><b>"+getSpringLocaleDelegate().getMessage(
				"PersonalTaskView.5","Aufgaben anzeigen")+"</b></html>"));
		for (int i = 0; i < rbShowTask.length; i++) {
			this.bgShowTasks.add(rbShowTask[i]);
			result.add(rbShowTask[i]);
		}
		return result;
	}
}

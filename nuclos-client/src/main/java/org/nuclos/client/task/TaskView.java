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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.SpringLocaleDelegate;

public abstract class TaskView extends JPanel implements ScheduledRefreshable {
	
	private final JToolBar toolbar = UIUtils.createNonFloatableToolBar();
	
	private final PopupButton popupExtras;
	
	private final JScrollPane scrlpn = new JScrollPane();

	private final JButton btnRefresh = new JButton();
	
	public final JTextField tfStatusBar = new StatusBarTextField(" ");
	
	private final int[] intervals = new int[] {	0, 5, 10, 30};
	private final String[] intervalLabels;
	private final String[] intervalDescs;
	
	private final ButtonGroup bgRefreshInterval = new ButtonGroup();
	private final JRadioButtonMenuItem[] rbRefresIntervals = new JRadioButtonMenuItem[intervals.length];

	private int refreshInterval = 0;
	
	// former Spring injection
	
	private SpringLocaleDelegate localeDelegate;
	
	// end of former Spring injection
	
	public TaskView() {
		setSpringLocaleDelegate(SpringApplicationContextHolder.getBean(SpringLocaleDelegate.class));
		
		popupExtras = new PopupButton(getSpringLocaleDelegate().getMessage("PopupButton.Extras","Extras"));
		intervalLabels = new String[] {
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.0.name","Manuell aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.5.name","Alle 5 Minuten aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.10.name","Alle 10 Minuten aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.30.name","Alle 30 Minuten aktualisieren")};
		intervalDescs = new String[] {
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.0.desc","Manuell aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.5.desc","Alle 5 Minuten aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.10.desc","Alle 10 Minuten aktualisieren"),
				getSpringLocaleDelegate().getMessage("TaskController.Refresh.30.desc","Alle 30 Minuten aktualisieren")};
		
		for (int i = 0; i < intervals.length; i++) {
			JRadioButtonMenuItem rb = new JRadioButtonMenuItem();
			rb.setToolTipText(intervalDescs[i]);
			bgRefreshInterval.add(rb);
			rbRefresIntervals[i] = rb;
		}	
	}
	
	public void init() {
		
		this.setLayout(new BorderLayout());
		this.add(toolbar, BorderLayout.NORTH);
		this.add(scrlpn, BorderLayout.CENTER);
		this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);
		this.tfStatusBar.setMinimumSize(new Dimension(0, this.tfStatusBar.getPreferredSize().height));

		toolbar.add(btnRefresh);
		btnRefresh.setToolTipText(getSpringLocaleDelegate().getMessage(
				"PersonalTaskController.3","Aufgabenliste aktualisieren"));
		
		List<JComponent> tbc = getToolbarComponents();
		if (tbc != null) {
			for (JComponent c : tbc) {
				toolbar.add(c);
			}
		}
		
		List<JComponent> emc = getExtrasMenuComponents();
		if (emc != null) {
			for (JComponent c : emc) {
				popupExtras.add(c);
			}
		}
		
		addRefreshIntervalsToPopupButton(popupExtras);
		this.toolbar.add(popupExtras);

		scrlpn.getViewport().add(getTable(), null);
		getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrlpn.setBackground(Color.WHITE);
		getTable().setBackground(Color.WHITE);
		UIUtils.setupCopyAction(getTable());
	}
	
	final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	protected final SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}
	
	protected void addRefreshIntervalsToPopupButton(PopupButton pb) {
		pb.addSeparator();
		pb.add(new JLabel("<html><b>"+getSpringLocaleDelegate().getMessage("TaskView.intervalRefresh","Intervall Aktualisierung")+"</b></html>"));
		for (int i = 0; i < rbRefresIntervals.length; i++) {
			pb.add(rbRefresIntervals[i]);
		}
	}
	
	@Override
	public void setRefreshInterval(int min){
		this.refreshInterval = min;
		for (int i = 0; i < intervals.length; i++) {
			if (intervals[i] == min) {
				rbRefresIntervals[i].setSelected(true);
				bgRefreshInterval.setSelected(rbRefresIntervals[i].getModel(), true);
			} else {
				rbRefresIntervals[i].setSelected(false);
				bgRefreshInterval.setSelected(rbRefresIntervals[i].getModel(), false);
			}
		}
	}
	
	@Override
	public int getRefreshInterval(){
		return this.refreshInterval;
	}

	@Override
	public int[] getRefreshIntervals() {
		return intervals;
	}

	@Override
	public String[] getRefreshIntervalLabels() {
		return intervalLabels;
	}

	@Override
	public JRadioButtonMenuItem[] getRefreshIntervalRadioButtons() {
		return rbRefresIntervals;
	}
	
	public JButton getRefreshButton() {
		return btnRefresh;
	}
	
	protected abstract List<JComponent> getToolbarComponents();
	
	protected abstract List<JComponent> getExtrasMenuComponents();
	
	protected abstract JTable getTable();
}

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

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.nuclos.client.ui.PopupButton;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction=true)
public abstract class TaskView extends JPanel implements ScheduledRefreshable {
	
	private final int[] intervals = new int[] {	0, 5, 10, 30};
	private final String[] intervalLabels = new String[] {
		getSpringLocaleDelegate().getMessage("TaskController.Refresh.0.name","Manuell aktualisieren"),
		getSpringLocaleDelegate().getMessage("TaskController.Refresh.5.name","Alle 5 Minuten aktualisieren"),
		getSpringLocaleDelegate().getMessage("TaskController.Refresh.10.name","Alle 10 Minuten aktualisieren"),
		getSpringLocaleDelegate().getMessage("TaskController.Refresh.30.name","Alle 30 Minuten aktualisieren")
	};
	private final String[] intervalDescs = new String[] {
			getSpringLocaleDelegate().getMessage("TaskController.Refresh.0.desc","Manuell aktualisieren"),
			getSpringLocaleDelegate().getMessage("TaskController.Refresh.5.desc","Alle 5 Minuten aktualisieren"),
			getSpringLocaleDelegate().getMessage("TaskController.Refresh.10.desc","Alle 10 Minuten aktualisieren"),
			getSpringLocaleDelegate().getMessage("TaskController.Refresh.30.desc","Alle 30 Minuten aktualisieren")
	};
	
	private final ButtonGroup bgRefreshInterval = new ButtonGroup();
	private final JRadioButtonMenuItem[] rbRefresIntervals = new JRadioButtonMenuItem[intervals.length];

	private int refreshInterval = 0;
	
	private SpringLocaleDelegate localeDelegate;
	
	public TaskView() {
		for (int i = 0; i < intervals.length; i++) {
			JRadioButtonMenuItem rb = new JRadioButtonMenuItem();
			rb.setToolTipText(intervalDescs[i]);
			bgRefreshInterval.add(rb);
			rbRefresIntervals[i] = rb;
		}
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
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
	
}

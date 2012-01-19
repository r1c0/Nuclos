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


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

/**
 * <code>TableModel</code> for <code>BackgroundProcessStatusPanel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */

public class BackgroundProcessStatusTableModel extends AbstractTableModel implements Observer {

	private static final String[] captions = {CommonLocaleDelegate.getMessage("BackgroundProcessStatusTableModel.4","Status"), CommonLocaleDelegate.getMessage("BackgroundProcessStatusTableModel.1","Hintergrundprozess"), CommonLocaleDelegate.getMessage("BackgroundProcessStatusTableModel.3","Startzeit"), CommonLocaleDelegate.getMessage("BackgroundProcessStatusTableModel.2","Meldung")};

	private final List<BackgroundProcessInfo> lstProcesses = new ArrayList<BackgroundProcessInfo>();

	public boolean hasFinishedProcess(){
		synchronized (this) {
			for(BackgroundProcessInfo info : lstProcesses) {
				if(info.getStatus().isFinished()){
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public int getColumnCount() {
		return captions.length;
	}

	@Override
	public String getColumnName(int column) {
		return captions[column];
	}

	@Override
	public int getRowCount() {
		return lstProcesses.size();
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		Object result = null;

		final BackgroundProcessInfo bpi = getRow(iRow);
		if (bpi != null) {
			switch (iColumn) {
				case 0:
					result = bpi.getStatus();
					break;
				case 1:
					result = bpi.getJobName();
					break;
				case 2:
					if (bpi.getStartedAt() != null) {
						final DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());
						result = format.format(bpi.getStartedAt());
					}
					break;
				case 3:
					result = bpi.getMessage();
					break;
			}
		}
		return result;
	}

	BackgroundProcessInfo getRow(int rowIndex) {
		return (lstProcesses.size() > rowIndex) ? lstProcesses.get(rowIndex) : null;
	}

	public void addEntry(BackgroundProcessInfo entry) {
		Observable statusObservable = new Observable(){
			@Override
			public void notifyObservers(){
				this.setChanged();
				super.notifyObservers();
			}
		};
		statusObservable.addObserver(this);
		entry.addObservable(statusObservable);
		lstProcesses.add(entry);
		fireTableDataChanged();
	}

	@Override
	public void update(Observable o, Object obj) {
		fireTableDataChanged();
	}
	
	public void removeEntry(int iIndex) {
		lstProcesses.remove(iIndex);
		fireTableDataChanged();
	}
	
}	// class BackgroundProcessStatusTableModel

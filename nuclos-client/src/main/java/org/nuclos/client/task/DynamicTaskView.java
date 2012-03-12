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

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.server.report.valueobject.DynamicTasklistVO;

public class DynamicTaskView extends TaskView {
	
	private final DynamicTasklistVO dynamicTasklist;
	
	private final JTable tbl = new CommonJTable();

	public DynamicTaskView(DynamicTasklistVO dynamicTasklist) {
		this.dynamicTasklist = dynamicTasklist;
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	public DynamicTasklistVO getDynamicTasklist() {
		return dynamicTasklist;
	}

	@Override
	protected List<JComponent> getToolbarComponents() {
		return null;
	}

	@Override
	protected List<JComponent> getExtrasMenuComponents() {
		return null;
	}

	@Override
	protected JTable getTable() {
		return tbl;
	}

}

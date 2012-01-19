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
package org.nuclos.client.datasource.querybuilder.gui;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.client.datasource.querybuilder.shapes.gui.ConstraintColumn;
import org.nuclos.common.database.query.definition.Table;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ColumnEntry {

	public static final int CONDITION_COUNT = 10;
	public static final int ROW_COUNT = CONDITION_COUNT + 6;

	public static class ConditionEntry {
		private String sCondition = "";

		public ConditionEntry() {
		}

		public String getCondition() {
			return sCondition;
		}

		public void setCondition(String sCondition) {
			this.sCondition = sCondition;
		}
	}

	private Table table;
	private ConstraintColumn column;
	private String sAlias;
	private boolean bVisible;
	private List<ConditionEntry> conditions = new ArrayList<ConditionEntry>();
	private String sOrderBy;
	private String sGroupBy;
	private int iDefaultWidth = 60;

	public ColumnEntry() {
		for (int i = 0; i < CONDITION_COUNT; i++) {
			conditions.add(new ConditionEntry());
		}
	}

	public boolean isVisible() {
		return bVisible;
	}

	public void setVisible(boolean bVisible) {
		this.bVisible = bVisible;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public ConstraintColumn getColumn() {
		return column;
	}

	public void setColumn(ConstraintColumn column) {
		this.column = column;
	}

	public String getGroupBy() {
		return sGroupBy;
	}

	public void setGroupBy(String sGroupBy) {
		this.sGroupBy = sGroupBy;
	}

	public String getOrderBy() {
		return sOrderBy;
	}

	public void setOrderBy(String sOrderBy) {
		this.sOrderBy = sOrderBy;
	}

	public String getCondition(int index) {
		return conditions.get(index).getCondition();
	}

	public void setCondition(int index, String sValue) {
		conditions.get(index).setCondition(sValue);
	}

	public int getNextAvailableCondition() {
		for (int i = 0; i < CONDITION_COUNT; i++) {
			if (conditions.get(i).getCondition().length() == 0) {
				return i;
			}
		}
		return -1;
	}

	public String getAlias() {
		return sAlias;
	}

	public void setAlias(String sAlias) {
		this.sAlias = sAlias;
	}

	public void reset() {
		table = null;
		column = null;
		bVisible = false;
		sAlias = null;
		conditions.clear();
		for (int i = 0; i < CONDITION_COUNT; i++) {
			conditions.add(new ConditionEntry());
		}
		sOrderBy = null;
		sGroupBy = null;
	}

	public List<ConditionEntry> getConditions() {
		return conditions;
	}

	public void addCondition(String value) {
		final ConditionEntry entry = conditions.get(getNextAvailableCondition());
		entry.setCondition(value);
	}

	public int getDefaultWidth() {
		return iDefaultWidth;
	}

	public void setDefaultWidth(int iDefaultWidth) {
		this.iDefaultWidth = iDefaultWidth;
	}
	
	

}	// class ColumnEntry

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

package org.nuclos.client.ui.util;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nuclos.client.wizard.util.MoreOptionPanel;
import org.nuclos.common2.SpringLocaleDelegate;

public class TableLayoutBuilder implements TableLayoutConstants {

	TableLayoutBuilder parent;
	JPanel panel;
	TableLayout tableLayout;
	Component lastComponent;
	double[] columnSpec;
	int row = -1;
	int column = 0;
	
	public TableLayoutBuilder() {
		this(new JPanel());
	}

	public TableLayoutBuilder(JPanel panel) {
		this(null, panel);
	}
	
	public TableLayoutBuilder(TableLayoutBuilder parent, JPanel panel) {
		this.parent = parent;
		this.tableLayout = new TableLayout();
		this.panel = panel;
		panel.setLayout(tableLayout);
	}

	public TableLayoutBuilder columns(double... columnSpec) {
		tableLayout.setColumn(columnSpec);
		return this;
	}
	
	public TableLayoutBuilder gaps(int hgap, int vgap) {
		tableLayout.setHGap(hgap);
		tableLayout.setVGap(vgap);
		return this;
	}
	
	
	public TableLayoutBuilder newRow() {
		return newRow(TableLayout.PREFERRED);
	}

	public TableLayoutBuilder newRow(double height) {
		row = tableLayout.getNumRow();
		tableLayout.insertRow(row, height);
		column = 0;
		return this;
	}
	
	public TableLayoutBuilder add(Component c) {
		return add(c, 1);
	}
	
	public TableLayoutBuilder skip() {
		column++;
		return this;
	}
	
	public TableLayoutBuilder add(Component c, int colspan) {
		return add(c, colspan, TableLayout.FULL, TableLayout.FULL);
	}

	public TableLayoutBuilder add(Component c, int colspan, int halign, int valign) {
		int nextColumn = column + colspan - 1;
		ensureColumns(nextColumn);
		panel.add(c, new TableLayoutConstraints(column, row, nextColumn, row, halign, valign));
		column = nextColumn + 1;
		return this;
	}
	
	public TableLayoutBuilder addFullSpan(Component c) {
		return add(c, Math.max(1, tableLayout.getNumColumn() - column));
	}

	public TableLayoutBuilder addLabel(String text) {
		return addLabel(text, CENTER);
	}

	public TableLayoutBuilder addLabel(String text, int valign) {
		return addLabel(text, null, valign);
	}

	public TableLayoutBuilder addLabel(String text, String toolTip, int valign) {
		JLabel label = createLabel(text, toolTip);
		add(label, 1, FULL, valign);
		return this;
	}

	public TableLayoutBuilder addLocalizedLabel(String resourceId) {
		return addLocalizedLabel(resourceId, CENTER);
	}
	
	public TableLayoutBuilder addLocalizedLabel(String resourceId, int valign) {
		return addLocalizedLabel(resourceId, null, valign);
	}	

	public TableLayoutBuilder addLocalizedLabel(String resourceId, String toolTipResourceId) {
		return addLocalizedLabel(resourceId, toolTipResourceId, CENTER);
	}	

	public TableLayoutBuilder addLocalizedLabel(String resourceId, String toolTipResourceId, int valign) {
		String text = SpringLocaleDelegate.getInstance().getMessage(resourceId, null) + ":";
		String toolTip = (toolTipResourceId != null) 
				? SpringLocaleDelegate.getInstance().getMessage(toolTipResourceId, null) : null;
		return addLabel(text, toolTip, valign);
	}	
	
	private JLabel createLabel(String text, String toolTipText) {
		JLabel label = new JLabel(text);
		if (toolTipText == null) {
			toolTipText = text;
			if (toolTipText.endsWith(":")) {
				toolTipText = toolTipText.substring(0, toolTipText.length() - 1).trim();
			}
		}
		label.setToolTipText(toolTipText);
		return label;
	}

	
	public TableLayoutBuilder newMoreOptionPanel() {
		JPanel optionPanel;
		newRow();
		addFullSpan(new MoreOptionPanel(optionPanel = new JPanel()));
		return new TableLayoutBuilder(optionPanel);
	}
	
	private void ensureColumns(int columns) {
		if (row == -1)
			newRow();
		for (int col = tableLayout.getNumColumn(); col < columns; col++) {
			tableLayout.insertColumn(col, TableLayout.PREFERRED);
		}
	}

	public TableLayoutBuilder close() {
		getPanel();
		if (parent == null) {
			throw new IllegalStateException();
		}
		return parent;
	}
	
	public JPanel getPanel() {
		return panel;
	}
}

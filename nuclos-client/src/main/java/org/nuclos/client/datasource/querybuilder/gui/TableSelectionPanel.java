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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.datasource.querybuilder.QueryBuilderIcons;
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
public class TableSelectionPanel extends JPanel implements DragGestureListener {
	
	private final boolean blnShowParameterPanel;

	private static class SystemObjectCellRenderer extends DefaultTableCellRenderer {
		final JLabel label = new JLabel();

		SystemObjectCellRenderer() {
			label.setIcon(QueryBuilderIcons.iconTable16);
			label.setOpaque(true);
			label.setVerticalTextPosition(SwingConstants.CENTER);
			label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			label.setText(value.toString());
			if (isSelected) {
				label.setBackground(Color.LIGHT_GRAY);
			}
			else {
				label.setBackground(Color.WHITE);
			}
			return label;
		}
	}

	private final ParameterPanel parameterpanel;

	private final TableSelectionModel selectionmodelSystemObjects = new TableSelectionModel();
	private final JTable tblSystemObjects = new JTable();

	private final TableSelectionModel selectionmodelQueries = new TableSelectionModel();
	private final JTable tblQueries = new JTable();

	private final DragSource dragsource = DragSource.getDefaultDragSource();

	public TableSelectionPanel(boolean blnShowParameterPanel, 
			boolean blnWithValuelistProviderColumn,
			boolean blnWithParameterLabelColumn) {
		this.blnShowParameterPanel = blnShowParameterPanel;
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
		
		parameterpanel = new ParameterPanel(blnWithValuelistProviderColumn, blnWithParameterLabelColumn);
		init();
	}

	protected void init() {
		tblSystemObjects.setModel(selectionmodelSystemObjects);
		tblSystemObjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSystemObjects.setRowSelectionAllowed(true);
		tblSystemObjects.setShowVerticalLines(true);
		tblSystemObjects.setShowHorizontalLines(false);
		tblSystemObjects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		final JScrollPane scrlpnSystemObjects = new JScrollPane();
		scrlpnSystemObjects.setViewportView(tblSystemObjects);

		tblQueries.setModel(selectionmodelQueries);
		tblQueries.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblQueries.setRowSelectionAllowed(true);
		tblQueries.setShowVerticalLines(true);
		tblQueries.setShowHorizontalLines(false);
		tblQueries.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		final JScrollPane scrlpnSavedQueries = new JScrollPane();
		scrlpnSavedQueries.setViewportView(tblQueries);

		final JTabbedPane tabpn = new JTabbedPane(JTabbedPane.TOP);
		tabpn.add(CommonLocaleDelegate.getMessage("TableSelectionPanel.3","Entit\u00e4ten"), scrlpnSystemObjects);
		tabpn.add(CommonLocaleDelegate.getMessage("TableSelectionPanel.2","Datenquellen"), scrlpnSavedQueries);

		if (blnShowParameterPanel) {
			final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitpn.setResizeWeight(0.72);
			splitpn.setBottomComponent(parameterpanel);
			splitpn.setTopComponent(tabpn);
			add(splitpn, BorderLayout.CENTER);
		} else {
			add(tabpn, BorderLayout.CENTER);
		}

		dragsource.createDefaultDragGestureRecognizer(tblSystemObjects, DnDConstants.ACTION_COPY, this);
		dragsource.createDefaultDragGestureRecognizer(tblQueries, DnDConstants.ACTION_COPY, this);
	}

	public void setTables(ArrayList<Table> lsttable) {
		Collections.sort(lsttable, new Comparator<Table>() {
			@Override
            public int compare(Table t1, Table t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

		selectionmodelQueries.removeAll();
		selectionmodelSystemObjects.removeAll();

		for (Table table : lsttable) {
			if (table.isQuery()) {
				selectionmodelQueries.addTable(table);
			}
			else {
				selectionmodelSystemObjects.addTable(table);
			}
		}

		final SystemObjectCellRenderer cellrenderer = new SystemObjectCellRenderer();

		tblSystemObjects.getColumnModel().getColumn(0).setCellRenderer(cellrenderer);
		tblSystemObjects.getColumnModel().getColumn(0).setPreferredWidth(140);
		tblSystemObjects.getColumnModel().getColumn(1).setPreferredWidth(50);
		tblSystemObjects.getColumnModel().getColumn(2).setPreferredWidth(400);

		tblQueries.getColumnModel().getColumn(0).setCellRenderer(cellrenderer);
		tblQueries.getColumnModel().getColumn(0).setPreferredWidth(140);
		tblQueries.getColumnModel().getColumn(1).setPreferredWidth(50);
		tblQueries.getColumnModel().getColumn(2).setPreferredWidth(400);

	}

	@Override
    public void dragGestureRecognized(DragGestureEvent dge) {
		if (dge.getComponent() == tblSystemObjects) {
			dge.startDrag(null, new TableTransferable(selectionmodelSystemObjects.getRow(tblSystemObjects.getSelectedRow()).getName()));
		}
		else if (dge.getComponent() == tblQueries) {
			dge.startDrag(null, new TableTransferable(selectionmodelQueries.getRow(tblQueries.getSelectedRow()).getName()));
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, Integer.MAX_VALUE);
	}

	public ParameterPanel getParameterPanel() {
		return parameterpanel;
	}

}	// class TableSelectionPanel

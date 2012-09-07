//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect;

import java.awt.event.MouseListener;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.model.FixedTableModel;
import org.nuclos.client.ui.model.MutableListModel;
import org.nuclos.client.ui.renderer.TopTableCellRendererDelegate;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.SpringLocaleDelegate;

/*
 *  TODO: For generification: is this class always used for CollectableEntityField?
 *        Yes, it is! (Thomas Pasch)
 * @author Thomas Pasch
 * @since Nuclos 3.1.01 this is a top-level class.
 */
public class SelectFixedColumnsPanel extends DefaultSelectObjectsPanel<CollectableEntityField> {

	private JTable tblSelectedColumn;

	public SelectFixedColumnsPanel() {
		this(null);
	}
	
	public SelectFixedColumnsPanel(JComponent header) {
		super(header);	
	}

	@Override
	protected void init() {
		super.init();
		
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		this.labAvailableColumns.setText(localeDelegate.getMessage("SelectFixedColumnsController.8","Verf\u00fcgbare Spalten"));
		this.labSelectedColumns.setText(localeDelegate.getMessage("SelectFixedColumnsController.1","Ausgew\u00e4hlte Spalten"));

		this.btnLeft.setToolTipText(localeDelegate.getMessage("SelectFixedColumnsController.5","Markierte Spalte(n) nicht anzeigen"));
		this.btnRight.setToolTipText(localeDelegate.getMessage("SelectFixedColumnsController.4","Markierte Spalte(n) anzeigen"));
		this.btnUp.setToolTipText(localeDelegate.getMessage("SelectFixedColumnsController.6","Markierte Spalte nach oben verschieben"));
		this.btnDown.setToolTipText(localeDelegate.getMessage("SelectFixedColumnsController.7","Markierte Spalte nach unten verschieben"));

		this.btnUp.setVisible(true);
		this.btnDown.setVisible(true);

		tblSelectedColumn = new JTable() {
			@Override
		    public TableCellRenderer getCellRenderer(int row, int column) {
				return new TopTableCellRendererDelegate(super.getCellRenderer(row, column));
		    }
		};
		tblSelectedColumn.setShowHorizontalLines(false);
		tblSelectedColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		scrlpnSelectedColumns.getViewport().add(tblSelectedColumn, null);
		scrlpnSelectedColumns.getViewport().setBackground(tblSelectedColumn.getBackground());
		scrlpnSelectedColumns.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public MutableListModel<CollectableEntityField> getAvailableColumnsModel() {
		return (MutableListModel<CollectableEntityField>) this.getJListAvailableObjects().getModel();
	}

	public MutableListModel<CollectableEntityField> getSelectedColumnsModel() {
		return ((FixedTableModel<CollectableEntityField>) this.tblSelectedColumn.getModel()).getObjectListModel();
	}

	public Set<CollectableEntityField> getFixedColumns() {
		return ((FixedTableModel<CollectableEntityField>) this.tblSelectedColumn.getModel()).getFixedObjSet();
	}

	@Override
	public void setSelectedColumnsModel(MutableListModel<CollectableEntityField> listmodelSelectedFields) {
		this.tblSelectedColumn.setModel(new FixedTableModel<CollectableEntityField>(listmodelSelectedFields));
	}

	public void addMouseListenerAvailableJComponent(MouseListener aListener) {
		this.getJListAvailableObjects().addMouseListener(aListener);
	}

	public void addMouseListenerSelectedJComponent(MouseListener aListener) {
		this.getJListSelectedObjects().addMouseListener(aListener);
		this.tblSelectedColumn.addMouseListener(aListener);
	}

	public void addSelectionListnerSelectedJCmponent(ListSelectionListener aListener) {
		this.tblSelectedColumn.getSelectionModel().addListSelectionListener(aListener);
	}

	public ListSelectionModel getSelectedModelSelectedJComponent() {
		return this.tblSelectedColumn.getSelectionModel();
	}

	public ListSelectionModel getSelectedModelAvailabelJComponent() {
		return this.getJListAvailableObjects().getSelectionModel();
	}

	public void setFixedColumns(Set<CollectableEntityField> fixedColumns) {
		((FixedTableModel<CollectableEntityField>) this.tblSelectedColumn.getModel()).setFixedObjSet(fixedColumns);
		final TableColumnModel cm = tblSelectedColumn.getColumnModel();
		// cm.getColumn(1).setPreferredWidth(2000);
		cm.getColumn(1).setMinWidth(50);
		cm.getColumn(0).setPreferredWidth(50);
		cm.getColumn(0).setMaxWidth(50);
		cm.getColumn(0).setMinWidth(50);
		fitFixedTable();
	}
	
	private void fitFixedTable() {
		TableUtils.setOptimalColumnWidth(tblSelectedColumn, 1);
	}

}	// inner class SelectColumnsPanel

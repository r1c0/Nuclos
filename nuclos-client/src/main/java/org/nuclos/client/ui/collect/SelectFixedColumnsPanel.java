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

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.model.FixedTableModel;
import org.nuclos.client.ui.model.MutableListModel;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;

/*
 *  TODO: For generification: is this class always used for CollectableEntityField?
 *        Yes, it is! (Thomas Pasch)
 * @author Thomas Pasch
 * @since Nuclos 3.1.01 this is a top-level class.
 */
public class SelectFixedColumnsPanel extends DefaultSelectObjectsPanel<CollectableEntityField> {

	private static final long serialVersionUID = 1L;
	
	private JTable tblSelectedColumn;

	public SelectFixedColumnsPanel() {
		this(null);
	}
	
	public SelectFixedColumnsPanel(JComponent header) {
		super(header);

		this.labAvailableColumns.setText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.8","Verf\u00fcgbare Spalten"));
		this.labSelectedColumns.setText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.1","Ausgew\u00e4hlte Spalten"));

		this.btnLeft.setToolTipText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.5","Markierte Spalte(n) nicht anzeigen"));
		this.btnRight.setToolTipText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.4","Markierte Spalte(n) anzeigen"));
		this.btnUp.setToolTipText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.6","Markierte Spalte nach oben verschieben"));
		this.btnDown.setToolTipText(CommonLocaleDelegate.getMessage("SelectFixedColumnsController.7","Markierte Spalte nach unten verschieben"));

		this.btnUp.setVisible(true);
		this.btnDown.setVisible(true);
	}

	@Override
	protected void init() {
		super.init();

		this.tblSelectedColumn = new JTable();
		this.tblSelectedColumn.setShowHorizontalLines(false);
		this.scrlpnSelectedColumns.getViewport().add(tblSelectedColumn, null);
		scrlpnSelectedColumns.getViewport().setBackground(tblSelectedColumn.getBackground());

	}

	@SuppressWarnings("unchecked")
	public MutableListModel<CollectableEntityField> getAvailableColumnsModel() {
		return (MutableListModel<CollectableEntityField>) this.getJListAvailableObjects().getModel();
	}

	@SuppressWarnings("unchecked")
	public MutableListModel<CollectableEntityField> getSelectedColumnsModel() {
		return ((FixedTableModel<CollectableEntityField>) this.tblSelectedColumn.getModel()).getObjectListModel();
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public void setFixedColumns(Set<CollectableEntityField> fixedColumns) {
		((FixedTableModel<CollectableEntityField>) this.tblSelectedColumn.getModel()).setFixedObjSet(fixedColumns);
		this.tblSelectedColumn.getColumnModel().getColumn(0).setPreferredWidth(200);
		this.tblSelectedColumn.getColumnModel().getColumn(1).setPreferredWidth(50);
		this.tblSelectedColumn.getColumnModel().getColumn(1).setMaxWidth(50);
	}

}	// inner class SelectColumnsPanel
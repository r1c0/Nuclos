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
package org.nuclos.client.ui.collect;

import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.model.CommonDefaultListModel;
import org.nuclos.client.ui.model.MutableListModel;
import org.nuclos.client.ui.model.SortedListModel;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;




/**
 * Controller for selecting visible columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SelectFixedColumnsController extends SelectObjectsController<CollectableEntityField> {

	// TODO For generification: is this class always used for CollectableEntityField?
	private static class SelectFixedColumnsPanel extends DefaultSelectObjectsPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTable tblSelectedColumn;

		public SelectFixedColumnsPanel() {
			super();

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
		public MutableListModel<Object> getAvailableColumnsModel() {
			return (MutableListModel<Object>) this.getJListAvailableObjects().getModel();
		}

		@SuppressWarnings("unchecked")
		public MutableListModel<Object> getSelectedColumnsModel() {
			return ((FixedTableModel) this.tblSelectedColumn.getModel()).getObjectListModel();
		}

		@SuppressWarnings("unchecked")
		public Set<CollectableEntityField> getFixedColumns() {
			return ((FixedTableModel) this.tblSelectedColumn.getModel()).getFixedObjSet();
		}

		public <T> void setAvailableColumnsModel(MutableListModel<T> listmodelAvailableFields) {
			this.getJListAvailableObjects().setModel(listmodelAvailableFields);
		}

		public <T> void setSelectedColumnsModel(MutableListModel<T> listmodelSelectedFields) {
			this.tblSelectedColumn.setModel(new FixedTableModel<T>(listmodelSelectedFields));
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
		public <T> void setFixedColumns(Set<T> fixedColumns) {
			((FixedTableModel<T>) this.tblSelectedColumn.getModel()).setFixedObjSet(fixedColumns);
			this.tblSelectedColumn.getColumnModel().getColumn(0).setPreferredWidth(200);
			this.tblSelectedColumn.getColumnModel().getColumn(1).setPreferredWidth(50);
			this.tblSelectedColumn.getColumnModel().getColumn(1).setMaxWidth(50);
		}

	}	// inner class SelectColumnsPanel

	private static class FixedTableModel<T> extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private MutableListModel<T> objectListModel;
		private final Set<T> fixedObjSet;

		public FixedTableModel(MutableListModel<T> objectColl) {
			super();
			this.fixedObjSet = new HashSet<T>();
			this.objectListModel = objectColl;
			this.objectListModel.addListDataListener(new ListDataListener() {

				@Override
				public void intervalAdded(ListDataEvent e) {
					FixedTableModel.this.fireTableRowsInserted(e.getIndex0(), e.getIndex1());
				}

				@Override
				public void intervalRemoved(ListDataEvent e) {
					FixedTableModel.this.fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
				}

				@Override
				public void contentsChanged(ListDataEvent e) {
					FixedTableModel.this.fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
				}
			});
		}

		public void setFixedObjSet(Set<T> fixedColumns) {
			fixedObjSet.clear();
			fixedObjSet.addAll(fixedColumns);
			FixedTableModel.this.fireTableStructureChanged();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return objectListModel.getSize();
		}

		@Override
		public String getColumnName(int col) {
			return (col == 0) ? "Spalte" : "Fixiert";
		}

		@Override
		public Object getValueAt(int row, int col) {
			return (col == 0) ? objectListModel.getElementAt(row) : isObjectFixed(objectListModel.getElementAt(row));
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1 && aValue instanceof Boolean) {
				if (((Boolean) aValue).booleanValue()) {
					if (this.fixedObjSet.size() + 1 >= objectListModel.getSize()) {
						JOptionPane.showMessageDialog(null, CommonLocaleDelegate.getMessage("SelectFixedColumnsController.2","Es d\u00fcrfen nicht alle Spalten ausgeblendet und fixiert werden"));

					}
					else {
						this.fixedObjSet.add((T) objectListModel.getElementAt(rowIndex));
					}
				}
				else {
					this.fixedObjSet.remove(objectListModel.getElementAt(rowIndex));
				}
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex != 0);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return (columnIndex == 0) ? Object.class : Boolean.class;
		}

		private Boolean isObjectFixed(Object rowObj) {
			return this.fixedObjSet.contains(rowObj);
		}

		public MutableListModel<T> getObjectListModel() {
			return objectListModel;
		}

		public Set<T> getFixedObjSet() {
			return fixedObjSet;
		}
	}

	private final SelectFixedColumnsPanel selectPnl = new SelectFixedColumnsPanel();

	public SelectFixedColumnsController(Component parent) {
		super(parent);
	}

	@Override
	protected SelectFixedColumnsPanel getPanel() {
		return this.selectPnl;
	}

	@Override
	protected void setupListeners(final MutableListModel<?> listmodelSelectedFields) {
		// add list selection listener for "right" button:
		this.getPanel().getJListAvailableObjects().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {

				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
				final boolean bEnable = !lsm.isSelectionEmpty();
				getPanel().btnRight.setEnabled(bEnable);
			}	// valueChanged
		});

		// add list selectioners for "left", "up" and "down" buttons:
		(this.getPanel()).addSelectionListnerSelectedJCmponent(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

				final boolean bEnable = !lsm.isSelectionEmpty();

				getPanel().btnLeft.setEnabled(bEnable);
				getPanel().btnUp.setEnabled(bEnable);
				getPanel().btnDown.setEnabled(bEnable);
			}	// valueChanged
		});

		this.getPanel().btnRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveRight();
			}
		});
		this.getPanel().btnLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveLeft();
			}
		});

		// double click on list entry as shortcut for pressing the corresponding button:
		this.getPanel().addMouseListenerAvailableJComponent(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					moveRight();
				}
			}
		});

		this.getPanel().addMouseListenerSelectedJComponent(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					moveLeft();
				}
			}
		});

		this.getPanel().btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveUpDown(-1);
			}
		});
		this.getPanel().btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveUpDown(+1);
			}
		});
	}
	
	/**
	 * performs the dialog. The lists given to the method are not modified.
	 * The resulting lists are available in the <code>getAvailableFields()</code> and
	 * <code>getSelectedFields()</code> methods, resp. They should be regarded only
	 * when this method returns <code>true</code>.
	 * @param fixedColumns
	 * @param aLstAvailableFields the list of available fields
	 * @param aLstSelectedFields the list of selected fields
	 * @param comparatorAvailableFields the <code>Comparator</code> used to sort the list of available fields.
	 * If null, the fields must be <code>Comparable</code>.
	 * @param sTitle
	 * @return Did the user press OK?
	 */
	public boolean run(ChoiceEntityFieldList ro, String sTitle) {
		// model --> dialog:

		// The lists given as parameters are copied here. The original lists are not modified.
		try {
			ro = (ChoiceEntityFieldList) ro.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalArgumentException(e);
		}
		// final List<CollectableEntityField> lstAvailableFields = new ArrayList<CollectableEntityField>(aLstAvailableFields);
		// final List<CollectableEntityField> lstSelectedFields = new ArrayList<CollectableEntityField>(aLstSelectedFields);

		final MutableListModel<CollectableEntityField> listmodelAvailableFields = new SortedListModel<CollectableEntityField>(ro.getAvailableFields(), ro.getComparatorForAvaible());
		final MutableListModel<CollectableEntityField> listmodelSelectedFields = new CommonDefaultListModel<CollectableEntityField>(ro.getSelectedFields());

		this.getPanel().setAvailableColumnsModel(listmodelAvailableFields);
		this.getPanel().setSelectedColumnsModel(listmodelSelectedFields);
		this.getPanel().setFixedColumns(ro.getFixed());

		// @todo the listeners are added here so calling run() multiple times is not possible
		this.setupListeners(listmodelSelectedFields);

		final JOptionPane optpn = new JOptionPane(getPanel(), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		// perform the dialog:
		final JDialog dlg = optpn.createDialog(this.getParent(), sTitle);
		dlg.setModal(true);
		dlg.setResizable(true);
		dlg.pack();
		dlg.setLocationRelativeTo(this.getParent());
		dlg.setVisible(true);

		final Integer iBtn = (Integer) optpn.getValue();

		return (iBtn != null && iBtn.intValue() == JOptionPane.OK_OPTION);
	}

	private static List<CollectableEntityField> getObjects(ListModel model) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();

		for (int i = 0; i < model.getSize(); ++i) {
			result.add((CollectableEntityField) model.getElementAt(i));
		}

		return result;
	}

	/**
	 * @return the selected objects, when the dialog is closed.
	 */
	@Override
	public List<CollectableEntityField> getSelectedObjects() {
		return getObjects(this.getPanel().getSelectedColumnsModel());
	}

	/**
	 * @return the available objects, when the dialog is closed
	 */
	@Override
	public List<CollectableEntityField> getAvailableObjects() {
		return getObjects(this.getPanel().getAvailableColumnsModel());
	}

	/**
	 * @return the fixed columns, when the dialog is closed
	 */
	public Set<CollectableEntityField> getFixedObjects() {
		return new HashSet<CollectableEntityField>(this.getPanel().getFixedColumns());
	}

	private void moveLeft() {
		MutableListModel<Object> modelSrc = this.getPanel().getSelectedColumnsModel();
		ListSelectionModel selectionModel = this.getPanel().getSelectedModelSelectedJComponent();
		MutableListModel<Object> modelDest = this.getPanel().getAvailableColumnsModel();
		final int[] aiSelectedIndices = getSelectedIndices(selectionModel);

		final List<Object> lstNotSelected = new ArrayList<Object>();

		for (int i = modelSrc.getSize() - 1; i >= 0; --i) {
			boolean isSelected = false;
			for (int y = aiSelectedIndices.length - 1; y >= 0; --y) {
				int index = aiSelectedIndices[y];
				if (i == index) {
					isSelected = true;
				}
			}

			if (!isSelected) {
				lstNotSelected.add(modelSrc.getElementAt(i));
			}
		}

		lstNotSelected.removeAll(getFixedObjects());

		if (lstNotSelected.size() == 0) {
			JOptionPane.showMessageDialog(this.getParent(), CommonLocaleDelegate.getMessage("SelectFixedColumnsController.3","Es d\u00fcrfen nicht alle Spalten ausgeblendet oder fixiert werden."));
		}
		else {
			moveLeftRight(modelSrc, modelDest, selectionModel);
		}
	}

	private void moveRight() {
		moveLeftRight(
				this.getPanel().getAvailableColumnsModel(),
				this.getPanel().getSelectedColumnsModel(),
				this.getPanel().getSelectedModelAvailabelJComponent());
	}

	private static void moveLeftRight(MutableListModel<Object> modelSrc, MutableListModel<Object> modelDest, ListSelectionModel selectionModel) {
		final int[] aiSelectedIndices = getSelectedIndices(selectionModel);

		// 1. add the selected rows to the dest list, in increasing order:
		for (int iSelectedIndex : aiSelectedIndices) {
			modelDest.add(modelSrc.getElementAt(iSelectedIndex));
		}	// for

		// 2. remove the selected rows from the source list, in decreasing order:
		for (int i = aiSelectedIndices.length - 1; i >= 0; --i) {
			int index = aiSelectedIndices[i];
			modelSrc.remove(index);
			index = Math.min(index, modelSrc.getSize() - 1);
			if (index >= 0) {
				selectionModel.setSelectionInterval(index, index);
			}
		}	// for
	}

	public void moveUpDown(int iDirection) {
		final MutableListModel<Object> listmodelSelectedFields = this.getPanel().getSelectedColumnsModel();

		final int iIndex = this.getPanel().getSelectedModelSelectedJComponent().getAnchorSelectionIndex();
		final int iNewIndex = iIndex + iDirection;
		if (iNewIndex >= 0 && iNewIndex < listmodelSelectedFields.getSize()) {
			final Object o = listmodelSelectedFields.getElementAt(iIndex);
			listmodelSelectedFields.remove(iIndex);
			listmodelSelectedFields.add(iNewIndex, o);
			this.getPanel().getSelectedModelSelectedJComponent().setSelectionInterval(iNewIndex, iNewIndex);
		}
	}

	private static int[] getSelectedIndices(ListSelectionModel sm) {
		final int iMinIndex = sm.getMinSelectionIndex();
		final int iMaxIndex = sm.getMaxSelectionIndex();

		if ((iMinIndex < 0) || (iMaxIndex < 0)) {
			return new int[0];
		}

		final int[] aiTemp = new int[1 + (iMaxIndex - iMinIndex)];
		int i = 0;
		for (int iIndex = iMinIndex; iIndex <= iMaxIndex; iIndex++) {
			if (sm.isSelectedIndex(iIndex)) {
				aiTemp[i++] = iIndex;
			}
		}
		final int[] result = new int[i];
		System.arraycopy(aiTemp, 0, result, 0, i);
		return result;
	}


}	// class SelectColumnsController

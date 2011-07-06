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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.model.CommonDefaultListModel;
import org.nuclos.client.ui.model.MutableListModel;
import org.nuclos.client.ui.model.SortedListModel;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;

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


	public SelectFixedColumnsController(Component parent, SelectFixedColumnsPanel panel) {
		super(parent, panel);
	}

	@Override
	protected void setupListeners(final MutableListModel<CollectableEntityField> listmodelSelectedFields) {
		final SelectFixedColumnsPanel panel = getSfcPanel();
		
		// add list selection listener for "right" button:
		panel.getJListAvailableObjects().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {

				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
				final boolean bEnable = !lsm.isSelectionEmpty();
				getPanel().btnRight.setEnabled(bEnable);
			}	// valueChanged
		});

		// add list selectioners for "left", "up" and "down" buttons:
		panel.addSelectionListnerSelectedJCmponent(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

				final boolean bEnable = !lsm.isSelectionEmpty();

				getPanel().btnLeft.setEnabled(bEnable);
				getPanel().btnUp.setEnabled(bEnable);
				getPanel().btnDown.setEnabled(bEnable);
			}	// valueChanged
		});

		panel.btnRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveRight();
			}
		});
		panel.btnLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveLeft();
			}
		});

		// double click on list entry as shortcut for pressing the corresponding button:
		panel.addMouseListenerAvailableJComponent(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					moveRight();
				}
			}
		});

		panel.addMouseListenerSelectedJComponent(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					moveLeft();
				}
			}
		});

		panel.btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				moveUpDown(-1);
			}
		});
		panel.btnDown.addActionListener(new ActionListener() {
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

		final SelectFixedColumnsPanel panel = getSfcPanel();
		panel.setAvailableColumnsModel(listmodelAvailableFields);
		panel.setSelectedColumnsModel(listmodelSelectedFields);
		panel.setFixedColumns(ro.getFixed());

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

	/**
	 * @return the fixed columns, when the dialog is closed
	 */
	public Set<CollectableEntityField> getFixedObjects() {
		return new HashSet<CollectableEntityField>(getSfcPanel().getFixedColumns());
	}

	private void moveLeft() {
		final SelectFixedColumnsPanel panel = getSfcPanel();
		MutableListModel<CollectableEntityField> modelSrc = panel.getSelectedColumnsModel();
		ListSelectionModel selectionModel = panel.getSelectedModelSelectedJComponent();
		MutableListModel<CollectableEntityField> modelDest = panel.getAvailableColumnsModel();
		final int[] aiSelectedIndices = getSelectedIndices(selectionModel);

		final List<CollectableEntityField> lstNotSelected = new ArrayList<CollectableEntityField>();

		for (int i = modelSrc.getSize() - 1; i >= 0; --i) {
			boolean isSelected = false;
			for (int y = aiSelectedIndices.length - 1; y >= 0; --y) {
				int index = aiSelectedIndices[y];
				if (i == index) {
					isSelected = true;
				}
			}

			if (!isSelected) {
				lstNotSelected.add((CollectableEntityField) modelSrc.getElementAt(i));
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
		final SelectFixedColumnsPanel panel = getSfcPanel();
		moveLeftRight(
				panel.getAvailableColumnsModel(),
				panel.getSelectedColumnsModel(),
				panel.getSelectedModelAvailabelJComponent());
	}

	private static void moveLeftRight(MutableListModel<CollectableEntityField> modelSrc, MutableListModel<CollectableEntityField> modelDest, ListSelectionModel selectionModel) {
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
		final SelectFixedColumnsPanel panel = getSfcPanel();
		final MutableListModel<CollectableEntityField> listmodelSelectedFields = panel.getSelectedColumnsModel();

		final int iIndex = panel.getSelectedModelSelectedJComponent().getAnchorSelectionIndex();
		final int iNewIndex = iIndex + iDirection;
		if (iNewIndex >= 0 && iNewIndex < listmodelSelectedFields.getSize()) {
			final Object o = listmodelSelectedFields.getElementAt(iIndex);
			listmodelSelectedFields.remove(iIndex);
			listmodelSelectedFields.add(iNewIndex, o);
			panel.getSelectedModelSelectedJComponent().setSelectionInterval(iNewIndex, iNewIndex);
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

	public SelectFixedColumnsPanel getSfcPanel() {
		return (SelectFixedColumnsPanel) getPanel();
	}
	
	/**
	 * @return the selected objects, when the dialog is closed.
	 */
	@Override
	public List<CollectableEntityField> getSelectedObjects() {
		// return getObjects(getSfcPanel().getJListSelectedObjects().getModel());
		return getObjects(getSfcPanel().getSelectedColumnsModel());
	}
	
}	// class SelectColumnsController

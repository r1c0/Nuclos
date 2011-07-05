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
package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.ui.collect.model.ResultObjects;

/**
 * Controller for selecting objects from a list of available objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class SelectObjectsController<T> extends Controller {

	public SelectObjectsController(Component parent) {
		super(parent);
	}

	protected abstract SelectObjectsPanel getPanel();

	protected void setupListeners(final MutableListModel<?> listmodelSelectedFields) {
		// add list selection listener for "right" button:
		this.getPanel().getJListAvailableObjects().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

				final boolean bEnable = !lsm.isSelectionEmpty();

				SelectObjectsController.this.getPanel().btnRight.setEnabled(bEnable);
			}  // valueChanged
		});

		// add list selectioners for "left", "up" and "down" buttons:
		this.getPanel().getJListSelectedObjects().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent ev) {
				final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

				final boolean bEnable = !lsm.isSelectionEmpty();

				SelectObjectsController.this.getPanel().btnLeft.setEnabled(bEnable);
				SelectObjectsController.this.getPanel().btnUp.setEnabled(bEnable);
				SelectObjectsController.this.getPanel().btnDown.setEnabled(bEnable);
			}  // valueChanged
		});

		/**
		 * inner class MoveLeftRightActionListener
		 */
		class MoveLeftRightActionListener implements ActionListener {
			private final JList jlstSrc;
			private final JList jlstDest;

			MoveLeftRightActionListener(JList jlstSrc, JList jlstDest) {
				this.jlstSrc = jlstSrc;
				this.jlstDest = jlstDest;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent ev) {
				final MutableListModel<Object> modelSrc = (MutableListModel<Object>) jlstSrc.getModel();
				final MutableListModel<Object> modelDest = (MutableListModel<Object>) jlstDest.getModel();
				final int[] aiSelectedIndices = jlstSrc.getSelectedIndices();

				// 1. add the selected rows to the dest list, in increasing order:
				for (int iSelectedIndex : aiSelectedIndices) {
					modelDest.add(modelSrc.getElementAt(iSelectedIndex));
				}

				// 2. remove the selected rows from the source list, in decreasing order:
				for (int i = aiSelectedIndices.length - 1; i >= 0; --i) {
					int index = aiSelectedIndices[i];
					modelSrc.remove(index);
					index = Math.min(index, modelSrc.getSize() - 1);
					if (index >= 0) {
						jlstSrc.setSelectedIndex(index);
					}
				}  // for
			}
		}  // inner class MoveLeftRightActionListener

		final ActionListener alMoveRight = new MoveLeftRightActionListener(this.getPanel().getJListAvailableObjects(),
				this.getPanel().getJListSelectedObjects());
		final ActionListener alMoveLeft = new MoveLeftRightActionListener(this.getPanel().getJListSelectedObjects(),
				this.getPanel().getJListAvailableObjects());

		this.getPanel().btnRight.addActionListener(alMoveRight);
		this.getPanel().btnLeft.addActionListener(alMoveLeft);

		// double click on list entry as shortcut for pressing the corresponding button:
		this.getPanel().getJListAvailableObjects().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					alMoveRight.actionPerformed(new ActionEvent(getPanel().btnRight, 0, null));
				}
			}
		});

		this.getPanel().getJListSelectedObjects().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					alMoveLeft.actionPerformed(new ActionEvent(getPanel().btnLeft, 0, null));
				}
			}
		});

		/**
		 * inner class MoveUpDownActionListener
		 */
		class MoveUpDownActionListener implements ActionListener {
			private final int iDirection;

			/**
			 * @param iDirection -1 for up, +1 for down
			 */
			MoveUpDownActionListener(int iDirection) {
				this.iDirection = iDirection;
			}

			@Override
			public void actionPerformed(ActionEvent ev) {
				/** @todo allow multi-selection */
				final JList jlstSelectedColumns = SelectObjectsController.this.getPanel().getJListSelectedObjects();
				final int iIndex = jlstSelectedColumns.getSelectedIndex();
				final int iNewIndex = iIndex + this.iDirection;
				if (iNewIndex >= 0 && iNewIndex < listmodelSelectedFields.getSize()) {
					final Object o = listmodelSelectedFields.getElementAt(iIndex);
					listmodelSelectedFields.remove(iIndex);
					listmodelSelectedFields.add(iNewIndex, o);
					jlstSelectedColumns.setSelectedIndex(iNewIndex);
				}
			}
		}  // inner class MoveUpDownActionListener

		this.getPanel().btnUp.addActionListener(new MoveUpDownActionListener(-1));
		this.getPanel().btnDown.addActionListener(new MoveUpDownActionListener(+1));
	}

	/**
	 * performs the dialog. The lists given to the method are not modified.
	 * The resulting lists are available in the <code>getAvailableFields()</code> and
	 * <code>getSelectedFields()</code> methods, resp. They should be regarded only
	 * when this method returns <code>true</code>.
	 * @param lstAvailableFields the list of available fields
	 * @param lstSelectedFields the list of selected fields
	 * @param comparatorAvailableFields the <code>Comparator</code> used to sort the list of available fields.
	 * If null, the fields must be <code>Comparable</code>.
	 * @param sTitle
	 * @return Did the user press OK?
	 */
	public boolean run(ResultObjects<T> ro, String sTitle) {
		// model --> dialog:

		// The lists given as parameters are copied here. The original lists are not modified.
		try {
			ro = (ResultObjects<T>) ro.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalArgumentException(e);
		}
		// final List<T> _lstAvailableFields = new ArrayList<T>(ro.getAvailableFields());
		// final List<T> _lstSelectedFields = new ArrayList<T>(ro.getSelectedFields());

		final MutableListModel<T> listmodelAvailableFields = new SortedListModel<T>(ro.getAvailableFields(), ro.getComparatorForAvaible());
		final MutableListModel<T> listmodelSelectedFields = new CommonDefaultListModel<T>(ro.getSelectedFields());

		this.getPanel().getJListAvailableObjects().setModel(listmodelAvailableFields);
		this.getPanel().getJListSelectedObjects().setModel(listmodelSelectedFields);

		final JOptionPane optpn = new JOptionPane(getPanel(), JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		/** @todo the listeners are added here so calling run() multiple times is not possible */
		this.setupListeners(listmodelSelectedFields);

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

	private List<T> getObjects(ListModel model) {
		final List<T> result = new ArrayList<T>();

		for (int i = 0; i < model.getSize(); ++i) {
			result.add((T) model.getElementAt(i));
		}

		return result;
	}

	/**
	 * @return the selected objects, when the dialog is closed.
	 */
	public List<T> getSelectedObjects() {
		return getObjects(this.getPanel().getJListSelectedObjects().getModel());
	}

	/**
	 * @return the available objects, when the dialog is closed
	 */
	public List<T> getAvailableObjects() {
		return getObjects(this.getPanel().getJListAvailableObjects().getModel());
	}

}  // class SelectObjectsController

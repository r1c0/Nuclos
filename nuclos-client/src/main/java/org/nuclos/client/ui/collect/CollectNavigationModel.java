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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/**
 * A model for navigating objects while collecting them.
 * Consists of a table model and a list selection model.
 * Useful for implementing navigation via navigation buttons.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectNavigationModel {

	private final TableModel tblmodel;

	private final ListSelectionModel listselectionmodel;
	
	private final List<ActionListener> listChangeListener = new ArrayList<ActionListener>();

	/**
	 * creates a <code>CollectNavigationModel</code> out of its components.
	 * @param tblmodel
	 * @param lsm
	 */
	public CollectNavigationModel(TableModel tblmodel, ListSelectionModel lsm) {
		this.tblmodel = tblmodel;
		this.listselectionmodel = lsm;
	}

	/**
	 * selects the first element/row
	 */
	public void selectFirstElement() {
		this.listselectionmodel.setSelectionInterval(0, 0);
		notifyChangeListener();
	}

	/**
	 * selects the previous element/row
	 * @precondition !isFirstElementSelected()
	 */
	public void selectPreviousElement() {
		if (isFirstElementSelected()) {
			throw new IllegalStateException("collect.navigation.model.exception.1");//"Das erste Element ist bereits ausgew\u00e4hlt.");
		}
		final int i = this.listselectionmodel.getAnchorSelectionIndex() - 1;
		this.listselectionmodel.setSelectionInterval(i, i);
		notifyChangeListener();
	}

	/**
	 * selects the next element/row
	 * @precondition !isLastElementSelected()
	 */
	public void selectNextElement() {
		if (isLastElementSelected()) {
			throw new IllegalStateException("collect.navigation.model.exception.2");//"Das letzte Element ist bereits ausgew\u00e4hlt.");
		}
		final int i = this.listselectionmodel.getAnchorSelectionIndex() + 1;
		this.listselectionmodel.setSelectionInterval(i, i);
		notifyChangeListener();
	}

	/**
	 * selects the last element/row
	 */
	public void selectLastElement() {
		final int iLast = getLastIndex();
		this.listselectionmodel.setSelectionInterval(iLast, iLast);
		notifyChangeListener();
	}

	public boolean isFirstElementSelected() {
		return (this.listselectionmodel.getAnchorSelectionIndex() == 0);
	}

	public boolean isLastElementSelected() {
		final int iSelected = this.listselectionmodel.getAnchorSelectionIndex();
		final int iLast = getLastIndex();
		return (iSelected == iLast);
	}

	private int getLastIndex() {
		return this.tblmodel.getRowCount() - 1;
	}
	
	private void notifyChangeListener() {
		for (ActionListener al : listChangeListener) {
			al.actionPerformed(new ActionEvent(this, 0, "SELECTION_CHANGED"));
		}
	}
	
	public void addChangeListener(ActionListener al) {
		this.listChangeListener.add(al);
	}
	
	public void removeChangeListener(ActionListener al) {
		this.listChangeListener.remove(al);
	}

}  // class CollectNavigationModel

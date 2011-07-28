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
package org.nuclos.client.ui.collect.model;

import java.util.Collection;
import java.util.List;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;

/**
 * <code>TableModel</code> for <code>Collectable</code>s. Each row in this table model is a <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public interface CollectableTableModel <Clct extends Collectable> extends CollectableEntityFieldBasedTableModel {

	/**
	 * Returns the value for the cell at <code>iColumn</code> and <code>iRow</code>.
	 *
	 * @param	iRow	the row whose value is to be queried
	 * @param	iColumn	 the column whose value is to be queried
	 * @return the value at the specified cell
	 */
	@Override
	public CollectableField getValueAt(int iRow, int iColumn);

	/**
	 * @param iRow
	 * @return the <code>Collectable</code> stored at row <code>iRow</code> in this model.
	 */
	Clct getCollectable(int iRow);

	/**
	 * sets the collectable in the given row
	 * @param iRow
	 */
	void setCollectable(int iRow, Clct clct);

	/**
	 * @return an unmodifiable <code>List</code> containing the Collectables (rows) in this model.
	 */
	List<Clct> getCollectables();

	/**
	 * @param lstclct
	 * @todo add precondition lstclct != null
	 */
	void setCollectables(List<Clct> lstclct);

	/**
	 * adds a row at the specified position and with the given contents.
	 * @param iRow
	 * @param clct
	 */
	void add(int iRow, Clct clct);

	/**
	 * adds a row at the end of this table model with the given contents.
	 * @param clct
	 */
	void add(Clct clct);

	/**
	 * removes the row at the specified position.
	 * @param iRow
	 */
	void remove(int iRow);

	/**
	 * removes the (first) row containing the given <code>Collectable</code>.
	 * @param clct
	 */
	void remove(Collectable clct);

	/**
	 * @param oId id of the <code>Collectable</code> to find.
	 * @return the index of the column with the given id. -1 if none was found.
	 */
	int findRowById(Object oId);

	/**
	 * removes all rows from this model.
	 */
	void clear();

	/**
	 * adds all of the given <code>Collectable</code>s to this model.
	 * @param collclct Collection<Collectable>
	 */
	void addAll(Collection<Clct> collclct);

	/**
	 * adds one column to this model.
	 * @param iColumn the column to add.
	 * @param clctef the <code>CollectableEntityField</code> that represents the column.
	 * @postcondition getEntityField(iColumn) == clctef
	 */
	void addColumn(int iColumn, CollectableEntityField clctef);

	/**
	 * removes the column with the given index from this model.
	 * @param iColumn
	 */
	void removeColumn(int iColumn);

	/**
	 * determines the (visible) columns of this model.
	 * Each column is represented by a <code>CollectableEntityField</code>.
	 * @param lstclctefColumns List<CollectableEntityField>
	 */
	void setColumns(List<? extends CollectableEntityField> lstclctefColumns);

	/**
	 * @param sFieldName
	 * @return the index of the column with the given fieldname. -1 if none was found.
	 */
	int findColumnByFieldName(String sFieldName);

}  // interface CollectableTableModel

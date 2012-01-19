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
package org.nuclos.client.ui.treetable;

import javax.swing.tree.TreeModel;

/**
 * Model for a <code>JTreeTable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:boris.sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 * @see JTreeTable
 */
public interface TreeTableModel extends TreeModel {

	/**
	 * @return the number of available columns.
	 */
	public int getColumnCount();

	/**
	 * @return the name of column number <code>iColumn</code>.
	 */
	public String getColumnName(int iColumn);

	/**
	 * @return the type of column number <code>iColumn</code>.
	 */
	public Class<?> getColumnClass(int iColumn);

	/**
	 * @return the value to be displayed for node <code>node</code> at column number <code>iColumn</code>.
	 */
	public Object getValueAt(Object node, int iColumn);

	/**
	 * @return Is the value for node <code>node</code> at column number <code>iColumn</code> editable?
	 */
	public boolean isCellEditable(Object node, int iColumn);

	/**
	 * sets the value for node <code>node</code> at column number <code>iColumn</code>.
	 */
	public void setValueAt(Object oValue, Object node, int iColumn);

}	// interface TreeTableModel

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
package org.nuclos.client.ui.table;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Provides a <code>TableCellEditor</code> for a <code>JTable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface TableCellEditorProvider {

	/**
	 * @param table
	 * @param iRow row of the table (not the table model)
	 * @param clctefTarget collectable entity field (for the column type)
	 * @return a <code>TableCellEditor</code> for the given table cell.
	 */
	TableCellEditor getTableCellEditor(JTable table, int iRow, CollectableEntityField clctefTarget);

}

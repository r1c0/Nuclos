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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;

/**
 * Helper class for collectable tables.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectableTableHelper {
	/**
	 * @return List<CollectableEntityField> the <code>CollectableEntityField</code>s from the given table's columns,
	 * in the order they are displayed in the table.
	 * @precondition tbl.getModel() instanceof CollectableEntityFieldBasedTableModel
	 */
	public static List<CollectableEntityField> getCollectableEntityFieldsFromColumns(JTable tbl) {
		if (!(tbl.getModel() instanceof CollectableEntityFieldBasedTableModel)) {
			throw new IllegalArgumentException("tbl");
		}
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();

		final CollectableEntityFieldBasedTableModel tblmdl = (CollectableEntityFieldBasedTableModel) tbl.getModel();

		final Enumeration<TableColumn> enumeration = tbl.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn column = enumeration.nextElement();
			result.add(tblmdl.getCollectableEntityField(column.getModelIndex()));
		}
		return result;
	}

	/**
	 * @return List<String> the field names from the given table's columns.
	 * @precondition tbl.getModel() instanceof CollectableTableModel
	 */
	public static List<String> getFieldNamesFromColumns(JTable tbl) {
		return CollectionUtils.transform(getCollectableEntityFieldsFromColumns(tbl), new CollectableEntityField.GetName());
	}

	public static List<Integer> getColumnWidths(JTable tbl) {
		final List<Integer> result = new ArrayList<Integer>();
		final Enumeration<TableColumn> enumeration = tbl.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn column = enumeration.nextElement();
			result.add(column.getWidth());
		}
		return result;
	}

	/**
	 * @return Map<String, Integer> the <code>CollectableEntityField</code> Names from the given table's columns
	 * @precondition tbl.getModel() instanceof CollectableEntityFieldBasedTableModel
	 */
	public static Map<String, Integer> getColumnWidthsMap(JTable tbl) {
		final Map<String, Integer> result = new HashMap<String, Integer>();
		final CollectableEntityFieldBasedTableModel tblmdl = (CollectableEntityFieldBasedTableModel) tbl.getModel();
		final Enumeration<TableColumn> enumeration = tbl.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			final TableColumn column = enumeration.nextElement();
			result.put(tblmdl.getCollectableEntityField(column.getModelIndex()).getName(), column.getWidth());
		}
		return result;
	}
	
}  // class CollectableTableHelper

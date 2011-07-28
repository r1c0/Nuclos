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

import javax.swing.table.TableModel;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;

/**
 * A <code>TableModel</code> where the meta information for each column is taken from a <code>CollectableEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectableEntityFieldBasedTableModel extends TableModel {

	/**
	 * @param iColumn
	 * @return the <code>CollectableEntityField</code> associated with the column with the given index.
	 */
	CollectableEntityField getCollectableEntityField(int iColumn);

	/**
	 * Converts the given value (which must be of the type stored in the table model) as a CollectableField.
 	 * @param oValue
	 * @return the given value as CollectableField
	 */
	public CollectableField getValueAsCollectableField(Object oValue);


}  // interface CollectableEntityFieldBasedTableModel

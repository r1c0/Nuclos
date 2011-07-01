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

import javax.swing.event.TableModelEvent;

/**
 * A TableModelEvent fired by a SortableTableModel to indicate that the table model was re-sorted.
 * Listeners can decide whether they (by default) treat the event like a regular "table model data changed" event
 * or whether they treat it like "the sorting changed, but the data is still the same".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class SortableTableModelEvent extends TableModelEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SortableTableModelEvent(SortableTableModel source) {
		super(source);
	}

}  // class SortableTableModelEvent

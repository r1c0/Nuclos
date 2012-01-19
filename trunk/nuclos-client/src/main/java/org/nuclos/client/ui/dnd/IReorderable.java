//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.dnd;

/**
 * Drag and Drop move support for within a JTables and JList. Mainly based on the code at 
 * <a href="http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable">
 * here</a>.
 * <p>
 * The model of the Swing component must implement {@link IReorderable}.
 * </p>
 * @author Thomas Pasch
 * @since 3.1.01
 */
public interface IReorderable {

	/**
	 * The model should normally force the 2 effected rows/lines to update, i.e.
	 * <code><pre>		
	 * fireTableRowsUpdated(fromModel, fromModel);
	 * fireTableRowsUpdated(toModel, toModel);		
	 * </pre><code>
	 * @param fromModel is in terms of the <em>model</em>.
	 * @param toModel is in terms of the <em>model</em>.
	 */
	void reorder(int fromModel, int toModel);
	
}

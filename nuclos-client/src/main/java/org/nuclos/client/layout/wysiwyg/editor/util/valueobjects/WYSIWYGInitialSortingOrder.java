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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_INITIAL_SORTING_ORDER;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;

/**
 * Small Valueclass for storing {@link LayoutMLConstants#ELEMENT_INITIALSORTINGORDER}<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class WYSIWYGInitialSortingOrder {

	private String name;
	private String sortingorder;

	/**
	 * 
	 * @param name The Name of the {@link WYSIWYGSubFormColumn} 
	 * @param sortingorder the Sortingorder to use:
	 * <ul>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_ASCENDING}</li>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_DESCENDING}</li>
	 * </ul>
	 */
	public WYSIWYGInitialSortingOrder(String name, String sortingorder) {
		setSortingOrder(sortingorder);
		this.name = name;
	}
	
	/**
	 * @return the Name of the SubformColumn
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the Name of the SubformColum
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the Sortingorder:
	 * <ul>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_ASCENDING}</li>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_DESCENDING}</li>
	 * </ul>
	 */
	public String getSortingOrder() {
		return sortingorder;
	}
	/**
	 * 
	 * @param sortingorder the Sortingorder to use:
	 * <ul>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_ASCENDING}</li>
	 * <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_DESCENDING}</li>
	 * </ul>
	 */
	public void setSortingOrder(String sortingorder) {
		if (PROPERTY_INITIAL_SORTING_ORDER.DESCENDING.equals(sortingorder))
			this.sortingorder = LayoutMLConstants.ATTRIBUTEVALUE_DESCENDING;
		else if (PROPERTY_INITIAL_SORTING_ORDER.ASCENDING.equals(sortingorder))
			this.sortingorder = LayoutMLConstants.ATTRIBUTEVALUE_ASCENDING;
		if (LayoutMLConstants.ATTRIBUTEVALUE_ASCENDING.equals(sortingorder))
			this.sortingorder = LayoutMLConstants.ATTRIBUTEVALUE_ASCENDING;
		else if (LayoutMLConstants.ATTRIBUTEVALUE_DESCENDING.equals(sortingorder))
			this.sortingorder = LayoutMLConstants.ATTRIBUTEVALUE_DESCENDING;
	}

}

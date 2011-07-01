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

/**
 * Small ValueClass for storing {@link LayoutMLConstants#ELEMENT_INITIALFOCUSCOMPONENT}<br>
 * This LayoutML Element sets the CollectableComponent which has the initial Focus when the Mask opens.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class WYSIWYGInitialFocusComponent {

	private String entity = null;
	private String name = null;
	
	/**
	 * 
	 * @param entity The Subform Entity (if there is one)
	 * @param name The Name of the CollectableComponent (or SubformColumn if entity is set)
	 */
	public WYSIWYGInitialFocusComponent(String entity, String name) {
		this.entity = entity;
		this.name = name;
	}
	
	/**
	 * @return the Subform Entity (if set, may be <b>null</b>
	 */
	public String getEntity() {
		return entity;
	}
	
	/**
	 * @param entity to set
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	/**
	 * @return the Name of the CollectableComponent (or SubformColumn if Entity is set)
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
